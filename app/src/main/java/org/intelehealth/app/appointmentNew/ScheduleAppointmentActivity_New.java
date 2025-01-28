package org.intelehealth.app.appointmentNew;

import static com.google.common.base.Preconditions.checkArgument;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.LocaleList;
import android.text.Html;
import android.util.DisplayMetrics;

import org.intelehealth.app.utilities.CustomLog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codeglo.coyamore.data.PreferenceHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.appointment.api.ApiClientAppointment;
import org.intelehealth.app.appointment.dao.AppointmentDAO;
import org.intelehealth.app.appointment.model.BookAppointmentRequest;
import org.intelehealth.app.appointment.model.SlotInfo;
import org.intelehealth.app.appointment.model.SlotInfoResponse;
import org.intelehealth.app.horizontalcalendar.CalendarModel;
import org.intelehealth.app.horizontalcalendar.HorizontalCalendarViewAdapter;
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.NavigationUtils;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.NetworkUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UuidGenerator;
import org.intelehealth.app.utilities.exception.DAOException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;

public class ScheduleAppointmentActivity_New extends BaseActivity implements NetworkUtils.InternetCheckUpdateInterface {
    private static final String TAG = "ScheduleAppointmentActi";
    RecyclerView rvMorningSlots, rvAfternoonSlots, rvEveningSlots;
    RecyclerView rvHorizontalCal;
    int currentMonth;
    int currentYear;
    ImageView ivPrevMonth, ivNextMonth;
    TextView tvSelectedMonthYear, tvPrevSelectedAppDetails, tvTitleReschedule;
    Calendar calendarInstance;
    String yearToCompare = "";
    String monthToCompare = "";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
    private String mSelectedStartDate = "";
    private String mSelectedEndDate = "";
    List<SlotInfo> slotInfoMorningList, slotInfoAfternoonList, slotInfoEveningList;
    Button btnBookAppointment;
    String selectedDateTime = "";
    SlotInfo slotInfoForBookApp;
    int appointmentId;
    String visitUuid;
    String patientUuid;
    String patientName;
    String speciality;
    String openMrsId;
    AlertDialog alertDialog;
    String actionTag = "";
    int requestCode = 0;
    String app_start_date, app_start_time, app_start_day;
    String rescheduleReason;
    NetworkUtils networkUtils;
    ImageView ivIsInternet, ivBackArrow;
    private ObjectAnimator syncAnimator;
    private SessionManager sessionManager;
    private BroadcastReceiver mBroadcastReceiver;
    private boolean isRescheduled = false;

    Set<Integer> broadcasterReceiverStatusMap = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLocale(ScheduleAppointmentActivity_New.this);
        setContentView(R.layout.activity_schedule_appointment_new);
        networkUtils = new NetworkUtils(ScheduleAppointmentActivity_New.this, this);
        sessionManager = new SessionManager(this);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(Color.WHITE);
        mSelectedStartDate = simpleDateFormat.format(new Date());
        mSelectedEndDate = simpleDateFormat.format(new Date());

        View toolbar = findViewById(R.id.toolbar_schedule_appointments);
        TextView tvTitle = toolbar.findViewById(R.id.tv_screen_title_common);
        ivIsInternet = toolbar.findViewById(R.id.imageview_is_internet_common);

        syncAnimator = ObjectAnimator.ofFloat(ivIsInternet, View.ROTATION, 0f, 359f).setDuration(1200);
        syncAnimator.setRepeatCount(ValueAnimator.INFINITE);
        syncAnimator.setInterpolator(new LinearInterpolator());

        tvTitle.setText(getResources().getString(R.string.schedule_appointment));

        initUI();

