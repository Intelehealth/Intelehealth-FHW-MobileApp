package org.intelehealth.app.ui.patient.fragment

import android.os.Bundle

import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.databinding.OnRebindCallback
import androidx.lifecycle.Observer
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import org.intelehealth.app.R
import org.intelehealth.app.activities.identificationActivity.model.Block
import org.intelehealth.app.activities.identificationActivity.model.DistData
import org.intelehealth.app.activities.identificationActivity.model.GramPanchayat
import org.intelehealth.app.activities.identificationActivity.model.StateData
import org.intelehealth.app.activities.identificationActivity.model.Village
import org.intelehealth.app.databinding.FragmentPatientAddressInfoBinding
import org.intelehealth.app.databinding.FragmentPatientOtherInfoBinding
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.ui.filter.FirstLetterUpperCaseInputFilter
import org.intelehealth.app.utilities.ArrayAdapterUtils
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


class PatientAddressInfoFragmentOLD : BasePatientFragment(R.layout.fragment_patient_address_info_old) {

    private lateinit var binding: FragmentPatientAddressInfoBinding
    private var isCityVillageEnabled: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentPatientAddressInfoBinding.bind(view)
        binding.textInputLayDistrict.isEnabled = false
        patientViewModel.updatePatientStage(PatientRegStage.ADDRESS)
        super.onViewCreated(view, savedInstanceState)
    }

  /*  private fun setupCountries() {
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

    private val onRebindCallback = object : OnRebindCallback<FragmentPatientAddressInfoBinding>() {
        override fun onBound(binding: FragmentPatientAddressInfoBinding?) {
            super.onBound(binding)
            if (binding != null) {
                Log.d(
                    "kaveridev",
                    "onBound: blockval : " + (binding.addressInfoConfig?.block?.isEnabled)
                )//impstart here
                val isBlockEnabled = (binding.addressInfoConfig?.block?.isEnabled);
                if (isBlockEnabled == true) {
                    patientViewModel.setCityVillageEnabled(false)
                } else {
                    Log.d(
                        "kaveridev",
                        "onBound: isEnabled : " + (binding.addressInfoConfig?.cityVillage?.isEnabled
                            ?: true)
                    )
                    patientViewModel.setCityVillageEnabled(
                        binding.addressInfoConfig?.cityVillage?.isEnabled ?: true
                    )
                }
                patientViewModel.addressInfoConfigCityVillageEnabled.observe(
                    viewLifecycleOwner
                ) { isEnabled ->
                    isCityVillageEnabled = isEnabled

                    Log.d("kaveridev", "setupBlocks: isEnabled : $isEnabled")
                    Log.d("kaveridev", "setupBlocks: isEnabled : " + isEnabled)
                    binding.llCityVillage.visibility = if (isEnabled) View.VISIBLE else View.GONE
                    binding.llCityVillage.isEnabled = isEnabled
                    val address3 = patient.address3;
                    if (address3 != null && address3.isNotEmpty() && address3.contains("Other")) {
                        Log.d("TAG", "setupBlocks: yes")
                        binding.textInputLayOtherBlock.visibility = View.VISIBLE
                        binding.llCityVillage.visibility = View.VISIBLE
                        binding.llVillageDropdown.visibility = View.GONE
                        binding.llOtherBlock.visibility = View.VISIBLE


                    } else {
                        Log.d("TAG", "setupBlocks: no")

                        binding.textInputLayOtherBlock.visibility = View.GONE
                        binding.llCityVillage.visibility = View.GONE
                        binding.llVillageDropdown.visibility = View.VISIBLE
                        binding.llOtherBlock.visibility = View.GONE
                    }
                }

            }

            setupCountries()
            setupStates()
            applyFilter()
            setInputTextChangListener()
            setClickListener()
        }
    }

    private fun setClickListener() {
        binding.frag2BtnBack.setOnClickListener {
            if (binding.autoCompleteBlock.text.contains("Other", ignoreCase = true)) {
                patient.address3 = binding.textInputOtherBlock.text.toString()
                patient.cityvillage = binding.textInputCityVillage.text.toString()
            }
            findNavController().popBackStack()
        }
        binding.frag2BtnNext.setOnClickListener {
            if (binding.autoCompleteBlock.text.contains("Other", ignoreCase = true)) {
                patient.address3 = binding.textInputOtherBlock.text.toString()
                patient.cityvillage = binding.textInputCityVillage.text.toString()
            }
            validateForm { savePatient() }
        }
    }

    private fun savePatient() {
        patient.apply {
            postalcode = binding.textInputPostalCode.text?.toString()
            var village: String

            if (binding.autoCompleteBlock.text.contains("Other", ignoreCase = true)) {
                patient.address3 = binding.textInputOtherBlock.text.toString()
                patient.cityvillage = binding.textInputCityVillage.text.toString()
                village = binding.textInputCityVillage.text?.toString().toString()
            } else {
                village = binding.autoCompleteVillageDropdown.text.toString()
            }
            cityvillage = if (district.isNullOrEmpty().not() && village.isNullOrEmpty()
                    .not()
            ) "${district}:$village"
            else district
            //else village
            address1 = binding.textInputAddress1.text?.toString()
            address2 = binding.textInputAddress2.text?.toString()
            postalcode = binding.textInputPostalCode.text?.toString()
            //address3 = binding.autoCompleteBlock.text?.toString()
            householdNumber = binding.textInputHouseholdNumber.text?.toString()

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
    }

    private fun setInputTextChangListener() {
        binding.textInputLayCityVillage.hideErrorOnTextChang(binding.textInputCityVillage)
        binding.textInputLayAddress1.hideErrorOnTextChang(binding.textInputAddress1)
        binding.textInputLayAddress2.hideErrorOnTextChang(binding.textInputAddress2)
        binding.textInputLayPostalCode.hideDigitErrorOnTextChang(binding.textInputPostalCode, 6)
        binding.textInputLayHouseholdNumber.hideDigitErrorOnTextChang(
            binding.textInputPostalCode, 10
        )

    }

    *//*
        private fun setupStates() {
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
                    val list: List<StateData> = binding.textInputLayState.tag as List<StateData>
                    val selectedState = list[i]
                    patient.stateprovince = selectedState.state
                    setupDistricts(selectedState)
                }
            }

        }
    *//*
    private fun setupStates() {
        var enable = false
        *//* val enable = patientRegistrationFields?.let {
             PatientRegFieldsUtils.getFieldEnableStatus(
                 it,
                 PatientRegConfigKeys.STATE
             )
         }*//*
        *//* binding.addressInfoConfig?.let {
             enable= it.state!!.isEditable
         }*//*
        binding.addressInfoConfig?.let { config ->
            val state = config.state
            enable = state?.isEditable ?: false // Use a safe call and provide a default value
        } ?: run {
            Log.d("TAG", "addressInfoConfig or state is null")
        }


        LanguageUtils.getStateList()?.let {
            binding.textInputLayState.tag = it
            val adapter: ArrayAdapter<StateData> = ArrayAdapterUtils.getObjectArrayAdapter(
                requireContext(), it
            )
            binding.autoCompleteState.setAdapter(adapter)
            Log.d("TAG", "setupStates: enable : " + enable)
            if (!enable) {
                val state = LanguageUtils.getState(getString(R.string.default_state))
                binding.autoCompleteState.setText(state.toString(), false)
                patient.stateprovince = binding.autoCompleteState.text.toString()

                state?.let { it1 -> setupDistricts(it1) }
            } else {
                if (patient.stateprovince != null && patient.stateprovince.isNotEmpty()) {
                    val state = LanguageUtils.getState(patient.stateprovince)
                    if (state != null) {
                        binding.autoCompleteState.setText(state.toString(), false)
                        setupDistricts(state)
                    }
                }
            }

            binding.autoCompleteState.setOnItemClickListener { adapterView, _, i, _ ->
                binding.textInputLayState.hideError()
                val list: List<StateData> = binding.textInputLayState.tag as List<StateData>
                val selectedState = list[i]
                patient.stateprovince = selectedState.state
                setupDistricts(selectedState)
            }
        }

    }

    *//*
        private fun setupDistricts(stateData: StateData) {
            binding.textInputLayDistrict.isEnabled = true
            val adapter: ArrayAdapter<DistData> = ArrayAdapterUtils.getObjectArrayAdapter(
                requireContext(), stateData.distDataList
            )
            binding.autoCompleteDistrict.setAdapter(adapter)
            binding.textInputLayDistrict.tag = stateData.distDataList
            if (patient.district != null && patient.district.isNotEmpty()) {
                val selected = LanguageUtils.getDistrict(stateData, patient.district)
                if (selected != null) {
                    binding.autoCompleteDistrict.setText(selected.toString(), false)
                    if (binding.llBlock.isEnabled) setupBlocks(selected)
                }
            }

            binding.autoCompleteDistrict.setOnItemClickListener { adapterView, _, i, _ ->
                binding.textInputLayDistrict.hideError()
                val dList: List<DistData> = binding.textInputLayDistrict.tag as List<DistData>
                patient.district = dList[i].name
                val selectedState = dList[i]
                if (binding.llBlock.isEnabled) setupBlocks(selectedState)
            }
        }
    *//*
    private fun setupDistricts(stateData: StateData) {
        Log.d("kaveridev", "setupDistricts: stateData : " + Gson().toJson(stateData))
        // binding.textInputLayDistrict.isEnabled = true
        var enable = false
        binding.addressInfoConfig?.let { config ->
            val district = config.district
            enable = district?.isEditable ?: false // Use a safe call and provide a default value
        } ?: run {
            Log.d("TAG", "addressInfoConfig or district is null")
        }
        val adapter: ArrayAdapter<DistData> = ArrayAdapterUtils.getObjectArrayAdapter(
            requireContext(), stateData.distDataList
        )
        binding.autoCompleteDistrict.setAdapter(adapter)
        Log.d("TAG", "setupDistricts: enable : " + enable)

        if (!enable) {
            val district =
                LanguageUtils.getDistrict(stateData, getString(R.string.default_district))
            binding.autoCompleteDistrict.setText(getString(R.string.default_district))
            patient.district = binding.autoCompleteDistrict.text.toString()
            Log.d("kaveridev", "setupDistricts: district :  " + district)
            if (binding.llBlock.isEnabled) district?.let { setupBlocks(it) }

        } else {
            binding.textInputLayDistrict.tag = stateData.distDataList
            if (patient.district != null && patient.district.isNotEmpty()) {
                val selected = LanguageUtils.getDistrict(stateData, patient.district)
                if (selected != null) {
                    binding.autoCompleteDistrict.setText(selected.toString(), false)
                    if (binding.llBlock.isEnabled) selected?.let { setupBlocks(it) }

                }
            }
        }


        binding.autoCompleteDistrict.setOnItemClickListener { adapterView, _, i, _ ->
            binding.textInputLayDistrict.hideError()
            val dList: List<DistData> = binding.textInputLayDistrict.tag as List<DistData>
            patient.district = dList[i].name
            val selectedState = dList[i]
            if (binding.llBlock.isEnabled) setupBlocks(selectedState)
        }
    }

    private fun validateForm(block: () -> Unit) {
        Timber.d { "Final patient =>${Gson().toJson(patient)}" }
        val error = R.string.this_field_is_mandatory
        binding.addressInfoConfig?.let {
            val bPostalCode = if (it.postalCode!!.isEnabled && it.postalCode!!.isMandatory) {
                binding.textInputLayPostalCode.validate(binding.textInputPostalCode, error).and(
                    binding.textInputLayPostalCode.validateDigit(
                        binding.textInputPostalCode, R.string.postal_code_6_dig_invalid_txt, 6
                    )
                )

            } else true


            val bCountry = if (it.country!!.isEnabled && it.country!!.isMandatory) {
                binding.textInputLayCountry.validateDropDowb(
                    binding.autoCompleteCountry, error
                )
            } else true

            val bState = if (it.state!!.isEnabled && it.state!!.isMandatory) {
                binding.textInputLayState.validateDropDowb(
                    binding.autoCompleteState, error
                )
            } else true

            val bDistrict = if (it.district!!.isEnabled && it.district!!.isMandatory) {
                binding.textInputLayDistrict.validateDropDowb(
                    binding.autoCompleteState, error
                )
            } else true

            val bBlock = if (it.block!!.isEnabled && it.block!!.isMandatory) {
                binding.textInputLayBlock.validateDropDowb(
                    binding.autoCompleteBlock, error
                )
            } else true

            val bVillageDropdown =
                if (it.block!!.isEnabled && it.block!!.isMandatory && !binding.autoCompleteBlock.text.contains(
                        "other", ignoreCase = true
                    )
                ) {
                    binding.textInputLayVillageDropdown.validateDropDowb(
                        binding.autoCompleteVillageDropdown, error
                    )
                } else true

            *//*   if (binding.autoCompleteBlock.text.contains("other", ignoreCase = true))
                   patientViewModel.setCityVillageEnabled(true)
               else
                   patientViewModel.setCityVillageEnabled(false)
   *//*
            Log.d(
                "kaveridev",
                "validateForm: cityval : " + patientViewModel.addressInfoConfigCityVillageEnabled.value
            )
            Log.d("kaveridev", "validateForm: isCityVillageEnabled : " + isCityVillageEnabled)
            Log.d("kaveridev", "validateForm: city mand : " + it.cityVillage!!.isMandatory)

            val bCityVillage =
                if (it.block!!.isEnabled && it.block!!.isMandatory && binding.autoCompleteBlock.text.contains(
                        "other", ignoreCase = true
                    )
                ) {
                    binding.textInputLayCityVillage.validate(binding.textInputCityVillage, error)
                        .and(
                            binding.textInputLayCityVillage.validateDigit(
                                binding.textInputCityVillage,
                                R.string.error_field_valid_village_required,
                                3
                            )
                        )
                } else true

            val bBlockOther =
                if (it.block!!.isEnabled && it.block!!.isMandatory && binding.autoCompleteBlock.text.contains(
                        "other", ignoreCase = true
                    )
                ) {
                    binding.textInputLayCityVillage.validate(binding.textInputCityVillage, error)
                        .and(
                            binding.textInputLayCityVillage.validateDigit(
                                binding.textInputCityVillage,
                                R.string.error_field_valid_village_required,
                                3
                            )
                        )
                } else true
            val bHouseholdNumber =
                if (it.householdNumber!!.isEnabled && it.householdNumber!!.isMandatory) {
                    binding.textInputLayHouseholdNumber.validate(
                        binding.textInputHouseholdNumber, error
                    )
                } else true

            val bAddress1 = if (it.address1!!.isEnabled && it.address1!!.isMandatory) {
                binding.textInputLayAddress1.validate(binding.textInputAddress1, error)
            } else true

            val bAddress2 = if (it.address2!!.isEnabled && it.address2!!.isMandatory) {
                binding.textInputLayAddress1.validate(binding.textInputAddress1, error)
            } else true


            if (bPostalCode.and(bCountry).and(bState).and(bDistrict).and(bBlock)
                    .and(bVillageDropdown).and(bCityVillage).and(bAddress1).and(bAddress2)
                    .and(bBlockOther).and(bHouseholdNumber)
            ) block.invoke()
        }
    }

    private fun setupBlocks(districtData: DistData) {
        val adapter: ArrayAdapter<Block> = ArrayAdapterUtils.getObjectArrayAdapter(
            requireContext(), districtData.blocks
        )
        binding.autoCompleteBlock.setAdapter(adapter)
        binding.textInputLayBlock.tag = districtData.blocks
        Log.d("kaveridev", "setupBlocks:blockname  " + patient.address3)

        if (patient.address3 != null && patient.address3.isNotEmpty()) {
            val selected = LanguageUtils.getBlock(districtData, patient.address3)
            Log.d("kaveridev", "setupBlocks: selected 1 : " + selected)
            // if (selected != null) {
            Log.d("kaveridev", "setupBlocks: selected 2 : " + selected)
            if (selected == null) {
                val selected = LanguageUtils.getBlock(districtData, "Other Block")
                binding.autoCompleteBlock.setText(selected.toString(), false)
                //binding.autoCompleteBlock.setText(getString(R.string.other_block))
                binding.textInputOtherBlock.setText(patient.address3)
                Log.d("kaveridev", "setupBlocks: yes")
                binding.textInputLayOtherBlock.visibility = View.VISIBLE
                binding.llCityVillage.visibility = View.VISIBLE
                binding.llVillageDropdown.visibility = View.GONE
                binding.llOtherBlock.visibility = View.VISIBLE
            } else {
                Log.d("kaveridev", "setupBlocks: no")
                binding.autoCompleteBlock.setText(selected.toString(), false)

                binding.textInputLayOtherBlock.visibility = View.GONE
                binding.llCityVillage.visibility = View.GONE
                binding.llVillageDropdown.visibility = View.VISIBLE
                binding.llOtherBlock.visibility = View.GONE
                setupVillages(selected)
            }
            // }
        }

        binding.autoCompleteBlock.setOnItemClickListener { adapterView, _, i, _ ->
            binding.textInputLayBlock.hideError()
            binding.textInputCityVillage.setText("")
            binding.textInputOtherBlock.setText("")
            binding.autoCompleteVillageDropdown.setText("")

            val blocksList: List<Block> = binding.textInputLayBlock.tag as List<Block>
            val selectedBlock = blocksList[i]
            if (selectedBlock.name?.contains("Other") == true) {
                Log.d("kaveridev", "setupBlocks: yes")
                binding.textInputLayOtherBlock.visibility = View.VISIBLE
                binding.llCityVillage.visibility = View.VISIBLE
                binding.llVillageDropdown.visibility = View.GONE
                binding.llOtherBlock.visibility = View.VISIBLE
                binding.textInputCityVillage.setText("")
                patient.address3 = binding.textInputOtherBlock.text.toString()
            } else {
                Log.d("kaveridev", "setupBlocks: no")
                binding.textInputLayOtherBlock.visibility = View.GONE
                binding.llCityVillage.visibility = View.GONE
                binding.llVillageDropdown.visibility = View.VISIBLE
                binding.llOtherBlock.visibility = View.GONE
                patient.address3 = blocksList[i].name
                binding.textInputCityVillage.setText("")
            }
            setupVillages(selectedBlock)
        }

    }

    private fun setupVillages(blocksData: Block) {
        // binding.textInputLayVillageDropdown.isEnabled = true

        val villages = mutableListOf<Village>()
        blocksData.gramPanchayats?.forEach { gramPanchayat ->
            gramPanchayat.villages?.let { villageList ->
                if (villageList.isNotEmpty()) {
                    villages.addAll(villageList)
                } else {
                    Log.d("kaveridev", "No villages available for ${gramPanchayat.name}")
                }
            } ?: run {
                Log.d("kaveridev", "Villages are null for ${gramPanchayat.name}")
            }
        }
        if (patient.villageWithoutDistrict != null && patient.villageWithoutDistrict.isNotEmpty()) {
            val selected = LanguageUtils.getVillage(
                blocksData.gramPanchayats?.get(0), patient.villageWithoutDistrict
            )
            Log.d("kaveridev", "setupVillages: selected 1 : " + selected)
            if (selected != null) {
                binding.autoCompleteVillageDropdown.setText(selected.toString(), false)
                Log.d("kaveridev", "setupVillages: selected 2 : " + selected)
            }
        }

        val adapter: ArrayAdapter<Village> =
            ArrayAdapterUtils.getObjectArrayAdapter(requireContext(), villages)

        binding.autoCompleteVillageDropdown.setAdapter(adapter)
        binding.textInputLayVillageDropdown.tag = blocksData.gramPanchayats

        if (patient.villageWithoutDistrict != null && patient.villageWithoutDistrict.isNotEmpty()) {
            val selected = LanguageUtils.getVillage(
                blocksData.gramPanchayats?.get(0), patient.villageWithoutDistrict
            )
            if (selected != null) {
                binding.autoCompleteVillageDropdown.setText(selected.toString(), false)
            }
        }

        binding.autoCompleteVillageDropdown.setOnItemClickListener { adapterView, _, i, _ ->
            binding.textInputLayVillageDropdown.hideError()
            // Retrieve the selected village from the villages list based on the index
            val selectedVillage = villages[i]
            if (binding.autoCompleteBlock.text.contains(
                    "Other",
                    ignoreCase = true
                )
            ) binding.textInputCityVillage.setText("")
            else patient.cityvillage = selectedVillage.name

        }
    }

    *//*
        private fun setupStates() {
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
                    val list: List<StateData> = binding.textInputLayState.tag as List<StateData>
                    val selectedState = list[i]
                    patient.stateprovince = selectedState.state
                    setupDistricts(selectedState)
                }
            }

        }

        private fun setupDistricts(stateData: StateData) {
            binding.textInputLayDistrict.isEnabled = true
            val adapter: ArrayAdapter<DistData> = ArrayAdapterUtils.getObjectArrayAdapter(
                requireContext(), stateData.distDataList
            )
            binding.autoCompleteDistrict.setAdapter(adapter)
            binding.textInputLayDistrict.tag = stateData.distDataList
            if (patient.district != null && patient.district.isNotEmpty()) {
                val selected = LanguageUtils.getDistrict(stateData, patient.district)
                if (selected != null) {
                    binding.autoCompleteDistrict.setText(selected.toString(), false)
                    if (binding.llBlock.isEnabled) setupBlocks(selected)
                }
            }

            binding.autoCompleteDistrict.setOnItemClickListener { adapterView, _, i, _ ->
                binding.textInputLayDistrict.hideError()
                val dList: List<DistData> = binding.textInputLayDistrict.tag as List<DistData>
                patient.district = dList[i].name
                val selectedState = dList[i]
                if (binding.llBlock.isEnabled) setupBlocks(selectedState)
            }
        }*//*
    KKKKKKKKKKKKKKKKKKKKK*/

