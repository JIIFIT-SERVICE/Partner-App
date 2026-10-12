package com.company.jifiit.Hotel // Change to your actual package name

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.company.jifiit.Constant
import com.company.jifiit.LoginActivity
import com.company.jifiit.R
import com.google.android.material.chip.Chip
import com.google.android.material.navigation.NavigationView
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.math.BigInteger
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowCompat

class HotelMainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    // ============================================================
    // ✅ GLOBAL VIEW VARIABLES
    // ============================================================
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    private lateinit var btnMenu: ImageView
    private lateinit var ivCalendar: ImageView

    private lateinit var btnSignOut: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var bookingsNotFound: View
    private lateinit var btnAddDeskBooking: com.google.android.material.button.MaterialButton
    private lateinit var tvEmptyTitle: TextView
    private lateinit var tvEmptySubtitle: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var tvYesterday: TextView
    private lateinit var tvToday: TextView
    private lateinit var tvTomorrow: TextView
    private lateinit var btnAddBooking: LinearLayout

    // ✅ Filter chips (service-type)
    private lateinit var chipAll: Chip
    private lateinit var chipYoga: Chip
    private lateinit var chipInRoom: Chip
    private lateinit var chipAyurveda: Chip
    private lateinit var chipPersonal: Chip

    private lateinit var bookingAdapter: BookingAdapter

    // ============================================================
    // ✅ GLOBAL DATA VARIABLES
    // ============================================================
    private lateinit var Id: BigInteger
    private var name: String = ""
    private var age: String = ""

    private var appUpdateManager: AppUpdateManager? = null

    private var selectedDate: String = "today"
    private var customSelectedDate: Date? = null

    // Full list from API + filtered list shown to adapter
    private val allBookings = mutableListOf<BookingModel>()
    private val bookingList = mutableListOf<BookingModel>()

    private var activeFilter: ServiceFilter = ServiceFilter.All

    companion object {
        private const val PERMISSION_REQUEST_CODE = 101
        private const val UPDATE_REQUEST_CODE = 100
    }

    private lateinit var sp: SharedPreferences
    private var currentSelectedButton: String = "today"
    private var selectedCalendarDate: String = ""

    /** Filter by service type (massage_types) */
    enum class ServiceFilter { All, Yoga, InRoom, Ayurveda, Personal }

    // ============================================================
    // ✅ onCreate
    // ============================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        setContentView(R.layout.activity_hotel_main)

        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.WHITE

        val root = findViewById<DrawerLayout>(R.id.drawer_layout)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, 0, bars.right, bars.bottom)
            insets
        }

        val appBar = findViewById<com.google.android.material.appbar.AppBarLayout>(R.id.appBarLayout)
        ViewCompat.setOnApplyWindowInsetsListener(appBar) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, bars.top, 0, 0)
            insets
        }

        val spCred = getSharedPreferences("credentials", MODE_PRIVATE)
        name = spCred.getString("Name", "Default Name") ?: "Default Name"
        age  = spCred.getString("Age", "Hii") ?: "Hii"

        val idString = spCred.getString("Id", "0") ?: "0"
        Id = try {
            BigInteger(idString)
        } catch (e: NumberFormatException) {
            BigInteger.ZERO
        }

        sp = getSharedPreferences("app_state", MODE_PRIVATE)

        bindViews()
        setupDrawer()
        setupRecyclerView()
        setupDatePills()
        setupCalendarIcon()
        setupFilterChips()
        styleBottomMenuItems()
    }

    // ============================================================
    // ✅ Bind all views
    // ============================================================
    private fun bindViews() {
        drawerLayout     = findViewById(R.id.drawer_layout)
        navView          = findViewById(R.id.nav_view)
        btnMenu          = findViewById(R.id.btnMenu)
        btnSignOut       = findViewById(R.id.btnPartnerSignOut)
        recyclerView     = findViewById(R.id.todolist)
        progressBar      = findViewById(R.id.progressBar)
        btnAddBooking    = findViewById(R.id.btnAddBooking)

        bookingsNotFound  = findViewById(R.id.Bookings_notFound)
        btnAddDeskBooking = bookingsNotFound.findViewById(R.id.btnAddDeskBooking)
        tvEmptyTitle      = bookingsNotFound.findViewById(R.id.tvEmptyTitle)
        tvEmptySubtitle   = bookingsNotFound.findViewById(R.id.tvEmptySubtitle)

        tvYesterday = findViewById(R.id.tvYesterday)
        tvToday     = findViewById(R.id.tvToday)
        tvTomorrow  = findViewById(R.id.tvTomorrow)

        ivCalendar  = findViewById(R.id.ivCalendar)

        // ✅ Filter chips
        chipAll       = findViewById(R.id.chip_all)
        chipYoga      = findViewById(R.id.chip_yoga)
        chipInRoom    = findViewById(R.id.chip_inroom)
        chipAyurveda  = findViewById(R.id.chip_ayurveda)
        chipPersonal  = findViewById(R.id.chip_personal)

        btnAddBooking.setOnClickListener {
            val intent = Intent(this, AddBookingActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        btnAddDeskBooking.setOnClickListener {
            val intent = Intent(this, AddBookingActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    // ============================================================
    // ✅ Drawer setup
    // ============================================================
    private fun setupDrawer() {
        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        btnSignOut.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            showLogoutDialog()
        }
        navView.setNavigationItemSelectedListener(this)
    }

    // ============================================================
    // ✅ RecyclerView setup
    // ============================================================
    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        bookingAdapter = BookingAdapter(bookingList) { booking ->
            Toast.makeText(this, "Clicked Booking: ${booking.bookingId}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = bookingAdapter
        recyclerView.visibility = View.VISIBLE
    }

    // ============================================================
    // ✅ Setup filter chips (single-select, checkable)
    // ============================================================
    private fun setupFilterChips() {
        chipAll.setOnClickListener       { onChipClicked(ServiceFilter.All) }
        chipYoga.setOnClickListener      { onChipClicked(ServiceFilter.Yoga) }
        chipInRoom.setOnClickListener    { onChipClicked(ServiceFilter.InRoom) }
        chipAyurveda.setOnClickListener  { onChipClicked(ServiceFilter.Ayurveda) }
        chipPersonal.setOnClickListener  { onChipClicked(ServiceFilter.Personal) }

        // Start with "All" selected
        chipAll.isChecked = true
    }

    /** Called when any chip is tapped. Only one chip stays checked. */
    private fun onChipClicked(filter: ServiceFilter) {
        Log.d("ChipClick", "Chip clicked: $filter")

        // Uncheck all
        chipAll.isChecked       = false
        chipYoga.isChecked      = false
        chipInRoom.isChecked    = false
        chipAyurveda.isChecked  = false
        chipPersonal.isChecked  = false

        // Check the tapped one
        when (filter) {
            ServiceFilter.All      -> chipAll.isChecked      = true
            ServiceFilter.Yoga     -> chipYoga.isChecked     = true
            ServiceFilter.InRoom   -> chipInRoom.isChecked   = true
            ServiceFilter.Ayurveda -> chipAyurveda.isChecked = true
            ServiceFilter.Personal -> chipPersonal.isChecked = true
        }

        applyFilter(filter)
    }

    // ============================================================
    // ✅ Apply filter and refresh adapter
    // ============================================================
    private fun applyFilter(filter: ServiceFilter) {
        activeFilter = filter
        bookingList.clear()

        when (filter) {
            ServiceFilter.All -> bookingList.addAll(allBookings)

            ServiceFilter.Yoga -> bookingList.addAll(allBookings.filter {
                it.massage_types.trim().equals("Yoga at Hotel", ignoreCase = true)
            })

            ServiceFilter.InRoom -> bookingList.addAll(allBookings.filter {
                it.massage_types.trim().equals("Massage at Hotel", ignoreCase = true)
            })

            ServiceFilter.Ayurveda -> bookingList.addAll(allBookings.filter {
                it.massage_types.trim().equals("Ayurveda at Hotel", ignoreCase = true)
            })

            ServiceFilter.Personal -> bookingList.addAll(allBookings.filter {
                it.massage_types.trim().equals("Personal Trainer at Hotel", ignoreCase = true)
            })
        }

        bookingAdapter.notifyDataSetChanged()
        updateChipCounts()

        if (bookingList.isEmpty()) {
            showEmptyState()
        } else {
            recyclerView.visibility = View.VISIBLE
            bookingsNotFound.visibility = View.GONE
        }
    }

    // ============================================================
    // ✅ Update chip labels with real counts
    // ============================================================
    private fun updateChipCounts() {
        val all       = allBookings.size
        val yoga      = allBookings.count { it.massage_types.trim().equals("Yoga at Hotel", true) }
        val inRoom    = allBookings.count { it.massage_types.trim().equals("Massage at Hotel", true) }
        val ayurveda  = allBookings.count { it.massage_types.trim().equals("Ayurveda at Hotel", true) }
        val personal  = allBookings.count { it.massage_types.trim().equals("Personal Trainer at Hotel", true) }

        chipAll.text       = "All $all"
        chipYoga.text      = "Yoga $yoga"
        chipInRoom.text    = "In-Room Massage $inRoom"
        chipAyurveda.text  = "Ayurveda $ayurveda"
        chipPersonal.text  = "Personal Trainer $personal"
    }

    // ============================================================
    // ✅ Setup date pills
    // ============================================================
    private fun setupDatePills() {
        val dateFormat = SimpleDateFormat("EEE • dd MMM", Locale.getDefault())
        val calendar = Calendar.getInstance()

        calendar.add(Calendar.DAY_OF_YEAR, -1)
        tvYesterday.text = "YESTERDAY\n${dateFormat.format(calendar.time)}"

        calendar.add(Calendar.DAY_OF_YEAR, 1)
        tvToday.text = "TODAY\n${dateFormat.format(calendar.time)}"

        calendar.add(Calendar.DAY_OF_YEAR, 1)
        tvTomorrow.text = "TOMORROW\n${dateFormat.format(calendar.time)}"

        tvYesterday.setOnClickListener {
            selectDatePill("yesterday")
            fetchBookings(Id, "yesterday")
        }
        tvToday.setOnClickListener {
            selectDatePill("today")
            fetchBookings(Id, "today")
        }
        tvTomorrow.setOnClickListener {
            selectDatePill("tomorrow")
            fetchBookings(Id, "tomorrow")
        }

        selectDatePill("today")
    }

    private fun selectDatePill(which: String) {
        selectedDate = which

        sp.edit().putString("selected_button", which).apply()
        if (which != "custom") {
            sp.edit().putString("selected_calendar_date", "").apply()
            selectedCalendarDate = ""
        }

        val tealColor  = Color.parseColor("#0B4F4F")
        val whiteColor = ContextCompat.getColor(this, android.R.color.white)

        listOf(tvYesterday, tvToday, tvTomorrow).forEach { pill ->
            pill.setBackgroundResource(0)
            pill.setTextColor(tealColor)
        }

        val activePill = when (which) {
            "yesterday" -> tvYesterday
            "tomorrow"  -> tvTomorrow
            else        -> tvToday
        }
        activePill.setBackgroundResource(R.drawable.bg_active_date_pill)
        activePill.setTextColor(whiteColor)

        resetCalendarIcon()
    }

    private fun resetCalendarIcon() {
        ivCalendar.setBackgroundResource(0)
        ivCalendar.setColorFilter(Color.parseColor("#0B4F4F"))
    }

    private fun activateCalendarIcon() {
        ivCalendar.setBackgroundResource(R.drawable.bg_active_calendar_icon)
        ivCalendar.setColorFilter(Color.WHITE)
    }

    private fun deselectAllPills() {
        val tealColor = Color.parseColor("#0B4F4F")
        listOf(tvYesterday, tvToday, tvTomorrow).forEach { pill ->
            pill.setBackgroundResource(0)
            pill.setTextColor(tealColor)
        }
    }

    private fun setupCalendarIcon() {
        ivCalendar.setOnClickListener { showDateRangePicker() }
    }

    private fun showDateRangePicker() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val minDate = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 30)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val picked = Calendar.getInstance()
                picked.set(year, month, dayOfMonth)
                onDatePickedFromCalendar(picked)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = minDate
        datePickerDialog.show()
    }

    private fun onDatePickedFromCalendar(picked: Calendar) {
        val today = Calendar.getInstance()

        val pickedMidnight = (picked.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
        }
        val todayMidnight = (today.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
        }

        val diffDays = ((pickedMidnight.timeInMillis - todayMidnight.timeInMillis) /
                (1000L * 60 * 60 * 24)).toInt()

        when (diffDays) {
            -1 -> { selectDatePill("yesterday"); fetchBookings(Id, "yesterday") }
            0  -> { selectDatePill("today");     fetchBookings(Id, "today") }
            1  -> { selectDatePill("tomorrow");  fetchBookings(Id, "tomorrow") }
            else -> {
                selectedDate = "custom"
                customSelectedDate = pickedMidnight.time

                deselectAllPills()
                activateCalendarIcon()

                val apiFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val selectedDateStr = apiFmt.format(pickedMidnight.time)

                selectedCalendarDate = selectedDateStr
                sp.edit().putString("selected_button", "calender").apply()
                sp.edit().putString("selected_calendar_date", selectedDateStr).apply()

                val uiFmt = SimpleDateFormat("EEE • dd MMM", Locale.getDefault())
                Toast.makeText(this, "Selected: ${uiFmt.format(pickedMidnight.time)}", Toast.LENGTH_SHORT).show()

                fetchBookings(Id, "specific_date", selectedDateStr)
            }
        }
    }

    private fun styleBottomMenuItems() {
        val grayColor = ContextCompat.getColor(this, R.color.Gray)

        navView.menu.findItem(R.id.nav_help)?.let {
            val title = SpannableString(it.title)
            title.setSpan(ForegroundColorSpan(grayColor), 0, title.length, 0)
            it.title = title
        }
        navView.menu.findItem(R.id.nav_terms)?.let {
            val title = SpannableString(it.title)
            title.setSpan(ForegroundColorSpan(grayColor), 0, title.length, 0)
            it.title = title
        }
    }

    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to log out?")
            .setPositiveButton("Confirm") { _, _ -> logOutUser() }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE)
            dialog.findViewById<TextView>(android.R.id.message)?.setTextColor(Color.BLACK)
        }
        dialog.show()
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // ============================================================
    // ✅ Fetch bookings
    // ============================================================
    private fun fetchBookings(id: BigInteger, listType: String, date: String? = null) {
        if (!isNetworkAvailable()) {
            progressBar.visibility = View.GONE
            recyclerView.visibility = View.GONE
            bookingsNotFound.visibility = View.VISIBLE
            tvEmptyTitle.text = "No Internet Connection"
            tvEmptySubtitle.text = "Please check your internet connection and try again."
            return
        }

        val timeZone = TimeZone.getDefault().id
        val url: String = when (listType) {
            "today"         -> "${Constant.fetchHotelBookings}$id&selectdate=today&timezone=$timeZone"
            "tomorrow"      -> "${Constant.fetchHotelBookings}$id&selectdate=tomorrow&timezone=$timeZone"
            "yesterday"     -> "${Constant.fetchHotelBookings}$id&selectdate=yesterday&timezone=$timeZone"
            "specific_date" -> "${Constant.fetchHotelBookings}$id&selectdate=$date&timezone=$timeZone"
            else -> {
                Toast.makeText(this, "Invalid list type selected.", Toast.LENGTH_SHORT).show()
                return
            }
        }
        Log.d("FetchBookings", "URL: $url")

        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        bookingsNotFound.visibility = View.GONE

        val requestQueue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                progressBar.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                try {
                    val status = response.getString("Status")
                    if (status.equals("success", ignoreCase = true)) {
                        bookingsNotFound.visibility = View.GONE
                        val dataArray: JSONArray? = response.optJSONArray("booking")

                        allBookings.clear()
                        if (dataArray != null && dataArray.length() > 0) {
                            for (i in 0 until dataArray.length()) {
                                val bookingObject = dataArray.getJSONObject(i)

                                val customerName  = bookingObject.optString("customer_name", "N/A")
                                val startTime     = bookingObject.optString("start_time", "N/A")
                                val address       = bookingObject.optString("landmark", "N/A")
                                val paymentStatus = bookingObject.optString("payment_status", "N/A")
                                val bookingId     = bookingObject.optString("id", "N/A")
                                val bookingDate   = bookingObject.optString("booking_date", "")
                                val inRoom        = bookingObject.optBoolean("in_room", false)
                                val room_num      = bookingObject.optString("room_number", "")
                                val massage_types = bookingObject.optString("massage_types", "")

                                Log.d("FetchBookings", "massage_types='$massage_types'")

                                allBookings.add(
                                    BookingModel(
                                        bookingId    = bookingId,
                                        isInRoom     = inRoom,
                                        status       = paymentStatus,
                                        customerName = customerName,
                                        roomInfo     = address,
                                        date         = bookingDate,
                                        time         = startTime,
                                        room_number  = room_num,
                                        massage_types = massage_types
                                    )
                                )
                            }
                        }

                        // Reset chip selection to "All" on every new fetch
                        chipAll.isChecked       = true
                        chipYoga.isChecked      = false
                        chipInRoom.isChecked    = false
                        chipAyurveda.isChecked  = false
                        chipPersonal.isChecked  = false

                        activeFilter = ServiceFilter.All
                        applyFilter(ServiceFilter.All)

                    } else {
                        allBookings.clear()
                        chipAll.isChecked = true
                        activeFilter = ServiceFilter.All
                        applyFilter(ServiceFilter.All)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                progressBar.visibility = View.GONE
                bookingsNotFound.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                Log.e("VolleyError", "Error: ${error.message}")
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    private fun showEmptyState() {
        recyclerView.visibility = View.GONE
        bookingsNotFound.visibility = View.VISIBLE
        tvEmptyTitle.text = "No appointments for this selection"
        tvEmptySubtitle.text = "Switch date tab or create a new desk booking using the button below."
    }

    // ============================================================
    // ✅ Logout
    // ============================================================
    private fun logOutUser() {
        val sp: SharedPreferences = getSharedPreferences("credentials", Context.MODE_PRIVATE)
        sp.edit().clear().apply()

        loggedOut(Id)

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun loggedOut(id: BigInteger) {
        val url = Constant.HotelLoggedout

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener<String> { response ->
                Log.d("LogoutResponse", "Response: $response")
                try {
                    val jsonObject = JSONObject(response)
                    val status = jsonObject.getString("status")
                    if (status == "success") {
                        // Success
                    }
                } catch (e: JSONException) {
                    Log.e("JSONError", "Error parsing response", e)
                }
            },
            Response.ErrorListener { error: VolleyError ->
                Log.e("VolleyError", "Error logging out", error)
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id"] = id.toString()
                return params
            }
        }

        Volley.newRequestQueue(this).add(stringRequest)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_overview -> Toast.makeText(this, "Hotel Overview Clicked", Toast.LENGTH_SHORT).show()
            R.id.nav_rooms    -> Toast.makeText(this, "Manage Rooms Clicked", Toast.LENGTH_SHORT).show()
            R.id.nav_history  -> { /* Navigate to history */ }
            R.id.nav_earnings -> { /* Navigate to earnings */ }
            R.id.nav_help     -> { /* Navigate to help */ }
            R.id.nav_terms    -> { /* Navigate to terms */ }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun checkRequiredPermissions() {
        val permissions = ArrayList<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.CAMERA)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.READ_PHONE_STATE)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.RECORD_AUDIO)

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            Log.d("PERMISSION", if (allGranted) "All permissions granted" else "Some permissions denied")
        }
    }

    private fun checkForUpdate() {
        val updateManager = AppUpdateManagerFactory.create(this)
        appUpdateManager = updateManager

        updateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                try {
                    updateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.FLEXIBLE,
                        this,
                        UPDATE_REQUEST_CODE
                    )
                } catch (e: IntentSender.SendIntentException) {
                    Log.e("APP_UPDATE", "Error starting update flow", e)
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_REQUEST_CODE && resultCode != RESULT_OK) {
            Log.d("APP_UPDATE", "Update flow failed! Result code: $resultCode")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        checkForUpdate()
        checkRequiredPermissions()

        currentSelectedButton = sp.getString("selected_button", "today") ?: "today"
        selectedCalendarDate  = sp.getString("selected_calendar_date", "") ?: ""

        when (currentSelectedButton) {
            "today"     -> tvToday.performClick()
            "tomorrow"  -> tvTomorrow.performClick()
            "yesterday" -> tvYesterday.performClick()
            "calender"  -> {
                if (selectedCalendarDate.isNotEmpty()) {
                    fetchBookings(Id, "specific_date", selectedCalendarDate)
                    deselectAllPills()
                    activateCalendarIcon()
                } else {
                    tvToday.performClick()
                }
            }
            else -> tvToday.performClick()
        }
    }
}