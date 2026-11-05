package com.company.jifiit

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class TrainingRegistrationKitActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var headerPricing: LinearLayout
    private lateinit var ivPricingChevron: ImageView
    private lateinit var layoutPricingContent: LinearLayout

    private lateinit var headerNextStep: LinearLayout
    private lateinit var ivNextStepChevron: ImageView
    private lateinit var layoutNextStepContent: LinearLayout

    private lateinit var headerNextStepDetails: LinearLayout
    private lateinit var ivNextStepDetailsChevron: ImageView
    private lateinit var layoutNextStepDetailsContent: LinearLayout

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

    private var isPricingExpanded = true
    private var isNextStepExpanded = false
    private var isNextStepDetailsExpanded = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training_registration_kit)

        setupStatusBar()
        initViews()
        setupMassageRecyclerView()
        setupListeners()
        updateTotalAmount()
    }

    private fun setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = Color.parseColor("#2563EB")
            // Dark background -> white status bar icons
            window.decorView.systemUiVisibility = 0
        }
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        headerPricing = findViewById(R.id.headerPricing)
        ivPricingChevron = findViewById(R.id.ivPricingChevron)
        layoutPricingContent = findViewById(R.id.layoutPricingContent)

        headerNextStep = findViewById(R.id.headerNextStep)
        ivNextStepChevron = findViewById(R.id.ivNextStepChevron)
        layoutNextStepContent = findViewById(R.id.layoutNextStepContent)

        headerNextStepDetails = findViewById(R.id.headerNextStepDetails)
        ivNextStepDetailsChevron = findViewById(R.id.ivNextStepDetailsChevron)
        layoutNextStepDetailsContent = findViewById(R.id.layoutNextStepDetailsContent)

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

    private val defaultTrainingPrice = 599
    private val defaultUniformPrice = 1399
    private val defaultOtherKitPrice = 799
    private val defaultBedPrice = 20000

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
        val massageList = listOf(
            MassageStepItem("Deep Tissue Massage", "(Live will check)", isApproved = true, statusText = "Approved"),
            MassageStepItem("Head Massage", "(Company will provide training)", isApproved = true, statusText = "Approved"),
            MassageStepItem("Swedish Massage", "(Live will check)", isApproved = true, statusText = "Approved"),
            MassageStepItem("Aromatherapy Massage", "(Company will provide training)", isApproved = false, statusText = "Pending"),
            MassageStepItem("Potli Ayurvedic Massage", "(Company will provide training)", isApproved = false, statusText = "Training Required"),
            MassageStepItem("Foot Reflexology", "(Live will check)", isApproved = true, statusText = "Approved"),
            MassageStepItem("Balinese Body Massage", "(Company will provide training)", isApproved = false, statusText = "Pending"),
            MassageStepItem("Thai Yoga Stretch Massage", "(Company will provide training)", isApproved = false, statusText = "Pending")
        )

        rvMassageList.layoutManager = LinearLayoutManager(this)
        rvMassageList.adapter = MassageStepAdapter(this, massageList)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        // Toggle Pricing card
        headerPricing.setOnClickListener {
            isPricingExpanded = !isPricingExpanded
            layoutPricingContent.visibility = if (isPricingExpanded) View.VISIBLE else View.GONE
            ivPricingChevron.animate().rotation(if (isPricingExpanded) 0f else 180f).setDuration(200).start()
        }

        // Toggle Next Step card
        headerNextStep.setOnClickListener {
            isNextStepExpanded = !isNextStepExpanded
            layoutNextStepContent.visibility = if (isNextStepExpanded) View.VISIBLE else View.GONE
            ivNextStepChevron.animate().rotation(if (isNextStepExpanded) 180f else 0f).setDuration(200).start()
        }

        // Toggle Next Step Details card
        headerNextStepDetails.setOnClickListener {
            isNextStepDetailsExpanded = !isNextStepDetailsExpanded
            layoutNextStepDetailsContent.visibility = if (isNextStepDetailsExpanded) View.VISIBLE else View.GONE
            ivNextStepDetailsChevron.animate().rotation(if (isNextStepDetailsExpanded) 0f else 180f).setDuration(200).start()
        }

        // Massage bed checkbox listener
        cbMassageBed.setOnCheckedChangeListener { _, _ ->
            updateTotalAmount()
        }

        // Allow tapping anywhere on the massage bed card to toggle checkbox
        cardMassageBed.setOnClickListener {
            cbMassageBed.isChecked = !cbMassageBed.isChecked
        }

        // Bottom Button: Process to Kit Training Fees
        btnProcessFees.setOnClickListener {
            startActivity(Intent(this, TrainingPaymentSuccessActivity::class.java))
        }
    }

    private fun onProcessFeesClicked() {
        val total = calculateTotal()
        val formattedTotal = String.format(Locale.US, "%,d", total)
        try {
            // Check if PaymentDialogActivity exists and can be launched
            val intent = Intent(this, PaymentDialogActivity::class.java).apply {
                putExtra("EXTRA_PAYMENT_AMOUNT", total.toString())
                putExtra("EXTRA_PAYMENT_TITLE", "Kit & Training Registration Fee")
                putExtra("EXTRA_INCLUDE_BED", cbMassageBed.isChecked)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        } catch (e: Exception) {
            Toast.makeText(this, "Proceeding to Kit & Training Fee payment: ₹$formattedTotal", Toast.LENGTH_SHORT).show()
        }
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
        private val items: List<MassageStepItem>
    ) : RecyclerView.Adapter<MassageStepAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvMassageName: TextView = view.findViewById(R.id.tvMassageName)
            val tvMassageSubtitle: TextView = view.findViewById(R.id.tvMassageSubtitle)
            val layoutStatusBadge: LinearLayout = view.findViewById(R.id.layoutStatusBadge)
            val ivStatusIcon: ImageView = view.findViewById(R.id.ivStatusIcon)
            val tvStatusText: TextView = view.findViewById(R.id.tvStatusText)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_massage_step, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.tvMassageName.text = item.name
            holder.tvMassageSubtitle.text = item.subtitle
            holder.tvStatusText.text = item.statusText

            if (item.isApproved) {
                holder.layoutStatusBadge.setBackgroundResource(R.drawable.bg_pill_approved_green)
                holder.ivStatusIcon.setImageResource(R.drawable.baseline_check_circle_outline_24)
                holder.ivStatusIcon.setColorFilter(Color.parseColor("#059669"))
                holder.tvStatusText.setTextColor(Color.parseColor("#059669"))
            } else {
                holder.layoutStatusBadge.setBackgroundResource(R.drawable.bg_pill_pending_amber)
                holder.ivStatusIcon.setImageResource(R.drawable.ic_clock_outline)
                holder.ivStatusIcon.setColorFilter(Color.parseColor("#D97706"))
                holder.tvStatusText.setTextColor(Color.parseColor("#D97706"))
            }
        }

        override fun getItemCount(): Int = items.size
    }
}

