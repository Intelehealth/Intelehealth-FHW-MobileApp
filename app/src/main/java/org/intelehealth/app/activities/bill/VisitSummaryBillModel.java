package org.intelehealth.app.activities.bill;

public class VisitSummaryBillModel {
    private String visitUuid;
    private String hideVisitUUID;
    private String visitType;
    private String receiptPaymentStatus;

    public String getVisitUuid() {
        return visitUuid;
    }

    public void setVisitUuid(String visitUuid) {
        this.visitUuid = visitUuid;
    }

    public String getHideVisitUUID() {
        return hideVisitUUID;
    }

    public void setHideVisitUUID(String hideVisitUUID) {
        this.hideVisitUUID = hideVisitUUID;
    }

    public String getVisitType() {
        return visitType;
    }

    public void setVisitType(String visitType) {
        this.visitType = visitType;
    }

    public String getReceiptPaymentStatus() {
        return receiptPaymentStatus;
    }

    public void setReceiptPaymentStatus(String receiptPaymentStatus) {
        this.receiptPaymentStatus = receiptPaymentStatus;
    }
}
