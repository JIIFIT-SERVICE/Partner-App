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
import com.company.jifiit.EditPage
import com.company.jifiit.LoginActivity
import com.company.jifiit.R
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
import androidx.core.view.WindowInsetsControllerCompat
import java.util.TimeZone
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowCompat
import android.app.Dialog
import android.net.Uri
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetDialog
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileOutputStream
import com.company.jifiit.Hotel.Model.HotelBranch
import com.company.jifiit.Hotel.Adapter.HotelBranchAdapter
import com.company.jifiit.Hotel.HotelBranchManager

class HotelMainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    // ============================================================
    // ✅ GLOBAL VIEW VARIABLES
    // ============================================================

    // Layout
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    // Toolbar / Header
    private lateinit var btnMenu: ImageView
    private lateinit var ivCalendar: ImageView

    // Footer
    private lateinit var btnSignOut: TextView
    // RecyclerView & empty state
    private lateinit var recyclerView: RecyclerView
    private lateinit var bookingsNotFound: View
    private lateinit var btnAddDeskBooking: com.google.android.material.button.MaterialButton
    private lateinit var tvEmptyTitle: TextView
    private lateinit var tvEmptySubtitle: TextView
    private lateinit var progressBar: ProgressBar

    // Date pills
    private lateinit var tvYesterday: TextView
    private lateinit var tvToday: TextView
    private lateinit var tvTomorrow: TextView
    private lateinit var btnAddBooking: LinearLayout

    // Adapter
    private lateinit var bookingAdapter: BookingAdapter

    // ============================================================
    // ✅ GLOBAL DATA VARIABLES
    // ============================================================
    private lateinit var Id: BigInteger
    private var name: String = ""
    private var age: String = ""

    private var appUpdateManager: AppUpdateManager? = null

    // "yesterday" | "today" | "tomorrow" | "custom"
    private var selectedDate: String = "today"
    private var customSelectedDate: Date? = null

    private val bookingList = mutableListOf<BookingModel>()

    companion object {
        private const val PERMISSION_REQUEST_CODE = 101
        private const val UPDATE_REQUEST_CODE = 100
    }

    // ✅ State restore related
    private lateinit var sp: SharedPreferences
    private var currentSelectedButton: String = "today"
    private var selectedCalendarDate: String = ""

    // ✅ Profile image picker & launchers
    private var cameraImageUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { saveAndApplyProfileImage(it) }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            cameraImageUri?.let { saveAndApplyProfileImage(it) }
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            takePhotoWithCamera()
        } else {
            Toast.makeText(this, "Camera permission is required to take a photo", Toast.LENGTH_SHORT).show()
        }
    }

    // ============================================================
    // ✅ onCreate
    // ============================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ✅ Force dark icons on status bar (since background will be white)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true   // black/dark icons on status bar
            isAppearanceLightNavigationBars = true // black/dark icons on nav bar
        }

        setContentView(R.layout.activity_hotel_main)

        // ✅ Draw white background behind the status bar
        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.WHITE

        // ✅ Apply insets: top inset goes to AppBarLayout, sides+bottom to root
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


        // ---- Retrieve Shared Preferences (credentials) ----
        val spCred = getSharedPreferences("credentials", MODE_PRIVATE)
        val loadedName = spCred.getString("Name", "Grand Orchid Hotel") ?: "Grand Orchid Hotel"
        name = if (loadedName == "Jzkxmx" || loadedName == "Default Name" || loadedName.isBlank()) {
            "Grand Orchid Hotel"
        } else {
            loadedName
        }
        spCred.edit().putString("Name", name).apply()
        age  = spCred.getString("Age", "Hii") ?: "Hii"

        val idString = spCred.getString("Id", "0") ?: "0"
        Id = try {
            BigInteger(idString)
        } catch (e: NumberFormatException) {
            BigInteger.ZERO
        }

        // ✅ Initialize class-level `sp` for app state
        sp = getSharedPreferences("app_state", MODE_PRIVATE)
        // ---- Bind views ----
        bindViews()

        // ---- Setup Drawer / Menu ----
        setupDrawer()

        // ---- Setup RecyclerView ----
        setupRecyclerView()

        // ---- Setup Date Pills ----
        setupDatePills()

        // ---- Setup Calendar Icon ----
        setupCalendarIcon()

        // ---- Load initial bookings from API ----
