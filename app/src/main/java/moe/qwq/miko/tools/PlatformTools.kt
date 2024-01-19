package moe.qwq.miko.tools

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageInfo
import android.os.Process
import android.provider.Settings
import mqq.app.MobileQQ
import kotlin.random.Random

internal object PlatformTools {
    fun getQQVersion(context: Context): String {
        val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return packageInfo.versionName
    }

    /**
     * 获取OIDB包的ClientVersion信息
     */
    fun getClientVersion(context: Context): String = "android ${getQQVersion(context)}"

    /**
     * 是否处于QQ MSF协议进程
     */
    fun isMsfProcess(): Boolean {
        return MobileQQ.getMobileQQ().qqProcessName.contains("msf", ignoreCase = true)
    }

    /**
     * 是否处于QQ主进程
     */
    fun isMainProcess(): Boolean {
        return isMqq() || isTim()
    }

    fun isMqq(): Boolean {
        return MobileQQ.getMobileQQ().qqProcessName == "com.tencent.mobileqq"
    }

    fun isMqqPackage(): Boolean {
        return MobileQQ.getMobileQQ().qqProcessName.startsWith("com.tencent.mobileqq")
    }

    fun isTim(): Boolean {
        return MobileQQ.getMobileQQ().qqProcessName == "com.tencent.tim"
    }

    fun killProcess(context: Context, processName: String) {
        for (processInfo in (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).runningAppProcesses) {
            if (processInfo.processName == processName) {
                Process.killProcess(processInfo.pid)
            }
        }
    }

    @SuppressLint("HardwareIds")
    fun getAndroidID(): String {
        var androidId =
            Settings.Secure.getString(MobileQQ.getContext().contentResolver, "android_id")
        if (androidId == null) {
            val sb = StringBuilder()
            for (i in 0..15) {
                sb.append(Random.nextInt(10))
            }
            androidId = sb.toString()
        }
        return androidId
    }
}