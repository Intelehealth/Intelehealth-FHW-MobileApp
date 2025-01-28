package org.intelehealth.app.ayu.visit.vital;

import static org.intelehealth.app.ayu.visit.common.VisitUtils.convertCtoF;
import static org.intelehealth.app.ayu.visit.common.VisitUtils.convertFtoC;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;

import org.intelehealth.app.utilities.CustomLog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwnerKt;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codeglo.coyamore.data.PreferenceHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.adapter.DialogSimpleListAdapter;
import org.intelehealth.app.adapter.SimpleItemData;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.ayu.visit.VisitCreationActionListener;
import org.intelehealth.app.ayu.visit.VisitCreationActivity;
import org.intelehealth.app.ayu.visit.common.VisitUtils;
import org.intelehealth.app.ayu.visit.model.BMIStatus;
import org.intelehealth.app.ayu.visit.model.CommonVisitData;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.models.VitalsObject;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.utilities.ConfigUtils;
import org.intelehealth.app.utilities.CustomLog;
import org.intelehealth.app.utilities.DecimalDigitsInputFilter;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.config.presenter.fields.data.PatientVitalRepository;
import org.intelehealth.config.presenter.fields.factory.PatientVitalViewModelFactory;
import org.intelehealth.config.presenter.fields.viewmodel.PatientVitalViewModel;
import org.intelehealth.config.room.ConfigDatabase;
import org.intelehealth.config.room.entity.PatientVital;
import org.intelehealth.config.utility.PatientVitalConfigKeys;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

