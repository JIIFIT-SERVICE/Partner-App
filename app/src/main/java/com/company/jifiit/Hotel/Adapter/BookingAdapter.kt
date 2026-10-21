package com.company.jifiit.Hotel

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.company.jifiit.R

class BookingAdapter(
    private var bookingList: List<BookingModel>,
    private val onFeedbackClick: ((BookingModel) -> Unit)? = null,
    private val onItemClick: (BookingModel) -> Unit = {}
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    constructor(
        bookingList: List<BookingModel>,
        onItemClick: (BookingModel) -> Unit
    ) : this(bookingList, null, onItemClick)

    inner class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBookingId: TextView = itemView.findViewById(R.id.tvBookingId)
        val tvInRoomTag: TextView = itemView.findViewById(R.id.tvInRoomTag)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        val tvCustomerName: TextView = itemView.findViewById(R.id.tvCustomerName)
        val tvRoomInfo: TextView = itemView.findViewById(R.id.tvRoomInfo)

        val ivExpertPhoto: ImageView = itemView.findViewById(R.id.ivExpertPhoto)
        val tvExpertName: TextView = itemView.findViewById(R.id.tvExpertName)
        val tvExpertRole: TextView = itemView.findViewById(R.id.tvExpertRole)
        val tvCardCommission: TextView = itemView.findViewById(R.id.tvCardCommission)

        val tvDate: TextView? = itemView.findViewById(R.id.tvDate)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
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

        // Service tag based on massage_types
        val tagLabel = getServiceTagLabel(booking.massage_types)
        if (tagLabel.isNullOrEmpty()) {
            holder.tvInRoomTag.visibility = View.GONE
        } else {
            holder.tvInRoomTag.visibility = View.VISIBLE
            holder.tvInRoomTag.text = tagLabel
        }

        // Status Badge styling & proper icon coloring
        val statusUpper = booking.status.trim().uppercase()
        holder.tvStatus.text = statusUpper

        when (statusUpper) {
            "PENDING" -> {
                val colorPending = Color.parseColor("#B45309")
                holder.tvStatus.setTextColor(colorPending)
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending_pill)
                val icon = ContextCompat.getDrawable(context, R.drawable.ic_pending)?.mutate()
                icon?.setBounds(0, 0, (14 * context.resources.displayMetrics.density).toInt(), (14 * context.resources.displayMetrics.density).toInt())
                icon?.setTint(colorPending)
                holder.tvStatus.setCompoundDrawables(icon, null, null, null)
                holder.tvStatus.compoundDrawableTintList = ColorStateList.valueOf(colorPending)
            }
            "COMPLETED" -> {
                val colorCompleted = Color.parseColor("#15803D")
                holder.tvStatus.setTextColor(colorCompleted)
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_completed_pill)
                val icon = ContextCompat.getDrawable(context, R.drawable.baseline_check_circle_outline_24)?.mutate()
                icon?.setBounds(0, 0, (14 * context.resources.displayMetrics.density).toInt(), (14 * context.resources.displayMetrics.density).toInt())
                icon?.setTint(colorCompleted)
                holder.tvStatus.setCompoundDrawables(icon, null, null, null)
                holder.tvStatus.compoundDrawableTintList = ColorStateList.valueOf(colorCompleted)
            }
            "CANCELLED", "CANCELED" -> {
                val colorCancelled = Color.parseColor("#DC2626")
                holder.tvStatus.setTextColor(colorCancelled)
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled_pill)
                val icon = ContextCompat.getDrawable(context, R.drawable.ic_close)?.mutate()
                icon?.setBounds(0, 0, (14 * context.resources.displayMetrics.density).toInt(), (14 * context.resources.displayMetrics.density).toInt())
                icon?.setTint(colorCancelled)
                holder.tvStatus.setCompoundDrawables(icon, null, null, null)
                holder.tvStatus.compoundDrawableTintList = ColorStateList.valueOf(colorCancelled)
            }
            "CONFIRMED" -> {
                val colorConfirmed = Color.parseColor("#1D4ED8")
                holder.tvStatus.setTextColor(colorConfirmed)
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_confirmed_pill)
                val icon = ContextCompat.getDrawable(context, R.drawable.baseline_check_circle_outline_24)?.mutate()
                icon?.setBounds(0, 0, (14 * context.resources.displayMetrics.density).toInt(), (14 * context.resources.displayMetrics.density).toInt())
                icon?.setTint(colorConfirmed)
                holder.tvStatus.setCompoundDrawables(icon, null, null, null)
                holder.tvStatus.compoundDrawableTintList = ColorStateList.valueOf(colorConfirmed)
            }
            else -> {
                val colorDefault = Color.parseColor("#64748B")
                holder.tvStatus.setTextColor(colorDefault)
                holder.tvStatus.setBackgroundResource(R.drawable.bg_service_tag_pill)
                holder.tvStatus.setCompoundDrawables(null, null, null, null)
                holder.tvStatus.compoundDrawableTintList = null
            }
        }

        // 2. Customer Name & Room Info
        holder.tvCustomerName.text = booking.customerName

        // Room formatting: "Room 112" with "Room " in grey and number in bold
        val rawRoom = if (booking.room_number.isNotBlank()) booking.room_number.trim() else booking.roomInfo.trim()
        val roomNumber = if (rawRoom.startsWith("Room", ignoreCase = true)) {
            rawRoom.replace(Regex("(?i)^Room\\s*"), "")
        } else {
            rawRoom
        }

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

        // 3. Expert Info (Requirement 2)
        val expertDisplayName = if (booking.expertName.isNotBlank()) booking.expertName else "Rohan Sharma"
        holder.tvExpertName.text = expertDisplayName
        holder.tvExpertRole.text = "ASSIGNED EXPERT"

        if (booking.expertPhoto.isNotBlank()) {
            Glide.with(context)
                .load(booking.expertPhoto)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(holder.ivExpertPhoto)
        } else {
            holder.ivExpertPhoto.setImageResource(R.drawable.ic_person)
        }

        // Commission Tag
        when (statusUpper) {
            "COMPLETED" -> {
                holder.tvCardCommission.visibility = View.VISIBLE
                val comm = if (booking.commissionAmount.isNotBlank()) booking.commissionAmount else "₹100"
                holder.tvCardCommission.text = "+$comm Commission"
                holder.tvCardCommission.setTextColor(Color.parseColor("#059669"))
                holder.tvCardCommission.setBackgroundResource(R.drawable.bg_commission_badge)
            }
            "CANCELLED", "CANCELED" -> {
                holder.tvCardCommission.visibility = View.VISIBLE
                holder.tvCardCommission.text = "₹0 Commission"
                holder.tvCardCommission.setTextColor(Color.parseColor("#94A3B8"))
                holder.tvCardCommission.setBackgroundResource(R.drawable.bg_pill_unselected)
            }
            else -> {
                holder.tvCardCommission.visibility = View.VISIBLE
                holder.tvCardCommission.text = "₹100 Pending"
                holder.tvCardCommission.setTextColor(Color.parseColor("#D97706"))
                holder.tvCardCommission.setBackgroundResource(R.drawable.bg_status_pending_pill)
            }
        }

        // 4. Time & Date
        val timeDisplay = if (booking.time.isNotBlank()) booking.time else "07:00 PM"
        val fullTimeText = if (booking.date.isNotBlank()) "$timeDisplay • ${booking.date}" else timeDisplay
        holder.tvTime.text = fullTimeText

        // Card item click
        holder.itemView.setOnClickListener { onItemClick(booking) }
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