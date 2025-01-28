package org.intelehealth.app.activities.bill;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.syncModule.SyncUtils;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class BillGenerationActivity extends BaseActivity {

    private static final String TAG = "BillConfirmationActivit";
    //Toolbar toolbar;
    private String patientName, patientVillage, patientOpenID, patientHideVisitID, patientPhoneNum, visitType, patientVisitID, billType;
    private ArrayList<String> selectedTests = new ArrayList<>();
    private TextView patientDetailsTV, paymentStatusTV;
    private String patientDetails;
    private String receiptNum = "XXXXX";
    private String billDateString = "DD MM YYYY";
    private LinearLayout consultCV, followUPCV, glucoseFCV, glucoseRCV, glucoseNFCV, glucosePPNCV, haemoglobinCV, cholesterolCV, bpCV, uricAcidCV, totalAmountCV, padd;
    private CardView finalBillCV;
    private TextView consultChargeTV, followUpChargeTV, glucoseFChargeTV, glucoseRChargeTV, glucoseNFChargeTV, glucosePPNChargeTV, haemoglobinChargeTV, cholesterolChargeTV, bpChargeTV, uricAcidChargeTV, totalAmountTV, payingBillTV;
    private Button btn_disConnect, btn_connect;
    private ProgressBar pb_connect;
    private String paymentStatus = "";
    private int total_amount = 0;
    private RadioButton yes, no;
    private String not_paying_reason = "";
    private EditText not_paying_reasonET;
    private TextInputLayout not_paying_reasonTIL;
    private RadioGroup radioGroup;
    private SessionManager sessionManager;
    private boolean hasLicense = false;
    private JSONObject obj = null;
    private Bitmap bitmap;
    private String finalBillPath = "";
    private SyncUtils syncUtils = new SyncUtils();
    ///ConceptAttributeListDAO conceptAttributeListDAO = new ConceptAttributeListDAO();
    //private ArrayList<PrinterInterface> printerInterfaceArrayList = new ArrayList<>();
    //private RTPrinter rtPrinter = null;
    //private PrinterFactory printerFactory;
    // public static PrinterInterface curPrinterInterface = null;
    private Bitmap mBitmap = null;
    private int bmpPrintWidth = 50;
    private String printStr;
    //private TextSetting textSetting;
    private String mChartsetName = "UTF-8";
    //private ESCFontTypeEnum curESCFontType = null;
    private Object configObj;
    private String appLanguage;
    private Button buttonSelectDevices;
    private Button confirmBillCV, printCV, downloadCV, shareCV;
    private TextView titleReason, tvErrorReason;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_generation);
        initViews();
        //setToolbar();
        //initBluetoothDevice();
    }

    private void initViews() {
        //toolbar = findViewById(R.id.toolbar);
        View toolbar = findViewById(R.id.toolbar_common);
        TextView tvTitle = toolbar.findViewById(R.id.tv_screen_title_common);
        ImageView ivBack = toolbar.findViewById(R.id.iv_back_arrow_common);
        ImageView ivIsInternet = toolbar.findViewById(R.id.imageview_is_internet_common);
        ivIsInternet.setImageDrawable(getResources().getDrawable(R.drawable.home_icon));
        ivIsInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BillGenerationActivity.this, HomeScreenActivity_New.class);
                startActivity(intent);
            }
        });
        ivBack.setVisibility(View.GONE);
        sessionManager = new SessionManager(this);
        appLanguage = sessionManager.getAppLanguage();
        if (!appLanguage.equalsIgnoreCase("")) {
            setLocale(appLanguage);
        }
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

        if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) bmpPrintWidth = 40;
        else if (sessionManager.getAppLanguage().equalsIgnoreCase("en")) bmpPrintWidth = 50;
        //editText
        not_paying_reasonTIL = findViewById(R.id.reasonTIL);
        not_paying_reasonET = findViewById(R.id.reasonET);
        titleReason = findViewById(R.id.tv_title_reason);
        not_paying_reasonET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    if (TextUtils.isEmpty(not_paying_reasonET.getText().toString())) {
                        tvErrorReason.setVisibility(View.VISIBLE);
                        not_paying_reasonET.setBackgroundDrawable(ContextCompat.getDrawable(BillGenerationActivity.this, R.drawable.input_field_error_bg_ui2));
                        return;
                    } else {
                        tvErrorReason.setVisibility(View.GONE);
                        not_paying_reasonET.setBackgroundDrawable(ContextCompat.getDrawable(BillGenerationActivity.this, R.drawable.bg_input_fieldnew));

                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //radioButton
        yes = findViewById(R.id.yes_pay_bill);
        no = findViewById(R.id.no_pay_bill);
        radioGroup = findViewById(R.id.radioGrp);

        //textViews
        patientDetailsTV = findViewById(R.id.patientDetailsTV);
        consultChargeTV = findViewById(R.id.consultation_chargesTV);
        followUpChargeTV = findViewById(R.id.follow_up_chargesTV);
        glucoseFChargeTV = findViewById(R.id.glucose_f_chargesTV);
        glucoseRChargeTV = findViewById(R.id.glucose_ran_chargesTV);
        glucoseNFChargeTV = findViewById(R.id.glucose_nf_chargesTV);
        glucosePPNChargeTV = findViewById(R.id.glucose_ppn_chargesTV);
        haemoglobinChargeTV = findViewById(R.id.haemeo_chargesTV);
        cholesterolChargeTV = findViewById(R.id.cholestrol_chargesTV);
        bpChargeTV = findViewById(R.id.bp_chargesTV);
        uricAcidChargeTV = findViewById(R.id.uric_acid_chargesTV);
        totalAmountTV = findViewById(R.id.total_chargesTV);
        payingBillTV = findViewById(R.id.paying_billTV);
        paymentStatusTV = findViewById(R.id.paymentStatus);
        buttonSelectDevices = findViewById(R.id.tv_device_selected);
        btn_connect = findViewById(R.id.btn_connect);
        btn_disConnect = findViewById(R.id.btn_disConnect);
        pb_connect = findViewById(R.id.pb_connect);
        padd = findViewById(R.id.padd);
        tvErrorReason = findViewById(R.id.tv_reason_error_not_pay);

        //cardViews
        consultCV = findViewById(R.id.consultation_chargesCV);
        followUPCV = findViewById(R.id.follow_up_chargesCV);
        glucoseFCV = findViewById(R.id.glucose_f_chargesCV);
        glucoseRCV = findViewById(R.id.glucose_ran_chargesCV);
        glucoseNFCV = findViewById(R.id.glucose_nf_chargesCV);
        glucosePPNCV = findViewById(R.id.glucose_ppn_chargesCV);
        haemoglobinCV = findViewById(R.id.haemeo_chargesCV);
        cholesterolCV = findViewById(R.id.cholestrol_chargesCV);
        bpCV = findViewById(R.id.bp_chargesCV);
        uricAcidCV = findViewById(R.id.uric_acid_chargesCV);
        totalAmountCV = findViewById(R.id.total_chargesCV);
        confirmBillCV = findViewById(R.id.button_confirm_bill);
        printCV = findViewById(R.id.button_print);
        downloadCV = findViewById(R.id.button_download);
        shareCV = findViewById(R.id.button_share);
        finalBillCV = findViewById(R.id.finalBillCV);
//        Log.v("bill", "card content: \n" +finalBillCV.getDisplay().toString());

        Intent intent = getIntent();
        if (intent != null) {
            patientName = intent.getStringExtra("patientName");
            patientPhoneNum = intent.getStringExtra("patientPhoneNum");
            patientVillage = intent.getStringExtra("patientVillage");
            patientOpenID = intent.getStringExtra("patientOpenMRSID");
            patientHideVisitID = intent.getStringExtra("patientHideVisitID");
            visitType = intent.getStringExtra("visitType");
            patientVisitID = intent.getStringExtra("patientVisitID");
            receiptNum = intent.getStringExtra("receiptNum");
            billDateString = intent.getStringExtra("receiptDate");
            billType = intent.getStringExtra("billType");
            selectedTests = (ArrayList<String>) intent.getSerializableExtra("testsList");
            Log.d(TAG, "kkbillinitViews: patientName : " + patientName);
            Log.d(TAG, "kkbillinitViews: receiptNum : " + receiptNum);
            tvTitle.setText(patientName + " : " + receiptNum);
        }
        Log.d(TAG, "kk05initViews: patientName : " + patientName);
        patientDetails = getString(R.string.receipt_no) + receiptNum + "\n" + getString(R.string.client_name) + patientName + "\n" + getString(R.string.client_id) + patientOpenID + "\n" + getString(R.string.visit_id) + patientHideVisitID + "\n" + getString(R.string.contact_no) + patientPhoneNum + "\n" + getString(R.string.client_village_name) + patientVillage + "\n" + getString(R.string.date_bill) + billDateString;

        patientDetailsTV.setText(patientDetails);
        manageCardView(selectedTests);
        if (!billType.equals("NA")) {
            if (billType.equals("Paid")) {
                paymentStatusTV.setVisibility(View.VISIBLE);
                paymentStatusTV.setText(getString(R.string.paid));
                //  paymentStatusTV.setBackgroundColor(Color.GREEN);
            } else if (billType.contains("Unpaid")) {
                paymentStatusTV.setVisibility(View.VISIBLE);
                paymentStatusTV.setText(getString(R.string.unpaid));
                //  paymentStatusTV.setBackgroundColor(Color.RED);
            }
            payingBillTV.setVisibility(View.GONE);
            radioGroup.setVisibility(View.GONE);
            confirmBillCV.setVisibility(View.GONE);
            printCV.setVisibility(View.VISIBLE);
            downloadCV.setVisibility(View.VISIBLE);
            shareCV.setVisibility(View.VISIBLE);
        }
        setPrices();
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

     /*   tv_device_selected.setOnClickListener(v -> {
            showBluetoothDeviceChooseDialog(); // Here on click, will open the Dialog that will show all the nearby Bluetooth devices...
        });*/

        confirmBillCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!yes.isChecked() && !no.isChecked()) {
                  /*  MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(BillGenerationActivity.this);
                    alertDialogBuilder.setTitle(R.string.error);
                    alertDialogBuilder.setMessage(R.string.select_payment_information);
                    alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                    Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                    IntelehealthApplication.setAlertDialogCustomTheme(BillGenerationActivity.this, alertDialog);*/
                    DialogUtils dialogUtils = new DialogUtils();
                    dialogUtils.showCommonDialog(BillGenerationActivity.this, 0, getResources().getString(R.string.error), getResources().getString(R.string.select_payment_information), true, getResources().getString(R.string.ok), getResources().getString(R.string.cancel), action -> {
                    });
                    return;
                }
                if (no.isChecked() && not_paying_reasonTIL.getVisibility() == View.VISIBLE) {
                    if (not_paying_reasonET.getText().toString().isEmpty()) {
                        tvErrorReason.setVisibility(View.VISIBLE);
                        tvErrorReason.setText(getString(R.string.enter_reason_toast));
                        //not_paying_reasonET.setError(getResources().getString(R.string.error_field_required));
                        // Toast.makeText(BillGenerationActivity.this, getString(R.string.enter_reason_toast), Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        not_paying_reason = not_paying_reasonET.getText().toString();
                        paymentStatus = "Unpaid - " + not_paying_reason;
                    }
                }
                boolean billSuccess = syncBillToServer();
                if (billSuccess) {
                    if (paymentStatus.equals("Paid")) {
                        paymentStatusTV.setVisibility(View.VISIBLE);
                        paymentStatusTV.setText(getString(R.string.paid));
                        //  paymentStatusTV.setBackgroundColor(Color.GREEN);
                    } else if (paymentStatus.contains("Unpaid")) {
                        paymentStatusTV.setVisibility(View.VISIBLE);
                        paymentStatusTV.setText(getString(R.string.unpaid));
                        //  paymentStatusTV.setBackgroundColor(Color.RED);
                    }
                    Toast.makeText(BillGenerationActivity.this, getString(R.string.bill_generated_success), Toast.LENGTH_LONG).show();
                    payingBillTV.setVisibility(View.GONE);
                    radioGroup.setVisibility(View.GONE);
                    confirmBillCV.setVisibility(View.GONE);
                    if (not_paying_reasonTIL.getVisibility() == View.VISIBLE) {
                        not_paying_reasonTIL.setVisibility(View.GONE);
                        titleReason.setVisibility(View.GONE);
                    }
                    printCV.setVisibility(View.VISIBLE);
                    downloadCV.setVisibility(View.VISIBLE);
                    shareCV.setVisibility(View.VISIBLE);
                }
            }
        });

     /*   btn_connect.setOnClickListener(v -> {
            doConnect(); //Here on clicking will connect with the selected Bluetooth device...
        });

        btn_disConnect.setOnClickListener(v -> {
            doDisConnect();
        });*/

        printCV.setOnClickListener(v -> {
            try {
                textPrint();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });

        downloadCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("size", "" + finalBillCV.getWidth() + " " + finalBillCV.getWidth());
                bitmap = LoadBitmap(finalBillCV, finalBillCV.getWidth(), finalBillCV.getHeight());
                createPdf();
            }
        });

        shareCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareFile();
            }
        });
    }

    private void setLocale(String appLanguage) {
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    private void textPrint() throws UnsupportedEncodingException {
//        printStr = pres_textview.getText().toString();
//
//        if (TextUtils.isEmpty(printStr)) {
//            printStr = "Hello Printer";
//        }

        /*switch (IntelehealthApplication.getInstance().getCurrentCmdType()) {
            case BaseEnum.CMD_ESC:
                escPrint();
                break;
            default:
                break;
        }*/
    }

    private boolean syncBillToServer() {
        boolean success = false;
        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);
        Date todayDate = new Date();
        String thisDate = currentDate.format(todayDate);
        String encounter_uuid = UUID.randomUUID().toString();
        success = createEncounter(encounter_uuid, thisDate);
        if (success) {
            success = createObs(encounter_uuid);
        }
        if (success) success = syncUtils.syncForeground("bill");
        return success;
    }

    private boolean createObs(String encounter_uuid) {
        boolean success = false;
        ObsDTO obsDTO = new ObsDTO();
        ObsDAO obsDAO = new ObsDAO();
        obsDTO.setConceptuuid(UuidDictionary.BILL_DATE);
        obsDTO.setEncounteruuid(encounter_uuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(billDateString);
        obsDTO.setUuid(AppConstants.NEW_UUID);

        try {
            success = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        obsDTO.setConceptuuid(UuidDictionary.BILL_VISIT_TYPE);
        obsDTO.setEncounteruuid(encounter_uuid);
        obsDTO.setCreator(sessionManager.getCreatorID());

        /* The change below, of appending the price with the visit type is done under ticket NAS-159.
        This is requested by Priya Joshi as for the visit type she was not getting the prices earlier but only the visit type value.

        Also, the above uuid is same as the uuid of the concept with the name "Billing Visit Type Consultation" in the backend thus the values
        on the server is accessed under this concept name "Billing Visit Type Consultation".
         */

        if (visitType.equalsIgnoreCase("Consultation")) obsDTO.setValue(visitType + " - 15");
        else obsDTO.setValue(visitType + " - 10");
        obsDTO.setUuid(AppConstants.NEW_UUID);

        try {
            success = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        obsDTO.setConceptuuid(UuidDictionary.BILL_PAYMENT_STATUS);
        obsDTO.setEncounteruuid(encounter_uuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(paymentStatus);
        obsDTO.setUuid(AppConstants.NEW_UUID);

        try {
            success = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        obsDTO.setConceptuuid(UuidDictionary.BILL_NUM);
        obsDTO.setEncounteruuid(encounter_uuid);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(receiptNum);
        obsDTO.setUuid(AppConstants.NEW_UUID);

        try {
            success = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }


        if (selectedTests.contains(getString(R.string.blood_glucose_non_fasting))) {
            obsDTO.setConceptuuid(UuidDictionary.BILL_PRICE_BLOOD_GLUCOSE_ID);
            obsDTO.setEncounteruuid(encounter_uuid);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue("15");
            obsDTO.setUuid(AppConstants.NEW_UUID);

            try {
                success = obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
        if (selectedTests.contains(getString(R.string.blood_glucose_fasting))) {
            obsDTO.setConceptuuid(UuidDictionary.BILL_PRICE_BLOOD_GLUCOSE_FASTING_ID);
            obsDTO.setEncounteruuid(encounter_uuid);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue("15");
            obsDTO.setUuid(AppConstants.NEW_UUID);

            try {
                success = obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
        if (selectedTests.contains(getString(R.string.blood_glucose_post_prandial))) {
            obsDTO.setConceptuuid(UuidDictionary.BILL_PRICE_BLOOD_GLUCOSE_POST_PRANDIAL_ID);
            obsDTO.setEncounteruuid(encounter_uuid);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue("15");
            obsDTO.setUuid(AppConstants.NEW_UUID);

            try {
                success = obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
        if (selectedTests.contains(getString(R.string.blood_glucose_random))) {
            obsDTO.setConceptuuid(UuidDictionary.BILL_PRICE_BLOOD_GLUCOSE_RANDOM_ID);
            obsDTO.setEncounteruuid(encounter_uuid);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue("15");
            obsDTO.setUuid(AppConstants.NEW_UUID);

            try {
                success = obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
        if (selectedTests.contains(getString(R.string.uric_acid))) {
            obsDTO.setConceptuuid(UuidDictionary.BILL_PRICE_URIC_ACID_ID);
            obsDTO.setEncounteruuid(encounter_uuid);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue("30");
            obsDTO.setUuid(AppConstants.NEW_UUID);

            try {
                success = obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
        if (selectedTests.contains(getString(R.string.total_cholestrol))) {
            obsDTO.setConceptuuid(UuidDictionary.BILL_PRICE_TOTAL_CHOLESTEROL_ID);
            obsDTO.setEncounteruuid(encounter_uuid);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue("80");
            obsDTO.setUuid(AppConstants.NEW_UUID);

            try {
                success = obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
        if (selectedTests.contains(getString(R.string.haemoglobin))) {
            obsDTO.setConceptuuid(UuidDictionary.BILL_PRICE_HEMOGLOBIN_ID);
            obsDTO.setEncounteruuid(encounter_uuid);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue("20");
            obsDTO.setUuid(AppConstants.NEW_UUID);

            try {
                success = obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
        if (selectedTests.contains(getString(R.string.visit_summary_bp))) {
            obsDTO.setConceptuuid(UuidDictionary.BILL_PRICE_BP_ID);
            obsDTO.setEncounteruuid(encounter_uuid);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue("5");
            obsDTO.setUuid(AppConstants.NEW_UUID);

            try {
                success = obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }

        return success;
    }

    private boolean createEncounter(String encounter_uuid, String thisDate) {
        boolean success = false;
        EncounterDTO encounterDTO = new EncounterDTO();
        EncounterDAO encounterDAO = new EncounterDAO();
        encounterDTO = new EncounterDTO();
        encounterDTO.setUuid(encounter_uuid);
        //encounterDTO.setEncounterTypeUuid(encounterDAO.getEncounterTypeUuid("Visit Billing Details"));
        encounterDTO.setEncounterTypeUuid("7030c68e-eecc-4656-bb0a-e465aea6195f");
        encounterDTO.setEncounterTime(thisDate);
        encounterDTO.setVisituuid(patientVisitID);
        encounterDTO.setSyncd(false);
        encounterDTO.setProvideruuid(sessionManager.getProviderID());
        Log.d("DTO", "DTO:detail " + encounterDTO.getProvideruuid());
        encounterDTO.setVoided(0);
        encounterDTO.setPrivacynotice_value("Accept");//privacy value added.

        try {
            success = encounterDAO.createEncountersToDB(encounterDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        return success;
    }

    private void shareFile() {
        //  String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/";
        File path = this.getExternalFilesDir("Bill");
        String fName = patientName + "_" + patientOpenID + "_" + billDateString + ".pdf";
        String finalPath = path + fName;
        finalBillPath = finalPath;
        if (finalBillPath.equals("")) {
            Toast.makeText(BillGenerationActivity.this, getString(R.string.download_bill), Toast.LENGTH_LONG).show();
            return;
        }

        //  File file = new File(finalBillPath);
        File file = new File(path, fName);
        if (!file.exists()) {
            Toast.makeText(BillGenerationActivity.this, getString(R.string.download_bill), Toast.LENGTH_LONG).show();
            return;
        }
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, "Share the file...."));
    }


    private void setPrices() {
        if (consultCV.getVisibility() == View.VISIBLE) {
            //String price = conceptAttributeListDAO.getConceptPrice("Billing Visit Type Consultation");
            //price = getPrice(price, price.indexOf('.'));
            String price = "15";
            consultChargeTV.setText("₹" + price + "/-");
            total_amount += Integer.parseInt(price);
        }
        if (followUPCV.getVisibility() == View.VISIBLE) {
            //String price = conceptAttributeListDAO.getConceptPrice("Billing Visit Type Followup");
            //price = getPrice(price, price.indexOf('.'));
            String price = "10";
            followUpChargeTV.setText("₹" + price + "/-");
            total_amount += Integer.parseInt(price);
        }
        if (glucoseRCV.getVisibility() == View.VISIBLE) {
            //String price = conceptAttributeListDAO.getConceptPrice("Blood Sugar (Random)");
            // price = getPrice(price, price.indexOf('.'));
            String price = "15";
            glucoseRChargeTV.setText("₹" + price + "/-");
            total_amount += Integer.parseInt(price);
        }
        if (glucoseFCV.getVisibility() == View.VISIBLE) {
            //String price = conceptAttributeListDAO.getConceptPrice("Blood Glucose (Fasting)");
            //price = getPrice(price, price.indexOf('.'));
            String price = "15";
            glucoseFChargeTV.setText("₹" + price + "/-");
            total_amount += Integer.parseInt(price);
        }
        if (glucosePPNCV.getVisibility() == View.VISIBLE) {
            //String price = conceptAttributeListDAO.getConceptPrice("Blood Sugar ( Post-prandial)");
            //price = getPrice(price, price.indexOf('.'));
            String price = "15";
            glucosePPNChargeTV.setText("₹" + price + "/-");
            total_amount += Integer.parseInt(price);
        }
       /* if (glucoseNFCV.getVisibility() == View.VISIBLE) {
            String price = conceptAttributeListDAO.getConceptPrice("Blood Sugar (Non-Fasting)");
            price = getPrice(price, price.indexOf('.'));
            glucoseNFChargeTV.setText("₹" + price + "/-");
            total_amount += Integer.parseInt(price);
        }*/
        if (uricAcidCV.getVisibility() == View.VISIBLE) {
            //String price = conceptAttributeListDAO.getConceptPrice("SERUM URIC ACID");
            //price = getPrice(price, price.indexOf('.'));
            String price = "30";
            uricAcidChargeTV.setText("₹" + price + "/-");
            total_amount += Integer.parseInt(price);
        }
        if (haemoglobinCV.getVisibility() == View.VISIBLE) {
            //String price = conceptAttributeListDAO.getConceptPrice("Haemoglobin Test");
            //price = getPrice(price, price.indexOf('.'));
            String price = "20";
            haemoglobinChargeTV.setText("₹" + price + "/-");
            total_amount += Integer.parseInt(price);
        }
        if (cholesterolCV.getVisibility() == View.VISIBLE) {
            //String price = conceptAttributeListDAO.getConceptPrice("TOTAL CHOLESTEROL");
            // price = getPrice(price, price.indexOf('.'));
            String price = "80";
            cholesterolChargeTV.setText("₹" + price + "/-");
            total_amount += Integer.parseInt(price);
        }
        if (bpCV.getVisibility() == View.VISIBLE) {
            //String price = conceptAttributeListDAO.getConceptPrice("BP Test");
            String price = "5";
            //price = getPrice(price, price.indexOf('.'));
            bpChargeTV.setText("₹" + price + "/-");
            total_amount += Integer.parseInt(price);
        }

        totalAmountTV.setText("₹" + String.valueOf(total_amount) + "/-");
    }

    private String getPrice(String price, int indexOf) {
        return price.substring(0, indexOf);
    }

    private void manageCardView(ArrayList<String> selectedTests) {
        if (visitType.equalsIgnoreCase("Consultation")) {
            consultCV.setVisibility(View.VISIBLE);
            followUPCV.setVisibility(View.GONE);
        }
        if (visitType.equalsIgnoreCase("Follow-Up")) {
            followUPCV.setVisibility(View.VISIBLE);
            consultCV.setVisibility(View.GONE);
        }
        if (selectedTests.contains(getString(R.string.blood_glucose_non_fasting)))
            glucoseNFCV.setVisibility(View.VISIBLE);
        if (selectedTests.contains(getString(R.string.blood_glucose_fasting)))
            glucoseFCV.setVisibility(View.VISIBLE);
        if (selectedTests.contains(getString(R.string.blood_glucose_post_prandial)))
            glucosePPNCV.setVisibility(View.VISIBLE);
        if (selectedTests.contains(getString(R.string.blood_glucose_random)))
            glucoseRCV.setVisibility(View.VISIBLE);
        if (selectedTests.contains(getString(R.string.uric_acid)))
            uricAcidCV.setVisibility(View.VISIBLE);
        if (selectedTests.contains(getString(R.string.total_cholestrol)))
            cholesterolCV.setVisibility(View.VISIBLE);
        if (selectedTests.contains(getString(R.string.haemoglobin)))
            haemoglobinCV.setVisibility(View.VISIBLE);
        if (selectedTests.contains(getString(R.string.visit_summary_bp)))
            bpCV.setVisibility(View.VISIBLE);

    }

   /* private void setToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        setTitle(patientName + " : " + receiptNum);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }*/

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        int id = view.getId();
        if (id == R.id.yes_pay_bill) {
            if (checked) {
                paymentStatus = "Paid";
                not_paying_reasonTIL.setVisibility(View.GONE);
                titleReason.setVisibility(View.GONE);
                not_paying_reasonET.setText("");
            }
        } else if (id == R.id.no_pay_bill) {
            if (checked) {
                paymentStatus = "Unpaid";
                not_paying_reasonTIL.setVisibility(View.VISIBLE);
                titleReason.setVisibility(View.VISIBLE);

            }
        }
    }

    @Override
    public void onBackPressed() {
        //do nothing
        //Use the buttons on the screen to navigate
    }

    private Bitmap LoadBitmap(View v, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);
        return bitmap;
    }

    private void createPdf() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        //  Display display = wm.getDefaultDisplay();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        float height = displaymetrics.heightPixels;
        float width = displaymetrics.widthPixels;

        int convertHeight = (int) height, convertWidth = (int) width;

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(convertWidth, convertHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();

        Paint paint = new Paint();
        canvas.drawPaint(paint);

        bitmap = Bitmap.createScaledBitmap(bitmap, convertWidth, convertHeight, true);

        paint.setColor(Color.BLUE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        document.finishPage(page);

        // write the document content
        //  String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/";
        File path = this.getExternalFilesDir("Bill");
//        File filePath = new File(path);
//        if (!filePath.exists())
//            filePath.mkdirs();

        String fName = patientName + "_" + patientOpenID + "_" + billDateString + ".pdf";
        File filePath = new File(path, fName);
        if (!filePath.exists()) filePath.mkdirs();

        String finalPath = path + fName;
        finalBillPath = finalPath;
//        File file = new File(filePath, fName);
        File file = new File(path, fName);
        if (file.exists()) file.delete();

        try {
            file.createNewFile();
            document.writeTo(new FileOutputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show();
            return;
        }

        document.close();
        Toast.makeText(this, "successfully pdf created", Toast.LENGTH_SHORT).show();

//        openPdf(finalPath);

    }

    private void openPdf(String path) {
        File file = new File(path);
        if (file.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "No Application for pdf view", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        if (item.getItemId() == R.id.detail_home) {
            Intent intent = new Intent(BillGenerationActivity.this, HomeScreenActivity_New.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //This will open a Dialog that will show all the Bluetooth devices...
/*
    private void showBluetoothDeviceChooseDialog() {
        BluetoothDeviceChooseDialog bluetoothDeviceChooseDialog = new BluetoothDeviceChooseDialog();
        bluetoothDeviceChooseDialog.setOnDeviceItemClickListener(
                new BluetoothDeviceChooseDialog.onDeviceItemClickListener() {
                    @Override
                    public void onDeviceItemClick(BluetoothDevice device) {
                        if (TextUtils.isEmpty(device.getName())) {
                            tv_device_selected.setText(device.getAddress());
                        } else {
                            tv_device_selected.setText(device.getName() + " [" + device.getAddress() + "]");
                        }
                        configObj = new BluetoothEdrConfigBean(device);
                        tv_device_selected.setTag(BaseEnum.HAS_DEVICE);
                        isConfigPrintEnable(configObj);

                        btn_disConnect.setBackgroundResource(R.drawable.bg_button_disable);
                        btn_connect.setBackgroundResource(R.drawable.bg_visit_details);
                    }
                });

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth.
            Toast.makeText(BillGenerationActivity.this,
                    getResources().getString(R.string.bluetooth_notsupported_device), Toast.LENGTH_SHORT).show();
            return;
        } else if (!mBluetoothAdapter.isEnabled()) {
            // Bluetooth is Turned OFF.
            Toast.makeText(BillGenerationActivity.this,
                    getResources().getString(R.string.turn_on_bluetooth), Toast.LENGTH_SHORT).show();
            return;
        } else {
            // Bluetooth is Turned ON.
            bluetoothDeviceChooseDialog.show(BillGenerationActivity.this.getSupportFragmentManager(), null);
        }
    }
*/

/*
    private void initBluetoothDevice() {
        IntelehealthApplication.getInstance().setCurrentCmdType(BaseEnum.CMD_ESC);
        // printerFactory = new UniversalPrinterFactory();
        printerFactory = new ThermalPrinterFactory();
        rtPrinter = printerFactory.create();
        PrinterObserverManager.getInstance().add(this);

        if (curPrinterInterface != null) {
            // to maintain the bluetooth pairing throughout the app.
            rtPrinter.setPrinterInterface(curPrinterInterface);
            tv_device_selected.setText(curPrinterInterface.getConfigObject().toString());
            tv_device_selected.setTag(BaseEnum.HAS_DEVICE);
            printerInterfaceArrayList.add(curPrinterInterface);
            rtPrinter.setPrinterInterface(curPrinterInterface);
            setPrintEnable(true);
            btn_disConnect.setBackgroundResource(R.drawable.bg_end_visit);
            btn_connect.setBackgroundResource(R.drawable.bg_button_disable);
        }

        IntelehealthApplication.getInstance().setRtPrinter(rtPrinter);
        rtPrinter = IntelehealthApplication.getInstance().getRtPrinter();
        textSetting = new TextSetting();

//        finalBillCV.setTextSize(60f);
//        finalBillCV.setIncludeFontPadding(false);
//        finalBillCV.setTextColor(getResources().getColor(R.color.ink_pen));
        finalBillCV.setBackgroundColor(getResources().getColor(R.color.white));
        // patientDetailsTV.setText(drSign_Text);
        finalBillCV.setDrawingCacheEnabled(true);
        finalBillCV.buildDrawingCache();
        finalBillCV.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        finalBillCV.layout(0, 0, finalBillCV.getMeasuredWidth(), finalBillCV.getMeasuredHeight());
        //  padd.setPadding(0,0,0,200);
        mBitmap = finalBillCV.getDrawingCache(); // converting cardview to bitmap
    }
*/

  /*  @Override
    public void printerObserverCallback(final PrinterInterface printerInterface, final int state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pb_connect.setVisibility(View.GONE);
                // disconnect and connect button color change.
                if (state == CommonEnum.CONNECT_STATE_SUCCESS) {
                    Toast.makeText(BillGenerationActivity.this, printerInterface.getConfigObject().toString()
                            + getString(R.string._main_connected), Toast.LENGTH_SHORT).show();
                } else if (state == CommonEnum.CONNECT_STATE_INTERRUPTED) {
                    if (printerInterface != null && printerInterface.getConfigObject() != null) {
                        Toast.makeText(BillGenerationActivity.this, printerInterface.getConfigObject().toString()
                                        + getString(R.string._main_disconnect),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(BillGenerationActivity.this, getString(R.string._main_disconnect),
                                Toast.LENGTH_SHORT).show();
                    }
                }


                switch (state) {
                    case CommonEnum.CONNECT_STATE_SUCCESS:
                        TimeRecordUtils.record("RT连接end：", System.currentTimeMillis());
//                        Toast.makeText(TextPrintESCActivity.this, printerInterface.getConfigObject().toString()
//                                + getString(R.string._main_connected), Toast.LENGTH_SHORT).show();
//                        tv_device_selected.setText(printerInterface.getConfigObject().toString());
                        tv_device_selected.setTag(BaseEnum.HAS_DEVICE);
                        curPrinterInterface = printerInterface; // set current Printer Interface
                        printerInterfaceArrayList.add(printerInterface);
                        rtPrinter.setPrinterInterface(printerInterface);
                        setPrintEnable(true);
                        btn_disConnect.setBackgroundResource(R.drawable.bg_end_visit);
                        btn_connect.setBackgroundResource(R.drawable.bg_button_disable);
                        break;
                    case CommonEnum.CONNECT_STATE_INTERRUPTED:
                        if (printerInterface != null && printerInterface.getConfigObject() != null) {
//                            Toast.makeText(TextPrintESCActivity.this, printerInterface.getConfigObject().toString()
//                                            + getString(R.string._main_disconnect),
//                                    Toast.LENGTH_SHORT).show();
                        } else {
//                            Toast.makeText(TextPrintESCActivity.this, getString(R.string._main_disconnect),
//                                    Toast.LENGTH_SHORT).show();
                        }
                        TimeRecordUtils.record("Time：", System.currentTimeMillis());
                        tv_device_selected.setText(R.string.please_connect);
                        tv_device_selected.setTag(BaseEnum.NO_DEVICE);
                        curPrinterInterface = null;
                        printerInterfaceArrayList.remove(printerInterface);
                        setPrintEnable(false);
                        btn_disConnect.setBackgroundResource(R.drawable.bg_button_disable);
                        btn_connect.setBackgroundResource(R.drawable.bg_button_disable);

                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    public void printerReadMsgCallback(PrinterInterface printerInterface, byte[] bytes) {

    }*/

    private void setPrintEnable(boolean isEnable) {
        // btn_txtprint.setEnabled(isEnable);
        btn_connect.setEnabled(!isEnable);
        btn_disConnect.setEnabled(isEnable);
    }

   /* private void isConfigPrintEnable(Object configObj) {
        if (isInConnectList(configObj)) {
            setPrintEnable(true);
        } else {
            setPrintEnable(false);
        }
    }*/

  /*  private boolean isInConnectList(Object configObj) {
        boolean isInList = false;
        for (int i = 0; i < printerInterfaceArrayList.size(); i++) {
            PrinterInterface printerInterface = printerInterfaceArrayList.get(i);
            if (configObj.toString().equals(printerInterface.getConfigObject().toString())) {
                if (printerInterface.getConnectState() == ConnectStateEnum.Connected) {
                    isInList = true;
                    break;
                }
            }
        }
        return isInList;
    }

    private void doConnect() {

        if (Integer.parseInt(tv_device_selected.getTag().toString()) == BaseEnum.NO_DEVICE) { // No device is selected.
            showAlertDialog(getString(R.string.main_pls_choose_device));
            return;
        }

        pb_connect.setVisibility(View.VISIBLE);
        TimeRecordUtils.record("Start：", System.currentTimeMillis());
        BluetoothEdrConfigBean bluetoothEdrConfigBean = (BluetoothEdrConfigBean) configObj;
        connectBluetooth(bluetoothEdrConfigBean);
    }

    private void doDisConnect() {
        if (Integer.parseInt(tv_device_selected.getTag().toString()) == BaseEnum.NO_DEVICE) {
            return;
        }
        if (rtPrinter != null && rtPrinter.getPrinterInterface() != null) {
            rtPrinter.disConnect();
        }

        // disconnect and connect button color change.
        btn_disConnect.setBackgroundResource(R.drawable.bg_button_disable);
        btn_connect.setBackgroundResource(R.drawable.bg_button_disable);

        tv_device_selected.setText(getString(R.string.please_connect));
        tv_device_selected.setTag(BaseEnum.NO_DEVICE);
        setPrintEnable(false);
    }*/

    public void showAlertDialog(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(BillGenerationActivity.this);
                dialog.setTitle("Please connect device");
                dialog.setMessage(msg);
                dialog.setNegativeButton(R.string.cancel, null);
                dialog.show();
            }
        });
    }

/*
    private void connectBluetooth(BluetoothEdrConfigBean bluetoothEdrConfigBean) {
        PIFactory piFactory = new BluetoothFactory();
        PrinterInterface printerInterface = piFactory.create();
        printerInterface.setConfigObject(bluetoothEdrConfigBean);

        rtPrinter.setPrinterInterface(printerInterface);
        try {
            rtPrinter.connect(bluetoothEdrConfigBean);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //do nothing...
        }
    }
*/

/*
    private void escPrint() throws UnsupportedEncodingException {
        printCV.setEnabled(false); // to avoid multiple prints at same time if user clicks by mistake
        printCV.setClickable(false);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (rtPrinter != null) {
                    CmdFactory escFac = new EscFactory();
                    Cmd escCmd = escFac.create();

                    escCmd.append(escCmd.getHeaderCmd());// Initial //btnCmds = 2......
                    escCmd.setChartsetName(mChartsetName);
                    CommonSetting commonSetting = new CommonSetting();
                    commonSetting.setAlign(ALIGN_MIDDLE);

                    BitmapSetting bitmapSetting = new BitmapSetting();
                    bitmapSetting.setBmpPrintMode(BmpPrintMode.MODE_SINGLE_COLOR);
                    bitmapSetting.setBimtapLimitWidth(bmpPrintWidth * 8);

                    Position txtposition = new Position(0, 0);
                    textSetting.setTxtPrintPosition(txtposition);
                    escCmd.append(escCmd.getCommonSettingCmd(commonSetting));

                    // textSetting.setAlign(CommonEnum.ALIGN_RIGHT);
                    // commonSetting.setEscLineSpacing(getInputLineSpacing());

//                    try {
//                        escCmd.append(escCmd.getTextCmd(textSetting, prescData));
//                    } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                    }
//                    escCmd.append(escCmd.getLFCRCmd());
                    escCmd.append(escCmd.getLFCRCmd());

                    // here printing the image...
                    try {
                        if (mBitmap != null)
                            escCmd.append(escCmd.getBitmapCmd(bitmapSetting, mBitmap));
                    } catch (SdkException e) {
                        e.printStackTrace();
                    }

                    escCmd.append(escCmd.getLFCRCmd());
                    escCmd.append(escCmd.getLFCRCmd());

                    //here it prints 2nd time taking the position of the cursor where the priting ended above.
//                    txtposition.x = 20;
//                    textSetting.setTxtPrintPosition(txtposition);
//                    try {
//                        escCmd.append(escCmd.getTextCmd(textSetting, doctorDetails));
//                    } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                    }
//                    escCmd.append(escCmd.getLFCRCmd());
//                    escCmd.append(escCmd.getLFCRCmd());
//                    escCmd.append(escCmd.getLFCRCmd());
//                    escCmd.append(escCmd.getLFCRCmd());
//                    escCmd.append(escCmd.getHeaderCmd());
//                    escCmd.append(escCmd.getLFCRCmd());

                    Log.i("bill", FuncUtils.ByteArrToHex(escCmd.getAppendCmds()));
                    if (rtPrinter.getPrinterInterface() != null) {
                        // If without selecting Bluetooth user click Print button crash happens so added this condition.
                        rtPrinter.writeMsgAsync(escCmd.getAppendCmds());
                        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(BillGenerationActivity.this);
                        alertdialogBuilder.setMessage(R.string.printing);
                        alertdialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                printCV.setEnabled(true);
                                printCV.setClickable(true);
                                finish();
                            }
                        });

                        androidx.appcompat.app.AlertDialog alertDialog = alertdialogBuilder.create();
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.setCancelable(false);
                        alertDialog.show();

                        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
                        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                        IntelehealthApplication.setAlertDialogCustomTheme(BillGenerationActivity.this, alertDialog);
                    } else {
                        printCV.setEnabled(true);
                        printCV.setClickable(true);
                        Toast.makeText(BillGenerationActivity.this, getResources().getString
                                (R.string.tip_have_no_paired_device), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
*/


}