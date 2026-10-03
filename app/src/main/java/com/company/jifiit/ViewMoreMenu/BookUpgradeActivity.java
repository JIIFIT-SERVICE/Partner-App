package com.company.jifiit.ViewMoreMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Adapter.SelectMassageAdapter;
import com.company.jifiit.Constant;
import com.company.jifiit.LocalStorage.DatabaseHelper;
import com.company.jifiit.MassageWorkingActivity;
import com.company.jifiit.R;
import com.company.jifiit.model.SelectMassageModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class BookUpgradeActivity extends AppCompatActivity implements OnTotalPriceChangeListener {

    SelectMassageAdapter selectMassageAdapter;
    ArrayList<SelectMassageModel> selectMassageModels;
    RecyclerView cart_list;
    private ProgressBar progressBar;
    String Orderid, ProductName, TableName,bookingType;
    TextView Product, totalMoneyTextView;
    ImageView coverImage,oilImage,RecommendedImage,BenefitsImage,pressureImage,techniqueImage;
    Button orderBtn;

    TextView coverDsc,techniqueDsc,pressureDsc,oilDsc,benefitsDsc,recommendedDsc;

    private RequestQueue requestQueue;
    private static final String API_URL = ""; // Replace with your actual script URL
    double totalDiscountPrice = 0.0;
    DatabaseHelper dbHelper;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_upgrade);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Select Massages");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Fetching Intent Data
        Orderid = getIntent().getStringExtra("id");
        ProductName = getIntent().getStringExtra("product_name");
        TableName = getIntent().getStringExtra("table_name");
        bookingType = getIntent().getStringExtra("bookingType");


        coverImage = findViewById(R.id.coverImage);
        oilImage = findViewById(R.id.oilImage);
        BenefitsImage = findViewById(R.id.benifitsImage);
        techniqueImage = findViewById(R.id.techniqueImage);
        pressureImage = findViewById(R.id.pressureImage);
        RecommendedImage = findViewById(R.id.recommendedImage);

        coverDsc = findViewById(R.id.coverDescription);
        techniqueDsc = findViewById(R.id.techniqueDescription);
        pressureDsc = findViewById(R.id.pressureDescription);
        oilDsc = findViewById(R.id.OilDescription);
        benefitsDsc = findViewById(R.id.BenefitsDescription);
        recommendedDsc = findViewById(R.id.RecommendedDescription);



        requestQueue = Volley.newRequestQueue(this);

        Product = findViewById(R.id.productName);
        Product.setText(ProductName);

        // Initialize total money TextView
        orderBtn = findViewById(R.id.orderBtn);
        totalMoneyTextView = findViewById(R.id.totalMoney);

        // Load stored discount price
