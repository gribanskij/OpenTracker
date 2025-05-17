package com.gribansky.opentracker.core

import android.text.format.DateFormat
import com.gribansky.opentracker.BuildConfig
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.util.Date
import kotlin.math.abs


private const val DATA_FILE_EXT_WRK = "wrk"
private const val DATA_FILE_EXT_TXT = "txt"
private const val DATA_FILE_PREFIX = "opentrk"
private const val DATA_IMEI_CODE = "123"
private const val DATA_FORMAT_VERSION = 1
private const val OPEN_TRACKER_TYPE = 1
private const val INIT_TIME_TO_CHANGE_FILE = 5 * 60 * 1000 //5 min
private const val TIME_INTERVAL_TO_CHANGE_FILE = 60 * 60 * 1000 //60 min

class TrackerManager(private val dirPath: String) {

    private var currentLogFile: File? = null

    private val scope = MainScope()

    private var nextSentTime: Long = System.currentTimeMillis() + INIT_TIME_TO_CHANGE_FILE

    private val handler = CoroutineExceptionHandler { _, exception ->
        println("CoroutineExceptionHandler got $exception")
    }

    private val commands = MutableSharedFlow<List<String>>(
        extraBufferCapacity = 2,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )

    init {
        scope.launch(handler) {
            commands.collect { log ->
                handleLog(log)
            }
        }
    }


    fun saveToLog(log: List<String>) {
        commands.tryEmit(log)
    }

    private suspend fun handleLog(log: List<String>) = coroutineScope {
        if (currentLogFile == null) currentLogFile = makeNewLogFile()
        saveToFile(log)
        if (System.currentTimeMillis() > nextSentTime || abs(System.currentTimeMillis() - nextSentTime) > TIME_INTERVAL_TO_CHANGE_FILE) {
            renameLogFile()
            nextSentTime = System.currentTimeMillis() + TIME_INTERVAL_TO_CHANGE_FILE
        }
    }


    private suspend fun saveToFile(log: List<String>) =
        withContext(NonCancellable + Dispatchers.IO) {

            currentLogFile?.let { f ->
                FileWriter(f, true).use { w ->
                    log.forEach {
                        w.write(it)
                    }
                    w.flush()
                }
            }
        }

    private suspend fun makeNewLogFile(): File = coroutineScope {
        val timePoint = DateFormat.format("yyyy-MM-dd kk-mm-ss", Date(System.currentTimeMillis()))
        val dataFileName = "$DATA_FILE_PREFIX$DATA_IMEI_CODE $timePoint.$DATA_FILE_EXT_WRK"
        val dataFile = File(dirPath, dataFileName)
        addHeader(dataFile)
        return@coroutineScope dataFile
    }

    private suspend fun addHeader(file: File) = withContext(NonCancellable + Dispatchers.IO) {
        val header = getHeader()
        FileWriter(file).use {
            it.write(header)
            it.flush()
        }
    }

    private fun getHeader(): String {
        return StringBuilder().apply {
            append(DATA_FORMAT_VERSION)
            append(",")
            append(OPEN_TRACKER_TYPE)
            append(",")
            append(BuildConfig.VERSION_CODE)
            append(",")
            append(DATA_IMEI_CODE)
            append("\n")
        }.toString()
    }

    private suspend fun renameLogFile() = withContext(NonCancellable + Dispatchers.IO) {
        currentLogFile?.let { f ->
            val fileNameForSend = f.name.replaceFirst(DATA_FILE_EXT_WRK, DATA_FILE_EXT_TXT)
            f.renameTo(File(dirPath,fileNameForSend))
        }
        currentLogFile = null

    }

    fun stop() {
        scope.cancel()
    }

}