package moe.fuqiuluo.entries

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
data class RecallMessage(
    @ProtoNumber(4) val peerId: Long,
    @ProtoNumber(11) val operation: RecallOperation,
)

@Serializable
data class RecallOperation(
    @ProtoNumber(1) val operatorUid: String? = null,
    @ProtoNumber(3) val msgInfo: RecallMsgInfo? = null,
    @ProtoNumber(9) val wording: RecallWording? = null
)

@Serializable
data class RecallMsgInfo(
    @ProtoNumber(1) val msgSeq: Long = Long.MIN_VALUE,
    @ProtoNumber(2) val msgTime: Long = Long.MIN_VALUE,
    @ProtoNumber(6) val senderUid: String? = null
)

@Serializable
data class RecallWording(
    @ProtoNumber(2) val wording: String? = null
)
