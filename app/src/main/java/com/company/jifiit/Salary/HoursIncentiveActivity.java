package com.company.jifiit.Salary;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Constant;
import com.company.jifiit.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.json.JSONException;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HoursIncentiveActivity extends AppCompatActivity {
    private TextView monthYearBtn, BackBtn, NextBtn, currentHours, hourIncentive;
    private TextView currentHoursText, nextMilestone, remainingHoursTxt;
    private LinearProgressIndicator progressBar;
    private Calendar calendar;
    private Calendar currentCalendar;
    // Declare arrays for all 12 segments
    private View[] filledViews = new View[12];
    private View[] tickMarks = new View[12];

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        setContentView(R.layout.activity_hours_incentive);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize MaterialToolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationIconTint(ContextCompat.getColor(this, R.color.black));

        // Initialize views
        monthYearBtn = findViewById(R.id.monthYearBtn);
        BackBtn = findViewById(R.id.BackBtn);
        NextBtn = findViewById(R.id.NextBtn);
        currentHours = findViewById(R.id.totalHours);
        hourIncentive = findViewById(R.id.hourincentive);
        currentHoursText = findViewById(R.id.current_hours);
        nextMilestone = findViewById(R.id.next_milestone);
        remainingHoursTxt = findViewById(R.id.remaining_hours);
        progressBar = findViewById(R.id.progress_bar);


        initializeCalendar();
        updateMonthYearButton();
        setButtonListeners();
        initializeProgressViews();
    }

    private void initializeProgressViews() {
        // Initialize filled progress segments (1-12)
        for (int i = 0; i < 12; i++) {
            int filledViewId = getResources().getIdentifier("filled_view" + (i+1), "id", getPackageName());
            filledViews[i] = findViewById(filledViewId);

            int tickMarkId = getResources().getIdentifier("tick_mark" + (i+1), "id", getPackageName());
            tickMarks[i] = findViewById(tickMarkId);
        }
    }
    private void initializeCalendar() {
        calendar = Calendar.getInstance();
        currentCalendar = Calendar.getInstance();
    }

    private void setButtonListeners() {
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
        BigInteger Id = new BigInteger(idString);

        int year = calendar.get(Calendar.YEAR);
        String monthName = new SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.getTime());
        String yearText = String.valueOf(year);

        SpannableString spannableString = new SpannableString(monthName + " " + yearText);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, monthName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        monthYearBtn.setText(spannableString);
        fetchHoursAndIncentiveData(Id, spannableString);

        // Show/hide next button based on current month
        if (calendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)) {
            NextBtn.setVisibility(View.GONE);
        } else {
            NextBtn.setVisibility(View.VISIBLE);
        }
    }

    private void fetchHoursAndIncentiveData(BigInteger id, SpannableString monthYear) {
        Log.d("HoursIncentive", "Fetching data for month: " + monthYear);
        String url = Constant.BasicSalary + id + "&monthYear=" + monthYear;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                String status = response.getString("status");
                if ("success".equals(status)) {
                    String totalHour = response.optString("Total_Hours", "0");
                    String totalDuration_in_minutes = response.optString("totalDuration_in_minutes", "0");
                    String incentive = response.optString("Hours_Incentive", "0");
                    int currentHours = Integer.parseInt(totalHour); // From API response
                    setupProgressSegments(currentHours);
                    updateUI(totalDuration_in_minutes, incentive);
                } else {
                    // Handle empty or error state
                    updateUI("0", "0");
                    Toast.makeText(this, "No data available", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Log.e("HoursIncentive", "JSON parsing error", e);
                Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Log.e("HoursIncentive", "Network error", error);
            Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
        });

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }



    private void updateUI(String totalDurationMinutesStr, String incentiveStr) {
        try {
            // Parse total duration in minutes and incentive
            int totalMinutes = Integer.parseInt(totalDurationMinutesStr);
            double incentive = Double.parseDouble(incentiveStr);

            // Calculate hours and minutes
            int hours = totalMinutes / 60;
            int minutes = totalMinutes % 60;

            // Format numbers for display
            NumberFormat numberFormat = NumberFormat.getInstance();

            // Update top summary with "Xhrs YYmin" format
            currentHours.setText(String.format(Locale.getDefault(), "%dhrs %02dmin", hours, minutes));
            hourIncentive.setText("₹" + numberFormat.format(incentive));

            // Update progress section with current time
            currentHoursText.setText(String.format(Locale.getDefault(), "%dhrs %02dmin", hours, minutes));

            // Determine next milestone and remaining time
            int nextMilestoneHours = calculateNextMilestone(totalMinutes / 60); // Convert to hours for milestone calculation
            int remainingTotalMinutes = (nextMilestoneHours * 60) - totalMinutes;
            int remainingHours = remainingTotalMinutes / 60;
            int remainingMinutes = remainingTotalMinutes % 60;

            // Format remaining time
            String remainingTimeText;
            if (remainingHours > 0) {
                remainingTimeText = String.format(Locale.getDefault(),
                        "Remaining: %dhrs %02dmin", remainingHours, remainingMinutes);
            } else {
                remainingTimeText = String.format(Locale.getDefault(),
                        "Remaining: %dmin", remainingMinutes);
            }

            nextMilestone.setText("Next: " + nextMilestoneHours + "hrs");
            remainingHoursTxt.setText(remainingTimeText);

            // Calculate progress percentage
            int previousMilestone = calculatePreviousMilestone(totalMinutes / 60);
            int progress = 0;
            if (nextMilestoneHours > previousMilestone) {
                progress = (int) (((float)((totalMinutes / 60) - previousMilestone) /
                        (nextMilestoneHours - previousMilestone)) * 100);
            }
            progressBar.setProgress(progress);

            // Update chart visualization
            updateChartVisualization(totalMinutes / 60);

        } catch (NumberFormatException e) {
            Log.e("HoursIncentive", "Number parsing error", e);
        }
    }

    private int calculateNextMilestone(int hours) {
        if (hours < 20) return 20;
        if (hours < 30) return 30;
        if (hours < 40) return 40;
        if (hours < 50) return 50;
        if (hours < 60) return 60;
        if (hours < 70) return 70;
        if (hours < 80) return 80;
        if (hours < 90) return 90;
        if (hours < 100) return 100;
        if (hours < 110) return 110;
        if (hours < 120) return 120;
        return 125;
    }

    private int calculatePreviousMilestone(int hours) {
        if (hours < 20) return 0;
        if (hours < 30) return 20;
        if (hours < 40) return 30;
        if (hours < 50) return 40;
        if (hours < 60) return 50;
        if (hours < 70) return 60;
        if (hours < 80) return 70;
        if (hours < 90) return 80;
        if (hours < 100) return 90;
        if (hours < 110) return 100;
        if (hours < 120) return 110;
        return 120;
    }

    private void updateChartVisualization(int hours) {
        // This is where you would update the chart visualization
        // based on the user's current hours. You can implement this
        // based on your specific visualization needs.

    }


    private void setupProgressSegments(int currentHours) {
        int[] milestones = {20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 125};

        for (int i = 0; i < 12; i++) {
            if (currentHours >= milestones[i]) {
                // Completed segment
                filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                tickMarks[i].setBackgroundResource(R.drawable.lock_open);
            } else {
                // Incomplete segment
                filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.gray_300));
                tickMarks[i].setBackgroundResource(R.drawable.slider_tick_mark);
            }
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}







