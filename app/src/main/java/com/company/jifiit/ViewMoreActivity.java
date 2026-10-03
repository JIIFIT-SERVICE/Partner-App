package com.company.jifiit;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;

import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.company.jifiit.ViewMoreMenu.CompletePaymenteActivity;
import com.company.jifiit.ViewMoreMenu.CompletedActivity;
import com.company.jifiit.ViewMoreMenu.FareActivity;
import com.company.jifiit.ViewMoreMenu.HelpsActivity;
import com.company.jifiit.ViewMoreMenu.RescheduleActivity;
import com.company.jifiit.ViewMoreMenu.UpgradeActivity;
import com.google.android.material.chip.Chip;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.util.List;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class ViewMoreActivity extends AppCompatActivity {

    private TextView Customer_Name, Customer_id,bookingStatus,paymensts, Massage_types, note, Status, FullAddress, TimeDuration, Massage, Address, Date, Time, Booking_id,receiverName,receiverRelationship,callReceiverButton,locationReceiverButton;

    Button ask_btn, Call_btn, start_btn;

    private String phoneNumbersForAllSlots; // To store all slot numbers
    private String slot1Number = null;
    private String slot2Number = null;
    private static final String TAG = "SimCardDetection";
    private static final int REQUEST_CODE = 1;
    private String fullAddress;
    private Map<String, String> numberMapping = new HashMap<>();
    Bitmap bitmap1;
    String encodedimage1;
    BigInteger EmployeeId;
    TableLayout tableLayout;
    String customerName, userStatus, mobile, T_mobile,reciverMobile;
    Chip need_bed, call_after_booking, ring_the_bell, do_not_call_before_reaching;
    ConstraintLayout dataLayout;
    public String otpStatus ="0";
    String fetchingOTP,name;
    String Id, startTime, endTime, duration, address, booking_statu, payment_status,formattedDate;
    CardView receiverCard;


    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        setContentView(R.layout.activity_view_more);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set title and back button colors
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Booking Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Set toolbar color and back button color
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        toolbar.getNavigationIcon().setTint(getResources().getColor(android.R.color.black));

        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String name = sp.getString("Name", "Default Name");
        T_mobile = sp.getString("mobile", "Default Name");
        String idString = sp.getString("Id", "");
        EmployeeId = new BigInteger(idString);

        Id = getIntent().getStringExtra("id");
        customerName = getIntent().getStringExtra("customer_name");
//        startTime = getIntent().getStringExtra("start_time");
        duration = getIntent().getStringExtra("duration");
        address = getIntent().getStringExtra("landmark");
        String paymentStatus = getIntent().getStringExtra("payment_status");
        String massageName = getIntent().getStringExtra("massage_name");
        paymensts = findViewById(R.id.paymentStatus);
        Massage_types = findViewById(R.id.massagetypes);

        receiverCard = findViewById(R.id.receiverCard);
        receiverName = findViewById(R.id.receiverName);
        receiverRelationship = findViewById(R.id.receiverRelationship);
        callReceiverButton = findViewById(R.id.callReceiverButton);
        locationReceiverButton = findViewById(R.id.locationReceiverButton);





        Customer_Name = findViewById(R.id.CusName);
  TextView Booking_id = findViewById(R.id.Booking_Id);
        Time = findViewById(R.id.time);
        Date = findViewById(R.id.date);
        Customer_id = findViewById(R.id.CusId);
//        Massage_types = findViewById(R.id.category);
        FullAddress = findViewById(R.id.address);
        note = findViewById(R.id.full_notes);
        Status = findViewById(R.id.status);

        Call_btn = findViewById(R.id.call_button);
        ask_btn = findViewById(R.id.ask_button);
        start_btn = findViewById(R.id.Start_button);

        tableLayout = findViewById(R.id.tableLayout);
        dataLayout = findViewById(R.id.data);


//        fetchData(Id);

        need_bed = findViewById(R.id.need_bed);
        call_after_booking = findViewById(R.id.call_after_booking);
        ring_the_bell = findViewById(R.id.ring_the_bell);
        do_not_call_before_reaching = findViewById(R.id.do_not_call_before_reaching);


        Booking_id.setText("Booking id:-  " + Id);
        Customer_Name.setText(customerName);

        Status.setText(userStatus);

//        details(Id);

        ask_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ask_btn.setBackgroundColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.ShapGreen));
                ask_btn.setTextColor(Color.WHITE);
                Call_btn.setBackgroundColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.white));
                Call_btn.setTextColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.ShapGreen));
                start_btn.setBackgroundColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.white));
                start_btn.setTextColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.ShapGreen));
                // Ensure you get the correct text from your TextView
                String dateText = Date.getText().toString(); // Replace `dateTextView` with your actual TextView's variable

                String message = "Hello *Dear client*, \n" +
                        "My name is *" + name + "*,\n" +
                        "I am your massage therapist for your booking schedule on " + startTime + " at " + dateText + ".\n" +
                        "To ensure a hassle-free experience and to avoid repeatedly asking for your address, could you please share your current location? This will greatly assist me in reaching you promptly.\n\n" +
                        "Thank you for your understanding! Looking forward to seeing you soon.\n\n" +
                        "Best regards,\n" +
                        name + "\n" +
                        "Jiifit LLP";

                // Encode the message
                String encodedMessage = Uri.encode(message);

                // Construct the WhatsApp URL
                String wpUrl = "https://wa.me/" + mobile + "?text=" + encodedMessage;

                // Start the intent
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(wpUrl));
                startActivity(intent);
            }
        });

        locationReceiverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                locationReceiverButton.setBackgroundColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.ShapGreen));
