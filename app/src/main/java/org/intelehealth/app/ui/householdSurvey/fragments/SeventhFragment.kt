package org.intelehealth.app.ui.householdSurvey.fragments

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.github.ajalt.timberkt.Timber
import com.google.android.datatransport.runtime.firebase.transport.LogEventDropped
import com.google.gson.Gson
import org.intelehealth.app.R
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity2
import org.intelehealth.app.ui.householdSurvey.models.HouseholdSurveyModel
import org.intelehealth.app.databinding.FragmentSeventhHouseholdSurveyBinding
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.ui.patient.fragment.PatientAddressInfoFragmentDirections
import org.intelehealth.app.utilities.DialogUtils
import org.intelehealth.app.utilities.HouseholdSurveyStage
import org.intelehealth.app.utilities.PatientRegStage
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.core.registry.PermissionRegistry
import java.util.Calendar
import java.util.Locale

class SeventhFragment : BaseHouseholdSurveyFragment(R.layout.fragment_seventh_household_survey) {

    private lateinit var binding: FragmentSeventhHouseholdSurveyBinding
    var selectedDate = Calendar.getInstance().timeInMillis
    private var patientUuid: String? = null
    private lateinit var updatedContext: Context

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSeventhHouseholdSurveyBinding.bind(view)
        houseHoldViewModel.updatePatientStage(HouseholdSurveyStage.SEVENTH_SCREEN)

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
            setupDistanceToSubCenter()
            setupDistanceToNearestPrimaryHealthCenter()
            setupDistanceToNearestCommunityHealthCentre()
            setupDistanceToNearestDistrictHospital()
            setupDistanceToNearestPathologicalLabDistance()
            setupDistanceToNearestPrivateClinicMBBSDoctor()
            setupDistanceToNearestPrivateClinicAlternateMedicine()
            setupDistanceToNearestTertiaryCareFacility()
            Log.d("devchdbsave7", "savePatient: householdSurveyModel : " + householdSurveyModel)
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
            "seventhScreen", patient,
            householdSurveyModel
        ).observe(viewLifecycleOwner) {
            it ?: return@observe
            houseHoldViewModel.handleResponse(it) { result -> if (result) navigateToDetails() }
        }
    }

    private fun navigateToDetails() {
        Log.d("TAG", "navigateToDetails: message saved")
        showSavedDialog()
        /*  SeventhFragmentDirections.actionSevenToSix().apply {
              findNavController().navigate(this)
          }*/
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
        setDataToDistanceToSubCenterUI()
        setDataToDistanceToNearestPrimaryHealthCenterUI()
        setDataToDistanceToNearestCommunityHealthCentreUI()
        setDataToDistanceToNearestDistrictHospitalUI()
        setDataToDistanceToNearestPathologicalLabUI()
        setDataToDistanceToNearestPrivateClinicMBBSDoctorUI()
        setDataToDistanceToNearestPrivateClinicAlternateMedicineUI()
        setDataToDistanceToNearestTertiaryCareFacilityUI()
    }

    override fun onPatientDataLoaded(householdSurveyModel: HouseholdSurveyModel) {
        super.onPatientDataLoaded(householdSurveyModel)
        Timber.d { Gson().toJson(householdSurveyModel) }
        setDataToUI();

        binding.patientSurveyAttributes = householdSurveyModel
        binding.isEditMode = houseHoldViewModel.isEditMode
    }

    private fun setupDistanceToSubCenter() {
        var distanceToSubCenter = "-"
        if (binding.distanceToSubCentreRadioGroup.checkedRadioButtonId != -1) {
             distanceToSubCenter = (binding.distanceToSubCentreRadioGroup
                .findViewById<RadioButton>(binding.distanceToSubCentreRadioGroup.checkedRadioButtonId)).text.toString()

        }
        householdSurveyModel.subCentreDistance = distanceToSubCenter
    }

    private fun setDataToDistanceToSubCenterUI() {
        val value1 = householdSurveyModel.subCentreDistance
        value1?.let {
            value1.let {
                when (it) {
                    getString(R.string.within_5_minutes) -> binding.subCenter5Minutes.isChecked =
                        true

                    getString(R.string.five_fifteen_minutes) -> binding.subCenter515Minutes.isChecked =
                        true

                    getString(R.string.fifteen_thirty_minutes) -> binding.subCenter1530Minutes.isChecked =
                        true

                    getString(R.string.more_than_thirty_minutes) -> binding.subCenterMoreThan30Minutes.isChecked =
                        true
                }
            }

        }
    }

    private fun setupDistanceToNearestPrimaryHealthCenter() {
        var nearestPrimaryHealthCenterDistance = "-"
        if (binding.distanceToNearestPrimaryHealthCentresRadioGroup.checkedRadioButtonId != -1) {
            nearestPrimaryHealthCenterDistance = (binding.distanceToNearestPrimaryHealthCentresRadioGroup
                .findViewById<RadioButton>(binding.distanceToNearestPrimaryHealthCentresRadioGroup.checkedRadioButtonId)).text.toString()
        }
        householdSurveyModel.nearestPrimaryHealthCenterDistance = nearestPrimaryHealthCenterDistance
    }

    private fun setDataToDistanceToNearestPrimaryHealthCenterUI() {
        val value1 = householdSurveyModel.nearestPrimaryHealthCenterDistance
        value1?.let {
            when (it) {
                getString(R.string.within_1_km) -> binding.phc1Km.isChecked = true
                getString(R.string.one_to_three_kms) -> binding.phc13Km.isChecked = true
                getString(R.string.three_to_five_kms) -> binding.phc35Km.isChecked = true
                getString(R.string.five_to_ten_kms) -> binding.phc510Km.isChecked = true
                getString(R.string.more_than_ten_kms) -> binding.phcMoreThan10Km.isChecked = true
            }
        }

    }

    private fun setupDistanceToNearestCommunityHealthCentre() {
        var distanceToCommunityHealthCentre = "-"
        if (binding.distanceToNearestCommunityHealthCentresRadioGroup.checkedRadioButtonId != -1) {
            distanceToCommunityHealthCentre =
                (binding.distanceToNearestCommunityHealthCentresRadioGroup
                    .findViewById<RadioButton>(binding.distanceToNearestCommunityHealthCentresRadioGroup.checkedRadioButtonId)).text.toString()
        }
        householdSurveyModel.nearestCommunityHealthCenterDistance = distanceToCommunityHealthCentre
    }

    private fun setDataToDistanceToNearestCommunityHealthCentreUI() {
        val value1 = householdSurveyModel.nearestCommunityHealthCenterDistance
        value1?.let {
            when (it) {
                getString(R.string.within_5_kms) -> binding.chcWithin5Kms.isChecked = true
                getString(R.string.five_to_ten_kms) -> binding.chc510Kms.isChecked = true
                getString(R.string.ten_to_twenty_kms) -> binding.chc1020Kms.isChecked = true
                getString(R.string.more_than_twenty_km) -> binding.chcMoreThan20Kms.isChecked =
                    true
            }
        }
    }

    private fun setupDistanceToNearestDistrictHospital() {
        var distanceToNearestDistrictHospital = "-"
        if (binding.distanceToNearestDistrictHospitalRadioGroup.checkedRadioButtonId != -1) {
             distanceToNearestDistrictHospital =
                (binding.distanceToNearestDistrictHospitalRadioGroup
                    .findViewById<RadioButton>(binding.distanceToNearestDistrictHospitalRadioGroup.checkedRadioButtonId)).text.toString()
        }
        householdSurveyModel.nearestDistrictHospitalDistance = distanceToNearestDistrictHospital
    }

    private fun setDataToDistanceToNearestDistrictHospitalUI() {
        val value1 = householdSurveyModel.nearestDistrictHospitalDistance
        value1?.let {
            when (it) {
                getString(R.string.within_ten_km) -> binding.dhWithin10Kms.isChecked = true
                getString(R.string.ten_to_twenty_kms) -> binding.dh1020Kms.isChecked = true
                getString(R.string.twenty_to_forty_km) -> binding.dh2040Kms.isChecked = true
                getString(R.string.fifty_to_seventy_km) -> binding.dh5070Kms.isChecked = true
                getString(R.string.more_than_seventy_km) -> binding.dhMoreThan70Kms.isChecked = true
            }
        }
    }

    private fun setupDistanceToNearestPathologicalLabDistance() {
        var distanceToNearestPathLab = "-"
        if (binding.distanceToNearestPathologicalLabRadioGroup.checkedRadioButtonId != -1) {
             distanceToNearestPathLab =
                (binding.distanceToNearestPathologicalLabRadioGroup
                    .findViewById<RadioButton>(binding.distanceToNearestPathologicalLabRadioGroup.checkedRadioButtonId)).text.toString()
        }
        householdSurveyModel.nearestPathologicalLabDistance = distanceToNearestPathLab
    }

    private fun setDataToDistanceToNearestPathologicalLabUI() {
        val value1 = householdSurveyModel.nearestPathologicalLabDistance
        value1?.let {
            when (it) {
                getString(R.string.within_ten_km) -> binding.plWithin10Km.isChecked = true
                getString(R.string.ten_to_twenty_kms) -> binding.pl1020Km.isChecked = true
                getString(R.string.twenty_to_forty_km) -> binding.pl2040Km.isChecked = true
                getString(R.string.fifty_to_seventy_km) -> binding.pl5070Km.isChecked = true
                getString(R.string.more_than_seventy_km) -> binding.plMoreThan70Km.isChecked = true
            }
        }
    }

    private fun setupDistanceToNearestPrivateClinicMBBSDoctor() {
        var privateClinicMBBSDoctor = "-"
        if (binding.distanceToNearestPrivateClinicWithAnMbbsDoctorRadioGroup.checkedRadioButtonId != -1) {
            privateClinicMBBSDoctor =
                (binding.distanceToNearestPrivateClinicWithAnMbbsDoctorRadioGroup
                    .findViewById<RadioButton>(binding.distanceToNearestPrivateClinicWithAnMbbsDoctorRadioGroup.checkedRadioButtonId)).text.toString()
        }
        householdSurveyModel.nearestPrivateClinicMBBSDoctor = privateClinicMBBSDoctor
    }

    private fun setDataToDistanceToNearestPrivateClinicMBBSDoctorUI() {
        val value1 = householdSurveyModel.nearestPrivateClinicMBBSDoctor
        value1?.let {
            when (it) {
                getString(R.string.within_5_kms) -> binding.pcWithin5Km.isChecked = true
                getString(R.string.five_to_ten_kms) -> binding.pc510Km.isChecked = true
                getString(R.string.ten_to_twenty_kms) -> binding.pc1020Km.isChecked = true
                getString(R.string.more_than_twenty_km) -> binding.pcMoreThan20Km.isChecked = true
            }
        }
    }

    private fun setupDistanceToNearestPrivateClinicAlternateMedicine() {
        var privateClinicAlternateMedicine = "-"
        if (binding.distanceToNearestPrivateClinicWithAlternate.checkedRadioButtonId != -1) {
             privateClinicAlternateMedicine =
                (binding.distanceToNearestPrivateClinicWithAlternate
                    .findViewById<RadioButton>(binding.distanceToNearestPrivateClinicWithAlternate.checkedRadioButtonId)).text.toString()
        }
        householdSurveyModel.nearestPrivateClinicAlternateMedicine = privateClinicAlternateMedicine
    }

    private fun setDataToDistanceToNearestPrivateClinicAlternateMedicineUI() {
        val value1 = householdSurveyModel.nearestPrivateClinicAlternateMedicine
        value1?.let {
            when (it) {
                getString(R.string.within_5_kms) -> binding.alternateWithin5Km.isChecked = true
                getString(R.string.five_to_ten_kms) -> binding.alternate510Km.isChecked = true
                getString(R.string.ten_to_twenty_kms) -> binding.alternate1020Km.isChecked = true
                getString(R.string.more_than_twenty_km) -> binding.alternateMoreThan20Km.isChecked =
                    true
            }
        }

    }

    private fun setupDistanceToNearestTertiaryCareFacility() {
        var tertiaryCareFacility = "-"
        if (binding.distanceToNearestTertiaryCareFacilityRadioGroup.checkedRadioButtonId != -1) {
            tertiaryCareFacility =
                (binding.distanceToNearestTertiaryCareFacilityRadioGroup
                    .findViewById<RadioButton>(binding.distanceToNearestTertiaryCareFacilityRadioGroup.checkedRadioButtonId)).text.toString()

        }
        householdSurveyModel.nearestTertiaryCareFacility = tertiaryCareFacility
    }

    private fun setDataToDistanceToNearestTertiaryCareFacilityUI() {
        val value1 = householdSurveyModel.nearestTertiaryCareFacility
        value1?.let {
            when (it) {
                getString(R.string.within_5_kms) -> binding.tertiaryWithin5Km.isChecked = true
                getString(R.string.five_to_ten_kms) -> binding.tertiary510Km.isChecked = true
                getString(R.string.ten_to_twenty_kms) -> binding.tertiary1020Km.isChecked = true
                getString(R.string.twenty_to_thirty_kms) -> binding.tertiary2030Km.isChecked = true
                getString(R.string.more_than_thirty_kms) -> binding.tertiaryMoreThan30Km.isChecked =
                    true
            }
        }
    }
    private fun showSavedDialog(){
        val dialogUtils = DialogUtils()
        dialogUtils.showCommonDialog(
            requireActivity(),
            R.drawable.ui2_complete_icon,
            getString(R.string.surveyDialogTitle),
            getString(R.string.surveyDialogMessage),
            true,
            resources.getString(R.string.ok),
            resources.getString(R.string.cancel)
        ) {
          /*  val intent = Intent(requireActivity(), PatientDetailActivity2::class.java)
            startActivity(intent)*/
            requireActivity().finish()
        }
    }
}