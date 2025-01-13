package org.intelehealth.app.ui.householdSurvey.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import org.intelehealth.app.R
import org.intelehealth.app.databinding.FragmentOneHouseholdSurveyBinding
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.ui.filter.FirstLetterUpperCaseInputFilter
import org.intelehealth.app.ui.householdSurvey.models.HouseholdSurveyModel
import org.intelehealth.app.ui.householdSurvey.utilities.HouseholdSurveyConstants.Companion.AVAILABLE_ACCEPTED
import org.intelehealth.app.ui.householdSurvey.utilities.HouseholdSurveyConstants.Companion.AVAILABLE_DEFERRED
import org.intelehealth.app.ui.householdSurvey.utilities.HouseholdSurveyConstants.Companion.NOT_AVAILABLE_ON_SECOND_VISIT
import org.intelehealth.app.ui.householdSurvey.utilities.HouseholdSurveyConstants.Companion.NOT_AVAILABLE_ON_SURVEY
import org.intelehealth.app.ui.householdSurvey.utilities.HouseholdSurveyConstants.Companion.NOT_AVAILABLE_ON_THIRD_VISIT
import org.intelehealth.app.ui.householdSurvey.utilities.HouseholdSurveyConstants.Companion.REFUSED_TO_PARTICIPATE
import org.intelehealth.app.utilities.DateAndTimeUtils
import org.intelehealth.app.utilities.HouseholdSurveyStage
import org.intelehealth.app.utilities.StringUtils
import org.intelehealth.app.utilities.extensions.addFilter

class FirstFragment : BaseHouseholdSurveyFragment(R.layout.fragment_one_household_survey) {
    private val TAG = "FirstFragment"
    private lateinit var binding: FragmentOneHouseholdSurveyBinding
    private var patientUuid: String? = null
    private var mHouseStructure: String? = null
    private var mResultVisit: String? = null
    private val mandatoryFields = mutableListOf<View>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentOneHouseholdSurveyBinding.bind(view)
        houseHoldViewModel.updatePatientStage(HouseholdSurveyStage.FIRST_SCREEN)

        initViews()
    }

    private fun radioButtonsClickListener() {
        // House Structure
        binding.rgStructureType.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbKucha -> {
                    mHouseStructure = "Kucha"
                }

                R.id.rbPucca -> {
                    mHouseStructure = "Pucca"
                }
            }
        }

        //Result of Visit
        binding.rgResultOfVisit.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbAvailableAndAccepted -> {
                    mResultVisit = "available and accepted"
                }

                R.id.rbAvailableAndDeferred -> {
                    mResultVisit = "available and deferred"
                }

                R.id.rbNotAvailableForSurvey -> {
                    mResultVisit = "Not available on Survey"
                }

                R.id.rbNotAvailableForSecondVisit -> {
                    mResultVisit = "Not available on second visit"
                }

                R.id.rbNotAvailableForThirdVisit -> {
                    mResultVisit = "Not available on third visit"
                }

                R.id.rbRefusedForVisit -> {
                    mResultVisit = "Refused to Participate"
                }
            }
            Log.d(TAG, "radioButtonsClickListener: mResultVisit : " + mResultVisit)
        }
    }

    private fun initViews() {
        val intent = requireActivity().intent
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid")
            Log.d(TAG, "initViews: patientUuid : "+patientUuid)
        }
        setClickListener()
        radioButtonsClickListener()
        applyFilter()
        mandatoryFields.addAll(listOf(binding.lblNameOfPrimaryRespondent,
            binding.textInputHouseholdNumber, binding.rgStructureType,  binding.rgResultOfVisit))

    }

    private fun setClickListener() {
        binding.btnFirstFragNext.setOnClickListener {
            savePatient()
        }
    }

    private fun savePatient() {
        if (!StringUtils.validateFields(mandatoryFields)) {
            Toast.makeText(context, R.string.fill_required_fields, Toast.LENGTH_SHORT).show()
            return
        }
        householdSurveyModel.apply {
            houseStructure = mHouseStructure
            resultOfVisit = mResultVisit
            namePrimaryRespondent = binding.textInputNameOfPrimaryRespondent.text?.toString()
            reportDateOfSurveyStarted = DateAndTimeUtils.currentDateTimeFormat()
            householdNumberOfSurvey = binding.textInputHouseholdNumber.text?.toString()

            houseHoldViewModel.updatedPatient(this)
            val patient = PatientDTO()
            patient.uuid = patientUuid
            saveAndNavigateToDetails(patient, householdSurveyModel)
        }
    }

  private fun saveAndNavigateToDetails(patient: PatientDTO,
                                       householdSurveyModel: HouseholdSurveyModel
  ) {
      houseHoldViewModel.savePatient("firstScreen",patient,householdSurveyModel).observe(viewLifecycleOwner) {
          it ?: return@observe
          houseHoldViewModel.handleResponse(it) { result -> if (result) navigateToDetails() }
      }
  }

    private fun navigateToDetails() {
        if (binding.rbRefusedForVisit.isChecked) {
            requireActivity().finish()
        }else{
            FirstFragmentDirections.actionOneToTwo().apply {
                findNavController().navigate(this)
            }
        }
    }

    override fun onPatientDataLoaded(householdSurveyModel: HouseholdSurveyModel) {
        super.onPatientDataLoaded(householdSurveyModel)
        Timber.d { Gson().toJson(householdSurveyModel) }
        setDataToUI();

        binding.patientSurveyAttributes = householdSurveyModel
        binding.isEditMode = houseHoldViewModel.isEditMode
    }

    private fun setDataToUI() {
        updateHouseStructureUI()
        updateSurveyStatus()
    }

    private fun updateHouseStructureUI() {
        //for house structure
        householdSurveyModel.houseStructure?.let {
            when {
                it.equals("Pucca", ignoreCase = true) -> binding.rbPucca.isChecked = true
                it.equals("Kucha", ignoreCase = true) -> binding.rbKucha.isChecked = true
                else -> {
                    binding.rbPucca.isChecked = false
                    binding.rbKucha.isChecked = false
                }
            }
        }
    }

    private fun updateSurveyStatus() {
        // Check and update the radio button based on the result value
        val result = householdSurveyModel.resultOfVisit
        result?.let {
            when {
                result.equals(
                    AVAILABLE_ACCEPTED,
                    ignoreCase = true
                ) -> setRadioButton(binding.rbAvailableAndAccepted)

                result.equals(
                    AVAILABLE_DEFERRED,
                    ignoreCase = true
                ) -> setRadioButton(binding.rbAvailableAndDeferred)

                result.equals(
                    NOT_AVAILABLE_ON_SURVEY,
                    ignoreCase = true
                ) -> setRadioButton(binding.rbNotAvailableForSurvey)

                result.equals(NOT_AVAILABLE_ON_SECOND_VISIT, ignoreCase = true) -> setRadioButton(
                    binding.rbNotAvailableForSecondVisit
                )

                result.equals(NOT_AVAILABLE_ON_THIRD_VISIT, ignoreCase = true) -> setRadioButton(
                    binding.rbNotAvailableForThirdVisit
                )

                result.equals(
                    REFUSED_TO_PARTICIPATE,
                    ignoreCase = true
                ) -> setRadioButton(binding.rbRefusedForVisit)
            }
        }
    }

    private fun setRadioButton(radioButton: RadioButton) {
        radioButton.isChecked = true
    }
    private fun applyFilter() {
        binding.textInputNameOfPrimaryRespondent.addFilter(FirstLetterUpperCaseInputFilter())
    }
}