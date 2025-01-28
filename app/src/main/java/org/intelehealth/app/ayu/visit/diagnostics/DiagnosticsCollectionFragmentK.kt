package org.intelehealth.app.ayu.visit.diagnostics

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.intelehealth.app.R
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.ayu.visit.VisitCreationActionListener
import org.intelehealth.app.ayu.visit.VisitCreationActivity
import org.intelehealth.app.ayu.visit.diagnostics.viewmodel.DiagnosticsCollectionViewModel
import org.intelehealth.app.ayu.visit.model.CommonVisitData
import org.intelehealth.app.database.dao.EncounterDAO
import org.intelehealth.app.database.dao.ObsDAO
import org.intelehealth.app.databinding.FragmentDiagnosticsCollectionBinding
import org.intelehealth.app.models.DiagnosticsModel
import org.intelehealth.app.models.dto.ObsDTO
import org.intelehealth.app.utilities.ConfigUtils
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.UuidDictionary
import org.intelehealth.app.utilities.exception.DAOException
import org.intelehealth.config.room.entity.Diagnostics

class DiagnosticsCollectionFragmentK : Fragment(), View.OnClickListener {

    private lateinit var viewModel: DiagnosticsCollectionViewModel
    private lateinit var mBinding: FragmentDiagnosticsCollectionBinding
    private lateinit var results: DiagnosticsModel
    var mIsEditMode: Boolean = false
    lateinit var patientUuid: String
    lateinit var visitUuid: String
    lateinit var encounterVitals: String
    lateinit var encounterAdultIntials: String
    lateinit var EncounterAdultInitial_LatestVisit: String
    lateinit var state: String
    lateinit var patientName: String
    lateinit var patientGender: String
    var intentTag: String? = null
    private var mActionListener: VisitCreationActionListener? = null
    private var sessionManager: SessionManager? = null
    private var configUtils: ConfigUtils? = null

    companion object {
        @JvmStatic
        fun newInstance(
            commonVisitData: CommonVisitData,
            isEditMode: Boolean,
            diagnosticsModel: DiagnosticsModel
        ): DiagnosticsCollectionFragmentK {
            val fragment = DiagnosticsCollectionFragmentK()

            fragment.mIsEditMode = isEditMode
            fragment.results = diagnosticsModel

            fragment.patientUuid = commonVisitData.patientUuid
            fragment.visitUuid = commonVisitData.visitUuid
            fragment.encounterVitals = commonVisitData.encounterUuidVitals
            fragment.encounterAdultIntials = commonVisitData.encounterUuidAdultIntial
            fragment.EncounterAdultInitial_LatestVisit =
                commonVisitData.encounterAdultInitialLatestVisit
            fragment.state = commonVisitData.state
            fragment.patientName = commonVisitData.patientName
            fragment.patientGender = commonVisitData.patientGender
            fragment.intentTag = commonVisitData.intentTag

            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_diagnostics_collection,
            container,
            false
        )

        // Hide error messages
        mBinding.tvGlucoseRandomError.visibility = View.GONE
        mBinding.tvGlucoseFastingError.visibility = View.GONE
        //mBinding.tvNonFastingGlucoseError.visibility = View.GONE
        mBinding.etvPostPrandialError.visibility = View.GONE
        mBinding.etvUricAcidError.visibility = View.GONE
        mBinding.etvCholestrolError.visibility = View.GONE
        mBinding.tvHemoglobinError.visibility = View.GONE


        //mBinding.etvNonFastingGlucose.addTextChangedListener(MyTextWatcher(mBinding.etvNonFastingGlucose))
        mBinding.etvGlucoseRandom.addTextChangedListener(MyTextWatcher(mBinding.etvGlucoseRandom))
        mBinding.etvGlucoseFasting.addTextChangedListener(MyTextWatcher(mBinding.etvGlucoseFasting))
        mBinding.etvPostPrandial.addTextChangedListener(MyTextWatcher(mBinding.etvPostPrandial))
        mBinding.etvHemoglobin.addTextChangedListener(MyTextWatcher(mBinding.etvHemoglobin))
        mBinding.etvUricAcid.addTextChangedListener(MyTextWatcher(mBinding.etvUricAcid))
        mBinding.etvCholesterol.addTextChangedListener(MyTextWatcher(mBinding.etvCholesterol))

        mBinding.btnSubmit.setOnClickListener(this)
        mBinding.btnSubmit.isClickable = true

     /*   if (mIsEditMode && results == null) {
            viewModel.loadSavedData(encounterVitals)
        }*/

        if (!mIsEditMode) {
            results = DiagnosticsModel() // Initialize a new instance for fresh data collection
        } else if (results == null) {
            // Ensure we have a valid DiagnosticsModel, especially for editing
            results = DiagnosticsModel()
            viewModel.loadSavedData(encounterVitals)
        }
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(DiagnosticsCollectionViewModel::class.java)


        // Other UI setup
        mBinding.btnSubmit.setOnClickListener {
            viewModel.saveData()
        }

        // Initialize data and edit mode
        encounterVitals?.let { viewModel.loadSavedData(it) }
        viewModel.setEditMode(mIsEditMode)
    }

