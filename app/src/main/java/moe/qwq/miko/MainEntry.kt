package moe.qwq.miko

import android.content.Context
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.callbacks.XC_LoadPackage
import moe.fuqiuluo.xposed.loader.LuoClassloader
import moe.qwq.miko.actions.ActionProcess
import moe.qwq.miko.ext.FuzzyClassKit
import moe.qwq.miko.utils.MMKVUtils
import moe.qwq.miko.utils.PlatformTools
import moe.qwq.miko.ext.afterHook
import mqq.app.MobileQQ
import java.lang.reflect.Field
import java.lang.reflect.Modifier

class MainEntry: IXposedHookLoadPackage {
    private var firstStageInit = false

    override fun handleLoadPackage(param: XC_LoadPackage.LoadPackageParam) {
        if (param.packageName == PACKAGE_NAME_QQ) {
            entryQQ(param.classLoader)
        }
    }

    private fun entryQQ(classLoader: ClassLoader) {
        val startup = afterHook(51) { param ->
            try {
                val loader = param.thisObject.javaClass.classLoader!!
                LuoClassloader.ctxClassLoader = loader
                val clz = loader
                    .loadClass("com.tencent.common.app.BaseApplicationImpl")
                val field = clz.declaredFields.first {
                    it.type == clz
                }
                val app: Context? = field.get(null) as? Context
                if (app != null) {
                    execStartupInit(app)
                } else {
                    log("[QwQ] Unable to fetch context")
                }
            } catch (e: Throwable) {
                log(e)
            }
        }

        kotlin.runCatching {
            val loadDex = classLoader.loadClass("com.tencent.mobileqq.startup.step.LoadDex")
            loadDex.declaredMethods
                .filter { it.returnType.equals(java.lang.Boolean.TYPE) && it.parameterTypes.isEmpty() }
                .forEach {
                    XposedBridge.hookMethod(it, startup)
                }
            firstStageInit = true
        }.onFailure {
            // For NT QQ
            val fieldList = arrayListOf<Field>()
            FuzzyClassKit.findClassesByField(classLoader, "com.tencent.mobileqq.startup.task.config") { _, field ->
                (field.type == HashMap::class.java || field.type == Map::class.java) && Modifier.isStatic(field.modifiers)
            }.forEach {
                it.declaredFields.forEach { field ->
                    if ((field.type == HashMap::class.java || field.type == Map::class.java)
                        && Modifier.isStatic(field.modifiers))
                        fieldList.add(field)
                }
            }
            fieldList.forEach {
                if (!it.isAccessible) it.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                (it.get(null) as? Map<String, Class<*>>).also { map ->
                    if (map == null) log("[QwQ] Not found matched entry")
                    else map.forEach { (key, clazz) ->
                        if (key.contains("LoadDex", ignoreCase = true)) {
                            clazz.declaredMethods.forEach {
                                if (it.parameterTypes.size == 1 && it.parameterTypes[0] == Context::class.java) {
                                    log("[QwQ] Try load fetchEntry's injector.")
                                    XposedBridge.hookMethod(it, startup)
                                }
                            }
                        }
                    }
                }
            }
            firstStageInit = true
        }
    }

    private fun execStartupInit(ctx: Context) {
        if (secStaticStageInited) return

        val classLoader = ctx.classLoader.also { requireNotNull(it) }
        LuoClassloader.hostClassLoader = classLoader

        if(injectClassloader(MainEntry::class.java.classLoader)) {
            if ("1" != System.getProperty("qwq_flag")) {
                System.setProperty("qwq_flag", "1")
            } else return

            log("[QwQ] Process Name = " + MobileQQ.getMobileQQ().qqProcessName)

            secStaticStageInited = true

            if(PlatformTools.isTim()) {
                MMKVUtils.initMMKV(ctx)
            }

            ActionManager.runFirst(ctx, when {
                PlatformTools.isMainProcess() -> ActionProcess.MAIN
                PlatformTools.isMsfProcess() -> ActionProcess.MSF
                else -> ActionProcess.ALL
            })
        }
    }

    private fun injectClassloader(moduleLoader: ClassLoader?): Boolean {
        if (moduleLoader != null) {
            if (kotlin.runCatching {
                    moduleLoader.loadClass("mqq.app.MobileQQ")
                }.isSuccess) {
                log("[QwQ] ModuleClassloader already injected.")
                return true
            }

            val parent = moduleLoader.parent
            val field = ClassLoader::class.java.declaredFields
                .first { it.name == "parent" }
            field.isAccessible = true

            field.set(LuoClassloader, parent)

            if (LuoClassloader.load("mqq.app.MobileQQ") == null) {
                log("[QwQ] LuoClassloader init failed.")
                return false
            }

            field.set(moduleLoader, LuoClassloader)

            return kotlin.runCatching {
                Class.forName("mqq.app.MobileQQ")
            }.onFailure {
                log("[QwQ] Classloader inject failed.")
            }.onSuccess {
                log("[QwQ] Classloader inject successfully.")
            }.isSuccess
        }
        return false
    }

    companion object {
        @JvmStatic var secStaticStageInited = false

        internal const val PACKAGE_NAME_QQ = "com.tencent.mobileqq"
        internal const val PACKAGE_NAME_QQ_INTERNATIONAL = "com.tencent.mobileqqi"
        internal const val PACKAGE_NAME_QQ_LITE = "com.tencent.qqlite"
        internal const val PACKAGE_NAME_TIM = "com.tencent.tim"
    }
}