package com.gribansky.opentracker.core

import android.Manifest
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import kotlin.math.abs


private const val COLLECT_TIMEOUT = 1*60*1000L //1 мин
private const val TIME_DIFFERENCE = 6*60*1000L //6 мин
private const val POSITIONS_LIMIT = 2

class LocationManager (
    private val fusedManager:FusedLocationProviderClient,
    private val result: ((List<PositionData>) -> Unit),
    private val isFake: ((Boolean) ->Unit)? = null,
):ILocation, LocationListener, Runnable {


    private val positionList = mutableListOf<PositionData>()
    private val handler = Handler(Looper.getMainLooper(), null)



    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun start() {
        positionList.clear()

        val builder = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,1000).apply {
            setMinUpdateDistanceMeters(0f)
            setWaitForAccurateLocation(true)
        }
        fusedManager.requestLocationUpdates(builder.build(), this, Looper.getMainLooper())
        handler.postDelayed(this, COLLECT_TIMEOUT)
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun stop() {
        handler.removeCallbacks(this)
        fusedManager.removeLocationUpdates(this)
    }


    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onLocationChanged(p0: Location) {
        if (isLocationTooOld(p0) || isFake(p0)) return
        positionList.add(PositionGpsData(gpsLocation = p0))
        if (positionList.size > POSITIONS_LIMIT)stopGps()

    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun run() {
        stopGps()
    }


    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun stopGps() {
        stop()
        if (positionList.isEmpty()) addLastKnownLocation()
        result.invoke(positionList)
    }



    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun addLastKnownLocation() {
        val request = fusedManager.lastLocation
        while (!request.isComplete) { }
        val loc: Location? = request.result
        if (loc!=null && !isLocationTooOld(loc) && !isFake(loc)){
            positionList.add(PositionGpsData(gpsLocation = loc))
        }
    }


    private fun isLocationTooOld(loc: Location): Boolean {
        val timeDif = abs(System.currentTimeMillis() - loc.time)
        return timeDif > TIME_DIFFERENCE
    }

    private fun isFake(location: Location): Boolean {
        val fake = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            location.isMock
        } else {
            location.isFromMockProvider
        }
        isFake?.invoke(fake)
        return fake
    }
}