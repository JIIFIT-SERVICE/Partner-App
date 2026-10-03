package com.company.jifiit.historypages;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Adapter.BookingHistoryAdapter;
import com.company.jifiit.Constant;
import com.company.jifiit.Leave.LeaveActivity;
import com.company.jifiit.Leave.LeaveHistory;
import com.company.jifiit.R;
import com.company.jifiit.ViewMoreActivity;
import com.company.jifiit.ViewMoreMenu.FareActivity;
import com.company.jifiit.model.BookingHistoryModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class BookingHistory extends AppCompatActivity {
    Button MonthYear, CanceledBtn, CompletedBtn;
    RecyclerView historyList;
    BookingHistoryAdapter bookingHistoryAdapter;
    ArrayList<BookingHistoryModel> bookingHistoryModels;
    TextView notFound;
    private int selectedMonth;
    private int selectedYear;
    private Calendar calendar;
    private BigInteger Id;
    private boolean isMonthYearClicked = false;
    private String[] months;
    float dX, dY;
    int lastAction;
    String idString;
    FloatingActionButton fab;
    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_booking_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("My Booking History");


        // Enable the back button in the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        idString = sp.getString("Id", "");
        Id = new BigInteger(idString);
        months = new DateFormatSymbols().getShortMonths();

        MonthYear = findViewById(R.id.monthYearbtn);
        CanceledBtn = findViewById(R.id.canceledbtn);
        CompletedBtn = findViewById(R.id.completedbtn);
        historyList = findViewById(R.id.historylist);
        notFound = findViewById(R.id.notFound);
        fab = findViewById(R.id.fab);

        historyList.setLayoutManager(new LinearLayoutManager(this));
        bookingHistoryModels = new ArrayList<>();
        bookingHistoryAdapter = new BookingHistoryAdapter(this, bookingHistoryModels);
        historyList.setAdapter(bookingHistoryAdapter);

        calendar = Calendar.getInstance();
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedYear = calendar.get(Calendar.YEAR);

        updateMonthYearButton();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BookingHistory.this, MonthlyUpgradeActivity.class);
                intent.putExtra("id",idString);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        FloatingActionButton fab = findViewById(R.id.fab);

