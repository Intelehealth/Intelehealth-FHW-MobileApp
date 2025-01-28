package org.intelehealth.app.activities.physcialExamActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.intelehealth.app.utilities.CustomLog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;


import org.intelehealth.app.activities.visitSummaryActivity.VisitSummaryActivity_New;
import org.intelehealth.app.shared.BaseActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.questionNodeActivity.QuestionsAdapter;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.ObsDAO;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.knowledgeEngine.PhysicalExam;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.utilities.FileUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UuidDictionary;

import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.app.utilities.pageindicator.ScrollingPagerIndicator;

import static org.intelehealth.app.database.dao.PatientsDAO.fetch_gender;

public class PhysicalExamActivity extends BaseActivity implements QuestionsAdapter.FabClickListener {
    final static String TAG = PhysicalExamActivity.class.getSimpleName();
    // private SectionsPagerAdapter mSectionsPagerAdapter;

    // private ViewPager mViewPager;

    static String patientUuid;
    static String visitUuid;
    String state;
    String patientName;
    String patientGender;
    String intentTag;
    private float float_ageYear_Month;

    ArrayList<String> selectedExamsList;

    SQLiteDatabase localdb;


    static String imageName;
    static String baseDir;
    static File filePath;


    String mFileName = "physExam.json";


    PhysicalExam physicalExamMap;

    String physicalString;
    Boolean complaintConfirmed = false;
    String encounterVitals;
    String encounterAdultIntials, EncounterAdultInitial_LatestVisit;
    SessionManager sessionManager;
    RecyclerView physExam_recyclerView;
    QuestionsAdapter adapter;
    String mgender;
    ScrollingPagerIndicator recyclerViewIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sessionManager = new SessionManager(this);
        String language = sessionManager.getAppLanguage();
        //In case of crash still the org should hold the current lang fix.
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        //  sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

        baseDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();

