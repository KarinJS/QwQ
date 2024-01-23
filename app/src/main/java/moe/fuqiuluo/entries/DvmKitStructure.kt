@file:Suppress("ReplaceWithEnumMap", "LocalVariableName")
package moe.fuqiuluo.entries

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.fuqiuluo.xposed.loader.LuoClassloader
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import moe.qwq.miko.internals.locators.ClassLocator
import moe.qwq.miko.internals.locators.FieldLocator
import moe.qwq.miko.internals.locators.MethodLocator
import moe.qwq.miko.internals.locators.QQSettingMeConfigLocator
import moe.qwq.miko.internals.locators.WebSecurityPluginV2PluginLocator
import java.lang.reflect.Array as JavaReflectArray

@Serializable
enum class ClassEnum(
    val locator: ClassLocator? = null
) {
    @SerialName("WebSecurityPluginV2Plugin") WebSecurityPluginV2Plugin(WebSecurityPluginV2PluginLocator),
    @SerialName("CodecWarpperImpl") CodecWarpperImpl,
    @SerialName("QQSettingMeConfig") QQSettingMeConfig(QQSettingMeConfigLocator),

}

@Serializable
enum class FieldEnum(
    val locator: FieldLocator? = null
) {
    @SerialName("QQSettingMeItem.Name") QQSettingMeItemName,

}

@Serializable
enum class MethodEnum(
    val locator: MethodLocator? = null
) {
    @Deprecated("Lower Speed")
    @SerialName("QQSettingMeConfig.GetItems") QQSettingMeConfigGetItems,
}

@Serializable
data class FuzzyDexKit(
    @SerialName("classes") val classes: HashMap<ClassEnum, ClassInfo> = hashMapOf(),
    @SerialName("fields") val fields: HashMap<FieldEnum, FieldInfo> = hashMapOf(),
    @SerialName("methods") val methods: HashMap<MethodEnum, MethodInfo> = hashMapOf(),
)

@Serializable
data class ClassInfo(
    @SerialName("full_name") val fullName: String,
    @SerialName("array") val isArray: Boolean,
) {
    val isVoid: Boolean = fullName == "void"

    fun toClass(): Class<*>? {
        if (isVoid) {
            return Void.TYPE
        }
        if (isArray) {
            return LuoClassloader.load(fullName)?.let {
                JavaReflectArray.newInstance(it, 0).javaClass
            }
        }
        return LuoClassloader.load(fullName)
    }

    companion object {
        val VOID = ClassInfo("void", false)
    }
}

@Serializable
data class FieldInfo(
    @SerialName("class") val parent: ClassInfo,
    @SerialName("name") val fieldName: String,
    @SerialName("private") val private: Boolean,
    @SerialName("static") val static: Boolean,
    @SerialName("type") val type: ClassInfo,
) {
    fun toField(): Field? {
        val cls = parent.toClass()
            ?: return null
        val fieldType = type.toClass()
            ?: return null
        return cls.declaredFields.firstOrNull {
            Modifier.isStatic(it.modifiers) == static &&
            Modifier.isPrivate(it.modifiers) == private &&
            it.name == fieldName &&
            it.type == fieldType
        }
    }
}

@Serializable
data class MethodInfo(
    @SerialName("class") val parent: ClassInfo,
    @SerialName("name") val methodName: String,
    @SerialName("private") val private: Boolean,
    @SerialName("static") val static: Boolean,
    @SerialName("args") val args: List<ClassInfo>,
    @SerialName("return_type") val returnType: ClassInfo,
) {
   fun toMethod(): Method? {
       val cls = parent.toClass()
           ?: return null
       val argTypes = args.map {
           it.toClass() ?: return null
       }.toTypedArray()
       val returnType = returnType.toClass()
           ?: return null
       return cls.declaredMethods.firstOrNull {
           Modifier.isStatic(it.modifiers) == static &&
           Modifier.isPrivate(it.modifiers) == private &&
           it.name == methodName &&
           it.parameterTypes.contentEquals(argTypes) &&
           it.returnType == returnType
       }
   }
}