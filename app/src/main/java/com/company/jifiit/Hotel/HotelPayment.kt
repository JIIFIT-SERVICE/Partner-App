package com.company.jifiit.Hotel

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.company.jifiit.Constant
import com.company.jifiit.Hotel.Adapter.CartItemAdapter
import com.company.jifiit.Hotel.Expert_activity.Companion.EXTRA_BOOKING_DATE
import com.company.jifiit.Hotel.Expert_activity.Companion.EXTRA_BOOKING_TIME
import com.company.jifiit.Hotel.Expert_activity.Companion.EXTRA_CLIENT_GENDER
import com.company.jifiit.Hotel.Expert_activity.Companion.EXTRA_EMPLOYEE_ID
import com.company.jifiit.Hotel.Expert_activity.Companion.EXTRA_EXPERT_ID
import com.company.jifiit.Hotel.Expert_activity.Companion.EXTRA_EXPERT_NAME
import com.company.jifiit.Hotel.Expert_activity.Companion.EXTRA_HOTEL_ID
import com.company.jifiit.Hotel.Expert_activity.Companion.EXTRA_NEED_BED
import com.company.jifiit.Hotel.Expert_activity.Companion.EXTRA_SELECTED_SERVICE
import com.company.jifiit.Hotel.Expert_activity.Companion.EXTRA_TOTAL_MASSAGE_DURATION
import com.company.jifiit.Hotel.HelperFunctions.HotelCommissionHelper
import com.company.jifiit.LocalStorage.HotelDatabaseHelper
import com.company.jifiit.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.math.BigInteger
import java.util.Locale
import java.util.concurrent.TimeUnit

class HotelPayment : AppCompatActivity() {

    // ----- Expert info -----
    private lateinit var expertId: String
    private lateinit var expertName: String
    private lateinit var employeeId: String

    // ----- Booking info -----
    private lateinit var hotelId: String
    private var totalMassageDuration: Int = 0
    private var needBed: Int = 0
    private lateinit var bookingDate: String
    private lateinit var bookingTime: String
    private lateinit var clientGender: String
    private lateinit var selectedService: String

    // ----- Guest / Room / Flags -----
    private var guestName: String = ""
    private var guestMobile: String = ""
    private var country_Code: String = ""
    private var Mobile: String = ""


    private var roomId: String = ""
    private var roomNumber: String = ""
    private var floorWing: String = ""
    private var ringDoorbell: Boolean = false
    private var doNotCall: Boolean = false
    private var callConcierge: Boolean = false
    private var specialNote: String = ""
    private var pendingNavigation = false

    // ----- Voice note -----
    private var voiceNotePath: String = ""

    // ----- DB -----
    private lateinit var dbHelper: HotelDatabaseHelper

    // ----- Views -----
    private lateinit var btnBack: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvServiceOriginal: TextView
    private lateinit var tvServiceDiscounted: TextView
    private lateinit var tvGst: TextView
    private lateinit var tvHotelCommission: TextView
    private lateinit var tvTotalPayable: TextView

    private lateinit var paymentCash: LinearLayout
    private lateinit var paymentOnline: LinearLayout
    private lateinit var paymentOnlineAfter: LinearLayout

    private lateinit var btnConfirmDispatch: TextView

    // ----- Adapter -----
    private lateinit var cartAdapter: CartItemAdapter
    private val cartItems = mutableListOf<HotelDatabaseHelper.CartItem>()

    // ----- Constants -----
    private val GST_PERCENT = 5.0
    private var HOTEL_CHARGE = 0.0
    private lateinit var Id: BigInteger

    // ----- Payment selection -----
    private var selectedPaymentMethod: String? = null

    private lateinit var ivCashIcon: ImageView
    private lateinit var tvCashTitle: TextView
    private lateinit var tvCashDesc: TextView

    private lateinit var ivOnlineIcon: ImageView
    private lateinit var tvOnlineTitle: TextView
    private lateinit var tvOnlineDesc: TextView

    private lateinit var ivOnlineAfterIcon: ImageView
    private lateinit var tvOnlineAfterTitle: TextView

    // ----- OkHttp client -----
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .build()

