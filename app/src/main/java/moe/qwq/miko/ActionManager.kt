package moe.qwq.miko

import android.content.Context
import moe.qwq.miko.actions.ActionProcess
import moe.qwq.miko.hooks.BrowserAccessRestrictions
import moe.qwq.miko.actions.FetchService
import moe.qwq.miko.actions.*
import moe.qwq.miko.hooks.AllowGroupFlashPic
import moe.qwq.miko.hooks.DefaultPacketHijacker
import moe.qwq.miko.hooks.DisableFlashPictures
import moe.qwq.miko.hooks.DisableQRLoginCheck
import moe.qwq.miko.hooks.DisableReplyAt
import moe.qwq.miko.hooks.ForceTabletMode
import moe.qwq.miko.hooks.HotUpdateSoPatch
import moe.qwq.miko.hooks.MessageEncrypt
import moe.qwq.miko.hooks.MessageTail
import moe.qwq.miko.hooks.OneClickLike
import moe.qwq.miko.hooks.OptimizeAtSort
import moe.qwq.miko.hooks.QQCrashHook
import moe.qwq.miko.hooks.RepeatMessage
import moe.qwq.miko.hooks.SimplifyBubbleFont
import moe.qwq.miko.hooks.SimplifyHomepageSidebar

object ActionManager {
    // TODO(ksp实现全自动添加action)
    private val FIRST_ACTION = arrayOf(
        WebJsBridge::class.java, // ALWAYS RUN
        FetchService::class.java, // ALWAYS RUN
        PatchMsfCore::class.java, // ALWAYS RUN
        HookCodec::class.java, // ALWAYS RUN

        OneClickLike::class.java,
        ForceTabletMode::class.java,

        BrowserAccessRestrictions::class.java, // ALWAYS RUN
        SimplifyBubbleFont::class.java,
        SimplifyHomepageSidebar::class.java,
        DefaultPacketHijacker::class.java,
        HotUpdateSoPatch::class.java,

        RepeatMessage::class.java,
        MessageTail::class.java,
        MessageEncrypt::class.java,
        DisableFlashPictures::class.java,
        AllowGroupFlashPic::class.java,
        QQCrashHook::class.java,
        OptimizeAtSort::class.java,
        DisableQRLoginCheck::class.java,
        DisableReplyAt::class.java
    )

    private val instanceMap = hashMapOf<Class<*>, IAction>()

    fun runFirst(ctx: Context, proc: ActionProcess) {
        FIRST_ACTION.forEach {
            val action = instanceOf(it)
            if (proc == action.process || proc == ActionProcess.ALL) {
                action(ctx)
            }
        }
    }

    private fun instanceOf(cls: Class<*>): IAction = instanceMap.getOrPut(cls) {
        cls.getConstructor().newInstance() as IAction
    }
}