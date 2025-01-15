package org.intelehealth.config.presenter.feature.viewmodel

import org.intelehealth.config.presenter.feature.data.FeatureActiveStatusRepository
import org.intelehealth.core.shared.ui.viewmodel.BaseViewModel

/**
 * Created by Vaghela Mithun R. on 12-04-2024 - 12:56.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class FeatureActiveStatusViewModel(private val repository: FeatureActiveStatusRepository) : BaseViewModel() {
    fun fetchFeaturesActiveStatus() = repository.getFeaturesActiveStatus()

    suspend fun fetchFeaturesActiveStatusSuspended() = repository.getFeaturesActiveStatusSuspended()
}