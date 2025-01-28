package org.intelehealth.app.ayu.visit.vital

import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.launch
import org.intelehealth.config.presenter.fields.viewmodel.DiagnosticsViewModel
import org.intelehealth.config.presenter.fields.viewmodel.PatientVitalViewModel

class CoroutineProvider {
    companion object{
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
        @JvmStatic
        fun usePatientDiagnosticsScope(
            scope: LifecycleCoroutineScope,
            diagnosticsViewModel: DiagnosticsViewModel,
            coroutineDataCallback: CoroutineDataCallback

        ) {
            scope.launch {
                val patientDiagnosticsList = diagnosticsViewModel.getAllEnabledFields()
                coroutineDataCallback.onReceiveData(patientDiagnosticsList)
            }
        }
    }
}