package com.company.jifiit.Hotel

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.company.jifiit.R

class HotelCommissionLedgerAdapter(
    private var ledgerList: List<HotelCommissionLedgerItem>,
    private val onItemClick: (HotelCommissionLedgerItem) -> Unit
) : RecyclerView.Adapter<HotelCommissionLedgerAdapter.LedgerViewHolder>() {

    class LedgerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivServiceLedgerIcon: android.widget.ImageView = itemView.findViewById(R.id.ivServiceLedgerIcon)
        val tvBookingTitle: TextView = itemView.findViewById(R.id.tvLedgerBookingTitle)
        val tvServiceSubtitle: TextView = itemView.findViewById(R.id.tvLedgerServiceSubtitle)
        val tvDateChannel: TextView = itemView.findViewById(R.id.tvLedgerDateChannel)
        val tvAmount: TextView = itemView.findViewById(R.id.tvLedgerAmount)
        val tvStatusBadge: TextView = itemView.findViewById(R.id.tvLedgerStatusBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LedgerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hotel_commission_ledger, parent, false)
        return LedgerViewHolder(view)
    }

    override fun onBindViewHolder(holder: LedgerViewHolder, position: Int) {
        val item = ledgerList[position]

        holder.tvBookingTitle.text = "Booking #${item.bookingId} • Room ${item.roomNumber}"
        holder.tvServiceSubtitle.text = "${item.customerName} • ${item.serviceName}"
        holder.tvDateChannel.text = "Date: ${item.dateText} • Booked via ${item.bookingChannel}"
        holder.tvAmount.text = item.amountText

        if (item.serviceName.contains("Yoga", ignoreCase = true)) {
            holder.ivServiceLedgerIcon.setImageResource(R.drawable.ic_yoga)
        } else {
            holder.ivServiceLedgerIcon.setImageResource(R.drawable.ic_massage)
        }

        if (item.isSettled) {
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_settled_pill)
            holder.tvStatusBadge.setTextColor(Color.parseColor("#065F46"))
            holder.tvStatusBadge.text = "Settled"
        } else {
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_pending_pill)
            holder.tvStatusBadge.setTextColor(Color.parseColor("#B45309"))
            holder.tvStatusBadge.text = "Pending Session"
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = ledgerList.size

    fun updateList(newList: List<HotelCommissionLedgerItem>) {
        ledgerList = newList
        notifyDataSetChanged()
    }
}
