package com.company.jifiit.Influencer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.company.jifiit.R
import com.google.android.material.button.MaterialButton

class InfluencerPrivacyPolicyActivity : AppCompatActivity() {

    private lateinit var headerContainer: LinearLayout
    private lateinit var privacyFooterContainer: LinearLayout
    private lateinit var btnBack: ImageView
    private lateinit var btnAccept: MaterialButton
    private lateinit var btnSupportEmail: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_influencer_privacy_policy)

        // Configure Status Bar for dark gradient header
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = false
        windowInsetsController.isAppearanceLightNavigationBars = true

        initializeViews()
        applyWindowInsets()
        setupListeners()
    }

    private fun initializeViews() {
        headerContainer = findViewById(R.id.headerContainer)
        privacyFooterContainer = findViewById(R.id.privacyFooterContainer)
        btnBack = findViewById(R.id.btnBack)
        btnAccept = findViewById(R.id.btnAccept)
        btnSupportEmail = findViewById(R.id.btnSupportEmail)
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            headerContainer.setPadding(
                headerContainer.paddingStart,
                systemBars.top + 16,
                headerContainer.paddingEnd,
                headerContainer.paddingBottom
            )
            privacyFooterContainer.setPadding(
                privacyFooterContainer.paddingStart,
                privacyFooterContainer.paddingTop,
                privacyFooterContainer.paddingEnd,
                systemBars.bottom + 12
            )
            insets
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finishActivityWithAnimation()
        }

        btnAccept.setOnClickListener {
            finishActivityWithAnimation()
        }

        btnSupportEmail.setOnClickListener {
            val email = "jiifitteam@gmail.com"
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, "JIIFIT Partner Program Privacy Inquiry")
            }
            try {
                startActivity(Intent.createChooser(intent, "Contact Support"))
            } catch (e: Exception) {
                Toast.makeText(this, "Email: $email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun finishActivityWithAnimation() {
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}

