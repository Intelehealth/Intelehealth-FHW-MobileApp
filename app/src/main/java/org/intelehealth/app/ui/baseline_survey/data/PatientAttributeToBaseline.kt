package org.intelehealth.app.ui.baseline_survey.data

import com.google.gson.Gson
import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.app.models.dto.PatientAttributesDTO.Column
import org.intelehealth.app.models.pushRequestApiCall.Attribute
import org.intelehealth.app.ui.baseline_survey.model.AlcoholConsumptionHistory
import org.intelehealth.app.ui.baseline_survey.model.Baseline
import org.intelehealth.app.ui.baseline_survey.model.MedicalHistory
import org.intelehealth.app.ui.baseline_survey.model.SmokingHistory
import org.intelehealth.app.ui.baseline_survey.model.TobaccoHistory
import org.intelehealth.app.utilities.extensions.getCultivableLandUnit
import org.intelehealth.app.utilities.extensions.getCultivableLandValue
import org.intelehealth.app.utilities.extensions.getHyphenOrRelation
import org.intelehealth.app.utilities.extensions.returnEmptyIfHyphen

class PatientAttributeToBaseline(private val patientsDAO: PatientsDAO) {

    fun getBaselineData(patientAttributeList: List<Attribute>): Baseline {
        return Baseline().apply {
            getGeneralData(list = patientAttributeList, baseline = this)
            getMedicalData(list = patientAttributeList, baseline = this)
            getOtherData(list = patientAttributeList, baseline = this)
        }
    }

    private fun getGeneralData(
        baseline: Baseline,
        list: List<Attribute>,
        patientsDAO: PatientsDAO = this.patientsDAO
    ) {
        list.forEach {
            val personTypeAttributeName = patientsDAO.getAttributesName(it.attributeType)
            when (personTypeAttributeName) {
                Column.CASTE.value -> baseline.caste = it.value.returnEmptyIfHyphen()
                Column.EDUCATION.value -> baseline.education = it.value.returnEmptyIfHyphen()
                Column.OCCUPATION.value -> baseline.occupation = it.value.returnEmptyIfHyphen()
                Column.BANK_ACCOUNT.value -> baseline.bankAccount = it.value.returnEmptyIfHyphen()
                Column.USE_WHATSAPP.value -> baseline.familyWhatsApp =
                    it.value.returnEmptyIfHyphen()

                Column.MARTIAL_STATUS.value -> baseline.martialStatus =
                    it.value.returnEmptyIfHyphen()

                Column.MGNREGA_CARD_STATUS.value -> baseline.mgnregaCard =
                    it.value.returnEmptyIfHyphen()

                Column.MOBILE_PHONE_TYPE.value -> baseline.phoneOwnership =
                    it.value.returnEmptyIfHyphen()

                Column.AYUSHMAN_CARD_STATUS.value -> baseline.ayushmanCard =
                    it.value.returnEmptyIfHyphen()
            }
        }
    }

    private fun getMedicalData(
        baseline: Baseline,
        list: List<Attribute>,
        patientsDAO: PatientsDAO = this.patientsDAO
    ) {
        list.forEach {
            val personTypeAttributeName = patientsDAO.getAttributesName(it.attributeType)
            when (personTypeAttributeName) {
                Column.HB_CHECKED.value -> baseline.hbCheck = it.value.returnEmptyIfHyphen()
                Column.BP_CHECKED.value -> baseline.bpCheck = it.value.returnEmptyIfHyphen()
                Column.SUGAR_CHECKED.value -> baseline.sugarCheck = it.value.returnEmptyIfHyphen()
                Column.OTHER_MEDICAL_HISTORY.value -> extractMedicalHistoryData(baseline, it.value)
                Column.SMOKING_STATUS.value -> extractSmokingHistoryData(baseline, it.value)
                Column.TOBACCO_STATUS.value -> extractTobaccoHistoryData(baseline, it.value)
                Column.ALCOHOL_CONSUMPTION_STATUS.value -> extractAlcoholHistoryData(
                    baseline,
                    it.value
                )
            }
        }
    }

