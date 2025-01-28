package org.intelehealth.app.activities.bill

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.CompoundButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import org.intelehealth.app.R
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.database.dao.EncounterDAO
import org.intelehealth.app.databinding.ConfirmTestDialogBinding
import org.intelehealth.app.models.Patient
import org.intelehealth.app.ui.billgeneration.activity.BillCreationActivity
import org.intelehealth.app.ui.billgeneration.models.BillDetails
import org.intelehealth.app.utilities.UuidDictionary
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

class VisitSummaryBillUtils(
    private val mContext: Context,
    private val patient: Patient,
    private val billModel: BillDetails
) {
    private val TAG = "VisitSummaryBillUtils"
    private var receiptNumValue: String? = null
    private var receiptDateValue: String? = null
    private var receiptPaymentStatusValue = "NA"
    private var billEncounterUuid = ""
    fun checkForOldBill(): String {

        val db = IntelehealthApplication.inteleHealthDatabaseHelper.writableDatabase
        val encounterDAO = EncounterDAO()
        val encounterIDSelection = "visituuid = ? AND voided = ?"
        val encounterIDArgs = arrayOf(billModel.patientVisitID, "0")
        val encounterCursor =
            db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null)

        encounterCursor?.use {
            while (it.moveToNext()) {
                val encounterTypeUuid = "7030c68e-eecc-4656-bb0a-e465aea6195f"
                if (encounterTypeUuid == it.getString(it.getColumnIndexOrThrow("encounter_type_uuid"))) {
                    billEncounterUuid = it.getString(it.getColumnIndexOrThrow("uuid"))
                }
            }
        }

        return billEncounterUuid
    }

    fun fetchBillDetails(billEncounterUuid: String) {
        val selectedTests = ArrayList<String>()
        val columns = arrayOf("value", "conceptuuid")
        val visitSelection = "encounteruuid = ? and voided = ?"
        val visitArgs = arrayOf(billEncounterUuid, "0")
        val db = IntelehealthApplication.inteleHealthDatabaseHelper.writableDatabase
        val visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, null)

        visitCursor?.use {
            while (it.moveToNext()) {
                val dbConceptID = it.getString(it.getColumnIndex("conceptuuid"))
                val dbValue = it.getString(it.getColumnIndex("value"))
                if (dbValue != null && dbValue != "0") {
                    parseBillData(selectedTests, dbConceptID, dbValue)
                }
            }
        }

        passIntent(selectedTests)
    }

    private fun parseBillData(selectedTests: ArrayList<String>, conceptId: String, value: String) {
        when (conceptId) {
            UuidDictionary.BILL_NUM -> {
                receiptNumValue = value
            }

            UuidDictionary.BILL_DATE -> {
                receiptDateValue = value
            }

            UuidDictionary.BILL_PAYMENT_STATUS -> {
                receiptPaymentStatusValue = value
            }

            UuidDictionary.BILL_PRICE_BP_ID -> {
                selectedTests.add(mContext.getString(R.string.visit_summary_bp))
            }

            UuidDictionary.BILL_PRICE_BLOOD_GLUCOSE_ID -> {
                selectedTests.add(mContext.getString(R.string.blood_glucose_non_fasting))
            }

            UuidDictionary.BILL_PRICE_BLOOD_GLUCOSE_RANDOM_ID -> {
                selectedTests.add(mContext.getString(R.string.blood_glucose_random))
            }

            UuidDictionary.BILL_PRICE_BLOOD_GLUCOSE_POST_PRANDIAL_ID -> {
                selectedTests.add(mContext.getString(R.string.blood_glucose_post_prandial))
            }

            UuidDictionary.BILL_PRICE_BLOOD_GLUCOSE_FASTING_ID -> {
                selectedTests.add(mContext.getString(R.string.blood_glucose_fasting))
            }

            UuidDictionary.BILL_PRICE_HEMOGLOBIN_ID -> {
                selectedTests.add(mContext.getString(R.string.haemoglobin))
            }

            UuidDictionary.BILL_PRICE_URIC_ACID_ID -> {
                selectedTests.add(mContext.getString(R.string.uric_acid))
            }

            UuidDictionary.BILL_PRICE_TOTAL_CHOLESTEROL_ID -> {
                selectedTests.add(mContext.getString(R.string.total_cholestrol))
            }

            else -> {
                Log.i(TAG, "parseData: $value")
            }
        }
    }

    fun showTestConfirmationCustomDialog(checkedTests: BooleanArray) {
        val selectedTests = ArrayList<String>()
        val testNames = arrayOf(
            mContext.getString(R.string.visit_summary_bp),
            mContext.getString(R.string.blood_glucose_non_fasting),
            mContext.getString(R.string.blood_glucose_fasting),
            mContext.getString(R.string.blood_glucose_post_prandial),
            mContext.getString(R.string.blood_glucose_random),
            mContext.getString(R.string.uric_acid),
            mContext.getString(R.string.total_cholestrol),
            mContext.getString(R.string.haemoglobin)
        )

        val dialogView = LayoutInflater.from(mContext).inflate(R.layout.confirm_test_dialog, null)
        val binding = ConfirmTestDialogBinding.bind(dialogView)
        val alertDialog = MaterialAlertDialogBuilder(mContext)
            .setView(binding.root)
            .setCancelable(false)
            .create()

        alertDialog.window?.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        alertDialog.setOnShowListener {
            val displayMetrics = mContext.resources.displayMetrics
            val width = (displayMetrics.widthPixels * 0.85).toInt()  // 85% of screen width
            val height = (displayMetrics.heightPixels * 0.70).toInt()  // 70% of screen height

            alertDialog.window?.setLayout(
                width,
                height  // Set height to 70% of screen
            )
        }

        alertDialog.window
            ?.setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg)
        /* binding.glucoseNfCB.isChecked = checkedTests[1]
         binding.glucoseFCB.isChecked = checkedTests[2]
         binding.glucosePpnCB.isChecked = checkedTests[3]
         binding.glucoseRanCB.isChecked = checkedTests[4]
         binding.uricAcidCB.isChecked = checkedTests[5]
         binding.cholesterolCB.isChecked = checkedTests[6]
         binding.haemoglobinCB.isChecked = checkedTests[7]
 */
        if (checkedTests[1]) binding.glucoseNfCB.isChecked = true
        if (checkedTests[2]) binding.glucoseFCB.isChecked = true
        if (checkedTests[3]) binding.glucosePpnCB.isChecked = true
        if (checkedTests[4]) binding.glucoseRanCB.isChecked = true
        if (checkedTests[5]) binding.uricAcidCB.isChecked = true
        if (checkedTests[6]) binding.cholesterolCB.isChecked = true
        if (checkedTests[7]) binding.haemoglobinCB.isChecked = true

        binding.bpCB.setOnClickListener { view ->
            checkedTests[0] = (view as CompoundButton).isChecked
        }

        binding.btnOkTests.setOnClickListener {
            for (i in checkedTests.indices) {
                if (checkedTests[i]) selectedTests.add(testNames[i])
            }
            receiptNumValue = generateReceiptNum()
            receiptDateValue = fetchSystemDateForBill()
            passIntent(selectedTests)
            alertDialog.dismiss()
        }

        binding.btnCancelTests.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    private fun passIntent(selectedTests: ArrayList<String>) {
        Log.d(TAG, "passIntent: receiptNumValue : "+receiptNumValue)
        Log.d(TAG, "passIntent: receiptDateValue : "+receiptDateValue)
        Log.d(TAG, "passIntent: selectedTests : "+Gson().toJson(selectedTests))

        billModel.apply {
            patientName = "${patient.first_name} ${patient.last_name}"
            patientOpenID = patient.openmrs_id ?: ""
            patientPhoneNum = patient.phone_number ?: ""
            patientVillage = patient.city_village ?: ""
            selectedTestsList = selectedTests as ArrayList<String?>
            receiptNum = receiptNumValue ?: ""
            billDateString = receiptDateValue ?: ""
            billType = receiptPaymentStatusValue
            patientVisitID = billModel.patientVisitID
            visitType = billModel.visitType
            patientHideVisitID = billModel.patientHideVisitID
            billEncounterUUID  = billEncounterUuid
        }
        Log.d(TAG, "passIntent: billDetails : "+Gson().toJson(billModel))
        val intent = Intent(mContext, BillCreationActivity::class.java)
        val args = Bundle().apply {
            putSerializable("billDetails", billModel as Serializable)
        }
        intent.putExtra("BUNDLE", args)
        mContext.startActivity(intent)
    }

    private fun generateReceiptNum(): String {
        val rnd = Random()
        val number = rnd.nextInt(9999)
        val first = patient.first_name[0]
        val last = patient.last_name[0]
        return "$first$last$number"
    }

    private fun fetchSystemDateForBill(): String {
        val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        return currentDate.format(Date())
    }
}