package com.company.jifiit.Wallet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Constant;
import com.company.jifiit.R;
import com.company.jifiit.ReferDialog;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;

public class WalletAcitivity extends AppCompatActivity {

    private TextView balance;
    private Button debit, credit;
    private TableLayout creditLayout, headerLayout, debitLayout, headerLayout2;
    private CircularProgressIndicator progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_acitivity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Pocket");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ReferDialog.showReferDialog(this);

        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String idString = sp.getString("Id", "");
        BigInteger id = new BigInteger(idString);

        initializeViews();
        getBalance(id);
        setupButtonListeners(id);

        credit.performClick();
    }

    private void initializeViews() {
        creditLayout = findViewById(R.id.creditLayout);
        debitLayout = findViewById(R.id.debitLayout);
        headerLayout = findViewById(R.id.headerLayout);
        headerLayout2 = findViewById(R.id.headerLayout2);
        balance = findViewById(R.id.CoinBalance);
        debit = findViewById(R.id.debitBtn);
        credit = findViewById(R.id.creditsBtn);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupButtonListeners(BigInteger id) {
        debit.setOnClickListener(v -> {
            getDebits(id);
            creditLayout.setVisibility(View.GONE);
            headerLayout.setVisibility(View.GONE);
            headerLayout2.setVisibility(View.VISIBLE);
            debitLayout.setVisibility(View.VISIBLE);
            updateButtonStyles(true);
        });

        credit.setOnClickListener(v -> {
            getCredits(id);
            debitLayout.setVisibility(View.GONE);
            creditLayout.setVisibility(View.VISIBLE);
            headerLayout.setVisibility(View.VISIBLE);
            headerLayout2.setVisibility(View.GONE);
            updateButtonStyles(false);
        });
    }

    private void updateButtonStyles(boolean isDebitSelected) {
        if (isDebitSelected) {
            debit.setBackgroundColor(ContextCompat.getColor(this, R.color.ShapGreen));
            debit.setTextColor(Color.WHITE);
            credit.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
            credit.setTextColor(ContextCompat.getColor(this, R.color.ShapGreen));
        } else {
            credit.setBackgroundColor(ContextCompat.getColor(this, R.color.ShapGreen));
            credit.setTextColor(Color.WHITE);
            debit.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
            debit.setTextColor(ContextCompat.getColor(this, R.color.ShapGreen));
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            creditLayout.setVisibility(View.GONE);
            debitLayout.setVisibility(View.GONE);
            headerLayout.setVisibility(View.GONE);
            headerLayout2.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void getBalance(BigInteger id) {
        showLoading(true);
        String url = Constant.balance + id;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    showLoading(false);
                    try {
                        if (response.getString("Status").equals("success")) {
                            balance.setText(response.getString("totalFare"));
                        } else {
                            Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        showError("Error parsing balance data");
                    }
                },
                error -> {
                    showLoading(false);
                    showError("Network error");
                });

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void getCredits(BigInteger id) {
        showLoading(true);
        TableLayout tableLayout = findViewById(R.id.creditLayout);
        tableLayout.removeAllViews();

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                Constant.walletHistory + id,
                null,
                response -> {
                    showLoading(false);
                    try {
                        if (response.getString("Status").equals("success")) {
                            JSONArray dataArray = response.getJSONArray("booking");
                            if (dataArray.length() == 0) {
                                showNoDataMessage(tableLayout, 4);
                                return;
                            }

                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject bookingObject = dataArray.getJSONObject(i);
                                TableRow row = createTableRow(i);

                                row.addView(createCell(bookingObject.getString("date"), i));
                                row.addView(createCell(bookingObject.getString("time"), i));
                                row.addView(createCell("+"+bookingObject.getString("wallet"), i, true));
                                row.addView(createCell(bookingObject.getString("balance"), i, true));

                                tableLayout.addView(row);
                            }
                            headerLayout.setVisibility(View.VISIBLE);
                            creditLayout.setVisibility(View.VISIBLE);
                        } else {
                            showNoDataMessage(tableLayout, 4);
                        }
                    } catch (JSONException e) {
                        showError("Error parsing credits data");
                    }
                },
                error -> {
                    showLoading(false);
                    showError("Network error");
                });

        requestQueue.add(jsonObjectRequest);
    }

    private void getDebits(BigInteger id) {
        showLoading(true);
        TableLayout tableLayout = findViewById(R.id.debitLayout);
        tableLayout.removeAllViews();

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                Constant.fareHistory + id,
                null,
                response -> {
                    showLoading(false);
                    try {
                        if (response.getString("Status").equals("success")) {
                            JSONArray dataArray = response.getJSONArray("booking");
                            if (dataArray.length() == 0) {
                                showNoDataMessage(tableLayout, 5);
                                return;
                            }

                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject bookingObject = dataArray.getJSONObject(i);
                                TableRow row = createTableRow(i);

                                row.addView(createCell(bookingObject.getString("time") + "\n" +
                                        bookingObject.getString("date"), i));
                                row.addView(createCell(bookingObject.getString("order_id"), i));
                                row.addView(createCell(bookingObject.getString("fareType"), i));
                                row.addView(createCell("-"+bookingObject.getString("fareValue"), i, false));
                                row.addView(createCell(bookingObject.getString("farebalance"), i, false));

                                tableLayout.addView(row);
                            }
                            headerLayout2.setVisibility(View.VISIBLE);
                            debitLayout.setVisibility(View.VISIBLE);
                        } else {
                            showNoDataMessage(tableLayout, 5);
                        }
                    } catch (JSONException e) {
                        showError("Error parsing debits data");
                    }
                },
                error -> {
                    showLoading(false);
                    showError("Network error");
                });

        requestQueue.add(jsonObjectRequest);
    }

    private TableRow createTableRow(int index) {
        TableRow row = new TableRow(this);
        row.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        row.setBackgroundColor(ContextCompat.getColor(this,
                index % 2 == 0 ? R.color.row_even : R.color.row_odd));

        return row;
    }

    private TextView createCell(String text, int rowIndex) {
        return createCell(text, rowIndex, null);
    }

    private TextView createCell(String text, int rowIndex, Boolean isCredit) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(16, 12, 16, 12);

        int textColor;
        if (isCredit == null) {
            textColor = ContextCompat.getColor(this, R.color.black);
        } else {
            textColor = ContextCompat.getColor(this,
                    isCredit ? R.color.Green : R.color.Red);
        }
        textView.setTextColor(textColor);

        TableRow.LayoutParams params = new TableRow.LayoutParams(
                0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
        textView.setLayoutParams(params);

        return textView;
    }

    private void showNoDataMessage(TableLayout tableLayout, int columnSpan) {
        TableRow row = new TableRow(this);
        row.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        TextView message = new TextView(this);
        message.setText(R.string.no_transaction_history);
        message.setTextColor(ContextCompat.getColor(this, R.color.black));
        message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        message.setGravity(Gravity.CENTER);
        message.setPadding(0, 24, 0, 24);

        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        params.span = columnSpan;

        message.setLayoutParams(params);
        row.addView(message);
        tableLayout.addView(row);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.wallet_manue,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add_request) {
            Intent intent = new Intent(WalletAcitivity.this, WalletRequestActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.help) {
            String phoneNumber = "tel:+919717550414";
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse(phoneNumber));
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}