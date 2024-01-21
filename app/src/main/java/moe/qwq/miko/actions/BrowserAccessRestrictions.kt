@file:Suppress("UNUSED_VARIABLE", "LocalVariableName")
package moe.qwq.miko.actions

import android.content.Context
import android.os.Bundle
import com.tencent.mobileqq.qroute.QRoute
import com.tencent.mobileqq.webview.api.IJsApi
import com.tencent.mobileqq.webview.swift.WebViewPlugin
import de.robv.android.xposed.XposedBridge
import moe.qwq.miko.ext.FuzzySearchClass
import moe.qwq.miko.ext.beforeHook
import moe.qwq.miko.ext.toast

class BrowserAccessRestrictions: IAction {
    override fun invoke(ctx: Context) {
        val tmpWebSecurityPluginV2Plugin = QRoute.api(IJsApi::class.java).getWebSecurityPluginV2Plugin<WebViewPlugin>()
        val WebSecurityPluginV2Plugin = tmpWebSecurityPluginV2Plugin.javaClass

        FuzzySearchClass.findClassesByMethod(WebSecurityPluginV2Plugin.name, isSubClass = true) { clz, method ->
            method.parameterCount == 1 && method.parameterTypes[0] == Bundle::class.java
        }.forEach {
            //ctx.toast(it.name)
            it.declaredMethods.filter {
                it.parameterCount == 1 && it.parameterTypes[0] == Bundle::class.java
            }.forEach {
                XposedBridge.hookMethod(it, beforeHook {
                    val bundle = it.args[0] as Bundle
                    if (bundle.getInt("jumpResult", 0) != 0) {
                        bundle.putInt("jumpResult", 0)
                        bundle.putString("jumpUrl", "")
                        //ctx.toast("阻止浏览器安全拦截")
                    }
                })
            }
        }

    }
}