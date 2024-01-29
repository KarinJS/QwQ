@file:OptIn(DelicateCoroutinesApi::class, ExperimentalUnsignedTypes::class,
    ExperimentalSerializationApi::class
)
package moe.qwq.miko.internals

import com.tencent.qqnt.kernel.nativeinterface.BroadcastHelperTransNotifyInfo
import com.tencent.qqnt.kernel.nativeinterface.Contact
import com.tencent.qqnt.kernel.nativeinterface.ContactMsgBoxInfo
import com.tencent.qqnt.kernel.nativeinterface.CustomWithdrawConfig
import com.tencent.qqnt.kernel.nativeinterface.DevInfo
import com.tencent.qqnt.kernel.nativeinterface.DownloadRelateEmojiResultInfo
import com.tencent.qqnt.kernel.nativeinterface.EmojiNotifyInfo
import com.tencent.qqnt.kernel.nativeinterface.EmojiResourceInfo
import com.tencent.qqnt.kernel.nativeinterface.FileTransNotifyInfo
import com.tencent.qqnt.kernel.nativeinterface.FirstViewDirectMsgNotifyInfo
import com.tencent.qqnt.kernel.nativeinterface.FirstViewGroupGuildInfo
import com.tencent.qqnt.kernel.nativeinterface.FreqLimitInfo
import com.tencent.qqnt.kernel.nativeinterface.GroupFileListResult
import com.tencent.qqnt.kernel.nativeinterface.GroupGuildNotifyInfo
import com.tencent.qqnt.kernel.nativeinterface.GroupItem
import com.tencent.qqnt.kernel.nativeinterface.GuildInteractiveNotificationItem
import com.tencent.qqnt.kernel.nativeinterface.GuildMsgAbFlag
import com.tencent.qqnt.kernel.nativeinterface.GuildNotificationAbstractInfo
import com.tencent.qqnt.kernel.nativeinterface.HitRelatedEmojiWordsResult
import com.tencent.qqnt.kernel.nativeinterface.IKernelMsgListener
import com.tencent.qqnt.kernel.nativeinterface.ImportOldDbMsgNotifyInfo
import com.tencent.qqnt.kernel.nativeinterface.InputStatusInfo
import com.tencent.qqnt.kernel.nativeinterface.JsonGrayBusiId
import com.tencent.qqnt.kernel.nativeinterface.KickedInfo
import com.tencent.qqnt.kernel.nativeinterface.MsgAbstract
import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import com.tencent.qqnt.kernel.nativeinterface.MsgElement
import com.tencent.qqnt.kernel.nativeinterface.MsgRecord
import com.tencent.qqnt.kernel.nativeinterface.MsgSetting
import com.tencent.qqnt.kernel.nativeinterface.PicElement
import com.tencent.qqnt.kernel.nativeinterface.RecvdOrder
import com.tencent.qqnt.kernel.nativeinterface.RelatedWordEmojiInfo
import com.tencent.qqnt.kernel.nativeinterface.SearchGroupFileResult
import com.tencent.qqnt.kernel.nativeinterface.TabStatusInfo
import com.tencent.qqnt.kernel.nativeinterface.TempChatInfo
import com.tencent.qqnt.kernel.nativeinterface.UnreadCntInfo
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
import java.util.ArrayList
import java.util.HashMap

