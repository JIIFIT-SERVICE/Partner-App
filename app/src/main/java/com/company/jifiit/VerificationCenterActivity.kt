package com.company.jifiit

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.company.jifiit.Referral.HelpActivity
import com.google.android.material.button.MaterialButton
import org.json.JSONObject

class VerificationCenterActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var btnToolbarHelp: ImageView
    private lateinit var tvPartnerId: TextView
    private lateinit var tvVerifiedHeadline: TextView
    private lateinit var tvVerifiedDescription: TextView
    private lateinit var tvCityBadge: TextView
    private lateinit var tvOfficeTitle: TextView
    private lateinit var tvOfficeAddress: TextView
    private lateinit var btnOpenMaps: LinearLayout
    private lateinit var tvScheduledDate: TextView
    private lateinit var tvAssignedTimeSlot: TextView
    private lateinit var btnHelpSupport: MaterialButton

    private val deskPhoneNumber = "+919717550414"
    private val officeAddressText = "Plot 14, Ring Road, Near Moolchand Metro Station, Lajpat Nagar 3, New Delhi - 110024"

    // Polling every 1 second
    private val pollHandler = Handler(Looper.getMainLooper())
    private var isPollRequestInProgress = false
    private val pollIntervalMs: Long = 1000L

    private val statusPollingRunnable = object : Runnable {
        override fun run() {
            if (!isFinishing && !isDestroyed) {
                checkRegistrationStatusAndKitPayment()
                pollHandler.postDelayed(this, pollIntervalMs)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification_center)

        setupStatusBar()
        initViews()
        loadPartnerData()
        setupListeners()
        checkRegistrationStatusAndKitPayment()
    }

    override fun onResume() {
        super.onResume()
        pollHandler.removeCallbacks(statusPollingRunnable)
        pollHandler.post(statusPollingRunnable)
    }

    override fun onPause() {
        super.onPause()
        pollHandler.removeCallbacks(statusPollingRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        pollHandler.removeCallbacks(statusPollingRunnable)
    }

    private fun setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = Color.parseColor("#FFFFFF")
        }
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnToolbarHelp = findViewById(R.id.btnToolbarHelp)
        tvPartnerId = findViewById(R.id.tvPartnerId)
        tvVerifiedHeadline = findViewById(R.id.tvVerifiedHeadline)
        tvVerifiedDescription = findViewById(R.id.tvVerifiedDescription)
        tvCityBadge = findViewById(R.id.tvCityBadge)
        tvOfficeTitle = findViewById(R.id.tvOfficeTitle)
        tvOfficeAddress = findViewById(R.id.tvOfficeAddress)
        btnOpenMaps = findViewById(R.id.btnOpenMaps)
        tvScheduledDate = findViewById(R.id.tvScheduledDate)
        tvAssignedTimeSlot = findViewById(R.id.tvAssignedTimeSlot)
        btnHelpSupport = findViewById(R.id.btnHelpSupport)
    }

    private fun loadPartnerData() {
        val sp = getSharedPreferences("credentials", MODE_PRIVATE)
        val id = intent.getStringExtra("EXTRA_EXPERT_ID") ?: sp.getString("Id", "")
        val city = sp.getString("City", "")

        if (!id.isNullOrBlank()) {
            tvPartnerId.text = "JIIFIT-$id"
        } else {
            tvPartnerId.text = "JIIFIT-517662"
        }

        if (!city.isNullOrBlank()) {
            tvCityBadge.text = "City: $city"
        }

        // Allow intent extras to override appointment details if passed
        val customDate = intent.getStringExtra("EXTRA_SCHEDULED_DATE")
        if (!customDate.isNullOrBlank()) {
            tvScheduledDate.text = customDate
        }

        val customSlot = intent.getStringExtra("EXTRA_TIME_SLOT")
        if (!customSlot.isNullOrBlank()) {
            tvAssignedTimeSlot.text = customSlot
        }
    }

    private fun checkRegistrationStatusAndKitPayment() {
        val sp = getSharedPreferences("credentials", MODE_PRIVATE)
        val therapistId = intent.getStringExtra("EXTRA_EXPERT_ID")
            ?: sp.getString("Id", "")
            ?: ""

        if (therapistId.isBlank()) {
            return
        }

        if (isPollRequestInProgress) {
            return
        }
        isPollRequestInProgress = true

        val request = object : StringRequest(
            Request.Method.POST,
            Constant.TrackRegistrationStatusUrl,
            { response ->
                isPollRequestInProgress = false
                if (!isFinishing && !isDestroyed) {
                    try {
                        val cleanResponse = response?.trim() ?: ""
                        val jsonStart = cleanResponse.indexOf('{')
                        val jsonEnd = cleanResponse.lastIndexOf('}')
                        val sanitized = if (jsonStart >= 0 && jsonEnd > jsonStart) {
                            cleanResponse.substring(jsonStart, jsonEnd + 1)
                        } else {
                            cleanResponse
                        }
                        val json = JSONObject(sanitized)
                        if (json.optBoolean("status", false)) {
                            val l1 = json.optInt("1st_layout", 0)
                            val l2 = json.optInt("2nd_layout", 0)
                            val l3 = json.optInt("3rd_layout", 0)
                            val l4 = json.optInt("4th_layout", 0)
                            val l5 = json.optInt("5th_layout", 0)
                            val kitPayment = json.optString("kit_payment", "").trim()

                            if (l1 == 1 && l2 == 1 && l3 == 1 && l4 == 1 && l5 == 1 && kitPayment.equals("pending", ignoreCase = true)) {
                                pollHandler.removeCallbacks(statusPollingRunnable)
                                val intent = Intent(this@VerificationCenterActivity, TrainingRegistrationKitActivity::class.java).apply {
                                    putExtra("EXTRA_EXPERT_ID", therapistId)
                                }
                                startActivity(intent)
                                finish()
                            } else {
                                val cityName = json.optString("city_name", "").ifBlank {
                                    json.optString("living_city_name", "")
                                }
                                if (cityName.isNotBlank()) {
                                    tvCityBadge.text = "City: $cityName"
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("VerificationCenter", "Error parsing tracking response", e)
                    }
                }
            },
            { error ->
                isPollRequestInProgress = false
                Log.e("VerificationCenter", "Error fetching tracking status: " + error.message)
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id"] = therapistId
                return params
            }
        }

        request.retryPolicy = DefaultRetryPolicy(12000, 1, 1.0f)
        Volley.newRequestQueue(this).add(request)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        btnToolbarHelp.setOnClickListener {
            openHelpAndSupport()
        }

        btnHelpSupport.setOnClickListener {
            openHelpAndSupport()
        }

        btnOpenMaps.setOnClickListener {
            openInGoogleMaps()
        }
    }

    private fun openHelpAndSupport() {
        try {
            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        } catch (e: Exception) {
            // Fallback to dial support
            dialDeskNumber()
        }
    }

    private fun dialDeskNumber() {
        try {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$deskPhoneNumber")
            }
            startActivity(dialIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "Could not open dialer: $deskPhoneNumber", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openInGoogleMaps() {
        val query = "Jiifit Regional Office, $officeAddressText"
        val geoUri = Uri.parse("geo:0,0?q=" + Uri.encode(query))
        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        try {
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(query))
                startActivity(Intent(Intent.ACTION_VIEW, browserUri))
            }
        } catch (e: Exception) {
            val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(query))
            startActivity(Intent(Intent.ACTION_VIEW, browserUri))
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}

