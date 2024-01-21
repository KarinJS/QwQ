package moe.qwq.miko.internals.receiver

import android.content.Context
import android.content.Intent

interface IBroadcastReceiver {
    suspend fun onReceive(context: Context, intent: Intent)

    fun getCommand(): String
}