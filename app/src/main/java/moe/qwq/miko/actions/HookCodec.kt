@file:Suppress("LocalVariableName", "SpellCheckingInspection")
package moe.qwq.miko.actions

import android.content.Context
import de.robv.android.xposed.XposedBridge
import moe.fuqiuluo.processor.HookAction
import moe.fuqiuluo.xposed.loader.LuoClassloader
import moe.qwq.miko.ext.EMPTY_BYTE_ARRAY
import moe.qwq.miko.ext.beforeHook
import moe.qwq.miko.ext.hookMethod
import moe.qwq.miko.ext.toHexString
import moe.qwq.miko.ext.toInnerValuesString
import moe.qwq.miko.internals.hijackers.IHijacker
import moe.qwq.miko.utils.PlatformTools

@HookAction(desc = "HookWrapperCodec实现捕获抓包")
class HookCodec: AlwaysRunAction() {
    override fun onRun(ctx: Context) {
        val CodecWarpper = LuoClassloader.load("com.tencent.qphone.base.util.CodecWarpper")
        if (CodecWarpper == null) {
            XposedBridge.log("[QwQ] 无法注入CodecWarpper！")
            return
        }/* else {
            XposedBridge.log("[QwQ] 注入CodecWarpper成功！")
        }*/
        CodecWarpper.hookMethod("nativeEncodeRequest", beforeHook {
            //                                              0          1           2            3           4            5            6        7        8          9        10       11       12              13            14          15            16
            // public static byte[] nativeEncodeRequest(int seq, String str, String str2, String str3, String str4, String cmd, byte[] bArr, int i2, int i3, String uin, byte b, byte b2, byte[] buffer, boolean z)
            // public static byte[] nativeEncodeRequest(int seq, String str, String str2, String str3, String str4, String cmd, byte[] bArr, int i2, int i3, String uin, byte b, byte b2, byte[] bArr2,  byte[] bArr3, byte[] buffer, boolean z)
            // public static byte[] nativeEncodeRequest(int seq, String str, String str2, String str3, String str4, String cmd, byte[] bArr, int i2, int i3, String uin, byte b, byte b2, byte b3,       byte[] bArr2, byte[] bArr3, byte[] buffer, boolean z)
            //XposedBridge.log(it.toInnerValuesString())

            val uin: String
            val seq: Int
            val buffer: ByteArray
            val cmd: String
            val bufferIndex: Int
            val msgCookie: ByteArray?
            when(it.args.size) {
                14 -> {
                    seq = it.args[0] as Int
                    cmd = it.args[5] as String
                    buffer = it.args[12] as ByteArray
                    bufferIndex = 12
                    msgCookie = it.args[6] as? ByteArray
                    uin = it.args[9] as String
                }
                16 -> {
                    seq = it.args[0] as Int
                    cmd = it.args[5] as String
                    buffer = it.args[14] as ByteArray
                    bufferIndex = 14
                    msgCookie = it.args[6] as? ByteArray
                    uin = it.args[9] as String
                }
                17 -> {
                    seq = it.args[0] as Int
                    cmd = it.args[5] as String
                    buffer = it.args[15] as ByteArray
                    bufferIndex = 15
                    msgCookie = it.args[6] as? ByteArray
                    uin = it.args[9] as String
                }
                else -> throw RuntimeException("nativeEncodeRequest参数个数不匹配")
            }
            //XposedBridge.log("nativeEncodeRequest: $seq $cmd ${buffer.toHexString()}")
            if (hijackers.firstOrNull { it.command == cmd }?.onHandle(it, uin, cmd, seq, buffer, bufferIndex) == true) {
                it.result = EMPTY_BYTE_ARRAY
            }
        })
    }

    override val process: ActionProcess = ActionProcess.MSF

    companion object {
        val hijackers = arrayListOf<IHijacker>()
    }
}