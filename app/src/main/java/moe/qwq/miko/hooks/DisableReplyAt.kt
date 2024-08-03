package moe.qwq.miko.hooks

import android.content.Context
import com.tencent.mobileqq.aio.msg.AIOMsgItem
import de.robv.android.xposed.XposedBridge
import moe.fuqiuluo.xposed.loader.LuoClassloader
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.ext.beforeHook
import moe.qwq.miko.internals.setting.QwQSetting

class DisableReplyAt: IAction {
    override fun onRun(ctx: Context) {
        val replyAt = LuoClassloader.load("com.tencent.mobileqq.aio.input.reply.c") // 混淆的类名
            ?: throw RuntimeException("ReplyAt not found")
        replyAt.declaredMethods.firstOrNull {
            it.returnType == Void.TYPE && it.parameterTypes.size == 1 && it.parameterTypes[0] == AIOMsgItem::class.java
        }?.let {
            val hook = beforeHook { param ->
                param.result = Unit
            }
            XposedBridge.hookMethod(it, hook)
        }
    }

    override val name: String = QwQSetting.DISABLE_REPLY_AT
}