/*
    private fun setupStates() {
        var enable = false
        */
/* val enable = patientRegistrationFields?.let {
             PatientRegFieldsUtils.getFieldEnableStatus(
                 it,
                 PatientRegConfigKeys.STATE
             )
         }*//*

        */
/* binding.addressInfoConfig?.let {
             enable= it.state!!.isEditable
         }*//*

        binding.addressInfoConfig?.let { config ->
            val state = config.state
            enable = state?.isEditable ?: false // Use a safe call and provide a default value
        } ?: run {
            Log.d("TAG", "addressInfoConfig or state is null")
        }


        LanguageUtils.getStateList()?.let {
            binding.textInputLayState.tag = it
            val adapter: ArrayAdapter<StateData> = ArrayAdapterUtils.getObjectArrayAdapter(
                requireContext(), it
            )
            binding.autoCompleteState.setAdapter(adapter)
            Log.d("TAG", "setupStates: enable : " + enable)
            if (!enable) {
                val state = LanguageUtils.getState(getString(R.string.default_state))
                binding.autoCompleteState.setText(state.toString(), false)
                patient.stateprovince = binding.autoCompleteState.text.toString()

                state?.let { it1 -> setupDistricts(it1) }
            } else {
                if (patient.stateprovince != null && patient.stateprovince.isNotEmpty()) {
                    val state = LanguageUtils.getState(patient.stateprovince)
                    if (state != null) {
                        binding.autoCompleteState.setText(state.toString(), false)
                        setupDistricts(state)
                    }
                }
            }

            binding.autoCompleteState.setOnItemClickListener { adapterView, _, i, _ ->
                binding.textInputLayState.hideError()
                val list: List<StateData> = binding.textInputLayState.tag as List<StateData>
                val selectedState = list[i]
                patient.stateprovince = selectedState.state
                setupDistricts(selectedState)
            }
        }

    }
*/
}