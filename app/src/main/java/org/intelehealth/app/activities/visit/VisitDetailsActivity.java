package org.intelehealth.app.activities.visit;

import static org.intelehealth.app.database.dao.EncounterDAO.fetchEncounterUuidForEncounterAdultInitials;
import static org.intelehealth.app.database.dao.EncounterDAO.fetchEncounterUuidForEncounterDiagnostics;
import static org.intelehealth.app.database.dao.EncounterDAO.fetchEncounterUuidForEncounterVitals;
import static org.intelehealth.app.database.dao.EncounterDAO.getChiefComplaint;
import static org.intelehealth.app.database.dao.ObsDAO.fetchDrDetailsFromLocalDb;
import static org.intelehealth.app.database.dao.ObsDAO.getFollowupDataForVisitUUID;
import static org.intelehealth.app.database.dao.PatientsDAO.phoneNumber;
import static org.intelehealth.app.database.dao.VisitAttributeListDAO.fetchSpecialityValue;
import static org.intelehealth.app.database.dao.VisitsDAO.fetchVisitModifiedDateForPrescPending;
import static org.intelehealth.app.database.dao.VisitsDAO.isVisitNotEnded;
import static org.intelehealth.app.utilities.DateAndTimeUtils.timeAgoFormat;
import static org.intelehealth.app.utilities.StringUtils.setGenderAgeLocal;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.text.Html;
import android.util.DisplayMetrics;

import org.intelehealth.app.utilities.CustomLog;

import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.ajalt.timberkt.Timber;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.R;
import org.intelehealth.app.activities.visit.adapter.PastVisitListingAdapter;
import org.intelehealth.app.activities.visit.model.PastVisitData;
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.appointment.dao.AppointmentDAO;
import org.intelehealth.app.appointment.model.AppointmentInfo;
import org.intelehealth.app.ayu.visit.model.VisitSummaryData;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.ProviderDAO;
import org.intelehealth.app.database.dao.RTCConnectionDAO;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.models.ClsDoctorDetails;
import org.intelehealth.app.models.PrescriptionModel;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.models.dto.ProviderDTO;
import org.intelehealth.app.models.dto.RTCConnectionDTO;
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.ui.patient.activity.PatientRegistrationActivity;
import org.intelehealth.app.ui2.utils.CheckInternetAvailability;
import org.intelehealth.app.utilities.AppointmentUtils;
import org.intelehealth.app.utilities.CustomLog;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.NetworkUtils;
import org.intelehealth.app.utilities.PatientRegStage;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.VisitUtils;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.app.webrtc.activity.IDAChatActivity;
import org.intelehealth.config.room.entity.FeatureActiveStatus;
import org.intelehealth.klivekit.model.RtcArgs;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Prajwal Waingankar on 16/09/2022.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */

public class VisitDetailsActivity extends BaseActivity implements NetworkUtils.InternetCheckUpdateInterface {
    private String patientName, patientUuid, gender, age, dob, openmrsID,
            visitID, visit_startDate, visit_speciality, followupDate, followUpDate_format, patient_photo_path, chief_complaint_value;
    private boolean isEmergency, hasPrescription;
    private TextView patName_txt, gender_age_txt, openmrsID_txt, chiefComplaint_txt, visitID_txt, presc_time,
            visit_startDate_txt, visit_startTime, visit_speciality_txt, followupDate_txt, followup_info, chief_complaint_txt, followup_accept_text;
    private ImageView profile_image, icon_presc_details;
    LinearLayout priorityTag;
    public static final String TAG = "VisitDetailsActivity";
    private RelativeLayout prescription_block, endvisit_relative_block, presc_remind_block,
            followup_relative_block, followup_start_card, yes_no_followup_relative,
            vs_card, presc_relative;
    private ImageButton presc_arrowRight, vs_arrowRight, backArrow, refresh,
            pat_call_btn, pat_whatsapp_btn;
    private ImageView dr_call_btn, dr_whatsapp_btn;
    ;
    private String vitalsUUID, adultInitialUUID, obsservermodifieddate, pat_phoneno, dr_MobileNo, dr_WhatsappNo, drDetails;
    private Button btn_end_visit, yes_followup_btn;
    private ClsDoctorDetails clsDoctorDetails;
    private NetworkUtils networkUtils;

    private RecyclerView mPastVisitsRecyclerView;
    private Context context;
    private FeatureActiveStatus mFeatureActiveStatus;
    @Override
    protected void onFeatureActiveStatusLoaded(FeatureActiveStatus activeStatus) {
        super.onFeatureActiveStatusLoaded(activeStatus);
        if (activeStatus != null) {
            mFeatureActiveStatus = activeStatus;
            if (activeStatus != null && !activeStatus.getChatSection()) {
                findViewById(R.id.fabStartChat).setVisibility(View.GONE);
            } else findViewById(R.id.fabStartChat).setVisibility(View.VISIBLE);
        }
    }

        @Override
        protected void onCreate (Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            setLocale(VisitDetailsActivity.this);
            setContentView(R.layout.activity_visit_details);

            // changing status bar color
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);

            handleBackPress();

            networkUtils = new NetworkUtils(this, this);
            context = VisitDetailsActivity.this;

            Intent intent = this.getIntent(); // The intent was passed to the activity
            if (intent != null) {
                patientName = intent.getStringExtra("patientname");
                patientUuid = intent.getStringExtra("patientUuid");
                gender = intent.getStringExtra("gender");
                dob = intent.getStringExtra("dob");
                age = intent.getStringExtra("age");
                CustomLog.d("TAG", "getAge_FollowUp: s : " + age);

                openmrsID = intent.getStringExtra("openmrsID");
                visitID = intent.getStringExtra("visit_ID");
                visit_startDate = intent.getStringExtra("visit_startDate");
                visit_speciality = intent.getStringExtra("visit_speciality");

                followupDate = intent.getStringExtra("followup_date");
                if (followupDate == null)
                    followupDate = getFollowupDataForVisitUUID(visitID);
                isEmergency = intent.getBooleanExtra("priority_tag", false);
                hasPrescription = intent.getBooleanExtra("hasPrescription", false);
                patient_photo_path = intent.getStringExtra("patient_photo");
                chief_complaint_value = intent.getStringExtra("chief_complaint");
                obsservermodifieddate = intent.getStringExtra("obsservermodifieddate");
            }

