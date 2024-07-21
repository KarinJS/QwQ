package moe.qwq.miko.internals.hooks

import android.content.Context
import com.tencent.mobileqq.qroute.QRoute
import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import com.tencent.qqnt.kernel.nativeinterface.MsgElement
import com.tencent.qqnt.kernel.nativeinterface.MsgRecord
import com.tencent.qqnt.kernel.nativeinterface.TextElement
import com.tencent.qqnt.msg.api.IMsgService
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import moe.fuqiuluo.processor.HookAction
import moe.qwq.miko.actions.ActionProcess
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.internals.setting.QwQSetting

@HookAction("发送消息预劫持")
class MessageHook: IAction {
    private fun handleMessageBody(msgs: ArrayList<MsgElement>) {
        if (msgs.isActionMsg()) return
        val tail by QwQSetting.getSetting<String>(name)
        val encrypt by QwQSetting.getSetting<String>(QwQSetting.MESSAGE_ENCRYPT)
        if (tail.isNotBlank()) {
            handleMessageTail(msgs, tail)
        }
        if (encrypt.isNotBlank()) {
            handleMessageEncrypt(msgs, encrypt)
        }
    }

    private fun handleMessageTail(msgs: ArrayList<MsgElement>, tail: String) { // 给消息添加小尾巴
        msgs.add(MsgElement().apply {
            this.elementType = MsgConstant.KELEMTYPETEXT
            this.textElement = TextElement()
            this.textElement.content = tail
        })
    }

    private fun handleMessageEncrypt(msgs: ArrayList<MsgElement>, encryptKey: String) {

    }

    override fun onRun(ctx: Context) {
/*        val msgService = QRoute.api(IMsgService::class.java)
        msgService.javaClass.methods.forEach {
            if ((it.name == "sendMsg" || it.name == "sendMsgWithMsgId") && it.parameterTypes.size > 3) {
                val isV1Hook = it.parameterTypes[1] == ArrayList::class.java
                val isV2Hook = it.parameterTypes[2] == ArrayList::class.java
                if (isV1Hook) {
                    XposedBridge.hookMethod(it, object: XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            hookSendMsgV1(param)
                        }
                    })
                } else if (isV2Hook) {
                    XposedBridge.hookMethod(it, object: XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            hookSendMsgV2(param)
                        }
                    })
                }
            }
        }*/
        com.tencent.qqnt.kernel.api.impl.MsgService::class.java.methods.forEach {
            if ((it.name == "sendMsg") && it.parameterTypes.size > 3) {
                val isV1Hook = it.parameterTypes[1] == ArrayList::class.java
                val isV2Hook = it.parameterTypes[2] == ArrayList::class.java
                if (isV1Hook) {
                    XposedBridge.hookMethod(it, object: XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            hookSendMsgV1(param)
                        }
                    })
                } else if (isV2Hook) {
                    XposedBridge.hookMethod(it, object: XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            hookSendMsgV2(param)
                        }
                    })
                }
            }
        }
    }

    private fun hookSendMsgV1(params: XC_MethodHook.MethodHookParam) {
        val msgs = params.args[1] as ArrayList<MsgElement>
        handleMessageBody(msgs)
    }

    private fun hookSendMsgV2(params: XC_MethodHook.MethodHookParam) {
        val msgs = params.args[2] as ArrayList<MsgElement>
        handleMessageBody(msgs)
    }

    override fun canRun(): Boolean {
        val tail by QwQSetting.getSetting<String>(name)
        val encrypt by QwQSetting.getSetting<String>(QwQSetting.MESSAGE_ENCRYPT)
        return tail.isNotEmpty() || encrypt.isNotEmpty()
    }

    override val name: String = QwQSetting.MESSAGE_TAIL

    override val process: ActionProcess
        get() = ActionProcess.MAIN
}


private fun ArrayList<MsgElement>.isActionMsg(): Boolean {
    return any {
        it.elementType == MsgConstant.KELEMTYPEACTIVITY ||
                it.elementType == MsgConstant.KELEMTYPEFEED ||
                it.elementType == MsgConstant.KELEMTYPEAVRECORD ||
                it.elementType == MsgConstant.KELEMTYPECALENDAR ||
                it.elementType == MsgConstant.KELEMTYPEGIPHY ||
                it.elementType == MsgConstant.KELEMTYPEGRAYTIP ||
                it.elementType == MsgConstant.KELEMTYPEINTEXTGIFT ||
                it.elementType == MsgConstant.KELEMTYPELIVEGIFT ||
                it.elementType == MsgConstant.KELEMTYPEYOLOGAMERESULT ||
                it.elementType == MsgConstant.KELEMTYPEWALLET ||
                it.elementType == MsgConstant.KELEMTYPEUNKNOWN ||
                it.elementType == MsgConstant.KELEMTYPETOFU
    }
}