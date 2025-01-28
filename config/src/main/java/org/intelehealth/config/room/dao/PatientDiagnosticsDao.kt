package org.intelehealth.config.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import org.intelehealth.config.room.entity.Diagnostics

@Dao
interface PatientDiagnosticsDao : CoreDao<Diagnostics> {
    //@Query("SELECT * FROM tbl_patient_diagnostics WHERE isEnabled = 1")
    @Query("SELECT * FROM tbl_patient_diagnostics")
    fun getAllEnabledLiveFields(): LiveData<List<Diagnostics>>

    @Query("SELECT * FROM tbl_patient_diagnostics")
    suspend fun getAllEnabledFields(): List<Diagnostics>

}