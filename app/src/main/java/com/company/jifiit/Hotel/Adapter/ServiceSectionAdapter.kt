package com.company.jifiit.Hotel.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.company.jifiit.Hotel.Model.ServiceItemModel
import com.company.jifiit.Hotel.Model.ServiceSectionModel
import com.company.jifiit.R

class ServiceSectionAdapter(
    private val sections: List<ServiceSectionModel>,
    private val onAddClick: (ServiceItemModel) -> Unit
) : RecyclerView.Adapter<ServiceSectionAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvSectionTitle: TextView = view.findViewById(R.id.tvSectionTitle)
        val tvSectionSubtitle: TextView = view.findViewById(R.id.tvSectionSubtitle)
        val rvCards: RecyclerView = view.findViewById(R.id.rvCards)
        val btnPrev: ImageView = view.findViewById(R.id.btnPrev)
        val btnNext: ImageView = view.findViewById(R.id.btnNext)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_section_header, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val section = sections[position]

        holder.tvSectionTitle.text = section.title
        holder.tvSectionSubtitle.text = "${section.items.size} options available"

        // Nested horizontal RecyclerView
        val cardAdapter = ServiceCardAdapter(section.items, onAddClick)
        holder.rvCards.layoutManager =
            LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
        holder.rvCards.adapter = cardAdapter
        holder.rvCards.setHasFixedSize(true)

        // Prev/Next scroll
        holder.btnPrev.setOnClickListener {
            holder.rvCards.smoothScrollBy((-400f).toInt(), 0)
        }
        holder.btnNext.setOnClickListener {
            holder.rvCards.smoothScrollBy(400, 0)
        }
    }

    override fun getItemCount() = sections.size
}