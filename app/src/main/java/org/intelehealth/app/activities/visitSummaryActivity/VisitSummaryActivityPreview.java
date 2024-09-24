package org.intelehealth.app.activities.visitSummaryActivity;

import static org.intelehealth.app.app.AppConstants.CONFIG_FILE_NAME;
import static org.intelehealth.app.ayu.visit.common.VisitUtils.convertCtoF;
import static org.intelehealth.app.ayu.visit.common.VisitUtils.getTranslatedAssociatedSymptomQString;
import static org.intelehealth.app.ayu.visit.common.VisitUtils.getTranslatedPatientDenies;
import static org.intelehealth.app.database.dao.EncounterDAO.fetchEncounterUuidForEncounterAdultInitials;
import static org.intelehealth.app.database.dao.EncounterDAO.fetchEncounterUuidForEncounterVitals;
import static org.intelehealth.app.database.dao.ObsDAO.fetchValueFromLocalDb;
import static org.intelehealth.app.knowledgeEngine.Node.bullet_arrow;
import static org.intelehealth.app.ui2.utils.CheckInternetAvailability.isNetworkAvailable;
import static org.intelehealth.app.utilities.DateAndTimeUtils.parse_DateToddMMyyyy;
import static org.intelehealth.app.utilities.DateAndTimeUtils.parse_DateToddMMyyyy_new;
import static org.intelehealth.app.utilities.StringUtils.setGenderAgeLocal;
import static org.intelehealth.app.utilities.UuidDictionary.ADDITIONAL_NOTES;
import static org.intelehealth.app.utilities.UuidDictionary.CLOSE_CASE;
import static org.intelehealth.app.utilities.UuidDictionary.FACILITY;
import static org.intelehealth.app.utilities.UuidDictionary.SEVERITY;
import static org.intelehealth.app.utilities.UuidDictionary.SPECIALITY;
import static org.intelehealth.app.utilities.UuidDictionary.VISIT_SUMMARY_LINK;
import static org.intelehealth.app.utilities.VisitUtils.endVisit;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.LocaleList;
import android.print.PdfConverter;
import android.print.PdfPrint;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.provider.MediaStore;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.print.PrintHelper;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.ajalt.timberkt.Timber;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.R;
import org.intelehealth.app.activities.additionalDocumentsActivity.AdditionalDocumentAdapter;
import org.intelehealth.app.activities.identificationActivity.IdentificationActivity_New;
import org.intelehealth.app.activities.notification.AdapterInterface;
import org.intelehealth.app.activities.prescription.PrescriptionBuilder;
import org.intelehealth.app.activities.visitSummaryActivity.facilitytovisit.FacilityToVisitModel;
import org.intelehealth.app.activities.visitSummaryActivity.model.ReferralFacilityData;
import org.intelehealth.app.adapter.PdfPrintDocumentAdapter;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.appointment.dao.AppointmentDAO;
import org.intelehealth.app.appointment.model.AppointmentInfo;
import org.intelehealth.app.ayu.visit.common.VisitUtils;
import org.intelehealth.app.ayu.visit.common.adapter.SummaryViewAdapter;
import org.intelehealth.app.ayu.visit.model.CommonVisitData;
import org.intelehealth.app.ayu.visit.model.VisitSummaryData;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.database.dao.ProviderDAO;
import org.intelehealth.app.database.dao.RTCConnectionDAO;
import org.intelehealth.app.database.dao.VisitAttributeListDAO;
import org.intelehealth.app.enums.ReferralFacilityDataFormatType;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.models.ClsDoctorDetails;
import org.intelehealth.app.models.DocumentObject;
import org.intelehealth.app.models.NotificationModel;
import org.intelehealth.app.models.Patient;
import org.intelehealth.app.models.VisitSummaryPdfData;
import org.intelehealth.app.models.VitalsObject;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.models.dto.ProviderDTO;
import org.intelehealth.app.models.dto.RTCConnectionDTO;
import org.intelehealth.app.services.DownloadService;
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.ui.specialization.SpecializationArrayAdapter;
import org.intelehealth.app.ui2.utils.CheckInternetAvailability;
import org.intelehealth.app.utilities.AppointmentUtils;
import org.intelehealth.app.utilities.BitmapUtils;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.DownloadFilesUtils;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.LanguageUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.NetworkUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.TooltipWindow;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.app.webrtc.activity.IDAChatActivity;
import org.intelehealth.config.presenter.language.factory.SpecializationViewModelFactory;
import org.intelehealth.config.presenter.specialization.data.SpecializationRepository;
import org.intelehealth.config.presenter.specialization.viewmodel.SpecializationViewModel;
import org.intelehealth.config.room.ConfigDatabase;
import org.intelehealth.config.room.entity.FeatureActiveStatus;
import org.intelehealth.config.room.entity.Specialization;
import org.intelehealth.config.utility.ResUtils;
import org.intelehealth.ihutils.ui.CameraActivity;
import org.intelehealth.klivekit.model.RtcArgs;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
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
 * Created by: Tanvir 0n 27-05-24 : 2:18 PM
 */
@SuppressLint("Range")
public class VisitSummaryActivityPreview extends BaseActivity implements AdapterInterface, NetworkUtils.InternetCheckUpdateInterface, PdfConverter.Companion.OnComplete {
    private static final String TAG = VisitSummaryActivityPreview.class.getSimpleName();
    private static final int PICK_IMAGE_FROM_GALLERY = 2001;
    //SQLiteDatabase db;
    Button btn_vs_sendvisit;
    private Context context;
    private ImageButton btn_up_header, btn_up_vitals_header, btn_up_visitreason_header, btn_up_phyexam_header, btn_up_medhist_header, btn_up_addnotes_vd_header;
    private RelativeLayout vitals_header_relative, chiefcomplaint_header_relative, physExam_header_relative, pathistory_header_relative, addnotes_vd_header_relative, special_vd_header_relative;
    private RelativeLayout vs_header_expandview, vs_vitals_header_expandview, vd_special_header_expandview, vs_visitreason_header_expandview, vs_phyexam_header_expandview, vs_medhist_header_expandview, vd_addnotes_header_expandview, vs_add_notes, parentLayout;
    private RelativeLayout add_additional_doc, add_doc_relative, doc_speciality_card;
    private LinearLayout btn_bottom_vs;
    private Button visit_summary_preview;
    SessionManager sessionManager, sessionManager1;
    String appLanguage, patientUuid, visitUuid, state, patientName, patientGender, intentTag, visitUUID, medicalAdvice_string = "", medicalAdvice_HyperLink = "", isSynedFlag = "";
    private float float_ageYear_Month;
    String encounterVitals, encounterUuidAdultIntial, EncounterAdultInitial_LatestVisit;
    SharedPreferences mSharedPreference;
    Boolean isPastVisit = false, isVisitSpecialityExists = false;
    Boolean isReceiverRegistered = false;
    ArrayList<String> physicalExams;
    VisitSummaryActivityPreview.DownloadPrescriptionService downloadPrescriptionService;
    private RecyclerView mAdditionalDocsRecyclerView, mPhysicalExamsRecyclerView, cc_recyclerview;
    private RecyclerView.LayoutManager mAdditionalDocsLayoutManager, mPhysicalExamsLayoutManager;
    Spinner speciality_spinner;
    private RecyclerView.LayoutManager cc_recyclerview_gridlayout;
    private AdditionalDocumentAdapter recyclerViewAdapter;
    private ComplaintHeaderAdapter cc_adapter;
    private String mEngReason = "";

    boolean hasLicense = false;
    private boolean hasPrescription = false;
    private boolean isRespiratory = false, uploaded = false, downloaded = false;
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
    TextView complaintView, patientReports_txtview, patientDenies_txtview;
    TextView famHistView;
    TextView patHistView;
    TextView physFindingsView;
    TextView mDoctorTitle;
    TextView mDoctorName;
    TextView mCHWname;
    TextView add_docs_title, tvAddNotesValueVS, reminder, incomplete_act, archieved_notifi;
    String addnotes_value = "";
    TextView respiratory;
    TextView respiratoryText;
    TextView tempfaren;
    TextView tempcel;
    TextView blur_txtview;
    String medHistory;
    String baseDir;
    String filePathPhyExam;
    File obsImgdir;
    String gender_tv;
    String mFileName = CONFIG_FILE_NAME;
    String mHeight, mWeight, mBMI, mBP, mPulse, mTemp, mSPO2, mresp;
    String speciality_selected = "";
    private TextView physcialExaminationDownloadText, vd_special_value;
    NetworkChangeReceiver receiver;
    public static final String FILTER = "io.intelehealth.client.activities.visit_summary_activity.REQUEST_PROCESSED";
    String encounterUuid;
    SwitchMaterial flag;
    private Handler mBackgroundHandler;
    private List<DocumentObject> rowListItem;
    String sign_url;

    LinearLayout editVitals, editPhysical, editFamHist, editMedHist, editComplaint, cc_details_edit, ass_symp_edit;
    ImageButton editAddDocs;
    ImageButton btn_up_special_vd_header;