    private fun getOtherData(
        baseline: Baseline,
        list: List<Attribute>,
        patientsDAO: PatientsDAO = this.patientsDAO
    ) {
        list.forEach {
            val personTypeAttributeName = patientsDAO.getAttributesName(it.attributeType)
            when (personTypeAttributeName) {
                Column.HOH_RELATIONSHIP.value -> it.value.getHyphenOrRelation(baseline)

                Column.RATION_CARD.value -> baseline.rationCardCheck =
                    it.value.returnEmptyIfHyphen()

                Column.ECONOMIC_STATUS.value -> baseline.economicStatus =
                    it.value.returnEmptyIfHyphen()

                Column.RELIGION.value -> baseline.religion = it.value.returnEmptyIfHyphen()
                Column.TOTAL_FAMILY_MEMBERS.value -> baseline.totalHouseholdMembers =
                    it.value.returnEmptyIfHyphen()

                Column.TOTAl_FAMILY_MEMBERS_STAYING.value -> baseline.usualHouseholdMembers =
                    it.value.returnEmptyIfHyphen()

                Column.NUMBER_OF_SMARTPHONES.value -> baseline.numberOfSmartphones =
                    it.value.returnEmptyIfHyphen()

                Column.NUMBER_OF_FEATURE_PHONES.value -> baseline.numberOfFeaturePhones =
                    it.value.returnEmptyIfHyphen()

                Column.NUMBER_OF_EARNING_MEMBERS.value -> baseline.numberOfEarningMembers =
                    it.value.returnEmptyIfHyphen()

                Column.ELECTRICITY_STATUS.value -> baseline.electricityCheck =
                    it.value.returnEmptyIfHyphen()

                Column.LOAD_SHEDDING_HOURS_PER_DAY.value -> baseline.loadSheddingHours =
                    it.value.returnEmptyIfHyphen()

                Column.LOAD_SHEDDING_DAYS_PER_WEEK.value -> baseline.loadSheddingDays =
                    it.value.returnEmptyIfHyphen()

                Column.RUNNING_WATER_AVAILABILITY.value -> baseline.waterCheck =
                    it.value.returnEmptyIfHyphen()

                Column.WATER_SUPPLY_AVAILABILITY_HOURS_PER_DAY.value -> baseline.waterAvailabilityHours =
                    it.value.returnEmptyIfHyphen()

                Column.WATER_SUPPLY_AVAILABILITY_DAYS_PER_WEEK.value -> baseline.waterAvailabilityDays =
                    it.value.returnEmptyIfHyphen()

                Column.DRINKING_WATER_SOURCE.value -> baseline.sourceOfWater =
                    it.value.returnEmptyIfHyphen()

                Column.SAFE_DRINKING_WATER.value -> baseline.safeguardWater =
                    it.value.returnEmptyIfHyphen()

                Column.TIME_DRINKING_WATER_SOURCE.value -> baseline.distanceFromWater =
                    it.value.returnEmptyIfHyphen()

                Column.TOILET_FACILITY.value -> baseline.toiletFacility =
                    it.value.returnEmptyIfHyphen()

                Column.HOUSE_STRUCTURE.value -> baseline.houseStructure =
                    it.value.returnEmptyIfHyphen()

                Column.FAMILY_CULTIVABLE_LAND.value -> {
                    baseline.cultivableLand = it.value.getCultivableLandUnit().returnEmptyIfHyphen()
                    baseline.cultivableLandValue =
                        it.value.getCultivableLandValue().returnEmptyIfHyphen()
                }

                Column.AVERAGE_ANNUAL_HOUSEHOLD_INCOME.value -> baseline.averageIncome =
                    it.value.returnEmptyIfHyphen()

                Column.COOKING_FUEL.value -> baseline.fuelType = it.value.returnEmptyIfHyphen()
                Column.HOUSEHOLD_LIGHTING.value -> baseline.sourceOfLight =
                    it.value.returnEmptyIfHyphen()

                Column.SOAP_HAND_WASHING_OCCASION.value -> baseline.handWashPractices =
                    it.value.returnEmptyIfHyphen()

                Column.TAKE_OUR_SERVICE.value -> baseline.ekalServiceCheck =
                    it.value.returnEmptyIfHyphen()
            }
        }
    }

    private fun extractMedicalHistoryData(baseline: Baseline, data: String) {
        val medicalHistory: MedicalHistory = Gson().fromJson(data, MedicalHistory::class.java)
        baseline.anemiaValue = medicalHistory.anemia.returnEmptyIfHyphen()
        baseline.bpValue = medicalHistory.hypertension.returnEmptyIfHyphen()
        baseline.diabetesValue = medicalHistory.diabetes.returnEmptyIfHyphen()
        baseline.arthritisValue = medicalHistory.arthritis.returnEmptyIfHyphen()
        baseline.surgeryValue = medicalHistory.anySurgeries.returnEmptyIfHyphen()
        baseline.surgeryReason = medicalHistory.reasonForSurgery.returnEmptyIfHyphen()
    }

    private fun extractSmokingHistoryData(baseline: Baseline, data: String) {
        val smokingHistory: SmokingHistory = Gson().fromJson(data, SmokingHistory::class.java)
        baseline.smokingHistory = smokingHistory.smokingStatus
        baseline.smokingRate = smokingHistory.rateOfSmoking
        baseline.smokingDuration = smokingHistory.durationOfSmoking
        baseline.smokingFrequency = smokingHistory.frequencyOfSmoking
    }

    private fun extractAlcoholHistoryData(baseline: Baseline, data: String) {
        val alcoholConsumptionHistory: AlcoholConsumptionHistory = Gson().fromJson(
            data,
            AlcoholConsumptionHistory::class.java
        )

        baseline.alcoholRate = alcoholConsumptionHistory.rateOfAlcoholConsumption
        baseline.alcoholHistory = alcoholConsumptionHistory.historyOfAlcoholConsumption
        baseline.alcoholDuration = alcoholConsumptionHistory.durationOfAlcoholConsumption
        baseline.alcoholFrequency = alcoholConsumptionHistory.frequencyOfAlcoholConsumption
    }

    private fun extractTobaccoHistoryData(baseline: Baseline, data: String) {
        val tobaccoHistory: TobaccoHistory = Gson().fromJson(data, TobaccoHistory::class.java)
        baseline.chewTobacco = tobaccoHistory.chewTobaccoStatus
    }
}