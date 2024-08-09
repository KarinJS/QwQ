package moe.qwq.miko.hooks

import android.content.Context
import android.widget.CheckBox
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import moe.fuqiuluo.processor.HookAction
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.ext.beforeHook
import moe.qwq.miko.ext.hookMethod
import moe.qwq.miko.internals.setting.QwQSetting

@HookAction(desc = "默认勾选复选框")
class CheckBoxHook: IAction {
    override fun onRun(ctx: Context) {
        XposedBridge.hookAllConstructors(CheckBox::class.java, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val checkBox = param.thisObject as CheckBox
                checkBox.post { checkBox.isChecked = true }
            }
        })
        val hook = beforeHook {
            it.args[0] = true
        }
        CheckBox::class.java.hookMethod("setChecked", hook)
    }

    override val name: String = QwQSetting.CHECK_BOX_HOOK
}