package org.intelehealth.app.ui.rosterquestionnaire.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import org.intelehealth.app.ui.rosterquestionnaire.di.IoDispatcher
import org.intelehealth.app.ui.rosterquestionnaire.model.HealthServiceModel
import org.intelehealth.app.ui.rosterquestionnaire.model.PregnancyOutComeModel
import org.intelehealth.app.ui.rosterquestionnaire.model.RoasterViewQuestion
import org.intelehealth.app.ui.rosterquestionnaire.usecase.GetAllRoasterDataUseCase
import org.intelehealth.app.ui.rosterquestionnaire.usecase.GetGeneralQuestionUseCase
import org.intelehealth.app.ui.rosterquestionnaire.usecase.GetHealthServiceQuestionUseCase
import org.intelehealth.app.ui.rosterquestionnaire.usecase.GetOutComeQuestionUseCase
import org.intelehealth.app.ui.rosterquestionnaire.usecase.InsertRoasterUseCase
import org.intelehealth.app.ui.rosterquestionnaire.utilities.NO
import org.intelehealth.app.ui.rosterquestionnaire.utilities.RoasterAttribute
import org.intelehealth.app.ui.rosterquestionnaire.utilities.RosterQuestionnaireStage
import javax.inject.Inject

@HiltViewModel
class RosterViewModel @Inject constructor(
    private val getHealthServiceQuestionUseCase: GetHealthServiceQuestionUseCase,
    private val getOutComeQuestionUseCase: GetOutComeQuestionUseCase,
    private val getGeneralQuestionUseCase: GetGeneralQuestionUseCase,
    private val insertRoasterUseCase: InsertRoasterUseCase,
    private val getAllRoasterDataUseCase: GetAllRoasterDataUseCase,
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    var isPregnancyVisible: Boolean = true
    var pregnancyOutcomeCount: String = ""
    var pregnancyOutcome: String = NO
    var pregnancyCount: String = ""
    var patientUuid: String = ""
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

    private val _isDataInserted = MutableLiveData(false)
    val isDataInserted: LiveData<Boolean> = _isDataInserted

    fun updateRosterStage(stage: RosterQuestionnaireStage) {
        mutableLiveRosterStage.postValue(stage)
    }

    fun addPregnancyOutcome(questionList: List<RoasterViewQuestion>, editPosition: Int = -1) {
        val pregnancyOutComeModel = PregnancyOutComeModel(
            title = questionList[0].localAnswer ?: questionList[0].answer,
            roasterViewQuestion = questionList
        )
        val list = _outComeLiveList.value ?: mutableListOf()
        if (editPosition < 0) {
            list.add(pregnancyOutComeModel)
        } else {
            list[editPosition] = pregnancyOutComeModel
        }
        _outComeLiveList.value = ArrayList(list)
    }

    fun addHealthService(questionList: List<RoasterViewQuestion>, editPosition: Int = -1) {
        val healthServiceModel = HealthServiceModel(
            title = questionList[0].localAnswer ?: questionList[0].answer,
            roasterViewQuestion = questionList
        )
        val list = _healthServiceLiveList.value ?: mutableListOf()
        if (editPosition < 0) {
            list.add(healthServiceModel)
        } else {
            list[editPosition] = healthServiceModel
        }
        _healthServiceLiveList.value = ArrayList(list)
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

    fun getOutcomeQuestionList(): ArrayList<RoasterViewQuestion> = getOutComeQuestionUseCase()

    fun getHealthServiceList(): ArrayList<RoasterViewQuestion> = getHealthServiceQuestionUseCase()

    fun getGeneralQuestionList() {
        _generalLiveList.postValue(getGeneralQuestionUseCase())
    }

    fun insertRoster() {
        viewModelScope.launch(ioDispatcher) {
            insertRoasterUseCase(
                patientUuid,
                generalLiveList.value,
                outComeLiveList.value,
                _healthServiceLiveList.value,
                pregnancyOutcome,
                pregnancyCount,
                pregnancyOutcomeCount,
            )
            _isDataInserted.postValue(true)
        }
    }

    fun getRoasterData() {
        viewModelScope.launch(ioDispatcher) {
            val allAttributeData = getAllRoasterDataUseCase.fetchAllData(patientUuid)

            // Fetch and post general data
            _generalLiveList.postValue(
                getAllRoasterDataUseCase.getGeneralData(
                    allAttributeData,
                    getGeneralQuestionUseCase()
                )
            )

            // Extract pregnancy-related data
            val pregnancyDataMap = mapOf(
                RoasterAttribute.NO_OF_TIME_PREGNANT.attributeName to { value: String ->
                    pregnancyCount = value
                },
                RoasterAttribute.PREGNANCY_PAST_TWO_YEARS.attributeName to { value: String ->
                    pregnancyOutcome = value
                },
                RoasterAttribute.NO_OF_PREGNANCY_OUTCOME_TWO_YEARS.attributeName to { value: String ->
                    pregnancyOutcomeCount = value
                }
            )

            pregnancyDataMap.forEach { (key, setter) ->
                allAttributeData.find { it.personAttributeTypeUuid == key }?.value?.let(setter)
            }

            // Fetch and post pregnancy outcome models
            _outComeLiveList.postValue(
                getAllRoasterDataUseCase.getPregnancyData(
                    allAttributeData,
                    getOutcomeQuestionList()
                )
                        as ArrayList<PregnancyOutComeModel>?
            )

            // Fetch and post health service models
            _healthServiceLiveList.postValue(
                getAllRoasterDataUseCase.getHealthServiceData(
                    allAttributeData,
                    getHealthServiceList()
                )
                        as ArrayList<HealthServiceModel>?
            )
        }
    }

    fun validateGeneralList(): Int? {
        return _generalLiveList.value?.indexOfFirst { it.answer.isNullOrEmpty() }
            .takeIf { it != -1 }
    }

    fun validatePregnancyOutcomeList(questions: List<RoasterViewQuestion>): Int? {
        return questions.indexOfFirst { it.isVisible && it.answer.isNullOrEmpty() }.takeIf { it != -1 }
    }


}