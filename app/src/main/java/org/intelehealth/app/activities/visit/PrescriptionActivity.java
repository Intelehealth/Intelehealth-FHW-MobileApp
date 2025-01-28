package org.intelehealth.app.activities.visit;

import static org.intelehealth.app.app.AppConstants.CONFIG_FILE_NAME;
import static org.intelehealth.app.ayu.visit.common.VisitUtils.getTranslatedAssociatedSymptomQString;
import static org.intelehealth.app.ayu.visit.common.VisitUtils.getTranslatedPatientDenies;
import static org.intelehealth.app.database.dao.EncounterDAO.getStartVisitNoteEncounterByVisitUUID;
import static org.intelehealth.app.database.dao.ObsDAO.fetchDrDetailsFromLocalDb;
import static org.intelehealth.app.utilities.DateAndTimeUtils.parse_DateToddMMyyyy;
import static org.intelehealth.app.utilities.DateAndTimeUtils.parse_DateToddMMyyyy_new;
import static org.intelehealth.app.utilities.UuidDictionary.PRESCRIPTION_LINK;
import static org.intelehealth.app.utilities.VisitUtils.endVisit;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.LocaleList;
import android.print.PdfPrint;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import org.intelehealth.app.activities.prescription.thermalprinter.PrintViewPrescription;
import org.intelehealth.app.activities.prescription.thermalprinter.PrintViewPrescriptionTest;
import org.intelehealth.app.activities.prescription.thermalprinter.PrintViewPrescriptionDataModel;
import org.intelehealth.app.utilities.CustomLog;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.activities.prescription.PrescriptionBuilder;
import org.intelehealth.app.activities.visit.adapter.PrescribedMedicineAdapter;
import org.intelehealth.app.activities.visit.model.PrescribedMedicineModel;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.appointment.dao.AppointmentDAO;
import org.intelehealth.app.appointment.model.AppointmentInfo;
import org.intelehealth.app.ayu.visit.model.VisitSummaryData;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.ProviderDAO;
import org.intelehealth.app.database.dao.VisitAttributeListDAO;
import org.intelehealth.app.database.dao.VisitsDAO;
import org.intelehealth.app.databinding.ActivityPrescription2Binding;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.models.ClsDoctorDetails;
import org.intelehealth.app.models.Patient;
import org.intelehealth.app.models.VitalsObject;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.models.dto.ProviderDTO;
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.ui.patient.activity.PatientRegistrationActivity;
import org.intelehealth.app.utilities.AppointmentUtils;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.NetworkUtils;
import org.intelehealth.app.utilities.PatientRegStage;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.config.room.entity.FeatureActiveStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Created by Prajwal Waingankar on 4/11/2022.
 * Github : @prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */
@SuppressLint("Range")
public class PrescriptionActivity extends BaseActivity implements NetworkUtils.InternetCheckUpdateInterface {
    private static final String TAG = "PrescriptionActivity";
    private String patientName, patientUuid, gender, age, openmrsID, vitalsUUID, adultInitialUUID, intentTag, visitID, visit_startDate, visit_speciality, patient_photo_path, chief_complaint_value;
    private ImageButton btn_up_header, btnup_drdetails_header, btnup_diagnosis_header, btnup_medication_header, btnup_test_header, btnup_speciality_header, btnup_followup_header, no_btn, yes_btn, downloadBtn;
    private LinearLayout presc_profile_header;
    private RelativeLayout dr_details_header_relative, diagnosis_header_relative, medication_header_relative, advice_header_relative, test_header_relative, referred_header_relative, followup_header_relative;
    private RelativeLayout vs_header_expandview, vs_drdetails_header_expandview, vs_diagnosis_header_expandview, vs_medication_header_expandview, vs_adviceheader_expandview, vs_testheader_expandview, vs_speciality_header_expandview, vs_followup_header_expandview, followup_date_block;
    private TextView patName_txt, gender_age_txt, openmrsID_txt, chiefComplaint_txt, visitID_txt, presc_time, mCHWname, drname, dr_age_gender, qualification, dr_speciality, reminder, incomplete_act, archieved_notifi, diagnosis_txt, test_txt, advice_txt, referred_speciality_txt, no_followup_txt, followup_date_txt, followup_subtext;
    private ImageView priorityTag, profile_image;
    private ActivityPrescription2Binding mBinding;
    private SessionManager sessionManager;
    String diagnosisReturned = "", rxReturned = "", testsReturned = "", referredSpeciality = "", adviceReturned = "", doctorName = "", additionalReturned = "", followUpDate = "";
    String medicalAdvice_string = "", medicalAdvice_HyperLink = "";
    private SQLiteDatabase db;
    private Patient patient = new Patient();
    private boolean hasPrescription = false;
    boolean downloaded = false;
    String encounterUuid;
    DownloadPrescriptionService downloadPrescriptionService;
    Boolean isReceiverRegistered = false;
    NetworkChangeReceiver receiver;
    private boolean isConnected = false;
    boolean uploaded = false;
    MenuItem internetCheck = null;
    String visitnoteencounteruuid = "";
    Button uploadButton, btn_vs_print, btn_vs_share;
    private WebView mWebView;
    public static String prescription1;
    public static String prescription2;
    boolean hasLicense = false, isRespiratory = false;
    private static String mFileName = CONFIG_FILE_NAME;
    private ImageButton backArrow, refresh, filter;
    private NetworkUtils networkUtils;
    private ObjectAnimator syncAnimator;
    ObsDTO complaint = new ObsDTO();
    ObsDTO famHistory = new ObsDTO();
    ObsDTO patHistory = new ObsDTO();
    ObsDTO phyExam = new ObsDTO();
    ObsDTO height = new ObsDTO();
    ObsDTO weight = new ObsDTO();
    ObsDTO pulse = new ObsDTO();
    ObsDTO bpSys = new ObsDTO();
    ObsDTO bpDias = new ObsDTO();
    ObsDTO temperature = new ObsDTO();
    ObsDTO spO2 = new ObsDTO();
    ObsDTO resp = new ObsDTO();
    String medHistory;
    String sign_url;
    String mHeight, mWeight, mBMI, mBP, mPulse, mTemp, mSPO2, mresp;
    private static final int GROUP_PERMISSION_REQUEST = 1000;
    String encounterVitals, encounterUuidAdultIntial, EncounterAdultInitial_LatestVisit;
    private FrameLayout filter_framelayout;
    private View hl_2;
    public static final String FILTER = "io.intelehealth.client.activities.visit_summary_activity.REQUEST_PROCESSED";
    private String medicalAdvice;
    ObsDTO bloodGlucoseRandom = new ObsDTO();
    ObsDTO bloodGlucoseFasting = new ObsDTO();
    ObsDTO bloodGlucosePostPrandial = new ObsDTO();
    ObsDTO hemoglobin = new ObsDTO();
    ObsDTO uricAcid = new ObsDTO();
    ObsDTO cholesterol = new ObsDTO();
    String mBloodGlucoseRandom, mBloodGlucoseFasting, mBloodGlucosePostPrandial, mHemoglobin, mUricAcid, mCholesterol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_prescription2);

        // Status Bar color -> White
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(Color.WHITE);


        initUI();
        networkUtils = new NetworkUtils(this, this);
        fetchIntent();
        setDataToView();
        expandableCardVisibilityHandling();
    }

    private FeatureActiveStatus mFeatureActiveStatus;

    @Override
    protected void onFeatureActiveStatusLoaded(FeatureActiveStatus activeStatus) {
        super.onFeatureActiveStatusLoaded(activeStatus);
        if (activeStatus != null) {
            mFeatureActiveStatus = activeStatus;
            Log.d(TAG, "onFeatureActiveStatusLoaded: mFeatureActiveStatus : " + mFeatureActiveStatus.getPrintUsingThermalPrinter());
            ((Button) findViewById(R.id.btn_vs_print)).setText(getString(activeStatus.getPrintUsingThermalPrinter() ? R.string.view_print : R.string.action_print));
        }
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

    private void initUI() {
        db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();

        filter_framelayout = findViewById(R.id.filter_framelayout);
        filter = findViewById(R.id.filter);

        reminder = findViewById(R.id.reminder);
        reminder.setText(getResources().getString(R.string.action_home));

        incomplete_act = findViewById(R.id.incomplete_act);
        incomplete_act.setText(getResources().getString(R.string.action_end_visit));

        archieved_notifi = findViewById(R.id.archieved_notifi);
        archieved_notifi.setVisibility(View.GONE);
        hl_2 = findViewById(R.id.hl_2);
        hl_2.setVisibility(View.GONE);

        patName_txt = findViewById(R.id.textView_name_value);
        profile_image = findViewById(R.id.profile_image);
        gender_age_txt = findViewById(R.id.textView_gender_value);
        openmrsID_txt = findViewById(R.id.textView_id_value);
        mCHWname = findViewById(R.id.chw_details);
        visitID_txt = findViewById(R.id.textView_visit_value);

        drname = findViewById(R.id.drname);
        dr_age_gender = findViewById(R.id.dr_age_gender);
        qualification = findViewById(R.id.qualification);
        dr_speciality = findViewById(R.id.dr_speciality);

        diagnosis_txt = findViewById(R.id.diagnosis_txt);
        advice_txt = findViewById(R.id.advice_txt);
        test_txt = findViewById(R.id.test_txt);
        referred_speciality_txt = findViewById(R.id.referred_speciality_txt);
        no_followup_txt = findViewById(R.id.no_followup_txt);
        followup_date_txt = findViewById(R.id.followup_date_txt);
        followup_subtext = findViewById(R.id.followup_info);
        followup_date_block = findViewById(R.id.followup_date_block);

        no_btn = findViewById(R.id.no_btn);
        yes_btn = findViewById(R.id.yes_btn);
        downloadBtn = findViewById(R.id.downloadBtn);
        btn_vs_print = findViewById(R.id.btn_vs_print);
        btn_vs_share = findViewById(R.id.btn_vs_share);   // share

        btn_up_header = findViewById(R.id.btn_up_header);
        presc_profile_header = findViewById(R.id.presc_profile_header);
        dr_details_header_relative = findViewById(R.id.dr_details_header_relative);
        diagnosis_header_relative = findViewById(R.id.diagnosis_header_relative);
        medication_header_relative = findViewById(R.id.medication_header_relative);
        advice_header_relative = findViewById(R.id.advice_header_relative);
        test_header_relative = findViewById(R.id.test_header_relative);
        referred_header_relative = findViewById(R.id.referred_header_relative);
        followup_header_relative = findViewById(R.id.followup_header_relative);

        btnup_drdetails_header = findViewById(R.id.btnup_drdetails_header);
        btnup_diagnosis_header = findViewById(R.id.btnup_diagnosis_header);
        btnup_medication_header = findViewById(R.id.btnup_medication_header);
        btnup_test_header = findViewById(R.id.btnup_test_header);
        btnup_speciality_header = findViewById(R.id.btnup_speciality_header);
        btnup_followup_header = findViewById(R.id.btnup_followup_header);

        vs_header_expandview = findViewById(R.id.vs_header_expandview);
        vs_drdetails_header_expandview = findViewById(R.id.vs_drdetails_header_expandview);
        vs_diagnosis_header_expandview = findViewById(R.id.vs_diagnosis_header_expandview);
        vs_medication_header_expandview = findViewById(R.id.vs_medication_header_expandview);
        vs_adviceheader_expandview = findViewById(R.id.vs_adviceheader_expandview);
        vs_testheader_expandview = findViewById(R.id.vs_testheader_expandview);
        vs_speciality_header_expandview = findViewById(R.id.vs_speciality_header_expandview);
        vs_followup_header_expandview = findViewById(R.id.vs_followup_header_expandview);

        backArrow = findViewById(R.id.backArrow);
        refresh = findViewById(R.id.refresh);

        backArrow.setOnClickListener(v -> {
            finish();
        });

/*
        refresh.setOnClickListener(v -> {
            syncNow(PrescriptionActivity.this, refresh, syncAnimator);
        });
*/
    }

    private void fetchIntent() {
        Intent intent = this.getIntent(); // The intent was passed to the activity
        sessionManager = new SessionManager(this);
        if (intent != null) {
            patientName = intent.getStringExtra("patientname");
            patientUuid = intent.getStringExtra("patientUuid");
            gender = intent.getStringExtra("gender");
            age = intent.getStringExtra("age");
//            CustomLog.d("TAG", "getAge_FollowUp: s : " + age);
            openmrsID = intent.getStringExtra("openmrsID");
            visitID = intent.getStringExtra("visit_ID");
            vitalsUUID = intent.getStringExtra("encounterUuidVitals");
            adultInitialUUID = intent.getStringExtra("encounterUuidAdultIntial");
            visit_startDate = intent.getStringExtra("visit_startDate");
            patient_photo_path = intent.getStringExtra("patient_photo");
            intentTag = intent.getStringExtra("tag");
            try {
                hasPrescription = new EncounterDAO().isPrescriptionReceived(visitID);
//                CustomLog.d(PrescriptionActivity.class.getSimpleName(),"has prescription main::%s", hasPrescription);
            } catch (DAOException e) {
                CustomLog.e(TAG, e.getMessage());
                throw new RuntimeException(e);
            }
            queryData(String.valueOf(patientUuid));
        }


    }

    @SuppressLint("LogNotTimber")
    private void setDataToView() {
        // settind data - start
        downloadPrescriptionDefault();
        registerDownloadPrescription();

        // json based presc header - start
        jsonBasedPrescTitle();
        // json based presc header - end

        //get from encountertbl from the encounter
        visitnoteencounteruuid = getStartVisitNoteEncounterByVisitUUID(visitID);
        // settind data - end

        // Patient Photo
        profile_image = findViewById(R.id.profile_image);
        if (patient_photo_path != null) {
            RequestBuilder<Drawable> requestBuilder = Glide.with(this)
                    .asDrawable().sizeMultiplier(0.3f);
            Glide.with(this).load(patient_photo_path).thumbnail(requestBuilder).centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(profile_image);
        } else {
            profile_image.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.avatar1));
        }
        // end

        patName_txt.setText(patientName);
        gender_age_txt.setText(gender + " " + age);
        openmrsID_txt.setText(openmrsID);
        mCHWname.setText(sessionManager.getChwname()); //session manager provider

        String hideVisitUUID = visitID;
        hideVisitUUID = hideVisitUUID.substring(hideVisitUUID.length() - 4, hideVisitUUID.length());
        visitID_txt.setText("XXXX" + hideVisitUUID);

        // dr details - start
        String drDetails = fetchDrDetailsFromLocalDb(visitID);
        parseDoctorDetails(drDetails);
        // dr details - end

        // download btn - start
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkConnection.isOnline(getApplication())) {
                    syncAnimator = ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 359f).setDuration(1200);
                    syncAnimator.setInterpolator(new LinearInterpolator());
                    syncAnimator.setRepeatCount(Animation.INFINITE);
                    syncAnimator.start();
                    Toast.makeText(PrescriptionActivity.this, getResources().getString(R.string.downloading), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(PrescriptionActivity.this, getResources().getString(R.string.prescription_not_downloaded_check_internet), Toast.LENGTH_LONG).show();
                }

                SyncUtils syncUtils = new SyncUtils();
                syncUtils.syncForeground("downloadPrescription");
                uploaded = true;

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        downloadPrescription();
                    }
                }, 5000);
            }
        });
        // download btn - end

        // Follow up - start
       /* if (followUpDate.equalsIgnoreCase("")) {
            no_followup_txt.setVisibility(View.VISIBLE);
            followup_date_block.setVisibility(View.GONE);
        } else {
            no_followup_txt.setVisibility(View.GONE);
            followup_date_block.setVisibility(View.VISIBLE);
        }*/
        // Follow up - end

        // Bottom Buttons - start
        btn_vs_print.setOnClickListener(v -> {
            if (mFeatureActiveStatus.getPrintUsingThermalPrinter()) {
                // PrintViewPrescription prescription = new PrintViewPrescription();
                PrintViewPrescriptionDataModel prescriptionDataModel = new PrintViewPrescriptionDataModel();
                prescriptionDataModel.setVisitUuid(visitID);
                prescriptionDataModel.setPatHistory(patHistory);
                prescriptionDataModel.setFamHistory(famHistory);
                prescriptionDataModel.setHeight(height);
                prescriptionDataModel.setWeight(weight);
                prescriptionDataModel.setBpSys(bpSys);
                prescriptionDataModel.setBpDias(bpDias);
                prescriptionDataModel.setPulse(pulse);
                prescriptionDataModel.setHasLicense(hasLicense);
                prescriptionDataModel.setTemperature(temperature);
                prescriptionDataModel.setResp(resp);
                prescriptionDataModel.setSpO2(spO2);
                prescriptionDataModel.setComplaint(complaint);
                prescriptionDataModel.setRxReturned(rxReturned);
                prescriptionDataModel.setTestsReturned(testsReturned);
                prescriptionDataModel.setDiagnosisReturned(diagnosisReturned);
                prescriptionDataModel.setFollowUpDate(followUpDate);
                prescriptionDataModel.setDoctorName(doctorName);
                prescriptionDataModel.setPrescription1(prescription1);
                prescriptionDataModel.setPrescription2(prescription2);
                prescriptionDataModel.setBP(mBP);
                prescriptionDataModel.setMedicalAdvice(medicalAdvice);
                prescriptionDataModel.setBloodGlucoseRandom(bloodGlucoseRandom);
                prescriptionDataModel.setBloodGlucoseFasting(bloodGlucoseFasting);
                prescriptionDataModel.setBloodGlucosePostPrandial(bloodGlucosePostPrandial);
                prescriptionDataModel.setHemoglobin(hemoglobin);
                prescriptionDataModel.setUricAcid(uricAcid);
                prescriptionDataModel.setCholesterol(cholesterol);

                Log.d(TAG, "setDataToView: bloodGlucoseRandom  : " + new Gson().toJson(bloodGlucoseRandom));
                Log.d(TAG, "setDataToView: bloodGlucoseFasting  : " + new Gson().toJson(bloodGlucoseFasting));
                Log.d(TAG, "setDataToView: bloodGlucosePostPrandial  : " + new Gson().toJson(bloodGlucosePostPrandial));
                Log.d(TAG, "setDataToView: hemoglobin  : " + new Gson().toJson(hemoglobin));
                Log.d(TAG, "setDataToView: uricAcid  : " + new Gson().toJson(uricAcid));
                Log.d(TAG, "setDataToView: cholesterol  : " + new Gson().toJson(cholesterol));

                PrintViewPrescription printViewPrescription = new PrintViewPrescription(
                        PrescriptionActivity.this, details, patient, prescriptionDataModel, PrescriptionActivity.this);
                try {
                    printViewPrescription.textPrint();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    doWebViewPrint_Button();
                } catch (ParseException e) {
                    e.printStackTrace();
                    CustomLog.e(TAG, e.getMessage());
                }
            }
        });

        btn_vs_share.setOnClickListener(v -> {
            sharePresc();
        });
        // Bottom Buttons - end

        // follow up - yes - start
        yes_btn.setOnClickListener(v -> {
            Drawable drawable = ContextCompat.getDrawable(PrescriptionActivity.this, R.drawable.dialog_visit_sent_success_icon);
            followupScheduledSuccess(PrescriptionActivity.this, drawable, getResources().getString(R.string.follow_up_scheduled), getResources().getString(R.string.follow_up_scheduled_successfully), getResources().getString(R.string.okay));
        });
        // follow up - yes - end

        // presc pdf downlaod - start
        downloadBtn.setOnClickListener(v -> {
            checkPerm();
        });

        filter.setOnClickListener(v -> {
            // filter options
            if (filter_framelayout.getVisibility() == View.VISIBLE)
                filter_framelayout.setVisibility(View.GONE);
            else filter_framelayout.setVisibility(View.VISIBLE);
        });

        reminder.setOnClickListener(v -> {
            // filter options
            Intent intent = new Intent(PrescriptionActivity.this, HomeScreenActivity_New.class);
            startActivity(intent);
            if (filter_framelayout.getVisibility() == View.VISIBLE)
                filter_framelayout.setVisibility(View.GONE);
            else filter_framelayout.setVisibility(View.VISIBLE);
        });

        incomplete_act.setOnClickListener(v -> {
            // filter options
//            Intent intent = new Intent(PrescriptionActivity.this, EndVisitActivity.class);
//            startActivity(intent);
            if (filter_framelayout.getVisibility() == View.VISIBLE)
                filter_framelayout.setVisibility(View.GONE);
            else filter_framelayout.setVisibility(View.VISIBLE);
            showEndVisitConfirmationDialog();
        });

        archieved_notifi.setOnClickListener(v -> {
            // filter options
            if (filter_framelayout.getVisibility() == View.VISIBLE)
                filter_framelayout.setVisibility(View.GONE);
            else filter_framelayout.setVisibility(View.VISIBLE);
        });
    }

    private void showEndVisitConfirmationDialog() {
        if (hasPrescription) {
            DialogUtils dialogUtils = new DialogUtils();
            dialogUtils.showCommonDialog(this, R.drawable.dialog_close_visit_icon, getResources().getString(R.string.confirm_end_visit_reason), getResources().getString(R.string.confirm_end_visit_reason_message), false, getResources().getString(R.string.confirm), getResources().getString(R.string.cancel), action -> {
                if (action == DialogUtils.CustomDialogListener.POSITIVE_CLICK) {
                    checkIfAppointmentExistsForVisit(visitID);
                }
            });
        } else {
            triggerEndVisit();
        }
    }

    private void checkIfAppointmentExistsForVisit(String visitUUID) {
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

    private void cancelAppointment(String visitUUID) {
        AppointmentInfo appointmentInfo = new AppointmentDAO().getAppointmentByVisitId(visitUUID);

        int appointmentID = appointmentInfo.getId();
        String reason = "Visit was ended";
        String providerID = sessionManager.getProviderID();
        String baseurl = BuildConfig.SERVER_URL + ":3004";

        new AppointmentUtils().cancelAppointmentRequestOnVisitEnd(visitUUID, appointmentID, reason, providerID, baseurl);
    }

    private void triggerEndVisit() {

//        String vitalsUUID = fetchEncounterUuidForEncounterVitals(visitID);
//        String adultInitialUUID = fetchEncounterUuidForEncounterAdultInitials(visitID);

        endVisit(this, visitID, patient.getUuid(), followUpDate, vitalsUUID, adultInitialUUID, "state", patient.getFirst_name() + " " + patient.getLast_name().substring(0, 1), PrescriptionActivity.class.getSimpleName());
    }

    // permission code - start
    private void checkPerm() {
        if (checkAndRequestPermissions()) {
            try {
                doWebViewPrint_downloadBtn();
            } catch (ParseException e) {
                e.printStackTrace();
                CustomLog.e(TAG, e.getMessage());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GROUP_PERMISSION_REQUEST) {
            boolean allGranted = grantResults.length != 0;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                checkPerm();
            } else {
                showPermissionDeniedAlert(permissions);
            }
        }
    }

    private void showPermissionDeniedAlert(String[] permissions) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(this);
        alertdialogBuilder.setMessage(R.string.reject_permission_results);
        alertdialogBuilder.setPositiveButton(R.string.retry_again, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                checkPerm();
            }
        });
        alertdialogBuilder.setNegativeButton(R.string.ok_close_now, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.show();

        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);

        positiveButton.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));

        negativeButton.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
    }

    private boolean checkAndRequestPermissions() {
        int writeExternalStoragePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            requestPermissions(listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), GROUP_PERMISSION_REQUEST);
            return false;
        }
        return true;
    }
    // permission code - end

    // presc create - start
    private void createWebPrintJob_downloadBtn(WebView webView, int contentHeight) {

        PrintManager printManager = (PrintManager) this.getSystemService(Context.PRINT_SERVICE);
        String docName = this.getString(R.string.app_name) + " Prescription";

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(docName);
        CustomLog.d("webview content height", "webview content height: " + contentHeight);

        if (contentHeight > 2683 && contentHeight <= 3000) {
            //medium size prescription...
            PrintAttributes.Builder pBuilder = new PrintAttributes.Builder();
            pBuilder.setMediaSize(PrintAttributes.MediaSize.ISO_B4);
            pBuilder.setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600));
            pBuilder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);
            // Create a print job with name and adapter instance
            String jobName = getString(R.string.app_name) + " " + getResources().getString(R.string._visit_summary);

            PdfPrint pdfPrint = new PdfPrint(pBuilder.build());

            /*String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Intelehealth_PDF/";
            String fileName = patientName + "_" + showVisitID() + ".pdf";*/
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/Intelehealth_PDF";
            String fileName = patientName.replace(" ", "_") + "_" + showVisitID() + ".pdf";
            File dir = new File(path);
            if (!dir.exists()) dir.mkdirs();

            File directory = new File(dir, fileName);

            //To display the preview window to user...
