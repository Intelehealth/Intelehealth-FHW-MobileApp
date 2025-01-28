package org.intelehealth.app.ui.billgeneration.models

import java.io.Serializable

data class BillDetails(
    var patientName: String = "",
    var patientPhoneNum: String= "",
    var patientVillage: String= "",
    var patientOpenID: String= "",
    var patientHideVisitID: String= "",
    var visitType: String= "",
    var patientVisitID: String= "",
    var receiptNum: String= "",
    var billDateString: String= "",
    var billType: String= "",
    var selectedTestsList: ArrayList<String?> = ArrayList(),
    var patientDetails: String= "",
    var billEncounterUUID: String= "",

): Serializable