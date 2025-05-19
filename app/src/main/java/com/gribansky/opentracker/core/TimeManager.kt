package com.gribansky.opentracker.core

import java.util.Calendar

private const val FIRST_START_TIME_INTERVAL =  1000 //ms
private const val START_WORK_HOUR = 8
private const val END_WORK_HOUR = 20
private const val IS_WORK_TIME_ONLY = false

class TimeManager {

     fun getNextTimePoint ():Long{
        return if (isInWrkTimeNow()) getFirstStartTimePoint()
        else getNextWorkDayTimePointStart()

    }

    private fun getFirstStartTimePoint():Long{
        return System.currentTimeMillis() + FIRST_START_TIME_INTERVAL
    }

    fun isInWrkTimeNow(): Boolean {
        if (!IS_WORK_TIME_ONLY) return true
        val calendar = Calendar.getInstance()
        return isWrkDay(calendar) && isWrkTime(calendar)
    }

    private fun isWrkTime(calendar: Calendar): Boolean {
        return calendar[Calendar.HOUR_OF_DAY] in START_WORK_HOUR until END_WORK_HOUR

    }

    private fun isWrkDay(cal: Calendar): Boolean {
        val dayOfWeek = cal[Calendar.DAY_OF_WEEK]
        val month = cal[Calendar.MONTH]
        val day = cal[Calendar.DAY_OF_MONTH]
        val year = cal[Calendar.YEAR]

        var isWrkDay = !(dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY)


        // исключения
        if (isWrkDay) {
            //рабочие дни - праздники
            if ((month == Calendar.JANUARY && year == 2025) && (day <= 3 || day == 6 || day == 7 || day == 8)) isWrkDay = false
            else if ((month == Calendar.MAY && year == 2025 ) && (day == 1 || day == 2 || day == 8 || day == 9 )) isWrkDay = false
            else if ((month == Calendar.JUNE && year == 2025 ) && (day == 12 || day == 13)) isWrkDay = false
            else if ((month == Calendar.NOVEMBER && year == 2025) && (day == 3 || day == 4)) isWrkDay = false
            else if ((month == Calendar.DECEMBER && year == 2025) && (day == 31)) isWrkDay = false

        } else {
            // выходные - рабочие
            if ((month == Calendar.NOVEMBER && year == 2025) && (day == 1)) isWrkDay = true
        }

        return isWrkDay
    }

    private fun getNextWorkDayTimePointStart():Long{

        val calendar = Calendar.getInstance()
        val hourNow = calendar[Calendar.HOUR_OF_DAY]

        if (hourNow > START_WORK_HOUR){
            calendar.add(Calendar.DAY_OF_MONTH,1)
            calendar.time
        }

        while (!isWrkDay(calendar)){
            calendar.add(Calendar.DAY_OF_MONTH,1)
            calendar.time
        }

        calendar[Calendar.HOUR_OF_DAY] = START_WORK_HOUR
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        return calendar.time.time

    }
}