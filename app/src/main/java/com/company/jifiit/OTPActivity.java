package com.company.jifiit;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Hotel.HotelMainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.messaging.FirebaseMessaging;
import org.json.JSONException;
import org.json.JSONObject;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class OTPActivity extends AppCompatActivity {
    private static final long TIMER_DURATION = 60000; // 1 minute
    private static final long TIMER_INTERVAL = 1000; // 1 second

    private CountDownTimer countDownTimer;
    private TextInputEditText otp1, otp2, otp3, otp4;
    private boolean resend_timer = false;
    private TextView userMobile, resendBtn;
    private String phone, code, token,userType,userId;
    private MaterialButton VerifyOtpBtn;

    // OTP sending control variables
    private final AtomicBoolean isOtpSendingInProgress = new AtomicBoolean(false);
    private boolean isInitialOTPSent = false;
    private boolean isActivityReady = false;
    private boolean isFirstResume = true;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        setContentView(R.layout.activity_otpactivity);

        // Initialize views
        resendBtn = findViewById(R.id.resendBtn);
        userMobile = findViewById(R.id.otpMobile);
        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        VerifyOtpBtn = findViewById(R.id.VerifyOtpBtn);

        // Get phone number from intent
        phone = getIntent().getStringExtra("phone");
        userId = getIntent().getStringExtra("user_id");
        userType = getIntent().getStringExtra("user_type");
        if (phone == null) {
            Toast.makeText(this, "Phone number not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userMobile.setText(phone);

        // Setup OTP fields
        setupOtpEditTexts();

        // Get FCM token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        token = task.getResult();
                        Log.d("FCM Token", token);
                    }
                });

        // Set click listeners
        resendBtn.setOnClickListener(v -> {
            if (resend_timer) {
                sendOTP(phone, true);
                Log.d("ResendOTP", "Resend button clicked");
            }
        });

        // Delay initial OTP send to ensure everything is ready
        new Handler().postDelayed(() -> {
            isActivityReady = true;
            maybeSendInitialOTP();
        }, 500);
    }

    private void checkAndAutoVerify() {
        String userEnteredOTP = otp1.getText().toString() +
                otp2.getText().toString() +
                otp3.getText().toString() +
                otp4.getText().toString();

        if (userEnteredOTP.length() == 4 && code != null && userEnteredOTP.equals(code)) {
            // Automatically trigger verification
            Verify(VerifyOtpBtn);
        }
    }

    private void maybeSendInitialOTP() {
        if (isActivityReady && !isInitialOTPSent && !isOtpSendingInProgress.get()) {
            sendOTP(phone, false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstResume) {
            isFirstResume = false;
            return; // Skip the first resume to prevent duplicate sends
        }
        maybeSendInitialOTP();
    }

    private void resetOtpFields() {
        otp1.setText("");
        otp2.setText("");
        otp3.setText("");
        otp4.setText("");
        otp1.requestFocus();
        otp2.setEnabled(false);
        otp3.setEnabled(false);
        otp4.setEnabled(false);
    }

    private void setupOtpEditTexts() {
        otp2.setEnabled(false);
        otp3.setEnabled(false);
        otp4.setEnabled(false);

        otp1.addTextChangedListener(new OTPTextWatcher(otp1, otp2));
        otp2.addTextChangedListener(new OTPTextWatcher(otp2, otp3));
        otp3.addTextChangedListener(new OTPTextWatcher(otp3, otp4));
        otp4.addTextChangedListener(new OTPTextWatcher(otp4, null));

        otp1.setOnKeyListener(new OTPKeyListener(otp1, null));
        otp2.setOnKeyListener(new OTPKeyListener(otp2, otp1));
        otp3.setOnKeyListener(new OTPKeyListener(otp3, otp2));
        otp4.setOnKeyListener(new OTPKeyListener(otp4, otp3));

        otp1.requestFocus();
    }

    private void sendOTP(String phone, boolean isResend) {
        if (!isResend && (isOtpSendingInProgress.get() || isInitialOTPSent)) {
            return;
        }

        if (!isOtpSendingInProgress.compareAndSet(false, true)) {
            return;
        }

        if (isResend) {
            resetOtpFields();
            isInitialOTPSent = false;
            startCountDownTimer();
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.jifiit_will_send_you_a_sms_massage_to_verify_your_phone_number));
        progressDialog.setCancelable(false);
        progressDialog.show();

        if (progressDialog.getWindow() != null) {
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.LogOTPRequest,
                response -> {
                    progressDialog.dismiss();
                    isOtpSendingInProgress.set(false);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");
                        if (status.equals("success")) {
                            code = jsonObject.getString("otp");
//                            Log.d("OTP",  code);
                            isInitialOTPSent = true;
                            if (!isResend) {
                                startCountDownTimer();
                            }
                        } else {
                            Toast.makeText(OTPActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(OTPActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    isOtpSendingInProgress.set(false);
                    if (!isInitialOTPSent) {
                        Toast.makeText(OTPActivity.this, "Error sending OTP", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("number", phone);
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(stringRequest);
    }

    private void startCountDownTimer() {
        resend_timer = false;
        resendBtn.setTextColor(Color.parseColor("#C10101"));

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(TIMER_DURATION, TIMER_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                resendBtn.setText("Resend Code (" + (millisUntilFinished / 1000) + ")");
            }

            @Override
            public void onFinish() {
                resend_timer = true;
                resendBtn.setText("Resend Code");
                resendBtn.setTextColor(getResources().getColor(R.color.Green));
            }
        }.start();
    }

    public void Verify(View view) {
        String userEnteredOTP = otp1.getText().toString() +
                otp2.getText().toString() +
                otp3.getText().toString() +
                otp4.getText().toString();

        if (userEnteredOTP.length() != 4) {
            Toast.makeText(this, "Please enter complete OTP", Toast.LENGTH_SHORT).show();
        } else if (code == null) {
            Toast.makeText(this, "OTP not received yet", Toast.LENGTH_SHORT).show();
        } else if (userEnteredOTP.equals(code)) {
            login(userId,userType);
        } else {
            Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
        }
    }

    private void login(String Id, String userType) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.Login,
                response -> {
                    Log.d("LoginResponse", "Response: " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        if (status.equals("success")) {
//                            Toast.makeText(this, "Mobile:"+ phone +"And User Type: "+ userType , Toast.LENGTH_SHORT).show();
                            fetchUserData(phone,userType);
                        } else {
                            Toast.makeText(OTPActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(OTPActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(OTPActivity.this, "Error logging in", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("phone", phone);
                params.put("Id", Id);
                params.put("userType", userType);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void fetchUserData(String phone, String userType) {
        String encodedPhone = Uri.encode(phone);
        String encodedUserType = Uri.encode(userType);

        String url = Constant.fetchAccount
                 + encodedPhone
                +"&userType=" + encodedUserType;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String status = response.getString("status");
                        if (status.equals("success")) {
                            JSONObject data = response.getJSONObject("data");
                            String name = "";
                            BigInteger Id = BigInteger.valueOf(data.getInt("id"));
                            String VerificationStatus = data.getString("verification");
                            String Age="0000-00-00";
                            if (userType.equals("expert")){
                                Age = data.getString("date_of_birth");
                                name = data.getString("name");
                            } else if (userType.equals("hotel")) {
                                name = data.getString("hotel_name");

                            }

                            String Mobile = phone;

                            saveUserNameToSharedPreferences(name, Id, VerificationStatus, Age, Mobile,userType);
                            loggedIn(Id, token,userType);
                            if (userType.equals("expert")){
                                startActivity(new Intent(OTPActivity.this, MainActivity.class));
                                overridePendingTransition(
                                        R.anim.slide_in_right,
                                        R.anim.slide_out_left
                                );
                                finish();
                            } else if (userType.equals("hotel")) {
                                startActivity(new Intent(OTPActivity.this, HotelMainActivity.class));
                                overridePendingTransition(
                                        R.anim.slide_in_right,
                                        R.anim.slide_out_left
                                );
                                finish();
                            }else if (userType.equals("wellness")) {

                            }else if (userType.equals("centre")) {

                            }


                        }
                    } catch (JSONException e) {
                        Toast.makeText(OTPActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(OTPActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void saveUserNameToSharedPreferences(String name, BigInteger Id, String Verification, String age, String Mobile, String userType) {
        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("Name", name);
        editor.putString("Id", Id.toString());
        editor.putString("Verification", Verification);
        editor.putString("Age", age);
        editor.putString("mobile", Mobile);
        editor.putString("userType", userType);
        editor.apply();
    }

    private void loggedIn(BigInteger id, String token, String userType) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.Loggedin,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (!jsonObject.getString("status").equals("success")) {
                            Log.e("LoginStatus", "Failed to update login status");
                        }
                    } catch (JSONException e) {
                        Log.e("JSONError", "Error parsing response", e);
                    }
                },
                error -> Log.e("VolleyError", "Error logging in", error)) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(id));
                params.put("token", token);
                params.put("userType", userType);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private class OTPTextWatcher implements TextWatcher {
        private final EditText currentEditText;
        private final EditText nextEditText;

        public OTPTextWatcher(EditText currentEditText, EditText nextEditText) {
            this.currentEditText = currentEditText;
            this.nextEditText = nextEditText;
        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();
            if (text.length() == 1 && nextEditText != null) {
                nextEditText.setEnabled(true);
                nextEditText.requestFocus();
            } else if (text.isEmpty() && nextEditText != null) {
                nextEditText.setEnabled(false);
                nextEditText.setText("");
            }

            // Check for auto-verification
            checkAndAutoVerify();
        }
    }

    private class OTPKeyListener implements View.OnKeyListener {
        private final EditText currentEditText;
        private final EditText previousEditText;

        public OTPKeyListener(EditText currentEditText, EditText previousEditText) {
            this.currentEditText = currentEditText;
            this.previousEditText = previousEditText;
        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (currentEditText.getText().toString().isEmpty() && previousEditText != null) {
                    previousEditText.setEnabled(true);
                    previousEditText.requestFocus();
                    previousEditText.setText("");
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        overridePendingTransition(
                R.anim.slide_in_left,
                R.anim.slide_out_right
        );
    }
}