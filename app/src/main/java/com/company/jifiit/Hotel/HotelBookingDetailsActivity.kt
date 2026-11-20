package com.company.jifiit.Hotel

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.company.jifiit.Constant
import com.company.jifiit.R
import org.json.JSONObject

class HotelBookingDetailsActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private var bookingId: String? = null

    companion object {
        private const val TAG = "HotelBookingDetails"

        // Replace with your actual backend API URL
        private var API_URL = Constant.HOTEL_BOOKING_Details
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        WindowCompat.getInsetsController(
            window,
            window.decorView
        ).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        setContentView(R.layout.activity_hotel_booking_details)

        bookingId = intent.getStringExtra("booking_id")

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v, insets ->
            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            overridePendingTransition(
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
        }

        if (!bookingId.isNullOrBlank()) {
            fetchBookingDetails(bookingId!!)
        } else {
            Toast.makeText(
                this,
                "Booking ID not found",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun fetchBookingDetails(id: String) {

        val params = JSONObject().apply {
            put("booking_id", id)
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            API_URL,
            params,
            { response ->

                try {
                    val status = response.optString("status")

                    if (status == "success") {

                        val data = response.getJSONObject("data")

                        val customerId =
                            data.optString("customer_id")

                        val orderDate =
                            data.optString("order_date")

                        val massageTypes =
                            data.optString("massage_types")

                        val startTime =
                            data.optString("start_time")

                        val endTime =
                            data.optString("end_time")

                        val note =
                            data.optString("note")

                        val fullAddress =
                            data.optString("full_address")

                        val needBed =
                            data.optInt("need_bed")

                        val callAfterBooking =
                            data.optInt("call_after_booking")

                        val ringBell =
                            data.optInt("ring_bell")

                        val doNotCallBeforeReaching =
                            data.optInt("do_not_call_before_reaching")

                        val userStatus =
                            data.optString("user_status")

                        val bookingStatus =
                            data.optString("status")

                        val otpStatus =
                            data.optString("otp_status")

                        val paymentStatus =
                            data.optString("payment_status")

                        val mobile =
                            data.optString("mobile")

                        val totalPrice =
                            data.optString("total_price")

                        val paymentMode =
                            data.optString("payment_mode")

                        val whoTake =
                            data.optString("who_take")

                        val receiverName =
                            data.optString("reciver_name")

                        val receiverWhatsapp =
                            data.optString("r_wp_num")

                        val relationship =
                            data.optString("relationship")

                        val photoPath =
                            data.optString("photoPath")

                        val otp =
                            data.optString("otp")

                        // Check received data in Logcat
                        Log.d(TAG, "Customer ID: $customerId")
                        Log.d(TAG, "Order Date: $orderDate")
                        Log.d(TAG, "Massage: $massageTypes")
                        Log.d(TAG, "Start Time: $startTime")
                        Log.d(TAG, "End Time: $endTime")
                        Log.d(TAG, "Address: $fullAddress")
                        Log.d(TAG, "Booking Status: $bookingStatus")
                        Log.d(TAG, "Payment Status: $paymentStatus")
                        Log.d(TAG, "Total Price: $totalPrice")
                        Log.d(TAG, "Payment Mode: $paymentMode")

                        // Update your TextViews here.

                    } else {
                        val message =
                            response.optString(
                                "message",
                                "Failed to fetch booking details"
                            )

                        Toast.makeText(
                            this,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Response parsing error", e)

                    Toast.makeText(
                        this,
                        "Error reading booking details",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            { error ->

                Log.e(
                    TAG,
                    "API request failed: ${error.message}",
                    error
                )

                Toast.makeText(
                    this,
                    "Failed to load booking details",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }
}