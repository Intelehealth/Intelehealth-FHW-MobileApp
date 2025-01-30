package org.intelehealth.app.activities.followuppatients;

import static org.intelehealth.app.database.dao.PatientsDAO.phoneNumber;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.LocaleList;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import org.intelehealth.app.utilities.CustomLog;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.activities.notification.usecase.DeleteLocalNotificationUseCase;
import org.intelehealth.app.activities.onboarding.PrivacyPolicyActivity_New;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.enums.DataLoadingType;
import org.intelehealth.app.enums.FollowupFilterTypeEnum;
import org.intelehealth.app.models.FollowUpModel;
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.ToastUtil;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import timber.log.Timber;

/**
 * Created by Prajwal Waingankar on 21/08/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
public class FollowUpPatientActivity_New extends BaseActivity {
    public static final String TAG = FollowUpPatientActivity_New.class.getName();
    RecyclerView rv_today, rv_tomorrow, rv_month;
    FollowUpPatientAdapter_New todaysAdapter, tomorrowsAdapter, othersAdapter;
    SessionManager sessionManager = null;
    private SQLiteDatabase db;
    private int offset = 0;
    private EncounterDAO encounterDAO;
    TextView toolbar_title, today_nodata, week_nodata, month_nodata;
    ImageButton refresh;
    int totalCounts = 0, totalCounts_today = 0, totalCounts_week = 0, totalCounts_month = 0;
    private EditText searchview_received;
    private ImageView closeButton;
    private Context context = FollowUpPatientActivity_New.this;
    private RelativeLayout no_patient_found_block;
    private LinearLayout main_block;
    List<FollowUpModel> todays_modelList, weeks_modelList, months_modelList;
    private List<FollowUpModel> todaysFollowUpDates = new ArrayList<>();
    private List<FollowUpModel> tomorrowssFollowUpDates = new ArrayList<>();
    private List<FollowUpModel> finalMonthsFollowUpDates = new ArrayList<>();
    ImageView filterIm, sortIm;
    FrameLayout filterFrameLay;
    TextView filterDateTv, filterStartDateTv, filterEndDateTv, tvResultsFor, othersTitle;
    Button applyBtDate, applyBtRange, dateBt, rangeBt;
    LinearLayout dateLayout, rangeLayout;
    HorizontalScrollView scrollChips;
    private List<FilterOptionsModel> filtersList;
    private boolean isChipInit = false;
    private FollowupFilterTypeEnum filterType = FollowupFilterTypeEnum.NONE;
    private boolean sortStatus = true;//true= ascending, false = descending
    private RelativeLayout parentLay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.follow_up_visits);

        handleBackPress();

        sessionManager = new SessionManager(this);
        String language = sessionManager.getAppLanguage();
        //In case of crash still the app should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());
        db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();

        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }

        initViews();
        followup_data();
        getIntentData();

        refresh.setOnClickListener(v -> {
            resetList(false);
            followup_data();
            Toast.makeText(this, getResources().getString(R.string.refreshed_successfully), Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * deleting followup notification after click on the item
     */
    private void getIntentData() {
        try {
            String uuid = getIntent().getStringExtra("uuid");
            if (uuid != null) {
                new DeleteLocalNotificationUseCase().deleteNotification(uuid);
            }
        } catch (Exception e) {
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(setLocale(newBase));
    }

    public Context setLocale(Context context) {
        SessionManager sessionManager1 = new SessionManager(context);
        String appLanguage = sessionManager1.getAppLanguage();
        Resources res = context.getResources();
        Configuration conf = res.getConfiguration();
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        conf.setLocale(locale);
        context.createConfigurationContext(conf);
        DisplayMetrics dm = res.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocales(new LocaleList(locale));
        } else {
            conf.locale = locale;
        }
        res.updateConfiguration(conf, dm);
        return context;
    }

    private RelativeLayout mTodayRelativeLayout, mWeekRelativeLayout, mMonthRelativeLayout;
    private NestedScrollView mBodyNestedScrollView;
    private TextView mEmptyTextView;

    private void initViews() {
        mTodayRelativeLayout = findViewById(R.id.rl_today);
        mWeekRelativeLayout = findViewById(R.id.rl_week);
        mMonthRelativeLayout = findViewById(R.id.rl_month);
        mBodyNestedScrollView = findViewById(R.id.nestedscrollview);
        mEmptyTextView = findViewById(R.id.empty_view_tv);

        toolbar_title = findViewById(R.id.toolbar_title);
        today_nodata = findViewById(R.id.today_nodata);
        week_nodata = findViewById(R.id.week_nodata);
        month_nodata = findViewById(R.id.month_nodata);
        rv_today = findViewById(R.id.recycler_today);
        rv_tomorrow = findViewById(R.id.rv_tomorrow);
        rv_month = findViewById(R.id.rv_thismonth);
        refresh = findViewById(R.id.refresh);

        filterIm = findViewById(R.id.filter_im);
        sortIm = findViewById(R.id.sort_im);
        filterFrameLay = findViewById(R.id.filter_frame);

        tvResultsFor = findViewById(R.id.tv_results_for);
        othersTitle = findViewById(R.id.others_title);

        filterDateTv = filterFrameLay.findViewById(R.id.tv_date);
        filterStartDateTv = filterFrameLay.findViewById(R.id.tv_start_date);
        filterEndDateTv = filterFrameLay.findViewById(R.id.tv_end_date);

        applyBtDate = filterFrameLay.findViewById(R.id.apply_date_bt);
        applyBtRange = filterFrameLay.findViewById(R.id.apply_range_bt);

        dateBt = filterFrameLay.findViewById(R.id.button_date);
        rangeBt = filterFrameLay.findViewById(R.id.button_range);

        dateLayout = filterFrameLay.findViewById(R.id.layout_date);
        rangeLayout = filterFrameLay.findViewById(R.id.layout_range);
        parentLay = findViewById(R.id.parent_lay);

        scrollChips = findViewById(R.id.scroll_chips);

        if (isChipInit) {
            tvResultsFor.setVisibility(View.VISIBLE);
            scrollChips.setVisibility(View.VISIBLE);
        } else {
            tvResultsFor.setVisibility(View.GONE);
            scrollChips.setVisibility(View.GONE);
        }

        toolbar_title.setText(new StringBuilder()
                .append(getString(R.string.label))
                .append(" (")
                .append(PatientsDAO.getAllFollowupPatientCount())
                .append(")"));

        ImageButton ibButtonBack = findViewById(R.id.vector);

        searchview_received = findViewById(R.id.searchview_received);
        closeButton = findViewById(R.id.iv_clear);
        no_patient_found_block = findViewById(R.id.no_patient_found_block);
        main_block = findViewById(R.id.main_block);
        ((TextView) findViewById(R.id.search_pat_hint_txt)).setText(getString(R.string.empty_message_for_patinet_search_visit_screen));
        LinearLayout addPatientTV = findViewById(R.id.add_new_patientTV);
        addPatientTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PrivacyPolicyActivity_New.class);
                intent.putExtra("intentType", "navigateFurther");
                intent.putExtra("add_patient", "add_patient");
                startActivity(intent);
                finish();
            }
        });

        ibButtonBack.setOnClickListener(v -> {
            Intent intent = new Intent(FollowUpPatientActivity_New.this, HomeScreenActivity_New.class);
            startActivity(intent);
        });

        clickListeners();

    }

    void clickListeners() {

        parentLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissFilterLayout();
            }
        });

        parentLay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    dismissFilterLayout();
                    view.performClick();
                    return false;
                }
                return false;
            }
        });

        dateBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dateLayout.setVisibility(View.VISIBLE);
                rangeLayout.setVisibility(View.GONE);
                dateBt.setBackground(ContextCompat.getDrawable(context, R.drawable.button_bg_forgot_pass_ui2));
                rangeBt.setBackground(ContextCompat.getDrawable(context, R.drawable.button_bg_forgot_pass_disabled_ui2));
            }
        });

        rangeBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dateLayout.setVisibility(View.GONE);
                rangeLayout.setVisibility(View.VISIBLE);
                dateBt.setBackground(ContextCompat.getDrawable(context, R.drawable.button_bg_forgot_pass_disabled_ui2));
                rangeBt.setBackground(ContextCompat.getDrawable(context, R.drawable.button_bg_forgot_pass_ui2));
            }
        });

        applyBtRange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateRange()) {
                    resetList(true);
                    resetData();
                    dismissFilterLayout();
                    filterType = FollowupFilterTypeEnum.RANGE;
                    setFiltersToTheGroup(new FilterOptionsModel("range", filterStartDateTv.getText().toString() + " - " + filterEndDateTv.getText().toString()));
                } else {
                    ToastUtil.showShortToast(FollowUpPatientActivity_New.this, getString(R.string.select_date_to_apply_filter));
                }
            }
        });

        applyBtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateDate()) {
                    resetList(true);
                    resetData();
                    dismissFilterLayout();
                    filterType = FollowupFilterTypeEnum.DATE;
                    setFiltersToTheGroup(new FilterOptionsModel("date", filterDateTv.getText().toString()));
                } else {
                    ToastUtil.showShortToast(FollowUpPatientActivity_New.this, getString(R.string.select_date_to_apply_filter));
                }
            }
        });

        filterStartDateTv.setOnClickListener(v -> selectDate(filterStartDateTv, "startDate"));
        filterEndDateTv.setOnClickListener(v -> selectDate(filterEndDateTv, "endDate"));
        filterDateTv.setOnClickListener(v -> selectDate(filterDateTv, ""));

        mBodyNestedScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    dismissFilterLayout();
                    return false;
                }
                return false;
            }
        });


        searchview_received.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().equalsIgnoreCase("")) {
                    searchview_received.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.blue_border_bg));
                } else {
                    searchview_received.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ui2_common_input_bg));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().isEmpty()) {
                    closeButton.setVisibility(View.VISIBLE);
                } else {
                    closeButton.setVisibility(View.GONE);
                }
            }
        });

        searchview_received.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    dismissFilterLayout();
                    resetList(false);
                    fetchAndSegregateData(DataLoadingType.INITIAL);
                }
                return false;
            }
        });

        searchview_received.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    dismissFilterLayout();
                    view.performClick();
                    return false;
                }
                return false;
            }
        });

        closeButton.setOnClickListener(v -> {
            dismissFilterLayout();
            no_patient_found_block.setVisibility(View.GONE);
            main_block.setVisibility(View.VISIBLE);
            resetData();
            searchview_received.setText("");
            resetList(false);
            fetchAndSegregateData(DataLoadingType.INITIAL);
        });

        filterIm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filterFrameLay.getVisibility() == View.VISIBLE) {
                    filterFrameLay.setVisibility(View.GONE);
                    filterIm.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ui2_ic_filter_bg));
                } else {
                    filterFrameLay.setVisibility(View.VISIBLE);
                    filterIm.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ui2_ic_filter_border_bg));
                }
            }
        });

        sortIm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortIm.setEnabled(false);
                sortStatus = !sortStatus;
                filterFrameLay.setVisibility(View.GONE);
                filterIm.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ui2_ic_filter_bg));
                resetList(false);
                fetchAndSegregateData(DataLoadingType.INITIAL);
                if (sortStatus) {
                    ToastUtil.showShortToast(FollowUpPatientActivity_New.this, ContextCompat.getString(FollowUpPatientActivity_New.this, R.string.sorted_by_ascending_order));
                } else {
                    ToastUtil.showShortToast(FollowUpPatientActivity_New.this, ContextCompat.getString(FollowUpPatientActivity_New.this, R.string.sorted_by_descending_order));
                }

                //protecting multiple click at a time
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sortIm.setEnabled(true);
                    }
                }, 500);
            }
        });
    }


    private void dismissFilterLayout() {
        if (filterFrameLay.getVisibility() == View.VISIBLE) {
            filterFrameLay.setVisibility(View.GONE);
            filterIm.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ui2_ic_filter_bg));
        }
    }

    private void resetList(boolean resetFilterRequired) {
        if (resetFilterRequired) {
            filterType = FollowupFilterTypeEnum.NONE;
        }
        todaysFollowUpDates.clear();
        tomorrowssFollowUpDates.clear();
        finalMonthsFollowUpDates.clear();
        try {
            todaysAdapter.notifyDataSetChanged();
            tomorrowsAdapter.notifyDataSetChanged();
            othersAdapter.notifyDataSetChanged();
        }catch (Exception e){}
    }

    private boolean validateDate() {
        return !filterDateTv.getText().toString().isEmpty();
    }

    private boolean validateRange() {
        return !filterStartDateTv.getText().toString().isEmpty() && !filterEndDateTv.getText().toString().isEmpty();
    }

    private void setFiltersToTheGroup(FilterOptionsModel inputModel) {
        filtersList = new ArrayList<>();
        filtersList.add(inputModel);

        if (filtersList.size() > 0) {
            scrollChips.setVisibility(View.VISIBLE);
            if (tvResultsFor != null) {
                tvResultsFor.setVisibility(View.VISIBLE);
            }
        }

        fetchAndSegregateData(DataLoadingType.INITIAL);

        ChipGroup chipGroup = findViewById(R.id.chipgroup_filter);
        isChipInit = true;
        chipGroup.removeAllViews();
        for (int index = 0; index < filtersList.size(); index++) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_custom_ui2, chipGroup, false);
            FilterOptionsModel filterOptionsModel = filtersList.get(index);
            String tagName = filterOptionsModel.getFilterValue();
            String tagName1 = filterOptionsModel.getFilterValue();
            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                tagName1 = StringUtils.en_hi_dob_updated(tagName);
            }else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                tagName1 = StringUtils.en__ru_dob(tagName);
            }
            int paddingDp = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 10f, getResources().getDisplayMetrics());
            chip.setText(tagName1);
            chip.setCloseIconVisible(true);
            chip.setBackground(ContextCompat.getDrawable(this, R.drawable.ui2_ic_selcted_chip_bg));
            chipGroup.addView(chip);
            chip.setOnCloseIconClickListener(view -> {
                filtersList.remove(filterOptionsModel);
                chipGroup.removeView(chip);
                if (filtersList != null && filtersList.size() == 0) {
                    scrollChips.setVisibility(View.GONE);
                    if (tvResultsFor != null) {
                        tvResultsFor.setVisibility(View.GONE);
                    }
                }
                resetList(true);
                resetData();
                fetchAndSegregateData(DataLoadingType.INITIAL);
            });
        }
    }


    private void selectDate(TextView textView, String value) {
        String date = textView.getText().toString();
        Calendar calendar = DateAndTimeUtils.convertStringToCalendarObject(date, "dd MMM, yyyy", sessionManager.getAppLanguage());

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.datepicker, (datePicker, year, month, day) -> {
            Calendar newDate = Calendar.getInstance();
            newDate.set(Calendar.YEAR, year);
            newDate.set(Calendar.MONTH, month);
            newDate.set(Calendar.DAY_OF_MONTH, day);

            Date selectedDate = newDate.getTime();
            textView.setText(DateAndTimeUtils.convertDateObjectToString(selectedDate, "dd MMM, yyyy"));
            //fetchAndSetUIData();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        DatePicker datePicker = datePickerDialog.getDatePicker();
        if (value.equalsIgnoreCase("startDate")) {
            if (!filterEndDateTv.getText().toString().isEmpty()) {
                Calendar maxDateforStartCal = DateAndTimeUtils.convertStringToCalendarObject(filterEndDateTv.getText().toString(), "dd MMM, yyyy", sessionManager.getAppLanguage());
                datePicker.setMaxDate(maxDateforStartCal.getTimeInMillis());
            }
        } else if (value.equalsIgnoreCase("endDate")) {
            if (!filterStartDateTv.getText().toString().isEmpty()) {
                Calendar minDateForEndCal = DateAndTimeUtils.convertStringToCalendarObject(filterStartDateTv.getText().toString(), "dd MMM, yyyy", sessionManager.getAppLanguage());
                datePicker.setMinDate(minDateForEndCal.getTimeInMillis());
            }
        }
        datePickerDialog.show();
    }

    private void resetData() {
        todaysAdapter = new FollowUpPatientAdapter_New(todaysFollowUpDates, this);
        rv_today.setNestedScrollingEnabled(false);
        rv_today.setAdapter(todaysAdapter);

        tomorrowsAdapter = new FollowUpPatientAdapter_New(tomorrowssFollowUpDates, this);
        rv_tomorrow.setNestedScrollingEnabled(false);
        rv_tomorrow.setAdapter(tomorrowsAdapter);

        othersAdapter = new FollowUpPatientAdapter_New(finalMonthsFollowUpDates, this);
        rv_month.setNestedScrollingEnabled(false);
        rv_month.setAdapter(othersAdapter);
    }


    private void followup_data() {
        fetchAndSegregateData(DataLoadingType.INITIAL);
    }

    private void fetchAndSegregateData(DataLoadingType dataLoadingType) {
        AlertDialog commonLoadingDialog;
        if(dataLoadingType == DataLoadingType.INITIAL){
            commonLoadingDialog = new DialogUtils().showCommonLoadingDialog(this, getString(R.string.loading), "");
            commonLoadingDialog.show();
        }else {
            commonLoadingDialog = null;
            ToastUtil.showShortToast(this,getString(R.string.loading));
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            List<FollowUpModel> initialFollowUpPatients = getAllPatientsFromDB();
            if (initialFollowUpPatients.isEmpty()) {
                if(dataLoadingType == DataLoadingType.INITIAL){
                    commonLoadingDialog.dismiss();
                }
                runOnUiThread(() -> shouldShowNoDataTextViewForAllRecyclerViews(true));
            } else {
                if (filterType != FollowupFilterTypeEnum.NONE) {
                    finalMonthsFollowUpDates.addAll(initialFollowUpPatients);
                    runOnUiThread(() -> shouldShowNoDataTextViewForAllRecyclerViews(false));
                    runOnUiThread(() -> {
                        mTodayRelativeLayout.setVisibility(View.GONE);
                        mWeekRelativeLayout.setVisibility(View.GONE);
                        othersTitle.setVisibility(View.GONE);
                        setMonthsDatesInRecyclerView(finalMonthsFollowUpDates);
                        if(dataLoadingType == DataLoadingType.INITIAL){
                            commonLoadingDialog.dismiss();
                        }
                    });
                } else {
                    finalMonthsFollowUpDates.addAll(initialFollowUpPatients);
                    runOnUiThread(() -> {
                        mTodayRelativeLayout.setVisibility(View.VISIBLE);
                        mWeekRelativeLayout.setVisibility(View.VISIBLE);
                        othersTitle.setVisibility(View.VISIBLE);
                        shouldShowNoDataTextViewForAllRecyclerViews(false);
                    });
                    getChiefComplaint(initialFollowUpPatients);
                    todaysFollowUpDates.addAll(getTodaysVisitsFromList(initialFollowUpPatients));
                    initialFollowUpPatients.removeAll(todaysFollowUpDates);
                    tomorrowssFollowUpDates.addAll(getTomorrowsVisitsFromList(initialFollowUpPatients));
                    finalMonthsFollowUpDates.removeAll(todaysFollowUpDates);
                    finalMonthsFollowUpDates.removeAll(tomorrowssFollowUpDates);
                    runOnUiThread(() -> {
                        setTodaysDatesInRecyclerView(todaysFollowUpDates);
                        setTomorrowsDatesInRecyclerView(tomorrowssFollowUpDates);
                        setMonthsDatesInRecyclerView(finalMonthsFollowUpDates);
                        if(dataLoadingType == DataLoadingType.INITIAL){
                            commonLoadingDialog.dismiss();
                        }
                    });
                }
            }
        });
    }

    private void setTodaysDatesInRecyclerView(List<FollowUpModel> todaysFollowUpDates) {
        if (todaysFollowUpDates.isEmpty()) {
            today_nodata.setVisibility(View.VISIBLE);
        } else {
            today_nodata.setVisibility(View.GONE);
            if (rv_today.getVisibility() == View.GONE) {
                rv_today.setVisibility(View.VISIBLE);
            }
            todaysAdapter = new FollowUpPatientAdapter_New(todaysFollowUpDates, this);
            rv_today.setNestedScrollingEnabled(false);
            rv_today.setAdapter(todaysAdapter);
        }
    }

    private void setTomorrowsDatesInRecyclerView(List<FollowUpModel> tomorrowssFollowUpDates) {
        if (tomorrowssFollowUpDates.isEmpty()) {
            week_nodata.setVisibility(View.VISIBLE);
        } else {
            week_nodata.setVisibility(View.GONE);
            if (rv_tomorrow.getVisibility() == View.GONE) {
                rv_tomorrow.setVisibility(View.VISIBLE);
            }
            tomorrowsAdapter = new FollowUpPatientAdapter_New(tomorrowssFollowUpDates, this);
            rv_tomorrow.setNestedScrollingEnabled(false);
            rv_tomorrow.setAdapter(tomorrowsAdapter);
        }
    }

    private void setMonthsDatesInRecyclerView(List<FollowUpModel> monthFollowUpDates) {
        if (monthFollowUpDates.isEmpty()) {
            month_nodata.setVisibility(View.VISIBLE);
        } else {
            month_nodata.setVisibility(View.GONE);
            if (rv_month.getVisibility() == View.GONE) {
                rv_month.setVisibility(View.VISIBLE);
            }
            othersAdapter = new FollowUpPatientAdapter_New(monthFollowUpDates, this);
            rv_month.setNestedScrollingEnabled(false);
            rv_month.setAdapter(othersAdapter);
        }
    }

    private List<FollowUpModel> getTomorrowsVisitsFromList(List<FollowUpModel> followUpList) {
        List<FollowUpModel> tomorrowsFollowUpList = new ArrayList<>();
        Date todayssDate = DateAndTimeUtils.getCurrentDateWithoutTime();
        Calendar c = Calendar.getInstance();
        c.setTime(todayssDate);
        c.add(Calendar.DAY_OF_MONTH, 1);
        Date tomorrowsDate = c.getTime();

        for (FollowUpModel followUpModel : followUpList) {
            String followUpDate = followUpModel.getFollowup_date().substring(0, 10);
            String tomorrowsDateStr = new SimpleDateFormat("yyyy-MM-dd").format(tomorrowsDate);
            if (tomorrowsDateStr.equals(followUpDate)) {
                tomorrowsFollowUpList.add(followUpModel);
            }
        }
        return tomorrowsFollowUpList;
    }

    private List<FollowUpModel> getTodaysVisitsFromList(List<FollowUpModel> followUpList) {
        List<FollowUpModel> todaysFollowUpList = new ArrayList<>();
        String todaysDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        for (FollowUpModel followUpModel : followUpList) {
            String followUpDate = followUpModel.getFollowup_date().substring(0, 10);
            if (todaysDate.equals(followUpDate)) {
                todaysFollowUpList.add(followUpModel);
            }
        }
        return todaysFollowUpList;
    }

    private void shouldShowNoDataTextViewForAllRecyclerViews(boolean isVisible) {
        if (todaysFollowUpDates.isEmpty()) {
            today_nodata.setVisibility(View.VISIBLE);
            rv_today.setVisibility(View.GONE);
        } else {
            today_nodata.setVisibility(View.GONE);
            rv_today.setVisibility(View.VISIBLE);
        }
        if (tomorrowssFollowUpDates.isEmpty()) {
            week_nodata.setVisibility(View.VISIBLE);
            rv_tomorrow.setVisibility(View.GONE);
        } else {
            week_nodata.setVisibility(View.GONE);
            rv_tomorrow.setVisibility(View.VISIBLE);
        }
        if (finalMonthsFollowUpDates.isEmpty()) {
            month_nodata.setVisibility(View.VISIBLE);
            rv_month.setVisibility(View.GONE);
        } else {
            month_nodata.setVisibility(View.GONE);
            rv_month.setVisibility(View.VISIBLE);
        }

    }


    private List<FollowUpModel> getChiefComplaint(List<FollowUpModel> followUpModels) {
        for (int i = 0; i < followUpModels.size(); i++) {
            if (followUpModels.get(i).getUuid() != null) {
                String visitUUID = followUpModels.get(i).getUuid();
                CustomLog.v("Followup", "visitid: " + visitUUID);
                String complaint_query = "select e.uuid, o.value  from tbl_encounter e, tbl_obs o where " + "e.visituuid = ? " + "and e.encounter_type_uuid = '8d5b27bc-c2cc-11de-8d13-0010c6dffd0f' " + // adult_initial
                        "and e.uuid = o.encounteruuid and o.conceptuuid = '3edb0e09-9135-481e-b8f0-07a26fa9a5ce'"; // chief complaint

                final Cursor cursor = db.rawQuery(complaint_query, new String[]{visitUUID});
                if (cursor.moveToFirst()) {
                    do {
                        try {
                            String complaint = cursor.getString(cursor.getColumnIndexOrThrow("value"));
                            followUpModels.get(i).setChiefComplaint(complaint);
                            CustomLog.v("Followup", "chiefcomplaint: " + complaint);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        }
        return followUpModels;
    }

    public List<FollowUpModel> getAllPatientsFromDB() {
        List<FollowUpModel> modelList = new ArrayList<>();
        String filterQuery = "";
        if (filterType == FollowupFilterTypeEnum.DATE) {
            String date = DateAndTimeUtils.date_formatter(filterDateTv.getText().toString(), "dd MMM, yyyy", "yyyy-MM-dd");
            filterQuery = " and followup_date = '" + date + "' ";
        } else if (filterType == FollowupFilterTypeEnum.RANGE) {
            String startDate = DateAndTimeUtils.date_formatter(filterStartDateTv.getText().toString(), "dd MMM, yyyy", "yyyy-MM-dd");
            String endDate = DateAndTimeUtils.date_formatter(filterEndDateTv.getText().toString(), "dd MMM, yyyy", "yyyy-MM-dd");
            filterQuery = " and (followup_date >= '" + startDate + "' and followup_date <= '" + endDate + "' ) ";
        }
        String middleName = "CASE WHEN b.middle_name IS NOT NULL THEN ' ' || b.middle_name || ' ' ELSE ' ' END";

        String searchQuery = "";
        CharSequence sQuery = searchview_received.getText();
        if (searchview_received.getText() != null && !searchview_received.getText().toString().isEmpty()) {
            searchQuery = "and ((patient_name_new LIKE " + "'%" + sQuery.toString() + "%') OR (b.openmrs_id LIKE " + "'%" + sQuery + "%')) ";
        }

        String sortQuery;
        if (sortStatus) {
            sortQuery = " order by o.value asc ";
        } else {
            sortQuery = " order by o.value desc ";
        }

        String query = "SELECT a.uuid as visituuid,b.first_name || " + middleName + " || b.last_name as patient_name_new, a.sync, a.patientuuid, substr(a.startdate, 1, 10) as startdate, "
                + "date(substr(o.value, 1, 10)) as followup_date, o.value as follow_up_info,"
                + "b.patient_photo, a.enddate, b.uuid, b.first_name, "
                + "b.middle_name, b.last_name, b.date_of_birth, b.openmrs_id, b.gender, c.value AS speciality, SUBSTR(o.value,1,10) AS value_text, MAX(o.obsservermodifieddate) AS obsservermodifieddate "
                + "FROM tbl_visit a, tbl_patient b, tbl_encounter d, tbl_obs o, tbl_visit_attribute c WHERE "
                + "a.uuid = c.visit_uuid AND   " +
                "a.patientuuid = b.uuid AND "
                + "a.uuid = d.visituuid AND d.uuid = o.encounteruuid AND o.conceptuuid = ? "
                + " AND o.voided='0' and "
                + "o.value is NOT NULL GROUP BY a.patientuuid"
                + " HAVING (value_text is NOT NULL AND LOWER(value_text) != 'no' AND value_text != '' ) "
                +filterQuery
                +searchQuery
                + sortQuery;

        Timber.tag("FOLLOWUP_QUERY").d(query);

        final Cursor cursor = db.rawQuery(query, new String[]{UuidDictionary.FOLLOW_UP_VISIT});  //"e8caffd6-5d22-41c4-8d6a-bc31a44d0c86"
        if (cursor.moveToFirst()) {
            do {
                try {
                    // Fetch encounters who have emergency set and udpate modelist.
                    String visitUuid = cursor.getString(cursor.getColumnIndexOrThrow("visituuid"));
                    String value_text = cursor.getString(cursor.getColumnIndexOrThrow("value_text"));
                        //boolean isCompletedExitedSurvey = new EncounterDAO().isCompletedExitedSurvey(visitUuid);
                        //if (isCompletedExitedSurvey) {
                        String emergencyUuid = "";
                        encounterDAO = new EncounterDAO();
                        try {
                            emergencyUuid = encounterDAO.getEmergencyEncounters(visitUuid, encounterDAO.getEncounterTypeUuid("EMERGENCY"));
                        } catch (DAOException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                            emergencyUuid = "";
                        }

                        if (!emergencyUuid.isEmpty() || !emergencyUuid.equalsIgnoreCase("")) { // ie. visit is emergency visit.
                            modelList.add(new FollowUpModel(visitUuid,
                                    cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("first_name")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("middle_name")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")),
                                    StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("uuid")))),
                                    cursor.getString(cursor.getColumnIndexOrThrow("gender")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("startdate")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("speciality")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("follow_up_info")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("sync")),
                                    true, cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("obsservermodifieddate")
                                    ))); // ie. visit is emergency visit.
                        } else {
                            modelList.add(new FollowUpModel( // ie. visit is NOT emergency visit.
                                    cursor.getString(cursor.getColumnIndexOrThrow("visituuid")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("first_name")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("middle_name")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")),
                                    StringUtils.mobileNumberEmpty(phoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("uuid")))),
                                    cursor.getString(cursor.getColumnIndexOrThrow("gender")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("startdate")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("speciality")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("follow_up_info")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("sync")),
                                    false,
                                    cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")),
                                    cursor.getString(cursor.getColumnIndexOrThrow("obsservermodifieddate")))); // ie. visit is NOT emergency visit.
                        }
                } catch (Exception e) {
                    e.printStackTrace();
//                    Toast.makeText(this, "error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        return modelList;
    }

    /**
     * removed onBackPressed function due to deprecation
     * and added this one to handle onBackPressed
     */
    private void handleBackPress() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(context, HomeScreenActivity_New.class);
                startActivity(intent);
            }
        });
    }

    public class FilterOptionsModel {
        private String filterType;
        private String filterValue;

        public FilterOptionsModel(String filterType, String filterValue) {
            this.filterType = filterType;
            this.filterValue = filterValue;
        }

        public String getFilterType() {
            return filterType;
        }

        public void setFilterType(String filterType) {
            this.filterType = filterType;
        }

        public String getFilterValue() {
            return filterValue;
        }

        public void setFilterValue(String filterValue) {
            this.filterValue = filterValue;
        }
    }

}
