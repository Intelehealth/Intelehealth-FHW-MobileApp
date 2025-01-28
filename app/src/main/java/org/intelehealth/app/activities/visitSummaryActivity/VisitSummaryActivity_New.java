package org.intelehealth.app.activities.visitSummaryActivity;

import static org.intelehealth.app.app.AppConstants.CONFIG_FILE_NAME;
import static org.intelehealth.app.ayu.visit.common.VisitUtils.convertCtoF;
import static org.intelehealth.app.ayu.visit.common.VisitUtils.getTranslatedAssociatedSymptomQString;
import static org.intelehealth.app.ayu.visit.common.VisitUtils.getTranslatedPatientDenies;
import static org.intelehealth.app.database.dao.EncounterDAO.fetchEncounterUuidForEncounterAdultInitials;
import static org.intelehealth.app.database.dao.EncounterDAO.fetchEncounterUuidForEncounterVitals;
import static org.intelehealth.app.database.dao.ObsDAO.fetchValueFromLocalDb;
import static org.intelehealth.app.knowledgeEngine.Node.bullet_arrow;
import static org.intelehealth.app.syncModule.SyncUtils.syncNow;
import static org.intelehealth.app.ui2.utils.CheckInternetAvailability.isNetworkAvailable;
import static org.intelehealth.app.utilities.DateAndTimeUtils.parse_DateToddMMyyyy;
import static org.intelehealth.app.utilities.DateAndTimeUtils.parse_DateToddMMyyyy_new;
import static org.intelehealth.app.utilities.StringUtils.setGenderAgeLocal;
import static org.intelehealth.app.utilities.UuidDictionary.ADDITIONAL_NOTES;
import static org.intelehealth.app.utilities.UuidDictionary.CONSULTATION_TYPE;
import static org.intelehealth.app.utilities.UuidDictionary.DIAGNOSIS;
import static org.intelehealth.app.utilities.UuidDictionary.ENCOUNTER_ADULTINITIAL;
import static org.intelehealth.app.utilities.UuidDictionary.FACILITY;
import static org.intelehealth.app.utilities.UuidDictionary.HW_FOLLOWUP_CONCEPT_ID;
import static org.intelehealth.app.utilities.UuidDictionary.PRESCRIPTION_LINK;
import static org.intelehealth.app.utilities.UuidDictionary.SEVERITY;
import static org.intelehealth.app.utilities.UuidDictionary.SPECIALITY;
import static org.intelehealth.app.utilities.UuidDictionary.VISIT_UPLOAD_TIME;
import static org.intelehealth.app.utilities.VisitUtils.endVisit;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.LocaleList;
import android.print.PdfPrint;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;

import org.intelehealth.app.activities.bill.VisitSummaryBillModel;
import org.intelehealth.app.activities.bill.VisitSummaryBillUtils;
import org.intelehealth.app.ui.billgeneration.models.BillDetails;
import org.intelehealth.app.utilities.CustomLog;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.ajalt.timberkt.Timber;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.R;
import org.intelehealth.app.activities.additionalDocumentsActivity.AdditionalDocumentAdapter;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.activities.notification.AdapterInterface;
import org.intelehealth.app.activities.prescription.PrescriptionBuilder;
import org.intelehealth.app.activities.visit.PrescriptionActivity;
import org.intelehealth.app.activities.visitSummaryActivity.facilitytovisit.FacilityToVisitArrayAdapter;
import org.intelehealth.app.activities.visitSummaryActivity.facilitytovisit.FacilityToVisitModel;
import org.intelehealth.app.activities.visitSummaryActivity.saverity.SeverityArrayAdapter;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.appointment.dao.AppointmentDAO;
import org.intelehealth.app.appointment.model.AppointmentInfo;
import org.intelehealth.app.appointmentNew.MyAppointmentNew.MyAppointmentActivityNew;
import org.intelehealth.app.appointmentNew.ScheduleAppointmentActivity_New;
import org.intelehealth.app.ayu.visit.VisitCreationActivity;
import org.intelehealth.app.ayu.visit.common.VisitUtils;
import org.intelehealth.app.ayu.visit.common.adapter.SummaryViewAdapter;
import org.intelehealth.app.ayu.visit.model.CommonVisitData;
import org.intelehealth.app.ayu.visit.model.VisitSummaryData;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.RTCConnectionDAO;
import org.intelehealth.app.database.dao.VisitAttributeListDAO;
import org.intelehealth.app.databinding.ActivityVisitSummaryNewBinding;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.models.ClsDoctorDetails;
import org.intelehealth.app.models.DocumentObject;
import org.intelehealth.app.models.NotificationModel;
import org.intelehealth.app.models.Patient;
import org.intelehealth.app.models.VitalsObject;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.models.dto.RTCConnectionDTO;
import org.intelehealth.app.services.DownloadService;
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.ui.patient.activity.PatientRegistrationActivity;
import org.intelehealth.app.ui.specialization.SpecializationArrayAdapter;
import org.intelehealth.app.ui2.utils.CheckInternetAvailability;
import org.intelehealth.app.utilities.AppointmentUtils;
import org.intelehealth.app.utilities.BitmapUtils;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.DownloadFilesUtils;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.FlavorKeys;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.NetworkUtils;
import org.intelehealth.app.utilities.PatientRegStage;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.TooltipWindow;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.app.webrtc.activity.IDAChatActivity;
import org.intelehealth.config.presenter.fields.data.DiagnosticsRepository;
import org.intelehealth.config.presenter.fields.data.PatientVitalRepository;
import org.intelehealth.config.presenter.fields.factory.DiagnosticsViewModelFactory;
import org.intelehealth.config.presenter.fields.factory.PatientVitalViewModelFactory;
import org.intelehealth.config.presenter.fields.viewmodel.DiagnosticsViewModel;
import org.intelehealth.config.presenter.fields.viewmodel.PatientVitalViewModel;
import org.intelehealth.config.presenter.language.factory.SpecializationViewModelFactory;
import org.intelehealth.config.presenter.specialization.data.SpecializationRepository;
import org.intelehealth.config.presenter.specialization.viewmodel.SpecializationViewModel;
import org.intelehealth.config.room.ConfigDatabase;
import org.intelehealth.config.room.entity.Diagnostics;
import org.intelehealth.config.room.entity.FeatureActiveStatus;
import org.intelehealth.config.room.entity.PatientVital;
import org.intelehealth.config.room.entity.Specialization;
import org.intelehealth.config.utility.PatientDiagnosticsConfigKeys;
import org.intelehealth.config.utility.PatientVitalConfigKeys;
import org.intelehealth.config.utility.ResUtils;
import org.intelehealth.ihutils.ui.CameraActivity;
import org.intelehealth.klivekit.model.RtcArgs;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * Created by: Prajwal Waingankar On: 2/Nov/2022
 * Github: prajwalmw
 */
@SuppressLint("Range")
public class VisitSummaryActivity_New extends BaseActivity implements AdapterInterface, NetworkUtils.InternetCheckUpdateInterface {
    private static final String TAG = VisitSummaryActivity_New.class.getSimpleName();
    private static final int PICK_IMAGE_FROM_GALLERY = 2001;
    //SQLiteDatabase db;
    Button btn_vs_sendvisit;
    private Context context;
    private ImageButton btn_up_header, btn_up_vitals_header, btn_up_visitreason_header, btn_up_phyexam_header, btn_up_medhist_header, btn_up_addnotes_vd_header;
    private RelativeLayout vitals_header_relative, chiefcomplaint_header_relative, physExam_header_relative,
            pathistory_header_relative, addnotes_vd_header_relative, special_vd_header_relative, associated_sym_relative,
            reports_relative, denies_relative, pathist_details_relative;
    private RelativeLayout vs_header_expandview, vs_vitals_header_expandview, vd_special_header_expandview, vs_visitreason_header_expandview, vs_phyexam_header_expandview, vs_medhist_header_expandview, vd_addnotes_header_expandview, vs_add_notes, parentLayout;
    private RelativeLayout add_additional_doc;
    private LinearLayout btn_bottom_printshare, ll_associated_sympt;
    private ConstraintLayout btn_bottom_vs;
    private TextInputEditText etAdditionalNotesVS;
    SessionManager sessionManager, sessionManager1;
    String appLanguage, patientUuid, visitUuid, state, patientName, patientGender, intentTag, visitUUID, medicalAdvice_string = "", medicalAdvice_HyperLink = "", isSynedFlag = "";
    private float float_ageYear_Month;
    String encounterVitals, encounterUuidAdultIntial, EncounterAdultInitial_LatestVisit;
    SharedPreferences mSharedPreference;
    Boolean isPastVisit = false, isVisitSpecialityExists = false;
    Boolean isReceiverRegistered = false;
    ArrayList<String> physicalExams;
    VisitSummaryActivity_New.DownloadPrescriptionService downloadPrescriptionService;
    private RecyclerView mAdditionalDocsRecyclerView, mPhysicalExamsRecyclerView, cc_recyclerview;
    private RecyclerView.LayoutManager mAdditionalDocsLayoutManager, mPhysicalExamsLayoutManager;
    private RecyclerView.LayoutManager cc_recyclerview_gridlayout;
    private AdditionalDocumentAdapter recyclerViewAdapter;
    private ComplaintHeaderAdapter cc_adapter;
    private String mEngReason = "";

    boolean hasLicense = false;
    private boolean hasPrescription = false;
    private boolean isRespiratory = false, uploaded = false, downloaded = false;
    Button uploadButton, /*btn_vs_print, btn_vs_share,*/
            mViewPrescriptionButton;
    ImageView ivPrescription;   // todo: not needed here

    Patient patient = new Patient();
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
    ObsDTO mBloodGroupObsDTO = new ObsDTO();
    ObsDTO resp = new ObsDTO();

    String diagnosisReturned = "";
    String rxReturned = "";
    String testsReturned = "";
    String adviceReturned = "";
    String doctorName = "";
    String additionalReturned = "";
    String followUpDate = "";
    String referredSpeciality = "";

//    CardView diagnosisCard;
//    CardView prescriptionCard;
//    CardView medicalAdviceCard;
//    CardView requestedTestsCard;
//    CardView additionalCommentsCard;
//    CardView followUpDateCard;
//    CardView card_print, card_share;
//
//
//    TextView diagnosisTextView;
//    TextView prescriptionTextView;
//    TextView medicalAdviceTextView;
//    TextView requestedTestsTextView;
//    TextView additionalCommentsTextView;
//    TextView followUpDateTextView;

    // new
    TextView nameView;
    TextView genderView;
    TextView idView;
    TextView visitView;
    TextView heightView;
    TextView weightView;
    TextView pulseView;
    TextView bpView;
    TextView tempView;
    TextView spO2View;
    TextView mBloodGroupTextView;
    TextView bmiView;
    TextView complaintView, patientReports_txtview, patientDenies_txtview, physical_exam_tv, physical_exam_info_tv;
    TextView famHistView;
    TextView patHistView;
    TextView physFindingsView;
    TextView mDoctorTitle;
    TextView mDoctorName;
    TextView mCHWname;
    TextView add_docs_title, tvAddNotesValueVS, reminder, incomplete_act, archieved_notifi;
    String addnotes_value = "";
    private TextInputLayout tilAdditionalNotesVS;

    TextView respiratory;
    TextView respiratoryText;
    TextView tempfaren;
    TextView tempcel;
    String medHistory;
    String baseDir;
    String filePathPhyExam;
    File obsImgdir;
    String gender_tv;
    String mFileName = CONFIG_FILE_NAME;
    String mHeight, mWeight, mBMI, mBP, mPulse, mTemp, mSPO2, mresp;
    String speciality_selected = "", selectedConsultationType = "";
    private TextView physcialExaminationDownloadText, vd_special_value;
    NetworkChangeReceiver receiver;
    public static final String FILTER = "io.intelehealth.client.activities.visit_summary_activity.REQUEST_PROCESSED";
    String encounterUuid;
    Spinner speciality_spinner;
    SwitchMaterial flag;
    private Handler mBackgroundHandler;
    private List<DocumentObject> rowListItem;
    String sign_url;

    LinearLayout editVitals, editPhysical, editFamHist, editMedHist, editComplaint, cc_details_edit, ass_symp_edit;
    ImageButton editAddDocs;
    ImageButton btn_up_special_vd_header;

    ImageView profile_image, downloadbtn;
    String profileImage = "";
    String profileImage1 = "";
    ImagesDAO imagesDAO = new ImagesDAO();
    private WebView mWebView;
    public static String prescription1;
    public static String prescription2;
    private CardView doc_speciality_card, special_vd_card, addnotes_vd_card;
    private VisitAttributeListDAO visitAttributeListDAO = new VisitAttributeListDAO();
    private ImageButton backArrow, priority_hint, refresh, filter;
    private NetworkUtils networkUtils;
    private static final int SCHEDULE_LISTING_INTENT = 2001;
    private static final int GROUP_PERMISSION_REQUEST = 1000;
    private static final int DIALOG_CAMERA_PERMISSION_REQUEST = 3000;
    private static final int DIALOG_GALLERY_PERMISSION_REQUEST = 4000;
    Button btnAppointment, openall_btn;
    private FrameLayout filter_framelayout;
    private View hl_2;
    private boolean priorityVisit = false;
    private ObjectAnimator syncAnimator;
    TooltipWindow tipWindow;
    Boolean doesAppointmentExist = false;

    private CommonVisitData mCommonVisitData;

    private SpecializationViewModel viewModel;
    private ActivityVisitSummaryNewBinding mBinding;
    private List<FacilityToVisitModel> facilityList = null;
    private List<String> severityList = null;
    private FacilityToVisitModel selectedFacilityToVisit = null;
    private String selectedSeverity = null;
    private String selectedFollowupDate, selectedFollowupTime;
    private String visitType = "Consultation";

