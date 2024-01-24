package moe.qwq.miko.internals.hooks

import android.content.Context
import android.view.View
import com.tencent.mobileqq.activity.VisitorsActivity
import com.tencent.mobileqq.data.CardProfile
import com.tencent.mobileqq.profile.vote.VoteHelper
import com.tencent.mobileqq.profilecard.base.component.AbsProfileHeaderComponent
import de.robv.android.xposed.XposedBridge
import moe.fuqiuluo.entries.MethodEnum.VoteHelperVote
import moe.fuqiuluo.entries.MethodEnum.valueOf
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.ext.hookMethod
import moe.qwq.miko.internals.helper.DvmLocator
import moe.qwq.miko.internals.setting.QwQSetting
import moe.qwq.miko.internals.setting.QwQSetting.ONE_KEY_LIKE
import java.lang.reflect.Method


class OneClickLike: IAction {
    override fun invoke(ctx: Context) {
        val setting = QwQSetting.getSetting(ONE_KEY_LIKE)
        runCatching {
            val vote = DvmLocator.findMethod(VoteHelperVote)
            val voteHelperField = VisitorsActivity::class.java.declaredFields.firstOrNull {
                it.type == VoteHelper::class.java
            }
            if (voteHelperField == null || vote == null) {
                setting.isFailed = true
                return
            }

            if (!QwQSetting.oneClickLike) return@runCatching

            voteHelperField.isAccessible = true
            VisitorsActivity::class.java.hookMethod("onClick").before {
                val view = it.args[0] as View
                val profile = view.tag
                if (profile == null || profile !is CardProfile) return@before
                val voteHelper = voteHelperField.get(it.thisObject) as VoteHelper
                for (i in 0..20) {
                    vote.invoke(voteHelper, profile, view)
                }
                it.result = Unit
            }

            AbsProfileHeaderComponent::class.java.hookMethod("handleVoteBtnClickForGuestProfile").before {
                val selfMethod = it.method as Method
                if (!selfMethod.isAccessible) {
                    selfMethod.isAccessible = true
                }
                for (i in 0..19) {
                    selfMethod.invoke(it.thisObject, it.args[0])
                }
            }
        }.onFailure {
            setting.isFailed = true
        }
    }
}