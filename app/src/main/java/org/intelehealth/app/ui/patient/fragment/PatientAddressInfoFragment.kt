package org.intelehealth.app.ui.patient.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.databinding.OnRebindCallback
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.intelehealth.app.BuildConfig
import org.intelehealth.app.R
import org.intelehealth.app.activities.identificationActivity.model.DistData
import org.intelehealth.app.activities.identificationActivity.model.ProvincesAndCities
import org.intelehealth.app.activities.identificationActivity.model.StateData
import org.intelehealth.app.databinding.FragmentPatientAddressInfoBinding
import org.intelehealth.app.databinding.FragmentPatientOtherInfoBinding
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
        /*patientViewModel.fetchAddressRegFields().observe(viewLifecycleOwner) {
            binding.addressInfoConfig = PatientRegFieldsUtils.buildPatientAddressInfoConfig(it)
            Timber.d { "Address Config => ${Gson().toJson(binding.addressInfoConfig)}" }
            binding.addOnRebindCallback(onRebindCallback)
        }*/

        lifecycleScope.launch {
            patientViewModel.fetchAddressRegFieldsSuspended().let {
                binding.addressInfoConfig = PatientRegFieldsUtils.buildPatientAddressInfoConfig(it)
                Timber.d { "Address Config => ${Gson().toJson(binding.addressInfoConfig)}" }
                binding.addOnRebindCallback(onRebindCallback)
            }
        }
    }

    private val onRebindCallback = object : OnRebindCallback<FragmentPatientAddressInfoBinding>() {
        override fun onBound(binding: FragmentPatientAddressInfoBinding?) {
            super.onBound(binding)
            setupCountries()
            setupStates()
            applyFilter()
            setInputTextChangListener()
            setClickListener()
            //province and cities required only for unfpa
            if(BuildConfig.FLAVOR_client == FlavorKeys.UNFPA){
                setupProvinceAndCities()
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
            postalcode = binding.textInputPostalCode.text?.toString()
            val village = binding.textInputCityVillage.text?.toString()
            cityvillage = if (district.isNullOrEmpty().not()) "${district}:$village"
            else village
            address1 = binding.textInputAddress1.text?.toString()
            address2 = binding.textInputAddress2.text?.toString()
            registrationAddressOfHf = binding.textInputRegistrationAddressOfHf.text?.toString()

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
        binding.textInputLayRegistrationAddressOfHf.hideErrorOnTextChang(binding.textInputRegistrationAddressOfHf)

        binding.textInputLayPostalCode.hideDigitErrorOnTextChang(binding.textInputPostalCode, 6)
    }

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

    private fun setupProvinceAndCities() {
        LanguageUtils.getProvincesAndCities().let {
            //province
            binding.textInputLayProvince.tag = it
            val adapter: ArrayAdapter<String> = ArrayAdapterUtils.getObjectArrayAdapter(
                requireContext(), LanguageUtils.getProvinceByLocal(it)
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
                patient.codeOfHealthFacility = LanguageUtils.getCodeOfHf(provincesAndCities.provinces[i])
            }

            //cities
            binding.textInputLayCity.tag = it
            val cityAdapter: ArrayAdapter<String> = ArrayAdapterUtils.getObjectArrayAdapter(
                requireContext(), LanguageUtils.getCityByLocal(it)
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
            }
        }

        binding.autoCompleteDistrict.setOnItemClickListener { adapterView, _, i, _ ->
            binding.textInputLayDistrict.hideError()
            val dList: List<DistData> = binding.textInputLayDistrict.tag as List<DistData>
            patient.district = dList[i].name
        }
    }

    private fun validateForm(block: () -> Unit) {
        Timber.d { "Final patient =>${Gson().toJson(patient)}" }
        val error = R.string.this_field_is_mandatory
        binding.addressInfoConfig?.let {
            val bPostalCode = if (it.postalCode?.isEnabled == true && it.postalCode?.isMandatory == true) {
                binding.textInputLayPostalCode.validate(binding.textInputPostalCode, error).and(
                    binding.textInputLayPostalCode.validateDigit(
                        binding.textInputPostalCode,
                        R.string.postal_code_6_dig_invalid_txt,
                        6
                    )
                )

            } else true


            val bCountry = if (it.country?.isEnabled == true && it.country?.isMandatory == true) {
                binding.textInputLayCountry.validateDropDowb(
                    binding.autoCompleteCountry,
                    error
                )
            } else true

            val bState = if (it.state?.isEnabled == true && it.state?.isMandatory == true) {
                binding.textInputLayState.validateDropDowb(
                    binding.autoCompleteState,
                    error
                )
            } else true

            val bDistrict = if (it.district?.isEnabled == true && it.district?.isMandatory == true) {
                binding.textInputLayDistrict.validateDropDowb(
                    binding.autoCompleteState,
                    error
                )
            } else true

            val bCityVillage = if (it.cityVillage?.isEnabled == true && it.cityVillage?.isMandatory == true) {
                binding.textInputLayCityVillage.validate(binding.textInputCityVillage, error).and(
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


            if (bPostalCode.and(bCountry).and(bState).and(bDistrict).and(bCityVillage)
                    .and(bAddress1).and(bAddress2).and(bProvince).and(bCity)
                    .and(bRelativeAddressOfHf)
            ) block.invoke()
        }
    }
}