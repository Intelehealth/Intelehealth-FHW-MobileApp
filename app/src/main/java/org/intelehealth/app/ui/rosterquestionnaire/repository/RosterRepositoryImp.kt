package org.intelehealth.app.ui.rosterquestionnaire.repository

import android.content.Context
import android.text.InputType
import org.intelehealth.app.R
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.app.models.dto.PatientAttributesDTO
import org.intelehealth.app.ui.rosterquestionnaire.model.RoasterViewQuestion
import org.intelehealth.app.ui.rosterquestionnaire.utilities.RoasterAttribute
import org.intelehealth.app.ui.rosterquestionnaire.utilities.RoasterQuestionView
import javax.inject.Inject

class RosterRepositoryImp @Inject constructor() : RosterRepository {

    private var context: Context = IntelehealthApplication.getAppContext()

    private val patientDao = PatientsDAO()

    override fun getGeneralQuestionList(): ArrayList<RoasterViewQuestion> {
        val list = ArrayList<RoasterViewQuestion>()
        list.apply {
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.SPINNER,
                    question = context.getString(R.string.txt_what_is_your_relationship_with_head_of_household),
                    spinnerItem = R.array.relationshipHoH,
                    errorMessage = context.getString(R.string.this_field_is_mandatory),
                    attribute = RoasterAttribute.RELATIONSHIP_STATUS_HOH.attributeName
                )
            )
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.SPINNER,
                    question = context.getString(R.string.what_is_your_marital_status),
                    spinnerItem = R.array.maritual,
                    errorMessage = context.getString(R.string.this_field_is_mandatory),
                    attribute = RoasterAttribute.MARITAL_STATUS.attributeName
                )
            )
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.SPINNER,
                    question = context.getString(R.string.what_is_your_education_status),
                    spinnerItem = R.array.education_nas,
                    errorMessage = context.getString(R.string.this_field_is_mandatory),
                    attribute = RoasterAttribute.EDUCATION_LEVEL.attributeName
                )
            )
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.SPINNER,
                    question = context.getString(R.string.nas_occupation),
                    spinnerItem = R.array.occupation_identification,
                    errorMessage = context.getString(R.string.this_field_is_mandatory),
                    attribute = RoasterAttribute.OCCUPATION.attributeName
                )
            )
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.SPINNER,
                    question = context.getString(R.string.txt_phone_ownership),
                    spinnerItem = R.array.phoneownership,
                    errorMessage = context.getString(R.string.this_field_is_mandatory),
                    attribute = RoasterAttribute.PHONE_OWNERSHIP.attributeName
                )
            )
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.SPINNER,
                    question = context.getString(R.string.txt_when_was_the_last_time_you_got_your_bp_checked),
                    spinnerItem = R.array.bp,
                    errorMessage = context.getString(R.string.this_field_is_mandatory),
                    attribute = RoasterAttribute.BP_CHECKED.attributeName
                )
            )
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.SPINNER,
                    question = context.getString(R.string.txt_when_was_the_last_time_you_got_your_sugar_level_checked),
                    spinnerItem = R.array.sugar,
                    errorMessage = context.getString(R.string.this_field_is_mandatory),
                    attribute = RoasterAttribute.SUGAR_CHECKED.attributeName
                )
            )
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.SPINNER,
                    question = context.getString(R.string.txt_when_was_the_last_time_you_got_your_hb_level_tested),
                    spinnerItem = R.array.hb,
                    errorMessage = context.getString(R.string.this_field_is_mandatory),
                    attribute = RoasterAttribute.HB_TEST.attributeName
                )
            )
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.SPINNER,
                    question = context.getString(R.string.txt_bmi_height_and_weight_checked),
                    spinnerItem = R.array.bmi,
                    errorMessage = context.getString(R.string.this_field_is_mandatory),
                    attribute = RoasterAttribute.BMI.attributeName
                )
            )
        }
        return list
    }

    override fun getOutcomeQuestionList(): ArrayList<RoasterViewQuestion> {
        val list = ArrayList<RoasterViewQuestion>()
        list.apply {
            // 1 Question
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.SPINNER,
                    question = context.getString(R.string.what_was_the_outcome_of_your_pregnancy),
                    spinnerItem = R.array.outcome_pregnancy_en,
                    errorMessage = context.getString(R.string.this_field_is_mandatory)
                )
            )
            // 2 Question
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.SPINNER,
                    question = context.getString(R.string.txt_is_the_child_alive),
                    spinnerItem = R.array.child_alive,
                    errorMessage = context.getString(R.string.this_field_is_mandatory)
                )
            )
            // 3 Question
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.SPINNER,
                    question = context.getString(R.string.was_this_birth_pre_term_earlier_than_due_date_for_delivery),
                    spinnerItem = R.array.child_pre_term,
                    errorMessage = context.getString(R.string.this_field_is_mandatory)
                )
            )
            // 4 Question
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.DATE_PICKER,
                    question = context.getString(R.string.what_was_the_year_of_pregnancy_outcome_a_for_pregnant_ladies_mention_the_date_when_conceived_b_in_case_of_other_options_mention_the_date_of_delivery_miscarriage),
                    errorMessage = context.getString(R.string.this_field_is_mandatory)
                )
            )
            // 5 Question
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.EDIT_TEXT,
                    question = context.getString(R.string.how_many_months_did_this_pregnancy_last),
                    inputType = InputType.TYPE_CLASS_NUMBER,
                    errorMessage = context.getString(R.string.this_field_is_mandatory)
                )
            )
            // 6 Question
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.EDIT_TEXT,
                    question = context.getString(R.string.txt_how_many_months_have_you_been_pregnant),
                    inputType = InputType.TYPE_CLASS_NUMBER,
                    errorMessage = context.getString(R.string.this_field_is_mandatory)
                )
            )
            // 7 Question
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.SPINNER,
                    question = context.getString(R.string.what_is_the_place_of_delivery),
                    spinnerItem = R.array.place_delivery_en,
                    errorMessage = context.getString(R.string.this_field_is_mandatory)
                )
            )
            // 8 Question
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.SPINNER,
                    question = context.getString(R.string.what_is_the_type_of_delivery),
                    spinnerItem = R.array.delivery_type_en,
                    errorMessage = context.getString(R.string.this_field_is_mandatory)
                )
            )
            // 9 Question
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.SPINNER,
                    question = context.getString(R.string.focal_facility_for_this_pregnancy),
                    spinnerItem = R.array.block,
                    errorMessage = context.getString(R.string.this_field_is_mandatory)
                )
            )
            // 10 Question
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.EDIT_TEXT,
                    question = context.getString(R.string.what_was_the_name_of_the_facility),
                    inputType = InputType.TYPE_CLASS_TEXT,
                    errorMessage = context.getString(R.string.this_field_is_mandatory)
                )
            )
            // 11 Question
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.SPINNER,
                    question = context.getString(R.string.txt_was_this_a_single_multiple_births),
                    spinnerItem = R.array.single_multiple_births,
                    errorMessage = context.getString(R.string.this_field_is_mandatory)
                )
            )
            // 12 Question
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.EDIT_TEXT,
                    question = context.getString(R.string.txt_how_old_was_the_baby_when_he_she_died),
                    inputType = InputType.TYPE_CLASS_NUMBER,
                    errorMessage = context.getString(R.string.this_field_is_mandatory)
                )
            )
            // 13 Question
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.SPINNER,
                    question = context.getString(R.string.txt_what_is_was_the_sex_of_the_baby),
                    spinnerItem = R.array.sex_of_baby,
                    errorMessage = context.getString(R.string.this_field_is_mandatory)
                )
            )
            // 14 Question
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.SPINNER,
                    question = context.getString(R.string.was_the_pregnancy_planned),
                    spinnerItem = R.array.pregnancy_planned_en,
                    errorMessage = context.getString(R.string.this_field_is_mandatory)
                )
            )
            // 15 Question
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.SPINNER,
                    question = context.getString(R.string.where_you_identified_as_a_high_risk_pregnancy_case),
                    spinnerItem = R.array.high_risk_pregnancy_en,
                    errorMessage = context.getString(R.string.this_field_is_mandatory)
                )
            )
            // 16 Question
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.SPINNER,
                    question = context.getString(R.string.txt_were_any_complications_identified_at_the_time_of_delivery),
                    spinnerItem = R.array.complications,
                    errorMessage = context.getString(R.string.this_field_is_mandatory)
                )
            )
        }
        return list
    }

    override fun getHealthServiceQuestionList(): ArrayList<RoasterViewQuestion> {
        val list = ArrayList<RoasterViewQuestion>()
        list.apply {
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.SPINNER,
                    question = context.getString(R.string.health_issue_reported),
                    spinnerItem = R.array.health_issue_reported_en,
                    errorMessage = context.getString(R.string.this_field_is_mandatory)
                )
            )
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.EDIT_TEXT,
                    question = context.getString(R.string.no_of_episodes_in_the_last_years),
                    errorMessage = context.getString(R.string.this_field_is_mandatory),
                    inputType = InputType.TYPE_CLASS_NUMBER
                )
            )

            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.SPINNER,
                    question = context.getString(R.string.generally_which_is_the_primary_health_provider_you_interacted_with_for_health_issues),
                    spinnerItem = R.array.primary_health_provider_en,
                    errorMessage = context.getString(R.string.this_field_is_mandatory)
                )
            )
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.SPINNER,
                    question = context.getString(R.string.first_location_of_visit_interaction),
                    spinnerItem = R.array.place_delivery_en,
                    errorMessage = context.getString(R.string.this_field_is_mandatory)
                )
            )
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.SPINNER,
                    question = context.getString(R.string.referred_to),
                    spinnerItem = R.array.referred_to_en,
                    errorMessage = context.getString(R.string.this_field_is_mandatory)
                )
            )
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.SPINNER,
                    question = context.getString(R.string.mode_of_transportation_used_to_reach_facility),
                    spinnerItem = R.array.mode_transport_en,
                    errorMessage = context.getString(R.string.this_field_is_mandatory)
                )
            )
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.EDIT_TEXT,
                    question = context.getString(R.string.average_cost_incurred_on_travel_and_stay_per_episode),
                    errorMessage = context.getString(R.string.this_field_is_mandatory),
                    inputType = InputType.TYPE_CLASS_NUMBER,
                )
            )
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.EDIT_TEXT,
                    question = context.getString(R.string.average_cost_incurred_on_consultation_fee_per_episode),
                    inputType = InputType.TYPE_CLASS_NUMBER,
                    errorMessage = context.getString(R.string.this_field_is_mandatory)
                )
            )
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.EDIT_TEXT,
                    question = context.getString(R.string.average_cost_incurred_on_medicines_per_episode),
                    inputType = InputType.TYPE_CLASS_NUMBER,
                    errorMessage = context.getString(R.string.this_field_is_mandatory)
                )
            )
            add(
                RoasterViewQuestion(
                    layoutId = RoasterQuestionView.SPINNER,
                    question = context.getString(R.string.score_for_experience_of_treatment),
                    spinnerItem = R.array.score_experience_en,
                    errorMessage = context.getString(R.string.this_field_is_mandatory)
                )
            )
        }
        return list
    }

    override fun insertRoaster(attributeList: ArrayList<PatientAttributesDTO>) {
        patientDao.insertPatientAttributes(attributeList)
    }

    override fun getAllRoasterData(patientUuid: String): ArrayList<PatientAttributesDTO> {
        return patientDao.getPatientRoaster(patientUuid)
    }

}
