package com.gribansky.opentracker.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class TrackerManager (private val timeManager: TimeManager) {

    private val _commands = MutableSharedFlow<TrackerCommands>()
    val commands: SharedFlow<TrackerCommands> = _commands.asSharedFlow()


    private val _trackerState = MutableStateFlow(TrackerState())
    val trackerState: StateFlow<TrackerState> = _trackerState.asStateFlow()



    fun setAction(action:TrackerAction){


        when(action){

            TrackerAction.TIME_SET -> {
                _commands.tryEmit(StartForeground)


            }
            TrackerAction.TIMEZONE_CHANGED -> {
                _commands.tryEmit(StartForeground)


            }

            TrackerAction.APP_UPDATED -> {
                _commands.tryEmit(StartForeground)


            }

            TrackerAction.NEXT_POINT -> {
                _commands.tryEmit(StartForeground)


            }

            TrackerAction.PHONE_RESTARTED -> {
                _commands.tryEmit(StartForeground)


            }
            TrackerAction.UNDEFINED -> {

                _commands.tryEmit(StartForeground)


            }

            TrackerAction.CLIENT_BIND -> {

                _commands.tryEmit(StartForeground)

            }

            TrackerAction.DATE_CHANGED -> {

                _commands.tryEmit(StartForeground)


            }
        }

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