    public void startTextChat(View view) {
        if (!CheckInternetAvailability.isNetworkAvailable(this)) {
            Toast.makeText(this, getString(R.string.not_connected_txt), Toast.LENGTH_SHORT).show();
            return;
        }

        EncounterDAO encounterDAO = new EncounterDAO();
        EncounterDTO encounterDTO = encounterDAO.getEncounterByVisitUUID(visitUUID);
        RTCConnectionDAO rtcConnectionDAO = new RTCConnectionDAO();
        RTCConnectionDTO rtcConnectionDTO = rtcConnectionDAO.getByVisitUUID(visitUUID);
        RtcArgs args = new RtcArgs();
        if (rtcConnectionDTO != null) {
            args.setDoctorUuid(rtcConnectionDTO.getConnectionInfo());
            args.setPatientId(patientUuid);
            args.setPatientName(patientName);
            args.setVisitId(visitUUID);
            args.setNurseId(encounterDTO.getProvideruuid());
            IDAChatActivity.startChatActivity(VisitSummaryActivity_New.this, args);
        } else {
            //chatIntent.putExtra("toUuid", ""); // assigned doctor uuid
            Toast.makeText(this, getResources().getString(R.string.wait_for_the_doctor_message), Toast.LENGTH_SHORT).show();
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

    public void startVideoChat(View view) {
        Toast.makeText(this, getString(R.string.video_call_req_sent), Toast.LENGTH_SHORT).show();
    }

    private FeatureActiveStatus mFeatureActiveStatus;

    @Override
    protected void onFeatureActiveStatusLoaded(FeatureActiveStatus activeStatus) {
        super.onFeatureActiveStatusLoaded(activeStatus);
        if (activeStatus != null) {
            mFeatureActiveStatus = activeStatus;
            findViewById(R.id.flFacilityToVisit).setVisibility(activeStatus.getVisitSummeryFacilityToVisit() ? View.VISIBLE : View.GONE);
            findViewById(R.id.flSeverity).setVisibility(activeStatus.getVisitSummerySeverityOfCase() ? View.VISIBLE : View.GONE);
            findViewById(R.id.fabStartChat).setVisibility(activeStatus.getChatSection() ? View.VISIBLE : View.GONE);
            findViewById(R.id.vitalsCard).setVisibility(activeStatus.getVitalSection() ? View.VISIBLE : View.GONE);
            findViewById(R.id.add_notes_relative).setVisibility(activeStatus.getVisitSummeryNote() ? View.VISIBLE : View.GONE);
            findViewById(R.id.add_doc_relative).setVisibility(activeStatus.getVisitSummeryAttachment() ? View.VISIBLE : View.GONE);
            findViewById(R.id.flVdCard).setVisibility(activeStatus.getVisitSummeryDoctorSpeciality() ? View.VISIBLE : View.GONE);
            findViewById(R.id.cardPriorityVisit).setVisibility(activeStatus.getVisitSummeryPriorityVisit() ? View.VISIBLE : View.GONE);
            findViewById(R.id.cvFollowup).setVisibility(activeStatus.getVisitSummeryHwFollowUp() ? View.VISIBLE : View.GONE);

            mBinding.flDiagnosisCard.setVisibility(activeStatus.getDiagnosisAtSecondaryLevel() ? View.VISIBLE : View.GONE);
            mBinding.flConsultationTypeCard.setVisibility(activeStatus.getTypeOfConsultation() ? View.VISIBLE : View.GONE);
//            if (!activeStatus.getVisitSummeryAppointment()) {
            Button btn = findViewById(R.id.btn_vs_appointment);
            boolean isAppointment = btn.getText().toString().equals(getString(R.string.appointment));
            if (isAppointment) {
                boolean activeAppointment = activeStatus.getVisitSummeryAppointment();
                btn.setVisibility(activeAppointment ? View.VISIBLE : View.GONE);
            }
            if (isVisitSpecialityExists)
                findViewById(R.id.btnGenerateBill).setVisibility(activeStatus.getGenerateBillButton() ? View.VISIBLE : View.GONE);

            findViewById(R.id.diagnosticsCard).setVisibility(activeStatus.getActiveStatusDiagnosticsSection() ? View.VISIBLE : View.GONE);

//            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_visit_summary_new);

        setupSpecialization();

        context = VisitSummaryActivity_New.this;


        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(Color.WHITE);

        //db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();

        initUI();
        networkUtils = new NetworkUtils(this, this);
        fetchingIntent();
        setViewsData();
        expandableCardVisibilityHandling();
        tipWindow = new TooltipWindow(VisitSummaryActivity_New.this);
        mBinding.tvtFollowUpDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
        mBinding.tvtFollowUpTime.setOnClickListener(v -> {
            // Get current time
            showTimePickerDialog();

        });

        setupVitalConfig();
        //for diagnstics section which is after vitals
        VisitDiagnosticsSummary visitDiagnosticsSummary = new VisitDiagnosticsSummary
                (mBinding, VisitSummaryActivity_New.this, null,
                        this, encounterVitals, mCommonVisitData);
        visitDiagnosticsSummary.initViews();
        setupVisibilityForSpecificFlavor();

        setupDiagnosticsConfig();
    }

    private void setupVisibilityForSpecificFlavor() {
        String physicalExamTitle = getString(R.string.physical_examination);
        String physicalExamSubtitle = getString(R.string.general_exams);
        //hiding associated symptoms,patient history for UNFPA
        if (BuildConfig.FLAVOR_client == FlavorKeys.UNFPA) {
            associated_sym_relative.setVisibility(View.GONE);
            mAssociateSymptomsLinearLayout.setVisibility(View.GONE);
            reports_relative.setVisibility(View.GONE);
            denies_relative.setVisibility(View.GONE);
            pathist_details_relative.setVisibility(View.GONE);
            physicalExamTitle = getString(R.string.obstetric_history);
            physicalExamSubtitle = getString(R.string.obstetric_history);
        } else if (BuildConfig.FLAVOR_client == FlavorKeys.KCDO) {
            physicalExamTitle = getString(R.string.relapse);
            physicalExamSubtitle = getString(R.string.relapse_info);
        }
        physical_exam_tv.setText(physicalExamTitle);
        physical_exam_info_tv.setText(physicalExamSubtitle);
    }

    private List<PatientVital> mPatientVitalList;
    private LinearLayout mHeightLinearLayout, mWeightLinearLayout, mBMILinearLayout, mBPLinearLayout, mPulseLinearLayout, mTemperatureLinearLayout, mSpo2LinearLayout, mRespiratoryRateLinearLayout, mBloodGroupLinearLayout;
    private LinearLayout mRandomGlucoseLinearLayout, mFastingGlucoseLinearLayout, mPostPrandialLinearLayout, mHemoglobinLinearLayout, mUricAcidLinearLayout, mCholestrolLinearLayout;
    private List<Diagnostics> mPatientDiagnosticsList;

    private void setupVitalConfig() {
        mHeightLinearLayout = findViewById(R.id.ll_height_container);
        mWeightLinearLayout = findViewById(R.id.ll_weight_container);
        mBMILinearLayout = findViewById(R.id.ll_bmi);
        mBPLinearLayout = findViewById(R.id.ll_bp_container);
        mPulseLinearLayout = findViewById(R.id.ll_pulse_container);
        mTemperatureLinearLayout = findViewById(R.id.ll_temperature_container);
        mSpo2LinearLayout = findViewById(R.id.ll_spo2_container);
        mRespiratoryRateLinearLayout = findViewById(R.id.ll_respiratory_rate_container);
        mBloodGroupLinearLayout = findViewById(R.id.ll_blood_group_container);

        PatientVitalRepository repository = new PatientVitalRepository(ConfigDatabase.getInstance(this).patientVitalDao());
        PatientVitalViewModelFactory factory = new PatientVitalViewModelFactory(repository);
        PatientVitalViewModel patientVitalViewModel = new ViewModelProvider(this, factory).get(PatientVitalViewModel.class);
        patientVitalViewModel.getAllEnabledLiveFields()
                .observe(this, it -> {
                            mPatientVitalList = it;
                            CustomLog.v(TAG, new Gson().toJson(mPatientVitalList));
                            updateUI();
                        }
                );
    }

    private void updateUI() {
        mHeightLinearLayout.setVisibility(View.GONE);
        mWeightLinearLayout.setVisibility(View.GONE);
        mBMILinearLayout.setVisibility(View.GONE);
        mBPLinearLayout.setVisibility(View.GONE);
        mPulseLinearLayout.setVisibility(View.GONE);
        mTemperatureLinearLayout.setVisibility(View.GONE);
        mSpo2LinearLayout.setVisibility(View.GONE);
        mRespiratoryRateLinearLayout.setVisibility(View.GONE);

        mBloodGroupLinearLayout.setVisibility(View.GONE);
        for (PatientVital patientVital : mPatientVitalList) {
            CustomLog.v(TAG, patientVital.getName() + "\t" + patientVital.getVitalKey());

            if (patientVital.getVitalKey().equals(PatientVitalConfigKeys.HEIGHT)) {
                mHeightLinearLayout.setVisibility(View.VISIBLE);

            } else if (patientVital.getVitalKey().equals(PatientVitalConfigKeys.WEIGHT)) {
                mWeightLinearLayout.setVisibility(View.VISIBLE);


            } else if (patientVital.getVitalKey().equals(PatientVitalConfigKeys.BMI)) {
                mBMILinearLayout.setVisibility(View.VISIBLE);

            } else if (patientVital.getVitalKey().equals(PatientVitalConfigKeys.SBP) || patientVital.getVitalKey().equals(PatientVitalConfigKeys.DBP)) {
                mBPLinearLayout.setVisibility(View.VISIBLE);
            } else if (patientVital.getVitalKey().equals(PatientVitalConfigKeys.PULSE)) {
                mPulseLinearLayout.setVisibility(View.VISIBLE);

            } else if (patientVital.getVitalKey().equals(PatientVitalConfigKeys.TEMPERATURE)) {
                mTemperatureLinearLayout.setVisibility(View.VISIBLE);

            } else if (patientVital.getVitalKey().equals(PatientVitalConfigKeys.SPO2)) {
                mSpo2LinearLayout.setVisibility(View.VISIBLE);

            } else if (patientVital.getVitalKey().equals(PatientVitalConfigKeys.RESPIRATORY_RATE)) {
                mRespiratoryRateLinearLayout.setVisibility(View.VISIBLE);

            } else if (patientVital.getVitalKey().equals(PatientVitalConfigKeys.BLOOD_TYPE)) {
                mBloodGroupLinearLayout.setVisibility(View.VISIBLE);

            }
        }
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Create TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(context,
                (view, hourOfDay, minute1) -> {
                    selectedFollowupTime = hourOfDay + ":" + minute1;
                    mBinding.tvtFollowUpTime.setText(selectedFollowupTime);
                }, hour, minute, true);
        timePickerDialog.show();
        timePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.colorPrimary)); // Change to your desired color
        timePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.colorPrimary));
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedFollowupDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                    mBinding.tvtFollowUpDate.setText(selectedFollowupDate);
                },
                year, month, day);

        // Disable past dates
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

        // Handling the Cancel button click
        datePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> {
            if (which == DatePickerDialog.BUTTON_NEGATIVE) {
                // Handle the cancel button action here if needed
                dialog.dismiss();
            }
        });

        datePickerDialog.show();
        // Change button colors dynamically after the dialog is shown
        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.colorPrimary)); // Change to your desired color
        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.colorPrimary));
    }

    private void setupSpecialization() {
        ConfigDatabase db = ConfigDatabase.getInstance(getApplicationContext());
        SpecializationRepository repository = new SpecializationRepository(db.specializationDao());
        viewModel = new ViewModelProvider(this, new SpecializationViewModelFactory(repository)).get(SpecializationViewModel.class);
        viewModel.fetchSpecialization().observe(this, specializations -> {
            CustomLog.d(TAG, new Gson().toJson(specializations));
            setupSpecializationDataSpinner(specializations);
            setupDiagnosisData();
            setupTypeOfConsultationSpinner();
            setFacilityToVisitSpinner();
            setSeveritySpinner();
            String followupValue = fetchValueFromLocalDb(visitUUID);
            if (!TextUtils.isEmpty(followupValue)) {
                mBinding.tvViewFollowUpDateTime.setText(followupValue);
            }
        });
    }


    private void fetchingIntent() {
        sessionManager = new SessionManager(getApplicationContext());
        sessionManager1 = new SessionManager(this);
        appLanguage = sessionManager1.getAppLanguage();
        if (!appLanguage.equalsIgnoreCase("")) {
            setLocale(appLanguage);
        }

        // todo: uncomment this block later for testing it is commented.
        final Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            if (intent.hasExtra("CommonVisitData")) {
                mCommonVisitData = intent.getExtras().getParcelable("CommonVisitData");

                visitUuid = mCommonVisitData.getVisitUuid();

                encounterVitals = mCommonVisitData.getEncounterUuidVitals();
                encounterUuidAdultIntial = mCommonVisitData.getEncounterUuidAdultIntial();
                EncounterAdultInitial_LatestVisit = mCommonVisitData.getEncounterAdultInitialLatestVisit();

                patientUuid = mCommonVisitData.getPatientUuid();
                patientGender = mCommonVisitData.getPatientGender();
                patientName = mCommonVisitData.getPatientName();
                float_ageYear_Month = mCommonVisitData.getPatientAgeYearMonth();
                intentTag = mCommonVisitData.getIntentTag();

                isPastVisit = mCommonVisitData.isPastVisit();
            } else {
                visitUuid = intent.getStringExtra("visitUuid");
                mCommonVisitData = new CommonVisitData();
                mCommonVisitData.setVisitUuid(visitUuid);

                encounterVitals = intent.getStringExtra("encounterUuidVitals");
                mCommonVisitData.setEncounterUuidVitals(encounterVitals);
                encounterUuidAdultIntial = intent.getStringExtra("encounterUuidAdultIntial");
                mCommonVisitData.setEncounterUuidAdultIntial(encounterUuidAdultIntial);
                EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");
                mCommonVisitData.setEncounterAdultInitialLatestVisit(EncounterAdultInitial_LatestVisit);

                patientUuid = intent.getStringExtra("patientUuid");
                mCommonVisitData.setPatientUuid(patientUuid);
                patientGender = intent.getStringExtra("gender");
                mCommonVisitData.setPatientGender(patientGender);
                patientName = intent.getStringExtra("name");
                mCommonVisitData.setPatientName(patientName);
                float_ageYear_Month = intent.getFloatExtra("float_ageYear_Month", 0);
                mCommonVisitData.setPatientAgeYearMonth(float_ageYear_Month);


                intentTag = intent.getStringExtra("tag");
                mCommonVisitData.setIntentTag(intentTag);

                isPastVisit = intent.getBooleanExtra("pastVisit", false);
                mCommonVisitData.setPastVisit(isPastVisit);
            }


            mSharedPreference = this.getSharedPreferences("visit_summary", Context.MODE_PRIVATE);
            try {
                hasPrescription = new EncounterDAO().isPrescriptionReceived(visitUuid);
                Timber.tag(TAG).d("has prescription main::%s", hasPrescription);
            } catch (DAOException e) {
                CustomLog.e(TAG, e.getMessage());
                throw new RuntimeException(e);
            }

            Set<String> selectedExams = sessionManager.getVisitSummary(patientUuid);
            if (physicalExams == null) physicalExams = new ArrayList<>();
            physicalExams.clear();
            if (selectedExams != null && !selectedExams.isEmpty()) {
                physicalExams.addAll(selectedExams);
            }


            queryData(String.valueOf(patientUuid));
            //generateAndViewBillData();
        }


        // receiver
        registerBroadcastReceiverDynamically();
        registerDownloadPrescription();
        if (!sessionManager.getLicenseKey().isEmpty()) hasLicense = true;

        // past visit checking based on intent - start
        if (isPastVisit) {
            editVitals.setVisibility(View.GONE);
            editComplaint.setVisibility(View.GONE);
            cc_details_edit.setVisibility(View.GONE);
            ass_symp_edit.setVisibility(View.GONE);
            editPhysical.setVisibility(View.GONE);
            editFamHist.setVisibility(View.GONE);
            editMedHist.setVisibility(View.GONE);
            editAddDocs.setVisibility(View.GONE);
            uploadButton.setVisibility(View.GONE);
            mBinding.layoutVisitSummarySections.imagebuttonEditDiagnostics.setVisibility(View.GONE);
//            btnSignSubmit.setVisibility(View.GONE);// todo: uncomment handle later.
            invalidateOptionsMenu();
        } else {
            if (visitUuid != null && !visitUuid.isEmpty()) {


                String visitIDorderBy = "startdate";
                String visitIDSelection = "uuid = ?";
                String[] visitIDArgs = {visitUuid};
                SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
                final Cursor visitIDCursor = db.query("tbl_visit", null, visitIDSelection, visitIDArgs, null, null, visitIDorderBy);
                if (visitIDCursor != null && visitIDCursor.moveToFirst() && visitIDCursor.getCount() > 0) {
                    visitIDCursor.moveToFirst();
                    visitUUID = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("uuid"));
                }
                if (visitIDCursor != null) visitIDCursor.close();
                if (visitUUID != null && !visitUUID.isEmpty()) {
                    addDownloadButton();
                }
            }
        }
        // past visit checking based on intent - end

        showVisitID();  // display visit ID.

        generateAndViewBillData();
        if (intentTag != null && !intentTag.isEmpty()) {


            boolean isCompletedExitedSurvey = false;
            boolean isPrescriptionReceived = false;
            isVisitSpecialityExists = speciality_row_exist_check(visitUUID);
            try {
                isCompletedExitedSurvey = new EncounterDAO().isCompletedExitedSurvey(visitUUID);
                isPrescriptionReceived = new EncounterDAO().isPrescriptionReceived(visitUUID);
            } catch (DAOException e) {
                e.printStackTrace();
                CustomLog.e(TAG, e.getMessage());
            }
            boolean isAllowForEdit = !isVisitSpecialityExists; //&& !isCompletedExitedSurvey && isPrescriptionReceived;
            // Edit btn visibility based on user coming from Visit Details screen - Start
            //if (intentTag.equalsIgnoreCase("VisitDetailsActivity")) {
            if (!isAllowForEdit) {
                editVitals.setVisibility(View.GONE);
                editComplaint.setVisibility(View.GONE);
                cc_details_edit.setVisibility(View.GONE);
                ass_symp_edit.setVisibility(View.GONE);
                editPhysical.setVisibility(View.GONE);
                editFamHist.setVisibility(View.GONE);
                editMedHist.setVisibility(View.GONE);
                editAddDocs.setVisibility(View.GONE);
                add_additional_doc.setVisibility(View.GONE);

                if (BuildConfig.FLAVOR_client == FlavorKeys.UNFPA) {
                    mBinding.diagnosisCard.setVisibility(View.GONE);
                    mBinding.diagnosisVdCard.setVisibility(View.VISIBLE);

                    mBinding.typeOfConsultationCard.setVisibility(View.GONE);
                    mBinding.consultationTypeVdCard.setVisibility(View.VISIBLE);
                }

                mBinding.layoutVisitSummarySections.imagebuttonEditDiagnostics.setVisibility(View.GONE);
                btn_bottom_printshare.setVisibility(View.VISIBLE);
                btn_bottom_vs.setVisibility(View.GONE);

                doc_speciality_card.setVisibility(View.GONE);
                special_vd_card.setVisibility(View.VISIBLE);

                mBinding.cvFacilityToVisitDoc.setVisibility(View.VISIBLE);
                mBinding.cvFacilityToVisit.setVisibility(View.GONE);

                mBinding.cvSeverityDoc.setVisibility(View.VISIBLE);
                mBinding.cvSeverity.setVisibility(View.GONE);

                mBinding.tvViewFollowUpDateTime.setVisibility(View.VISIBLE);
                mBinding.llDateTime.setVisibility(View.GONE);

                // vs_add_notes.setVisibility(View.GONE);

                addnotes_vd_card.setVisibility(View.VISIBLE);
                tilAdditionalNotesVS.setVisibility(View.GONE);
                tvAddNotesValueVS.setVisibility(View.VISIBLE);
                addnotes_value = visitAttributeListDAO.getVisitAttributesList_specificVisit(visitUuid, ADDITIONAL_NOTES);
                if (!addnotes_value.equalsIgnoreCase("")) {
                    if (addnotes_value.equalsIgnoreCase("No notes added for Doctor.")) {
                        tvAddNotesValueVS.setText(getString(R.string.no_notes_added_for_doctor));
                    } else tvAddNotesValueVS.setText(addnotes_value);
                } else {
                    addnotes_value = getString(R.string.no_notes_added_for_doctor);  // "No notes added for Doctor."
                    tvAddNotesValueVS.setText(addnotes_value);
                }
            } else {
                //TODO : Hide for beta release
                /*editVitals.setVisibility(View.VISIBLE);
                editComplaint.setVisibility(View.VISIBLE);
                cc_details_edit.setVisibility(View.VISIBLE);
                ass_symp_edit.setVisibility(View.VISIBLE);
                editPhysical.setVisibility(View.VISIBLE);
                editFamHist.setVisibility(View.VISIBLE);
                editMedHist.setVisibility(View.VISIBLE);
                editAddDocs.setVisibility(View.VISIBLE);*/
                editVitals.setVisibility(View.VISIBLE);
                editComplaint.setVisibility(View.VISIBLE);
                //cc_details_edit.setVisibility(View.VISIBLE);
                //ass_symp_edit.setVisibility(View.VISIBLE);
                editPhysical.setVisibility(View.VISIBLE);
                editFamHist.setVisibility(View.VISIBLE);
                editMedHist.setVisibility(View.VISIBLE);
                editAddDocs.setVisibility(View.VISIBLE);

                add_additional_doc.setVisibility(View.VISIBLE);

                btn_bottom_printshare.setVisibility(View.GONE);
                btn_bottom_vs.setVisibility(View.VISIBLE);

                if (BuildConfig.FLAVOR_client == FlavorKeys.UNFPA) {
                    mBinding.diagnosisCard.setVisibility(View.VISIBLE);
                    mBinding.diagnosisVdCard.setVisibility(View.GONE);

                    mBinding.typeOfConsultationCard.setVisibility(View.VISIBLE);
                    mBinding.consultationTypeVdCard.setVisibility(View.GONE);
                }

                doc_speciality_card.setVisibility(View.VISIBLE);
                special_vd_card.setVisibility(View.GONE);
                // vs_add_notes.setVisibility(View.VISIBLE);

                addnotes_vd_card.setVisibility(View.VISIBLE);
                tilAdditionalNotesVS.setVisibility(View.VISIBLE);
                tvAddNotesValueVS.setVisibility(View.GONE);
                mBinding.layoutVisitSummarySections.imagebuttonEditDiagnostics.setVisibility(View.VISIBLE);

            }
            // Edit btn visibility based on user coming from Visit Details screen - End

        }


        if (!isVisitSpecialityExists) {
            doc_speciality_card.setVisibility(View.VISIBLE);
            special_vd_card.setVisibility(View.GONE);


            flag.setEnabled(true);
            flag.setClickable(true);
        } else {
            flag.setEnabled(false);
            flag.setClickable(false);
        }
        btn_bottom_printshare.setVisibility(View.GONE);
        btn_bottom_vs.setVisibility(View.VISIBLE);
        CustomLog.d(TAG, "has prescription::%s", hasPrescription);
        updateUIState();

        //here we changing the appointment button behavior
        //based on appointment status
        if (new AppointmentDAO().checkAppointmentStatus(visitUUID).equals(AppConstants.CANCELLED)) {
            btnAppointment.setText(getString(R.string.appointment));
        } else if (new AppointmentDAO().checkAppointmentStatus(visitUUID).equals(AppConstants.BOOKED)) {
            btnAppointment.setText(getString(R.string.reschedule));
            doesAppointmentExist = true;
        }


        setupDiagnosisData();
        setupTypeOfConsultationSpinner();
    }

    private void updateUIState() {
        if (hasPrescription) {
            if (BuildConfig.FLAVOR_client == FlavorKeys.UNFPA) {
                mBinding.diagnosisCard.setVisibility(View.GONE);
                mBinding.diagnosisVdCard.setVisibility(View.VISIBLE);

                mBinding.typeOfConsultationCard.setVisibility(View.GONE);
                mBinding.consultationTypeVdCard.setVisibility(View.VISIBLE);
            }
            doc_speciality_card.setVisibility(View.GONE);
            special_vd_card.setVisibility(View.VISIBLE);


            mBinding.cvFacilityToVisitDoc.setVisibility(View.VISIBLE);
            mBinding.cvFacilityToVisit.setVisibility(View.GONE);

            mBinding.cvSeverityDoc.setVisibility(View.VISIBLE);
            mBinding.cvSeverity.setVisibility(View.GONE);

            mBinding.tvViewFollowUpDateTime.setVisibility(View.VISIBLE);
            mBinding.llDateTime.setVisibility(View.GONE);

            btn_bottom_printshare.setVisibility(View.VISIBLE);
            btn_bottom_vs.setVisibility(View.GONE);

            add_additional_doc.setVisibility(View.GONE);
            editAddDocs.setVisibility(View.GONE);
        } else {
            isVisitSpecialityExists = speciality_row_exist_check(visitUUID);
            int visibility = isVisitSpecialityExists ? View.GONE : View.VISIBLE;
            add_additional_doc.setVisibility(visibility);
            editAddDocs.setVisibility(visibility);
            if (recyclerViewAdapter != null) {
                recyclerViewAdapter.hideCancelBtnAddDoc(visibility == View.GONE);
            }
        }
    }

    private int mOpenCount = 0;

    private void expandableCardVisibilityHandling() {
        openall_btn.setOnClickListener(v -> {

            if (mOpenCount == 0) {
                openall_btn.setText(getResources().getString(R.string.close_all));
                openall_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_keyboard_arrow_up_24, 0);
                vs_vitals_header_expandview.setVisibility(View.VISIBLE);
                vs_visitreason_header_expandview.setVisibility(View.VISIBLE);
                vs_phyexam_header_expandview.setVisibility(View.VISIBLE);
                vs_medhist_header_expandview.setVisibility(View.VISIBLE);
                vd_special_header_expandview.setVisibility(View.VISIBLE);
                vd_addnotes_header_expandview.setVisibility(View.VISIBLE);
                mBinding.vdDiagnosisHeaderExpandview.setVisibility(View.VISIBLE);
                mBinding.vdConsultationTypeHeaderExpandview.setVisibility(View.VISIBLE);
                mOpenCount = 6;
                mBinding.layoutVisitSummarySections.vsDiagnosticsHeaderExpandview.setVisibility(View.VISIBLE);
                mOpenCount = 7;
            } else {
                openall_btn.setText(getResources().getString(R.string.open_all));
                openall_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_keyboard_arrow_down_24, 0);
                vs_vitals_header_expandview.setVisibility(View.GONE);
                vs_visitreason_header_expandview.setVisibility(View.GONE);
                vs_phyexam_header_expandview.setVisibility(View.GONE);
                vs_medhist_header_expandview.setVisibility(View.GONE);
                vd_special_header_expandview.setVisibility(View.GONE);
                vd_addnotes_header_expandview.setVisibility(View.GONE);
                mBinding.layoutVisitSummarySections.vsDiagnosticsHeaderExpandview.setVisibility(View.GONE);
                mBinding.vdDiagnosisHeaderExpandview.setVisibility(View.GONE);
                mBinding.vdConsultationTypeHeaderExpandview.setVisibility(View.GONE);
                mOpenCount = 0;
            }

        });

        btn_up_header.setOnClickListener(v -> {
            if (vs_header_expandview.getVisibility() == View.VISIBLE)
                vs_header_expandview.setVisibility(View.GONE);
            else vs_header_expandview.setVisibility(View.VISIBLE);
        });


        vitals_header_relative.setOnClickListener(v -> {
            if (vs_vitals_header_expandview.getVisibility() == View.VISIBLE) {
                vs_vitals_header_expandview.setVisibility(View.GONE);
                mOpenCount--;
                if (mOpenCount == 0) {
                    openall_btn.setText(getResources().getString(R.string.open_all));
                    openall_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_keyboard_arrow_down_24, 0);
                }
            } else {
                mOpenCount++;
                vs_vitals_header_expandview.setVisibility(View.VISIBLE);
                openall_btn.setText(getResources().getString(R.string.close_all));
                openall_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_keyboard_arrow_up_24, 0);
            }
        });


        chiefcomplaint_header_relative.setOnClickListener(v -> {
            if (vs_visitreason_header_expandview.getVisibility() == View.VISIBLE) {
                vs_visitreason_header_expandview.setVisibility(View.GONE);
                mOpenCount--;
                if (mOpenCount == 0) {
                    openall_btn.setText(getResources().getString(R.string.open_all));
                    openall_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_keyboard_arrow_down_24, 0);
                }
            } else {
                mOpenCount++;
                vs_visitreason_header_expandview.setVisibility(View.VISIBLE);
                openall_btn.setText(getResources().getString(R.string.close_all));
                openall_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_keyboard_arrow_up_24, 0);
            }
        });


        physExam_header_relative.setOnClickListener(v -> {
            if (vs_phyexam_header_expandview.getVisibility() == View.VISIBLE) {
                vs_phyexam_header_expandview.setVisibility(View.GONE);
                mOpenCount--;
                if (mOpenCount == 0) {
                    openall_btn.setText(getResources().getString(R.string.open_all));
                    openall_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_keyboard_arrow_down_24, 0);
                }
            } else {
                mOpenCount++;
                vs_phyexam_header_expandview.setVisibility(View.VISIBLE);
                openall_btn.setText(getResources().getString(R.string.close_all));
                openall_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_keyboard_arrow_up_24, 0);
            }
        });


        pathistory_header_relative.setOnClickListener(v -> {
            if (vs_medhist_header_expandview.getVisibility() == View.VISIBLE) {
                vs_medhist_header_expandview.setVisibility(View.GONE);
                mOpenCount--;
                if (mOpenCount == 0) {
                    openall_btn.setText(getResources().getString(R.string.open_all));
                    openall_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_keyboard_arrow_down_24, 0);
                }
            } else {
                mOpenCount++;
                vs_medhist_header_expandview.setVisibility(View.VISIBLE);
                openall_btn.setText(getResources().getString(R.string.close_all));
                openall_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_keyboard_arrow_up_24, 0);
            }
        });


        special_vd_header_relative.setOnClickListener(v -> {
            if (vd_special_header_expandview.getVisibility() == View.VISIBLE) {
                vd_special_header_expandview.setVisibility(View.GONE);
                mOpenCount--;
                if (mOpenCount == 0) {
                    openall_btn.setText(getResources().getString(R.string.open_all));
                    openall_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_keyboard_arrow_down_24, 0);
                }
            } else {
                mOpenCount++;
                vd_special_header_expandview.setVisibility(View.VISIBLE);
                openall_btn.setText(getResources().getString(R.string.close_all));
                openall_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_keyboard_arrow_up_24, 0);
            }
        });
        mBinding.rlFacilityToVisitHeader.setOnClickListener(v -> {
            if (mBinding.rlFacilityToVisitHeaderExpandView.getVisibility() == View.VISIBLE) {
                mBinding.rlFacilityToVisitHeaderExpandView.setVisibility(View.GONE);
                mOpenCount--;
                if (mOpenCount == 0) {
                    openall_btn.setText(getResources().getString(R.string.open_all));
                    openall_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_keyboard_arrow_down_24, 0);
                }
            } else {
                mOpenCount++;
                mBinding.rlFacilityToVisitHeaderExpandView.setVisibility(View.VISIBLE);
                openall_btn.setText(getResources().getString(R.string.close_all));
                openall_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_keyboard_arrow_up_24, 0);
            }
        });
        mBinding.rlSeverityHeader.setOnClickListener(v -> {
            if (mBinding.rlSavertyHeaderExpandView.getVisibility() == View.VISIBLE) {
                mBinding.rlSavertyHeaderExpandView.setVisibility(View.GONE);
                mOpenCount--;
                if (mOpenCount == 0) {
                    openall_btn.setText(getResources().getString(R.string.open_all));
                    openall_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_keyboard_arrow_down_24, 0);
                }
            } else {
                mOpenCount++;
                mBinding.rlSavertyHeaderExpandView.setVisibility(View.VISIBLE);
                openall_btn.setText(getResources().getString(R.string.close_all));
                openall_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_keyboard_arrow_up_24, 0);
            }
        });


        addnotes_vd_header_relative.setOnClickListener(v -> {
            if (vd_addnotes_header_expandview.getVisibility() == View.VISIBLE) {
                vd_addnotes_header_expandview.setVisibility(View.GONE);
                mOpenCount--;
                if (mOpenCount == 0) {
                    openall_btn.setText(getResources().getString(R.string.open_all));
                    openall_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_keyboard_arrow_down_24, 0);
                }
            } else {
                mOpenCount++;
                vd_addnotes_header_expandview.setVisibility(View.VISIBLE);
                openall_btn.setText(getResources().getString(R.string.close_all));
                openall_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_keyboard_arrow_up_24, 0);
            }
        });

        mBinding.diagnosisVdHeaderRelative.setOnClickListener(v -> {
            if (mBinding.vdDiagnosisHeaderExpandview.getVisibility() == View.VISIBLE) {
                mBinding.vdDiagnosisHeaderExpandview.setVisibility(View.GONE);
                mOpenCount--;
                if (mOpenCount == 0) {
                    openall_btn.setText(getResources().getString(R.string.open_all));
                    openall_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_keyboard_arrow_down_24, 0);
                }
            } else {
                mOpenCount++;
                mBinding.vdDiagnosisHeaderExpandview.setVisibility(View.VISIBLE);
                openall_btn.setText(getResources().getString(R.string.close_all));
                openall_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_keyboard_arrow_up_24, 0);
            }
        });

        mBinding.consultationTypeVdHeaderRelative.setOnClickListener(v -> {
            if (mBinding.vdConsultationTypeHeaderExpandview.getVisibility() == View.VISIBLE) {
                mBinding.vdConsultationTypeHeaderExpandview.setVisibility(View.GONE);
                mOpenCount--;
                if (mOpenCount == 0) {
                    openall_btn.setText(getResources().getString(R.string.open_all));
                    openall_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_keyboard_arrow_down_24, 0);
                }
            } else {
                mOpenCount++;
                mBinding.vdConsultationTypeHeaderExpandview.setVisibility(View.VISIBLE);
                openall_btn.setText(getResources().getString(R.string.close_all));
                openall_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_keyboard_arrow_up_24, 0);
            }
        });
        mBinding.layoutVisitSummarySections.diagnosticsHeaderRelative.setOnClickListener(v -> {
            if (mBinding.layoutVisitSummarySections.vsDiagnosticsHeaderExpandview.getVisibility() == View.VISIBLE) {
                mBinding.layoutVisitSummarySections.vsDiagnosticsHeaderExpandview.setVisibility(View.GONE);
                mOpenCount--;
                if (mOpenCount == 0) {
                    openall_btn.setText(getResources().getString(R.string.open_all));
                    openall_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_keyboard_arrow_down_24, 0);
                }
            } else {
                mOpenCount++;
                mBinding.layoutVisitSummarySections.vsDiagnosticsHeaderExpandview.setVisibility(View.VISIBLE);
                openall_btn.setText(getResources().getString(R.string.close_all));
                openall_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_keyboard_arrow_up_24, 0);
            }
        });

    }

    private String complaintLocalString = "", physicalExamLocaleString = "", patientHistoryLocaleString = "", familyHistoryLocaleString = "";

    private void setViewsData() {
        physicalDoumentsUpdates();

        if (patientUuid != null && patientUuid.isEmpty()) {
            queryData(String.valueOf(patientUuid));

            // Patient Photo
            //1.
            try {
                profileImage = imagesDAO.getPatientProfileChangeTime(patientUuid);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                CustomLog.e(TAG, e.getMessage());
            }
        }

        //2.
        if (patient.getPatient_photo() == null || patient.getPatient_photo().equalsIgnoreCase("")) {
            if (NetworkConnection.isOnline(context)) {
                profilePicDownloaded(patient);
            }
        }
        //3.
        if (!profileImage.equalsIgnoreCase(profileImage1)) {
            if (NetworkConnection.isOnline(context)) {
                profilePicDownloaded(patient);
            }
        }

        if (patient.getPatient_photo() != null) {
            RequestBuilder<Drawable> requestBuilder = Glide.with(context).asDrawable().sizeMultiplier(0.3f);
            Glide.with(context).load(patient.getPatient_photo()).thumbnail(requestBuilder).centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(profile_image);
        } else {
            profile_image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.avatar1));
        }
        // photo - end

        // header title set
        nameView.setText(patientName);

        gender_tv = patientGender;
        setGenderAgeLocal(context, genderView, patient.getDate_of_birth(), patient.getGender(), sessionManager);

        if (patient.getOpenmrs_id() != null && !patient.getOpenmrs_id().isEmpty()) {
            idView.setText(patient.getOpenmrs_id());
        } else {
            idView.setText(getString(R.string.patient_not_registered));
        }

        mCHWname = findViewById(R.id.chw_details);
        mCHWname.setText(sessionManager.getChwname()); //session manager provider
        // header title set - end

        // vitals values set.
        if (height.getValue() != null) {
            if (height.getValue().trim().isEmpty() || height.getValue().trim().equals("0")) {
                heightView.setText(getResources().getString(R.string.no_information));
            } else {
                heightView.setText(height.getValue());
            }
        }

        if (weight.getValue() != null) {
            if (weight.getValue().trim().isEmpty() || weight.getValue().trim().equals("0"))
                weightView.setText(getResources().getString(R.string.no_information));
            else weightView.setText(weight.getValue());
        } else weightView.setText(getResources().getString(R.string.no_information));

        CustomLog.d(TAG, "onCreate: " + weight.getValue());
        if (weight.getValue() != null) {
            String mWeight = weight.getValue().split(" ")[0];
            String mHeight = height.getValue().split(" ")[0];
            if ((mHeight != null && mWeight != null) && !mHeight.isEmpty() && !mWeight.isEmpty()) {
                double numerator = Double.parseDouble(mWeight) * 10000;
                double denominator = Double.parseDouble(mHeight) * Double.parseDouble(mHeight);
                double bmi_value = numerator / denominator;
                mBMI = String.format(Locale.ENGLISH, "%.2f", bmi_value);
            } else {
                mBMI = "";
            }

            if (mBMI.trim().isEmpty() || mBMI.equalsIgnoreCase(""))
                bmiView.setText(getResources().getString(R.string.no_information));
            else bmiView.setText(mBMI);

        }

        String bpText = bpSys.getValue() + "/" + bpDias.getValue();
        if (bpText.equals("/")) {  //when new patient is being registered we get / for BP
            bpView.setText(getResources().getString(R.string.no_information));
        } else if (bpText.equalsIgnoreCase("null/null")) {
            //when we setup app and get data from other users, we get null/null from server...
            bpView.setText(getResources().getString(R.string.no_information));
        } else {
            bpView.setText(bpText);
        }

        if (pulse.getValue() != null) {
            if (pulse.getValue().trim().isEmpty() || pulse.getValue().trim().equals("0"))
                pulseView.setText(getResources().getString(R.string.no_information));
            else pulseView.setText(pulse.getValue());
        } else pulseView.setText(getResources().getString(R.string.no_information));

        if (spO2.getValue() != null) {
            if (spO2.getValue().trim().isEmpty() || spO2.getValue().trim().equals("0"))
                spO2View.setText(getResources().getString(R.string.no_information));
            else spO2View.setText(spO2.getValue());
        } else spO2View.setText(getResources().getString(R.string.no_information));

        if (mBloodGroupObsDTO.getValue() != null) {
            if (mBloodGroupObsDTO.getValue().trim().isEmpty() || mBloodGroupObsDTO.getValue().trim().equals("null"))
                mBloodGroupTextView.setText(getResources().getString(R.string.no_information));
            else
                mBloodGroupTextView.setText(VisitUtils.getBloodPressureEnStringFromCode(mBloodGroupObsDTO.getValue()));
        } else mBloodGroupTextView.setText(getResources().getString(R.string.no_information));


        // temperature - start
        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(Objects.requireNonNullElse(FileUtils.readFileRoot(CONFIG_FILE_NAME, this), String.valueOf(FileUtils.encodeJSON(this, CONFIG_FILE_NAME)))); //Load the config file
            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(VisitSummaryActivity_New.this, mFileName)));
            }
            if (obj.getBoolean("mCelsius")) {
                tempcel.setVisibility(View.VISIBLE);
                tempfaren.setVisibility(View.GONE);
                tempView.setText(temperature.getValue());
                CustomLog.d("temp", "temp_C: " + temperature.getValue());
            } else if (obj.getBoolean("mFahrenheit")) {
                tempfaren.setVisibility(View.VISIBLE);
                tempcel.setVisibility(View.GONE);
                if (temperature.getValue() != null && !temperature.getValue().isEmpty()) {
                    CustomLog.d("temp", "temp_F: " + tempView.getText().toString());
                    tempView.setText(convertCtoF(TAG, temperature.getValue()));
                }
            }

        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            CustomLog.e(TAG, e.getMessage());
        }
        // temperature - end

        jsonBasedPrescTitle();
        if (isRespiratory) {
            respiratoryText.setVisibility(View.VISIBLE);
            respiratory.setVisibility(View.VISIBLE);
        } else {
            respiratoryText.setVisibility(View.GONE);
            respiratory.setVisibility(View.GONE);
        }

        if (resp.getValue() != null) {
            if (resp.getValue().trim().isEmpty() || resp.getValue().trim().equals("0"))
                respiratory.setText(getResources().getString(R.string.no_information));
            else respiratory.setText(resp.getValue());
        } else respiratory.setText(getResources().getString(R.string.no_information));
        // vitals values set - end

        setQAData();

        // additional doc data
        ImagesDAO imagesDAO = new ImagesDAO();
        ArrayList<String> fileuuidList = new ArrayList<String>();
        ArrayList<File> fileList = new ArrayList<File>();

        if (encounterUuidAdultIntial != null) {
            try {
                fileuuidList = imagesDAO.getImageUuid(encounterUuidAdultIntial, UuidDictionary.COMPLEX_IMAGE_AD);
                for (String fileuuid : fileuuidList) {
                    String filename = AppConstants.IMAGE_PATH + fileuuid + ".jpg";
                    if (new File(filename).exists()) {
                        fileList.add(new File(filename));
                    }
                }
            } catch (DAOException e) {
                e.printStackTrace();
                CustomLog.e(TAG, e.getMessage());
            }
            rowListItem = new ArrayList<>();

            for (File file : fileList)
                rowListItem.add(new DocumentObject(file.getName(), file.getAbsolutePath()));

            RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(this);
            mAdditionalDocsRecyclerView.setHasFixedSize(true);
            mAdditionalDocsRecyclerView.setLayoutManager(linearLayoutManager);

            recyclerViewAdapter = new AdditionalDocumentAdapter(this, encounterUuidAdultIntial, rowListItem, AppConstants.IMAGE_PATH, this, isVisitSpecialityExists);
//            if (intentTag.equalsIgnoreCase("VisitDetailsActivity")) {
//
//            } else {
//                recyclerViewAdapter = new AdditionalDocumentAdapter(this, encounterUuidAdultIntial, rowListItem, AppConstants.IMAGE_PATH, this, false);
//            }

            mAdditionalDocsRecyclerView.setAdapter(recyclerViewAdapter);
            add_docs_title.setText(getResources().getString(R.string.add_additional_documents) + " (" + recyclerViewAdapter.getItemCount() + ")");


            editAddDocs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                /*Intent addDocs = new Intent(VisitSummaryActivity_New.this, AdditionalDocumentsActivity.class);
                addDocs.putExtra("patientUuid", patientUuid);
                addDocs.putExtra("visitUuid", visitUuid);
                addDocs.putExtra("encounterUuidVitals", encounterVitals);
                addDocs.putExtra("encounterUuidAdultIntial", encounterUuidAdultIntial);
                startActivity(addDocs);*/
                    selectImage();
                }
            });
            // additional doc data - end
        }


        // speciality data
        //if row is present i.e. if true is returned by the function then the spinner will be disabled.
        CustomLog.d("visitUUID", "onCreate_uuid: " + visitUuid);
        isVisitSpecialityExists = speciality_row_exist_check(visitUuid);
        if (isVisitSpecialityExists) {
            speciality_spinner.setEnabled(false);
            flag.setEnabled(false);
            flag.setClickable(false);
            //mBinding.btnGenerateBill.setVisibility(View.VISIBLE);
        } else {
            flag.setEnabled(true);
            flag.setClickable(true);
            //mBinding.btnGenerateBill.setVisibility(View.GONE);
        }


        // todo: speciality code comes in upload btn as well so add that too....later...
        // speciality data - end

        if (visitUuid != null) {
            // Priority data
            EncounterDAO encounterDAO = new EncounterDAO();
            String emergencyUuid = "";
            try {
                emergencyUuid = encounterDAO.getEmergencyEncounters(visitUuid, encounterDAO.getEncounterTypeUuid("EMERGENCY"));
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                CustomLog.e(TAG, e.getMessage());
            }

            if (!emergencyUuid.isEmpty() || !emergencyUuid.equalsIgnoreCase("")) {
                flag.setChecked(true);
                flag.setEnabled(false);
                priorityVisit = true;
            }
        }


        flag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                priorityVisit = isChecked;
                try {
                    EncounterDAO encounterDAO = new EncounterDAO();
                    encounterDAO.setEmergency(visitUuid, isChecked);
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    CustomLog.e(TAG, e.getMessage());
                }
            }
        });
        // Priority data - end

        // edit listeners - start
        editVitals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(VisitSummaryActivity_New.this, VisitCreationActivity.class);
