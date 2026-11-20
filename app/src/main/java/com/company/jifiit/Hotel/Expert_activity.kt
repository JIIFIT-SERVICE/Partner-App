package com.company.jifiit.Hotel

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.company.jifiit.Constant
import com.company.jifiit.Hotel.Adapter.ExpertAdapter
import com.company.jifiit.Hotel.Model.ExpertModel
import com.company.jifiit.LocalStorage.HotelDatabaseHelper
import com.company.jifiit.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import org.json.JSONObject
import java.math.BigInteger

class Expert_activity : AppCompatActivity() {

    // =====================================================
    // EXTRA KEYS (shared with HotelPayment)
    // =====================================================
    companion object {
        const val EXTRA_EXPERT_ID = "EXPERT_ID"
        const val EXTRA_EXPERT_NAME = "EXPERT_NAME"
        const val EXTRA_EMPLOYEE_ID = "EMPLOYEE_ID"
        const val EXTRA_HOTEL_ID = "HOTEL_ID"
        const val EXTRA_TOTAL_MASSAGE_DURATION = "TOTAL_MASSAGE_DURATION"
        const val EXTRA_NEED_BED = "NEED_BED"
        const val EXTRA_BOOKING_DATE = "BOOKING_DATE"
        const val EXTRA_BOOKING_TIME = "BOOKING_TIME"
        const val EXTRA_CLIENT_GENDER = "CLIENT_GENDER"
        const val EXTRA_SELECTED_SERVICE = "SELECTED_SERVICE"
    }

    // =====================================================
    // VIEWS
    // =====================================================
    private lateinit var btnBack: ImageView
    private lateinit var btnHome: ImageView
    private lateinit var rvExperts: RecyclerView
    private lateinit var btnContinue: MaterialButton
    private lateinit var tvAvailableTitle: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var cartFab: FrameLayout
    private lateinit var btnMaleExpert: LinearLayout
    private lateinit var btnFemaleExpert: LinearLayout
    private lateinit var switchBed: SwitchMaterial

    // =====================================================
    // EXPERT LIST
    // =====================================================
    private val expertList = ArrayList<ExpertModel>()
    private lateinit var expertAdapter: ExpertAdapter

    // =====================================================
    // SELECTED EXPERT
    // =====================================================
    private var selectedExpert: ExpertModel? = null
    private var selectedExpertId: String? = null

    // =====================================================
    // HOTEL ID
    // =====================================================
    private lateinit var Id: BigInteger

    // =====================================================
    // BOOKING DATA
    // =====================================================
    private var bookingDate: String = ""
    private var bookingTime: String = ""

    // =====================================================
    // GENDER STATE
    // =====================================================
    private var selectedExpertGender: String = "" // "male" or "female"
    private lateinit var rbMaleExpert: RadioButton
    private lateinit var rbFemaleExpert: RadioButton

    // =====================================================
    // INCOMING EXTRAS (from Slot_activity)
    // =====================================================
    private var guestName: String = ""
    private var guestMobile: String = ""
    private var country_Code: String = ""
    private var Mobile: String = ""

    private var roomId: String = ""
    private var roomNumber: String = ""
    private var floorWing: String = ""
    private var ringDoorbell: Boolean = false
    private var doNotCall: Boolean = false
    private var callConcierge: Boolean = false
    private var specialNote: String = ""
    private var voiceNotePath: String = ""

    // =====================================================
    // ON CREATE
    // =====================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        setContentView(R.layout.activity_therist)

