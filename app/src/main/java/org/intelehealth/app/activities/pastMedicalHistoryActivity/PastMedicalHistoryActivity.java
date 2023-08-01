package org.intelehealth.app.activities.pastMedicalHistoryActivity;


import static org.intelehealth.app.database.dao.PatientsDAO.fetch_gender;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.familyHistoryActivity.FamilyHistoryActivity;
import org.intelehealth.app.activities.questionNodeActivity.QuestionsAdapter;
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.LocaleHelper;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.app.utilities.pageindicator.ScrollingPagerIndicator;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class PastMedicalHistoryActivity extends AppCompatActivity implements QuestionsAdapter.FabClickListener {

    String patient = "patient";
    String patientUuid;
    String visitUuid;
    String state;
    String patientName;
    String patientGender;
    String intentTag;
    private float float_ageYear_Month;
    JSONObject jsonObject = new JSONObject();
    ArrayList<String> physicalExams;
    int lastExpandedPosition = -1;
    ImageView ivIntuitiveScroll;

    String mFileName = "patHist.json";
    String image_Prefix = "MH";
    String imageDir = "Medical History";
    String imageName;
    File filePath;

    SQLiteDatabase localdb, db;
    String mgender;

    boolean hasLicense = false;
    String edit_PatHist = "";

//  String mFileName = "DemoHistory.json";

    private static final String TAG = PastMedicalHistoryActivity.class.getSimpleName();

    Node patientHistoryMap;
    // CustomExpandableListAdapter adapter;
    //ExpandableListView historyListView;

    String patientHistory = "";
    String phistory = "";

    boolean flag = false;

    SessionManager sessionManager = null;
    private String encounterVitals;
    private String encounterAdultIntials, EncounterAdultInitial_LatestVisit;
    RecyclerView pastMedical_recyclerView;
    QuestionsAdapter adapter;
    ScrollingPagerIndicator recyclerViewIndicator;
    String new_result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);

        //this language code is no longer required as we are moving towards more optimised as well as generic code for localisation. Check "attachBaseContext".
        String language = sessionManager.getAppLanguage();
        //In case of crash still the org should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

        localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        filePath = new File(AppConstants.IMAGE_PATH);
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        e = sharedPreferences.edit();

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            edit_PatHist = intent.getStringExtra("edit_PatHist");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
            EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");
            state = intent.getStringExtra("state");
            patientName = intent.getStringExtra("name");
            patientGender = intent.getStringExtra("gender");
            intentTag = intent.getStringExtra("tag");
            float_ageYear_Month = intent.getFloatExtra("float_ageYear_Month", 0);

            if (edit_PatHist == null)
                new_result = getValue(getPastMedicalVisitData(), sessionManager.getAppLanguage());
        }

        if (intentTag == null || !intentTag.equalsIgnoreCase("edit")) {
            MaterialAlertDialogBuilder aidAlertDialog = new MaterialAlertDialogBuilder(this);
            aidAlertDialog.setMessage(getString(R.string.past_medical_history_aid_skip_message));
            aidAlertDialog.setCancelable(false);
            aidAlertDialog.setPositiveButton(getString(R.string.aid_skip), (yesAidAlertDialog, which) -> {
                Intent skipIntent = new Intent(PastMedicalHistoryActivity.this, FamilyHistoryActivity.class);
                skipIntent.putExtra("patientUuid", patientUuid);
                skipIntent.putExtra("visitUuid", visitUuid);
                skipIntent.putExtra("encounterUuidVitals", encounterVitals);
                skipIntent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                skipIntent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                skipIntent.putExtra("state", state);
                skipIntent.putExtra("name", patientName);
                skipIntent.putExtra("gender", patientGender);
                skipIntent.putExtra("float_ageYear_Month", float_ageYear_Month);
                skipIntent.putExtra("tag", intentTag);
                //    intent.putStringArrayListExtra("exams", physicalExams);
                startActivity(skipIntent);
            });

            aidAlertDialog.setNegativeButton(getString(R.string.aid_enter_data), (noAidAlertDialog, which) -> {
                boolean past = sessionManager.isReturning();
                if (past && edit_PatHist == null) {
                    MaterialAlertDialogBuilder alertdialog = new MaterialAlertDialogBuilder(this);
                    alertdialog.setTitle(getString(R.string.question_update_details));
//            TextView textViewTitle = new TextView(this);
//            textViewTitle.setText(getString(R.string.question_update_details));
//            textViewTitle.setTextColor(getResources().getColor((R.color.colorPrimary)));
//            textViewTitle.setPadding(30,50,30,0);
//            textViewTitle.setTextSize(16F);
//            textViewTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
//            alertdialog.setCustomTitle(textViewTitle);
                    //AlertDialog.Builder alertdialog = new AlertDialog.Builder(PastMedicalHistoryActivity.this,R.style.AlertDialogStyle);

                    View layoutInflater = LayoutInflater.from(PastMedicalHistoryActivity.this)
                            .inflate(R.layout.past_fam_hist_previous_details, null);
                    alertdialog.setView(layoutInflater);
                    TextView textView = layoutInflater.findViewById(R.id.textview_details);
                    Log.v(TAG, new_result);

                    if (sessionManager.getAppLanguage().equalsIgnoreCase("ar")) {
                        textView.setText(Html.fromHtml(new_result));
                    } else {
                        textView.setText(Html.fromHtml(new_result));
                    }


//            alertdialog.setMessage(getString(R.string.question_update_details));
                    alertdialog.setPositiveButton(getString(R.string.generic_yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // allow to edit
                            flag = true;
                        }
                    });
                    alertdialog.setNegativeButton(getString(R.string.generic_no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            String[] columns = {"value", " conceptuuid"};
                            try {
                                String medHistSelection = "encounteruuid = ? AND conceptuuid = ? AND voided!='1'";
                                String[] medHistArgs = {EncounterAdultInitial_LatestVisit, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB};
                                Cursor medHistCursor = localdb.query("tbl_obs", columns, medHistSelection, medHistArgs, null, null, null);
                                medHistCursor.moveToLast();
                                phistory = medHistCursor.getString(medHistCursor.getColumnIndexOrThrow("value"));
                                medHistCursor.close();
                            } catch (CursorIndexOutOfBoundsException e) {
                                phistory = ""; // if medical history does not exist
                            }

                            // skip
                            flag = false;
                            if (phistory != null && !phistory.isEmpty() && !phistory.equals("null")) {
                                insertDb(phistory);
                            }

                            Intent intent = new Intent(PastMedicalHistoryActivity.this, FamilyHistoryActivity.class);
                            intent.putExtra("patientUuid", patientUuid);
                            intent.putExtra("visitUuid", visitUuid);
                            intent.putExtra("encounterUuidVitals", encounterVitals);
                            intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                            intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                            intent.putExtra("state", state);
                            intent.putExtra("name", patientName);
                            intent.putExtra("gender", patientGender);
                            intent.putExtra("float_ageYear_Month", float_ageYear_Month);
                            intent.putExtra("tag", intentTag);
                            //    intent.putStringArrayListExtra("exams", physicalExams);
                            startActivity(intent);

                        }
                    });
                    AlertDialog alertDialog = alertdialog.create();
                    alertDialog.show();

                    Button pb = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    pb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                    pb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                    Button nb = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                    nb.setTextColor(getResources().getColor((R.color.colorPrimary)));
                    nb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                    alertDialog.setCancelable(false);
                    alertDialog.setCanceledOnTouchOutside(false);
                    IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
                }
                noAidAlertDialog.dismiss();
            });

            aidAlertDialog.show();
        }

        setTitle(getString(R.string.title_activity_patient_history));
        setTitle(getTitle() + ": " + patientName);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_medical_history);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        recyclerViewIndicator = findViewById(R.id.recyclerViewIndicator);
        pastMedical_recyclerView = findViewById(R.id.pastMedical_recyclerView);
        ivIntuitiveScroll = findViewById(R.id.iv_intuitive_scroll);

        if (sessionManager.getAppLanguage().equalsIgnoreCase("ar")) {
            ivIntuitiveScroll.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_swipe, null));
            ivIntuitiveScroll.setRotationY(180);
        } else {
            ivIntuitiveScroll.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_swipe, null));
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        pastMedical_recyclerView.setLayoutManager(linearLayoutManager);
        pastMedical_recyclerView.setItemAnimator(new DefaultItemAnimator());
        PagerSnapHelper helper = new PagerSnapHelper();
        helper.attachToRecyclerView(pastMedical_recyclerView);
        FloatingActionButton fab = findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                triggerConfirmation();
            }

        });


