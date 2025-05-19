package com.gribansky.opentracker.core.log

import android.annotation.SuppressLint
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.abs

private const val COLLECT_TIMEOUT = 1 * 60 * 1000L //1 мин
private const val TIME_DIFFERENCE = 6 * 60 * 1000L //6 мин
private const val POSITIONS_LIMIT = 2

@SuppressLint("MissingPermission")
class LocationManager(
    private val fusedManager: FusedLocationProviderClient,
) : ILocation {

    private val positionList = mutableListOf<PositionData>()

    private val handler = Handler(Looper.getMainLooper(), null)

    private val handleStop = Runnable { stopWork() }

    private val locListener = LocationListener { handleLocationPoint(it) }

    private var isFake: ((Boolean) -> Unit)? = null

    private var result: ((List<PositionData>) -> Unit)? = null


    override suspend fun getPoints() = suspendCancellableCoroutine { continuation ->

        start {
            object : (List<PositionData>) -> Unit {
                override fun invoke(p1: List<PositionData>) {
                    continuation.resume(p1)
                }
            }
            continuation.invokeOnCancellation {
                stop()
            }
        }
    }


    private fun start(callBack: (List<PositionData>) -> Unit) {
        result = callBack
        positionList.clear()

        val builder = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).apply {
            setMinUpdateDistanceMeters(0f)
            setWaitForAccurateLocation(true)
        }
        fusedManager.requestLocationUpdates(builder.build(), locListener, Looper.getMainLooper())
        handler.postDelayed(handleStop, COLLECT_TIMEOUT)
    }

    private fun stop() {
        handler.removeCallbacks(handleStop)
        fusedManager.removeLocationUpdates(locListener)
    }


    private fun handleLocationPoint(it: Location) {
        if (isLocationTooOld(it)) return
        if (isFake(it)) {
            isFake?.invoke(true)
            return
        } else isFake?.invoke(false)
        positionList.add(PositionGpsData(gpsLocation = it))
        if (positionList.size > POSITIONS_LIMIT) stopWork()
    }


    private fun stopWork() {
        stop()
        addLastKnownLocation()
        sendResult()
    }

    private fun sendResult() {
        result?.invoke(positionList)
    }


    private fun addLastKnownLocation() {
        if (positionList.isNotEmpty())return
        val request = fusedManager.lastLocation
        while (!request.isComplete) {
        }
        val loc: Location? = request.result
        if (loc != null && !isLocationTooOld(loc) && !isFake(loc)) {
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