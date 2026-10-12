package com.company.jifiit.Hotel

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.company.jifiit.Hotel.Adapter.ManageRoomsAdapter
import com.company.jifiit.Hotel.Model.HotelRoomItem
import com.company.jifiit.R

class ManageRoomsActivity : AppCompatActivity() {

    private lateinit var btnBackToDashboard: LinearLayout
    private lateinit var layoutFloorTabs: LinearLayout
    private lateinit var rvRoomsGrid: RecyclerView
    private lateinit var roomsAdapter: ManageRoomsAdapter

    private var selectedFloorNumber = 3 // Floor 3 selected by default as shown in reference design

    data class FloorData(
        val floor: Int,
        val startRoom: Int,
        val endRoom: Int
    )

    private val floors = listOf(
        FloorData(1, 101, 124),
        FloorData(2, 201, 224),
        FloorData(3, 301, 324),
        FloorData(4, 401, 424),
        FloorData(5, 501, 524)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manage_rooms)

        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        val root = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, bars.top, 0, bars.bottom)
            insets
        }

        initViews()
        setupFloorTabs()
        setupRoomsGrid()
    }

    private fun initViews() {
        btnBackToDashboard = findViewById(R.id.btnBackToDashboard)
        layoutFloorTabs = findViewById(R.id.layoutFloorTabs)
        rvRoomsGrid = findViewById(R.id.rvRoomsGrid)

        btnBackToDashboard.setOnClickListener {
            finish()
        }
    }

    private fun setupFloorTabs() {
        layoutFloorTabs.removeAllViews()

        floors.forEach { floorData ->
            val tabView = LayoutInflater.from(this)
                .inflate(R.layout.item_floor_tab, layoutFloorTabs, false)

            val tvFloorLabel = tabView.findViewById<TextView>(R.id.tvFloorLabel)
            val tvFloorNumber = tabView.findViewById<TextView>(R.id.tvFloorNumber)
            val tvRangeStart = tabView.findViewById<TextView>(R.id.tvFloorRangeStart)
            val tvRangeDash = tabView.findViewById<TextView>(R.id.tvFloorRangeDash)
            val tvRangeEnd = tabView.findViewById<TextView>(R.id.tvFloorRangeEnd)

            tvFloorNumber.text = floorData.floor.toString()
            tvRangeStart.text = "(${floorData.startRoom}"
            tvRangeEnd.text = "${floorData.endRoom})"

            val isSelected = floorData.floor == selectedFloorNumber
            updateFloorTabAppearance(tabView, isSelected)

            tabView.setOnClickListener {
                if (selectedFloorNumber != floorData.floor) {
                    selectedFloorNumber = floorData.floor
                    setupFloorTabs()
                    loadRoomsForFloor(selectedFloorNumber)
                }
            }

            layoutFloorTabs.addView(tabView)
        }
    }

    private fun updateFloorTabAppearance(view: View, isSelected: Boolean) {
        val tvFloorLabel = view.findViewById<TextView>(R.id.tvFloorLabel)
        val tvFloorNumber = view.findViewById<TextView>(R.id.tvFloorNumber)
        val tvRangeStart = view.findViewById<TextView>(R.id.tvFloorRangeStart)
        val tvRangeDash = view.findViewById<TextView>(R.id.tvFloorRangeDash)
        val tvRangeEnd = view.findViewById<TextView>(R.id.tvFloorRangeEnd)

        if (isSelected) {
            view.setBackgroundResource(R.drawable.bg_floor_card_selected)
            val white = Color.WHITE
            tvFloorLabel.setTextColor(white)
            tvFloorNumber.setTextColor(white)
            tvRangeStart.setTextColor(white)
            tvRangeDash.setTextColor(white)
            tvRangeEnd.setTextColor(white)
        } else {
            view.setBackgroundResource(R.drawable.bg_floor_card_unselected)
            val dark = Color.parseColor("#0F172A")
            tvFloorLabel.setTextColor(dark)
            tvFloorNumber.setTextColor(dark)
            tvRangeStart.setTextColor(dark)
            tvRangeDash.setTextColor(dark)
            tvRangeEnd.setTextColor(dark)
        }
    }

    private fun setupRoomsGrid() {
        rvRoomsGrid.layoutManager = GridLayoutManager(this, 2)
        roomsAdapter = ManageRoomsAdapter(
            roomsList = emptyList(),
            onBookClick = { room ->
                openBookSessionWorkflow(room)
            },
            onRoomClick = { room ->
                if (room.isBooked) {
                    showActiveSessionDialog(room)
                } else {
                    openBookSessionWorkflow(room)
                }
            }
        )
        rvRoomsGrid.adapter = roomsAdapter

        loadRoomsForFloor(selectedFloorNumber)
    }

    private fun loadRoomsForFloor(floor: Int) {
        val rooms = when (floor) {
            3 -> listOf(
                HotelRoomItem("Room 301", "Presidential Suite", 3, isBooked = false),
                HotelRoomItem("Room 302", "Deluxe Suite", 3, isBooked = false),
                HotelRoomItem("Room 303", "Deluxe Suite", 3, isBooked = false),
                HotelRoomItem("Room 304", "Executive Suite", 3, isBooked = true, activeSessionText = "MASSAGE • 01:00 PM"),
                HotelRoomItem("Room 305", "Executive Suite", 3, isBooked = false),
                HotelRoomItem("Room 306", "Superior Deluxe", 3, isBooked = false),
                HotelRoomItem("Room 307", "Club Room", 3, isBooked = true, activeSessionText = "YOGA • 05:00 PM"),
                HotelRoomItem("Room 308", "Presidential Suite", 3, isBooked = false),
                HotelRoomItem("Room 309", "Deluxe Suite", 3, isBooked = false),
                HotelRoomItem("Room 310", "Executive Suite", 3, isBooked = false),
                HotelRoomItem("Room 311", "Superior Deluxe", 3, isBooked = false),
                HotelRoomItem("Room 312", "Club Room", 3, isBooked = false)
            )
            1 -> generateRoomsForFloor(1, 101, 112)
            2 -> generateRoomsForFloor(2, 201, 212)
            4 -> generateRoomsForFloor(4, 401, 412)
            5 -> generateRoomsForFloor(5, 501, 512)
            else -> generateRoomsForFloor(floor, floor * 100 + 1, floor * 100 + 12)
        }

        roomsAdapter.updateRooms(rooms)
    }

    private fun generateRoomsForFloor(floor: Int, start: Int, end: Int): List<HotelRoomItem> {
        val types = listOf("Presidential Suite", "Deluxe Suite", "Executive Suite", "Superior Deluxe", "Club Room")
        val list = mutableListOf<HotelRoomItem>()
        for (i in start..end) {
            val type = types[(i % types.size)]
            val isBooked = (i == start + 3)
            val sessionText = if (isBooked) "MASSAGE • 02:30 PM" else null
            list.add(HotelRoomItem("Room $i", type, floor, isBooked, sessionText))
        }
        return list
    }

    private fun openBookSessionWorkflow(room: HotelRoomItem) {
        val intent = Intent(this, AddBookingActivity::class.java).apply {
            putExtra("ROOM_NUMBER", room.roomNumber)
            putExtra("FLOOR_NUMBER", room.floor.toString())
        }
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun showActiveSessionDialog(room: HotelRoomItem) {
        AlertDialog.Builder(this)
            .setTitle(room.roomNumber)
            .setMessage("Active Service: ${room.activeSessionText ?: "In Session"}\nCategory: ${room.roomType}\nStatus: Ongoing Concierge Delivery")
            .setPositiveButton("View Booking Details") { _, _ ->
                Toast.makeText(this, "Opening booking for ${room.roomNumber}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Close", null)
            .create()
            .show()
    }
}