            // end visit - start
            endvisit_relative_block = findViewById(R.id.endvisit_relative_block);
            btn_end_visit = findViewById(R.id.btn_end_visit);
            backArrow = findViewById(R.id.backArrow);
            refresh = findViewById(R.id.refresh);
            // end visit - end

            pat_call_btn = findViewById(R.id.pat_call_btn);
            pat_whatsapp_btn = findViewById(R.id.pat_whatsapp_btn);
            dr_call_btn = findViewById(R.id.dr_call_btn);
            dr_whatsapp_btn = findViewById(R.id.dr_whatsapp_btn);

            try {
                pat_phoneno = StringUtils.mobileNumberEmpty(phoneNumber(patientUuid));
                if (pat_phoneno.equalsIgnoreCase("N/A"))
                    pat_phoneno = "";
            } catch (DAOException e) {
                e.printStackTrace();
            }
            CustomLog.v("VD", "vd_pat_phone: " + pat_phoneno);

            // Fetching dr details from Local db.
            drDetails = fetchDrDetailsFromLocalDb(visitID);
            Gson gson = new Gson();
            clsDoctorDetails = gson.fromJson(drDetails, ClsDoctorDetails.class);

            if (clsDoctorDetails != null) {
                CustomLog.e("TAG", "TEST VISIT: " + clsDoctorDetails.toString());
                dr_MobileNo = "+91" + clsDoctorDetails.getPhoneNumber();
                dr_WhatsappNo = "+91" + clsDoctorDetails.getWhatsapp();
            }
            // end

            // calling and whatsapp - start
            pat_call_btn.setOnClickListener(v -> {
                calling_feature(pat_phoneno);
            });

            pat_whatsapp_btn.setOnClickListener(v -> {
                whatsapp_feature(pat_phoneno);
            });

        /*dr_call_btn.setOnClickListener(v -> {
            calling_feature(dr_MobileNo);
        });

        dr_whatsapp_btn.setOnClickListener(v -> {
            whatsapp_feature(dr_WhatsappNo);
        });*/
            // calling and whatsapp - end

            backArrow.setOnClickListener(v -> {
                finish();
            });

            // Patient Photo
            profile_image = findViewById(R.id.profile_image);
            if (patient_photo_path != null) {
                RequestBuilder<Drawable> requestBuilder = Glide.with(this)
                        .asDrawable().sizeMultiplier(0.3f);
                Glide.with(this)
                        .load(patient_photo_path)
                        .thumbnail(requestBuilder)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(profile_image);
            } else {
                profile_image.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.avatar1));
            }

            // visit summary - start
            vs_arrowRight = findViewById(R.id.vs_arrowRight);
            vs_card = findViewById(R.id.vs_card);
            presc_relative = findViewById(R.id.presc_relative);
            vitalsUUID = fetchEncounterUuidForEncounterVitals(visitID);
            adultInitialUUID = fetchEncounterUuidForEncounterAdultInitials(visitID);

/*
        vs_arrowRight.setOnClickListener(v -> {
            Intent in = new Intent(this, VisitSummaryActivity_New.class);
            in.putExtra("patientUuid", patientUuid);
            in.putExtra("visitUuid", visitID);
            in.putExtra("gender", gender);
            in.putExtra("name", patientName);
            in.putExtra("encounterUuidVitals", vitalsUUID);
            in.putExtra("encounterUuidAdultIntial", adultInitialUUID);
            in.putExtra("float_ageYear_Month", age);
            in.putExtra("tag", "VisitDetailsActivity");
            startActivity(in);
        });
*/
            vs_card.setOnClickListener(v -> {
                Intent in = new Intent(this, VisitSummaryActivity_New.class);
                in.putExtra("patientUuid", patientUuid);
                in.putExtra("visitUuid", visitID);
                in.putExtra("gender", gender);
                in.putExtra("name", patientName);
                in.putExtra("encounterUuidVitals", vitalsUUID);
                in.putExtra("encounterUuidAdultIntial", adultInitialUUID);
                in.putExtra("float_ageYear_Month", age);
                in.putExtra("tag", "VisitDetailsActivity");
                startActivity(in);
            });
            // visit summary - end

            // presc block - start
            prescription_block = findViewById(R.id.prescription_block);
            presc_time = findViewById(R.id.presc_time);
            presc_arrowRight = findViewById(R.id.presc_arrowRight);
            presc_remind_block = findViewById(R.id.presc_remind_block);
            icon_presc_details = findViewById(R.id.icon_presc_details);

            if (hasPrescription) {
                presc_arrowRight.setVisibility(View.VISIBLE);
                presc_relative.setClickable(true);
                presc_remind_block.setVisibility(View.GONE);
                if (!obsservermodifieddate.equalsIgnoreCase("")) {
                    //  String modifiedDate = fetchEncounterModifiedDateForPrescGiven(visitID);
                    String modifiedDate = obsservermodifieddate;
                    modifiedDate = timeAgoFormat(modifiedDate);
                    presc_time.setText(getResources().getString(R.string.received) + " " + modifiedDate);
                    icon_presc_details.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.prescription_icon));
                }

