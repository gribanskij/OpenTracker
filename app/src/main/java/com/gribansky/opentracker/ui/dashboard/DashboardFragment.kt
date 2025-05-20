package com.gribansky.opentracker.ui.dashboard

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.gribansky.opentracker.R
import com.gribansky.opentracker.core.log.PositionData
import com.gribansky.opentracker.core.TRACKER_CLIENT_BIND
import com.gribansky.opentracker.core.TrackerService
import com.gribansky.opentracker.databinding.FragmentDashboardBinding
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private  var scope:CoroutineScope? = null
    private lateinit var mService: TrackerService
    private var mBound: Boolean = false

    private val timeFormat = SimpleDateFormat("HH:mm:ss dd-MM-yy ", Locale.getDefault())

    private val hAdapter by lazy { HistoryAdapter() }



    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as TrackerService.LocalBinder
            mService = binder.getService()
            mBound = true
            scope?.observeTrackerState()
            scope?.observeTrackerHistory()
        }
        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDashboardBinding.bind(view)
        //val dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

        binding.history.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = this@DashboardFragment.hAdapter
            //addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL))
        }

    }

    override fun onStart() {
        super.onStart()
        scope = MainScope()
        requestPermissions()
        bindToService()
    }

    override fun onStop() {
        super.onStop()
        scope?.cancel()
        unBindFromService()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun bindToService() {
        val intent = Intent(requireActivity(), TrackerService::class.java).apply {
            action = TRACKER_CLIENT_BIND
        }
        requireActivity().startService(intent)
        requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun unBindFromService() {
        if (mBound) {
            requireActivity().unbindService(connection)
            mBound = false
        }
    }

    private fun CoroutineScope.observeTrackerState() {
        launch {
            mService.trackerState.collect { state ->
                val sTime = state.serviceLastStartTime?.let { timeFormat.format(Date(it)) } ?: "не определено"
                val posTime = state.gpsLastTime?.let { timeFormat.format(Date(it)) } ?: "не определено"
                val status = if (state.isForeground) "работает" else "ожидает запуска"
                val logTimeStr = state.logTime?.let { timeFormat.format(Date(it)) } ?: "не определено"
                val readyPackets = state.packetsReady ?: "-"
                val sentPackets = state.packetsSent ?: "-"

                val sb =
                    "Время старта: $sTime\n" +
                            "Последняя точка в: $posTime\n" +
                            "Статус трекера: $status\n" +
                            "Логирование: $logTimeStr\n" +
                            "Пакетов к отправке: $readyPackets\n" +
                            "Пакетов отправлено: $sentPackets"

                binding.textDashboard.text = sb

            }
        }
    }

    private fun CoroutineScope.observeTrackerHistory() {
        launch {
            mService.trackerHistory.collect { data ->
                hAdapter.setData(data)
            }
        }
    }


    private fun requestPermissions() {
        val list = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        list.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        list.add(Manifest.permission.ACCESS_FINE_LOCATION)
        list.add(Manifest.permission.READ_PHONE_STATE)


        requestPermissions(list.toTypedArray(),0)
    }

    private fun isIgnoreOptimization(): Boolean {
        val powerManager = ContextCompat.getSystemService(requireContext(), PowerManager::class.java)
        return powerManager?.isIgnoringBatteryOptimizations(requireContext().packageName)?:false
    }


    private fun isCanBackgroundLocation(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return checkSelfPermission(requireContext(),Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }


    private inner class HistoryAdapter : ListDelegationAdapter<MutableList<Any>>() {


        init {
            items = mutableListOf()
            delegatesManager.apply {
                addDelegate(HistoryDelegate())
            }
        }

        fun setData(data: List<PositionData>) {
            items?.clear()
            items?.addAll(data)
            notifyDataSetChanged()
        }
    }
}