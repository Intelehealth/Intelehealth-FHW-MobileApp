package org.intelehealth.app.activities.familyHistoryActivity;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.TextView;


import org.apache.commons.lang3.StringUtils;
import org.intelehealth.app.activities.pastMedicalHistoryActivity.PastMedicalHistoryActivity;
import org.intelehealth.app.utilities.LocaleHelper;
import org.intelehealth.app.utilities.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.questionNodeActivity.QuestionsAdapter;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UuidDictionary;

import org.intelehealth.app.activities.physcialExamActivity.PhysicalExamActivity;
import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.app.utilities.pageindicator.ScrollingPagerIndicator;

public class FamilyHistoryActivity extends AppCompatActivity implements QuestionsAdapter.FabClickListener {
    private static final String TAG = FamilyHistoryActivity.class.getSimpleName();

    String patientUuid;
    String visitUuid;
    String state;
    String patientName;
    String patientGender;
    String intentTag;
    private float float_ageYear_Month;
    JSONObject jsonObject = new JSONObject();
    ArrayList<String> physicalExams;
    String mFileName = "famHist.json";
    int lastExpandedPosition = -1;

    Node familyHistoryMap;
    //CustomExpandableListAdapter adapter;
    // ExpandableListView familyListView;

    ArrayList<String> insertionList = new ArrayList<>();
    String insertion = "", phistory = "", fhistory = "";
    boolean flag = false;
    boolean hasLicense = false;
    SharedPreferences.Editor e;
    SQLiteDatabase localdb, db;
    SessionManager sessionManager;
    String encounterVitals;
    String encounterAdultIntials, EncounterAdultInitial_LatestVisit;
    private String imageName = null;
    private File filePath;
    ScrollingPagerIndicator recyclerViewIndicator;

