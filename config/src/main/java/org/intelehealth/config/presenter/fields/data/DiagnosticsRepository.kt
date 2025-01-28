package org.intelehealth.config.presenter.fields.data

import org.intelehealth.config.room.dao.PatientDiagnosticsDao
import org.intelehealth.config.room.dao.PatientVitalDao

class DiagnosticsRepository(private val diagnosticsDao: PatientDiagnosticsDao) {

    fun getAllEnabledLiveFields() = diagnosticsDao.getAllEnabledLiveFields()
    suspend fun getAllEnabledFields() = diagnosticsDao.getAllEnabledFields()


}