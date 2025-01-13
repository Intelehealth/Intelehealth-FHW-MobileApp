package org.intelehealth.app.ayu.visit.reason;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import org.intelehealth.app.utilities.CustomLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.ayu.visit.VisitCreationActionListener;
import org.intelehealth.app.ayu.visit.VisitCreationActivity;
import org.intelehealth.app.ayu.visit.common.OnItemSelection;
import org.intelehealth.app.ayu.visit.common.VisitUtils;
import org.intelehealth.app.ayu.visit.common.adapter.QuestionsListingAdapter;
import org.intelehealth.app.ayu.visit.model.CommonVisitData;
import org.intelehealth.app.ayu.visit.model.ComplainBasicInfo;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.models.AnswerResult;
import org.intelehealth.app.utilities.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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


    public static VisitReasonQuestionsFragment newInstance(CommonVisitData commonVisitData, boolean isEditMode, List<Node> nodeList) {
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

        if (mIsEditMode) {
            view.findViewById(R.id.ll_footer).setVisibility(View.VISIBLE);
            view.findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    mActionListener.onFormSubmitted(VisitCreationActivity.STEP_3_VISIT_REASON_QUESTION_SUMMARY, mIsEditMode, null);

                }
            });
            view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mIsEditMode && ((VisitCreationActivity) requireActivity()).isEditTriggerFromVisitSummary()) {
                        requireActivity().setResult(Activity.RESULT_OK);
                        requireActivity().finish();
                    }
                }
            });
        }
        RecyclerView recyclerView = view.findViewById(R.id.rcv_questions);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setStackFromEnd(!mIsEditMode);
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        DefaultOnItemSelection itemSelectionListener = new DefaultOnItemSelection(recyclerView);
        //mCurrentRootOptionList = mCurrentNode.getOptionsList();

        for (int i = 0; i < mChiefComplainRootNodeList.size(); i++) {
            CustomLog.v("VISIT_REASON", new Gson().toJson(mChiefComplainRootNodeList.get(i)));
            ComplainBasicInfo complainBasicInfo = new ComplainBasicInfo();
            complainBasicInfo.setComplainName(mChiefComplainRootNodeList.get(i).getText());
            complainBasicInfo.setComplainNameByLocale(mChiefComplainRootNodeList.get(i).findDisplay());
            complainBasicInfo.setOptionSize(mChiefComplainRootNodeList.get(i).getOptionsList().size());
            if (complainBasicInfo.getComplainName().equalsIgnoreCase(Node.ASSOCIATE_SYMPTOMS)) {
                complainBasicInfo.setComplainNameByLocale(getString(R.string.associated_symptoms_header_visit_creation));
                complainBasicInfo.setAssociateSymptom(true);
            }
            mRootComplainBasicInfoHashMap.put(i, complainBasicInfo);
        }
        mQuestionsListingAdapter = new QuestionsListingAdapter(recyclerView, getActivity(), false, false, null, mCurrentComplainNodeIndex, mRootComplainBasicInfoHashMap, mIsEditMode, itemSelectionListener);
