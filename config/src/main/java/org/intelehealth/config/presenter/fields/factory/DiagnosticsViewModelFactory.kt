package org.intelehealth.config.presenter.fields.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.intelehealth.config.presenter.fields.data.DiagnosticsRepository
import org.intelehealth.config.presenter.fields.viewmodel.DiagnosticsViewModel

class DiagnosticsViewModelFactory (private val repository: DiagnosticsRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DiagnosticsViewModel(repository) as T
    }
}