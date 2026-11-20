package com.company.jifiit.Hotel

import android.content.Context
import android.content.SharedPreferences
import com.company.jifiit.Hotel.Model.HotelBranch
import org.json.JSONArray
import org.json.JSONObject

class HotelBranchManager private constructor(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "hotel_branches_prefs"
        private const val KEY_BRANCHES = "branches_json_list"
        private const val KEY_SELECTED_BRANCH_ID = "selected_branch_id"

        @Volatile
        private var instance: HotelBranchManager? = null

        fun getInstance(context: Context): HotelBranchManager {
            return instance ?: synchronized(this) {
                instance ?: HotelBranchManager(context.applicationContext).also { instance = it }
            }
        }
    }

    init {
        ensureDefaultBranches()
    }

    private fun ensureDefaultBranches() {
        val jsonString = prefs.getString(KEY_BRANCHES, null)
        if (jsonString.isNullOrEmpty()) {
            val defaultList = listOf(
                HotelBranch(
                    id = "101",
                    name = "Grand Orchid - Sector 14",
                    branchTag = "Main Branch",
                    legalBusinessName = "Grand Orchid Hospitality Pvt Ltd",
                    categoryStars = 4,
                    propertyType = "Hotel",
                    establishmentYear = "2018",
                    totalRooms = "50",
                    roomTypes = "Deluxe Room,Executive Suite,Club Room",
                    city = "Gurgaon",
                    cityId = "1",
                    locality = "Sector 14",
                    localityId = "101",
                    address = "Plot 42, DLF Phase 2, Near MG Road, Gurgaon, Haryana - 122002",
                    gstPan = "06AAACG1234F1Z8",
                    phone = "+91 98765 43210",
                    email = "sector14@grandorchid.com",
                    initials = "GOH",
                    partnerId = "Official Partner JIIFIT LLP • ID #101"
                ),
                HotelBranch(
                    id = "102",
                    name = "Grand Orchid - DLF Cyber City",
                    branchTag = "Cyber City",
                    legalBusinessName = "Grand Orchid Hospitality Pvt Ltd",
                    categoryStars = 5,
                    propertyType = "Boutique",
                    establishmentYear = "2021",
                    totalRooms = "75",
                    roomTypes = "Executive Suite,Presidential Suite,Business Deluxe",
                    city = "Gurgaon",
                    cityId = "1",
                    locality = "Cyber City",
                    localityId = "102",
                    address = "Building 8, DLF Cyber Hub, DLF Phase 2, Gurgaon, Haryana - 122008",
                    gstPan = "06AAACG1234F1Z8",
                    phone = "+91 98765 43211",
                    email = "cybercity@grandorchid.com",
                    initials = "GOC",
                    partnerId = "Official Partner JIIFIT LLP • ID #102"
                ),
                HotelBranch(
                    id = "103",
                    name = "Grand Orchid - Golf Course Road",
                    branchTag = "Golf Course",
                    legalBusinessName = "Grand Orchid Hospitality Pvt Ltd",
                    categoryStars = 5,
                    propertyType = "Resort",
                    establishmentYear = "2020",
                    totalRooms = "60",
                    roomTypes = "Pool Villa,Luxury Suite,Golf View Deluxe",
                    city = "Gurgaon",
                    cityId = "1",
                    locality = "Golf Course Road",
                    localityId = "103",
                    address = "Sector 54, Golf Course Road, Gurgaon, Haryana - 122011",
                    gstPan = "06AAACG1234F1Z8",
                    phone = "+91 98765 43212",
                    email = "golfcourse@grandorchid.com",
                    initials = "GOG",
                    partnerId = "Official Partner JIIFIT LLP • ID #103"
                ),
                HotelBranch(
                    id = "104",
                    name = "Grand Orchid - Aerocity Delhi",
                    branchTag = "Aerocity",
                    legalBusinessName = "Grand Orchid Hospitality Pvt Ltd",
                    categoryStars = 5,
                    propertyType = "Hotel",
                    establishmentYear = "2022",
                    totalRooms = "110",
                    roomTypes = "Airport View Suite,Executive Club,Superior Deluxe",
                    city = "Delhi",
                    cityId = "2",
                    locality = "Aerocity",
                    localityId = "201",
                    address = "Hospitality District, Asset 3, Aerocity, New Delhi - 110037",
                    gstPan = "07AAACG1234F1Z9",
                    phone = "+91 98765 43213",
                    email = "aerocity@grandorchid.com",
                    initials = "GOA",
                    partnerId = "Official Partner JIIFIT LLP • ID #104"
                )
            )
            saveBranches(defaultList)
            setSelectedBranchId("101")
        }
    }

    fun getAllBranches(): List<HotelBranch> {
        val jsonString = prefs.getString(KEY_BRANCHES, null) ?: return emptyList()
        val list = mutableListOf<HotelBranch>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(HotelBranch.fromJson(obj))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveBranches(branches: List<HotelBranch>) {
        try {
            val jsonArray = JSONArray()
            branches.forEach { jsonArray.put(it.toJson()) }
            prefs.edit().putString(KEY_BRANCHES, jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getSelectedBranchId(): String {
        return prefs.getString(KEY_SELECTED_BRANCH_ID, "101") ?: "101"
    }

    fun getSelectedBranch(): HotelBranch {
        val branches = getAllBranches()
        val selectedId = getSelectedBranchId()
        return branches.find { it.id == selectedId }
            ?: branches.firstOrNull()
            ?: HotelBranch("101", "Grand Orchid Hotel", "Main Branch")
    }

    fun setSelectedBranchId(branchId: String) {
        prefs.edit().putString(KEY_SELECTED_BRANCH_ID, branchId).apply()
        syncSelectedBranchToHotelDetails()
    }

    /**
     * Propagates selected branch details to `hotel_details` and `credentials`
     * SharedPreferences so all activities load the selected branch information.
     */
    fun syncSelectedBranchToHotelDetails() {
        val branch = getSelectedBranch()
        val spDetails = context.getSharedPreferences("hotel_details", Context.MODE_PRIVATE)
        val spCred = context.getSharedPreferences("credentials", Context.MODE_PRIVATE)

        spDetails.edit().apply {
            putString("hotel_name", branch.name)
            putString("legal_business_name", branch.legalBusinessName)
            putInt("hotel_category", branch.categoryStars)
            putString("property_type", branch.propertyType)
            putString("establishment_year", branch.establishmentYear)
            putString("total_rooms", branch.totalRooms)
            putString("room_types", branch.roomTypes)
            putString("city", branch.city)
            putString("city_id", branch.cityId)
            putString("locality", branch.locality)
            putString("locality_id", branch.localityId)
            putString("address", branch.address)
            putString("gst_pan", branch.gstPan)
            putString("branch_id", branch.id)
            putString("branch_tag", branch.branchTag)
            apply()
        }

        spCred.edit().apply {
            putString("Name", branch.name)
            putString("BranchId", branch.id)
            apply()
        }
    }

    /**
     * When user saves in EditHotelDetailsActivity, sync back to the currently selected branch.
     */
    fun syncHotelDetailsToSelectedBranch() {
        val spDetails = context.getSharedPreferences("hotel_details", Context.MODE_PRIVATE)
        val branches = getAllBranches().toMutableList()
        val selectedId = getSelectedBranchId()
        val index = branches.indexOfFirst { it.id == selectedId }

        if (index != -1) {
            val branch = branches[index]
            branch.name = spDetails.getString("hotel_name", branch.name) ?: branch.name
            branch.legalBusinessName = spDetails.getString("legal_business_name", branch.legalBusinessName) ?: branch.legalBusinessName
            branch.categoryStars = spDetails.getInt("hotel_category", branch.categoryStars)
            branch.propertyType = spDetails.getString("property_type", branch.propertyType) ?: branch.propertyType
            branch.establishmentYear = spDetails.getString("establishment_year", branch.establishmentYear) ?: branch.establishmentYear
            branch.totalRooms = spDetails.getString("total_rooms", branch.totalRooms) ?: branch.totalRooms
            branch.roomTypes = spDetails.getString("room_types", branch.roomTypes) ?: branch.roomTypes
            branch.city = spDetails.getString("city", branch.city) ?: branch.city
            branch.cityId = spDetails.getString("city_id", branch.cityId) ?: branch.cityId
            branch.locality = spDetails.getString("locality", branch.locality) ?: branch.locality
            branch.localityId = spDetails.getString("locality_id", branch.localityId) ?: branch.localityId
            branch.address = spDetails.getString("address", branch.address) ?: branch.address
            branch.gstPan = spDetails.getString("gst_pan", branch.gstPan) ?: branch.gstPan

            branches[index] = branch
            saveBranches(branches)
        }
    }

    fun addBranch(branch: HotelBranch) {
        val branches = getAllBranches().toMutableList()
        branches.add(branch)
        saveBranches(branches)
    }
}
