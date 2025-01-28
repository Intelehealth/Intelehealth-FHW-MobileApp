package org.intelehealth.app.ui.patient.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.databinding.OnRebindCallback
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.github.ajalt.timberkt.Timber
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import org.intelehealth.app.BuildConfig
import org.intelehealth.app.R
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.databinding.Dialog2NumbersPickerBinding
import org.intelehealth.app.databinding.FragmentPatientOtherInfoBinding
import org.intelehealth.app.databinding.FragmentPatientPersonalInfoBinding
import org.intelehealth.app.databinding.FragmentPatientPersonalInfoOldDesignBinding
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.ui.dialog.CalendarDialog
import org.intelehealth.app.ui.filter.FirstLetterUpperCaseInputFilter
import org.intelehealth.app.utilities.AgeUtils
import org.intelehealth.app.utilities.ArrayAdapterUtils
import org.intelehealth.app.utilities.DateAndTimeUtils
import org.intelehealth.app.utilities.FlavorKeys
import org.intelehealth.app.utilities.LanguageUtils
import org.intelehealth.app.utilities.PatientRegFieldsUtils
import org.intelehealth.app.utilities.PatientRegStage
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.extensions.addFilter
import org.intelehealth.app.utilities.extensions.hideDigitErrorOnTextChang
import org.intelehealth.app.utilities.extensions.hideError
import org.intelehealth.app.utilities.extensions.hideErrorOnTextChang
import org.intelehealth.app.utilities.extensions.validate
import org.intelehealth.app.utilities.extensions.validateDigit
import org.intelehealth.app.utilities.extensions.validateDropDowb
import org.intelehealth.core.registry.PermissionRegistry
import org.intelehealth.core.registry.PermissionRegistry.Companion.CAMERA
import org.intelehealth.ihutils.ui.CameraActivity
import org.intelehealth.klivekit.utils.DateTimeUtils
import org.joda.time.LocalDate
import org.joda.time.Period
import org.joda.time.PeriodType
import java.io.File
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

