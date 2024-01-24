package moe.qwq.miko.internals.hooks

import android.content.Context
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.internals.setting.QwQSetting

class RepeatMessage: IAction {
    override fun invoke(ctx: Context) {
        if (!QwQSetting.repeatMessage) return

    }
}