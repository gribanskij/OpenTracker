package com.gribansky.opentracker.core.log

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.abs
import kotlin.random.Random


private const val BASE_SEND_INTERVAL = 30 * 60 * 1000L // 30 min

class NetSender (private val dispatcher: CoroutineDispatcher = Dispatchers.IO) : INetSender {



    private var nextTimeToSend = System.currentTimeMillis() + getRandomInterval()


    override suspend fun send(files:List<String>, sendNow:Boolean):Int = coroutineScope{

        if (sendNow){
            val count = send(files)
            return@coroutineScope count
        }

        if (System.currentTimeMillis() > nextTimeToSend || abs(System.currentTimeMillis() - nextTimeToSend)> BASE_SEND_INTERVAL){
            nextTimeToSend = getNextTimeToSend()
            send(files)
        } else 0

    }


    private suspend fun send(files:List<String>):Int = withContext(dispatcher){

        var sentPackets = 0

        files.forEach {
            val file = File(it)
            if (file.exists()) file.delete()
            sentPackets++
            delay(1000)
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