package com.company.jifiit

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import android.text.Html

class ExpertEarningActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var btnContinueRegister: MaterialButton
    private lateinit var btnViewFullSlab: MaterialButton
    private lateinit var tvExampleText: TextView
    private lateinit var tvExpertDescription: TextView
    private lateinit var helpBtn: TextView

    private val fullText = "Designed for verified wellness and therapy experts specializing in massage, yoga, physiotherapy and more . Receive doorstep booking requests right from your home Earn based on completed bookings, service rates, and customer demand in your area with high commission earnings better than any other company."
    private val shortText = "Designed for verified wellness and therapy experts specializing in massage, yoga, physiotherapy... "
    private val readMoreText = "Read More"
    private val readLessText = " Read Less"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expert_earning)

        setupStatusBar()
        initViews()
        setupReadMoreDescription()
        setupListeners()

        val exampleHtml = "Example: If you get a <b><font color=\"#0F172A\">₹1,000 booking</font></b>, Jiifit fee is ₹215 → <b><font color=\"#047857\">You receive ₹785</font></b> directly into your wallet!"

        tvExampleText.text = Html.fromHtml(exampleHtml, Html.FROM_HTML_MODE_COMPACT)
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
        btnViewFullSlab = findViewById(R.id.btnViewFullSlab)
        helpBtn = findViewById(R.id.helpBtn)
        tvExampleText = findViewById(R.id.tvExampleText)
        tvExpertDescription = findViewById(R.id.tvExpertDescription)
    }

    private fun setupReadMoreDescription() {
        tvExpertDescription.movementMethod = LinkMovementMethod.getInstance()
        tvExpertDescription.highlightColor = Color.TRANSPARENT
        showCollapsed()
    }

    private fun showCollapsed() {
        val spannable = SpannableString(shortText + readMoreText)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                showExpanded()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = Color.parseColor("#FFFFFF")
                ds.isFakeBoldText = true
            }
        }

        spannable.setSpan(
            clickableSpan,
            shortText.length,
            shortText.length + readMoreText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        tvExpertDescription.text = spannable
    }

    private fun showExpanded() {
        val spannable = SpannableString(fullText + readLessText)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                showCollapsed()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = Color.parseColor("#FFFFFF")
                ds.isFakeBoldText = true
            }
        }

        spannable.setSpan(
            clickableSpan,
            fullText.length,
            fullText.length + readLessText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        tvExpertDescription.text = spannable
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

        btnViewFullSlab.setOnClickListener {
            val intent = Intent(this@ExpertEarningActivity, Register::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        helpBtn.setOnClickListener {
            val intent = Intent(this@ExpertEarningActivity, Register::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}