        // =================================================
        // WINDOW INSETS
        // =================================================
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                maxOf(systemBars.bottom, ime.bottom)
            )
            insets
        }

        // =================================================
        // INITIALIZE VIEWS
        // =================================================
        btnBack          = findViewById(R.id.btnBack)
        rvExperts        = findViewById(R.id.rvExperts)
        btnContinue      = findViewById(R.id.btnContinue)
        tvAvailableTitle = findViewById(R.id.tvAvailableTitle)
        btnMaleExpert    = findViewById(R.id.btnMaleExpert)
        btnFemaleExpert  = findViewById(R.id.btnFemaleExpert)
        rbMaleExpert     = findViewById(R.id.rbMaleExpert)
        rbFemaleExpert   = findViewById(R.id.rbFemaleExpert)

        // =================================================
        // READ ALL INCOMING EXTRAS  (single place)
        // =================================================
        readIncomingExtras()

        // =================================================
        // INITIAL GENDER UI
        // =================================================
        if (selectedExpertGender.isEmpty()) {
            selectedExpertGender = "male"
        }
        applyGenderUi(selectedExpertGender)

        // =================================================
        // BACK BUTTON
        // =================================================
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        // =================================================
        // RECYCLER VIEW
        // =================================================
        rvExperts.layoutManager = LinearLayoutManager(this)
        rvExperts.setHasFixedSize(false)
        rvExperts.isNestedScrollingEnabled = true

        // =================================================
        // ADAPTER
        // =================================================
        expertAdapter = ExpertAdapter(expertList) { expert ->
            selectExpert(expert)
        }
        rvExperts.adapter = expertAdapter

        // =================================================
        // CONTINUE BUTTON
        // =================================================
        btnContinue.setOnClickListener {
            continueToPayment()
        }

        // =================================================
        // GENDER TOGGLE
        // =================================================
        btnMaleExpert.setOnClickListener {
            if (selectedExpertGender == "male") return@setOnClickListener
            applyGenderUi("male")
            loadExperts()
        }

        btnFemaleExpert.setOnClickListener {
            if (selectedExpertGender == "female") return@setOnClickListener
            applyGenderUi("female")
            loadExperts()
        }

        // =================================================
        // LOAD EXPERTS
        // =================================================
        loadExperts()
    }

    // =====================================================
    // READ INCOMING EXTRAS  (called ONCE from onCreate)
    // =====================================================
    private fun readIncomingExtras() {

        // ----- Guest / room / flags -----
        guestName     = intent.getStringExtra("guest_name")      ?: ""
        guestMobile   = intent.getStringExtra("guest_mobile_full")    ?: ""
        country_Code  = intent.getStringExtra("country_code").orEmpty()
        Mobile = intent.getStringExtra("guest_mobile").orEmpty()


        roomId        = intent.getStringExtra("room_id")         ?: ""
        roomNumber    = intent.getStringExtra("room_number")     ?: ""
        floorWing     = intent.getStringExtra("floor_wing")      ?: ""

        ringDoorbell  = intent.getBooleanExtra("ring_doorbell",  false)
        doNotCall     = intent.getBooleanExtra("do_not_call",    false)
        callConcierge = intent.getBooleanExtra("call_concierge", false)

        specialNote   = intent.getStringExtra("special_note")    ?: ""
        voiceNotePath = intent.getStringExtra("voice_note_path") ?: ""

        // ----- Booking -----
        bookingDate = intent.getStringExtra("booking_date") ?: ""
        bookingTime = intent.getStringExtra("booking_time") ?: ""

        // ----- Gender -----
        selectedExpertGender = intent.getStringExtra("client_gender") ?: "male"

        // ----- Debug -----
        Log.d("EXPERT_INTENT", "guest_name     = $guestName")
        Log.d("EXPERT_INTENT", "guest_mobile_with_Full   = $guestMobile")
        Log.d("EXPERT_INTENT", "country_code   = $country_Code")
        Log.d("EXPERT_INTENT", "guest_mobile   = $Mobile")
        Log.d("EXPERT_INTENT", "room_id        = $roomId")
        Log.d("EXPERT_INTENT", "room_number    = $roomNumber")
        Log.d("EXPERT_INTENT", "floor_wing     = $floorWing")
        Log.d("EXPERT_INTENT", "ring_doorbell  = $ringDoorbell")
        Log.d("EXPERT_INTENT", "do_not_call    = $doNotCall")
        Log.d("EXPERT_INTENT", "call_concierge = $callConcierge")
        Log.d("EXPERT_INTENT", "special_note   = $specialNote")
        Log.d("EXPERT_INTENT", "voice_note_path= $voiceNotePath")
        Log.d("EXPERT_INTENT", "booking_date   = $bookingDate")
        Log.d("EXPERT_INTENT", "booking_time   = $bookingTime")
        Log.d("EXPERT_INTENT", "gender         = $selectedExpertGender")
    }

    // =====================================================
    // GENDER UI
    // =====================================================
    private fun applyGenderUi(gender: String) {
        selectedExpertGender = gender

        val isMale = gender == "male"

        btnMaleExpert.setBackgroundResource(
            if (isMale) R.drawable.bg_radio_selected
            else R.drawable.bg_radio_unselected
        )
        btnFemaleExpert.setBackgroundResource(
            if (isMale) R.drawable.bg_radio_unselected
            else R.drawable.bg_radio_selected
        )

        rbMaleExpert.isChecked = isMale
        rbFemaleExpert.isChecked = !isMale

        val selectedTint   = Color.parseColor("#1976D2")
        val unselectedTint = Color.parseColor("#8892A0")

        rbMaleExpert.buttonTintList =
            ColorStateList.valueOf(if (isMale) selectedTint else unselectedTint)

        rbFemaleExpert.buttonTintList =
            ColorStateList.valueOf(if (isMale) unselectedTint else selectedTint)
    }

    // =====================================================
    // LOAD EXPERTS FROM API
    // =====================================================
    private fun loadExperts() {

        val spCred   = getSharedPreferences("credentials", MODE_PRIVATE)
        val idString = spCred.getString("Id", "0") ?: "0"

        Id = try {
            BigInteger(idString)
        } catch (e: NumberFormatException) {
            BigInteger.ZERO
        }

        // Clear old data + reset selection
        expertList.clear()
        expertAdapter.notifyDataSetChanged()

        selectedExpert   = null
        selectedExpertId = null

        btnContinue.isEnabled = false

        val dbHelper = HotelDatabaseHelper(this)
        val totalMassageDuration = dbHelper.getTotalMassageDuration()

        // Read directly from intent (source of truth)
        val needBed     = intent.getIntExtra("need_bed", 0)
        val bookingDateI = intent.getStringExtra("booking_date") ?: ""
        val bookingTimeI = intent.getStringExtra("booking_time") ?: ""

        // Keep local fields in sync
        bookingDate = bookingDateI
        bookingTime = bookingTimeI

        // Use current toggle gender (fallback to intent)
        if (selectedExpertGender.isEmpty()) {
            selectedExpertGender =
                intent.getStringExtra("client_gender") ?: "male"
        }

        val clientGender = selectedExpertGender
        val SELECTED_SERVICE = intent.getStringExtra("SELECTED_SERVICE") ?: ""

        // Title
        tvAvailableTitle.text =
            "Available Expert for $bookingTime ($bookingDate)"

        // Required data check
        if (totalMassageDuration <= 0 ||
            bookingDate.isEmpty() ||
            bookingTime.isEmpty() ||
            clientGender.isEmpty() ||
            SELECTED_SERVICE.isEmpty()
        ) {
            btnContinue.isEnabled = false
            Toast.makeText(
                this,
                "Booking information is missing",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Build JSON
        val jsonBody = JSONObject().apply {
            put("totalMassageDuration", totalMassageDuration)
            put("hotel_id", Id)
            put("need_bed", needBed)
            put("booking_date", bookingDate)
            put("booking_time", bookingTime)
            put("client_gender", clientGender)
        }

        Log.d("EXPERT_API", "Request: $jsonBody")

        // Volley request
        val request = JsonObjectRequest(
            Request.Method.POST,
            Constant.EXPERT_API,
            jsonBody,

            { response ->
                Log.d("EXPERT_API", "Response: $response")
                parseExpertResponse(response)
            },

            { error ->
                btnContinue.isEnabled = false

                Log.e("EXPERT_API", "Volley Error", error)

                val statusCode = error.networkResponse?.statusCode
                val responseBody = error.networkResponse?.data?.let {
                    String(it, Charsets.UTF_8)
                }

                Log.e("EXPERT_API", "HTTP Status: $statusCode")
                Log.e("EXPERT_API", "Response Body: $responseBody")

                val message = if (!responseBody.isNullOrEmpty()) {
                    responseBody
                } else {
                    "Server error: $statusCode"
                }

                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        )

        request.retryPolicy = DefaultRetryPolicy(30000, 1, 1.0f)

        Volley.newRequestQueue(this).add(request)
    }

    // =====================================================
    // PARSE EXPERT RESPONSE
    // =====================================================
    private fun parseExpertResponse(response: JSONObject) {

        try {
            val success = response.optBoolean("success", false)

            if (!success) {
                btnContinue.isEnabled = false
                val errorMessage = response.optString(
                    "error",
                    "No available therapists found"
                )
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                return
            }

            val therapists = response.optJSONArray("therapists")

            if (therapists == null || therapists.length() == 0) {
                btnContinue.isEnabled = false
                Toast.makeText(
                    this,
                    "No available experts found",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            expertList.clear()

            for (i in 0 until therapists.length()) {

                val therapist = therapists.getJSONObject(i)

                val id         = therapist.optString("id", "")
                val name       = therapist.optString("name", "")
                val employeeId = id

                val experience = therapist.optString("experience", "0 years")
                val image      = therapist.optString("profile_photo", "")

                val role = therapist.optString("role", "Massage Expert")
                val hometown = therapist.optString("howe_town", "")
                val lifetimeExp = therapist.optString("lifetime_exp", "")
                val rating = therapist.optString("rating", "0.0")
                val ratingCount = therapist.optString("rating_count", "0")
                val availability = therapist.optString(
                    "availability",
                    "Available for male client only"
                )

                val expert = ExpertModel(
                    id = id,
                    name = name,
                    employeeId = employeeId,
                    experience = experience,
                    image = image,
                    role = role,
                    hometown = hometown,
                    lifetimeExp = lifetimeExp,
                    rating = rating,
                    ratingCount = ratingCount,
                    availability = availability
                )

                expertList.add(expert)
            }

            expertAdapter.notifyDataSetChanged()
            btnContinue.isEnabled = expertList.isNotEmpty()

        } catch (e: Exception) {
            e.printStackTrace()
            btnContinue.isEnabled = false
            Toast.makeText(
                this,
                "Failed to process expert data",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // =====================================================
    // SELECT EXPERT
    // =====================================================
    private fun selectExpert(expert: ExpertModel) {

        selectedExpert   = expert
        selectedExpertId = expert.id

        Log.d("SELECTED_EXPERT", "Expert ID:   ${expert.id}")
        Log.d("SELECTED_EXPERT", "Employee ID: ${expert.employeeId}")

        Toast.makeText(
            this,
            "${expert.name} selected",
            Toast.LENGTH_SHORT
        ).show()
    }

    // =====================================================
    // CONTINUE TO PAYMENT
    // =====================================================
    private fun continueToPayment() {

        if (selectedExpert == null) {
            Toast.makeText(
                this,
                "Please select an expert first",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val expert   = selectedExpert!!
        val expertId = selectedExpertId ?: ""

        // Collect booking data
        val dbHelper = HotelDatabaseHelper(this)
        val totalMassageDuration = dbHelper.getTotalMassageDuration()

        val needBed = intent.getIntExtra("need_bed", 0)

        // Use local fields (already synced from intent in onCreate)
        val clientGender    = selectedExpertGender
        val selectedService = intent.getStringExtra("SELECTED_SERVICE") ?: ""
        val hotelId         = Id.toString()   // BigInteger -> String

        // Validation
        if (bookingDate.isEmpty() ||
            bookingTime.isEmpty() ||
            clientGender.isEmpty() ||
            selectedService.isEmpty()
        ) {
            Toast.makeText(
                this,
                "Booking information is missing",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // =================================================
        // NEXT ACTIVITY
        // =================================================
        val intent = Intent(this, HotelPayment::class.java)

        // ----- Expert info -----
        intent.putExtra(EXTRA_EXPERT_ID,   expertId)
        intent.putExtra(EXTRA_EXPERT_NAME, expert.name)
        intent.putExtra(EXTRA_EMPLOYEE_ID, expert.employeeId)

        // ----- Booking info -----
        intent.putExtra(EXTRA_HOTEL_ID,               hotelId)
        intent.putExtra(EXTRA_TOTAL_MASSAGE_DURATION, totalMassageDuration)
        intent.putExtra(EXTRA_NEED_BED,               needBed)
        intent.putExtra(EXTRA_BOOKING_DATE,           bookingDate)
        intent.putExtra(EXTRA_BOOKING_TIME,           bookingTime)
        intent.putExtra(EXTRA_CLIENT_GENDER,          clientGender)
        intent.putExtra(EXTRA_SELECTED_SERVICE,       selectedService)

        // ----- Guest / Room / Flags forwarded from Slot_activity -----
        intent.putExtra("guest_name",      guestName)
        intent.putExtra("guest_mobile_full", guestMobile)
        intent.putExtra("country_code", country_Code)
        intent.putExtra("guest_mobile", Mobile)

        intent.putExtra("room_id",         roomId)
        intent.putExtra("room_number",     roomNumber)
        intent.putExtra("floor_wing",      floorWing)
        intent.putExtra("ring_doorbell",   ringDoorbell)
        intent.putExtra("do_not_call",     doNotCall)
        intent.putExtra("call_concierge",  callConcierge)
        intent.putExtra("special_note",    specialNote)
        intent.putExtra("voice_note_path", voiceNotePath)   // 👈 critical

        startActivity(intent)

        overridePendingTransition(
            R.anim.slide_in_right,
            R.anim.slide_out_left
        )
    }

    // =====================================================
    // BACK PRESS
    // =====================================================
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}