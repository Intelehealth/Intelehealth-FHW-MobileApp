package org.intelehealth.app.activities.prescription.thermalprinter;

import static org.intelehealth.app.app.AppConstants.CONFIG_FILE_NAME;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.lang3.StringUtils;
import org.intelehealth.app.R;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.models.ClsDoctorDetails;
import org.intelehealth.app.models.Patient;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PrintViewPrescriptionTest {
    Context context;
    ClsDoctorDetails clsDoctorDetails;
    Patient patient;
    PrintViewPrescriptionDataModel dataModel;
    String mHeight, mWeight, mBMI, mBP, mPulse, mTemp, mSPO2, mresp;
    private static String mFileName = CONFIG_FILE_NAME;
    private static final String TAG = "PrintViewPrescription";
    private AppCompatActivity activityContext;

    public PrintViewPrescriptionTest(Context context, ClsDoctorDetails clsDoctorDetails, Patient patient, PrintViewPrescriptionDataModel dataModel, AppCompatActivity activityContext) {
        this.context = context;
        this.clsDoctorDetails = clsDoctorDetails;
        this.patient = patient;
        this.dataModel = dataModel;
        this.activityContext = activityContext;
    }

    public void textPrint() {
        if (clsDoctorDetails != null) {
            String htmlDocPrescription = generatePrescriptionHtml();
            Log.d(TAG, "textPrint: presckaveri1 : " + htmlDocPrescription);
            // Bitmap doctorSignature = getdoctorsignature();
            String htmlDoctorDetails = getDoctorDetailsHTML();
            Intent intent_esc = new Intent(context, TextPrintESCActivity.class);
            intent_esc.putExtra("sms_prescripton", htmlDocPrescription);
            intent_esc.putExtra("doctorDetails", htmlDoctorDetails);
            intent_esc.putExtra("font-family", clsDoctorDetails.getFontOfSign());
            intent_esc.putExtra("drSign-text", clsDoctorDetails.getTextOfSign());
            context.startActivity(intent_esc);
        } else {
            Toast.makeText(context, context.getResources().getString(R.string.no_prescription_available), Toast.LENGTH_SHORT).show();
        }

    }


    private int getPatientAge(String mPatientDob) {
        Calendar today = Calendar.getInstance();
        Calendar dob = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = sdf.parse(mPatientDob);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        dob.setTime(date);
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        return age;
    }

    private String getDoctorDetailsHTML() {
        // Generate an HTML document on the fly:
        String doctrRegistartionNum = "";
        // String docDigitallySign = "";
        String doctorDetailStr = "";
        if (clsDoctorDetails != null) {

            doctrRegistartionNum = !TextUtils.isEmpty(clsDoctorDetails.getRegistrationNumber()) ? "Registration No:" + clsDoctorDetails.getRegistrationNumber() : "";

            doctorDetailStr =/* "<div style=\"text-align:right;margin-right:0px;margin-top:3px;\">" +*/

                    "<br><span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + clsDoctorDetails.getName() + "</span><br>" + // Dr.Name
                            "<span style=\"font-size:12pt; color:#212121;padding: 0px;\">" + "  " + clsDoctorDetails.getQualification() //Dr. Qualifi
                            + " " + clsDoctorDetails.getSpecialization() + "</span><br>" + doctrRegistartionNum;
            Log.e("precs", "htmlpresc_doctor: " + Html.fromHtml(doctorDetailStr).toString());

        }
        return doctorDetailStr;
    }

    private String convertCtoF(String temperature) {
        String resultVal;
        NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
        double a = Double.parseDouble(temperature);
        double b = (a * 9 / 5) + 32;
        nf.format(b);
        double roundOff = Math.round(b * 100.0) / 100.0;
        resultVal = nf.format(roundOff);
        return resultVal;
    }

    private String stringToWeb(String input) {
        String formatted = "";
        if (input != null && !input.isEmpty()) {

            String para_open = "<p style=\"font-size:11pt; margin: 0px; padding: 0px;\">";
            String para_close = "</p>";
            formatted = para_open + Node.big_bullet + input.replaceAll("\n", para_close + para_open + Node.big_bullet) + para_close;
        }

        return formatted;
    }

    private String stringToWeb_sms(String input) {
        String formatted = "";
        if (input != null && !input.isEmpty()) {

            String para_open = "<style=\"font-size:11pt; margin: 0px; padding: 0px;\">";
            String para_close = "<br>";
            formatted = para_open + "- " + input.replaceAll("\n", para_close + para_open + "- ") + para_close;
        }
        return formatted;
    }

    public String parseDateToddMMyyyy(String time) {
        String inputPattern = "dd-MM-yyyy";
        String outputPattern = "dd MMM yyyy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern, Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern, Locale.ENGLISH);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    private String adviceFromDoctor() {
        String advice_web = "";
        String medicalAdviceTextViewText = dataModel.getMedicalAdvice();
        if (medicalAdviceTextViewText != null && !medicalAdviceTextViewText.isEmpty()) {
            String advice_doctor__ = medicalAdviceTextViewText.replace("Start Audio Call with Doctor", "Start Audio Call with Doctor_").replace("Start WhatsApp Call with Doctor", "Start WhatsApp Call with Doctor_");

            if (advice_doctor__.indexOf("Start") != -1 || advice_doctor__.lastIndexOf(("Doctor_") + 9) != -1) {
                String advice_split = new StringBuilder(advice_doctor__).delete(advice_doctor__.indexOf("Start"), advice_doctor__.lastIndexOf("Doctor_") + 9).toString();
                advice_web = stringToWeb(advice_split.replace("\n\n", "\n")); //showing advice here...
                advice_web = advice_web.replace(Node.big_bullet, "- ");
                Log.d("Hyperlink", "hyper_print: " + advice_web); //gets called when clicked on button of print button
            } else {
                advice_web = stringToWeb(advice_doctor__.replace("\n\n", "\n")); //showing advice here...
                advice_web = advice_web.replace(Node.big_bullet, "- ");
                Log.d("Hyperlink", "hyper_print: " + advice_web); //gets called when clicked on button of print button
            }
        }
        return advice_web;
    }

    private String followUp_web() {
        String followUp_web = "";
        String followUpDateStr = "";
        String followUpDate = dataModel.getFollowUpDate();
        if (followUpDate != null && followUpDate.contains(",")) {
            String[] spiltFollowDate = followUpDate.split(",");
            if (spiltFollowDate[0] != null && spiltFollowDate[0].contains("-")) {
                String remainingStr = "";
                for (int i = 1; i <= spiltFollowDate.length - 1; i++) {
                    remainingStr = ((!TextUtils.isEmpty(remainingStr)) ? remainingStr + ", " : "") + spiltFollowDate[i];
                }
                followUpDateStr = parseDateToddMMyyyy(spiltFollowDate[0]) + ", " + remainingStr;
            } else {
                followUpDateStr = followUpDate;
            }
        } else {
            followUpDateStr = followUpDate;
            followUp_web = stringToWeb_sms(followUpDateStr);

        }
        return followUp_web;
    }

    private String formatPatientDetails(String heading, String heading2, String mPatientName, int age, String mGender) {
        String htmlDocument = String.format("<b id=\"heading_1\">%s</b><br>" + "<b id=\"heading_2\" style=\"font-size:5pt; margin: 0px; padding: 0px; text-align: center;\">%s</b><br>" + " ----------------------------------------------- " + "<br><br>" + "<b id=\"patient_name\" style=\"font-size:12pt; margin: 0px; padding: 0px;\">%s</b><br>" + "<id=\"patient_details\" style=\"font-size:12pt; margin: 0px; padding:" +
                " 0px;\">Age: %s | Gender: %s" + "<br><br>", heading, heading2, mPatientName, age, mGender);
        return htmlDocument;
    }

    private String formatDiagnostics() {
        String htmlDocument = String.format("<b id=\"diagnostics_heading\">* Diagnostics</b><br>" + "<id=\"diagnostics\" style=\"font-size:12pt;margin:0px; padding: 0px;\">-Glucose (Random): %s <br> -Glucose (Fasting): %s <br> -Glucose (Post-Prandial): %s <br> -HGB: %s <br> -Uric Acid: %s <br> -Total Cholesterol: %s <br><br>", "101", "102", "103", "104", "105", "106");
        return htmlDocument;
    }

    private String formatDiagnosis() {
        String htmlDocument = "";
        String diagnosis_web = stringToWeb_sms(dataModel.getDiagnosisReturned());
        if (!diagnosis_web.isEmpty()) {
            htmlDocument = String.format("<b id=\"diagnosis_heading\" >* Diagnosis </b><br>" + "%s <br>", diagnosis_web);
        }
        return htmlDocument;
    }

    private String formatMedicationPlan() {
        String htmlDocument = "";
        String rx_web = stringToWeb_sms(dataModel.getRxReturned().trim().replace("\n\n", "\n").replace(Node.bullet, ""));
        if (!rx_web.isEmpty()) {
            htmlDocument = String.format("<b id=\"rx_heading\" >* Medication(s) plan </b><br>" + "%s <br>", rx_web);
        }
        return htmlDocument;
    }

    private String formatPrescribedTests() {
        String htmlDocument = "";
        Log.d(TAG, "formatPrescribedTests: tests  : " + dataModel.getTestsReturned());
        if (!dataModel.getTestsReturned().isEmpty()) {
            htmlDocument = String.format("<b id=\"tests_heading\" >* Recommended Investigation(s) </b><br>" + "%s " + "<br>", dataModel.getTestsReturned());
        }
        return htmlDocument;
    }

    private String formatAdviceFromDoctor() {
        String htmlDocument = "";
        String adviceFromDr = adviceFromDoctor();
        if (!adviceFromDr.isEmpty()) {
            htmlDocument = String.format("<b id=\"advice_heading\" >* Advice </b><br>" + "%s" + "<br>", adviceFromDr);
        }
        return htmlDocument;
    }

    private String formatFollowUpDate() {
        String htmlDocument = "";
        if (!followUp_web().isEmpty()) {
            htmlDocument = String.format("<b id=\"follow_up_heading\" >* Follow Up Date </b><br>" + "%s" + "<br>", followUp_web());
        }
        return htmlDocument;
    }

    private String getVisitStartDate(String visitUuid) {
        String[] columnsToReturn = {"startdate"};
        String selection = "uuid = ?";
        String[] selectionArgs = {visitUuid};
        String orderBy = "startdate";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        final Cursor cursor = db.query("tbl_visit", columnsToReturn, selection, selectionArgs, null, null, orderBy);
        cursor.moveToLast();
        String startDateTime = cursor.getString(cursor.getColumnIndexOrThrow("startdate"));
        cursor.close();
        return startDateTime;
    }

    private String generatePrescriptionHtml() {
        String mPatientName = patient.getFirst_name() + " " + ((!TextUtils.isEmpty(patient.getMiddle_name())) ? patient.getMiddle_name() : "") + " " + ((!TextUtils.isEmpty(patient.getLast_name())) ? patient.getLast_name() : "");

        String prescriptionHtml = new StringBuilder()
                .append(formatPatientDetails(dataModel.getPrescription1(), dataModel.getPrescription2(), mPatientName, getPatientAge(patient.getDate_of_birth()), patient.getGender()))
                .append(formatDiagnostics())
                .append(formatDiagnosis())
                .append(formatMedicationPlan())
                .append(formatPrescribedTests())
                .append(formatAdviceFromDoctor())
                .append(formatFollowUpDate())
                .toString();

        Log.d(TAG, "Generated Prescription HTML: " + prescriptionHtml);
        return prescriptionHtml;
    }

    private String getFormattedComplaints(String complaint) {
        if (TextUtils.isEmpty(complaint)) {
            return "";
        }

        StringBuilder formattedComplaint = new StringBuilder();
        String[] complaints = StringUtils.split(complaint, Node.bullet_arrow);

        for (String comp : complaints) {
            // Check if the trimmed complaint is not empty
            if (!TextUtils.isEmpty(comp.trim())) {
                // Append a formatted complaint with the big bullet and trim unwanted spaces
                formattedComplaint.append(Node.big_bullet)
                        .append(comp.trim().substring(0, comp.indexOf(":")))
                        .append("<br/>");
            }
        }

        return formattedComplaint.toString();
    }

}
