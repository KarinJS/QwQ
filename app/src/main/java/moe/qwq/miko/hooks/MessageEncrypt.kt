package moe.qwq.miko.hooks

import android.content.Context
import com.google.protobuf.ByteString
import com.google.protobuf.UnknownFieldSet
import com.tencent.mobileqq.fe.FEKit
import de.robv.android.xposed.XC_MethodHook
import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.readBytes
import kotlinx.io.core.writeFully
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import moe.fuqiuluo.entries.QQSsoSecureInfo
import moe.fuqiuluo.entries.TextMsgExtPbResvAttr
import moe.fuqiuluo.processor.HookAction
import moe.qwq.miko.actions.ActionProcess
import moe.qwq.miko.actions.HookCodec
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.ext.getUnknownObject
import moe.qwq.miko.ext.getUnknownObjects
import moe.qwq.miko.internals.QQInterfaces
import moe.qwq.miko.internals.hijackers.IHijacker
import moe.qwq.miko.internals.setting.QwQSetting
import moe.qwq.miko.utils.AesUtils.aesEncrypt
import moe.qwq.miko.utils.AesUtils.md5

@HookAction(desc = "消息加密抄送")
class MessageEncrypt: IAction, QQInterfaces() {
    override fun onRun(ctx: Context) {
        HookCodec.hijackers.add(object: IHijacker {
            override fun onHandle(
                param: XC_MethodHook.MethodHookParam,
                uin: String,
                cmd: String,
                seq: Int,
                buffer: ByteArray,
                bufferIndex: Int
            ): Boolean {
                this@MessageEncrypt.onHandle(param, uin, cmd, seq, buffer, bufferIndex)
                return false
            }
            override val command: String = "MessageSvc.PbSendMsg"
        })
    }

    private fun onHandle(param: XC_MethodHook.MethodHookParam, uin: String, cmd: String, seq: Int, buffer: ByteArray, bufferIndex: Int) {
        if (buffer.size <= 4) return
        val unknownFields = UnknownFieldSet.parseFrom(buffer.copyOfRange(4, buffer.size))
        if (!unknownFields.hasField(1)) return
        val routingHead  = unknownFields.getUnknownObject(1)
        if (routingHead.hasField(1)) return // 私聊消息不加密
        val msgBody = unknownFields.getUnknownObject(3)

        val builder = UnknownFieldSet.newBuilder(unknownFields)
        builder.clearField(3) // 清除原消息体

        val newMsgBody = generateEncryptedMsgBody(msgBody)
        builder.addField(3, UnknownFieldSet.Field.newBuilder().also {
            it.addLengthDelimited(newMsgBody.toByteString())
        }.build())

        val data = builder.build().toByteArray()

        if (bufferIndex == 15 && param.args[13] != null) {
            //PlatformTools.copyToClipboard(text = "Sign13: ${(param.args[13] as ByteArray).toHexString()}")
            // 因为包体改变，重新签名
            val qqSecurityHead = UnknownFieldSet.parseFrom(param.args[13] as ByteArray)
            val qqSecurityHeadBuilder = UnknownFieldSet.newBuilder(qqSecurityHead)
            //XposedBridge.log(qqSecurityHead.getField(24).toInnerValuesString())
            qqSecurityHeadBuilder.clearField(24)
            val sign = FEKit.getInstance().getSign(cmd, data, seq, uin)
            //XposedBridge.log(sign.toInnerValuesString())
            qqSecurityHeadBuilder.addField(24, UnknownFieldSet.Field.newBuilder().also {
                it.addLengthDelimited(ByteString.copyFrom(ProtoBuf.encodeToByteArray(QQSsoSecureInfo(
                    secSig = sign.sign,
                    extra = sign.extra,
                    deviceToken = sign.token
                ))))
            }.build())
            param.args[13] = qqSecurityHeadBuilder.build().toByteArray()
        }

        if (bufferIndex == 15 && param.args[14] != null) {
            //PlatformTools.copyToClipboard(text = "Sign14: ${(param.args[14] as ByteArray).toHexString()}")
            val qqSecurityHead = UnknownFieldSet.parseFrom(param.args[14] as ByteArray)
            val qqSecurityHeadBuilder = UnknownFieldSet.newBuilder(qqSecurityHead)
            qqSecurityHeadBuilder.clearField(24)
            val sign = FEKit.getInstance().getSign(cmd, data, seq, uin)
            qqSecurityHeadBuilder.addField(24, UnknownFieldSet.Field.newBuilder().also {
                it.addLengthDelimited(ByteString.copyFrom(ProtoBuf.encodeToByteArray(QQSsoSecureInfo(
                    secSig = sign.sign,
                    extra = sign.extra,
                    deviceToken = sign.token
                ))))
            }.build())
            param.args[14] = qqSecurityHeadBuilder.build().toByteArray()
        }

        //PlatformTools.copyToClipboard(text = "SendData: ${(data).toHexString()}")
        param.args[bufferIndex] = BytePacketBuilder().also {
            it.writeInt(data.size + 4)
            it.writeFully(data)
        }.build().readBytes()
        //sendBuffer(cmd, true, data)
        //param.result = EMPTY_BYTE_ARRAY
    }

