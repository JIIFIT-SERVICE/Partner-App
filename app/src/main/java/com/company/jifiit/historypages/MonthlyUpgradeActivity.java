package com.company.jifiit.historypages;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Constant;
import com.company.jifiit.R;
import com.company.jifiit.model.BookingHistoryModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MonthlyUpgradeActivity extends AppCompatActivity {

    String Id;
    ProgressBar loadingBar;
    CardView first,second;
    TextView bookingTxt,upgradeTxt,reviewTxt,hoursTxt,cash,instruction;

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_upgrade);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("More Insights");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);



        Id = getIntent().getStringExtra("id");
        bookingTxt = findViewById(R.id.booking);
        upgradeTxt = findViewById(R.id.upgrade);
        reviewTxt = findViewById(R.id.review);
        hoursTxt = findViewById(R.id.hours);
        cash = findViewById(R.id.cash);
        instruction = findViewById(R.id.txtAmount);
        historybtn(Id);

//        bookingTxt.setText("Your id is: "+ Id);

    }

    private void historybtn(String id) {
        loadingBar = findViewById(R.id.progress_bar);
        first = findViewById(R.id.firstCard);
        second = findViewById(R.id.secondCard);

        // Show the ProgressBar and hide other cards
        loadingBar.setVisibility(View.VISIBLE);


        // Construct the URL with query parameters
        String url = Constant.insights + id;

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        @SuppressLint({"SetTextI18n", "ResourceAsColor"}) JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        // Hide the ProgressBar and show the cards
                        loadingBar.setVisibility(View.GONE);

                        // Parse the "status" field from the response
                        String status = response.optString("status");
                        if ("success".equals(status)) {
                            Log.d("ResponseData", response.toString());

                            JSONObject data = response.optJSONObject("data");
                            if (data != null) {
                                String totalUpgrade = data.getString("Total_upgrade");
                                String totalIncentive = data.getString("totalIncentive");
                                String totalHours = data.getString("totalHours");
                                String displayMinutes = data.getString("displayMinutes");
                                String hoursIncentive = data.getString("HoursIncentive");
                                String orderCount = data.getString("orderCount");
                                String ratings = data.getString("ratings");
                                String total_price = data.getString("total_price");
                                String ratingPrice = data.getString("ratingPrice");

                                int price = Integer.parseInt(total_price);
                                this.bookingTxt.setText("Total Booking: " + orderCount);
                                this.upgradeTxt.setText("Total Upgrade: " + totalUpgrade + " ( ₹" + totalIncentive + " )");
                                this.hoursTxt.setText("Total Hours: " + totalHours + "hrs " + displayMinutes + "Mins" + "( ₹" + hoursIncentive + " )");
                                this.reviewTxt.setText("Total Review: " + ratings + " ( ₹" + ratingPrice + " )");
                                if (price > 0) {
                                    this.cash.setText("You have Total ₹" + total_price + " in Cash");
                                    cash.setVisibility(View.VISIBLE);
                                    instruction.setText("Submit the cash tomorrow before 2 pm.");
                                    instruction.setVisibility(View.VISIBLE);
                                    instruction.setTextColor(getResources().getColor(R.color.black)); // Correctly set color
                                } else {
                                    instruction.setVisibility(View.VISIBLE);
                                    cash.setVisibility(View.GONE);
                                    instruction.setText("Congratulations! You don't have any cash.");
                                    instruction.setTextColor(getResources().getColor(R.color.DeepGreen)); // Correctly set color
                                }

                            } else {
                                Toast.makeText(this, "No data found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        // Handle parsing errors
                        loadingBar.setVisibility(View.VISIBLE);
                        Log.e("ParsingError", "Error while parsing response", e);
                        Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Handle network errors
                    loadingBar.setVisibility(View.VISIBLE);

                    String errorMessage = "Network error. Please check your connection.";
                    if (error.networkResponse != null) {
                        errorMessage += " (Error code: " + error.networkResponse.statusCode + ")";
                    }

                    Log.e("VolleyError", errorMessage, error);
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();

                    // Optionally, show a retry mechanism
                    new AlertDialog.Builder(this)
                            .setTitle("Network Error")
                            .setMessage("Unable to fetch data. Do you want to retry?")
                            .setPositiveButton("Retry", (dialog, which) -> historybtn(id))
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .show();
                }
        );

        // Add the request to the queue
        requestQueue.add(jsonObjectRequest);
    }





    @Override
    public boolean onSupportNavigateUp () {
        onBackPressed();
        return true;
    }
}