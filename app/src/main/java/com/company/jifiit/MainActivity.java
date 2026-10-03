package com.company.jifiit;

import static okio.HashingSink.sha256;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import com.company.jifiit.Adapter.BookingAdapter;
import com.company.jifiit.Commission.CommissionActivity;
import com.company.jifiit.Leave.LeaveHistory;
import com.company.jifiit.MyPerformance.MyPerformanceActivity;
import com.company.jifiit.MyQr.My_Qr;
import com.company.jifiit.Salary.NewSalaryActivity;
import com.company.jifiit.TrainingHub.TrainingHubActivity;
import com.company.jifiit.Referral.HelpActivity;
import com.company.jifiit.Referral.PrivacyAndPolicy;
import com.company.jifiit.Referral.ReferGuestActivity;
import com.company.jifiit.Referral.ReferralFriendActivity;
import com.company.jifiit.Referral.Terms;

import com.company.jifiit.Verification.UnverifiedActivity;
import com.company.jifiit.Wallet.WalletAcitivity;
import com.company.jifiit.historypages.BookingHistory;
import com.company.jifiit.model.BookingModel;
import com.company.jifiit.notification.NotificationsActivity;

import com.google.android.material.navigation.NavigationView;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


import java.util.TimeZone;


//import com.phonepe.intent.sdk.api.models.TransactionRequest;




import org.json.JSONObject;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{
    TextView wish,userId, TherapistName, TherapistId;
    ListView Todoview;
    TextView RatingStatus, WorkStatus, WorkingYear,notFound,DayText;
    CircleImageView  profileImg;
    Button  Today,Tomorrow,yesterdayTextView;
    //    Button todayShift,tomorrowShift;
    CardView Cal;
    Button Unverified,Verified;
    TextView RatingBar;
    RecyclerView listView;
    TextView RevToday,RevTotal,UpgradToday,UpgradTotal,HoursToday,HoursTotal;
    RecyclerView recyclerView;
    BookingAdapter bookingAdapter;
    ArrayList<BookingModel> bookings;

    ProgressBar progressBar;

    BigInteger Id;

    Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
    TimeZone timeZone = calendar.getTimeZone();

    ImageView calendarIcon;
    private String currentSelectedButton = "today"; // default to today
    private String selectedCalendarDate = ""; // Store the selected calendar date
    private RequestQueue requestQueue;
    private String currentOrderId;

    private AppUpdateManager appUpdateManager;
    private static final int UPDATE_REQUEST_CODE = 100;
    private static final int PERMISSION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);
//        ReferDialog.showReferDialog(this);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        //Today's Date Show
        String[][] selectedDate = {new String[]{""}};



        Today = findViewById(R.id.tvDate);
        Tomorrow = findViewById(R.id.tvTomorrow);
        yesterdayTextView = findViewById(R.id.tvYesterday);
        Cal = findViewById(R.id.calendarCard);
        calendarIcon = findViewById(R.id.calendarIcon);


        listView =findViewById(R.id.todolist);
        progressBar = findViewById(R.id.progress_bar);
        notFound=findViewById(R.id.Bookings_notFound);
        notFound.setVisibility(View.VISIBLE);
        userId = findViewById(R.id.UserId);

        LinearLayout items = findViewById(R.id.items1);
        ImageView Up = findViewById(R.id.btnArowUp);
        ImageButton Down = findViewById(R.id.btnArowDown);
        Up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(items.getVisibility() == View.VISIBLE){
                    items.setVisibility(View.GONE);
                    Up.setVisibility(View.GONE);
                    Down.setVisibility(View.VISIBLE);
                }
            }
        });
        Down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(items.getVisibility() == View.GONE){
                    items.setVisibility(View.VISIBLE);
                    Up.setVisibility(View.VISIBLE);
                    Down.setVisibility(View.GONE);
                }
            }
        });

        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String name = sp.getString("Name", "Default Name");
        String idString = sp.getString("Id", "");
        Id = new BigInteger(idString);
        String Age = sp.getString("Age","Hii");


//        fetchVerification(Id);
//        fetchWorkingStatus(Id);
//        Experienceed(Id);
//        Review(Id);
//        Hours(Id);
//        Upgrades(Id);

        SimpleDateFormat dateFormat = new SimpleDateFormat("EE - d MMM yyyy", Locale.getDefault());
        dateFormat.setTimeZone(timeZone);

