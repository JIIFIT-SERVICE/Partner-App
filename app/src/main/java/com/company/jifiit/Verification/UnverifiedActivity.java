package com.company.jifiit.Verification;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.company.jifiit.Constant;
import com.company.jifiit.MainActivity;
import com.company.jifiit.R;
import android.content.SharedPreferences;
import android.widget.TextView;

import java.math.BigInteger;


public class UnverifiedActivity extends AppCompatActivity {

    Button WhatsApp,VisitOffice;
    TextView mobile;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_unverified);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        setTitle("Verification Desk");

        // Enable the back button in the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String name = sp.getString("Name", "Default Name");
        String idString = sp.getString("Id", "");
        BigInteger Id = new BigInteger(idString);
//        String Mobile = sp.getString("mobile","");


        WhatsApp=findViewById(R.id.WhatsAppBtn);
        VisitOffice=findViewById(R.id.VisitOfficeBtn);

        WhatsApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String wpUrl = "https://wa.me/+919717550414?text=*Hello* JIIFIT team , \nMy name is *" + name + "* ,\n"
                        + "employee code is *" + Id + "*\n\n"
                        + "Please verify my account as soon as possible so that I can get an appointment. "
                        + "I want to be part of the JIIFIT team as a therapist. \n\nThank You \uD83D\uDE4F";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(wpUrl));
                startActivity(intent);
            }
        });
        VisitOffice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();  // Go back to the previous activity when the back button is pressed
        return true;
    }
}
