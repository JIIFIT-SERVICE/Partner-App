package com.company.jifiit.ViewMoreMenu;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QrWebViewActivity extends AppCompatActivity {

    private ImageView qrImageView;
    private String orderId;
    private double amount;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_web_view);

        qrImageView = findViewById(R.id.qrImageView);

        orderId = getIntent().getStringExtra("orderId");
        amount = getIntent().getDoubleExtra("amount", 0);

        fetchQrCode();
    }

    private void fetchQrCode() {
        String url = "https://jiifit.com/Jifiit/payment-gatway/dynamicQRCode/submitpayment.php"
                + "?payAmount=" + amount
                + "&order_id=" + orderId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    String res = response.optString("res");
                    if ("success".equalsIgnoreCase(res)) {
                        String qrUrl = response.optString("qr_url");

                        if (!qrUrl.isEmpty()) {
                            generateQrCode(qrUrl);
                        } else {
                            Toast.makeText(this, "QR URL not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "API Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        );

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    private void generateQrCode(String qrUrl) {
        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            BitMatrix matrix = writer.encode(qrUrl, BarcodeFormat.QR_CODE, 500, 500);
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(matrix);
            qrImageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}


