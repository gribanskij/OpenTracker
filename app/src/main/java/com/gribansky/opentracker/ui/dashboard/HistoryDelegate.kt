package com.gribansky.opentracker.ui.dashboard


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gribansky.opentracker.core.PositionData
import com.gribansky.opentracker.core.PositionDataLog
import com.gribansky.opentracker.core.PositionGpsData
import com.gribansky.opentracker.core.PositionGsmData
import com.gribansky.opentracker.databinding.HistoryItemBinding
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class HistoryDelegate: AdapterDelegate<MutableList<Any>>() {


    private val dateFormat = SimpleDateFormat("HH:mm:ss dd.MM.yy", Locale.getDefault())


    override fun isForViewType(items: MutableList<Any>, position: Int) =
        items[position] is PositionData

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val binding =HistoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }


    override fun onBindViewHolder(
        items: MutableList<Any>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payloads: MutableList<Any>
    ) {
        (holder as ViewHolder).bind(
            items[position] as PositionData,
            position
        )
    }

    private inner class ViewHolder(
        private val binding: HistoryItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {



        fun bind(item: PositionData, pos: Int) {

            when(item){
                is PositionGpsData -> {bindGps(item)}
                is PositionDataLog -> {bindLog(item)}
                is PositionGsmData -> {bindGsm(item)}
            }
        }

        private fun bindGps(p:PositionGpsData){

            val time = dateFormat.format(Date(p.eventDate))
            val desc = "lat:${p.gpsLocation.latitude}, lon:${p.gpsLocation.longitude}"

            binding.date.text = time
            binding.type.text = "GPS"
            binding.desc.text = desc

        }

        private fun bindLog(p:PositionDataLog){

            val time = dateFormat.format(Date(p.eventDate))
            val desc = "${p.logTag}:${p.logMessage}"

            binding.date.text = time
            binding.type.text = "LOG"
            binding.desc.text = desc


        }

        private fun bindGsm(p:PositionGsmData){

            val time = dateFormat.format(Date(p.eventDate))
            val desc = "???"

            binding.date.text = time
            binding.type.text = "GSM"
            binding.desc.text = desc

        }


    }
}