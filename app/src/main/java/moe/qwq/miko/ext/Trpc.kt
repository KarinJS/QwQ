package moe.qwq.miko.ext

import com.tencent.qphone.base.remote.FromServiceMsg
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import moe.fuqiuluo.entries.TrpcOidb
import moe.qwq.miko.tools.DeflateTools
import tencent.im.oidb.oidb_sso
import kotlin.reflect.KClass

inline fun <reified T : Any> ByteArray.decodeProtobuf(to: KClass<T>? = null): T {
    return ProtoBuf.decodeFromByteArray(this)
}

fun FromServiceMsg.decodeToOidb(): oidb_sso.OIDBSSOPkg {
    return kotlin.runCatching {
        oidb_sso.OIDBSSOPkg().mergeFrom(wupBuffer.slice(4).let {
            if (it[0] == 0x78.toByte()) DeflateTools.uncompress(it) else it
        })
    }.getOrElse {
        oidb_sso.OIDBSSOPkg().mergeFrom(wupBuffer.let {
            if (it[0] == 0x78.toByte()) DeflateTools.uncompress(it) else it
        })
    }
}

fun FromServiceMsg.decodeToTrpcOidb(): TrpcOidb {
    return kotlin.runCatching {
        wupBuffer.slice(4).let {
            if (it[0] == 0x78.toByte()) DeflateTools.uncompress(it) else it
        }.decodeProtobuf<TrpcOidb>()
    }.getOrElse {
        wupBuffer.let {
            if (it[0] == 0x78.toByte()) DeflateTools.uncompress(it) else it
        }.decodeProtobuf<TrpcOidb>()
    }
}