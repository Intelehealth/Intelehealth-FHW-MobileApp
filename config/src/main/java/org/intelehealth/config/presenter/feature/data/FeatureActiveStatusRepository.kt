package org.intelehealth.config.presenter.feature.data

import org.intelehealth.config.room.dao.FeatureActiveStatusDao

/**
 * Created by Vaghela Mithun R. on 10-04-2024 - 18:11.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class FeatureActiveStatusRepository(private val featureActiveStatusDao: FeatureActiveStatusDao) {
    fun getFeaturesActiveStatus() = featureActiveStatusDao.getFeatureActiveStatusLiveRecord()

    suspend fun getFeaturesActiveStatusSuspended() = featureActiveStatusDao.getFeatureActiveStatus()

    suspend fun getAllRecord() = featureActiveStatusDao.getAllRecord()

    suspend fun getRecord() = featureActiveStatusDao.getRecord()
}