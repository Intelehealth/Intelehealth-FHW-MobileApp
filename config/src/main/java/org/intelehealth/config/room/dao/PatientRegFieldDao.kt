package org.intelehealth.config.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import org.intelehealth.config.room.entity.PatientRegistrationFields

/**
 * Created by Vaghela Mithun R. on 15-03-2024 - 16:10.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@Dao
interface PatientRegFieldDao : CoreDao<PatientRegistrationFields> {
    @Query("SELECT * FROM tbl_patient_registration_fields")
    suspend fun getAllRecord(): List<PatientRegistrationFields>

    @Query("SELECT * FROM tbl_patient_registration_fields where groupId = :groupId")
    fun getGroupLiveField(groupId: String): LiveData<List<PatientRegistrationFields>>

    @Query("SELECT * FROM tbl_patient_registration_fields where groupId = :groupId")
    suspend fun getGroupField(groupId: String): List<PatientRegistrationFields>

    @Query("SELECT * FROM tbl_patient_registration_fields WHERE isMandatory = 1")
    fun getAllMandatoryLiveFields(): LiveData<List<PatientRegistrationFields>>

    @Query("SELECT * FROM tbl_patient_registration_fields WHERE isEditable = 1")
    fun getAllEditableLiveFields(): LiveData<List<PatientRegistrationFields>>

    @Query("SELECT * FROM tbl_patient_registration_fields WHERE isEnabled = 1")
    fun getAllEnabledLiveFields(): LiveData<List<PatientRegistrationFields>>

    @Query("SELECT * FROM tbl_patient_registration_fields WHERE isEnabled = 1 AND groupId = :groupId")
    fun getAllEnabledLiveGroupFields(groupId: String): LiveData<List<PatientRegistrationFields>>

    @Query("SELECT * FROM tbl_patient_registration_fields WHERE name = :name")
    fun getLiveRecord(name: String): LiveData<PatientRegistrationFields>

    @Query("SELECT * FROM tbl_patient_registration_fields WHERE idKey = :idKey")
    fun getLiveKeyRecord(idKey: String): LiveData<PatientRegistrationFields>

}