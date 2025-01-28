package org.intelehealth.app.ayu.visit.familyhist;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.app.R;
import org.intelehealth.app.ayu.visit.VisitCreationActionListener;
import org.intelehealth.app.ayu.visit.VisitCreationActivity;
import org.intelehealth.app.ayu.visit.common.OnItemSelection;
import org.intelehealth.app.ayu.visit.common.VisitUtils;
import org.intelehealth.app.ayu.visit.common.adapter.QuestionsListingAdapter;
import org.intelehealth.app.ayu.visit.model.CommonVisitData;
import org.intelehealth.app.ayu.visit.model.ComplainBasicInfo;
import org.intelehealth.app.knowledgeEngine.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FamilyHistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FamilyHistoryFragment extends Fragment {

    public static final String TAG = "FamilyHistoryFragment";
    private List<Node> mCurrentRootOptionList = new ArrayList<>();
    private int mCurrentComplainNodeOptionsIndex = 0;
    private QuestionsListingAdapter mQuestionsListingAdapter;
    private Node mCurrentNode;
    private VisitCreationActionListener mActionListener;
    private boolean mIsEditMode = false;

    public FamilyHistoryFragment() {
        // Required empty public constructor
    }

    private String engineVersion;

    public String getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }


    // TODO: Rename and change types and number of parameters
    public static FamilyHistoryFragment newInstance(CommonVisitData commonVisitData, boolean isEditMode, Node node) {
        FamilyHistoryFragment fragment = new FamilyHistoryFragment();
        fragment.mCurrentNode = node;
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
        //sessionManager = new SessionManager(context);
    }

    private final HashMap<Integer, ComplainBasicInfo> mRootComplainBasicInfoHashMap = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_family_history, container, false);
        if (mCurrentNode != null) {
            RecyclerView recyclerView = view.findViewById(R.id.rcv_questions);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireActivity());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            linearLayoutManager.setStackFromEnd(true);
            linearLayoutManager.setReverseLayout(false);
            linearLayoutManager.setSmoothScrollbarEnabled(true);
            recyclerView.setLayoutManager(linearLayoutManager);
            mCurrentRootOptionList = mCurrentNode.getOptionsList();

            ComplainBasicInfo complainBasicInfo = new ComplainBasicInfo();
            complainBasicInfo.setComplainName("Family History");
            complainBasicInfo.setOptionSize(mCurrentRootOptionList.size());
            complainBasicInfo.setFamilyHistory(true);
            mRootComplainBasicInfoHashMap.put(0, complainBasicInfo);

            if (mIsEditMode) {
                view.findViewById(R.id.ll_footer).setVisibility(View.VISIBLE);
                if (!((VisitCreationActivity) requireActivity()).isEditTriggerFromVisitSummary()) {
                    view.findViewById(R.id.btn_cancel).setVisibility(View.INVISIBLE);
                }
                view.findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        mActionListener.onFormSubmitted(VisitCreationActivity.STEP_6_HISTORY_SUMMARY, mIsEditMode, null);

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

            mQuestionsListingAdapter = new QuestionsListingAdapter(recyclerView, requireActivity(), false, false, null, 0, mRootComplainBasicInfoHashMap, mIsEditMode, new OnItemSelection() {
                @Override
                public void onSelect(Node node, int index, boolean isSkipped, Node parentNode) {
                    // avoid the scroll for old data change
                    if (mCurrentComplainNodeOptionsIndex - index >= 1) {
                        return;
                    }

//                    if (node.getOptionsList() != null && node.isRequired() && node.getOptionsList().size() > 0) {
//                        int selectCount = 0;
//                        for (Node option : node.getOptionsList()) {
//                            if (option.isSelected()) selectCount++;
//                        }
//                        node.setDataCaptured(selectCount > 0);
//                        node.setSelected(node.isDataCaptured());
//                    }

                    if (isSkipped) {
                        mQuestionsListingAdapter.geItems().get(index).setSelected(false);
                        mQuestionsListingAdapter.geItems().get(index).setDataCaptured(false);
                        mQuestionsListingAdapter.notifyItemChanged(index);
                    }
                    //Log.v("onSelect", "node - " + node.getText());
                    if (mCurrentComplainNodeOptionsIndex < mCurrentRootOptionList.size() - 1) {
                        mCurrentComplainNodeOptionsIndex++;

                        mQuestionsListingAdapter.addItem(mCurrentRootOptionList.get(mCurrentComplainNodeOptionsIndex), getEngineVersion());
                    /*recyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                        }
                    }, 100);*/
                        VisitUtils.scrollNow(recyclerView, 300, 0, 500, mIsEditMode, false);

                        VisitUtils.scrollNow(recyclerView, 1400, 0, 1400, mIsEditMode, false);

                        mActionListener.onProgress((int) 100 / mCurrentRootOptionList.size());
                    } else {
                        if (!mIsEditMode)
                            mActionListener.onFormSubmitted(VisitCreationActivity.STEP_6_HISTORY_SUMMARY, mIsEditMode, null);
                        else
                            Toast.makeText(requireActivity(), getString(R.string.please_submit_to_proceed_next_step), Toast.LENGTH_SHORT).show();
                    }
                    linearLayoutManager.setStackFromEnd(false);
                }

                @Override
                public void needTitleChange(String title) {
                    mActionListener.onTitleChange(title);
                }

                @Override
                public void onAllAnswered(boolean isAllAnswered) {
                    if (!mIsEditMode)
                        mActionListener.onFormSubmitted(VisitCreationActivity.STEP_6_HISTORY_SUMMARY, mIsEditMode, null);
                    else
                        Toast.makeText(requireActivity(), getString(R.string.please_submit_to_proceed_next_step), Toast.LENGTH_SHORT).show();
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
            });

            recyclerView.setAdapter(mQuestionsListingAdapter);
            if (mIsEditMode) {
                mQuestionsListingAdapter.addItemAll(mCurrentRootOptionList);
                mCurrentComplainNodeOptionsIndex = mCurrentRootOptionList.size() - 1;
            } else {
                mQuestionsListingAdapter.addItem(mCurrentRootOptionList.get(mCurrentComplainNodeOptionsIndex), getEngineVersion());

            }
        } else {
            Toast.makeText(requireActivity(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
        }
        return view;
    }
}