package org.intelehealth.config.presenter.fields.viewmodel

import org.intelehealth.config.presenter.fields.data.RegFieldRepository
import org.intelehealth.config.utility.FieldGroup
import org.intelehealth.core.shared.ui.viewmodel.BaseViewModel

/**
 * Created by Vaghela Mithun R. on 12-04-2024 - 12:56.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
open class RegFieldViewModel(private val repository: RegFieldRepository) : BaseViewModel() {
    fun fetchEnabledPersonalRegFields() = repository.getAllEnabledGroupField(FieldGroup.PERSONAL)

    fun fetchEnabledAddressRegFields() = repository.getAllEnabledGroupField(FieldGroup.ADDRESS)

    fun fetchEnabledOtherRegFields() = repository.getAllEnabledGroupField(FieldGroup.OTHER)

    fun fetchEnabledAllRegFields() = repository.getAllEnabledLiveFields()

    fun fetchPersonalRegFields() = repository.getGroupFields(FieldGroup.PERSONAL)

    fun fetchAddressRegFields() = repository.getGroupFields(FieldGroup.ADDRESS)

    fun fetchOtherRegFields() = repository.getGroupFields(FieldGroup.OTHER)

    suspend fun fetchPersonalRegFieldsSuspended() = repository.getGroupFieldsSuspended(FieldGroup.PERSONAL)

    suspend fun fetchAddressRegFieldsSuspended() = repository.getGroupFieldsSuspended(FieldGroup.ADDRESS)

    suspend fun fetchOtherRegFieldsSuspended() = repository.getGroupFieldsSuspended(FieldGroup.OTHER)
}