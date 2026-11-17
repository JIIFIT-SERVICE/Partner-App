package com.company.jifiit

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ApplicationSubmittedActivity : AppCompatActivity() {

    private val TAG = "AppSubmittedActivity"

    private lateinit var tvWelcomeName: TextView
    private lateinit var tvAppIdAndDate: TextView
    private lateinit var btnEditDetails: TextView
    private lateinit var btnDone: TextView
    private lateinit var btnContactSupport: TextView

    // Document Cards
    private lateinit var cardDocResume: LinearLayout
    private lateinit var tvDocResumeFile: TextView
    private lateinit var tvDocResumeStatus: TextView

    private lateinit var cardDocAadhaarFront: LinearLayout
    private lateinit var tvDocAadhaarFrontFile: TextView
    private lateinit var tvDocAadhaarFrontStatus: TextView

    private lateinit var cardDocAadhaarBack: LinearLayout
    private lateinit var tvDocAadhaarBackFile: TextView
    private lateinit var tvDocAadhaarBackStatus: TextView

    private lateinit var cardDoc10th: LinearLayout
    private lateinit var tvDoc10thFile: TextView
    private lateinit var tvDoc10thStatus: TextView

    private lateinit var cardDoc12th: LinearLayout
    private lateinit var tvDoc12thFile: TextView
    private lateinit var tvDoc12thStatus: TextView

    private lateinit var cardDocGrad: LinearLayout
    private lateinit var tvDocGradFile: TextView
    private lateinit var tvDocGradStatus: TextView

    // URLs fetched from DB
    private var urlResume: String = ""
    private var urlAadhaarFront: String = ""
    private var urlAadhaarBack: String = ""
    private var url10th: String = ""
    private var url12th: String = ""
    private var urlGrad: String = ""

    private var therapistId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_application_submitted)

        setupStatusBar()
        initViews()
        setupListeners()
        loadRegistrationStatus()
    }

    private fun setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = Color.parseColor("#F8F9FD")
        }
    }

    private fun initViews() {
        tvWelcomeName = findViewById(R.id.tvWelcomeName)
        tvAppIdAndDate = findViewById(R.id.tvAppIdAndDate)
        btnEditDetails = findViewById(R.id.btnEditDetails)
        btnDone = findViewById(R.id.btnDone)
        btnContactSupport = findViewById(R.id.btnContactSupport)

        cardDocResume = findViewById(R.id.cardDocResume)
        tvDocResumeFile = findViewById(R.id.tvDocResumeFile)
        tvDocResumeStatus = findViewById(R.id.tvDocResumeStatus)

        cardDocAadhaarFront = findViewById(R.id.cardDocAadhaarFront)
        tvDocAadhaarFrontFile = findViewById(R.id.tvDocAadhaarFrontFile)
        tvDocAadhaarFrontStatus = findViewById(R.id.tvDocAadhaarFrontStatus)

        cardDocAadhaarBack = findViewById(R.id.cardDocAadhaarBack)
        tvDocAadhaarBackFile = findViewById(R.id.tvDocAadhaarBackFile)
        tvDocAadhaarBackStatus = findViewById(R.id.tvDocAadhaarBackStatus)

        cardDoc10th = findViewById(R.id.cardDoc10th)
        tvDoc10thFile = findViewById(R.id.tvDoc10thFile)
        tvDoc10thStatus = findViewById(R.id.tvDoc10thStatus)

        cardDoc12th = findViewById(R.id.cardDoc12th)
        tvDoc12thFile = findViewById(R.id.tvDoc12thFile)
        tvDoc12thStatus = findViewById(R.id.tvDoc12thStatus)

        cardDocGrad = findViewById(R.id.cardDocGrad)
        tvDocGradFile = findViewById(R.id.tvDocGradFile)
        tvDocGradStatus = findViewById(R.id.tvDocGradStatus)

        val sp = getSharedPreferences("credentials", MODE_PRIVATE)
        therapistId = intent.getStringExtra("EXTRA_EXPERT_ID") ?: sp.getString("Id", "") ?: ""

        if (therapistId.isNotBlank()) {
            tvAppIdAndDate.text = "Application ID: JIIFIT-$therapistId"
        }
    }

    private fun setupListeners() {
        cardDocResume.setOnClickListener { openDocument(urlResume, "Candidate Resume") }
        cardDocAadhaarFront.setOnClickListener { openDocument(urlAadhaarFront, "Aadhaar Card (Front)") }
        cardDocAadhaarBack.setOnClickListener { openDocument(urlAadhaarBack, "Aadhaar Card (Back)") }
        cardDoc10th.setOnClickListener { openDocument(url10th, "10th Certificate") }
        cardDoc12th.setOnClickListener { openDocument(url12th, "12th Certificate") }
        cardDocGrad.setOnClickListener { openDocument(urlGrad, "Graduation / Diploma Certificate") }

        btnEditDetails.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
            finish()
        }

        btnDone.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }

        btnContactSupport.setOnClickListener {
            try {
                val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919717550414"))
                startActivity(callIntent)
            } catch (e: Exception) {
                Toast.makeText(this, "Support: +919717550414", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadRegistrationStatus() {
        if (therapistId.isBlank()) {
            Log.w(TAG, "No therapist ID found to track status")
            return
        }

        val request = object : StringRequest(
            Request.Method.POST,
            Constant.TrackRegistrationStatusUrl,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (!json.optBoolean("status", false)) {
                        Log.w(TAG, "Tracking failed: " + json.optString("message"))
                    } else {
                        val verification = json.optString("verification", "UNVERIFIED")
                        if (verification.equals("VERIFIED", ignoreCase = true)) {
                            val intent = Intent(this, VerificationCenterActivity::class.java)
                            intent.putExtra("EXTRA_EXPERT_ID", therapistId)
                            startActivity(intent)
                            finish()
                        } else {
                            val name = json.optString("name", "")
                            if (name.isNotBlank()) {
                                tvWelcomeName.text = "Welcome to Jiifit, $name!"
                            }

                            val createdAt = json.optString("created_at", "")
                            if (createdAt.isNotBlank()) {
                                tvAppIdAndDate.text = "Application ID: JIIFIT-$therapistId · Submitted on $createdAt"
                            }

                            // Resume
                            urlResume = json.optString("resume_url", "")
                            val fileResume = json.optString("resume_file", "")
                            if (fileResume.isNotBlank()) {
                                tvDocResumeFile.text = "File: $fileResume"
                                tvDocResumeStatus.text = "View Document"
                            } else {
                                tvDocResumeFile.text = "Not Uploaded"
                                tvDocResumeStatus.text = "Pending"
                            }

                            // Aadhaar Front
                            urlAadhaarFront = json.optString("aadhaar_front_url", "")
                            val fileAadhaarFront = json.optString("aadhaar_front_file", "")
                            if (fileAadhaarFront.isNotBlank()) {
                                tvDocAadhaarFrontFile.text = "File: $fileAadhaarFront"
                                tvDocAadhaarFrontStatus.text = "View Document"
                            } else {
                                tvDocAadhaarFrontFile.text = "Not Uploaded"
                                tvDocAadhaarFrontStatus.text = "Pending"
                            }

                            // Aadhaar Back
                            urlAadhaarBack = json.optString("aadhaar_back_url", "")
                            val fileAadhaarBack = json.optString("aadhaar_back_file", "")
                            if (fileAadhaarBack.isNotBlank()) {
                                tvDocAadhaarBackFile.text = "File: $fileAadhaarBack"
                                tvDocAadhaarBackStatus.text = "View Document"
                            } else {
                                tvDocAadhaarBackFile.text = "Not Uploaded"
                                tvDocAadhaarBackStatus.text = "Pending"
                            }

                            // 10th Certificate
                            url10th = json.optString("madhyamik_url", "")
                            val file10th = json.optString("madhyamik_file", "")
                            if (file10th.isNotBlank()) {
                                tvDoc10thFile.text = "File: $file10th"
                                tvDoc10thStatus.text = "View Document"
                            } else {
                                tvDoc10thFile.text = "Not Uploaded"
                                tvDoc10thStatus.text = "Pending"
                            }

                            // 12th Certificate
                            url12th = json.optString("hs_url", "")
                            val file12th = json.optString("hs_file", "")
                            if (file12th.isNotBlank()) {
                                tvDoc12thFile.text = "File: $file12th"
                                tvDoc12thStatus.text = "View Document"
                            } else {
                                tvDoc12thFile.text = "Not Uploaded"
                                tvDoc12thStatus.text = "Optional"
                            }

                            // Graduation Certificate
                            urlGrad = json.optString("graduation_url", "")
                            val fileGrad = json.optString("graduation_file", "")
                            if (fileGrad.isNotBlank()) {
                                tvDocGradFile.text = "File: $fileGrad"
                                tvDocGradStatus.text = "View Document"
                            } else {
                                tvDocGradFile.text = "Not Uploaded"
                                tvDocGradStatus.text = "Optional"
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing tracking response", e)
                }
            },
            { error ->
                Log.e(TAG, "Failed to track registration status: " + error.message)
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id"] = therapistId
                return params
            }
        }

        request.retryPolicy = DefaultRetryPolicy(15000, 0, 1.0f)
        Volley.newRequestQueue(this).add(request)
    }

    private fun openDocument(url: String, docName: String) {
        if (url.isBlank()) {
            Toast.makeText(this, "$docName was not uploaded or is not available.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Cannot open document URL: $url", e)
            Toast.makeText(this, "No browser or PDF viewer available to open this document.", Toast.LENGTH_SHORT).show()
        }
    }
}
