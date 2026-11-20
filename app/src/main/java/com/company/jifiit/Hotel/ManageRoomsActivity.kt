package com.company.jifiit.Hotel

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.company.jifiit.Hotel.Adapter.ManageRoomsAdapter
import com.company.jifiit.Hotel.Model.HotelRoomItem
import com.company.jifiit.R
import com.google.android.material.button.MaterialButton

class ManageRoomsActivity : AppCompatActivity() {

    private lateinit var drawerLayout: androidx.drawerlayout.widget.DrawerLayout
    private lateinit var navView: com.google.android.material.navigation.NavigationView
    private lateinit var btnAddFloor: MaterialButton
    private lateinit var btnDeleteFloor: MaterialButton
    private lateinit var btnAddRoom: MaterialButton
    private lateinit var layoutFloorTabs: LinearLayout
    private lateinit var tvFloorRoomsSummary: TextView
    private lateinit var layoutEmptyRooms: View
    private lateinit var rvRoomsGrid: RecyclerView
    private lateinit var roomsAdapter: ManageRoomsAdapter

    private var selectedFloorNumber = 3

    data class FloorData(
        val floor: Int,
        var startRoom: Int,
        var endRoom: Int
    )

    private val floors = mutableListOf(
        FloorData(1, 101, 112),
        FloorData(2, 201, 212),
        FloorData(3, 301, 312),
        FloorData(4, 401, 412),
        FloorData(5, 501, 512)
    )

