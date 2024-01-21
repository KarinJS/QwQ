@file:OptIn(DelicateCoroutinesApi::class, ExperimentalUnsignedTypes::class,
    ExperimentalSerializationApi::class
)
package moe.qwq.miko.internals

import de.robv.android.xposed.XposedBridge
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import kotlinx.io.core.readBytes
import kotlinx.io.core.readUInt
import kotlinx.io.core.readULong
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import moe.fuqiuluo.entries.MessageHead
import moe.fuqiuluo.entries.MessagePush
import moe.fuqiuluo.entries.RecallMessage
import moe.qwq.miko.ext.toHexString
import moe.qwq.miko.tools.QwQSetting

object AioListener {

    fun onMsgPush(msgPush: MessagePush): Boolean {
        val msgType = msgPush.msgBody.content.msgType
        val subType = msgPush.msgBody.content.msgSubType
        val msgHead = msgPush.msgBody.msgHead

        return when(msgType) {
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
                138 -> onC2CRecall(msgHead, msgPush.msgBody.body.richMsg)
                //290 -> onC2cPoke(msgTime, pb)
                else -> false
            }
            732 -> when (subType) {
                //12 -> onGroupBan(msgTime, pb)
                //16 -> onGroupTitleChange(msgTime, pb)
                17 -> onGroupRecall(msgHead, msgPush.msgBody.body.richMsg)
                //20 -> onGroupPokeAndGroupSign(msgTime, pb)
                //21 -> onEssenceMessage(msgTime, pb)
                else -> false
            }

            else -> false
        }
    }

    private fun onC2CRecall(msgHead: MessageHead, richMsg: ByteArray): Boolean {
        GlobalScope.launch {

        }
        return QwQSetting.interceptRecall
    }

    private fun onGroupRecall(msgHead: MessageHead, richMsg: ByteArray): Boolean {
        GlobalScope.launch {
            val reader = ByteReadPacket(richMsg)
            val buffer = try {
                if (reader.readUInt() == msgHead.peerId.toUInt()) {
                    reader.discardExact(1)
                    reader.readBytes(reader.readShort().toInt())
                } else richMsg
            } finally {
                reader.release()
            }
            val recallData = ProtoBuf.decodeFromByteArray<RecallMessage>(buffer)

            val groupCode = msgHead.peerId
            val targetUid = recallData.operation.msgInfo?.senderUid ?: "0"
            val operatorUid = recallData.operation.operatorUid ?: "0"
            val msgSeq = recallData.operation.msgInfo?.msgSeq ?: 0L
            val wording = recallData.operation.wording?.wording ?: ""


        }
        return QwQSetting.interceptRecall
    }
}