package org.intelehealth.config.presenter.fields.viewmodel

import org.intelehealth.config.presenter.fields.data.DiagnosticsRepository
import org.intelehealth.config.presenter.fields.data.PatientVitalRepository
import org.intelehealth.core.shared.ui.viewmodel.BaseViewModel

class DiagnosticsViewModel (private val repository: DiagnosticsRepository) : BaseViewModel() {

    fun getAllEnabledLiveFields() = repository.getAllEnabledLiveFields()
    suspend fun getAllEnabledFields() = repository.getAllEnabledFields()
}