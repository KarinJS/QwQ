@file:Suppress("LocalVariableName")

package moe.qwq.miko.internals.hooks

import android.content.Context
import com.tencent.common.config.pad.DeviceType
import com.tencent.qqnt.kernel.nativeinterface.InitSessionConfig
import de.robv.android.xposed.XposedBridge
import moe.fuqiuluo.xposed.loader.LuoClassloader
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.ext.FuzzyClassKit
import moe.qwq.miko.ext.afterHook
import moe.qwq.miko.ext.hookMethod
import moe.qwq.miko.internals.setting.QwQSetting

class ForceTabletMode: IAction {
    override fun invoke(ctx: Context) {
        if (QwQSetting.forceTabletMode) {
            val returnTablet = afterHook {
                it.result = DeviceType.TABLET
            }

            FuzzyClassKit.findClassesByMethod("com.tencent.common.config.pad") { _, method ->
                method.returnType == DeviceType::class.java
            }.forEach { clazz ->
                val method = clazz.declaredMethods.first { it.returnType == DeviceType::class.java }
                XposedBridge.hookMethod(method, returnTablet)
            }

            val PadUtil = LuoClassloader.load("com.tencent.common.config.pad.PadUtil")
            PadUtil?.declaredMethods?.filter {
                it.returnType == DeviceType::class.java
            }?.forEach {
                XposedBridge.hookMethod(it, returnTablet)
            }

            val deviceTypeField = InitSessionConfig::class.java.declaredFields.firstOrNull {
                it.type == com.tencent.qqnt.kernel.nativeinterface.DeviceType::class.java
            }
            if (deviceTypeField != null) {
                XposedBridge.hookAllConstructors(InitSessionConfig::class.java, afterHook {
                    if (!deviceTypeField.isAccessible) deviceTypeField.isAccessible = true
                    deviceTypeField.set(it.thisObject, com.tencent.qqnt.kernel.nativeinterface.DeviceType.KPAD)
                })
            }
            InitSessionConfig::class.java.hookMethod("getDeviceType").after {
                it.result = com.tencent.qqnt.kernel.nativeinterface.DeviceType.KPAD
            }

            //InitSessionConfig::class.java.hookMethod("getPlatform").after {
            //    it.result = PlatformType.KMAC
            //}
        }
    }
}