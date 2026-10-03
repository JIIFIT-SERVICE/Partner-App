package com.company.jifiit.Hotel

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.company.jifiit.Hotel.Adapter.CartItemAdapter
import com.company.jifiit.LocalStorage.HotelDatabaseHelper
import com.company.jifiit.R
import com.google.android.material.button.MaterialButton
import java.util.Locale

class YourCartActivity : AppCompatActivity() {

    // =========================
    // VIEWS
    // =========================

    private lateinit var recyclerView: RecyclerView

    private lateinit var serviceTotalOriginal: TextView
    private lateinit var serviceTotalDiscounted: TextView
    private lateinit var selectedByYou: TextView

    private lateinit var gstAmount: TextView
    private lateinit var hotelChargeAmount: TextView
    private lateinit var payableChargeAmount: TextView

    private lateinit var orderBtn: MaterialButton
    private lateinit var btnBack: ImageView

    // =========================
    // DATABASE
    // =========================

    private lateinit var dbHelper: HotelDatabaseHelper

    // =========================
    // ADAPTER
    // =========================

    private lateinit var cartAdapter: CartItemAdapter

    private val cartItems =
        mutableListOf<HotelDatabaseHelper.CartItem>()

    // =========================
    // CONSTANTS
    // =========================

    private val GST_PERCENT = 5.0
    private val HOTEL_CHARGE = 500.0

    private lateinit var service: String
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // =========================
        // SYSTEM BAR
        // =========================

