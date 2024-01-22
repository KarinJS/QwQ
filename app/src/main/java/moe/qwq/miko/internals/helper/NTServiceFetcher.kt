@file:OptIn(ExperimentalSerializationApi::class)

package moe.qwq.miko.internals.helper

import com.tencent.qqnt.kernel.api.IKernelService
import com.tencent.qqnt.kernel.api.impl.MsgService
import de.robv.android.xposed.XposedBridge
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import moe.fuqiuluo.entries.InfoSyncPush
import moe.fuqiuluo.entries.MessagePush
import moe.qwq.miko.ext.hookMethod
import moe.qwq.miko.internals.AioListener
import moe.qwq.miko.tools.QwQSetting

internal object NTServiceFetcher {
    private lateinit var iKernelService: IKernelService
    private var curKernelHash = 0

    fun onFetch(service: IKernelService) {
        val msgService = service.msgService ?: return
        val curHash = service.hashCode() + msgService.hashCode()
        if (isInitForNt(curHash)) return

        curKernelHash = curHash
        this.iKernelService = service
        initNTKernel(msgService)
    }

    private inline fun isInitForNt(hash: Int): Boolean {
        return hash == curKernelHash
    }

    private fun initNTKernel(msgService: MsgService) {
        XposedBridge.log("[QwQ] Init NT Kernel.")

        QwQSetting.getSetting(QwQSetting.INTERCEPT_RECALL).isFailed = false
        kernelService.wrapperSession.javaClass.hookMethod("onMsfPush").before {
            val cmd = it.args[0] as String
            val buffer = it.args[1] as ByteArray
            if (cmd == "trpc.msg.register_proxy.RegisterProxy.InfoSyncPush") {
                val syncPush = ProtoBuf.decodeFromByteArray<InfoSyncPush>(buffer)
                if (AioListener.onInfoSyncPush(syncPush)) {
                    it.result = Unit
                }
            } else if (cmd == "trpc.msg.olpush.OlPushService.MsgPush") {
                val msgPush = ProtoBuf.decodeFromByteArray<MessagePush>(buffer)
                if (AioListener.onMsgPush(msgPush)) {
                    it.result = Unit
                }
            }
        }


    }

    val kernelService: IKernelService
        get() = iKernelService
}