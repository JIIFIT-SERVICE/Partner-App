package com.company.jifiit.Hotel

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.company.jifiit.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class AddBookingActivity : AppCompatActivity() {

    // Colors
    private val colorSelectedBg = 0xFFE8F0FE.toInt()
    private val colorSelectedStroke = 0xFF1A56DB.toInt()
    private val colorSelectedText = 0xFF1A56DB.toInt()
    private val colorDefaultBg = 0xFFFFFFFF.toInt()
    private val colorDefaultStroke = 0xFFD0D7DE.toInt()
    private val colorDefaultText = 0xFF1A1A1A.toInt()
    private val colorDefaultSubText = 0xFF757575.toInt()

    private val colorBtnDisabled = 0xFFB0B7C3.toInt()
    private val colorBtnEnabled = 0xFF1A56DB.toInt()

    private data class ServiceCard(
        val card: MaterialCardView,
        val title: TextView,
        val subtitle: TextView,
        val buttonLabel: String,
        val serviceName: String,
        val icon: ImageView,
        val IconBackground: FrameLayout
    )

    private lateinit var cards: List<ServiceCard>
    private lateinit var btnProceed: MaterialButton
    private lateinit var btnBack: ImageView
    // Holds the selected service name ("Yoga at Hotel", "Massage at Hotel", etc.)
    private var selectedService: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true   // black/dark icons on status bar
            isAppearanceLightNavigationBars = true // black/dark icons on nav bar
        }
        setContentView(R.layout.activity_add_booking)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnProceed = findViewById(R.id.btnProceed)
        btnBack = findViewById(R.id.btnBack)


        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            overridePendingTransition(
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            // অথবা সরাসরি: finish()
        }
        cards = listOf(
            ServiceCard(
                findViewById(R.id.cardYoga),
                findViewById(R.id.tvYogaTitle),
                findViewById(R.id.tvYogaSub),
                "CONTINUE",
                "Yoga at Hotel",
                findViewById(R.id.yogaIcon),
                findViewById(R.id.yogaImgBack)
            ),
            ServiceCard(
                findViewById(R.id.cardMassage),
                findViewById(R.id.tvMassageTitle),
                findViewById(R.id.tvMassageSub),
                "CONTINUE",
                "Massage at Hotel",
                findViewById(R.id.msgIcon),
                findViewById(R.id.msgImgBack)
            ),
            ServiceCard(
                findViewById(R.id.cardAyurveda),
                findViewById(R.id.tvAyurvedaTitle),
                findViewById(R.id.tvAyurvedaSub),
                "CONTINUE",
                "Ayurveda at Hotel",
                findViewById(R.id.ayuIcon),
                findViewById(R.id.ayuImgBack)
            ),
            ServiceCard(
                findViewById(R.id.cardTrainer),
                findViewById(R.id.tvTrainerTitle),
                findViewById(R.id.tvTrainerSub),
                "CONTINUE",
                "Gym at Hotel",
                findViewById(R.id.gymIcon),
                findViewById(R.id.gymImgBack)
            )
        )

        // Initially all unselected + button disabled
        cards.forEach { applyUnselected(it) }
        setButtonEnabled(false)

        // Click listeners
        cards.forEach { service ->
            service.card.setOnClickListener {
                cards.forEach { applyUnselected(it) }
                applySelected(service)

                // Store the selected service name
                selectedService = service.serviceName

                setButtonEnabled(true, service.buttonLabel)
            }
        }

        btnProceed.setOnClickListener {
            val service = selectedService ?: return@setOnClickListener

//            Toast.makeText(this, "Selected Service: $service", Toast.LENGTH_SHORT).show()
            // Pass to next page
            val intent = Intent(this,HotelSelectServices::class.java)
            intent.putExtra("SELECTED_SERVICE", service)
            startActivity(intent)
            overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
        }
    }

    private fun applySelected(service: ServiceCard) {
        service.card.setCardBackgroundColor(colorSelectedBg)
        service.card.setStrokeColor(colorSelectedStroke)
        service.card.strokeWidth = dp(2)
        service.title.setTextColor(colorSelectedText)
        service.subtitle.setTextColor(colorSelectedText)
        service.icon.setColorFilter(colorSelectedText, PorterDuff.Mode.SRC_IN)
        service.IconBackground.setBackgroundResource(R.drawable.bg_icon_mint)
    }

    private fun applyUnselected(service: ServiceCard) {
        service.card.setCardBackgroundColor(colorDefaultBg)
        service.card.setStrokeColor(colorDefaultStroke)
        service.card.strokeWidth = dp(1)
        service.title.setTextColor(colorDefaultText)
        service.subtitle.setTextColor(colorDefaultSubText)
        service.icon.setColorFilter(colorDefaultText, PorterDuff.Mode.SRC_IN)
        service.IconBackground.setBackgroundResource(R.drawable.bg_icon_gray)
    }

    private fun setButtonEnabled(enabled: Boolean, label: String = "Select a service to continue") {
        btnProceed.isEnabled = enabled
        btnProceed.text = label
        btnProceed.backgroundTintList =
            android.content.res.ColorStateList.valueOf(
                if (enabled) colorBtnEnabled else colorBtnDisabled
            )
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
            super.onBackPressed()
        overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
        }

}