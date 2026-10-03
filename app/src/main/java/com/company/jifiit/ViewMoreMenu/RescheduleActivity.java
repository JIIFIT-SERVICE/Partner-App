package com.company.jifiit.ViewMoreMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Adapter.RescheduleAdapter;
import com.company.jifiit.Adapter.TimeSlotAdapter;
import com.company.jifiit.Constant;
import com.company.jifiit.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class RescheduleActivity extends AppCompatActivity {
    private static final String TAG = "RescheduleActivity";
    private static final String DATE_PREFIX = "Selected Date: ";

    private TextView tvSelectedDate;
    private Button btnFromTime, btnSubmit;
    private Calendar fromTimeCalendar;

    private String startTime, endTime, orderId, duration, date, therapistId;
    private List<TimeRange> bookedSlots = new ArrayList<>();
    private List<TimeRange> blockedSlots = new ArrayList<>();
    private String currentSelectedDate;
    private String currentSelectedTime;
    private ProgressBar progressBar;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reschedule);

        // Initialize calendar first
        progressBar = findViewById(R.id.progressBar);
        fromTimeCalendar = Calendar.getInstance();

        initializeToolbar();
        initializeSharedPreferences();
        getIntentExtras();
        initializeViews();
        setupDateSelection();
        setupSubmitButton();

        // Set click listener for time selection button
        btnFromTime.setOnClickListener(v -> showTimeSlotDialog());
    }

    private void showLoading(boolean show) {
        runOnUiThread(() -> {
            if (show) {
                progressBar.setVisibility(View.VISIBLE);
                btnFromTime.setEnabled(false);
                btnSubmit.setEnabled(false);
            } else {
                progressBar.setVisibility(View.GONE);
                btnFromTime.setEnabled(true);
                btnSubmit.setEnabled(true);
            }
        });
    }
    private void initializeToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Reschedule booking");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void initializeSharedPreferences() {
        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        therapistId = sp.getString("Id", "");
    }

    private void getIntentExtras() {
        startTime = getIntent().getStringExtra("time");
        endTime = getIntent().getStringExtra("endTime");
        orderId = getIntent().getStringExtra("id");
        duration = getIntent().getStringExtra("duration");
        date = getIntent().getStringExtra("date");
    }

    private void initializeViews() {
        tvSelectedDate = findViewById(R.id.dateView);
        btnFromTime = findViewById(R.id.btnFromTime);
        btnSubmit = findViewById(R.id.btnSubmitLeave);
        currentSelectedDate = date;
        currentSelectedTime = startTime;
        tvSelectedDate.setText(DATE_PREFIX + date);
        btnFromTime.setText(startTime);
    }

    private void setupDateSelection() {
        RecyclerView recyclerView = findViewById(R.id.recyclerViewDates);
        recyclerView.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));

        List<String[]> dates = getDatesList();
        RescheduleAdapter adapter = new RescheduleAdapter(dates, fullDate -> {
            // Store the previous selection
            String previousDate = currentSelectedDate;
            currentSelectedDate = fullDate;

            tvSelectedDate.setText(DATE_PREFIX + fullDate);

            // Only reload if date actually changed
            if (!fullDate.equals(previousDate)) {
                loadTimeSlotsForSelectedDate(fullDate);
            }
        }, date);
        recyclerView.setAdapter(adapter);
    }

    private void loadTimeSlotsForSelectedDate(String selectedDate) {
        // Only reset time selection if we're changing to a different date
        if (!selectedDate.equals(currentSelectedDate)) {
            currentSelectedTime = null;
            btnFromTime.setText("Select Time");
            endTime = null;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        try {
            Date parsedDate = dateFormat.parse(selectedDate);
            SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dateParam = apiDateFormat.format(parsedDate);

            fetchTherapistData(therapistId, dateParam, new TherapistDataCallback() {
                @Override
                public void onDataLoaded() {
                    // If we have a previously selected time for this date, try to keep it
                    if (currentSelectedTime != null) {
                        runOnUiThread(() -> {
                            btnFromTime.setText(currentSelectedTime);
                            if (parseSelectedTime(currentSelectedTime)) {
                                calculateEndTime();
                            }
                        });
                    }
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        Toast.makeText(RescheduleActivity.this, message, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingInflatedId")
    private void showTimeSlotDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_time_slots, null);
        builder.setView(dialogView);

        RecyclerView rvTimeSlots = dialogView.findViewById(R.id.rvTimeSlots);
        TextView tvLoading = dialogView.findViewById(R.id.tvLoading);
        TextView tvNoSlots = dialogView.findViewById(R.id.tvNoSlots);
        ProgressBar dialogProgress = dialogView.findViewById(R.id.dialogProgress);
        ImageButton btnClose = dialogView.findViewById(R.id.btnClose);
        btnClose.setColorFilter(R.color.black);

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

        String selectedDateStr = tvSelectedDate.getText().toString().replace(DATE_PREFIX, "");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        try {
            Date selectedDate = dateFormat.parse(selectedDateStr);
            SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dateParam = apiDateFormat.format(selectedDate);

            fetchTherapistData(therapistId, dateParam, new TherapistDataCallback() {
                @Override
                public void onDataLoaded() {
                    runOnUiThread(() -> {
                        List<String> allSlots = generateTimeSlots();
                        List<String> availableSlots = filterAvailableSlots(allSlots);

                        dialogProgress.setVisibility(View.GONE);
                        tvLoading.setVisibility(View.GONE);

                        if (availableSlots.isEmpty()) {
                            tvNoSlots.setVisibility(View.VISIBLE);
                        } else {
                            rvTimeSlots.setVisibility(View.VISIBLE);

                            TimeSlotAdapter adapter = new TimeSlotAdapter(availableSlots, currentSelectedTime, time -> {
                                currentSelectedTime = time;
                                btnFromTime.setText(time);
                                if (parseSelectedTime(time)) {
                                    calculateEndTime();
                                }
                                ((AlertDialog) dialogView.getTag()).dismiss();
                            });

                            rvTimeSlots.setAdapter(adapter);

                            if (currentSelectedTime != null && availableSlots.contains(currentSelectedTime)) {
                                int position = availableSlots.indexOf(currentSelectedTime);
                                rvTimeSlots.post(() -> {
                                    layoutManager.scrollToPosition(position);
                                    adapter.notifyItemChanged(position);
                                });
                            }
                        }
                    });
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        dialogProgress.setVisibility(View.GONE);
                        tvLoading.setVisibility(View.GONE);
                        tvNoSlots.setVisibility(View.VISIBLE);
                        Toast.makeText(RescheduleActivity.this, message, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
            dialogProgress.setVisibility(View.GONE);
            tvLoading.setVisibility(View.GONE);
            tvNoSlots.setVisibility(View.VISIBLE);
        }

        AlertDialog dialog = builder.create();
        dialogView.setTag(dialog);
        dialog.show();
    }

    private List<String> generateTimeSlots() {
        List<String> timeSlots = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());

        // Get current date and time
        Calendar now = Calendar.getInstance();
        Calendar selectedDate = Calendar.getInstance();

        try {
            // Parse the selected date from tvSelectedDate
            String dateStr = tvSelectedDate.getText().toString().replace(DATE_PREFIX, "");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date parsedDate = dateFormat.parse(dateStr);
            selectedDate.setTime(parsedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Check if selected date is today
        boolean isToday = isSameDay(now, selectedDate);

        // Start at 8:00 AM
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // End at 10:45 PM
        Calendar endTime = Calendar.getInstance();
        endTime.set(Calendar.HOUR_OF_DAY, 22); // 10 PM
        endTime.set(Calendar.MINUTE, 45);      // 45 minutes
        endTime.set(Calendar.SECOND, 0);

        // If today, start from current time + 15 minutes (rounded up)
        if (isToday) {
            calendar = (Calendar) now.clone();
            calendar.add(Calendar.MINUTE, 15); // Add 15 minute buffer

            // Round up to next 15-minute interval
            int minute = calendar.get(Calendar.MINUTE);
            int remainder = minute % 15;
            if (remainder != 0) {
                calendar.add(Calendar.MINUTE, 15 - remainder);
            }
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }

        // Generate slots until 10:45 PM
        while (!calendar.after(endTime)) {
            timeSlots.add(sdf.format(calendar.getTime()));
            calendar.add(Calendar.MINUTE, 15); // Add 15-minute intervals
        }

        return timeSlots;
    }


    private void fetchTherapistData(String therapistId, String date, TherapistDataCallback callback) {
//        showLoading(true);

        // Clear previous data
        bookedSlots.clear();
        blockedSlots.clear();

        // Use CountDownLatch to synchronize both API calls
        CountDownLatch latch = new CountDownLatch(2);
        AtomicBoolean hasError = new AtomicBoolean(false);
        AtomicReference<String> errorMessage = new AtomicReference<>("");

        fetchTherapistBookings(therapistId, date, new BookingsCallback() {
            @Override
            public void onSuccess(List<TimeRange> bookings) {
                bookedSlots.addAll(bookings);
                latch.countDown();
                checkAllDataLoaded(callback);
            }

            @Override
            public void onError(String message) {
                hasError.set(true);
                errorMessage.set(message);
                latch.countDown();
            }
        });

        fetchTherapistLeaves(therapistId, date, new LeavesCallback() {
            @Override
            public void onSuccess(List<TimeRange> leaves) {
                blockedSlots.addAll(leaves);
                latch.countDown();
                checkAllDataLoaded(callback);
            }

            @Override
            public void onError(String message) {
                hasError.set(true);
                errorMessage.set(message);
                latch.countDown();
            }
        });

        new Thread(() -> {
            try {
                latch.await();
//                showLoading(false);

                if (hasError.get()) {
                    callback.onError(errorMessage.get());
                } else {
                    callback.onDataLoaded();
                }
            } catch (InterruptedException e) {
//                showLoading(false);
                callback.onError("Data loading interrupted");
            }
        }).start();
    }



    private void checkAllDataLoaded(TherapistDataCallback callback) {
        // This method would be more robust with proper synchronization
        // For simplicity, we're assuming both calls will complete
        callback.onDataLoaded();
    }

    private void fetchTherapistBookings(String therapistId, String date, BookingsCallback callback) {
        String url = Constant.therapistOrder + therapistId + "&date=" + date;

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getBoolean("success")) {
                                JSONArray bookings = jsonResponse.getJSONArray("bookings");
                                List<TimeRange> result = new ArrayList<>();
                                for (int i = 0; i < bookings.length(); i++) {
                                    JSONObject booking = bookings.getJSONObject(i);
                                    String start = normalizeTime(booking.getString("start_time"));
                                    String end = normalizeTime(booking.getString("end_time"));
                                    result.add(new TimeRange(start, end));
                                }
                                callback.onSuccess(result);
                            }
                            else {
                                callback.onError(jsonResponse.optString("message", "No bookings available"));
                            }
                        } catch (JSONException e) {
                            callback.onError("Error parsing bookings");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError("Network error loading bookings");
                    }
                });

        queue.add(request);
    }

    private void fetchTherapistLeaves(String therapistId, String date, LeavesCallback callback) {
        String url = Constant.therapistLeave + therapistId + "&date=" + date;

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getBoolean("success")) {
                                JSONArray leaves = jsonResponse.getJSONArray("leaves");
                                List<TimeRange> result = new ArrayList<>();

                                for (int i = 0; i < leaves.length(); i++) {
                                    JSONObject leave = leaves.getJSONObject(i);
                                    String leaveType = leave.getString("leave_type").toUpperCase();

                                    if (leaveType.equals("BLOCK") || leaveType.equals("CASUAL LEAVE")) {
                                        String from = normalizeTime(leave.getString("fromtime"));
                                        String to = normalizeTime(leave.getString("totime"));
                                        result.add(new TimeRange(from, to));
                                    }
                                }

                                callback.onSuccess(result);
                            }
                            else {
//                                 callback.onError("");
                            }
                        } catch (JSONException e) {
                            callback.onError("Error parsing leaves");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError("Network error loading leaves");
                    }
                });

        queue.add(request);
    }

    private List<String> filterAvailableSlots(List<String> allSlots) {
        List<String> availableSlots = new ArrayList<>();

        for (String slot : allSlots) {
            if (isSlotAvailable(slot)) {
                availableSlots.add(slot);
            }
        }

        // If our selected time is no longer available, clear it
        if (currentSelectedTime != null && !availableSlots.contains(currentSelectedTime)) {
            currentSelectedTime = null;
            runOnUiThread(() -> {
                btnFromTime.setText("Select Time");
                endTime = null;
            });
        }

        return availableSlots;
    }

    private boolean isSlotAvailable(String slotTime) {
        String normalizedSlot = normalizeTime(slotTime);

        // Check against booked slots
        for (TimeRange booked : bookedSlots) {
            if (normalizedSlot.compareTo(booked.start) >= 0 &&
                    normalizedSlot.compareTo(booked.end) < 0) {
                return false;
            }
        }

        // Check against blocked slots
        for (TimeRange blocked : blockedSlots) {
            if (normalizedSlot.compareTo(blocked.start) >= 0 &&
                    normalizedSlot.compareTo(blocked.end) < 0) {
                return false;
            }
        }

        return true;
    }

    private String normalizeTime(String timeStr) {
        try {
            SimpleDateFormat sdf12 = new SimpleDateFormat("h:mm a", Locale.getDefault());
            SimpleDateFormat sdf24 = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = sdf12.parse(timeStr);
            return sdf24.format(date);
        } catch (ParseException e) {
            try {
                // Try parsing as 24-hour format if AM/PM parsing fails
                SimpleDateFormat sdf24 = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Date date = sdf24.parse(timeStr);
                return sdf24.format(date);
            } catch (ParseException e2) {
                return "00:00"; // default value if parsing fails
            }
        }
    }

    private boolean parseSelectedTime(String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            Date date = sdf.parse(time);
            if (date != null && fromTimeCalendar != null) {
                fromTimeCalendar.setTime(date);
                return true;
            }
        } catch (ParseException e) {
            Log.e(TAG, "Time parsing failed", e);
            Toast.makeText(this, "Failed to parse selected time", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void calculateEndTime() {
        try {
            if (fromTimeCalendar == null || duration == null) {
                Toast.makeText(this, "Time not properly initialized", Toast.LENGTH_SHORT).show();
                return;
            }

            int durationMinutes = Integer.parseInt(duration);
            Calendar endTimeCalendar = (Calendar) fromTimeCalendar.clone();
            endTimeCalendar.add(Calendar.MINUTE, durationMinutes);

            // Format for display
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            endTime = sdf.format(endTimeCalendar.getTime());
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid duration format", e);
            Toast.makeText(this, "Invalid duration format", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSubmitButton() {
        btnSubmit.setOnClickListener(v -> showConfirmationDialog());
    }

    private void showConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to re-schedule your booking time and date?")
                .setPositiveButton("OK", (dialog, which) -> submitData())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void submitData() {
        String selectedDate = tvSelectedDate.getText().toString().replace(DATE_PREFIX, "");
        String selectedStartTime = btnFromTime.getText().toString();

        if (selectedDate.isEmpty() || selectedStartTime.isEmpty() || endTime == null) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = Constant.reschedule;
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> handleApiResponse(response),
                error -> {
                    Log.e(TAG, "API Error: " + error.getMessage(), error);
                    Toast.makeText(this, "Failed to submit data", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("order_id", orderId);
                params.put("date", selectedDate);
                params.put("start_time", selectedStartTime);
                params.put("end_time", endTime);
                params.put("id", therapistId);
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    private void handleApiResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            String status = jsonResponse.getString("status");
            String message = jsonResponse.getString("message");

            if ("success".equals(status)) {
                Toast.makeText(this, "Rescheduled successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON Parsing Error: " + e.getMessage(), e);
            Toast.makeText(this, "Error processing response", Toast.LENGTH_SHORT).show();
        }
    }

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

    // Helper method to check if two Calendar instances represent the same day
    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Data class for time ranges
    private static class TimeRange {
        String start;
        String end;

        TimeRange(String start, String end) {
            this.start = start;
            this.end = end;
        }
    }

    // Callback interfaces
    interface TherapistDataCallback {
        void onDataLoaded();
        void onError(String message);
    }

    interface BookingsCallback {
        void onSuccess(List<TimeRange> bookings);
        void onError(String message);
    }

    interface LeavesCallback {
        void onSuccess(List<TimeRange> leaves);
        void onError(String message);
    }

    // Adapter for time slots
}