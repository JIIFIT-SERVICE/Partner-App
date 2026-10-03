package com.company.jifiit.Salary;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Constant;
import com.company.jifiit.R;

import org.json.JSONException;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class UpgradeIncentiveActivity extends AppCompatActivity {

    private Calendar calendar;
    private Calendar currentCalendar;
    TextView monthYearBtn, BackBtn, NextBtn, TotalAmounts,TotalUpgrades, Review_incentive,TotalSalary;
    TextView Review10, Review30, Review60, Review90, Incentive_10, Incentive_30, Incentive_60, Incentive_90;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }


        setContentView(R.layout.activity_upgrade_incentive);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set title and back button colors
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Set toolbar color and back button color
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        toolbar.getNavigationIcon().setTint(getResources().getColor(android.R.color.black));

        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String idString = sp.getString("Id", "");
        BigInteger Id = new BigInteger(idString);

        monthYearBtn = findViewById(R.id.monthYearBtn);
        BackBtn = findViewById(R.id.BackBtn);
        NextBtn = findViewById(R.id.NextBtn);

        Review10 = findViewById(R.id.Review10);
        Review30 = findViewById(R.id.Review30);
        Review60 = findViewById(R.id.Review60);
        Review90 = findViewById(R.id.Review90);

        Incentive_10 = findViewById(R.id.Incentive_10);
        Incentive_30 = findViewById(R.id.Incentive_30);
        Incentive_60 = findViewById(R.id.Incentive_60);
        Incentive_90 = findViewById(R.id.Incentive_90);

        initializeCalendar();

        // Set initial value for the MonthYear button
        updateMonthYearButton();

        setButtonListeners();
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
        BigInteger Id = new BigInteger(idString);
        int year = calendar.get(Calendar.YEAR);
        String monthName = new SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.getTime());
        String yearText = String.valueOf(year);

        SpannableString spannableString = new SpannableString(monthName + " " + yearText);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, monthName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        monthYearBtn.setText(spannableString);
        BasicSalary(Id, spannableString);
        // Hours(Id, spannableString);

        // Check if the displayed month and year match the current month and year
        if (calendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)) {
            NextBtn.setVisibility(View.GONE);
        } else {
            NextBtn.setVisibility(View.VISIBLE);
        }
    }

    private void BasicSalary(BigInteger id, SpannableString monthYear) {
        String url = Constant.BasicSalary + id + "&monthYear=" + monthYear;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    String status = response.optString("status", "error");

                    if ("success".equals(status)) {
                        // Use helper method to fetch values with default fallback
                        String review_10 = response.optString("Review_10", "0");
                        String review_30 = response.optString("Review_30", "0");
                        String review_60 = response.optString("Review_60", "0");
                        String review_90 = response.optString("Review_90", "0");

                        String incentive_10 = response.optString("incentive_10", "0000");
                        String incentive_30 = response.optString("incentive_30", "0000");
                        String incentive_60 = response.optString("incentive_60", "0000");
                        String incentive_90 = response.optString("incentive_90", "0000");

                        // Set Text
                        Review10.setText(String.format("Completed %s massage", review_10));
                        Review30.setText(String.format("Completed %s massage", review_30));
                        Review60.setText(String.format("Completed %s massage", review_60));
                        Review90.setText(String.format("Completed %s massage", review_90));

                        Incentive_10.setText(incentive_10);
                        Incentive_30.setText(incentive_30);
                        Incentive_60.setText(incentive_60);
                        Incentive_90.setText(incentive_90);

                    } else {
                        // Handle 'error' status or unexpected cases
                        setDefaultValues();
                    }

                },
                error -> {
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                    setDefaultValues();
                }
        );

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    // Helper method to reset values to default
    private void setDefaultValues() {
        Review10.setText("Completed 0 massage");
        Review30.setText("Completed 0 massage");
        Review60.setText("Completed 0 massage");
        Review90.setText("Completed 0 massage");

        Incentive_10.setText("0000");
        Incentive_30.setText("0000");
        Incentive_60.setText("0000");
        Incentive_90.setText("0000");
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
