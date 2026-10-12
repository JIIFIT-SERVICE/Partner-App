package com.company.jifiit.Hotel

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.company.jifiit.Constant
import com.company.jifiit.Hotel.Model.RoomModel
import com.company.jifiit.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.hbb20.CountryCodePicker
import org.json.JSONObject
import java.math.BigInteger

class Room_Info : AppCompatActivity() {

    private lateinit var btnBack: ImageView

    private lateinit var etGuestName: EditText
    private lateinit var etRoomNumber: MaterialAutoCompleteTextView
    private lateinit var etFloorWing: EditText
    private lateinit var etGuestMobile: EditText

    private lateinit var countryCodePicker: CountryCodePicker
    private lateinit var btnContinue: MaterialButton
    var phone: String = ""

    // =====================================================
    // ROOM DATA
    // =====================================================
    private lateinit var Id: BigInteger
    private lateinit var sp: SharedPreferences
    private val roomList = ArrayList<RoomModel>()

    private lateinit var roomAdapter: ArrayAdapter<String>

    private var selectedRoom: RoomModel? = null

    private lateinit var service: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        WindowCompat.getInsetsController(
            window,
            window.decorView
        ).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        setContentView(R.layout.activity_room_info)

        // =====================================================
        // WINDOW INSETS
        // =====================================================

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                maxOf(systemBars.bottom, ime.bottom)
            )
            insets
        }
        service = intent.getStringExtra("SELECTED_SERVICE") ?: ""

        val spCred = getSharedPreferences("credentials", MODE_PRIVATE)

        val idString = spCred.getString("Id", "0") ?: "0"
        Id = try {
            BigInteger(idString)
        } catch (e: NumberFormatException) {
            BigInteger.ZERO
        }
        // =====================================================
        // FIND VIEWS
        // =====================================================

        btnBack = findViewById(R.id.btnBack)

        etGuestName = findViewById(R.id.etGuestName)
        etRoomNumber = findViewById(R.id.etRoomNumber)
        etFloorWing = findViewById(R.id.etFloorWing)
        etGuestMobile = findViewById(R.id.etGuestMobile)

        countryCodePicker =
            findViewById(R.id.login_countrycode)

        btnContinue =
            findViewById(R.id.btnContinue)

        // =====================================================
        // COUNTRY CODE
        // =====================================================

        countryCodePicker.registerCarrierNumberEditText(etGuestMobile)

        // =====================================================
        // ROOM DROPDOWN
        // =====================================================

        setupRoomDropdown()

        // =====================================================
        // LOAD ROOMS FROM BACKEND
        // =====================================================

        getRooms()

        // =====================================================
        // BACK
        // =====================================================

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            overridePendingTransition(
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            // অথবা সরাসরি: finish()
        }

        // =====================================================
        // CONTINUE
        // =====================================================

        btnContinue.setOnClickListener {

            validateAndContinue()

        }
        countryCodePicker.setDialogBackgroundColor(Color.WHITE)
        countryCodePicker.setDialogTextColor(Color.BLACK)

    }

    // =========================================================
    // SETUP ROOM DROPDOWN
    // =========================================================

    // =========================================================
