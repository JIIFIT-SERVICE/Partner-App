package com.company.jifiit.Hotel

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.company.jifiit.Hotel.Adapter.ServiceSectionAdapter
import com.company.jifiit.Hotel.Model.ServiceItemModel
import com.company.jifiit.Hotel.Model.ServiceSectionModel
import com.company.jifiit.R

import android.annotation.SuppressLint
import android.content.Intent
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.company.jifiit.Constant
import com.facebook.shimmer.ShimmerFrameLayout

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import com.company.jifiit.LocalStorage.DatabaseHelper
import com.company.jifiit.LocalStorage.HotelDatabaseHelper

class HotelSelectServices : AppCompatActivity() {
    private lateinit var rvSections: RecyclerView
    private lateinit var sectionAdapter: ServiceSectionAdapter
    private val sectionList = mutableListOf<ServiceSectionModel>()

    private lateinit var btnBack: ImageView
   private lateinit var btnCart: FrameLayout
   private lateinit var service: String
   private lateinit var PageName: TextView
   private lateinit var tvCartBadge: TextView
    private lateinit var dbHelper: HotelDatabaseHelper
    // ⚠️ Replace with your actual API URL
    private val API_URL = Constant.SERVICE_API

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    private lateinit var shimmerSkeleton: ShimmerFrameLayout
    private lateinit var tvNoData: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        setContentView(R.layout.activity_hotel_select_services)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        dbHelper =
            HotelDatabaseHelper(this)

