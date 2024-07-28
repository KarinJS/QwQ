package moe.qwq.miko.hooks

import android.content.Context
import com.tencent.qphone.base.util.CodecWarpper
import moe.fuqiuluo.processor.HookAction
import moe.qwq.miko.actions.ActionProcess
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.ext.EMPTY_BYTE_ARRAY
import moe.qwq.miko.ext.hookMethod
import moe.qwq.miko.internals.setting.QwQSetting
import moe.qwq.miko.utils.PlatformTools

@HookAction(desc = "拦截无用发包 + 修复主题验证 + 禁用更新检查")
class DefaultPacketHijacker: IAction {
    override val name: String = QwQSetting.DISABLE_USELESS_PACKET

    override fun onRun(ctx: Context) {
        if (!PlatformTools.isMsfProcess()) return
        CodecWarpper::class.java.hookMethod("nativeEncodeRequest").before {
            val cmd = it.args[5] as? String
                ?: return@before
            if (cmd in TRASH_PACKET) {
                it.result = EMPTY_BYTE_ARRAY
            } else if (cmd == "ProfileService.CheckUpdateReq") {
                it.result = EMPTY_BYTE_ARRAY
            }
        }
    }

    companion object {
        private val TRASH_PACKET = setOf(
            "AuthSvr.ThemeAuth", // 主题验证
            "trpc.qqva.uni_log_server.uni_log_server.Report",
            "OidbSvc.0x5eb_ForTheme",
            "QQClubComm.getNewFlag",
            "LightAppSvc.mini_app_ad.GetAd",
            "TianShu.GetAds", // noteworthy
            "trpc.qqshop.adpush.PushService.GetAd"
        )
    }

    override val process: ActionProcess
        get() = ActionProcess.MSF
}