package com.company.jifiit

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import androidx.core.widget.NestedScrollView
import org.json.JSONArray
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

    // Working Location Views
    private lateinit var tvWorkingCity: TextView
    private lateinit var nsvWorkingAreas: NestedScrollView
    private lateinit var llWorkingAreasContainer: LinearLayout

    // Approved Services Views
    private lateinit var tvApprovedServicesHeadline: TextView
    private lateinit var tvExperienceLevel: TextView
    private lateinit var nsvApprovedServices: NestedScrollView
    private lateinit var llApprovedServicesContainer: LinearLayout

    // URLs fetched from DB
    private var urlResume: String = ""
    private var urlAadhaarFront: String = ""
    private var urlAadhaarBack: String = ""
    private var url10th: String = ""
    private var url12th: String = ""
    private var urlGrad: String = ""

    private var therapistId: String = ""

    // Polling and auto-refresh (polls every 1 second)
    private val pollHandler = Handler(Looper.getMainLooper())
    private var isPollRequestInProgress = false
    private val pollIntervalMs: Long = 1000L

    private val statusPollingRunnable = object : Runnable {
        override fun run() {
            if (!isFinishing && !isDestroyed) {
                loadRegistrationStatus()
                pollHandler.postDelayed(this, pollIntervalMs)
            }
        }
    }

    // Anti-flicker cached values (only updates UI when backend values change)
    private var lastRenderedName: String = ""
    private var lastRenderedFileResume: String = ""
    private var lastRenderedFileFront: String = ""
    private var lastRenderedFileBack: String = ""
    private var lastRenderedFile10th: String = ""
    private var lastRenderedFile12th: String = ""
    private var lastRenderedFileGrad: String = ""
    private var lastRenderedCityDisplay: String = ""
    private var lastRenderedAreasRaw: String = ""
    private var lastRenderedExpCombined: String = ""
    private var lastRenderedServicesRaw: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_application_submitted)

        setupStatusBar()
        initViews()
        setupListeners()
        loadRegistrationStatus()
    }

    override fun onResume() {
        super.onResume()
        startPolling()
    }

    override fun onPause() {
        super.onPause()
        stopPolling()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPolling()
    }

    private fun startPolling() {
        pollHandler.removeCallbacks(statusPollingRunnable)
        pollHandler.post(statusPollingRunnable)
    }

    private fun stopPolling() {
        pollHandler.removeCallbacks(statusPollingRunnable)
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

        tvWorkingCity = findViewById(R.id.workingCity)
        nsvWorkingAreas = findViewById(R.id.nsvWorkingAreas)
        llWorkingAreasContainer = findViewById(R.id.llWorkingAreasContainer)

        tvApprovedServicesHeadline = findViewById(R.id.tvApprovedServicesHeadline)
        tvExperienceLevel = findViewById(R.id.tvExperienceLevel)
        nsvApprovedServices = findViewById(R.id.nsvApprovedServices)
        llApprovedServicesContainer = findViewById(R.id.llApprovedServicesContainer)

        nsvWorkingAreas.isNestedScrollingEnabled = true
        nsvApprovedServices.isNestedScrollingEnabled = true

        nsvWorkingAreas.setOnTouchListener { v, _ ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }
        nsvApprovedServices.setOnTouchListener { v, _ ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }

        val sp = getSharedPreferences("credentials", MODE_PRIVATE)
        therapistId = intent.getStringExtra("EXTRA_EXPERT_ID") ?: sp.getString("Id", "") ?: ""

        if (therapistId.isNotBlank()) {
            tvAppIdAndDate.text = "Application ID: JIIFIT-$therapistId"
        }
    }

    private fun setupListeners() {
        cardDocResume.setOnClickListener {
            openDocument(urlResume, "Candidate Resume")
        }
        cardDocAadhaarFront.setOnClickListener {
            openDocument(urlAadhaarFront, "Aadhaar Card (Front)")
        }
        cardDocAadhaarBack.setOnClickListener {
            openDocument(urlAadhaarBack, "Aadhaar Card (Back)")
        }
        cardDoc10th.setOnClickListener {
            openDocument(url10th, "10th Certificate")
        }
        cardDoc12th.setOnClickListener {
            openDocument(url12th, "12th Certificate")
        }
        cardDocGrad.setOnClickListener {
            openDocument(urlGrad, "Graduation / Diploma Certificate")
        }

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
                    if (!json.optBoolean("status", false)) {
                        Log.w(TAG, "Tracking failed: " + json.optString("message"))
                    } else {
                        val verification = json.optString("verification", "UNVERIFIED")
                        val l1 = json.optInt("1st_layout", 0)
                        val l2 = json.optInt("2nd_layout", 0)
                        val l3 = json.optInt("3rd_layout", 0)
                        val l4 = json.optInt("4th_layout", 0)
                        val l5 = json.optInt("5th_layout", 0)
                        val kitPayment = json.optString("kit_payment", "").trim()

                        if (verification.equals("VERIFIED", ignoreCase = true)) {
                            stopPolling()
                            if (l1 == 1 && l2 == 1 && l3 == 1 && l4 == 1 && l5 == 1 && kitPayment.equals("pending", ignoreCase = true)) {
                                val intent = Intent(this, TrainingRegistrationKitActivity::class.java)
                                intent.putExtra("EXTRA_EXPERT_ID", therapistId)
                                startActivity(intent)
                                finish()
                            } else {
                                val intent = Intent(this, VerificationCenterActivity::class.java)
                                intent.putExtra("EXTRA_EXPERT_ID", therapistId)
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            val name = json.optString("name", "").trim()
                            if (name.isNotBlank() && name != lastRenderedName) {
                                lastRenderedName = name
                                tvWelcomeName.text = "Welcome to Jiifit, $name!"
                            }

                            val createdAt = json.optString("created_at", "").trim()
                            if (createdAt.isNotBlank()) {
                                tvAppIdAndDate.text = "Application ID: JIIFIT-$therapistId · Submitted on $createdAt"
                            }

                            // Resume
                            val fileResume = json.optString("resume_file", "")
                            if (fileResume != lastRenderedFileResume) {
                                lastRenderedFileResume = fileResume
                                urlResume = json.optString("resume_url", "")
                                if (fileResume.isNotBlank()) {
                                    tvDocResumeFile.text = "File: $fileResume"
                                    tvDocResumeStatus.text = "View Document"
                                } else {
                                    tvDocResumeFile.text = "Not Uploaded"
                                    tvDocResumeStatus.text = "Pending"
                                }
                            }

                            // Aadhaar Front
                            val fileAadhaarFront = json.optString("aadhaar_front_file", "")
                            if (fileAadhaarFront != lastRenderedFileFront) {
                                lastRenderedFileFront = fileAadhaarFront
                                urlAadhaarFront = json.optString("aadhaar_front_url", "")
                                if (fileAadhaarFront.isNotBlank()) {
                                    tvDocAadhaarFrontFile.text = "File: $fileAadhaarFront"
                                    tvDocAadhaarFrontStatus.text = "View Document"
                                } else {
                                    tvDocAadhaarFrontFile.text = "Not Uploaded"
                                    tvDocAadhaarFrontStatus.text = "Pending"
                                }
                            }

                            // Aadhaar Back
                            val fileAadhaarBack = json.optString("aadhaar_back_file", "")
                            if (fileAadhaarBack != lastRenderedFileBack) {
                                lastRenderedFileBack = fileAadhaarBack
                                urlAadhaarBack = json.optString("aadhaar_back_url", "")
                                if (fileAadhaarBack.isNotBlank()) {
                                    tvDocAadhaarBackFile.text = "File: $fileAadhaarBack"
                                    tvDocAadhaarBackStatus.text = "View Document"
                                } else {
                                    tvDocAadhaarBackFile.text = "Not Uploaded"
                                    tvDocAadhaarBackStatus.text = "Pending"
                                }
                            }

                            // 10th Certificate
                            val file10th = json.optString("madhyamik_file", "")
                            if (file10th != lastRenderedFile10th) {
                                lastRenderedFile10th = file10th
                                url10th = json.optString("madhyamik_url", "")
                                if (file10th.isNotBlank()) {
                                    tvDoc10thFile.text = "File: $file10th"
                                    tvDoc10thStatus.text = "View Document"
                                } else {
                                    tvDoc10thFile.text = "Not Uploaded"
                                    tvDoc10thStatus.text = "Pending"
                                }
                            }

                            // 12th Certificate
                            val file12th = json.optString("hs_file", "")
                            if (file12th != lastRenderedFile12th) {
                                lastRenderedFile12th = file12th
                                url12th = json.optString("hs_url", "")
                                if (file12th.isNotBlank()) {
                                    tvDoc12thFile.text = "File: $file12th"
                                    tvDoc12thStatus.text = "View Document"
                                } else {
                                    tvDoc12thFile.text = "Not Uploaded"
                                    tvDoc12thStatus.text = "Optional"
                                }
                            }

                            // Graduation Certificate
                            val fileGrad = json.optString("graduation_file", "")
                            if (fileGrad != lastRenderedFileGrad) {
                                lastRenderedFileGrad = fileGrad
                                urlGrad = json.optString("graduation_url", "")
                                if (fileGrad.isNotBlank()) {
                                    tvDocGradFile.text = "File: $fileGrad"
                                    tvDocGradStatus.text = "View Document"
                                } else {
                                    tvDocGradFile.text = "Not Uploaded"
                                    tvDocGradStatus.text = "Optional"
                                }
                            }

                            // Working Location (City & Areas)
                            val cityName = json.optString("city_name", "").trim()
                            val cityId = json.optString("city_id", "").trim()
                            val cityDisplay = if (cityName.isNotBlank()) "City: $cityName" else if (cityId.isNotBlank()) "City: $cityId" else ""
                            if (cityDisplay.isNotBlank() && cityDisplay != lastRenderedCityDisplay) {
                                lastRenderedCityDisplay = cityDisplay
                                tvWorkingCity.text = cityDisplay
                            }

                            val rawAreaIds = json.optString("area_ids", "").trim()
                            val areasArray = json.optJSONArray("areas")
                            val areasSignature = if (areasArray != null) areasArray.toString() else rawAreaIds
                            if (areasSignature != lastRenderedAreasRaw) {
                                lastRenderedAreasRaw = areasSignature
                                llWorkingAreasContainer.removeAllViews()
                                var areasCount = 0
                                if (areasArray != null && areasArray.length() > 0) {
                                    for (i in 0 until areasArray.length()) {
                                        val areaName = areasArray.optString(i, "").trim()
                                        if (areaName.isNotBlank()) {
                                            areasCount++
                                            val tvArea = TextView(this@ApplicationSubmittedActivity).apply {
                                                layoutParams = LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                                ).apply {
                                                    topMargin = if (areasCount == 1) dpToPx(14) else dpToPx(10)
                                                }
                                                setBackgroundResource(R.drawable.sub_round)
                                                backgroundTintList = ColorStateList.valueOf(
                                                    ContextCompat.getColor(context, R.color.sub_grey_bg)
                                                )
                                                setPadding(dpToPx(14), dpToPx(14), dpToPx(14), dpToPx(14))
                                                text = "📍 $areaName"
                                                setTextColor(ContextCompat.getColor(context, R.color.sub_text))
                                                textSize = 15f
                                            }
                                            llWorkingAreasContainer.addView(tvArea)
                                        }
                                    }
                                } else {
                                    val cleanAreas = rawAreaIds.replace("[", "").replace("]", "").replace("\"", "").replace("'", "")
                                    val tokens = cleanAreas.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                    if (tokens.isNotEmpty()) {
                                        for (i in tokens.indices) {
                                            areasCount++
                                            val tvArea = TextView(this@ApplicationSubmittedActivity).apply {
                                                layoutParams = LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                                ).apply {
                                                    topMargin = if (i == 0) dpToPx(14) else dpToPx(10)
                                                }
                                                setBackgroundResource(R.drawable.sub_round)
                                                backgroundTintList = ColorStateList.valueOf(
                                                    ContextCompat.getColor(context, R.color.sub_grey_bg)
                                                )
                                                setPadding(dpToPx(14), dpToPx(14), dpToPx(14), dpToPx(14))
                                                text = "📍 Operating Hub (${tokens[i]})"
                                                setTextColor(ContextCompat.getColor(context, R.color.sub_text))
                                                textSize = 15f
                                            }
                                            llWorkingAreasContainer.addView(tvArea)
                                        }
                                    }
                                }

                                // Make llWorkingAreasContainer scrollable when multiple areas exist
                                if (areasCount > 3) {
                                    nsvWorkingAreas.layoutParams = (nsvWorkingAreas.layoutParams as LinearLayout.LayoutParams).apply {
                                        height = dpToPx(190)
                                    }
                                } else {
                                    nsvWorkingAreas.layoutParams = (nsvWorkingAreas.layoutParams as LinearLayout.LayoutParams).apply {
                                        height = LinearLayout.LayoutParams.WRAP_CONTENT
                                    }
                                }
                            }

                            // Experience Level
                            val expRaw = json.optString("experience", "").trim()
                            val expDesc = json.optString("experience_desc", "").trim()
                            val expSignature = "$expRaw|$expDesc"
                            if (expSignature != lastRenderedExpCombined) {
                                lastRenderedExpCombined = expSignature
                                val formattedExp = when {
                                    expRaw.isEmpty() -> if (expDesc.isNotEmpty()) "Experience: $expDesc" else "Experience Level: Not specified"
                                    expRaw == "0" || expRaw.equals("Fresher", ignoreCase = true) || expRaw.equals("Freshers", ignoreCase = true) -> "Experience Level: Freshers"
                                    expRaw.toIntOrNull() != null -> {
                                        val y = expRaw.toInt()
                                        "Experience Level: $y ${if (y == 1) "Year" else "Years"}"
                                    }
                                    expRaw.lowercase().contains("year") -> "Experience Level: $expRaw"
                                    else -> "Experience Level: $expRaw Years"
                                }
                                tvExperienceLevel.text = formattedExp
                            }

                            // Approved / Selected Services (Dynamic from selected_services)
                            val rawServices = json.optString("selected_services", "").trim()
                            val rawServicesObj = json.opt("selected_services")?.toString() ?: ""
                            if (rawServicesObj != lastRenderedServicesRaw) {
                                lastRenderedServicesRaw = rawServicesObj
                                llApprovedServicesContainer.removeAllViews()
                                val servicesList = mutableListOf<String>()

                                if (json.has("selected_services") && json.optJSONArray("selected_services") != null) {
                                    val arr = json.optJSONArray("selected_services")!!
                                    for (i in 0 until arr.length()) {
                                        val s = arr.optString(i, "").trim()
                                        if (s.isNotEmpty()) servicesList.add(s)
                                    }
                                } else if (rawServices.isNotEmpty()) {
                                    if (rawServices.startsWith("[") && rawServices.endsWith("]")) {
                                        try {
                                            val arr = JSONArray(rawServices)
                                            for (i in 0 until arr.length()) {
                                                val s = arr.optString(i, "").trim()
                                                if (s.isNotEmpty()) servicesList.add(s)
                                            }
                                        } catch (_: Exception) {
                                            val cleanStr = rawServices.replace("[", "").replace("]", "").replace("\"", "").replace("'", "")
                                            servicesList.addAll(cleanStr.split(",").map { it.trim() }.filter { it.isNotEmpty() })
                                        }
                                    } else {
                                        servicesList.addAll(rawServices.split(",").map { it.trim() }.filter { it.isNotEmpty() })
                                    }
                                }

                                if (servicesList.isNotEmpty()) {
                                    tvApprovedServicesHeadline.text = "✨ APPROVED SERVICES (${servicesList.size})"
                                    for (i in servicesList.indices) {
                                        val serviceName = servicesList[i]
                                        val tvService = TextView(this@ApplicationSubmittedActivity).apply {
                                            layoutParams = LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                            ).apply {
                                                topMargin = if (i == 0) dpToPx(14) else dpToPx(10)
                                            }
                                            setBackgroundResource(R.drawable.sub_round)
                                            backgroundTintList = ColorStateList.valueOf(
                                                ContextCompat.getColor(context, R.color.sub_blue_bg)
                                            )
                                            setPadding(dpToPx(14), dpToPx(14), dpToPx(14), dpToPx(14))
                                            text = "✓ $serviceName"
                                            setTextColor(ContextCompat.getColor(context, R.color.sub_navy_chip))
                                            textSize = 15f
                                        }
                                        llApprovedServicesContainer.addView(tvService)
                                    }

                                    if (servicesList.size > 4) {
                                        nsvApprovedServices.layoutParams = (nsvApprovedServices.layoutParams as LinearLayout.LayoutParams).apply {
                                            height = dpToPx(240)
                                        }
                                    } else {
                                        nsvApprovedServices.layoutParams = (nsvApprovedServices.layoutParams as LinearLayout.LayoutParams).apply {
                                            height = LinearLayout.LayoutParams.WRAP_CONTENT
                                        }
                                    }
                                } else {
                                    tvApprovedServicesHeadline.text = "✨ APPROVED SERVICES"
                                    val tvNoService = TextView(this@ApplicationSubmittedActivity).apply {
                                        layoutParams = LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT
                                        ).apply {
                                            topMargin = dpToPx(14)
                                        }
                                        setBackgroundResource(R.drawable.sub_round)
                                        backgroundTintList = ColorStateList.valueOf(
                                            ContextCompat.getColor(context, R.color.sub_blue_bg)
                                        )
                                        setPadding(dpToPx(14), dpToPx(14), dpToPx(14), dpToPx(14))
                                        text = "No services selected yet"
                                        setTextColor(ContextCompat.getColor(context, R.color.sub_navy_chip))
                                        textSize = 15f
                                    }
                                    llApprovedServicesContainer.addView(tvNoService)
                                    nsvApprovedServices.layoutParams = (nsvApprovedServices.layoutParams as LinearLayout.LayoutParams).apply {
                                        height = LinearLayout.LayoutParams.WRAP_CONTENT
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing tracking response", e)
                }
            }
        },
            { error ->
                isPollRequestInProgress = false
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

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
}
