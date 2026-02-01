package com.example.hydrasense

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(private val readings: List<NetworkReading>) : 
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPh: TextView = view.findViewById(R.id.tvHistoryPh)
        val tvStatus: TextView = view.findViewById(R.id.tvHistoryStatus)
        val tvTime: TextView = view.findViewById(R.id.tvHistoryTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reading = readings[position]
        holder.tvPh.text = "pH: ${reading.ph_value}"
        
        val isAcidic = reading.ph_value < 6.0
        holder.tvStatus.text = if (isAcidic) "Acidic" else "Normal"
        holder.tvStatus.setTextColor(if (isAcidic) Color.RED else Color.parseColor("#4CAF50"))
        
        holder.tvTime.text = "${reading.date} ${reading.timestamp}"
    }

    override fun getItemCount() = readings.size
}
