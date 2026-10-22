package com.company.jifiit.Hotel

import android.app.Dialog
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
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.company.jifiit.Constant
import com.company.jifiit.R
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BookingAdapter(
    private val bookingList: List<BookingModel>,
    private val onItemClick: (BookingModel) -> Unit,
    private val actionListener: BookingActionListener? = null
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    // ============================================================
    // ✅ CALLBACK INTERFACE — notifies Activity after success
    // ============================================================
    interface BookingActionListener {
        fun onBookingUpdated()
    }

    inner class BookingViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val shimmerLayout: ShimmerFrameLayout =
            itemView.findViewById(R.id.shimmerLayout)

        val tvBookingId: TextView =
            itemView.findViewById(R.id.tvBookingId)

        val tvInRoomTag: TextView =
            itemView.findViewById(R.id.tvInRoomTag)

        val tvStatus: TextView =
            itemView.findViewById(R.id.tvStatus)

        val ivExpert: ImageView =
            itemView.findViewById(R.id.ivExpertAvatar)

        val expertName: TextView =
            itemView.findViewById(R.id.tvExpertName)

        val totalDuration: TextView =
            itemView.findViewById(R.id.tvTotalDuration)

        val tvCustomerName: TextView =
            itemView.findViewById(R.id.tvCustomerName)

        val tvRoomInfo: TextView =
            itemView.findViewById(R.id.tvRoomInfo)

        val tvDate: TextView =
            itemView.findViewById(R.id.tvDate)

        val tvTime: TextView =
            itemView.findViewById(R.id.tvTime)

        val btnCall: MaterialButton =
            itemView.findViewById(R.id.btnCall)

        val btnHelp: MaterialButton =
            itemView.findViewById(R.id.btnHelp)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.hote_item_booking_card, parent, false)

        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: BookingViewHolder,
        position: Int
    ) {
        val booking = bookingList[position]
        val context = holder.itemView.context

        holder.tvBookingId.text =
            if (booking.bookingId.startsWith("#")) {
                booking.bookingId
            } else {
                "#${booking.bookingId}"
            }

        val tagLabel = getServiceTagLabel(booking.massage_types)

        if (tagLabel.isNullOrEmpty()) {
            holder.tvInRoomTag.visibility = View.GONE
        } else {
            holder.tvInRoomTag.visibility = View.VISIBLE
            holder.tvInRoomTag.text = tagLabel
        }

        val statusUpper = booking.status.trim().uppercase(Locale.ROOT)
        holder.tvStatus.text = statusUpper

        val statusColor = when (statusUpper) {
            "PENDING" -> Color.parseColor("#B45309")
            "COMPLETED" -> Color.parseColor("#15803D")
            "CONFIRMED" -> Color.parseColor("#1D4ED8")
            "CANCELED", "CANCELLED" -> Color.parseColor("#DC2626") // red
            else -> Color.parseColor("#64748B")
        }

        holder.tvStatus.setTextColor(statusColor)

        val statusBackground = when (statusUpper) {
            "PENDING" -> R.drawable.bg_status_pending_pill
            "COMPLETED" -> R.drawable.bg_status_completed_pill
            "CONFIRMED" -> R.drawable.bg_status_confirmed_pill
            "CANCELED", "CANCELLED" -> R.drawable.bg_status_canceled_pill
            else -> R.drawable.bg_service_tag_pill
        }

        holder.tvStatus.setBackgroundResource(statusBackground)

        val statusIcon = when (statusUpper) {
            "PENDING" -> R.drawable.ic_pending
            "COMPLETED", "CONFIRMED" ->
                R.drawable.baseline_check_circle_outline_24
            else -> null
        }

        val icon = statusIcon?.let {
            ContextCompat.getDrawable(context, it)?.mutate()
        }

        icon?.setBounds(
            0,
            0,
            dp(context, 14),
            dp(context, 14)
        )
        icon?.setTint(statusColor)

        holder.tvStatus.setCompoundDrawables(
            icon, null, null, null
        )

        holder.tvCustomerName.text = booking.customerName
        holder.expertName.text = booking.expertName
        holder.totalDuration.text = "${booking.duration} mins"

        val rawRoom = booking.room_number.trim()
        val roomNumber = if (
            rawRoom.startsWith("Room", ignoreCase = true)
        ) {
            rawRoom.replace(Regex("(?i)^Room\\s*"), "")
        } else {
            rawRoom
        }

        val numPart = if (
            roomNumber.isNotEmpty() &&
            !roomNumber.equals("N/A", ignoreCase = true)
        ) {
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

        holder.tvTime.text = if (
            booking.time.isNotBlank() && booking.time != "N/A"
        ) {
            booking.time
        } else {
            "07:00 PM"
        }

        holder.tvDate.text = booking.date

        holder.itemView.setOnClickListener {
            onItemClick(booking)
        }

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

        val isCancelled = statusUpper == "CANCELED" || statusUpper == "CANCELLED"

        if (isCancelled) {
            holder.btnCall.visibility = View.GONE
            holder.btnHelp.visibility = View.GONE
        } else {
            holder.btnCall.visibility = View.VISIBLE
            holder.btnHelp.visibility = View.VISIBLE
        }

        holder.btnCall.setOnClickListener {
            val number = booking.client_number.trim()

            if (
                number.isEmpty() ||
                number.equals("N/A", ignoreCase = true)
            ) {
                Toast.makeText(
                    context,
                    "Phone number not available",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            try {
                context.startActivity(
                    Intent(
                        Intent.ACTION_DIAL,
                        Uri.parse("tel:$number")
                    )
                )
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Unable to open dialer",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        holder.btnHelp.setOnClickListener {
            showHelpSheet(booking, context)
        }
    }

    // ============================================================
    // HELP BOTTOM SHEET
    // ============================================================
    private fun showHelpSheet(
        booking: BookingModel,
        context: Context
    ) {
        val dialog = BottomSheetDialog(context)

        val sheetView = LayoutInflater.from(context)
            .inflate(R.layout.bottom_sheet_help, null)

        dialog.setContentView(sheetView)

        val btnClose = sheetView.findViewById<ImageView>(R.id.btnCloseHelp)
        val optionReschedule =
            sheetView.findViewById<LinearLayout>(R.id.optionReschedule)
        val optionCancel =
            sheetView.findViewById<LinearLayout>(R.id.optionCancel)

        val tvDateTime = sheetView.findViewById<TextView>(R.id.tvHelpDateTime)
        val tvTherapist = sheetView.findViewById<TextView>(R.id.tvHelpTherapist)

        val timeText = if (
            booking.time.isNotBlank() && booking.time != "N/A"
        ) booking.time else "07:00 PM"

        tvDateTime.text = "${booking.date} at $timeText"
        tvTherapist.text = booking.expertName

        btnClose.setOnClickListener { dialog.dismiss() }

        optionReschedule.setOnClickListener {
            dialog.dismiss()
            onReschedule(booking, context)
        }

        optionCancel.setOnClickListener {
            dialog.dismiss()
            showCancelSheet(booking, context)
        }

        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )

            bottomSheet?.let { sheet ->
                sheet.setBackgroundColor(Color.TRANSPARENT)

                BottomSheetBehavior.from(sheet).apply {
                    state = BottomSheetBehavior.STATE_EXPANDED
                    skipCollapsed = true
                }
            }
        }

        dialog.show()
    }

    private fun showCancelSheet(
        booking: BookingModel,
        context: Context
    ) {
        val dialog = BottomSheetDialog(context)
        val sheetView = LayoutInflater.from(context)
            .inflate(R.layout.bottom_sheet_cancel, null)
        dialog.setContentView(sheetView)

        val btnClose = sheetView.findViewById<ImageView>(R.id.btnCloseCancel)
        val btnKeepBooking = sheetView.findViewById<TextView>(R.id.btnKeepBooking)
        val btnYesCancel = sheetView.findViewById<TextView>(R.id.btnYesCancel)
        val tvCancelDateTime = sheetView.findViewById<TextView>(R.id.tvCancelDateTime)

        tvCancelDateTime.text = "${booking.date} at ${booking.time}"

        btnClose?.setOnClickListener { dialog.dismiss() }
        btnKeepBooking.setOnClickListener { dialog.dismiss() }

        btnYesCancel.setOnClickListener {
            cancelBooking(booking, context, btnYesCancel, dialog)
        }

        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheet?.let { sheet ->
                sheet.setBackgroundColor(Color.TRANSPARENT)
                BottomSheetBehavior.from(sheet).apply {
                    state = BottomSheetBehavior.STATE_EXPANDED
                    skipCollapsed = true
                }
            }
        }

        dialog.show()
    }


    private fun cancelBooking(
        booking: BookingModel,
        context: Context,
        btnYesCancel: TextView,
        dialog: BottomSheetDialog
    ) {
        val originalText = btnYesCancel.text

        btnYesCancel.isEnabled = false
        btnYesCancel.text = "Cancelling..."

        val jsonBody = JSONObject().apply {
            put("booking_id", booking.bookingId.toString()) // ensure string
        }
        Log.d("cancelBooking", "JSON body: $jsonBody")

        val request = object : JsonObjectRequest(
            Method.POST,
            Constant.CANCEL_HOTEL_BOOKING,
            jsonBody,
            { response ->
                val status = response.optBoolean("status", false)
                val message = response.optString("message", "Something went wrong")

                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                if (status) {
                    dialog.dismiss()
                    actionListener?.onBookingUpdated()
                } else {
                    btnYesCancel.isEnabled = true
                    btnYesCancel.text = originalText
                }
            },
            { error ->
                btnYesCancel.isEnabled = true
                btnYesCancel.text = originalText

                val msg = when (error) {
                    is com.android.volley.TimeoutError -> "Request timed out. Please try again."
                    is com.android.volley.NoConnectionError -> "No internet connection."
                    is com.android.volley.ServerError -> "Server error. Please try again later."
                    is com.android.volley.AuthFailureError -> "Authentication failed."
                    else -> error.message ?: "Network error. Please try again."
                }
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json; charset=utf-8"
                // If you use auth token, add it here:
                // headers["Authorization"] = "Bearer $token"
                return headers
            }
        }

        request.setShouldCache(false)
        // Reuse a singleton queue instead of creating one each time
        VolleySingleton.getInstance(context).add(request)
    }

    object VolleySingleton {
        private var requestQueue: RequestQueue? = null

        fun getInstance(context: Context): RequestQueue {
            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(context.applicationContext)
            }
            return requestQueue!!
        }
    }

    // ============================================================
    // RESCHEDULE BOTTOM SHEET
    // ============================================================
    private fun onReschedule(
        booking: BookingModel,
        context: Context
    ) {
        val dialog = BottomSheetDialog(context)

        val sheetView = LayoutInflater.from(context)
            .inflate(R.layout.bottom_sheet_reschedule, null)

        dialog.setContentView(sheetView)

        val dateContainer =
            sheetView.findViewById<LinearLayout>(R.id.dateContainer)

        val timeContainer =
            sheetView.findViewById<GridLayout>(R.id.timeContainer)

        val btnClose =
            sheetView.findViewById<TextView>(R.id.btnCloseReschedule)

        val btnBack =
            sheetView.findViewById<TextView>(R.id.btnBackReschedule)

        val btnConfirm =
            sheetView.findViewById<TextView>(R.id.btnConfirmReschedule)

        val density = context.resources.displayMetrics.density

        fun dp(value: Int): Int = (value * density).toInt()

        fun makeBackground(
            fillColor: Int,
            strokeColor: Int,
            radiusDp: Int
        ): android.graphics.drawable.GradientDrawable {
            return android.graphics.drawable.GradientDrawable().apply {
                setColor(fillColor)
                cornerRadius = dp(radiusDp).toFloat()
                setStroke(dp(1), strokeColor)
            }
        }

        val blue = Color.parseColor("#0875D1")
        val lightBlue = Color.parseColor("#F1F8FF")
        val borderBlue = Color.parseColor("#C7E0FA")
        val textBlue = Color.parseColor("#0069BE")

        var selectedDate = ""
        var selectedTime = ""

        val dateViews = mutableListOf<TextView>()
        val timeViews = mutableListOf<TextView>()

        val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        )

        val weekdayFormat = SimpleDateFormat(
            "EEE",
            Locale.getDefault()
        )

        val numberFormat = SimpleDateFormat(
            "dd",
            Locale.getDefault()
        )

        fun styleOption(
            textView: TextView,
            selected: Boolean,
            radiusDp: Int
        ) {
            textView.background = if (selected) {
                makeBackground(blue, blue, radiusDp)
            } else {
                makeBackground(lightBlue, borderBlue, radiusDp)
            }

            textView.setTextColor(
                if (selected) Color.WHITE else textBlue
            )

            textView.setTypeface(
                null,
                if (selected) Typeface.BOLD else Typeface.NORMAL
            )
        }

        fun refreshDates() {
            dateViews.forEachIndexed { index, textView ->
                val calendar = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, index)
                }

                val dateValue = dateFormat.format(calendar.time)

                styleOption(
                    textView,
                    dateValue == selectedDate,
                    12
                )
            }
        }

        fun refreshTimes() {
            timeViews.forEach { textView ->
                styleOption(
                    textView,
                    textView.tag == selectedTime,
                    8
                )
            }
        }

        val therapistId = booking.ExpertId

        val apiUrl = Constant.HOTEL_FETCH_TIME_SLOT

        val requestQueue = Volley.newRequestQueue(context)

        fun fetchTimeSlots(dateValue: String, duration: String, bookingId: String) {
            selectedTime = ""
            timeViews.clear()
            timeContainer.removeAllViews()

            btnConfirm.isEnabled = false
            btnConfirm.alpha = 0.5f

            val loadingView = TextView(context).apply {
                text = "Loading available slots..."
                textSize = 12f
                gravity = Gravity.CENTER
                setTextColor(Color.GRAY)

                layoutParams = GridLayout.LayoutParams().apply {
                    width = GridLayout.LayoutParams.MATCH_PARENT
                    height = dp(45)
                    columnSpec = GridLayout.spec(0, 2)
                }
            }

            timeContainer.addView(loadingView)

            val requestUrl = Uri.parse(apiUrl)
                .buildUpon()
                .appendQueryParameter("therapistId", therapistId)
                .appendQueryParameter("date", dateValue)
                .appendQueryParameter("duration", duration)
                .appendQueryParameter("BookingId", bookingId)
                .build()
                .toString()

            val request = JsonObjectRequest(
                Request.Method.GET,
                requestUrl,
                null,
                { response ->
                    timeContainer.removeAllViews()
                    timeViews.clear()
                    selectedTime = ""

                    val slots = mutableListOf<String>()
                    val slotsArray = response.optJSONArray("slots")

                    if (slotsArray != null) {
                        for (i in 0 until slotsArray.length()) {
                            val slot = slotsArray.optString(i).trim()

                            if (slot.isNotEmpty()) {
                                slots.add(slot)
                            }
                        }
                    }

                    if (slots.isEmpty()) {
                        val emptyView = TextView(context).apply {
                            text = "No available slots for this date"
                            textSize = 12f
                            gravity = Gravity.CENTER
                            setTextColor(Color.GRAY)

                            layoutParams = GridLayout.LayoutParams().apply {
                                width = GridLayout.LayoutParams.MATCH_PARENT
                                height = dp(45)
                                columnSpec = GridLayout.spec(0, 2)
                            }
                        }

                        timeContainer.addView(emptyView)
                        return@JsonObjectRequest
                    }

                    slots.forEach { slot ->
                        val timeView = TextView(context).apply {
                            text = "◷  $slot"
                            tag = slot
                            textSize = 10f
                            gravity = Gravity.CENTER

                            setPadding(
                                dp(3),
                                dp(6),
                                dp(3),
                                dp(6)
                            )

                            layoutParams = GridLayout.LayoutParams().apply {
                                width = 0
                                height = dp(36)

                                columnSpec = GridLayout.spec(
                                    GridLayout.UNDEFINED,
                                    1f
                                )

                                setMargins(
                                    dp(3),
                                    dp(4),
                                    dp(3),
                                    dp(4)
                                )
                            }

                            setOnClickListener {
                                selectedTime = slot
                                refreshTimes()

                                btnConfirm.isEnabled = true
                                btnConfirm.alpha = 1f
                            }
                        }

                        timeViews.add(timeView)
                        timeContainer.addView(timeView)
                    }

                    refreshTimes()
                },
                { error ->
                    timeContainer.removeAllViews()
                    timeViews.clear()
                    selectedTime = ""

                    val errorView = TextView(context).apply {
                        text = "Unable to load slots. Please try again."
                        textSize = 12f
                        gravity = Gravity.CENTER
                        setTextColor(Color.RED)
                    }

                    timeContainer.addView(errorView)

                    btnConfirm.isEnabled = false
                    btnConfirm.alpha = 0.5f

                    Toast.makeText(
                        context,
                        "Failed to fetch available slots",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )

            requestQueue.add(request)
        }

        repeat(7) { index ->
            val calendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, index)
            }

            val dateValue = dateFormat.format(calendar.time)

            val dayLabel = when (index) {
                0 -> "Today"
                1 -> "Tomorrow"
                else -> weekdayFormat.format(calendar.time)
            }

            val dateNumber = numberFormat.format(calendar.time)

            val dateView = TextView(context).apply {
                text = "$dayLabel\n$dateNumber"
                textSize = 10f
                gravity = Gravity.CENTER

                setPadding(
                    dp(5),
                    dp(7),
                    dp(5),
                    dp(7)
                )

                layoutParams = LinearLayout.LayoutParams(
                    dp(58),
                    dp(54)
                ).apply {
                    marginEnd = dp(6)
                }

                setOnClickListener {
                    if (selectedDate != dateValue) {
                        selectedDate = dateValue
                        refreshDates()
                        fetchTimeSlots(
                            selectedDate,
                            booking.duration,
                            booking.bookingId
                        )
                    }
                }
            }

            dateViews.add(dateView)
            dateContainer.addView(dateView)
        }

        selectedDate = dateFormat.format(Calendar.getInstance().time)
        refreshDates()

        fetchTimeSlots(
            selectedDate,
            booking.duration,
            booking.bookingId
        )

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        btnBack.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            if (selectedDate.isBlank() || selectedTime.isBlank()) {
                Toast.makeText(
                    context,
                    "Please select an available date and time",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            rescheduleBooking(
                context = context,
                dialog = dialog,
                btnConfirm = btnConfirm,
                bookingId = booking.bookingId,
                newDate = selectedDate,
                newTime = selectedTime,
                duration = booking.duration,
                endTime = booking.EndTime,
                therapistId = therapistId
            )
        }

        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )

            bottomSheet?.let { sheet ->
                sheet.setBackgroundColor(Color.TRANSPARENT)

                BottomSheetBehavior.from(sheet).apply {
                    state = BottomSheetBehavior.STATE_EXPANDED
                    skipCollapsed = true
                }
            }
        }

        dialog.show()
    }

    private fun getServiceTagLabel(
        massageTypes: String?
    ): String? {
        return when (massageTypes?.trim()?.lowercase(Locale.ROOT)) {
            "massage at hotel" -> "In-Room Massage"
            "yoga at hotel" -> "Yoga"
            "personal trainer at hotel" -> "Personal Trainer"
            "ayurveda at hotel" -> "Ayurveda"
            else -> null
        }
    }

    private fun rescheduleBooking(
        context: Context,
        dialog: Dialog,
        btnConfirm: TextView,
        bookingId: Any,
        newDate: String,
        newTime: String,
        duration: Any,
        endTime: String,
        therapistId: Any
    ) {
        val url = Constant.RESCHEDULE_HOTEL_BOOKING

        if (bookingId.toString().isBlank() || therapistId.toString().isBlank()) {
            Toast.makeText(context, "Missing booking or therapist id", Toast.LENGTH_SHORT).show()
            return
        }

        btnConfirm.isEnabled = false
        btnConfirm.text = "Rescheduling..."

        val params = JSONObject().apply {
            put("booking_id", bookingId.toString())
            put("booking_date", newDate)
            put("booking_time", newTime)
            put("end_time", endTime)
            put("duration", duration.toString())
            put("therapist_id", therapistId.toString())
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            url,
            params,
            { response ->
                // ✅ Handle multiple possible success keys
                val statusStr = response.optString("status", "").lowercase()
                val statusCap = response.optString("Status", "").lowercase()
                val successBool = response.optBoolean("success", false)

                val isSuccess =
                    statusStr == "success" ||
                            statusCap == "success" ||
                            successBool

                val message = response.optString("message",
                    response.optString("Message", "Something went wrong"))

                (context as? android.app.Activity)?.runOnUiThread {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                    if (isSuccess) {
                        if (dialog.isShowing) dialog.dismiss()
                        actionListener?.onBookingUpdated()
                    } else {
                        btnConfirm.isEnabled = true
                        btnConfirm.text = "Confirm"
                    }
                }
            },
            { error ->
                (context as? android.app.Activity)?.runOnUiThread {
                    btnConfirm.isEnabled = true
                    btnConfirm.text = "Confirm"

                    val message = if (error.networkResponse != null) {
                        val statusCode = error.networkResponse.statusCode
                        val body = try {
                            String(error.networkResponse.data ?: ByteArray(0))
                        } catch (e: Exception) { "" }
                        "HTTP $statusCode: $body"
                    } else {
                        error.message ?: "Network error. Please try again."
                    }

                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            }
        )

        Volley.newRequestQueue(context).add(request)
    }

    override fun getItemCount(): Int = bookingList.size

    private fun dp(context: Context, value: Int): Int {
        return (value * context.resources.displayMetrics.density).toInt()
    }
}