object AioListener: IKernelMsgListener {
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
        if (picElement.isFlashPic == false) return
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
    }

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

            if (senderUid == appRuntime.currentUid) return@launch

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

    override fun onAddSendMsg(msgRecord: MsgRecord?) {
        
    }

    override fun onBroadcastHelperDownloadComplete(broadcastHelperTransNotifyInfo: BroadcastHelperTransNotifyInfo?) {
        
    }

    override fun onBroadcastHelperProgerssUpdate(broadcastHelperTransNotifyInfo: BroadcastHelperTransNotifyInfo?) {
        
    }

    override fun onChannelFreqLimitInfoUpdate(
        contact: Contact?,
        z: Boolean,
        freqLimitInfo: FreqLimitInfo?
    ) {

    }

    override fun onContactUnreadCntUpdate(hashMap: HashMap<Int, HashMap<String, UnreadCntInfo>>?) {
        
    }

    override fun onCustomWithdrawConfigUpdate(customWithdrawConfig: CustomWithdrawConfig?) {
        
    }

    override fun onDraftUpdate(contact: Contact?, arrayList: ArrayList<MsgElement>?, j2: Long) {
        
    }

    override fun onEmojiDownloadComplete(emojiNotifyInfo: EmojiNotifyInfo?) {
        
    }

    override fun onEmojiResourceUpdate(emojiResourceInfo: EmojiResourceInfo?) {
        
    }

    override fun onFeedEventUpdate(firstViewDirectMsgNotifyInfo: FirstViewDirectMsgNotifyInfo?) {
        
    }

    override fun onFileMsgCome(arrayList: ArrayList<MsgRecord>?) {
        
    }

    override fun onFirstViewDirectMsgUpdate(firstViewDirectMsgNotifyInfo: FirstViewDirectMsgNotifyInfo?) {
        
    }

    override fun onFirstViewGroupGuildMapping(arrayList: ArrayList<FirstViewGroupGuildInfo>?) {
        
    }

    override fun onGrabPasswordRedBag(
        i2: Int,
        str: String?,
        i3: Int,
        recvdOrder: RecvdOrder?,
        msgRecord: MsgRecord?
    ) {
        
    }

    override fun onGroupFileInfoAdd(groupItem: GroupItem?) {
        
    }

    override fun onGroupFileInfoUpdate(groupFileListResult: GroupFileListResult?) {
        
    }

    override fun onGroupGuildUpdate(groupGuildNotifyInfo: GroupGuildNotifyInfo?) {
        
    }

    override fun onGroupTransferInfoAdd(groupItem: GroupItem?) {
        
    }

    override fun onGroupTransferInfoUpdate(groupFileListResult: GroupFileListResult?) {
        
    }

    override fun onGuildInteractiveUpdate(guildInteractiveNotificationItem: GuildInteractiveNotificationItem?) {
        
    }

    override fun onGuildMsgAbFlagChanged(guildMsgAbFlag: GuildMsgAbFlag?) {

    }

    override fun onGuildNotificationAbstractUpdate(guildNotificationAbstractInfo: GuildNotificationAbstractInfo?) {
        
    }

    override fun onHitCsRelatedEmojiResult(downloadRelateEmojiResultInfo: DownloadRelateEmojiResultInfo?) {
        
    }

    override fun onHitEmojiKeywordResult(hitRelatedEmojiWordsResult: HitRelatedEmojiWordsResult?) {
        
    }

    override fun onHitRelatedEmojiResult(relatedWordEmojiInfo: RelatedWordEmojiInfo?) {
        
    }

    override fun onImportOldDbProgressUpdate(importOldDbMsgNotifyInfo: ImportOldDbMsgNotifyInfo?) {
        
    }

    override fun onInputStatusPush(inputStatusInfo: InputStatusInfo?) {
        
    }

    override fun onKickedOffLine(kickedInfo: KickedInfo?) {
        
    }

    override fun onLineDev(arrayList: ArrayList<DevInfo>?) {
        
    }

    override fun onLogLevelChanged(j2: Long) {
        
    }

    override fun onMsgAbstractUpdate(arrayList: ArrayList<MsgAbstract>?) {
        
    }

    override fun onMsgBoxChanged(arrayList: ArrayList<ContactMsgBoxInfo>?) {
        
    }

    override fun onMsgDelete(contact: Contact?, arrayList: ArrayList<Long>?) {
        
    }

    override fun onMsgEventListUpdate(hashMap: HashMap<String, ArrayList<Long>>?) {
        
    }

    override fun onMsgInfoListAdd(arrayList: ArrayList<MsgRecord>?) {
        
    }

    override fun onMsgInfoListUpdate(arrayList: ArrayList<MsgRecord>?) {
        
    }

    override fun onMsgQRCodeStatusChanged(i2: Int) {
        
    }

    override fun onMsgRecall(i2: Int, str: String?, j2: Long) {
        
    }

    override fun onMsgSecurityNotify(msgRecord: MsgRecord?) {
        
    }

    override fun onMsgSettingUpdate(msgSetting: MsgSetting?) {
        
    }

    override fun onNtFirstViewMsgSyncEnd() {
        
    }

    override fun onNtMsgSyncEnd() {
        
    }

    override fun onNtMsgSyncStart() {
        
    }

    override fun onReadFeedEventUpdate(firstViewDirectMsgNotifyInfo: FirstViewDirectMsgNotifyInfo?) {
        
    }

    override fun onRecvGroupGuildFlag(i2: Int) {
        
    }

    override fun onRecvMsgSvrRspTransInfo(
        j2: Long,
        contact: Contact?,
        i2: Int,
        i3: Int,
        str: String?,
        bArr: ByteArray?
    ) {
        
    }

    override fun onRecvOnlineFileMsg(arrayList: ArrayList<MsgRecord>?) {
        
    }

    override fun onRecvS2CMsg(arrayList: ArrayList<Byte>?) {
        
    }

    override fun onRecvSysMsg(arrayList: ArrayList<Byte>?) {
        
    }

    override fun onRecvUDCFlag(i2: Int) {
        
    }

    override fun onRichMediaDownloadComplete(fileTransNotifyInfo: FileTransNotifyInfo?) {
        
    }

    override fun onRichMediaProgerssUpdate(fileTransNotifyInfo: FileTransNotifyInfo?) {
        
    }

    override fun onRichMediaUploadComplete(fileTransNotifyInfo: FileTransNotifyInfo?) {
        
    }

    override fun onSearchGroupFileInfoUpdate(searchGroupFileResult: SearchGroupFileResult?) {
        
    }

    override fun onSendMsgError(j2: Long, contact: Contact?, i2: Int, str: String?) {
        
    }

    override fun onSysMsgNotification(i2: Int, j2: Long, j3: Long, arrayList: ArrayList<Byte>?) {
        
    }

    override fun onTempChatInfoUpdate(tempChatInfo: TempChatInfo?) {
        
    }

    override fun onUnreadCntAfterFirstView(hashMap: HashMap<Int, ArrayList<UnreadCntInfo>>?) {
        
    }

    override fun onUnreadCntUpdate(hashMap: HashMap<Int, ArrayList<UnreadCntInfo>>?) {
        
    }

    override fun onUserChannelTabStatusChanged(z: Boolean) {
        
    }

    override fun onUserOnlineStatusChanged(z: Boolean) {
        
    }

    override fun onUserTabStatusChanged(arrayList: ArrayList<TabStatusInfo>?) {
        
    }

    override fun onlineStatusBigIconDownloadPush(i2: Int, j2: Long, str: String?) {
        
    }

    override fun onlineStatusSmallIconDownloadPush(i2: Int, j2: Long, str: String?) {
        
    }
}