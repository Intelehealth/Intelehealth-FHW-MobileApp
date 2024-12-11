package org.intelehealth.coreroomdb.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import org.intelehealth.coreroomdb.entity.Visit

@Dao
interface VisitDao : CoreDao<Visit> {

    @Query("SELECT * FROM tbl_visit WHERE synced = '0' OR synced = 'false'")
    fun getUnsyncedVisits(): LiveData<List<Visit>>

    @Query("UPDATE tbl_visit SET synced = :sync WHERE uuid = :visitUuid")
    fun updateVisitSync(visitUuid: String, sync: String)

    @Query("UPDATE tbl_visit SET endDate = :endDate WHERE uuid = :visitUuid")
    fun updateVisitEndDate(visitUuid: String, endDate: String)

    @Query("SELECT patientUuid FROM tbl_visit WHERE uuid = :visitUuid")
    fun getPatientUuidByVisit(visitUuid: String): LiveData<String>

    @Query("SELECT endDate FROM tbl_visit WHERE uuid = :visitUuid")
    fun getVisitEndDate(visitUuid: String): LiveData<String>

    @Query("SELECT uuid FROM tbl_visit WHERE patientUuid = :patientUuid")
    fun getVisitUuidByPatientUuid(patientUuid: String): LiveData<String>

}