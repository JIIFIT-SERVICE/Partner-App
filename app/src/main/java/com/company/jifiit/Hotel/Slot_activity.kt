package com.company.jifiit.Hotel

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import com.company.jifiit.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.ceil

class Slot_activity : AppCompatActivity() {

    // =====================================================
    // GUEST DATA
    // =====================================================

    private lateinit var guestName: String
    private lateinit var guestMobile: String

    // =====================================================
    // ROOM DATA
    // =====================================================

    private lateinit var roomId: String
    private lateinit var roomNumber: String
    private lateinit var floorWing: String

    // =====================================================
    // THERAPIST
    // =====================================================

    private lateinit var expertType: String

    // =====================================================
    // CHECKBOX PREFERENCES
    // =====================================================

    private var ringDoorbell: Boolean = false
    private var doNotCall: Boolean = false
    private var callConcierge: Boolean = false

    // =====================================================
    // SPECIAL NOTE
    // =====================================================

    private lateinit var specialNote: String

    // =====================================================
    // SELECTED SERVICE
    // =====================================================

    private lateinit var selectedService: String

    // =====================================================
    // DATE / TIME UI
    // =====================================================

    private lateinit var dateContainer: LinearLayout
    private lateinit var timeGrid: LinearLayout
    private lateinit var timeScrollView: NestedScrollView
    private lateinit var btnContinue: MaterialButton
    private lateinit var btnBack: ImageView
    private lateinit var tvSelectedSlot: TextView

    // =====================================================
    // SELECTED DATE
    // =====================================================

    private var selectedDateCalendar: Calendar? = null

    private var selectedDateString: String = ""
    private var selectedTimeString: String = ""

    // =====================================================
    // SELECTED UI
    // =====================================================

    private var selectedDateView: MaterialCardView? = null
    private var selectedTimeView: MaterialCardView? = null


    // =====================================================
    // CONSTANTS
    // =====================================================

    private val START_HOUR = 8
    private val START_MINUTE = 0

    private val END_HOUR = 21
    private val END_MINUTE = 30

    private val SLOT_INTERVAL_MINUTES = 15

    private var voiceNotePath: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        WindowCompat.getInsetsController(window,window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        setContentView(R.layout.activity_slot)

        // =====================================================
        // WINDOW INSETS
        // =====================================================

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v, insets ->

            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime =
                insets.getInsets(WindowInsetsCompat.Type.ime())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                maxOf(systemBars.bottom, ime.bottom)
            )

