package org.intelehealth.app.ayu.visit.diagnostics.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.ayu.visit.diagnostics.repository.DiagnosticsCollectionRepository
import org.intelehealth.app.database.dao.EncounterDAO
import org.intelehealth.app.database.dao.ObsDAO
import org.intelehealth.app.databinding.FragmentDiagnosticsCollectionBinding
import org.intelehealth.app.models.DiagnosticsModel
import org.intelehealth.app.models.dto.ObsDTO
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.UuidDictionary
import org.intelehealth.app.utilities.exception.DAOException
import org.intelehealth.config.presenter.fields.data.DiagnosticsRepository
import javax.inject.Inject

class DiagnosticsCollectionViewModel @Inject constructor(
    private val repository: DiagnosticsCollectionRepository
) : ViewModel() {

    private val _results = MutableLiveData<ObsDTO>()
    val results: LiveData<ObsDTO> get() = _results

    private val _encounterUuid = MutableLiveData<String>()
    val encounterUuid: LiveData<String> get() = _encounterUuid

    private var isEditMode: Boolean = false


    fun loadSavedData(encounterUuid: String) {
        _encounterUuid.value = encounterUuid
        _results.value = repository.getResultsFromDatabase(encounterUuid)
    }

    fun saveData() {
        _results.value?.let {
            if (isEditMode) {
                repository.updateResults(it)
            } else {
                repository.insertResults(it)
            }
        }
    }

    fun setEditMode(isEditMode: Boolean) {
        this.isEditMode = isEditMode
    }
}