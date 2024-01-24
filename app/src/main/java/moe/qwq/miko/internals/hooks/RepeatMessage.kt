@file:Suppress("LocalVariableName", "UNCHECKED_CAST")
package moe.qwq.miko.internals.hooks

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.tencent.mobileqq.aio.msglist.holder.component.msgfollow.AIOMsgFollowComponent
import com.tencent.mobileqq.qroute.QRoute
import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import com.tencent.qqnt.kernel.nativeinterface.MsgRecord
import com.tencent.qqnt.msg.api.IMsgService
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.ext.afterHook
import moe.qwq.miko.ext.toast
import moe.qwq.miko.internals.helper.ContactHelper
import moe.qwq.miko.internals.helper.MessageTools
import moe.qwq.miko.internals.helper.NTServiceFetcher
import moe.qwq.miko.internals.helper.msgService
import moe.qwq.miko.internals.setting.QwQSetting
import moe.qwq.miko.internals.setting.QwQSetting.REPEAT_MESSAGE

class RepeatMessage: IAction {
    override fun invoke(ctx: Context) {
        val setting = QwQSetting.getSetting(REPEAT_MESSAGE)
        runCatching {
            val ImageViewLazyField = AIOMsgFollowComponent::class.java.declaredFields.first {
                it.type.isInterface && it.type.name == Lazy::class.java.name
            }
            ImageViewLazyField.isAccessible = true
            if (!QwQSetting.repeatMessage) return
            XposedBridge.hookMethod(AIOMsgFollowComponent::class.java.declaredMethods.first {
                it.parameterCount == 3 && it.parameterTypes[0] == Integer.TYPE && it.parameterTypes[2] == List::class.java
            }, afterHook {
                val imageView = XposedHelpers.callMethod(ImageViewLazyField.get(it.thisObject), "getValue") as ImageView
                if (imageView.context.javaClass.name.contains("MultiForwardActivity")) {
                    return@afterHook
                }
                val msgObject = it.args[1]
                //val msgId = XposedHelpers.callMethod(msgObject, "getMsgId") as Long
                val msgRecord = XposedHelpers.callMethod(msgObject, "getMsgRecord") as MsgRecord
                if (disableRepeat(msgRecord) || msgRecord.elements.isEmpty()) return@afterHook
                if (imageView.visibility != View.VISIBLE) {
                    imageView.visibility = View.VISIBLE
                }
                imageView.setOnClickListener clickPlusIcon@{
                    val contact = ContactHelper.generateContactV2(msgRecord.chatType, msgRecord.peerUid)
                    val newMsgId = MessageTools.generateMsgUniseq(msgRecord.chatType)
                    val msgService = QRoute.api(IMsgService::class.java)
                    msgService.sendMsgWithMsgId(contact, newMsgId, msgRecord.elements) { result, _ ->
                        if (result != 0) {
                            log("[QwQ] repeat message failed: (msgType = ${msgRecord.msgType})")
                        }
                    }
                }
            })
        }.onFailure {
            setting.isFailed = true
            log(it)
        }
    }

    private fun disableRepeat(msgRecord: MsgRecord): Boolean {
        return when (msgRecord.msgType) {
            MsgConstant.KMSGTYPEWALLET, MsgConstant.KMSGTYPEGRAYTIPS -> true
            19 -> true
            else -> false
        }
    }
}