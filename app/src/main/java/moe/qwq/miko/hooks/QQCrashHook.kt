@file:Suppress("LocalVariableName")
package moe.qwq.miko.hooks

import android.content.Context
import de.robv.android.xposed.XposedBridge
import moe.fuqiuluo.processor.HookAction
import moe.fuqiuluo.xposed.loader.LuoClassloader
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.ext.beforeHook
import moe.qwq.miko.ext.hookMethod
import moe.qwq.miko.internals.setting.QwQSetting
import java.lang.reflect.Modifier

@HookAction(desc = "QQCrashManager注入")
class QQCrashHook: IAction {
    override fun onRun(ctx: Context) {
        val QQCrashReportManager = LuoClassloader.load("com.tencent.qqperf.monitor.crash.QQCrashReportManager")
            ?: throw RuntimeException("QQCrashReportManager not found")

        XposedBridge.hookMethod(QQCrashReportManager.declaredMethods.first {
            !Modifier.isStatic(it.modifiers) && it.returnType == Void.TYPE && it.parameterTypes.size == 2
        }, beforeHook {
            it.result = Unit
        })

        val QQCrashHandleListener = LuoClassloader.load("com.tencent.qqperf.monitor.crash.QQCrashHandleListener")
            ?: throw RuntimeException("QQCrashHandleListener not found")
        val hook = beforeHook {
            it.result = Unit
        }
        QQCrashHandleListener.hookMethod("onCrashHandleEnd", hook)
        QQCrashHandleListener.hookMethod("onCrashHandleStart", hook)
        QQCrashHandleListener.hookMethod("onCrashSaving", hook)
    }

    override val name: String = QwQSetting.DISABLE_QQ_CRASH_REPORT
}