            insets
        }


        // =====================================================
        // VIEW INITIALIZATION
        // =====================================================

        btnBack = findViewById(R.id.btnBack)

        dateContainer = findViewById(R.id.dateContainer)
        timeGrid = findViewById(R.id.timeGrid)
        timeScrollView = findViewById(R.id.timeScrollView)
        btnContinue = findViewById(R.id.btnContinue)
        tvSelectedSlot = findViewById(R.id.tvSelectedSlot)


        // =====================================================
        // BACK BUTTON
        // =====================================================

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            overridePendingTransition(
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
        }


        // =====================================================
        // GET DATA FROM PREVIOUS ACTIVITY
        // =====================================================

        // Guest
        guestName =
            intent.getStringExtra("guest_name").orEmpty()

        guestMobile =
            intent.getStringExtra("guest_mobile").orEmpty()


        // Room
        roomId =
            intent.getStringExtra("room_id").orEmpty()

        roomNumber =
            intent.getStringExtra("room_number").orEmpty()

        floorWing =
            intent.getStringExtra("floor_wing").orEmpty()


        // Therapist
        expertType =
            intent.getStringExtra("expert_type").orEmpty()


        // Checkbox preferences
        ringDoorbell =
            intent.getBooleanExtra("ring_doorbell", false)

        doNotCall =
            intent.getBooleanExtra("do_not_call", false)

        callConcierge =
            intent.getBooleanExtra("call_concierge", false)


        // Special note
        specialNote =
            intent.getStringExtra("special_note").orEmpty()

        voiceNotePath  = intent.getStringExtra("voice_note_path").orEmpty()
        // Selected service
        selectedService =
            intent.getStringExtra("SELECTED_SERVICE").orEmpty()


        // =====================================================
        // GENERATE DATES
        // =====================================================

        generateNextSevenDays()


        // =====================================================
        // CONTINUE
        // =====================================================

        btnContinue.setOnClickListener {

            if (selectedDateString.isEmpty()) {

                Toast.makeText(
                    this,
                    "Please select a date",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (selectedTimeString.isEmpty()) {

                Toast.makeText(
                    this,
                    "Please select a time slot",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }




            // =============================================
            // NEXT ACTIVITY
            // =============================================


            val intent = Intent(this, Expert_activity::class.java)

            intent.putExtra("guest_name", guestName)
            intent.putExtra("guest_mobile", guestMobile)
            intent.putExtra("room_id", roomId)
            intent.putExtra("room_number", roomNumber)
            intent.putExtra("floor_wing", floorWing)
            intent.putExtra("client_gender", expertType)
            intent.putExtra("ring_doorbell", ringDoorbell)
            intent.putExtra("do_not_call", doNotCall)
            intent.putExtra("call_concierge", callConcierge)
            intent.putExtra("special_note", specialNote)
            intent.putExtra("voice_note_path", voiceNotePath)
            intent.putExtra("SELECTED_SERVICE", selectedService)
            intent.putExtra("booking_date", selectedDateString)
            intent.putExtra("booking_time", selectedTimeString)

            startActivity(intent)

        }
    }


    // =====================================================
    // GENERATE NEXT 7 DAYS
    // =====================================================

    private fun generateNextSevenDays() {

        dateContainer.removeAllViews()

        val today = Calendar.getInstance()

        for (i in 0..6) {

            val date = today.clone() as Calendar

            date.add(Calendar.DAY_OF_YEAR, i)

            val dateCard = createDateCard(date, i)

            dateContainer.addView(dateCard)

            // Automatically select today
            if (i == 0) {
                selectDate(date, dateCard)
            }
        }
    }


    // =====================================================
    // CREATE DATE CARD
    // =====================================================

    private fun createDateCard(
        calendar: Calendar,
        position: Int
    ): MaterialCardView {

        // Defensive copy so later mutation doesn't affect this card
        val cardCalendar = calendar.clone() as Calendar

        val card = MaterialCardView(this)

        val cardParams =
            LinearLayout.LayoutParams(
                82.dpToPx(),
                78.dpToPx()
            )

        cardParams.setMargins(0, 0, 8.dpToPx(), 0)

        card.layoutParams = cardParams

        card.radius = 14.dpToPx().toFloat()
        card.cardElevation = 0f
        card.strokeWidth = 1.dpToPx()
        card.isClickable = true
        card.isFocusable = true


        // =================================================
        // DATE CONTENT
        // =================================================

        val container = LinearLayout(this)

        container.layoutParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )

        container.orientation = LinearLayout.VERTICAL
        container.gravity = Gravity.CENTER


        // =================================================
        // TOP LABEL
        // =================================================

        val topText = TextView(this)

        topText.text =
            when (position) {
                0 -> "TODAY"
                1 -> "TOMORROW"
                else -> SimpleDateFormat(
                    "EEE",
                    Locale.getDefault()
                ).format(cardCalendar.time).uppercase()
            }

        topText.textSize = 8f
        topText.typeface =
            Typeface.create("sans", Typeface.BOLD)
        topText.gravity = Gravity.CENTER


        // =================================================
        // DATE NUMBER
        // =================================================

        val dateText = TextView(this)

        dateText.text =
            SimpleDateFormat(
                "dd",
                Locale.getDefault()
            ).format(cardCalendar.time)

        dateText.textSize = 16f
        dateText.typeface =
            Typeface.create("sans", Typeface.BOLD)
        dateText.gravity = Gravity.CENTER


        // =================================================
        // MONTH
        // =================================================

        val monthText = TextView(this)

        monthText.text =
            SimpleDateFormat(
                "MMM",
                Locale.getDefault()
            ).format(cardCalendar.time)

        monthText.textSize = 9f
        monthText.gravity = Gravity.CENTER


        container.addView(topText)
        container.addView(dateText)
        container.addView(monthText)

        card.addView(container)


        // =================================================
        // INITIAL UI
        // =================================================

        setDateCardUnselected(
            card,
            topText,
            dateText,
            monthText
        )


        // =================================================
        // CLICK
        // =================================================

        card.setOnClickListener {
            selectDate(cardCalendar, card)
        }


        return card
    }


    // =====================================================
    // SELECT DATE
    // =====================================================

    private fun selectDate(
        calendar: Calendar,
        card: MaterialCardView
    ) {

        // -----------------------------------------------
        // Reset previous selected date
        // -----------------------------------------------

        selectedDateView?.let { previousCard ->
            resetDateCard(previousCard)
        }


        // -----------------------------------------------
        // Current selected (clone so shared Calendar
        // doesn't leak into other cards)
        // -----------------------------------------------

        selectedDateView = card

        selectedDateCalendar =
            calendar.clone() as Calendar


        selectedDateString =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).format(calendar.time)


        // -----------------------------------------------
        // Selected UI
        // -----------------------------------------------

        setDateCardSelected(card)


        // -----------------------------------------------
        // Generate times
        // -----------------------------------------------

        generateTimeSlots(calendar)

        updateFooterSlotText()
    }


    // =====================================================
    // DATE SELECTED UI
    // =====================================================

    private fun setDateCardSelected(
        card: MaterialCardView
    ) {

        card.setCardBackgroundColor(
            ContextCompat.getColor(this, R.color.Button_Blue)
        )

        card.strokeColor =
            ContextCompat.getColor(this, R.color.Button_Blue)

        card.strokeWidth = 2.dpToPx()


        val container = card.getChildAt(0)

        if (container is LinearLayout) {

            for (i in 0 until container.childCount) {

                val child = container.getChildAt(i)

                if (child is TextView) {
                    child.setTextColor(
                        ContextCompat.getColor(this, R.color.white)
                    )
                }
            }
        }
    }


    // =====================================================
    // DATE UNSELECTED
    // =====================================================

    private fun setDateCardUnselected(
        card: MaterialCardView,
        topText: TextView,
        dateText: TextView,
        monthText: TextView
    ) {

        card.setCardBackgroundColor(
            ContextCompat.getColor(this, R.color.white)
        )

        card.strokeColor =
            ContextCompat.getColor(this, R.color.light_Gray)

        card.strokeWidth = 1.dpToPx()

        topText.setTextColor(
            ContextCompat.getColor(this, R.color.Gray)
        )

        dateText.setTextColor(
            ContextCompat.getColor(this, R.color.Text_Blue)
        )

        monthText.setTextColor(
            ContextCompat.getColor(this, R.color.Gray)
        )
    }


    // =====================================================
    // RESET DATE CARD
    // =====================================================

    private fun resetDateCard(
        card: MaterialCardView
    ) {

        card.setCardBackgroundColor(
            ContextCompat.getColor(this, R.color.white)
        )

        card.strokeColor =
            ContextCompat.getColor(this, R.color.light_Gray)

        card.strokeWidth = 1.dpToPx()

        val container = card.getChildAt(0)

        if (container is LinearLayout) {

            for (i in 0 until container.childCount) {

                val child = container.getChildAt(i)

                if (child is TextView) {

                    if (i == 1) {
                        child.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.Text_Blue
                            )
                        )
                    } else {
                        child.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.Gray
                            )
                        )
                    }
                }
            }
        }
    }


    // =====================================================
    // GENERATE TIME SLOTS
    // =====================================================

    private fun generateTimeSlots(
        selectedDate: Calendar
    ) {

        timeGrid.removeAllViews()

        selectedTimeView = null
        selectedTimeString = ""


        // =================================================
        // START TIME
        // =================================================

        val startCalendar =
            selectedDate.clone() as Calendar

        startCalendar.set(Calendar.HOUR_OF_DAY, START_HOUR)
        startCalendar.set(Calendar.MINUTE, START_MINUTE)
        startCalendar.set(Calendar.SECOND, 0)
        startCalendar.set(Calendar.MILLISECOND, 0)


        // =================================================
        // TODAY LOGIC
        // =================================================

        val today = Calendar.getInstance()

        val isToday =
            selectedDate.get(Calendar.YEAR) ==
                    today.get(Calendar.YEAR) &&
                    selectedDate.get(Calendar.DAY_OF_YEAR) ==
                    today.get(Calendar.DAY_OF_YEAR)


        if (isToday) {

            val minimumTime = Calendar.getInstance()

            // Current time + 15 minutes
            minimumTime.add(Calendar.MINUTE, 15)


            // Round UP to next 15-minute slot
            val minute = minimumTime.get(Calendar.MINUTE)

            val roundedMinute =
                ceil(minute / SLOT_INTERVAL_MINUTES.toDouble())
                    .toInt() * SLOT_INTERVAL_MINUTES

            minimumTime.set(Calendar.MINUTE, 0)
            minimumTime.set(Calendar.SECOND, 0)
            minimumTime.set(Calendar.MILLISECOND, 0)

            minimumTime.add(Calendar.MINUTE, roundedMinute)


            if (minimumTime.after(startCalendar)) {
                startCalendar.timeInMillis =
                    minimumTime.timeInMillis
            }
        }


        // =================================================
        // END TIME
        // =================================================

        val endCalendar =
            selectedDate.clone() as Calendar

        endCalendar.set(Calendar.HOUR_OF_DAY, END_HOUR)
        endCalendar.set(Calendar.MINUTE, END_MINUTE)
        endCalendar.set(Calendar.SECOND, 0)
        endCalendar.set(Calendar.MILLISECOND, 0)


        // =================================================
        // NO SLOT AVAILABLE
        // =================================================

        if (startCalendar.after(endCalendar)) {

            val noSlotText = TextView(this)

            noSlotText.layoutParams =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

            noSlotText.gravity = Gravity.CENTER

            noSlotText.setPadding(
                16.dpToPx(),
                25.dpToPx(),
                16.dpToPx(),
                25.dpToPx()
            )

            noSlotText.text = "No slots available for this date."
            noSlotText.textSize = 12f
            noSlotText.setTextColor(
                ContextCompat.getColor(this, R.color.Gray)
            )

            timeGrid.addView(noSlotText)

            updateFooterSlotText()

            return
        }


        // =================================================
        // GENERATE EVERY 15 MINUTES (3 columns per row)
        //
        // IMPORTANT: Build a fresh Calendar per slot so the
        // click listener captures the slot's OWN time — not
        // the mutated shared `current` variable.
        // =================================================

        val totalSlots =
            ((endCalendar.timeInMillis - startCalendar.timeInMillis) /
                    (SLOT_INTERVAL_MINUTES * 60_000L)).toInt() + 1

        var row: LinearLayout? = null
        var columnIndex = 0

        for (slotIndex in 0 until totalSlots) {

            // Create an independent Calendar for this slot
            val slotCalendar =
                startCalendar.clone() as Calendar

            slotCalendar.add(
                Calendar.MINUTE,
                slotIndex * SLOT_INTERVAL_MINUTES
            )


            if (columnIndex == 0) {

                row = LinearLayout(this)

                row.layoutParams =
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )

                row.orientation = LinearLayout.HORIZONTAL
                row.gravity = Gravity.CENTER_VERTICAL

                timeGrid.addView(row)
            }


            val timeCard = createTimeSlot(slotCalendar)

            row?.addView(timeCard)


            columnIndex++

            if (columnIndex == 3) {
                columnIndex = 0
            }
        }


        // =================================================
        // FILL EMPTY CELLS
        // =================================================

        if (columnIndex != 0) {

            while (columnIndex < 3) {

                val emptyView = View(this)

                emptyView.layoutParams =
                    LinearLayout.LayoutParams(
                        0,
                        58.dpToPx(),
                        1f
                    ).apply {

                        setMargins(
                            4.dpToPx(),
                            4.dpToPx(),
                            4.dpToPx(),
                            4.dpToPx()
                        )
                    }

                row?.addView(emptyView)

                columnIndex++
            }
        }


        // =================================================
        // RESET SCROLL POSITION
        // =================================================

        timeScrollView.post {
            timeScrollView.scrollTo(0, 0)
        }
    }


    // =====================================================
    // CREATE TIME SLOT CARD
    // =====================================================

    private fun createTimeSlot(
        calendar: Calendar
    ): MaterialCardView {

        // =================================================
        // Defensive copy — THIS IS THE FIX
        //
        // Without this, every slot's click listener references
        // the same mutable Calendar and always reports the
        // last slot's time (e.g. 9:45 PM).
        // =================================================

        val slotCalendar = calendar.clone() as Calendar


        val card = MaterialCardView(this)

        val params =
            LinearLayout.LayoutParams(
                0,
                58.dpToPx(),
                1f
            )

        params.setMargins(
            4.dpToPx(),
            4.dpToPx(),
            4.dpToPx(),
            4.dpToPx()
        )

        card.layoutParams = params

        card.radius = 10.dpToPx().toFloat()
        card.cardElevation = 0f

        card.setCardBackgroundColor(
            ContextCompat.getColor(this, R.color.white)
        )

        card.strokeWidth = 1.dpToPx()

        card.strokeColor =
            ContextCompat.getColor(this, R.color.light_Gray)

        card.isClickable = true
        card.isFocusable = true


        // =================================================
        // TIME TEXT
        // =================================================

        val timeText = TextView(this)

        timeText.layoutParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )

        timeText.gravity = Gravity.CENTER

        timeText.text =
            SimpleDateFormat(
                "hh:mm\na",
                Locale.getDefault()
            ).format(slotCalendar.time)   // ← use copy

        timeText.textSize = 10.5f
        timeText.typeface =
            Typeface.create("sans", Typeface.BOLD)

        timeText.setLineSpacing(0f, 1f)

        timeText.setTextColor(
            ContextCompat.getColor(this, R.color.Text_Blue)
        )


        card.addView(timeText)


        // =================================================
        // CLICK
        // =================================================

        card.setOnClickListener {

            // ---------------------------------------------
            // Reset previous selected slot
            // ---------------------------------------------

            selectedTimeView?.let { previous ->

                previous.setCardBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.white
                    )
                )

                previous.strokeColor =
                    ContextCompat.getColor(
                        this,
                        R.color.light_Gray
                    )

                previous.strokeWidth = 1.dpToPx()


                val previousText = previous.getChildAt(0)

                if (previousText is TextView) {

                    previousText.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.Text_Blue
                        )
                    )
                }
            }


            // ---------------------------------------------
            // Current selected — use slotCalendar (copy)
            // ---------------------------------------------

            selectedTimeView = card

            selectedTimeString =
                SimpleDateFormat(
                    "HH:mm",
                    Locale.getDefault()
                ).format(slotCalendar.time)   // ← THE FIX


            // ---------------------------------------------
            // Selected UI
            // ---------------------------------------------

            card.setCardBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.BackgroundBlue
                )
            )

            card.strokeColor =
                ContextCompat.getColor(
                    this,
                    R.color.Button_Blue
                )

            card.strokeWidth = 2.dpToPx()


            timeText.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.Button_Blue
                )
            )


            // ---------------------------------------------
            // Update footer
            // ---------------------------------------------

            updateFooterSlotText()
        }


        return card
    }


    // =====================================================
    // UPDATE FOOTER SLOT TEXT
    // =====================================================

    private fun updateFooterSlotText() {

        val datePart =
            if (selectedDateString.isNotEmpty()) {

                try {

                    val input =
                        SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.getDefault()
                        )

                    val output =
                        SimpleDateFormat(
                            "EEE, dd MMM",
                            Locale.getDefault()
                        )

                    val parsed = input.parse(selectedDateString)

                    if (parsed != null) {
                        output.format(parsed)
                    } else {
                        selectedDateString
                    }

                } catch (e: Exception) {
                    selectedDateString
                }

            } else {
                ""
            }


        val timePart =
            if (selectedTimeString.isNotEmpty()) {

                try {

                    val input =
                        SimpleDateFormat(
                            "HH:mm",
                            Locale.getDefault()
                        )

                    val output =
                        SimpleDateFormat(
                            "hh:mm a",
                            Locale.getDefault()
                        )

                    val parsed = input.parse(selectedTimeString)

                    if (parsed != null) {
                        output.format(parsed)
                    } else {
                        selectedTimeString
                    }

                } catch (e: Exception) {
                    selectedTimeString
                }

            } else {
                ""
            }


        tvSelectedSlot.text =
            when {

                datePart.isNotEmpty() && timePart.isNotEmpty() ->
                    "Selected: $datePart • $timePart"

                datePart.isNotEmpty() ->
                    "Selected date: $datePart • Time not selected"

                else ->
                    "You can change slot before confirming"
            }
    }


    // =====================================================
    // DP TO PX
    // =====================================================

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }


    // =====================================================
    // BACK PRESS
    // =====================================================

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
    }
}