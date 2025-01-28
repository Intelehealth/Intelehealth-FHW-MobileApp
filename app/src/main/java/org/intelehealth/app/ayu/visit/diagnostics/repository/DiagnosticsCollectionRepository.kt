package org.intelehealth.app.ayu.visit.diagnostics.repository

import org.intelehealth.app.database.InteleHealthDatabaseHelper
import org.intelehealth.app.database.dao.ObsDAO
import org.intelehealth.app.models.DiagnosticsModel
import org.intelehealth.app.models.dto.ObsDTO
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.UuidDictionary

class DiagnosticsCollectionRepository(
    private val databaseHelper: InteleHealthDatabaseHelper,
    private val sessionManager: SessionManager
) {

    fun getResultsFromDatabase(encounterUuid: String): ObsDTO {
        val db = databaseHelper.writableDatabase
        val columns = arrayOf("value", "conceptuuid")
        val selection = "encounteruuid = ? AND voided != '1'"
        val selectionArgs = arrayOf(encounterUuid)
        val cursor = db.query("tbl_obs", columns, selection, selectionArgs, null, null, null)
        val results = ObsDTO()

        if (cursor.moveToFirst()) {
            do {
                val conceptId = cursor.getString(cursor.getColumnIndex("conceptuuid"))
                val value = cursor.getString(cursor.getColumnIndex("value"))
                parseData(conceptId, value, results)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return results
    }


    fun updateResults(obsDTO: ObsDTO) {
        val obsDao = ObsDAO()
        obsDao.updateObs(obsDTO)
        updateEncounterSync(databaseHelper)
    }

    fun insertResults(obsDTO: ObsDTO) {
        val obsDao = ObsDAO()
        obsDao.insertObs(obsDTO)
        updateEncounterSync(databaseHelper)
    }

    private fun updateEncounterSync(databaseHelper: InteleHealthDatabaseHelper) {
        // Implement the logic for updating encounter sync
        // This method can also be a part of the companion object if it doesn't rely on instance-specific data
    }

    private fun parseData(conceptId: String, value: String?, results: ObsDTO) {
        when (conceptId) {
            UuidDictionary.BLOOD_GLUCOSE_RANDOM -> results.value = value
            UuidDictionary.BLOOD_GLUCOSE_FASTING -> results.value = value
            UuidDictionary.BLOOD_GLUCOSE -> results.value = value
            UuidDictionary.BLOOD_GLUCOSE_POST_PRANDIAL -> results.value = value
            UuidDictionary.HEMOGLOBIN -> results.value = value
            UuidDictionary.URIC_ACID -> results.value = value
            UuidDictionary.TOTAL_CHOLESTEROL -> results.value = value
        }
    }
}
