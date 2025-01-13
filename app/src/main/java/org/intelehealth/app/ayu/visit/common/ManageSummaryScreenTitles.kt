package org.intelehealth.app.ayu.visit.common

import android.content.Context
import android.util.Log
import org.intelehealth.app.BuildConfig
import org.intelehealth.app.R
import org.intelehealth.app.ayu.visit.VisitCreationActivity.STEP_1_VITAL_SUMMARY
import org.intelehealth.app.ayu.visit.VisitCreationActivity.STEP_2_DIAGNOSTICS_SUMMARY
import org.intelehealth.app.ayu.visit.VisitCreationActivity.STEP_3_VISIT_REASON_QUESTION_SUMMARY
import org.intelehealth.app.ayu.visit.VisitCreationActivity.STEP_4_PHYSICAL_SUMMARY_EXAMINATION
import org.intelehealth.app.ayu.visit.VisitCreationActivity.STEP_6_HISTORY_SUMMARY
import org.intelehealth.app.utilities.FlavorKeys
import org.intelehealth.config.room.entity.FeatureActiveStatus

class ManageSummaryScreenTitles {
    companion object {
        private const val TAG = "ManageSummaryScreenTitl"

        @JvmStatic
        fun setScreenTitle(
            context: Context,
            featureActiveStatus: FeatureActiveStatus,
            screenId: Int
        ): String {
            var currentScreenIndex = 1
            var title = ""
            val isVitalEnabled = featureActiveStatus.vitalSection
            val isDiagnosticsEnabled = featureActiveStatus.activeStatusDiagnosticsSection

            val vitalScreenIndex = if (isVitalEnabled) 1 else 0
            val diagnosticsScreenIndex =
                if (isDiagnosticsEnabled) vitalScreenIndex + 1 else vitalScreenIndex
            val visitReasonScreenIndex = maxOf(vitalScreenIndex, diagnosticsScreenIndex) + 1

            var adjustedTotalScreen = 5
            if (!isVitalEnabled) adjustedTotalScreen--
            if (!isDiagnosticsEnabled) adjustedTotalScreen--

            when (screenId) {
                STEP_1_VITAL_SUMMARY -> {
                    if (isVitalEnabled) {
                        currentScreenIndex = vitalScreenIndex
                        title = context.getString(
                            R.string._vitals_summary,
                            currentScreenIndex,
                            adjustedTotalScreen
                        )
                    }
                }
                STEP_2_DIAGNOSTICS_SUMMARY -> {
                    if (isDiagnosticsEnabled) {
                        currentScreenIndex = diagnosticsScreenIndex
                        title = context.getString(
                            R.string._diagnostics_summary,
                            currentScreenIndex,
                            adjustedTotalScreen
                        )
                    }
                }
                STEP_3_VISIT_REASON_QUESTION_SUMMARY -> {
                    currentScreenIndex = visitReasonScreenIndex
                    title = context.getString(
                        R.string._visit_reason_summary,
                        currentScreenIndex,
                        adjustedTotalScreen
                    )
                }
                STEP_4_PHYSICAL_SUMMARY_EXAMINATION -> {
                    currentScreenIndex = visitReasonScreenIndex + 1
                    title = context.getString(
                        R.string.ui2_physical_exam_summay_title,
                        currentScreenIndex,
                        adjustedTotalScreen
                    )
                    if (BuildConfig.FLAVOR_client === FlavorKeys.KCDO) {
                        title = context.getString(
                            R.string.ui2_relapse_summary_title,
                            currentScreenIndex,
                            adjustedTotalScreen)
                    } else if (BuildConfig.FLAVOR_client === FlavorKeys.UNFPA) {
                        title = context.getString(
                            R.string.ui2_obstetric_history_summary_title,
                            currentScreenIndex,
                            adjustedTotalScreen)
                    }
                }
                STEP_6_HISTORY_SUMMARY -> {
                    currentScreenIndex = visitReasonScreenIndex + 2
                    title = context.getString(
                        R.string.ui2_medical_hist_title_text,
                        currentScreenIndex,
                        adjustedTotalScreen
                    )
                }
                else -> {
                    Log.w(TAG, "Unknown screenId: $screenId")
                }
            }
            return title
        }
    }
}
