package com.company.jifiit;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Adapter.RegisterAdapter;
import com.hbb20.CountryCodePicker;

import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;


public class Register extends AppCompatActivity {


    EditText regName, regAge,City,regAddress, regMobile, regEmail, Aadhaar,ReferralCode;
    TextView currentAge;


    CountryCodePicker countryCodePicker;
    RadioGroup radioGroup;
    ViewPager viewPager;
    String[] slideTexts;
    RegisterAdapter registerAdapter;
    TextView sliderText;
    CircleImageView img1;
    Button regButton,calender;
    ImageView  LivPhot;
    Bitmap bitmap1;
    String encodedimage1;

    private int currentImageNumber;
    //    public OkHttpClient client = new OkHttpClient();
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            int currentItem = viewPager.getCurrentItem();
            int nextItem = (currentItem + 1) % slideTexts.length;
            viewPager.setCurrentItem(nextItem, true);
            handler.postDelayed(this, 3000); // 3 seconds delay
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Your Title");
        }




        // Initialize views
        regName = findViewById(R.id.reg_name);
        regName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        regAge = findViewById(R.id.reg_age);
        regAge.setClickable(false);
        regAge.setFocusable(false);
        calender = findViewById(R.id.tvCalendar);
        regAddress = findViewById(R.id.reg_address);
        regAddress.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        regMobile = findViewById(R.id.reg_mobile);
        countryCodePicker = findViewById(R.id.VerifyCountryCode);
        countryCodePicker.registerCarrierNumberEditText(regMobile);
        regEmail = findViewById(R.id.reg_email);
        radioGroup = findViewById(R.id.radioGroup);
        regButton = findViewById(R.id.reg_submit);
        currentAge=findViewById(R.id.current_age);
        Aadhaar = findViewById(R.id.reg_Aadhaar);
        ReferralCode=findViewById(R.id.reg_referral);

        City = findViewById(R.id.City_address);
        //String[] locations = {"Ahmedabad", "Bengaluru", "Chennai", "Delhi", "Gurgaon", "Hyderabad", "Kolkata", "New Delhi", "Noida", "Pune"};
        AutoCompleteTextView downLocation = (AutoCompleteTextView) City;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, Cities.CITIES);
        downLocation.setAdapter(adapter);

        downLocation.setBackgroundColor(Color.WHITE);
        downLocation.setInputType(InputType.TYPE_NULL);

        // Show drop-down list on touch and prevent keyboard from opening
        downLocation.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                downLocation.showDropDown();
                return false; // Return false to allow default behavior (focus, etc.)
            }
        });


        // Format Aadhaar number as the user types
        Aadhaar.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            private boolean deletingHyphen;
            private int hyphenStart;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (count > 0 && s.charAt(start) == ' ' && start > 0 && s.charAt(start - 1) != ' ') {
                    deletingHyphen = true;
                    hyphenStart = start - 1;
                } else {
                    deletingHyphen = false;
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting || deletingHyphen) {
                    return;
                }

                isFormatting = true;
                String input = s.toString().replaceAll("\\s+", ""); // Remove spaces
                if (input.length() > 12) {
                    input = input.substring(0, 12); // Limit to 12 characters
                }
                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < input.length(); i++) {
                    formatted.append(input.charAt(i));
                    if (i == 3 || i == 7) {
                        formatted.append(" "); // Add space after every 4th and 8th character
                    }
                }

                Aadhaar.setText(formatted.toString());
                Aadhaar.setSelection(formatted.length());

                isFormatting = false;

                if (deletingHyphen) {
                    Aadhaar.setSelection(hyphenStart);
                    deletingHyphen = false;
                }
            }
        });

        regEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String email = regEmail.getText().toString().trim();

                if (isValidEmail(email)) {
                    regEmail.setError(null); // Clear any error message
                } else {
                    regEmail.setError("Invalid email"); // Show error message
                }
            }

            private boolean isValidEmail(String email) {
                String emailRegex1 = "^[a-zA-Z0-9]{1,20}@[a-z0-9]{1,20}\\.[a-zA-Z]{2,3}$";
                String emailRegex2 = "^[a-zA-Z0-9]{1,20}\\.[a-zA-Z0-9]{1,20}@[a-z0-9]{1,20}\\.[a-zA-Z]{2,3}$";
                return email.matches(emailRegex1) || email.matches(emailRegex2);
            }
        });


        LivPhot = findViewById(R.id.uploadLivePhotoButton);

        img1 = findViewById(R.id.upLivePhoto);



        ImageView backBtn = findViewById(R.id.backbtn);
        backBtn.setOnClickListener(v -> onBackPressed());



        LivPhot.setOnClickListener(v -> OpenGallery());




