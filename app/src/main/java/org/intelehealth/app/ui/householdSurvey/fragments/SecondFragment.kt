package org.intelehealth.app.ui.householdSurvey.fragments

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import androidx.navigation.fragment.findNavController
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import org.intelehealth.app.R
import org.intelehealth.app.ui.householdSurvey.models.HouseholdSurveyModel
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.databinding.FragmentSecondHouseholdSurveyBinding
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.ui.filter.FirstLetterUpperCaseInputFilter
import org.intelehealth.app.utilities.ArrayAdapterUtils
import org.intelehealth.app.utilities.HouseholdSurveyStage
import org.intelehealth.app.utilities.LanguageUtils
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.StringUtils
import org.intelehealth.app.utilities.StringUtils.getHouseholdHeadReligion
import org.intelehealth.app.utilities.extensions.addFilter
import org.json.JSONArray
import org.json.JSONException
import java.util.Locale

class SecondFragment : BaseHouseholdSurveyFragment(R.layout.fragment_second_household_survey) {
    private val TAG = "SecondFragment"
    private lateinit var binding: FragmentSecondHouseholdSurveyBinding
    private var patientUuid: String? = null
    private lateinit var updatedContext: Context

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSecondHouseholdSurveyBinding.bind(view)
        houseHoldViewModel.updatePatientStage(HouseholdSurveyStage.SECOND_SCREEN)

