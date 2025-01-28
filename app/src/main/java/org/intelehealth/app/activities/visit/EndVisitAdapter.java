package org.intelehealth.app.activities.visit;

import static org.intelehealth.app.database.dao.EncounterDAO.fetchEncounterUuidForEncounterAdultInitials;
import static org.intelehealth.app.database.dao.EncounterDAO.fetchEncounterUuidForEncounterVitals;
import static org.intelehealth.app.database.dao.EncounterDAO.getStartVisitNoteEncounterByVisitUUID;
import static org.intelehealth.app.database.dao.PatientsDAO.phoneNumber;
import static org.intelehealth.app.utilities.StringUtils.setGenderAgeLocal;
import static org.intelehealth.app.utilities.UuidDictionary.PRESCRIPTION_LINK;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import org.intelehealth.app.utilities.CustomLog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.checkerframework.checker.units.qual.A;
import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.appointment.dao.AppointmentDAO;
import org.intelehealth.app.appointment.model.AppointmentInfo;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.VisitAttributeListDAO;
import org.intelehealth.app.models.PrescriptionModel;
import org.intelehealth.app.utilities.AppointmentUtils;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.DownloadFilesUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.VisitUtils;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.config.room.entity.FeatureActiveStatus;

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
    FeatureActiveStatus featureActiveStatus;

    public EndVisitAdapter(Context context, List<PrescriptionModel> arrayList, FeatureActiveStatus featureActiveStatus) {
        this.context = context;
        this.arrayList.addAll(arrayList);
        this.featureActiveStatus = featureActiveStatus;
        sessionManager = new SessionManager(context);
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


            //  1. Age
            /*String age = DateAndTimeUtils.getAge_FollowUp(model.getDob(), context);
            holder.search_gender.setText(model.getGender() + " " + age);*/
            setGenderAgeLocal(context, holder.search_gender, model.getDob(), model.getGender(), sessionManager);

            // share icon visibility
            /*String encounteruuid = getStartVisitNoteEncounterByVisitUUID(model.getVisitUuid());
            if (!encounteruuid.isEmpty() && !encounteruuid.equalsIgnoreCase("")) {
                holder.shareicon.setVisibility(View.VISIBLE);
            } else {
                holder.shareicon.setVisibility(View.GONE);
            }*/

            if (model.isHasPrescription())
                holder.shareicon.setVisibility(View.VISIBLE);
            else
                holder.shareicon.setVisibility(View.GONE);

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
                RequestBuilder<Drawable> requestBuilder = Glide.with(holder.itemView.getContext())
                        .asDrawable().sizeMultiplier(0.3f);
                Glide.with(context)
                        .load(model.getPatient_photo())
                        .override(50, 50)
                        //.thumbnail(requestBuilder)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .skipMemoryCache(true)
                        .into(holder.profile_image);
            } else {
                holder.profile_image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.avatar1));
            }
            // photo - end

            // start date show
            if (!model.getVisit_start_date().equalsIgnoreCase("null") || !model.getVisit_start_date().isEmpty()) {
                String startDate = model.getVisit_start_date();
                startDate = DateAndTimeUtils.date_formatter(startDate,
                        "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "dd MMM 'at' HH:mm a");    // IDA-1346
                //CustomLog.v("startdate", "startDAte: " + startDate);
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
                    startDate = StringUtils.en_hi_dob_three(startDate);
                holder.fu_date_txtview.setText(startDate);
            }

            //    holder.fu_date_txtview.setText(model.getVisit_start_date());

            holder.end_visit_btn.setOnClickListener(v -> {
                showConfirmDialog(model);
            });

            holder.fu_cardview_item.setOnClickListener(v -> {
                Intent intent = new Intent(context, VisitDetailsActivity.class);
                intent.putExtra("patientname", model.getFirst_name() + " " + model.getLast_name().substring(0, 1));
                intent.putExtra("patientUuid", model.getPatientUuid());
                intent.putExtra("gender", model.getGender());
                intent.putExtra("dob", model.getDob());
                String age1 = DateAndTimeUtils.getAge_FollowUp(model.getDob(), context);
                intent.putExtra("age", age1);
                intent.putExtra("priority_tag", model.isEmergency());
                intent.putExtra("hasPrescription", model.isHasPrescription());
                intent.putExtra("openmrsID", model.getOpenmrs_id());
                intent.putExtra("visit_ID", model.getVisitUuid());
                intent.putExtra("visit_startDate", model.getVisit_start_date());
                intent.putExtra("patient_photo", model.getPatient_photo());
                intent.putExtra("obsservermodifieddate", model.getObsservermodifieddate());
                context.startActivity(intent);
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
        if (model.isHasPrescription()) {
            triggerEndVisit(model);
        } else {
            if (featureActiveStatus.getRestrictEndVisit()) {
                //added restrictEndVisit because in NAS - we cant end the visit is prescription not shared by dr -Nas-ida migration
                DialogUtils dialogUtils = new DialogUtils();
                dialogUtils.showCommonDialog(context, R.drawable.dialog_close_visit_icon, context.getResources().getString(R.string.alert_label_txt), context.getResources().getString(R.string.prescription_notprovided_msg), true, context.getResources().getString(R.string.ok), context.getResources().getString(R.string.cancel), action -> {
                });
            } else {
                DialogUtils dialogUtils = new DialogUtils();
                dialogUtils.showCommonDialog(
                        context,
                        R.drawable.dialog_close_visit_icon,
                        context.getResources().getString(R.string.confirm_end_visit_reason),
                        context.getResources().getString(R.string.confirm_end_visit_reason_message),
                        false,
                        context.getResources().getString(R.string.confirm),
                        context.getResources().getString(R.string.cancel),
                        action -> {
                            if (action == DialogUtils.CustomDialogListener.POSITIVE_CLICK) {
                                checkIfAppointmentExistsForVisit(model);
                            }
                        });
            }

        }
    }

    private void checkIfAppointmentExistsForVisit(PrescriptionModel model) {
        // First check if there is an appointment or not
        AppointmentDAO appointmentDAO = new AppointmentDAO();
        if (!appointmentDAO.doesAppointmentExistForVisit(model.getVisitUuid())) {
            triggerEndVisit(model);
            return;
        }

        String appointmentDateTime = appointmentDAO.getTimeAndDateForAppointment(model.getVisitUuid());
        boolean isCurrentTimeAfterAppointmentTime = DateAndTimeUtils.isCurrentDateTimeAfterAppointmentTime(appointmentDateTime);

        // Next, check if the time for appointment is passed. In case the time has passed, we don't need to cancel the appointment as it is automatically completed.
        if (isCurrentTimeAfterAppointmentTime) {
            triggerEndVisit(model);
            return;
        }

        // In case the appointment time is not passed, only in that case, we will display the dialog for ending the appointment.
        new DialogUtils().triggerEndAppointmentConfirmationDialog(context, action -> {
            if (action == DialogUtils.CustomDialogListener.POSITIVE_CLICK) {
                cancelAppointment(model);
                triggerEndVisit(model);
            }
        });
    }

    private void triggerEndVisit(PrescriptionModel model) {
        String vitalsUUID = fetchEncounterUuidForEncounterVitals(model.getVisitUuid());
        String adultInitialUUID = fetchEncounterUuidForEncounterAdultInitials(model.getVisitUuid());

        VisitUtils.endVisit(
                context,
                model.getVisitUuid(),
                model.getPatientUuid(),
                model.getFollowup_date(),
                vitalsUUID,
                adultInitialUUID,
                "state",
                model.getFirst_name() + " " + model.getLast_name().substring(0, 1),
                "VisitDetailsActivity"
        );
    }

    private void cancelAppointment(PrescriptionModel model) {
        AppointmentInfo appointmentInfo = new AppointmentDAO().getAppointmentByVisitId(model.getVisitUuid());

        String visitID = model.getVisitUuid();
        int appointmentID = appointmentInfo.getId();
        String reason = "Visit was ended";
        String providerID = sessionManager.getProviderID();
        String baseurl = BuildConfig.SERVER_URL + ":3004";

        new AppointmentUtils().cancelAppointmentRequestOnVisitEnd(visitID, appointmentID, reason, providerID, baseurl);
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

      /*  if(model.getPhone_number()!=null)
            editText.setText(model.getPhone_number());*/

        try {
            String phoneNo = phoneNumber(model.getPatientUuid());
            editText.setText(phoneNo);
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }


        sharebtn.setOnClickListener(v -> {
            if (!editText.getText().toString().equalsIgnoreCase("")) {
                String phoneNumber = /*"+91" +*/ editText.getText().toString();
                String whatsappMessage = String.format("https://api.whatsapp.com/send?phone=%s&text=%s",
                        phoneNumber, context.getResources().getString(R.string.hello_thankyou_for_using_intelehealth_app_to_download_click_here)
                                + partial_whatsapp_presc_url + Uri.encode("#") + prescription_link + context.getResources().getString(R.string.and_enter_your_patient_id)
                                + model.getOpenmrs_id());
                CustomLog.v("whatsappMessage", whatsappMessage);
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
        private TextView name, fu_date_txtview, search_gender;
        private ImageView profile_image;
        private LinearLayout shareicon;


        public Myholder(@NonNull View itemView) {
            super(itemView);
            end_visit_btn = itemView.findViewById(R.id.end_visit_btn);
            fu_cardview_item = itemView.findViewById(R.id.fu_cardview_item);
            name = itemView.findViewById(R.id.fu_patname_txtview);
            search_gender = itemView.findViewById(R.id.search_gender);
            fu_date_txtview = itemView.findViewById(R.id.fu_date_txtview);
            profile_image = itemView.findViewById(R.id.profile_image);
            shareicon = itemView.findViewById(R.id.shareiconLL);
            end_visit_btn.setVisibility(View.VISIBLE);

            // Setting the priority FrameLayout as Gone since we are not showing it here.
            itemView.findViewById(R.id.fl_priority).setVisibility(View.GONE);
        }
    }

    public void profilePicDownloaded(PrescriptionModel model, EndVisitAdapter.Myholder holder) {
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
                            RequestBuilder<Drawable> requestBuilder = Glide.with(holder.itemView.getContext())
                                    .asDrawable().sizeMultiplier(0.3f);
                            Glide.with(context)
                                    .load(AppConstants.IMAGE_PATH + model.getPatientUuid() + ".jpg")
                                    .override(50, 50)
                                    //.thumbnail(requestBuilder)
                                    .centerCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
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
