package com.company.jifiit.Salary;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Constant;
import com.company.jifiit.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SalaryActivity extends AppCompatActivity {

    View filled_view1, filled_view2, filled_view3, filled_view4, filled_view5, filled_view6, filled_view7;
    View tick_mark1, tick_mark2, tick_mark3, tick_mark4, tick_mark5, tick_mark6, tick_mark7;

    TextView monthYearBtn, BackBtn, NextBtn, TotalAmounts, TotalHours, TotalUpgrades, Review_incentive;
    TextView TotalSalary, Ratings, HoursIncentive,hoursIncetive;
    Button basicSalary;
    TextView Leave;
    LinearLayout Footer;
    TextView Review10,Review30,Review60,Review90,Incentive_10,Incentive_30,Incentive_60,Incentive_90;
    private Calendar calendar;
    private Calendar currentCalendar;

    View[] filledViews,tickMark;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_salary);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Salary");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String idString = sp.getString("Id", "");
        BigInteger Id = new BigInteger(idString);
//       ImageView back = findViewById(R.id.back_button);
//        back.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//
//            }
//        });
        basicSalary = findViewById(R.id.basicSalaryBtn);

        initializeViews();
        initializeCalendar();

        // Set initial value for the MonthYear button
        updateMonthYearButton();

        setButtonListeners();

    }

    private void initializeViews() {
        filled_view1 = findViewById(R.id.filled_view1);
        filled_view2 = findViewById(R.id.filled_view2);
        filled_view3 = findViewById(R.id.filled_view3);
        filled_view4 = findViewById(R.id.filled_view4);
        filled_view5 = findViewById(R.id.filled_view5);
        filled_view6 = findViewById(R.id.filled_view6);
        filled_view7 = findViewById(R.id.filled_view7);

        tick_mark1 = findViewById(R.id.tick_mark1);
        tick_mark2 = findViewById(R.id.tick_mark2);
        tick_mark3 = findViewById(R.id.tick_mark3);
        tick_mark4 = findViewById(R.id.tick_mark4);
        tick_mark5 = findViewById(R.id.tick_mark5);
        tick_mark6 = findViewById(R.id.tick_mark6);
        tick_mark7 = findViewById(R.id.tick_mark7);


filledViews = new View[5001];
        for (int i = 50; i <= 5000; i += 50) {
            // Generate the view ID dynamically
            String viewIdName = "filled_view" + i;
            int resId = getResources().getIdentifier(viewIdName, "id", getPackageName());

            if (resId != 0) { // Check if the ID exists in the XML
                filledViews[i / 50] = findViewById(resId);
            }
        }
        tickMark = new View[5001];
        for (int i = 50; i <= 5000; i += 50) {
            // Generate the view ID dynamically
            String viewIdName = "tick_mark" + i;
            int resId = getResources().getIdentifier(viewIdName, "id", getPackageName());

            if (resId != 0) { // Check if the ID exists in the XML
                tickMark[i / 50] = findViewById(resId);
            }
        }



        TotalHours = findViewById(R.id.totalHours);
        TotalAmounts = findViewById(R.id.workingAmount);
        TotalUpgrades = findViewById(R.id.upgrade);
        Review_incentive = findViewById(R.id.ReviewIncentive);
        TotalSalary = findViewById(R.id.totalSalary);
        Ratings = findViewById(R.id.RatingText);

        monthYearBtn = findViewById(R.id.monthYearBtn);
        BackBtn = findViewById(R.id.backBtn);
        NextBtn = findViewById(R.id.nextBtn);


        HoursIncentive = findViewById(R.id.HoursIncentive);
        hoursIncetive = findViewById(R.id.hourincentive);
        Leave = findViewById(R.id.leaveBtn);
        Footer = findViewById(R.id.footer);


        Review10 = findViewById(R.id.Review10);
        Review30 = findViewById(R.id.Review30);
        Review60 = findViewById(R.id.Review60);
        Review90 = findViewById(R.id.Review90);

        Incentive_10 = findViewById(R.id.Incentive_10);
        Incentive_30 = findViewById(R.id.Incentive_30);
        Incentive_60 = findViewById(R.id.Incentive_60);
        Incentive_90 = findViewById(R.id.Incentive_90);

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

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                String status = response.getString("status");
                if ("success".equals(status)) {
                    int basicSalaryValue = response.optInt("basics_salary", 0);
                    String workingAmount = response.optString("total_cut_of_salary", "0000");
                    String leaveStatus = response.optString("deduction_salary", "0");
                    String hours = response.optString("Hours_Incentive", "0000");
                    String totalHour = response.optString("Total_Hours", "00hr 00min");
                    String total = response.optString("TotalAmount", "0000");
                    String paymentStatus = response.optString("payment_status", "");
//                    Log.d("PaymentStatus", "Payment Status: " + paymentStatus);
                    String Upgrade = response.optString("incentiveOnUpgrade", "0000");
                    String Rating_Incentive = response.optString("Rating_Incentive", "0000");
                    String Rating = response.optString("ratings", "0000");
//                    String Rating = "37";

                    String ScrollViewOnHours = response.optString("hours", "0000");

                    String review_10 = response.optString("Review_10", "0000");
                    String review_30 = response.optString("Review_30", "0000");
                    String review_60 = response.optString("Review_60", "0000");
                    String review_90 = response.optString("Review_90", "0000");


                    String incentive_10 = response.optString("incentive_10", "0000");
                    String incentive_30 = response.optString("incentive_30", "0000");
                    String incentive_60 = response.optString("incentive_60", "0000");
                    String incentive_90 = response.optString("incentive_90", "0000");



                    HoursIncentive.setText(hours);
                    hoursIncetive.setText(hours);
                    TotalHours.setText(totalHour +"hours");
                    basicSalary.setText("Basic Salary: " + basicSalaryValue);
                    TotalAmounts.setText(workingAmount);
                    Leave.setText(leaveStatus);
                    TotalSalary.setText(total);
                    TotalUpgrades.setText(Upgrade);
                    Review_incentive.setText(Rating_Incentive);
                    Ratings.setText("Ratings: "+Rating);


                    Review10.setText("Completed "+review_10+" massage");
                    Review30.setText("Completed "+review_30+" massage");
                    Review60.setText("Completed "+review_60+" massage");
                    Review90.setText("Completed "+review_90+" massage");

                    Incentive_10.setText(incentive_10);
                    Incentive_30.setText(incentive_30);
                    Incentive_60.setText(incentive_60);
                    Incentive_90.setText(incentive_90);


                    int color; // Variable to store the color resource

                    switch (paymentStatus.toLowerCase()) {
                        case "paid":
                            color = R.color.Green;
                            break;
                        case "process":
                            color = R.color.orange;
                            break;
                        case "hold":
                            color = R.color.Red;
                            break;
                        default:
                            color = R.color.ShapGreen; // Optional: Set a default color
                            break;
                    }

// Set the footer background color
                    Footer.setBackgroundColor(ContextCompat.getColor(this, color));

                    int scrollViewHours = Integer.parseInt(ScrollViewOnHours);
                    if (scrollViewHours >= 125) {
                        filled_view1.setBackgroundColor(getResources().getColor(R.color.orange));
                        tick_mark1.setBackgroundResource(R.drawable.lock_open);
                        filled_view2.setBackgroundColor(getResources().getColor(R.color.orange));
                        tick_mark2.setBackgroundResource(R.drawable.lock_open);
                        filled_view3.setBackgroundColor(getResources().getColor(R.color.orange));
                        tick_mark3.setBackgroundResource(R.drawable.lock_open);
                        filled_view4.setBackgroundColor(getResources().getColor(R.color.orange));
                        tick_mark4.setBackgroundResource(R.drawable.lock_open);
                        filled_view5.setBackgroundColor(getResources().getColor(R.color.orange));
                        tick_mark5.setBackgroundResource(R.drawable.lock_open);
                        filled_view6.setBackgroundColor(getResources().getColor(R.color.orange));
                        tick_mark6.setBackgroundResource(R.drawable.lock_open);
                        filled_view7.setBackgroundColor(getResources().getColor(R.color.orange));
                        tick_mark7.setBackgroundResource(R.drawable.lock_open);
                    } else if (scrollViewHours >=110 ) {
                        filled_view1.setBackgroundColor(getResources().getColor(R.color.orange));
                        tick_mark1.setBackgroundResource(R.drawable.lock_open);
                        filled_view2.setBackgroundColor(getResources().getColor(R.color.orange));
                        tick_mark2.setBackgroundResource(R.drawable.lock_open);
                        filled_view3.setBackgroundColor(getResources().getColor(R.color.orange));
                        tick_mark3.setBackgroundResource(R.drawable.lock_open);
                        filled_view4.setBackgroundColor(getResources().getColor(R.color.orange));
                        tick_mark4.setBackgroundResource(R.drawable.lock_open);
                        filled_view5.setBackgroundColor(getResources().getColor(R.color.orange));
                        tick_mark5.setBackgroundResource(R.drawable.lock_open);
                        filled_view6.setBackgroundColor(getResources().getColor(R.color.orange));
                        tick_mark6.setBackgroundResource(R.drawable.lock_open);
                        filled_view7.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark7.setBackgroundResource(R.drawable.slider_tick_mark);
                    }else if (scrollViewHours >=100 ) {
                        filled_view1.setBackgroundColor(getResources().getColor(R.color.orange));
                        tick_mark1.setBackgroundResource(R.drawable.lock_open);
                        filled_view2.setBackgroundColor(getResources().getColor(R.color.orange));
                        tick_mark2.setBackgroundResource(R.drawable.lock_open);
                        filled_view3.setBackgroundColor(getResources().getColor(R.color.orange));
                        tick_mark3.setBackgroundResource(R.drawable.lock_open);
                        filled_view4.setBackgroundColor(getResources().getColor(R.color.orange));
                        tick_mark4.setBackgroundResource(R.drawable.lock_open);
                        filled_view5.setBackgroundColor(getResources().getColor(R.color.orange));
                        tick_mark5.setBackgroundResource(R.drawable.lock_open);
                        filled_view6.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark6.setBackgroundResource(R.drawable.slider_tick_mark);
                        filled_view7.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark7.setBackgroundResource(R.drawable.slider_tick_mark);
                    }else if (scrollViewHours >=90 ) {
                        filled_view1.setBackgroundColor(getResources().getColor(R.color.orange));
                        tick_mark1.setBackgroundResource(R.drawable.lock_open);
                        filled_view2.setBackgroundColor(getResources().getColor(R.color.orange));
                        tick_mark2.setBackgroundResource(R.drawable.lock_open);
                        filled_view3.setBackgroundColor(getResources().getColor(R.color.orange));
                        tick_mark3.setBackgroundResource(R.drawable.lock_open);
                        filled_view4.setBackgroundColor(getResources().getColor(R.color.orange));
                        tick_mark4.setBackgroundResource(R.drawable.lock_open);
                        filled_view5.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark5.setBackgroundResource(R.drawable.slider_tick_mark);
                        filled_view6.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark6.setBackgroundResource(R.drawable.slider_tick_mark);
                        filled_view7.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark7.setBackgroundResource(R.drawable.slider_tick_mark);
                    }else if (scrollViewHours >=80 ) {
                        filled_view1.setBackgroundColor(getResources().getColor(R.color.orange));
                        tick_mark1.setBackgroundResource(R.drawable.lock_open);
                        filled_view2.setBackgroundColor(getResources().getColor(R.color.orange));
                        tick_mark2.setBackgroundResource(R.drawable.lock_open);
                        filled_view3.setBackgroundColor(getResources().getColor(R.color.orange));
                        tick_mark3.setBackgroundResource(R.drawable.lock_open);
                        filled_view4.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark4.setBackgroundResource(R.drawable.slider_tick_mark);
                        filled_view5.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark5.setBackgroundResource(R.drawable.slider_tick_mark);
                        filled_view6.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark6.setBackgroundResource(R.drawable.slider_tick_mark);
                        filled_view7.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark7.setBackgroundResource(R.drawable.slider_tick_mark);
                    }else if (scrollViewHours >=70 ) {
                        filled_view1.setBackgroundColor(getResources().getColor(R.color.orange));
                        tick_mark1.setBackgroundResource(R.drawable.lock_open);
                        filled_view2.setBackgroundColor(getResources().getColor(R.color.orange));
                        tick_mark2.setBackgroundResource(R.drawable.lock_open);
                        tick_mark3.setBackgroundResource(R.drawable.slider_tick_mark);
                        filled_view4.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark4.setBackgroundResource(R.drawable.slider_tick_mark);
                        filled_view5.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark5.setBackgroundResource(R.drawable.slider_tick_mark);
                        filled_view6.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark6.setBackgroundResource(R.drawable.slider_tick_mark);
                        filled_view7.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark7.setBackgroundResource(R.drawable.slider_tick_mark);
                    }else if (scrollViewHours >=60 ) {
                        filled_view1.setBackgroundColor(getResources().getColor(R.color.orange));
                        tick_mark1.setBackgroundResource(R.drawable.lock_open);
                        filled_view2.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark2.setBackgroundResource(R.drawable.slider_tick_mark);
                        filled_view3.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark3.setBackgroundResource(R.drawable.slider_tick_mark);
                        filled_view4.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark4.setBackgroundResource(R.drawable.slider_tick_mark);
                        filled_view5.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark5.setBackgroundResource(R.drawable.slider_tick_mark);
                        filled_view6.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark6.setBackgroundResource(R.drawable.slider_tick_mark);
                        filled_view7.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark7.setBackgroundResource(R.drawable.slider_tick_mark);
                    }else if (scrollViewHours < 60 ){
                        filled_view1.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark1.setBackgroundResource(R.drawable.slider_tick_mark);
                        filled_view2.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark2.setBackgroundResource(R.drawable.slider_tick_mark);
                        filled_view3.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark3.setBackgroundResource(R.drawable.slider_tick_mark);
                        filled_view4.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark4.setBackgroundResource(R.drawable.slider_tick_mark);
                        filled_view5.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark5.setBackgroundResource(R.drawable.slider_tick_mark);
                        filled_view6.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark6.setBackgroundResource(R.drawable.slider_tick_mark);
                        filled_view7.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark7.setBackgroundResource(R.drawable.slider_tick_mark);
                    }


                    int Ratings = Integer.parseInt(Rating);
                    if (Ratings>=100)
                    {
                        for (int i = 1; i <= 100; i++) { // Change 100 to 1000 or 5000 as needed
                            if (filledViews[i] != null) {
                                filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                            }
                            if (tickMark[i] != null) {
                                tickMark[i].setBackgroundResource(R.drawable.lock_open);
                            }
                        }
                    } else if (Ratings >= 99) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 99) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    } else if (Ratings >= 98) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 98) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }else if (Ratings >= 97) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 97) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 96) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 96) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 95) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 95) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 94) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 94) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 93) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 93) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 92) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 92) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 91) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 91) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 90) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 90) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 89) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 89) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 88) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 88) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 87) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 87) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 86) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 86) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 85) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 85) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 84) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 84) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 83) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 83) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 82) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 82) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 81) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 81) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 80) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 80) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 79) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 79) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 78) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 78) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 77) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 77) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 76) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 76) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 75) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 75) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 74) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 74) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 73) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 73) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 72) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 72) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 71) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 71) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 70) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 70) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 69) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 69) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 68) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 68) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 67) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 67) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 66) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 66) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 65) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 65) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 64) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 64) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }
                    }
                    else if (Ratings >= 63) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 63) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 62) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 62) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 61) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 61) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 60) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 60) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 59) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 59) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 58) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 58) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 57) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 57) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 56) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 56) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 55) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 55) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 54) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 54) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 53) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 53) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 52) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 52) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 51) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 51) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 50) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 50) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 49) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 49) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 48) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 48) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 47) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 47) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }else if (Ratings >= 46) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 46) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 45) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 45) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 44) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 44) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 43) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 43) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 42) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 42) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 41) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 41) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 40) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 40) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 39) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 39) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 38) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 38) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 37) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 37) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 36) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 36) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 35) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 10) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 34) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 34) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 33) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 33) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 32) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 32) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 31) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 31) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 30) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 30) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 29) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 29) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 28) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 28) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 27) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 27) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 26) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 26) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 25) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 25) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 24) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 24) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 23) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 23) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 22) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 22) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 21) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 21) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 20) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 20) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 19) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 19) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 18) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 18) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 17) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 17) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 16) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 16) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 15) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 15) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 14) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 14) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 13) {

                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 13) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }
                    }
                    else if (Ratings >= 12) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 12) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }
                    else if (Ratings >= 11) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 11) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }

                    }

                    else if (Ratings>=10) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 10) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }
                    } else if (Ratings>=9) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 9) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }
                    }else if (Ratings>=8) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 8) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }
                    }else if (Ratings>=7) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 7) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }
                    }else if (Ratings>=6) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 6) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }
                    }else if (Ratings>=5) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 5) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }
                    }else if (Ratings>=4) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 4) {
                                    // For the first 6 elements, set orange and open lock
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else {
                                    // For remaining elements, set white and slider tick mark
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }
                    }else if (Ratings >= 3) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 3) { // For the first 3 elements
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else { // For the remaining elements
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }
                    }else if (Ratings>=2) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 2) { // For the first 3 elements
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else { // For the remaining elements
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }
                    }else if (Ratings>=1) {
                        for (int i = 1; i <= 100; i++) { // Adjust the upper limit as needed
                            if (filledViews[i] != null && tickMark[i] != null) {
                                if (i <= 1) { // For the first 3 elements
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                    tickMark[i].setBackgroundResource(R.drawable.lock_open);
                                } else { // For the remaining elements
                                    filledViews[i].setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                                    tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                                }
                            }
                        }
                    }else if (Ratings==0) {
                        for (int i = 1; i <= 100; i++) { // Change 100 to 1000 or 5000 as needed
                            if (filledViews[i] != null) {
                                filledViews[i].setBackgroundColor(getResources().getColor(R.color.white));
                            }
                            if (tickMark[i] != null) {
                                tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                            }
                        }
                    }
                } else {
                    basicSalary.setText("Basic Salary: 0000");
                    TotalAmounts.setText("0000");
                    Leave.setText("0000");
                    HoursIncentive.setText("0000");
                    hoursIncetive.setText("0000");
                    TotalHours.setText("00hr 00min");
                    TotalSalary.setText("0000");
                    TotalUpgrades.setText("0000");
                    Ratings.setText("Ratings: 00");

                    Review_incentive.setText("0000");


                    Review10.setText("Completed 0 massage");
                    Review30.setText("Completed 0 massage");
                    Review60.setText("Completed 0 massage");
                    Review90.setText("Completed 0 massage");

                    Incentive_10.setText("0000");
                    Incentive_30.setText("0000");
                    Incentive_60.setText("0000");
                    Incentive_90.setText("0000");
                    Review_incentive.setText("0000");
                    String paymentStatus = "0000";
                    if (paymentStatus=="0000"){
                        Footer.setBackgroundColor(ContextCompat.getColor(this, R.color.Red));
                    }
                    int scrollViewHours = Integer.parseInt("0000");
                    if (scrollViewHours==0000) {
                        filled_view1.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark1.setBackgroundResource(R.drawable.slider_tick_mark);
                        filled_view2.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark2.setBackgroundResource(R.drawable.slider_tick_mark);
                        filled_view3.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark3.setBackgroundResource(R.drawable.slider_tick_mark);
                        filled_view4.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark4.setBackgroundResource(R.drawable.slider_tick_mark);
                        filled_view5.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark5.setBackgroundResource(R.drawable.slider_tick_mark);
                        filled_view6.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark6.setBackgroundResource(R.drawable.slider_tick_mark);
                        filled_view7.setBackgroundColor(getResources().getColor(R.color.white));
                        tick_mark7.setBackgroundResource(R.drawable.slider_tick_mark);
                    }
                    int Ratings = Integer.parseInt("00");
                    if(Ratings==0) {
                        for (int i = 1; i <= 100; i++) { // Change 100 to 1000 or 5000 as needed
                            if (filledViews[i] != null) {
                                filledViews[i].setBackgroundColor(getResources().getColor(R.color.white));
                            }
                            if (tickMark[i] != null) {
                                tickMark[i].setBackgroundResource(R.drawable.slider_tick_mark);
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
        });

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }




    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
