package moe.qwq.miko.tools

object QwQSetting {


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
}