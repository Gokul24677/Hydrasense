package com.example.hydrasense

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HydrationHistoryAdapter(private val records: List<HydrationRecord>) :
    RecyclerView.Adapter<HydrationHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTime: TextView = view.findViewById(R.id.tvHistoryTime)
        val tvRec: TextView = view.findViewById(R.id.tvHistoryRec)
        val tvPh: TextView = view.findViewById(R.id.tvHistoryPh)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_analysis_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = records[position]
        holder.tvTime.text = record.time
        holder.tvRec.text = record.recommendation
        holder.tvPh.text = "pH ${record.ph}"
    }

    override fun getItemCount() = records.size
}