// Display "Today" with dynamic time zone date
        String date = dateFormat.format(calendar.getTime());
        SpannableString spannableString = new SpannableString("TODAY\n" + date);
        spannableString.setSpan(new RelativeSizeSpan(1.5f), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);  // Bigger size for "TODAY"
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        Today.setText(spannableString);

// Click listener for Today
        Today.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentSelectedButton = "today";
                fetchBookings(Id, "today", null);
                Today.setBackgroundColor(getResources().getColor(R.color.Blue));
                Today.setTextColor(Color.WHITE);
                Tomorrow.setBackgroundColor(getResources().getColor(R.color.white));
                Tomorrow.setTextColor(Color.BLUE);
                yesterdayTextView.setBackgroundColor(getResources().getColor(R.color.white));
                yesterdayTextView.setTextColor(Color.BLUE);

                Cal.setCardBackgroundColor(getResources().getColor(R.color.white));

                calendarIcon.setColorFilter(getResources().getColor(R.color.Blue));
            }
        });

// Prepare "Tomorrow" date
        calendar.add(Calendar.DAY_OF_YEAR, 1); // Move to tomorrow
        String tomorrowDate = dateFormat.format(calendar.getTime());
        SpannableString spannableString1 = new SpannableString("TOMORROW\n" + tomorrowDate);
        spannableString1.setSpan(new RelativeSizeSpan(1.5f), 0, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);  // Bigger size for "TOMORROW"
        spannableString1.setSpan(new StyleSpan(Typeface.BOLD), 0, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        Tomorrow.setText(spannableString1);

// Click listener for Tomorrow
        Tomorrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentSelectedButton = "tomorrow";
                fetchBookings(Id, "tomorrow", null);
                Tomorrow.setBackgroundColor(getResources().getColor(R.color.Blue));
                Tomorrow.setTextColor(Color.WHITE);
                Today.setBackgroundColor(getResources().getColor(R.color.white));
                Today.setTextColor(Color.BLUE);
                yesterdayTextView.setBackgroundColor(getResources().getColor(R.color.white));
                yesterdayTextView.setTextColor(Color.BLUE);
                Cal.setCardBackgroundColor(getResources().getColor(R.color.white));

                calendarIcon.setColorFilter(getResources().getColor(R.color.Blue));
            }
        });

// (Optional) If you want to print the timezone in the log for testing:
        Log.d("TimeZoneTest", "Current Time Zone: " + timeZone.getID());


        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1); // Move back by 1 day (yesterday)

//        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        String yesterdayDate = dateFormat.format(calendar.getTime());

// Create a styled string with "YESTERDAY" in bold and larger font
        SpannableString spannableString2 = new SpannableString("YESTERDAY\n" + yesterdayDate);
        spannableString2.setSpan(new RelativeSizeSpan(1.5f), 0, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Larger text for "YESTERDAY"
        spannableString2.setSpan(new StyleSpan(Typeface.BOLD), 0, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Bold text for "YESTERDAY"

// Set the formatted text to the TextView
        yesterdayTextView.setText(spannableString2);
        yesterdayTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentSelectedButton = "yesterday";
                fetchBookings(Id, "yesterday", null);
                yesterdayTextView.setBackgroundColor(getResources().getColor(R.color.Blue));
                yesterdayTextView.setTextColor(Color.WHITE);
                Today.setBackgroundColor(getResources().getColor(R.color.white));
                Today.setTextColor(Color.BLUE);
                Tomorrow.setBackgroundColor(getResources().getColor(R.color.white));
                Tomorrow.setTextColor(Color.BLUE);
                Cal.setCardBackgroundColor(getResources().getColor(R.color.white));

                calendarIcon.setColorFilter(getResources().getColor(R.color.Blue));

            }
        });





        //Show Calender
