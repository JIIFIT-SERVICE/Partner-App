package com.company.jifiit.Leave;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Adapter.LeaveAdapter;
import com.company.jifiit.Constant;

import com.company.jifiit.R;
import com.company.jifiit.ReferDialog;
import com.company.jifiit.model.LeaveModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;

public class LeaveHistory extends AppCompatActivity {
    Button Weekly, Yearly, Illness;
    RecyclerView LeaveList;
    LeaveAdapter leaveAdapter;
    ImageView back;
    ArrayList<LeaveModel> leaveModels;
    BigInteger Id;
    TextView weekName,weekBalance,totalYear,YearlyBalance,totalinlless,inllessBalance;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_leave_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Leaves");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        ReferDialog.showReferDialog(this);
        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String name = sp.getString("Name", "Default Name");
        String idString = sp.getString("Id", "");
        Id = new BigInteger(idString);
//        balanceLeave(Id);
//        yearBalance(Id);
//        illness(Id);

        LeaveList = findViewById(R.id.LeaveList);


//        Weekly = findViewById(R.id.Weekly);
//        Yearly = findViewById(R.id.Yearly);
//        Illness = findViewById(R.id.Inlless);



    }

//    private void illness(BigInteger id) {
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
//                (Request.Method.GET, Constant.inllessAvailable + id, null, response -> {
//                    try {
//                        String balance = response.getString("YearlyOff");
//                        String yearlyCount = response.getString("YearlyCount");
//
//                        // Update UI with balance and yearly count
//                        totalinlless.setText("Total: " + yearlyCount);
//                        inllessBalance.setText("Balance: " + balance);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }, error -> {
//                    // Handle error
//                    Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_SHORT).show();
//                });
//
//        // Add the request to the RequestQueue
//        Volley.newRequestQueue(this).add(jsonObjectRequest);
//    }
//
//    private void yearBalance(BigInteger id) {
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
//                (Request.Method.GET, Constant.yearlyAvailable + id, null, response -> {
//                    try {
//                        String balance = response.getString("YearlyOff");
//                        String yearlyCount = response.getString("YearlyCount");
//
//                        // Update UI with balance and yearly count
//                        totalYear.setText("Total: " + yearlyCount);
//                        YearlyBalance.setText("Balance: " + balance);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }, error -> {
//                    // Handle error
//                    Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_SHORT).show();
//                });
//
//        // Add the request to the RequestQueue
//        Volley.newRequestQueue(this).add(jsonObjectRequest);
//    }
//
//
//    private void balanceLeave(BigInteger id) {
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
//                (Request.Method.GET, Constant.weeklyAvailable + id, null, response -> {
//                    try {
//                        String balance = response.getString("Balance");
//                        String weeklyOffDay = response.getString("WeeklyOffDay");
//
//                        // Update UI with balance and weekly off day
//                        weekName.setText("Day: " + weeklyOffDay);
//                        weekBalance.setText("Balance: " + balance);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }, error -> {
//                    // Handle error
//                    Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_SHORT).show();
//                });
//
//        // Add the request to the RequestQueue
//        Volley.newRequestQueue(this).add(jsonObjectRequest);
//
//    }

    private void fetchHistory(BigInteger id, String type) {
        LeaveList.setLayoutManager(new LinearLayoutManager(this));
        leaveModels = new ArrayList<>();
        leaveAdapter = new LeaveAdapter(leaveModels, this);
        LeaveList.setAdapter(leaveAdapter);

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, Constant.leaveHistory + id + "&type=" + type, null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject jsonObject = response.getJSONObject(i);
//                                String id = jsonObject.getString("id");
                            String dayName = jsonObject.getString("dayName"); // Ensure key matches
                            String date = jsonObject.getString("date");
                            String dayQuantitis = jsonObject.getString("dayQuantitis");
                            String approval = jsonObject.getString("approval");
                            String fromTime = jsonObject.getString("fromtime");
                            String  ToTime= jsonObject.getString("totime");
                            String leave_type= jsonObject.getString("leave_type");

                            LeaveModel leaveModel = new LeaveModel(dayName, date, dayQuantitis, approval,ToTime,fromTime,leave_type);
                            leaveModels.add(leaveModel);
                        }
                        leaveAdapter.notifyDataSetChanged();  // Notify the adapter
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Handle error
                    Toast.makeText(getApplicationContext(), "Error occurred: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Volley Error", "Error occurred", error);
                });

        requestQueue.add(jsonArrayRequest);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.leave_history,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

//        if (id == R.id.item1) {
//            Intent intent = new Intent(LeaveHistory.this, LeaveActivity.class);
//            startActivity(intent);
//            return true;
//        }else
            if (id == R.id.item4) {
            Intent intent = new Intent(LeaveHistory.this, CasualLeave.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        fetchHistory(Id, "CASUAL+LEAVE");
        super.onResume();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
