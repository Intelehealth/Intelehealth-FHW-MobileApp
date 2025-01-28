package org.intelehealth.app.ui.rosterquestionnaire.usecase

import com.google.gson.Gson
import org.intelehealth.app.database.dao.SyncDAO
import org.intelehealth.app.models.dto.PatientAttributesDTO
import org.intelehealth.app.ui.rosterquestionnaire.model.HealthIssues
import org.intelehealth.app.ui.rosterquestionnaire.model.HealthServiceModel
import org.intelehealth.app.ui.rosterquestionnaire.model.PregnancyOutComeModel
import org.intelehealth.app.ui.rosterquestionnaire.model.PregnancyRosterData
import org.intelehealth.app.ui.rosterquestionnaire.model.RoasterViewQuestion
import org.intelehealth.app.ui.rosterquestionnaire.repository.RosterRepository

import org.intelehealth.app.ui.rosterquestionnaire.utilities.RoasterAttribute
import java.util.UUID
import javax.inject.Inject

class InsertRoasterUseCase @Inject constructor(private val repository: RosterRepository) {
    operator fun invoke(
        uuid: String,
        generalQuestionList: ArrayList<RoasterViewQuestion>?,
        pregnancyOutcomeList: ArrayList<PregnancyOutComeModel>?,
        healthServiceModelList: ArrayList<HealthServiceModel>?,
        pregnancyOutcome: String,
        pregnancyCount: String,
        pregnancyOutcomeCount: String,
    ) {
        val patientAttributesDTOList = ArrayList<PatientAttributesDTO>()

        // Add Patient General
        prepareGeneralData(generalQuestionList, uuid, patientAttributesDTOList)

        // Add Patient Outcome
        preparePregnancyData(
            uuid,
            pregnancyOutcome,
            pregnancyCount,
            pregnancyOutcomeCount,
            patientAttributesDTOList,
            pregnancyOutcomeList
        )

        // Add Health Service
        prepareHealthService(healthServiceModelList, uuid, patientAttributesDTOList)

        repository.insertRoaster(patientAttributesDTOList)
        val syncDAO = SyncDAO()
        syncDAO.pushDataApi()
    }

    private fun prepareHealthService(
        healthServiceModelList: ArrayList<HealthServiceModel>?,
        uuid: String,
        patientAttributesDTOList: ArrayList<PatientAttributesDTO>,
    ) {
        val healthIssueModelList = ArrayList<HealthIssues>()
        healthServiceModelList?.forEach {
            val healthServiceModel = it.roasterViewQuestion
            val healthIssueModel = HealthIssues(
                healthIssueReported = healthServiceModel[0].answer ?: "",
                numberOfEpisodesInTheLastYear = healthServiceModel[1].answer ?: "",
                primaryHealthcareProviderValue = healthServiceModel[2].answer ?: "",
                firstLocationOfVisit = healthServiceModel[3].answer ?: "",
                referredTo = healthServiceModel[4].answer ?: "",
                modeOfTransportation = healthServiceModel[5].answer ?: "",
                averageCostOfTravelAndStayPerEpisode = healthServiceModel[6].answer ?: "",
                averageCostOfConsultation = healthServiceModel[7].answer ?: "",
                averageCostOfMedicine = healthServiceModel[8].answer ?: "",
                scoreForExperienceOfTreatment = healthServiceModel[9].answer ?: "",
            )
            healthIssueModelList.add(healthIssueModel)
            val patientAttributesDTO = PatientAttributesDTO()
            patientAttributesDTO.uuid = UUID.randomUUID().toString()
            patientAttributesDTO.patientuuid = uuid
            patientAttributesDTO.personAttributeTypeUuid =
                RoasterAttribute.HEALTH_ISSUE_REPORTED.attributeName
            patientAttributesDTO.value = Gson().toJson(healthIssueModelList)
            patientAttributesDTOList.add(patientAttributesDTO)
        }
    }

    private fun prepareGeneralData(
        generalQuestionList: ArrayList<RoasterViewQuestion>?,
        uuid: String,
        patientAttributesDTOList: ArrayList<PatientAttributesDTO>,
    ) {
        generalQuestionList?.forEach {
            val patientAttributesDTO = PatientAttributesDTO()
            patientAttributesDTO.uuid = it.uuid ?: UUID.randomUUID().toString()
            patientAttributesDTO.patientuuid = uuid
            patientAttributesDTO.personAttributeTypeUuid = it.attribute
            patientAttributesDTO.value = it.answer
            patientAttributesDTOList.add(patientAttributesDTO)
        }

    }

