package moe.qwq.miko.internals.receiver

/**
 * 从哪个地方接收广播
 */
enum class ReceiveScope {
    MSF, MAIN
}

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class BroadcastReceiver(
    val scope: ReceiveScope
)

