package moe.qwq.miko.internals.hijackers

import de.robv.android.xposed.XC_MethodHook

interface IHijacker {
    fun onHandle(
        param: XC_MethodHook.MethodHookParam,
        uin: String,
        cmd: String,
        seq: Int,
        buffer: ByteArray,
        bufferIndex: Int
    ): Boolean

    /**
     * @return the cmd of the packet
     */
    val command: String
}