    private fun preparePregnancyData(
        uuid: String,
        pregnancyOutcome: String,
        pregnancyCount: String,
        pregnancyOutcomeCount: String,
        patientAttributesDTOList: ArrayList<PatientAttributesDTO>,
        pregnancyOutcomeList: ArrayList<PregnancyOutComeModel>?,
    ) {
        var patientAttributesDTO = PatientAttributesDTO()
        patientAttributesDTO.uuid = UUID.randomUUID().toString()
        patientAttributesDTO.patientuuid = uuid
        patientAttributesDTO.personAttributeTypeUuid =
            RoasterAttribute.NO_OF_TIME_PREGNANT.attributeName
        patientAttributesDTO.value = pregnancyCount
        patientAttributesDTOList.add(patientAttributesDTO)

        patientAttributesDTO = PatientAttributesDTO()
        patientAttributesDTO.uuid = UUID.randomUUID().toString()
        patientAttributesDTO.patientuuid = uuid
        patientAttributesDTO.personAttributeTypeUuid =
            RoasterAttribute.PREGNANCY_PAST_TWO_YEARS.attributeName
        patientAttributesDTO.value = pregnancyOutcome
        patientAttributesDTOList.add(patientAttributesDTO)


        patientAttributesDTO = PatientAttributesDTO()
        patientAttributesDTO.uuid = UUID.randomUUID().toString()
        patientAttributesDTO.patientuuid = uuid
        patientAttributesDTO.personAttributeTypeUuid =
            RoasterAttribute.NO_OF_PREGNANCY_OUTCOME_TWO_YEARS.attributeName
        patientAttributesDTO.value = pregnancyOutcomeCount
        patientAttributesDTOList.add(patientAttributesDTO)

        val pregnancyRosterList = ArrayList<PregnancyRosterData>()
        pregnancyOutcomeList?.forEach {
            val pregnancyOutComeQuestion = it.roasterViewQuestion
            val pregnancyRosterData = PregnancyRosterData(
                pregnancyOutcome = pregnancyOutComeQuestion[0].answer ?: "",
                isChildAlive = pregnancyOutComeQuestion[1].answer ?: "",
                isPreTerm = pregnancyOutComeQuestion[2].answer ?: "",
                yearOfPregnancyOutcome = pregnancyOutComeQuestion[3].answer ?: "",
                monthsOfPregnancy = pregnancyOutComeQuestion[4].answer ?: "",
                monthsBeenPregnant = pregnancyOutComeQuestion[5].answer ?: "",
                placeOfDelivery = pregnancyOutComeQuestion[6].answer ?: "",
                typeOfDelivery = pregnancyOutComeQuestion[7].answer ?: "",
                focalFacilityForPregnancy = pregnancyOutComeQuestion[8].answer ?: "",
                facilityName = pregnancyOutComeQuestion[9].answer ?: "",
                singleMultipleBirths = pregnancyOutComeQuestion[10].answer ?: "",
                babyAgeDied = pregnancyOutComeQuestion[11].answer ?: "",
                sexOfBaby = pregnancyOutComeQuestion[12].answer ?: "",
                pregnancyPlanned = pregnancyOutComeQuestion[13].answer ?: "",
                highRiskPregnancy = pregnancyOutComeQuestion[14].answer ?: "",
                pregnancyComplications = pregnancyOutComeQuestion[15].answer ?: "",
            )
            pregnancyRosterList.add(pregnancyRosterData)
        }
        patientAttributesDTO = PatientAttributesDTO()
        patientAttributesDTO.uuid = UUID.randomUUID().toString()
        patientAttributesDTO.patientuuid = uuid
        patientAttributesDTO.personAttributeTypeUuid =
            RoasterAttribute.PREGNANCY_OUTCOME_REPORTED.attributeName
        patientAttributesDTO.value = Gson().toJson(pregnancyRosterList)
        patientAttributesDTOList.add(patientAttributesDTO)

    }


}