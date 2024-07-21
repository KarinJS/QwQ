package moe.qwq.miko.internals.entries

import com.tencent.qqnt.kernel.nativeinterface.LinkInfo
import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import com.tencent.qqnt.kernel.nativeinterface.MsgElement
import com.tencent.qqnt.kernel.nativeinterface.TextElement
import kotlinx.serialization.Serializable

@Serializable
class EcTextElem(
    var atChannelId: Long? = null,
    var atNtUid: String? = null,
    var atRoleColor: Int? = null,
    var atRoleId: Long? = null,
    var atRoleName: String? = null,
    var atTinyId: Long = 0,
    var atType: Int = 0,
    var atUid: Long = 0,
    var content: String? = null,
    var linkInfo: EcLinkInfo? = null,
    var needNotify: Int? = null,
    var subElementType: Int? = null
) {
    companion object {
        fun from(text: TextElement): EcTextElem {
            return EcTextElem(
                atChannelId = text.atChannelId,
                atNtUid = text.atNtUid,
                atRoleColor = text.atRoleColor,
                atRoleId = text.atRoleId,
                atRoleName = text.atRoleName,
                atTinyId = text.atTinyId,
                atType = text.atType,
                atUid = text.atUid,
                content = text.content,
                linkInfo = text.linkInfo?.let { EcLinkInfo(it.icon, it.tencentDocType, it.title) },
                needNotify = text.needNotify,
                subElementType = text.subElementType
            )
        }
    }

    fun toTextElement(): TextElement {
        return TextElement().apply {
            atChannelId = this@EcTextElem.atChannelId
            atNtUid = this@EcTextElem.atNtUid
            atRoleColor = this@EcTextElem.atRoleColor
            atRoleId = this@EcTextElem.atRoleId
            atRoleName = this@EcTextElem.atRoleName
            atTinyId = this@EcTextElem.atTinyId
            atType = this@EcTextElem.atType
            atUid = this@EcTextElem.atUid
            content = this@EcTextElem.content
            linkInfo = this@EcTextElem.linkInfo?.let { LinkInfo(it.title, it.icon, it.tencentDocType) }
            needNotify = this@EcTextElem.needNotify
            subElementType = this@EcTextElem.subElementType
        }
    }

    fun toMsgElement(): MsgElement {
        return MsgElement().apply {
            elementType = MsgConstant.KELEMTYPETEXT
            textElement = toTextElement()
        }
    }
}

@Serializable
class EcLinkInfo(
    var icon: String? = null,
    var tencentDocType: Int? = null,
    var title: String? = null,
)