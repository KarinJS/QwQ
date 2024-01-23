package moe.qwq.miko.actions

import android.content.Context
import android.webkit.JavascriptInterface
import com.tencent.smtt.sdk.WebView
import de.robv.android.xposed.XposedBridge
import moe.qwq.miko.ext.afterHook
import moe.qwq.miko.ext.getVersionName
import moe.qwq.miko.ext.json
import moe.qwq.miko.ext.toast
import moe.qwq.miko.tools.PlatformTools
import moe.qwq.miko.internals.setting.QwQSetting
import mqq.app.MobileQQ
import java.net.URL

class WebJsBridge: IAction {
    override fun invoke(ctx: Context) {
        val onLoad = afterHook {
            val web = it.thisObject as WebView
            val url = URL(web.url)
            if (url.host == "qwq.qq.com" || url.host == "qwq.dev") {
                web.loadUrl("http://${QwQSetting.settingUrl}")
            } else if (url.host == QwQSetting.settingUrl.split(":")[0]) {
                web.addJavascriptInterface(QwQJsBridge, "qwq")
            }
        }
        WebView::class.java.declaredMethods
            .filter { it.name == "loadUrl" || it.name == "loadData" || it.name == "loadDataWithBaseURL" }
            .forEach { XposedBridge.hookMethod(it, onLoad) }
    }

    companion object QwQJsBridge {
        @JavascriptInterface
        fun getQQVersion(): String {
            return PlatformTools.getQQVersion(MobileQQ.getContext())
        }

        @JavascriptInterface
        fun getStatus(): String {
            return "LSPosed 已激活"
        }

        @JavascriptInterface
        fun getModuleVersion(): String {
            return MobileQQ.getContext().getVersionName("moe.qwq.miko")
        }

        @JavascriptInterface
        fun toast(str: String) {
            MobileQQ.getContext().toast(str)
        }

        @JavascriptInterface
        fun getSetting(key: String): String {
            val setting = QwQSetting.getSetting(key)
            return mapOf(
                "value" to setting.getValue(setting, null),
                "failed" to setting.isFailed
            ).json.toString()
        }

        @JavascriptInterface
        fun setSetting(key: String, value: Boolean) {
            val setting = QwQSetting.getSetting(key) as QwQSetting.Setting<Any>
            setting.setValue(setting, null, value)
        }
    }
}