package com.company.jifiit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Hotel.HotelRegistraion;
import com.company.jifiit.Referral.HelpActivity;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class JoinerTypes extends AppCompatActivity {

    // =========================================================
    // JOINER TYPE BUTTONS
    // =========================================================

    private MaterialButton btnHotelSelect;
    private MaterialButton btnWellnessSelect;
    private MaterialButton btnCentreSelect;
    private MaterialButton btnExpertApply,btnWhatsApp,btnCall;


    // =========================================================
    // FAQ VIEWS
    // =========================================================

    private LinearLayout categoryContainer;
    private LinearLayout faqContainer;

    private TextView tvFaqTitle;
    private TextView tvFaqCount;




    // =========================================================
    // FAQ DATA
    // =========================================================

    private final ArrayList<FaqCategory> faqCategories = new ArrayList<>();


    // =========================================================
    // SELECTED CATEGORY
    // =========================================================

    // -1 = All Questions
    // otherwise category index
    private int selectedCategoryIndex = -1;


    // =========================================================
    // API URL
    // =========================================================



    // =========================================================
    // ACTIVITY CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
// EDGE TO EDGE
// =========================

        EdgeToEdge.enable(this);

// =========================
// SYSTEM BAR
// =========================

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(
                        getWindow(),
                        getWindow().getDecorView()
                );

        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);

        setContentView(R.layout.activity_joiner_types);

        initializeViews();

        setupClickListeners();

        loadFaqsFromServer();
    }


    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initializeViews() {

        // -----------------------------------------------------
        // Joiner buttons
        // -----------------------------------------------------

        btnHotelSelect = findViewById(R.id.tvHotelSelect);
        btnWellnessSelect = findViewById(R.id.tvWellnessSelect);
        btnCentreSelect = findViewById(R.id.tvCentreSelect);
        btnExpertApply = findViewById(R.id.btnExpertApply);

        btnCall = findViewById(R.id.btnCall);
        btnWhatsApp = findViewById(R.id.btnWhatsApp);

        // -----------------------------------------------------
        // Dynamic FAQ containers
        // -----------------------------------------------------

        categoryContainer = findViewById(R.id.categoryContainer);

        faqContainer = findViewById(R.id.faqContainer);


        // -----------------------------------------------------
        // FAQ title and count
        // -----------------------------------------------------

        tvFaqTitle = findViewById(R.id.tvFaqTitle);

        tvFaqCount = findViewById(R.id.tvFaqCount);
    }


    // =========================================================
    // JOINER BUTTON CLICK LISTENERS
    // =========================================================

    private void setupClickListeners() {

        // Hotel
        if (btnHotelSelect != null) {

            btnHotelSelect.setOnClickListener(v -> {

                startActivity(new Intent(JoinerTypes.this, HotelRegistraion.class ));
                overridePendingTransition(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                );

            });
        }


        // Wellness
        if (btnWellnessSelect != null) {

            btnWellnessSelect.setOnClickListener(v -> {

                // Open Wellness Partner registration here

            });
        }


        // Centre
        if (btnCentreSelect != null) {

            btnCentreSelect.setOnClickListener(v -> {

                // Open Centre Partner registration here

            });
        }


        // Expert
        if (btnExpertApply != null) {

            btnExpertApply.setOnClickListener(v -> {

                startActivity(new Intent(JoinerTypes.this, Register.class ));
                overridePendingTransition(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                );

            });
        }

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String customerPhoneNumber = "+919717550414";

                // Lookup the actual phone number in the mapping

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + customerPhoneNumber));

                // Check for permission before making the call
                if (ContextCompat.checkSelfPermission(JoinerTypes.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // Request the permission if it is not granted
                    ActivityCompat.requestPermissions(JoinerTypes.this, new String[]{Manifest.permission.CALL_PHONE}, 1);
                    return;
                }

                // Start the call if permission is granted
                startActivity(callIntent);
            }
        });



        btnWhatsApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String wpUrl = "https://wa.me/+919717550414";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(wpUrl));
                startActivity(intent);

            }
        });
    }


    // =========================================================
    // LOAD FAQ FROM PHP
    // =========================================================

    private void loadFaqsFromServer() {

        StringRequest request = new StringRequest(
                Request.Method.GET,
                Constant.FAQ_API_URL,

                response -> {

                    parseFaqResponse(response);

                },

                error -> {

                    error.printStackTrace();

                    // Optional:
                    // Show error message here

                }
        );


        RequestQueue requestQueue =
                Volley.newRequestQueue(this);

        requestQueue.add(request);
    }


    // =========================================================
    // PARSE PHP JSON RESPONSE
    // =========================================================

    private void parseFaqResponse(String response) {

        try {

            JSONObject jsonObject =
                    new JSONObject(response);


            String status =
                    jsonObject.optString("status");


            if (!status.equalsIgnoreCase("success")) {

                return;
            }


            JSONArray categoriesArray =
                    jsonObject.optJSONArray("categories");


            if (categoriesArray == null) {

                return;
            }


            // Clear old data
            faqCategories.clear();


            // -------------------------------------------------
            // Read categories
            // -------------------------------------------------

            for (int i = 0;
                 i < categoriesArray.length();
                 i++) {

                JSONObject categoryObject =
                        categoriesArray.getJSONObject(i);


                int categoryId =
                        categoryObject.optInt("id");


                String categoryName =
                        categoryObject.optString("name");


                JSONArray questionsArray =
                        categoryObject.optJSONArray("questions");


                ArrayList<FaqItem> questions =
                        new ArrayList<>();


                if (questionsArray != null) {

                    for (int j = 0;
                         j < questionsArray.length();
                         j++) {

                        JSONObject questionObject =
                                questionsArray.getJSONObject(j);


                        int questionId =
                                questionObject.optInt("id");


                        String question =
                                questionObject.optString(
                                        "question"
                                );


                        String answer =
                                questionObject.optString(
                                        "answer"
                                );


                        questions.add(
                                new FaqItem(
                                        questionId,
                                        question,
                                        answer
                                )
                        );
                    }
                }


                // Only add category if it contains questions
                if (!questions.isEmpty()) {

                    faqCategories.add(
                            new FaqCategory(
                                    categoryId,
                                    categoryName,
                                    questions
                            )
                    );
                }
            }


            // -------------------------------------------------
            // Build UI
            // -------------------------------------------------

            createCategoryChips();


            // Initially show ALL QUESTIONS
            selectedCategoryIndex = -1;

            updateCategorySelection();

            showAllQuestions();


        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // =========================================================
    // CREATE CATEGORY CHIPS
    // =========================================================

    private void createCategoryChips() {

        categoryContainer.removeAllViews();


        // =====================================================
        // ALL QUESTIONS CHIP
        // =====================================================

        TextView allChip =
                createCategoryChip(
                        "All Questions"
                );


        allChip.setOnClickListener(v -> {

            if (selectedCategoryIndex == -1) {
                return;
            }

            selectedCategoryIndex = -1;

            updateCategorySelection();

            showAllQuestions();

        });


        categoryContainer.addView(allChip);


        // =====================================================
        // DYNAMIC CATEGORY CHIPS
        // =====================================================

        for (int i = 0;
             i < faqCategories.size();
             i++) {

            final int categoryIndex = i;


            FaqCategory category =
                    faqCategories.get(i);


            TextView categoryChip =
                    createCategoryChip(
                            category.name
                    );


            categoryChip.setOnClickListener(v -> {

                if (selectedCategoryIndex ==
                        categoryIndex) {

                    return;
                }


                selectedCategoryIndex =
                        categoryIndex;


                updateCategorySelection();

                showCategoryQuestions(
                        categoryIndex
                );

            });


            categoryContainer.addView(
                    categoryChip
            );
        }
    }


    // =========================================================
    // CREATE SINGLE CATEGORY CHIP
    // =========================================================

    private TextView createCategoryChip(
            String text
    ) {

        View view =
                LayoutInflater.from(this)
                        .inflate(
                                R.layout.item_faq_category,
                                categoryContainer,
                                false
                        );


        TextView chip =
                view.findViewById(
                        R.id.tvCategory
                );


        chip.setText(text);


        return chip;
    }


    // =========================================================
    // UPDATE CHIP SELECTED STATE
    // =========================================================

    private void updateCategorySelection() {

        int childCount =
                categoryContainer.getChildCount();


        for (int i = 0;
             i < childCount;
             i++) {

            View child =
                    categoryContainer.getChildAt(i);


            if (!(child instanceof TextView)) {
                continue;
            }


            TextView chip =
                    (TextView) child;


            boolean selected;


            // First child = All Questions
            if (i == 0) {

                selected =
                        selectedCategoryIndex == -1;

            } else {

                selected =
                        selectedCategoryIndex ==
                                (i - 1);
            }


            updateChipAppearance(
                    chip,
                    selected
            );
        }
    }


    // =========================================================
    // UPDATE CHIP APPEARANCE
    // =========================================================

    private void updateChipAppearance(
            TextView chip,
            boolean selected
    ) {

        if (selected) {

            chip.setBackgroundResource(
                    R.drawable.bg_faq_chip_selected
            );


            chip.setTextColor(
                    Color.WHITE
            );


            chip.setTypeface(
                    null,
                    Typeface.BOLD
            );

        } else {

            chip.setBackgroundResource(
                    R.drawable.bg_faq_chip
            );


            chip.setTextColor(
                    ContextCompat.getColor(
                            this,
                            R.color.faq_text_primary
                    )
            );


            chip.setTypeface(
                    null,
                    Typeface.NORMAL
            );
        }
    }


    // =========================================================
    // SHOW ALL QUESTIONS
    // =========================================================

    private void showAllQuestions() {

        faqContainer.removeAllViews();


        int totalQuestions = 0;


        for (FaqCategory category :
                faqCategories) {


            for (FaqItem faq :
                    category.questions) {


                addFaqItem(faq);

                totalQuestions++;
            }
        }


        // Header
        tvFaqTitle.setText(
                "ALL QUESTIONS"
        );


        // Count
        setFaqCount(totalQuestions);
    }


    // =========================================================
    // SHOW SELECTED CATEGORY QUESTIONS
    // =========================================================

    private void showCategoryQuestions(
            int categoryIndex
    ) {

        faqContainer.removeAllViews();


        if (categoryIndex < 0 ||
                categoryIndex >=
                        faqCategories.size()) {

            showAllQuestions();

            return;
        }


        FaqCategory category =
                faqCategories.get(
                        categoryIndex
                );


        // -----------------------------------------------------
        // Header
        // -----------------------------------------------------

        tvFaqTitle.setText(
                category.name.toUpperCase()
        );


        // -----------------------------------------------------
        // Count
        // -----------------------------------------------------

        setFaqCount(
                category.questions.size()
        );


        // -----------------------------------------------------
        // Questions
        // -----------------------------------------------------

        for (FaqItem faq :
                category.questions) {

            addFaqItem(faq);
        }
    }


    // =========================================================
    // ADD SINGLE FAQ CARD
    // =========================================================

    private void addFaqItem(
            FaqItem faq
    ) {

        View view =
                LayoutInflater.from(this)
                        .inflate(
                                R.layout.item_faq,
                                faqContainer,
                                false
                        );


        // -----------------------------------------------------
        // Views
        // -----------------------------------------------------

        LinearLayout faqHeader =
                view.findViewById(
                        R.id.faqHeader
                );


        TextView tvQuestion =
                view.findViewById(
                        R.id.tvQuestion
                );


        TextView tvAnswer =
                view.findViewById(
                        R.id.tvAnswer
                );


        ImageView ivArrow =
                view.findViewById(
                        R.id.ivArrow
                );


        // -----------------------------------------------------
        // Set data
        // -----------------------------------------------------

        tvQuestion.setText(
                faq.question
        );


        tvAnswer.setText(
                faq.answer
        );


        // -----------------------------------------------------
        // Initial state
        // -----------------------------------------------------

        tvAnswer.setVisibility(
                View.GONE
        );


        ivArrow.setImageResource(
                R.drawable.arrow_down
        );


        // -----------------------------------------------------
        // Click
        // -----------------------------------------------------

        faqHeader.setOnClickListener(v -> {

            if (tvAnswer.getVisibility()
                    == View.VISIBLE) {


                // ---------------------------------------------
                // COLLAPSE
                // ---------------------------------------------

                tvAnswer.setVisibility(
                        View.GONE
                );


                ivArrow.setImageResource(
                        R.drawable.arrow_down
                );


            } else {


                // ---------------------------------------------
                // EXPAND
                // ---------------------------------------------

                tvAnswer.setVisibility(
                        View.VISIBLE
                );


                ivArrow.setImageResource(
                        R.drawable.arrow_up
                );
            }

        });


        // -----------------------------------------------------
        // Add to container
        // -----------------------------------------------------

        faqContainer.addView(
                view
        );
    }


    // =========================================================
    // FAQ COUNT
    // =========================================================

    private void setFaqCount(
            int count
    ) {

        if (count == 1) {

            tvFaqCount.setText(
                    "1 Article"
            );

        } else {

            tvFaqCount.setText(
                    count + " Articles"
            );
        }
    }


    // =========================================================
    // FAQ CATEGORY MODEL
    // =========================================================

    private static class FaqCategory {

        int id;

        String name;

        ArrayList<FaqItem> questions;


        FaqCategory(
                int id,
                String name,
                ArrayList<FaqItem> questions
        ) {

            this.id = id;

            this.name = name;

            this.questions = questions;
        }
    }


    // =========================================================
    // FAQ ITEM MODEL
    // =========================================================

    private static class FaqItem {

        int id;

        String question;

        String answer;


        FaqItem(
                int id,
                String question,
                String answer
        ) {

            this.id = id;

            this.question = question;

            this.answer = answer;
        }
    }

    @Override
    public void onBackPressed() {
            super.onBackPressed();
        overridePendingTransition(
                R.anim.slide_in_left,
                R.anim.slide_out_right
        );
        }
}