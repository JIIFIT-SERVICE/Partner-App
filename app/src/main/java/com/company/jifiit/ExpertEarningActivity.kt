package com.company.jifiit

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

/**
 * ExpertEarningActivity
 * Displays the projected income, fee transparent slabs, and certified expert perks.
 * Shown when the user clicks "Join" -> "Join as Certified Expert" in JoinerTypes,
 * before navigating into the Create Account / Register screen.
 */
class ExpertEarningActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var btnContinueRegister: MaterialButton

    private lateinit var chipSlab1: TextView
    private lateinit var chipSlab2: TextView
    private lateinit var chipSlab3: TextView
    private lateinit var chipSlab4: TextView

    private lateinit var tvExampleText: TextView
    private lateinit var tvBookingAmount: TextView
    private lateinit var tvJiifitFeeLabel: TextView
    private lateinit var tvJiifitFee: TextView
    private lateinit var tvPayoutSharePercentage: TextView
    private lateinit var tvTakeHomeAmount: TextView

    private val colorSelectedText = Color.parseColor("#1A56DB")
    private val colorUnselectedText = Color.parseColor("#475569")

    data class SlabInfo(
        val bookingAmount: String,
        val feeLabel: String,
        val feeAmount: String,
        val payoutPercentage: String,
        val takeHomeAmount: String,
        val exampleText: String
    )

    private val slabMap = mapOf(
        1 to SlabInfo(
            bookingAmount = "₹650",
            feeLabel = "Jiifit Platform & Support Fee (~20%)",
            feeAmount = "- ₹130",
            payoutPercentage = "Keep 80% of booking fee",
            takeHomeAmount = "₹520",
            exampleText = "Example: If you get a ₹650 booking, Jiifit fee is ₹130 → You receive ₹520 directly into your bank account!"
        ),
        2 to SlabInfo(
            bookingAmount = "₹1,000",
            feeLabel = "Jiifit Platform & Support Fee (~21.5%)",
            feeAmount = "- ₹215",
            payoutPercentage = "Keep 78.5% of booking fee",
            takeHomeAmount = "₹785",
            exampleText = "Example: If you get a ₹1,000 booking, Jiifit fee is ₹215 → You receive ₹785 directly into your bank account!"
        ),
        3 to SlabInfo(
            bookingAmount = "₹1,500",
            feeLabel = "Jiifit Platform & Support Fee (~20%)",
            feeAmount = "- ₹300",
            payoutPercentage = "Keep 80% of booking fee",
            takeHomeAmount = "₹1,200",
            exampleText = "Example: If you get a ₹1,500 booking, Jiifit fee is ₹300 → You receive ₹1,200 directly into your bank account!"
        ),
        4 to SlabInfo(
            bookingAmount = "₹2,500",
            feeLabel = "Jiifit Platform & Support Fee (~18%)",
            feeAmount = "- ₹450",
            payoutPercentage = "Keep 82% of booking fee",
            takeHomeAmount = "₹2,050",
            exampleText = "Example: If you get a ₹2,500 booking, Jiifit fee is ₹450 → You receive ₹2,050 directly into your bank account!"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expert_earning)

        setupStatusBar()
        initViews()
        setupListeners()

        // Default select Slab 2 (₹1,000 Booking) as shown in reference design
        selectSlab(2)
    }

    private fun setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = Color.parseColor("#FFFFFF")
        }
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnContinueRegister = findViewById(R.id.btnContinueRegister)

        chipSlab1 = findViewById(R.id.chipSlab1)
        chipSlab2 = findViewById(R.id.chipSlab2)
        chipSlab3 = findViewById(R.id.chipSlab3)
        chipSlab4 = findViewById(R.id.chipSlab4)

        tvExampleText = findViewById(R.id.tvExampleText)
        tvBookingAmount = findViewById(R.id.tvBookingAmount)
        tvJiifitFeeLabel = findViewById(R.id.tvJiifitFeeLabel)
        tvJiifitFee = findViewById(R.id.tvJiifitFee)
        tvPayoutSharePercentage = findViewById(R.id.tvPayoutSharePercentage)
        tvTakeHomeAmount = findViewById(R.id.tvTakeHomeAmount)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        btnContinueRegister.setOnClickListener {
            val intent = Intent(this@ExpertEarningActivity, Register::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        chipSlab1.setOnClickListener { selectSlab(1) }
        chipSlab2.setOnClickListener { selectSlab(2) }
        chipSlab3.setOnClickListener { selectSlab(3) }
        chipSlab4.setOnClickListener { selectSlab(4) }
    }

    private fun selectSlab(index: Int) {
        val chips = listOf(chipSlab1, chipSlab2, chipSlab3, chipSlab4)

        chips.forEachIndexed { i, chip ->
            val isCurrent = (i + 1) == index
            chip.setBackgroundResource(
                if (isCurrent) R.drawable.bg_slab_chip_selected
                else R.drawable.bg_slab_chip_unselected
            )
            chip.setTextColor(if (isCurrent) colorSelectedText else colorUnselectedText)
        }

        val slab = slabMap[index] ?: return
        tvExampleText.text = slab.exampleText
        tvBookingAmount.text = slab.bookingAmount
        tvJiifitFeeLabel.text = slab.feeLabel
        tvJiifitFee.text = slab.feeAmount
        tvPayoutSharePercentage.text = slab.payoutPercentage
        tvTakeHomeAmount.text = slab.takeHomeAmount
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
