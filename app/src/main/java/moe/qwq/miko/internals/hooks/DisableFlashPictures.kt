package moe.qwq.miko.internals.hooks

import android.content.Context
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.internals.setting.QwQSetting

class DisableFlashPictures: IAction {
    override fun invoke(ctx: Context) {
        if (!QwQSetting.disableFlashPicture) return
        0
    }
}