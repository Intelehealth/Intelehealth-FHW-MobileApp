package org.intelehealth.app.ayu.visit.diagnostics;

import static org.intelehealth.app.ayu.visit.VisitCreationActivity.STEP_2_DIAGNOSTICS_SUMMARY;
import static org.intelehealth.app.ayu.visit.common.VisitUtils.convertCtoF;
import static org.intelehealth.app.syncModule.SyncUtils.syncNow;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.ayu.visit.VisitCreationActionListener;
import org.intelehealth.app.ayu.visit.VisitCreationActivity;
import org.intelehealth.app.ayu.visit.common.ManageSummaryScreenTitles;
import org.intelehealth.app.databinding.FragmentDiagnosticsCollectionBinding;
import org.intelehealth.app.databinding.FragmentDiagnosticsCollectionSummaryBinding;
import org.intelehealth.app.models.DiagnosticsModel;
import org.intelehealth.app.utilities.ConfigUtils;
import org.intelehealth.app.utilities.CustomLog;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.config.presenter.fields.data.DiagnosticsRepository;
import org.intelehealth.config.presenter.fields.factory.DiagnosticsViewModelFactory;
import org.intelehealth.config.presenter.fields.viewmodel.DiagnosticsViewModel;
import org.intelehealth.config.room.ConfigDatabase;
import org.intelehealth.config.room.entity.Diagnostics;
import org.intelehealth.config.room.entity.FeatureActiveStatus;
import org.intelehealth.config.utility.PatientDiagnosticsConfigKeys;
import org.intelehealth.config.utility.PatientDiagnosticsConfigKeys;

import java.util.List;
import java.util.Objects;

public class DiagnosticsCollectionSummaryFragment extends Fragment {
    private static final String TAG = DiagnosticsCollectionSummaryFragment.class.getSimpleName();

    private VisitCreationActionListener mActionListener;
    SessionManager sessionManager;
    private DiagnosticsModel diagnosticsModel;
    private boolean mIsEditMode = false;
    private List<Diagnostics> mDiagnosticsList;
    private FragmentDiagnosticsCollectionSummaryBinding mBinding;

    public DiagnosticsCollectionSummaryFragment() {
        // Required empty public constructor
    }


    public static DiagnosticsCollectionSummaryFragment newInstance(DiagnosticsModel result, boolean isEditMode) {
        DiagnosticsCollectionSummaryFragment fragment = new DiagnosticsCollectionSummaryFragment();
        fragment.diagnosticsModel = result;
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
        /* int index = status.getVitalSection() ? 2 : 1;
        int total = status.getVitalSection() ? 5 : 4;*/
        String title = ManageSummaryScreenTitles.setScreenTitle(requireActivity(), status, STEP_2_DIAGNOSTICS_SUMMARY);
        TextView tvTitle = view.findViewById(R.id.tv_sub_title);
        tvTitle.setText(title);

        //config viewmodel initialization
        DiagnosticsRepository repository = new DiagnosticsRepository(ConfigDatabase.getInstance(requireActivity()).patientDiagnosticsDao());
        DiagnosticsViewModelFactory factory = new DiagnosticsViewModelFactory(repository);
        DiagnosticsViewModel diagnosticsViewModel = new ViewModelProvider(this, factory).get(DiagnosticsViewModel.class);
        //requireActivity();
        diagnosticsViewModel.getAllEnabledLiveFields()
                .observe(requireActivity(), it -> {
                            mDiagnosticsList = it;
                            //Timber.tag(TAG).v(new Gson().toJson(mPatientVitalList));
                            updateUI();
                        }
                );
    }

