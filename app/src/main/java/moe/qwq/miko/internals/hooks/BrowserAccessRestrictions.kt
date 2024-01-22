@file:Suppress("LocalVariableName")

package moe.qwq.miko.internals.hooks

import android.content.Context
import android.os.Bundle
import com.tencent.mobileqq.qroute.QRoute
import com.tencent.mobileqq.webview.api.IJsApi
import com.tencent.mobileqq.webview.swift.WebViewPlugin
import de.robv.android.xposed.XposedBridge
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.ext.FuzzyClassKit
import moe.qwq.miko.ext.beforeHook
import moe.qwq.miko.tools.QwQSetting

class BrowserAccessRestrictions: IAction {
    override fun invoke(ctx: Context) {
        val tmpWebSecurityPluginV2Plugin = QRoute.api(IJsApi::class.java)
            .getWebSecurityPluginV2Plugin<WebViewPlugin>()
        val WebSecurityPluginV2Plugin = tmpWebSecurityPluginV2Plugin.javaClass

        FuzzyClassKit.findClassesByMethod(
            WebSecurityPluginV2Plugin.name,
            isSubClass = true
        ) { clz, method ->
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