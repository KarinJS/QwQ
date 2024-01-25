@file:OptIn(DelicateCoroutinesApi::class)

package moe.qwq.miko.internals.hooks

import android.content.Context
import com.tencent.common.app.AppInterface
import com.tencent.qphone.base.remote.ToServiceMsg
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.ext.hookMethod
import moe.qwq.miko.internals.helper.AppRuntimeFetcher
import moe.qwq.miko.internals.setting.QwQSetting
import kotlin.random.Random
import kotlin.random.nextInt

/**
 * 拦截无用发包 + 修复主题验证 + 禁用更新检查
 */
class DefaultPacketHijacker: IAction {
    override fun invoke(ctx: Context) {
        val app = AppRuntimeFetcher.appRuntime
        if (app !is AppInterface) return

        AppInterface::class.java.hookMethod("sendToService").before {
            val toServiceMsg = it.args[0] as ToServiceMsg
            if (QwQSetting.disableUselessPacket && toServiceMsg.serviceCmd in TRASH_PACKET) {
                it.result = Unit
            } else if (QwQSetting.disableUpdateCheck && toServiceMsg.serviceCmd == "ProfileService.CheckUpdateReq") {
                it.result = Unit
            }

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
        private val TRASH_PACKET = arrayOf(
            "AuthSvr.ThemeAuth", // 主题验证
            "FeedCloudSvr.trpc.feedcloud.eeveeundealmsg.EeveeMsgChannel.FcUndealMsgs",
            "trpc.qqva.uni_log_server.uni_log_server.Report",
            "OidbSvc.0x5eb_ForTheme",
            "QQClubComm.getNewFlag",
            "LightAppSvc.mini_app_ad.GetAd",
            "TianShu.GetAds", // noteworthy
            "LightAppSvc.mini_app_info.GetAppInfoByLink",
            "SQQzoneSvc.getActiveFeeds"
        )
    }
}