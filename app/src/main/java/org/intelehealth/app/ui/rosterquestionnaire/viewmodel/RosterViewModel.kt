package org.intelehealth.app.ui.rosterquestionnaire.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.intelehealth.app.ui.rosterquestionnaire.model.HealthServiceModel
import org.intelehealth.app.ui.rosterquestionnaire.model.PregnancyOutComeModel
import org.intelehealth.app.ui.rosterquestionnaire.model.RoasterViewQuestion
import org.intelehealth.app.ui.rosterquestionnaire.usecase.GetGeneralQuestionUseCase
import org.intelehealth.app.ui.rosterquestionnaire.usecase.GetHealthServiceQuestionUseCase
import org.intelehealth.app.ui.rosterquestionnaire.usecase.GetOutComeQuestionUseCase
import org.intelehealth.app.ui.rosterquestionnaire.utilities.RosterQuestionnaireStage
import javax.inject.Inject

@HiltViewModel
class RosterViewModel @Inject constructor(
    private val getHealthServiceQuestionUseCase: GetHealthServiceQuestionUseCase,
    private val getOutComeQuestionUseCase: GetOutComeQuestionUseCase,
    private val getGeneralQuestionUseCase: GetGeneralQuestionUseCase,
) : ViewModel() {


    var patientUuid: String? = null
    private var mutableLiveRosterStage = MutableLiveData(RosterQuestionnaireStage.GENERAL_ROSTER)
    val rosterStageData: LiveData<RosterQuestionnaireStage> get() = mutableLiveRosterStage

    var isEditMode: Boolean = false

    private val _generalLiveList = MutableLiveData<ArrayList<RoasterViewQuestion>>(arrayListOf())
    val generalLiveList: LiveData<ArrayList<RoasterViewQuestion>> = _generalLiveList

    private val _outComeLiveList = MutableLiveData<ArrayList<PregnancyOutComeModel>>(arrayListOf())
    val outComeLiveList: LiveData<ArrayList<PregnancyOutComeModel>> = _outComeLiveList

    private val _healthServiceLiveList =
        MutableLiveData<ArrayList<HealthServiceModel>>(arrayListOf())
    val healthServiceLiveList: LiveData<ArrayList<HealthServiceModel>> = _healthServiceLiveList

    var existingRoasterQuestionList: ArrayList<RoasterViewQuestion>? = null
    var existPregnancyOutComePosition = 0

    fun updateRosterStage(stage: RosterQuestionnaireStage) {
        mutableLiveRosterStage.postValue(stage)
    }

    fun addPregnancyOutcome(questionList: List<RoasterViewQuestion>) {
        val pregnancyOutComeModel = PregnancyOutComeModel(
            title = questionList[0].answer ?: "",
            roasterViewQuestion = questionList
        )
        val list = _outComeLiveList.value ?: mutableListOf()
        if (existingRoasterQuestionList == null) {
            list.add(pregnancyOutComeModel)
        } else {
            list[existPregnancyOutComePosition] = pregnancyOutComeModel
            existingRoasterQuestionList = null
        }
        _outComeLiveList.postValue(list as ArrayList<PregnancyOutComeModel>?)
    }

    fun addHealthService(questionList: List<RoasterViewQuestion>) {
        val healthServiceModel = HealthServiceModel(
            title = questionList[0].answer ?: "",
            roasterViewQuestion = questionList
        )
        val list = _healthServiceLiveList.value ?: mutableListOf()
        if (existingRoasterQuestionList == null) {
            list.add(healthServiceModel)
        } else {
            list[existPregnancyOutComePosition] = healthServiceModel
            existingRoasterQuestionList = null
        }
        _healthServiceLiveList.postValue(list as ArrayList<HealthServiceModel>?)
    }


    fun deletePregnancyOutcome(position: Int) {
        val list = _outComeLiveList.value ?: mutableListOf()
        list.removeAt(position)
        _outComeLiveList.postValue(list as ArrayList<PregnancyOutComeModel>?)

    }

    fun deleteHealthService(position: Int) {
        val list = _healthServiceLiveList.value ?: mutableListOf()
        list.removeAt(position)
        _healthServiceLiveList.postValue(list as ArrayList<HealthServiceModel>?)

    }

    fun getOutcomeQuestionList(): ArrayList<RoasterViewQuestion> =
        getOutComeQuestionUseCase(existingRoasterQuestionList)

    fun getHealthServiceList(): ArrayList<RoasterViewQuestion> =
        getHealthServiceQuestionUseCase(existingRoasterQuestionList)

    fun getGeneralQuestionList() {
        _generalLiveList.postValue(getGeneralQuestionUseCase(_generalLiveList.value))
    }
}