//        mQuestionsListingAdapter = new QuestionsListingAdapter(recyclerView, getActivity(), false, false, null, mCurrentComplainNodeIndex, mRootComplainBasicInfoHashMap, mIsEditMode, new OnItemSelection() {
//            @Override
//            public void onSelect(Node node, int index, boolean isSkipped, Node parentNode) {
//                Log.v("onSelect QuestionsListingAdapter", "index - " + index + " \t mCurrentComplainNodeOptionsIndex - " + mCurrentComplainNodeOptionsIndex);
//                Log.v("onSelect QuestionsListingAdapter", "node - " + node.getText());
//                // avoid the scroll for old data change
//                if (mCurrentComplainNodeOptionsIndex - index >= 1) {
//                    Log.v("onSelect", "Scrolling index - " + index);
//                    VisitUtils.scrollNow(recyclerView, 100, 0, 1000, mIsEditMode);
//                    return;
//                }
//                if (isSkipped) {
//                    boolean isRequiredToUnselectParent = mQuestionsListingAdapter.geItems().get(index).getOptionsList() == null || mQuestionsListingAdapter.geItems().get(index).size() <= 1;
//                    if (isRequiredToUnselectParent) {
//                        mQuestionsListingAdapter.geItems().get(index).setSelected(false);
//                        mQuestionsListingAdapter.geItems().get(index).setDataCaptured(false);
//                    }
//
//                    if (mQuestionsListingAdapter.geItems().get(index).getOptionsList() != null && mQuestionsListingAdapter.geItems().get(index).getOptionsList().size() > 0)
//                        for (int i = 0; i < mQuestionsListingAdapter.geItems().get(index).getOptionsList().size(); i++) {
//                            mQuestionsListingAdapter.geItems().get(index).getOptionsList().get(i).setSelected(false);
//                            mQuestionsListingAdapter.geItems().get(index).getOptionsList().get(i).setDataCaptured(false);
//                        }
//                    if (mQuestionsListingAdapter.geItems().get(index).isRequired()) {
//                        mQuestionsListingAdapter.notifyItemChanged(index);
//                        return;
//                    } else {
//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                mQuestionsListingAdapter.notifyItemChanged(index);
//                            }
//                        }, 1000);
//                    }
//                }
//
//                completeProcess(this, index);
////                if (mCurrentComplainNodeOptionsIndex < mCurrentNode.getOptionsList().size() - 1)
////                    mCurrentComplainNodeOptionsIndex++;
////                else {
////                    if (mChiefComplainRootNodeList.size() == 1) mCurrentComplainNodeIndex = 0;
////                    else mCurrentComplainNodeIndex += 1;
////                    mCurrentComplainNodeOptionsIndex = 0;
////                    mQuestionsListingAdapter.setRootNodeIndex(mCurrentComplainNodeIndex);
////                    mCurrentNode = mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex);
////                }
////                if (mRootComplainBasicInfoHashMap.get(mCurrentComplainNodeIndex).isAssociateSymptom()) {
////                    //linearLayoutManager.setStackFromEnd(false);
////                    if (!mQuestionsListingAdapter.isIsAssociateSymptomsLoaded())
////                        mQuestionsListingAdapter.addItem(mCurrentNode, mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex).getEngineVersion());
////                    mQuestionsListingAdapter.setAssociateSymptomsLoaded(true);
////                } else {
////                    //linearLayoutManager.setStackFromEnd(false);
////                    mQuestionsListingAdapter.addItem(mCurrentNode.getOptionsList().get(mCurrentComplainNodeOptionsIndex), mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex).getEngineVersion());
////                }
//
//                VisitUtils.scrollNow(recyclerView, 300, 0, 500, mIsEditMode);
//
//                VisitUtils.scrollNow(recyclerView, 1400, 0, 1400, mIsEditMode);
//
//
//                mActionListener.onProgress((int) 60 / mCurrentNode.getOptionsList().size());
//                if (!mIsEditMode) linearLayoutManager.setStackFromEnd(false);
//            }
//
//            @Override
//            public void needTitleChange(String title) {
//                mActionListener.onTitleChange(title);
//            }
//
//            @Override
//            public void onAllAnswered(boolean isAllAnswered) {
//                if (!mIsEditMode)
//                    mActionListener.onFormSubmitted(VisitCreationActivity.STEP_2_VISIT_REASON_QUESTION_SUMMARY, mIsEditMode, null);
//                else
//                    Toast.makeText(getActivity(), getString(R.string.please_submit_to_proceed_next_step), Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onCameraRequest() {
//
//            }
//
//            @Override
//            public void onImageRemoved(int nodeIndex, int imageIndex, String image) {
//
//            }
//        });

        recyclerView.setAdapter(mQuestionsListingAdapter);
        mQuestionsListingAdapter.setRootNodeIndex(mCurrentComplainNodeIndex);
        mQuestionsListingAdapter.addItem(mCurrentNode.getOptionsList().get(mCurrentComplainNodeOptionsIndex), mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex).getEngineVersion());

        if (mIsEditMode) {
            boolean pendingForAddAll = true;
            while (pendingForAddAll) {

                pendingForAddAll = completeProcess(itemSelectionListener, mCurrentComplainNodeOptionsIndex);
//                if (mCurrentComplainNodeOptionsIndex < mCurrentNode.getOptionsList().size() - 1)
//                    mCurrentComplainNodeOptionsIndex++;
//                else {
//                    mCurrentComplainNodeOptionsIndex = 0;
//                    mCurrentComplainNodeIndex += 1;
//                    mQuestionsListingAdapter.setRootNodeIndex(mCurrentComplainNodeIndex);
//                    mCurrentNode = mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex);
//                }
//                if (mRootComplainBasicInfoHashMap.get(mCurrentComplainNodeIndex).isAssociateSymptom()) {
//                    //linearLayoutManager.setStackFromEnd(false);
//                    if (!mQuestionsListingAdapter.isIsAssociateSymptomsLoaded())
//                        mQuestionsListingAdapter.addItem(mCurrentNode, mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex).getEngineVersion());
//                    mQuestionsListingAdapter.setAssociateSymptomsLoaded(true);
//                    pendingForAddAll = false;
//                } else {
//                    //linearLayoutManager.setStackFromEnd(true);
//                    mQuestionsListingAdapter.addItem(mCurrentNode.getOptionsList().get(mCurrentComplainNodeOptionsIndex), mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex).getEngineVersion());
//                }
            }
        }
        return view;
    }
    private Set<String> mLoadedIds = new HashSet<String>();
    private class DefaultOnItemSelection implements OnItemSelection {

        private final RecyclerView recyclerView;

        public DefaultOnItemSelection(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
        }

        @Override
        public void onSelect(Node node, int index, boolean isSkipped, Node parentNode) {
            CustomLog.v("onSelect QuestionsListingAdapter", "index - " + index + " \t mCurrentComplainNodeOptionsIndex - " + mCurrentComplainNodeOptionsIndex);
            CustomLog.v("onSelect QuestionsListingAdapter", "node - " + node.getText());
            // avoid the scroll for old data change
            String parentRootQuestionId = mQuestionsListingAdapter.geItems().get(index).getId();
            String parentRootQuestionDisplay = mQuestionsListingAdapter.geItems().get(index).findDisplay();
            CustomLog.v("onSelect", "parentRootQuestionId - " + parentRootQuestionId);
            CustomLog.v("onSelect", "parentRootQuestionDisplay - " + parentRootQuestionDisplay);

            if (mCurrentComplainNodeOptionsIndex - index >= 1) {
//                mLoadedIds.add(parentRootQuestionId);
//                Log.v("onSelect", "Scrolling index - " + index);
//                VisitUtils.scrollNow(recyclerView, 100, 0, 1000, mIsEditMode, mLoadedIds.contains(parentRootQuestionId));
                return;
            }

            CustomLog.v("onSelect QuestionsListingAdapter", "mLoadedIds - " + mLoadedIds);
            if (isSkipped) {
                boolean isRequiredToUnselectParent = mQuestionsListingAdapter.geItems().get(index).getOptionsList() == null || mQuestionsListingAdapter.geItems().get(index).size() <= 1;
                if (isRequiredToUnselectParent) {
                    mQuestionsListingAdapter.geItems().get(index).setSelected(false);
                    mQuestionsListingAdapter.geItems().get(index).setDataCaptured(false);
                }

                if (mQuestionsListingAdapter.geItems().get(index).getOptionsList() != null && mQuestionsListingAdapter.geItems().get(index).getOptionsList().size() > 0)
                    for (int i = 0; i < mQuestionsListingAdapter.geItems().get(index).getOptionsList().size(); i++) {
                        mQuestionsListingAdapter.geItems().get(index).getOptionsList().get(i).setSelected(false);
                        mQuestionsListingAdapter.geItems().get(index).getOptionsList().get(i).setDataCaptured(false);
                    }
                if (mQuestionsListingAdapter.geItems().get(index).isRequired()) {
                    mQuestionsListingAdapter.notifyItemChanged(index);
                    return;
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mQuestionsListingAdapter.notifyItemChanged(index);
                        }
                    }, 1000);
                }
            }


            completeProcess(this, index);
