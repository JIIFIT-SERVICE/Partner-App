package com.company.jifiit.ViewMoreMenu

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TableLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.company.jifiit.Constant
import com.company.jifiit.LocalStorage.DatabaseHelper
import com.company.jifiit.MainActivity
import com.company.jifiit.MyQr.My_Qr
import com.company.jifiit.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.phonepe.intent.sdk.api.B2BPGRequestBuilder
import com.phonepe.intent.sdk.api.PhonePe
import com.phonepe.intent.sdk.api.models.PhonePeEnvironment
import org.json.JSONException
import org.json.JSONObject
import java.math.BigInteger
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.HashMap

class CompletePaymenteActivity: AppCompatActivity() {

    private var isQrCodeSelected = false
    private lateinit var link: TextView
    private lateinit var text: TextView
    private lateinit var amountTxt: TextView
    private lateinit var senderName: TextView
    private lateinit var feedBackLink: Button
    private lateinit var invoiceLink: Button
    private lateinit var cash: CardView
    private lateinit var Qr_code: CardView
    private lateinit var qrCodeImageView: ImageView
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var paymentLayout: LinearLayout
    private lateinit var completedLayout: LinearLayout
    private lateinit var qrCardContainer: CardView
    private lateinit var completeBtn: Button
    private lateinit var RefreshBtn: Button
    private lateinit var qrLayout: LinearLayout
    private lateinit var footer: LinearLayout
    private lateinit var cashLayout: LinearLayout
    private lateinit var tableLayout: TableLayout

    private var Orderid: String? = null
    private var names: String? = null
    private var totalPrice = 0.0
    private var isPaymentCompleted = false
    private var has_upi = false
    private var Upi_Id: String? = null
    private var Upi_name: String? = null
    private var EmployeeId: BigInteger? = null
    private var customerName: String? = null
    private var userStatus: String? = null
    private var mobile: String? = null
    private var T_mobile: String? = null

    private var qrCountDownTimer: CountDownTimer? = null

    private val MERCHANT_ID = "JIIFITONLINE"
    private val SALT_KEY = "518d0d50-2f8e-455f-8cc1-b2bc0e981bb2"
    private val SALT_INDEX = "1"
    private val B2B_PG_URL = "/pg/v1/pay"


