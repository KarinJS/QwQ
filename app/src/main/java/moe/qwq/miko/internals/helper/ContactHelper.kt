package moe.qwq.miko.internals.helper

import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import kotlinx.coroutines.suspendCancellableCoroutine
import moe.fuqiuluo.maple.MapleContact
import moe.qwq.miko.internals.QQInterfaces
import java.lang.IllegalArgumentException
import kotlin.coroutines.resume

internal object ContactHelper {
    suspend fun getUinByUidAsync(uid: String): String {
        if (uid.isBlank() || uid == "0") {
            return "0"
        }

        val kernelService = NTServiceFetcher.kernelService
        val sessionService = kernelService.wrapperSession

        return suspendCancellableCoroutine { continuation ->
            sessionService.uixConvertService.getUin(hashSetOf(uid)) {
                continuation.resume(it)
            }
        }[uid]?.toString() ?: "0"
    }

    suspend fun getUidByUinAsync(peerId: Long): String {
        val kernelService = NTServiceFetcher.kernelService
        val sessionService = kernelService.wrapperSession
        return suspendCancellableCoroutine { continuation ->
            sessionService.uixConvertService.getUid(hashSetOf(peerId)) {
                continuation.resume(it)
            }
        }[peerId]!!
    }

    suspend fun generateContact(chatType: Int, id: String, subId: String = ""): MapleContact {
        val peerId = if (MsgConstant.KCHATTYPEC2C == chatType || MsgConstant.KCHATTYPETEMPC2CFROMGROUP == chatType) {
            if (id.startsWith("u_")) id else getUidByUinAsync(id.toLong())
        } else id

        return generateContactByUid(chatType, peerId, subId)
    }

    fun generateContactByUid(chatType: Int, uid: String, subId: String = ""): MapleContact {
        if (chatType == MsgConstant.KCHATTYPEC2C && !uid.startsWith("u_")) {
            throw IllegalArgumentException("uid must start with u_")
        }
    return MapleContact.new(QQInterfaces.maple, chatType, uid, subId)
    }
}