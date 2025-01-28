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
import org.intelehealth.app.databinding.FragmentFifthHouseholdSurveyBinding
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.ui.filter.FirstLetterUpperCaseInputFilter
import org.intelehealth.app.utilities.HouseholdSurveyStage
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.StringUtils
import org.intelehealth.app.utilities.StringUtils.getWaterSourceDistanceEdit
import org.intelehealth.app.utilities.extensions.addFilter
import org.json.JSONArray
import org.json.JSONException
import java.util.Calendar
import java.util.Locale

class FifthFragment : BaseHouseholdSurveyFragment(R.layout.fragment_fifth_household_survey) {

    private lateinit var binding: FragmentFifthHouseholdSurveyBinding
    var selectedDate = Calendar.getInstance().timeInMillis
    private var patientUuid: String? = null
    private lateinit var updatedContext: Context


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFifthHouseholdSurveyBinding.bind(view)
        houseHoldViewModel.updatePatientStage(HouseholdSurveyStage.FIFTH_SCREEN)
        initViews()
    }

    private fun setClickListener() {
        binding.frag2BtnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.frag2BtnNext.setOnClickListener {
            savePatient()
        }
        binding.otherCheckbox.setOnCheckedChangeListener { _, isChecked ->
            binding.llOtherSourcesOfFuelLayout.visibility =
                if (isChecked) View.VISIBLE else View.GONE
        }
        binding.otherSourceOfLightingCheckbox.setOnCheckedChangeListener { _, isChecked ->
            binding.llOtherSourcesOfLightingLayout.visibility =
                if (isChecked) View.VISIBLE else View.GONE
        }
        binding.otherSourceOfWaterCheckbox.setOnCheckedChangeListener { _, isChecked ->
            binding.llOtherSourcesOfDrinkingWaterLayout.visibility =
                if (isChecked) View.VISIBLE else View.GONE
        }
        binding.otherWaysOfPurifyingWaterCheckbox.setOnCheckedChangeListener { _, isChecked ->
            binding.llotherWaysOfPurifyingWaterEditText.visibility =
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

    private fun savePatient() {
        householdSurveyModel.apply {
            setupForMainLightingSource()
            setupForWaterPurification()
            setupForDrinkingWaterSource()
            setupForFuelForCooking()
            setupForHouseholdToiletFacility()

            Log.d("devchdbsave4", "savePatient: householdSurveyModel : " + householdSurveyModel)
            houseHoldViewModel.updatedPatient(this)
            val patient = PatientDTO()
            patient.uuid = patientUuid
            saveAndNavigateToDetails(patient, householdSurveyModel)
        }
    }

    private fun saveAndNavigateToDetails(
        patient: PatientDTO, householdSurveyModel: HouseholdSurveyModel
    ) {
        houseHoldViewModel.savePatient(
            "fifthScreen", patient, householdSurveyModel
        ).observe(viewLifecycleOwner) {
            it ?: return@observe
            houseHoldViewModel.handleResponse(it) { result -> if (result) navigateToDetails() }
        }
    }

    private fun navigateToDetails() {
        FifthFragmentDirections.actionFiveToSix().apply {
            findNavController().navigate(this)
        }
    }

    private fun setupForFuelForCooking() {
        var otherCookingFuel: String = if (binding.otherCheckbox.isChecked) {
            StringUtils.getValue(binding.textInputOtherSourcesOfFuelLayout.text.toString())
        } else {
            "-"
        }
        householdSurveyModel.cookingFuelType = StringUtils.getSelectedCheckboxes(
            binding.householdCookingFuelCheckboxLinearLayout,
            SessionManager(requireActivity()).appLanguage,
            context,
            otherCookingFuel
        )
    }

    private fun setDataForFuelForCookingUI() {
        val fuelForCooking = householdSurveyModel.cookingFuelType
        fun setCheckboxState(checkbox: CheckBox, stringResId: Int) {
            checkbox.isChecked =
                fuelForCooking?.contains(updatedContext.getString(stringResId)) == true
        }
        // Set states for various checkboxes
        setCheckboxState(binding.electricityCheckbox, R.string.electricity)
        setCheckboxState(binding.lpgNaturalGasCheckbox, R.string.lpg_natural_gas)
        setCheckboxState(binding.biogasCheckbox, R.string.biogas_checkbox)
        setCheckboxState(binding.keroseneCheckbox, R.string.kerosene)
        setCheckboxState(binding.coalCheckbox, R.string.coal_lignite)
        setCheckboxState(binding.woodCheckbox, R.string.wood)
        setCheckboxState(binding.charcoalCheckbox, R.string.charcoal)
        setCheckboxState(binding.strawShrubsGrassCheckbox, R.string.straw_shrubs_grass)
        setCheckboxState(binding.agriculturalCropWasteCheckbox, R.string.agricultural_crop_waste)
        setCheckboxState(binding.dungCakesCheckbox, R.string.dung_cakes)

        // Handle "Other" checkbox with additional logic
        if (fuelForCooking?.contains(updatedContext.getString(R.string.other_specify)) == true) {
            setCheckboxState(binding.otherCheckbox, R.string.other_specify)

            val tempContext =
                if (SessionManager(requireActivity()).appLanguage.equals("mr", ignoreCase = true)) {
                    val config =
                        Configuration(IntelehealthApplication.getAppContext().resources.configuration)
                    config.setLocale(Locale("en"))
                    requireContext().createConfigurationContext(config)
                } else {
                    requireContext()
                }

            try {
                val jsonArray = JSONArray(fuelForCooking)
                val otherSourceOfFuel =
                    (0 until jsonArray.length()).asSequence().map { jsonArray.getString(it) }
                        .firstOrNull { it.contains(tempContext.getString(R.string.other_specify)) }
                        ?.substringAfter(": ")?.takeIf { it != "-" }

                otherSourceOfFuel?.let {
                    binding.textInputOtherSourcesOfFuelLayout.setText(it)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        } else {
            binding.otherCheckbox.isChecked = false
        }
    }

    private fun setupForMainLightingSource() {
        var otherMainLightingSource: String = if (binding.otherSourceOfLightingCheckbox.isChecked) {
            StringUtils.getValue(binding.textInputOtherSourcesOfLightingLayout.text.toString())
        } else {
            "-"
        }
        householdSurveyModel.mainLightingSource = StringUtils.getSelectedCheckboxes(
            binding.mainSourceOfLightingCheckboxLinearLayout,
            SessionManager(requireActivity()).appLanguage,
            context,
            otherMainLightingSource
        )
    }

    private fun setDataForMainLightingSourceUI() {
        val mainLightingSource = householdSurveyModel.mainLightingSource
        if (!mainLightingSource.isNullOrEmpty()) {
            fun setCheckboxState(checkbox: CheckBox, stringResId: Int) {
                checkbox.isChecked =
                    mainLightingSource.contains(requireActivity().getString(stringResId))
            }

            setCheckboxState(binding.lanternCheckbox, R.string.lantern)
            setCheckboxState(binding.keroseneLampCheckbox, R.string.kerosene_lamp)
            setCheckboxState(binding.candleCheckbox, R.string.candle)
            setCheckboxState(binding.electricCheckbox, R.string.electric)
            setCheckboxState(binding.lpgCheckbox, R.string.lpg)
            setCheckboxState(binding.solarEnergyCheckbox, R.string.solar_energy)
            setCheckboxState(binding.noneCheckbox, R.string.none)

            if (mainLightingSource.contains(requireActivity().getString(R.string.other_specify))) {
                binding.otherSourceOfLightingCheckbox.isChecked = true

                val tempContext = if (SessionManager(requireActivity()).appLanguage.equals(
                        "mr", ignoreCase = true
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
                    val jsonArray = JSONArray(mainLightingSource)
                    val otherSourceOfLighting =
                        (0 until jsonArray.length()).asSequence().map { jsonArray.getString(it) }
                            .firstOrNull {
                                it.contains(tempContext.getString(R.string.other_specify)) && it != "-"
                            }?.substringAfter(": ")?.takeIf { it.isNotBlank() }

                    binding.textInputOtherSourcesOfLightingLayout.setText(
                        otherSourceOfLighting ?: ""
                    )
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            } else {
                binding.otherSourceOfLightingCheckbox.isChecked = false
            }
        }
    }

    private fun setupForDrinkingWaterSource() {
        var otherDrinkingWaterSource: String = if (binding.otherSourceOfWaterCheckbox.isChecked) {
            StringUtils.getValue(binding.textInputOtherSourcesDrinkingWater.text.toString())
        } else {
            "-"
        }
        householdSurveyModel.mainDrinkingWaterSource = StringUtils.getSelectedCheckboxes(
            binding.mainSourceOfDrinkingWaterCheckboxLinearLayout,
            SessionManager(requireActivity()).appLanguage,
            context,
            otherDrinkingWaterSource
        )
    }

    private fun setDataForDrinkingWaterSourceUI() {
        val mainDrinkingWaterSource = householdSurveyModel.mainDrinkingWaterSource
        if (!mainDrinkingWaterSource.isNullOrEmpty()) {
            // Helper function to set checkbox state
            fun setCheckboxState(checkbox: CheckBox, stringResId: Int) {
                checkbox.isChecked =
                    mainDrinkingWaterSource.contains(requireActivity().getString(stringResId))
            }

            // Setting checkbox states for all drinking water sources
            setCheckboxState(binding.pipedIntoDwellingCheckbox, R.string.piped_into_dwelling)
            setCheckboxState(binding.pipedIntoYardPlotCheckbox, R.string.piped_into_yard_plot)
            setCheckboxState(binding.publicTapStandpipeCheckbox, R.string.public_tap_standpipe)
            setCheckboxState(binding.tubeWellBoreholeCheckbox, R.string.tube_well_borehole)
            setCheckboxState(binding.protectedWellCheckbox, R.string.protected_well_checkbox)
            setCheckboxState(binding.unprotectedWellCheckbox, R.string.unprotected_well)
            setCheckboxState(binding.protectedSpringCheckbox, R.string.protected_spring)
            setCheckboxState(binding.unprotectedSpringCheckbox, R.string.unprotected_spring)
            setCheckboxState(binding.rainwaterCheckbox, R.string.rainwater)
            setCheckboxState(binding.tankerTruckCheckbox, R.string.tanker_truck)
            setCheckboxState(binding.cartWithSmallTankCheckbox, R.string.cart_with_small_tank)
            setCheckboxState(binding.surfaceWaterCheckbox, R.string.surface_water)
            setCheckboxState(binding.commonHandPumpCheckbox, R.string.common_hand_pump)
            setCheckboxState(binding.handPumpAtHomeCheckbox, R.string.hand_pump_at_home)

            if (mainDrinkingWaterSource.contains(requireActivity().getString(R.string.other_specify))) {
                binding.otherSourceOfWaterCheckbox.isChecked = true

                val tempContext = if (SessionManager(requireActivity()).appLanguage.equals(
                        "mr", ignoreCase = true
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
                    val jsonArray = JSONArray(mainDrinkingWaterSource)
                    val otherSourceOfWater =
                        (0 until jsonArray.length()).asSequence().map { jsonArray.getString(it) }
                            .firstOrNull {
                                it.contains(tempContext.getString(R.string.other_specify)) && it != "-"
                            }?.substringAfter(": ")?.takeIf { it.isNotBlank() }

                    binding.textInputOtherSourcesDrinkingWater.setText(otherSourceOfWater ?: "")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            } else {
                binding.otherSourceOfWaterCheckbox.isChecked = false
            }
        }
    }

    private fun setupForWaterPurification() {
        var otherWaterPurificationSource: String =
            if (binding.otherWaysOfPurifyingWaterCheckbox.isChecked) {
                StringUtils.getValue(binding.inputOtherPurificationMethod.text.toString())
            } else {
                "-"
            }

        householdSurveyModel.saferWaterProcess = StringUtils.getSelectedCheckboxes(
            binding.llSaferWaterPurificationCheckBox,
            SessionManager(requireActivity()).appLanguage,
            context,
            otherWaterPurificationSource
        )
    }

    private fun setDataForWaterPurification() {
        val saferWaterProcess = householdSurveyModel.saferWaterProcess

        saferWaterProcess?.let {
            fun setCheckboxState(checkbox: CheckBox, stringResId: Int) {
                checkbox.isChecked = it.contains(requireContext().getString(stringResId))
            }

            setCheckboxState(binding.boilCheckbox, R.string.boil)
            setCheckboxState(binding.useAlumCheckbox, R.string.use_alum)
            setCheckboxState(
                binding.addBleachChlorineTabletsDropsCheckbox,
                R.string.add_bleach_chlorine_tablets_drops
            )
            setCheckboxState(binding.strainThroughAClothCheckbox, R.string.strain_through_a_cloth)
            setCheckboxState(
                binding.useWaterFilterCheckbox, R.string.use_water_filter_ceramic_sand_composite_etc
            )
            setCheckboxState(
                binding.useElectronicPurifierCheckbox, R.string.use_electronic_purifier
            )
            setCheckboxState(
                binding.otherSourceOfWaterNoMeasuresTakenForPurificationDrinkingAsItIs,
                R.string.no_measures_taken_for_purification_drinking_as_it_is
            )
            setCheckboxState(binding.letItStandAndSettleCheckbox, R.string.let_it_stand_and_settle)
            setCheckboxState(binding.notTreatedCheckbox, R.string.not_treated)

            if (it.contains(requireContext().getString(R.string.other_specify))) {
                binding.otherWaysOfPurifyingWaterCheckbox.isChecked = true

                val tempContext = if (SessionManager(requireActivity()).appLanguage.equals(
                        "mr", ignoreCase = true
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
                    val jsonArray = JSONArray(it)
                    val otherSourceOfPurifyingWater =
                        (0 until jsonArray.length()).asSequence().map { jsonArray.getString(it) }
                            .firstOrNull { it.contains(tempContext.getString(R.string.other_specify)) && it != "-" }
                            ?.substringAfter(": ")?.takeIf { it.isNotBlank() }

                    binding.inputOtherPurificationMethod.setText(otherSourceOfPurifyingWater ?: "")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            } else {
                binding.otherWaysOfPurifyingWaterCheckbox.isChecked = false
            }
        }
    }

    private fun setupForHouseholdToiletFacility() {
        householdSurveyModel.householdToiletFacility = StringUtils.getSelectedCheckboxes(
            binding.familyToiletFacilityCheckboxLinearLayout,
            SessionManager(requireActivity()).appLanguage,
            context,
            ""
        )
    }

    private fun setDataForHouseholdToiletFacility() {
        val toiletFacility = householdSurveyModel.householdToiletFacility
        if (!toiletFacility.isNullOrEmpty()) {
            fun setCheckboxState(checkbox: CheckBox, stringResId: Int) {
                checkbox.isChecked =
                    toiletFacility.contains(requireContext().getString(stringResId))
            }
            setCheckboxState(
                binding.flushToPipedSewerSystemCheckbox, R.string.flush_to_piped_sewer_system
            )
            setCheckboxState(binding.flushToSepticTankCheckbox, R.string.flush_to_septic_tank)
            setCheckboxState(binding.flushToPitLatrineCheckbox, R.string.flush_to_pit_latrine)
            setCheckboxState(binding.flushToSomewhereElseCheckbox, R.string.flush_to_somewhere_else)
            setCheckboxState(binding.flushDontKnowWhereCheckbox, R.string.flush_dont_know_where)
            setCheckboxState(
                binding.ventilatedImprovedPitCheckbox,
                R.string.ventilated_improved_pit_biogas_latrine
            )
            setCheckboxState(binding.pitLatrineWithSlabCheckbox, R.string.pit_latrine_with_slab)
            setCheckboxState(
                binding.pitLatrineWithoutSlabCheckbox, R.string.pit_latrine_without_slab_open_pit
            )
            setCheckboxState(
                binding.twinPitCompostingToiletCheckbox, R.string.twin_pit_composting_toilet
            )
            setCheckboxState(binding.dryToiletCheckbox, R.string.dry_toilet)
            setCheckboxState(binding.communalToiletCheckbox, R.string.communal_toilet)
            setCheckboxState(
                binding.noFacilityUsesOpenFieldCheckbox,
                R.string.no_facility_uses_open_space_or_field
            )
        }
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

        setDataForMainLightingSourceUI()
        setDataForDrinkingWaterSourceUI()
        setDataForWaterPurification()
        setDataForFuelForCookingUI()
        setDataForHouseholdToiletFacility()
    }

    override fun onPatientDataLoaded(householdSurveyModel: HouseholdSurveyModel) {
        super.onPatientDataLoaded(householdSurveyModel)
        Timber.d { Gson().toJson(householdSurveyModel) }
        setDataToUI()

        binding.patientSurveyAttributes = householdSurveyModel
        binding.isEditMode = houseHoldViewModel.isEditMode
    }
    private fun applyFilter() {
        binding.textInputOtherSourcesOfFuelLayout.addFilter(FirstLetterUpperCaseInputFilter())
        binding.textInputOtherSourcesOfLightingLayout.addFilter(FirstLetterUpperCaseInputFilter())
        binding.textInputOtherSourcesDrinkingWater.addFilter(FirstLetterUpperCaseInputFilter())
        binding.inputOtherPurificationMethod.addFilter(FirstLetterUpperCaseInputFilter())
    }
}