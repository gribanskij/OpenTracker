package com.gribansky.opentracker.core.log

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.coroutineScope


class LogManager(private val locationProvider:ILocation, private val saver:IFileSaver, private val sender:INetSender) :
    ILogManager {



    private val handler = CoroutineExceptionHandler { _, exception ->

    }

    private val packetsForSend = mutableListOf<String>()


    override suspend fun startLogCollect(
        path: String,
        events: List<String>,
        now: Boolean
    ): LogResult = coroutineScope {
        var points = emptyList<PositionData>()

        val logEntries = if (now) {
            // Если now == true, используем только события
            buildList {
                addAll(events)
            }
        } else {
            // Иначе собираем точки и объединяем с событиями
            points = locationProvider.getPoints()
            buildList {
                addAll(events)
                addAll(points.map { it.getDataInString() })
            }
        }

        // Сохраняем лог в файл
        val packets = saver.save(path, logEntries, now)
        // Отправляем пакеты
        val sentPackets = sender.send(packets, now)

        // Формируем результат
        LogResult(
            errorDesc = null,
            ready = packets.size,
            sent = sentPackets,
            points = points.ifEmpty {
                listOf(
                    PositionDataLog(
                        logTag = "GPS receiver:",
                        logMessage = "no points collected"
                    )
                )
            }
        )
    }
}