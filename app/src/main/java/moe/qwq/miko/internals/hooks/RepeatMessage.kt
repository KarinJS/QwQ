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
import moe.fuqiuluo.processor.HookAction
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.ext.afterHook
import moe.qwq.miko.internals.helper.ContactHelper
import moe.qwq.miko.internals.helper.MessageTools
import moe.qwq.miko.internals.setting.QwQSetting.REPEAT_MESSAGE

@HookAction("复读机")
class RepeatMessage: IAction {
    override val name: String = REPEAT_MESSAGE

    override fun onRun(ctx: Context) {
        val ImageViewLazyField = AIOMsgFollowComponent::class.java.declaredFields.first {
            it.type.isInterface && it.type.name == Lazy::class.java.name
        }
        ImageViewLazyField.isAccessible = true

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

                //msgService.resendMsg(contact, msgId) { result, _ ->
                //    if (result != 0) {
                //        log("[QwQ] repeat message failed: (msgType = ${msgRecord.msgType})")
                //    }
                //}

                msgService.sendMsgWithMsgId(contact, newMsgId, msgRecord.elements) { result, _ ->
                    if (result != 0) {
                        log("[QwQ] repeat message failed: (msgType = ${msgRecord.msgType})")
                    }
                }
            }
        })
    }

    private fun disableRepeat(msgRecord: MsgRecord): Boolean {
        return when (msgRecord.msgType) {
            MsgConstant.KMSGTYPEWALLET, MsgConstant.KMSGTYPEGRAYTIPS -> true
            19 -> true
            else -> false
        }
    }
}