/**
 * Created by Vaghela Mithun R. on 27-06-2024 - 13:42.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PatientPersonalInfoFragment :
    BasePatientFragment(R.layout.fragment_patient_personal_info_old_design) {
    private lateinit var binding: FragmentPatientPersonalInfoOldDesignBinding
    var selectedDate = Calendar.getInstance().timeInMillis
    private val permissionRegistry by lazy {
        PermissionRegistry(requireContext(), requireActivity().activityResultRegistry)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPatientPersonalInfoOldDesignBinding.bind(view)
        patientViewModel.updatePatientStage(PatientRegStage.PERSONAL)
    }

    private fun setupAge() {
        binding.textInputETAge.setOnClickListener {
            val dialogBinding = Dialog2NumbersPickerBinding.inflate(layoutInflater)
            MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogStyle).apply {
                setTitle(R.string.identification_screen_prompt_age)
                setView(dialogBinding.root)
                dialogBinding.dialog3NumbersUnit.visibility = View.VISIBLE
                dialogBinding.dialog2NumbersQuantity.minValue = 0
                dialogBinding.dialog2NumbersQuantity.maxValue = 100
                dialogBinding.dialog2NumbersUnit.minValue = 0
                dialogBinding.dialog2NumbersUnit.maxValue = 12
                dialogBinding.dialog3NumbersUnit.minValue = 0
                dialogBinding.dialog3NumbersUnit.maxValue = 31
                val period = getAgePeriod()
                dialogBinding.dialog2NumbersQuantity.value = period.years
                dialogBinding.dialog2NumbersUnit.value = period.months
                dialogBinding.dialog3NumbersUnit.value = period.days
            }.create().also { dialog ->

                dialog.window?.let {
                    it.setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg)
                    val width = resources.getDimensionPixelSize(R.dimen.internet_dialog_width)
                    it.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
                }

                dialogBinding.btnCancelPicker.setOnClickListener { dialog.dismiss() }
                dialogBinding.buttonOkPicker.setOnClickListener {
                    val month = dialogBinding.dialog2NumbersUnit.value
                    val year = dialogBinding.dialog2NumbersQuantity.value
                    val days = dialogBinding.dialog3NumbersUnit.value

                    bindAgeAndDobValue(year, month, days)
                    dialog.dismiss()
                }

            }.show()
        }
    }

    private fun bindAgeAndDobValue(year: Int, month: Int, days: Int) {
        bindAgeValue(year, month, days)

        Calendar.getInstance().apply {
            add(Calendar.YEAR, -year)
            add(Calendar.MONTH, -month)
            add(Calendar.DAY_OF_MONTH, -days)
        }.also { bindDobValue(it) }
    }

    private fun bindAgeValue(year: Int, month: Int, days: Int) {
        DateAndTimeUtils.formatAgeInYearsMonthsDate(
            context,
            year,
            month,
            days
        ).apply { binding.textInputETAge.setText(this) }

        updateGuardianVisibility(year, month, days)
        binding.textInputLayAge.hideError()
    }

    private fun updateGuardianVisibility(year: Int, month: Int, days: Int) {
        val visibility = AgeUtils.isGuardianRequired(year, month, days)
        Timber.d { "Year[$year]/Month[$month]/Day[$days] => visibility[$visibility]" }
        binding.personalConfig?.let {
            Timber.d { "personalConfig => visibility[$visibility]" }
            it.guardianName?.isEnabled = it.guardianName?.isEnabled?.let { enabled ->
                if (visibility) enabled else false
            } ?: visibility

            it.guardianType?.isEnabled = it.guardianType?.isEnabled?.let { enabled ->
                if (visibility) enabled else false
            } ?: visibility

            binding.llGuardianName.isVisible = it.guardianName?.isEnabled ?: visibility
            binding.llGuardianType.isVisible = it.guardianType?.isEnabled ?: visibility
            binding.personalConfig = it
        }
    }

    private fun bindDobValue(calendar: Calendar) {
        selectedDate = calendar.timeInMillis
        val sdf = DateTimeUtils.getSimpleDateFormat(
            DateTimeUtils.MMM_DD_YYYY_FORMAT,
            TimeZone.getDefault()
        )
        val formattedDate = sdf.format(calendar.time)
        binding.textInputETDob.setText(formattedDate)
        binding.textInputLayDob.hideError()
        updateDob()
    }

    override fun onPatientDataLoaded(patient: PatientDTO) {
        super.onPatientDataLoaded(patient)
        Timber.d { "onPatientDataLoaded" }
        Timber.d { Gson().toJson(patient) }
        fetchPersonalInfoConfig()
        if(BuildConfig.FLAVOR_client == FlavorKeys.UNFPA){
            patient.apply {
                gender = gender?:"F"
            }
        }
        binding.patient = patient
        binding.isEditMode = patientViewModel.isEditMode
    }

    private fun fetchPersonalInfoConfig() {
        patientViewModel.fetchPersonalRegFields().observe(viewLifecycleOwner) {
            binding.personalConfig = PatientRegFieldsUtils.buildPatientPersonalInfoConfig(it)
            setupGuardianType()
            setupEmContactType()
            setupDOB()
            setupAge()
            applyFilter()
            setGender()
            setClickListener()
            setInputTextChangListener()
//            binding.addOnRebindCallback(onRebindCallback)
        }
    }

//    private val onRebindCallback =
//        object : OnRebindCallback<FragmentPatientPersonalInfoOldDesignBinding>() {
//            on
//            override fun onBound(binding: FragmentPatientPersonalInfoOldDesignBinding?) {
//                super.onBound(binding)
//                Timber.d { "OnRebindCallback.onBound" }
//
//            }
//        }

    private fun setClickListener() {
        binding.patientImgview.setOnClickListener { requestPermission() }
        binding.btnPatientPersonalNext.setOnClickListener {
            validateForm { savePatient() }
        }
    }

    private fun savePatient() {
        patient.apply {
            bindGenderValue()
            firstname = binding.textInputETFName.text?.toString()
            middlename = binding.textInputETMName.text?.toString()
            lastname = binding.textInputETLName.text?.toString()
            phonenumber = binding.countrycodeSpinner.fullNumberWithPlus
            guardianName = binding.textInputETGuardianName.text?.toString()
            emContactName = binding.textInputETECName.text?.toString()
            emContactNumber = binding.ccpEmContactPhone.fullNumberWithPlus

            patientViewModel.updatedPatient(this)
            if (patientViewModel.isEditMode) {
                saveAndNavigateToDetails()
            } else {
                if (patientViewModel.activeStatusAddressSection) {
                    PatientPersonalInfoFragmentDirections.navigationPersonalToAddress().apply {
                        findNavController().navigate(this)
                    }
                } else if (patientViewModel.activeStatusOtherSection) {
                    PatientPersonalInfoFragmentDirections.navigationPersonalToOther().apply {
                        findNavController().navigate(this)
                    }
                } else saveAndNavigateToDetails()
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
        PatientPersonalInfoFragmentDirections.navigationPersonalToDetails(
            patient.uuid, "searchPatient", "false"
        ).apply {
            findNavController().navigate(this)
            requireActivity().finish()
        }
    }

    private fun setGender() {
        if(BuildConfig.FLAVOR_client == FlavorKeys.UNFPA){
            binding.btnMale.isCheckable = false
            binding.btnFemale.isCheckable = false
            binding.btnOther.isCheckable = false
        }
        binding.toggleGender.addOnButtonCheckedListener { _, checkedId, _ ->
            binding.tvGenderError.isVisible = false
            bindGenderValue()
        }
    }

    private fun bindGenderValue(){
        patient.gender = when (binding.toggleGender.checkedButtonId) {
            R.id.btnMale -> "M"
            R.id.btnFemale -> "F"
            R.id.btnOther -> "O"
            else -> "O"
        }
    }

    private fun requestPermission() {
        permissionRegistry.requestPermission(CAMERA).observe(viewLifecycleOwner) {
            permissionRegistry.removePermissionObserve(viewLifecycleOwner)
            if (it[CAMERA] == true) takePicture()
            else showPermissionDeniedAlert()
        }
    }

    private fun takePicture() {
        val filePath = File(AppConstants.IMAGE_PATH + patient.uuid)
        if (!filePath.exists()) {
            filePath.mkdir()
        }

        val cameraIntent = Intent(activity, CameraActivity::class.java)
        cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, patient.uuid)
        cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, filePath.toString())
        cameraActivityResult.launch(cameraIntent)
    }

    private val cameraActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            patient.patientPhoto = result.data!!.getStringExtra("RESULT")
            binding.patient = patient
            if (!patient.patientPhoto.isNullOrEmpty()) binding.profileImageError.isVisible = false
            Timber.d { "Profile path => ${patient.patientPhoto}" }
        }
    }

    private fun showPermissionDeniedAlert() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setMessage(R.string.reject_permission_results)
            setPositiveButton(R.string.retry_again) { _, _ -> requestPermission() }
            setNegativeButton(R.string.ok_close_now) { _, _ -> requireActivity().finish() }
        }.show()
    }

    private fun setupDOB() {
        patient.dateofbirth?.let {
            parseDob(it, DateTimeUtils.YYYY_MM_DD_HYPHEN)

            DateTimeUtils.formatToLocalDate(
                Date(selectedDate),
                DateTimeUtils.MMM_DD_YYYY_FORMAT
            ).apply { binding.textInputETDob.setText(this) }
        }

        binding.textInputETDob.setOnClickListener {
            showDatePickerDialog(selectedDate)
        }
    }

    private fun parseDob(date: String, format: String) {
        selectedDate = DateTimeUtils.parseDate(date, format, TimeZone.getDefault()).time
        val period = getAgePeriod()
        bindAgeValue(period.years, period.months, period.days)
    }

    private fun getAgePeriod(): Period {
        val pastCalendar = Calendar.getInstance().apply { timeInMillis = selectedDate }
        val birthdate = LocalDate(
            pastCalendar[Calendar.YEAR],
            pastCalendar[Calendar.MONTH] + 1,
            pastCalendar[Calendar.DAY_OF_MONTH]
        ) //Birth date
        val now = LocalDate() //Today's date
        return Period(birthdate, now, PeriodType.yearMonthDay())
    }

    private fun showDatePickerDialog(selectedDate: Long) {
        CalendarDialog.Builder()
            .maxDate(Calendar.getInstance().timeInMillis)
            .selectedDate(selectedDate)
            .format(DateTimeUtils.MMM_DD_YYYY_FORMAT)
            .listener(dateListener)
            .build().show(childFragmentManager, CalendarDialog.TAG)
    }

    private val dateListener = object : CalendarDialog.OnDatePickListener {
        override fun onDatePick(day: Int, month: Int, year: Int, value: String?) {
            value?.let { parseDob(it, DateTimeUtils.MMM_DD_YYYY_FORMAT) }
            updateDob()
            binding.textInputETDob.setText(value)
            binding.textInputLayDob.hideError()
        }
    }

    private fun updateDob() {
        Calendar.getInstance().apply {
            timeInMillis = selectedDate
        }.also {
            DateTimeUtils.formatToLocalDate(it.time, DateTimeUtils.YYYY_MM_DD_HYPHEN).apply {
                patient.dateofbirth = this
            }
        }
    }

    private fun applyFilter() {
        binding.textInputETFName.addFilter(FirstLetterUpperCaseInputFilter())
        binding.textInputETMName.addFilter(FirstLetterUpperCaseInputFilter())
        binding.textInputETLName.addFilter(FirstLetterUpperCaseInputFilter())
        binding.textInputETGuardianName.addFilter(FirstLetterUpperCaseInputFilter())
        binding.textInputETECName.addFilter(FirstLetterUpperCaseInputFilter())
    }

    private fun setInputTextChangListener() {
        binding.countrycodeSpinner.registerCarrierNumberEditText(binding.textInputETPhoneNumber)
        binding.countrycodeSpinner.setNumberAutoFormattingEnabled(false)
        binding.countrycodeSpinner.fullNumber = patient.phonenumber
        binding.ccpEmContactPhone.registerCarrierNumberEditText(binding.textInputETEMPhoneNumber)
        binding.ccpEmContactPhone.setNumberAutoFormattingEnabled(false)
        binding.ccpEmContactPhone.fullNumber = patient.emContactNumber
        binding.textInputLayFName.hideErrorOnTextChang(binding.textInputETFName)
        binding.textInputLayMName.hideErrorOnTextChang(binding.textInputETMName)
        binding.textInputLayLName.hideErrorOnTextChang(binding.textInputETLName)
        binding.textInputLayGuardianName.hideErrorOnTextChang(binding.textInputETGuardianName)
        binding.textInputLayECName.hideErrorOnTextChang(binding.textInputETECName)
        binding.textInputLayPhoneNumber.hideDigitErrorOnTextChang(
            binding.textInputETPhoneNumber,
            10
        )
        binding.textInputLayEMPhoneNumber.hideDigitErrorOnTextChang(
            binding.textInputETEMPhoneNumber,
            10
        )
    }

    private fun setupGuardianType() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.guardian_type)
        binding.autoCompleteGuardianType.setAdapter(adapter)
        if (patient.guardianType != null && patient.guardianType.isNotEmpty()) {
            binding.autoCompleteGuardianType.setText(patient.guardianType, false)
        }
        binding.autoCompleteGuardianType.setOnItemClickListener { _, _, i, _ ->
            binding.textInputLayGuardianType.hideError()
            LanguageUtils.getSpecificLocalResource(requireContext(), "en").apply {
                patient.guardianType = this.getStringArray(R.array.guardian_type)[i]
            }
        }
    }

    private fun setupEmContactType() {
        val adapter = ArrayAdapterUtils.getArrayAdapter(requireContext(), R.array.contact_type)
        binding.autoCompleteEmContactType.setAdapter(adapter)
        if (patient.contactType != null && patient.contactType.isNotEmpty()) {
            binding.autoCompleteEmContactType.setText(patient.contactType, false)
        }
        binding.autoCompleteEmContactType.setOnItemClickListener { _, _, i, _ ->
            binding.textInputLayEmContactType.hideError()
            LanguageUtils.getSpecificLocalResource(requireContext(), "en").apply {
                patient.contactType = this.getStringArray(R.array.contact_type)[i]
            }
        }
    }

    private fun validateForm(block: () -> Unit) {
        val error = R.string.this_field_is_mandatory
        binding.personalConfig?.let {
            val bProfile = if (it.profilePic!!.isEnabled && it.profilePic!!.isMandatory) {
                !patient.patientPhoto.isNullOrEmpty()
            } else true

            binding.profileImageError.isVisible = bProfile.not()

            val bFName = if (it.firstName!!.isEnabled && it.firstName!!.isMandatory) {
                binding.textInputLayFName.validate(binding.textInputETFName, error)
            } else true

            val bMName = if (it.middleName!!.isEnabled && it.middleName!!.isMandatory) {
                binding.textInputLayMName.validate(binding.textInputETMName, error)
            } else true

            val bLName = if (it.lastName!!.isEnabled && it.lastName!!.isMandatory) {
                binding.textInputLayLName.validate(binding.textInputETLName, error)
            } else true

            val bGender = if (it.gender!!.isEnabled && it.gender!!.isMandatory) {
                !patient.gender.isNullOrEmpty()
            } else true

            binding.tvGenderError.isVisible = bGender.not()

            val bDob = if (it.dob!!.isEnabled && it.dob!!.isMandatory) {
                binding.textInputLayDob.validate(binding.textInputETDob, error)
            } else true

            val bAge = if (it.age!!.isEnabled && it.age!!.isMandatory) {
                binding.textInputLayAge.validate(binding.textInputETAge, error)
            } else true

            val bPhone = if (it.phone!!.isEnabled && it.phone!!.isMandatory) {
                binding.textInputLayPhoneNumber.validate(binding.textInputETPhoneNumber, error).and(
                    binding.textInputLayPhoneNumber.validateDigit(
                        binding.textInputETPhoneNumber,
                        R.string.enter_10_digits,
                        10
                    )
                )

            } else true

            val bGuardianType = if (it.guardianType!!.isEnabled && it.guardianType!!.isMandatory) {
                binding.textInputLayGuardianType.validateDropDowb(
                    binding.autoCompleteGuardianType,
                    error
                )
            } else true

            val bGName = if (it.guardianName!!.isEnabled && it.guardianName!!.isMandatory) {
                binding.textInputLayGuardianName.validate(
                    binding.textInputETGuardianName,
                    error
                )
            } else true

            val bEmName =
                if (it.emergencyContactName!!.isEnabled && it.emergencyContactName!!.isMandatory) {
                    binding.textInputLayECName.validate(binding.textInputETECName, error)
                } else true

            val bEmPhone =
                if (it.emergencyContactNumber!!.isEnabled && it.emergencyContactNumber!!.isMandatory) {
                    Timber.d { "Emergency validation" }
                    binding.textInputLayEMPhoneNumber.validate(
                        binding.textInputETEMPhoneNumber,
                        error
                    ).and(
                        binding.textInputLayEMPhoneNumber.validateDigit(
                            binding.textInputETEMPhoneNumber,
                            R.string.enter_10_digits,
                            10
                        )
                    ).and(binding.textInputETPhoneNumber.text?.let { phone ->
                        val valid =
                            phone.toString() != binding.textInputETEMPhoneNumber.text.toString()
                        if (!valid) {
                            binding.textInputLayEMPhoneNumber.error = getString(
                                R.string.phone_number_and_emergency_number_can_not_be_the_same
                            )
                        }
                        valid
                    } ?: false)


                } else true

            val bEmContactType =
                if (it.emergencyContactType!!.isEnabled && it.emergencyContactType!!.isMandatory) {
                    binding.textInputLayEmContactType.validateDropDowb(
                        binding.autoCompleteEmContactType,
                        error
                    )
                } else true

            if (bProfile.and(bFName).and(bMName).and(bLName).and(bGender)
                    .and(bDob).and(bAge).and(bPhone).and(bGName).and(bGuardianType)
                    .and(bEmName).and(bEmPhone).and(bEmContactType)
            ) block.invoke()
        }
    }
}