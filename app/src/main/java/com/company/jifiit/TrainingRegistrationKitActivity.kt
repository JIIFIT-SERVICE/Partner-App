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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
            val total = calculateTotal()
            val randomTxn = "TXN" + (100000000..999999999).random()
            val intent = Intent(this, TrainingPaymentSuccessActivity::class.java).apply {
                putExtra(TrainingPaymentSuccessActivity.EXTRA_TRANSACTION_ID, randomTxn)
                putExtra(TrainingPaymentSuccessActivity.EXTRA_REGISTRATION_STATUS, "Confirmed")
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
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
    ) : RecyclerView.Adapter<MassageStepAdapter.MassageViewHolder>() {

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
