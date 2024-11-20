package org.intelehealth.app.activities.visit.staticEnabledFields

import org.intelehealth.app.utilities.UuidDictionary
import org.intelehealth.config.room.entity.PatientVital
import org.intelehealth.config.utility.PatientVitalConfigKeys

object VitalsEnabledFieldsHelper {

    fun getStaticVitalsEnabledFields(): List<PatientVital> {
        val fields: MutableList<PatientVital> = mutableListOf()

        // Height
        var currentField = PatientVital(
            name = "",
            vitalKey = PatientVitalConfigKeys.HEIGHT,
            uuid = UuidDictionary.HEIGHT,
            isMandatory = true
        )
        fields.add(currentField)

        // Weight
        currentField = PatientVital(
            name = "",
            vitalKey = PatientVitalConfigKeys.WEIGHT,
            uuid = UuidDictionary.WEIGHT,
            isMandatory = true
        )
        fields.add(currentField)

        // BMI
        currentField = PatientVital(
            name = "",
            vitalKey = PatientVitalConfigKeys.BMI,
            uuid = "",
            isMandatory = true
        )
        fields.add(currentField)

        // SBP
        currentField = PatientVital(
            name = "",
            vitalKey = PatientVitalConfigKeys.SBP,
            uuid = UuidDictionary.SYSTOLIC_BP,
            isMandatory = true
        )
        fields.add(currentField)

        // DBP
        currentField = PatientVital(
            name = "",
            vitalKey = PatientVitalConfigKeys.DBP,
            uuid = UuidDictionary.DIASTOLIC_BP,
            isMandatory = true
        )
        fields.add(currentField)

        // Pulse
        currentField = PatientVital(
            name = "",
            vitalKey = PatientVitalConfigKeys.PULSE,
            uuid = UuidDictionary.PULSE,
            isMandatory = true
        )
        fields.add(currentField)

        // Temperature
        currentField = PatientVital(
            name = "",
            vitalKey = PatientVitalConfigKeys.TEMPERATURE,
            uuid = UuidDictionary.TEMPERATURE,
            isMandatory = true
        )
        fields.add(currentField)

        // SPO2
        currentField = PatientVital(
            name = "",
            vitalKey = PatientVitalConfigKeys.SPO2,
            uuid = UuidDictionary.SPO2,
            isMandatory = true
        )
        fields.add(currentField)

        // Respiratory Rate
        currentField = PatientVital(
            name = "",
            vitalKey = PatientVitalConfigKeys.RESPIRATORY_RATE,
            uuid = UuidDictionary.RESPIRATORY,
            isMandatory = true
        )
        fields.add(currentField)

        // Blood Type
        currentField = PatientVital(
            name = "",
            vitalKey = PatientVitalConfigKeys.BLOOD_TYPE,
            uuid = UuidDictionary.BLOOD_GROUP,
            isMandatory = true
        )
        fields.add(currentField)

        // Haemoglobin
        currentField = PatientVital(
            name = "",
            vitalKey = PatientVitalConfigKeys.HAEMOGLOBIN,
            uuid = UuidDictionary.HAEMOGLOBIN,
            isMandatory = true
        )
        fields.add(currentField)

        // Sugar Random
        currentField = PatientVital(
            name = "",
            vitalKey = PatientVitalConfigKeys.SUGAR_RANDOM,
            uuid = UuidDictionary.SUGAR_LEVEL_RANDOM,
            isMandatory = true
        )
        fields.add(currentField)

        return fields
    }
}