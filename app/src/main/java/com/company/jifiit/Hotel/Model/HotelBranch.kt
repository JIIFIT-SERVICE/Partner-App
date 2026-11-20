package com.company.jifiit.Hotel.Model

import org.json.JSONObject

data class HotelBranch(
    val id: String,
    var name: String,
    var branchTag: String,
    var legalBusinessName: String = "Grand Orchid Hospitality Pvt Ltd",
    var categoryStars: Int = 4,
    var propertyType: String = "Hotel",
    var establishmentYear: String = "2018",
    var totalRooms: String = "50",
    var roomTypes: String = "Deluxe Room,Executive Suite,Club Room",
    var city: String = "Gurgaon",
    var cityId: String = "1",
    var locality: String = "Sector 14",
    var localityId: String = "101",
    var address: String = "Plot 42, DLF Phase 2, Near MG Road, Gurgaon, Haryana - 122002",
    var gstPan: String = "06AAACG1234F1Z8",
    var phone: String = "+91 98765 43210",
    var email: String = "sector14@grandorchid.com",
    var initials: String = "GOH",
    var partnerId: String = "Official Partner JIIFIT LLP"
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id)
        json.put("name", name)
        json.put("branchTag", branchTag)
        json.put("legalBusinessName", legalBusinessName)
        json.put("categoryStars", categoryStars)
        json.put("propertyType", propertyType)
        json.put("establishmentYear", establishmentYear)
        json.put("totalRooms", totalRooms)
        json.put("roomTypes", roomTypes)
        json.put("city", city)
        json.put("cityId", cityId)
        json.put("locality", locality)
        json.put("localityId", localityId)
        json.put("address", address)
        json.put("gstPan", gstPan)
        json.put("phone", phone)
        json.put("email", email)
        json.put("initials", initials)
        json.put("partnerId", partnerId)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): HotelBranch {
            return HotelBranch(
                id = json.optString("id", "101"),
                name = json.optString("name", "Grand Orchid Hotel"),
                branchTag = json.optString("branchTag", "Main Branch"),
                legalBusinessName = json.optString("legalBusinessName", "Grand Orchid Hospitality Pvt Ltd"),
                categoryStars = json.optInt("categoryStars", 4),
                propertyType = json.optString("propertyType", "Hotel"),
                establishmentYear = json.optString("establishmentYear", "2018"),
                totalRooms = json.optString("totalRooms", "50"),
                roomTypes = json.optString("roomTypes", "Deluxe Room,Executive Suite,Club Room"),
                city = json.optString("city", "Gurgaon"),
                cityId = json.optString("cityId", "1"),
                locality = json.optString("locality", "Sector 14"),
                localityId = json.optString("localityId", "101"),
                address = json.optString("address", "Plot 42, DLF Phase 2, Near MG Road, Gurgaon, Haryana - 122002"),
                gstPan = json.optString("gstPan", "06AAACG1234F1Z8"),
                phone = json.optString("phone", "+91 98765 43210"),
                email = json.optString("email", "info@grandorchid.com"),
                initials = json.optString("initials", "GOH"),
                partnerId = json.optString("partnerId", "Official Partner JIIFIT LLP")
            )
        }
    }
}
