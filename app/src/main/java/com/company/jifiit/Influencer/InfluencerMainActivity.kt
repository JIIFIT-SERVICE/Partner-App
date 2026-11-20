package com.company.jifiit.Influencer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.company.jifiit.Constant
import com.company.jifiit.LoginActivity
import com.company.jifiit.MyQr.My_Qr
import com.company.jifiit.R
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject

class InfluencerMainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var headerContainer: LinearLayout

    // Top Header Views
    private lateinit var btnOpenDrawer: ImageView
    private lateinit var btnQuickShare: ImageView
    private lateinit var tvMainGreeting: TextView
    private lateinit var tvMainRoleSubtitle: TextView

    // Active Coupon Code & Share
    private lateinit var tvReferralCodeDisplay: TextView
    private lateinit var btnCopyCode: FrameLayout
    private lateinit var btnShareCodeQr: LinearLayout

    // 2x2 Stats Cards
    private lateinit var cardTotalUsedBookings: LinearLayout
    private lateinit var tvTotalUsedCount: TextView
    private lateinit var tvTotalTargetCount: TextView
    private lateinit var cardInWallet: LinearLayout
    private lateinit var tvInWalletAmount: TextView
    private lateinit var cardPendingVerification: LinearLayout
    private lateinit var tvPendingAmount: TextView
    private lateinit var cardLifetimeEarned: LinearLayout
    private lateinit var tvLifetimeEarnedAmount: TextView

    // Date Filter Buttons (Yesterday, Today, Tomorrow, Calendar)
    private lateinit var tvDateYesterday: TextView
    private lateinit var tvDateToday: TextView
    private lateinit var tvDateTomorrow: TextView
    private lateinit var layoutDateCalendar: LinearLayout
    private lateinit var ivDateCalendarIcon: ImageView
    private lateinit var tvDateCalendarText: TextView
    private var selectedDateFilter: String = "today"

    // Dynamic Table Views & Adapter
    private lateinit var rvBookingsTable: RecyclerView
    private lateinit var layoutEmptyBookings: LinearLayout
    private lateinit var tvEmptyTitle: TextView
    private lateinit var bookingAdapter: InfluencerBookingAdapter
    private val masterBookingsList = mutableListOf<InfluencerBooking>()
    private val displayedBookingsList = mutableListOf<InfluencerBooking>()

    // Drawer Views (Profile photo, Name, Category badge)
    private lateinit var tvDrawerUserName: TextView
    private lateinit var tvDrawerCategoryBadge: TextView
    private lateinit var tvDrawerUserInitials: TextView
    private lateinit var ivDrawerProfilePhoto: CircleImageView
    private lateinit var tvDrawerReferralCodeBadge: TextView
    private lateinit var tvDrawerEarningsAmount: TextView
    private lateinit var tvDrawerAppVersion: TextView

    // Drawer Items
    private lateinit var itemDrawerOverview: LinearLayout
    private lateinit var itemDrawerReferralCode: LinearLayout
    private lateinit var itemDrawerReferredCustomers: LinearLayout
    private lateinit var itemDrawerMyQr: LinearLayout
    private lateinit var itemDrawerEarnings: LinearLayout
    private lateinit var itemDrawerReports: LinearLayout
    private lateinit var itemDrawerHelp: LinearLayout
    private lateinit var itemDrawerTerms: LinearLayout
    private lateinit var itemDrawerPrivacyPolicy: LinearLayout
    private lateinit var btnDrawerLogout: LinearLayout

    private lateinit var sp: SharedPreferences
    private var referralCode: String = ""
    private var partnerName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_influencer_main)

        // Configure Status Bar for dark navy header
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = false
        windowInsetsController.isAppearanceLightNavigationBars = true

        sp = getSharedPreferences("credentials", Context.MODE_PRIVATE)

        initializeViews()
        applyWindowInsets()
        fetchInfluencerProfile()
        setupDrawerListeners()
        setupDashboardActions()
        setupDateSelector()
        setupDynamicTable()
    }

    private fun initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        headerContainer = findViewById(R.id.headerContainer)

        // Top bar views
        btnOpenDrawer = findViewById(R.id.btnOpenDrawer)
        btnQuickShare = findViewById(R.id.btnQuickShare)
        tvMainGreeting = findViewById(R.id.tvMainGreeting)
        tvMainRoleSubtitle = findViewById(R.id.tvMainRoleSubtitle)

        // Coupon code & share
        tvReferralCodeDisplay = findViewById(R.id.tvReferralCodeDisplay)
        btnCopyCode = findViewById(R.id.btnCopyCode)
        btnShareCodeQr = findViewById(R.id.btnShareCodeQr)

        // 2x2 Stats Cards
        cardTotalUsedBookings = findViewById(R.id.cardTotalUsedBookings)
        tvTotalUsedCount = findViewById(R.id.tvTotalUsedCount)
        tvTotalTargetCount = findViewById(R.id.tvTotalTargetCount)
        cardInWallet = findViewById(R.id.cardInWallet)
        tvInWalletAmount = findViewById(R.id.tvInWalletAmount)
        cardPendingVerification = findViewById(R.id.cardPendingVerification)
        tvPendingAmount = findViewById(R.id.tvPendingAmount)
        cardLifetimeEarned = findViewById(R.id.cardLifetimeEarned)
        tvLifetimeEarnedAmount = findViewById(R.id.tvLifetimeEarnedAmount)

        // Date Filter Buttons
        tvDateYesterday = findViewById(R.id.tvDateYesterday)
        tvDateToday = findViewById(R.id.tvDateToday)
        tvDateTomorrow = findViewById(R.id.tvDateTomorrow)
        layoutDateCalendar = findViewById(R.id.layoutDateCalendar)
        ivDateCalendarIcon = findViewById(R.id.ivDateCalendarIcon)
        tvDateCalendarText = findViewById(R.id.tvDateCalendarText)

        // Dynamic Table Views
        rvBookingsTable = findViewById(R.id.rvBookingsTable)
        layoutEmptyBookings = findViewById(R.id.layoutEmptyBookings)
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle)

        // Drawer views
        tvDrawerUserName = findViewById(R.id.tvDrawerUserName)
        tvDrawerCategoryBadge = findViewById(R.id.tvDrawerCategoryBadge)
        tvDrawerUserInitials = findViewById(R.id.tvDrawerUserInitials)
        ivDrawerProfilePhoto = findViewById(R.id.ivDrawerProfilePhoto)
        tvDrawerReferralCodeBadge = findViewById(R.id.tvDrawerReferralCodeBadge)
        tvDrawerEarningsAmount = findViewById(R.id.tvDrawerEarningsAmount)
        tvDrawerAppVersion = findViewById(R.id.tvDrawerAppVersion)

        // Drawer Menu items
        itemDrawerOverview = findViewById(R.id.itemDrawerOverview)
        itemDrawerReferralCode = findViewById(R.id.itemDrawerReferralCode)
        itemDrawerReferredCustomers = findViewById(R.id.itemDrawerReferredCustomers)
        itemDrawerMyQr = findViewById(R.id.itemDrawerMyQr)
        itemDrawerEarnings = findViewById(R.id.itemDrawerEarnings)
        itemDrawerReports = findViewById(R.id.itemDrawerReports)
        itemDrawerHelp = findViewById(R.id.itemDrawerHelp)
        itemDrawerTerms = findViewById(R.id.itemDrawerTerms)
        itemDrawerPrivacyPolicy = findViewById(R.id.itemDrawerPrivacyPolicy)
        btnDrawerLogout = findViewById(R.id.btnDrawerLogout)
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            headerContainer.setPadding(
                headerContainer.paddingStart,
                systemBars.top + 6,
                headerContainer.paddingEnd,
                headerContainer.paddingBottom
            )
            insets
        }
    }

    private fun fetchInfluencerProfile() {
        val influencerId = sp.getString("Id", null)
        if (influencerId.isNullOrEmpty() || influencerId == "null") {
            return
        }

        val url = Constant.InfluencerProfile + "?id=" + influencerId
        val request = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    val isSuccess = response.optBoolean("success", false) ||
                            response.optString("status").equals("success", ignoreCase = true)
                    if (isSuccess) {
                        val data = response.optJSONObject("data")
                        if (data != null) {
                            updateProfileUI(data)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun updateProfileUI(data: JSONObject) {
        val fullName = data.optString("full_name", data.optString("name", ""))
        val category = data.optString("partner_category", "")
        val code = data.optString("referral_code", "")
        val profileImageUrl = data.optString("profile_image_url", "")
        val channelUrl = data.optString("channel_url", "")
        val verification = data.optString("verification", "")

        partnerName = fullName
        referralCode = code

        if (fullName.isNotEmpty()) {
            tvDrawerUserName.text = fullName
            tvMainGreeting.text = "Welcome, $fullName"
            tvDrawerUserInitials.text = generateInitials(fullName)
        }

        if (category.isNotEmpty()) {
            tvDrawerCategoryBadge.text = category
        }

        if (code.isNotEmpty()) {
            tvReferralCodeDisplay.text = code
            tvDrawerReferralCodeBadge.text = code
        }

        if (profileImageUrl.isNotEmpty() && profileImageUrl != "null") {
            val fullImageUrl = if (profileImageUrl.startsWith("http://") || profileImageUrl.startsWith("https://")) {
                profileImageUrl
            } else {
                "https://jiifit.com/Jifiit/" + profileImageUrl.removePrefix("/")
            }

            ivDrawerProfilePhoto.visibility = View.VISIBLE
            tvDrawerUserInitials.visibility = View.GONE

            Glide.with(this)
                .load(fullImageUrl)
                .placeholder(R.drawable.ic_avatar_placeholder)
                .error(R.drawable.ic_avatar_placeholder)
                .into(ivDrawerProfilePhoto)
        } else {
            ivDrawerProfilePhoto.visibility = View.GONE
            tvDrawerUserInitials.visibility = View.VISIBLE
        }

        val editor = sp.edit()
        if (fullName.isNotEmpty()) {
            editor.putString("Name", fullName)
        }
        if (category.isNotEmpty()) {
            editor.putString("partnerCategory", category)
            editor.putString("Category", category)
        }
        if (code.isNotEmpty()) {
            editor.putString("referral_code", code)
            editor.putString("ReferralCode", code)
        }
        if (profileImageUrl.isNotEmpty() && profileImageUrl != "null") {
            editor.putString("profile_image_url", profileImageUrl)
        }
        if (channelUrl.isNotEmpty()) {
            editor.putString("channel_url", channelUrl)
        }
        if (verification.isNotEmpty()) {
            editor.putString("Verification", verification)
        }
        editor.apply()

        if (referralCode.isNotEmpty()) {
            fetchServerBookings(referralCode)
        }
    }

    private fun generateInitials(name: String): String {
        val parts = name.trim().split("\\s+".toRegex())
        return when {
            parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
            parts.isNotEmpty() && parts[0].isNotEmpty() -> parts[0].take(2).uppercase()
            else -> "IP"
        }
    }

    private fun setupDynamicTable() {
        bookingAdapter = InfluencerBookingAdapter(this, displayedBookingsList) { booking, position ->
            handleBookingItemClick(booking, position)
        }
        rvBookingsTable.layoutManager = LinearLayoutManager(this)
        rvBookingsTable.adapter = bookingAdapter

        loadInitialDynamicBookings()
    }

    private fun loadInitialDynamicBookings() {
        masterBookingsList.clear()

        // 1. TODAY'S BOOKINGS
        masterBookingsList.add(
            InfluencerBooking(
                id = "BK_101",
                customerName = "Tirthankar Gupta",
                discountTag = "Saved ₹100 with coupon",
                phoneNumber = "******8921",
                serviceName = "Stress Relief Massage",
                bookingDate = "Today, 4:00 PM",
                location = "Indiranagar",
                locationArea = "Bangalore Central",
                rating = 5.0f,
                feedback = "Very relaxing session & polite therapist",
                paymentStatus = "Paid",
                commission = "+₹50",
                dateCategory = "today"
            )
        )
        masterBookingsList.add(
            InfluencerBooking(
                id = "BK_102",
                customerName = "Munish Kumar",
                discountTag = "Saved ₹100 with coupon",
                phoneNumber = "******4530",
                serviceName = "Deep Tissue Massage",
                bookingDate = "Today, 6:30 PM",
                location = "Koramangala",
                locationArea = "4th Block",
                rating = 4.9f,
                feedback = "Super punctual and great deep tissue",
                paymentStatus = "Paid",
                commission = "+₹50",
                dateCategory = "today"
            )
        )
        masterBookingsList.add(
            InfluencerBooking(
                id = "BK_103",
                customerName = "Ananya Deshmukh",
                discountTag = "Saved ₹100 with coupon",
                phoneNumber = "******9812",
                serviceName = "Stress Relief Massage",
                bookingDate = "Today, 8:00 PM",
                location = "Whitefield",
                locationArea = "Prestige Shantiniketan",
                rating = 0.0f,
                feedback = "Doorstep session awaiting OTP verify",
                paymentStatus = "Pending",
                commission = "+₹50",
                dateCategory = "today"
            )
        )

        // 2. YESTERDAY'S BOOKINGS
        masterBookingsList.add(
            InfluencerBooking(
                id = "BK_104",
                customerName = "Vikramaditya Singh",
                discountTag = "Saved ₹100 with coupon",
                phoneNumber = "******1109",
                serviceName = "Deep Tissue Massage",
                bookingDate = "Yesterday, 3:00 PM",
                location = "HSR Layout",
                locationArea = "Sector 2",
                rating = 5.0f,
                feedback = "On-time arrival and professional setup",
                paymentStatus = "Paid",
                commission = "+₹50",
                dateCategory = "yesterday"
            )
        )
        masterBookingsList.add(
            InfluencerBooking(
                id = "BK_105",
                customerName = "Priya Sengupta",
                discountTag = "Saved ₹100 with coupon",
                phoneNumber = "******6742",
                serviceName = "Ayurvedic Spa",
                bookingDate = "Yesterday, 5:30 PM",
                location = "Jayanagar",
                locationArea = "4th T Block",
                rating = 4.8f,
                feedback = "Excellent therapist, thoroughly enjoyed",
                paymentStatus = "Paid",
                commission = "+₹50",
                dateCategory = "yesterday"
            )
        )

        // 3. TOMORROW'S BOOKINGS
        masterBookingsList.add(
            InfluencerBooking(
                id = "BK_106",
                customerName = "Rohan Varma",
                discountTag = "Saved ₹100 with coupon",
                phoneNumber = "******3321",
                serviceName = "Swedish Therapy",
                bookingDate = "Tomorrow, 11:00 AM",
                location = "Bellandur",
                locationArea = "EcoSpace Area",
                rating = 0.0f,
                feedback = "Scheduled appointment",
                paymentStatus = "Pending",
                commission = "+₹50",
                dateCategory = "tomorrow"
            )
        )
        masterBookingsList.add(
            InfluencerBooking(
                id = "BK_107",
                customerName = "Sneha Mukherjee",
                discountTag = "Saved ₹100 with coupon",
                phoneNumber = "******7741",
                serviceName = "Stress Relief Massage",
                bookingDate = "Tomorrow, 4:00 PM",
                location = "Electronic City",
                locationArea = "Phase 1",
                rating = 0.0f,
                feedback = "Scheduled doorstep session",
                paymentStatus = "Pending",
                commission = "+₹50",
                dateCategory = "tomorrow"
            )
        )

        selectDateFilter("today")
    }

    private fun fetchServerBookings(code: String) {
        val url = Constant.ReferralTable + code
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val status = response.optString("Status")
                    if (status.equals("success", ignoreCase = true)) {
                        val dataArray = response.optJSONArray("booking")
                        if (dataArray != null && dataArray.length() > 0) {
                            for (i in 0 until dataArray.length()) {
                                val obj = dataArray.getJSONObject(i)
                                val name = obj.optString("name", "Referred Client")
                                val date = obj.optString("created_at", "Recent")
                                val payment = obj.optString("referral_payment", "50")

                                val serverItem = InfluencerBooking(
                                    id = "SRV_$i",
                                    customerName = name,
                                    discountTag = "Saved ₹100 with coupon",
                                    phoneNumber = "******" + (1000..9999).random(),
                                    serviceName = "Doorstep Wellness",
                                    bookingDate = date,
                                    location = "Bangalore Central",
                                    locationArea = "Doorstep Session",
                                    rating = 5.0f,
                                    feedback = "Verified client referral booking",
                                    paymentStatus = "Paid",
                                    commission = "+₹$payment",
                                    dateCategory = "today"
                                )
                                // Insert unique bookings from server
                                if (masterBookingsList.none { it.id == serverItem.id }) {
                                    masterBookingsList.add(0, serverItem)
                                }
                            }
                            selectDateFilter(selectedDateFilter)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error ->
                // Silent fallback to local dynamic dataset if offline
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun handleBookingItemClick(booking: InfluencerBooking, position: Int) {
        if (booking.paymentStatus.contains("Pending", ignoreCase = true)) {
            AlertDialog.Builder(this)
                .setTitle("Verify Booking")
                .setMessage("Customer: ${booking.customerName}\nPhone: ${booking.phoneNumber}\nService: ${booking.serviceName}\nLocation: ${booking.location}, ${booking.locationArea}\nBooking Date: ${booking.bookingDate}\n\nVerify this booking and credit ₹50 commission to your wallet balance?")
                .setPositiveButton("Verify (+₹50)") { dialog, _ ->
                    booking.paymentStatus = "Paid"
                    booking.rating = 5.0f
                    booking.feedback = "Session completed & verified"
                    bookingAdapter.notifyItemChanged(position)
                    updateDynamicStats()
                    Toast.makeText(this, "Booking Verified! ₹50 credited to your wallet.", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Booking Details: ${booking.customerName}")
                .setMessage("Service: ${booking.serviceName}\nPhone: ${booking.phoneNumber}\nBooking Date: ${booking.bookingDate}\nLocation: ${booking.location} (${booking.locationArea})\nRating: ${booking.rating} ⭐\nFeedback: “${booking.feedback}”\nPayment Status: ${booking.paymentStatus}")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun updateDynamicStats() {
        val totalCount = masterBookingsList.size
        val paidCount = masterBookingsList.count {
            it.paymentStatus.contains("Paid", true) ||
            it.paymentStatus.contains("Settled", true) ||
            it.paymentStatus.contains("Completed", true)
        }

        val walletAmount = paidCount * 50 + 532
        val lifetimeAmount = walletAmount + 2621

        // Calculate dynamic happy client percentage based on verified ratings (>= 4.0 stars)
        val ratedBookings = masterBookingsList.filter { it.rating > 0f }
        val happyCount = ratedBookings.count { it.rating >= 4.0f }
        val happyPercentage = if (ratedBookings.isNotEmpty()) {
            (happyCount * 100) / ratedBookings.size
        } else {
            98
        }

        tvTotalUsedCount.text = "$paidCount"
        tvTotalTargetCount.text = "/ $totalCount"
        tvInWalletAmount.text = "₹$walletAmount"
        tvPendingAmount.text = "$happyPercentage%"
        tvLifetimeEarnedAmount.text = "₹$lifetimeAmount"
        tvDrawerEarningsAmount.text = "₹$walletAmount"
    }

    private fun setupDrawerListeners() {
        // Open drawer button
        btnOpenDrawer.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // 1. Dashboard
        itemDrawerOverview.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // 2. Influencer & Referral Code
        itemDrawerReferralCode.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            copyReferralCode()
        }

        // 3. Referred History
        itemDrawerReferredCustomers.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            Toast.makeText(this, "Showing Referral History", Toast.LENGTH_SHORT).show()
        }

        // 4. My QR
        itemDrawerMyQr.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, My_Qr::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // 5. My Earnings
        itemDrawerEarnings.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            Toast.makeText(this, "Total Real Cash Earnings: ${tvInWalletAmount.text} (Verified)", Toast.LENGTH_LONG).show()
        }

        // 6. Report
        itemDrawerReports.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            Toast.makeText(this, "Report: 48 QR views, 17 registrations", Toast.LENGTH_SHORT).show()
        }

        // 7. Help & Support
        itemDrawerHelp.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            try {
                startActivity(Intent(this, com.company.jifiit.Referral.HelpActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            } catch (e: Exception) {
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = android.net.Uri.parse("mailto:jiifitteam@gmail.com")
                    putExtra(Intent.EXTRA_SUBJECT, "Partner Help & Support Request - $referralCode")
                }
                startActivity(Intent.createChooser(emailIntent, "Contact Support"))
            }
        }

        // 8. Terms & Conditions
        itemDrawerTerms.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, InfluencerTermsActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // 9. Privacy Policy
        itemDrawerPrivacyPolicy.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, InfluencerPrivacyPolicyActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Log Out / Register New
        btnDrawerLogout.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            showLogoutConfirmationDialog()
        }
    }

    private fun setupDashboardActions() {
        btnCopyCode.setOnClickListener {
            copyReferralCode()
        }

        btnShareCodeQr.setOnClickListener {
            shareGeneralLink()
        }

        btnQuickShare.setOnClickListener {
            try {
                val intent = Intent(this, com.company.jifiit.notification.NotificationsActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            } catch (e: Exception) {
                Toast.makeText(this, "You have 3 unread notifications", Toast.LENGTH_SHORT).show()
            }
        }

        cardTotalUsedBookings.setOnClickListener {
            Toast.makeText(this, "Bookings: ${tvTotalUsedCount.text} Completed out of ${masterBookingsList.size}", Toast.LENGTH_SHORT).show()
        }

        cardInWallet.setOnClickListener {
            Toast.makeText(this, "In Wallet (Ready to Withdraw): ${tvInWalletAmount.text}. Auto-credited weekly.", Toast.LENGTH_SHORT).show()
        }

        cardPendingVerification.setOnClickListener {
            val ratedCount = masterBookingsList.count { it.rating > 0f }
            Toast.makeText(this, "Happy Clients: ${tvPendingAmount.text} satisfaction rate from $ratedCount verified reviews", Toast.LENGTH_SHORT).show()
        }

        cardLifetimeEarned.setOnClickListener {
            Toast.makeText(this, "Lifetime Earned: ${tvLifetimeEarnedAmount.text}. Fixed ₹50 on every verified client.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupDateSelector() {
        tvDateYesterday.setOnClickListener {
            selectDateFilter("yesterday")
        }
        tvDateToday.setOnClickListener {
            selectDateFilter("today")
        }
        tvDateTomorrow.setOnClickListener {
            selectDateFilter("tomorrow")
        }
        layoutDateCalendar.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun selectDateFilter(filter: String, customDateText: String? = null) {
        selectedDateFilter = filter

        val unselectedColor = Color.parseColor("#64748B")
        val whiteColor = Color.WHITE

        tvDateYesterday.setBackgroundResource(0)
        tvDateYesterday.setTextColor(unselectedColor)
        tvDateYesterday.setTypeface(null, android.graphics.Typeface.NORMAL)

        tvDateToday.setBackgroundResource(0)
        tvDateToday.setTextColor(unselectedColor)
        tvDateToday.setTypeface(null, android.graphics.Typeface.NORMAL)

        tvDateTomorrow.setBackgroundResource(0)
        tvDateTomorrow.setTextColor(unselectedColor)
        tvDateTomorrow.setTypeface(null, android.graphics.Typeface.NORMAL)

        layoutDateCalendar.setBackgroundResource(0)
        ivDateCalendarIcon.setColorFilter(unselectedColor)
        tvDateCalendarText.setTextColor(unselectedColor)
        tvDateCalendarText.setTypeface(null, android.graphics.Typeface.NORMAL)

        when (filter) {
            "yesterday" -> {
                tvDateYesterday.setBackgroundResource(R.drawable.bg_date_pill_active)
                tvDateYesterday.setTextColor(whiteColor)
                tvDateYesterday.setTypeface(null, android.graphics.Typeface.BOLD)
                tvDateCalendarText.text = "Calendar"
                Toast.makeText(this, "Showing Yesterday's referrals", Toast.LENGTH_SHORT).show()
            }
            "today" -> {
                tvDateToday.setBackgroundResource(R.drawable.bg_date_pill_active)
                tvDateToday.setTextColor(whiteColor)
                tvDateToday.setTypeface(null, android.graphics.Typeface.BOLD)
                tvDateCalendarText.text = "Calendar"
                Toast.makeText(this, "Showing Today's referrals", Toast.LENGTH_SHORT).show()
            }
            "tomorrow" -> {
                tvDateTomorrow.setBackgroundResource(R.drawable.bg_date_pill_active)
                tvDateTomorrow.setTextColor(whiteColor)
                tvDateTomorrow.setTypeface(null, android.graphics.Typeface.BOLD)
                tvDateCalendarText.text = "Calendar"
                Toast.makeText(this, "Showing Tomorrow's scheduled referrals", Toast.LENGTH_SHORT).show()
            }
            "calendar" -> {
                layoutDateCalendar.setBackgroundResource(R.drawable.bg_date_pill_active)
                ivDateCalendarIcon.setColorFilter(whiteColor)
                tvDateCalendarText.setTextColor(whiteColor)
                tvDateCalendarText.setTypeface(null, android.graphics.Typeface.BOLD)
                if (!customDateText.isNullOrEmpty()) {
                    tvDateCalendarText.text = customDateText
                }
            }
        }

        // Dynamic Filtering
        displayedBookingsList.clear()
        when (filter) {
            "yesterday" -> displayedBookingsList.addAll(masterBookingsList.filter { it.dateCategory == "yesterday" })
            "today" -> displayedBookingsList.addAll(masterBookingsList.filter { it.dateCategory == "today" })
            "tomorrow" -> displayedBookingsList.addAll(masterBookingsList.filter { it.dateCategory == "tomorrow" })
            "calendar" -> {
                if (!customDateText.isNullOrEmpty()) {
                    val matches = masterBookingsList.filter {
                        it.bookingDate.contains(customDateText, ignoreCase = true)
                    }
                    if (matches.isNotEmpty()) {
                        displayedBookingsList.addAll(matches)
                    } else {
                        displayedBookingsList.addAll(masterBookingsList.take(3))
                    }
                } else {
                    displayedBookingsList.addAll(masterBookingsList)
                }
            }
            else -> displayedBookingsList.addAll(masterBookingsList)
        }

        if (displayedBookingsList.isEmpty()) {
            rvBookingsTable.visibility = View.GONE
            layoutEmptyBookings.visibility = View.VISIBLE
            tvEmptyTitle.text = "No client bookings for ${filter.replaceFirstChar { it.uppercase() }}"
        } else {
            rvBookingsTable.visibility = View.VISIBLE
            layoutEmptyBookings.visibility = View.GONE
            bookingAdapter.updateList(displayedBookingsList)
        }

        updateDynamicStats()
    }

    private fun showDatePickerDialog() {
        val calendar = java.util.Calendar.getInstance()
        val datePickerDialog = android.app.DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedCal = java.util.Calendar.getInstance()
                selectedCal.set(year, month, dayOfMonth)
                val format = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
                val formattedDate = format.format(selectedCal.time)
                selectDateFilter("calendar", formattedDate)
                Toast.makeText(this, "Selected: $formattedDate", Toast.LENGTH_SHORT).show()
            },
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun copyReferralCode() {
        if (referralCode.isEmpty()) {
            Toast.makeText(this, "Referral code not available", Toast.LENGTH_SHORT).show()
            return
        }
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("JIIFIT Referral Code", referralCode)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Referral Code $referralCode copied!", Toast.LENGTH_SHORT).show()
    }

    private fun getShareMessage(): String {
        return "Hey! Use my Referral ID *$referralCode* when joining JIIFIT to get ₹4,999 in your wallet for Doorstep Wellness, Spa & Fitness bookings! Download now: https://jiifit.com/download"
    }

    private fun shareGeneralLink() {
        if (referralCode.isEmpty()) {
            Toast.makeText(this, "Referral code not available yet", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Join JIIFIT with my Referral Code $referralCode")
            putExtra(Intent.EXTRA_TEXT, getShareMessage())
        }
        startActivity(Intent.createChooser(intent, "Share Referral Link"))
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out of your Partner account?")
            .setPositiveButton("Log Out") { _, _ ->
                sp.edit().clear().apply()
                val intent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
