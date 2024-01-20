package moe.qwq.miko.internals.hijackers

import com.tencent.qphone.base.remote.FromServiceMsg
import de.robv.android.xposed.XposedBridge
import moe.fuqiuluo.proto.ProtoMap
import moe.fuqiuluo.proto.ProtoUtils
import moe.fuqiuluo.proto.asInt
import moe.fuqiuluo.proto.asLong
import moe.qwq.miko.ext.slice
import moe.qwq.miko.tools.QwQSetting

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
            return true
        }
        return false
    }

    private fun onC2CRecall(msgTime: Long, pb: ProtoMap): Boolean {
        if (QwQSetting.interceptRecall) {
            return true
        }
        return false
    }

    override fun getCmd(): String = "trpc.msg.olpush.OlPushService.MsgPush"
}