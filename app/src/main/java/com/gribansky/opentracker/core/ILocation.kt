package com.gribansky.opentracker.core

interface ILocation {

    suspend fun getPoints():List<PositionData>

}
