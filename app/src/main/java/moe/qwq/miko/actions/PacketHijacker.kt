package moe.qwq.miko.actions

import android.content.Context
import com.tencent.msf.service.protocol.pb.SSOLoginMerge
import com.tencent.qphone.base.remote.FromServiceMsg
import com.tencent.qphone.base.util.CodecWarpper
import de.robv.android.xposed.XposedBridge
import moe.fuqiuluo.entries.ClassEnum.CodecWarpperImpl
import moe.fuqiuluo.processor.HookAction
import moe.qwq.miko.ext.EMPTY_BYTE_ARRAY
import moe.qwq.miko.ext.hookMethod
import moe.qwq.miko.ext.slice
import moe.qwq.miko.internals.helper.DvmLocator
import moe.qwq.miko.internals.hijackers.IHijacker
import moe.qwq.miko.internals.setting.QwQSetting
import moe.qwq.miko.tools.PlatformTools
import java.util.concurrent.atomic.AtomicBoolean

@HookAction("包拦截工具")
class PacketHijacker: AlwaysRunAction() {
    override fun onRun(ctx: Context) {
        if (!PlatformTools.isMsfProcess()) return
        try {
            DvmLocator.findClass(CodecWarpperImpl)?.let {
                hookReceive(it)
                return
            }
            val isInit = AtomicBoolean(false)
            CodecWarpper::class.java.hookMethod("init").after {
                if (isInit.get()) return@after
                hookReceive(it.thisObject.javaClass)
                isInit.lazySet(true)
            }
            CodecWarpper::class.java.hookMethod("nativeOnReceData").before {
                if (isInit.get()) return@before
                hookReceive(it.thisObject.javaClass)
                isInit.lazySet(true)
            }
        } catch (e: Throwable) {
            XposedBridge.log(e)
        }
    }

    private fun hookReceive(tzClass: Class<*>) {
        //val onResponse = tzClass.getDeclaredMethod("onResponse", Integer.TYPE, Any::class.java, Integer.TYPE)
        DvmLocator.locateClass(CodecWarpperImpl, tzClass)
        tzClass.hookMethod("onResponse").before {
            val from = it.args[1] as FromServiceMsg
            try {
                if ("SSO.LoginMerge" == from.serviceCmd) {
                    val merge = SSOLoginMerge.BusiBuffData()
                        .mergeFrom(from.wupBuffer.slice(4))
                    val busiBufVec = merge.BusiBuffVec.get()
                    busiBufVec.forEach { item ->
                        val cmd = item.ServiceCmd.get()
                        if (packetHijackers.containsKey(cmd)) {
                            if(packetHijackers[cmd]!!.onHandle(FromServiceMsg().apply {
                                this.requestSsoSeq = item.SeqNo.get()
                                this.serviceCmd = cmd
                                putWupBuffer(item.BusiBuff.get().toByteArray())
                            })) {
                                busiBufVec.remove(item)
                            }
                        }
                    }
                    merge.BusiBuffVec.set(busiBufVec)
                    from.putWupBuffer(merge.toByteArray())
                } else {
                    val cmd = from.serviceCmd
                    if (packetHijackers.containsKey(cmd)) {
                        if(packetHijackers[cmd]!!.onHandle(from)) {
                            //from.serviceCmd = "Injected_${from.serviceCmd}"
                            // Adjust the interception plan to allow him to receive the packet,
                            // but need to prompt for failure
                            from.setBusinessFail(1)
                            from.putWupBuffer(EMPTY_BYTE_ARRAY)
                        }
                    }
                }
            } finally {
                it.args[1] = from
            }
        }
    }

    override val process: ActionProcess
        get() = ActionProcess.MSF

    companion object {
        private val packetHijackers = hashMapOf<String, IHijacker>()
    }
}