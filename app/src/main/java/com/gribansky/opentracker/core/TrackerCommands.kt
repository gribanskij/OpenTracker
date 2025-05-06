package com.gribansky.opentracker.core

sealed class TrackerCommands

class RestartTimer(val startTime:Long):TrackerCommands()
data object StopForeground : TrackerCommands()
data object StartForeground : TrackerCommands()
data object StartCollectLocPoints : TrackerCommands ()
