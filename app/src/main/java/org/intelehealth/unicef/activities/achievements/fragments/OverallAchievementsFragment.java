package org.intelehealth.unicef.activities.achievements.fragments;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.app.AppConstants;
import org.intelehealth.unicef.utilities.DateAndTimeUtils;
import org.intelehealth.unicef.utilities.SessionManager;
import org.intelehealth.unicef.utilities.StringUtils;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OverallAchievementsFragment extends Fragment {

    private TextView tvOverallPatientsAdded;
    private TextView tvOverallVisitsEnded;
    private TextView tvOverallPatientSatisfactionScore;
    private TextView tvOverallTimeSpent;

    private SessionManager sessionManager;
    private UsageStats overallUsageStats;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = ((MyAchievementsFragment) requireParentFragment()).sessionManager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overall_achievements_ui2, container, false);
        initUI(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchAndSetUIData();
    }

    private void initUI(View view) {
        tvOverallPatientsAdded = view.findViewById(R.id.tv_overall_patients_added);
        tvOverallVisitsEnded = view.findViewById(R.id.tv_overall_visits_ended);
        tvOverallPatientSatisfactionScore = view.findViewById(R.id.tv_overall_patient_satisfaction_score);
        tvOverallTimeSpent = view.findViewById(R.id.tv_overall_time_spent);
    }

    private void fetchAndSetUIData() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            setOverallPatientsCreated();
            setOverallVisitsEnded();
            setOverallPatientSatisfactionScore();
            setOverallTimeSpent();
        });
    }

    // get the overall number of visits that were ended by the current health worker
    private void setOverallPatientsCreated() {
        String patientsCreatedTodayQuery = "SELECT COUNT(DISTINCT patientuuid) FROM tbl_patient_attribute WHERE person_attribute_type_uuid = \"84f94425-789d-4293-a0d8-9dc01dbb4f07\" AND value = ?";
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
        final Cursor overallPatientsCreatedCursor = db.rawQuery(patientsCreatedTodayQuery, new String[]{sessionManager.getProviderID()});
        overallPatientsCreatedCursor.moveToFirst();
        String overallPatientsCreatedCount = overallPatientsCreatedCursor.getString(overallPatientsCreatedCursor.getColumnIndex(overallPatientsCreatedCursor.getColumnName(0)));
        requireActivity().runOnUiThread(() -> tvOverallPatientsAdded.setText(overallPatientsCreatedCount));
        overallPatientsCreatedCursor.close();
    }

    // get the overall patient satisfaction score for the health worker
    private void setOverallVisitsEnded() {
        String visitsEndedTodayQuery = "SELECT COUNT(DISTINCT visituuid) FROM tbl_encounter WHERE provider_uuid = ? AND encounter_type_uuid = \"629a9d0b-48eb-405e-953d-a5964c88dc30\"";
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
        final Cursor overallVisitsEndedCursor = db.rawQuery(visitsEndedTodayQuery, new String[]{sessionManager.getProviderID()});

        overallVisitsEndedCursor.moveToFirst();
        String overallVisitsEndedCount = overallVisitsEndedCursor.getString(overallVisitsEndedCursor.getColumnIndex(overallVisitsEndedCursor.getColumnName(0)));
        requireActivity().runOnUiThread(() -> tvOverallVisitsEnded.setText(overallVisitsEndedCount));
        overallVisitsEndedCursor.close();
    }

    // get the overall average patient satisfaction score for the health worker
    private void setOverallPatientSatisfactionScore() {
        double averageScore = 0.0, totalScore = 0.0;

        String overallAverageSatisfactionScoreQuery = "SELECT value FROM tbl_obs WHERE conceptuuid = \"78284507-fb71-4354-9b34-046ab205e18f\" AND encounteruuid IN (SELECT uuid FROM tbl_encounter WHERE provider_uuid = ?)";
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();
        final Cursor satisfactionScoreCursor = db.rawQuery(overallAverageSatisfactionScoreQuery, new String[]{sessionManager.getProviderID()});

        if (satisfactionScoreCursor.moveToFirst()) {
            do {
                double currentScore = Double.parseDouble(satisfactionScoreCursor.getString(satisfactionScoreCursor.getColumnIndexOrThrow("value")));
                totalScore = totalScore + currentScore;
            } while (satisfactionScoreCursor.moveToNext());

            averageScore = totalScore / satisfactionScoreCursor.getCount();
        }

        double finalAverageScore = averageScore;
        requireActivity().runOnUiThread(() -> tvOverallPatientSatisfactionScore.setText(StringUtils.formatDoubleValues(finalAverageScore)));
        satisfactionScoreCursor.close();
    }

    private void setOverallTimeSpent() {
        long startTime = DateAndTimeUtils.convertStringDateToMilliseconds(sessionManager.getFirstProviderLoginTime(), "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        long endDate = System.currentTimeMillis();

        UsageStatsManager usageStatsManager = ((MyAchievementsFragment) requireParentFragment()).usageStatsManager;
        Map<String, UsageStats> aggregateStatsMap = usageStatsManager.queryAndAggregateUsageStats(startTime, endDate);
        overallUsageStats = aggregateStatsMap.get("org.intelehealth.unicef");

        requireActivity().runOnUiThread(() -> {
            String totalTimeSpent = "";
            if (overallUsageStats != null) {
                totalTimeSpent = String.format(Locale.ENGLISH, DateAndTimeUtils.convertMillisecondsToHoursAndMinutes(overallUsageStats.getTotalTimeInForeground()));
            } else {
                totalTimeSpent = "0h 0m";
            }
            tvOverallTimeSpent.setText(totalTimeSpent);
        });
    }
}