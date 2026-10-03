package com.company.jifiit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class CodeActivity extends AppCompatActivity {
    EditText enter_otp;
    TextView lable;
    Button verify;
    String fetchingOTP,Id ,name;
    String startTime,endTime;
    BigInteger therapistId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Otp verify");
        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String idString = sp.getString("Id", "");
        therapistId = new BigInteger(idString);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Id = getIntent().getStringExtra("id");
        startTime = getIntent().getStringExtra("start_time");
        endTime = getIntent().getStringExtra("end_time");
        name = getIntent().getStringExtra("customer_name");
        fetchOtp(Id,therapistId);
        enter_otp = findViewById(R.id.otpEditText);
        verify = findViewById(R.id.verifyButton);



    }

    private void fetchOtp(String id, BigInteger therapistId) {
        String url = Constant.fetchBookingOtp + id; // Assuming Constant.fetchBookingOtp is the base URL

        // Create a RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);

        // Create a StringRequest to fetch the response from the server
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Parse the JSON response
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");

                            if (status.equals("success")) {
                                // Extract the booking_otp from the JSON response
                                fetchingOTP = jsonObject.getString("booking_otp");
                                verify.setOnClickListener(v -> {
                                    // Get the value entered by the user in the OTP field
                                    String enteredOtp = enter_otp.getText().toString().trim();

                                    // Compare the entered OTP with the fetched OTP
                                    if (enteredOtp.equals(fetchingOTP)) {
                                        // OTP is correct, open a new activity
//                                        Intent intent = new Intent(CodeActivity.this, MassageWorkingActivity.class);
                                        Intent intent = new Intent(CodeActivity.this, PhotoUpload.class);
                                        intent.putExtra("id",Id);
                                        intent.putExtra("start_time",startTime);
                                        intent.putExtra("end_time",endTime);
                                        intent.putExtra("customer_name",name);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                        otpstatusUpdate(id,therapistId);
                                        startActivity(intent);// Replace NewActivity with the actual activity you want to open
                                    } else {
                                        // OTP is incorrect, show an error message
                                        Toast.makeText(CodeActivity.this, "Please enter the correct code", Toast.LENGTH_LONG).show();
                                    }
                                });

                            } else {
                                // Handle the error, no booking found
                                String message = jsonObject.getString("message");
                                Toast.makeText(CodeActivity.this, message, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(CodeActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error response from the server
                        Toast.makeText(CodeActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                    }
                });

        // Add the request to the RequestQueue
        queue.add(stringRequest);
    }

    private void otpstatusUpdate(String id, BigInteger therapistId) {
        String url = Constant.otp_status_update;
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    try {
                        // First check if response is valid JSON
                        if (response.trim().startsWith("{") || response.trim().startsWith("[")) {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");

                            if ("success".equals(status)) {
                                Log.d("OTP Status Update", "OTP status updated successfully!");
                            } else {
                                String message = jsonResponse.optString("message", "Unknown error");
                                Toast.makeText(this, "Failed: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Handle non-JSON response
                            Log.e("OTP Status Update", "Non-JSON response: " + response);
                            Toast.makeText(this, "Server returned invalid response", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("OTP Status Update", "JSON Parsing error: " + e.getMessage());
                        Toast.makeText(this, "Error processing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    String errorMessage = "Network error";
                    if (error.networkResponse != null) {
                        try {
                            errorMessage = new String(error.networkResponse.data, "UTF-8");
                            // Try to parse as JSON even for errors
                            if (errorMessage.trim().startsWith("{")) {
                                JSONObject errorJson = new JSONObject(errorMessage);
                                errorMessage = errorJson.optString("message", errorMessage);
                            }
                        } catch (Exception e) {
                            errorMessage = error.getMessage();
                        }
                    }
                    Toast.makeText(this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                params.put("therapist_id", String.valueOf(therapistId));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }
        };

        // Set timeout and retry policy
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000, // 10 seconds timeout
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(stringRequest);
    }

    @Override
    public boolean onSupportNavigateUp () {
        onBackPressed();
        return true;
    }
}