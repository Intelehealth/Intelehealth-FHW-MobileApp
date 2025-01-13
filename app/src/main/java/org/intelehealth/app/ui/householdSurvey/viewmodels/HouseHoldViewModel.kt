package org.intelehealth.app.ui.householdSurvey.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.ui.householdSurvey.repository.HouseholdRepository
import org.intelehealth.app.utilities.HouseholdSurveyStage
import org.intelehealth.core.shared.ui.viewmodel.BaseViewModel

class HouseHoldViewModel(
    private val repository: HouseholdRepository
) : BaseViewModel() {

    private var mutableLivePatient = MutableLiveData<org.intelehealth.app.ui.householdSurvey.models.HouseholdSurveyModel>()
    val patientSurveyAttributesData: LiveData<org.intelehealth.app.ui.householdSurvey.models.HouseholdSurveyModel> get() = mutableLivePatient
    private var mutableLivePatientStage = MutableLiveData(HouseholdSurveyStage.FIRST_SCREEN)
    val patientStageData: LiveData<HouseholdSurveyStage> get() = mutableLivePatientStage
    var isEditMode: Boolean = false

    fun loadPatientDetails(
        patientId: String
    ) = executeLocalQuery {
        repository.fetchPatient(patientId)
    }.asLiveData()


    fun updatePatientStage(stage: HouseholdSurveyStage) {
        mutableLivePatientStage.postValue(stage)
    }

    fun updatedPatient(householdSurveyModel: org.intelehealth.app.ui.householdSurvey.models.HouseholdSurveyModel) {
        Timber.d { "Saved patient attrs=> ${Gson().toJson(householdSurveyModel)}" }
        mutableLivePatient.postValue(householdSurveyModel)
    }

    fun savePatient(fragmentIdentifier: String, patientDTO: PatientDTO, householdSurveyModel: org.intelehealth.app.ui.householdSurvey.models.HouseholdSurveyModel) =
        executeLocalInsertUpdateQuery {
            return@executeLocalInsertUpdateQuery patientSurveyAttributesData.value?.let {
                Log.d("devKZchk", "savePatient: " + patientDTO.uuid)
                return@let if (isEditMode) repository.updateHouseholdPatientAttributes(fragmentIdentifier,
                    patientDTO,
                    householdSurveyModel
                )
                else repository.addHouseholdPatientAttributes(fragmentIdentifier,
                    patientDTO,
                    householdSurveyModel
                ) // TODO: check with mithun this is creating a new record again with parent ID.
            } ?: false
        }.asLiveData()
}