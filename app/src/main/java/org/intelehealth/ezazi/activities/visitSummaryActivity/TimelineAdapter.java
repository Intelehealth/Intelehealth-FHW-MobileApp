package org.intelehealth.ezazi.activities.visitSummaryActivity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.database.dao.ObsDAO;
import org.intelehealth.ezazi.models.dto.EncounterDTO;
import org.intelehealth.ezazi.models.dto.ObsDTO;
import org.intelehealth.ezazi.partogram.PartogramDataCaptureActivity;
import org.intelehealth.ezazi.utilities.SessionManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Prajwal Maruti Waingankar on 04-05-2022, 19:14
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder> {
    Context context;
    private String patientUuid, patientName, visitUuid;
    ArrayList<EncounterDTO> encounterDTOList;
    ObsDAO obsDAO;
    ObsDTO obsDTO;
    SessionManager sessionManager;
    private static final int HOURLY = 0;
    private static final int HALF_HOUR = 1;
    private static final int FIFTEEN_MIN = 2;
    ImageView iv_prescription;
    String isVCEPresent = "";
    int isMissed = 0;
    int issubmitted = 0;

    public TimelineAdapter(Context context, Intent intent, ArrayList<EncounterDTO> encounterDTOList,
                           SessionManager sessionManager, String isVCEPresent) {
        this.context = context;
        this.encounterDTOList = encounterDTOList;
        this.sessionManager = sessionManager;
        this.isVCEPresent = isVCEPresent;

        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            patientName = intent.getStringExtra("name");

//            String time = intent.getStringExtra("encounter_time");
//            SimpleDateFormat timeLineTime = new SimpleDateFormat("HH:mm a", Locale.ENGLISH);
//            String timeLineTimeValue = timeLineTime.format(todayDate);
        }

    }

    @NonNull
    @Override
    public TimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.timeline_listitem, parent, false);
        return new TimelineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
        if (encounterDTOList.size() > 0) {

            holder.ivEdit.setContentDescription(
                    new StringBuilder().append("ivEdit_desc_").append(holder.ivEdit.toString()));
            holder.cardview.setContentDescription(
                    new StringBuilder().append("cardview_desc_").append(holder.cardview.toString()));

            if (encounterDTOList.get(position).getEncounterTime() != null &&
                    !encounterDTOList.get(position).getEncounterTime().equalsIgnoreCase("")) {

                // Stage 1
                if (encounterDTOList.get(position).getEncounterTypeUuid()
                        .equalsIgnoreCase("ee560d18-34a1-4ad8-87c8-98aed99c663d")) {
                    holder.stage1start.setVisibility(View.VISIBLE);
                } else {
                    holder.stage1start.setVisibility(View.GONE);
                }

                // Stage 2
                if (encounterDTOList.get(position).getEncounterTypeUuid()
                        .equalsIgnoreCase("558cc1b8-c352-4b27-9ec2-131fc19c26f0")) {
                    holder.stage2start.setVisibility(View.VISIBLE);
                } else {
                    holder.stage2start.setVisibility(View.GONE);
                }

                String time = encounterDTOList.get(position).getEncounterTime();
                SimpleDateFormat longTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);
                SimpleDateFormat longTimeFormat_ = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm a", Locale.ENGLISH);
                String encounterTimeAmPmFormat = "";
                Calendar encounterTimeCalendar = Calendar.getInstance();
                // check for this enc any obs created if yes than show submitted...
                obsDAO = new ObsDAO();
                issubmitted = obsDAO.checkObsAddedOrNt(encounterDTOList.get(position).getUuid(), sessionManager.getCreatorID());
                try {
                    Date timeDateType = time.contains("T") && time.contains("+") ? longTimeFormat.parse(time) : longTimeFormat_.parse(time);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(timeDateType);
                    encounterTimeCalendar.setTime(timeDateType);

                    Log.v("Timeline", "position&CardTime: " + position + " - " + calendar.getTime());
                    if (!encounterDTOList.get(position).getEncounterTypeName().equalsIgnoreCase("") &&
                            encounterDTOList.get(position).getEncounterTypeName().toLowerCase().contains("stage1")) { // start
                        if (position % 2 == 0) { // Even
                            //calendar.add(Calendar.HOUR, 1);
                            calendar.add(Calendar.MINUTE, 20); // Add 1hr + 20min
                            // calendar.add(Calendar.MINUTE, 2); // Testing
                            Log.v("Timeline", "calendarTime 1Hr: " + calendar.getTime().toString());
                        } else { // Odd
                            calendar.add(Calendar.MINUTE, 10); // Add 30min + 10min
                            // calendar.add(Calendar.MINUTE, 1); // Testing
                            Log.v("Timeline", "calendarTime 30min: " + calendar.getTime().toString());
                        }
                    } // end.
                    else if (!encounterDTOList.get(position).getEncounterTypeName().equalsIgnoreCase("") &&
                            encounterDTOList.get(position).getEncounterTypeName().toLowerCase().contains("stage2")) {
                        calendar.add(Calendar.MINUTE, 5); // Add 15min + 5min since Stage 2
                        // calendar.add(Calendar.MINUTE, 1); // Testing
                        Log.v("Timeline", "calendarTime 1Hr: " + calendar.getTime().toString());
                    } else {
                        // do nothing
                    }

                    if (calendar.after(Calendar.getInstance())) { // ie. eg: 7:20 is after of current (6:30) eg.
                        holder.cardview.setClickable(true);
                        holder.cardview.setEnabled(true);
                        holder.summary_textview.setText("Pending!");
                        holder.summaryNoteTextview.setText("Tap here to collect the history data!");
                        holder.summary_textview.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                        holder.ivEdit.setVisibility(View.GONE);
                    } else {
                        holder.cardview.setClickable(false);
                        holder.cardview.setEnabled(false);

                        /* since card is disabled that means the either the user has filled data or has forgotten to fill.
                         We need to check this by using the encounterUuid and checking in obs tbl if any obs is created.
                         If no obs created than create Missed Enc obs for this disabled encounter. */
                        isMissed = obsDAO.checkObsAndCreateMissedObs(encounterDTOList.get(position).getUuid(), sessionManager.getCreatorID());
                        if (isMissed == 1 || isMissed == 3) {
                            holder.summaryNoteTextview.setText("You have missed to collect the history data.");
                            holder.summary_textview.setText(context.getResources().getString(R.string.missed_interval));
                            holder.summary_textview.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                            holder.ivEdit.setVisibility(View.GONE);
                        } else if (isMissed == 2) {
                            holder.summaryNoteTextview.setText("You have submitted the history data.");
                            holder.summary_textview.setText(context.getResources().getString(R.string.submitted_interval));
                            holder.summary_textview.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));


                            holder.ivEdit.setVisibility(View.VISIBLE);

                        }
                    }

                    encounterTimeAmPmFormat = timeFormat.format(timeDateType);
                    Log.v("timeline", "AM Format: " + encounterTimeAmPmFormat);
                } catch (ParseException e) {
                    e.printStackTrace();
                    Log.e("timeline", "AM Format: " + e.getMessage());

                    // work around since backend end Time not coming in same format in which we r sending
                    Date timeDateType = null;
                    try {
                        timeDateType = longTimeFormat_.parse(time);
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(timeDateType);

                    Log.v("Timeline", "position&CardTime: " + position + "- " + calendar.getTime());
                    if (!encounterDTOList.get(position).getEncounterTypeName().equalsIgnoreCase("") &&
                            encounterDTOList.get(position).getEncounterTypeName().toLowerCase().contains("stage1")) { // start
                        if (position % 2 == 0) { // Even
                            calendar.add(Calendar.HOUR, 1);
                            calendar.add(Calendar.MINUTE, 20); // Add 1hr + 20min
                            //  calendar.add(Calendar.MINUTE, 2); // Testing
                            Log.v("Timeline", "calendarTime 1Hr: " + calendar.getTime().toString());
                        } else { // Odd
                            calendar.add(Calendar.MINUTE, 40); // Add 30min + 10min
                            // calendar.add(Calendar.MINUTE, 1); // Testing
                            Log.v("Timeline", "calendarTime 30min: " + calendar.getTime().toString());
                        }
                    } else if (!encounterDTOList.get(position).getEncounterTypeName().equalsIgnoreCase("") &&
                            encounterDTOList.get(position).getEncounterTypeName().toLowerCase().contains("stage2")) {
                        calendar.add(Calendar.MINUTE, 20); // Add 15min + 5min since Stage 2
                        // calendar.add(Calendar.MINUTE, 1); // Testing

                        Log.v("Timeline", "calendarTime 1Hr: " + calendar.getTime().toString());
                    } else {
                        // do nothing
                    }

                    if (calendar.after(Calendar.getInstance())) { // ie. eg: 7:20 is after of current (6:30) eg.
                        holder.cardview.setClickable(true);
                        holder.cardview.setEnabled(true);
                        //  holder.cardview.setCardBackgroundColor(context.getResources().getColor(R.color.amber));
                        holder.ivEdit.setVisibility(View.GONE);
                    } else {
                        holder.cardview.setClickable(false);
                        holder.cardview.setEnabled(false);
                        //  holder.cardview.setCardElevation(0);

                        /* since card is disabled that means the either the user has filled data or has forgotten to fill.
                         We need to check this by using the encounterUuid and checking in obs tbl if any obs is created.
                         If no obs created than create Missed Enc obs for this disabled encounter. */
                        isMissed = obsDAO.checkObsAndCreateMissedObs(encounterDTOList.get(position).getUuid(), sessionManager.getCreatorID());
                        if (isMissed == 1 || isMissed == 3) {
                            holder.summary_textview.setText(context.getResources().getString(R.string.missed_interval));
                            holder.summary_textview.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                            holder.ivEdit.setVisibility(View.GONE);
                        } else if (isMissed == 2) {
                            holder.summary_textview.setText(context.getResources().getString(R.string.submitted_interval));
                            holder.summary_textview.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                            holder.ivEdit.setVisibility(View.VISIBLE);
                        }
                    }

                    encounterTimeAmPmFormat = timeFormat.format(timeDateType);
                    Log.v("timeline", "AM Format: " + encounterTimeAmPmFormat);
                    //
                }

                if (issubmitted == 2) { // This so that once submitted it should be closed and not allowed to edit again.
                    holder.cardview.setClickable(false);
                    holder.cardview.setEnabled(false);
                    holder.summaryNoteTextview.setText("You have submitted the history data.");
                    holder.summary_textview.setText(context.getResources().getString(R.string.submitted_interval));
                    holder.summary_textview.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                    holder.ivEdit.setVisibility(View.VISIBLE);
                    Log.v("timeline", "minutes enc time: " + time);
                    Log.v("timeline", "minutes enc time: " + encounterTimeCalendar.getTime().toString());
                    long diff = Calendar.getInstance().getTimeInMillis() - encounterTimeCalendar.getTimeInMillis();//as given

                    long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
                    Log.v("timeline", "minutes : " + minutes);
                    int limit = encounterDTOList.get(position).getEncounterTypeName().toLowerCase().contains("stage2") ? 5 : 20;
                    if (minutes <= limit) {
                        holder.ivEdit.setVisibility(View.VISIBLE);
                    } else {
                        holder.ivEdit.setVisibility(View.GONE);
                    }
                }

                if (!isVCEPresent.equalsIgnoreCase("")) { // If visit complete than disable all the cards.
                    holder.cardview.setClickable(false);
                    holder.cardview.setEnabled(false);
                }

                holder.timeTextview.setText(encounterTimeAmPmFormat);
            }
        }
    }


    @Override
    public int getItemCount() {
        return encounterDTOList.size();
    }

    public class TimelineViewHolder extends RecyclerView.ViewHolder {
        CardView cardview;
        TextView timeTextview, summary_textview, stage1start, stage2start, summaryNoteTextview;
        FrameLayout frame1, frame2, frame3, frame4;
        ImageView ivEdit;
        int index;

        public TimelineViewHolder(@NonNull View itemView) {
            super(itemView);

            summaryNoteTextview = itemView.findViewById(R.id.summary_note_textview_timelineSc);
            ivEdit = itemView.findViewById(R.id.ivEdit_timelineSc);
            ivEdit.setVisibility(View.GONE);

            cardview = itemView.findViewById(R.id.cardview_parent_timelineSc);
            timeTextview = itemView.findViewById(R.id.time1_timelineSc);
            stage1start = itemView.findViewById(R.id.stage1start_timelineSc);
            stage2start = itemView.findViewById(R.id.stage2start_timelineSc);
            summary_textview = itemView.findViewById(R.id.summary_textview_timelineSc);
            frame1 = itemView.findViewById(R.id.frame1_timelineSc);
            ivEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nextIntent(true);
                }
            });
            cardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    nextIntent(false);
                }
            });
        }

        void nextIntent(boolean isEditMode) {
            int type = 10;
            int stage = 1;
            String[] name = encounterDTOList.get(getAdapterPosition()).getEncounterTypeName().split("_");
            if (encounterDTOList.get(getAdapterPosition()).getEncounterTypeName().toLowerCase().contains("stage1")) {
                //type = getAdapterPosition() % 2 != 0 ? HALF_HOUR : HOURLY; // card clicked is 30min OR 1 Hr
                type = Integer.parseInt(name[2]) == 2 ? HALF_HOUR : HOURLY; // card clicked is 30min OR 1 Hr
            } else if (encounterDTOList.get(getAdapterPosition()).getEncounterTypeName().toLowerCase().contains("stage2")) {
                stage = 2;
                //type = FIFTEEN_MIN; // card clicked is 15mins.
                //Stage2_Hour1_1
                if (Integer.parseInt(name[2]) == 1) {
                    type = HOURLY;
                } else if (Integer.parseInt(name[2]) == 3) {
                    type = HALF_HOUR;
                } else {
                    type = FIFTEEN_MIN;
                }
            }


            Intent i1 = new Intent(context, PartogramDataCaptureActivity.class);
            i1.putExtra("patientUuid", patientUuid);
            i1.putExtra("name", patientName);
            i1.putExtra("visitUuid", visitUuid);
            i1.putExtra("encounterUuid", encounterDTOList.get(getAdapterPosition()).getUuid());
            i1.putExtra("type", type);
            i1.putExtra("stage", stage);
            i1.putExtra("isEditMode", isEditMode);
            context.startActivity(i1);
        }
    }

}