//            PrintJob printJob = printManager.print(jobName, printAdapter,
//                    pBuilder.build());

            //TODO: write different functions for <= Lollipop versions..
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //to write to a pdf file...
                pdfPrint.print(webView.createPrintDocumentAdapter(jobName), dir, fileName, new PdfPrint.CallbackPrint() {
                    @Override
                    public void success(String path) {
                        Toast.makeText(PrescriptionActivity.this, getResources().getString(R.string.downloaded_to) + " " + path, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure() {
                        Toast.makeText(PrescriptionActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }

                });
            } else {
                //to write to a pdf file...
                pdfPrint.print(printAdapter, dir, fileName, new PdfPrint.CallbackPrint() {
                    @Override
                    public void success(String path) {
                        Toast.makeText(PrescriptionActivity.this, getResources().getString(R.string.downloaded_to) + " " + path, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure() {
                        Toast.makeText(PrescriptionActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }

                });
            }

//            PrintJob printJob = printManager.print(jobName, printAdapter,
//                    pBuilder.build());
        } else if (contentHeight == 0) {
            //in case of webview bug of 0 contents...
            PrintAttributes.Builder pBuilder = new PrintAttributes.Builder();
            pBuilder.setMediaSize(PrintAttributes.MediaSize.JIS_B4);
            pBuilder.setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600));
            pBuilder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);
            // Create a print job with name and adapter instance
            String jobName = getString(R.string.app_name) + " Visit Summary";

            PdfPrint pdfPrint = new PdfPrint(pBuilder.build());

            /*String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Intelehealth_PDF/";
            String fileName = patientName + "_" + showVisitID() + ".pdf";*/
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/Intelehealth_PDF";
            String fileName = patientName.replace(" ", "_") + "_" + showVisitID() + ".pdf";
            File dir = new File(path);
            if (!dir.exists()) dir.mkdirs();

            File directory = new File(dir, fileName);

            //To display the preview window to user...
//            PrintJob printJob = printManager.print(jobName, printAdapter,
//                    pBuilder.build());

            //TODO: write different functions for <= Lollipop versions..
            //to write to a pdf file...
            pdfPrint.print(webView.createPrintDocumentAdapter(jobName), dir, fileName, new PdfPrint.CallbackPrint() {
                @Override
                public void success(String path) {
                    Toast.makeText(PrescriptionActivity.this, getResources().getString(R.string.downloaded_to) + " " + path, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure() {
                    Toast.makeText(PrescriptionActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }

            });

//            PrintJob printJob = printManager.print(jobName, printAdapter,
//                    pBuilder.build());
        } else if (contentHeight > 3000) {
            //large size prescription...
            PrintAttributes.Builder pBuilder = new PrintAttributes.Builder();
            pBuilder.setMediaSize(PrintAttributes.MediaSize.JIS_B4);
            pBuilder.setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600));
            pBuilder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);
            // Create a print job with name and adapter instance
            String jobName = getString(R.string.app_name) + " " + getResources().getString(R.string._visit_summary);

            PdfPrint pdfPrint = new PdfPrint(pBuilder.build());

            /*String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Intelehealth_PDF/";
            String fileName = patientName + "_" + showVisitID() + ".pdf";*/
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/Intelehealth_PDF";
            String fileName = patientName.replace(" ", "_") + "_" + showVisitID() + ".pdf";
            File dir = new File(path);
            if (!dir.exists()) dir.mkdirs();

            File directory = new File(dir, fileName);

            //To display the preview window to user...
//            PrintJob printJob = printManager.print(jobName, printAdapter,
//                    pBuilder.build());

            //TODO: write different functions for <= Lollipop versions..
            //to write to a pdf file...
            pdfPrint.print(webView.createPrintDocumentAdapter(jobName), dir, fileName, new PdfPrint.CallbackPrint() {
                @Override
                public void success(String path) {
                    Toast.makeText(PrescriptionActivity.this, getResources().getString(R.string.downloaded_to) + " " + path, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure() {
                    Toast.makeText(PrescriptionActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }

            });

//            PrintJob printJob = printManager.print(jobName, printAdapter,
//                    pBuilder.build());
        } else {
            //small size prescription...
            // Create a print job with name and adapter instance
            String jobName = getString(R.string.app_name) + " " + getResources().getString(R.string._visit_summary);

            CustomLog.d("PrintPDF", "PrintPDF");
            PrintAttributes.Builder pBuilder = new PrintAttributes.Builder();
            pBuilder.setMediaSize(PrintAttributes.MediaSize.NA_LETTER);
            pBuilder.setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600));
            pBuilder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);
            PdfPrint pdfPrint = new PdfPrint(pBuilder.build());

            /*String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Intelehealth_PDF/";
            String fileName = patientName + "_" + showVisitID() + ".pdf";*/
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/Intelehealth_PDF";
            String fileName = patientName.replace(" ", "_") + "_" + showVisitID() + ".pdf";
            File dir = new File(path);
            if (!dir.exists()) dir.mkdirs();

            File directory = new File(dir, fileName);

            //To display the preview window to user...
//            PrintJob printJob = printManager.print(jobName, printAdapter,
//                    pBuilder.build());

            //end...

            //TODO: write different functions for <= Lollipop versions..
            //to write to a pdf file...
            pdfPrint.print(webView.createPrintDocumentAdapter(jobName), dir, fileName, new PdfPrint.CallbackPrint() {
                @Override
                public void success(String path) {
                    Toast.makeText(PrescriptionActivity.this, getResources().getString(R.string.downloaded_to) + " " + path, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure() {
                    Toast.makeText(PrescriptionActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }

            });
            //            PrintJob printJob = printManager.print(jobName, printAdapter,
//                    new PrintAttributes.Builder().build());

        }
    }

    private String showVisitID() {
        String hideVisitUUID = "";
        if (visitID != null && !visitID.isEmpty()) {
            hideVisitUUID = visitID;
            hideVisitUUID = hideVisitUUID.substring(hideVisitUUID.length() - 4, hideVisitUUID.length());
            //  visitView.setText("XXXX" + hideVisitUUID);
            hideVisitUUID = "XXXX" + hideVisitUUID;
        }
        return hideVisitUUID;
    }


    private void doWebViewPrint_downloadBtn() throws ParseException {
        // Create a WebView object specifically for printing
        WebView webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                CustomLog.i("Patient WebView", "page finished loading " + url);
                int webview_heightContent = view.getContentHeight();
                CustomLog.d("variable i", "variable i: " + webview_heightContent);
                createWebPrintJob_downloadBtn(view, webview_heightContent);
                mWebView = null;
            }
        });

        String mPatientName = patient.getFirst_name() + " " + ((!TextUtils.isEmpty(patient.getMiddle_name())) ? patient.getMiddle_name() : "") + " " + patient.getLast_name();
        String mPatientOpenMRSID = patient.getOpenmrs_id();
        String mPatientDob = patient.getDate_of_birth();
        String mAddress = ((!TextUtils.isEmpty(patient.getAddress1())) ? patient.getAddress1() + "\n" : "") + ((!TextUtils.isEmpty(patient.getAddress2())) ? patient.getAddress2() : "");
        String mCityState = patient.getCity_village();
        String mPhone = (!TextUtils.isEmpty(patient.getPhone_number())) ? patient.getPhone_number() : "";
        String mState = patient.getState_province();
        String mCountry = patient.getCountry();

        String mSdw = (!TextUtils.isEmpty(patient.getSdw())) ? patient.getSdw() : "";
        String mOccupation = patient.getOccupation();
        String mGender = patient.getGender();

        Calendar c = Calendar.getInstance();
        System.out.println(getString(R.string.current_time) + c.getTime());

        String[] columnsToReturn = {"startdate"};
        String visitIDorderBy = "startdate";
        String visitIDSelection = "uuid = ?";
        String[] visitIDArgs = {visitID};
        db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        final Cursor visitIDCursor = db.query("tbl_visit", columnsToReturn, visitIDSelection, visitIDArgs, null, null, visitIDorderBy);
        visitIDCursor.moveToLast();
        String startDateTime = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("startdate"));
        visitIDCursor.close();
        String mDate = DateAndTimeUtils.SimpleDatetoLongDate(startDateTime);

        String mPatHist = patHistory.getValue();
        if (mPatHist == null) {
            mPatHist = "";
        }
        String mFamHist = famHistory.getValue();
        if (mFamHist == null) {
            mFamHist = "";
        }

        mHeight = height.getValue();
        mWeight = weight.getValue();
        mBP = bpSys.getValue() + "/" + bpDias.getValue();
        mPulse = pulse.getValue();
        mBloodGlucoseRandom = bloodGlucoseRandom.getValue();
        mBloodGlucoseFasting = bloodGlucoseFasting.getValue();
        mBloodGlucosePostPrandial = bloodGlucosePostPrandial.getValue();
        mHemoglobin = hemoglobin.getValue();
        mCholesterol = cholesterol.getValue();
        mUricAcid = uricAcid.getValue();
        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(Objects.requireNonNullElse(FileUtils.readFileRoot(CONFIG_FILE_NAME, this), String.valueOf(FileUtils.encodeJSON(this, CONFIG_FILE_NAME)))); //Load the config file
            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(this, mFileName)));
            }//Load the config file

            if (obj.getBoolean("mTemperature")) {
                if (obj.getBoolean("mCelsius")) {

                    mTemp = getResources().getString(R.string.prescription_temp_c) + " " + (!TextUtils.isEmpty(temperature.getValue()) ? temperature.getValue().toString() : "");

                } else if (obj.getBoolean("mFahrenheit")) {

//                    mTemp = "Temperature(F): " + temperature.getValue();
                    mTemp = getResources().getString(R.string.prescription_temp_f) + " " + (!TextUtils.isEmpty(temperature.getValue()) ? convertCtoF(temperature.getValue()) : "");
                }
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            CustomLog.e(TAG, e.getMessage());
        }
        mresp = resp.getValue();
        mSPO2 = getResources().getString(R.string.spo2) + ": " + (!TextUtils.isEmpty(spO2.getValue()) ? spO2.getValue() : "");
        String mComplaint = complaint.getValue();

        //Show only the headers of the complaints in the printed prescription
        String[] complaints = org.apache.commons.lang3.StringUtils.split(mComplaint, Node.bullet_arrow);
        mComplaint = "";
        String colon = ":";
        String mComplaint_new = "";
        if (complaints != null) {
            for (String comp : complaints) {
                if (!comp.trim().isEmpty()) {
                    mComplaint = mComplaint + Node.big_bullet + comp.substring(0, comp.indexOf(colon)) + "<br/>";

                }
            }
            if (!mComplaint.isEmpty()) {
                mComplaint = mComplaint.substring(0, mComplaint.length() - 2);
                mComplaint = mComplaint.replaceAll("<b>", "");
                mComplaint = mComplaint.replaceAll("</b>", "");
            }
        }

        if (mComplaint.contains(Node.ASSOCIATE_SYMPTOMS)) {
            String[] cc = org.apache.commons.lang3.StringUtils.split(mComplaint, Node.bullet_arrow);
            for (String compla : cc) {
                mComplaint = mComplaint.substring(0, compla.indexOf(Node.ASSOCIATE_SYMPTOMS) - 3);
                //   mComplaint = "Test Complaint";
            }
        } else {

        }

        if (mComplaint.contains("जुड़े लक्षण")) {
            String[] cc = org.apache.commons.lang3.StringUtils.split(mComplaint, Node.bullet_arrow);
            for (String compla : cc) {
                mComplaint = mComplaint.substring(0, compla.indexOf("जुड़े लक्षण") - 3);
            }
        } else {

        }


        if (mPatientOpenMRSID == null) {
            mPatientOpenMRSID = getString(R.string.patient_not_registered);
        }

        String para_open = "<p style=\"font-size:11pt; margin: 0px; padding: 0px;\">";
        String para_close = "</p>";

        Calendar today = Calendar.getInstance();
        Calendar dob = Calendar.getInstance();


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = sdf.parse(mPatientDob);
        dob.setTime(date);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        String rx_web = stringToWeb(rxReturned);

        String tests_web = stringToWeb(testsReturned.trim().replace("\n\n", "\n").replace(Node.bullet, ""));

        String referredSpeciality_web = stringToWeb(referredSpeciality);

        String advice_web = stringToWeb(adviceReturned);
        //  String advice_web = "";
