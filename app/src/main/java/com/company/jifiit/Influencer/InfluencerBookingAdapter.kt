package com.company.jifiit.Influencer

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.company.jifiit.R
import java.util.Locale

class InfluencerBookingAdapter(
    private val context: Context,
    private var bookings: MutableList<InfluencerBooking>,
    private val onItemClick: ((InfluencerBooking, Int) -> Unit)? = null
) : RecyclerView.Adapter<InfluencerBookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rowRootLayout: LinearLayout = itemView.findViewById(R.id.rowRootLayout)
        val tvCustomerName: TextView = itemView.findViewById(R.id.tvRowCustomerName)
        val tvDiscountTag: TextView = itemView.findViewById(R.id.tvRowDiscountTag)
        val tvPhone: TextView = itemView.findViewById(R.id.tvRowPhone)
        val tvServiceName: TextView = itemView.findViewById(R.id.tvRowServiceName)
        val tvBookingDate: TextView = itemView.findViewById(R.id.tvRowBookingDate)
        val tvLocation: TextView = itemView.findViewById(R.id.tvRowLocation)
        val tvLocationArea: TextView = itemView.findViewById(R.id.tvRowLocationArea)
        val layoutRating: LinearLayout = itemView.findViewById(R.id.layoutRowRating)
        val tvRating: TextView = itemView.findViewById(R.id.tvRowRating)
        val tvFeedback: TextView = itemView.findViewById(R.id.tvRowFeedback)
        val layoutPaymentStatus: LinearLayout = itemView.findViewById(R.id.layoutRowPaymentStatus)
        val ivStatusIcon: ImageView = itemView.findViewById(R.id.ivRowStatusIcon)
        val tvPaymentStatus: TextView = itemView.findViewById(R.id.tvRowPaymentStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_influencer_booking_row, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val item = bookings[position]

        // 1. Customer Name & Discount
        holder.tvCustomerName.text = item.customerName
        holder.tvDiscountTag.text = item.discountTag

        // 2. Phone Number
        holder.tvPhone.text = item.phoneNumber

        // 3. Service & Booking Date
        holder.tvServiceName.text = item.serviceName
        holder.tvBookingDate.text = item.bookingDate

        // 4. Location
        holder.tvLocation.text = item.location
        holder.tvLocationArea.text = item.locationArea

        // 5. Feedback & Rating
        if (item.rating > 0f) {
            holder.layoutRating.visibility = View.VISIBLE
            holder.tvRating.text = String.format(Locale.US, "%.1f", item.rating)
        } else {
            holder.layoutRating.visibility = View.GONE
        }

        if (item.feedback.isNotEmpty()) {
            holder.tvFeedback.text = "“${item.feedback}”"
            holder.tvFeedback.setTextColor(Color.parseColor("#475569"))
        } else {
            holder.tvFeedback.text = "Awaiting review"
            holder.tvFeedback.setTextColor(Color.parseColor("#94A3B8"))
        }

        // 6. Payment Status
        val status = item.paymentStatus
        when {
            status.equals("Paid", ignoreCase = true) ||
            status.equals("Settled", ignoreCase = true) ||
            status.contains("Completed", ignoreCase = true) -> {
                holder.layoutPaymentStatus.setBackgroundResource(R.drawable.bg_status_completed_pill)
                holder.ivStatusIcon.setImageResource(R.drawable.ic_check_circle_green)
                holder.tvPaymentStatus.text = "Paid"
                holder.tvPaymentStatus.setTextColor(Color.parseColor("#15803D"))
            }
            status.contains("Pending", ignoreCase = true) -> {
                holder.layoutPaymentStatus.setBackgroundResource(R.drawable.bg_status_pending_pill)
                holder.ivStatusIcon.setImageResource(R.drawable.ic_clock_circle_amber)
                holder.tvPaymentStatus.text = "Pending"
                holder.tvPaymentStatus.setTextColor(Color.parseColor("#B45309"))
            }
            status.contains("Cancel", ignoreCase = true) -> {
                holder.layoutPaymentStatus.setBackgroundResource(R.drawable.bg_pill_masked_phone)
                holder.ivStatusIcon.setImageResource(R.drawable.ic_check_circle)
                holder.tvPaymentStatus.text = "Cancelled"
                holder.tvPaymentStatus.setTextColor(Color.parseColor("#DC2626"))
            }
            else -> {
                holder.layoutPaymentStatus.setBackgroundResource(R.drawable.bg_status_completed_pill)
                holder.ivStatusIcon.setImageResource(R.drawable.ic_check_circle_green)
                holder.tvPaymentStatus.text = status
                holder.tvPaymentStatus.setTextColor(Color.parseColor("#15803D"))
            }
        }

        holder.layoutPaymentStatus.setOnClickListener {
            onItemClick?.invoke(item, position)
        }

        holder.rowRootLayout.setOnClickListener {
            onItemClick?.invoke(item, position)
        }
    }

    override fun getItemCount(): Int = bookings.size

    fun updateList(newList: List<InfluencerBooking>) {
        bookings = newList.toMutableList()
        notifyDataSetChanged()
    }
}

