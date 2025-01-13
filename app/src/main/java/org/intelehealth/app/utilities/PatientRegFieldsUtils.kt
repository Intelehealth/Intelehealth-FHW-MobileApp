package org.intelehealth.app.utilities

import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import com.hbb20.CountryCodePicker
import org.intelehealth.app.ui.patient.config.AddressInfoConfig
import org.intelehealth.app.ui.patient.config.OtherInfoConfig
import org.intelehealth.app.ui.patient.config.PersonalInfoConfig
import org.intelehealth.config.room.entity.PatientRegistrationFields
import java.lang.StringBuilder

/**
 * Created by Tanvir Hasan on 30-04-2024 : 12-31.
 * Email: mhasan@intelehealth.org
 */
object PatientRegFieldsUtils {
    fun configField(
        isEditMode: Boolean,
        field: PatientRegistrationFields,
        layoutView: View,
        fieldViewPrimary: View?,
        fieldViewSecondary: View?,
        titleTv: TextView?
    ) {
        layoutView.visibility = if(field.isEnabled)  View.VISIBLE else View.GONE

        //if existing title has * then removing it first
        var titleStr = titleTv?.text
        titleStr?.let {
            if (it.contains('*')) {
                titleStr = it.toString().replace("*", "")
            }
        }
        //setting asterisk for mandatory fields
        if (field.isMandatory) {
            titleTv?.text = StringBuilder().append(titleStr).append(" *")
        } else {
            titleTv?.text = titleStr
        }

        //view updating while edit mode is false
        if (isEditMode && !field.isEditable) {
            //disabling radio group child here
            if (fieldViewPrimary is RadioGroup) {
                for (i in 0 until fieldViewPrimary.childCount) {
                    val child: View = fieldViewPrimary.getChildAt(i)
                    val radioButton = child as RadioButton
                    radioButton.isClickable = false
                }
            } else {
                fieldViewPrimary?.isEnabled = false
                //some fields has secondary fields, ex: phone num has country picker
                //this type of view are handling here
                fieldViewSecondary?.let {
                    when (fieldViewSecondary) {
                        is CountryCodePicker -> {
                            fieldViewSecondary.setCcpClickable(false)
                        }

                        is Button -> {
                            fieldViewSecondary.setVisibility(View.GONE)
                        }

                        is TextView -> {
                            fieldViewSecondary.setVisibility(View.GONE)
                        }

                        else -> {
                            fieldViewSecondary.isEnabled = false
                        }
                    }
                }
            }
        }
    }

    /**
     * checking enable status of each field
     */
    fun getFieldEnableStatus(
        patientRegistrationFields: List<PatientRegistrationFields>,
        fieldType: String
    ): Boolean {
        if (patientRegistrationFields.isEmpty()) return false
        patientRegistrationFields.find { it.idKey == fieldType }?.let {
            return it.isEnabled
        }
        return false
    }

    /**
     * checking mandatory status of each field
     */
    fun getFieldMandatoryStatus(
        patientRegistrationFields: List<PatientRegistrationFields>,
        fieldType: String
    ): Boolean {
        if (patientRegistrationFields.isEmpty()) return false
        patientRegistrationFields.find { it.idKey == fieldType }?.let {
            return it.isMandatory
        }
        return false
    }

    @JvmStatic
    fun buildPatientPersonalInfoConfig(patientRegistrationFields: List<PatientRegistrationFields>): PersonalInfoConfig {
        return PersonalInfoConfig().apply {
            patientRegistrationFields.forEach {
                when (it.idKey) {
                    PatientRegConfigKeys.FIRST_NAME -> firstName = it
                    PatientRegConfigKeys.MIDDLE_NAME -> middleName = it
                    PatientRegConfigKeys.LAST_NAME -> lastName = it
                    PatientRegConfigKeys.DOB -> dob = it
                    PatientRegConfigKeys.AGE -> age = it
                    PatientRegConfigKeys.PHONE_NUM -> phone = it
                    PatientRegConfigKeys.PROFILE_PHOTO -> profilePic = it
                    PatientRegConfigKeys.GENDER -> gender = it
                    PatientRegConfigKeys.GUARDIAN_NAME -> guardianName = it
                    PatientRegConfigKeys.GUARDIAN_TYPE -> guardianType = it
                    PatientRegConfigKeys.EM_CONTACT_NAME -> emergencyContactName = it
                    PatientRegConfigKeys.EM_CONTACT_NUMBER -> emergencyContactNumber = it
                    PatientRegConfigKeys.EM_CONTACT_TYPE -> emergencyContactType = it
                }
            }
        }
    }

    @JvmStatic
    fun buildPatientAddressInfoConfig(patientRegistrationFields: List<PatientRegistrationFields>): AddressInfoConfig {
        return AddressInfoConfig().apply {
            patientRegistrationFields.forEach {
                when (it.idKey) {
                    PatientRegConfigKeys.POSTAL_CODE -> postalCode = it
                    PatientRegConfigKeys.COUNTRY -> country = it
                    PatientRegConfigKeys.STATE -> state = it
                    PatientRegConfigKeys.DISTRICT -> district = it
                    PatientRegConfigKeys.VILLAGE_TOWN_CITY -> cityVillage = it
                    PatientRegConfigKeys.ADDRESS_1 -> address1 = it
                    PatientRegConfigKeys.ADDRESS_2 -> address2 = it
                    PatientRegConfigKeys.PROVINCES -> province = it
                    PatientRegConfigKeys.CITIES -> city = it
                    PatientRegConfigKeys.REGISTRATION_ADDRESS_OF_HF -> registrationAddressOfHf = it
                    PatientRegConfigKeys.BLOCK -> block = it
                    PatientRegConfigKeys.HOUSEHOLD_NUMBER -> householdNumber = it

                }
            }
        }
    }

    @JvmStatic
    fun buildPatientOtherInfoConfig(patientRegistrationFields: List<PatientRegistrationFields>): OtherInfoConfig {
        return OtherInfoConfig().apply {
            patientRegistrationFields.forEach {
                when (it.idKey) {
                    PatientRegConfigKeys.NATIONAL_ID -> nationalId = it
                    PatientRegConfigKeys.OCCUPATION -> occuptions = it
                    PatientRegConfigKeys.SOCIAL_CATEGORY -> socialCategory = it
                    PatientRegConfigKeys.EDUCATION -> education = it
                    PatientRegConfigKeys.ECONOMIC_CATEGORY -> economicCategory = it

                    PatientRegConfigKeys.TMH_CASE_SUMMARY -> tmhCaseNumber = it
                    PatientRegConfigKeys.REQUEST_ID -> requestId = it
                    PatientRegConfigKeys.RELATIVE_PHONE_NUM -> relativePhoneNumber = it
                    PatientRegConfigKeys.DISCIPLINE -> discipline = it
                    PatientRegConfigKeys.DEPARTMENT -> department = it

                    PatientRegConfigKeys.INN -> inn = it
                    PatientRegConfigKeys.CODE_OF_HEALTHY_FACILITY -> codeOfHealthyFacility = it
                    PatientRegConfigKeys.HEALTH_FACILITY_NAME -> healthFacilityName = it
                    PatientRegConfigKeys.CODE_OF_DEPARTMENT -> codeOfDepartment = it
                }
            }
        }
    }
}