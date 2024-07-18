package moe.qwq.miko.internals.hooks

import android.content.Context
import android.os.Bundle
import com.tencent.mobileqq.qroute.QRoute
import com.tencent.mobileqq.webview.api.IJsApi
import com.tencent.mobileqq.webview.swift.WebViewPlugin
import de.robv.android.xposed.XposedBridge
import moe.fuqiuluo.entries.ClassEnum
import moe.fuqiuluo.entries.ClassEnum.WebSecurityPluginV2Plugin
import moe.fuqiuluo.processor.HookAction
import moe.qwq.miko.actions.AlwaysRunAction
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.ext.FuzzyClassKit
import moe.qwq.miko.ext.beforeHook
import moe.qwq.miko.ext.toast
import moe.qwq.miko.internals.helper.DvmLocator
import moe.qwq.miko.internals.setting.QwQSetting

@HookAction(desc = "内置浏览器安全限制解除")
class BrowserAccessRestrictions: AlwaysRunAction() {
    override val name: String = QwQSetting.ANTI_BROWSER_ACCESS_RESTRICTIONS

    override fun onRun(ctx: Context) {
        val WebSecurityPluginV2Plugin = DvmLocator.findClass(WebSecurityPluginV2Plugin)
        if (WebSecurityPluginV2Plugin == null) {
            ctx.toast("QwQ模块无法载入")
            return
        }

        FuzzyClassKit.findClassesByMethod(
            WebSecurityPluginV2Plugin.name,
            isSubClass = true
        ) { _, method ->
            method.parameterCount == 1 && method.parameterTypes[0] == Bundle::class.java
        }.forEach {
            it.declaredMethods.filter {
                it.parameterCount == 1 && it.parameterTypes[0] == Bundle::class.java
            }.forEach {
                XposedBridge.hookMethod(it, beforeHook {
                    val bundle = it.args[0] as Bundle
                    if (bundle.getInt("jumpResult", 0) != 0) {
                        bundle.putInt("jumpResult", 0)
                        bundle.putString("jumpUrl", "")
                    }
                })
            }
        }
    }
}