    private val roomsByFloor = mutableMapOf<Int, MutableList<HotelRoomItem>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_manage_rooms)

        // ✅ White icons on brand blue status bar matching application theme
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = true
        }

        window.statusBarColor = ContextCompat.getColor(this, R.color.hotel_primary)
        window.navigationBarColor = Color.WHITE

        val root = findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.drawer_layout)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, 0, bars.right, bars.bottom)
            insets
        }

        val header = findViewById<View>(R.id.layoutHotelHeader)
        ViewCompat.setOnApplyWindowInsetsListener(header) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, bars.top, v.paddingRight, v.paddingBottom)
            insets
        }

        initDefaultRooms()
        initViews()
        setupFloorTabs()
        setupRoomsGrid()
    }

    private fun initDefaultRooms() {
        roomsByFloor[3] = mutableListOf(
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
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        btnAddFloor = findViewById(R.id.btnAddFloor)
        btnDeleteFloor = findViewById(R.id.btnDeleteFloor)
        btnAddRoom = findViewById(R.id.btnAddRoom)
        layoutFloorTabs = findViewById(R.id.layoutFloorTabs)
        tvFloorRoomsSummary = findViewById(R.id.tvFloorRoomsSummary)
        layoutEmptyRooms = findViewById(R.id.layoutEmptyRooms)
        rvRoomsGrid = findViewById(R.id.rvRoomsGrid)

        HotelHeaderHelper.setupHeader(this, drawerLayout, navView, R.id.nav_rooms)

        btnAddFloor.setOnClickListener {
            showAddFloorDialog()
        }

        btnDeleteFloor.setOnClickListener {
            showDeleteFloorConfirmationDialog()
        }

        btnAddRoom.setOnClickListener {
            showAddRoomDialog()
        }
    }

    private fun setupFloorTabs() {
        layoutFloorTabs.removeAllViews()

        floors.forEach { floorData ->
            val tabView = LayoutInflater.from(this)
                .inflate(R.layout.item_floor_tab, layoutFloorTabs, false)

            val tvFloorNumber = tabView.findViewById<TextView>(R.id.tvFloorNumber)
            val tvRangeStart = tabView.findViewById<TextView>(R.id.tvFloorRangeStart)
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
            onDeleteRoomClick = { room ->
                showDeleteRoomConfirmationDialog(room)
            },
            onRoomClick = { room ->
                if (room.isBooked) {
                    showActiveSessionDialog(room)
                } else {
                    Toast.makeText(this, "${room.roomNumber} is Available (${room.roomType})", Toast.LENGTH_SHORT).show()
                }
            }
        )
        rvRoomsGrid.adapter = roomsAdapter

        loadRoomsForFloor(selectedFloorNumber)
    }

    private fun getOrCreateRoomsForFloor(floor: Int): MutableList<HotelRoomItem> {
        return roomsByFloor.getOrPut(floor) {
            val floorData = floors.firstOrNull { it.floor == floor }
            val start = floorData?.startRoom ?: (floor * 100 + 1)
            val end = floorData?.endRoom ?: (floor * 100 + 12)
            generateRoomsForFloor(floor, start, end)
        }
    }

    private fun loadRoomsForFloor(floor: Int) {
        val rooms = getOrCreateRoomsForFloor(floor)

        tvFloorRoomsSummary.text = "Floor $floor Rooms (${rooms.size})"

        if (rooms.isEmpty()) {
            rvRoomsGrid.visibility = View.GONE
            layoutEmptyRooms.visibility = View.VISIBLE
        } else {
            rvRoomsGrid.visibility = View.VISIBLE
            layoutEmptyRooms.visibility = View.GONE
        }

        roomsAdapter.updateRooms(rooms.toList())
    }

    private fun generateRoomsForFloor(floor: Int, start: Int, end: Int): MutableList<HotelRoomItem> {
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

    // ============================================================
    // ✅ ADD FLOOR
    // ============================================================
    private fun showAddFloorDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_floor, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val etFloorNumber = dialogView.findViewById<EditText>(R.id.etNewFloorNumber)
        val etStartRoom = dialogView.findViewById<EditText>(R.id.etNewFloorStartRoom)
        val etEndRoom = dialogView.findViewById<EditText>(R.id.etNewFloorEndRoom)
        val btnCancel = dialogView.findViewById<View>(R.id.btnCancelNewFloor)
        val btnSave = dialogView.findViewById<View>(R.id.btnSaveNewFloor)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val floorStr = etFloorNumber.text.toString().trim()
            if (floorStr.isEmpty()) {
                etFloorNumber.error = "Floor number is required"
                return@setOnClickListener
            }

            val floorNum = floorStr.toIntOrNull()
            if (floorNum == null || floorNum <= 0) {
                etFloorNumber.error = "Enter a valid floor number"
                return@setOnClickListener
            }

            if (floors.any { it.floor == floorNum }) {
                etFloorNumber.error = "Floor $floorNum already exists"
                return@setOnClickListener
            }

            val defaultStart = floorNum * 100 + 1
            val defaultEnd = floorNum * 100 + 12

            val startRoom = etStartRoom.text.toString().trim().toIntOrNull() ?: defaultStart
            val endRoom = etEndRoom.text.toString().trim().toIntOrNull() ?: defaultEnd

            val newFloor = FloorData(floorNum, startRoom, maxOf(startRoom, endRoom))
            floors.add(newFloor)
            floors.sortBy { it.floor }

            roomsByFloor[floorNum] = generateRoomsForFloor(floorNum, startRoom, maxOf(startRoom, endRoom))

            selectedFloorNumber = floorNum
            setupFloorTabs()
            loadRoomsForFloor(selectedFloorNumber)

            Toast.makeText(this, "Floor $floorNum added successfully", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    // ============================================================
    // ✅ DELETE FLOOR
    // ============================================================
    private fun showDeleteFloorConfirmationDialog() {
        if (floors.size <= 1) {
            Toast.makeText(this, "Cannot delete the only remaining floor.", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Delete Floor $selectedFloorNumber")
            .setMessage("Are you sure you want to delete Floor $selectedFloorNumber and all its rooms? This cannot be undone.")
            .setPositiveButton("Delete Floor") { _, _ ->
                val floorToDelete = selectedFloorNumber
                floors.removeAll { it.floor == floorToDelete }
                roomsByFloor.remove(floorToDelete)

                selectedFloorNumber = floors.firstOrNull()?.floor ?: 1
                setupFloorTabs()
                loadRoomsForFloor(selectedFloorNumber)

                Toast.makeText(this, "Floor $floorToDelete deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    // ============================================================
    // ✅ ADD ROOM
    // ============================================================
    private fun showAddRoomDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_room, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvSubtitle = dialogView.findViewById<TextView>(R.id.tvAddRoomDialogSubtitle)
        val etRoomNumber = dialogView.findViewById<EditText>(R.id.etNewRoomNumber)
        val etRoomType = dialogView.findViewById<EditText>(R.id.etNewRoomType)
        val btnCancel = dialogView.findViewById<View>(R.id.btnCancelNewRoom)
        val btnSave = dialogView.findViewById<View>(R.id.btnSaveNewRoom)

        tvSubtitle.text = "Add a room to Floor $selectedFloorNumber"

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val roomInput = etRoomNumber.text.toString().trim()
            if (roomInput.isEmpty()) {
                etRoomNumber.error = "Room number is required"
                return@setOnClickListener
            }

            val formattedRoomNumber = if (roomInput.startsWith("Room ", ignoreCase = true)) {
                roomInput
            } else {
                "Room $roomInput"
            }

            val roomTypeInput = etRoomType.text.toString().trim().ifEmpty { "Deluxe Suite" }

            val currentRooms = getOrCreateRoomsForFloor(selectedFloorNumber)
            if (currentRooms.any { it.roomNumber.equals(formattedRoomNumber, ignoreCase = true) }) {
                etRoomNumber.error = "$formattedRoomNumber already exists on this floor"
                return@setOnClickListener
            }

            val newRoom = HotelRoomItem(
                roomNumber = formattedRoomNumber,
                roomType = roomTypeInput,
                floor = selectedFloorNumber,
                isBooked = false
            )

            currentRooms.add(newRoom)
            loadRoomsForFloor(selectedFloorNumber)

            // Update endRoom display in floor tab if appropriate
            val numericPart = roomInput.filter { it.isDigit() }.toIntOrNull()
            if (numericPart != null) {
                val floorData = floors.firstOrNull { it.floor == selectedFloorNumber }
                if (floorData != null && numericPart > floorData.endRoom) {
                    floorData.endRoom = numericPart
                    setupFloorTabs()
                }
            }

            Toast.makeText(this, "$formattedRoomNumber added successfully", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    // ============================================================
    // ✅ DELETE ROOM
    // ============================================================
    private fun showDeleteRoomConfirmationDialog(room: HotelRoomItem) {
        AlertDialog.Builder(this)
            .setTitle("Delete ${room.roomNumber}")
            .setMessage("Are you sure you want to remove ${room.roomNumber} (${room.roomType}) from Floor $selectedFloorNumber?")
            .setPositiveButton("Delete") { _, _ ->
                val currentRooms = getOrCreateRoomsForFloor(selectedFloorNumber)
                currentRooms.removeAll { it.roomNumber == room.roomNumber }
                loadRoomsForFloor(selectedFloorNumber)
                Toast.makeText(this, "${room.roomNumber} removed", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun showActiveSessionDialog(room: HotelRoomItem) {
        AlertDialog.Builder(this)
            .setTitle(room.roomNumber)
            .setMessage("Active Service: ${room.activeSessionText ?: "In Session"}\nCategory: ${room.roomType}\nStatus: Ongoing Concierge Delivery")
            .setPositiveButton("Close", null)
            .create()
            .show()
    }
}
