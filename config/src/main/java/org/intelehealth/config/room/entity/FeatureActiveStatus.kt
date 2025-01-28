package org.intelehealth.config.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Vaghela Mithun R. on 29-05-2024 - 17:25.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@Entity(tableName = "tbl_feature_active_status")
data class FeatureActiveStatus(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @SerializedName("notes_section")
    val visitSummeryNote: Boolean,
    @SerializedName("attachment_section")
    val visitSummeryAttachment: Boolean,
    @SerializedName("doctor_specialty_section")
    val visitSummeryDoctorSpeciality: Boolean,
    @SerializedName("priority_visit_section")
    val visitSummeryPriorityVisit: Boolean,
    @SerializedName("appointment_button")
    val visitSummeryAppointment: Boolean,
    @SerializedName("severity_of_case_section")
    val visitSummerySeverityOfCase: Boolean,
    @SerializedName("facility_to_visit_section")
    val visitSummeryFacilityToVisit: Boolean,
    @SerializedName("hw_followup_section")
    val visitSummeryHwFollowUp: Boolean,

    @SerializedName("diagnosis_at_secondary_level")
    val diagnosisAtSecondaryLevel: Boolean = false,

    @SerializedName("type_of_consultation")
    val typeOfConsultation: Boolean = false,
    @SerializedName("generate_bill_button")
    val generateBillButton: Boolean,
    @SerializedName("restrict_end_visit_till_prescription_download")
    val restrictEndVisit: Boolean,
    @SerializedName("print_using_thermal_printer")
    val printUsingThermalPrinter: Boolean
) {
    var videoSection: Boolean = true
    var chatSection: Boolean = true

    @SerializedName("patient_vitals_section")
    var vitalSection: Boolean = true

    @SerializedName("patient_reg_address")
    var activeStatusPatientAddress: Boolean = true

    @SerializedName("patient_reg_other")
    var activeStatusPatientOther: Boolean = true

    @SerializedName("patient_family_member_registration")
    var activeStatusPatientFamilyMemberRegistration: Boolean = true

    @SerializedName("abha_section")
    var activeStatusAbha: Boolean = true

    @SerializedName("patient_household_survey")
    var activeStatusPatientHouseholdSurvey: Boolean = true

    @SerializedName("roster_questionnaire_section")
    var activeStatusRosterQuestionnaireSection: Boolean = true

    @SerializedName("patient_diagnostics_section")
    var activeStatusDiagnosticsSection: Boolean = true

    @SerializedName("patient_draft_survey")
    var activeStatusPatientDraftSurvey: Boolean = true
}
