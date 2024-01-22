package moe.qwq.miko.ext

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XCallback
import moe.fuqiuluo.xposed.loader.LuoClassloader
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier


internal typealias MethodHooker = (MethodHookParam) -> Unit

internal class XCHook {
    var before = nullableOf<MethodHooker>()
    var after = nullableOf<MethodHooker>()

    fun after(after: MethodHooker): XCHook {
        this.after.set(after)
        return this
    }

    fun before(before: MethodHooker): XCHook {
        this.before.set(before)
        return this
    }
}

internal fun Class<*>.hookMethod(name: String): XCHook {
    return XCHook().also {
        XposedBridge.hookAllMethods(this, name, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                it.before.getOrNull()?.invoke(param)
            }

            override fun afterHookedMethod(param: MethodHookParam) {
                it.after.getOrNull()?.invoke(param)
            }
        })
    }
}

internal fun beforeHook(ver: Int = XCallback.PRIORITY_DEFAULT, block: (param: XC_MethodHook.MethodHookParam) -> Unit): XC_MethodHook {
    return object :XC_MethodHook(ver) {
        override fun beforeHookedMethod(param: MethodHookParam) {
            block(param)
        }
    }
}

internal fun afterHook(ver: Int = XCallback.PRIORITY_DEFAULT, block: (param: XC_MethodHook.MethodHookParam) -> Unit): XC_MethodHook {
    return object :XC_MethodHook(ver) {
        override fun afterHookedMethod(param: MethodHookParam) {
            block(param)
        }
    }
}

object FuzzyClassKit {
    private val dic = arrayOf(
        "r" , "t", "o", "a", "b", "c", "e", "f", "d", "g", "h", "i", "j", "k", "l", "m", "n", "p", "q", "s", "t", "u", "v", "w", "x", "y", "z"
    )

    fun findClassByField(prefix: String, check: (Field) -> Boolean): Class<*>? {
        dic.forEach { className ->
            val clz = LuoClassloader.load("$prefix.$className")
            clz?.fields?.forEach {
                if (it.modifiers and Modifier.STATIC != 0
                    && !isBaseType(it.type)
                    && check(it)
                ) return clz
            }
        }
        return null
    }

    fun findClassesByField(
        classLoader: ClassLoader = FuzzyClassKit::class.java.classLoader ?: LuoClassloader,
        prefix: String,
        check: (Class<*>, Field) -> Boolean
    ): List<Class<*>> {
        val list = arrayListOf<Class<*>>()
        dic.forEach { className ->
            kotlin.runCatching {
                val clz = classLoader.loadClass("$prefix.$className")
                clz.declaredFields.forEach {
                    if (!isBaseType(it.type) && check(clz, it)) {
                        list.add(clz)
                    }
                }
            }
        }
        return list
    }


    fun findClassByMethod(prefix: String, isSubClass: Boolean = false, check: (Class<*>, Method) -> Boolean): Class<*>? {
        dic.forEach { className ->
            val clz = LuoClassloader.load("$prefix${if (isSubClass) "$" else "."}$className")
            clz?.methods?.forEach {
                if (check(clz, it)) return clz
            }
        }
        return null
    }

    fun findClassesByMethod(prefix: String, isSubClass: Boolean = false, check: (Class<*>, Method) -> Boolean): List<Class<*>> {
        val arrayList = arrayListOf<Class<*>>()
        dic.forEach { className ->
            val clz = LuoClassloader.load("$prefix${if (isSubClass) "$" else "."}$className")
            clz?.methods?.forEach {
                if (check(clz, it)) arrayList.add(clz)
            }
        }
        return arrayList
    }

    private fun isBaseType(clz: Class<*>): Boolean {
        return clz == Long::class.java ||
                clz == Double::class.java ||
                clz == Float::class.java ||
                clz == Int::class.java ||
                clz == Short::class.java ||
                clz == Char::class.java ||
                clz == Byte::class.java
    }
}

/**
 * 打印一个类的内部数据（调试）
 */
internal fun Any.toInnerValuesString(): String {
    val builder = StringBuilder()
    val clz = javaClass
    builder.append(clz.canonicalName)
    builder.append("========>\n")
    clz.declaredFields.forEach {
        if (!Modifier.isStatic(it.modifiers)) {
            if (!it.isAccessible) {
                it.isAccessible = true
            }
            builder.append(it.name)
            builder.append(" = ")
            when (val v = it.get(this)) {
                null -> builder.append("null")
                is ByteArray -> builder.append(v.toHexString())
                is Map<*, *> -> {
                    builder.append("{\n\t")
                    v.forEach { key, value ->
                        builder.append("\t")
                        builder.append(key)
                        builder.append(" = ")
                        builder.append(value)
                        builder.append("\n")
                    }
                    builder.append("}")
                }
                is List<*> -> {
                    builder.append("[\n\t")
                    v.forEach { value ->
                        builder.append("\t")
                        builder.append(value)
                        builder.append("\n")
                    }
                    builder.append("]")
                }
                else -> builder.append(v)
            }
            builder.append("\n")
        }
    }
    builder.append("=======================>\n")
    return builder.toString()
}

internal fun Any.toInnerValuesString(clz: Class<*>): String {
    val builder = StringBuilder()
    builder.append(clz.canonicalName)
    builder.append("========>\n")
    clz.declaredFields.forEach {
        if (!Modifier.isStatic(it.modifiers)) {
            if (!it.isAccessible) {
                it.isAccessible = true
            }
            builder.append(it.name)
            builder.append(" = ")
            when (val v = it.get(this)) {
                null -> builder.append("null")
                is ByteArray -> builder.append(v.toHexString())
                is Map<*, *> -> {
                    builder.append("{\n\t")
                    v.forEach { key, value ->
                        builder.append("\t")
                        builder.append(key)
                        builder.append(" = ")
                        builder.append(value)
                        builder.append("\n")
                    }
                    builder.append("}")
                }
                is List<*> -> {
                    builder.append("[\n\t")
                    v.forEach { value ->
                        builder.append("\t")
                        builder.append(value)
                        builder.append("\n")
                    }
                    builder.append("]")
                }
                else -> builder.append(v)
            }
            builder.append("\n")
        }
    }
    builder.append("=======================>\n")
    return builder.toString()
}
