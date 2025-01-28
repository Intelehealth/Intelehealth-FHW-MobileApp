package org.intelehealth.app.activities.prescription.thermalprinter

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import org.intelehealth.app.app.AppConstants.CONFIG_FILE_NAME
import org.intelehealth.app.models.ClsDoctorDetails
import org.intelehealth.app.models.Patient
import org.intelehealth.app.app.AppConstants.CONFIG_FILE_NAME

import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.widget.Toast


import org.apache.commons.lang3.StringUtils
import org.intelehealth.app.R
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.knowledgeEngine.Node


import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PrintViewPrescription(
    private val context: Context,
    private val clsDoctorDetails: ClsDoctorDetails?,
    private val patient: Patient,
    private val dataModel: PrintViewPrescriptionDataModel,
    private val activityContext: AppCompatActivity
) {

    private var mHeight: String? = null
    private var mWeight: String? = null
    private var mBMI: String? = null
    private var mBP: String? = null
    private var mPulse: String? = null
    private var mTemp: String? = null
    private var mSPO2: String? = null
    private var mResp: String? = null

    companion object {
        private val mFileName = CONFIG_FILE_NAME
        private const val TAG = "PrintViewPrescription"
    }

    fun textPrint() {
        if (clsDoctorDetails != null) {
            val htmlDocPrescription = generatePrescriptionHtml()
            Log.d(TAG, "textPrint: presckaveri1 : $htmlDocPrescription")
            val htmlDoctorDetails = getDoctorDetailsHTML()
            val intentEsc = Intent(context, TextPrintESCActivity::class.java).apply {
                putExtra("sms_prescripton", htmlDocPrescription)
                putExtra("doctorDetails", htmlDoctorDetails)
                putExtra("font-family", clsDoctorDetails.fontOfSign)
                putExtra("drSign-text", clsDoctorDetails.textOfSign)
            }
            context.startActivity(intentEsc)
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.no_prescription_available),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getPatientAge(mPatientDob: String): Int {
        val today = Calendar.getInstance()
        val dob = Calendar.getInstance()

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val date = try {
            sdf.parse(mPatientDob)
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
        date?.let { dob.time = it }
        return today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
    }

    private fun getDoctorDetailsHTML(): String {
        var doctrRegistartionNum = ""
        var doctorDetailStr = ""
        clsDoctorDetails?.let {
            doctrRegistartionNum = if (!it.registrationNumber.isNullOrEmpty()) {
                "Registration No: ${it.registrationNumber}"
            } else {
                ""
            }
            doctorDetailStr =
                "<br><span style=\"font-size:12pt; color:#212121;padding: 0px;\">${it.name}</span><br>" +
                        "<span style=\"font-size:12pt; color:#212121;padding: 0px;\"> ${it.qualification} ${it.specialization}</span><br>$doctrRegistartionNum"
            Log.e("precs", "htmlpresc_doctor: ${Html.fromHtml(doctorDetailStr)}")
        }
        return doctorDetailStr
    }

    private fun convertCtoF(temperature: String): String {
        val nf = NumberFormat.getInstance(Locale.ENGLISH)
        val a = temperature.toDouble()
        val b = (a * 9 / 5) + 32
        val roundOff = Math.round(b * 100.0) / 100.0
        return nf.format(roundOff)
    }

    private fun stringToWeb(input: String?): String {
        var formatted = ""
        if (!input.isNullOrEmpty()) {
            val paraOpen = "<p style=\"font-size:11pt; margin: 0px; padding: 0px;\">"
            val paraClose = "</p>"
            formatted = "$paraOpen${Node.big_bullet}${
                input.replace(
                    "\n",
                    "$paraClose$paraOpen${Node.big_bullet}"
                )
            }$paraClose"
        }
        return formatted
    }

    fun parseDateToddMMyyyy(time: String): String {
        val inputPattern = "dd-MM-yyyy"
        val outputPattern = "dd MMM yyyy"
        val inputFormat = SimpleDateFormat(inputPattern, Locale.ENGLISH)
        val outputFormat = SimpleDateFormat(outputPattern, Locale.ENGLISH)

        val date: Date?
        var str: String? = null

        try {
            date = inputFormat.parse(time)
            str = outputFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return str ?: ""
    }

    private fun adviceFromDoctor(): String {
        var adviceWeb = ""
        val medicalAdviceTextViewText = dataModel.medicalAdvice
        if (!medicalAdviceTextViewText.isNullOrEmpty()) {
            val adviceDoctor = medicalAdviceTextViewText.replace(
                "Start Audio Call with Doctor",
                "Start Audio Call with Doctor_"
            ).replace("Start WhatsApp Call with Doctor", "Start WhatsApp Call with Doctor_")

            val startIndex = adviceDoctor.indexOf("Start")
            val endIndex = adviceDoctor.lastIndexOf("Doctor_") + 9

            // Ensure indices are valid before attempting to delete
            adviceWeb = if (startIndex != -1 && endIndex > startIndex) {
                val adviceSplit = StringBuilder(adviceDoctor)
                    .delete(startIndex, endIndex)
                    .toString()
                stringToWeb(adviceSplit.replace("\n\n", "\n")).replace(Node.big_bullet, "- ")
            } else {
                stringToWeb(adviceDoctor.replace("\n\n", "\n")).replace(Node.big_bullet, "- ")
            }

            Log.d("Hyperlink", "hyper_print: $adviceWeb")
        }
        return adviceWeb
    }

    private fun formatPatientDetails(
        heading: String,
        heading2: String,
        mPatientName: String,
        age: Int,
        mGender: String
    ): String {
        return "<b id=\"heading_1\">$heading</b><br><b id=\"heading_2\" style=\"font-size:5pt; margin: 0px; padding: 0px; text-align: center;\">$heading2</b><br> ----------------------------------------------- <br><br><b id=\"patient_name\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">$mPatientName</b><br><id=\"patient_details\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">Age: $age | Gender: $mGender<br><br>"
    }

    private fun formatDiagnostics(): String {
        return "<b id=\"diagnostics_heading\">* Diagnostics</b><br><id=\"diagnostics\" " +
                "style=\"font-size:12pt;margin:0px; padding: 0px;\">-Glucose (Random):" +
                " ${dataModel.bloodGlucoseRandom.value ?: ""} <br> -Glucose (Fasting): ${dataModel.bloodGlucoseFasting.value ?: ""} " +
                "<br> -Glucose (Post-Prandial): " +
                "${dataModel.bloodGlucosePostPrandial.value ?: ""} <br> -HGB: ${dataModel.hemoglobin.value ?: ""} <br> -Uric Acid:" +
                " ${dataModel.uricAcid.value ?: ""} <br> -Total Cholesterol: ${dataModel.cholesterol.value ?: ""} <br><br>"
    }

    private fun formatDiagnosis(): String {
        var htmlDocument = ""
        val diagnosisWeb = stringToWebSms(dataModel.diagnosisReturned)
        if (diagnosisWeb.isNotEmpty()) {
            htmlDocument = "<b id=\"diagnosis_heading\" >* Diagnosis </b><br>$diagnosisWeb <br>"
        }
        return htmlDocument
    }

    private fun formatMedicationPlan(): String {
        var htmlDocument = ""
        val rxWeb = stringToWebSms(
            dataModel.rxReturned.trim().replace("\n\n", "\n").replace(Node.bullet, "")
        )
        if (rxWeb.isNotEmpty()) {
            htmlDocument = "<b id=\"rx_heading\" >* Medication(s) plan </b><br>$rxWeb <br>"
        }
        return htmlDocument
    }

    private fun formatPrescribedTests(): String {
        var htmlDocument = ""
        Log.d(TAG, "formatPrescribedTests: tests  : ${dataModel.testsReturned}")
        if (dataModel.testsReturned.isNotEmpty()) {
            htmlDocument =
                "<b id=\"tests_heading\" >* Recommended Investigation(s) </b><br>${dataModel.testsReturned} <br>"
        }
        return htmlDocument
    }

    private fun generatePrescriptionHtml(): String {
        val mPatientName = patient.first_name + " " +
                (if (!TextUtils.isEmpty(patient.middle_name)) patient.middle_name else "") + " " +
                (if (!TextUtils.isEmpty(patient.last_name)) patient.last_name else "")

        val prescriptionHtml = StringBuilder()
            .append(
                formatPatientDetails(
                    dataModel.prescription1,
                    dataModel.prescription2,
                    mPatientName,
                    getPatientAge(patient.date_of_birth),
                    patient.gender
                )
            )
            .append(formatDiagnostics())
            .append(formatDiagnosis())
            .append(formatMedicationPlan())
            .append(formatPrescribedTests())
            .append(formatAdviceFromDoctor())
            .append(formatFollowUpDate())
            .toString()

        Log.d(TAG, "Generated Prescription HTML: $prescriptionHtml")
        return prescriptionHtml
    }

    private fun formatAdviceFromDoctor(): String {
        var htmlDocument = ""
        val adviceFromDr = adviceFromDoctor()
        if (adviceFromDr.isNotEmpty()) {
            htmlDocument = "<b id=\"advice_heading\">* Advice </b><br>$adviceFromDr<br>"
        }
        return htmlDocument
    }

    private fun formatFollowUpDate(): String {
        var htmlDocument = ""
        val followUpWeb = followUpWeb()
        if (followUpWeb.isNotEmpty()) {
            htmlDocument = "<b id=\"follow_up_heading\">* Follow Up Date </b><br>$followUpWeb<br>"
        }
        return htmlDocument
    }

    private fun followUpWeb(): String {
        var followUpWeb = ""
        var followUpDateStr = ""
        val followUpDate = dataModel.followUpDate

        if (!followUpDate.isNullOrEmpty() && followUpDate.contains(",")) {
            val splitFollowDate = followUpDate.split(",")
            if (splitFollowDate[0].contains("-")) {
                var remainingStr = ""
                for (i in 1 until splitFollowDate.size) {
                    remainingStr = if (remainingStr.isNotEmpty()) {
                        "$remainingStr, ${splitFollowDate[i]}"
                    } else {
                        splitFollowDate[i]
                    }
                }
                followUpDateStr = "${parseDateToddMMyyyy(splitFollowDate[0])}, $remainingStr"
            } else {
                followUpDateStr = followUpDate
            }
        } else {
            followUpDateStr = followUpDate ?: ""
        }
        followUpWeb = stringToWebSms(followUpDateStr)
        return followUpWeb
    }

    private fun stringToWebSms(input: String?): String {
        var formatted = ""
        if (!input.isNullOrEmpty()) {
            val paraOpen = "<style=\"font-size:11pt; margin: 0px; padding: 0px;\">"
            val paraClose = "<br>"
            formatted = "$paraOpen- ${input.replace("\n", "$paraClose$paraOpen- ")}$paraClose"
        }
        return formatted
    }

}