    private fun generateEncryptedMsgBody(msgBody: UnknownFieldSet): UnknownFieldSet {
        val encryptKey = QwQSetting.getSetting<String>(name).getValue(null, null)
        if (encryptKey.isBlank()) {
            // 未设置加密密钥
            return msgBody
        }

        val elements = UnknownFieldSet.Field.newBuilder()
        msgBody.getUnknownObject(1).let { richText ->
            richText.getUnknownObjects(2).forEach { element ->
                if (element.hasField(37) || element.hasField(9)) {
                    elements.addLengthDelimited(element.toByteString()) // 通用字段，不自己合成
                }
            }
        }

        val newMsgBody = UnknownFieldSet.newBuilder()
        val richText = UnknownFieldSet.newBuilder()

        elements.addLengthDelimited(DEFAULT_FACE) // add image
        elements.addLengthDelimited(UnknownFieldSet.newBuilder().also { builder ->
            builder.addField(1, UnknownFieldSet.Field.newBuilder().also {
                it.addLengthDelimited(UnknownFieldSet.newBuilder().also { textElement ->
                    textElement.addField(1, UnknownFieldSet.Field.newBuilder().also { content ->
                        content.addLengthDelimited(ByteString.copyFromUtf8("[爱你]"))
                    }.build())

                    textElement.addField(12, UnknownFieldSet.Field.newBuilder().also { content ->
                        content.addLengthDelimited(ByteString.copyFrom(ProtoBuf.encodeToByteArray(TextMsgExtPbResvAttr(
                            wording = BytePacketBuilder().also {
                                it.writeInt(0x114514)
                                it.writeInt(encryptKey.hashCode())
                                it.writeFully(aesEncrypt(msgBody.toByteArray(), md5(encryptKey)))
                            }.build().readBytes()
                        ))))
                    }.build())
                }.build().toByteString())
            }.build())
        }.build().toByteString()) // add text

        richText.addField(2, elements.build())

        newMsgBody.addField(1, UnknownFieldSet.Field.newBuilder().also {
            it.addLengthDelimited(richText.build().toByteString())
        }.build())

        return newMsgBody.build()
    }

    override fun canRun(): Boolean {
        val setting = QwQSetting.getSetting<String>(name)
        return setting.getValue(null, null).isNotBlank()
    }

    override val process: ActionProcess = ActionProcess.MSF

    override val name: String = QwQSetting.MESSAGE_ENCRYPT
}

private val DEFAULT_FACE by lazy {
    ByteString.fromHex("323d0a055b5177515d1002180122101a156dd3d4367c701aecfe157b16f7f728b5bf0e30033a1035636664613661666530633537383466480050c80158c801")
}