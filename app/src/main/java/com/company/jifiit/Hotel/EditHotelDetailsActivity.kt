package com.company.jifiit.Hotel

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.company.jifiit.Constant
import com.company.jifiit.R
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.Slider
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class EditHotelDetailsActivity : AppCompatActivity() {

    // Top Bar Views
    private lateinit var ivBack: ImageView
    private lateinit var btnCancel: Button
    private lateinit var btnSaveChanges: Button

    // Card 1: Hotel Information
    private lateinit var etHotelName: EditText
    private lateinit var etLegalBusinessName: EditText
    private lateinit var sliderCategory: Slider
    private lateinit var tvSelectedCategory: TextView
    private lateinit var tvStar1: TextView
    private lateinit var tvStar2: TextView
    private lateinit var tvStar3: TextView
    private lateinit var tvStar4: TextView
    private lateinit var tvStar5: TextView
    private lateinit var rgPropertyType: RadioGroup
    private lateinit var rbHotel: RadioButton
    private lateinit var rbBoutique: RadioButton
    private lateinit var rbResort: RadioButton
    private lateinit var rbServiceApartment: RadioButton

    // Card 2: Property & Room Details
    private lateinit var etYearEstab: EditText
    private lateinit var etTotalRooms: EditText
    private lateinit var cgRoomTypes: ChipGroup
    private lateinit var etNewRoomType: EditText
    private lateinit var btnAddRoomType: Button

    // Card 3: Property Location
    private lateinit var layoutSelectCity: LinearLayout
    private lateinit var tvCity: TextView
    private lateinit var layoutSelectLocality: LinearLayout
    private lateinit var tvLocality: TextView
    private lateinit var etFullAddress: EditText

    // Card 4: Legal & Tax Details
    private lateinit var etGstPan: EditText

    // Internal State
    private val roomTypesList = mutableListOf<String>()
    private var selectedCityId: String = ""
    private var selectedLocalityId: String = ""

    private val cachedCities = mutableListOf<Pair<String, String>>() // id to name
    private val cachedLocalities = mutableListOf<Pair<String, String>>() // id to name

    private val defaultCities = listOf("Gurgaon", "Delhi", "Noida", "Bengaluru", "Mumbai", "Hyderabad", "Pune", "Kolkata", "Chennai", "Jaipur")
    private val defaultLocalities = listOf("Sector 14", "DLF Phase 2", "Cyber City", "Golf Course Road", "Sohna Road", "MG Road", "Sushant Lok", "Sector 29", "Udyog Vihar")

    private lateinit var spDetails: SharedPreferences
    private lateinit var spCred: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_hotel_details)

        spDetails = getSharedPreferences("hotel_details", Context.MODE_PRIVATE)
        spCred = getSharedPreferences("credentials", Context.MODE_PRIVATE)

        initViews()
        setupListeners()
        loadSavedDetails()
    }

    private fun initViews() {
        ivBack = findViewById(R.id.ivBack)
        btnCancel = findViewById(R.id.btnCancel)
        btnSaveChanges = findViewById(R.id.btnSaveChanges)

        etHotelName = findViewById(R.id.etHotelName)
        etLegalBusinessName = findViewById(R.id.etLegalBusinessName)
        sliderCategory = findViewById(R.id.sliderCategory)
        tvSelectedCategory = findViewById(R.id.tvSelectedCategory)
        tvStar1 = findViewById(R.id.tvStar1)
        tvStar2 = findViewById(R.id.tvStar2)
        tvStar3 = findViewById(R.id.tvStar3)
        tvStar4 = findViewById(R.id.tvStar4)
        tvStar5 = findViewById(R.id.tvStar5)

        rgPropertyType = findViewById(R.id.rgPropertyType)
        rbHotel = findViewById(R.id.rbHotel)
        rbBoutique = findViewById(R.id.rbBoutique)
        rbResort = findViewById(R.id.rbResort)
        rbServiceApartment = findViewById(R.id.rbServiceApartment)

        etYearEstab = findViewById(R.id.etYearEstab)
        etTotalRooms = findViewById(R.id.etTotalRooms)
        cgRoomTypes = findViewById(R.id.cgRoomTypes)
        etNewRoomType = findViewById(R.id.etNewRoomType)
        btnAddRoomType = findViewById(R.id.btnAddRoomType)

        layoutSelectCity = findViewById(R.id.layoutSelectCity)
        tvCity = findViewById(R.id.tvCity)
        layoutSelectLocality = findViewById(R.id.layoutSelectLocality)
        tvLocality = findViewById(R.id.tvLocality)
        etFullAddress = findViewById(R.id.etFullAddress)

        etGstPan = findViewById(R.id.etGstPan)
    }

    private fun setupListeners() {
        ivBack.setOnClickListener {
            finish()
        }

        btnCancel.setOnClickListener {
            finish()
        }

        btnSaveChanges.setOnClickListener {
            saveDetails()
        }

        // Category Slider listener
        sliderCategory.addOnChangeListener { _, value, _ ->
            updateCategoryUI(value.toInt())
        }

        // Star labels click to move slider
        val starViews = listOf(tvStar1 to 1, tvStar2 to 2, tvStar3 to 3, tvStar4 to 4, tvStar5 to 5)
        starViews.forEach { (view, star) ->
            view.setOnClickListener {
                sliderCategory.value = star.toFloat()
                updateCategoryUI(star)
            }
        }

        // Add Room Type Button
        btnAddRoomType.setOnClickListener {
            val newType = etNewRoomType.text.toString().trim()
            if (newType.isEmpty()) {
                Toast.makeText(this, "Please enter a room type", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (roomTypesList.contains(newType)) {
                Toast.makeText(this, "Room type already added", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            roomTypesList.add(newType)
            etNewRoomType.text.clear()
            renderRoomTypeChips()
        }

        // City Selector Click
        layoutSelectCity.setOnClickListener {
            showCitySelectorDialog()
        }

        // Locality Selector Click
        layoutSelectLocality.setOnClickListener {
            showLocalitySelectorDialog()
        }
    }

    private fun updateCategoryUI(stars: Int) {
        val starText = if (stars == 1) "1 Star" else "$stars Stars"
        tvSelectedCategory.text = "Selected: ★ $starText Hotel"

        val activeBlue = Color.parseColor("#0080FF")
        val inactiveGray = Color.parseColor("#6B7280")

        val starViews = listOf(tvStar1 to 1, tvStar2 to 2, tvStar3 to 3, tvStar4 to 4, tvStar5 to 5)
        starViews.forEach { (view, num) ->
            if (num == stars) {
                view.setTextColor(activeBlue)
                view.setTypeface(null, Typeface.BOLD)
            } else {
                view.setTextColor(inactiveGray)
                view.setTypeface(null, Typeface.NORMAL)
            }
        }
    }

    private fun renderRoomTypeChips() {
        cgRoomTypes.removeAllViews()
        val inflater = LayoutInflater.from(this)

        for (room in roomTypesList) {
            val chipView = inflater.inflate(R.layout.item_room_type_chip, cgRoomTypes, false)
            val tvTitle = chipView.findViewById<TextView>(R.id.tvChipTitle)
            val ivClose = chipView.findViewById<ImageView>(R.id.ivChipClose)

            tvTitle.text = room
            ivClose.setOnClickListener {
                roomTypesList.remove(room)
                renderRoomTypeChips()
            }

            cgRoomTypes.addView(chipView)
        }
    }

    private fun loadSavedDetails() {
        // Hotel Name: credentials takes precedence if present, else default
        val savedCredName = spCred.getString("Name", null)
        val defaultHotelName = if (!savedCredName.isNullOrBlank() && savedCredName != "Jzkxmx" && savedCredName != "Default Name") {
            savedCredName
        } else {
            "Grand Orchid Hotel"
        }

        val hotelName = spDetails.getString("hotel_name", defaultHotelName) ?: defaultHotelName
        val legalName = spDetails.getString("legal_business_name", "Grand Orchid Hospitality Pvt Ltd") ?: "Grand Orchid Hospitality Pvt Ltd"
        val categoryStars = spDetails.getInt("hotel_category", 4)
        val propertyType = spDetails.getString("property_type", "Hotel") ?: "Hotel"
        val yearEstab = spDetails.getString("establishment_year", "2018") ?: "2018"
        val totalRooms = spDetails.getString("total_rooms", "50") ?: "50"
        val city = spDetails.getString("city", "Gurgaon") ?: "Gurgaon"
        selectedCityId = spDetails.getString("city_id", "") ?: ""
        val locality = spDetails.getString("locality", "Sector 14") ?: "Sector 14"
        selectedLocalityId = spDetails.getString("locality_id", "") ?: ""
        val address = spDetails.getString("address", "Plot 42, DLF Phase 2, Near MG Road,\nGurgaon, Haryana - 122002") ?: "Plot 42, DLF Phase 2, Near MG Road,\nGurgaon, Haryana - 122002"
        val gstPan = spDetails.getString("gst_pan", "06AAACG1234F1Z8") ?: "06AAACG1234F1Z8"

        // Set edit texts
        etHotelName.setText(hotelName)
        etLegalBusinessName.setText(legalName)
        etYearEstab.setText(yearEstab)
        etTotalRooms.setText(totalRooms)
        tvCity.text = city
        tvLocality.text = locality
        etFullAddress.setText(address)
        etGstPan.setText(gstPan)

        // Set Category Slider
        sliderCategory.value = categoryStars.coerceIn(1, 5).toFloat()
        updateCategoryUI(categoryStars.coerceIn(1, 5))

        // Set Property Type
        when (propertyType.lowercase()) {
            "boutique" -> rbBoutique.isChecked = true
            "resort" -> rbResort.isChecked = true
            "service apartment" -> rbServiceApartment.isChecked = true
            else -> rbHotel.isChecked = true
        }

        // Room Types
        val savedRoomsStr = spDetails.getString("room_types", null)
        roomTypesList.clear()
        if (!savedRoomsStr.isNullOrEmpty()) {
            savedRoomsStr.split(",").forEach {
                val trimmed = it.trim()
                if (trimmed.isNotEmpty()) roomTypesList.add(trimmed)
            }
        } else {
            // Default room types as shown in screenshot
            roomTypesList.addAll(listOf("Deluxe Room", "Executive Suite", "Standard AC"))
        }
        renderRoomTypeChips()
    }

    private fun getSelectedPropertyType(): String {
        return when (rgPropertyType.checkedRadioButtonId) {
            R.id.rbBoutique -> "Boutique"
            R.id.rbResort -> "Resort"
            R.id.rbServiceApartment -> "Service Apartment"
            else -> "Hotel"
        }
    }

    private fun showCitySelectorDialog() {
        if (cachedCities.isNotEmpty()) {
            showCityList(cachedCities.map { it.second }.toTypedArray(), cachedCities.map { it.first }.toTypedArray())
            return
        }

        val url = Constant.get_cities
        val request = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    val status = response.optBoolean("status", false)
                    val citiesArray = response.optJSONArray("cities")
                    if (status && citiesArray != null && citiesArray.length() > 0) {
                        cachedCities.clear()
                        val names = mutableListOf<String>()
                        val ids = mutableListOf<String>()
                        for (i in 0 until citiesArray.length()) {
                            val obj = citiesArray.getJSONObject(i)
                            val id = obj.optString("id")
                            val name = obj.optString("name")
                            cachedCities.add(id to name)
                            names.add(name)
                            ids.add(id)
                        }
                        showCityList(names.toTypedArray(), ids.toTypedArray())
                    } else {
                        showFallbackCities()
                    }
                } catch (e: Exception) {
                    showFallbackCities()
                }
            },
            {
                showFallbackCities()
            }
        )
        request.retryPolicy = DefaultRetryPolicy(4000, 1, 1.0f)
        Volley.newRequestQueue(this).add(request)
    }

    private fun showFallbackCities() {
        val names = defaultCities.toTypedArray()
        val ids = Array(names.size) { "" }
        showCityList(names, ids)
    }

    private fun showCityList(names: Array<String>, ids: Array<String>) {
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.select_dialog_item, names) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(Color.parseColor("#111827"))
                view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                view.setPadding(40, 24, 40, 24)
                return view
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Select City")
            .setAdapter(adapter) { _, which ->
                tvCity.text = names[which]
                if (which < ids.size) selectedCityId = ids[which]
                // Reset locality cache when city changes
                cachedLocalities.clear()
            }
            .show()
    }

    private fun showLocalitySelectorDialog() {
        if (cachedLocalities.isNotEmpty()) {
            showLocalityList(cachedLocalities.map { it.second }.toTypedArray(), cachedLocalities.map { it.first }.toTypedArray())
            return
        }

        if (selectedCityId.isNotEmpty()) {
            val url = "${Constant.get_localities}?city_id=${Uri.encode(selectedCityId)}"
            val request = JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                { response ->
                    try {
                        val status = response.optBoolean("status", false)
                        val dataArray = response.optJSONArray("data")
                        if (status && dataArray != null && dataArray.length() > 0) {
                            cachedLocalities.clear()
                            val names = mutableListOf<String>()
                            val ids = mutableListOf<String>()
                            for (i in 0 until dataArray.length()) {
                                val obj = dataArray.getJSONObject(i)
                                val id = obj.optString("id")
                                val name = obj.optString("name")
                                cachedLocalities.add(id to name)
                                names.add(name)
                                ids.add(id)
                            }
                            showLocalityList(names.toTypedArray(), ids.toTypedArray())
                        } else {
                            showFallbackLocalities()
                        }
                    } catch (e: Exception) {
                        showFallbackLocalities()
                    }
                },
                {
                    showFallbackLocalities()
                }
            )
            request.retryPolicy = DefaultRetryPolicy(4000, 1, 1.0f)
            Volley.newRequestQueue(this).add(request)
        } else {
            showFallbackLocalities()
        }
    }

    private fun showFallbackLocalities() {
        val names = defaultLocalities.toTypedArray()
        val ids = Array(names.size) { "" }
        showLocalityList(names, ids)
    }

    private fun showLocalityList(names: Array<String>, ids: Array<String>) {
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.select_dialog_item, names) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(Color.parseColor("#111827"))
                view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                view.setPadding(40, 24, 40, 24)
                return view
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Select Locality / Area")
            .setAdapter(adapter) { _, which ->
                tvLocality.text = names[which]
                if (which < ids.size) selectedLocalityId = ids[which]
            }
            .show()
    }

    private fun saveDetails() {
        val hotelName = etHotelName.text.toString().trim()
        val legalName = etLegalBusinessName.text.toString().trim()
        val yearEstab = etYearEstab.text.toString().trim()
        val totalRooms = etTotalRooms.text.toString().trim()
        val city = tvCity.text.toString().trim()
        val locality = tvLocality.text.toString().trim()
        val address = etFullAddress.text.toString().trim()
        val gstPan = etGstPan.text.toString().trim()
        val categoryStars = sliderCategory.value.toInt()
        val propertyType = getSelectedPropertyType()

        // Validation
        if (hotelName.isEmpty()) {
            etHotelName.error = "Hotel name is required"
            etHotelName.requestFocus()
            return
        }
        if (legalName.isEmpty()) {
            etLegalBusinessName.error = "Legal business name is required"
            etLegalBusinessName.requestFocus()
            return
        }
        if (yearEstab.isEmpty()) {
            etYearEstab.error = "Establishment year is required"
            etYearEstab.requestFocus()
            return
        }
        if (totalRooms.isEmpty()) {
            etTotalRooms.error = "Total rooms is required"
            etTotalRooms.requestFocus()
            return
        }
        if (address.isEmpty()) {
            etFullAddress.error = "Address is required"
            etFullAddress.requestFocus()
            return
        }

        // Save into hotel_details SharedPreferences
        spDetails.edit().apply {
            putString("hotel_name", hotelName)
            putString("legal_business_name", legalName)
            putInt("hotel_category", categoryStars)
            putString("property_type", propertyType)
            putString("establishment_year", yearEstab)
            putString("total_rooms", totalRooms)
            putString("room_types", roomTypesList.joinToString(","))
            putString("city", city)
            putString("city_id", selectedCityId)
            putString("locality", locality)
            putString("locality_id", selectedLocalityId)
            putString("address", address)
            putString("gst_pan", gstPan)
            apply()
        }

        // Also update the global credentials "Name" so HotelMainActivity and Drawer update immediately
        spCred.edit().apply {
            putString("Name", hotelName)
            apply()
        }

        // Sync with currently selected hotel branch
        HotelBranchManager.getInstance(this).syncHotelDetailsToSelectedBranch()

        Toast.makeText(this, "Hotel details updated successfully", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
        finish()
    }
}