        //for reschedule appointment as per old flow
        actionTag = getIntent().getStringExtra("actionTag").toLowerCase();
        requestCode = getIntent().getIntExtra("requestCode", 0);
        if (actionTag != null && !actionTag.isEmpty() && actionTag.equals("rescheduleappointment")) {

            tvPrevSelectedAppDetails.setVisibility(View.VISIBLE);
            tvTitleReschedule.setVisibility(View.VISIBLE);


            appointmentId = getIntent().getIntExtra("appointmentId", 0);
            visitUuid = getIntent().getStringExtra("visitUuid");
            patientUuid = getIntent().getStringExtra("patientUuid");
            patientName = getIntent().getStringExtra("patientName");
            speciality = getIntent().getStringExtra("speciality");
            openMrsId = getIntent().getStringExtra("openMrsId");
            app_start_date = getIntent().getStringExtra("app_start_date");
            app_start_time = getIntent().getStringExtra("app_start_time");
            app_start_day = getIntent().getStringExtra("app_start_day");
            rescheduleReason = getIntent().getStringExtra("rescheduleReason");
            String prevDetails = app_start_day + ", " + DateAndTimeUtils.getDateInDDMMMMYYYYFormat(app_start_date) + " " + getResources().getString(R.string.at) + " " + app_start_time;
            tvPrevSelectedAppDetails.setText(prevDetails);
        } else if (actionTag != null && !actionTag.isEmpty() && actionTag.equals("new_schedule")) {
            visitUuid = getIntent().getStringExtra("visitUuid");
            patientUuid = getIntent().getStringExtra("patientUuid");
            patientName = getIntent().getStringExtra("patientName");
            appointmentId = getIntent().getIntExtra("appointmentId", 0);
            openMrsId = getIntent().getStringExtra("openMrsId");
            speciality = getIntent().getStringExtra("speciality");
        }

        if (app_start_date != null && app_start_time != null) {
            isRescheduled = true;
        }

        if (speciality != null) {
            getSlots();

        } else {
            Toast.makeText(this, getResources().getString(R.string.speciality_must_not_null), Toast.LENGTH_SHORT).show();
        }

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Toast.makeText(context, getString(R.string.sync_completed), Toast.LENGTH_SHORT).show();
                CustomLog.v(TAG, "onReceive  flag=  " + mIsPendingForAppointmentSave);
                CustomLog.v(TAG, "onReceive JOB =  " + intent.getIntExtra("JOB", -1));
                if (mIsPendingForAppointmentSave) {
                    mStatusCount = 0;
                    broadcasterReceiverStatusMap.add(intent.getIntExtra("JOB", -1));
                    //sometimes the broadcaster receiver returning same status multipple times
                    //that's why added those values on SET then calculating
                    for (int status : broadcasterReceiverStatusMap) {
                        mStatusCount += status;
                    }
                    //mStatusCount = mStatusCount + intent.getIntExtra("JOB", -1);
                    if (mStatusCount == AppConstants.SYNC_PULL_PUSH_APPOINTMENT_PULL_DATA_DONE) {
                        if (mSyncAlertDialog != null && mSyncAlertDialog.isShowing()) {
                            if (!isFinishing() && !isDestroyed()) {
                                mSyncAlertDialog.dismiss();
                            }
                        }

                        //saving result status to shared pref to handle the result in worse case
                        sessionManager.setAppointmentResult(true);
                        ScheduleAppointmentActivity_New.this.setResult(requestCode);
                        finish();
                    }
                } else {
                    CustomLog.v(TAG, "Sync Done!");
                    if (mSyncAlertDialog != null && mSyncAlertDialog.isShowing()) {
                        if (!isFinishing() && !isDestroyed()) {
                            mSyncAlertDialog.dismiss();
                        }
                    }
                    Intent newIntent = getIntent();
                    newIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(newIntent);
                    overridePendingTransition(0, 0);
                    finish();
                }

