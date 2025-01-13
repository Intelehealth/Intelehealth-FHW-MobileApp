package org.intelehealth.app.ui.householdSurvey.fragments

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import androidx.navigation.fragment.findNavController
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import org.intelehealth.app.R
import org.intelehealth.app.ui.householdSurvey.models.HouseholdSurveyModel
import org.intelehealth.app.databinding.FragmentFourthHouseholdSurveyBinding
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.utilities.HouseholdSurveyStage
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.StringUtils
import org.intelehealth.app.utilities.StringUtils.getCultivableLand
import org.intelehealth.app.utilities.StringUtils.getCultivableLandEdit
import java.util.Calendar
import java.util.Locale

class FourthFragment : BaseHouseholdSurveyFragment(R.layout.fragment_fourth_household_survey) {

    private lateinit var binding: FragmentFourthHouseholdSurveyBinding
    var selectedDate = Calendar.getInstance().timeInMillis
    private lateinit var updatedContext: Context
    private var patientUuid: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFourthHouseholdSurveyBinding.bind(view)
        houseHoldViewModel.updatePatientStage(HouseholdSurveyStage.FOURTH_SCREEN)
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

        binding.cultivableLandRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId != -1) {
                binding.llCultivatedLandAmount.visibility = View.VISIBLE
            } else {
                binding.llCultivatedLandAmount.visibility = View.GONE
            }
        }
    }

    private fun initViews() {
        val intent = requireActivity().intent
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid")
        }
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

        setDataToCultivatedLandUI()
        setDataToAnnualHouseholdIncomeUI()
        setDataToMonthlyExpenditureOnFoodUI()
        setDataToAnnualExpenditureOnHealthUI()
        setDataToAnnualExpenditureOnEducationUI()
        setDataToAnnualExpenditureOnClothingUI()
        setDataToMonthlyExpenditureOnIntoxicants()
        setDataToHouseholdBPLCardUI()
        setDataToHouseholdAntodayaCardUI()
        setDataToHouseholdRSBYCardUI()
        setDataToHouseholdMGNREGACardUI()
    }

    private fun savePatient() {
        householdSurveyModel.apply {
            setupCultivatedLand()
            setupAnnualHouseholdIncome()
            setupMonthlyExpenditureOnFood()
            setupAnnualExpenditureOnHealth()
            setupAnnualExpenditureOnEducation()
            setupAnnualExpenditureOnClothing()
            setupMonthlyExpenditureOnIntoxicants()
            setupForHouseholdBPLCard()
            setupForHouseholdAntodayaCard()
            setupForHouseholdRSBYCard()
            setupForHouseholdMGNREGACard()

            Log.d("devchdbsave4", "savePatient: householdSurveyModel : " + householdSurveyModel)
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
            "fourthScreen", patient,
            householdSurveyModel
        ).observe(viewLifecycleOwner) {
            it ?: return@observe
            houseHoldViewModel.handleResponse(it) { result -> if (result) navigateToDetails() }
        }
    }

    private fun navigateToDetails() {
        FourthFragmentDirections.actionFourToFive().apply {
            findNavController().navigate(this)
        }
    }

    private fun setupCultivatedLand() {
        var cultivatedLand = "-"
        if (binding.cultivableLandRadioGroup.checkedRadioButtonId != -1) {
            val selectedRadioButton = binding.cultivableLandRadioGroup.findViewById<RadioButton>(
                binding.cultivableLandRadioGroup.checkedRadioButtonId
            )

            cultivatedLand = binding.textInputCultivatedLandOwnedByHousehold.text.toString()
            cultivatedLand = "${StringUtils.getValue(cultivatedLand)} " +
                    getCultivableLand(
                        selectedRadioButton.text.toString().trim(),
                        requireContext(),
                        SessionManager(requireActivity()).appLanguage
                    )
        }
        householdSurveyModel.householdCultivableLand = cultivatedLand

    }

    private fun setDataToCultivatedLandUI() {
        val value1 = householdSurveyModel.householdCultivableLand
        if (!value1.isNullOrEmpty()) {
            val splitString = value1.split(" ").toMutableList()

            // Ensure there are at least two elements in splitString
            if (splitString.size > 1) {
                splitString[1] = getCultivableLandEdit(
                    splitString[1],
                    requireContext(),
                    SessionManager(requireActivity()).appLanguage
                )

                when {
                    splitString[1].equals(
                        getString(R.string.hectare),
                        ignoreCase = true
                    ) -> binding.hectareRadioButton.isChecked = true

                    splitString[1].equals(
                        getString(R.string.acre),
                        ignoreCase = true
                    ) -> binding.acreRadioButton.isChecked = true

                    splitString[1].equals(
                        getString(R.string.bigha),
                        ignoreCase = true
                    ) -> binding.bighaRadioButton.isChecked = true

                    splitString[1].equals(
                        getString(R.string.gunta),
                        ignoreCase = true
                    ) -> binding.guntaRadioButton.isChecked = true

                    else -> {
                        binding.hectareRadioButton.isChecked = false
                        binding.acreRadioButton.isChecked = false
                        binding.bighaRadioButton.isChecked = false
                        binding.guntaRadioButton.isChecked = false
                    }
                }
            } else {
                /* // If splitString doesn't have enough elements, reset all radio buttons
                 binding.hectareRadioButton.isChecked = false
                 binding.acreRadioButton.isChecked = false
                 binding.bighaRadioButton.isChecked = false
                 binding.guntaRadioButton.isChecked = false*/
            }

            // If the first part of splitString is not "-", set it to the EditText
            if (splitString.isNotEmpty() && !splitString[0].equals("-", ignoreCase = true)) {
                binding.textInputCultivatedLandOwnedByHousehold.setText(splitString[0])
            }
        }
    }

    private fun setupAnnualHouseholdIncome() {
         var averageAnnualHouseholdIncome = "-"
        if (binding.averageAnnualHouseholdIncomeRadioGroup.checkedRadioButtonId != -1) {
            averageAnnualHouseholdIncome = (binding.averageAnnualHouseholdIncomeRadioGroup
                .findViewById<RadioButton>(binding.averageAnnualHouseholdIncomeRadioGroup.checkedRadioButtonId)).text.toString()
        }
        householdSurveyModel.averageAnnualHouseholdIncome = averageAnnualHouseholdIncome

    }

    private fun setDataToAnnualHouseholdIncomeUI() {
        val value1 = householdSurveyModel.averageAnnualHouseholdIncome
        value1?.let {
            when (it.lowercase()) {
                getString(R.string.zero_thirty_thousand).lowercase() -> binding.annualHouseholdIncome0.isChecked =
                    true

                getString(R.string.thirty_fifty_thousand).lowercase() -> binding.annualHouseholdIncome1.isChecked =
                    true

                getString(R.string.fifty_thousand_one_lakh).lowercase() -> binding.annualHouseholdIncome2.isChecked =
                    true

                getString(R.string.one_lakh_two_lakh_fifty_thousand).lowercase() -> binding.annualHouseholdIncome4.isChecked =
                    true

                getString(R.string.more_than_two_lakh_fifty_thousand).lowercase() -> binding.annualHouseholdIncome4.isChecked =
                    true
            }
        }

    }

    private fun setupMonthlyExpenditureOnFood() {
        var monthlyFoodExpenditure = "-"
        if (binding.monthlyFoodExpenditureRadioGroup.checkedRadioButtonId != -1) {
            monthlyFoodExpenditure = (binding.monthlyFoodExpenditureRadioGroup
                .findViewById<RadioButton>(binding.monthlyFoodExpenditureRadioGroup.checkedRadioButtonId)).text.toString()
        }
        householdSurveyModel.monthlyFoodExpenditure = monthlyFoodExpenditure
    }

    private fun setDataToMonthlyExpenditureOnFoodUI() {
        val value1 = householdSurveyModel.monthlyFoodExpenditure
        value1?.let {
            when (it.lowercase()) {
                getString(R.string.zero_fifteen_hundred).lowercase() -> binding.monthlyFoodExpense0.isChecked =
                    true

                getString(R.string.fifteen_twenty_five_hundred).lowercase() -> binding.monthlyFoodExpense1.isChecked =
                    true

                getString(R.string.twenty_five_hundred_five_thousand).lowercase() -> binding.monthlyFoodExpense2.isChecked =
                    true

                getString(R.string.five_ten_thousand).lowercase() -> binding.monthlyFoodExpense3.isChecked =
                    true

                getString(R.string.more_than_ten_thousand).lowercase() -> binding.monthlyFoodExpense4.isChecked =
                    true
            }
        }
    }

    private fun setupAnnualExpenditureOnHealth() {
        var annualHealthExpenditure = "-"
        if (binding.annualHealthExpenditureRadioGroup.checkedRadioButtonId != -1) {
            annualHealthExpenditure = (binding.annualHealthExpenditureRadioGroup
                .findViewById<RadioButton>(binding.annualHealthExpenditureRadioGroup.checkedRadioButtonId)).text.toString()
        }
        householdSurveyModel.annualHealthExpenditure = annualHealthExpenditure
    }

    private fun setDataToAnnualExpenditureOnHealthUI() {
        val value1 = householdSurveyModel.annualHealthExpenditure
        value1?.let {
            when (it.lowercase()) {
                getString(R.string.zero_five_thousand).lowercase() -> binding.healthExpense0.isChecked =
                    true

                getString(R.string.five_thousand_one_ten_thousand).lowercase() -> binding.healthExpense1.isChecked =
                    true

                getString(R.string.ten_thousand_one_twenty_thousand).lowercase() -> binding.healthExpense2.isChecked =
                    true

                getString(R.string.twenty_thousand_one_thirty_thousand).lowercase() -> binding.healthExpense3.isChecked =
                    true

                getString(R.string.more_than_thirty_thousand).lowercase() -> binding.greaterThanThirtyThousandRadioButton.isChecked =
                    true
            }
        }
    }

    private fun setupAnnualExpenditureOnEducation() {
        var annualEducationExpenditure = "-"
        if (binding.annualEducationExpenditureRadioGroup.checkedRadioButtonId != -1) {
            annualEducationExpenditure = (binding.annualEducationExpenditureRadioGroup
                .findViewById<RadioButton>(binding.annualEducationExpenditureRadioGroup.checkedRadioButtonId)).text.toString()
        }
        householdSurveyModel.annualEducationExpenditure = annualEducationExpenditure
    }

    private fun setDataToAnnualExpenditureOnEducationUI() {
        val value1 = householdSurveyModel.annualEducationExpenditure
        value1?.let {
            when (it.lowercase()) {
                getString(R.string.zero).lowercase() -> binding.annualEducationExpense0.isChecked =
                    true

                getString(R.string.zero_ten_thousand).lowercase() -> binding.annualEducationExpense1.isChecked =
                    true

                getString(R.string.ten_twenty_thousand).lowercase() -> binding.annualEducationExpense2.isChecked =
                    true

                getString(R.string.twenty_forty_thousand).lowercase() -> binding.annualEducationExpense3.isChecked =
                    true

                getString(R.string.forty_thousand_one_lakh).lowercase() -> binding.annualEducationExpense4.isChecked =
                    true

                getString(R.string.more_than_one_lakh).lowercase() -> binding.annualEducationExpense5.isChecked =
                    true
            }
        }
    }

    private fun setupAnnualExpenditureOnClothing() {
        var annualClothingExpenditure = "-"
        if (binding.annualClothingExpenditureRadioGroup.checkedRadioButtonId != -1) {
            annualClothingExpenditure = (binding.annualClothingExpenditureRadioGroup
                .findViewById<RadioButton>(binding.annualClothingExpenditureRadioGroup.checkedRadioButtonId)).text.toString()
        }
        householdSurveyModel.annualClothingExpenditure = annualClothingExpenditure
    }

    private fun setDataToAnnualExpenditureOnClothingUI() {
        val value1 = householdSurveyModel.annualClothingExpenditure
        value1?.let {
            when (it.lowercase()) {
                getString(R.string.zero_ten_thousand).lowercase() -> binding.annualClothingExpense0.isChecked =
                    true

                getString(R.string.ten_twenty_thousand).lowercase() -> binding.annualClothingExpense1.isChecked =
                    true

                getString(R.string.twenty_forty_thousand).lowercase() -> binding.annualClothingExpense2.isChecked =
                    true

                getString(R.string.forty_thousand_one_lakh).lowercase() -> binding.annualClothingExpense3.isChecked =
                    true

                getString(R.string.more_than_one_lakh).lowercase() -> binding.annualClothingExpense4.isChecked =
                    true
            }
        }
    }

    private fun setupMonthlyExpenditureOnIntoxicants() {
        var monthlyIntoxicantsExpenditure = "-"
        if (binding.monthlyIntoxicantsExpenditureRadioGroup.checkedRadioButtonId != -1) {
            monthlyIntoxicantsExpenditure = (binding.monthlyIntoxicantsExpenditureRadioGroup
                .findViewById<RadioButton>(binding.monthlyIntoxicantsExpenditureRadioGroup.checkedRadioButtonId)).text.toString()
        }
        householdSurveyModel.monthlyIntoxicantsExpenditure = monthlyIntoxicantsExpenditure
    }

    private fun setDataToMonthlyExpenditureOnIntoxicants() {
        val value1 = householdSurveyModel.monthlyIntoxicantsExpenditure
        value1?.let {
            when (it.lowercase()) {
                getString(R.string.one_to_six_hundred).lowercase() -> binding.intoxicExpense0.isChecked =
                    true

                getString(R.string.six_hundred_one_thousand).lowercase() -> binding.intoxicExpense1.isChecked =
                    true

                getString(R.string.thousand_to_fifteen_hundred).lowercase() -> binding.intoxicExpense2.isChecked =
                    true

                getString(R.string.fifteen_hundred_to_twenty_five_hundred).lowercase() -> binding.intoxicExpense3.isChecked =
                    true

                getString(R.string.more_than_twenty_five_hundred).lowercase() -> binding.intoxicExpense4.isChecked =
                    true
            }
        }
    }

    private fun setupForHouseholdBPLCard() {
        var householdBPLCardStatus = "-"
        if (binding.bplCardCouponRadioGroup.checkedRadioButtonId != -1) {
            householdBPLCardStatus = (binding.bplCardCouponRadioGroup
                .findViewById<RadioButton>(binding.bplCardCouponRadioGroup.checkedRadioButtonId)).text.toString()
        }
        householdSurveyModel.householdBPLCardStatus = householdBPLCardStatus
    }

    private fun setDataToHouseholdBPLCardUI() {
        val value1 = householdSurveyModel.householdBPLCardStatus
        value1?.let {
            when (it.lowercase()) {
                getString(R.string.yes_card_seen).lowercase() -> binding.bplYesCardSeen.isChecked =
                    true

                getString(R.string.yes_card_not_seen).lowercase() -> binding.bplYesCardNotSeen.isChecked =
                    true

                getString(R.string.no_card).lowercase() -> binding.bplNoCard.isChecked = true
                getString(R.string.DO_NOT_KNOW).lowercase() -> binding.bplDoNotKnow.isChecked = true
            }
        }
    }

    private fun setupForHouseholdAntodayaCard() {
        var householdAntodayaCardStatus = "-"
        if (binding.antodayaCardCouponRadioGroup.checkedRadioButtonId != -1) {
            householdAntodayaCardStatus = (binding.antodayaCardCouponRadioGroup
                .findViewById<RadioButton>(binding.antodayaCardCouponRadioGroup.checkedRadioButtonId)).text.toString()
        }
        householdSurveyModel.householdAntodayaCardStatus = householdAntodayaCardStatus
    }

    private fun setDataToHouseholdAntodayaCardUI() {
        val value1 = householdSurveyModel.householdAntodayaCardStatus
        value1?.let {
            when {
                it.equals(
                    getString(R.string.yes_card_seen),
                    ignoreCase = true
                ) -> binding.antodayaYesCardSeen.isChecked = true

                it.equals(
                    getString(R.string.yes_card_not_seen),
                    ignoreCase = true
                ) -> binding.antodayaYesCardNotSeen.isChecked = true

                it.equals(
                    getString(R.string.no_card),
                    ignoreCase = true
                ) -> binding.antodayaNoCard.isChecked = true

                it.equals(
                    getString(R.string.DO_NOT_KNOW),
                    ignoreCase = true
                ) -> binding.antodayaDoNotKnow.isChecked = true
            }
        }
    }

    private fun setupForHouseholdRSBYCard() {
        var householdRSBYCardStatus = "-"
        if (binding.rsbyCardRadioGroup.checkedRadioButtonId != -1) {
            householdRSBYCardStatus = (binding.rsbyCardRadioGroup
                .findViewById<RadioButton>(binding.rsbyCardRadioGroup.checkedRadioButtonId)).text.toString()
        }
        householdSurveyModel.householdRSBYCardStatus = householdRSBYCardStatus
    }

    private fun setDataToHouseholdRSBYCardUI() {
        val value1 = householdSurveyModel.householdRSBYCardStatus
        value1?.let {
            when {
                it.equals(
                    getString(R.string.yes_card_seen),
                    ignoreCase = true
                ) -> binding.rsbyYesCardSeen.isChecked = true

                it.equals(
                    getString(R.string.yes_card_not_seen),
                    ignoreCase = true
                ) -> binding.rsbyYesCardNotSeen.isChecked = true

                it.equals(
                    getString(R.string.no_card),
                    ignoreCase = true
                ) -> binding.rsbyNoCard.isChecked = true

                it.equals(
                    getString(R.string.DO_NOT_KNOW),
                    ignoreCase = true
                ) -> binding.rsbyDoNotKnow.isChecked = true
            }
        }
    }

    private fun setupForHouseholdMGNREGACard() {
        var householdAntodayaCardStatus = "-"
        if (binding.mgnregaCardRadioGroup.checkedRadioButtonId != -1) {
            householdAntodayaCardStatus = (binding.mgnregaCardRadioGroup
                .findViewById<RadioButton>(binding.mgnregaCardRadioGroup.checkedRadioButtonId)).text.toString()
        }
        householdSurveyModel.householdMGNREGACardStatus = householdAntodayaCardStatus
    }

    private fun setDataToHouseholdMGNREGACardUI() {
        val value1 = householdSurveyModel.householdMGNREGACardStatus
        value1?.let {
            when {
                it.equals(
                    getString(R.string.yes_card_seen),
                    ignoreCase = true
                ) -> binding.mgnregaYesCardSeen.isChecked = true

                it.equals(
                    getString(R.string.yes_card_not_seen),
                    ignoreCase = true
                ) -> binding.mgnregaYesCardNotSeen.isChecked = true

                it.equals(
                    getString(R.string.no_card),
                    ignoreCase = true
                ) -> binding.mgnregaNoCard.isChecked = true

                it.equals(
                    getString(R.string.DO_NOT_KNOW),
                    ignoreCase = true
                ) -> binding.mgnregaDoNotKnow.isChecked = true
            }
        }
    }

}