//        if(medicalAdviceTextView.getText().toString().indexOf("Start") != -1 ||
//                medicalAdviceTextView.getText().toString().lastIndexOf(("User") + 6) != -1) {
   /*     String advice_doctor__ = medication_txt.getText().toString()
                .replace("Start Audio Call with Doctor", "Start Audio Call with Doctor_")
                .replace("Start WhatsApp Call with Doctor", "Start WhatsApp Call with Doctor_");

        if (advice_doctor__.indexOf("Start") != -1 ||
                advice_doctor__.lastIndexOf(("Doctor_") + 9) != -1) {

            String advice_split = new StringBuilder(advice_doctor__)
                    .delete(advice_doctor__.indexOf("Start"),
                            advice_doctor__.lastIndexOf("Doctor_") + 9).toString();

            advice_web = stringToWeb(advice_split.replace("\n\n", "\n")); //showing advice here...
            CustomLog.d("Hyperlink", "hyper_print: " + advice_web); //gets called when clicked on button of print button
        } else {
            advice_web = stringToWeb(advice_doctor__.replace("\n\n", "\n")); //showing advice here...
            CustomLog.d("Hyperlink", "hyper_print: " + advice_web); //gets called when clicked on button of print button
        }*/

        String diagnosis_web = stringToWeb(diagnosisReturned);

