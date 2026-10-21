package com.company.jifiit.Hotel

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.company.jifiit.R
import com.company.jifiit.ViewMoreMenu.RescheduleActivity
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.button.MaterialButton

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
        val ivOverflow: ImageView? = itemView.findViewById(R.id.ivOverflow)

        val tvCustomerName: TextView = itemView.findViewById(R.id.tvCustomerName)
        val tvRoomInfo: TextView = itemView.findViewById(R.id.tvRoomInfo)

        val tvDate: TextView? = itemView.findViewById(R.id.tvDate)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvTotalDuration: TextView? = itemView.findViewById(R.id.tvTotalDuration)

        val shimmerLayout: ShimmerFrameLayout? = itemView.findViewById(R.id.shimmerLayout)
        val ivExpertPhoto: ImageView? = itemView.findViewById(R.id.ivExpertPhoto)
        val tvExpertName: TextView = itemView.findViewById(R.id.tvExpertName)
        val tvExpertRole: TextView? = itemView.findViewById(R.id.tvExpertRole)
        val tvCardCommission: TextView? = itemView.findViewById(R.id.tvCardCommission)

        val btnCall: MaterialButton? = itemView.findViewById(R.id.btnCall)
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
            rawRoom.replace(Regex("(?i)^Room\\s*:?\\s*"), "")
        } else {
            rawRoom
        }
        val numPart = if (roomNumber.isNotEmpty() && !roomNumber.equals("N/A", ignoreCase = true)) {
            roomNumber
        } else {
            "101"
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

        // 3. Time, Date & Duration
        val timeDisplay = if (booking.time.isNotBlank() && booking.time != "N/A") booking.time else "07:00 PM"
        holder.tvTime.text = timeDisplay

        if (holder.tvDate != null) {
            val dateDisplay = if (booking.date.isNotBlank() && booking.date != "N/A") booking.date else "Tue, 26 Aug"
            holder.tvDate.text = dateDisplay
        }

        if (holder.tvTotalDuration != null) {
            if (booking.duration.isNotBlank() && booking.duration != "0" && !booking.duration.equals("N/A", ignoreCase = true)) {
                holder.tvTotalDuration.visibility = View.VISIBLE
                val durText = if (booking.duration.contains("min", ignoreCase = true) || booking.duration.contains("h", ignoreCase = true)) {
                    booking.duration
                } else {
                    "${booking.duration} mins"
                }
                holder.tvTotalDuration.text = durText
            } else {
                holder.tvTotalDuration.visibility = View.GONE
            }
        }

        // 4. Assigned Expert Info & Photo with Shimmer
        val expertDisplayName = if (booking.expertName.isNotBlank()) booking.expertName else "Rohan Sharma"
        holder.tvExpertName.text = expertDisplayName
        holder.tvExpertRole?.text = "ASSIGNED EXPERT"

        val photoUrl = if (booking.image.isNotBlank()) booking.image else booking.expertPhoto
        if (holder.ivExpertPhoto != null) {
            if (holder.shimmerLayout != null) {
                holder.shimmerLayout.visibility = View.VISIBLE
                holder.shimmerLayout.startShimmer()
                holder.ivExpertPhoto.visibility = View.INVISIBLE

                if (photoUrl.isNotBlank()) {
                    Glide.with(context)
                        .load(photoUrl)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>,
                                isFirstResource: Boolean
                            ): Boolean {
                                holder.shimmerLayout.stopShimmer()
                                holder.shimmerLayout.visibility = View.GONE
                                holder.ivExpertPhoto.visibility = View.VISIBLE
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable,
                                model: Any,
                                target: Target<Drawable>?,
                                dataSource: DataSource,
                                isFirstResource: Boolean
                            ): Boolean {
                                holder.shimmerLayout.stopShimmer()
                                holder.shimmerLayout.visibility = View.GONE
                                holder.ivExpertPhoto.visibility = View.VISIBLE
                                return false
                            }
                        })
                        .error(R.drawable.ic_person)
                        .into(holder.ivExpertPhoto)
                } else {
                    holder.shimmerLayout.stopShimmer()
                    holder.shimmerLayout.visibility = View.GONE
                    holder.ivExpertPhoto.visibility = View.VISIBLE
                    holder.ivExpertPhoto.setImageResource(R.drawable.ic_person)
                }
            } else {
                if (photoUrl.isNotBlank()) {
                    Glide.with(context)
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(holder.ivExpertPhoto)
                } else {
                    holder.ivExpertPhoto.setImageResource(R.drawable.ic_person)
                }
            }
        }

        // 5. Commission Info / Badge
        if (holder.tvCardCommission != null) {
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
        }

        // 6. Clicks: Item Card
        holder.itemView.setOnClickListener { onItemClick(booking) }

        // 7. Clicks: Call Expert Button
        holder.btnCall?.setOnClickListener {
            val phoneNum = if (booking.client_number.isNotBlank()) booking.client_number else booking.phone
            val number = phoneNum.trim()

            if (number.isEmpty() || number.equals("N/A", ignoreCase = true)) {
                Toast.makeText(context, "Phone number not available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$number")
                }
                context.startActivity(dialIntent)
            } catch (e: Exception) {
                Toast.makeText(context, "Unable to open dialer", Toast.LENGTH_SHORT).show()
            }
        }

        // 8. Clicks: 3-dot overflow menu
        holder.ivOverflow?.setOnClickListener { anchor ->
            val popup = PopupMenu(anchor.context, anchor)
            popup.menuInflater.inflate(R.menu.menu_booking_overflow, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_reschedule -> {
                        onReschedule(booking, context)
                        true
                    }
                    R.id.action_cancel -> {
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun onReschedule(booking: BookingModel, context: Context) {
        try {
            val intent = Intent(context, RescheduleActivity::class.java).apply {
                putExtra("id", booking.bookingId)
                putExtra("time", booking.time)
                putExtra("duration", booking.duration)
                putExtra("endTime", booking.EndTime)
                putExtra("date", booking.date)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open reschedule screen", Toast.LENGTH_SHORT).show()
        }
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