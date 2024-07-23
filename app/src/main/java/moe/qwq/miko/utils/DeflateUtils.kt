package moe.qwq.miko.utils

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.Deflater
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.Inflater


object DeflateUtils {
    fun uncompress(inputByte: ByteArray?): ByteArray {
        var len: Int
        val infill = Inflater()
        infill.setInput(inputByte)
        val bos = ByteArrayOutputStream()
        val outByte = ByteArray(1024)
        try {
            while (!infill.finished()) {
                len = infill.inflate(outByte)
                if (len == 0) {
                    break
                }
                bos.write(outByte, 0, len)
            }
            infill.end()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                bos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return bos.toByteArray()
    }

    fun compress(inputByte: ByteArray?): ByteArray {
        var len: Int
        val defile = Deflater()
        defile.setInput(inputByte)
        defile.finish()
        val bos = ByteArrayOutputStream()
        val outputByte = ByteArray(1024)
        try {
            while (!defile.finished()) {
                len = defile.deflate(outputByte)
                bos.write(outputByte, 0, len)
            }
            defile.end()
        } finally {
            try {
                bos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return bos.toByteArray()
    }

    fun gzip(data: ByteArray): ByteArray {
        val input = ByteArrayInputStream(data)
        val outputStream = ByteArrayOutputStream()
        try {
            val cache = ByteArray(1024)
            val stream = GZIPOutputStream(outputStream)
            while (true) {
                val read = input.read(cache, 0, 1024)
                if (read == -1) {
                    break
                }
                stream.write(cache, 0, read)
            }
            stream.flush()
            stream.close()
            return outputStream.toByteArray()
        } finally {
            outputStream.close()
            input.close()
        }
    }

    fun ungzip(bytes: ByteArray): ByteArray {
        val out = ByteArrayOutputStream()
        val `in` = ByteArrayInputStream(bytes)
        try {
            val ungzip = GZIPInputStream(`in`)
            val buffer = ByteArray(256)
            var n: Int
            while (ungzip.read(buffer).also { n = it } >= 0) {
                out.write(buffer, 0, n)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return out.toByteArray()
    }
}