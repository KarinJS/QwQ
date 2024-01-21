package moe.qwq.miko.tools

import mqq.app.MobileQQ

object QwQSetting {
    private val DataDir = MobileQQ.getContext().getExternalFilesDir(null)!!
        .parentFile!!.resolve("Tencent/QwQ").also {
            it.mkdirs()
        }

    /**
     * 是否拦截撤回消息事件
     */
    var interceptRecall: Boolean
        get() = MMKVTools.mmkvWithId("qwq")
            .getBoolean("intercept_recall", false)

        set(value) {
            MMKVTools.mmkvWithId("qwq")
                .putBoolean("intercept_recall", value)
        }

    /**
     * 反浏览器访问限制
     */
    var antiBrowserAccessRestrictions: Boolean
        get() = MMKVTools.mmkvWithId("qwq")
            .getBoolean("anti_browser_access_restriction", true)

        set(value) {
            MMKVTools.mmkvWithId("qwq")
                .putBoolean("anti_browser_access_restriction", value)
        }

    val settingUrl: String
        get() = DataDir.resolve("domain").also {
            if (!it.exists()) {
                it.createNewFile()
                it.writeText("qwq.owo233.com")
            }
        }.readText()

}