//                intent1.putExtra("patientUuid", patientUuid);
//                intent1.putExtra("visitUuid", visitUuid);
//                intent1.putExtra("gender", patientGender);
//                intent1.putExtra("encounterUuidVitals", encounterVitals);
//                intent1.putExtra("encounterUuidAdultIntial", encounterUuidAdultIntial);
//                intent1.putExtra("name", patientName);
//                intent1.putExtra("tag", "edit");
//                intent1.putExtra("float_ageYear_Month", float_ageYear_Month);
//                intent1.putExtra("edit_for", VisitCreationActivity.STEP_1_VITAL);

                mCommonVisitData.setEditFor(VisitCreationActivity.STEP_1_VITAL);
                mCommonVisitData.setIntentTag("edit");
                intent1.putExtra("CommonVisitData", mCommonVisitData);

                //startActivity(intent1);
                mStartForEditVisit.launch(intent1);
            }
        });

        // complaint
        editComplaint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MaterialAlertDialogBuilder complaintDialog = new MaterialAlertDialogBuilder(VisitSummaryActivity_New.this);
                complaintDialog.setTitle(getString(R.string.visit_summary_complaint));
                final LayoutInflater inflater = getLayoutInflater();
                View convertView = inflater.inflate(R.layout.dialog_edit_entry, null);
                complaintDialog.setView(convertView);

                final TextView complaintText = convertView.findViewById(R.id.textView_entry);
                if (complaint.getValue() != null) {
                    complaintText.setText(Html.fromHtml(complaintLocalString));
                }
                complaintText.setEnabled(false);

                /*complaintDialog.setPositiveButton(getString(R.string.generic_manual_entry), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final MaterialAlertDialogBuilder textInput = new MaterialAlertDialogBuilder(VisitSummaryActivity_New.this);
                        //  textInput.setTitle(R.string.question_text_input);
                        final LayoutInflater inflater = LayoutInflater.from(VisitSummaryActivity_New.this);
                        View convertView = inflater.inflate(R.layout.dialog_edittext, null);
                        textInput.setView(convertView);

                        //   final EditText dialogEditText = new EditText(VisitSummaryActivity_New.this);
                        EditText dialogEditText = convertView.findViewById(R.id.editText_mobileno);
                        Button sharebtn = convertView.findViewById(R.id.sharebtn);
                        sharebtn.setVisibility(View.GONE);

                        if (complaint.getValue() != null) {
                            dialogEditText.setText(Html.fromHtml(complaint.getValue()));
                        } else {
                            dialogEditText.setText("");
                        }
                        CustomLog.v("complai", "complai: " + complaint.getValue());
                        //  textInput.setView(dialogEditText);
                        textInput.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                complaint.setValue(dialogEditText.getText().toString().replace("\n", "<br>"));
                                if (complaint.getValue() != null) {
                                    complaintText.setText(Html.fromHtml(complaint.getValue()));
                                    complaintView.setText(Html.fromHtml(complaint.getValue()));
                                }
                                updateDatabase(complaint.getValue(), UuidDictionary.CURRENT_COMPLAINT);
                                Dialog.dismiss();
                            }
                        });
                        textInput.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Dialog.dismiss();
                            }
                        });
                        AlertDialog alertDialog = textInput.create();
                        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
                        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
                        int width = VisitSummaryActivity_New.this.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
                        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
                        alertDialog.show();

                        Button pb = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        pb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                        pb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                        Button nb = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                        nb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                        nb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                        Button neutralb = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                        neutralb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                        neutralb.setTypeface(ResourcesCompat.getFont(VisitSummaryActivity_New.this, R.font.lato_bold));

                        IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity_New.this, alertDialog);
                        dialogInterface.dismiss();
                    }
                });*/

                complaintDialog.setNegativeButton(getString(R.string.edit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Deleting the old image in physcial examination
                        if (obsImgdir.exists()) {
                            ImagesDAO imagesDAO = new ImagesDAO();

                            try {
                                List<String> imageList = imagesDAO.getImages(encounterUuidAdultIntial, UuidDictionary.COMPLEX_IMAGE_PE);
                                for (String obsImageUuid : imageList) {
                                    String imageName = obsImageUuid + ".jpg";
                                    new File(obsImgdir, imageName).deleteOnExit();
                                }
                                imagesDAO.deleteConceptImages(encounterUuidAdultIntial, UuidDictionary.COMPLEX_IMAGE_PE);
                            } catch (DAOException e1) {
                                FirebaseCrashlytics.getInstance().recordException(e1);
                                CustomLog.e(TAG, e1.getMessage());
                            }
                        }

                        Intent intent1 = new Intent(VisitSummaryActivity_New.this, VisitCreationActivity.class);
//                        intent1.putExtra("patientUuid", patientUuid);
//                        intent1.putExtra("visitUuid", visitUuid);
//                        intent1.putExtra("gender", patientGender);
//                        intent1.putExtra("encounterUuidVitals", encounterVitals);
//                        intent1.putExtra("encounterUuidAdultIntial", encounterUuidAdultIntial);
//                        intent1.putExtra("name", patientName);
//                        intent1.putExtra("tag", "edit");
//                        intent1.putExtra("float_ageYear_Month", float_ageYear_Month);
//                        intent1.putExtra("edit_for", VisitCreationActivity.STEP_2_VISIT_REASON);

                        mCommonVisitData.setEditFor(VisitCreationActivity.STEP_3_VISIT_REASON);
                        mCommonVisitData.setIntentTag("edit");
                        intent1.putExtra("CommonVisitData", mCommonVisitData);
                        //startActivity(intent1);
                        mStartForEditVisit.launch(intent1);
                        dialogInterface.dismiss();
                    }
                });

                complaintDialog.setNeutralButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                //complaintDialog.show();
                AlertDialog alertDialog = complaintDialog.create();
                alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
                alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
                int width = VisitSummaryActivity_New.this.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
                alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
                alertDialog.show();

                Button pb = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                pb.setTextColor(ContextCompat.getColor(VisitSummaryActivity_New.this, R.color.colorPrimary));
                pb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                Button nb = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                nb.setTextColor(ContextCompat.getColor(VisitSummaryActivity_New.this, (R.color.colorPrimary)));
                nb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                Button neutralb = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                neutralb.setTextColor(ContextCompat.getColor(VisitSummaryActivity_New.this, (R.color.colorPrimary)));
                neutralb.setTypeface(ResourcesCompat.getFont(VisitSummaryActivity_New.this, R.font.lato_bold));

                IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity_New.this, alertDialog);
            }
        });

        // physical exam
        editPhysical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MaterialAlertDialogBuilder physicalDialog = new MaterialAlertDialogBuilder(VisitSummaryActivity_New.this);
                physicalDialog.setTitle(getString(R.string.visit_summary_on_examination));
                final LayoutInflater inflater = getLayoutInflater();
                View convertView = inflater.inflate(R.layout.dialog_edit_entry, null);
                physicalDialog.setView(convertView);

                final TextView physicalText = convertView.findViewById(R.id.textView_entry);
                if (phyExam.getValue() != null)
                    physicalText.setText(Html.fromHtml(physicalExamLocaleString));
                physicalText.setEnabled(false);

                /*physicalDialog.setPositiveButton(getString(R.string.generic_manual_entry), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final MaterialAlertDialogBuilder textInput = new MaterialAlertDialogBuilder(VisitSummaryActivity_New.this);
                        //  textInput.setTitle(R.string.question_text_input);
                        final LayoutInflater inflater = LayoutInflater.from(VisitSummaryActivity_New.this);
                        View convertView = inflater.inflate(R.layout.dialog_edittext, null);
                        textInput.setView(convertView);

                        //   final EditText dialogEditText = new EditText(VisitSummaryActivity_New.this);
                        EditText dialogEditText = convertView.findViewById(R.id.editText_mobileno);
                        Button sharebtn = convertView.findViewById(R.id.sharebtn);
                        sharebtn.setVisibility(View.GONE);

                        if (phyExam.getValue() != null)
                            dialogEditText.setText(Html.fromHtml(phyExam.getValue()));
                        else
                            dialogEditText.setText("");
                        //  textInput.setView(dialogEditText);
                        textInput.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                phyExam.setValue(dialogEditText.getText().toString().replace("\n", "<br>"));
                                if (phyExam.getValue() != null) {
                                    physicalText.setText(Html.fromHtml(phyExam.getValue()));
                                    physFindingsView.setText(Html.fromHtml(phyExam.getValue()));
                                }
                                updateDatabase(phyExam.getValue(), UuidDictionary.PHYSICAL_EXAMINATION);
                                Dialog.dismiss();
                            }
                        });
                        textInput.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Dialog.dismiss();
                            }
                        });
                        //  AlertDialog dialog = textInput.show();
                        AlertDialog alertDialog = textInput.create();
                        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
                        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
                        int width = VisitSummaryActivity_New.this.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
                        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
                        alertDialog.show();

                        Button pb = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        pb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                        pb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                        Button nb = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                        nb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                        nb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                        Button neutralb = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                        neutralb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                        neutralb.setTypeface(ResourcesCompat.getFont(VisitSummaryActivity_New.this, R.font.lato_bold));

                        IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity_New.this, alertDialog);
                        dialogInterface.dismiss();
                    }
                });*/

                physicalDialog.setNegativeButton(getString(R.string.edit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (obsImgdir.exists()) {
                            ImagesDAO imagesDAO = new ImagesDAO();

                            try {
                                List<String> imageList = imagesDAO.getImages(encounterUuidAdultIntial, UuidDictionary.COMPLEX_IMAGE_PE);
                                for (String obsImageUuid : imageList) {
                                    String imageName = obsImageUuid + ".jpg";
                                    new File(obsImgdir, imageName).deleteOnExit();
                                }
                                imagesDAO.deleteConceptImages(encounterUuidAdultIntial, UuidDictionary.COMPLEX_IMAGE_PE);
                            } catch (DAOException e1) {
                                FirebaseCrashlytics.getInstance().recordException(e1);
                                CustomLog.e(TAG, e1.getMessage());
                            }
                        }
                        Intent intent1 = new Intent(VisitSummaryActivity_New.this, VisitCreationActivity.class);
//                        intent1.putExtra("patientUuid", patientUuid);
//                        intent1.putExtra("visitUuid", visitUuid);
//                        intent1.putExtra("gender", patientGender);
//                        intent1.putExtra("encounterUuidVitals", encounterVitals);
//                        intent1.putExtra("encounterUuidAdultIntial", encounterUuidAdultIntial);
//                        intent1.putExtra("name", patientName);
//                        intent1.putExtra("tag", "edit");
//                        intent1.putExtra("float_ageYear_Month", float_ageYear_Month);
//                        intent1.putExtra("edit_for", VisitCreationActivity.STEP_3_PHYSICAL_EXAMINATION);

                        mCommonVisitData.setEditFor(VisitCreationActivity.STEP_4_PHYSICAL_EXAMINATION);
                        mCommonVisitData.setIntentTag("edit");
                        intent1.putExtra("CommonVisitData", mCommonVisitData);
                        //startActivity(intent1);
                        mStartForEditVisit.launch(intent1);
                        dialogInterface.dismiss();
                    }
                });

                physicalDialog.setNeutralButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                AlertDialog alertDialog = physicalDialog.create();
                alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
                alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
                int width = VisitSummaryActivity_New.this.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
                alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
                alertDialog.show();

                Button pb = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                pb.setTextColor(ContextCompat.getColor(VisitSummaryActivity_New.this, (R.color.colorPrimary)));
                pb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                Button nb = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                nb.setTextColor(ContextCompat.getColor(VisitSummaryActivity_New.this, (R.color.colorPrimary)));
                nb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                Button neutralb = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                neutralb.setTextColor(ContextCompat.getColor(VisitSummaryActivity_New.this, (R.color.colorPrimary)));
                neutralb.setTypeface(ResourcesCompat.getFont(VisitSummaryActivity_New.this, R.font.lato_bold));

                IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity_New.this, alertDialog);
            }
        });

        // medical history
        editMedHist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MaterialAlertDialogBuilder historyDialog = new MaterialAlertDialogBuilder(VisitSummaryActivity_New.this);
                historyDialog.setTitle(getString(R.string.visit_summary_medical_history));
                final LayoutInflater inflater = getLayoutInflater();
                View convertView = inflater.inflate(R.layout.dialog_edit_entry, null);
                historyDialog.setView(convertView);

                final TextView historyText = convertView.findViewById(R.id.textView_entry);
                if (patHistory.getValue() != null)
                    historyText.setText(Html.fromHtml(patientHistoryLocaleString));
                historyText.setEnabled(false);

                /*historyDialog.setPositiveButton(getString(R.string.generic_manual_entry), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final MaterialAlertDialogBuilder textInput = new MaterialAlertDialogBuilder(VisitSummaryActivity_New.this);
                        //  textInput.setTitle(R.string.question_text_input);
                        final LayoutInflater inflater = LayoutInflater.from(VisitSummaryActivity_New.this);
                        View convertView = inflater.inflate(R.layout.dialog_edittext, null);
                        textInput.setView(convertView);

                        //   final EditText dialogEditText = new EditText(VisitSummaryActivity_New.this);
                        EditText dialogEditText = convertView.findViewById(R.id.editText_mobileno);
                        Button sharebtn = convertView.findViewById(R.id.sharebtn);
                        sharebtn.setVisibility(View.GONE);

                        if (patHistory.getValue() != null)
                            dialogEditText.setText(Html.fromHtml(patHistory.getValue()));
                        else
                            dialogEditText.setText("");
                        //    textInput.setView(dialogEditText);
                        textInput.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //patHistory.setValue(dialogEditText.getText().toString());
                                patHistory.setValue(dialogEditText.getText().toString().replace("\n", "<br>"));

                                if (patHistory.getValue() != null) {
                                    historyText.setText(Html.fromHtml(patHistory.getValue()));
                                    patHistView.setText(Html.fromHtml(patHistory.getValue()));
                                }
                                updateDatabase(patHistory.getValue(), UuidDictionary.RHK_MEDICAL_HISTORY_BLURB);
                                Dialog.dismiss();
                            }
                        });
                        textInput.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Dialog.dismiss();
                            }
                        });
//                        AlertDialog dialog = textInput.show();
//                        IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity_New.this, dialog);
//                        dialogInterface.dismiss();

                        AlertDialog alertDialog = textInput.create();
                        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
                        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
                        int width = VisitSummaryActivity_New.this.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
                        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
                        alertDialog.show();

                        Button pb = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        pb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                        pb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                        Button nb = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                        nb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                        nb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                        Button neutralb = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                        neutralb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                        neutralb.setTypeface(ResourcesCompat.getFont(VisitSummaryActivity_New.this, R.font.lato_bold));

                        IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity_New.this, alertDialog);
                        dialogInterface.dismiss();
                    }
                });*/

                historyDialog.setNegativeButton(getString(R.string.edit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent1 = new Intent(VisitSummaryActivity_New.this, VisitCreationActivity.class);
//                        intent1.putExtra("patientUuid", patientUuid);
//                        intent1.putExtra("visitUuid", visitUuid);
//                        intent1.putExtra("gender", patientGender);
//                        intent1.putExtra("encounterUuidVitals", encounterVitals);
//                        intent1.putExtra("encounterUuidAdultIntial", encounterUuidAdultIntial);
//                        intent1.putExtra("name", patientName);
//                        intent1.putExtra("tag", "edit");
//                        intent1.putExtra("float_ageYear_Month", float_ageYear_Month);
//                        intent1.putExtra("edit_for", VisitCreationActivity.STEP_4_PAST_MEDICAL_HISTORY);

                        mCommonVisitData.setEditFor(VisitCreationActivity.STEP_5_PAST_MEDICAL_HISTORY);
                        mCommonVisitData.setIntentTag("edit");
                        intent1.putExtra("CommonVisitData", mCommonVisitData);
                        //startActivity(intent1);
                        mStartForEditVisit.launch(intent1);
                        dialogInterface.dismiss();
                    }
                });

                historyDialog.setNeutralButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

//                historyDialog.show();
                AlertDialog alertDialog = historyDialog.create();
                alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
                alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
                int width = VisitSummaryActivity_New.this.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
                alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
                alertDialog.show();

                Button pb = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                pb.setTextColor(ContextCompat.getColor(VisitSummaryActivity_New.this, (R.color.colorPrimary)));
                pb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                Button nb = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                nb.setTextColor(ContextCompat.getColor(VisitSummaryActivity_New.this, (R.color.colorPrimary)));
                nb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                Button neutralb = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                neutralb.setTextColor(ContextCompat.getColor(VisitSummaryActivity_New.this, (R.color.colorPrimary)));
                neutralb.setTypeface(ResourcesCompat.getFont(VisitSummaryActivity_New.this, R.font.lato_bold));

                IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity_New.this, alertDialog);
                //  dialogInterface.dismiss();
            }
        });

        // family history
        editFamHist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialAlertDialogBuilder famHistDialog = new MaterialAlertDialogBuilder(VisitSummaryActivity_New.this);
                //final MaterialAlertDialogBuilder famHistDialog = new MaterialAlertDialogBuilder(VisitSummaryActivity_New.this,R.style.AlertDialogStyle);
                famHistDialog.setTitle(getString(R.string.visit_summary_family_history));
                final LayoutInflater inflater = getLayoutInflater();
                View convertView = inflater.inflate(R.layout.dialog_edit_entry, null);
                famHistDialog.setView(convertView);

                final TextView famHistText = convertView.findViewById(R.id.textView_entry);
                if (famHistory.getValue() != null)
                    famHistText.setText(Html.fromHtml(familyHistoryLocaleString));
                famHistText.setEnabled(false);

                /*famHistDialog.setPositiveButton(getString(R.string.generic_manual_entry), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MaterialAlertDialogBuilder textInput = new MaterialAlertDialogBuilder(VisitSummaryActivity_New.this);
                        // final MaterialAlertDialogBuilder textInput = new MaterialAlertDialogBuilder(VisitSummaryActivity_New.this);
                        //  textInput.setTitle(R.string.question_text_input);
                        final LayoutInflater inflater = LayoutInflater.from(VisitSummaryActivity_New.this);
                        View convertView = inflater.inflate(R.layout.dialog_edittext, null);
                        textInput.setView(convertView);

                        //  final EditText dialogEditText = new EditText(VisitSummaryActivity_New.this);
                        EditText dialogEditText = convertView.findViewById(R.id.editText_mobileno);
                        Button sharebtn = convertView.findViewById(R.id.sharebtn);
                        sharebtn.setVisibility(View.GONE);

                        if (famHistory.getValue() != null)
                            dialogEditText.setText(Html.fromHtml(famHistory.getValue()));
                        else
                            dialogEditText.setText("");
                        //    textInput.setView(dialogEditText);
                        textInput.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //famHistory.setValue(dialogEditText.getText().toString());
                                famHistory.setValue(dialogEditText.getText().toString().replace("\n", "<br>"));

                                if (famHistory.getValue() != null) {
                                    famHistText.setText(Html.fromHtml(famHistory.getValue()));
                                    famHistView.setText(Html.fromHtml(famHistory.getValue()));
                                }
                                updateDatabase(famHistory.getValue(), UuidDictionary.RHK_FAMILY_HISTORY_BLURB);
                                Dialog.dismiss();
                            }
                        });
                        textInput.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Dialog.dismiss();
                            }
                        });
                      *//*  AlertDialog alertDialog = textInput.show();
                        dialogInterface.dismiss();
                        IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity_New.this, alertDialog);
*//*
                        AlertDialog alertDialog = textInput.create();
                        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
                        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
                        int width = VisitSummaryActivity_New.this.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
                        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
                        alertDialog.show();

                        Button pb = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        pb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                        pb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                        Button nb = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                        nb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                        nb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                        Button neutralb = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                        neutralb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                        neutralb.setTypeface(ResourcesCompat.getFont(VisitSummaryActivity_New.this, R.font.lato_bold));

                        IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity_New.this, alertDialog);
                        dialogInterface.dismiss();
                    }
                });*/

                famHistDialog.setNeutralButton(getString(R.string.generic_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                famHistDialog.setNegativeButton(R.string.edit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Intent intent1 = new Intent(VisitSummaryActivity_New.this, VisitCreationActivity.class);
//                        intent1.putExtra("patientUuid", patientUuid);
//                        intent1.putExtra("visitUuid", visitUuid);
//                        intent1.putExtra("gender", patientGender);
//                        intent1.putExtra("encounterUuidVitals", encounterVitals);
//                        intent1.putExtra("encounterUuidAdultIntial", encounterUuidAdultIntial);
//                        intent1.putExtra("name", patientName);
//                        intent1.putExtra("tag", "edit");
//                        intent1.putExtra("float_ageYear_Month", float_ageYear_Month);
//                        intent1.putExtra("edit_for", VisitCreationActivity.STEP_5_FAMILY_HISTORY);

                        mCommonVisitData.setEditFor(VisitCreationActivity.STEP_6_FAMILY_HISTORY);
                        mCommonVisitData.setIntentTag("edit");
                        intent1.putExtra("CommonVisitData", mCommonVisitData);

                        //startActivity(intent1);
                        mStartForEditVisit.launch(intent1);
                        dialogInterface.dismiss();
                    }
                });

