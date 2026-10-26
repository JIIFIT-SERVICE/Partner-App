package com.company.jifiit;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Adapter.AreaAdapter;
import com.company.jifiit.Adapter.AreaDropdownAdapter;
import com.company.jifiit.Adapter.RegisterPagerAdapter;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.hbb20.CountryCodePicker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class Register extends AppCompatActivity {

    private static final String TAG = "Register";
    private static final int TOTAL_PAGES = 4;
    private static final int MIN_REQUIRED_AREAS = 50;

    private ViewPager2 viewPager;
    private Button btnNext, btnPrevious;
    private TextView tvStep;
    private ProgressBar progressBar;
    private RegisterPagerAdapter adapter;
    private int currentPage = 0;

    // ============================================================
    // PAGE 1 - PERSONAL DETAILS
    // ============================================================

    private EditText regName, regAadhaar, regMobile, regEmail,
            regDob, regAddress;

    private TextInputEditText regCity;
    private TextView tvAge;
    private RadioGroup rgGender;
    private CountryCodePicker ccp;
    private CircleImageView imgPhoto;
    private Button btnUploadPhoto;

    private String selectedDob = null;
    private String encodedPhoto = null;

    // City state
    private String selectedCityId = "";
    private String selectedLocalityId = "";
    private String selectedHubId = "";

    // Page 1 (status = 0)
    private final ArrayList<String> cityNamesP1 = new ArrayList<>();
    private final ArrayList<String> cityIdsP1 = new ArrayList<>();
    private boolean cityLoadedP1 = false;

    // Page 3 (status = 1)
    private final ArrayList<String> cityNamesP3 = new ArrayList<>();
    private final ArrayList<String> cityIdsP3 = new ArrayList<>();
    private boolean cityLoadedP3 = false;
    private boolean cityLoadingP3 = false;
    private boolean isCityPickerPendingP3 = false;

    private final ArrayList<String> localityNames = new ArrayList<>();
    private final ArrayList<String> localityIds = new ArrayList<>();
    private final ArrayList<String> localityHubIds = new ArrayList<>();

    // ============================================================
    // PAGE 2 - PROFESSIONAL DETAILS
    // ============================================================

    private RadioGroup rgCategory;
    private GridLayout glExperience;
    private EditText etExpDesc;

    private String selectedCategory = "";
    private String selectedExperience = "";

    private boolean categoryUiBuilt = false;

    private static final String[][] CATEGORIES = {
            {"Massage Therapist"},
            {"Ayurveda Massage Therapist"},
            {"Personal Trainer"},
            {"Yoga Teacher"},
            {"Physiotherapist"},
            {"Zumba Dance Teacher"},
            {"Chiropractor"}
    };

    private static final String[] EXPERIENCE = {
            "Fresher",
            "Less than 1 year",
            "1–3 Years",
            "3–5 Years",
            "5-8 Years",
            "8-10 Years",
            "10-15 Years",
            "15+ Years"
    };

    // ============================================================
    // PAGE 3 - PREFERENCES / SERVICE AREAS
    // ============================================================

    private MaterialAutoCompleteTextView actvCityDropdown;
    private RecyclerView rvAreas;
    private TextView tvTotalAreas;
    private View llNoAreasSelected;
    private View llAreasLoading;
    private TextView tvEmptyTitle;
    private TextView tvEmptySubtitle;

    private AreaAdapter areaAdapter;

    private final ArrayList<AreaAdapter.Area> allAreas = new ArrayList<>();
    private final LinkedHashSet<String> selectedAreaIds = new LinkedHashSet<>();

    private String selectedCityIdP3 = "";
    private boolean areasLoaded = false;
    private boolean isLoadingAreas = false;
    private String areasLoadedCityId = "";

    // ============================================================
    // PAGE 4 - REFERRAL
    // ============================================================

    private EditText etReferral;

    // ============================================================
    // IMAGE PICKER
    // ============================================================

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Uri uri = result.getData().getData();

                            if (uri == null) {
                                return;
                            }

                            if (imgPhoto != null) {
                                imgPhoto.setImageURI(uri);
                            }

                            try {
                                InputStream is = getContentResolver().openInputStream(uri);
                                if (is == null) {
                                    Toast.makeText(this, "Unable to open selected image", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                Bitmap bmp = BitmapFactory.decodeStream(is);
                                is.close();

                                if (bmp != null) {
                                    encodedPhoto = encodeImage(bmp);
                                    validateCurrentPage();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Image loading failed", e);
                                Toast.makeText(this, "Unable to load image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );

    // ============================================================
    // ACTIVITY LIFECYCLE
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());

        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);

        setContentView(R.layout.activity_register);

        initViews();
        setupViewPager();
        setupButtons();
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        tvStep = findViewById(R.id.tvSte);
        progressBar = findViewById(R.id.progressBar);

        if (btnNext != null) {
            btnNext.setEnabled(false);
            btnNext.setBackgroundColor(Color.parseColor("#BDBDBD"));
        }

        View backButton = findViewById(R.id.backbtn);
        if (backButton != null) {
            backButton.setOnClickListener(v -> onBackPressed());
        }
    }

    private void setupViewPager() {
        adapter = new RegisterPagerAdapter();

        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(false);
        viewPager.setOffscreenPageLimit(1);

        viewPager.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        currentPage = position;
                        updateUI();

                        // Safety net: if user somehow lands on page 2 (index 1),
                        // make sure page-3 city list starts loading.
                        if (position == 1 && !cityLoadedP3 && !cityLoadingP3
                                && cityNamesP3.isEmpty()) {
                            loadCities(1);
                        }

                        viewPager.post(() -> {
                            initPageViews(position);
                            setupTextWatchers(position);
                            validateCurrentPage();
                        });
                    }
                }
        );

        viewPager.post(() -> {
            initPageViews(0);
            setupTextWatchers(0);
            validateCurrentPage();
        });
    }

    private View getCurrentPageView() {
        if (viewPager == null || adapter == null) {
            return null;
        }

        RecyclerView rv = (RecyclerView) viewPager.getChildAt(0);
        if (rv == null) {
            return null;
        }

        RecyclerView.ViewHolder holder = rv.findViewHolderForAdapterPosition(currentPage);
        return holder != null ? holder.itemView : null;
    }

    // ============================================================
    // INITIALIZE PAGE VIEWS
    // ============================================================

    private void initPageViews(int position) {
        View pageView = getCurrentPageView();

        if (pageView == null) {
            viewPager.postDelayed(() -> initPageViews(position), 50);
            return;
        }

        switch (position) {
            // ----------------------------------------------------
            // PAGE 1 - PERSONAL
            // ----------------------------------------------------
            case 0:
                regName = pageView.findViewById(R.id.reg_name);
                regAadhaar = pageView.findViewById(R.id.reg_aadhaar);
                regMobile = pageView.findViewById(R.id.reg_mobile);
                regEmail = pageView.findViewById(R.id.reg_email);
                regDob = pageView.findViewById(R.id.reg_age);
                regCity = pageView.findViewById(R.id.etCity);
                regAddress = pageView.findViewById(R.id.reg_address);
                tvAge = pageView.findViewById(R.id.current_age);
                rgGender = pageView.findViewById(R.id.radioGroup);
                ccp = pageView.findViewById(R.id.VerifyCountryCode);
                imgPhoto = pageView.findViewById(R.id.upLivePhoto);
                btnUploadPhoto = pageView.findViewById(R.id.uploadLivePhotoButton);

                if (ccp != null && regMobile != null) {
                    ccp.setDefaultCountryUsingNameCode("IN");
                    ccp.resetToDefaultCountry();
                    ccp.registerCarrierNumberEditText(regMobile);
                }

                setupAadhaarFormatter();
                setupCityDropdown();
                setupGenderChips();
                setupDobPicker();
                setupPhotoUpload();
                break;

            // ----------------------------------------------------
            // PAGE 2 - PROFESSIONAL
            // ----------------------------------------------------
            case 1:
                rgCategory = pageView.findViewById(R.id.rgCategory);
                glExperience = pageView.findViewById(R.id.glExperience);
                etExpDesc = pageView.findViewById(R.id.reg_exp_desc);

                if (!categoryUiBuilt) {
                    buildCategoryRows();
                    buildExperienceChips();
                    categoryUiBuilt = true;
                }
                break;

            // ----------------------------------------------------
            // PAGE 3 - PREFERENCES
            // ----------------------------------------------------
            case 2:
                actvCityDropdown = pageView.findViewById(R.id.actvCityDropdown);
                rvAreas = pageView.findViewById(R.id.rvAreas);
                tvTotalAreas = pageView.findViewById(R.id.tvTotalAreas);
                llNoAreasSelected = pageView.findViewById(R.id.llNoAreasSelected);
                llAreasLoading = pageView.findViewById(R.id.llAreasLoading);
                tvEmptyTitle = pageView.findViewById(R.id.tvEmptyTitle);
                tvEmptySubtitle = pageView.findViewById(R.id.tvEmptySubtitle);

                View cityContainer = pageView.findViewById(R.id.cityContainer);
                if (cityContainer != null) {
                    cityContainer.setOnClickListener(v -> openCityPickerPage3());
                }

                View ivCityDropdownArrow = pageView.findViewById(R.id.ivCityDropdownArrow);
                if (ivCityDropdownArrow != null) {
                    ivCityDropdownArrow.setOnClickListener(v -> openCityPickerPage3());
                }

                setupCityDropdownPage3();
                setupAreaPreferences();
                break;

            // ----------------------------------------------------
            // PAGE 4 - REFERRAL
            // ----------------------------------------------------
            case 3:
                etReferral = pageView.findViewById(R.id.reg_referral);
                TextView signIn = pageView.findViewById(R.id.do_you);

                if (signIn != null) {
                    signIn.setOnClickListener(v ->
                            startActivity(new Intent(Register.this, LoginActivity.class))
                    );
                }
                break;
        }
    }

    // ============================================================
    // PAGE 1 - CITY
    // ============================================================

    private void setupCityDropdown() {
        if (regCity == null) {
            return;
        }

        regCity.setFocusable(false);
        regCity.setFocusableInTouchMode(false);
        regCity.setCursorVisible(false);
        regCity.setClickable(true);
        regCity.setLongClickable(false);
        regCity.setInputType(android.text.InputType.TYPE_NULL);
        regCity.setKeyListener(null);

        View parent = (View) regCity.getParent();
        if (parent != null) {
            parent.setFocusable(false);
            parent.setFocusableInTouchMode(false);
            parent.setClickable(false);
        }

        regCity.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.clearFocus();
                openCityPicker();
                return true;
            }
            return true;
        });
    }

    private void openCityPicker() {
        if (cityLoadedP1 && !cityNamesP1.isEmpty()) {
            showCityDialog();
        } else {
            loadCities(0);
        }
    }

    private void showCityDialog() {
        String[] cities = cityNamesP1.toArray(new String[0]);

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.select_dialog_item,
                cities
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(Color.BLACK);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                textView.setPadding(40, 24, 40, 24);
                textView.setBackgroundColor(Color.WHITE);
                return view;
            }
        };

        AlertDialog cityDialog = new AlertDialog.Builder(this, R.style.WhiteAlertDialog)
                .setTitle("Select City")
                .setAdapter(cityAdapter, (dialog, which) -> {
                    String newCityId = cityIdsP1.get(which);
                    selectedCityId = newCityId;

                    if (regCity != null) {
                        regCity.setText(cityNamesP1.get(which));
                    }

                    selectedLocalityId = "";
                    selectedHubId = "";

                    localityNames.clear();
                    localityIds.clear();
                    localityHubIds.clear();

                    loadDataByCityId(selectedCityId);
                    validateCurrentPage();
                    toggleArrowDirection(false);
                })
                .create();

        cityDialog.setOnDismissListener(dialog -> toggleArrowDirection(false));

        cityDialog.setOnShowListener(dialogInterface -> {
            Window w = cityDialog.getWindow();
            if (w != null) {
                // Force whole dialog window white
                w.setBackgroundDrawable(new ColorDrawable(Color.WHITE));

                // Title
                int titleId = getResources().getIdentifier("alertTitle", "id", "android");
                TextView title = cityDialog.findViewById(titleId);
                if (title != null) {
                    title.setTextColor(Color.BLACK);
                    title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    title.setTypeface(null, Typeface.BOLD);
                    title.setPadding(40, 30, 40, 20);
                    title.setBackgroundColor(Color.WHITE);
                }

                // List
                ListView listView = cityDialog.getListView();
                if (listView != null) {
                    listView.setBackgroundColor(Color.WHITE);
                    listView.setDivider(new ColorDrawable(Color.LTGRAY));
                    listView.setDividerHeight(1);
                    listView.setPadding(0, 10, 0, 10);
                    listView.setVerticalScrollBarEnabled(true);
                }
            }
        });

        cityDialog.show();

        if (cityDialog.getWindow() != null) {
            cityDialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    android.view.WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
    }

    // ============================================================
    // PAGE 3 - CITY PREFERENCES
    // ============================================================

    private void openCityPickerPage3() {
        toggleArrowDirection(true);
        if (cityLoadedP3 && !cityNamesP3.isEmpty()) {
            showCityDialogPage3();
        } else {
            isCityPickerPendingP3 = true;
            loadCities(1);
        }
    }

    private void showCityDialogPage3() {
        if (cityNamesP3.isEmpty()) {
            openCityPickerPage3();
            return;
        }

        String[] cities = cityNamesP3.toArray(new String[0]);

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.select_dialog_item,
                cities
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(Color.BLACK);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                textView.setPadding(40, 24, 40, 24);
                textView.setBackgroundColor(Color.WHITE);
                return view;
            }
        };

        AlertDialog cityDialog = new AlertDialog.Builder(this, R.style.WhiteAlertDialog)
                .setTitle("Select City")
                .setAdapter(cityAdapter, (dialog, which) -> {
                    if (which >= 0 && which < cityIdsP3.size()) {
                        String newCityId = cityIdsP3.get(which);
                        String newCityName = cityNamesP3.get(which);
                        onCitySelectedPage3(newCityId, newCityName);
                    }
                    toggleArrowDirection(false);
                })
                .create();

        cityDialog.setOnDismissListener(dialog -> toggleArrowDirection(false));

        cityDialog.setOnShowListener(dialogInterface -> {
            Window w = cityDialog.getWindow();
            if (w != null) {
                // Force whole dialog window white
                w.setBackgroundDrawable(new ColorDrawable(Color.WHITE));

                // Title
                int titleId = getResources().getIdentifier("alertTitle", "id", "android");
                TextView title = cityDialog.findViewById(titleId);
                if (title != null) {
                    title.setTextColor(Color.BLACK);
                    title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    title.setTypeface(null, Typeface.BOLD);
                    title.setPadding(40, 30, 40, 20);
                    title.setBackgroundColor(Color.WHITE);
                }

                // List
                ListView listView = cityDialog.getListView();
                if (listView != null) {
                    listView.setBackgroundColor(Color.WHITE);
                    listView.setDivider(new ColorDrawable(Color.LTGRAY));
                    listView.setDividerHeight(1);
                    listView.setPadding(0, 10, 0, 10);
                    listView.setVerticalScrollBarEnabled(true);
                }
            }
        });

        cityDialog.show();

        if (cityDialog.getWindow() != null) {
            cityDialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    android.view.WindowManager.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void setupCityDropdownPage3() {
        if (actvCityDropdown == null) {
            return;
        }

        refreshCityDropdownPage3();

        actvCityDropdown.setOnClickListener(v -> openCityPickerPage3());
        actvCityDropdown.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.clearFocus();
                openCityPickerPage3();
                return true;
            }
            return false;
        });

        actvCityDropdown.setOnItemClickListener((parent, view, position, id) -> {
            Object selectedItem = parent.getItemAtPosition(position);
            if (selectedItem == null) return;

            String selectedCityName = selectedItem.toString();
            int which = cityNamesP3.indexOf(selectedCityName);
            if (which >= 0 && which < cityIdsP3.size()) {
                onCitySelectedPage3(cityIdsP3.get(which), selectedCityName);
            }
        });

        if (cityNamesP3.isEmpty() && !cityLoadedP3 && !cityLoadingP3) {
            loadCities(1);
        }
    }

    private void refreshCityDropdownPage3() {
        if (actvCityDropdown == null) return;

        actvCityDropdown.setDropDownBackgroundDrawable(new ColorDrawable(Color.WHITE));

        if (!cityNamesP3.isEmpty()) {
            ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    cityNamesP3
            ) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    if (view instanceof TextView) {
                        TextView tv = (TextView) view;
                        tv.setTextColor(Color.BLACK);
                        tv.setBackgroundColor(Color.WHITE);
                    }
                    return view;
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    if (view instanceof TextView) {
                        TextView tv = (TextView) view;
                        tv.setTextColor(Color.BLACK);
                        tv.setBackgroundColor(Color.WHITE);
                        tv.setPadding(32, 24, 32, 24);
                    }
                    return view;
                }
            };
            actvCityDropdown.setAdapter(cityAdapter);
        }

        if (!TextUtils.isEmpty(selectedCityIdP3) && !cityNamesP3.isEmpty()) {
            int idx = cityIdsP3.indexOf(selectedCityIdP3);
            if (idx >= 0 && idx < cityNamesP3.size()) {
                actvCityDropdown.setText(cityNamesP3.get(idx), false);
            }
        } else {
            actvCityDropdown.setText("", false);
        }
    }

    private void onCitySelectedPage3(String newCityId, String newCityName) {
        boolean cityChanged = !newCityId.equals(selectedCityIdP3);
        selectedCityIdP3 = newCityId;

        if (actvCityDropdown != null) {
            actvCityDropdown.setText(newCityName, false);
        }

        if (cityChanged) {
            allAreas.clear();
            selectedAreaIds.clear();
            areasLoaded = false;
            isLoadingAreas = false;
            areasLoadedCityId = "";

            if (areaAdapter != null) {
                areaAdapter.clear();
            }

            updateAreaCount();
        }

        loadAreasByCity();
        validateCurrentPage();
    }

    // ============================================================
    // COMMON CITY LOADER (STATUS = 0 FOR P1, STATUS = 1 FOR P3)
    // ============================================================

    private void loadCities(int status) {
        // Prevent duplicate calls while one is already in flight
        if (status == 1 && cityLoadingP3) {
            return;
        }

        if (status == 1) {
            cityLoadingP3 = true;
        }

        toggleArrowDirection(true);

        String url = "https://jiifit.com/Jifiit/get_cities_for_expert.php?status=" + status;
        Log.d(TAG, "Fetching cities with URL: " + url);

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    if (status == 1) {
                        cityLoadingP3 = false;
                    }

                    try {
                        boolean apiStatus = response.optBoolean("status", false);

                        if (!apiStatus) {
                            Toast.makeText(this, "Failed to load cities", Toast.LENGTH_SHORT).show();
                            isCityPickerPendingP3 = false;
                            toggleArrowDirection(false);
                            return;
                        }

                        JSONArray citiesArray = response.optJSONArray("cities");

                        if (citiesArray == null) {
                            isCityPickerPendingP3 = false;
                            toggleArrowDirection(false);
                            return;
                        }

                        ArrayList<String> targetNames = (status == 0) ? cityNamesP1 : cityNamesP3;
                        ArrayList<String> targetIds = (status == 0) ? cityIdsP1 : cityIdsP3;

                        targetNames.clear();
                        targetIds.clear();

                        for (int i = 0; i < citiesArray.length(); i++) {
                            JSONObject cityObject = citiesArray.optJSONObject(i);
                            if (cityObject == null) continue;

                            targetIds.add(cityObject.optString("id", ""));
                            targetNames.add(cityObject.optString("name", ""));
                        }

                        if (targetNames.isEmpty()) {
                            Toast.makeText(this, "No cities available", Toast.LENGTH_SHORT).show();
                            isCityPickerPendingP3 = false;
                            toggleArrowDirection(false);
                            return;
                        }

                        if (status == 0) {
                            cityLoadedP1 = true;
                            toggleArrowDirection(false);
                            showCityDialog();
                        } else {
                            cityLoadedP3 = true;
                            refreshCityDropdownPage3();
                            toggleArrowDirection(false);
                            if (isCityPickerPendingP3) {
                                isCityPickerPendingP3 = false;
                                showCityDialogPage3();
                            }
                        }

                    } catch (Exception e) {
                        isCityPickerPendingP3 = false;
                        Log.e(TAG, "City parsing failed", e);
                        Toast.makeText(this, "Error loading cities", Toast.LENGTH_SHORT).show();
                        toggleArrowDirection(false);
                    }
                },
                error -> {
                    if (status == 1) {
                        cityLoadingP3 = false;
                        isCityPickerPendingP3 = false;
                    }
                    Log.e(TAG, "City API error", error);
                    Toast.makeText(this, "Unable to connect to server", Toast.LENGTH_SHORT).show();
                    toggleArrowDirection(false);
                }
        );

        request.setRetryPolicy(
                new DefaultRetryPolicy(
                        8000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );

        requestQueue.add(request);
    }

    private void toggleArrowDirection(boolean loading) {
        runOnUiThread(() -> {
            View pageView = getCurrentPageView();
            if (pageView == null) return;

            View ivCityArrowP1 = pageView.findViewById(R.id.ivCityDropdownArrow);
            View ivCityArrowP3 = pageView.findViewById(R.id.ivCityDropdownArrow);

            View arrowView = ivCityArrowP3 != null ? ivCityArrowP3 : ivCityArrowP1;

            if (arrowView != null) {
                if (loading) {
                    arrowView.animate()
                            .rotation(180f)
                            .setDuration(200)
                            .start();
                    arrowView.setEnabled(false);
                } else {
                    arrowView.animate()
                            .rotation(0f)
                            .setDuration(200)
                            .start();
                    arrowView.setEnabled(true);
                }
            }
        });
    }

    private void loadDataByCityId(String cityId) {
        if (TextUtils.isEmpty(cityId)) {
            return;
        }

        localityNames.clear();
        localityIds.clear();
        localityHubIds.clear();
        selectedLocalityId = "";
        selectedHubId = "";

        String url = Constant.get_localities + "?city_id=" + Uri.encode(cityId);
        Log.d(TAG, "Loading localities from: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONArray localityArray = null;
                        if (response.has("localities")) {
                            localityArray = response.optJSONArray("localities");
                        } else if (response.has("data")) {
                            localityArray = response.optJSONArray("data");
                        }

                        if (localityArray != null && localityArray.length() > 0) {
                            for (int i = 0; i < localityArray.length(); i++) {
                                JSONObject obj = localityArray.optJSONObject(i);
                                if (obj == null) continue;

                                String id = obj.optString("id", "");
                                String name = obj.optString("name", "");
                                String hubId = obj.optString("hub_id", "");

                                if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(name)) {
                                    localityIds.add(id.trim());
                                    localityNames.add(name.trim());
                                    localityHubIds.add(hubId.trim());
                                }
                            }
                        }

                        Log.d(TAG, "Loaded " + localityNames.size() + " localities for city ID: " + cityId);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse locality/hub data", e);
                    }
                },
                error -> {
                    String errorMsg = "Failed to load area hubs";
                    if (error instanceof TimeoutError) {
                        errorMsg = "Timeout loading area hubs";
                    } else if (error.networkResponse != null) {
                        errorMsg = "HTTP Error " + error.networkResponse.statusCode;
                    }
                    Log.e(TAG, "Locality API error: " + errorMsg, error);
                }
        );

        request.setRetryPolicy(
                new DefaultRetryPolicy(
                        10000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );

        Volley.newRequestQueue(getApplicationContext()).add(request);
    }

    // ============================================================
    // PAGE 3 - AREA PREFERENCES
    // ============================================================

    private void setupAreaPreferences() {
        if (rvAreas == null) {
            return;
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvAreas.setLayoutManager(layoutManager);
        rvAreas.setNestedScrollingEnabled(true);
        rvAreas.setHasFixedSize(true);
        rvAreas.setItemAnimator(null);

        areaAdapter = new AreaAdapter(
                allAreas,
                selectedAreaIds,
                () -> {
                    updateAreaCount();
                    validateCurrentPage();
                }
        );
        rvAreas.setAdapter(areaAdapter);

        updateAreaCount();

        if (!TextUtils.isEmpty(selectedCityIdP3) && allAreas.isEmpty()) {
            loadAreasByCity();
        }
    }

    private int getRequiredAreasCount() {
        if (allAreas == null || allAreas.isEmpty()) {
            return MIN_REQUIRED_AREAS;
        }
        return Math.min(MIN_REQUIRED_AREAS, allAreas.size());
    }

    private void updateAreaCount() {
        int count = selectedAreaIds.size();
        int required = getRequiredAreasCount();

        if (tvTotalAreas != null) {
            if (count < required) {
                tvTotalAreas.setText(count + " / " + required + " selected");
            } else {
                tvTotalAreas.setText(count + " locations selected");
            }
        }

        if (llNoAreasSelected != null && rvAreas != null) {
            if (isLoadingAreas) {
                llNoAreasSelected.setVisibility(View.GONE);
                rvAreas.setVisibility(View.GONE);
            } else if (allAreas.isEmpty() || TextUtils.isEmpty(selectedCityIdP3)) {
                llNoAreasSelected.setVisibility(View.VISIBLE);
                rvAreas.setVisibility(View.GONE);
                if (tvEmptyTitle != null) {
                    if (TextUtils.isEmpty(selectedCityIdP3)) {
                        tvEmptyTitle.setText("Select a city to view hubs");
                        if (tvEmptySubtitle != null) {
                            tvEmptySubtitle.setText("Choose your primary city above to display all available operating locations.");
                        }
                    } else {
                        tvEmptyTitle.setText("No locations available");
                        if (tvEmptySubtitle != null) {
                            tvEmptySubtitle.setText("No operating hubs found for the selected city.");
                        }
                    }
                }
            } else {
                llNoAreasSelected.setVisibility(View.GONE);
                if (rvAreas.getVisibility() != View.VISIBLE) {
                    rvAreas.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void loadAreasByCity() {
        if (TextUtils.isEmpty(selectedCityIdP3)) {
            return;
        }

        isLoadingAreas = true;
        areasLoaded = false;

        allAreas.clear();
        selectedAreaIds.clear();

        if (areaAdapter != null) {
            areaAdapter.clear();
        }

        if (llAreasLoading != null) {
            llAreasLoading.setVisibility(View.VISIBLE);
        }
        if (llNoAreasSelected != null) {
            llNoAreasSelected.setVisibility(View.GONE);
        }
        if (rvAreas != null) {
            rvAreas.setVisibility(View.GONE);
        }

        updateAreaCount();
        validateCurrentPage();

        String url = Constant.get_localities + "?city_id=" + Uri.encode(selectedCityIdP3);
        Log.d(TAG, "Area API URL: " + url);

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                responseStr -> {
                    isLoadingAreas = false;
                    if (llAreasLoading != null) {
                        llAreasLoading.setVisibility(View.GONE);
                    }

                    try {
                        allAreas.clear();
                        String trimmed = responseStr != null ? responseStr.trim() : "";
                        JSONArray dataArray = null;

                        if (trimmed.startsWith("{")) {
                            JSONObject json = new JSONObject(trimmed);
                            if (json.has("data")) {
                                dataArray = json.optJSONArray("data");
                            } else if (json.has("areas")) {
                                dataArray = json.optJSONArray("areas");
                            }
                        } else if (trimmed.startsWith("[")) {
                            dataArray = new JSONArray(trimmed);
                        }

                        if (dataArray != null) {
                            for (int i = 0; i < dataArray.length(); i++) {
                                JSONObject item = dataArray.optJSONObject(i);
                                if (item == null) continue;

                                String areaId = item.optString("id", "");
                                String areaName = item.optString("name", "");
                                String areaDesc = item.optString("description", "");

                                if (!TextUtils.isEmpty(areaId) && !TextUtils.isEmpty(areaName)) {
                                    allAreas.add(new AreaAdapter.Area(areaId.trim(), areaName.trim(), areaDesc.trim()));
                                }
                            }
                        }

                        Collections.sort(allAreas, (a, b) ->
                                a.name.trim().compareToIgnoreCase(b.name.trim())
                        );

                        areasLoaded = true;
                        areasLoadedCityId = selectedCityIdP3;

                        if (areaAdapter != null) {
                            areaAdapter.setAreas(allAreas);
                        }

                        updateAreaCount();
                        validateCurrentPage();

                        if (allAreas.isEmpty()) {
                            Toast.makeText(Register.this, "No areas available for this city", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        areasLoaded = false;
                        areasLoadedCityId = "";
                        Log.e(TAG, "Area parsing failed", e);
                        Toast.makeText(Register.this, "Unable to read area data", Toast.LENGTH_SHORT).show();
                        updateAreaCount();
                        validateCurrentPage();
                    }
                },
                error -> {
                    isLoadingAreas = false;
                    areasLoaded = false;
                    areasLoadedCityId = "";
                    if (llAreasLoading != null) {
                        llAreasLoading.setVisibility(View.GONE);
                    }

                    String errorMessage = "Unknown error";
                    if (error instanceof TimeoutError) {
                        errorMessage = "Request timeout";
                    } else if (error instanceof com.android.volley.NoConnectionError) {
                        errorMessage = "No internet connection";
                    } else if (error.networkResponse != null) {
                        errorMessage = "HTTP " + error.networkResponse.statusCode;
                    }

                    Log.e(TAG, "Area API failed: " + errorMessage, error);
                    Toast.makeText(Register.this, "Area loading failed: " + errorMessage, Toast.LENGTH_LONG).show();
                    updateAreaCount();
                    validateCurrentPage();
                }
        );

        request.setRetryPolicy(
                new DefaultRetryPolicy(
                        15000,
                        1,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );

        Volley.newRequestQueue(getApplicationContext()).add(request);
    }

    private void setupAreaDropdownAdapter() {
        // Obsolete: list is now rendered directly
    }

    private void showAllAreasDropdown() {
        // Obsolete: list is now rendered directly
    }

    // ============================================================
    // PAGE 1 - AADHAAR FORMATTER
    // ============================================================

    private void setupAadhaarFormatter() {
        if (regAadhaar == null) {
            return;
        }

        regAadhaar.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;

                isFormatting = true;
                String input = s.toString().replaceAll("\\s+", "");

                if (input.length() > 12) {
                    input = input.substring(0, 12);
                }

                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < input.length(); i++) {
                    formatted.append(input.charAt(i));
                    if (i == 3 || i == 7) {
                        formatted.append(" ");
                    }
                }

                regAadhaar.setText(formatted.toString());
                regAadhaar.setSelection(formatted.length());
                isFormatting = false;
            }
        });
    }

    // ============================================================
    // PAGE 1 - GENDER
    // ============================================================

    private void setupGenderChips() {
        if (rgGender == null) {
            return;
        }

        rgGender.setOnCheckedChangeListener((group, checkedId) -> {
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (!(child instanceof RadioButton)) continue;

                RadioButton rb = (RadioButton) child;
                boolean selected = rb.getId() == checkedId;

                rb.setBackgroundResource(
                        selected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected
                );

                rb.setTextColor(
                        getResources().getColor(
                                selected ? R.color.white : R.color.Text_Blue
                        )
                );
            }
            validateCurrentPage();
        });
    }

    // ============================================================
    // PAGE 1 - DATE OF BIRTH
    // ============================================================

    private void setupDobPicker() {
        if (regDob == null) {
            return;
        }

        regDob.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            DatePickerDialog dialog = new DatePickerDialog(
                    Register.this,
                    (view, year, month, day) -> {
                        Calendar dob = Calendar.getInstance();
                        dob.set(year, month, day);

                        selectedDob = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dob.getTime());
                        regDob.setText(new SimpleDateFormat("MM / dd / yyyy", Locale.getDefault()).format(dob.getTime()));

                        Calendar today = Calendar.getInstance();
                        int years = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
                        int months = today.get(Calendar.MONTH) - dob.get(Calendar.MONTH);

                        if (today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH)) {
                            months--;
                        }

                        if (months < 0) {
                            years--;
                            months += 12;
                        }

                        if (years < 18) {
                            tvAge.setText(R.string.age_warning);
                            tvAge.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        } else {
                            tvAge.setText("Your current age is : " + years + " years and " + months + " months");
                            tvAge.setTextColor(getResources().getColor(R.color.green_success));
                        }

                        validateCurrentPage();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            dialog.getDatePicker().setMaxDate(new Date().getTime());
            dialog.show();
        });
    }

    // ============================================================
    // PAGE 1 - PHOTO UPLOAD
    // ============================================================

    private void setupPhotoUpload() {
        if (btnUploadPhoto == null) {
            return;
        }

        btnUploadPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        });
    }

    // ============================================================
    // PAGE 2 - CATEGORY
    // ============================================================

    private void buildCategoryRows() {
        if (rgCategory == null) {
            return;
        }

        rgCategory.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (String[] category : CATEGORIES) {
            View row = inflater.inflate(R.layout.item_category_option, rgCategory, false);
            RadioButton rb = row.findViewById(R.id.rbOption);
            TextView title = row.findViewById(R.id.tvCatTitle);

            title.setText(category[0]);
            String categoryName = category[0];

            row.setOnClickListener(view -> {
                rb.setChecked(true);
                selectedCategory = categoryName;
                highlightCategoryRows();
                validateCurrentPage();
            });

            rb.setOnCheckedChangeListener((button, checked) -> {
                if (checked) {
                    selectedCategory = categoryName;
                    highlightCategoryRows();
                    validateCurrentPage();
                }
            });

            rgCategory.addView(row);
        }
    }

    private void highlightCategoryRows() {
        if (rgCategory == null) {
            return;
        }

        for (int i = 0; i < rgCategory.getChildCount(); i++) {
            View row = rgCategory.getChildAt(i);
            RadioButton rb = row.findViewById(R.id.rbOption);

            row.setBackgroundResource(
                    rb.isChecked()
                            ? R.drawable.bg_category_row_selected
                            : R.drawable.bg_category_row
            );
        }
    }

    // ============================================================
    // PAGE 2 - EXPERIENCE
    // ============================================================

    private void buildExperienceChips() {
        if (glExperience == null) {
            return;
        }

        glExperience.removeAllViews();

        for (String experience : EXPERIENCE) {
            RadioButton rb = new RadioButton(this);
            rb.setText(experience);
            rb.setButtonDrawable(null);
            rb.setGravity(Gravity.CENTER);
            rb.setBackgroundResource(R.drawable.bg_chip_unselected);
            rb.setTextColor(getResources().getColor(R.color.Text_Blue));
            rb.setPadding(24, 24, 24, 24);

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = 0;
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            lp.setMargins(6, 6, 6, 6);
            lp.height = (int) (52 * getResources().getDisplayMetrics().density);
            rb.setLayoutParams(lp);

            rb.setOnClickListener(view -> {
                selectedExperience = experience;
                updateExperienceChips();
                validateCurrentPage();
            });

            glExperience.addView(rb);
        }
    }

    private void updateExperienceChips() {
        if (glExperience == null) {
            return;
        }

        for (int i = 0; i < glExperience.getChildCount(); i++) {
            RadioButton rb = (RadioButton) glExperience.getChildAt(i);
            boolean selected = rb.getText().toString().equals(selectedExperience);

            rb.setBackgroundResource(
                    selected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected
            );

            rb.setTextColor(
                    getResources().getColor(
                            selected ? R.color.white : R.color.Text_Blue
                    )
            );

            rb.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    // ============================================================
    // TEXT WATCHERS
    // ============================================================

    private void setupTextWatchers(int position) {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateCurrentPage();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        switch (position) {
            case 0:
                if (regName != null) regName.addTextChangedListener(watcher);
                if (regEmail != null) regEmail.addTextChangedListener(watcher);
                if (regAddress != null) regAddress.addTextChangedListener(watcher);
                if (regCity != null) regCity.addTextChangedListener(watcher);
                if (regMobile != null) regMobile.addTextChangedListener(watcher);
                break;

            case 1:
                if (etExpDesc != null) etExpDesc.addTextChangedListener(watcher);
                break;

            case 2:
                // Checked via AreaAdapter callbacks
                break;

            case 3:
                if (etReferral != null) etReferral.addTextChangedListener(watcher);
                break;
        }
    }

    // ============================================================
    // VALIDATION
    // ============================================================

    private boolean validateCurrentPage() {
        boolean valid = false;

        switch (currentPage) {
            case 0:
                valid = validatePersonal();
                break;
            case 1:
                valid = validateProfessional();
                break;
            case 2:
                valid = validatePreferences();
                break;
            case 3:
                valid = validateReferral();
                break;
        }

        updateNextButtonState(valid);
        return valid;
    }

    private boolean validatePersonal() {
        if (regName == null || regAadhaar == null) return false;
        if (TextUtils.isEmpty(regName.getText())) return false;

        String aadhaar = regAadhaar.getText().toString().trim();
        if (aadhaar.length() != 14) return false;

        if (ccp == null || !ccp.isValidFullNumber()) return false;

        String email = regEmail != null && regEmail.getText() != null
                ? regEmail.getText().toString().trim()
                : "";

        if (!isValidEmail(email)) return false;

        if (rgGender == null || rgGender.getCheckedRadioButtonId() == -1) return false;
        if (selectedDob == null) return false;

        if (tvAge != null && tvAge.getText().toString().equals(getString(R.string.age_warning))) {
            return false;
        }

        if (regCity == null || TextUtils.isEmpty(regCity.getText())) return false;
        if (TextUtils.isEmpty(selectedCityId)) return false;
        if (regAddress == null || TextUtils.isEmpty(regAddress.getText())) return false;
        if (encodedPhoto == null) return false;

        return true;
    }

    private boolean validateProfessional() {
        return !TextUtils.isEmpty(selectedCategory)
                && !TextUtils.isEmpty(selectedExperience);
    }

    private boolean validatePreferences() {
        return !TextUtils.isEmpty(selectedCityIdP3)
                && areasLoaded
                && selectedAreaIds != null
                && selectedAreaIds.size() >= getRequiredAreasCount();
    }

    private boolean validateReferral() {
        return true;
    }

    private boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) return false;
        Pattern pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        return pattern.matcher(email).matches();
    }

    // ============================================================
    // NAVIGATION UI
    // ============================================================

    private void updateNextButtonState(boolean valid) {
        if (btnNext == null) return;

        if (valid) {
            btnNext.setEnabled(true);
            btnNext.setBackgroundColor(Color.parseColor("#1F5FE0"));
            btnNext.setTextColor(Color.WHITE);
        } else {
            btnNext.setEnabled(false);
            btnNext.setBackgroundColor(Color.parseColor("#BDBDBD"));
            btnNext.setTextColor(Color.parseColor("#757575"));
        }
    }

    private void updateUI() {
        tvStep.setText((currentPage + 1) + " / " + TOTAL_PAGES);
        progressBar.setProgress((currentPage + 1) * 100 / TOTAL_PAGES);

        btnPrevious.setVisibility(View.VISIBLE);

        if (currentPage == TOTAL_PAGES - 1) {
            btnNext.setText("SUBMIT");
        } else {
            btnNext.setText("CONTINUE");
        }

        btnPrevious.setText(currentPage == 0 ? "BACK" : "PREVIOUS");
    }

    private void setupButtons() {
        btnNext.setOnClickListener(v -> {
            if (currentPage == TOTAL_PAGES - 1) {
                submitRegistration();
            } else if (validateCurrentPage()) {

                // ✅ Preload Page-3 city list as soon as user leaves Page 1
                if (currentPage == 0 && !cityLoadedP3 && !cityLoadingP3) {
                    loadCities(1);
                }

                viewPager.setCurrentItem(currentPage + 1, true);
            } else {
                String message;

                if (currentPage == 2 && TextUtils.isEmpty(selectedCityIdP3)) {
                    message = "Please select your primary city";
                } else if (currentPage == 2 && !areasLoaded) {
                    message = "Please wait for areas to load";
                } else if (currentPage == 2) {
                    int req = getRequiredAreasCount();
                    message = "Please select at least " + req + " location hubs (Currently selected: " + selectedAreaIds.size() + ")";
                } else {
                    message = "Please fill all required fields";
                }

                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        btnPrevious.setOnClickListener(v -> {
            if (currentPage > 0) {
                viewPager.setCurrentItem(currentPage - 1, true);
            } else {
                onBackPressed();
            }
        });
    }

    // ============================================================
    // IMAGE ENCODING
    // ============================================================

    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float ratio = (float) width / (float) height;
        if (ratio > 1) {
            width = 500;
            height = (int) (width / ratio);
        } else {
            height = 500;
            width = (int) (height * ratio);
        }

        Bitmap resized = Bitmap.createScaledBitmap(bitmap, width, height, true);
        resized.compress(Bitmap.CompressFormat.JPEG, 80, baos);

        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    // ============================================================
    // SUBMIT REGISTRATION
    // ============================================================

    private void submitRegistration() {
        if (!validatePersonal() || !validateProfessional() || !validatePreferences()) {
            Toast.makeText(
                    this,
                    "Please complete all required registration steps",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        final String name = regName.getText().toString().trim();
        final String aadhaar = regAadhaar.getText().toString().trim();
        final String mobile = ccp.getFullNumberWithPlus();
        final String email = regEmail.getText().toString().trim();
        final String dob = selectedDob;
        final String address = regAddress.getText().toString().trim();
        final String referral = etReferral != null ? etReferral.getText().toString().trim() : "";
        final String expDesc = etExpDesc != null ? etExpDesc.getText().toString().trim() : "";
        final String cityId = !TextUtils.isEmpty(selectedCityIdP3) ? selectedCityIdP3 : selectedCityId;
        final String areaIds = TextUtils.join(",", selectedAreaIds);

        String gender = "";
        RadioButton selectedGender = findViewById(rgGender.getCheckedRadioButtonId());
        if (selectedGender != null) {
            gender = selectedGender.getText().toString();
        }
        final String finalGender = gender;

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(
                Request.Method.POST,
                Constant.RegisterUrl,
                response -> {
                    progressDialog.dismiss();
                    Log.d(TAG, "Response: " + response);

                    if (response.contains("Aadhaar number already exists")) {
                        Toast.makeText(this, "This Aadhaar is already registered.", Toast.LENGTH_SHORT).show();
                    } else if (response.contains("Mobile number already exists")) {
                        Toast.makeText(this, "This mobile is already registered.", Toast.LENGTH_SHORT).show();
                    } else if (response.contains("The code is not valid")) {
                        Toast.makeText(this, "Invalid referral code", Toast.LENGTH_SHORT).show();
                    } else if (response.contains("Successfully added user")) {
                        Toast.makeText(this, "Registration Success", Toast.LENGTH_SHORT).show();
                        sendEmail(mobile);

                        Intent intent = new Intent(Register.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Registration failed, try again later", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    if (error instanceof TimeoutError) {
                        Toast.makeText(this, "Network timeout", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "Registration request failed", error);
                        Toast.makeText(this, "Unable to submit registration", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("email", email);
                params.put("aadhar_card_no", aadhaar);
                params.put("phone", mobile);
                params.put("date_of_birth", dob);
                params.put("gender", finalGender);
                params.put("address", address);
                params.put("city_id", cityId);
                params.put("bounsReferral", referral);
                params.put("profile_photo", encodedPhoto);
                params.put("expert_category", selectedCategory);
                params.put("experience", selectedExperience);
                params.put("experience_desc", expDesc);
                params.put("area_ids", areaIds);
                return params;
            }
        };

        request.setRetryPolicy(
                new DefaultRetryPolicy(
                        30000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    // ============================================================
    // SEND EMAIL
    // ============================================================

    private void sendEmail(String mobile) {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(
                Request.Method.POST,
                Constant.email,
                response -> Log.d(TAG, "Email response: " + response),
                error -> Log.e(TAG, "Email request failed", error)
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("phone", mobile);
                return params;
            }
        };

        queue.add(request);
    }

    // ============================================================
    // BACK BUTTON
    // ============================================================

    @Override
    public void onBackPressed() {
        if (currentPage > 0) {
            viewPager.setCurrentItem(currentPage - 1, true);
        } else {
            super.onBackPressed();
        }
    }
}