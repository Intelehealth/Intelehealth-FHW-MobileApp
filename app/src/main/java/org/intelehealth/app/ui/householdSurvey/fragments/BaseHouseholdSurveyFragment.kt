package org.intelehealth.app.ui.householdSurvey.fragments

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import org.intelehealth.app.ui.householdSurvey.factory.HouseHoldViewModelFactory
import org.intelehealth.app.ui.householdSurvey.models.HouseholdSurveyModel

abstract class BaseHouseholdSurveyFragment (@LayoutRes layoutResId: Int) : Fragment(layoutResId) {
    protected var householdSurveyModel: org.intelehealth.app.ui.householdSurvey.models.HouseholdSurveyModel =
        org.intelehealth.app.ui.householdSurvey.models.HouseholdSurveyModel()
    protected val houseHoldViewModel by lazy {
        return@lazy HouseHoldViewModelFactory.create(requireActivity(), requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        houseHoldViewModel.patientSurveyAttributesData.observe(viewLifecycleOwner) {
            onPatientDataLoaded(it)
        }
    }

    open fun onPatientDataLoaded(householdSurveyModel: org.intelehealth.app.ui.householdSurvey.models.HouseholdSurveyModel) {
        this.householdSurveyModel = householdSurveyModel
    }
}