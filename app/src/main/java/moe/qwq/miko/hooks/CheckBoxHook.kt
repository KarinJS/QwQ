package moe.qwq.miko.hooks

import android.content.Context
import android.widget.CheckBox
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import moe.fuqiuluo.processor.HookAction
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.internals.setting.QwQSetting

@HookAction(desc = "默认勾选复选框")
class CheckBoxHook: IAction {
    override fun onRun(ctx: Context) {
        val loginContext = listOf(
            "com.tencent.mobileqq.activity.LoginActivity",
            "com.tencent.mobileqq.activity.LoginPublicFragmentActivity"
        )
        XposedBridge.hookAllConstructors(CheckBox::class.java, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val context = param.args[0] as Context
                if (loginContext.contains(context.javaClass.name)) {
                    val checkBox = param.thisObject as CheckBox
                    checkBox.post { checkBox.isChecked = true }
                }
            }
        })
    }

    override val name: String = QwQSetting.CHECK_BOX_HOOK
}