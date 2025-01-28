package org.intelehealth.app.ui.draftsurvey.repository

import android.database.sqlite.SQLiteDatabase
import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.ui.draftsurvey.utils.HouseholdSurveyAttributes

class DraftSurveyRepository (private val database: SQLiteDatabase, private val patientsDAO: PatientsDAO) {

    fun fetchPatientData(): List<PatientDTO> {
        val patientUUIDList = fetchUniquePatientUuidFromAttributes()
        val patientDTOList = mutableListOf<PatientDTO>()
        database.beginTransaction()
        try {
            for (uuid in patientUUIDList) {
                val patientData = fetchValueAttrFromPatAttrTbl(uuid)

                if (patientData.isNotEmpty()) {
                    patientDTOList.addAll(patientData)
                }
            }
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
        return patientDTOList
    }

    private fun fetchUniquePatientUuidFromAttributes(): List<String> {
        val patientUUIDs = mutableSetOf<String>()
        val query = "SELECT DISTINCT(patientuuid) FROM tbl_patient_attribute"
        val cursor = database.rawQuery(query, null)

        cursor.use {
            while (it.moveToNext()) {
                patientUUIDs.add(it.getString(it.getColumnIndexOrThrow("patientuuid")))
            }
        }
        return patientUUIDs.toList()
    }

    private fun fetchValueAttrFromPatAttrTbl(patientUUID: String): List<PatientDTO> {
        val patientDTOSet = mutableSetOf<PatientDTO>()
        val query = """
        SELECT * FROM tbl_patient_attribute AS c
        WHERE c.patientuuid = ? 
          AND c.modified_date = (
            SELECT MAX(d.modified_date) 
            FROM tbl_patient_attribute AS d 
            WHERE d.person_attribute_type_uuid = c.person_attribute_type_uuid)
        GROUP BY c.person_attribute_type_uuid
    """

        val cursor = database.rawQuery(query, arrayOf(patientUUID))

        cursor.use {
            var conditionMet = false
            while (it.moveToNext() && !conditionMet) {
                val attributeType = it.getString(it.getColumnIndexOrThrow("person_attribute_type_uuid"))
                val attributeValue = it.getString(it.getColumnIndexOrThrow("value"))
                val name = patientsDAO.getAttributesName(attributeType)

                if (name != null && attributeValue  != null && isDraftConditionMet(name, attributeValue)) {
                    val patientDTO = getPatientData(patientUUID)
                    patientDTO?.let { dto ->
                        patientDTOSet.add(dto)
                        conditionMet = true // Set flag to true to stop further execution for this patientUUID
                    }
                }
            }
        }

        return patientDTOSet.toList()
    }

    private fun isDraftConditionMet(name: String, value: String): Boolean {
        val invalidValues = listOf("-", "[]", "Select")
        return name in HouseholdSurveyAttributes.getAllKeys() && value !in invalidValues
    }



    private fun getPatientData(patientUUID: String): PatientDTO? {
        val selection = "uuid=?"
        val args = arrayOf(patientUUID)
        val cursor = database.query("tbl_patient", null, selection, args, null, null, null)
        val patientDTO= PatientDTO()
            if (cursor.moveToFirst()) {
                patientDTO.uuid = patientUUID
                patientDTO.firstname = cursor.getString(cursor.getColumnIndexOrThrow("first_name"))
                patientDTO.middlename = cursor.getString(cursor.getColumnIndexOrThrow("middle_name"))
                patientDTO.lastname = cursor.getString(cursor.getColumnIndexOrThrow("last_name"))
                patientDTO.openmrsId = cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id"))
                patientDTO.dateofbirth = cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth"))
                patientDTO.gender = cursor.getString(cursor.getColumnIndexOrThrow("gender"))
                patientDTO.patientPhoto = cursor.getString(cursor.getColumnIndexOrThrow("patient_photo"))

            }

        return patientDTO
    }
}