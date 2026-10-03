package com.company.jifiit.ViewMoreMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.company.jifiit.R;
import com.company.jifiit.Referral.HelpActivity;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class HelpsActivity extends AppCompatActivity {
    Button Email,Call,whatsApp;
    private Map<String, String> numberMapping = new HashMap<>();


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_help);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Helps");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String name = sp.getString("Name", "Default Name");
        String idString = sp.getString("Id", "");
        BigInteger Id = new BigInteger(idString);

        String time = getIntent().getStringExtra("time");
        String BookingId = getIntent().getStringExtra("id");


        Email=findViewById(R.id.email);
        Call=findViewById(R.id.call);
        whatsApp=findViewById(R.id.whatsapp);


        Email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String recipientEmail = "jiifitteam@gmail.com";
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + recipientEmail));

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    // Handle case where no email app is available
                    // You can show a message to the user
                    Toast.makeText(HelpsActivity.this, "No email app found", Toast.LENGTH_SHORT).show();
                }
            }
        });


        Call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String customerPhoneNumber = "+919717550414";

                // Lookup the actual phone number in the mapping

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + customerPhoneNumber));

                // Check for permission before making the call
                if (ContextCompat.checkSelfPermission(HelpsActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // Request the permission if it is not granted
                    ActivityCompat.requestPermissions(HelpsActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 1);
                    return;
                }

                // Start the call if permission is granted
                startActivity(callIntent);
            }
        });


        whatsApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String wpUrl = "https://wa.me/+919717550414?text=*Hello* JIIFIT team , \nMy name is *" + name + "* ,\n"
                        + "employee code is *" + Id + "*\n\n"
                        + "I have an appointment at " + time + "\n. "
                        + "And the booking id is " + BookingId
                        + "I am facing some problems so please I need support";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(wpUrl));
                startActivity(intent);

            }
        });

    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}