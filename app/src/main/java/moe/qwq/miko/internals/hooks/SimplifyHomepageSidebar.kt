@file:Suppress("UNUSED_VARIABLE", "LocalVariableName")

package moe.qwq.miko.internals.hooks

import android.content.Context
import moe.fuqiuluo.xposed.loader.LuoClassloader
import moe.qwq.miko.actions.ActionProcess
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.ext.FuzzyClassKit
import moe.qwq.miko.tools.QwQSetting

class SimplifyHomepageSidebar: IAction {
    override fun invoke(ctx: Context) {
        val QQSettingMeBizBean = LuoClassloader.load("com.tencent.mobileqq.activity.qqsettingme.config.QQSettingMeBizBean")
        if (QQSettingMeBizBean != null) {
            val configItems = FuzzyClassKit.findClassesByField(prefix = "com.tencent.mobileqq.activity.qqsettingme.config") { _, field ->

                false
            }

        } else {
            QwQSetting.getSetting(QwQSetting.SIMPLIFY_HOMEPAGE_SIDEBAR).isFailed = true
        }
    }

    override val process: ActionProcess = ActionProcess.MAIN
}