                ivIsInternet.clearAnimation();
                syncAnimator.cancel();
            }
        };
        IntentFilter filterSend = new IntentFilter();
        filterSend.addAction(AppConstants.SYNC_NOTIFY_INTENT_ACTION);
        ContextCompat.registerReceiver(
                this,
                mBroadcastReceiver,
                filterSend,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );
    }

    private int mStatusCount = 0;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
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


    private void initUI() {

        rvMorningSlots = findViewById(R.id.rv_morning_time_slots);
        rvAfternoonSlots = findViewById(R.id.rv_afternoon_time_slots);
        rvEveningSlots = findViewById(R.id.rv_evening_time_slots);
        btnBookAppointment = findViewById(R.id.btn_book_appointment);
        btnBookAppointment.setOnClickListener(v -> {
            CustomLog.d(TAG, "initUI: selectedDateTime : " + selectedDateTime);
            if (!selectedDateTime.isEmpty()) {
                bookAppointmentDialog(ScheduleAppointmentActivity_New.this, selectedDateTime);


            } else {
                Toast.makeText(this, getResources().getString(R.string.please_select_time_slot), Toast.LENGTH_SHORT).show();

            }
        });

        ivBackArrow = findViewById(R.id.iv_back_arrow_common);
        ivBackArrow.setOnClickListener(v -> finish());
        rvMorningSlots.setLayoutManager(new GridLayoutManager(this, 3));
        rvAfternoonSlots.setLayoutManager(new GridLayoutManager(this, 3));
        rvEveningSlots.setLayoutManager(new GridLayoutManager(this, 3));

        rvHorizontalCal = findViewById(R.id.rv_horizontal_cal);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        rvHorizontalCal.setLayoutManager(linearLayoutManager);
        ivPrevMonth = findViewById(R.id.iv_prev_month1);
        ivNextMonth = findViewById(R.id.iv_next_month1);
        tvSelectedMonthYear = findViewById(R.id.tv_selected_month_year);
        tvPrevSelectedAppDetails = findViewById(R.id.tv_prev_scheduled_details);
        tvTitleReschedule = findViewById(R.id.tv_title_reschedule);


        calendarInstance = Calendar.getInstance();
        currentMonth = calendarInstance.getActualMaximum(Calendar.MONTH);
        currentYear = calendarInstance.get(Calendar.YEAR);
        monthToCompare = String.valueOf(currentMonth);
        yearToCompare = String.valueOf(currentYear);
        SimpleDateFormat month_date = new SimpleDateFormat("MMMM", Locale.ENGLISH);
        String month_name = month_date.format(calendarInstance.getTime());
        if (sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
            month_name = StringUtils.en__hi_dob(month_name);
        tvSelectedMonthYear.setText(month_name + ", " + currentYear);
        currentMonth = calendarInstance.get(Calendar.MONTH) + 1;
        monthToCompare = String.valueOf(currentMonth);

        if (monthToCompare.equals(String.valueOf(currentMonth)) && yearToCompare.equals(String.valueOf(currentYear))) {
            enableDisablePreviousButton(false);

        } else {
            enableDisablePreviousButton(true);

        }
        getAllDatesOfSelectedMonth(calendarInstance, true, String.valueOf(currentMonth), String.valueOf(currentYear), String.valueOf(currentMonth));

        ivNextMonth.setOnClickListener(v -> {
            //get next months dates for horizontal calendar view
            getNextMonthDates();
        });
        ivPrevMonth.setOnClickListener(v -> {
            //get this months dates for horizontal calendar view

            getPreviousMonthDates();
        });

    }


    private void getSlots() {

        findViewById(R.id.tv_morning_label).setVisibility(View.GONE);
        findViewById(R.id.rv_morning_time_slots).setVisibility(View.GONE);

        findViewById(R.id.tv_afternoon_label).setVisibility(View.GONE);
        findViewById(R.id.rv_afternoon_time_slots).setVisibility(View.GONE);

        findViewById(R.id.tv_evening_label).setVisibility(View.GONE);
        findViewById(R.id.rv_evening_time_slots).setVisibility(View.GONE);

        //findViewById(R.id.empty_tv).setVisibility(View.GONE);
        findViewById(R.id.tv_time_slot_title).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.empty_tv)).setText(getString(R.string.loading_slots));
        //api for get appointment slots for selected date and doctor speciality
        PreferenceHelper helper = new PreferenceHelper(this);
        String token = "Bearer " + helper.getString(org.intelehealth.klivekit.data.PreferenceHelper.AUTH_TOKEN);
        String baseurl = BuildConfig.SERVER_URL + ":3004";
        ApiClientAppointment.getInstance(baseurl).getApi().getSlots(token, mSelectedStartDate, mSelectedEndDate, speciality).enqueue(new Callback<SlotInfoResponse>() {
            @Override
            public void onResponse(Call<SlotInfoResponse> call, retrofit2.Response<SlotInfoResponse> response) {
                SlotInfoResponse slotInfoResponse = response.body();

                List<SlotInfo> slotInfoList = new ArrayList<>();
                slotInfoMorningList = new ArrayList<>();
                slotInfoAfternoonList = new ArrayList<>();
                slotInfoEveningList = new ArrayList<>();

                if (isRescheduled && slotInfoResponse != null) {
                    removePreviousAppointmentDateTime(slotInfoResponse);
                }


                if (slotInfoResponse != null) slotInfoList.addAll(slotInfoResponse.getDates());

                for (int i = 0; i < slotInfoList.size(); i++) {
                    SlotInfo slotInfo = slotInfoList.get(i);
                    if (!slotInfo.getSlotTime().isEmpty() && slotInfo.getSlotTime().contains(" ")) {
                        String[] splitedTime = slotInfo.getSlotTime().split(" ");
                        if (splitedTime[1].trim().equals("AM")) {

                            slotInfoMorningList.add(slotInfo);
                        }

                    }
                }

                for (int i = 0; i < slotInfoList.size(); i++) {
                    SlotInfo slotInfo = slotInfoList.get(i);
                    if (!slotInfo.getSlotTime().isEmpty() && slotInfo.getSlotTime().contains(" ")) {
                        String[] splitedTime = slotInfo.getSlotTime().split(" ");
                        double appointmentTime;
                        if (splitedTime[1].trim().equals("PM")) {
                            if (splitedTime[0].contains(":")) {
                                String time = splitedTime[0].replace(":", ".");
                                appointmentTime = Double.parseDouble(time);

                            } else {
                                appointmentTime = Double.parseDouble(splitedTime[0]);
                            }
                            if ((appointmentTime >= 1 && appointmentTime <= 6) || appointmentTime >= 12) {
                                slotInfoAfternoonList.add(slotInfo);

                            } else {
                                slotInfoEveningList.add(slotInfo);

                            }

                        }

                    }

                }

                // sort data
                sortByTime(slotInfoMorningList);
                sortByTime(slotInfoAfternoonList);
                sortByTime(slotInfoAfternoonList);

                boolean isSlotNotAvailable = true;

                findViewById(R.id.tv_morning_label).setVisibility(slotInfoMorningList.isEmpty() ? View.GONE : View.VISIBLE);
                findViewById(R.id.rv_morning_time_slots).setVisibility(slotInfoMorningList.isEmpty() ? View.GONE : View.VISIBLE);
                setDataForMorningAppointments(slotInfoMorningList);
                isSlotNotAvailable = slotInfoMorningList.isEmpty();

                findViewById(R.id.tv_afternoon_label).setVisibility(slotInfoAfternoonList.isEmpty() ? View.GONE : View.VISIBLE);
                findViewById(R.id.rv_afternoon_time_slots).setVisibility(slotInfoAfternoonList.isEmpty() ? View.GONE : View.VISIBLE);
                setDataForAfternoonAppointments(slotInfoAfternoonList);
                if (isSlotNotAvailable) isSlotNotAvailable = slotInfoAfternoonList.isEmpty();

                findViewById(R.id.tv_evening_label).setVisibility(slotInfoEveningList.isEmpty() ? View.GONE : View.VISIBLE);
                findViewById(R.id.rv_evening_time_slots).setVisibility(slotInfoEveningList.isEmpty() ? View.GONE : View.VISIBLE);
                setDataForEveningAppointments(slotInfoEveningList);
                if (isSlotNotAvailable) isSlotNotAvailable = slotInfoEveningList.isEmpty();

                ((TextView) findViewById(R.id.empty_tv)).setText(getString(R.string.slot_empty_message));
                findViewById(R.id.empty_tv).setVisibility(isSlotNotAvailable ? View.VISIBLE : View.GONE);
                findViewById(R.id.tv_time_slot_title).setVisibility(isSlotNotAvailable ? View.GONE : View.VISIBLE);

            }

            @Override
            public void onFailure(Call<SlotInfoResponse> call, Throwable t) {
                CustomLog.v("onFailure", t.getMessage());
                //CustomLog out operation if response code is 401
                new NavigationUtils().logoutOperation(ScheduleAppointmentActivity_New.this, t);
            }
        });

    }

    private void removePreviousAppointmentDateTime(SlotInfoResponse slotInfoResponse) {
        List<SlotInfo> slots = slotInfoResponse.getDates();
        slots.removeIf(slotInfo -> slotInfo.getSlotDate().equalsIgnoreCase(app_start_date) && slotInfo.getSlotTime().equalsIgnoreCase(app_start_time));
        slotInfoResponse.setDates(slots);
    }

    private void sortByTime(List<SlotInfo> slotInfoList) {
        Collections.sort(slotInfoList, new Comparator<SlotInfo>() {
            @Override
            public int compare(SlotInfo t1, SlotInfo t2) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
                Date x = null;
                Date y = null;
                try {
                    x = simpleDateFormat.parse(t1.getSlotTime());
                    y = simpleDateFormat.parse(t2.getSlotTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                return x.compareTo(y);
            }
        });
    }

    private void setDataForAfternoonAppointments(List<SlotInfo> slotInfoList) {
        PickUpTimeSlotsAdapter slotListingAdapter = new PickUpTimeSlotsAdapter(ScheduleAppointmentActivity_New.this, slotInfoList, "afternoon", new PickUpTimeSlotsAdapter.OnItemSelection() {
            @Override
            public void onSelect(SlotInfo slotInfo) {
                String result = getDayOfMonthSuffix(slotInfo.getSlotDate());
                selectedDateTime = result + " " + getResources().getString(R.string.at) + " " + slotInfo.getSlotTime();

                slotInfoForBookApp = slotInfo;
                setDataForMorningAppointments(slotInfoMorningList);
                setDataForEveningAppointments(slotInfoEveningList);

            }
        });
        rvAfternoonSlots.setAdapter(slotListingAdapter);

    }

    private void setDataForEveningAppointments(List<SlotInfo> slotInfoList) {
        PickUpTimeSlotsAdapter slotListingAdapter = new PickUpTimeSlotsAdapter(ScheduleAppointmentActivity_New.this, slotInfoList, "evening", new PickUpTimeSlotsAdapter.OnItemSelection() {
            @Override
            public void onSelect(SlotInfo slotInfo) {
                String result = getDayOfMonthSuffix(slotInfo.getSlotDate());
                selectedDateTime = result + " " + getResources().getString(R.string.at) + " " + slotInfo.getSlotTime();

                slotInfoForBookApp = slotInfo;

                setDataForAfternoonAppointments(slotInfoAfternoonList);
                setDataForMorningAppointments(slotInfoMorningList);

            }
        });
        rvEveningSlots.setAdapter(slotListingAdapter);

    }

    private void setDataForMorningAppointments(List<SlotInfo> slotInfoList) {
        PickUpTimeSlotsAdapter slotListingAdapter = new PickUpTimeSlotsAdapter(ScheduleAppointmentActivity_New.this, slotInfoList, "morning", new PickUpTimeSlotsAdapter.OnItemSelection() {
            @Override
            public void onSelect(SlotInfo slotInfo) {
                slotInfoForBookApp = slotInfo;
                String result = getDayOfMonthSuffix(slotInfo.getSlotDate());
                selectedDateTime = result + " " + getResources().getString(R.string.at) + " " + slotInfo.getSlotTime();
                setDataForAfternoonAppointments(slotInfoAfternoonList);
                setDataForEveningAppointments(slotInfoEveningList);

            }
        });
        rvMorningSlots.setAdapter(slotListingAdapter);


    }

    private void getAllDatesOfSelectedMonth(Calendar calendar, boolean isCurrentMonth, String selectedMonth, String selectedYear, String selectedMonthForDays) {

        int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int currentDay;
        if (isCurrentMonth) {
            currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        } else {
            currentDay = 1;
        }
        int daysLeft = lastDay - currentDay;

        CalendarModel calendarModel;
        SimpleDateFormat inFormat = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat outFormat = new SimpleDateFormat("EEEE");

        List<CalendarModel> listOfDates = new ArrayList<>();
        for (int i = currentDay; i <= lastDay; i++) {

            try {
                String inputDate = i + "-" + selectedMonthForDays + "-" + selectedYear;
                Date date = inFormat.parse(inputDate);
                if (date != null) {
                    String dayForDate = outFormat.format(date);
                    String dayForDateFinal = dayForDate.substring(0, 3);

                    if (i == currentDay) {
                        calendarModel = new CalendarModel(dayForDateFinal, i, currentDay, true, selectedMonth, selectedYear, false, selectedMonthForDays);

                    } else {
                        calendarModel = new CalendarModel(dayForDateFinal, i, currentDay, false, selectedMonth, selectedYear, false, selectedMonthForDays);

                    }

                    listOfDates.add(calendarModel);

                } else {
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        //  HorizontalCalendarViewAdapter horizontalCalendarViewAdapter = new HorizontalCalendarViewAdapter(this, listOfDates,this);

        rvHorizontalCal.setAdapter(new HorizontalCalendarViewAdapter(this, listOfDates, calendarModel1 -> {
            int date = calendarModel1.getDate();
            String month = calendarModel1.getSelectedMonthForDays();
            String year = calendarModel1.getSelectedYear();
            mSelectedStartDate = date + "/" + month + "/" + year;
            mSelectedEndDate = date + "/" + month + "/" + year;
            getSlots();

        }));

    }

    private void getPreviousMonthDates() {
        calendarInstance.add(Calendar.MONTH, -1);
        Calendar nowCalendar = Calendar.getInstance();
        if (nowCalendar.get(Calendar.YEAR) <= calendarInstance.get(Calendar.YEAR) && nowCalendar.get(Calendar.MONTH) > calendarInstance.get(Calendar.MONTH)) {
            calendarInstance.add(Calendar.MONTH, 1);
            enableDisablePreviousButton(false);
            return;
        }
        Date monthNameNEw = calendarInstance.getTime();
        Date date = null;
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy", Locale.ENGLISH);
        try {
            date = formatter.parse(monthNameNEw.toString());
            String formateDate = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(date);

            String[] dateSplit = formateDate.split("/");
            yearToCompare = dateSplit[2];
            monthToCompare = dateSplit[1];
            String[] monthYear = DateAndTimeUtils.getMonthAndYearFromGivenDate(formateDate);

            if (monthYear.length > 0) {
                String selectedPrevMonth = monthYear[0];
                String selectedPrevMonthYear = monthYear[1];
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
                    selectedPrevMonth = StringUtils.en__hi_dob(selectedPrevMonth);
                tvSelectedMonthYear.setText(selectedPrevMonth + ", " + selectedPrevMonthYear);
                if (calendarInstance.get(Calendar.MONTH) + 1 == currentMonth && calendarInstance.get(Calendar.YEAR) == currentYear) {
                    enableDisablePreviousButton(false);

                    getAllDatesOfSelectedMonth(calendarInstance, true, monthToCompare, selectedPrevMonthYear, monthToCompare);

                } else {
                    enableDisablePreviousButton(true);

                    getAllDatesOfSelectedMonth(calendarInstance, false, monthToCompare, selectedPrevMonthYear, monthToCompare);

                }

            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void getNextMonthDates() {
        enableDisablePreviousButton(true);

        calendarInstance.add(Calendar.MONTH, 1);
        Date monthNameNEw = calendarInstance.getTime();
        Date date = null;
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy", Locale.ENGLISH);
        try {
            date = formatter.parse(monthNameNEw.toString());
            String formateDate = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(date);
            String[] monthYear = DateAndTimeUtils.getMonthAndYearFromGivenDate(formateDate);
            String selectedNextMonth;
            String selectedMonthYear;

            if (monthYear.length > 0) {
                selectedNextMonth = monthYear[0];
                selectedMonthYear = monthYear[1];
                String[] dateSplit = formateDate.split("/");
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
                    selectedNextMonth = StringUtils.en__hi_dob(selectedNextMonth);
                tvSelectedMonthYear.setText(selectedNextMonth + ", " + selectedMonthYear);
                getAllDatesOfSelectedMonth(calendarInstance, calendarInstance.get(Calendar.MONTH) + 1 == currentMonth && calendarInstance.get(Calendar.YEAR) == currentYear, selectedNextMonth, selectedMonthYear, dateSplit[1]);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void enableDisablePreviousButton(boolean wantToEnable) {
        //for enable and disable previous month button if month is less than current month
        if (wantToEnable) {
            ivPrevMonth.setEnabled(true);
            ivPrevMonth.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);

        } else {
            ivPrevMonth.setEnabled(false);
            ivPrevMonth.setColorFilter(ContextCompat.getColor(this, R.color.font_black_3), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }


    public interface OnItemSelection {
        public void onSelect(SlotInfo slotInfo);
    }

    public void bookAppointmentDialog(Context context, String info) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.dialog_book_appointment_dialog_ui2, null);
        alertdialogBuilder.setView(convertView);
        TextView tvInfo = convertView.findViewById(R.id.tv_info_dialog_app);
        Button noButton = convertView.findViewById(R.id.button_no_appointment);
        Button yesButton = convertView.findViewById(R.id.btn_yes_appointment);
        String infoText = getResources().getString(R.string.sure_to_book_appointment, selectedDateTime);
        if (sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
            infoText = StringUtils.en__hi_dob(infoText);
        tvInfo.setText(Html.fromHtml(infoText));

        alertDialog = alertdialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg);
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

        yesButton.setOnClickListener(v -> {
            bookAppointment();
            alertDialog.dismiss();
        });

        noButton.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    private void bookAppointment() {
        BookAppointmentRequest request = new BookAppointmentRequest();
        if (appointmentId != 0) {
            request.setAppointmentId(appointmentId);
            request.setReason(rescheduleReason);
        }
        request.setUuid(new UuidGenerator().UuidGenerator());
        request.setSlotDay(slotInfoForBookApp.getSlotDay());
        request.setSlotDate(slotInfoForBookApp.getSlotDate());
        request.setSlotDuration(slotInfoForBookApp.getSlotDuration());
        request.setSlotDurationUnit(slotInfoForBookApp.getSlotDurationUnit());
        request.setSlotTime(slotInfoForBookApp.getSlotTime());
        request.setSpeciality(slotInfoForBookApp.getSpeciality());
        request.setUserUuid(slotInfoForBookApp.getUserUuid());
        request.setDrName(slotInfoForBookApp.getDrName());
        request.setVisitUuid(visitUuid);
        request.setPatientName(patientName);
        request.setPatientId(patientUuid);
        request.setOpenMrsId(openMrsId);
        request.setLocationUuid(new SessionManager(ScheduleAppointmentActivity_New.this).getLocationUuid());
        request.setHwUUID(new SessionManager(ScheduleAppointmentActivity_New.this).getProviderID()); // user id / healthworker id
        Gson gson = new Gson();
        AppointmentDAO appointmentDAO = new AppointmentDAO();
        try {
            appointmentDAO.insertAppointmentToDb(request);
        } catch (DAOException e) {
            e.printStackTrace();
        }
        if (NetworkConnection.isOnline(getApplication())) {
            mSyncAlertDialog = new DialogUtils().showCommonLoadingDialog(this, getResources().getString(R.string.booking_appointment), getResources().getString(R.string.please_wait));
            final Handler handler = new Handler();
            handler.postDelayed(() -> {
                SyncUtils syncUtils = new SyncUtils();
                boolean isSynced = syncUtils.syncForeground("scheduleAppointment");
                mIsPendingForAppointmentSave = true;
            }, 100);
        } else {
            ScheduleAppointmentActivity_New.this.setResult(requestCode);
            finish();
        }
    }

    private AlertDialog mSyncAlertDialog;
    private boolean mIsPendingForAppointmentSave = false;

    String getDayOfMonthSuffix(String date) {
        String result = "";
        String[] splitedDate = new String[0];
        if (!date.isEmpty()) {
            splitedDate = date.split("/");
            int n = Integer.parseInt(splitedDate[0]);
            checkArgument(n >= 1 && n <= 31, "illegal day of month: " + n);
            if (n >= 11 && n <= 13) {
                result = "th";
            }
            switch (n % 10) {
                case 1:
                    result = "st";
                case 2:
                    result = "nd";
                case 3:
                    result = "rd";
                default:
                    result = "th";
            }
        }
        String[] resultMonth = DateAndTimeUtils.getMonthAndYearFromGivenDate(date);
        String finalDate = splitedDate[0] + result + " " + resultMonth[0];
        return finalDate;
    }

    //update ui as per internet availability
    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        if (isInternetAvailable) {
            ivIsInternet.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ui2_ic_internet_available));

        } else {
            ivIsInternet.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ui2_ic_no_internet));

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            //unregister receiver for internet check
            networkUtils.unregisterNetworkReceiver();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        //register receiver for internet check
        networkUtils.callBroadcastReceiver();

    }

    public void syncNow(View view) {
        if (NetworkConnection.isOnline(this)) {
            new SyncUtils().syncBackground();
            //Toast.makeText(this, getString(R.string.sync_strated), Toast.LENGTH_SHORT).show();
            ivIsInternet.clearAnimation();
            syncAnimator.start();
        }
    }

}