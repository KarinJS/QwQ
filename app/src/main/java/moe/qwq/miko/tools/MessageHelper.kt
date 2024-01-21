package moe.qwq.miko.tools

import com.tencent.imcore.message.BaseQQMessageFacade
import com.tencent.mobileqq.app.QQAppInterface
import com.tencent.mobileqq.data.MessageForGrayTips
import com.tencent.mobileqq.data.MessageRecord
import com.tencent.mobileqq.msg.api.IMessageRecordFactory
import com.tencent.mobileqq.qroute.QRoute
import moe.qwq.miko.internals.helper.AppRuntimeFetcher.appRuntime
import java.lang.reflect.Method

object MessageHelper {
    private lateinit var METHOD_ADD_LOCAL_MSG: Method

    fun generateGrayTips(peerId: String, targetId: String, isTroop: Int, msg: String, time: Long, msgSeq: Long, msgUid: Long, tipType: Int = MessageRecord.MSG_TYPE_TROOP_GAP_GRAY_TIPS): MessageForGrayTips {
        val msgFactory = QRoute.api(IMessageRecordFactory::class.java)
        val record = msgFactory.createMsgRecordByMsgType(tipType) as MessageForGrayTips
        record.init(appRuntime.account, peerId, targetId, msg, time, tipType, isTroop, time)
        record.msgUid = msgUid
        record.shmsgseq = msgSeq
        record.isread = true
        return record
    }

    fun addLocalMessage(record: MessageRecord) {
        val appInterface = appRuntime as QQAppInterface
        val messageFacade = appInterface.messageFacade
        if (!::METHOD_ADD_LOCAL_MSG.isInitialized) {
            METHOD_ADD_LOCAL_MSG = BaseQQMessageFacade::class.java.declaredMethods.first {
                it.parameterCount == 3 &&
                        it.parameterTypes[0] == List::class.java &&
                        it.parameterTypes[1] == String::class.java &&
                        it.parameterTypes[2] == java.lang.Boolean.TYPE
            }.apply {
                if (!isAccessible) isAccessible = true
            }
        }
        METHOD_ADD_LOCAL_MSG.invoke(messageFacade, listOf(record), appRuntime.currentUin, true)
    }
}