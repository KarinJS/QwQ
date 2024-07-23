package moe.qwq.miko.hooks

import android.content.Context
import com.tencent.qphone.base.remote.ToServiceMsg
import de.robv.android.xposed.XposedBridge
import moe.fuqiuluo.processor.HookAction
import moe.qwq.miko.actions.ActionProcess
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.actions.PatchMsfCore
import moe.qwq.miko.internals.hijackers.IHijacker
import moe.qwq.miko.internals.setting.QwQSetting

@HookAction(desc = "消息加密抄送")
class MessageEncrypt: IAction {
    override fun onRun(ctx: Context) {
        val encryptKey = QwQSetting.getSetting<String>(name).getValue(null, null)

    }

    override fun canRun(): Boolean {
        val setting = QwQSetting.getSetting<String>(name)
        return setting.getValue(null, null).isNotBlank()
    }

    //override val process: ActionProcess = ActionProcess.MAIN

    override val name: String = QwQSetting.MESSAGE_ENCRYPT
}