package com.gribansky.opentracker.core.log

interface ILogManager{
    suspend fun startLogCollect (path: String, events: List<String>, now: Boolean):LogResult
}

