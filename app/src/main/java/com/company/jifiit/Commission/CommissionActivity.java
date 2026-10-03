package com.company.jifiit.Commission;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Adapter.CommissionAdapter;
import com.company.jifiit.Constant;
import com.company.jifiit.R;
import com.company.jifiit.model.CommissionModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class CommissionActivity extends AppCompatActivity {
    TextView monthYearBtn, BackBtn, NextBtn;
    TextView selectAllBtn;
    private Calendar calendar;
    private Calendar currentCalendar;

    private RecyclerView Commissionlist;
    private CommissionAdapter commissionAdapter;
    private ArrayList<CommissionModel> commissionList = new ArrayList<>();
    TextView notFound, balanceAmount, balTxt;
    Button withdrawBtn;
    BigInteger Id;
    BigDecimal DueAmnt = BigDecimal.ZERO;
    BigDecimal SelectedAmount = BigDecimal.ZERO; // Track selected amount
    private boolean isWithdrawMode = false;
    private Toolbar toolbar;

    // Global variables for last payment request
    private String lastPaymentRequestDate = null;
    private boolean isPaymentDateFetched = false;
    private static final int DAYS_LIMIT = 7; // 7 days restriction

    private ProgressBar progressBar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commission);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Earning");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setTint(getResources().getColor(android.R.color.black));
        }

        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String idString = sp.getString("Id", "");
        Id = new BigInteger(idString);

        monthYearBtn = findViewById(R.id.monthYearBtn);
        BackBtn = findViewById(R.id.BackBtn);
        NextBtn = findViewById(R.id.NextBtn);
        balanceAmount = findViewById(R.id.balanceAmount);
        notFound = findViewById(R.id.notFound);
        withdrawBtn = findViewById(R.id.withdrawBtn);
        selectAllBtn = findViewById(R.id.selectAllBtn);
        balTxt = findViewById(R.id.balTxt);
        progressBar = findViewById(R.id.progressBar);

        Commissionlist = findViewById(R.id.Commissionlist);
        Commissionlist.setLayoutManager(new LinearLayoutManager(this));
        commissionList = new ArrayList<>();
        commissionAdapter = new CommissionAdapter(this, commissionList);
        Commissionlist.setAdapter(commissionAdapter);

        // Set withdrawal button click listener
        withdrawBtn.setOnClickListener(v -> {
            // Check for 7-day restriction before entering withdrawal mode
            if (!isWithdrawMode) {
                if (isPaymentDateFetched && lastPaymentRequestDate != null) {
                    if (isWithinLast7Days(lastPaymentRequestDate)) {
                        showWithdrawalRestrictionDialog();
                        return;
                    }
                }
            }
            toggleWithdrawMode();
        });

        // Set Select All button click listener
        selectAllBtn.setOnClickListener(v -> handleSelectAll());
    }

    private void initializeCalendar() {
        calendar = Calendar.getInstance();
        currentCalendar = Calendar.getInstance();
    }

    private void setButtonListeners() {
        monthYearBtn.setOnClickListener(v -> updateMonthYearButton());

        BackBtn.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1);
            updateMonthYearButton();
        });

        NextBtn.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1);
            updateMonthYearButton();
        });
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

            // Exit selection mode when changing months
            if (isWithdrawMode) {
                toggleWithdrawMode();
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

    private void toggleWithdrawMode() {
        isWithdrawMode = !isWithdrawMode;

        if (isWithdrawMode) {
            // Enter selection mode
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setDisplayShowHomeEnabled(false);
            }
            // Hide navigation icon
            if (toolbar.getNavigationIcon() != null) {
                toolbar.getNavigationIcon().setVisible(false, false);
            }

            withdrawBtn.setText("Confirm");
            withdrawBtn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.orange));

            selectAllBtn.setVisibility(View.VISIBLE);
            monthYearBtn.setVisibility(View.GONE);
            BackBtn.setVisibility(View.GONE);
            NextBtn.setVisibility(View.GONE);
            selectAllBtn.setText("Select All");
            balTxt.setText("Withdrawal Amount");

            // Reset selected amount
            SelectedAmount = BigDecimal.ZERO;
            updateBalanceAmount();

            commissionAdapter.setSelectionMode(true);
            commissionAdapter.setOnItemSelectionListener((selectedCount, selectedIds) -> {
                if (selectedCount > 0) {
                    withdrawBtn.setText("Confirm (" + selectedCount + ")");
                } else {
                    withdrawBtn.setText("Confirm");
                }
                updateSelectedAmount();
                updateSelectAllButtonText();
            });
            Toast.makeText(this, "Select unpaid bookings to withdraw", Toast.LENGTH_LONG).show();
        } else {
            // Exit selection mode
            if (commissionAdapter.getSelectedCount() > 0) {
                confirmWithdrawal();
            } else {
                exitSelectionMode();
            }
        }
    }

    private void exitSelectionMode() {

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        // Show navigation icon
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setVisible(true, false);
        }
        commissionAdapter.setOnItemSelectionListener(null);
        commissionAdapter.setSelectionMode(false);
        balTxt.setText("Balance");
        withdrawBtn.setText("Withdraw");
        withdrawBtn.setBackgroundTintList(
                ContextCompat.getColorStateList(this, R.color.Green));

        selectAllBtn.setVisibility(View.GONE);
        monthYearBtn.setVisibility(View.VISIBLE);
        BackBtn.setVisibility(View.VISIBLE);
        NextBtn.setVisibility(View.VISIBLE);

        isWithdrawMode = false;

        // Reset selected amount and show total due
        SelectedAmount = BigDecimal.ZERO;
        updateBalanceAmount();
        updateMonthYearButton();
    }

    private void updateSelectedAmount() {
        List<String> selectedIds = commissionAdapter.getSelectedIds();
        SelectedAmount = BigDecimal.ZERO;

        for (CommissionModel model : commissionList) {
            if (selectedIds.contains(model.getBookingId())) {
                String netAmountStr = model.getNetAmount().replace("₹", "").trim();
                try {
                    SelectedAmount = SelectedAmount.add(new BigDecimal(netAmountStr));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        updateBalanceAmount();
    }

    private void handleSelectAll() {
        if (!isWithdrawMode) return;

        int unpaidCount = getUnpaidCount();
        int selectedCount = commissionAdapter.getSelectedCount();

        if (selectedCount == unpaidCount && unpaidCount > 0) {
            // All are selected - Deselect all
            commissionAdapter.clearSelection();
            selectAllBtn.setText("Select All");
        } else {
            // Select all unpaid items
            commissionAdapter.selectAll();
            selectAllBtn.setText("Deselect All");
        }

        updateSelectAllButtonText();
        updateSelectedAmount();
    }

    private void updateSelectAllButtonText() {
        if (!isWithdrawMode) return;

        int unpaidCount = getUnpaidCount();
        int selectedCount = commissionAdapter.getSelectedCount();

        if (unpaidCount == 0) {
            selectAllBtn.setText("No Items");
            selectAllBtn.setEnabled(false);
        } else if (selectedCount == unpaidCount) {
            selectAllBtn.setText("Deselect All");
            selectAllBtn.setEnabled(true);
        } else {
            selectAllBtn.setText("Select All (" + selectedCount + "/" + unpaidCount + ")");
            selectAllBtn.setEnabled(true);
        }
    }

    private int getUnpaidCount() {
        int count = 0;
        for (CommissionModel model : commissionList) {
            if (model.getTh_payment_status() == 0) {
                count++;
            }
        }
        return count;
    }

    private void confirmWithdrawal() {
        List<String> selectedIds = commissionAdapter.getSelectedIds();

        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "No bookings selected", Toast.LENGTH_SHORT).show();
            exitSelectionMode();
            return;
        }

        // Safety: recompute SelectedAmount right before confirmation
        updateSelectedAmount();

        if (SelectedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            Toast.makeText(this, "Selected amount is zero. Please select valid bookings.", Toast.LENGTH_LONG).show();
            return;
        }

        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirm Withdrawal")
                .setMessage("Are you sure you want to withdraw " + selectedIds.size() +
                        " booking(s) totaling ₹" + SelectedAmount.setScale(2, RoundingMode.HALF_UP).toPlainString() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    processWithdrawal(selectedIds);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    exitSelectionMode();
                })
                .show();
    }

    private void processWithdrawal(List<String> selectedIds) {
        String url = Constant.wthidrawlRqst;

        // Safety: make sure SelectedAmount reflects the current selection
        updateSelectedAmount();

        if (SelectedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            Toast.makeText(this, "Withdrawal amount is zero. Aborting.", Toast.LENGTH_LONG).show();
            exitSelectionMode();
            return;
        }

        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Processing withdrawal...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            JSONObject requestBody = new JSONObject();
            JSONArray idsArray = new JSONArray();
            for (String id : selectedIds) {
                idsArray.put(id);
            }
            requestBody.put("booking_ids", idsArray);

            requestBody.put("therapist_id", Id.toString());

            // ✅ FIX: BigDecimal is not a supported type by org.json.put().
            // Sending .doubleValue() ensures it serializes as a number.
            // Alternatively use toPlainString() to send as a string.
            double requestedAmount = SelectedAmount
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();

            requestBody.put("requested_amount", requestedAmount);
            requestBody.put("number_of_orders", selectedIds.size());

            // Get current device date and time
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String currentDateTime = sdf.format(new Date());

            // Add current date and time to request
            requestBody.put("payment_request_date", currentDateTime);

            // Optional: Add timezone information
            requestBody.put("timezone", TimeZone.getDefault().getID());

            // Debug: log the final payload
            Log.d("Withdrawal", "Request JSON: " + requestBody.toString());

            RequestQueue queue = Volley.newRequestQueue(this);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    requestBody,
                    response -> {
                        progressDialog.dismiss();

                        try {
                            if (response.has("Status") && response.getString("Status").equals("success")) {
                                Toast.makeText(this, "Withdrawal successful!", Toast.LENGTH_LONG).show();
                                // Fetch updated last request date
                                fetchLastRequestDateTime(Id);
                            } else {
                                String message = response.has("message") ? response.getString("message") : "Withdrawal failed";
                                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error processing response", Toast.LENGTH_SHORT).show();
                        }
                        exitSelectionMode();
                    },
                    error -> {
                        progressDialog.dismiss();

                        // Get the raw response body
                        if (error.networkResponse != null) {
                            try {
                                String responseBody = new String(error.networkResponse.data, "UTF-8");

                                // Try to extract JSON even if there's HTML
                                if (responseBody.contains("{") && responseBody.contains("}")) {
                                    int start = responseBody.indexOf("{");
                                    int end = responseBody.lastIndexOf("}") + 1;
                                    if (start >= 0 && end > start) {
                                        String jsonPart = responseBody.substring(start, end);
                                        try {
                                            JSONObject jsonResponse = new JSONObject(jsonPart);
                                            String message = jsonResponse.has("message") ?
                                                    jsonResponse.getString("message") : "Unknown error";
                                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                                            exitSelectionMode();
                                            return;
                                        } catch (JSONException e) {
                                            // Fall through
                                        }
                                    }
                                }

                                String errorMessage = "Server error: " + error.networkResponse.statusCode;
                                if (responseBody.length() < 100) {
                                    errorMessage = responseBody;
                                }
                                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();

                            } catch (Exception e) {
                                Toast.makeText(this, "Error processing response", Toast.LENGTH_SHORT).show();
                            }
                        } else if (error.getMessage() != null) {
                            Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show();
                        }

                        exitSelectionMode();
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Accept", "application/json");
                    return headers;
                }

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };

            request.setRetryPolicy(new DefaultRetryPolicy(
                    30000, // 30 seconds timeout
                    3, // 3 retries
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            Log.d("Withdrawal", "Adding request to queue");
            queue.add(request);

        } catch (JSONException e) {
            progressDialog.dismiss();
            Log.e("Withdrawal", "JSON Exception: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            progressDialog.dismiss();
            Log.e("Withdrawal", "Unexpected error: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Unexpected error occurred", Toast.LENGTH_SHORT).show();
        }
    }

    private void FetchCommission(BigInteger id, SpannableString monthYear) {
        String url = Constant.fetchCommission + id + "&monthYear=" + monthYear.toString();
        DueAmnt = BigDecimal.ZERO;
        SelectedAmount = BigDecimal.ZERO;

        // Show loading
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (Commissionlist != null) {
            Commissionlist.setVisibility(View.GONE);
        }
        if (notFound != null) {
            notFound.setVisibility(View.GONE);
        }

        RequestQueue requestQueue = Volley.newRequestQueue(this);

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
                        if (response.has("Status")) {
                            String status = response.getString("Status");

                            if (status.equalsIgnoreCase("error")) {
                                showNotFoundView();
                                return;
                            }

                            if (!status.equalsIgnoreCase("success")) {
                                showNotFoundView();
                                return;
                            }
                        }

                        if (!response.has("bookings")) {
                            Toast.makeText(this, "No commission data found", Toast.LENGTH_SHORT).show();
                            commissionList.clear();
                            if (commissionAdapter != null) {
                                commissionAdapter.notifyDataSetChanged();
                            }
                            showNotFoundView();
                            return;
                        }

                        JSONArray dataArray = response.getJSONArray("bookings");
                        commissionList.clear();

                        if (dataArray.length() > 0) {
                            if (Commissionlist != null) {
                                Commissionlist.setVisibility(View.VISIBLE);
                            }
                            if (notFound != null) {
                                notFound.setVisibility(View.GONE);
                            }
                        } else {
                            showNotFoundView();
                            return;
                        }

                        DueAmnt = BigDecimal.ZERO;
                        SelectedAmount = BigDecimal.ZERO;

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject obj = dataArray.getJSONObject(i);

                            String bookingId = obj.getString("id");
                            String customerName = obj.getString("customer_name");
                            String massageName = obj.getString("massage_name");
                            String bookingDate = obj.getString("order_date");
                            String bookingTime = obj.getString("start_time");
                            String bookingType = obj.getString("bookingType");

                            BigDecimal product_cost = new BigDecimal(obj.getString("product_cost"));
                            Integer th_payment_status = obj.getInt("th_payment_status");

                            BigDecimal bookingAmount = new BigDecimal(obj.getString("booking_Amont"));
                            BigDecimal commissionAmount = new BigDecimal(obj.getString("commission"));
                            BigDecimal gstAmount = new BigDecimal(obj.getString("gst"));
                            BigDecimal customerPaidAmount = new BigDecimal(obj.getString("customer_paid"));

                            BigDecimal netAmount = customerPaidAmount
                                    .subtract(commissionAmount)
                                    .subtract(gstAmount)
                                    .subtract(product_cost);

                            if (th_payment_status == 0) {
                                DueAmnt = DueAmnt.add(netAmount);
                            }

                            // Format amounts with ₹ symbol and 2 decimal places
                            String bookingAmountStr = "₹" + bookingAmount.setScale(2, RoundingMode.HALF_UP);
                            String commissionAmountStr = "₹" + commissionAmount.setScale(2, RoundingMode.HALF_UP);
                            String gstAmountStr = "₹" + gstAmount.setScale(2, RoundingMode.HALF_UP);
                            String netAmountStr = "₹" + netAmount.setScale(2, RoundingMode.HALF_UP);
                            String customerPaidStr = "₹" + customerPaidAmount.setScale(2, RoundingMode.HALF_UP);
                            String product_costStr = "₹" + product_cost.setScale(2, RoundingMode.HALF_UP);

                            CommissionModel model = new CommissionModel(
                                    bookingId,
                                    bookingType,
                                    bookingDate,
                                    bookingTime,
                                    customerName,
                                    massageName,
                                    bookingAmountStr,
                                    commissionAmountStr,
                                    gstAmountStr,
                                    netAmountStr,
                                    customerPaidStr,
                                    product_costStr,
                                    th_payment_status
                            );

                            commissionList.add(model);
                        }

                        updateBalanceAmount();

                        if (commissionAdapter != null) {
                            commissionAdapter.notifyDataSetChanged();
                        }

                    } catch (JSONException e) {
                        Log.e("JSON_ERROR", "Error: " + e.getMessage(), e);
                        Toast.makeText(this, "Data parsing error", Toast.LENGTH_SHORT).show();
                        showNotFoundView();
                    } catch (Exception e) {
                        Log.e("FETCH_ERROR", "Error: " + e.getMessage(), e);
                        Toast.makeText(this, "Error processing data", Toast.LENGTH_SHORT).show();
                        showNotFoundView();
                    }
                },
                error -> {
                    // Hide loading on error
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }

                    Log.e("VOLLEY_ERROR", "Network error: " + error.toString(), error);
                    Toast.makeText(this, "Network error. Please check connection.", Toast.LENGTH_SHORT).show();
                    showNotFoundView();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsonObjectRequest);
    }

    private void showNotFoundView() {
        if (Commissionlist != null) {
            Commissionlist.setVisibility(View.GONE);
        }
        if (notFound != null) {
            notFound.setVisibility(View.VISIBLE);
        }
        commissionList.clear();
        if (commissionAdapter != null) {
            commissionAdapter.notifyDataSetChanged();
        }
        DueAmnt = BigDecimal.ZERO;
        SelectedAmount = BigDecimal.ZERO;
        updateBalanceAmount();
    }

    private void updateBalanceAmount() {
        if (balanceAmount != null) {
            String formattedAmount;
            if (isWithdrawMode) {
                // Show selected amount in withdrawal mode
                formattedAmount = "₹" + SelectedAmount.setScale(2, RoundingMode.HALF_UP).toPlainString();
                // Change color to orange when in selection mode
                balanceAmount.setTextColor(ContextCompat.getColor(this, R.color.orange));
            } else {
                // Show total due amount in normal mode
                formattedAmount = "₹" + DueAmnt.setScale(2, RoundingMode.HALF_UP).toPlainString();
                balanceAmount.setTextColor(ContextCompat.getColor(this, R.color.Green));
            }
            balanceAmount.setText(formattedAmount);
        }
    }

    /**
     * Check if the given date is within the last 7 days
     * @param dateString Date string in format "yyyy-MM-dd HH:mm:ss"
     * @return true if within 7 days, false otherwise
     */
    private boolean isWithinLast7Days(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date lastRequestDate = sdf.parse(dateString);
            Date currentDate = new Date();

            // Calculate difference in milliseconds
            long diffInMillies = currentDate.getTime() - lastRequestDate.getTime();
            long diffInDays = diffInMillies / (24 * 60 * 60 * 1000);

            Log.d("PaymentDate", "Days since last request: " + diffInDays);

            // Return true if within 7 days (including today)
            return diffInDays < DAYS_LIMIT;

        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Show dialog for withdrawal restriction
     */
    private void showWithdrawalRestrictionDialog() {
        String formattedDate = formatDateForDisplay(lastPaymentRequestDate);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Withdrawal Restriction")
                .setMessage("Sorry, you have already requested a withdrawal on " + formattedDate +
                        ".\n\nYou can request again after " + getNextAvailableDate() + ".")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Format date for display
     */
    private String formatDateForDisplay(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateString;
        }
    }

    /**
     * Calculate the next available date (after 7 days)
     */
    private String getNextAvailableDate() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date lastDate = sdf.parse(lastPaymentRequestDate);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(lastDate);
            calendar.add(Calendar.DAY_OF_YEAR, DAYS_LIMIT);

            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            return outputFormat.format(calendar.getTime());

        } catch (ParseException e) {
            e.printStackTrace();
            return "7 days from your last request";
        }
    }

    /**
     * Fetch the last payment request date from server
     */
    private void fetchLastRequestDateTime(BigInteger therapistId) {
        String url = Constant.fetchLastRequest + therapistId;

        Log.d("PaymentDate", "URL: " + url);

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    Log.d("PaymentDate", "Response: " + response);

                    try {
                        JSONObject json = new JSONObject(response);

                        if (json.getString("Status").equals("success")) {
                            lastPaymentRequestDate = json.getString("payment_request_date");
                            isPaymentDateFetched = true;

                            Log.d("PaymentDate", "Last request date: " + lastPaymentRequestDate);

                            // Check if within 7 days and update UI accordingly
                            if (isWithinLast7Days(lastPaymentRequestDate)) {
                                // Show warning on button
                                String formattedDate = formatDateForDisplay(lastPaymentRequestDate);
                                withdrawBtn.setText("Requested on\n " + formattedDate);
                                withdrawBtn.setEnabled(false);
                                withdrawBtn.setBackgroundTintList(
                                        ContextCompat.getColorStateList(this, android.R.color.darker_gray));
                            } else {
                                withdrawBtn.setText("Withdraw");
                                withdrawBtn.setEnabled(true);
                                withdrawBtn.setBackgroundTintList(
                                        ContextCompat.getColorStateList(this, R.color.Green));
                            }

                        } else {
                            // No previous request found - allow withdrawal
                            lastPaymentRequestDate = null;
                            isPaymentDateFetched = true;
                            withdrawBtn.setText("Withdraw");
                            withdrawBtn.setEnabled(true);
                            withdrawBtn.setBackgroundTintList(
                                    ContextCompat.getColorStateList(this, R.color.Green));

                            Log.d("PaymentDate", "No previous payment request found");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("PaymentDate", "JSON Error: " + e.getMessage());
                        // On error, still allow withdrawal but with a warning
                        isPaymentDateFetched = true;
                        withdrawBtn.setText("Withdraw");
                        withdrawBtn.setEnabled(true);
                        withdrawBtn.setBackgroundTintList(
                                ContextCompat.getColorStateList(this, R.color.Green));
                    }
                },
                error -> {
                    Log.e("PaymentDate", "Network Error: " + error.toString());
                    // On network error, still allow withdrawal but with a warning
                    isPaymentDateFetched = true;
                    withdrawBtn.setText("Withdraw");
                    withdrawBtn.setEnabled(true);
                    withdrawBtn.setBackgroundTintList(
                            ContextCompat.getColorStateList(this, R.color.Green));
                    Toast.makeText(this, "Could not check withdrawal history", Toast.LENGTH_SHORT).show();
                });

        queue.add(request);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.commission_menue, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_commission_chart) {
            Intent intent = new Intent(CommissionActivity.this, CommissionChart.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.see_request_status) {
            Intent intent = new Intent(CommissionActivity.this, Payment_Request_Status.class);
            startActivity(intent);
            return true;
        }

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isWithdrawMode) {
            exitSelectionMode();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializeCalendar();
        updateMonthYearButton();
        setButtonListeners();
        // Fetch last request date on resume
        fetchLastRequestDateTime(Id);
    }
}