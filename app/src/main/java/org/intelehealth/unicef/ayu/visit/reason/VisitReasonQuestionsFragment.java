package org.intelehealth.unicef.ayu.visit.reason;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.ayu.visit.VisitCreationActionListener;
import org.intelehealth.unicef.ayu.visit.VisitCreationActivity;
import org.intelehealth.unicef.ayu.visit.common.OnItemSelection;
import org.intelehealth.unicef.ayu.visit.common.VisitUtils;
import org.intelehealth.unicef.ayu.visit.common.adapter.QuestionsListingAdapter;
import org.intelehealth.unicef.ayu.visit.model.ComplainBasicInfo;
import org.intelehealth.unicef.knowledgeEngine.Node;
import org.intelehealth.unicef.utilities.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VisitReasonQuestionsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VisitReasonQuestionsFragment extends Fragment {

    private List<String> mSelectedComplains = new ArrayList<>();
    private VisitCreationActionListener mActionListener;
    SessionManager sessionManager;
    private List<Node> mChiefComplainRootNodeList = new ArrayList<>();
    private int mCurrentComplainNodeIndex = 0;
    private Node mCurrentNode;
    private boolean mIsEditMode = false;

    public VisitReasonQuestionsFragment() {
        // Required empty public constructor
    }


    public static VisitReasonQuestionsFragment newInstance(Intent intent, boolean isEditMode, List<Node> nodeList) {
        VisitReasonQuestionsFragment fragment = new VisitReasonQuestionsFragment();
        fragment.mIsEditMode = isEditMode;
        fragment.mChiefComplainRootNodeList = nodeList;
        fragment.mCurrentNode = fragment.mChiefComplainRootNodeList.get(fragment.mCurrentComplainNodeIndex);
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
    }


    //private List<Node> mCurrentRootOptionList = new ArrayList<>();
    private int mCurrentComplainNodeOptionsIndex = 0;
    private QuestionsListingAdapter mQuestionsListingAdapter;
    private HashMap<Integer, ComplainBasicInfo> mRootComplainBasicInfoHashMap = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View view = inflater.inflate(R.layout.fragment_visit_reason_questions, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.rcv_questions);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        //mCurrentRootOptionList = mCurrentNode.getOptionsList();

        for (int i = 0; i < mChiefComplainRootNodeList.size(); i++) {
            Log.v("VISIT_REASON", new Gson().toJson(mChiefComplainRootNodeList.get(i)));
            ComplainBasicInfo complainBasicInfo = new ComplainBasicInfo();
            complainBasicInfo.setComplainName(mChiefComplainRootNodeList.get(i).getText());
            complainBasicInfo.setOptionSize(mChiefComplainRootNodeList.get(i).getOptionsList().size());
            if (complainBasicInfo.getComplainName().equalsIgnoreCase("Associated symptoms"))
                complainBasicInfo.setAssociateSymptom(true);
            mRootComplainBasicInfoHashMap.put(i, complainBasicInfo);
        }
        mQuestionsListingAdapter = new QuestionsListingAdapter(recyclerView, getActivity(), false, null, mCurrentComplainNodeIndex, mRootComplainBasicInfoHashMap, new OnItemSelection() {
            @Override
            public void onSelect(Node node, int index) {
                Log.v("onSelect", "index - " + index + " \t mCurrentComplainNodeOptionsIndex - " + mCurrentComplainNodeOptionsIndex);
                // avoid the scroll for old data change
                if (mCurrentComplainNodeOptionsIndex - index >= 1) {
                    Log.v("onSelect", "Scrolling index - " + index);
                    VisitUtils.scrollNow(recyclerView, 100, 0, 1000);
                    return;
                }
                //Log.v("onSelect", "node - " + node.getText());
                if (mCurrentComplainNodeOptionsIndex < mCurrentNode.getOptionsList().size() - 1)
                    mCurrentComplainNodeOptionsIndex++;
                else {
                    mCurrentComplainNodeOptionsIndex = 0;
                    mCurrentComplainNodeIndex += 1;
                    mQuestionsListingAdapter.setRootNodeIndex(mCurrentComplainNodeIndex);
                    mCurrentNode = mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex);
                }
                if (mRootComplainBasicInfoHashMap.get(mCurrentComplainNodeIndex).isAssociateSymptom()) {
                    linearLayoutManager.setStackFromEnd(false);
                    if (!mQuestionsListingAdapter.isIsAssociateSymptomsLoaded())
                        mQuestionsListingAdapter.addItem(mCurrentNode);
                    mQuestionsListingAdapter.setAssociateSymptomsLoaded(true);
                } else {
                    linearLayoutManager.setStackFromEnd(true);
                    mQuestionsListingAdapter.addItem(mCurrentNode.getOptionsList().get(mCurrentComplainNodeOptionsIndex));
                }

                VisitUtils.scrollNow(recyclerView, 100, 0, 500);

                VisitUtils.scrollNow(recyclerView, 1200, 0, 1000);


                mActionListener.onProgress((int) 60 / mCurrentNode.getOptionsList().size());
            }

            @Override
            public void needTitleChange(String title) {
                mActionListener.onTitleChange(title);
            }

            @Override
            public void onAllAnswered(boolean isAllAnswered) {
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_2_VISIT_REASON_QUESTION_SUMMARY, false, null);
            }

            @Override
            public void onCameraRequest() {

            }

            @Override
            public void onImageRemoved(int index, String image) {

            }
        });

        recyclerView.setAdapter(mQuestionsListingAdapter);
        mQuestionsListingAdapter.setRootNodeIndex(mCurrentComplainNodeIndex);
        mQuestionsListingAdapter.addItem(mCurrentNode.getOptionsList().get(mCurrentComplainNodeOptionsIndex));
        return view;
    }

}