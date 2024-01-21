package moe.qwq.miko.internals.hijackers

import com.google.protobuf.InvalidProtocolBufferException
import com.tencent.qphone.base.remote.FromServiceMsg
import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import de.robv.android.xposed.XposedBridge
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import kotlinx.io.core.readBytes
import moe.fuqiuluo.proto.ProtoMap
import moe.fuqiuluo.proto.ProtoUtils
import moe.fuqiuluo.proto.asByteArray
import moe.fuqiuluo.proto.asInt
import moe.fuqiuluo.proto.asList
import moe.fuqiuluo.proto.asLong
import moe.fuqiuluo.proto.asMap
import moe.fuqiuluo.proto.asULong
import moe.fuqiuluo.proto.asUtf8String
import moe.qwq.miko.ext.slice
import moe.qwq.miko.ext.toHexString
import moe.qwq.miko.internals.broadcasts.broadcast
import moe.qwq.miko.internals.helper.AppRuntimeFetcher
import moe.qwq.miko.internals.receiver.MsgRecall
import moe.qwq.miko.tools.QwQSetting

object InfoSyncPush: IHijacker {
    override fun onHandle(fromServiceMsg: FromServiceMsg): Boolean {
        try {
            val pb = ProtoUtils.decodeFromByteArray(fromServiceMsg.wupBuffer.slice(4))
            if (!pb.has(3)) return false
            when(pb[3].asInt) {
                2 -> if (pb.has(8)) {
                    //XposedBridge.log(pb.toJson().toString())
                    //if (!pb.has(8, 4)) {
                    //    XposedBridge.log(pb.toByteArray().toHexString())
                    //    XposedBridge.log(fromServiceMsg.wupBuffer.toHexString())
                    //}
                    pb[8, 4, 8].asList.value.forEach {
                        if(onEventSync(it.asMap)) return true
                    }
                }
            }
        } catch (e: Throwable) {
            if (e is InvalidProtocolBufferException) {
                return true
            }
            XposedBridge.log(e)
        }
        return false
    }

    private fun onEventSync(pb: ProtoMap): Boolean {
        val msgType = pb[2, 1].asInt
        var subType = 0
        if (pb.has(2, 2) && pb.has(2, 3)) {
            subType = pb[2, 2].asInt
        }
        val msgTime = pb[2, 6].asLong
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

    private fun onGroupRecall(msgTime: Long, pb: ProtoMap): Boolean {
        return QwQSetting.interceptRecall
    }

    private fun onC2CRecall(msgTime: Long, pb: ProtoMap): Boolean {
        return QwQSetting.interceptRecall
    }

    override fun getCmd(): String = "trpc.msg.register_proxy.RegisterProxy.InfoSyncPush"
}