//                famHistDialog.show();
                AlertDialog alertDialog = famHistDialog.create();
                alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
                alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
                int width = VisitSummaryActivity_New.this.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
                alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
                alertDialog.show();

                Button pb = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                pb.setTextColor(ContextCompat.getColor(VisitSummaryActivity_New.this, (R.color.colorPrimary)));
                // pb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                Button nb = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                nb.setTextColor(ContextCompat.getColor(VisitSummaryActivity_New.this, (R.color.colorPrimary)));
                //nb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                Button neutralb = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                neutralb.setTextColor(ContextCompat.getColor(VisitSummaryActivity_New.this, (R.color.colorPrimary)));
                neutralb.setTypeface(ResourcesCompat.getFont(VisitSummaryActivity_New.this, R.font.lato_bold));
                IntelehealthApplication.setAlertDialogCustomTheme(VisitSummaryActivity_New.this, alertDialog);
            }
        });
        // edit listeners - end


        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(NetworkConnection.isOnline(context)){
                visitSendDialog(context, ContextCompat.getDrawable(VisitSummaryActivity_New.this, R.drawable.dialog_close_visit_icon), getResources().getString(R.string.send_visit), getResources().getString(R.string.are_you_sure_you_want_to_send_visit), getResources().getString(R.string.yes), getResources().getString(R.string.no));
//                }else {
//                    Toast.makeText(context, R.string.this_feature_is_not_available_in_offline_mode, Toast.LENGTH_SHORT).show();
//                }
            }
        });
        // upload btn click - end

        // json based presc header - start
        jsonBasedPrescTitle();
        // json based presc header - end

        downloadbtn.setOnClickListener(v -> {
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
            Intent intent = new Intent(VisitSummaryActivity_New.this, HomeScreenActivity_New.class);
            startActivity(intent);
            if (filter_framelayout.getVisibility() == View.VISIBLE)
                filter_framelayout.setVisibility(View.GONE);
            else filter_framelayout.setVisibility(View.VISIBLE);
        });

        incomplete_act.setOnClickListener(v -> {
            // filter options
//            Intent intent = new Intent(VisitSummaryActivity_New.this, EndVisitActivity.class);
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
        // edit listeners - start
        mBinding.layoutVisitSummarySections.imagebuttonEditDiagnostics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(context, VisitCreationActivity.class);

                mCommonVisitData.setEditFor(VisitCreationActivity.STEP_2_DIAGNOSTICS);
                mCommonVisitData.setIntentTag("edit");
                intent1.putExtra("CommonVisitData", mCommonVisitData);

                //startActivity(intent1);
                mStartForEditVisit.launch(intent1);
            }
        });

    }

    private void setupSpecializationDataSpinner(List<Specialization> specializations) {
        //spinner is being populated with the speciality values...
//        ProviderAttributeLIstDAO providerAttributeLIstDAO = new ProviderAttributeLIstDAO();

//        List<String> items = providerAttributeLIstDAO.getAllValues();
        CustomLog.d("specc", "spec: " + visitUuid);
        String special_value = visitAttributeListDAO.getVisitAttributesList_specificVisit(visitUuid, SPECIALITY);
        //Hashmap to List<String> add all value
        SpecializationArrayAdapter stringArrayAdapter = new SpecializationArrayAdapter(this, specializations);
        speciality_spinner.setAdapter(stringArrayAdapter);
        //  if(getResources().getConfiguration().locale.getLanguage().equalsIgnoreCase("en")) {
//        if (items != null) {
        specializations.add(0, new Specialization("select_specialization_text",
                getString(R.string.select_specialization_text)));
//            stringArrayAdapter = new SpecializationArrayAdapter(this, specializations);
//            speciality_spinner.setAdapter(stringArrayAdapter);
//        } else {
//            stringArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.speciality_values));
//            speciality_spinner.setAdapter(stringArrayAdapter);
//        }

        if (special_value != null) {
            int spinner_position = stringArrayAdapter.getPosition(special_value);
            speciality_spinner.setSelection(spinner_position);
            Specialization sp = stringArrayAdapter.getItem(spinner_position);
            String displayValue = ResUtils.getStringResourceByName(this, sp.getSKey());
            vd_special_value.setText(" " + Node.bullet + "  " + displayValue);
            speciality_selected = special_value;
        }

        speciality_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    CustomLog.d("SPINNER", "SPINNER_Selected: " + adapterView.getItemAtPosition(i).toString());
                    Specialization specialization = (Specialization) view.getTag(R.id.speciality_spinner);
                    speciality_selected = specialization.getName();
                    String value = ResUtils.getStringResourceByName(VisitSummaryActivity_New.this, specialization.getSKey());
                    vd_special_value.setText(" " + Node.bullet + "  " + value);
                    CustomLog.d("SPINNER", "SPINNER_Selected_final: " + speciality_selected);
                    CustomLog.d("ResUtils", "SPINNER_Selected_final: " + value);
                } else {
                    speciality_selected = "";
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setupDiagnosisData() {
        String diagnosis = visitAttributeListDAO.getVisitAttributesList_specificVisit(visitUuid, DIAGNOSIS);
        if (!TextUtils.isEmpty(diagnosis)) {
            mBinding.vdDiagnosisValue.setText(" " + Node.bullet + "  " + diagnosis);
        } else {
            mBinding.vdDiagnosisValue.setText(getString(R.string.no_data_found));
        }
    }

    private void setupTypeOfConsultationSpinner() {
        String consultationType = visitAttributeListDAO.getVisitAttributesList_specificVisit(visitUuid, CONSULTATION_TYPE);
        if (!TextUtils.isEmpty(consultationType)) {
            mBinding.vdConsultationTypeValue.setText(" " + Node.bullet + "  " + consultationType);
        } else {
            mBinding.vdConsultationTypeValue.setText(getString(R.string.no_data_found));
        }

        mBinding.typeOfConsultationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    CustomLog.d("SPINNER", "SPINNER_Selected: " + adapterView.getItemAtPosition(i).toString());
                    selectedConsultationType = adapterView.getItemAtPosition(i).toString();
                } else {
                    selectedConsultationType = "";
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private List<FacilityToVisitModel> getFacilityList() {
        facilityList = new ArrayList<FacilityToVisitModel>();
        facilityList.add(new FacilityToVisitModel("0", "Select Facility"));
        facilityList.add(new FacilityToVisitModel("1", "Asha"));
        facilityList.add(new FacilityToVisitModel("2", "AWW"));
        facilityList.add(new FacilityToVisitModel("3", "HWC/AAM"));
        facilityList.add(new FacilityToVisitModel("4", "CHC"));
        facilityList.add(new FacilityToVisitModel("5", "DH"));
        facilityList.add(new FacilityToVisitModel("6", "Medical"));
        facilityList.add(new FacilityToVisitModel("7", "Collage AB - PVT"));
        facilityList.add(new FacilityToVisitModel("8", "Hospital Other"));
        return facilityList;
    }

    private List<String> getSeverityList() {
        severityList = new ArrayList<String>();
        severityList.add("Select Severity");
        severityList.add("Low");
        severityList.add("Normal");
        severityList.add("Moderate");
        severityList.add("High");
        severityList.add("Critical");
        return severityList;
    }

    private void setFacilityToVisitSpinner() {
        if (facilityList == null || facilityList.isEmpty()) {
            facilityList = getFacilityList();
        }
        String facility = visitAttributeListDAO.getVisitAttributesList_specificVisit(visitUuid, FACILITY);
        if (!TextUtils.isEmpty(facility)) {
            mBinding.tvFacilityToVisitValue.setText(" " + Node.bullet + "  " + facility);
        }

        FacilityToVisitArrayAdapter arrayAdapter = new FacilityToVisitArrayAdapter(this, facilityList);
        mBinding.spinnerFacilityToVisit.setAdapter(arrayAdapter);
        mBinding.spinnerFacilityToVisit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
//                    Timber.tag("SPINNER").d("SPINNER_Selected: %s", adapterView.getItemAtPosition(i).toString());
                    selectedFacilityToVisit = facilityList.get(i);
                } else {
                    selectedFacilityToVisit = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setSeveritySpinner() {
        if (severityList == null || severityList.isEmpty()) {
            severityList = getSeverityList();
        }

        SeverityArrayAdapter arrayAdapter = new SeverityArrayAdapter(this, severityList);

        String severity = visitAttributeListDAO.getVisitAttributesList_specificVisit(visitUuid, SEVERITY);
        if (!TextUtils.isEmpty(severity)) {
            mBinding.tvSavertyValue.setText(" " + Node.bullet + "  " + severity);
        }


        mBinding.spinnerSeverity.setAdapter(arrayAdapter);
        mBinding.spinnerSeverity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    selectedSeverity = severityList.get(i);
                } else {
                    selectedSeverity = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void showEndVisitConfirmationDialog() {
        if (!hasPrescription) {
            if (mFeatureActiveStatus.getRestrictEndVisit()) {
                //added restrictEndVisit because in NAS - we cant end the visit is prescription not shared by dr -Nas-ida migration
                DialogUtils dialogUtils = new DialogUtils();
                dialogUtils.showCommonDialog(this, R.drawable.dialog_close_visit_icon, context.getResources().getString(R.string.alert_label_txt), context.getResources().getString(R.string.prescription_notprovided_msg), true, context.getResources().getString(R.string.ok), context.getResources().getString(R.string.cancel), action -> {
                });
            } else {
                DialogUtils dialogUtils = new DialogUtils();
                dialogUtils.showCommonDialog(this, R.drawable.dialog_close_visit_icon, context.getResources().getString(R.string.confirm_end_visit_reason), context.getResources().getString(R.string.confirm_end_visit_reason_message), false, context.getResources().getString(R.string.confirm), context.getResources().getString(R.string.cancel), action -> {
                    if (action == DialogUtils.CustomDialogListener.POSITIVE_CLICK) {
                        checkIfAppointmentExistsForVisit(visitUUID);
                    }
                });
            }
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

    //    private void endVisit(){
//        if (!hasPrescription) {
//            checkIfAppointmentExistsForVisit(visitUUID);
//        } else {
//            triggerEndVisit();
//        }
//    }
    private void triggerEndVisit() {

        String vitalsUUID = fetchEncounterUuidForEncounterVitals(visitUUID);
        String adultInitialUUID = fetchEncounterUuidForEncounterAdultInitials(visitUUID);

        endVisit(context, visitUUID, patient.getUuid(), followUpDate, vitalsUUID, adultInitialUUID, "state", patient.getFirst_name() + " " + patient.getLast_name().substring(0, 1), "VisitDetailsActivity");
    }

    // permission code - start
    private void checkPerm() {
        if (checkAndRequestPermissions()) {
            try {
                if (hasPrescription) {
                    doWebViewPrint_downloadBtn();
                } else {
                    DialogUtils dialogUtils = new DialogUtils();
                    dialogUtils.showCommonDialog(VisitSummaryActivity_New.this, R.drawable.ui2_ic_warning_internet, getResources().getString(R.string.no_prescription_available), getResources().getString(R.string.no_prescription_title), true, getResources().getString(R.string.okay), null, new DialogUtils.CustomDialogListener() {
                        @Override
                        public void onDialogActionDone(int action) {
                        }
                    });
                }
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
                showPermissionDeniedAlert(permissions, 2);
            }
        } else if (requestCode == DIALOG_CAMERA_PERMISSION_REQUEST) {
            boolean allGranted = grantResults.length != 0;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                checkPerm(0);
            } else {
                showPermissionDeniedAlert(permissions, 0);
            }
        } else if (requestCode == DIALOG_GALLERY_PERMISSION_REQUEST) {
            boolean allGranted = grantResults.length != 0;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                checkPerm(1);
            } else {
                showPermissionDeniedAlert(permissions, 1);
            }
        }
    }

    private void showPermissionDeniedAlert(String[] permissions, int id) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(this);
        alertdialogBuilder.setMessage(R.string.reject_permission_results);
        alertdialogBuilder.setPositiveButton(R.string.retry_again, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (id == 2) checkPerm();
                else if (id == 0) checkPerm(0);
                else if (id == 1) checkPerm(1);
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

    private boolean checkAndRequestPermissions(int id) {
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (id == 0) {
            int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.CAMERA);
            }

            if (!listPermissionsNeeded.isEmpty()) {
                requestPermissions(listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), DIALOG_CAMERA_PERMISSION_REQUEST);
                return false;
            }

        }

        if (id == 1) {
//            int writeExternalStoragePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                writeExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES);
//            }

            int writeExternalStoragePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                writeExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES);
                if (writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES);
                }
            } else {
                if (writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                    listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }

            if (!listPermissionsNeeded.isEmpty()) {
                requestPermissions(listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), DIALOG_GALLERY_PERMISSION_REQUEST);
                return false;
            }

        }

        return true;
    }

    // permission code - end

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
            CustomLog.e(TAG, e.getMessage());
        }
    }

    private String showVisitID() {
        if (visitUUID != null && !visitUUID.isEmpty()) {
            String hideVisitUUID = visitUUID;
            hideVisitUUID = hideVisitUUID.substring(hideVisitUUID.length() - 4, hideVisitUUID.length());
            visitView.setText("XXXX" + hideVisitUUID);
        }
        return visitView.getText().toString();
    }

    private void showSelectSpeciliatyErrorDialog() {
        TextView t = (TextView) speciality_spinner.getSelectedView();
        if (t != null) {
            t.setError(getString(R.string.please_select_specialization_msg));
            t.setTextColor(Color.RED);
            showSpecialisationDialog();
        }
    }

    private void showSpecialisationDialog() {
        DialogUtils dialogUtils = new DialogUtils();
        dialogUtils.showCommonDialog(VisitSummaryActivity_New.this, R.drawable.ui2_ic_warning_internet, getResources().getString(R.string.please_select_specialization_msg), "", true, getResources().getString(R.string.okay), getResources().getString(R.string.cancel), new DialogUtils.CustomDialogListener() {
            @Override
            public void onDialogActionDone(int action) {

            }
        });
    }

    ActivityResultLauncher<Intent> cameraActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            String mCurrentPhotoPath = result.getData().getStringExtra("RESULT");
            saveImage(mCurrentPhotoPath);
        }
    });

    ActivityResultLauncher<Intent> galleryActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            if (result.getData() != null) {
                Uri selectedImage = result.getData().getData();
                String[] filePath = {MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                //Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
                CustomLog.v("path", picturePath + "");
                BitmapUtils.fileCompressed(picturePath);

                // copy & rename the file
                String finalImageName = UUID.randomUUID().toString();
                final String finalFilePath = AppConstants.IMAGE_PATH + finalImageName + ".jpg";
                BitmapUtils.copyFile(picturePath, finalFilePath);
                compressImageAndSave(finalFilePath);
            }
        }
    });


    // Permission - start
    private void checkPerm(int item) {
        if (item == 0) {
            if (checkAndRequestPermissions(item)) {
                Intent cameraIntent = new Intent(VisitSummaryActivity_New.this, CameraActivity.class);
                String imageName = UUID.randomUUID().toString();
                cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
                cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, AppConstants.IMAGE_PATH);
                cameraActivityResult.launch(cameraIntent);
            }
        } else if (item == 1) {
            if (checkAndRequestPermissions(item)) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryActivityResult.launch(intent);
            }
        }
    }

    // Permission - end
    private AlertDialog mImagePickerAlertDialog;

    /**
     * Open dialog to Select douments from Image and Camera as Per the Choices
     */
    private void selectImage() {
        mImagePickerAlertDialog = DialogUtils.showCommonImagePickerDialog(this, getString(R.string.additional_doc_image_picker_title), new DialogUtils.ImagePickerDialogListener() {
            @Override
            public void onActionDone(int action) {
                mImagePickerAlertDialog.dismiss();
                if (action == DialogUtils.ImagePickerDialogListener.CAMERA) {
                    checkPerm(action);

                } else if (action == DialogUtils.ImagePickerDialogListener.GALLERY) {
                    checkPerm(action);
                }
            }
        });


    }

    private void initUI() {
        // textview - start
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

        backArrow = findViewById(R.id.backArrow);
        refresh = findViewById(R.id.refresh);
        profile_image = findViewById(R.id.profile_image);
        downloadbtn = findViewById(R.id.downloadbtn);
        nameView = findViewById(R.id.textView_name_value);
        genderView = findViewById(R.id.textView_gender_value);
        //OpenMRS Id
        idView = findViewById(R.id.textView_id_value);
        visitView = findViewById(R.id.textView_visit_value);

        tilAdditionalNotesVS = findViewById(R.id.tilAdditionalNotesVS);
        etAdditionalNotesVS = findViewById(R.id.etAdditionalNotesVS);

//        android:hint="@string/leave_a_note_for_doctor"
        etAdditionalNotesVS.setHint(R.string.leave_a_note_for_doctor);
        etAdditionalNotesVS.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                etAdditionalNotesVS.setHint("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equalsIgnoreCase(""))
                    etAdditionalNotesVS.setHint(R.string.leave_a_note_for_doctor);
                else etAdditionalNotesVS.setHint("");
            }
        });
        // textview - end

        // up-down btn - start
        btn_up_header = findViewById(R.id.btn_up_header);
        openall_btn = findViewById(R.id.openall_btn);
        btn_up_vitals_header = findViewById(R.id.btn_up_vitals_header);
        vitals_header_relative = findViewById(R.id.vitals_header_relative);
        parentLayout = findViewById(R.id.parentLayout);
        btn_up_visitreason_header = findViewById(R.id.btn_up_visitreason_header);
        chiefcomplaint_header_relative = findViewById(R.id.chiefcomplaint_header_relative);
        btn_up_phyexam_header = findViewById(R.id.btn_up_phyexam_header);
        physExam_header_relative = findViewById(R.id.physExam_header_relative);
        btn_up_medhist_header = findViewById(R.id.btn_up_medhist_header);
        pathistory_header_relative = findViewById(R.id.pathistory_header_relative);
        btn_up_special_vd_header = findViewById(R.id.btn_up_special_vd_header);
        special_vd_header_relative = findViewById(R.id.special_vd_header_relative);
        associated_sym_relative = findViewById(R.id.associ_sym_relative);
        btn_up_addnotes_vd_header = findViewById(R.id.btn_up_addnotes_vd_header);
        addnotes_vd_header_relative = findViewById(R.id.addnotes_vd_header_relative);
        reports_relative = findViewById(R.id.reports_relative);
        denies_relative = findViewById(R.id.denies_relative);
        pathist_details_relative = findViewById(R.id.pathist_details_relative);

        vs_header_expandview = findViewById(R.id.vs_header_expandview);
        vs_vitals_header_expandview = findViewById(R.id.vs_vitals_header_expandview);
        vs_visitreason_header_expandview = findViewById(R.id.vs_visitreason_header_expandview);
        vs_phyexam_header_expandview = findViewById(R.id.vs_phyexam_header_expandview);
        vs_medhist_header_expandview = findViewById(R.id.vs_medhist_header_expandview);
        vd_special_header_expandview = findViewById(R.id.vd_special_header_expandview);
        vd_addnotes_header_expandview = findViewById(R.id.vd_addnotes_header_expandview);
        vs_add_notes = findViewById(R.id.vs_add_notes);
        tvAddNotesValueVS = findViewById(R.id.tvAddNotesValueVS);
        // up-down btn - end

        // vitals ids
        heightView = findViewById(R.id.textView_height_value);
        weightView = findViewById(R.id.textView_weight_value);
        pulseView = findViewById(R.id.textView_pulse_value);
        bpView = findViewById(R.id.textView_bp_value);
        tempView = findViewById(R.id.textView_temp_value);

        vd_special_value = findViewById(R.id.vd_special_value);
        doc_speciality_card = findViewById(R.id.doc_speciality_card);
        addnotes_vd_card = findViewById(R.id.addnotes_vd_card);
        special_vd_card = findViewById(R.id.special_vd_card);

        priority_hint = findViewById(R.id.priority_hint);

        priority_hint.setOnClickListener(v -> {
            if (!tipWindow.isTooltipShown())
                tipWindow.showToolTip(priority_hint, getResources().getString(R.string.priority_hint));

            //  Toast.makeText(context, R.string.priority_hint, Toast.LENGTH_SHORT).show();
//            Snackbar.make(parentLayout, R.string.priority_hint, Snackbar.LENGTH_SHORT).show();
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                /*Intent intent = new Intent(VisitSummaryActivity_New.this, HomeScreenActivity_New.class);
                startActivity(intent);*/
            }
        });


        tempfaren = findViewById(R.id.textView_temp_faren);
        tempcel = findViewById(R.id.textView_temp);

        spO2View = findViewById(R.id.textView_pulseox_value);
        mBloodGroupTextView = findViewById(R.id.textView_blood_group);
        respiratory = findViewById(R.id.textView_respiratory_value);
        respiratoryText = findViewById(R.id.textView_respiratory);
        bmiView = findViewById(R.id.textView_bmi_value);
        // vitals ids - end

        // complaint ids
        cc_recyclerview = findViewById(R.id.cc_recyclerview);


        complaintView = findViewById(R.id.textView_content_complaint);
        patientReports_txtview = findViewById(R.id.patientReports_txtview);
        patientDenies_txtview = findViewById(R.id.patientDenies_txtview);

        physical_exam_tv = findViewById(R.id.physical_exam_tv);
        physical_exam_info_tv = findViewById(R.id.physical_exam_info_tv);
        // complaint ids - end

        // Phys exam ids
        physFindingsView = findViewById(R.id.physFindingsView);
        mPhysicalExamsRecyclerView = findViewById(R.id.recy_physexam);
        physcialExaminationDownloadText = findViewById(R.id.physcial_examination_download);
        // Phys exam ids - end

        // medical history
        famHistView = findViewById(R.id.textView_content_famhist);
        patHistView = findViewById(R.id.textView_content_pathist);
        // medical history - end

        // additonal doc
        add_docs_title = findViewById(R.id.add_docs_title);
        mAdditionalDocsRecyclerView = findViewById(R.id.recy_additional_documents);
        editAddDocs = findViewById(R.id.imagebutton_edit_additional_document);
        // additonal doc - end

        // speciality ids
        speciality_spinner = findViewById(R.id.speciality_spinner);
        // speciality ids - end

        // priority id
        flag = findViewById(R.id.flaggedcheckbox);
        // priority id - end

        // edit - start
        editVitals = findViewById(R.id.imagebutton_edit_vitals);
        editComplaint = findViewById(R.id.imagebutton_edit_complaint);
        cc_details_edit = findViewById(R.id.cc_details_edit);
        ass_symp_edit = findViewById(R.id.ass_symp_edit);
        editPhysical = findViewById(R.id.imagebutton_edit_physexam);
        editFamHist = findViewById(R.id.imagebutton_edit_famhist);
        editMedHist = findViewById(R.id.imagebutton_edit_pathist);
        editAddDocs = findViewById(R.id.imagebutton_edit_additional_document);
        // edit - end

        // Bottom Buttons - start
        btn_bottom_printshare = findViewById(R.id.btn_bottom_printshare);   // linear: print - share
        /*btn_vs_print = findViewById(R.id.btn_vs_print);   // print
        btn_vs_share = findViewById(R.id.btn_vs_share);   // share*/
        mViewPrescriptionButton = findViewById(R.id.btnPrescriptionView);   // share*/

        btn_bottom_vs = findViewById(R.id.btn_bottom_vs);   // appointment - upload
        uploadButton = findViewById(R.id.btn_vs_sendvisit);

        mViewPrescriptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(VisitSummaryActivity_New.this, PrescriptionActivity.class);
                in.putExtra("patientname", patientName);
                in.putExtra("patientUuid", patientUuid);
                in.putExtra("patient_photo", patient.getPatient_photo());
                in.putExtra("visit_ID", visitUUID);
                in.putExtra("visit_startDate", "");
                in.putExtra("gender", patient.getGender());
                in.putExtra("encounterUuidVitals", encounterVitals);
                in.putExtra("encounterUuidAdultIntial", encounterUuidAdultIntial);
                String age = DateAndTimeUtils.getAge_FollowUp(patient.getDate_of_birth(), VisitSummaryActivity_New.this);

                in.putExtra("age", age);
                in.putExtra("tag", "VISITSUMMARY");
                in.putExtra("followupDate", "");
                in.putExtra("openmrsID", patient.getOpenmrs_id());

                startActivity(in);
            }
        });

        // Bottom Buttons - end
        refresh.setOnClickListener(v -> {
            syncNow(VisitSummaryActivity_New.this, refresh, syncAnimator);
        });

        // file set
        baseDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        obsImgdir = new File(AppConstants.IMAGE_PATH);

        add_additional_doc = findViewById(R.id.add_additional_doc);

        // navigation for book appointmnet
        btnAppointment = findViewById(R.id.btn_vs_appointment);
        btnAppointment.setOnClickListener(v -> {
            if (!NetworkConnection.isOnline(context)) {
                setAppointmentButtonStatus();
                Toast.makeText(context, R.string.this_feature_is_not_available_in_offline_mode, Toast.LENGTH_SHORT).show();
                return;
            }

            if (priorityVisit) {
                Toast.makeText(VisitSummaryActivity_New.this, getResources().getString(R.string.no_appointment_for_priority), Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isVisitSpecialityExists) {
                Toast.makeText(VisitSummaryActivity_New.this, getResources().getString(R.string.please_upload_visit), Toast.LENGTH_SHORT).show();
                return;
            }

            if (doesAppointmentExist) {
                String subtitle = getResources().getString(R.string.sure_to_reschedule_appointment, patientName);
                rescheduleAppointment(VisitSummaryActivity_New.this, getResources().getString(R.string.reschedule_appointment_new), subtitle, getResources().getString(R.string.yes), getResources().getString(R.string.no));
                return;
            }

            Intent in = new Intent(VisitSummaryActivity_New.this, ScheduleAppointmentActivity_New.class);
            in.putExtra("visitUuid", visitUuid);
            in.putExtra("patientUuid", patientUuid);
            in.putExtra("patientName", patientName);
            in.putExtra("appointmentId", 0);
            in.putExtra("actionTag", "new_schedule");
            in.putExtra("openMrsId", patient.getOpenmrs_id());
            in.putExtra("speciality", speciality_selected);
            in.putExtra("requestCode", AppConstants.EVENT_APPOINTMENT_BOOKING_FROM_VISIT_SUMMARY);
            mStartForScheduleAppointment.launch(in);
        });
    }


    private void rescheduleAppointment(VisitSummaryActivity_New context, String title, String subTitle, String positiveBtnTxt, String negativeBtnTxt) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.dialog_book_appointment_dialog_ui2, null);
        alertdialogBuilder.setView(convertView);
        ImageView icon = convertView.findViewById(R.id.iv_dialog_image);
        TextView dialog_title = convertView.findViewById(R.id.tv_title_book_app);
        TextView tvInfo = convertView.findViewById(R.id.tv_info_dialog_app);
        Button noButton = convertView.findViewById(R.id.button_no_appointment);
        Button yesButton = convertView.findViewById(R.id.btn_yes_appointment);

        icon.setImageDrawable(ContextCompat.getDrawable(VisitSummaryActivity_New.this, R.drawable.ui2_ic_book_app_red));

        dialog_title.setText(title);
        tvInfo.setText(Html.fromHtml(subTitle));
        yesButton.setText(positiveBtnTxt);
        noButton.setText(negativeBtnTxt);


        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg);
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

        noButton.setOnClickListener(v -> alertDialog.dismiss());

        yesButton.setOnClickListener(v -> {
            alertDialog.dismiss();
            askReasonForRescheduleAppointment(VisitSummaryActivity_New.this);
        });

        alertDialog.show();
    }

    private void askReasonForRescheduleAppointment(Context context) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.dialog_ask_reason_new_ui2, null);
        alertdialogBuilder.setView(convertView);

        final TextView titleTextView = convertView.findViewById(R.id.titleTv_new);
        titleTextView.setText(getString(R.string.please_select_your_reschedule_reason));
        final EditText reasonEtv = convertView.findViewById(R.id.reasonEtv_new);
        reasonEtv.setVisibility(View.GONE);
        final RadioButton rb1 = convertView.findViewById(R.id.rb_no_doctor);
        final RadioButton rb2 = convertView.findViewById(R.id.rb_no_patient);
        final RadioButton rb3 = convertView.findViewById(R.id.rb_other_ask);

        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg);
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

        final RadioGroup optionsRadioGroup = convertView.findViewById(R.id.rg_ask_reason);
        optionsRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_no_doctor) {
                    rb1.setButtonDrawable(ContextCompat.getDrawable(VisitSummaryActivity_New.this, R.drawable.ui2_ic_selected_green));
                    rb2.setButtonDrawable(ContextCompat.getDrawable(VisitSummaryActivity_New.this, R.drawable.ui2_ic_circle));
                    rb3.setButtonDrawable(ContextCompat.getDrawable(VisitSummaryActivity_New.this, R.drawable.ui2_ic_circle));
                    reasonEtv.setVisibility(View.GONE);
                    reasonEtv.setText(getString(R.string.doctor_is_not_available));
                    mEngReason = "Doctor is not available";
                } else if (checkedId == R.id.rb_no_patient) {
                    rb2.setButtonDrawable(ContextCompat.getDrawable(VisitSummaryActivity_New.this, R.drawable.ui2_ic_selected_green));
                    rb1.setButtonDrawable(ContextCompat.getDrawable(VisitSummaryActivity_New.this, R.drawable.ui2_ic_circle));
                    rb3.setButtonDrawable(ContextCompat.getDrawable(VisitSummaryActivity_New.this, R.drawable.ui2_ic_circle));
                    reasonEtv.setVisibility(View.GONE);
                    reasonEtv.setText(getString(R.string.patient_is_not_available));
                    mEngReason = "Patient is not available";
                } else if (checkedId == R.id.rb_other_ask) {
                    rb3.setButtonDrawable(ContextCompat.getDrawable(VisitSummaryActivity_New.this, R.drawable.ui2_ic_selected_green));
                    rb2.setButtonDrawable(ContextCompat.getDrawable(VisitSummaryActivity_New.this, R.drawable.ui2_ic_circle));
                    rb1.setButtonDrawable(ContextCompat.getDrawable(VisitSummaryActivity_New.this, R.drawable.ui2_ic_circle));
                    reasonEtv.setText("");
                    reasonEtv.setVisibility(View.VISIBLE);
                }
            }
        });

        final Button textView = convertView.findViewById(R.id.btn_save_ask);
        final Button btnCancel = convertView.findViewById(R.id.btn_cancel_ask);

        textView.setOnClickListener(v -> {
            alertDialog.dismiss();
            String reason = reasonEtv.getText().toString().trim();

            if (reason.isEmpty()) {
                Toast.makeText(VisitSummaryActivity_New.this, getString(R.string.please_enter_reason_txt), Toast.LENGTH_SHORT).show();
            } else {
                AppointmentInfo appointmentInfo = new AppointmentDAO().getAppointmentByVisitId(visitUUID);
                Intent in = new Intent(context, ScheduleAppointmentActivity_New.class);
                in.putExtra("actionTag", "rescheduleAppointment");
                in.putExtra("visitUuid", visitUUID);
                in.putExtra("patientUuid", patientUuid);
                in.putExtra("patientName", patientName);
                in.putExtra("appointmentId", appointmentInfo.getId());
                in.putExtra("openMrsId", patient.getOpenmrs_id());
                in.putExtra("app_start_date", appointmentInfo.getSlotDate());
                in.putExtra("app_start_time", appointmentInfo.getSlotTime());
                in.putExtra("app_start_day", appointmentInfo.getSlotDay());
                in.putExtra("rescheduleReason", mEngReason);
                in.putExtra("speciality", speciality_selected);
                in.putExtra("requestCode", AppConstants.EVENT_APPOINTMENT_BOOKING_FROM_VISIT_SUMMARY);
                mStartForScheduleAppointment.launch(in);
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();

            }
        });
        alertDialog.show();

    }


    private final ActivityResultLauncher<Intent> mStartForScheduleAppointment = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        int resultCode = result.getResultCode();
        boolean appointmentResult = sessionManager.getAppointmentResult();
        if (resultCode == AppConstants.EVENT_APPOINTMENT_BOOKING_FROM_VISIT_SUMMARY) {
            navigateToMyAppointment();
        }
        //sometimes RESULT_CANCELED calls even we need to handle event
        //that's why added the logic when result is RESULT_CANCELED
        else if (resultCode == RESULT_CANCELED) {
            if (appointmentResult) {
                navigateToMyAppointment();
            }
        }
        sessionManager.setAppointmentResult(false);
    });

    void navigateToMyAppointment() {
        if (!isFinishing() && !isDestroyed()) {
            Toast.makeText(VisitSummaryActivity_New.this, getResources().getString(R.string.appointment_booked_successfully), Toast.LENGTH_LONG).show();
            Intent in = new Intent(VisitSummaryActivity_New.this, MyAppointmentActivityNew.class);
            startActivity(in);
            finish();
        } else {
            CustomLog.d("CCCCCV", "Destry" + VisitSummaryActivity_New.this);
        }
    }

    private void sharePresc() {
        if (hasPrescription) {
            MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
            final LayoutInflater inflater = LayoutInflater.from(context);
            View convertView = inflater.inflate(R.layout.dialog_sharepresc, null);
            alertdialogBuilder.setView(convertView);

            EditText editText = convertView.findViewById(R.id.editText_mobileno);
            Button sharebtn = convertView.findViewById(R.id.sharebtn);


            String partial_whatsapp_presc_url = new UrlModifiers().setwhatsappPresciptionUrl();
            String prescription_link = visitAttributeListDAO.getVisitAttributesList_specificVisit(visitUuid, PRESCRIPTION_LINK);
            String whatsapp_url = partial_whatsapp_presc_url.concat(prescription_link);
            editText.setText(patient.getPhone_number());

            sharebtn.setOnClickListener(v -> {
                if (!editText.getText().toString().equalsIgnoreCase("")) {
                    String phoneNumber = /*"+91" +*/ editText.getText().toString();
                    String whatsappMessage = getResources().getString(R.string.hello_thankyou_for_using_intelehealth_app_to_download_click_here) + whatsapp_url + getString(R.string.and_enter_your_patient_id) + idView.getText().toString();
                    CustomLog.d("PPPPP", prescription_link);
                    // Toast.makeText(context, R.string.whatsapp_presc_toast, Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://api.whatsapp.com/send?phone=%s&text=%s", phoneNumber, getResources().getString(R.string.hello_thankyou_for_using_intelehealth_app_to_download_click_here) + partial_whatsapp_presc_url + Uri.encode("#") + prescription_link + getString(R.string.and_enter_your_patient_id) + idView.getText().toString()))));

                    // isreturningWhatsapp = true;

                } else {
                    Toast.makeText(context, getResources().getString(R.string.please_enter_mobile_number), Toast.LENGTH_SHORT).show();
                }

            });

            AlertDialog alertDialog = alertdialogBuilder.create();
            alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
            int width = context.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
            alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            alertDialog.show();

        } else {


            Toast.makeText(context, getResources().getString(R.string.download_prescription_first_before_sharing), Toast.LENGTH_SHORT).show();
        }
    }


    private void visitSendDialog(Context context, Drawable drawable, String title, String subTitle, String positiveBtnTxt, String negativeBtnTxt) {
        //validate diagnosis and type of consultation
        if (mBinding.diagnosisTextInput.getText().toString().isEmpty()) {
            mBinding.diagnosisTextInput.setError(getString(R.string.enter_diagnosis));
        }
        if (selectedConsultationType.isEmpty()) {
            TextView view = (TextView) mBinding.typeOfConsultationSpinner.getSelectedView();
            if (view != null) {
                view.setError(getString(R.string.select_consultation_type));
                view.setTextColor(Color.RED);
            }
        }
        if (speciality_selected == null || speciality_selected.isEmpty()) {
            showSelectSpeciliatyErrorDialog();
        }

        if (mFeatureActiveStatus.getDiagnosisAtSecondaryLevel() && mBinding.diagnosisTextInput.getText().toString().isEmpty()) {
            Timber.tag(TAG).d("DiagnosisAtSecondaryLevel");
            return;
        } else if (mFeatureActiveStatus.getTypeOfConsultation() && selectedConsultationType.isEmpty()) {
            Timber.tag(TAG).d("TypeOfConsultation");
            return;
        } else if (mFeatureActiveStatus.getVisitSummeryDoctorSpeciality() && (speciality_selected == null || speciality_selected.isEmpty())) {
            Timber.tag(TAG).d("DoctorSpeciality");
            return;
        } else Timber.tag(TAG).d("visitSendDialog: success");

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
            alertDialog.dismiss();
            visitUploadBlock();
        });

        if (!isFinishing() && !isDestroyed()) {
            alertDialog.show();
        }
    }

    private void visitUploadBlock() {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        CustomLog.d("visitUUID", "upload_click: " + visitUUID);

        isVisitSpecialityExists = speciality_row_exist_check(visitUUID);
        if (speciality_selected != null && !speciality_selected.isEmpty()) {
            viewModel.fetchSpecializationByName(speciality_selected).observe(this, specialization -> {
                String value = ResUtils.getStringResourceByName(VisitSummaryActivity_New.this, specialization.getSKey());
                vd_special_value.setText(" " + Node.bullet + "  " + value);
            });

            VisitAttributeListDAO visitAttributeListDAO = new VisitAttributeListDAO();

            boolean isUpdateVisitDone = false;
            try {
                if (!isVisitSpecialityExists) {
                    isUpdateVisitDone = visitAttributeListDAO.insertVisitAttributes(visitUuid, speciality_selected, SPECIALITY);
                }
                if (selectedFacilityToVisit != null) {
                    visitAttributeListDAO.insertVisitAttributes(visitUuid, selectedFacilityToVisit.getName(), FACILITY);
                }
                if (selectedSeverity != null) {
                    visitAttributeListDAO.insertVisitAttributes(visitUuid, selectedSeverity, SEVERITY);
                }

                visitAttributeListDAO.insertVisitAttributes(visitUuid, AppConstants.dateAndTimeUtils.currentDateTime(), VISIT_UPLOAD_TIME);

                if (!mBinding.diagnosisTextInput.getText().toString().isEmpty()) {
                    visitAttributeListDAO.insertVisitAttributes(visitUuid, mBinding.diagnosisTextInput.getText().toString(), DIAGNOSIS);
                }

                if (!selectedConsultationType.isEmpty()) {
                    visitAttributeListDAO.insertVisitAttributes(visitUuid, selectedConsultationType, CONSULTATION_TYPE);
                }


                if (!TextUtils.isEmpty(selectedFollowupDate) && !TextUtils.isEmpty(selectedFollowupTime)) {
                    EncounterDAO encounterDAO = new EncounterDAO();
                    EncounterDTO encounterDTO = new EncounterDTO();
                    encounterDTO.setUuid(UUID.randomUUID().toString());
                    encounterDTO.setVisituuid(visitUuid);
                    encounterDTO.setSyncd(false);
                    encounterDTO.setProvideruuid(sessionManager.getProviderID());
                    encounterDTO.setEncounterTypeUuid(ENCOUNTER_ADULTINITIAL);
                    encounterDTO.setVoided(0);
                    try {
                        encounterDAO.createEncountersToDB(encounterDTO);
                    } catch (DAOException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }

                    String adultInitialUUID = fetchEncounterUuidForEncounterAdultInitials(visitUUID);

//                    Step - 2 Create observation data object and set the value

                    ObsDTO obsDTO = new ObsDTO();
                    obsDTO.setUuid(UUID.randomUUID().toString()); // HW follow up conceptId
                    obsDTO.setEncounteruuid(adultInitialUUID); // fetched adult initial uuid
                    obsDTO.setConceptuuid(HW_FOLLOWUP_CONCEPT_ID); // HW follow up conceptId
                    obsDTO.setValue(selectedFollowupDate + ", Time:" + selectedFollowupTime + ", Remark: Follow-up");
                    obsDTO.setCreator(sessionManager.getCreatorID());

//                    Step - 3 create observation dao and call insertObs method

                    try {
                        ObsDAO obsDAO = new ObsDAO();
                        obsDAO.insertObs(obsDTO);
                    } catch (DAOException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }

                }
                CustomLog.d("Update_Special_Visit", "Update_Special_Visit: " + isUpdateVisitDone);
            } catch (DAOException e) {
                e.printStackTrace();
                CustomLog.d("Update_Special_Visit", "Update_Special_Visit: " + isUpdateVisitDone);
            }

            // Additional Notes - Start
            try {
                String addnotes = etAdditionalNotesVS.getText().toString().trim();
                CustomLog.v("addnotes", "addnotes: " + addnotes);
                if (!addnotes.equalsIgnoreCase("") && addnotes != null)
                    visitAttributeListDAO.insertVisitAttributes(visitUuid, addnotes, ADDITIONAL_NOTES);
                else
                    visitAttributeListDAO.insertVisitAttributes(visitUuid, "No notes added for Doctor.", ADDITIONAL_NOTES);
                // keeping raw string as we dont want regional lang data to be stored in DB.
            } catch (DAOException e) {
                e.printStackTrace();
                CustomLog.v("addnotes", "addnotes - error: " + e.getMessage());
            }
            // Additional Notes - End

            if (isVisitSpecialityExists) {
                speciality_spinner.setEnabled(false);
                flag.setEnabled(false);
                flag.setClickable(false);
            } else {
                flag.setEnabled(true);
                flag.setClickable(true);
            }

            if (flag.isChecked()) {
                priorityVisit = true;
                try {
                    EncounterDAO encounterDAO = new EncounterDAO();
                    encounterDAO.setEmergency(visitUuid, true);
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }
            if (patient.getOpenmrs_id() == null || patient.getOpenmrs_id().isEmpty()) {
                String patientSelection = "uuid = ?";
                String[] patientArgs = {String.valueOf(patient.getUuid())};
                String table = "tbl_patient";
                String[] columnsToReturn = {"openmrs_id"};
                final Cursor idCursor = db.query(table, columnsToReturn, patientSelection, patientArgs, null, null, null);

                if (idCursor.moveToFirst()) {
                    do {
                        patient.setOpenmrs_id(idCursor.getString(idCursor.getColumnIndex("openmrs_id")));
                    } while (idCursor.moveToNext());
                }
                idCursor.close();
            }

            if (patient.getOpenmrs_id() == null || patient.getOpenmrs_id().isEmpty()) {
            }

            if (visitUUID == null || visitUUID.isEmpty()) {
                String visitIDSelection = "uuid = ?";
                String[] visitIDArgs = {visitUuid};
                final Cursor visitIDCursor = db.query("tbl_visit", null, visitIDSelection, visitIDArgs, null, null, null);
                if (visitIDCursor != null && visitIDCursor.moveToFirst()) {
                    visitUUID = visitIDCursor.getString(visitIDCursor.getColumnIndexOrThrow("uuid"));
                }
                if (visitIDCursor != null) visitIDCursor.close();
            }

            if (!flag.isChecked()) {
                //
            }

            if (NetworkConnection.isOnline(getApplication())) {
                Toast.makeText(context, getResources().getString(R.string.upload_started), Toast.LENGTH_LONG).show();

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                            Added the 4 sec delay and then push data.For some reason doing immediately does not work
                        //Do something after 100ms
                        SyncUtils syncUtils = new SyncUtils();
                        boolean isSynced = syncUtils.syncForeground("visitSummary");
                        if (isSynced) {
                            // remove the local cache
                            sessionManager.removeVisitEditCache(SessionManager.CHIEF_COMPLAIN_LIST + visitUuid);
                            sessionManager.removeVisitEditCache(SessionManager.CHIEF_COMPLAIN_QUESTION_NODE + visitUuid);
                            sessionManager.removeVisitEditCache(SessionManager.PHY_EXAM + visitUuid);
                            sessionManager.removeVisitEditCache(SessionManager.PATIENT_HISTORY + visitUuid);
                            sessionManager.removeVisitEditCache(SessionManager.FAMILY_HISTORY + visitUuid);
                            // ie. visit is uploded successfully.
                            Drawable drawable = ContextCompat.getDrawable(VisitSummaryActivity_New.this, R.drawable.dialog_visit_sent_success_icon);
                            setAppointmentButtonStatus();
                            visitSentSuccessDialog(context, drawable, getResources().getString(R.string.visit_successfully_sent), getResources().getString(R.string.patient_visit_sent), getResources().getString(R.string.okay));

                            /*AppConstants.notificationUtils.DownloadDone(patientName + " " + getString(R.string.visit_data_upload),
                                    getString(R.string.visit_uploaded_successfully), 3, VisitSummaryActivity_New.this);*/
                            isSynedFlag = "1";
                            //
                            showVisitID();
                            CustomLog.d("visitUUID", "showVisitID: " + visitUUID);
                            isVisitSpecialityExists = speciality_row_exist_check(visitUUID);
                            if (isVisitSpecialityExists) {
                                speciality_spinner.setEnabled(false);
                                flag.setEnabled(false);
                                flag.setClickable(false);
                            } else {
                                flag.setEnabled(true);
                                flag.setClickable(true);
                            }
                            fetchingIntent();
                        } else {
                            AppConstants.notificationUtils.DownloadDone(patientName + " " + getString(R.string.visit_data_failed), getString(R.string.visit_uploaded_failed), 3, VisitSummaryActivity_New.this);
                        }
                        uploaded = true;
                    }
                }, 4000);
            } else {
                add_additional_doc.setVisibility(View.GONE);
                fetchingIntent();
                AppConstants.notificationUtils.DownloadDone(patientName + " " + getString(R.string.visit_data_failed), getString(R.string.visit_uploaded_failed), 3, VisitSummaryActivity_New.this);
            }
        } else {
            showSelectSpeciliatyErrorDialog();
        }
    }

    /**
     * function to set appointment button status
     */
    private void setAppointmentButtonStatus() {
        isVisitSpecialityExists = speciality_row_exist_check(visitUUID);
        //added the logic because we will enable appointment button when visit is exist only
        if (isVisitSpecialityExists && NetworkConnection.isOnline(context)) {
            btnAppointment.setBackground(getDrawable(R.drawable.ui2_common_primary_bg));
            btnAppointment.setEnabled(true);
        } else {
            btnAppointment.setBackground(getDrawable(R.drawable.ui2_bg_disabled_time_slot));
            btnAppointment.setEnabled(false);
        }

    }

    private void visitSentSuccessDialog(Context context, Drawable drawable, String title, String subTitle, String neutral) {

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
            //commented to stop navigation bcz navigation from appointment
          /*  Intent intent = new Intent(VisitSummaryActivity_New.this, HomeScreenActivity_New.class);
            startActivity(intent);*/
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    private BroadcastReceiver broadcastReceiverForIamgeDownlaod = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onResume();
            physicalDoumentsUpdates();
        }
    };

    private void physicalDoumentsUpdates() {
        ImagesDAO imagesDAO = new ImagesDAO();
        ArrayList<String> fileuuidList = new ArrayList<String>();
        ArrayList<File> fileList = new ArrayList<File>();
        try {
            fileuuidList = imagesDAO.getImageUuid(encounterUuidAdultIntial, UuidDictionary.COMPLEX_IMAGE_PE);
            for (String fileuuid : fileuuidList) {
                String filename = AppConstants.IMAGE_PATH + fileuuid + ".jpg";
                if (new File(filename).exists()) {
                    fileList.add(new File(filename));
                }
            }
            if (fileList.size() == 0) {
                physcialExaminationDownloadText.setVisibility(View.GONE);
            }
            HorizontalAdapter horizontalAdapter = new HorizontalAdapter(fileList, this);
            mPhysicalExamsLayoutManager = new LinearLayoutManager(VisitSummaryActivity_New.this, LinearLayoutManager.HORIZONTAL, false);
            mPhysicalExamsRecyclerView.setLayoutManager(mPhysicalExamsLayoutManager);
            mPhysicalExamsRecyclerView.setAdapter(horizontalAdapter);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        } catch (Exception file) {
            Logger.logD(TAG, file.getMessage());
        }
    }

    // setting locale
    public void setLocale(String appLanguage) {
        Resources res = getResources();
        Configuration conf = res.getConfiguration();
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        conf.setLocale(locale);
        getApplicationContext().createConfigurationContext(conf);
        DisplayMetrics dm = res.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocales(new LocaleList(locale));
        } else {
            conf.locale = locale;
        }
        res.updateConfiguration(conf, dm);
    }

    // receiver download
    public void registerBroadcastReceiverDynamically() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("MY_BROADCAST_IMAGE_DOWNLAOD");
        ContextCompat.registerReceiver(this, broadcastReceiverForIamgeDownlaod, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    public void registerDownloadPrescription() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("downloadprescription");
        ContextCompat.registerReceiver(this, downloadPrescriptionService, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    public void deleteNotifi_Item(List<NotificationModel> list, int position) {

    }

    @Override
    public void deleteAddDoc_Item(List<DocumentObject> documentList, int position) {
        documentList.remove(position);
        add_docs_title.setText(getResources().getString(R.string.add_additional_documents) + " (" + recyclerViewAdapter.getItemCount() + ")");
    }

    public void openAll(View view) {
    }


    // download pres service class
    public class DownloadPrescriptionService extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.logD(TAG, "Download prescription happen" + new SimpleDateFormat("yyyy MM dd_HH mm ss").format(Calendar.getInstance().getTime()));
            downloadPrescriptionDefault();
            downloadDoctorDetails();
        }
    }

    // download presc default
    public void downloadPrescriptionDefault() {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        String visitnote = "";
        EncounterDAO encounterDAO = new EncounterDAO();
        String encounterIDSelection = "visituuid = ? AND voided = ?";
        String[] encounterIDArgs = {visitUuid, "0"}; // so that the deleted values dont come in the presc.
        Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null);
        if (encounterCursor != null && encounterCursor.moveToFirst()) {
            do {
                if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_VISIT_NOTE").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                    visitnote = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                }
                if (encounterDAO.getEncounterTypeUuid("ENCOUNTER_VISIT_COMPLETE").equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                    hasPrescription = true;
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
                //hasPrescription = "true"; //if any kind of prescription data is present...
                parseData(dbConceptID, dbValue);
            } while (visitCursor.moveToNext());
        }
        visitCursor.close();
        downloaded = true;

        //checks if prescription is downloaded and if so then sets the icon color.
        if (hasPrescription) {
            //   ivPrescription.setImageDrawable(getResources().getDrawable(R.drawable.ic_prescription_green));
        }
    }

    // downlaod doctor details
    private void downloadDoctorDetails() {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        String visitnote = "";
        EncounterDAO encounterDAO = new EncounterDAO();
        String encounterIDSelection = "visituuid = ? ";
        String[] encounterIDArgs = {visitUuid};
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
//                parseDoctorDetails(dbValue);
            } while (visitCursor.moveToNext());
        }
        visitCursor.close();
    }

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
            case UuidDictionary.BLOOD_GROUP: //BLOOD_GROUP
            {
                mBloodGroupObsDTO.setValue(value);
                break;
            }
            case UuidDictionary.TELEMEDICINE_DIAGNOSIS: {
                if (!diagnosisReturned.isEmpty()) {
                    diagnosisReturned = diagnosisReturned + ",\n" + value;
                } else {
                    diagnosisReturned = value;
                }
              /*  if (diagnosisCard.getVisibility() != View.VISIBLE) {
                    diagnosisCard.setVisibility(View.VISIBLE);
                }
                diagnosisTextView.setText(diagnosisReturned);*/
                //checkForDoctor();
                break;
            }
            case UuidDictionary.JSV_MEDICATIONS: {
                CustomLog.i(TAG, "parseData: val:" + value);
                CustomLog.i(TAG, "parseData: rx" + rxReturned);
                if (!rxReturned.trim().isEmpty()) {
                    rxReturned = rxReturned + "\n" + value;
                } else {
                    rxReturned = value;
                }
                CustomLog.i(TAG, "parseData: rxfin" + rxReturned);
               /* if (prescriptionCard.getVisibility() != View.VISIBLE) {
                    prescriptionCard.setVisibility(View.VISIBLE);
                }
                prescriptionTextView.setText(rxReturned);*/
                //checkForDoctor();
                break;
            }
            case UuidDictionary.MEDICAL_ADVICE: {
                if (!adviceReturned.isEmpty()) {
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

                medicalAdvice_string = adviceReturned.replaceAll(medicalAdvice_HyperLink, "");
                CustomLog.d("Hyperlink", "hyper_string: " + medicalAdvice_string);

                /*
                 * variable a contains the hyperlink sent from webside.
                 * variable b contains the string data (medical advice) of patient.
                 * */
               /* medicalAdvice_string = medicalAdvice_string.replace("\n\n", "\n");
                medicalAdviceTextView.setText(Html.fromHtml(medicalAdvice_HyperLink +
                        medicalAdvice_string.replaceAll("\n", "<br><br>")));*/

                adviceReturned = adviceReturned.replaceAll("\n", "<br><br>");
                //  medicalAdviceTextView.setText(Html.fromHtml(adviceReturned));
               /* medicalAdviceTextView.setText(Html.fromHtml(adviceReturned.replace("Doctor_", "Doctor")));
                medicalAdviceTextView.setMovementMethod(LinkMovementMethod.getInstance());
                CustomLog.d("hyper_textview", "hyper_textview: " + medicalAdviceTextView.getText().toString());*/
                //checkForDoctor();
                break;
            }
            case UuidDictionary.REQUESTED_TESTS: {
                if (!testsReturned.isEmpty()) {
                    testsReturned = testsReturned + "\n\n" + Node.bullet + " " + value;
                } else {
                    testsReturned = Node.bullet + " " + value;
                }
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
            }

            case UuidDictionary.ADDITIONAL_COMMENTS: {

//                additionalCommentsCard.setVisibility(View.GONE);

                if (!additionalReturned.isEmpty()) {
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
                if (!followUpDate.isEmpty()) {
                    followUpDate = followUpDate + "," + value;
                } else {
                    followUpDate = value;
                }
              /*  if (followUpDateCard.getVisibility() != View.VISIBLE) {
                    followUpDateCard.setVisibility(View.VISIBLE);
                }
                followUpDateTextView.setText(followUpDate);*/
                //checkForDoctor();
                break;
            }

            default:
                CustomLog.i(TAG, "parseData: " + value);
                break;
        }
    }

    // parse doctor details
    ClsDoctorDetails objClsDoctorDetails;

    private void parseDoctorDetails(String dbValue) {
        Gson gson = new Gson();
        objClsDoctorDetails = gson.fromJson(dbValue, ClsDoctorDetails.class);
        CustomLog.e(TAG, "TEST VISIT: " + objClsDoctorDetails);

        String doctorSign = "";
        String doctrRegistartionNum = "";
        // String docDigitallySign = "";
        String doctorDetailStr = "";
        if (objClsDoctorDetails != null) {
            //  frameLayout_doctor.setVisibility(View.VISIBLE);   // todo: handle later.

            doctorSign = objClsDoctorDetails.getTextOfSign();
            doctrRegistartionNum = !TextUtils.isEmpty(objClsDoctorDetails.getRegistrationNumber()) ? getString(R.string.dr_registration_no) + objClsDoctorDetails.getRegistrationNumber() : "";

            doctorDetailStr = "<div style=\"text-align:right;margin-right:0px;margin-top:0px;\">" + "<span style=\"font-size:12pt; color:#448AFF;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getName()) ? objClsDoctorDetails.getName() : "") + "</span><br>" + (!TextUtils.isEmpty(objClsDoctorDetails.getSpecialization()) ? objClsDoctorDetails.getSpecialization() : "") + "</span><br>" + "<span style=\"font-size:12pt;color:#448AFF;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getEmailId()) ? "Email: " + objClsDoctorDetails.getEmailId() : "") + "</span><br>" + (!TextUtils.isEmpty(objClsDoctorDetails.getRegistrationNumber()) ? "Registration No: " + objClsDoctorDetails.getRegistrationNumber() : "") + "</div>";

            //    mDoctorName.setText(Html.fromHtml(doctorDetailStr).toString().trim()); // todo: handle later
        }
    }

    // query data

    /**
     * This methods retrieves patient data from database.
     *
     * @param dataString variable of type String
     * @return void
     */

    public void queryData(String dataString) {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        String patientSelection = "uuid = ?";
        String[] patientArgs = {dataString};

        String table = "tbl_patient";
        String[] columnsToReturn = {"openmrs_id", "first_name", "middle_name", "last_name", "date_of_birth", "address1", "address2", "city_village", "state_province", "country", "postal_code", "phone_number", "gender", "sdw", "occupation", "patient_photo"};
        final Cursor idCursor = db.query(table, columnsToReturn, patientSelection, patientArgs, null, null, null);

        if (idCursor.moveToFirst()) {
            do {
                patient.setUuid(patientUuid);
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
            String[] famHistArgs = {encounterUuidAdultIntial, UuidDictionary.RHK_FAMILY_HISTORY_BLURB};
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

            String[] medHistArgs = {encounterUuidAdultIntial, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB};

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
        String[] visitArgs = {encounterVitals};
        if (encounterVitals != null) {
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
        //Diagnostics code kaveri
        String[] visitArgsDiagnostics = {encounterVitals};
        if (encounterVitals != null) {
            try {
                Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgsDiagnostics, null, null, null);
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
        String[] encounterargs = {encounterUuidAdultIntial, UuidDictionary.COMPLEX_IMAGE_AD, UuidDictionary.COMPLEX_IMAGE_PE};
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


    /*temperature convert*/
    /*private String convertCtoF(String temperature) {
        String resultVal;
        NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
        double a = Double.parseDouble(temperature);
        double b = (a * 9 / 5) + 32;
        nf.format(b);
        double roundOff = Math.round(b * 100.0) / 100.0;
        resultVal = nf.format(roundOff);
        return resultVal;
    }*/

    /*PhysExam images downlaod*/
    private void physcialExaminationImagesDownload() {
        ImagesDAO imagesDAO = new ImagesDAO();
        if (encounterUuidAdultIntial != null) {
            try {
                List<String> imageList = imagesDAO.isImageListObsExists(encounterUuidAdultIntial, UuidDictionary.COMPLEX_IMAGE_PE);
                if (imageList.size() == 0) {
                    physcialExaminationDownloadText.setVisibility(View.GONE);
                } else {
                    for (String images : imageList) {
                        if (imagesDAO.isLocalImageUuidExists(images))
                            physcialExaminationDownloadText.setVisibility(View.GONE);
                        else physcialExaminationDownloadText.setVisibility(View.VISIBLE);
                    }
                }
            } catch (DAOException e) {
                e.printStackTrace();
            }
        }

        physcialExaminationDownloadText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownload(UuidDictionary.COMPLEX_IMAGE_PE);
                physcialExaminationDownloadText.setVisibility(View.GONE);
            }
        });
    }

    private void startDownload(String imageType) {
        Intent intent = new Intent(this, DownloadService.class);
        intent.putExtra("patientUuid", patientUuid);
        intent.putExtra("visitUuid", visitUuid);
        intent.putExtra("encounterUuidVitals", encounterVitals);
        intent.putExtra("encounterUuidAdultIntial", encounterUuidAdultIntial);
        intent.putExtra("ImageType", imageType);
        startService(intent);
    }
    /*PhysExam images downlaod - end*/

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
                LocalBroadcastManager.getInstance(context).unregisterReceiver(downloadPrescriptionService);
            }
            if (receiver != null) {
                unregisterReceiver(receiver);
            }
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);


            //unregister receiver for internet check
            networkUtils.unregisterNetworkReceiver();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleMessage(intent);
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        if (receiver != null) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
            receiver = null;
        }
        if (downloadPrescriptionService != null) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(downloadPrescriptionService);
            downloadPrescriptionService = null;
        }
        isReceiverRegistered = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        //get from encountertbl from the encounter
       /* if (visitnoteencounteruuid.equalsIgnoreCase("")) {
            visitnoteencounteruuid = getStartVisitNoteEncounterByVisitUUID(visitUuid);
        }*/ // todo: uncomment and handle later....

        if (downloadPrescriptionService == null) {
            registerDownloadPrescription();
        }

        callBroadcastReceiver();

        // showing additional images...
        ImagesDAO imagesDAO = new ImagesDAO();
        ArrayList<String> fileuuidList = new ArrayList<String>();
        ArrayList<File> fileList = new ArrayList<File>();
        try {
            fileuuidList = imagesDAO.getImageUuid(encounterUuidAdultIntial, UuidDictionary.COMPLEX_IMAGE_AD);
            for (String fileuuid : fileuuidList) {
                String filename = AppConstants.IMAGE_PATH + fileuuid + ".jpg";
                if (new File(filename).exists()) {
                    fileList.add(new File(filename));
                }
            }

          /*  HorizontalAdapter horizontalAdapter = new HorizontalAdapter(fileList, this);
            mAdditionalDocsLayoutManager = new LinearLayoutManager(VisitSummaryActivity_New.this,
                    LinearLayoutManager.HORIZONTAL, false);
            mAdditionalDocsRecyclerView.setLayoutManager(mAdditionalDocsLayoutManager);
            mAdditionalDocsRecyclerView.setAdapter(horizontalAdapter);*/

            RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(this);
            mAdditionalDocsRecyclerView.setHasFixedSize(true);
            mAdditionalDocsRecyclerView.setLayoutManager(linearLayoutManager);

            recyclerViewAdapter = new AdditionalDocumentAdapter(this, encounterUuidAdultIntial, rowListItem, AppConstants.IMAGE_PATH, this, isVisitSpecialityExists);
//            if (intentTag.equalsIgnoreCase("VisitDetailsActivity")) {
//                recyclerViewAdapter = new AdditionalDocumentAdapter(this, encounterUuidAdultIntial, rowListItem, AppConstants.IMAGE_PATH, this, true);
//            } else {
//                recyclerViewAdapter = new AdditionalDocumentAdapter(this, encounterUuidAdultIntial, rowListItem, AppConstants.IMAGE_PATH, this, false);
//            }

            mAdditionalDocsRecyclerView.setAdapter(recyclerViewAdapter);
            add_docs_title.setText(getResources().getString(R.string.add_additional_documents) + " (" + recyclerViewAdapter.getItemCount() + ")");

//            if (recyclerViewAdapter != null) {
//                if (intentTag.equalsIgnoreCase("VisitDetailsActivity")) {
//                    recyclerViewAdapter.hideCancelBtnAddDoc(true);
//                } else {
//                    recyclerViewAdapter.hideCancelBtnAddDoc(false);
//                }
//            }

        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        } catch (Exception file) {
            Logger.logD(TAG, file.getMessage());
        }

        setAppointmentButtonStatus();
        setupVisibilityForSpecificFlavor();
    }

    // Netowork reciever
    public class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            isNetworkAvailable(context);
        }
    }

    // handle message
    private void handleMessage(Intent msg) {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        CustomLog.i(TAG, "handleMessage: Entered");
        Bundle data = msg.getExtras();
        int check = 0;
        if (data != null) {
            check = data.getInt("Restart");
        }
        if (check == 100) {
            CustomLog.i(TAG, "handleMessage: 100");
            diagnosisReturned = "";
            rxReturned = "";
            testsReturned = "";
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
            CustomLog.i(TAG, "handleMessage: 200");
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
                        CustomLog.i(TAG, "found diagnosis");
                        break;
                    case UuidDictionary.JSV_MEDICATIONS:
                        CustomLog.i(TAG, "found medications");
                        break;
                    case UuidDictionary.MEDICAL_ADVICE:
                        CustomLog.i(TAG, "found medical advice");
                        break;
                    case UuidDictionary.ADDITIONAL_COMMENTS:
                        CustomLog.i(TAG, "found additional comments");
                        break;
                    case UuidDictionary.REQUESTED_TESTS:
                        CustomLog.i(TAG, "found tests");
                        break;
                    default:
                }
                obsCursor.close();
                //   addDownloadButton();
                //if any obs  found then end the visit
                //endVisit();
            } else {
                CustomLog.i(TAG, "found sothing for test");
            }
        }
    }

    // add downlaod button
    private void addDownloadButton() {
      /*  if (!downloadButton.isEnabled()) {
            downloadButton.setEnabled(true);
            downloadButton.setVisibility(View.VISIBLE);
        }*/
    }

    // speciality alrady exists checking

    /**
     * @param uuid the visit uuid of the patient visit records is passed to the function.
     * @return boolean value will be returned depending upon if the row exists in the tbl_visit_attribute tbl
     */
    private boolean speciality_row_exist_check(String uuid) {
        boolean isExists = false;

        if (uuid != null) {
            SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
            db.beginTransaction();
            Cursor cursor = db.rawQuery("SELECT * FROM tbl_visit_attribute WHERE visit_uuid=?", new String[]{uuid});

            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    isExists = true;
                }
            }
            cursor.close();
            db.setTransactionSuccessful();
            db.endTransaction();

        }
        return isExists;

    }

    // start activity for result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CameraActivity.TAKE_IMAGE) {
            if (resultCode == RESULT_OK) {
                String mCurrentPhotoPath = data.getStringExtra("RESULT");
                saveImage(mCurrentPhotoPath);
            }
        } else if (requestCode == PICK_IMAGE_FROM_GALLERY) {
            if (data != null) {
                Uri selectedImage = data.getData();
                String[] filePath = {MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                //Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
                CustomLog.v("path", picturePath + "");
                BitmapUtils.fileCompressed(picturePath);

                // copy & rename the file
                String finalImageName = UUID.randomUUID().toString();
                final String finalFilePath = AppConstants.IMAGE_PATH + finalImageName + ".jpg";
                BitmapUtils.copyFile(picturePath, finalFilePath);
                compressImageAndSave(finalFilePath);
            }
        }
    }

    // save image
    private void saveImage(String picturePath) {
        CustomLog.v("AdditionalDocuments", "picturePath = " + picturePath);
        File photo = new File(picturePath);
        BitmapUtils.fileCompressed(picturePath);
        if (photo.exists()) {
            try {
                long length = photo.length();
                length = length / 1024;
                CustomLog.e("------->>>>", length + "");
            } catch (Exception e) {
                System.out.println("File not found : " + e.getMessage() + e);
            }

            recyclerViewAdapter.add(new DocumentObject(photo.getName(), photo.getAbsolutePath()));
            updateImageDatabase(StringUtils.getFileNameWithoutExtension(photo));
        }
    }

    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    // compress image

    /**
     * @param filePath Final Image path to compress.
     */
    // TODO: crash as there is no permission given in setup app section for firsttime user.
    void compressImageAndSave(final String filePath) {
        getBackgroundHandler().post(new Runnable() {
            @Override
            public void run() {
                boolean flag = BitmapUtils.fileCompressed(filePath);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (flag) {
                            saveImage(filePath);
                        } else
                            Toast.makeText(VisitSummaryActivity_New.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }

    // update image database
    private void updateImageDatabase(String imageuuid) {
        ImagesDAO imagesDAO = new ImagesDAO();
        try {
            imagesDAO.insertObsImageDatabase(imageuuid, encounterUuidAdultIntial, UuidDictionary.COMPLEX_IMAGE_AD, AppConstants.IMAGE_ADDITIONAL_DOC);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }


    // udpate database block

    /**
     * This method updates patient details to database.
     *
     * @param string    variable of type String
     * @param conceptID variable of type int
     */
    private void updateDatabase(String string, String conceptID) {
        ObsDTO obsDTO = new ObsDTO();
        ObsDAO obsDAO = new ObsDAO();
        try {
            obsDTO.setConceptuuid(String.valueOf(conceptID));
            obsDTO.setEncounteruuid(encounterUuidAdultIntial);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(string);
            obsDTO.setUuid(obsDAO.getObsuuid(encounterUuidAdultIntial, String.valueOf(conceptID)));

            obsDAO.updateObs(obsDTO);


        } catch (DAOException dao) {
            FirebaseCrashlytics.getInstance().recordException(dao);
        }

        EncounterDAO encounterDAO = new EncounterDAO();
        try {
            encounterDAO.updateEncounterSync("false", encounterUuidAdultIntial);
            encounterDAO.updateEncounterModifiedDate(encounterUuidAdultIntial);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    // profile pic downalod
    public void profilePicDownloaded(Patient patientModel) {
        sessionManager = new SessionManager(context);
        UrlModifiers urlModifiers = new UrlModifiers();
        String url = urlModifiers.patientProfileImageUrl(patientModel.getUuid());
        Logger.logD("TAG", "profileimage url" + url);
        Observable<ResponseBody> profilePicDownload = AppConstants.apiInterface.PERSON_PROFILE_PIC_DOWNLOAD(url, "Basic " + sessionManager.getEncoded());
        profilePicDownload.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new DisposableObserver<ResponseBody>() {
            @Override
            public void onNext(ResponseBody file) {
                DownloadFilesUtils downloadFilesUtils = new DownloadFilesUtils();
                downloadFilesUtils.saveToDisk(file, patientModel.getUuid());
                Logger.logD("TAG", file.toString());
            }

            @Override
            public void onError(Throwable e) {
                Logger.logD("TAG", e.getMessage());
            }

            @Override
            public void onComplete() {
                Logger.logD("TAG", "complete" + patientModel.getPatient_photo());
                PatientsDAO patientsDAO = new PatientsDAO();
                boolean updated = false;
                try {
                    updated = patientsDAO.updatePatientPhoto(patientModel.getUuid(), AppConstants.IMAGE_PATH + patientModel.getUuid() + ".jpg");
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
                if (updated) {
                    RequestBuilder<Drawable> requestBuilder = Glide.with(context).asDrawable().sizeMultiplier(0.3f);
                    Glide.with(context).load(AppConstants.IMAGE_PATH + patientModel.getUuid() + ".jpg").thumbnail(requestBuilder).centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(profile_image);
                }
                ImagesDAO imagesDAO = new ImagesDAO();
                boolean isImageDownloaded = false;
                try {
                    isImageDownloaded = imagesDAO.insertPatientProfileImages(AppConstants.IMAGE_PATH + patientModel.getUuid() + ".jpg", patientModel.getUuid());
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }
        });
    }

    // Print - start
    private void doWebViewPrint_Button() throws ParseException {
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
        String[] visitIDArgs = {visitUuid};
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
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
                    mTemp = getResources().getString(R.string.prescription_temp_f) + " " + (!TextUtils.isEmpty(temperature.getValue()) ? convertCtoF(TAG, temperature.getValue()) : "");
                }
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
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
                mComplaint = mComplaint.substring(0, compla.indexOf("Associated symptoms") - 3); // todo: uncomment later.
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
        // added the chief complain from pre-generated list during visit summary display and commenting above old logic
        //if (mIsCCInOldFormat) {
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
        //}


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

        String advice_web = stringToWeb(adviceReturned);
        //    String advice_web = "";
//        if(medicalAdviceTextView.getText().toString().indexOf("Start") != -1 ||
//                medicalAdviceTextView.getText().toString().lastIndexOf(("User") + 6) != -1) {
/*        String advice_doctor__ = medicalAdviceTextView.getText().toString()
                .replace("Start Audio Call with Doctor", "Start Audio Call with Doctor_")
                .replace("Start WhatsApp Call with Doctor", "Start WhatsApp Call with Doctor_");

        if (advice_doctor__.indexOf("Start") != -1 ||
                advice_doctor__.lastIndexOf(("Doctor_") + 9) != -1) {


//        String advice_web = stringToWeb(medicalAdvice_string.trim().replace("\n\n", "\n"));
//        CustomLog.d("Hyperlink", "hyper_print: " + advice_web);
//        String advice_split = new StringBuilder(medicalAdviceTextView.getText().toString())
//                .delete(medicalAdviceTextView.getText().toString().indexOf("Start"),
//                        medicalAdviceTextView.getText().toString().lastIndexOf("User")+6).toString();
            //lastIndexOf("User") will give index of U of User
            //so the char this will return is U...here User + 6 will return W eg: User\n\nWatch as +6 will give W

            String advice_split = new StringBuilder(advice_doctor__)
                    .delete(advice_doctor__.indexOf("Start"),
                            advice_doctor__.lastIndexOf("Doctor_") + 9).toString();
            //lastIndexOf("Doctor_") will give index of D of Doctor_
            //so the char this will return is D...here Doctor_ + 9 will return W eg: Doctor_\n\nWatch as +9 will give W


//        String advice_web = stringToWeb(advice_split.replace("\n\n", "\n")); //showing advice here...
//        CustomLog.d("Hyperlink", "hyper_print: " + advice_web); //gets called when clicked on button of print button
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

        String bp = mBP;
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
        if (objClsDoctorDetails != null && objClsDoctorDetails.getFontOfSign() != null) {
            if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("youthness")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/Youthness.ttf');";
            } else if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("asem")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/Asem.otf');";
            } else if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("arty")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/Arty.otf');";
            } else if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("almondita")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/almondita.ttf');";
            }
        }
        String font_face = "<style>" + "                @font-face {" + "                    font-family: \"MyFont\";" + fontFamilyFile + "                }" + "            </style>";

        String doctorSign = "";
        String doctrRegistartionNum = "";
        // String docDigitallySign = "";
        String doctorDetailStr = "";
        if (objClsDoctorDetails != null) {
            //  docDigitallySign = "Digitally Signed By";
            doctorSign = objClsDoctorDetails.getTextOfSign();

            sign_url = BuildConfig.SERVER_URL + "/ds/" + objClsDoctorDetails.getUuid() + "_sign.png";

            doctrRegistartionNum = !TextUtils.isEmpty(objClsDoctorDetails.getRegistrationNumber()) ? getString(R.string.dr_registration_no) + objClsDoctorDetails.getRegistrationNumber() : "";
//            doctorDetailStr = "<div style=\"text-align:right;margin-right:0px;margin-top:3px;\">" +
//                    "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + objClsDoctorDetails.getName() + "</span><br>" +
//                    "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + "  " + objClsDoctorDetails.getQualification() + ", " + objClsDoctorDetails.getSpecialization() + "</span><br>" +
//                    //  "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getPhoneNumber()) ?
//                    //  getString(R.string.dr_phone_number) + objClsDoctorDetails.getPhoneNumber() : "") + "</span><br>" +
//                    "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getEmailId()) ?
//                    getString(R.string.dr_email) + objClsDoctorDetails.getEmailId() : "") + "</span><br>" +
//                    "</div>";


            doctorDetailStr = "<div style=\"text-align:right;margin-right:0px;\">" + "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + objClsDoctorDetails.getName() + "</span><br>" + "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + "  " + (objClsDoctorDetails.getQualification() == null || objClsDoctorDetails.getQualification().equalsIgnoreCase("null") ? "" : objClsDoctorDetails.getQualification() + ", ") + objClsDoctorDetails.getSpecialization() + "</span><br>" +
                    //  "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getPhoneNumber()) ?
                    //  getString(R.string.dr_phone_number) + objClsDoctorDetails.getPhoneNumber() : "") + "</span><br>" +
                    "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getEmailId()) ? getString(R.string.dr_email) + objClsDoctorDetails.getEmailId() : "") + "</span><br>" + "</div>";
//            mDoctorName.setText(doctrRegistartionNum + "\n" + Html.fromHtml(doctorDetailStr));
        }

        PrescriptionBuilder prescriptionBuilder = new PrescriptionBuilder(this);
        VitalsObject vitalsData = getAllVitalsData();
        String prescriptionString = prescriptionBuilder.builder(patient, vitalsData, diagnosisReturned, rxReturned, adviceReturned, testsReturned, referredSpeciality, followUpDate, objClsDoctorDetails, mFeatureActiveStatus);


        if (isRespiratory) {
            String htmlDocument = String.format(/*font_face +*/ "<b><p id=\"heading_1\" style=\"font-size:16pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<p id=\"heading_2\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<p id=\"heading_3\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<hr style=\"font-size:12pt;\">" + "<br/>" +
                            /* doctorDetailStr +*/
                            "<p id=\"patient_name\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">%s</p></b>" + "<p id=\"patient_details\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Age: %s | Gender: %s  </p>" + "<p id=\"address_and_contact\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Address and Contact: %s</p>" + "<p id=\"visit_details\" style=\"font-size:12pt; margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient Id: %s | Date of visit: %s </p><br>" + "<b><p id=\"vitals_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px;; padding: 0px;\">Vitals</p></b>" + "<p id=\"vitals\" style=\"font-size:12pt;margin:0px; padding: 0px;\">Height(cm): %s | Weight(kg): %s | BMI: %s | Blood Pressure: %s | Pulse(bpm): %s | %s | Respiratory Rate: %s |  %s </p><br>" +
                                    /*"<b><p id=\"patient_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient History</p></b>" +
                                    "<p id=\"patient_history\" style=\"font-size:11pt;margin:0px; padding: 0px;\"> %s</p><br>" +
                                    "<b><p id=\"family_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Family History</p></b>" +
                                    "<p id=\"family_history\" style=\"font-size:11pt;margin: 0px; padding: 0px;\"> %s</p><br>" +*/
                            "<b><p id=\"complaints_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Presenting complaint(s)</p></b>" + para_open + "%s" + para_close + "<br><br>" + "<u><b><p id=\"diagnosis_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Diagnosis</p></b></u>" + "%s<br>" + "<u><b><p id=\"rx_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Medication(s) plan</p></b></u>" + "%s<br>" + "<u><b><p id=\"tests_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Recommended Investigation(s)</p></b></u>" + "%s<br>" + "<u><b><p id=\"advice_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">General Advice</p></b></u>" + "%s<br>" + "<u><b><p id=\"follow_up_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Follow Up Date</p></b></u>" + "%s<br>" + "<div style=\"text-align:right;margin-right:50px;margin-top:0px;\">" +
                            //  "<span style=\"font-size:80pt;font-family: MyFont;padding: 0px;\">" + doctorSign + "</span>" +
                            "<img src=" + sign_url + " alt=\"Dr Signature\">" + // doctor signature...
                            doctorDetailStr + "<p style=\"font-size:12pt; margin-top:-0px; padding: 0px;\">" + doctrRegistartionNum + "</p>" + "</div>", heading, heading2, heading3, mPatientName, age, mGender, /*mSdw*/ address, mPatientOpenMRSID, mDate, (!TextUtils.isEmpty(mHeight)) ? mHeight : "", (!TextUtils.isEmpty(mWeight)) ? mWeight : "", (!TextUtils.isEmpty(mBMI)) ? mBMI : "", (!TextUtils.isEmpty(bp)) ? bp : "", (!TextUtils.isEmpty(mPulse)) ? mPulse : "", (!TextUtils.isEmpty(mTemp)) ? mTemp : "", (!TextUtils.isEmpty(mresp)) ? mresp : "", (!TextUtils.isEmpty(mSPO2)) ? mSPO2 : "",
                    /*pat_hist, fam_hist,*/ mComplaint, diagnosis_web, rx_web, tests_web, advice_web/*""*/, followUp_web, doctor_web);
            webView.loadDataWithBaseURL(null, prescriptionString, "text/HTML", "UTF-8", null);
        } else {
            String htmlDocument = String.format(font_face + "<b><p id=\"heading_1\" style=\"font-size:16pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<p id=\"heading_2\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<p id=\"heading_3\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<hr style=\"font-size:12pt;\">" + "<br/>" + "<p id=\"patient_name\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">%s</p></b>" + "<p id=\"patient_details\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Age: %s | Gender: %s </p>" + "<p id=\"address_and_contact\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Address and Contact: %s</p>" + "<p id=\"visit_details\" style=\"font-size:12pt; margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient Id: %s | Date of visit: %s </p><br>" + "<b><p id=\"vitals_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px;; padding: 0px;\">Vitals</p></b>" + "<p id=\"vitals\" style=\"font-size:12pt;margin:0px; padding: 0px;\">Height(cm): %s | Weight(kg): %s | BMI: %s | Blood Pressure: %s | Pulse(bpm): %s | %s | %s </p><br>" +
                                    /*"<b><p id=\"patient_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient History</p></b>" +
                                    "<p id=\"patient_history\" style=\"font-size:11pt;margin:0px; padding: 0px;\"> %s</p><br>" +
                                    "<b><p id=\"family_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Family History</p></b>" +
                                    "<p id=\"family_history\" style=\"font-size:11pt;margin: 0px; padding: 0px;\"> %s</p><br>" +*/
                            "<b><p id=\"complaints_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Presenting complaint(s)</p></b>" + para_open + "%s" + para_close + "<br><br>" + "<u><b><p id=\"diagnosis_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Diagnosis</p></b></u>" + "%s<br>" + "<u><b><p id=\"rx_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Medication(s) plan</p></b></u>" + "%s<br>" + "<u><b><p id=\"tests_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Recommended Investigation(s)</p></b></u>" + "%s<br>" + "<u><b><p id=\"advice_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">General Advice</p></b></u>" + "%s<br>" + "<u><b><p id=\"follow_up_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Follow Up Date</p></b></u>" + "%s<br>" + "<div style=\"text-align:right;margin-right:50px;margin-top:0px;\">" + "<span style=\"font-size:80pt;font-family: MyFont;padding: 0px;\">" + doctorSign + "</span><br>" + doctorDetailStr + "<span style=\"font-size:12pt; margin-top:5px; padding: 0px;\">" + doctrRegistartionNum + "</span>" + "</div>", heading, heading2, heading3, mPatientName, age, mGender, /*mSdw*/ address, mPatientOpenMRSID, mDate, (!TextUtils.isEmpty(mHeight)) ? mHeight : "", (!TextUtils.isEmpty(mWeight)) ? mWeight : "", (!TextUtils.isEmpty(mBMI)) ? mBMI : "", (!TextUtils.isEmpty(bp)) ? bp : "", (!TextUtils.isEmpty(mPulse)) ? mPulse : "", (!TextUtils.isEmpty(mTemp)) ? mTemp : "", (!TextUtils.isEmpty(mSPO2)) ? mSPO2 : "",
                    /*pat_hist, fam_hist,*/ mComplaint, diagnosis_web, rx_web, tests_web, /*advice_web*/"", followUp_web, doctor_web);
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
            String jobName = getString(R.string.app_name) + " " + getResources().getString(R.string._visit_summary);

            //To display the preview window to user...
            PrintJob printJob = printManager.print(jobName, printAdapter, pBuilder.build());


        } else if (contentHeight == 0) {
            //in case of webview bug of 0 contents...
            PrintAttributes.Builder pBuilder = new PrintAttributes.Builder();
            pBuilder.setMediaSize(PrintAttributes.MediaSize.JIS_B4);
            pBuilder.setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600));
            pBuilder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);
            // Create a print job with name and adapter instance
            String jobName = getString(R.string.app_name) + " " + getResources().getString(R.string._visit_summary);

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
            String jobName = getString(R.string.app_name) + " " + getResources().getString(R.string._visit_summary);

            //To display the preview window to user...
            PrintJob printJob = printManager.print(jobName, printAdapter, pBuilder.build());
        } else {
            String jobName = getString(R.string.app_name) + " " + getResources().getString(R.string._visit_summary);

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
    // Print - end

    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        CustomLog.d("TAG", "updateUIForInternetAvailability: ");
        if (isInternetAvailable) {
            refresh.setImageDrawable(ContextCompat.getDrawable(VisitSummaryActivity_New.this, R.drawable.ui2_ic_internet_available));
        } else {
            refresh.setImageDrawable(ContextCompat.getDrawable(VisitSummaryActivity_New.this, R.drawable.ui2_ic_no_internet));
        }
        setAppointmentButtonStatus();
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
        String[] visitIDArgs = {visitUuid};
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
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
                    mTemp = getResources().getString(R.string.prescription_temp_f) + " " + (!TextUtils.isEmpty(temperature.getValue()) ? convertCtoF(TAG, temperature.getValue()) : "");
                }
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
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
                mComplaint = mComplaint.substring(0, compla.indexOf(Node.ASSOCIATE_SYMPTOMS) - 3); // todo: uncomment later.
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

        String advice_web = stringToWeb(adviceReturned);
        //    String advice_web = "";
//        if(medicalAdviceTextView.getText().toString().indexOf("Start") != -1 ||
//                medicalAdviceTextView.getText().toString().lastIndexOf(("User") + 6) != -1) {
/*        String advice_doctor__ = medicalAdviceTextView.getText().toString()
                .replace("Start Audio Call with Doctor", "Start Audio Call with Doctor_")
                .replace("Start WhatsApp Call with Doctor", "Start WhatsApp Call with Doctor_");

        if (advice_doctor__.indexOf("Start") != -1 ||
                advice_doctor__.lastIndexOf(("Doctor_") + 9) != -1) {


//        String advice_web = stringToWeb(medicalAdvice_string.trim().replace("\n\n", "\n"));
//        CustomLog.d("Hyperlink", "hyper_print: " + advice_web);
//        String advice_split = new StringBuilder(medicalAdviceTextView.getText().toString())
//                .delete(medicalAdviceTextView.getText().toString().indexOf("Start"),
//                        medicalAdviceTextView.getText().toString().lastIndexOf("User")+6).toString();
            //lastIndexOf("User") will give index of U of User
            //so the char this will return is U...here User + 6 will return W eg: User\n\nWatch as +6 will give W

            String advice_split = new StringBuilder(advice_doctor__)
                    .delete(advice_doctor__.indexOf("Start"),
                            advice_doctor__.lastIndexOf("Doctor_") + 9).toString();
            //lastIndexOf("Doctor_") will give index of D of Doctor_
            //so the char this will return is D...here Doctor_ + 9 will return W eg: Doctor_\n\nWatch as +9 will give W


//        String advice_web = stringToWeb(advice_split.replace("\n\n", "\n")); //showing advice here...
//        CustomLog.d("Hyperlink", "hyper_print: " + advice_web); //gets called when clicked on button of print button
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

        String bp = mBP;
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
        if (objClsDoctorDetails != null && objClsDoctorDetails.getFontOfSign() != null) {
            if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("youthness")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/Youthness.ttf');";
            } else if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("asem")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/Asem.otf');";
            } else if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("arty")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/Arty.otf');";
            } else if (objClsDoctorDetails.getFontOfSign().toLowerCase().equalsIgnoreCase("almondita")) {
                fontFamilyFile = "src: url('file:///android_asset/fonts/almondita.ttf');";
            }
        }
        String font_face = "<style>" + "                @font-face {" + "                    font-family: \"MyFont\";" + fontFamilyFile + "                }" + "            </style>";

        String doctorSign = "";
        String doctrRegistartionNum = "";
        // String docDigitallySign = "";
        String doctorDetailStr = "";
        if (objClsDoctorDetails != null) {
            //  docDigitallySign = "Digitally Signed By";
            doctorSign = objClsDoctorDetails.getTextOfSign();

            sign_url = BuildConfig.SERVER_URL + "/ds/" + objClsDoctorDetails.getUuid() + "_sign.png";

            doctrRegistartionNum = !TextUtils.isEmpty(objClsDoctorDetails.getRegistrationNumber()) ? getString(R.string.dr_registration_no) + objClsDoctorDetails.getRegistrationNumber() : "";
//            doctorDetailStr = "<div style=\"text-align:right;margin-right:0px;margin-top:3px;\">" +
//                    "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + objClsDoctorDetails.getName() + "</span><br>" +
//                    "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + "  " + objClsDoctorDetails.getQualification() + ", " + objClsDoctorDetails.getSpecialization() + "</span><br>" +
//                    //  "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getPhoneNumber()) ?
//                    //  getString(R.string.dr_phone_number) + objClsDoctorDetails.getPhoneNumber() : "") + "</span><br>" +
//                    "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getEmailId()) ?
//                    getString(R.string.dr_email) + objClsDoctorDetails.getEmailId() : "") + "</span><br>" +
//                    "</div>";


            doctorDetailStr = "<div style=\"text-align:right;margin-right:0px;\">" + "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + objClsDoctorDetails.getName() + "</span><br>" + "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + "  " + (objClsDoctorDetails.getQualification() == null || objClsDoctorDetails.getQualification().equalsIgnoreCase("null") ? "" : objClsDoctorDetails.getQualification() + ", ") + objClsDoctorDetails.getSpecialization() + "</span><br>" +
                    //  "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getPhoneNumber()) ?
                    //  getString(R.string.dr_phone_number) + objClsDoctorDetails.getPhoneNumber() : "") + "</span><br>" +
                    "<span style=\"font-size:12pt;color:#212121;padding: 0px;\">" + (!TextUtils.isEmpty(objClsDoctorDetails.getEmailId()) ? getString(R.string.dr_email) + objClsDoctorDetails.getEmailId() : "") + "</span><br>" + "</div>";
//            mDoctorName.setText(doctrRegistartionNum + "\n" + Html.fromHtml(doctorDetailStr));
        }

        if (isRespiratory) {
            String htmlDocument = String.format(/*font_face +*/ "<b><p id=\"heading_1\" style=\"font-size:16pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<p id=\"heading_2\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<p id=\"heading_3\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<hr style=\"font-size:12pt;\">" + "<br/>" +
                    /* doctorDetailStr +*/
                    "<p id=\"patient_name\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">%s</p></b>" + "<p id=\"patient_details\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Age: %s | Gender: %s  </p>" + "<p id=\"address_and_contact\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Address and Contact: %s</p>" + "<p id=\"visit_details\" style=\"font-size:12pt; margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient Id: %s | Date of visit: %s </p><br>" + "<b><p id=\"vitals_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px;; padding: 0px;\">Vitals</p></b>" + "<p id=\"vitals\" style=\"font-size:12pt;margin:0px; padding: 0px;\">Height(cm): %s | Weight(kg): %s | BMI: %s | Blood Pressure: %s | Pulse(bpm): %s | %s | Respiratory Rate: %s |  %s </p><br>" + "<b><p id=\"patient_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient History</p></b>" + "<p id=\"patient_history\" style=\"font-size:11pt;margin:0px; padding: 0px;\"> %s</p><br>" + "<b><p id=\"family_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Family History</p></b>" + "<p id=\"family_history\" style=\"font-size:11pt;margin: 0px; padding: 0px;\"> %s</p><br>" + "<b><p id=\"complaints_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Presenting complaint(s)</p></b>" + para_open + "%s" + para_close + "<br><br>" + "<u><b><p id=\"diagnosis_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Diagnosis</p></b></u>" + "%s<br>" + "<u><b><p id=\"rx_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Medication(s) plan</p></b></u>" + "%s<br>" + "<u><b><p id=\"tests_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Recommended Investigation(s)</p></b></u>" + "%s<br>" + "<u><b><p id=\"advice_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">General Advice</p></b></u>" + "%s<br>" + "<u><b><p id=\"follow_up_heading\" style=\"font-size:15pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Follow Up Date</p></b></u>" + "%s<br>" + "<div style=\"text-align:right;margin-right:50px;margin-top:0px;\">" +
                    //  "<span style=\"font-size:80pt;font-family: MyFont;padding: 0px;\">" + doctorSign + "</span>" +
                    "<img src=" + sign_url + " alt=\"Dr Signature\">" + // doctor signature...
                    doctorDetailStr + "<p style=\"font-size:12pt; margin-top:-0px; padding: 0px;\">" + doctrRegistartionNum + "</p>" + "</div>", heading, heading2, heading3, mPatientName, age, mGender, /*mSdw*/ address, mPatientOpenMRSID, mDate, (!TextUtils.isEmpty(mHeight)) ? mHeight : "", (!TextUtils.isEmpty(mWeight)) ? mWeight : "", (!TextUtils.isEmpty(mBMI)) ? mBMI : "", (!TextUtils.isEmpty(bp)) ? bp : "", (!TextUtils.isEmpty(mPulse)) ? mPulse : "", (!TextUtils.isEmpty(mTemp)) ? mTemp : "", (!TextUtils.isEmpty(mresp)) ? mresp : "", (!TextUtils.isEmpty(mSPO2)) ? mSPO2 : "", pat_hist, fam_hist, mComplaint, diagnosis_web, rx_web, tests_web, advice_web/*""*/, followUp_web, doctor_web);
            webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null);
        } else {
            String htmlDocument = String.format(font_face + "<b><p id=\"heading_1\" style=\"font-size:16pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<p id=\"heading_2\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<p id=\"heading_3\" style=\"font-size:12pt; margin: 0px; padding: 0px; text-align: center;\">%s</p>" + "<hr style=\"font-size:12pt;\">" + "<br/>" + "<p id=\"patient_name\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">%s</p></b>" + "<p id=\"patient_details\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Age: %s | Gender: %s </p>" + "<p id=\"address_and_contact\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Address and Contact: %s</p>" + "<p id=\"visit_details\" style=\"font-size:12pt; margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient Id: %s | Date of visit: %s </p><br>" + "<b><p id=\"vitals_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px;; padding: 0px;\">Vitals</p></b>" + "<p id=\"vitals\" style=\"font-size:12pt;margin:0px; padding: 0px;\">Height(cm): %s | Weight(kg): %s | BMI: %s | Blood Pressure: %s | Pulse(bpm): %s | %s | %s </p><br>" +
                                    /*"<b><p id=\"patient_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Patient History</p></b>" +
                                    "<p id=\"patient_history\" style=\"font-size:11pt;margin:0px; padding: 0px;\"> %s</p><br>" +
                                    "<b><p id=\"family_history_heading\" style=\"font-size:11pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Family History</p></b>" +
                                    "<p id=\"family_history\" style=\"font-size:11pt;margin: 0px; padding: 0px;\"> %s</p><br>" +*/
                            "<b><p id=\"complaints_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Presenting complaint(s)</p></b>" + para_open + "%s" + para_close + "<br><br>" + "<u><b><p id=\"diagnosis_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Diagnosis</p></b></u>" + "%s<br>" + "<u><b><p id=\"rx_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Medication(s) plan</p></b></u>" + "%s<br>" + "<u><b><p id=\"tests_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Recommended Investigation(s)</p></b></u>" + "%s<br>" + "<u><b><p id=\"advice_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">General Advice</p></b></u>" + "%s<br>" + "<u><b><p id=\"follow_up_heading\" style=\"font-size:12pt;margin-top:5px; margin-bottom:0px; padding: 0px;\">Follow Up Date</p></b></u>" + "%s<br>" + "<div style=\"text-align:right;margin-right:50px;margin-top:0px;\">" + "<span style=\"font-size:80pt;font-family: MyFont;padding: 0px;\">" + doctorSign + "</span><br>" + doctorDetailStr + "<span style=\"font-size:12pt; margin-top:5px; padding: 0px;\">" + doctrRegistartionNum + "</span>" + "</div>", heading, heading2, heading3, mPatientName, age, mGender, /*mSdw*/ address, mPatientOpenMRSID, mDate, (!TextUtils.isEmpty(mHeight)) ? mHeight : "", (!TextUtils.isEmpty(mWeight)) ? mWeight : "", (!TextUtils.isEmpty(mBMI)) ? mBMI : "", (!TextUtils.isEmpty(bp)) ? bp : "", (!TextUtils.isEmpty(mPulse)) ? mPulse : "", (!TextUtils.isEmpty(mTemp)) ? mTemp : "", (!TextUtils.isEmpty(mSPO2)) ? mSPO2 : "",
                    /*pat_hist, fam_hist,*/ mComplaint, diagnosis_web, rx_web, tests_web, /*advice_web*/"", followUp_web, doctor_web);
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
                        Toast.makeText(VisitSummaryActivity_New.this, getResources().getString(R.string.downloaded_to) + " " + path, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure() {
                        Toast.makeText(context, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }

                });
            } else {
                //to write to a pdf file...
                pdfPrint.print(printAdapter, dir, fileName, new PdfPrint.CallbackPrint() {
                    @Override
                    public void success(String path) {
                        Toast.makeText(VisitSummaryActivity_New.this, getResources().getString(R.string.downloaded_to) + " " + path, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure() {
                        Toast.makeText(context, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
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
            String jobName = getString(R.string.app_name) + " " + getResources().getString(R.string._visit_summary);

            PdfPrint pdfPrint = new PdfPrint(pBuilder.build());

            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/Intelehealth_PDF";
            String fileName = patientName.replace(" ", "_") + "_" + showVisitID() + ".pdf";

            File dir = new File(path);
            CustomLog.v(TAG, "dir.exists() : " + dir.exists());
            if (!dir.exists()) dir.mkdirs();


            //To display the preview window to user...
//            PrintJob printJob = printManager.print(jobName, printAdapter,
//                    pBuilder.build());

            //TODO: write different functions for <= Lollipop versions..
            //to write to a pdf file...
            pdfPrint.print(webView.createPrintDocumentAdapter(jobName), dir, fileName, new PdfPrint.CallbackPrint() {
                @Override
                public void success(String path) {
                    Toast.makeText(VisitSummaryActivity_New.this, getResources().getString(R.string.downloaded_to) + " " + path, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure() {
                    Toast.makeText(context, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(VisitSummaryActivity_New.this, getResources().getString(R.string.downloaded_to) + ": " + path, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure() {
                    Toast.makeText(context, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(VisitSummaryActivity_New.this, getResources().getString(R.string.downloaded_to) + ": " + path, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure() {
                    Toast.makeText(context, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }

            });
            //            PrintJob printJob = printManager.print(jobName, printAdapter,
//                    new PrintAttributes.Builder().build());

        }


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

    ActivityResultLauncher<Intent> mStartForEditVisit = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
//                        recreate();
                fetchingIntent();
                setViewsData();
                //for diagnostics section visit taking
                VisitDiagnosticsSummary visitDiagnosticsSummary = new VisitDiagnosticsSummary
                        (mBinding, VisitSummaryActivity_New.this, null,
                                VisitSummaryActivity_New.this, encounterVitals, mCommonVisitData);
                visitDiagnosticsSummary.initViews();
            }
        }
    });

    /*private String getTranslatedAssociatedSymptomQString(String localeCode) {
        if (localeCode.equalsIgnoreCase("hi")) {
            return "क्या आपको निम्न लक्षण है";
        } else if (localeCode.equalsIgnoreCase("or")) {
            return "ତମର ଏହି ଲକ୍ଷଣ ସବୁ ଅଛି କି?";
        } else {
            return "Do you have the following symptom(s)?";
        }
    }

    private String getTranslatedGeneralExamsQString(String localeCode) {
        if (localeCode.equalsIgnoreCase("hi")) {
            return "सामान्य परीक्षणै";
        } else if (localeCode.equalsIgnoreCase("or")) {
            return "ସାଧାରଣ ପରୀକ୍ଷା";
        } else {
            return "General Exams";
        }
    }*/

    private LinearLayout mAssociateSymptomsLinearLayout, mComplainSummaryLinearLayout, mPhysicalExamSummamryLinearLayout, mPastMedicalHistorySummaryLinearLayout, mFamilyHistorySummaryLinearLayout;
    private TextView mAssociateSymptomsLabelTextView;
    private boolean mIsCCInOldFormat = true;
    ;

    private void setQAData() {
        mIsCCInOldFormat = false;
        mFamilyHistorySummaryLinearLayout = findViewById(R.id.ll_family_history_summary);
        mPastMedicalHistorySummaryLinearLayout = findViewById(R.id.ll_patient_history_summary);

        mPhysicalExamSummamryLinearLayout = findViewById(R.id.ll_physical_exam_summary);

        mComplainSummaryLinearLayout = findViewById(R.id.ll_complain_summary);
        mAssociateSymptomsLinearLayout = findViewById(R.id.ll_associated_sympt);
        //mAssociateSymptomsLabelTextView = findViewById(R.id.tv_ass_complain_label);


        // complaints data
        if (complaint.getValue() != null) {
            String value = complaint.getValue();
            //boolean isInOldFormat = true;
            //Show Visit summary data in Clinical Format for English language only
            //Else for other language keep the data in Question Answer format
            Timber.tag(TAG).d("Complain => %s", value);
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
                }
            }
            CustomLog.v(TAG, "isInOldFormat: " + mIsCCInOldFormat);
            CustomLog.v(TAG, "complaint: " + value);
            String valueArray[] = null;
            boolean isAssociateSymptomFound = false;
            if (mIsCCInOldFormat) {
                complaintView.setVisibility(View.VISIBLE);
                reports_relative.setVisibility(View.VISIBLE);
                denies_relative.setVisibility(View.VISIBLE);

                valueArray = value.split("►<b> " + Node.ASSOCIATE_SYMPTOMS + "</b>:  <br/>");
                isAssociateSymptomFound = valueArray.length >= 2;
                CustomLog.v(TAG, "complaint: " + valueArray[0]);
                CustomLog.v(TAG, "complaint associated: " + (isAssociateSymptomFound ? valueArray[1] : "no Associated Symptom found in value"));
                String[] headerchips = valueArray[0].split("►");
                List<String> cc_tempvalues = new ArrayList<>(Arrays.asList(headerchips));

                // Emptying this list so that when the user comes back from the chief complaint screen - they see only 1 instance of values.
                if (!mChiefComplainList.isEmpty()) {
                    mChiefComplainList.clear();
                }

                for (int i = 0; i < cc_tempvalues.size(); i++) {
                    if (!cc_tempvalues.get(i).equalsIgnoreCase("") && cc_tempvalues.get(i).contains(":"))
                        mChiefComplainList.add(cc_tempvalues.get(i).substring(0, headerchips[i].indexOf(":")));
                }

                cc_recyclerview_gridlayout = new GridLayoutManager(this, 2);
                cc_recyclerview.setLayoutManager(cc_recyclerview_gridlayout);
                cc_adapter = new ComplaintHeaderAdapter(this, mChiefComplainList);
                cc_recyclerview.setAdapter(cc_adapter);

                String patientReports = getResources().getString(R.string.no_data_added);
                String patientDenies = getResources().getString(R.string.no_data_added);

                if (valueArray[0] != null)
                    complaintView.setText(Html.fromHtml(valueArray[0])); // todo: uncomment later
                if (isAssociateSymptomFound) {


                    if (valueArray[1].contains("• Patient reports") && valueArray[1].contains("• Patient denies")) {
                        String assoValueBlock[] = valueArray[1].replace("• Patient denies -<br>", "• Patient denies -<br/>").split("• Patient denies -<br/>");

                        // index 0 - Reports
                        String reports[] = assoValueBlock[0].replace("• Patient reports -<br>", "• Patient reports -<br/>").split("• Patient reports -<br/>");
                        patientReports = reports[1];
                        patientDenies = assoValueBlock[1];
                        complaintView.setText(Html.fromHtml(valueArray[0])); // todo: uncomment later
                    } else if (valueArray[1].contains("• Patient reports")) {
                        // todo: handle later -> comment added on 14 nov 2022
                        String reports[] = valueArray[1].replace("• Patient reports -<br>", "• Patient reports -<br/>").split("• Patient reports -<br/>");
                        patientReports = reports[1];
                    } else if (valueArray[1].contains("• Patient denies")) {
                        // todo: handle later -> comment added on 14 nov 2022
                        String assoValueBlock[] = valueArray[1].replace("• Patient denies -<br>", "• Patient denies -<br/>").split("• Patient denies -<br/>");
                        patientDenies = assoValueBlock[1];
                    }

                }

                // todo: testing:
            /*String data = "►Abdominal Pain: <br><span style=\"color:#7F7B92\">• Site</span> &emsp;&emsp; Upper (R) - Right Hypochondrium.<br>" +
                    "• Pain does not radiate.<br>• 4 Hours.<br><span style=\"color:#7F7B92\">• Onset</span> &emsp;&emsp; Gradual.<br><span style=\"color:#7F7B92\">• Timing</span> &emsp;&emsp; Morning.<br>" +
                    "<span style=\"color:#7F7B92\">• Character of the pain*</span> &emsp;&emsp; Constant.<br><span style=\"color:#7F7B92\">• Severity</span> &emsp;&emsp; Mild, 1-3.<br>" +
                    "<span style=\"color:#7F7B92\">• Exacerbating Factors</span> &emsp;&emsp; Hunger.<br><span style=\"color:#7F7B92\">• Relieving Factors</span> &emsp;&emsp; Food.<br><span style=\"color:#7F7B92\">• Prior treatment sought</span> &emsp;&emsp; None.";
            complaintView.setText(Html.fromHtml(data));*/
                // todo: testin end

                // associated symp.
                patientReports_txtview.setText(Html.fromHtml(patientReports));
                patientDenies_txtview.setText(Html.fromHtml(patientDenies));
            } else {
                /*String c1 = "►" + getTranslatedAssociatedSymptomQString(sessionManager.getAppLanguage());
                Log.v(TAG, "complaint c1: " + c1);
                valueArray = value.split(c1);
                isAssociateSymptomFound = valueArray.length >= 2;
                if (isAssociateSymptomFound)
                    valueArray[1] = valueArray[1].split("::")[1];*/
                setDataForChiefComplainSummary(complaintLocalString);
            }


        }
        // complaints data - end

        // phys exam data
        if (phyExam.getValue() != null) {
            String value = phyExam.getValue();
            boolean isInOldFormat = true;
            //Show Visit summary data in Clinical Format for English language only
            //Else for other language keep the data in Question Answer format
            if (value.startsWith("{") && value.endsWith("}")) {
                try {
                    JSONObject jsonObject = new JSONObject(value);
                    if (!sessionManager.getAppLanguage().equals("en") && jsonObject.has("l-" + sessionManager.getAppLanguage())) {
                        value = jsonObject.getString("l-" + sessionManager.getAppLanguage());
                        isInOldFormat = false;
                    } else {
                        value = jsonObject.getString("en");
                        isInOldFormat = true;
                    }
                    physicalExamLocaleString = value;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            CustomLog.v(TAG, "phyExam : " + value);
            if (isInOldFormat) {
                physFindingsView.setVisibility(View.VISIBLE);
                String[] valueArray;
                //general exam exam replaced as Obstetric History on UNFPA
                //that's why added the logic
                if (BuildConfig.FLAVOR_client == FlavorKeys.UNFPA) {
                    valueArray = value.replace("Obstetric History: <br>", "<b>Obstetric History: </b><br/>").split("<b>Obstetric History: </b><br/>");
                } else {
                    valueArray = value.replace("General exams: <br>", "<b>General exams: </b><br/>").split("<b>General exams: </b><br/>");
                }

                if (BuildConfig.FLAVOR_client == FlavorKeys.KCDO) {
                    physFindingsView.setText(Html.fromHtml(valueArray[0]));
                } else {
                    if (valueArray.length > 1)
                        physFindingsView.setText(Html.fromHtml(valueArray[1]));//.replaceFirst("<b>", "<br/><b>")));
                }

            } else {
                //physFindingsView.setText(Html.fromHtml(value.replaceFirst("<b>", "<br/><b>")));
                setDataForPhysicalExamSummary(physicalExamLocaleString);
            }
        }
        //image download for physcialExamination documents
        Paint p = new Paint();
        physcialExaminationDownloadText.setPaintFlags(p.getColor());
        physcialExaminationDownloadText.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        physcialExaminationImagesDownload();
        // phys exam data - end

        // medical history data

        // past medical hist
        if (patHistory.getValue() != null) {
            String value = patHistory.getValue();
            boolean isInOldFormat = true;
            //Show Visit summary data in Clinical Format for English language only
            //Else for other language keep the data in Question Answer format
            if (value.startsWith("{") && value.endsWith("}")) {
                try {
                    JSONObject jsonObject = new JSONObject(value);
                    if (!sessionManager.getAppLanguage().equals("en") && jsonObject.has("l-" + sessionManager.getAppLanguage())) {
                        value = jsonObject.getString("l-" + sessionManager.getAppLanguage());
                        isInOldFormat = false;
                    } else {
                        value = jsonObject.getString("en");
                        isInOldFormat = true;
                    }
                    patientHistoryLocaleString = value;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            CustomLog.v(TAG, "patHistory : " + value);
            if (isInOldFormat) {
                patHistView.setVisibility(View.VISIBLE);
                patHistView.setText(Html.fromHtml(value));
            } else setDataForPatientMedicalHistorySummary(patientHistoryLocaleString);
        }
        // past medical hist - end

        // family history
        if (famHistory.getValue() != null) {
            String value = famHistory.getValue();
            boolean isInOldFormat = true;
            //Show Visit summary data in Clinical Format for English language only
            //Else for other language keep the data in Question Answer format
            if (value.startsWith("{") && value.endsWith("}")) {
                try {
                    JSONObject jsonObject = new JSONObject(value);
                    if (!sessionManager.getAppLanguage().equals("en") && jsonObject.has("l-" + sessionManager.getAppLanguage())) {
                        value = jsonObject.getString("l-" + sessionManager.getAppLanguage());
                        isInOldFormat = false;
                    } else {
                        value = jsonObject.getString("en");
                        isInOldFormat = true;
                    }
                    familyHistoryLocaleString = value;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            CustomLog.v(TAG, "famHistory : " + value);
            if (isInOldFormat) {
                famHistView.setVisibility(View.VISIBLE);
                famHistView.setText(Html.fromHtml(value));
            } else setDataForFamilyHistorySummary(familyHistoryLocaleString);
        }
        // family history - end
        // medical history data - end
    }

    List<String> mChiefComplainList = new ArrayList<>();

    private void setDataForChiefComplainSummary(String answerInLocale) {
        mChiefComplainList.clear();
        String lCode = sessionManager.getAppLanguage();
        //String answerInLocale = mSummaryStringJsonObject.getString("l-" + lCode);
        answerInLocale = answerInLocale.replaceAll("<.*?>", "");
        System.out.println(answerInLocale);
        CustomLog.v(TAG, answerInLocale);
        //►दस्त::● आपको ये लक्षण कब से है• 6 घंटे● दस्त शुरू कैसे हुए?•धीरे धीरे● २४ घंटे में कितनी बार दस्त हुए?•३ से कम बार● दस्त किस प्रकार के है?•पक्का● क्या आपको पिछले महीनो में दस्त शुरू होने से पहले किसी असामान्य भोजन/तरल पदार्थ से अपच महसूस हुआ है•नहीं● क्या आपने आज यहां आने से पहले इस समस्या के लिए कोई उपचार (स्व-दवा या घरेलू उपचार सहित) लिया है या किसी स्वास्थ्य प्रदाता को दिखाया है?•कोई नहीं● अतिरिक्त जानकारी•bsbdbd►क्या आपको निम्न लक्षण है::•उल्टीPatient denies -•दस्त के साथ पेट दर्द•सुजन•मल में खून•बुखार•अन्य [वर्णन करे]

        String[] spt = answerInLocale.split("►");
        List<String> list = new ArrayList<>();
        String associatedSymptomsString = "";
        for (String s : spt) {
            if (s.isEmpty()) continue;
            //String s1 =  new String(s.getBytes(), "UTF-8");
            CustomLog.v(TAG, "Chunk - " + s);
            //if (s.trim().startsWith(getTranslatedAssociatedSymptomQString(lCode))) {
            //if (s.trim().contains("Patient denies -•")) {
            if (s.trim().contains(getTranslatedPatientDenies(lCode)) || s.trim().contains(getTranslatedAssociatedSymptomQString(lCode))) {
                associatedSymptomsString = s;
                CustomLog.v(TAG, "associatedSymptomsString - " + associatedSymptomsString);
            } else {
                list.add(s);
            }

        }
        mComplainSummaryLinearLayout.removeAllViews();


        for (int i = 0; i < list.size(); i++) {
            String complainName = "";
            List<VisitSummaryData> visitSummaryDataList = new ArrayList<>();
            String[] spt1 = list.get(i).split("●");
            for (String value : spt1) {
                if (value.contains("::")) {
                    complainName = value.replace("::", "");
                    System.out.println(complainName);
                    mChiefComplainList.add(complainName);
                } else {
                    String[] qa = value.split("•");
                    if (qa.length == 2) {
                        String k = value.split("•")[0].trim();
                        String v = value.split("•")[1].trim();
                        VisitSummaryData summaryData = new VisitSummaryData();
                        summaryData.setQuestion(k);
                        summaryData.setDisplayValue(v);
                        visitSummaryDataList.add(summaryData);
                    } else {


                        //String k = value.split("•")[0].trim();
                        StringBuilder stringBuilder = new StringBuilder();
                        String key = "";
                        String lastString = "";
                        for (int j = 0; j < qa.length; j++) {
                            String v1 = qa[j];
                            if (lastString.equals(v1)) continue;
                            //if (!stringBuilder.toString().isEmpty()) stringBuilder.append("\n");
                            stringBuilder.append(v1);
                            lastString = v1;
                            if (j % 2 != 0) {
                                String v = qa[j].trim();
                                VisitSummaryData summaryData = new VisitSummaryData();
                                summaryData.setQuestion(key);
                                summaryData.setDisplayValue(v);
                                visitSummaryDataList.add(summaryData);

                            } else {
                                key = qa[j].trim();
                            }
                        }
                    }
                }

            }

            if (!complainName.isEmpty() && !visitSummaryDataList.isEmpty()) {
                View view = View.inflate(this, R.layout.ui2_summary_main_row_item_view, null);
                TextView complainLabelTextView = view.findViewById(R.id.tv_complain_label);
                complainLabelTextView.setText(complainName);
                view.findViewById(R.id.tv_change).setVisibility(View.GONE);
                view.findViewById(R.id.height_adjust_view).setVisibility(View.GONE);
                RecyclerView recyclerView = view.findViewById(R.id.rcv_qa);
                recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
                SummaryViewAdapter summaryViewAdapter = new SummaryViewAdapter(recyclerView, this, visitSummaryDataList, new SummaryViewAdapter.OnItemSelection() {
                    @Override
                    public void onSelect(VisitSummaryData data) {

                    }
                });
                recyclerView.setAdapter(summaryViewAdapter);
                mComplainSummaryLinearLayout.addView(view);
            }
        }

        // set all chief complain list
        cc_recyclerview_gridlayout = new GridLayoutManager(this, 2);
        cc_recyclerview.setLayoutManager(cc_recyclerview_gridlayout);
        cc_adapter = new ComplaintHeaderAdapter(this, mChiefComplainList);
        cc_recyclerview.setAdapter(cc_adapter);

        // ASSOCIATED SYMPTOMS
        String[] tempAS = associatedSymptomsString.split("::");
        if (tempAS.length >= 2) {
            String title = tempAS[0];
            //mAssociateSymptomsLabelTextView.setText(title);  // not required

            associatedSymptomsString = tempAS[1];
        }
        String[] sections = associatedSymptomsString.split(getTranslatedPatientDenies(lCode));


        CustomLog.v(TAG, associatedSymptomsString);
        String[] spt1 = associatedSymptomsString.trim().split("•");
        CustomLog.e(TAG, associatedSymptomsString);
        CustomLog.e(TAG, String.valueOf(spt1.length));
        mAssociateSymptomsLinearLayout.removeAllViews();

        for (int i = 0; i < sections.length; i++) {
            String patientReports = sections[i]; // Patient reports & // Patient denies
            if (patientReports != null && patientReports.length() >= 2) {
                patientReports = patientReports.substring(1);
                patientReports = patientReports.replace("•", ", ");
                View view = View.inflate(this, R.layout.ui2_summary_qa_ass_sympt_row_item_view, null);
                TextView keyTextView = view.findViewById(R.id.tv_question_label);
                keyTextView.setText(i == 0 ? getString(R.string.patient_reports) : getString(R.string.patient_denies));
                TextView valueTextView = view.findViewById(R.id.tv_answer_value);
                valueTextView.setText(patientReports);
           /* if (patientReportsDenies.isEmpty()) {
                view.findViewById(R.id.iv_blt).setVisibility(View.GONE);
            } else {
                view.findViewById(R.id.iv_blt).setVisibility(View.VISIBLE);
            }*/
                //hiding associated symptoms for UNFPA
                if (BuildConfig.FLAVOR_client != FlavorKeys.UNFPA) {
                    mAssociateSymptomsLinearLayout.addView(view);
                }
            }
        }

        if (mAssociateSymptomsLinearLayout.getChildCount() == 0) {
            findViewById(R.id.associ_sym_label_tv).setVisibility(View.GONE);
        } else {
            findViewById(R.id.associ_sym_label_tv).setVisibility(View.GONE);
        }


            /*for (int i = 0; i < mAnsweredRootNodeList.size(); i++) {
                List<VisitSummaryData> itemList = new ArrayList<VisitSummaryData>();
                for (int j = 0; j < mAnsweredRootNodeList.get(i).getOptionsList().size(); j++) {
                    VisitSummaryData summaryData = new VisitSummaryData();
                    summaryData.setDisplayValue(mAnsweredRootNodeList.get(i).getOptionsList().get(j).getText());
                    itemList.add(summaryData);
                }
            }*/
    }

    private void setDataForPhysicalExamSummary(String summaryString) {
        mPhysicalExamSummamryLinearLayout.removeAllViews();
        String str = summaryString;//"►<b>Abdominal Pain</b>: <br/>• Site - Upper (C) - Epigastric.<br/>• Pain radiates to - Middle (R) - Right Lumbar.<br/>• Onset - Gradual.<br/>• Timing - Morning.<br/>• Character of the pain - Constant.<br/>• Severity - Mild, 1-3.<br/>• Exacerbating Factors - Hunger.<br/>• Relieving Factors - Food.<br/>• Prior treatment sought - None.<br/> ►<b>Associated symptoms</b>: <br/>• Patient reports -<br/> Anorexia <br/>• Patient denies -<br/> Diarrhea,  Constipation,  Fever<br/>";
        str = str.replaceAll("<.*?>", "");
        System.out.println("prepareSummary - " + str);
        String[] spt = str.split("►");
        List<String> list = new ArrayList<>();
        LinkedHashMap<String, List<String>> mapData = new LinkedHashMap<String, List<String>>();

        for (String s : spt) {
            System.out.println(s);
            if (s.isEmpty()) continue;
            String[] spt1 = s.split("•");
            String complainName = "";
            for (String s1 : spt1) {
                if (s1.trim().endsWith(":")) {
                    complainName = s1;
                    list = new ArrayList<>();
                    mapData.put(s1, list);
                } else {
                    mapData.get(complainName).add(s1);
                }
            }

        }
        System.out.println(mapData);
        for (Map.Entry<String, List<String>> entry : mapData.entrySet()) {
            String _complain = entry.getKey();
            List<String> _list = entry.getValue();

            if (!_complain.isEmpty() && !_list.isEmpty()) {
                View view = View.inflate(this, R.layout.ui2_summary_main_row_item_view, null);
                TextView complainLabelTextView = view.findViewById(R.id.tv_complain_label);
                complainLabelTextView.setText(getFormattedComplain(_complain));
                CustomLog.v("PH0_complain", _complain);
                if (_complain.trim().equalsIgnoreCase(VisitUtils.getTranslatedGeneralExamString(sessionManager.getAppLanguage()))) {
                    complainLabelTextView.setVisibility(View.GONE);
                }
                view.findViewById(R.id.height_adjust_view).setVisibility(View.GONE);
                view.findViewById(R.id.tv_change).setVisibility(View.GONE);
                RecyclerView recyclerView = view.findViewById(R.id.rcv_qa);
                recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
                List<VisitSummaryData> visitSummaryDataList = new ArrayList<>();
                String k1 = "";
                String lastString = "";

                for (int i = 0; i < _list.size(); i++) {
                    CustomLog.v("PH0", _list.get(i));
                    String val = _list.get(i);
                    String v1 = val;
                    if (lastString.equals(v1)) continue;
                    //if (!stringBuilder.toString().isEmpty()) stringBuilder.append("\n");
                    //stringBuilder.append(v1);
                    lastString = v1;
                    if (i % 2 != 0) {
                        String v = val.trim();
                        if (v.contains(":") && v.split(":").length > 1) {
                            v = v.split(":")[1];
                        }
                        VisitSummaryData summaryData = new VisitSummaryData();
                        summaryData.setQuestion(k1);
                        while (v.endsWith("-")) {
                            v = v.substring(0, v.length() - 1);
                        }
                        summaryData.setDisplayValue(v);
                        visitSummaryDataList.add(summaryData);

                    } else {
                        k1 = val.trim();
                        if (k1.contains("-●")) {
                            String[] temp = k1.split("-●");
                            VisitSummaryData summaryData = new VisitSummaryData();
                            summaryData.setQuestion(temp[0]);
                            summaryData.setDisplayValue("");
                            visitSummaryDataList.add(summaryData);
                            k1 = temp[1];
                        }
                    }
                }


                SummaryViewAdapter summaryViewAdapter = new SummaryViewAdapter(recyclerView, this, visitSummaryDataList, new SummaryViewAdapter.OnItemSelection() {

                    @Override
                    public void onSelect(VisitSummaryData data) {

                    }
                });
                recyclerView.setAdapter(summaryViewAdapter);
               /* SummarySingleViewAdapter summaryViewAdapter = new SummarySingleViewAdapter(recyclerView, getActivity(), _list, new SummarySingleViewAdapter.OnItemSelection() {
                    @Override
                    public void onSelect(String data) {

                    }
                });
                recyclerView.setAdapter(summaryViewAdapter);*/
                mPhysicalExamSummamryLinearLayout.addView(view);
            }
        }

    }

    /**
     * formatting complain here
     * if any unexpected complain has came then format it here
     *
     * @param complain
     * @return
     */
    private String getFormattedComplain(String complain) {
        if (!complain.trim().equals(getString(R.string.general_exam_title).trim())) {
            return complain;
        }
        return "";
    }

    private void setDataForPatientMedicalHistorySummary(String summaryStringPastHistory) {
        mPastMedicalHistorySummaryLinearLayout.removeAllViews();
        String str = summaryStringPastHistory;//"►<b>Abdominal Pain</b>: <br/>• Site - Upper (C) - Epigastric.<br/>• Pain radiates to - Middle (R) - Right Lumbar.<br/>• Onset - Gradual.<br/>• Timing - Morning.<br/>• Character of the pain - Constant.<br/>• Severity - Mild, 1-3.<br/>• Exacerbating Factors - Hunger.<br/>• Relieving Factors - Food.<br/>• Prior treatment sought - None.<br/> ►<b>Associated symptoms</b>: <br/>• Patient reports -<br/> Anorexia <br/>• Patient denies -<br/> Diarrhea,  Constipation,  Fever<br/>";
        //String str1 = mSummaryStringFamilyHistory;//"►<b>Abdominal Pain</b>: <br/>• Site - Upper (C) - Epigastric.<br/>• Pain radiates to - Middle (R) - Right Lumbar.<br/>• Onset - Gradual.<br/>• Timing - Morning.<br/>• Character of the pain - Constant.<br/>• Severity - Mild, 1-3.<br/>• Exacerbating Factors - Hunger.<br/>• Relieving Factors - Food.<br/>• Prior treatment sought - None.<br/> ►<b>Associated symptoms</b>: <br/>• Patient reports -<br/> Anorexia <br/>• Patient denies -<br/> Diarrhea,  Constipation,  Fever<br/>";
        str = str.replaceAll("<.*?>", "");
        //str1 = str1.replaceAll("<.*?>", "");
        System.out.println("mSummaryStringPastHistory - " + str);
        //System.out.println("mSummaryStringFamilyHistory - " + str1);
        String[] spt = str.split("●");
        //String[] spt1 = str1.split("●");
        List<String> list = new ArrayList<>();
        TreeMap<String, List<String>> mapData = new TreeMap<>(Collections.reverseOrder());
        mapData.put("Patient history", new ArrayList<>());
        //mapData.put("Family history", new ArrayList<>());
        for (String s : spt) {
            System.out.println(s);
            if (!s.trim().isEmpty()) mapData.get("Patient history").add(s.trim());


        }
        /*for (String s : spt1) {
            System.out.println(s);
            if (!s.trim().isEmpty())
                mapData.get("Family history").add(s.trim());


        }*/

        System.out.println(mapData);
        for (String key : mapData.keySet()) {

            String _complain = key.equalsIgnoreCase("Patient history") ? getString(R.string.title_activity_get_patient_history) : getString(R.string.title_activity_family_history);
            List<String> _list = mapData.get(key);

            if (!_complain.isEmpty() && !_list.isEmpty()) {
                View view = View.inflate(this, R.layout.ui2_summary_main_row_item_view, null);
                TextView complainLabelTextView = view.findViewById(R.id.tv_complain_label);
                View vv = view.findViewById(R.id.height_adjust_view);
                complainLabelTextView.setText(_complain);
                complainLabelTextView.setVisibility(View.GONE);
                vv.setVisibility(View.GONE);
                view.findViewById(R.id.tv_change).setVisibility(View.GONE);
                RecyclerView recyclerView = view.findViewById(R.id.rcv_qa);
                recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
                List<VisitSummaryData> visitSummaryDataList = new ArrayList<>();
                for (int i = 0; i < _list.size(); i++) {
                    CustomLog.v("K", "_list.get(i) - " + _list.get(i));
                    String[] qa = _list.get(i).split("•");
                    if (qa.length == 2) {
                        String k = qa[0].trim();
                        String v = qa[1].trim();
                        CustomLog.v("K", "k - " + k);
                        CustomLog.v("V", "V - " + v);
                        if (v.contains(":") && v.split(":").length > 1) {
                            v = v.split(":")[1];
                        }
                        VisitSummaryData summaryData = new VisitSummaryData();
                        summaryData.setQuestion(k.isEmpty() ? v : k);
                        summaryData.setDisplayValue(k.isEmpty() ? "" : v);
                        visitSummaryDataList.add(summaryData);
                    } else {
                        boolean isOddSequence = qa.length % 2 != 0;
                        CustomLog.v("isOddSequence", qa.length + " = " + isOddSequence);
                        //String k = value.split("•")[0].trim();
                        StringBuilder stringBuilder = new StringBuilder();
                        String k1 = "";
                        String lastString = "";
                        if (key.equalsIgnoreCase("Patient history")) {

                            for (int j = 0; j < qa.length; j++) {
                                boolean isLastItem = j == qa.length - 1;
                                String v1 = qa[j];
                                CustomLog.v("V", v1);
                                if (lastString.equals(v1)) continue;
                                //if (!stringBuilder.toString().isEmpty()) stringBuilder.append("\n");
                                stringBuilder.append(v1);
                                lastString = v1;
                                if (j % 2 != 0) {
                                    String v = qa[j].trim();
                                    if (v.contains(":") && v.split(":").length > 1) {
                                        v = v.split(":")[1];
                                    }


                                    VisitSummaryData summaryData = new VisitSummaryData();
                                    summaryData.setQuestion(k1);

                                    summaryData.setDisplayValue(v);
                                    visitSummaryDataList.add(summaryData);


                                } else {
                                    if (isLastItem && isOddSequence) {
                                        visitSummaryDataList.get(visitSummaryDataList.size() - 1).setDisplayValue(visitSummaryDataList.get(visitSummaryDataList.size() - 1).getDisplayValue() + bullet_arrow + qa[j].trim());
                                    } else {
                                        k1 = qa[j].trim();
                                    }
                                }
                            }
                        } else {
                            for (int j = 0; j < qa.length; j++) {
                                CustomLog.v("QA", "qa - " + qa[j]);
                                if (j == 0) {
                                    k1 = qa[j];
                                } else {
                                    if (!stringBuilder.toString().isEmpty())
                                        stringBuilder.append(bullet_arrow);
                                    stringBuilder.append(qa[j]);
                                }

                            }
                            VisitSummaryData summaryData = new VisitSummaryData();
                            summaryData.setQuestion(k1);
                            summaryData.setDisplayValue(stringBuilder.toString());
                            visitSummaryDataList.add(summaryData);
                        }

                    }


                }

                SummaryViewAdapter summaryViewAdapter = new SummaryViewAdapter(recyclerView, this, visitSummaryDataList, new SummaryViewAdapter.OnItemSelection() {

                    @Override
                    public void onSelect(VisitSummaryData data) {

                    }
                });
                recyclerView.setAdapter(summaryViewAdapter);
                mPastMedicalHistorySummaryLinearLayout.addView(view);
            }
        }

    }

    private void setDataForFamilyHistorySummary(String summaryStringFamilyHistory) {
        mFamilyHistorySummaryLinearLayout.removeAllViews();
        //String str = mSummaryStringPastHistory;//"►<b>Abdominal Pain</b>: <br/>• Site - Upper (C) - Epigastric.<br/>• Pain radiates to - Middle (R) - Right Lumbar.<br/>• Onset - Gradual.<br/>• Timing - Morning.<br/>• Character of the pain - Constant.<br/>• Severity - Mild, 1-3.<br/>• Exacerbating Factors - Hunger.<br/>• Relieving Factors - Food.<br/>• Prior treatment sought - None.<br/> ►<b>Associated symptoms</b>: <br/>• Patient reports -<br/> Anorexia <br/>• Patient denies -<br/> Diarrhea,  Constipation,  Fever<br/>";
        String str1 = summaryStringFamilyHistory;//"►<b>Abdominal Pain</b>: <br/>• Site - Upper (C) - Epigastric.<br/>• Pain radiates to - Middle (R) - Right Lumbar.<br/>• Onset - Gradual.<br/>• Timing - Morning.<br/>• Character of the pain - Constant.<br/>• Severity - Mild, 1-3.<br/>• Exacerbating Factors - Hunger.<br/>• Relieving Factors - Food.<br/>• Prior treatment sought - None.<br/> ►<b>Associated symptoms</b>: <br/>• Patient reports -<br/> Anorexia <br/>• Patient denies -<br/> Diarrhea,  Constipation,  Fever<br/>";
        //str = str.replaceAll("<.*?>", "");
        str1 = str1.replaceAll("<.*?>", "");
        //System.out.println("mSummaryStringPastHistory - " + str);
        System.out.println("mSummaryStringFamilyHistory - " + str1);
        //String[] spt = str.split("●");
        String[] spt1 = str1.split("●");
        List<String> list = new ArrayList<>();
        TreeMap<String, List<String>> mapData = new TreeMap<>(Collections.reverseOrder());
        //mapData.put("Patient history", new ArrayList<>());
        mapData.put("Family history", new ArrayList<>());
        /*for (String s : spt) {
            System.out.println(s);
            if (!s.trim().isEmpty())
                mapData.get("Patient history").add(s.trim());


        }*/
        for (String s : spt1) {
            System.out.println(s);
            if (!s.trim().isEmpty()) mapData.get("Family history").add(s.trim());


        }

        System.out.println(mapData);
        for (String key : mapData.keySet()) {

            String _complain = key.equalsIgnoreCase("Patient history") ? getString(R.string.title_activity_get_patient_history) : getString(R.string.title_activity_family_history);
            List<String> _list = mapData.get(key);

            if (!_complain.isEmpty() && !_list.isEmpty()) {
                View view = View.inflate(this, R.layout.ui2_summary_main_row_item_view, null);
                TextView complainLabelTextView = view.findViewById(R.id.tv_complain_label);
                View vv = view.findViewById(R.id.height_adjust_view);
                complainLabelTextView.setText(_complain);
                complainLabelTextView.setVisibility(View.GONE);
                vv.setVisibility(View.GONE);
                view.findViewById(R.id.tv_change).setVisibility(View.GONE);
                RecyclerView recyclerView = view.findViewById(R.id.rcv_qa);
                recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
                List<VisitSummaryData> visitSummaryDataList = new ArrayList<>();
                for (int i = 0; i < _list.size(); i++) {
                    CustomLog.v("K", "_list.get(i) - " + _list.get(i));
                    String[] qa = _list.get(i).split("•");
                    if (qa.length == 2) {
                        String k = qa[0].trim();
                        String v = qa[1].trim();
                        CustomLog.v("K", "k - " + k);
                        CustomLog.v("V", "V - " + v);
                        if (v.contains(":") && v.split(":").length > 1) {
                            v = v.split(":")[1];
                        }
                        VisitSummaryData summaryData = new VisitSummaryData();
                        summaryData.setQuestion(k.isEmpty() ? v : k);
                        summaryData.setDisplayValue(k.isEmpty() ? "" : v);
                        visitSummaryDataList.add(summaryData);
                    } else {
                        boolean isOddSequence = qa.length % 2 != 0;
                        CustomLog.v("isOddSequence", qa.length + " = " + isOddSequence);
                        //String k = value.split("•")[0].trim();
                        StringBuilder stringBuilder = new StringBuilder();
                        String k1 = "";
                        String lastString = "";
                        if (key.equalsIgnoreCase("Patient history")) {

                            for (int j = 0; j < qa.length; j++) {
                                boolean isLastItem = j == qa.length - 1;
                                String v1 = qa[j];
                                CustomLog.v("V", v1);
                                if (lastString.equals(v1)) continue;
                                //if (!stringBuilder.toString().isEmpty()) stringBuilder.append("\n");
                                stringBuilder.append(v1);
                                lastString = v1;
                                if (j % 2 != 0) {
                                    String v = qa[j].trim();
                                    if (v.contains(":") && v.split(":").length > 1) {
                                        v = v.split(":")[1];
                                    }


                                    VisitSummaryData summaryData = new VisitSummaryData();
                                    summaryData.setQuestion(k1);

                                    summaryData.setDisplayValue(v);
                                    visitSummaryDataList.add(summaryData);


                                } else {
                                    if (isLastItem && isOddSequence) {
                                        visitSummaryDataList.get(visitSummaryDataList.size() - 1).setDisplayValue(visitSummaryDataList.get(visitSummaryDataList.size() - 1).getDisplayValue() + bullet_arrow + qa[j].trim());
                                    } else {
                                        k1 = qa[j].trim();
                                    }
                                }
                            }
                        } else {
                            for (int j = 0; j < qa.length; j++) {
                                CustomLog.v("QA", "qa - " + qa[j]);
                                if (j == 0) {
                                    k1 = qa[j];
                                } else {
                                    if (!stringBuilder.toString().isEmpty())
                                        stringBuilder.append(bullet_arrow);
                                    stringBuilder.append(qa[j]);
                                }

                            }
                            VisitSummaryData summaryData = new VisitSummaryData();
                            summaryData.setQuestion(k1);
                            summaryData.setDisplayValue(stringBuilder.toString());
                            visitSummaryDataList.add(summaryData);
                        }

                    }


                }
                CustomLog.v("visitSummaryDataList", visitSummaryDataList.size() + " visitSummaryDataList");
                SummaryViewAdapter summaryViewAdapter = new SummaryViewAdapter(recyclerView, this, visitSummaryDataList, new SummaryViewAdapter.OnItemSelection() {

                    @Override
                    public void onSelect(VisitSummaryData data) {

                    }
                });
                recyclerView.setAdapter(summaryViewAdapter);
                mFamilyHistorySummaryLinearLayout.addView(view);
            }
        }

    }

    private void generateAndViewBillData() {
        if (complaintView.getText().toString().contains("Follow up visit"))
            visitType = "Follow-Up";
        BillDetails billModel = new BillDetails();
        billModel.setPatientVisitID(visitUuid);
        billModel.setPatientHideVisitID(showVisitID());
        billModel.setVisitType(visitType);
        //billModel.setReceiptPaymentStatus();
        Log.d(TAG, "kkgenerateAndViewBillData: visitUuid : " + visitUuid);
        Log.d(TAG, "kkgenerateAndViewBillData: showVisitID() : " + showVisitID());
        Log.d(TAG, "kkgenerateAndViewBillData: visitType : " + visitType);

        if (isVisitSpecialityExists && mFeatureActiveStatus.getGenerateBillButton()) {
            speciality_spinner.setEnabled(false);
            mBinding.btnGenerateBill.setVisibility(View.VISIBLE);
        }
        VisitSummaryBillUtils billUtils = new VisitSummaryBillUtils(VisitSummaryActivity_New.this, patient, billModel);

        String billEncounterUuid = billUtils.checkForOldBill();
        Log.d(TAG, "generateAndViewBillData: billEncounterUuid : " + billEncounterUuid);
        if (!billEncounterUuid.equals("")) {
            //mBinding.editDiagnostics.setVisibility(View.GONE);//temp
            mBinding.btnGenerateBill.setText(getResources().getString(R.string.view_bill));
        }
        mBinding.btnGenerateBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String billEncounterUuid = billUtils.checkForOldBill();
                if (!billEncounterUuid.equals("")) {
                    billUtils.fetchBillDetails(billEncounterUuid);
                } else {
                    boolean[] selectedTests = new boolean[8];
                    if (!mBinding.layoutVisitSummarySections.textViewGlucoseRandomValue.getText().toString().isEmpty() && isNumeric(mBinding.layoutVisitSummarySections.textViewGlucoseRandomValue.getText().toString()))
                        selectedTests[1] = false;//no use seen
                    if (!mBinding.layoutVisitSummarySections.textViewGlucoseFastingValue.getText().toString().isEmpty() && isNumeric(mBinding.layoutVisitSummarySections.textViewGlucoseFastingValue.getText().toString()))
                        selectedTests[2] = true;
                    if (!mBinding.layoutVisitSummarySections.textViewPostPrandialValue.getText().toString().isEmpty() && isNumeric(mBinding.layoutVisitSummarySections.textViewPostPrandialValue.getText().toString()))
                        selectedTests[3] = true;
                    if (!mBinding.layoutVisitSummarySections.textViewGlucoseRandomValue.getText().toString().isEmpty() && isNumeric(mBinding.layoutVisitSummarySections.textViewGlucoseRandomValue.getText().toString()))
                        selectedTests[4] = true;
                    if (!mBinding.layoutVisitSummarySections.textViewUricAcidValue.getText().toString().isEmpty() && isNumeric(mBinding.layoutVisitSummarySections.textViewUricAcidValue.getText().toString()))
                        selectedTests[5] = true;
                    if (!mBinding.layoutVisitSummarySections.textViewTotalCholestrolValue.getText().toString().isEmpty() && isNumeric(mBinding.layoutVisitSummarySections.textViewTotalCholestrolValue.getText().toString()))
                        selectedTests[6] = true;
                    if (!mBinding.layoutVisitSummarySections.textViewHemoglobinValue.getText().toString().isEmpty() && isNumeric(mBinding.layoutVisitSummarySections.textViewHemoglobinValue.getText().toString()))
                        selectedTests[7] = true;
                    Log.d(TAG, "onClick: selectedTests :: "+new Gson().toJson(selectedTests));
                    billUtils.showTestConfirmationCustomDialog(selectedTests);

                }
            }
        });

    }

    public boolean isNumeric(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        for (char c : input.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }
    private void setupDiagnosticsConfig() {
        mRandomGlucoseLinearLayout = findViewById(R.id.ll_glucose_random_container);
        mFastingGlucoseLinearLayout = findViewById(R.id.ll_glucose_fasting_container);
        mPostPrandialLinearLayout = findViewById(R.id.ll_post_prandial_container);
        mHemoglobinLinearLayout = findViewById(R.id.ll_hemoglobin_container);
        mUricAcidLinearLayout = findViewById(R.id.ll_uric_acid_container);
        mCholestrolLinearLayout = findViewById(R.id.ll_total_cholestrol_container);


        DiagnosticsRepository repository = new DiagnosticsRepository(ConfigDatabase.getInstance(this).patientDiagnosticsDao());
        DiagnosticsViewModelFactory factory = new DiagnosticsViewModelFactory(repository);
        DiagnosticsViewModel diagnosticsViewModel = new ViewModelProvider(this, factory).get(DiagnosticsViewModel.class);
        diagnosticsViewModel.getAllEnabledLiveFields()
                .observe(this, it -> {
                    mPatientDiagnosticsList = it;
                            CustomLog.v(TAG, new Gson().toJson(mPatientDiagnosticsList));
                            updateUIForDiagnostics();
                        }
                );

    }

    private void updateUIForDiagnostics() {
        mRandomGlucoseLinearLayout.setVisibility(View.GONE);
        mFastingGlucoseLinearLayout.setVisibility(View.GONE);
        mPostPrandialLinearLayout.setVisibility(View.GONE);
        mHemoglobinLinearLayout.setVisibility(View.GONE);
        mUricAcidLinearLayout.setVisibility(View.GONE);
        mCholestrolLinearLayout.setVisibility(View.GONE);

        for (Diagnostics diagnostics : mPatientDiagnosticsList) {
            CustomLog.v(TAG, diagnostics.getName() + "\t" + diagnostics.getDiagnosticsKey());

            if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.RANDOM_BLOOD_SUGAR)) {
                mRandomGlucoseLinearLayout.setVisibility(View.VISIBLE);

            } else if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.FASTING_BLOOD_SUGAR)) {
                mFastingGlucoseLinearLayout.setVisibility(View.VISIBLE);

            } else if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.POST_PRANDIAL_BLOOD_SUGAR)) {
                mPostPrandialLinearLayout.setVisibility(View.VISIBLE);

            } else if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.HEAMOGLOBIN)) {
                mHemoglobinLinearLayout.setVisibility(View.VISIBLE);

            } else if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.URIC_ACID)) {
                mUricAcidLinearLayout.setVisibility(View.VISIBLE);

            } else if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.TOTAL_CHOLESTEROL)) {
                mCholestrolLinearLayout.setVisibility(View.VISIBLE);

            }
        }
    }

}