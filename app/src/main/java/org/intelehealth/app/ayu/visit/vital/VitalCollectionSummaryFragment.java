package org.intelehealth.app.ayu.visit.vital;

import static org.intelehealth.app.ayu.visit.VisitCreationActivity.STEP_1_VITAL_SUMMARY;
import static org.intelehealth.app.ayu.visit.VisitCreationActivity.STEP_2_DIAGNOSTICS_SUMMARY;
import static org.intelehealth.app.ayu.visit.common.VisitUtils.convertCtoF;
import static org.intelehealth.app.syncModule.SyncUtils.syncNow;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.checkerframework.checker.units.qual.A;
import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.ayu.visit.VisitCreationActionListener;
import org.intelehealth.app.ayu.visit.VisitCreationActivity;
import org.intelehealth.app.ayu.visit.common.ManageSummaryScreenTitles;
import org.intelehealth.app.ayu.visit.common.VisitUtils;
import org.intelehealth.app.ayu.visit.common.adapter.NodeAdapterUtils;
import org.intelehealth.app.ayu.visit.model.ReasonData;
import org.intelehealth.app.models.VitalsObject;
import org.intelehealth.app.utilities.ConfigUtils;
import org.intelehealth.app.utilities.CustomLog;
import org.intelehealth.app.utilities.FlavorKeys;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.config.presenter.fields.data.PatientVitalRepository;
import org.intelehealth.config.presenter.fields.factory.PatientVitalViewModelFactory;
import org.intelehealth.config.presenter.fields.viewmodel.PatientVitalViewModel;
import org.intelehealth.config.room.ConfigDatabase;
import org.intelehealth.config.room.entity.FeatureActiveStatus;
import org.intelehealth.config.room.entity.PatientVital;
import org.intelehealth.config.utility.PatientVitalConfigKeys;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VitalCollectionSummaryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VitalCollectionSummaryFragment extends Fragment {
    private static final String TAG = VitalCollectionSummaryFragment.class.getSimpleName();

    private VisitCreationActionListener mActionListener;
    SessionManager sessionManager;
    private VitalsObject mVitalsObject;
    private boolean mIsEditMode = false;
    private List<PatientVital> mPatientVitalList;
    private LinearLayout mHeightLinearLayout, mWeightLinearLayout, mBMILinearLayout, mBPLinearLayout, mPulseLinearLayout, mTemperatureLinearLayout, mSpo2LinearLayout, mRespiratoryRateLinearLayout, mBloodGroupLinearLayout;

    public VitalCollectionSummaryFragment() {
        // Required empty public constructor
    }


    public static VitalCollectionSummaryFragment newInstance(VitalsObject result, boolean isEditMode) {
        VitalCollectionSummaryFragment fragment = new VitalCollectionSummaryFragment();
        fragment.mVitalsObject = result;
        fragment.mIsEditMode = isEditMode;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActionListener = (VisitCreationActionListener) context;
        sessionManager = new SessionManager(context);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FeatureActiveStatus status = ((VisitCreationActivity) requireActivity()).getFeatureActiveStatus();
        String title = ManageSummaryScreenTitles.setScreenTitle(requireActivity(), status, STEP_1_VITAL_SUMMARY);
        TextView tvTitle = view.findViewById(R.id.tv_sub_title);
        tvTitle.setText(title);
        //config viewmodel initialization
        PatientVitalRepository repository = new PatientVitalRepository(ConfigDatabase.getInstance(requireActivity()).patientVitalDao());
        PatientVitalViewModelFactory factory = new PatientVitalViewModelFactory(repository);
        PatientVitalViewModel patientVitalViewModel = new ViewModelProvider(this, factory).get(PatientVitalViewModel.class);
        //requireActivity();
        patientVitalViewModel.getAllEnabledLiveFields()
                .observe(requireActivity(), it -> {
                            mPatientVitalList = it;
                            //Timber.tag(TAG).v(new Gson().toJson(mPatientVitalList));
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_vital_collection_summary, container, false);

        mHeightLinearLayout = view.findViewById(R.id.ll_height_container);
        mWeightLinearLayout = view.findViewById(R.id.ll_weight_container);
        mBMILinearLayout = view.findViewById(R.id.ll_bmi);
        mBPLinearLayout = view.findViewById(R.id.ll_bp_container);
        mPulseLinearLayout = view.findViewById(R.id.ll_pulse_container);
        mTemperatureLinearLayout = view.findViewById(R.id.ll_temperature_container);
        mSpo2LinearLayout = view.findViewById(R.id.ll_spo2_container);
        mRespiratoryRateLinearLayout = view.findViewById(R.id.ll_respiratory_rate_container);
        mBloodGroupLinearLayout = view.findViewById(R.id.ll_blood_group_container);


        if (mVitalsObject != null) {
            if (mVitalsObject.getHeight() != null && !mVitalsObject.getHeight().isEmpty() && !mVitalsObject.getHeight().equalsIgnoreCase("0"))
                ((TextView) view.findViewById(R.id.tv_height)).setText(mVitalsObject.getHeight() + " " + getResources().getString(R.string.cm));
            else
                ((TextView) view.findViewById(R.id.tv_height)).setText(getString(R.string.ui2_no_information));

            if (mVitalsObject.getWeight() != null && !mVitalsObject.getWeight().isEmpty())
                ((TextView) view.findViewById(R.id.tv_weight)).setText(mVitalsObject.getWeight() + " " + getResources().getString(R.string.kg));
            else
                ((TextView) view.findViewById(R.id.tv_weight)).setText(getString(R.string.ui2_no_information));

            if (mVitalsObject.getBmi() != null && !mVitalsObject.getBmi().isEmpty())
                ((TextView) view.findViewById(R.id.tv_bmi)).setText(mVitalsObject.getBmi() + " " + getResources().getString(R.string.kg_m));
            else
                ((TextView) view.findViewById(R.id.tv_bmi)).setText(getString(R.string.ui2_no_information));


            if (mVitalsObject.getBpsys() != null && !mVitalsObject.getBpsys().isEmpty())
                ((TextView) view.findViewById(R.id.tv_bp)).setText(mVitalsObject.getBpsys() + "/" + mVitalsObject.getBpdia());
            else
                ((TextView) view.findViewById(R.id.tv_bp)).setText(getString(R.string.ui2_no_information));
            if (mVitalsObject.getPulse() != null && !mVitalsObject.getPulse().isEmpty())
                ((TextView) view.findViewById(R.id.tv_pulse)).setText(mVitalsObject.getPulse() + " " + getResources().getString(R.string.bpm));
            else
                ((TextView) view.findViewById(R.id.tv_pulse)).setText(getString(R.string.ui2_no_information));

            if (mVitalsObject.getTemperature() != null && !mVitalsObject.getTemperature().isEmpty()) {
                if (new ConfigUtils(getActivity()).fahrenheit()) {
                    ((TextView) view.findViewById(R.id.tv_temperature)).setText(convertCtoF(TAG, mVitalsObject.getTemperature()));
                } else {
                    ((TextView) view.findViewById(R.id.tv_temperature)).setText(mVitalsObject.getTemperature());
                }
            } else {

                ((TextView) view.findViewById(R.id.tv_temperature)).setText(getString(R.string.ui2_no_information));
            }

            if (mVitalsObject.getSpo2() != null && !mVitalsObject.getSpo2().isEmpty())
                ((TextView) view.findViewById(R.id.tv_spo2)).setText(mVitalsObject.getSpo2() + " %");
            else
                ((TextView) view.findViewById(R.id.tv_spo2)).setText(getString(R.string.ui2_no_information));

            if (mVitalsObject.getBloodGroup() != null && !mVitalsObject.getBloodGroup().isEmpty())
                ((TextView) view.findViewById(R.id.tv_blood_group)).setText(VisitUtils.getBloodPressureEnStringFromCode(mVitalsObject.getBloodGroup()));
            else
                ((TextView) view.findViewById(R.id.tv_blood_group)).setText(getString(R.string.ui2_no_information));


            if (mVitalsObject.getResp() != null && !mVitalsObject.getResp().isEmpty())
                ((TextView) view.findViewById(R.id.tv_respiratory_rate)).setText(mVitalsObject.getResp() + " " + getResources().getString(R.string.breaths_min));
            else
                ((TextView) view.findViewById(R.id.tv_respiratory_rate)).setText(getString(R.string.ui2_no_information));
        }
        view.findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsEditMode && ((VisitCreationActivity) requireActivity()).isEditTriggerFromVisitSummary()) {
                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                } else {
                    // mActionListener.onFormSubmitted(VisitCreationActivity.STEP_3_VISIT_REASON, mIsEditMode, mVitalsObject);
                    FeatureActiveStatus status = ((VisitCreationActivity) requireActivity()).getFeatureActiveStatus();
                    int nextStep = (status != null && status.getActiveStatusDiagnosticsSection())
                            ? VisitCreationActivity.STEP_2_DIAGNOSTICS
                            : VisitCreationActivity.STEP_3_VISIT_REASON;
                    mActionListener.onFormSubmitted(nextStep, mIsEditMode, mVitalsObject);

                }
            }
        });
        view.findViewById(R.id.tv_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_1_VITAL, mIsEditMode, mVitalsObject);
            }
        });
        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_1_VITAL, mIsEditMode, mVitalsObject);
            }
        });
        view.findViewById(R.id.img_btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_1_VITAL, mIsEditMode, mVitalsObject);
            }
        });
        ImageButton refresh = view.findViewById(R.id.imb_btn_refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkConnection.isOnline(getActivity())) {
                    syncNow(getActivity(), refresh, syncAnimator);
                }
            }
        });
        return view;
    }

    private ObjectAnimator syncAnimator;

    private List<ReasonData> getVisitReasonFilesNamesOnly() {
        List<ReasonData> reasonDataList = new ArrayList<ReasonData>();
        try {
            String[] temp = null;
            CustomLog.e("MindMapURL", "Successfully get MindMap URL" + sessionManager.getLicenseKey());
            if (!sessionManager.getLicenseKey().isEmpty()) {
                File base_dir = new File(requireActivity().getFilesDir().getAbsolutePath() + File.separator + AppConstants.JSON_FOLDER);
                File[] files = base_dir.listFiles();
                temp = new String[files.length];
                for (int i = 0; i < files.length; i++) {
                    temp[i] = files[i].getName();
                }
            } else {
                temp = getActivity().getApplicationContext().getAssets().list("engines");

            }
            for (String s : temp) {
                String fileName = s.split(".json")[0];
                //Timber.tag("VisitReasonCaptureFragment").d("File name=>%s", fileName);
                ReasonData reasonData = new ReasonData();
                reasonData.setReasonName(fileName);
                reasonData.setReasonNameLocalized(NodeAdapterUtils.getTheChiefComplainNameWRTLocale(getActivity(), fileName));
                reasonDataList.add(reasonData);
            }
        } catch (IOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        return reasonDataList;
    }

}
