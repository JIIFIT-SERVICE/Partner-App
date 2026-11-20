package com.company.jifiit.Hotel.HelperFunctions

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.company.jifiit.Constant
import java.math.BigInteger

object HotelCommissionHelper {

    fun fetchCommissionCharge(
        context: Context,
        hotelRegistrationId: BigInteger,
        onSuccess: (Double) -> Unit,
        onError: (String) -> Unit
    ) {
        if (hotelRegistrationId <= BigInteger.ZERO) {
            onError("Invalid hotel_registration_id")
            return
        }

        val url =
            Constant.GET_COMMISSION_CHARGE + hotelRegistrationId

        val request = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->

                try {
                    if (response.optBoolean("status", false)) {

                        val data = response.optJSONArray("data")

                        if (data != null && data.length() > 0) {

                            val charge = data
                                .getJSONObject(0)
                                .optDouble("commission_charge", 0.0)

                            Log.d(
                                "CommissionAPI",
                                "Commission charge: $charge"
                            )

                            onSuccess(charge)

                        } else {
                            onError("No commission data found")
                        }

                    } else {
                        onError(
                            response.optString(
                                "message",
                                "API returned false status"
                            )
                        )
                    }

                } catch (e: Exception) {
                    Log.e("CommissionAPI", "JSON parsing error", e)
                    onError("JSON parsing error: ${e.message}")
                }
            },
            { error ->
                Log.e("CommissionAPI", "API request failed", error)
                onError(error.message ?: "Network request failed")
            }
        )

        Volley.newRequestQueue(context.applicationContext)
            .add(request)
    }
}