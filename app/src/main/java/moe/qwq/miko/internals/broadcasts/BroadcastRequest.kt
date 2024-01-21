package moe.qwq.miko.internals.broadcasts

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.DelicateCoroutinesApi

fun interface ICallback {
    suspend fun handle(intent: Intent)
}

data class BroadcastRequest(
    val cmd: String = "",
    val seq: Int = -1,
    val values: ContentValues? = null,
    val callback: ICallback? = null,
) {
    override fun hashCode(): Int {
        return (cmd + seq).hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BroadcastRequest

        if (cmd != other.cmd) return false
        if (seq != other.seq) return false
        if (values != other.values) return false
        if (callback != other.callback) return false

        return true
    }
}
