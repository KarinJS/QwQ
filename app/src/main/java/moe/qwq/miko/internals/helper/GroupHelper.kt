@file:OptIn(DelicateCoroutinesApi::class)

package moe.qwq.miko.internals.helper

import com.tencent.common.app.AppInterface
import com.tencent.mobileqq.app.BusinessHandlerFactory
import com.tencent.mobileqq.data.troop.TroopMemberInfo
import com.tencent.mobileqq.troop.api.ITroopMemberInfoService
import com.tencent.qqnt.kernel.nativeinterface.MemberInfo
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import moe.qwq.miko.internals.helper.AppRuntimeFetcher.appRuntime
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.coroutines.resume

internal object GroupHelper {
    private val RefreshTroopMemberInfoLock by lazy {
        Mutex()
    }

    private lateinit var METHOD_REQ_MEMBER_INFO: Method
    private lateinit var METHOD_REQ_MEMBER_INFO_V2: Method

    suspend fun getTroopMemberInfoByUinViaNt(groupId: String, qq: Long): Result<MemberInfo> {
        val kernelService = NTServiceFetcher.kernelService
        val sessionService = kernelService.wrapperSession
        val groupService = sessionService.groupService
        val info = suspendCancellableCoroutine {
            groupService.getTransferableMemberInfo(groupId.toLong()) { code, _, data ->
                if (code != 0) {
                    it.resume(null)
                    return@getTransferableMemberInfo
                }
                data.forEach { (_, info) ->
                    if (info.uin == qq) {
                        it.resume(info)
                        return@forEach
                    }
                }
                it.resume(null)
            }
        }
        return if (info != null) {
            Result.success(info)
        } else {
            Result.failure(Exception("获取群成员信息失败"))
        }
    }

    suspend fun getTroopMemberInfoByUin(
        groupId: String,
        uin: String,
        refresh: Boolean = false
    ): Result<TroopMemberInfo> {
        val service = appRuntime.getRuntimeService(ITroopMemberInfoService::class.java, "all")
        var info = service.getTroopMember(groupId, uin)
        if (refresh || !service.isMemberInCache(groupId, uin) || info == null || info.troopnick == null) {
            info = requestTroopMemberInfo(service, groupId.toLong(), uin.toLong()).getOrNull()
        }
        if (info == null) {
            info = getTroopMemberInfoByUinViaNt(groupId, uin.toLong()).getOrNull()?.let {
                TroopMemberInfo().apply {
                    troopnick = it.cardName
                    friendnick = it.nick
                }
            }
        }
        return if (info != null) {
            Result.success(info)
        } else {
            Result.failure(Exception("获取群成员信息失败"))
        }
    }

    suspend fun getTroopMemberInfoByUid(groupId: String, uid: String): Result<MemberInfo> {
        val kernelService = NTServiceFetcher.kernelService
        val sessionService = kernelService.wrapperSession
        val groupService = sessionService.groupService
        val info = withTimeoutOrNull(5000) {
            suspendCancellableCoroutine {
                groupService.getTransferableMemberInfo(groupId.toLong()) { code, _, data ->
                    if (code != 0) {
                        it.resume(null)
                        return@getTransferableMemberInfo
                    }
                    data.forEach { (tmpUid, info) ->
                        if (tmpUid == uid) {
                            it.resume(info)
                            return@forEach
                        }
                    }
                }
            }
        }
        return if (info != null) {
            Result.success(info)
        } else {
            Result.failure(Exception("获取群成员信息失败"))
        }
    }

    private suspend fun requestTroopMemberInfo(service: ITroopMemberInfoService, groupId: Long, memberUin: Long): Result<TroopMemberInfo> {
        val info = RefreshTroopMemberInfoLock.withLock {
            val groupIdStr = groupId.toString()
            val memberUinStr = memberUin.toString()

            service.deleteTroopMember(groupIdStr, memberUinStr)

            requestMemberInfoV2(groupId, memberUin)
            requestMemberInfo(groupId, memberUin)

            withTimeoutOrNull(10000) {
                while (!service.isMemberInCache(groupIdStr, memberUinStr)) {
                    delay(200)
                }
                return@withTimeoutOrNull service.getTroopMember(groupIdStr, memberUinStr)
            }
        }
        return if (info != null) {
            Result.success(info)
        } else {
            Result.failure(Exception("获取群成员信息失败"))
        }
    }
    private fun requestMemberInfo(groupId: Long, memberUin: Long) {
        val app = AppRuntimeFetcher.appRuntime
        if (app !is AppInterface)
            throw RuntimeException("AppRuntime cannot cast to AppInterface")
        val businessHandler = app.getBusinessHandler(BusinessHandlerFactory.TROOP_MEMBER_CARD_HANDLER)

        if (!::METHOD_REQ_MEMBER_INFO.isInitialized) {
            METHOD_REQ_MEMBER_INFO = businessHandler.javaClass.declaredMethods.first {
                it.parameterCount == 2 &&
                        it.parameterTypes[0] == Long::class.java &&
                        it.parameterTypes[1] == Long::class.java &&
                        !Modifier.isPrivate(it.modifiers)
            }
        }

        METHOD_REQ_MEMBER_INFO.invoke(businessHandler, groupId, memberUin)
    }

    private fun requestMemberInfoV2(groupId: Long, memberUin: Long) {
        val app = AppRuntimeFetcher.appRuntime
        if (app !is AppInterface)
            throw RuntimeException("AppRuntime cannot cast to AppInterface")
        val businessHandler = app.getBusinessHandler(BusinessHandlerFactory.TROOP_MEMBER_CARD_HANDLER)

        if (!::METHOD_REQ_MEMBER_INFO_V2.isInitialized) {
            METHOD_REQ_MEMBER_INFO_V2 = businessHandler.javaClass.declaredMethods.first {
                it.parameterCount == 3 &&
                        it.parameterTypes[0] == String::class.java &&
                        it.parameterTypes[1] == String::class.java &&
                        !Modifier.isPrivate(it.modifiers)
            }
        }

        METHOD_REQ_MEMBER_INFO_V2.invoke(businessHandler, groupId.toString(), groupUin2GroupCode(groupId).toString(), arrayListOf(memberUin.toString()))
    }

    fun groupUin2GroupCode(groupuin: Long): Long {
        var calc = groupuin / 1000000L
        while (true) {
            calc -= if (calc >= 0 + 202 && calc + 202 <= 10) {
                (202 - 0).toLong()
            } else if (calc >= 11 + 480 && calc <= 19 + 480) {
                (480 - 11).toLong()
            } else if (calc >= 20 + 2100 && calc <= 66 + 2100) {
                (2100 - 20).toLong()
            } else if (calc >= 67 + 2010 && calc <= 156 + 2010) {
                (2010 - 67).toLong()
            } else if (calc >= 157 + 2147 && calc <= 209 + 2147) {
                (2147 - 157).toLong()
            } else if (calc >= 210 + 4100 && calc <= 309 + 4100) {
                (4100 - 210).toLong()
            } else if (calc >= 310 + 3800 && calc <= 499 + 3800) {
                (3800 - 310).toLong()
            } else {
                break
            }
        }
        return calc * 1000000L + groupuin % 1000000L
    }
}