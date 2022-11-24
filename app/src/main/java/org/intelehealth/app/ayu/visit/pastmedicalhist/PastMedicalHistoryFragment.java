package org.intelehealth.app.ayu.visit.pastmedicalhist;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.intelehealth.app.R;
import org.intelehealth.app.ayu.visit.VisitCreationActionListener;
import org.intelehealth.app.ayu.visit.VisitCreationActivity;
import org.intelehealth.app.ayu.visit.common.adapter.QuestionsListingAdapter;
import org.intelehealth.app.knowledgeEngine.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PastMedicalHistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PastMedicalHistoryFragment extends Fragment {

    private List<Node> mCurrentRootOptionList = new ArrayList<>();
    private int mCurrentComplainNodeOptionsIndex = 0;
    private QuestionsListingAdapter mQuestionsListingAdapter;
    private Node mCurrentNode;
    private VisitCreationActionListener mActionListener;

    public PastMedicalHistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PastMedicalHistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PastMedicalHistoryFragment newInstance(Intent intent, Node node) {
        PastMedicalHistoryFragment fragment = new PastMedicalHistoryFragment();
        fragment.mCurrentNode = node;
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
        View view = inflater.inflate(R.layout.fragment_past_medical_history, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.rcv_questions);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        mCurrentRootOptionList = mCurrentNode.getOptionsList();

        mQuestionsListingAdapter = new QuestionsListingAdapter(recyclerView, getActivity(), mCurrentRootOptionList.size(), new QuestionsListingAdapter.OnItemSelection() {
            @Override
            public void onSelect(Node node) {
                //Log.v("onSelect", "node - " + node.getText());
                if (mCurrentComplainNodeOptionsIndex < mCurrentRootOptionList.size() - 1)
                    mCurrentComplainNodeOptionsIndex++;
                else {
                    mCurrentComplainNodeOptionsIndex = 0;

                }
                mQuestionsListingAdapter.addItem(mCurrentRootOptionList.get(mCurrentComplainNodeOptionsIndex));
                recyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                    }
                }, 100);

                mActionListener.onProgress((int) 100 / mCurrentRootOptionList.size());
            }

            @Override
            public void needTitleChange(String title) {
                mActionListener.onTitleChange(title);
            }

            @Override
            public void onAllAnswered(boolean isAllAnswered) {
                mActionListener.onFormSubmitted(VisitCreationActivity.STEP_2_VISIT_REASON_QUESTION_SUMMARY, null);
            }
        });

        recyclerView.setAdapter(mQuestionsListingAdapter);
        mQuestionsListingAdapter.addItem(mCurrentRootOptionList.get(mCurrentComplainNodeOptionsIndex));
        return view;
    }
}