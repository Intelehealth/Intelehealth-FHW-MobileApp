package org.intelehealth.nak.ayu.visit.reason;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

import org.intelehealth.nak.R;
import org.intelehealth.nak.ayu.visit.VisitCreationActionListener;
import org.intelehealth.nak.ayu.visit.VisitCreationActivity;
import org.intelehealth.nak.ayu.visit.common.VisitUtils;
import org.intelehealth.nak.ayu.visit.common.adapter.NodeAdapterUtils;
import org.intelehealth.nak.ayu.visit.model.ReasonData;
import org.intelehealth.nak.ayu.visit.model.ReasonGroupData;
import org.intelehealth.nak.ayu.visit.reason.adapter.ReasonListingAdapter;
import org.intelehealth.nak.ayu.visit.reason.adapter.SelectedChipsGridAdapter;
import org.intelehealth.nak.knowledgeEngine.Node;
import org.intelehealth.nak.utilities.DialogUtils;
import org.intelehealth.nak.utilities.FileUtils;
import org.intelehealth.nak.utilities.SessionManager;
import org.intelehealth.nak.utilities.WindowsUtils;
import org.json.JSONObject;

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

    public static VisitReasonCaptureFragment newInstance(Intent intent, boolean isEditMode, boolean cleanEdit) {
        VisitReasonCaptureFragment fragment = new VisitReasonCaptureFragment();
        fragment.mIsEditMode = isEditMode;
        fragment.patientUuid = intent.getStringExtra("patientUuid");
        fragment.visitUuid = intent.getStringExtra("visitUuid");
        fragment.encounterVitals = intent.getStringExtra("encounterUuidVitals");
        fragment.encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
        fragment.EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");
        fragment.state = intent.getStringExtra("state");
        fragment.patientName = intent.getStringExtra("name");
        fragment.patientGender = intent.getStringExtra("gender");
        fragment.intentTag = intent.getStringExtra("tag");
        //fragment.mEditFor = intent.getIntExtra("edit_for", STEP_1_VITAL);
        fragment.float_ageYear_Month = intent.getFloatExtra("float_ageYear_Month", 0);
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
                if (mSelectedComplains.isEmpty()) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.please_select_at_least_one_complaint), Toast.LENGTH_SHORT).show();
                    return;
                }
                showConfirmDialog();
            }
        });

        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_1_VITAL_SUMMARY, false, null);
            }
        });

        // TODO: we are adding this below string array for keeping these two protocol enable for search also
        mFinalEnabledMMList.clear();
        String[] mindmapsNames = new String[]{"Abdominal Pain", "Diarrhea", "Fever", "Hypertension"};//, "Menstrual disorder"};//getVisitReasonFilesNamesOnly();

        for (String mindmapsName : mindmapsNames) {
            String fileLocation = "engines/" + mindmapsName + ".json";
            JSONObject currentFile = FileUtils.encodeJSON(getActivity(), fileLocation);
            Node mainNode = new Node(currentFile);
            if (VisitUtils.checkNodeValidByGenderAndAge(patientGender, float_ageYear_Month, mainNode.getGender(), mainNode.getMin_age(), mainNode.getMax_age())) {
                mFinalEnabledMMList.add(mindmapsName);
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

        return view;
    }

    private void showConfirmDialog() {
        DialogUtils dialogUtils = new DialogUtils();
        dialogUtils.showCommonDialogWithChipsGrid(getActivity(), new ArrayList<ReasonData>(mSelectedComplains), R.drawable.ui2_visit_reason_summary_icon, getResources().getString(R.string.confirm_visit_reason), getResources().getString(R.string.are_you_sure_the_patient_has_the_following_reasons_for_a_visit), false, getResources().getString(R.string.yes), getResources().getString(R.string.no), new DialogUtils.CustomDialogListener() {
            @Override
            public void onDialogActionDone(int action) {
                if (action == DialogUtils.CustomDialogListener.POSITIVE_CLICK) {
                    mActionListener.onFormSubmitted(VisitCreationActivity.STEP_2_VISIT_REASON_QUESTION, false, new ArrayList<ReasonData>(mSelectedComplains)); // send the selected mms
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
            String[] temp = getActivity().getApplicationContext().getAssets().list("engines");
            for (String s : temp) {
                String fileName = s.split(".json")[0];
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
            fileNames = getActivity().getApplicationContext().getAssets().list("engines");
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


}