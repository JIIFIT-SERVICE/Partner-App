package com.company.jifiit

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class ExpertEarningSlabsActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var btnContinueRegister: MaterialButton
    private lateinit var helpBtn: TextView
    private lateinit var tvCalculationExample: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expert_earning_slabs)

        setupStatusBar()
        applyWindowInsets()
        initViews()
        setupCalculationText()
        setupListeners()
    }

    private fun setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = Color.parseColor("#FFFFFF")
        }
    }

    private fun applyWindowInsets() {
        val toolbar = findViewById<View>(R.id.toolbar) ?: return
        val initialPaddingTop = toolbar.paddingTop
        val fallbackStatusBarHeight = getStatusBarHeight()

        val main = findViewById<View>(R.id.main)
        if (main != null) {
            ViewCompat.setOnApplyWindowInsetsListener(main) { _, insets ->
                val statusBarInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
                val topPadding = if (statusBarInset > 0) statusBarInset else fallbackStatusBarHeight
                toolbar.setPadding(
                    toolbar.paddingStart,
                    initialPaddingTop + topPadding,
                    toolbar.paddingEnd,
                    toolbar.paddingBottom
                )
                insets
            }
            ViewCompat.requestApplyInsets(main)
        } else {
            toolbar.setPadding(
                toolbar.paddingStart,
                initialPaddingTop + fallbackStatusBarHeight,
                toolbar.paddingEnd,
                toolbar.paddingBottom
            )
        }
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnContinueRegister = findViewById(R.id.btnContinueRegister)
        helpBtn = findViewById(R.id.helpBtn)
        tvCalculationExample = findViewById(R.id.tvCalculationExample)
    }

    private fun setupCalculationText() {
        val calculationHtml = "On a <b><font color=\"#0F172A\">₹1,000 session</font></b>, Jiifit fee is ₹215, and your direct expert take-home is <b><font color=\"#059669\">₹785</font></b> credited directly into your bank account."
        tvCalculationExample.text = Html.fromHtml(calculationHtml, Html.FROM_HTML_MODE_COMPACT)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        btnContinueRegister.setOnClickListener {
            val intent = Intent(this@ExpertEarningSlabsActivity, Register::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        helpBtn.setOnClickListener {
            ExpertHelpBottomSheet.show(this@ExpertEarningSlabsActivity)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}

