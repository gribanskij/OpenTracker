package com.gribansky.opentracker.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.gribansky.opentracker.data.PreferencesKeys
import kotlinx.coroutines.flow.first

import kotlinx.coroutines.runBlocking
import java.util.Calendar

private const val FIRST_START_TIME_INTERVAL =  1000 //ms
private const val START_WORK_HOUR = 8
private const val END_WORK_HOUR = 20

class TimeManager (private val dataStore: DataStore<Preferences>) {

     fun getNextTimePoint ():Long{
        return if (isInWrkTimeNow()) getFirstStartTimePoint()
        else getNextWorkDayTimePointStart()

    }

    private fun getFirstStartTimePoint():Long{
        return System.currentTimeMillis() + FIRST_START_TIME_INTERVAL
    }

    fun isInWrkTimeNow(): Boolean {

        return runBlocking {
            val useWorkTime = dataStore.data.first()[PreferencesKeys.USE_WORK_TIME] ?: true
            if (!useWorkTime) return@runBlocking true
            val calendar = Calendar.getInstance()
            return@runBlocking isWrkDay(calendar) && isWrkTime(calendar)

        }
    }

    private fun isWrkTime(calendar: Calendar): Boolean {
        val hour = calendar[Calendar.HOUR_OF_DAY]
        return hour in START_WORK_HOUR until END_WORK_HOUR

    }

    private fun isWrkDay(cal: Calendar): Boolean {
        val dayOfWeek = cal[Calendar.DAY_OF_WEEK]
        val month = cal[Calendar.MONTH]
        val day = cal[Calendar.DAY_OF_MONTH]
        val year = cal[Calendar.YEAR]

        var isWorkDay = !(dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY)


        // исключения
        if (isWorkDay) {
            // Праздничные дни (2025)
            when {
                month == Calendar.JANUARY && year == 2025 && (day <= 3 || day in listOf(6,7,8)) -> isWorkDay = false
                month == Calendar.MAY && year == 2025 && (day in listOf(1,2,8,9)) -> isWorkDay = false
                month == Calendar.JUNE && year == 2025 && (day in listOf(12,13)) -> isWorkDay = false
                month == Calendar.NOVEMBER && year == 2025 && (day in listOf(3,4)) -> isWorkDay = false
                month == Calendar.DECEMBER && year == 2025 && day == 31 -> isWorkDay = false
            }

        } else {
            // выходные - рабочие
            if ((month == Calendar.NOVEMBER && year == 2025) && (day == 1)) isWorkDay = true
        }

        return isWorkDay
    }

    private fun getNextWorkDayTimePointStart():Long{

        val calendar = Calendar.getInstance()

        // Если сейчас после начала рабочего времени, переходим на следующий день
        if (calendar[Calendar.HOUR_OF_DAY] >= START_WORK_HOUR) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            // Обнуляем время для следующего дня
            resetToStartOfWorkHours(calendar)
        } else {
            // Если сейчас до начала рабочего времени, устанавливаем на сегодня в START_WORK_HOUR
            resetToStartOfWorkHours(calendar)
        }

        // Ищем следующий рабочий день
        while (!isWrkDay(calendar)) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            resetToStartOfWorkHours(calendar)
        }

        return calendar.timeInMillis
    }

    private fun resetToStartOfWorkHours(cal: Calendar) {
        cal.set(Calendar.HOUR_OF_DAY, START_WORK_HOUR)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
    }

}