package org.intelehealth.app.ui.baseline_survey.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import org.intelehealth.app.R
import org.intelehealth.app.activities.patientDetailActivity.StaticPatientRegistrationEnabledFieldsHelper
import org.intelehealth.app.databinding.FragmentBaselineSurveyMedicalBinding
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.utilities.ArrayAdapterUtils
import org.intelehealth.app.utilities.BaselineSurveyStage
import org.intelehealth.app.utilities.LanguageUtils
import org.intelehealth.app.utilities.PatientRegFieldsUtils
import org.intelehealth.app.utilities.extensions.hideError

/**
 * Created by Shazzad H Kanon on 06-12-2024 - 11:00.
 * Email : shazzad@intelehealth.org
 * Mob   : +8801647040520
 **/


class BaselineMedicalFragment :
    BaseFragmentBaselineSurvey(R.layout.fragment_baseline_survey_medical) {

    private lateinit var binding: FragmentBaselineSurveyMedicalBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentBaselineSurveyMedicalBinding.bind(view)
        baselineSurveyViewModel.updateBaselineStage(BaselineSurveyStage.MEDICAL)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onPatientDataLoaded(patient: PatientDTO) {
        super.onPatientDataLoaded(patient)
        Timber.d { "onPatientDataLoaded" }
        Timber.d { Gson().toJson(patient) }
        fetchMedicalBaselineConfig()
        binding.patient = patient
        binding.baselineEditMode = baselineSurveyViewModel.baselineEditMode
    }

    private fun fetchMedicalBaselineConfig() {
        val it = getStaticPatientRegistrationFields()
        binding.medicalConfig = PatientRegFieldsUtils.buildMedicalBaselineConfig(it)
        setValues()
        setClickListener()
    }

    private fun getStaticPatientRegistrationFields() =
        StaticPatientRegistrationEnabledFieldsHelper.getEnabledMedicalBaselineFields()

    private fun setValues() {
        setupHbCheck()
        setupBpCheck()
        setupSugarCheck()
        setupSurgeries()
    }

    private fun setupHbCheck() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.hb_check)
        binding.acHbCheck.setAdapter(adapter)
        binding.acHbCheck.setText("Select your choice", false)

        binding.acHbCheck.setOnItemClickListener { _, _, i, _ ->
            binding.tilHbCheckOption.hideError()
            LanguageUtils.getSpecificLocalResource(requireContext(), "en").apply {
                binding.acHbCheck.setText(this.getStringArray(R.array.hb_check)[i], false)
            }
        }
    }

    private fun setupBpCheck() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.bp_check)
        binding.acBpCheck.setAdapter(adapter)
        binding.acBpCheck.setText("Select your choice", false)

        binding.acBpCheck.setOnItemClickListener { _, _, i, _ ->
            binding.tilBpCheckOption.hideError()
            LanguageUtils.getSpecificLocalResource(requireContext(), "en").apply {
                binding.acBpCheck.setText(this.getStringArray(R.array.bp_check)[i], false)
            }
        }
    }

    private fun setupSugarCheck() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.sugar_check)
        binding.acSugarCheck.setAdapter(adapter)
        binding.acSugarCheck.setText("Select your choice", false)

        binding.acSugarCheck.setOnItemClickListener { _, _, i, _ ->
            binding.tilSugarCheckOption.hideError()
            LanguageUtils.getSpecificLocalResource(requireContext(), "en").apply {
                binding.acSugarCheck.setText(this.getStringArray(R.array.sugar_check)[i], false)
            }
        }
    }

    private fun setupSurgeryReasonCheck() {
        val adapter =
            ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.surgery_reason_check)
        binding.acSurgeryReasonCheck.setAdapter(adapter)
        binding.acSurgeryReasonCheck.setText("Select your choice", false)

        binding.acSurgeryReasonCheck.setOnItemClickListener { _, _, i, _ ->
            binding.tilSurgeryReasonOption.hideError()
            LanguageUtils.getSpecificLocalResource(requireContext(), "en").apply {
                binding.acSurgeryReasonCheck.setText(
                    this.getStringArray(R.array.surgery_reason_check)[i],
                    false
                )
            }
        }
    }

    private fun setupSurgeries() {
        binding.rgSurgeryOptions.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radioSurgeryYes -> {
                    binding.rgSurgeryOptions.check(R.id.radioSurgeryYes)
                    binding.medicalConfig?.surgeryReason?.isEnabled = true
                    setupSurgeryReasonCheck()
                }

                R.id.radioSurgeryNo -> {
                    binding.rgSurgeryOptions.check(R.id.radioSurgeryNo)
                    binding.medicalConfig?.surgeryReason?.isEnabled = false
                }
            }
        }
    }

    private fun setClickListener() {
        binding.frag2BtnBack.setOnClickListener {
            BaselineMedicalFragmentDirections.navigationMedicalToGeneral().apply {
                findNavController().navigate(this)
            }
        }
        binding.frag2BtnNext.setOnClickListener {
            BaselineMedicalFragmentDirections.navigationMedicalToOther().apply {
                findNavController().navigate(this)
            }
        }
    }
}