    private void updateUI() {
        mBinding.llBloodGlucoseContainer.setVisibility(View.GONE);
        mBinding.llGlucoseRandomContainer.setVisibility(View.GONE);
        mBinding.llFastingGlucoseContainer.setVisibility(View.GONE);
        mBinding.llGlucosePostPrandialContainer.setVisibility(View.GONE);
        mBinding.llHemoglobinContainer.setVisibility(View.GONE);
        mBinding.llUricAcidContainer.setVisibility(View.GONE);
        mBinding.llCholesetrolContainer.setVisibility(View.GONE);
        Log.d(TAG, "updateUI: mDiagnosticsList : "+ new Gson().toJson(mDiagnosticsList));

        for (Diagnostics diagnostics : mDiagnosticsList) {
            CustomLog.v(TAG, diagnostics.getName() + "\t" + diagnostics.getDiagnosticsKey());

          /*  if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.BLOOD_GLUCOSE)) {
                mBinding.llBloodGlucoseContainer.setVisibility(View.VISIBLE);

            } else*/
            if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.RANDOM_BLOOD_SUGAR)) {
                mBinding.llGlucoseRandomContainer.setVisibility(View.VISIBLE);

            } else if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.FASTING_BLOOD_SUGAR)) {
                mBinding.llFastingGlucoseContainer.setVisibility(View.VISIBLE);
            } else if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.POST_PRANDIAL_BLOOD_SUGAR)) {
                mBinding.llGlucosePostPrandialContainer.setVisibility(View.VISIBLE);

            } else if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.HEAMOGLOBIN)) {
                mBinding.llHemoglobinContainer.setVisibility(View.VISIBLE);

            } else if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.URIC_ACID)) {
                mBinding.llUricAcidContainer.setVisibility(View.VISIBLE);
            } else if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.TOTAL_CHOLESTEROL)) {
                mBinding.llCholesetrolContainer.setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_diagnostics_collection_summary, container, false);
        if (diagnosticsModel != null) {
            if (diagnosticsModel.getBloodGlucoseNonFasting() != null && !diagnosticsModel.getBloodGlucoseNonFasting().isEmpty() && !diagnosticsModel.getBloodGlucoseRandom().equalsIgnoreCase("0"))
                mBinding.tvBloodGlucose.setText(diagnosticsModel.getBloodGlucoseNonFasting());
            else
                mBinding.tvBloodGlucose.setText(getString(R.string.ui2_no_information));

            if (diagnosticsModel.getBloodGlucoseRandom() != null && !diagnosticsModel.getBloodGlucoseRandom().isEmpty() && !diagnosticsModel.getBloodGlucoseRandom().equalsIgnoreCase("0"))
                mBinding.tvGlucoseRandom.setText(diagnosticsModel.getBloodGlucoseRandom());
            else
                mBinding.tvGlucoseRandom.setText(getString(R.string.ui2_no_information));

            if (diagnosticsModel.getBloodGlucoseFasting() != null && !diagnosticsModel.getBloodGlucoseFasting().isEmpty())
                mBinding.tvFastingGlucose.setText(diagnosticsModel.getBloodGlucoseFasting());
            else
                mBinding.tvFastingGlucose.setText(getString(R.string.ui2_no_information));

            if (diagnosticsModel.getBloodGlucosePostPrandial() != null && !diagnosticsModel.getBloodGlucosePostPrandial().isEmpty())
                mBinding.tvGlucosePostPrandial.setText(diagnosticsModel.getBloodGlucosePostPrandial());
            else
                mBinding.tvGlucosePostPrandial.setText(getString(R.string.ui2_no_information));
            if (diagnosticsModel.getHemoglobin() != null && !diagnosticsModel.getHemoglobin().isEmpty())
                mBinding.tvHemoglobin.setText(diagnosticsModel.getHemoglobin());
            else
                mBinding.tvHemoglobin.setText(getString(R.string.ui2_no_information));

            if (diagnosticsModel.getUricAcid() != null && !diagnosticsModel.getUricAcid().isEmpty())
                mBinding.tvUricAcid.setText(diagnosticsModel.getUricAcid());
            else
                mBinding.tvUricAcid.setText(getString(R.string.ui2_no_information));

            if (diagnosticsModel.getCholesterol() != null && !diagnosticsModel.getCholesterol().isEmpty())
                mBinding.tvCholesetrol.setText(diagnosticsModel.getCholesterol());
            else
                mBinding.tvCholesetrol.setText(getString(R.string.ui2_no_information));
        }
        mBinding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsEditMode && ((VisitCreationActivity) requireActivity()).isEditTriggerFromVisitSummary()) {
                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                } else {
                    mActionListener.onFormSubmitted(VisitCreationActivity.STEP_3_VISIT_REASON, mIsEditMode, diagnosticsModel);
                }
            }
        });
        mBinding.tvChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_2_DIAGNOSTICS, mIsEditMode, diagnosticsModel);
            }
        });
        mBinding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_2_DIAGNOSTICS, mIsEditMode, diagnosticsModel);
            }
        });
        mBinding.imgBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_2_DIAGNOSTICS, mIsEditMode, diagnosticsModel);
            }
        });
        mBinding.imbBtnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkConnection.isOnline(requireActivity())) {
                    syncNow(getActivity(), mBinding.imbBtnRefresh, syncAnimator);
                }
            }
        });
        return mBinding.getRoot();
    }

    private ObjectAnimator syncAnimator;
}