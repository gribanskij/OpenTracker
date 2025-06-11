package com.gribansky.opentracker.ui

import kotlinx.serialization.Serializable

@Serializable
data class TrackerState (

    val serviceLastStartTime:Long? = null,
    val serviceFutureEndTime: Long? = null,
    val isForeground:Boolean = false,
    val gpsLastTime:Long? = null,

    val logTime:Long? = null,
    val packetsReady:Int? = null,
    val packetsSent:Int? = null,

    val gpsLastTimeReceived:Long? = null,
    val gsmLastTimeReceived:Long? = null,
    val packetsSentLastTime: Long? = null,
    val packetsReadyLastTime: Long? = null

    )