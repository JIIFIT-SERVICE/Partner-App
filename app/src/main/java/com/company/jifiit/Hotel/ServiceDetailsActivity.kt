package com.company.jifiit.Hotel

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.company.jifiit.Constant
import com.company.jifiit.LocalStorage.HotelDatabaseHelper
import com.company.jifiit.Hotel.Model.ServiceDetailsModel
import com.company.jifiit.Hotel.Model.ServiceDurationModel
import com.company.jifiit.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import org.json.JSONObject
import java.util.Locale
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.company.jifiit.Hotel.Adapter.AddOnAdapter
import com.company.jifiit.Hotel.Model.AddOnModel
import com.facebook.shimmer.ShimmerFrameLayout

class ServiceDetailsActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var tvHeaderTitle: TextView
    private lateinit var tvServiceTitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var ivHero: ImageView
    private lateinit var llDurationOptions: LinearLayout
    private lateinit var tvTotal: TextView
    private lateinit var addOnCard: CardView
    private lateinit var detailCard: CardView
    private lateinit var btnCustomize: MaterialButton
    private lateinit var shimmerLayout: ShimmerFrameLayout
    private var tvTotalOriginal: TextView? = null

    private var serviceDetails: ServiceDetailsModel? = null
    private lateinit var hotelDatabaseHelper: HotelDatabaseHelper

    // Selected items map: optionId -> quantity
    private val selectedItems = LinkedHashMap<String, Int>()

    // Store all options for total calculation
    private val allOptions = mutableListOf<ServiceDurationModel>()
    private lateinit var rvAddOns: RecyclerView
    private lateinit var addOnAdapter: AddOnAdapter
    private val addOnList = mutableListOf<AddOnModel>()
    private var serviceId = ""
    private var serviceType = ""
    private lateinit var service: String



    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        setContentView(R.layout.activity_service_details)

        hotelDatabaseHelper = HotelDatabaseHelper(this)

        llDurationOptions = findViewById(R.id.llDurationOptions)
        rvAddOns = findViewById(R.id.rvAddOns)

        rvAddOns.layoutManager =
            LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        rvAddOns.isNestedScrollingEnabled = false

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnBack = findViewById(R.id.btnBack)
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle)
        tvServiceTitle = findViewById(R.id.tvServiceTitle)
        tvDescription = findViewById(R.id.tvDescription)
        ivHero = findViewById(R.id.ivHero)
        tvTotal = findViewById(R.id.tvTotal)
        addOnCard = findViewById(R.id.add_on_card)
        detailCard = findViewById(R.id.details_card)
        btnCustomize = findViewById(R.id.btnCustomize)
        shimmerLayout = findViewById(R.id.shimmerLayout)

        addOnCard.visibility = View.GONE
        detailCard.visibility = View.VISIBLE

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
//        updateCartButton()


        serviceId = intent.getStringExtra("SERVICE_ID") ?: ""
        service = intent.getStringExtra("SELECTED_SERVICE") ?: ""
        serviceType = intent.getStringExtra("SERVICE_TYPE") ?: ""
        val serviceTitle = intent.getStringExtra("SERVICE_TITLE") ?: ""

        if (serviceTitle.isNotEmpty()) {
            tvHeaderTitle.text = serviceTitle
            tvServiceTitle.text = serviceTitle
        }

        if (serviceId.isEmpty()) {
            Toast.makeText(this, "Service ID not found", Toast.LENGTH_SHORT).show()
            return
        }


        btnCustomize.setOnClickListener {

            if (hotelDatabaseHelper.getCartItemCount() > 0) {
                val intent = Intent(this, YourCartActivity::class.java).apply {
                    putExtra("SELECTED_SERVICE", service)
                }
                startActivity(intent)
                overridePendingTransition(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
            }
        }


    }

    private fun updateCartButton() {

        val cartCount = hotelDatabaseHelper.getCartItemCount()

        if (cartCount > 0) {

            // Enable button
            btnCustomize.isEnabled = true
            btnCustomize.alpha = 1f

            // Show count
            btnCustomize.text = "Cart ($cartCount) ›"

            // Normal button color
            btnCustomize.backgroundTintList =
                ContextCompat.getColorStateList(
                    this,
                    R.color.Button_Blue
                )

            btnCustomize.setTextColor(
                ContextCompat.getColor(this, android.R.color.white)
            )

        } else {

            // Disable button
            btnCustomize.isEnabled = false
            btnCustomize.alpha = 1f

            // Empty cart
            btnCustomize.text = "Cart ›"

            // Gray button
            btnCustomize.backgroundTintList =
                ContextCompat.getColorStateList(
                    this,
                    R.color.lightGray
                )

            btnCustomize.setTextColor(
                ContextCompat.getColor(this, R.color.Gray)
            )
        }
    }


    // =========================================================
    // API
    // =========================================================

    private fun fetchServiceDetails(serviceId: String, serviceType: String) {

        val url = Constant.SERVICE_DETAILS_API +
                "?service_id=${serviceId}" +
                "&service_type=${serviceType}"

        val request = StringRequest(
            Request.Method.GET,
            url,

            { response ->
                try {
                    val jsonObject = JSONObject(response)
                    val success = jsonObject.optBoolean("success", false)

                    if (!success) {
                        Toast.makeText(
                            this,
                            jsonObject.optString("message", "Unable to load service"),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@StringRequest
                    }

                    val rootServiceId = jsonObject.optString("service_id", serviceId)
                    val rootServiceType = jsonObject.optString("service_type", serviceType)
                    val serviceName = jsonObject.optString("service_name", "")

                    val dataArray = jsonObject.optJSONArray("data")
                    if (dataArray == null || dataArray.length() == 0) {
                        Toast.makeText(this, "No service options found", Toast.LENGTH_SHORT).show()
                        return@StringRequest
                    }

                    val firstItem = dataArray.getJSONObject(0)
                    val type = firstItem.optString("types", rootServiceType)
                    val name = firstItem.optString("name", serviceName)
                    val description = firstItem.optString("features", "")
                    val image = firstItem.optString("image", "")
                    val cover = firstItem.optString("cover", "")
                    val technique = firstItem.optString("technique", "")
                    val pressure = firstItem.optString("pressure", "")
                    val oil = firstItem.optString("oil", "")
                    val benefits = firstItem.optString("benefits", "")
                    val recommendedFor = firstItem.optString("recommended_for", "")

                    val options = ArrayList<ServiceDurationModel>()

                    for (i in 0 until dataArray.length()) {
                        val item = dataArray.getJSONObject(i)
                        options.add(
                            ServiceDurationModel(
                                id = item.optString("id", ""),
                                name = item.optString("name", name),
                                duration = item.optString("duration", ""),
                                originalPrice = item.optDouble("price", 0.0),
                                discountPrice = item.optDouble("discount_price", 0.0),
                                discountPercent = item.optInt("percentage_discount", 0),
                                image = item.optString("image", ""),
                                type = item.optString("types", type)
                            )
                        )
                    }

                    serviceDetails = ServiceDetailsModel(
                        serviceId = rootServiceId,
                        serviceType = type,
                        name = name,
                        description = description,
                        image = image,
                        cover = cover,
                        technique = technique,
                        pressure = pressure,
                        oil = oil,
                        benefits = benefits,
                        recommendedFor = recommendedFor,
                        options = options
                    )

                    updateServiceUI(serviceDetails!!)

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Invalid server response", Toast.LENGTH_SHORT).show()
                }
            },

            { error ->
                error.printStackTrace()
                Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }


    // =========================================================
    // UPDATE UI
    // =========================================================

    private fun updateServiceUI(data: ServiceDetailsModel) {

        tvHeaderTitle.text = data.name
        tvServiceTitle.text = data.name

        if (data.description.isNotEmpty()) {
            tvDescription.text = formatDescription(data.description)
            tvDescription.visibility = View.VISIBLE
        } else {
            tvDescription.visibility = View.GONE
        }
        // =====================================================
// HERO IMAGE + SHIMMER
// =====================================================

        shimmerLayout.visibility = View.VISIBLE
        shimmerLayout.startShimmer()

        ivHero.visibility = View.VISIBLE

        if (data.image.isNotEmpty()) {

            Glide.with(this)
                .load(data.image)
                .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {

                    override fun onLoadFailed(
                        e: com.bumptech.glide.load.engine.GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {

                        // Image load failed → stop shimmer
                        shimmerLayout.stopShimmer()
                        shimmerLayout.visibility = View.GONE

                        return false
                    }

                    override fun onResourceReady(
                        resource: android.graphics.drawable.Drawable,
                        model: Any,
                        target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                        dataSource: com.bumptech.glide.load.DataSource,
                        isFirstResource: Boolean
                    ): Boolean {

                        // Image loaded successfully → stop shimmer
                        shimmerLayout.stopShimmer()
                        shimmerLayout.visibility = View.GONE

                        return false
                    }
                })
                .into(ivHero)

        } else {

            // No image URL → don't keep shimmer running
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = View.GONE

            ivHero.setImageResource(R.mipmap.hotel_body_massage)
        }

        // Save all options for total calculation
        allOptions.clear()
        allOptions.addAll(data.options)

        createDurationOptions(data.options)

        updateDetailSection(data)

        // Initial total (no selection)
        updateTotalDisplay()
    }


    private fun formatDescription(raw: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(raw, Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(raw)
        }
    }


    private fun updateDetailSection(data: ServiceDetailsModel) {
        findViewById<TextView>(R.id.tvCover).text = data.cover
        findViewById<TextView>(R.id.tvTechnique).text = data.technique
        findViewById<TextView>(R.id.tvPressure).text = data.pressure
        findViewById<TextView>(R.id.tvOil).text = data.oil
        findViewById<TextView>(R.id.tvBenefits).text = data.benefits
        findViewById<TextView>(R.id.tvRecommendedFor).text = data.recommendedFor
    }


    // =========================================================
    // DYNAMIC DURATION OPTIONS
    // =========================================================



    private fun createDurationOptions(options: List<ServiceDurationModel>) {

        llDurationOptions.removeAllViews()
        selectedItems.clear()

        if (options.isEmpty()) {
            Toast.makeText(this, "No duration available", Toast.LENGTH_SHORT).show()
            return
        }

        options.forEach { option ->

            val card = MaterialCardView(this)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.bottomMargin = dpToPx(10)
            card.layoutParams = params
            card.radius = dpToPx(12).toFloat()
            card.cardElevation = 0f

            // ---------- MAIN ROW ----------
            val mainLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dpToPx(14), dpToPx(14), dpToPx(14), dpToPx(14))
            }

            // ---------- LEFT: duration + discount ----------
            val leftLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                )
            }

            val durationText = TextView(this).apply {
                text = "${option.duration} minutes"
                setTextColor(ContextCompat.getColor(this@ServiceDetailsActivity, R.color.black))
                textSize = 15f
                setTypeface(null, Typeface.BOLD)
            }
            leftLayout.addView(durationText)

            if (option.discountPercent > 0) {
                val discountText = TextView(this).apply {
                    text = "${option.discountPercent}% OFF"
                    setTextColor(ContextCompat.getColor(this@ServiceDetailsActivity, R.color.Text_Blue))
                    textSize = 12f
                    setPadding(0, dpToPx(3), 0, 0)
                }
                leftLayout.addView(discountText)
            }

            mainLayout.addView(leftLayout)

            // ---------- MIDDLE: price ----------
            val priceLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.END
            }

            val discountPriceText = TextView(this).apply {
                text = "₹${formatPrice(option.discountPrice)}"
                setTextColor(ContextCompat.getColor(this@ServiceDetailsActivity, R.color.black))
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
            }
            priceLayout.addView(discountPriceText)

            val originalPriceText = TextView(this).apply {
                text = "₹${formatPrice(option.originalPrice)}"
                setTextColor(ContextCompat.getColor(this@ServiceDetailsActivity, R.color.Gray))
                textSize = 12f
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
            priceLayout.addView(originalPriceText)

            mainLayout.addView(priceLayout)

            // ---------- RIGHT: Add / Quantity ----------
            val actionContainer = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginStart = dpToPx(12) }
            }

            val btnAdd = MaterialButton(this).apply {
                text = "Add"
                textSize = 14f
                isAllCaps = false
                minWidth = dpToPx(70)
                setPadding(dpToPx(18), 0, dpToPx(18), 0)
                backgroundTintList =
                    ContextCompat.getColorStateList(this@ServiceDetailsActivity, R.color.BackgroundBlue)
                setTextColor(ContextCompat.getColor(this@ServiceDetailsActivity, R.color.Text_Blue))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, dpToPx(38)
                )
            }

            val llQuantity = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                visibility = View.GONE
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, dpToPx(38)
                )
            }

            val btnMinus = TextView(this).apply {
                text = "−"
                textSize = 20f
                gravity = Gravity.CENTER
                setTextColor(ContextCompat.getColor(this@ServiceDetailsActivity, R.color.Text_Blue))
                setTypeface(null, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(34), LinearLayout.LayoutParams.MATCH_PARENT
                )
            }

            val tvQuantity = TextView(this).apply {
                text = "1"
                textSize = 14f
                gravity = Gravity.CENTER
                setTextColor(ContextCompat.getColor(this@ServiceDetailsActivity, R.color.black))
                setTypeface(null, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(28), LinearLayout.LayoutParams.MATCH_PARENT
                )
            }

            val btnPlus = TextView(this).apply {
                text = "+"
                textSize = 20f
                gravity = Gravity.CENTER
                setTextColor(ContextCompat.getColor(this@ServiceDetailsActivity, R.color.Text_Blue))
                setTypeface(null, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(34), LinearLayout.LayoutParams.MATCH_PARENT
                )
            }

            llQuantity.addView(btnMinus)
            llQuantity.addView(tvQuantity)
            llQuantity.addView(btnPlus)

            actionContainer.addView(btnAdd)
            actionContainer.addView(llQuantity)

            mainLayout.addView(actionContainer)
            card.addView(mainLayout)

            // =====================================================
            // LOCAL STATE
            // =====================================================
            var quantity = 0
            var isSelected = false

            fun applyUI() {
                if (isSelected && quantity > 0) {
                    card.strokeWidth = dpToPx(2)
                    card.strokeColor = ContextCompat.getColor(this, R.color.Text_Blue)
                    card.setCardBackgroundColor(
                        ContextCompat.getColor(this, R.color.BackgroundBlue)
                    )
                    btnAdd.visibility = View.GONE
                    llQuantity.visibility = View.VISIBLE
                    tvQuantity.text = quantity.toString()
                } else {
                    card.strokeWidth = dpToPx(1)
                    card.strokeColor = ContextCompat.getColor(this, R.color.lightGray)
                    card.setCardBackgroundColor(
                        ContextCompat.getColor(this, android.R.color.white)
                    )
                    btnAdd.visibility = View.VISIBLE
                    llQuantity.visibility = View.GONE
                }
            }

            // =====================================================
            // RESTORE FROM DB — using option.type
            // =====================================================
            val storedQuantity = hotelDatabaseHelper.getServiceQuantity(
                serviceId = option.id,
                serviceType = option.type        // ✅ FIX
            )

            if (storedQuantity > 0) {
                isSelected = true
                quantity = storedQuantity
                selectedItems[option.id] = quantity
            }

            // =====================================================
            // CLICK HANDLERS
            // =====================================================

            // Add → insert with option.type
            btnAdd.setOnClickListener {
                isSelected = true
                quantity = 1
                selectedItems[option.id] = quantity
                addOnCard.visibility = View.VISIBLE
                detailCard.visibility = View.GONE


                val existing = hotelDatabaseHelper.getServiceQuantity(option.id, option.type)
                if (existing > 0) {
                    hotelDatabaseHelper.updateServiceQuantity(
                        productId = option.id,
                        serviceType = option.type,       // ✅ FIX
                        quantity = quantity
                    )
                } else {
                    hotelDatabaseHelper.insertService(
                        productId = option.id,
                        productName = option.name,
                        originalPrice = option.originalPrice,
                        discountPrice = option.discountPrice,
                        quantity = quantity,
                        duration = option.duration,
                        type = option.type,              // ✅ matches delete
                        discount = option.discountPercent.toDouble()
                    )


                }

                applyUI()
                updateTotalDisplay()
                updateCartButton()
            }

            // Plus
            btnPlus.setOnClickListener {
                if (isSelected) {
                    quantity++
                    selectedItems[option.id] = quantity

                    hotelDatabaseHelper.updateServiceQuantity(
                        productId = option.id,
                        serviceType = option.type,       // ✅ FIX
                        quantity = quantity
                    )

                    applyUI()
                    updateTotalDisplay()
                    updateCartButton()
                }
            }

            // Minus → delete when 0, using option.type
            btnMinus.setOnClickListener {
                if (isSelected) {
                    quantity--
                    if (quantity <= 0) {
                        quantity = 0
                        isSelected = false
                        selectedItems.remove(option.id)

                        // ✅ FIX: previously used "massage" — now uses option.type
                        val deleted = hotelDatabaseHelper.deleteService(
                            productId = option.id,
                            serviceType = option.type
                        )

                        // Fallback: if type mismatch ever occurs, force-delete by product_id
                        if (!deleted) {
                            hotelDatabaseHelper.deleteServiceByProductId(option.id)
                        }
                    } else {
                        selectedItems[option.id] = quantity

                        hotelDatabaseHelper.updateServiceQuantity(
                            productId = option.id,
                            serviceType = option.type,   // ✅ FIX
                            quantity = quantity
                        )
                    }

                    applyUI()
                    updateTotalDisplay()
                    updateCartButton()
                }
            }

            // Default state
            applyUI()
            llDurationOptions.addView(card)
        }
    }


    // =========================================================
    // TOTAL CALCULATION
    // =========================================================

    private fun updateTotalDisplay() {

        val totalDiscount =
            hotelDatabaseHelper.getTotalDiscountPrice()


        // Discount total
        tvTotal.text =
            "₹${formatPrice(totalDiscount)}"
    }


    private fun formatPrice(price: Double): String {
        return if (price % 1.0 == 0.0) {
            String.format(Locale.US, "%.0f", price)
        } else {
            String.format(Locale.US, "%.2f", price)
        }
    }

    private fun fetchAddOn() {

        val url = Constant.ADD_ON_API

        val request = StringRequest(
            Request.Method.GET,
            url,

            { response ->
                try {
                    android.util.Log.d("ADDON_RAW", response)

                    val jsonObject = JSONObject(response)

                    // =====================================================
                    // 1. Find the items array — support both API shapes
                    // =====================================================

                    var dataArray: org.json.JSONArray? = null

                    // Shape A: { "success": true, "data": [ ... ] }
                    val successA = jsonObject.optBoolean("success", false)
                    if (successA) {
                        dataArray = jsonObject.optJSONArray("data")
                    }

                    // Shape B: { "status": true, "sections": [ { "items": [ ... ] } ] }
                    if (dataArray == null) {
                        val statusB = jsonObject.optBoolean("status", false)
                        if (statusB) {
                            val sections = jsonObject.optJSONArray("sections")
                            if (sections != null && sections.length() > 0) {
                                val firstSection = sections.optJSONObject(0)
                                dataArray = firstSection?.optJSONArray("items")
                            }
                        }
                    }

                    if (dataArray == null || dataArray.length() == 0) {
                        rvAddOns.visibility = View.GONE
                        return@StringRequest
                    }

                    // =====================================================
                    // 2. Parse items (works for both shapes)
                    // =====================================================

                    addOnList.clear()

                    for (i in 0 until dataArray.length()) {

                        val item = dataArray.getJSONObject(i)

                        val id = item.optString("id", "")

                        // Server may send "title" OR "name"
                        val title = item.optString(
                            "title",
                            item.optString("name", "")
                        )

                        // Server may send "duration" OR "duration_minutes"
                        val duration = item.optString(
                            "duration",
                            item.optString("duration_minutes", "")
                        )

                        // Server already sends formatted strings — use them directly
                        val price = item.optString("price", "")                     // "₹343"
                        val originalPrice = item.optString("originalPrice", "")     // "₹429"
                        val discountPercent = item.optString("discountPercent", "") // "20%"
                        val saveBadge = item.optString("saveBadge", "")             // "Save ₹86"

                        val type = item.optString(
                            "type",
                            item.optString("types", "")
                        )

                        val image = item.optString("image", "")

                        // optionsCount: try both key variants
                        val optionsCount = item.optInt(
                            "optionsCount",
                            item.optInt("options_count", 1)
                        )

                        // meta: build from duration + saveBadge
                        val meta = when {
                            duration.isNotEmpty() && saveBadge.isNotEmpty() ->
                                "$duration mins · $saveBadge"
                            duration.isNotEmpty() ->
                                "$duration mins"
                            saveBadge.isNotEmpty() ->
                                saveBadge
                            else -> ""
                        }

                        addOnList.add(
                            AddOnModel(
                                id = id,
                                title = title,
                                meta = meta,
                                price = price,                 // ✅ use server string
                                originalPrice = originalPrice, // ✅ use server string
                                discountPercent = discountPercent,
                                saveBadge = saveBadge,
                                optionsCount = optionsCount,
                                duration = duration,
                                type = type,
                                image = image
                            )
                        )
                    }

                    // =====================================================
                    // 3. Update RecyclerView
                    // =====================================================

                    if (addOnList.isEmpty()) {
                        rvAddOns.visibility = View.GONE
                    } else {
                        rvAddOns.visibility = View.VISIBLE

                        val selectedService =
                            intent.getStringExtra("SELECTED_SERVICE") ?: ""

                        addOnAdapter = AddOnAdapter(
                            context = this,
                            items = addOnList,
                            service = selectedService,
                            onAddClick = { /* handled in adapter */ }
                        )

                        rvAddOns.adapter = addOnAdapter
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    android.util.Log.e("ADDON_ERR", "parse failed", e)
                    rvAddOns.visibility = View.GONE
                }
            },

            { error ->
                error.printStackTrace()
                rvAddOns.visibility = View.GONE
            }
        )

        Volley.newRequestQueue(this).add(request)
    }


    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }




    override fun onResume() {
        super.onResume()
        fetchServiceDetails(serviceId, serviceType)
        fetchAddOn()
        updateCartButton()
        updateTotalDisplay()
    }



    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}