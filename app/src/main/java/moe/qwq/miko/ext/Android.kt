package moe.qwq.miko.ext

import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.widget.Toast
import de.robv.android.xposed.XposedBridge


internal lateinit var GlobalUi: Handler

internal fun Context.toast(msg: String, flag: Int = Toast.LENGTH_SHORT) {
    if (!::GlobalUi.isInitialized) {
        XposedBridge.log(msg)
        return
    }
    GlobalUi.post { Toast.makeText(this, msg, flag).show() }
}

fun Context.getVersionName(packageName: String = getPackageName()): String {
    var versionName: String = ""
    try {
        versionName = packageManager.getPackageInfo(packageName, 0).versionName
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return versionName
}