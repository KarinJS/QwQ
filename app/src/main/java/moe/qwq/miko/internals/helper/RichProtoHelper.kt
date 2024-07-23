@file:OptIn(DelicateCoroutinesApi::class, ExperimentalStdlibApi::class)
package moe.qwq.miko.internals.helper

import com.tencent.qqnt.kernel.nativeinterface.MsgConstant
import com.tencent.qqnt.kernel.nativeinterface.PicElement
import de.robv.android.xposed.XposedBridge
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import moe.fuqiuluo.QQInterfaces
import moe.fuqiuluo.entries.*
import moe.qwq.miko.ext.decodeProtobuf
import moe.qwq.miko.ext.decodeToTrpcOidb

private const val GPRO_PIC = "gchat.qpic.cn"
private const val MULTIMEDIA_DOMAIN = "multimedia.nt.qq.com.cn"
private const val C2C_PIC = "c2cpicdw.qpic.cn"

object RichProtoHelper: QQInterfaces() {
    private lateinit var cacheRkey: DownloadRkeyRsp
    private var lastReqTime = 0L

    private suspend fun getTempNtRKey(): Result<DownloadRkeyRsp> {
        if (System.currentTimeMillis() - lastReqTime < 60_000) {
            return Result.success(cacheRkey)
        }
        runCatching {
            val req = ProtoBuf.encodeToByteArray(NtV2RichMediaReq(
                head = MultiMediaReqHead(
                    commonHead = CommonHead(
                        requestId = 1u,
                        cmd = 202u
                    ),
                    sceneInfo = SceneInfo(
                        requestType = 2u,
                        businessType = 1u,
                        sceneType = 0u,
                    ),
                    clientMeta = ClientMeta(2u)
                ),
                downloadRkey = DownloadRkeyReq(
                    types = listOf(10, 20),
                    downloadType = 2
                )
            ))
            val fromServiceMsg = sendOidbAW("OidbSvcTrpcTcp.0x9067_202", 0x9067, 202, req, true)
            if (fromServiceMsg == null || fromServiceMsg.wupBuffer == null) {
                return Result.failure(Exception("failed to fetch NtTempRKey: ${fromServiceMsg?.wupBuffer?.toHexString()}"))
            }
            val trpc = fromServiceMsg.decodeToTrpcOidb()
            if (trpc.buffer == null) {
                return Result.failure(Exception("failed to fetch NtTempRKey: ${trpc.msg}"))
            }

            trpc.buffer.decodeProtobuf<NtV2RichMediaRsp>().downloadRkeyRsp?.let {
                cacheRkey = it
                lastReqTime = System.currentTimeMillis()
                return Result.success(it)
            }
        }.onFailure {
            return Result.failure(it)
        }
        return Result.failure(Exception("failed to fetch NtTempRKey"))
    }

    suspend fun getTempPicDownloadUrl(
        chatType: Int,
        originalUrl: String,
        md5: String,
        image: PicElement,
        storeId: Int = 0,
        peer: String? = null,
        subPeer: String? = null,
    ): String {
        val isNtServer = originalUrl.startsWith("/download") || storeId == 1
        if (isNtServer) {
            val tmpRKey = getTempNtRKey().onFailure {
                XposedBridge.log(it)
            }
            if (tmpRKey.isSuccess) {
                val tmpRKeyRsp = tmpRKey.getOrThrow()
                val tmpRKeyMap = hashMapOf<UInt, String>()
                tmpRKeyRsp.rkeys?.forEach { rKeyInfo ->
                    tmpRKeyMap[rKeyInfo.type] = rKeyInfo.rkey
                }
                val rkey = tmpRKeyMap[when(chatType) {
                    MsgConstant.KCHATTYPEDISC, MsgConstant.KCHATTYPEGROUP -> 10u
                    MsgConstant.KCHATTYPEC2C -> 20u
                    MsgConstant.KCHATTYPEGUILD -> 10u
                    else -> 0u
                }]
                if (rkey != null) {
                    return "https://$MULTIMEDIA_DOMAIN$originalUrl$rkey"
                } else {
                    XposedBridge.log("RKEY获取失败")
                }
            }
        }
        return when (chatType) {
            MsgConstant.KCHATTYPEDISC, MsgConstant.KCHATTYPEGROUP -> getGroupPicDownUrl(
                originalUrl = originalUrl,
                md5 = md5,
                fileId = image.fileUuid,
                width = image.picWidth.toUInt(),
                height = image.picHeight.toUInt(),
                sha = "",
                fileSize = image.fileSize.toULong(),
                peer = peer ?: "0"
            )

            MsgConstant.KCHATTYPEC2C -> getC2CPicDownUrl(
                originalUrl = originalUrl,
                md5 = md5,
                fileId = image.fileUuid,
                width = image.picWidth.toUInt(),
                height = image.picHeight.toUInt(),
                sha = "",
                fileSize = image.fileSize.toULong(),
                peer = peer ?: "0",
                storeId = storeId
            )

            MsgConstant.KCHATTYPEGUILD -> getGuildPicDownUrl(
                originalUrl = originalUrl,
                md5 = md5,
                fileId = image.fileUuid,
                width = image.picWidth.toUInt(),
                height = image.picHeight.toUInt(),
                sha = "",
                fileSize = image.fileSize.toULong(),
                peer = peer ?: "0",
                subPeer = subPeer ?: "0"
            )

            else -> throw UnsupportedOperationException("Not supported chat type: $chatType")
        }
    }

    fun getGroupPicDownUrl(
        originalUrl: String,
        md5: String,
        peer: String = "",
        fileId: String = "",
        sha: String = "",
        fileSize: ULong = 0uL,
        width: UInt = 0u,
        height: UInt = 0u
    ): String {
        val domain = GPRO_PIC
        if (originalUrl.isNotEmpty()) {
            return "https://$domain$originalUrl"
        }
        return "https://$domain/gchatpic_new/0/0-0-${md5.uppercase()}/0?term=2"
    }

    fun getC2CPicDownUrl(
        originalUrl: String,
        md5: String,
        peer: String = "",
        fileId: String = "",
        sha: String = "",
        fileSize: ULong = 0uL,
        width: UInt = 0u,
        height: UInt = 0u,
        storeId: Int = 0
    ): String {
        val domain = C2C_PIC
        if (originalUrl.isNotEmpty()) {
            return "https://$domain$originalUrl"
        }
        return "https://$domain/offpic_new/0/0-0-${md5}/0?term=2"
    }

    fun getGuildPicDownUrl(
        originalUrl: String,
        md5: String,
        peer: String = "",
        subPeer: String = "",
        fileId: String = "",
        sha: String = "",
        fileSize: ULong = 0uL,
        width: UInt = 0u,
        height: UInt = 0u
    ): String {
        val domain = GPRO_PIC
        if (originalUrl.isNotEmpty()) {
            return "https://$domain$originalUrl"
        }
        return "https://$domain/qmeetpic/0/0-0-${md5.uppercase()}/0?term=2"
    }
}