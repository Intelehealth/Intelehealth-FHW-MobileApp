package org.intelehealth.app.activities.visit;

import static org.intelehealth.app.database.dao.EncounterDAO.fetchEncounterUuidForEncounterAdultInitials;
import static org.intelehealth.app.database.dao.EncounterDAO.fetchEncounterUuidForEncounterVitals;
import static org.intelehealth.app.database.dao.EncounterDAO.getStartVisitNoteEncounterByVisitUUID;
import static org.intelehealth.app.utilities.UuidDictionary.PRESCRIPTION_LINK;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.VisitAttributeListDAO;
import org.intelehealth.app.models.PrescriptionModel;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.DownloadFilesUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.VisitUtils;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.ArrayList;
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
public class EndVisitAdapter extends RecyclerView.Adapter<EndVisitAdapter.Myholder> {
    private Context context;
    List<PrescriptionModel> arrayList = new ArrayList<>();
    ImagesDAO imagesDAO = new ImagesDAO();
    String profileImage = "";
    String profileImage1 = "";
    SessionManager sessionManager;

    public EndVisitAdapter(Context context, List<PrescriptionModel> arrayList) {
        this.context = context;
        this.arrayList.addAll(arrayList);
    }

    @NonNull
    @Override
    public EndVisitAdapter.Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View row = inflater.inflate(R.layout.followup_list_item, parent, false);
        return new EndVisitAdapter.Myholder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull EndVisitAdapter.Myholder holder, int position) {
        PrescriptionModel model = arrayList.get(position);
        if (model != null) {
            // name
            holder.name.setText(model.getFirst_name() + " " + model.getLast_name());

            // share icon visibility
            String encounteruuid = getStartVisitNoteEncounterByVisitUUID(model.getVisitUuid());
            if (!encounteruuid.isEmpty() && !encounteruuid.equalsIgnoreCase("")) {
                holder.shareicon.setVisibility(View.VISIBLE);
            } else {
                holder.shareicon.setVisibility(View.GONE);
            }

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
                Glide.with(context)
                        .load(model.getPatient_photo())
                        .override(100, 100)
                        .thumbnail(0.3f)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(holder.profile_image);
            } else {
                holder.profile_image.setImageDrawable(context.getResources().getDrawable(R.drawable.avatar1));
            }
            // photo - end

            // start date show
            if (!model.getVisit_start_date().equalsIgnoreCase("null") || !model.getVisit_start_date().isEmpty()) {
                String startDate = model.getVisit_start_date();
                startDate = DateAndTimeUtils.date_formatter(startDate,
                        "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "dd MMM 'at' HH:mm a");    // IDA-1346
                Log.v("startdate", "startDAte: " + startDate);
                holder.fu_date_txtview.setText(startDate);
            }

        //    holder.fu_date_txtview.setText(model.getVisit_start_date());

            holder.end_visit_btn.setOnClickListener(v -> {
                showConfirmDialog(model);
            });

            holder.shareicon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sharePresc(model);
                }
            });
        }
    }

    private void showConfirmDialog(final PrescriptionModel model) {
        DialogUtils dialogUtils = new DialogUtils();
        dialogUtils.showCommonDialog(context, R.drawable.dialog_close_visit_icon, context.getResources().getString(R.string.confirm_end_visit_reason), context.getResources().getString(R.string.confirm_end_visit_reason_message), false, context.getResources().getString(R.string.confirm), context.getResources().getString(R.string.cancel), new DialogUtils.CustomDialogListener() {
            @Override
            public void onDialogActionDone(int action) {
                if (action == DialogUtils.CustomDialogListener.POSITIVE_CLICK) {
                    String vitalsUUID = fetchEncounterUuidForEncounterVitals(model.getVisitUuid());
                    String adultInitialUUID = fetchEncounterUuidForEncounterAdultInitials(model.getVisitUuid());

                    VisitUtils.endVisit(context, model.getVisitUuid(), model.getPatientUuid(), model.getFollowup_date(),
                            vitalsUUID, adultInitialUUID, "state",
                            model.getFirst_name() + " " + model.getLast_name().substring(0, 1), "VisitDetailsActivity");
                }
            }
        });
    }

    private void sharePresc(final PrescriptionModel model) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.dialog_sharepresc, null);
        alertdialogBuilder.setView(convertView);
        EditText editText = convertView.findViewById(R.id.editText_mobileno);
        Button sharebtn = convertView.findViewById(R.id.sharebtn);
        String partial_whatsapp_presc_url = new UrlModifiers().setwhatsappPresciptionUrl();
        String prescription_link = new VisitAttributeListDAO().getVisitAttributesList_specificVisit(model.getVisitUuid(), PRESCRIPTION_LINK);
        if(model.getPhone_number()!=null)
        editText.setText(model.getPhone_number());
        sharebtn.setOnClickListener(v -> {
            if (!editText.getText().toString().equalsIgnoreCase("")) {
                String phoneNumber = /*"+91" +*/ editText.getText().toString();
                String whatsappMessage = String.format("https://api.whatsapp.com/send?phone=%s&text=%s",
                        phoneNumber, context.getResources().getString(R.string.hello_thankyou_for_using_intelehealth_app_to_download_click_here)
                                + partial_whatsapp_presc_url + Uri.encode("#") + prescription_link + context.getResources().getString(R.string.and_enter_your_patient_id)
                                + model.getOpenmrs_id());
                Log.v("whatsappMessage", whatsappMessage);
                context.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(whatsappMessage)));
            } else {
                Toast.makeText(context, context.getResources().getString(R.string.please_enter_mobile_number),
                        Toast.LENGTH_SHORT).show();
            }

        });

        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        alertDialog.show();
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class Myholder extends RecyclerView.ViewHolder {
        Button end_visit_btn;
        private CardView fu_cardview_item;
        private TextView name, fu_date_txtview;
        private ImageView profile_image;
        private LinearLayout shareicon;


        public Myholder(@NonNull View itemView) {
            super(itemView);
            end_visit_btn = itemView.findViewById(R.id.end_visit_btn);
            fu_cardview_item = itemView.findViewById(R.id.fu_cardview_item);
            name = itemView.findViewById(R.id.fu_patname_txtview);
            fu_date_txtview = itemView.findViewById(R.id.fu_date_txtview);
            profile_image = itemView.findViewById(R.id.profile_image);
            shareicon = itemView.findViewById(R.id.shareiconLL);
            end_visit_btn.setVisibility(View.VISIBLE);
        }
    }

    public void profilePicDownloaded(PrescriptionModel model, EndVisitAdapter.Myholder holder) {
        sessionManager = new SessionManager(context);
        UrlModifiers urlModifiers = new UrlModifiers();
        String url = urlModifiers.patientProfileImageUrl(model.getPatientUuid());
        Logger.logD("TAG", "profileimage url" + url);

        Observable<ResponseBody> profilePicDownload = AppConstants.apiInterface.PERSON_PROFILE_PIC_DOWNLOAD
                (url, "Basic " + sessionManager.getEncoded());
        profilePicDownload.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<ResponseBody>() {
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
                            updated = patientsDAO.updatePatientPhoto(model.getPatientUuid(),
                                    AppConstants.IMAGE_PATH + model.getPatientUuid() + ".jpg");
                        } catch (DAOException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                        if (updated) {
                            Glide.with(context)
                                    .load(AppConstants.IMAGE_PATH + model.getPatientUuid() + ".jpg")
                                    .override(100, 100)
                                    .thumbnail(0.3f)
                                    .centerCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true)
                                    .into(holder.profile_image);
                        }
                        ImagesDAO imagesDAO = new ImagesDAO();
                        boolean isImageDownloaded = false;
                        try {
                            isImageDownloaded = imagesDAO.insertPatientProfileImages(
                                    AppConstants.IMAGE_PATH + model.getPatientUuid() + ".jpg", model.getPatientUuid());
                        } catch (DAOException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                    }
                });
    }

}
