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
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Adapter.RegisterPagerAdapter;
import com.google.android.material.textfield.TextInputEditText;
import com.hbb20.CountryCodePicker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class Register extends AppCompatActivity {

    private static final String TAG = "Register";
    private static final int TOTAL_PAGES = 3;

    private ViewPager2 viewPager;
    private Button btnNext, btnPrevious;
    private TextView tvStep;
    private ProgressBar progressBar;
    private RegisterPagerAdapter adapter;
    private int currentPage = 0;

    // Page 1
    private EditText regName, regAadhaar, regMobile, regEmail, regDob, regAddress;
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
    private final ArrayList<String> cityNames = new ArrayList<>();
    private final ArrayList<String> cityIds = new ArrayList<>();
    private final ArrayList<String> localityNames = new ArrayList<>();
    private final ArrayList<String> localityIds = new ArrayList<>();
    private final ArrayList<String> localityHubIds = new ArrayList<>();
    private boolean cityLoaded = false;

    // Page 2
    private RadioGroup rgCategory;
    private GridLayout glExperience;
    private EditText etExpDesc;
    private String selectedCategory = "";
    private String selectedExperience = "";
    private boolean categoryUiBuilt = false;

    // Page 3
    private EditText etReferral;

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
            "Fresher", "Less than 1 year", "1–3 Years", "3–5 Years", "5-8 Years" , "8-10 Years","10-15 Years", "15+ Years"
    };

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri == null) return;
                    if (imgPhoto != null) imgPhoto.setImageURI(uri);
                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        InputStream is = getContentResolver().openInputStream(uri);
                        Bitmap bmp = BitmapFactory.decodeStream(is);
                        encodedPhoto = encodeImage(bmp);
                        validateCurrentPage();
                    } catch (Exception e) { e.printStackTrace(); }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(
                        getWindow(),
                        getWindow().getDecorView()
                );

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

        findViewById(R.id.backbtn).setOnClickListener(v -> onBackPressed());
    }

    private void setupViewPager() {
        adapter = new RegisterPagerAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(false);
        viewPager.setOffscreenPageLimit(1);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPage = position;
                updateUI();

                viewPager.post(() -> {
                    initPageViews(position);
                    setupTextWatchers(position);
                    validateCurrentPage();
                });
            }
        });

        viewPager.post(() -> {
            initPageViews(0);
            setupTextWatchers(0);
            validateCurrentPage();
        });
    }

    private View getCurrentPageView() {
        if (viewPager == null || adapter == null) return null;
        androidx.recyclerview.widget.RecyclerView rv =
                (androidx.recyclerview.widget.RecyclerView) viewPager.getChildAt(0);
        if (rv == null) return null;
        androidx.recyclerview.widget.RecyclerView.ViewHolder holder =
                rv.findViewHolderForAdapterPosition(currentPage);
        return holder != null ? holder.itemView : null;
    }

    private void initPageViews(int position) {
        View pageView = getCurrentPageView();
        if (pageView == null) {
            viewPager.postDelayed(() -> initPageViews(position), 50);
            return;
        }

        switch (position) {
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

            case 2:
                etReferral = pageView.findViewById(R.id.reg_referral);
                TextView signIn = pageView.findViewById(R.id.do_you);
                if (signIn != null) {
                    signIn.setOnClickListener(v ->
                            startActivity(new Intent(Register.this, LoginActivity.class)));
                }
                break;
        }
    }

    // ============================================================
    // PAGE 1
    // ============================================================

 private void setupCityDropdown() {
        if (regCity == null) return;

        // Focus completely block
        regCity.setFocusable(false);
        regCity.setFocusableInTouchMode(false);
        regCity.setCursorVisible(false);
        regCity.setClickable(true);
        regCity.setLongClickable(false);
        regCity.setInputType(android.text.InputType.TYPE_NULL);
        regCity.setKeyListener(null);

        // Parent TextInputLayout focusable off korun
        View parent = (View) regCity.getParent();
        if (parent != null) {
            parent.setFocusable(false);
            parent.setFocusableInTouchMode(false);
            parent.setClickable(false);
        }

        // Single tap e dialog open
        regCity.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                Log.d("CITY_TEST", "regCity ACTION_UP");
                v.clearFocus();
                openCityPicker();
                return true; // event consume kori — TextInputEditText ar handle korbe na
            }
            return true;
        });
    }

    private void openCityPicker() {
        if (cityLoaded && !cityNames.isEmpty()) {
            showCityDialog();
        } else {
            loadCities();
        }
    }

    private void loadCities() {
        toggleArrowDirection(true);

        String url = Constant.get_cities;
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        boolean status = response.getBoolean("status");
                        if (!status) {
                            Toast.makeText(this, "Failed to load cities", Toast.LENGTH_SHORT).show();
                            toggleArrowDirection(false);
                            return;
                        }

                        JSONArray citiesArray = response.getJSONArray("cities");

                        cityNames.clear();
                        cityIds.clear();

                        for (int i = 0; i < citiesArray.length(); i++) {
                            JSONObject cityObject = citiesArray.getJSONObject(i);
                            cityIds.add(cityObject.getString("id"));
                            cityNames.add(cityObject.getString("name"));
                        }

                        if (cityNames.isEmpty()) {
                            Toast.makeText(this, "No cities available", Toast.LENGTH_SHORT).show();
                            toggleArrowDirection(false);
                            return;
                        }

                        cityLoaded = true;
                        showCityDialog();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error loading cities", Toast.LENGTH_SHORT).show();
                        toggleArrowDirection(false);
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Unable to connect to server", Toast.LENGTH_SHORT).show();
                    toggleArrowDirection(false);
                }
        );

        request.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(request);
    }

    private void showCityDialog() {
        String[] cities = cityNames.toArray(new String[0]);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
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
                textView.setPadding(40, 20, 40, 20);
                return view;
            }
        };

        AlertDialog cityDialog = new AlertDialog.Builder(this)
                .setTitle("Select City")
                .setAdapter(adapter, (dialog, which) -> {
                    selectedCityId = cityIds.get(which);
                    regCity.setText(cityNames.get(which));

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
            if (cityDialog.getWindow() != null) {
                GradientDrawable drawable = new GradientDrawable();
                drawable.setColor(Color.WHITE);
                drawable.setCornerRadius(20);
                cityDialog.getWindow().setBackgroundDrawable(drawable);
            }

            int titleId = getResources().getIdentifier("alertTitle", "id", "android");
            TextView title = cityDialog.findViewById(titleId);
            if (title != null) {
                title.setTextColor(Color.BLACK);
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                title.setTypeface(null, Typeface.BOLD);
                title.setPadding(40, 30, 40, 20);
            }

            ListView listView = cityDialog.getListView();
            if (listView != null) {
                listView.setDivider(new ColorDrawable(Color.LTGRAY));
                listView.setDividerHeight(1);
                listView.setPadding(0, 10, 0, 10);
                listView.setVerticalScrollBarEnabled(true);
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

    private void toggleArrowDirection(boolean loading) {
        // Apnar existing arrow direction / loading animation logic ekhane thakbe.
        // Jodi na thake, empty rakhte paren.
    }

    private void loadDataByCityId(String cityId) {
        // Apnar locality/hub load logic ekhane thakbe (jodi thake).
        // Na thakle empty rakhte paren.
    }

    private void setupAadhaarFormatter() {
        if (regAadhaar == null) return;
        regAadhaar.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            private boolean deletingHyphen;
            private int hyphenStart;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (count > 0 && s.charAt(start) == ' ' && start > 0 && s.charAt(start - 1) != ' ') {
                    deletingHyphen = true;
                    hyphenStart = start - 1;
                } else {
                    deletingHyphen = false;
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting || deletingHyphen) {
                    return;
                }

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

                if (deletingHyphen) {
                    regAadhaar.setSelection(hyphenStart);
                    deletingHyphen = false;
                }
            }
        });
    }

    private void setupGenderChips() {
        if (rgGender == null) return;
        rgGender.setOnCheckedChangeListener((g, id) -> {
            for (int i = 0; i < g.getChildCount(); i++) {
                RadioButton rb = (RadioButton) g.getChildAt(i);
                boolean sel = rb.getId() == id;
                rb.setBackgroundResource(sel
                        ? R.drawable.bg_chip_selected
                        : R.drawable.bg_chip_unselected);
                rb.setTextColor(getResources().getColor(
                        sel ? R.color.white : R.color.Text_Blue));
            }
            validateCurrentPage();
        });
    }

    private void setupDobPicker() {
        if (regDob == null) return;
        regDob.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dlg = new DatePickerDialog(Register.this,
                    (view, y, m, d) -> {
                        Calendar dob = Calendar.getInstance();
                        dob.set(y, m, d);
                        selectedDob = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .format(dob.getTime());
                        regDob.setText(new SimpleDateFormat("MM / dd / yyyy", Locale.getDefault())
                                .format(dob.getTime()));

                        Calendar today = Calendar.getInstance();
                        int years = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
                        int months = today.get(Calendar.MONTH) - dob.get(Calendar.MONTH);
                        if (today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH)) months--;
                        if (months < 0) { years--; months += 12; }

                        if (years < 18) {
                            tvAge.setText(R.string.age_warning);
                            tvAge.setTextColor(getResources().getColor(
                                    android.R.color.holo_red_dark));
                        } else {
                            tvAge.setText("Your current age is : " + years
                                    + " years and " + months + " months");
                            tvAge.setTextColor(getResources().getColor(R.color.green_success));
                        }
                        validateCurrentPage();
                    },
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            dlg.getDatePicker().setMaxDate(new Date().getTime());
            dlg.show();
        });
    }

    private void setupPhotoUpload() {
        if (btnUploadPhoto == null) return;
        btnUploadPhoto.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            galleryLauncher.launch(i);
        });
    }

    // ============================================================
    // PAGE 2
    // ============================================================

    private void buildCategoryRows() {
        if (rgCategory == null) return;
        rgCategory.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (String[] cat : CATEGORIES) {
            View row = inflater.inflate(R.layout.item_category_option, rgCategory, false);
            RadioButton rb = row.findViewById(R.id.rbOption);
            TextView title = row.findViewById(R.id.tvCatTitle);

            title.setText(cat[0]);

            final String name = cat[0];
            row.setOnClickListener(view -> {
                rb.setChecked(true);
                selectedCategory = name;
                highlightCategoryRows();
                validateCurrentPage();
            });
            rb.setOnCheckedChangeListener((b, checked) -> {
                if (checked) {
                    selectedCategory = name;
                    highlightCategoryRows();
                    validateCurrentPage();
                }
            });
            rgCategory.addView(row);
        }
    }

    private void highlightCategoryRows() {
        for (int i = 0; i < rgCategory.getChildCount(); i++) {
            View row = rgCategory.getChildAt(i);
            RadioButton rb = row.findViewById(R.id.rbOption);
            row.setBackgroundResource(rb.isChecked()
                    ? R.drawable.bg_category_row_selected
                    : R.drawable.bg_category_row);
        }
    }

    private void buildExperienceChips() {
        if (glExperience == null) return;
        glExperience.removeAllViews();
        for (String exp : EXPERIENCE) {
            RadioButton rb = new RadioButton(this);
            rb.setText(exp);
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
                selectedExperience = exp;
                updateExperienceChips();
                validateCurrentPage();
            });
            glExperience.addView(rb);
        }
    }

    private void updateExperienceChips() {
        for (int i = 0; i < glExperience.getChildCount(); i++) {
            RadioButton rb = (RadioButton) glExperience.getChildAt(i);
            boolean sel = rb.getText().toString().equals(selectedExperience);
            rb.setBackgroundResource(sel
                    ? R.drawable.bg_chip_selected
                    : R.drawable.bg_chip_unselected);
            rb.setTextColor(getResources().getColor(
                    sel ? R.color.white : R.color.Text_Blue));
            rb.setTypeface(null, sel ? Typeface.BOLD : Typeface.NORMAL);
        }
    }

    // ============================================================
    // WATCHERS
    // ============================================================

    private void setupTextWatchers(int position) {
        TextWatcher w = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { validateCurrentPage(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        switch (position) {
            case 0:
                if (regName != null) regName.addTextChangedListener(w);
                if (regEmail != null) regEmail.addTextChangedListener(w);
                if (regAddress != null) regAddress.addTextChangedListener(w);
                if (regCity != null) regCity.addTextChangedListener(w);
                if (regMobile != null) regMobile.addTextChangedListener(w);
                break;
            case 1:
                if (etExpDesc != null) etExpDesc.addTextChangedListener(w);
                break;
            case 2:
                if (etReferral != null) etReferral.addTextChangedListener(w);
                break;
        }
    }

    // ============================================================
    // VALIDATION
    // ============================================================

    private boolean validateCurrentPage() {
        boolean ok = false;
        switch (currentPage) {
            case 0: ok = validatePersonal(); break;
            case 1: ok = validateProfessional(); break;
            case 2: ok = validateReferral(); break;
        }
        updateNextButtonState(ok);
        return ok;
    }

    private boolean validatePersonal() {
        if (regName == null || regAadhaar == null) return false;
        if (TextUtils.isEmpty(regName.getText())) return false;
        String aadhaar = regAadhaar.getText().toString().trim();
        if (aadhaar.length() != 14) return false;
        if (ccp == null || !ccp.isValidFullNumber()) return false;
        String email = regEmail != null && regEmail.getText() != null
                ? regEmail.getText().toString().trim() : "";
        if (!isValidEmail(email)) return false;
        if (rgGender == null || rgGender.getCheckedRadioButtonId() == -1) return false;
        if (selectedDob == null) return false;
        if (regCity == null || TextUtils.isEmpty(regCity.getText())) return false;
        if (TextUtils.isEmpty(selectedCityId)) return false;
        if (regAddress == null || TextUtils.isEmpty(regAddress.getText())) return false;
        if (encodedPhoto == null) return false;
        return true;
    }

    private boolean validateProfessional() {
        return !TextUtils.isEmpty(selectedCategory) && !TextUtils.isEmpty(selectedExperience);
    }

    private boolean validateReferral() {
        return true; // optional
    }

    private boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) return false;
        Pattern p = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        return p.matcher(email).matches();
    }

    // ============================================================
    // NAV
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

        if (currentPage == TOTAL_PAGES - 1) btnNext.setText("SUBMIT");
        else btnNext.setText("CONTINUE");

        btnPrevious.setText(currentPage == 0 ? "BACK" : "PREVIOUS");
    }

    private void setupButtons() {
        btnNext.setOnClickListener(v -> {
            if (currentPage == TOTAL_PAGES - 1) {
                submitRegistration();
            } else if (validateCurrentPage()) {
                viewPager.setCurrentItem(currentPage + 1, true);
            } else {
                Toast.makeText(this, "Please fill all required fields",
                        Toast.LENGTH_SHORT).show();
            }
        });

        btnPrevious.setOnClickListener(v -> {
            if (currentPage > 0) viewPager.setCurrentItem(currentPage - 1, true);
            else onBackPressed();
        });
    }

    // ============================================================
    // SUBMIT
    // ============================================================

    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int w = bitmap.getWidth(), h = bitmap.getHeight();
        float ratio = (float) w / (float) h;
        if (ratio > 1) { w = 500; h = (int) (w / ratio); }
        else           { h = 500; w = (int) (h * ratio); }
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, w, h, true);
        resized.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    private void submitRegistration() {
        final String name = regName.getText().toString().trim();
        final String aadhaar = regAadhaar.getText().toString().trim();
        final String mobile = ccp.getFullNumberWithPlus();
        final String email = regEmail.getText().toString().trim();
        final String dob = selectedDob;
        final String city = regCity.getText().toString().trim();
        final String address = regAddress.getText().toString().trim();
        final String referral = etReferral != null
                ? etReferral.getText().toString().trim() : "";
        final String expDesc = etExpDesc != null
                ? etExpDesc.getText().toString().trim() : "";

        String gender = "";
        RadioButton rb = findViewById(rgGender.getCheckedRadioButtonId());
        if (rb != null) gender = rb.getText().toString();
        final String fGender = gender;

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Please wait...");
        pd.setCancelable(false);
        pd.show();

        StringRequest req = new StringRequest(Request.Method.POST, Constant.RegisterUrl,
                response -> {
                    pd.dismiss();
                    Log.d(TAG, "Response: " + response);
                    if (response.contains("Aadhaar number already exists")) {
                        Toast.makeText(this, "This Aadhaar is already registered.",
                                Toast.LENGTH_SHORT).show();
                    } else if (response.contains("Mobile number already exists")) {
                        Toast.makeText(this, "This mobile is already registered.",
                                Toast.LENGTH_SHORT).show();
                    } else if (response.contains("The code is not valid")) {
                        Toast.makeText(this, "Invalid referral code",
                                Toast.LENGTH_SHORT).show();
                    } else if (response.contains("Successfully added user")) {
                        Toast.makeText(this, "Registration Success",
                                Toast.LENGTH_SHORT).show();
                        sendEmail(mobile);
                        Intent i = new Intent(Register.this, LoginActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        finish();
                    } else {
                        Toast.makeText(this, "Registration failed, try again later",
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    pd.dismiss();
                    if (error instanceof TimeoutError) {
                        Toast.makeText(this, "Network timeout", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("name", name);
                p.put("email", email);
                p.put("aadhar_card_no", aadhaar);
                p.put("phone", mobile);
                p.put("date_of_birth", dob);
                p.put("gender", fGender);
                p.put("address", address);
                p.put("city_id", selectedCityId);
                p.put("bounsReferral", referral);
                p.put("profile_photo", encodedPhoto);
                p.put("expert_category", selectedCategory);
                p.put("experience", selectedExperience);
                p.put("experience_desc", expDesc);
                return p;
            }
        };
        req.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue q = Volley.newRequestQueue(this);
        q.add(req);
    }

    private void sendEmail(String mobile) {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest req = new StringRequest(Request.Method.POST, Constant.email,
                response -> {},
                error -> {}) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> p = new HashMap<>();
                p.put("phone", mobile);
                return p;
            }
        };
        queue.add(req);
    }

    @Override
    public void onBackPressed() {
        if (currentPage > 0) viewPager.setCurrentItem(currentPage - 1, true);
        else super.onBackPressed();
    }
}