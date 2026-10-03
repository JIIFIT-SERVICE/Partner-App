package com.company.jifiit.Leave;

import static androidx.fragment.app.FragmentManager.TAG;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Adapter.DatesAdapter;
import com.company.jifiit.Adapter.TimeSlotAdapter;
import com.company.jifiit.Constant;
import com.company.jifiit.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.text.style.ForegroundColorSpan;

public class CasualLeave extends AppCompatActivity {
    TextView date, totalDuration;
    Spinner spinnerOption1;
    BigInteger Id;
    Button EmergencyCall, Submit, btnFromTime, btnToTime;
    Calendar fromTimeCalendar, toTimeCalendar;
    Double BasicSalary;
    String city_id;
    String Manager_Number;
    private String currentSelectedFromTime = null;
    private String currentSelectedToTime = null;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_casual_leave);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Casual leave");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String name = sp.getString("Name", "Default Name");
        String idString = sp.getString("Id", "");
        Id = new BigInteger(idString);

        FetchBasicSalary(Id);
        setupDateRecyclerView();
        initializeViews();
        setupButtonClickListeners();
    }


    private void setupDateRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerViewDates);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        List<String[]> dates = getDatesList();
        DatesAdapter adapter = new DatesAdapter(dates, fullDate -> {
            date.setText("Selected Date: " + fullDate);

            // Enable From Time button when date is selected
            btnFromTime.setEnabled(true);
            btnFromTime.setAlpha(1.0f);

            // Clear any previous time selections
            currentSelectedFromTime = null;
            currentSelectedToTime = null;
            btnFromTime.setText("Select Time");
            btnToTime.setText("Select Time");
            totalDuration.setText("");
        });
        recyclerView.setAdapter(adapter);
    }
    private void initializeViews() {
        btnFromTime = findViewById(R.id.btnFromTime);
        btnToTime = findViewById(R.id.btnToTime);
        EmergencyCall = findViewById(R.id.EmgCall);
        Submit = findViewById(R.id.btnSubmitLeave);
        date = findViewById(R.id.dateView);
        totalDuration = findViewById(R.id.totalDuration);

        btnFromTime.setEnabled(false);
        btnFromTime.setAlpha(0.5f);
        btnToTime.setEnabled(false);
        btnToTime.setAlpha(0.5f); // Make it look disabled

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fromTimeCalendar = Calendar.getInstance();
            toTimeCalendar = Calendar.getInstance();
        }
    }

    private void setupButtonClickListeners() {
        Submit.setOnClickListener(v -> showLeaveRequestBottomSheet());

        EmergencyCall.setOnClickListener(v -> makeCall(Manager_Number));

        btnFromTime.setOnClickListener(view -> showTimeSlotDialog(true));

        btnToTime.setOnClickListener(view -> showTimeSlotDialog(false));
    }

    // Update the showTimeSlotDialog method for the "To Time" button
    @SuppressLint("NewApi")
    private void showTimeSlotDialog(boolean isFromTime) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_time_slots, null);
        builder.setView(dialogView);

        RecyclerView rvTimeSlots = dialogView.findViewById(R.id.rvTimeSlots);
        TextView tvLoading = dialogView.findViewById(R.id.tvLoading);
        TextView tvNoSlots = dialogView.findViewById(R.id.tvNoSlots);
        ProgressBar dialogProgress = dialogView.findViewById(R.id.dialogProgress);
        ImageButton btnClose = dialogView.findViewById(R.id.btnClose);
        btnClose.setColorFilter(ContextCompat.getColor(this, R.color.black));

        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        rvTimeSlots.setLayoutManager(layoutManager);

        btnClose.setOnClickListener(v -> {
            AlertDialog dialog = (AlertDialog) dialogView.getTag();
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        });

        // Show loading state initially
        rvTimeSlots.setVisibility(View.GONE);
        tvNoSlots.setVisibility(View.GONE);
        tvLoading.setVisibility(View.VISIBLE);
        dialogProgress.setVisibility(View.VISIBLE);

        // Generate all time slots
        List<String> allSlots = generateTimeSlots();

        // If this is for "To Time" and we have a "From Time" selected, filter the slots
        List<String> availableSlots = allSlots;
        if (!isFromTime && currentSelectedFromTime != null) {
            availableSlots = new ArrayList<>();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
                Date fromTimeDate = sdf.parse(currentSelectedFromTime);

                for (String slot : allSlots) {
                    Date slotDate = sdf.parse(slot);
                    if (slotDate.after(fromTimeDate)) {
                        availableSlots.add(slot);
                    }
                }

                // Also ensure we don't go past 10:00 PM
                Date endTime = sdf.parse("10:00 PM");
                availableSlots.removeIf(slot -> {
                    try {
                        return sdf.parse(slot).after(endTime);
                    } catch (ParseException e) {
                        return false;
                    }
                });

            } catch (ParseException e) {
                e.printStackTrace();
                availableSlots = allSlots; // Fallback to all slots if parsing fails
            }
        }

        // Simulate loading completion
        List<String> finalAvailableSlots = availableSlots;
        new Handler().postDelayed(() -> {
            dialogProgress.setVisibility(View.GONE);
            tvLoading.setVisibility(View.GONE);

            if (finalAvailableSlots.isEmpty()) {
                tvNoSlots.setVisibility(View.VISIBLE);
            } else {
                rvTimeSlots.setVisibility(View.VISIBLE);

                TimeSlotAdapter adapter = new TimeSlotAdapter(finalAvailableSlots,
                        isFromTime ? currentSelectedFromTime : currentSelectedToTime,
                        time -> {
                            if (isFromTime) {
                                currentSelectedFromTime = time;
                                btnFromTime.setText(time);
                                if (parseSelectedTime(time, true)) {
                                    calculateDuration(BasicSalary);
                                }
                                // Clear the To Time when From Time changes
                                currentSelectedToTime = null;
                                btnToTime.setText("Select Time");

                                // Enable To Time button now that From Time is selected
                                btnToTime.setEnabled(true);
                                btnToTime.setAlpha(1.0f); // Make it look enabled
                            } else {
                                currentSelectedToTime = time;
                                btnToTime.setText(time);
                                if (parseSelectedTime(time, false)) {
                                    calculateDuration(BasicSalary);
                                }
                            }
                            ((AlertDialog) dialogView.getTag()).dismiss();
                        });



                rvTimeSlots.setAdapter(adapter);

                String currentSelected = isFromTime ? currentSelectedFromTime : currentSelectedToTime;
                if (currentSelected != null && finalAvailableSlots.contains(currentSelected)) {
                    int position = finalAvailableSlots.indexOf(currentSelected);
                    rvTimeSlots.post(() -> {
                        layoutManager.scrollToPosition(position);
                        adapter.notifyItemChanged(position);
                    });
                }
            }
        }, 500);

        AlertDialog dialog = builder.create();
        dialogView.setTag(dialog);
        dialog.show();
    }

    // Update the generateTimeSlots method to include up to 10:00 PM
    @SuppressLint("NewApi")
    private List<String> generateTimeSlots() {
        List<String> timeSlots = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());

        Calendar calendar = Calendar.getInstance();

        try {
            String selectedDateStr = date.getText().toString().replace("Selected Date: ", "");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date selectedDate = dateFormat.parse(selectedDateStr);

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            Calendar selectedCal = Calendar.getInstance();
            selectedCal.setTime(selectedDate);
            selectedCal.set(Calendar.HOUR_OF_DAY, 0);
            selectedCal.set(Calendar.MINUTE, 0);
            selectedCal.set(Calendar.SECOND, 0);
            selectedCal.set(Calendar.MILLISECOND, 0);

            if (selectedCal.equals(today)) {
                // For today - start from current time rounded to next 15 min
                calendar = Calendar.getInstance();
                int minute = calendar.get(Calendar.MINUTE);
                int remainder = minute % 15;
                if (remainder != 0) {
                    calendar.add(Calendar.MINUTE, 15 - remainder);
                }
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
            } else {
                // For future dates - start from 8:00 AM
                calendar.set(Calendar.HOUR_OF_DAY, 8);
                calendar.set(Calendar.MINUTE, 0);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            // Default to 8:00 AM if date parsing fails
            calendar.set(Calendar.HOUR_OF_DAY, 8);
            calendar.set(Calendar.MINUTE, 0);
        }

        // End at 10:00 PM
        Calendar endTime = Calendar.getInstance();
        endTime.set(Calendar.HOUR_OF_DAY, 22); // 10 PM
        endTime.set(Calendar.MINUTE, 0);
        endTime.set(Calendar.SECOND, 0);

        // Generate slots until 10:00 PM
        while (!calendar.after(endTime)) {
            timeSlots.add(sdf.format(calendar.getTime()));
            calendar.add(Calendar.MINUTE, 15);
        }

        return timeSlots;
    }

    @SuppressLint({"NewApi", "RestrictedApi"})
    private boolean parseSelectedTime(String time, boolean isFromTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            Date date = sdf.parse(time);
            if (date != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                if (isFromTime) {
                    fromTimeCalendar = calendar;
                } else {
                    toTimeCalendar = calendar;
                }
                return true;
            }
        } catch (ParseException e) {
            Log.e(TAG, "Time parsing failed", e);
            Toast.makeText(this, "Failed to parse selected time", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
    @SuppressLint("NewApi")
    private void calculateDuration(double basicSalary) {
        if (fromTimeCalendar == null || toTimeCalendar == null) {
            return;
        }

        // Get the selected date from the TextView
        String selectedDateStr = date.getText().toString().replace("Selected Date: ", "");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        try {
            Date selectedDate = dateFormat.parse(selectedDateStr);
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.setTime(selectedDate);

            // Get the actual number of days in the selected month
            int daysInMonth = selectedCal.getActualMaximum(Calendar.DAY_OF_MONTH);

            long fromTimeInMillis = fromTimeCalendar.getTimeInMillis();
            long toTimeInMillis = toTimeCalendar.getTimeInMillis();

            // Adjust for midnight crossover if necessary
            if (toTimeInMillis < fromTimeInMillis) {
                toTimeInMillis += 24 * 60 * 60 * 1000;
            }

            long durationInMillis = toTimeInMillis - fromTimeInMillis;
            int hours = (int) (durationInMillis / (1000 * 60 * 60));
            int minutes = (int) ((durationInMillis % (1000 * 60 * 60)) / (1000 * 60));

            StringBuilder durationString = new StringBuilder();
            if (hours > 0) {
                durationString.append(hours).append(" hour").append(hours > 1 ? "s" : "");
            }
            if (minutes > 0) {
                if (durationString.length() > 0) {
                    durationString.append(" ");
                }
                durationString.append(minutes).append(" minute").append(minutes > 1 ? "s" : "");
            }
            if (durationString.length() == 0) {
                durationString.append("0 minutes");
            }

            // Use the actual days in month instead of hardcoded 30
            double perMinuteSalary = basicSalary / (daysInMonth * 14 * 60);
            double totalDeduction = (hours * 60 + minutes) * perMinuteSalary;
            String deductionText = String.format("%.2f", totalDeduction);

            SpannableString formattedText = new SpannableString("Leave Duration: " + durationString +
                    "\nSalary Deduction: ₹" + deductionText);

            // Apply bold style to labels
            formattedText.setSpan(new StyleSpan(Typeface.BOLD), 0, "Leave Duration:".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            int salaryStartIndex = "Leave Duration: ".length() + durationString.length() + 1; // Start of "Salary Deduction"
            formattedText.setSpan(new StyleSpan(Typeface.BOLD), salaryStartIndex, salaryStartIndex + "Salary Deduction:".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Apply red color if deduction is ₹0.00
            if (totalDeduction == 0.00) {
                int amountStartIndex = salaryStartIndex + "Salary Deduction: ₹".length();
                formattedText.setSpan(new ForegroundColorSpan(Color.RED), amountStartIndex, formattedText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                int amountStartIndex = salaryStartIndex + "Salary Deduction: ₹".length();
                formattedText.setSpan(new ForegroundColorSpan(Color.BLACK), amountStartIndex, formattedText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            totalDuration.setText(formattedText);

        } catch (ParseException e) {
            e.printStackTrace();
            // Fallback to default 30 days if date parsing fails
            double perMinuteSalary = basicSalary / (30 * 14 * 60);
            // ... rest of your calculation with fallback value
            Toast.makeText(this, "Error calculating days in month, using default value", Toast.LENGTH_SHORT).show();
        }
    }
    @SuppressLint("NewApi")
    private List<String[]> getDatesList() {
        List<String[]> dates = new ArrayList<>();
         SimpleDateFormat displayFormat = new SimpleDateFormat("EE \n d", Locale.getDefault());
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < 5; i++) {
            String displayDate = displayFormat.format(calendar.getTime()).toUpperCase();
            String fullDate = fullDateFormat.format(calendar.getTime());
            dates.add(new String[]{displayDate, fullDate});
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        return dates;
    }
    @SuppressLint("NewApi")
    private void showLeaveRequestBottomSheet() {
        String selectedDate = date.getText().toString().split(": ").length > 1 ?
                date.getText().toString().split(": ")[1] : "";
        String convertedDate = "";

        if (!selectedDate.isEmpty()) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date parsedDate = inputFormat.parse(selectedDate);
                convertedDate = outputFormat.format(parsedDate);
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String fromTime = btnFromTime.getText().toString();
        String toTime = btnToTime.getText().toString();

        if (selectedDate.isEmpty() || fromTime.equals("Select Time") || toTime.equals("Select Time")) {
            Toast.makeText(this, "Please select all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_reason_form, null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();

        Spinner spinnerReason = bottomSheetView.findViewById(R.id.formSelection);

        // Custom adapter for Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                R.layout.custom_spinner_item,
                getResources().getStringArray(R.array.selection_options2)
        ) {
            @Override
            public boolean isEnabled(int position) {
                // Disable the first item
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);

                if (position == 0) {
                    textView.setTextColor(Color.GRAY);
                } else {
                    textView.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        spinnerReason.setAdapter(adapter);
        spinnerReason.setSelection(0, false);

        ImageView closeButton = bottomSheetView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> bottomSheetDialog.dismiss());

        Button submitButton = bottomSheetView.findViewById(R.id.formSubmitButton);
        String finalConvertedDate = convertedDate;

        submitButton.setOnClickListener(v -> {
            String selectedReason = spinnerReason.getSelectedItem().toString();
            if (selectedReason.equals("Select Reason for Leave")) {
                Toast.makeText(CasualLeave.this, "Please select a reason for leave", Toast.LENGTH_SHORT).show();
                return;
            }
            submitLeaveRequest(selectedReason, finalConvertedDate, fromTime, toTime, Id);
            bottomSheetDialog.dismiss();
        });
    }


    private void submitLeaveRequest(String selectedReason, String selectedDate, String fromTime, String toTime, BigInteger id) {
        if (isDataValid(selectedReason, selectedDate, fromTime, toTime, id)) {
            // Show progress indicator
            CircularProgressIndicator progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);

            // Disable submit button to prevent multiple submissions
            Submit.setEnabled(false);
            Submit.setAlpha(0.5f); // Make button look disabled

            Map<String, String> leaveData = new HashMap<>();
            leaveData.put("id", String.valueOf(id));
            leaveData.put("date", selectedDate);
            leaveData.put("start_time", fromTime);
            leaveData.put("end_time", toTime);
            leaveData.put("reason", selectedReason);

            String url = Constant.casualLeave;

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST, url, new JSONObject(leaveData),
                    response -> {
                        // Hide progress on success
                        progressBar.setVisibility(View.GONE);
                        Submit.setEnabled(true);
                        Submit.setAlpha(1.0f);
                        handleBackendResponse(response);
                    },
                    error -> {
                        // Hide progress on error
                        progressBar.setVisibility(View.GONE);
                        Submit.setEnabled(true);
                        Submit.setAlpha(1.0f);
                        handleError(error);
                    }
            );

            // Set retry policy
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    20000, // 20 seconds timeout
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            Volley.newRequestQueue(this).add(jsonObjectRequest);
        } else {
            Toast.makeText(this, "Please fill in all the required fields", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isDataValid(String selectedReason, String selectedDate, String fromTime, String toTime, BigInteger id) {
        return selectedReason != null && !selectedReason.isEmpty() &&
                selectedDate != null && !selectedDate.isEmpty() &&
                fromTime != null && !fromTime.isEmpty() &&
                toTime != null && !toTime.isEmpty() &&
                id != null;
    }

    private void handleBackendResponse(JSONObject response) {
        runOnUiThread(() -> {
            CircularProgressIndicator progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.GONE);

            String status = response.optString("status", "error");
            String message = response.optString("message", "Unknown error");

            if (status.equals("success")) {
                showSuccessDialog();
                clearAllFields();
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleError(VolleyError error) {
        runOnUiThread(() -> {
            CircularProgressIndicator progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.GONE);
            Submit.setEnabled(true);
            Submit.setAlpha(1.0f);

            String errorMessage;
            if (error.networkResponse != null) {
                int statusCode = error.networkResponse.statusCode;
                String responseBody = new String(error.networkResponse.data);
                errorMessage = "Error Code: " + statusCode + ", Message: " + responseBody;
            } else if (error instanceof TimeoutError) {
                errorMessage = "Request timed out. Please try again.";
            } else if (error instanceof NoConnectionError) {
                errorMessage = "No internet connection. Please check your network.";
            } else if (error instanceof AuthFailureError) {
                errorMessage = "Authentication failure. Please check your credentials.";
            } else {
                errorMessage = "Unknown error occurred: " + error.getMessage();
            }

            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            Log.d("VolleyError", errorMessage);
        });
    }

    private void FetchBasicSalary(BigInteger id) {
        String url = Constant.basicSalary + id;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            String basicSalary = response.getString("basic_salary");
                            String city = response.getString("city_id");
                            basicSalary = basicSalary.replace(",", "");
                            this.BasicSalary = Double.parseDouble(basicSalary);
                            findNumber(city);
                        } else {
                            String message = response.optString("message", "Error: No message provided.");
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "JSON Parsing Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Invalid salary format: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    String errorMessage = error.getMessage() != null ? error.getMessage() : "Unknown error occurred.";
                    Toast.makeText(getApplicationContext(), "Error occurred: " + errorMessage, Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void findNumber(String city) {
        String url = Constant.emgSupport + city;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            this.Manager_Number = response.getString("phone");
                        } else {
                            String message = response.optString("message", "Error: No message provided.");
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "JSON Parsing Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    String errorMessage = error.getMessage() != null ? error.getMessage() : "Unknown error occurred.";
                    Toast.makeText(getApplicationContext(), "Error occurred: " + errorMessage, Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void makeCall(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Invalid phone number.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
        } else {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);
        }
    }
    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_success, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
        Button btnOk = dialogView.findViewById(R.id.btnOk);

        tvMessage.setText("Leave submitted successfully!");

        AlertDialog dialog = builder.create();

        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
            clearAllFields();
        });

        dialog.show();
    }

    private void clearAllFields() {
        // Clear date selection
        date.setText("");

        // Clear time selections
        currentSelectedFromTime = null;
        currentSelectedToTime = null;
        btnFromTime.setText("Select Time");
        btnToTime.setText("Select Time");
        btnFromTime.setEnabled(false);
        btnFromTime.setAlpha(0.5f);
        btnToTime.setEnabled(false);
        btnToTime.setAlpha(0.5f);

        // Clear duration display
        totalDuration.setText("");

        // Reset the date recycler view to today
        setupDateRecyclerView();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}