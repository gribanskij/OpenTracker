package com.gribansky.opentracker.core

import android.text.format.DateFormat
import com.gribansky.opentracker.BuildConfig
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.PrintWriter
import java.util.Date


private const val TIME_INTERVAL_TO_SEND = 60 //мин


private const val DATA_FILE_EXT_WRK = "wrk"
private const val DATA_FILE_PREFIX = "opentrk"
private const val DATA_IMEI_CODE = "123"
private const val DATA_FORMAT_VERSION = 1
private const val OPEN_TRACKER_TYPE = 1

class TrackerManager (private val dirPath:String) {

    private var currentLogFile:File? = null

    private val scope = MainScope()

    private val handler = CoroutineExceptionHandler { _, exception ->
        println("CoroutineExceptionHandler got $exception")
    }

    private val commands = MutableSharedFlow<List<String>>(extraBufferCapacity = 2, onBufferOverflow = BufferOverflow.DROP_LATEST)

    init {

        scope.launch (handler) {
            commands.collect{
                if (currentLogFile == null) currentLogFile= makeNewLogFile()
                saveToFile(it)

                currentLogFile?.renameTo(File(""))

        }

        }
    }



    private val _trackerState = MutableStateFlow(TrackerState())
    val trackerState: StateFlow<TrackerState> = _trackerState.asStateFlow()


    private var nextSentTime:Long = 0




    fun saveToLog(log:List<String>){
        commands.tryEmit(log)
    }


    private suspend fun saveToFile(log:List<String>) = withContext(NonCancellable + Dispatchers.IO){

        currentLogFile?.let {f->
            PrintWriter(f).use { w->
                log.forEach {
                    w.println(it)
                }
                w.flush()
            }
        }
    }

    private suspend fun makeNewLogFile():File = coroutineScope{
        val timePoint = DateFormat.format("yyyy-MM-dd kk-mm-ss", Date(System.currentTimeMillis()))
        val dataFileName = "$DATA_FILE_PREFIX$DATA_IMEI_CODE $timePoint.$DATA_FILE_EXT_WRK"
        val dataFile = File(dirPath, dataFileName)
        addHeader(dataFile)
        return@coroutineScope dataFile
    }

    private suspend fun addHeader(file:File) = withContext(Dispatchers.IO){
        val header = getHeader()
        PrintWriter(file).use {
            it.println(header)
            it.flush()
        }
    }

    private fun getHeader():String{
        return StringBuilder().apply {
            append(DATA_FORMAT_VERSION)
            append(",")
            append(OPEN_TRACKER_TYPE)
            append(",")
            append(BuildConfig.VERSION_CODE)
            append(",")
            append(DATA_IMEI_CODE)
        }.toString()
    }

    fun stop(){
        scope.cancel()
    }

}