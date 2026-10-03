package com.company.jifiit.Wallet;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import com.company.jifiit.Constant;
import com.company.jifiit.R;

import java.math.BigInteger;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class WalletRequestActivity extends AppCompatActivity {
EditText coinAmount;
Button submitBtn;

    @SuppressLint({"MissingInflatedId", "LocalSuppress"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_wallet_request);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Add Request");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String idString = sp.getString("Id", "");
        BigInteger id = new BigInteger(idString);



        coinAmount = findViewById(R.id.coinAmount);
        submitBtn = findViewById(R.id.submitBtn);


        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String coin = coinAmount.getText().toString();
                RequestCoins(coin, id);
            }
        });

    }


    private void RequestCoins(String coin, BigInteger id) {

        // Your backend API URL
        String url = Constant.CoinsRequest;  // Replace with your PHP endpoint

        // Initialize Volley RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);

        // Create a POST request
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("VolleyResponse", "Response: " + response);  // Log the full response
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");

                            if (success) {
                                // Handle both 'message' and 'messages[]' formats
                                if (jsonObject.has("message")) {
                                    String message = jsonObject.getString("message");
                                    Log.d("VolleySuccess", "Message: " + message);
                                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                                } else if (jsonObject.has("messages")) {
                                    JSONArray messagesArray = jsonObject.getJSONArray("messages");
                                    if (messagesArray.length() > 0) {
                                        String message = messagesArray.getString(0);  // Get the first message
                                        Log.d("VolleySuccess", "Message: " + message);
                                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "No message received", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                String error = jsonObject.getString("error");
                                Log.d("VolleyError", "Error: " + error);
                                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.d("VolleyJSONException", "JSON Error: " + e.getMessage(), e);
                            Toast.makeText(getApplicationContext(), "JSON Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("VolleyError", "Volley Error: " + error.getMessage(), error);
                        Toast.makeText(getApplicationContext(), "Volley Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {

            // Add parameters to the request
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("coin", coin);                   // Coin amount
                params.put("id", String.valueOf(id));       // BigInteger ID
                Log.d("VolleyParams", "Params: " + params.toString());
                return params;
            }
        };

        // Add the request to the queue
        queue.add(request);
    }



    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}