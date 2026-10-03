package com.company.jifiit.Leave;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Constant;
import com.company.jifiit.R;

import org.json.JSONException;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class LeaveActivity extends AppCompatActivity {
    LinearLayout week, leaveDays;
    Button mon, tues, wed, thur, fri, offday;
    private TimeZone deviceTimeZone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave);

        // Get device's default timezone
        Calendar calendar = Calendar.getInstance();
        deviceTimeZone = calendar.getTimeZone();
        Log.d("TimeZoneTest", "Current Time Zone: " + deviceTimeZone.getID());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Weekly off");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        initializeViews();
        checkTimeAndShowLayout();
    }

    private void initializeViews() {
        week = findViewById(R.id.weeks);
        leaveDays = findViewById(R.id.leaveDays);
        mon = findViewById(R.id.btn_monday);
        tues = findViewById(R.id.btn_tuesday);
        wed = findViewById(R.id.btn_wednesday);
        thur = findViewById(R.id.btn_thursday);
        fri = findViewById(R.id.btn_friday);
        offday = findViewById(R.id.offday);

        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String idString = sp.getString("Id", "");
        if (idString.isEmpty()) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        BigInteger Id = new BigInteger(idString);
        setupButtonListeners(Id);
        fetchdays(Id);
    }

    private void checkTimeAndShowLayout() {
        Calendar calendar = Calendar.getInstance(deviceTimeZone);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minuteOfHour = calendar.get(Calendar.MINUTE);

        // Check if it's Sunday between 9:00 PM and 11:50 PM in device's timezone
        boolean isSundayWindow = dayOfWeek == Calendar.SUNDAY &&
                ((hourOfDay >= 21 && hourOfDay < 23) ||
                        (hourOfDay == 23 && minuteOfHour < 50));

        week.setVisibility(isSundayWindow ? View.VISIBLE : View.GONE);
        leaveDays.setVisibility(isSundayWindow ? View.GONE : View.VISIBLE);

        // Schedule the next check for when the window might change
        scheduleNextCheck();
    }

    private void scheduleNextCheck() {
        long delayMillis = calculateDelayMillisUntilNextCheck();
        new Handler().postDelayed(this::checkTimeAndShowLayout, delayMillis);
    }

    private long calculateDelayMillisUntilNextCheck() {
        Calendar now = Calendar.getInstance(deviceTimeZone);
        Calendar nextCheckTime = Calendar.getInstance(deviceTimeZone);

        // If we're currently in the Sunday window
        if (isInSundayWindow(now)) {
            // Next check should be at 11:50 PM to hide the layout
            nextCheckTime.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            nextCheckTime.set(Calendar.HOUR_OF_DAY, 23);
            nextCheckTime.set(Calendar.MINUTE, 50);
            nextCheckTime.set(Calendar.SECOND, 0);

            // If it's already past 11:50 PM Sunday, check next Sunday
            if (now.after(nextCheckTime)) {
                nextCheckTime.add(Calendar.DAY_OF_YEAR, 7);
            }
        } else {
            // Next check should be at next Sunday 9:00 PM
            nextCheckTime.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            nextCheckTime.set(Calendar.HOUR_OF_DAY, 21);
            nextCheckTime.set(Calendar.MINUTE, 0);
            nextCheckTime.set(Calendar.SECOND, 0);

            // If it's already past Sunday 9:00 PM, check next Sunday
            if (now.after(nextCheckTime)) {
                nextCheckTime.add(Calendar.DAY_OF_YEAR, 7);
            }
        }

        return nextCheckTime.getTimeInMillis() - now.getTimeInMillis();
    }

    private boolean isInSundayWindow(Calendar calendar) {
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minuteOfHour = calendar.get(Calendar.MINUTE);

        return dayOfWeek == Calendar.SUNDAY &&
                ((hourOfDay >= 21 && hourOfDay < 23) ||
                        (hourOfDay == 23 && minuteOfHour < 50));
    }

    private void setupButtonListeners(BigInteger id) {
        mon.setOnClickListener(v -> applyLeave(id, "Monday"));
        tues.setOnClickListener(v -> applyLeave(id, "Tuesday"));
        wed.setOnClickListener(v -> applyLeave(id, "Wednesday"));
        thur.setOnClickListener(v -> applyLeave(id, "Thursday"));
        fri.setOnClickListener(v -> applyLeave(id, "Friday"));
    }

    private void applyLeave(final BigInteger id, final String weekday) {
        String url = Constant.weeklyOff + "?id=" + id + "&weekday=" + weekday;
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.equalsIgnoreCase("Record inserted successfully")) {
                        week.setVisibility(View.GONE);
                        leaveDays.setVisibility(View.VISIBLE);
                        fetchdays(id);
                    }
                    showMessageDialog(response);
                },
                error -> {
                    Toast.makeText(this, "Error applying leave", Toast.LENGTH_LONG).show();
                }
        );

        queue.add(stringRequest);
    }

    private void showMessageDialog(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void fetchdays(BigInteger id) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = Constant.fetchdays + id;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String status = response.getString("status");
                        if (status.equals("success")) {
                            String weeklyOffDay = response.getString("weekly_off_day");

                            if (weeklyOffDay.equals("No data found for the current week.") ||
                                    weeklyOffDay.equals("Not specified")) {
                                // Don't change visibility here - let checkTimeAndShowLayout handle it
                            } else {
                                try {
                                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                    inputFormat.setTimeZone(deviceTimeZone);
                                    SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
                                    outputFormat.setTimeZone(deviceTimeZone);
                                    Date date = inputFormat.parse(weeklyOffDay);
                                    String dayName = outputFormat.format(date);
                                    offday.setText(dayName);
                                } catch (ParseException e) {
                                    offday.setText("Invalid Date");
                                }
                            }
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show());

        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up any handlers to prevent memory leaks
        new Handler().removeCallbacksAndMessages(null);
    }
}