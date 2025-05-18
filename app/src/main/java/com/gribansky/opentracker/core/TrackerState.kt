package com.gribansky.opentracker.core

import kotlinx.serialization.Serializable

@Serializable
data class TrackerState (
    val gpsLastTimeReceived:Long? = null,
    val gsmLastTimeReceived:Long? = null,
    val userName:String? = null,
    val serviceLastStartTime:Long? = null,
    val serviceFutureEndTime: Long? = null,
    val locCount:Int = 0,
    val isForeground:Boolean = false,
    val gpsLastTime:Long? = null,

    val logTime:Long? = null,
    val packetsReady:Int? = null,
    val packetsSent:Int? = null

)