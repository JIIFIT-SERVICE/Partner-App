package com.company.jifiit.ViewMoreMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Constant;
import com.company.jifiit.R;
import com.company.jifiit.ViewMoreActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ViewTableAcitivity extends AppCompatActivity {

    String Id;
    TableLayout tableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_table_acitivity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("View table");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        tableLayout = findViewById(R.id.tableLayout);

        Id = getIntent().getStringExtra("id");
        fetchData(Id);
    }

    private void fetchData(String id) {
        String url = Constant.view_table + id;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        populateTable(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ViewTableAcitivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest);
    }

    private void populateTable(JSONArray data) {
        int totalDurationInMinutes = 0; // For summing up durations
        double totalDiscountPrice = 0; // For summing up discount prices

        try {
            for (int i = 0; i < data.length(); i++) {
                JSONObject jsonObject = data.getJSONObject(i);

                String itemType = jsonObject.getString("item_type");
                String itemName = jsonObject.getString("item_name");
                String itemQuantity = jsonObject.getString("item_quantity");
                String itemDuration = jsonObject.getString("item_duration");

                // Convert duration to an integer
                // Create a new row for the table
                TableRow tableRow = new TableRow(this);

                // Add columns to the row with the item details and different gravity
                tableRow.addView(createTextView(itemType, Gravity.LEFT, false)); // Left-aligned
                tableRow.addView(createTextView(itemName, Gravity.CENTER, false)); // Center-aligned
                tableRow.addView(createTextView(itemQuantity, Gravity.RIGHT, false)); // Center-aligned
                tableRow.addView(createTextView(itemDuration, Gravity.LEFT, false)); // Left-aligned

                // Add the row to the TableLayout
                tableLayout.addView(tableRow);
            }

            // Add totals row after populating the data

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to create a TextView for each table cell with customizable gravity
    private TextView createTextView(String text, int gravity, boolean isHeader) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setGravity(gravity); // Set custom gravity

        // Set padding in pixels
        int padding = dpToPx(16); // Convert 16 dp to pixels
        textView.setPadding(padding, padding, padding, padding);

        // Set layout parameters
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, // Width
                TableRow.LayoutParams.WRAP_CONTENT  // Height
        );
        layoutParams.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4)); // Set margins
        textView.setLayoutParams(layoutParams);

        if (isHeader) {
            textView.setTypeface(null, Typeface.BOLD); // Bold text for headers
        }

        return textView;
    }

    // Convert dp to pixels
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}