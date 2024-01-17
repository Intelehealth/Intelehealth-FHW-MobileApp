package org.intelehealth.ekalarogya.appointment.sync;

import android.content.Context;
import android.util.Log;


import org.intelehealth.ekalarogya.appointment.api.ApiClientAppointment;
import org.intelehealth.ekalarogya.appointment.dao.AppointmentDAO;
import org.intelehealth.ekalarogya.appointment.model.AppointmentListingResponse;
import org.intelehealth.ekalarogya.utilities.NavigationUtils;
import org.intelehealth.ekalarogya.utilities.ResponseChecker;
import org.intelehealth.ekalarogya.utilities.SessionManager;
import org.intelehealth.ekalarogya.utilities.exception.DAOException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class AppointmentSync {
    public static void getAppointments(Context context) {
        SessionManager sessionManager = new SessionManager(context);
        String authHeader = "Bearer " + sessionManager.getJwtAuthToken();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        String selectedStartDate = simpleDateFormat.format(new Date());
        String selectedEndDate = simpleDateFormat.format(new Date(new Date().getTime() + 30L * 24 * 60 * 60 * 1000));
        String baseurl = "https://" + sessionManager.getServerUrl() + ":3004";
        ApiClientAppointment.getInstance(baseurl).getApi()
                .getSlotsAll(selectedStartDate, selectedEndDate, sessionManager.getCurrentLocationUuid(), authHeader)
                .enqueue(new Callback<AppointmentListingResponse>() {
                    @Override
                    public void onResponse(Call<AppointmentListingResponse> call, retrofit2.Response<AppointmentListingResponse> response) {
                        ResponseChecker<AppointmentListingResponse> responseChecker = new ResponseChecker<>(response);

                        if (responseChecker.isNotAuthorized()) {
                            sessionManager.setJwtAuthToken(null);
                            NavigationUtils navigationUtils = new NavigationUtils();
                            navigationUtils.triggerSignOutOn401Response(context);
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
                    }


                    @Override
                    public void onFailure(Call<AppointmentListingResponse> call, Throwable t) {
                        Log.v("onFailure", t.getMessage());
                    }
                });

    }
}
