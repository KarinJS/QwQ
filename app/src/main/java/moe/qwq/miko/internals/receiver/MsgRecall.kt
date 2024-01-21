package moe.qwq.miko.internals.receiver

import android.content.Context
import android.content.Intent
import com.tencent.mobileqq.qroute.QRoute
import com.tencent.qqnt.kernel.nativeinterface.GrayTipElement
import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import com.tencent.qqnt.kernel.nativeinterface.MsgElement
import com.tencent.qqnt.kernel.nativeinterface.RevokeElement
import com.tencent.qqnt.msg.api.IMsgService
import de.robv.android.xposed.XposedBridge
import moe.qwq.miko.internals.broadcasts.DynamicReceiver
import moe.qwq.miko.internals.helper.ContactHelper

@BroadcastReceiver(ReceiveScope.MSF)
object MsgRecall: IBroadcastReceiver {
    override suspend fun onReceive(context: Context, intent: Intent) {
        val chatType = intent.getIntExtra("chatType", 0)
        val peerId = intent.getStringExtra("peerId") ?: ""
        val operatorUid = intent.getStringExtra("operatorUid") ?: ""
        val targetUid = intent.getStringExtra("targetUid") ?: ""
        val msgSeq = intent.getLongExtra("msgSeq", 0)
        val tipText = intent.getStringExtra("tipText") ?: ""

        XposedBridge.log("撤回消息事件")
        val contact = ContactHelper.generateContact(chatType, peerId)
        val msgService = QRoute.api(IMsgService::class.java)
        msgService.addSendMsg(contact, arrayListOf(MsgElement().also {
            it.elementType = MsgConstant.KELEMTYPEGRAYTIP
            val revokeElement = RevokeElement(0L, 0L, operatorUid, "A", "B", "C", targetUid, false, "天才：$tipText")
            it.grayTipElement = GrayTipElement(MsgConstant.GRAYTIPELEMENTSUBTYPEREVOKE, revokeElement, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)
        }))
    }

    operator fun invoke(
        chatType: Int,
        peerId: String,
        operatorUid: String,
        targetUid: String,
        msgSeq: Long,
        tipText: String
    ): Intent = Intent().apply {
        action = DynamicReceiver.MAIN_ACTION
        putExtra("__cmd", getCommand())
        putExtra("chatType", chatType)
        putExtra("peerId", peerId)
        putExtra("operatorUid", operatorUid)
        putExtra("targetUid", targetUid)
        putExtra("msgSeq", msgSeq)
        putExtra("tipText", tipText)
    }

    override fun getCommand(): String = "msg_recall"
}