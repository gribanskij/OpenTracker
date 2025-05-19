package com.gribansky.opentracker.core.log

import android.Manifest
import androidx.annotation.RequiresPermission
import com.gribansky.opentracker.core.LocationManager
import com.gribansky.opentracker.core.PositionData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


class LogManager(dispatcher: CoroutineDispatcher = Dispatchers.Main, private val saver:FileSaver, private val sender:NetSender) {

    private val scope = CoroutineScope(dispatcher)

    private var result:((LogResult)->Unit)? = null

    private val handler = CoroutineExceptionHandler { _, exception ->

        result?.invoke(LogResult(
            errorDesc = exception.localizedMessage?:exception.message

        ))

    }

    private val commands = MutableSharedFlow<Pair<String,List<String>>>(
        extraBufferCapacity = 5,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )

    init {
        scope.launch(handler) {
            commands.collect { log ->
                val packets = saver.save(log.first,log.second)
                val sentPackets = sender.send(packets)

                result?.invoke(
                    LogResult(
                        errorDesc = null,
                        ready = packets.size,
                        sent = sentPackets
                    )
                )
            }
        }
    }


    fun saveToLog(path:String,log: List<String>,callback:(LogResult)->Unit) {
        result = callback
        commands.tryEmit(Pair(path,log))
    }


    fun stop() {
        scope.cancel()
    }



}