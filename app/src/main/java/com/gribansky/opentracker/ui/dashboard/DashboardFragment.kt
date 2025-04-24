package com.gribansky.opentracker.ui.dashboard

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gribansky.opentracker.R
import com.gribansky.opentracker.core.TrackerService
import com.gribansky.opentracker.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private lateinit var mService: TrackerService
    private var mBound: Boolean = false

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as TrackerService.LocalBinder
            mService = binder.getService()
            mBound = true
            mService.setUpClient {
                binding.textDashboard.text = it.toString()
            }
        }
        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDashboardBinding.bind(view)
        //val dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

    }

    override fun onStart() {
        super.onStart()
        bindToService()
    }

    override fun onStop() {
        super.onStop()
        unBindFromService()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun bindToService(){
        requireActivity().startService(Intent(requireActivity(), TrackerService::class.java))
        Intent(requireActivity(), TrackerService::class.java).also { intent ->
            requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun unBindFromService(){
        requireActivity().unbindService(connection)
    }
}