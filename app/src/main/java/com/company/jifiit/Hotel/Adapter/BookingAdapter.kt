package com.company.jifiit.Hotel

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.company.jifiit.R
import com.google.android.material.button.MaterialButton

class BookingAdapter(
    private val bookingList: List<BookingModel>,
    private val onItemClick: (BookingModel) -> Unit
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    inner class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBookingId: TextView = itemView.findViewById(R.id.tvBookingId)
        val tvInRoomTag: TextView = itemView.findViewById(R.id.tvInRoomTag)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val ivOverflow: ImageView = itemView.findViewById(R.id.ivOverflow)

        val tvCustomerName: TextView = itemView.findViewById(R.id.tvCustomerName)
        val tvRoomInfo: TextView = itemView.findViewById(R.id.tvRoomInfo)

        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)

        val btnCall: MaterialButton = itemView.findViewById(R.id.btnCall)
        val btnViewDetails: MaterialButton = itemView.findViewById(R.id.btnViewDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.hote_item_booking_card, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookingList[position]

        // 1. Header
        holder.tvBookingId.text = "#${booking.bookingId}"

        // ✅ Service tag based on massage_types
        val tagLabel = getServiceTagLabel(booking.massage_types)
        if (tagLabel.isNullOrEmpty()) {
            holder.tvInRoomTag.visibility = View.GONE
        } else {
            holder.tvInRoomTag.visibility = View.VISIBLE
            holder.tvInRoomTag.text = tagLabel
        }

        // Status color + background
        holder.tvStatus.text = booking.status
        when (booking.status.uppercase()) {
            "PENDING" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#FF9800"))
                holder.tvStatus.setBackgroundResource(R.drawable.bg_tag_pending)
            }
            "COMPLETED" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
                holder.tvStatus.setBackgroundResource(R.drawable.bg_tag_pending)
            }
            "CONFIRMED" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#1A56DB"))
                holder.tvStatus.setBackgroundResource(R.drawable.bg_tag_blue)
            }
            else -> {
                holder.tvStatus.setTextColor(Color.parseColor("#757575"))
                holder.tvStatus.setBackgroundResource(R.drawable.bg_tag_pending)
            }
        }

        // 2. Customer info
        holder.tvCustomerName.text = booking.customerName
        holder.tvRoomInfo.text = booking.room_number

        // 3. Date + Time
        holder.tvDate.text = booking.date
        holder.tvTime.text = booking.time

        // 4. Clicks
        holder.itemView.setOnClickListener { onItemClick(booking) }
        holder.btnViewDetails.setOnClickListener { onItemClick(booking) }
        holder.btnCall.setOnClickListener { onItemClick(booking) }
        holder.ivOverflow.setOnClickListener { /* popup menu */ }
    }

    private fun getServiceTagLabel(massageTypes: String?): String? {
        return when (massageTypes?.trim()?.lowercase()) {
            "massage at hotel"          -> "In-Room Massage"
            "yoga at hotel"             -> "Yoga"
            "personal trainer at hotel" -> "Personal Trainer"
            "ayurveda at hotel"         -> "Ayurveda"
            else                        -> null
        }
    }

    override fun getItemCount(): Int = bookingList.size
}