package com.company.jifiit.TrainingHub;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Adapter.LeaveAdapter;
import com.company.jifiit.Adapter.TrainingAdapter;
import com.company.jifiit.Constant;
import com.company.jifiit.R;
import com.company.jifiit.ReferDialog;
import com.company.jifiit.model.LeaveModel;
import com.company.jifiit.model.TrainingModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.TimeZone;


public class TrainingHubActivity extends AppCompatActivity {

    RecyclerView VideoList;
    TrainingAdapter trainingAdapter;
    ArrayList<TrainingModel> trainingModels;
    String timeZone = java.util.TimeZone.getDefault().getID();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_training_hub);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        ReferDialog.showReferDialog(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Training Hub");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Set toolbar color and back button color
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        toolbar.getNavigationIcon().setTint(getResources().getColor(android.R.color.black));


    }

    private void fetchVideos() {

        // Initialize RecyclerView
        VideoList = findViewById(R.id.VideoList);
        VideoList.setLayoutManager(new LinearLayoutManager(this));
        trainingModels = new ArrayList<>();
        trainingAdapter = new TrainingAdapter(this, trainingModels);
        VideoList.setAdapter(trainingAdapter);

        // Show loading indicator
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading videos...");
        progressDialog.setCancelable(false);
        progressDialog.hide();

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = Constant.fetchTrainingDetails+"?timezone=" +timeZone;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    progressDialog.dismiss();
                    try {
                        // Extract the 'data' array from the response
                        JSONArray dataArray = response.getJSONArray("data");

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject jsonObject = dataArray.getJSONObject(i);

                            // Validate and handle null values
                            String id = jsonObject.optString("id", "");
                            String thumbnail = jsonObject.optString("thumbnail", "");
                            String uploaded_date = jsonObject.optString("time_ago", "Date not available");
                            String description = jsonObject.optString("description", "");
                            String title = jsonObject.optString("title", "Untitled");
                            String videoPath = jsonObject.optString("video_path", "");
                            String fileType = jsonObject.optString("file_type", "");
                            int index = jsonObject.optInt("index", 0);

                            // Only add valid items
                            if (!thumbnail.isEmpty() && (!videoPath.isEmpty() || fileType.equals("Photo"))) {
                                TrainingModel trainingModel = new TrainingModel(
                                        id,
                                        thumbnail,
                                        description,
                                        uploaded_date,
                                        videoPath,
                                        title,
                                        fileType,
                                        index
                                );
                                trainingModels.add(trainingModel);
                            }
                        }

                        if (trainingModels.isEmpty()) {
//                        showEmptyState();
                        } else {
                            trainingAdapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        Log.e("JSON Parsing", "Error parsing JSON", e);
                        Toast.makeText(this, "Error parsing video data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Log.e("Volley Error", "Error fetching videos", error);

                    if (trainingModels.isEmpty()) {
//                    showEmptyState();
                    }

                    if (error instanceof TimeoutError) {
                        Toast.makeText(this, "Request timed out. Please try again", Toast.LENGTH_SHORT).show();
                    } else if (error instanceof NoConnectionError) {
                        Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error loading videos", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Set retry policy
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000, // 10 seconds timeout
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(jsonObjectRequest);
    }


//    private void showEmptyState() {
//        // You can show a TextView or ImageView indicating no videos available
//        TextView emptyView = findViewById(R.id.empty_view); // Make sure you have this in your layout
//        if (emptyView != null) {
//            emptyView.setVisibility(View.VISIBLE);
//            VideoList.setVisibility(View.GONE);
//            emptyView.setText("No videos available");
//        }
//    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        fetchVideos();

        super.onResume();
    }
}