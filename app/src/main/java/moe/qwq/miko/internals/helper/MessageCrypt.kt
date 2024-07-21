@file:OptIn(ExperimentalSerializationApi::class)

package moe.qwq.miko.internals.helper

import android.graphics.BitmapFactory
import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import com.tencent.qqnt.kernel.nativeinterface.MsgElement
import com.tencent.qqnt.kernel.nativeinterface.PicElement
import com.tencent.qqnt.kernel.nativeinterface.QQNTWrapperUtil
import com.tencent.qqnt.kernel.nativeinterface.RichMediaFilePathInfo
import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.readBytes
import kotlinx.io.core.use
import kotlinx.io.core.writeFully
import kotlinx.io.streams.inputStream
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import moe.fuqiuluo.xposed.loader.LuoClassloader
import moe.qwq.miko.internals.entries.EcTextElem
import moe.qwq.miko.internals.setting.QwQSetting
import moe.qwq.miko.tools.FileUtils
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import androidx.exifinterface.media.ExifInterface
import de.robv.android.xposed.XposedBridge
import kotlinx.io.core.ByteReadPacket
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray

object MessageCrypt {
    private val IV_AES by lazy { "5201314fuqiuluo!".toByteArray() }
    private val randomPicDir by lazy {
        QwQSetting.dataDir.resolve("randomFaces").also {
            if (it.isFile) it.delete()
            if (!it.exists()) {
                it.mkdirs()
                val randomFaceFile = it.resolve("base.gif")
                randomFaceFile.outputStream().use {
                    LuoClassloader.moduleLoader.getResourceAsStream("assets/random_face.gif").use { origin ->
                        origin.copyTo(it)
                    }
                }
            }
        }
    }
    private val tmpPicDir by lazy {
        QwQSetting.dataDir.resolve("tmp").also {
            if (it.isFile) it.delete()
            if (!it.exists()) it.mkdirs()
        }
    }

    fun decrypt(data: ByteArray, keyStr: String): Result<ArrayList<MsgElement>> {
        val aesKey = md5(keyStr)
        val decrypt = aesDecrypt(data, aesKey)
        val reader = ByteReadPacket(decrypt)
        val result = ArrayList<MsgElement>()
        repeat(reader.readInt()) {
            when (val elementType = reader.readInt()) {
                MsgConstant.KELEMTYPETEXT -> {
                    val text = ProtoBuf.decodeFromByteArray<EcTextElem>(reader.readBytes(reader.readInt()))
                    result.add(text.toMsgElement())
                }
                else -> return Result.failure(RuntimeException("Unsupported msg type: $elementType"))
            }
        }
        return Result.success(result)
    }

    fun encrypt(msgs: ArrayList<MsgElement>, uin: String, keyStr: String): Result<MsgElement> {
        // 加上uin作为Hash判断，防止别人转发加密后的消息后被解密解密
        // 这种转发的加密消息不该被解密！
        val keyHash = (keyStr + uin).hashCode()
        val msgBuilder = BytePacketBuilder()
        msgBuilder.writeInt(msgs.size)
        msgs.forEach { msg ->
            msgBuilder.writeInt(msg.elementType)
            when (msg.elementType) {
                MsgConstant.KELEMTYPETEXT -> {
                    val encrypt = ProtoBuf.encodeToByteArray(EcTextElem.from(msg.textElement))
                    msgBuilder.writeInt(encrypt.size)
                    msgBuilder.writeFully(encrypt)
                }

                // TODO support more msg
                else -> return Result.failure(RuntimeException("Unsupported msg type: ${msg.elementType}"))
            }
        }

        val data = msgBuilder.build().readBytes()
        val aesKey = md5(keyStr)

        val builder = BytePacketBuilder()
        val encrypt = aesEncrypt(data, aesKey)
        builder.writeFully(encrypt)
        builder.writeInt(encrypt.size)
        builder.writeInt(keyHash)
        builder.writeInt(0x114514)

        val tmpFile = tmpPicDir.resolve("${System.currentTimeMillis()}.tmp")
        tmpFile.outputStream().use { out ->
            (randomPicDir
                .listFiles { f -> f.isFile }
                ?.random() ?: return Result.failure(RuntimeException("No random face found"))).inputStream().use {
                it.copyTo(out)
            }
            builder.build().inputStream().use { it.copyTo(out) }
        }

        val elem = MsgElement()
        elem.elementType = MsgConstant.KELEMTYPEPIC
        val pic = PicElement()
        pic.md5HexStr = QQNTWrapperUtil.CppProxy.genFileMd5Hex(tmpFile.absolutePath)

        val msgService = NTServiceFetcher.kernelService.msgService!!
        val originalPath = msgService.getRichMediaFilePathForMobileQQSend(RichMediaFilePathInfo(2, 0, pic.md5HexStr, tmpFile.name, 1, 0, null, "", true))

        //XposedBridge.log("${pic.md5HexStr} encrypt: $originalPath")

        if (!QQNTWrapperUtil.CppProxy.fileIsExist(originalPath) || QQNTWrapperUtil.CppProxy.getFileSize(originalPath) != tmpFile.length()) {
            val thumbPath = msgService.getRichMediaFilePathForMobileQQSend(RichMediaFilePathInfo(2, 0, pic.md5HexStr, tmpFile.name, 2, 720, null, "", true))
            QQNTWrapperUtil.CppProxy.copyFile(tmpFile.absolutePath, originalPath)
            QQNTWrapperUtil.CppProxy.copyFile(tmpFile.absolutePath, thumbPath)
        }

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(originalPath, options)
        val exifInterface = ExifInterface(originalPath!!)
        val orientation = exifInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )
        if (orientation != ExifInterface.ORIENTATION_ROTATE_90 && orientation != ExifInterface.ORIENTATION_ROTATE_270) {
            pic.picWidth = options.outWidth
            pic.picHeight = options.outHeight
        } else {
            pic.picWidth = options.outHeight
            pic.picHeight = options.outWidth
        }
        pic.sourcePath = originalPath
        pic.fileSize = QQNTWrapperUtil.CppProxy.getFileSize(originalPath)
        pic.original = true
        pic.picType = FileUtils.getPicType(tmpFile)
        pic.picSubType = 0
        pic.isFlashPic = false

        elem.picElement = pic
        tmpFile.delete()
        return Result.success(elem)
    }

    private fun md5(str: String): ByteArray {
        val md = MessageDigest.getInstance("MD5")
        val bytes = md.digest(str.toByteArray())
        return bytes
    }

    private fun aesEncrypt(data: ByteArray, key: ByteArray): ByteArray {
        val mAlgorithmParameterSpec = IvParameterSpec(IV_AES)
        val mSecretKeySpec = SecretKeySpec(key, "AES")
        val mCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        mCipher.init(Cipher.ENCRYPT_MODE, mSecretKeySpec, mAlgorithmParameterSpec)
        return mCipher.doFinal(data)
    }

    private fun aesDecrypt(data: ByteArray, key: ByteArray): ByteArray {
        val mAlgorithmParameterSpec = IvParameterSpec(IV_AES)
        val mSecretKeySpec = SecretKeySpec(key, "AES")
        val mCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        mCipher.init(Cipher.DECRYPT_MODE, mSecretKeySpec, mAlgorithmParameterSpec)
        return mCipher.doFinal(data)
    }
}