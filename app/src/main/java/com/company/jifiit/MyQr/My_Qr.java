package com.company.jifiit.MyQr;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Constant;
import com.company.jifiit.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class My_Qr extends AppCompatActivity {

    Button add_qr, btn_update, btn_delete;
    LinearLayout action_buttons, empty_state_container;
    CardView card_qr, card_details;
    ImageView iv_qr_code;
    TextView tv_name, tv_upi_id;
    ProgressBar progress_qr;
    RequestQueue requestQueue;
    ImageButton btnBack;

    private BigInteger therapistId;
    private String currentName = "";
    private String currentUpiId = "";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_qr);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String idString = sp.getString("Id", "");
        therapistId = new BigInteger(idString);

        // Initialize all views
        add_qr = findViewById(R.id.btn_upload);
        btn_delete = findViewById(R.id.btn_delete);
        btn_update = findViewById(R.id.btn_update);
        action_buttons = findViewById(R.id.action_buttons);
        empty_state_container = findViewById(R.id.empty_state_container);
        card_qr = findViewById(R.id.card_qr);
        card_details = findViewById(R.id.card_details);
        iv_qr_code = findViewById(R.id.iv_qr_code);
        tv_name = findViewById(R.id.tv_name);
        tv_upi_id = findViewById(R.id.tv_upi_id);
        progress_qr = findViewById(R.id.progress_qr);
        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> onBackPressed());

        // Initialize RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        // Set button click listeners
        add_qr.setOnClickListener(v -> {
            showUpiFormDialog(false);
        });

        btn_delete.setOnClickListener(v -> {
            deleteQrCode();
        });

        btn_update.setOnClickListener(v -> {
            showUpiFormDialog(true);
        });

        // Fetch UPI details from API
        fetchUpiDetails(therapistId);
    }

    private void showUpiFormDialog(boolean isUpdate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_upi_form, null);

        TextInputEditText etName = dialogView.findViewById(R.id.et_name);
        TextInputEditText etUpiId = dialogView.findViewById(R.id.et_upi_id);
        TextInputLayout inputLayoutName = dialogView.findViewById(R.id.input_layout_name);
        TextInputLayout inputLayoutUpi = dialogView.findViewById(R.id.input_layout_upi);
        Button btnSubmit = dialogView.findViewById(R.id.btn_submit);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        // Update button text based on action
        btnSubmit.setText(isUpdate ? "Update" : "Generate");

        // If updating, pre-fill current values
        if (isUpdate && !TextUtils.isEmpty(currentName)) {
            etName.setText(currentName);
        }
        if (isUpdate && !TextUtils.isEmpty(currentUpiId)) {
            etUpiId.setText(currentUpiId);
        }

        AlertDialog dialog = builder.setView(dialogView).create();
        dialog.show();

        // Handle submit button click
        btnSubmit.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String upiId = etUpiId.getText().toString().trim();

            // Validate inputs
            if (validateInputs(name, upiId, inputLayoutName, inputLayoutUpi)) {
                dialog.dismiss();
                saveUpiDetails(name, upiId);
            }
        });

        // Handle cancel button click
        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private boolean validateInputs(String name, String upiId,
                                   TextInputLayout inputLayoutName,
                                   TextInputLayout inputLayoutUpi) {
        boolean isValid = true;

        if (TextUtils.isEmpty(name)) {
            inputLayoutName.setError("Name is required");
            isValid = false;
        } else {
            inputLayoutName.setError(null);
        }

        if (TextUtils.isEmpty(upiId)) {
            inputLayoutUpi.setError("UPI ID is required");
            isValid = false;
        } else if (!isValidUpiId(upiId)) {
            inputLayoutUpi.setError("Invalid UPI ID format");
            isValid = false;
        } else {
            inputLayoutUpi.setError(null);
        }

        return isValid;
    }

    private boolean isValidUpiId(String upiId) {
        // Basic UPI ID validation - should contain @ and be reasonable length
        return upiId.contains("@") && upiId.length() >= 5 && upiId.length() <= 50;
    }

    private void saveUpiDetails(String name, String upiId) {
        // Show loading
        progress_qr.setVisibility(View.VISIBLE);

        // Upload only name and UPI ID to server
        uploadUpiDetails(name, upiId);
    }

    private void uploadUpiDetails(String name, String upiId) {
        String url = Constant.uploadQr;

        Map<String, String> params = new HashMap<>();
        params.put("therapist_id", therapistId.toString());
        params.put("name", name);
        params.put("upi_id", upiId);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    progress_qr.setVisibility(View.GONE);
                    try {
                        Log.d("UPI_UPLOAD", "Response: " + response);
                        JSONObject jsonResponse = new JSONObject(response);
                        boolean success = jsonResponse.getBoolean("success");
                        String message = jsonResponse.getString("message");

                        if (success) {
                            Toast.makeText(My_Qr.this, message, Toast.LENGTH_SHORT).show();
                            // Update local variables
                            currentName = name;
                            currentUpiId = upiId;
                            // Generate and display QR code locally
                            generateAndDisplayQR(name, upiId);
                        } else {
                            Toast.makeText(My_Qr.this, "Failed: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(My_Qr.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progress_qr.setVisibility(View.GONE);
                    Toast.makeText(My_Qr.this, "Upload failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("UPI_UPLOAD", "Error: " + error.toString());
                }) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }

            @Override
            public RetryPolicy getRetryPolicy() {
                return new DefaultRetryPolicy(
                        30000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                );
            }
        };

        requestQueue.add(stringRequest);
    }

    private void generateAndDisplayQR(String name, String upiId) {
        try {
            // Generate UPI payment string
            String upiPaymentString = generateUpiPaymentString(name, upiId);

            // Generate QR code bitmap
            Bitmap qrBitmap = generateQRCode(upiPaymentString);

            if (qrBitmap != null) {
                // Display the QR code
                iv_qr_code.setImageBitmap(qrBitmap);

                // Update details
                tv_name.setText(name);
                tv_upi_id.setText(upiId);

                // Show QR state
                showQrState(true);
            } else {
                Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private String generateUpiPaymentString(String name, String upiId) {
        String encodedName = name.replace(" ", "%20");
        return "upi://pay?pa=" + upiId + "&pn=" + encodedName + "&am=&cu=INR&tn=Payment";
    }

    private Bitmap generateQRCode(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // High error correction
            hints.put(EncodeHintType.MARGIN, 1); // Small margin

            int qrSize = 512; // Size of QR code
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, qrSize, qrSize, hints);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            Log.e("QR_GENERATION", "Error generating QR code: " + e.getMessage());
            return null;
        }
    }

    private void deleteQrCode() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete UPI Details");
        builder.setMessage("Are you sure you want to delete your UPI details and QR code?");
        builder.setPositiveButton("Delete", (dialog, which) -> {
            performDeleteUpiDetails();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void performDeleteUpiDetails() {
        progress_qr.setVisibility(View.VISIBLE);

        String url = Constant.deleteQr + "?therapist_id=" + therapistId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                response -> {
                    progress_qr.setVisibility(View.GONE);
                    try {
                        boolean success = response.getBoolean("success");
                        String message = response.getString("message");

                        if (success) {
                            Toast.makeText(My_Qr.this, message, Toast.LENGTH_SHORT).show();
                            // Clear local variables
                            currentName = "";
                            currentUpiId = "";
                            // Clear UI
                            iv_qr_code.setImageResource(R.drawable.ic_qr_placeholder);
                            tv_name.setText("Not Set");
                            tv_upi_id.setText("Not Set");
                            // Show empty state
                            showQrState(false);
                        } else {
                            Toast.makeText(My_Qr.this, "Delete failed: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(My_Qr.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progress_qr.setVisibility(View.GONE);
                    Toast.makeText(My_Qr.this, "Delete failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(jsonObjectRequest);
    }

    private void fetchUpiDetails(BigInteger id) {
        progress_qr.setVisibility(View.VISIBLE);
        iv_qr_code.setVisibility(View.GONE);

        String url = Constant.fetchQr + id;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        Log.d("UPI_API", "Full Response: " + response.toString());

                        boolean success = response.getBoolean("success");
                        boolean hasUpi = response.optBoolean("has_upi", false);

                        // ADD DEBUG LOGS
                        Log.d("UPI_API", "success: " + success);
                        Log.d("UPI_API", "hasUpi: " + hasUpi);
                        Log.d("UPI_API", "has_upi value from JSON: " + response.optString("has_upi", "not found"));

                        if (success && hasUpi) {
                            // Get name and UPI ID from response
                            String name = response.optString("name", "");
                            String upiId = response.optString("upi_id", "");

                            Log.d("UPI_API", "Name: " + name);
                            Log.d("UPI_API", "UPI ID: " + upiId);

                            // Update local variables
                            currentName = name;
                            currentUpiId = upiId;

                            generateAndDisplayQR(name, upiId);

                            Log.d("UPI_API", "QR code generated and displayed");
                        } else {
                            // No UPI details
                            showQrState(false);
                            Log.d("UPI_API", "No UPI details found - success: " + success + ", hasUpi: " + hasUpi);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showQrState(false);
                        Log.e("UPI_API", "JSON Error: " + e.getMessage());
                    }

                    progress_qr.setVisibility(View.GONE);
                    iv_qr_code.setVisibility(View.VISIBLE);
                },
                error -> {
                    error.printStackTrace();
                    showQrState(false);
                    progress_qr.setVisibility(View.GONE);
                    iv_qr_code.setVisibility(View.VISIBLE);
                    Log.e("UPI_API", "Network Error: " + error.getMessage());
                });

        requestQueue.add(jsonObjectRequest);
    }

    private void showQrState(boolean hasUpi) {
        if (hasUpi) {
            // Show QR code and details
            card_qr.setVisibility(View.VISIBLE);
            card_details.setVisibility(View.VISIBLE);
            action_buttons.setVisibility(View.VISIBLE);
            empty_state_container.setVisibility(View.GONE);
        } else {
            // Show empty state
            card_qr.setVisibility(View.GONE);
            card_details.setVisibility(View.GONE);
            action_buttons.setVisibility(View.GONE);
            empty_state_container.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(this);
        }
    }
}