package org.intelehealth.app.appointment;

import android.app.DatePickerDialog;
import android.app.Dialog;
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
import android.view.Window;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.appointment.adapter.SlotListingAdapter;
import org.intelehealth.app.appointment.api.ApiClientAppointment;
import org.intelehealth.app.appointment.dao.AppointmentDAO;
import org.intelehealth.app.appointment.model.AppointmentDetailsResponse;
import org.intelehealth.app.appointment.model.BookAppointmentRequest;
import org.intelehealth.app.appointment.model.SlotInfo;
import org.intelehealth.app.appointment.model.SlotInfoResponse;
import org.intelehealth.app.appointment.utils.MyDatePicker;
import org.intelehealth.app.utilities.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class ScheduleListingActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

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

        if (sessionManager.getAppLanguage().equals("or")) {
            if (speciality.equalsIgnoreCase("General Physician")) {
                specialityTextView.setText("ସାଧାରଣ ଚିକିତ୍ସକ");
            }
        }
        if (sessionManager.getAppLanguage().equals("hi")) {
            if (speciality.equalsIgnoreCase("General Physician")) {
                specialityTextView.setText("सामान्य चिकित्सक");
            }
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
//        Locale locale = new Locale(appLanguage);
//        Locale.setDefault(locale);
//        Configuration config = new Configuration();
//        config.locale = locale;
//        getBaseContext().getResources().updateConfiguration(config,getBaseContext().getResources().getDisplayMetrics());
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
    private String mEngReason = "";

    private void askReason(final SlotInfo slotInfo) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.appointment_cancel_reason_view);

        final TextView titleTextView = (TextView) dialog.findViewById(R.id.titleTv);
        titleTextView.setText(getString(R.string.please_select_your_reschedule_reason));
        final EditText reasonEtv = dialog.findViewById(R.id.reasonEtv);
        reasonEtv.setVisibility(View.GONE);
        final RadioGroup optionsRadioGroup = dialog.findViewById(R.id.reasonRG);
        optionsRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbR1) {
                    reasonEtv.setVisibility(View.GONE);
                    reasonEtv.setText(getString(R.string.doctor_is_not_available));
                    mEngReason = "Doctor is not available";
                } else if (checkedId == R.id.rbR2) {
                    reasonEtv.setVisibility(View.GONE);
                    reasonEtv.setText(getString(R.string.patient_is_not_available));
                    mEngReason = "Patient is not available";
                } else if (checkedId == R.id.rbR3) {
                    reasonEtv.setText("");
                    reasonEtv.setVisibility(View.VISIBLE);
                }
            }
        });

        final TextView textView = dialog.findViewById(R.id.submitTV);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                String reason = reasonEtv.getText().toString().trim();
                if (reason.isEmpty()) {
                    Toast.makeText(ScheduleListingActivity.this, getString(R.string.please_enter_reason_txt), Toast.LENGTH_SHORT).show();
                    return;
                }
                bookAppointment(slotInfo, mEngReason.isEmpty() ? reason : mEngReason);
            }
        });

        dialog.show();

    }


    private void bookAppointment(SlotInfo slotInfo, String reason) {
        BookAppointmentRequest request = new BookAppointmentRequest();
        if (appointmentId != 0) {
            request.setAppointmentId(appointmentId);
            request.setReason(reason);
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

        String baseurl = "https://" + new SessionManager(this).getServerUrl() + ":3004";
        String url = baseurl + (appointmentId == 0 ? "/api/appointment/bookAppointment" : "/api/appointment/rescheduleAppointment");
        ApiClientAppointment.getInstance(baseurl).getApi()
                .bookAppointment(url, request)
                .enqueue(new Callback<AppointmentDetailsResponse>() {
                    @Override
                    public void onResponse(Call<AppointmentDetailsResponse> call, retrofit2.Response<AppointmentDetailsResponse> response) {
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

        String baseurl = "https://" + new SessionManager(this).getServerUrl() + ":3004";
        ApiClientAppointment.getInstance(baseurl).getApi()
                .getSlots(mSelectedStartDate, mSelectedEndDate, speciality)
                .enqueue(new Callback<SlotInfoResponse>() {
                    @Override
                    public void onResponse(Call<SlotInfoResponse> call, retrofit2.Response<SlotInfoResponse> response) {
                        SlotInfoResponse slotInfoResponse = response.body();

                        SlotListingAdapter slotListingAdapter = new SlotListingAdapter(rvSlots,
                                ScheduleListingActivity.this,
                                slotInfoResponse.getDates(), new SlotListingAdapter.OnItemSelection() {
                            @Override
                            public void onSelect(SlotInfo slotInfo) {
                                //------before reschedule need to cancel appointment----
                                AppointmentDAO appointmentDAO = new AppointmentDAO();
                                appointmentDAO.deleteAppointmentByVisitId(visitUuid);
                                if (appointmentId != 0) {
                                    askReason(slotInfo);
                                } else {
                                    bookAppointment(slotInfo, null);
                                }

                            }
                        });
                        rvSlots.setAdapter(slotListingAdapter);
                        if (slotListingAdapter.getItemCount() == 0) {
                            findViewById(R.id.llEmptyView).setVisibility(View.VISIBLE);
                        } else {
                            findViewById(R.id.llEmptyView).setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<SlotInfoResponse> call, Throwable t) {
                        Log.v("onFailure", t.getMessage());
                    }
                });

    }

}