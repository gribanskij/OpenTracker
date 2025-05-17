package com.gribansky.opentracker.core.logManager

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch




class TrackerManager(dispatcher: CoroutineDispatcher = Dispatchers.Main, private val saver:FileSaver) {

    private val scope = CoroutineScope(dispatcher)

    private val handler = CoroutineExceptionHandler { _, exception ->
        println("CoroutineExceptionHandler got $exception")
    }

    private val commands = MutableSharedFlow<Pair<String,List<String>>>(
        extraBufferCapacity = 2,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )

    init {
        scope.launch(handler) {
            commands.collect { log ->
                saver.save(log.first,log.second)
            }
        }
    }


    fun saveToLog(path:String,log: List<String>) {
        commands.tryEmit(Pair(path,log))
    }


    fun stop() {
        scope.cancel()
    }

}