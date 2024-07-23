package moe.qwq.miko.internals.hooks

import android.content.Context
import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import com.tencent.qqnt.kernel.nativeinterface.MsgElement
import com.tencent.qqnt.kernel.nativeinterface.MsgRecord
import com.tencent.qqnt.kernel.nativeinterface.PicElement
import com.tencent.qqnt.kernel.nativeinterface.RichMediaFilePathInfo
import com.tencent.qqnt.kernel.nativeinterface.TextElement
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.fuqiuluo.processor.HookAction
import moe.qwq.miko.actions.ActionProcess
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.ext.ifNullOrEmpty
import moe.qwq.miko.internals.helper.MessageCrypt
import moe.qwq.miko.internals.helper.NTServiceFetcher
import moe.qwq.miko.internals.helper.RichProtoHelper
import moe.qwq.miko.internals.helper.msgService
import moe.qwq.miko.internals.setting.QwQSetting
import moe.qwq.miko.tools.DownloadUtils
import moe.qwq.miko.tools.PlatformTools
import moe.qwq.miko.tools.PlatformTools.QQ_9_0_8_VER
import java.io.File
import java.io.RandomAccessFile

@HookAction("发送消息预劫持")
class MessageHook: IAction {
    companion object {
        fun tryHandleMessageDecrypt(record: MsgRecord) {
            val encrypt by QwQSetting.getSetting<String>(QwQSetting.MESSAGE_ENCRYPT)
            if (encrypt.isBlank()) return
            if (record.elements.size != 1 || record.elements.first().elementType != MsgConstant.KELEMTYPEPIC) return

            val pic = record.elements.first().picElement
            val msgService = NTServiceFetcher.kernelService.msgService!!
            val originalPath = msgService.getRichMediaFilePathForMobileQQSend(
                RichMediaFilePathInfo(2, 0, pic.md5HexStr, "", 1, 0, null, "", true)
            )!!
            if (decodeLocalMsg(originalPath, encrypt, pic, record)) return // 解密失败？文件不存在，大概率是这个图片没有被缓存在本地
/*            val md5 = (pic.md5HexStr ?: pic.fileName
                .replace("{", "")
                .replace("}", "")
                .replace("-", "").split(".")[0])
                .uppercase()
            var storeId = 0
            if (PlatformTools.getQQVersionCode() > QQ_9_0_8_VER) {
                storeId = pic.storeID
            }
            val originalUrl = pic.originImageUrl ?: ""
            val downloadUrl = RichProtoHelper.getTempPicDownloadUrl(
                record.chatType, originalUrl, md5, pic, storeId,
                peer = when(record.chatType) {
                    MsgConstant.KCHATTYPEDISC, MsgConstant.KCHATTYPEGROUP -> record.peerUin.toString()
                    MsgConstant.KCHATTYPEC2C -> record.senderUin.toString()
                    MsgConstant.KCHATTYPEGUILD -> record.channelId.ifNullOrEmpty { record.peerUin.toString() } ?: "0"
                    else -> null
                },
                subPeer = when(record.chatType) {
                    MsgConstant.KCHATTYPEDISC, MsgConstant.KCHATTYPEGROUP -> null
                    MsgConstant.KCHATTYPEC2C -> null
                    MsgConstant.KCHATTYPEGUILD -> record.guildId ?: "0"
                    else -> null
                }
            )
            val originalFileTmp = File("$originalPath.1")
            DownloadUtils.download(downloadUrl, originalFileTmp)
            *//*if(!decodeLocalMsg(originalFileTmp.absolutePath, encrypt, pic, record)) {
                XposedBridge.log("[QwQ] 解密消息失败，无法解密消息: $originalFileTmp, ${originalFileTmp.exists()}, $downloadUrl")
            } else {
                //File(originalPath).let { if (!it.exists()) originalFileTmp.renameTo(it) }
            }*/
        }

        private fun decodeLocalMsg(
            originalPath: String,
            encrypt: String,
            pic: PicElement,
            record: MsgRecord
        ): Boolean {
            if (!File(originalPath).exists()) return false
            val originalRandomFile = RandomAccessFile(originalPath, "r")
            val length = originalRandomFile.length()
            originalRandomFile.seek(length - 12)
            val dataSize = originalRandomFile.readInt()
            val hash = originalRandomFile.readInt()
            val magic = originalRandomFile.readInt()
            if (magic == 0x114514 && hash == (encrypt + record.senderUin).hashCode()) {
                val data = ByteArray(dataSize)
                originalRandomFile.seek(length - 12 - dataSize)
                originalRandomFile.read(data)
                originalRandomFile.close()
                MessageCrypt.decrypt(data, encrypt).onSuccess {
                    record.elements.clear()
                    record.elements.addAll(it)
                }.onFailure {
                    XposedBridge.log("消息解密失败: ${it.stackTraceToString()}")
                }
            }
            return true
        }
    }

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
        MessageCrypt.encrypt(msgs, PlatformTools.app.currentAccountUin, encryptKey).onFailure {
            XposedBridge.log("[QwQ] 消息加密失败: ${it.stackTraceToString()}")
        }.onSuccess {
            msgs.clear()
            msgs.add(it)
        }
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