package moe.qwq.miko.tools

import java.io.File

object FileUtils {
    private val PicIdMap = hashMapOf(
        "jpg" to 1000,
        "bmp" to 1005,
        "gif" to 2000,
        "png" to 1001,
        "webp" to 1002,
        "sharpp" to 1004,
        "apng" to 2001,
    )

    fun getFileType(file: File): String {
        val bytes = ByteArray(2)
        file.inputStream().use {
            it.read(bytes)
        }
        return when ("${bytes[0].toUByte()}${bytes[1].toUByte()}".toInt()) {
            6677 -> "bmp"
            7173 -> "gif"
            7784 -> "midi"
            7790 -> "exe"
            8075 -> "zip"
            8273 -> "webp"
            8297 -> "rar"
            13780 -> "png"
            255216 -> "jpg"
            else -> "jpg"
        }
    }

    fun getPicType(file: File): Int {
        return PicIdMap[getFileType(file)] ?: 1000
    }
}