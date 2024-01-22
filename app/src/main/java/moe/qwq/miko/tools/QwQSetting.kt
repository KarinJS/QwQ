@file:Suppress("UNCHECKED_CAST", "RemoveExplicitTypeArguments")
package moe.qwq.miko.tools

import com.tencent.mmkv.MMKV
import mqq.app.MobileQQ
import kotlin.reflect.KProperty

object QwQSetting {
    const val INTERCEPT_RECALL = "intercept_recall"
    const val ANTI_BROWSER_ACCESS_RESTRICTIONS = "anti_browser_access_restrictions"
    const val SIMPLIFY_HOMEPAGE_SIDEBAR = "simplify_homepage_sidebar"
    const val DISABLE_UPDATE_CHECK = "disable_update_check"

    private val DataDir = MobileQQ.getContext().getExternalFilesDir(null)!!
        .parentFile!!.resolve("Tencent/QwQ").also {
            it.mkdirs()
        }
    private val config: MMKV get() = MMKVTools.mmkvWithId("qwq")
    private val settingMap = hashMapOf<String, Setting<*>>(
        INTERCEPT_RECALL to Setting<Boolean>(INTERCEPT_RECALL, SettingType.BOOLEAN),
        ANTI_BROWSER_ACCESS_RESTRICTIONS to Setting<Boolean>(ANTI_BROWSER_ACCESS_RESTRICTIONS, SettingType.BOOLEAN),
        SIMPLIFY_HOMEPAGE_SIDEBAR to Setting<Boolean>(SIMPLIFY_HOMEPAGE_SIDEBAR, SettingType.BOOLEAN),
        DISABLE_UPDATE_CHECK to Setting<Boolean>(DISABLE_UPDATE_CHECK, SettingType.BOOLEAN)
    )

    /**
     * 是否拦截撤回消息事件
     */
    var interceptRecall by settingMap[INTERCEPT_RECALL] as Setting<Boolean>

    /**
     * 反浏览器访问限制
     */
    var antiBrowserAccessRestrictions by settingMap[ANTI_BROWSER_ACCESS_RESTRICTIONS] as Setting<Boolean>

    /**
     * 主页侧边栏简化
     */
    var simplifyHomepageSidebar by settingMap[SIMPLIFY_HOMEPAGE_SIDEBAR] as Setting<Boolean>

    /**
     * 禁止更新检查
     */
    var disableUpdateCheck by settingMap[DISABLE_UPDATE_CHECK] as Setting<Boolean>

    val settingUrl: String
        get() = DataDir.resolve("domain").also {
            if (!it.exists()) {
                it.createNewFile()
                it.writeText("qwq.owo233.com")
            }
        }.readText()

    fun getSetting(key: String): Setting<*> {
        return settingMap[key]!!
    }

    enum class SettingType {
        STRING, INT, BOOLEAN
    }

    class Setting<T: Any>(
        val name: String,
        private val type: SettingType
    ) {
        /**
         * 功能不支持该QQ版本时为true
         */
        var isFailed = false

        operator fun getValue(any: Any, property: KProperty<*>?): T {
            val value = when(type) {
                SettingType.STRING -> config.getString(name, "")
                SettingType.INT -> config.getInt(name, 0)
                SettingType.BOOLEAN -> config.getBoolean(name, false)
            }
            return value as T
        }

        operator fun setValue(any: Any, property: KProperty<*>?, t: T) {
            when(type) {
                SettingType.STRING -> config.putString(name, t as String)
                SettingType.INT -> config.putInt(name, t as Int)
                SettingType.BOOLEAN -> config.putBoolean(name, if (t is String) t.toBooleanStrict() else t as Boolean)
            }
        }
    }
}