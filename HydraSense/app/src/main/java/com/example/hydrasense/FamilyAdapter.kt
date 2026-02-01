package com.example.hydrasense

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FamilyAdapter(private val members: List<FamilyMember>) :
    RecyclerView.Adapter<FamilyAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvMemberName)
        val tvStatus: TextView = view.findViewById(R.id.tvMemberStatus)
        val tvPercent: TextView = view.findViewById(R.id.tvPercentage)
        val pbHydration: ProgressBar = view.findViewById(R.id.pbHydration)
        val imgMember: ImageView = view.findViewById(R.id.imgMember)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_family, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val member = members[position]
        holder.tvName.text = member.name
        holder.tvStatus.text = "Status: ${member.status}"
        holder.tvPercent.text = "${member.hydrationPercentage}%"
        holder.pbHydration.progress = member.hydrationPercentage
        holder.imgMember.setImageResource(member.avatarRes)
        
        // Color coding status
        val color = if (member.hydrationPercentage < 40) "#D32F2F" else "#4CAF50"
        holder.tvStatus.setTextColor(android.graphics.Color.parseColor(color))
        holder.tvPercent.setTextColor(android.graphics.Color.parseColor(color))
    }

    override fun getItemCount() = members.size
}
