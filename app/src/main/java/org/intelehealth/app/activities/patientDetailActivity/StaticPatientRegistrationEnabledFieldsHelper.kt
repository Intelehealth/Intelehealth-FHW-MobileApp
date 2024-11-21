package org.intelehealth.app.activities.patientDetailActivity

import org.intelehealth.app.utilities.PatientRegConfigKeys
import org.intelehealth.config.room.entity.PatientRegistrationFields

object StaticPatientRegistrationEnabledFieldsHelper {

    fun getEnabledPersonalInfoFields(): List<PatientRegistrationFields> {
        val fields: MutableList<PatientRegistrationFields> = mutableListOf()

        // Profile photo
        var currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.PROFILE_PHOTO,
            isMandatory = false,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        // First name
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.FIRST_NAME,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        // Middle name
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.MIDDLE_NAME,
            isMandatory = false,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        // Last name
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.LAST_NAME,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        // Gender
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.GENDER,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        // Dob
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.DOB,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        // Age
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.AGE,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        // Guardian Name
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.GUARDIAN_NAME,
            isMandatory = false,
            isEditable = false,
            isEnabled = false
        )

        fields.add(currentField)

        // Guardian Type
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.GUARDIAN_TYPE,
            isMandatory = false,
            isEditable = false,
            isEnabled = false
        )

        fields.add(currentField)

        // Phone Number
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.PHONE_NUM,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        // Emergency Contact Type
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.EM_CONTACT_TYPE,
            isMandatory = false,
            isEditable = false,
            isEnabled = false
        )

        fields.add(currentField)

        // Emergency Contact Name
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.EM_CONTACT_NAME,
            isMandatory = false,
            isEditable = false,
            isEnabled = false
        )

        fields.add(currentField)

        // Emergency Phone Number
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.EM_CONTACT_NUMBER,
            isMandatory = false,
            isEditable = false,
            isEnabled = false
        )

        fields.add(currentField)
        return fields
    }

    fun getEnabledAddressInfoFields(): List<PatientRegistrationFields> {
        val fields: MutableList<PatientRegistrationFields> = mutableListOf()

        // Postal Code
        var currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.POSTAL_CODE,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        // Country
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.COUNTRY,
            isMandatory = true,
            isEditable = false,
            isEnabled = true
        )

        fields.add(currentField)

        // State
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.STATE,
            isMandatory = true,
            isEditable = false,
            isEnabled = true
        )

        fields.add(currentField)

        // District
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.DISTRICT,
            isMandatory = false,
            isEditable = false,
            isEnabled = false
        )

        fields.add(currentField)

        // City / Village
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.VILLAGE_TOWN_CITY,
            isMandatory = true,
            isEditable = false,
            isEnabled = true
        )

        fields.add(currentField)

        // Address 1
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.ADDRESS_1,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        // Address 2
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.ADDRESS_2,
            isMandatory = false,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)
        return fields
    }

    fun getEnabledOtherInfoFields(): List<PatientRegistrationFields> {
        val fields: MutableList<PatientRegistrationFields> = mutableListOf()

        // National Id
        var currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.NATIONAL_ID,
            isMandatory = false,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        // Occupations
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.OCCUPATION,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        // Social Category
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.SOCIAL_CATEGORY,
            isMandatory = false,
            isEditable = false,
            isEnabled = false
        )

        fields.add(currentField)

        // Education
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.EDUCATION,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        // Economic Category
        currentField = PatientRegistrationFields(
            id = 0,
            groupId = "",
            name = "",
            idKey = PatientRegConfigKeys.ECONOMIC_CATEGORY,
            isMandatory = true,
            isEditable = true,
            isEnabled = true
        )

        fields.add(currentField)

        return fields
    }

    fun getAllEnabledPatientInfoFields(): List<PatientRegistrationFields> {
        return mutableListOf<PatientRegistrationFields>().also {
            it.addAll(getEnabledPersonalInfoFields())
            it.addAll(getEnabledAddressInfoFields())
            it.addAll(getEnabledOtherInfoFields())
        }
    }

    fun isGuardianActivated(): Boolean {
        val field = StaticPatientRegistrationEnabledFieldsHelper.getAllEnabledPatientInfoFields()
        field.forEach {
            if (it.idKey == PatientRegConfigKeys.GUARDIAN_TYPE) {
                return it.isEnabled
            }
        }
        return false
    }
}