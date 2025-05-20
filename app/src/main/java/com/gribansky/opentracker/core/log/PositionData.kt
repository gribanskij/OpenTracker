package com.gribansky.opentracker.core.log

import android.location.Location
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val GPS_DATA_TYPE = 1
private const val GSM_DATA_TYPE = 2
private const val LOG_DATA_TYPE = 3

sealed interface PositionData {
    val eventDate: Long
    val sdf: SimpleDateFormat
        get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ", Locale.getDefault())

    fun getDataInString(): String
}

data class PositionGpsData(
    override val eventDate: Long = System.currentTimeMillis(),
    val gpsLocation: Location
) : PositionData {
    override fun getDataInString(): String {
        return formatData(
            GPS_DATA_TYPE,
            sdf.format(Date(gpsLocation.time)),
            gpsLocation.latitude,
            gpsLocation.longitude,
            gpsLocation.speed,
            gpsLocation.altitude,
            gpsLocation.bearing,
            sdf.format(Date(eventDate)),
            gpsLocation.accuracy
        )
    }
}

data class PositionGsmData(
    override val eventDate: Long = System.currentTimeMillis(),
    val cellTower: CellTowerInfo
) : PositionData {
    override fun getDataInString(): String {
        // Реализуйте логику для GSM данных, если необходимо.
        TODO("Not yet implemented")
    }
}

data class PositionDataLog(
    override val eventDate: Long = System.currentTimeMillis(),
    val logTag: String,
    val logMessage: String
) : PositionData {
    override fun getDataInString(): String {
        return formatData(
            LOG_DATA_TYPE,
            sdf.format(Date(eventDate)),
            logTag,
            logMessage
        )
    }
}

private fun formatData(vararg data: Any): String {
    return data.joinToString(",") + "\n"
}