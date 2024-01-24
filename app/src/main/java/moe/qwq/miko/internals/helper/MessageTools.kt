package moe.qwq.miko.internals.helper

import kotlin.math.abs
import kotlin.random.Random

object MessageTools {
    fun generateMsgUniseq(chatType: Int): Long {
        val uniseq = (System.currentTimeMillis() / 1000) shl (8 * 4)
        val random = abs(Random.nextInt()).toLong() and 0xffffff00L
        return uniseq or random or chatType.toLong()
    }
}