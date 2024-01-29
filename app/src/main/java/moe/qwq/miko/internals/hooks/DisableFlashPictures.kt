@file:Suppress("UNUSED_VARIABLE", "LocalVariableName")

package moe.qwq.miko.internals.hooks

import android.content.Context
import android.content.Intent
import android.view.View
import com.tencent.mobileqq.aio.msg.PicMsgItem
import com.tencent.mobileqq.aio.msglist.AIOMsgItemFactoryProvider
import com.tencent.mobileqq.qroute.QRoute
import com.tencent.qqnt.aio.flashpic.FlashPicActivity
import com.tencent.qqnt.aio.msg.api.IAIOMsgItemApi
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import moe.fuqiuluo.xposed.loader.LuoClassloader
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.ext.afterHook
import moe.qwq.miko.ext.beforeHook
import moe.qwq.miko.ext.hookMethod
import moe.qwq.miko.internals.setting.QwQSetting
import moe.qwq.miko.internals.setting.QwQSetting.DISABLE_FLASH_PICTURE
import java.lang.reflect.Modifier

class DisableFlashPictures: IAction {
    override fun invoke(ctx: Context) {
        if (!QwQSetting.disableFlashPicture) return

        val setting = QwQSetting.getSetting(DISABLE_FLASH_PICTURE)
        runCatching {
            //QRoute.api(IAIOMsgItemApi::class.java).javaClass.hookMethod("generateFlashPicExtBuf").before {
            //    it.args[0] = false
            //}
            //FlashPicActivity::class.java.hookMethod("onCountDownStop").before {
            //    it.result = Unit
            //}
            //val CountDownProgressBar = LuoClassloader.load("com.tencent.widget.CountDownProgressBar")!!
            //val CountDownProgressBarField = FlashPicActivity::class.java.declaredFields.first {
            //    it.type == CountDownProgressBar
            // }
            //CountDownProgressBarField.isAccessible = true
            //FlashPicActivity::class.java.hookMethod("initView").after {
            //    val progressBar = CountDownProgressBarField.get(it.thisObject) as View
            //    progressBar.visibility = View.GONE
            //    XposedHelpers.callMethod(progressBar, "setTotalMills", 10 * 60 * 1000, 600)
            //}

            //XposedBridge.hookMethod(PicMsgItem::class.java.declaredMethods.first {
            //    it.parameterCount == 0 && it.returnType == Boolean::class.java
            //}, beforeHook {
            //    it.result = false
            //})

            XposedBridge.hookMethod(AIOMsgItemFactoryProvider::class.java.declaredMethods.first {
                Modifier.isPublic(it.modifiers) && it.returnType != Void.TYPE && it.parameterCount == 1 && it.parameterTypes[0] == Integer.TYPE
            }, afterHook {
                val id = it.args[0] as Int
                if (id == 84) {
                    it.result = XposedBridge.invokeOriginalMethod(it.method, it.thisObject, arrayOf(5))
                } else if (id == 85) {
                    it.result = XposedBridge.invokeOriginalMethod(it.method, it.thisObject, arrayOf(4))
                }
            })
        }.onFailure {
            setting.isFailed = true
        }
    }
}