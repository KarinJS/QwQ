package moe.qwq.miko.ext

import android.content.Context
import android.os.Handler
import android.widget.Toast
import de.robv.android.xposed.XposedBridge

internal lateinit var GlobalUi: Handler

internal fun Context.toast(msg: String, flag: Int = Toast.LENGTH_SHORT) {
    XposedBridge.log(msg)
    if (!::GlobalUi.isInitialized) {
        return
    }
    GlobalUi.post { Toast.makeText(this, msg, flag).show() }
}