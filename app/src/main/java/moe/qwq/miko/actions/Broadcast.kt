package moe.qwq.miko.actions

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import moe.qwq.miko.ext.GlobalUi
import moe.qwq.miko.tools.PlatformTools

class Broadcast: IAction {
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun invoke(ctx: Context) {
        //kotlin.runCatching {
        //    MobileQQ.getMobileQQ().unregisterReceiver(DynamicReceiver)
        //}

        if (PlatformTools.isMainProcess()) {
            GlobalUi = Handler(ctx.mainLooper)
        }
    }
}