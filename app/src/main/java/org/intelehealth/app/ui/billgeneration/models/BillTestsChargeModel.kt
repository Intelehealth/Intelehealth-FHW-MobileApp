package org.intelehealth.app.ui.billgeneration.models

data class BillTestsChargeModel (
    var consultationChargesVisible: Boolean = false,
    var consultationChargeAmount: String = "",
    var followUpChargesVisible: Boolean = false,
    var followUpChargeAmount: String = "",
    var glucoseFastingChargesVisible: Boolean = false,
    var glucoseFastingChargeAmount: String = "",
    var glucosePostPrandialChargesVisible: Boolean = false,
    var glucosePostPrandialChargeAmount: String = "",
    var hemoglobinChargesVisible: Boolean = false,
    var hemoglobinChargeAmount: String = "",
    var uricAcidChargesVisible: Boolean = false,
    var uricAcidChargeAmount: String = "",
    var cholestrolChargesVisible: Boolean = false,
    var cholestrolChargeAmount: String = "",
    var bpChargesVisible: Boolean = false,
    var bpChargeAmount: String = "",
    var glucoseRandomVisible: Boolean = false,
    var glucoseRandomAmount: String = "",
    var totalChargeAmount: String = "0"
)