//                locationReceiverButton.setTextColor(Color.WHITE);
//                callReceiverButton.setBackgroundColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.transparent));
//                callReceiverButton.setTextColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.ShapGreen));
                // Ensure you get the correct text from your TextView
                String dateText = Date.getText().toString(); // Replace `dateTextView` with your actual TextView's variable

                String message = "Hello *Dear client*, \n" +
                        "My name is *" + name + "*,\n" +
                        "I am your massage therapist for your booking schedule on " + startTime + " at " + dateText + ".\n" +
                        "To ensure a hassle-free experience and to avoid repeatedly asking for your address, could you please share your current location? This will greatly assist me in reaching you promptly.\n\n" +
                        "Thank you for your understanding! Looking forward to seeing you soon.\n\n" +
                        "Best regards,\n" +
                        name + "\n" +
                        "Jiifit LLP";

                // Encode the message
                String encodedMessage = Uri.encode(message);

                // Construct the WhatsApp URL
                String wpUrl = "https://wa.me/" + reciverMobile + "?text=" + encodedMessage;

                // Start the intent
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(wpUrl));
                startActivity(intent);
            }
        });

        Call_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update button UI
                Call_btn.setBackgroundColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.ShapGreen));
                Call_btn.setTextColor(Color.WHITE);
                ask_btn.setBackgroundColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.white));
                ask_btn.setTextColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.ShapGreen));
                start_btn.setBackgroundColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.white));
                start_btn.setTextColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.ShapGreen));

                // Simply open the dialer with the mobile number
                openDialer(mobile);
            }
        });

        callReceiverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update button UI
//                callReceiverButton.setBackgroundColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.ShapGreen));
//                callReceiverButton.setTextColor(Color.WHITE);
//                locationReceiverButton.setBackgroundColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.transparent));
//                locationReceiverButton.setTextColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.ShapGreen));

                // Simply open the dialer with the mobile number
                openDialer(reciverMobile);
            }
        });





    }
    private void openDialer(String phoneNumber) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to open dialer", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchData(String id) {
        String url = Constant.tableFetched + id;
        LinearLayout servicesContainer = findViewById(R.id.servicesContainer);
        servicesContainer.removeAllViews(); // Clear any existing views

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    double subtotal = 0;
                    double totalDiscount = 0;
                    double originalTotal = 0;

                    // Add headers
                    addServiceHeader(servicesContainer);

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject jsonObject = response.getJSONObject(i);

                            String itemName = jsonObject.getString("item_name");
                            int itemDuration = jsonObject.getInt("item_duration");
                            int itemQuantity = jsonObject.getInt("item_quantity");
                            double itemDiscount = jsonObject.getDouble("item_discounts");
                            double itemOriginalPrice = jsonObject.getDouble("item_price");
                            double itemDiscountPrice = jsonObject.getDouble("item_discount_price");

                            // Calculate totals
                            double serviceOriginalTotal = itemOriginalPrice * itemQuantity;
                            double serviceDiscountedTotal = itemDiscountPrice * itemQuantity;
                            double serviceDiscount = serviceOriginalTotal - serviceDiscountedTotal;

                            originalTotal += serviceOriginalTotal;
                            subtotal += serviceDiscountedTotal;
                            totalDiscount += serviceDiscount;

                            // Add service row to the layout
                            addServiceRow(servicesContainer,
                                    itemName + " (" + itemDuration + " min) × " + itemQuantity,
                                    "₹ " + String.format("%.2f", serviceOriginalTotal),
                                    "₹ " + String.format("%.2f", serviceDiscountedTotal));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    // Calculate GST (5%)
                    double gst = subtotal * 0.05;
                    double total = subtotal + gst;

                    // Update totals
                    TextView subtotalText = findViewById(R.id.subtotalText);
                    TextView gstText = findViewById(R.id.gstText);
                    TextView totalText = findViewById(R.id.totalText);
                    TextView savingsText = findViewById(R.id.savingsText);

                    subtotalText.setText("₹ " + String.format("%.2f", subtotal));
                    gstText.setText("₹ " + String.format("%.2f", gst));
                    totalText.setText("₹ " + String.format("%.2f", total));