//        Cal = findViewById(R.id.tvCalendar);
        Cal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get current date to set as default in the date picker dialog
                currentSelectedButton = "calender";
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                Calendar selectedCalendar = Calendar.getInstance();
                                selectedCalendar.set(year, month, dayOfMonth);

                                Calendar minDate = Calendar.getInstance();
                                minDate.add(Calendar.MONTH, -1); // last 1 month

                                if (selectedCalendar.before(minDate)) {
                                    Toast.makeText(MainActivity.this, "Please select a date within the last 1 month", Toast.LENGTH_SHORT).show();
                                } else {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                    String selectedDate = sdf.format(selectedCalendar.getTime());
                                    selectedCalendarDate = sdf.format(selectedCalendar.getTime());

                                    fetchBookings(Id, "specific_date", selectedDate);
                                    Cal.setCardBackgroundColor(getResources().getColor(R.color.Blue));
                                    Tomorrow.setBackgroundColor(getResources().getColor(R.color.white));
                                    Today.setTextColor(Color.BLUE);
                                    Tomorrow.setTextColor(Color.BLUE);
                                    Today.setBackgroundColor(getResources().getColor(R.color.white));
                                    yesterdayTextView.setBackgroundColor(getResources().getColor(R.color.white));
                                    yesterdayTextView.setTextColor(Color.BLUE);
                                    calendarIcon.setColorFilter(getResources().getColor(R.color.white));
                                }
                            }
                        }, year, month, dayOfMonth);

// Set minimum date to 1 month ago
                Calendar oneMonthAgo = Calendar.getInstance();
                oneMonthAgo.add(Calendar.MONTH, -1);
                datePickerDialog.getDatePicker().setMinDate(oneMonthAgo.getTimeInMillis());


                datePickerDialog.show();

            }
        });



        userId.setText(name);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);


        View headerView = navigationView.getHeaderView(0);
        ImageView backBtn = headerView.findViewById(R.id.backbtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
            }
        });

        Verified=headerView.findViewById(R.id.verified);
        Unverified=headerView.findViewById(R.id.unverified);
        Unverified.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UnverifiedActivity.class);
                startActivity(intent);

            }
        });


        RevToday=findViewById(R.id.revToday);
        RevTotal=findViewById(R.id.revTotal);
        UpgradToday=findViewById(R.id.UpToday);
        UpgradTotal=findViewById(R.id.UpTotal);
        HoursToday=findViewById(R.id.HoursToday);
        HoursTotal=findViewById(R.id.HoursTotal);





        ImageView EditBtn =headerView.findViewById(R.id.editBtn);
        ImageView NotificationBtn =headerView.findViewById(R.id.notificationBtn);
        TherapistName = headerView.findViewById(R.id.employeeName);
        TherapistId =   headerView.findViewById(R.id.employeeId);
        TherapistName.setText(name);
        TherapistId.setText("Employee's I'd:  "+  Id);
        profileImg = headerView.findViewById(R.id.employeeProfile);

        Glide.with(this)
                .load(Constant.ProfileImage + Id)
                .placeholder(R.drawable.account_circle_24)
                .error(R.drawable.outline_add_circle_outline_24)
                .skipMemoryCache(true)               // Skip memory cache
                .diskCacheStrategy(DiskCacheStrategy.NONE)  // Skip disk cache
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.e("Glide Error", "Failed to load image: " + e);
                        if (e != null) {
                            e.logRootCauses("Glide Error");
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                }).into(profileImg);




        RatingStatus=headerView.findViewById(R.id.ratingText);
        WorkStatus =headerView.findViewById(R.id.OrderText);
        WorkingYear=headerView.findViewById(R.id.YearText);
        RatingBar=headerView.findViewById(R.id.ratingBar);
