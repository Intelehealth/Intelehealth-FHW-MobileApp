package org.intelehealth.app.ayu.visit.physicalexam;

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
import org.intelehealth.app.knowledgeEngine.PhysicalExam;
import org.intelehealth.app.utilities.CustomLog;
import org.intelehealth.app.utilities.DialogUtils;

import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PhysicalExaminationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PhysicalExaminationFragment extends Fragment {

    //private List<Node> mCurrentRootOptionList = new ArrayList<>();
    private int mCurrentComplainNodeOptionsIndex = 0;
    private int mCurrentChildComplainNodeOptionsIndex = 0;
    private QuestionsListingAdapter mQuestionsListingAdapter;
    private PhysicalExam physicalExam;
    private VisitCreationActionListener mActionListener;
    private boolean mIsEditMode = false;

    public PhysicalExaminationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActionListener = (VisitCreationActionListener) context;
        //sessionManager = new SessionManager(context);
        ((VisitCreationActivity) context).setImageUtilsListener(new VisitCreationActivity.ImageUtilsListener() {
            @Override
            public void onImageReady(Bundle bundle) {
                mQuestionsListingAdapter.addImageInLastNode(bundle.getString("image"));
            }

            @Override
            public void onImageReadyForDelete(int nodeIndex, int imageIndex, String imageName) {
                mQuestionsListingAdapter.removeImageInLastNode(nodeIndex, imageIndex, imageName);
            }
        });
    }

    public static PhysicalExaminationFragment newInstance(CommonVisitData commonVisitData, boolean isEditMode, PhysicalExam physicalExamMap) {
        PhysicalExaminationFragment fragment = new PhysicalExaminationFragment();
        fragment.mIsEditMode = isEditMode;
        fragment.physicalExam = physicalExamMap;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private final HashMap<Integer, ComplainBasicInfo> mRootComplainBasicInfoHashMap = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_physical_examination, container, false);
        if (physicalExam != null) {
            if (mIsEditMode) {
                view.findViewById(R.id.ll_footer).setVisibility(View.VISIBLE);
                if (!((VisitCreationActivity) requireActivity()).isEditTriggerFromVisitSummary()) {
                    view.findViewById(R.id.btn_cancel).setVisibility(View.INVISIBLE);
                }
                view.findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        mActionListener.onFormSubmitted(VisitCreationActivity.STEP_4_PHYSICAL_SUMMARY_EXAMINATION, mIsEditMode, null);

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
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireActivity());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            linearLayoutManager.setStackFromEnd(!mIsEditMode);
            linearLayoutManager.setReverseLayout(false);
            linearLayoutManager.setSmoothScrollbarEnabled(true);
            recyclerView.setLayoutManager(linearLayoutManager);
            //mCurrentRootOptionList = mCurrentNode.getOptionsList();
            ComplainBasicInfo complainBasicInfo = new ComplainBasicInfo();
            complainBasicInfo.setComplainName("Physical Exam");

            complainBasicInfo.setOptionSize(physicalExam.getTotalNumberOfExams());
            complainBasicInfo.setPhysicalExam(true);
            mRootComplainBasicInfoHashMap.put(0, complainBasicInfo);

            mQuestionsListingAdapter = new QuestionsListingAdapter(recyclerView, requireActivity(), false, true, physicalExam, 0, mRootComplainBasicInfoHashMap, mIsEditMode, new OnItemSelection() {
                @Override
                public void onSelect(Node node, int index, boolean isSkipped, Node parentNode) {
                    // avoid the scroll for old data change
                    if (mCurrentComplainNodeOptionsIndex - index >= 1) {
                        return;
                    }
                    if (isSkipped) {
                        mQuestionsListingAdapter.geItems().get(index).setSelected(false);
                        mQuestionsListingAdapter.geItems().get(index).setDataCaptured(false);
                        mQuestionsListingAdapter.notifyItemChanged(index);
                    }
                    CustomLog.v("onSelect", "node - " + node.getText());
                    if (mCurrentComplainNodeOptionsIndex < physicalExam.getTotalNumberOfExams() - 1) {
                        //if (mCurrentChildComplainNodeOptionsIndex < physicalExam.getExamNode(mCurrentComplainNodeOptionsIndex).getOptionsList().size()) {
                        //if (mCurrentChildComplainNodeOptionsIndex == physicalExam.getExamNode(mCurrentComplainNodeOptionsIndex).getOptionsList().size() - 1) {
                        //    mCurrentChildComplainNodeOptionsIndex = 0;
                        mCurrentComplainNodeOptionsIndex++;
                        // } else {
                        //    mCurrentChildComplainNodeOptionsIndex++;

                        //}


                        mQuestionsListingAdapter.addItem(physicalExam.getExamNode(mCurrentComplainNodeOptionsIndex).getOption(0), physicalExam.getEngineVersion());
                   /* recyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                        }
                    }, 100);*/
                        VisitUtils.scrollNow(recyclerView, 300, 0, 500, mIsEditMode, false);

                        VisitUtils.scrollNow(recyclerView, 1400, 0, 1400, mIsEditMode, false);

                        mActionListener.onProgress((int) 100 / physicalExam.getTotalNumberOfExams());
                        // }
                    } else {
                        if (!mIsEditMode)
                            mActionListener.onFormSubmitted(VisitCreationActivity.STEP_4_PHYSICAL_SUMMARY_EXAMINATION, mIsEditMode, null);
                        else
                            Toast.makeText(requireActivity(), getString(R.string.please_submit_to_proceed_next_step), Toast.LENGTH_SHORT).show();
                    }
                    if (!mIsEditMode)
                        linearLayoutManager.setStackFromEnd(false);
                }

                @Override
                public void needTitleChange(String title) {
                    // mActionListener.onTitleChange(title);
                }

                @Override
                public void onAllAnswered(boolean isAllAnswered) {
                    if (!mIsEditMode)
                        mActionListener.onFormSubmitted(VisitCreationActivity.STEP_4_PHYSICAL_SUMMARY_EXAMINATION, mIsEditMode, null);
                    else
                        Toast.makeText(requireActivity(), getString(R.string.please_submit_to_proceed_next_step), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCameraRequest() {
                    mActionListener.onCameraOpenRequest();
                }

                @Override
                public void onImageRemoved(int nodeIndex, int imageIndex, String image) {
                    mActionListener.onImageRemoved(nodeIndex, imageIndex, image);
                }

                @Override
                public void onTerminalNodeAnsweredForParentUpdate(String parentNodeId) {

                }
            });

            recyclerView.setAdapter(mQuestionsListingAdapter);
            mQuestionsListingAdapter.addItem(physicalExam.getExamNode(mCurrentComplainNodeOptionsIndex).getOption(0), physicalExam.getEngineVersion());
            showSanityDialog();
            if (mIsEditMode) {
                while (true) {
                    if (mCurrentComplainNodeOptionsIndex < physicalExam.getTotalNumberOfExams() - 1) {
                        mCurrentComplainNodeOptionsIndex++;
                        mQuestionsListingAdapter.addItem(physicalExam.getExamNode(mCurrentComplainNodeOptionsIndex).getOption(0), physicalExam.getEngineVersion());


                    } else {
                        break;
                    }
                }
            /*recyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                }
            }, 100);*/
            }
        } else {
            Toast.makeText(requireActivity(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
        }
        return view;
    }

    private void showSanityDialog() {
        DialogUtils dialogUtils = new DialogUtils();
        dialogUtils.showCommonDialog(requireActivity(), R.drawable.ui2_ic_warning_sanity, getResources().getString(R.string.sanity_alert_title), "", true, getResources().getString(R.string.okay), getResources().getString(R.string.cancel), new DialogUtils.CustomDialogListener() {
            @Override
            public void onDialogActionDone(int action) {

            }
        });
    }
}