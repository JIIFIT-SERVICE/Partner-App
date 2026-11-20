package com.company.jifiit

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.company.jifiit.Referral.HelpActivity

class TrainingPaymentSuccessActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var tvRegistrationStatus: TextView
    private lateinit var tvTransactionId: TextView
    private lateinit var btnHelpSupport: Button
    private var tvRedirectCountdown: TextView? = null

    private var helpPhoneNumber: String = DEFAULT_HELP_PHONE
    private var redirectTimer: CountDownTimer? = null
    private var isRedirected = false

    companion object {
        const val EXTRA_TRANSACTION_ID = "EXTRA_TRANSACTION_ID"
        const val EXTRA_REGISTRATION_STATUS = "EXTRA_REGISTRATION_STATUS"
        const val EXTRA_MANAGER_PHONE = "EXTRA_MANAGER_PHONE"
        const val DEFAULT_HELP_PHONE = "+911140509900"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training_payment_success)

        // Clear user credentials from SharedPreferences upon payment success
        clearUserCredentials()

        setupStatusBar()
        initViews()
        populateData()
        setupListeners()
        start10SecondRedirectTimer()
    }

    private fun clearUserCredentials() {
        val sp = getSharedPreferences("credentials", Context.MODE_PRIVATE)
        sp.edit().clear().apply()
    }

    private fun setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = Color.parseColor("#FFFFFF")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvRegistrationStatus = findViewById(R.id.tvRegistrationStatus)
        tvTransactionId = findViewById(R.id.tvTransactionId)
        btnHelpSupport = findViewById(R.id.btnHelpSupport)
        tvRedirectCountdown = findViewById(R.id.tvRedirectCountdown)
    }

    private fun populateData() {
        val txnId = intent.getStringExtra(EXTRA_TRANSACTION_ID)
        if (!txnId.isNullOrBlank()) {
            tvTransactionId.text = txnId
        } else {
            val randomSuffix = (100000000..999999999).random()
            tvTransactionId.text = "TXN$randomSuffix"
        }

        val regStatus = intent.getStringExtra(EXTRA_REGISTRATION_STATUS)
        if (!regStatus.isNullOrBlank()) {
            tvRegistrationStatus.text = regStatus
        } else {
            tvRegistrationStatus.text = "Confirmed"
        }

        val customPhone = intent.getStringExtra(EXTRA_MANAGER_PHONE)
        if (!customPhone.isNullOrBlank()) {
            helpPhoneNumber = customPhone
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            handleBackNavigation()
        }

        btnHelpSupport.setOnClickListener {
            openHelpAndSupport()
        }
    }

    /**
     * Shows payment success for 10 seconds then automatically redirects to LoginActivity.
     */
    private fun start10SecondRedirectTimer() {
        redirectTimer?.cancel()
        redirectTimer = object : CountDownTimer(10000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000L) + 1
                tvRedirectCountdown?.text = "Redirecting to Login in ${secondsRemaining}s..."
            }

            override fun onFinish() {
                tvRedirectCountdown?.text = "Redirecting to Login..."
                redirectToLogin()
            }
        }.start()
    }

    private fun redirectToLogin() {
        if (isRedirected) return
        isRedirected = true
        redirectTimer?.cancel()

        // Double check credentials are fully cleared
        clearUserCredentials()

        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun openHelpAndSupport() {
        try {
            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        } catch (e: Exception) {
            try {
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$helpPhoneNumber")
                }
                startActivity(dialIntent)
            } catch (ex: Exception) {
                Toast.makeText(this, "Help & Support contact: $helpPhoneNumber", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleBackNavigation() {
        redirectToLogin()
    }

    override fun onBackPressed() {
        redirectToLogin()
    }

    override fun onDestroy() {
        super.onDestroy()
        redirectTimer?.cancel()
    }
}
