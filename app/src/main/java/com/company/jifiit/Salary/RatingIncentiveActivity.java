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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Adapter.RewardAdapter;
import com.company.jifiit.Constant;
import com.company.jifiit.R;
import com.company.jifiit.model.RewardModel;

import org.json.JSONException;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import jp.wasabeef.recyclerview.animators.FlipInLeftYAnimator;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation;

public class RatingIncentiveActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RewardAdapter adapter;
    private List<RewardModel> rewardList;
    TextView pointsCounter, ReviewCounter,levelText;
    private Calendar calendar;
    private Calendar currentCalendar;
    TextView monthYearBtn, BackBtn, NextBtn;
    int points = 0;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        setContentView(R.layout.activity_rating_incentive);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        toolbar.getNavigationIcon().setTint(getResources().getColor(android.R.color.black));

        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String idString = sp.getString("Id", "");
        BigInteger Id = new BigInteger(idString);



        monthYearBtn = findViewById(R.id.monthYearBtn);
        BackBtn = findViewById(R.id.BackBtn);
        NextBtn = findViewById(R.id.NextBtn);
        recyclerView = findViewById(R.id.horizontalRecyclerView);
        pointsCounter = findViewById(R.id.pointsCounter);
        levelText = findViewById(R.id.levelText);

        ReviewCounter = findViewById(R.id.ReviewCounter);
        initializeCalendar();

        // Set initial value for the MonthYear button
        updateMonthYearButton();

        setButtonListeners();
        ImageButton btnSlideLeft = findViewById(R.id.btnSlideLeft);
        ImageButton btnSlideRight = findViewById(R.id.btnSlideRight);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        rewardList = new ArrayList<>();


        btnSlideLeft.setOnClickListener(v -> {
            int currentPosition = layoutManager.findFirstVisibleItemPosition();
            if (currentPosition > 0) {
                recyclerView.smoothScrollToPosition(currentPosition - 1);
            }
        });

        btnSlideRight.setOnClickListener(v -> {
            int currentPosition = layoutManager.findFirstVisibleItemPosition();
            if (currentPosition < recyclerView.getAdapter().getItemCount() - 1) {
                recyclerView.smoothScrollToPosition(currentPosition + 1);
            }
        });

        // 🔥 Fetch points


    }

    // 🔥 Function to fetch points

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

                    // Fetching values from the response
                    String ratingIncentive = response.optString("Rating_Incentive", "0000");
                    String rating = response.optString("ratings", "0000");

                    // Handle ratingIncentive (which might contain decimals)
                    if ("0000.00".equals(ratingIncentive) || "0000".equals(ratingIncentive)) {
                        pointsCounter.setText("₹0");
                    } else {
                        // Remove any decimal part and parse as integer
                        String cleanIncentive = ratingIncentive.split("\\.")[0];
                        try {
                            int incentiveValue = Integer.parseInt(cleanIncentive);
                            pointsCounter.setText("₹" + incentiveValue);
                        } catch (NumberFormatException e) {
                            pointsCounter.setText("₹0");
                            Log.e("BasicSalary", "Failed to parse incentive: " + ratingIncentive, e);
                        }
                    }

                    // Handle rating
                    try {
                        int points = Integer.parseInt(rating);
                        ReviewCounter.setText("⭐" + points);

                        // Track the last unlocked level
                        int lastUnlockedLevel = 0;

                        // Loop through the levels
                        for (int i = 1; i <= 15; i++) {
                            if (points >= i * 10) {
                                rewardList.add(new RewardModel(R.mipmap.unlocked_box_removebg_preview, "Level " + i));
                                lastUnlockedLevel = i;  // Store the last unlocked level (1-based index)
                            } else {
                                rewardList.add(new RewardModel(R.mipmap.locked_box_copy_removebg_preview, "Level " + i));
                            }
                        }

                        adapter = new RewardAdapter(this, rewardList);
                        recyclerView.setAdapter(adapter);

                        // Scroll to the last unlocked level
                        int finalUnlockedLevel = lastUnlockedLevel - 1;  // Adjust index for RecyclerView
                        recyclerView.post(() -> recyclerView.scrollToPosition(finalUnlockedLevel));

                        // Display the achieved level text
                        if (lastUnlockedLevel > 0) {
                            String suffix;
                            if (lastUnlockedLevel == 1) {
                                suffix = "st";
                            } else if (lastUnlockedLevel == 2) {
                                suffix = "nd";
                            } else if (lastUnlockedLevel == 3) {
                                suffix = "rd";
                            } else {
                                suffix = "th";
                            }
                            levelText.setText("Achieved " + lastUnlockedLevel + suffix + " Level");
                        } else {
                            levelText.setText("Achievement Level 0");
                        }

                    } catch (NumberFormatException e) {
                        ReviewCounter.setText("⭐0");
                        Log.e("BasicSalary", "Failed to parse rating: " + rating, e);
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
        });

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }



    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
