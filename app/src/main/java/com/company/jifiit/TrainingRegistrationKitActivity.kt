package com.company.jifiit

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.phonepe.intent.sdk.api.B2BPGRequestBuilder
import com.phonepe.intent.sdk.api.PhonePe
import com.phonepe.intent.sdk.api.models.PhonePeEnvironment
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.Locale

class TrainingRegistrationKitActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var rvMassageList: RecyclerView
    private lateinit var btnProcessFees: RelativeLayout
    private lateinit var tvTotalPayableAmount: TextView

    private lateinit var cardMassageBed: LinearLayout
    private lateinit var cbMassageBed: AppCompatCheckBox
    private lateinit var tvPriceBed: TextView
    private lateinit var tvPriceKitSummary: TextView
    private lateinit var tvBedSummaryStatus: TextView
    private lateinit var tvPriceTraining: TextView
    private lateinit var tvPriceUniform: TextView
    private lateinit var tvPriceOtherKit: TextView

    private val defaultTrainingPrice = 599
    private val defaultUniformPrice = 1399
    private val defaultOtherKitPrice = 799
    private val defaultBedPrice = 20000

    private var therapistId: String = ""
    private var massageAdapter: MassageStepAdapter? = null
    private val massageItems = mutableListOf<MassageStepItem>()

    // PhonePe Payment Gateway Constants
    private val MERCHANT_ID = "JIIFITONLINE"
    private val SALT_KEY = "518d0d50-2f8e-455f-8cc1-b2bc0e981bb2"
    private val SALT_INDEX = "1"
    private val B2B_PG_URL = "/pg/v1/pay"
    private val PHONEPE_REQUEST_CODE = 200

    private var currentTransactionId: String = ""
    private var currentPayableAmount: Double = 0.0
    private var userMobile: String = ""

    companion object {
        private const val TAG = "TrainingKitActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training_registration_kit)

        val sp = getSharedPreferences("credentials", Context.MODE_PRIVATE)
        therapistId = intent.getStringExtra("EXTRA_EXPERT_ID")
            ?.takeIf { it.isNotBlank() }
            ?: sp.getString("Id", "")?.takeIf { it.isNotBlank() }
            ?: sp.getString("id", "")?.takeIf { it.isNotBlank() }
            ?: sp.getString("therapist_id", "")?.takeIf { it.isNotBlank() }
            ?: ""

        userMobile = sp.getString("mobile", "")?.takeIf { it.isNotBlank() }
            ?: sp.getString("Mobile", "")?.takeIf { it.isNotBlank() }
            ?: sp.getString("phone", "")?.takeIf { it.isNotBlank() }
            ?: ""

        // Initialize PhonePe SDK
        PhonePe.init(this, PhonePeEnvironment.RELEASE, MERCHANT_ID, null)

        setupStatusBar()
        initViews()
        setupMassageRecyclerView()
        setupListeners()
        updateTotalAmount()
    }

    private fun setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = Color.parseColor("#2563EB")
            // Blue background -> white status bar icons
            window.decorView.systemUiVisibility = 0
        }
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        rvMassageList = findViewById(R.id.rvMassageList)
        btnProcessFees = findViewById(R.id.btnProcessFees)
        tvTotalPayableAmount = findViewById(R.id.tvTotalPayableAmount)

        cardMassageBed = findViewById(R.id.cardMassageBed)
        cbMassageBed = findViewById(R.id.cbMassageBed)
        tvPriceBed = findViewById(R.id.tvPriceBed)
        tvPriceKitSummary = findViewById(R.id.tvPriceKitSummary)
        tvBedSummaryStatus = findViewById(R.id.tvBedSummaryStatus)
        tvPriceTraining = findViewById(R.id.tvPriceTraining)
        tvPriceUniform = findViewById(R.id.tvPriceUniform)
        tvPriceOtherKit = findViewById(R.id.tvPriceOtherKit)
    }

    private fun parsePrice(tv: TextView, default: Int): Int {
        val digits = tv.text.toString().replace("[^0-9]".toRegex(), "")
        return digits.toIntOrNull() ?: default
    }

    private fun getKitFee(): Int {
        val training = parsePrice(tvPriceTraining, defaultTrainingPrice)
        val uniform = parsePrice(tvPriceUniform, defaultUniformPrice)
        val otherKit = parsePrice(tvPriceOtherKit, defaultOtherKitPrice)
        return training + uniform + otherKit
    }

    private fun getBedFee(): Int {
        return parsePrice(tvPriceBed, defaultBedPrice)
    }

    private fun calculateTotal(): Int {
        val kitFee = getKitFee()
        val bedFee = getBedFee()
        return if (cbMassageBed.isChecked) {
            kitFee + bedFee
        } else {
            kitFee
        }
    }

    private fun updateTotalAmount() {
        val isChecked = cbMassageBed.isChecked
        val kitFee = getKitFee()
        val bedFee = getBedFee()
        val total = if (isChecked) kitFee + bedFee else kitFee

        tvPriceKitSummary.text = "₹" + String.format(Locale.US, "%,d", kitFee)
        tvTotalPayableAmount.text = "₹" + String.format(Locale.US, "%,d", total)

        if (isChecked) {
            cardMassageBed.setBackgroundResource(R.drawable.bg_card_massage_bed_selected)
            tvBedSummaryStatus.text = "+₹" + String.format(Locale.US, "%,d", bedFee)
            tvBedSummaryStatus.setTextColor(Color.parseColor("#4338CA"))
            tvBedSummaryStatus.setTypeface(null, android.graphics.Typeface.BOLD)
        } else {
            cardMassageBed.setBackgroundResource(R.drawable.bg_card_massage_bed)
            tvBedSummaryStatus.text = "Not Selected (₹0)"
            tvBedSummaryStatus.setTextColor(Color.parseColor("#64748B"))
            tvBedSummaryStatus.setTypeface(null, android.graphics.Typeface.NORMAL)
        }
    }

    private fun setupMassageRecyclerView() {
        rvMassageList.layoutManager = LinearLayoutManager(this)
        massageItems.clear()
        massageItems.add(
            MassageStepItem(
                name = "Loading massage services...",
                subtitle = "Fetching details from database...",
                isApproved = false,
                statusText = "Loading"
            )
        )
        massageAdapter = MassageStepAdapter(this, massageItems)
        rvMassageList.adapter = massageAdapter

        fetchTherapistRegistrationAndMassages()
    }

    /**
     * 1. Query track_registration_status.php by therapist ID (from SharedPreferences).
     * 2. Read selected categories (e.g. 1 for home_massage, 2 for Ayurved massage).
     * 3. Read selected_services column data.
     */
    private fun fetchTherapistRegistrationAndMassages() {
        if (therapistId.isBlank()) {
            Log.w(TAG, "No therapist ID found in SharedPreferences or Intent. Loading full massage catalog.")
            fetchAllMassagesAndMatch(emptyList(), isMassage = true, isAyurveda = true)
            return
        }

        val request = object : StringRequest(
            Request.Method.POST,
            Constant.TrackRegistrationStatusUrl,
            { response ->
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
                            val phone = json.optString("phone", "").trim()
                            if (phone.isNotBlank()) {
                                userMobile = phone
                            }

                            val categories = json.optString("categories", "").ifBlank {
                                json.optString("expert_category", "")
                            }
                            val selectedServicesRaw = json.optString("selected_services", "")
                            val selectedList = parseSelectedServices(selectedServicesRaw)

                            val isMassage = isCategoryMassage(categories)
                            val isAyurveda = isCategoryAyurveda(categories)

                            // Default to fetching both if category field is empty or unrecognized
                            val fetchMassage = isMassage || (!isMassage && !isAyurveda)
                            val fetchAyurveda = isAyurveda || (!isMassage && !isAyurveda)

                            fetchAllMassagesAndMatch(selectedList, fetchMassage, fetchAyurveda)
                        } else {
                            Log.w(TAG, "track_registration_status returned status false: " + json.optString("message"))
                            fetchAllMassagesAndMatch(emptyList(), isMassage = true, isAyurveda = true)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing tracking response: ${e.message}", e)
                        fetchAllMassagesAndMatch(emptyList(), isMassage = true, isAyurveda = true)
                    }
                }
            },
            { error ->
                Log.e(TAG, "Error fetching tracking status: ${error.message}")
                if (!isFinishing && !isDestroyed) {
                    fetchAllMassagesAndMatch(emptyList(), isMassage = true, isAyurveda = true)
                }
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id"] = therapistId
                return params
            }
        }

        request.retryPolicy = DefaultRetryPolicy(10000, 1, 1.0f)
        Volley.newRequestQueue(this).add(request)
    }

    private fun isCategoryMassage(categoriesRaw: String): Boolean {
        if (categoriesRaw.isBlank()) return false
        val lower = categoriesRaw.lowercase(Locale.ROOT)
        val tokens = categoriesRaw.split(",", "[", "]", "\"", "'", " ", ";")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (tokens.contains("1") || tokens.contains("9")) return true
        if (lower.contains("massage at home") || lower.contains("massage therapist")) return true
        return lower.contains("massage") && !lower.contains("ayurved")
    }

    private fun isCategoryAyurveda(categoriesRaw: String): Boolean {
        if (categoriesRaw.isBlank()) return false
        val lower = categoriesRaw.lowercase(Locale.ROOT)
        val tokens = categoriesRaw.split(",", "[", "]", "\"", "'", " ", ";")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (tokens.contains("2")) return true
        return lower.contains("ayurved")
    }

    private fun parseSelectedServices(raw: String): List<String> {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return emptyList()

        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            try {
                val arr = JSONArray(trimmed)
                val list = mutableListOf<String>()
                for (i in 0 until arr.length()) {
                    val s = arr.optString(i, "").trim()
                        .replace("✓", "").replace("✔", "").replace("√", "").trim()
                    if (s.isNotBlank() && !list.contains(s)) {
                        list.add(s)
                    }
                }
                return list
            } catch (_: Exception) {}
        }

        return trimmed.split(",")
            .map {
                it.trim()
                    .replace("✓", "")
                    .replace("✔", "")
                    .replace("√", "")
                    .trim()
            }
            .filter { it.isNotBlank() }
    }

    /**
     * Fetches all service names from home_massage and/or ayurved_massages tables in DB.
     */
    private fun fetchAllMassagesAndMatch(
        selectedList: List<String>,
        isMassage: Boolean,
        isAyurveda: Boolean
    ) {
        val collectedServices = mutableListOf<String>()
        var expectedFetches = 0
        if (isMassage) expectedFetches++
        if (isAyurveda) expectedFetches++
        if (expectedFetches == 0) {
            expectedFetches = 2
        }

        var completedFetches = 0

        val onFetchComplete: (List<String>) -> Unit = { servicesFromApi ->
            synchronized(collectedServices) {
                collectedServices.addAll(servicesFromApi)
                completedFetches++
                if (completedFetches >= expectedFetches) {
                    runOnUiThread {
                        displayMatchedMassages(collectedServices, selectedList)
                    }
                }
            }
        }

        val queue = Volley.newRequestQueue(this)

        if (isMassage || (!isMassage && !isAyurveda)) {
            val reqMassage = StringRequest(
                Request.Method.GET,
                Constant.getHomeMassage,
                { response ->
                    onFetchComplete(parseServicesList(response))
                },
                { error ->
                    Log.e(TAG, "Error fetching home_massage services: ${error.message}")
                    onFetchComplete(emptyList())
                }
            )
            reqMassage.retryPolicy = DefaultRetryPolicy(10000, 1, 1.0f)
            queue.add(reqMassage)
        }

        if (isAyurveda || (!isMassage && !isAyurveda)) {
            val reqAyurveda = StringRequest(
                Request.Method.GET,
                Constant.getAyurvedaServices,
                { response ->
                    onFetchComplete(parseServicesList(response))
                },
                { error ->
                    Log.e(TAG, "Error fetching ayurved_massages services: ${error.message}")
                    onFetchComplete(emptyList())
                }
            )
            reqAyurveda.retryPolicy = DefaultRetryPolicy(10000, 1, 1.0f)
            queue.add(reqAyurveda)
        }
    }

    private fun parseServicesList(response: String?): List<String> {
        val list = mutableListOf<String>()
        if (response.isNullOrBlank()) return list

        try {
            val trimmed = response.trim()
            val jsonStart = trimmed.indexOfAny(charArrayOf('{', '['))
            if (jsonStart < 0) return list
            val jsonEnd = if (trimmed[jsonStart] == '{') trimmed.lastIndexOf('}') else trimmed.lastIndexOf(']')
            if (jsonEnd <= jsonStart) return list
            val sanitized = trimmed.substring(jsonStart, jsonEnd + 1)

            val array: JSONArray? = if (sanitized.startsWith("[")) {
                JSONArray(sanitized)
            } else {
                val obj = JSONObject(sanitized)
                when {
                    obj.has("services") -> obj.optJSONArray("services")
                    obj.has("home_massage") -> obj.optJSONArray("home_massage")
                    obj.has("ayurved_massages") -> obj.optJSONArray("ayurved_massages")
                    obj.has("data") -> obj.optJSONArray("data")
                    obj.has("items") -> obj.optJSONArray("items")
                    else -> null
                }
            }

            if (array != null) {
                for (i in 0 until array.length()) {
                    val item = array.opt(i)
                    when (item) {
                        is String -> {
                            val s = item.trim()
                            if (s.isNotEmpty() && !list.contains(s)) list.add(s)
                        }
                        is JSONObject -> {
                            val name = item.optString("name", "").ifBlank {
                                item.optString("service_name", "").ifBlank {
                                    item.optString("title", "")
                                }
                            }.trim()
                            if (name.isNotEmpty() && !list.contains(name)) list.add(name)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing services list: ${e.message}", e)
        }
        return list
    }

    private fun normalizeService(name: String): String {
        return name
            .replace("✓", "")
            .replace("✔", "")
            .replace("√", "")
            .replace("–", " ")
            .replace("-", " ")
            .replace("&", " and ")
            .replace("[^a-zA-Z0-9 ]".toRegex(), " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
            .lowercase(Locale.ROOT)
    }

    private fun isServiceMatched(dbServiceName: String, selectedServices: List<String>): Boolean {
        val normDb = normalizeService(dbServiceName)
        if (normDb.isBlank()) return false

        for (sel in selectedServices) {
            val normSel = normalizeService(sel)
            if (normSel.isBlank()) continue

            // 1. Exact match
            if (normDb == normSel) {
                return true
            }

            // 2. Substring match for substantial names (>= 8 chars)
            if (normDb.length >= 8 && normSel.length >= 8) {
                if (normDb.contains(normSel) || normSel.contains(normDb)) {
                    return true
                }
            }

            // 3. Keyword / word intersection match
            val dbWords = normDb.split(" ").filter { it.length > 2 }.toSet()
            val selWords = normSel.split(" ").filter { it.length > 2 }.toSet()
            if (dbWords.isNotEmpty() && selWords.isNotEmpty()) {
                val intersection = dbWords.intersect(selWords)
                val minSize = minOf(dbWords.size, selWords.size)
                if (minSize >= 3 && intersection.size >= minSize) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Matches all catalog services against therapist's selected_services:
     * - Match -> "Approved" (isApproved = true, "(Live will check)")
     * - No match -> "Training Required" (isApproved = false, "(Company will provide training)")
     */
    private fun displayMatchedMassages(allServices: List<String>, selectedList: List<String>) {
        if (isFinishing || isDestroyed) return

        val deduplicated = mutableListOf<String>()
        val seen = mutableSetOf<String>()

        for (s in allServices) {
            val norm = normalizeService(s)
            if (norm.isNotBlank() && !seen.contains(norm)) {
                seen.add(norm)
                deduplicated.add(s)
            }
        }

        val items = deduplicated.map { serviceName ->
            val matched = isServiceMatched(serviceName, selectedList)
            MassageStepItem(
                name = serviceName,
                subtitle = if (matched) "(Live will check)" else "(Company will provide training)",
                isApproved = matched,
                statusText = if (matched) "Approved" else "Training Required"
            )
        }.sortedWith(
            compareByDescending<MassageStepItem> { it.isApproved }
                .thenBy { it.name }
        )

        massageItems.clear()
        if (items.isEmpty()) {
            massageItems.add(
                MassageStepItem(
                    name = "No massage services found",
                    subtitle = "(Company will provide training)",
                    isApproved = false,
                    statusText = "Training Required"
                )
            )
        } else {
            massageItems.addAll(items)
        }

        massageAdapter?.updateItems(massageItems)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        // Massage bed checkbox listener
        cbMassageBed.setOnCheckedChangeListener { _, _ ->
            updateTotalAmount()
        }

        // Allow tapping anywhere on the massage bed card to toggle checkbox
        cardMassageBed.setOnClickListener {
            cbMassageBed.isChecked = !cbMassageBed.isChecked
        }

        // Bottom Button: Process to Kit Training Fees -> Launch PhonePe Payment Gateway
        btnProcessFees.setOnClickListener {
            val parsedFromTv = tvTotalPayableAmount.text.toString().replace("[^0-9]".toRegex(), "").toDoubleOrNull()
            val totalAmount = parsedFromTv ?: calculateTotal().toDouble()

            if (totalAmount <= 0) {
                Toast.makeText(this, "Invalid payable amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            startPhonePePayment(totalAmount)
        }
    }

    /**
     * Launch PhonePe Payment Gateway with amount from tvTotalPayableAmount
     */
    private fun startPhonePePayment(amount: Double) {
        currentPayableAmount = amount
        currentTransactionId = "KIT_${therapistId.ifBlank { "EXP" }}_${System.currentTimeMillis()}"
        val amountInPaise = (amount * 100).toLong()

        val sp = getSharedPreferences("credentials", Context.MODE_PRIVATE)
        val candidateMobile = userMobile.ifBlank {
            sp.getString("mobile", "")?.takeIf { it.isNotBlank() }
                ?: sp.getString("Mobile", "")?.takeIf { it.isNotBlank() }
                ?: sp.getString("phone", "")?.takeIf { it.isNotBlank() }
                ?: ""
        }

        val cleanMobile = candidateMobile.replace("[^0-9]".toRegex(), "").let {
            if (it.length > 10) it.takeLast(10) else it
        }.ifBlank { "9999999999" }

        val payload = JSONObject().apply {
            put("merchantId", MERCHANT_ID)
            put("merchantTransactionId", currentTransactionId)
            put("merchantUserId", "UID_${therapistId.ifBlank { "USER" }}")
            put("amount", amountInPaise)
            put("callbackUrl", Constant.KitPaymentCallbackUrl)
            put("redirectUrl", Constant.KitPaymentRedirectUrl)
            put("redirectMode", "REDIRECT")
            put("mobileNumber", cleanMobile)
            put("paymentInstrument", JSONObject().apply {
                put("type", "PAY_PAGE")
            })
        }

        val base64Payload = Base64.encodeToString(
            payload.toString().toByteArray(Charset.defaultCharset()),
            Base64.NO_WRAP
        )

        val checksum = sha256(base64Payload + B2B_PG_URL + SALT_KEY) + "###" + SALT_INDEX

        val b2BPGRequest = B2BPGRequestBuilder()
            .setData(base64Payload)
            .setChecksum(checksum)
            .setUrl(B2B_PG_URL)
            .build()

        try {
            val intent = PhonePe.getImplicitIntent(this, b2BPGRequest, "")
            if (intent != null) {
                startActivityForResult(intent, PHONEPE_REQUEST_CODE)
            } else {
                Toast.makeText(this, "Opening PhonePe payment gateway...", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "PhonePe launch error: ${e.message}", e)
            Toast.makeText(this, "Payment error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PHONEPE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Payment successful! Updating registration status...", Toast.LENGTH_SHORT).show()
                // Clear user credentials from SharedPreferences after successful payment
                val sp = getSharedPreferences("credentials", Context.MODE_PRIVATE)
                sp.edit().clear().apply()

                updateKitPaymentStatusOnServer(currentTransactionId, currentPayableAmount)
            } else {
                Toast.makeText(this, "Payment Cancelled or Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Update therapist record in DB when payment is success:
     * kit_payment = 'completed', payment_amount = $amount, transaction_id = $txnId, payment_date = NOW()
     */
    private fun updateKitPaymentStatusOnServer(txnId: String, amount: Double) {
        val request = object : StringRequest(
            Request.Method.POST,
            Constant.UpdateKitPaymentUrl,
            { response ->
                Log.d(TAG, "Update kit payment response: $response")
                navigateToSuccessScreen(txnId)
            },
            { error ->
                Log.e(TAG, "Failed to update kit payment on server: ${error.message}")
                // Still navigate to success since PhonePe completed successfully
                navigateToSuccessScreen(txnId)
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id"] = therapistId
                params["payment_amount"] = String.format(Locale.US, "%.2f", amount)
                params["transaction_id"] = txnId
                return params
            }
        }

        request.retryPolicy = DefaultRetryPolicy(12000, 1, 1.0f)
        Volley.newRequestQueue(this).add(request)
    }

    private fun navigateToSuccessScreen(txnId: String) {
        if (isFinishing || isDestroyed) return

        val sp = getSharedPreferences("credentials", Context.MODE_PRIVATE)
        sp.edit().clear().apply()

        val intent = Intent(this, TrainingPaymentSuccessActivity::class.java).apply {
            putExtra(TrainingPaymentSuccessActivity.EXTRA_TRANSACTION_ID, txnId)
            putExtra(TrainingPaymentSuccessActivity.EXTRA_REGISTRATION_STATUS, "Confirmed")
            putExtra("EXTRA_EXPERT_ID", therapistId)
        }
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun sha256(input: String): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    // ============================================================
    // DATA MODEL & RECYCLERVIEW ADAPTER FOR MASSAGE LIST
    // ============================================================
    data class MassageStepItem(
        val name: String,
        val subtitle: String,
        val isApproved: Boolean,
        val statusText: String
    )

    class MassageStepAdapter(
        private val context: Context,
        private var items: List<MassageStepItem>
    ) : RecyclerView.Adapter<MassageStepAdapter.MassageViewHolder>() {

        fun updateItems(newItems: List<MassageStepItem>) {
            this.items = newItems
            notifyDataSetChanged()
        }

        inner class MassageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvMassageName: TextView = itemView.findViewById(R.id.tvMassageName)
            val tvMassageSubtitle: TextView = itemView.findViewById(R.id.tvMassageSubtitle)
            val layoutStatusBadge: LinearLayout = itemView.findViewById(R.id.layoutStatusBadge)
            val ivStatusIcon: ImageView = itemView.findViewById(R.id.ivStatusIcon)
            val tvStatusText: TextView = itemView.findViewById(R.id.tvStatusText)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MassageViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_massage_step, parent, false)
            return MassageViewHolder(view)
        }

        override fun onBindViewHolder(holder: MassageViewHolder, position: Int) {
            val item = items[position]
            holder.tvMassageName.text = item.name
            holder.tvMassageSubtitle.text = item.subtitle
            holder.tvStatusText.text = item.statusText

            if (item.isApproved) {
                holder.layoutStatusBadge.setBackgroundResource(R.drawable.bg_pill_approved_green)
                holder.tvStatusText.setTextColor(Color.parseColor("#059669"))
                holder.ivStatusIcon.setImageResource(R.drawable.baseline_check_circle_outline_24)
                holder.ivStatusIcon.setColorFilter(Color.parseColor("#059669"))
            } else if (item.statusText.equals("Loading", ignoreCase = true)) {
                holder.layoutStatusBadge.setBackgroundResource(R.drawable.bg_pill_pending_amber)
                holder.tvStatusText.setTextColor(Color.parseColor("#64748B"))
                holder.ivStatusIcon.setImageResource(R.drawable.ic_clock_outline)
                holder.ivStatusIcon.setColorFilter(Color.parseColor("#64748B"))
            } else {
                holder.layoutStatusBadge.setBackgroundResource(R.drawable.bg_pill_pending_amber)
                holder.tvStatusText.setTextColor(Color.parseColor("#D97706"))
                holder.ivStatusIcon.setImageResource(R.drawable.ic_clock_outline)
                holder.ivStatusIcon.setColorFilter(Color.parseColor("#D97706"))
            }
        }

        override fun getItemCount(): Int = items.size
    }
}