    @SuppressLint("MissingInflatedId", "LocalSuppress")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complete_paymente)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Payment"

        val sp = getSharedPreferences("credentials", MODE_PRIVATE)
        val name = sp.getString("Name", "Default Name")
        T_mobile = sp.getString("mobile", "Default Name")

        val idString = sp.getString("Id", "")
        EmployeeId = BigInteger(idString)

        PhonePe.init(this, PhonePeEnvironment.RELEASE, MERCHANT_ID, null)

        cash = findViewById(R.id.cashCard)
        Qr_code = findViewById(R.id.qrCard)
        text = findViewById(R.id.paymentInProgressText)
        amountTxt = findViewById(R.id.AmountTxt)
        senderName = findViewById(R.id.senderName)
        completeBtn = findViewById(R.id.backbtn)
        RefreshBtn = findViewById(R.id.refreshBtn)
        feedBackLink = findViewById(R.id.feedBackLink)
        invoiceLink = findViewById(R.id.invoiceLink)

        qrCodeImageView = findViewById(R.id.qrCodeImageView)
        qrCardContainer = findViewById(R.id.qrCardContainer)

        completedLayout = findViewById(R.id.completedLayout)
        paymentLayout = findViewById(R.id.paymentLayout)
        footer = findViewById(R.id.footer)
        Orderid = intent.getStringExtra("id")
        names = intent.getStringExtra("customer_name")

        text.text = "Order Id: $Orderid"
        senderName.text = "Scan to pay,\n$names"
        senderName.visibility = View.GONE
        text.visibility = View.GONE

        qrLayout = findViewById(R.id.qr_layout)
        cashLayout = findViewById(R.id.cash_layout)

        completeBtn.setOnClickListener {
            try {
                if (::dbHelper.isInitialized) {
                    dbHelper.deleteProducts(Orderid)
                } else {
                    Log.e("Database", "DatabaseHelper not initialized")
                }
                val intent = Intent(this@CompletePaymenteActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Log.e("Database", "Error deleting products: ${e.message}")
                val intent = Intent(this@CompletePaymenteActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }

        RefreshBtn.setOnClickListener {
            AlertDialog.Builder(this@CompletePaymenteActivity)
                .setTitle("$names\nTotal Cash ₹$totalPrice")
                .setMessage("Have You Collect The Booking Amount?")
                .setPositiveButton("Yes") { _, _ ->
                    submitCash(Orderid, "online")
                }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        feedBackLink.setOnClickListener {
            val url = Constant.feedbackLink + Orderid
            showQRCodeDialog(url)
        }

        invoiceLink.setOnClickListener {
            shareInvoice()
        }
    }

    private fun shareInvoice() {
        try {
            val secureHash = generateSecureHash(Orderid.toString())
            val secureUrl = Constant.shareInvoice + "?hash=" + secureHash

            val message = "Hello Dear *${names?.trim()}*,\n\n" +
                    "Thank you for choosing Jiifit LLP — JIIFIT India's 1st specialised massage company for clients place.\n\n" +
                    "Your massage session has been successfully completed.\n" +
                    "Please find your invoice below:\n\n" +
                    "Click here to download your invoice:\n\n" +
                    secureUrl + "\n\n" +
                    "We look forward to serving you again soon!\n\n" +
                    "Warm regards,\n" +
                    "Team Jiifit\n" +
                    "www.jiifit.com"

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Invoice #$Orderid")
                putExtra(Intent.EXTRA_TEXT, message)
            }

            startActivity(Intent.createChooser(shareIntent, "Share Invoice"))
        } catch (e: Exception) {
            Toast.makeText(this, "Error sharing invoice", Toast.LENGTH_SHORT).show()
            Log.e("SharePDF", "Sharing failed", e)
        }
    }

    private fun generateSecureHash(orderId: String): String {
        return try {
            val salt = Constant.HASH_SALT
            val input = orderId + salt
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(input.toByteArray(StandardCharsets.UTF_8))
            val hexString = StringBuilder()
            for (b in hashBytes) {
                val hex = String.format("%02x", b)
                hexString.append(hex)
            }
            hexString.toString()
        } catch (e: Exception) {
            Log.e("HashError", "Failed to generate hash", e)
            ""
        }
    }

    private fun showQRCodeDialog(url: String) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(url, BarcodeFormat.QR_CODE, 400, 400)

            val dialog = Dialog(this)
            dialog.setContentView(R.layout.dialog_qr_code)

            val qrCodeImageView = dialog.findViewById<ImageView>(R.id.qrCodeImageView)
            qrCodeImageView.setImageBitmap(bitmap)

            val closeIcon = dialog.findViewById<ImageView>(R.id.closeIcon)
            closeIcon.setOnClickListener { dialog.dismiss() }

            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error generating QR code", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun checkPaymentStatus(orderId: String?, therapistId: BigInteger?) {
        // Show progress bar
        showProgressBar(true)
        paymentLayout.visibility = View.GONE

        val url = Constant.checkPayment + orderId + "&id=" + therapistId

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                // Hide progress bar on success
                showProgressBar(false)

                val success = response.optBoolean("success", true)

                if (success) {
                    val paymentStatus = response.optString("payment_status", "unknown")
                    totalPrice = response.optDouble("total_price", 0.0)
                    val hasUpi = response.optBoolean("has_upi", true)
                    Upi_Id = response.optString("upi_id", "unknown")
                    Upi_name = response.optString("upi_name", "unknown")
                    T_mobile = response.optString("mobile", T_mobile)

                    amountTxt.text = "Amount To be paid: ₹$totalPrice"
                    amountTxt.visibility = View.VISIBLE

                    when (paymentStatus.lowercase()) {
                        "completed" -> {
                            isPaymentCompleted = true
                            paymentLayout.visibility = View.GONE
                            completedLayout.visibility = View.VISIBLE
                            qrCodeImageView.visibility = View.GONE
                            qrCardContainer.visibility = View.GONE
                            text.visibility = View.GONE
                            senderName.visibility = View.GONE
                            completeBtn.visibility = View.VISIBLE
                            RefreshBtn.visibility = View.GONE
                            Toast.makeText(this, "Payment Completed", Toast.LENGTH_SHORT).show()
                        }
                        "failed" -> {
                            Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show()
                        }
                        "pending" -> {
                            // Remove previous listeners to avoid duplicates
                            Qr_code.setOnClickListener(null)
                            cash.setOnClickListener(null)
                            paymentLayout.visibility = View.VISIBLE
                            completedLayout.visibility = View.GONE


                            Qr_code.setOnClickListener {
                                startPayment(totalPrice, orderId, T_mobile)
                            }

                            cash.setOnClickListener {
                                try {
                                    qrCountDownTimer?.cancel()
                                    qrCountDownTimer = null

                                    resetQRPaymentState()

                                    qrCodeImageView.visibility = View.GONE
                                    qrCardContainer.visibility = View.GONE
                                    completeBtn.visibility = View.GONE
                                    RefreshBtn.visibility = View.GONE
                                    text.visibility = View.GONE
                                    senderName.visibility = View.GONE
                                    cashLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.deep_creamy_white))
                                    qrLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.creamy_white))

                                    AlertDialog.Builder(this@CompletePaymenteActivity)
                                        .setTitle("$names\nTotal Cash ₹$totalPrice")
                                        .setMessage("Do you have cash in hand?")
                                        .setPositiveButton("OK") { _, _ ->
                                            submitCash(orderId, "cash")
                                            cash.setBackgroundColor(Color.TRANSPARENT)
                                        }
                                        .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                                        .show()
                                } catch (e: Exception) {
                                    Toast.makeText(this@CompletePaymenteActivity, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        else -> {
                            Toast.makeText(this, "Invalid response from server", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    val errorMessage = response.optString("message", "Unknown error occurred")
                    Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                // Hide progress bar on error
                showProgressBar(false)

                Log.e("PaymentStatus", "Error: ${error.toString()}")
                Toast.makeText(this, "Error fetching payment status", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(jsonObjectRequest)
    }


    private fun showProgressBar(show: Boolean) {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        if (show) {
            progressBar.visibility = View.VISIBLE
            // Optional: Disable click events on views
            Qr_code.isEnabled = false
            cash.isEnabled = false
        } else {
            progressBar.visibility = View.GONE
            Qr_code.isEnabled = true
            cash.isEnabled = true
        }
    }

    private fun startPayment(amount: Double, orderId: String?, mobile: String?) {
        val sp = getSharedPreferences("credentials", MODE_PRIVATE)
        val userId = sp.getString("Id", "USER_UNKNOWN")

        // Unique transaction ID
        val finalTxnId = "${orderId}_${System.currentTimeMillis()}"
        val amountInPaise = (amount * 100).toLong()

        val payload = JSONObject().apply {
            put("merchantId", MERCHANT_ID)
            put("merchantTransactionId", finalTxnId)
            put("merchantUserId", "UID_$userId")
            put("amount", amountInPaise)
            put("callbackUrl", Constant.CallBackUrl2)
            put("mobileNumber", mobile)
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
                startActivityForResult(intent, 100)
            } else {
                Toast.makeText(this, "Opening Payment Gateway...", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("PhonePeError", "Exception: ${e.message}")
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Checking payment status...", Toast.LENGTH_SHORT).show()
                checkPaymentStatus(Orderid, EmployeeId)
            } else {
                Toast.makeText(this, "Payment Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sha256(input: String): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }


    private fun resetQRPaymentState() {
        qrCodeImageView.visibility = View.GONE
        qrCardContainer.visibility = View.GONE
        completeBtn.visibility = View.GONE
        RefreshBtn.visibility = View.GONE
        text.visibility = View.GONE
        senderName.visibility = View.GONE

        val timerText = findViewById<TextView>(R.id.timerText)
        timerText?.visibility = View.GONE

        qrLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.creamy_white))
        isQrCodeSelected = false
    }

    private fun submitCash(orderId: String?, type: String) {
        val url = Constant.paymentUpdate

        val progressDialog = ProgressDialog(this).apply {
            setMessage("Uploading, please wait...")
            setCancelable(false)
            show()
        }

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                progressDialog.dismiss()
                try {
                    if (response.trim().startsWith("{") || response.trim().startsWith("[")) {
                        val jsonResponse = JSONObject(response)
                        val status = jsonResponse.optString("status")
                        val message = jsonResponse.optString("message")
                        if ("success".equals(status, ignoreCase = true)) {
                            Toast.makeText(this@CompletePaymenteActivity, message, Toast.LENGTH_SHORT).show()
                            checkPaymentStatus(orderId, EmployeeId)
                        } else {
                            Toast.makeText(this@CompletePaymenteActivity, "Error: $message", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("InvalidResponse", "Response is not JSON: $response")
                        Toast.makeText(this, "Unexpected server response", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this@CompletePaymenteActivity, "Error parsing response", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                progressDialog.dismiss()
                val errorMessage = error.message ?: "An unexpected error occurred"
                Toast.makeText(this@CompletePaymenteActivity, "Error: $errorMessage", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["order_id"] = orderId ?: ""
                params["type"] = type
                return params
            }
        }

        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun generateQRCode(phonePePaymentUrl: String) {
        val qrCodeImageView = findViewById<ImageView>(R.id.qrCodeImageView)

        try {
            val writer = MultiFormatWriter()
            val bitMatrix = writer.encode(phonePePaymentUrl, BarcodeFormat.QR_CODE, 300, 300)
            val encoder = BarcodeEncoder()
            val bitmap = encoder.createBitmap(bitMatrix)

            qrCodeImageView.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        if (!isPaymentCompleted) {
            super.onBackPressed()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return if (!isPaymentCompleted) {
            onBackPressed()
            true
        } else {
            false
        }
    }

    override fun onResume() {
        super.onResume()
        checkPaymentStatus(Orderid, EmployeeId)
//        Toast.makeText(this@CompletePaymenteActivity,   "This is Kotlin file: $T_mobile", Toast.LENGTH_LONG).show()
    }
}