//                    if (totalDiscount > 0) {
//                        savingsText.setText("You saved ₹ " + String.format("%.2f", totalDiscount) + " on this booking");
//                    }

                },
                error -> Toast.makeText(ViewMoreActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show());

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest);
    }

    private void addServiceHeader(LinearLayout container) {
        // Create header layout
        ConstraintLayout headerLayout = new ConstraintLayout(this);
        headerLayout.setLayoutParams(new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT));

        // Service Name Header
        TextView serviceNameHeader = new TextView(this);
        serviceNameHeader.setId(View.generateViewId());
        serviceNameHeader.setText("Service");
        serviceNameHeader.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        serviceNameHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        serviceNameHeader.setTypeface(null, Typeface.BOLD);

        ConstraintLayout.LayoutParams nameParams = new ConstraintLayout.LayoutParams(
                0, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        nameParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        nameParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        nameParams.endToStart = View.generateViewId(); // Will be set for original price header
        nameParams.width = 0;
        nameParams.matchConstraintPercentWidth = 0.5f;
        serviceNameHeader.setLayoutParams(nameParams);
        headerLayout.addView(serviceNameHeader);

        // Original Price Header
        TextView originalPriceHeader = new TextView(this);
        originalPriceHeader.setId(View.generateViewId());
        originalPriceHeader.setText("Original");
        originalPriceHeader.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        originalPriceHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        originalPriceHeader.setTypeface(null, Typeface.BOLD);
        originalPriceHeader.setGravity(Gravity.END);

        ConstraintLayout.LayoutParams originalParams = new ConstraintLayout.LayoutParams(
                0, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        originalParams.startToEnd = serviceNameHeader.getId();
        originalParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        originalParams.endToStart = View.generateViewId(); // Will be set for discounted price header
        originalParams.width = 0;
        originalParams.matchConstraintPercentWidth = 0.25f;
        originalPriceHeader.setLayoutParams(originalParams);
        headerLayout.addView(originalPriceHeader);

        // Discounted Price Header
        TextView discountedPriceHeader = new TextView(this);
        discountedPriceHeader.setId(View.generateViewId());
        discountedPriceHeader.setText("Price");
        discountedPriceHeader.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        discountedPriceHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        discountedPriceHeader.setTypeface(null, Typeface.BOLD);
        discountedPriceHeader.setGravity(Gravity.END);

        ConstraintLayout.LayoutParams discountedParams = new ConstraintLayout.LayoutParams(
                0, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        discountedParams.startToEnd = originalPriceHeader.getId();
        discountedParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        discountedParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        discountedParams.width = 0;
        discountedParams.matchConstraintPercentWidth = 0.25f;
        discountedPriceHeader.setLayoutParams(discountedParams);
        headerLayout.addView(discountedPriceHeader);

        container.addView(headerLayout);
    }

    private void addServiceRow(LinearLayout container, String serviceName, String originalPrice, String discountedPrice) {
        // Create row layout
        ConstraintLayout rowLayout = new ConstraintLayout(this);
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        rowLayout.setLayoutParams(layoutParams);

        // Service Name
        TextView nameText = new TextView(this);
        nameText.setId(View.generateViewId());
        nameText.setText(serviceName);
        nameText.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        nameText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

        ConstraintLayout.LayoutParams nameParams = new ConstraintLayout.LayoutParams(
                0, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        nameParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        nameParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        nameParams.width = 0;
        nameParams.matchConstraintPercentWidth = 0.5f;
        nameText.setLayoutParams(nameParams);
        rowLayout.addView(nameText);

        // Original Price
        TextView originalText = new TextView(this);
        originalText.setId(View.generateViewId());
        originalText.setText(originalPrice);
        originalText.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        originalText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        originalText.setGravity(Gravity.END);

        ConstraintLayout.LayoutParams originalParams = new ConstraintLayout.LayoutParams(
                0, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        originalParams.startToEnd = nameText.getId();
        originalParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        originalParams.width = 0;
        originalParams.matchConstraintPercentWidth = 0.25f;
        originalText.setLayoutParams(originalParams);
        rowLayout.addView(originalText);

        // Discounted Price
        TextView discountedText = new TextView(this);
        discountedText.setId(View.generateViewId());
        discountedText.setText(discountedPrice);
        discountedText.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        discountedText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        discountedText.setGravity(Gravity.END);

        ConstraintLayout.LayoutParams discountedParams = new ConstraintLayout.LayoutParams(
                0, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        discountedParams.startToEnd = originalText.getId();
        discountedParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        discountedParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        discountedParams.width = 0;
        discountedParams.matchConstraintPercentWidth = 0.25f;
        discountedText.setLayoutParams(discountedParams);
        rowLayout.addView(discountedText);

        container.addView(rowLayout);
    }


    private void details(String id) {
        // Show progress bar while request is in progress
        dataLayout.setVisibility(View.GONE);

        // Check if the device is connected to the internet
        if (!isNetworkAvailable()) {
            // If there's no internet connection, show a Toast message
            Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
            return; // Exit the method if there's no internet
        }

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // Create the JSON request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                Constant.Details + id,
                null,
                response -> {
                    // Hide progress bar and show data layout when response is received
                    dataLayout.setVisibility(View.VISIBLE);

                    try {
                        String status = response.getString("status");
                        if ("success".equals(status)) {
                            JSONObject data = response.getJSONObject("data");
                            Button paymentBtn = findViewById(R.id.creditsBtn);
                            // Set customer ID and massage type
                            Customer_id.setText("Id:  " + data.getString("customer_id"));
                            String massageTypes = data.getString("massage_types");
                            FullAddress.setText(data.getString("full_address"));
                            note.setText(data.getString("note"));
                            userStatus = data.getString("user_status");
                            mobile = data.getString("mobile");
                            this.startTime = data.getString("start_time");
                            endTime = data.getString("end_time");
                            booking_statu = data.getString("status");
                            payment_status = data.getString("payment_status");
                            otpStatus = data.getString("otp_status");
                            String Amount = data.getString("total_price");
                            String mode = data.getString("payment_mode");
                            String who_take = data.getString("who_take");
                            String reciver_name = data.getString("reciver_name");
                            reciverMobile = data.getString("r_wp_num");
                            String relationship = data.getString("relationship");
                            String photoPath = data.getString("photoPath");
                            invalidateOptionsMenu();

                            if ("Completed".equals(payment_status)) {

                                paymensts.setText("paid");
                                paymensts.setBackground(ContextCompat.getDrawable(ViewMoreActivity.this, R.drawable.button_green));
                            } else if ("Pending".equals(payment_status)) {
                                if ("cash".equals(mode)) {
                                    paymensts.setText("cash");
                                } else if ("online".equals(mode)) {
                                    paymensts.setText("online");
                                }
                            } else {
                                paymensts.setText("Canceled");
                            }

                            if ("Home Massage".equals(massageTypes)) {
                                Massage_types.setText("massage at home");
                            } else if ("Hotel Massage".equals(massageTypes)) {
                                Massage_types.setText("massage at hotel");
                            } else if ("Corporate Massage".equals(massageTypes)) {
                                Massage_types.setText("Massage At Corporate");
                            }

                            if("Someone Else".equals(who_take)){
                                receiverCard.setVisibility(View.VISIBLE);
                                receiverName.setText(reciver_name);
                                receiverRelationship.setText(relationship);
                            }else{
                                receiverCard.setVisibility(View.GONE);
                                receiverName.setText("");
                                receiverRelationship.setText("");
                            }





                            // Set onClickListener for the start button
                            start_btn.setOnClickListener(v -> {
                                start_btn.setBackgroundColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.ShapGreen));
                                start_btn.setTextColor(Color.WHITE);
                                ask_btn.setBackgroundColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.white));
                                ask_btn.setTextColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.ShapGreen));
                                Call_btn.setBackgroundColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.white));
                                Call_btn.setTextColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.ShapGreen));
                                if ("1".equals(otpStatus) && photoPath.equals("")){
                                    attendance(Id,EmployeeId);
                                } else if ("0".equals(otpStatus) && photoPath.equals("")) {
                                    Intent intent = new Intent(ViewMoreActivity.this, PhotoUpload.class);
                                    intent.putExtra("id",Id);
                                    intent.putExtra("start_time",startTime);
                                    intent.putExtra("end_time",endTime);
                                    intent.putExtra("customer_name",customerName);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                } else {
                                    Intent intent = new Intent(ViewMoreActivity.this, MassageWorkingActivity.class);
                                    intent.putExtra("id",Id);
                                    intent.putExtra("start_time",startTime);
                                    intent.putExtra("end_time",endTime);
                                    intent.putExtra("customer_name",customerName);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }

                            });
                            Call_btn.setVisibility(View.GONE);
                            ask_btn.setVisibility(View.GONE);
                            start_btn.setVisibility(View.GONE);
                            paymentBtn.setVisibility(View.GONE);
                            // Show or hide buttons based on the booking and payment status
                            if ("Completed".equals(payment_status) && "Completed".equals(booking_statu)) {
                                paymentBtn.setVisibility(View.GONE);
                                paymentBtn.setText("Payment Completed");
                                paymentBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.Green));
                                paymentBtn.setTextColor(Color.WHITE);
                            } else if ("Completed".equals(booking_statu) && "Pending".equals(payment_status)) {
                                paymentBtn.setVisibility(View.VISIBLE);
                                Call_btn.setVisibility(View.VISIBLE);
                                ask_btn.setVisibility(View.VISIBLE);
                                ask_btn.setText("Ask Payment");
                                paymentBtn.setText("Payment...");
                                paymentBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.orange));
                                paymentBtn.setTextColor(Color.WHITE);
                                paymentBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Log.d("customer_name", customerName != null ? customerName : "Name is null");
