package com.gribansky.opentracker.core.log

interface IFileSaver {
    suspend fun save(dirPath: String, log: List<String>, makeNow: Boolean): Result<List<String>>
}