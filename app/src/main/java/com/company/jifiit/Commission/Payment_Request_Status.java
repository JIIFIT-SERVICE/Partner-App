package com.company.jifiit.Commission;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Adapter.PaymentRequestAdapter;
import com.company.jifiit.Constant;
import com.company.jifiit.R;
import com.company.jifiit.model.PaymentRequestModel;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Payment_Request_Status extends AppCompatActivity {
    TextView notFound, balanceAmount, balTxt;
    TextView monthYearBtn, BackBtn, NextBtn;

    private Calendar calendar;
    private Calendar currentCalendar;

    BigDecimal DueAmnt = BigDecimal.ZERO;
    private Toolbar toolbar;
    BigInteger Id;

    RecyclerView paymentRecyclerView;
    TabLayout statusTabs;

    private PaymentRequestAdapter paymentRequestAdapter;
    private ArrayList<PaymentRequestModel> paymentRequestList = new ArrayList<>();
    private ArrayList<PaymentRequestModel> filteredList = new ArrayList<>();
    private ProgressBar progressBar;

    // Variables to track current filter
    private String currentFilter = "pending"; // Changed default to "pending"

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_request_status);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Payment Status");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setTint(getResources().getColor(android.R.color.black));
        }

        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String idString = sp.getString("Id", "");
        if (!idString.isEmpty()) {
            Id = new BigInteger(idString);
        }



        // Set default tab selection
        if (statusTabs != null) {
            statusTabs.getTabAt(0).select(); // Select Pending tab by default
        }
    }

    private void initializeViews() {
        balanceAmount = findViewById(R.id.balanceAmount);
        balTxt = findViewById(R.id.balTxt);
        notFound = findViewById(R.id.notFound);
        monthYearBtn = findViewById(R.id.monthYearBtn);
        BackBtn = findViewById(R.id.BackBtn);
        NextBtn = findViewById(R.id.NextBtn);
        paymentRecyclerView = findViewById(R.id.paymentRecyclerView);
        statusTabs = findViewById(R.id.statusTabs);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupTabListeners() {
        if (statusTabs == null) {
            Log.e("PaymentStatus", "statusTabs is null");
            return;
        }

        statusTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0: // Pending tab
                        currentFilter = "pending";
                        applyFilter();
                        break;
                    case 1: // Completed tab
                        currentFilter = "completed";
                        applyFilter();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Not needed
            }
        });
    }

    private void initializeCalendar() {
        calendar = Calendar.getInstance();
        currentCalendar = Calendar.getInstance();
    }

    private void setButtonListeners() {
        if (monthYearBtn != null) {
            monthYearBtn.setOnClickListener(v -> updateMonthYearButton());
        }

        if (BackBtn != null) {
            BackBtn.setOnClickListener(v -> {
                calendar.add(Calendar.MONTH, -1);
                updateMonthYearButton();
            });
        }

        if (NextBtn != null) {
            NextBtn.setOnClickListener(v -> {
                calendar.add(Calendar.MONTH, 1);
                updateMonthYearButton();
            });
        }
    }

    private void updateMonthYearButton() {
        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String idString = sp.getString("Id", "");
        if (idString.isEmpty()) {
            return;
        }

        try {
            BigInteger Id = new BigInteger(idString);
            int year = calendar.get(Calendar.YEAR);

            String monthName = new SimpleDateFormat("MMM", Locale.ENGLISH).format(calendar.getTime());
            String yearText = String.valueOf(year);

            SpannableString spannableString = new SpannableString(monthName + " " + yearText);
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, monthName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            if (monthYearBtn != null) {
                monthYearBtn.setText(spannableString);
            }

            FetchCommission(Id, spannableString);

            if (calendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                    calendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)) {
                if (NextBtn != null) NextBtn.setVisibility(View.GONE);
            } else {
                if (NextBtn != null) NextBtn.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error processing data", Toast.LENGTH_SHORT).show();
        }
    }

    private void FetchCommission(BigInteger id, SpannableString monthYear) {
        String url = Constant.fetchPaymentRequestStatus + id + "&monthYear=" + monthYear.toString();

        // Show loading state
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (notFound != null) {
            notFound.setVisibility(View.GONE);
        }
        if (paymentRecyclerView != null) {
            paymentRecyclerView.setVisibility(View.GONE);
        }

        // Clear existing data
        paymentRequestList.clear();
        filteredList.clear();
        if (paymentRequestAdapter != null) {
            paymentRequestAdapter.updateData(filteredList);
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    // Hide loading
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }

                    try {
                        boolean status = response.getBoolean("status");
                        String message = response.getString("message");

                        if (status) {
                            JSONArray dataArray = response.getJSONArray("data");

                            if (dataArray.length() > 0) {
                                // Parse the data
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject obj = dataArray.getJSONObject(i);

                                    String idStr = obj.getString("id");
                                    String requestedMoney = obj.getString("requested_money");
                                    String totalBooking = obj.getString("total_booking");
                                    String deductionForBed = obj.getString("deduction_for_bed");
                                    String lateFine = obj.getString("late_fine");
                                    String CanceledFine = obj.getString("canceled_booking_fine");
                                    String othersFine = obj.getString("othersfine");   // fixed key (was using canceled_booking_fine)
                                    String totalPaid = obj.getString("total_paid");
                                    String statusStr = obj.getString("status");
                                    String PlarfromFee = obj.getString("withdrawal_processing_fee");

                                    String fromOrderDate = obj.optString("from_order_date", "");
                                    String toOrderDate = obj.optString("to_order_date", "");

                                    String paymentRequestDate = obj.getString("payment_request_date");
                                    String updatedDate = obj.getString("updated");
                                    String receipt = obj.getString("receipt");

                                    // Format dates
                                    String startDate = formatDate(fromOrderDate);
                                    String endDate = formatDate(toOrderDate);
                                    String RequestDate = formatDate(paymentRequestDate);
                                    String updatedAt = formatDate(updatedDate);



                                    PaymentRequestModel model = new PaymentRequestModel(
                                            idStr,
                                            startDate,
                                            endDate,
                                            requestedMoney,
                                            totalBooking,
                                            deductionForBed,
                                            lateFine,
                                            CanceledFine,
                                            totalPaid,
                                            statusStr,
                                            receipt,
                                            othersFine,
                                            RequestDate,
                                            updatedAt,
                                            PlarfromFee

                                    );

                                    paymentRequestList.add(model);
                                }

                                // Initialize adapter if null
                                if (paymentRequestAdapter == null) {
                                    paymentRequestAdapter = new PaymentRequestAdapter(this, filteredList);
                                    if (paymentRecyclerView != null) {
                                        paymentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                                        paymentRecyclerView.setAdapter(paymentRequestAdapter);
                                    }
                                }

                                // Apply current filter
                                applyFilter();

                            } else {
                                // No data found
                                if (paymentRecyclerView != null) {
                                    paymentRecyclerView.setVisibility(View.GONE);
                                }
                                if (notFound != null) {
                                    notFound.setVisibility(View.VISIBLE);
                                    notFound.setText("No payment records found for " + monthYear.toString());
                                }
                                updateBalanceAmount(BigDecimal.ZERO, "pending");
                            }
                        } else {
                            // API returned status false
                            if (paymentRecyclerView != null) {
                                paymentRecyclerView.setVisibility(View.GONE);
                            }
                            if (notFound != null) {
                                notFound.setVisibility(View.VISIBLE);
                                notFound.setText(message != null ? message : "No data available");
                            }
                            updateBalanceAmount(BigDecimal.ZERO, "pending");
                        }

                    } catch (JSONException e) {
                        Log.e("PaymentStatus", "JSON Parsing error: " + e.getMessage());
                        showError("Error parsing data");
                    }
                },
                error -> {
                    // Hide loading on error
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }

                    Log.e("PaymentStatus", "Volley Error: " + error.getMessage());
                    showError("Network error: " + error.getMessage());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                3,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        queue.add(jsonObjectRequest);
    }

    private void applyFilter() {
        filteredList.clear();

        if (paymentRequestList.isEmpty()) {
            if (paymentRecyclerView != null) {
                paymentRecyclerView.setVisibility(View.GONE);
            }
            if (notFound != null) {
                notFound.setVisibility(View.VISIBLE);
                notFound.setText("No payment records found");
            }
            updateBalanceAmount(BigDecimal.ZERO, currentFilter);
            return;
        }

        for (PaymentRequestModel model : paymentRequestList) {
            String status = model.getStatus();

            switch (currentFilter) {
                case "pending":
                    if (status != null && status.equals("0") || status != null && status.equals("2")) {
                        filteredList.add(model);
                    }
                    break;
                case "completed":
                    if (status != null && status.equals("1")) {
                        filteredList.add(model);
                    }
                    break;
                default:
                    filteredList.add(model);
                    break;
            }
        }

        // Update adapter with filtered list
        if (paymentRequestAdapter != null) {
            paymentRequestAdapter.updateData(filteredList);
        }

        // Show/hide views based on filtered results
        if (filteredList.isEmpty()) {
            if (paymentRecyclerView != null) {
                paymentRecyclerView.setVisibility(View.GONE);
            }
            if (notFound != null) {
                notFound.setVisibility(View.VISIBLE);
                String filterText = currentFilter.equals("pending") ? "pending" : "completed";
                notFound.setText("No " + filterText + " payment records found");
            }
            updateBalanceAmount(BigDecimal.ZERO, currentFilter);
        } else {
            if (paymentRecyclerView != null) {
                paymentRecyclerView.setVisibility(View.VISIBLE);
            }
            if (notFound != null) {
                notFound.setVisibility(View.GONE);
            }
            // Calculate total based on current filter
            calculateTotalAmount();
        }
    }

    private void calculateTotalAmount() {
        BigDecimal totalAmount = BigDecimal.ZERO;
        int count = 0;

        for (PaymentRequestModel model : filteredList) {
            try {
                String totalPaid = model.getTotalPaid();
                if (totalPaid != null && !totalPaid.isEmpty()) {
                    BigDecimal amount = new BigDecimal(totalPaid);
                    totalAmount = totalAmount.add(amount);
                    count++;
                }
            } catch (NumberFormatException e) {
                // Skip invalid numbers
            }
        }

        updateBalanceAmount(totalAmount, currentFilter);

        Log.d("PaymentStatus", currentFilter + " Total: " + totalAmount + " (" + count + " items)");
    }

    private void updateBalanceAmount(BigDecimal amount, String filter) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

        if (balanceAmount != null) {
            balanceAmount.setText(currencyFormat.format(amount));

            // Set color based on filter
            if (filter.equals("pending")) {
                // Orange color for pending
                balanceAmount.setTextColor(getResources().getColor(R.color.orange));
            } else if (filter.equals("completed")) {
                // Green color for completed
                balanceAmount.setTextColor(getResources().getColor(R.color.Green));
            } else {
                // Default color (black/dark)
                balanceAmount.setTextColor(getResources().getColor(R.color.Red));
            }
        }

        if (balTxt != null) {
            String label = filter.equals("pending") ? "Total Pending" : "Total Completed";
            balTxt.setText(label);

            // Also set color for the label to match
            if (filter.equals("pending")) {
                balTxt.setTextColor(getResources().getColor(R.color.orange));
            } else if (filter.equals("completed")) {
                balTxt.setTextColor(getResources().getColor(R.color.Green));
            } else {
                balTxt.setTextColor(getResources().getColor(R.color.Red));
            }
        }
    }

    private void showError(String message) {
        if (notFound != null) {
            notFound.setVisibility(View.VISIBLE);
            notFound.setText(message);
        }
        if (paymentRecyclerView != null) {
            paymentRecyclerView.setVisibility(View.GONE);
        }
        updateBalanceAmount(BigDecimal.ZERO, currentFilter);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty() || dateString.equals("null")) {
            return "N/A";
        }

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);

            java.util.Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);

        } catch (Exception e) {
            Log.e("DateFormat", "Error parsing date: " + dateString, e);
            return dateString;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializeViews();
        setupTabListeners();
        initializeCalendar();
        updateMonthYearButton();
        setButtonListeners();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}