package moe.qwq.miko.internals.locators

import com.tencent.mobileqq.activity.qqsettingme.config.QQSettingMeBizBean
import com.tencent.mobileqq.qroute.QRoute
import com.tencent.mobileqq.webview.api.IJsApi
import com.tencent.mobileqq.webview.swift.WebViewPlugin
import moe.qwq.miko.ext.FuzzyClassKit

fun interface ClassLocator {
    operator fun invoke(): Class<*>?
}

object WebSecurityPluginV2PluginLocator: ClassLocator {
    override fun invoke(): Class<*> {
        return QRoute.api(IJsApi::class.java).getWebSecurityPluginV2Plugin<WebViewPlugin>().javaClass
    }
}

object QQSettingMeConfigLocator: ClassLocator {
    override fun invoke(): Class<*>? {
        return FuzzyClassKit.findClassesByField(prefix = "com.tencent.mobileqq.activity.qqsettingme.config") { _, field ->
            field.type.isArray && field.type.componentType == QQSettingMeBizBean::class.java
        }.firstOrNull()
    }
}