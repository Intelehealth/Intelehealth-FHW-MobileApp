package org.intelehealth.app.appointment;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.R;
import org.intelehealth.app.appointment.adapter.AppointmentListingAdapter;
import org.intelehealth.app.appointment.api.ApiClientAppointment;
import org.intelehealth.app.appointment.dao.AppointmentDAO;
import org.intelehealth.app.appointment.model.AppointmentInfo;
import org.intelehealth.app.appointment.model.AppointmentListingResponse;
import org.intelehealth.app.models.auth.ResponseChecker;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.app.webrtc.activity.BaseActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class AppointmentListingActivity extends BaseActivity {
    RecyclerView rvAppointments;
    private String mSelectedStartDate = "";
    private String mSelectedEndDate = "";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_listing);
        //this language code is no longer required as we are moving towards more optimised as well as generic code for localisation. Check "attachBaseContext".
//        setLocale(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.appointment_listing_title);
        rvAppointments = findViewById(R.id.rvAppointments);
        rvAppointments.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mSelectedStartDate = simpleDateFormat.format(new Date());
        mSelectedEndDate = simpleDateFormat.format(new Date(new Date().getTime() + 30L * 24 * 60 * 60 * 1000));
        getAppointments();
        getSlots();
    }

    private void getAppointments() {
        List<AppointmentInfo> appointmentInfoList = new AppointmentDAO().getAppointments();
        AppointmentListingAdapter appointmentListingAdapter = new AppointmentListingAdapter(rvAppointments, this, appointmentInfoList, new AppointmentListingAdapter.OnItemSelection() {
            @Override
            public void onSelect(AppointmentInfo appointmentInfo) {

            }
        }, sessionManager.getAppLanguage());
        rvAppointments.setAdapter(appointmentListingAdapter);
        if (appointmentInfoList.isEmpty()) {
            findViewById(R.id.llEmptyView).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.llEmptyView).setVisibility(View.GONE);
        }
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

    private void getSlots() {
        SessionManager sessionManager = new SessionManager(this);
        String authHeader = "Bearer " + sessionManager.getJwtAuthToken();

        String baseurl = BuildConfig.SERVER_URL + ":3004";
        ApiClientAppointment.getInstance(baseurl).getApi()
                .getSlotsAll(mSelectedStartDate, mSelectedEndDate, sessionManager.getLocationUuid(), authHeader)

                .enqueue(new Callback<AppointmentListingResponse>() {
                    @Override
                    public void onResponse(Call<AppointmentListingResponse> call, retrofit2.Response<AppointmentListingResponse> response) {
                        ResponseChecker<AppointmentListingResponse> responseChecker = new ResponseChecker<>(response);
                        if (responseChecker.isNotAuthorized()) {
                            //TODO: redirect to login screen
                            return;
                        }

                        if (response.body() == null) return;
                        AppointmentListingResponse slotInfoResponse = response.body();
                        AppointmentDAO appointmentDAO = new AppointmentDAO();
                        appointmentDAO.deleteAllAppointments();
                        for (int i = 0; i < slotInfoResponse.getData().size(); i++) {

                            try {
                                appointmentDAO.insert(slotInfoResponse.getData().get(i));
                            } catch (DAOException e) {
                                e.printStackTrace();
                            }
                        }

                        getAppointments();
                        /*AppointmentListingAdapter slotListingAdapter = new AppointmentListingAdapter(rvAppointments,
                                AppointmentListingActivity.this,
                                slotInfoResponse.getData(), new AppointmentListingAdapter.OnItemSelection() {
                            @Override
                            public void onSelect(AppointmentInfo appointmentInfo) {

                            }


                        });
                        rvAppointments.setAdapter(slotListingAdapter);
                        if (slotListingAdapter.getItemCount() == 0) {
                            findViewById(R.id.llEmptyView).setVisibility(View.VISIBLE);
                        } else {
                            findViewById(R.id.llEmptyView).setVisibility(View.GONE);
                        }*/
                    }

                    @Override
                    public void onFailure(Call<AppointmentListingResponse> call, Throwable t) {
                        Log.v("onFailure", t.getMessage());
                    }
                });

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
}