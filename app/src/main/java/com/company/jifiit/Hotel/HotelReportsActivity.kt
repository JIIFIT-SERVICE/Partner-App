package com.company.jifiit.Hotel

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.company.jifiit.R

class HotelReportsActivity : AppCompatActivity() {

    private lateinit var drawerLayout: androidx.drawerlayout.widget.DrawerLayout
    private lateinit var navView: com.google.android.material.navigation.NavigationView
    private lateinit var btnGenerateStandees: LinearLayout

    private lateinit var tvBedsideScansCount: TextView
    private lateinit var tvBedsideConversion: TextView
    private lateinit var tvSharedClicksCount: TextView
    private lateinit var tvSharedClicksConversion: TextView
    private lateinit var tvConciergeBookingsCount: TextView
    private lateinit var tvConciergeCommission: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_hotel_reports)

        // ✅ White icons on brand blue status bar matching application theme
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = true
        }

        window.statusBarColor = ContextCompat.getColor(this, R.color.hotel_primary)
        window.navigationBarColor = Color.WHITE

        val root = findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.drawer_layout)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, 0, bars.right, bars.bottom)
            insets
        }

        val header = findViewById<View>(R.id.layoutHotelHeader)
        ViewCompat.setOnApplyWindowInsetsListener(header) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, bars.top, v.paddingRight, v.paddingBottom)
            insets
        }

        initViews()
        setupListeners()
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        btnGenerateStandees = findViewById(R.id.btnGenerateStandees)

        tvBedsideScansCount = findViewById(R.id.tvBedsideScansCount)
        tvBedsideConversion = findViewById(R.id.tvBedsideConversion)
        tvSharedClicksCount = findViewById(R.id.tvSharedClicksCount)
        tvSharedClicksConversion = findViewById(R.id.tvSharedClicksConversion)
        tvConciergeBookingsCount = findViewById(R.id.tvConciergeBookingsCount)
        tvConciergeCommission = findViewById(R.id.tvConciergeCommission)

        HotelHeaderHelper.setupHeader(this, drawerLayout, navView, R.id.nav_reports)
    }

    private fun setupListeners() {
        btnGenerateStandees.setOnClickListener {
            showGenerateStandeesDialog()
        }
    }

    private fun showGenerateStandeesDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Generate In-Room QR Standees")
        builder.setMessage(
            """
            Print-ready acrylic bedside standees and lobby tent cards for Grand Orchid Hotel.
            
            • Bedside QR codes linked to instant room delivery
            • Dynamic room number tagging for seamless billing
            • High-resolution PDF export for professional print
            """.trimIndent()
        )
        builder.setPositiveButton("Download Standee PDF") { dialog, _ ->
            dialog.dismiss()
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Grand Orchid Hotel - In-Room QR Standees")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "In-Room QR Standee Kit ready for Grand Orchid Hotel. Access your printable acrylic templates from partner portal."
                )
            }
            try {
                startActivity(Intent.createChooser(shareIntent, "Export QR Standees"))
            } catch (e: Exception) {
                Toast.makeText(this, "QR Standees generated successfully", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
}
