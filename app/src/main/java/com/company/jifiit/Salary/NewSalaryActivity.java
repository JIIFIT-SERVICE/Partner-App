package com.company.jifiit.Salary;

import android.annotation.SuppressLint;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;

import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Constant;
import com.company.jifiit.R;
import com.company.jifiit.ReferDialog;


import org.json.JSONException;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.text.NumberFormat;
import java.util.Locale;

public class NewSalaryActivity extends AppCompatActivity {


    TextView monthYearBtn, BackBtn, NextBtn, TotalAmounts,TotalUpgrades, Review_incentive,TotalSalary;
    TextView HoursIncentive;
    TextView basicSalary;
    TextView Leave;
    LinearLayout Footer;

    private Calendar calendar;
    private Calendar currentCalendar;




    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        setContentView(R.layout.activity_new_salary);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set title and back button colors
// In onCreate()
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Salary");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            // Safe check for navigation icon
            if (toolbar.getNavigationIcon() != null) {
                toolbar.getNavigationIcon().setTint(getResources().getColor(android.R.color.black));
            }
        }        // Set toolbar color and back button color
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        toolbar.getNavigationIcon().setTint(getResources().getColor(android.R.color.black));
//        ReferDialog.showReferDialog(this);
        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String idString = sp.getString("Id", "");
        BigInteger Id = new BigInteger(idString);

        monthYearBtn = findViewById(R.id.monthYearBtn);
        BackBtn = findViewById(R.id.BackBtn);
        NextBtn = findViewById(R.id.NextBtn);
        basicSalary = findViewById(R.id.basicSalaryBtn);


        TotalAmounts = findViewById(R.id.workingAmount);
        HoursIncentive = findViewById(R.id.HoursIncentive);
        TotalUpgrades = findViewById(R.id.upgrade);
        Review_incentive = findViewById(R.id.ReviewIncentive);
        Leave = findViewById(R.id.leaveBtn);
        TotalSalary = findViewById(R.id.totalSalary);

        Footer = findViewById(R.id.footer);
//      initializeViews();
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

    // Use explicit locale for formatting
    private void updateMonthYearButton() {
        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String idString = sp.getString("Id", "");
        if (idString.isEmpty()) {
            return;
        }

        try {
            BigInteger Id = new BigInteger(idString);
            int year = calendar.get(Calendar.YEAR);

            // Use explicit locale for month formatting
            String monthName = new SimpleDateFormat("MMM", Locale.ENGLISH).format(calendar.getTime());
            String yearText = String.valueOf(year);

            SpannableString spannableString = new SpannableString(monthName + " " + yearText);
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, monthName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            if (monthYearBtn != null) {
                monthYearBtn.setText(spannableString);
            }

            BasicSalary(Id, spannableString);

            // Check if the displayed month and year match the current month and year
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

    private void BasicSalary(BigInteger id, SpannableString monthYear) {
        String url = Constant.BasicSalary + id + "&monthYear=" + monthYear;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                String status = response.getString("status");
                if ("success".equals(status)) {
                    // Safe parsing with defaults
                    int basicSalaryValue = response.optInt("basics_salary", 0);
                    String workingAmount = response.optString("total_cut_of_salary", "0");
                    String leaveStatus = response.optString("deduction_salary", "0");
                    String hours = response.optString("Hours_Incentive", "0");
                    String total = response.optString("TotalAmount", "0");
                    String paymentStatus = response.optString("payment_status", "unknown");
                    String Upgrade = response.optString("incentiveOnUpgrade", "0");
                    String Rating_Incentive = response.optString("Rating_Incentive", "0");

                    // Safe number formatting
                    try {
                        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
                        String formattedSalary = "₹" + formatter.format(basicSalaryValue);
                        basicSalary.setText(formattedSalary);

                        // Parse total amount safely
                        double totalAmount = 0;
                        try {
                            totalAmount = Double.parseDouble(total);
                        } catch (NumberFormatException e) {
                            totalAmount = 0;
                        }
                        String formattedTotal = "₹" + formatter.format(totalAmount);
                        TotalSalary.setText(formattedTotal);

                    } catch (Exception e) {
                        basicSalary.setText("₹0");
                        TotalSalary.setText("₹0");
                    }

                    // Set other text views with safe defaults
                    HoursIncentive.setText(hours.isEmpty() ? "0" : hours);
                    TotalAmounts.setText(workingAmount.isEmpty() ? "0" : workingAmount);
                    Leave.setText(leaveStatus.equals("0") ? "0" : leaveStatus);
                    TotalUpgrades.setText(Upgrade.isEmpty() ? "0" : Upgrade);
                    Review_incentive.setText(Rating_Incentive.isEmpty() ? "0" : Rating_Incentive);

                    // Handle payment status
                    int color = R.color.ShapGreen; // Default color
                    try {
                        switch (paymentStatus.toLowerCase()) {
                            case "paid": color = R.color.Green; break;
                            case "process": color = R.color.orange; break;
                            case "hold": color = R.color.Red; break;
                        }
                    } catch (Exception e) {
                        // Keep default color
                    }

                    if (Footer != null) {
                        Footer.setBackgroundColor(ContextCompat.getColor(this, color));
                    }
                } else {
                    // Set default values safely
                    setDefaultValues();
                }
            } catch (JSONException e) {
                setDefaultValues();
                Toast.makeText(getApplicationContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            setDefaultValues();
            Toast.makeText(getApplicationContext(), "Network error", Toast.LENGTH_SHORT).show();
        });

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void setDefaultValues() {
        runOnUiThread(() -> {
            try {
                basicSalary.setText("₹0");
                TotalAmounts.setText("0");
                Leave.setText("0");
                HoursIncentive.setText("0");
                TotalUpgrades.setText("0");
                Review_incentive.setText("0");
                TotalSalary.setText("₹0");
                if (Footer != null) {
                    Footer.setBackgroundColor(ContextCompat.getColor(this, R.color.Red));
                }
            } catch (Exception e) {
                // Handle any UI thread exceptions
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.salary_manue,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.hours) {
            Intent intent = new Intent(NewSalaryActivity.this, HoursIncentiveActivity.class);
            startActivity(intent);
            return true;
        }else if (id == R.id.review) {
            Intent intent = new Intent(NewSalaryActivity.this, RatingIncentiveActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.upgrade) {
            Intent intent = new Intent(NewSalaryActivity.this, UpgradeIncentiveActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel all Volley requests when activity is destroyed
        if (Volley.newRequestQueue(this) != null) {
            Volley.newRequestQueue(this).getCache().clear();
        }
    }
}
