package com.gribansky.opentracker.core.log

interface ILocation {

    suspend fun getPoints():List<PositionData>

}
