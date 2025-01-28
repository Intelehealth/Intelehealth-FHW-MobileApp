package org.intelehealth.app.ui.rosterquestionnaire.usecase

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.models.dto.PatientAttributesDTO
import org.intelehealth.app.ui.rosterquestionnaire.model.HealthIssues
import org.intelehealth.app.ui.rosterquestionnaire.model.HealthServiceModel
import org.intelehealth.app.ui.rosterquestionnaire.model.PregnancyOutComeModel
import org.intelehealth.app.ui.rosterquestionnaire.model.PregnancyRosterData
import org.intelehealth.app.ui.rosterquestionnaire.model.RoasterViewQuestion
import org.intelehealth.app.ui.rosterquestionnaire.repository.RosterRepository
import org.intelehealth.app.ui.rosterquestionnaire.utilities.BORN_ALIVE
import org.intelehealth.app.ui.rosterquestionnaire.utilities.CURRENTLY_PREGNANT
import org.intelehealth.app.ui.rosterquestionnaire.utilities.INDUCED_ABORTION
import org.intelehealth.app.ui.rosterquestionnaire.utilities.MISCARRIAGE
import org.intelehealth.app.ui.rosterquestionnaire.utilities.RoasterAttribute
import org.intelehealth.app.ui.rosterquestionnaire.utilities.RoasterQuestionView
import org.intelehealth.app.ui.rosterquestionnaire.utilities.STILL_BIRTH
import org.intelehealth.app.utilities.LanguageUtils

import javax.inject.Inject

class GetAllRoasterDataUseCase @Inject constructor(private val repository: RosterRepository) {
    private val context = IntelehealthApplication.getAppContext()
    fun fetchAllData(patientId: String): ArrayList<PatientAttributesDTO> {
        return repository.getAllRoasterData(patientId)
    }

    fun getGeneralData(
        attributeList: ArrayList<PatientAttributesDTO>,
        generalQuestion: ArrayList<RoasterViewQuestion>,
    ): ArrayList<RoasterViewQuestion> {
        // Iterate through each item in generalQuestion
        generalQuestion.forEach { question ->
            // Find a matching attribute in the attributeList based on the attribute and personAttributeTypeUuid
            val matchingAttribute = attributeList.find {
                it.personAttributeTypeUuid == question.attribute
            }
            // If a match is found, update the answer in the generalQuestion
            if (matchingAttribute != null) {
                question.answer = matchingAttribute.value
            }
            if (question.layoutId == RoasterQuestionView.SPINNER) {
                val englishList =
                    LanguageUtils.getStringArrayInLocale(context, question.spinnerItem!!, "en")
                val listItem = context.resources.getStringArray(question.spinnerItem)
                    .toList()
                val position = englishList.indexOf(question.answer)
                if (position != -1) {
                    question.answer = englishList[position]
                    question.localAnswer = listItem[position]
                }
            }
        }
        return generalQuestion
    }

    fun getPregnancyData(
        allAttributeData: List<PatientAttributesDTO>,
        outcomeQuestionList: List<RoasterViewQuestion>,
    ): List<PregnancyOutComeModel> {
        // Find matching pregnancy outcome data
        val pregnancyDataJson =
            allAttributeData.find { it.personAttributeTypeUuid == RoasterAttribute.PREGNANCY_OUTCOME_REPORTED.attributeName }?.value
                ?: ""
        val pregnancyList = parseJsonToPregnancyRosterData(pregnancyDataJson)

        // Build the PregnancyOutComeModel list
        val pregnancyOutComeList = pregnancyList.map { pregnancy ->
            val pregnancyOutComeQuestion = outcomeQuestionList.map { it.copy() }.apply {
                if (size >= 15) {
                    this[0].answer = pregnancy.pregnancyOutcome
                    this[1].answer = pregnancy.isChildAlive
                    this[2].answer = pregnancy.isPreTerm
                    this[3].answer = pregnancy.yearOfPregnancyOutcome
                    this[4].answer = pregnancy.monthsOfPregnancy
                    this[5].answer = pregnancy.monthsBeenPregnant
                    this[6].answer = pregnancy.placeOfDelivery
                    this[7].answer = pregnancy.typeOfDelivery
                    this[8].answer = pregnancy.focalFacilityForPregnancy
                    this[9].answer = pregnancy.facilityName
                    this[10].answer = pregnancy.singleMultipleBirths
                    this[11].answer = pregnancy.babyAgeDied
                    this[12].answer = pregnancy.sexOfBaby
                    this[13].answer = pregnancy.pregnancyPlanned
                    this[14].answer = pregnancy.highRiskPregnancy
                    this[15].answer = pregnancy.pregnancyComplications

                }
            }

            pregnancyOutComeQuestion.forEachIndexed { index, item ->
                if (item.layoutId == RoasterQuestionView.SPINNER) {
                    val englishList =
                        LanguageUtils.getStringArrayInLocale(context, item.spinnerItem!!, "en")
                    val listItem = context.resources.getStringArray(item.spinnerItem)
                        .toList()
                    val position = englishList.indexOf(item.answer)
                    if (position != -1) {
                        item.answer = englishList[position]
                        item.localAnswer = listItem[position]
                    }
                }
                changePregnancyVisibility(index, item, pregnancyOutComeQuestion)
            }
            PregnancyOutComeModel(
                title = pregnancyOutComeQuestion[0].localAnswer?:pregnancyOutComeQuestion[0].answer,
                roasterViewQuestion = pregnancyOutComeQuestion
            )
        }


        return pregnancyOutComeList
    }

