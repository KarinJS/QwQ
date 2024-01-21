package moe.qwq.miko.internals.helper

import moe.qwq.miko.tools.PlatformTools
import mqq.app.AppRuntime
import mqq.app.MobileQQ

internal object AppRuntimeFetcher {
    val appRuntime: AppRuntime
        get() = if (PlatformTools.isMqqPackage())
            MobileQQ.getMobileQQ().waitAppRuntime()
        else
            MobileQQ.getMobileQQ().waitAppRuntime(null)
}