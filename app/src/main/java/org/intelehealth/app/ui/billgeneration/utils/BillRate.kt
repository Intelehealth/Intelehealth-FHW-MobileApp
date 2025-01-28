package org.intelehealth.app.ui.billgeneration.utils

enum class BillRate(val value: Int) {
    URIC_ACID(30),
    CHOLESTEROL(80),
    GLUCOSE_FASTING(15),
    GLUCOSE_POST_PRANDIAL(15),
    HEMOGLOBIN(20),
    BP(5),
    CONSULTATION(15),
    FOLLOW_UP(10),
    GLUCOSE_RANDOM(15),
    GLUCOSE_NON_FASTING(15)
}
enum class VisitType(val value: String) {
    CONSULTATION("Consultation"),
    FOLLOW_UP("Follow-Up"),
}
enum class PaymentStatus(val value: String) {
    PAID("Paid"),
    UNPAID("Unpaid"),
}