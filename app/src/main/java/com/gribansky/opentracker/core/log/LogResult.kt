package com.gribansky.opentracker.core.log

data class LogResult(
    val sendError:String? = null,
    val saveError:String? = null,
    val sent:Int? = null,
    val ready:Int? = null,
    val time:Long = System.currentTimeMillis(),
    val collectedPoints:List<PositionData> = emptyList()
)