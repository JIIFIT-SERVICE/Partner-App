package com.company.jifiit.MyPerformance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Constant;
import com.company.jifiit.R;
import com.company.jifiit.ReferDialog;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class MyPerformanceActivity extends AppCompatActivity {

    private LineChart chart;
    private Button dateButton;
    private int selectedMonth;
    private int selectedYear;
    private Calendar calendar;

    TextView RevToday,RevTotal,UpgradToday,UpgradTotal,HoursToday,HoursTotal,Nodata;
    private boolean isMonthYearClicked = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_my_performance);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("My Performance");
//        ReferDialog.showReferDialog(this);
        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String idString = sp.getString("Id", "");
        BigInteger Id = new BigInteger(idString);

//        ImageView back = findViewById(R.id.back_button);
//        back.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//
//            }
//        });

        RevToday=findViewById(R.id.revToday);
        RevTotal=findViewById(R.id.revTotal);
        UpgradToday=findViewById(R.id.UpToday);
        UpgradTotal=findViewById(R.id.UpTotal);
        HoursToday=findViewById(R.id.HoursToday);
        HoursTotal=findViewById(R.id.HoursTotal);
        Nodata = findViewById(R.id.noData);
        Review(Id);
        Hours(Id);
        Upgrades(Id);




        calendar = Calendar.getInstance();
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedYear = calendar.get(Calendar.YEAR);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);



        dateButton = findViewById(R.id.monthYearbtn);
        chart = findViewById(R.id.chart);
        dateButton.setBackgroundColor(getResources().getColor(R.color.Blue));
        dateButton.setTextColor(Color.WHITE);
        updateMonthYearButton();
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMonthYearPicker();

            }
        });
    }




    private void updateMonthYearButton() {
            String monthName = new SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.getTime());
            String yearText = String.valueOf(selectedYear);

            SpannableString spannableString = new SpannableString(monthName + "\n" + yearText);
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, monthName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            dateButton.setText(spannableString);

            String formattedMonth = String.format("%04d-%02d", selectedYear, selectedMonth + 1); // Month is 0-based in DatePicker
            fetchPerformanceData(formattedMonth);
        }

        final long[] lastClickTime = {0};
        final long DOUBLE_CLICK_TIME_DELTA = 300;

        private boolean isDoubleClick() {
            long clickTime = System.currentTimeMillis();
            if (clickTime - lastClickTime[0] < DOUBLE_CLICK_TIME_DELTA) {
                lastClickTime[0] = 0; // Reset last click time to avoid continuous double-click detection
                return true;
            } else {
                lastClickTime[0] = clickTime;
                return false;
            }
        }


    private void showMonthYearPicker() {
        final Dialog dialog = new Dialog(MyPerformanceActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout layout = new LinearLayout(MyPerformanceActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        dialog.setContentView(layout);

        NumberPicker monthPicker = new NumberPicker(MyPerformanceActivity.this);
        String[] months = new DateFormatSymbols().getShortMonths();
        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11); // Fixed to cover all months
        monthPicker.setDisplayedValues(months);
        layout.addView(monthPicker);

        NumberPicker yearPicker = new NumberPicker(MyPerformanceActivity.this);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        yearPicker.setMinValue(currentYear - 100);
        yearPicker.setMaxValue(currentYear);
        yearPicker.setValue(currentYear);
        layout.addView(yearPicker);

        Button selectButton = new Button(MyPerformanceActivity.this);
        selectButton.setText("Select");
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedMonth = monthPicker.getValue();
                selectedYear = yearPicker.getValue();
                String monthName = months[selectedMonth];
                String yearText = String.valueOf(selectedYear);

                SpannableString spannableString = new SpannableString(monthName + "\n" + yearText);
                spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, monthName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                dateButton.setText(spannableString);

                String formattedMonth = String.format("%04d-%02d", selectedYear, selectedMonth + 1); // Month is 0-based in DatePicker
                fetchPerformanceData(formattedMonth);
                dialog.dismiss();
            }
        });
        layout.addView(selectButton);

        dialog.show();
    }

    private void fetchPerformanceData(String formattedMonth) {
        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String idString = sp.getString("Id", "");
        BigInteger Id = new BigInteger(idString);

        new Thread(() -> {
            try {
                URL url = new URL(Constant.retings + Id + "&selected_month=" + formattedMonth);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();

                    JSONArray jsonArray = new JSONArray(stringBuilder.toString());

                    if (jsonArray.length() == 0) {
                        runOnUiThread(() -> {
                            Nodata.setVisibility(View.VISIBLE);
                            chart.setVisibility(View.GONE);
                        });
                        return;
                    }

                    ArrayList<Entry> reviewEntries = new ArrayList<>();
                    ArrayList<Entry> upgradeEntries = new ArrayList<>();
                    ArrayList<Entry> durationEntries = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        int reviewValue = obj.getInt("Review");
                        int upgradeValue = obj.getInt("Upgrade");
                        float durationValue = (float) obj.getDouble("Hours"); // Assuming duration is in hours
                        reviewEntries.add(new Entry(obj.getInt("Day"), reviewValue));
                        upgradeEntries.add(new Entry(obj.getInt("Day"), upgradeValue));
                        durationEntries.add(new Entry(obj.getInt("Day"), durationValue));
                    }

                    LineDataSet reviewDataSet = new LineDataSet(reviewEntries, "Reviews");
                    reviewDataSet.setColor(Color.BLUE);
                    reviewDataSet.setValueTextColor(Color.BLUE);
                    reviewDataSet.setLineWidth(3f);
                    reviewDataSet.setValueTextSize(15f);
                    reviewDataSet.setCircleRadius(6f);
                    reviewDataSet.setCircleHoleRadius(3f);

                    LineDataSet upgradeDataSet = new LineDataSet(upgradeEntries, "Upgrades");
                    upgradeDataSet.setColor(Color.RED);
                    upgradeDataSet.setValueTextColor(Color.RED);
                    upgradeDataSet.setLineWidth(4f);
                    upgradeDataSet.setValueTextSize(15f);
                    upgradeDataSet.setCircleRadius(6f);
                    upgradeDataSet.setCircleHoleRadius(3f);

                    LineDataSet durationDataSet = new LineDataSet(durationEntries, "Hours");
                    durationDataSet.setColor(Color.GREEN);
                    durationDataSet.setValueTextColor(Color.GREEN);
                    durationDataSet.setLineWidth(4f);
                    durationDataSet.setValueTextSize(15f);
                    durationDataSet.setCircleRadius(6f);
                    durationDataSet.setCircleHoleRadius(3f);

                    LineData lineData = new LineData(reviewDataSet, upgradeDataSet, durationDataSet);
                    chart.setData(lineData);

                    runOnUiThread(() -> {
                        Nodata.setVisibility(View.GONE);
                        chart.setVisibility(View.VISIBLE);

                        XAxis xAxis = chart.getXAxis();
                        xAxis.setTextSize(12f); // Set horizontal (X-axis) text size
                        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                        xAxis.setGranularity(1.3f);

                        YAxis leftYAxis = chart.getAxisLeft();
                        leftYAxis.setTextSize(15f); // Set vertical (left Y-axis) text size

                        YAxis rightYAxis = chart.getAxisRight();
                        rightYAxis.setTextSize(15f);

                        chart.getDescription().setText("Performance Chart");
                        chart.getDescription().setTextSize(10f); // Set the text size here
                        chart.getDescription().setEnabled(true);

                        Legend legend = chart.getLegend();
                        legend.setForm(Legend.LegendForm.CIRCLE);
                        legend.setTextSize(15f);
                        legend.setFormSize(15f);
                        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
                        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP); // Set legend at the top
                        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
                        legend.setDrawInside(false);
                        legend.setYOffset(10f);

                        // Custom legend entries
                        LegendEntry[] legendEntries = new LegendEntry[3];
                        legendEntries[0] = new LegendEntry("Reviews", Legend.LegendForm.CIRCLE, 10f, 2f, null, Color.BLUE);
                        legendEntries[1] = new LegendEntry("Upgrades", Legend.LegendForm.CIRCLE, 10f, 2f, null, Color.RED);
                        legendEntries[2] = new LegendEntry("Hours", Legend.LegendForm.CIRCLE, 10f, 2f, null, Color.GREEN);
                        legend.setCustom(legendEntries);

                        chart.invalidate();
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }


    private void Review(BigInteger id) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, Constant.Review + id, null, response -> {
                    try {
                        String todayStatus = response.getString("status_today");
                        String thisMonthStatus = response.getString("status_month");

                        if (todayStatus.equals("success")) {
                            int todayCount = response.getInt("today");
                            // Handle the case when there are orders for today
                            RevToday.setText("Today: " + todayCount);
                        } else {
                            // Handle error response for today's orders
                            RevToday.setText("Today: 0");
                            //Toast.makeText(getApplicationContext(), "Error fetching today's orders", Toast.LENGTH_SHORT).show();
                        }

                        if (thisMonthStatus.equals("success")) {
                            int thisMonthCount = response.getInt("month");
                            // Handle the case when there are orders for this month
                            RevTotal.setText("Total: " + thisMonthCount);
                        } else {
                            // Handle error response for this month's orders
                            RevTotal.setText("Total: 0");
                            //Toast.makeText(getApplicationContext(), "Error fetching this month's orders", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    // Handle error
                    Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_SHORT).show();
                });

        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void Hours(BigInteger id) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, Constant.Hours + id, null, response -> {
                    try {
                        String todayStatus = response.getString("today");
                        String thisMonthStatus = response.getString("month");

                        if (todayStatus.equals("success")) {
                            String todayDuration = response.getString("total_duration_today");
                            HoursToday.setText("Today: " + todayDuration);
                        } else {
                            // Handle error response for today's orders
                            Toast.makeText(getApplicationContext(), "Error fetching today's orders", Toast.LENGTH_SHORT).show();
                        }

                        if (thisMonthStatus.equals("success")) {
                            String thisMonthDuration = response.getString("total_duration_month");
                            HoursTotal.setText("Total: " + thisMonthDuration);
                        } else {
                            // Handle error response for this month's orders
                            Toast.makeText(getApplicationContext(), "Error fetching this month's orders", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    // Handle error
                    Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_SHORT).show();
                });

        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
    private void Upgrades(BigInteger id) {
        String url = Constant.Upgrades + id;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    try {
                        String status = response.getString("Status");
                        int todayCount = response.getInt("today");
                        int thisMonthCount = response.getInt("this_month");

                        if (status.equals("success")) {
                            // Display the counts
                            UpgradToday.setText("Today: " + todayCount);
                            UpgradTotal.setText("Total: " + thisMonthCount);
                        } else {
                            // Handle error response
                            Toast.makeText(getApplicationContext(), "Error fetching upgrades", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    // Handle error
                    Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_SHORT).show();
                });

        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
