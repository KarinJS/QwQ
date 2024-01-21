@file:OptIn(DelicateCoroutinesApi::class, ExperimentalUnsignedTypes::class,
    ExperimentalSerializationApi::class
)
package moe.qwq.miko.internals

import com.tencent.qqnt.kernel.nativeinterface.JsonGrayBusiId
import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import de.robv.android.xposed.XposedBridge
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
import moe.fuqiuluo.entries.Message
import moe.fuqiuluo.entries.MessageHead
import moe.fuqiuluo.entries.MessagePush
import moe.fuqiuluo.entries.RecallMessage
import moe.qwq.miko.internals.helper.ContactHelper
import moe.qwq.miko.internals.helper.LocalGrayTips
import moe.qwq.miko.tools.MessageHelper
import moe.qwq.miko.tools.PlatformTools
import moe.qwq.miko.tools.QwQSetting
import mqq.app.MobileQQ
import kotlin.random.Random

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
                17 -> onGroupRecall(msgPush.msgBody, msgPush.msgBody.body.richMsg)
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

    private fun onGroupRecall(message: Message, richMsg: ByteArray): Boolean {
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
            val recallData = ProtoBuf.decodeFromByteArray<RecallMessage>(buffer)

            val groupCode = message.msgHead.peerId
            val msgUid = message.content.msgUid
            val targetUid = recallData.operation.msgInfo?.senderUid ?: "0"
            val operatorUid = recallData.operation.operatorUid ?: "0"
            val msgSeq = recallData.operation.msgInfo?.msgSeq ?: 0L
            val wording = recallData.operation.wording?.wording ?: ""

            val target = ContactHelper.getUinByUidAsync(targetUid)
            val operator = ContactHelper.getUinByUidAsync(operatorUid)

            runCatching {
                val contact = ContactHelper.generateContact(
                    chatType = MsgConstant.KCHATTYPEGROUP,
                    id = groupCode.toString()
                )
                LocalGrayTips.addLocalGrayTip(contact, JsonGrayBusiId.AIO_AV_GROUP_NOTICE, LocalGrayTips.Align.CENTER) {
                    member(operatorUid, operator, "操作者", "3")
                    text("尝试撤回")
                    member(targetUid, target, "目标", "3")
                    text("的")
                    msgRef("消息", msgSeq)
                    text("，但是失败了")
                    image("https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png", "Baidu")
                }
            }.onFailure {
                XposedBridge.log(it)
            }
        }
        return QwQSetting.interceptRecall
    }
}