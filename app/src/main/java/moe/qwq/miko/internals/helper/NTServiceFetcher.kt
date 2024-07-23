@file:OptIn(ExperimentalSerializationApi::class, DelicateCoroutinesApi::class)
@file:Suppress("UNCHECKED_CAST")

package moe.qwq.miko.internals.helper

import com.google.protobuf.UnknownFieldSet
import com.tencent.qqnt.kernel.api.IKernelService
import com.tencent.qqnt.kernel.api.impl.MsgService
import de.robv.android.xposed.XposedBridge
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import moe.fuqiuluo.entries.MessagePush
import moe.qwq.miko.ext.hookMethod
import moe.qwq.miko.internals.AioListener

internal object NTServiceFetcher {
    private lateinit var iKernelService: IKernelService
    private var curKernelHash = 0
    //private var isMsgListenerHookLoaded = false

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

/*        msgService.javaClass.hookMethod("addMsgListener").before {
            val listener = it.args[0]
            if (isMsgListenerHookLoaded) return@before

            val hookV1 = beforeHook {
                val newMsgs = arrayListOf<MsgRecord>()
                (it.args[0] as ArrayList<MsgRecord>).forEach { msg ->
                    MessageHook.tryHandleMessageDecrypt(msg)
                    newMsgs.add(msg)
                }
                it.args[0] = newMsgs
            }
            val hookV2 = beforeHook {
                val msg = it.args[0] as MsgRecord
                MessageHook.tryHandleMessageDecrypt(msg)
            }

            listener.javaClass.hookMethod("onRecvMsg", hookV1)
            listener.javaClass.hookMethod("onMsgInfoListAdd", hookV1)
            listener.javaClass.hookMethod("onMsgInfoListUpdate", hookV1)
            listener.javaClass.hookMethod("onAddSendMsg", hookV2)

            isMsgListenerHookLoaded = true
        }*/

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
                        if(AioListener.onMsgPush(it, msgPush)) {
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