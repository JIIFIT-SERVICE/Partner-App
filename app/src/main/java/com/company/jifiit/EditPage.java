package com.company.jifiit;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hbb20.CountryCodePicker;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditPage extends AppCompatActivity {

    TextView id, Name;
    CountryCodePicker countryCodePicker;
    private EditText phoneInput, Address, Email, City;
    CircleImageView circleImageView;
    ImageView Camera;
    Button Submit;
    Bitmap bitmap1;
    String encodedimage1;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_edit_page);
        // Initialize the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle("Edit Profile");

        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String name = sp.getString("Name", "Default Name");
        String idString = sp.getString("Id", "");
        BigInteger Id = new BigInteger(idString);

        id = findViewById(R.id.editTextId);
        Name = findViewById(R.id.editTextName);
        phoneInput = findViewById(R.id.UpdateNumber);
        Address = findViewById(R.id.UpdateAddress);
        Email = findViewById(R.id.UpdateEmails);
        circleImageView = findViewById(R.id.employeeProfile);
        Camera = findViewById(R.id.cameraBtn);
        Submit = findViewById(R.id.update);
        countryCodePicker = findViewById(R.id.login_countrycode);
        countryCodePicker.registerCarrierNumberEditText(phoneInput);
        City = findViewById(R.id.City_address);

        //String[] locations = {"Ahmedabad", "Bengaluru", "Chennai", "Delhi", "Gurgaon", "Hyderabad", "Kolkata", "New Delhi", "Noida", "Pune"};
        AutoCompleteTextView downLocation = (AutoCompleteTextView) City;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, Cities.CITIES);
        downLocation.setAdapter(adapter);
        downLocation.setBackgroundColor(Color.WHITE);
        downLocation.setInputType(InputType.TYPE_NULL);

        downLocation.setOnTouchListener((v, event) -> {
            downLocation.showDropDown();
            return false;
        });

        Glide.with(this)
                .load(Constant.ProfileImage + Id)
                .placeholder(R.drawable.account_circle_24)
                .error(R.drawable.outline_add_circle_outline_24)
                .skipMemoryCache(true) // 🚫 Skip memory cache
                .diskCacheStrategy(DiskCacheStrategy.NONE) // 🚫 Disable disk cache
                .into(circleImageView);

        Camera.setOnClickListener(v -> OpenGallery());

        Submit.setOnClickListener(v -> {
            profile(Id);
        });

        id.setText("ID: " + Id);
        Name.setText(name);
    }

    private void OpenGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        openGallery.launch(intent);
    }

    ActivityResultLauncher<Intent> openGallery = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        if (result.getData() != null) {
                            Uri uriImage = result.getData().getData();
                            circleImageView.setImageURI(uriImage);


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
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream); // Compress further
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        encodedimage1 = Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void profile(BigInteger id) {
        final String mobile = phoneInput != null && !phoneInput.getText().toString().trim().isEmpty() ? countryCodePicker.getFullNumberWithPlus() : "";
        if (!mobile.isEmpty() && (countryCodePicker == null || !countryCodePicker.isValidFullNumber())) {
            if (phoneInput != null) {
                phoneInput.setError("Phone number not valid");
            }
            return;
        }
        final String email = Email.getText().toString().trim();
        final String address = Address.getText().toString().trim();
        final String city = City.getText().toString().trim();

        // Show a ProgressDialog to indicate buffering
        ProgressDialog progressDialog = new ProgressDialog(EditPage.this);
        progressDialog.setMessage("Updating Profile...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, Constant.EditPage + id,
                response -> {
                    progressDialog.dismiss(); // Dismiss the ProgressDialog when done
                    if (response.contains("User already exists")) {
                        Toast.makeText(EditPage.this, "This user already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        phoneInput.setText("");
                        Address.setText("");
                        Email.setText("");
                        City.setText("");
                        Toast.makeText(EditPage.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss(); // Dismiss the ProgressDialog on error
                    if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                        Toast.makeText(EditPage.this, "Network Timeout. Please try again.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EditPage.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", id.toString());
                if (!mobile.isEmpty()) {
                    params.put("phone", mobile);
                }
                params.put("email", email);
                params.put("city_id", city);
                params.put("address", address);
                if (encodedimage1 != null && !encodedimage1.isEmpty()) {
                    params.put("profile_photo", encodedimage1);
                }
                return params;
            }
        };

        // Set retry policy to handle timeout
        request.setRetryPolicy(new DefaultRetryPolicy(
                30000, // Timeout in milliseconds (10 seconds)
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, // Number of retries
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)); // Backoff multiplier

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
