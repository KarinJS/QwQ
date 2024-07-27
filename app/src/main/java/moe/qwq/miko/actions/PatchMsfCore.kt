@file:Suppress("LocalVariableName")
package moe.qwq.miko.actions

import android.content.Context
import com.tencent.mobileqq.msf.sdk.MsfMessagePair
import de.robv.android.xposed.XposedBridge
import moe.fuqiuluo.processor.HookAction
import moe.fuqiuluo.xposed.loader.LuoClassloader
import moe.qwq.miko.ext.hookMethod
import moe.qwq.miko.internals.msf.MSFHandler.onPush
import moe.qwq.miko.internals.msf.MSFHandler.onResp

@HookAction("注入MSF收包任务")
class PatchMsfCore: AlwaysRunAction() {
    override fun onRun(ctx: Context) {
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

/*        val MobileQQServiceBase = LuoClassloader.load("com.tencent.mobileqq.service.MobileQQServiceBase")
        if (MobileQQServiceBase == null) {
            XposedBridge.log("[QwQ] 无法注入MobileQQServiceBase！部分服务可能不可用！")
        } else {
            val realHandleRequest = kotlin.runCatching { MobileQQServiceBase.getMethod("realHandleRequest", ToServiceMsg::class.java, Class::class.java) }
                .getOrElse {
                    MobileQQServiceBase.methods.firstOrNull { it.returnType == Void.TYPE && it.parameterTypes.size == 2 && it.parameterTypes[0] == ToServiceMsg::class.java && it.parameterTypes[1] == Class::class.java }
                }
            if (realHandleRequest == null) {
                XposedBridge.log("[QwQ] 无法注入MobileQQServiceBase.realHandleRequest！部分服务可能不可用！")
            } else {
                XposedBridge.hookMethod(realHandleRequest, beforeHook {
                    val toServiceMsg = it.args[0] as ToServiceMsg
                    val isPb = toServiceMsg.getAttribute("req_pb_protocol_flag", false)
                    val cmd = toServiceMsg.serviceCmd
                    if(hijackerList.firstOrNull {
                        it.command == cmd
                    }?.onHandle(toServiceMsg, isPb) == true) {
                        it.result = Unit
                    }
                })
            }
        }*/
    }

/*    companion object {
        val hijackerList = arrayListOf<IHijacker>()
    }*/
}