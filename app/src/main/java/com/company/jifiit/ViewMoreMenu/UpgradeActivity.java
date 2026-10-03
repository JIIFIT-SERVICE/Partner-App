package com.company.jifiit.ViewMoreMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Adapter.BookingHistoryAdapter;
import com.company.jifiit.Adapter.CartAdapter;
import com.company.jifiit.Constant;
import com.company.jifiit.LocalStorage.DatabaseHelper;
import com.company.jifiit.R;
import com.company.jifiit.model.CartModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UpgradeActivity extends AppCompatActivity {
    String Id,bookingType;
    TextView order_id,cartBadge;
    private DatabaseHelper dbHelper;
    RecyclerView cart_list;
    CartAdapter cartAdapter;
    ArrayList<CartModel> cartModels;
    View view1,view2,view3,view4;
    ProgressBar progressBar;
    LinearLayout bundile,oil_massage,dry_massage,add_ons;
    float dX, dY;
    int lastAction;
    FloatingActionButton fab;

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        dbHelper = new DatabaseHelper(this);
        Id = getIntent().getStringExtra("id");
        bookingType = getIntent().getStringExtra("bookingType");
        if ("Exchange".equals(bookingType)){
            setTitle("Exchange massages");
        } else if ("Upgrade".equals(bookingType)) {
            setTitle("Upgrade massages");
        }

        progressBar = findViewById(R.id.progress_bar);
        view1 = findViewById(R.id.view1);
        view2 = findViewById(R.id.view2);
        view3 = findViewById(R.id.view3);
        view4 = findViewById(R.id.view4);

        cart_list = findViewById(R.id.cart_category);
        cartBadge = findViewById(R.id.cart_badge);

        // Set the layout manager to GridLayoutManager with 2 columns
        int numberOfColumns = 2; // This specifies two items in a row
        cart_list.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        // Initialize the list of cart models
        cartModels = new ArrayList<>();

        // Initialize the adapter and set it to the RecyclerView
        cartAdapter = new CartAdapter(this, cartModels,Id,bookingType);
        cart_list.setAdapter(cartAdapter);

//        order_id = findViewById(R.id.id);
//        order_id.setText(Id);

        bundile = findViewById(R.id.packages);
        oil_massage = findViewById(R.id.oil_massages);
        dry_massage = findViewById(R.id.dry_massages);
        add_ons = findViewById(R.id.add_ons);

        bundile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchDataFromAPI("packages");
                view1.setBackgroundColor(getResources().getColor(R.color.Green));
                view2.setBackgroundColor(getResources().getColor(R.color.white));
                view3.setBackgroundColor(getResources().getColor(R.color.white));
                view4.setBackgroundColor(getResources().getColor(R.color.white));
            }
        });
        bundile.performClick();
        oil_massage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchDataFromAPI("oil_massage");
                view2.setBackgroundColor(getResources().getColor(R.color.Green));
                view1.setBackgroundColor(getResources().getColor(R.color.white));
                view3.setBackgroundColor(getResources().getColor(R.color.white));
                view4.setBackgroundColor(getResources().getColor(R.color.white));
            }
        });

        dry_massage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchDataFromAPI("dry_massage");
                view3.setBackgroundColor(getResources().getColor(R.color.Green));
                view2.setBackgroundColor(getResources().getColor(R.color.white));
                view1.setBackgroundColor(getResources().getColor(R.color.white));
                view4.setBackgroundColor(getResources().getColor(R.color.white));
            }
        });

        add_ons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchDataFromAPI("add_on");
                view4.setBackgroundColor(getResources().getColor(R.color.Green));
                view2.setBackgroundColor(getResources().getColor(R.color.white));
                view3.setBackgroundColor(getResources().getColor(R.color.white));
                view1.setBackgroundColor(getResources().getColor(R.color.white));
            }
        });




        // Set click listener for cart icon
        findViewById(R.id.cart_icon).setOnClickListener(v -> {
            Intent intent = new Intent(UpgradeActivity.this, AddToCartActivity.class);
            intent.putExtra("id", Id);
            intent.putExtra("bookingType", bookingType);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

    }

    private void updateCartBadge() {

        int count = dbHelper.getCartItemCount(Id);
        if (count > 0) {
            cartBadge.setText(String.valueOf(count));
            cartBadge.setVisibility(View.VISIBLE);
        } else {
            cartBadge.setVisibility(View.INVISIBLE);
        }
    }

    private void fetchDataFromAPI(String type) {
        // Show the progress bar and hide the RecyclerView
        progressBar.setVisibility(View.VISIBLE);
        cart_list.setVisibility(View.GONE);
        if (!isNetworkAvailable()) {
            // If there's no internet connection, show a Toast message
            Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
            return; // Exit the method if there's no internet
        }
        String url = Constant.fetchApiFrom + type;

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        String status = response.getString("status");
                        if (status.equals("success")) {
                            cartModels.clear();
                            cart_list.setVisibility(View.VISIBLE);

                            JSONArray dataArray = response.getJSONArray("data");
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject item = dataArray.getJSONObject(i);

                                String id = item.getString("id");
                                String name = item.getString("name");
                                String image = item.getString("image");
                                String price = item.getString("price");
                                String discountPrice = item.getString("discountPrice");
                                String saveAmount = item.getString("saveamount");
                                String duration = item.getString("duration");
                                String discounts = item.getString("discounts");
                                String types = item.getString("type");
                                String nameCount = item.getString("nameCount");

                                CartModel cartItem = new CartModel(discountPrice, price, name, saveAmount, duration, nameCount, discounts, image, types, id);
                                cartModels.add(cartItem);
                            }
                            cartAdapter.notifyDataSetChanged();
                        } else {
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        // Hide the progress bar after data is loaded
                        progressBar.setVisibility(View.GONE);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE); // Hide progress bar on error
                }
        );

        requestQueue.add(jsonObjectRequest);
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }



    @Override
    public boolean onSupportNavigateUp () {
        dbHelper.deleteProducts(Id);
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        updateCartBadge();
        super.onResume();
    }
}