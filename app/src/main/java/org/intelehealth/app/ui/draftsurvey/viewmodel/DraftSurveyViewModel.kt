package org.intelehealth.app.ui.draftsurvey.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.ui.draftsurvey.repository.DraftSurveyRepository

class DraftSurveyViewModel (private val repository: DraftSurveyRepository) : ViewModel() {

    private val _patientDTOList = MutableLiveData<List<PatientDTO>>()
    val patientDTOList: LiveData<List<PatientDTO>> = _patientDTOList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadPatientData() {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val patientList = repository.fetchPatientData()
            withContext(Dispatchers.Main) {
                if (_patientDTOList.value != patientList) {
                    _patientDTOList.value = patientList
                }
                _isLoading.value = false
            }
        }
    }

}