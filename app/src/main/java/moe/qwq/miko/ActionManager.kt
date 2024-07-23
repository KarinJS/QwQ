package moe.qwq.miko

import android.content.Context
import moe.qwq.miko.actions.ActionProcess
import moe.qwq.miko.internals.hooks.BrowserAccessRestrictions
import moe.qwq.miko.actions.FetchService
import moe.qwq.miko.actions.*
import moe.qwq.miko.internals.hooks.*

object ActionManager {
    // TODO(ksp实现全自动添加action)
    private val FIRST_ACTION = arrayOf(
        WebJsBridge::class.java, // ALWAYS RUN
        FetchService::class.java, // ALWAYS RUN
        PacketHijacker::class.java, // ALWAYS RUN
        PatchMsfCore::class.java, // ALWAYS RUN

        OneClickLike::class.java,
        ForceTabletMode::class.java,

        BrowserAccessRestrictions::class.java, // ALWAYS RUN
        SimplifyHomepageSidebar::class.java,
        DefaultPacketHijacker::class.java,
        HotUpdateSoPatch::class.java,

        RepeatMessage::class.java,
        MessageHook::class.java,
        DisableFlashPictures::class.java,
        AllowGroupFlashPic::class.java,
        QQCrashHook::class.java,
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