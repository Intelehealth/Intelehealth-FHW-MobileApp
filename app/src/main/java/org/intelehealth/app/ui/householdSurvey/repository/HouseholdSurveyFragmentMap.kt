package org.intelehealth.app.ui.householdSurvey.repository

object HouseholdSurveyFragmentMap {
    private val fragmentFieldsMap = mapOf(
        "firstScreen" to listOf(
            "houseStructure",
            "resultOfVisit",
            "namePrimaryRespondent",
            "ReportDate of survey started",
            "HouseholdNumber"
        ),
        "secondScreen" to listOf(
            "numberOfSmartPhones",
            "numberOfFeaturePhones",
            "numberOfEarningMembers",
            "primarySourceOfIncome"
        )
    )

    fun getFieldsForFragment(fragmentIdentifier: String): List<String> {
        return org.intelehealth.app.ui.householdSurvey.repository.HouseholdSurveyFragmentMap.fragmentFieldsMap[fragmentIdentifier] ?: emptyList()
    }
}