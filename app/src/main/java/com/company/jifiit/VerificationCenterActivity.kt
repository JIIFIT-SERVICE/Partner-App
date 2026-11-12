package com.company.jifiit

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.company.jifiit.Referral.HelpActivity
import com.google.android.material.button.MaterialButton

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification_center)

        setupStatusBar()
        initViews()
        loadPartnerData()
        setupListeners()
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
        val id = sp.getString("Id", "")
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

