package moe.qwq.miko.hooks

import android.content.Context
import android.view.View
import com.tencent.mobileqq.activity.VisitorsActivity
import com.tencent.mobileqq.data.CardProfile
import com.tencent.mobileqq.profile.vote.VoteHelper
import com.tencent.mobileqq.profilecard.base.component.AbsProfileHeaderComponent
import com.tencent.mobileqq.vas.api.IVasSingedApi
import de.robv.android.xposed.XposedBridge
import moe.fuqiuluo.entries.MethodEnum.VoteHelperVote
import moe.fuqiuluo.processor.HookAction
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.ext.hookMethod
import moe.qwq.miko.internals.QQInterfaces
import moe.qwq.miko.internals.locators.DvmLocator
import moe.qwq.miko.internals.setting.QwQSetting.ONE_KEY_LIKE

@HookAction("一键20赞")
class OneClickLike: IAction {
    override val name: String = ONE_KEY_LIKE

    override fun onRun(ctx: Context) {
        val vote = DvmLocator.findMethod(VoteHelperVote)
        val voteHelperField = VisitorsActivity::class.java.declaredFields.firstOrNull {
            it.type == VoteHelper::class.java
        }
        if (voteHelperField == null || vote == null) {
            throw RuntimeException("VoteHelper or VoteHelper.vote not found")
        }

        voteHelperField.isAccessible = true
        VisitorsActivity::class.java.hookMethod("onClick").before {
            val view = it.args[0] as View
            val profile = view.tag
            if (profile == null || profile !is CardProfile) return@before
            val voteHelper = voteHelperField.get(it.thisObject) as VoteHelper
            for (i in 0..<getMaxVoteCount()) {
                vote.invoke(voteHelper, profile, view)
            }
            it.result = Unit
        }

        AbsProfileHeaderComponent::class.java.hookMethod("handleVoteBtnClickForGuestProfile").before {
            for (i in 0..<getMaxVoteCount()) {
                XposedBridge.invokeOriginalMethod(it.method, it.thisObject, it.args)
            }
        }
    }

    private fun getMaxVoteCount(): Int {
        return if (isSVip()) 20 else 10
    }

    private fun isSVip(): Boolean {
        kotlin.runCatching {
            val service = QQInterfaces.app.getRuntimeService(IVasSingedApi::class.java, "all")
            return service.vipStatus.isSVip
        }.onFailure {
            XposedBridge.log(it)
        }
        return true
    }
}