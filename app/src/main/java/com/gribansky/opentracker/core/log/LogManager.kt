package com.gribansky.opentracker.core.log

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.coroutineScope


class LogManager(private val locationProvider:ILocation, private val saver:FileSaver, private val sender:NetSender) {



    private val handler = CoroutineExceptionHandler { _, exception ->

    }

    private val packetsForSend = mutableListOf<String>()


    suspend fun startLogCollect(path:String, events: List<String>):LogResult = coroutineScope  {

        val log = mutableListOf<String>()
        val points = locationProvider.getPoints()
        log.addAll(events)
        log.addAll(points.map { it.getDataInString() })
        val packets = saver.save(path,log)
        val sentPackets = sender.send(packets)

        return@coroutineScope LogResult(
            errorDesc = null,
            ready = packets.size,
            sent = sentPackets,
            points = points.ifEmpty {
            listOf(
                PositionDataLog(
                    logTag = "GPS reciver:",
                    logMessage = "no points collected"
                )
            )
        }
        )
    }
}