//            if (mCurrentComplainNodeOptionsIndex < mCurrentNode.getOptionsList().size() - 1)
//                mCurrentComplainNodeOptionsIndex++;
//            else {
//                // here we have only Women's Health chief complain and not more than one
//                // so this code written for more then 1 chief complain which is not require here
//                if (mChiefComplainRootNodeList.size() > 1) {
//                    mCurrentComplainNodeOptionsIndex = 0;
//                    mCurrentComplainNodeIndex += 1;
//                    mQuestionsListingAdapter.setRootNodeIndex(mCurrentComplainNodeIndex);
//                    mCurrentNode = mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex);
//                } else {
//                    AnswerResult allAnswered = mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex).checkAllRequiredAnsweredRootNode(requireContext());
//                    if (allAnswered.result) onAllAnswered(true);
//                }
//            }
//            if (mRootComplainBasicInfoHashMap.get(mCurrentComplainNodeIndex).isAssociateSymptom()) {
//                //linearLayoutManager.setStackFromEnd(false);
//                if (!mQuestionsListingAdapter.isIsAssociateSymptomsLoaded())
//                    mQuestionsListingAdapter.addItem(mCurrentNode, mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex).getEngineVersion());
//                mQuestionsListingAdapter.setAssociateSymptomsLoaded(true);
//            } else {
//                //linearLayoutManager.setStackFromEnd(false);
//                if (mCurrentComplainNodeOptionsIndex != index)
//                    mQuestionsListingAdapter.addItem(mCurrentNode.getOptionsList().get(mCurrentComplainNodeOptionsIndex), mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex).getEngineVersion());
//            }

            VisitUtils.scrollNow(recyclerView, 300, 0, 500, mIsEditMode, mLoadedIds.contains(parentRootQuestionId));

            VisitUtils.scrollNow(recyclerView, 1400, 0, 1400, mIsEditMode, mLoadedIds.contains(parentRootQuestionId));


            mActionListener.onProgress((int) 60 / mCurrentNode.getOptionsList().size());
            ((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).setStackFromEnd(false);
            mLoadedIds.add(parentRootQuestionId);
        }

        @Override
        public void needTitleChange(String title) {
            mActionListener.onTitleChange(title);
        }

        @Override
        public void onAllAnswered(boolean isAllAnswered) {
            if (!mIsEditMode)
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_3_VISIT_REASON_QUESTION_SUMMARY, mIsEditMode, null);
            else
                Toast.makeText(getActivity(), getString(R.string.please_submit_to_proceed_next_step), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCameraRequest() {

        }

        @Override
        public void onImageRemoved(int nodeIndex, int imageIndex, String image) {

        }

        @Override
        public void onTerminalNodeAnsweredForParentUpdate(String parentNodeId) {

        }
    }

    private boolean completeProcess(OnItemSelection itemSelection, int index) {
        if (mCurrentComplainNodeOptionsIndex < mCurrentNode.getOptionsList().size() - 1)
            mCurrentComplainNodeOptionsIndex++;
        else {
            // here we have only Women's Health chief complain and not more than one
            // so this code written for more then 1 chief complain which is not require here
            if (mCurrentComplainNodeIndex < mChiefComplainRootNodeList.size() - 1) {
                mCurrentComplainNodeOptionsIndex = 0;
                mCurrentComplainNodeIndex += 1;
                mQuestionsListingAdapter.setRootNodeIndex(mCurrentComplainNodeIndex);
                mCurrentNode = mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex);
            } else {
                AnswerResult allAnswered = mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex).checkAllRequiredAnsweredRootNode(requireContext());
                if (allAnswered.result) itemSelection.onAllAnswered(true);
                return false;
            }
        }
        if (mRootComplainBasicInfoHashMap.get(mCurrentComplainNodeIndex).isAssociateSymptom()) {
            //linearLayoutManager.setStackFromEnd(false);
            if (!mQuestionsListingAdapter.isIsAssociateSymptomsLoaded())
                mQuestionsListingAdapter.addItem(mCurrentNode, mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex).getEngineVersion());
            mQuestionsListingAdapter.setAssociateSymptomsLoaded(true);
            return false;
        } else {
            //linearLayoutManager.setStackFromEnd(false);
            if (mCurrentComplainNodeOptionsIndex != index) {
                mQuestionsListingAdapter.addItem(mCurrentNode.getOptionsList().get(mCurrentComplainNodeOptionsIndex),
                        mChiefComplainRootNodeList.get(mCurrentComplainNodeIndex).getEngineVersion());
                return true;
            } else return false;
        }
    }
}