package com.gribansky.opentracker.core.log

import android.text.format.DateFormat
import com.gribansky.opentracker.BuildConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
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
private const val TIME_INTERVAL_TO_CHANGE_FILE = 5 * 60 * 1000

class FileSaver(private val dispatcher: CoroutineDispatcher = Dispatchers.IO) {


    private var currentLogFile: File? = null
    private var nextRenameTime: Long = System.currentTimeMillis() + INIT_TIME_TO_CHANGE_FILE



    suspend fun save(dirPath:String,log: List<String>):List<String> = coroutineScope {
        if (currentLogFile == null) currentLogFile = makeNewLogFile(dirPath)
        saveToFile(log)
        if (System.currentTimeMillis() > nextRenameTime || abs(System.currentTimeMillis() - nextRenameTime) > TIME_INTERVAL_TO_CHANGE_FILE) {
            renameLogFile(dirPath)
            nextRenameTime = System.currentTimeMillis() + TIME_INTERVAL_TO_CHANGE_FILE
        }
        val packets = File(dirPath).listFiles()?.filter { it.name.contains(DATA_FILE_EXT_TXT) }?.map { it.absolutePath }?: emptyList()
        return@coroutineScope packets
    }



    private suspend fun saveToFile(log: List<String>) =
        withContext(NonCancellable + dispatcher) {

            currentLogFile?.let { f ->
                FileWriter(f, true).use { w ->
                    log.forEach {
                        w.write(it)
                    }
                    w.flush()
                }
            }
        }

    private suspend fun makeNewLogFile(dirPath:String): File = coroutineScope {
        val timePoint = DateFormat.format("yyyy-MM-dd kk-mm-ss", Date(System.currentTimeMillis()))
        val dataFileName = "$DATA_FILE_PREFIX$DATA_IMEI_CODE $timePoint.$DATA_FILE_EXT_WRK"
        val dataFile = File(dirPath, dataFileName)
        addHeader(dataFile)
        return@coroutineScope dataFile
    }

    private suspend fun addHeader(file: File) = withContext(NonCancellable + dispatcher) {
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

    private suspend fun renameLogFile(dirPath:String) = withContext(NonCancellable + dispatcher) {
        currentLogFile?.let { f ->
            val fileNameForSend = f.name.replaceFirst(DATA_FILE_EXT_WRK, DATA_FILE_EXT_TXT)
            f.renameTo(File(dirPath, fileNameForSend))
        }
        currentLogFile = null

    }



}