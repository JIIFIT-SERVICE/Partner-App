package com.company.jifiit.Hotel

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.company.jifiit.R
import com.google.android.material.bottomsheet.BottomSheetDialog

class HotelEarningsActivity : AppCompatActivity() {

    private lateinit var drawerLayout: androidx.drawerlayout.widget.DrawerLayout
    private lateinit var navView: com.google.android.material.navigation.NavigationView
    private lateinit var btnExportStatement: LinearLayout
    private lateinit var btnManagePayoutAccount: LinearLayout
    private lateinit var rvCommissionLedger: RecyclerView
    private lateinit var ledgerAdapter: HotelCommissionLedgerAdapter

    private val sampleLedgerItems = listOf(
        HotelCommissionLedgerItem(
            bookingId = "834",
            roomNumber = "112",
            customerName = "Megha Sharma",
            serviceName = "Happy Hour Foot Reflexology + Head & Shoulder Massage",
            dateText = "2026-09-22 at 07:00 PM",
            bookingChannel = "FRONT DESK",
            amountText = "+₹500.00",
            isSettled = false
        ),
        HotelCommissionLedgerItem(
            bookingId = "823",
            roomNumber = "304",
            customerName = "Charu Khanna",
            serviceName = "Pain Relieving Deep Tissue Massage",
            dateText = "2026-09-22 at 01:00 PM",
            bookingChannel = "QR CODE",
            amountText = "+₹500.00",
            isSettled = true
        ),
        HotelCommissionLedgerItem(
            bookingId = "835",
            roomNumber = "418",
            customerName = "Vikramaditya Roy",
            serviceName = "Morning Hatha Yoga & Pranayama",
            dateText = "2026-09-23 at 07:30 AM",
            bookingChannel = "SHARED LINK",
            amountText = "+₹500.00",
            isSettled = false
        ),
        HotelCommissionLedgerItem(
            bookingId = "810",
            roomNumber = "205",
            customerName = "Ananya Verma",
            serviceName = "Ayurvedic Abhyanga Therapy",
            dateText = "2026-09-21 at 05:00 PM",
            bookingChannel = "FRONT DESK",
            amountText = "+₹500.00",
            isSettled = true
        ),
        HotelCommissionLedgerItem(
            bookingId = "798",
            roomNumber = "102",
            customerName = "Rohan Kapoor",
            serviceName = "Stress Relief Head Massage",
            dateText = "2026-09-20 at 03:30 PM",
            bookingChannel = "QR CODE",
            amountText = "+₹500.00",
            isSettled = true
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_hotel_earnings)

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
        setupRecyclerView()
        setupListeners()
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        btnExportStatement = findViewById(R.id.btnExportStatement)
        btnManagePayoutAccount = findViewById(R.id.btnManagePayoutAccount)
        rvCommissionLedger = findViewById(R.id.rvCommissionLedger)

        HotelHeaderHelper.setupHeader(this, drawerLayout, navView, R.id.nav_earnings)
    }

    private fun setupRecyclerView() {
        rvCommissionLedger.layoutManager = LinearLayoutManager(this)
        ledgerAdapter = HotelCommissionLedgerAdapter(sampleLedgerItems) { item ->
            showLedgerItemDetails(item)
        }
        rvCommissionLedger.adapter = ledgerAdapter
    }

    private fun setupListeners() {
        btnExportStatement.setOnClickListener {
            exportStatement()
        }

        btnManagePayoutAccount.setOnClickListener {
            showManagePayoutDialog()
        }
    }

    private fun exportStatement() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Grand Orchid Hotel - Commission Payout Statement")
            putExtra(
                Intent.EXTRA_TEXT,
                """
                GRAND ORCHID HOTEL - COMMISSION LEDGER STATEMENT
                ------------------------------------------------
                Total Commission Earned: ₹2,500.00 (5 bookings)
                Settled to Bank Account: ₹1,500.00 (3 sessions)
                Active / In-Flight Payout: ₹1,000.00 (2 sessions)
                
                Registered Bank: HDFC Bank Ltd.
                Account: **********8492
                IFSC: HDFC0001092
                Beneficiary: Grand Orchid Hospitality LLP
                
                Recent Itemized Ledger:
                - Booking #834 (Room 112): ₹500.00 [Pending Session]
                - Booking #823 (Room 304): ₹500.00 [Settled]
                - Booking #835 (Room 418): ₹500.00 [Pending Session]
                - Booking #810 (Room 205): ₹500.00 [Settled]
                - Booking #798 (Room 102): ₹500.00 [Settled]
                """.trimIndent()
            )
        }
        try {
            startActivity(Intent.createChooser(shareIntent, "Export Payout Statement"))
        } catch (e: Exception) {
            Toast.makeText(this, "Payout statement exported successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showManagePayoutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Registered Settlement Account")
        builder.setMessage(
            """
            Bank: HDFC Bank Ltd.
            Account Number: **********8492
            IFSC Code: HDFC0001092
            Account Holder: Grand Orchid Hospitality LLP
            Payout Frequency: Automated Weekly Settlement (Every Friday)
            
            To update your banking details, please submit an IFSC verification request or contact partner support.
            """.trimIndent()
        )
        builder.setPositiveButton("Close") { dialog, _ ->
            dialog.dismiss()
        }
        builder.setNegativeButton("Help Support") { dialog, _ ->
            dialog.dismiss()
            Toast.makeText(this, "Opening partner support...", Toast.LENGTH_SHORT).show()
        }
        builder.show()
    }

    private fun showLedgerItemDetails(item: HotelCommissionLedgerItem) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Booking #${item.bookingId} - Room ${item.roomNumber}")
        builder.setMessage(
            """
            Guest: ${item.customerName}
            Service: ${item.serviceName}
            Date & Time: ${item.dateText}
            Source: ${item.bookingChannel}
            
            Guaranteed Commission: ${item.amountText}
            Payout Status: ${if (item.isSettled) "Settled to Bank Account" else "In-Flight (Pending Session Completion)"}
            """.trimIndent()
        )
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
}