        WindowCompat.getInsetsController(
            window,
            window.decorView
        ).apply {

            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        // =========================
        // LAYOUT
        // =========================

        setContentView(R.layout.activity_your_cart)

        // =========================
        // WINDOW INSETS
        // =========================

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v, insets ->

            val systemBars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        service = intent.getStringExtra("SELECTED_SERVICE") ?: ""

        // =========================
        // INITIALIZE VIEWS
        // =========================

        btnBack =
            findViewById(R.id.btnBack)

        recyclerView =
            findViewById(R.id.add_to_cart_category)

        serviceTotalOriginal =
            findViewById(R.id.service_total_original)

        serviceTotalDiscounted =
            findViewById(R.id.service_total_discounted)

        selectedByYou =
            findViewById(R.id.selected_by_you)

        gstAmount =
            findViewById(R.id.gst_amount)

        hotelChargeAmount =
            findViewById(R.id.hotel_charge_amount)

        payableChargeAmount =
            findViewById(R.id.payable_charge_amount)

        orderBtn =
            findViewById(R.id.orderBtn)

        // =========================
        // DATABASE
        // =========================

        dbHelper =
            HotelDatabaseHelper(this)

        // =========================
        // BACK BUTTON
        // =========================

        btnBack.setOnClickListener {

            onBackPressedDispatcher.onBackPressed()

            overridePendingTransition(
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
        }

        orderBtn.setOnClickListener {
            val intent = Intent(this, Room_Info::class.java).apply {
                putExtra("SELECTED_SERVICE", service)
            }
            startActivity(intent)
            overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
        }
        // =========================
        // RECYCLER VIEW
        // =========================

        recyclerView.layoutManager =
            LinearLayoutManager(this)

        recyclerView.setHasFixedSize(false)

        cartAdapter = CartItemAdapter(
            cartItems,
            dbHelper
        ) {

            // Called whenever:
            // quantity changes
            // OR item is deleted

            updateCartSummary()
        }

        recyclerView.adapter =
            cartAdapter

        // =========================
        // LOAD CART
        // =========================

        loadCart()
    }

    // =========================================================
    // LOAD CART
    // =========================================================

    private fun loadCart() {

        cartItems.clear()

        cartItems.addAll(
            dbHelper.getAllCartItems()
        )

        cartAdapter.notifyDataSetChanged()

        updateCartSummary()
    }

    // =========================================================
    // UPDATE CART SUMMARY
    // =========================================================

    @SuppressLint("ResourceAsColor")
    private fun updateCartSummary() {

        // =====================================================
        // CART QUANTITY
        // =====================================================

        val totalRows = cartItems.size

        selectedByYou.text =
            if (totalRows == 0) {

                "Your cart is empty"

            } else {

                "$totalRows item${if (totalRows > 1) "s" else ""} selected"
            }

        // =====================================================
        // ORIGINAL SERVICE TOTAL
        // =====================================================

        val originalTotal =
            cartItems.sumOf {

                it.originalPrice * it.quantity
            }

        // =====================================================
        // DISCOUNTED SERVICE TOTAL
        // =====================================================

        val discountedTotal =
            cartItems.sumOf {

                it.discountPrice * it.quantity
            }

        // =====================================================
        // GST 5%
        // =====================================================
        // GST is calculated on discounted service total.
        // =====================================================

        val gst =
            discountedTotal * GST_PERCENT / 100.0

        // =====================================================
        // HOTEL CHARGE
        // =====================================================

        val hotelCharge =
            if (cartItems.isEmpty()) {
                0.0
            } else {
                HOTEL_CHARGE
            }

        // =====================================================
        // TOTAL PAYABLE
        // =====================================================

        val totalPayable =
            discountedTotal +
                    gst +
                    hotelCharge

        // =====================================================
        // UPDATE SERVICE TOTAL
        // =====================================================

        serviceTotalOriginal.text =
            "₹${formatPrice(originalTotal)}"

        serviceTotalDiscounted.text =
            "₹${formatPrice(discountedTotal)}"

        // =====================================================
        // UPDATE GST
        // =====================================================

        gstAmount.text =
            "₹${formatPrice(gst)}"

        // =====================================================
        // UPDATE HOTEL CHARGE
        // =====================================================

        hotelChargeAmount.text =
            "₹${formatPrice(hotelCharge)}"

        // =====================================================
        // UPDATE TOTAL PAYABLE
        // =====================================================

        payableChargeAmount.text =
            "₹${formatPrice(totalPayable)}"

        // =====================================================
        // LOG
        // =====================================================

        println("========== CART SUMMARY ==========")
        println("Original Total   : ₹${formatPrice(originalTotal)}")
        println("Discounted Total : ₹${formatPrice(discountedTotal)}")
        println("GST 5%            : ₹${formatPrice(gst)}")
        println("Hotel Charge      : ₹${formatPrice(hotelCharge)}")
        println("Total Payable     : ₹${formatPrice(totalPayable)}")
        println("==================================")

        // =====================================================
        // EMPTY CART UI
        // =====================================================

        if (cartItems.isEmpty()) {

            recyclerView.visibility =
                View.GONE

            orderBtn.isEnabled =
                false

            orderBtn.alpha =
                0.5f

            // MaterialButton-এর জন্য backgroundTint ব্যবহার
            orderBtn.backgroundTintList =
                ContextCompat.getColorStateList(
                    this,
                    R.color.Gray
                )

        } else {

            recyclerView.visibility =
                View.VISIBLE

            orderBtn.isEnabled =
                true

            orderBtn.alpha =
                1f

            orderBtn.backgroundTintList =
                ContextCompat.getColorStateList(
                    this,
                    R.color.Button_Blue
                )
        }
    }

    // =========================================================
    // FORMAT PRICE
    // =========================================================

    private fun formatPrice(price: Double): String {

        return if (price % 1.0 == 0.0) {

            price.toInt().toString()

        } else {

            String.format(
                Locale.US,
                "%.2f",
                price
            )
        }
    }

    // =========================================================
    // ACTIVITY RESUME
    // =========================================================

    override fun onResume() {

        super.onResume()

        // User যদি অন্য Activity থেকে ফিরে আসে,
        // তাহলে SQLite থেকে আবার cart load হবে।

        if (::cartAdapter.isInitialized) {

            loadCart()
        }
    }

    // =========================================================
    // BACK PRESS
    // =========================================================

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {

        super.onBackPressed()

        overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
    }
}