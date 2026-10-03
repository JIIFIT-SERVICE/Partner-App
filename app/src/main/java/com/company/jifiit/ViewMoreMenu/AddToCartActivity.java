package com.company.jifiit.ViewMoreMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Adapter.AddToCartAdapter;
import com.company.jifiit.Constant;
import com.company.jifiit.LocalStorage.DatabaseHelper;
import com.company.jifiit.R;
import com.company.jifiit.model.AddToCartModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddToCartActivity extends AppCompatActivity implements OnTotalPriceChangeListener, OnTotalOriginalPriceChangeListener {

    String orderId, bookingType, url;
    TextView totalOriginal, totalDiscount;
    DatabaseHelper dbHelper;
    RecyclerView cart_list;
    Button orderBtn;
    ProgressBar btnProgressBar;
    AddToCartAdapter addToCartAdapter;
    ArrayList<AddToCartModel> addToCartModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_cart);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Your Cart");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Initialize RecyclerView and other components
        cart_list = findViewById(R.id.add_to_cart_category);
        cart_list.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Initialize the cart models list and adapter
        addToCartModels = new ArrayList<>();
        addToCartAdapter = new AddToCartAdapter(this, addToCartModels, this, this);
        cart_list.setAdapter(addToCartAdapter);

        // Get the orderId from Intent
        orderId = getIntent().getStringExtra("id");
        bookingType = getIntent().getStringExtra("bookingType");
        totalOriginal = findViewById(R.id.service_total_original);
        totalDiscount = findViewById(R.id.service_total_discounted);

        // Initialize progress bar
        btnProgressBar = findViewById(R.id.btnProgressBar);

        orderBtn = findViewById(R.id.orderBtn);
        if ("Upgrade".equals(bookingType)) {
            orderBtn.setText("CONFIRM UPGRADE");
        } else if ("Exchange".equals(bookingType)) {
            orderBtn.setText("CONFIRM EXCHANGE");
        }

        // Fetch data from SQLite for the given orderId
        fetchDataFromSQLite(orderId);

        orderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeUpgrade(orderId);
            }
        });
    }

    @SuppressLint("Range")
    private void fetchDataFromSQLite(String orderId) {
        // Clear previous data
        addToCartModels.clear();

        // Fetch the cursor from the fetchServices method
        Cursor cursor = dbHelper.fetchAddedProduct(orderId);
        float totalDiscountPrice = 0.0f;
        float totalOriginalPrice = 0.0f;
        // Check if any rows were returned
        if (cursor.getCount() > 0) {
            // Loop through the cursor to retrieve each row's data
            while (cursor.moveToNext()) {
                // Extract data from the current row
                String productName = cursor.getString(cursor.getColumnIndex("product_name"));
                String duration = cursor.getString(cursor.getColumnIndex("duration"));
                String quantity = cursor.getString(cursor.getColumnIndex("quantity"));
                String productId = cursor.getString(cursor.getColumnIndex("product_id"));
                String type = cursor.getString(cursor.getColumnIndex("type"));
                String orderID = cursor.getString(cursor.getColumnIndex("order_id"));
                String discountPrice = cursor.getString(cursor.getColumnIndex("discount_price"));
                String originalPrice = cursor.getString(cursor.getColumnIndex("original_price"));

                int Intquantity = Integer.parseInt(quantity);
                float FloatdiscountPrice = Float.parseFloat(discountPrice);
                float FloatOriginalPice = Float.parseFloat(originalPrice);

                // Calculate the total discount price for this product
                float productTotalDiscountPrice = FloatdiscountPrice * Intquantity;
                float productTotalOriginalPrice = FloatOriginalPice * Intquantity;

                // Add to the running total for the entire cart
                totalDiscountPrice += productTotalDiscountPrice;
                totalOriginalPrice += productTotalOriginalPrice;

                AddToCartModel addToCartModel =
                        new AddToCartModel(productName,
                                duration,
                                quantity,
                                productId,
                                type,
                                orderID,
                                discountPrice,
                                originalPrice);

                // Add the AddToCartModel to the list
                addToCartModels.add(addToCartModel);

                totalDiscount.setText("₹" + totalDiscountPrice);
                totalOriginal.setText("₹" + totalOriginalPrice);
            }

            // Notify the adapter about the data change
            addToCartAdapter.notifyDataSetChanged();
        } else {
            // Handle case when no data is found
            // Toast.makeText(getApplicationContext(), "No items found for this order.", Toast.LENGTH_SHORT).show();
        }

        // Close the cursor after use
        cursor.close();
    }

    @SuppressLint("Range")
    private void placeUpgrade(String orderId) {
        // Show loading indicator and disable button
        showProgress();

        // Fetch the cursor from the fetchServices method
        Cursor cursor = dbHelper.fetchAddedProduct(orderId);
        JSONArray jsonArray = new JSONArray();  // Create a JSON array to hold product details

        // Check if any rows were returned
        if (cursor.getCount() > 0) {
            // Loop through the cursor to retrieve each row's data
            while (cursor.moveToNext()) {
                // Extract data from the current row
                String productName = cursor.getString(cursor.getColumnIndex("product_name"));
                String duration = cursor.getString(cursor.getColumnIndex("duration"));
                String quantity = cursor.getString(cursor.getColumnIndex("quantity"));
                String productId = cursor.getString(cursor.getColumnIndex("product_id"));
                String type = cursor.getString(cursor.getColumnIndex("type"));
                String discounts = cursor.getString(cursor.getColumnIndex("discount"));
                String orderID = cursor.getString(cursor.getColumnIndex("order_id"));
                String discountPrice = cursor.getString(cursor.getColumnIndex("discount_price"));
                String originalPrice = cursor.getString(cursor.getColumnIndex("original_price"));

                // Create a JSON object for the current product
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("product_name", productName);
                    jsonObject.put("duration", duration);
                    jsonObject.put("quantity", quantity);
                    jsonObject.put("product_id", productId);
                    jsonObject.put("type", type);
                    jsonObject.put("discount", discounts);
                    jsonObject.put("order_id", orderID);
                    jsonObject.put("discount_price", discountPrice);
                    jsonObject.put("original_price", originalPrice);
                } catch (JSONException e) {
                    Log.e("JSON Error", "Failed to create JSON object: " + e.toString());
                }

                // Add the product JSON object to the array
                jsonArray.put(jsonObject);
            }
        } else {
            // Handle case when no data is found
            Toast.makeText(getApplicationContext(), "Your cart is empty. Please add items to continue.", Toast.LENGTH_SHORT).show();
            cursor.close();  // Close the cursor early if no items found
            hideProgress(); // Hide progress if no items found
            return; // Exit the method early
        }

        // Close the cursor after use
        cursor.close();

        // Send the JSON array to the PHP API only if it contains data
        if (jsonArray.length() > 0) {
            sendDataToApi(jsonArray);
        } else {
            Log.e("Data Error", "No data to send to the API.");
            hideProgress(); // Hide progress if no data to send
        }
    }

    private void sendDataToApi(JSONArray jsonArray) {
        if ("Upgrade".equals(bookingType)) {
            url = Constant.placeUpgrade;
        } else if ("Exchange".equals(bookingType)) {
            url = Constant.placeExchange;
        }

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null,
                response -> {
                    // Hide progress when response is received
                    hideProgress();

                    try {
                        String status = response.getString("status");
                        String message = response.getString("message");

                        if ("success".equals(status)) {
                            // Show success message
                            if ("Upgrade".equals(bookingType)) {
                                Toast.makeText(getApplicationContext(), "Successfully added your upgrade", Toast.LENGTH_SHORT).show();
                            } else if ("Exchange".equals(bookingType)) {
                                Toast.makeText(getApplicationContext(), "Successfully exchanged your massage", Toast.LENGTH_SHORT).show();
                            }

                            // Clear cart data
                            dbHelper.deleteProducts(orderId);

                            // Navigate back to previous activity
                            setResult(RESULT_OK); // Optional: Set result if you need to notify the previous activity
                            finish(); // This will close the current activity and go back

                        } else {
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Hide progress when error occurs
                    hideProgress();

                    String errorMsg = "Error sending data";
                    if (error instanceof TimeoutError) {
                        errorMsg = "Request timeout - please try again";
                    } else if (error instanceof NoConnectionError) {
                        errorMsg = "No internet connection";
                    } else if (error.networkResponse != null) {
                        try {
                            errorMsg = new String(error.networkResponse.data, "utf-8");
                        } catch (Exception e) {
                            errorMsg = "Server error: " + error.networkResponse.statusCode;
                        }
                    }
                    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                    Log.e("API", "Request failed", error);
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }

            @Override
            public byte[] getBody() {
                return jsonArray.toString().getBytes();
            }
        };

        requestQueue.add(jsonObjectRequest);
    }

    // Helper method to show progress
    private void showProgress() {
        btnProgressBar.setVisibility(View.VISIBLE);
        orderBtn.setText(""); // Clear button text
        orderBtn.setEnabled(false); // Disable button to prevent multiple clicks
    }

    // Helper method to hide progress
    private void hideProgress() {
        btnProgressBar.setVisibility(View.GONE);

        // Restore button text based on booking type
        if ("Upgrade".equals(bookingType)) {
            orderBtn.setText("CONFIRM UPGRADE");
        } else if ("Exchange".equals(bookingType)) {
            orderBtn.setText("CONFIRM EXCHANGE");
        }

        orderBtn.setEnabled(true); // Re-enable button
    }

    @Override
    public void onTotalPriceChanged(double totalPrice) {
        // Update the totalMoneyTextView when data changes
        totalDiscount.setText(String.format("₹%.2f", totalPrice));
    }

    @Override
    public void onTotalOriginalPriceChanged(double totalOriginalPrice) {
        totalOriginal.setText(String.format("₹%.2f", totalOriginalPrice));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}