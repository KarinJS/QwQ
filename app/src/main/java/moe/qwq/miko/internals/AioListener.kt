@file:OptIn(DelicateCoroutinesApi::class, ExperimentalUnsignedTypes::class,
    ExperimentalSerializationApi::class
)
package moe.qwq.miko.internals

import com.google.protobuf.UnknownFieldSet
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
import moe.fuqiuluo.entries.GroupRecallMessage
import moe.fuqiuluo.entries.MessagePush
import moe.qwq.miko.ext.ifNullOrEmpty
import moe.qwq.miko.internals.helper.ContactHelper
import moe.qwq.miko.internals.helper.GroupHelper
import moe.qwq.miko.internals.helper.LocalGrayTips
import moe.qwq.miko.internals.setting.QwQSetting

object AioListener {
/*TODO TRY FIX GRAYTIP FOR FLASH PIC
override fun onRecvMsg(recordLisrt: ArrayList<MsgRecord>) {
        recordLisrt.forEach { record ->
            GlobalScope.launch {
                if (record.elements.size == 1
                    && record.elements.first().elementType == MsgConstant.KELEMTYPEPIC) {
                    onFlashPic(record, record.elements.first().picElement)
                }
            }
        }
    }

    private suspend fun onFlashPic(record: MsgRecord, picElement: PicElement) {
        if (picElement.isFlashPic == null || picElement.isFlashPic == false) return
        if (picElement.original) return
        //if (picElement.picType != 0) return
        val peer = if (record.chatType == MsgConstant.KCHATTYPEGROUP)
            record.peerUin
        else record.senderUin
        val busiId = if (record.chatType == MsgConstant.KCHATTYPEGROUP)
            JsonGrayBusiId.AIO_AV_GROUP_NOTICE else JsonGrayBusiId.AIO_AV_C2C_NOTICE
        val contact = ContactHelper.generateContact(record.chatType, peer.toString())

        LocalGrayTips.addLocalGrayTip(contact, busiId, LocalGrayTips.Align.CENTER) {
            text("对方发送了一个")
            msgRef("闪照", record.msgSeq)
        }
    }*/

    fun onInfoSyncPush(fieldSet: UnknownFieldSet): Result<UnknownFieldSet> {
        val type = fieldSet.getField(3)
        if (!type.varintList.any { it == 2L }) {
            return Result.success(fieldSet)
        }

        val interceptRecall = QwQSetting.getSetting<Boolean>(QwQSetting.INTERCEPT_RECALL)
            .getValue(null, null)
        val builder = UnknownFieldSet.newBuilder(fieldSet)
        builder.clearField(8) // 移除content的内容

        val contentsBuilder = UnknownFieldSet.Field.newBuilder()
        val contents = fieldSet.getField(8)
        contents.groupList.forEach { content ->
            var isRecallEvent = false
            val bodies = content.getField(4)
            bodies.groupList.forEach { body ->
                val msgs = body.getField(8)
                msgs.groupList.forEach { msg ->
                    val msgHead = msg.getField(2).groupList.first()
                    val msgType = msgHead.getField(1).varintList.first()
                    val msgSubType = msgHead.getField(2).varintList.first()
                    isRecallEvent = (msgType == 528L && msgSubType == 138L) || (msgType == 732L && msgSubType == 17L) && interceptRecall
                }
            }
            if (!isRecallEvent) {
                contentsBuilder.addGroup(content)
            }
        }

        builder.addField(8, contentsBuilder.build())
        return Result.success(builder.build())
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
        val interceptRecall = QwQSetting.getSetting<Boolean>(QwQSetting.INTERCEPT_RECALL)
            .getValue(null, null)
        if (!interceptRecall) return false
        GlobalScope.launch {
            val recallData = ProtoBuf.decodeFromByteArray<C2CRecallMessage>(richMsg)

            val senderUid = recallData.info.senderUid
            val receiverUid = recallData.info.receiverUid
            val msgSeq = recallData.info.msgSeq
            val msgClientSeq = recallData.info.msgClientSeq
            val msgUid = recallData.info.msgUid
            val msgTime = recallData.info.msgTime
            val wording = recallData.info.wording?.wording ?: ""

            if (senderUid == QQInterfaces.app.currentUid) return@launch

            val sender = ContactHelper.getUinByUidAsync(senderUid)
            val receiver = ContactHelper.getUinByUidAsync(receiverUid)

            val contact = ContactHelper.generateContact(
                chatType = MsgConstant.KCHATTYPEC2C,
                id = senderUid
            )
            LocalGrayTips.addLocalGrayTip(contact, JsonGrayBusiId.AIO_AV_C2C_NOTICE, LocalGrayTips.Align.CENTER) {
                text("对方尝试撤回一条")
                msgRef("消息", msgSeq)
            }
        }
        return true
    }

    private fun onGroupRecall(message: Message, richMsg: ByteArray): Boolean {
        val interceptRecall = QwQSetting.getSetting<Boolean>(QwQSetting.INTERCEPT_RECALL)
            .getValue(null, null)
        if (!interceptRecall) return false
        GlobalScope.launch {
            val reader = ByteReadPacket(richMsg)
            val buffer = try {
                if (reader.readUInt() == message.msgHead.peerId) {
                    reader.discardExact(1)
                    reader.readBytes(reader.readShort().toInt())
                } else richMsg
            } finally {
                reader.release()
            }
            val recallData = ProtoBuf.decodeFromByteArray<GroupRecallMessage>(buffer)

            if (recallData.type != 7u || recallData.peerId == 0u) return@launch

            val groupCode = recallData.peerId.toLong()
            val msgUid = message.content.msgUid
            val targetUid = recallData.operation.msgInfo?.senderUid ?: ""
            val operatorUid = recallData.operation.operatorUid ?: ""
            val msgSeq = recallData.operation.msgInfo?.msgSeq ?: 0L
            val wording = recallData.operation.wording?.wording ?: ""

            if (operatorUid == QQInterfaces.app.currentUid) return@launch

            val target = ContactHelper.getUinByUidAsync(targetUid)
            val operator = ContactHelper.getUinByUidAsync(operatorUid)

            /*var targetNick = GroupHelper.getTroopMemberNickByUin(groupCode, target.toLong())?.let {
                it.troopNick
                    .ifNullOrEmpty(it.friendNick)
                    .ifNullOrEmpty(it.showName)
                    .ifNullOrEmpty(it.autoRemark)
            }*/

            var targetNick: String? = null
            if (targetNick == null) {
                targetNick = (if (targetUid.isEmpty()) null else GroupHelper.getTroopMemberInfoByUin(groupCode, target.toLong()).getOrNull())?.let {
                    it.troopnick.ifNullOrEmpty { it.friendnick }
                } ?: targetUid
            }


/*            var operatorNick = GroupHelper.getTroopMemberNickByUin(groupCode, operator.toLong())?.let {
                it.troopNick
                    .ifNullOrEmpty(it.friendNick)
                    .ifNullOrEmpty(it.showName)
                    .ifNullOrEmpty(it.autoRemark)
            }*/
            var operatorNick: String? = null


            if (operatorNick == null) {
                operatorNick = (if (operatorUid.isEmpty()) null else GroupHelper.getTroopMemberInfoByUin(groupCode, operator.toLong()).getOrNull())?.let {
                    it.troopnick.ifNullOrEmpty { it.friendnick }
                } ?: operatorUid
            }

            //XposedBridge.log("targetNick: $targetNick, operatorNick: $operatorNick, onGroupRecall")

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