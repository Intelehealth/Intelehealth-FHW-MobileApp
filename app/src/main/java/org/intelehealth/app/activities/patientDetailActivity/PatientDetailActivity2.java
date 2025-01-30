package org.intelehealth.app.activities.patientDetailActivity;

import static org.intelehealth.app.utilities.DialogUtils.patientRegistrationDialog;
import static org.intelehealth.app.utilities.StringUtils.en__as_dob;
import static org.intelehealth.app.utilities.StringUtils.en__bn_dob;
import static org.intelehealth.app.utilities.StringUtils.en__gu_dob;
import static org.intelehealth.app.utilities.StringUtils.en__hi_dob;
import static org.intelehealth.app.utilities.StringUtils.en__kn_dob;
import static org.intelehealth.app.utilities.StringUtils.en__ml_dob;
import static org.intelehealth.app.utilities.StringUtils.en__mr_dob;
import static org.intelehealth.app.utilities.StringUtils.en__or_dob;
import static org.intelehealth.app.utilities.StringUtils.en__ru_dob;
import static org.intelehealth.app.utilities.StringUtils.en__ta_dob;
import static org.intelehealth.app.utilities.StringUtils.en__te_dob;
import static org.intelehealth.app.utilities.StringUtils.switch_as_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_as_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_as_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_bn_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_bn_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_bn_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_gu_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_gu_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_gu_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_hi_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_hi_contact_type_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_hi_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_hi_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_hi_guardian_type_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_kn_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_kn_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_kn_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ml_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ml_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ml_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_mr_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_mr_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_mr_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_or_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_or_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_or_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ru_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ru_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ru_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ta_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ta_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_ta_education_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_te_caste_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_te_economic_edit;
import static org.intelehealth.app.utilities.StringUtils.switch_te_education_edit;

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
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.DisplayMetrics;

