package com.company.jifiit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.company.jifiit.ViewMoreMenu.CompletePaymenteActivity
import com.phonepe.intent.sdk.api.B2BPGRequestBuilder
import com.phonepe.intent.sdk.api.PhonePe
import com.phonepe.intent.sdk.api.models.PhonePeEnvironment
import org.json.JSONObject
import java.nio.charset.Charset
import java.security.MessageDigest

class PaymentDialogActivity : AppCompatActivity() {
    private lateinit var bookingIdText: TextView
    private lateinit var bookingIdTextCustomer: TextView

    private lateinit var serviceAmountText: TextView
    private lateinit var serviceAmountTextCustomer: TextView

    private lateinit var customerName: TextView

    private lateinit var gstAmountText: TextView
    private lateinit var gstAmountTextCustomer: TextView

    private lateinit var totalAmountText: TextView
    private lateinit var totalAmountTextCustomer: TextView

    private lateinit var payBtn: Button
    private lateinit var paymentBtn: Button

    private lateinit var userMobile: String
    private var customerNameValue: String = ""

    private lateinit var company_due_payment: LinearLayout
    private lateinit var previous_due_payment: LinearLayout

    // Constants - Double check these from your PhonePe Dashboard
    private val MERCHANT_ID = "JIIFITONLINE"
    private val SALT_KEY = "518d0d50-2f8e-455f-8cc1-b2bc0e981bb2"
    private val SALT_INDEX = "1"
    private val B2B_PG_URL = "/pg/v1/pay"

    companion object {
        @JvmStatic
        fun start(
            context: Context,
            orderId: String,
            serviceCharge: Double,
            gst: Double,
            payableAmount: Double,
            mobileNumber: String,
            commission: Int,
            paymentStatus: String,
            customer_name: String
        ) {
            val intent = Intent(context, PaymentDialogActivity::class.java)
            intent.putExtra("orderId", orderId)
            intent.putExtra("serviceCharge", serviceCharge)
            intent.putExtra("gst", gst)
            intent.putExtra("payableAmount", payableAmount)
            intent.putExtra("MobileNumber", mobileNumber)
            intent.putExtra("Commission", commission)
            intent.putExtra("PaymentStatus", paymentStatus)
            intent.putExtra("customerName", customer_name)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_payment_dialog)

        // Make activity window transparent and dialog-like
        window.setBackgroundDrawableResource(android.R.color.transparent)
        window.setDimAmount(0.5f) // Dim background
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        setFinishOnTouchOutside(false)

        // Disable back button during payment process
        onBackPressedDispatcher.addCallback(this) {
            // Do nothing
        }

        setupUI()

        // Initialize PhonePe SDK
        PhonePe.init(this, PhonePeEnvironment.RELEASE, MERCHANT_ID, null)
    }

    private fun setupUI() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val orderId = intent.getStringExtra("orderId") ?: ""
        val serviceCharge = intent.getDoubleExtra("serviceCharge", 0.0)
        val gst = intent.getDoubleExtra("gst", 0.0)
        val payableAmount = intent.getDoubleExtra("payableAmount", 0.0)
        userMobile = intent.getStringExtra("MobileNumber") ?: ""
        val commission = intent.getIntExtra("Commission", 0)
        val paymentStatus = intent.getStringExtra("PaymentStatus") ?: ""
        customerNameValue = intent.getStringExtra("customerName") ?: ""

        // Initialize views
        customerName = findViewById(R.id.customer_name_text)
        company_due_payment = findViewById(R.id.dailog_box)
        previous_due_payment = findViewById(R.id.dailog_box_for_customerPayment)

        bookingIdText = findViewById(R.id.booking_id_text)
        bookingIdTextCustomer = findViewById(R.id.booking_id_text_for_customer)

        serviceAmountText = findViewById(R.id.service_amount_text)
        serviceAmountTextCustomer = findViewById(R.id.amount_received_text)

        gstAmountText = findViewById(R.id.gst_amount_text)
        gstAmountTextCustomer = findViewById(R.id.gst_amount_text_customer)

        totalAmountText = findViewById(R.id.total_amount_text)
        totalAmountTextCustomer = findViewById(R.id.total_amount_text_customer)

        payBtn = findViewById(R.id.pay_button)
        paymentBtn = findViewById(R.id.payment_button)

        // Handle different scenarios
        when {
            commission == 0 && paymentStatus.equals("Completed", ignoreCase = true) -> {
                // Company due payment - show payment dialog
                company_due_payment.visibility = View.VISIBLE
                previous_due_payment.visibility = View.GONE

                bookingIdText.text = "#$orderId"
                serviceAmountText.text = "₹ %.2f".format(serviceCharge)
                gstAmountText.text = "₹ %.2f".format(gst)
                totalAmountText.text = "₹ %.2f".format(payableAmount)

                payBtn.setOnClickListener {
                    startPayment(payableAmount, orderId, userMobile)
                }
            }
            commission == 0 && paymentStatus.equals("Pending", ignoreCase = true) -> {
                // Previous due payment - show customer payment dialog
                company_due_payment.visibility = View.GONE
                previous_due_payment.visibility = View.VISIBLE

                bookingIdTextCustomer.text = "#$orderId"
                serviceAmountTextCustomer.text = "₹ %.2f".format(serviceCharge)
                gstAmountTextCustomer.text = "₹ %.2f".format(gst)
                totalAmountTextCustomer.text = "₹ %.2f".format(payableAmount)
                customerName.text = customerNameValue

                paymentBtn.setOnClickListener {
                    val intent = Intent(this@PaymentDialogActivity, CompletePaymenteActivity::class.java)
                    intent.putExtra("id", orderId)
                    intent.putExtra("customer_name", customerNameValue)
                    startActivity(intent)
                }
            }
            else -> {
                // Default case - show company due payment
                company_due_payment.visibility = View.VISIBLE
                previous_due_payment.visibility = View.GONE

                bookingIdText.text = "#$orderId"
                serviceAmountText.text = "₹ %.2f".format(serviceCharge)
                gstAmountText.text = "₹ %.2f".format(gst)
                totalAmountText.text = "₹ %.2f".format(payableAmount)

                payBtn.setOnClickListener {
                    startPayment(payableAmount, orderId, userMobile)
                }
            }
        }
    }

    private fun startPayment(amount: Double, orderId: String, mobile: String) {
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
            put("callbackUrl", Constant.CallBackUrl)
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
                finish()
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
}