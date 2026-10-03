package com.company.jifiit;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashActivityNew extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_new);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);


        SystemClock.sleep(3000);
        Intent loginIntent = new Intent(SplashActivityNew.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }
}