         btnCart = findViewById(R.id.btnCart)
         tvCartBadge = findViewById(R.id.tvCartBadge)
        btnCart.setOnClickListener {
            val intent = Intent(this, YourCartActivity::class.java).apply {
                putExtra("SELECTED_SERVICE", service)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
         makeDraggable(btnCart)

        shimmerSkeleton = findViewById(R.id.shimmerSkeleton)
        tvNoData = findViewById(R.id.tvNoData)
        PageName = findViewById(R.id.PageName)

        service = intent.getStringExtra("SELECTED_SERVICE").toString()

        rvSections = findViewById(R.id.rvSections)
        rvSections.layoutManager = LinearLayoutManager(this)

        // ✅ Adapter setup — empty list দিয়ে
        sectionAdapter = ServiceSectionAdapter(sectionList) { item ->
            val intent = Intent(this, ServiceDetailsActivity::class.java).apply {
                putExtra("SERVICE_ID", item.id)
                putExtra("SERVICE_TYPE", item.type)          // dry / oil / package / add-on
                putExtra("SERVICE_TITLE", item.title)        // optional, useful for toolbar
                putExtra("SELECTED_SERVICE", service)        // optional context (Massage at Hotel, etc.)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        rvSections.adapter = sectionAdapter

        // ✅ API call — dummy data replace


        btnBack = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            clearHotelCartAndExit()
        }
    }

    /**
     * Simple API call — OkHttp + manual JSON parse.
     * No Retrofit, no Gson, no separate API layer.
     */
    private fun fetchServicesSimple() {
        lifecycleScope.launch {
            try {
                shimmerSkeleton.visibility = View.VISIBLE
                shimmerSkeleton.startShimmer()
                rvSections.visibility = View.GONE
                tvNoData.visibility = View.GONE

                Log.d("API", "1. API call starting: $API_URL")

                val jsonText = withContext(Dispatchers.IO) {
                    val request = Request.Builder().url(API_URL).get().build()
                    httpClient.newCall(request).execute().use { response ->
                        Log.d("API", "2. HTTP code: ${response.code}")
                        if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
                        response.body?.string() ?: throw Exception("Empty response")
                    }
                }

                Log.d("API", "3. Response length: ${jsonText.length}")
                Log.d("API", "4. Response body: $jsonText")   // ← পুরো JSON দেখো

                shimmerSkeleton.stopShimmer()
                shimmerSkeleton.visibility = View.GONE

                val root = JSONObject(jsonText)
                val status = root.optBoolean("status", false)

                Log.d("API", "5. status = $status")

                if (!status) {
                    tvNoData.visibility = View.VISIBLE
                    tvNoData.text = "Failed: ${root.optString("message", "Unknown error")}"
                    return@launch
                }

                val sectionsArray = root.optJSONArray("sections")
                Log.d("API", "6. sections count = ${sectionsArray?.length() ?: 0}")

                if (sectionsArray == null || sectionsArray.length() == 0) {
                    tvNoData.visibility = View.VISIBLE
                    tvNoData.text = "No services available"
                    return@launch
                }

                sectionList.clear()
                for (i in 0 until sectionsArray.length()) {
                    val sectionObj = sectionsArray.getJSONObject(i)
                    val itemsArray = sectionObj.optJSONArray("items")

                    Log.d("API", "7. Section [$i]: ${sectionObj.optString("title")}, items = ${itemsArray?.length() ?: 0}")

                    val items = mutableListOf<ServiceItemModel>()
                    if (itemsArray != null) {
                        for (j in 0 until itemsArray.length()) {
                            val itemObj = itemsArray.getJSONObject(j)
                            items.add(
                                ServiceItemModel(
                                    id = itemObj.optString("id", ""),
                                    title = itemObj.optString("title", ""),
                                    image = itemObj.optString("image", ""),
                                    saveBadge = itemObj.optString("saveBadge", ""),
                                    optionsCount = itemObj.optInt("optionsCount", 1),
                                    price = itemObj.optString("price", ""),
                                    originalPrice = itemObj.optString("originalPrice", ""),
                                    discountPercent = itemObj.optString("discountPercent", ""),
                                    duration = itemObj.optString("duration", ""),
                                    type = itemObj.optString("type", "")
                                )
                            )
                        }
                    }

                    sectionList.add(
                        ServiceSectionModel(
                            title = sectionObj.optString("title", ""),
                            subtitle = sectionObj.optString("subtitle", ""),
                            items = items
                        )
                    )
                }

                Log.d("API", "8. sectionList size = ${sectionList.size}")

                rvSections.visibility = View.VISIBLE
                sectionAdapter.notifyDataSetChanged()

                Log.d("API", "9. UI updated")

            } catch (e: Exception) {
                Log.e("API", "ERROR", e)
                shimmerSkeleton.stopShimmer()
                shimmerSkeleton.visibility = View.GONE
                rvSections.visibility = View.GONE
                tvNoData.visibility = View.VISIBLE
                tvNoData.text = "Network error: ${e.localizedMessage}"
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun makeDraggable(view: View) {
        var dX = 0f
        var dY = 0f
        var isDragging = false
        var downRawX = 0f
        var downRawY = 0f

        val touchSlop = android.view.ViewConfiguration.get(this).scaledTouchSlop

        view.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                    downRawX = event.rawX
                    downRawY = event.rawY
                    isDragging = false
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (!isDragging) {
                        val dx = kotlin.math.abs(event.rawX - downRawX)
                        val dy = kotlin.math.abs(event.rawY - downRawY)
                        if (dx > touchSlop || dy > touchSlop) {
                            isDragging = true
                        }
                    }

                    if (isDragging) {
                        val newX = event.rawX + dX
                        val newY = event.rawY + dY

                        val parent = v.parent as View
                        val maxX = (parent.width - v.width).toFloat()
                        val maxY = (parent.height - v.height).toFloat()

                        v.x = newX.coerceIn(0f, maxX)
                        v.y = newY.coerceIn(0f, maxY)
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        v.performClick()
                    }
                    isDragging = false
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    isDragging = false
                    true
                }

                else -> false
            }
        }
    }

    private fun updateCartBadge() {

        val count = dbHelper.getCartItemCount()

        tvCartBadge.text = count.toString()

        if (count > 0) {
            tvCartBadge.visibility = View.VISIBLE
        } else {
            tvCartBadge.visibility = View.GONE
        }
    }



    @Deprecated("Deprecated in Java")
    override fun onResume() {
        super.onResume()
//        updateCartBadge()
        if (service.equals("Massage at Hotel")) {

            fetchServicesSimple();

        } else if (service.equals("Yoga at Hotel")) {

//            fetchYogaServices();
            fetchServicesSimple();


        } else if (service.equals("Ayurveda at Hotel")) {

//            fetchAyurvedaServices();
            fetchServicesSimple();


        } else if (service.equals("Gym at Hotel")) {

//            fetchGymServices();
            fetchServicesSimple();


        }

        updateCartBadge()
    }

    private fun clearHotelCartAndExit() {
        dbHelper.clearCart()

        onBackPressedDispatcher.onBackPressed()

        overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        dbHelper.clearCart()

        super.onBackPressed()

        overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
    }
}