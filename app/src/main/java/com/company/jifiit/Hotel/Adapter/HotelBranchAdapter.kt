package com.company.jifiit.Hotel.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.company.jifiit.Hotel.Model.HotelBranch
import com.company.jifiit.R

class HotelBranchAdapter(
    private var branches: List<HotelBranch>,
    private var selectedBranchId: String,
    private val onBranchClick: (HotelBranch) -> Unit
) : RecyclerView.Adapter<HotelBranchAdapter.BranchViewHolder>() {

    class BranchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: View = itemView.findViewById(R.id.cardBranchItem)
        val tvName: TextView = itemView.findViewById(R.id.tvItemBranchName)
        val tvTag: TextView = itemView.findViewById(R.id.tvItemBranchTag)
        val tvAddress: TextView = itemView.findViewById(R.id.tvItemBranchAddress)
        val tvRooms: TextView = itemView.findViewById(R.id.tvItemBranchRooms)
        val tvStars: TextView = itemView.findViewById(R.id.tvItemBranchStars)
        val tvPropertyType: TextView = itemView.findViewById(R.id.tvItemBranchPropertyType)
        val ivSelectedCheck: ImageView = itemView.findViewById(R.id.ivSelectedCheck)
        val tvActiveBadge: TextView = itemView.findViewById(R.id.tvActiveBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BranchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hotel_branch, parent, false)
        return BranchViewHolder(view)
    }

    override fun onBindViewHolder(holder: BranchViewHolder, position: Int) {
        val branch = branches[position]
        val isSelected = branch.id == selectedBranchId

        holder.tvName.text = branch.name
        holder.tvTag.text = "${branch.branchTag} • ID #${branch.id}"
        holder.tvAddress.text = branch.address
        holder.tvRooms.text = "${branch.totalRooms} Rooms"
        holder.tvStars.text = "${branch.categoryStars}-Star"
        holder.tvPropertyType.text = branch.propertyType

        if (isSelected) {
            holder.container.setBackgroundResource(R.drawable.bg_branch_item_selected)
            holder.ivSelectedCheck.visibility = View.VISIBLE
            holder.tvActiveBadge.visibility = View.VISIBLE
        } else {
            holder.container.setBackgroundResource(R.drawable.bg_branch_item_unselected)
            holder.ivSelectedCheck.visibility = View.GONE
            holder.tvActiveBadge.visibility = View.GONE
        }

        holder.container.setOnClickListener {
            selectedBranchId = branch.id
            notifyDataSetChanged()
            onBranchClick(branch)
        }
    }

    override fun getItemCount(): Int = branches.size

    fun updateData(newBranches: List<HotelBranch>, newSelectedId: String) {
        branches = newBranches
        selectedBranchId = newSelectedId
        notifyDataSetChanged()
    }
}
