package org.intelehealth.app.ui.patient.fragment

import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.annotation.RequiresApi
import androidx.databinding.OnRebindCallback
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.intelehealth.app.BuildConfig
import org.intelehealth.app.R
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.databinding.FragmentPatientOtherInfoBinding
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.ui.filter.FirstLetterUpperCaseInputFilter
import org.intelehealth.app.utilities.ArrayAdapterUtils
import org.intelehealth.app.utilities.CustomLog
import org.intelehealth.app.utilities.FlavorKeys
import org.intelehealth.app.utilities.LanguageUtils
import org.intelehealth.app.utilities.PatientRegFieldsUtils
import org.intelehealth.app.utilities.PatientRegStage
import org.intelehealth.app.utilities.extensions.addFilter
import org.intelehealth.app.utilities.extensions.hideDigitErrorOnTextChang
import org.intelehealth.app.utilities.extensions.hideError
import org.intelehealth.app.utilities.extensions.hideErrorOnTextChang
import org.intelehealth.app.utilities.extensions.validate
import org.intelehealth.app.utilities.extensions.validateDigit
import org.intelehealth.app.utilities.extensions.validateDropDowb
import java.io.File

/**
 * Created by Vaghela Mithun R. on 27-06-2024 - 13:42.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PatientOtherInfoFragment : BasePatientFragment(R.layout.fragment_patient_other_info) {
    private lateinit var binding: FragmentPatientOtherInfoBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentPatientOtherInfoBinding.bind(view)
        patientViewModel.updatePatientStage(PatientRegStage.OTHER)
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setupSocialCategory() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.caste)
        binding.autoCompleteSocialCategory.setAdapter(adapter)
        if (patient.caste != null && patient.caste.isNotEmpty()) {
            binding.autoCompleteSocialCategory.setText(patient.caste, false)
        }
        binding.autoCompleteSocialCategory.setOnItemClickListener { _, _, i, _ ->
            binding.textInputLaySocialCategory.hideError()
            LanguageUtils.getSpecificLocalResource(requireContext(), "en").apply {
                patient.caste = this.getStringArray(R.array.caste)[i]
            }
        }
    }


    override fun onPatientDataLoaded(patient: PatientDTO) {
        super.onPatientDataLoaded(patient)
        Timber.d { "onPatientDataLoaded" }
        Timber.d { Gson().toJson(patient) }
        patient.codeOfHealthFacility = LanguageUtils.getCodeOfHf(patient.province)

        binding.patient = patient
        binding.isEditMode = patientViewModel.isEditMode
        fetchPersonalInfoConfig()
    }

    private fun fetchPersonalInfoConfig() {

        /* patientViewModel.fetchOtherRegFields().observe(viewLifecycleOwner) {
             binding.otherInfoConfig = PatientRegFieldsUtils.buildPatientOtherInfoConfig(it)
             setupSocialCategory()
             setupEducations()
             setupHealthFacility()
             setupEconomicCategory()
             applyFilter()
             setInputTextChangListener()
             setClickListener()
         }*/

        lifecycleScope.launch {
            patientViewModel.fetchOtherRegFieldsSuspended().let {
                binding.otherInfoConfig = PatientRegFieldsUtils.buildPatientOtherInfoConfig(it)
                setupSocialCategory()
                setupEducations()
                setupHealthFacility()
                setupEconomicCategory()
                applyFilter()
                setInputTextChangListener()
                setClickListener()
                setupCodeOfHealthFacilityTextField()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupCodeOfHealthFacilityTextField() {
        if (BuildConfig.FLAVOR_client == FlavorKeys.UNFPA) {
            binding.textInputCodeOfHealthyFacility.isEnabled = false
            binding.textInputCodeOfHealthyFacility.isClickable = false
            binding.textInputCodeOfHealthyFacility.isFocusableInTouchMode = false
            binding.textInputCodeOfHealthyFacility.inputType = InputType.TYPE_NULL
        }
    }

    private fun setupEconomicCategory() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.economic)
        binding.autoCompleteEconomicCategory.setAdapter(adapter)
        if (patient.economic != null && patient.economic.isNotEmpty()) {
            binding.autoCompleteEconomicCategory.setText(patient.economic, false)
        }
        binding.autoCompleteEconomicCategory.setOnItemClickListener { _, _, i, _ ->
            binding.textInputLayEducation.hideError()
            LanguageUtils.getSpecificLocalResource(requireContext(), "en").apply {
                patient.economic = this.getStringArray(R.array.economic)[i]
            }
        }
    }

    private fun setClickListener() {
        binding.frag2BtnBack.setOnClickListener { findNavController().popBackStack() }
        binding.frag2BtnNext.setOnClickListener {
            validateForm { savePatient() }
        }
    }

    private fun savePatient() {
        patient.apply {
            nationalID = binding.textInputNationalId.text?.toString()
            occupation = binding.textInputOccupation.text?.toString()
            tmhCaseNumber = binding.textInputTmhCaseNumber.text?.toString()
            requestId = binding.textInputRequestId.text?.toString()
            discipline = binding.textInputDiscipline.text?.toString()
            department = binding.textInputDepartment.text?.toString()
            relativePhoneNumber = binding.textInputRelativePhoneNumber.text?.toString()

            inn = binding.textInputInn.text?.toString()
            codeOfHealthFacility = binding.textInputCodeOfHealthyFacility.text?.toString()
            codeOfDepartment = binding.textInputCodeOfDepartment.text?.toString()
            department = binding.textInputDepartment.text?.toString()

            patientViewModel.updatedPatient(this)
            patientViewModel.savePatient().observe(viewLifecycleOwner) {
                it ?: return@observe
                patientViewModel.handleResponse(it) { result -> if (result) navigateToDetails() }
            }
        }
    }

    private fun navigateToDetails() {
        PatientOtherInfoFragmentDirections.navigationOtherToDetails(
            patient.uuid, "searchPatient", "false"
        ).also {
            deleteUnusedImages()
            findNavController().navigate(it)
            requireActivity().finish()
        }
    }

    /**
     * after moving the image from unSavedImage folder to main folder
     * we are deleting moved image
     */
    private fun deleteUnusedImages() {
        try {
            val source =
                File(AppConstants.IMAGE_PATH + AppConstants.UNSAVED_IMAGE_DIRECTORY + File.separator + patient.uuid + ".jpg")

            if (source.exists() && source.path.contains(AppConstants.UNSAVED_IMAGE_DIRECTORY)) {
                source.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun applyFilter() {
//        binding.textInputNationalId.addFilter(FirstLetterUpperCaseInputFilter())
        binding.textInputOccupation.addFilter(FirstLetterUpperCaseInputFilter())
        binding.textInputDepartment.addFilter(FirstLetterUpperCaseInputFilter())
    }

    private fun setInputTextChangListener() {
        binding.textInputLayNationalId.hideErrorOnTextChang(binding.textInputNationalId)
        binding.textInputLayOccupation.hideErrorOnTextChang(binding.textInputOccupation)

        binding.textInputLayTmhCaseNumber.hideErrorOnTextChang(binding.textInputTmhCaseNumber)
        binding.textInputLayRequestId.hideErrorOnTextChang(binding.textInputRequestId)
        binding.textInputLayDiscipline.hideErrorOnTextChang(binding.textInputDiscipline)
        binding.textInputLayDepartment.hideErrorOnTextChang(binding.textInputDepartment)
        binding.textInputLayRelativePhoneNumber.hideDigitErrorOnTextChang(
            binding.textInputRelativePhoneNumber, 10
        )
        binding.textInputLayInn.hideErrorOnTextChang(binding.textInputInn)
        binding.textInputLayCodeOfHealthyFacility.hideErrorOnTextChang(binding.textInputCodeOfHealthyFacility)
        binding.textInputLayCodeOfDepartment.hideErrorOnTextChang(binding.textInputCodeOfDepartment)
    }

    private fun setupEducations() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.education)
        binding.autoCompleteEducation.setAdapter(adapter)
        if (patient.education != null && patient.education.isNotEmpty()) {
            binding.autoCompleteEducation.setText(patient.education, false)
        }
        binding.autoCompleteEducation.setOnItemClickListener { _, _, i, _ ->
            binding.textInputLayEducation.hideError()
            LanguageUtils.getSpecificLocalResource(requireContext(), "en").apply {
                patient.education = this.getStringArray(R.array.education)[i]
            }
        }
    }

    private fun setupHealthFacility() {
        val adapter =
            ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.health_facility_name)
        binding.autoCompleteHealthFacilityName.setAdapter(adapter)
        if (patient.healthFacilityName != null && patient.healthFacilityName.isNotEmpty()) {
            binding.autoCompleteHealthFacilityName.setText(patient.healthFacilityName, false)
        }
        binding.autoCompleteHealthFacilityName.setOnItemClickListener { _, _, i, _ ->
            binding.textInputLayHealthFacilityName.hideError()
            LanguageUtils.getSpecificLocalResource(requireContext(), "en").apply {
                patient.healthFacilityName = this.getStringArray(R.array.health_facility_name)[i]
            }
        }
    }

    private fun validateForm(block: () -> Unit) {
        Timber.d { "Final patient =>${Gson().toJson(patient)}" }
        val error = R.string.this_field_is_mandatory
        binding.otherInfoConfig?.let {
            val bNationalId =
                if (it.nationalId?.isEnabled == true && it.nationalId?.isMandatory == true) {
                    binding.textInputLayNationalId.validate(binding.textInputNationalId, error)
                } else true

            val bOccuptions =
                if (it.occuptions?.isEnabled == true && it.occuptions?.isMandatory == true) {
                    binding.textInputLayOccupation.validate(binding.textInputOccupation, error)
                } else true

            val bInn = if (it.inn?.isEnabled == true && it.inn?.isMandatory == true) {
                binding.textInputLayInn.validate(binding.textInputInn, error)
            } else true

            val bCodeOfHealthyFacility =
                if (it.codeOfHealthyFacility?.isEnabled == true && it.codeOfHealthyFacility?.isMandatory == true) {
                    binding.textInputLayCodeOfHealthyFacility.validate(
                        binding.textInputCodeOfHealthyFacility,
                        error
                    )
                } else true

            val bCodeOfDepartment =
                if (it.codeOfDepartment?.isEnabled == true && it.codeOfDepartment?.isMandatory == true) {
                    binding.textInputLayCodeOfDepartment.validate(
                        binding.textInputCodeOfDepartment,
                        error
                    )
                } else true

            val bDepartment =
                if (it.department?.isEnabled == true && it.department?.isMandatory == true) {
                    binding.textInputLayDepartment.validate(binding.textInputDepartment, error)
                } else true

            val bHealthFacilityName =
                if (it.healthFacilityName?.isEnabled == true && it.healthFacilityName?.isMandatory == true) {
                    binding.textInputLayHealthFacilityName.validateDropDowb(
                        binding.autoCompleteHealthFacilityName, error
                    )
                } else true


            val bSocialCategory =
                if (it.socialCategory?.isEnabled == true && it.socialCategory?.isMandatory == true) {
                    binding.textInputLaySocialCategory.validateDropDowb(
                        binding.autoCompleteSocialCategory, error
                    )
                } else true

            val bEducation =
                if (it.education?.isEnabled == true && it.education?.isMandatory == true) {
                    binding.textInputLayEducation.validateDropDowb(
                        binding.autoCompleteEducation, error
                    )
                } else true

            val bEconomic =
                if (it.economicCategory?.isEnabled == true && it.economicCategory?.isMandatory == true) {
                    binding.textInputLayEconomicCategory.validateDropDowb(
                        binding.autoCompleteEconomicCategory, error
                    )
                } else true

            val tmhCaseNumber =
                if (it.tmhCaseNumber?.isEnabled == true && it.tmhCaseNumber?.isMandatory == true) {
                    binding.textInputLayTmhCaseNumber.validate(
                        binding.textInputTmhCaseNumber, error
                    )
                } else true

            val requestId =
                if (it.requestId?.isEnabled == true && it.requestId?.isMandatory == true) {
                    binding.textInputLayRequestId.validate(
                        binding.textInputRequestId, error
                    )
                } else true

            val discipline =
                if (it.discipline?.isEnabled == true && it.discipline?.isMandatory == true) {
                    binding.textInputLayDiscipline.validate(
                        binding.textInputDiscipline, error
                    )
                } else true

            val relativePhoneNumber =
                if (it.relativePhoneNumber?.isEnabled == true && it.relativePhoneNumber?.isMandatory == true) {
                    binding.textInputLayRelativePhoneNumber.validate(
                        binding.textInputRelativePhoneNumber, error
                    ).and(
                        binding.textInputLayRelativePhoneNumber.validateDigit(
                            binding.textInputRelativePhoneNumber, R.string.enter_10_digits, 10
                        )
                    )


                } else true


            if (bOccuptions.and(bSocialCategory).and(bEducation).and(bEconomic).and(bNationalId)
                    .and(bOccuptions)
                    .and(tmhCaseNumber).and(requestId).and(discipline).and(relativePhoneNumber)
                    .and(bInn)
                    .and(bCodeOfHealthyFacility).and(bHealthFacilityName).and(bCodeOfDepartment)
                    .and(bDepartment)
            ) block.invoke()
        }
    }
}