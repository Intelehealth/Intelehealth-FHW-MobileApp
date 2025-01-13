package org.intelehealth.app.ayu.visit.reason;

import android.content.Context;
import android.os.Bundle;

import org.intelehealth.app.utilities.CustomLog;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.ayu.visit.VisitCreationActionListener;
import org.intelehealth.app.ayu.visit.VisitCreationActivity;
import org.intelehealth.app.ayu.visit.common.VisitUtils;
import org.intelehealth.app.ayu.visit.common.adapter.NodeAdapterUtils;
import org.intelehealth.app.ayu.visit.model.CommonVisitData;
import org.intelehealth.app.ayu.visit.model.ReasonData;
import org.intelehealth.app.ayu.visit.model.ReasonGroupData;
import org.intelehealth.app.ayu.visit.reason.adapter.ReasonListingAdapter;
import org.intelehealth.app.ayu.visit.reason.adapter.SelectedChipsGridAdapter;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.utilities.CustomLog;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.FlavorKeys;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.WindowsUtils;
import org.intelehealth.config.room.entity.FeatureActiveStatus;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VisitReasonCaptureFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VisitReasonCaptureFragment extends Fragment {

    private VisitCreationActionListener mActionListener;
    SessionManager sessionManager;
    private AutoCompleteTextView mVisitReasonAutoCompleteTextView;
    private RecyclerView mSelectedComplainRecyclerView;
    private TextView mEmptyReasonLabelTextView;
    //private ImageView mClearImageView;

    private List<ReasonData> mSelectedComplains = new ArrayList<>();
    private List<ReasonGroupData> mVisitReasonItemList;
    private ReasonListingAdapter mReasonListingAdapter;

    private String patientName = "";
    private String patientGender = "";
    private String intentTag;
    private String state;
    private String patientUuid;
    private String visitUuid;
    private String encounterVitals;
    private float float_ageYear_Month;
    private String encounterAdultIntials = "", EncounterAdultInitial_LatestVisit = "";
    private List<String> mFinalEnabledMMList = new ArrayList<>();
    private List<ReasonData> mRawReasonDataList = new ArrayList<>();
    private boolean mIsEditMode = false;
    private Button btnCancel, btnSubmit;

    public VisitReasonCaptureFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActionListener = (VisitCreationActionListener) context;
        sessionManager = new SessionManager(context);
        mRawReasonDataList = getVisitReasonFilesNamesOnly();
    }

    public static VisitReasonCaptureFragment newInstance(CommonVisitData commonVisitData, boolean isEditMode, boolean cleanEdit) {
        VisitReasonCaptureFragment fragment = new VisitReasonCaptureFragment();
        fragment.mIsEditMode = isEditMode;
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
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_visit_reason_capture, container, false);
        mSelectedComplainRecyclerView = view.findViewById(R.id.rcv_selected_container);
        //mClearImageView = view.findViewById(R.id.iv_clear);
        mEmptyReasonLabelTextView = view.findViewById(R.id.tv_empty_reason_lbl);
        mVisitReasonAutoCompleteTextView = view.findViewById(R.id.actv_reasons);
        view.findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BuildConfig.FLAVOR_client == FlavorKeys.UNFPA) {
                    if (mVisitReasonAutoCompleteTextView.getText().toString().isEmpty()) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.please_select_at_least_one_complaint), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ReasonData data = new ReasonData();
                    data.setReasonName(mVisitReasonAutoCompleteTextView.getText().toString());
                    data.setCustom(true);
                    data.setDefaultReasonName("Visit Reason");
                    data.setReasonNameLocalized(data.getReasonName());
                    mSelectedComplains.add(data);
                    mVisitReasonAutoCompleteTextView.setText("", true);
                } else {
                    if (mSelectedComplains.isEmpty()) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.please_select_at_least_one_complaint), Toast.LENGTH_SHORT).show();
                        return;
                    }

                }
                showConfirmDialog();
            }
        });

        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mActionListener.onFormSubmitted(VisitCreationActivity.STEP_2_DIAGNOSTICS_SUMMARY, false, null);
                boolean diagnosticsActiveStatus = ((VisitCreationActivity) requireActivity()).getFeatureActiveStatus().getActiveStatusDiagnosticsSection();
                boolean vitalsActiveStatus = ((VisitCreationActivity) requireActivity()).getFeatureActiveStatus().getVitalSection();
                if (diagnosticsActiveStatus) {
                    mActionListener.onFormSubmitted(VisitCreationActivity.STEP_2_DIAGNOSTICS_SUMMARY, false, null);
                } else if (vitalsActiveStatus) {
                    mActionListener.onFormSubmitted(VisitCreationActivity.STEP_1_VITAL_SUMMARY, false, null);
                } else {

                }
            }
        });

        if (BuildConfig.FLAVOR_client == FlavorKeys.UNFPA) {
            mSelectedComplainRecyclerView.setVisibility(View.GONE);
            view.findViewById(R.id.rcv_all_reason).setVisibility(View.GONE);
            view.findViewById(R.id.tv_selected_reason_lbl).setVisibility(View.GONE);
            view.findViewById(R.id.tv_all_reason_lbl).setVisibility(View.GONE);
            mEmptyReasonLabelTextView.setVisibility(View.GONE);
            mVisitReasonAutoCompleteTextView.setCompoundDrawables(null, null, null, null);
        } else {
            // TODO: we are adding this below string array for keeping these two protocol enable for search also
            mFinalEnabledMMList.clear();
            List<ReasonData> mindmapReasonDataList = getVisitReasonFilesNamesOnly();


            for (ReasonData data : mindmapReasonDataList) {
                String mindMapName = data.getReasonName();
                JSONObject currentFile = null;
                if (!sessionManager.getLicenseKey().isEmpty()) {
                    currentFile = FileUtils.encodeJSONFromFile(requireActivity(), mindMapName + ".json");
                } else {
                    String fileLocation = "engines/" + mindMapName + ".json";
                    currentFile = FileUtils.encodeJSON(getActivity(), fileLocation);
                }

                Node mainNode = new Node(currentFile);
                if (VisitUtils.checkNodeValidByGenderAndAge(patientGender, float_ageYear_Month, mainNode.getGender(), mainNode.getMin_age(), mainNode.getMax_age())) {
                    mFinalEnabledMMList.add(mindMapName);
                }
            }
            String[] mindmapsNamesFinalArray = new String[mFinalEnabledMMList.size()];

            for (int i = 0; i < mFinalEnabledMMList.size(); i++) {
                for (int j = 0; j < mRawReasonDataList.size(); j++) {
                    if (mFinalEnabledMMList.get(i).equalsIgnoreCase(mRawReasonDataList.get(j).getReasonName())) {
                        mindmapsNamesFinalArray[i] = NodeAdapterUtils.formatChiefComplainWithLocaleName(mRawReasonDataList.get(j));
                        break;
                    }
                }

            }


            ArrayAdapter<String> adapter = new ArrayAdapter<String>
                    (getActivity(), R.layout.ui2_custome_dropdown_item_view, mindmapsNamesFinalArray);

            mVisitReasonAutoCompleteTextView.setThreshold(2);
            mVisitReasonAutoCompleteTextView.setAdapter(adapter);
            mVisitReasonAutoCompleteTextView.setDropDownBackgroundResource(R.drawable.popup_menu_background);

            mVisitReasonAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    String name = NodeAdapterUtils.getEngChiefComplainNameOnly((String) adapterView.getItemAtPosition(position));
                    if (!name.isEmpty()) {
                        ReasonData data = new ReasonData();
                        data.setReasonName(name);
                        data.setReasonNameLocalized(NodeAdapterUtils.getTheChiefComplainNameWRTLocale(getActivity(), name));
                        boolean isExist = false;
                        for (int i = 0; i < mSelectedComplains.size(); i++) {
                            if (mSelectedComplains.get(i).getReasonName().equalsIgnoreCase(name)) {
                                isExist = true;
                                break;
                            }
                        }
                        if (!isExist) {
                            //mSelectedComplains.clear(); //TODO: Need to remove this line in next release after fixing the multiple MMs crash issue
                            mSelectedComplains.add(data);
                        } else
                            Toast.makeText(getActivity(), getString(R.string.already_selected_lbl), Toast.LENGTH_SHORT).show();

                        // cross check for list also to keep on sync both selected
                        for (int i = 0; i < mVisitReasonItemList.size(); i++) {
                            List<ReasonData> reasonDataList = mVisitReasonItemList.get(i).getReasons();
                            for (int j = 0; j < reasonDataList.size(); j++) {
                                ReasonData reasonData = reasonDataList.get(j);
                                if (reasonData.getReasonName().equalsIgnoreCase(name)) {
                                    mVisitReasonItemList.get(i).getReasons().get(j).setSelected(true);
                                    break; //TODO: Need to remove this line in next release after fixing the multiple MMs crash issue
                                }/*else{ //TODO: Need to remove this line in next release after fixing the multiple MMs crash issue
                                mVisitReasonItemList.get(i).getReasons().get(j).setSelected(false);
                            }*/
                            }
                        }
                        mReasonListingAdapter.refresh(mVisitReasonItemList);

                        showSelectedComplains();
                        mVisitReasonAutoCompleteTextView.setText("");
                        WindowsUtils.hideSoftKeyboard((AppCompatActivity) getActivity());
                    }
                }
            });
       /* mClearImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSelectedComplains.clear();
                mEmptyReasonLabelTextView.setVisibility(View.VISIBLE);
                mClearImageView.setVisibility(View.GONE);
                mSelectedComplainRecyclerView.setVisibility(View.GONE);
                Toast.makeText(getActivity(), "Selection clear!", Toast.LENGTH_SHORT).show();
            }
        });*/

            RecyclerView recyclerView = view.findViewById(R.id.rcv_all_reason);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            mVisitReasonItemList = getVisitReasonList();
            mReasonListingAdapter = new ReasonListingAdapter(recyclerView, getActivity(), mVisitReasonItemList, new ReasonListingAdapter.OnItemSelection() {
                @Override
                public void onSelect(ReasonData data) {
                    if (!mSelectedComplains.contains(data)) {
                        //mSelectedComplains.clear(); //TODO: Need to remove this line in next release after fixing the multiple MMs crash issue
                        mSelectedComplains.add(data);
                        showSelectedComplains();
                        //TODO: Need to remove this line in next release after fixing the multiple MMs crash issue
                    /*for (int i = 0; i < mVisitReasonItemList.size(); i++) {
                        List<ReasonData> reasonDataList = mVisitReasonItemList.get(i).getReasons();
                        for (int j = 0; j < reasonDataList.size(); j++) {
                            ReasonData reasonData = reasonDataList.get(j);
                            if (reasonData.getReasonName().equalsIgnoreCase(data.getReasonName())) {
                                mVisitReasonItemList.get(i).getReasons().get(j).setSelected(true);
                                break; //TODO: Need to remove this line in next release after fixing the multiple MMs crash issue
                            }else{ //TODO: Need to remove this line in next release after fixing the multiple MMs crash issue
                                mVisitReasonItemList.get(i).getReasons().get(j).setSelected(false);
                            }
                        }
                    }
                    mReasonListingAdapter.refresh(mVisitReasonItemList);*/
                        //TODO: EDN
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.already_selected_lbl), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            recyclerView.setAdapter(mReasonListingAdapter);

            btnCancel = view.findViewById(R.id.btn_cancel);
            btnSubmit = view.findViewById(R.id.btn_submit);
            manageBackButtonVisibility();

        }
        return view;
    }


    private void showConfirmDialog() {
        DialogUtils dialogUtils = new DialogUtils();
        dialogUtils.showCommonDialogWithChipsGrid(getActivity(), new ArrayList<ReasonData>(mSelectedComplains), R.drawable.ui2_visit_reason_summary_icon, getResources().getString(R.string.confirm_visit_reason), getResources().getString(R.string.are_you_sure_the_patient_has_the_following_reasons_for_a_visit), false, getResources().getString(R.string.yes), getResources().getString(R.string.no), new DialogUtils.CustomDialogListener() {
            @Override
            public void onDialogActionDone(int action) {
                if (action == DialogUtils.CustomDialogListener.POSITIVE_CLICK) {
                    mActionListener.onFormSubmitted(VisitCreationActivity.STEP_3_VISIT_REASON_QUESTION, false, new ArrayList<ReasonData>(mSelectedComplains)); // send the selected mms
                }
            }
        });
    }

    private void showSelectedComplains() {
        if (mSelectedComplains.isEmpty()) {
            mEmptyReasonLabelTextView.setVisibility(View.VISIBLE);
            //mClearImageView.setVisibility(View.GONE);
            mSelectedComplainRecyclerView.setVisibility(View.GONE);
        } else {
            mEmptyReasonLabelTextView.setVisibility(View.GONE);
            //mClearImageView.setVisibility(View.VISIBLE);
            mSelectedComplainRecyclerView.setVisibility(View.VISIBLE);
        }


        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getActivity());
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);

        mSelectedComplainRecyclerView.setLayoutManager(layoutManager);
        SelectedChipsGridAdapter reasonChipsGridAdapter = new SelectedChipsGridAdapter(mSelectedComplainRecyclerView, getActivity(), new ArrayList<ReasonData>(mSelectedComplains), new SelectedChipsGridAdapter.OnItemSelection() {
            @Override
            public void onSelect(ReasonData data) {

            }

            @Override
            public void onRemoved(ReasonData data) {
                mSelectedComplains.remove(data);
                for (int i = 0; i < mVisitReasonItemList.size(); i++) {
                    List<ReasonData> reasonDataList = mVisitReasonItemList.get(i).getReasons();
                    for (int j = 0; j < reasonDataList.size(); j++) {
                        ReasonData reasonData = reasonDataList.get(j);
                        if (reasonData.getReasonName().equalsIgnoreCase(data.getReasonName())) {
                            mVisitReasonItemList.get(i).getReasons().get(j).setSelected(false);
                            break;
                        }
                    }
                }
                mReasonListingAdapter.refresh(mVisitReasonItemList);
                showSelectedComplains();
            }
        });
        mSelectedComplainRecyclerView.setAdapter(reasonChipsGridAdapter);

        /*for (String value : mSelectedComplains) {
            View itemView = View.inflate(getActivity(), R.layout.ui2_chips_for_reason_item_view, null);
            TextView nameTextView = itemView.findViewById(R.id.tv_name);
            nameTextView.setBackgroundResource(R.drawable.ui2_common_primary_bg);
            nameTextView.setTextColor(getResources().getColor(R.color.white));
            nameTextView.setText(value);
            mSelectedComplainLinearLayout.addView(itemView);
            Space space = new Space(getActivity());
            space.setMinimumWidth(16);
            mSelectedComplainLinearLayout.addView(space);
        }*/
    }

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

    /**
     * @return
     */
    private String[] getVisitReasonFiles() {
        String[] fileNames = new String[0];
        try {
            if (!sessionManager.getLicenseKey().isEmpty()) {
                File base_dir = new File(requireActivity().getFilesDir().getAbsolutePath() + File.separator + AppConstants.JSON_FOLDER);
                File[] files = base_dir.listFiles();
                fileNames = new String[files.length];
                for (int i = 0; i < files.length; i++) {
                    fileNames[i] = files[i].getName();
                }
            } else {
                fileNames = getActivity().getApplicationContext().getAssets().list("engines");

            }
        } catch (IOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        return fileNames;
    }

    /**
     * @return
     */
    private List<ReasonGroupData> getVisitReasonList() {
        List<ReasonGroupData> itemList = new ArrayList<>();

        char startC = NodeAdapterUtils.getStartCharAsPerLocale();
        char endC = NodeAdapterUtils.getEndCharAsPerLocale();

        for (char c = startC; c <= endC; ++c) {
            ReasonGroupData reasonGroupData = new ReasonGroupData();
            reasonGroupData.setAlphabet(String.valueOf(c));
            List<ReasonData> list = new ArrayList<ReasonData>();
            for (ReasonData reasonData : mRawReasonDataList) {
                //String chiefComplainNameByLocale =  NodeAdapterUtils.getTheChiefComplainNameWRTLocale(getActivity(), fileName);
                if (reasonData.getReasonNameLocalized().toUpperCase().startsWith(String.valueOf(c))) {
                    //ReasonData reasonData = new ReasonData();
                    //reasonData.setReasonName(fileName);
//                  // TODO: we are adding this below conditions for keeping these protocol enable for selection
                    reasonData.setEnabled(mFinalEnabledMMList.contains(reasonData.getReasonName()));
                    //reasonData.setReasonNameLocalized(chiefComplainNameByLocale);
                    list.add(reasonData);
                }
            }
            reasonGroupData.setReasons(list);
            if (!list.isEmpty()) {
                itemList.add(reasonGroupData);
            }
        }


        return itemList;
    }
    private void manageBackButtonVisibility() {
        boolean vitalsActiveStatus = ((VisitCreationActivity) requireActivity()).getFeatureActiveStatus().getVitalSection();
        boolean diagnosticsActiveStatus = ((VisitCreationActivity) requireActivity()).getFeatureActiveStatus().getActiveStatusDiagnosticsSection();
        btnCancel.setVisibility((vitalsActiveStatus || diagnosticsActiveStatus) ? View.VISIBLE : View.GONE);
        if (btnCancel.getVisibility() == View.GONE) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) btnSubmit.getLayoutParams();
            params.width = LinearLayout.LayoutParams.MATCH_PARENT;
            params.weight = 0f;
            params.setMargins(0, params.topMargin, params.rightMargin, params.bottomMargin);
            btnSubmit.setLayoutParams(params);
        } else {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) btnSubmit.getLayoutParams();
            params.width = 0;
            params.weight = 1f;
            params.setMargins(16, params.topMargin, params.rightMargin, params.bottomMargin);
            btnSubmit.setLayoutParams(params);
        }
    }


}