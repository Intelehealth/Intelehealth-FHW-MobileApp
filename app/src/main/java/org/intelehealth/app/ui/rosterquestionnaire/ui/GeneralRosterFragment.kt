package org.intelehealth.app.ui.rosterquestionnaire.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.intelehealth.app.R
import org.intelehealth.app.databinding.FragmentGeneralRosterBinding
import org.intelehealth.app.ui.patient.fragment.PatientOtherInfoFragmentDirections
import org.intelehealth.app.ui.rosterquestionnaire.ui.RosterQuestionnaireMainActivity.Companion.handleBackEventFromRosterToPatientReg
import org.intelehealth.app.ui.rosterquestionnaire.ui.RosterQuestionnaireMainActivity.Companion.startRosterQuestionnaire

import org.intelehealth.app.ui.rosterquestionnaire.utilities.RosterQuestionnaireStage
import org.intelehealth.app.ui.rosterquestionnaire.viewmodel.RosterViewModel
import org.intelehealth.app.utilities.ArrayAdapterUtils
import org.intelehealth.app.utilities.LanguageUtils
import org.intelehealth.app.utilities.PatientRegStage
import org.intelehealth.app.utilities.extensions.hideError
import org.intelehealth.app.utilities.extensions.hideErrorOnTextChang
import org.intelehealth.app.utilities.extensions.validate
import org.intelehealth.app.utilities.extensions.validateDropDowb

@AndroidEntryPoint
class GeneralRosterFragment : BaseRosterFragment(R.layout.fragment_general_roster) {
    private lateinit var binding: FragmentGeneralRosterBinding
    private var patientUuid: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentGeneralRosterBinding.bind(view)
        rosterViewModel = ViewModelProvider.create(requireActivity())[RosterViewModel::class]
        rosterViewModel.updateRosterStage(RosterQuestionnaireStage.GENERAL_ROSTER)

