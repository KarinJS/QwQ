package moe.qwq.miko.actions

import android.content.Context
import android.webkit.JavascriptInterface
import com.tencent.smtt.sdk.WebView
import de.robv.android.xposed.XposedBridge
import moe.qwq.miko.ext.afterHook
import moe.qwq.miko.ext.toast
import moe.qwq.miko.tools.MMKVTools
import moe.qwq.miko.tools.PlatformTools
import moe.qwq.miko.tools.QwQSetting
import mqq.app.MobileQQ
import java.net.URL

class WebJsBridge: IAction {
    override fun invoke(ctx: Context) {
        val onLoad = afterHook {
            val web = it.thisObject as WebView
            val url = URL(web.url)
            if (url.host == "qwq.qq.com") {
                web.loadUrl("http://${QwQSetting.settingUrl}")
            } else if (url.host == QwQSetting.settingUrl) {
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
            return "1.0.0"
        }

        @JavascriptInterface
        fun toast(str: String) {
            MobileQQ.getContext().toast(str)
        }

        @JavascriptInterface
        fun mmkvGetValueString(key: String): String {
            return MMKVTools.mmkvWithId("qwq").getString(key, "")!!
        }

        @JavascriptInterface
        fun mmkvGetValueBoolean(key: String): Boolean {
            return MMKVTools.mmkvWithId("qwq").getBoolean(key, false)
        }

        @JavascriptInterface
        fun mmkvGetValueInt(key: String): Int {
            return MMKVTools.mmkvWithId("qwq").getInt(key, 0)
        }

        @JavascriptInterface
        fun mmkvSetValueBoolean(key: String, value: Boolean) {
            MMKVTools.mmkvWithId("qwq").putBoolean(key, value)
        }

        @JavascriptInterface
        fun mmkvSetValueString(key: String, value: String) {
            MMKVTools.mmkvWithId("qwq").putString(key, value)
        }

        @JavascriptInterface
        fun mmkvSetValueInt(key: String, value: Int) {
            MMKVTools.mmkvWithId("qwq").putInt(key, value)
        }
    }
}