        initViews()
    }

    private fun setClickListener() {
        binding.frag2BtnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.frag2BtnNext.setOnClickListener {
            savePatient()

        }
        binding.otherIncomeCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
            binding.llOtherSourcesOfIncomeLayout.visibility =
                if (isChecked) View.VISIBLE else View.GONE
        }
    }

    private fun initViews() {
        val intent = requireActivity().intent
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid")
        }
        setClickListener()
        applyFilter()
    }

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
        setupReligion()
        setupCaste()
        setIncomeSourcesDataToUI()
    }

    private fun setupIncomeSources() {
        val otherIncome: String = if (binding.otherIncomeCheckbox.isChecked) {
            StringUtils.getValue(binding.textInputOtherSourcesOfIncome.text.toString())
        } else {
            "-"
        }
        householdSurveyModel.primarySourceOfIncome = StringUtils.getSelectedCheckboxes(
            binding.llPrimarySourceOfIncome,
            SessionManager(requireActivity()).appLanguage,
            context,
            otherIncome
        )
    }


    private fun savePatient() {
        householdSurveyModel.apply {
            headOfHouseholdName = binding.textInputHeadOfHousehold.text.toString()
            numberOfSmartPhones = binding.textInputNumberOfSmartPhones.text.toString()
            numberOfFeaturePhones = binding.textInputNumberOfFeaturePhones.text.toString()
            numberOfEarningMembers = binding.textInputNumberOfEarningMembers.text.toString()
            setupIncomeSources()
            //for other religion
            if (binding.llOtherReligionLayout.visibility == View.VISIBLE) {
                religion =
                    binding.textInpuOtherReligion.text.toString().takeIf { it.isNotEmpty() } ?: "-"
            }
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
            "secondScreen", patient,
            householdSurveyModel
        ).observe(viewLifecycleOwner) {
            it ?: return@observe
            houseHoldViewModel.handleResponse(it) { result -> if (result) navigateToDetails() }
        }
    }

    private fun navigateToDetails() {
        SecondFragmentDirections.actionTwoToThree().apply {
            findNavController().navigate(this)
        }
    }

    private fun setupReligion() {
        val adapter =
            ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.householdHeadReligion)
        binding.autoCompleteReligion.setAdapter(adapter)

        if (householdSurveyModel.religion != null && householdSurveyModel.religion.isNotEmpty() && !householdSurveyModel.religion.equals(
                "-"
            )
        ) {
            val translatedValue = StringUtils.getHouseholdHeadReligionEdit(
                householdSurveyModel.religion,
                requireContext(),
                SessionManager(requireActivity()).appLanguage
            )
            val position: Int = getIndex(binding.autoCompleteReligion, translatedValue)
            if (position == -1) { // If "Other" was selected by the user
                binding.llOtherReligionLayout.visibility = View.VISIBLE
                binding.autoCompleteReligion.setText(
                    requireActivity().getString(R.string.religion_other),
                    false
                ) // Set "Other" in AutoCompleteTextView
                binding.textInpuOtherReligion.setText(householdSurveyModel.religion)
            } else {
                binding.llOtherReligionLayout.visibility = View.GONE
                binding.autoCompleteReligion.setText(householdSurveyModel.religion, false)
            }
        } else {
            binding.autoCompleteReligion.setText("")
            binding.autoCompleteReligion.clearFocus()
        }
        binding.autoCompleteReligion.setOnItemClickListener { parent, view, position, id ->
            if (position == 5) {
                binding.llOtherReligionLayout.visibility = View.VISIBLE
            } else {
                binding.llOtherReligionLayout.visibility = View.GONE
                var religion = getHouseholdHeadReligion(
                    parent.getItemAtPosition(position).toString(),
                    requireContext(),
                    SessionManager(requireActivity()).appLanguage
                )
                if (religion.isNullOrEmpty()) {
                    religion = "-"
                }
                householdSurveyModel.religion = religion
            }
        }
    }

    private fun setupCaste() {
        val adapter =
            ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.householdHeadCaste)
        binding.autoCompleteCaste.setAdapter(adapter)
        if (householdSurveyModel.caste != null && householdSurveyModel.caste.isNotEmpty()) {
            binding.autoCompleteCaste.setText(householdSurveyModel.caste, false)
        }
        binding.autoCompleteCaste.setOnItemClickListener { _, _, i, _ ->
            // binding.textInputLayEducation.hideError()
            LanguageUtils.getSpecificLocalResource(requireContext(), "en").apply {
                householdSurveyModel.caste = this.getStringArray(R.array.householdHeadCaste)[i]
            }
        }
    }

    private fun setIncomeSourcesDataToUI() {
        val primaryIncomeSourceValue = householdSurveyModel.primarySourceOfIncome
        if (!primaryIncomeSourceValue.isNullOrEmpty()) {
            try {
                val jsonArray = JSONArray(primaryIncomeSourceValue)
                if (jsonArray.length() > 0) {
                    fun setCheckboxState(checkbox: CheckBox, stringResId: Int) {
                        checkbox.isChecked =
                            primaryIncomeSourceValue.contains(
                                updatedContext.getString(
                                    stringResId
                                )
                            ) == true
                    }
                    setCheckboxState(
                        binding.saleOfCerealProductionCheckbox,
                        R.string.sale_of_cereal_production
                    )
                    setCheckboxState(
                        binding.saleOfAnimalProductsCheckbox,
                        R.string.sale_of_animals_or_animal_products
                    )
                    setCheckboxState(
                        binding.agriculturalWageLaborCheckbox,
                        R.string.agricultural_wage_labor_employed_for_farm_work
                    )
                    setCheckboxState(
                        binding.salariedWorkerCheckbox,
                        R.string.salaried_worker_fixed_monthly_salary
                    )
                    setCheckboxState(
                        binding.selfEmployedCheckbox,
                        R.string.self_employed_non_agricultural_petty_business
                    )
                    setCheckboxState(
                        binding.dailyLaborCheckbox,
                        R.string.daily_labor_unskilled_work_agricultural_non_agricultural
                    )
                    setCheckboxState(binding.nregaCheckbox, R.string.nrega)
                    setCheckboxState(binding.seasonalLaborCheckbox, R.string.seasonal_labor)
                    setCheckboxState(binding.pensionCheckbox, R.string.pension)
                    setCheckboxState(binding.remittanceCheckbox, R.string.remittances_checkbox)

                    if (primaryIncomeSourceValue.contains(requireActivity().getString(R.string.other_please_specify))) {
                        binding.otherIncomeCheckbox.isChecked = true

                        val tempContext = if (SessionManager(requireActivity()).appLanguage.equals(
                                "mr",
                                ignoreCase = true
                            )
                        ) {
                            val config =
                                Configuration(IntelehealthApplication.getAppContext().resources.configuration)
                            config.setLocale(Locale("en"))
                            requireContext().createConfigurationContext(config)
                        } else {
                            requireContext()
                        }

                        try {
                            val jsonArray = JSONArray(primaryIncomeSourceValue)
                            val otherIncome = (0 until jsonArray.length())
                                .asSequence()
                                .map { jsonArray.getString(it) }
                                .firstOrNull { it.contains(tempContext.getString(R.string.other_please_specify)) }
                                ?.substringAfter(": ")
                                ?.takeIf { it != "-" }

                            binding.textInputOtherSourcesOfIncome.setText(otherIncome ?: "")
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    } else {
                        binding.otherIncomeCheckbox.isChecked = false
                    }
                }
            } catch (e: Exception) {
            }
        }
    }


    private fun getIndex(autoCompleteTextView: AutoCompleteTextView, target: String): Int {
        val adapter = autoCompleteTextView.adapter
        if (adapter != null) {
            for (i in 0 until adapter.count) {
                if (adapter.getItem(i).toString().equals(target, ignoreCase = true)) {
                    return i
                }
            }
        }
        return -1 // Return -1 if not found
    }
    private fun applyFilter() {
        binding.textInputHeadOfHousehold.addFilter(FirstLetterUpperCaseInputFilter())
        binding.textInpuOtherReligion.addFilter(FirstLetterUpperCaseInputFilter())
        binding.textInputOtherSourcesOfIncome.addFilter(FirstLetterUpperCaseInputFilter())
    }
}