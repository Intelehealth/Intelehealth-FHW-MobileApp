package org.intelehealth.app.utilities

/**
 * Created by Tanvir Hasan on 29-04-2024 : 16-31.
 * Email: mhasan@intelehealth.org
 *
 * Patient register config keys
 */
class PatientRegConfigKeys {
    companion object{
        //PERSONAL
        const val PROFILE_PHOTO = "p_profile_photo"
        const val FIRST_NAME = "p_first_name"
        const val MIDDLE_NAME = "p_middle_name"
        const val LAST_NAME = "p_last_name"
        const val GENDER = "p_gender"
        const val DOB = "p_date_of_birth"
        const val AGE = "p_age"
        const val PHONE_NUM = "p_phone_number"
        const val GUARDIAN_NAME = "p_guardian_name"
        const val GUARDIAN_TYPE = "p_guardian_type"
        const val EM_CONTACT_NAME = "p_emergency_contact_name"
        const val EM_CONTACT_NUMBER = "p_emergency_contact_number"
        const val EM_CONTACT_TYPE = "p_contact_type"

        //ADDRESS
        const val POSTAL_CODE = "a_postal_address"
        const val COUNTRY = "a_country"
        const val STATE = "a_state"
        const val DISTRICT = "a_district"
        const val VILLAGE_TOWN_CITY = "a_village_town_city"
        const val ADDRESS_1 = "a_corresponding_address_1"
        const val ADDRESS_2 = "a_corresponding_address_2"
        const val PROVINCES = "a_provinces"
        const val CITIES = "a_cities"
        const val REGISTRATION_ADDRESS_OF_HF = "a_registration_address_of_health_facility"
        const val BLOCK = "a_block"
        const val HOUSEHOLD_NUMBER = "a_household_number"

        //OTHERS
        const val NATIONAL_ID = "o_national_id"
        const val OCCUPATION = "o_occupation"
        const val SOCIAL_CATEGORY = "o_social_category"
        const val EDUCATION = "o_education"
        const val ECONOMIC_CATEGORY = "o_economic_category"

        const val TMH_CASE_SUMMARY = "o_tmh_case_number"
        const val REQUEST_ID = "o_request_id"
        const val RELATIVE_PHONE_NUM = "o_relative_phone_number"
        const val DISCIPLINE = "o_discipline"
        const val DEPARTMENT = "o_department"

        const val INN = "o_inn"
        const val CODE_OF_HEALTHY_FACILITY = "o_code_of_the_health_facility"
        const val HEALTH_FACILITY_NAME = "o_health_facility_name"
        const val CODE_OF_DEPARTMENT = "o_code_of_the_department"
    }
}