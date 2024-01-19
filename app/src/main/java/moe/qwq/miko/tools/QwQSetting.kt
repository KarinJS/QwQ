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


}