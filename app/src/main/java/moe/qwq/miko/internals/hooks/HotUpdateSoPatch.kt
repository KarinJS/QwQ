package moe.qwq.miko.internals.hooks

import android.content.Context
import com.tencent.mobileqq.earlydownload.xmldata.XmlData
import de.robv.android.xposed.XposedBridge
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.ext.hookMethod
import moe.qwq.miko.tools.QwQSetting
import moe.qwq.miko.tools.QwQSetting.DISABLE_HOT_UPDATE_SO_BY_TRAFFIC

class HotUpdateSoPatch: IAction {
    override fun invoke(ctx: Context) {
        if (!QwQSetting.disableHotUpdateSoByTraffic) return

        runCatching {
            XmlData::class.java.hookMethod("updateServerInfo").after {
                val xmlData = it.thisObject as XmlData
                xmlData.StoreBackup = false // 禁止备份
                xmlData.load2G = false
                xmlData.load3G = false
                xmlData.net_2_2G = false
                xmlData.net_2_3G = false

            }
        }.onFailure {
            XposedBridge.log(it)
            QwQSetting.getSetting(DISABLE_HOT_UPDATE_SO_BY_TRAFFIC).isFailed = true
        }
    }
}