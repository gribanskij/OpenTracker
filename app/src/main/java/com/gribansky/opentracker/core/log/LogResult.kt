package com.gribansky.opentracker.core.log

data class LogResult(
    val errorDesc:String? = null,
    val sent:Int? = null,
    val ready:Int? = null,
    val time:Long = System.currentTimeMillis(),
    val points:List<PositionData> = emptyList()
)