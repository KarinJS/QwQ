package moe.qwq.miko.internals.helper

import com.tencent.qqnt.kernel.api.IKernelService
import com.tencent.qqnt.kernel.api.impl.MsgService
import com.tencent.qqnt.kernel.nativeinterface.IKernelGroupService
import com.tencent.qqnt.kernel.nativeinterface.IKernelMsgListener
import de.robv.android.xposed.XposedBridge
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import moe.qwq.miko.ext.hookMethod
import moe.qwq.miko.tools.PlatformTools
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
        initNTKernelListener(msgService)
    }

    private inline fun isInitForNt(hash: Int): Boolean {
        return hash == curKernelHash
    }

    private fun initNTKernelListener(msgService: MsgService) {
        XposedBridge.log("[QwQ] Init NT Kernel Listener.")
        msgService.javaClass.hookMethod("addMsgListener").before {
            val listener = it.args[0] as IKernelMsgListener
            listener.javaClass.hookMethod("onMsgRecall").before {
                if (QwQSetting.interceptRecall) it.result = Unit
            }
        }

    }

    val kernelService: IKernelService
        get() = iKernelService
}