/*
            presc_arrowRight.setOnClickListener(v -> {
                Intent in = new Intent(this, PrescriptionActivity.class);
                in.putExtra("patientname", patientName);
                in.putExtra("patientUuid", patientUuid);
                in.putExtra("patient_photo", patient_photo_path);
                in.putExtra("visit_ID", visitID);
                in.putExtra("visit_startDate", visit_startDate);
                in.putExtra("gender", gender);
                in.putExtra("encounterUuidVitals", vitalsUUID);
                in.putExtra("encounterUuidAdultIntial", adultInitialUUID);
                in.putExtra("age", age);
                in.putExtra("tag", "VisitDetailsActivity");
                in.putExtra("followupDate", followupDate);
                in.putExtra("openmrsID", openmrsID);
                startActivity(in);
            });
*/
                presc_relative.setOnClickListener(v -> {
                    Intent in = new Intent(this, PrescriptionActivity.class);
                    in.putExtra("patientname", patientName);
                    in.putExtra("patientUuid", patientUuid);
                    in.putExtra("patient_photo", patient_photo_path);
                    in.putExtra("visit_ID", visitID);
                    in.putExtra("visit_startDate", visit_startDate);
                    in.putExtra("gender", gender);
                    in.putExtra("encounterUuidVitals", vitalsUUID);
                    in.putExtra("encounterUuidAdultIntial", adultInitialUUID);
                    in.putExtra("age", age);
                    in.putExtra("tag", "VisitDetailsActivity");
                    in.putExtra("followupDate", followUpDate_format);
                    in.putExtra("openmrsID", openmrsID);
                    startActivity(in);
                });
            } else {
                // if no presc given than show the dialog of remind and pending based on time passed from visit uplaoded.
                presc_arrowRight.setVisibility(View.GONE);
                presc_relative.setClickable(false);
                //   String modifiedDate = fetchVisitModifiedDateForPrescPending(visitID);

                String modifiedDate = "";
                if (!obsservermodifieddate.equalsIgnoreCase("")) {
                    modifiedDate = obsservermodifieddate;
                    modifiedDate = timeAgoFormat(modifiedDate);
                } else {
                    modifiedDate = fetchVisitModifiedDateForPrescPending(visitID);
                    modifiedDate = timeAgoFormat(modifiedDate);
                }

                if (modifiedDate.contains("minutes") || modifiedDate.contains("hours") || modifiedDate.contains("minute") || modifiedDate.contains("hour")) {
                    // here dont show remind block
                    presc_remind_block.setVisibility(View.GONE);
                } else {
                    // here show remind block as its pending from more than 1 day.
                    //presc_remind_block.setVisibility(View.VISIBLE); // show remind btn for presc to be given as its more than days.
                    presc_remind_block.setVisibility(View.GONE); // For now
                }
                String timeText = getResources().getString(R.string.pending_since) + " " + modifiedDate.replace("ago", "");
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
                    timeText = modifiedDate.replace("पहले", "") + "से पेंडिंग है";
                presc_time.setText(timeText);
                presc_time.setTextColor(ContextCompat.getColor(this, R.color.red));
                icon_presc_details.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.prescription_red_icon));
            }
            // presc block - end

            patName_txt = findViewById(R.id.patname_txt);
            patName_txt.setText(patientName);

            gender_age_txt = findViewById(R.id.gender_age_txt);