public class VitalCollectionFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = VitalCollectionFragment.class.getSimpleName();
    private VisitCreationActionListener mActionListener;
    private String patientName = "";
    private String patientGender = "";
    private String intentTag;
    private String state;
    private String patientUuid;
    private String visitUuid;
    private String encounterVitals;
    private float float_ageYear_Month;
    private int mAgeInMonth;
    private String encounterAdultIntials = "", EncounterAdultInitial_LatestVisit = "";
    //private Spinner mHeightSpinner, mWeightSpinner;
    private EditText mHeightEditText, mWeightEditText;
    private TextView mBMITextView, mBmiStatusTextView;
    //private LinearLayout mBMILinearLayout;
    TextView mHeightErrorTextView, mWeightErrorTextView, mPulseErrorTextView, mSpo2ErrorTextView, mRespErrorTextView, mBpSysErrorTextView, mBpDiaErrorTextView, mTemperatureErrorTextView, mBloodGroupErrorTextView;
    EditText mPulseEditText, mBpSysEditText, mBpDiaEditText, mTemperatureEditText, mSpo2EditText, mRespEditText;
    private Button mSubmitButton;

    private String heightvalue = "";
    private String weightvalue = "";
    private int flag_height = 0, flag_weight = 0;
    SessionManager sessionManager;
    ConfigUtils configUtils;
    VitalsObject results = new VitalsObject();

    private List<Integer> mHeightMasterList = new ArrayList<>();
    private List<Integer> mWeightMasterList = new ArrayList<>();
    private boolean mIsEditMode = false;
    private TextView mBloodGroupTextView;
    private AlertDialog mBloodGroupAlertDialog;
    private View mRootView;
    private CardView mHeightCardView, mWeightCardView, mBMICardView, mSBPCardView, mDBPCardView, mPulseCardView, mTemperatureCardView, mSpo2CardView, mRespiratoryCardView, mBloodGroupCardView;

    private List<PatientVital> mPatientVitalList;
    private VitalPreference vitalPref;

    public VitalCollectionFragment() {
        // Required empty public constructor
    }


    public static VitalCollectionFragment newInstance(CommonVisitData commonVisitData, boolean isEditMode, VitalsObject vitalsObject) {
        VitalCollectionFragment fragment = new VitalCollectionFragment();


        fragment.mIsEditMode = isEditMode;
        fragment.results = vitalsObject;

        fragment.patientUuid = commonVisitData.getPatientUuid();//intent.getStringExtra("patientUuid");
        fragment.visitUuid = commonVisitData.getVisitUuid(); // intent.getStringExtra("visitUuid");
        fragment.encounterVitals = commonVisitData.getEncounterUuidVitals();//intent.getStringExtra("encounterUuidVitals");
        fragment.encounterAdultIntials = commonVisitData.getEncounterUuidAdultIntial();//intent.getStringExtra("encounterUuidAdultIntial");
        fragment.EncounterAdultInitial_LatestVisit = commonVisitData.getEncounterAdultInitialLatestVisit();//intent.getStringExtra("EncounterAdultInitial_LatestVisit");
        fragment.state = commonVisitData.getState();//intent.getStringExtra("state");
        fragment.patientName = commonVisitData.getPatientName();//intent.getStringExtra("name");
        fragment.patientGender = commonVisitData.getPatientGender();//intent.getStringExtra("gender");
        fragment.intentTag = commonVisitData.getIntentTag();//intent.getStringExtra("tag");
        fragment.float_ageYear_Month = commonVisitData.getPatientAgeYearMonth();//intent.getFloatExtra("float_ageYear_Month", 0);
        String[] temp = String.valueOf(fragment.float_ageYear_Month).split("\\.");
        fragment.mAgeInMonth = Integer.parseInt(temp[0]) * 12 + Integer.parseInt(temp[1]);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActionListener = (VisitCreationActionListener) context;
        sessionManager = new SessionManager(context);
        configUtils = new ConfigUtils(context);

        //mHeightMasterList = getNumbersInRange(Integer.parseInt(AppConstants.MINIMUM_HEIGHT), Integer.parseInt(AppConstants.MAXIMUM_HEIGHT));
        //mWeightMasterList = getNumbersInRange(Integer.parseInt(AppConstants.MINIMUM_WEIGHT), Integer.parseInt(AppConstants.MAXIMUM_WEIGHT));
    }

    /*public List<Integer> getNumbersUsingIntStreamRange(int start, int end) {
        return IntStream.range(start, end)
                .boxed()
                .collect(Collectors.toList());
    }*/
    public List<Integer> getNumbersInRange(int start, int end) {
        List<Integer> result = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            result.add(i);
        }
        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SessionManager.getInstance(getContext()).getVitalPreference() != null) {
            vitalPref = SessionManager.getInstance(getContext()).getVitalPreference();
        } else {
            vitalPref = new VitalPreference();
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_vital_collection, container, false);
        CustomLog.v("float_ageYear_Month", float_ageYear_Month + "");
        //mHeightSpinner = view.findViewById(R.id.sp_height);
        //mWeightSpinner = view.findViewById(R.id.sp_weight);

        mBloodGroupTextView = mRootView.findViewById(R.id.tv_blood_group_spinner);

        mHeightEditText = mRootView.findViewById(R.id.etv_height);
        mWeightEditText = mRootView.findViewById(R.id.etv_weight);

        mHeightEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(3, 0)});
        mWeightEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(3, 0)});
        /*mHeightTextView.setOnClickListener(this);
        mWeightTextView.setOnClickListener(this);*/

        mBMICardView = mRootView.findViewById(R.id.ll_bmi);
        if (float_ageYear_Month <= 19)
            mBMICardView.setVisibility(View.GONE);
        mBMITextView = mRootView.findViewById(R.id.tv_bmi_value);
        mBmiStatusTextView = mRootView.findViewById(R.id.tv_bmi_status);

        mBpSysEditText = mRootView.findViewById(R.id.etv_bp_sys);
        mBpDiaEditText = mRootView.findViewById(R.id.etv_bp_dia);

        mSpo2EditText = mRootView.findViewById(R.id.etv_spo2);
        mPulseEditText = mRootView.findViewById(R.id.etv_pulse);
        mRespEditText = mRootView.findViewById(R.id.etv_respiratory_rate);
        mTemperatureEditText = mRootView.findViewById(R.id.etv_temperature);
        mTemperatureEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(3, 0)});
        // errors
        mHeightErrorTextView = mRootView.findViewById(R.id.tv_height_error);
        mWeightErrorTextView = mRootView.findViewById(R.id.tv_weight_error);

        mBpSysErrorTextView = mRootView.findViewById(R.id.etv_bp_sys_error);
        mBpDiaErrorTextView = mRootView.findViewById(R.id.etv_bp_dia_error);

        mSpo2ErrorTextView = mRootView.findViewById(R.id.etv_spo2_error);

        mPulseErrorTextView = mRootView.findViewById(R.id.etv_pulse_error);

        mRespErrorTextView = mRootView.findViewById(R.id.etv_respiratory_rate_error);

        mTemperatureErrorTextView = mRootView.findViewById(R.id.etv_temperature_error);
        mBloodGroupErrorTextView = mRootView.findViewById(R.id.tv_blood_group_error);

        mHeightErrorTextView.setVisibility(View.GONE);
        mWeightErrorTextView.setVisibility(View.GONE);
        mBpSysErrorTextView.setVisibility(View.GONE);
        mBpDiaErrorTextView.setVisibility(View.GONE);
        mSpo2ErrorTextView.setVisibility(View.GONE);
        mPulseErrorTextView.setVisibility(View.GONE);
        mRespErrorTextView.setVisibility(View.GONE);
        mTemperatureErrorTextView.setVisibility(View.GONE);
        mBloodGroupErrorTextView.setVisibility(View.GONE);

        mHeightEditText.addTextChangedListener(new MyTextWatcher(mHeightEditText));
        mWeightEditText.addTextChangedListener(new MyTextWatcher(mWeightEditText));

        mBpSysEditText.addTextChangedListener(new MyTextWatcher(mBpSysEditText));
        mBpDiaEditText.addTextChangedListener(new MyTextWatcher(mBpDiaEditText));
        mSpo2EditText.addTextChangedListener(new MyTextWatcher(mSpo2EditText));
        mPulseEditText.addTextChangedListener(new MyTextWatcher(mPulseEditText));
        mRespEditText.addTextChangedListener(new MyTextWatcher(mRespEditText));
        mTemperatureEditText.addTextChangedListener(new MyTextWatcher(mTemperatureEditText));

        mSubmitButton = mRootView.findViewById(R.id.btn_submit);
        mSubmitButton.setOnClickListener(this);
        mSubmitButton.setClickable(true);
        mHeightCardView = mRootView.findViewById(R.id.ll_height_container);

        mWeightCardView = mRootView.findViewById(R.id.ll_weight_container);

        mBMICardView = mRootView.findViewById(R.id.ll_bmi);

        mSBPCardView = mRootView.findViewById(R.id.ll_sbp_container);

        mDBPCardView = mRootView.findViewById(R.id.ll_dbp_container);

        mPulseCardView = mRootView.findViewById(R.id.ll_pulse_container);

        mTemperatureCardView = mRootView.findViewById(R.id.ll_temperature_container);

        mSpo2CardView = mRootView.findViewById(R.id.ll_spo2_container);

        mRespiratoryCardView = mRootView.findViewById(R.id.ll_respiratory_rate_container);

        mBloodGroupCardView = mRootView.findViewById(R.id.ll_blood_group_container);


        //showHeightListing();


        //showWeightListing();

        if (mIsEditMode && results == null) {
            loadSavedDateForEditFromDB();
        }

        mBloodGroupTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<SimpleItemData> mItemList = new ArrayList<SimpleItemData>();
                List<String> displaySelection = new ArrayList<>();
                String locale = new SessionManager(requireActivity()).getAppLanguage();
                displaySelection = Arrays.asList(getResources().getStringArray(R.array.blood_group_list));
                for (int i = 0; i < displaySelection.size(); i++) {
                    SimpleItemData simpleItemData = new SimpleItemData();
                    simpleItemData.setTitle(displaySelection.get(i));
                    simpleItemData.setObject(VisitUtils.getBloodPressureCode(displaySelection.get(i)));
                    if (displaySelection.get(i).equalsIgnoreCase("Don\\'t Know")) {
                        simpleItemData.setTitleLocal(getString(R.string.dont_know));
                    }

                    if (displaySelection.get(i).equalsIgnoreCase("O-")) {
                        simpleItemData.setSubTitleLocal(getString(R.string.universal_donor));
                    }

                    if (displaySelection.get(i).equalsIgnoreCase("AB+")) {
                        simpleItemData.setSubTitleLocal(getString(R.string.universal_recipient));
                    }


                    mItemList.add(simpleItemData);
                }
                MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(requireActivity());
                final LayoutInflater inflater = getLayoutInflater();
                View convertView = inflater.inflate(R.layout.list_dialog_language, null);
                alertDialogBuilder.setView(convertView);

                RecyclerView recyclerView = convertView.findViewById(R.id.lang_dialog_list_view);

                recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false));
                DialogSimpleListAdapter dialogListAdapter = new DialogSimpleListAdapter(recyclerView, requireActivity(), mItemList, new DialogSimpleListAdapter.OnItemSelection() {
                    @Override
                    public void onSelect(SimpleItemData data) {
                        if (mBloodGroupAlertDialog != null) {
                            mBloodGroupAlertDialog.dismiss();
                        }
                        mBloodGroupTextView.setText(data.getTitle());
                        //as we are saving code not text for blood group
                        mBloodGroupTextView.setTag(data.getObject().toString());
                        boolean isValid = isValidaForm();
                        setDisabledSubmit(!isValid);
                    }
                });
                recyclerView.setAdapter(dialogListAdapter);
                mBloodGroupAlertDialog = alertDialogBuilder.show();
                mBloodGroupAlertDialog.getWindow().setBackgroundDrawableResource(R.drawable.popup_menu_background);
            }
        });

        setDataIfExist();

        return mRootView;
    }


    private void setDataIfExist() {
        vitalPref = SessionManager.getInstance(getContext()).getVitalPreference();
        if (vitalPref != null) {
            mHeightEditText.setText(vitalPref.getHeight());
            mWeightEditText.setText(vitalPref.getWeight());
            mBMITextView.setText(vitalPref.getBmi());
            mBpSysEditText.setText(vitalPref.getBpSystolic());
            mBpSysEditText.setText(vitalPref.getBpSystolic());
            mBpDiaEditText.setText(vitalPref.getBpDiastolic());
            mTemperatureEditText.setText(vitalPref.getTemperature());
            mSpo2EditText.setText(vitalPref.getSpO2());
            mRespEditText.setText(vitalPref.getRespiratoryRate());
            mBloodGroupErrorTextView.setText(vitalPref.getRespiratoryRate());
        }
    }

    private void updatePreference() {
        VitalPreference vitalPref = new VitalPreference();
        vitalPref.setHeight(mHeightEditText.getText().toString());
        vitalPref.setWeight(mWeightEditText.getText().toString());
        vitalPref.setBmi(mBMITextView.getText().toString());
        vitalPref.setBpDiastolic(mBpDiaEditText.getText().toString());
        vitalPref.setBpSystolic(mBpSysEditText.getText().toString());
        vitalPref.setTemperature(mTemperatureEditText.getText().toString());
        vitalPref.setSpO2(mSpo2EditText.getText().toString());
        vitalPref.setRespiratoryRate(mRespEditText.getText().toString());
        SessionManager.getInstance(getContext()).saveVitalPreference(vitalPref);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //config viewmodel initialization
        PatientVitalRepository repository = new PatientVitalRepository(ConfigDatabase.getInstance(requireActivity()).patientVitalDao());
        PatientVitalViewModelFactory factory = new PatientVitalViewModelFactory(repository);
        PatientVitalViewModel patientVitalViewModel = new ViewModelProvider(this, factory).get(PatientVitalViewModel.class);
        //requireActivity();
        /*patientVitalViewModel.getAllEnabledLiveFields()
                .observe(requireActivity(), it -> {
                            mPatientVitalList = it;
                            //Timber.tag(TAG).v(new Gson().toJson(mPatientVitalList));
                            updateUI();
                        }
                );*/
        CoroutineProvider.usePatientVitalScope(
                LifecycleOwnerKt.getLifecycleScope(this),
                patientVitalViewModel,
                data -> {
                    mPatientVitalList = (List<PatientVital>) data;
                    updateUI();
                }
        );
    }

    private void updateUI() {
        mHeightCardView.setVisibility(View.GONE);
        mWeightCardView.setVisibility(View.GONE);
        mBMICardView.setVisibility(View.GONE);
        mSBPCardView.setVisibility(View.GONE);
        mDBPCardView.setVisibility(View.GONE);
        mPulseCardView.setVisibility(View.GONE);
        mTemperatureCardView.setVisibility(View.GONE);
        mSpo2CardView.setVisibility(View.GONE);
        mRespiratoryCardView.setVisibility(View.GONE);
        mBloodGroupCardView.setVisibility(View.GONE);
        /*if (float_ageYear_Month <= 19)
            bmiLinearLayout.setVisibility(View.GONE);
        else
            bmiLinearLayout.setVisibility(View.VISIBLE);*/

        for (PatientVital patientVital : mPatientVitalList) {
            CustomLog.v(TAG, patientVital.getName() + "\t" + patientVital.getVitalKey());

            if (patientVital.getVitalKey().equals(PatientVitalConfigKeys.HEIGHT)) {
                mHeightCardView.setVisibility(View.VISIBLE);
                mHeightCardView.setTag(patientVital);
                appendMandatorySing(patientVital.isMandatory(), mRootView.findViewById(R.id.tv_height_lbl));
            } else if (patientVital.getVitalKey().equals(PatientVitalConfigKeys.WEIGHT)) {
                mWeightCardView.setVisibility(View.VISIBLE);
                mWeightCardView.setTag(patientVital);
                appendMandatorySing(patientVital.isMandatory(), mRootView.findViewById(R.id.tv_wight_lbl));

            } else if (patientVital.getVitalKey().equals(PatientVitalConfigKeys.BMI)) {
                mBMICardView.setVisibility(View.VISIBLE);
                mBMICardView.setTag(patientVital);
                appendMandatorySing(patientVital.isMandatory(), mRootView.findViewById(R.id.tv_bmi_lbl));

            } else if (patientVital.getVitalKey().equals(PatientVitalConfigKeys.SBP)) {
                mSBPCardView.setVisibility(View.VISIBLE);
                mSBPCardView.setTag(patientVital);
                appendMandatorySing(patientVital.isMandatory(), mRootView.findViewById(R.id.tv_bp_sys_lbl));

            } else if (patientVital.getVitalKey().equals(PatientVitalConfigKeys.DBP)) {
                mDBPCardView.setVisibility(View.VISIBLE);
                mDBPCardView.setTag(patientVital);
                appendMandatorySing(patientVital.isMandatory(), mRootView.findViewById(R.id.tv_bp_dia_lbl));

            } else if (patientVital.getVitalKey().equals(PatientVitalConfigKeys.PULSE)) {
                mPulseCardView.setVisibility(View.VISIBLE);
                mPulseCardView.setTag(patientVital);
                appendMandatorySing(patientVital.isMandatory(), mRootView.findViewById(R.id.tv_pulse_lbl));
            } else if (patientVital.getVitalKey().equals(PatientVitalConfigKeys.TEMPERATURE)) {
                mTemperatureCardView.setVisibility(View.VISIBLE);
                mTemperatureCardView.setTag(patientVital);
                appendMandatorySing(patientVital.isMandatory(), mRootView.findViewById(R.id.tv_temperature_lbl));

            } else if (patientVital.getVitalKey().equals(PatientVitalConfigKeys.SPO2)) {
                mSpo2CardView.setVisibility(View.VISIBLE);
                mSpo2CardView.setTag(patientVital);
                appendMandatorySing(patientVital.isMandatory(), mRootView.findViewById(R.id.tv_spo2_lbl));
            } else if (patientVital.getVitalKey().equals(PatientVitalConfigKeys.RESPIRATORY_RATE)) {
                mRespiratoryCardView.setVisibility(View.VISIBLE);
                mRespiratoryCardView.setTag(patientVital);
                appendMandatorySing(patientVital.isMandatory(), mRootView.findViewById(R.id.tv_respiratory_rate_lbl));
            } else if (patientVital.getVitalKey().equals(PatientVitalConfigKeys.BLOOD_TYPE)) {
                mBloodGroupCardView.setVisibility(View.VISIBLE);
                mBloodGroupCardView.setTag(patientVital);
                appendMandatorySing(patientVital.isMandatory(), mRootView.findViewById(R.id.tv_blood_group_lbl));
            }
        }
        LinearLayout otherBlockLinearLayout = mRootView.findViewById(R.id.ll_other_info_block_container);
        otherBlockLinearLayout.setVisibility(countVisible(otherBlockLinearLayout) == 1 ? View.GONE : View.VISIBLE);

        LinearLayout patientVitalBlockLinearLayout = mRootView.findViewById(R.id.ll_patient_vital_block_container);
        patientVitalBlockLinearLayout.setVisibility(countVisible(patientVitalBlockLinearLayout) == 1 ? View.GONE : View.VISIBLE);


    }

    private void appendMandatorySing(boolean isMandatory, TextView textView) {
        if (isMandatory) {
            textView.append("*");
        }
    }

    private int countVisible(ViewGroup myLayout) {
        if (myLayout == null) return 0;
        int count = 0;
        for (int i = 0; i < myLayout.getChildCount(); i++) {
            if (myLayout.getChildAt(i).getVisibility() == View.VISIBLE)
                count++;
        }
        return count;
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
            updatePreference();
        }

        @Override
        public void afterTextChanged(Editable editable) {

            String val = editable.toString().trim();
            if (val.equals(".")) {
                editText.setText("");
                return;
            }
            boolean isValid = isValidaForm();
            updatePreference();
            setDisabledSubmit(!isValid);
        }
    }


    private boolean isValidaForm() {
        boolean isValid = true;

        //if (editText.getId() == R.id.etv_height) {
        String heightVal = mHeightEditText.getText().toString().trim();

        String weight = mWeightEditText.getText().toString().trim();
        if (mHeightCardView.getTag() != null && ((PatientVital) mHeightCardView.getTag()).isMandatory() && heightVal.isEmpty()) {
            mHeightErrorTextView.setText(getString(R.string.error_field_required));
            mHeightErrorTextView.setVisibility(View.VISIBLE);
            //mHeightEditText.requestFocus();
            mHeightEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            return false;
        } else {
            mHeightErrorTextView.setVisibility(View.GONE);
            mHeightEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
        }
        if (mHeightCardView.getTag() != null) {
            if (heightVal.isEmpty()) {
                if (weight.isEmpty()) {
                    mHeightErrorTextView.setVisibility(View.GONE);
                    mHeightEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                } else {
                    mHeightErrorTextView.setVisibility(View.VISIBLE);
                    mHeightErrorTextView.setText(getString(R.string.error_field_required));
                    mHeightEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                    return false;
                }
            } else {
                if ((Double.parseDouble(heightVal) > Double.parseDouble(AppConstants.MAXIMUM_HEIGHT)) ||
                        (Double.parseDouble(heightVal) < Double.parseDouble(AppConstants.MINIMUM_HEIGHT))) {
                    //et.setError(getString(R.string.bpsys_error, AppConstants.MINIMUM_BP_SYS, AppConstants.MAXIMUM_BP_SYS));

                    mHeightErrorTextView.setText(getString(R.string.height_error, AppConstants.MINIMUM_HEIGHT, AppConstants.MAXIMUM_HEIGHT));
                    mHeightErrorTextView.setVisibility(View.VISIBLE);
                    mHeightEditText.requestFocus();
                    mHeightEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                    return false;

                } else {
                    mHeightErrorTextView.setVisibility(View.GONE);
                    mHeightEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            }
            heightvalue = heightVal;
            calculateBMI();
        }
        //}

        //if (editText.getId() == R.id.etv_weight) {
        String wightVal = mWeightEditText.getText().toString().trim();
        String height = mHeightEditText.getText().toString().trim();
        if (mWeightCardView.getTag() != null && ((PatientVital) mWeightCardView.getTag()).isMandatory() && wightVal.isEmpty()) {
            mWeightErrorTextView.setText(getString(R.string.error_field_required));
            mWeightErrorTextView.setVisibility(View.VISIBLE);
            //mWeightEditText.requestFocus();
            mWeightEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            return false;
        } else {
            mWeightErrorTextView.setVisibility(View.GONE);
            mWeightEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
        }
        if (mWeightCardView.getTag() != null) {
            if (wightVal.isEmpty()) {
                if (height.isEmpty()) {
                    mWeightErrorTextView.setVisibility(View.GONE);
                    mWeightEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                } else {
                    mWeightErrorTextView.setVisibility(View.VISIBLE);
                    mWeightErrorTextView.setText(getString(R.string.error_field_required));
                    mWeightEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                    return false;
                }
            } else {
                if ((Double.parseDouble(wightVal) > Double.parseDouble(AppConstants.getMaxWeightByAge(mAgeInMonth))) ||
                        (Double.parseDouble(wightVal) < Double.parseDouble(AppConstants.getMinWeightByAge(mAgeInMonth)))) {
                    //et.setError(getString(R.string.bpdia_error, AppConstants.MINIMUM_BP_DSYS, AppConstants.MAXIMUM_BP_DSYS));
                    mWeightErrorTextView.setText(getString(R.string.weight_error, AppConstants.getMinWeightByAge(mAgeInMonth), AppConstants.getMaxWeightByAge(mAgeInMonth)));
                    mWeightErrorTextView.setVisibility(View.VISIBLE);
                    mWeightEditText.requestFocus();
                    mWeightEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                    return false;
                } else {
                    mWeightErrorTextView.setVisibility(View.GONE);
                    mWeightEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }

            }
            weightvalue = wightVal;
            calculateBMI();
        }
        // }


        //if (editText.getId() == R.id.etv_bp_sys) {
        String bpSysVal = mBpSysEditText.getText().toString().trim();

        String bpDia = mBpDiaEditText.getText().toString().trim();
        if (bpSysVal.isEmpty()) {
            if (mSBPCardView.getTag() != null && ((PatientVital) mSBPCardView.getTag()).isMandatory()) {
                mBpSysErrorTextView.setText(getString(R.string.error_field_required));
                mBpSysErrorTextView.setVisibility(View.VISIBLE);
                //mBpSysEditText.requestFocus();
                mBpSysEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                return false;
            } else {
                if (bpDia.isEmpty()) {
                    mBpSysErrorTextView.setVisibility(View.GONE);
                    mBpSysEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                } else {
                    mBpSysErrorTextView.setVisibility(View.VISIBLE);
                    mBpSysErrorTextView.setText(getString(R.string.error_field_required));
                    mBpSysEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);

                    mBpDiaErrorTextView.setVisibility(View.GONE);
                    mBpDiaEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);

                    return false;
                }
            }
        } else {
            if ((Double.parseDouble(bpSysVal) > Double.parseDouble(AppConstants.MAXIMUM_BP_SYS)) ||
                    (Double.parseDouble(bpSysVal) < Double.parseDouble(AppConstants.MINIMUM_BP_SYS))) {
                //et.setError(getString(R.string.bpsys_error, AppConstants.MINIMUM_BP_SYS, AppConstants.MAXIMUM_BP_SYS));

                mBpSysErrorTextView.setText(getString(R.string.bpsys_error, AppConstants.MINIMUM_BP_SYS, AppConstants.MAXIMUM_BP_SYS));
                mBpSysErrorTextView.setVisibility(View.VISIBLE);
                mBpSysEditText.requestFocus();
                mBpSysEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);

                mBpDiaErrorTextView.setVisibility(View.GONE);
                mBpDiaEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);

                return false;
                //return;
            }

            if (bpDia.isEmpty()) {
                mBpSysErrorTextView.setVisibility(View.GONE);
                mBpSysEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
            } else {
                int bpSysInt = Integer.parseInt(bpSysVal);
                int bpDiaInt = Integer.parseInt(bpDia);
                if (bpDiaInt >= bpSysInt) {
                    mBpSysErrorTextView.setVisibility(View.VISIBLE);
                    mBpSysErrorTextView.setText(getString(R.string.bp_validation_sys));
                    mBpSysEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);

                    mBpDiaErrorTextView.setVisibility(View.GONE);
                    mBpDiaEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);

                    return false;
                } else {
                    mBpSysErrorTextView.setVisibility(View.GONE);
                    mBpSysEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);

                    mBpDiaErrorTextView.setVisibility(View.GONE);
                    mBpDiaEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            }
        }
        //}

        //if (editText.getId() == R.id.etv_bp_dia) {
        String bpDiaVal = mBpDiaEditText.getText().toString().trim();
        String bpSys = mBpSysEditText.getText().toString().trim();

        if (bpDiaVal.isEmpty()) {
            if (mDBPCardView.getTag() != null && ((PatientVital) mDBPCardView.getTag()).isMandatory()) {
                mBpDiaErrorTextView.setText(getString(R.string.error_field_required));
                mBpDiaErrorTextView.setVisibility(View.VISIBLE);
                //mBpDiaEditText.requestFocus();
                mBpDiaEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                return false;
            } else {
                if (bpSys.isEmpty()) {
                    mBpDiaErrorTextView.setVisibility(View.GONE);
                    mBpDiaEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                } else {
                    mBpDiaErrorTextView.setVisibility(View.VISIBLE);
                    mBpDiaErrorTextView.setText(getString(R.string.error_field_required));
                    mBpDiaEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                    return false;
                }
            }
        } else {
            if ((Double.parseDouble(bpDiaVal) > Double.parseDouble(AppConstants.MAXIMUM_BP_DSYS)) ||
                    (Double.parseDouble(bpDiaVal) < Double.parseDouble(AppConstants.MINIMUM_BP_DSYS))) {
                //et.setError(getString(R.string.bpdia_error, AppConstants.MINIMUM_BP_DSYS, AppConstants.MAXIMUM_BP_DSYS));
                mBpDiaErrorTextView.setText(getString(R.string.bpdia_error, AppConstants.MINIMUM_BP_DSYS, AppConstants.MAXIMUM_BP_DSYS));
                mBpDiaErrorTextView.setVisibility(View.VISIBLE);
                mBpDiaEditText.requestFocus();
                mBpDiaEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                return false;
                //return;
            }
            if (bpSys.isEmpty()) {
                mBpDiaErrorTextView.setVisibility(View.GONE);
                mBpDiaEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
            } else {
                int bpSysInt = Integer.parseInt(bpSys);
                int bpDiaInt = Integer.parseInt(bpDiaVal);
                if (bpDiaInt >= bpSysInt) {
                    mBpDiaErrorTextView.setVisibility(View.VISIBLE);
                    mBpDiaErrorTextView.setText(getString(R.string.bp_validation_dia));
                    mBpDiaEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                    return false;
                } else {
                    mBpDiaErrorTextView.setVisibility(View.GONE);
                    mBpDiaEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);

                    mBpSysErrorTextView.setVisibility(View.GONE);
                    mBpSysEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);

                }
            }
        }
        // }


        //if (editText.getId() == R.id.etv_pulse) {
        String pulseVal = mPulseEditText.getText().toString().trim();
        if (pulseVal.isEmpty()) {
            if (mPulseCardView.getTag() != null && ((PatientVital) mPulseCardView.getTag()).isMandatory()) {
                mPulseErrorTextView.setText(getString(R.string.error_field_required));
                mPulseErrorTextView.setVisibility(View.VISIBLE);
                //mPulseEditText.requestFocus();
                mPulseEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                return false;
            } else {
                    /*mPulseErrorTextView.setVisibility(View.VISIBLE);
                    mPulseErrorTextView.setText(getString(R.string.error_field_required));
                    mPulseEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);*/
                mPulseErrorTextView.setVisibility(View.GONE);
                mPulseEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
            }

        } else {
            if ((Double.parseDouble(pulseVal) > Double.parseDouble(AppConstants.MAXIMUM_PULSE)) ||
                    (Double.parseDouble(pulseVal) < Double.parseDouble(AppConstants.MINIMUM_PULSE))) {

                mPulseErrorTextView.setText(getString(R.string.pulse_error, AppConstants.MINIMUM_PULSE, AppConstants.MAXIMUM_PULSE));
                mPulseErrorTextView.setVisibility(View.VISIBLE);
                mPulseEditText.requestFocus();
                mPulseEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);

                return false;
            } else {
                mPulseErrorTextView.setVisibility(View.GONE);
                mPulseEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
            }

        }
        // } 

        //if (editText.getId() == R.id.etv_temperature) {
        String temperatureVal = mTemperatureEditText.getText().toString().trim();
        if (temperatureVal.isEmpty()) {
            if (mTemperatureCardView.getTag() != null && ((PatientVital) mTemperatureCardView.getTag()).isMandatory()) {
                mTemperatureErrorTextView.setText(getString(R.string.error_field_required));
                mTemperatureErrorTextView.setVisibility(View.VISIBLE);
                // mTemperatureEditText.requestFocus();
                mTemperatureEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                return false;
            } else {
                    /*mTemperatureErrorTextView.setVisibility(View.VISIBLE);
                    mTemperatureErrorTextView.setText(getString(R.string.error_field_required));
                    mTemperatureEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);*/
                mTemperatureErrorTextView.setVisibility(View.GONE);
                mTemperatureEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
            }
        } else {
            if (configUtils.celsius()) {
                if ((Double.parseDouble(temperatureVal) > Double.parseDouble(AppConstants.MAXIMUM_TEMPERATURE_CELSIUS)) ||
                        (Double.parseDouble(temperatureVal) < Double.parseDouble(AppConstants.MINIMUM_TEMPERATURE_CELSIUS))) {
                    //et.setError(getString(R.string.temp_error, AppConstants.MINIMUM_TEMPERATURE_CELSIUS, AppConstants.MAXIMUM_TEMPERATURE_CELSIUS));
                    mTemperatureErrorTextView.setText(getString(R.string.temp_error, AppConstants.MINIMUM_TEMPERATURE_CELSIUS, AppConstants.MAXIMUM_TEMPERATURE_CELSIUS));
                    mTemperatureErrorTextView.setVisibility(View.VISIBLE);
                    mTemperatureEditText.requestFocus();
                    mTemperatureEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                    return false;

                } else {
                    mTemperatureErrorTextView.setVisibility(View.GONE);
                    mTemperatureEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            } else if (configUtils.fahrenheit()) {
                if ((Double.parseDouble(temperatureVal) > Double.parseDouble(AppConstants.MAXIMUM_TEMPERATURE_FARHENIT)) ||
                        (Double.parseDouble(temperatureVal) < Double.parseDouble(AppConstants.MINIMUM_TEMPERATURE_FARHENIT))) {
                    mTemperatureErrorTextView.setText(getString(R.string.temp_error, AppConstants.MINIMUM_TEMPERATURE_FARHENIT, AppConstants.MAXIMUM_TEMPERATURE_FARHENIT));
                    mTemperatureErrorTextView.setVisibility(View.VISIBLE);
                    mTemperatureEditText.requestFocus();
                    mTemperatureEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                    return false;

                } else {
                    mTemperatureErrorTextView.setVisibility(View.GONE);
                    mTemperatureEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            }

        }
        //}

        //if (editText.getId() == R.id.etv_spo2) {
        String spo2Val = mSpo2EditText.getText().toString().trim();
        if (spo2Val.isEmpty()) {
            if (mSpo2CardView.getTag() != null && ((PatientVital) mSpo2CardView.getTag()).isMandatory()) {
                mSpo2ErrorTextView.setText(getString(R.string.error_field_required));
                mSpo2ErrorTextView.setVisibility(View.VISIBLE);
                //mSpo2EditText.requestFocus();
                mSpo2EditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                return false;
            } else {
                    /*mSpo2ErrorTextView.setVisibility(View.VISIBLE);
                    mSpo2ErrorTextView.setText(getString(R.string.error_field_required));
                    mSpo2EditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);*/
                mSpo2ErrorTextView.setVisibility(View.GONE);
                mSpo2EditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
            }
        } else {
            if ((Double.parseDouble(spo2Val) > Double.parseDouble(AppConstants.MAXIMUM_SPO2)) ||
                    (Double.parseDouble(spo2Val) < Double.parseDouble(AppConstants.MINIMUM_SPO2))) {
                mSpo2ErrorTextView.setText(getString(R.string.spo2_error, AppConstants.MINIMUM_SPO2, AppConstants.MAXIMUM_SPO2));
                mSpo2ErrorTextView.setVisibility(View.VISIBLE);
                mSpo2EditText.requestFocus();
                mSpo2EditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                return false;

            } else {
                mSpo2ErrorTextView.setVisibility(View.GONE);
                mSpo2EditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
            }

        }
        //}

        // if (editText.getId() == R.id.etv_respiratory_rate) {
        String respRateVal = mRespEditText.getText().toString().trim();

        if (respRateVal.isEmpty()) {
            if (mRespiratoryCardView.getTag() != null && ((PatientVital) mRespiratoryCardView.getTag()).isMandatory()) {
                mRespErrorTextView.setText(getString(R.string.error_field_required));
                mRespErrorTextView.setVisibility(View.VISIBLE);
                //mRespEditText.requestFocus();
                mRespEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                return false;
            } else {
                    /*mRespErrorTextView.setVisibility(View.VISIBLE);
                    mRespErrorTextView.setText(getString(R.string.error_field_required));
                    mRespEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);*/
                mRespErrorTextView.setVisibility(View.GONE);
                mRespEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
            }
        } else {
            if ((Double.parseDouble(respRateVal) > Double.parseDouble(AppConstants.MAXIMUM_RESPIRATORY)) ||
                    (Double.parseDouble(respRateVal) < Double.parseDouble(AppConstants.MINIMUM_RESPIRATORY))) {
                mRespErrorTextView.setText(getString(R.string.resp_error, AppConstants.MINIMUM_RESPIRATORY, AppConstants.MAXIMUM_RESPIRATORY));
                mRespErrorTextView.setVisibility(View.VISIBLE);
                mRespEditText.requestFocus();
                mRespEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                return false;

            } else {
                mRespErrorTextView.setVisibility(View.GONE);
                mRespEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
            }

        }
        // }


        String bloodGroup = mBloodGroupTextView.getText().toString().trim();

        if (mBloodGroupCardView.getTag() != null && ((PatientVital) mBloodGroupCardView.getTag()).isMandatory() && bloodGroup.isEmpty()) {
            mBloodGroupErrorTextView.setText(getString(R.string.error_field_required));
            mBloodGroupErrorTextView.setVisibility(View.VISIBLE);
            //mPulseEditText.requestFocus();
            mBloodGroupTextView.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            return false;
        } else {
                    /*mPulseErrorTextView.setVisibility(View.VISIBLE);
                    mPulseErrorTextView.setText(getString(R.string.error_field_required));
                    mPulseEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);*/
            mBloodGroupErrorTextView.setVisibility(View.GONE);
            mBloodGroupTextView.setBackgroundResource(R.drawable.bg_input_fieldnew);
        }


        return isValid;
    }

    private void setDisabledSubmit(boolean disableNow) {
        if (disableNow) {
            mSubmitButton.setClickable(false);
            mSubmitButton.setEnabled(false);
//            mSubmitButton.setBackgroundResource(R.drawable.ui2_common_primary_bg_disabled_1);
        } else {
            mSubmitButton.setClickable(true);
            mSubmitButton.setEnabled(true);
//            mSubmitButton.setBackgroundResource(R.drawable.ui2_common_primary_bg);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_submit) {//validate
            mSubmitButton.setClickable(false);
            boolean isValid = isValidaForm();
            setDisabledSubmit(!isValid);
            if (isValid) {
                isDataReadyForSaving();
                mActionListener.onProgress(100);
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_1_VITAL_SUMMARY, mIsEditMode, results);
            }
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
            if (results.getHeight() != null && !results.getHeight().isEmpty() && !results.getHeight().equalsIgnoreCase("0")) {
                //mHeightSpinner.setSelection(mHeightArrayAdapter.getPosition(results.getHeight() + " " + getResources().getString(R.string.cm)), true);
                mHeightEditText.setText(results.getHeight());
            }


            if (results.getWeight() != null && !results.getWeight().isEmpty()) {
                //mWeightSpinner.setSelection(mWeightArrayAdapter.getPosition(results.getWeight() + " " + getResources().getString(R.string.kg)), true);
                mWeightEditText.setText(results.getWeight());
            }

            /*if (results.getBmi() != null && !results.getBmi().isEmpty())
              pass*/


            if (results.getBpsys() != null && !results.getBpsys().isEmpty())
                mBpSysEditText.setText(results.getBpsys());

            if (results.getBpdia() != null && !results.getBpdia().isEmpty())
                mBpDiaEditText.setText(results.getBpdia());


            if (results.getPulse() != null && !results.getPulse().isEmpty())
                mPulseEditText.setText(results.getPulse());

            if (results.getTemperature() != null && !results.getTemperature().isEmpty()) {
                if (new ConfigUtils(getActivity()).fahrenheit()) {
                    mTemperatureEditText.setText(convertCtoF(TAG, results.getTemperature()));
                } else {
                    mTemperatureEditText.setText(results.getTemperature());
                }
                if (mTemperatureEditText.getText().toString().endsWith(".")) {
                    mTemperatureEditText.setText(mTemperatureEditText.getText().toString().replace(".", ""));
                }
            }
            if (results.getSpo2() != null && !results.getSpo2().isEmpty())
                mSpo2EditText.setText(results.getSpo2());
            if (results.getBloodGroup() != null && !results.getBloodGroup().isEmpty())
                mBloodGroupTextView.setText(VisitUtils.getBloodPressureEnStringFromCode(results.getBloodGroup()));

            if (results.getResp() != null && !results.getResp().isEmpty())
                mRespEditText.setText(results.getResp());

        }
    }


    private BMIStatus getBmiStatus(double bmi) {
        //"< 18.5: Underweight
        //18.5 to 24.9: Normal Weight
        //25.0 to 29.9: Overweight
        //30.0 to 34.9: Moderate Obesity (Class 1)
        //35.0 to 39.9: Severe Obesity (Class 2)
        //40.0 & Above: Very Severe (Morbid) Obesity (Class 3)"
        BMIStatus bmiStatus = new BMIStatus();
        bmiStatus.setStatus("");
        bmiStatus.setColor(R.color.gray);
        if (bmi < 18.5) {
            bmiStatus.setStatus(getResources().getString(R.string.underweight));
            bmiStatus.setColor(R.color.ui2_bmi1);
        } else if (bmi > 18.5 && bmi <= 24.9) {
            bmiStatus.setStatus(getResources().getString(R.string.normal));
            bmiStatus.setColor(R.color.ui2_bmi2);
        } else if (bmi > 24.9 && bmi <= 29.9) {
            bmiStatus.setStatus(getResources().getString(R.string.overweight));
            bmiStatus.setColor(R.color.ui2_bmi3);
        } else if (bmi > 29.9 && bmi <= 34.9) {
            bmiStatus.setStatus(getResources().getString(R.string.morbidly_obese));
            bmiStatus.setColor(R.color.ui2_bmi4);
        } else if (bmi > 34.9 && bmi <= 39.9) {
            bmiStatus.setStatus(getResources().getString(R.string.severely_obese));
            bmiStatus.setColor(R.color.ui2_bmi5);
        } else if (bmi >= 39.9) {
            bmiStatus.setStatus(getResources().getString(R.string.very_severely_obese));
            bmiStatus.setColor(R.color.ui2_bmi6);
        }
        return bmiStatus;
    }


    public void calculateBMI() {

        mBMITextView.setText("");
        mBmiStatusTextView.setText("");
        if (weightvalue == null || weightvalue.isEmpty() || heightvalue == null || heightvalue.isEmpty()) {
            return;
        }
        double numerator = Double.parseDouble(weightvalue) * 10000;
        double denominator = (Double.parseDouble(heightvalue)) * (Double.parseDouble(heightvalue));
        double bmi_value = numerator / denominator;
        DecimalFormat df = new DecimalFormat("0.00");
        mBMITextView.setText(df.format(bmi_value) + " kg/m");
        CustomLog.d("BMI", "BMI: " + mBMITextView.getText().toString());
        //mBMI.setText(String.format(Locale.ENGLISH, "%.2f", bmi_value));

        BMIStatus bmiStatus = getBmiStatus(bmi_value);
        mBmiStatusTextView.setText(String.format("(%s)", bmiStatus.getStatus()));
        mBmiStatusTextView.setTextColor(ContextCompat.getColor(getActivity(), bmiStatus.getColor()));
    }

    public void calculateBMI_onEdit(String height, String weight) {
        if (height != null && weight != null && height.toString().trim().length() > 0 && !height.toString().startsWith(".") &&
                weight.toString().trim().length() > 0 && !weight.toString().startsWith(".")) {

            mBMITextView.setText("");
            mBmiStatusTextView.setText("");
            double numerator = Double.parseDouble(weight) * 10000;
            double denominator = (Double.parseDouble(height)) * (Double.parseDouble(height));
            double bmi_value = numerator / denominator;
            DecimalFormat df = new DecimalFormat("0.00");
            mBMITextView.setText(df.format(bmi_value) + " kg/m");
            CustomLog.d("BMI", "BMI: " + mBMITextView.getText().toString());
            //mBMI.setText(String.format(Locale.ENGLISH, "%.2f", bmi_value));

            BMIStatus bmiStatus = getBmiStatus(bmi_value);
            mBmiStatusTextView.setText(String.format("(%s)", bmiStatus.getStatus()));
            mBmiStatusTextView.setTextColor(ContextCompat.getColor(getActivity(), (bmiStatus.getColor())));
        } else {
            // do nothing
            mBMITextView.setText("");
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
            case UuidDictionary.HEIGHT: //Height
                heightvalue = value;
                //mHeightTextView.setText(value);
                if (heightvalue != null && !heightvalue.isEmpty() && !heightvalue.equalsIgnoreCase("0")) {
                    //CustomLog.v(TAG, "getHeight - " + results.getHeight());
                    //CustomLog.v(TAG, "getPosition - " + mHeightArrayAdapter.getPosition(results.getHeight()));
                    //mHeightSpinner.setSelection(mHeightArrayAdapter.getPosition(heightvalue + " " + getResources().getString(R.string.cm)), true);
                    mHeightEditText.setText(heightvalue);
                }


                break;
            case UuidDictionary.WEIGHT: //Weight
                weightvalue = value;
                if (weightvalue != null && !weightvalue.isEmpty()) {
                    //mWeightSpinner.setSelection(mWeightArrayAdapter.getPosition(weightvalue + " " + getResources().getString(R.string.kg)), true);
                    mWeightEditText.setText(weightvalue);
                }
                //mWeightTextView.setText(value);
                break;
            case UuidDictionary.PULSE: //Pulse
                if (value != null && !value.isEmpty())
                    mPulseEditText.setText(value);
                break;
            case UuidDictionary.SYSTOLIC_BP: //Systolic BP
                if (value != null && !value.isEmpty())
                    mBpSysEditText.setText(value);
                break;
            case UuidDictionary.DIASTOLIC_BP: //Diastolic BP
                if (value != null && !value.isEmpty())
                    mBpDiaEditText.setText(value);
                break;
            case UuidDictionary.TEMPERATURE: //Temperature

                //mTemperatureEditText.setText(value);
                if (new ConfigUtils(getActivity()).fahrenheit()) {
                    mTemperatureEditText.setText(convertCtoF(TAG, value));
                } else {
                    mTemperatureEditText.setText(value);
                }
                if (mTemperatureEditText.getText().toString().endsWith(".")) {
                    mTemperatureEditText.setText(mTemperatureEditText.getText().toString().replace(".", ""));
                }

                break;
            //    Respiratory added by mahiti dev team
            case UuidDictionary.RESPIRATORY: //Respiratory
                if (value != null && !value.isEmpty())
                    mRespEditText.setText(value);
                break;
            case UuidDictionary.SPO2: //SpO2
                if (value != null && !value.isEmpty())
                    mSpo2EditText.setText(value);
                break;
            case UuidDictionary.BLOOD_GROUP: //SpO2
                if (value != null && !value.isEmpty()) {
                    mBloodGroupTextView.setText(VisitUtils.getBloodPressureEnStringFromCode(value));
                    mBloodGroupTextView.setTag(value);
                }
                break;
            default:
                break;

        }
        //on edit on vs screen, the bmi will be set in vitals bmi edit field.
        if (mBMITextView.getText().toString().equalsIgnoreCase("")) {
            calculateBMI_onEdit(heightvalue, weightvalue);
        }
    }

    public boolean isDataReadyForSaving() {
        /*boolean cancel = false;
        View focusView = null;

        String height = heightvalue;
        String weight = weightvalue;
        if (mHeightLinearLayout.getTag() != null && ((PatientVital) mHeightLinearLayout.getTag()).isMandatory()) {
            if (height.isEmpty()) {
                mHeightErrorTextView.setVisibility(View.VISIBLE);
                mHeightErrorTextView.setText(getString(R.string.error_field_required));
                //mHeightSpinner.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                mHeightEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                return false;
            } else {
                mHeightErrorTextView.setVisibility(View.GONE);
                //mHeightSpinner.setBackgroundResource(R.drawable.edittext_border);
                mHeightEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
            }
        }
        if (mWeightLinearLayout.getTag() != null && ((PatientVital) mWeightLinearLayout.getTag()).isMandatory()) {
            //if (!isPatientAdult() && weight.isEmpty()) {
            if (weight.isEmpty()) {
                mWeightErrorTextView.setVisibility(View.VISIBLE);
                //mWeightErrorTextView.setText(getString(R.string.error_field_required_non_adult));
                mWeightErrorTextView.setText(getString(R.string.error_field_required));
                //mWeightSpinner.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                mWeightEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                return false;
            } else {
                mWeightErrorTextView.setVisibility(View.GONE);
                //mWeightSpinner.setBackgroundResource(R.drawable.edittext_border);
                mWeightEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
            }
        }

           *//* if (!weight.isEmpty() && height.isEmpty()) {
                mHeightErrorTextView.setVisibility(View.VISIBLE);
                mHeightErrorTextView.setText(getString(R.string.error_field_required));
                //mHeightSpinner.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                mHeightEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                return false;
            } else {
                mHeightErrorTextView.setVisibility(View.GONE);
                //mHeightSpinner.setBackgroundResource(R.drawable.edittext_border);
                mHeightEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
            }

            if (!height.isEmpty() && weight.isEmpty()) {
                mWeightErrorTextView.setVisibility(View.VISIBLE);
                mWeightErrorTextView.setText(getString(R.string.error_field_required));
                //mWeightSpinner.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                mHeightEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                return false;
            } else {
                mWeightErrorTextView.setVisibility(View.GONE);
                //mWeightSpinner.setBackgroundResource(R.drawable.edittext_border);
                mWeightEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
            }*//*


        // Store values at the time of the fab is clicked.
        ArrayList<EditText> values = new ArrayList<EditText>();
        *//*values.add(mHeight);
        values.add(mWeight);*//*

        values.add(mBpSysEditText); //0
        values.add(mBpDiaEditText); // 1
        values.add(mSpo2EditText);// 2
        values.add(mPulseEditText); // 3
        values.add(mRespEditText); // 4
        values.add(mTemperatureEditText); // 5

        // Check to see if values were inputted.
        for (int i = 0; i < values.size(); i++) {

            if (i == 3) {
                EditText et = values.get(i);
                String abc2 = et.getText().toString().trim();
                if (!abc2.isEmpty() && !abc2.equals("0.0")) {
                    if ((Double.parseDouble(abc2) > Double.parseDouble(AppConstants.MAXIMUM_PULSE)) ||
                            (Double.parseDouble(abc2) < Double.parseDouble(AppConstants.MINIMUM_PULSE))) {
                        //et.setError(getString(R.string.pulse_error, AppConstants.MINIMUM_PULSE, AppConstants.MAXIMUM_PULSE));
                        focusView = et;
                        mPulseErrorTextView.setText(getString(R.string.pulse_error, AppConstants.MINIMUM_PULSE, AppConstants.MAXIMUM_PULSE));
                        mPulseErrorTextView.setVisibility(View.VISIBLE);
                        mPulseEditText.requestFocus();
                        mPulseEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                        cancel = true;
                        break;
                    } else {
                        mPulseErrorTextView.setVisibility(View.GONE);
                        mPulseEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                    }
//       }
                } else {
                    mPulseErrorTextView.setVisibility(View.GONE);
                    mPulseEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }

            } else if (i == 0) {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (!abc1.isEmpty() && !abc1.equals("0.0")) {
                    if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_BP_SYS)) ||
                            (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_BP_SYS))) {
                        //et.setError(getString(R.string.bpsys_error, AppConstants.MINIMUM_BP_SYS, AppConstants.MAXIMUM_BP_SYS));

                        mBpSysErrorTextView.setText(getString(R.string.bpsys_error, AppConstants.MINIMUM_BP_SYS, AppConstants.MAXIMUM_BP_SYS));
                        mBpSysErrorTextView.setVisibility(View.VISIBLE);
                        mBpSysEditText.requestFocus();
                        mBpSysEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                        cancel = true;
                        break;
                    } else {
                        //mBpSysErrorTextView.setVisibility(View.GONE);
                        //mBpSysEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                        String bpDia = mBpDiaEditText.getText().toString().trim();
                        if (bpDia.isEmpty()) {
                            mBpSysErrorTextView.setVisibility(View.GONE);
                            mBpSysEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                        } else {
                            int bpSysInt = Integer.parseInt(abc1);
                            int bpDiaInt = Integer.parseInt(bpDia);
                            if (bpDiaInt >= bpSysInt) {
                                mBpSysErrorTextView.setVisibility(View.VISIBLE);
                                mBpSysErrorTextView.setText(getString(R.string.bp_validation_sys));
                                mBpSysEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                                cancel = true;
                                break;
                            } else {
                                mBpSysErrorTextView.setVisibility(View.GONE);
                                mBpSysEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                            }
                        }
                    }
//       }
                } else {
                    mBpSysErrorTextView.setVisibility(View.GONE);
                    mBpSysEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }

            } else if (i == 1) {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (!abc1.isEmpty() && !abc1.equals("0.0")) {
                    if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_BP_DSYS)) ||
                            (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_BP_DSYS))) {
                        //et.setError(getString(R.string.bpdia_error, AppConstants.MINIMUM_BP_DSYS, AppConstants.MAXIMUM_BP_DSYS));
                        mBpDiaErrorTextView.setText(getString(R.string.bpdia_error, AppConstants.MINIMUM_BP_DSYS, AppConstants.MAXIMUM_BP_DSYS));
                        mBpDiaErrorTextView.setVisibility(View.VISIBLE);
                        mBpDiaEditText.requestFocus();
                        mBpDiaEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                        cancel = true;
                        break;
                    } else {
                        //mBpDiaErrorTextView.setVisibility(View.GONE);
                        //mBpDiaEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                        String bpSys = mBpSysEditText.getText().toString().trim();
                        if (bpSys.isEmpty()) {
                            mBpDiaErrorTextView.setVisibility(View.GONE);
                            mBpDiaEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                        } else {
                            int bpSysInt = Integer.parseInt(bpSys);
                            int bpDiaInt = Integer.parseInt(abc1);
                            if (bpDiaInt >= bpSysInt) {
                                mBpDiaErrorTextView.setVisibility(View.VISIBLE);
                                mBpDiaErrorTextView.setText(getString(R.string.bp_validation_dia));
                                mBpDiaEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                                cancel = true;
                                break;
                            } else {
                                mBpDiaErrorTextView.setVisibility(View.GONE);
                                mBpDiaEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                            }
                        }
                    }
//       }
                } else {
                    mBpDiaErrorTextView.setVisibility(View.GONE);
                    mBpDiaEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }

            } else if (i == 5) {
                PatientVital patientVital = (PatientVital) mTemperatureLinearLayout.getTag();
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (patientVital != null && patientVital.isMandatory()) {
                    mTemperatureErrorTextView.setText(getString(R.string.error_field_required));
                    mTemperatureErrorTextView.setVisibility(View.VISIBLE);
                    mTemperatureEditText.requestFocus();
                    mTemperatureEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                    cancel = true;
                    break;
                }
                if (!abc1.isEmpty() && !abc1.equals("0.0")) {
                    if (configUtils.celsius()) {
                        if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_TEMPERATURE_CELSIUS)) ||
                                (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_TEMPERATURE_CELSIUS))) {
                            //et.setError(getString(R.string.temp_error, AppConstants.MINIMUM_TEMPERATURE_CELSIUS, AppConstants.MAXIMUM_TEMPERATURE_CELSIUS));
                            mTemperatureErrorTextView.setText(getString(R.string.temp_error, AppConstants.MINIMUM_TEMPERATURE_CELSIUS, AppConstants.MAXIMUM_TEMPERATURE_CELSIUS));
                            mTemperatureErrorTextView.setVisibility(View.VISIBLE);
                            mTemperatureEditText.requestFocus();
                            mTemperatureEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                            cancel = true;
                            break;
                        } else {
                            mTemperatureErrorTextView.setVisibility(View.GONE);
                            mTemperatureEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                        }
                    } else if (configUtils.fahrenheit()) {
                        if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_TEMPERATURE_FARHENIT)) ||
                                (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_TEMPERATURE_FARHENIT))) {
                            //et.setError(getString(R.string.temp_error, AppConstants.MINIMUM_TEMPERATURE_FARHENIT, AppConstants.MAXIMUM_TEMPERATURE_FARHENIT));
                            mTemperatureErrorTextView.setText(getString(R.string.temp_error, AppConstants.MINIMUM_TEMPERATURE_FARHENIT, AppConstants.MAXIMUM_TEMPERATURE_FARHENIT));
                            mTemperatureErrorTextView.setVisibility(View.VISIBLE);
                            mTemperatureEditText.requestFocus();
                            mTemperatureEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                            cancel = true;
                            break;
                        } else {
                            mTemperatureErrorTextView.setVisibility(View.GONE);
                            mTemperatureEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                        }
                    }
                } else {
                    mTemperatureErrorTextView.setVisibility(View.GONE);
                    mTemperatureEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            } else if (i == 4) {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (!abc1.isEmpty() && !abc1.equals("0.0")) {
                    if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_RESPIRATORY)) ||
                            (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_RESPIRATORY))) {
                        //et.setError(getString(R.string.resp_error, AppConstants.MINIMUM_RESPIRATORY, AppConstants.MAXIMUM_RESPIRATORY));
                        mRespErrorTextView.setText(getString(R.string.resp_error, AppConstants.MINIMUM_RESPIRATORY, AppConstants.MAXIMUM_RESPIRATORY));
                        mRespErrorTextView.setVisibility(View.VISIBLE);
                        mRespEditText.requestFocus();
                        mRespEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                        cancel = true;
                        break;
                    } else {
                        mRespErrorTextView.setVisibility(View.GONE);
                        mRespEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                    }
//       }
                } else {
                    mRespErrorTextView.setVisibility(View.GONE);
                    mRespEditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            } else {
                EditText et = values.get(i);
                String abc1 = et.getText().toString().trim();
                if (!abc1.isEmpty() && !abc1.equals("0.0")) {
                    if ((Double.parseDouble(abc1) > Double.parseDouble(AppConstants.MAXIMUM_SPO2)) ||
                            (Double.parseDouble(abc1) < Double.parseDouble(AppConstants.MINIMUM_SPO2))) {
                        //et.setError(getString(R.string.spo2_error, AppConstants.MINIMUM_SPO2, AppConstants.MAXIMUM_SPO2));
                        mSpo2ErrorTextView.setText(getString(R.string.spo2_error, AppConstants.MINIMUM_SPO2, AppConstants.MAXIMUM_SPO2));
                        mSpo2ErrorTextView.setVisibility(View.VISIBLE);
                        mSpo2EditText.requestFocus();
                        mSpo2EditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
                        cancel = true;
                        break;
                    } else {
                        mSpo2ErrorTextView.setVisibility(View.GONE);
                        mSpo2EditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                    }
//       }
                } else {
                    mSpo2ErrorTextView.setVisibility(View.GONE);
                    mSpo2EditText.setBackgroundResource(R.drawable.bg_input_fieldnew);
                }
            }
        }
        if (cancel) {
            return false;
        }

        if (mBpSysEditText.getText().toString().trim().isEmpty() && !mBpDiaEditText.getText().toString().trim().isEmpty()) {
            mBpSysErrorTextView.setVisibility(View.VISIBLE);
            mBpSysErrorTextView.setText(getString(R.string.error_field_required));
            mBpSysEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mBpSysEditText.requestFocus();
            return false;
        }
        if (!mBpSysEditText.getText().toString().trim().isEmpty() && mBpDiaEditText.getText().toString().trim().isEmpty()) {
            mBpDiaErrorTextView.setVisibility(View.VISIBLE);
            mBpDiaErrorTextView.setText(getString(R.string.error_field_required));
            mBpDiaEditText.setBackgroundResource(R.drawable.input_field_error_bg_ui2);
            mBpDiaEditText.requestFocus();
            return false;
        }*/

        try {
            if (results == null) {
                results = new VitalsObject();
            }
            String height = mHeightEditText.getText().toString().trim();
            String weight = mWeightEditText.getText().toString().trim();
            if (!height.equals("")) {
                results.setHeight(height);
            } else {
                results.setHeight("0");
            }
            results.setWeight(weight);
            results.setPulse((mPulseEditText.getText().toString()));
            results.setBpdia((mBpDiaEditText.getText().toString()));
            results.setBpsys((mBpSysEditText.getText().toString()));
            if (!mTemperatureEditText.getText().toString().isEmpty()) {
                if (configUtils.fahrenheit()) {
                    results.setTemperature(convertFtoC(TAG, mTemperatureEditText.getText().toString()));
                } else {
                    results.setTemperature((mTemperatureEditText.getText().toString()));
                }
            } else {
                results.setTemperature("");
            }
            results.setResp((mRespEditText.getText().toString()));
            results.setSpo2((mSpo2EditText.getText().toString()));
            if (mBloodGroupTextView.getTag() != null)
                results.setBloodGroup(mBloodGroupTextView.getTag().toString());
            else
                results.setBloodGroup("");
            if (mBMITextView.getText() != null && mBMITextView.getText().toString().trim().contains(" ")) {
                results.setBmi(mBMITextView.getText().toString().trim().split(" ")[0]);
            } else {
                results.setBmi("");
            }


        } catch (NumberFormatException e) {
            //Snackbar.make(findViewById(R.id.cl_table), R.string.error_non_decimal_no_added, Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }

//


        ObsDAO obsDAO = new ObsDAO();
        ObsDTO obsDTO = new ObsDTO();
        if (getActivity().getIntent().equals("edit")) {
            ObsDAO.deleteExistingVitalsDataIfExists(visitUuid);

            try {
                PatientVital patientVital = (PatientVital) mHeightCardView.getTag();
                if ((patientVital != null && patientVital.isMandatory()) || !results.getHeight().isEmpty()) {
                    obsDTO = new ObsDTO();
                    obsDTO.setConceptuuid(UuidDictionary.HEIGHT);
                    obsDTO.setEncounteruuid(encounterVitals);
                    obsDTO.setCreator(sessionManager.getCreatorID());
                    if (results.getHeight().equals("")) {
                        obsDTO.setValue("0");
                    } else {
                        obsDTO.setValue(results.getHeight());
                    }
                    //obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.HEIGHT));
                    obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, patientVital.getUuid()));
                    obsDTO.setConceptsetuuid(UuidDictionary.OBS_TYPE_VITAL_SET);

                    obsDAO.updateObs(obsDTO);
                }

                patientVital = (PatientVital) mWeightCardView.getTag();
                if ((patientVital != null && patientVital.isMandatory()) || !results.getWeight().isEmpty()) {
                    obsDTO = new ObsDTO();
                    obsDTO.setConceptuuid(UuidDictionary.WEIGHT);
                    obsDTO.setEncounteruuid(encounterVitals);
                    obsDTO.setCreator(sessionManager.getCreatorID());
                    obsDTO.setValue(results.getWeight());
                    //obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.WEIGHT));
                    obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, patientVital.getUuid()));
                    obsDTO.setConceptsetuuid(UuidDictionary.OBS_TYPE_VITAL_SET);

                    obsDAO.updateObs(obsDTO);
                }

                patientVital = (PatientVital) mPulseCardView.getTag();
                if ((patientVital != null && patientVital.isMandatory()) || !results.getPulse().isEmpty()) {
                    obsDTO = new ObsDTO();
                    obsDTO.setConceptuuid(UuidDictionary.PULSE);
                    obsDTO.setEncounteruuid(encounterVitals);
                    obsDTO.setCreator(sessionManager.getCreatorID());
                    obsDTO.setValue(results.getPulse());
                    //obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.PULSE));
                    obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, patientVital.getUuid()));
                    obsDTO.setConceptsetuuid(UuidDictionary.OBS_TYPE_VITAL_SET);

                    obsDAO.updateObs(obsDTO);
                }

                patientVital = (PatientVital) mSBPCardView.getTag();
                if ((patientVital != null && patientVital.isMandatory()) || !results.getBpsys().isEmpty()) {
                    obsDTO = new ObsDTO();
                    obsDTO.setConceptuuid(UuidDictionary.SYSTOLIC_BP);
                    obsDTO.setEncounteruuid(encounterVitals);
                    obsDTO.setCreator(sessionManager.getCreatorID());
                    obsDTO.setValue(results.getBpsys());
                    //obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.SYSTOLIC_BP));
                    obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, patientVital.getUuid()));
                    obsDTO.setConceptsetuuid(UuidDictionary.OBS_TYPE_VITAL_SET);

                    obsDAO.updateObs(obsDTO);
                }

                patientVital = (PatientVital) mDBPCardView.getTag();
                if ((patientVital != null && patientVital.isMandatory()) || !results.getBpdia().isEmpty()) {
                    obsDTO = new ObsDTO();
                    obsDTO.setConceptuuid(UuidDictionary.DIASTOLIC_BP);
                    obsDTO.setEncounteruuid(encounterVitals);
                    obsDTO.setCreator(sessionManager.getCreatorID());
                    obsDTO.setValue(results.getBpdia());
                    //obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.DIASTOLIC_BP));
                    obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, patientVital.getUuid()));
                    obsDTO.setConceptsetuuid(UuidDictionary.OBS_TYPE_VITAL_SET);

                    obsDAO.updateObs(obsDTO);
                }

                patientVital = (PatientVital) mTemperatureCardView.getTag();
                if ((patientVital != null && patientVital.isMandatory()) || !results.getTemperature().isEmpty()) {
                    obsDTO = new ObsDTO();
                    obsDTO.setConceptuuid(UuidDictionary.TEMPERATURE);
                    obsDTO.setEncounteruuid(encounterVitals);
                    obsDTO.setCreator(sessionManager.getCreatorID());
                    obsDTO.setValue(results.getTemperature());
                    //obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.TEMPERATURE));
                    obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, patientVital.getUuid()));
                    obsDTO.setConceptsetuuid(UuidDictionary.OBS_TYPE_VITAL_SET);

                    obsDAO.updateObs(obsDTO);
                }

                patientVital = (PatientVital) mRespiratoryCardView.getTag();
                if ((patientVital != null && patientVital.isMandatory()) || !results.getResp().isEmpty()) {
                    obsDTO = new ObsDTO();
                    obsDTO.setConceptuuid(UuidDictionary.RESPIRATORY);
                    obsDTO.setEncounteruuid(encounterVitals);
                    obsDTO.setCreator(sessionManager.getCreatorID());
                    obsDTO.setValue(results.getResp());
                    //obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.RESPIRATORY));
                    obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, patientVital.getUuid()));
                    obsDTO.setConceptsetuuid(UuidDictionary.OBS_TYPE_VITAL_SET);

                    obsDAO.updateObs(obsDTO);
                }

                patientVital = (PatientVital) mSpo2CardView.getTag();
                if ((patientVital != null && patientVital.isMandatory()) || !results.getSpo2().isEmpty()) {
                    obsDTO = new ObsDTO();
                    obsDTO.setConceptuuid(UuidDictionary.SPO2);
                    obsDTO.setEncounteruuid(encounterVitals);
                    obsDTO.setCreator(sessionManager.getCreatorID());
                    obsDTO.setValue(results.getSpo2());
                    //obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.SPO2));
                    obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, patientVital.getUuid()));
                    obsDTO.setConceptsetuuid(UuidDictionary.OBS_TYPE_VITAL_SET);

                    obsDAO.updateObs(obsDTO);
                }

                patientVital = (PatientVital) mBloodGroupCardView.getTag();
                if ((patientVital != null && patientVital.isMandatory()) || !results.getBloodGroup().isEmpty()) {
                    obsDTO = new ObsDTO();
                    obsDTO.setConceptuuid(UuidDictionary.BLOOD_GROUP);
                    obsDTO.setEncounteruuid(encounterVitals);
                    obsDTO.setCreator(sessionManager.getCreatorID());
                    obsDTO.setValue(results.getBloodGroup());
                    //obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, UuidDictionary.BLOOD_GROUP));
                    obsDTO.setUuid(obsDAO.getObsuuid(encounterVitals, patientVital.getUuid()));
                    obsDTO.setConceptsetuuid(UuidDictionary.OBS_TYPE_VITAL_SET);

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

                /*Intent intent = new Intent(getActivity(), VisitSummaryActivity.class);
                intent.putExtra("patientUuid", patientUuid);
                intent.putExtra("visitUuid", visitUuid);
                intent.putExtra("encounterUuidVitals", encounterVitals);
                intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                intent.putExtra("state", state);
                intent.putExtra("name", patientName);
                intent.putExtra("gender", patientGender);
                intent.putExtra("tag", intentTag);
                intent.putExtra("hasPrescription", "false");
                startActivity(intent);*/
            } catch (DAOException dao) {
                FirebaseCrashlytics.getInstance().recordException(dao);
            }
        } else {
            ObsDAO.deleteExistingVitalsDataIfExists(visitUuid);

            PatientVital patientVital = (PatientVital) mHeightCardView.getTag();
            if ((patientVital != null && patientVital.isMandatory()) || (patientVital != null && !results.getHeight().isEmpty())) {
                obsDTO = new ObsDTO();
                //obsDTO.setConceptuuid(UuidDictionary.HEIGHT);
                obsDTO.setConceptuuid(patientVital.getUuid());
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                if (results.getHeight().equals("")) {
                    obsDTO.setValue("0");
                } else {
                    obsDTO.setValue(results.getHeight());
                }

                obsDTO.setUuid(AppConstants.NEW_UUID);
                obsDTO.setConceptsetuuid(UuidDictionary.OBS_TYPE_VITAL_SET);

                try {
                    obsDAO.insertObs(obsDTO);
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }

            patientVital = (PatientVital) mWeightCardView.getTag();
            if ((patientVital != null && patientVital.isMandatory()) || (patientVital != null && !results.getWeight().isEmpty())) {
                obsDTO = new ObsDTO();
                //obsDTO.setConceptuuid(UuidDictionary.WEIGHT);
                obsDTO.setConceptuuid(patientVital.getUuid());
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getWeight());
                obsDTO.setConceptsetuuid(UuidDictionary.OBS_TYPE_VITAL_SET);


                try {
                    obsDAO.insertObs(obsDTO);
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }

            patientVital = (PatientVital) mPulseCardView.getTag();
            if ((patientVital != null && patientVital.isMandatory()) || (patientVital != null && !results.getPulse().isEmpty())) {

                obsDTO = new ObsDTO();
                //obsDTO.setConceptuuid(UuidDictionary.PULSE);
                obsDTO.setConceptuuid(patientVital.getUuid());
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getPulse());
                obsDTO.setConceptsetuuid(UuidDictionary.OBS_TYPE_VITAL_SET);

                try {
                    obsDAO.insertObs(obsDTO);
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }

            patientVital = (PatientVital) mSBPCardView.getTag();
            if ((patientVital != null && patientVital.isMandatory()) || (patientVital != null && !results.getBpsys().isEmpty())) {
                obsDTO = new ObsDTO();
                //obsDTO.setConceptuuid(UuidDictionary.SYSTOLIC_BP);
                obsDTO.setConceptuuid(patientVital.getUuid());
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getBpsys());
                obsDTO.setConceptsetuuid(UuidDictionary.OBS_TYPE_VITAL_SET);

                try {
                    obsDAO.insertObs(obsDTO);
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }

            patientVital = (PatientVital) mDBPCardView.getTag();
            if ((patientVital != null && patientVital.isMandatory()) || (patientVital != null && !results.getBpdia().isEmpty())) {
                obsDTO = new ObsDTO();
                //obsDTO.setConceptuuid(UuidDictionary.DIASTOLIC_BP);
                obsDTO.setConceptuuid(patientVital.getUuid());
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getBpdia());
                obsDTO.setConceptsetuuid(UuidDictionary.OBS_TYPE_VITAL_SET);

                try {
                    obsDAO.insertObs(obsDTO);
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }

            patientVital = (PatientVital) mTemperatureCardView.getTag();
            if ((patientVital != null && patientVital.isMandatory()) || (patientVital != null && !results.getTemperature().isEmpty())) {
                obsDTO = new ObsDTO();
                //obsDTO.setConceptuuid(UuidDictionary.TEMPERATURE);
                obsDTO.setConceptuuid(patientVital.getUuid());
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getTemperature());
                obsDTO.setConceptsetuuid(UuidDictionary.OBS_TYPE_VITAL_SET);

                try {
                    obsDAO.insertObs(obsDTO);
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }

            patientVital = (PatientVital) mRespiratoryCardView.getTag();
            if ((patientVital != null && patientVital.isMandatory()) || (patientVital != null && !results.getResp().isEmpty())) {
                obsDTO = new ObsDTO();
                //obsDTO.setConceptuuid(UuidDictionary.RESPIRATORY);
                obsDTO.setConceptuuid(patientVital.getUuid());
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getResp());
                obsDTO.setConceptsetuuid(UuidDictionary.OBS_TYPE_VITAL_SET);

                try {
                    obsDAO.insertObs(obsDTO);
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }

            patientVital = (PatientVital) mSpo2CardView.getTag();
            if ((patientVital != null && patientVital.isMandatory()) || (patientVital != null && !results.getSpo2().isEmpty())) {
                obsDTO = new ObsDTO();
                //obsDTO.setConceptuuid(UuidDictionary.SPO2);
                obsDTO.setConceptuuid(patientVital.getUuid());
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getSpo2());
                obsDTO.setConceptsetuuid(UuidDictionary.OBS_TYPE_VITAL_SET);

                try {
                    obsDAO.insertObs(obsDTO);
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }

            patientVital = (PatientVital) mBloodGroupCardView.getTag();
            if ((patientVital != null && patientVital.isMandatory()) || (patientVital != null && !results.getBloodGroup().isEmpty())) {
                obsDTO = new ObsDTO();
                //obsDTO.setConceptuuid(UuidDictionary.BLOOD_GROUP);
                obsDTO.setConceptuuid(patientVital.getUuid());
                obsDTO.setEncounteruuid(encounterVitals);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(results.getBloodGroup());
                obsDTO.setConceptsetuuid(UuidDictionary.OBS_TYPE_VITAL_SET);

                try {
                    obsDAO.insertObs(obsDTO);
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }
        }
        return true;
    }

    /**
     * check patient is adult or not
     *
     * @return
     */
    private boolean isPatientAdult() {
        return float_ageYear_Month > 18;
    }

    /*private String convertFtoC(String temperature) {
        CustomLog.i(TAG, "convertFtoC IN: " + temperature);
        if (temperature != null && temperature.length() > 0) {
            String result = "";
            double fTemp = Double.parseDouble(temperature);
            double cTemp = ((fTemp - 32) * 5 / 9);

//            DecimalFormat dtime = new DecimalFormat("#.##");
            DecimalFormat dtime = new DecimalFormat("#.#");
            cTemp = Double.parseDouble(dtime.format(cTemp));
            result = String.format("%.1f", cTemp);
            //result = String.valueOf(cTemp);
            CustomLog.i(TAG, "convertFtoC OUT: " + result);

            return result;
        }
        return "";

    }

    private String convertCtoF(String temperature) {
        CustomLog.i(TAG, "convertCtoF IN: " + temperature);

        if (temperature == null) return "";
        String result = "";
        double a = Double.parseDouble(String.valueOf(temperature));
        Double b = (a * 9 / 5) + 32;

        //DecimalFormat dtime = new DecimalFormat("#.##");
        DecimalFormat dtime = new DecimalFormat("#.#");
        b = Double.parseDouble(dtime.format(b));
        result = String.format("%.1f", b);
        //result = String.valueOf(b);
        CustomLog.i(TAG, "convertCtoF OUT: " + result);
        return result;

    }*/


}