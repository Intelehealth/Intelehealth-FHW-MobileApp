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
class HouseholdRepositoryOld (private val patientsDao: PatientsDAO,
    private val sqlHelper: SQLiteOpenHelper,
    ) {
        fun addHouseholdPatientAttributes(
            fragmentIdentifier: String,
            patient: PatientDTO,
            householdSurveyModel: org.intelehealth.app.ui.householdSurvey.models.HouseholdSurveyModel
        ): Boolean {
            Log.d("devKZchk", "addHouseholdPatientAttributes: patient uuid : " + patient.uuid)
            bindPatientAttributes(fragmentIdentifier, patient, householdSurveyModel).let {
                val flag = patientsDao.updatePatientSurveyInDb(it.uuid, it.patientAttributesDTOList)
                syncOnServer()
                return flag
            }
        }

        fun updateHouseholdPatientAttributes(
            fragmentIdentifier: String,
            patient: PatientDTO,
            householdSurveyModel: org.intelehealth.app.ui.householdSurvey.models.HouseholdSurveyModel
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
            householdSurveyModel: org.intelehealth.app.ui.householdSurvey.models.HouseholdSurveyModel
        ) = patient.apply {
            patientAttributesDTOList =
                createPatientAttributes(fragmentIdentifier, patient, householdSurveyModel, patient.uuid)
            syncd = false
        }

        fun fetchPatient(uuid: String): org.intelehealth.app.ui.householdSurvey.models.HouseholdSurveyModel {
            Timber.d { "uuid => $uuid" }
            val patientsDao = PatientsDAO()
            var householdSurveyModel =
                org.intelehealth.app.ui.householdSurvey.models.HouseholdSurveyModel()
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
                    Log.e("patientUUIDss", "$patientUUIDs")
                    for (uuid in patientUUIDs) {
                        householdSurveyModel = getAllRecords(uuid)
                        Log.d(
                            "kzdevchk",
                            "fetchPatient: householdSurveyModel result : " + Gson().toJson(
                                householdSurveyModel
                            )
                        )
                        /* PatientQueryBuilder().buildPatientSurveyAttributesDetailsQueryNew(uuid).apply {
                             Timber.d { "Query => $this" }
                             val cursor = sqlHelper.readableDatabase.rawQuery(this, null)
                             householdSurveyModel =
                                 patientsDao.retrievePatientHouseholdSurveyAttributes(cursor)
                             return householdSurveyModel
                         }*/
                    }
                } catch (e: Exception) {
                    Log.d("kzdevchk", "fetchPatient: " + e.localizedMessage)
                    e.printStackTrace()
                    // Handle exception if needed
                }
            }

            return householdSurveyModel
        }


        private fun createPatientAttribute(
            patientId: String,
            attrName: String,
            value: String?
        ) = PatientAttributesDTO().apply {
            uuid = UUID.randomUUID().toString()
            patientuuid = patientId
            personAttributeTypeUuid = patientsDao.getUuidForAttribute(attrName)
            this.value = value
            Log.d("devchk", "createPatientAttribute: personAttributeTypeUuid : "+personAttributeTypeUuid)
            Log.d("devchk", "createPatientAttribute: value : "+value)

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

        fun syncOnServer() {
            if (NetworkConnection.isOnline(IntelehealthApplication.getAppContext())) {
                val syncDAO = SyncDAO()
                val imagesPushDAO = ImagesPushDAO()
                syncDAO.pushDataApi()
                imagesPushDAO.patientProfileImagesPush()
            }
        }

        private fun getAllRecords(patientUuid: String): org.intelehealth.app.ui.householdSurvey.models.HouseholdSurveyModel {
            val db = IntelehealthApplication.inteleHealthDatabaseHelper.writeDb
            val patientsDao = PatientsDAO()
            var householdSurveyModel =
                org.intelehealth.app.ui.householdSurvey.models.HouseholdSurveyModel()
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
            Log.d("devkzchk", "setData: cursor count : ${idCursor1.count}")
            var name = ""

            if (idCursor1.moveToFirst()) {
                do {
                    try {
                        name = patientsDao.getAttributesName(
                            idCursor1.getString(
                                idCursor1.getColumnIndexOrThrow("person_attribute_type_uuid")
                            )
                        )
                    } catch (e: DAOException) {
                        FirebaseCrashlytics.getInstance().recordException(e)
                    }

                    val value12 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))
                    Log.d("devkzchk", "setData: value from attributes  : $value12")

                    when {
                        name.equals("NamePrimaryRespondent", ignoreCase = true) -> {
                            val value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))
                            if (!value1.isNullOrEmpty() && value1 != "-") {
                                householdSurveyModel.namePrimaryRespondent = value1
                            }
                        }

                        name.equals("HouseholdNumber", ignoreCase = true) -> {
                            val value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))
                            if (!value1.isNullOrEmpty() && value1 != "-") {
                                householdSurveyModel.householdNumberOfSurvey = value1
                            }
                        }

                        name.equals("HouseStructure", ignoreCase = true) -> {
                            val value1 = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))
                            householdSurveyModel.houseStructure = value1
                        }

                        name.equals("ResultOfVisit", ignoreCase = true) -> {
                            val result = idCursor1.getString(idCursor1.getColumnIndexOrThrow("value"))
                            householdSurveyModel.resultOfVisit = result
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
            householdSurveyModel: org.intelehealth.app.ui.householdSurvey.models.HouseholdSurveyModel,
            patientUuid: String
        ): List<PatientAttributesDTO> {

            // Get the fields (parameter keys) based on the fragment identifier
            val fieldsForFragment = org.intelehealth.app.ui.householdSurvey.repository.HouseholdSurveyFragmentMap.getFieldsForFragment(fragmentIdentifier)

            // Create a list to hold the patient attributes
            val attributesList = arrayListOf<PatientAttributesDTO>()

            // Dynamically add attributes based on the fields for this fragment
            fieldsForFragment.forEach { field ->
                Log.d("devchk", "createPatientAttributes: field : "+field)
                val attributeValue = when (field) {
                    //First screen
                    PatientAttributesDTO.Column.REPORT_DATE_OF_SURVEY_STARTED.value -> householdSurveyModel.reportDateOfSurveyStarted
                    PatientAttributesDTO.Column.HOUSE_STRUCTURE.value -> householdSurveyModel.houseStructure
                    PatientAttributesDTO.Column.RESULT_OF_VISIT.value -> householdSurveyModel.resultOfVisit
                    PatientAttributesDTO.Column.NAME_OF_PRIMARY_RESPONDENT.value -> householdSurveyModel.namePrimaryRespondent
                    PatientAttributesDTO.Column.HOUSEHOLD_NUMBER_OF_SURVEY.value -> householdSurveyModel.householdNumberOfSurvey
                    PatientAttributesDTO.Column.NUMBER_OF_SMARTPHONES.value -> householdSurveyModel.numberOfSmartPhones

                    //Second screen
                    PatientAttributesDTO.Column.NUMBER_OF_FEATURE_PHONES.value -> householdSurveyModel.numberOfFeaturePhones

                    // PatientAttributesDTO.Column.PRIMARY_SOURCE_OF_INCOME.value -> householdSurveyModel.primarySourceOfIncome

                    // Add more fields here as necessary
                    else -> {
                        Log.d("devchk", "createPatientAttributes: Unknown field: : "+field)
                        null
                    }
                }


                // Only add non-null attributes to the list
                //if (attributeValue != null) {
                attributesList.add(
                    createPatientAttribute(
                        patientUuid,
                        field,
                        attributeValue
                    )
                )
                // }
            }
            Log.d("devchk", "createPatientAttributes: attributesList : "+Gson().toJson(attributesList))
            return attributesList
        }

        /*
            private fun createPatientAttributes(
                fragmentIdentifier: String,
                patient: PatientDTO,
                householdSurveyModel: HouseholdSurveyModel,
                patientUuid: String
            ) = arrayListOf<PatientAttributesDTO>()
                .apply {
                    add(
                        createPatientAttribute(
                            patientUuid,
                            PatientAttributesDTO.Column.REPORT_DATE_OF_SURVEY_STARTED.value,
                            householdSurveyModel.reportDateOfSurveyStarted
                        )
                    )
                    add(
                        createPatientAttribute(
                            patientUuid,
                            PatientAttributesDTO.Column.HOUSE_STRUCTURE.value,
                            householdSurveyModel.houseStructure
                        )
                    )
                    add(
                        createPatientAttribute(
                            patientUuid,
                            PatientAttributesDTO.Column.RESULT_OF_VISIT.value,
                            householdSurveyModel.resultOfVisit
                        )
                    )
                    add(
                        createPatientAttribute(
                            patientUuid,
                            PatientAttributesDTO.Column.NAME_OF_PRIMARY_RESPONDENT.value,
                            householdSurveyModel.namePrimaryRespondent
                        )
                    )
                    add(
                        createPatientAttribute(
                            patientUuid,
                            PatientAttributesDTO.Column.HOUSEHOLD_NUMBER_OF_SURVEY.value,
                            householdSurveyModel.householdNumberOfSurvey
                        )
                    )
                    add(
                        createPatientAttribute(
                            patientUuid,
                            PatientAttributesDTO.Column.NUMBER_OF_SMARTPHONES.value,
                            householdSurveyModel.numberOfSmartPhones
                        )
                    )
                }
        */

    }