@file:Suppress("LocalVariableName", "SpellCheckingInspection")
package moe.qwq.miko.actions

import android.content.Context
import de.robv.android.xposed.XposedBridge
import moe.fuqiuluo.processor.HookAction
import moe.fuqiuluo.xposed.loader.LuoClassloader
import moe.qwq.miko.ext.beforeHook
import moe.qwq.miko.ext.hookMethod

@HookAction(desc = "HookWrapperCodec实现捕获抓包")
class HookCodec: AlwaysRunAction() {
    override fun onRun(ctx: Context) {
        val CodecWarpper = LuoClassloader.load("com.tencent.qphone.base.util.CodecWarpper")
        if (CodecWarpper == null) {
            XposedBridge.log("[QwQ] 无法注入CodecWarpper！")
            return
        }
        CodecWarpper.hookMethod("nativeEncodeRequest", beforeHook {

        })
    }
}