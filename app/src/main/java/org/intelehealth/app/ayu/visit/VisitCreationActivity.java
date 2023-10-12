package org.intelehealth.app.ayu.visit;

import static org.intelehealth.app.knowledgeEngine.Node.bullet_arrow;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.ayu.visit.common.VisitUtils;
import org.intelehealth.app.ayu.visit.familyhist.FamilyHistoryFragment;
import org.intelehealth.app.ayu.visit.model.ReasonData;
import org.intelehealth.app.ayu.visit.pastmedicalhist.MedicalHistorySummaryFragment;
import org.intelehealth.app.ayu.visit.pastmedicalhist.PastMedicalHistoryFragment;
import org.intelehealth.app.ayu.visit.physicalexam.PhysicalExamSummaryFragment;
import org.intelehealth.app.ayu.visit.physicalexam.PhysicalExaminationFragment;
import org.intelehealth.app.ayu.visit.reason.VisitReasonCaptureFragment;
import org.intelehealth.app.ayu.visit.reason.VisitReasonQuestionsFragment;
import org.intelehealth.app.ayu.visit.reason.VisitReasonSummaryFragment;
import org.intelehealth.app.ayu.visit.vital.VitalCollectionFragment;
import org.intelehealth.app.ayu.visit.vital.VitalCollectionSummaryFragment;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.knowledgeEngine.PhysicalExam;
import org.intelehealth.app.models.AnswerResult;
import org.intelehealth.app.models.VitalsObject;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.BitmapUtils;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.ihutils.ui.CameraActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public class VisitCreationActivity extends AppCompatActivity implements VisitCreationActionListener {

    private static final String TAG = VisitCreationActivity.class.getSimpleName();
    private static final String VITAL_FRAGMENT = "VITAL";
    private static final String VITAL_SUMMARY_FRAGMENT = "VITAL_SUMMARY";
    private static final String VISIT_REASON_FRAGMENT = "VISIT_REASON";
    private static final String VISIT_REASON_QUESTION_FRAGMENT = "VISIT_REASON_QUESTION";
    private static final String VISIT_REASON_SUMMARY_FRAGMENT = "VISIT_REASON_SUMMARY";
    private static final String PHYSICAL_EXAM_FRAGMENT = "PHYSICAL_EXAM";
    private static final String PHYSICAL_EXAM_SUMMARY_FRAGMENT = "PHYSICAL_EXAM_SUMMARY";
    private static final String PAST_MEDICAL_HISTORY_FRAGMENT = "PAST_MEDICAL_HISTORY";
    private static final String PAST_MEDICAL_HISTORY_SUMMARY_FRAGMENT = "PAST_MEDICAL_HISTORY_SUMMARY";
    private static final String FAMILY_HISTORY_SUMMARY_FRAGMENT = "FAMILY_HISTORY_SUMMARY";
    public static final int STEP_1_VITAL = 1;
    public static final int STEP_1_VITAL_SUMMARY = 1001;
    public static final int STEP_2_VISIT_REASON = 2;
    public static final int STEP_2_VISIT_REASON_QUESTION = 3;
    public static final int STEP_2_VISIT_REASON_QUESTION_ASSOCIATE_SYMPTOMS = 4;
    public static final int STEP_2_VISIT_REASON_QUESTION_SUMMARY = 44;
    public static final int STEP_3_PHYSICAL_EXAMINATION = 5;
    public static final int STEP_3_PHYSICAL_SUMMARY_EXAMINATION = 55;
    public static final int STEP_4_PAST_MEDICAL_HISTORY = 6;
    public static final int STEP_5_FAMILY_HISTORY = 7;
    public static final int STEP_5_HISTORY_SUMMARY = 8;
    public static final int STEP_6_VISIT_SUMMARY = 9;
    public static final int FROM_SUMMARY_RESUME_BACK_FOR_EDIT = 33;


    private int mCurrentStep = STEP_1_VITAL;

    SessionManager sessionManager;
    private String patientName = "";
    private String patientGender = "";
    private String intentTag;
    private String state;
    private String patientUuid;
    private String visitUuid;
    private String encounterVitals;
    private float float_ageYear_Month;
    private int mAgeInMonth;
    private String mAgeAndMonth;
    private String encounterAdultIntials = "", EncounterAdultInitial_LatestVisit = "";

    private FrameLayout mSummaryFrameLayout;
    private ProgressBar mStep1ProgressBar, mStep2ProgressBar, mStep3ProgressBar, mStep4ProgressBar;

    // Chief complain
    //private List<Node> mAnsweredRootNodeList = new ArrayList<>();
    private List<Node> mChiefComplainRootNodeList = new ArrayList<>();
    private List<Node> mAssociateSymptomsNodeList = new ArrayList<>();
    private int mCurrentComplainNodeIndex = 0;
    private int mCurrentComplainNodeOptionsIndex = 0;
    private List<ReasonData> mSelectedComplainList = new ArrayList<ReasonData>();

    // Physical Examination

    // Past Medical History

    // Family History

    private boolean mIsEditMode = false;
    private boolean mIsEditTriggerFromVisitSummary = false;
    private int mEditFor = 0; // STEP_1_VITAL , STEP_2_VISIT_REASON, STEP_3_PHYSICAL_EXAMINATION, STEP_4_PAST_MEDICAL_HISTORY

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_creation);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.white));
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        }

        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        mSummaryFrameLayout = findViewById(R.id.fl_steps_summary);
        mStep1ProgressBar = findViewById(R.id.prog_bar_step1);
        mStep2ProgressBar = findViewById(R.id.prog_bar_step2);
        mStep3ProgressBar = findViewById(R.id.prog_bar_step3);
        mStep4ProgressBar = findViewById(R.id.prog_bar_step4);

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
            EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");
            state = intent.getStringExtra("state");
            patientName = intent.getStringExtra("name");
            patientGender = intent.getStringExtra("gender");
            intentTag = intent.getStringExtra("tag");
            mEditFor = intent.getIntExtra("edit_for", STEP_1_VITAL);
            float_ageYear_Month = intent.getFloatExtra("float_ageYear_Month", 0);
            String[] temp = String.valueOf(float_ageYear_Month).split("\\.");
            mAgeInMonth = Integer.parseInt(temp[0]) * 12 + Integer.parseInt(temp[1]);
            if (Integer.parseInt(temp[0]) == 0) {
                mAgeAndMonth = temp[1] + " Months";
            } else if (Integer.parseInt(temp[0]) == 0) {
                mAgeAndMonth = temp[0] + " Years";
            } else {
                mAgeAndMonth = temp[0] + " Years " + temp[1] + " Months";
            }

            if (intentTag.equalsIgnoreCase("edit")) {
                mIsEditMode = true;
                mIsEditTriggerFromVisitSummary = true;
            }
            Log.v(TAG, "Patient ID: " + patientUuid);
            Log.v(TAG, "Visit ID: " + visitUuid);
            Log.v(TAG, "Patient Name: " + patientName);
            Log.v(TAG, "Intent Tag: " + intentTag);
            Log.v(TAG, "Intent float_ageYear_Month: " + float_ageYear_Month);
            ((TextView) findViewById(R.id.tv_title)).setText(patientName);
            ((TextView) findViewById(R.id.tv_title_desc)).setText(String.format("%s/%s", patientGender, mAgeAndMonth));

        }

        if (encounterAdultIntials.equalsIgnoreCase("") || encounterAdultIntials == null) {
            encounterAdultIntials = UUID.randomUUID().toString();

        }

        EncounterDAO encounterDAO = new EncounterDAO();
        EncounterDTO encounterDTO = new EncounterDTO();
        encounterDTO.setUuid(encounterAdultIntials);
        encounterDTO.setEncounterTypeUuid(encounterDAO.getEncounterTypeUuid("ENCOUNTER_ADULTINITIAL"));
        encounterDTO.setEncounterTime(AppConstants.dateAndTimeUtils.currentDateTime());
        encounterDTO.setVisituuid(visitUuid);
        encounterDTO.setSyncd(false);
        encounterDTO.setProvideruuid(sessionManager.getProviderID());
        Log.d("DTO", "DTOcomp: " + encounterDTO.getProvideruuid());
        encounterDTO.setVoided(0);
        try {
            encounterDAO.createEncountersToDB(encounterDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }


        Bundle bundle = new Bundle();
        bundle.putString("patientUuid", patientUuid);
        bundle.putString("visitUuid", visitUuid);
        bundle.putString("encounterUuidVitals", encounterVitals);

        if (!mIsEditMode)
            getSupportFragmentManager().beginTransaction().
                    replace(R.id.fl_steps_body, VitalCollectionFragment.newInstance(getIntent(), mIsEditMode, null), VITAL_FRAGMENT).
                    commit();
        else makeReadyForEdit();
    }

    public boolean isEditTriggerFromVisitSummary() {
        return mIsEditTriggerFromVisitSummary;
    }

    private void makeReadyForEdit() {
        findViewById(R.id.ll_progress_steps).setVisibility(View.GONE);
        // init all resources
        mSelectedComplainList = new Gson().fromJson(sessionManager.getVisitEditCache(SessionManager.CHIEF_COMPLAIN_LIST + visitUuid), new TypeToken<List<ReasonData>>() {
        }.getType());

        mChiefComplainRootNodeList = new Gson().fromJson(sessionManager.getVisitEditCache(SessionManager.CHIEF_COMPLAIN_QUESTION_NODE + visitUuid), new TypeToken<List<Node>>() {
        }.getType());

        if (!sessionManager.getVisitEditCache(SessionManager.PHY_EXAM + visitUuid).isEmpty()) {
            physicalExamMap = new Gson().fromJson(sessionManager.getVisitEditCache(SessionManager.PHY_EXAM + visitUuid), PhysicalExam.class);
            physicalExamMap.refreshOnlyLocaleTitle();
        } else
            loadPhysicalExam();

        if (!sessionManager.getVisitEditCache(SessionManager.PATIENT_HISTORY + visitUuid).isEmpty())
            mPastMedicalHistoryNode = new Gson().fromJson(sessionManager.getVisitEditCache(SessionManager.PATIENT_HISTORY + visitUuid), Node.class);
        else
            mPastMedicalHistoryNode = loadPastMedicalHistory();

        if (!sessionManager.getVisitEditCache(SessionManager.FAMILY_HISTORY + visitUuid).isEmpty())
            mFamilyHistoryNode = new Gson().fromJson(sessionManager.getVisitEditCache(SessionManager.FAMILY_HISTORY + visitUuid), Node.class);
        else
            mFamilyHistoryNode = loadFamilyHistory();
        switch (mEditFor) {
            case STEP_1_VITAL:
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fl_steps_body, VitalCollectionFragment.newInstance(getIntent(), mIsEditMode, null), VITAL_FRAGMENT).
                        commit();
                break;
            case STEP_2_VISIT_REASON:


                //loadChiefComplainNodeForSelectedNames(mSelectedComplainList);
                //mStep2ProgressBar.setProgress(40);
                setTitle(getResources().getString(R.string.visit_reason) + " : " + mSelectedComplainList.get(0).getReasonNameLocalized());
                //Toast.makeText(this, "Show vital summary", Toast.LENGTH_SHORT).show();
                //mSummaryFrameLayout.setVisibility(View.GONE);
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fl_steps_body, VisitReasonQuestionsFragment.newInstance(getIntent(), mIsEditMode, mChiefComplainRootNodeList), VISIT_REASON_QUESTION_FRAGMENT).
                        commit();
                break;
            case STEP_3_PHYSICAL_EXAMINATION:
                mStep3ProgressBar.setProgress(10);
                setTitle(getResources().getString(R.string._phy_examination));
                mSummaryFrameLayout.setVisibility(View.GONE);
                //mPhysicalExamNode =
                //loadPhysicalExam();
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fl_steps_body, PhysicalExaminationFragment.newInstance(getIntent(), mIsEditMode, physicalExamMap), PHYSICAL_EXAM_FRAGMENT).
                        commit();
                break;
            case STEP_4_PAST_MEDICAL_HISTORY:
                showPastMedicalHistoryFragment(mIsEditMode);
                break;
            case STEP_5_FAMILY_HISTORY:
                showFamilyHistoryFragment(mIsEditMode);
                break;
        }
    }

    public void backPress(View view) {
        finish();
    }

    private VitalsObject mVitalsObject;

    @Override
    public void onFormSubmitted(int nextAction, boolean isEditMode, Object object) {
        mCurrentStep = nextAction;

        switch (nextAction) {
            case STEP_1_VITAL_SUMMARY:
                if (object != null)
                    mVitalsObject = (VitalsObject) object;
                if (mVitalsObject != null) {
                    //Toast.makeText(this, "Show vital summary", Toast.LENGTH_SHORT).show();
                    mSummaryFrameLayout.setVisibility(View.VISIBLE);
                    mStep1ProgressBar.setProgress(100);
                    getSupportFragmentManager().beginTransaction().
                            replace(R.id.fl_steps_summary, VitalCollectionSummaryFragment.newInstance(mVitalsObject, isEditMode), VITAL_SUMMARY_FRAGMENT).
                            commit();
                }
                break;
            case STEP_1_VITAL:
                //Toast.makeText(this, "Show vital summary", Toast.LENGTH_SHORT).show();
                ((TextView) findViewById(R.id.tv_sub_title)).setText(getResources().getString(R.string._1_4_vitals));
                mStep1ProgressBar.setProgress(100);
                mStep2ProgressBar.setProgress(0);
                mStep3ProgressBar.setProgress(0);
                mStep4ProgressBar.setProgress(0);
                mSummaryFrameLayout.setVisibility(View.GONE);
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fl_steps_body, VitalCollectionFragment.newInstance(getIntent(), isEditMode, mVitalsObject), VITAL_FRAGMENT).
                        commit();
                break;
            case STEP_2_VISIT_REASON:
                mStep2ProgressBar.setProgress(20);
                ((TextView) findViewById(R.id.tv_sub_title)).setText(getResources().getString(R.string.visit_reason));
                //Toast.makeText(this, "Show vital summary", Toast.LENGTH_SHORT).show();

                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fl_steps_body, VisitReasonCaptureFragment.newInstance(getIntent(), isEditMode, false), VISIT_REASON_FRAGMENT).
                        commit();
                mSummaryFrameLayout.setVisibility(View.GONE);
                break;

            case STEP_2_VISIT_REASON_QUESTION:
                mSelectedComplainList = (List<ReasonData>) object;
                loadChiefComplainNodeForSelectedNames(mSelectedComplainList);
                mStep2ProgressBar.setProgress(40);
                setTitle(getResources().getString(R.string.visit_reason) + " : " + mSelectedComplainList.get(0).getReasonNameLocalized());
                //Toast.makeText(this, "Show vital summary", Toast.LENGTH_SHORT).show();
                //mSummaryFrameLayout.setVisibility(View.GONE);
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fl_steps_body, VisitReasonQuestionsFragment.newInstance(getIntent(), isEditMode, mChiefComplainRootNodeList), VISIT_REASON_QUESTION_FRAGMENT).
                        commit();
                break;
            case FROM_SUMMARY_RESUME_BACK_FOR_EDIT:
                mSummaryFrameLayout.setVisibility(View.GONE);
                if (object != null) {
                    int caseNo = (int) object;
                    if (caseNo == STEP_4_PAST_MEDICAL_HISTORY) {
                        showPastMedicalHistoryFragment(isEditMode);
                    } else if (caseNo == STEP_5_FAMILY_HISTORY) {
                        showFamilyHistoryFragment(isEditMode);
                    } else if (caseNo == STEP_3_PHYSICAL_EXAMINATION) {
                        mStep3ProgressBar.setProgress(100);
                        setTitle(getResources().getString(R.string._phy_examination));
                        mSummaryFrameLayout.setVisibility(View.GONE);
                        //mPhysicalExamNode =
                        //loadPhysicalExam();
                        getSupportFragmentManager().beginTransaction().
                                replace(R.id.fl_steps_body, PhysicalExaminationFragment.newInstance(getIntent(), isEditMode, physicalExamMap), PHYSICAL_EXAM_FRAGMENT).
                                commit();
                    }
                    // step 2
                    else if (caseNo == STEP_2_VISIT_REASON_QUESTION) {
                        //showFamilyHistoryFragment(isEditMode);
                    } else if (caseNo == STEP_2_VISIT_REASON_QUESTION_ASSOCIATE_SYMPTOMS) {
                        //showFamilyHistoryFragment(isEditMode);
                    }
                }
                break;
            case STEP_2_VISIT_REASON_QUESTION_SUMMARY:
                if (isSavedVisitReason()) {
                    mStep2ProgressBar.setProgress(100);

                    mSummaryFrameLayout.setVisibility(View.VISIBLE);
                    getSupportFragmentManager().beginTransaction().
                            replace(R.id.fl_steps_summary, VisitReasonSummaryFragment.newInstance(getIntent(), insertionWithLocaleJsonString, isEditMode), VISIT_REASON_QUESTION_FRAGMENT).
                            commit();
                }
                break;

            case STEP_3_PHYSICAL_EXAMINATION:
                mStep3ProgressBar.setProgress(10);
                setTitle(getResources().getString(R.string._phy_examination));
                mSummaryFrameLayout.setVisibility(View.GONE);
                //mPhysicalExamNode =
                loadPhysicalExam();
                getSupportFragmentManager().beginTransaction().
                        replace(R.id.fl_steps_body, PhysicalExaminationFragment.newInstance(getIntent(), isEditMode, physicalExamMap), PHYSICAL_EXAM_FRAGMENT).
                        commit();
                break;
            case STEP_3_PHYSICAL_SUMMARY_EXAMINATION:
                if (isSavedPhysicalExam()) {
                    mSummaryFrameLayout.setVisibility(View.VISIBLE);
                    getSupportFragmentManager().beginTransaction().
                            //replace(R.id.fl_steps_summary, PhysicalExamSummaryFragment.newInstance(getIntent(), physicalString, isEditMode), PHYSICAL_EXAM_SUMMARY_FRAGMENT).
                                    replace(R.id.fl_steps_summary, PhysicalExamSummaryFragment.newInstance(getIntent(), physicalStringLocale, isEditMode), PHYSICAL_EXAM_SUMMARY_FRAGMENT).
                            commit();
                }
                break;
            case STEP_4_PAST_MEDICAL_HISTORY:
                showPastMedicalHistoryFragment(isEditMode);
                break;

            case STEP_5_FAMILY_HISTORY:
                showFamilyHistoryFragment(isEditMode);

                break;

            case STEP_5_HISTORY_SUMMARY:
                if (isSavedPastHistory()) {
                    mSummaryFrameLayout.setVisibility(View.VISIBLE);
                    getSupportFragmentManager().beginTransaction().
                            replace(R.id.fl_steps_summary, MedicalHistorySummaryFragment.newInstance(getIntent(), patientHistoryLocale, familyHistoryLocale, isEditMode), PAST_MEDICAL_HISTORY_SUMMARY_FRAGMENT).
                            commit();
                }
                break;
            case STEP_6_VISIT_SUMMARY:
                Intent intent1 = new Intent(VisitCreationActivity.this, VisitSummaryActivity_New.class); // earlier visitsummary
                intent1.putExtra("patientUuid", patientUuid);
                intent1.putExtra("visitUuid", visitUuid);
                intent1.putExtra("encounterUuidVitals", encounterVitals);
                intent1.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                intent1.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                intent1.putExtra("state", state);
                intent1.putExtra("name", patientName);
                intent1.putExtra("gender", patientGender);
                intent1.putExtra("tag", intentTag);
                intent1.putExtra("float_ageYear_Month", float_ageYear_Month);
                intent1.putExtra("hasPrescription", "false");
                // intent1.putStringArrayListExtra("exams", selectedExamsList);
                startActivity(intent1);
                finish();
                break;
        }
    }

    private void showPastMedicalHistoryFragment(boolean isEditMode) {
        mStep4ProgressBar.setProgress(10);
        setTitle(getResources().getString(R.string.patinet_history));
        mSummaryFrameLayout.setVisibility(View.GONE);

        if (mPastMedicalHistoryNode == null) {
            mPastMedicalHistoryNode = loadPastMedicalHistory();
            isEditMode = false;
        }
        getSupportFragmentManager().beginTransaction().
                replace(R.id.fl_steps_body, PastMedicalHistoryFragment.newInstance(getIntent(), isEditMode, mPastMedicalHistoryNode), PAST_MEDICAL_HISTORY_FRAGMENT).
                commit();
    }

    private void showFamilyHistoryFragment(boolean isEditMode) {
        mStep4ProgressBar.setProgress(50);
        setTitle(getResources().getString(R.string._medical_family_history));
        mSummaryFrameLayout.setVisibility(View.GONE);
        //boolean isEditMode = true;
        if (mFamilyHistoryNode == null) {
            mFamilyHistoryNode = loadFamilyHistory();
            isEditMode = false;
        }
        getSupportFragmentManager().beginTransaction().
                replace(R.id.fl_steps_body, FamilyHistoryFragment.newInstance(getIntent(), isEditMode, mFamilyHistoryNode), FAMILY_HISTORY_SUMMARY_FRAGMENT).
                commit();
    }

    private boolean isSavedPastHistory() {
        return savePastHistoryData();
    }

    private boolean isSavedPhysicalExam() {
        return savePhysicalExamData();
    }

    private boolean isSavedVisitReason() {

        // save to cache
        sessionManager.setVisitEditCache(SessionManager.CHIEF_COMPLAIN_LIST + visitUuid, new Gson().toJson(mSelectedComplainList));
        sessionManager.setVisitEditCache(SessionManager.CHIEF_COMPLAIN_QUESTION_NODE + visitUuid, new Gson().toJson(mChiefComplainRootNodeList));
        //**********
        insertion = "";
        insertionLocale = "";
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < mChiefComplainRootNodeList.size(); i++) {
            Node node = mChiefComplainRootNodeList.get(i);
            Log.v(TAG, "mChiefComplainRootNodeList- " + node.findDisplay());
            String val = formatComplainRecord(node, i == mChiefComplainRootNodeList.size() - 1);
            String answerInLocale = bullet_arrow + node.findDisplay() + "::" + node.formQuestionAnswer(0);
            Log.v(TAG, "answerInLocale- " + answerInLocale);

            stringBuilder.append(answerInLocale);
            if (val == null) {
                return false;
            }
        }
        insertionLocale = stringBuilder.toString();


        if (insertion.contains("<br/> ►<b>Associated symptoms</b>: <br/>►<b> Associated symptoms</b>:  <br/>")) {
            insertion = insertion.replace("<br/> ►<b>Associated symptoms</b>: <br/>►<b> Associated symptoms</b>:  <br/>", "<br/>►<b> Associated symptoms</b>:  <br/>");
        }
        JSONObject jsonObject = new JSONObject();
        try {
            insertionLocale = VisitUtils.replaceEnglishCommonString(insertionLocale, sessionManager.getAppLanguage());
            String[] matchDate = DateAndTimeUtils.findDateFromStringDDMMMYYY(insertionLocale);
            if (matchDate != null) {
                for (String date : matchDate) {
                    insertionLocale = insertionLocale.replaceAll(date, DateAndTimeUtils.formatInLocalDateForDDMMMYYYY(date, sessionManager.getAppLanguage()));
                }
            }
            insertion = VisitUtils.replaceToEnglishCommonString(insertion, sessionManager.getAppLanguage());
            jsonObject.put("en", insertion);
            //if(!sessionManager.getAppLanguage().equalsIgnoreCase("en")) {
            jsonObject.put("l-" + sessionManager.getAppLanguage(), insertionLocale);
            //}
            insertionWithLocaleJsonString = jsonObject.toString();
            Log.v(TAG, insertionWithLocaleJsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return insertChiefComplainToDb(insertionWithLocaleJsonString);
    }

    private Node mPhysicalExamNode;
    private String mLastChiefComplainPhysicalString = "";

    private List<Node> loadPhysicalExam() {
        ArrayList<String> physicalExams = new ArrayList<>();
        ArrayList<String> childNodeSelectedPhysicalExams = mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex).getPhysicalExamList();
        if (!childNodeSelectedPhysicalExams.isEmpty())
            physicalExams.addAll(childNodeSelectedPhysicalExams); //For Selected child nodes

        ArrayList<String> rootNodePhysicalExams = parseExams(mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex));
        if (rootNodePhysicalExams != null && !rootNodePhysicalExams.isEmpty())
            physicalExams.addAll(rootNodePhysicalExams); //For Root Node
        Set<String> selectedExams = new LinkedHashSet<>(physicalExams);
        mLastChiefComplainPhysicalString = mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex).getPhysicalExams();
        String[] exm = mLastChiefComplainPhysicalString.split(";");
        HashMap<String, List<String>> map = new HashMap<String, List<String>>();
        for (String s : exm) {
            if (s.contains(":") && s.split(":").length >= 2) {
                String rootNodeName = s.split(":")[0];
                String childNodeName = s.split(":")[1];

                List<String> list = new ArrayList<>();
                if (map.containsKey(rootNodeName)) {
                    list = map.get(rootNodeName);
                }
                list.add(childNodeName);
                map.put(rootNodeName, list);
            }
        }
        String fileLocation = "physExam.json";
        Node filterNode = loadFileToNode(fileLocation);
        ArrayList<String> selectedExamsList = new ArrayList<>(selectedExams);
        Log.v(TAG, "selectedExamsList- " + new Gson().toJson(selectedExamsList));
        physicalExamMap = new PhysicalExam(FileUtils.encodeJSON(this, fileLocation), selectedExamsList);
        physicalExamMap.refreshOnlyLocaleTitle();
        physicalExamMap.setEngineVersion(filterNode.getEngineVersion());
        List<Node> optionsList = new ArrayList<>();
        for (int i = 0; i < filterNode.getOptionsList().size(); i++) {
            /*if (i == 0) {
                optionsList.add(filterNode.getOptionsList().get(i).getOptionsList().get(0).getOptionsList().get(0));
            }*/
            if (map.containsKey(filterNode.getOptionsList().get(i).getText()) && filterNode.getOptionsList().get(i).getOptionsList()!=null) {
                for (int j = 0; j < filterNode.getOptionsList().get(i).getOptionsList().size(); j++) {
                    optionsList.add(filterNode.getOptionsList().get(i).getOptionsList().get(j).getOptionsList().get(0));
                }
            }
        }
        filterNode.setOptionsList(optionsList);
        return physicalExamMap.getSelectedNodes();
    }

    private Node mPastMedicalHistoryNode;

    private Node loadPastMedicalHistory() {
        String fileLocation = "patHist.json";
        return loadFileToNode(fileLocation);
    }

    private Node mFamilyHistoryNode;

    private Node loadFamilyHistory() {
        String fileLocation = "famHist.json";
        return loadFileToNode(fileLocation);
    }

    private Node loadFileToNode(String fileLocation) {
        JSONObject currentFile = FileUtils.encodeJSON(this, fileLocation);
        Node mainNode = new Node(currentFile);
        mainNode.getOptionsList().removeIf(node -> !VisitUtils.checkNodeValidByGenderAndAge(patientGender, float_ageYear_Month, node.getGender(), node.getMin_age(), node.getMax_age()));
        return mainNode;
    }

    private Node mCommonAssociateSymptoms = null;

    private void loadChiefComplainNodeForSelectedNames(List<ReasonData> selectedComplains) {
        for (int i = 0; i < selectedComplains.size(); i++) {
            String fileLocation = "engines/" + selectedComplains.get(i).getReasonName() + ".json";
            JSONObject currentFile = FileUtils.encodeJSON(this, fileLocation);
            Node mainNode = new Node(currentFile);
            List<Node> optionList = new ArrayList<>();
            Node associateSymptoms = null;
            Log.v(TAG, "optionList  mainNode- " + mainNode.getText());
            for (int j = 0; j < mainNode.getOptionsList().size(); j++) {
                if (mainNode.getOptionsList().get(j).getText().equalsIgnoreCase("Associated symptoms")) {
                    if (mCommonAssociateSymptoms == null)
                        mCommonAssociateSymptoms = mainNode.getOptionsList().get(j);
                    else {
                        mCommonAssociateSymptoms.getOptionsList().addAll(mainNode.getOptionsList().get(j).getOptionsList());
                    }

                } else {
                    if (VisitUtils.checkNodeValidByGenderAndAge(patientGender, float_ageYear_Month, mainNode.getOptionsList().get(j).getGender(), mainNode.getOptionsList().get(j).getMin_age(), mainNode.getOptionsList().get(j).getMax_age())) {
                        mainNode.getOptionsList().get(j).getOptionsList().removeIf(node -> !VisitUtils.checkNodeValidByGenderAndAge(patientGender, float_ageYear_Month, node.getGender(), node.getMin_age(), node.getMax_age()));
                        optionList.add(mainNode.getOptionsList().get(j));
                    }
                }
            }
            /*if (mCommonAssociateSymptoms != null) {

                mCommonAssociateSymptoms.getOptionsList().removeIf(node -> !checkNodeValidByGenderAndAge(node.getGender(), node.getMin_age(), node.getMax_age()));

                //optionList.add(associateSymptoms);
            }*/
            mainNode.setOptionsList(optionList);
            mChiefComplainRootNodeList.add(mainNode);

        }
        if (mCommonAssociateSymptoms != null) {

            mCommonAssociateSymptoms.setOptionsList(getNodeWithoutDuplicates(mCommonAssociateSymptoms.getOptionsList()));
            mCommonAssociateSymptoms.getOptionsList().removeIf(node -> !VisitUtils.checkNodeValidByGenderAndAge(patientGender, float_ageYear_Month, node.getGender(), node.getMin_age(), node.getMax_age()));

            //optionList.add(associateSymptoms);
            mChiefComplainRootNodeList.add(mCommonAssociateSymptoms);
        }


    }

    private static List<Node> getNodeWithoutDuplicates(final List<Node> nodes) {
        Set<Node> nodeSet = new TreeSet<Node>(new NodeComparator());
        nodeSet.addAll(nodes);
        return new ArrayList<Node>(nodeSet);
    }

    static class NodeComparator implements Comparator<Node> {

        @Override
        public int compare(Node n1, Node n2) {
            return n1.getText().compareToIgnoreCase(n2.getText());

        }

    }


    public void setTitle(String text) {
        ((TextView) findViewById(R.id.tv_sub_title)).setText(text);
    }

    @Override
    public void onProgress(int progress) {
        switch (mCurrentStep) {
            case STEP_2_VISIT_REASON_QUESTION:
                mStep2ProgressBar.setProgress(mStep2ProgressBar.getProgress() + progress);
                break;
            case STEP_3_PHYSICAL_EXAMINATION:
                mStep3ProgressBar.setProgress(mStep2ProgressBar.getProgress() + progress);
                break;
        }
    }

    @Override
    public void onTitleChange(String title) {
        switch (mCurrentStep) {
            case STEP_2_VISIT_REASON_QUESTION:
                if (title == null || title.isEmpty()) {
                    setTitle(getResources().getString(R.string.visit_reason) + " : " + mSelectedComplainList.get(0).getReasonNameLocalized());
                } else {
                    setTitle(title);
                }
                break;
            case STEP_3_PHYSICAL_EXAMINATION:
                setTitle(title);
                break;
        }

    }

    @Override
    public void onManualClose() {
        switch (mCurrentStep) {
            case STEP_1_VITAL_SUMMARY:
                mSummaryFrameLayout.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onCameraOpenRequest() {
        openCamera();
    }

    @Override
    public void onImageRemoved(int nodeIndex, int imageIndex, String image) {
        deleteImageFromDatabase(nodeIndex, imageIndex, image);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    boolean nodeComplete = false;

    public void filterNodeQuestions() {

    }

    String insertion = "";
    String insertionLocale = "";
    String insertionWithLocaleJsonString = "";

    //new code for the one by one complain data capture
    public String formatComplainRecord(Node currentNode, boolean isAssociateSymptom) {
        // checking any question missing
        // can check also compulsory question

        AnswerResult answerResult = isAssociateSymptom ? currentNode.checkAllRequiredAnsweredRootNode(this) : currentNode.checkAllRequiredAnswered(this);
        if (!answerResult.result) {
            // show alert dialog
            DialogUtils dialogUtils = new DialogUtils();
            dialogUtils.showCommonDialog(VisitCreationActivity.this, 0, getString(R.string.alert_label_txt), answerResult.requiredStrings, true, getResources().getString(R.string.generic_ok), getResources().getString(R.string.cancel), new DialogUtils.CustomDialogListener() {
                @Override
                public void onDialogActionDone(int action) {

                }
            });
            /*MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
            alertDialogBuilder.setMessage(answerResult.requiredStrings);
            alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                }
            });
            Dialog alertDialog = alertDialogBuilder.show();*/
            Log.v(TAG, answerResult.requiredStrings);
            return null;
        }


        // upload images if any

        // generate language from current node

        String complaintString = isAssociateSymptom ? currentNode.generateLanguageSingleNode() : currentNode.generateLanguage();

        Log.v("formatComplainRecord", "Value - " + complaintString);
        if (complaintString != null && !complaintString.isEmpty()) {
            //     String complaintFormatted = complaintString.replace("?,", "?:");

            String complaint = currentNode.getText();
            //    complaintDetails.put(complaint, complaintFormatted);

//                insertion = insertion.concat(Node.bullet_arrow + "<b>" + complaint + "</b>" + ": " + Node.next_line + complaintString + " ");
            insertion = insertion.concat(bullet_arrow + "<b>" + complaint + "</b>" + ": " + Node.next_line + complaintString + " ");
        } else {
            String complaint = currentNode.getText();
            if (!complaint.equalsIgnoreCase(getResources().getString(R.string.associated_symptoms))) {
//                    insertion = insertion.concat(Node.bullet_arrow + "<b>" + complaint + "</b>" + ": " + Node.next_line + " ");
                insertion = insertion.concat(bullet_arrow + "<b>" + complaint + "</b>" + ": " + Node.next_line + " ");
            }
        }
        Log.v("formatComplainRecord", "Value - " + insertion);
        return insertion;

    }

    /**
     *
     */
    private void showNextComplainQueries() {
        mCurrentComplainNodeIndex++;
        mStep2ProgressBar.setProgress(mStep2ProgressBar.getProgress() + 10);
        setTitle(getResources().getString(R.string.visit_reason) + " : " + mSelectedComplainList.get(mCurrentComplainNodeIndex).getReasonNameLocalized());
        //Toast.makeText(this, "Show vital summary", Toast.LENGTH_SHORT).show();
        //mSummaryFrameLayout.setVisibility(View.GONE);
       /* getSupportFragmentManager().beginTransaction().
                replace(R.id.fl_steps_body, VisitReasonQuestionsFragment.newInstance(getIntent(), mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex)), VISIT_REASON_QUESTION_FRAGMENT).
                commit();*/
    }

    /**
     * Insert into DB could be made into a Helper Method, but isn't because there are specific concept IDs used each time.
     * Although this could also be made into a function, for now it has now been.
     *
     * @param value String to put into DB
     * @return DB Row number, never used
     */
    private boolean insertChiefComplainToDb(String value) {
        boolean isInserted = false;
        try {
            Log.i(TAG, "insertChiefComplainToDb: " + patientUuid + " " + visitUuid + " " + UuidDictionary.CURRENT_COMPLAINT);
            Log.i(TAG, "insertChiefComplainToDb: " + value);
            ObsDAO obsDAO = new ObsDAO();
            ObsDTO obsDTO = new ObsDTO();
            String uuidOBS = obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.CURRENT_COMPLAINT);
            Log.i(TAG, "insertChiefComplainToDb: uuidOBS - " + uuidOBS);
            obsDTO.setConceptuuid(UuidDictionary.CURRENT_COMPLAINT);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(StringUtils.getValue1(value));
            if (uuidOBS != null) {
                obsDTO.setUuid(uuidOBS);
                Log.v("obsDTO update", new Gson().toJson(obsDTO));

                isInserted = obsDAO.updateObs(obsDTO);
            } else {
                Log.v("obsDTO insert", new Gson().toJson(obsDTO));
                isInserted = obsDAO.insertObs(obsDTO);
            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }


        return isInserted;
    }


    private void updateDatabase(String string) {
        Log.i(TAG, "updateDatabase: " + patientUuid + " " + visitUuid + " " + UuidDictionary.CURRENT_COMPLAINT);
//        }
        ObsDTO obsDTO = new ObsDTO();
        ObsDAO obsDAO = new ObsDAO();
        try {
            obsDTO.setConceptuuid(UuidDictionary.CURRENT_COMPLAINT);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(string);
            obsDTO.setUuid(obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.CURRENT_COMPLAINT));

            obsDAO.updateObs(obsDTO);

        } catch (DAOException dao) {
            FirebaseCrashlytics.getInstance().recordException(dao);
        }

        EncounterDAO encounterDAO = new EncounterDAO();
        try {
            encounterDAO.updateEncounterSync("false", encounterAdultIntials);
            encounterDAO.updateEncounterModifiedDate(encounterAdultIntials);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

    }

    private ArrayList<String> parseExams(Node node) {
        ArrayList<String> examList = new ArrayList<>();
        String rawExams = node.getPhysicalExams();
        if (rawExams != null) {
            String[] splitExams = rawExams.split(";");
            examList.addAll(Arrays.asList(splitExams));
            return examList;
        }
        return null;
    }

    public static void openCamera(Activity activity, String imagePath, String imageName) {
        Log.d(TAG, "open Camera!");
        Intent cameraIntent = new Intent(activity, CameraActivity.class);
        if (imageName != null && imagePath != null) {
            File filePath = new File(imagePath);
            if (!filePath.exists()) {
                boolean res = filePath.mkdirs();
            }
            cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
            cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, imagePath);
        }
        activity.startActivityForResult(cameraIntent, Node.TAKE_IMAGE_FOR_NODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Node.TAKE_IMAGE_FOR_NODE) {
            if (resultCode == RESULT_OK) {
                String mCurrentPhotoPath = data.getStringExtra("RESULT");
                // currentNode.setImagePath(mCurrentPhotoPath);
                // currentNode.displayImage(this, filePath.getAbsolutePath(), imageName);
            }
        }
    }

    /*Physical exam*/
    private boolean insertDbPhysicalExam(String value) {
        Log.i(TAG, "insertDb: ");
        boolean isInserted = false;
        try {
            ObsDAO obsDAO = new ObsDAO();
            ObsDTO obsDTO = new ObsDTO();
            String uuidOBS = obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.PHYSICAL_EXAMINATION);
            Log.i(TAG, "insertDbPhysicalExam: uuidOBS - " + uuidOBS);

            obsDTO.setConceptuuid(UuidDictionary.PHYSICAL_EXAMINATION);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(StringUtils.getValue(value));

            if (uuidOBS != null) {
                obsDTO.setUuid(uuidOBS);
                Log.v("obsDTO update", new Gson().toJson(obsDTO));

                isInserted = obsDAO.updateObs(obsDTO);
            } else {
                Log.v("obsDTO insert", new Gson().toJson(obsDTO));
                isInserted = obsDAO.insertObs(obsDTO);
            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        return isInserted;
    }

    String physicalString;
    String physicalStringLocale = "";
    String physicalStringWithLocaleJsonString = "";
    Boolean complaintConfirmed = false;
    PhysicalExam physicalExamMap;

    private boolean savePhysicalExamData() {
        Log.v(TAG, "savePhysicalExamData");
        // save to cache
        sessionManager.setVisitEditCache(SessionManager.PHY_EXAM + visitUuid, new Gson().toJson(physicalExamMap));
        //**********
        complaintConfirmed = physicalExamMap.areRequiredAnswered();

        if (complaintConfirmed) {

            physicalString = physicalExamMap.generateFindings();
            //physicalStringLocale = sessionManager.getAppLanguage().equalsIgnoreCase("en") ?
            //       physicalString : physicalExamMap.generateFindingsByLocale(sessionManager.getAppLanguage());
            physicalStringLocale = physicalExamMap.generateFindingsByLocale(sessionManager.getAppLanguage());
            Log.v(TAG, "physicalStringLocale -" + physicalStringLocale);
            while (physicalString.contains("[Describe"))
                physicalString = physicalString.replace("[Describe]", "");

            List<String> imagePathList = physicalExamMap.getImagePathList();
            Log.v(TAG, "savePhysicalExamData, imagePathList " + imagePathList);
            if (imagePathList != null) {
                for (String imagePath : imagePathList) {
                    updateImageDatabase(imagePath);
                }
            }
            JSONObject jsonObject = new JSONObject();
            try {
                physicalStringLocale = VisitUtils.replaceEnglishCommonString(physicalStringLocale, sessionManager.getAppLanguage());
                String[] matchDate = DateAndTimeUtils.findDateFromStringDDMMMYYY(physicalStringLocale);
                if (matchDate != null) {
                    for (String date : matchDate) {
                        physicalStringLocale = physicalStringLocale.replaceAll(date, DateAndTimeUtils.formatInLocalDateForDDMMMYYYY(date, sessionManager.getAppLanguage()));
                    }
                }
                physicalString = VisitUtils.replaceToEnglishCommonString(physicalString, sessionManager.getAppLanguage());
                jsonObject.put("en", physicalString);
                //if(!sessionManager.getAppLanguage().equalsIgnoreCase("en")) {
                jsonObject.put("l-" + sessionManager.getAppLanguage(), physicalStringLocale);
                //}
                physicalStringWithLocaleJsonString = jsonObject.toString();
                Log.v(TAG, physicalStringWithLocaleJsonString);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            questionsMissing();
        }
        return insertDbPhysicalExam(physicalStringWithLocaleJsonString);
    }

    private String patientHistory, familyHistory;
    String patientHistoryLocale = "", familyHistoryLocale = "";
    String patientHistoryWithLocaleJsonString = "", familyHistoryWithLocaleJsonString = "";

    /**
     * @return
     */
    private boolean savePastHistoryData() {
        // save to cache
        sessionManager.setVisitEditCache(SessionManager.PATIENT_HISTORY + visitUuid, new Gson().toJson(mPastMedicalHistoryNode));
        sessionManager.setVisitEditCache(SessionManager.FAMILY_HISTORY + visitUuid, new Gson().toJson(mFamilyHistoryNode));
        //**********
        patientHistory = mPastMedicalHistoryNode.generateLanguage();
        patientHistoryLocale = mPastMedicalHistoryNode.formQuestionAnswer(0);
        if (mPastMedicalHistoryNode.getEngineVersion() != null && mPastMedicalHistoryNode.getEngineVersion().equals("4.0"))
            patientHistoryLocale = mPastMedicalHistoryNode.formQuestionAnswerV2(0);
        while (patientHistory.contains("[Describe"))
            patientHistory = patientHistory.replace("[Describe]", "");

        //familyHistory = mFamilyHistoryNode.generateLanguage();

        familyHistory = generateFamilyHistoryAns(false);
        familyHistoryLocale = generateFamilyHistoryAns(true);

        familyHistory = familyHistory.replaceAll("null.", "");


        while (familyHistory.contains("[Describe"))
            familyHistory = familyHistory.replace("[Describe]", "");
        List<String> imagePathList = mFamilyHistoryNode.getImagePathList();

        if (imagePathList != null) {
            for (String imagePath : imagePathList) {
                updateImageDatabase(imagePath);
            }
        }

        JSONObject jsonObject = new JSONObject();
        JSONObject jsonObject1 = new JSONObject();
        try {
            patientHistoryLocale = VisitUtils.replaceEnglishCommonString(patientHistoryLocale, sessionManager.getAppLanguage());

            String[] matchDate = DateAndTimeUtils.findDateFromStringDDMMMYYY(patientHistoryLocale);
            if (matchDate != null) {
                for (String date : matchDate) {
                    patientHistoryLocale = patientHistoryLocale.replaceAll(date, DateAndTimeUtils.formatInLocalDateForDDMMMYYYY(date, sessionManager.getAppLanguage()));
                }
            }

            patientHistory = VisitUtils.replaceToEnglishCommonString(patientHistory, sessionManager.getAppLanguage());
            jsonObject.put("en", patientHistory);
            //if(!sessionManager.getAppLanguage().equalsIgnoreCase("en")) {
            jsonObject.put("l-" + sessionManager.getAppLanguage(), patientHistoryLocale);
            //}
            patientHistoryWithLocaleJsonString = jsonObject.toString();
            Log.v(TAG, patientHistoryWithLocaleJsonString);

            familyHistoryLocale = VisitUtils.replaceEnglishCommonString(familyHistoryLocale, sessionManager.getAppLanguage());

            String[] matchDate1 = DateAndTimeUtils.findDateFromStringDDMMMYYY(familyHistoryLocale);
            if (matchDate1 != null) {
                for (String date : matchDate1) {
                    familyHistoryLocale = familyHistoryLocale.replaceAll(date, DateAndTimeUtils.formatInLocalDateForDDMMMYYYY(date, sessionManager.getAppLanguage()));
                }
            }

            familyHistory = VisitUtils.replaceToEnglishCommonString(familyHistory, sessionManager.getAppLanguage());
            jsonObject1.put("en", familyHistory);
            //if(!sessionManager.getAppLanguage().equalsIgnoreCase("en")) {
            jsonObject1.put("l-" + sessionManager.getAppLanguage(), familyHistoryLocale);
            //}
            familyHistoryWithLocaleJsonString = jsonObject1.toString();
            Log.v(TAG, familyHistoryWithLocaleJsonString);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return insertDbPastHistory(patientHistoryWithLocaleJsonString, familyHistoryWithLocaleJsonString);
    }

    private String generateFamilyHistoryAns(boolean isLocale) {
        String familyHistory = "";
        ArrayList<String> familyInsertionList = new ArrayList<>();
        if (mFamilyHistoryNode.anySubSelected()) {
            for (Node node : mFamilyHistoryNode.getOptionsList()) {
                if (node.isSelected()) {
                    String familyString = !isLocale ? node.generateLanguage() : node.formQuestionAnswer(0);
                    String toInsert = (!isLocale ? node.getText() : node.findDisplay()) + " : " + familyString;
                    //toInsert = toInsert.replaceAll(Node.bullet, "");
                    toInsert = toInsert.replaceAll(" - ", ", ");
                    toInsert = toInsert.replaceAll("<br/>", "");
                    if (org.apache.commons.lang3.StringUtils.right(toInsert, 2).equals(", ")) {
                        toInsert = toInsert.substring(0, toInsert.length() - 2);
                    }
                    toInsert = toInsert + ".<br/>";
                    familyInsertionList.add(toInsert);
                }
            }
        }

        for (int i = 0; i < familyInsertionList.size(); i++) {
            if (i == 0) {
                familyHistory = Node.bullet + familyInsertionList.get(i);
            } else {
                familyHistory = familyHistory + " " + Node.bullet + familyInsertionList.get(i);
            }
        }
        return familyHistory;
    }

    /*Physical exam*/
    private boolean insertDbPastHistory(String patientHistory, String familyHistory) {
        Log.i(TAG, "insertDb: ");
        boolean isInserted = false;
        try {
            ObsDAO obsDAO = new ObsDAO();

            String uuidOBS = obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB);
            Log.i(TAG, "insertDbPastHistory patientHistory : uuidOBS - " + uuidOBS);

            ObsDTO obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.RHK_MEDICAL_HISTORY_BLURB);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(StringUtils.getValue(patientHistory));


            if (uuidOBS != null) {
                obsDTO.setUuid(uuidOBS);
                Log.v("obsDTO update", new Gson().toJson(obsDTO));

                isInserted = obsDAO.updateObs(obsDTO);
            } else {
                Log.v("obsDTO insert", new Gson().toJson(obsDTO));
                isInserted = obsDAO.insertObs(obsDTO);
            }

            String uuidOBS1 = obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.RHK_FAMILY_HISTORY_BLURB);
            Log.i(TAG, "insertDbPastHistory familyHistory : uuidOBS - " + uuidOBS1);
            obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.RHK_FAMILY_HISTORY_BLURB);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(org.intelehealth.app.utilities.StringUtils.getValue(familyHistory));

            if (uuidOBS1 != null) {
                obsDTO.setUuid(uuidOBS1);
                Log.v("obsDTO update", new Gson().toJson(obsDTO));

                isInserted = obsDAO.updateObs(obsDTO);
            } else {
                Log.v("obsDTO insert", new Gson().toJson(obsDTO));
                isInserted = obsDAO.insertObs(obsDTO);
            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        return isInserted;
    }

    public void questionsMissing() {
        DialogUtils dialogUtils = new DialogUtils();
        dialogUtils.showCommonDialog(VisitCreationActivity.this, 0, getString(R.string.alert_label_txt), getResources().getString(R.string.question_answer_all_phy_exam), true, getResources().getString(R.string.generic_ok), getResources().getString(R.string.cancel), new DialogUtils.CustomDialogListener() {
            @Override
            public void onDialogActionDone(int action) {

            }
        });

        /*MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
        alertDialogBuilder.setMessage(getResources().getString(R.string.question_answer_all_phy_exam));
        alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.show();
        //alertDialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);*/
    }

    private void updateImageDatabase(String imageName) {
        ImagesDAO imagesDAO = new ImagesDAO();

        try {
            imagesDAO.insertObsImageDatabase(imageName, encounterAdultIntials, UuidDictionary.COMPLEX_IMAGE_PE);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }


    private void deleteImageFromDatabase(int nodeIndex, int imageIndex, String imageName) {
        ImagesDAO imagesDAO = new ImagesDAO();

        try {
            String obsUUID = imageName.substring(imageName.lastIndexOf("/") + 1).split("\\.")[0];
            imagesDAO.deleteImageFromDatabase(obsUUID);
            imageUtilsListener.onImageReadyForDelete(nodeIndex, imageIndex, imageName);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    ActivityResultLauncher<Intent> mStartForCameraResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        // Handle the Intent
                        String mCurrentPhotoPath = data.getStringExtra("RESULT");

                        Bundle bundle = new Bundle();
                        bundle.putString("image", mCurrentPhotoPath);
                        imageUtilsListener.onImageReady(bundle);

                        //physicalExamMap.setImagePath(mCurrentPhotoPath);
                        Log.i(TAG, mCurrentPhotoPath);
                        //physicalExamMap.displayImage(this, filePath.getAbsolutePath(), imageName);
                        updateImageDatabase(mLastSelectedImageName);
                    }
                }
            });
    ActivityResultLauncher<Intent> mStartForGalleryResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        String currentPhotoPath = "";
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

                            // copy & rename the file
                            mLastSelectedImageName = UUID.randomUUID().toString();
                            currentPhotoPath = AppConstants.IMAGE_PATH + mLastSelectedImageName + ".jpg";
                            BitmapUtils.copyFile(picturePath, currentPhotoPath);

                            // Handle the Intent


                            Bundle bundle = new Bundle();
                            bundle.putString("image", currentPhotoPath);
                            imageUtilsListener.onImageReady(bundle);

                            //physicalExamMap.setImagePath(mCurrentPhotoPath);
                            Log.i(TAG, currentPhotoPath);
                            //physicalExamMap.displayImage(this, filePath.getAbsolutePath(), imageName);
                            updateImageDatabase(mLastSelectedImageName);
                        } else {
                            Toast.makeText(VisitCreationActivity.this, getResources().getString(R.string.unable_to_pick_data), Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            });

    private String mLastSelectedImageName = "";

    public void openCamera() {
        validatePermissionAndIntent();
    }

    private void cameraStart() {
        File file = new File(AppConstants.IMAGE_PATH);
        final String imagePath = file.getAbsolutePath();
        final String imageName = UUID.randomUUID().toString();
        mLastSelectedImageName = imageName;
        Intent cameraIntent = new Intent(VisitCreationActivity.this, CameraActivity.class);
        File filePath = new File(imagePath);
        if (!filePath.exists()) {
            boolean res = filePath.mkdirs();
        }
        cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
        cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, imagePath);
        //mContext.startActivityForResult(cameraIntent, Node.TAKE_IMAGE_FOR_NODE);
        mStartForCameraResult.launch(cameraIntent);
    }

    private void galleryStart() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        mStartForGalleryResult.launch(intent);
    }

    private static final int MY_CAMERA_REQUEST_CODE = 1001;
    private static final int PICK_IMAGE_FROM_GALLERY = 2001;
    private AlertDialog mImagePickerAlertDialog;

    private void selectImage() {
        mImagePickerAlertDialog = DialogUtils.showCommonImagePickerDialog(this, getString(R.string.add_image_by), new DialogUtils.ImagePickerDialogListener() {
            @Override
            public void onActionDone(int action) {
                mImagePickerAlertDialog.dismiss();
                if (action == DialogUtils.ImagePickerDialogListener.CAMERA) {
                    cameraStart();

                } else if (action == DialogUtils.ImagePickerDialogListener.GALLERY) {
                    galleryStart();
                }
            }
        });
       /* final CharSequence[] options = {getString(R.string.take_photo), getString(R.string.choose_from_gallery), getString(R.string.cancel)};
        AlertDialog.Builder builder = new AlertDialog.Builder(VisitCreationActivity.this);
        builder.setTitle(getResources().getString(R.string.add_image_by));
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    cameraStart();

                } else if (item == 1) {
                    galleryStart();

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();*/
    }


    private void validatePermissionAndIntent() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        } else {
            //cameraStart();
            selectImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                cameraStart();
                selectImage();
            } else {
                Toast.makeText(this, getResources().getString(R.string.camera_permission_denied), Toast.LENGTH_LONG).show();
            }
        }
    }

    ImageUtilsListener imageUtilsListener;

    public void setImageUtilsListener(ImageUtilsListener imageUtilsListener) {
        this.imageUtilsListener = imageUtilsListener;
    }

    private ObjectAnimator syncAnimator;

    public void syncNow(View view) {
        if (NetworkConnection.isOnline(this)) {
            SyncUtils.syncNow(this, view, syncAnimator);
        }
    }

    public void showInfo(View view) {
    }

    public interface ImageUtilsListener {
        void onImageReady(Bundle bundle);

        void onImageReadyForDelete(int nodeIndex, int imageIndex, String imageName);
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            });
}