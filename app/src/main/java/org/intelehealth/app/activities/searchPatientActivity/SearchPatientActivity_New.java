package org.intelehealth.app.activities.searchPatientActivity;

import static org.intelehealth.app.database.dao.EncounterDAO.getStartVisitNoteEncounterByVisitUUID;
import static org.intelehealth.app.database.dao.PatientsDAO.getQueryPatientsObs;
import static org.intelehealth.app.database.dao.PatientsDAO.isVisitPresentForPatient_fetchVisitValues;
import static org.intelehealth.app.utilities.StringUtils.inputFilter_SearchBar;
import static org.intelehealth.app.utilities.StringUtils.setGenderAgeLocal;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.LocaleList;
import android.provider.SearchRecentSuggestions;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.DisplayMetrics;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.activities.onboarding.PersonalConsentActivity;
import org.intelehealth.app.utilities.AddPatientUtils;
import org.intelehealth.app.utilities.CustomLog;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.onboarding.PrivacyPolicyActivity_New;
import org.intelehealth.app.activities.searchPatientActivity.adapter.SearchChipsPreviewGridAdapter;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.PatientsDAO;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.models.dto.VisitDTO;
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.utilities.CustomLog;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.DownloadFilesUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * Created by: Prajwal Waingankar On: 29/Aug/2022
 * Github: prajwalmw
 */

public class SearchPatientActivity_New extends BaseActivity {
    RecyclerView search_recycelview;
    SearchPatientAdapter_New adapter;
    EditText mSearchEditText;
    TextView search_hint_text, allPatientsTV;
    LinearLayout addPatientTV;
    String query;
    boolean fullyLoaded = false;
    RelativeLayout view_nopatientfound;
    public static final String TAG = "SearchPatient_New";
    private SearchRecentSuggestions suggestions;
    private SessionManager sessionManager;
    private SQLiteDatabase db;
    private ImageButton backbtn;
    View dividerView;
    ImageView iconSearch, iconClear;

    private RecyclerView mSearchHistoryRecyclerView;

