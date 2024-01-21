package moe.qwq.miko.internals.helper

import com.tencent.qqnt.kernel.nativeinterface.Contact
import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import kotlinx.coroutines.suspendCancellableCoroutine
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

    suspend fun generateContact(chatType: Int, id: String, subId: String = ""): Contact {
        val peerId = if (MsgConstant.KCHATTYPEC2C == chatType || MsgConstant.KCHATTYPETEMPC2CFROMGROUP == chatType) {
            if (id.startsWith("u_")) id else getUidByUinAsync(id.toLong())
        } else id
        return Contact(chatType, peerId, subId)
    }
}