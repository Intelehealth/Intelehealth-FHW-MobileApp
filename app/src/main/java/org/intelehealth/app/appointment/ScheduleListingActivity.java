package org.intelehealth.app.appointment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.R;
import org.intelehealth.app.appointment.adapter.SlotListingAdapter;
import org.intelehealth.app.appointment.api.ApiClientAppointment;
import org.intelehealth.app.appointment.dao.AppointmentDAO;
import org.intelehealth.app.appointment.model.AppointmentDetailsResponse;
import org.intelehealth.app.appointment.model.BookAppointmentRequest;
import org.intelehealth.app.appointment.model.SlotInfo;
import org.intelehealth.app.appointment.model.SlotInfoResponse;
import org.intelehealth.app.appointment.utils.MyDatePicker;
import org.intelehealth.app.models.auth.ResponseChecker;
import org.intelehealth.app.utilities.NavigationUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.webrtc.activity.BaseActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleListingActivity extends BaseActivity implements DatePickerDialog.OnDateSetListener {

    String visitUuid;
    String patientUuid;
    String patientName;
    String speciality;
    String openMrsId;
    private TextView mDateTextView;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

    private String mSelectedStartDate = "";
    private String mSelectedEndDate = "";
    SessionManager sessionManager;
    private RecyclerView rvSlots;
    int appointmentId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_listing);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.appointment_booking_title);
        appointmentId = getIntent().getIntExtra("appointmentId", 0);
        visitUuid = getIntent().getStringExtra("visitUuid");
        patientUuid = getIntent().getStringExtra("patientUuid");
        patientName = getIntent().getStringExtra("patientName");
        speciality = getIntent().getStringExtra("speciality");
        openMrsId = getIntent().getStringExtra("openMrsId");

        sessionManager = new SessionManager(this);

        mDateTextView = findViewById(R.id.tvDate);
        mSelectedStartDate = simpleDateFormat.format(new Date());
        mSelectedEndDate = simpleDateFormat.format(new Date());
        mDateTextView.setText(mSelectedEndDate);
        TextView specialityTextView = findViewById(R.id.tvSpeciality);
        specialityTextView.setText(speciality);

        if (sessionManager.getAppLanguage().equals("ar")) {
            specialityTextView.setText(StringUtils.getProviderNameInArabic(speciality));
        }

        rvSlots = findViewById(R.id.rvSlots);
        rvSlots.setLayoutManager(new GridLayoutManager(this, 3));
        getSlots();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDateSet(android.widget.DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(year, monthOfYear, dayOfMonth);
        Date date = cal.getTime();

        String dateString = simpleDateFormat.format(date);
        mSelectedStartDate = dateString;
        mSelectedEndDate = dateString;
        mDateTextView.setText(mSelectedEndDate);
        getSlots();
    }


    public void selectDate(View view) {
        MyDatePicker datePicker = new MyDatePicker();
        datePicker.show(getSupportFragmentManager(), "DATE PICK");

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

    private boolean isAuthorised(Response<AppointmentDetailsResponse> response) {
        int responseCode = response.code();
        return responseCode != 401;
    }

    private void bookAppointment(SlotInfo slotInfo) {
        String authHeader = "Bearer " + sessionManager.getJwtAuthToken();

        BookAppointmentRequest request = new BookAppointmentRequest();
        if (appointmentId != 0) {
            request.setAppointmentId(appointmentId);
        }
        request.setSlotDay(slotInfo.getSlotDay());
        request.setSlotDate(slotInfo.getSlotDate());
        request.setSlotDuration(slotInfo.getSlotDuration());
        request.setSlotDurationUnit(slotInfo.getSlotDurationUnit());
        request.setSlotTime(slotInfo.getSlotTime());
        request.setSpeciality(slotInfo.getSpeciality());
        request.setUserUuid(slotInfo.getUserUuid());
        request.setDrName(slotInfo.getDrName());
        request.setVisitUuid(visitUuid);
        request.setPatientName(patientName);
        request.setPatientId(patientUuid);
        request.setOpenMrsId(openMrsId);
        request.setLocationUuid(new SessionManager(ScheduleListingActivity.this).getLocationUuid());
        request.setHwUUID(new SessionManager(ScheduleListingActivity.this).getProviderID()); // user id / healthworker id

        //doing the below changes for the new push packet created for sila.swaida.org server
        if (appointmentId == 0) {
            request.setReason("Patient needs consultation");
        } else {
            request.setReason("Patient not available");
        }

        String baseurl = BuildConfig.SERVER_URL + ":3004";
        String url = baseurl + (appointmentId == 0 ? "/api/appointment/bookAppointment" : "/api/appointment/rescheduleAppointment");
        ApiClientAppointment.getInstance(baseurl).getApi()
                .bookAppointment(url, request, authHeader)
                .enqueue(new Callback<AppointmentDetailsResponse>() {
                    @Override
                    public void onResponse(Call<AppointmentDetailsResponse> call, retrofit2.Response<AppointmentDetailsResponse> response) {
                        ResponseChecker<AppointmentDetailsResponse> responseChecker = new ResponseChecker<>(response);
                        if (responseChecker.isNotAuthorized()) {
                            sessionManager.setJwtAuthToken(null);
                            NavigationUtils navigationUtils = new NavigationUtils();
                            navigationUtils.triggerSignOutOn401Response(ScheduleListingActivity.this);
                            return;
                        }

                        AppointmentDetailsResponse appointmentDetailsResponse = response.body();

                        if (appointmentDetailsResponse == null || !appointmentDetailsResponse.isStatus()) {
                            Toast.makeText(ScheduleListingActivity.this, getString(R.string.appointment_booked_failed), Toast.LENGTH_SHORT).show();
                            getSlots();
                        } else {
                            Toast.makeText(ScheduleListingActivity.this, getString(R.string.appointment_booked_successfully), Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        }

                    }

                    @Override
                    public void onFailure(Call<AppointmentDetailsResponse> call, Throwable t) {
                        Log.v("onFailure", t.getMessage());
                        Toast.makeText(ScheduleListingActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void getSlots() {
        String authHeader = "Bearer " + sessionManager.getJwtAuthToken();

        String baseurl = BuildConfig.SERVER_URL + ":3004";
        ApiClientAppointment.getInstance(baseurl).getApi()
                .getSlots(mSelectedStartDate, mSelectedEndDate, speciality, authHeader)
                .enqueue(new Callback<SlotInfoResponse>() {
                    @Override
                    public void onResponse(Call<SlotInfoResponse> call, retrofit2.Response<SlotInfoResponse> response) {
                        ResponseChecker<SlotInfoResponse> responseChecker = new ResponseChecker<>(response);
                        if (responseChecker.isNotAuthorized()) {
                            sessionManager.setJwtAuthToken(null);
                            NavigationUtils navigationUtils = new NavigationUtils();
                            navigationUtils.triggerSignOutOn401Response(ScheduleListingActivity.this);
                            return;
                        }

                        SlotInfoResponse slotInfoResponse = response.body();
                        if (slotInfoResponse == null) {
                            findViewById(R.id.llEmptyView).setVisibility(View.VISIBLE);
                        } else {
                            findViewById(R.id.llEmptyView).setVisibility(View.GONE);
                            SlotListingAdapter slotListingAdapter = new SlotListingAdapter(rvSlots,
                                    ScheduleListingActivity.this,
                                    slotInfoResponse.getDates(), new SlotListingAdapter.OnItemSelection() {
                                @Override
                                public void onSelect(SlotInfo slotInfo) {
                                    //------before reschedule need to cancel appointment----
                                    AppointmentDAO appointmentDAO = new AppointmentDAO();
                                    appointmentDAO.deleteAppointmentByVisitId(visitUuid);
                                    bookAppointment(slotInfo);

                                }
                            });
                            rvSlots.setAdapter(slotListingAdapter);

                            if (slotListingAdapter.getItemCount() == 0) {
                                findViewById(R.id.llEmptyView).setVisibility(View.VISIBLE);
                            } else {
                                findViewById(R.id.llEmptyView).setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<SlotInfoResponse> call, Throwable t) {
                        Log.v("onFailure", t.getMessage());
                    }
                });

    }

}