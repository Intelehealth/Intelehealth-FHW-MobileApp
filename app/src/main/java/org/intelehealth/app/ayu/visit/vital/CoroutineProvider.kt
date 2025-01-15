package org.intelehealth.app.ayu.visit.vital

import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.launch
import org.intelehealth.config.presenter.feature.viewmodel.FeatureActiveStatusViewModel
import org.intelehealth.config.presenter.fields.viewmodel.PatientVitalViewModel

/**
 * normally java doesn't support coroutine
 * this class is for accessing coroutine scope from java
 */
class CoroutineProvider {
    companion object{

        /**
         * getting all active status of vitals
         */
        @JvmStatic
        fun usePatientVitalScope(
            scope: LifecycleCoroutineScope,
            patientVitalViewModel: PatientVitalViewModel,
            coroutineDataCallback: CoroutineDataCallback

        ) {
            scope.launch {
                val patientVitalList = patientVitalViewModel.getAllEnabledFields()
                coroutineDataCallback.onReceiveData(patientVitalList)
            }
        }

        /**
         * getting all feature active status here
         */
        @JvmStatic
        fun useFeatureActiveStatusScope(
            scope: LifecycleCoroutineScope,
            featureActiveStatusViewModel: FeatureActiveStatusViewModel,
            coroutineDataCallback: CoroutineDataCallback

        ) {
            scope.launch {
                val patientVitalList = featureActiveStatusViewModel.fetchFeaturesActiveStatusSuspended()
                coroutineDataCallback.onReceiveData(patientVitalList)
            }
        }
    }
}