//        EditText regAgeEditText = findViewById(R.id.reg_age);
        calender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });


        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    savedData();
            }
        });



        slideTexts = new String[]{
                getString(R.string.Slider1),
                getString(R.string.slider2),
                getString(R.string.slider3),
                getString(R.string.slider4),
                getString(R.string.slider5),
                getString(R.string.slider6),
                getString(R.string.slider7),
                getString(R.string.slider8),
                getString(R.string.text2),
                getString(R.string.slider9),

        };

        viewPager = findViewById(R.id.viewPager);
        registerAdapter = new RegisterAdapter(this, slideTexts);
        viewPager.setAdapter(registerAdapter);

        // Optionally, if you want to display the current slide text on a TextView
        sliderText = findViewById(R.id.slide_text);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
               //sliderText.setText(slideTexts[position]);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }

        });
        handler.postDelayed(runnable, 3000);


    }

    private void OpenGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        openGallery.launch(intent);
    }





    private String selectedDateOfBirth; // Add this variable at the top of your class

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar dob = Calendar.getInstance();
                        dob.set(year, month, dayOfMonth);
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        String dateString = sdf.format(dob.getTime());
                        selectedDateOfBirth = dateString; // Update selectedDateOfBirth with the selected date
                        regAge.setText(dateString);

                        // Calculate age
                        Calendar today = Calendar.getInstance();
                        int years = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
                        int months = today.get(Calendar.MONTH) - dob.get(Calendar.MONTH);
                        if (today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH)) {
                            months--;
                        }
                        if (months < 0) {
                            years--;
                            months += 12;
                        }

                        if (years < 18) {
                            currentAge.setText(R.string.age_warning);
                            currentAge.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        } else {
                            String ageText = "Your current age is : " + years + " years and " + months + " months";
                            currentAge.setText(ageText);
                            currentAge.setTextColor(getResources().getColor(R.color.Green));
                        }
                    }

                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
        datePickerDialog.show();
    }



    // ActivityResultLauncher for handling the result of the gallery intent
    ActivityResultLauncher<Intent> openGallery = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        if (result.getData() != null) {
                            Uri uriImage = result.getData().getData();
                            img1.setImageURI(uriImage);

                            try {
                                if (uriImage != null) {
                                    // Persist URI permission
                                    getContentResolver().takePersistableUriPermission(uriImage, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                    InputStream inputStream = getContentResolver().openInputStream(uriImage);
                                    bitmap1 = BitmapFactory.decodeStream(inputStream);
                                    encodeImage(bitmap1);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });

    private Bitmap getResizedBitmap(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();
        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = 500;
            height = (int) (width / bitmapRatio);
        } else {
            height = 500;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private void encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Bitmap resizedBitmap = getResizedBitmap(bitmap); // Resize to a max size of 500px
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream); // Compress further
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        encodedimage1 = Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
    private void sendEmail(String mobile) {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.email,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            String message = jsonObject.getString("message");
                            Toast.makeText(Register.this, message, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(Register.this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(Register.this, "Error sending Mail: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("phone", mobile);
                return params;
            }
        };

        queue.add(stringRequest);
    }

    public void savedData() {
        final String name = regName.getText().toString().trim();
        final String email = regEmail.getText().toString().trim();
        final String mobile = countryCodePicker.getFullNumberWithPlus();
        final String City_id = City.getText().toString().trim();
        final String address = regAddress.getText().toString().trim();
        final String aadhaarStr = Aadhaar.getText().toString().trim();
        final String referralCode = ReferralCode.getText().toString().trim();
        final String ageText = selectedDateOfBirth;

        final int selectedId = radioGroup.getCheckedRadioButtonId();
        String gender = "";
        if (selectedId != -1) {
            RadioButton selectedRadioButton = findViewById(selectedId);
            gender = selectedRadioButton.getText().toString();
        }

        if (name.isEmpty() || email.isEmpty() || mobile.isEmpty() || address.isEmpty() || aadhaarStr.isEmpty() || ageText.isEmpty() || City_id.isEmpty()) {
            Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
            return;
        } else if (!countryCodePicker.isValidFullNumber()) {
            Toast.makeText(this, "Mobile number is not valid", Toast.LENGTH_SHORT).show();
            return;
        }
        if (encodedimage1 == null) {
            Toast.makeText(this, "Please upload profile photo", Toast.LENGTH_SHORT).show();
            return;
        }
        if (aadhaarStr.length() != 14) { // Correct Aadhaar length check
            Toast.makeText(this, "Aadhaar number should be exactly 12 digits long", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] dateParts = ageText.split("-");
        if (dateParts.length != 3) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            return;
        }
        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]);
        int day = Integer.parseInt(dateParts[2]);
        Calendar dob = Calendar.getInstance();
        dob.set(year, month - 1, day); // Month is zero-based in Calendar

        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.MONTH) < dob.get(Calendar.MONTH) ||
                (today.get(Calendar.MONTH) == dob.get(Calendar.MONTH) &&
                        today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH))) {
            age--;
        }

        final int finalAge = age;
        final String finalGender = gender;

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Details");
        builder.setMessage("Is this correct?\n\nEmail: " + email + "\nAadhaar: " + aadhaarStr + "\nMobile: " + mobile);
        builder.setPositiveButton("OK", (dialog, which) -> {
            progressDialog.show();
            StringRequest request = new StringRequest(Request.Method.POST, Constant.RegisterUrl,
                    response -> {
                        progressDialog.dismiss();
                        if (response.contains("Aadhaar number already exists")) {
                            Toast.makeText(Register.this, "This Aadhaar number is already registered. Please log in with the registered mobile number.", Toast.LENGTH_SHORT).show();
                        } else if (response.contains("Mobile number already exists")) {
                            Toast.makeText(Register.this, "This mobile number is already registered. Please log in using this mobile number.", Toast.LENGTH_SHORT).show();
                        } else if (response.contains("The code is not valid")) {
                            Toast.makeText(Register.this, "The referral code is not valid. Use the correct code.", Toast.LENGTH_SHORT).show();
                        } else if (response.contains("Successfully added user")) {
                            Toast.makeText(Register.this, "Registration Success", Toast.LENGTH_SHORT).show();
                            sendEmail(mobile);
                            Intent intent = new Intent(Register.this, LoginActivity.class);
                            startActivity(intent);
                        }else{
                            Toast.makeText(Register.this, "Registration Failed, Try again latter", Toast.LENGTH_SHORT).show();
                        }
                    }, error -> {
                progressDialog.dismiss();
                if (error instanceof TimeoutError) {
                    Toast.makeText(getApplicationContext(), "Network timeout. Please try again.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("name", name);
                    params.put("email", email);
                    params.put("aadhar_card_no", aadhaarStr);
                    params.put("phone", mobile);
                    params.put("date_of_birth", selectedDateOfBirth);
                    params.put("gender", finalGender);
                    params.put("address", address);
                    params.put("city_id", City_id);
                    params.put("bounsReferral", referralCode);
                    params.put("profile_photo", encodedimage1);
                    return params;
                }
            };
            // Increase the timeout duration
            request.setRetryPolicy(new DefaultRetryPolicy(
                    30000, // Timeout in milliseconds
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            queue.add(request);
        });
        builder.setNegativeButton("Edit", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }


}