//        String comments_web = stringToWeb(additionalReturned);

        String followUpDateStr = "";
        if (followUpDate != null && followUpDate.contains(",")) {
            String[] spiltFollowDate = followUpDate.split(",");
            if (spiltFollowDate[0] != null && spiltFollowDate[0].contains("-")) {
                String remainingStr = "";
                for (int i = 1; i <= spiltFollowDate.length - 1; i++) {
                    remainingStr = ((!TextUtils.isEmpty(remainingStr)) ? remainingStr + ", " : "") + spiltFollowDate[i];
                }
                followUpDateStr = parse_DateToddMMyyyy(spiltFollowDate[0]) + ", " + remainingStr;
            } else {
                followUpDateStr = followUpDate;
            }
        } else {
            followUpDateStr = followUpDate;
        }

        String followUp_web = stringToWeb(followUpDateStr);

        String doctor_web = stringToWeb(doctorName);

        String heading = prescription1;
        String heading2 = prescription2;
        String heading3 = "<br/>";

        //  String bp = mBP;
        String bp = "";
        if (bp.equals("/") || bp.equals("null/null")) bp = "";

        String address = mAddress + " " + mCityState + ((!TextUtils.isEmpty(mPhone)) ? ", " + mPhone : "");

        String fam_hist = mFamHist;
        String pat_hist = mPatHist;

        if (fam_hist.trim().isEmpty()) {
            fam_hist = getString(R.string.no_history_family_found);
        } else {
            fam_hist = fam_hist.replaceAll(Node.bullet, Node.big_bullet);
        }

        if (pat_hist.trim().isEmpty()) {
            pat_hist = getString(R.string.no_history_patient_illness_found);
        }

        // Generate an HTML document on the fly:
        String fontFamilyFile = "";
        if (details != null && details.getFontOfSign() != null) {
            if (details.getFontOfSign().toLowerCase().equalsIgnoreCase("youthness")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/Youthness.ttf');";
            } else if (details.getFontOfSign().toLowerCase().equalsIgnoreCase("asem")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/Asem.otf');";
            } else if (details.getFontOfSign().toLowerCase().equalsIgnoreCase("arty")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/Arty.otf');";
            } else if (details.getFontOfSign().toLowerCase().equalsIgnoreCase("almondita")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/almondita.ttf');";
            }
        }

        String font_face = "<style>" + "                @font-face {" + "                    font-family: \"MyFont\";" + fontFamilyFile + "                }" + "            </style>";

        String doctorSign = "";
        String doctrRegistartionNum = "";
        // String docDigitallySign = "";
        String doctorDetailStr = "";
        if (details != null) {
            //  docDigitallySign = "Digitally Signed By";
            doctorSign = details.getTextOfSign();

            sign_url = BuildConfig.SERVER_URL + "/ds/" + details.getUuid() + "_sign.png";
            CustomLog.v("signurl", "signurl: " + sign_url);

            doctrRegistartionNum = !TextUtils.isEmpty(details.getRegistrationNumber()) ? getString(R.string.dr_registration_no) + details.getRegistrationNumber() : "";

            doctorDetailStr = "<div style=\"text-align:right;margin-right:0px;margin-top:0px;\">" + "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + details.getName() + "</span><br>" + "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + "  " + (details.getQualification() == null || details.getQualification().equalsIgnoreCase("null") ? "" : details.getQualification() + ", ") + details.getSpecialization() + "</span><br>" +
                    //  "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(details.getPhoneNumber()) ?
                    //  getString(R.string.dr_phone_number) + details.getPhoneNumber() : "") + "</span><br>" +
                    "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(details.getEmailId()) ? getString(R.string.dr_email) + details.getEmailId() : "") + "</span><br>" + "</div>";
        }

        if (isRespiratory) {
            String htmlDocument = String.format(font_face + "<b><p id=\"heading_1\" style=\"font-size:16pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<p id=\"heading_2\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<p id=\"heading_3\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<hr style=\"font-size:12pt;\">" + "<br/>" +
                    /* doctorDetailStr +*/
                    "<p id=\"patient_name\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">%s</p></b>" + "<p id=\"patient_details\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Age: %s | Gender: %s  </p>" + "<p id=\"address_and_contact\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Address and Contact: %s</p>" + "<p id=\"visit_details\" style=\"font-size:12pt; margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient Id: %s | Date of visit: %s </p><br>" + "<b><p id=\"vitals_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px;; padding: 0px;\">Vitals</p></b>" + "<p id=\"vitals\" style=\"font-size:12pt;margin:0px; padding: 0px;\">Height(cm): %s | Weight(kg): %s | BMI: %s | Blood Pressure: %s | Pulse(bpm): %s | %s | Respiratory Rate: %s |  %s </p><br>" + "<b><p id=\"patient_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient History</p></b>" + "<p id=\"patient_history\" style=\"font-size:11pt;margin:0px; padding: 0px;\"> %s</p><br>" + "<b><p id=\"family_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Family History</p></b>" + "<p id=\"family_history\" style=\"font-size:11pt;margin: 0px; padding: 0px;\"> %s</p><br>" + "<b><p id=\"complaints_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Presenting complaint(s)</p></b>" + para_open + "%s" + para_close + "<br><br>" + "<u><b><p id=\"diagnosis_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Diagnosis</p></b></u>" + "%s<br>" + "<u><b><p id=\"rx_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Medication(s) plan</p></b></u>" + "%s<br>" + "<u><b><p id=\"tests_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Recommended Investigation(s)</p></b></u>" + "%s<br>" + "<u><b><p id=\"advice_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">General Advice</p></b></u>" + "%s<br>" + "<u><b><p id=\"follow_up_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Follow Up Date</p></b></u>" + "%s<br>" + "<div style=\"text-align:right;margin-right:50px;margin-top:0px;\">" +
                    //  "<span style=\"font-size:80pt;font-family: MyFont;padding: 0px;\">" + doctorSign + "</span>" +
                    "<img src=" + sign_url + " alt=\"Dr Signature\">" + // doctor signature...
                    doctorDetailStr + "<p style=\"font-size:12pt; margin-top:-0px; padding: 0px;\">" + doctrRegistartionNum + "</p>" + "</div>", heading, heading2, heading3, mPatientName, age, mGender, /*mSdw*/ address, mPatientOpenMRSID, mDate, (!TextUtils.isEmpty(mHeight)) ? mHeight : "", (!TextUtils.isEmpty(mWeight)) ? mWeight : "", (!TextUtils.isEmpty(mBMI)) ? mBMI : "", (!TextUtils.isEmpty(bp)) ? bp : "", (!TextUtils.isEmpty(mPulse)) ? mPulse : "", (!TextUtils.isEmpty(mTemp)) ? mTemp : "", (!TextUtils.isEmpty(mresp)) ? mresp : "", (!TextUtils.isEmpty(mSPO2)) ? mSPO2 : "", pat_hist, fam_hist, mComplaint, diagnosis_web, rx_web, tests_web, advice_web, followUp_web, doctor_web);
            webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);
        } else {
            String htmlDocument = String.format(font_face + "<b><p id=\"heading_1\" style=\"font-size:16pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<p id=\"heading_2\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<p id=\"heading_3\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<hr style=\"font-size:12pt;\">" + "<br/>" + "<p id=\"patient_name\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">%s</p></b>" + "<p id=\"patient_details\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Age: %s | Gender: %s </p>" + "<p id=\"address_and_contact\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Address and Contact: %s</p>" + "<p id=\"visit_details\" style=\"font-size:12pt; margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient Id: %s | Date of visit: %s </p><br>" + "<b><p id=\"vitals_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px;; padding: 0px;\">Vitals</p></b>" + "<p id=\"vitals\" style=\"font-size:12pt;margin:0px; padding: 0px;\">Height(cm): %s | Weight(kg): %s | BMI: %s | Blood Pressure: %s | Pulse(bpm): %s | %s | %s </p><br>" +
                                    /*"<b><p id=\"patient_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient History</p></b>" +
                                    "<p id=\"patient_history\" style=\"font-size:11pt;margin:0px; padding: 0px;\"> %s</p><br>" +
                                    "<b><p id=\"family_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Family History</p></b>" +
                                    "<p id=\"family_history\" style=\"font-size:11pt;margin: 0px; padding: 0px;\"> %s</p><br>" +*/
                            "<b><p id=\"complaints_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Presenting complaint(s)</p></b>" + para_open + "%s" + para_close + "<br><br>" + "<u><b><p id=\"diagnosis_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Diagnosis</p></b></u>" + "%s<br>" + "<u><b><p id=\"rx_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Medication(s) plan</p></b></u>" + "%s<br>" + "<u><b><p id=\"tests_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Recommended Investigation(s)</p></b></u>" + "%s<br>" + "<u><b><p id=\"advice_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">General Advice</p></b></u>" + "%s<br>" + "<u><b><p id=\"follow_up_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Follow Up Date</p></b></u>" + "%s<br>" + "<div style=\"text-align:right;margin-right:50px;margin-top:0px;\">" + "<span style=\"font-size:80pt;font-family: MyFont;padding: 0px;\">" + doctorSign + "</span><br>" + doctorDetailStr + "<span style=\"font-size:12pt; margin-top:5px; padding: 0px;\">" + doctrRegistartionNum + "</span>" + "</div>", heading, heading2, heading3, mPatientName, age, mGender, /*mSdw*/ address, mPatientOpenMRSID, mDate,
                    /*(!TextUtils.isEmpty(mHeight)) ? mHeight :*/ "",
                    /*(!TextUtils.isEmpty(mWeight)) ? mWeight :*/ "",
                    /*(!TextUtils.isEmpty(mBMI)) ? mBMI :*/ "", (!TextUtils.isEmpty(bp)) ? bp : "",
                    /*(!TextUtils.isEmpty(mPulse)) ? mPulse :*/ "",
                    /*(!TextUtils.isEmpty(mTemp)) ? mTemp :*/ "",
                    /*(!TextUtils.isEmpty(mSPO2)) ? mSPO2 :*/ "",
                    /*pat_hist, fam_hist,*/
                    /*mComplaint*/ "", diagnosis_web, rx_web, tests_web, advice_web, followUp_web, doctor_web);
            webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);
        }


        /**
         * +
         * "<b><p id=\"comments_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Doctor's Note</p></b>" +
         * "%s"
         */

        // Keep a reference to WebView object until you pass the PrintDocumentAdapter
        // to the PrintManager
        mWebView = webView;
    }

    // presc create - end

    // presc pdf downlaod - end

    private void followupScheduledSuccess(Context context, Drawable drawable, String title, String subTitle, String neutral) {

        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.dialog_patient_registration, null);
        alertdialogBuilder.setView(convertView);
        ImageView icon = convertView.findViewById(R.id.dialog_icon);
        TextView dialog_title = convertView.findViewById(R.id.dialog_title);
        TextView dialog_subtitle = convertView.findViewById(R.id.dialog_subtitle);
        Button positive_btn = convertView.findViewById(R.id.positive_btn);
        Button negative_btn = convertView.findViewById(R.id.negative_btn);
        negative_btn.setVisibility(View.GONE);  // as this view requires only one button so other button has hidden.

        icon.setImageDrawable(drawable);
        dialog_title.setText(title);
        dialog_subtitle.setText(subTitle);
        positive_btn.setText(neutral);

        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);


        positive_btn.setOnClickListener(v -> {
            /*Intent intent = new Intent(PrescriptionActivity.this, HomeScreenActivity_New.class);
            startActivity(intent);*/
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    private void expandableCardVisibilityHandling() {
        presc_profile_header.setOnClickListener(v -> {
            if (vs_header_expandview.getVisibility() == View.VISIBLE)
                vs_header_expandview.setVisibility(View.GONE);
            else vs_header_expandview.setVisibility(View.VISIBLE);
        });

        dr_details_header_relative.setOnClickListener(v -> {
            if (vs_drdetails_header_expandview.getVisibility() == View.VISIBLE)
                vs_drdetails_header_expandview.setVisibility(View.GONE);
            else vs_drdetails_header_expandview.setVisibility(View.VISIBLE);
        });

        diagnosis_header_relative.setOnClickListener(v -> {
            if (vs_diagnosis_header_expandview.getVisibility() == View.VISIBLE)
                vs_diagnosis_header_expandview.setVisibility(View.GONE);
            else vs_diagnosis_header_expandview.setVisibility(View.VISIBLE);
        });

        medication_header_relative.setOnClickListener(v -> {
            if (mBinding.llPrescribedMedicine.getVisibility() == View.VISIBLE)
                mBinding.llPrescribedMedicine.setVisibility(View.GONE);
            else mBinding.llPrescribedMedicine.setVisibility(View.VISIBLE);
        });

        advice_header_relative.setOnClickListener(v -> {
            if (vs_adviceheader_expandview.getVisibility() == View.VISIBLE)
                vs_adviceheader_expandview.setVisibility(View.GONE);
            else vs_adviceheader_expandview.setVisibility(View.VISIBLE);
        });

        test_header_relative.setOnClickListener(v -> {
            if (vs_testheader_expandview.getVisibility() == View.VISIBLE)
                vs_testheader_expandview.setVisibility(View.GONE);
            else vs_testheader_expandview.setVisibility(View.VISIBLE);
        });

        referred_header_relative.setOnClickListener(v -> {
            if (vs_speciality_header_expandview.getVisibility() == View.VISIBLE)
                vs_speciality_header_expandview.setVisibility(View.GONE);
            else vs_speciality_header_expandview.setVisibility(View.VISIBLE);
        });

        followup_header_relative.setOnClickListener(v -> {
            if (vs_followup_header_expandview.getVisibility() == View.VISIBLE)
                vs_followup_header_expandview.setVisibility(View.GONE);
            else vs_followup_header_expandview.setVisibility(View.VISIBLE);
        });
    }

    // parse dr details - start
    ClsDoctorDetails details;

    private void parseDoctorDetails(String dbValue) {
        if (dbValue == null || dbValue.isEmpty() || dbValue.equalsIgnoreCase("null")) {
            Toast.makeText(this, getString(R.string.unablet_get_the_doct_info_alert), Toast.LENGTH_SHORT).show();
            return;
        }
        CustomLog.e("TAG", "parseDoctorDetails : " + dbValue);
        Gson gson = new Gson();
        details = gson.fromJson(dbValue, ClsDoctorDetails.class);

        if (details == null) {
            return;
        }
        CustomLog.e("TAG", "TEST VISIT: " + details.toString());
        drname.setText(details.getName());
        try {
            ProviderDTO providerDTO = new ProviderDAO().getProviderInfo(details.getUuid());
            String[] ymdData = DateAndTimeUtils.getAgeInYearMonth(providerDTO.getDateofbirth()).split(" ");
            int mAgeYears = Integer.valueOf(ymdData[0]);
            dr_age_gender.setText("(" + providerDTO.getGender() + ", " + mAgeYears + ")");
        } catch (DAOException e) {
            e.printStackTrace();
            CustomLog.e(TAG, e.getMessage());
        }

        if (details.getQualification() != null && !details.getQualification().isEmpty())
            qualification.setText(details.getQualification());
        dr_speciality.setText(details.getSpecialization());
    }

    private   String addBulletPoints(String inputString) {
        // Remove all occurrences of "&null" (case-insensitive) and "null" (case-insensitive)
        String cleanedString = inputString
                .replaceAll("(?i)& ?\\bnull\\b", "") // Remove "&null" or "null" with optional "&"
                .trim();

        // Split the cleaned string into lines
        String[] lines = cleanedString.split("\\r?\\n");
        StringBuilder result = new StringBuilder();

        // Add bullet point to each non-empty line
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                result.append("• ").append(line.trim()).append("\n");
            }
        }

        // Remove the last extra newline character
        if (result.length() > 0) {
            result.setLength(result.length() - 1);
        }

        return result.toString();
    }
  private   String addBulletPoints1(String inputString) {


        // Split the cleaned string into lines
        String[] lines = inputString.split("\\r?\\n");
        StringBuilder result = new StringBuilder();

        // Add bullet point to each non-empty line
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                result.append("• ").append(line.trim()).append("\n");
            }
        }

        // Remove the last extra newline character
        if (result.length() > 0) {
            result.setLength(result.length() - 1);
        }

        return result.toString();
    }

    // parse dr details - end
    // parse presc value - start

    /**
     * This method distinguishes between different concepts using switch case to populate the information into the relevant sections (eg:complaints, physical exam, vitals, etc.).
     *
     * @param concept_id variable of type int.
     * @param value      variable of type String.
     */
    private void parseData(String concept_id, String value) {
        switch (concept_id) {
            case UuidDictionary.CURRENT_COMPLAINT: { //Current Complaint
                complaint.setValue(value.replace("?<b>", Node.bullet_arrow));
                break;
            }
            case UuidDictionary.PHYSICAL_EXAMINATION: { //Physical Examination
                phyExam.setValue(value);
                break;
            }
            case UuidDictionary.HEIGHT: //Height
            {
                height.setValue(value);
                break;
            }
            case UuidDictionary.WEIGHT: //Weight
            {
                weight.setValue(value);
                break;
            }
            case UuidDictionary.PULSE: //Pulse
            {
                pulse.setValue(value);
                break;
            }
            case UuidDictionary.SYSTOLIC_BP: //Systolic BP
            {
                bpSys.setValue(value);
                break;
            }
            case UuidDictionary.DIASTOLIC_BP: //Diastolic BP
            {
                bpDias.setValue(value);
                break;
            }
            case UuidDictionary.TEMPERATURE: //Temperature
            {
                temperature.setValue(value);
                break;
            }
            //    Respiratory added by mahiti dev team
            case UuidDictionary.RESPIRATORY: //Respiratory
            {
                resp.setValue(value);
                break;
            }
            case UuidDictionary.SPO2: //SpO2
            {
                spO2.setValue(value);
                break;
            }

            case UuidDictionary.TELEMEDICINE_DIAGNOSIS: {
                if (!diagnosisReturned.isEmpty() && !diagnosisReturned.contains(value)) {
                    diagnosisReturned = diagnosisReturned + "\n" + value;
                } else {
                    diagnosisReturned = value;
                }
                diagnosis_txt.setText(addBulletPoints(diagnosisReturned));
                break;
            }

            case UuidDictionary.JSV_MEDICATIONS: {
                CustomLog.i("TAG", "parse_va: " + value);
                CustomLog.i("TAG", "parseData: rx" + rxReturned);
                if (!rxReturned.trim().isEmpty() && !rxReturned.contains(value)) {
                    rxReturned = rxReturned + "\n" + value;
                } else {
                    rxReturned = value;
                }
                CustomLog.i("TAG", "parseData: rxfin" + rxReturned);
//                medication_txt.setText(Html.fromHtml(getMedicationData()));
                setMedicationAdapter();
                //checkForDoctor();
                break;
            }
            case UuidDictionary.MEDICAL_ADVICE: {
                if (!adviceReturned.isEmpty() && !adviceReturned.contains(value)) {
                    adviceReturned = adviceReturned + "\n" + value;
                    CustomLog.d("GAME", "GAME: " + adviceReturned);
                } else {
                    adviceReturned = value;
                    CustomLog.d("GAME", "GAME_2: " + adviceReturned);
                }
              /*  if (medicalAdviceCard.getVisibility() != View.VISIBLE) {
                    medicalAdviceCard.setVisibility(View.VISIBLE);
                }*/
                //medicalAdviceTextView.setText(adviceReturned);
                CustomLog.d("Hyperlink", "hyper_global: " + medicalAdvice_string);

                int j = adviceReturned.indexOf('<');
                int i = adviceReturned.lastIndexOf('>');
                if (i >= 0 && j >= 0) {
                    medicalAdvice_HyperLink = adviceReturned.substring(j, i + 1);
                } else {
                    medicalAdvice_HyperLink = "";
                }

                CustomLog.d("Hyperlink", "Hyperlink: " + medicalAdvice_HyperLink);

                if (!medicalAdvice_HyperLink.isEmpty())
                {
                    medicalAdvice_string = adviceReturned.replaceAll(medicalAdvice_HyperLink, "\n");
                    advice_txt.setText(addBulletPoints1(medicalAdvice_string));
                }else
                {
                    medicalAdvice_string = adviceReturned.replaceAll(medicalAdvice_HyperLink, "");
                    advice_txt.setText(addBulletPoints1(medicalAdvice_string));
                }
                if (!medicalAdvice_string.equalsIgnoreCase(""))
                    advice_txt.setText(addBulletPoints1(medicalAdvice_string));
                CustomLog.d("Hyperlink", "hyper_string: " + medicalAdvice_string);

                /*
                 * variable a contains the hyperlink sent from webside.
                 * variable b contains the string data (medical advice) of patient.
                 * */
               /* medicalAdvice_string = medicalAdvice_string.replace("\n\n", "\n");
                medicalAdviceTextView.setText(Html.fromHtml(medicalAdvice_HyperLink +
                        medicalAdvice_string.replaceAll("\n", "<br><br>")));*/

                adviceReturned = adviceReturned.replaceAll("\n", "<br><br>");
                medicalAdvice = String.valueOf(Html.fromHtml(adviceReturned.replace("Doctor_", "Doctor")));
                Log.d(TAG, "kkparseData: medicalAdvice : " + medicalAdvice);
                //  medicalAdviceTextView.setText(Html.fromHtml(adviceReturned));
               /* medicalAdviceTextView.setText(Html.fromHtml(adviceReturned.replace("Doctor_", "Doctor")));
                medicalAdviceTextView.setMovementMethod(LinkMovementMethod.getInstance());
                CustomLog.d("hyper_textview", "hyper_textview: " + medicalAdviceTextView.getText().toString());*/
                //checkForDoctor();
                break;
            }
            case UuidDictionary.REQUESTED_TESTS: {
                if (!testsReturned.isEmpty() && !testsReturned.contains(value)) {
                    testsReturned = testsReturned + "\n\n" + Node.bullet + " " + value;
                } else {
                    testsReturned = Node.bullet + " " + value;
                }
                test_txt.setText(testsReturned);
              /*  if (requestedTestsCard.getVisibility() != View.VISIBLE) {
                    requestedTestsCard.setVisibility(View.VISIBLE);
                }
                requestedTestsTextView.setText(testsReturned);*/
                //checkForDoctor();
                break;
            }
            case UuidDictionary.REFERRED_SPECIALIST: {
                if (!referredSpeciality.isEmpty() && !referredSpeciality.contains(value)) {
                    referredSpeciality = referredSpeciality + "\n\n" + Node.bullet + " " + value;
                } else {
                    referredSpeciality = Node.bullet + " " + value;
                }
                referred_speciality_txt.setText(referredSpeciality);
            }
            case UuidDictionary.ADDITIONAL_COMMENTS: {

//                additionalCommentsCard.setVisibility(View.GONE);

                if (!additionalReturned.isEmpty() && !additionalReturned.contains(value)) {
                    additionalReturned = additionalReturned + "," + value;
                } else {
                    additionalReturned = value;
                }
////                if (additionalCommentsCard.getVisibility() != View.VISIBLE) {
////                    additionalCommentsCard.setVisibility(View.VISIBLE);
////                }
//                additionalCommentsTextView.setText(additionalReturned);
                //checkForDoctor();
                break;
            }
            case UuidDictionary.FOLLOW_UP_VISIT: {
                if (!followUpDate.isEmpty() && !followUpDate.contains(value)) {
                    followUpDate = followUpDate + "," + value;
                } else {
                    followUpDate = value;
                }

                if (followUpDate == null || followUpDate.isEmpty() || followUpDate.equalsIgnoreCase("No")) {

                    no_followup_txt.setVisibility(View.VISIBLE);
                    followup_date_block.setVisibility(View.GONE);

                    followup_subtext.setVisibility(View.GONE);
                    break;
                } else {
                    no_followup_txt.setVisibility(View.GONE);
                    followup_date_block.setVisibility(View.VISIBLE);

                    followup_subtext.setVisibility(View.VISIBLE);

                }

                if (followup_date_block.getVisibility() != View.VISIBLE) {
                    followup_date_block.setVisibility(View.VISIBLE);
                }
                if (no_followup_txt.getVisibility() == View.VISIBLE) {
                    no_followup_txt.setVisibility(View.GONE);
                }
                String followUpDate_format = DateAndTimeUtils.date_formatter(followUpDate, "yyyy-MM-dd", "dd MMMM,yyyy");
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
                    followUpDate_format = StringUtils.en__hi_dob(followUpDate_format);
                followup_date_txt.setText(followUpDate_format);
                CustomLog.v("Prescriotion", "followUpDate - " + followUpDate);

                if (DateAndTimeUtils.isCurrentDateBeforeFollowUpDate(followUpDate, "yyyy-MM-dd")) {
                    String followUpSubText = getResources().getString(R.string.doctor_suggested_follow_up_on, followUpDate_format);
                    if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                        followUpSubText = StringUtils.en__hi_dob(followUpSubText);
                    }
                    followup_subtext.setText(followUpSubText);
                } else {
                    followup_subtext.setText(getResources().getString(R.string.follow_up_date_arrived));
                }


                //checkForDoctor();
                break;
            }
            case UuidDictionary.BLOOD_GLUCOSE_RANDOM: // Glucose - Random
            {
                bloodGlucoseRandom.setValue(value);
                break;
            }
            case UuidDictionary.BLOOD_GLUCOSE_POST_PRANDIAL: // Glucose - Post-prandial
            {
                bloodGlucosePostPrandial.setValue(value);
                break;
            }
            case UuidDictionary.BLOOD_GLUCOSE_FASTING: // Glucose
            {
                bloodGlucoseFasting.setValue(value);
                break;
            }
            case UuidDictionary.HEMOGLOBIN: // Hemoglobin
            {
                hemoglobin.setValue(value);
                break;
            }
            case UuidDictionary.URIC_ACID: // Uric Acid
            {
                uricAcid.setValue(value);
                break;
            }
            case UuidDictionary.TOTAL_CHOLESTEROL: // Cholestrol
            {
                cholesterol.setValue(value);
                break;
            }

            default:
                CustomLog.i("TAG", "parseData: " + value);
                break;
        }
    }

    /**
     * handling additional data here
     */

    private String getMedicationData() {
        if (rxReturned.isEmpty()) {
            return "";
        }

        String finalMedicationDataString = "";
        String titleStart = "<font color=" + Color.GRAY + ">";
        String titleEnd = "</font>";

        StringBuilder medicationData = new StringBuilder();

        String[] medicationDataArray = rxReturned.split("\n");

        for (String s : medicationDataArray) {
            if (!s.contains(":")) {
                medicationData.append(titleStart);
                medicationData.append(getString(R.string.additional_instruction));
                medicationData.append(titleEnd);
                medicationData.append("<br>");
                medicationData.append(s);
            } else {
                medicationData.append(s);
                medicationData.append("<br>");
                medicationData.append("<br>");
            }
        }
        if (medicationData.length() == 0) return "";

        finalMedicationDataString = medicationData.toString();

        return finalMedicationDataString;
    }

    private List<PrescribedMedicineModel> getMedicationData1() {
        List<PrescribedMedicineModel> medicineModelList = new ArrayList<>();
        if (rxReturned.isEmpty()) {
            return medicineModelList;
        } else {
            mBinding.tvNoPrescription.setVisibility(View.GONE);
            mBinding.dividerNoPrescription.setVisibility(View.GONE);
        }
        hideAdditionalInstruction();

        String[] medicationDataArray = rxReturned.split("\n");

        for (String medicine : medicationDataArray) {
            if (medicine.contains(":")) {
                String[] medicineDetailArray = medicine.split(":");
                PrescribedMedicineModel medicineModel = new PrescribedMedicineModel();
                for (int i = 0; i < medicineDetailArray.length; i++) {
                    switch (i) {
                        case 0 -> medicineModel.setMedicineName(medicineDetailArray[i]);
                        case 1 -> medicineModel.setStrength(medicineDetailArray[i]);
                        case 2 -> medicineModel.setNoOfDays(medicineDetailArray[i]);
                        case 3 -> medicineModel.setTiming(medicineDetailArray[i]);
                        default -> medicineModel.setRemark(medicineDetailArray[i]);
                    }
                }
                medicineModelList.add(medicineModel);
            } else {
                if (!medicine.isEmpty()) {
                    setAdditionalInstruction(medicine);
                }

            }
        }
        return medicineModelList;
    }

    private void hideAdditionalInstruction() {
        mBinding.dividerAdditionalInstruction.setVisibility(View.GONE);
        mBinding.tvAdditionalInstructionDesc.setVisibility(View.GONE);
        mBinding.tvAdditionalInstructionTitle.setVisibility(View.GONE);
    }

    private void setAdditionalInstruction(String medicine) {
        mBinding.dividerAdditionalInstruction.setVisibility(View.VISIBLE);
        mBinding.tvAdditionalInstructionDesc.setVisibility(View.VISIBLE);
        mBinding.tvAdditionalInstructionTitle.setVisibility(View.VISIBLE);
        mBinding.tvAdditionalInstructionDesc.setText(medicine);
    }

    // parse presc value - end

    // downlaod - start
    public void downloadPrescriptionDefault() {
        String visitnote = "";
        EncounterDAO encounterDAO = new EncounterDAO();
        String encounterIDSelection = "visituuid = ? AND voided = ?";
        String[] encounterIDArgs = {visitID, "0"}; // so that the deleted values dont come in the presc.
        Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null);
        if (encounterCursor != null && encounterCursor.moveToFirst()) {
            do {
                if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_VISIT_NOTE").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                    visitnote = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                }
            } while (encounterCursor.moveToNext());

        }
        encounterCursor.close();

        String[] columns = {"value", " conceptuuid"};
        String visitSelection = "encounteruuid = ? and voided = ? and sync = ?";
        String[] visitArgs = {visitnote, "0", "TRUE"}; // so that the deleted values dont come in the presc.
        Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, null);
        if (visitCursor.moveToFirst()) {
            do {
                String dbConceptID = visitCursor.getString(visitCursor.getColumnIndex("conceptuuid"));
                String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                hasPrescription = true; //if any kind of prescription data is present...
                parseData(dbConceptID, dbValue);
            } while (visitCursor.moveToNext());
        }
        visitCursor.close();

        downloaded = true;
    }
    // query data - end

    // handle - start
    private void handleMessage(Intent msg) {
        CustomLog.i("TAG", "handleMessage: Entered");
        Bundle data = msg.getExtras();
        int check = 0;
        if (data != null) {
            check = data.getInt("Restart");
        }
        if (check == 100) {
            CustomLog.i("TAG", "handleMessage: 100");
            diagnosisReturned = "";
            rxReturned = "";
            testsReturned = "";
            referredSpeciality = "";
            adviceReturned = "";
            additionalReturned = "";
            followUpDate = "";
            String[] columns = {"value", " conceptuuid"};
            String visitSelection = "encounteruuid = ? ";
            String[] visitArgs = {encounterUuid};

            Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, null);
            if (visitCursor.moveToFirst()) {
                do {
                    String dbConceptID = visitCursor.getString(visitCursor.getColumnIndex("conceptuuid"));
                    String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                    parseData(dbConceptID, dbValue);
                } while (visitCursor.moveToNext());
            }
            visitCursor.close();

        } else if (check == 200) {
            CustomLog.i("TAG", "handleMessage: 200");
            String[] columns = {"concept_id"};
            String orderBy = "visit_id";

            //obscursor checks in obs table
            Cursor obsCursor = db.query("tbl_obs", columns, null, null, null, null, orderBy);

            //dbconceptid will store data found in concept_id

            if (obsCursor.moveToFirst() && obsCursor.getCount() > 1) {
                String dbConceptID = obsCursor.getString(obsCursor.getColumnIndex("conceptuuid"));

//                    if obsCursor founds something move to next
                while (obsCursor.moveToNext()) ;

                switch (dbConceptID) {
                    //case values for each prescription
                    case UuidDictionary.TELEMEDICINE_DIAGNOSIS:
                        CustomLog.i("TAG", "found diagnosis");
                        break;
                    case UuidDictionary.JSV_MEDICATIONS:
                        CustomLog.i("TAG", "found medications");
                        break;
                    case UuidDictionary.MEDICAL_ADVICE:
                        CustomLog.i("TAG", "found medical advice");
                        break;
                    case UuidDictionary.ADDITIONAL_COMMENTS:
                        CustomLog.i("TAG", "found additional comments");
                        break;
                    case UuidDictionary.REQUESTED_TESTS:
                        CustomLog.i("TAG", "found tests");
                        break;
                    default:
                }

                //if any obs  found then end the visit
                //endVisit();

            }
            obsCursor.close();
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleMessage(intent);
        }
    };

    public void registerDownloadPrescription() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("downloadprescription");
        ContextCompat.registerReceiver(this, downloadPrescriptionService, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    public void callBroadcastReceiver() {
        if (!isReceiverRegistered) {
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            receiver = new NetworkChangeReceiver();
            ContextCompat.registerReceiver(this, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
            isReceiverRegistered = true;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerDownloadPrescription();
        callBroadcastReceiver();
        ContextCompat.registerReceiver(this, mMessageReceiver, new IntentFilter(FILTER), ContextCompat.RECEIVER_NOT_EXPORTED);

        //register receiver for internet check
        networkUtils.callBroadcastReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (downloadPrescriptionService != null) {
                LocalBroadcastManager.getInstance(PrescriptionActivity.this).unregisterReceiver(downloadPrescriptionService);
            }
            if (receiver != null) {
                unregisterReceiver(receiver);
            }
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);


            //unregister receiver for internet check
            networkUtils.unregisterNetworkReceiver();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            CustomLog.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (receiver != null) {
            LocalBroadcastManager.getInstance(PrescriptionActivity.this).unregisterReceiver(receiver);
            receiver = null;
        }
        if (downloadPrescriptionService != null) {
            LocalBroadcastManager.getInstance(PrescriptionActivity.this).unregisterReceiver(downloadPrescriptionService);
            downloadPrescriptionService = null;
        }
        isReceiverRegistered = false;
    }

    @Override
    public void onResume() // register the receiver here
    {
        //get from encountertbl from the encounter
        if (visitnoteencounteruuid.equalsIgnoreCase("")) {
            visitnoteencounteruuid = getStartVisitNoteEncounterByVisitUUID(visitID);
        }

        if (downloadPrescriptionService == null) {
            registerDownloadPrescription();
        }
        super.onResume();

        callBroadcastReceiver();
    }

    @Override
    protected void onDestroy() {
        if (downloadPrescriptionService != null) {
            LocalBroadcastManager.getInstance(PrescriptionActivity.this).unregisterReceiver(downloadPrescriptionService);
        }
        super.onDestroy();

    }
    // handle - end

    public class DownloadPrescriptionService extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.logD("TAG", "Download prescription happen" + new SimpleDateFormat("yyyy MM dd_HH mm ss").format(Calendar.getInstance().getTime()));
            downloadPrescriptionDefault();
            downloadDoctorDetails();
        }
    }

    // downlaod dr - start
    private void downloadDoctorDetails() {
        String visitnote = "";
        EncounterDAO encounterDAO = new EncounterDAO();
        String encounterIDSelection = "visituuid = ? ";
        String[] encounterIDArgs = {visitID};
        String encounter_type_uuid_comp = "bd1fbfaa-f5fb-4ebd-b75c-564506fc309e";// make the encounter_type_uuid as constant later on.
        Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null);
        if (encounterCursor != null && encounterCursor.moveToFirst()) {
            do {
                if (encounter_type_uuid_comp.equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                    visitnote = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                }
            } while (encounterCursor.moveToNext());

        }
        encounterCursor.close();

        String[] columns = {"value", " conceptuuid"};
        String visitSelection = "encounteruuid = ? and voided!='1' ";
        String[] visitArgs = {visitnote};
        Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, null);
        if (visitCursor.moveToFirst()) {
            do {
                String dbConceptID = visitCursor.getString(visitCursor.getColumnIndex("conceptuuid"));
                String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                parseDoctorDetails(dbValue);
            } while (visitCursor.moveToNext());
        }
        visitCursor.close();
    }
    // downlaod dr - end

    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            isNetworkAvailable(context);
        }

    }

    // network check - start
    private void isNetworkAvailable(Context context) {
        int flag = 0;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            switch (activeNetwork.getType()) {
                case ConnectivityManager.TYPE_WIFI, ConnectivityManager.TYPE_MOBILE -> {
                    if (!isConnected) {
                        if (internetCheck != null) {
                            internetCheck.setIcon(R.mipmap.ic_data_on);
                            flag = 1;
                        }
                    }
                }
                default -> {
                    flag = 0;
                }
            }
        }

        if (flag == 0) {
            if (internetCheck != null) {
                internetCheck.setIcon(R.mipmap.ic_data_off);
            }
        }
    }
    // network check - end

    // downlaod presc - start
    public void downloadPrescription() {
        VisitsDAO visitsDAO = new VisitsDAO();
        try {
            if (visitsDAO.getDownloadedValue(visitID).equalsIgnoreCase("false") && uploaded) {
                String visitnote = "";

                EncounterDAO encounterDAO = new EncounterDAO();
                String encounterIDSelection = "visituuid = ? AND voided = ?";
                String[] encounterIDArgs = {visitID, "0"}; // voided = 0 so that the Deleted values dont come in the presc.
                Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null);
                if (encounterCursor != null && encounterCursor.moveToFirst()) {
                    do {
                        if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_VISIT_NOTE").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                            visitnote = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                        }
                    } while (encounterCursor.moveToNext());

                }
                //   if (encounterCursor != null) {
                encounterCursor.close();
                //   }

                if (!diagnosisReturned.isEmpty()) {
                    diagnosisReturned = "";
                    diagnosis_txt.setText("");
                    //   diagnosisCard.setVisibility(View.GONE);
                }
                if (!rxReturned.isEmpty()) {
                    rxReturned = "";
                    //  prescriptionCard.setVisibility(View.GONE);

                }
                if (!adviceReturned.isEmpty()) {
                    adviceReturned = "";
                    advice_txt.setText("");
                    //  medicalAdviceCard.setVisibility(View.GONE);
                }
                if (!referredSpeciality.isEmpty()) {
                    referredSpeciality = "";
                    referred_speciality_txt.setText("");
                    //  medicalAdviceCard.setVisibility(View.GONE);
                }
                if (!testsReturned.isEmpty()) {
                    testsReturned = "";
                    test_txt.setText("");
                }

                if (!followUpDate.isEmpty()) {
                    followUpDate = "";
                    followup_date_txt.setText("");
                    //  followUpDateCard.setVisibility(View.GONE);

                }

                String[] columns = {"value", " conceptuuid"};
                String visitSelection = "encounteruuid = ? and voided = ? and sync = ?";
                String[] visitArgs = {visitnote, "0", "TRUE"}; // so that the deleted values dont come in the presc.
                Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, null);
                if (visitCursor.moveToFirst()) {
                    do {
                        String dbConceptID = visitCursor.getString(visitCursor.getColumnIndex("conceptuuid"));
                        String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                        hasPrescription = true; //if any kind of prescription data is present...
                        parseData(dbConceptID, dbValue);
                    } while (visitCursor.moveToNext());
                }
                visitCursor.close();

                if (uploaded) {
                    try {
                        downloaded = visitsDAO.isUpdatedDownloadColumn(visitID, true);
                        Toast.makeText(PrescriptionActivity.this, getResources().getString(R.string.downloaded_successfully), Toast.LENGTH_SHORT).show();
                    } catch (DAOException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                        CustomLog.e(TAG, e.getMessage());
                    }
                }
                downloadDoctorDetails();
                syncAnimator.end();
            } else {
                syncAnimator.end();
                Toast.makeText(PrescriptionActivity.this, getResources().getString(R.string.prescription_up_to_date), Toast.LENGTH_SHORT).show();
            }

        } catch (DAOException e) {
            e.printStackTrace();
            CustomLog.e(TAG, e.getMessage());
        }
    }
    // downlaod presc - end

    // presc share - start
    private void sharePresc() {
        if (hasPrescription) {
            MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(PrescriptionActivity.this);
            final LayoutInflater inflater = LayoutInflater.from(PrescriptionActivity.this);
            View convertView = inflater.inflate(R.layout.dialog_sharepresc, null);
            alertdialogBuilder.setView(convertView);

            EditText editText = convertView.findViewById(R.id.editText_mobileno);
            Button sharebtn = convertView.findViewById(R.id.sharebtn);

           /* AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            EditText editText = new EditText(this);
            editText.setInputType(InputType.TYPE_CLASS_PHONE);

            InputFilter inputFilter = new InputFilter() {
                @Override
                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                    return null;
                }
            };*/

            String partial_whatsapp_presc_url = new UrlModifiers().setwhatsappPresciptionUrl();
            String prescription_link = new VisitAttributeListDAO().getVisitAttributesList_specificVisit(visitID, PRESCRIPTION_LINK);
            String whatsapp_url = partial_whatsapp_presc_url.concat(prescription_link);
            editText.setText(patient.getPhone_number());

//                    Spanned hyperlink_whatsapp = HtmlCompat.fromHtml("<a href=" + whatsapp_url + ">Click Here</a>", HtmlCompat.FROM_HTML_MODE_COMPACT);

            //  editText.setFilters(new InputFilter[]{inputFilter, new InputFilter.LengthFilter(15)});
           /* LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            editText.setLayoutParams(layoutParams);
            alertDialog.setView(editText);
*/
            //   alertDialog.setMessage(getResources().getString(R.string.enter_mobile_number_to_share_prescription));
            sharebtn.setOnClickListener(v -> {
                if (!editText.getText().toString().equalsIgnoreCase("")) {
                    String phoneNumber = /*"+91" +*/ editText.getText().toString();
                    String whatsappMessage = String.format("https://api.whatsapp.com/send?phone=%s&text=%s", phoneNumber, getResources().getString(R.string.hello_thankyou_for_using_intelehealth_app_to_download_click_here) + partial_whatsapp_presc_url + Uri.encode("#") + prescription_link + getString(R.string.and_enter_your_patient_id) + openmrsID_txt.getText().toString());
                    CustomLog.v("whatsappMessage", whatsappMessage);
                    // Toast.makeText(context, R.string.whatsapp_presc_toast, Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(whatsappMessage)));

                    // isreturningWhatsapp = true;

                } else {
                    Toast.makeText(PrescriptionActivity.this, getResources().getString(R.string.please_enter_mobile_number), Toast.LENGTH_SHORT).show();
                }

            });

            AlertDialog alertDialog = alertdialogBuilder.create();
            alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
            int width = PrescriptionActivity.this.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
            alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            alertDialog.show();

//            alertDialog.setPositiveButton(getResources().getString(R.string.share),
//                    new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//
//                            if (!editText.getText().toString().equalsIgnoreCase("")) {
//                                String phoneNumber = /*"+91" +*/ editText.getText().toString();
//                                String whatsappMessage = getResources().getString(R.string.hello_thankyou_for_using_intelehealth_app_to_download_click_here)
//                                        + whatsapp_url + getString(R.string.and_enter_your_patient_id) + idView.getText().toString();
//
//                                // Toast.makeText(context, R.string.whatsapp_presc_toast, Toast.LENGTH_LONG).show();
//                                startActivity(new Intent(Intent.ACTION_VIEW,
//                                        Uri.parse(
//                                                String.format("https://api.whatsapp.com/send?phone=%s&text=%s",
//                                                        phoneNumber, whatsappMessage))));
//
//                                // isreturningWhatsapp = true;
//
//                            } else {
//                                Toast.makeText(context, getResources().getString(R.string.please_enter_mobile_number),
//                                        Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });

           /* AlertDialog dialog = alertDialog.show();
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
            //alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);*/
        } else {
            /*AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage(getResources().getString(R.string.download_prescription_first_before_sharing));
            alertDialog.setPositiveButton(getResources().getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Dialog.dismiss();
                        }
                    });

            AlertDialog dialog = alertDialog.show();
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
            //alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);*/

            Toast.makeText(PrescriptionActivity.this, getResources().getString(R.string.download_prescription_first_before_sharing), Toast.LENGTH_SHORT).show();
        }
    }

