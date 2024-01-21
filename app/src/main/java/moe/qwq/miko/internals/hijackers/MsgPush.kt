package moe.qwq.miko.internals.hijackers

import com.tencent.mobileqq.qroute.QRoute
import com.tencent.qphone.base.remote.FromServiceMsg
import com.tencent.qqnt.kernel.nativeinterface.IOperateCallback
import com.tencent.qqnt.kernel.nativeinterface.LocalGrayTipElement
import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import com.tencent.qqnt.msg.api.IMsgService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
import moe.qwq.miko.ext.toast
import moe.qwq.miko.internals.broadcasts.broadcast
import moe.qwq.miko.internals.helper.AppRuntimeFetcher
import moe.qwq.miko.internals.helper.ContactHelper
import moe.qwq.miko.internals.receiver.MsgRecall
import moe.qwq.miko.tools.QwQSetting
import mqq.app.AppRuntime
import mqq.app.MobileQQ

object MsgPush: IHijacker {
    override fun onHandle(fromServiceMsg: FromServiceMsg): Boolean {
        try {
            val pb = ProtoUtils.decodeFromByteArray(fromServiceMsg.wupBuffer.slice(4))
            if (
                !pb.has(1, 3)
                || !pb.has(1, 2)
                || !pb.has(1, 2, 6)
            ) return false
            val msgType = pb[1, 2, 1].asInt
            var subType = 0
            if (pb.has(1, 2, 3) && pb.has(1, 2, 2)) {
                subType = pb[1, 2, 2].asInt
            }
            val msgTime = pb[1, 2, 6].asLong
            return when (msgType) {
                //33 -> onGroupMemIncreased(msgTime, pb)
                //34 -> onGroupMemberDecreased(msgTime, pb)
                //44 -> onGroupAdminChange(msgTime, pb)
                //84 -> onGroupApply(msgTime, pb)
                //87 -> onInviteGroup(msgTime, pb)
                528 -> when (subType) {
                    //35 -> onFriendApply(msgTime, pb)
                    //39 -> onCardChange(msgTime, pb)
                    // invite
                    //68 -> onGroupApply(msgTime, pb)
                    138 -> onC2CRecall(msgTime, pb)
                    //290 -> onC2cPoke(msgTime, pb)
                    else -> false
                }

                732 -> when (subType) {
                    //12 -> onGroupBan(msgTime, pb)
                    //16 -> onGroupTitleChange(msgTime, pb)
                    17 -> onGroupRecall(msgTime, pb)
                    //20 -> onGroupPokeAndGroupSign(msgTime, pb)
                    //21 -> onEssenceMessage(msgTime, pb)
                    else -> false
                }

                else -> false
            }
        } catch (_: Throwable) {
        }
        return false
    }

    private fun onGroupRecall(msgTime: Long, pb: ProtoMap): Boolean {
        if (QwQSetting.interceptRecall) {
            var detail = pb[1, 3, 2]
            if (detail !is ProtoMap) {
                try {
                    val readPacket = ByteReadPacket(detail.asByteArray)
                    readPacket.discardExact(4)
                    readPacket.discardExact(1)
                    detail = ProtoUtils.decodeFromByteArray(readPacket.readBytes(readPacket.readShort().toInt()))
                    readPacket.release()
                } catch (e: Exception) {
                    return false
                }
            }
            val groupCode: Long = try {
                detail[4].asULong
            }catch (e: ClassCastException){
                detail[4].asList.value[0].asULong
            }
            val operatorUid = detail[11, 1].asUtf8String
            val targetUid = detail[11, 3, 6].asUtf8String
            val msgSeq = detail[11, 3, 1].asLong
            val tipText = if (detail.has(11, 9)) detail[11, 9, 2].asUtf8String else ""

            MsgRecall(
                chatType = MsgConstant.KCHATTYPEGROUP,
                peerId = groupCode.toString(),
                operatorUid = operatorUid,
                targetUid = targetUid,
                msgSeq = msgSeq,
                tipText = tipText
            ).broadcast()

            return true
        }
        return false
    }

    private fun onC2CRecall(msgTime: Long, pb: ProtoMap): Boolean {
        if (QwQSetting.interceptRecall) {
            val operationUid = pb[1, 3, 2, 1, 1].asUtf8String
            val msgSeq = pb[1, 3, 2, 1, 20].asLong
            val tipText = if (pb.has(1, 3, 2, 1, 13)) pb[1, 3, 2, 1, 13, 2].asUtf8String else ""

            MsgRecall(
                chatType = MsgConstant.KCHATTYPEC2C,
                peerId = operationUid,
                operatorUid = operationUid,
                targetUid = AppRuntimeFetcher.appRuntime.currentUid,
                msgSeq = msgSeq,
                tipText = tipText
            ).broadcast()

            return true
        }
        return false
    }

    override fun getCmd(): String = "trpc.msg.olpush.OlPushService.MsgPush"
}