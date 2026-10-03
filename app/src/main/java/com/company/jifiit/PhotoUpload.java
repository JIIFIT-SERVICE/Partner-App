package com.company.jifiit;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;


import android.Manifest;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
public class PhotoUpload extends AppCompatActivity {

    private Button takePhotoButton, uploadButton;
    private ImageView imageView;
    private Bitmap capturedBitmap;
    private String Id, name, startTime, endTime;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;
    private static final String FILE_NAME = "photo.jpg";

    View overlayView;
    TextView photoPlaceHolder;


    // Update this with your server URL

    BigInteger therapistId;

    @SuppressLint("MissingInflatedId")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_upload);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set title and back button colors
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Photo Upload");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Set toolbar text and icon colors to black
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setTint(getResources().getColor(android.R.color.white));
        }

        // Get data from intent
        Id = getIntent().getStringExtra("id");
        startTime = getIntent().getStringExtra("start_time");
        endTime = getIntent().getStringExtra("end_time");
        name = getIntent().getStringExtra("customer_name");
        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String idString = sp.getString("Id", "");
        therapistId = new BigInteger(idString);

        overlayView = findViewById(R.id.overlayView);
        photoPlaceHolder = findViewById(R.id.placeholderText);

        overlayView.setVisibility(View.VISIBLE);
        photoPlaceHolder.setVisibility(View.VISIBLE);
        // Initialize components
        initComponents();
    }

    private void initComponents() {
        takePhotoButton = findViewById(R.id.takePhotoButton);
        uploadButton = findViewById(R.id.uploadButton);
        imageView = findViewById(R.id.imageView);

        takePhotoButton.setOnClickListener(v -> checkCameraPermission());
        uploadButton.setOnClickListener(v -> {
            if (capturedBitmap != null) {
                uploadPhotoToServer();
            } else {
                Toast.makeText(this, "Please take a photo first", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void checkCameraPermission() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) {

            // Camera permission already granted
            openCamera();

        } else {

            // Permission not granted
            // Android default permission dialog will appear
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.CAMERA
                    },
                    CAMERA_PERMISSION_REQUEST_CODE
            );
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            capturedBitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(capturedBitmap);
            overlayView.setVisibility(View.GONE);
            photoPlaceHolder.setVisibility(View.GONE);
            Toast.makeText(this, "Photo captured! Click Upload to submit.", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadPhotoToServer() {
        if (Id == null || Id.isEmpty()) {
            Toast.makeText(this, "Order ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert bitmap to Base64 string
        String imageString = bitmapToBase64(capturedBitmap);

        // Create request queue
        RequestQueue queue = Volley.newRequestQueue(this);

        // Create string request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.UPLOAD_PHOTO_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        Toast.makeText(PhotoUpload.this, response, Toast.LENGTH_SHORT).show();

                        // If upload successful, you can navigate back or clear image
                        if (response.contains("success") || response.contains("Success")) {
                            Intent intent = new Intent(PhotoUpload.this, MassageWorkingActivity.class);
                            intent.putExtra("id",Id);
                            intent.putExtra("start_time",startTime);
                            intent.putExtra("end_time",endTime);
                            intent.putExtra("customer_name",name);
//                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            otpstatusUpdate(Id,therapistId);
                            startActivity(intent);
                            finish();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(PhotoUpload.this, "Upload failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("order_id", Id);
                params.put("photo", imageString);
                return params;
            }
        };

        // Add request to queue
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
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
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

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {

            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Log.d("PERMISSION", "Camera permission granted");

                // Permission granted → open camera
                openCamera();

            } else {

                Log.d("PERMISSION", "Camera permission denied");

                // Check if Android will show the permission dialog again
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.CAMERA
                )) {

                    Toast.makeText(
                            this,
                            "Please allow Camera permission from Settings",
                            Toast.LENGTH_LONG
                    ).show();

                } else {

                    // Normal denial.
                    // Don't show Toast.
                    Log.d(
                            "PERMISSION",
                            "Camera denied, permission can be requested again"
                    );
                }
            }
        }
    }

    // Alternative: Save to file first if needed
    private void savePhotoToFile() {
        try {
            File file = new File(getCacheDir(), FILE_NAME);
            FileOutputStream stream = new FileOutputStream(file);
            capturedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.flush();
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}