package com.company.jifiit.Hotel

import android.content.Intent
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
    private val onItemClick: (BookingModel) -> Unit
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    inner class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val shimmerLayout: ShimmerFrameLayout =
            itemView.findViewById(R.id.shimmerLayout)
        val tvBookingId: TextView = itemView.findViewById(R.id.tvBookingId)
        val tvInRoomTag: TextView = itemView.findViewById(R.id.tvInRoomTag)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val ivOverflow: ImageView = itemView.findViewById(R.id.ivOverflow)
        val ivExpert: ImageView = itemView.findViewById(R.id.ivExpertAvatar)
        val expertName: TextView = itemView.findViewById(R.id.tvExpertName)
        val totalDuration: TextView = itemView.findViewById(R.id.tvTotalDuration)

        val tvCustomerName: TextView = itemView.findViewById(R.id.tvCustomerName)
        val tvRoomInfo: TextView = itemView.findViewById(R.id.tvRoomInfo)

        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)

        val btnCall: MaterialButton = itemView.findViewById(R.id.btnCall)
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
        holder.expertName.text = booking.expertName
        holder.totalDuration.text = booking.duration +" mins"


        // ✅ Room formatting: "Room: 112" with "Room: " in grey and number in bold
        val rawRoom = booking.room_number.trim()
        val roomNumber = if (rawRoom.startsWith("Room", ignoreCase = true)) {
            rawRoom.replace(Regex("(?i)^Room\\s*"), "")
        } else {
            rawRoom
        }
        val numPart = if (roomNumber.isNotEmpty() && !roomNumber.equals("N/A", ignoreCase = true)) {
            roomNumber
        } else {
            "101"
        }

        val spannableRoom = SpannableStringBuilder()
        val prefix = "Room: "
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

        // 3. Time & Date
        holder.tvTime.text = if (booking.time.isNotBlank() && booking.time != "N/A") booking.time else "07:00 PM"
        holder.tvDate.text = booking.date

        // 4. Clicks
        holder.itemView.setOnClickListener { onItemClick(booking) }


        holder.shimmerLayout.visibility = View.VISIBLE
        holder.shimmerLayout.startShimmer()
        holder.ivExpert.visibility = View.INVISIBLE

        if (booking.image.isNotEmpty()) {

            Glide.with(holder.itemView.context)
                .load(booking.image)
                .listener(object : RequestListener<Drawable> {

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {

                        holder.shimmerLayout.stopShimmer()
                        holder.shimmerLayout.visibility = View.GONE
                        holder.ivExpert.visibility = View.VISIBLE
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
                        holder.ivExpert.visibility = View.VISIBLE
                        return false
                    }

                })
                .error(R.mipmap.hotel_body_massage)
                .into(holder.ivExpert)

        } else {

            holder.shimmerLayout.stopShimmer()
            holder.shimmerLayout.visibility = View.GONE
            holder.ivExpert.visibility = View.VISIBLE
            holder.ivExpert.setImageResource(
                R.mipmap.hotel_body_massage
            )
        }
        // ✅ Call button → directly dial client_number
        holder.btnCall.setOnClickListener {
            val number = booking.client_number.trim()

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

        holder.ivOverflow.setOnClickListener { anchor ->
            val popup = PopupMenu(anchor.context, anchor)
            popup.menuInflater.inflate(R.menu.menu_booking_overflow, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_reschedule -> {
                        onReschedule(booking, context)
                        true
                    }
                    R.id.action_cancel -> {
//                        onCancel(booking)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }


    private fun onReschedule(booking: BookingModel, context: android.content.Context) {

        val intent = Intent(context, RescheduleActivity::class.java)

        intent.putExtra("id", booking.bookingId)
        intent.putExtra("time", booking.time)
        intent.putExtra("duration", booking.duration)

        // Replace these with the actual BookingModel property names
        intent.putExtra("endTime", booking.EndTime)
        intent.putExtra("date", booking.date)

        context.startActivity(intent)
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