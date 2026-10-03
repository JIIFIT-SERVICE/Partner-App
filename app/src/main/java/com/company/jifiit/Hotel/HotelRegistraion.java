package com.company.jifiit.Hotel;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.company.jifiit.Constant;
import com.company.jifiit.Hotel.Adapter.HotelRegistrationAdapter;
import com.company.jifiit.LoginActivity;
import com.company.jifiit.R;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.textfield.TextInputEditText;
import com.hbb20.CountryCodePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class HotelRegistraion extends AppCompatActivity {

    private static final String TAG = "HotelRegistration";
    private static final int DOCUMENT_PICK_REQUEST_CODE = 100;

    private ViewPager2 viewPager;
    private Button btnNext;
    private Button btnPrevious;
    private TextView tvStep;
    private ProgressBar progressBar;
    private HotelRegistrationAdapter adapter;
    private int currentPage = 0;
    private static final int TOTAL_PAGES = 6;

    // Page 1
    private TextInputEditText etHotelName;
    private TextInputEditText etLegalBusinessName;

    // Page 2
    private RadioGroup rgHotelType;
    private TextInputEditText etHotelMember;
    private TextInputEditText etWhatsAppEmail;

    // Page 3
    private TextInputEditText etEstablishmentYear;
    private TextInputEditText etTotalRooms;
    private TextInputEditText etRoomType;
    private Button btnAddRoom;
    private TextView tvAddedRooms;
    private RangeSlider rsHotelCategory;

    // Page 4
    private TextInputEditText etCity;
    private TextInputEditText etLocality;
    private TextInputEditText etAddress;

    // Page 5
    private TextInputEditText etContactName;
    private TextInputEditText etContactNumber;
    private TextInputEditText etContactEmail;
    private TextInputEditText etDesignation;
    private CountryCodePicker ccpContact;

    // Page 6
    private CheckBox cbAcceptFinal;
    private CheckBox cbAcceptTerms;

    private boolean isLastPageCheckboxInit = false;
    private String selectedCityId = "";
    private String selectedLocalityId = "";
    private String selectedHubId = "";

    private final ArrayList<String> localityNames = new ArrayList<>();
    private final ArrayList<String> localityIds = new ArrayList<>();
    private final ArrayList<String> localityHubIds = new ArrayList<>();

    private Uri selectedDocumentUri = null;

    // Room types stored as objects
    private final ArrayList<JSONObject> roomTypes = new ArrayList<>();


    private static final String[] DESIGNATION_OPTIONS = {
            "Owner",
            "General Manager",
            "Manager",
            "Front Office Manager",
            "GRE"
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_registraion);

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
    }

    private void setupViewPager() {
        adapter = new HotelRegistrationAdapter();
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

                if (position == TOTAL_PAGES - 1) {
                    initCheckboxes();
                } else {
                    isLastPageCheckboxInit = false;
                }
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

        androidx.recyclerview.widget.RecyclerView recyclerView =
                (androidx.recyclerview.widget.RecyclerView) viewPager.getChildAt(0);

        if (recyclerView == null) return null;

        androidx.recyclerview.widget.RecyclerView.ViewHolder holder =
                recyclerView.findViewHolderForAdapterPosition(currentPage);

        return holder != null ? holder.itemView : null;
    }

    private void setupDocumentUpload(View pageView) {
        Button btnUploadDocument = pageView.findViewById(R.id.btnUploadDocument);
        if (btnUploadDocument != null) {
            btnUploadDocument.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                        "application/pdf",
                        "image/jpeg",
                        "image/png",
                        "image/webp"
                });
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                try {
                    startActivityForResult(
                            Intent.createChooser(intent, "Select Legal Document"),
                            DOCUMENT_PICK_REQUEST_CODE
                    );
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(this, "Please install a file manager to upload documents",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DOCUMENT_PICK_REQUEST_CODE
                && resultCode == RESULT_OK
                && data != null) {

            Uri selectedFileUri = data.getData();
            if (selectedFileUri != null) {
                selectedDocumentUri = selectedFileUri;

                View pageView = getCurrentPageView();
                if (pageView != null) {
                    Button btnUploadDocument = pageView.findViewById(R.id.btnUploadDocument);
                    if (btnUploadDocument != null) {
                        btnUploadDocument.setText("Document Selected");
                        btnUploadDocument.setBackgroundTintList(
                                ContextCompat.getColorStateList(this, R.color.DeepShapGreen)
                        );
                        btnUploadDocument.setTextColor(
                                ContextCompat.getColor(this, R.color.white)
                        );
                    }
                }
                validateCurrentPage();
                Toast.makeText(this, "Document selected successfully",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initPageViews(int position) {
        View pageView = getCurrentPageView();
        if (pageView == null) {
            viewPager.postDelayed(() -> initPageViews(position), 50);
            return;
        }

        switch (position) {
            case 0:
                etHotelName = pageView.findViewById(R.id.etHotelName);
                etLegalBusinessName = pageView.findViewById(R.id.etLegalBusinessName);
                break;
            case 1:
                rgHotelType = pageView.findViewById(R.id.rgHotelType);
                etHotelMember = pageView.findViewById(R.id.etHotelMember);
                etWhatsAppEmail = pageView.findViewById(R.id.etWhatsAppEmail);
                break;
            case 2:
                etEstablishmentYear = pageView.findViewById(R.id.etEstablishmentYear);
                etTotalRooms = pageView.findViewById(R.id.etTotalRooms);
                etRoomType = pageView.findViewById(R.id.etRoomType);
                btnAddRoom = pageView.findViewById(R.id.btnAddRoom);
                tvAddedRooms = pageView.findViewById(R.id.tvAddedRooms);
                rsHotelCategory = pageView.findViewById(R.id.rsHotelCategory);

                setupDocumentUpload(pageView);

                if (rsHotelCategory != null) {
                    rsHotelCategory.addOnChangeListener((slider, value, fromUser) -> {
                        TextView tvSelectedStar = pageView.findViewById(R.id.tvSelectedStar);
                        if (tvSelectedStar != null) {
                            int starValue = Math.round(value);
                            tvSelectedStar.setText("Selected: ⭐ " + starValue
                                    + " Star" + (starValue > 1 ? "s" : ""));
                        }
                        validateCurrentPage();
                    });
                }
                break;
            case 3:
                etCity = pageView.findViewById(R.id.etCity);
                etLocality = pageView.findViewById(R.id.etLocality);
                etAddress = pageView.findViewById(R.id.etAddress);

                setupCitySelection();
                setupLocalitySelection();
                break;
            case 4:
                etContactName = pageView.findViewById(R.id.etContactName);
                etContactNumber = pageView.findViewById(R.id.etContactNumber);
                etContactEmail = pageView.findViewById(R.id.etContactEmail);
                etDesignation = pageView.findViewById(R.id.etDesignation);

                // ---- CountryCodePicker wiring ----
                ccpContact = pageView.findViewById(R.id.contact_countrycode);

                if (ccpContact != null) {
                    // 2. Set India as default (if not already set in XML)
                    ccpContact.setDefaultCountryUsingNameCode("IN");
                    ccpContact.resetToDefaultCountry();

                    // 3. Set the dialog background to White and text to Black programmatically
                    // This avoids the "attribute not found" XML error
                    ccpContact.setDialogBackgroundColor(Color.WHITE);
                    ccpContact.setDialogTextColor(Color.BLACK);

                    // 4. Link with your EditText
                    if (etContactNumber != null) {
                        ccpContact.registerCarrierNumberEditText(etContactNumber);
                    }
                }

                // Re-validate on phone change
                if (etContactNumber != null) {
                    etContactNumber.addTextChangedListener(new TextWatcher() {
                        @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                        @Override public void afterTextChanged(Editable s) {}
                        @Override
                        public void onTextChanged(CharSequence s, int st, int b, int c) {
                            validateCurrentPage();
                        }
                    });
                }

                if (etDesignation != null) {
                    etDesignation.setOnClickListener(v -> showDesignationDialog());
                }
                break;
            case 5:
                cbAcceptFinal = pageView.findViewById(R.id.cbAcceptFinal);
                setupCheckboxListeners();
                break;
        }
    }

    private void showDesignationDialog() {
        if (etDesignation == null) return;

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Select Designation")
                .setItems(DESIGNATION_OPTIONS, (d, which) -> {
                    etDesignation.setText(DESIGNATION_OPTIONS[which]);
                    validateCurrentPage();
                })
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            // ---- White background with rounded corners ----
            if (dialog.getWindow() != null) {
                GradientDrawable drawable = new GradientDrawable();
                drawable.setColor(Color.WHITE);
                drawable.setCornerRadius(20);
                dialog.getWindow().setBackgroundDrawable(drawable);
            }

            // ---- White title ----
            int titleId = getResources().getIdentifier("alertTitle", "id", "android");
            TextView title = dialog.findViewById(titleId);
            if (title != null) {
                title.setTextColor(Color.BLACK);
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                title.setTypeface(null, Typeface.BOLD);
                title.setPadding(40, 30, 40, 20);
                title.setBackgroundColor(Color.WHITE);
            }

            // ---- White list area + black text ----
            ListView listView = dialog.getListView();
            if (listView != null) {
                listView.setBackgroundColor(Color.WHITE);
                listView.setDivider(new android.graphics.drawable.ColorDrawable(Color.LTGRAY));
                listView.setDividerHeight(1);
                listView.setPadding(0, 10, 0, 10);
            }
        });

        dialog.show();

        // ---- Force white background on the list items (must be after show()) ----
        ListView listView = dialog.getListView();
        if (listView != null) {
            listView.setBackgroundColor(Color.WHITE);
            listView.setCacheColorHint(Color.WHITE);

            listView.setAdapter(new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_list_item_1,
                    DESIGNATION_OPTIONS
            ) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView tv = (TextView) view;
                    tv.setTextColor(Color.BLACK);
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    tv.setPadding(40, 30, 40, 30);
                    tv.setBackgroundColor(Color.WHITE);
                    return view;
                }
            });

            listView.setOnItemClickListener((parent, view, position, id) -> {
                etDesignation.setText(DESIGNATION_OPTIONS[position]);
                validateCurrentPage();
                dialog.dismiss();
            });
        }
    }

    private void setupCitySelection() {
        if (etCity == null) return;

        etCity.setOnClickListener(v -> {
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
                            ArrayList<String> cityNames = new ArrayList<>();
                            ArrayList<String> cityIds = new ArrayList<>();

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
                                        etCity.setText(cityNames.get(which));

                                        selectedLocalityId = "";
                                        selectedHubId = "";
                                        localityNames.clear();
                                        localityIds.clear();
                                        localityHubIds.clear();

                                        if (etLocality != null) etLocality.setText("");

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
                                    listView.setDivider(
                                            new android.graphics.drawable.ColorDrawable(Color.LTGRAY));
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
        });
    }

    private void setupLocalitySelection() {
        if (etLocality == null) return;

        etLocality.setFocusable(false);
        etLocality.setClickable(true);
        etLocality.setCursorVisible(false);

        etLocality.setOnClickListener(v -> {
            if (selectedCityId == null || selectedCityId.trim().isEmpty()) {
                Toast.makeText(this, "Please select city first", Toast.LENGTH_SHORT).show();
                return;
            }

            if (localityNames.isEmpty()) {
                Toast.makeText(this, "No locality available", Toast.LENGTH_SHORT).show();
                return;
            }

            showLocalityDialog();
        });
    }

    private void showLocalityDialog() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(30, 20, 30, 20);

        EditText searchEditText = new EditText(this);
        searchEditText.setHint("Search locality / area...");
        searchEditText.setSingleLine(true);
        searchEditText.setTextSize(15);
        searchEditText.setTextColor(Color.BLACK);
        searchEditText.setHintTextColor(Color.parseColor("#9AA5A1"));
        searchEditText.setPadding(25, 0, 25, 0);

        GradientDrawable searchBackground = new GradientDrawable();
        searchBackground.setColor(Color.parseColor("#F5F5F5"));
        searchBackground.setCornerRadius(15);
        searchEditText.setBackground(searchBackground);

        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(52)
        );
        searchParams.setMargins(0, 0, 0, dpToPx(10));
        container.addView(searchEditText, searchParams);

        ListView listView = new ListView(this);
        listView.setDivider(new android.graphics.drawable.ColorDrawable(Color.LTGRAY));
        listView.setDividerHeight(1);
        listView.setVerticalScrollBarEnabled(true);

        container.addView(listView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(350)
        ));

        ArrayAdapter<String> localityAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                new ArrayList<>(localityNames)
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(Color.BLACK);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                textView.setPadding(40, 25, 40, 25);
                return view;
            }
        };

        listView.setAdapter(localityAdapter);

        AlertDialog localityDialog = new AlertDialog.Builder(this)
                .setTitle("Select Locality / Area")
                .setView(container)
                .create();

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchText = s.toString().trim().toLowerCase();
                localityAdapter.clear();

                for (String locality : localityNames) {
                    if (locality.toLowerCase().contains(searchText)) {
                        localityAdapter.add(locality);
                    }
                }
                localityAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = localityAdapter.getItem(position);
            if (selectedName == null) return;

            int originalIndex = localityNames.indexOf(selectedName);
            if (originalIndex >= 0) {
                selectedLocalityId = localityIds.get(originalIndex);
                selectedHubId = localityHubIds.get(originalIndex);
                etLocality.setText(selectedName);
                validateCurrentPage();
            }
            localityDialog.dismiss();
        });

        localityDialog.setOnShowListener(dialogInterface -> {
            if (localityDialog.getWindow() != null) {
                GradientDrawable drawable = new GradientDrawable();
                drawable.setColor(Color.WHITE);
                drawable.setCornerRadius(20);
                localityDialog.getWindow().setBackgroundDrawable(drawable);
            }

            int titleId = getResources().getIdentifier("alertTitle", "id", "android");
            TextView title = localityDialog.findViewById(titleId);
            if (title != null) {
                title.setTextColor(Color.BLACK);
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                title.setTypeface(null, Typeface.BOLD);
                title.setPadding(40, 30, 40, 15);
            }
        });

        localityDialog.show();

        if (localityDialog.getWindow() != null) {
            localityDialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }

    private void toggleArrowDirection(boolean isOpen) {
        if (etCity == null) return;

        Drawable arrowDrawable;
        if (isOpen) {
            arrowDrawable = ContextCompat.getDrawable(this, R.drawable.arrow_up);
        } else {
            arrowDrawable = ContextCompat.getDrawable(this, R.drawable.arrow_down);
        }

        if (arrowDrawable != null) {
            etCity.setCompoundDrawablesWithIntrinsicBounds(null, null, arrowDrawable, null);
            DrawableCompat.setTint(arrowDrawable, ContextCompat.getColor(this, R.color.gray));
        }
    }

    private void loadDataByCityId(String cityId) {
        if (cityId == null || cityId.trim().isEmpty()) return;

        String url = Constant.get_localities + "?city_id=" + Uri.encode(cityId);
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        boolean status = response.getBoolean("status");

                        localityNames.clear();
                        localityIds.clear();
                        localityHubIds.clear();
                        selectedLocalityId = "";
                        selectedHubId = "";

                        if (etLocality != null) etLocality.setText("");

                        if (!status) {
                            Toast.makeText(this, "No locality available", Toast.LENGTH_SHORT).show();
                            validateCurrentPage();
                            return;
                        }

                        JSONArray data = response.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject locality = data.getJSONObject(i);
                            localityIds.add(locality.getString("id"));
                            localityNames.add(locality.getString("name"));
                            localityHubIds.add(locality.getString("hub_id"));
                        }

                        if (localityNames.isEmpty()) {
                            Toast.makeText(this, "No locality available for this city",
                                    Toast.LENGTH_SHORT).show();
                        }

                        validateCurrentPage();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error loading locality", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Failed to load locality", Toast.LENGTH_SHORT).show();
                }
        );

        request.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(request);
    }

    private void setupTextWatchers(int position) {
        switch (position) {
            case 0:
                if (etHotelName != null) etHotelName.addTextChangedListener(createTextWatcher());
                if (etLegalBusinessName != null)
                    etLegalBusinessName.addTextChangedListener(createTextWatcher());
                break;
            case 1:
                if (etHotelMember != null) etHotelMember.addTextChangedListener(createTextWatcher());
                if (etWhatsAppEmail != null) etWhatsAppEmail.addTextChangedListener(createTextWatcher());
                if (rgHotelType != null) {
                    rgHotelType.setOnCheckedChangeListener((group, checkedId) -> validateCurrentPage());
                }
                break;
            case 2:
                if (etEstablishmentYear != null)
                    etEstablishmentYear.addTextChangedListener(createTextWatcher());
                if (etTotalRooms != null) etTotalRooms.addTextChangedListener(createTextWatcher());
                if (btnAddRoom != null) btnAddRoom.setOnClickListener(v -> addRoomType());
                break;
            case 3:
                if (etLocality != null) etLocality.addTextChangedListener(createTextWatcher());
                if (etAddress != null) etAddress.addTextChangedListener(createTextWatcher());
                break;
            case 4:
                if (etContactName != null) etContactName.addTextChangedListener(createTextWatcher());
                // Contact number watcher already attached in initPageViews via CCP
                if (etContactEmail != null) etContactEmail.addTextChangedListener(createTextWatcher());
                if (etDesignation != null) etDesignation.addTextChangedListener(createTextWatcher());
                break;
        }
    }

    private void addRoomType() {
        String roomType = getTextFromEditText(etRoomType);
        String totalRoomsText = getTextFromEditText(etTotalRooms);

        if (TextUtils.isEmpty(totalRoomsText)) {
            Toast.makeText(this, "Please enter total rooms first", Toast.LENGTH_SHORT).show();
            return;
        }

        int totalRooms;
        try {
            totalRooms = Integer.parseInt(totalRoomsText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid total room number",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (roomTypes.size() >= totalRooms) {
            Toast.makeText(this, "You can add only " + totalRooms + " room types",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(roomType)) {
            Toast.makeText(this, "Please enter a room type", Toast.LENGTH_SHORT).show();
            return;
        }

//        if (roomType.matches("\\d+")) {
//            Toast.makeText(this, "Room type cannot be only numbers",
//                    Toast.LENGTH_SHORT).show();
//            return;
//        }

        JSONObject roomObj = new JSONObject();
        try {
            roomObj.put("room_number", roomType);
            roomObj.put("description", roomType);
            roomObj.put("is_ac", 1);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        roomTypes.add(roomObj);

        StringBuilder display = new StringBuilder();
        for (int i = 0; i < roomTypes.size(); i++) {
            if (i > 0) display.append(", ");
            display.append(roomTypes.get(i).optString("room_number"));
        }
        tvAddedRooms.setText(display.toString());

        etRoomType.setText("");
        validateCurrentPage();
    }

    private TextWatcher createTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateCurrentPage();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
    }

    private void initCheckboxes() {
        View pageView = getCurrentPageView();
        if (pageView != null) {
            cbAcceptFinal = pageView.findViewById(R.id.cbAcceptFinal);
            if (!isLastPageCheckboxInit) {
                setupCheckboxListeners();
                isLastPageCheckboxInit = true;
            }
            validateCurrentPage();
        } else {
            viewPager.postDelayed(this::initCheckboxes, 100);
        }
    }

    private void setupCheckboxListeners() {
        if (cbAcceptFinal != null) {
            cbAcceptFinal.setOnCheckedChangeListener((buttonView, isChecked) -> validateCurrentPage());
        }
    }

    private boolean validateCurrentPage() {
        boolean isValid = false;

        switch (currentPage) {
            case 0: isValid = validateBusinessDetailsPage(); break;
            case 1: isValid = validateLegalContactPage(); break;
            case 2: isValid = validateHotelDetailsPage(); break;
            case 3: isValid = validateRoomLocationPage(); break;
            case 4: isValid = validateContactPersonPage(); break;
            case 5: isValid = validateTermsPage(); break;
        }

        updateNextButtonState(isValid);
        return isValid;
    }

    private boolean validateBusinessDetailsPage() {
        String hotelName = getTextFromEditText(etHotelName);
        String legalBusinessName = getTextFromEditText(etLegalBusinessName);

        if (TextUtils.isEmpty(hotelName)) return false;
        if (TextUtils.isEmpty(legalBusinessName)) return false;
        return true;
    }

    private boolean validateLegalContactPage() {
        if (rgHotelType == null || rgHotelType.getCheckedRadioButtonId() == -1) return false;

        String hotelMember = getTextFromEditText(etHotelMember);
        String email = getTextFromEditText(etWhatsAppEmail);

        if (TextUtils.isEmpty(hotelMember)) return false;
        if (TextUtils.isEmpty(email)) return false;
        if (!isValidEmail(email)) return false;
        return true;
    }

    private boolean validateHotelDetailsPage() {
        String yearStr = getTextFromEditText(etEstablishmentYear);
        String roomsStr = getTextFromEditText(etTotalRooms);

        if (TextUtils.isEmpty(yearStr)) return false;

        try {
            int year = Integer.parseInt(yearStr);
            int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            if (year < 1900 || year > currentYear) return false;
        } catch (NumberFormatException e) {
            return false;
        }

        if (TextUtils.isEmpty(roomsStr)) return false;

        try {
            int rooms = Integer.parseInt(roomsStr);
            if (rooms < 1) return false;
        } catch (NumberFormatException e) {
            return false;
        }

        return !roomTypes.isEmpty();
    }

    private boolean validateRoomLocationPage() {
        String city = getTextFromEditText(etCity);
        String locality = getTextFromEditText(etLocality);
        String address = getTextFromEditText(etAddress);

        if (TextUtils.isEmpty(city)) return false;
        if (TextUtils.isEmpty(locality)) return false;
        if (TextUtils.isEmpty(address)) return false;
        return true;
    }

    private boolean validateContactPersonPage() {
        String name        = getTextFromEditText(etContactName);
        String email       = getTextFromEditText(etContactEmail);
        String designation = getTextFromEditText(etDesignation);

        if (TextUtils.isEmpty(name))        return false;
        if (TextUtils.isEmpty(email))       return false;
        if (!isValidEmail(email))           return false;
        if (TextUtils.isEmpty(designation)) return false;

        // ---- CountryCodePicker validation ----
        if (ccpContact == null || etContactNumber == null) return false;

        String raw = getTextFromEditText(etContactNumber);
        if (TextUtils.isEmpty(raw)) return false;

        // Validate against selected country's rules
        if (!ccpContact.isValidFullNumber()) return false;

        return true;
    }

    private boolean validateTermsPage() {
        return cbAcceptFinal != null && cbAcceptFinal.isChecked();
    }

    private String getTextFromEditText(TextInputEditText editText) {
        if (editText == null) return "";
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) return false;
        Pattern emailPattern = Pattern.compile(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        );
        return emailPattern.matcher(email).matches();
    }

    private void updateNextButtonState(boolean isValid) {
        if (btnNext != null) {
            if (isValid) {
                btnNext.setEnabled(true);
                btnNext.setBackgroundColor(Color.parseColor("#2196F3"));
                btnNext.setTextColor(Color.WHITE);
            } else {
                btnNext.setEnabled(false);
                btnNext.setBackgroundColor(Color.parseColor("#BDBDBD"));
                btnNext.setTextColor(Color.parseColor("#757575"));
            }
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

        if (currentPage == 0) {
            btnPrevious.setText("BACK");
        } else {
            btnPrevious.setText("PREVIOUS");
        }
    }

    private void setupButtons() {
        btnNext.setOnClickListener(v -> {
            if (currentPage == TOTAL_PAGES - 1) {
                if (validateTermsPage()) {
                    submitRegistration();
                }
            } else {
                if (validateCurrentPage()) {
                    viewPager.setCurrentItem(currentPage + 1, true);
                } else {
                    Toast.makeText(this, "Please fill all required fields",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnPrevious.setOnClickListener(v -> {
            if (currentPage > 0) {
                viewPager.setCurrentItem(currentPage - 1, true);
            } else {
                onBackPressed();
                overridePendingTransition(
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                );
            }
        });
    }

    // ---- copy Uri to cache dir so we can send it as a File ----
    private File copyUriToCacheFile(Uri uri) {
        try {
            android.content.ContentResolver cr = getContentResolver();
            String mime = cr.getType(uri);
            String ext = "dat";
            if (mime != null) {
                if (mime.contains("pdf")) ext = "pdf";
                else if (mime.contains("png")) ext = "png";
                else if (mime.contains("jpeg") || mime.contains("jpg")) ext = "jpg";
                else if (mime.contains("webp")) ext = "webp";
            }

            File out = new File(getCacheDir(),
                    "hotel_doc_" + System.currentTimeMillis() + "." + ext);

            InputStream is = cr.openInputStream(uri);
            if (is == null) return null;

            FileOutputStream fos = new FileOutputStream(out);
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) != -1) fos.write(buf, 0, n);
            is.close();
            fos.close();
            return out;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void submitRegistration() {
        if (!validateTermsPage()) {
            Toast.makeText(this, "Please accept the terms and conditions",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnNext.setEnabled(false);
        btnNext.setText("SUBMITTING...");

        String hotelName = getTextFromEditText(etHotelName);
        String legalBusinessName = getTextFromEditText(etLegalBusinessName);

        String hotelType = "";
        if (rgHotelType != null && rgHotelType.getCheckedRadioButtonId() != -1) {
            RadioButton selectedHotelType = rgHotelType.findViewById(
                    rgHotelType.getCheckedRadioButtonId());
            if (selectedHotelType != null) {
                hotelType = selectedHotelType.getText().toString().trim();
            }
        }
        String hotelMember = getTextFromEditText(etHotelMember);
        String whatsAppEmail = getTextFromEditText(etWhatsAppEmail);

        String establishmentYear = getTextFromEditText(etEstablishmentYear);
        String totalRooms = getTextFromEditText(etTotalRooms);

        String hotelCategory = "";
        if (rsHotelCategory != null && !rsHotelCategory.getValues().isEmpty()) {
            hotelCategory = String.valueOf(Math.round(rsHotelCategory.getValues().get(0)));
        }

        String city = getTextFromEditText(etCity);
        String locality = getTextFromEditText(etLocality);
        String address = getTextFromEditText(etAddress);

        String contactName = getTextFromEditText(etContactName);
        String contactEmail = getTextFromEditText(etContactEmail);
        String designation = getTextFromEditText(etDesignation);

        // ---- Extract phone (from EditText) + country code (from CCP) ----
        String contactNumber = getTextFromEditText(etContactNumber);
        String countryCode = "91";
        if (ccpContact != null) {
            // getSelectedCountryCode() returns dialing code without "+" e.g. "91"
            countryCode = ccpContact.getSelectedCountryCode();
        }

        boolean acceptedTerms = cbAcceptFinal != null && cbAcceptFinal.isChecked();

        JSONArray roomTypesArray = new JSONArray();
        for (JSONObject room : roomTypes) roomTypesArray.put(room);

        JSONObject payload = new JSONObject();
        try {
            payload.put("hotel_name", hotelName);
            payload.put("legal_business_name", legalBusinessName);
            payload.put("hotel_type", hotelType);
            payload.put("hotel_member", hotelMember);
            payload.put("whatsapp_email", whatsAppEmail);
            payload.put("establishment_year", establishmentYear);
            payload.put("total_rooms", totalRooms);
            payload.put("room_types", roomTypesArray);
            payload.put("hotel_category", hotelCategory);
            payload.put("city_id", selectedCityId);
            payload.put("city", city);
            payload.put("locality_id", selectedLocalityId);
            payload.put("locality", locality);
            payload.put("hub_id", selectedHubId);
            payload.put("address", address);
            payload.put("contact_name", contactName);
            payload.put("contact_number", contactNumber);
            payload.put("country_code", countryCode);
            payload.put("contact_email", contactEmail);
            payload.put("designation", designation);
            payload.put("accepted_terms", acceptedTerms ? 1 : 0);

            Log.d(TAG, "Request JSON: " + payload);
        } catch (JSONException e) {
            e.printStackTrace();
            btnNext.setEnabled(true);
            btnNext.setText("SUBMIT");
            Toast.makeText(this, "Failed to prepare registration data",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> stringParams = new HashMap<>();
        stringParams.put("data", payload.toString());

        Map<String, File> fileParams = new HashMap<>();
        if (selectedDocumentUri != null) {
            File tmp = copyUriToCacheFile(selectedDocumentUri);
            if (tmp != null) fileParams.put("document", tmp);
        }

        String url = Constant.hotel_registration;
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        MultipartRequest request = new MultipartRequest(
                url,
                stringParams,
                fileParams,
                response -> {
                    Log.d(TAG, "Response: " + response);
                    try {
                        JSONObject obj = new JSONObject(response);
                        boolean status = obj.optBoolean("status");
                        String message = obj.optString("message", "Done");

                        Toast.makeText(HotelRegistraion.this, message,
                                Toast.LENGTH_LONG).show();

                        if (status) {
                            Intent intent = new Intent(HotelRegistraion.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            btnNext.setEnabled(true);
                            btnNext.setText("SUBMIT");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        btnNext.setEnabled(true);
                        btnNext.setText("SUBMIT");
                        Toast.makeText(HotelRegistraion.this,
                                "Invalid server response", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    btnNext.setEnabled(true);
                    btnNext.setText("SUBMIT");

                    String msg = "Unable to submit registration";
                    if (error.networkResponse != null) {
                        msg = "Server error: " + error.networkResponse.statusCode;
                    }
                    Toast.makeText(HotelRegistraion.this, msg, Toast.LENGTH_LONG).show();
                }
        );

        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(request);
    }

    @Override
    public void onBackPressed() {
        if (currentPage > 0) {
            viewPager.setCurrentItem(currentPage - 1, true);
        } else {
            super.onBackPressed();
            overridePendingTransition(
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
            );
        }
    }
}