    ImageView profile_image;
    String profileImage = "";
    String profileImage1 = "";
    ImagesDAO imagesDAO = new ImagesDAO();
    private WebView mWebView;
    public static String prescription1;
    public static String prescription2;
    private CardView profileImageCard;
    RelativeLayout special_vd_card, addnotes_vd_card;
    private VisitAttributeListDAO visitAttributeListDAO = new VisitAttributeListDAO();
    private ImageButton backArrow, priority_hint;
    private NetworkUtils networkUtils;
    private static final int SCHEDULE_LISTING_INTENT = 2001;
    private static final int GROUP_PERMISSION_REQUEST = 1000;
    private static final int DIALOG_CAMERA_PERMISSION_REQUEST = 3000;
    private static final int DIALOG_GALLERY_PERMISSION_REQUEST = 4000;
    Button openall_btn;
    private FrameLayout filter_framelayout;
    private View hl_2;
    private boolean priorityVisit = false;
    private ObjectAnimator syncAnimator;
    TooltipWindow tipWindow;
    Boolean doesAppointmentExist = false;

    private CommonVisitData mCommonVisitData;

    private SpecializationViewModel viewModel;

    Button printBt, sharePatientBt, shareFacilityBt;
    private Bitmap bitmap;
    private String filePath;
    private ArrayList<File> fileList;
    private VisitSummaryPdfData visitSummaryPdfData = new VisitSummaryPdfData();
    private StringBuilder chifComplainStringBuilder;
    private View view;
    private StringBuilder physicalExamStringBuilder;
    private StringBuilder medicalHistoryStringBuilder;
    String htmlContent = "<b>Pdf</b>";
    private List<FacilityToVisitModel> facilityList = null;
    private List<String> severityList = null;
    private boolean mIsFollowUpTypeVisit = false;
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
            IDAChatActivity.startChatActivity(VisitSummaryActivityPreview.this, args);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_summary_preview);
        setupSpecialization();
        context = VisitSummaryActivityPreview.this;

        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(Color.WHITE);

        //db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();

        initUI();
        networkUtils = new NetworkUtils(this, this);
        fetchingIntent();
        setViewsData();
        expandableCardVisibilityHandling();
        tipWindow = new TooltipWindow(VisitSummaryActivityPreview.this);

        removeUnnecessaryView();
    }

    private void removeUnnecessaryView() {
        editVitals.setVisibility(View.GONE);
        editComplaint.setVisibility(View.GONE);
        cc_details_edit.setVisibility(View.GONE);
        ass_symp_edit.setVisibility(View.GONE);
        editPhysical.setVisibility(View.GONE);
        editFamHist.setVisibility(View.GONE);
        editMedHist.setVisibility(View.GONE);
        editAddDocs.setVisibility(View.GONE);

        openall_btn.setVisibility(View.GONE);
        btn_up_vitals_header.setVisibility(View.GONE);
        btn_up_visitreason_header.setVisibility(View.GONE);
        btn_up_phyexam_header.setVisibility(View.GONE);
        btn_up_medhist_header.setVisibility(View.GONE);
        btn_up_special_vd_header.setVisibility(View.GONE);
        btn_up_addnotes_vd_header.setVisibility(View.GONE);
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
            mIsFollowUpTypeVisit = intent.getBooleanExtra("IsFollowUpTypeVisit", false);
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
                throw new RuntimeException(e);
            }

            Set<String> selectedExams = sessionManager.getVisitSummary(patientUuid);
            if (physicalExams == null) physicalExams = new ArrayList<>();
            physicalExams.clear();
            if (selectedExams != null && !selectedExams.isEmpty()) {
                physicalExams.addAll(selectedExams);
            }

            queryData(String.valueOf(patientUuid));
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

        if (intentTag != null && !intentTag.isEmpty()) {

            boolean isAllowForEdit = false;
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

                special_vd_card.setVisibility(View.VISIBLE);
                // vs_add_notes.setVisibility(View.GONE);

                addnotes_vd_card.setVisibility(View.VISIBLE);
                tvAddNotesValueVS.setVisibility(View.VISIBLE);
                String addNote = getString(R.string.no_notes_added_for_doctor);
                addnotes_value = visitAttributeListDAO.getVisitAttributesList_specificVisit(visitUuid, ADDITIONAL_NOTES);
                if (!addnotes_value.equalsIgnoreCase("")) {
                    if (addnotes_value.equalsIgnoreCase("No notes added for Doctor.")) {
                        tvAddNotesValueVS.setText(getString(R.string.no_notes_added_for_doctor));
                        visitSummaryPdfData.setAdditionalNote(getString(R.string.no_notes_added_for_doctor));
                    } else {
                        tvAddNotesValueVS.setText(addnotes_value);
                        addNote = addnotes_value;
                        visitSummaryPdfData.setAdditionalNote(addnotes_value);
                    }
                } else {
                    addnotes_value = getString(R.string.no_notes_added_for_doctor);  // "No notes added for Doctor."
                    tvAddNotesValueVS.setText(addnotes_value);
                }

                visitSummaryPdfData.setAdditionalNote(addNote);
            } else {
                editVitals.setVisibility(View.VISIBLE);
                editComplaint.setVisibility(View.VISIBLE);
                editPhysical.setVisibility(View.VISIBLE);
                editFamHist.setVisibility(View.VISIBLE);
                editMedHist.setVisibility(View.VISIBLE);
                editAddDocs.setVisibility(View.VISIBLE);

                add_additional_doc.setVisibility(View.GONE);


                special_vd_card.setVisibility(View.GONE);
                // vs_add_notes.setVisibility(View.VISIBLE);

                addnotes_vd_card.setVisibility(View.VISIBLE);
                tvAddNotesValueVS.setVisibility(View.GONE);

            }
            // Edit btn visibility based on user coming from Visit Details screen - End

        }


        if (!isVisitSpecialityExists) {
            special_vd_card.setVisibility(View.GONE);
            flag.setEnabled(true);
        } else {
            flag.setEnabled(false);
        }
        flag.setClickable(false);
        visitSummaryPdfData.setPriorityVisit(ContextCompat.getString(this, R.string.no));
        Timber.tag(TAG).d("has prescription::%s", hasPrescription);
        updateUIState();

    }

    private void updateUIState() {
        if (hasPrescription) {
            special_vd_card.setVisibility(View.VISIBLE);

            add_additional_doc.setVisibility(View.GONE);
            editAddDocs.setVisibility(View.GONE);
        } else {
            isVisitSpecialityExists = speciality_row_exist_check(visitUUID);
            int visibility = isVisitSpecialityExists ? View.GONE : View.VISIBLE;
            editAddDocs.setVisibility(visibility);
            if (recyclerViewAdapter != null) {
                recyclerViewAdapter.hideCancelBtnAddDoc(visibility == View.GONE);
            }
        }

        bindCloseCaseReason();
    }

    private void bindCloseCaseReason() {
        String closeReason = visitAttributeListDAO.getVisitAttributesList_specificVisit(visitUuid, CLOSE_CASE);
        closeReason = LanguageUtils.getLocalValueFromArray(this, closeReason, R.array.close_case_reason);
        if (!TextUtils.isEmpty(closeReason)) {
            visitSummaryPdfData.setCloseCaseReason(" " + Node.bullet + "  " + closeReason);
            TextView tvCloseCaseReason = findViewById(R.id.tvCloseCaseToVisitValue);
            tvCloseCaseReason.setText(" " + Node.bullet + "  " + closeReason);
            findViewById(R.id.flCloseCaseToVisit).setVisibility(View.VISIBLE);
        } else findViewById(R.id.flCloseCaseToVisit).setVisibility(View.GONE);
    }

    private int mOpenCount = 0;

    private void expandableCardVisibilityHandling() {

        vs_header_expandview.setVisibility(View.VISIBLE);

        vs_vitals_header_expandview.setVisibility(View.VISIBLE);

        vs_visitreason_header_expandview.setVisibility(View.VISIBLE);

        vs_phyexam_header_expandview.setVisibility(View.VISIBLE);

        vs_medhist_header_expandview.setVisibility(View.VISIBLE);

        vd_special_header_expandview.setVisibility(View.VISIBLE);

        vd_addnotes_header_expandview.setVisibility(View.VISIBLE);
        findViewById(R.id.rlSavertyHeaderExpandView).setVisibility(View.VISIBLE);
        findViewById(R.id.rlFacilityToVisitHeaderExpandView).setVisibility(View.VISIBLE);


        Log.d("visitUUID", "onCreate_uuid: " + visitUuid);
        isVisitSpecialityExists = speciality_row_exist_check(visitUuid);
        if (isVisitSpecialityExists) {
            speciality_spinner.setEnabled(false);
            flag.setEnabled(false);
            flag.setClickable(false);
        } else {
            flag.setEnabled(true);
            flag.setClickable(true);
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
            }

            if (!emergencyUuid.isEmpty() || !emergencyUuid.equalsIgnoreCase("")) {
                flag.setChecked(true);
                flag.setEnabled(false);
                priorityVisit = true;
                visitSummaryPdfData.setPriorityVisit(ContextCompat.getString(this, R.string.yes));
            }
        }
    }

    private void setupSpecialization() {
        ConfigDatabase db = ConfigDatabase.getInstance(getApplicationContext());
        SpecializationRepository repository = new SpecializationRepository(db.specializationDao());
        viewModel = new ViewModelProvider(this, new SpecializationViewModelFactory(repository)).get(SpecializationViewModel.class);
        viewModel.fetchSpecialization().observe(this, specializations -> {
            Timber.tag(TAG).d(new Gson().toJson(specializations));
            setupSpecializationDataSpinner(specializations);
            setFacilityToVisitSpinner();
            setReferralFacilitySpinner();
            setSeveritySpinner();
            String followupValue = fetchValueFromLocalDb(visitUUID);
            Timber.tag(TAG).d("follow up=>%s", followupValue);
            if (!TextUtils.isEmpty(followupValue)) {
                try {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ((TextView) findViewById(R.id.tvViewFollowUpDateTime)).setText(getFormattedDateTime(followupValue));
                    visitSummaryPdfData.setFollowUpDate(getFormattedDateTime(followupValue));
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                String noInfo = getString(R.string.no_information);
                ((TextView) findViewById(R.id.tvViewFollowUpDateTime)).setText(noInfo);
                visitSummaryPdfData.setFollowUpDate(noInfo);
            }
        });
    }

    //    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String getFormattedDateTime(String followupValue) {
        // Extract the date and time part
        String datePart = followupValue.split(", Time:")[0];
        String timePart = followupValue.split(", Time:")[1].split(", Remark:")[0];

        // Parse the date and time parts
//        LocalDateTime dateTime = LocalDateTime.parse(datePart + " " + timePart, DateTimeFormatter.ofPattern("dd-MM-yyyy h:mm a"));
//
//        // Format the date and time into the desired format
//        return dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy, h:mm a"));
        return datePart + " " + timePart;

    }

    private void setFacilityToVisitSpinner() {
        String facility = visitAttributeListDAO.getVisitAttributesList_specificVisit(visitUuid, FACILITY);
        if (!facility.isEmpty()) {
            try {
                ReferralFacilityData referralFacilityData = new Gson().fromJson(facility, ReferralFacilityData.class);
                if (referralFacilityData != null) {
                    facility = LanguageUtils.getLocalValueFromArray(this, referralFacilityData.getCategory(), R.array.visit_facilities);
                }
            } catch (Exception e) {
            }
        }
        Timber.tag(TAG).d("facility=>%s", facility);
        if (!TextUtils.isEmpty(facility)) {
            ((TextView) findViewById(R.id.tvFacilityToVisitValue)).setText(" " + Node.bullet + "  " + facility);
            visitSummaryPdfData.setFacility(" " + Node.bullet + "  " + facility);
        } else {
            String noInfo = getString(R.string.no_information);
            ((TextView) findViewById(R.id.tvFacilityToVisitValue)).setText(noInfo);
            visitSummaryPdfData.setFacility(noInfo);
        }
    }

    private void setReferralFacilitySpinner() {
        String facility = visitAttributeListDAO.getVisitAttributesList_specificVisit(visitUuid, FACILITY);
        try {
            ReferralFacilityData referralFacilityData = new Gson().fromJson(facility, ReferralFacilityData.class);
            if (referralFacilityData != null) {
                facility = LanguageUtils.getReferralFacilityDataByLanguage(referralFacilityData, ReferralFacilityDataFormatType.VIEW);
            } else {
                facility = "";
            }
        } catch (Exception e) {
            facility = "";
        }

        Timber.tag(TAG).d("facility=>%s", facility);
        if (!TextUtils.isEmpty(facility)) {
            ((FrameLayout) findViewById(R.id.flReferralFacility)).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.tvReferralFacilityValue)).setText(" " + Node.bullet + "  " + facility);
            visitSummaryPdfData.setFacility(" " + Node.bullet + "  " + facility);
        } else {
            ((FrameLayout) findViewById(R.id.flReferralFacility)).setVisibility(View.GONE);
        }
    }

    private void setSeveritySpinner() {
        String severity = visitAttributeListDAO.getVisitAttributesList_specificVisit(visitUuid, SEVERITY);
        severity = LanguageUtils.getLocalValueFromArray(this, severity, R.array.visit_severity);
        Timber.tag(TAG).d("severity=>%s", severity);
        if (!TextUtils.isEmpty(severity)) {
            ((TextView) findViewById(R.id.tvSavertyValue)).setText(" " + Node.bullet + "  " + severity);
            visitSummaryPdfData.setSeverity(" " + Node.bullet + "  " + severity);
        } else {
            String noInfo = getString(R.string.no_information);
            ((TextView) findViewById(R.id.tvSavertyValue)).setText(noInfo);
            visitSummaryPdfData.setSeverity(noInfo);
        }

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
            RequestBuilder<Drawable> requestBuilder = Glide.with(context)
                    .asDrawable()
                    .sizeMultiplier(0.3f);

            Glide.with(context).load(patient.getPatient_photo())
                    .thumbnail(requestBuilder)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(profile_image);

            profileImageCard.setRadius(R.dimen.cardcornerradius_imagev);
            visitSummaryPdfData.setPatientImage(patient.getPatient_photo());
        } else {
            profile_image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.avatar1));
        }


        nameView.setText(patientName);
        visitSummaryPdfData.setPatientName(patientName);

        gender_tv = patientGender;
        setGenderAgeLocal(context, genderView, patient.getDate_of_birth(), patient.getGender(), sessionManager);
        visitSummaryPdfData.setGenderAge(genderView.getText().toString());

        var patientId = getString(R.string.patient_not_registered);
        if (patient.getOpenmrs_id() != null && !patient.getOpenmrs_id().isEmpty()) {
            patientId = patient.getOpenmrs_id();
        }
        idView.setText(patientId);
        visitSummaryPdfData.setPatientId(patientId);

        mCHWname = findViewById(R.id.chw_details);
        mCHWname.setText(sessionManager.getChwname()); //session manager provider
        // header title set - end
        visitSummaryPdfData.setChwName(sessionManager.getChwname());

        // vitals values set.
        String heightStr = getResources().getString(R.string.no_information);
        if (height.getValue() != null) {
            if (height.getValue().trim().isEmpty() || height.getValue().trim().equals("0")) {
                heightStr = getResources().getString(R.string.no_information);
            } else {
                heightStr = height.getValue();
            }
        }
        heightView.setText(heightStr);
        visitSummaryPdfData.setHeight(heightStr);

        String weightStr = getResources().getString(R.string.no_information);
        if (weight.getValue() != null) {
            if (weight.getValue().trim().isEmpty() || weight.getValue().trim().equals("0"))
                weightStr = getResources().getString(R.string.no_information);
            else {
                weightStr = weight.getValue();
            }
        }
        weightView.setText(weightStr);
        visitSummaryPdfData.setWeight(weightStr);

        Log.d(TAG, "onCreate: " + weight.getValue());
        String bmiStr = getResources().getString(R.string.no_information);
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
            if (mBMI.trim().isEmpty() || mBMI.equalsIgnoreCase("")) {
                bmiStr = getResources().getString(R.string.no_information);
            } else {
                bmiStr = mBMI;
            }
            bmiView.setText(bmiStr);
        }
        visitSummaryPdfData.setBmi(bmiStr);

        String bpStr = getResources().getString(R.string.no_information);
        String bpText = bpSys.getValue() + "/" + bpDias.getValue();
        if (bpText.equals("/")) {  //when new patient is being registered we get / for BP
            bpView.setText(getResources().getString(R.string.no_information));
        } else if (bpText.equalsIgnoreCase("null/null")) {
            //when we setup app and get data from other users, we get null/null from server...
            bpStr = getResources().getString(R.string.no_information);
        } else {
            bpStr = bpText;
        }
        bpView.setText(bpStr);
        visitSummaryPdfData.setBp(bpStr);

        String pulseStr = getResources().getString(R.string.no_information);
        if (pulse.getValue() != null) {
            if (pulse.getValue().trim().isEmpty() || pulse.getValue().trim().equals("0"))
                pulseStr = getResources().getString(R.string.no_information);
            else {
                pulseStr = pulse.getValue();
            }
        }

        pulseView.setText(pulseStr);
        visitSummaryPdfData.setPulse(pulseStr);

        String sp02Str = getResources().getString(R.string.no_information);
        if (spO2.getValue() != null) {
            if (spO2.getValue().trim().isEmpty() || spO2.getValue().trim().equals("0"))
                sp02Str = getResources().getString(R.string.no_information);
            else {
                sp02Str = spO2.getValue();
            }
        }

        spO2View.setText(sp02Str);
        visitSummaryPdfData.setSpoTwo(sp02Str);

        String blGroupStr = getResources().getString(R.string.no_information);
        if (mBloodGroupObsDTO.getValue() != null) {
            if (mBloodGroupObsDTO.getValue().trim().isEmpty() || mBloodGroupObsDTO.getValue().trim().equals("null"))
                blGroupStr = getResources().getString(R.string.no_information);
            else {
                blGroupStr = VisitUtils.getBloodPressureEnStringFromCode(mBloodGroupObsDTO.getValue());
            }
        }
        mBloodGroupTextView.setText(blGroupStr);
        visitSummaryPdfData.setBlGroup(blGroupStr);


        // temperature - start
        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(Objects.requireNonNullElse(FileUtils.readFileRoot(CONFIG_FILE_NAME, this), String.valueOf(FileUtils.encodeJSON(this, CONFIG_FILE_NAME)))); //Load the config file
            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(VisitSummaryActivityPreview.this, mFileName)));
            }
            String tempStr = getResources().getString(R.string.no_information);
            String tempHeader = "";
            if (obj.getBoolean("mCelsius")) {
                tempcel.setVisibility(View.VISIBLE);
                tempfaren.setVisibility(View.GONE);
                tempHeader = ContextCompat.getString(this, R.string.visit_summary_temperature);
                tempStr = temperature.getValue();
                Log.d("temp", "temp_C: " + temperature.getValue());
            } else if (obj.getBoolean("mFahrenheit")) {
                tempfaren.setVisibility(View.VISIBLE);
                tempcel.setVisibility(View.GONE);
                tempHeader = ContextCompat.getString(this, R.string.temperature_f);
                if (temperature.getValue() != null && !temperature.getValue().isEmpty()) {
                    tempStr = convertCtoF(TAG, temperature.getValue());
                    Log.d("temp", "temp_F: " + tempView.getText().toString());
                }
            }

            tempView.setText(tempStr);
            visitSummaryPdfData.setTempHeader(tempHeader);
            visitSummaryPdfData.setTemp(tempStr);


        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
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

        String respStr = getResources().getString(R.string.no_information);
        if (resp.getValue() != null) {
            if (resp.getValue().trim().isEmpty() || resp.getValue().trim().equals("0"))
                respStr = getResources().getString(R.string.no_information);
            else {
                respStr = resp.getValue();
            }
        }
        respiratory.setText(respStr);
        visitSummaryPdfData.setRespiratory(respStr);
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
            }
            rowListItem = new ArrayList<>();

            for (File file : fileList)
                rowListItem.add(new DocumentObject(file.getName(), file.getAbsolutePath()));

            if (rowListItem.size() == 0) {
                add_doc_relative.setVisibility(View.GONE);
            } else {
                add_doc_relative.setVisibility(View.VISIBLE);
            }

            RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(this);
            mAdditionalDocsRecyclerView.setHasFixedSize(true);
            mAdditionalDocsRecyclerView.setLayoutManager(linearLayoutManager);

            recyclerViewAdapter = new AdditionalDocumentAdapter(this,
                    encounterUuidAdultIntial,
                    rowListItem, AppConstants.IMAGE_PATH,
                    this,
                    true
            );

            mAdditionalDocsRecyclerView.setAdapter(recyclerViewAdapter);
            add_docs_title.setText(getResources().getString(R.string.add_additional_documents) + " (" + recyclerViewAdapter.getItemCount() + ")");
            visitSummaryPdfData.setAdditionalDocList(rowListItem);
        }


        // speciality data
        //if row is present i.e. if true is returned by the function then the spinner will be disabled.
        Log.d("visitUUID", "onCreate_uuid: " + visitUuid);
        isVisitSpecialityExists = speciality_row_exist_check(visitUuid);
        flag.setClickable(false);


        // todo: speciality code comes in upload btn as well so add that too....later...
        // speciality data - end

        jsonBasedPrescTitle();

    }

    private void setupSpecializationDataSpinner(List<Specialization> specializations) {
        Log.d("specc", "spec: " + visitUuid);
        String special_value = visitAttributeListDAO.getVisitAttributesList_specificVisit(visitUuid, SPECIALITY);
        //Hashmap to List<String> add all value
        SpecializationArrayAdapter stringArrayAdapter = new SpecializationArrayAdapter(this, specializations);
        speciality_spinner.setAdapter(stringArrayAdapter);

        specializations.add(0, new Specialization("select_specialization_text",
                getString(R.string.select_specialization_text)));

        if (special_value != null) {
            int spinner_position = stringArrayAdapter.getPosition(special_value);

            speciality_spinner.setSelection(spinner_position);
            Specialization sp = stringArrayAdapter.getItem(spinner_position);
            String displayValue = ResUtils.getStringResourceByName(this, sp.getSKey());
            vd_special_value.setText(" " + Node.bullet + "  " + displayValue);
            speciality_selected = special_value;

            doc_speciality_card.setVisibility(View.GONE);
            speciality_spinner.setEnabled(false);
            if (spinner_position > 0) {
                special_vd_card.setVisibility(View.VISIBLE);
                visitSummaryPdfData.setDoctorSpeciality(" " + Node.bullet + "  " + displayValue);
            }
        }

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
                    dialogUtils.showCommonDialog(VisitSummaryActivityPreview.this, R.drawable.ui2_ic_warning_internet, getResources().getString(R.string.no_prescription_available), getResources().getString(R.string.no_prescription_title), true, getResources().getString(R.string.okay), null, new DialogUtils.CustomDialogListener() {
                        @Override
                        public void onDialogActionDone(int action) {

                        }
                    });
                }
            } catch (ParseException e) {
                e.printStackTrace();
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
        }
    }

    private String showVisitID() {
        if (visitUUID != null && !visitUUID.isEmpty()) {
            String hideVisitUUID = visitUUID;
            hideVisitUUID = hideVisitUUID.substring(hideVisitUUID.length() - 4, hideVisitUUID.length());
            visitView.setText("XXXX" + hideVisitUUID);
            visitSummaryPdfData.setVisitId("XXXX" + hideVisitUUID);
        }
        return visitView.getText().toString();
    }

    private void showSpecialisationDialog() {
        DialogUtils dialogUtils = new DialogUtils();
        dialogUtils.showCommonDialog(VisitSummaryActivityPreview.this, R.drawable.ui2_ic_warning_internet, getResources().getString(R.string.please_select_specialization_msg), "", true, getResources().getString(R.string.okay), getResources().getString(R.string.cancel), new DialogUtils.CustomDialogListener() {
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
                Log.v("path", picturePath + "");
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
                Intent cameraIntent = new Intent(VisitSummaryActivityPreview.this, CameraActivity.class);
                String imageName = UUID.randomUUID().toString();
                cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
                cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, AppConstants.IMAGE_PATH);
                cameraActivityResult.launch(cameraIntent);
            }
        } else if (item == 1) {
            if (checkAndRequestPermissions(item)) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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
    private void uiUpdateForFollowUpVisit(){
        findViewById(R.id.flFacilityToVisit).setVisibility(!mIsFollowUpTypeVisit ? View.VISIBLE : View.GONE);
        findViewById(R.id.flReferralFacility).setVisibility(!mIsFollowUpTypeVisit ? View.VISIBLE : View.GONE);
        findViewById(R.id.flSeverity).setVisibility(!mIsFollowUpTypeVisit ? View.VISIBLE : View.GONE);
        //findViewById(R.id.flCloseCaseToVisit).setVisibility(!mIsFollowUpTypeVisit ? View.VISIBLE : View.GONE);
    }
    private void initUI() {
        sharePatientBt = findViewById(R.id.share_patient_bt);
        shareFacilityBt = findViewById(R.id.share_referral_facility_bt);
        // textview - start
        filter_framelayout = findViewById(R.id.filter_framelayout);

        reminder = findViewById(R.id.reminder);
        reminder.setText(getResources().getString(R.string.action_home));

        incomplete_act = findViewById(R.id.incomplete_act);
        incomplete_act.setText(getResources().getString(R.string.action_end_visit));

        archieved_notifi = findViewById(R.id.archieved_notifi);
        archieved_notifi.setVisibility(View.GONE);
        hl_2 = findViewById(R.id.hl_2);
        hl_2.setVisibility(View.GONE);

        backArrow = findViewById(R.id.backArrow);
        profile_image = findViewById(R.id.profile_image);
        nameView = findViewById(R.id.textView_name_value);
        genderView = findViewById(R.id.textView_gender_value);
        //OpenMRS Id
        idView = findViewById(R.id.textView_id_value);
        visitView = findViewById(R.id.textView_visit_value);

        doc_speciality_card = findViewById(R.id.doc_speciality_card);

        // up-down btn - start
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
        btn_up_addnotes_vd_header = findViewById(R.id.btn_up_addnotes_vd_header);
        addnotes_vd_header_relative = findViewById(R.id.addnotes_vd_header_relative);
        add_doc_relative = findViewById(R.id.add_doc_relative);

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
        addnotes_vd_card = findViewById(R.id.addnotes_vd_card);
        special_vd_card = findViewById(R.id.special_vd_card);
        priority_hint = findViewById(R.id.priority_hint);
        profileImageCard = findViewById(R.id.profile);

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

        btn_bottom_vs = findViewById(R.id.btn_bottom_vs);   // appointment - upload

        // file set
        baseDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        obsImgdir = new File(AppConstants.IMAGE_PATH);

        add_additional_doc = findViewById(R.id.add_additional_doc);

        sharePatientBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareVisitToWhatsApp(patient.getPhone_number(), "");
            }
        });

        shareFacilityBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ReferralFacilityData referralFacilityData = getReferralFacilityData();
                    if(referralFacilityData.getContactNumber() == 0){
                        Toast.makeText(VisitSummaryActivityPreview.this, getString(R.string.facility_not_found), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    shareVisitToWhatsApp(String.valueOf(referralFacilityData.getContactNumber()), String.valueOf(referralFacilityData.getId()));
                }catch (Exception e){
                    Toast.makeText(VisitSummaryActivityPreview.this, getString(R.string.facility_not_found), Toast.LENGTH_SHORT).show();
                }
            }
        });

        uiUpdateForFollowUpVisit();
    }

    private void shareVisitToWhatsApp(String phoneNumber, String facilityId) {
        String visitSummary_link = new VisitAttributeListDAO().getVisitAttributesList_specificVisit(visitUUID, VISIT_SUMMARY_LINK);
        if (visitSummary_link.isEmpty()) {
            Toast.makeText(VisitSummaryActivityPreview.this, getString(R.string.visit_summary_link_not_found), Toast.LENGTH_SHORT).show();
            return;
        }
        String partial_whatsapp_url = new UrlModifiers().getWhatsappUrl();
        if (phoneNumber.length() <= 10) {
            phoneNumber = "+91" + phoneNumber;
        }

        String whatsappMessage = String.format("https://api.whatsapp.com/send?phone=%s&text=%s",
                phoneNumber,
                getString(R.string.hello_thank_you_for_using_intelehealth_to_download_your_visit_summary_click_here)
                        + partial_whatsapp_url
                        + Uri.encode("#")
                        + visitSummary_link
                        + "/" +facilityId+"\n"
                        + getString(R.string.to_view_the_visit_summary_click_on_the_link_and_enter_otp_which_will_be_sent_to_your_registered_mobile_number));
        Log.v("whatsappMessage", whatsappMessage);
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(whatsappMessage)));
    }

    private ReferralFacilityData getReferralFacilityData() {
        String facility = visitAttributeListDAO.getVisitAttributesList_specificVisit(visitUuid, FACILITY);
        return  new Gson().fromJson(facility, ReferralFacilityData.class);
    }

    private void showShareDialog() {
        try {
            String hwName = new ProviderDAO().getProviderName(sessionManager.getCreatorID(), ProviderDTO.Columns.USER_UUID.value);
            if (hwName != null && !hwName.isEmpty()) {
                hwName = hwName.substring(0, 1).toUpperCase(Locale.getDefault()) + hwName.substring(1);
                String msg = String.format(getString(R.string.hw_message_sent_text), hwName);
                DialogUtils dialogUtils = new DialogUtils();
                dialogUtils.showCommonDialog(VisitSummaryActivityPreview.this, R.drawable.info_blue_svg, getResources().getString(R.string.alert_txt),
                        msg, true, getResources().getString(R.string.action_btn_send),
                        getResources().getString(R.string.cancel), action -> shareOperation(msg));
            }
        } catch (DAOException e) {
            throw new RuntimeException(e);
        }

    }

    private void sentMsgToWhatsApp(String msg) {
        String phoneNumber = patient.getPhone_number();
        String whatsappMessage = String.format("https://api.whatsapp.com/send?phone=%s&text=%s", phoneNumber, msg);
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(whatsappMessage)));

    }

    private void shareOperation(String msg) {
        htmlContent = VisitSummaryPdfGenerator.generateHtmlContent(context, visitSummaryPdfData, mIsFollowUpTypeVisit);
        WebView webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {

                PrintAttributes.Builder pBuilder = new PrintAttributes.Builder();
                pBuilder.setMediaSize(PrintAttributes.MediaSize.ISO_B4);
                pBuilder.setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600));
                pBuilder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);
                // Create a print job with name and adapter instance


                String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/" + getString(R.string.app_name);
                String fileName = patientName.replace(" ", "_") + "_" + System.currentTimeMillis() + ".pdf";
                File dir = new File(path);
                if (!dir.exists()) dir.mkdirs();
                String jobName = getString(R.string.app_name) + " " + getResources().getString(R.string._visit_summary);
                PdfPrint pdfPrint = new PdfPrint(pBuilder.build());
                //to write to a pdf file...
                pdfPrint.print(webView.createPrintDocumentAdapter(jobName), dir, fileName, new PdfPrint.CallbackPrint() {
                    @Override
                    public void success(String path) {
                        Timber.tag(TAG).d("Pdf Saved path => %s", path);
                        sentMsgToWhatsApp(msg);
                        Toast.makeText(VisitSummaryActivityPreview.this, R.string.please_attach_the_patient_visit_details_pdf, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure() {
                        Toast.makeText(VisitSummaryActivityPreview.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }

                });
            }
        });

        webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null);

    }

    private boolean isWhatsAppInstalled() {
        PackageManager packageManager = getPackageManager();
        try {
            packageManager.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void share(String path) {
        if (!isWhatsAppInstalled()) {
            Toast.makeText(this, R.string.whatsapp_not_installed_on_your_device, Toast.LENGTH_SHORT).show();
            return;
        }
        File pdfFile = new File(path);
        Uri pdfUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", pdfFile);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, pdfUri);
        intent.setType("application/pdf"); // Change the MIME type according to your file type
        intent.setPackage("com.whatsapp");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(intent);


    }

    private void printOperation() {
        ///printImage(bitmap);
       /* filePath = PdfGenerationUtils.generateSinglePagePdf(context,bitmap, "/visit_summary_" + System.currentTimeMillis() + ".pdf");
        printPDF(filePath);*/
        WebView webView = new WebView(this);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setJavaScriptEnabled(true);

        String htmlContent = VisitSummaryPdfGenerator.generateHtmlContent(context, visitSummaryPdfData, mIsFollowUpTypeVisit);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });

        String headerColor = "#4CAF50";
        String iconColor = "#03fc1c";

        webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null);
    }


    /**
     * function to set appointment button status
     */
    private void setAppointmentButtonStatus() {
        isVisitSpecialityExists = speciality_row_exist_check(visitUUID);

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
        fileList = new ArrayList<File>();
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
            mPhysicalExamsLayoutManager = new GridLayoutManager(VisitSummaryActivityPreview.this, 2, LinearLayoutManager.VERTICAL, false);
            mPhysicalExamsRecyclerView.setLayoutManager(mPhysicalExamsLayoutManager);
            mPhysicalExamsRecyclerView.setAdapter(horizontalAdapter);
            visitSummaryPdfData.setPhysicalExamImageList(fileList);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void printImage(Bitmap bitmap) {
        // Initialize the PrintHelper
        PrintHelper printHelper = new PrintHelper(this);
        printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);

        // Print the bitmap
        printHelper.printBitmap("Print Image", bitmap);
    }

    private void printPDF(String filePath) {
        PrintAttributes printAttributes = new PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setResolution(new PrintAttributes.Resolution("res1", "Resolution", 300, 300))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build();

        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        try {
            PrintDocumentAdapter printAdapter = new PdfPrintDocumentAdapter(this, filePath);
            printManager.print("PDF Print", printAdapter, printAttributes);
        } catch (Exception e) {
            Toast.makeText(this, "Error printing PDF", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onWriteComplete() {
        share(this.getFilesDir() + "/visit_summary.pdf");
    }

    @Override
    public void onWriteFailed() {

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
                parseDoctorDetails(dbValue);
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
                Log.i(TAG, "parseData: val:" + value);
                Log.i(TAG, "parseData: rx" + rxReturned);
                if (!rxReturned.trim().isEmpty()) {
                    rxReturned = rxReturned + "\n" + value;
                } else {
                    rxReturned = value;
                }
                Log.i(TAG, "parseData: rxfin" + rxReturned);
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
                    Log.d("GAME", "GAME: " + adviceReturned);
                } else {
                    adviceReturned = value;
                    Log.d("GAME", "GAME_2: " + adviceReturned);
                }
              /*  if (medicalAdviceCard.getVisibility() != View.VISIBLE) {
                    medicalAdviceCard.setVisibility(View.VISIBLE);
                }*/
                //medicalAdviceTextView.setText(adviceReturned);
                Log.d("Hyperlink", "hyper_global: " + medicalAdvice_string);

                int j = adviceReturned.indexOf('<');
                int i = adviceReturned.lastIndexOf('>');
                if (i >= 0 && j >= 0) {
                    medicalAdvice_HyperLink = adviceReturned.substring(j, i + 1);
                } else {
                    medicalAdvice_HyperLink = "";
                }

                Log.d("Hyperlink", "Hyperlink: " + medicalAdvice_HyperLink);

                medicalAdvice_string = adviceReturned.replaceAll(medicalAdvice_HyperLink, "");
                Log.d("Hyperlink", "hyper_string: " + medicalAdvice_string);

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
                Log.d("hyper_textview", "hyper_textview: " + medicalAdviceTextView.getText().toString());*/
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
                Log.i(TAG, "parseData: " + value);
                break;
        }
    }

    // parse doctor details
    ClsDoctorDetails objClsDoctorDetails;

    private void parseDoctorDetails(String dbValue) {
        Gson gson = new Gson();
        objClsDoctorDetails = gson.fromJson(dbValue, ClsDoctorDetails.class);
        Log.e(TAG, "TEST VISIT: " + objClsDoctorDetails);

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


            RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(this);
            mAdditionalDocsRecyclerView.setHasFixedSize(true);
            mAdditionalDocsRecyclerView.setLayoutManager(linearLayoutManager);

            recyclerViewAdapter = new AdditionalDocumentAdapter(this, encounterUuidAdultIntial, rowListItem, AppConstants.IMAGE_PATH, this, true);


            mAdditionalDocsRecyclerView.setAdapter(recyclerViewAdapter);
            add_docs_title.setText(getResources().getString(R.string.add_additional_documents) + " (" + recyclerViewAdapter.getItemCount() + ")");


        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        } catch (Exception file) {
            Logger.logD(TAG, file.getMessage());
        }

        setAppointmentButtonStatus();

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
        Log.i(TAG, "handleMessage: Entered");
        Bundle data = msg.getExtras();
        int check = 0;
        if (data != null) {
            check = data.getInt("Restart");
        }
        if (check == 100) {
            Log.i(TAG, "handleMessage: 100");
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
            Log.i(TAG, "handleMessage: 200");
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
                        Log.i(TAG, "found diagnosis");
                        break;
                    case UuidDictionary.JSV_MEDICATIONS:
                        Log.i(TAG, "found medications");
                        break;
                    case UuidDictionary.MEDICAL_ADVICE:
                        Log.i(TAG, "found medical advice");
                        break;
                    case UuidDictionary.ADDITIONAL_COMMENTS:
                        Log.i(TAG, "found additional comments");
                        break;
                    case UuidDictionary.REQUESTED_TESTS:
                        Log.i(TAG, "found tests");
                        break;
                    default:
                }
                obsCursor.close();
                //   addDownloadButton();
                //if any obs  found then end the visit
                //endVisit();
            } else {
                Log.i(TAG, "found sothing for test");
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
                Log.v("path", picturePath + "");
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
        Log.v("AdditionalDocuments", "picturePath = " + picturePath);
        File photo = new File(picturePath);
        BitmapUtils.fileCompressed(picturePath);
        if (photo.exists()) {
            try {
                long length = photo.length();
                length = length / 1024;
                Log.e("------->>>>", length + "");
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
                            Toast.makeText(VisitSummaryActivityPreview.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
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
                    RequestBuilder<Drawable> requestBuilder = Glide.with(context)
                            .asDrawable()
                            .sizeMultiplier(0.3f);
                    Glide.with(context).
                            load(AppConstants.IMAGE_PATH + patientModel.getUuid() + ".jpg")
                            .thumbnail(requestBuilder)
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(profile_image);
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
                Log.i("Patient WebView", "page finished loading " + url);
                int webview_heightContent = view.getContentHeight();
                Log.d("variable i", "variable i: " + webview_heightContent);
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
            Log.v("mChiefComplainList", "CC - " + val);
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
        String prescriptionString = prescriptionBuilder.builder(patient, vitalsData, diagnosisReturned, rxReturned, adviceReturned, testsReturned, referredSpeciality, followUpDate, objClsDoctorDetails);


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
        Log.d("webview content height", "webview content height: " + contentHeight);

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

            Log.d("PrintPDF", "PrintPDF");
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
                Log.i("Patient WebView", "page finished loading " + url);
                int webview_heightContent = view.getContentHeight();
                Log.d("variable i", "variable i: " + webview_heightContent);
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
//        Log.d("Hyperlink", "hyper_print: " + advice_web);
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
//        Log.d("Hyperlink", "hyper_print: " + advice_web); //gets called when clicked on button of print button
            advice_web = stringToWeb(advice_split.replace("\n\n", "\n")); //showing advice here...
            Log.d("Hyperlink", "hyper_print: " + advice_web); //gets called when clicked on button of print button
        } else {
            advice_web = stringToWeb(advice_doctor__.replace("\n\n", "\n")); //showing advice here...
            Log.d("Hyperlink", "hyper_print: " + advice_web); //gets called when clicked on button of print button
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
        Log.d("webview content height", "webview content height: " + contentHeight);

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
            String path = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/Intelehealth_PDF";
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
                        Toast.makeText(VisitSummaryActivityPreview.this, getResources().getString(R.string.downloaded_to) + " " + path, Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(VisitSummaryActivityPreview.this, getResources().getString(R.string.downloaded_to) + " " + path, Toast.LENGTH_SHORT).show();
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

            String path = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/Intelehealth_PDF";
            String fileName = patientName.replace(" ", "_") + "_" + showVisitID() + ".pdf";

            File dir = new File(path);
            Log.v(TAG, "dir.exists() : " + dir.exists());
            if (!dir.exists()) dir.mkdirs();


            //To display the preview window to user...
//            PrintJob printJob = printManager.print(jobName, printAdapter,
//                    pBuilder.build());

            //TODO: write different functions for <= Lollipop versions..
            //to write to a pdf file...
            pdfPrint.print(webView.createPrintDocumentAdapter(jobName), dir, fileName, new PdfPrint.CallbackPrint() {
                @Override
                public void success(String path) {
                    Toast.makeText(VisitSummaryActivityPreview.this, getResources().getString(R.string.downloaded_to) + " " + path, Toast.LENGTH_SHORT).show();
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
            String path = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/Intelehealth_PDF";
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
                    Toast.makeText(VisitSummaryActivityPreview.this, getResources().getString(R.string.downloaded_to) + ": " + path, Toast.LENGTH_SHORT).show();
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

            Log.d("PrintPDF", "PrintPDF");
            PrintAttributes.Builder pBuilder = new PrintAttributes.Builder();
            pBuilder.setMediaSize(PrintAttributes.MediaSize.NA_LETTER);
            pBuilder.setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600));
            pBuilder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);
            PdfPrint pdfPrint = new PdfPrint(pBuilder.build());

            /*String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Intelehealth_PDF/";
            String fileName = patientName + "_" + showVisitID() + ".pdf";*/
            String path = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/Intelehealth_PDF";
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
                    Toast.makeText(VisitSummaryActivityPreview.this, getResources().getString(R.string.downloaded_to) + ": " + path, Toast.LENGTH_SHORT).show();
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

        Intent intent2 = new Intent(this, IdentificationActivity_New.class);
        intent2.putExtra("patientUuid", patientDTO.getUuid());
        intent2.putExtra("ScreenEdit", "personal_edit");
        intent2.putExtra("patient_detail", true);

        Bundle args = new Bundle();
        args.putSerializable("patientDTO", (Serializable) patientDTO);
        intent2.putExtra("BUNDLE", args);
        startActivity(intent2);
    }

    ActivityResultLauncher<Intent> mStartForEditVisit = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
//                        recreate();
                fetchingIntent();
                setViewsData();
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
        chifComplainStringBuilder = new StringBuilder();
        physicalExamStringBuilder = new StringBuilder();
        medicalHistoryStringBuilder = new StringBuilder();

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
            Log.v(TAG, "isInOldFormat: " + mIsCCInOldFormat);
            Log.v(TAG, "complaint: " + value);
            String valueArray[] = null;
            boolean isAssociateSymptomFound = false;
            if (mIsCCInOldFormat) {
                complaintView.setVisibility(View.VISIBLE);
                findViewById(R.id.reports_relative).setVisibility(View.VISIBLE);
                findViewById(R.id.denies_relative).setVisibility(View.VISIBLE);

                valueArray = value.split("►<b> " + Node.ASSOCIATE_SYMPTOMS + "</b>:  <br/>");
                isAssociateSymptomFound = valueArray.length >= 2;
                Log.v(TAG, "complaint: " + valueArray[0]);
                Log.v(TAG, "complaint associated: " + (isAssociateSymptomFound ? valueArray[1] : "no Associated Symptom found in value"));
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
                visitSummaryPdfData.setChiefComplaintList(mChiefComplainList);

                String patientReports = getResources().getString(R.string.no_data_added);
                String patientDenies = getResources().getString(R.string.no_data_added);

                if (valueArray[0] != null) {
                    complaintView.setText(Html.fromHtml(valueArray[0]));
                    chifComplainStringBuilder
                            .append("<br />")
                            .append("<b>")
                            .append("<h3>")
                            .append(ContextCompat.getString(this, R.string.details))
                            .append("</h3>")
                            .append("</b>")
                            .append("")
                            .append(valueArray[0]);
                    visitSummaryPdfData.setChiefComplain(chifComplainStringBuilder.toString());
                }
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
                chifComplainStringBuilder
                        .append("<br />")
                        .append("<b>")
                        .append("<h3>")
                        .append(ContextCompat.getString(this, R.string.associated_symptoms))
                        .append("</h3>")
                        .append("</b>")
                        .append("<br />")
                        .append("<span style=\"color: grey;\">")
                        .append(ContextCompat.getString(this, R.string.patient_reports))
                        .append("</span>")
                        .append("<br />")
                        .append("<p class=\"text-with-margin\">")
                        .append(patientReports)
                        .append("</p>")
                        .append("<br />")
                        .append("<span style=\"color: grey;\">")
                        .append(ContextCompat.getString(this, R.string.patient_denies))
                        .append("</span>")
                        .append("<br />")
                        .append("<p class=\"text-with-margin\">")
                        .append(patientDenies)
                        .append("</p>");
                patientReports_txtview.setText(Html.fromHtml(patientReports));
                patientDenies_txtview.setText(Html.fromHtml(patientDenies));

                visitSummaryPdfData.setChiefComplain(chifComplainStringBuilder.toString());
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
            Log.v(TAG, "phyExam : " + value);
            if (isInOldFormat) {
                physFindingsView.setVisibility(View.VISIBLE);
                String valueArray[] = value.replace("General exams: <br>", "<b>General exams: </b><br/>").split("<b>General exams: </b><br/>");
                if (valueArray.length > 1) {
                    physFindingsView.setText(Html.fromHtml(valueArray[1]));
                    physicalExamStringBuilder
                            .append("</br><b>")
                            .append("<h3>")
                            .append(ContextCompat.getString(this, R.string.general_exams))
                            .append("</h3>")
                            .append("</b>")
                            .append("</b></br><br/>")
                            .append(valueArray[1]);
                    visitSummaryPdfData.setPhysicalExam(physicalExamStringBuilder.toString());
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
            Log.v(TAG, "patHistory : " + value);
            if (isInOldFormat) {
                patHistView.setVisibility(View.VISIBLE);
                patHistView.setText(Html.fromHtml(value));
                medicalHistoryStringBuilder
                        .append("<br />")
                        .append("<h3>")
                        .append(ContextCompat.getString(this, R.string.button_history))
                        .append("</h3>")
                        .append("</br>")
                        .append(value);
                visitSummaryPdfData.setMedicalHistory(medicalHistoryStringBuilder.toString());
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
            Log.v(TAG, "famHistory : " + value);
            if (isInOldFormat) {
                famHistView.setVisibility(View.VISIBLE);
                famHistView.setText(Html.fromHtml(value));
                medicalHistoryStringBuilder
                        .append("<br /></br><b>")
                        .append("<h3>")
                        .append(ContextCompat.getString(this, R.string.header_family_history))
                        .append("</h3>")
                        .append("</b></br>")
                        .append(value);
                visitSummaryPdfData.setMedicalHistory(medicalHistoryStringBuilder.toString());
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
        Log.v(TAG, answerInLocale);
        //►दस्त::● आपको ये लक्षण कब से है• 6 घंटे● दस्त शुरू कैसे हुए?•धीरे धीरे● २४ घंटे में कितनी बार दस्त हुए?•३ से कम बार● दस्त किस प्रकार के है?•पक्का● क्या आपको पिछले महीनो में दस्त शुरू होने से पहले किसी असामान्य भोजन/तरल पदार्थ से अपच महसूस हुआ है•नहीं● क्या आपने आज यहां आने से पहले इस समस्या के लिए कोई उपचार (स्व-दवा या घरेलू उपचार सहित) लिया है या किसी स्वास्थ्य प्रदाता को दिखाया है?•कोई नहीं● अतिरिक्त जानकारी•bsbdbd►क्या आपको निम्न लक्षण है::•उल्टीPatient denies -•दस्त के साथ पेट दर्द•सुजन•मल में खून•बुखार•अन्य [वर्णन करे]

        String[] spt = answerInLocale.split("►");
        List<String> list = new ArrayList<>();
        String associatedSymptomsString = "";
        for (String s : spt) {
            if (s.isEmpty()) continue;
            //String s1 =  new String(s.getBytes(), "UTF-8");
            Log.v(TAG, "Chunk - " + s);
            //if (s.trim().startsWith(getTranslatedAssociatedSymptomQString(lCode))) {
            //if (s.trim().contains("Patient denies -•")) {
            if (s.trim().contains(getTranslatedPatientDenies(lCode)) || s.trim().contains(getTranslatedAssociatedSymptomQString(lCode))) {
                associatedSymptomsString = s;
                Log.v(TAG, "associatedSymptomsString - " + associatedSymptomsString);
            } else {
                list.add(s);
            }

        }
        mComplainSummaryLinearLayout.removeAllViews();

        chifComplainStringBuilder
                .append("<b>")
                .append("<br />")
                .append("<b>")
                .append("<h3>")
                .append(ContextCompat.getString(this, R.string.details))
                .append("</h3>")
                .append("<b>")
                .append("<br />");

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

                if (visitSummaryDataList.size() > 0) {
                    chifComplainStringBuilder
                            .append("<br /><h4>")
                            .append(complainName)
                            .append("</h4><br /><br />");
                }

                for (VisitSummaryData data : visitSummaryDataList) {
                    if (data.getDisplayValue().isEmpty() || data.getDisplayValue() == null) {
                        chifComplainStringBuilder
                                .append("<span style=\"color: black;\">")
                                .append(data.getQuestion())
                                .append("</span>")
                                .append("<br />")
                                .append("<br />");
                    } else {
                        chifComplainStringBuilder
                                .append("&nbsp;")
                                .append("<span style=\"color: grey;\">")
                                .append("●")
                                .append("&nbsp;")
                                .append(data.getQuestion())
                                .append("<br />")
                                .append("</span>")
                                .append("&nbsp;")
                                .append("&nbsp;")
                                .append("&nbsp;")
                                .append("&nbsp;")
                                .append("<span style=\"color: black;\">")
                                .append(data.getDisplayValue())
                                .append("</span>")
                                .append("<br />")
                                .append("<br />");
                    }

                }
                visitSummaryPdfData.setChiefComplain(chifComplainStringBuilder.toString());
            }
        }

        // set all chief complain list
        cc_recyclerview_gridlayout = new GridLayoutManager(this, 2);
        cc_recyclerview.setLayoutManager(cc_recyclerview_gridlayout);
        cc_adapter = new ComplaintHeaderAdapter(this, mChiefComplainList);
        cc_recyclerview.setAdapter(cc_adapter);
        visitSummaryPdfData.setChiefComplaintList(mChiefComplainList);


        // ASSOCIATED SYMPTOMS
        String[] tempAS = associatedSymptomsString.split("::");
        if (tempAS.length >= 2) {
            String title = tempAS[0];
            //mAssociateSymptomsLabelTextView.setText(title);  // not required

            associatedSymptomsString = tempAS[1];
        }
        String[] sections = associatedSymptomsString.split(getTranslatedPatientDenies(lCode));


        Log.v(TAG, associatedSymptomsString);
        String[] spt1 = associatedSymptomsString.trim().split("•");
        Log.e(TAG, associatedSymptomsString);
        Log.e(TAG, String.valueOf(spt1.length));
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
                chifComplainStringBuilder
                        .append("<br />")
                        .append("<span style=\"color: grey;\">")
                        .append(i == 0 ? getString(R.string.patient_reports) : getString(R.string.patient_denies))
                        .append("</span>")
                        .append(patientReports);
           /* if (patientReportsDenies.isEmpty()) {
                view.findViewById(R.id.iv_blt).setVisibility(View.GONE);
            } else {
                view.findViewById(R.id.iv_blt).setVisibility(View.VISIBLE);
            }*/
                mAssociateSymptomsLinearLayout.addView(view);
                visitSummaryPdfData.setChiefComplain(chifComplainStringBuilder.toString());

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
        physicalExamStringBuilder
                .append("<br />")
                .append("<h3>")
                .append(ContextCompat.getString(this, R.string.general_exams))
                .append("</h3>");

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
                Log.v("PH0_complain", _complain);
                if (_complain.trim().equalsIgnoreCase(VisitUtils.getTranslatedGeneralExamString(sessionManager.getAppLanguage()))) {
                    complainLabelTextView.setVisibility(View.GONE);
                    _complain = "";
                }
                view.findViewById(R.id.height_adjust_view).setVisibility(View.GONE);
                view.findViewById(R.id.tv_change).setVisibility(View.GONE);
                RecyclerView recyclerView = view.findViewById(R.id.rcv_qa);
                recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
                List<VisitSummaryData> visitSummaryDataList = new ArrayList<>();
                String k1 = "";
                String lastString = "";

                for (int i = 0; i < _list.size(); i++) {
                    Log.v("PH0", _list.get(i));
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

                if (visitSummaryDataList.size() > 0 && !_complain.isEmpty()) {
                    physicalExamStringBuilder
                            .append("<br />")
                            .append(getFormattedComplain(_complain))
                            .append("<br />");
                }

                for (VisitSummaryData data : visitSummaryDataList) {
                    if (data.getDisplayValue().isEmpty() || data.getDisplayValue() == null) {
                        physicalExamStringBuilder
                                .append("<span style=\"color: black;\">")
                                .append(data.getQuestion())
                                .append("</span>")
                                .append("<br />")
                                .append("<br />");
                    } else {
                        physicalExamStringBuilder
                                .append("&nbsp;")
                                .append("<span style=\"color: grey;\">")
                                .append("●")
                                .append("&nbsp;")
                                .append(data.getQuestion())
                                .append("<br />")
                                .append("</span>")
                                .append("&nbsp;")
                                .append("&nbsp;")
                                .append("&nbsp;")
                                .append("&nbsp;")
                                .append("<span style=\"color: black;\">")
                                .append(data.getDisplayValue())
                                .append("</span>")
                                .append("<br />")
                                .append("<br />");
                    }

                }
                visitSummaryPdfData.setPhysicalExam(physicalExamStringBuilder.toString());
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

            String _complain = key.equalsIgnoreCase("Patient history") ? getString(R.string.button_history) : getString(R.string.title_activity_family_history);
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
                    Log.v("K", "_list.get(i) - " + _list.get(i));
                    String[] qa = _list.get(i).split("•");
                    if (qa.length == 2) {
                        String k = qa[0].trim();
                        String v = qa[1].trim();
                        Log.v("K", "k - " + k);
                        Log.v("V", "V - " + v);
                        if (v.contains(":") && v.split(":").length > 1) {
                            v = v.split(":")[1];
                        }
                        VisitSummaryData summaryData = new VisitSummaryData();
                        summaryData.setQuestion(k.isEmpty() ? v : k);
                        summaryData.setDisplayValue(k.isEmpty() ? "" : v);
                        visitSummaryDataList.add(summaryData);
                    } else {
                        boolean isOddSequence = qa.length % 2 != 0;
                        Log.v("isOddSequence", qa.length + " = " + isOddSequence);
                        //String k = value.split("•")[0].trim();
                        StringBuilder stringBuilder = new StringBuilder();
                        String k1 = "";
                        String lastString = "";
                        if (key.equalsIgnoreCase("Patient history")) {

                            for (int j = 0; j < qa.length; j++) {
                                boolean isLastItem = j == qa.length - 1;
                                String v1 = qa[j];
                                Log.v("V", v1);
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
                                Log.v("QA", "qa - " + qa[j]);
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


                if (visitSummaryDataList.size() > 0) {
                    medicalHistoryStringBuilder
                            .append("<br />")
                            .append("<h3>")
                            .append(getFormattedComplain(_complain))
                            .append("</h3>")
                            .append("<br />");
                }

                for (VisitSummaryData data : visitSummaryDataList) {
                    if (data.getDisplayValue().isEmpty() || data.getDisplayValue() == null) {
                        medicalHistoryStringBuilder
                                .append("<span style=\"color: black;\">")
                                .append(data.getQuestion())
                                .append("</span>")
                                .append("<br />")
                                .append("<br />");
                    } else {
                        medicalHistoryStringBuilder
                                .append("&nbsp;")
                                .append("<span style=\"color: grey;\">")
                                .append("●")
                                .append("&nbsp;")
                                .append(data.getQuestion())
                                .append("<br />")
                                .append("</span>")
                                .append("&nbsp;")
                                .append("&nbsp;")
                                .append("&nbsp;")
                                .append("&nbsp;")
                                .append("<span style=\"color: black;\">")
                                .append(data.getDisplayValue())
                                .append("</span>")
                                .append("<br />")
                                .append("<br />");
                    }

                }
                visitSummaryPdfData.setMedicalHistory(medicalHistoryStringBuilder.toString());
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
                    Log.v("K", "_list.get(i) - " + _list.get(i));
                    String[] qa = _list.get(i).split("•");
                    if (qa.length == 2) {
                        String k = qa[0].trim();
                        String v = qa[1].trim();
                        Log.v("K", "k - " + k);
                        Log.v("V", "V - " + v);
                        if (v.contains(":") && v.split(":").length > 1) {
                            v = v.split(":")[1];
                        }
                        VisitSummaryData summaryData = new VisitSummaryData();
                        summaryData.setQuestion(k.isEmpty() ? v : k);
                        summaryData.setDisplayValue(k.isEmpty() ? "" : v);
                        visitSummaryDataList.add(summaryData);
                    } else {
                        boolean isOddSequence = qa.length % 2 != 0;
                        Log.v("isOddSequence", qa.length + " = " + isOddSequence);
                        //String k = value.split("•")[0].trim();
                        StringBuilder stringBuilder = new StringBuilder();
                        String k1 = "";
                        String lastString = "";
                        if (key.equalsIgnoreCase("Patient history")) {

                            for (int j = 0; j < qa.length; j++) {
                                boolean isLastItem = j == qa.length - 1;
                                String v1 = qa[j];
                                Log.v("V", v1);
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
                                Log.v("QA", "qa - " + qa[j]);
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
                Log.v("visitSummaryDataList", visitSummaryDataList.size() + " visitSummaryDataList");
                SummaryViewAdapter summaryViewAdapter = new SummaryViewAdapter(recyclerView, this, visitSummaryDataList, new SummaryViewAdapter.OnItemSelection() {

                    @Override
                    public void onSelect(VisitSummaryData data) {

                    }
                });
                recyclerView.setAdapter(summaryViewAdapter);
                mFamilyHistorySummaryLinearLayout.addView(view);


                if (visitSummaryDataList.size() > 0) {
                    medicalHistoryStringBuilder
                            .append("<br />")
                            .append("<br />")
                            .append("<h3>")
                            .append(getFormattedComplain(_complain))
                            .append("</h3>")
                            .append("<br />");
                }

                for (VisitSummaryData data : visitSummaryDataList) {
                    if (data.getDisplayValue().isEmpty() || data.getDisplayValue() == null) {
                        medicalHistoryStringBuilder
                                .append("</h3>")
                                .append(data.getQuestion())
                                .append("</h3>")
                                .append("<br />")
                                .append("<br />");
                    } else {
                        medicalHistoryStringBuilder
                                .append("&nbsp;")
                                .append("<span style=\"color: grey;\">")
                                .append("●")
                                .append("&nbsp;")
                                .append(data.getQuestion())
                                .append("<br />")
                                .append("</span>")
                                .append("&nbsp;")
                                .append("&nbsp;")
                                .append("&nbsp;")
                                .append("&nbsp;")
                                .append("<span style=\"color: black;\">")
                                .append(data.getDisplayValue())
                                .append("</span>")
                                .append("<br />")
                                .append("<br />");
                    }

                }
                visitSummaryPdfData.setMedicalHistory(medicalHistoryStringBuilder.toString());


            }
        }

    }

    @Override
    protected void onFeatureActiveStatusLoaded(FeatureActiveStatus activeStatus) {
        super.onFeatureActiveStatusLoaded(activeStatus);
        if (activeStatus != null) {
            findViewById(R.id.flFacilityToVisit).setVisibility(activeStatus.getVisitSummeryFacilityToVisit() ? View.VISIBLE : View.GONE);
            findViewById(R.id.flSeverity).setVisibility(activeStatus.getVisitSummerySeverityOfCase() ? View.VISIBLE : View.GONE);
            findViewById(R.id.vitalsCard).setVisibility(activeStatus.getVitalSection() ? View.VISIBLE : View.GONE);
            findViewById(R.id.add_notes_relative).setVisibility(activeStatus.getVisitSummeryNote() ? View.VISIBLE : View.GONE);
            findViewById(R.id.add_doc_relative).setVisibility(activeStatus.getVisitSummeryAttachment() ? View.VISIBLE : View.GONE);
            findViewById(R.id.flVdCard).setVisibility(activeStatus.getVisitSummeryDoctorSpeciality() ? View.VISIBLE : View.GONE);
            findViewById(R.id.cardPriorityVisit).setVisibility(activeStatus.getVisitSummeryPriorityVisit() ? View.VISIBLE : View.GONE);
            //findViewById(R.id.btn_vs_appointment).setVisibility(activeStatus.getVisitSummeryAppointment() ? View.VISIBLE : View.GONE);
            visitSummaryPdfData.setActiveStatus(activeStatus);
            uiUpdateForFollowUpVisit();
        }
    }
}