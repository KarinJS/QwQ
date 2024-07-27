@file:Suppress("LocalVariableName")

package moe.qwq.miko.hooks

import android.content.Context
import com.tencent.common.config.AppSetting
import de.robv.android.xposed.XposedBridge
import moe.fuqiuluo.processor.HookAction
import moe.qwq.miko.actions.ActionProcess
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.ext.afterHook
import moe.qwq.miko.ext.getStaticObject
import moe.qwq.miko.internals.setting.QwQSetting

@HookAction("强制平板模式协议登录")
class ForceTabletMode: IAction {
    override val name: String = QwQSetting.FORCE_TABLET_MODE

    override val process: ActionProcess = ActionProcess.MSF

    override fun onRun(ctx: Context) {
        XposedBridge.hookMethod(AppSetting::class.java.getDeclaredMethod("f"), afterHook { param ->
            val appId = AppSetting::class.java.getStaticObject("f")
            param.result = appId
        })
    }
}