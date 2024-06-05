package org.intelehealth.config.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.intelehealth.config.room.entity.ActiveLanguage
import org.intelehealth.config.room.entity.ConfigDictionary
import org.intelehealth.config.room.entity.PatientVital

/**
 * Created by Vaghela Mithun R. on 15-03-2024 - 16:10.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@Dao
interface PatientVitalDao : CoreDao<PatientVital> {

    @Query("SELECT * FROM tbl_patient_vital")
    suspend fun getAllRecord(): List<PatientVital>

    @Query("SELECT * FROM tbl_patient_vital where vitalKey = :key")
    suspend fun getRecord(key: String): PatientVital

    @Query("SELECT * FROM tbl_patient_vital")
    fun getAllLiveRecord(): LiveData<List<PatientVital>>

    @Query("SELECT * FROM tbl_patient_vital where vitalKey = :key")
    fun getLiveRecord(key: String): LiveData<PatientVital>

}