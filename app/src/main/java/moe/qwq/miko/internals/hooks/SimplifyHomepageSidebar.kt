@file:Suppress("UNUSED_VARIABLE", "LocalVariableName", "UNCHECKED_CAST")
package moe.qwq.miko.internals.hooks

import android.content.Context
import com.tencent.mobileqq.activity.qqsettingme.config.QQSettingMeBizBean
import de.robv.android.xposed.XposedBridge
import moe.fuqiuluo.xposed.loader.LuoClassloader
import moe.qwq.miko.actions.ActionProcess
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.ext.FuzzyClassKit
import moe.qwq.miko.ext.afterHook
import moe.qwq.miko.tools.QwQSetting
import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * 个人设置侧边栏简化
 */
class SimplifyHomepageSidebar: IAction {
    companion object {
        private val TRASH_ITEMS = arrayOf(
            "d_zplan", "d_qq_shopping", "d_smallworld", "d_vip_identity", "d_financial", "d_decoration", "d_vip_card", "d_minigame"
        )
        private lateinit var FIELD_KEY: Field

        private fun findKeyField(bean: QQSettingMeBizBean) {
            if(!::FIELD_KEY.isInitialized) {
                FIELD_KEY = QQSettingMeBizBean::class.java.declaredFields.first {
                    !Modifier.isStatic(it.modifiers) && it.type == String::class.java && (it.also {
                        if (!it.isAccessible) it.isAccessible = true
                    }.get(bean) as String).startsWith("d_")
                }
            }
        }
    }

    override fun invoke(ctx: Context) {
        val setting = QwQSetting.getSetting(QwQSetting.SIMPLIFY_HOMEPAGE_SIDEBAR)
        val QQSettingMeBizBean = LuoClassloader.load("com.tencent.mobileqq.activity.qqsettingme.config.QQSettingMeBizBean")
        if (QQSettingMeBizBean != null) {
            if (!QwQSetting.simplifyHomepageSidebar) return
            val QQSettingMeConfigs = FuzzyClassKit.findClassesByField(prefix = "com.tencent.mobileqq.activity.qqsettingme.config") { _, field ->
                field.type.isArray && field.type.componentType == QQSettingMeBizBean
            }
            if (QQSettingMeConfigs.isEmpty()) {
                setting.isFailed = true
            } else {
                runCatching {
                    val QQSettingMeConfig = QQSettingMeConfigs.first()
                    XposedBridge.hookMethod(QQSettingMeConfig.methods.first {
                        !Modifier.isStatic(it.modifiers) && it.returnType.isArray && it.returnType.componentType == QQSettingMeBizBean
                    }, afterHook {
                        val result = it.result as Array<QQSettingMeBizBean>
                        if (result.isEmpty()) return@afterHook
                        findKeyField(result.first())
                        it.result = result.filter {
                            (FIELD_KEY.get(it) as String) !in TRASH_ITEMS
                        }.toTypedArray()
                    })
                }.onFailure {
                    setting.isFailed = true
                    XposedBridge.log(it)
                }
            }
        } else {
            setting.isFailed = true
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