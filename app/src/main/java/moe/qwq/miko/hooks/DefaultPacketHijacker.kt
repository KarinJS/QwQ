package moe.qwq.miko.hooks

import android.content.Context
import com.tencent.qphone.base.util.CodecWarpper
import moe.fuqiuluo.processor.HookAction
import moe.qwq.miko.actions.ActionProcess
import moe.qwq.miko.actions.IAction
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
                it.result = Unit
            } else if (cmd == "ProfileService.CheckUpdateReq") {
                it.result = Unit
            }

            /*  仅为测试
            else if (cmd in TEST_PACKET) {
                log("[QwQ 已拦截发送包] cmd: $cmd")
                it.result = Unit
            } */

            /* 协议一键赞 问题多多
            else if (QwQSetting.oneClickLike && toServiceMsg.serviceCmd == "VisitorSvc.ReqFavorite" &&
                !toServiceMsg.extraData.getBoolean("qwq", false)
                ) {
                toServiceMsg.extraData.putBoolean("qwq", true)
                GlobalScope.launch {
                    var total = 0
                    while (total < 20) {
                        var random = Random.nextInt(1 .. 10)
                        if (20 - total < random) {
                            random = 20 - total
                        }
                        toServiceMsg.extraData.putInt("iCount", random)
                        app.sendToService(toServiceMsg)
                        total += random
                    }
                }
                it.result = Unit
            }*/
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
            "LightAppSvc.mini_app_info.GetAppInfoByLink",
            "trpc.qqshop.adpush.PushService.GetAd"
        )
    }

    override val process: ActionProcess
        get() = ActionProcess.MSF
}