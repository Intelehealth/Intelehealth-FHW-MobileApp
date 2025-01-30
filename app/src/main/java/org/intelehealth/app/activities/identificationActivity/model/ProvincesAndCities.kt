package org.intelehealth.app.activities.identificationActivity.model

data class ProvincesAndCities(
    var provinces: ArrayList<String> = arrayListOf(),
    var cities: ArrayList<String> = arrayListOf(),

    var provinces_ru: ArrayList<String> = arrayListOf(),
    var cities_ru: ArrayList<String> = arrayListOf(),

    var code_of_hf: ArrayList<String> = arrayListOf()
)
