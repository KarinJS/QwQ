package moe.qwq.miko.internals.hooks

import android.content.Context
import com.tencent.mobileqq.aio.msglist.AIOMsgItemFactoryProvider
import de.robv.android.xposed.XposedBridge
import moe.fuqiuluo.processor.HookAction
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.ext.afterHook
import moe.qwq.miko.internals.setting.QwQSetting.DISABLE_FLASH_PICTURE
import java.lang.reflect.Modifier

@HookAction("把闪照变成普通照片")
class DisableFlashPictures: IAction {
    override val name: String = DISABLE_FLASH_PICTURE

    override fun onRun(ctx: Context) {
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
    }
}