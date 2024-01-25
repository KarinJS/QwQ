@file:OptIn(DelicateCoroutinesApi::class, ExperimentalUnsignedTypes::class,
    ExperimentalSerializationApi::class
)
package moe.qwq.miko.internals

import com.tencent.qqnt.kernel.nativeinterface.JsonGrayBusiId
import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import kotlinx.io.core.readBytes
import kotlinx.io.core.readUInt
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import moe.fuqiuluo.entries.C2CRecallMessage
import moe.fuqiuluo.entries.Message
import moe.fuqiuluo.entries.MessageHead
import moe.fuqiuluo.entries.MessagePush
import moe.fuqiuluo.entries.GroupRecallMessage
import moe.fuqiuluo.entries.InfoSyncPush
import moe.qwq.miko.ext.ifNullOrEmpty
import moe.qwq.miko.internals.helper.AppRuntimeFetcher.appRuntime
import moe.qwq.miko.internals.helper.ContactHelper
import moe.qwq.miko.internals.helper.GroupHelper
import moe.qwq.miko.internals.helper.LocalGrayTips
import moe.qwq.miko.internals.setting.QwQSetting

object AioListener {

    fun onInfoSyncPush(infoSyncPush: InfoSyncPush): Boolean {
        if (infoSyncPush.type == 2) {
            infoSyncPush.syncContent?.body?.forEach { body ->
                body.msgs?.forEach {
                    val msgType = it.content.msgType
                    val subType = it.content.msgSubType
                    if (msgType == 528 && subType == 138) {
                        return QwQSetting.interceptRecall
                    } else if (msgType == 732 && subType == 17) {
                        return QwQSetting.interceptRecall
                    }
                }
            }
        }
        return false
    }

    fun onMsgPush(msgPush: MessagePush): Boolean {
        val msgType = msgPush.msgBody.content.msgType
        val subType = msgPush.msgBody.content.msgSubType

        return onMessage(msgType, subType, msgPush.msgBody)
    }

    private fun onMessage(msgType: Int, subType: Int, msgBody: Message): Boolean {
        return when(msgType) {
            528 -> when (subType) {
                138 -> onC2CRecall(msgBody.msgHead, msgBody.body!!.richMsg)
                else -> false
            }
            732 -> when (subType) {
                17 -> onGroupRecall(msgBody, msgBody.body!!.richMsg)
                else -> false
            }
            else -> false
        }
    }

    private fun onC2CRecall(msgHead: MessageHead, richMsg: ByteArray): Boolean {
        if (!QwQSetting.interceptRecall) return false
        GlobalScope.launch {
            val recallData = ProtoBuf.decodeFromByteArray<C2CRecallMessage>(richMsg)

            val senderUid = recallData.info.senderUid
            val receiverUid = recallData.info.receiverUid
            val msgSeq = recallData.info.msgSeq
            val msgUid = recallData.info.msgUid
            val msgTime = recallData.info.msgTime
            val wording = recallData.info.wording?.wording ?: ""

            val sender = ContactHelper.getUinByUidAsync(senderUid)
            val receiver = ContactHelper.getUinByUidAsync(receiverUid)

            val contact = ContactHelper.generateContact(
                chatType = MsgConstant.KCHATTYPEC2C,
                id = senderUid
            )
            LocalGrayTips.addLocalGrayTip(contact, JsonGrayBusiId.AIO_AV_C2C_NOTICE, LocalGrayTips.Align.CENTER) {
                text("对方尝试撤回自己的")
                msgRef("消息", msgSeq)
            }
        }
        return true
    }

    private fun onGroupRecall(message: Message, richMsg: ByteArray): Boolean {
        if (!QwQSetting.interceptRecall) return false
        GlobalScope.launch {
            val reader = ByteReadPacket(richMsg)
            val buffer = try {
                if (reader.readUInt() == message.msgHead.peerId.toUInt()) {
                    reader.discardExact(1)
                    reader.readBytes(reader.readShort().toInt())
                } else richMsg
            } finally {
                reader.release()
            }
            val recallData = ProtoBuf.decodeFromByteArray<GroupRecallMessage>(buffer)

            val groupCode = GroupHelper.groupUin2GroupCode(message.msgHead.peerId)
            val msgUid = message.content.msgUid
            val targetUid = recallData.operation.msgInfo?.senderUid ?: ""
            val operatorUid = recallData.operation.operatorUid ?: ""
            val msgSeq = recallData.operation.msgInfo?.msgSeq ?: 0L
            val wording = recallData.operation.wording?.wording ?: ""

            if (operatorUid == appRuntime.currentUid) return@launch

            val target = ContactHelper.getUinByUidAsync(targetUid)
            val operator = ContactHelper.getUinByUidAsync(operatorUid)

            val targetInfo = if (targetUid.isEmpty()) null else GroupHelper.getTroopMemberInfoByUin(groupCode.toString(), target).getOrNull()
            val targetNick = targetInfo?.troopnick
                .ifNullOrEmpty(targetInfo?.friendnick) ?: targetUid
            val operatorInfo = if (operatorUid.isEmpty()) null else GroupHelper.getTroopMemberInfoByUin(groupCode.toString(), operator).getOrNull()
            val operatorNick = operatorInfo?.troopnick
                .ifNullOrEmpty(operatorInfo?.friendnick) ?: operatorUid

            val contact = ContactHelper.generateContact(
                chatType = MsgConstant.KCHATTYPEGROUP,
                id = groupCode.toString()
            )
            LocalGrayTips.addLocalGrayTip(contact, JsonGrayBusiId.AIO_AV_GROUP_NOTICE, LocalGrayTips.Align.CENTER) {
                member(operatorUid, operator, operatorNick, "3")
                text("尝试撤回")
                if (targetUid == operatorUid) {
                    text("自己")
                } else {
                    member(targetUid, target, targetNick, "3")
                }
                text("的")
                msgRef("消息", msgSeq)
            }
        }
        return true
    }
}