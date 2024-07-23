package moe.qwq.miko.internals.hooks

import android.content.Context
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import moe.fuqiuluo.entries.ClassEnum
import moe.fuqiuluo.processor.HookAction
import moe.qwq.miko.actions.IAction
import moe.qwq.miko.ext.hookMethod
import moe.qwq.miko.internals.locators.DvmLocator
import moe.qwq.miko.internals.setting.QwQSetting
import java.lang.reflect.Modifier

@HookAction(desc = "打开群聊的闪照开关")
class AllowGroupFlashPic : IAction {
    override val name: String = QwQSetting.ALLOW_GROUP_FLASH_PIC

    override fun onRun(ctx: Context) {
        //val QAlbumPickerContext = QAlbumPickerContext::class.java
        val PreviewUserInteractionPart = DvmLocator.findClass(ClassEnum.PreviewUserInteractionPart)
            ?: throw RuntimeException("PreviewUserInteractionPart not found")
        val viewFieldList = PreviewUserInteractionPart.declaredFields.filter {
            Modifier.isPrivate(it.modifiers) && it.type == CheckBox::class.java || it.type == TextView::class.java
        }

        PreviewUserInteractionPart.hookMethod("onInitView").after {
            viewFieldList.forEach { field ->
                if (!field.isAccessible) {
                    field.isAccessible = true
                }
                val view = field.get(it.thisObject) as View
                if (view.visibility == View.GONE) {
                    view.visibility = View.VISIBLE
                }
            }
        }

        /*
        XposedBridge.hookMethod(QAlbumPickerContext.declaredMethods.first {
            it.returnType == Void.TYPE && it.parameterCount > 2 && it.parameterTypes[0] == LuoClassloader.load("androidx.fragment.app.FragmentActivity")!!
        }, afterHook {

        })
        val propertiesField = QAlbumPickerContext.declaredFields.first {
            Modifier.isStatic(it.modifiers) && it.type.isArray && it.type.componentType == KProperty::class.java
        }
        val properties = propertiesField.get(null) as Array<KProperty<*>>
        val flashKProperty = properties.first {
            it.name.contains("flash", ignoreCase = true)
        }*/
    }
}