//    private void sharePresc() {
//        if (hasPrescription.equalsIgnoreCase("true")) {
//            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
//            EditText editText = new EditText(this);
//            editText.setInputType(InputType.TYPE_CLASS_PHONE);
//
//            InputFilter inputFilter = new InputFilter() {
//                @Override
//                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
//                    return null;
//                }
//            };
//            String partial_whatsapp_presc_url = new UrlModifiers().setwhatsappPresciptionUrl();
//            String whatsapp_url = partial_whatsapp_presc_url.concat(visitID);
//
//            editText.setFilters(new InputFilter[]{inputFilter, new InputFilter.LengthFilter(15)});
//            editText.setText(patient.getPhone_number());
//            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams
//                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            editText.setLayoutParams(layoutParams);
//            alertDialog.setView(editText);
//
//            alertDialog.setMessage(getResources().getString(R.string.enter_mobile_number_to_share_prescription));
//            alertDialog.setPositiveButton(getResources().getString(R.string.share),
//                    new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//
//                            if (!editText.getText().toString().equalsIgnoreCase("")) {
//                                String phoneNumber = /*"+91" +*/ editText.getText().toString();
//                                String whatsappMessage = getResources().getString(R.string.hello_thankyou_for_using_intelehealth_app_to_download_click_here)
//                                        + whatsapp_url + getString(R.string.and_enter_your_patient_id) + openmrsID_txt.getText().toString();
//
//                                startActivity(new Intent(Intent.ACTION_VIEW,
//                                        Uri.parse(
//                                                String.format("https://api.whatsapp.com/send?phone=%s&text=%s",
//                                                        phoneNumber, whatsappMessage))));
//                            } else {
//                                Toast.makeText(PrescriptionActivity.this, getResources().getString(R.string.please_enter_mobile_number),
//                                        Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });
//
//            AlertDialog dialog = alertDialog.show();
//            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
//            positiveButton.setTextColor(PrescriptionActivity.this.getResources().getColor(R.color.colorPrimaryDark));
//            //alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(Typeface.DEFAULT, Typeface.BOLD);
//            IntelehealthApplication.setAlertDialogCustomTheme(PrescriptionActivity.this, dialog);
//        }
//        else {
//            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
//            alertDialog.setMessage(getResources().getString(R.string.download_prescription_first_before_sharing));
//            alertDialog.setPositiveButton(getResources().getString(R.string.ok),
//                    new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            Dialog.dismiss();
//                        }
//                    });
//
//            AlertDialog dialog = alertDialog.show();
//            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
//            positiveButton.setTextColor(PrescriptionActivity.this.getResources().getColor(R.color.colorPrimaryDark));
//            //alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(Typeface.DEFAULT, Typeface.BOLD);
//            IntelehealthApplication.setAlertDialogCustomTheme(PrescriptionActivity.this, dialog);
//        }
//    }
    // presc share - end

    private boolean mIsCCInOldFormat = true;
    private List<String> mChiefComplainList = new ArrayList<>();
    private String complaintLocalString = "";

    // Print - start
    private void doWebViewPrint_Button() throws ParseException {
        // Create a WebView object specifically for printing
        WebView webView = new WebView(this);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                CustomLog.i("Patient WebView", "page finished loading " + url);
                int webview_heightContent = view.getContentHeight();
                CustomLog.d("variable i", "variable i: " + webview_heightContent);
                createWebPrintJob_Button(view, webview_heightContent);
                mWebView = null;
            }
        });

        String mPatientName = patient.getFirst_name() + " " + ((!TextUtils.isEmpty(patient.getMiddle_name())) ? patient.getMiddle_name() : "") + " " + patient.getLast_name();
        String mPatientOpenMRSID = patient.getOpenmrs_id();
        String mPatientDob = patient.getDate_of_birth();
        String mAddress = ((!TextUtils.isEmpty(patient.getAddress1())) ? patient.getAddress1() + "\n" : "") + ((!TextUtils.isEmpty(patient.getAddress2())) ? patient.getAddress2() : "");
        String mCityState = patient.getCity_village();
        String mPhone = (!TextUtils.isEmpty(patient.getPhone_number())) ? patient.getPhone_number() : "";
        String mState = patient.getState_province();
        String mCountry = patient.getCountry();

        String mSdw = (!TextUtils.isEmpty(patient.getSdw())) ? patient.getSdw() : "";
        String mOccupation = patient.getOccupation();
        String mGender = patient.getGender();

        Calendar c = Calendar.getInstance();
        System.out.println(getString(R.string.current_time) + c.getTime());

        String[] columnsToReturn = {"startdate"};
        String visitIDorderBy = "startdate";
        String visitIDSelection = "uuid = ?";
        String[] visitIDArgs = {visitID};
        db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        final Cursor visitIDCursor = db.query("tbl_visit", columnsToReturn, visitIDSelection, visitIDArgs, null, null, visitIDorderBy);
        visitIDCursor.moveToLast();
        String startDateTime = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("startdate"));
        visitIDCursor.close();
        String mDate = DateAndTimeUtils.SimpleDatetoLongDate(startDateTime);

        String mPatHist = patHistory.getValue();
        if (mPatHist == null) {
            mPatHist = "";
        }
        String mFamHist = famHistory.getValue();
        if (mFamHist == null) {
            mFamHist = "";
        }

        mHeight = height.getValue();
        mWeight = weight.getValue();
        mBP = bpSys.getValue() + "/" + bpDias.getValue();
        mPulse = pulse.getValue();
        mBloodGlucoseRandom = bloodGlucoseRandom.getValue();
        mBloodGlucoseFasting = bloodGlucoseFasting.getValue();
        mBloodGlucosePostPrandial = bloodGlucosePostPrandial.getValue();
        mHemoglobin = hemoglobin.getValue();
        mCholesterol = cholesterol.getValue();
        mUricAcid = uricAcid.getValue();
        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(Objects.requireNonNullElse(FileUtils.readFileRoot(CONFIG_FILE_NAME, this), String.valueOf(FileUtils.encodeJSON(this, CONFIG_FILE_NAME)))); //Load the config file
            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(this, mFileName)));
            }//Load the config file

            if (obj.getBoolean("mTemperature")) {
                if (obj.getBoolean("mCelsius")) {

                    mTemp = getResources().getString(R.string.prescription_temp_c) + " " + (!TextUtils.isEmpty(temperature.getValue()) ? temperature.getValue().toString() : "");

                } else if (obj.getBoolean("mFahrenheit")) {

//                    mTemp = "Temperature(F): " + temperature.getValue();
                    mTemp = getResources().getString(R.string.prescription_temp_f) + " " + (!TextUtils.isEmpty(temperature.getValue()) ? convertCtoF(temperature.getValue()) : "");
                }
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            CustomLog.e(TAG, e.getMessage());
        }
        mresp = resp.getValue();
        mSPO2 = getResources().getString(R.string.spo2) + ": " + (!TextUtils.isEmpty(spO2.getValue()) ? spO2.getValue() : "");
        String mComplaint = complaint.getValue();

        //Show only the headers of the complaints in the printed prescription
        String[] complaints = org.apache.commons.lang3.StringUtils.split(mComplaint, Node.bullet_arrow);
        mComplaint = "";
        /*String colon = ":";
        String mComplaint_new = "";
        if (complaints != null) {
            for (String comp : complaints) {
                if (!comp.trim().isEmpty()) {
                    mComplaint = mComplaint + Node.big_bullet + comp.substring(0, comp.indexOf(colon)) + "<br/>";

                }
            }
            if (!mComplaint.isEmpty()) {
                mComplaint = mComplaint.substring(0, mComplaint.length() - 2);
                mComplaint = mComplaint.replaceAll("<b>", "");
                mComplaint = mComplaint.replaceAll("</b>", "");
            }
        }

        if (mComplaint.contains("Associated symptoms")) {
            String[] cc = org.apache.commons.lang3.StringUtils.split(mComplaint, Node.bullet_arrow);
            for (String compla : cc) {
                mComplaint = mComplaint.substring(0, compla.indexOf("Associated symptoms") - 3);
                //   mComplaint = "Test Complaint";
            }
        } else {

        }

        if (mComplaint.contains("जुड़े लक्षण")) {
            String[] cc = org.apache.commons.lang3.StringUtils.split(mComplaint, Node.bullet_arrow);
            for (String compla : cc) {
                mComplaint = mComplaint.substring(0, compla.indexOf("जुड़े लक्षण") - 3);
            }
        } else {

        }*/

        if (complaint.getValue() != null) {
            String value = complaint.getValue();
            //boolean isInOldFormat = true;
            //Show Visit summary data in Clinical Format for English language only
            //Else for other language keep the data in Question Answer format
            if (value.startsWith("{") && value.endsWith("}")) {
                try {
                    JSONObject jsonObject = new JSONObject(value);
                    if (!sessionManager.getAppLanguage().equals("en") && jsonObject.has("l-" + sessionManager.getAppLanguage())) {
                        value = jsonObject.getString("l-" + sessionManager.getAppLanguage());
                        mIsCCInOldFormat = false;
                    } else {
                        value = jsonObject.getString("en");
                        mIsCCInOldFormat = true;
                    }
                    complaintLocalString = value;
                } catch (JSONException e) {
                    e.printStackTrace();
                    CustomLog.e(TAG, e.getMessage());
                }
            }

            String valueArray[] = null;
            boolean isAssociateSymptomFound = false;
            if (mIsCCInOldFormat) {
                mChiefComplainList.clear();
                valueArray = value.split("►<b> " + Node.ASSOCIATE_SYMPTOMS + "</b>:  <br/>");
                isAssociateSymptomFound = valueArray.length >= 2;
                String[] headerchips = valueArray[0].split("►");
                List<String> cc_tempvalues = new ArrayList<>(Arrays.asList(headerchips));

                for (int i = 0; i < cc_tempvalues.size(); i++) {
                    if (!cc_tempvalues.get(i).equalsIgnoreCase(""))
                        mChiefComplainList.add(cc_tempvalues.get(i).substring(0, headerchips[i].indexOf(":")));
                }


            } else {
                String answerInLocale = complaintLocalString;
                mChiefComplainList.clear();
                String lCode = sessionManager.getAppLanguage();
                //String answerInLocale = mSummaryStringJsonObject.getString("l-" + lCode);
                answerInLocale = answerInLocale.replaceAll("<.*?>", "");
                System.out.println(answerInLocale);
                //Log.v(TAG, answerInLocale);
                //►दस्त::● आपको ये लक्षण कब से है• 6 घंटे● दस्त शुरू कैसे हुए?•धीरे धीरे● २४ घंटे में कितनी बार दस्त हुए?•३ से कम बार● दस्त किस प्रकार के है?•पक्का● क्या आपको पिछले महीनो में दस्त शुरू होने से पहले किसी असामान्य भोजन/तरल पदार्थ से अपच महसूस हुआ है•नहीं● क्या आपने आज यहां आने से पहले इस समस्या के लिए कोई उपचार (स्व-दवा या घरेलू उपचार सहित) लिया है या किसी स्वास्थ्य प्रदाता को दिखाया है?•कोई नहीं● अतिरिक्त जानकारी•bsbdbd►क्या आपको निम्न लक्षण है::•उल्टीPatient denies -•दस्त के साथ पेट दर्द•सुजन•मल में खून•बुखार•अन्य [वर्णन करे]

                String[] spt = answerInLocale.split("►");
                List<String> list = new ArrayList<>();
                String associatedSymptomsString = "";
                for (String s : spt) {
                    if (s.isEmpty()) continue;
                    //String s1 =  new String(s.getBytes(), "UTF-8");
                    System.out.println("Chunk - " + s);
                    //if (s.trim().startsWith(getTranslatedAssociatedSymptomQString(lCode))) {
                    //if (s.trim().contains("Patient denies -•")) {
                    if (s.trim().contains(getTranslatedPatientDenies(lCode)) || s.trim().contains(getTranslatedAssociatedSymptomQString(lCode))) {
                        associatedSymptomsString = s;
                        System.out.println("associatedSymptomsString - " + associatedSymptomsString);
                    } else {
                        list.add(s);
                    }

                }


                for (int i = 0; i < list.size(); i++) {
                    String complainName = "";
                    List<VisitSummaryData> visitSummaryDataList = new ArrayList<>();
                    String[] spt1 = list.get(i).split("●");
                    for (String v1 : spt1) {
                        if (v1.contains("::")) {
                            complainName = v1.replace("::", "");
                            System.out.println(complainName);
                            mChiefComplainList.add(complainName);
                        }
                    }

                }
            }


        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < mChiefComplainList.size(); i++) {

            String val = mChiefComplainList.get(i).trim();
            val = val.replaceAll("<.*?>", "");
            CustomLog.v("mChiefComplainList", "CC - " + val);
            if (!val.toLowerCase().contains("h/o specific illness")) {
                if (!stringBuilder.toString().isEmpty()) {
                    stringBuilder.append(",");
                }
                stringBuilder.append(val);
            }

        }
        mComplaint = stringBuilder.toString().trim();


        if (mPatientOpenMRSID == null) {
            mPatientOpenMRSID = getString(R.string.patient_not_registered);
        }

        String para_open = "<p style=\"font-size:11pt; margin: 0px; padding: 0px;\">";
        String para_close = "</p>";

        Calendar today = Calendar.getInstance();
        Calendar dob = Calendar.getInstance();


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = sdf.parse(mPatientDob);
        dob.setTime(date);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        String rx_web = stringToWeb(rxReturned);

        String tests_web = stringToWeb(testsReturned.trim().replace("\n\n", "\n").replace(Node.bullet, ""));

        String referredSpeciality_web = stringToWeb(referredSpeciality);

        String advice_web = stringToWeb(adviceReturned);
        //  String advice_web = "";
