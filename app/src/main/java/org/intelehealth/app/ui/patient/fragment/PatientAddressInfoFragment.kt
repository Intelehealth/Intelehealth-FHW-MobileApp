package org.intelehealth.app.ui.patient.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.OnRebindCallback
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.github.ajalt.timberkt.Timber
import com.github.ajalt.timberkt.Timber.tag
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.intelehealth.app.BuildConfig
import org.intelehealth.app.R
import org.intelehealth.app.activities.identificationActivity.model.Block
import org.intelehealth.app.activities.identificationActivity.model.DistData
import org.intelehealth.app.activities.identificationActivity.model.ProvincesAndCities
import org.intelehealth.app.activities.identificationActivity.model.StateData
import org.intelehealth.app.activities.identificationActivity.model.Village
import org.intelehealth.app.databinding.FragmentPatientAddressInfoBinding
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.ui.filter.FirstLetterUpperCaseInputFilter
import org.intelehealth.app.ui.patient.activity.PatientRegistrationActivity
import org.intelehealth.app.utilities.ArrayAdapterUtils
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

/**
 * Created by Vaghela Mithun R. on 27-06-2024 - 13:42.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PatientAddressInfoFragment : BasePatientFragment(R.layout.fragment_patient_address_info) {

    private lateinit var binding: FragmentPatientAddressInfoBinding
    private var isCityVillageEnabled: Boolean = false
    private var isOtherBlockSelected: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentPatientAddressInfoBinding.bind(view)
        binding.textInputLayDistrict.isEnabled = false
        patientViewModel.updatePatientStage(PatientRegStage.ADDRESS)
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setupCountries() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.countries)
        binding.autoCompleteCountry.setAdapter(adapter)
        if (patient.country != null && patient.country.isNotEmpty()) {
//            binding.autoCompleteCountry.setSelection(adapter.getPosition(patient.country))
            binding.autoCompleteCountry.setText(patient.country, false)
        } else {
            val defaultValue = getString(R.string.default_country)
            Timber.d { "default $defaultValue index[${adapter.getPosition(defaultValue)}]" }
//            binding.autoCompleteCountry.setSelection(adapter.getPosition(defaultValue))
            binding.autoCompleteCountry.setText(defaultValue, false)
            LanguageUtils.getSpecificLocalResource(requireContext(), "en").apply {
                patient.country = this.getString(R.string.default_country)
            }
        }
        binding.textInputLayCountry.isEnabled = false
        binding.autoCompleteCountry.setOnItemClickListener { _, _, i, _ ->
            binding.textInputLayCountry.hideError()
            LanguageUtils.getSpecificLocalResource(requireContext(), "en").apply {
                patient.country = this.getStringArray(R.array.countries)[i]
            }
        }
    }


    override fun onPatientDataLoaded(patient: PatientDTO) {
        super.onPatientDataLoaded(patient)
        Timber.d { "onPatientDataLoaded" }
        Timber.d { Gson().toJson(patient) }
        binding.patient = patient
        binding.isEditMode = patientViewModel.isEditMode
        fetchPersonalInfoConfig()
    }

    private fun fetchPersonalInfoConfig() {
        patientViewModel.fetchAddressRegFields().observe(viewLifecycleOwner) {
            binding.addressInfoConfig = PatientRegFieldsUtils.buildPatientAddressInfoConfig(it)
            Timber.d { "Address Config => ${Gson().toJson(binding.addressInfoConfig)}" }
            binding.addOnRebindCallback(onRebindCallback)
        }
    }

    private fun setClickListener() {
        binding.frag2BtnBack.setOnClickListener {
            setOtherBlockData()
            findNavController().popBackStack()
        }
        binding.frag2BtnNext.setOnClickListener {
            setOtherBlockData()
            validateForm { savePatient() }
        }
    }

    private fun savePatient() {
        patient.apply {
            postalcode = binding.textInputPostalCode.text?.toString()
            /* val village = binding.textInputCityVillage.text?.toString()
             cityvillage = if (district.isNullOrEmpty().not()) "${district}:$village"
             else village*/
            address2 = binding.textInputAddress2.text?.toString()
            registrationAddressOfHf = binding.textInputRegistrationAddressOfHf.text?.toString()

            //householdNumber = binding.textInputHouseholdNumber.text?.toString()
             address1 = binding.textInputAddress1.text?.toString()
            address6 = binding.textInputHouseholdNumber.text?.toString()

            var village: String
            if (binding.llBlock.isEnabled) {
                if (binding.autoCompleteBlock.text.contains("Other", ignoreCase = true)) {
                    address3 = binding.textInputOtherBlock.text.toString()
                    village = binding.textInputCityVillage.text?.toString().toString()
                } else {
                    village = binding.autoCompleteVillageDropdown.text.toString()
                    address3 = binding.autoCompleteBlock.text.toString()
                }
            } else {
                village = binding.textInputCityVillage.text.toString()
            }
            /*cityvillage = if (district.isNullOrEmpty().not()) "${district}:$village"
            else village*/
            cityvillage = village

            patientViewModel.updatedPatient(this)
            if (patientViewModel.isEditMode) {
                saveAndNavigateToDetails()
            } else {
                if (patientViewModel.activeStatusOtherSection.not()) {
                    saveAndNavigateToDetails()
                } else {
                    PatientAddressInfoFragmentDirections.navigationAddressToOther().apply {
                        findNavController().navigate(this)
                    }
                }
            }
        }
    }

    private fun saveAndNavigateToDetails() {
        patientViewModel.savePatient().observe(viewLifecycleOwner) {
            it ?: return@observe
            patientViewModel.handleResponse(it) { result -> if (result) navigateToDetails() }
        }
    }

    private fun navigateToDetails() {
        PatientAddressInfoFragmentDirections.navigationAddressToDetails(
            patient.uuid, "searchPatient", "false"
        ).apply {
            findNavController().navigate(this)
            requireActivity().finish()
        }
    }

    private fun applyFilter() {
        binding.textInputCityVillage.addFilter(FirstLetterUpperCaseInputFilter())
        binding.textInputAddress1.addFilter(FirstLetterUpperCaseInputFilter())
        binding.textInputAddress2.addFilter(FirstLetterUpperCaseInputFilter())
        binding.textInputOtherBlock.addFilter(FirstLetterUpperCaseInputFilter())
    }

    private fun setInputTextChangListener() {
        binding.textInputLayCityVillage.hideErrorOnTextChang(binding.textInputCityVillage)
        binding.textInputLayAddress1.hideErrorOnTextChang(binding.textInputAddress1)
        binding.textInputLayAddress2.hideErrorOnTextChang(binding.textInputAddress2)
        binding.textInputLayRegistrationAddressOfHf.hideErrorOnTextChang(binding.textInputRegistrationAddressOfHf)

        binding.textInputLayPostalCode.hideDigitErrorOnTextChang(binding.textInputPostalCode, 6)
        binding.textInputLayOtherBlock.hideErrorOnTextChang(binding.textInputOtherBlock)
        binding.textInputLayHouseholdNumber.hideErrorOnTextChang(binding.textInputHouseholdNumber)
    }

    private fun setupStates() {
        val isConfigStateEditable = binding.addressInfoConfig?.state?.isEditable ?: true
        val defaultState = LanguageUtils.getState(getString(R.string.default_state))

        //binding.autoCompleteDistrict.setAdapter(null)

        if (!isConfigStateEditable && defaultState != null && defaultState.state.isNotEmpty()) {
            /*  For Project with default state values like NAS ::  1 !isConfigStateEditable - state disabled from admin panel
               2 defaultState - project has default state :: if above both conditions true then it has default value and make it editable false*/
            setFieldEnabledStatus(binding.textInputLayState, false)
            binding.autoCompleteState.setText(defaultState.toString(), false)
            patient.stateprovince = binding.autoCompleteState.text.toString()
            setupDistricts(defaultState)
        } else {
            binding.autoCompleteState.setText("", false)
            // patient.stateprovince = binding.autoCompleteState.text.toString()
            // IDA flow (No default value for state
            setFieldEnabledStatus(binding.textInputLayState, isConfigStateEditable)
            /*binding.autoCompleteState.setText("", false)
                patient.stateprovince = binding.autoCompleteState.text.toString()*/

            LanguageUtils.getStateList()?.let {
                binding.textInputLayState.tag = it
                val adapter: ArrayAdapter<StateData> = ArrayAdapterUtils.getObjectArrayAdapter(
                    requireContext(), it
                )
                binding.autoCompleteState.setAdapter(adapter)
                if (patient.stateprovince != null && patient.stateprovince.isNotEmpty()) {
                    val state = LanguageUtils.getState(patient.stateprovince)
                    if (state != null) {
                        binding.autoCompleteState.setText(state.toString(), false)
                        setupDistricts(state)
                    }
                }
                binding.autoCompleteState.setOnItemClickListener { adapterView, _, i, _ ->
                    binding.textInputLayState.hideError()
                    eraseAllBlockFields()
                    val list: List<StateData> = binding.textInputLayState.tag as List<StateData>
                    val selectedState = list[i]
                    binding.autoCompleteDistrict.setText("", false)
                    patient.district = binding.autoCompleteDistrict.text.toString()
                    patient.stateprovince = selectedState.state
                    setupDistricts(selectedState)
                }
            }

        }
    }

    private fun setupProvinceAndCities() {
        LanguageUtils.getProvincesAndCities().let {
            //province
            binding.textInputLayProvince.tag = it
            val adapter: ArrayAdapter<String> = ArrayAdapterUtils.getObjectArrayAdapter(
                requireContext(), it.provinces
            )
            binding.autoCompleteProvince.setAdapter(adapter)

            if (patient.province != null && patient.province.isNotEmpty()) {
                val province = LanguageUtils.getProvince(patient.province)
                if (province != null) {
                    binding.autoCompleteProvince.setText(province.toString(), false)
                }
            }

            binding.autoCompleteProvince.setOnItemClickListener { adapterView, _, i, _ ->
                binding.textInputLayProvince.hideError()
                val provincesAndCities: ProvincesAndCities =
                    binding.textInputLayProvince.tag as ProvincesAndCities
                patient.province = provincesAndCities.provinces[i]
            }

            //cities
            binding.textInputLayCity.tag = it
            val cityAdapter: ArrayAdapter<String> = ArrayAdapterUtils.getObjectArrayAdapter(
                requireContext(), it.cities
            )
            binding.autoCompleteCity.setAdapter(cityAdapter)

            if (patient.city != null && patient.city.isNotEmpty()) {
                val city = LanguageUtils.getCity(patient.city)
                if (city != null) {
                    binding.autoCompleteCity.setText(city.toString(), false)
                }
            }

            binding.autoCompleteCity.setOnItemClickListener { adapterView, _, i, _ ->
                binding.textInputLayCity.hideError()
                val provincesAndCities: ProvincesAndCities =
                    binding.textInputLayCity.tag as ProvincesAndCities
                patient.city = provincesAndCities.cities[i]
            }
        }

    }

    private fun setupDistricts(stateData: StateData) {

        val isConfigDistrictEditable = binding.addressInfoConfig?.district?.isEditable ?: true
        val defaultDistrict =
            LanguageUtils.getDistrict(stateData, getString(R.string.default_district))

        if (!isConfigDistrictEditable && defaultDistrict != null && defaultDistrict.name.isNotEmpty()) {
            /*  For Project with default state values like NAS ::  1 !isConfigDistrictEditable - state disabled from admin panel
               2 defaultDistrict - project has default state :: if above both conditions true then it has default value and make it editable false*/
            setFieldEnabledStatus(binding.textInputLayDistrict, false)
            binding.autoCompleteDistrict.setText(defaultDistrict.toString(), false)
            patient.district = binding.autoCompleteDistrict.text.toString()
            if (binding.llBlock.isEnabled) setupBlocks(defaultDistrict)
        } else {
            // IDA flow (No default value for state
            setFieldEnabledStatus(binding.textInputLayDistrict, isConfigDistrictEditable)
            val adapter: ArrayAdapter<DistData> = ArrayAdapterUtils.getObjectArrayAdapter(
                requireContext(), stateData.distDataList
            )
            binding.autoCompleteDistrict.setAdapter(adapter)

            if (patient.district != null && patient.district.isNotEmpty()) {
                val selected = LanguageUtils.getDistrict(stateData, patient.district)
                if (selected != null) {
                    binding.autoCompleteDistrict.setText(selected.toString(), false)
                    if (binding.llBlock.isEnabled) setupBlocks(selected)
                }
            }

            binding.textInputLayDistrict.tag = stateData.distDataList
            binding.autoCompleteDistrict.setOnItemClickListener { adapterView, _, i, _ ->
                binding.textInputLayDistrict.hideError()
                val dList: List<DistData> = binding.textInputLayDistrict.tag as List<DistData>
                patient.district = dList[i].name
                if (binding.llBlock.isEnabled) setupBlocks(dList[i])
            }
        }
    }

    private fun validateForm(block: () -> Unit) {
        Timber.d { "Final patient =>${Gson().toJson(patient)}" }
        val error = R.string.this_field_is_mandatory
        binding.addressInfoConfig?.let {
            val bPostalCode = if (it.postalCode?.isEnabled == true && it.postalCode?.isMandatory == true) {
                binding.textInputLayPostalCode.validate(binding.textInputPostalCode, error).and(
                    binding.textInputLayPostalCode.validateDigit(
                        binding.textInputPostalCode, R.string.postal_code_6_dig_invalid_txt, 6
                    )
                )

            } else true


            val bCountry = if (it.country?.isEnabled == true && it.country?.isMandatory == true) {
                binding.textInputLayCountry.validateDropDowb(
                    binding.autoCompleteCountry, error
                )
            } else true

            val bState = if (it.state?.isEnabled == true && it.state?.isMandatory == true) {
                binding.textInputLayState.validateDropDowb(
                    binding.autoCompleteState, error
                )
            } else true

            val bDistrict = if (it.district?.isEnabled == true && it.district?.isMandatory == true) {
                binding.textInputLayDistrict.validateDropDowb(
                    binding.autoCompleteState, error
                )
            } else true

            val bCityVillage =
                if (it.cityVillage?.isEnabled == true && it.cityVillage?.isMandatory == true && !it.block!!.isEnabled) {
                    binding.textInputLayCityVillage.validate(binding.textInputCityVillage, error)
                        .and(
                            binding.textInputLayCityVillage.validateDigit(
                                binding.textInputCityVillage,
                                R.string.error_field_valid_village_required,
                                3
                            )
                        )
                } else true

            val bProvince = if (it.province?.isEnabled == true && it.province?.isMandatory == true) {
                binding.textInputLayProvince.validateDropDowb(
                    binding.autoCompleteProvince,
                    error
                )
            } else true

            val bCity = if (it.city?.isEnabled == true && it.city?.isMandatory == true) {
                binding.textInputLayCity.validateDropDowb(
                    binding.autoCompleteCity,
                    error
                )
            } else true

            val bRelativeAddressOfHf =
                if (it.registrationAddressOfHf?.isEnabled == true && it.registrationAddressOfHf?.isMandatory == true) {
                    binding.textInputLayRegistrationAddressOfHf.validate(
                        binding.textInputRegistrationAddressOfHf,
                        R.string.error_field_required,
                    )
                } else true


            val bAddress1 = if (it.address1?.isEnabled == true && it.address1?.isMandatory == true) {
                binding.textInputLayAddress1.validate(binding.textInputAddress1, error)
            } else true

            val bAddress2 = if (it.address2?.isEnabled == true && it.address2?.isMandatory == true) {
                binding.textInputLayAddress2.validate(binding.textInputAddress2, error)
            } else true

            val bBlock =if (it.block?.isEnabled == true && it.block?.isMandatory == true) {
                binding.textInputLayBlock.validateDropDowb(
                    binding.autoCompleteBlock, error
                )
            } else true

            val bVillageField = if (it.block?.isEnabled == true && it.block?.isMandatory == true) {
                if (binding.autoCompleteBlock.text.toString()
                        .contains("Other", ignoreCase = true)
                ) {
                    binding.textInputLayOtherBlock.validate(binding.textInputOtherBlock, error)
                    binding.textInputLayCityVillage.validate(binding.textInputCityVillage, error)
                } else {
                    binding.textInputLayVillageDropdown.validateDropDowb(
                        binding.autoCompleteVillageDropdown, error
                    )
                }
            } else true

            val bHouseholdNumber =
                if (it.householdNumber?.isEnabled == true && it.householdNumber?.isMandatory == true) {
                    binding.textInputLayHouseholdNumber.validate(
                        binding.textInputHouseholdNumber, error
                    )
                } else true

            if (bPostalCode.and(bCountry).and(bState).and(bDistrict).and(bCityVillage)
                    .and(bAddress1).and(bAddress2).and(bProvince).and(bCity)
                    .and(bRelativeAddressOfHf)
                    .and(bAddress1).and(bAddress2).and(bBlock).and(bVillageField)
                    .and(bHouseholdNumber)
            ) block.invoke()
        }
    }

    private fun setFieldEnabledStatus(view: View, isEnabled: Boolean) {
        view.isEnabled = isEnabled
    }

    private fun setupBlocks(districtData: DistData) {
        if (districtData.blocks != null) {
            val adapter: ArrayAdapter<Block> = ArrayAdapterUtils.getObjectArrayAdapter(
                requireContext(), districtData.blocks
            )
            binding.autoCompleteBlock.setAdapter(adapter)
            binding.textInputLayBlock.tag = districtData.blocks
            if (patient.address3 != null && patient.address3.isNotEmpty()) {
                val selected = LanguageUtils.getBlock(districtData, patient.address3)
                if (selected == null) {
                    val selected = LanguageUtils.getBlock(districtData, "Other Block")
                    binding.autoCompleteBlock.setText(selected.toString(), false)
                    binding.textInputOtherBlock.setText(patient.address3)
                    enableOtherBlock()
                    isOtherBlockSelected = true;
                } else {
                    isOtherBlockSelected = false;
                    binding.autoCompleteBlock.setText(selected.toString(), false)
                    disableOtherBlock()
                    setupVillages(selected)
                }
            }

            binding.autoCompleteBlock.setOnItemClickListener { adapterView, _, i, _ ->
                binding.autoCompleteVillageDropdown.setText("", false)
                //patient.cityvillage = binding.autoCompleteVillageDropdown.text.toString()
                binding.textInputLayBlock.hideError()

                binding.textInputCityVillage.setText("")
                binding.textInputOtherBlock.setText("")
                binding.autoCompleteVillageDropdown.setText("")

                val blocksList: List<Block> = binding.textInputLayBlock.tag as List<Block>
                val selectedBlock = blocksList[i]
                if (selectedBlock.name?.contains("Other") == true) {
                    binding.textInputLayOtherBlock.visibility = View.VISIBLE
                    enableOtherBlock()
                    binding.textInputCityVillage.setText("")
                    patient.address3 = binding.textInputOtherBlock.text.toString()
                    isOtherBlockSelected = true;
                } else {
                    disableOtherBlock()
                    patient.address3 = blocksList[i].name
                    binding.textInputCityVillage.setText("")
                    isOtherBlockSelected = false;
                }
                setupVillages(selectedBlock)
            }
        } else {
            eraseAllBlockFields()

        }
    }

    private fun setupVillages(blocksData: Block) {
        val villages = mutableListOf<Village>()

        if (patient.villageWithoutDistrict != null && patient.villageWithoutDistrict.isNotEmpty()) {
            val selected = LanguageUtils.getVillage(
                blocksData.gramPanchayats?.get(0),
                patient.villageWithoutDistrict
            )
            if (selected != null) {
                binding.autoCompleteVillageDropdown.setText(selected.toString(), false)
            }
        }

        blocksData.gramPanchayats?.forEach { gramPanchayat ->
            gramPanchayat.villages?.let { villageList ->
                if (villageList.isNotEmpty()) {
                    villages.addAll(villageList)

                    val adapter: ArrayAdapter<Village> =
                        ArrayAdapterUtils.getObjectArrayAdapter(requireContext(), villages)
                    binding.autoCompleteVillageDropdown.setAdapter(adapter)
                    binding.textInputLayVillageDropdown.tag = blocksData.gramPanchayats

                    binding.autoCompleteVillageDropdown.setOnItemClickListener { adapterView, _, i, _ ->
                        binding.textInputLayVillageDropdown.hideError()
                        val selectedVillage = villages[i]
                        if (binding.autoCompleteBlock.text.contains("Other", ignoreCase = true)
                        ) binding.textInputCityVillage.setText("")
                         else patient.cityvillage = selectedVillage.name
                    }
                } else {
                }
            } ?: run {
            }
        }


    }

    private val onRebindCallback = object : OnRebindCallback<FragmentPatientAddressInfoBinding>() {
        override fun onBound(binding: FragmentPatientAddressInfoBinding?) {
            super.onBound(binding)
            /*for NAS corresponding address is not required and address 1
             means household no value thats why disabled the corresponding address 1 field for nas*/

            observeBlockAndVillageChange()
            //  resetAdaptersAndFieldData();
            setupCountries()
            setupStates()
            //setupProvinceAndCities()
            applyFilter()
            setInputTextChangListener()
            setClickListener()
            if(BuildConfig.FLAVOR_client == FlavorKeys.UNFPA){
                setupProvinceAndCities()
            }
        }
    }

    private fun observeBlockAndVillageChange() {
        val isBlockEnabled = (binding.addressInfoConfig?.block?.isEnabled);
        isBlockEnabled?.let {
            patientViewModel.setCityVillageEnabled(
                if (isBlockEnabled == true) false
                else binding.addressInfoConfig?.cityVillage?.isEnabled ?: true
            )
        }
        patientViewModel.addressInfoConfigCityVillageEnabled.observe(
            viewLifecycleOwner
        ) { isEnabled ->
            isCityVillageEnabled = isEnabled

            manageBlockVisibility(isBlockEnabled ?: false)
        }

    }

    private fun resetAdaptersAndFieldData() {
        // Reset adapter and clear text for each AutoCompleteTextView
        binding.autoCompleteState.setAdapter(null)
        binding.autoCompleteState.setText("", false)
        patient.stateprovince = ""

        binding.autoCompleteDistrict.setAdapter(null)
        binding.autoCompleteDistrict.setText("", false)
        patient.district = ""
    }

    private fun enableOtherBlock() {
        binding.textInputLayOtherBlock.visibility = View.VISIBLE
        binding.llCityVillage.visibility = View.VISIBLE
        binding.llVillageDropdown.visibility = View.GONE
        binding.llOtherBlock.visibility = View.VISIBLE
        if (isOtherBlockSelected) {
            binding.lblCityVillage.text =
                getString(R.string.identification_screen_prompt_city) + "*"
        }
    }

    private fun disableOtherBlock() {
        binding.textInputLayOtherBlock.visibility = View.GONE
        binding.llCityVillage.visibility = View.GONE
        binding.llVillageDropdown.visibility = View.VISIBLE
        binding.llOtherBlock.visibility = View.GONE
        //binding.lblCityVillage.text = getString(R.string.identification_screen_prompt_city)
    }

    private fun manageBlockVisibility(isBlockEnabled: Boolean) {
        val address3 = patient.address3;
        if (isBlockEnabled) {
            binding.llBlock.visibility = View.VISIBLE
            binding.llBlock.isEnabled = true
            if ((address3 != null && address3.isNotEmpty() && isOtherBlockSelected) || (isOtherBlockSelected)) {
                enableOtherBlock()
            } else {
                disableOtherBlock()
            }
        } else {
            binding.llBlock.visibility = View.GONE
            binding.llBlock.isEnabled = false

            binding.llCityVillage.visibility =
                if (isCityVillageEnabled) View.VISIBLE else View.GONE
        }
    }

    private fun setOtherBlockData() {
        //if (binding.autoCompleteBlock.text.contains("Other", ignoreCase = true)) {
        if (isOtherBlockSelected()) {
            patient.address3 = binding.textInputOtherBlock.text.toString()
            patient.cityvillage = binding.textInputCityVillage.text.toString()
        }
        //}
    }

    private fun isOtherBlockSelected1() =
        binding.autoCompleteBlock.text.contains("Other", ignoreCase = true) ||
                (patient.address3 != null && patient.address3.isNotEmpty() && patient.address3.contains(
                    "Other",
                    ignoreCase = true
                ))

    private fun isOtherBlockSelected() =
        binding.autoCompleteBlock.text.contains("Other", ignoreCase = true)

    private fun eraseAllBlockFields() {
        binding.autoCompleteBlock.setText("", false)
        binding.autoCompleteVillageDropdown.setText("", false)
        binding.textInputOtherBlock.setText("")
        binding.autoCompleteBlock.setAdapter(null)
        binding.autoCompleteVillageDropdown.setAdapter(null)
        if (isOtherBlockSelected)
            binding.textInputCityVillage.setText("")
        if (binding.llBlock.isEnabled) {
            isOtherBlockSelected = false;
            disableOtherBlock()
        }
    }
}
