package com.company.jifiit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Hotel.HotelMainActivity;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hbb20.CountryCodePicker;

import android.os.Build;
public class LoginActivity extends AppCompatActivity {
    private EditText phoneInput;
    String phone;
//    char[] code;

    String code,token;
    CountryCodePicker countryCodePicker;
    Button sendOtpBtn, RegisterButton , Verify;
    EditText Otp;
    ProgressBar progressBar;

    private ViewPager2 viewPager;
    private ImageSliderAdapter adapter;
    private List<Integer> images = Arrays.asList(
            R.mipmap.massage_at_home,
            R.mipmap.yoga,
            R.mipmap.zumba,
            R.mipmap.personal_trainer,
            R.mipmap.physiotherapist,
            R.mipmap.nutrionist,
            R.mipmap.hotel_expert,
            R.mipmap.ayurveda,
            R.mipmap.chiropractor,
            R.mipmap.discover_customers,
            R.mipmap.product_list


    );
    private TextView sliderText;
    private String[] texts;

    private LinearLayout dotsLayout;
    private ImageView[] dots;

    private int currentPage = 0;
    private final int delay = 3000; // 3 seconds delay
    private final Handler handler = new Handler();
    private final Runnable runnable = new Runnable() {
        public void run() {
            if (adapter.getItemCount() == currentPage) {
                currentPage = 0;
            }
            viewPager.setCurrentItem(currentPage++, true);
            handler.postDelayed(this, delay);
        }
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        verifyuserexistance();
        viewPager = findViewById(R.id.viewPager);
        adapter = new ImageSliderAdapter(images);
        viewPager.setAdapter(adapter);

        texts = new String[]{
                getString(R.string.text1),
                getString(R.string.text2),
                getString(R.string.text3),
                getString(R.string.text1),
                getString(R.string.text2),
                getString(R.string.text3),
                getString(R.string.text1),
                getString(R.string.text2),
                getString(R.string.text3),
                getString(R.string.text1),
                getString(R.string.text2)
        };

        dotsLayout = findViewById(R.id.dotsLayout);
        addDots();

        sliderText = findViewById(R.id.sliderText);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateDots(position);
                sliderText.setText(texts[position]);
            }
        });

        // Start auto-scrolling
        handler.postDelayed(runnable, delay);

        countryCodePicker = findViewById(R.id.login_countrycode);
        phoneInput = findViewById(R.id.login_mobile_number);
        sendOtpBtn = findViewById(R.id.send_Otp_btn);
        RegisterButton = findViewById(R.id.registerButton);
        progressBar = findViewById(R.id.login_progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        countryCodePicker.registerCarrierNumberEditText(phoneInput);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // API 29+ supports direct cursor color
            phoneInput.setTextCursorDrawable(null); // reset first
            android.graphics.drawable.Drawable cursor =
                    ContextCompat.getDrawable(this, R.drawable.cursor_medium_ash);
            if (cursor != null) {
                phoneInput.setTextCursorDrawable(cursor);
            }
        } else {
            // Pre-API 29: use reflection to set the cursor drawable color
            try {
                java.lang.reflect.Field f = android.widget.TextView.class
                        .getDeclaredField("mCursorDrawableRes");
                f.setAccessible(true);
                f.setInt(phoneInput, R.drawable.cursor_medium_ash);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        countryCodePicker.setDialogBackgroundColor(Color.WHITE);
        countryCodePicker.setDialogTextColor(Color.BLACK);



        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        token = task.getResult();
                        Log.d("FCM Token", token);
//                      sendTokenToServer(token); // Send token to your PHP backend
                    }
                });


        sendOtpBtn.setOnClickListener((v) -> {
            if (!countryCodePicker.isValidFullNumber()) {
                phoneInput.setError("Phone number not valid");
                return;
            }
        sendOTP();
//           login();

        });



        Otp=findViewById(R.id.otp1);
        Verify = findViewById(R.id.verifyOtpButton);
        Verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputCode = Otp.getText().toString();
                phone = countryCodePicker.getFullNumberWithPlus();
                if (inputCode.equals("")) {
                    Toast.makeText(LoginActivity.this, "Please Enter Otp", Toast.LENGTH_SHORT).show();
                 } else {
                    if (inputCode.equals(String.valueOf(code))) {
                        login();
                    } else {
                        Toast.makeText(LoginActivity.this, "failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        });

        progressBar.setVisibility(View.GONE);

    }

    private void sendTokenToServer(String token) {

    }

    private void sendOTP() {

        phone = countryCodePicker.getFullNumberWithPlus();

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                Constant.getAccounts,

                response -> {
                    try {

                        JSONObject jsonObject = new JSONObject(response);

                        String status = jsonObject.optString("status");
                        String message = jsonObject.optString("message");

                        if ("success".equals(status)) {

                            JSONArray accounts = jsonObject.optJSONArray("accounts");

                            if (accounts != null && accounts.length() > 0) {

                                Intent intent = new Intent(
                                        LoginActivity.this,
                                        AccountSelectionActivity.class
                                );

                                intent.putExtra("phone", phone);
                                intent.putExtra("accounts", accounts.toString());

                                startActivity(intent);
                                overridePendingTransition(
                                        R.anim.slide_in_right,
                                        R.anim.slide_out_left
                                );


                            } else {
                                Log.d("Number is: ",phone);
                                Toast.makeText(
                                        LoginActivity.this,
                                        "No accounts found",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }

                        } else {

                            Toast.makeText(
                                    LoginActivity.this,
                                    message,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                    } catch (JSONException e) {

                        e.printStackTrace();

                        Toast.makeText(
                                LoginActivity.this,
                                "Invalid server response",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                },

                error -> {

                    error.printStackTrace();

                    Toast.makeText(
                            LoginActivity.this,
                            "Unable to check account",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        ) {

            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<>();

                params.put("number", phone);

                return params;
            }
        };

        queue.add(stringRequest);
    }

        public void login() {
        phone = countryCodePicker.getFullNumberWithPlus();

        if (phone.isEmpty()) {
            // Handle empty phone number
            Toast.makeText(LoginActivity.this, "Please enter a phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.Login,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("LoginResponse", "Response: " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            if (status.equals("success")) {
                                // Login successful, save credentials and start MainActivity
                                fetchUserData(phone);

                            } else {
                                String message = jsonObject.getString("message");
                                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("JSONError", "Error parsing response", e);
                            Toast.makeText(LoginActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VolleyError", "Error logging in", error);
                        Toast.makeText(LoginActivity.this, "Error logging in: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("phone", phone);
                // Add other login parameters here if needed
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


    private void fetchUserData(String phone) {
        // Create a request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // Create a request to fetch user data
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constant.fetchAccount + phone, null,
                response -> {
                    try {
                        // Log the response for debugging
                        Log.d("JSON_RESPONSE", response.toString());

                        // Parse the JSON response
                        String status = response.getString("status");

                        // Check if the status is success
                        if (status.equals("success")) {
                            // Extract user data
                            JSONObject data = response.getJSONObject("data");
                            String name = data.getString("name");
                            BigInteger Id = BigInteger.valueOf(data.getInt("id"));
                            String VerificationStatus = data.getString("verification");
                            String Age = data.getString("date_of_birth");
                            String Mobile = data.getString("phone");

                            // Save the t_Name to SharedPreferences
                            saveUserNameToSharedPreferences(name, Id,VerificationStatus , Age , Mobile);

                            // Start MainActivity
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            loggedIn(Id,token);
                            startActivity(intent);
                            finish(); // Optional: Close the LoginActivity to prevent going back
                            Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        } else {
                            String message = response.getString("message");
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();

                        Toast.makeText(LoginActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Handle errors
                    Toast.makeText(LoginActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                });

        // Add the request to the request queue
        requestQueue.add(jsonObjectRequest);
    }


    private void saveUserNameToSharedPreferences(String name, BigInteger Id, String Verification, String age, String Mobile) {
        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("Name", name);
        editor.putString("Id", Id.toString()); // Convert BigInteger to String
        editor.putString("com/company/jifiit/Verification", Verification);
        editor.putString("Age", age);
        editor.putString("mobile",Mobile);
        editor.apply();
    }


    private void addDots() {
        dots = new ImageView[images.size()];
        for (int i = 0; i < images.size(); i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.dot_inactive));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            dotsLayout.addView(dots[i], params);
        }

        updateDots(0); // Set the initial dot as active
    }

    private void updateDots(int position) {
        for (int i = 0; i < images.size(); i++) {
            dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.dot_inactive));
        }

        dots[position].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.dot_active));
    }

    private void loggedIn(BigInteger id, String token) {
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
                            Log.e("JSONError", "Error parsing response", e);
                            Log.e("LoginError", "Invalid JSON: " + response);
                            Toast.makeText(LoginActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VolleyError", "Error logging out", error);

                        Toast.makeText(LoginActivity.this, "Error logging out: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(id));
                params.put("token",token);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop auto-scrolling when the activity is destroyed
        handler.removeCallbacks(runnable);
    }

    public void verifyuserexistance() {

        SharedPreferences sp =
                getSharedPreferences("credentials", MODE_PRIVATE);

        // Check whether user is already logged in
        if (sp.contains("Id")) {

            String userType = sp.getString("userType", "");

            if (userType.equals("expert")) {

                startActivity(
                        new Intent(this, MainActivity.class)
                );

            } else if (userType.equals("hotel")) {

                startActivity(new Intent(this, HotelMainActivity.class));
            }

            finish();
        }
    }

    public void OpenReg(View view) {

        startActivity(new Intent(this, JoinerTypes.class));

        // Swipe animation: current screen moves left, new screen comes from right
        overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_left
        );

        phoneInput.setText("");
    }


}