        localdb = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        sessionManager = new SessionManager(this);
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
        // AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
        alertDialogBuilder.setTitle(getResources().getString(R.string.wash_hands));
        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.hand_wash, null);
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.show();
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
        //alertDialog.show();

        Button pb = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        pb.setTextColor(ContextCompat.getColor(this, (R.color.colorPrimary)));
        //pb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        selectedExamsList = new ArrayList<>();
        Intent intent = this.getIntent(); // The intent was passed to the activity
        if (intent != null) {
            patientUuid = intent.getStringExtra("patientUuid");
            visitUuid = intent.getStringExtra("visitUuid");
            encounterVitals = intent.getStringExtra("encounterUuidVitals");
            encounterAdultIntials = intent.getStringExtra("encounterUuidAdultIntial");
            EncounterAdultInitial_LatestVisit = intent.getStringExtra("EncounterAdultInitial_LatestVisit");
            state = intent.getStringExtra("state");
            patientName = intent.getStringExtra("name");
            patientGender = intent.getStringExtra("gender");
            float_ageYear_Month = intent.getFloatExtra("float_ageYear_Month", 0);
            intentTag = intent.getStringExtra("tag");
            Set<String> selectedExams = sessionManager.getVisitSummary(patientUuid);
            selectedExamsList.clear();
            if (selectedExams != null)
                selectedExamsList.addAll(selectedExams);
            filePath = new File(AppConstants.IMAGE_PATH);
        }

        if ((selectedExamsList == null) || selectedExamsList.isEmpty()) {
            CustomLog.d(TAG, "No additional exams were triggered");
            physicalExamMap = new PhysicalExam(FileUtils.encodeJSON(this, mFileName), selectedExamsList);
        } else {
            Set<String> selectedExamsWithoutDuplicates = new LinkedHashSet<>(selectedExamsList);
            CustomLog.d(TAG, selectedExamsList.toString());
            selectedExamsList.clear();
            selectedExamsList.addAll(selectedExamsWithoutDuplicates);
            CustomLog.d(TAG, selectedExamsList.toString());
            for (String string : selectedExamsList)
                CustomLog.d(TAG, string);

            boolean hasLicense = false;
//            if (sessionManager.getLicenseKey() != null && !sessionManager.getLicenseKey().isEmpty())
            if (!sessionManager.getLicenseKey().isEmpty())
                hasLicense = true;

            if (hasLicense) {
                try {
                    JSONObject currentFile = null;
                    currentFile = new JSONObject(FileUtils.readFileRoot(mFileName, this));
                    physicalExamMap = new PhysicalExam(currentFile, selectedExamsList);
                } catch (JSONException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            } else {
                physicalExamMap = new PhysicalExam(FileUtils.encodeJSON(this, mFileName), selectedExamsList);
            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_physical_exam);
        setTitle(getString(R.string.title_activity_physical_exam));
        Toolbar toolbar = findViewById(R.id.toolbar);
        recyclerViewIndicator = findViewById(R.id.recyclerViewIndicator);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
        toolbar.setTitleTextColor(Color.WHITE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        setTitle(patientName + ": " + getTitle());
        physExam_recyclerView = findViewById(R.id.physExam_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        physExam_recyclerView.setLayoutManager(linearLayoutManager);
        physExam_recyclerView.setItemAnimator(new DefaultItemAnimator());
        PagerSnapHelper helper = new PagerSnapHelper();
        helper.attachToRecyclerView(physExam_recyclerView);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
       /* mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), physicalExamMap);

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        if (mViewPager != null) {
            mViewPager.setAdapter(mSectionsPagerAdapter);
        }*/
        /*TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setSelectedTabIndicatorHeight(15);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tabLayout.setSelectedTabIndicatorColor(getColor(R.color.amber));
            tabLayout.setTabTextColors(getColor(R.color.white), getColor(R.color.amber));
        } else {
            tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.amber));
            tabLayout.setTabTextColors(getResources().getColor(R.color.white), getResources().getColor(R.color.amber));
        }
        if (tabLayout != null) {
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            tabLayout.setupWithViewPager(mViewPager);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });

         */
      /*
      Commented to avoid crash...
        CustomLog.e(TAG, "PhyExam: " + physicalExamMap.getTotalNumberOfExams());*/

        mgender = fetch_gender(patientUuid);

        if (mgender.equalsIgnoreCase("M")) {
            physicalExamMap.fetchItem("0");
        } else if (mgender.equalsIgnoreCase("F")) {
            physicalExamMap.fetchItem("1");
        }
        physicalExamMap.refresh(selectedExamsList); //refreshing the physical exam nodes with updated json

        // flaoting value of age is passed to Node for comparison...
        physicalExamMap.fetchAge(float_ageYear_Month);
        physicalExamMap.refresh(selectedExamsList); //refreshing the physical exam nodes with updated json

        adapter = new QuestionsAdapter(this, physicalExamMap, physExam_recyclerView, this.getClass().getSimpleName(), this, false);
        physExam_recyclerView.setAdapter(adapter);
        recyclerViewIndicator.attachToRecyclerView(physExam_recyclerView);

    }

    private boolean insertDb(String value) {
        CustomLog.i(TAG, "insertDb: ");

        ObsDAO obsDAO = new ObsDAO();
        ObsDTO obsDTO = new ObsDTO();
        obsDTO.setConceptuuid(UuidDictionary.PHYSICAL_EXAMINATION);
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

    @Override
    public void fabClickedAtEnd() {

        complaintConfirmed = physicalExamMap.areRequiredAnswered();

        if (complaintConfirmed) {

            physicalString = physicalExamMap.generateFindings();

            List<String> imagePathList = physicalExamMap.getImagePathList();

            if (imagePathList != null) {
                for (String imagePath : imagePathList) {
                    updateImageDatabase();
                }
            }

            if (intentTag != null && intentTag.equals("edit")) {
                updateDatabase(physicalString);
                Intent intent = new Intent(PhysicalExamActivity.this, VisitSummaryActivity_New.class);
                intent.putExtra("patientUuid", patientUuid);
                intent.putExtra("visitUuid", visitUuid);
                intent.putExtra("gender", mgender);
                intent.putExtra("encounterUuidVitals", encounterVitals);
                intent.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                intent.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                intent.putExtra("state", state);
                intent.putExtra("name", patientName);
                intent.putExtra("gender", patientGender);
                intent.putExtra("float_ageYear_Month", float_ageYear_Month);
                intent.putExtra("tag", intentTag);
                intent.putExtra("hasPrescription", "false");

                for (String exams : selectedExamsList) {
                    CustomLog.i(TAG, "onClick:++ " + exams);
                }
                // intent.putStringArrayListExtra("exams", selectedExamsList);
                startActivity(intent);
            } else {
                boolean obsId = insertDb(physicalString);
                Intent intent1 = new Intent(PhysicalExamActivity.this, VisitSummaryActivity_New.class); // earlier visitsummary
                intent1.putExtra("patientUuid", patientUuid);
                intent1.putExtra("visitUuid", visitUuid);
                intent1.putExtra("encounterUuidVitals", encounterVitals);
                intent1.putExtra("encounterUuidAdultIntial", encounterAdultIntials);
                intent1.putExtra("EncounterAdultInitial_LatestVisit", EncounterAdultInitial_LatestVisit);
                intent1.putExtra("state", state);
                intent1.putExtra("name", patientName);
                intent1.putExtra("gender", patientGender);
                intent1.putExtra("tag", intentTag);
                intent1.putExtra("float_ageYear_Month", float_ageYear_Month);
                intent1.putExtra("hasPrescription", "false");

                // intent1.putStringArrayListExtra("exams", selectedExamsList);
                startActivity(intent1);
            }

        } else {
            questionsMissing();
        }

    }

    @Override
    public void onChildListClickEvent(int groupPosition, int childPos, int physExamPos) {
        Node question = physicalExamMap.getExamNode(physExamPos).getOption(groupPosition).getOption(childPos);
        //CustomLog.d("Clicked", question.language());
        question.toggleSelected();
        if (physicalExamMap.getExamNode(physExamPos).getOption(groupPosition).anySubSelected()) {
            physicalExamMap.getExamNode(physExamPos).getOption(groupPosition).setSelected(true);
        } else {
            physicalExamMap.getExamNode(physExamPos).getOption(groupPosition).setUnselected();
        }
        adapter.notifyDataSetChanged();


        if (question.getInputType() != null && question.isSelected()) {

            if (question.getInputType().equals("camera")) {
                if (!filePath.exists()) {
                    boolean res = filePath.mkdirs();
                    CustomLog.i("RES>", "" + filePath + " -> " + res);
                }
                imageName = UUID.randomUUID().toString();
                Node.handleQuestion(question, this, adapter, filePath.toString(), imageName);
            } else {
                Node.handleQuestion(question, this, adapter, null, null);
            }


        }

        if (!question.isTerminal() && question.isSelected()) {
            Node.subLevelQuestion(question, this, adapter, filePath.toString(), imageName);
        }
    }


    private void updateDatabase(String string) {
        ObsDTO obsDTO = new ObsDTO();
        ObsDAO obsDAO = new ObsDAO();
        try {
            obsDTO.setConceptuuid(UuidDictionary.PHYSICAL_EXAMINATION);
            obsDTO.setEncounteruuid(encounterAdultIntials);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(string);
            obsDTO.setUuid(obsDAO.getObsuuid(encounterAdultIntials, UuidDictionary.PHYSICAL_EXAMINATION));

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

    public void questionsMissing() {
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(this);
        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,R.style.AlertDialogStyle);
        alertDialogBuilder.setMessage(getResources().getString(R.string.question_answer_all_phy_exam));
        alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.show();
        //alertDialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
    }

    private void updateImageDatabase() {
        ImagesDAO imagesDAO = new ImagesDAO();

        try {
            imagesDAO.insertObsImageDatabase(imageName, encounterAdultIntials, UuidDictionary.COMPLEX_IMAGE_PE, "");
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
                physicalExamMap.setImagePath(mCurrentPhotoPath);
                CustomLog.i(TAG, mCurrentPhotoPath);
                physicalExamMap.displayImage(this, filePath.getAbsolutePath(), imageName);
                updateImageDatabase();

            }

        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */

        public static PhysicalExam exam_list;

        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        CustomExpandableListAdapter adapter;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber, PhysicalExam exams, String patientUuid, String visitUuid) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putString("patientUuid", patientUuid);
            args.putString("visitUuid", visitUuid);
            exam_list = exams;
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_physical_exam, container, false);

            final ImageView imageView = rootView.findViewById(R.id.physical_exam_image_view);

            TextView textView = rootView.findViewById(R.id.physical_exam_text_view);
            ExpandableListView expandableListView = rootView.findViewById(R.id.physical_exam_expandable_list_view);

            int viewNumber = getArguments().getInt(ARG_SECTION_NUMBER);
            final String patientUuid1 = getArguments().getString("patientUuid");
            final String visitUuid1 = getArguments().getString("visitUuid");
            final Node viewNode = exam_list.getExamNode(viewNumber - 1);
            final String parent_name = exam_list.getExamParentNodeName(viewNumber - 1);
            String nodeText = parent_name + " : " + viewNode.findDisplay();

            textView.setText(nodeText);

            Node displayNode = viewNode.getOption(0);

            if (displayNode.isAidAvailable()) {
                String type = displayNode.getJobAidType();
                if (type.equals("video")) {
                    imageView.setVisibility(View.GONE);
                } else if (type.equals("image")) {
                    imageView.setVisibility(View.VISIBLE);
                    String drawableName = "physicalExamAssets/" + displayNode.getJobAidFile() + ".jpg";
                    try {
                        // get input stream
                        InputStream ims = getContext().getAssets().open(drawableName);
                        // load image as Drawable
                        Drawable d = Drawable.createFromStream(ims, null);
                        // set image to ImageView
                        imageView.setImageDrawable(d);
                        imageView.setMinimumHeight(500);
                        imageView.setMinimumWidth(500);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        imageView.setVisibility(View.GONE);
                    }
                } else {
                    imageView.setVisibility(View.GONE);
                }
            } else {
                imageView.setVisibility(View.GONE);
            }


            adapter = new CustomExpandableListAdapter(getContext(), viewNode, this.getClass().getSimpleName());
            expandableListView.setAdapter(adapter);


            expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {


                    return false;
                }
            });

            expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                @Override
                public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                    return true;
                }
            });

            expandableListView.expandGroup(0);


            return rootView;
        }


    }

    @Override
    protected void onStop() {
        super.onStop();
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

}


