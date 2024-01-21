package moe.qwq.miko.internals.hijackers

import com.tencent.qphone.base.remote.FromServiceMsg
import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import kotlinx.io.core.readBytes
import moe.fuqiuluo.proto.ProtoMap
import moe.fuqiuluo.proto.ProtoUtils
import moe.fuqiuluo.proto.asByteArray
import moe.fuqiuluo.proto.asInt
import moe.fuqiuluo.proto.asList
import moe.fuqiuluo.proto.asLong
import moe.fuqiuluo.proto.asULong
import moe.fuqiuluo.proto.asUtf8String
import moe.qwq.miko.ext.slice
import moe.qwq.miko.internals.broadcasts.broadcast
import moe.qwq.miko.internals.helper.AppRuntimeFetcher
import moe.qwq.miko.internals.receiver.MsgRecall
import moe.qwq.miko.tools.QwQSetting

object InfoSyncPush: IHijacker {
    override fun onHandle(fromServiceMsg: FromServiceMsg): Boolean {
        try {
            val pb = ProtoUtils.decodeFromByteArray(fromServiceMsg.wupBuffer.slice(4))
            when(pb[3].asInt) {
                2 -> {
                    if (pb.has(8)) {
                        val msgType = pb[8, 4, 8, 2, 1].asInt
                        var subType = 0
                        if (pb.has(8, 4, 8, 2, 2) && pb.has(8, 4, 8, 2, 3)) {
                            subType = pb[8, 4, 8, 2, 2].asInt
                        }
                        val msgTime = pb[8, 4, 8, 2, 6].asLong
                        return when (msgType) {
                            528 -> when (subType) {
                                138 -> onC2CRecall(msgTime, pb)
                                else -> false
                            }

                            732 -> when (subType) {
                                17 -> onGroupRecall(msgTime, pb)
                                else -> false
                            }

                            else -> false
                        }
                    }
                }
            }
        } catch (_: Throwable) {}
        return false
    }

    private fun onGroupRecall(msgTime: Long, pb: ProtoMap): Boolean {
        return QwQSetting.interceptRecall
    }

    private fun onC2CRecall(msgTime: Long, pb: ProtoMap): Boolean {
        return QwQSetting.interceptRecall
    }

    override fun getCmd(): String = "trpc.msg.register_proxy.RegisterProxy.InfoSyncPush"
}