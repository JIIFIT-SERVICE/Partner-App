package com.company.jifiit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AccountSelectionActivity extends AppCompatActivity {

    private String phone;

    private TextView tvPhone,tvSubtitle, tvTherapistName, tvHotelName, tvWellnessName, tvCentreName;

    private View cardTherapist;
    private View cardHotel;
    private View cardWellness;
    private View cardCentre;

    private String selectedUserType;
    private String selectedUserId;

    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_account_selection);

        tvPhone = findViewById(R.id.tvPhone);
        tvSubtitle = findViewById(R.id.tvSubtitle);

        cardTherapist = findViewById(R.id.cardTherapist);
        cardHotel     = findViewById(R.id.cardHotel);
        cardWellness  = findViewById(R.id.cardWellness);
        cardCentre    = findViewById(R.id.cardCentre);
        tvTherapistName = findViewById(R.id.th_Name);
        tvHotelName = findViewById(R.id.ht_Name);
        tvWellnessName = findViewById(R.id.wb_Name);
        tvCentreName = findViewById(R.id.ct_Name);

        btnBack = findViewById(R.id.btnBack);

        phone = getIntent().getStringExtra("phone");

        String accountsJson =
                getIntent().getStringExtra("accounts");

        if (phone != null) {
            tvPhone.setText(phone);
        }

        cardTherapist.setVisibility(View.GONE);
        cardHotel.setVisibility(View.GONE);
        cardWellness.setVisibility(View.GONE);
        cardCentre.setVisibility(View.GONE);

        if (accountsJson != null) {
            loadAccounts(accountsJson);
        }

        cardTherapist.setOnClickListener(v -> {
            selectedUserType = "expert";
            openOTP();
        });

        cardHotel.setOnClickListener(v -> {
            selectedUserType = "hotel";
            openOTP();
        });

        cardWellness.setOnClickListener(v -> {
            selectedUserType = "wellness";
            openOTP();
        });

        cardCentre.setOnClickListener(v -> {
            selectedUserType = "centre";
            openOTP();
        });


        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
            );
        });
    }

    private void loadAccounts(String accountsJson) {

        int visibleCount = 0;

        try {

            JSONArray accounts =
                    new JSONArray(accountsJson);

            for (int i = 0; i < accounts.length(); i++) {

                JSONObject account =
                        accounts.getJSONObject(i);

                String userType =
                        account.getString("user_type");

                String userId = account.getString("user_id");
                String userName = account.getString("user_name");

                if (userType.equals("expert")) {

                    cardTherapist.setVisibility(View.VISIBLE);
                    cardTherapist.setTag(userId);
                    tvTherapistName.setText(userName);
                    visibleCount++;

                } else if (userType.equals("hotel")) {

                    cardHotel.setVisibility(View.VISIBLE);
                    cardHotel.setTag(userId);
                    tvHotelName.setText(userName);
                    visibleCount++;

                } else if (userType.equals("wellness")) {

                    cardWellness.setVisibility(View.VISIBLE);
                    cardWellness.setTag(userId);
                    tvWellnessName.setText(userName);
                    visibleCount++;

                } else if (userType.equals("centre")) {

                    cardCentre.setVisibility(View.VISIBLE);
                    cardCentre.setTag(userId);
                    tvCentreName.setText(userName);
                    visibleCount++;
                }
            }

            // 🔽 Dynamic subtitle based on count
            if (visibleCount == 1) {

                tvSubtitle.setText(
                        "Your account is linked to this phone number. Continue to proceed."
                );

            } else {

                tvSubtitle.setText(
                        "Multiple accounts are linked to this phone number. Select an account to continue."
                );
            }

        } catch (JSONException e) {

            e.printStackTrace();

            Toast.makeText(
                    this,
                    "Unable to load accounts",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void openOTP() {

        View selectedCard;

        switch (selectedUserType) {

            case "expert":
                selectedCard = cardTherapist;
                break;

            case "hotel":
                selectedCard = cardHotel;
                break;

            case "wellness":
                selectedCard = cardWellness;
                break;

            case "centre":
                selectedCard = cardCentre;
                break;

            default:
                selectedCard = null;
        }

        if (selectedCard == null) {
            return;
        }

        Object tag = selectedCard.getTag();

        if (tag == null) {

            Toast.makeText(
                    this,
                    "Account information missing",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        selectedUserId = tag.toString();

        Intent intent =
                new Intent(
                        AccountSelectionActivity.this,
                        OTPActivity.class
                );

        intent.putExtra("phone", phone);
        intent.putExtra("user_type", selectedUserType);
        intent.putExtra("user_id", selectedUserId);

        startActivity(intent);

        overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_left
        );
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Or if you want to do something custom:
        // finish();

        overridePendingTransition(
                R.anim.slide_in_left,
                R.anim.slide_out_right
        );
    }

}