//        if(medicalAdviceTextView.getText().toString().indexOf("Start") != -1 ||
//                medicalAdviceTextView.getText().toString().lastIndexOf(("User") + 6) != -1) {
   /*     String advice_doctor__ = medication_txt.getText().toString()
                .replace("Start Audio Call with Doctor", "Start Audio Call with Doctor_")
                .replace("Start WhatsApp Call with Doctor", "Start WhatsApp Call with Doctor_");

        if (advice_doctor__.indexOf("Start") != -1 ||
                advice_doctor__.lastIndexOf(("Doctor_") + 9) != -1) {

            String advice_split = new StringBuilder(advice_doctor__)
                    .delete(advice_doctor__.indexOf("Start"),
                            advice_doctor__.lastIndexOf("Doctor_") + 9).toString();

            advice_web = stringToWeb(advice_split.replace("\n\n", "\n")); //showing advice here...
            CustomLog.d("Hyperlink", "hyper_print: " + advice_web); //gets called when clicked on button of print button
        } else {
            advice_web = stringToWeb(advice_doctor__.replace("\n\n", "\n")); //showing advice here...
            CustomLog.d("Hyperlink", "hyper_print: " + advice_web); //gets called when clicked on button of print button
        }*/

        String diagnosis_web = stringToWeb(diagnosisReturned);

