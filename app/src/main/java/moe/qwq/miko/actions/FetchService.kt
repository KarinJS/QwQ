package moe.qwq.miko.actions

import android.content.Context
import com.tencent.qqnt.kernel.api.IKernelService
import com.tencent.qqnt.kernel.api.impl.KernelServiceImpl
import de.robv.android.xposed.XposedBridge
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moe.fuqiuluo.processor.HookAction
import moe.qwq.miko.ext.hookMethod
import moe.qwq.miko.internals.helper.NTServiceFetcher
import moe.qwq.miko.tools.PlatformTools

@HookAction("Fetch NT Service")
class FetchService: AlwaysRunAction() {
    override fun onRun(ctx: Context) {
        if (PlatformTools.isMqq()) {
            KernelServiceImpl::class.java.hookMethod("initService").after {
                val service = it.thisObject as IKernelService
                NTServiceFetcher.onFetch(service)
            }
        } else if (PlatformTools.isTim()) {
            // TIM 尚未进入 NTKernel
            XposedBridge.log("[QwQ] NTKernel init failed: tim not support NT")
        }
    }
}