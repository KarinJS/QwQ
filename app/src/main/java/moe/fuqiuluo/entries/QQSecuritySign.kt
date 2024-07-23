package moe.fuqiuluo.entries

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/*
message SsoSecureInfo {
  required bytes sec_sig = 1;
  required bytes device_token = 2;
  required bytes extra = 3;
}
 */
@Serializable
data class QQSsoSecureInfo(
    @ProtoNumber(1) val secSig: ByteArray,
    @ProtoNumber(2) val deviceToken: ByteArray,
    @ProtoNumber(3) val extra: ByteArray
)