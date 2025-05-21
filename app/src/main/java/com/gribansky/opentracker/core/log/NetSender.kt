package com.gribansky.opentracker.core.log

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.abs
import kotlin.random.Random

private const val BASE_SEND_INTERVAL = 30 * 60 * 1000L // 30 min

class NetSender(private val dispatcher: CoroutineDispatcher = Dispatchers.IO) : INetSender {

    private var nextTimeToSend = System.currentTimeMillis() + getRandomInterval()

    override suspend fun send(files: List<String>, sendNow: Boolean): Result<Int> {
        return runCatching {
            if (sendNow || shouldSendNow()) {
                send(files).also {  nextTimeToSend = getNextTimeToSend() }
            } else {
                0
            }
        }
    }

    private suspend fun send(files: List<String>): Int = withContext(dispatcher) {
        files.forEach { file ->
            File(file).takeIf { it.exists() }?.delete()
            delay(1000)
        }
        files.size
    }

    private fun shouldSendNow(): Boolean {
        val currentTime = System.currentTimeMillis()
        return currentTime > nextTimeToSend || abs(currentTime - nextTimeToSend) > BASE_SEND_INTERVAL
    }

    private fun getNextTimeToSend(): Long {
        return BASE_SEND_INTERVAL + getRandomInterval() + System.currentTimeMillis()
    }

    private fun getRandomInterval(): Long {
        return Random.nextInt(15, 30) * 60 * 1000L
    }
}