    RecyclerView family_history_recyclerView;
    QuestionsAdapter adapter;
    String edit_FamHist = "";
    String new_result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);

        //this language code is no longer required as we are moving towards more optimised as well as generic code for localisation. Check "attachBaseContext".
        /*String language = sessionManager.getAppLanguage();
        //In case of crash still the org should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());*/

        localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        filePath = new File(AppConstants.IMAGE_PATH);

        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            state = intent.getStringExtra("state");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            edit_FamHist = intent.getStringExtra("edit_FamHist");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
            EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");
            patientName = intent.getStringExtra("name");
            patientGender = intent.getStringExtra("gender");
            intentTag = intent.getStringExtra("tag");
            float_ageYear_Month = intent.getFloatExtra("float_ageYear_Month", 0);

            if (edit_FamHist == null)
                new_result = getValue(getFamilyHistoryVisitData(), sessionManager.getAppLanguage());
        }

        if (intentTag == null || !intentTag.equalsIgnoreCase("edit")) {
            MaterialAlertDialogBuilder aidAlertDialog = new MaterialAlertDialogBuilder(this);
            aidAlertDialog.setCancelable(false);
            aidAlertDialog.setMessage(getString(R.string.family_history_aid_skip_message));
            aidAlertDialog.setPositiveButton(getString(R.string.aid_skip), (dialog, which) -> {
                Intent skipIntent = new Intent(FamilyHistoryActivity.this, PhysicalExamActivity.class);
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
                noAidAlertDialog.dismiss();

                // If the user clicks on Enter Data option, we will show them the other dialog regarding if they want to update the previous details or not

                boolean past = sessionManager.isReturning();
                if (past && edit_FamHist == null) {
                    MaterialAlertDialogBuilder alertdialog = new MaterialAlertDialogBuilder(this);
                    alertdialog.setTitle(getString(R.string.question_update_details));
                    //AlertDialog.Builder alertdialog = new AlertDialog.Builder(FamilyHistoryActivity.this,R.style.AlertDialogStyle);
//            TextView textViewTitle = new TextView(this);
//            textViewTitle.setText(getString(R.string.question_update_details));
//            textViewTitle.setTextColor(getResources().getColor((R.color.colorPrimary)));
//            textViewTitle.setPadding(30,50,30,0);
//            textViewTitle.setTextSize(16F);
//            textViewTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
//            alertdialog.setCustomTitle(textViewTitle);

                    View layoutInflater = LayoutInflater.from(FamilyHistoryActivity.this)
                            .inflate(R.layout.past_fam_hist_previous_details, null);
                    alertdialog.setView(layoutInflater);
                    TextView textView = layoutInflater.findViewById(R.id.textview_details);
                    textView.setSingleLine(false);
                    Log.v(TAG, new_result);

                    if (sessionManager.getAppLanguage().equalsIgnoreCase("ar")) {
                        textView.setText(Html.fromHtml(getUpdateTranslations(new_result)));
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
                            // skip
                            flag = false;

                            String[] columns = {"value", " conceptuuid"};

                            try {
                                String famHistSelection = "encounteruuid = ? AND conceptuuid = ? AND voided!='1'";
                                String[] famHistArgs = {EncounterAdultInitial_LatestVisit, UuidDictionary.RHK_FAMILY_HISTORY_BLURB};
                                Cursor famHistCursor = localdb.query("tbl_obs", columns, famHistSelection, famHistArgs, null, null, null);
                                famHistCursor.moveToLast();
                                fhistory = famHistCursor.getString(famHistCursor.getColumnIndexOrThrow("value"));
                                famHistCursor.close();
                            } catch (CursorIndexOutOfBoundsException e) {
                                fhistory = ""; // if family history does not exist
                            }

                            if (fhistory != null && !fhistory.isEmpty() && !fhistory.equals("null")) {
                                insertDb(fhistory);
                            }

                            Intent intent = new Intent(FamilyHistoryActivity.this, PhysicalExamActivity.class);
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

            });
            aidAlertDialog.show();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_history);
        setTitle(R.string.title_activity_family_history);
        recyclerViewIndicator = findViewById(R.id.recyclerViewIndicator);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        setTitle(patientName + ": " + getTitle());

        FloatingActionButton fab = findViewById(R.id.fab);
        family_history_recyclerView = findViewById(R.id.family_history_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        family_history_recyclerView.setLayoutManager(linearLayoutManager);
        family_history_recyclerView.setItemAnimator(new DefaultItemAnimator());
        PagerSnapHelper helper = new PagerSnapHelper();
        helper.attachToRecyclerView(family_history_recyclerView);
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
                familyHistoryMap = new Node(currentFile); //Load the family history mind map
            } catch (JSONException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        } else {
            familyHistoryMap = new Node(FileUtils.encodeJSON(this, mFileName)); //Load the family history mind map
        }

        //  familyListView = findViewById(R.id.family_history_expandable_list_view);

        adapter = new QuestionsAdapter(this, familyHistoryMap, family_history_recyclerView, this.getClass().getSimpleName(), this, false);
        family_history_recyclerView.setAdapter(adapter);
        recyclerViewIndicator.attachToRecyclerView(family_history_recyclerView);
        if (sessionManager.getAppLanguage().equalsIgnoreCase("ar"))
            recyclerViewIndicator.setScaleX(-1);

        /*adapter = new CustomExpandableListAdapter(this, familyHistoryMap, this.getClass().getSimpleName());
        familyListView.setAdapter(adapter);*/

        /*familyListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                return false;
            }
        });*/
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }

    private String getFamilyHistoryVisitData() {
        String result = "";
        db = AppConstants.inteleHealthDatabaseHelper.getWritableDatabase();

        String[] columns = {"value", " conceptuuid"};

        try {
            String famHistSelection = "encounteruuid = ? AND conceptuuid = ? AND voided!='1'";
            String[] famHistArgs = {EncounterAdultInitial_LatestVisit, UuidDictionary.RHK_FAMILY_HISTORY_BLURB};
            Cursor famHistCursor = localdb.query("tbl_obs", columns, famHistSelection, famHistArgs, null, null, null);
            famHistCursor.moveToLast();
            result = famHistCursor.getString(famHistCursor.getColumnIndexOrThrow("value"));
            famHistCursor.close();
        } catch (CursorIndexOutOfBoundsException e) {
            result = ""; // if family history does not exist
        }

        db.close();
        return result;
    }

    private void onListClick(View v, int groupPosition, int childPosition) {
        Node clickedNode = familyHistoryMap.getOption(groupPosition).getOption(childPosition);
        Log.i(TAG, "onChildClick: ");
        clickedNode.toggleSelected();
        if (familyHistoryMap.getOption(groupPosition).anySubSelected()) {
            familyHistoryMap.getOption(groupPosition).setSelected(true);
        } else {
            familyHistoryMap.getOption(groupPosition).setUnselected();
        }
        adapter.notifyDataSetChanged();

        if (clickedNode.getInputType() != null) {
            if (!clickedNode.getInputType().equals("camera")) {
                Node.handleQuestion(clickedNode, FamilyHistoryActivity.this, adapter, null, null);
            }
        }
        if (!filePath.exists()) {
            boolean res = filePath.mkdirs();
            Log.i("RES>", "" + filePath + " -> " + res);
        }

        imageName = UUID.randomUUID().toString();

        if (!familyHistoryMap.getOption(groupPosition).getOption(childPosition).isTerminal() &&
                familyHistoryMap.getOption(groupPosition).getOption(childPosition).isSelected()) {
            Node.subLevelQuestion(clickedNode, FamilyHistoryActivity.this, adapter, filePath.toString(), imageName);
        }

    }

    private void triggerConfirmation() {
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);

        // Depending on the app language, our alert dialog text will be translated
        if (sessionManager.getAppLanguage().equalsIgnoreCase("ar")) {
            String message = Html.fromHtml(familyHistoryMap.formQuestionAnswer(0)).toString();
            //changes done to handle null pointer exception crash
            if (message != null && !message.isEmpty()) {
                message = message
                        .replace("Question not answered", "سؤال لم يتم الإجابة عليه")
                        .replace("Patient reports -", "يقر المريض ب-")
                        .replace("Patient denies -", "ينفي المريض ب-")
                        .replace("Hours", "ساعات")
                        .replace("Days", "أيام")
                        .replace("Weeks", "أسابيع")
                        .replace("Months", "شهور")
                        .replace("Years", "سنوات")
                        .replace("times per hour", "مرات في الساعة")
                        .replace("time per day", "الوقت في اليوم")
                        .replace("times per week", "مرات بالأسبوع")
                        .replace("times per month", "مرات في الشهر")
                        .replace("times per year", "مرات في السنة")
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
                        .replace("Dec", "شهر كانون الأول");
            }
            alertDialogBuilder.setMessage(message);
        } else {
            // Else case handles the English language
            alertDialogBuilder.setMessage(Html.fromHtml(familyHistoryMap.formQuestionAnswer(0)));
        }

        // Handle positive button click
        alertDialogBuilder.setPositiveButton(R.string.generic_yes, (dialog, which) -> {
            dialog.dismiss();
            onFabClick();
        });

        // Handle negative button click
        alertDialogBuilder.setNegativeButton(R.string.generic_back, ((dialog, which) -> dialog.dismiss()));
        Dialog alertDialog = alertDialogBuilder.show();
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
    }

    private void onFabClick() {
        if (familyHistoryMap.anySubSelected()) {
            for (Node node : familyHistoryMap.getOptionsList()) {
                if (node.isSelected()) {
                    String familyString = Node.bullet + node.getText() + " : " + node.generateLanguage();
                    String familyStringArabic = Node.bullet + node.getDisplay_arabic() + " : " + node.generateLanguage("ar");
                    Map<String, String> complaintData = new HashMap<>();
                    complaintData.put("en", familyString);
                    complaintData.put("ar", familyStringArabic);
                    Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                    familyString = gson.toJson(complaintData);
                    String toInsert = familyString;
                    toInsert = toInsert.replaceAll(Node.bullet, "");
                    toInsert = toInsert.replaceAll(" - ", ", ");
                    toInsert = toInsert.replaceAll("<br/>", "");
                    if (StringUtils.right(toInsert, 2).equals(", ")) {
                        toInsert = toInsert.substring(0, toInsert.length() - 2);
                    }
                    //this is not requiring for this as we are storing data in json format and parsing is not possible after appending this.
//                    toInsert = toInsert + ".<br/>";
                    insertionList.add(toInsert);
                }
            }
        }

        for (int i = 0; i < insertionList.size(); i++) {
            if (i == 0) {
                insertion = insertionList.get(i);
            } else {
                insertion = insertion + " " + Node.bullet + insertionList.get(i);
            }
        }

        insertion = insertion.replaceAll("null.", "");

        List<String> imagePathList = familyHistoryMap.getImagePathList();

        if (imagePathList != null) {
            for (String imagePath : imagePathList) {
                updateImageDatabase(imagePath);
            }
        }


        if (intentTag != null && intentTag.equals("edit")) {
            updateDatabase(insertion);

            Intent intent = new Intent(FamilyHistoryActivity.this, VisitSummaryActivity.class);
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

            if (flag == true) {
                // only if OK clicked, collect this new info (old patient)
                if (insertion.length() > 0) {
                    fhistory = fhistory + insertion;
                } else {
                    fhistory = fhistory + "";
                }
                insertDb(fhistory);
            } else {
                insertDb(insertion); // new details of family history
            }

            flag = false;
            sessionManager.setReturning(false);
            Intent intent = new Intent(FamilyHistoryActivity.this, PhysicalExamActivity.class); // earlier it was vitals
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
            //   intent.putStringArrayListExtra("exams", physicalExams);
            startActivity(intent);
        }


    }

    public boolean insertDb(String value) {
        boolean isInserted = false;
        //the following changes are being done under ticket SYR-127. Check out ticket description for more details... - Nishita Goyal
        if (!value.isEmpty() && !value.equalsIgnoreCase("") && !value.equalsIgnoreCase(" ")) {
            ObsDAO obsDAO = new ObsDAO();
            ObsDTO obsDTO = new ObsDTO();
            obsDTO.setConceptuuid(UuidDictionary.RHK_FAMILY_HISTORY_BLURB);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(org.intelehealth.app.utilities.StringUtils.getValue(value));
            try {
                isInserted = obsDAO.insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
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

    private void updateDatabase(String string) {

        //the following changes are being done under ticket SYR-127. Check out ticket description for more details... - Nishita Goyal
        if (!string.isEmpty() && !string.equalsIgnoreCase("") && !string.equalsIgnoreCase(" ")) {
            ObsDTO obsDTO = new ObsDTO();
            ObsDAO obsDAO = new ObsDAO();
            try {
                obsDTO.setConceptuuid(UuidDictionary.RHK_FAMILY_HISTORY_BLURB);
                obsDTO.setEncounteruuid(encounterAdultIntials);
                obsDTO.setCreator(sessionManager.getCreatorID());
                obsDTO.setValue(string);
                obsDTO.setUuid(obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.RHK_FAMILY_HISTORY_BLURB));
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
        } else {
            //the following changes are being done under ticket SYR-127. Check out ticket description for more details... - Nishita Goyal
            //update previous obs value's void = 1
        }
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void fabClickedAtEnd() {
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

    private String getUpdateTranslations(String text) {
        if (text != null && !text.isEmpty()) {
            text = text
                    .replace("High BP", "ارتفاع ضغط الدم")
                    .replace("Heart Disease", "مرض قلبي بسن < 50")
                    .replace("Stroke", " سكتة دماغية")
                    .replace("Diabetes", "داء السكري")
                    .replace("Asthma", "الربو")
                    .replace("Tuberculosis", "مرض السل")
                    .replace("Jaundice", "يرقان")
                    .replace("Cancer", "سرطان")
                    .replace("Other", "أمراض أخرى")
                    .replace("Mother", "الأم")
                    .replace("Father", "أب")
                    .replace("Sister", "أخت")
                    .replace("Brother", "أخ")
                    .replace("Do you have a family history of any of the following?", "هل لديك قصة عائلية لأي من الأمراض التالية ؟");
        }
        return text;
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
}



