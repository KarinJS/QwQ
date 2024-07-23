package moe.qwq.miko.utils

import kotlinx.io.core.toByteArray
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AesUtils {
    private val IV_AES = "5201314fuqiuluo!".toByteArray()

    fun md5(str: String): ByteArray {
        val md = MessageDigest.getInstance("MD5")
        val bytes = md.digest(str.toByteArray())
        return bytes
    }

    fun aesEncrypt(data: ByteArray, key: ByteArray): ByteArray {
        val mAlgorithmParameterSpec = IvParameterSpec(IV_AES)
        val mSecretKeySpec = SecretKeySpec(key, "AES")
        val mCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        mCipher.init(Cipher.ENCRYPT_MODE, mSecretKeySpec, mAlgorithmParameterSpec)
        return mCipher.doFinal(data)
    }

    fun aesDecrypt(data: ByteArray, key: ByteArray): ByteArray {
        val mAlgorithmParameterSpec = IvParameterSpec(IV_AES)
        val mSecretKeySpec = SecretKeySpec(key, "AES")
        val mCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        mCipher.init(Cipher.DECRYPT_MODE, mSecretKeySpec, mAlgorithmParameterSpec)
        return mCipher.doFinal(data)
    }
}