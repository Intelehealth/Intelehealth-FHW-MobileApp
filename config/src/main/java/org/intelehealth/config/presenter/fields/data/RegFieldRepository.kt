package org.intelehealth.config.presenter.fields.data

import org.intelehealth.config.room.dao.PatientRegFieldDao
import org.intelehealth.config.utility.FieldGroup

/**
 * Created by Vaghela Mithun R. on 10-04-2024 - 18:11.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
open class RegFieldRepository(private val regFieldDao: PatientRegFieldDao) {
    fun getGroupFields(group: FieldGroup) = regFieldDao.getGroupLiveField(group.value)
    suspend fun getGroupFieldsSuspended(group: FieldGroup) = regFieldDao.getGroupField(group.value)

    suspend fun getAllRecord() = regFieldDao.getAllRecord()

    fun getAllEnabledGroupField(group: FieldGroup) =
        regFieldDao.getAllEnabledLiveGroupFields(group.value)

    fun getAllEnabledLiveFields() = regFieldDao.getAllEnabledLiveFields()

    fun getAllMandatoryLiveFields() = regFieldDao.getAllMandatoryLiveFields()

    fun getAllEditableLiveFields() = regFieldDao.getAllEditableLiveFields()

    fun getLiveRecord(name: String) = regFieldDao.getLiveRecord(name)
}