//        gender_age_txt.setText(gender + " " + age);
            setGenderAgeLocal(context, gender_age_txt, dob, gender, sessionManager);

            openmrsID_txt = findViewById(R.id.openmrsID_txt);
            openmrsID_txt.setText(openmrsID);

            // priority - start
            priorityTag = findViewById(R.id.llPriorityTagVisitDetails);
            if (isEmergency)
                priorityTag.setVisibility(View.VISIBLE);
            else
                priorityTag.setVisibility(View.GONE);
            // priority - end

            chief_complaint_txt = findViewById(R.id.chief_complaint_txt);
            if (chief_complaint_value == null)
                chief_complaint_value = getChiefComplaint(visitID);
            CustomLog.v(TAG, "chief_Complaint: " + chief_complaint_value);

        /*if (chief_complaint_value != null) {
            int first = chief_complaint_value.indexOf("<b>");
            int last = chief_complaint_value.indexOf("</b>");
            chief_complaint_value = chief_complaint_value.substring(first, last + 4);
            Log.v(TAG, "chief_Complaint: " + chief_complaint_value);
            Log.v(TAG, "a: " + first + " b: " + last + " C: " + chief_complaint_value);
        } else {  // ie. here user is coming from Prescription screen and not Follow up screen.
            chief_complaint_value = getChiefComplaint(visitID);
            Log.v(TAG, "chief_Complaint: " + chief_complaint_value);
            int first = chief_complaint_value.indexOf("<b>");
            int last = chief_complaint_value.indexOf("</b>");
            chief_complaint_value = chief_complaint_value.substring(first, last + 4);
            Log.v(TAG, "chief_Complaint: " + chief_complaint_value);
            Log.v(TAG, "a: " + first + " b: " + last + " C: " + chief_complaint_value);
        }*/

            if (chief_complaint_value != null && !chief_complaint_value.isEmpty()) {


                boolean needToShowCoreValue = false;
                if (chief_complaint_value.startsWith("{") && chief_complaint_value.endsWith("}")) {
                    try {
                        // isInOldFormat = false;
                        JSONObject jsonObject = new JSONObject(chief_complaint_value);
                        if (jsonObject.has("l-" + sessionManager.getAppLanguage())) {
                            chief_complaint_value = jsonObject.getString("l-" + sessionManager.getAppLanguage());
                            needToShowCoreValue = false;
                        } else {
                            needToShowCoreValue = true;
                            chief_complaint_value = jsonObject.getString("en");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    needToShowCoreValue = true;
                }

                if (needToShowCoreValue) {
                    chief_complaint_value = chief_complaint_value.replace("?<b>", Node.bullet_arrow);

                    String[] complaints = org.apache.commons.lang3.StringUtils.split(chief_complaint_value, Node.bullet_arrow);

                    chief_complaint_value = "";
                    String colon = ":";
                    if (complaints != null) {
                        for (String comp : complaints) {
                            if (!comp.trim().isEmpty() && comp.contains(colon)) {
                                chief_complaint_value = chief_complaint_value + Node.bullet_arrow + comp.substring(0, comp.indexOf(colon)) + "<br/>";

                            }
                        }
                        if (!chief_complaint_value.isEmpty()) {
                            chief_complaint_value = chief_complaint_value.replaceAll(Node.bullet_arrow, "");
                            chief_complaint_value = chief_complaint_value.replaceAll("<br/>", ", ");
                            chief_complaint_value = chief_complaint_value.replaceAll(Node.ASSOCIATE_SYMPTOMS, "");
                            //visitValue = visitValue.substring(0, visitValue.length() - 2);
                            chief_complaint_value = chief_complaint_value.replaceAll("<b>", "");
                            chief_complaint_value = chief_complaint_value.replaceAll("</b>", "");
                            chief_complaint_value = chief_complaint_value.trim();
                            while (chief_complaint_value.endsWith(",")) {
                                chief_complaint_value = chief_complaint_value.substring(0, chief_complaint_value.length() - 1).trim();
                            }
                        }
                    }
                    chief_complaint_txt.setText(Html.fromHtml(chief_complaint_value));
                } else {
                    chief_complaint_value = chief_complaint_value.replaceAll("<.*?>", "");
                    System.out.println(chief_complaint_value);
                    CustomLog.v(TAG, chief_complaint_value);
                    //►दस्त::● आपको ये लक्षण कब से है• 6 घंटे● दस्त शुरू कैसे हुए?•धीरे धीरे● २४ घंटे में कितनी बार दस्त हुए?•३ से कम बार● दस्त किस प्रकार के है?•पक्का● क्या आपको पिछले महीनो में दस्त शुरू होने से पहले किसी असामान्य भोजन/तरल पदार्थ से अपच महसूस हुआ है•नहीं● क्या आपने आज यहां आने से पहले इस समस्या के लिए कोई उपचार (स्व-दवा या घरेलू उपचार सहित) लिया है या किसी स्वास्थ्य प्रदाता को दिखाया है?•कोई नहीं● अतिरिक्त जानकारी•bsbdbd►क्या आपको निम्न लक्षण है::•उल्टीPatient denies -•दस्त के साथ पेट दर्द•सुजन•मल में खून•बुखार•अन्य [वर्णन करे]

                    String[] spt = chief_complaint_value.split("►");
                    List<String> list = new ArrayList<>();
                    StringBuilder stringBuilder = new StringBuilder();
                    for (String s : spt) {
                        String complainName = "";
                        if (s.isEmpty()) continue;
                        //String s1 =  new String(s.getBytes(), "UTF-8");
                        System.out.println(s);
                        String[] spt1 = s.split("::●");
                        complainName = spt1[0];

                        //if (s.trim().startsWith(getTranslatedAssociatedSymptomQString(lCode))) {
                        if (!complainName.trim().contains(org.intelehealth.app.ayu.visit.common.VisitUtils.getTranslatedPatientDenies(sessionManager.getAppLanguage()))) {
                            System.out.println(complainName);
                            if (!stringBuilder.toString().isEmpty()) stringBuilder.append(", ");
                            stringBuilder.append(complainName);
                        }

                    }


                    chief_complaint_txt.setText(stringBuilder.toString());
                }
            }
            chief_complaint_txt.setTextColor(ContextCompat.getColor(this, R.color.headline_text_color));
            chief_complaint_txt.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.fu_name_txt_size));
            //chief_complaint_txt.setText(Html.fromHtml(chief_complaint_value));

            visitID_txt = findViewById(R.id.visitID);
            String hideVisitUUID = visitID;
            hideVisitUUID = hideVisitUUID.substring(hideVisitUUID.length() - 4, hideVisitUUID.length());
            visitID_txt.setText(getResources().getString(R.string.visitID) + " XXXX" + hideVisitUUID);

            // Start Date and Time - start
            visit_startDate_txt = findViewById(R.id.visit_startDate);
            visit_startTime = findViewById(R.id.visit_startTime);

            if (visit_startDate != null) {
                CustomLog.v("Followup", "actual date: " + visit_startDate);

                // Time - start
                String startTime = DateAndTimeUtils.date_formatter(visit_startDate,
                        "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                        "HH:mm a");    // Eg. 26 Sep 2022 at 03:15 PM
                CustomLog.v("SearchPatient", "date: " + startTime);
                visit_startTime.setText(startTime);
                // Time - end

                visit_startDate = DateAndTimeUtils.date_formatter(visit_startDate, "yyyy-MM-dd", "dd MMMM yyyy");
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
                    visit_startDate = StringUtils.en__hi_dob(visit_startDate);
                CustomLog.v("Followup", "foramted date: " + visit_startDate);
                visit_startDate_txt.setText(visit_startDate);
            }

            // Start Date and Time - end

            // speciality - start
            visit_speciality_txt = findViewById(R.id.visit_speciality);
            visit_speciality = fetchSpecialityValue(visitID);
            visit_speciality_txt.setText(visit_speciality);

       /* if (visit_speciality != null)
            visit_speciality_txt.setText(visit_speciality);
        else {
            visit_speciality = fetchSpecialityValue(visitID);
            visit_speciality_txt.setText(visit_speciality);
        }*/
            // speciality - end

            // follow up - start
            followup_relative_block = findViewById(R.id.followup_relative_block);   // entire block of follow up section.
            followup_start_card = findViewById(R.id.followup_start_card);   // Block that shows to Start Follow up.
            yes_no_followup_relative = findViewById(R.id.yes_no_followup_relative);   // yes no button for follow up.
            followupDate_txt = findViewById(R.id.followup_date_txtv);
            followup_info = findViewById(R.id.followup_info);
            yes_followup_btn = findViewById(R.id.yes_followup_btn);
            followup_accept_text = findViewById(R.id.followup_accept_text);

            if (followupDate != null) {
                String originalFollowUpDate = followupDate;
                followUpDate_format = DateAndTimeUtils.date_formatter(followupDate, "yyyy-MM-dd", "dd MMMM,yyyy");
                followup_relative_block.setVisibility(View.VISIBLE);
                yes_no_followup_relative.setVisibility(View.VISIBLE);
                followupDate = DateAndTimeUtils.date_formatter(followupDate, "yyyy-MM-dd", "dd MMMM");
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
                    followupDate = StringUtils.en__hi_dob(followupDate);
                if (followupDate != null && !followupDate.isEmpty()) {
                    followupDate_txt.setText(getResources().getString(R.string.follow_up_on) + " " + followupDate);
                    followup_info.setText(getResources().getString(R.string.please_take) + " " + patientName + getResources().getString(R.string.s_follow_up_visit));

                    if (DateAndTimeUtils.isCurrentDateBeforeFollowUpDate(originalFollowUpDate, "yyyy-MM-dd")) {
                        String followUpAcceptText = getResources().getString(R.string.doctor_suggested_follow_up_on, followUpDate_format);
                        if (sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
                            followUpAcceptText = StringUtils.en__hi_dob(followUpAcceptText);
                        followup_accept_text.setText(followUpAcceptText);
                    } else {
                        followup_accept_text.setText(getResources().getString(R.string.follow_up_date_arrived));
                    }
                } else {
                    followup_relative_block.setVisibility(View.GONE);
                    yes_no_followup_relative.setVisibility(View.GONE);
                }

                CustomLog.v("vd", "vd: " + followup_info);
            } else {
                followup_relative_block.setVisibility(View.GONE);
                yes_no_followup_relative.setVisibility(View.GONE);
            }

            yes_followup_btn.setOnClickListener(v -> {
                yes_no_followup_relative.setVisibility(View.GONE);
                followup_start_card.setVisibility(View.VISIBLE);
            });
            // follow up - end

            // end visit - start
            PrescriptionModel pres = isVisitNotEnded(visitID);
            if (pres.getVisitUuid() != null) {
                endvisit_relative_block.setVisibility(View.VISIBLE);
                btn_end_visit.setOnClickListener(v -> {
                    if (!hasPrescription) {
                        if (mFeatureActiveStatus.getRestrictEndVisit()) {
                            //added restrictEndVisit because in NAS - we cant end the visit is prescription not shared by dr -Nas-ida migration
                            DialogUtils dialogUtils = new DialogUtils();
                            dialogUtils.showCommonDialog(context, R.drawable.dialog_close_visit_icon, context.getResources().getString(R.string.alert_label_txt), context.getResources().getString(R.string.prescription_notprovided_msg), true, context.getResources().getString(R.string.ok), context.getResources().getString(R.string.cancel), action -> {
                            });
                        } else {
                            checkIfAppointmentExistsForVisit(visitID);
                        }
                    } else {
                        triggerEndVisit();
                    }
                });
            } else {
                endvisit_relative_block.setVisibility(View.GONE);
            }
            // end visit - end

            mPastVisitsRecyclerView = findViewById(R.id.rcv_past_visits);
            mPastVisitsRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
            initForPastVisit();

            mBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    //Toast.makeText(context, getString(R.string.sync_completed), Toast.LENGTH_SHORT).show();
                    CustomLog.v(TAG, "Sync Done!");
                    refresh.clearAnimation();
                    syncAnimator.cancel();
                    recreate();
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

            syncAnimator = ObjectAnimator.ofFloat(refresh, View.ROTATION, 0f, 359f).setDuration(1200);
            syncAnimator.setRepeatCount(ValueAnimator.INFINITE);
            syncAnimator.setInterpolator(new LinearInterpolator());
        }

        private BroadcastReceiver mBroadcastReceiver;
        private ObjectAnimator syncAnimator;
        private List<PastVisitData> mPastVisitDataList = new ArrayList<PastVisitData>();


        @Override
        protected void attachBaseContext (Context newBase){
            super.attachBaseContext(setLocale(newBase));
        }

        SessionManager sessionManager;

        public Context setLocale (Context context){
            sessionManager = new SessionManager(context);
            String appLanguage = sessionManager.getAppLanguage();
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

        private void initForPastVisit () {
            mPastVisitDataList.clear();
            SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
            String visitSelection = "uuid != ? and patientuuid = ? and enddate IS NOT NULL and enddate != ''";
            String[] visitArgs = {visitID, patientUuid};
            String[] visitColumns = {"uuid", "startdate", "enddate"};
            String visitOrderBy = "startdate";
            Cursor visitCursor = db.query("tbl_visit", visitColumns, visitSelection, visitArgs, null, null, visitOrderBy);
            if (visitCursor == null || visitCursor.getCount() <= 0) {
                findViewById(R.id.cv_past_visits).setVisibility(View.GONE);
            } else {
                findViewById(R.id.cv_past_visits).setVisibility(View.VISIBLE);
                if (visitCursor.moveToLast()) {
                    do {
                        EncounterDAO encounterDAO = new EncounterDAO();
                        String date = visitCursor.getString(visitCursor.getColumnIndexOrThrow("startdate"));
                        String end_date = visitCursor.getString(visitCursor.getColumnIndexOrThrow("enddate"));
                        String visit_id = visitCursor.getString(visitCursor.getColumnIndexOrThrow("uuid"));

                        boolean isCompletedExitedSurvey = false;
                        try {
                            isCompletedExitedSurvey = new EncounterDAO().isCompletedExitedSurvey(visit_id);
                        } catch (DAOException e) {
                            e.printStackTrace();
                        }
                        if (isCompletedExitedSurvey) {

                            String encounterlocalAdultintial = "";
                            String encountervitalsLocal = null;
                            String encounterIDSelection = "visituuid = ?";

                            String[] encounterIDArgs = {visit_id};

                            Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null);
                            if (encounterCursor != null && encounterCursor.moveToFirst()) {
                                do {
                                    if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_VITALS").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                                        encountervitalsLocal = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                                    }
                                    if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_ADULTINITIAL").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                                        encounterlocalAdultintial = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                                    }

                                } while (encounterCursor.moveToNext());
                            }
                            encounterCursor.close();

                            String previsitSelection = "encounteruuid = ? AND conceptuuid = ? and voided !='1'";
                            String[] previsitArgs = {encounterlocalAdultintial, UuidDictionary.CURRENT_COMPLAINT};
                            String[] previsitColumms = {"value", " conceptuuid", "encounteruuid"};
                            Cursor previsitCursor = db.query("tbl_obs", previsitColumms, previsitSelection, previsitArgs, null, null, null);
                            if (previsitCursor != null && previsitCursor.moveToLast()) {

                                String visitValue = previsitCursor.getString(previsitCursor.getColumnIndexOrThrow("value"));
                                boolean needToShowCoreValue = false;
                                if (visitValue.startsWith("{") && visitValue.endsWith("}")) {
                                    try {
                                        // isInOldFormat = false;
                                        JSONObject jsonObject = new JSONObject(visitValue);
                                        if (jsonObject.has("l-" + sessionManager.getAppLanguage())) {
                                            visitValue = jsonObject.getString("l-" + sessionManager.getAppLanguage());
                                            needToShowCoreValue = false;
                                        } else {
                                            needToShowCoreValue = true;
                                            visitValue = jsonObject.getString("en");
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    needToShowCoreValue = true;
                                }
                                if (visitValue != null && !visitValue.isEmpty()) {

                                    if (needToShowCoreValue) {
                                        visitValue = visitValue.replace("?<b>", Node.bullet_arrow);

                                        String[] complaints = org.apache.commons.lang3.StringUtils.split(visitValue, Node.bullet_arrow);

                                        visitValue = "";
                                        String colon = ":";
                                        if (complaints != null) {
                                            for (String comp : complaints) {
                                                if (!comp.trim().isEmpty()) {
                                                    visitValue = visitValue + Node.bullet_arrow + comp.substring(0, comp.indexOf(colon)) + "<br/>";

                                                }
                                            }
                                            if (!visitValue.isEmpty()) {
                                                visitValue = visitValue.replaceAll(Node.bullet_arrow, "");
                                                visitValue = visitValue.replaceAll("<br/>", "");
                                                visitValue = visitValue.replaceAll(Node.ASSOCIATE_SYMPTOMS, "");
                                                //visitValue = visitValue.substring(0, visitValue.length() - 2);
                                                visitValue = visitValue.replaceAll("<b>", "");
                                                visitValue = visitValue.replaceAll("</b>", "");
                                            }
                                        }
                                    } else {
                                        String chiefComplain = "";
                                        visitValue = visitValue.replaceAll("<.*?>", "");
                                        System.out.println(visitValue);
                                        CustomLog.v(TAG, visitValue);
                                        //►दस्त::● आपको ये लक्षण कब से है• 6 घंटे● दस्त शुरू कैसे हुए?•धीरे धीरे● २४ घंटे में कितनी बार दस्त हुए?•३ से कम बार● दस्त किस प्रकार के है?•पक्का● क्या आपको पिछले महीनो में दस्त शुरू होने से पहले किसी असामान्य भोजन/तरल पदार्थ से अपच महसूस हुआ है•नहीं● क्या आपने आज यहां आने से पहले इस समस्या के लिए कोई उपचार (स्व-दवा या घरेलू उपचार सहित) लिया है या किसी स्वास्थ्य प्रदाता को दिखाया है?•कोई नहीं● अतिरिक्त जानकारी•bsbdbd►क्या आपको निम्न लक्षण है::•उल्टीPatient denies -•दस्त के साथ पेट दर्द•सुजन•मल में खून•बुखार•अन्य [वर्णन करे]

                                        String[] spt = visitValue.split("►");
                                        List<String> list = new ArrayList<>();

                                        for (String s : spt) {
                                            if (s.isEmpty()) continue;
                                            //String s1 =  new String(s.getBytes(), "UTF-8");
                                            System.out.println(s);
                                            //if (s.trim().startsWith(getTranslatedAssociatedSymptomQString(lCode))) {
                                            if (!s.trim().contains(org.intelehealth.app.ayu.visit.common.VisitUtils.getTranslatedPatientDenies(sessionManager.getAppLanguage()))) {
                                                list.add(s);
                                            }

                                        }
                                        StringBuilder stringBuilder = new StringBuilder();
                                        int size = list.size() == 1 ? list.size() : list.size() - 1;
                                        for (int i = 0; i < size; i++) {
                                            String complainName = "";
                                            List<VisitSummaryData> visitSummaryDataList = new ArrayList<>();
                                            String[] spt1 = list.get(i).split("●");
                                            for (String value : spt1) {
                                                if (value.contains("::")) {
                                                    if (!stringBuilder.toString().isEmpty())
                                                        stringBuilder.append(",");
                                                    complainName = value.replace("::", "");
                                                    System.out.println(complainName);
                                                    stringBuilder.append(complainName);
                                                }
                                            }
                                        }
                                        visitValue = stringBuilder.toString();
                                    }
                                    SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                                    try {

                                        Date formatted = currentDate.parse(date);
                                        String visitDate = currentDate.format(formatted);
                                        //createOldVisit(visitDate, visit_id, end_date, visitValue, encountervitalsLocal, encounterlocalAdultintial);
                                        PastVisitData pastVisitData = new PastVisitData();
                                        pastVisitData.setVisitDate(visitDate);
                                        pastVisitData.setVisitUUID(visit_id);
                                        pastVisitData.setChiefComplain(visitValue);
                                        pastVisitData.setEncounterVitals(encountervitalsLocal);
                                        pastVisitData.setEncounterAdultInitial(encounterlocalAdultintial);
                                        mPastVisitDataList.add(pastVisitData);
                                        CustomLog.v(TAG, new Gson().toJson(mPastVisitDataList));

                                    } catch (ParseException e) {
                                        FirebaseCrashlytics.getInstance().recordException(e);
                                    }
                                }
                            }


                        }
                    } while (visitCursor.moveToPrevious());
                }

                if (!mPastVisitDataList.isEmpty()) {
                    PastVisitListingAdapter pastVisitListingAdapter = new PastVisitListingAdapter(mPastVisitsRecyclerView, VisitDetailsActivity.this, mPastVisitDataList, new PastVisitListingAdapter.OnItemSelected() {
                        @Override
                        public void onItemSelected(PastVisitData pastVisitData) {
                            Intent in = new Intent(VisitDetailsActivity.this, VisitSummaryActivity_New.class);
                            in.putExtra("patientUuid", patientUuid);
                            in.putExtra("visitUuid", pastVisitData.getVisitUUID());
                            in.putExtra("gender", gender);
                            in.putExtra("name", patientName);
                            in.putExtra("encounterUuidVitals", pastVisitData.getEncounterVitals());
                            in.putExtra("encounterUuidAdultIntial", pastVisitData.getEncounterAdultInitial());
                            in.putExtra("float_ageYear_Month", age);
                            in.putExtra("tag", "VisitDetailsActivity");
                            startActivity(in);
                        }
                    });
                    mPastVisitsRecyclerView.setAdapter(pastVisitListingAdapter);
                }
            }
        }

        /**
         * This will open Whatsapp with a pre-defined message to be sent to the user.
         *
         * @param phoneno
         */
        // TODO: check with Sagar for this message to be passed...
        private void whatsapp_feature (String phoneno){
            try {
                String nurseName = new ProviderDAO().getProviderName(sessionManager.getProviderID(), ProviderDTO.Columns.PROVIDER_UUID.value);
                if (phoneno != null && !phoneno.equalsIgnoreCase("")) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(
                                    String.format("https://api.whatsapp.com/send?phone=%s&text=%s",
                                            phoneno, getResources().getString(R.string.nurse_whatsapp_message, nurseName)))));
                } else {
                    Toast.makeText(VisitDetailsActivity.this, getResources().getString(R.string.mobile_no_not_provided), Toast.LENGTH_SHORT).show();
                }
            } catch (DAOException e) {
                throw new RuntimeException(e);
            }

        }

        /**
         * This will open up Dialer with the phone number passed for user to initiate call.
         *
         * @param phoneno
         */
        private void calling_feature (String phoneno){
            if (phoneno != null && !phoneno.equalsIgnoreCase("")) {
                Intent i1 = new Intent(Intent.ACTION_DIAL);
                i1.setData(Uri.parse("tel:" + phoneno));
                startActivity(i1);
            } else {
                Toast.makeText(VisitDetailsActivity.this, getResources().getString(R.string.mobile_no_not_provided), Toast.LENGTH_SHORT).show();
            }
        }

        private void handleBackPress () {
            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    finish();
                }
            });
        }

        @Override
        public void updateUIForInternetAvailability ( boolean isInternetAvailable){
            CustomLog.d("TAG", "updateUIForInternetAvailability: ");
            if (isInternetAvailable) {
                refresh.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ui2_ic_internet_available));
            } else {
                refresh.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ui2_ic_no_internet));
            }
        }

        @Override
        public void onStart () {
            super.onStart();
            //register receiver for internet check
            networkUtils.callBroadcastReceiver();
        }

        @Override
        public void onStop () {
            super.onStop();
            try {
                //unregister receiver for internet check
                networkUtils.unregisterNetworkReceiver();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        public void startTextChat (View view){
            if (!CheckInternetAvailability.isNetworkAvailable(this)) {
                Toast.makeText(this, getString(R.string.not_connected_txt), Toast.LENGTH_SHORT).show();
                return;
            }
            EncounterDAO encounterDAO = new EncounterDAO();
            EncounterDTO encounterDTO = encounterDAO.getEncounterByVisitUUID(visitID);
            RTCConnectionDAO rtcConnectionDAO = new RTCConnectionDAO();
            RTCConnectionDTO rtcConnectionDTO = rtcConnectionDAO.getByVisitUUID(visitID);
            RtcArgs args = new RtcArgs();
            if (rtcConnectionDTO != null) {
                args.setDoctorUuid(rtcConnectionDTO.getConnectionInfo());
                args.setPatientId(patientUuid);
                args.setPatientName(patientName);
                args.setVisitId(visitID);
                args.setNurseId(encounterDTO.getProvideruuid());
                IDAChatActivity.startChatActivity(VisitDetailsActivity.this, args);
            } else {
                //chatIntent.putExtra("toUuid", ""); // assigned doctor uuid
                Toast.makeText(this, getResources().getString(R.string.wait_for_the_doctor_message), Toast.LENGTH_SHORT).show();
            }
        }

        public void startVideoChat (View view){
            Toast.makeText(this, getString(R.string.video_call_req_sent), Toast.LENGTH_SHORT).show();
        /*EncounterDAO encounterDAO = new EncounterDAO();
        EncounterDTO encounterDTO = encounterDAO.getEncounterByVisitUUIDLimit1(visitID);
        RTCConnectionDAO rtcConnectionDAO = new RTCConnectionDAO();
        RTCConnectionDTO rtcConnectionDTO = rtcConnectionDAO.getByVisitUUID(visitID);
        Intent in = new Intent(VisitDetailsActivity.this, CompleteActivity.class);
        String roomId = patientUuid;
        String doctorName = "";
        String nurseId = encounterDTO.getProvideruuid();
        in.putExtra("roomId", roomId);
        in.putExtra("isInComingRequest", false);
        in.putExtra("doctorname", doctorName);
        in.putExtra("nurseId", nurseId);
        in.putExtra("startNewCall", true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        int callState = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getCallState();
        if (callState == TelephonyManager.CALL_STATE_IDLE) {
            startActivity(in);
        }*/

        }

        public void editPatientInfo (View view){
            PatientDTO patientDTO = new PatientDTO();
            String patientSelection = "uuid = ?";
            String[] patientArgs = {patientUuid};
            String[] patientColumns = {"uuid", "openmrs_id", "first_name", "middle_name", "last_name", "gender",
                    "date_of_birth", "address1", "address2", "city_village", "state_province",
                    "postal_code", "country", "phone_number", "gender", "sdw",
                    "patient_photo"};
            SQLiteDatabase db = db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
            Cursor idCursor = db.query("tbl_patient", patientColumns, patientSelection, patientArgs, null, null, null);
            if (idCursor.moveToFirst()) {
                do {
                    patientDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                    patientDTO.setOpenmrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("openmrs_id")));
                    patientDTO.setFirstname(idCursor.getString(idCursor.getColumnIndexOrThrow("first_name")));
                    patientDTO.setMiddlename(idCursor.getString(idCursor.getColumnIndexOrThrow("middle_name")));
                    patientDTO.setLastname(idCursor.getString(idCursor.getColumnIndexOrThrow("last_name")));
                    patientDTO.setGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                    patientDTO.setDateofbirth(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                    patientDTO.setAddress1(idCursor.getString(idCursor.getColumnIndexOrThrow("address1")));
                    patientDTO.setAddress2(idCursor.getString(idCursor.getColumnIndexOrThrow("address2")));
                    patientDTO.setCityvillage(idCursor.getString(idCursor.getColumnIndexOrThrow("city_village")));
                    patientDTO.setStateprovince(idCursor.getString(idCursor.getColumnIndexOrThrow("state_province")));
                    patientDTO.setPostalcode(idCursor.getString(idCursor.getColumnIndexOrThrow("postal_code")));
                    patientDTO.setCountry(idCursor.getString(idCursor.getColumnIndexOrThrow("country")));
                    patientDTO.setPhonenumber(idCursor.getString(idCursor.getColumnIndexOrThrow("phone_number")));
                    patientDTO.setGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                    patientDTO.setPatientPhoto(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_photo")));
                } while (idCursor.moveToNext());
            }
            idCursor.close();

            String patientSelection1 = "patientuuid = ?";
            String[] patientArgs1 = {patientUuid};
            String[] patientColumns1 = {"value", "person_attribute_type_uuid"};
            Cursor idCursor1 = db.query("tbl_patient_attribute", patientColumns1, patientSelection1, patientArgs1, null, null, null);
            String name = "";
            if (idCursor1.moveToFirst()) {
                do {
                    try {
                        name = new PatientsDAO().getAttributesName(idCursor1.getString(idCursor1.getColumnIndexOrThrow("person_attribute_type_uuid")));
                    } catch (DAOException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }

                    if (name.equalsIgnoreCase("caste")) {
                        patientDTO.setCaste(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                    }
                    if (name.equalsIgnoreCase("Telephone Number")) {
                        patientDTO.setPhonenumber(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                    }
                    if (name.equalsIgnoreCase("Education Level")) {
                        patientDTO.setEducation(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                    }
                    if (name.equalsIgnoreCase("Economic Status")) {
                        patientDTO.setEconomic(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                    }
                    if (name.equalsIgnoreCase("occupation")) {
                        patientDTO.setOccupation(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                    }
                    if (name.equalsIgnoreCase("Son/wife/daughter")) {
                        patientDTO.setSon_dau_wife(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                    }
                    if (name.equalsIgnoreCase("ProfileImageTimestamp")) {

                    }
                    if (name.equalsIgnoreCase("createdDate")) {
                        patientDTO.setCreatedDate(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                    }
                    if (name.equalsIgnoreCase("providerUUID")) {
                        patientDTO.setProviderUUID(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                    }

                } while (idCursor1.moveToNext());
            }
            idCursor1.close();

            PatientRegistrationActivity.startPatientRegistration(this, patientDTO.getUuid(), PatientRegStage.PERSONAL);
//        Intent intent2 = new Intent(this, IdentificationActivity_New.class);
//        intent2.putExtra("patientUuid", patientDTO.getUuid());
//        intent2.putExtra("ScreenEdit", "personal_edit");
//        intent2.putExtra("patient_detail", true);
//
//        Bundle args = new Bundle();
//        args.putSerializable("patientDTO", (Serializable) patientDTO);
//        intent2.putExtra("BUNDLE", args);
//        startActivity(intent2);
        }

        public void syncNow (View view){
            if (NetworkConnection.isOnline(this)) {
                refresh.clearAnimation();
                syncAnimator.start();
                new SyncUtils().syncBackground();
                //Toast.makeText(this, getString(R.string.sync_strated), Toast.LENGTH_SHORT).show();
            }
        }

        private void checkIfAppointmentExistsForVisit (String visitUUID){
            // First check if there is an appointment or not
            AppointmentDAO appointmentDAO = new AppointmentDAO();
            if (!appointmentDAO.doesAppointmentExistForVisit(visitUUID)) {
                triggerEndVisit();
                return;
            }

            String appointmentDateTime = appointmentDAO.getTimeAndDateForAppointment(visitUUID);
            boolean isCurrentTimeAfterAppointmentTime = DateAndTimeUtils.isCurrentDateTimeAfterAppointmentTime(appointmentDateTime);

            // Next, check if the time for appointment is passed. In case the time has passed, we don't need to cancel the appointment as it is automatically completed.
            if (isCurrentTimeAfterAppointmentTime) {
                triggerEndVisit();
                return;
            }

            // In case the appointment time is not passed, only in that case, we will display the dialog for ending the appointment.
            new DialogUtils().triggerEndAppointmentConfirmationDialog(this, action -> {
                if (action == DialogUtils.CustomDialogListener.POSITIVE_CLICK) {
                    cancelAppointment(visitUUID);
                    triggerEndVisit();
                }
            });
        }

        private void triggerEndVisit () {
            VisitUtils.endVisit(VisitDetailsActivity.this, visitID, patientUuid, followupDate, vitalsUUID, adultInitialUUID, "state", patientName, "VisitDetailsActivity");
        }

        private void cancelAppointment (String visitUUID){
            AppointmentInfo appointmentInfo = new AppointmentDAO().getAppointmentByVisitId(visitUUID);

            int appointmentID = appointmentInfo.getId();
            String reason = "Visit was ended";
            String providerID = sessionManager.getProviderID();
            String baseurl = BuildConfig.SERVER_URL + ":3004";

            new AppointmentUtils().cancelAppointmentRequestOnVisitEnd(visitUUID, appointmentID, reason, providerID, baseurl);
        }
    }