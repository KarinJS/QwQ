@file:Suppress("LocalVariableName")
package moe.qwq.miko.internals.locators

import com.tencent.mobileqq.activity.qqsettingme.config.QQSettingMeBizBean
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedBridge.log
import moe.fuqiuluo.entries.ClassEnum
import moe.qwq.miko.internals.helper.DvmLocator
import java.lang.reflect.Method
import java.lang.reflect.Modifier

fun interface MethodLocator {
    operator fun invoke(): Pair<Class<*>, Method>?
}

/*
object QQSettingMeConfigGetItemsLocator: MethodLocator {
    override fun invoke(): Pair<Class<*>, Method>? {
        val QQSettingMeConfig = DvmLocator.findClass(ClassEnum.QQSettingMeConfig)
            ?: return null
        val method = QQSettingMeConfig.methods.firstOrNull {
            !Modifier.isStatic(it.modifiers) && it.returnType.isArray && it.returnType.componentType == QQSettingMeBizBean::class.java
        } ?: return null
        return QQSettingMeConfig to method
    }
}
*/