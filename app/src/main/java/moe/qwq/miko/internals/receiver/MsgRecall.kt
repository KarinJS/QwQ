package moe.qwq.miko.internals.receiver

import android.content.Context
import android.content.Intent
import moe.qwq.miko.internals.broadcasts.DynamicReceiver

@BroadcastReceiver(ReceiveScope.MSF)
object MsgRecall: IBroadcastReceiver {
    override suspend fun onReceive(context: Context, intent: Intent) {
        val chatType = intent.getIntExtra("chatType", 0)
        val peerId = intent.getStringExtra("peerId") ?: ""
        val operatorUid = intent.getStringExtra("operatorUid") ?: ""
        val targetUid = intent.getStringExtra("targetUid") ?: ""
        val msgSeq = intent.getLongExtra("msgSeq", 0)
        val tipText = intent.getStringExtra("tipText") ?: ""

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