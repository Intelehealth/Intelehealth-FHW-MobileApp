package org.intelehealth.app.ayu.visit.vital;

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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.intelehealth.app.R;
import org.intelehealth.app.ayu.visit.VisitCreationActionListener;
import org.intelehealth.app.ayu.visit.VisitCreationActivity;
import org.intelehealth.app.models.VitalsObject;
import org.intelehealth.app.utilities.ConfigUtils;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_vital_collection_summary, container, false);
        if (mVitalsObject.getHeight() != null && !mVitalsObject.getHeight().isEmpty() && !mVitalsObject.getHeight().equalsIgnoreCase("0"))
            ((TextView) view.findViewById(R.id.tvHeightValueVitalsSummary)).setText(mVitalsObject.getHeight() + " " + getResources().getString(R.string.cm));
        else
            ((TextView) view.findViewById(R.id.tvHeightValueVitalsSummary)).setText(getString(R.string.ui2_no_information));

        if (mVitalsObject.getWeight() != null && !mVitalsObject.getWeight().isEmpty())
            ((TextView) view.findViewById(R.id.tvWeightValueVitalsSummary)).setText(mVitalsObject.getWeight() + " " + getResources().getString(R.string.kg));
        else
            ((TextView) view.findViewById(R.id.tvWeightValueVitalsSummary)).setText(getString(R.string.ui2_no_information));

        if (mVitalsObject.getBmi() != null && !mVitalsObject.getBmi().isEmpty())
            ((TextView) view.findViewById(R.id.tvBMIValueVitalsSummary)).setText(mVitalsObject.getBmi() + " " + getResources().getString(R.string.kg_m));
        else
            ((TextView) view.findViewById(R.id.tvBMIValueVitalsSummary)).setText(getString(R.string.ui2_no_information));


        if (mVitalsObject.getBpsys() != null && !mVitalsObject.getBpsys().isEmpty())
            ((TextView) view.findViewById(R.id.tvBPValueVitalsSummary)).setText(mVitalsObject.getBpsys() + "/" + mVitalsObject.getBpdia());
        else
            ((TextView) view.findViewById(R.id.tvBPValueVitalsSummary)).setText(getString(R.string.ui2_no_information));
        if (mVitalsObject.getPulse() != null && !mVitalsObject.getPulse().isEmpty())
            ((TextView) view.findViewById(R.id.tvPulseValueVitalsSummary)).setText(mVitalsObject.getPulse() + " " + getResources().getString(R.string.bpm));
        else
            ((TextView) view.findViewById(R.id.tvPulseValueVitalsSummary)).setText(getString(R.string.ui2_no_information));

        if (mVitalsObject.getTemperature() != null && !mVitalsObject.getTemperature().isEmpty()) {
            if (new ConfigUtils(getActivity()).fahrenheit()) {
                ((TextView) view.findViewById(R.id.tvTempValueVitalsSummary)).setText(convertCtoF(TAG, mVitalsObject.getTemperature()));
            } else {
                ((TextView) view.findViewById(R.id.tvTempValueVitalsSummary)).setText(mVitalsObject.getTemperature());
            }
        } else {

            ((TextView) view.findViewById(R.id.tvTempValueVitalsSummary)).setText(getString(R.string.ui2_no_information));
        }

        if (mVitalsObject.getSpo2() != null && !mVitalsObject.getSpo2().isEmpty())
            ((TextView) view.findViewById(R.id.tvSpO2ValueVitalsSummary)).setText(mVitalsObject.getSpo2() + " %");
        else
            ((TextView) view.findViewById(R.id.tvSpO2ValueVitalsSummary)).setText(getString(R.string.ui2_no_information));

        if (mVitalsObject.getResp() != null && !mVitalsObject.getResp().isEmpty())
            ((TextView) view.findViewById(R.id.tvRRValueVitalsSummary)).setText(mVitalsObject.getResp() + " " + getResources().getString(R.string.breaths_min));
        else
            ((TextView) view.findViewById(R.id.tvRRValueVitalsSummary)).setText(getString(R.string.ui2_no_information));

        view.findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsEditMode) {
                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                } else {
                    mActionListener.onFormSubmitted(VisitCreationActivity.STEP_2_VISIT_REASON, mIsEditMode, mVitalsObject);
                }
            }
        });
        view.findViewById(R.id.tvEditVitalsSummary).setOnClickListener(new View.OnClickListener() {
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
        view.findViewById(R.id.ibCancelVitalsSummary).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_1_VITAL, mIsEditMode, mVitalsObject);
            }
        });
        ImageButton refresh = view.findViewById(R.id.ibRefreshVitalsSummary);
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
}