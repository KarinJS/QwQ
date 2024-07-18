package moe.fuqiuluo.processor

// 编译期注解
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class HookAction(
    val desc: String
)
