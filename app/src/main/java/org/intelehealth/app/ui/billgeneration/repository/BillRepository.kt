package org.intelehealth.app.ui.billgeneration.repository

import android.content.Context
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.intelehealth.app.R
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.database.dao.EncounterDAO
import org.intelehealth.app.database.dao.ImagesPushDAO
import org.intelehealth.app.database.dao.ObsDAO
import org.intelehealth.app.database.dao.SyncDAO
import org.intelehealth.app.models.dto.EncounterDTO
import org.intelehealth.app.models.dto.ObsDTO
import org.intelehealth.app.ui.billgeneration.models.BillDetails
import org.intelehealth.app.ui.billgeneration.utils.BillRate
import org.intelehealth.app.utilities.NetworkConnection
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.UuidDictionary
import org.intelehealth.app.utilities.exception.DAOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class BillRepository(private val sessionManager: SessionManager, private val context: Context) {
    private var notPayingReason: String = ""
    private var paymentStatusValue: String = ""

    fun setNotPayingReason(reason: String) {
        notPayingReason = reason
    }

    fun confirmBill(billDetails: BillDetails, paymentStatus: String): Boolean {
        // Save bill data to the server or database
        paymentStatusValue =paymentStatus
        val encounterUuid = getEncounterUuid(billDetails)
        if (encounterUuid.isNotEmpty()) {
            Log.d("billkz", "confirmBill: billDetails insert : "+Gson().toJson(billDetails))
            Log.d("billkz", "confirmBill: billtype : "+billDetails.billType)
            Log.d("billkz", "confirmBill: paymentStatus : "+paymentStatus)

            val listOfBillObs =  createObservations(encounterUuid, billDetails)
            val obsDAO = ObsDAO()
            try {
                listOfBillObs.forEach { obsDTO ->
                    obsDAO.insertObs(obsDTO)
                }

            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
            }
            return true

        }
        return false
    }

    private fun createObservations(encounterUuid: String, billDetails: BillDetails) =
        arrayListOf<ObsDTO>()
            .apply {
                add(createObs(UuidDictionary.BILL_DATE, encounterUuid, billDetails.billDateString))
               add(createObs(UuidDictionary.BILL_VISIT_TYPE, encounterUuid, billDetails.visitType)) //consultation or follow up
                add(createObs(UuidDictionary.BILL_PAYMENT_STATUS, encounterUuid, paymentStatusValue)) //paid or unpaid
                add(createObs(UuidDictionary.BILL_NUM, encounterUuid, billDetails.receiptNum))
                if(billDetails.selectedTestsList.contains(context.getString(R.string.blood_glucose_non_fasting)))  add(createObs(UuidDictionary.BILL_PRICE_BLOOD_GLUCOSE_ID, encounterUuid,  BillRate.GLUCOSE_NON_FASTING.value.toString()))
                if(billDetails.selectedTestsList.contains(context.getString(R.string.blood_glucose_fasting)))add(createObs(UuidDictionary.BILL_PRICE_BLOOD_GLUCOSE_FASTING_ID, encounterUuid,  BillRate.GLUCOSE_FASTING.value.toString()))
                if(billDetails.selectedTestsList.contains(context.getString(R.string.blood_glucose_post_prandial))) add(createObs(UuidDictionary.BILL_PRICE_BLOOD_GLUCOSE_POST_PRANDIAL_ID, encounterUuid,  BillRate.GLUCOSE_POST_PRANDIAL.value.toString()))
                if(billDetails.selectedTestsList.contains(context.getString(R.string.blood_glucose_random)))add(createObs(UuidDictionary.BILL_PRICE_BLOOD_GLUCOSE_RANDOM_ID, encounterUuid,  BillRate.GLUCOSE_RANDOM.value.toString()))
                if(billDetails.selectedTestsList.contains(context.getString(R.string.uric_acid))) add(createObs(UuidDictionary.BILL_PRICE_URIC_ACID_ID, encounterUuid,  BillRate.URIC_ACID.value.toString()))
                if(billDetails.selectedTestsList.contains(context.getString(R.string.total_cholestrol)))add(createObs(UuidDictionary.BILL_PRICE_TOTAL_CHOLESTEROL_ID, encounterUuid,  BillRate.CHOLESTEROL.value.toString()))
                if(billDetails.selectedTestsList.contains(context.getString(R.string.haemoglobin)))add(createObs(UuidDictionary.BILL_PRICE_HEMOGLOBIN_ID, encounterUuid,  BillRate.HEMOGLOBIN.value.toString()))
                if(billDetails.selectedTestsList.contains(context.getString(R.string.visit_summary_bp)))add(createObs(UuidDictionary.BILL_PRICE_BP_ID, encounterUuid,  BillRate.BP.value.toString()))
            }

    private fun createObs(
        conceptUuid: String,
        encounterUuid: String,
        value: String?

    ) = ObsDTO().apply {
        val updatedValue = when (conceptUuid) {
            UuidDictionary.BILL_VISIT_TYPE ->
                "$value - ${if (value.equals("Consultation", ignoreCase = true)) "15" else "10"}"

            UuidDictionary.BILL_PAYMENT_STATUS ->
                paymentStatusValue

            else -> value
        }

        conceptuuid = conceptUuid
        encounteruuid = encounterUuid
        creator = sessionManager.creatorID
        uuid = AppConstants.NEW_UUID
        this.value = updatedValue
    }


    private fun createEncounter(
        encounterUuid: String,
        thisDate: String,
        billDetails: BillDetails?
    ): Boolean {
        var success = false
        val encounterDTO = EncounterDTO().apply {
            uuid = encounterUuid
            encounterTypeUuid = UuidDictionary.VISIT_BILLING_DETAILS
            encounterTime = thisDate
            visituuid = billDetails?.patientVisitID ?: return false
            syncd = false
            provideruuid = sessionManager.providerID
            voided = 0
            privacynotice_value = "Accept" // privacy value added.
        }

        try {
            success = EncounterDAO().createEncountersToDB(encounterDTO)
        } catch (e: DAOException) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }

        return success
    }

    private fun getEncounterUuid(billDetails: BillDetails?): String {
        val currentDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH)
        val todayDate = Date()
        val thisDate: String = currentDate.format(todayDate)
        val encounterUuid = UUID.randomUUID().toString()
        var success = createEncounter(encounterUuid, thisDate, billDetails)
        return if (success) encounterUuid else ""
    }
    suspend fun syncOnServer() {
        withContext(Dispatchers.IO) {
            if (NetworkConnection.isOnline(IntelehealthApplication.getAppContext())) {
                val syncDAO = SyncDAO()
                val imagesPushDAO = ImagesPushDAO()
                syncDAO.pushDataApi()
                imagesPushDAO.patientProfileImagesPush()
            }        }

    }
}
