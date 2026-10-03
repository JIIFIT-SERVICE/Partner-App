package com.company.jifiit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Adapter.CustomImageAdapter;
import com.company.jifiit.ViewMoreMenu.CompletePaymenteActivity;
import com.company.jifiit.ViewMoreMenu.CompletedActivity;
import com.company.jifiit.ViewMoreMenu.UpgradeActivity;
import com.company.jifiit.ViewMoreMenu.ViewTableAcitivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MassageWorkingActivity extends AppCompatActivity {


    TextView order_id, Start_Time, End_Time, normal_timer,upgrade_timer,count;
    private View overlay;
    CountDownTimer normal_countDownTimer,upgrade_countDownTimer;

    ViewPager2 viewPager;
    CustomImageAdapter adapter;
    boolean isFormVisible = false;

    LinearLayout upgrade_layout;

    private MediaPlayer mediaPlayer;
    private ImageButton playPauseButton;
    private ImageButton muteButton;
    boolean isPlaying = false;
    boolean isMuted = false;
    boolean normal_isTimerRunning = false;
    boolean upgrade_isTimerRunning = false;
    long upgrade_remainingTimeInMillis = 0;
    long normal_remainingTimeInMillis = 0;
    Button normal_Stopbtn,upgrade_StopBtn;
    Button upgrade_btn,completed_btn,massages_view_btn;
    TextClock clock;
    String Id, startTime, endTime, name;
    private String formattedDuration;
    int currentPage = 0;
    private final int delay = 3000; // 3 seconds delay
    private final Handler handler = new Handler();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_massage_working);

        clock = findViewById(R.id.textClock);

        upgrade_btn = findViewById(R.id.extendbtn);
        massages_view_btn = findViewById(R.id.massagesbtn);
        completed_btn = findViewById(R.id.completedbtn);


        Start_Time = findViewById(R.id.Start_time);
        End_Time = findViewById(R.id.End_time);





        Id = getIntent().getStringExtra("id");
        endTime = getIntent().getStringExtra("end_time");
        startTime = getIntent().getStringExtra("start_time");
        name = getIntent().getStringExtra("customer_name");

//        End_Time.setText(endTime);


        // Set the clock format
        clock.setFormat12Hour("hh:mm a");
        clock.setFormat24Hour(null);


        massages_view_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MassageWorkingActivity.this, ViewTableAcitivity.class);
                intent.putExtra("id", Id);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });


        upgrade_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MassageWorkingActivity.this, UpgradeActivity.class);
                intent.putExtra("id", Id);
                intent.putExtra("bookingType","Upgrade");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });


        completed_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MassageWorkingActivity.this);
                builder.setTitle("Confirmation");
                builder.setMessage("Are you sure you want to complete this session?");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SimpleDateFormat uploadPutFormat = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            uploadPutFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        }
                        String uploadCurrentTime = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            uploadCurrentTime = uploadPutFormat.format(new Date());
                        }

//                        Intent intent = new Intent(MassageWorkingActivity.this, CompletedActivity.class);

                        Intent intent = new Intent(MassageWorkingActivity.this, CompletePaymenteActivity.class);
                        intent.putExtra("id", Id);
                        intent.putExtra("customer_name", name);

                        pauseAudio();
                        Upgrade_stopTimer();
                        normal_stopTimer();
                        uploadTime(uploadCurrentTime, Id, "End_time");

                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        dialog.dismiss();
                        finish(); // ✅ good
                    }
                });

                builder.setNegativeButton("No", null);
                builder.show();
            }
        });


        // Set the "No" button to cancel
//                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss(); // Close the dialog without any action
//                    }
//                });
//
//                // Show the dialog
//                builder.create().show();
//            }
//        });


        normal_timer = findViewById(R.id.timerTextView);
        upgrade_timer = findViewById(R.id.UpgradeTimerTextView);

        upgrade_layout = findViewById(R.id.upgrade_timer);
        upgrade_layout.setVisibility(View.GONE);
        normal_Stopbtn = findViewById(R.id.stopButton);
        upgrade_StopBtn = findViewById(R.id.UpgradeStopButton);
