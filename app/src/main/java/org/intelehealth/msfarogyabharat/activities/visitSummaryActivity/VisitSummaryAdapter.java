package org.intelehealth.msfarogyabharat.activities.visitSummaryActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.msfarogyabharat.R;

import java.util.List;

/**
 * Created By: Prajwal Waingankar on 22-Oct-21
 * Github: prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */

public class VisitSummaryAdapter extends RecyclerView.Adapter<VisitSummaryAdapter.VisitSummaryViewHolder> {
    List<String> complaintList;
    List<String> visitUuidList;
    List<String> physexamList;

    public VisitSummaryAdapter(List<String> visitUuidList, List<String> complaintList, List<String> physexamList) {
        this.visitUuidList = visitUuidList;
        this.complaintList = complaintList;
        this.physexamList = physexamList;
    }

    @Override
    public VisitSummaryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_visitsummary, parent, false);

        return new VisitSummaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VisitSummaryAdapter.VisitSummaryViewHolder holder, int position) {
        String complaint = complaintList.get(position);
        String physexam = physexamList.get(position);
      //  String visits = visitUuidList.get(position);

        holder.textView_caseTitle.setText("Case " + (position + 1));
        holder.textView_content_complaint.setText(complaint);
        holder.textView_content_physexam.setText(physexam);
    }

    @Override
    public int getItemCount() {
        return visitUuidList.size();
    }

    public class VisitSummaryViewHolder extends RecyclerView.ViewHolder {
        TextView textView_caseTitle, textView_content_complaint, textView_content_physexam;
        LinearLayout linearlayout_body;

        public VisitSummaryViewHolder(View itemView) {
            super(itemView);
            textView_caseTitle = itemView.findViewById(R.id.textView_caseTitle);
            textView_content_complaint = itemView.findViewById(R.id.textView_content_complaint);
            textView_content_physexam = itemView.findViewById(R.id.textView_content_physexam);
            linearlayout_body = itemView.findViewById(R.id.linearlayout_body);

            textView_caseTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(linearlayout_body.getVisibility() == View.VISIBLE) {
                        linearlayout_body.setVisibility(View.GONE);
                    }
                    else {
                        linearlayout_body.setVisibility(View.VISIBLE);
                    }


                }
            });
        }
    }
}