// Add touch listener to make FAB draggable
        fab.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                // Get screen dimensions
                View parentView = (View) view.getParent();
                int parentWidth = parentView.getWidth();
                int parentHeight = parentView.getHeight();

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        // Capture the initial position
                        dX = view.getX() - event.getRawX();
                        dY = view.getY() - event.getRawY();
                        lastAction = MotionEvent.ACTION_DOWN;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        // Calculate new position
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        // Apply boundary constraints
                        newX = Math.max(0, Math.min(newX, parentWidth - view.getWidth()));
                        newY = Math.max(0, Math.min(newY, parentHeight - view.getHeight()));

                        // Set the FAB's new position
                        view.setX(newX);
                        view.setY(newY);
                        lastAction = MotionEvent.ACTION_MOVE;
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (lastAction == MotionEvent.ACTION_DOWN) {
                            // Handle click event if no movement occurred
                            view.performClick();
                        }
                        return true;

                    default:
                        return false;
                }
            }
        });

        MonthYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDoubleClick()) {
                    return;
                }
                isMonthYearClicked = true;
                updateMonthYearButton();
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1; // Adding 1 to match the CalendarPicker month range

                String monthName = new SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.getTime());
                String yearText = String.valueOf(year);

                SpannableString spannableString = new SpannableString(monthName + "\n" + yearText);
                spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, monthName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                MonthYear.setText(spannableString);
                historybtn(Id, selectedMonth, selectedYear, "All");
                MonthYear.setBackgroundColor(getResources().getColor(R.color.Blue));
                MonthYear.setTextColor(Color.WHITE);

                CompletedBtn.setBackgroundColor(getResources().getColor(R.color.White));
                CompletedBtn.setTextColor(Color.BLUE);
                CanceledBtn.setBackgroundColor(getResources().getColor(R.color.white));
                CanceledBtn.setTextColor(Color.BLUE);
            }
        });





        CompletedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateButtonColors(CompletedBtn, CanceledBtn);
                historybtn(Id, selectedMonth, selectedYear, "Completed");
                CompletedBtn.setBackgroundColor(getResources().getColor(R.color.Blue));
                CompletedBtn.setTextColor(Color.WHITE);
                CanceledBtn.setBackgroundColor(getResources().getColor(R.color.white));
                CanceledBtn.setTextColor(Color.BLUE);
            }
        });

        CanceledBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateButtonColors(CanceledBtn, CompletedBtn);
                historybtn(Id, selectedMonth, selectedYear, "Canceled");
                CompletedBtn.setBackgroundColor(getResources().getColor(R.color.White));
                CompletedBtn.setTextColor(Color.BLUE);
                CanceledBtn.setBackgroundColor(getResources().getColor(R.color.Blue));
                CanceledBtn.setTextColor(Color.WHITE);

            }
        });

        MonthYear.performClick();
    }
    final long[] lastClickTime = {0};
    final long DOUBLE_CLICK_TIME_DELTA = 300;
    private boolean isDoubleClick() {
        long clickTime = System.currentTimeMillis();
        if (clickTime - lastClickTime[0] < DOUBLE_CLICK_TIME_DELTA) {
            // Double click detected
            showMonthYearPicker();
            return true;
        }
        lastClickTime[0] = clickTime;
        return false;
    }

    private void showMonthYearPicker() {
        final Dialog dialog = new Dialog(BookingHistory.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout layout = new LinearLayout(BookingHistory.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        dialog.setContentView(layout);

        NumberPicker monthPicker = new NumberPicker(BookingHistory.this);
        String[] months = new DateFormatSymbols().getShortMonths();
        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(Calendar.getInstance().get(Calendar.MONTH));
        monthPicker.setDisplayedValues(Arrays.copyOfRange(months, 0, Calendar.getInstance().get(Calendar.MONTH) + 1));
        layout.addView(monthPicker);

        NumberPicker yearPicker = new NumberPicker(BookingHistory.this);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        yearPicker.setMinValue(currentYear - 100);
        yearPicker.setMaxValue(currentYear);
        yearPicker.setValue(currentYear);
        layout.addView(yearPicker);

        Button selectButton = new Button(BookingHistory.this);
        selectButton.setText("Select");
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedDate = months[monthPicker.getValue()] + " \n" + yearPicker.getValue();
                SpannableString spannableString = new SpannableString(selectedDate);
                spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, months[monthPicker.getValue()].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                MonthYear.setText(spannableString);
                selectedMonth = monthPicker.getValue();
                selectedYear = yearPicker.getValue();
                historybtn(Id, selectedMonth, selectedYear, "All");
                dialog.dismiss();
            }
        });
        layout.addView(selectButton);

        dialog.show();
    }


    private void updateMonthYearButton() {
        String monthName = new SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.getTime());
        String yearText = String.valueOf(selectedYear);

        SpannableString spannableString = new SpannableString(monthName + "\n" + yearText);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, monthName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        MonthYear.setText(spannableString);
        MonthYear.setBackgroundColor(getResources().getColor(R.color.Blue));
        MonthYear.setTextColor(Color.WHITE);
        CompletedBtn.setBackgroundColor(getResources().getColor(R.color.white));
        CompletedBtn.setTextColor(Color.BLUE);
        CanceledBtn.setBackgroundColor(getResources().getColor(R.color.white));
        CanceledBtn.setTextColor(Color.BLUE);
    }


    private void updateButtonColors(Button selectedButton, Button otherButton) {
        selectedButton.setBackgroundColor(getResources().getColor(R.color.Blue));
        selectedButton.setTextColor(Color.WHITE);
        otherButton.setBackgroundColor(getResources().getColor(R.color.white));
        otherButton.setTextColor(Color.BLUE);
    }
    private void historybtn(BigInteger id, int month,int year,String listType) {
        String url;
        if (listType.equals("All")) {
            url = Constant.BookingHistory + id + "&month=" + getMonthAbbreviation(month) + "&year=" + year + "&payment_status=All" ;
        } else if (listType.equals("Completed")) {
            url =Constant.BookingHistory + id + "&month=" + getMonthAbbreviation(month) + "&year=" + year + "&payment_status=Completed";
        } else if (listType.equals("Canceled")) {
            url = Constant.BookingHistory + id + "&month=" + getMonthAbbreviation(month) + "&year=" + year + "&payment_status=Canceled";
        } else {
            // Handle unknown listType
            return;
        }
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String status = response.getString("Status");
                        if (status.equals("success")) {
                            bookingHistoryModels.clear();
                            historyList.setVisibility(View.VISIBLE);
                            notFound.setVisibility(View.GONE);
                            JSONArray bookingsArray = response.getJSONArray("bookings");
                            for (int i = 0; i < bookingsArray.length(); i++) {
                                JSONObject bookingObject = bookingsArray.getJSONObject(i);
                                String customerName = bookingObject.getString("customer_name");
                                String massageName = bookingObject.getString("massage_name");
                                String startTime = bookingObject.getString("start_time");
                                String duration = bookingObject.getString("duration");
                                String address = bookingObject.getString("address"); // Corrected key name
                                String paymentStatus = bookingObject.getString("payment_status");
                                String bookingId = bookingObject.getString("id");
                                String date = bookingObject.getString("order_date");
                                String paymentType = bookingObject.getString("payment_type");
                                String fare = bookingObject.getString("fare");
                                String customers_id = bookingObject.getString("customers_id");
                                String ratings = bookingObject.getString("ratings");
                                String amount = bookingObject.getString("amount");
                                String bookingType = bookingObject.getString("bookingType");


                                BookingHistoryModel bookingHistoryModel = new BookingHistoryModel(
                                        customerName,
                                        massageName,
                                        startTime,
                                        duration,
                                        address,
                                        paymentStatus,
                                        bookingId,
                                        date,
                                        paymentType,
                                        fare,customers_id,ratings
                                ,amount,bookingType);

                                bookingHistoryModels.add(bookingHistoryModel);
                            }

                            // Update the adapter after adding new items
                            bookingHistoryAdapter.notifyDataSetChanged();
                        } else {
                            //Toast.makeText(getApplicationContext(), "No bookings found", Toast.LENGTH_SHORT).show();
                            historyList.setVisibility(View.GONE);
                            notFound.setVisibility(View.VISIBLE);
                            notFound.setText("No booking in this month");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(getApplicationContext(), "Volley Error occurred: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("VolleyError", "Error occurred", error);
                });

        requestQueue.add(jsonObjectRequest);
    }



    private String getMonthAbbreviation(int month) {
        String[] months = new DateFormatSymbols().getShortMonths();
        if (month >= 0 && month < months.length) {
            return months[month];
        }
        return "";
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
