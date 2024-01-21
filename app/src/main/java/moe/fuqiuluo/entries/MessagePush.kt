@file:OptIn(ExperimentalSerializationApi::class)
package moe.fuqiuluo.entries

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import moe.qwq.miko.ext.EMPTY_BYTE_ARRAY

@Serializable
data class MessagePush(
    @ProtoNumber(1) var msgBody: MessageRecord
)

@Serializable
data class MessageRecord(
    @ProtoNumber(1) var msgHead: MessageHead,
    @ProtoNumber(2) var content: MessageContentInfo,
    @ProtoNumber(3) var body: MessageBody
)

@Serializable
data class MessageHead(
    @ProtoNumber(1) val peerId: Long = Long.MIN_VALUE,
    @ProtoNumber(2) val peerUid: String? = null,
    @ProtoNumber(5) val targetId: Long = Long.MIN_VALUE,
    @ProtoNumber(6) val targetUid: String? = null
)

@Serializable
data class MessageContentInfo(
    @ProtoNumber(1) val msgType: Int = Int.MIN_VALUE,
    @ProtoNumber(2) val msgSubType: Int = Int.MIN_VALUE,
    @ProtoNumber(3) val subType: Int = Int.MIN_VALUE,
    @ProtoNumber(5) val msgSeq: Int = Int.MIN_VALUE,
    @ProtoNumber(6) val msgTime: Long = Long.MIN_VALUE,
    @ProtoNumber(12) val msgUid: Long = Long.MIN_VALUE,
)

@Serializable
data class MessageBody(
    @ProtoNumber(2) val richMsg: ByteArray = EMPTY_BYTE_ARRAY,
)