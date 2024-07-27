package moe.qwq.miko.internals.setting

import com.tencent.mmkv.MMKV
import moe.qwq.miko.utils.MMKVUtils
import mqq.app.MobileQQ
import kotlin.reflect.KProperty

object QwQSetting {
    const val DISABLE_QRLOGIN_CHECK: String = "disable_qrlogin_check"
    const val DISABLE_QQ_CRASH_REPORT: String = "disable_qq_crash_report"
    const val MESSAGE_ENCRYPT: String = "message_encrypt"
    const val MESSAGE_TAIL: String = "message_tail"
    const val INTERCEPT_RECALL = "intercept_recall"
    const val ANTI_BROWSER_ACCESS_RESTRICTIONS = "anti_browser_access_restrictions"
    const val SIMPLIFY_HOMEPAGE_SIDEBAR = "simplify_homepage_sidebar"
    const val DISABLE_UPDATE_CHECK = "disable_update_check"
    const val DISABLE_HOT_UPDATE_SO_BY_TRAFFIC = "disable_hot_update_so_by_traffic"
    const val DISABLE_USELESS_PACKET = "disable_useless_packet"
    const val ONE_KEY_LIKE = "one_click_like"
    const val FORCE_TABLET_MODE = "force_tablet_mode"
    const val SIMPLIFY_BUBBLE_FONT = "simplify_bubble_font"
    const val SIMPLIFY_BUBBLE_AVATAR = "simplify_bubble_avatar"
    const val REPEAT_MESSAGE = "repeat_message"
    const val DISABLE_VISIT_GROUP_ANIMATION = "disable_visit_group_animation"
    const val SUPER_GROUP_FILE = "super_group_file"
    const val SHOW_BAN_OPERATOR = "show_ban_operator"
    const val OPTIMIZE_AT_SORT = "optimize_at_sort"
    const val DISABLE_FLASH_PICTURE = "disable_flash_picture"
    const val ALLOW_GROUP_FLASH_PIC = "allow_group_flash_pic"

    internal val dataDir = MobileQQ.getContext().getExternalFilesDir(null)!!
        .parentFile!!.resolve("Tencent/QwQ").also {
            it.mkdirs()
        }

    private val config: MMKV get() = MMKVUtils.mmkvWithId("qwq")
    val settingMap = hashMapOf<String, Setting<out Any>>(
        INTERCEPT_RECALL to Setting<Boolean>(INTERCEPT_RECALL, SettingType.BOOLEAN),
        ANTI_BROWSER_ACCESS_RESTRICTIONS to Setting(ANTI_BROWSER_ACCESS_RESTRICTIONS, SettingType.BOOLEAN, true),
        SIMPLIFY_HOMEPAGE_SIDEBAR to Setting<Boolean>(SIMPLIFY_HOMEPAGE_SIDEBAR, SettingType.BOOLEAN),
        DISABLE_UPDATE_CHECK to Setting<Boolean>(DISABLE_UPDATE_CHECK, SettingType.BOOLEAN),
        DISABLE_HOT_UPDATE_SO_BY_TRAFFIC to Setting<Boolean>(DISABLE_HOT_UPDATE_SO_BY_TRAFFIC, SettingType.BOOLEAN),
        DISABLE_USELESS_PACKET to Setting(DISABLE_USELESS_PACKET, SettingType.BOOLEAN, true),
        ONE_KEY_LIKE to Setting<Boolean>(ONE_KEY_LIKE, SettingType.BOOLEAN),
        FORCE_TABLET_MODE to Setting<Boolean>(FORCE_TABLET_MODE, SettingType.BOOLEAN),
        SIMPLIFY_BUBBLE_FONT to Setting<Boolean>(SIMPLIFY_BUBBLE_FONT, SettingType.BOOLEAN),
        SIMPLIFY_BUBBLE_AVATAR to Setting<Boolean>(SIMPLIFY_BUBBLE_AVATAR, SettingType.BOOLEAN),
        REPEAT_MESSAGE to Setting<Boolean>(REPEAT_MESSAGE, SettingType.BOOLEAN),
        DISABLE_VISIT_GROUP_ANIMATION to Setting<Boolean>(DISABLE_VISIT_GROUP_ANIMATION, SettingType.BOOLEAN),
        SUPER_GROUP_FILE to Setting<Boolean>(SUPER_GROUP_FILE, SettingType.BOOLEAN),
        SHOW_BAN_OPERATOR to Setting<Boolean>(SHOW_BAN_OPERATOR, SettingType.BOOLEAN),
        OPTIMIZE_AT_SORT to Setting<Boolean>(OPTIMIZE_AT_SORT, SettingType.BOOLEAN),
        DISABLE_FLASH_PICTURE to Setting<Boolean>(DISABLE_FLASH_PICTURE, SettingType.BOOLEAN),
        ALLOW_GROUP_FLASH_PIC to Setting<Boolean>(ALLOW_GROUP_FLASH_PIC, SettingType.BOOLEAN),
        MESSAGE_TAIL to Setting(MESSAGE_TAIL, SettingType.STRING, ""),
        MESSAGE_ENCRYPT to Setting(MESSAGE_ENCRYPT, SettingType.STRING, ""), // 消息加密密钥
        DISABLE_QQ_CRASH_REPORT to Setting(DISABLE_QQ_CRASH_REPORT, SettingType.BOOLEAN, true),
        DISABLE_QRLOGIN_CHECK to Setting(DISABLE_QRLOGIN_CHECK, SettingType.BOOLEAN, true)
    )

    val settingUrl: String
        get() = dataDir.resolve("domain").also {
            if (!it.exists()) {
                it.createNewFile()
                it.writeText("qwq-web-setting.pages.dev")
            }
        }.readText()

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any> getSetting(key: String): Setting<T> {
        val result = settingMap[key] ?: Setting(key, SettingType.BOOLEAN)
        return result as Setting<T>
    }

    enum class SettingType {
        STRING, INT, BOOLEAN
    }

    class Setting<T: Any>(
        val name: String,
        val type: SettingType,
        val default: T? = null
    ) {
        /**
         * 功能不支持该QQ时为true
         */
        var isFailed = false

        operator fun getValue(t: T?, property: KProperty<*>?): T {
            val value = when(type) {
                SettingType.STRING -> config.getString(name, (default as? String) ?: "")
                SettingType.INT -> config.getInt(name, (default as? Int) ?: 0)
                SettingType.BOOLEAN -> config.getBoolean(name, (default as? Boolean) ?: false)
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