package com.company.jifiit.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.company.jifiit.Constant;
import com.company.jifiit.R;

import com.company.jifiit.TrainingHub.VideoPlayerActivity;
import com.company.jifiit.model.TrainingModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainingAdapter extends RecyclerView.Adapter<TrainingAdapter.TrainingViewHolder> {

    private Context context;
    private List<TrainingModel> trainingList;

    public TrainingAdapter(Context context, List<TrainingModel> trainingList) {
        this.context = context;
        this.trainingList = trainingList;
    }

    @NonNull
    @Override
    public TrainingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_video, parent, false);
        return new TrainingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrainingViewHolder holder, int position) {
        TrainingModel trainingModel = trainingList.get(position);

        holder.description.setText(trainingModel.getTitle());
        holder.uploadedDate.setText("• "+trainingModel.getUploaded_date());

        fetchingViews(trainingModel.getId(), holder);
        SharedPreferences sp = context.getSharedPreferences("credentials", Context.MODE_PRIVATE);
        String idString = sp.getString("Id", "");
        BigInteger userId = new BigInteger(idString);

        String fileType = trainingModel.getFileType();

        // Get layout parameters
        ViewGroup.LayoutParams layoutParams = holder.thumbnail.getLayoutParams();

        if ("Video".equals(fileType)) {
            // Fixed height for videos (160dp)
            int heightInPx = (int) (160 * context.getResources().getDisplayMetrics().density);
            layoutParams.height = heightInPx;
            holder.thumbnail.setLayoutParams(layoutParams);

            holder.videoNo.setText("No."+trainingModel.getIndex()+":");
            holder.videoNo.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load(Constant.thumbnail + trainingModel.getThumbnail())
                    .into(holder.thumbnail);

            holder.thumbnail.setOnClickListener(v -> {
                sendViewDataToServer(trainingModel.getId(), userId);
                Intent intent = new Intent(context, VideoPlayerActivity.class);
                intent.putExtra("video_path", Constant.trainingVideo + trainingModel.getVideo_path());
                intent.putExtra("id", trainingModel.getId());
                intent.putExtra("Content", trainingModel.getDescription());
                intent.putExtra("title", trainingModel.getTitle());
                intent.putExtra("Date", trainingModel.getUploaded_date());
                context.startActivity(intent);
            });
        } else if ("Photo".equals(fileType)) {
            // Wrap content for photos (dynamic height)
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            holder.thumbnail.setLayoutParams(layoutParams);
            holder.thumbnail.setAdjustViewBounds(true);

            holder.videoNo.setVisibility(View.GONE);

            Glide.with(context)
                    .load(Constant.training_photos + trainingModel.getThumbnail())
                    .into(holder.thumbnail);

            holder.thumbnail.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Rect scrollBounds = new Rect();
                    ((Activity) context).findViewById(android.R.id.content).getHitRect(scrollBounds);
                    if (holder.thumbnail.getLocalVisibleRect(scrollBounds)) {
                        sendViewDataToServer(trainingModel.getId(), userId);
                        holder.thumbnail.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
    }

//    private void countImpression(String id) {
//        StringRequest request = new StringRequest(Request.Method.POST, Constant.impressionUrl,
//                response -> {
//                    Log.d("Impression", "Counted for ID: " + id);
//                },
//                error -> {
//                    Log.e("Impression", "Error: " + error.getMessage());
//                }) {
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<>();
//                params.put("training_id", id.toString());
//                return params;
//            }
//        };
//
//        RequestQueue queue = Volley.newRequestQueue(context);
//        queue.add(request);
//    }

    private void sendViewDataToServer(String id, BigInteger userId) {
        String url = Constant.VideInsert;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                },
                error -> {
                    // Handle the error
                    Log.d("Error", error.toString());
                    Toast.makeText(context, "Error occurred", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("video_id", String.valueOf(id));
                params.put("therapist_id", String.valueOf(userId));
                return params;
            }
        };

        Volley.newRequestQueue(context).add(stringRequest);
    }

    private void fetchingViews(String id, TrainingViewHolder holder) {
        String url = Constant.likeDislikViews + id;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {
                    try {
                        if (response.has("error")) {
                            String error = response.getString("error");
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                            Log.d("fetchingViews", "Error in response: " + error);
                        } else {
                            int v_id_count = response.getInt("v_id_count");
                            holder.views.setText("• " + v_id_count + " views");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("fetchingViews", "JSONException: " + e.getMessage());
                    }
                }, error -> {
                    Toast.makeText(context, "Error occurred", Toast.LENGTH_SHORT).show();
                    Log.d("fetchingViews", "Volley Error: " + error.getMessage());
                });

        Volley.newRequestQueue(context).add(jsonObjectRequest);
    }



    @Override
    public int getItemCount() {
        return trainingList.size();
    }

    public static class TrainingViewHolder extends RecyclerView.ViewHolder {

        ImageView thumbnail;
        TextView views,videoNo, likes, dislikes, uploadedDate, description;
        ImageButton likesBtn , DislikeBtn;

        public TrainingViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            description = itemView.findViewById(R.id.description);
            videoNo = itemView.findViewById(R.id.Id);
            views = itemView.findViewById(R.id.view);
            uploadedDate = itemView.findViewById(R.id.Date);
            likesBtn = itemView.findViewById(R.id.likeBtn);
//            DislikeBtn = itemView.findViewById(R.id.disLikeBtn);
        }
    }
}
