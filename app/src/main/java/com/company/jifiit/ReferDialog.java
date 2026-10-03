package com.company.jifiit;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.FileProvider;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Referral.TermsAndConditions;
import org.json.JSONException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;

public class ReferDialog {
    private static AlertDialog dialog;
    private static Handler handler = new Handler();
    private static int secondsRemaining;
    private static String phone = "Loading...", name = "Loading...";
    private static String referralCode = "Loading...";

    interface FetchDataCallback {
        void onDataFetched();
    }

    public static void showReferDialog(Context context) {
        secondsRemaining = 5;

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomWidthDialog);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_refer, null);

        SharedPreferences sp = context.getSharedPreferences("credentials", Context.MODE_PRIVATE);
        String idString = sp.getString("Id", "");

        TextView tvTimer = view.findViewById(R.id.tvTimer);
        TextView tvTerm = view.findViewById(R.id.tvTerm);
        ImageView ivClose = view.findViewById(R.id.ivClose);
        Button inviteBtn = view.findViewById(R.id.btnInvite);

        if (tvTerm != null) {
            tvTerm.setOnClickListener(v -> {
                Intent intent = new Intent(context, TermsAndConditions.class);
                context.startActivity(intent);
            });
        }

        builder.setView(view);
        builder.setCancelable(false);

        dialog = builder.create();

        // Set window properties with margins
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Calculate margin size (e.g., 32dp)
            int margin = (int) (context.getResources().getDisplayMetrics().density * 12); // you can adjust this value

            // Set left and right padding only
            dialog.getWindow().getDecorView().setPadding(margin, 0, margin, 0);
        }


        dialog.show();

        ivClose.setVisibility(View.GONE);

        if (!idString.isEmpty()) {
            BigInteger id = new BigInteger(idString);
            fetchData(context, id, new FetchDataCallback() {
                @Override
                public void onDataFetched() {
                    Log.d("ReferDialog", "Data fetched - Phone: " + phone + ", Code: " + referralCode);
                }
            });
        }

        inviteBtn.setOnClickListener(v -> generateAndShareImage(context));
        ivClose.setOnClickListener(v -> dismissDialog());

        final Runnable countdown = new Runnable() {
            @Override
            public void run() {
                if (secondsRemaining > 0) {
                    tvTimer.setText(String.valueOf(secondsRemaining));
                    secondsRemaining--;
                    handler.postDelayed(this, 1000);
                } else {
                    tvTimer.setVisibility(View.GONE);
                    ivClose.setVisibility(View.VISIBLE);
                }
            }
        };



        handler.post(countdown);
    }

    public static void fetchData(Context context, BigInteger id, FetchDataCallback callback) {
        String url = Constant.referralRequest + id;
        Log.d("ReferDialog", "Fetching data from: " + url);

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        Log.d("ReferDialog", "Response: " + response.toString());

                        if (response.has("phone")) {
                            phone = response.getString("phone");
                        } else if (response.has("Phone")) {
                            phone = response.getString("Phone");
                        } else if (response.has("mobile")) {
                            phone = response.getString("mobile");
                        } else {
                            phone = "Not Available";
                        }

                        if (response.has("referral_code")) {
                            referralCode = response.getString("referral_code");
                        } else if (response.has("referralCode")) {
                            referralCode = response.getString("referralCode");
                        } else if (response.has("code")) {
                            referralCode = response.getString("code");
                        } else {
                            referralCode = "N/A";
                        }

                        if (response.has("name")) {
                            name = response.getString("name");
                        }

                        Log.d("ReferDialog", "Set values - Phone: " + phone + ", Code: " + referralCode);
                        callback.onDataFetched();
                    } catch (JSONException e) {
                        Log.e("ReferDialog", "JSON parsing error", e);
                        phone = "Error";
                        referralCode = "Error";
                        callback.onDataFetched();
                    }
                },
                error -> {
                    Log.e("ReferDialog", "Network error: " + error.getMessage());
                    phone = "Network Error";
                    referralCode = "Network Error";
                    callback.onDataFetched();
                }
        );

        queue.add(jsonObjectRequest);
    }

    public static void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        handler.removeCallbacksAndMessages(null);
    }

    @SuppressLint({"MissingInflatedId", "LocalSuppress"})
    private static void generateAndShareImage(Context context) {
        View cardView = LayoutInflater.from(context).inflate(R.layout.job_vacancy_card, null);
        TextView referralCodeView = cardView.findViewById(R.id.referral_code);
        referralCodeView.setText("Use Referral Code:- " + referralCode);

        TextView phoneView = cardView.findViewById(R.id.phone_number);
        phoneView.setText("Hii, This is " + name + "\nCall me for the job information and join Easily,\n Contact on:- " + phone);

        cardView.measure(
                View.MeasureSpec.makeMeasureSpec(1240, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(1754, View.MeasureSpec.EXACTLY)
        );
        cardView.layout(0, 0, 1240, 1754);

        Bitmap bitmap = Bitmap.createBitmap(1240, 1754, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        cardView.draw(canvas);

        try {
            File imageFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "jiifit_job_opportunity.png");
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            Uri imageUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", imageFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Hii, This is " + name + "\nCheck out this job opportunity \nJoin as a massage therapist today \n" +
                    "\uD83E\uDD29\nHere is the app link download\n\uD83D\uDD17 https://play.google.com/store/apps/details?id=com.company.jifiit&pcampaignid=web_share\nDownload theJiifit therapist app\nAnd\nUse Referral code: " + referralCode + "\nContact with me for more information\nCall " + phone);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(shareIntent, "Share Job Opportunity"));
        } catch (IOException e) {
            Log.e("ReferDialog", "Error sharing image", e);
        }
    }
}