import org.intelehealth.app.models.dto.PatientAttributesDTO;
import org.intelehealth.app.utilities.CustomLog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.activities.identificationActivity.model.DistData;
import org.intelehealth.app.activities.identificationActivity.model.StateDistMaster;
import org.intelehealth.app.activities.searchPatientActivity.SearchPatientActivity_New;
import org.intelehealth.app.activities.visit.adapter.PastVisitListingAdapter;
import org.intelehealth.app.activities.visit.model.PastVisitData;
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.ayu.visit.VisitCreationActivity;
import org.intelehealth.app.ayu.visit.common.VisitUtils;
import org.intelehealth.app.ayu.visit.model.CommonVisitData;
import org.intelehealth.app.database.InteleHealthDatabaseHelper;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.VisitsDAO;
import org.intelehealth.app.databinding.ActivityPatientDetail2Binding;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.models.dto.VisitDTO;
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.ui.patient.activity.PatientRegistrationActivity;
import org.intelehealth.app.utilities.AgeUtils;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.DownloadFilesUtils;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.LanguageUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.NetworkUtils;
import org.intelehealth.app.utilities.PatientRegConfigKeys;
import org.intelehealth.app.utilities.PatientRegFieldsUtils;
import org.intelehealth.app.utilities.PatientRegStage;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.config.presenter.fields.data.RegFieldRepository;
import org.intelehealth.config.presenter.fields.factory.RegFieldViewModelFactory;
import org.intelehealth.config.presenter.fields.viewmodel.RegFieldViewModel;
import org.intelehealth.config.room.ConfigDatabase;
import org.intelehealth.config.room.entity.FeatureActiveStatus;
import org.intelehealth.config.room.entity.PatientRegistrationFields;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class PatientDetailActivity2 extends BaseActivity implements NetworkUtils.InternetCheckUpdateInterface {
    private static final String TAG = PatientDetailActivity2.class.getSimpleName();
    TextView name_txtview, openmrsID_txt, patientname, gender, patientdob, patientage, phone,
            postalcode, patientcountry, patientstate, patientdistrict, village, address1, addr2View,
            son_daughter_wife, patientoccupation, patientcaste, patienteducation, patienteconomicstatus, patientNationalID,
            guardina_name_tv, guardian_type_tv, contact_type_tv, em_contact_name_tv, em_contact_number_tv,
            tmh_case_number_tv, request_id_tv, relative_phone_num_tv, discipline_tv, department_tv,
            provinceTv, cityTv, registrationAddressOfHfTv, innTv, codeOfHealthFacilityTv, healthFacilityNameTv,
            codeOfDepartmentTv, departmentTv;

    TableRow nameTr, genderTr, dobTr, ageTr, phoneNumTr, guardianTypeTr, guardianNameTr,
            emContactNameTr, emContactTypeTr, emContactNumberTr, postalCodeTr, countryTr,
            stateTr, districtTr, villageCityTr, addressOneTr, addressTwoTr, nidTr, occupationTr, socialCategoryTr,
            educationTr, economicCategoryTr, tmhCaseNumberTr, requestIdTr, relativePhnNumTr, disciplineTr, departmentTr,
            provinceTr, cityTr, registrationAddressOfHfTr,
            innTr, codeOfHealthFacilityTr, healthFacilityNameTr, codeOfDepartmentTr;

    SessionManager sessionManager = null;
    //    Patient patientDTO = new Patient();
    PatientsDAO patientsDAO = new PatientsDAO();
    private boolean hasLicense = false;
    //SQLiteDatabase db = null;
    private PatientDTO patientDTO;
    String profileImage = "";
    String profileImage1 = "";
    Context context;
    String patientName, mGender;
    ImagesDAO imagesDAO = new ImagesDAO();
    float float_ageYear_Month;
    ImageView profile_image;
    LinearLayout personal_edit, address_edit, others_edit;
    Myreceiver reMyreceive;
    IntentFilter filter;
    Button startVisitBtn;
    EncounterDTO encounterDTO;
    ImageView cancelBtn;
    //private boolean returning;
    //private String encounterAdultIntials = "";
    //String phistory = "";

    String privacy_value_selected;
    String phistory = "";
    String fhistory = "";
    LinearLayout previousVisitsList;
    String visitValue;
    private String encounterVitals = "";
    private String encounterAdultIntials = "";
    private boolean returning;
    private ImageView refresh, cancelbtn;
    private NetworkUtils networkUtils;
    String tag = "";
    RegFieldViewModel regFieldViewModel;

    List<PatientRegistrationFields> patientAllFields;
    private ActivityPatientDetail2Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPatientDetail2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionManager = new SessionManager(this);
        String language = sessionManager.getAppLanguage();
        context = PatientDetailActivity2.this;

        networkUtils = new NetworkUtils(this, this);

        //config viewmodel initialization
        RegFieldRepository repository = new RegFieldRepository(ConfigDatabase.getInstance(this).patientRegFieldDao());
        RegFieldViewModelFactory factory = new RegFieldViewModelFactory(repository);
        regFieldViewModel = new ViewModelProvider(this, factory).get(RegFieldViewModel.class);

        //In case of crash still the org should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }

        //db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        filter = new IntentFilter("OpenmrsID");
        reMyreceive = new Myreceiver();


        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(Color.WHITE);

        Intent intent = getIntent();
        if (intent != null) {
            tag = intent.getStringExtra("tag");
            if (intent.hasExtra("BUNDLE")) {
                Bundle args = intent.getBundleExtra("BUNDLE");
                patientDTO = (PatientDTO) args.getSerializable("patientDTO");
            } else {
                patientDTO = new PatientDTO();
                patientDTO.setUuid(intent.getStringExtra("patientUuid"));
            }
            privacy_value_selected = intent.getStringExtra("privacy"); //intent value from IdentificationActivity.
        }

        initUI();


        personal_edit.setOnClickListener(v -> {
            PatientRegistrationActivity.startPatientRegistration(this, patientDTO.getUuid(), PatientRegStage.PERSONAL);
//            Intent intent2 = new Intent(PatientDetailActivity2.this, IdentificationActivity_New.class);
//            intent2.putExtra("patientUuid", patientDTO.getUuid());
//            intent2.putExtra("ScreenEdit", "personal_edit");
//            intent2.putExtra("patient_detail", true);
//            Bundle args = new Bundle();
//            args.putSerializable("patientDTO", (Serializable) patientDTO);
//            intent2.putExtra("BUNDLE", args);
//            startActivity(intent2);
            finish();
        });

        address_edit.setOnClickListener(v -> {
            PatientRegistrationActivity.startPatientRegistration(this, patientDTO.getUuid(), PatientRegStage.ADDRESS);
//            Intent intent2 = new Intent(PatientDetailActivity2.this, IdentificationActivity_New.class);
//            intent2.putExtra("patientUuid", patientDTO.getUuid());
//            intent2.putExtra("ScreenEdit", "address_edit");
//            intent2.putExtra("patient_detail", true);
//            Bundle args = new Bundle();
//            args.putSerializable("patientDTO", (Serializable) patientDTO);
//            intent2.putExtra("BUNDLE", args);
//            startActivity(intent2);
            finish();
        });

        others_edit.setOnClickListener(v -> {
            PatientRegistrationActivity.startPatientRegistration(this, patientDTO.getUuid(), PatientRegStage.OTHER);
//            Intent intent2 = new Intent(PatientDetailActivity2.this, IdentificationActivity_New.class);
//            intent2.putExtra("patientUuid", patientDTO.getUuid());
//            intent2.putExtra("ScreenEdit", "others_edit");
//            intent2.putExtra("patient_detail", true);
//            Bundle args = new Bundle();
//            args.putSerializable("patientDTO", (Serializable) patientDTO);
//            intent2.putExtra("BUNDLE", args);
//            startActivity(intent2);
            finish();
        });

        cancelbtn.setOnClickListener(v -> {
            Intent i = new Intent(PatientDetailActivity2.this, HomeScreenActivity_New.class);
            startActivity(i);
            finish();
        });

        startVisitBtn.setOnClickListener(v -> {
            patientRegistrationDialog(context,
                    ContextCompat.getDrawable(this, R.drawable.dialog_icon_complete),
                    getResources().getString(R.string.patient_registered),
                    getResources().getString(R.string.does_patient_start_visit_now),
                    getResources().getString(R.string.button_continue),
                    getResources().getString(R.string.cancel), new DialogUtils.CustomDialogListener() {
                        @Override
                        public void onDialogActionDone(int action) {
                            if (action == DialogUtils.CustomDialogListener.POSITIVE_CLICK) {
                                Intent in = new Intent(PatientDetailActivity2.this, TeleconsultationConsentActivity.class);
                                CommonVisitData commonVisitData = new CommonVisitData();
                                commonVisitData.setPatientUuid(patientDTO.getUuid());
                                commonVisitData.setPrivacyNote(privacy_value_selected);
                                in.putExtra("CommonVisitData", commonVisitData);
                                startActivity(in);
                                // startVisit();
                                // mStartForConsentApproveResult.launch(new Intent(PatientDetailActivity2.this, TeleconsultationConsentActivity.class));
                            }
                        }
                    });
          /*  startVisitDialog(PatientDetailActivity2.this, getResources().getDrawable(R.drawable.dialog_visit_sent_success_icon),
                    "ssss", "swwwww", "yes", "no");*/ // todo: added jsut for testing purposes...
        });

        mPersonalHeaderRelativeLayout = findViewById(R.id.relative_personal_header);
        mAddressHeaderRelativeLayout = findViewById(R.id.relative_address_header);
        mOthersHeaderRelativeLayout = findViewById(R.id.relative_others_header);
        mPersonalHeaderRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RelativeLayout extraRelativeLayout = findViewById(R.id.ll_personal_extra);
                ImageView imageView = findViewById(R.id.iv_personal_drop);
                if (extraRelativeLayout.getVisibility() == View.VISIBLE) {
                    extraRelativeLayout.setVisibility(View.GONE);
                    imageView.setRotation(180);
                } else {
                    extraRelativeLayout.setVisibility(View.VISIBLE);
                    imageView.setRotation(0);
                }
            }
        });
        mAddressHeaderRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RelativeLayout extraRelativeLayout = findViewById(R.id.ll_address_extra);
                ImageView imageView = findViewById(R.id.iv_address_drop);
                if (extraRelativeLayout.getVisibility() == View.VISIBLE) {
                    extraRelativeLayout.setVisibility(View.GONE);
                    imageView.setRotation(180);
                } else {
                    extraRelativeLayout.setVisibility(View.VISIBLE);
                    imageView.setRotation(0);
                }
            }
        });
        mOthersHeaderRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RelativeLayout extraRelativeLayout = findViewById(R.id.ll_others_extra);
                ImageView imageView = findViewById(R.id.iv_others_drop);
                if (extraRelativeLayout.getVisibility() == View.VISIBLE) {
                    extraRelativeLayout.setVisibility(View.GONE);
                    imageView.setRotation(180);
                } else {
                    extraRelativeLayout.setVisibility(View.VISIBLE);
                    imageView.setRotation(0);
                }
            }
        });

        syncAnimator = ObjectAnimator.ofFloat(refresh, View.ROTATION, 0f, 359f).setDuration(1200);
        syncAnimator.setRepeatCount(ValueAnimator.INFINITE);
        syncAnimator.setInterpolator(new LinearInterpolator());

        cancelbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    private BroadcastReceiver mBroadcastReceiver;
    private ObjectAnimator syncAnimator;


    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    @Override
    protected void onResume() {
        super.onResume();
        setDisplay(patientDTO.getUuid());
    }

    private RelativeLayout mPersonalHeaderRelativeLayout, mAddressHeaderRelativeLayout, mOthersHeaderRelativeLayout;

    ActivityResultLauncher<Intent> mStartForConsentApproveResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == AppConstants.TELECONSULTATION_CONSENT_ACCEPT) {
                        //Intent intent = result.getData();
                        // Handle the Intent
                        startVisit();
                    }
                }
            });

    private void startVisit() {
        // before starting, we determine if it is new visit for a returning patient
        // extract both FH and PMH
        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);
        Date todayDate = new Date();
        String thisDate = currentDate.format(todayDate);


        String uuid = UUID.randomUUID().toString();
        EncounterDAO encounterDAO = new EncounterDAO();
        EncounterDTO encounterDTO = new EncounterDTO();
        encounterDTO.setUuid(UUID.randomUUID().toString());
        encounterDTO.setEncounterTypeUuid(encounterDAO.getEncounterTypeUuid("ENCOUNTER_VITALS"));
        encounterDTO.setEncounterTime(thisDate);
        encounterDTO.setVisituuid(uuid);
        encounterDTO.setSyncd(false);
        encounterDTO.setProvideruuid(sessionManager.getProviderID());
        CustomLog.d("DTO", "DTO:detail " + encounterDTO.getProvideruuid());
        encounterDTO.setVoided(0);
        encounterDTO.setPrivacynotice_value(privacy_value_selected);//privacy value added.

        try {
            encounterDAO.createEncountersToDB(encounterDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            CustomLog.e(TAG, e.getMessage());
        }

        InteleHealthDatabaseHelper mDatabaseHelper = new InteleHealthDatabaseHelper(PatientDetailActivity2.this);
        SQLiteDatabase sqLiteDatabase = mDatabaseHelper.getReadableDatabase();

        String CREATOR_ID = sessionManager.getCreatorID();
        returning = false;
        sessionManager.setReturning(returning);

        String[] cols = {"value"};
        Cursor cursor = sqLiteDatabase.query("tbl_obs", cols, "encounteruuid=? and conceptuuid=?",// querying for PMH (Past Medical History)
                new String[]{encounterAdultIntials, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB},
                null, null, null);

        if (cursor.moveToFirst()) {
            // rows present
            do {
                // so that null data is not appended
                phistory = phistory + cursor.getString(0);

            }
            while (cursor.moveToNext());
            returning = true;
            sessionManager.setReturning(returning);
        }
        cursor.close();

        Intent intent2 = new Intent(PatientDetailActivity2.this, VisitCreationActivity.class);
        String fullName = patientDTO.getFirstname() + " " + patientDTO.getLastname();
        String patientUuid = patientDTO.getUuid();
        intent2.putExtra("patientUuid", patientUuid);
        VisitDTO visitDTO = new VisitDTO();
        visitDTO.setUuid(uuid);
        visitDTO.setPatientuuid(patientDTO.getUuid());
        visitDTO.setStartdate(thisDate);
        visitDTO.setVisitTypeUuid(UuidDictionary.VISIT_TELEMEDICINE);
        visitDTO.setLocationuuid(sessionManager.getLocationUuid());
        visitDTO.setSyncd(false);
        visitDTO.setCreatoruuid(sessionManager.getCreatorID());//static
        VisitsDAO visitsDAO = new VisitsDAO();

        try {
            visitsDAO.insertPatientToDB(visitDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            CustomLog.e(TAG, e.getMessage());
        }

        // visitUuid = String.valueOf(visitLong);
//                localdb.close();
        intent2.putExtra("patientUuid", patientUuid);
        intent2.putExtra("visitUuid", uuid);
        intent2.putExtra("encounterUuidVitals", encounterDTO.getUuid());
        intent2.putExtra("encounterUuidAdultIntial", "");
        intent2.putExtra("EncounterAdultInitial_LatestVisit", encounterAdultIntials);
        intent2.putExtra("name", fullName);
        intent2.putExtra("gender", mGender);
        intent2.putExtra("tag", "new");
        intent2.putExtra("float_ageYear_Month", float_ageYear_Month);
        startActivity(intent2);
        finish();
    }


    private void initUI() {
        refresh = findViewById(R.id.refresh);
        cancelbtn = findViewById(R.id.cancelbtn);

        profile_image = findViewById(R.id.profile_image);
        name_txtview = findViewById(R.id.name_txtview);
        openmrsID_txt = findViewById(R.id.openmrsID_txt);

        patientname = findViewById(R.id.name);
        gender = findViewById(R.id.gender);
        patientdob = findViewById(R.id.dob);
        patientage = findViewById(R.id.age);
        phone = findViewById(R.id.phone);

        guardian_type_tv = findViewById(R.id.guardian_type_tv);
        guardina_name_tv = findViewById(R.id.guardian_name_tv);
        contact_type_tv = findViewById(R.id.contact_type_tv);
        em_contact_name_tv = findViewById(R.id.em_contact_name_tv);
        em_contact_number_tv = findViewById(R.id.em_contact_number_tv);

        tmh_case_number_tv = findViewById(R.id.tmh_case_number);
        request_id_tv = findViewById(R.id.request_id);
        relative_phone_num_tv = findViewById(R.id.relative_phn_number);
        discipline_tv = findViewById(R.id.discipline);
        department_tv = findViewById(R.id.department);

        provinceTv = findViewById(R.id.province);
        cityTv = findViewById(R.id.city);
        registrationAddressOfHfTv = findViewById(R.id.registrationAddressOfHf);

        innTv = findViewById(R.id.inn);
        codeOfHealthFacilityTv = findViewById(R.id.code_of_healthy_facility);
        healthFacilityNameTv = findViewById(R.id.health_facility_name);
        codeOfDepartmentTv = findViewById(R.id.code_of_department);
        departmentTv = findViewById(R.id.department);

        nameTr = findViewById(R.id.name_tr);
        genderTr = findViewById(R.id.gender_tr);
        dobTr = findViewById(R.id.dob_tr);
        ageTr = findViewById(R.id.age_tr);
        phoneNumTr = findViewById(R.id.phone_num_tr);
        guardianTypeTr = findViewById(R.id.guardian_type_table_row);
        guardianNameTr = findViewById(R.id.guardian_name_table_row);
        emContactNameTr = findViewById(R.id.em_contact_name_tr);
        emContactNumberTr = findViewById(R.id.em_contact_num_tr);
        emContactTypeTr = findViewById(R.id.contact_type_tr);

        postalCodeTr = findViewById(R.id.postal_code_tr);
        countryTr = findViewById(R.id.country_tr);
        stateTr = findViewById(R.id.state_tr);
        districtTr = findViewById(R.id.district_tr);
        villageCityTr = findViewById(R.id.village_city_tr);
        guardianTypeTr = findViewById(R.id.guardian_type_table_row);
        addressOneTr = findViewById(R.id.address1_tr);
        addressTwoTr = findViewById(R.id.tr_address_2);

        nidTr = findViewById(R.id.nid_tr);
        occupationTr = findViewById(R.id.occupation_tr);
        socialCategoryTr = findViewById(R.id.social_category_tr);
        educationTr = findViewById(R.id.education_tr);
        economicCategoryTr = findViewById(R.id.economic_category_tr);

        tmhCaseNumberTr = findViewById(R.id.tmh_case_number_tr);
        requestIdTr = findViewById(R.id.request_id_tr);
        relativePhnNumTr = findViewById(R.id.relative_phone_number_tr);
        disciplineTr = findViewById(R.id.discipline_tr);
        departmentTr = findViewById(R.id.department_tr);

        innTr = findViewById(R.id.inn_tr);
        codeOfHealthFacilityTr = findViewById(R.id.code_of_healthy_facility_tr);
        healthFacilityNameTr = findViewById(R.id.health_facility_name_tr);
        codeOfDepartmentTr = findViewById(R.id.code_of_department_tr);
        departmentTr = findViewById(R.id.department_tr);

        postalcode = findViewById(R.id.postalcode);
        patientcountry = findViewById(R.id.country);
        patientstate = findViewById(R.id.state);
        patientdistrict = findViewById(R.id.district);
        village = findViewById(R.id.village);
        address1 = findViewById(R.id.address1);
        addr2View = findViewById(R.id.addr2View);

        provinceTr = findViewById(R.id.province_tr);
        cityTr = findViewById(R.id.city_tr);
        registrationAddressOfHfTr = findViewById(R.id.registrationAddressOfHf_tr);

        son_daughter_wife = findViewById(R.id.son_daughter_wife);
        patientNationalID = findViewById(R.id.national_ID);
        patientoccupation = findViewById(R.id.occupation);
        patientcaste = findViewById(R.id.caste);
        patienteducation = findViewById(R.id.education);
        patienteconomicstatus = findViewById(R.id.economicstatus);

        personal_edit = findViewById(R.id.personal_edit);
        address_edit = findViewById(R.id.address_edit);
        others_edit = findViewById(R.id.others_edit);
        cancelbtn = findViewById(R.id.cancelbtn);

        startVisitBtn = findViewById(R.id.startVisitBtn);

        mCurrentVisitsRecyclerView = findViewById(R.id.rcv_open_visits);
        mCurrentVisitsRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        mPastVisitsRecyclerView = findViewById(R.id.rcv_past_visits);
        mPastVisitsRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        fetchAllConfig();

        setFullName();
        initForOpenVisit();
        initForPastVisit();
    }

    /**
     * fetching reg config from local db
     */
    private void fetchAllConfig() {
        regFieldViewModel.fetchEnabledAllRegFields()
                .observe(this, it -> {
                            patientAllFields = it;
                            configAllFields();
                        }
                );
    }

    /**
     * changing fields status based on config data
     */
    private void configAllFields() {
        String[] ymdData = DateAndTimeUtils.getAgeInYearMonth(patientDTO.getDateofbirth()).split(" ");
        boolean isGuardianRequire = false;
        if (ymdData.length > 2) {
            int mAgeYears = ymdData[0] != null && !ymdData[0].isEmpty() ? Integer.parseInt(ymdData[0]) : 0;
            int mAgeMonths = ymdData[1] != null && !ymdData[1].isEmpty() ? Integer.parseInt(ymdData[1]) : 0;
            int mAgeDays = ymdData[2] != null && !ymdData[2].isEmpty() ? Integer.parseInt(ymdData[2]) : 0;
            isGuardianRequire = AgeUtils.INSTANCE.isGuardianRequired(mAgeYears, mAgeMonths, mAgeDays);
        }

        for (PatientRegistrationFields fields : patientAllFields) {
            switch (fields.getIdKey()) {
                case PatientRegConfigKeys.GENDER -> PatientRegFieldsUtils.INSTANCE.configField(
                        false,
                        fields,
                        genderTr,
                        null,
                        null,
                        null
                );
                case PatientRegConfigKeys.DOB -> PatientRegFieldsUtils.INSTANCE.configField(
                        false,
                        fields,
                        dobTr,
                        null,
                        null,
                        null
                );
                case PatientRegConfigKeys.AGE -> PatientRegFieldsUtils.INSTANCE.configField(
                        false,
                        fields,
                        ageTr,
                        null,
                        null,
                        null
                );
                case PatientRegConfigKeys.GUARDIAN_TYPE -> {
                    if (isGuardianRequire) {
                        PatientRegFieldsUtils.INSTANCE.configField(
                                false,
                                fields,
                                guardianTypeTr,
                                null,
                                null,
                                null
                        );
                    }
                }
                case PatientRegConfigKeys.GUARDIAN_NAME -> {
                    if (isGuardianRequire) {
                        PatientRegFieldsUtils.INSTANCE.configField(
                                false,
                                fields,
                                guardianNameTr,
                                null,
                                null,
                                null
                        );
                    }
                }
                case PatientRegConfigKeys.PHONE_NUM -> PatientRegFieldsUtils.INSTANCE.configField(
                        false,
                        fields,
                        phoneNumTr,
                        null,
                        null,
                        null
                );
                case PatientRegConfigKeys.EM_CONTACT_TYPE ->
                        PatientRegFieldsUtils.INSTANCE.configField(
                                false,
                                fields,
                                emContactTypeTr,
                                null,
                                null,
                                null
                        );
                case PatientRegConfigKeys.EM_CONTACT_NAME ->
                        PatientRegFieldsUtils.INSTANCE.configField(
                                false,
                                fields,
                                emContactNameTr,
                                null,
                                null,
                                null
                        );
                case PatientRegConfigKeys.EM_CONTACT_NUMBER ->
                        PatientRegFieldsUtils.INSTANCE.configField(
                                false,
                                fields,
                                emContactNumberTr,
                                null,
                                null,
                                null
                        );

                case PatientRegConfigKeys.POSTAL_CODE -> PatientRegFieldsUtils.INSTANCE.configField(
                        false,
                        fields,
                        postalCodeTr,
                        null,
                        null,
                        null
                );
                case PatientRegConfigKeys.COUNTRY -> {
                    PatientRegFieldsUtils.INSTANCE.configField(
                            false,
                            fields,
                            countryTr,
                            null,
                            null,
                            null
                    );
                }
                case PatientRegConfigKeys.STATE -> PatientRegFieldsUtils.INSTANCE.configField(
                        false,
                        fields,
                        stateTr,
                        null,
                        null,
                        null
                );
                case PatientRegConfigKeys.DISTRICT -> PatientRegFieldsUtils.INSTANCE.configField(
                        false,
                        fields,
                        districtTr,
                        null,
                        null,
                        null
                );
                case PatientRegConfigKeys.VILLAGE_TOWN_CITY -> {
                    PatientRegFieldsUtils.INSTANCE.configField(
                            false,
                            fields,
                            villageCityTr,
                            null,
                            null,
                            null
                    );
                }
                case PatientRegConfigKeys.ADDRESS_1 -> PatientRegFieldsUtils.INSTANCE.configField(
                        false,
                        fields,
                        addressOneTr,
                        null,
                        null,
                        null
                );
                case PatientRegConfigKeys.ADDRESS_2 -> PatientRegFieldsUtils.INSTANCE.configField(
                        false,
                        fields,
                        addressTwoTr,
                        null,
                        null,
                        null
                );

                case PatientRegConfigKeys.NATIONAL_ID -> {
                    PatientRegFieldsUtils.INSTANCE.configField(
                            false,
                            fields,
                            nidTr,
                            null,
                            null,
                            null
                    );
                }
                case PatientRegConfigKeys.OCCUPATION -> PatientRegFieldsUtils.INSTANCE.configField(
                        false,
                        fields,
                        occupationTr,
                        null,
                        null,
                        null
                );
                case PatientRegConfigKeys.SOCIAL_CATEGORY ->
                        PatientRegFieldsUtils.INSTANCE.configField(
                                false,
                                fields,
                                socialCategoryTr,
                                null,
                                null,
                                null
                        );
                case PatientRegConfigKeys.EDUCATION -> PatientRegFieldsUtils.INSTANCE.configField(
                        false,
                        fields,
                        educationTr,
                        null,
                        null,
                        null
                );

                case PatientRegConfigKeys.ECONOMIC_CATEGORY ->
                        PatientRegFieldsUtils.INSTANCE.configField(
                                false,
                                fields,
                                economicCategoryTr,
                                null,
                                null,
                                null
                        );

                case PatientRegConfigKeys.TMH_CASE_SUMMARY ->
                        PatientRegFieldsUtils.INSTANCE.configField(
                                false,
                                fields,
                                tmhCaseNumberTr,
                                null,
                                null,
                                null
                        );

                case PatientRegConfigKeys.REQUEST_ID -> PatientRegFieldsUtils.INSTANCE.configField(
                        false,
                        fields,
                        requestIdTr,
                        null,
                        null,
                        null
                );

                case PatientRegConfigKeys.RELATIVE_PHONE_NUM ->
                        PatientRegFieldsUtils.INSTANCE.configField(
                                false,
                                fields,
                                relativePhnNumTr,
                                null,
                                null,
                                null
                        );

                case PatientRegConfigKeys.DISCIPLINE -> PatientRegFieldsUtils.INSTANCE.configField(
                        false,
                        fields,
                        disciplineTr,
                        null,
                        null,
                        null
                );

                case PatientRegConfigKeys.DEPARTMENT -> PatientRegFieldsUtils.INSTANCE.configField(
                        false,
                        fields,
                        departmentTr,
                        null,
                        null,
                        null
                );

                case PatientRegConfigKeys.PROVINCES -> PatientRegFieldsUtils.INSTANCE.configField(
                        false,
                        fields,
                        provinceTr,
                        null,
                        null,
                        null
                );

                case PatientRegConfigKeys.CITIES -> PatientRegFieldsUtils.INSTANCE.configField(
                        false,
                        fields,
                        cityTr,
                        null,
                        null,
                        null
                );

                case PatientRegConfigKeys.REGISTRATION_ADDRESS_OF_HF ->
                        PatientRegFieldsUtils.INSTANCE.configField(
                                false,
                                fields,
                                registrationAddressOfHfTr,
                                null,
                                null,
                                null
                        );

                case PatientRegConfigKeys.INN -> PatientRegFieldsUtils.INSTANCE.configField(
                        false,
                        fields,
                        innTr,
                        null,
                        null,
                        null
                );

                case PatientRegConfigKeys.CODE_OF_HEALTHY_FACILITY ->
                        PatientRegFieldsUtils.INSTANCE.configField(
                                false,
                                fields,
                                codeOfHealthFacilityTr,
                                null,
                                null,
                                null
                        );

                case PatientRegConfigKeys.HEALTH_FACILITY_NAME ->
                        PatientRegFieldsUtils.INSTANCE.configField(
                                false,
                                fields,
                                healthFacilityNameTr,
                                null,
                                null,
                                null
                        );

                case PatientRegConfigKeys.CODE_OF_DEPARTMENT ->
                        PatientRegFieldsUtils.INSTANCE.configField(
                                false,
                                fields,
                                codeOfDepartmentTr,
                                null,
                                null,
                                null
                        );

            }
        }
    }


    private RecyclerView mPastVisitsRecyclerView;
    private List<PastVisitData> mPastVisitDataList = new ArrayList<PastVisitData>();

    private RecyclerView mCurrentVisitsRecyclerView;
    private List<PastVisitData> mCurrentVisitDataList = new ArrayList<PastVisitData>();

    private void initForOpenVisit() {
        if (patientDTO == null || patientDTO.getUuid() == null) {
            return;
        }
        mCurrentVisitDataList.clear();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        String visitSelection = "patientuuid = ?";
        String[] visitArgs = {patientDTO.getUuid()};
        String[] visitColumns = {"uuid", "startdate", "enddate"};
        String visitOrderBy = "startdate";
        Cursor visitCursor = db.query("tbl_visit", visitColumns, visitSelection, visitArgs, null, null, visitOrderBy);
        //if (visitCursor == null || visitCursor.getCount() <= 0) {
        //     findViewById(R.id.cv_open_visits).setVisibility(View.GONE);
        //    startVisitBtn.setVisibility(View.VISIBLE);
        //} else {
        //   findViewById(R.id.cv_open_visits).setVisibility(View.VISIBLE);
        //   startVisitBtn.setVisibility(View.GONE);
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
                    CustomLog.e(TAG, e.getMessage());
                }
                if (!isCompletedExitedSurvey) {

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
                                CustomLog.e(TAG, e.getMessage());
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
                                        if (!comp.trim().isEmpty() && comp.indexOf(colon) > 0) {
                                            visitValue = visitValue + Node.bullet_arrow + comp.substring(0, comp.indexOf(colon)) + "<br/>";
                                        }
                                    }
                                    if (!visitValue.isEmpty()) {
                                        visitValue = visitValue.replaceAll(Node.bullet_arrow, "");
                                        visitValue = visitValue.replaceAll("<br/>", ", ");
                                        visitValue = visitValue.replaceAll(Node.ASSOCIATE_SYMPTOMS, "");
                                        //visitValue = visitValue.substring(0, visitValue.length() - 2);
                                        visitValue = visitValue.replaceAll("<b>", "");
                                        visitValue = visitValue.replaceAll("</b>", "");
                                        visitValue = visitValue.trim();
                                        while (visitValue.endsWith(",")) {
                                            visitValue = visitValue.substring(0, visitValue.length() - 1).trim();
                                        }
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

                                StringBuilder stringBuilder = new StringBuilder();
                                for (String s : spt) {
                                    String complainName = "";
                                    if (s.isEmpty()) continue;
                                    //String s1 =  new String(s.getBytes(), "UTF-8");
                                    System.out.println(s);
                                    String[] spt1 = s.split("::●");
                                    complainName = spt1[0];

                                    //if (s.trim().startsWith(getTranslatedAssociatedSymptomQString(lCode))) {
                                    if (!complainName.trim().contains(VisitUtils.getTranslatedPatientDenies(sessionManager.getAppLanguage()))) {
                                        System.out.println(complainName);
                                        if (!stringBuilder.toString().isEmpty())
                                            stringBuilder.append(", ");
                                        stringBuilder.append(complainName);
                                    }

                                }
                                /*StringBuilder stringBuilder = new StringBuilder();
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
                                    }*/
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
                                mCurrentVisitDataList.add(pastVisitData);
                                CustomLog.v(TAG, new Gson().toJson(mCurrentVisitDataList));

                            } catch (ParseException e) {
                                FirebaseCrashlytics.getInstance().recordException(e);
                                CustomLog.e(TAG, e.getMessage());
                            }
                        }
                    }


                }
            } while (visitCursor.moveToPrevious());
        }
        CustomLog.v(TAG, "initForOpenVisit - " + new Gson().toJson(mCurrentVisitDataList));
        if (!mCurrentVisitDataList.isEmpty()) {
            PastVisitListingAdapter pastVisitListingAdapter = new PastVisitListingAdapter(mCurrentVisitsRecyclerView, PatientDetailActivity2.this, mCurrentVisitDataList, new PastVisitListingAdapter.OnItemSelected() {
                @Override
                public void onItemSelected(PastVisitData pastVisitData) {
                    intentForVisitDetails(pastVisitData);
                }
            });
            mCurrentVisitsRecyclerView.setAdapter(pastVisitListingAdapter);
        }

        if (mCurrentVisitDataList.isEmpty()) {
            findViewById(R.id.cv_open_visits).setVisibility(View.GONE);
            startVisitBtn.setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.cv_open_visits).setVisibility(View.VISIBLE);
            startVisitBtn.setVisibility(View.GONE);
        }
        // }
    }

    /**
     * @param pastVisitData
     */
    private void intentForVisitDetails(PastVisitData pastVisitData) {
        Intent in = new Intent(PatientDetailActivity2.this, VisitSummaryActivity_New.class);
        in.putExtra("patientUuid", patientDTO.getUuid());
        in.putExtra("visitUuid", pastVisitData.getVisitUUID());
        in.putExtra("gender", mGender);
        in.putExtra("name", patientName);
        in.putExtra("encounterUuidVitals", pastVisitData.getEncounterVitals());
        in.putExtra("encounterUuidAdultIntial", pastVisitData.getEncounterAdultInitial());
        in.putExtra("float_ageYear_Month", float_ageYear_Month);
        in.putExtra("tag", "VisitDetailsActivity");
        startActivity(in);
    }

    /**
     * set patient full name here
     */
    private void setFullName() {
        if (patientDTO.getMiddlename() == null) {
            patientName = patientDTO.getFirstname() + " " + patientDTO.getLastname();
        } else {
            patientName = patientDTO.getFirstname() + " " + patientDTO.getMiddlename() + " " + patientDTO.getLastname();
        }
    }

    public void setDisplay(String dataString) {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        patientDTO = new PatientDTO();
        String patientSelection = "uuid = ?";
        String[] patientArgs = {dataString};
        String[] patientColumns = {"uuid", "openmrs_id", "first_name", "middle_name", "last_name", "gender",
                "date_of_birth", "address1", "address2", "city_village", "state_province",
                "postal_code", "country", "phone_number", "gender", "sdw",
                "patient_photo", "guardian_type", "guardian_name", "contact_type", "em_contact_name", "em_contact_num"};
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

                patientDTO.setGuardianType(idCursor.getString(idCursor.getColumnIndexOrThrow("guardian_type")));
                patientDTO.setGuardianName(idCursor.getString(idCursor.getColumnIndexOrThrow("guardian_name")));
                patientDTO.setContactType(idCursor.getString(idCursor.getColumnIndexOrThrow("contact_type")));
                patientDTO.setEmContactName(idCursor.getString(idCursor.getColumnIndexOrThrow("em_contact_name")));
                patientDTO.setEmContactNumber(idCursor.getString(idCursor.getColumnIndexOrThrow("em_contact_num")));
            } while (idCursor.moveToNext());
        }
        idCursor.close();

        String patientSelection1 = "patientuuid = ?";
        String[] patientArgs1 = {dataString};
        String[] patientColumns1 = {"value", "person_attribute_type_uuid"};
        Cursor idCursor1 = db.query("tbl_patient_attribute", patientColumns1, patientSelection1, patientArgs1, null, null, null);
        String name = "";
        if (idCursor1.moveToFirst()) {
            do {
                try {
                    name = patientsDAO.getAttributesName(idCursor1.getString(idCursor1.getColumnIndexOrThrow("person_attribute_type_uuid")));
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    CustomLog.e(TAG, e.getMessage());
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
                if (name.equalsIgnoreCase("NationalID")) {
                    patientDTO.setNationalID(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("ProfileImageTimestamp")) {
                    profileImage1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"));
                }
                if (name.equalsIgnoreCase("createdDate")) {
                    patientDTO.setCreatedDate(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }
                if (name.equalsIgnoreCase("providerUUID")) {
                    patientDTO.setProviderUUID(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }

                if (name.equalsIgnoreCase(PatientAttributesDTO.Column.TMH_CASE_NUMBER.value)) {
                    patientDTO.setTmhCaseNumber(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }

                if (name.equalsIgnoreCase(PatientAttributesDTO.Column.REQUEST_ID.value)) {
                    patientDTO.setRequestId(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }

                if (name.equalsIgnoreCase(PatientAttributesDTO.Column.RELATIVE_PHONE_NUMBER.value)) {
                    patientDTO.setRelativePhoneNumber(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }

                if (name.equalsIgnoreCase(PatientAttributesDTO.Column.DISCIPLINE.value)) {
                    patientDTO.setDiscipline(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }

                if (name.equalsIgnoreCase(PatientAttributesDTO.Column.DEPARTMENT.value)) {
                    patientDTO.setDepartment(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }

                if (name.equalsIgnoreCase(PatientAttributesDTO.Column.PROVINCES.value)) {
                    patientDTO.setProvince(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }

                if (name.equalsIgnoreCase(PatientAttributesDTO.Column.CITIES.value)) {
                    patientDTO.setCity(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }

                if (name.equalsIgnoreCase(PatientAttributesDTO.Column.REGISTRATION_ADDRESS_OF_HF.value)) {
                    patientDTO.setRegistrationAddressOfHf(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }

                if (name.equalsIgnoreCase(PatientAttributesDTO.Column.INN.value)) {
                    patientDTO.setInn(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }

                if (name.equalsIgnoreCase(PatientAttributesDTO.Column.CODE_OF_HEALTH_FACILITY.value)) {
                    patientDTO.setCodeOfHealthFacility(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }

                if (name.equalsIgnoreCase(PatientAttributesDTO.Column.HEALTH_FACILITY_NAME.value)) {
                    patientDTO.setHealthFacilityName(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }

                if (name.equalsIgnoreCase(PatientAttributesDTO.Column.CODE_OF_DEPARTMENT.value)) {
                    patientDTO.setCodeOfDepartment(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }

                if (name.equalsIgnoreCase(PatientAttributesDTO.Column.DEPARTMENT.value)) {
                    patientDTO.setDepartment(idCursor1.getString(idCursor1.getColumnIndexOrThrow("value")));
                }

            } while (idCursor1.moveToNext());
        }
        idCursor1.close();

        if (!sessionManager.getLicenseKey().isEmpty()) {
            hasLicense = true;
        }

        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(Objects.requireNonNullElse
                        (FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, context),
                                String.valueOf(FileUtils.encodeJSON(context, AppConstants.CONFIG_FILE_NAME)))); //Load the config file
            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(this, AppConstants.CONFIG_FILE_NAME)));
            }

            //Display the fields on the Add Patient screen as per the config file
            // todo: uncomment later and hadnle this case.
         /*   if (obj.getBoolean("casteLayout")) {
                casteRow.setVisibility(View.VISIBLE);
            } else {
                casteRow.setVisibility(View.GONE);
            }
            if (obj.getBoolean("educationLayout")) {
                educationRow.setVisibility(View.VISIBLE);
            } else {
                educationRow.setVisibility(View.GONE);
            }
            if (obj.getBoolean("economicLayout")) {
                economicRow.setVisibility(View.VISIBLE);
            } else {
                economicRow.setVisibility(View.GONE);
            }
*/
        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            CustomLog.e(TAG, e.getMessage());
//            Issue #627
//            added the catch exception to check the config and throwing back to setup activity
            Toast.makeText(getApplicationContext(), "JsonException" + e, Toast.LENGTH_LONG).show();
        }

        //changing patient to patientDTO object
        if (patientDTO.getMiddlename() == null) {
            patientName = patientDTO.getFirstname() + " " + patientDTO.getLastname();
        } else {
            patientName = patientDTO.getFirstname() + " " + patientDTO.getMiddlename() + " " + patientDTO.getLastname();
        }

        // setting patient name to the name textviews.
        name_txtview.setText(patientName);
        patientname.setText(patientName);


        // setting profile image of patient
        try {
            profileImage = imagesDAO.getPatientProfileChangeTime(patientDTO.getUuid());
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            CustomLog.e(TAG, e.getMessage());
        }

        if (patientDTO.getPatientPhoto() == null || patientDTO.getPatientPhoto().equalsIgnoreCase("")) {
            if (NetworkConnection.isOnline(getApplication())) {
                profilePicDownloaded();
            }
        }
        if (!profileImage.equalsIgnoreCase(profileImage1)) {
            if (NetworkConnection.isOnline(getApplication())) {
                profilePicDownloaded();
            }
        }
        RequestBuilder<Drawable> requestBuilder = Glide.with(this)
                .asDrawable().sizeMultiplier(0.3f);

        Glide.with(this)
                .load(patientDTO.getPatientPhoto())
                .thumbnail(requestBuilder)
                .centerCrop()
                .error(R.drawable.avatar1)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(profile_image);

        // setting openmrs id
        if (patientDTO.getOpenmrsId() != null && !patientDTO.getOpenmrsId().isEmpty()) {
            openmrsID_txt.setText(patientDTO.getOpenmrsId());
        } else {
            openmrsID_txt.setText(getString(R.string.patient_not_registered));
        }

        // setTitle(patientDTO.getOpenmrs_id());

        CustomLog.e(TAG, "patientDTO - " + new Gson().toJson(patientDTO));
        int mAgeYears = -1, mAgeMonths = 0, mAgeDays = 0;
        // setting age
        if (patientDTO.getDateofbirth() != null) {
            String[] ymdData = DateAndTimeUtils.getAgeInYearMonth(patientDTO.getDateofbirth()).split(" ");
            mAgeYears = Integer.parseInt(ymdData[0]);
            mAgeMonths = Integer.parseInt(ymdData[1]);
            mAgeDays = Integer.parseInt(ymdData[2]);
            String age = DateAndTimeUtils.formatAgeInYearsMonthsDate(this, mAgeYears, mAgeMonths, mAgeDays).replace("-", "");
            patientage.setText(age);
            float_ageYear_Month = DateAndTimeUtils.getFloat_Age_Year_Month(patientDTO.getDateofbirth());

            // setting date of birth
            String dob = DateAndTimeUtils.getFormatedDateOfBirthAsView(patientDTO.getDateofbirth());
            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                String dob_text = en__hi_dob(dob); //to show text of English into Hindi...
                patientdob.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                String dob_text = en__or_dob(dob); //to show text of English into Odiya...
                patientdob.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                String dob_text = en__bn_dob(dob); //to show text of English into Odiya...
                patientdob.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                String dob_text = en__gu_dob(dob); //to show text of English into Gujarati...
                patientdob.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                String dob_text = en__te_dob(dob); //to show text of English into telugu...
                patientdob.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                String dob_text = en__mr_dob(dob); //to show text of English into telugu...
                patientdob.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                String dob_text = en__as_dob(dob); //to show text of English into telugu...
                patientdob.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                String dob_text = en__ml_dob(dob); //to show text of English into telugu...
                patientdob.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                String dob_text = en__kn_dob(dob); //to show text of English into telugu...
                patientdob.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                String dob_text = en__ru_dob(dob); //to show text of English into Russian...
                patientdob.setText(dob_text);
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                String dob_text = en__ta_dob(dob); //to show text of English into Tamil...
                patientdob.setText(dob_text);
            } else {
                patientdob.setText(dob);
            }
        }

        // setting gender
        if (patientDTO.getGender() == null || patientDTO.getGender().equals("")) {
            gender.setVisibility(View.GONE);
        } else {
            mGender = patientDTO.getGender();
            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                if (patientDTO.getGender().equalsIgnoreCase("M")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_male));
                } else if (patientDTO.getGender().equalsIgnoreCase("F")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_female));
                } else if (patientDTO.getGender().equalsIgnoreCase("O")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_other));
                } else {
                    gender.setText(patientDTO.getGender());
                }
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                if (patientDTO.getGender().equalsIgnoreCase("M")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_male));
                } else if (patientDTO.getGender().equalsIgnoreCase("F")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_female));
                } else if (patientDTO.getGender().equalsIgnoreCase("O")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_other));
                } else {
                    gender.setText(patientDTO.getGender());
                }
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                if (patientDTO.getGender().equalsIgnoreCase("M")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_male));
                } else if (patientDTO.getGender().equalsIgnoreCase("F")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_female));
                } else if (patientDTO.getGender().equalsIgnoreCase("O")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_other));
                } else {
                    gender.setText(patientDTO.getGender());
                }
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                if (patientDTO.getGender().equalsIgnoreCase("M")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_male));
                } else if (patientDTO.getGender().equalsIgnoreCase("F")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_female));
                } else if (patientDTO.getGender().equalsIgnoreCase("O")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_other));
                } else {
                    gender.setText(patientDTO.getGender());
                }
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                if (patientDTO.getGender().equalsIgnoreCase("M")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_male));
                } else if (patientDTO.getGender().equalsIgnoreCase("F")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_female));
                } else if (patientDTO.getGender().equalsIgnoreCase("O")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_other));
                } else {
                    gender.setText(patientDTO.getGender());
                }
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                if (patientDTO.getGender().equalsIgnoreCase("M")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_male));
                } else if (patientDTO.getGender().equalsIgnoreCase("F")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_female));
                } else if (patientDTO.getGender().equalsIgnoreCase("O")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_other));
                } else {
                    gender.setText(patientDTO.getGender());
                }
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                if (patientDTO.getGender().equalsIgnoreCase("M")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_male));
                } else if (patientDTO.getGender().equalsIgnoreCase("F")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_female));
                } else if (patientDTO.getGender().equalsIgnoreCase("Other")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_other));
                } else {
                    gender.setText(patientDTO.getGender());
                }
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                if (patientDTO.getGender().equalsIgnoreCase("M")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_male));
                } else if (patientDTO.getGender().equalsIgnoreCase("F")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_female));
                } else if (patientDTO.getGender().equalsIgnoreCase("O")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_other));
                } else {
                    gender.setText(patientDTO.getGender());
                }
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                if (patientDTO.getGender().equalsIgnoreCase("M")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_male));
                } else if (patientDTO.getGender().equalsIgnoreCase("F")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_female));
                } else if (patientDTO.getGender().equalsIgnoreCase("O")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_other));
                } else {
                    gender.setText(patientDTO.getGender());
                }
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                if (patientDTO.getGender().equalsIgnoreCase("M")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_male));
                } else if (patientDTO.getGender().equalsIgnoreCase("F")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_female));
                } else if (patientDTO.getGender().equalsIgnoreCase("O")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_other));
                } else {
                    gender.setText(patientDTO.getGender());
                }
            } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                if (patientDTO.getGender().equalsIgnoreCase("M")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_male));
                } else if (patientDTO.getGender().equalsIgnoreCase("F")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_female));
                } else if (patientDTO.getGender().equalsIgnoreCase("O")) {
                    gender.setText(getResources().getString(R.string.identification_screen_checkbox_other));
                } else {
                    gender.setText(patientDTO.getGender());
                }
            } else {
                gender.setText(patientDTO.getGender());
            }
        }

        // setting address1
        if (patientDTO.getAddress1() == null || patientDTO.getAddress1().equals("")) {
            //  address1.setVisibility(View.GONE);
            address1.setText(getResources().getString(R.string.no_address_added));
        } else {
            address1.setText(patientDTO.getAddress1());
        }

        //setting address 2
        if (patientDTO.getAddress2() == null || patientDTO.getAddress2().equals("")) { //
            addr2View.setText(getResources().getString(R.string.no_address_added));
        } else {
            addr2View.setText(patientDTO.getAddress2());
        }

        // setting country
        String country;
        if (patientDTO.getCountry() != null) {
            country = patientDTO.getCountry().trim();
        } else {
            country = getResources().getString(R.string.no_country_added);
        }
        patientcountry.setText(StringUtils.switch_hi_en_country(country, sessionManager.getAppLanguage()));

        // setting state
        String state;
        if (patientDTO.getStateprovince() != null) {
            state = patientDTO.getStateprovince().trim();
        } else {
            state = getResources().getString(R.string.no_state_added);
        }
        patientstate.setText(getStateTranslated(state, sessionManager.getAppLanguage()));

        // setting district and city

        String district = patientDTO.getDistrict();
        String city_village = patientDTO.getCityvillage();
        if (patientDTO.getCityvillage() != null && patientDTO.getCityvillage().length() > 0) {
            String[] district_city = patientDTO.getCityvillage().trim().split(":");
            if (district_city.length == 2) {
                district = district_city[0];
                city_village = district_city[1];
            }
        }


        if (district != null) {
            patientdistrict.setText(getDistrictTranslated(state, district, sessionManager.getAppLanguage()));
        } else {
            patientdistrict.setText(getResources().getString(R.string.no_district_added));
        }

        if (city_village != null) {
            village.setText(city_village);
        } else {
            village.setText(getResources().getString(R.string.no_city_added));
        }


        // end - city and district

        // setting postal code
        if (patientDTO.getPostalcode() != null) {
            postalcode.setText(patientDTO.getPostalcode());
        } else {
            postalcode.setText(getResources().getString(R.string.no_postal_code_added));
        }

        // setting phone number
        if (patientDTO.getPhonenumber() != null && !patientDTO.getPhonenumber().isEmpty()) {
            phone.setText(patientDTO.getPhonenumber());
        } else {
            phone.setText(getResources().getString(R.string.no_mobile_number_added));
        }

        // setting education status
        if (patientDTO.getEducation() != null) {
            if (patientDTO.getEducation().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                patienteducation.setText("नहीं दिया गया");
            } else if (patientDTO.getEducation().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                patienteducation.setText("ଦିଅ ଯାଇ ନାହିଁ");
            } else if (patientDTO.getEducation().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                patienteducation.setText("પૂરી પાડવામાં આવેલ નથી");
            } else if (patientDTO.getEducation().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                patienteducation.setText("సమకూర్చబడలేదు");
            } else if (patientDTO.getEducation().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                patienteducation.setText("झाले नाही");
            } else if (patientDTO.getEducation().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                patienteducation.setText("প্ৰদান কৰা হোৱা নাই");
            } else if (patientDTO.getEducation().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                patienteducation.setText("നൽകിയിട്ടില്ല");
            } else if (patientDTO.getEducation().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                patienteducation.setText("ಒದಗಿಸಲಾಗಿಲ್ಲ");
            } else if (patientDTO.getEducation().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                patienteducation.setText("Не предоставлен");
            } else if (patientDTO.getEducation().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                patienteducation.setText("সরবরাহ করা হয়নি");
            } else if (patientDTO.getEducation().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                patienteducation.setText("வழங்கப்படவில்லை");
            } else {
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                    String education = switch_hi_education_edit(patientDTO.getEducation());
                    patienteducation.setText(education);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                    String education = switch_or_education_edit(patientDTO.getEducation());
                    patienteducation.setText(education);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                    String education = switch_ta_education_edit(patientDTO.getEducation());
                    patienteducation.setText(education);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                    String education = switch_te_education_edit(patientDTO.getEducation());
                    patienteducation.setText(education);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                    String education = switch_mr_education_edit(patientDTO.getEducation());
                    patienteducation.setText(education);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                    String education = switch_as_education_edit(patientDTO.getEducation());
                    patienteducation.setText(education);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                    String education = switch_ml_education_edit(patientDTO.getEducation());
                    patienteducation.setText(education);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                    String education = switch_kn_education_edit(patientDTO.getEducation());
                    patienteducation.setText(education);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                    String education = switch_ru_education_edit(patientDTO.getEducation());
                    patienteducation.setText(education);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                    String education = switch_gu_education_edit(patientDTO.getEducation());
                    patienteducation.setText(education);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                    String education = switch_bn_education_edit(patientDTO.getEducation());
                    patienteducation.setText(education);
                } else {
                    patienteducation.setText(patientDTO.getEducation());
                }
            }
        }

        // setting economic status
        if (patientDTO.getEconomic() != null) {
            if (patientDTO.getEconomic().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                patienteconomicstatus.setText("नहीं दिया गया");
            } else if (patientDTO.getEconomic().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                patienteconomicstatus.setText("ଦିଅ ଯାଇ ନାହିଁ");
            } else if (patientDTO.getEconomic().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                patienteconomicstatus.setText("வழங்கப்படவில்லை");
            } else if (patientDTO.getEconomic().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                patienteconomicstatus.setText("પૂરી પાડવામાં આવેલ નથી");
            } else if (patientDTO.getEconomic().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                patienteconomicstatus.setText("సమకూర్చబడలేదు");
            } else if (patientDTO.getEconomic().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                patienteconomicstatus.setText("झाले नाही");
            } else if (patientDTO.getEconomic().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                patienteconomicstatus.setText("প্ৰদান কৰা হোৱা নাই");
            } else if (patientDTO.getEconomic().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                patienteconomicstatus.setText("നൽകിയിട്ടില്ല");
            } else if (patientDTO.getEconomic().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                patienteconomicstatus.setText("ಒದಗಿಸಲಾಗಿಲ್ಲ");
            } else if (patientDTO.getEconomic().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                patienteconomicstatus.setText("Не предоставлен");
            } else if (patientDTO.getEconomic().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                patienteconomicstatus.setText("সরবরাহ করা হয়নি");
            } else {
                patienteconomicstatus.setText(patientDTO.getEconomic());
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                    String economic = switch_hi_economic_edit(patientDTO.getEconomic());
                    patienteconomicstatus.setText(economic);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                    String economic = switch_or_economic_edit(patientDTO.getEconomic());
                    patienteconomicstatus.setText(economic);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                    String economic = switch_ta_economic_edit(patientDTO.getEconomic());
                    patienteconomicstatus.setText(economic);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                    String economic = switch_bn_economic_edit(patientDTO.getEconomic());
                    patienteconomicstatus.setText(economic);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                    String economic = switch_gu_economic_edit(patientDTO.getEconomic());
                    patienteconomicstatus.setText(economic);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                    String economic = switch_te_economic_edit(patientDTO.getEconomic());
                    patienteconomicstatus.setText(economic);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                    String economic = switch_mr_economic_edit(patientDTO.getEconomic());
                    patienteconomicstatus.setText(economic);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                    String economic = switch_as_economic_edit(patientDTO.getEconomic());
                    patienteconomicstatus.setText(economic);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                    String economic = switch_ml_economic_edit(patientDTO.getEconomic());
                    patienteconomicstatus.setText(economic);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                    String economic = switch_kn_economic_edit(patientDTO.getEconomic());
                    patienteconomicstatus.setText(economic);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                    String economic = switch_ru_economic_edit(patientDTO.getEconomic());
                    patienteconomicstatus.setText(economic);
                } else {
                    patienteconomicstatus.setText(patientDTO.getEconomic());
                }
            }
        }

        // setting caste value
        if (patientDTO.getCaste() != null) {
            if (patientDTO.getCaste().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                patientcaste.setText("नहीं दिया गया");
            } else if (patientDTO.getCaste().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                patientcaste.setText("ଦିଅ ଯାଇ ନାହିଁ");
            } else if (patientDTO.getCaste().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                patientcaste.setText("సమకూర్చబడలేదు");
            } else if (patientDTO.getCaste().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                patientcaste.setText("झाले नाही");
            } else if (patientDTO.getCaste().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                patientcaste.setText("প্ৰদান কৰা হোৱা নাই");
            } else if (patientDTO.getCaste().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                patientcaste.setText("നൽകിയിട്ടില്ല");
            } else if (patientDTO.getCaste().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                patientcaste.setText("ಒದಗಿಸಲಾಗಿಲ್ಲ");
            } else if (patientDTO.getCaste().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                patientcaste.setText("Не предоставлен");
            } else if (patientDTO.getCaste().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                patientcaste.setText("પૂરી પાડવામાં આવેલ નથી");
            } else if (patientDTO.getCaste().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                patientcaste.setText("সরবরাহ করা হয়নি");
            } else if (patientDTO.getCaste().equalsIgnoreCase("Not provided") &&
                    sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                patientcaste.setText("வழங்கப்படவில்லை");
            } else {
                patientcaste.setText(patientDTO.getCaste());
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                    String caste = switch_hi_caste_edit(patientDTO.getCaste());
                    patientcaste.setText(caste);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                    String caste = switch_or_caste_edit(patientDTO.getCaste());
                    patientcaste.setText(caste);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
                    String caste = switch_gu_caste_edit(patientDTO.getCaste());
                    patientcaste.setText(caste);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                    String caste = switch_te_caste_edit(patientDTO.getCaste());
                    patientcaste.setText(caste);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
                    String caste = switch_mr_caste_edit(patientDTO.getCaste());
                    patientcaste.setText(caste);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
                    String caste = switch_as_caste_edit(patientDTO.getCaste());
                    patientcaste.setText(caste);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
                    String caste = switch_ml_caste_edit(patientDTO.getCaste());
                    patientcaste.setText(caste);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
                    String caste = switch_kn_caste_edit(patientDTO.getCaste());
                    patientcaste.setText(caste);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
                    String caste = switch_ru_caste_edit(patientDTO.getCaste());
                    patientcaste.setText(caste);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
                    String caste = switch_bn_caste_edit(patientDTO.getCaste());
                    patientcaste.setText(caste);
                } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
                    String caste = switch_ta_caste_edit(patientDTO.getCaste());
                    patientcaste.setText(caste);
                } else {
                    patientcaste.setText(patientDTO.getCaste());
                }
            }
        }

        // setting son/daughet_wife value
        if (patientDTO.getSon_dau_wife() != null && !patientDTO.getSon_dau_wife().

                equals("")) {
            son_daughter_wife.setText(patientDTO.getSon_dau_wife());
        } else {
            son_daughter_wife.setVisibility(View.GONE);
        }

        // setting national ID value
        if (patientDTO.getNationalID() != null && !patientDTO.getNationalID().

                equals("")) {
            patientNationalID.setText(patientDTO.getNationalID());
        } else {
            patientNationalID.setText(getResources().getString(R.string.not_provided));
        }

        // setting occupation value
        if (patientDTO.getOccupation() != null && !patientDTO.getOccupation().

                equals("")) {
            patientoccupation.setText(patientDTO.getOccupation());
        } else {
            patientoccupation.setText(getString(R.string.not_provided));
        }

        if (AgeUtils.INSTANCE.isGuardianRequired(mAgeYears, mAgeMonths, mAgeDays) && mAgeYears > -1) {
            guardianNameTr.setVisibility(View.VISIBLE);
            guardianTypeTr.setVisibility(View.VISIBLE);

            //guardian type
            if (patientDTO.getGuardianType() != null && !patientDTO.getGuardianType().equals("")) {
                if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                    String type = switch_hi_guardian_type_edit(patientDTO.getGuardianType());
                    guardian_type_tv.setText(type);
                } else {
                    guardian_type_tv.setText(patientDTO.getGuardianType());
                }
            } else {
                guardian_type_tv.setText(getString(R.string.not_provided));
            }

            //guardian name
            if (patientDTO.getGuardianName() != null && !patientDTO.getGuardianName().equals("")) {
                guardina_name_tv.setText(patientDTO.getGuardianName());
            } else {
                guardina_name_tv.setText(getString(R.string.not_provided));
            }
        } else {
            guardianNameTr.setVisibility(View.GONE);
            guardianTypeTr.setVisibility(View.GONE);

        }

        //contact type
        if (patientDTO.getContactType() != null && !patientDTO.getContactType().

                equals("")) {
            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                String type = switch_hi_contact_type_edit(patientDTO.getContactType());
                contact_type_tv.setText(type);
            } else {
                contact_type_tv.setText(patientDTO.getContactType());
            }
        } else {
            contact_type_tv.setText(getString(R.string.not_provided));
        }

        //emergency contact name
        if (patientDTO.getEmContactName() != null && !patientDTO.getEmContactName().

                equals("")) {
            em_contact_name_tv.setText(patientDTO.getEmContactName());
        } else {
            em_contact_name_tv.setText(getString(R.string.not_provided));
        }

        //emergency contact number
        if (patientDTO.getEmContactNumber() != null && !patientDTO.getEmContactNumber().

                equals("")) {
            em_contact_number_tv.setText(patientDTO.getEmContactNumber());
        } else {
            em_contact_number_tv.setText(getString(R.string.not_provided));
        }

        //tmh case number
        if (patientDTO.getTmhCaseNumber() != null && !patientDTO.getTmhCaseNumber().

                equals("")) {
            tmh_case_number_tv.setText(patientDTO.getTmhCaseNumber());
        } else {
            tmh_case_number_tv.setText(getString(R.string.not_provided));
        }

        //request id
        if (patientDTO.getRequestId() != null && !patientDTO.getRequestId().

                equals("")) {
            request_id_tv.setText(patientDTO.getRequestId());
        } else {
            request_id_tv.setText(getString(R.string.not_provided));
        }

        //relative phone num
        if (patientDTO.getRelativePhoneNumber() != null && !patientDTO.getRelativePhoneNumber().

                equals("")) {
            relative_phone_num_tv.setText(patientDTO.getRelativePhoneNumber());
        } else {
            relative_phone_num_tv.setText(getString(R.string.not_provided));
        }

        //discipline
        if (patientDTO.getDiscipline() != null && !patientDTO.getDiscipline().

                equals("")) {
            discipline_tv.setText(patientDTO.getDiscipline());
        } else {
            discipline_tv.setText(getString(R.string.not_provided));
        }

        //department
        if (patientDTO.getDepartment() != null && !patientDTO.getDepartment().

                equals("")) {
            department_tv.setText(patientDTO.getDepartment());
        } else {
            department_tv.setText(getString(R.string.not_provided));
        }

        //province
        if (patientDTO.getProvince() != null && !patientDTO.getProvince().

                equals("")) {
            provinceTv.setText(LanguageUtils.getProvince(patientDTO.getProvince()));
        } else {
            provinceTv.setText(getString(R.string.not_provided));
        }

        //city
        if (patientDTO.getCity() != null && !patientDTO.getCity().

                equals("")) {
            cityTv.setText(LanguageUtils.getCity(patientDTO.getCity()));
        } else {
            cityTv.setText(getString(R.string.not_provided));
        }

        //registration address
        if (patientDTO.getRegistrationAddressOfHf() != null && !patientDTO.getRegistrationAddressOfHf().

                equals("")) {
            registrationAddressOfHfTv.setText(patientDTO.getRegistrationAddressOfHf());
        } else {
            registrationAddressOfHfTv.setText(getString(R.string.not_provided));
        }

        //Inn
        if (patientDTO.getInn() != null && !patientDTO.getInn().

                equals("")) {
            innTv.setText(patientDTO.getInn());
        } else {
            innTv.setText(getString(R.string.not_provided));
        }

        //Code of the Health Facility
        if (patientDTO.getCodeOfHealthFacility() != null && !patientDTO.getCodeOfHealthFacility().

                equals("")) {
            codeOfHealthFacilityTv.setText(patientDTO.getCodeOfHealthFacility());
        } else {
            codeOfHealthFacilityTv.setText(getString(R.string.not_provided));
        }

        //Health facility name
        if (patientDTO.getHealthFacilityName() != null && !patientDTO.getHealthFacilityName().

                equals("")) {
            healthFacilityNameTv.setText(StringUtils.getHealthyFacilityName(patientDTO.getHealthFacilityName(), sessionManager.getCurrentLang()));
        } else {
            healthFacilityNameTv.setText(getString(R.string.not_provided));
        }

        //Code of the Department
        if (patientDTO.getCodeOfDepartment() != null && !patientDTO.getCodeOfDepartment().

                equals("")) {
            codeOfDepartmentTv.setText(patientDTO.getCodeOfDepartment());
        } else {
            codeOfDepartmentTv.setText(getString(R.string.not_provided));
        }

        //department
        if (patientDTO.getDepartment() != null && !patientDTO.getDepartment().

                equals("")) {
            departmentTv.setText(patientDTO.getDepartment());
        } else {
            departmentTv.setText(getString(R.string.not_provided));
        }
    }

    private String getStateTranslated(String state, String language) {
        String desiredVal = state;
        JSONObject jsonObject = FileUtils.encodeJSON(PatientDetailActivity2.this, "state_district_tehsil.json");
        if (jsonObject != null) {
            StateDistMaster mStateDistMaster = new Gson().fromJson(jsonObject.toString(), StateDistMaster.
                    class);
            for (int i = 0; i < mStateDistMaster.getStateDataList().size(); i++) {
                String sName = mStateDistMaster.getStateDataList().get(i).getState();
                if (sName.equalsIgnoreCase(state)) {
                    if (language.equalsIgnoreCase("hi"))
                        desiredVal = mStateDistMaster.getStateDataList().get(i).getStateHindi();
                    else if (language.equalsIgnoreCase("en"))
                        desiredVal = mStateDistMaster.getStateDataList().get(i).getState();
                    break;
                }
            }
        }

        return desiredVal;
    }

    private String getDistrictTranslated(String state, String district, String language) {
        StateDistMaster mStateDistMaster = new Gson().fromJson(FileUtils.encodeJSON(PatientDetailActivity2.this, "state_district_tehsil.json").toString(), StateDistMaster.class);
        List<DistData> distDataList = new ArrayList<>();
        String desiredVal = district;

        for (int i = 0; i < mStateDistMaster.getStateDataList().size(); i++) {
            String sName = mStateDistMaster.getStateDataList().get(i).getState();
            if (sName.equalsIgnoreCase(state)) {
                distDataList = mStateDistMaster.getStateDataList().get(i).getDistDataList();
                break;
            }
        }

        if (distDataList != null && !distDataList.isEmpty())
            for (int i = 0; i < distDataList.size(); i++) {
                if (distDataList.get(i).getName().equalsIgnoreCase(district)) {
                    if (language.equalsIgnoreCase("hi"))
                        desiredVal = distDataList.get(i).getNameHindi();
                    else if (language.equalsIgnoreCase("en"))
                        desiredVal = distDataList.get(i).getName();
                    break;
                }
            }

        return desiredVal;
    }

    // profile pic download
    public void profilePicDownloaded() {
        UrlModifiers urlModifiers = new UrlModifiers();
        String url = urlModifiers.patientProfileImageUrl(patientDTO.getUuid());
        Logger.logD(TAG, "profileimage url" + url);
        Observable<ResponseBody> profilePicDownload = AppConstants.apiInterface.PERSON_PROFILE_PIC_DOWNLOAD(url, "Basic " + sessionManager.getEncoded());
        profilePicDownload.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody file) {
                        DownloadFilesUtils downloadFilesUtils = new DownloadFilesUtils();
                        downloadFilesUtils.saveToDisk(file, patientDTO.getUuid());
                        Logger.logD(TAG, file.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.logD(TAG, e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Logger.logD(TAG, "complete" + patientDTO.getPatientPhoto());
                        PatientsDAO patientsDAO = new PatientsDAO();
                        boolean updated = false;
                        try {
                            updated = patientsDAO.updatePatientPhoto(patientDTO.getUuid(), AppConstants.IMAGE_PATH + patientDTO.getUuid() + ".jpg");
                        } catch (DAOException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                            CustomLog.e(TAG, e.getMessage());
                        }
                        if (updated) {
                            RequestBuilder<Drawable> requestBuilder = Glide.with(PatientDetailActivity2.this)
                                    .asDrawable().sizeMultiplier(0.3f);
                            Glide.with(PatientDetailActivity2.this)
                                    .load(AppConstants.IMAGE_PATH + patientDTO.getUuid() + ".jpg")
                                    .thumbnail(requestBuilder)
                                    .centerCrop()
                                    .error(R.drawable.avatar1)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true)
                                    .into(profile_image);
                        }
                        ImagesDAO imagesDAO = new ImagesDAO();
                        boolean isImageDownloaded = false;
                        try {
                            isImageDownloaded = imagesDAO.insertPatientProfileImages(AppConstants.IMAGE_PATH +
                                    patientDTO.getUuid() + ".jpg", patientDTO.getUuid());
                        } catch (DAOException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                            CustomLog.e(TAG, e.getMessage());
                        }
                    }
                });
    }

    public void backPress(View view) {
        Intent intent = new Intent(this, SearchPatientActivity_New.class);
        startActivity(intent);
        finish();
    }

    public void syncNow(View view) {
        if (NetworkConnection.isOnline(this)) {
            refresh.clearAnimation();
            syncAnimator.start();
            new SyncUtils().syncBackground();
            //Toast.makeText(this, getString(R.string.sync_strated), Toast.LENGTH_SHORT).show();
        }
    }

    // Receiver class for Openmrs ID
    public class Myreceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                openmrsID_txt.setText(patientsDAO.getOpenmrsId(patientDTO.getUuid()));
                setTitle(openmrsID_txt.getText());
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                CustomLog.e(TAG, e.getMessage());
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        ContextCompat.registerReceiver(
                context,
                reMyreceive,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Toast.makeText(context, getString(R.string.sync_completed), Toast.LENGTH_SHORT).show();
                CustomLog.v(TAG, "Sync Done!");
                try {
                    refresh.clearAnimation();
                    syncAnimator.cancel();
                    recreate();
                } catch (Exception e) {
                    CustomLog.d(TAG, e.getMessage());
                }
            }
        };


        // sometimes crash happens whenever we register mBroadcastReceiver on oncreate and unregister from ondestroy
        // because the onreceive function listen the broadcaster receiver even out activity is on background mode
        // So that's why registering the mBroadcastReceiver on onstart and destroying it from onstop

        IntentFilter filterSend = new IntentFilter();
        filterSend.addAction(AppConstants.SYNC_NOTIFY_INTENT_ACTION);
        ContextCompat.registerReceiver(
                context,
                mBroadcastReceiver,
                filterSend,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );

        //register receiver for internet check
        networkUtils.callBroadcastReceiver();
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            unregisterReceiver(reMyreceive);
            // sometimes crash happens whenever we register mBroadcastReceiver on oncreate and unregister from ondestroy
            // because the onreceive function listen the broadcaster receiver even out activity is on background mode
            // So that's why registering the mBroadcastReceiver on onstart and destroying it from onstop
            unregisterReceiver(mBroadcastReceiver);
            //unregister receiver for internet check
            networkUtils.unregisterNetworkReceiver();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            CustomLog.d(TAG, e.getMessage());
        }
    }

   /* @Override
    protected void onDestroy() {
        unregisterReceiver(reMyreceive);
        super.onDestroy();
    }*/

    // Dialog show
    public void startVisitDialog(Context context, Drawable drawable, String title, String subTitle,
                                 String positiveBtnTxt, String negativeBtnTxt) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.dialog_patient_registration, null);
        alertdialogBuilder.setView(convertView);
        ImageView icon = convertView.findViewById(R.id.dialog_icon);
        TextView dialog_title = convertView.findViewById(R.id.dialog_title);
        TextView dialog_subtitle = convertView.findViewById(R.id.dialog_subtitle);
        Button positive_btn = convertView.findViewById(R.id.positive_btn);
        Button negative_btn = convertView.findViewById(R.id.negative_btn);

        icon.setImageDrawable(drawable);
        dialog_title.setText(title);
        dialog_subtitle.setText(subTitle);
        positive_btn.setText(positiveBtnTxt);
        negative_btn.setText(negativeBtnTxt);


        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

        negative_btn.setOnClickListener(v -> {
            alertDialog.dismiss();

        });

        positive_btn.setOnClickListener(v -> {
            //  checkVisitOrStartNewVisit();  // commented as this isnt being in use.
        });

        alertDialog.show();
    }

