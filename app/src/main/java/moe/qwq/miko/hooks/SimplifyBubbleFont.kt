package moe.qwq.miko.hooks

import android.content.Context
import com.tencent.qqnt.kernel.nativeinterface.VASMsgBubble
import com.tencent.qqnt.kernel.nativeinterface.VASMsgFont
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import moe.fuqiuluo.processor.HookAction
import moe.fuqiuluo.xposed.loader.LuoClassloader
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.ext.beforeHook
import moe.qwq.miko.ext.hookMethod
import moe.qwq.miko.internals.setting.QwQSetting
import moe.qwq.miko.utils.PlatformTools
import moe.qwq.miko.utils.PlatformTools.QQ_9_0_15_VER
import mqq.app.MobileQQ
import java.io.File
import java.lang.reflect.Modifier

@HookAction("简化气泡字体")
class SimplifyBubbleFont: IAction {
    private val paths = arrayOf("/bubble_info", "/files/bubble_info", "/files/bubble_paster", "/files/vas_material_folder/bubble_dir")

    override fun onRun(ctx: Context) {
        if (PlatformTools.getQQVersionCode() >= QQ_9_0_15_VER) {
            XposedBridge.hookAllConstructors(VASMsgBubble::class.java, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val v = param.thisObject as VASMsgBubble
                    v.bubbleId = 0
                    v.subBubbleId = 0
                }
            })
            XposedBridge.hookAllConstructors(VASMsgFont::class.java, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val v = param.thisObject as VASMsgFont
                    v.fontId = 0
                    v.magicFontType = 0
                }
            })
        } else if (PlatformTools.isQQnt()) {
            val hook = beforeHook {
                it.result = 0
            }
            VASMsgBubble::class.java.hookMethod("getBubbleId", hook)
            VASMsgBubble::class.java.hookMethod("getSubBubbleId", hook)
            VASMsgFont::class.java.hookMethod("getFontId", hook)
            VASMsgFont::class.java.hookMethod("getMagicFontType", hook)
        } else {
            updateChmod(paths, true)
            val kAIOMsgItem = LuoClassloader.load("com.tencent.mobileqq.aio.msg.AIOMsgItem")
            val kAIOBubbleSkinInfo = LuoClassloader.load("com.tencent.mobileqq.aio.msglist.holder.skin.AIOBubbleSkinInfo")
            kAIOMsgItem?.declaredMethods?.first {
                !Modifier.isStatic(it.modifiers) && it.returnType == Void.TYPE && it.parameterTypes.size == 1 && it.parameterTypes[0] == kAIOBubbleSkinInfo
            }?.let { method ->
                XposedBridge.hookMethod(method, beforeHook {
                    it.args[0] = null
                    it.result = null
                })
            }
        }
    }

    private fun updateChmod(paths: Array<String>, enabled: Boolean) {
        for (path in paths) {
            val dir = File(MobileQQ.getContext().getExternalFilesDir(null)!!.parentFile!!.absolutePath + path)
            val curr = !dir.exists() || !dir.canRead()
            if (dir.exists()) {
                if (enabled && !curr) {
                    dir.setReadable(false)
                    dir.setWritable(false)
                    dir.setExecutable(false)
                }
                if (!enabled && curr) {
                    dir.setWritable(true)
                    dir.setReadable(true)
                    dir.setExecutable(true)
                }
            }
        }
    }

    override val name: String = QwQSetting.SIMPLIFY_BUBBLE_FONT

    override fun canRun(): Boolean {
        val setting by QwQSetting.getSetting<Boolean>(name)
        if (!setting) {
            updateChmod(paths, false)
        }
        return setting
    }
}