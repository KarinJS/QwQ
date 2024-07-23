@file:OptIn(DelicateCoroutinesApi::class, ObsoleteCoroutinesApi::class)
package moe.qwq.miko.tools

import de.robv.android.xposed.XposedBridge
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import java.io.File
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.minutes

object DownloadUtils {
    val GlobalClient: HttpClient by lazy {
        HttpClient {
            //install(HttpCookies)
            install(HttpTimeout) {
                requestTimeoutMillis = 15000
                connectTimeoutMillis = 15000
                socketTimeoutMillis = 15000
            }
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                })
            }
        }
    }
    private const val MAX_THREAD = 4

    suspend fun download(
        urlAdr: String,
        dest: File,
        threadCount: Int = MAX_THREAD,
        headers: Map<String, String> = mapOf()
    ): Boolean {
        if (!dest.exists()) {
            dest.createNewFile()
        }
        var threadCnt = if(threadCount == 0) MAX_THREAD else threadCount
        val url = URL(urlAdr)
        val connection = withContext(Dispatchers.IO) { url.openConnection() } as HttpURLConnection
        headers.forEach { (k, v) ->
            connection.setRequestProperty(k, v)
        }
        connection.requestMethod = "GET"
        connection.connectTimeout = 5000
        val responseCode = connection.responseCode
        if (responseCode == 200) {
            val contentLength = connection.contentLength
            if (contentLength <= 0) {
                return downloadByKtor(url, dest)
            } else {
                withContext(Dispatchers.IO) {
                    val raf = RandomAccessFile(dest, "rw")
                    raf.setLength(contentLength.toLong())
                    raf.close()
                }
            }
            if (contentLength <= 1024 * 1024) {
                threadCnt = 1
            }
            var blockSize = (contentLength * (1.0 / threadCnt)).roundToInt()
            connection.disconnect()
            val progress = atomic(0)
            val channel = Channel<Int>()
            var processed = 0
            repeat(threadCnt) {
                if (processed + blockSize != contentLength && it == threadCnt - 1) {
                    blockSize = contentLength - processed
                }
                val start = processed
                val end = processed + blockSize - 1
                GlobalScope.launch(Dispatchers.IO) {
                    reallyDownload(url, start, end, dest, channel)
                }
                processed += blockSize
            }
            withTimeoutOrNull(1.minutes) {
                while (progress.value < contentLength) {
                    if(progress.addAndGet(channel.receive()) >= contentLength) {
                        break
                    }
                }
                return@withTimeoutOrNull true
            } ?: dest.delete()
            return true
        }
        return false
    }

    private suspend fun downloadByKtor(url: URL, dest: File): Boolean {
        val respond = GlobalClient.get(url)
        if (respond.status == HttpStatusCode.OK) {
            val channel = respond.bodyAsChannel()
            withContext(Dispatchers.IO) {
                dest.outputStream().use {
                    channel.toInputStream().use { input ->
                        input.copyTo(it)
                    }
                }
            }
            return true
        } else {
            XposedBridge.log("[QwQ] Download failed: ${respond.status}")
        }
        return false
    }

    private suspend fun reallyDownload(url: URL, start: Int, end: Int, dest: File, channel: Channel<Int>) {
        val openConnection: HttpURLConnection = withContext(Dispatchers.IO) { url.openConnection() } as HttpURLConnection
        openConnection.requestMethod = "GET"
        openConnection.connectTimeout = 5000
        openConnection.setRequestProperty("range", "bytes=$start-$end")
        val responseCode = openConnection.responseCode
        if (responseCode == 206) {
            val inputStream = openConnection.inputStream
            val raf = withContext(Dispatchers.IO) {
                RandomAccessFile(dest, "rw").also {
                    it.seek(start.toLong())
                }
            }
            var len: Int
            val buf = ByteArray(1024)
            var flag = true
            while (flag) {
                len = withContext(Dispatchers.IO) {
                    inputStream.read(buf)
                }
                flag = len != -1
                if (flag) {
                    withContext(Dispatchers.IO) {
                        raf.write(buf, 0, len)
                    }
                    channel.send(len)
                }
            }
            withContext(Dispatchers.IO) {
                inputStream.close()
                raf.close()
            }
        }
        openConnection.disconnect()
    }

}