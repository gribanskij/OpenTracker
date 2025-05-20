package com.gribansky.opentracker.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gribansky.opentracker.core.log.PositionData
import com.gribansky.opentracker.core.log.PositionDataLog
import com.gribansky.opentracker.core.log.PositionGpsData
import com.gribansky.opentracker.core.log.PositionGsmData
import com.gribansky.opentracker.databinding.HistoryItemBinding
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryDelegate : AdapterDelegate<MutableList<Any>>() {

    private val dateFormat = SimpleDateFormat("HH:mm:ss dd.MM.yy", Locale.getDefault())

    override fun isForViewType(items: MutableList<Any>, position: Int) =
        items[position] is PositionData

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val binding = HistoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        items: MutableList<Any>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payloads: MutableList<Any>
    ) {
        (holder as ViewHolder).bind(items[position] as PositionData)
    }

    private inner class ViewHolder(
        private val binding: HistoryItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PositionData) {
            val time = dateFormat.format(Date(item.eventDate))
            val (type, desc) = when (item) {
                is PositionGpsData -> "GPS" to "lat:${item.gpsLocation.latitude}, lon:${item.gpsLocation.longitude}"
                is PositionDataLog -> "LOG" to "${item.logTag}:${item.logMessage}"
                is PositionGsmData -> "GSM" to "???"
            }
            setTextViews(time, type, desc)
        }

        private fun setTextViews(time: String, type: String, desc: String) {
            binding.date.text = time
            binding.type.text = type
            binding.desc.text = desc
        }
    }
}