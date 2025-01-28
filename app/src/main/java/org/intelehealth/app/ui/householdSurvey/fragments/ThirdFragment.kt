package org.intelehealth.app.ui.householdSurvey.fragments

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import androidx.navigation.fragment.findNavController
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import org.intelehealth.app.R
import org.intelehealth.app.ui.householdSurvey.models.HouseholdSurveyModel
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.databinding.FragmentThirdHouseholdSurveyBinding
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.utilities.HouseholdSurveyStage
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.StringUtils
import org.intelehealth.app.utilities.StringUtils.getWaterSourceDistance
import org.intelehealth.app.utilities.StringUtils.getWaterSourceDistanceEdit
import org.intelehealth.core.registry.PermissionRegistry
import org.json.JSONArray
import org.json.JSONException
import java.util.Calendar
import java.util.Locale

class ThirdFragment : BaseHouseholdSurveyFragment(R.layout.fragment_third_household_survey) {

    private lateinit var binding: FragmentThirdHouseholdSurveyBinding
    var selectedDate = Calendar.getInstance().timeInMillis
    private var patientUuid: String? = null
    private lateinit var updatedContext: Context
    private var mDistanceFromWaterSource: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentThirdHouseholdSurveyBinding.bind(view)
        houseHoldViewModel.updatePatientStage(HouseholdSurveyStage.THIRD_SCREEN)

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
        binding.rgHouseholdElectricity.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbElectricityYes -> {
                    binding.llHouseholdElectricityYesOption.visibility = View.VISIBLE
                }

