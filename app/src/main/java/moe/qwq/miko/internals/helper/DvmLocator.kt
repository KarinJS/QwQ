package moe.qwq.miko.internals.helper

import de.robv.android.xposed.XposedBridge
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import moe.fuqiuluo.entries.ClassEnum
import moe.fuqiuluo.entries.ClassInfo
import moe.fuqiuluo.entries.FieldEnum
import moe.fuqiuluo.entries.FieldInfo
import moe.fuqiuluo.entries.FuzzyDexKit
import moe.fuqiuluo.entries.MethodEnum
import moe.fuqiuluo.entries.MethodInfo
import moe.qwq.miko.internals.locators.ClassLocator
import moe.qwq.miko.internals.locators.FieldLocator
import moe.qwq.miko.internals.locators.MethodLocator
import moe.qwq.miko.internals.setting.QwQSetting
import mqq.app.MobileQQ
import oicq.wlogin_sdk.tools.MD5
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

object DvmLocator {
    private val file = QwQSetting.dataDir.resolve(
        MD5.getFileMD5(File(MobileQQ.getContext().applicationInfo.sourceDir))
    )
    private var cacheMap: FuzzyDexKit

    init {
        if (!file.exists()) file.writeText(Json.encodeToString(FuzzyDexKit()))
        cacheMap = Json.decodeFromString(FuzzyDexKit.serializer(), file.readText())
    }

    fun findClass(cls: ClassEnum, locator: ClassLocator? = cls.locator): Class<*>? {
        if (locator != null && !cacheMap.classes.containsKey(cls)) {
            locateClass(cls, runCatching { locator() }.getOrNull())
        }
        if(cacheMap.classes.containsKey(cls)) {
            val ret = cacheMap.classes[cls]!!.toClass()
            if (ret == null) {
                XposedBridge.log("[QwQ] $cls locate failed!")
            }
            return ret
        }
        return null
    }

    fun locateClass(cls: ClassEnum, clz: Class<*>?) {
        if (clz == null || cacheMap.classes.containsKey(cls)) return
        cacheMap.classes[cls] = ClassInfo(clz.name, false)
        update()
    }

    fun findField(field: FieldEnum, locator: FieldLocator? = field.locator): Field? {
        if (locator != null && !cacheMap.fields.containsKey(field)) {
           locateField(field, runCatching { locator() }.getOrNull())
        }
        return cacheMap.fields[field]?.toField()
    }

    fun locateField(field: FieldEnum, result: Pair<Class<*>, Field>?) {
        if (result != null && !cacheMap.fields.containsKey(field)) {
            val type = if (result.second.type.isArray)
                ClassInfo(result.second.type.componentType.name, true)
            else ClassInfo(result.second.type.name, false)
            cacheMap.fields[field] = FieldInfo(
                parent = ClassInfo(result.first.name, false),
                fieldName = result.second.name,
                type = type,
                private = Modifier.isPrivate(result.second.modifiers),
                static = Modifier.isStatic(result.second.modifiers),
            )
            update()
        }
    }

    fun findMethod(method: MethodEnum, locator: MethodLocator? = method.locator): Method? {
        if (locator != null && !cacheMap.methods.containsKey(method)) {
            locateMethod(method, runCatching { locator() }.getOrNull())
        }
        return cacheMap.methods[method]?.toMethod()
    }

    fun locateMethod(method: MethodEnum, result: Pair<Class<*>, Method>?) {
        if (result != null && !cacheMap.methods.containsKey(method)) {
            val returnType = if (result.second.returnType == Void.TYPE) ClassInfo.VOID
            else if (result.second.returnType.isArray) ClassInfo(result.second.returnType.componentType.name, true)
            else ClassInfo(result.second.returnType.name, false)
            cacheMap.methods[method] = MethodInfo(
                parent = ClassInfo(result.first.name, false),
                methodName = result.second.name,
                returnType = returnType,
                private = Modifier.isPrivate(result.second.modifiers),
                static = Modifier.isStatic(result.second.modifiers),
                args = result.second.parameterTypes.map {
                    if (it.isArray) ClassInfo(it.componentType.name, true)
                    else ClassInfo(it.name, false)
                },
            )
            update()
        }
    }

    fun update() {
        file.writeText(Json.encodeToString(cacheMap))
    }
}
