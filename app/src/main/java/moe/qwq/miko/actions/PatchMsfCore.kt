package moe.qwq.miko.actions

import android.content.Context
import com.tencent.mobileqq.msf.sdk.MsfMessagePair
import de.robv.android.xposed.XposedBridge
import moe.qwq.miko.internals.msf.MSFHandler.onPush
import moe.qwq.miko.internals.msf.MSFHandler.onResp
import moe.fuqiuluo.processor.HookAction
import moe.fuqiuluo.xposed.loader.LuoClassloader
import moe.qwq.miko.ext.hookMethod

@HookAction("注入MSF收包任务")
class PatchMsfCore: AlwaysRunAction() {
    override fun onRun(ctx: Context) {
        runCatching {
            val MSFRespHandleTask = LuoClassloader.load("mqq.app.msghandle.MSFRespHandleTask")
            if (MSFRespHandleTask == null) {
                XposedBridge.log("[QwQ] 无法注入MSFRespHandleTask！")
            } else {
                val msfPair = MSFRespHandleTask.declaredFields.first {
                    it.type == MsfMessagePair::class.java
                }
                msfPair.isAccessible = true
                MSFRespHandleTask.hookMethod("run").before {
                    val pair = msfPair.get(it.thisObject) as MsfMessagePair
                    if (pair.toServiceMsg == null) {
                        onPush(pair.fromServiceMsg)
                    } else {
                        onResp(pair.toServiceMsg, pair.fromServiceMsg)
                    }
                }
            }
        }.onFailure {
            XposedBridge.log(it)
        }
    }
}