                R.id.rbElectricityNo -> {
                    binding.llHouseholdElectricityYesOption.visibility = View.GONE
                    binding.loadSheddingHoursTextView.setText("0")
                    binding.loadSheddingDaysPerWeekTextView.setText("0")
                    householdSurveyModel.noOfHoursOfLoadSheddingPerDay= null
                    householdSurveyModel.noOfDaysOfLoadSheddingPerWeek= null
                    //reset val of yesoption check  - kaveri
                }
            }
        }
        binding.runningWaterRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.running_water_yes_checkbox -> {
                    binding.llPrimarySourceOfWaterYesOption.visibility = View.VISIBLE
                    binding.llPrimarySourceOfWaterNoOption.visibility = View.GONE
                }

                R.id.running_water_no_checkbox -> {
                    binding.llPrimarySourceOfWaterYesOption.visibility = View.GONE
                    binding.llPrimarySourceOfWaterNoOption.visibility = View.VISIBLE
                    binding.textInputWaterSupplyAvailabilityHrsPerDay.setText("0")
                    binding.textInputWaterSupplyAvailabilityDaysPerWeek.setText("0")
                    householdSurveyModel.waterSupplyAvailabilityHrsPerDay= null
                    householdSurveyModel.waterSupplyAvailabilityDaysperWeek= null
                    //reset val of yesoption check  - kaveri
                }
            }
        }
        binding.rgWaterSourceDistance.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbDistanceMeter -> {
                    mDistanceFromWaterSource = "Meter"
                    binding.llWaterSourceDistance.visibility = View.VISIBLE
                }

                R.id.rbDistanceKMeter -> {
                    mDistanceFromWaterSource = "KM"
                    binding.llWaterSourceDistance.visibility = View.VISIBLE
                }
            }
        }
        binding.otherCheckboxWaterSource.setOnCheckedChangeListener { buttonView, isChecked ->
            binding.llOtherSourcesOfWaterEdiText.visibility =
                if (isChecked) View.VISIBLE else View.GONE
        }
    }

    private fun initViews() {
        val intent = requireActivity().intent
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid")
        }
        setClickListener()
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

        setDataToElectricityStatusOptions()
        setDataForRunningWaterOptions()
        setDataToBankStatusRadio()
        setWaterSourceDistance()
    }

    private fun saveAndNavigateToDetails(
        patient: PatientDTO,
        householdSurveyModel: HouseholdSurveyModel
    ) {
        houseHoldViewModel.savePatient(
            "thirdScreen", patient,
            householdSurveyModel
        ).observe(viewLifecycleOwner) {
            it ?: return@observe
            houseHoldViewModel.handleResponse(it) { result -> if (result) navigateToDetails() }
        }
    }

    private fun navigateToDetails() {
        ThirdFragmentDirections.actionThreeToFour().apply {
            findNavController().navigate(this)
        }
    }

    private fun setDataToElectricityStatusOptions() {
        val electricityStatus = householdSurveyModel.householdElectricityStatus
        if (!electricityStatus.isNullOrEmpty()) {
            //from db
            electricityStatus.let {
                if (it.equals(updatedContext.getString(R.string.yes), ignoreCase = true)) {
                    binding.rbElectricityYes.isChecked = true
                    if (!householdSurveyModel.noOfHoursOfLoadSheddingPerDay.isNullOrEmpty() && !householdSurveyModel.noOfHoursOfLoadSheddingPerDay.equals("-"))
                        binding.loadSheddingHoursTextView.setText(householdSurveyModel.noOfHoursOfLoadSheddingPerDay)

                    if (!householdSurveyModel.noOfDaysOfLoadSheddingPerWeek.isNullOrEmpty() && !householdSurveyModel.noOfHoursOfLoadSheddingPerDay.equals("-"))
                        binding.loadSheddingDaysPerWeekTextView.setText(householdSurveyModel.noOfDaysOfLoadSheddingPerWeek)

                } else if (it.equals(updatedContext.getString(R.string.no), ignoreCase = true)) {
                    binding.rbElectricityNo.isChecked = true
                }
            }
        }

    }

    private fun savePatient() {
        householdSurveyModel.apply {
            waterSourceDistance = (binding.textInputWaterSourceDistance.text?.toString() + " "+mDistanceFromWaterSource).trim()
            //waterSourceDistance = binding.textInputWaterSourceDistance.text?.toString()

            setupElectricityOptions()
            setupRunningWaterOptions()
            setupBankStatusRadioButtons()
            /*
             //for other religion
             if (binding.llOtherReligionLayout.visibility == View.VISIBLE) {
                 religion =
                     binding.textInpuOtherReligion.text.toString().takeIf { it.isNotEmpty() } ?: "-"
             }*/
            Log.d("devchdbsave", "savePatient: householdSurveyModel : " + householdSurveyModel)
            houseHoldViewModel.updatedPatient(this)
            val patient = PatientDTO()
            patient.uuid = patientUuid
            saveAndNavigateToDetails(patient, householdSurveyModel)
        }
    }

    private fun setupElectricityOptions() {
        //insert
        if (binding.rgHouseholdElectricity.checkedRadioButtonId == binding.rbElectricityYes.id) {
            householdSurveyModel.householdElectricityStatus = StringUtils.getPreTerm(
                binding.rbElectricityYes.text.toString(),
                SessionManager(requireActivity()).appLanguage
            )
        } else if (binding.rgHouseholdElectricity.checkedRadioButtonId == binding.rbElectricityNo.id) {
            householdSurveyModel.householdElectricityStatus = StringUtils.getPreTerm(
                binding.rbElectricityNo.text.toString(),
                SessionManager(requireActivity()).appLanguage
            )
        }

        if (binding.rgHouseholdElectricity.checkedRadioButtonId == binding.rbElectricityYes.id) {
            householdSurveyModel.noOfHoursOfLoadSheddingPerDay =
                binding.loadSheddingHoursTextView.text.toString().let {
                    it.ifEmpty { "-" }
                }
            householdSurveyModel.noOfDaysOfLoadSheddingPerWeek =
                binding.loadSheddingDaysPerWeekTextView.text.toString().let {
                    it.ifEmpty { "-" }
                }
        }


    }

    private fun setupRunningWaterOptions() {
        //insert
        if (binding.runningWaterRadioGroup.checkedRadioButtonId == binding.runningWaterYesCheckbox.id) {
            householdSurveyModel.runningWaterStatus = StringUtils.getPreTerm(
                binding.runningWaterYesCheckbox.text.toString(),
                SessionManager(requireActivity()).appLanguage
            )
        } else if (binding.runningWaterRadioGroup.checkedRadioButtonId == binding.runningWaterNoCheckbox.id) {
            householdSurveyModel.runningWaterStatus = StringUtils.getPreTerm(
                binding.runningWaterNoCheckbox.text.toString(),
                SessionManager(requireActivity()).appLanguage
            )
        }

        if (binding.runningWaterRadioGroup.checkedRadioButtonId == binding.runningWaterYesCheckbox.id) {
            //for yes option

            if (!binding.textInputWaterSupplyAvailabilityHrsPerDay.text.toString()
                    .isNullOrEmpty()
            ) {
                householdSurveyModel.waterSupplyAvailabilityHrsPerDay =
                    binding.textInputWaterSupplyAvailabilityHrsPerDay.text.toString()
            } else {
                householdSurveyModel.waterSupplyAvailabilityHrsPerDay = "-"
            }

            if (!binding.textInputWaterSupplyAvailabilityDaysPerWeek.text.toString()
                    .isNullOrEmpty()
            ) {
                householdSurveyModel.waterSupplyAvailabilityDaysperWeek =
                    binding.textInputWaterSupplyAvailabilityDaysPerWeek.text.toString()
            } else {
                householdSurveyModel.waterSupplyAvailabilityDaysperWeek = "-"
            }

        } else if (binding.runningWaterRadioGroup.checkedRadioButtonId == binding.runningWaterNoCheckbox.id) {
            //for no option 1 Checkboxes group for water options
            val otherWaterSource: String = if (binding.otherCheckboxWaterSource.isChecked) {
                StringUtils.getValue(binding.textInputOtherSourcesOfWater.text.toString())
            } else {
                "-"
            }
            //for water sources
            householdSurveyModel.primarySourceOfRunningWater = StringUtils.getSelectedCheckboxes(
                binding.primarySourceOfWaterCheckboxLinearLayout,
                SessionManager(requireActivity()).appLanguage,
                context,
                otherWaterSource
            )
            //for water distance
            val distance =
                StringUtils.getValue(binding.textInputWaterSourceDistance.text.toString()) + " " +
                        if (binding.rgWaterSourceDistance.checkedRadioButtonId == binding.rbDistanceMeter.id) {
                            getWaterSourceDistance(
                                binding.rbDistanceMeter.text.toString(),
                                requireContext(),
                                SessionManager(requireActivity()).appLanguage
                            )
                        } else {
                            getWaterSourceDistance(
                                binding.rbDistanceKMeter.text.toString(),
                                requireContext(),
                                SessionManager(requireActivity()).appLanguage
                            )
                        }
            householdSurveyModel.waterSourceDistance = distance


        }


    }

    private fun setDataForRunningWaterOptions() {
        val waterStatus = householdSurveyModel.runningWaterStatus
        if (!waterStatus.isNullOrEmpty()) {
            householdSurveyModel.runningWaterStatus?.let {
                when {
                    it.equals("Yes", ignoreCase = true) -> {
                        binding.runningWaterYesCheckbox.isChecked = true
                        waterStatus.let {
                            if (it.equals(
                                    updatedContext.getString(R.string.yes),
                                    ignoreCase = true
                                )
                            ) {
                                binding.runningWaterYesCheckbox.isChecked = true
                                if (!householdSurveyModel.waterSupplyAvailabilityHrsPerDay.isNullOrEmpty() && !householdSurveyModel.waterSupplyAvailabilityHrsPerDay.equals("-"))
                                    binding.textInputWaterSupplyAvailabilityHrsPerDay.setText(householdSurveyModel.waterSupplyAvailabilityHrsPerDay)

                                if (!householdSurveyModel.waterSupplyAvailabilityDaysperWeek.isNullOrEmpty() && !householdSurveyModel.waterSupplyAvailabilityDaysperWeek.equals("-"))
                                    binding.textInputWaterSupplyAvailabilityDaysPerWeek.setText(householdSurveyModel.waterSupplyAvailabilityDaysperWeek)

                            } else if (it.equals(
                                    updatedContext.getString(R.string.no),
                                    ignoreCase = true
                                )
                            ) {
                                binding.runningWaterNoCheckbox.isChecked = true
                            }
                        }

                    }

                    it.equals("No", ignoreCase = true) -> {
                        binding.runningWaterNoCheckbox.isChecked = true

                        //checkboxes layout -water sources
                        setDataToNoNoOptionForWaterStatus()
                    }

                    else -> {
                        binding.runningWaterYesCheckbox.isChecked = false
                        binding.runningWaterNoCheckbox.isChecked = false
                    }
                }
            }
            //from db


        }

    }

    private fun setDataToNoNoOptionForWaterStatus() {
        val waterSources = householdSurveyModel.primarySourceOfRunningWater
        if (!waterSources.isNullOrEmpty()) {
            try {
                val jsonArray = JSONArray(waterSources)
                if (jsonArray.length() > 0) {
                    fun setCheckboxState(checkbox: CheckBox, stringResId: Int) {
                        checkbox.isChecked =
                            waterSources.contains(
                                requireActivity().getString(
                                    stringResId
                                )
                            ) == true
                    }
                    setCheckboxState(
                        binding.villageTankCheckbox,
                        R.string.village_tank
                    )
                    setCheckboxState(
                        binding.openWellCheckbox,
                        R.string.open_well
                    )
                    setCheckboxState(
                        binding.handPumpCheckbox,
                        R.string.hand_pump_checkbox
                    )
                    setCheckboxState(
                        binding.boreWellCheckbox,
                        R.string.bore_well
                    )
                    setCheckboxState(
                        binding.riverCheckbox,
                        R.string.river
                    )
                    setCheckboxState(
                        binding.pondCheckbox,
                        R.string.pond
                    )
                    setCheckboxState(binding.otherCheckboxWaterSource, R.string.other)

                    if (waterSources.contains(requireActivity().getString(R.string.other))) {
                        binding.otherCheckboxWaterSource.isChecked = true

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
                            val jsonArray = JSONArray(waterSources)
                            val otherWaterSource = (0 until jsonArray.length())
                                .asSequence()
                                .map { jsonArray.getString(it) }
                                .firstOrNull { it.contains(tempContext.getString(R.string.other)) }
                                ?.substringAfter(": ")
                                ?.takeIf { it != "-" }

                            binding.textInputOtherSourcesOfWater.setText(otherWaterSource ?: "")
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    } else {
                        binding.otherCheckboxWaterSource.isChecked = false
                    }
                }
            } catch (e: Exception) {
            }
        }

        //2 Distance to the source
        val waterSourceDistance = householdSurveyModel.waterSourceDistance
        if (!waterSourceDistance.isNullOrEmpty() && !waterSourceDistance.equals(
                "No water source distance",
                ignoreCase = true
            ) && waterSourceDistance != "-"
        ) {

            val splitString = waterSourceDistance.split(" ").toMutableList()
            splitString[1] = getWaterSourceDistanceEdit(
                splitString[1],
                requireContext(),
                SessionManager(requireActivity()).appLanguage
            )

            if (splitString[1].equals(getString(R.string.meter), ignoreCase = true)) {
                binding.rbDistanceKMeter.isChecked = true
            } else {
                binding.rbDistanceKMeter.isChecked = true
            }

            binding.textInputWaterSourceDistance.setText(splitString[0])
        }
    }

    private fun setupBankStatusRadioButtons() {
        binding.rgBankAccount.checkedRadioButtonId.let { checkedId ->
            householdSurveyModel.householdBankAccountStatus = if (checkedId != -1) {
                if (checkedId == binding.rbBankAccountYes.id) {
                    StringUtils.getPreTerm(
                        binding.rbBankAccountYes.text.toString(),
                        SessionManager(requireActivity()).appLanguage
                    )
                } else {
                    StringUtils.getPreTerm(
                        binding.rbBankAccountNo.text.toString(),
                        SessionManager(requireActivity()).appLanguage
                    )
                }
            } else {
               "-"
            }
        }

    }

    private fun setDataToBankStatusRadio() {
        val value1 = householdSurveyModel.householdBankAccountStatus
        when {
            value1 != null && value1.equals(
                updatedContext.getString(R.string.yes),
                ignoreCase = true
            ) ->
                binding.rbBankAccountYes.isChecked = true

            value1 != null && value1.equals(
                updatedContext.getString(R.string.no),
                ignoreCase = true
            ) ->
                binding.rbBankAccountNo.isChecked = true
        }


    }
    private fun setWaterSourceDistance() {
        householdSurveyModel.waterSourceDistance?.let {
            val splitString = it.split(" ")
            binding.textInputWaterSourceDistance.setText(splitString[0])

            when (splitString.getOrNull(1)?.lowercase()) {
                "meter" -> binding.rbDistanceMeter.isChecked = true
                "km" -> binding.rbDistanceKMeter.isChecked = true
                else -> {
                binding.rbDistanceMeter.isChecked = false
                binding.rbDistanceKMeter.isChecked = false
            }
            }
        }
    }
}