package org.intelehealth.app.ui.householdSurvey.repository

import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.github.ajalt.timberkt.Timber
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import org.intelehealth.app.ui.householdSurvey.models.HouseholdSurveyModel
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.database.dao.ImagesPushDAO
import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.app.database.dao.SyncDAO
import org.intelehealth.app.models.dto.PatientAttributesDTO
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.utilities.NetworkConnection
import org.intelehealth.app.utilities.exception.DAOException
import java.util.UUID

class HouseholdRepository(
    private val patientsDao: PatientsDAO,
    private val sqlHelper: SQLiteOpenHelper,
) {
    fun addHouseholdPatientAttributes(
        fragmentIdentifier: String,
        patient: PatientDTO,
        householdSurveyModel: HouseholdSurveyModel
    ): Boolean {
        bindPatientAttributes(fragmentIdentifier, patient, householdSurveyModel).let {
            val flag = patientsDao.updatePatientSurveyInDb(it.uuid, it.patientAttributesDTOList)
            //sync on 7th screen only as per nas 3.0
            if (fragmentIdentifier.isNotEmpty() && fragmentIdentifier == "seventhScreen") {
                syncOnServer()
            }
            return flag
        }
    }

    fun updateHouseholdPatientAttributes(
        fragmentIdentifier: String,
        patient: PatientDTO,
        householdSurveyModel: HouseholdSurveyModel
    ): Boolean {
        return bindPatientAttributes(fragmentIdentifier, patient, householdSurveyModel).let {
            val flag = patientsDao.updatePatientSurveyInDb(it.uuid, it.patientAttributesDTOList)
            syncOnServer()
            return flag
        }
    }

    private fun bindPatientAttributes(
        fragmentIdentifier: String,
        patient: PatientDTO,
        householdSurveyModel: HouseholdSurveyModel
    ) = patient.apply {
        patientAttributesDTOList =
            createPatientAttributes(fragmentIdentifier, patient, householdSurveyModel, patient.uuid)
        syncd = false
    }

    private fun mapAllScreenAttributes(
        fragmentIdentifier: String,
        patient: PatientDTO,
        householdSurveyModel: HouseholdSurveyModel,
        uuid: String?
    ) {

    }

    fun fetchPatient(uuid: String): HouseholdSurveyModel {
        Timber.d { "uuid => $uuid" }
        val patientsDao = PatientsDAO()
        var householdSurveyModel =
            HouseholdSurveyModel()
        var houseHoldValue = ""
        try {
            houseHoldValue = patientsDao.getHouseHoldValue(uuid)
        } catch (e: DAOException) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
        if (houseHoldValue.isNotEmpty()) {
            // Fetch all patient UUIDs from houseHoldValue
            try {
                val patientUUIDs = ArrayList(patientsDao.getPatientUUIDs(houseHoldValue))
                for (uuid in patientUUIDs) {
                    householdSurveyModel = getAllRecords(uuid)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return householdSurveyModel
    }

    private fun createPatientAttribute(
        patientId: String,
        attrName: String,
        value: String?
    ): PatientAttributesDTO? {
        return if (!value.isNullOrEmpty()) {
            PatientAttributesDTO().apply {
                uuid = UUID.randomUUID().toString()
                patientuuid = patientId
                personAttributeTypeUuid = patientsDao.getUuidForAttribute(attrName)
                this.value = value
            }
        } else {
            null // Return null if value is null or empty
        }
    }


    /*private fun createPatientAttribute(
        patientId: String,
        attrName: String,
        value: String?
    ) = PatientAttributesDTO().apply {
        uuid = UUID.randomUUID().toString()
        patientuuid = patientId
        personAttributeTypeUuid = patientsDao.getUuidForAttribute(attrName)
        this.value = value
    }*/


    fun syncOnServer() {
        if (NetworkConnection.isOnline(IntelehealthApplication.getAppContext())) {
            val syncDAO = SyncDAO()
            val imagesPushDAO = ImagesPushDAO()
            syncDAO.pushDataApi()
            imagesPushDAO.patientProfileImagesPush()
        }
    }

    private fun getAllRecords(patientUuid: String): HouseholdSurveyModel {
        val db = IntelehealthApplication.inteleHealthDatabaseHelper.writeDb
        val patientsDao = PatientsDAO()
        var householdSurveyModel =
            HouseholdSurveyModel()
        val patientSelection1 = "patientuuid = ?"
        val patientArgs1 = arrayOf(patientUuid)
        val patientColumns1 = arrayOf("value", "person_attribute_type_uuid")
        val idCursor1 = db.query(
            "tbl_patient_attribute",
            patientColumns1,
            patientSelection1,
            patientArgs1,
            null,
            null,
            null
        )
        Log.d("devkzchk1111", "setData: cursor count : ${idCursor1.count}")
        var name = ""
        var count = 0
        if (idCursor1.moveToFirst()) {
            do {
                try {
                    count++
                    Log.d("devkzchk1111", "getAllRecords: count : " + count)
                    name = patientsDao.getAttributesName(
                        idCursor1.getString(
                            idCursor1.getColumnIndexOrThrow("person_attribute_type_uuid")
                        )
                    )
                } catch (e: DAOException) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                }

                val value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))
                Log.d("devkzchk", "setData: value from attributes  : $value1")

                when {
                    name.equals("NamePrimaryRespondent", ignoreCase = true) -> {
                        if (!value1.isNullOrEmpty() && value1 != "-") {
                            householdSurveyModel.namePrimaryRespondent = value1
                        }
                    }

                    name.equals("HouseholdNumber", ignoreCase = true) -> {
                        if (!value1.isNullOrEmpty() && value1 != "-") {
                            householdSurveyModel.householdNumberOfSurvey = value1
                        }
                    }

                    name.equals("HouseStructure", ignoreCase = true) -> {
                        householdSurveyModel.houseStructure = value1
                    }

                    name.equals("ResultOfVisit", ignoreCase = true) -> {
                        householdSurveyModel.resultOfVisit = value1
                    }
                    //second screen
                    name.equals("householdHeadName", ignoreCase = true) -> {
                        householdSurveyModel.headOfHouseholdName = value1
                    }

                    name.equals("householdHeadReligion", ignoreCase = true) -> {
                        householdSurveyModel.religion = value1
                    }

                    name.equals("householdHeadCaste", ignoreCase = true) -> {
                        householdSurveyModel.caste = value1
                    }

                    name.equals("noOfSmartphones", ignoreCase = true) -> {
                        householdSurveyModel.numberOfSmartPhones = value1
                    }

                    name.equals("noOfFeaturePhones", ignoreCase = true) -> {
                        householdSurveyModel.numberOfFeaturePhones = value1
                    }

                    name.equals("noOfEarningMembers", ignoreCase = true) -> {
                        householdSurveyModel.numberOfEarningMembers = value1
                    }

                    name.equals("primarySourceOfIncome", ignoreCase = true) -> {
                        householdSurveyModel.primarySourceOfIncome = value1
                    }

                    //third screen
                    name.equals("householdElectricityStatus", ignoreCase = true) -> {
                        householdSurveyModel.householdElectricityStatus = value1
                    }

                    name.equals("noOfLoadSheddingHrsPerDay", ignoreCase = true) -> {
                        householdSurveyModel.noOfHoursOfLoadSheddingPerDay = value1
                    }

                    name.equals("noOfLoadSheddingHrsPerWeek", ignoreCase = true) -> {
                        householdSurveyModel.noOfDaysOfLoadSheddingPerWeek = value1
                    }

                    name.equals("runningWaterStatus", ignoreCase = true) -> {
                        householdSurveyModel.runningWaterStatus = value1
                    }

                    name.equals("primarySourceOfRunningWater", ignoreCase = true) -> {
                        householdSurveyModel.primarySourceOfRunningWater = value1
                    }

                    name.equals("waterSourceDistance", ignoreCase = true) -> {
                        householdSurveyModel.waterSourceDistance = value1
                    }

                    name.equals("waterSupplyAvailabilityHrsPerDay", ignoreCase = true) -> {
                        householdSurveyModel.waterSupplyAvailabilityHrsPerDay = value1
                    }

                    name.equals("waterSupplyAvailabilityDaysperWeek", ignoreCase = true) -> {
                        householdSurveyModel.waterSupplyAvailabilityDaysperWeek = value1
                    }

                    name.equals("householdBankAccountStatus", ignoreCase = true) -> {
                        householdSurveyModel.householdBankAccountStatus = value1
                    }
                    //fourth fragment
                    name.equals("householdCultivableLand", ignoreCase = true) -> {
                        householdSurveyModel.householdCultivableLand = value1
                    }

                    name.equals("averageAnnualHouseholdIncome", ignoreCase = true) -> {
                        householdSurveyModel.averageAnnualHouseholdIncome = value1
                    }

                    name.equals("monthlyFoodExpenditure", ignoreCase = true) -> {
                        householdSurveyModel.monthlyFoodExpenditure = value1
                    }

                    name.equals("annualHealthExpenditure", ignoreCase = true) -> {
                        householdSurveyModel.annualHealthExpenditure = value1
                    }

                    name.equals("annualEducationExpenditure", ignoreCase = true) -> {
                        householdSurveyModel.annualEducationExpenditure = value1
                    }

                    name.equals("annualClothingExpenditure", ignoreCase = true) -> {
                        householdSurveyModel.annualClothingExpenditure = value1
                    }

                    name.equals("monthlyIntoxicantsExpenditure", ignoreCase = true) -> {
                        householdSurveyModel.monthlyIntoxicantsExpenditure = value1
                    }

                    name.equals("householdBPLCardStatus", ignoreCase = true) -> {
                        householdSurveyModel.householdBPLCardStatus = value1
                    }

                    name.equals("householdAntodayaCardStatus", ignoreCase = true) -> {
                        householdSurveyModel.householdAntodayaCardStatus = value1
                    }

                    name.equals("householdRSBYCardStatus", ignoreCase = true) -> {
                        householdSurveyModel.householdRSBYCardStatus = value1
                    }

                    name.equals("householdMGNREGACardStatus", ignoreCase = true) -> {
                        householdSurveyModel.householdMGNREGACardStatus = value1
                    }
                    //fifth fragment
                    name.equals("cookingFuelType", ignoreCase = true) -> {
                        householdSurveyModel.cookingFuelType = value1
                    }

                    name.equals("mainLightingSource", ignoreCase = true) -> {
                        householdSurveyModel.mainLightingSource = value1
                    }

                    name.equals("mainDrinkingWaterSource", ignoreCase = true) -> {
                        householdSurveyModel.mainDrinkingWaterSource = value1
                    }

                    name.equals("saferWaterProcess", ignoreCase = true) -> {
                        householdSurveyModel.saferWaterProcess = value1
                    }

                    name.equals("householdToiletFacility", ignoreCase = true) -> {
                        householdSurveyModel.householdToiletFacility = value1
                    }
                    //sixth fragment
                    name.equals("householdOpenDefecationStatus", ignoreCase = true) -> {
                        householdSurveyModel.householdOpenDefecationStatus = value1
                    }

                    name.equals("foodItemsPreparedInTwentyFourHrs", ignoreCase = true) -> {
                        householdSurveyModel.foodItemsPreparedInTwentyFourHrs = value1
                    }
                    //seventh fragment
                    name.equals("subCentreDistance", ignoreCase = true) -> {
                        householdSurveyModel.subCentreDistance = value1
                    }

                    name.equals("nearestPrimaryHealthCenterDistance", ignoreCase = true) -> {
                        householdSurveyModel.nearestPrimaryHealthCenterDistance = value1
                    }

                    name.equals("nearestCommunityHealthCenterDistance", ignoreCase = true) -> {
                        householdSurveyModel.nearestCommunityHealthCenterDistance = value1
                    }

                    name.equals("nearestDistrictHospitalDistance", ignoreCase = true) -> {
                        householdSurveyModel.nearestDistrictHospitalDistance = value1
                    }

                    name.equals("nearestPathologicalLabDistance", ignoreCase = true) -> {
                        householdSurveyModel.nearestPathologicalLabDistance = value1
                    }

                    name.equals("nearestPrivateClinicMBBSDoctor", ignoreCase = true) -> {
                        householdSurveyModel.nearestPrivateClinicMBBSDoctor = value1
                    }

                    name.equals("nearestPrivateClinicAlternateMedicine", ignoreCase = true) -> {
                        householdSurveyModel.nearestPrivateClinicAlternateMedicine =
                            value1
                    }

                    name.equals("nearestTertiaryCareFacility", ignoreCase = true) -> {
                        householdSurveyModel.nearestTertiaryCareFacility = value1
                    }

                }
            } while (idCursor1.moveToNext())
        }
        idCursor1.close()
        return householdSurveyModel
    }

    private fun createPatientAttributes(
        fragmentIdentifier: String,
        patient: PatientDTO,
        householdSurveyModel: HouseholdSurveyModel,
        patientUuid: String
    ) = arrayListOf<PatientAttributesDTO>().apply {
        if (fragmentIdentifier.isNotEmpty() && fragmentIdentifier.equals(
                "firstScreen",
                ignoreCase = true
            )
        ) {
            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.REPORT_DATE_OF_SURVEY_STARTED.value,
                householdSurveyModel.reportDateOfSurveyStarted
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.HOUSE_STRUCTURE.value,
                householdSurveyModel.houseStructure
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.RESULT_OF_VISIT.value,
                householdSurveyModel.resultOfVisit
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.NAME_OF_PRIMARY_RESPONDENT.value,
                householdSurveyModel.namePrimaryRespondent
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.HOUSEHOLD_NUMBER_OF_SURVEY.value,
                householdSurveyModel.householdNumberOfSurvey
            )?.let { add(it) }
        }

        if (fragmentIdentifier.isNotEmpty() && fragmentIdentifier.equals(
                "secondScreen",
                ignoreCase = true
            )
        ) {
            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.HOUSEHOLD_HEAD_NAME.value,
                householdSurveyModel.headOfHouseholdName
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.HOUSEHOLD_HEAD_RELIGION.value,
                householdSurveyModel.religion
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.HOUSEHOLD_HEAD_CAST.value,
                householdSurveyModel.caste
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.NUMBER_OF_SMARTPHONES.value,
                householdSurveyModel.numberOfSmartPhones
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.NUMBER_OF_FEATURE_PHONES.value,
                householdSurveyModel.numberOfFeaturePhones
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.NUMBER_OF_EARNING_MEMBERS.value,
                householdSurveyModel.numberOfEarningMembers
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.PRIMARY_SOURCE_OF_INCOME.value,
                householdSurveyModel.primarySourceOfIncome
            )?.let { add(it) }
        }

        if (fragmentIdentifier.isNotEmpty() && fragmentIdentifier.equals(
                "thirdScreen",
                ignoreCase = true
            )
        ) {
            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.HOUSEHOLD_ELECTRICITY_STATUS.value,
                householdSurveyModel.householdElectricityStatus
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.NO_OF_LOAD_SHEDDING_HRS_PER_DAY.value,
                householdSurveyModel.noOfHoursOfLoadSheddingPerDay
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.NO_OF_LOAD_SHEDDING_HRS_PER_WEEK.value,
                householdSurveyModel.noOfDaysOfLoadSheddingPerWeek
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.PRIMARY_SOURCE_OF_RUNNING_WATER.value,
                householdSurveyModel.primarySourceOfRunningWater
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.RUNNING_WATER_STATUS.value,
                householdSurveyModel.runningWaterStatus
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.WATER_SOURCE_DISTANCE.value,
                householdSurveyModel.waterSourceDistance
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.WATER_SUPPLY_AVAILABILITY_HRS_PER_DAY.value,
                householdSurveyModel.waterSupplyAvailabilityHrsPerDay
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.WATER_AVAILABILITY_DAYS_PER_WEEK.value,
                householdSurveyModel.waterSupplyAvailabilityDaysperWeek
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.HOUSEHOLD_BANK_ACCOUNT_STATUS.value,
                householdSurveyModel.householdBankAccountStatus
            )?.let { add(it) }
        }

        if (fragmentIdentifier.isNotEmpty() && fragmentIdentifier.equals(
                "fourthScreen",
                ignoreCase = true
            )
        ) {
            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.HOUSEHOLD_CULTIVABLE_LAND.value,
                householdSurveyModel.householdCultivableLand
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.AVERAGE_ANNUAL_HOUSEHOLD_INCOME.value,
                householdSurveyModel.averageAnnualHouseholdIncome
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.ANNUAL_HEALTH_EXPENDITURE.value,
                householdSurveyModel.annualHealthExpenditure
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.MONTHLY_FOOD_EXPENDITURE.value,
                householdSurveyModel.monthlyFoodExpenditure
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.ANNUAL_EDUCATION_EXPENDITURE.value,
                householdSurveyModel.annualEducationExpenditure
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.ANNUAL_CLOTHING_EXPENDITURE.value,
                householdSurveyModel.annualClothingExpenditure
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.MONTHLY_INTOXICANTS_EXPENDITURE.value,
                householdSurveyModel.monthlyIntoxicantsExpenditure
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.HOUSEHOLD_BPL_CARD_STATUS.value,
                householdSurveyModel.householdBPLCardStatus
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.HOUSEHOLD_ANTODAYA_CARD_STATUS.value,
                householdSurveyModel.householdAntodayaCardStatus
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.HOUSEHOLD_RSBY_CARD_STATUS.value,
                householdSurveyModel.householdRSBYCardStatus
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.HOUSEHOLD_MGNREGA_CARD_STATUS.value,
                householdSurveyModel.householdMGNREGACardStatus
            )?.let { add(it) }
        }

        if (fragmentIdentifier.isNotEmpty() && fragmentIdentifier.equals(
                "fifthScreen",
                ignoreCase = true
            )
        ) {
            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.COOKING_FUEL_TYPE.value,
                householdSurveyModel.cookingFuelType
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.MAIN_LIGHTING_SOURCE.value,
                householdSurveyModel.mainLightingSource
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.MAIN_DRINKING_WATER_SOURCE.value,
                householdSurveyModel.mainDrinkingWaterSource
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.SAFER_WATER_PROCESS.value,
                householdSurveyModel.saferWaterProcess
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.HOUSEHOLD_TOILET_FACILITY.value,
                householdSurveyModel.householdToiletFacility
            )?.let { add(it) }
        }

        if (fragmentIdentifier.isNotEmpty() && fragmentIdentifier.equals(
                "sixthScreen",
                ignoreCase = true
            )
        ) {
            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.HOUSEHOLD_OPEN_DEFECATION_STATUS.value,
                householdSurveyModel.householdOpenDefecationStatus
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.FOOD_ITEMS_PREPARED_IN_TWENTY_FOUR_HRS.value,
                householdSurveyModel.foodItemsPreparedInTwentyFourHrs
            )?.let { add(it) }
        }
        if (fragmentIdentifier.isNotEmpty() && fragmentIdentifier.equals(
                "seventhScreen",
                ignoreCase = true
            )
        ) {
            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.SUB_CENTRE_DISTANCE.value,
                householdSurveyModel.subCentreDistance
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.NEAREST_PRIMARY_HEALTH_CENTER_DISTANCE.value,
                householdSurveyModel.nearestPrimaryHealthCenterDistance
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.NEAREST_COMMUNITY_HEALTH_CENTER_DISTANCE.value,
                householdSurveyModel.nearestCommunityHealthCenterDistance
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.NEAREST_DISTRICT_HOSPITAL_DISTANCE.value,
                householdSurveyModel.nearestDistrictHospitalDistance
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.NEAREST_PATHOLOGICAL_LAB_DISTANCE.value,
                householdSurveyModel.nearestPathologicalLabDistance
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.NEAREST_PRIVATE_CLINIC_MBBS_DOCTOR.value,
                householdSurveyModel.nearestPrivateClinicMBBSDoctor
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.NEAREST_PRIVATE_CLINIC_ALTERNATE_MEDICINE.value,
                householdSurveyModel.nearestPrivateClinicAlternateMedicine
            )?.let { add(it) }

            createPatientAttribute(
                patientUuid,
                PatientAttributesDTO.Column.NEAREST_TERTIARY_CARE_FACILITY.value,
                householdSurveyModel.nearestTertiaryCareFacility
            )?.let { add(it) }
        }

    }

    private fun updatePatientAttribute(
        patientId: String,
        attrName: String,
        value: String?
    ) = PatientAttributesDTO().apply {
        uuid = UUID.randomUUID().toString()
        patientuuid = patientId
        personAttributeTypeUuid = patientsDao.getUuidForAttribute(attrName)
        this.value = value
    }
    fun getPatientUuidsForHouseholdValue(patientUuid: String): List<String> {
        val patientsDao = PatientsDAO()
        var houseHoldValue: String = ""
        try {
            houseHoldValue = patientsDao.getHouseHoldValue(patientUuid)
        } catch (e: DAOException) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }

        return if (houseHoldValue.isNotEmpty()) {
            // Fetch all patient UUIDs from houseHoldValue
            try {
                patientsDao.getPatientUUIDs(houseHoldValue)?.toList() ?: emptyList()
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                emptyList()
            }
        } else {
            emptyList()
        }
    }
}