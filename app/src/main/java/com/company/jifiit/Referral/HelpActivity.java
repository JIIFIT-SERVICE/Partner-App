package com.company.jifiit.Referral;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.company.jifiit.R;

public class HelpActivity extends AppCompatActivity {

    private View btnBack;
    private View cardCall;
    private View cardWhatsapp;
    private View cardEmail;
    private View cardFaq;

    private static final String SUPPORT_PHONE = "+919641225522";
    private static final String SUPPORT_EMAIL = "info@jiifit.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        // Light status bar with dark icons
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(true);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        cardCall = findViewById(R.id.cardCall);
        cardWhatsapp = findViewById(R.id.cardWhatsapp);
        cardEmail = findViewById(R.id.cardEmail);
        cardFaq = findViewById(R.id.cardFaq);
    }

    private void setupClickListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finishWithTransition());
        }

        if (cardCall != null) {
            cardCall.setOnClickListener(v -> {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + SUPPORT_PHONE));
                startActivity(dialIntent);
            });
        }

        if (cardWhatsapp != null) {
            cardWhatsapp.setOnClickListener(v -> {
                try {
                    String wpUrl = "https://wa.me/919641225522?text=Hello%20Jiifit%20Support%2C%20I%20need%20assistance";
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(wpUrl));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "WhatsApp not installed or cannot open link", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (cardEmail != null) {
            cardEmail.setOnClickListener(v -> {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:" + SUPPORT_EMAIL));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Jiifit Support Request");
                try {
                    startActivity(Intent.createChooser(emailIntent, "Send Email via..."));
                } catch (Exception e) {
                    Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (cardFaq != null) {
            cardFaq.setOnClickListener(v -> showFaqDialog());
        }
    }

    private void showFaqDialog() {
        String[] faqs = {
            "Q: How do bookings and referrals work?\nA: Refer clients using your unique referral code or QR. Once they complete a verified booking, your commission is credited instantly.\n",
            "Q: When can I withdraw my wallet earnings?\nA: Verified earnings are auto-credited weekly directly to your registered UPI or bank account.\n",
            "Q: Can I reschedule or cancel a client booking?\nA: Yes, bookings can be modified up to 2 hours prior to the appointment with zero cancellation penalty.\n",
            "Q: What if a client does not show up?\nA: If a client no-shows or cancels at doorstep, standard protection terms apply to protect partner earnings."
        };

        StringBuilder sb = new StringBuilder();
        for (String faq : faqs) {
            sb.append(faq).append("\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("Frequently Asked Questions")
                .setMessage(sb.toString().trim())
                .setPositiveButton("Got It", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void finishWithTransition() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
