package com.company.jifiit.ViewMoreMenu;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;

import android.app.ProgressDialog;

import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;

import android.os.Bundle;

import android.os.CountDownTimer;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;

import com.android.volley.toolbox.JsonObjectRequest;

import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.company.jifiit.Constant;
import com.company.jifiit.LocalStorage.DatabaseHelper;
import com.company.jifiit.MainActivity;
import com.company.jifiit.MassageWorkingActivity;
import com.company.jifiit.MyQr.My_Qr;
import com.company.jifiit.R;
import com.company.jifiit.ViewMoreActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;


import org.json.JSONException;
import org.json.JSONObject;


import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;



public class CompletedActivity2 extends AppCompatActivity {


    private boolean isQrCodeSelected = false;
    TextView link, text, amountTxt, senderName;
    Button feedBackLink, invoiceLink;
    CardView cash, Qr_code;
    String Orderid, names;
    ImageView qrCodeImageView;
    double totalPrice;

    DatabaseHelper dbHelper;
    LinearLayout paymentLayout, completedLayout;
    CardView qrCardContainer;

    Button completeBtn, RefreshBtn;
    private CountDownTimer qrCountDownTimer;
    LinearLayout qrLayout, footer;
    LinearLayout cashLayout;
    private boolean isPaymentCompleted = false;
    private boolean has_upi = false;
    String Upi_Id,Upi_name;

    BigInteger EmployeeId;
    TableLayout tableLayout;
    String customerName, userStatus, mobile, T_mobile;

    @SuppressLint({"MissingInflatedId", "LocalSuppress"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed2);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Payment");

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);


        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String name = sp.getString("Name", "Default Name");
        T_mobile = sp.getString("mobile", "Default Name");
        String idString = sp.getString("Id", "");
        EmployeeId = new BigInteger(idString);


        cash = findViewById(R.id.cashCard);
        Qr_code = findViewById(R.id.qrCard);
        text = findViewById(R.id.paymentInProgressText);
        amountTxt = findViewById(R.id.AmountTxt);
        senderName = findViewById(R.id.senderName);
        completeBtn = findViewById(R.id.backbtn);
        RefreshBtn = findViewById(R.id.refreshBtn);
        feedBackLink = findViewById(R.id.feedBackLink);
        invoiceLink = findViewById(R.id.invoiceLink);


        qrCodeImageView = findViewById(R.id.qrCodeImageView);
        qrCardContainer = findViewById(R.id.qrCardContainer);

        completedLayout = findViewById(R.id.completedLayout);
        paymentLayout = findViewById(R.id.paymentLayout);
        footer = findViewById(R.id.footer);
        Orderid = getIntent().getStringExtra("id");
        names = getIntent().getStringExtra("customer_name");

        text.setText("Order Id: " + Orderid);
        senderName.setText("Scan to pay,\n" + names);
        senderName.setVisibility(View.GONE);
        text.setVisibility(View.GONE);
//      footer.setVisibility(View.GONE);
        qrLayout = findViewById(R.id.qr_layout);
        cashLayout = findViewById(R.id.cash_layout);

        completeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (dbHelper != null) {
                        dbHelper.deleteProducts(Orderid);
                    } else {
                        Log.e("Database", "DatabaseHelper not initialized");
                    }
                    Intent intent = new Intent(CompletedActivity2.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish(); // Finish current activity
                } catch (Exception e) {
                    Log.e("Database", "Error deleting products: " + e.getMessage());
                    // Still proceed to MainActivity even if delete fails
                    Intent intent = new Intent(CompletedActivity2.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        });
        RefreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CompletedActivity2.this);
                builder.setTitle(names + "\nTotal Cash ₹" + totalPrice)
                        .setMessage("Have You Collect The Booking Amount?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Call the submitCash() function when OK is clicked
                            submitCash(Orderid, "online");
//                            finish();
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });


        feedBackLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = Constant.feedbackLink + Orderid;
                showQRCodeDialog(url);
            }
        });
        invoiceLink.setOnClickListener(v -> {
            // Replace the direct URL sharing with secure version
            shareInvoice(); // This uses the hashed URL
        });
    }

    // Method to download PDF
    private void shareInvoice() {
        try {
            // Generate secure hash
            String secureHash = generateSecureHash(String.valueOf(Orderid));
            String secureUrl = Constant.shareInvoice + "?hash=" + secureHash;

            String message = "Hello Dear *" + names.trim() + "*,\n\n"
                    + "Thank you for choosing Jiifit LLP — JIIFIT India’s 1st specialised massage company for clients place.\n\n"
                    + "Your massage session has been successfully completed.\n"
                    + "Please find your invoice below:\n\n"
                    + "Click here to download your invoice:\n\n"
                    + secureUrl + "\n\n"
                    + "We look forward to serving you again soon!\n\n"
                    + "Warm regards,\n"
                    + "Team Jiifit\n"
                    + "www.jiifit.com";

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Invoice #" + Orderid);
            shareIntent.putExtra(Intent.EXTRA_TEXT, message);

            startActivity(Intent.createChooser(shareIntent, "Share Invoice"));
        } catch (Exception e) {
            Toast.makeText(this, "Error sharing invoice", Toast.LENGTH_SHORT).show();
            Log.e("SharePDF", "Sharing failed", e);
        }
    }


    private String generateSecureHash(String orderId) {
        try {
            // Store this salt in your Constants file instead of hardcoding
            String salt = Constant.HASH_SALT;
            String input = orderId + salt;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = String.format("%02x", b);
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            Log.e("HashError", "Failed to generate hash", e);
            return ""; // Fail securely
        }
    }

    private void showQRCodeDialog(String url) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(url, BarcodeFormat.QR_CODE, 400, 400);

            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_qr_code);

            ImageView qrCodeImageView = dialog.findViewById(R.id.qrCodeImageView);
            qrCodeImageView.setImageBitmap(bitmap);

            // Close icon functionality
            ImageView closeIcon = dialog.findViewById(R.id.closeIcon);
            closeIcon.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating QR code", Toast.LENGTH_SHORT).show();
        }

    }


    private void checkPaymentStatus(String orderId, BigInteger TherapistId) {
        // Define the URL for the API, appending the order ID parameter
        String url = Constant.checkPayment + orderId + "&id=" + TherapistId;

        // Create a JsonObjectRequest to retrieve the payment status
        @SuppressLint("ResourceAsColor") JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    // Handle the response from the server
                    boolean success = response.optBoolean("success", true);

                    if (success) {
                        // Extract the payment status and total price
                        String paymentStatus = response.optString("payment_status", "unknown");
                        totalPrice = response.optDouble("total_price", 0);
                        boolean hasUpi = response.optBoolean("has_upi", true);
                        Upi_Id = response.optString("upi_id", "unknown");
                        Upi_name = response.optString("upi_name", "unknown");
//                        String status = response.optString("status", "failed");
                        double amoun = 10.00;

                        amountTxt.setText("Amount To be paid: ₹" + totalPrice);
                        amountTxt.setVisibility(View.VISIBLE);
                        if (paymentStatus.equalsIgnoreCase("Completed")) {
                            isPaymentCompleted = true;
                            paymentLayout.setVisibility(View.GONE);
                            completedLayout.setVisibility(View.VISIBLE);

                            qrCodeImageView.setVisibility(View.GONE);
                            qrCardContainer.setVisibility(View.GONE);
                            text.setVisibility(View.GONE);
                            senderName.setVisibility(View.GONE);
                            completeBtn.setVisibility(View.VISIBLE);
                            RefreshBtn.setVisibility(View.GONE);
//                            }
                            Toast.makeText(this, "Payment Completed", Toast.LENGTH_SHORT).show();
                        } else if (paymentStatus.equalsIgnoreCase("failed")) {
                            Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show();
                        } else if (paymentStatus.equalsIgnoreCase("Pending")) {
                            // Existing UPI payment flow
                            String upiId = Upi_Id; // Your UPI ID
                            String name = Upi_name; // Merchant name
                            String note = "Payment for Order: " + orderId; // Transaction note

                            // Generate the UPI payment URL
                            String upiPaymentUrl = "upi://pay?pa=" + Uri.encode(upiId) +
                                    "&pn=" + Uri.encode(name) +
                                    "&tn=" + Uri.encode(note) +
                                    "&am=" + Uri.encode(String.valueOf(totalPrice)) +
                                    "&cu=" + Uri.encode("INR");

                            // Set up QR code generation on button click
                            Qr_code.setOnClickListener(v -> {
                                if (!hasUpi) {
                                    Intent intent = new Intent(CompletedActivity2.this, My_Qr.class);
                                    startActivity(intent);
                                } else {
                                    // Cancel any existing timer
                                    if (qrCountDownTimer != null) {
                                        qrCountDownTimer.cancel();
                                    }

                                    // Reset cash selection
                                    isQrCodeSelected = true;
                                    cashLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.creamy_white));
                                    completeBtn.setVisibility(View.GONE);
                                    RefreshBtn.setVisibility(View.VISIBLE);

                                    // Generate and show QR code
                                    generateQRCode(upiPaymentUrl);
                                    qrCodeImageView.setVisibility(View.VISIBLE);
                                    qrCardContainer.setVisibility(View.VISIBLE);
                                    senderName.setVisibility(View.VISIBLE);
                                    amountTxt.setVisibility(View.VISIBLE);
                                    text.setVisibility(View.VISIBLE);
                                    qrLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.deep_creamy_white));

                                    // Show timer
                                    TextView timerText = findViewById(R.id.timerText);
                                    timerText.setVisibility(View.VISIBLE);

                                    // Start fresh 60 second countdown
                                    qrCountDownTimer = new CountDownTimer(60000, 1000) {
                                        public void onTick(long millisUntilFinished) {
                                            String statusText = "Checking payment status... ";
                                            String countdownText = millisUntilFinished / 1000 + "s";
                                            String fullText = statusText + countdownText;

                                            SpannableString spannable = new SpannableString(fullText);

                                            spannable.setSpan(
                                                    new ForegroundColorSpan(ContextCompat.getColor(CompletedActivity2.this, R.color.black)),
                                                    0,
                                                    statusText.length(),
                                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                            );

                                            spannable.setSpan(
                                                    new ForegroundColorSpan(ContextCompat.getColor(CompletedActivity2.this, R.color.Green)),
                                                    statusText.length(),
                                                    fullText.length(),
                                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                            );

                                            timerText.setText(spannable);
                                        }

                                        public void onFinish() {
                                            resetQRPaymentState();
                                        }
                                    }.start();
                                }
                            });



                            cash.setOnClickListener(v -> {
                                try {
                                    if (qrCountDownTimer != null) {
                                        qrCountDownTimer.cancel();
                                        qrCountDownTimer = null;
                                    }

                                    // Reset QR payment state
                                    resetQRPaymentState();

                                    qrCodeImageView.setVisibility(View.GONE);
                                    qrCardContainer.setVisibility(View.GONE);
                                    completeBtn.setVisibility(View.GONE);
                                    RefreshBtn.setVisibility(View.GONE);
                                    text.setVisibility(View.GONE);
                                    senderName.setVisibility(View.GONE);
                                    cashLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.deep_creamy_white));
                                    qrLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.creamy_white));

                                    // Show confirmation dialog
                                    AlertDialog.Builder builder = new AlertDialog.Builder(CompletedActivity2.this);
                                    builder.setTitle(names + "\nTotal Cash ₹" + totalPrice)
                                            .setMessage("Do you have cash in hand?")
                                            .setPositiveButton("OK", (dialog, which) -> {
                                                // Call the submitCash() function when OK is clicked
                                                submitCash(orderId, "cash");
                                                cash.setBackgroundColor(Color.TRANSPARENT);
                                            })
                                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                            .show();
                                } catch (Exception e) {
                                    Toast.makeText(CompletedActivity2.this, "An error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(this, "Invalid response from server", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle unsuccessful response
                        String errorMessage = response.optString("message", "Unknown error occurred");
                        Toast.makeText(this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Log the error details for debugging
                    Log.e("PaymentStatus", "Error: " + error.toString());
                    Toast.makeText(this, "Error fetching payment status", Toast.LENGTH_SHORT).show();
                }
        );

        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void resetQRPaymentState() {
        qrCodeImageView.setVisibility(View.GONE);
        qrCardContainer.setVisibility(View.GONE);
        completeBtn.setVisibility(View.GONE);
        RefreshBtn.setVisibility(View.GONE);

        text.setVisibility(View.GONE);
        senderName.setVisibility(View.GONE);

        TextView timerText = findViewById(R.id.timerText);
        if (timerText != null) {
            timerText.setVisibility(View.GONE);
        }

        qrLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.creamy_white));
        isQrCodeSelected = false;
    }

    private void submitCash(String orderId, String type) {
        String url = Constant.paymentUpdate;

        // Use a modern progress indicator
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading, please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show(); // Show progress dialog

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss(); // Hide progress dialog
                    try {
                        // Check if the response is a valid JSON format
                        if (response.trim().startsWith("{") || response.trim().startsWith("[")) {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.optString("status");
                            String message = jsonResponse.optString("message");
                            if ("success".equalsIgnoreCase(status)) {
                                Toast.makeText(CompletedActivity2.this, message, Toast.LENGTH_SHORT).show();
                                checkPaymentStatus(orderId,EmployeeId);
                            } else {
                                Toast.makeText(CompletedActivity2.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("InvalidResponse", "Response is not JSON: " + response);
                            Toast.makeText(this, "Unexpected server response", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(CompletedActivity2.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss(); // Hide progress dialog
                    String errorMessage = (error.getMessage() != null) ? error.getMessage() : "An unexpected error occurred";
                    Toast.makeText(CompletedActivity2.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("order_id", orderId);
                params.put("type", type);
                return params;
            }
        };

        // Add the request to the queue
        Volley.newRequestQueue(this).add(stringRequest);
    }


    private void generateQRCode(String phonePePaymentUrl) {
        ImageView qrCodeImageView = findViewById(R.id.qrCodeImageView);

        try {
            // QR code generation logic
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(phonePePaymentUrl, BarcodeFormat.QR_CODE, 300, 300);
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(bitMatrix);

            // Display the QR code in the ImageView
            qrCodeImageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onBackPressed() {
        if (!isPaymentCompleted) {
            // Only allow back press if payment isn't completed
            super.onBackPressed();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        // If payment is completed, do nothing (back button is disabled)
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (!isPaymentCompleted) {
            onBackPressed();
            return true;
        }
        return false;  // Disable up navigation when payment is completed
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPaymentStatus(Orderid,EmployeeId);
    }
}
