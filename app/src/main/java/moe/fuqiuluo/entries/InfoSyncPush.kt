@file:OptIn(ExperimentalSerializationApi::class)

package moe.fuqiuluo.entries

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
data class InfoSyncPush(
    @ProtoNumber(3) val type: Int = Int.MIN_VALUE,
    @ProtoNumber(4) val pushId: Long = Long.MIN_VALUE,
    @ProtoNumber(8) val syncContent: InfoSyncPushContent? = null,
)

@Serializable
data class InfoSyncPushContent(
    @ProtoNumber(3) val head: SyncInfoHead? = null,
    @ProtoNumber(4) val body: ArrayList<SyncInfoBody>? = null,
    @ProtoNumber(5) val subHead: SyncInfoHead? = null,
)

@Serializable
data class SyncInfoHead(
    @ProtoNumber(1) val syncTime: Long = Long.MIN_VALUE
)

@Serializable
data class SyncInfoBody(
    @ProtoNumber(1) val peer: Long = Long.MIN_VALUE,
    @ProtoNumber(2) val peerUid: String = "",
    @ProtoNumber(5) val eventTime: Long = Long.MIN_VALUE,
    @ProtoNumber(8) val msgs: ArrayList<Message>? = null
)