    // =========================================================
    // PRICE BREAKDOWN
    // =========================================================
    private data class PriceBreakdown(
        val originalTotal: Double,
        val discountedTotal: Double,
        val gst: Double,
        val hotelCommission: Double,
        val totalPayable: Double
    )

    private fun computePriceBreakdown(): PriceBreakdown {
        val originalTotal   = cartItems.sumOf { it.originalPrice * it.quantity }
        val discountedTotal = cartItems.sumOf { it.discountPrice * it.quantity }
        val gst             = discountedTotal * GST_PERCENT / 100.0
        val hotelCommission = if (cartItems.isEmpty()) 0.0 else HOTEL_CHARGE
        val totalPayable    = discountedTotal + gst + hotelCommission

        return PriceBreakdown(
            originalTotal   = originalTotal,
            discountedTotal = discountedTotal,
            gst             = gst,
            hotelCommission = hotelCommission,
            totalPayable    = totalPayable
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        setContentView(R.layout.activity_hotel_payment)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        // =========================================================
        // GET EXPERT INFO
        // =========================================================
        expertId   = intent.getStringExtra(EXTRA_EXPERT_ID)   ?: ""
        expertName = intent.getStringExtra(EXTRA_EXPERT_NAME) ?: ""
        employeeId = intent.getStringExtra(EXTRA_EMPLOYEE_ID) ?: ""

        // =========================================================
        // GET BOOKING INFO
        // =========================================================
        hotelId              = intent.getStringExtra(EXTRA_HOTEL_ID) ?: ""
        totalMassageDuration = intent.getIntExtra(EXTRA_TOTAL_MASSAGE_DURATION, 0)
        needBed              = intent.getIntExtra(EXTRA_NEED_BED, 0)
        bookingDate          = intent.getStringExtra(EXTRA_BOOKING_DATE) ?: ""
        bookingTime          = intent.getStringExtra(EXTRA_BOOKING_TIME) ?: ""
        clientGender         = intent.getStringExtra(EXTRA_CLIENT_GENDER) ?: ""
        selectedService      = intent.getStringExtra(EXTRA_SELECTED_SERVICE) ?: ""

        // =========================================================
        // GET GUEST / ROOM / FLAGS
        // =========================================================
        guestName     = intent.getStringExtra("guest_name")   ?: ""
        guestMobile   = intent.getStringExtra("guest_mobile_full")    ?: ""
        country_Code  = intent.getStringExtra("country_code").orEmpty()
        Mobile = intent.getStringExtra("guest_mobile").orEmpty()


        roomId        = intent.getStringExtra("room_id")      ?: ""
        roomNumber    = intent.getStringExtra("room_number")  ?: ""
        floorWing     = intent.getStringExtra("floor_wing")   ?: ""

        ringDoorbell  = intent.getBooleanExtra("ring_doorbell",  false)
        doNotCall     = intent.getBooleanExtra("do_not_call",    false)
        callConcierge = intent.getBooleanExtra("call_concierge", false)

        specialNote   = intent.getStringExtra("special_note") ?: ""

        // =========================================================
        // GET VOICE NOTE PATH
        // =========================================================
        voiceNotePath = intent.getStringExtra("voice_note_path").orEmpty()

        Log.d("HotelPayment", "===== RECEIVED EXTRAS =====")
        Log.d("HotelPayment", "expertId       = $expertId")
        Log.d("HotelPayment", "expertName     = $expertName")
        Log.d("HotelPayment", "employeeId     = $employeeId")
        Log.d("HotelPayment", "hotelId        = $hotelId")
        Log.d("HotelPayment", "totalDuration  = $totalMassageDuration")
        Log.d("HotelPayment", "needBed        = $needBed")
        Log.d("HotelPayment", "bookingDate    = $bookingDate")
        Log.d("HotelPayment", "bookingTime    = $bookingTime")
        Log.d("HotelPayment", "clientGender   = $clientGender")
        Log.d("HotelPayment", "selectedService= $selectedService")
        Log.d("HotelPayment", "guestName      = $guestName")
        Log.d("HotelPayment", "guestMobileFull= $guestMobile")
        Log.d("HotelPayment", "country_Code   = $country_Code")
        Log.d("HotelPayment", "Mobile         = $Mobile")
        Log.d("HotelPayment", "roomId         = $roomId")
        Log.d("HotelPayment", "roomNumber     = $roomNumber")
        Log.d("HotelPayment", "floorWing      = $floorWing")
        Log.d("HotelPayment", "ringDoorbell   = $ringDoorbell")
        Log.d("HotelPayment", "doNotCall      = $doNotCall")
        Log.d("HotelPayment", "callConcierge  = $callConcierge")
        Log.d("HotelPayment", "specialNote    = $specialNote")
        Log.d("HotelPayment", "voiceNotePath  = $voiceNotePath")
        Log.d("HotelPayment", "============================")

        // =========================================================
        // INIT VIEWS
        // =========================================================
        btnBack = findViewById(R.id.btnBack)

        recyclerView        = findViewById(R.id.rvPaymentServices)
        tvServiceOriginal   = findViewById(R.id.tvServiceOriginal)
        tvServiceDiscounted = findViewById(R.id.tvServiceDiscounted)
        tvGst               = findViewById(R.id.tvGst)
        tvHotelCommission   = findViewById(R.id.tvHotelCommission)
        tvTotalPayable      = findViewById(R.id.tvTotalPayable)

        paymentCash        = findViewById(R.id.paymentCash)
        paymentOnline      = findViewById(R.id.paymentOnline)
        paymentOnlineAfter = findViewById(R.id.paymentOnlineAfter)

        btnConfirmDispatch = findViewById(R.id.btnConfirmDispatch)

        ivCashIcon  = findViewById(R.id.ivCashIcon)
        tvCashTitle = findViewById(R.id.tvCashTitle)
        tvCashDesc  = findViewById(R.id.tvCashDesc)

        ivOnlineIcon  = findViewById(R.id.ivOnlineIcon)
        tvOnlineTitle = findViewById(R.id.tvOnlineTitle)
        tvOnlineDesc  = findViewById(R.id.tvOnlineDesc)

        ivOnlineAfterIcon  = findViewById(R.id.ivOnlineAfterIcon)
        tvOnlineAfterTitle = findViewById(R.id.tvOnlineAfterTitle)

        // =========================================================
        // DATABASE
        // =========================================================
        dbHelper = HotelDatabaseHelper(this)

        // =========================================================
        // BACK
        // =========================================================
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        // =========================================================
        // RECYCLER VIEW
        // =========================================================
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(false)

        cartAdapter = CartItemAdapter(cartItems, dbHelper) {
            updatePaymentSummary()
        }
        recyclerView.adapter = cartAdapter

        // =========================================================
        // PAYMENT METHOD SELECTION
        // =========================================================
        paymentCash.setOnClickListener {
            selectedPaymentMethod = "cash"
            applyPaymentSelection()
        }
        paymentOnline.setOnClickListener {
            selectedPaymentMethod = "online"
            applyPaymentSelection()
        }
        paymentOnlineAfter.setOnClickListener {
            selectedPaymentMethod = "online_after"
            applyPaymentSelection()
        }

        // =========================================================
        // LOAD CART DATA
        // =========================================================
        loadCartData()
        val spCred = getSharedPreferences("credentials", MODE_PRIVATE)

        val idString = spCred.getString("Id", "0") ?: "0"
        Id = try {
            BigInteger(idString)
        } catch (e: NumberFormatException) {
            BigInteger.ZERO
        }

        HotelCommissionHelper.fetchCommissionCharge(
            context = this,
            hotelRegistrationId = Id,
            onSuccess = { charge ->

                HOTEL_CHARGE = charge

                Log.d("HotelPayment", "Commission received: ₹$HOTEL_CHARGE")

                // 🔑 Re-render the price summary with the new value
                runOnUiThread {
                    updatePaymentSummary()
                }
            },
            onError = { message ->
                Log.e("HotelPayment", message)

                // Optional: keep a fallback value so UI doesn't show ₹0
                HOTEL_CHARGE = 0.0
                runOnUiThread {
                    updatePaymentSummary()
                }
            }
        )

        // =========================================================
        // CONFIRM BUTTON
        // =========================================================
        btnConfirmDispatch.setOnClickListener {

            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedPaymentMethod == null) {
                Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnConfirmDispatch.isEnabled = false
            val originalText = btnConfirmDispatch.text
            btnConfirmDispatch.text = "Submitting..."

            submitBookingToBackend(
                paymentMethod    = selectedPaymentMethod!!,
                expertId         = expertId,
                expertName       = expertName,
                employeeId       = employeeId,
                hotelId          = hotelId,
                totalDuration    = totalMassageDuration,
                needBed          = needBed,
                bookingDate      = bookingDate,
                bookingTime      = bookingTime,
                clientGender     = clientGender,
                selectedService  = selectedService,
                cartItems        = cartItems,

                onSuccess = { responseJson ->

                    // ----- Read orderId + otp from server response -----
                    val orderId = responseJson
                        .optString("orderId", "N/A")
                        .takeIf { it.isNotBlank() && it != "null" }
                        ?: "N/A"

                    val bookingOtp = responseJson
                        .optJSONObject("booking")
                        ?.optString("booking_otp")
                        ?.takeIf { it.isNotBlank() && it != "null" }
                        ?: responseJson
                            .optString("booking_otp", "0000")
                            .takeIf { it.isNotBlank() && it != "null" }
                        ?: "0000"

                    Log.d("BookingAPI", "OrderId=$orderId | OTP=$bookingOtp")

                    runOnUiThread {
                        btnConfirmDispatch.isEnabled = true
                        btnConfirmDispatch.text = originalText

                        showWhatsAppDialog(
                            orderId       = orderId,
                            guestName     = guestName,
                            guestMobile   = guestMobile,
                            bookingDate   = bookingDate,
                            bookingTime   = bookingTime,
                            therapistName = expertName,
                            otp           = bookingOtp,
                            onCartCleared = {
                                Log.d("HotelPayment", "Cart cleared after dialog closed")
                            }
                        )
                    }
                },

                onError = { errMsg ->
                    runOnUiThread {
                        btnConfirmDispatch.isEnabled = true
                        btnConfirmDispatch.text = originalText
                        Toast.makeText(this, "Failed: $errMsg", Toast.LENGTH_LONG).show()
                    }
                }
            )
        }
    }

    // =========================================================
    // SHOW WHATSAPP CONFIRMATION DIALOG
    // =========================================================
    private fun showWhatsAppDialog(
        orderId: String,
        guestName: String,
        guestMobile: String,
        bookingDate: String,
        bookingTime: String,
        therapistName: String,
        otp: String = "0000",
        onCartCleared: () -> Unit = {}
    ) {

        val message = buildWhatsAppMessage(
            orderId = orderId,
            guestName = guestName,
            date = bookingDate,
            time = bookingTime,
            therapistName = therapistName,
            otp = otp
        )

        val dialogView = layoutInflater.inflate(R.layout.dialog_whatsapp_confirm, null)

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val tvSub = dialogView.findViewById<TextView>(R.id.tvDialogSubtitle)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tvDialogMessage)
        val btnSend = dialogView.findViewById<TextView>(R.id.btnSendWhatsApp)
        val btnClose = dialogView.findViewById<TextView>(R.id.btnCloseDialog)

        tvTitle.text = "Booking Confirmed!"
        tvSub.text = "Order #$orderId"

        // Hide the optional message preview if you don't want it
        // tvMessage.text = "Your massage appointment has been successfully scheduled."

        // ---- One-shot cart clear ----
// ---- One-shot cart clear + navigation ----
        var cartCleared = false


        fun clearCartOnce() {
            if (!cartCleared) {
                cartCleared = true
                dbHelper.clearCart()
                loadCartData()
                onCartCleared()
            }
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

// ----- Send on WhatsApp -----
        btnSend.setOnClickListener {
            val cleanedNumber = guestMobile.replace(Regex("[^\\d]"), "")

            if (cleanedNumber.isEmpty()) {
                Toast.makeText(this, "Guest mobile missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val encoded = Uri.encode(message)
            val url = "https://wa.me/$cleanedNumber?text=$encoded"

            // Clear cart now
            clearCartOnce()

            // Mark that we should navigate when user comes back
            pendingNavigation = true

            dialog.dismiss()

            try {
                val whatsappIntent = android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    Uri.parse(url)
                )
                startActivity(whatsappIntent)
            } catch (e: Exception) {
                Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_LONG).show()
                // WhatsApp not available → go to main anyway
                goToMainActivity()
            }
        }

// ----- Close button -----
        btnClose.setOnClickListener {
            clearCartOnce()
            dialog.dismiss()
            finish()   // only close when user explicitly closes
        }

// ----- Dismiss listener (back press / outside tap) -----
        dialog.setOnDismissListener {
            clearCartOnce()
            // Don't call finish() here — let the user stay on the page if they just dismissed
        }

        dialog.show()
    }

    // =========================================================
    // BUILD WHATSAPP MESSAGE
    // =========================================================
    private fun buildWhatsAppMessage(
        orderId: String,
        guestName: String,
        date: String,
        time: String,
        therapistName: String,
        otp: String = "0000"
    ): String {

        val b = computePriceBreakdown()

        val servicesList = cartItems.joinToString("\n") { item ->
            val lineTotal = item.discountPrice * item.quantity
            "•  ${item.productName} (${item.duration} minutes) x ${item.quantity} = ₹${"%.2f".format(lineTotal)}"
        }

        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val wish = when (hour) {
            in 0..11  -> "Morning"
            in 12..16 -> "Afternoon"
            in 17..20 -> "Evening"
            else      -> "Night"
        }

        return """
Hello $guestName, Good $wish!

We're excited to let you know that *your massage booking with JIIFIT has been successfully confirmed!*
*Jiifit – India's 1st specialised massage company for client's place* 🧖

📌*Booking Summary:*
• Booking ID: $orderId
• Booking Date: $date
• Booking Time: $time

*Selected Massage:*
$servicesList

• Service Charges: ₹${"%.2f".format(b.discountedTotal)}
• GST (5%): ₹${"%.2f".format(b.gst)}
• Hotel Commission: ₹${"%.2f".format(b.hotelCommission)}
• *Total Amount Payable: ₹${"%.2f".format(b.totalPayable)}*

AFTER MASSAGE YOU CAN PAY BY UPI / CASH

*Therapist Details:*
• Name: $therapistName

*OTP: $otp*
Share this OTP with the Therapist before start your session

*Want to know more?*

• view Therapist full profile
• cancel, and reschedule
• get Booking history
• get contact number of the Therapist
*Get all the details one step away visit jiifit.com*

*Thank you for choosing Jiifit!*
We look forward to providing you a relaxing and rejuvenating experience right in the comfort of your place.

*Warm regards*,
*Team Jiifit*
Empower your wellness journey with Jiifit
    """.trimIndent()
    }

    // =========================================================
    // SUBMIT BOOKING (multipart: fields + voice note)
    // =========================================================
    private fun submitBookingToBackend(
        paymentMethod: String,
        expertId: String,
        expertName: String,
        employeeId: String,
        hotelId: String,
        totalDuration: Int,
        needBed: Int,
        bookingDate: String,
        bookingTime: String,
        clientGender: String,
        selectedService: String,
        cartItems: List<HotelDatabaseHelper.CartItem>,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = Constant.HOTEL_BOOKING_API

        val breakdown = computePriceBreakdown()

        val itemsArray = JSONArray()
        for (item in cartItems) {
            val jsonItem = JSONObject().apply {
                put("id", item.id)
                put("product_id", item.productId)
                put("product_name", item.productName)
                put("original_price", item.originalPrice)
                put("discount_price", item.discountPrice)
                put("quantity", item.quantity)
                put("duration", item.duration)
                put("type", item.type)
                put("discount", item.discount)
            }
            itemsArray.put(jsonItem)
        }

        val builder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("payment_method",   paymentMethod)
            .addFormDataPart("expert_id",        expertId)
            .addFormDataPart("expert_name",      expertName)
            .addFormDataPart("employee_id",      employeeId)
            .addFormDataPart("hotel_id",         hotelId)
            .addFormDataPart("total_duration",   totalDuration.toString())
            .addFormDataPart("need_bed",         needBed.toString())
            .addFormDataPart("booking_date",     bookingDate)
            .addFormDataPart("booking_time",     bookingTime)
            .addFormDataPart("client_gender",    clientGender)
            .addFormDataPart("selected_service", selectedService)
            .addFormDataPart("cart_items",       itemsArray.toString())
            .addFormDataPart("total_original",   breakdown.originalTotal.toString())
            .addFormDataPart("total_discounted", breakdown.discountedTotal.toString())
            .addFormDataPart("gst",              breakdown.gst.toString())
            .addFormDataPart("hotel_commission", breakdown.hotelCommission.toString())
            .addFormDataPart("total_payable",    breakdown.totalPayable.toString())
            .addFormDataPart("total_items",      cartItems.size.toString())
            .addFormDataPart("guest_name",       guestName)
            .addFormDataPart("country_code",     country_Code)
            .addFormDataPart("guest_mobile", Mobile)
            .addFormDataPart("room_id",          roomId)
            .addFormDataPart("room_number",      roomNumber)
            .addFormDataPart("floor_wing",       floorWing)
            .addFormDataPart("ring_doorbell",    if (ringDoorbell)  "1" else "0")
            .addFormDataPart("do_not_call",      if (doNotCall)     "1" else "0")
            .addFormDataPart("call_concierge",   if (callConcierge) "1" else "0")
            .addFormDataPart("special_note",     specialNote)

        // Attach voice note if present
        if (voiceNotePath.isNotEmpty()) {
            val file = File(voiceNotePath)
            if (file.exists()) {
                val mediaType = when (file.extension.lowercase()) {
                    "mp3" -> "audio/mpeg"
                    "m4a" -> "audio/mp4"
                    "aac" -> "audio/aac"
                    "wav" -> "audio/wav"
                    "ogg" -> "audio/ogg"
                    "3gp" -> "audio/3gpp"
                    else  -> "application/octet-stream"
                }.toMediaTypeOrNull()

                builder.addFormDataPart(
                    "voice_note",
                    file.name,
                    file.asRequestBody(mediaType)
                )
                Log.d("BookingAPI", "Voice note attached: ${file.name} (${file.length()} bytes)")
            } else {
                Log.w("BookingAPI", "Voice file missing on device: $voiceNotePath")
            }
        } else {
            Log.d("BookingAPI", "No voice note to attach")
        }

        val requestBody = builder.build()

        val request = Request.Builder()
            .url(url)
            .addHeader("Accept", "application/json")
            .post(requestBody)
            .build()

        Log.d("BookingAPI", "POST → $url")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("BookingAPI", "Failure", e)
                runOnUiThread { onError(e.message ?: "Network error") }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string().orEmpty()
                Log.d("BookingAPI", "Code=${response.code} Body=$body")

                if (!response.isSuccessful) {
                    runOnUiThread { onError("Server error: ${response.code}") }
                    return
                }

                try {
                    val json = JSONObject(body)
                    val success = json.optBoolean("success", false) ||
                            json.optBoolean("status",  false)
                    if (success) {
                        runOnUiThread { onSuccess(json) }
                    } else {
                        runOnUiThread {
                            onError(json.optString("message", "Unknown server error"))
                        }
                    }
                } catch (e: Exception) {
                    Log.e("BookingAPI", "Parse error", e)
                    runOnUiThread { onError("Invalid server response") }
                }
            }
        })
    }

    // =========================================================
    // LOAD CART DATA
    // =========================================================
    private fun loadCartData() {
        cartItems.clear()
        cartItems.addAll(dbHelper.getAllCartItems())
        cartAdapter.notifyDataSetChanged()
        updatePaymentSummary()
    }

    // =========================================================
    // UPDATE PAYMENT SUMMARY
    // =========================================================
    @SuppressLint("SetTextI18n")
    private fun updatePaymentSummary() {

        val b = computePriceBreakdown()

        tvServiceOriginal.text = "₹${formatPrice(b.originalTotal)}"
        tvServiceOriginal.paintFlags =
            tvServiceOriginal.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        tvServiceOriginal.setTextColor(
            ContextCompat.getColor(this, android.R.color.holo_red_dark)
        )

        tvServiceDiscounted.text = "₹${formatPrice(b.discountedTotal)}"
        tvGst.text               = "₹${formatPrice(b.gst)}"
        tvHotelCommission.text   = "₹${formatPrice(b.hotelCommission)}"
        tvTotalPayable.text      = "₹${formatPrice(b.totalPayable)}"

        recyclerView.visibility = if (cartItems.isEmpty()) View.GONE else View.VISIBLE

        updateConfirmButtonState()
    }

    // =========================================================
    // CONFIRM BUTTON ENABLE / DISABLE
    // =========================================================
    private fun updateConfirmButtonState() {

        val canConfirm = cartItems.isNotEmpty() && selectedPaymentMethod != null

        if (canConfirm) {
            btnConfirmDispatch.isEnabled   = true
            btnConfirmDispatch.isClickable = true
            btnConfirmDispatch.alpha = 1f
            btnConfirmDispatch.setTextColor(
                ContextCompat.getColor(this, android.R.color.white)
            )
            btnConfirmDispatch.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.Button_Blue)
            btnConfirmDispatch.setBackgroundResource(R.drawable.bg_confirm_button)
        } else {
            btnConfirmDispatch.isEnabled   = false
            btnConfirmDispatch.isClickable = false
            btnConfirmDispatch.alpha = 1f
            btnConfirmDispatch.setTextColor(
                ContextCompat.getColor(this, android.R.color.white)
            )
            btnConfirmDispatch.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.Gray)
        }
    }

    // =========================================================
    // PAYMENT METHOD VISUAL UPDATE
    // =========================================================
    private fun applyPaymentSelection() {

        val selectedColor    = ContextCompat.getColor(this, R.color.PrimaryBlue)
        val normalTitleColor = ContextCompat.getColor(this, R.color.DarkText)
        val normalDescColor  = ContextCompat.getColor(this, R.color.BodyText)

        val cashSelected = selectedPaymentMethod == "cash"
        paymentCash.setBackgroundResource(
            if (cashSelected) R.drawable.bg_payment_selected
            else R.drawable.bg_payment_option
        )
        ivCashIcon.imageTintList = ColorStateList.valueOf(
            if (cashSelected) selectedColor else normalTitleColor
        )
        tvCashTitle.setTextColor(if (cashSelected) selectedColor else normalTitleColor)
        tvCashDesc.setTextColor(if (cashSelected) selectedColor else normalDescColor)
        tvCashDesc.alpha = if (cashSelected) 1f else 0.85f

        val onlineSelected = selectedPaymentMethod == "online"
        paymentOnline.setBackgroundResource(
            if (onlineSelected) R.drawable.bg_payment_selected
            else R.drawable.bg_payment_option
        )
        ivOnlineIcon.imageTintList = ColorStateList.valueOf(
            if (onlineSelected) selectedColor else normalTitleColor
        )
        tvOnlineTitle.setTextColor(if (onlineSelected) selectedColor else normalTitleColor)
        tvOnlineDesc.setTextColor(if (onlineSelected) selectedColor else normalDescColor)
        tvOnlineDesc.alpha = if (onlineSelected) 1f else 0.85f

        val onlineAfterSelected = selectedPaymentMethod == "online_after"
        paymentOnlineAfter.setBackgroundResource(
            if (onlineAfterSelected) R.drawable.bg_payment_selected
            else R.drawable.bg_payment_option
        )
        ivOnlineAfterIcon.imageTintList = ColorStateList.valueOf(
            if (onlineAfterSelected) selectedColor else normalTitleColor
        )
        tvOnlineAfterTitle.setTextColor(
            if (onlineAfterSelected) selectedColor else normalTitleColor
        )

        updateConfirmButtonState()
    }

    // =========================================================
    // FORMAT PRICE
    // =========================================================
    private fun formatPrice(price: Double): String {
        return if (price % 1.0 == 0.0) {
            price.toInt().toString()
        } else {
            String.format(Locale.US, "%.2f", price)
        }
    }

    private fun goToMainActivity() {
        val intent = android.content.Intent(this, HotelMainActivity::class.java)
        intent.flags =
            android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                    android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    // =========================================================
    // RESUME
    // =========================================================
    override fun onResume() {
        super.onResume()
        if (::cartAdapter.isInitialized) {
            loadCartData()
        }

        if (pendingNavigation) {
            pendingNavigation = false
            goToMainActivity()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}