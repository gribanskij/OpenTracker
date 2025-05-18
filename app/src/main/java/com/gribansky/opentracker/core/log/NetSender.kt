package com.gribansky.opentracker.core.log

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.abs
import kotlin.random.Random


private const val BASE_SEND_INTERVAL = 30 * 60 * 1000L // 30 min

class NetSender (private val dispatcher: CoroutineDispatcher = Dispatchers.IO) {



    private var nextTimeToSend = System.currentTimeMillis() + getRandomInterval()



    suspend fun send(files:List<String>):Int = withContext(dispatcher){

        var sentPackets = 0

        if (System.currentTimeMillis() > nextTimeToSend || abs(System.currentTimeMillis() - nextTimeToSend)> BASE_SEND_INTERVAL){

            files.forEach {
                val f = File(it)
                if (f.exists())f.delete()
                sentPackets ++
                delay(1000)
            }

            nextTimeToSend = getNextTimeToSend()

        }

        return@withContext sentPackets
    }



    private fun getNextTimeToSend():Long{
        return BASE_SEND_INTERVAL + getRandomInterval()+ System.currentTimeMillis()
    }


    private fun getRandomInterval():Long{
        return Random.nextInt(15,30)*60*1000L
    }


}