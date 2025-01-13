
package org.intelehealth.app.models.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PatientAttributesDTO implements Serializable {

    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("value")
    @Expose
    private String value;
    @SerializedName("person_attribute_type_uuid")
    @Expose
    private String personAttributeTypeUuid;
    @SerializedName("patientuuid")
    @Expose
    private String patientuuid;


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getPersonAttributeTypeUuid() {
        return personAttributeTypeUuid;
    }

    public void setPersonAttributeTypeUuid(String personAttributeTypeUuid) {
        this.personAttributeTypeUuid = personAttributeTypeUuid;
    }

    public String getPatientuuid() {
        return patientuuid;
    }

    public void setPatientuuid(String patientuuid) {
        this.patientuuid = patientuuid;
    }

    @Override
    public String toString() {
        return "PatientAttributesDTO{" +
                "uuid='" + uuid + '\'' +
                ", value='" + value + '\'' +
                ", personAttributeTypeUuid='" + personAttributeTypeUuid + '\'' +
                ", patientuuid='" + patientuuid + '\'' +
                '}';
    }

    public enum Column {
        TELEPHONE("Telephone Number"),
        ECONOMIC_STATUS("Economic Status"),
        EDUCATION("Education Level"),
        PROVIDER_ID("providerUUID"),
        OCCUPATION("occupation"),
        SWD("Son/wife/daughter"),
        NATIONAL_ID("NationalID"),
        PROFILE_IMG_TIMESTAMP("ProfileImageTimestamp"),
        CAST("Caste"),
        CREATED_DATE("createdDate"),
        TMH_CASE_NUMBER("TMH Case Number"),
        REQUEST_ID("Request ID"),
        RELATIVE_PHONE_NUMBER("Relative Phone Number"),
        DISCIPLINE("Discipline"),
        DEPARTMENT("Department"),
        PROVINCES("Provinces"),
        CITIES("Cities"),
        REGISTRATION_ADDRESS_OF_HF("Registration address of health facility"),
        INN("INN"),
        CODE_OF_HEALTH_FACILITY("Code of the Health Facility"),
        HEALTH_FACILITY_NAME("Health facility name"),
        CODE_OF_DEPARTMENT("Code of the Department"),
        HOUSEHOLD_UUID_LINKING("HouseHold"),
        BLOCK("blockSurvey"),

        //household survey attributes
        HOUSE_STRUCTURE("HouseStructure"),
        RESULT_OF_VISIT("ResultOfVisit"),
        NAME_OF_PRIMARY_RESPONDENT("NamePrimaryRespondent"),
        REPORT_DATE_OF_PATIENT_CREATED("ReportDate of patient created"),
        HOUSEHOLD_NUMBER_OF_SURVEY("HouseholdNumber"),
        REPORT_DATE_OF_SURVEY_STARTED("ReportDate of survey started"),
        //second screen
        NUMBER_OF_SMARTPHONES("noOfSmartphones"),
        NUMBER_OF_FEATURE_PHONES("noOfFeaturePhones"),
        HOUSEHOLD_HEAD_NAME("householdHeadName"),
        HOUSEHOLD_HEAD_RELIGION("householdHeadReligion"),
        HOUSEHOLD_HEAD_CAST("householdHeadCaste"),
        NUMBER_OF_EARNING_MEMBERS("noOfEarningMembers"),
        PRIMARY_SOURCE_OF_INCOME("primarySourceOfIncome"),

        //third screen
        HOUSEHOLD_ELECTRICITY_STATUS("householdElectricityStatus"),
        NO_OF_LOAD_SHEDDING_HRS_PER_DAY("noOfLoadSheddingHrsPerDay"),
        NO_OF_LOAD_SHEDDING_HRS_PER_WEEK("noOfLoadSheddingHrsPerWeek"),
        RUNNING_WATER_STATUS("runningWaterStatus"),
        PRIMARY_SOURCE_OF_RUNNING_WATER("primarySourceOfRunningWater"),
        WATER_SOURCE_DISTANCE("waterSourceDistance"),
        WATER_SUPPLY_AVAILABILITY_HRS_PER_DAY("waterSupplyAvailabilityHrsPerDay"),
        WATER_AVAILABILITY_DAYS_PER_WEEK("waterSupplyAvailabilityDaysperWeek"),
        HOUSEHOLD_BANK_ACCOUNT_STATUS("householdBankAccountStatus"),
        //fourth screen
        HOUSEHOLD_CULTIVABLE_LAND("householdCultivableLand"),
        AVERAGE_ANNUAL_HOUSEHOLD_INCOME("averageAnnualHouseholdIncome"),
        MONTHLY_FOOD_EXPENDITURE("monthlyFoodExpenditure"),
        ANNUAL_HEALTH_EXPENDITURE("annualHealthExpenditure"),
        ANNUAL_EDUCATION_EXPENDITURE("annualEducationExpenditure"),
        ANNUAL_CLOTHING_EXPENDITURE("annualClothingExpenditure"),
        MONTHLY_INTOXICANTS_EXPENDITURE("monthlyIntoxicantsExpenditure"),
        HOUSEHOLD_BPL_CARD_STATUS("householdBPLCardStatus"),
        HOUSEHOLD_ANTODAYA_CARD_STATUS("householdAntodayaCardStatus"),
        HOUSEHOLD_RSBY_CARD_STATUS("householdRSBYCardStatus"),
        HOUSEHOLD_MGNREGA_CARD_STATUS("householdMGNREGACardStatus"),
        //fifth screen
        COOKING_FUEL_TYPE("cookingFuelType"),
        MAIN_LIGHTING_SOURCE("mainLightingSource"),
        MAIN_DRINKING_WATER_SOURCE("mainDrinkingWaterSource"),
        SAFER_WATER_PROCESS("saferWaterProcess"),
        HOUSEHOLD_TOILET_FACILITY("householdToiletFacility"),
        //sixth fragment
        HOUSEHOLD_OPEN_DEFECATION_STATUS("householdOpenDefecationStatus"),
        FOOD_ITEMS_PREPARED_IN_TWENTY_FOUR_HRS("foodItemsPreparedInTwentyFourHrs"),

        //seventh fragment
        SUB_CENTRE_DISTANCE("subCentreDistance"),
        NEAREST_PRIMARY_HEALTH_CENTER_DISTANCE("nearestPrimaryHealthCenterDistance"),

        NEAREST_COMMUNITY_HEALTH_CENTER_DISTANCE("nearestCommunityHealthCenterDistance"),
        NEAREST_DISTRICT_HOSPITAL_DISTANCE("nearestDistrictHospitalDistance"),
        NEAREST_PATHOLOGICAL_LAB_DISTANCE("nearestPathologicalLabDistance"),
        NEAREST_PRIVATE_CLINIC_MBBS_DOCTOR("nearestPrivateClinicMBBSDoctor"),
        NEAREST_PRIVATE_CLINIC_ALTERNATE_MEDICINE("nearestPrivateClinicAlternateMedicine"),
        NEAREST_TERTIARY_CARE_FACILITY("nearestTertiaryCareFacility"),
        EMERGENCY_CONTACT_NAME("Emergency Contact Name"),
        EMERGENCY_CONTACT_NUMBER("Emergency Contact Number"),
        EMERGENCY_CONTACT_TYPE("Emergency Contact Type");


        public final String value;


        Column(String value) {
            this.value = value;
        }
    }
}