    private fun changePregnancyVisibility(
        position: Int,
        roasterQuestion: RoasterViewQuestion,
        pregnancyOutcomeList: List<RoasterViewQuestion>,
    ) {
        if (position != 0) return

        val visibilityRules = mapOf(
            BORN_ALIVE to listOf(5, 11),
            STILL_BIRTH to listOf(1, 2, 5, 11),
            INDUCED_ABORTION to listOf(1, 2, 5, 8, 9, 10, 11, 12, 15),
            MISCARRIAGE to listOf(1, 2, 5, 6, 8, 9, 10, 11, 12, 15),
            CURRENTLY_PREGNANT to listOf(1, 2, 3, 4, 6, 7, 8, 9, 10, 11)
        )

        val hiddenIndices = visibilityRules[roasterQuestion.answer] ?: emptyList()

        pregnancyOutcomeList.forEachIndexed { index, item ->
            item.isVisible = index !in hiddenIndices
        }
    }

    private fun parseJsonToPregnancyRosterData(jsonString: String): List<PregnancyRosterData> {
        val gson = Gson()
        val listType = object : TypeToken<List<PregnancyRosterData>>() {}.type
        return gson.fromJson(jsonString, listType)
    }

    fun getHealthServiceData(
        allAttributeData: List<PatientAttributesDTO>,
        healthServiceList: List<RoasterViewQuestion>,
    ): List<HealthServiceModel> {
        // Find the matching health issues data
        val healthIssuesJson =
            allAttributeData.find { it.personAttributeTypeUuid == RoasterAttribute.HEALTH_ISSUE_REPORTED.attributeName }?.value
                ?: ""
        val healthIssuesList = parseJsonToHealthRosterData(healthIssuesJson)

        // Build the HealthServiceModel list
        val healthServiceModelList = healthIssuesList.map { healthIssue ->
            val healthServiceQuestionList = healthServiceList.map { it.copy() }.apply {
                if (size >= 10) {
                    this[0].answer = healthIssue.healthIssueReported
                    this[1].answer = healthIssue.numberOfEpisodesInTheLastYear
                    this[2].answer = healthIssue.primaryHealthcareProviderValue
                    this[3].answer = healthIssue.firstLocationOfVisit
                    this[4].answer = healthIssue.referredTo
                    this[5].answer = healthIssue.modeOfTransportation
                    this[6].answer = healthIssue.averageCostOfTravelAndStayPerEpisode
                    this[7].answer = healthIssue.averageCostOfConsultation
                    this[8].answer = healthIssue.averageCostOfMedicine
                    this[9].answer = healthIssue.scoreForExperienceOfTreatment
                }
            }

            healthServiceQuestionList.forEachIndexed { _, item ->
                if (item.layoutId == RoasterQuestionView.SPINNER) {
                    val englishList =
                        LanguageUtils.getStringArrayInLocale(context, item.spinnerItem!!, "en")
                    val listItem = context.resources.getStringArray(item.spinnerItem)
                        .toList()
                    val position = englishList.indexOf(item.answer)
                    if (position != -1) {
                        item.answer = englishList[position]
                        item.localAnswer = listItem[position]
                    }
                }
            }

            HealthServiceModel(
                title = healthServiceQuestionList[0].localAnswer
                    ?: healthServiceQuestionList[0].answer,
                roasterViewQuestion = healthServiceQuestionList
            )
        }

        return healthServiceModelList
    }

    private fun parseJsonToHealthRosterData(jsonString: String): List<HealthIssues> {
        val gson = Gson()
        val listType = object : TypeToken<List<HealthIssues>>() {}.type
        return gson.fromJson(jsonString, listType)
    }
}