package com.gribansky.opentracker.core.log

interface INetSender {
    suspend fun send(files: List<String>, sendNow: Boolean): Result<Int>
}