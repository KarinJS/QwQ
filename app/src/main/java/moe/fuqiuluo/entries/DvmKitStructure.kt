@file:Suppress("ReplaceWithEnumMap", "LocalVariableName")
package moe.fuqiuluo.entries

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.fuqiuluo.xposed.loader.LuoClassloader
import moe.qwq.miko.internals.locators.AbstractPreviewUiLocator
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import moe.qwq.miko.internals.locators.*
import java.lang.reflect.Array as JavaReflectArray

@Serializable
enum class ClassEnum(
    val locator: ClassLocator? = null
) {
    @SerialName("WebSecurityPluginV2Plugin") WebSecurityPluginV2Plugin(WebSecurityPluginV2PluginLocator),
    @SerialName("CodecWarpperImpl") CodecWarpperImpl,
    @SerialName("QQSettingMeConfig") QQSettingMeConfig(QQSettingMeConfigLocator),
    @SerialName("AbstractPreviewUi") AbstractPreviewUi(AbstractPreviewUiLocator),
    @SerialName("PreviewUserInteractionPart") PreviewUserInteractionPart(PreviewUserInteractionPartLocator)
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
    @SerialName("VoteHelper.Vote") VoteHelperVote(VoteHelperVoteLocator),
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
    fun toClass(): Class<*>? {
        if (isArray) {
            return LuoClassloader.load(fullName)?.let {
                JavaReflectArray.newInstance(it, 0).javaClass
            }
        }
        return LuoClassloader.load(fullName)
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
) {
   fun toMethod(): Method? {
       val cls = parent.toClass()
           ?: return null
       val argTypes = args.map {
           it.toClass() ?: return null
       }.toTypedArray()
       return cls.declaredMethods.firstOrNull {
           Modifier.isStatic(it.modifiers) == static &&
           Modifier.isPrivate(it.modifiers) == private &&
           it.name == methodName &&
           it.parameterTypes.contentEquals(argTypes)
       }
   }
}