package com.company.jifiit.Referral;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Adapter.ReferralAdapter;
import com.company.jifiit.Constant;
import com.company.jifiit.R;
import com.company.jifiit.model.ReferralModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;

public class ReferralFriendActivity extends AppCompatActivity {

    TextView nameTv,Referralcode,BounusText,TermsAndConditions;
    RecyclerView listView;
    ArrayList<ReferralModel> referralModels;
    ReferralAdapter referralAdapter;
    Button status,offers,send;
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_referral_friend);
//        Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        //setTitle("Refer and earn");

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);


        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String name = sp.getString("Name", "Default Name");
        String idString = sp.getString("Id", "");
        BigInteger Id = new BigInteger(idString);
        referralCode(Id);
        getMoney(Id);
        Referralcode=findViewById(R.id.Code);
        BounusText=findViewById(R.id.getMoney);
        TermsAndConditions=findViewById(R.id.termsButton);
        send=findViewById(R.id.invite);
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close this activity and go back to the previous one
            }
        });


        status=findViewById(R.id.status);
        offers=findViewById(R.id.offers);
        LinearLayout items=findViewById(R.id.OffersLabel);
        LinearLayout items2=findViewById(R.id.StatusLable);
        offers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                items.setVisibility(View.VISIBLE);
                items2.setVisibility(View.GONE);
                offers.setBackgroundColor(getResources().getColor(R.color.Gray));
                status.setBackgroundColor(getResources().getColor(R.color.Blue)); // Reset other buttons
            }
        });
        offers.performClick();

//        nameTv= findViewById(R.id.name);
//        nameTv.setText(name);

      status.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              items2.setVisibility(View.VISIBLE);
              items.setVisibility(View.GONE);
              status.setBackgroundColor(getResources().getColor(R.color.Gray));
              offers.setBackgroundColor(getResources().getColor(R.color.Blue)); // Reset other buttons
          }
      });


      TermsAndConditions.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              Intent intent = new Intent(ReferralFriendActivity.this,com.company.jifiit.Referral.TermsAndConditions.class);
              startActivity(intent);
          }
      });


    }

    private void getMoney(BigInteger id) {
        String url = Constant.getMoney + id.toString();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    try {
                        String status = response.getString("Status");
                        if (status.equals("success")) {
                            JSONArray dataArray = response.getJSONArray("booking");
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject bookingObject = dataArray.getJSONObject(i);
                                String dateAfter8Months = bookingObject.getString("date_after_8_months");
                                BounusText.setText("You will get reward on : " + dateAfter8Months);
                            }
                        } else {
                            // Handle error status
//                            Toast.makeText(getApplicationContext(), "No bookings found", Toast.LENGTH_SHORT).show();
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
    }


    private String referralCode(BigInteger id) {
        String url = Constant.ReferralCode + id.toString();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    try {
                        String status = response.getString("status");
                        if (status.equals("success")) {
                           String referralcode = response.getString("referral_code");
                            Referralcode.setText("Referral Code is: " + referralcode);
                            fetchData(referralcode);
                            send.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String appLink = "https://play.google.com/store/apps/details?id=com.company.jifiit";
                                    String referralCode = referralcode;

                                    Intent sendIntent = new Intent();
                                    sendIntent.setAction(Intent.ACTION_SEND);
                                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out this app: " + appLink + "\nUse my referral code: " + referralCode);
                                    sendIntent.setType("text/plain");

                                    startActivity(Intent.createChooser(sendIntent, "Share app link and referral code"));
                                }
                            });
                        } else {
                            // Handle error response
//                            String message = response.getString("message");
//                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
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


    private void fetchData(String referralCode) {
        listView = findViewById(R.id.Referrallist); // Assuming your RecyclerView id is 'Referrallist'
        listView.setLayoutManager(new LinearLayoutManager(this));
        referralModels = new ArrayList<>();
        referralAdapter = new ReferralAdapter(this, referralModels);
        listView.setAdapter(referralAdapter);

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constant.ReferralTable + referralCode, null,
                response -> {
                    try {
                        String status = response.getString("Status");
                        if (status.equals("success")) {
                            JSONArray dataArray = response.getJSONArray("booking");
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject bookingObject = dataArray.getJSONObject(i);
                                String customerName = bookingObject.getString("name");
                                String createdAt = bookingObject.getString("created_at");
                                String months = bookingObject.getString("months");
                                String referralPayment = bookingObject.getString("referral_payment");

                                ReferralModel referralModel = new ReferralModel(
                                        customerName,
                                        createdAt,
                                        months,
                                        referralPayment
                                );
                                referralModels.add(referralModel);
                            }
                            referralAdapter.notifyDataSetChanged();  // Notify the adapter
                        } else {
                            // Handle error status
                            referralModels.clear(); // Clear the referralModels list
                            referralAdapter.notifyDataSetChanged();
                            // Show a message or handle the error in UI
//                            Toast.makeText(getApplicationContext(), "No bookings found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Handle error
                    Toast.makeText(getApplicationContext(), "Error occurred: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Volley Error", "Error occurred", error);
                });

        requestQueue.add(jsonObjectRequest);
    }

//    @Override
//    public boolean onSupportNavigateUp() {
//        onBackPressed();
//        return true;
//    }
}