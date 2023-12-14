package org.intelehealth.kf.appointmentNew;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.kf.R;
import org.intelehealth.kf.app.AppConstants;
import org.intelehealth.kf.appointment.model.AppointmentInfo;
import org.intelehealth.kf.database.dao.ImagesDAO;
import org.intelehealth.kf.database.dao.PatientsDAO;
import org.intelehealth.kf.utilities.DateAndTimeUtils;
import org.intelehealth.kf.utilities.DownloadFilesUtils;
import org.intelehealth.kf.utilities.Logger;
import org.intelehealth.kf.utilities.NetworkConnection;
import org.intelehealth.kf.utilities.SessionManager;
import org.intelehealth.kf.utilities.UrlModifiers;
import org.intelehealth.kf.utilities.exception.DAOException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class TodaysMyAppointmentsAdapter extends RecyclerView.Adapter<TodaysMyAppointmentsAdapter.MyViewHolder> {
    private static final String TAG = "TodaysMyAppointmentsAda";
    Context context;
    List<AppointmentInfo> appointmentInfoList;
    String whichAppointments;

    public TodaysMyAppointmentsAdapter(Context context, List<AppointmentInfo> appointmentInfoList, String whichAppointments) {
        this.context = context;
        this.appointmentInfoList = appointmentInfoList;
        this.whichAppointments = whichAppointments;

    }

    @Override
    public TodaysMyAppointmentsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todays_appointments_ui2_new, parent, false);
        TodaysMyAppointmentsAdapter.MyViewHolder myViewHolder = new TodaysMyAppointmentsAdapter.MyViewHolder(view);

        return myViewHolder;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(TodaysMyAppointmentsAdapter.MyViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: appointmentInfoList : " + appointmentInfoList.size());

        try {
            AppointmentInfo appointmentInfoModel = appointmentInfoList.get(position);
            if (appointmentInfoModel.getPatientProfilePhoto() == null || appointmentInfoModel.getPatientProfilePhoto().equalsIgnoreCase("")) {
                if (NetworkConnection.isOnline(context)) {
                    profilePicDownloaded(appointmentInfoModel, holder);
                }
            }


            if (appointmentInfoModel.getPatientProfilePhoto() != null && !appointmentInfoModel.getPatientProfilePhoto().isEmpty()) {
                Glide.with(context)
                        .load(appointmentInfoModel.getPatientProfilePhoto())
                        .thumbnail(0.3f)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(holder.ivProfileImage);
            } else {
                holder.ivProfileImage.setImageDrawable(context.getResources().getDrawable(R.drawable.avatar1));
            }
            holder.doctNameTextView.setText("Dr. " + appointmentInfoModel.getDrName());
            if (whichAppointments.equalsIgnoreCase("upcoming")) {

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
                String currentDateTime = dateFormat.format(new Date());
                String slottime = appointmentInfoModel.getSlotDate() + " " + appointmentInfoModel.getSlotTime();

                long diff = 0;
                try {
                    diff = dateFormat.parse(slottime).getTime() - dateFormat.parse(currentDateTime).getTime();
                    long second = diff / 1000;
                    long minutes = second / 60;
                    Log.v("AppointmentInfo", "Diff minutes - " + minutes);

                    String timeText = "";
                    //check for appointmet but presc not given and visit not completed
                    if (minutes > 0) {
                        if (minutes >= 60) {
                            long hours = minutes / 60;
                            if (hours > 24) {

                                holder.tvPatientName.setText(appointmentInfoModel.getPatientName());
                                holder.ivTime.setImageDrawable(context.getResources().getDrawable(R.drawable.ui2_ic_calendar));
                                holder.ivTime.setColorFilter(ContextCompat.getColor(context, R.color.iconTintGray), PorterDuff.Mode.SRC_IN);

                                timeText = DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(appointmentInfoModel.getSlotDate()) + ", " + context.getString(R.string.at) + " " + appointmentInfoModel.getSlotTime();
                                holder.tvDate.setText(timeText);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    holder.tvDate.setTextColor(context.getColor(R.color.iconTintGray));
                                }
                            } else {
                                timeText = context.getString(R.string.in) + " " + hours + " " + context.getString(R.string.hours_at) + " " + appointmentInfoModel.getSlotTime();
                                holder.ivTime.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary1), PorterDuff.Mode.SRC_IN);
                                holder.tvPatientName.setText(appointmentInfoModel.getPatientName());


                                holder.tvDate.setText(timeText);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    holder.tvDate.setTextColor(context.getColor(R.color.colorPrimary1));
                                }
                            }
                        } else {
                            timeText = context.getString(R.string.in) + " " + minutes + " " + context.getString(R.string.minute);
                            holder.ivTime.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary1), PorterDuff.Mode.SRC_IN);
                            holder.tvPatientName.setText(appointmentInfoModel.getPatientName());

                            holder.tvDate.setText(timeText);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                holder.tvDate.setTextColor(context.getColor(R.color.colorPrimary1));
                            }
                        }
                    }


                } catch (ParseException e) {
                    Log.d(TAG, "onBindViewHolder: date exce : " + e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }


            if (whichAppointments.equalsIgnoreCase("completed")) {
                //bcz of common UI
                //hide  : ivTime, tvDate, tvPatientId
                //show :  tvPrescRecStatus

                holder.ivTime.setVisibility(View.GONE);
                holder.tvDate.setVisibility(View.GONE);
                holder.tvPatientId.setVisibility(View.GONE);
                holder.tvPrescRecStatus.setVisibility(View.VISIBLE);
                holder.tvPatientName.setText(appointmentInfoModel.getPatientName());
                holder.tvDate.setText(DateAndTimeUtils.getDisplayDateAndTime(appointmentInfoModel.getPresc_received_time()));
                Log.d(TAG, "onBindViewHolder:time :  " + appointmentInfoModel.getPresc_received_time());
                if (appointmentInfoModel.isPrescription_exists()) {
                    holder.tvPrescRecStatus.setBackground(context.getResources().getDrawable(R.drawable.ui2_ic_presc_received));
                } else {
                    holder.tvPrescRecStatus.setBackground(context.getResources().getDrawable(R.drawable.ui2_ic_presc_pending));

                }
            }

            if (whichAppointments.equalsIgnoreCase("cancelled")) {
                holder.tvPatientName.setText(appointmentInfoModel.getPatientName());
                holder.ivTime.setVisibility(View.VISIBLE);
                holder.tvDate.setVisibility(View.VISIBLE);
                holder.tvPrescRecStatus.setVisibility(View.GONE);
                holder.tvPatientId.setVisibility(View.GONE);

                holder.tvDate.setText(appointmentInfoModel.getSlotTime());
                Log.d(TAG, "onBindViewHolder: time : " + appointmentInfoModel.getSlotDate());
                Log.d(TAG, "onBindViewHolder: time : " + appointmentInfoModel.getSlotTime());

            }


            holder.cardParent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  /*    patientname patientUuid gender age openmrsID visit_ID visit_startDate visit_speciality followup_date
                  priority_tag hasPrescription patient_photo chief_complaint */

                    Intent intent = new Intent(context, AppointmentDetailsActivity.class);
                    intent.putExtra("patientname", appointmentInfoModel.getPatientName());
                    intent.putExtra("patientUuid", appointmentInfoModel.getPatientId());
                    intent.putExtra("gender", "");
                    //String age = DateAndTimeUtils.getAge_FollowUp(appointmentInfoModel.get(), context);
                    intent.putExtra("age", "");
                    intent.putExtra("priority_tag", "");
                    intent.putExtra("hasPrescription", appointmentInfoModel.isPrescription_exists());
                    intent.putExtra("openmrsID", appointmentInfoModel.getOpenMrsId());
                    intent.putExtra("visit_ID", appointmentInfoModel.getVisitUuid());
                    intent.putExtra("visit_startDate", "");
                    intent.putExtra("patient_photo", appointmentInfoModel.getPatientProfilePhoto());
                    intent.putExtra("app_start_date", appointmentInfoModel.getSlotDate());
                    intent.putExtra("app_start_time", appointmentInfoModel.getSlotTime());
                    intent.putExtra("visit_speciality", appointmentInfoModel.getSpeciality());
                    intent.putExtra("appointment_id", appointmentInfoModel.getId());
                    intent.putExtra("app_start_day", appointmentInfoModel.getSlotDay());
                    intent.putExtra("prescription_received_time", DateAndTimeUtils.getDisplayDateAndTime(appointmentInfoModel.getPresc_received_time()));
                    intent.putExtra("status", appointmentInfoModel.getStatus());

                    context.startActivity(intent);

                }
            });


        } catch (Exception e) {
            Log.d(TAG, "onBindViewHolder: e main : " + e.getLocalizedMessage());
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return appointmentInfoList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        CardView cardParent;
        TextView tvPatientName, tvDate, tvPatientId, tvPrescRecStatus, doctNameTextView;
        ImageView ivProfileImage, ivTime, IvPriorityTag;


        public MyViewHolder(View itemView) {
            super(itemView);
            cardParent = itemView.findViewById(R.id.card_todays_appointments1);
            tvPatientName = itemView.findViewById(R.id.tv_patient_name_todays);
            ivProfileImage = itemView.findViewById(R.id.profile_image_todays);
            tvDate = itemView.findViewById(R.id.tv_date_appointment_todays);
            ivTime = itemView.findViewById(R.id.iv_time_todays);
            IvPriorityTag = itemView.findViewById(R.id.iv_priority_tag1_todays);
            tvPatientId = itemView.findViewById(R.id.tv_patient_id_todays);
            tvPrescRecStatus = itemView.findViewById(R.id.tv_presc_rec_status);
            doctNameTextView = itemView.findViewById(R.id.tv_dr_name_todays);


        }
    }

    public void profilePicDownloaded(AppointmentInfo model, MyViewHolder holder) {
        SessionManager sessionManager = new SessionManager(context);
        UrlModifiers urlModifiers = new UrlModifiers();
        String url = urlModifiers.patientProfileImageUrl(model.getUuid());
        Observable<ResponseBody> profilePicDownload = AppConstants.apiInterface.PERSON_PROFILE_PIC_DOWNLOAD
                (url, "Basic " + sessionManager.getEncoded());
        profilePicDownload.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody file) {
                        DownloadFilesUtils downloadFilesUtils = new DownloadFilesUtils();
                        downloadFilesUtils.saveToDisk(file, model.getUuid());
                        Logger.logD("TAG", file.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.logD("TAG", e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Logger.logD("TAG", "complete" + model.getPatientProfilePhoto());
                        PatientsDAO patientsDAO = new PatientsDAO();
                        boolean updated = false;
                        try {
                            updated = patientsDAO.updatePatientPhoto(model.getUuid(),
                                    AppConstants.IMAGE_PATH + model.getUuid() + ".jpg");
                        } catch (DAOException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                        if (updated) {
                            Glide.with(context)
                                    .load(AppConstants.IMAGE_PATH + model.getUuid() + ".jpg")
                                    .thumbnail(0.3f)
                                    .centerCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true)
                                    .into(holder.ivProfileImage);
                        }
                        ImagesDAO imagesDAO = new ImagesDAO();
                        boolean isImageDownloaded = false;
                        try {
                            isImageDownloaded = imagesDAO.insertPatientProfileImages(
                                    AppConstants.IMAGE_PATH + model.getUuid() + ".jpg", model.getUuid());
                        } catch (DAOException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                    }
                });
    }

}
