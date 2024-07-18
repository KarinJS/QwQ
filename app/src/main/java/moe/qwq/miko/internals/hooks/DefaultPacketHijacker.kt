package moe.qwq.miko.internals.hooks

import android.content.Context
import com.tencent.qphone.base.util.CodecWarpper
import de.robv.android.xposed.XposedBridge.log
import moe.fuqiuluo.processor.HookAction
import moe.qwq.miko.actions.ActionProcess
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.ext.hookMethod
import moe.qwq.miko.internals.setting.QwQSetting
import moe.qwq.miko.tools.PlatformTools

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
            "FeedCloudSvr.trpc.feedcloud.eeveeundealmsg.EeveeMsgChannel.FcUndealMsgs",
            "trpc.qqva.uni_log_server.uni_log_server.Report",
            "OidbSvc.0x5eb_ForTheme",
            "QQClubComm.getNewFlag",
            "LightAppSvc.mini_app_ad.GetAd",
            "TianShu.GetAds", // noteworthy
            "LightAppSvc.mini_app_info.GetAppInfoByLink",
            "SQQzoneSvc.getActiveFeeds",
            "trpc.qqshop.adpush.PushService.GetAd"
        )
        private val TEST_PACKET = setOf(
            "SQQzoneSvc.getUndealCount",
            "trpc.qq_new_tech.status_svc.StatusService.SsoHeartBeat",
            "Heartbeat.Alive"
        )
    }

    override val process: ActionProcess
        get() = ActionProcess.MSF
}