    fun isDataReadyForSaving(): Boolean {
        try {
            // Initialize results if it's null
            if (results == null) {
                results = DiagnosticsModel()
            }

            // Set values from the UI components
            results.apply {
                bloodGlucoseRandom = mBinding.etvGlucoseRandom.text.toString()
                bloodGlucoseFasting = mBinding.etvGlucoseFasting.text.toString()
                //bloodGlucoseNonFasting = mBinding.etvNonFastingGlucose.text.toString()
                bloodGlucosePostPrandial = mBinding.etvPostPrandial.text.toString()
                hemoglobin = mBinding.etvHemoglobin.text.toString()
                uricAcid = mBinding.etvUricAcid.text.toString()
                cholesterol = mBinding.etvCholesterol.text.toString()
            }

            // Instantiate DAOs
            val obsDAO = ObsDAO()
            val encounterDAO = EncounterDAO()

            // Create a new ObsDTO object
            val obsDTO = ObsDTO()

            fun processDiagnostic(diagnostic: Diagnostics?, conceptUuid: String, value: String) {
                if ((diagnostic?.isMandatory == true) || value.isNotEmpty()) {
                    obsDTO.apply {
                        conceptuuid = conceptUuid
                        encounteruuid = encounterVitals
                        creator = SessionManager(requireActivity()).creatorID
                        this.value = value
                        if (diagnostic != null) {
                            uuid = obsDAO.getObsuuid(encounterVitals, diagnostic.uuid)
                        }
                    }
                    try {
                        if (activity?.intent?.action == "edit") {
                            obsDAO.updateObs(obsDTO)
                        } else {
                            obsDAO.insertObs(obsDTO)
                        }
                    } catch (e: DAOException) {
                        FirebaseCrashlytics.getInstance().recordException(e)
                    }
                }
            }

            if (activity?.intent?.action == "edit") {
                ObsDAO.deleteExistingVitalsDataIfExists(visitUuid)

                results.bloodGlucoseRandom?.let {
                    processDiagnostic(
                        mBinding.llGlucoseRandomContainer.tag as? Diagnostics,
                        UuidDictionary.BLOOD_GLUCOSE_RANDOM,
                        it
                    )
                }
                results.bloodGlucoseFasting?.let {
                    processDiagnostic(
                        mBinding.llGlusoseFastingContainer.tag as? Diagnostics,
                        UuidDictionary.BLOOD_GLUCOSE_FASTING,
                        it
                    )
                }
                results.bloodGlucosePostPrandial?.let {
                    processDiagnostic(
                        mBinding.llPostPrandialContainer.tag as? Diagnostics,
                        UuidDictionary.BLOOD_GLUCOSE_POST_PRANDIAL,
                        it
                    )
                }
              /*  results.bloodGlucoseNonFasting?.let {
                    processDiagnostic(
                        mBinding.llNonFastingContainer.tag as? Diagnostics,
                        UuidDictionary.BLOOD_GLUCOSE,
                        it
                    )
                }*/
                results.uricAcid?.let {
                    processDiagnostic(
                        mBinding.llUricAcidContainer.tag as? Diagnostics,
                        UuidDictionary.URIC_ACID,
                        it
                    )
                }
                results.cholesterol?.let {
                    processDiagnostic(
                        mBinding.llCholestrolContainer.tag as? Diagnostics,
                        UuidDictionary.TOTAL_CHOLESTEROL,
                        it
                    )
                }
                results.hemoglobin?.let {
                    processDiagnostic(
                        mBinding.llHemoglobinContainer.tag as? Diagnostics,
                        UuidDictionary.HEMOGLOBIN,
                        it
                    )
                }

                try {
                    encounterDAO.apply {
                        updateEncounterSync("false", encounterVitals)
                        updateEncounterModifiedDate(encounterVitals)
                    }
                } catch (e: DAOException) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                }
            } else {
                ObsDAO.deleteExistingVitalsDataIfExists(visitUuid)

                results.bloodGlucoseRandom?.let {
                    processDiagnostic(
                        mBinding.llGlucoseRandomContainer.tag as? Diagnostics,
                        UuidDictionary.BLOOD_GLUCOSE_RANDOM,
                        it
                    )
                }
                results.bloodGlucoseFasting?.let {
                    processDiagnostic(
                        mBinding.llGlusoseFastingContainer.tag as? Diagnostics,
                        UuidDictionary.BLOOD_GLUCOSE_FASTING,
                        it
                    )
                }
               /* results.bloodGlucoseNonFasting?.let {
                    processDiagnostic(
                        mBinding.llNonFastingContainer.tag as? Diagnostics,
                        UuidDictionary.BLOOD_GLUCOSE,
                        it
                    )
                }*/
                results.bloodGlucosePostPrandial?.let {
                    processDiagnostic(
                        mBinding.llPostPrandialContainer.tag as? Diagnostics,
                        UuidDictionary.BLOOD_GLUCOSE_POST_PRANDIAL,
                        it
                    )
                }
                results.hemoglobin?.let {
                    processDiagnostic(
                        mBinding.llHemoglobinContainer.tag as? Diagnostics,
                        UuidDictionary.HEMOGLOBIN,
                        it
                    )
                }
                results.cholesterol?.let {
                    processDiagnostic(
                        mBinding.llCholestrolContainer.tag as? Diagnostics,
                        UuidDictionary.TOTAL_CHOLESTEROL,
                        it
                    )
                }
                results.uricAcid?.let {
                    processDiagnostic(
                        mBinding.llUricAcidContainer.tag as? Diagnostics,
                        UuidDictionary.URIC_ACID,
                        it
                    )
                }
            }

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle exception, e.g., show a Snackbar
            // Snackbar.make(findViewById(R.id.cl_table), R.string.error_non_decimal_no_added, Snackbar.LENGTH_LONG).setAction("Action", null).show()
            return false
        }
    }

    inner class MyTextWatcher(private val editText: EditText) : TextWatcher {

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // No operation
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // No operation
        }

        override fun afterTextChanged(editable: Editable?) {
            val value = editable?.toString()?.trim()
            if (value == ".") {
                editText.setText("")
                return
            }
            val isValid = isValidaForm()
            //setDisabledSubmit(!isValid)
        }


    }

    override fun onClick(v: View?) {
        if (isValidaForm()) {
            isDataReadyForSaving()
            mActionListener?.onProgress(100)
            mActionListener?.onFormSubmitted(
                VisitCreationActivity.STEP_2_DIAGNOSTICS_SUMMARY,
                mIsEditMode,
                results
            )
        }
    }

    private fun isValidaForm(): Boolean {
        var isValid = true

        val bloodGlucoseRandom = mBinding.etvGlucoseRandom.text.toString().trim()
        if (bloodGlucoseRandom.isNotEmpty()) {
            val randomValue = bloodGlucoseRandom.toDoubleOrNull()
            if (randomValue != null && (randomValue > AppConstants.MAXIMUM_GLUCOSE_RANDOM.toDouble() ||
                        randomValue < AppConstants.MINIMUM_GLUCOSE_RANDOM.toDouble())
            ) {
                mBinding.tvGlucoseRandomError.text = getString(
                    R.string.glucose_random_error,
                    AppConstants.MINIMUM_GLUCOSE_RANDOM,
                    AppConstants.MAXIMUM_GLUCOSE_RANDOM
                )
                mBinding.tvGlucoseRandomError.visibility = View.VISIBLE
                mBinding.etvGlucoseRandom.requestFocus()
                mBinding.etvGlucoseRandom.setBackgroundResource(R.drawable.input_field_error_bg_ui2)
                isValid = false
            } else {
                mBinding.tvGlucoseRandomError.visibility = View.GONE
                mBinding.etvGlucoseRandom.setBackgroundResource(R.drawable.bg_input_fieldnew)
            }
        }

        val glucoseFasting = mBinding.etvGlucoseFasting.text.toString().trim()
        if (glucoseFasting.isNotEmpty()) {
            val fastingValue = glucoseFasting.toDoubleOrNull()
            if (fastingValue != null && (fastingValue > AppConstants.MAXIMUM_GLUCOSE_FASTING.toDouble() ||
                        fastingValue < AppConstants.MINIMUM_GLUCOSE_FASTING.toDouble())
            ) {
                mBinding.tvGlucoseFastingError.text = getString(
                    R.string.glucose_fasting_error,
                    AppConstants.MINIMUM_GLUCOSE_FASTING,
                    AppConstants.MAXIMUM_GLUCOSE_FASTING
                )
                mBinding.tvGlucoseFastingError.visibility = View.VISIBLE
                mBinding.etvGlucoseFasting.requestFocus()
                mBinding.etvGlucoseFasting.setBackgroundResource(R.drawable.input_field_error_bg_ui2)
                isValid = false
            } else {
                mBinding.tvGlucoseFastingError.visibility = View.GONE
                mBinding.etvGlucoseFasting.setBackgroundResource(R.drawable.bg_input_fieldnew)
            }
        }

       /* val nonFastingGlucose = mBinding.etvNonFastingGlucose.text.toString().trim()
        if (nonFastingGlucose.isNotEmpty()) {
            val nonFastingValue = nonFastingGlucose.toDoubleOrNull()
            if (nonFastingValue != null && (nonFastingValue > AppConstants.MAXIMUM_GLUCOSE_NON_FASTING.toDouble() ||
                        nonFastingValue < AppConstants.MINIMUM_GLUCOSE_NON_FASTING.toDouble())
            ) {
                mBinding.tvNonFastingGlucoseError.text = getString(
                    R.string.glucose_non_fasting_error,
                    AppConstants.MINIMUM_GLUCOSE_NON_FASTING,
                    AppConstants.MAXIMUM_GLUCOSE_NON_FASTING
                )
                mBinding.tvNonFastingGlucoseError.visibility = View.VISIBLE
                mBinding.etvNonFastingGlucose.requestFocus()
                mBinding.etvNonFastingGlucose.setBackgroundResource(R.drawable.input_field_error_bg_ui2)
                isValid = false
            } else {
                mBinding.tvNonFastingGlucoseError.visibility = View.GONE
                mBinding.etvNonFastingGlucose.setBackgroundResource(R.drawable.bg_input_fieldnew)
            }
        }
*/
        val postPrandial = mBinding.etvPostPrandial.text.toString().trim()
        if (postPrandial.isNotEmpty()) {
            val postPrandialValue = postPrandial.toDoubleOrNull()
            if (postPrandialValue != null && (postPrandialValue > AppConstants.MAXIMUM_GLUCOSE_POST_PRANDIAL.toDouble() ||
                        postPrandialValue < AppConstants.MINIMUM_GLUCOSE_POST_PRANDIAL.toDouble())
            ) {
                mBinding.etvPostPrandial.setText(
                    getString(
                        R.string.post_prandial_error,
                        AppConstants.MINIMUM_GLUCOSE_POST_PRANDIAL,
                        AppConstants.MAXIMUM_GLUCOSE_POST_PRANDIAL
                    )
                )
                mBinding.etvPostPrandialError.visibility = View.VISIBLE
                mBinding.etvPostPrandial.requestFocus()
                mBinding.etvPostPrandial.setBackgroundResource(R.drawable.input_field_error_bg_ui2)
                isValid = false
            } else {
                mBinding.etvPostPrandialError.visibility = View.GONE
                mBinding.etvPostPrandial.setBackgroundResource(R.drawable.bg_input_fieldnew)
            }
        }

        val hemoglobin = mBinding.etvHemoglobin.text.toString().trim()
        if (hemoglobin.isNotEmpty()) {
            val hemoglobinValue = hemoglobin.toDoubleOrNull()
            if (hemoglobinValue != null && (hemoglobinValue > AppConstants.MAXIMUM_HEMOGLOBIN.toDouble() ||
                        hemoglobinValue < AppConstants.MINIMUM_HEMOGLOBIN.toDouble())
            ) {
                mBinding.tvHemoglobinError.text = getString(
                    R.string.hemoglobin_error,
                    AppConstants.MINIMUM_HEMOGLOBIN,
                    AppConstants.MAXIMUM_HEMOGLOBIN
                )
                mBinding.tvHemoglobinError.visibility = View.VISIBLE
                mBinding.etvHemoglobin.requestFocus()
                mBinding.etvHemoglobin.setBackgroundResource(R.drawable.input_field_error_bg_ui2)
                isValid = false
            } else {
                mBinding.tvHemoglobinError.visibility = View.GONE
                mBinding.etvHemoglobin.setBackgroundResource(R.drawable.bg_input_fieldnew)
            }
        }

        val uricAcid = mBinding.etvUricAcid.text.toString().trim()
        if (uricAcid.isNotEmpty()) {
            val uricAcidValue = uricAcid.toDoubleOrNull()
            if (uricAcidValue != null && (uricAcidValue > AppConstants.MAXIMUM_URIC_ACID.toDouble() ||
                        uricAcidValue < AppConstants.MINIMUM_URIC_ACID.toDouble())
            ) {
                mBinding.etvUricAcidError.text = getString(
                    R.string.uric_acid_error,
                    AppConstants.MINIMUM_URIC_ACID,
                    AppConstants.MAXIMUM_URIC_ACID
                )
                mBinding.etvUricAcidError.visibility = View.VISIBLE
                mBinding.etvUricAcid.requestFocus()
                mBinding.etvUricAcid.setBackgroundResource(R.drawable.input_field_error_bg_ui2)
                isValid = false
            } else {
                mBinding.etvUricAcidError.visibility = View.GONE
                mBinding.etvUricAcid.setBackgroundResource(R.drawable.bg_input_fieldnew)
            }
        }

        val totalCholesterol = mBinding.etvCholesterol.text.toString().trim()
        if (totalCholesterol.isEmpty()) {
            if (mBinding.llCholestrolContainer.tag != null && (mBinding.llCholestrolContainer.tag as Diagnostics).isMandatory) {
                mBinding.etvCholesterol.setText(getString(R.string.error_field_required))
                mBinding.etvCholestrolError.visibility = View.VISIBLE
                mBinding.etvCholesterol.requestFocus()
                mBinding.etvCholesterol.setBackgroundResource(R.drawable.input_field_error_bg_ui2)
                isValid = false
            } else {
                mBinding.etvCholestrolError.visibility = View.GONE
                mBinding.etvCholesterol.setBackgroundResource(R.drawable.bg_input_fieldnew)
            }
        } else {
            val cholesterolValue = totalCholesterol.toDoubleOrNull()
            if (cholesterolValue != null && (cholesterolValue > AppConstants.MAXIMUM_TOTAL_CHOLSTEROL.toDouble() ||
                        cholesterolValue < AppConstants.MINIMUM_TOTAL_CHOLSTEROL.toDouble())
            ) {
                mBinding.etvCholestrolError.text = getString(
                    R.string.cholestrol_acid_error,
                    AppConstants.MINIMUM_TOTAL_CHOLSTEROL,
                    AppConstants.MAXIMUM_TOTAL_CHOLSTEROL
                )
                mBinding.etvCholestrolError.visibility = View.VISIBLE
                mBinding.etvCholesterol.requestFocus()
                mBinding.etvCholesterol.setBackgroundResource(R.drawable.input_field_error_bg_ui2)
                isValid = false
            } else {
                mBinding.etvCholestrolError.visibility = View.GONE
                mBinding.etvCholesterol.setBackgroundResource(R.drawable.bg_input_fieldnew)
            }
        }

        return isValid
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActionListener = context as VisitCreationActionListener
        sessionManager = SessionManager(context)
        configUtils = ConfigUtils(context)
    }

}