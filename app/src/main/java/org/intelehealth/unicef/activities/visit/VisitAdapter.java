package org.intelehealth.unicef.activities.visit;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.app.AppConstants;
import org.intelehealth.unicef.database.dao.ImagesDAO;
import org.intelehealth.unicef.database.dao.PatientsDAO;
import org.intelehealth.unicef.models.PrescriptionModel;
import org.intelehealth.unicef.utilities.DateAndTimeUtils;
import org.intelehealth.unicef.utilities.DownloadFilesUtils;
import org.intelehealth.unicef.utilities.Logger;
import org.intelehealth.unicef.utilities.NetworkConnection;
import org.intelehealth.unicef.utilities.SessionManager;
import org.intelehealth.unicef.utilities.StringUtils;
import org.intelehealth.unicef.utilities.UrlModifiers;
import org.intelehealth.unicef.utilities.exception.DAOException;

import java.util.List;

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
public class VisitAdapter extends RecyclerView.Adapter<VisitAdapter.Myholder> {
    private Context context;
    List<PrescriptionModel> list;
    ImagesDAO imagesDAO = new ImagesDAO();
    String profileImage = "";
    String profileImage1 = "";
    SessionManager sessionManager;
    private String appLanguage;

    public VisitAdapter(Context context, List<PrescriptionModel> list, String appLanguage) {
        this.context = context;
        this.list = list;
        this.appLanguage = appLanguage;
    }

    @NonNull
    @Override
    public VisitAdapter.Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.followup_list_item, parent, false);
        return new VisitAdapter.Myholder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull VisitAdapter.Myholder holder, int position) {
        PrescriptionModel model = list.get(position);
        if (model != null) {

            // share icon visibility
            if (model.isHasPrescription()) holder.shareicon.setVisibility(View.VISIBLE);
            else holder.shareicon.setVisibility(View.GONE);

            // end

            holder.name.setText(model.getFirst_name() + " " + model.getLast_name());

            // Patient Photo
            //1.
            try {
                profileImage = imagesDAO.getPatientProfileChangeTime(model.getPatientUuid());
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
                Glide.with(context).load(model.getPatient_photo()).override(50, 50).thumbnail(0.3f).centerCrop().skipMemoryCache(false).diskCacheStrategy(DiskCacheStrategy.RESULT).into(holder.profile_image);
            } else {
                holder.profile_image.setImageDrawable(context.getResources().getDrawable(R.drawable.avatar1));
            }
            // photo - end

            // visit start date
            if (!model.getVisit_start_date().equalsIgnoreCase("null") || !model.getVisit_start_date().isEmpty()) {
                String startDate = model.getVisit_start_date();
                startDate = DateAndTimeUtils.date_formatter(startDate, "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "dd MMMM");
                if (appLanguage.equalsIgnoreCase("ru")) {
                    startDate = StringUtils.en__ru_dob(startDate);
                }

                holder.fu_date_txtview.setText(startDate);
            }

            // Emergency - start
            if (model.isEmergency()) holder.fu_priority_tag.setVisibility(View.VISIBLE);
            else holder.fu_priority_tag.setVisibility(View.GONE);
            // Emergency - end


            holder.fu_cardview_item.setOnClickListener(v -> {
                Intent intent = new Intent(context, VisitDetailsActivity.class);
                intent.putExtra("patientname", model.getFirst_name() + " " + model.getLast_name().substring(0, 1));
                intent.putExtra("patientUuid", model.getPatientUuid());
                intent.putExtra("gender", model.getGender());
                String age = DateAndTimeUtils.getAge_FollowUp(model.getDob(), context);
                intent.putExtra("age", age);
                intent.putExtra("priority_tag", model.isEmergency());
                intent.putExtra("hasPrescription", model.isHasPrescription());
                intent.putExtra("openmrsID", model.getOpenmrs_id());
                intent.putExtra("visit_ID", model.getVisitUuid());
                intent.putExtra("visit_startDate", model.getVisit_start_date());
                intent.putExtra("patient_photo", model.getPatient_photo());
                intent.putExtra("obsservermodifieddate", model.getObsservermodifieddate());
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Myholder extends RecyclerView.ViewHolder {
        private CardView fu_cardview_item;
        private TextView name, fu_date_txtview, fu_priority_tag;
        private ImageView profile_image;
        private LinearLayoutCompat shareicon;

        public Myholder(@NonNull View itemView) {
            super(itemView);
            fu_cardview_item = itemView.findViewById(R.id.fu_cardview_item);
            name = itemView.findViewById(R.id.fu_patname_txtview);
            fu_date_txtview = itemView.findViewById(R.id.fu_date_txtview);
            profile_image = itemView.findViewById(R.id.profile_image);
            fu_priority_tag = itemView.findViewById(R.id.fu_priority_tag);
            shareicon = itemView.findViewById(R.id.shareicon);
        }
    }

    // profile downlaod
    public void profilePicDownloaded(PrescriptionModel model, VisitAdapter.Myholder holder) {
        sessionManager = new SessionManager(context);
        UrlModifiers urlModifiers = new UrlModifiers();
        String url = urlModifiers.patientProfileImageUrl(model.getPatientUuid());
        Logger.logD("TAG", "profileimage url" + url);

        Observable<ResponseBody> profilePicDownload = AppConstants.apiInterface.PERSON_PROFILE_PIC_DOWNLOAD(url, "Basic " + sessionManager.getEncoded());
        profilePicDownload.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new DisposableObserver<ResponseBody>() {
            @Override
            public void onNext(ResponseBody file) {
                DownloadFilesUtils downloadFilesUtils = new DownloadFilesUtils();
                downloadFilesUtils.saveToDisk(file, model.getPatientUuid());
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
                    updated = patientsDAO.updatePatientPhoto(model.getPatientUuid(), AppConstants.IMAGE_PATH + model.getPatientUuid() + ".jpg");
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
                if (updated) {
                    Glide.with(context).load(AppConstants.IMAGE_PATH + model.getPatientUuid() + ".jpg").override(50, 50).thumbnail(0.3f).centerCrop().skipMemoryCache(false).diskCacheStrategy(DiskCacheStrategy.RESULT).into(holder.profile_image);
                }
                ImagesDAO imagesDAO = new ImagesDAO();
                boolean isImageDownloaded = false;
                try {
                    isImageDownloaded = imagesDAO.insertPatientProfileImages(AppConstants.IMAGE_PATH + model.getPatientUuid() + ".jpg", model.getPatientUuid());
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }
        });
    }

}