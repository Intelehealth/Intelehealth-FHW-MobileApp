package org.intelehealth.app.activities.prescription.thermalprinter;

import org.intelehealth.app.models.dto.ObsDTO;

public class PrintViewPrescriptionDataModel {
    /*
        visitUuid, patHistory, famHistory, height, weight, bpSys, bpDias, pulse, hasLicense,
        temperature, resp, spO2, complaint, rxReturned, testsReturned, medicalAdviceTextView,
        diagnosisReturned, followUpDate, doctorName, prescription1, prescription2, mBP
     */
    private String visitUuid;
    private ObsDTO patHistory;
    private ObsDTO famHistory;
    private ObsDTO height;
    private ObsDTO weight;
    private ObsDTO bpSys;

    public boolean isHasLicense() {
        return hasLicense;
    }

    public void setHasLicense(boolean hasLicense) {
        this.hasLicense = hasLicense;
    }

    private ObsDTO bpDias;
    private ObsDTO pulse;
    private boolean hasLicense;
    private ObsDTO temperature;
    private ObsDTO resp;
    private ObsDTO spO2;

    private ObsDTO bloodGlucoseRandom;
    private ObsDTO bloodGlucoseFasting;
    private ObsDTO bloodGlucosePostPrandial;

    public ObsDTO getBloodGlucoseRandom() {
        return bloodGlucoseRandom;
    }

    public void setBloodGlucoseRandom(ObsDTO bloodGlucoseRandom) {
        this.bloodGlucoseRandom = bloodGlucoseRandom;
    }

    public ObsDTO getBloodGlucoseFasting() {
        return bloodGlucoseFasting;
    }

    public void setBloodGlucoseFasting(ObsDTO bloodGlucoseFasting) {
        this.bloodGlucoseFasting = bloodGlucoseFasting;
    }

    public ObsDTO getBloodGlucosePostPrandial() {
        return bloodGlucosePostPrandial;
    }

    public void setBloodGlucosePostPrandial(ObsDTO bloodGlucosePostPrandial) {
        this.bloodGlucosePostPrandial = bloodGlucosePostPrandial;
    }

    public ObsDTO getHemoglobin() {
        return hemoglobin;
    }

    public void setHemoglobin(ObsDTO hemoglobin) {
        this.hemoglobin = hemoglobin;
    }

    public ObsDTO getCholesterol() {
        return cholesterol;
    }

    public void setCholesterol(ObsDTO cholesterol) {
        this.cholesterol = cholesterol;
    }

    public ObsDTO getUricAcid() {
        return uricAcid;
    }

    public void setUricAcid(ObsDTO uricAcid) {
        this.uricAcid = uricAcid;
    }

    private ObsDTO hemoglobin;
    private ObsDTO cholesterol;
    private ObsDTO uricAcid;

    public String getVisitUuid() {
        return visitUuid;
    }

    public void setVisitUuid(String visitUuid) {
        this.visitUuid = visitUuid;
    }

    public ObsDTO getPatHistory() {
        return patHistory;
    }

    public void setPatHistory(ObsDTO patHistory) {
        this.patHistory = patHistory;
    }

    public ObsDTO getFamHistory() {
        return famHistory;
    }

    public void setFamHistory(ObsDTO famHistory) {
        this.famHistory = famHistory;
    }

    public ObsDTO getHeight() {
        return height;
    }

    public void setHeight(ObsDTO height) {
        this.height = height;
    }

    public ObsDTO getWeight() {
        return weight;
    }

    public void setWeight(ObsDTO weight) {
        this.weight = weight;
    }

    public ObsDTO getBpSys() {
        return bpSys;
    }

    public void setBpSys(ObsDTO bpSys) {
        this.bpSys = bpSys;
    }

    public ObsDTO getBpDias() {
        return bpDias;
    }

    public void setBpDias(ObsDTO bpDias) {
        this.bpDias = bpDias;
    }

    public ObsDTO getPulse() {
        return pulse;
    }

    public void setPulse(ObsDTO pulse) {
        this.pulse = pulse;
    }


    public ObsDTO getTemperature() {
        return temperature;
    }

    public void setTemperature(ObsDTO temperature) {
        this.temperature = temperature;
    }

    public ObsDTO getResp() {
        return resp;
    }

    public void setResp(ObsDTO resp) {
        this.resp = resp;
    }

    public ObsDTO getSpO2() {
        return spO2;
    }

    public void setSpO2(ObsDTO spO2) {
        this.spO2 = spO2;
    }

    public ObsDTO getComplaint() {
        return complaint;
    }

    public void setComplaint(ObsDTO complaint) {
        this.complaint = complaint;
    }

    public String getRxReturned() {
        return rxReturned;
    }

    public void setRxReturned(String rxReturned) {
        this.rxReturned = rxReturned;
    }

    public String getTestsReturned() {
        return testsReturned;
    }

    public void setTestsReturned(String testsReturned) {
        this.testsReturned = testsReturned;
    }

    public String getDiagnosisReturned() {
        return diagnosisReturned;
    }

    public void setDiagnosisReturned(String diagnosisReturned) {
        this.diagnosisReturned = diagnosisReturned;
    }

    public String getFollowUpDate() {
        return followUpDate;
    }

    public void setFollowUpDate(String followUpDate) {
        this.followUpDate = followUpDate;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getPrescription1() {
        return prescription1;
    }

    public void setPrescription1(String prescription1) {
        this.prescription1 = prescription1;
    }

    public String getPrescription2() {
        return prescription2;
    }

    public void setPrescription2(String prescription2) {
        this.prescription2 = prescription2;
    }


    public String getBP() {
        return BP;
    }

    public void setBP(String BP) {
        this.BP = BP;
    }

    private ObsDTO complaint;
    private String rxReturned;
    private String testsReturned;
    private String diagnosisReturned;

    public String getMedicalAdvice() {
        return medicalAdvice;
    }

    public void setMedicalAdvice(String medicalAdvice) {
        this.medicalAdvice = medicalAdvice;
    }

    private String followUpDate;
    private String doctorName;
    private String prescription1;
    private String prescription2;
    private String BP;
    private String medicalAdvice;

}