//package com.company.jifiit.Salary;
//
//import android.annotation.SuppressLint;
//import android.content.SharedPreferences;
//import android.graphics.Typeface;
//import android.os.Build;
//import android.os.Bundle;
//import android.text.Spannable;
//import android.text.SpannableString;
//import android.text.style.StyleSpan;
//import android.util.Log;
//import android.view.View;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//import androidx.core.content.ContextCompat;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//
//import com.android.volley.Request;
//import com.android.volley.toolbox.JsonObjectRequest;
//import com.android.volley.toolbox.Volley;
//import com.company.jifiit.Constant;
//import com.company.jifiit.R;
//
//import org.json.JSONException;
//
//import java.math.BigInteger;
//import java.text.NumberFormat;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Locale;
//
//public class HoursIncentiveActivity extends AppCompatActivity {
//    TextView monthYearBtn, BackBtn, NextBtn, TotalHours,hoursIncetive,TotalUpgrades, Review_incentive,TotalSalary;
//    private Calendar calendar;
//    private Calendar currentCalendar;
//    View[] filledViews,tickMark;
//    View filled_view1, filled_view2, filled_view3, filled_view4, filled_view5, filled_view6;
//    View tick_mark1, tick_mark2, tick_mark3, tick_mark4, tick_mark5, tick_mark6;
//
//    @SuppressLint("MissingInflatedId")
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//        }
//        setContentView(R.layout.activity_hours_incentive);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        // Set title and back button colors
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setTitle("Hours Incentive");
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setDisplayShowHomeEnabled(true);
//        }
//        // Set toolbar color and back button color
//        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
//        toolbar.getNavigationIcon().setTint(getResources().getColor(android.R.color.black));
//
//        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
//        String idString = sp.getString("Id", "");
//        BigInteger Id = new BigInteger(idString);
//
//
//        monthYearBtn = findViewById(R.id.monthYearBtn);
//        BackBtn = findViewById(R.id.BackBtn);
//        NextBtn = findViewById(R.id.NextBtn);
//        initializeViews();
//        initializeCalendar();
//
//        // Set initial value for the MonthYear button
//        updateMonthYearButton();
//
//        setButtonListeners();
//    }
//    private void initializeViews() {
//        filled_view1 = findViewById(R.id.filled_view1);
//        filled_view2 = findViewById(R.id.filled_view2);
//        filled_view3 = findViewById(R.id.filled_view3);
//        filled_view4 = findViewById(R.id.filled_view4);
//        filled_view5 = findViewById(R.id.filled_view5);
//        filled_view6 = findViewById(R.id.filled_view6);
//
//        tick_mark1 = findViewById(R.id.tick_mark1);
//        tick_mark2 = findViewById(R.id.tick_mark2);
//        tick_mark3 = findViewById(R.id.tick_mark3);
//        tick_mark4 = findViewById(R.id.tick_mark4);
//        tick_mark5 = findViewById(R.id.tick_mark5);
//        tick_mark6 = findViewById(R.id.tick_mark7);
//        TotalHours = findViewById(R.id.totalHours);
//        hoursIncetive = findViewById(R.id.hourincentive);
//
//        filledViews = new View[5001];
//        for (int i = 50; i <= 5000; i += 50) {
//            // Generate the view ID dynamically
//            String viewIdName = "filled_view" + i;
//            int resId = getResources().getIdentifier(viewIdName, "id", getPackageName());
//
//            if (resId != 0) { // Check if the ID exists in the XML
//                filledViews[i / 50] = findViewById(resId);
//            }
//        }
//        tickMark = new View[5001];
//        for (int i = 50; i <= 5000; i += 50) {
//            // Generate the view ID dynamically
//            String viewIdName = "tick_mark" + i;
//            int resId = getResources().getIdentifier(viewIdName, "id", getPackageName());
//
//            if (resId != 0) { // Check if the ID exists in the XML
//                tickMark[i / 50] = findViewById(resId);
//            }
//        }
//    }
//    private void initializeCalendar() {
//        calendar = Calendar.getInstance();
//        currentCalendar = Calendar.getInstance();
//    }
//
//    private void setButtonListeners() {
//        monthYearBtn.setOnClickListener(v -> updateMonthYearButton());
//
//        BackBtn.setOnClickListener(v -> {
//            calendar.add(Calendar.MONTH, -1);
//            updateMonthYearButton();
//        });
//
//        NextBtn.setOnClickListener(v -> {
//            calendar.add(Calendar.MONTH, 1);
//            updateMonthYearButton();
//        });
//    }
//
//    private void updateMonthYearButton() {
//        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
//        String idString = sp.getString("Id", "");
//        BigInteger Id = new BigInteger(idString);
//        int year = calendar.get(Calendar.YEAR);
//        String monthName = new SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.getTime());
//        String yearText = String.valueOf(year);
//
//        SpannableString spannableString = new SpannableString(monthName + " " + yearText);
//        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, monthName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//        monthYearBtn.setText(spannableString);
//        BasicSalary(Id, spannableString);
//        // Hours(Id, spannableString);
//
//        // Check if the displayed month and year match the current month and year
//        if (calendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
//                calendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)) {
//            NextBtn.setVisibility(View.GONE);
//        } else {
//            NextBtn.setVisibility(View.VISIBLE);
//        }
//    }
//
//    private void BasicSalary(BigInteger id, SpannableString monthYear) {
//        String url = Constant.BasicSalary + id + "&monthYear=" + monthYear;
//
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
//            try {
//                String status = response.getString("status");
//                if ("success".equals(status)) {
//
//                    String ScrollViewOnHours = response.optString("hours", "0000");
//                    String hours = response.optString("Hours_Incentive", "0000");
//                    String totalHour = response.optString("Total_Hours", "00hr 00min");
//                    Log.d("Hour", "Total Hours: " + ScrollViewOnHours);
//                    TotalHours.setText(totalHour +"hours");
//                    hoursIncetive.setText(hours);
//                    int scrollViewHours = Integer.parseInt(totalHour);
//                    if (scrollViewHours >=125 ) {
//                        filled_view1.setBackgroundColor(getResources().getColor(R.color.orange));
//                        tick_mark1.setBackgroundResource(R.drawable.lock_open);
//                        filled_view2.setBackgroundColor(getResources().getColor(R.color.orange));
//                        tick_mark2.setBackgroundResource(R.drawable.lock_open);
//                        filled_view3.setBackgroundColor(getResources().getColor(R.color.orange));
//                        tick_mark3.setBackgroundResource(R.drawable.lock_open);
//                        filled_view4.setBackgroundColor(getResources().getColor(R.color.orange));
//                        tick_mark4.setBackgroundResource(R.drawable.lock_open);
//                        filled_view5.setBackgroundColor(getResources().getColor(R.color.orange));
//                        tick_mark5.setBackgroundResource(R.drawable.lock_open);
//                        filled_view6.setBackgroundColor(getResources().getColor(R.color.orange));
//                        tick_mark6.setBackgroundResource(R.drawable.slider_tick_mark);
//                    }else if (scrollViewHours >=110 ) {
//                        filled_view1.setBackgroundColor(getResources().getColor(R.color.orange));
//                        tick_mark1.setBackgroundResource(R.drawable.lock_open);
//                        filled_view2.setBackgroundColor(getResources().getColor(R.color.orange));
//                        tick_mark2.setBackgroundResource(R.drawable.lock_open);
//                        filled_view3.setBackgroundColor(getResources().getColor(R.color.orange));
//                        tick_mark3.setBackgroundResource(R.drawable.lock_open);
//                        filled_view4.setBackgroundColor(getResources().getColor(R.color.orange));
//                        tick_mark4.setBackgroundResource(R.drawable.lock_open);
//                        filled_view5.setBackgroundColor(getResources().getColor(R.color.orange));
//                        tick_mark5.setBackgroundResource(R.drawable.lock_open);
//                        filled_view6.setBackgroundColor(getResources().getColor(R.color.white));
//                        tick_mark6.setBackgroundResource(R.drawable.slider_tick_mark);
//                    }else if (scrollViewHours >=100 ) {
//                        filled_view1.setBackgroundColor(getResources().getColor(R.color.orange));
//                        tick_mark1.setBackgroundResource(R.drawable.lock_open);
//                        filled_view2.setBackgroundColor(getResources().getColor(R.color.orange));
//                        tick_mark2.setBackgroundResource(R.drawable.lock_open);
//                        filled_view3.setBackgroundColor(getResources().getColor(R.color.orange));
//                        tick_mark3.setBackgroundResource(R.drawable.lock_open);
//                        filled_view4.setBackgroundColor(getResources().getColor(R.color.orange));
//                        tick_mark4.setBackgroundResource(R.drawable.lock_open);
//                        filled_view5.setBackgroundColor(getResources().getColor(R.color.white));
//                        tick_mark5.setBackgroundResource(R.drawable.slider_tick_mark);
//                        filled_view6.setBackgroundColor(getResources().getColor(R.color.white));
//                        tick_mark6.setBackgroundResource(R.drawable.slider_tick_mark);
//                    }else if (scrollViewHours >=90 ) {
//                        filled_view1.setBackgroundColor(getResources().getColor(R.color.orange));
//                        tick_mark1.setBackgroundResource(R.drawable.lock_open);
//                        filled_view2.setBackgroundColor(getResources().getColor(R.color.orange));
//                        tick_mark2.setBackgroundResource(R.drawable.lock_open);
//                        filled_view3.setBackgroundColor(getResources().getColor(R.color.orange));
//                        tick_mark3.setBackgroundResource(R.drawable.lock_open);
//                        filled_view4.setBackgroundColor(getResources().getColor(R.color.white));
//                        tick_mark4.setBackgroundResource(R.drawable.slider_tick_mark);
//                        filled_view5.setBackgroundColor(getResources().getColor(R.color.white));
//                        tick_mark5.setBackgroundResource(R.drawable.slider_tick_mark);
//                        filled_view6.setBackgroundColor(getResources().getColor(R.color.white));
//                        tick_mark6.setBackgroundResource(R.drawable.slider_tick_mark);
//                    }else if (scrollViewHours >=80 ) {
//                        filled_view1.setBackgroundColor(getResources().getColor(R.color.orange));
//                        tick_mark1.setBackgroundResource(R.drawable.lock_open);
//                        filled_view2.setBackgroundColor(getResources().getColor(R.color.orange));
//                        tick_mark2.setBackgroundResource(R.drawable.lock_open);
//                        tick_mark3.setBackgroundResource(R.drawable.slider_tick_mark);
//                        filled_view4.setBackgroundColor(getResources().getColor(R.color.white));
//                        tick_mark4.setBackgroundResource(R.drawable.slider_tick_mark);
//                        filled_view5.setBackgroundColor(getResources().getColor(R.color.white));
//                        tick_mark5.setBackgroundResource(R.drawable.slider_tick_mark);
//                        filled_view6.setBackgroundColor(getResources().getColor(R.color.white));
//                        tick_mark6.setBackgroundResource(R.drawable.slider_tick_mark);
//                    }else if (scrollViewHours >=70 ) {
//                        filled_view1.setBackgroundColor(getResources().getColor(R.color.orange));
//                        tick_mark1.setBackgroundResource(R.drawable.lock_open);
//                        filled_view2.setBackgroundColor(getResources().getColor(R.color.white));
//                        tick_mark2.setBackgroundResource(R.drawable.slider_tick_mark);
//                        filled_view3.setBackgroundColor(getResources().getColor(R.color.white));
//                        tick_mark3.setBackgroundResource(R.drawable.slider_tick_mark);
//                        filled_view4.setBackgroundColor(getResources().getColor(R.color.white));
//                        tick_mark4.setBackgroundResource(R.drawable.slider_tick_mark);
//                        filled_view5.setBackgroundColor(getResources().getColor(R.color.white));
//                        tick_mark5.setBackgroundResource(R.drawable.slider_tick_mark);
//                        filled_view6.setBackgroundColor(getResources().getColor(R.color.white));
//                        tick_mark6.setBackgroundResource(R.drawable.slider_tick_mark);
//                    }else if (scrollViewHours < 70 ){
//                        filled_view1.setBackgroundColor(getResources().getColor(R.color.white));
//                        tick_mark1.setBackgroundResource(R.drawable.slider_tick_mark);
//                        filled_view2.setBackgroundColor(getResources().getColor(R.color.white));
//                        tick_mark2.setBackgroundResource(R.drawable.slider_tick_mark);
//                        filled_view3.setBackgroundColor(getResources().getColor(R.color.white));
//                        tick_mark3.setBackgroundResource(R.drawable.slider_tick_mark);
//                        filled_view4.setBackgroundColor(getResources().getColor(R.color.white));
//                        tick_mark4.setBackgroundResource(R.drawable.slider_tick_mark);
//                        filled_view5.setBackgroundColor(getResources().getColor(R.color.white));
//                        tick_mark5.setBackgroundResource(R.drawable.slider_tick_mark);
//                        filled_view6.setBackgroundColor(getResources().getColor(R.color.white));
//                        tick_mark6.setBackgroundResource(R.drawable.slider_tick_mark);
//                    }
//                }else {
//                    int scrollViewHours = Integer.parseInt("0000");
//                    if (scrollViewHours==0000) {
//                        filled_view1.setBackgroundColor(getResources().getColor(R.color.white));
//                        tick_mark1.setBackgroundResource(R.drawable.slider_tick_mark);
//                        filled_view2.setBackgroundColor(getResources().getColor(R.color.white));
//                        tick_mark2.setBackgroundResource(R.drawable.slider_tick_mark);
//                        filled_view3.setBackgroundColor(getResources().getColor(R.color.white));
//                        tick_mark3.setBackgroundResource(R.drawable.slider_tick_mark);
//                        filled_view4.setBackgroundColor(getResources().getColor(R.color.white));
//                        tick_mark4.setBackgroundResource(R.drawable.slider_tick_mark);
//                        filled_view5.setBackgroundColor(getResources().getColor(R.color.white));
//                        tick_mark5.setBackgroundResource(R.drawable.slider_tick_mark);
//                        filled_view6.setBackgroundColor(getResources().getColor(R.color.white));
//                        tick_mark6.setBackgroundResource(R.drawable.slider_tick_mark);
//                    }
//                }
//            } catch (JSONException e) {
//                Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
//            }
//        }, error -> {
//            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
//        });
//
//        Volley.newRequestQueue(this).add(jsonObjectRequest);
//    }
//
//    @Override
//    public boolean onSupportNavigateUp() {
//        onBackPressed();
//        return true;
//    }
//}