//        YearText = headerView.findViewById(R.id.year);
//        MonthText = headerView.findViewById(R.id.month);
        DayText = headerView.findViewById(R.id.days);





        EditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, EditPage.class));
            }
        });

        NotificationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NotificationsActivity.class));
            }
        });




        //ImageView greetingImage = findViewById(R.id.greeting_image);
        wish= findViewById(R.id.Wish);

        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        int DayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        if (timeOfDay >= 0 && timeOfDay <12)
        {
            wish.setText("Good Morning");
            //  greetingImage.setImageResource(R.mipmap.lever_soleil);
            birthDayWish(Id);

        }else if (timeOfDay >= 12 && timeOfDay <16)
        {
            wish.setText("Good afternoon");
            // greetingImage.setImageResource(R.mipmap.noon);

        }else if (timeOfDay >= 16 && timeOfDay <21)
        {
            wish.setText("Good Evening");
            //  greetingImage.setImageResource(R.mipmap.coucher_soleil);

        }else if (timeOfDay >= 21 && timeOfDay< 24)
        {
            wish.setText("Good Night");
            //  greetingImage.setImageResource(R.mipmap.night);

        } else if (DayOfMonth == Integer.parseInt(Age)) {
            wish.setText("Happy Birthday" + name);

        }

        checkForUpdate();
        checkRequiredPermissions();
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
//                            Toast.makeText(getApplicationContext(), "Error fetching today's orders", Toast.LENGTH_SHORT).show();
                        }

                        if (thisMonthStatus.equals("success")) {
                            String thisMonthDuration = response.getString("total_duration_month");
                            HoursTotal.setText("Total: " + thisMonthDuration);
                        } else {
                            // Handle error response for this month's orders
//                            Toast.makeText(getApplicationContext(), "Error fetching this month's orders", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    // Handle error
//                    Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_SHORT).show();
                });

        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(jsonObjectRequest);
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
//                    Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_SHORT).show();
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
//                            Toast.makeText(getApplicationContext(), "Error fetching upgrades", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    // Handle error
//                    Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_SHORT).show();
                });

        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }




    private void fetchBookings(BigInteger id, String listType, String date) {
        // Check for network connectivity
        if (!isNetworkAvailable()) {
            progressBar.setVisibility(View.GONE);
            listView.setVisibility(View.GONE);
            notFound.setVisibility(View.VISIBLE);
            notFound.setText("No Internet Connection");
            Toast.makeText(this, "Please check your internet connection.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialize the RecyclerView and adapter
        listView.setLayoutManager(new LinearLayoutManager(this));
        bookings = new ArrayList<>();
        bookingAdapter = new BookingAdapter(bookings);
        listView.setAdapter(bookingAdapter);

        // Construct the URL based on the list type
        String timeZone = java.util.TimeZone.getDefault().getID();
        String url;
        switch (listType) {
            case "today":
                url = Constant.fetchBookings + id + "&selectdate=today&timezone="+timeZone;
                Log.d("Today Time zone:", timeZone);
                break;
            case "tomorrow":
                url = Constant.fetchBookings + id + "&selectdate=tomorrow&timezone=" + timeZone;
                Log.d("Tomorrow Time zone:", timeZone);
                break;
            case "yesterday":
                url = Constant.fetchBookings + id + "&selectdate=yesterday&timezone=" + timeZone;
                Log.d("Tomorrow Time zone:", timeZone);
                break;
            case "specific_date":
                url = Constant.fetchBookings + id + "&selectdate=" + date + "&timezone=" + timeZone;
                Log.d("Date Time zone:", timeZone);
                break;
            default:
                Toast.makeText(this, "Invalid list type selected.", Toast.LENGTH_SHORT).show();
                return;
        }

        // Show the progress bar before making the request
        progressBar.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);

        // Create a Volley request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // Define the request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                    try {
                        String status = response.getString("Status");
                        if ("success".equalsIgnoreCase(status)) {
                            notFound.setVisibility(View.GONE);
                            JSONArray dataArray = response.optJSONArray("booking");
                            if (dataArray != null && dataArray.length() > 0) {
                                bookings.clear(); // Clear previous data
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject bookingObject = dataArray.getJSONObject(i);

                                    // Extract booking details
                                    String customerName = bookingObject.optString("customer_name", "N/A");
                                    String massageName = bookingObject.optString("massage_names", "N/A");
                                    String startTime = bookingObject.optString("start_time", "N/A");
                                    String duration = bookingObject.optString("duration", "N/A");
                                    String address = bookingObject.optString("landmark", "N/A");
                                    String paymentStatus = bookingObject.optString("payment_status", "N/A");
                                    String bookingId = bookingObject.optString("id", "N/A");

                                    // Add the booking to the list
                                    BookingModel booking = new BookingModel(
                                            customerName,
                                            massageName,
                                            startTime,
                                            duration,
                                            address,
                                            paymentStatus,
                                            bookingId
                                    );
                                    bookings.add(booking);
                                }
                                bookingAdapter.notifyDataSetChanged(); // Update the adapter
                            } else {
                                // No bookings found
                                bookings.clear();
                                bookingAdapter.notifyDataSetChanged();
                                notFound.setVisibility(View.VISIBLE);
                                notFound.setText("No booking relax now 😎");
                            }
                        } else {
                            // Handle non-success status
                            bookings.clear();
                            bookingAdapter.notifyDataSetChanged();
                            notFound.setVisibility(View.VISIBLE);
                            notFound.setText("No booking relax now 😎");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
//                        Toast.makeText(this, "Parsing error occurred.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Handle errors
                    progressBar.setVisibility(View.GONE);
                    notFound.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
//                    Toast.makeText(getApplicationContext(), "Error occurred: " + error.getMessage(), Toast.LENGTH_SHORT).show();
//                    Log.e("Volley Error", "Error occurred", error);
                });

        // Add the request to the queue
        requestQueue.add(jsonObjectRequest);
    }

    /**
     * Check if the network is available
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }


    private void Experienceed(BigInteger id) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, Constant.Experience + id, null, response -> {
                    try {
                        String status = response.getString("status");
                        if (status.equals("success")) {
                            JSONObject data = response.getJSONObject("data");
                            String createdAt = data.getString("created_at");

                            String mobileNumber = data.getString("phone");
                            if (createdAt.equals("0000-00-00")) {
                                WorkingYear.setText("0");
                            } else {
                                WorkExperienceStatus(id);
                            }
                            new android.os.Handler().postDelayed(() -> {
                                if (Id != null) {
                                    fetchCommisson(Id,mobileNumber);
                                }
                            }, 1000); // 2 second delay

                        } else {
                            // Handle error response
                            String message = response.getString("message");
//                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    // Handle error
//                    Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_SHORT).show();
                });

        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void WorkExperienceStatus(BigInteger id) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constant.WorkingExperience + id, null,
                response -> {
                    try {
                        Log.d("JSON_RESPONSE", response.toString());
                        String status = response.getString("status");

                        if (status.equals("success")) {
                            String message = response.getString("message");
                            String[] parts = message.split("\\.");
                            int years = Integer.parseInt(parts[0]);
                            int months = Integer.parseInt(parts[1]);
                            int days = Integer.parseInt(parts[2]);

                            Log.d("Experience","Years: "+years+ " Days: " + days + " Months: " + months );

                            DayText.setVisibility(View.VISIBLE);

                            // Display the experience based on different scenarios
                            if (years == 0 && months == 0 && days == 0) {
                                // No experience
                                WorkingYear.setText("0");
                                DayText.setText("DAYS");
                            }
                            else if (years > 0) {
                                // Has years - show years with decimal months (12.3 years)
                                double totalExperience = years + (months / 12.0) + (days / 365.0);
                                String formattedExperience = String.format("%.1f", totalExperience);
                                WorkingYear.setText(formattedExperience);
                                DayText.setText(getYearText(years));
                            }
                            else if (months > 0) {
                                // Only months - show just the months count
                                WorkingYear.setText(String.valueOf(months));
                                DayText.setText(getMonthText(months));
                            }
                            else {
                                // Only days
                                WorkingYear.setText(String.valueOf(days));
                                DayText.setText(getDayText(days));
                            }

                        } else {
                            Toast.makeText(MainActivity.this, "Have a Good day", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Handle error if needed
                    Log.e("VOLLEY_ERROR", error.toString());
                });
        requestQueue.add(jsonObjectRequest);
    }

    private String getYearText(int years) {
        return years == 1 ? "YEAR" : "YEARS";
    }

    private String getMonthText(int months) {
        return months == 1 ? "MONTH" : "MONTHS";
    }

    private String getDayText(int days) {
        return days == 1 ? "DAY" : "DAYS";
    }




    private void birthDayWish(BigInteger id) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constant.BirthDayWish + id, null,
                response -> {
                    try {
                        Log.d("JSON_RESPONSE", response.toString());
                        String status = response.getString("status");
                        if (status.equals("success")) {
                            String message = response.getString("message");
                            wish.setText(message);
                        } else {
                            Toast.makeText(MainActivity.this,"Have a Good day",Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
//                        Toast.makeText(MainActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
//                    Toast.makeText(MainActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                });
        requestQueue.add(jsonObjectRequest);
    }


    private void fetchWorkingStatus(BigInteger id) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constant.CompletedWorked + id, null,
                response -> {
                    try {
                        Log.d("JSON_RESPONSE", response.toString());
                        String status = response.getString("status");
                        float maxRating = 0;
                        if (status.equals("success")) {
                            int totalRows = response.getInt("total_rows");
                            maxRating = (float) response.getDouble("average_rating");

                            WorkStatus.setText(String.valueOf(totalRows));
                            RatingStatus.setText(String.valueOf(maxRating));
                            setRatingBarColor(RatingBar, maxRating);

                        } else {
                            WorkStatus.setText("0");
                            RatingStatus.setText("0.0");
                            setRatingBarColor(RatingBar, maxRating);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
//                        Toast.makeText(MainActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        WorkStatus.setText("*");
                    }
                },
                error -> {
//                    Toast.makeText(MainActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                });
        requestQueue.add(jsonObjectRequest);
    }

    private void setRatingBarColor(TextView ratingBar, float maxRating) {
        if (maxRating > 4.0) {
            ratingBar.setTextColor(Color.parseColor("#FFD700")); // Gold color
        } else {
            ratingBar.setTextColor(Color.RED); // Red color
        }
    }

    public boolean isVerified = false;
    private void fetchVerification(BigInteger id) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constant.ProfileVerification + id, null,
                response -> {
                    try {
                        Log.d("JSON_RESPONSE", response.toString());
                        String status = response.getString("status");
                        if (status.equals("success")) {
                            Verified.setVisibility(View.VISIBLE);
                            Unverified.setVisibility(View.GONE);
                            isVerified = true;
                        } else {
                            Unverified.setVisibility(View.VISIBLE);
                            Verified.setVisibility(View.GONE);
                            if (!isVerified) {
                                NavigationView navigationView = findViewById(R.id.nav_view);
                                Menu navMenu = navigationView.getMenu();
                                navMenu.findItem(R.id.history).setVisible(false);
                                navMenu.findItem(R.id.TrainingHub).setVisible(false);
                                navMenu.findItem(R.id.leaves).setVisible(false);
                                navMenu.findItem(R.id.myQr).setVisible(false);
//                                navMenu.findItem(R.id.nav_wallet).setVisible(false);
//                                navMenu.findItem(R.id.nav_Loans).setVisible(false);
                                navMenu.findItem(R.id.nav_myperformance).setVisible(false);
                                navMenu.findItem(R.id.nav_salary).setVisible(false);
                                navMenu.findItem(R.id.nav_refer).setVisible(false);
                                navMenu.findItem(R.id.nav_shareGuest).setVisible(false);
                                navMenu.findItem(R.id.nav_privacy).setVisible(false);
                                navMenu.findItem(R.id.nav_Terms).setVisible(false);
                                navMenu.findItem(R.id.nav_help).setVisible(false);
                                return;
                            }

                            isVerified = false;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
//                        Toast.makeText(MainActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
//                    Toast.makeText(MainActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                });
        requestQueue.add(jsonObjectRequest);

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.nav_home) {
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            }
        } else if (id == R.id.history) {
            Intent intent = new Intent(MainActivity.this, BookingHistory.class);
            startActivity(intent);
        }
        else if (id == R.id.TrainingHub) {
            Intent intent = new Intent(MainActivity.this, TrainingHubActivity.class);
            startActivity(intent);
            // Handle inventory
        }
        else if (id == R.id.leaves) {
            Intent intent = new Intent(MainActivity.this, LeaveHistory.class);
            startActivity(intent);
            // Handle leaves
        }
        else if (id == R.id.myQr) {
            Intent intent = new Intent(MainActivity.this, My_Qr.class);
            startActivity(intent);
            // Handle leaves
        }
//        else if (id == R.id.nav_wallet) {
//            // Handle fins
//            Intent intent = new Intent(MainActivity.this, WalletAcitivity.class);
//            startActivity(intent);
//        }

//        else if (id == R.id.nav_Loans) {
//            // Handle loans
//        }

        else if (id == R.id.nav_myperformance) {
            // Handle my performance
            Intent intent = new Intent(MainActivity.this, MyPerformanceActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_salary) {
            // Handle salary
            Intent intent = new Intent(MainActivity.this, CommissionActivity.class);
            startActivity(intent);
        }
//        else if (id == R.id.nav_salary) {
//            // Handle salary
//            Intent intent = new Intent(MainActivity.this, NewSalaryActivity.class);
//            startActivity(intent);
//        }
        else if (id == R.id.nav_refer) {
            // Handle refer
            Intent intent = new Intent(MainActivity.this, ReferralFriendActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_shareGuest) {
            // Handle share guest
            Intent intent = new Intent(MainActivity.this, ReferGuestActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_privacy) {
            // Handle privacy
            Intent intent = new Intent(MainActivity.this, PrivacyAndPolicy.class);
            startActivity(intent);
        } else if (id == R.id.nav_Terms) {
            // Handle terms
            Intent intent = new Intent(MainActivity.this, Terms.class);
            startActivity(intent);
        } else if (id == R.id.nav_help) {
            // Handle help
            Intent intent = new Intent(MainActivity.this, HelpActivity.class);
            startActivity(intent);
        } else if (id == R.id.logout) {
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.END)) {
                drawer.closeDrawer(GravityCompat.END);
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
            builder.setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            logOutUser();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Cancelled
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();

            // Ensure white background
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

            // Optional: Customize text colors
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE);

            TextView messageText = dialog.findViewById(android.R.id.message);
            if (messageText != null) {
                messageText.setTextColor(Color.BLACK);
            }
        }


        return true;
    }



    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }


    private void logOutUser() {
        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        loggedOut(Id);
        startActivity(intent);
        finish();
    }

    private void loggedOut(BigInteger id) {
        String url = Constant.Loggedout; // Make sure Constant.Loggedout is the correct URL

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("LogoutResponse", "Response: " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            if (status.equals("success")) {
                                // Logout successful, handle the response as needed
//                                Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                            } else {
//                                String message = jsonObject.getString("message");
//                                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("JSONError", "Error parsing response", e);
                            Toast.makeText(MainActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        Log.e("VolleyError", "Error logging out", error);
//                        Toast.makeText(MainActivity.this, "Error logging out: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(id));
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
    private void loggedIn(BigInteger id) {
        String url = Constant.Loggedin; // Make sure Constant.Loggedout is the correct URL

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("LogoutResponse", "Response: " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            if (status.equals("success")) {
                                // Logout successful, handle the response as needed
//                                Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                            } else {
//                                String message = jsonObject.getString("message");
//                                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
//                            Log.e("JSONError", "Error parsing response", e);
//                            Toast.makeText(MainActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        Log.e("VolleyError", "Error logging out", error);
//                        Toast.makeText(MainActivity.this, "Error logging out: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(id));
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void clearAppStatePreferences() {
        SharedPreferences sp = getSharedPreferences("app_state", MODE_PRIVATE);
        sp.edit().clear().apply();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START); // Close the drawer if open
        } else {
            super.onBackPressed();
            finishAffinity();
//            loggedOut(Id);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

//        loggedIn(Id);
        // You can also add any other setup code that should run each time the activity becomes visible
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Save the current selected button
        SharedPreferences sp = getSharedPreferences("app_state", MODE_PRIVATE);
        sp.edit().putString("selected_button", currentSelectedButton).apply();
    }

    private void fetchCommisson(BigInteger id , String mobileNumber) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constant.fetchDuePayment + id, null,
                response -> {
                    try {
                        Log.d("JSON_RESPONSE", response.toString());
                        String status = response.getString("status");

                        if (status.equals("success")) {
                            // Check if commission_details exists in the response
                            if (response.has("commission_details") && !response.isNull("commission_details")) {
                                JSONObject commissionDetails = response.getJSONObject("commission_details");

                                // Get commission status and amount
                                Integer companyStatus = commissionDetails.getInt("commission_status");
                                String order_id = commissionDetails.getString("order_id");
                                String customer_name = commissionDetails.getString("customer_name");
                                Double service_charge = commissionDetails.getDouble("service_charge");
                                Double gst = commissionDetails.getDouble("gst");
                                Double payable_amount = commissionDetails.getDouble("payable_amount");
                                String payable_status = commissionDetails.getString("payment_status");

                                if (companyStatus == 0 && payable_status.equals("Completed")) {
                                    showCommissionPaymentDialog(order_id, service_charge, gst, payable_amount, mobileNumber,companyStatus,payable_status,customer_name);
                                } else if (companyStatus == 0 && payable_status.equals("Pending")) {
                                    showCommissionPaymentDialog(order_id, service_charge, gst, payable_amount, mobileNumber,companyStatus,payable_status,customer_name);
                                }
                            } else {
                                // Handle case when there are no commission details
                                // Check if there's a message from the server
                                String message = response.optString("message", "No pending previous payment");
                                Log.d("MainActivity", "No commission details: " + message);

                                // Optionally show a toast or update UI
                                // Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();

                                // You can also check the commission_pending flag if needed
                                boolean hasPendingCommission = response.optBoolean("commission_pending", false);
                                if (!hasPendingCommission) {
                                    // No pending commissions - you might want to hide any commission-related UI
                                    // For example:
                                    // runOnUiThread(() -> {
                                    //     commissionLayout.setVisibility(View.GONE);
                                    // });
                                }
                            }
                        } else {
                            // Handle error case from server
                            String message = response.optString("message", "Unknown error");
                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(MainActivity.this, "Error fetching commission data", Toast.LENGTH_SHORT).show();
                });
        requestQueue.add(jsonObjectRequest);
    }

    private void showCommissionPaymentDialog(String orderId, Double serviceCharge, Double gst, Double payableAmount, String mobileNumber, Integer companyStatus, String payableStatus, String customerName) {
        PaymentDialogActivity.start(this,orderId,serviceCharge,gst,payableAmount,mobileNumber,companyStatus,payableStatus,customerName);
    }



    private void checkForUpdate() {

        appUpdateManager = AppUpdateManagerFactory.create(this);

        appUpdateManager.getAppUpdateInfo()
                .addOnSuccessListener(appUpdateInfo -> {

                    if (appUpdateInfo.updateAvailability()
                            == UpdateAvailability.UPDATE_AVAILABLE
                            && appUpdateInfo.isUpdateTypeAllowed(
                            AppUpdateType.FLEXIBLE)) {

                        try {
                            appUpdateManager.startUpdateFlowForResult(
                                    appUpdateInfo,
                                    AppUpdateType.FLEXIBLE,
                                    this,
                                    UPDATE_REQUEST_CODE
                            );
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    private void checkRequiredPermissions() {

        ArrayList<String> permissions = new ArrayList<>();

        // Camera
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);
        }

        // Location
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        // Notification - Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        // Phone State
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
        ) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_PHONE_STATE);
        }

        // Request only missing permissions
        if (!permissions.isEmpty()) {

            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    permissions.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE
            );

        } else {

            Log.d("PERMISSION", "All permissions granted");
        }
    }


    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == PERMISSION_REQUEST_CODE) {

            boolean allGranted = true;

            for (int result : grantResults) {

                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {

                Log.d("PERMISSION", "All permissions granted");

            } else {

                // Don't show Toast
                // Don't show another dialog here

                Log.d(
                        "PERMISSION",
                        "Some permissions were denied"
                );
            }
        }
    }



    @Override
    protected void onResume() {
        super.onResume();

        // Restore the selected button
        SharedPreferences sp = getSharedPreferences("app_state", MODE_PRIVATE);
        currentSelectedButton = sp.getString("selected_button", "today"); // default to today

        // Programmatically click the appropriate button
        switch(currentSelectedButton) {
            case "today":
                Today.performClick();
                break;
            case "tomorrow":
                Tomorrow.performClick();
                break;
            case "yesterday":
                yesterdayTextView.performClick();
                break;
            case "calender":
                if (!selectedCalendarDate.isEmpty()) {
                    // Directly fetch bookings for the saved date without showing calendar
                    fetchBookings(Id, "specific_date", selectedCalendarDate);
                    // Update UI to show calendar is selected
                    Cal.setCardBackgroundColor(getResources().getColor(R.color.Blue));
                    Tomorrow.setBackgroundColor(getResources().getColor(R.color.white));
                    Today.setTextColor(Color.BLUE);
                    Tomorrow.setTextColor(Color.BLUE);
                    Today.setBackgroundColor(getResources().getColor(R.color.white));
                    yesterdayTextView.setBackgroundColor(getResources().getColor(R.color.white));
                    yesterdayTextView.setTextColor(Color.BLUE);
                    calendarIcon.setColorFilter(getResources().getColor(R.color.white));
                } else {
                    // Fallback to today if no date was saved
                    Today.performClick();
                }
                break;
            default:
                Today.performClick();
        }

        fetchVerification(Id);
        fetchWorkingStatus(Id);
        Experienceed(Id);
        Review(Id);

//        Hours(Id);
//        Upgrades(Id);
    }

    @Override
    protected void onDestroy() {
        // Check if the app is really finishing (not just rotating screen)
        if (isFinishing()) {
            clearAppStatePreferences();
        }
        super.onDestroy();
    }



}
