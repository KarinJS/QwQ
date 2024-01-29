package moe.qwq.miko.internals.locators

import android.widget.CheckBox
import com.tencent.mobileqq.activity.qqsettingme.config.QQSettingMeBizBean
import com.tencent.mobileqq.qroute.QRoute
import com.tencent.mobileqq.webview.api.IJsApi
import com.tencent.mobileqq.webview.swift.WebViewPlugin
import moe.fuqiuluo.xposed.loader.LuoClassloader
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

object PreviewUserInteractionPartLocator: ClassLocator {
    override fun invoke(): Class<*>? {
        return LuoClassloader.load("com.tencent.qqnt.qbasealbum.preview.fragment.PreviewUserInteractionPart") ?: run {
            var matchedFieldCnt = 0
            FuzzyClassKit.findClassesByField(prefix = "com.tencent.qqnt.qbasealbum.preview.fragment") { _, field ->
                if (field.type.name.contains("RecyclerView")) {
                    matchedFieldCnt++
                }
                if (field.type == CheckBox::class.java) {
                    matchedFieldCnt++
                }
                matchedFieldCnt >= 3
            }.firstOrNull()
        }
    }
}

object AbstractPreviewUiLocator: ClassLocator {
    override fun invoke(): Class<*>? {
        return LuoClassloader.load("com.tencent.qqnt.qbasealbum.customization.preview.AbstractPreviewUi") ?: run {
            var matchedFieldCnt = 0
            FuzzyClassKit.findClassesByField(prefix = "com.tencent.qqnt.qbasealbum.customization.preview") { _, field ->
                if (field.type == CheckBox::class.java) {
                    matchedFieldCnt++
                }
                matchedFieldCnt >= 3
            }.firstOrNull()
        }
    }
}