package org.intelehealth.app.ayu.visit.vital

data class VitalPreference(
    var height: String = "",
    var weight: String = "",
    var bmi: String = "",
    var bpSystolic: String = "",
    var bpDiastolic: String = "",
    var pulse: String = "",
    var temperature: String = "",
    var spO2: String = "",
    var respiratoryRate: String = "",
    var fbs: String = "",
)