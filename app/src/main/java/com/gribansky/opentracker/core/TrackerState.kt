package com.gribansky.opentracker.core

data class TrackerState (
    val gpsLastTimeReceived:Long? = null,
    val gsmLastTimeReceived:Long? = null,
    val userName:String? = null,
    val serviceLastStartTime:Long? = null,
    val serviceFutureEndTime: Long? = null

)