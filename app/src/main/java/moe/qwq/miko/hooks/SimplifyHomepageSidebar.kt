@file:Suppress("UNUSED_VARIABLE", "LocalVariableName", "UNCHECKED_CAST")
package moe.qwq.miko.hooks

import android.content.Context
import com.tencent.mobileqq.activity.qqsettingme.config.QQSettingMeBizBean
import de.robv.android.xposed.XposedBridge
import moe.fuqiuluo.entries.ClassEnum
import moe.fuqiuluo.entries.FieldEnum.QQSettingMeItemName
import moe.fuqiuluo.processor.HookAction
import moe.qwq.miko.actions.ActionProcess
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.ext.afterHook
import moe.qwq.miko.internals.locators.DvmLocator
import moe.qwq.miko.internals.setting.QwQSetting
import java.lang.reflect.Field
import java.lang.reflect.Modifier

@HookAction("个人设置侧边栏简化")
class SimplifyHomepageSidebar: IAction {
    companion object {
        private val TRASH_ITEMS = arrayOf(
            //"d_zplan"
            "d_qq_shopping", "d_smallworld", "d_vip_identity", "d_financial", "d_decoration", "d_vip_card", "d_minigame"
        )
        private lateinit var FieldQQSettingMeItemName: Field

        private fun findItemNameField(bean: QQSettingMeBizBean) {
            if(!Companion::FieldQQSettingMeItemName.isInitialized) {
                DvmLocator.findField(QQSettingMeItemName)?.let {
                    FieldQQSettingMeItemName = it
                    return
                }
                FieldQQSettingMeItemName = QQSettingMeBizBean::class.java.declaredFields.first {
                    !Modifier.isStatic(it.modifiers) && it.type == String::class.java && (it.also {
                        if (!it.isAccessible) it.isAccessible = true
                    }.get(bean) as? String)?.startsWith("d_") == true
                }
                DvmLocator.locateField(QQSettingMeItemName, QQSettingMeBizBean::class.java to FieldQQSettingMeItemName)
            }
        }
    }

    override val name: String = QwQSetting.SIMPLIFY_HOMEPAGE_SIDEBAR

    override fun onRun(ctx: Context) {
        val QQSettingMeBizBean = QQSettingMeBizBean::class.java

        val QQSettingMeConfig = DvmLocator.findClass(ClassEnum.QQSettingMeConfig)
        if (QQSettingMeConfig == null) {
            throw RuntimeException("QQSettingMeConfig not found")
        } else {
            XposedBridge.hookMethod(QQSettingMeConfig.methods.firstOrNull {
                !Modifier.isStatic(it.modifiers) && it.returnType.isArray && it.returnType.componentType == com.tencent.mobileqq.activity.qqsettingme.config.QQSettingMeBizBean::class.java
            }, afterHook {
                val result = it.result as Array<QQSettingMeBizBean>
                if (result.isEmpty()) return@afterHook
                findItemNameField(result.first())
                if (!FieldQQSettingMeItemName.isAccessible) {
                    FieldQQSettingMeItemName.isAccessible = true
                }
                it.result = result.filter { bean ->
                    (FieldQQSettingMeItemName.get(bean) as? String) !in TRASH_ITEMS
                }.toTypedArray()
            })
        }
    }

    override val process: ActionProcess = ActionProcess.MAIN
}

// d_zplan 超级QQ秀
// d_qq_shopping 直播
// d_smallworld 我的小世界
// d_vip_identity 开通会员
// d_qqwallet 我的QQ钱包
// d_financial 财富小金库
// d_decoration 我的个性装扮
// d_lovespace 我的情侣空间
// d_intimate_space 我的亲密空间
// d_vip_card 免流量
// d_minigame 我的小游戏
// d_album 我的相册
// d_favorite 我的收藏
// d_document 我的文件
// d_video 我的视频