        initViews()
        clickListeners()
    }


    private fun initViews() {
        val intent = requireActivity().intent
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid")
        }
        setupRelationDropdownNew()
        setupMaritalStatusDropdown()
        setupEducationStatusDropdown()
        setupOccupationStatusDropdown()
        setupPhoneOwnershipDropdown()
        setupBpCheckedDropdown()
        setupSugarCheckedDropdown()
        setupBmiCheckedDropdown()
        setupHbCheckedDropdown()
    }

    private fun setupRelationDropdown() {
        /*var houseHoldValue = ""
        try {
            houseHoldValue = PatientsDAO().getHouseHoldValue(patientDTO.getUuid())
        } catch (e: DAOException) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }*/
        //val isHouseholdUuidPresent = SessionManager(requireActivity()).householdUuid.isNotEmpty()
        //Log.d(TAG, "setupRelationDropdown: isHouseholdUuidPresent : "+isHouseholdUuidPresent)
        val isHouseholdUuidPresent = true
        val mRelationshipValue =
            "Self" // TODO: this is mRelationshipValue from IdentificationActivity check while implementation

        val relationshipArrayName =
            if (isHouseholdUuidPresent && mRelationshipValue.equals("Self", ignoreCase = true))
                "relationshipHoH_Self"
            else "relationshipHoH"

        val relationshipArrayId = resources.getIdentifier(
            relationshipArrayName,
            "array",
            requireContext().packageName
        )
        if (relationshipArrayId != 0) {
            val relationshipArray = resources.getStringArray(relationshipArrayId)
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                relationshipArray
            )
            binding.autoCompleteWhatIsYourRelation.setAdapter(adapter)
        }

        binding.autoCompleteWhatIsYourRelation.setOnItemClickListener { parent, view, position, id ->
            binding.textInputLayRelation.hideError()
            LanguageUtils.getSpecificLocalResource(requireContext(), "en").apply {
                if (position == 15) {
                    binding.llOtherRelationLayout.visibility = View.VISIBLE
                } else {
                    binding.llOtherRelationLayout.visibility = View.GONE
                }
            }
        }
    }

    private fun setupMaritalStatusDropdown() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.maritual)
        binding.autoCompleteMaritalStatus.setAdapter(adapter)

        binding.autoCompleteMaritalStatus.setOnItemClickListener { _, _, i, _ ->
            binding.textInputLayMaritalStatus.hideError()
            LanguageUtils.getSpecificLocalResource(requireContext(), "en").apply {

            }
        }
    }

    private fun setupEducationStatusDropdown() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.education_nas)
        binding.autoCompleteEducationStatus.setAdapter(adapter)
        binding.autoCompleteEducationStatus.setOnItemClickListener { _, _, i, _ ->
            binding.textInputLayEducationStatus.hideError()
            LanguageUtils.getSpecificLocalResource(requireContext(), "en").apply {

            }
        }
    }

    private fun setupOccupationStatusDropdown() {
        val adapter =
            ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.occupation_identification)
        binding.autoCompleteOccupation.setAdapter(adapter)

        binding.autoCompleteOccupation.setOnItemClickListener { parent, view, position, id ->
            binding.textInputLayOccupation.hideError()
            LanguageUtils.getSpecificLocalResource(requireContext(), "en").apply {
                if (position == 12) {
                    binding.llOtherOccupationLayout.visibility = View.VISIBLE
                } else {
                    binding.llOtherOccupationLayout.visibility = View.GONE
                }
            }

        }
    }

    private fun setupPhoneOwnershipDropdown() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.phoneownership)
        binding.autoCompletePhoneOwnership.setAdapter(adapter)
        binding.autoCompletePhoneOwnership.setOnItemClickListener { _, _, i, _ ->
            binding.textInputLayPhoneOwnership.hideError()
            LanguageUtils.getSpecificLocalResource(requireContext(), "en").apply {
            }
        }
    }

    private fun setupBpCheckedDropdown() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.bp)
        binding.autoCompleteCheckedBpLastTime.setAdapter(adapter)
        binding.autoCompleteCheckedBpLastTime.setOnItemClickListener { _, _, i, _ ->
            binding.textInputLayCheckedBpLastTime.hideError()
            LanguageUtils.getSpecificLocalResource(requireContext(), "en").apply {

            }
        }
    }

    private fun setupSugarCheckedDropdown() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.sugar)
        binding.autoCompleteSugarCheckedLastTime.setAdapter(adapter)
        binding.autoCompleteSugarCheckedLastTime.setOnItemClickListener { _, _, i, _ ->
            binding.textInputLaySugarCheckedLastTime.hideError()
            LanguageUtils.getSpecificLocalResource(requireContext(), "en").apply {

            }
        }
    }

    private fun setupBmiCheckedDropdown() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.bmi)
        binding.autoCompleteBMICheckedLastTime.setAdapter(adapter)
        binding.autoCompleteBMICheckedLastTime.setOnItemClickListener { _, _, i, _ ->
            binding.textInputLayBMICheckedLastTime.hideError()
            LanguageUtils.getSpecificLocalResource(requireContext(), "en").apply {

            }
        }
    }

    private fun setupHbCheckedDropdown() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.hb)
        binding.autoCompleteHbCheckedLastTime.setAdapter(adapter)
        binding.autoCompleteHbCheckedLastTime.setOnItemClickListener { _, _, i, _ ->
            binding.textInputLayHbCheckedLastTime.hideError()
            LanguageUtils.getSpecificLocalResource(requireContext(), "en").apply {

            }
        }
    }

    private fun validateForm(block: () -> Unit) {
        val error = R.string.this_field_is_mandatory
        /* val relation = binding.textInputLayRelation.validateDropDowb(
             binding.autoCompleteWhatIsYourRelation,
             error
         )*/
        val relation = when {
            // If "Other" is selected, validate the "Other" relation  field
            binding.llOtherRelationLayout.visibility == View.VISIBLE -> {
                binding.textInputLayOtherRelationLayout.validate(
                    binding.textInpuOtherRelation,
                    error
                )
            }

            else -> {
                // If "Other" is not selected, check if the dropdown is not empty
                binding.textInputLayRelation.validateDropDowb(
                    binding.autoCompleteWhatIsYourRelation,
                    error
                )
            }
        }

        val maritalStatus = binding.textInputLayMaritalStatus.validateDropDowb(
            binding.autoCompleteMaritalStatus,
            error
        )
        val educationStatus = binding.textInputLayEducationStatus.validateDropDowb(
            binding.autoCompleteEducationStatus,
            error
        )

        val occupation = when {
            // If "Other" is selected, validate the "Other" occupation field
            binding.llOtherOccupationLayout.visibility == View.VISIBLE -> {
                binding.textInputLayOtherOccupationLayout.validate(
                    binding.textInpuOtherOccupation,
                    error
                )
            }

            else -> {
                // If "Other" is not selected, check if the dropdown is not empty
                binding.textInputLayOccupation.validateDropDowb(
                    binding.autoCompleteOccupation,
                    error
                )
            }
        }

        val phoneOwnership = binding.textInputLayPhoneOwnership.validateDropDowb(
            binding.autoCompletePhoneOwnership,
            error
        )
        val lastTimeBpChecked = binding.textInputLayCheckedBpLastTime.validateDropDowb(
            binding.autoCompleteCheckedBpLastTime,
            error
        )
        val lastTimeSugarChecked = binding.textInputLaySugarCheckedLastTime.validateDropDowb(
            binding.autoCompleteSugarCheckedLastTime,
            error
        )
        val lastTimeHbChecked = binding.textInputLayHbCheckedLastTime.validateDropDowb(
            binding.autoCompleteHbCheckedLastTime,
            error
        )
        val lastTimeBmiChecked = binding.textInputLayBMICheckedLastTime.validateDropDowb(
            binding.autoCompleteBMICheckedLastTime,
            error
        )

        if (relation.and(maritalStatus).and(educationStatus).and(occupation).and(phoneOwnership)
                .and(lastTimeBpChecked).and(lastTimeSugarChecked).and(lastTimeHbChecked)
                .and(lastTimeBmiChecked)
        ) block.invoke()
        /*  if (relation.and(maritalStatus).and(educationStatus).and(occupation).and(phoneOwnership)
                  .and(lastTimeBpChecked).and(lastTimeSugarChecked).and(lastTimeHbChecked)
          ) block.invoke()*/
    }

    private fun clickListeners() {
        // val activityBinding = (requireActivity() as RosterQuestionnaireMainActivity).binding
        binding.frag2BtnBack.setOnClickListener {
            // Handle back button click
            handleBackEventFromRosterToPatientReg(
                requireActivity(),
                patientUuid,

                PatientRegStage.OTHER
            )
        }

        binding.frag2BtnNext.setOnClickListener {
            validateForm { navigateToDetails() }
        }
        binding.textInputLayOtherRelationLayout.hideErrorOnTextChang(binding.textInpuOtherRelation)
        binding.textInputLayOtherOccupationLayout.hideErrorOnTextChang(binding.textInpuOtherOccupation)
    }

    private fun navigateToDetails() {
      /*  if (rosterViewModel.isEditMode) {
            GeneralRosterFragmentDirections.navigationGeneralToDetails(
                patientUuid, "roster", "false"
            ).also {
                findNavController().navigate(it)
                requireActivity().finish()
            }
        } else {*/
            GeneralRosterFragmentDirections.navigationGeneralToPregnancyRoster().apply {
                findNavController().navigate(this)
            }
       // }
    }

    private fun setupRelationDropdownNew() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.relationshipHoH)
        binding.autoCompleteWhatIsYourRelation.setAdapter(adapter)

        binding.autoCompleteWhatIsYourRelation.setOnItemClickListener { parent, view, position, id ->
            binding.textInputLayRelation.hideError()
            LanguageUtils.getSpecificLocalResource(requireContext(), "en").apply {
                if (position == 15) {
                    binding.llOtherRelationLayout.visibility = View.VISIBLE
                } else {
                    binding.llOtherRelationLayout.visibility = View.GONE
                }
            }
        }
    }
}