//        totalDiscountPrice = getTotalDiscountPrice();
        totalMoneyTextView.setText("₹" + totalDiscountPrice);

        orderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BookUpgradeActivity.this, AddToCartActivity.class);
                intent.putExtra("id", Orderid);
                intent.putExtra("bookingType", bookingType);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        // Initialize RecyclerView and List
        cart_list = findViewById(R.id.cart_category);
        progressBar = findViewById(R.id.progressBar);
        selectMassageModels = new ArrayList<>();
        selectMassageAdapter = new SelectMassageAdapter(this, selectMassageModels, Orderid, ProductName, this); // Pass the activity instance

        // Setup RecyclerView
        cart_list.setLayoutManager(new LinearLayoutManager(this));
        cart_list.setAdapter(selectMassageAdapter);

        // Fetch Services
        fetchServices(ProductName, TableName);
        detailsAbout(ProductName, TableName);
        fetchImages();

        dbHelper = new DatabaseHelper(this);
        fetchProduct(Orderid);
    }

    private void detailsAbout(String productName, String tableName) {
        // Define the URL of the PHP script
        String url = Constant.detailsAbout + productName + "&table=" + tableName;


        // Initialize a RequestQueue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // Create a JsonObjectRequest to fetch data
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> handleResponse(response),
                error -> handleVolleyError(error)
        );

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    // Method to handle the response from the server
    private void handleResponse(JSONObject response) {
        try {
            String status = response.getString("status");
            if ("success".equals(status)) {
                // Fetch the data object
                JSONObject dataObject = response.getJSONObject("data");

                // Extract data from JSON object
                String cover = dataObject.optString("cover", "N/A");
                String technique = dataObject.optString("technique", "N/A");
                String pressure = dataObject.optString("pressure", "N/A");
                String oil = dataObject.optString("oil", "N/A");
                String benefits = dataObject.optString("benefits", "N/A");
                String recommendedFor = dataObject.optString("recommended_for", "N/A");

                // Update UI elements
                updateUI(cover, technique, pressure, oil, benefits, recommendedFor);

                // Notify the adapter about the data change (if applicable)
                selectMassageAdapter.notifyDataSetChanged();
            } else {
            }
        } catch (JSONException e) {
            e.printStackTrace();
//            Toast.makeText(this, "JSON Parsing Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Method to handle Volley errors
    private void handleVolleyError(VolleyError error) {
        Toast.makeText(this, "Network Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
//        Log.e("VolleyError", "Error occurred", error);
    }

    // Method to update the UI with fetched data
    private void updateUI(String cover, String technique, String pressure, String oil, String benefits, String recommendedFor) {
        coverDsc.setText(cover);
        techniqueDsc.setText(technique);
        pressureDsc.setText(pressure);
        oilDsc.setText(oil);
        benefitsDsc.setText(benefits);
        recommendedDsc.setText(recommendedFor);
    }
    private void fetchProduct(String orderId) {
        // Fetch the total price using the order ID
        double totalPrice = dbHelper.getTotalPriceByOrderId(orderId);

        // Display the total in the totalMoneyTextView
        if (totalPrice > 0) {
            totalMoneyTextView.setText(String.format("₹: %.2f", totalPrice));
        } else {
            totalMoneyTextView.setText("₹: 0.0");
        }
    }




    private void fetchServices(String productName, String tableName) {
        // Show ProgressBar and hide RecyclerView while loading
        progressBar.setVisibility(View.VISIBLE);
        cart_list.setVisibility(View.GONE);

        // Define the URL of the PHP script
        String url = Constant.fetchServices + productName + "&table=" + tableName;

        // Initialize a RequestQueue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // Create a JsonObjectRequest to fetch data
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        // Check response status
                        String status = response.getString("status");
                        if (status.equals("success")) {
                            // Clear existing data
                            selectMassageModels.clear();

                            // Fetch data array
                            JSONArray dataArray = response.getJSONArray("data");
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject item = dataArray.getJSONObject(i);

                                // Extract data from JSON object
                                String id = item.getString("id");
                                String originalPrice = item.getString("price");
                                String discountPrice = item.getString("discountPrice");
                                String discounts = item.getString("discounts");
                                String duration = item.getString("duration");
                                String type = item.getString("type");

                                // Create a SelectMassageModel object
                                SelectMassageModel massageItem = new SelectMassageModel(id, originalPrice, discountPrice, discounts, duration, type);

                                // Add the SelectMassageModel to the list
                                selectMassageModels.add(massageItem);
                            }

                            // Notify the adapter about the data change
                            selectMassageAdapter.notifyDataSetChanged();

                            // Show RecyclerView and hide ProgressBar
                            cart_list.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                        } else {
                            // Handle no data found case
//                            Toast.makeText(getApplicationContext(), "No data found", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE); // Hide ProgressBar
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
//                        Toast.makeText(getApplicationContext(), "Error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE); // Hide ProgressBar
                    }
                },
                error -> {
                    // Handle Volley error
//                    Toast.makeText(getApplicationContext(), "Volley Error occurred: " + error.getMessage(), Toast.LENGTH_SHORT).show();
//                    Log.e("VolleyError", "Error occurred", error);

                    // Hide ProgressBar on error
                    progressBar.setVisibility(View.GONE);
                }
        );

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }







    private void fetchImages() {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, Constant.fetchIcons, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            if (response.length() > 0) {
                                setImage(BenefitsImage, response.getString(0));
                            }
                            if (response.length() > 1) {
                                setImage(coverImage, response.getString(1));
                            }
                            if (response.length() > 2) {
                                setImage(oilImage, response.getString(2));
                            }
                            if (response.length() > 3) {
                                setImage(pressureImage, response.getString(3));
                            }
                            if (response.length() > 4) {
                                setImage(RecommendedImage, response.getString(4));
                            }
                            if (response.length() > 5) {
                                setImage(techniqueImage, response.getString(5));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle the error
            }
        });

        requestQueue.add(jsonArrayRequest);
    }

    private void setImage(ImageView imageView, String url) {
        ImageRequest imageRequest = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        imageView.setImageBitmap(response);
                    }
                }, 0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.RGB_565,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        imageView.setImageResource(R.drawable.image_24); // Your placeholder
                    }
                });

        requestQueue.add(imageRequest);
    }



    @Override
    public void onTotalPriceChanged(double totalPrice) {
        // Update the totalMoneyTextView when data changes
        totalMoneyTextView.setText(String.format("₹: %.2f", totalPrice));
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Call the function to set the End Time when coming back from UpgradeActivity
        fetchProduct(Orderid);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
