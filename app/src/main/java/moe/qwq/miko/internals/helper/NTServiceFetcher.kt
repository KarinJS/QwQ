@file:OptIn(ExperimentalSerializationApi::class)

package moe.qwq.miko.internals.helper

import com.google.protobuf.UnknownFieldSet
import com.tencent.qqnt.kernel.api.IKernelService
import com.tencent.qqnt.kernel.api.impl.MsgService
import com.tencent.qqnt.kernel.nativeinterface.MsgRecord
import de.robv.android.xposed.XposedBridge
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import moe.fuqiuluo.entries.MessagePush
import moe.qwq.miko.ext.hookMethod
import moe.qwq.miko.internals.AioListener
import moe.qwq.miko.internals.hooks.MessageHook

internal object NTServiceFetcher {
    private lateinit var iKernelService: IKernelService
    private var curKernelHash = 0
    private var isMsgListenerHookLoaded = false

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

        msgService.javaClass.hookMethod("addMsgListener").before {
            val listener = it.args[0]
            if (isMsgListenerHookLoaded) return@before
            listener.javaClass.hookMethod("onRecvMsg").before {
                val msgs = it.args[0] as ArrayList<MsgRecord>
                msgs.forEach { msg ->
                    MessageHook.tryHandleMessageDecrypt(msg)
                }
            }

            listener.javaClass.hookMethod("onAddSendMsg").before {
                val record = it.args[0] as MsgRecord
                MessageHook.tryHandleMessageDecrypt(record)
            }
        }

        kernelService.wrapperSession.javaClass.hookMethod("onMsfPush").before {
            runCatching {
                val cmd = it.args[0] as String
                val buffer = it.args[1] as? ByteArray ?: return@before
                when (cmd) {
                    "trpc.msg.register_proxy.RegisterProxy.InfoSyncPush" -> {
                        val unknownFieldSet = UnknownFieldSet.parseFrom(buffer)
                        AioListener.onInfoSyncPush(unknownFieldSet).onSuccess { new ->
                            it.args[1] = new.toByteArray()
                        }.onFailure {
                            XposedBridge.log(it)
                        }
                    }
                    "trpc.msg.olpush.OlPushService.MsgPush" -> {
                        val msgPush = ProtoBuf.decodeFromByteArray<MessagePush>(buffer)
                        if(AioListener.onMsgPush(msgPush)) {
                            it.result = Unit // 提前结束
                        } else {
                            return@before
                        }
                    }
                    else -> { }
                }
            }.onFailure {
                XposedBridge.log(it)
            }
        }
    }

    val kernelService: IKernelService
        get() = iKernelService
}