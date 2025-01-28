package org.intelehealth.app.ayu.visit.diagnostics;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwnerKt;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.ayu.visit.VisitCreationActionListener;
import org.intelehealth.app.ayu.visit.VisitCreationActivity;
import org.intelehealth.app.ayu.visit.model.CommonVisitData;
import org.intelehealth.app.ayu.visit.vital.CoroutineProvider;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.databinding.FragmentDiagnosticsCollectionBinding;
import org.intelehealth.app.models.DiagnosticsModel;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.utilities.ConfigUtils;
import org.intelehealth.app.utilities.CustomLog;
import org.intelehealth.app.utilities.DecimalDigitsInputFilter;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.config.presenter.fields.data.DiagnosticsRepository;
import org.intelehealth.config.presenter.fields.factory.DiagnosticsViewModelFactory;
import org.intelehealth.config.presenter.fields.viewmodel.DiagnosticsViewModel;
import org.intelehealth.config.room.ConfigDatabase;
import org.intelehealth.config.room.entity.Diagnostics;
import org.intelehealth.config.room.entity.PatientVital;
import org.intelehealth.config.utility.PatientDiagnosticsConfigKeys;

import java.util.List;

public class DiagnosticsCollectionFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = DiagnosticsCollectionFragment.class.getSimpleName();
    private VisitCreationActionListener mActionListener;
    private String patientName = "";
    private String patientGender = "";
    private String intentTag;
    private String state;
    private String patientUuid;
    private String visitUuid;
    private String encounterVitals;
    private String encounterAdultIntials = "", EncounterAdultInitial_LatestVisit = "";
    private SessionManager sessionManager;
    private ConfigUtils configUtils;
    private DiagnosticsModel results = new DiagnosticsModel();
    private boolean mIsEditMode = false;
    private List<Diagnostics> mPatientDiagnosticsList;
    private FragmentDiagnosticsCollectionBinding mBinding;

    public DiagnosticsCollectionFragment() {
    }


    public static DiagnosticsCollectionFragment newInstance(CommonVisitData commonVisitData, boolean isEditMode, DiagnosticsModel diagnosticsModel) {
        DiagnosticsCollectionFragment fragment = new DiagnosticsCollectionFragment();


        fragment.mIsEditMode = isEditMode;
        fragment.results = diagnosticsModel;

        fragment.patientUuid = commonVisitData.getPatientUuid();//intent.getStringExtra("patientUuid");
        fragment.visitUuid = commonVisitData.getVisitUuid(); // intent.getStringExtra("visitUuid");
        fragment.encounterVitals = commonVisitData.getEncounterUuidVitals();//intent.getStringExtra("encounterUuidVitals");
        fragment.encounterAdultIntials = commonVisitData.getEncounterUuidAdultIntial();//intent.getStringExtra("encounterUuidAdultIntial");
        fragment.EncounterAdultInitial_LatestVisit = commonVisitData.getEncounterAdultInitialLatestVisit();//intent.getStringExtra("EncounterAdultInitial_LatestVisit");
        fragment.state = commonVisitData.getState();//intent.getStringExtra("state");
        fragment.patientName = commonVisitData.getPatientName();//intent.getStringExtra("name");
        fragment.patientGender = commonVisitData.getPatientGender();//intent.getStringExtra("gender");
        fragment.intentTag = commonVisitData.getIntentTag();//intent.getStringExtra("tag");
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActionListener = (VisitCreationActionListener) context;
        sessionManager = new SessionManager(context);
        configUtils = new ConfigUtils(context);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_diagnostics_collection, container, false);
        // mBinding.etvPostPrandial.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(3, 0)});

        mBinding.tvGlucoseRandomError.setVisibility(View.GONE);
        mBinding.tvGlucoseFastingError.setVisibility(View.GONE);
        //mBinding.tvNonFastingGlucoseError.setVisibility(View.GONE);
        mBinding.etvPostPrandialError.setVisibility(View.GONE);
        mBinding.etvUricAcidError.setVisibility(View.GONE);
        mBinding.etvCholestrolError.setVisibility(View.GONE);
        mBinding.tvHemoglobinError.setVisibility(View.GONE);

        //mBinding.etvNonFastingGlucose.addTextChangedListener(new DiagnosticsCollectionFragment.MyTextWatcher(mBinding.etvNonFastingGlucose));
        mBinding.etvGlucoseRandom.addTextChangedListener(new DiagnosticsCollectionFragment.MyTextWatcher(mBinding.etvGlucoseRandom));
        mBinding.etvGlucoseFasting.addTextChangedListener(new DiagnosticsCollectionFragment.MyTextWatcher(mBinding.etvGlucoseFasting));
        mBinding.etvPostPrandial.addTextChangedListener(new DiagnosticsCollectionFragment.MyTextWatcher(mBinding.etvPostPrandial));
        mBinding.etvHemoglobin.addTextChangedListener(new DiagnosticsCollectionFragment.MyTextWatcher(mBinding.etvHemoglobin));
        mBinding.etvUricAcid.addTextChangedListener(new DiagnosticsCollectionFragment.MyTextWatcher(mBinding.etvUricAcid));
        mBinding.etvCholesterol.addTextChangedListener(new DiagnosticsCollectionFragment.MyTextWatcher(mBinding.etvCholesterol));

        mBinding.btnSubmit.setOnClickListener(this);
        mBinding.btnSubmit.setClickable(true);
        mBinding.btnCancel.setOnClickListener(this);
        mBinding.btnCancel.setClickable(true);

        if (mIsEditMode && results == null) {
            loadSavedDateForEditFromDB();
        }

        return mBinding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        manageBackButtonVisibility();

        //config viewmodel initialization
        DiagnosticsRepository repository = new DiagnosticsRepository(ConfigDatabase.getInstance(requireActivity()).patientDiagnosticsDao());
        DiagnosticsViewModelFactory factory = new DiagnosticsViewModelFactory(repository);
        DiagnosticsViewModel diagnosticsViewModel = new ViewModelProvider(this, factory).get(DiagnosticsViewModel.class);
        //requireActivity();
       /* diagnosticsViewModel.getAllEnabledLiveFields()
                .observe(requireActivity(), it -> {
                            mPatientDiagnosticsList = it;
                            updateUI();
                        }
                );*/
        CoroutineProvider.usePatientDiagnosticsScope(
                LifecycleOwnerKt.getLifecycleScope(this),
                diagnosticsViewModel,
                data -> {
                    mPatientDiagnosticsList = (List<Diagnostics>) data;
                    updateUI();
                }
        );
    }

    private void updateUI() {
        //resetAllFields();
        mBinding.llGlucoseRandomContainer.setVisibility(View.GONE);
        mBinding.llGlusoseFastingContainer.setVisibility(View.GONE);
        //mBinding.tvNonFastingGlucoseError.setVisibility(View.GONE);
        mBinding.llPostPrandialContainer.setVisibility(View.GONE);
        mBinding.llHemoglobinContainer.setVisibility(View.GONE);
        mBinding.llUricAcidContainer.setVisibility(View.GONE);
        mBinding.llCholestrolContainer.setVisibility(View.GONE);

        for (Diagnostics diagnostics : mPatientDiagnosticsList) {
            CustomLog.v(TAG, diagnostics.getName() + "\t" + diagnostics.getDiagnosticsKey());

            if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.RANDOM_BLOOD_SUGAR)) {
                mBinding.llGlucoseRandomContainer.setVisibility(View.VISIBLE);
                mBinding.llGlucoseRandomContainer.setTag(diagnostics);
                appendMandatorySing(diagnostics.isMandatory(), mBinding.tvGlucoseRandomLbl);
            } else if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.FASTING_BLOOD_SUGAR)) {
                mBinding.llGlusoseFastingContainer.setVisibility(View.VISIBLE);
                mBinding.llGlusoseFastingContainer.setTag(diagnostics);
                appendMandatorySing(diagnostics.isMandatory(), mBinding.tvGlusoseFastingLbl);
            }/* else if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.BLOOD_GLUCOSE)) {
                mBinding.llNonFastingContainer.setVisibility(View.VISIBLE);
                mBinding.llNonFastingContainer.setTag(diagnostics);
                appendMandatorySing(diagnostics.isMandatory(), mBinding.tvNonFastingLbl);
            }*/ else if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.POST_PRANDIAL_BLOOD_SUGAR)) {
                mBinding.llPostPrandialContainer.setVisibility(View.VISIBLE);
                mBinding.llPostPrandialContainer.setTag(diagnostics);
                appendMandatorySing(diagnostics.isMandatory(), mBinding.tvPostPrandialLbl);
            } else if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.HEAMOGLOBIN)) {
                mBinding.llHemoglobinContainer.setVisibility(View.VISIBLE);
                mBinding.llHemoglobinContainer.setTag(diagnostics);
                appendMandatorySing(diagnostics.isMandatory(), mBinding.tvHemoglobinLbl);
            } else if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.URIC_ACID)) {
                mBinding.llUricAcidContainer.setVisibility(View.VISIBLE);
                mBinding.llUricAcidContainer.setTag(diagnostics);
                appendMandatorySing(diagnostics.isMandatory(), mBinding.tvUricAcidLbl);
            } else if (diagnostics.getDiagnosticsKey().equals(PatientDiagnosticsConfigKeys.TOTAL_CHOLESTEROL)) {
                mBinding.llCholestrolContainer.setVisibility(View.VISIBLE);
                mBinding.llCholestrolContainer.setTag(diagnostics);
                appendMandatorySing(diagnostics.isMandatory(), mBinding.tvCholestrolLbl);
            }
        }
    }

    private void appendMandatorySing(boolean isMandatory, TextView textView) {
        if (isMandatory) {
            textView.append("*");
        }
    }


    class MyTextWatcher implements TextWatcher {
        EditText editText;

        MyTextWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            String val = editable.toString().trim();
            if (val.equals(".")) {
                editText.setText("");
                return;
            }
            boolean isValid = isValidForm();
            setDisabledSubmit(!isValid);
        }
    }

    private boolean isValidForm() {
        boolean isValid = true;

        // Utility method to validate each field
        isValid &= validateField(
                mBinding.etvGlucoseRandom.getText().toString().trim(),
                (Diagnostics) mBinding.llGlucoseRandomContainer.getTag(),
                mBinding.tvGlucoseRandomError,
                mBinding.etvGlucoseRandom,
                R.string.error_field_required,
                R.string.glucose_random_error,
                AppConstants.MINIMUM_GLUCOSE_RANDOM,
                AppConstants.MAXIMUM_GLUCOSE_RANDOM
        );

        isValid &= validateField(
                mBinding.etvGlucoseFasting.getText().toString().trim(),
                (Diagnostics) mBinding.llGlusoseFastingContainer.getTag(),
                mBinding.tvGlucoseFastingError,
                mBinding.etvGlucoseFasting,
                R.string.error_field_required,
                R.string.glucose_fasting_error,
                AppConstants.MINIMUM_GLUCOSE_FASTING,
                AppConstants.MAXIMUM_GLUCOSE_FASTING
        );

        isValid &= validateField(
                mBinding.etvPostPrandial.getText().toString().trim(),
                (Diagnostics) mBinding.llPostPrandialContainer.getTag(),
                mBinding.etvPostPrandialError,
                mBinding.etvPostPrandial,
                R.string.error_field_required,
                R.string.post_prandial_error,
                AppConstants.MINIMUM_GLUCOSE_POST_PRANDIAL,
                AppConstants.MAXIMUM_GLUCOSE_POST_PRANDIAL
        );

        isValid &= validateField(
                mBinding.etvHemoglobin.getText().toString().trim(),
                (Diagnostics) mBinding.llHemoglobinContainer.getTag(),
                mBinding.tvHemoglobinError,
                mBinding.etvHemoglobin,
                R.string.error_field_required,
                R.string.hemoglobin_error,
                AppConstants.MINIMUM_HEMOGLOBIN,
                AppConstants.MAXIMUM_HEMOGLOBIN
        );

        isValid &= validateField(
                mBinding.etvUricAcid.getText().toString().trim(),
                (Diagnostics) mBinding.llUricAcidContainer.getTag(),
                mBinding.etvUricAcidError,
                mBinding.etvUricAcid,
                R.string.error_field_required,
                R.string.uric_acid_error,
                AppConstants.MINIMUM_URIC_ACID,
                AppConstants.MAXIMUM_URIC_ACID
        );

        isValid &= validateField(
                mBinding.etvCholesterol.getText().toString().trim(),
                (Diagnostics) mBinding.llCholestrolContainer.getTag(),
                mBinding.etvCholestrolError,
                mBinding.etvCholesterol,
                R.string.error_field_required,
                R.string.cholestrol_acid_error,
                AppConstants.MINIMUM_TOTAL_CHOLSTEROL,
                AppConstants.MAXIMUM_TOTAL_CHOLSTEROL
        );

        return isValid;
    }

    private boolean validateField(String value, Diagnostics diagnosticsTag, TextView errorTextView, EditText editText, int mandatoryErrorRes, int rangeErrorRes, String minValue, String maxValue) {
        if (diagnosticsTag != null && diagnosticsTag.isMandatory() && value.isEmpty()) {
            errorTextView.setText(getString(mandatoryErrorRes));
            errorTextView.setVisibility(View.VISIBLE);
            editText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            return false;
        }

        if (!value.isEmpty()) {
            double numericValue = Double.parseDouble(value);
            double min = Double.parseDouble(minValue);
            double max = Double.parseDouble(maxValue);

            if (numericValue < min || numericValue > max) {
                errorTextView.setText(getString(rangeErrorRes, minValue, maxValue));
                errorTextView.setVisibility(View.VISIBLE);
                editText.requestFocus();
                editText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                return false;
            }
        }

        // Clear any previous errors
        errorTextView.setVisibility(View.GONE);
        editText.setBackgroundResource(R.drawable.bg_input_fieldnew);
        return true;
    }

    private void setDisabledSubmit(boolean disableNow) {
        if (disableNow) {
            mBinding.btnSubmit.setClickable(false);
            mBinding.btnSubmit.setEnabled(false);
//            mSubmitButton.setBackgroundResource(R.drawable.ui2_common_primary_bg_disabled_1);
        } else {
            mBinding.btnSubmit.setClickable(true);
            mBinding.btnSubmit.setEnabled(true);
//            mSubmitButton.setBackgroundResource(R.drawable.ui2_common_primary_bg);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_submit) {
            mBinding.btnSubmit.setClickable(false);
            boolean isValid = isValidForm();
            Log.d(TAG, "onClick: btn_submit clicked- " + isValid);//validate

            if (isValid) {
                isDataReadyForSaving();
                mActionListener.onProgress(100);
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_2_DIAGNOSTICS_SUMMARY, mIsEditMode, results);
            }
            setDisabledSubmit(!isValid);
        }
        if (view.getId() == R.id.btn_cancel) {
            mActionListener.onFormSubmitted(VisitCreationActivity.STEP_1_VITAL_SUMMARY, false, null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }

    private void initData() {
        // set existing data
        if (results != null) {
            if (results.getBloodGlucoseRandom() != null && !results.getBloodGlucoseRandom().isEmpty())
                mBinding.etvGlucoseRandom.setText(results.getBloodGlucoseRandom());

            if (results.getBloodGlucoseFasting() != null && !results.getBloodGlucoseFasting().isEmpty())
                mBinding.etvGlucoseFasting.setText(results.getBloodGlucoseFasting());

//            if (results.getBloodGlucoseNonFasting() != null && !results.getBloodGlucoseNonFasting().isEmpty())
//                mBinding.etvNonFastingGlucose.setText(results.getBloodGlucoseNonFasting());

            if (results.getBloodGlucosePostPrandial() != null && !results.getBloodGlucosePostPrandial().isEmpty())
                mBinding.etvPostPrandial.setText(results.getBloodGlucosePostPrandial());

            if (results.getHemoglobin() != null && !results.getHemoglobin().isEmpty())
                mBinding.etvHemoglobin.setText(results.getHemoglobin());

            if (results.getUricAcid() != null && !results.getUricAcid().isEmpty())
                mBinding.etvUricAcid.setText(results.getUricAcid());

            if (results.getCholesterol() != null && !results.getCholesterol().isEmpty())
                mBinding.etvCholesterol.setText(results.getCholesterol());


        }
    }

    public void loadSavedDateForEditFromDB() {

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        String[] columns = {"value", " conceptuuid"};
        String visitSelection = "encounteruuid = ? and voided!='1'";
        String[] visitArgs = {encounterVitals};
        Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, null);
        if (visitCursor.moveToFirst()) {
            do {
                String dbConceptID = visitCursor.getString(visitCursor.getColumnIndex("conceptuuid"));
                String dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
                parseData(dbConceptID, dbValue);
            } while (visitCursor.moveToNext());
        }
        visitCursor.close();
    }

    private void parseData(String concept_id, String value) {
        switch (concept_id) {
            case UuidDictionary.BLOOD_GLUCOSE_RANDOM:
                if (value != null && !value.isEmpty())
                    mBinding.etvGlucoseRandom.setText(value);
                break;
            case UuidDictionary.BLOOD_GLUCOSE_FASTING: //Pulse
                if (value != null && !value.isEmpty())
                    mBinding.etvGlucoseFasting.setText(value);
                break;
           /* case UuidDictionary.BLOOD_GLUCOSE: //Pulse
                if (value != null && !value.isEmpty())
                    mBinding.etvNonFastingGlucose.setText(value);
                break;*/
            case UuidDictionary.BLOOD_GLUCOSE_POST_PRANDIAL: //Pulse
                if (value != null && !value.isEmpty())
                    mBinding.etvPostPrandial.setText(value);
                break;
            case UuidDictionary.HEMOGLOBIN: //Respiratory
                if (value != null && !value.isEmpty())
                    mBinding.etvHemoglobin.setText(value);
                break;
            case UuidDictionary.URIC_ACID: //Respiratory
                if (value != null && !value.isEmpty())
                    mBinding.etvUricAcid.setText(value);
                break;
            case UuidDictionary.TOTAL_CHOLESTEROL: //Respiratory
                if (value != null && !value.isEmpty())
                    mBinding.etvCholesterol.setText(value);
                break;
            default:
                break;

        }
    }

    public boolean isDataReadyForSaving() {
        try {
            if (results == null) {
                results = new DiagnosticsModel();
            }

            results.setBloodGlucoseRandom((mBinding.etvGlucoseRandom.getText().toString()));
            results.setBloodGlucoseFasting((mBinding.etvGlucoseFasting.getText().toString()));
            //results.setBloodGlucoseNonFasting((mBinding.etvNonFastingGlucose.getText().toString()));
            results.setBloodGlucosePostPrandial((mBinding.etvPostPrandial.getText().toString()));
            results.setHemoglobin((mBinding.etvHemoglobin.getText().toString()));
            results.setUricAcid((mBinding.etvUricAcid.getText().toString()));
            results.setCholesterol((mBinding.etvCholesterol.getText().toString()));

        } catch (NumberFormatException e) {
            //Snackbar.make(findViewById(R.id.cl_table), R.string.error_non_decimal_no_added, Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }

//


        ObsDAO obsDAO = new ObsDAO();
        ObsDTO obsDTO = new ObsDTO();
        if (getActivity().getIntent().equals("edit")) {
            ObsDAO.deleteExistingDiagnosticsDataIfExists(visitUuid);

            try {
                Diagnostics diagnostics = (Diagnostics) mBinding.llGlucoseRandomContainer.getTag();
                if ((diagnostics != null && diagnostics.isMandatory()) || !results.getBloodGlucoseRandom().isEmpty()) {
                    obsDTO = new ObsDTO();
                    obsDTO.setConceptuuid(UuidDictionary.BLOOD_GLUCOSE_RANDOM);
                    obsDTO.setEncounteruuid(encounterVitals);
                    obsDTO.setCreator(sessionManager.getCreatorID());
                    obsDTO.setValue(results.getBloodGlucoseRandom());
                    //obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.SPO2));
                    obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, diagnostics.getUuid()));
                    obsDTO.setConceptsetuuid(UuidDictionary.OBS_TYPE_DIAGNOSTICS_SET);

                    obsDAO.updateObs(obsDTO);
                }

                diagnostics = (Diagnostics) mBinding.llGlusoseFastingContainer.getTag();
                if ((diagnostics != null && diagnostics.isMandatory()) || !results.getBloodGlucoseFasting().isEmpty()) {
                    obsDTO = new ObsDTO();
                    obsDTO.setConceptuuid(UuidDictionary.BLOOD_GLUCOSE_FASTING);
                    obsDTO.setEncounteruuid(encounterVitals);
                    obsDTO.setCreator(sessionManager.getCreatorID());
                    obsDTO.setValue(results.getBloodGlucoseFasting());
                    //obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.PULSE));
                    obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, diagnostics.getUuid()));
                    obsDTO.setConceptsetuuid(UuidDictionary.OBS_TYPE_DIAGNOSTICS_SET);

                    obsDAO.updateObs(obsDTO);
                }

                diagnostics = (Diagnostics) mBinding.llPostPrandialContainer.getTag();
                if ((diagnostics != null && diagnostics.isMandatory()) || !results.getBloodGlucosePostPrandial().isEmpty()) {
                    obsDTO = new ObsDTO();
                    obsDTO.setConceptuuid(UuidDictionary.BLOOD_GLUCOSE_POST_PRANDIAL);
                    obsDTO.setEncounteruuid(encounterVitals);
                    obsDTO.setCreator(sessionManager.getCreatorID());
                    obsDTO.setValue(results.getBloodGlucosePostPrandial());
                    //obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.TEMPERATURE));
                    obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, diagnostics.getUuid()));
                    obsDTO.setConceptsetuuid(UuidDictionary.OBS_TYPE_DIAGNOSTICS_SET);

                    obsDAO.updateObs(obsDTO);
                }

              /*  diagnostics = (Diagnostics) mBinding.llNonFastingContainer.getTag();
                if ((diagnostics != null && diagnostics.isMandatory()) || !results.getBloodGlucoseNonFasting().isEmpty()) {
                    obsDTO = new ObsDTO();
                    obsDTO.setConceptuuid(UuidDictionary.BLOOD_GLUCOSE);
                    obsDTO.setEncounteruuid(encounterVitals);
                    obsDTO.setCreator(sessionManager.getCreatorID());
                    obsDTO.setValue(results.getBloodGlucoseNonFasting());
                    //obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.RESPIRATORY));
                    obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, diagnostics.getUuid()));
                    
                    obsDAO.updateObs(obsDTO);
                }*/

                diagnostics = (Diagnostics) mBinding.llUricAcidContainer.getTag();
                if ((diagnostics != null && diagnostics.isMandatory()) || !results.getUricAcid().isEmpty()) {
                    obsDTO = new ObsDTO();
                    obsDTO.setConceptuuid(UuidDictionary.URIC_ACID);
                    obsDTO.setEncounteruuid(encounterVitals);
                    obsDTO.setCreator(sessionManager.getCreatorID());
                    obsDTO.setValue(results.getUricAcid());
                    //obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.RESPIRATORY));
                    obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, diagnostics.getUuid()));
                    obsDTO.setConceptsetuuid(UuidDictionary.OBS_TYPE_DIAGNOSTICS_SET);

                    obsDAO.updateObs(obsDTO);
                }

                diagnostics = (Diagnostics) mBinding.llCholestrolContainer.getTag();
                if ((diagnostics != null && diagnostics.isMandatory()) || !results.getCholesterol().isEmpty()) {
                    obsDTO = new ObsDTO();
                    obsDTO.setConceptuuid(UuidDictionary.TOTAL_CHOLESTEROL);
                    obsDTO.setEncounteruuid(encounterVitals);
                    obsDTO.setCreator(sessionManager.getCreatorID());
                    obsDTO.setValue(results.getCholesterol());
                    //obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.RESPIRATORY));
                    obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, diagnostics.getUuid()));
                    obsDTO.setConceptsetuuid(UuidDictionary.OBS_TYPE_DIAGNOSTICS_SET);

                    obsDAO.updateObs(obsDTO);
                }

                diagnostics = (Diagnostics) mBinding.llHemoglobinContainer.getTag();
                if ((diagnostics != null && diagnostics.isMandatory()) || !results.getHemoglobin().isEmpty()) {
                    obsDTO = new ObsDTO();
                    obsDTO.setConceptuuid(UuidDictionary.HEMOGLOBIN);
                    obsDTO.setEncounteruuid(encounterVitals);
                    obsDTO.setCreator(sessionManager.getCreatorID());
                    obsDTO.setValue(results.getHemoglobin());
                    //obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.RESPIRATORY));
                    obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, diagnostics.getUuid()));
                    obsDTO.setConceptsetuuid(UuidDictionary.OBS_TYPE_DIAGNOSTICS_SET);

                    obsDAO.updateObs(obsDTO);
                }
                //making flag to false in the encounter table so it will sync again
                EncounterDAO encounterDAO = new EncounterDAO();
                try {
                    encounterDAO.updateEncounterSync("false", encounterVitals);
                    encounterDAO.updateEncounterModifiedDate(encounterVitals);
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }

            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                e.printStackTrace();
                Log.d(TAG, "isDataReadyForSaving: diagnostics exec in: " + e.getLocalizedMessage());
            }
        } else {
            try {
                ObsDAO.deleteExistingDiagnosticsDataIfExists(visitUuid);

                Diagnostics diagnostics = (Diagnostics) mBinding.llGlucoseRandomContainer.getTag();
                if (diagnostics != null && !results.getBloodGlucoseRandom().isEmpty()) {
                    obsDTO = new ObsDTO();
                    //obsDTO.setConceptuuid(UuidDictionary.PULSE);
                    obsDTO.setConceptuuid(diagnostics.getUuid());
                    obsDTO.setEncounteruuid(encounterVitals);
                    obsDTO.setCreator(sessionManager.getCreatorID());
                    obsDTO.setValue(results.getBloodGlucoseRandom());
                    obsDTO.setConceptsetuuid(UuidDictionary.OBS_TYPE_DIAGNOSTICS_SET);

                    try {
                        obsDAO.insertObs(obsDTO);
                    } catch (DAOException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }
                }

                diagnostics = (Diagnostics) mBinding.llGlusoseFastingContainer.getTag();

                if (diagnostics != null && !results.getBloodGlucoseFasting().isEmpty()) {
                    obsDTO = new ObsDTO();
                    //obsDTO.setConceptuuid(UuidDictionary.TEMPERATURE);
                    obsDTO.setConceptuuid(diagnostics.getUuid());
                    obsDTO.setEncounteruuid(encounterVitals);
                    obsDTO.setCreator(sessionManager.getCreatorID());
                    obsDTO.setValue(results.getBloodGlucoseFasting());
                    obsDTO.setConceptsetuuid(UuidDictionary.OBS_TYPE_DIAGNOSTICS_SET);

                    try {
                        obsDAO.insertObs(obsDTO);
                    } catch (DAOException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }
                }

                /*diagnostics = (Diagnostics) mBinding.llNonFastingContainer.getTag();
                Log.d(TAG, "isDataReadyForSaving: kz NonFasting : "+results.getBloodGlucoseNonFasting());
                Log.d(TAG, "isDataReadyForSaving: diagnostics : "+diagnostics);

                if (diagnostics != null && !results.getBloodGlucoseNonFasting().isEmpty()) {
                    obsDTO = new ObsDTO();
                    //obsDTO.setConceptuuid(UuidDictionary.RESPIRATORY);
                    obsDTO.setConceptuuid(diagnostics.getUuid());
                    obsDTO.setEncounteruuid(encounterVitals);
                    obsDTO.setCreator(sessionManager.getCreatorID());
                    obsDTO.setValue(results.getBloodGlucoseNonFasting());
                    
                    Log.d(TAG, "isDataReadyForSaving: NonFasting : " + obsDTO);
                    try {
                        obsDAO.insertObs(obsDTO);
                    } catch (DAOException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }
                }*/
                diagnostics = (Diagnostics) mBinding.llPostPrandialContainer.getTag();

                if (diagnostics != null && !results.getBloodGlucosePostPrandial().isEmpty()) {
                    obsDTO = new ObsDTO();
                    //obsDTO.setConceptuuid(UuidDictionary.TEMPERATURE);
                    obsDTO.setConceptuuid(diagnostics.getUuid());
                    obsDTO.setEncounteruuid(encounterVitals);
                    obsDTO.setCreator(sessionManager.getCreatorID());
                    obsDTO.setValue(results.getBloodGlucosePostPrandial());
                    obsDTO.setConceptsetuuid(UuidDictionary.OBS_TYPE_DIAGNOSTICS_SET);

                    try {
                        obsDAO.insertObs(obsDTO);
                    } catch (DAOException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }
                }
                diagnostics = (Diagnostics) mBinding.llHemoglobinContainer.getTag();

                if (diagnostics != null && !results.getHemoglobin().isEmpty()) {
                    obsDTO = new ObsDTO();
                    //obsDTO.setConceptuuid(UuidDictionary.SPO2);
                    obsDTO.setConceptuuid(diagnostics.getUuid());
                    obsDTO.setEncounteruuid(encounterVitals);
                    obsDTO.setCreator(sessionManager.getCreatorID());
                    obsDTO.setValue(results.getHemoglobin());
                    obsDTO.setConceptsetuuid(UuidDictionary.OBS_TYPE_DIAGNOSTICS_SET);

                    try {
                        obsDAO.insertObs(obsDTO);
                    } catch (DAOException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }
                }
                diagnostics = (Diagnostics) mBinding.llCholestrolContainer.getTag();
                if (diagnostics != null && !results.getCholesterol().isEmpty()) {
                    obsDTO = new ObsDTO();
                    //obsDTO.setConceptuuid(UuidDictionary.SPO2);
                    obsDTO.setConceptuuid(diagnostics.getUuid());
                    obsDTO.setEncounteruuid(encounterVitals);
                    obsDTO.setCreator(sessionManager.getCreatorID());
                    obsDTO.setValue(results.getCholesterol());
                    obsDTO.setConceptsetuuid(UuidDictionary.OBS_TYPE_DIAGNOSTICS_SET);

                    try {
                        obsDAO.insertObs(obsDTO);
                    } catch (DAOException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }
                }
                diagnostics = (Diagnostics) mBinding.llUricAcidContainer.getTag();
                if (diagnostics != null && !results.getUricAcid().isEmpty()) {
                    obsDTO = new ObsDTO();
                    //obsDTO.setConceptuuid(UuidDictionary.SPO2);
                    obsDTO.setConceptuuid(diagnostics.getUuid());
                    obsDTO.setEncounteruuid(encounterVitals);
                    obsDTO.setCreator(sessionManager.getCreatorID());
                    obsDTO.setValue(results.getUricAcid());
                    obsDTO.setConceptsetuuid(UuidDictionary.OBS_TYPE_DIAGNOSTICS_SET);

                    try {
                        obsDAO.insertObs(obsDTO);
                    } catch (DAOException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private void manageBackButtonVisibility() {
        boolean vitalsActiveStatus = ((VisitCreationActivity) requireActivity()).getFeatureActiveStatus().getVitalSection();
        mBinding.btnCancel.setVisibility(vitalsActiveStatus ? View.VISIBLE : View.GONE);
        if (mBinding.btnCancel.getVisibility() == View.GONE) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mBinding.btnSubmit.getLayoutParams();
            params.width = LinearLayout.LayoutParams.MATCH_PARENT;
            params.weight = 0f;
            params.setMargins(0, params.topMargin, params.rightMargin, params.bottomMargin);
            mBinding.btnSubmit.setLayoutParams(params);
        } else {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mBinding.btnSubmit.getLayoutParams();
            params.width = 0;
            params.weight = 1f;
            params.setMargins(16, params.topMargin, params.rightMargin, params.bottomMargin);
            mBinding.btnSubmit.setLayoutParams(params);
        }
    }
    private void resetAllFields() {


        mBinding.tvGlucoseRandomLbl.setText(getString(R.string.blood_glucose_random));
        mBinding.tvGlusoseFastingLbl.setText(getString(R.string.blood_glucose_fasting));
        mBinding.tvPostPrandialLbl.setText(getString(R.string.blood_glucose_post_prandial));
        mBinding.tvHemoglobinLbl.setText(getString(R.string.haemoglobin));
        mBinding.tvUricAcidLbl.setText(getString(R.string.uric_acid));
        mBinding.tvCholestrolLbl.setText(getString(R.string.total_cholestrol));

        mBinding.etvGlucoseRandom.setBackgroundResource(R.drawable.bg_input_fieldnew);
        mBinding.etvGlucoseFasting.setBackgroundResource(R.drawable.bg_input_fieldnew);
        mBinding.etvPostPrandial.setBackgroundResource(R.drawable.bg_input_fieldnew);
        mBinding.etvHemoglobin.setBackgroundResource(R.drawable.bg_input_fieldnew);
        mBinding.etvUricAcid.setBackgroundResource(R.drawable.bg_input_fieldnew);
        mBinding.etvCholesterol.setBackgroundResource(R.drawable.bg_input_fieldnew);

        mBinding.tvGlucoseRandomError.setVisibility(View.GONE);
        mBinding.tvGlucoseFastingError.setVisibility(View.GONE);
        mBinding.etvPostPrandialError.setVisibility(View.GONE);
        mBinding.tvHemoglobinError.setVisibility(View.GONE);
        mBinding.etvCholestrolError.setVisibility(View.GONE);
        mBinding.etvUricAcidError.setVisibility(View.GONE);

    }
}