package com.company.jifiit.Leave;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Constant;
import com.company.jifiit.R;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class IllnessActivity extends AppCompatActivity {
    Button Illness,Prescription;
    EditText dateNumber,FromDate,Todate;
    ImageView fromDate,toDate,back;
    private boolean isProcessing = false;
    private boolean isClickable = false;
    private boolean isFromDateSelected = false;

    Bitmap bitmap1;
    String encodedimage1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_illness);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Illness");


        // Enable the back button in the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String name = sp.getString("Name", "Default Name");
        String idString = sp.getString("Id", "");
        BigInteger Id = new BigInteger(idString);

        fromDate=findViewById(R.id.start_date_calendar_icon);
        toDate=findViewById(R.id.end_date_calendar_icon);
        FromDate=findViewById(R.id.start_date_edit_text);
        Todate=findViewById(R.id.end_date_edit_text);
        Illness=findViewById(R.id.button);
        Prescription = findViewById(R.id.upload_document_button);

        Prescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withContext(getApplicationContext())
                        .withPermission(Manifest.permission.CAMERA)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                // Permission granted, you can proceed with capturing images
                                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(intent,1);
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                                // Permission denied, handle this situation
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                                // Show a rationale to the user and continue requesting the permission
                                permissionToken.continuePermissionRequest();
                            }
                        }).check();
            }
        });
        Illness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IllnessApply();
            }
        });

        toDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFromDateSelected = false;
                datePicker();
            }
        });
        fromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFromDateSelected = true;
                datePicker();
            }
        });
    }
    private void datePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a date picker dialog with the current date as default
        DatePickerDialog datePickerDialog = new DatePickerDialog(IllnessActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar selectedCalendar = Calendar.getInstance();
                        selectedCalendar.set(year, month, dayOfMonth);

                        Calendar minDate = Calendar.getInstance();
                        minDate.add(Calendar.DATE, -1); // Set minimum date to today

                        if (selectedCalendar.before(minDate)) {
                            // Selected date is before today, show a message or handle it accordingly
                            Toast.makeText(IllnessActivity.this, "Please select today or a future date", Toast.LENGTH_SHORT).show();
                        } else {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            String selectedDate = sdf.format(selectedCalendar.getTime());
                            // Call API based on selected date
                            if (isFromDateSelected) {
                                FromDate.setText(selectedDate);
                            } else {
                                Todate.setText(selectedDate);
                            }


                        }
                    }
                }, year, month, dayOfMonth);

        // Set the minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        // Show the date picker dialog
        datePickerDialog.show();
    }
    private void IllnessApply() {
        final String StartDate = FromDate.getText().toString().trim();
        final String EndDate   = Todate.getText().toString().trim();
        if (StartDate.isEmpty() || EndDate.isEmpty()){
            Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (encodedimage1 == null) {
            Toast.makeText(this, "Please upload all required photos", Toast.LENGTH_SHORT).show();
            return;
        }


        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String idString = sp.getString("Id", "");
        BigInteger Id = new BigInteger(idString);
        final String id = Id.toString();

        String url = Constant.inlless + id;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(IllnessActivity.this, response, Toast.LENGTH_SHORT).show();
                        FromDate.setText("");
                        Todate.setText("");
                        encodedimage1 = null;
                        showMessageDialog(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(IllnessActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                params.put("date", StartDate);
                params.put("todate", EndDate);
                params.put("prescription",encodedimage1);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            bitmap1 = (Bitmap) data.getExtras().get("data");
            if (bitmap1 != null) {
                encodebitmap(bitmap1, 1);

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void encodebitmap(Bitmap bitmap1, int i) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap1.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        byte[] byteofimages = byteArrayOutputStream.toByteArray();
        if (i == 1) {
            encodedimage1 = android.util.Base64.encodeToString(byteofimages, Base64.DEFAULT);
        }
    }
    private void showMessageDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Close the dialog
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}