package org.intelehealth.config.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.intelehealth.config.room.entity.ActiveLanguage
import org.intelehealth.config.room.entity.ConfigDictionary
import org.intelehealth.config.room.entity.FeatureActiveStatus
import org.intelehealth.config.room.entity.PatientVital

/**
 * Created by Vaghela Mithun R. on 15-03-2024 - 16:10.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@Dao
interface FeatureActiveStatusDao : CoreDao<FeatureActiveStatus> {

    @Query("SELECT * FROM tbl_feature_active_status")
    suspend fun getAllRecord(): List<FeatureActiveStatus>

    @Query("SELECT * FROM tbl_feature_active_status")
    suspend fun getRecord(): FeatureActiveStatus

    @Query("SELECT * FROM tbl_feature_active_status")
    fun getAllLiveRecord(): LiveData<List<FeatureActiveStatus>>

    @Query("SELECT * FROM tbl_feature_active_status")
    fun getFeatureActiveStatusLiveRecord(): LiveData<FeatureActiveStatus>

    @Query("SELECT * FROM tbl_feature_active_status")
    suspend fun getFeatureActiveStatus(): FeatureActiveStatus

}