// SETUP ROOM DROPDOWN
// =========================================================

    private fun setupRoomDropdown() {

        roomAdapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_dropdown_item_1line,
            ArrayList()
        ) {

            override fun getView(
                position: Int,
                convertView: android.view.View?,
                parent: android.view.ViewGroup
            ): android.view.View {

                val view = super.getView(position, convertView, parent)

                val textView = view as android.widget.TextView

                // Dropdown background
                view.setBackgroundColor(Color.WHITE)

                // Dropdown text
                textView.setTextColor(Color.BLACK)

                // Text size
                textView.textSize = 15f

                // Padding
                textView.setPadding(
                    20,
                    18,
                    20,
                    18
                )

                return view
            }
        }

        etRoomNumber.setAdapter(roomAdapter)

        // =====================================================
        // ITEM SELECTED
        // =====================================================

        etRoomNumber.setOnItemClickListener { _, _, position, _ ->

            if (position >= 0 && position < roomList.size) {

                selectedRoom = roomList[position]

                // Selected room number
                etRoomNumber.setText(
                    selectedRoom?.roomNumber ?: "",
                    false
                )

                // Dropdown closed
                etRoomNumber.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.arrow_down,
                    0
                )
            }
        }

        // =====================================================
        // OPEN / CLOSE DROPDOWN
        // =====================================================

        etRoomNumber.setOnClickListener {

            if (roomList.isEmpty()) {

                Toast.makeText(
                    this,
                    "No rooms available",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                if (etRoomNumber.isPopupShowing) {

                    // -----------------------------------------
                    // DROPDOWN IS OPEN → CLOSE IT
                    // -----------------------------------------

                    etRoomNumber.dismissDropDown()

                    etRoomNumber.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.arrow_down,
                        0
                    )

                } else {

                    // -----------------------------------------
                    // DROPDOWN IS CLOSED → OPEN IT
                    // -----------------------------------------

                    etRoomNumber.showDropDown()

                    etRoomNumber.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.arrow_up,
                        0
                    )
                }
            }
        }

        // =====================================================
        // DROPDOWN DISMISS LISTENER
        // =====================================================

        etRoomNumber.setOnDismissListener {

            etRoomNumber.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.arrow_down,
                0
            )
        }
    }

    // =========================================================
    // GET ROOMS FROM BACKEND
    // =========================================================

    private fun getRooms() {

        val request = StringRequest(
            Request.Method.GET,
            Constant.GET_HOTEL_ROOMS_API+Id,

            { response ->

                try {

                    val jsonObject =
                        JSONObject(response)

                    val status =
                        jsonObject.optBoolean("status", false)

                    if (!status) {

                        Toast.makeText(
                            this,
                            jsonObject.optString(
                                "message",
                                "Unable to load rooms"
                            ),
                            Toast.LENGTH_SHORT
                        ).show()

                        return@StringRequest
                    }

                    val dataArray =
                        jsonObject.optJSONArray("data")

                    roomList.clear()

                    roomAdapter.clear()

                    if (dataArray != null) {

                        for (i in 0 until dataArray.length()) {

                            val item =
                                dataArray.getJSONObject(i)

                            val room = RoomModel(

                                id = item.optString("id"),

                                roomNumber =
                                item.optString("room_number")
                            )

                            roomList.add(room)

                            roomAdapter.add(
                                room.roomNumber
                            )
                        }
                    }

                    roomAdapter.notifyDataSetChanged()

                } catch (e: Exception) {

                    e.printStackTrace()

                    Toast.makeText(
                        this,
                        "Invalid server response",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },

            { error ->

                error.printStackTrace()

                Toast.makeText(
                    this,
                    "Failed to load rooms",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        Volley
            .newRequestQueue(this)
            .add(request)
    }

    // =========================================================
    // VALIDATE FORM
    // =========================================================

    private fun validateAndContinue() {

        val guestName =
            etGuestName.text.toString().trim()

        val roomNumber =
            etRoomNumber.text.toString().trim()

        val floorWing =
            etFloorWing.text.toString().trim()

        val mobile =
            etGuestMobile.text.toString().trim()

        // =====================================================
        // GUEST NAME
        // =====================================================

        if (guestName.isEmpty()) {

            etGuestName.error =
                "Enter guest name"

            etGuestName.requestFocus()

            return
        }

        // =====================================================
        // ROOM
        // =====================================================

        if (roomNumber.isEmpty()) {

            etRoomNumber.error =
                "Select room number"

            etRoomNumber.requestFocus()

            return
        }

        // Make sure room was actually selected
        if (selectedRoom == null) {

            etRoomNumber.error =
                "Please select a room from the list"

            etRoomNumber.requestFocus()

            return
        }

        // =====================================================
        // MOBILE
        // =====================================================

        if (mobile.isEmpty()) {

            etGuestMobile.error =
                "Enter mobile number"

            etGuestMobile.requestFocus()

            return
        }

        // =====================================================
        // MOBILE VALIDATION
        // =====================================================

        if (!countryCodePicker.isValidFullNumber) {

            etGuestMobile.error =
                "Enter a valid mobile number"

            etGuestMobile.requestFocus()

            return
        }

        // =====================================================
        // COMPLETE MOBILE
        // =====================================================

        val fullMobileNumber =
            countryCodePicker.fullNumberWithPlus

        // =====================================================
        // SELECTED ROOM
        // =====================================================

        val selectedRoomData =
            selectedRoom ?: return

        // =====================================================
        // INTENT
        // =====================================================

        val intent = Intent(this, Preference_Bed::class.java)

        // Guest
        intent.putExtra(
            "guest_name",
            guestName
        )

        intent.putExtra(
            "guest_mobile",
            fullMobileNumber
        )

        // Room
        intent.putExtra(
            "room_id",
            selectedRoomData.id
        )

        intent.putExtra(
            "room_number",
            selectedRoomData.roomNumber
        )

        intent.putExtra("floor_wing", floorWing)
        intent.putExtra("SELECTED_SERVICE", service)
        startActivity(intent)

        overridePendingTransition(
            R.anim.slide_in_right,
            R.anim.slide_out_left
        )
    }

    // =========================================================
    // BACK PRESS
    // =========================================================

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {

        super.onBackPressed()

        overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
    }
}