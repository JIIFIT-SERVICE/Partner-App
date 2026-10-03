package com.company.jifiit.Hotel

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.company.jifiit.Constant
import com.company.jifiit.Hotel.Adapter.ExpertAdapter
import com.company.jifiit.Hotel.Model.ExpertModel
import com.company.jifiit.LocalStorage.HotelDatabaseHelper
import com.company.jifiit.R
import com.google.android.material.button.MaterialButton
import org.json.JSONObject
import java.math.BigInteger

class Expert_activity : AppCompatActivity() {

    // =====================================================
    // VIEWS
    // =====================================================

    private lateinit var btnBack: ImageView
    private lateinit var rvExperts: RecyclerView
    private lateinit var btnContinue: MaterialButton

    // =====================================================
    // EXPERT LIST
    // =====================================================

    private val expertList = ArrayList<ExpertModel>()

    private lateinit var expertAdapter: ExpertAdapter

    // =====================================================
    // SELECTED EXPERT
    // =====================================================

    private var selectedExpert: ExpertModel? = null

    private var selectedExpertId: String? = null

    // =====================================================
    // API URL
    // =====================================================

    private lateinit var Id: BigInteger

    // =====================================================
    // ON CREATE
    // =====================================================

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

        setContentView(R.layout.activity_therist)

        // =================================================
        // WINDOW INSETS
        // =================================================

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v, insets ->

