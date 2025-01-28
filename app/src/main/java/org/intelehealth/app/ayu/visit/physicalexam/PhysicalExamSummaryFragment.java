package org.intelehealth.app.ayu.visit.physicalexam;

import static org.intelehealth.app.ayu.visit.VisitCreationActivity.STEP_3_VISIT_REASON_QUESTION_SUMMARY;
import static org.intelehealth.app.ayu.visit.VisitCreationActivity.STEP_4_PHYSICAL_SUMMARY_EXAMINATION;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import org.intelehealth.app.BuildConfig;

import org.intelehealth.app.ayu.visit.common.ManageSummaryScreenTitles;
import org.intelehealth.app.utilities.CustomLog;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.ayu.visit.VisitCreationActionListener;
import org.intelehealth.app.ayu.visit.VisitCreationActivity;
import org.intelehealth.app.ayu.visit.common.adapter.SummaryViewAdapter;
import org.intelehealth.app.ayu.visit.model.CommonVisitData;
import org.intelehealth.app.ayu.visit.model.VisitSummaryData;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.FlavorKeys;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.config.room.entity.FeatureActiveStatus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PhysicalExamSummaryFragment extends Fragment {
    private List<Node> mAnsweredRootNodeList = new ArrayList<>();
    private List<List<VisitSummaryData>> mAllItemList = new ArrayList<>();
    private List<VisitSummaryData> mItemList = new ArrayList<VisitSummaryData>();
    private LinearLayout mSummaryLinearLayout;
    private VisitCreationActionListener mActionListener;
    SessionManager sessionManager;
    private boolean mIsEditMode = false;

    public PhysicalExamSummaryFragment() {
        // Required empty public constructor
    }

    private String mSummaryString;

    public static PhysicalExamSummaryFragment newInstance(CommonVisitData commonVisitData, String values, boolean isEditMode) {
        PhysicalExamSummaryFragment fragment = new PhysicalExamSummaryFragment();
        fragment.mSummaryString = values;
        fragment.mIsEditMode = isEditMode;
        //fragment.prepareSummary();
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActionListener = (VisitCreationActionListener) context;
        sessionManager = new SessionManager(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
       /* FeatureActiveStatus status = ((VisitCreationActivity) requireActivity()).getFeatureActiveStatus();
        int index = status.getVitalSection() ? 4 : 3;
        int total = status.getVitalSection() ? 5 : 4;
        TextView tvTitle = view.findViewById(R.id.tv_sub_title);
        tvTitle.setText(getString(R.string.ui2_relapse_summary_title, index, total));*/
        FeatureActiveStatus status = ((VisitCreationActivity) requireActivity()).getFeatureActiveStatus();
        String title = ManageSummaryScreenTitles.setScreenTitle(requireActivity(), status, STEP_4_PHYSICAL_SUMMARY_EXAMINATION);
        TextView tvTitle = view.findViewById(R.id.tv_sub_title);
        tvTitle.setText(title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_physical_exam_summary, container, false);
        mSummaryLinearLayout = view.findViewById(R.id.ll_summary);
        view.findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsEditMode && ((VisitCreationActivity) requireActivity()).isEditTriggerFromVisitSummary()) {
                    requireActivity().setResult(Activity.RESULT_OK);
                    requireActivity().finish();
                } else{
                    if(BuildConfig.FLAVOR_client == FlavorKeys.UNFPA){
                        mActionListener.onFormSubmitted(VisitCreationActivity.FROM_SUMMARY_RESUME_BACK_FOR_EDIT, mIsEditMode, VisitCreationActivity.STEP_6_FAMILY_HISTORY);
                    }else {
                        mActionListener.onFormSubmitted(VisitCreationActivity.STEP_5_PAST_MEDICAL_HISTORY, mIsEditMode, null);
                    }
                }

            }
        });

        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.onFormSubmitted(VisitCreationActivity.FROM_SUMMARY_RESUME_BACK_FOR_EDIT, true, VisitCreationActivity.STEP_4_PHYSICAL_EXAMINATION);
            }
        });
        view.findViewById(R.id.img_btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionListener.onFormSubmitted(VisitCreationActivity.FROM_SUMMARY_RESUME_BACK_FOR_EDIT, true, VisitCreationActivity.STEP_4_PHYSICAL_EXAMINATION);
            }
        });
        view.findViewById(R.id.imb_btn_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkConnection.isOnline(requireActivity())) {
                    new SyncUtils().syncBackground();
                    // Toast.makeText(requireActivity(), getString(R.string.sync_strated), Toast.LENGTH_SHORT).show();
                }
            }
        });
        prepareSummary();

        return view;
    }

    private void prepareSummary() {
        mSummaryLinearLayout.removeAllViews();
        String str = mSummaryString;//"►<b>Abdominal Pain</b>: <br/>• Site - Upper (C) - Epigastric.<br/>• Pain radiates to - Middle (R) - Right Lumbar.<br/>• Onset - Gradual.<br/>• Timing - Morning.<br/>• Character of the pain - Constant.<br/>• Severity - Mild, 1-3.<br/>• Exacerbating Factors - Hunger.<br/>• Relieving Factors - Food.<br/>• Prior treatment sought - None.<br/> ►<b>Associated symptoms</b>: <br/>• Patient reports -<br/> Anorexia <br/>• Patient denies -<br/> Diarrhea,  Constipation,  Fever<br/>";
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
                View view = View.inflate(requireActivity(), R.layout.ui2_summary_main_row_item_view, null);
                TextView complainLabelTextView = view.findViewById(R.id.tv_complain_label);
                view.findViewById(R.id.height_adjust_view).setVisibility(View.GONE);
                complainLabelTextView.setText(_complain);
                view.findViewById(R.id.tv_change).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mActionListener.onFormSubmitted(VisitCreationActivity.FROM_SUMMARY_RESUME_BACK_FOR_EDIT, true, VisitCreationActivity.STEP_4_PHYSICAL_EXAMINATION);
                    }
                });
                RecyclerView recyclerView = view.findViewById(R.id.rcv_qa);
                recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false));
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
                        if (v.endsWith(",")) {
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


                SummaryViewAdapter summaryViewAdapter = new SummaryViewAdapter(recyclerView, requireActivity(), visitSummaryDataList, new SummaryViewAdapter.OnItemSelection() {

                    @Override
                    public void onSelect(VisitSummaryData data) {

                    }
                });
                recyclerView.setAdapter(summaryViewAdapter);
               /* SummarySingleViewAdapter summaryViewAdapter = new SummarySingleViewAdapter(recyclerView, requireActivity(), _list, new SummarySingleViewAdapter.OnItemSelection() {
                    @Override
                    public void onSelect(String data) {

                    }
                });
                recyclerView.setAdapter(summaryViewAdapter);*/
                mSummaryLinearLayout.addView(view);
            }
        }

    }


}