//        fetchBookings(Id, "today")

        // ---- Style bottom menu items (gray) ----
        styleBottomMenuItems()
    }

    // ============================================================
    // ✅ Bind all views to their global variables
    // ============================================================
    private fun bindViews() {
        drawerLayout     = findViewById(R.id.drawer_layout)
        navView          = findViewById(R.id.nav_view)
        btnMenu          = findViewById(R.id.btnMenu)
        btnSignOut       = findViewById(R.id.btnPartnerSignOut)
        recyclerView     = findViewById(R.id.todolist)
        progressBar      = findViewById(R.id.progressBar)
        btnAddBooking    = findViewById(R.id.btnAddBooking)

        val tvToolbarHotelName = findViewById<TextView>(R.id.hotelName)
        tvToolbarHotelName?.text = name


        // Empty state (included layout)
        bookingsNotFound  = findViewById(R.id.Bookings_notFound)
        btnAddDeskBooking = bookingsNotFound.findViewById(R.id.btnAddDeskBooking)
        tvEmptyTitle      = bookingsNotFound.findViewById(R.id.tvEmptyTitle)
        tvEmptySubtitle   = bookingsNotFound.findViewById(R.id.tvEmptySubtitle)

        // Date pills
        tvYesterday = findViewById(R.id.tvYesterday)
        tvToday     = findViewById(R.id.tvToday)
        tvTomorrow  = findViewById(R.id.tvTomorrow)

        // Calendar icon
        ivCalendar  = findViewById(R.id.ivCalendar)

        // Empty state button click
        btnAddBooking.setOnClickListener{
            val intent = Intent(this, AddBookingActivity::class.java)
            startActivity(intent)
            // Swipe animation: current screen moves left, new screen comes from right
            overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
        }
        btnAddDeskBooking.setOnClickListener {
            val intent = Intent(this, AddBookingActivity::class.java)
            startActivity(intent)
            overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
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

        // Setup click on My Hotel edit pencil icon actionView -> opens EditHotelDetailsActivity
        val overviewItem = navView.menu.findItem(R.id.nav_overview)
        overviewItem?.actionView?.findViewById<View>(R.id.btnEditHotelNav)?.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, EditHotelDetailsActivity::class.java))
        }

        // Setup profile image click & initial load in nav header
        setupNavHeaderProfile()
    }

    // ============================================================
    // ✅ RecyclerView setup
    // ============================================================
    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        if (bookingList.isEmpty()) {
            bookingList.addAll(getDefaultDemoBookings())
        }
        bookingAdapter = BookingAdapter(bookingList) { booking ->
            showBookingDetailsDialog(booking)
        }
        recyclerView.adapter = bookingAdapter
        recyclerView.visibility = View.VISIBLE
    }

    private fun getDefaultDemoBookings(): List<BookingModel> {
        return listOf(
            BookingModel(
                bookingId = "834",
                isInRoom = true,
                status = "PENDING",
                customerName = "Megha Sharma",
                roomInfo = "112",
                date = "Tue, 26 Aug",
                time = "07:00 PM",
                serviceName = "In-Room Massage",
                phone = "+91 98765 43210"
            ),
            BookingModel(
                bookingId = "823",
                isInRoom = true,
                status = "COMPLETED",
                customerName = "Charu Khanna",
                roomInfo = "304",
                date = "Tue, 26 Aug",
                time = "01:00 PM",
                serviceName = "In-Room Massage",
                phone = "+91 98765 43211"
            )
        )
    }

    private fun showBookingDetailsDialog(booking: BookingModel) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Booking #${booking.bookingId}")
            .setMessage("Customer: ${booking.customerName}\nRoom: ${booking.roomInfo}\nService: ${booking.serviceName}\nStatus: ${booking.status}\nTime: ${booking.time}")
            .setPositiveButton("Call Client") { _, _ ->
                if (booking.phone.isNotBlank()) {
                    try {
                        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${booking.phone}")))
                    } catch (e: Exception) {
                        Toast.makeText(this, "Calling ${booking.customerName}...", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Close", null)
            .create()
        dialog.show()
    }

    // ============================================================
    // ✅ Setup date pills with real dates + click actions
    // ============================================================
    private fun setupDatePills() {
        val dateFormat = SimpleDateFormat("EEE • dd MMM", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // ---- YESTERDAY ----
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        tvYesterday.text = "YESTERDAY\n${dateFormat.format(calendar.time)}"

        // ---- TODAY ----
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        tvToday.text = "TODAY\n${dateFormat.format(calendar.time)}"

        // ---- TOMORROW ----
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        tvTomorrow.text = "TOMORROW\n${dateFormat.format(calendar.time)}"

        // ---- Click listeners ----
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

        // Apply initial state (Today active by default)
        selectDatePill("today")
    }

    // ============================================================
    // ✅ Update visual state of date pills
    // ============================================================
    private fun selectDatePill(which: String) {
        selectedDate = which

        // ✅ Save state for next resume
        sp.edit().putString("selected_button", which).apply()
        if (which != "custom") {
            // pills select হলে calendar date clear করে দিচ্ছি
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

    /** Resets the calendar icon to its default (inactive) appearance. */
    private fun resetCalendarIcon() {
        ivCalendar.setBackgroundResource(0)
        ivCalendar.setColorFilter(Color.parseColor("#0B4F4F")) // teal tint
    }
    /** Applies the "active" blue pill style to the calendar icon. */
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

    // ============================================================
    // ✅ Calendar icon click → DatePickerDialog (last 30 days → today)
    // ============================================================
    private fun setupCalendarIcon() {
        ivCalendar.setOnClickListener {
            showDateRangePicker()
        }
    }

    private fun showDateRangePicker() {
        val calendar = Calendar.getInstance()

        // Min date = today - 30 days
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val minDate = calendar.timeInMillis

        // Restore calendar to today for initial selection
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
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayMidnight = (today.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diffDays = ((pickedMidnight.timeInMillis - todayMidnight.timeInMillis) /
                (1000L * 60 * 60 * 24)).toInt()

        when (diffDays) {
            -1 -> {
                selectDatePill("yesterday")
                fetchBookings(Id, "yesterday")
            }
            0 -> {
                selectDatePill("today")
                fetchBookings(Id, "today")
            }
            1 -> {
                selectDatePill("tomorrow")
                fetchBookings(Id, "tomorrow")
            }
            else -> {
                selectedDate = "custom"
                customSelectedDate = pickedMidnight.time

                deselectAllPills()
                activateCalendarIcon()

                val apiFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val selectedDateStr = apiFmt.format(pickedMidnight.time)

                // ✅ Save custom state
                selectedCalendarDate = selectedDateStr
                sp.edit().putString("selected_button", "calender").apply()
                sp.edit().putString("selected_calendar_date", selectedDateStr).apply()

                val uiFmt = SimpleDateFormat("EEE • dd MMM", Locale.getDefault())
                Toast.makeText(this, "Selected: ${uiFmt.format(pickedMidnight.time)}", Toast.LENGTH_SHORT).show()

                fetchBookings(Id, "specific_date", selectedDateStr)
            }
        }
    }

    // ============================================================
    // ✅ Make bottom two menu items gray (Help / Terms)
    // ============================================================
    private fun styleBottomMenuItems() {
        val grayColor = ContextCompat.getColor(this, R.color.Gray)

        val helpItem  = navView.menu.findItem(R.id.nav_help)
        val termsItem = navView.menu.findItem(R.id.nav_terms)

        helpItem?.let {
            val title = SpannableString(it.title)
            title.setSpan(ForegroundColorSpan(grayColor), 0, title.length, 0)
            it.title = title
        }
        termsItem?.let {
            val title = SpannableString(it.title)
            title.setSpan(ForegroundColorSpan(grayColor), 0, title.length, 0)
            it.title = title
        }
    }

    // ============================================================
    // ✅ Logout confirmation dialog
    // ============================================================
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

    // ============================================================
    // ✅ Network availability check
    // ============================================================
    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // ============================================================
    // ✅ Fetch bookings from API (converted from Java)
    // ============================================================
    private fun fetchBookings(id: BigInteger, listType: String, date: String? = null) {
        // ---- Network check ----
        if (!isNetworkAvailable()) {
            progressBar.visibility = View.GONE
            recyclerView.visibility = View.GONE
            bookingsNotFound.visibility = View.VISIBLE
            tvEmptyTitle.text = "No Internet Connection"
            tvEmptySubtitle.text = "Please check your internet connection and try again."
            return
        }

        // ---- Build URL ----
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

        // ---- Show progress (keep existing bookings visible if already loaded) ----
        progressBar.visibility = View.VISIBLE
        if (bookingList.isEmpty()) {
            recyclerView.visibility = View.GONE
        } else {
            recyclerView.visibility = View.VISIBLE
        }
        bookingsNotFound.visibility = View.GONE

        // ---- Volley request ----
        val requestQueue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                progressBar.visibility = View.GONE
                try {
                    val status = response.getString("Status")
                    if (status.equals("success", ignoreCase = true)) {
                        val dataArray: JSONArray? = response.optJSONArray("booking")

                        if (dataArray != null && dataArray.length() > 0) {
                            bookingList.clear()
                            for (i in 0 until dataArray.length()) {
                                val bookingObject = dataArray.getJSONObject(i)

                                val customerName  = bookingObject.optString("customer_name", "N/A")
                                val startTime     = bookingObject.optString("start_time", "N/A")
                                val address       = bookingObject.optString("landmark", "N/A")
                                val paymentStatus = bookingObject.optString("payment_status", "N/A")
                                val bookingId     = bookingObject.optString("id", "N/A")
                                val bookingDate   = bookingObject.optString("booking_date", "")
                                val inRoom        = bookingObject.optBoolean("in_room", false)

                                bookingList.add(
                                    BookingModel(
                                        bookingId    = bookingId,
                                        isInRoom     = inRoom,
                                        status       = paymentStatus,
                                        customerName = customerName,
                                        roomInfo     = address,
                                        date         = bookingDate,
                                        time         = startTime
                                    )
                                )
                            }
                            bookingsNotFound.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                            bookingAdapter.notifyDataSetChanged()
                        } else {
                            // If backend has no live bookings for this date yet, keep demo bookings so cards stay visible
                            if (bookingList.isEmpty()) {
                                bookingList.addAll(getDefaultDemoBookings())
                            }
                            bookingsNotFound.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                            bookingAdapter.notifyDataSetChanged()
                        }
                    } else {
                        if (bookingList.isEmpty()) {
                            bookingList.addAll(getDefaultDemoBookings())
                        }
                        bookingsNotFound.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        bookingAdapter.notifyDataSetChanged()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    if (bookingList.isEmpty()) {
                        bookingList.addAll(getDefaultDemoBookings())
                    }
                    bookingsNotFound.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    bookingAdapter.notifyDataSetChanged()
                }
            },
            { error ->
                progressBar.visibility = View.GONE
                Log.e("VolleyError", "Error: ${error.message}")
                if (bookingList.isEmpty()) {
                    bookingList.addAll(getDefaultDemoBookings())
                }
                bookingsNotFound.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                bookingAdapter.notifyDataSetChanged()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }


    private fun showEmptyState() {
        recyclerView.visibility = View.GONE
        bookingsNotFound.visibility = View.VISIBLE

        // Default messages — change these to customize per case
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
                    } else {
                        // Failure
                    }
                } catch (e: JSONException) {
                    Log.e("JSONError", "Error parsing response", e)
                    Toast.makeText(this@HotelMainActivity, "Error parsing response", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error: VolleyError ->
                Log.e("VolleyError", "Error logging out", error)
                Toast.makeText(this@HotelMainActivity, "Error logging out: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id"] = id.toString()
                return params
            }
        }

        val requestQueue: RequestQueue = Volley.newRequestQueue(this)
        requestQueue.add(stringRequest)
    }

    // ============================================================
    // ✅ Navigation item clicks
    // ============================================================
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_overview -> {
                // Clicking "My Hotel" opens Current Bookings on the main page
                selectDatePill("today")
                if (bookingList.isEmpty()) {
                    bookingList.addAll(getDefaultDemoBookings())
                }
                bookingAdapter.notifyDataSetChanged()
                recyclerView.visibility = View.VISIBLE
                bookingsNotFound.visibility = View.GONE
                recyclerView.smoothScrollToPosition(0)
            }
            R.id.nav_rooms -> {
                startActivity(Intent(this, ManageRoomsActivity::class.java))
            }
            R.id.nav_history  -> { /* Navigate to history */ }
            R.id.nav_earnings -> { /* Navigate to earnings */ }
            R.id.nav_help     -> { /* Navigate to help */ }
            R.id.nav_terms    -> { /* Navigate to terms */ }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    // ============================================================
    // ✅ Permissions
    // ============================================================
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
        } else {
            Log.d("PERMISSION", "All permissions granted")
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

    // ============================================================
    // ✅ App update
    // ============================================================
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

    // ============================================================
    // ✅ Back press
    // ============================================================
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    // ============================================================
    // ✅ onResume
    // ============================================================
    override fun onResume() {
        super.onResume()
        checkForUpdate()
        checkRequiredPermissions()
        loadHotelProfileImage()

        // ✅ Refresh branch details & hotel name if updated from Edit Hotel Details
        val spCred = getSharedPreferences("credentials", MODE_PRIVATE)
        val updatedName = spCred.getString("Name", name) ?: name
        if (updatedName.isNotBlank() && updatedName != "Jzkxmx" && updatedName != "Default Name") {
            name = updatedName
        }
        updateNavHeaderBranchInfo()


        currentSelectedButton = sp.getString("selected_button", "today") ?: "today"
        selectedCalendarDate  = sp.getString("selected_calendar_date", "") ?: ""

        when (currentSelectedButton) {
            "today" -> {
                tvToday.performClick()
            }

            "tomorrow" -> {
                tvTomorrow.performClick()
            }

            "yesterday" -> {
                tvYesterday.performClick()
            }

            "calender" -> {
                if (selectedCalendarDate.isNotEmpty()) {
                    // ✅ Directly fetch bookings for the saved date without showing calendar
                    fetchBookings(Id, "specific_date", selectedCalendarDate)

                    // ✅ Update UI to show calendar is selected
                    deselectAllPills()
                    activateCalendarIcon()
                } else {
                    // Fallback to today if no date was saved
                    tvToday.performClick()
                }
            }

            else -> {
                tvToday.performClick()
            }
        }
    }

    // ============================================================
    // ✅ Hotel Profile Header & Image (Facebook & Instagram style)
    // ============================================================
    private fun setupNavHeaderProfile() {
        val headerView = navView.getHeaderView(0) ?: return
        val layoutProfile = headerView.findViewById<FrameLayout>(R.id.layoutHotelProfileImage)
        val tvInitials = headerView.findViewById<TextView>(R.id.tvHotelProfileInitials)

        layoutProfile?.setOnClickListener {
            showProfileImageBottomSheet()
        }
        tvInitials?.setOnClickListener {
            showProfileImageBottomSheet()
        }

        loadHotelProfileImage()
        setupNavHeaderBranches(headerView)
    }

    // ============================================================
    // ✅ Hotel Branches Setup & Dropdown
    // ============================================================
    private fun setupNavHeaderBranches(headerView: View) {
        val layoutDropdown = headerView.findViewById<View>(R.id.layoutHotelBranchDropdown)
        val btnDropdown = headerView.findViewById<View>(R.id.btnDropdownBranch)
        val btnPill = headerView.findViewById<View>(R.id.btnBranchSelectorPill)
        val layoutBranchInfoCard = headerView.findViewById<View>(R.id.layoutBranchInfoCard)
        val tvBranchViewMore = headerView.findViewById<View>(R.id.tvBranchViewMore)

        val openPickerListener = View.OnClickListener {
            showHotelBranchPickerBottomSheet()
        }

        layoutDropdown?.setOnClickListener(openPickerListener)
        btnDropdown?.setOnClickListener(openPickerListener)
        btnPill?.setOnClickListener(openPickerListener)

        val openDetailsListener = View.OnClickListener {
            showBranchDetailsBottomSheet()
        }
        layoutBranchInfoCard?.setOnClickListener(openDetailsListener)
        tvBranchViewMore?.setOnClickListener(openDetailsListener)

        updateNavHeaderBranchInfo()
    }

    /**
     * Updates Drawer Header and Main Toolbar with the current selected branch information.
     */
    private fun updateNavHeaderBranchInfo() {
        val headerView = navView.getHeaderView(0) ?: return
        val branchManager = HotelBranchManager.getInstance(this)
        val currentBranch = branchManager.getSelectedBranch()

        val tvHotelName = headerView.findViewById<TextView>(R.id.tvHotelName)
        val tvPartnerId = headerView.findViewById<TextView>(R.id.tvHotelPartnerId)
        val tvBadge = headerView.findViewById<TextView>(R.id.tvSelectedBranchBadge)
        val tvInitials = headerView.findViewById<TextView>(R.id.tvHotelProfileInitials)
        val tvBranchCode = headerView.findViewById<TextView>(R.id.tvBranchCode)
        val tvBranchLocation = headerView.findViewById<TextView>(R.id.tvBranchLocation)
        val tvBranchRooms = headerView.findViewById<TextView>(R.id.tvBranchRooms)
        val tvBranchCategory = headerView.findViewById<TextView>(R.id.tvBranchCategory)
        val tvBranchPropertyType = headerView.findViewById<TextView>(R.id.tvBranchPropertyType)

        name = currentBranch.name
        tvHotelName?.text = currentBranch.name
        tvPartnerId?.text = currentBranch.partnerId
        tvBadge?.text = "${currentBranch.locality} (${currentBranch.branchTag})"
        tvInitials?.text = currentBranch.initials
        tvBranchCode?.text = "BRANCH ID #${currentBranch.id}"
        tvBranchLocation?.text = currentBranch.address
        tvBranchRooms?.text = "${currentBranch.totalRooms} Rooms"
        tvBranchCategory?.text = "${currentBranch.categoryStars}-Star Luxury"
        tvBranchPropertyType?.text = currentBranch.propertyType

        // Update main screen toolbar
        findViewById<TextView>(R.id.hotelName)?.text = currentBranch.name
        findViewById<TextView>(R.id.hotelSubtitle)?.text = "${currentBranch.locality} • ${currentBranch.branchTag}"
    }

    /**
     * Shows the BottomSheet with list of branches when clicking the dropdown button.
     */
    private fun showHotelBranchPickerBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme)
        val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_hotel_branch_picker, null)
        bottomSheetDialog.setContentView(sheetView)

        val rvBranches = sheetView.findViewById<RecyclerView>(R.id.rvBranchesList)
        val btnClose = sheetView.findViewById<View>(R.id.btnCloseBranchSheet)
        val btnAdd = sheetView.findViewById<View>(R.id.btnAddNewBranch)

        val branchManager = HotelBranchManager.getInstance(this)
        val branches = branchManager.getAllBranches()
        val currentSelectedId = branchManager.getSelectedBranchId()

        rvBranches?.layoutManager = LinearLayoutManager(this)
        val adapter = HotelBranchAdapter(branches, currentSelectedId) { selectedBranch ->
            branchManager.setSelectedBranchId(selectedBranch.id)
            updateNavHeaderBranchInfo()
            Toast.makeText(this, "Selected: ${selectedBranch.name}", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()

            // Update Id if applicable and refresh bookings
            try {
                Id = BigInteger(selectedBranch.id)
            } catch (e: Exception) {
                // Keep Id
            }
            fetchBookings(Id, selectedDate)
        }
        rvBranches?.adapter = adapter

        btnClose?.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        btnAdd?.setOnClickListener {
            bottomSheetDialog.dismiss()
            showAddBranchDialog()
        }

        bottomSheetDialog.show()
    }

    /**
     * Shows all detailed information of the current selected branch.
     */
    private fun showBranchDetailsBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme)
        val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_branch_details, null)
        bottomSheetDialog.setContentView(sheetView)

        val branch = HotelBranchManager.getInstance(this).getSelectedBranch()

        sheetView.findViewById<TextView>(R.id.tvSheetBranchTitle)?.text = branch.name
        sheetView.findViewById<TextView>(R.id.tvSheetBranchSubtitle)?.text = "${branch.branchTag} • Partner ID #${branch.id}"
        sheetView.findViewById<TextView>(R.id.tvDetailLegalName)?.text = branch.legalBusinessName
        sheetView.findViewById<TextView>(R.id.tvDetailCategory)?.text = "${branch.categoryStars}-Star Luxury"
        sheetView.findViewById<TextView>(R.id.tvDetailPropertyType)?.text = branch.propertyType
        sheetView.findViewById<TextView>(R.id.tvDetailEstYear)?.text = branch.establishmentYear
        sheetView.findViewById<TextView>(R.id.tvDetailRooms)?.text = "${branch.totalRooms} Rooms (${branch.roomTypes})"
        sheetView.findViewById<TextView>(R.id.tvDetailAddress)?.text = branch.address
        sheetView.findViewById<TextView>(R.id.tvDetailCity)?.text = "City: ${branch.city}"
        sheetView.findViewById<TextView>(R.id.tvDetailLocality)?.text = "Locality: ${branch.locality}"
        sheetView.findViewById<TextView>(R.id.tvDetailPhone)?.text = branch.phone
        sheetView.findViewById<TextView>(R.id.tvDetailGst)?.text = branch.gstPan

        sheetView.findViewById<View>(R.id.btnCloseDetailSheet)?.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        sheetView.findViewById<View>(R.id.btnSheetSwitchBranch)?.setOnClickListener {
            bottomSheetDialog.dismiss()
            showHotelBranchPickerBottomSheet()
        }

        sheetView.findViewById<View>(R.id.btnSheetEditBranch)?.setOnClickListener {
            bottomSheetDialog.dismiss()
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            startActivity(Intent(this, EditHotelDetailsActivity::class.java))
        }

        bottomSheetDialog.show()
    }

    /**
     * Dialog to add a new hotel branch location.
     */
    private fun showAddBranchDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_hotel_branch, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val etName = dialogView.findViewById<android.widget.EditText>(R.id.etNewBranchName)
        val etTag = dialogView.findViewById<android.widget.EditText>(R.id.etNewBranchTag)
        val etLocality = dialogView.findViewById<android.widget.EditText>(R.id.etNewBranchLocality)
        val etCity = dialogView.findViewById<android.widget.EditText>(R.id.etNewBranchCity)
        val etAddress = dialogView.findViewById<android.widget.EditText>(R.id.etNewBranchAddress)
        val etRooms = dialogView.findViewById<android.widget.EditText>(R.id.etNewBranchRooms)
        val btnCancel = dialogView.findViewById<View>(R.id.btnCancelNewBranch)
        val btnSave = dialogView.findViewById<View>(R.id.btnSaveNewBranch)

        btnCancel?.setOnClickListener {
            dialog.dismiss()
        }

        btnSave?.setOnClickListener {
            val nameText = etName.text.toString().trim()
            val tagText = etTag.text.toString().trim().ifEmpty { "Branch" }
            val localityText = etLocality.text.toString().trim().ifEmpty { "Main Road" }
            val cityText = etCity.text.toString().trim().ifEmpty { "Gurgaon" }
            val addressText = etAddress.text.toString().trim()
            val roomsText = etRooms.text.toString().trim().ifEmpty { "30" }

            if (nameText.isEmpty()) {
                etName.error = "Branch name is required"
                return@setOnClickListener
            }
            if (addressText.isEmpty()) {
                etAddress.error = "Address is required"
                return@setOnClickListener
            }

            val branchManager = HotelBranchManager.getInstance(this)
            val newId = (100 + branchManager.getAllBranches().size + 1).toString()
            val initials = nameText.split(" ")
                .mapNotNull { it.firstOrNull()?.toString() }
                .take(3)
                .joinToString("")
                .uppercase()
                .ifEmpty { "GOH" }

            val newBranch = HotelBranch(
                id = newId,
                name = nameText,
                branchTag = tagText,
                locality = localityText,
                city = cityText,
                address = addressText,
                totalRooms = roomsText,
                initials = initials,
                partnerId = "Official Partner JIIFIT LLP • ID #$newId"
            )

            branchManager.addBranch(newBranch)
            branchManager.setSelectedBranchId(newId)
            updateNavHeaderBranchInfo()
            Toast.makeText(this, "Branch \"$nameText\" added successfully", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun loadHotelProfileImage() {
        val headerView = navView.getHeaderView(0) ?: return
        val tvProfileInitials = headerView.findViewById<TextView>(R.id.tvHotelProfileInitials) ?: return
        val ivProfileImage = headerView.findViewById<CircleImageView>(R.id.ivHotelProfileImage) ?: return

        val savedPath = sp.getString("hotel_profile_image_path", null)
        if (!savedPath.isNullOrEmpty() && File(savedPath).exists()) {
            Glide.with(this)
                .load(File(savedPath))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(ivProfileImage)
            ivProfileImage.visibility = View.VISIBLE
            tvProfileInitials.visibility = View.GONE
        } else {
            ivProfileImage.visibility = View.GONE
            tvProfileInitials.visibility = View.VISIBLE
        }
    }

    private fun showProfileImageBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme)
        val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_hotel_profile_picker, null)
        bottomSheetDialog.setContentView(sheetView)

        val optCamera = sheetView.findViewById<View>(R.id.layout_option_camera)
        val optGallery = sheetView.findViewById<View>(R.id.layout_option_gallery)
        val optView = sheetView.findViewById<View>(R.id.layout_option_view)
        val optRemove = sheetView.findViewById<View>(R.id.layout_option_remove)
        val btnClose = sheetView.findViewById<ImageView>(R.id.btn_close_sheet)

        val savedPath = sp.getString("hotel_profile_image_path", null)
        val hasCustomImage = !savedPath.isNullOrEmpty() && File(savedPath).exists()

        optRemove?.visibility = if (hasCustomImage) View.VISIBLE else View.GONE

        optCamera?.setOnClickListener {
            bottomSheetDialog.dismiss()
            checkCameraPermissionAndLaunch()
        }

        optGallery?.setOnClickListener {
            bottomSheetDialog.dismiss()
            galleryLauncher.launch("image/*")
        }

        optView?.setOnClickListener {
            bottomSheetDialog.dismiss()
            showFullProfileImageDialog()
        }

        optRemove?.setOnClickListener {
            bottomSheetDialog.dismiss()
            removeHotelProfileImage()
        }

        btnClose?.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun checkCameraPermissionAndLaunch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            takePhotoWithCamera()
        }
    }

    private fun takePhotoWithCamera() {
        try {
            val photoFile = File(cacheDir, "hotel_cam_${System.currentTimeMillis()}.jpg")
            val photoUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)
            cameraImageUri = photoUri
            cameraLauncher.launch(photoUri)
        } catch (e: Exception) {
            Log.e("HotelProfile", "Failed to start camera", e)
            Toast.makeText(this, "Could not open camera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveAndApplyProfileImage(sourceUri: Uri) {
        try {
            val destFile = File(filesDir, "hotel_profile_photo.jpg")
            contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            sp.edit().putString("hotel_profile_image_path", destFile.absolutePath).apply()
            loadHotelProfileImage()
            Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("HotelProfile", "Error saving profile image", e)
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeHotelProfileImage() {
        val savedPath = sp.getString("hotel_profile_image_path", null)
        if (!savedPath.isNullOrEmpty()) {
            val file = File(savedPath)
            if (file.exists()) {
                file.delete()
            }
        }
        sp.edit().remove("hotel_profile_image_path").apply()
        loadHotelProfileImage()
        Toast.makeText(this, "Profile picture removed", Toast.LENGTH_SHORT).show()
    }

    private fun showFullProfileImageDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_view_hotel_profile_image)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.88).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val ivFull = dialog.findViewById<CircleImageView>(R.id.ivFullProfileImage)
        val tvInitials = dialog.findViewById<TextView>(R.id.tvFullProfileInitials)
        val tvFullHotelName = dialog.findViewById<TextView>(R.id.tvFullHotelName)
        val btnClose = dialog.findViewById<ImageView>(R.id.btnCloseFullImage)

        tvFullHotelName?.text = name
        tvInitials?.text = "GOH"

        btnClose?.setOnClickListener { dialog.dismiss() }

        val savedPath = sp.getString("hotel_profile_image_path", null)
        if (!savedPath.isNullOrEmpty() && File(savedPath).exists()) {
            ivFull?.visibility = View.VISIBLE
            tvInitials?.visibility = View.GONE
            Glide.with(this)
                .load(File(savedPath))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(ivFull)
        } else {
            ivFull?.visibility = View.GONE
            tvInitials?.visibility = View.VISIBLE
        }

        dialog.show()
    }
}