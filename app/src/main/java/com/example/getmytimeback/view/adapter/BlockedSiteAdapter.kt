package com.example.getmytimeback.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.getmytimeback.R
import com.example.getmytimeback.model.BlockedSite

class BlockedSiteAdapter(private val sites: List<BlockedSite>) :
    RecyclerView.Adapter<BlockedSiteAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val siteName: TextView = view.findViewById(R.id.tvSiteName)
        val allowedTime: TextView = view.findViewById(R.id.tvAllowedTime)
        val consumedTime: TextView = view.findViewById(R.id.tvConsumedTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_blocked_site, parent, false) // Ensure correct layout file
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val site = sites[position]
        holder.siteName.text = site.site
        holder.allowedTime.text = "Allowed: ${site.allowedTime} min"
        holder.consumedTime.text = "Used: ${site.consumedTime} sec"
    }

    override fun getItemCount() = sites.size
}
