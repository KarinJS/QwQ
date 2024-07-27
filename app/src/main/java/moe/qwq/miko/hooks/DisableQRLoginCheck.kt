package moe.qwq.miko.hooks

import android.app.Activity
import android.os.CountDownTimer
import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import moe.fuqiuluo.processor.HookAction
import moe.fuqiuluo.xposed.loader.LuoClassloader
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.internals.setting.QwQSetting

@HookAction("禁用相册和长按扫码登录检查")
class DisableQRLoginCheck: IAction {
    override fun onRun(ctx: Context) {
        val qrAgentLoginManager = LuoClassloader.load("com.tencent.open.agent.QrAgentLoginManager")
            ?: throw RuntimeException("QrAgentLoginManager not found")
        qrAgentLoginManager.declaredMethods.firstOrNull {
            it.returnType == Void.TYPE && it.parameterTypes.size == 3 && it.parameterTypes[0] == Boolean::class.java
        }?.let { method ->
            XposedBridge.hookMethod(method, object: XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    param.args[0] = false
                }
            })
        }
        // 伪装非多窗口模式,使得在分屏模式下也可以使用扫码
        Activity::class.java.getDeclaredMethod("isInMultiWindowMode").let { method ->
            XposedBridge.hookMethod(method, object: XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    param.result = false
                }
            })
        }
        // 跳过扫码风险登录五秒等待时间
        for (i in 97 until 108) {
            val clazz = LuoClassloader.load("com.tencent.biz.qrcode.activity.QRLoginAuthActivity\$${i.toChar()}")?: break
            if (clazz.superclass != CountDownTimer::class.java) continue
            XposedBridge.hookAllConstructors(clazz, object: XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    param.args[1] = 0
                    param.args[2] = 0
                }
            })
            return
        }
    }

    override val name: String = QwQSetting.DISABLE_QRLOGIN_CHECK
}