//        String comments_web = stringToWeb(additionalReturned);

        String followUpDateStr = "";
        if (followUpDate != null && followUpDate.contains(",")) {
            String[] spiltFollowDate = followUpDate.split(",");
            if (spiltFollowDate[0] != null && spiltFollowDate[0].contains("-")) {
                String remainingStr = "";
                for (int i = 1; i <= spiltFollowDate.length - 1; i++) {
                    remainingStr = ((!TextUtils.isEmpty(remainingStr)) ? remainingStr + ", " : "") + spiltFollowDate[i];
                }
                followUpDateStr = parse_DateToddMMyyyy_new(spiltFollowDate[0]) + ", " + remainingStr;
            } else {
                followUpDateStr = followUpDate;
            }
        } else {
            followUpDateStr = followUpDate;
        }

        String followUp_web = stringToWeb(followUpDateStr);

        String doctor_web = stringToWeb(doctorName);

        String heading = prescription1;
        String heading2 = prescription2;
        String heading3 = "<br/>";

        //  String bp = mBP;
        String bp = "";
        if (bp.equals("/") || bp.equals("null/null")) bp = "";

        String address = mAddress + " " + mCityState + ((!TextUtils.isEmpty(mPhone)) ? ", " + mPhone : "");

        String fam_hist = mFamHist;
        String pat_hist = mPatHist;

        if (fam_hist.trim().isEmpty()) {
            fam_hist = getString(R.string.no_history_family_found);
        } else {
            fam_hist = fam_hist.replaceAll(Node.bullet, Node.big_bullet);
        }

        if (pat_hist.trim().isEmpty()) {
            pat_hist = getString(R.string.no_history_patient_illness_found);
        }

        // Generate an HTML document on the fly:
        String fontFamilyFile = "";
        if (details != null && details.getFontOfSign() != null) {
            if (details.getFontOfSign().toLowerCase().equalsIgnoreCase("youthness")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/Youthness.ttf');";
            } else if (details.getFontOfSign().toLowerCase().equalsIgnoreCase("asem")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/Asem.otf');";
            } else if (details.getFontOfSign().toLowerCase().equalsIgnoreCase("arty")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/Arty.otf');";
            } else if (details.getFontOfSign().toLowerCase().equalsIgnoreCase("almondita")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/almondita.ttf');";
            }
        }

        String font_face = "<style>" + "                @font-face {" + "                    font-family: \"MyFont\";" + fontFamilyFile + "                }" + "            </style>";

        String doctorSign = "";
        String doctrRegistartionNum = "";
        // String docDigitallySign = "";
        String doctorDetailStr = "";
        if (details != null) {
            //  docDigitallySign = "Digitally Signed By";
            doctorSign = details.getTextOfSign();

            sign_url = BuildConfig.SERVER_URL + "/ds/" + details.getUuid() + "_sign.png";
            CustomLog.v("signurl", "signurl: " + sign_url);

            doctrRegistartionNum = !TextUtils.isEmpty(details.getRegistrationNumber()) ? getString(R.string.dr_registration_no) + details.getRegistrationNumber() : "";

            doctorDetailStr = "<div style=\"text-align:right;margin-right:0px;margin-top:0px;\">" + "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + details.getName() + "</span><br>" + "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + "  " + (details.getQualification() == null || details.getQualification().equalsIgnoreCase("null") ? "" : details.getQualification() + ", ") + details.getSpecialization() + "</span><br>" +
                    //  "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(details.getPhoneNumber()) ?
                    //  getString(R.string.dr_phone_number) + details.getPhoneNumber() : "") + "</span><br>" +
                    "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(details.getEmailId()) ? getString(R.string.dr_email) + details.getEmailId() : "") + "</span><br>" + "</div>";
        }

        PrescriptionBuilder prescriptionBuilder = new PrescriptionBuilder(this);
        VitalsObject vitalsData = getAllVitalsData();
        String prescriptionString = prescriptionBuilder.builder(patient, vitalsData, diagnosisReturned, rxReturned, adviceReturned, testsReturned, referredSpeciality, followUpDate, details, mFeatureActiveStatus);

        if (isRespiratory) {
            String htmlDocument = String.format(font_face + "<b><p id=\"heading_1\" style=\"font-size:16pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<p id=\"heading_2\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<p id=\"heading_3\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<hr style=\"font-size:12pt;\">" + "<br/>" +
                            /* doctorDetailStr +*/
                            "<p id=\"patient_name\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">%s</p></b>" + "<p id=\"patient_details\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Age: %s | Gender: %s  </p>" + "<p id=\"address_and_contact\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Address and Contact: %s</p>" + "<p id=\"visit_details\" style=\"font-size:12pt; margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient Id: %s | Date of visit: %s </p><br>" + "<b><p id=\"vitals_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px;; padding: 0px;\">Vitals</p></b>" + "<p id=\"vitals\" style=\"font-size:12pt;margin:0px; padding: 0px;\">Height(cm): %s | Weight(kg): %s | BMI: %s | Blood Pressure: %s | Pulse(bpm): %s | %s | Respiratory Rate: %s |  %s </p><br>" +
                            //"<b><p id=\"patient_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient History</p></b>" +
                            //"<p id=\"patient_history\" style=\"font-size:11pt;margin:0px; padding: 0px;\"> %s</p><br>" +
                            //"<b><p id=\"family_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Family History</p></b>" +
                            //"<p id=\"family_history\" style=\"font-size:11pt;margin: 0px; padding: 0px;\"> %s</p><br>" +
                            "<b><p id=\"complaints_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Presenting complaint(s)</p></b>" + para_open + "%s" + para_close + "<br>" + "<u><b><p id=\"diagnosis_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Diagnosis</p></b></u>" + "%s<br>" + "<u><b><p id=\"rx_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Medication(s) plan</p></b></u>" + "%s<br>" + "<u><b><p id=\"tests_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Recommended Investigation(s)</p></b></u>" + "%s<br>" + "<u><b><p id=\"advice_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">General Advice</p></b></u>" + "%s<br>" + "<u><b><p id=\"follow_up_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Follow Up Date</p></b></u>" + "%s<br>" + "<div style=\"text-align:right;margin-right:50px;margin-top:0px;\">" +
                            //  "<span style=\"font-size:80pt;font-family: MyFont;padding: 0px;\">" + doctorSign + "</span>" +
                            "<img src=" + sign_url + " alt=\"Dr Signature\">" + // doctor signature...
                            doctorDetailStr + "<p style=\"font-size:12pt; margin-top:-0px; padding: 0px;\">" + doctrRegistartionNum + "</p>" + "</div>", heading, heading2, heading3, mPatientName, age, mGender, /*mSdw*/ address, mPatientOpenMRSID, mDate, (!TextUtils.isEmpty(mHeight)) ? mHeight : "", (!TextUtils.isEmpty(mWeight)) ? mWeight : "", (!TextUtils.isEmpty(mBMI)) ? mBMI : "", (!TextUtils.isEmpty(bp)) ? bp : "", (!TextUtils.isEmpty(mPulse)) ? mPulse : "", (!TextUtils.isEmpty(mTemp)) ? mTemp : "", (!TextUtils.isEmpty(mresp)) ? mresp : "", (!TextUtils.isEmpty(mSPO2)) ? mSPO2 : "",
//                            pat_hist, fam_hist,
                    mComplaint, diagnosis_web, rx_web, tests_web, advice_web, followUp_web, doctor_web);
            webView.loadDataWithBaseURL(null, prescriptionString, "text/HTML", "UTF-8", null);
        } else {
            String htmlDocument = String.format(font_face + "<b><p id=\"heading_1\" style=\"font-size:16pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<p id=\"heading_2\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<p id=\"heading_3\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<hr style=\"font-size:12pt;\">" + "<br/>" + "<p id=\"patient_name\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">%s</p></b>" + "<p id=\"patient_details\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Age: %s | Gender: %s </p>" + "<p id=\"address_and_contact\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Address and Contact: %s</p>" + "<p id=\"visit_details\" style=\"font-size:12pt; margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient Id: %s | Date of visit: %s </p><br>" + "<b><p id=\"vitals_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px;; padding: 0px;\">Vitals</p></b>" + "<p id=\"vitals\" style=\"font-size:12pt;margin:0px; padding: 0px;\">Height(cm): %s | Weight(kg): %s | BMI: %s | Blood Pressure: %s | Pulse(bpm): %s | %s | %s </p><br>" +
                                    /*"<b><p id=\"patient_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient History</p></b>" +
                                    "<p id=\"patient_history\" style=\"font-size:11pt;margin:0px; padding: 0px;\"> %s</p><br>" +
                                    "<b><p id=\"family_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Family History</p></b>" +
                                    "<p id=\"family_history\" style=\"font-size:11pt;margin: 0px; padding: 0px;\"> %s</p><br>" +*/
                            "<b><p id=\"complaints_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Presenting complaint(s)</p></b>" + para_open + "%s" + para_close + "<br><br>" + "<u><b><p id=\"diagnosis_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Diagnosis</p></b></u>" + "%s<br>" + "<u><b><p id=\"rx_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Medication(s) plan</p></b></u>" + "%s<br>" + "<u><b><p id=\"tests_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Recommended Investigation(s)</p></b></u>" + "%s<br>" + "<u><b><p id=\"advice_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">General Advice</p></b></u>" + "%s<br>" + "<u><b><p id=\"follow_up_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Follow Up Date</p></b></u>" + "%s<br>" + "<div style=\"text-align:right;margin-right:50px;margin-top:0px;\">" + "<span style=\"font-size:80pt;font-family: MyFont;padding: 0px;\">" + doctorSign + "</span><br>" + doctorDetailStr + "<span style=\"font-size:12pt; margin-top:5px; padding: 0px;\">" + doctrRegistartionNum + "</span>" + "</div>", heading, heading2, heading3, mPatientName, age, mGender, /*mSdw*/ address, mPatientOpenMRSID, mDate,
                    /*(!TextUtils.isEmpty(mHeight)) ? mHeight :*/ "",
                    /*(!TextUtils.isEmpty(mWeight)) ? mWeight :*/ "",
                    /*(!TextUtils.isEmpty(mBMI)) ? mBMI :*/ "", (!TextUtils.isEmpty(bp)) ? bp : "",
                    /*(!TextUtils.isEmpty(mPulse)) ? mPulse :*/ "",
                    /*(!TextUtils.isEmpty(mTemp)) ? mTemp :*/ "",
                    /*(!TextUtils.isEmpty(mSPO2)) ? mSPO2 :*/ "",
                    /*pat_hist, fam_hist,*/
                    /*mComplaint*/ "", diagnosis_web, rx_web, tests_web, advice_web, followUp_web, doctor_web);
            webView.loadDataWithBaseURL(null, prescriptionString, "text/HTML", "UTF-8", null);
        }


        /**
         * +
         * "<b><p id=\"comments_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Doctor's Note</p></b>" +
         * "%s"
         */

        // Keep a reference to WebView object until you pass the PrintDocumentAdapter
        // to the PrintManager
        mWebView = webView;
    }

    private VitalsObject getAllVitalsData() {
        VitalsObject vitalsObject = new VitalsObject();
        vitalsObject.setHeight(height.getValue());
        vitalsObject.setWeight(weight.getValue());
        vitalsObject.setPulse(pulse.getValue());
        vitalsObject.setResp(resp.getValue());
        vitalsObject.setSpo2(spO2.getValue());
        vitalsObject.setTemperature(temperature.getValue());
        vitalsObject.setBpdia(bpDias.getValue());
        vitalsObject.setBpsys(bpSys.getValue());
        return vitalsObject;
    }

    // print job
    //print button start
    private void createWebPrintJob_Button(WebView webView, int contentHeight) {
        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) this.getSystemService(Context.PRINT_SERVICE);
        String docName = this.getString(R.string.app_name) + " Prescription";

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(docName);
        CustomLog.d("webview content height", "webview content height: " + contentHeight);

        if (contentHeight > 2683 && contentHeight <= 3000) {
            //medium size prescription...
            PrintAttributes.Builder pBuilder = new PrintAttributes.Builder();
            pBuilder.setMediaSize(PrintAttributes.MediaSize.ISO_B4);
            pBuilder.setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600));
            pBuilder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);
            // Create a print job with name and adapter instance
            String jobName = getString(R.string.app_name) + getResources().getString(R.string._visit_summary);

            //To display the preview window to user...
            PrintJob printJob = printManager.print(jobName, printAdapter, pBuilder.build());


        } else if (contentHeight == 0) {
            //in case of webview bug of 0 contents...
            PrintAttributes.Builder pBuilder = new PrintAttributes.Builder();
            pBuilder.setMediaSize(PrintAttributes.MediaSize.JIS_B4);
            pBuilder.setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600));
            pBuilder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);
            // Create a print job with name and adapter instance
            String jobName = getString(R.string.app_name) + getResources().getString(R.string._visit_summary);

            //To display the preview window to user...
            PrintJob printJob = printManager.print(jobName, printAdapter, pBuilder.build());

            //TODO: write different functions for <= Lollipop versions..

        } else if (contentHeight > 3000) {
            //large size prescription...
            PrintAttributes.Builder pBuilder = new PrintAttributes.Builder();
            pBuilder.setMediaSize(PrintAttributes.MediaSize.JIS_B4);
            pBuilder.setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600));
            pBuilder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);
            // Create a print job with name and adapter instance
            String jobName = getString(R.string.app_name) + getResources().getString(R.string._visit_summary);

            //To display the preview window to user...
            PrintJob printJob = printManager.print(jobName, printAdapter, pBuilder.build());
        } else {
            String jobName = getString(R.string.app_name) + getResources().getString(R.string._visit_summary);

            CustomLog.d("PrintPDF", "PrintPDF");
            PrintAttributes.Builder pBuilder = new PrintAttributes.Builder();
            pBuilder.setMediaSize(PrintAttributes.MediaSize.NA_LETTER);
            pBuilder.setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600));
            pBuilder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);

            //To display the preview window to user...
            PrintJob printJob = printManager.print(jobName, printAdapter, pBuilder.build());
            //end...
        }
    }

    // string to web
    private String stringToWeb(String input) {
        String formatted = "";
        if (input != null && !input.isEmpty()) {

            String para_open = "<p style=\"font-size:11pt; margin: 0px; padding: 0px;\">";
            String para_close = "</p>";
            formatted = para_open + Node.big_bullet + input.replaceAll("\n", para_close + para_open + Node.big_bullet) + para_close;
        }

        return formatted;
    }

    private void jsonBasedPrescTitle() {
        //Check for license key and load the correct config file
        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(Objects.requireNonNullElse(FileUtils.readFileRoot(CONFIG_FILE_NAME, this), String.valueOf(FileUtils.encodeJSON(this, CONFIG_FILE_NAME)))); //Load the config file
            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(this, mFileName)));
            }
            prescription1 = obj.getString("presciptionHeader1");

            prescription2 = obj.getString("presciptionHeader2");

            //For AFI we are not using Respiratory Value
            if (obj.getBoolean("mResp")) {
                isRespiratory = true;
            } else {
                isRespiratory = false;
            }

        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }
    // Print - end

    /**
     * This methods retrieves patient data from database.
     *
     * @param dataString variable of type String
     * @return void
     */

    public void queryData(String dataString) {
        String patientSelection = "uuid = ?";
        String[] patientArgs = {dataString};

        String table = "tbl_patient";
        String[] columnsToReturn = {"openmrs_id", "first_name", "middle_name", "last_name", "date_of_birth", "address1", "address2", "city_village", "state_province", "country", "postal_code", "phone_number", "gender", "sdw", "occupation", "patient_photo"};
        final Cursor idCursor = db.query(table, columnsToReturn, patientSelection, patientArgs, null, null, null);

        if (idCursor.moveToFirst()) {
            do {
                patient.setOpenmrs_id(idCursor.getString(idCursor.getColumnIndex("openmrs_id")));
                patient.setFirst_name(idCursor.getString(idCursor.getColumnIndex("first_name")));
                patient.setMiddle_name(idCursor.getString(idCursor.getColumnIndex("middle_name")));
                patient.setLast_name(idCursor.getString(idCursor.getColumnIndex("last_name")));
                patient.setDate_of_birth(idCursor.getString(idCursor.getColumnIndex("date_of_birth")));
                patient.setAddress1(idCursor.getString(idCursor.getColumnIndex("address1")));
                patient.setAddress2(idCursor.getString(idCursor.getColumnIndex("address2")));
                patient.setCity_village(idCursor.getString(idCursor.getColumnIndex("city_village")));
                patient.setState_province(idCursor.getString(idCursor.getColumnIndex("state_province")));
                patient.setCountry(idCursor.getString(idCursor.getColumnIndex("country")));
                patient.setPostal_code(idCursor.getString(idCursor.getColumnIndex("postal_code")));
                patient.setPhone_number(idCursor.getString(idCursor.getColumnIndex("phone_number")));
                patient.setGender(idCursor.getString(idCursor.getColumnIndex("gender")));
                patient.setSdw(idCursor.getString(idCursor.getColumnIndexOrThrow("sdw")));
                patient.setOccupation(idCursor.getString(idCursor.getColumnIndexOrThrow("occupation")));
                patient.setPatient_photo(idCursor.getString(idCursor.getColumnIndex("patient_photo")));
            } while (idCursor.moveToNext());
        }
        idCursor.close();

        PatientsDAO patientsDAO = new PatientsDAO();
        String patientSelection1 = "patientuuid = ?";
        String[] patientArgs1 = {patientUuid};
        String[] patientColumns1 = {"value", "person_attribute_type_uuid"};
        Cursor idCursor1 = db.query("tbl_patient_attribute", patientColumns1, patientSelection1, patientArgs1, null, null, null);
        String name = "";
        if (idCursor1.moveToFirst()) {
            do {
                try {
                    name = patientsDAO.getAttributesName(idCursor1.getString(idCursor1.getColumnIndexOrThrow("person_attribute_type_uuid")));
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }

                if (name.equalsIgnoreCase("caste")) {
                    patient.setCaste(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Telephone Number")) {
                    patient.setPhone_number(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Education Level")) {
                    patient.setEducation_level(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Economic Status")) {
                    patient.setEconomic_status(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("occupation")) {
                    patient.setOccupation(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("Son/wife/daughter")) {
                    patient.setSdw(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("NationalID")) {
                    patient.setNationalID(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }

            } while (idCursor1.moveToNext());
        }
        idCursor1.close();
        String[] columns = {"value", " conceptuuid"};

        try {
            String famHistSelection = "encounteruuid = ? AND conceptuuid = ?";
            String[] famHistArgs = {adultInitialUUID, UuidDictionary.RHK_FAMILY_HISTORY_BLURB};
            Cursor famHistCursor = db.query("tbl_obs", columns, famHistSelection, famHistArgs, null, null, null);
            famHistCursor.moveToLast();
            String famHistText = famHistCursor.getString(famHistCursor.getColumnIndexOrThrow("value"));
            famHistory.setValue(famHistText);
            famHistCursor.close();
        } catch (CursorIndexOutOfBoundsException e) {
            famHistory.setValue(""); // if family history does not exist
        }

        try {
            String medHistSelection = "encounteruuid = ? AND conceptuuid = ?";

            String[] medHistArgs = {adultInitialUUID, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB};

            Cursor medHistCursor = db.query("tbl_obs", columns, medHistSelection, medHistArgs, null, null, null);
            medHistCursor.moveToLast();
            String medHistText = medHistCursor.getString(medHistCursor.getColumnIndexOrThrow("value"));
            patHistory.setValue(medHistText);

            if (medHistText != null && !medHistText.isEmpty()) {

                medHistory = patHistory.getValue();


                medHistory = medHistory.replace("\"", "");
                medHistory = medHistory.replace("\n", "");
                do {
                    medHistory = medHistory.replace("  ", "");
                } while (medHistory.contains("  "));
            }
            medHistCursor.close();
        } catch (CursorIndexOutOfBoundsException e) {
            patHistory.setValue(""); // if medical history does not exist
        }

        //vitals display code
        String visitSelection = "encounteruuid = ? AND voided!='1'";
        String[] visitArgs = {vitalsUUID};
        if (vitalsUUID != null) {
            try {
                Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, null);
                if (visitCursor != null && visitCursor.moveToFirst()) {
                    do {
                        String dbConceptID = visitCursor.getString(visitCursor.getColumnIndex("conceptuuid"));
                        String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                        parseData(dbConceptID, dbValue);
                    } while (visitCursor.moveToNext());
                }
                if (visitCursor != null) {
                    visitCursor.close();
                }
            } catch (SQLException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }

        //adult intails display code
        String encounterselection = "encounteruuid = ? AND conceptuuid != ? AND conceptuuid != ? AND voided!='1'";
        String[] encounterargs = {adultInitialUUID, UuidDictionary.COMPLEX_IMAGE_AD, UuidDictionary.COMPLEX_IMAGE_PE};
        Cursor encountercursor = db.query("tbl_obs", columns, encounterselection, encounterargs, null, null, null);
        try {
            if (encountercursor != null && encountercursor.moveToFirst()) {
                do {
                    String dbConceptID = encountercursor.getString(encountercursor.getColumnIndex("conceptuuid"));
                    String dbValue = encountercursor.getString(encountercursor.getColumnIndex("value"));
                    parseData(dbConceptID, dbValue);
                } while (encountercursor.moveToNext());
            }
            if (encountercursor != null) {
                encountercursor.close();
            }
        } catch (SQLException sql) {
            FirebaseCrashlytics.getInstance().recordException(sql);
        }

        downloadPrescriptionDefault();
        downloadDoctorDetails();
    }

    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        CustomLog.d("TAG", "updateUIForInternetAvailability: ");
        if (isInternetAvailable) {
            refresh.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ui2_ic_internet_available));
        } else {
            refresh.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ui2_ic_no_internet));
        }
    }

    private String convertCtoF(String temperature) {
        String resultVal;
        NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
        double a = Double.parseDouble(temperature);
        double b = (a * 9 / 5) + 32;
        nf.format(b);
        double roundOff = Math.round(b * 100.0) / 100.0;
        resultVal = nf.format(roundOff);
        return resultVal;
    }

    public void editPatientInfo(View view) {
        PatientDTO patientDTO = new PatientDTO();
        String patientSelection = "uuid = ?";
        String[] patientArgs = {patientUuid};
        String[] patientColumns = {"uuid", "openmrs_id", "first_name", "middle_name", "last_name", "gender", "date_of_birth", "address1", "address2", "city_village", "state_province", "postal_code", "country", "phone_number", "gender", "sdw", "patient_photo"};
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

    private void setMedicationAdapter() {
        // Initialize RecyclerView
        mBinding.rvPrescribedMedicine.setLayoutManager(new LinearLayoutManager(this));

        // Initialize your data
        List<PrescribedMedicineModel> medicineList = getMedicationData1();
        // Add your prescribed medications to medicineList

        // Initialize adapter
        PrescribedMedicineAdapter adapter = new PrescribedMedicineAdapter(medicineList);

        // Set adapter to RecyclerView
        mBinding.rvPrescribedMedicine.setAdapter(adapter);
    }
}