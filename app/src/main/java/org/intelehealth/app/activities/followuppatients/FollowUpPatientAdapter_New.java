package org.intelehealth.app.activities.followuppatients;

import static org.intelehealth.app.utilities.StringUtils.setGenderAgeLocal;
import static org.intelehealth.app.utilities.StringUtils.setGenderAgeLocalByCommaContact;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import org.intelehealth.app.utilities.CustomLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity2;
import org.intelehealth.app.activities.visit.VisitDetailsActivity;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.models.FollowUpModel;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.DownloadFilesUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.exception.DAOException;

import java.io.Serializable;
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

/**
 * Created by Prajwal Waingankar on 21/08/22.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
public class FollowUpPatientAdapter_New extends RecyclerView.Adapter<FollowUpPatientAdapter_New.Myholder> {
    List<FollowUpModel> patients;
    Context context;
    ImagesDAO imagesDAO = new ImagesDAO();
    String profileImage = "";
    String profileImage1 = "";
    SessionManager sessionManager;

    public FollowUpPatientAdapter_New(List<FollowUpModel> patients, Context context) {
        this.patients = patients;
        this.context = context;
        sessionManager = new SessionManager(context);
    }

    public FollowUpPatientAdapter_New(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public FollowUpPatientAdapter_New.Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.followup_list_item_1, parent, false);
        return new FollowUpPatientAdapter_New.Myholder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowUpPatientAdapter_New.Myholder holder, int position) {
        if (patients != null) {
            if (position >= patients.size()) return;
            final FollowUpModel model = patients.get(position);
            holder.setIsRecyclable(false);

            setGenderAgeLocalByCommaContact(context, holder.search_gender, model.getDate_of_birth(), model.getGender(), sessionManager);

            if (model != null) {

                // Patient Photo
                //1.
                try {
                    profileImage = imagesDAO.getPatientProfileChangeTime(model.getPatientuuid());
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
                //2.
                if (model.getPatient_photo() == null || model.getPatient_photo().equalsIgnoreCase("")) {
                    if (NetworkConnection.isOnline(context)) {
                        profilePicDownloaded(model, holder);
                    }
                }
                //3.
                if (!profileImage.equalsIgnoreCase(profileImage1)) {
                    if (NetworkConnection.isOnline(context)) {
                        profilePicDownloaded(model, holder);
                    }
                }

                if (model.getPatient_photo() != null) {
                    RequestBuilder<Drawable> requestBuilder = Glide.with(holder.itemView.getContext())
                            .asDrawable().sizeMultiplier(0.3f);
                    Glide.with(context).load(model.getPatient_photo()).thumbnail(requestBuilder).centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(holder.profile_image);
                } else {
                    holder.profile_image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.avatar1));
                }
                // photo - end

                // Patient Name section
                CustomLog.v("Followup", new Gson().toJson(model));
                String fullName = "";
                if (model.getMiddle_name() == null || model.getMiddle_name().isEmpty()) {
                    fullName = model.getFirst_name() + " " + model.getLast_name();
                } else {
                    fullName = model.getFirst_name() + " " + model.getMiddle_name() + " " + model.getLast_name();
                }

                holder.fu_patname_txtview.setText(fullName);
                holder.openmrs_id_tv.setText(model.getOpenmrs_id());

                // Followup Date section
                if (!model.getFollowup_date().equalsIgnoreCase("null") && !model.getFollowup_date().isEmpty()) {
                    try {
                        CustomLog.v("getFollowup_date", model.getFollowup_date());

                        String followupDateTimeRaw = "";
                        try {
                            followupDateTimeRaw = model.getFollowup_date().substring(0, 26);
                        } catch (Exception e) {
                            followupDateTimeRaw = model.getFollowup_date().substring(0, 25);
                        }

                        CustomLog.v("getFollowup_date", followupDateTimeRaw + "OK");
                        String followupDateTime = followupDateTimeRaw.trim().replace(", Time:", "");
                        CustomLog.v("getFollowup_date", "final followupDate " + followupDateTime);

                        String todaysDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                        String formatedFollowupDate = DateAndTimeUtils.date_formatter(followupDateTime, "yyyy-MM-dd hh:mm a", "yyyy-MM-dd");

                        if (todaysDate.equals(formatedFollowupDate)) {
                            String getTimeDiff = getTimeDiff(followupDateTime);
                            if (!getTimeDiff.isEmpty()) {
                                holder.tv_time_diff.setVisibility(View.VISIBLE);
                                holder.tv_time_diff.setText(getTimeDiff);
                            } else {
                                holder.tv_time_diff.setVisibility(View.GONE);
                            }

                            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorAccentLightCard));
                        } else {
                            holder.tv_time_diff.setVisibility(View.GONE);
                            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white));
                        }

                        Date fDate = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.ENGLISH).parse(followupDateTime);
                        Date nowDate = new Date();
                        if (fDate.getTime() >= nowDate.getTime()) {
                            holder.fu_date_txtview.setTextColor(context.getColor(R.color.gray_3));
                        } else {
                            holder.fu_date_txtview.setTextColor(context.getColor(R.color.red));
                        }
                        String followupDate = DateAndTimeUtils.date_formatter(followupDateTime, "yyyy-MM-dd hh:mm a", "dd-MM-yyyy, HH:mm");
                        if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")){
                            followupDate = StringUtils.en__hi_dob(followupDate);
                        }else if(sessionManager.getAppLanguage().equalsIgnoreCase("ru")){
                            followupDate = StringUtils.en__ru_dob(followupDate);
                        }
                        holder.fu_date_txtview.setText(followupDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                // Emergency/Priority tag code.
                if (model.isEmergency()) holder.fu_priority_tag.setVisibility(View.VISIBLE);
                else holder.fu_priority_tag.setVisibility(View.GONE);
            }

            // Patient Age
            String age = DateAndTimeUtils.getAge_FollowUp(model.getDate_of_birth(), context);

            holder.cardView.setOnClickListener(v -> {
                Intent intent = new Intent(context, PatientDetailActivity2.class);
                intent.putExtra("patientUuid", model.getPatientuuid());
                intent.putExtra("patientName", model.getFirst_name() + " " + model.getLast_name());
                intent.putExtra("tag", "newPatient");
                intent.putExtra("hasPrescription", "false");
                context.startActivity(intent);
            });
        }
    }

    private String getTimeDiff(String followupDateTimeRaw) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
        String currentDateTime = dateFormat.format(new Date());
        long diff = 0;
        try {
            diff = dateFormat.parse(followupDateTimeRaw).getTime() - dateFormat.parse(currentDateTime).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long second = diff / 1000;
        long minutes = second / 60;
        // check for appointment but prescription not given and visit not completed
        if (minutes > 0) {
            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                return getTimeDuration(minutes) +
                        context.getString(R.string.in);
            }else {
                return context.getString(R.string.in) + " " + getTimeDuration(minutes);
            }
        } else {
            minutes = Math.abs(minutes);
            return getTimeDuration(minutes) + " " + context.getString(R.string.over);
        }
    }

    private String getTimeDuration(long minutes) {
        if (minutes >= 60) {
            long hours = minutes / 60;
            long mins = minutes % 60;
            if (hours < 24) {
                return hours + " " + context.getString(R.string.hours) + " " +
                        mins + " " + context.getString(R.string.minutes_txt);
            }
        } else {
            return minutes + " " + context.getString(R.string.minutes_txt);
        }
        return "";
    }

    @Override
    public int getItemCount() {
        return patients.size(); // todo: uncomment
        //   return 2;   // todo: testing
    }

    class Myholder extends RecyclerView.ViewHolder {
        CardView cardView;
        private View rootView;
        TextView fu_patname_txtview, fu_date_txtview, search_gender, tv_time_diff, openmrs_id_tv;
        ImageView profile_image;
        LinearLayout fu_priority_tag;

        public Myholder(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.fu_cardview_item);
            fu_patname_txtview = itemView.findViewById(R.id.fu_patname_txtview);
            fu_date_txtview = itemView.findViewById(R.id.fu_date_txtview);
            openmrs_id_tv = itemView.findViewById(R.id.openmrs_id_tv);
            tv_time_diff = itemView.findViewById(R.id.tv_time_diff);
            fu_priority_tag = itemView.findViewById(R.id.llPriorityTagFollowUpListItem1);
            profile_image = itemView.findViewById(R.id.profile_image);
            search_gender = itemView.findViewById(R.id.search_gender);
            rootView = itemView;

            //   fu_date_txtview.setText("22 June"); // todo: testing.
        }

        public View getRootView() {
            return rootView;
        }
    }

    public void profilePicDownloaded(FollowUpModel model, Myholder holder) {
        UrlModifiers urlModifiers = new UrlModifiers();
        String url = urlModifiers.patientProfileImageUrl(model.getPatientuuid());
        Logger.logD("TAG", "profileimage url" + url);
        Observable<ResponseBody> profilePicDownload = AppConstants.apiInterface.PERSON_PROFILE_PIC_DOWNLOAD(url, "Basic " + sessionManager.getEncoded());
        profilePicDownload.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new DisposableObserver<ResponseBody>() {
            @Override
            public void onNext(ResponseBody file) {
                DownloadFilesUtils downloadFilesUtils = new DownloadFilesUtils();
                downloadFilesUtils.saveToDisk(file, model.getPatientuuid());
                Logger.logD("TAG", file.toString());
            }

            @Override
            public void onError(Throwable e) {
                Logger.logD("TAG", e.getMessage());
            }

            @Override
            public void onComplete() {
                Logger.logD("TAG", "complete" + model.getPatient_photo());
                PatientsDAO patientsDAO = new PatientsDAO();
                boolean updated = false;
                try {
                    updated = patientsDAO.updatePatientPhoto(model.getPatientuuid(), AppConstants.IMAGE_PATH + model.getPatientuuid() + ".jpg");
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
                if (updated) {

                    RequestBuilder<Drawable> requestBuilder = Glide.with(holder.itemView.getContext())
                            .asDrawable().sizeMultiplier(0.3f);
                    Glide.with(context).load(AppConstants.IMAGE_PATH + model.getPatientuuid() + ".jpg").thumbnail(requestBuilder).centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(holder.profile_image);
                }
                ImagesDAO imagesDAO = new ImagesDAO();
                boolean isImageDownloaded = false;
                try {
                    isImageDownloaded = imagesDAO.insertPatientProfileImages(AppConstants.IMAGE_PATH + model.getPatientuuid() + ".jpg", model.getPatientuuid());
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
//                        if (isImageDownloaded)
//                            AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_image_download_notifi), "" + patient_new.getFirst_name() + "" + patient_new.getLast_name() + "'s Image Download Incomplete.", 4, getApplication());
//                        else
//                            AppConstants.notificationUtils.DownloadDone(getString(R.string.patient_image_download_notifi), "" + patient_new.getFirst_name() + "" + patient_new.getLast_name() + "'s Image Download Incomplete.", 4, getApplication());
            }
        });
    }


}
