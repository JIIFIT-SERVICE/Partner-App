package com.company.jifiit.Influencer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
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
import com.company.jifiit.LoginActivity
import com.company.jifiit.MyQr.My_Qr
import com.company.jifiit.R
import com.google.android.material.button.MaterialButton
import de.hdodenhof.circleimageview.CircleImageView

class InfluencerMainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var headerContainer: LinearLayout

    // Dashboard Views
    private lateinit var btnOpenDrawer: ImageView
    private lateinit var btnQuickShare: ImageView
    private lateinit var tvMainGreeting: TextView
    private lateinit var tvMainRoleSubtitle: TextView
    private lateinit var tvReferralCodeDisplay: TextView
    private lateinit var btnCopyCode: MaterialButton
    private lateinit var btnShareWhatsapp: MaterialButton
    private lateinit var btnShareGeneral: MaterialButton

    // Drawer Views (matching screenshot)
    private lateinit var btnDrawerClose: ImageView
    private lateinit var tvDrawerUserName: TextView
    private lateinit var tvDrawerUserEmail: TextView
    private lateinit var tvDrawerCategoryBadge: TextView
    private lateinit var tvDrawerUserInitials: TextView
    private lateinit var ivDrawerProfilePhoto: CircleImageView
    private lateinit var ivDrawerEditProfile: View
    private lateinit var tvActiveProperty: TextView
    private lateinit var tvActiveProfile: TextView
    private lateinit var tvDrawerReferralCodeBadge: TextView
    private lateinit var tvDrawerReferredCategoryBadge: TextView
    private lateinit var tvDrawerEarningsAmount: TextView
    private lateinit var tvDrawerAppVersion: TextView

    // Drawer Items
    private lateinit var itemDrawerOverview: LinearLayout
    private lateinit var itemDrawerReferralCode: LinearLayout
    private lateinit var itemDrawerReferredCustomers: LinearLayout
    private lateinit var itemDrawerMyQr: LinearLayout
    private lateinit var itemDrawerEarnings: LinearLayout
    private lateinit var itemDrawerReports: LinearLayout
    private lateinit var btnDrawerLogout: LinearLayout

    // Quick Tool Cards on Dashboard
    private lateinit var cardReferredCustomers: LinearLayout
    private lateinit var cardReports: LinearLayout
    private lateinit var cardMyQr: LinearLayout
    private lateinit var cardPartnerTerms: LinearLayout

    private lateinit var sp: SharedPreferences
    private var referralCode: String = "SOMN0004"
    private var partnerName: String = "Somnath Ray"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_influencer_main)

        // Configure Status Bar for dark gradient header
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = false
        windowInsetsController.isAppearanceLightNavigationBars = true

        sp = getSharedPreferences("credentials", Context.MODE_PRIVATE)

        initializeViews()
        applyWindowInsets()
        loadPartnerData()
        setupDrawerListeners()
        setupDashboardActions()
    }

    private fun initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        headerContainer = findViewById(R.id.headerContainer)

        // Dashboard views
        btnOpenDrawer = findViewById(R.id.btnOpenDrawer)
        btnQuickShare = findViewById(R.id.btnQuickShare)
        tvMainGreeting = findViewById(R.id.tvMainGreeting)
        tvMainRoleSubtitle = findViewById(R.id.tvMainRoleSubtitle)
        tvReferralCodeDisplay = findViewById(R.id.tvReferralCodeDisplay)
        btnCopyCode = findViewById(R.id.btnCopyCode)
        btnShareWhatsapp = findViewById(R.id.btnShareWhatsapp)
        btnShareGeneral = findViewById(R.id.btnShareGeneral)

        // Quick cards on Dashboard
        cardReferredCustomers = findViewById(R.id.cardReferredCustomers)
        cardReports = findViewById(R.id.cardReports)
        cardMyQr = findViewById(R.id.cardMyQr)
        cardPartnerTerms = findViewById(R.id.cardPartnerTerms)

        // Drawer views
        btnDrawerClose = findViewById(R.id.btnDrawerClose)
        tvDrawerUserName = findViewById(R.id.tvDrawerUserName)
        tvDrawerUserEmail = findViewById(R.id.tvDrawerUserEmail)
        tvDrawerCategoryBadge = findViewById(R.id.tvDrawerCategoryBadge)
        tvDrawerUserInitials = findViewById(R.id.tvDrawerUserInitials)
        ivDrawerProfilePhoto = findViewById(R.id.ivDrawerProfilePhoto)
        ivDrawerEditProfile = findViewById(R.id.ivDrawerEditProfile)
        tvActiveProperty = findViewById(R.id.tvActiveProperty)
        tvActiveProfile = findViewById(R.id.tvActiveProfile)
        tvDrawerReferralCodeBadge = findViewById(R.id.tvDrawerReferralCodeBadge)
        tvDrawerReferredCategoryBadge = findViewById(R.id.tvDrawerReferredCategoryBadge)
        tvDrawerEarningsAmount = findViewById(R.id.tvDrawerEarningsAmount)
        tvDrawerAppVersion = findViewById(R.id.tvDrawerAppVersion)

        // Drawer Menu items
        itemDrawerOverview = findViewById(R.id.itemDrawerOverview)
        itemDrawerReferralCode = findViewById(R.id.itemDrawerReferralCode)
        itemDrawerReferredCustomers = findViewById(R.id.itemDrawerReferredCustomers)
        itemDrawerMyQr = findViewById(R.id.itemDrawerMyQr)
        itemDrawerEarnings = findViewById(R.id.itemDrawerEarnings)
        itemDrawerReports = findViewById(R.id.itemDrawerReports)
        btnDrawerLogout = findViewById(R.id.btnDrawerLogout)
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            headerContainer.setPadding(
                headerContainer.paddingStart,
                systemBars.top + 14,
                headerContainer.paddingEnd,
                headerContainer.paddingBottom
            )
            insets
        }
    }

    private fun loadPartnerData() {
        val savedName = sp.getString("Name", null)
        if (!savedName.isNullOrEmpty() && savedName != "null") {
            partnerName = savedName
        }

        // Generate initials (e.g. "Alex Das" -> "AD", "Somnath Ray" -> "SR")
        val initials = generateInitials(partnerName)
        tvDrawerUserInitials.text = initials
        tvDrawerUserName.text = partnerName

        val savedEmail = sp.getString("email", sp.getString("Email", null))
        if (!savedEmail.isNullOrEmpty() && savedEmail != "null") {
            tvDrawerUserEmail.text = savedEmail
        } else {
            val emailPrefix = partnerName.lowercase().replace("\\s+".toRegex(), "")
            tvDrawerUserEmail.text = if (emailPrefix.isNotEmpty()) "$emailPrefix@gmail.com" else "alexdas@gmail.com"
        }

        val savedCategory = sp.getString("partnerCategory", sp.getString("Category", null))
        if (!savedCategory.isNullOrEmpty() && savedCategory != "null") {
            tvDrawerCategoryBadge.text = savedCategory
        } else {
            tvDrawerCategoryBadge.text = "YouTuber / Content Creator"
        }

        tvMainGreeting.text = "Welcome back, ${partnerName.split(" ").firstOrNull() ?: partnerName}!"
        tvActiveProfile.text = "${partnerName.split(" ").firstOrNull() ?: partnerName} (Partner)"
        tvActiveProperty.text = "Grand Orchid ..."

        // Build Referral Code (e.g., SOMN0004)
        val cleanNamePrefix = partnerName.replace(" ", "").take(4).uppercase()
        val savedId = sp.getString("Id", "0004") ?: "0004"
        val codeId = if (savedId.length >= 4) savedId.takeLast(4) else savedId.padStart(4, '0')
        referralCode = if (cleanNamePrefix.length == 4) "$cleanNamePrefix$codeId" else "SOMN$codeId"

        tvReferralCodeDisplay.text = referralCode
        tvDrawerReferralCodeBadge.text = referralCode
        tvDrawerReferredCategoryBadge.text = "Massage & Spa"
        tvDrawerEarningsAmount.text = "₹732"
    }

    private fun generateInitials(name: String): String {
        val parts = name.trim().split("\\s+".toRegex())
        return when {
            parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
            parts.isNotEmpty() && parts[0].isNotEmpty() -> parts[0].take(2).uppercase()
            else -> "AD"
        }
    }

    private fun setupDrawerListeners() {
        // Open & close drawer buttons
        btnOpenDrawer.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        btnDrawerClose.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
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

        // 3. Referred History (Renamed from Referred Customers)
        itemDrawerReferredCustomers.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            Toast.makeText(this, "Referred History: 14 Active bookings, 3 Pending", Toast.LENGTH_SHORT).show()
        }

        // 4. My QR (Added into list as requested)
        itemDrawerMyQr.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, My_Qr::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // 5. My Earnings
        itemDrawerEarnings.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            Toast.makeText(this, "Total Real Cash Earnings: ₹732 (Verified)", Toast.LENGTH_LONG).show()
        }

        // 6. Report (Renamed from Reports & Standee Scans)
        itemDrawerReports.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            Toast.makeText(this, "Report: 48 QR views, 17 registrations", Toast.LENGTH_SHORT).show()
        }

        // Edit Profile Click on Avatar badge
        ivDrawerEditProfile.setOnClickListener {
            Toast.makeText(this, "Partner Profile: $partnerName", Toast.LENGTH_SHORT).show()
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

        btnShareWhatsapp.setOnClickListener {
            shareOnWhatsApp()
        }

        btnShareGeneral.setOnClickListener {
            shareGeneralLink()
        }

        btnQuickShare.setOnClickListener {
            shareGeneralLink()
        }

        cardReferredCustomers.setOnClickListener {
            Toast.makeText(this, "Referred History: 14 Completed, 3 Pending", Toast.LENGTH_SHORT).show()
        }

        cardReports.setOnClickListener {
            Toast.makeText(this, "Report: 48 Total QR Scans & Registrations", Toast.LENGTH_SHORT).show()
        }

        cardMyQr.setOnClickListener {
            startActivity(Intent(this, My_Qr::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        cardPartnerTerms.setOnClickListener {
            startActivity(Intent(this, InfluencerTermsActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun copyReferralCode() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("JIIFIT Referral Code", referralCode)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Referral Code $referralCode copied!", Toast.LENGTH_SHORT).show()
    }

    private fun getShareMessage(): String {
        return "Hey! Use my Referral ID *$referralCode* when joining JIIFIT to get ₹4,999 in your wallet for Doorstep Wellness, Spa & Fitness bookings! Download now: https://jiifit.com/download"
    }

    private fun shareOnWhatsApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            setPackage("com.whatsapp")
            putExtra(Intent.EXTRA_TEXT, getShareMessage())
        }
        try {
            startActivity(shareIntent)
        } catch (e: Exception) {
            shareGeneralLink()
        }
    }

    private fun shareGeneralLink() {
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
