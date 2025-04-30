package com.gribansky.opentracker.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

class TrackerManager (private val timeManager: TimeManager) {

    private val _commands = MutableSharedFlow<Int>()
    val commands: SharedFlow<Int> = _commands


    private val _trackerState = MutableStateFlow(0)
    val trackerState: StateFlow<Int> = _trackerState



    fun setAction(action:String){


    }

    fun stopService(){

    }

    private fun saveState(){

    }

    private fun restoreState(){

    }

    private fun saveLog(){

    }

}