//                                        Intent intent = new Intent(ViewMoreActivity.this, CompletedActivity.class);

                                        Intent intent = new Intent(ViewMoreActivity.this, CompletePaymenteActivity.class);
                                        intent.putExtra("id", Id);
                                        intent.putExtra("customer_name", customerName);
                                        startActivity(intent);

                                    }
                                });

                                ask_btn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        showPaymentDialog(Amount,mobile);
                                    }
                                });
                            } else {
                                Call_btn.setVisibility(View.VISIBLE);
                                ask_btn.setVisibility(View.VISIBLE);
                                start_btn.setVisibility(View.VISIBLE);
                                // paymentBtn remains GONE
                            }
                            // Set user status color and text
                            if ("Regular".equalsIgnoreCase(userStatus)) {
                                Status.setTextColor(ContextCompat.getColor(this, R.color.orange)); // Set orange color
                            } else if ("New".equalsIgnoreCase(userStatus)) {
                                Status.setTextColor(ContextCompat.getColor(this, R.color.Green)); // Set green color
                            } else if ("Old".equalsIgnoreCase(userStatus)) {
                                Status.setTextColor(ContextCompat.getColor(this, R.color.Red)); // Set red color
                            }
                            Status.setTypeface(Status.getTypeface(), Typeface.BOLD); // Make text bold
                            Status.setText("(" + userStatus + ")");
                            need_bed.setVisibility(View.VISIBLE);
                            // Check and set visibility for additional fields
                            if (data.getInt("need_bed") == 1) {
                                need_bed.setText("Yes Bed");
                            } else {
                                need_bed.setText("No Bed");
                            }

                            if (data.getInt("call_after_booking") == 1) {
                                call_after_booking.setChipBackgroundColorResource(R.color.ShapGreen);
                                call_after_booking.setTextColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.white));
                                call_after_booking.setVisibility(View.VISIBLE);
                            } else {
                                call_after_booking.setVisibility(View.GONE);
                            }

                            if (data.getInt("ring_bell") == 1) {
                                ring_the_bell.setChipBackgroundColorResource(R.color.ShapGreen);
                                ring_the_bell.setTextColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.white));
                                ring_the_bell.setVisibility(View.VISIBLE);
                            } else {
                                ring_the_bell.setVisibility(View.GONE);
                            }

                            if (data.getInt("do_not_call_before_reaching") == 1) {
                                do_not_call_before_reaching.setChipBackgroundColorResource(R.color.ShapGreen);
                                do_not_call_before_reaching.setTextColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.white));
                                do_not_call_before_reaching.setVisibility(View.VISIBLE);
                            } else {
                                do_not_call_before_reaching.setVisibility(View.GONE);
                            }

                            // Format and display order date and time
                            String orderDate = data.getString("order_date");
                            this.formattedDate = formatDateToDDMMYYYY(orderDate);
                            Date.setText(formattedDate);
                            Time.setText(startTime);

                        } else {
                            // Show error message if status is not success
                            String message = response.getString("message");
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        // Handle JSON parsing error
                        Log.e("ViewMoreActivity", "JSON Parsing error: " + e.getMessage());
                    }
                },
                error -> {
                    // Hide progress bar and show error message on failure
                    dataLayout.setVisibility(View.GONE);
                    Log.e("ViewMoreActivity", "Volley error: " + error.getMessage());
                    Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_SHORT).show();
                }
        );

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }



    private void attendance(String id, BigInteger employeeId) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_custom_layout, null);

        // Find views in the custom layout
        EditText otpDigit1 = dialogView.findViewById(R.id.otpDigit1);
        EditText otpDigit2 = dialogView.findViewById(R.id.otpDigit2);
        EditText otpDigit3 = dialogView.findViewById(R.id.otpDigit3);
        EditText otpDigit4 = dialogView.findViewById(R.id.otpDigit4);
        Button dialogButton = dialogView.findViewById(R.id.dialogButton);
        ImageView closeIcon = dialogView.findViewById(R.id.closeIcon);
        TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
        TextView errorTextView = dialogView.findViewById(R.id.errorTextView);

        dialogMessage.setText("Enter the 4-digit OTP");
        // Create AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Set focus order
        setOtpInputBehavior(otpDigit1, otpDigit2, null);
        setOtpInputBehavior(otpDigit2, otpDigit3, otpDigit1);
        setOtpInputBehavior(otpDigit3, otpDigit4, otpDigit2);
        setOtpInputBehavior(otpDigit4, null, otpDigit3);

        // Handle submit button click
        dialogButton.setOnClickListener(v -> {
            // Get OTP when submit button is clicked
            String enteredOtp = otpDigit1.getText().toString() +
                    otpDigit2.getText().toString() +
                    otpDigit3.getText().toString() +
                    otpDigit4.getText().toString();

            // Validate OTP input
            if (enteredOtp.length() != 4) {
                Toast.makeText(ViewMoreActivity.this, "Please enter a valid 4-digit OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            // Fetch the OTP from server and compare
            fetchOtp(id, employeeId, new OtpFetchCallback() {
                @Override
                public void onOtpFetched(String fetchedOtp) {
                    runOnUiThread(() -> {
                        if (enteredOtp.equals(fetchedOtp)) {
                            // OTP matched - proceed with attendance
                            errorTextView.setText("OTP verified successfully!");
                            errorTextView.setTextColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.Green));

                            // You can add your attendance marking logic here
                            // markAttendance(id, employeeId);
                            Intent intent = new Intent(ViewMoreActivity.this, PhotoUpload.class);
                            intent.putExtra("id",Id);
                            intent.putExtra("start_time",startTime);
                            intent.putExtra("end_time",endTime);
                            intent.putExtra("customer_name",customerName);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            otpstatusUpdate(id,employeeId);
                            startActivity(intent);
                            // Close dialog after delay
                            new Handler().postDelayed(dialog::dismiss, 1000);
                        } else {
                            // OTP didn't match
                            errorTextView.setText("Invalid OTP. Please try again.");
                            errorTextView.setTextColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.Red));
                        }
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> {
                        errorTextView.setText(errorMessage);
                        errorTextView.setTextColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.Red));
                    });
                }
            });
        });

        // Handle close icon click
        closeIcon.setOnClickListener(v -> dialog.dismiss());
    }

    // OtpFetchCallback interface
    interface OtpFetchCallback {
        void onOtpFetched(String otp);
        void onError(String errorMessage);
    }

    private void setOtpInputBehavior(final EditText current, final EditText next, final EditText previous) {
        current.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1 && next != null) {
                    next.requestFocus(); // Move to next EditText
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        current.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (current.getText().toString().isEmpty() && previous != null) {
                        previous.requestFocus(); // Move cursor back on delete
                    }
                }
                return false;
            }
        });

        // Prevent filling other boxes before the first one
        current.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && previous != null && previous.getText().toString().isEmpty()) {
                    previous.requestFocus(); // Ensure correct sequence
                }
            }
        });
    }






    // Method to check if network is available
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }


    // Method to format date from YYYY-MM-DD to DD-MM-YYYY
    private String formatDateToDDMMYYYY(String dateStr) {
        try {
            // Input format (from the server)
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            // Output format (desired format)
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");

            // Parse the input date string into a Date object
            Date date = inputFormat.parse(dateStr);

            // Format the date into the desired output format and return it
            return outputFormat.format(date);

        } catch (Exception e) {
            e.printStackTrace();
            // Return original date string if formatting fails
            return dateStr;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.viewmoremenu, menu);
        return true; // No changes here
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem exchangeItem = menu.findItem(R.id.Exchange);
        MenuItem rescheduleItem = menu.findItem(R.id.rescheduleItem);
//        Log.d("PrepareOptionsMenu", "otpStatus value: " + otpStatus);
        // Hide or show the "Exchange" option based on otpStatus
        // Hide or show the "Exchange" option based on otpStatus
        if ("1".equals(this.otpStatus)) {
            exchangeItem.setVisible(true);
            rescheduleItem.setVisible(true);
        } else {
            exchangeItem.setVisible(false);
            rescheduleItem.setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.rescheduleItem) {
            Intent intent = new Intent(ViewMoreActivity.this, RescheduleActivity.class);
            intent.putExtra("id", Id);
            intent.putExtra("time", startTime);
            intent.putExtra("duration", duration);
            intent.putExtra("endTime", endTime);
            intent.putExtra("date", formattedDate);
            startActivity(intent);
        } else if (id == R.id.Exchange) {
            Intent intent = new Intent(ViewMoreActivity.this, UpgradeActivity.class);
            intent.putExtra("id", Id);
            intent.putExtra("bookingType", "Exchange");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (id == R.id.Helps) {
            Intent intent = new Intent(ViewMoreActivity.this, HelpsActivity.class);
            intent.putExtra("id", Id);
            intent.putExtra("time", startTime);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
//       else if (id == R.id.fare) {
//            Intent intent = new Intent(ViewMoreActivity.this, FareActivity.class);
//            intent.putExtra("id", Id);
//            intent.putExtra("time", startTime);
//            intent.putExtra("location", address);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
//        }

        return super.onOptionsItemSelected(item);
    }

    // Handle the permission request result



    private void fetchOtp(String id, BigInteger therapistId, OtpFetchCallback callback) {
        String url = Constant.fetchBookingOtp + id;
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");

                        if (status.equals("success")) {
                            String otp = jsonObject.getString("booking_otp");
                            callback.onOtpFetched(otp);
                        } else {
                            String message = jsonObject.optString("message", "Failed to fetch OTP");
                            callback.onError(message);
                        }
                    } catch (JSONException e) {
                        callback.onError("Error parsing response");
                    }
                },
                error -> callback.onError("Network error: " + error.getMessage()));

        queue.add(stringRequest);
    }





    private void otpstatusUpdate(String id, BigInteger therapistId) {
        String url = Constant.otp_status_update;
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    try {
                        // First check if response is valid JSON
                        if (response.trim().startsWith("{") || response.trim().startsWith("[")) {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");

                            if ("success".equals(status)) {
                                Log.d("OTP Status Update", "OTP status updated successfully!");
                            } else {
                                String message = jsonResponse.optString("message", "Unknown error");
                                Toast.makeText(this, "Failed: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Handle non-JSON response
                            Log.e("OTP Status Update", "Non-JSON response: " + response);
                            Toast.makeText(this, "Server returned invalid response", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("OTP Status Update", "JSON Parsing error: " + e.getMessage());
                        Toast.makeText(this, "Error processing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    String errorMessage = "Network error";
                    if (error.networkResponse != null) {
                        try {
                            errorMessage = new String(error.networkResponse.data, "UTF-8");
                            // Try to parse as JSON even for errors
                            if (errorMessage.trim().startsWith("{")) {
                                JSONObject errorJson = new JSONObject(errorMessage);
                                errorMessage = errorJson.optString("message", errorMessage);
                            }
                        } catch (Exception e) {
                            errorMessage = error.getMessage();
                        }
                    }
                    Toast.makeText(this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                params.put("therapist_id", String.valueOf(therapistId));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }
        };

        // Set timeout and retry policy
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000, // 10 seconds timeout
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(stringRequest);
    }



    private void showPaymentDialog(String amount, String mobile) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_payment);

        // Make dialog background transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // Initialize views
        ImageView qrCodeImage = dialog.findViewById(R.id.qr_code_image);
        Button whatsappBtn = dialog.findViewById(R.id.whatsapp_button);
        Button closeBtn = dialog.findViewById(R.id.close_button);
        TextView upiIdText = dialog.findViewById(R.id.upi_id_text);
//        TextView amountText = dialog.findViewById(R.id.amount_text);

        // Set payment details
        String upiId = "jiifitindia@hdfcbank";
        String name = "JIIFIT LLP";

        // Format UPI payment string
        String paymentData = "upi://pay?pa=" + upiId +
                "&pn=" + URLEncoder.encode(name) +
                (amount.isEmpty() ? "" : "&am=" + amount) +
                "&cu=INR";

        // Display payment details
        upiIdText.setText("UPI ID: " + upiId);
        upiIdText.setVisibility(View.GONE);
//        amountText.setText("Amount: ₹" + amount);

        // Generate and show QR code
        try {
            Bitmap qrBitmap = generateQRCode(paymentData, 800);
            qrCodeImage.setImageBitmap(qrBitmap);

            // WhatsApp button - shares payment details and QR code
            whatsappBtn.setOnClickListener(v -> {
                String shareMessage = "Payment Request\n\n" +
                        "Please send ₹" + amount + " to:\n" +
                        "UPI ID: " + upiId + "\n" +
                        "Name: " + name + "\n\n" +
                        "Scan the QR code below to pay directly.";

                sendViaWhatsApp(shareMessage, qrBitmap, mobile);
                dialog.dismiss();
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error generating QR code", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        // Close button
        closeBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private Bitmap generateQRCode(String text, int size) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size, hints);

        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }

        return bitmap;
    }

    private void sendViaWhatsApp(String message, Bitmap qrBitmap, String phoneNumber) {
        Log.d("WhatsAppShare", phoneNumber);
        try {
            // 1. Create cache directory if it doesn't exist
            File cacheDir = new File(getCacheDir(), "images");
            if (!cacheDir.exists()) {
                if (!cacheDir.mkdirs()) {
                    Toast.makeText(this, "Failed to create cache directory", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // 2. Save QR code to file
            File qrFile = new File(cacheDir, "payment_qr_" + System.currentTimeMillis() + ".png");
            try (FileOutputStream fos = new FileOutputStream(qrFile)) {
                if (!qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)) {
                    Toast.makeText(this, "Failed to save QR image", Toast.LENGTH_SHORT).show();
                    return;
                }
                fos.flush();
            }

            // 3. Get content URI using FileProvider
            Uri contentUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    qrFile
            );
            // 4. Format phone number (remove all non-digit characters)
//            String formattedNumber = phoneNumber.replaceAll("[^0-9]", "");
//
//            if (formattedNumber.isEmpty()) {
//                Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
//                return;
//            }



            // 5. Create WhatsApp intent with multiple package options
            Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
            whatsappIntent.setType("image/*");
            whatsappIntent.putExtra(Intent.EXTRA_TEXT, message);
            whatsappIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Try both regular WhatsApp and WhatsApp Business
            String[] whatsappPackages = {"com.whatsapp", "com.whatsapp.w4b"};

            for (String pkg : whatsappPackages) {
                whatsappIntent.setPackage(pkg);
//                Log.d("WhatsAppShare", "formatted number: " + formattedNumber);
                whatsappIntent.setData(Uri.parse("https://wa.me/"+phoneNumber));

                if (whatsappIntent.resolveActivity(getPackageManager()) != null) {
                    try {
                        startActivity(whatsappIntent);
                        return; // Exit if successful
                    } catch (SecurityException e) {
                        Log.e("WhatsAppShare", "Permission error", e);
                    } catch (ActivityNotFoundException e) {
                        Log.e("WhatsAppShare", "Activity not found", e);
                    }
                }
            }

            // 6. Fallback to generic share if WhatsApp not found
            shareViaOtherApps(message, contentUri);

        } catch (IOException e) {
            Log.e("WhatsAppShare", "File error", e);
            Toast.makeText(this, "Error saving QR code", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException e) {
            Log.e("WhatsAppShare", "FileProvider error", e);
            Toast.makeText(this, "Configuration error", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("WhatsAppShare", "Unexpected error", e);
            Toast.makeText(this, "Error sharing payment", Toast.LENGTH_SHORT).show();
        }
    }
    private void shareViaOtherApps(String message, Uri imageUri) {
        Intent fallbackIntent = new Intent(Intent.ACTION_SEND);
        fallbackIntent.setType("image/*");
        fallbackIntent.putExtra(Intent.EXTRA_TEXT, message);
        fallbackIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        fallbackIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(fallbackIntent, "Share Payment Details"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Call the details function with the appropriate ID
        details(Id);
        fetchData(Id);
        start_btn.setBackgroundColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.white));
        start_btn.setTextColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.ShapGreen));
        ask_btn.setBackgroundColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.white));
        ask_btn.setTextColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.ShapGreen));
        Call_btn.setBackgroundColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.white));
        Call_btn.setTextColor(ContextCompat.getColor(ViewMoreActivity.this, R.color.ShapGreen));
        // Replace orderId with the actual variable if needed
    }




    @Override
    public boolean onSupportNavigateUp () {
        onBackPressed();
        return true;
    }


}
