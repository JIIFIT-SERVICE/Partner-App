package com.company.jifiit.Hotel

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.company.jifiit.R
import com.google.android.material.button.MaterialButton

class BookingAdapter(
    private var bookingList: List<BookingModel>,
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
        val context = holder.itemView.context

        // 1. Header: Booking ID
        holder.tvBookingId.text = if (booking.bookingId.startsWith("#")) booking.bookingId else "#${booking.bookingId}"

        // ✅ Service tag based on massage_types
        val tagLabel = getServiceTagLabel(booking.massage_types)
        if (tagLabel.isNullOrEmpty()) {
            holder.tvInRoomTag.visibility = View.GONE
        } else {
            holder.tvInRoomTag.visibility = View.VISIBLE
            holder.tvInRoomTag.text = tagLabel
        }

        // Status Badge styling
        val statusUpper = booking.status.trim().uppercase()
        holder.tvStatus.text = statusUpper

        when (statusUpper) {
            "PENDING" -> {
                val colorPending = Color.parseColor("#B45309")
                holder.tvStatus.setTextColor(colorPending)
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending_pill)
                val icon = ContextCompat.getDrawable(context, R.drawable.ic_pending)
                icon?.setBounds(0, 0, (14 * context.resources.displayMetrics.density).toInt(), (14 * context.resources.displayMetrics.density).toInt())
                icon?.setTint(colorPending)
                holder.tvStatus.setCompoundDrawables(icon, null, null, null)
            }
            "COMPLETED" -> {
                val colorCompleted = Color.parseColor("#15803D")
                holder.tvStatus.setTextColor(colorCompleted)
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_completed_pill)
                val icon = ContextCompat.getDrawable(context, R.drawable.baseline_check_circle_outline_24)
                icon?.setBounds(0, 0, (14 * context.resources.displayMetrics.density).toInt(), (14 * context.resources.displayMetrics.density).toInt())
                icon?.setTint(colorCompleted)
                holder.tvStatus.setCompoundDrawables(icon, null, null, null)
            }
            "CONFIRMED" -> {
                val colorConfirmed = Color.parseColor("#1D4ED8")
                holder.tvStatus.setTextColor(colorConfirmed)
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_confirmed_pill)
                val icon = ContextCompat.getDrawable(context, R.drawable.baseline_check_circle_outline_24)
                icon?.setBounds(0, 0, (14 * context.resources.displayMetrics.density).toInt(), (14 * context.resources.displayMetrics.density).toInt())
                icon?.setTint(colorConfirmed)
                holder.tvStatus.setCompoundDrawables(icon, null, null, null)
            }
            else -> {
                val colorDefault = Color.parseColor("#64748B")
                holder.tvStatus.setTextColor(colorDefault)
                holder.tvStatus.setBackgroundResource(R.drawable.bg_service_tag_pill)
                holder.tvStatus.setCompoundDrawables(null, null, null, null)
            }
        }

        // 2. Customer Name & Room Info
        holder.tvCustomerName.text = booking.customerName
        holder.tvRoomInfo.text = booking.room_number

        // Room formatting: "Room 112" with "Room " in grey and number in bold
        val rawRoom = booking.roomInfo.trim()
        val roomNumber = if (rawRoom.startsWith("Room", ignoreCase = true)) {
            rawRoom.replace(Regex("(?i)^Room\\s*"), "")
        } else {
            rawRoom
        }
        val roomText = if (roomNumber.isNotEmpty()) "Room $roomNumber" else "Room 101"

        val spannableRoom = SpannableStringBuilder()
        val prefix = "Room "
        spannableRoom.append(prefix)
        spannableRoom.setSpan(
            ForegroundColorSpan(Color.parseColor("#64748B")),
            0,
            prefix.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val numberStart = prefix.length
        val numPart = if (roomNumber.isNotEmpty()) roomNumber else "101"
        spannableRoom.append(numPart)
        spannableRoom.setSpan(
            ForegroundColorSpan(Color.parseColor("#0F172A")),
            numberStart,
            numberStart + numPart.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableRoom.setSpan(
            StyleSpan(Typeface.BOLD),
            numberStart,
            numberStart + numPart.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        holder.tvRoomInfo.text = spannableRoom

        // 3. Time
        holder.tvTime.text = if (booking.time.isNotBlank()) booking.time else "07:00 PM"
        holder.tvDate.text = booking.date

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

    fun updateData(newList: List<BookingModel>) {
        bookingList = newList
        notifyDataSetChanged()
    }
}