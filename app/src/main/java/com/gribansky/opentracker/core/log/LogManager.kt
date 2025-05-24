package com.gribansky.opentracker.core.log


import kotlinx.coroutines.supervisorScope


class LogManager(private val locationProvider:ILocation, private val saver:IFileSaver, private val sender:INetSender) :
    ILogManager {



    override suspend fun startLogCollect(
        path: String,
        events: List<String>,
        now: Boolean
    ): LogResult = supervisorScope {

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
        val saveResult = saver.save(path, logEntries, now)

        // Отправляем пакеты
        val sentResult = sender.send(saveResult.getOrNull()?: emptyList(), now)


        // Формируем результат
        LogResult(
            saveError = saveResult.exceptionOrNull()?.message,
            sendError = sentResult.exceptionOrNull()?.message,
            ready = saveResult.getOrNull()?.size,
            sent = sentResult.getOrNull(),
            collectedPoints = points.ifEmpty {
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