//        if (sessionManager.getLicenseKey() != null && !sessionManager.getLicenseKey().isEmpty())
        if (!sessionManager.getLicenseKey().isEmpty())
            hasLicense = true;

        if (hasLicense) {
            try {
                JSONObject currentFile = null;
                currentFile = new JSONObject(FileUtils.readFileRoot(mFileName, this));
                patientHistoryMap = new Node(currentFile); //Load the patient history mind map
            } catch (JSONException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        } else {
            patientHistoryMap = new Node(FileUtils.encodeJSON(this, mFileName)); //Load the patient history mind map
        }

       /* historyListView = findViewById(R.id.patient_history_expandable_list_view);
        adapter = new CustomExpandableListAdapter(this, patientHistoryMap, this.getClass().getSimpleName()); //The adapter might change depending on the activity.
        historyListView.setAdapter(adapter);*/


        mgender = fetch_gender(patientUuid);

        if (mgender.equalsIgnoreCase("M")) {
            patientHistoryMap.fetchItem("0");
        } else if (mgender.equalsIgnoreCase("F")) {
            patientHistoryMap.fetchItem("1");
        }

        // flaoting value of age is passed to Node for comparison...
        patientHistoryMap.fetchAge(float_ageYear_Month);

        adapter = new QuestionsAdapter(this, patientHistoryMap, pastMedical_recyclerView, this.getClass().getSimpleName(), this, false);
        pastMedical_recyclerView.setAdapter(adapter);

        recyclerViewIndicator.attachToRecyclerView(pastMedical_recyclerView);
        if (sessionManager.getAppLanguage().equalsIgnoreCase("ar"))
            recyclerViewIndicator.setScaleX(-1);


       /* historyListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                onListClick(v, groupPosition, childPosition);
                return false;
            }
        });

        //Same fix as before, close all other groups when something is clicked.
        historyListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {

                if (lastExpandedPosition != -1
                        && groupPosition != lastExpandedPosition) {
                    historyListView.collapseGroup(lastExpandedPosition);
                }
                lastExpandedPosition = groupPosition;
            }
        });*/
    }

    public String getValue(String value, String language) {
        try {
            jsonObject = new JSONObject(value);
            if (TextUtils.isEmpty(language))
                return jsonObject.optString("en");
            else
                return jsonObject.optString(language);
        } catch (Exception e) {
            return value;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }


    private void onListClick(View v, int groupPosition, int childPosition) {
        Node clickedNode = patientHistoryMap.getOption(groupPosition).getOption(childPosition);
        clickedNode.toggleSelected();

        //Nodes and the expandable list act funny, so if anything is clicked, a lot of stuff needs to be updated.
        if (patientHistoryMap.getOption(groupPosition).anySubSelected()) {
            patientHistoryMap.getOption(groupPosition).setSelected(true);
        } else {
            patientHistoryMap.getOption(groupPosition).setUnselected();
        }
        adapter.notifyDataSetChanged();

        if (clickedNode.getInputType() != null) {
            if (!clickedNode.getInputType().equals("camera")) {
                imageName = UUID.randomUUID().toString();
                Node.handleQuestion(clickedNode, PastMedicalHistoryActivity.this, adapter, null, null);
            }
        }

        Log.i(TAG, String.valueOf(clickedNode.isTerminal()));
        if (!clickedNode.isTerminal() && clickedNode.isSelected()) {
            imageName = UUID.randomUUID().toString();

            Node.subLevelQuestion(clickedNode, PastMedicalHistoryActivity.this, adapter, filePath.toString(), imageName);
        }

    }


    // Method to trigger confirmation dialog
    private void triggerConfirmation() {
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);

        if (sessionManager.getAppLanguage().equalsIgnoreCase("ar")) {
            String message = Html.fromHtml(patientHistoryMap.formQuestionAnswer(0)).toString();
            //changes done to handle null pointer exception crash
            if (message != null && !message.isEmpty()) {
                message = message
                        .replace("Question not answered", "سؤال لم يتم الإجابة عليه")
                        .replace("since", "حيث")
                        .replace("From", "حسب")
                        .replace("To", "ل")
                        .replace("Since", "حيث")
                        .replace("Hours", "ساعات")
                        .replace("Days", "أيام")
                        .replace("Weeks", "أسابيع")
                        .replace("Months", "شهور")
                        .replace("Years", "سنوات")
                        .replace("Jan", "كانون الثاني")
                        .replace("Feb", "شهر شباط")
                        .replace("Mar", "شهر اذار")
                        .replace("Apr", "أشهر نيسان")
                        .replace("May", "شهر أيار")
                        .replace("Jun", "شهر حزيران")
                        .replace("Jul", "شهر تموز")
                        .replace("Aug", "شهر أب")
                        .replace("Sep", "شهر أيلول")
                        .replace("Oct", "شهر تشرين الأول")
                        .replace("Nov", "شهر تشرين الثاني")
                        .replace("Dec", "شهر كانون الأول")
                        .replace("Frequency", "تكرار")
                        .replace("Not taking any medication", "عدم تناول أي دواء")
                        .replace("Medication name 1", "اسم الدواء 1")
                        .replace("Medication name 2", "اسم الدواء 2")
                        .replace("Medication name 3", "اسم الدواء 3")
                        .replace("Medication name 4", "اسم الدواء 4")
                        .replace("Medication name 5", "اسم الدواء 5");
            }
            alertDialogBuilder.setMessage(message);
        } else {
            alertDialogBuilder.setMessage(Html.fromHtml(patientHistoryMap.formQuestionAnswer(0)));
        }


        // Handle positive button click
        alertDialogBuilder.setPositiveButton(R.string.generic_yes, (dialog, which) -> {
            dialog.dismiss();
            fabClick();
        });

        // Handle negative button click
        alertDialogBuilder.setNegativeButton(R.string.generic_back, ((dialog, which) -> dialog.dismiss()));
        Dialog alertDialog = alertDialogBuilder.show();
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
    }

    private void fabClick() {
        //If nothing is selected, there is nothing to put into the database.

        List<String> imagePathList = patientHistoryMap.getImagePathList();

        if (imagePathList != null) {
            for (String imagePath : imagePathList) {
                updateImageDatabase(imagePath);
            }
        }


        if (intentTag != null && intentTag.equals("edit")) {
            if (patientHistoryMap.anySubSelected()) {
                patientHistory = patientHistoryMap.generateLanguage();
                String patientHistoryArabic = patientHistoryMap.generateLanguage("ar");

                //changes done to handle null pointer exception crash
                if (patientHistoryArabic != null && !patientHistoryArabic.isEmpty()) {
                    patientHistoryArabic = patientHistoryArabic
                            .replace("Question not answered", "سؤال لم يتم الإجابة عليه")
                            .replace("since", "حيث")
                            .replace("From", "حسب")
                            .replace("To", "ل")
                            .replace("Since", "حيث")
                            .replace("Hours", "ساعات")
                            .replace("Days", "أيام")
                            .replace("Weeks", "أسابيع")
                            .replace("Months", "شهور")
                            .replace("Years", "سنوات")
                            .replace("Jan", "كانون الثاني")
                            .replace("Feb", "شهر شباط")
                            .replace("Mar", "شهر اذار")
                            .replace("Apr", "أشهر نيسان")
                            .replace("May", "شهر أيار")
                            .replace("Jun", "شهر حزيران")
                            .replace("Jul", "شهر تموز")
                            .replace("Aug", "شهر أب")
                            .replace("Sep", "شهر أيلول")
                            .replace("Oct", "شهر تشرين الأول")
                            .replace("Nov", "شهر تشرين الثاني")
                            .replace("Dec", "شهر كانون الأول")
                            .replace("Frequency", "تكرار")
                            .replace("Not taking any medication", "عدم تناول أي دواء")
                            .replace("Medication name 1", "اسم الدواء 1")
                            .replace("Medication name 2", "اسم الدواء 2")
                            .replace("Medication name 3", "اسم الدواء 3")
                            .replace("Medication name 4", "اسم الدواء 4")
                            .replace("Medication name 5", "اسم الدواء 5");
                }

                Map<String, String> patientHistoryData = new HashMap<>();
                patientHistoryData.put("en", patientHistory);
                patientHistoryData.put("ar", patientHistoryArabic);
                Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                patientHistory = gson.toJson(patientHistoryData);
                // update details of patient's visit, when edit button on VisitSummary is pressed
            }

            updateDatabase(patientHistory);
            // displaying all values in another activity
            Intent intent = new Intent(PastMedicalHistoryActivity.this, VisitSummaryActivity.class);
            intent.putExtra("patientUuid", patientUuid);
            intent.putExtra("visitUuid", visitUuid);
            intent.putExtra("encounterUuidVitals", encounterVitals);
            intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
            intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
            intent.putExtra("state", state);
            intent.putExtra("name", patientName);
            intent.putExtra("gender", patientGender);
            intent.putExtra("tag", intentTag);
            intent.putExtra("hasPrescription", "false");
            startActivity(intent);
        } else {
            //  if(patientHistoryMap.anySubSelected()){
            patientHistory = patientHistoryMap.generateLanguage();
            String patientHistoryArabic = patientHistoryMap.generateLanguage("ar");

            //changes done to handle null pointer exception crash
            if (patientHistoryArabic != null && !patientHistoryArabic.isEmpty()) {
                patientHistoryArabic = patientHistoryArabic
                        .replace("Question not answered", "سؤال لم يتم الإجابة عليه")
                        .replace("since", "حيث")
                        .replace("From", "حسب")
                        .replace("To", "ل")
                        .replace("Since", "حيث")
                        .replace("Hours", "ساعات")
                        .replace("Days", "أيام")
                        .replace("Weeks", "أسابيع")
                        .replace("Months", "شهور")
                        .replace("Years", "سنوات")
                        .replace("Jan", "كانون الثاني")
                        .replace("Feb", "شهر شباط")
                        .replace("Mar", "شهر اذار")
                        .replace("Apr", "أشهر نيسان")
                        .replace("May", "شهر أيار")
                        .replace("Jun", "شهر حزيران")
                        .replace("Jul", "شهر تموز")
                        .replace("Aug", "شهر أب")
                        .replace("Sep", "شهر أيلول")
                        .replace("Oct", "شهر تشرين الأول")
                        .replace("Nov", "شهر تشرين الثاني")
                        .replace("Dec", "شهر كانون الأول")
                        .replace("Frequency", "تكرار")
                        .replace("Not taking any medication", "عدم تناول أي دواء")
                        .replace("Medication name 1", "اسم الدواء 1")
                        .replace("Medication name 2", "اسم الدواء 2")
                        .replace("Medication name 3", "اسم الدواء 3")
                        .replace("Medication name 4", "اسم الدواء 4")
                        .replace("Medication name 5", "اسم الدواء 5");
            }
            Map<String, String> patientHistoryData = new HashMap<>();
            patientHistoryData.put("en", patientHistory);
            patientHistoryData.put("ar", patientHistoryArabic);
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            patientHistory = gson.toJson(patientHistoryData);

            if (flag == true) { // only if OK clicked, collect this new info (old patient)
                phistory = phistory + patientHistory; // only PMH updated
                sessionManager.setReturning(true);
                insertDb(phistory);
                // however, we concat it here to patientHistory and
                // pass it along to FH, not inserting into db
            } else  // new patient, directly insert into database
            {
                insertDb(patientHistory);
            }

            Intent intent = new Intent(PastMedicalHistoryActivity.this, FamilyHistoryActivity.class);
            intent.putExtra("patientUuid", patientUuid);
            intent.putExtra("visitUuid", visitUuid);
            intent.putExtra("encounterUuidVitals", encounterVitals);
            intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
            intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
            intent.putExtra("state", state);
            intent.putExtra("name", patientName);
            intent.putExtra("gender", patientGender);
            intent.putExtra("float_ageYear_Month", float_ageYear_Month);
            intent.putExtra("tag", intentTag);
            //       intent.putStringArrayListExtra("exams", physicalExams);
            startActivity(intent);

        }
    }


    /**
     * This method inserts medical history of patient in database.
     *
     * @param value variable of type String
     * @return long
     */
    public boolean insertDb(String value) {
        ObsDAO obsDAO = new ObsDAO();
        ObsDTO obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.RHK_MEDICAL_HISTORY_BLURB);
        obsDTO.setEncounteruuid(encounterAdultIntials);
        obsDTO.setCreator(sessionManager.getCreatorID());
        obsDTO.setValue(StringUtils.getValue(value));
        boolean isInserted = false;
        try {
            isInserted = obsDAO.insertObs(obsDTO);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        return isInserted;
    }


    private void updateImageDatabase(String imagePath) {

        ImagesDAO imagesDAO = new ImagesDAO();

        try {
            imagesDAO.insertObsImageDatabase(imageName, encounterAdultIntials, "");
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }


    /**
     * This method updates medical history of patient in database.
     *
     * @param string variable of type String
     * @return void
     */
    private void updateDatabase(String string) {

        ObsDTO obsDTO = new ObsDTO();
        ObsDAO obsDAO = new ObsDAO();
        try {
            obsDTO.setConceptuuid(UuidDictionary.RHK_MEDICAL_HISTORY_BLURB);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(string);
            obsDTO.setUuid(obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB));
            obsDAO.updateObs(obsDTO);

        } catch (DAOException dao) {
            FirebaseCrashlytics.getInstance().recordException(dao);
        }

        EncounterDAO encounterDAO = new EncounterDAO();
        try {
            encounterDAO.updateEncounterSync("false", encounterAdultIntials);
            encounterDAO.updateEncounterModifiedDate(encounterAdultIntials);
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Node.TAKE_IMAGE_FOR_NODE) {
            if (resultCode == RESULT_OK) {
                String mCurrentPhotoPath = data.getStringExtra("RESULT");
                patientHistoryMap.setImagePath(mCurrentPhotoPath);
                Log.i(TAG, mCurrentPhotoPath);
                patientHistoryMap.displayImage(this, filePath.getAbsolutePath(), imageName);
            }
        }
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void fabClickedAtEnd() {
        // patientHistoryMap = node;
        triggerConfirmation();
    }

    @Override
    public void onChildListClickEvent(int groupPos, int childPos, int physExamPos) {
        onListClick(null, groupPos, childPos);
    }


    public void AnimateView(View v) {

        int fadeInDuration = 500; // Configure time values here
        int timeBetween = 3000;
        int fadeOutDuration = 1000;

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); // add this
        fadeIn.setDuration(fadeInDuration);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); // and this
        fadeOut.setStartOffset(fadeInDuration + timeBetween);
        fadeOut.setDuration(fadeOutDuration);

        AnimationSet animation = new AnimationSet(false); // change to false
        animation.addAnimation(fadeIn);
        animation.addAnimation(fadeOut);
        animation.setRepeatCount(1);
        if (v != null) {
            v.setAnimation(animation);
        }


    }

    public void bottomUpAnimation(View v) {

        if (v != null) {
            v.setVisibility(View.VISIBLE);
            Animation bottomUp = AnimationUtils.loadAnimation(this,
                    R.anim.bottom_up);
            v.startAnimation(bottomUp);
        }

    }

    private String getPastMedicalVisitData() {
        String result = "";
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();
        // String[] columns = {"value"};
        String[] columns = {"value", " conceptuuid"};
        try {
            String medHistSelection = "encounteruuid = ? AND conceptuuid = ? AND voided!='1'";
            String[] medHistArgs = {EncounterAdultInitial_LatestVisit, UuidDictionary.RHK_MEDICAL_HISTORY_BLURB};
            Cursor medHistCursor = localdb.query("tbl_obs", columns, medHistSelection, medHistArgs, null, null, null);
            medHistCursor.moveToLast();
            result = medHistCursor.getString(medHistCursor.getColumnIndexOrThrow("value"));
            medHistCursor.close();
        } catch (CursorIndexOutOfBoundsException e) {
            result = ""; // if medical history does not exist
        }
        db.close();
        return result;
    }
}