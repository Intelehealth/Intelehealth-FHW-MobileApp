package org.intelehealth.app.ui.householdSurvey.fragments

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import androidx.navigation.fragment.findNavController
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import org.intelehealth.app.R
import org.intelehealth.app.ui.householdSurvey.models.HouseholdSurveyModel
import org.intelehealth.app.databinding.FragmentSixthHouseholdSurveyBinding
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.ui.patient.fragment.PatientAddressInfoFragmentDirections
import org.intelehealth.app.utilities.HouseholdSurveyStage
import org.intelehealth.app.utilities.PatientRegStage
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.StringUtils
import org.intelehealth.core.registry.PermissionRegistry
import java.util.Calendar
import java.util.Locale

class SixthFragment : BaseHouseholdSurveyFragment(R.layout.fragment_sixth_household_survey) {

    private lateinit var binding: FragmentSixthHouseholdSurveyBinding
    var selectedDate = Calendar.getInstance().timeInMillis
    private var patientUuid: String? = null
    private lateinit var updatedContext: Context

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSixthHouseholdSurveyBinding.bind(view)
        houseHoldViewModel.updatePatientStage(HouseholdSurveyStage.SIXTH_SCREEN)
        initViews()
        setClickListener()
    }

    private fun setClickListener() {
        binding.frag2BtnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.frag2BtnNext.setOnClickListener {
            savePatient()

        }
    }

    private fun initViews() {
        val intent = requireActivity().intent
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid")
        }
    }

    private fun savePatient() {
        householdSurveyModel.apply {
            setupFamilyMemberDefeated()
            setupForKindOfFoodPrepared()

            Log.d("devchdbsave6", "savePatient: householdSurveyModel : " + householdSurveyModel)
            houseHoldViewModel.updatedPatient(this)
            val patient = PatientDTO()
            patient.uuid = patientUuid
            saveAndNavigateToDetails(patient, householdSurveyModel)
        }
    }

    private fun saveAndNavigateToDetails(
        patient: PatientDTO,
        householdSurveyModel: HouseholdSurveyModel
    ) {
        houseHoldViewModel.savePatient(
            "sixthScreen", patient,
            householdSurveyModel
        ).observe(viewLifecycleOwner) {
            it ?: return@observe
            houseHoldViewModel.handleResponse(it) { result -> if (result) navigateToDetails() }
        }
    }

    private fun navigateToDetails() {
        SixthFragmentDirections.actionSixToSeven().apply {
            findNavController().navigate(this)
        }
    }

    private fun setupFamilyMemberDefeated() {
        var defeation = "-"
        binding.defecationInOpenRadioGroup.checkedRadioButtonId.takeIf { it != -1 }?.let {
            defeation = StringUtils.getPreTerm(
                (binding.defecationInOpenRadioGroup.findViewById<RadioButton>(it)).text.toString(),
                SessionManager(requireActivity()).appLanguage
            )
        }
        householdSurveyModel.householdOpenDefecationStatus = defeation
    }

    private fun setupForKindOfFoodPrepared() {
        householdSurveyModel.foodItemsPreparedInTwentyFourHrs = StringUtils.getSelectedCheckboxes(
            binding.foodPreparedInThePastTwentyFourHoursLinearLayout,
            SessionManager(requireActivity()).appLanguage,
            context,
            ""
        )
    }

    private fun setDataForKindOfFoodPrepared() {
        val value = householdSurveyModel.foodItemsPreparedInTwentyFourHrs
        if (!value.isNullOrEmpty()) {
            if (value.contains(requireContext().getString(R.string.starch_staple_food)))
                binding.starchStapleFoodCheckbox.isChecked = true

            if (value.contains(requireContext().getString(R.string.beans_and_peas)))
                binding.beansAndPeasCheckbox.isChecked = true

            if (value.contains(requireContext().getString(R.string.nuts_and_seeds)))
                binding.nutsAndSeedsCheckbox.isChecked = true

            if (value.contains(requireContext().getString(R.string.dairy)))
                binding.dairyCheckbox.isChecked = true

            if (value.contains(requireContext().getString(R.string.eggs)))
                binding.eggsCheckbox.isChecked = true

            if (value.contains(requireContext().getString(R.string.flesh_food)))
                binding.fleshFoodCheckbox.isChecked = true

            if (value.contains(requireContext().getString(R.string.any_vegetables)))
                binding.anyVegetablesCheckbox.isChecked = true
        }
    }
    private fun setDataForFamilyMemberDefeated() {
        val value1 = householdSurveyModel.householdOpenDefecationStatus
        when {
            value1 != null && value1.equals(
                updatedContext.getString(R.string.yes),
                ignoreCase = true
            ) ->
                binding.defecationYesRadioButton.isChecked = true

            value1 != null && value1.equals(
                updatedContext.getString(R.string.no),
                ignoreCase = true
            ) ->
                binding.defecationNoRadioButton.isChecked = true
        }


    }

   /* private fun setDataForFamilyMemberDefeated() {
        binding.defecationInOpenRadioGroup.checkedRadioButtonId.let { checkedId ->
            householdSurveyModel.householdOpenDefecationStatus = if (checkedId != -1) {
                if (checkedId == binding.defecationYesRadioButton.id) {
                    StringUtils.getPreTerm(
                        binding.defecationYesRadioButton.text.toString(),
                        SessionManager(requireActivity()).appLanguage
                    )
                } else {
                    StringUtils.getPreTerm(
                        binding.defecationNoRadioButton.text.toString(),
                        SessionManager(requireActivity()).appLanguage
                    )
                }
            } else {
                "-"
            }
        }
    }*/

    override fun onPatientDataLoaded(householdSurveyModel: HouseholdSurveyModel) {
        super.onPatientDataLoaded(householdSurveyModel)
        Timber.d { Gson().toJson(householdSurveyModel) }
        setDataToUI()

        binding.patientSurveyAttributes = householdSurveyModel
        binding.isEditMode = houseHoldViewModel.isEditMode
    }

    private fun setDataToUI() {
        updatedContext =
            if (SessionManager(requireActivity()).appLanguage.equals("mr", ignoreCase = true)) {
                val configuration = Configuration(requireContext().resources.configuration)
                configuration.setLocale(Locale("en"))
                requireContext().createConfigurationContext(configuration)
            } else {
                requireContext()
            }
        setDataForFamilyMemberDefeated()
        setDataForKindOfFoodPrepared()
    }
}