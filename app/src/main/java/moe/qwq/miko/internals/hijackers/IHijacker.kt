package moe.qwq.miko.internals.hijackers

import com.tencent.qphone.base.remote.ToServiceMsg

interface IHijacker {
    fun onHandle(toServiceMsg: ToServiceMsg, isPb: Boolean): Boolean

    /**
     * @return the cmd of the packet
     */
    val command: String
}