    private final int limit = 20;
    private int start = 0, end = start + limit;
    private boolean isFullyLoaded = false;
    List<PatientDTO> patientDTOList;
    List<PatientDTO> recent = new ArrayList<>();
    AlertDialog commonLoadingDialog;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_patient_new);
        sessionManager = new SessionManager(this);
        search_recycelview = findViewById(R.id.search_recycelview);
        LinearLayoutManager lm = new LinearLayoutManager(getApplicationContext());
        search_recycelview.setLayoutManager(lm);
        initializeRecyclerView(lm);

        mSearchEditText = findViewById(R.id.search_txt_enter);
        mSearchEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25), inputFilter_SearchBar}); //maxlength 25

        search_hint_text = findViewById(R.id.search_hint_text);
        view_nopatientfound = findViewById(R.id.view_nopatientfound);
        backbtn = findViewById(R.id.backbtn);
        dividerView = findViewById(R.id.divider_view);
        allPatientsTV = findViewById(R.id.all_patients_tv);
        addPatientTV = findViewById(R.id.add_new_patientTV);
        iconSearch = findViewById(R.id.icon_search);
        iconClear = findViewById(R.id.icon_clear);


        mSearchHistoryRecyclerView = findViewById(R.id.rcv_selected_container);
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);

        mSearchHistoryRecyclerView.setLayoutManager(layoutManager);

        previous_SearchResults();
        queryAllPatients();

        addPatientTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddPatientUtils.navigate(SearchPatientActivity_New.this);
                finish();
            }
        });

        iconClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mSearchEditText.getText().toString().isEmpty()) {
                    recent.clear();
                    mSearchEditText.setText("");
                }
            }
        });

        iconSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchText = mSearchEditText.getText().toString();
                performSearch(searchText);
            }
        });

        backbtn.setOnClickListener(v -> {
            finish();
        });

        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
               /* mSearchEditText.requestFocus();
                iconClear.setVisibility(View.GONE);
                iconSearch.setVisibility(View.VISIBLE);*/
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().isEmpty()) {
                    iconClear.setVisibility(View.GONE);
                    iconSearch.setVisibility(View.VISIBLE);
                    allPatientsTV.setText(getString(R.string.all_patients_txt));

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            query = "";
                            doQuery(query);
                        }
                    }, 100);

                } else {
                    iconClear.setVisibility(View.VISIBLE);
                    iconSearch.setVisibility(View.GONE);
                }

            }
        });

        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch(mSearchEditText.getText().toString());
                    return true;
                }
                return false;
            }
        });
    }

    private void performSearch(String searchText) {
        if (!searchText.isEmpty()) {
            iconSearch.setVisibility(View.GONE);
            iconClear.setVisibility(View.VISIBLE);
            String text = searchText;

            if (sessionManager.getAppLanguage().equalsIgnoreCase("hi"))
                allPatientsTV.setText("\"" + text + "\"" + " " + getResources().getString(R.string.results_for));
            else
                allPatientsTV.setText(getResources().getString(R.string.results_for) + " \"" + text + "\"");

            mSearchEditText.setTextColor(ContextCompat.getColor(this, R.color.white));
            managePreviousSearchStorage(text);
            query = text;
            doQuery(text);
        } else {
            allPatientsTV.setText(getString(R.string.all_patients_txt));
            query = "";
            doQuery(query);
        }
    }

    private void managePreviousSearchStorage(String text) {
        String retrievedPreviousData = sessionManager.getPreviousSearchQuery();
        List<String> retrievedPreviousSearchList = new ArrayList<>();
        if (retrievedPreviousData != null && !retrievedPreviousData.isEmpty()) {
            if (retrievedPreviousData.contains(",")) {
                retrievedPreviousSearchList = new ArrayList<String>(Arrays.asList(retrievedPreviousData.split(",")));
            } else retrievedPreviousSearchList.add(retrievedPreviousData);

            if (retrievedPreviousSearchList.size() == 5) {
                retrievedPreviousSearchList.remove(0);
            }
            if (!retrievedPreviousSearchList.contains(text)) retrievedPreviousSearchList.add(text);
            StringBuffer sb = new StringBuffer();
            for (String s : retrievedPreviousSearchList) {
                sb.append(s);
                sb.append(",");
            }
            String str = sb.toString();
            sessionManager.setPreviousSearchQuery(str);
        } else {
            sessionManager.setPreviousSearchQuery(text);
        }
        previous_SearchResults();
    }

    private void queryAllPatients() {
        patientDTOList = PatientsDAO.getAllPatientsFromDB(limit, start);   // fetch first 15 records and dont skip any records ie. start = 0 for 2nd itertion skip first 15records.
        CustomLog.d(TAG, "queryAllPatients: " + patientDTOList.size());
        commonLoadingDialog = new DialogUtils().showCommonLoadingDialog(this, getString(R.string.loading), "");
        commonLoadingDialog.setCancelable(false);

        if (!patientDTOList.isEmpty()) { // ie. the entered text is present in db

            /**
             * handling patient get operation in bg thread
             */
            compositeDisposable.add(fetchDataForTagObs(patientDTOList)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(patientDTOS -> {
                        if (patientDTOS.isEmpty()) {
                            searchData_Unavailable();
                            commonLoadingDialog.dismiss();
                            return;
                        }
                        searchData_Available();
                        try {
                            adapter = new SearchPatientAdapter_New(SearchPatientActivity_New.this, patientDTOS);
                            search_recycelview.setAdapter(adapter);
                            start = end;
                            end += limit;
                        } catch (Exception e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                            Logger.logE("doquery", "doquery", e);
                        }
                        commonLoadingDialog.dismiss();
                    }, err -> {
                        commonLoadingDialog.dismiss();
                    })
            );
        } else {
            searchData_Unavailable();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(setLocale(newBase));
    }

    public Context setLocale(Context context) {
        SessionManager sessionManager1 = new SessionManager(context);
        String appLanguage = sessionManager1.getAppLanguage();
        Resources res = context.getResources();
        Configuration conf = res.getConfiguration();
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        conf.setLocale(locale);
        context.createConfigurationContext(conf);
        DisplayMetrics dm = res.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            conf.setLocales(new LocaleList(locale));
        } else {
            conf.locale = locale;
        }
        res.updateConfiguration(conf, dm);
        return context;
    }

    private void previous_SearchResults() {
        String previousSearchText = sessionManager.getPreviousSearchQuery();
        List<String> raWPreviousSearchList = new ArrayList<>();
        List<String> retrievedPreviousSearchList = new ArrayList<>();

        if (previousSearchText.isEmpty()) {
            search_hint_text.setVisibility(View.GONE);
            dividerView.setVisibility(View.GONE);
        } else {
            allPatientsTV.setVisibility(View.VISIBLE);
            if (previousSearchText.contains(",")) {
                raWPreviousSearchList = new ArrayList<String>(Arrays.asList(previousSearchText.split(",")));
            } else raWPreviousSearchList.add(previousSearchText);

            String value = "";
            for (int i = raWPreviousSearchList.size() - 1; i >= 0; i--) {
                //value = value + "\"" + retrievedPreviousSearchList.get(i) + "\"" + "\n";
                retrievedPreviousSearchList.add(raWPreviousSearchList.get(i));
            }

            search_hint_text.setVisibility(View.VISIBLE);
            //search_hint_text.setText("Previously Searched..\n" + value);
        }
        SearchChipsPreviewGridAdapter searchChipsPreviewGridAdapter = new SearchChipsPreviewGridAdapter(this, mSearchHistoryRecyclerView, retrievedPreviousSearchList, new SearchChipsPreviewGridAdapter.OnItemSelection() {
            @Override
            public void onSelect(String data) {
                mSearchEditText.setText(data);
                mSearchEditText.setSelection(mSearchEditText.getText().length());
                mSearchEditText.clearFocus();
                iconSearch.performClick();
            }

            @Override
            public void onRemoved(String data) {

            }
        });
        mSearchHistoryRecyclerView.setAdapter(searchChipsPreviewGridAdapter);

    }

    private void doQuery(String query) {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);

        recent.clear();
        if (query.equalsIgnoreCase("")) {
            try {
                searchData_Available();
                adapter = new SearchPatientAdapter_New(this, patientDTOList);
                fullyLoaded = true;
                search_recycelview.setAdapter(adapter);

            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                Logger.logE("doquery", "doquery", e);
            }
            return;
        }

        /**
         * handling searching operation in background thread
         */
        commonLoadingDialog = new DialogUtils().showCommonLoadingDialog(this, getString(R.string.loading), "");
        commonLoadingDialog.setCancelable(false);

        compositeDisposable.add(getQueryPatientsObs(query) // fetches all the list of patients.
                .concatMap(new Function<List<PatientDTO>, ObservableSource<List<PatientDTO>>>() {
                    @Override
                    public ObservableSource<List<PatientDTO>> apply(List<PatientDTO> patientDTOS) throws Exception {
                        return fetchDataForTagObs(patientDTOS);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(patientDTOS -> {
                    if (patientDTOS.isEmpty()) {
                        searchData_Unavailable();
                        commonLoadingDialog.dismiss();
                        return;
                    }
                    CustomLog.v(TAG, "size: " + patientDTOS.size());

                    searchData_Available();

                    recent = patientDTOS;

                    try {
                        adapter = new SearchPatientAdapter_New(SearchPatientActivity_New.this, patientDTOS);
                        fullyLoaded = true;
                        search_recycelview.setAdapter(adapter);

                    } catch (Exception e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                        CustomLog.e("doquery", "doquery", e);
                    }
                    commonLoadingDialog.dismiss();
                }, err -> {
                    commonLoadingDialog.dismiss();
                }));
    }

    private List<PatientDTO> fetchDataforTags(List<PatientDTO> patientDTOList) {
        db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();

        /**
         * 1. Check first if visit is present for this patient or not if yes than do other code logic.
         */
        for (int i = 0; i < patientDTOList.size(); i++) {
            VisitDTO visitDTO = isVisitPresentForPatient_fetchVisitValues(patientDTOList.get(i).getUuid());

            /**
             * 2. now check if only visit is present than only proceed to get value for priority tag, presc tag, startdate tag.
             */
            if (visitDTO.getUuid() != null && visitDTO.getStartdate() != null) {
                //  1. Priority Tag.
                EncounterDAO encounterDAO = new EncounterDAO();
                String emergencyUuid = "";
                try {
                    emergencyUuid = encounterDAO.getEmergencyEncounters(visitDTO.getUuid(), encounterDAO.getEncounterTypeUuid("EMERGENCY"));
                } catch (DAOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    emergencyUuid = "";
                }
                if (!emergencyUuid.isEmpty() || !emergencyUuid.equalsIgnoreCase("")) { // ie. visit is emergency visit.
                    patientDTOList.get(i).setEmergency(true);
                } else { //ie. visit not emergency.
                    patientDTOList.get(i).setEmergency(false);
                }

                //  2. startdate added.
                String visit_start_date = DateAndTimeUtils.date_formatter(visitDTO.getStartdate(), "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "dd MMM 'at' HH:mm a");    // Eg. 26 Sep 2022 at 03:15 PM
                CustomLog.v("SearchPatient", "date: " + visit_start_date);

                patientDTOList.get(i).setVisit_startdate(visit_start_date);

                //  3. prescription received/pending tag logic.
                String encounteruuid = getStartVisitNoteEncounterByVisitUUID(visitDTO.getUuid());
                if (!encounteruuid.isEmpty() && !encounteruuid.equalsIgnoreCase("")) {
                    patientDTOList.get(i).setPrescription_exists(true);
                } else {
                    patientDTOList.get(i).setPrescription_exists(false);
                }

                // checking if visit is uploaded or not - start
                patientDTOList.get(i).setVisitDTO(visitDTO);
                // checking if visit is uploaded or not - end

            } else {
                /**
                 * no visit for this patient.
                 * dont add startvisitdate value into this model keep it null and later check for null check and add logic
                 */
            }
        }

        return patientDTOList;
    }

    private Observable<List<PatientDTO>> fetchDataForTagObs(List<PatientDTO> patientDTOList) {
        ImagesDAO imagesDAO = new ImagesDAO();
        return Observable.fromCallable(() -> {
            for (int i = 0; i < patientDTOList.size(); i++) {
                VisitDTO visitDTO = isVisitPresentForPatient_fetchVisitValues(patientDTOList.get(i).getUuid());

                /**
                 * 2. now check if only visit is present than only proceed to get value for priority tag, presc tag, startdate tag.
                 */
                if (visitDTO.getUuid() != null && visitDTO.getStartdate() != null) {
                    //  1. Priority Tag.
                    EncounterDAO encounterDAO = new EncounterDAO();
                    String emergencyUuid = "";
                    try {
                        emergencyUuid = encounterDAO.getEmergencyEncounters(visitDTO.getUuid(), encounterDAO.getEncounterTypeUuid("EMERGENCY"));
                    } catch (DAOException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                        emergencyUuid = "";
                    }
                    if (!emergencyUuid.isEmpty() || !emergencyUuid.equalsIgnoreCase("")) { // ie. visit is emergency visit.
                        patientDTOList.get(i).setEmergency(true);
                    } else { //ie. visit not emergency.
                        patientDTOList.get(i).setEmergency(false);
                    }

                    //  2. startdate added.
                    String visit_start_date = DateAndTimeUtils.date_formatter(visitDTO.getStartdate(), "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "dd MMM 'at' HH:mm a");    // Eg. 26 Sep 2022 at 03:15 PM
                    CustomLog.v("SearchPatient", "date: " + visit_start_date);

                    patientDTOList.get(i).setVisit_startdate(visit_start_date);

                    //  3. prescription received/pending tag logic.
                    String encounteruuid = getStartVisitNoteEncounterByVisitUUID(visitDTO.getUuid());
                    if (!encounteruuid.isEmpty() && !encounteruuid.equalsIgnoreCase("")) {
                        patientDTOList.get(i).setPrescription_exists(true);
                    } else {
                        patientDTOList.get(i).setPrescription_exists(false);
                    }

                    // checking if visit is uploaded or not - start
                    patientDTOList.get(i).setVisitDTO(visitDTO);
                    // checking if visit is uploaded or not - end

                    patientDTOList.get(i).setGenderAgeString(setGenderAgeLocal(SearchPatientActivity_New.this, patientDTOList.get(i).getDateofbirth(), patientDTOList.get(i).getGender()));

                    //removed patient image loading from here
                    //and moved to sync dao
                    /*String profileImage = "";
                    try {
                        profileImage = imagesDAO.getPatientProfileChangeTime(patientDTOList.get(i).getUuid());
                    } catch (DAOException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                    } finally {
                        patientDTOList.get(i).setPatientImageFromImageDao(profileImage);
                    }

                    if (patientDTOList.get(i).getPatientPhoto() == null || patientDTOList.get(i).getPatientPhoto().equalsIgnoreCase("")) {
                        if (NetworkConnection.isOnline(this)) {
                            profilePicDownloaded(patientDTOList.get(i));
                        }
                    }
                    //3.
                    if (profileImage == null || !profileImage.equalsIgnoreCase("")) {
                        if (NetworkConnection.isOnline(this)) {
                            profilePicDownloaded(patientDTOList.get(i));
                        }
                    }*/
                } else {
                    /**
                     * no visit for this patient.
                     * dont add startvisitdate value into this model keep it null and later check for null check and add logic
                     */
                }
            }

            return patientDTOList;
        });
    }

    public void profilePicDownloaded(PatientDTO model) {
        UrlModifiers urlModifiers = new UrlModifiers();
        String url = urlModifiers.patientProfileImageUrl(model.getUuid());
        Logger.logD("TAG", "profileimage url" + url);
        Observable<ResponseBody> profilePicDownload = AppConstants.apiInterface.PERSON_PROFILE_PIC_DOWNLOAD
                (url, "Basic " + sessionManager.getEncoded());
        profilePicDownload.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<ResponseBody>() {
                    @Override
                    public void onNext(ResponseBody file) {
                        DownloadFilesUtils downloadFilesUtils = new DownloadFilesUtils();
                        downloadFilesUtils.saveToDisk(file, model.getUuid());
                        Logger.logD("TAG", file.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.logD("TAG", e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Logger.logD("TAG", "complete" + model.getPatientPhoto());
                        PatientsDAO patientsDAO = new PatientsDAO();
                        boolean updated = false;
                        try {
                            updated = patientsDAO.updatePatientPhoto(model.getUuid(),
                                    AppConstants.IMAGE_PATH + model.getUuid() + ".jpg");
                            model.setPatientImageFromDownload(AppConstants.IMAGE_PATH + model.getUuid() + ".jpg");
                        } catch (DAOException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                        ImagesDAO imagesDAO = new ImagesDAO();
                        boolean isImageDownloaded = false;
                        try {
                            isImageDownloaded = imagesDAO.insertPatientProfileImages(
                                    AppConstants.IMAGE_PATH + model.getUuid() + ".jpg", model.getUuid());
                        } catch (DAOException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                    }
                });
    }

    private void searchData_Available() {
        mSearchHistoryRecyclerView.setVisibility(View.VISIBLE);
        search_hint_text.setVisibility(View.VISIBLE);
        view_nopatientfound.setVisibility(View.GONE);
        search_recycelview.setVisibility(View.VISIBLE);

        if (sessionManager.getPreviousSearchQuery().isEmpty() && sessionManager.getPreviousSearchQuery().equalsIgnoreCase("")) {
            search_hint_text.setVisibility(View.GONE);
            allPatientsTV.setVisibility(View.VISIBLE);
            dividerView.setVisibility(View.GONE);
        }
//        findViewById(R.id.search_clear_tv).setVisibility(View.VISIBLE);
    }

    private void searchData_Unavailable() {
        mSearchHistoryRecyclerView.setVisibility(View.GONE);
        search_hint_text.setVisibility(View.GONE);
        view_nopatientfound.setVisibility(View.VISIBLE);
        search_recycelview.setVisibility(View.GONE);
        dividerView.setVisibility(View.GONE);
        allPatientsTV.setVisibility(View.GONE);
        findViewById(R.id.search_clear_tv).setVisibility(View.GONE);
    }

    public void clearSearch(View view) {
        mSearchEditText.setText("");
        view.setVisibility(View.GONE);
    }

    private void initializeRecyclerView(LinearLayoutManager linearLayoutManager) {
        search_recycelview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (patientDTOList != null && patientDTOList.size() == 0) {
                    isFullyLoaded = true;
                    return;
                }
                if (!isFullyLoaded && newState == RecyclerView.SCROLL_STATE_IDLE && linearLayoutManager.findLastVisibleItemPosition() == adapter.getItemCount() - 1) {
                    if (recent != null) {
                        if (recent.size() > 0) {

                        } else {
                            Toast.makeText(SearchPatientActivity_New.this, R.string.loading_more, Toast.LENGTH_SHORT).show();
                            setMoreDataIntoRecyclerView();
                        }
                    }
                }
            }
        });
    }

    // This method will be accessed every time the person scrolls the recyclerView further.
    private void setMoreDataIntoRecyclerView() {
        if (recent.size() > 0) {    // on scroll, new data loads issue fix.

        } else {
            if (patientDTOList != null && patientDTOList.size() == 0) {
                isFullyLoaded = true;
                return;
            }

            //   patientDTOList = PatientsDAO.getAllPatientsFromDB(limit, start);    // for n iteration limit be fixed == 15 and start - offset will keep skipping each records.
            List<PatientDTO> tempList = PatientsDAO.getAllPatientsFromDB(limit, start); // for n iteration limit be fixed == 15 and start - offset will keep skipping each records.
            if (tempList.size() > 0) {
                patientDTOList.addAll(tempList);
                CustomLog.d(TAG, "queryAllPatients: " + patientDTOList.size());
                adapter.patientDTOS.addAll(tempList);
                adapter.notifyDataSetChanged();
                start = end;
                end += limit;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}
