package moe.qwq.miko.hooks

import android.content.Context
import com.tencent.mobileqq.earlydownload.xmldata.XmlData
import de.robv.android.xposed.XposedBridge
import moe.fuqiuluo.processor.HookAction
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.ext.hookMethod
import moe.qwq.miko.internals.setting.QwQSetting
import moe.qwq.miko.internals.setting.QwQSetting.DISABLE_HOT_UPDATE_SO_BY_TRAFFIC

@HookAction("禁用热更新so")
class HotUpdateSoPatch: IAction {
    override val name: String = DISABLE_HOT_UPDATE_SO_BY_TRAFFIC

    override fun onRun(ctx: Context) {
        XmlData::class.java.hookMethod("updateServerInfo").after {
            val xmlData = it.thisObject as XmlData
            xmlData.StoreBackup = false // 禁止备份
            xmlData.load2G = false
            xmlData.load3G = false
            xmlData.net_2_2G = false
            xmlData.net_2_3G = false
        }
    }
}