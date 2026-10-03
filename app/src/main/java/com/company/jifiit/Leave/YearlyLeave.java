package com.company.jifiit.Leave;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Constant;
import com.company.jifiit.R;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class YearlyLeave extends AppCompatActivity {
    EditText dateNumber,FromDate,Todate;
    TextView date;
    CalendarView calendarView;
    Button Apply;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yearly_leave);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Yearly leave");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        dateNumber=findViewById(R.id.TotalDays);
        date=findViewById(R.id.date_edit_text);
        Apply=findViewById(R.id.apply_button);
        calendarView=findViewById(R.id.calender);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(year, month, dayOfMonth);

                Calendar currentDate = Calendar.getInstance();

                if (selectedDate.before(currentDate)) {
                    Toast.makeText(getApplicationContext(), "Please select a date on or after today", Toast.LENGTH_SHORT).show();
                } else {
                    // Format the month and day to include leading zeros
                    String formattedMonth = String.format("%02d", month + 1); // Adding 1 to month because it's zero-based
                    String formattedDayOfMonth = String.format("%02d", dayOfMonth);

                    // Update the TextView with the selected date
                    String selectedDateStr = year + "-" + formattedMonth + "-" + formattedDayOfMonth;
                    date.setText(selectedDateStr);
                }
            }
        });
        Apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Submit();
            }
        });
    }

    private void Submit() {
        final String Date = date.getText().toString().trim();
        final String TotalDays = dateNumber.getText().toString().trim();
        if (Date.isEmpty() || TotalDays.isEmpty()){
            Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences sp = getSharedPreferences("credentials", MODE_PRIVATE);
        String idString = sp.getString("Id", "");
        BigInteger Id = new BigInteger(idString);
        final String id = Id.toString();

        String url = Constant.yearlyOff + id;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(YearlyLeave.this, response, Toast.LENGTH_SHORT).show();
                        date.setText("");
                        dateNumber.setText("");
                        showMessageDialog(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(YearlyLeave.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                params.put("selectDate", Date);
                params.put("dayQuantitis", TotalDays);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }
    private void showMessageDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Close the dialog
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}