            val systemBars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            val ime =
                insets.getInsets(
                    WindowInsetsCompat.Type.ime()
                )

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                maxOf(
                    systemBars.bottom,
                    ime.bottom
                )
            )

            insets
        }

        // =================================================
        // INITIALIZE VIEWS
        // =================================================

        btnBack = findViewById(R.id.btnBack)

        rvExperts = findViewById(R.id.rvExperts)

        btnContinue = findViewById(R.id.btnContinue)

        // =================================================
        // BACK BUTTON
        // =================================================

        btnBack.setOnClickListener {

            onBackPressedDispatcher.onBackPressed()

            overridePendingTransition(
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
        }

        // =================================================
        // RECYCLER VIEW
        // =================================================

        rvExperts.layoutManager =
            GridLayoutManager(
                this,
                2
            )

        rvExperts.setHasFixedSize(false)

        rvExperts.isNestedScrollingEnabled = true

        // =================================================
        // ADAPTER
        // =================================================

        expertAdapter = ExpertAdapter(
            expertList
        ) { expert ->

            selectExpert(expert)

        }

        rvExperts.adapter = expertAdapter

        // =================================================
        // CONTINUE BUTTON
        // =================================================

        btnContinue.setOnClickListener {

            continueToPayment()

        }

        // =================================================
        // LOAD EXPERTS FROM API
        // =================================================

        loadExperts()
    }


    // =====================================================
    // LOAD EXPERTS FROM API
    // =====================================================

    private fun loadExperts() {

        val spCred = getSharedPreferences("credentials", MODE_PRIVATE)
        val idString = spCred.getString("Id", "0") ?: "0"
        Id = try {
            BigInteger(idString)
        } catch (e: NumberFormatException) {
            BigInteger.ZERO
        }
        // -------------------------------------------------
        // CLEAR OLD DATA
        // -------------------------------------------------

        expertList.clear()
        expertAdapter.notifyDataSetChanged()

        // -------------------------------------------------
        // SHOW LOADING
        // -------------------------------------------------

        btnContinue.isEnabled = false

        val dbHelper = HotelDatabaseHelper(this)
        val totalMassageDuration = dbHelper.getTotalMassageDuration()


        val needBed =  intent.getIntExtra("need_bed", 0)

        val bookingDate =  intent.getStringExtra("booking_date") ?: ""

        val bookingTime =intent.getStringExtra("booking_time") ?: ""

        val clientGender =intent.getStringExtra("client_gender") ?: ""
        val SELECTED_SERVICE =intent.getStringExtra("SELECTED_SERVICE") ?: ""

        // -------------------------------------------------
        // CHECK REQUIRED DATA
        // -------------------------------------------------

        if ( totalMassageDuration <= 0 || bookingDate.isEmpty() || bookingTime.isEmpty() || clientGender.isEmpty() || SELECTED_SERVICE.isEmpty()) {

            btnContinue.isEnabled = false

            Toast.makeText(this,"Booking information is missing",Toast.LENGTH_LONG).show()

            return
        }


        // -------------------------------------------------
        // CREATE JSON
        // -------------------------------------------------

        val jsonBody = JSONObject()

        jsonBody.put("totalMassageDuration",totalMassageDuration)

        jsonBody.put("hotel_id",Id)

        jsonBody.put("need_bed", needBed)

        jsonBody.put("booking_date",bookingDate)

        jsonBody.put("booking_time",bookingTime)

        jsonBody.put( "client_gender",clientGender)



        // -------------------------------------------------
        // DEBUG
        // -------------------------------------------------

        android.util.Log.d("EXPERT_API","Request: $jsonBody")


        // -------------------------------------------------
        // VOLLEY REQUEST
        // -------------------------------------------------

        val request = JsonObjectRequest(Request.Method.POST, Constant.EXPERT_API, jsonBody,{
            response ->
                android.util.Log.d("EXPERT_API","Response: $response")
                parseExpertResponse(response)

            },

            { error ->

                btnContinue.isEnabled = false

                android.util.Log.e(
                    "EXPERT_API",
                    "Volley Error",
                    error
                )

                val statusCode =
                    error.networkResponse?.statusCode

                val responseBody =
                    error.networkResponse?.data?.let {
                        String(it, Charsets.UTF_8)
                    }

                android.util.Log.e(
                    "EXPERT_API",
                    "HTTP Status: $statusCode"
                )

                android.util.Log.e(
                    "EXPERT_API",
                    "Response Body: $responseBody"
                )

                val message =
                    if (!responseBody.isNullOrEmpty()) {
                        responseBody
                    } else {
                        "Server error: $statusCode"
                    }

                Toast.makeText(
                    this,
                    message,
                    Toast.LENGTH_LONG
                ).show()
            }
        )

        // -------------------------------------------------
        // REQUEST TIMEOUT
        // -------------------------------------------------

        request.retryPolicy = com.android.volley.DefaultRetryPolicy(30000, 1, 1.0f)

        // -------------------------------------------------
        // ADD REQUEST
        // -------------------------------------------------

        Volley.newRequestQueue(this).add(request)
    }


    // =====================================================
    // PARSE EXPERT RESPONSE
    // =====================================================

    private fun parseExpertResponse(response: JSONObject) {

        try {

            val success =response.optBoolean("success",false)

            if (!success) {

                btnContinue.isEnabled = false

                val errorMessage =  response.optString("error", "No available therapists found")

                Toast.makeText(this,errorMessage, Toast.LENGTH_LONG).show()

                return
            }


            // -------------------------------------------------
            // GET THERAPISTS ARRAY
            // -------------------------------------------------

            val therapists = response.optJSONArray("therapists")


            if (therapists == null || therapists.length() == 0) {

                btnContinue.isEnabled = false

                Toast.makeText(this,"No available experts found",Toast.LENGTH_LONG).show()

                return
            }


            // -------------------------------------------------
            // CLEAR EXISTING LIST
            // -------------------------------------------------

            expertList.clear()


            // -------------------------------------------------
            // LOOP THROUGH THERAPISTS
            // -------------------------------------------------

            for (i in 0 until therapists.length()) {

                val therapist = therapists.getJSONObject(i)


                // -------------------------------------------------
                // ID
                // -------------------------------------------------

                val id = therapist.optString("id","")


                // -------------------------------------------------
                // NAME
                // -------------------------------------------------

                val name =
                    therapist.optString(
                        "name",
                        ""
                    )


                // -------------------------------------------------
                // EMPLOYEE ID
                // -------------------------------------------------
                //
                // You said $id itself is employee ID.
                //

                val employeeId = id


                // -------------------------------------------------
                // EXPERIENCE
                // -------------------------------------------------

                val experience =
                    therapist.optString(
                        "experience",
                        "0 years"
                    )


                // -------------------------------------------------
                // IMAGE
                // -------------------------------------------------

                val image =
                    therapist.optString(
                        "profile_photo",
                        ""
                    )


                // -------------------------------------------------
                // CREATE MODEL
                // -------------------------------------------------

                val expert =
                    ExpertModel(
                        id = id,
                        name = name,
                        employeeId = employeeId,
                        experience = experience,
                        image = image
                    )


                // -------------------------------------------------
                // ADD TO LIST
                // -------------------------------------------------

                expertList.add(
                    expert
                )
            }


            // -------------------------------------------------
            // UPDATE RECYCLER VIEW
            // -------------------------------------------------

            expertAdapter.notifyDataSetChanged()


            // -------------------------------------------------
            // ENABLE CONTINUE
            // -------------------------------------------------

            btnContinue.isEnabled =
                expertList.isNotEmpty()


        } catch (e: Exception) {

            e.printStackTrace()

            btnContinue.isEnabled = false

            Toast.makeText(
                this,
                "Failed to process expert data",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    // =====================================================
    // SELECT EXPERT
    // =====================================================

    private fun selectExpert(
        expert: ExpertModel
    ) {

        // -------------------------------------------------
        // STORE COMPLETE EXPERT
        // -------------------------------------------------

        selectedExpert = expert

        // -------------------------------------------------
        // STORE EXPERT ID
        // -------------------------------------------------

        selectedExpertId = expert.id

        // -------------------------------------------------
        // DEBUG
        // -------------------------------------------------

        android.util.Log.d(
            "SELECTED_EXPERT",
            "Expert ID: ${expert.id}"
        )

        android.util.Log.d(
            "SELECTED_EXPERT",
            "Employee ID: ${expert.employeeId}"
        )

        // -------------------------------------------------
        // TOAST
        // -------------------------------------------------

        Toast.makeText(
            this,
            "${expert.name} selected",
            Toast.LENGTH_SHORT
        ).show()
    }


    // =====================================================
    // CONTINUE
    // =====================================================

    private fun continueToPayment() {

        // -------------------------------------------------
        // CHECK SELECTION
        // -------------------------------------------------

        if (selectedExpert == null) {

            Toast.makeText(this,"Please select an expert first", Toast.LENGTH_SHORT ).show()

            return
        }


        // -------------------------------------------------
        // GET SELECTED EXPERT
        // -------------------------------------------------

        val expert =   selectedExpert!!

        val expertId =  selectedExpertId ?: ""


        // -------------------------------------------------
        // DEBUG
        // -------------------------------------------------

        android.util.Log.d("SELECTED_EXPERT","Expert ID: $expertId")

        android.util.Log.d("SELECTED_EXPERT","Name: ${expert.name}")


        // -------------------------------------------------
        // TEST
        // -------------------------------------------------

        Toast.makeText(this,"Expert ID: $expertId", Toast.LENGTH_SHORT).show()


        // -------------------------------------------------
        // NEXT ACTIVITY
        // -------------------------------------------------

        /*
        val intent = Intent(
            this,
            PaymentActivity::class.java
        )

        intent.putExtra(
            "EXPERT_ID",
            expertId
        )

        intent.putExtra(
            "EXPERT_NAME",
            expert.name
        )

        intent.putExtra(
            "EMPLOYEE_ID",
            expert.employeeId
        )

        startActivity(intent)

        overridePendingTransition(
            R.anim.slide_in_right,
            R.anim.slide_out_left
        )
        */
    }


    // =====================================================
    // BACK PRESS
    // =====================================================

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {

        super.onBackPressed()

        overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
    }
}