/*
    private void checkVisitOrStartNewVisit() {
        // before starting, we determine if it is new visit for a returning patient
        // extract both FH and PMH
        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);
        Date todayDate = new Date();
        String thisDate = currentDate.format(todayDate);


        String uuid = UUID.randomUUID().toString();
        EncounterDAO encounterDAO = new EncounterDAO();
        encounterDTO = new EncounterDTO();
        encounterDTO.setUuid(UUID.randomUUID().toString());
        encounterDTO.setEncounterTypeUuid(encounterDAO.getEncounterTypeUuid("ENCOUNTER_VITALS"));
        encounterDTO.setEncounterTime(thisDate);
        encounterDTO.setVisituuid(uuid);
        encounterDTO.setSyncd(false);
        encounterDTO.setProvideruuid(sessionManager.getProviderID());
        CustomLog.d("DTO", "DTO:detail " + encounterDTO.getProvideruuid());
        encounterDTO.setVoided(0);
        //   encounterDTO.setPrivacynotice_value(privacy_value_selected);//privacy value added. // TODO: handle later.

        try {
            encounterDAO.createEncountersToDB(encounterDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        InteleHealthDatabaseHelper mDatabaseHelper = new InteleHealthDatabaseHelper(PatientDetailActivity2.this);
        SQLiteDatabase sqLiteDatabase = mDatabaseHelper.getReadableDatabase();

        String CREATOR_ID = sessionManager.getCreatorID();
        returning = false;
        sessionManager.setReturning(returning);

        String[] cols = {"value"};
        // querying for PMH (Past Medical History)
        Cursor cursor = sqLiteDatabase.query("tbl_obs", cols, "encounteruuid=? and conceptuuid=?",
                new String[]{encounterAdultIntials, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB},
                null, null, null);

        if (cursor.moveToFirst()) {
            // rows present
            do {
                // so that null data is not appended
                phistory = phistory + cursor.getString(0);

            }
            while (cursor.moveToNext());
            returning = true;
            sessionManager.setReturning(returning);
        }
        cursor.close();

//                Cursor cursor1 = sqLiteDatabase.query("tbl_obs", cols, "encounteruuid=? and conceptuuid=?",// querying for FH (Family History)
//                        new String[]{encounterAdultIntials, UuidDictionary.RHK_FAMILY_HISTORY_BLURB},
//                        null, null, null);
//                if (cursor1.moveToFirst()) {
//                    // rows present
//                    do {
//                        fhistory = fhistory + cursor1.getString(0);
//                    }
//                    while (cursor1.moveToNext());
//                    returning = true;
//                    sessionManager.setReturning(returning);
//                }
//                cursor1.close();

        // Will display data for patient as it is present in database
        // Toast.makeText(PatientDetailActivity.this,"PMH: "+phistory,Toast.LENGTH_SHORT).sƒhow();
        // Toast.makeText(PatientDetailActivity.this,"FH: "+fhistory,Toast.LENGTH_SHORT).show();

        Intent intent2 = new Intent(PatientDetailActivity2.this, VitalsActivity.class);
        String fullName = patientDTO.getFirstname() + " " + patientDTO.getLastname();
        intent2.putExtra("patientUuid", patientDTO.getUuid());
        VisitDTO visitDTO = new VisitDTO();
        visitDTO.setUuid(uuid);
        visitDTO.setPatientuuid(patientDTO.getUuid());
        visitDTO.setStartdate(thisDate);
        visitDTO.setVisitTypeUuid(UuidDictionary.VISIT_TELEMEDICINE);
        visitDTO.setLocationuuid(sessionManager.getLocationUuid());
        visitDTO.setSyncd(false);
        visitDTO.setCreatoruuid(sessionManager.getCreatorID());//static
        VisitsDAO visitsDAO = new VisitsDAO();

        try {
            visitsDAO.insertPatientToDB(visitDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        // visitUuid = String.valueOf(visitLong);
//                localdb.close();
        intent2.putExtra("patientUuid", patientDTO.getUuid());
        intent2.putExtra("visitUuid", uuid);
        intent2.putExtra("encounterUuidVitals", encounterDTO.getUuid());
        intent2.putExtra("encounterUuidAdultIntial", "");
        intent2.putExtra("EncounterAdultInitial_LatestVisit", encounterAdultIntials);
        intent2.putExtra("name", fullName);
        intent2.putExtra("gender", mGender);
        intent2.putExtra("tag", "new");
        intent2.putExtra("float_ageYear_Month", float_ageYear_Month);
        startActivity(intent2);
    }
*/

    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        CustomLog.d("TAG", "updateUIForInternetAvailability: ");
        if (isInternetAvailable) {
            refresh.setImageDrawable(ContextCompat.getDrawable(PatientDetailActivity2.this, R.drawable.ui2_ic_internet_available));
        } else {
            refresh.setImageDrawable(ContextCompat.getDrawable(PatientDetailActivity2.this, R.drawable.ui2_ic_no_internet));
        }
    }

    private void initForPastVisit() {
        mPastVisitDataList.clear();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        String visitSelection = "patientuuid = ? and enddate IS NOT NULL and enddate != ''";
        String[] visitArgs = {patientDTO.getUuid()};
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
                                            if (!comp.trim().isEmpty() && comp.contains(colon)) {
                                                visitValue = visitValue + Node.bullet_arrow + comp.substring(0, comp.indexOf(colon)) + "<br/>";

                                            }
                                        }
                                        if (!visitValue.isEmpty()) {
                                            visitValue = visitValue.replaceAll(Node.bullet_arrow, "");
                                            visitValue = visitValue.replaceAll("<br/>", ", ");
                                            visitValue = visitValue.replaceAll(Node.ASSOCIATE_SYMPTOMS, "");
                                            //visitValue = visitValue.substring(0, visitValue.length() - 2);
                                            visitValue = visitValue.replaceAll("<b>", "");
                                            visitValue = visitValue.replaceAll("</b>", "");
                                            visitValue = visitValue.trim();
                                            while (visitValue.endsWith(",")) {
                                                visitValue = visitValue.substring(0, visitValue.length() - 1).trim();
                                            }
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

                                    StringBuilder stringBuilder = new StringBuilder();
                                    for (String s : spt) {
                                        String complainName = "";
                                        if (s.isEmpty()) continue;
                                        //String s1 =  new String(s.getBytes(), "UTF-8");
                                        System.out.println(s);
                                        String[] spt1 = s.split("::●");
                                        complainName = spt1[0];

                                        //if (s.trim().startsWith(getTranslatedAssociatedSymptomQString(lCode))) {
                                        if (!complainName.trim().contains(VisitUtils.getTranslatedPatientDenies(sessionManager.getAppLanguage()))) {
                                            System.out.println(complainName);
                                            if (!stringBuilder.toString().isEmpty())
                                                stringBuilder.append(", ");
                                            stringBuilder.append(complainName);
                                        }

                                    }
                                /*StringBuilder stringBuilder = new StringBuilder();
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
                                    }*/
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
                findViewById(R.id.cv_past_visits).setVisibility(View.VISIBLE);
                PastVisitListingAdapter pastVisitListingAdapter = new PastVisitListingAdapter(mPastVisitsRecyclerView, PatientDetailActivity2.this, mPastVisitDataList, new PastVisitListingAdapter.OnItemSelected() {
                    @Override
                    public void onItemSelected(PastVisitData pastVisitData) {
                        intentForVisitDetails(pastVisitData);
                    }
                });
                mPastVisitsRecyclerView.setAdapter(pastVisitListingAdapter);
            } else {
                findViewById(R.id.cv_past_visits).setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onFeatureActiveStatusLoaded(FeatureActiveStatus activeStatus) {
        super.onFeatureActiveStatusLoaded(activeStatus);
        if (activeStatus != null) {
            binding.setAddressActiveStatus(activeStatus.getActiveStatusPatientAddress());
            binding.setOtherActiveStatus(activeStatus.getActiveStatusPatientOther());
        }
    }
}