//        fetchStopReasonAndUpdateOrder(Id);
        //updateEndTime(Id);
        checkTimer(Id);
        initializeMediaPlayer();
        fetchImages();
        fetchTimer(Id);


        normal_Stopbtn.setOnClickListener(v -> {
            if (normal_isTimerRunning) {

                if (!isFormVisible) {
                    // Show the bottom sheet form
                    Normal_showBottomSheetForm();
                }
            } else {
                normal_startTimer(normal_remainingTimeInMillis);
               // Start the timer with the remaining time
            }
        });

        upgrade_StopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!normal_isTimerRunning && upgrade_isTimerRunning) {

                    if (!isFormVisible) {
                        // Show the bottom sheet form
                        Upgrade_showBottomSheetForm();
                    }
                } else {
                    upgrade_startTimer(upgrade_remainingTimeInMillis);
                }
            }
        });






        playPauseButton = findViewById(R.id.playPauseButton);
        muteButton = findViewById(R.id.muteButton);

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    pauseAudio();
                } else {
                    playAudio();
                }
            }
        });

        muteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMute();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                resetPlayer();
            }
        });
    }




    private void checkTimer(String id) {
        String url = Constant.remaining_time+id;
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // Create a new JsonArrayRequest to fetch the JSON response
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Process the JSON response
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = response.getJSONObject(i);
                                String timeDurationSts = jsonObject.getString("time_durationSts");
                                String upgrade_durationSts = jsonObject.getString("upgrade_durationSts");
                                if (Integer.parseInt(timeDurationSts) == 0 && Integer.parseInt(upgrade_durationSts) == 1) {
                                    // Call normal_fetchTimer() if timeDurationSts is 0 and upgrade_durationSts is 1
                                    normal_fetchTimer(id);
                                }
                                // Check if timeDurationSts == 1 and upgrade_durationSts == 0
                                else if (Integer.parseInt(timeDurationSts) == 1 && Integer.parseInt(upgrade_durationSts) == 0) {
                                    // Reset the timer and fetch the upgrade duration
                                    normal_timer.setText("00 hours 00 min 00 sec");
                                    normal_isTimerRunning = false;
                                    normal_Stopbtn.setEnabled(false);
                                    upgrade_remaining_duration(id);  // Use lowercase 'id' for consistency
                                }
                                // Check if both timeDurationSts and upgrade_durationSts are 1
                                else if (Integer.parseInt(timeDurationSts) == 1 && Integer.parseInt(upgrade_durationSts) == 1) {
                                    // Stop both timers and show upgrade layout
                                    normal_timer.setText("00 hours 00 min 00 sec");
                                    normal_isTimerRunning = false;
                                    normal_Stopbtn.setEnabled(false);
                                    upgrade_isTimerRunning = false;
                                    upgrade_timer.setText("00 hours 00 min 00 sec");
                                    upgrade_StopBtn.setEnabled(false);
                                    upgrade_layout.setVisibility(View.VISIBLE);
                                }
                                // Check if both timeDurationSts and upgrade_durationSts are 0
                                else if (Integer.parseInt(timeDurationSts) == 0 && Integer.parseInt(upgrade_durationSts) == 0) {
                                    // Fetch normal timer if both values are 0
                                    normal_fetchTimer(id);
                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MassageWorkingActivity.this, "Error parsing JSON", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error response
                        Toast.makeText(MassageWorkingActivity.this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Add the request to the RequestQueue
        requestQueue.add(jsonArrayRequest);
    }


    private void fetchTimer(String id) {
        String url = Constant.fetchTimer+id;
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // Create a new JsonArrayRequest to fetch the JSON response
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Process the JSON response
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = response.getJSONObject(i);
                                String workingStartTime = jsonObject.getString("working_startTime");
                                int totalTime = jsonObject.getInt("totalWorkingTime");
                                if ("00:00:00".equals(workingStartTime)) {
                                    SimpleDateFormat displayFormat = null;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        displayFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                                    }
                                    SimpleDateFormat uploadPutFormat = null;
                                    String uploadCurrentTime = null;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        uploadPutFormat = new SimpleDateFormat("HH:mm:", Locale.getDefault());
                                    }

                                    // Get current time
                                    Date currentTime = new Date();
                                    String formattedCurrentTime = null;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        formattedCurrentTime = displayFormat.format(currentTime);
                                    }
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        uploadCurrentTime = uploadPutFormat.format(currentTime); // 24-hour format
                                    }

                                    // Set current time to Start_Time TextView
                                    Start_Time.setText(formattedCurrentTime);

                                    // Parse `totalTime` to an integer for adding minutes


                                    // Use Calendar to add `totalTime` (in minutes) to `currentTime`
                                    Calendar calendar = null;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        calendar = Calendar.getInstance();
                                    }
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        calendar.setTime(currentTime);
                                    }
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        calendar.add(Calendar.MINUTE, totalTime);
                                    }

                                    // Format the new end time in 12-hour format
                                    String formattedEndTime = null;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        formattedEndTime = displayFormat.format(calendar.getTime());
                                    }

                                    // Set the calculated end time to End_Time TextView
                                    End_Time.setText(formattedEndTime);

                                    uploadTime(uploadCurrentTime,Id,"start_time");
                                }else{
                                    SimpleDateFormat inputFormat = null;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        inputFormat = new SimpleDateFormat("HH:mm:", Locale.getDefault());
                                    }
                                    SimpleDateFormat outputFormat = null;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        outputFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                                    }
                                    SimpleDateFormat displayFormat = null;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        displayFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                                    }

                                    try {
                                        // Parse workingStartTime from "HH:mm:ss" format to a Date object
                                        Date date = null; // parse "00:00:00" or any valid time
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            date = inputFormat.parse(workingStartTime);
                                        }

                                        // Format the date to 12-hour format for display as Start_Time
                                        String formattedTime = null; // convert to 12-hour format
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            formattedTime = outputFormat.format(date);
                                        }
                                        Start_Time.setText(formattedTime); // Set formatted time to Start_Time TextView

                                        // Initialize Calendar with the parsed date
                                        Calendar calendar = null;
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            calendar = Calendar.getInstance();
                                        }
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            calendar.setTime(date); // Set to the parsed start time
                                        }

                                        // Add `totalTime` (in minutes) to this time
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                            calendar.add(Calendar.MINUTE, totalTime);
                                        }

                                        // Format the calculated end time in 12-hour format with AM/PM
                                        String formattedEndTime = null;
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                            formattedEndTime = displayFormat.format(calendar.getTime());
                                        }
                                        End_Time.setText(formattedEndTime); // Set the calculated end time to End_Time TextView

                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                        // Optional: set a default fallback if parsing fails
                                        Start_Time.setText("Invalid Time");
                                    }
                                }



                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MassageWorkingActivity.this, "Error parsing JSON", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error response
                        Toast.makeText(MassageWorkingActivity.this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Add the request to the RequestQueue
        requestQueue.add(jsonArrayRequest);
    }

    private void uploadTime(String UploadCurrentTime,String id,String type) {
        String url = Constant.uploadTimer;

        // Make a GET request to the API using Volley
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Parse the JSON response
                            JSONObject jsonResponse = new JSONObject(response);

                            // Check the status from the response
                            String status = jsonResponse.getString("status");

                            if (status.equals("success")) {

                                String message = jsonResponse.getString("message");

                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            } else {
                                // If there was an error
                                String errorMessage = jsonResponse.getString("message");
                                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle any errors from the request
                        Toast.makeText(getApplicationContext(), "Failed to communicate with server", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Send the order ID and selected reason as POST parameters
                Map<String, String> params = new HashMap<>();
                params.put("working_startTime",UploadCurrentTime);
                params.put("orderId",id);
                params.put("Type",type);
                return params;
            }
        };

        // Add the request to the RequestQueue (make sure to initialize the RequestQueue if it's not)
        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void Normal_showBottomSheetForm() {
        // Inflate the bottom sheet view
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_form, null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();

        Spinner formSelection = bottomSheetView.findViewById(R.id.formSelection);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.selection_options, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        formSelection.setAdapter(adapter);

        ImageView closeButton = bottomSheetView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> {
            bottomSheetDialog.dismiss(); // Dismiss the bottom sheet on close button click
            normal_Stopbtn.setEnabled(true); // Enable the normal_Stopbtn after closing the bottom sheet
            isFormVisible = false; // Update form visibility status
        });

        // Fetch stop reason and liCount when the bottom sheet is opened
        fetchStopReasonAndUpdateOrder(Id, bottomSheetView);

        // Handle form submission
        Button formSubmitButton = bottomSheetView.findViewById(R.id.formSubmitButton);
        formSubmitButton.setOnClickListener(v -> {
            // Retrieve the selected value from the Spinner
            String selectedOption = formSelection.getSelectedItem().toString();

            // Retrieve the current timer value from the normal_timer TextView
            String currentTimerValue = normal_timer.getText().toString();

            // Create the reason string
            String selectedReason = "Normal Timer: "+selectedOption + ",( Timer Value: " + currentTimerValue + ")";

            // Access the liCount TextView
            TextView liCountTextView = bottomSheetView.findViewById(R.id.licount);

            // Parse the liCount value
            int liCount = Integer.parseInt(liCountTextView.getText().toString().split(" / ")[0]);

            // Check if liCount is less than 3
            if (liCount < 3) {
                // Call updateUIWithStopReason if liCount is less than 3
                updateUIWithStopReason(selectedReason, Id);

                // Close the bottom sheet after form submission
                bottomSheetDialog.dismiss();
                normal_Stopbtn.setEnabled(true); // Enable the button
                isFormVisible = false; // Update form visibility status

                // Optionally, call the stop timer function
                normal_stopTimer();
                pauseAudio();
            } else {
                // Show a message if liCount is 3 or more
                Toast.makeText(this, "You have reached the maximum limit of stop reasons (3).", Toast.LENGTH_SHORT).show();
            }
        });


        bottomSheetDialog.setOnDismissListener(dialog -> {
            normal_Stopbtn.setEnabled(true); // Enable the button after dismissing the bottom sheet
            isFormVisible = false; // Update form visibility status
        });

        isFormVisible = true; // Mark the form as visible
    }

    private void fetchStopReasonAndUpdateOrder(String id, View bottomSheetView) {
        // Construct the URL for the API request
        String url = Constant.fetch_stop_reason + id; // Add the query parameter for ID

        // Create a RequestQueue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // Create a StringRequest for GET method
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        // Parse the JSON response
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");

                        if ("success".equals(status)) {
                            int liCount = jsonObject.getInt("li_count");
                            TextView count = bottomSheetView.findViewById(R.id.licount); // Access licount from bottomSheetView

                            // Check if TextView is not null before setting text
                            if (count != null) {
                                count.setText(liCount + " / 3");
                            } else {
                                Log.e("MassageWorkingActivity", "licount TextView not found in bottom sheet");
                            }
                        } else {
                            // Handle the error case
                            String message = jsonObject.getString("message");
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Handle error
                    Toast.makeText(this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // Add the request to the RequestQueue
        requestQueue.add(stringRequest);
    }



    private void updateUIWithStopReason(String selectedReason, String id) {
        String url = Constant.submit_stop_reason;

        // Make a GET request to the API using Volley
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Parse the JSON response
                            JSONObject jsonResponse = new JSONObject(response);

                            // Check the status from the response
                            String status = jsonResponse.getString("status");

                            if (status.equals("success")) {

                                String message = jsonResponse.getString("message");

                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            } else {
                                // If there was an error
                                String errorMessage = jsonResponse.getString("message");
                                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle any errors from the request
                        Toast.makeText(getApplicationContext(), "Failed to communicate with server", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Send the order ID and selected reason as POST parameters
                Map<String, String> params = new HashMap<>();
                params.put("selected_reason", selectedReason);
                params.put("id", id);
                return params;
            }
        };

        // Add the request to the RequestQueue (make sure to initialize the RequestQueue if it's not)
        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void normal_fetchTimer(String id) {
        String url = Constant.working_table + id;
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    normal_populateTable(response);
                    normal_Stopbtn.performClick();
                    playAudio();// Perform button click after fetching data
                },
                error -> Toast.makeText(MassageWorkingActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show());

        requestQueue.add(jsonArrayRequest);
    }

    private void normal_populateTable(JSONArray data) {
        try {
            if (data.length() > 0) {
                JSONObject jsonObject = data.getJSONObject(0);
                if (jsonObject.has("total_duration")) {
                    int totalMinutes = Integer.parseInt(jsonObject.getString("total_duration"));
                    normal_remainingTimeInMillis = totalMinutes * 60 * 1000; // Convert total minutes to milliseconds

                    String formattedDuration = String.format(Locale.getDefault(), "%02d hours %02d min %02d sec",
                            totalMinutes / 60, totalMinutes % 60, 0);

                    // Set the formatted duration to the TextView
                    normal_timer.setText(formattedDuration);
                }
            }
        } catch (JSONException | NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(MassageWorkingActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
        }
    }

    private void normal_startTimer(long millis) {
        normal_countDownTimer = new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                normal_remainingTimeInMillis = millisUntilFinished;

                // Calculate hours, minutes, and seconds from remaining milliseconds
                int hours = (int) (millisUntilFinished / 1000) / 3600;
                int minutes = (int) ((millisUntilFinished / 1000) % 3600) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;

                // Update the TextView to show the countdown
                String timeFormatted = String.format(Locale.getDefault(), "%02d hours %02d min %02d sec", hours, minutes, seconds);
                normal_timer.setText(timeFormatted);
            }

            @Override
            public void onFinish() {
                // Set the timer to 00 hours 00 min 00 sec when the time is up
                updateNoramlStats(Id);
                normal_timer.setText("00 hours 00 min 00 sec");
                normal_isTimerRunning = false;
                saveTimerState();
                normal_Stopbtn.setEnabled(false);
                upgrade_layout.setVisibility(View.VISIBLE);
                upgrade_remaining_duration(Id);
            }
        }.start();

        normal_isTimerRunning = true;
        normal_Stopbtn.setText(R.string.Stop_time);
    }

    private void updateNoramlStats(String id) {
        // Your API URL (replace with the actual URL of your PHP API)
        String url =Constant.updateNoramlStats;
        // RequestQueue to handle network operations
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Parse the JSON response
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            String message = jsonResponse.getString("message");

                            if (status.equals("success")) {
                                // Handle successful update
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            } else {
                                // Handle error returned from the server
                                Toast.makeText(getApplicationContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error normal timer update", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error response
                        Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                // Send POST parameters
                Map<String, String> params = new HashMap<>();
                params.put("id", id);  // Sending 'id' parameter
                return params;
            }
        };

        // Add the request to the RequestQueue
        requestQueue.add(stringRequest);
    }


    private void saveTimerState() {
        SharedPreferences prefs = getSharedPreferences("TimerPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("remainingTime", normal_remainingTimeInMillis);
        editor.putBoolean("isTimerRunning", normal_isTimerRunning);
        editor.putBoolean("isButtonEnabled", normal_Stopbtn.isEnabled());
        editor.apply();
    }


    private void normal_stopTimer() {
        if (normal_countDownTimer != null) {
            normal_countDownTimer.cancel();
            normal_isTimerRunning = false;
            normal_Stopbtn.setText(R.string.start_time);
        }
    }










    private void Upgrade_showBottomSheetForm() {
        // Inflate the bottom sheet view
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_form, null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();

        Spinner formSelection = bottomSheetView.findViewById(R.id.formSelection);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.selection_options, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the Spinner
        formSelection.setAdapter(adapter);

        // Handle the close button functionality (manual close)
        ImageView closeButton = bottomSheetView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> {
            bottomSheetDialog.dismiss(); // Dismiss the bottom sheet on close button click
            upgrade_StopBtn.setEnabled(true); // Enable the normal_Stopbtn after closing the bottom sheet
            isFormVisible = false; // Update form visibility status
        });

        // Fetch stop reason and liCount when the bottom sheet is opened
        fetchStopReasonAndUpdateOrder(Id, bottomSheetView);

        // Handle form submission
        Button formSubmitButton = bottomSheetView.findViewById(R.id.formSubmitButton);
        formSubmitButton.setOnClickListener(v -> {

            String selectedOption = formSelection.getSelectedItem().toString();

            // Retrieve the current timer value from the normal_timer TextView
            String currentTimerValue = upgrade_timer.getText().toString();

            // Create the reason string
            String selectedReason = "Upgrade Timer: "+selectedOption + ",( Timer Value: " + currentTimerValue + ")";

            // Access the liCount TextView
            TextView liCountTextView = bottomSheetView.findViewById(R.id.licount);

            // Parse the liCount value
            int liCount = Integer.parseInt(liCountTextView.getText().toString().split(" / ")[0]);

            // Check if liCount is less than 3
            if (liCount < 3) {
                // Call updateUIWithStopReason if liCount is less than 3
                updateUIWithStopReason(selectedReason, Id);

                // Close the bottom sheet after form submission
                bottomSheetDialog.dismiss();
                upgrade_StopBtn.setEnabled(true); // Enable the button
                isFormVisible = false; // Update form visibility status

                // Optionally, call the stop timer function
                Upgrade_stopTimer();
                pauseAudio();
            } else {
                // Show a message if liCount is 3 or more
                Toast.makeText(this, "You have reached the maximum limit of stop reasons (3).", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle dismissing the bottom sheet (clicking outside)
        bottomSheetDialog.setOnDismissListener(dialog -> {
            upgrade_StopBtn.setEnabled(true); // Enable the button after dismissing the bottom sheet
            isFormVisible = false; // Update form visibility status
        });

        isFormVisible = true; // Mark the form as visible
    }




    private void upgrade_remaining_duration(String id) {
        String url = Constant.working_upggrade + id;
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // Fetch the JSON data from the API
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    upgrade_populateTable(response);  // Populate the table with the response data
                    upgrade_StopBtn.performClick();
                    playAudio();// Perform button click after fetching data
                },
                error -> Toast.makeText(MassageWorkingActivity.this, "Error fetching upgrade", Toast.LENGTH_SHORT).show());

        requestQueue.add(jsonArrayRequest);  // Add request to the queue
    }

    private void upgrade_populateTable(JSONArray data) {
        try {
            if (data.length() > 0) {
                JSONObject jsonObject = data.getJSONObject(0);  // Get the first object from the array

                if (jsonObject.has("upgrade_duration") && jsonObject.has("upgrade_durationSts")) {
                    // Fetch both values
                    int upgradeDuration = Integer.parseInt(jsonObject.getString("upgrade_duration"));
                    int upgradeDurationSts = Integer.parseInt(jsonObject.getString("upgrade_durationSts"));

                    if (upgradeDuration == 0 && upgradeDurationSts == 1) {

                        normal_timer.setText("00 hours 00 min 00 sec");
                        normal_isTimerRunning = false;
                        normal_Stopbtn.setEnabled(false);

                        upgrade_isTimerRunning = false;
                        upgrade_StopBtn.setEnabled(false);
                        upgrade_layout.setVisibility(View.GONE);
                    } else {

                        int totalMinutes = upgradeDuration;  // Assuming upgrade_duration is in minutes
                        upgrade_remainingTimeInMillis = totalMinutes * 60 * 1000;  // Convert minutes to milliseconds
                        String formattedDuration = String.format(Locale.getDefault(), "%02d hours %02d min %02d sec",
                                totalMinutes / 60, totalMinutes % 60, 0);
                        upgrade_timer.setText(formattedDuration);
                    }
                } else {
                    Toast.makeText(MassageWorkingActivity.this, "Required fields not found in response", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (JSONException | NumberFormatException e) {
            // Handle any errors that occur during JSON parsing or number formatting
            e.printStackTrace();
            Toast.makeText(MassageWorkingActivity.this, "Error starting upgrade", Toast.LENGTH_SHORT).show();
        }
    }

    private void upgrade_startTimer(long upgradeRemainingTimeInMillis) {
        upgrade_countDownTimer = new CountDownTimer(upgradeRemainingTimeInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                upgrade_remainingTimeInMillis = millisUntilFinished;
                upgrade_layout.setVisibility(View.VISIBLE);
                // Calculate hours, minutes, and seconds from remaining milliseconds
                int hours = (int) (millisUntilFinished / 1000) / 3600;
                int minutes = (int) ((millisUntilFinished / 1000) % 3600) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;

                // Update the TextView to show the countdown
                String timeFormatted = String.format(Locale.getDefault(), "%02d hours %02d min %02d sec", hours, minutes, seconds);
                upgrade_timer.setText(timeFormatted);
            }

            @Override
            public void onFinish() {
                normal_timer.setText("00 hours 00 min 00 sec");
                normal_isTimerRunning = false;
                // Set the timer to 00 hours 00 min 00 sec when the time is up
                updateUpgradeStats(Id);
                upgrade_timer.setText("00 hours 00 min 00 sec");
                upgrade_isTimerRunning = false;
                saveUpgradeTimerState();
                upgrade_StopBtn.setEnabled(false);
                upgrade_layout.setVisibility(View.VISIBLE);
            }
        }.start();

        upgrade_isTimerRunning = true;
        upgrade_StopBtn.setText(R.string.Stop_time);

    }

    private void saveUpgradeTimerState() {
        SharedPreferences prefs = getSharedPreferences("TimerPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("remainingTime", upgrade_remainingTimeInMillis);
        editor.putBoolean("isTimerRunning", upgrade_isTimerRunning);
        editor.putBoolean("isButtonEnabled", upgrade_StopBtn.isEnabled());
        editor.apply();
    }

    private void updateUpgradeStats(String id) {
        String url =Constant.updateUpgradeStats;
        // RequestQueue to handle network operations
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Parse the JSON response
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            String message = jsonResponse.getString("message");

                            if (status.equals("success")) {
                                // Handle successful update
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            } else {
                                // Handle error returned from the server
                                Toast.makeText(getApplicationContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error response
                        Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                // Send POST parameters
                Map<String, String> params = new HashMap<>();
                params.put("id", id);  // Sending 'id' parameter
                return params;
            }
        };

        // Add the request to the RequestQueue
        requestQueue.add(stringRequest);

    }


    private void Upgrade_stopTimer() {
        if (upgrade_countDownTimer != null) {
            upgrade_countDownTimer.cancel();
            upgrade_isTimerRunning = false;
            upgrade_StopBtn.setText(R.string.start_time);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Call the function to set the End Time when coming back from UpgradeActivity
//       updateEndTime(Id);
        fetchTimer(Id);
    }

    private void updateEndTime(String id) {
        String url = Constant.EndTime + id;
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // Create a new JsonObjectRequest to fetch the JSON response
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Process the JSON response
                        try {
                            // Check if the status is 'success'
                            if (response.getString("status").equals("success")) {
                                String end_time = response.getString("end_time");
                                End_Time.setText(end_time);  // Update the TextView
                            } else {
                                Toast.makeText(MassageWorkingActivity.this, "No booking found", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MassageWorkingActivity.this, "Error parsing JSON", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error response
                        Toast.makeText(MassageWorkingActivity.this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }


    @Override
    public void onBackPressed() {

            super.onBackPressed();

    }


    @Override
    protected void onStop() {
        super.onStop();
        // Call your function to store remaining time when the user navigates away (home button, etc.)
    }

    private void setupViewPager(List<String> imageUrls) {
        viewPager = findViewById(R.id.viewPager);
        adapter = new CustomImageAdapter(MassageWorkingActivity.this, imageUrls); // Pass the activity context
        viewPager.setAdapter(adapter);

        handler.postDelayed(new Runnable() {
            public void run() {
                if (adapter.getItemCount() == currentPage) {
                    currentPage = 0;
                }
                viewPager.setCurrentItem(currentPage++, true);
                handler.postDelayed(this, delay);
            }
        }, delay);
    }


    private void initializeMediaPlayer() {
        mediaPlayer = MediaPlayer.create(this, R.raw.jiift_3hrs);
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true); // Enable looping
            mediaPlayer.start(); // Start playback
        }
    }


    private void playAudio() {
        // Check if both buttons are disabled
        if (!normal_Stopbtn.isEnabled() && !upgrade_StopBtn.isEnabled()) {

            return;
        }

        // If either button is enabled, proceed with playing audio
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
            playPauseButton.setImageResource(R.drawable.ic_pause);
            isPlaying = true;
        }
    }


    private void pauseAudio() {
        mediaPlayer.pause();
        playPauseButton.setImageResource(R.drawable.ic_play_arrow);
        isPlaying = false;
    }

    private void resetPlayer() {
        mediaPlayer.seekTo(0);
        playPauseButton.setImageResource(R.drawable.ic_play_arrow);
        isPlaying = false;
    }

    private void toggleMute() {
        if (isMuted) {
            mediaPlayer.setVolume(1.0f, 1.0f);
            muteButton.setImageResource(R.drawable.volume_up);
            isMuted = false;
        } else {
            mediaPlayer.setVolume(0.0f, 0.0f);
            muteButton.setImageResource(R.drawable.ic_volume_off);
            isMuted = true;
        }
    }

    private void fetchImages() {
        String url =Constant.fetchSliders; // Replace with your PHP script URL

        // Create a RequestQueue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // Create a JsonArrayRequest
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        List<String> imageUrls = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                imageUrls.add(response.getString(i));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        setupViewPager(imageUrls);  // Pass image URLs to the setupViewPager method
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                        error.printStackTrace();
                    }
                });

        // Add the request to the RequestQueue
        requestQueue.add(jsonArrayRequest);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacksAndMessages(null);
    }

}
