package moe.qwq.miko.internals.hijackers

import com.tencent.qphone.base.remote.FromServiceMsg

interface IHijacker {

    /**
     * @return true If QQ is blocked from receiving this packet
     */
    fun onHandle(fromServiceMsg: FromServiceMsg): Boolean

    /**
     * @return the cmd of the packet
     */
    fun getCmd(): String
}