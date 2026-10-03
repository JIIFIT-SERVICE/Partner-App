package com.company.jifiit.ViewMoreMenu;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Cities;
import com.company.jifiit.Constant;
import com.company.jifiit.R;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class FareActivity extends AppCompatActivity {
    Button UpSubmit, DownSubmit;
    ImageButton  UpImage, DownImage;
    EditText UpFare, DownFare;
    TextView UpLocation, DownLocation, TotalFare;
    Bitmap bitmap1, bitmap2;
    String encodedimage1, encodedimage2;
    String id, address;
    LinearLayout Uplayout,DownLayout;

    TextView uppath,downpath;

    private ActivityResultLauncher<Intent> openGallery1;
    private ActivityResultLauncher<Intent> openGallery2;


    private SharedPreferences sharedPreferences;
    private static final String PERMANENT_PREFS = "PermanentPrefs";


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_fare);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Fare");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);



        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String idString = sp.getString("Id", "");
        BigInteger Id = new BigInteger(idString);
        final String therapistsId = Id.toString();


        id = getIntent().getStringExtra("id");
        address = getIntent().getStringExtra("location");
        totalfare(id);
        layoutDisable(id);

        uppath=findViewById(R.id.uppath);
        downpath=findViewById(R.id.downpath);

        UpImage = findViewById(R.id.upImage);
        DownImage = findViewById(R.id.downImage);

        UpSubmit = findViewById(R.id.UpSubmit);
        DownSubmit = findViewById(R.id.downSubmit);

        UpFare = findViewById(R.id.upFare);
        DownFare = findViewById(R.id.downFare);

        Uplayout=findViewById(R.id.uplayout);
        DownLayout=findViewById(R.id.downLayer);
        DownLayout.setEnabled(false);
        setChildrenEnabled(DownLayout, false);

        UpLocation = findViewById(R.id.upLocation);
        UpLocation.setText(address);

        TotalFare = findViewById(R.id.totaFare);
        DownLocation = findViewById(R.id.downLocation);

       // String[] locations = {"Howrah", "Kolkata", "Delhi", "Mumbai", "Chennai"};
        AutoCompleteTextView downLocation = (AutoCompleteTextView) DownLocation;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, Cities.CITIES);
        downLocation.setAdapter(adapter);

        downLocation.setDropDownHeight(600);
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
        
        UpImage.setOnClickListener(v -> OpenGallery(1));
        DownImage.setOnClickListener(v -> OpenGallery(2));

        openGallery1 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri uriImage1 = result.getData().getData();
                handleImageUri(uriImage1, 1);
            }
        });

        openGallery2 = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri uriImage2 = result.getData().getData();
                handleImageUri(uriImage2, 2);
            }
        });

        UpSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpSubmitData(therapistsId, id);

            }
        });
        DownSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownSubmitData(therapistsId, id);
            }
        });

    }

    private void handleImageUri(Uri uriImage, int requestCode) {
        try {
            if (uriImage != null) {
                // Persist URI permission
                getContentResolver().takePersistableUriPermission(uriImage, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                InputStream inputStream = getContentResolver().openInputStream(uriImage);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                if (requestCode == 1) {
                    uppath.setText(getFileName(uriImage));
                    encodeBitmapImage(bitmap, 1);
                } else if (requestCode == 2) {
                    downpath.setText(getFileName(uriImage));
                    encodeBitmapImage(bitmap, 2);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void OpenGallery(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        if (requestCode == 1) {
            openGallery1.launch(intent);
        } else if (requestCode == 2) {
            openGallery2.launch(intent);
        }
    }

    private void setChildrenEnabled(ViewGroup layout, boolean enabled) {
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setEnabled(enabled);
        }
    }

//    SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
private void UpSubmitData(String therapistId, String orderId) {
    String UpFareValue = UpFare.getText().toString().trim();
    String UpPath = uppath.getText().toString().trim();
    String UpAddress = address;

    if (UpFareValue.isEmpty()) {
        Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
        return;
    }
    if (encodedimage1 == null) {
        Toast.makeText(this, "Please upload all required photos", Toast.LENGTH_SHORT).show();
        return;
    }

    String url = Constant.UpFare;
    ProgressDialog progressDialog = new ProgressDialog(this);
    progressDialog.setMessage("Uploading,Please wait...");
    progressDialog.setCancelable(false);
    progressDialog.show(); // Show progress dialog

    StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
            response -> {
                progressDialog.dismiss(); // Hide progress dialog
                Toast.makeText(FareActivity.this, response, Toast.LENGTH_SHORT).show();
                showMessageDialog(response);

                // Save values in SharedPreferences with unique keys
                saveToPermanentSharedPreferences("UpFareValue", UpFareValue, orderId);
                saveToPermanentSharedPreferences("UpPath", UpPath, orderId);

                Uplayout.setEnabled(false);
                setChildrenEnabled(Uplayout, false);

                // Enable DownLayout and its children
                DownLayout.setEnabled(true);
                setChildrenEnabled(DownLayout, true);

                totalfare(id);
            },
            error -> {
                progressDialog.dismiss(); // Hide progress dialog
                Toast.makeText(FareActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
    ) {
        @Override
        protected Map<String, String> getParams() {
            Map<String, String> params = new HashMap<>();
            params.put("therapist_id", therapistId);
            params.put("order_id", orderId);
            params.put("fareValue", UpFareValue);
            params.put("location", UpAddress);
            params.put("fareImage", encodedimage1);
            return params;
        }
    };

    // Set a retry policy with a timeout of 10 seconds and a max of 3 retries
    stringRequest.setRetryPolicy(new DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

    RequestQueue queue = Volley.newRequestQueue(this);
    queue.add(stringRequest);
}

    private void DownSubmitData(String therapistsId, String orderId) {
        String DownFareValue = DownFare.getText().toString().trim();
        String Address = DownLocation.getText().toString().trim();
        String Path = downpath.getText().toString().trim();

        if (DownFareValue.isEmpty()) {
            Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (encodedimage2 == null) {
            Toast.makeText(this, "Please upload all required photos", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = Constant.DownFare;
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading,Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show(); // Show progress dialog

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss(); // Hide progress dialog
                    Toast.makeText(FareActivity.this, response, Toast.LENGTH_SHORT).show();
                    showMessageDialog(response);

                    // Save values in SharedPreferences with unique keys
                    saveToPermanentSharedPreferences("DownFareValue", DownFareValue, orderId);
                    saveToPermanentSharedPreferences("DownPath", Path, orderId);
                    saveToPermanentSharedPreferences("Address", Address, orderId);

                    encodedimage2 = null;

                    Uplayout.setEnabled(false);
                    setChildrenEnabled(Uplayout, false);

                    // Disable DownLayout and its children
                    DownLayout.setEnabled(false);
                    setChildrenEnabled(DownLayout, false);

                    totalfare(id);
                },
                error -> {
                    progressDialog.dismiss(); // Hide progress dialog
                    Toast.makeText(FareActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("therapist_id", therapistsId);
                params.put("order_id", orderId);
                params.put("fareValue", DownFareValue);
                params.put("location", Address);
                params.put("fareImage", encodedimage2);
                return params;
            }
        };

        // Set a retry policy with a timeout of 10 seconds and a max of 3 retries
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }


    private String totalfare(String id) {
        String url = Constant.totalFare + id;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    try {
                        String status = response.getString("Status");
                        if (status.equals("success")) {
                            String referralcode = response.getString("totalFare");
                            TotalFare.setText(referralcode);
                        } else {
                            // Handle error response
                            String message = response.getString("message");
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
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
        return url;
    }


    private void showMessageDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    private void encodeBitmapImage(Bitmap bitmap, int imageNumber) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Bitmap resizedBitmap = getResizedBitmap(bitmap); // Resize to a max size of 500px
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream); // Compress further
        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        if (imageNumber == 1) {
            encodedimage1 = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        } else {
            encodedimage2 = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        }
    }

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

    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }


    private String layoutDisable(String id) {
        String url = Constant.LayoutDisable + id;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    try {
                        String status = response.getString("Status");
                        if (status.equals("both")) {
                            Uplayout.setEnabled(false);
                            setChildrenEnabled(Uplayout, false);
                            DownLayout.setEnabled(false);
                            setChildrenEnabled(DownLayout, false);

                            // Retrieve values from SharedPreferences with unique keys
                            String upFareValue = getFromPermanentSharedPreferences("UpFareValue", id);
                            String upPath = getFromPermanentSharedPreferences("UpPath", id);
                            String downFareValue = getFromPermanentSharedPreferences("DownFareValue", id);
                            String downPath = getFromPermanentSharedPreferences("DownPath", id);
                            String address = getFromPermanentSharedPreferences("Address", id);

                            // Set values to views
                            UpFare.setText(upFareValue);
                            uppath.setText(upPath);
                            DownFare.setText(downFareValue);
                            downpath.setText(downPath);
                            DownLocation.setText(address);
                            String message = response.getString("message");
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                        } else if (status.equals("upfare")) {
                            Uplayout.setEnabled(false);
                            setChildrenEnabled(Uplayout, false);
                            DownLayout.setEnabled(true);
                            setChildrenEnabled(DownLayout, true);

                            String upFareValue = getFromPermanentSharedPreferences("UpFareValue", id);
                            String upPath = getFromPermanentSharedPreferences("UpPath", id);
                            // Set values to views
                            UpFare.setText(upFareValue);
                            uppath.setText(upPath);

                        } else {
                            // Handle error response
                            String message = response.getString("message");
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
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
        return url;
    }

    private void saveToPermanentSharedPreferences(String key, String value, String bookingId) {
        SharedPreferences.Editor editor = getPermanentSharedPreferences().edit();
        editor.putString(key + "_" + bookingId, value);
        editor.apply();
    }

    private SharedPreferences getPermanentSharedPreferences() {
        return getSharedPreferences(PERMANENT_PREFS, MODE_PRIVATE);
    }
    private String getFromPermanentSharedPreferences(String key, String bookingId) {
        return getPermanentSharedPreferences().getString(key + "_" + bookingId, "");
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
