package com.company.jifiit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

data class EarningSlab(
    val range: String,
    val fee: String,
    val earning: String,
    val highlight: Boolean = false
)

class MyEarningsActivity : AppCompatActivity() {

    // Static for now, replace with API data when the backend is ready
    private val slabs = listOf(
        EarningSlab("₹0 - ₹250", "₹85", "₹165"),
        EarningSlab("₹251 - ₹549", "₹135", "₹414"),
        EarningSlab("₹550 - ₹750", "₹155", "₹595"),
        EarningSlab("₹751 - ₹1000", "₹215", "₹785", highlight = true),
        EarningSlab("₹1001 - ₹1200", "₹235", "₹965"),
        EarningSlab("₹1201 - ₹1500", "₹285", "₹1,215"),
        EarningSlab("₹1501 - ₹2000", "₹335", "₹1,665"),
        EarningSlab("₹2001 - ₹2500", "₹485", "₹2,015"),
        EarningSlab("Above ₹2500", "₹535", "₹2,465+ (on ₹3000)")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_earnings)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        val container = findViewById<LinearLayout>(R.id.slabContainer)
        val inflater = LayoutInflater.from(this)

        slabs.forEach { slab ->
            val row = inflater.inflate(R.layout.item_earning_slab, container, false)
            row.findViewById<TextView>(R.id.tvRange).text = slab.range
            row.findViewById<TextView>(R.id.tvFee).text = slab.fee
            row.findViewById<TextView>(R.id.tvEarning).text = slab.earning
            if (slab.highlight) {
                row.setBackgroundColor(ContextCompat.getColor(this, R.color.earn_blue_light))
                row.findViewById<TextView>(R.id.tvBadge).visibility = View.VISIBLE
            }
            container.addView(row)
        }
    }
}


