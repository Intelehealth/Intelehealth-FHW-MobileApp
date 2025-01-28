package org.intelehealth.app.database.dao;

import static org.intelehealth.klivekit.data.PreferenceHelper.CONFIG_VERSION;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.intelehealth.app.utilities.CustomLog;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.appointment.dao.AppointmentDAO;
import org.intelehealth.app.database.InteleHealthDatabaseHelper;
import org.intelehealth.app.models.ActivePatientModel;
import org.intelehealth.app.models.dto.PatientDTO;
import org.intelehealth.app.models.dto.ResponseDTO;
import org.intelehealth.app.models.dto.VisitDTO;
import org.intelehealth.app.models.pushRequestApiCall.PushRequestApiCall;
import org.intelehealth.app.models.pushResponseApiCall.PushResponseApiCall;
import org.intelehealth.app.services.InitialSyncIntentService;
import org.intelehealth.app.syncModule.SyncProgress;
import org.intelehealth.app.utilities.CustomLog;
import org.intelehealth.app.utilities.DownloadFilesUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.NotificationID;
import org.intelehealth.app.utilities.PatientsFrameJson;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.config.data.ConfigRepository;
import org.intelehealth.config.network.response.ConfigResponse;
import org.intelehealth.klivekit.data.PreferenceHelper;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncDAO {
    public static final String TAG = "SyncDAO";
    public static final String PULL_ISSUE = "PULL_ISSUE";
    SessionManager sessionManager = null;
    InteleHealthDatabaseHelper mDbHelper;
    private SQLiteDatabase db;
    String appLanguage;

    private static final SyncProgress liveDataSync = new SyncProgress();


    public boolean SyncData(ResponseDTO responseDTO) throws DAOException {
        boolean isSynced = true;
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        appLanguage = sessionManager.getAppLanguage();
        if (!appLanguage.equalsIgnoreCase("")) {
            setLocale(appLanguage);
        }
        PatientsDAO patientsDAO = new PatientsDAO();
        VisitsDAO visitsDAO = new VisitsDAO();
        EncounterDAO encounterDAO = new EncounterDAO();
        ObsDAO obsDAO = new ObsDAO();
        LocationDAO locationDAO = new LocationDAO();
        ProviderDAO providerDAO = new ProviderDAO();
        VisitAttributeListDAO visitAttributeListDAO = new VisitAttributeListDAO();
        ProviderAttributeLIstDAO providerAttributeLIstDAO = new ProviderAttributeLIstDAO();

        try {
            Logger.logD(TAG, "pull sync started");
            saveConfig(responseDTO.getData().getConfigResponse());

            patientsDAO.patinetAttributeMaster(responseDTO.getData().getPatientAttributeTypeMasterDTO());
            Logger.logD(TAG, "patinetAttributeMaster = " + responseDTO.getData().getPatientAttributeTypeMasterDTO().size());

            patientsDAO.insertPatients(responseDTO.getData().getPatientDTO());
            Logger.logD(TAG, "insertPatients = " + responseDTO.getData().getPatientDTO().size());

            patientsDAO.patientAttributes(responseDTO.getData().getPatientAttributesDTO());
            Logger.logD(TAG, "insertPatientAttributes = " + responseDTO.getData().getPatientAttributesDTO().size());

            visitsDAO.insertVisit(responseDTO.getData().getVisitDTO());
            Logger.logD(TAG, "insertVisit = " + responseDTO.getData().getVisitDTO().size());

            encounterDAO.insertEncounter(responseDTO.getData().getEncounterDTO());
            Logger.logD(TAG, "insertEncounter = " + responseDTO.getData().getEncounterDTO().size());

            obsDAO.insertObsTemp(responseDTO.getData().getObsDTO());
            Logger.logD(TAG, "insertObsTemp = " + responseDTO.getData().getObsDTO().size());

            locationDAO.insertLocations(responseDTO.getData().getLocationDTO());
            Logger.logD(TAG, "insertLocations = " + responseDTO.getData().getLocationDTO().size());

            providerDAO.insertProviders(responseDTO.getData().getProviderlist());
            Logger.logD(TAG, "insertProviders = " + responseDTO.getData().getProviderlist().size());

            providerAttributeLIstDAO.insertProvidersAttributeList
                    (responseDTO.getData().getProviderAttributeList());
            Logger.logD(TAG, "insertProvidersAttributeList = " + responseDTO.getData().getProviderAttributeList().size());

            visitAttributeListDAO.insertProvidersAttributeList(responseDTO.getData().getVisitAttributeList());
            Logger.logD(TAG, "insertVisitAttributeList = " + responseDTO.getData().getVisitAttributeList().size());

            //downloading images if not found
            downloadPatientImages(responseDTO.getData().getPatientDTO());
//           visitsDAO.insertVisitAttribToDB(responseDTO.getData().getVisitAttributeList())

            //Logger.logD(TAG, "Pull ENCOUNTER: " + responseDTO.getData().getEncounterDTO());
            Logger.logD(TAG, "Pull sync ended");
            sessionManager.setFirstTimeSyncExecute(false);
            IntelehealthApplication.getAppContext().sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                    .setPackage(IntelehealthApplication.getAppContext().getPackageName())
                    .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_PUSH_DATA_TO_LOCAL_DB_DONE));
        } catch (Exception e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
            Logger.logE(TAG, "Exception => " + e.getLocalizedMessage(), e);
            throw new DAOException(e.getMessage(), e);
        }

        return isSynced;

    }

    /**
     * here downloading patient images while syncing
     * to get rid of lagging and ANR
     *
     * @param patientDTOList
     */
    private void downloadPatientImages(List<PatientDTO> patientDTOList) {
        ImagesDAO imagesDAO = new ImagesDAO();
        for (int i = 0; i < patientDTOList.size(); i++) {
            String profileImage = "";
            try {
                profileImage = imagesDAO.getPatientProfileChangeTime(patientDTOList.get(i).getUuid());
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            } finally {
                patientDTOList.get(i).setPatientImageFromImageDao(profileImage);
            }

            if (patientDTOList.get(i).getPatientPhoto() == null || patientDTOList.get(i).getPatientPhoto().equalsIgnoreCase("")) {
                if (NetworkConnection.isOnline(IntelehealthApplication.getAppContext())) {
                    profilePicDownloaded(patientDTOList.get(i));
                }
            }
            //3.
            if (profileImage == null || !profileImage.equalsIgnoreCase("")) {
                if (NetworkConnection.isOnline(IntelehealthApplication.getAppContext())) {
                    profilePicDownloaded(patientDTOList.get(i));
                }
            }
        }
    }

    public void profilePicDownloaded(PatientDTO model) {
        UrlModifiers urlModifiers = new UrlModifiers();
        String url = urlModifiers.patientProfileImageUrl(model.getUuid());
        Logger.logD("TAG", "profileimage url" + url);
        Observable<ResponseBody> profilePicDownload = AppConstants.apiInterface.PERSON_PROFILE_PIC_DOWNLOAD
                (url, "Basic " + sessionManager.getEncoded());
        profilePicDownload
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
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

    private void saveConfig(ConfigResponse response) {
        CustomLog.d(TAG, "saveConfig");
        PreferenceHelper helper = new PreferenceHelper(IntelehealthApplication.getAppContext());
        int version = helper.get(CONFIG_VERSION, 0);
        CustomLog.d(TAG, "saveConfig old version => %s", version);
        if (version > 0 && response.getVersion() > version) {
            ConfigRepository repository = new ConfigRepository(IntelehealthApplication.getAppContext());
            repository.saveAllConfig(response, () -> Unit.INSTANCE);
            CustomLog.d(TAG, "saveConfig new version => %s", response.getVersion());
        } else helper.save(CONFIG_VERSION, response.getVersion());
    }

    public boolean pullData_Background(final Context context, int pageNo) {

        mDbHelper = new InteleHealthDatabaseHelper(context);
        db = mDbHelper.getWriteDb();

        sessionManager = new SessionManager(context);
        String encoded = sessionManager.getEncoded();
        String oldDate = sessionManager.getPullExcutedTime();
        Log.d(TAG, "pullData_Background: encoded : " + encoded);
        String url = BuildConfig.SERVER_URL + "/EMR-Middleware/webapi/pull/pulldata/" +
                sessionManager.getLocationUuid() + "/" + sessionManager.getPullExcutedTime() +
                "/" + pageNo + "/" + AppConstants.PAGE_LIMIT;
        ;
//        String url =  sessionManager.getServerUrl() + "/pulldata/" + sessionManager.getLocationUuid() + "/" + sessionManager.getPullExcutedTime();
        Call<ResponseDTO> middleWarePullResponseCall = AppConstants.apiInterface.RESPONSE_DTO_CALL(url, "Basic " + encoded);
        Logger.logD("Start pull request", "Started");
        middleWarePullResponseCall.enqueue(new Callback<ResponseDTO>() {
            @Override
            public void onResponse(Call<ResponseDTO> call, Response<ResponseDTO> response) {
                // AppConstants.notificationUtils.showNotifications("Sync background", "Sync in progress..", 1, IntelehealthApplication.getAppContext());
                if (response.body() != null && response.body().getData() != null) {
                    sessionManager.setPulled(response.body().getData().getPullexecutedtime());
                }
                if (response.isSuccessful()) {
                    // SyncDAO syncDAO = new SyncDAO();

                    //handling response data from background thread
                    //to prevent lagging
                    /*Single.fromCallable(() -> populatePullSuccessBackground(response, context))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe();*/
                    Single.fromCallable(() -> populatePullSuccessBackground(response, context))
                            .subscribeOn(Schedulers.io())
                            .observeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(result -> {
                                // Handle success here, `result` is the output of `populatePullSuccessBackground`
                            }, throwable -> {
                                // Handle error here, `throwable` will contain the exception
                                Log.e("RxJavaError", "Error occurred in populatePullSuccessBackground", throwable);
                                // You can also take additional action like showing a user-friendly error message or retrying the operation
                            });
                }

                Logger.logD("End Pull request", "Ended");
                sessionManager.setLastPulledDateTime(AppConstants.dateAndTimeUtils.currentDateTimeInHome());


                //Workmanager request is used in ForeGround sync in place of this as per Intele_safe
                /*Intent intent = new Intent(IntelehealthApplication.getAppContext(), LastSyncIntentService.class);
                IntelehealthApplication.getAppContext().startService(intent);*/
                IntelehealthApplication.getAppContext().sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                        .setPackage(IntelehealthApplication.getAppContext().getPackageName())
                        .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_PULL_DATA_DONE));
            }

            @Override
            public void onFailure(Call<ResponseDTO> call, Throwable t) {
                Logger.logD("pull data", "exception" + t.getMessage());
                IntelehealthApplication.getAppContext().sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                        .setPackage(IntelehealthApplication.getAppContext().getPackageName())
                        .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_FAILED));
            }
        });
        sessionManager.setPullSyncFinished(true);
        return true;
    }

    Object populatePullSuccessBackground(Response<ResponseDTO> response, Context context) {
        boolean sync = false;

        try {
            sync = SyncData(response.body());
            CustomLog.d(TAG, "onResponse: response body : " + response.body().toString());

        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            CustomLog.e(TAG, e.getMessage());
        }
        if (sync) {
            int nextPageNo = response.body().getData().getPageNo();
            int totalCount = response.body().getData().getTotalCount();
            if (nextPageNo != -1) {
                pullData_Background(context, nextPageNo);
                return null;
            } else {
                //we are not handling
                //if(!from.equals("pres")){
                sessionManager.setPullExcutedTime(sessionManager.isPulled());
                Intent broadcast = new Intent();
                broadcast.putExtra("JOB", AppConstants.SYNC_PULL_DATA_DONE);
                broadcast.setAction(AppConstants.SYNC_NOTIFY_INTENT_ACTION);
                broadcast.setPackage(IntelehealthApplication.getAppContext().getPackageName());
                context.sendBroadcast(broadcast);
                //}

                CustomLog.d(TAG, "onResponse: sync : " + sync);
                sessionManager.setLastSyncDateTime(AppConstants.dateAndTimeUtils.getcurrentDateTime(sessionManager.getAppLanguage()));
            }
        } else {
            IntelehealthApplication.getAppContext().sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                    .setPackage(IntelehealthApplication.getAppContext().getPackageName())
                    .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_FAILED));
        }

        if (sessionManager.getTriggerNoti().equals("yes")) {
            if (response.body().getData() != null) {
                ArrayList<String> listPatientUUID = new ArrayList<String>();
                List<VisitDTO> listVisitDTO = new ArrayList<>();
                ArrayList<String> encounterVisitUUID = new ArrayList<String>();
                for (int i = 0; i < response.body().getData().getEncounterDTO().size(); i++) {
                    if (response.body().getData().getEncounterDTO().get(i)
                            .getEncounterTypeUuid().equalsIgnoreCase("bd1fbfaa-f5fb-4ebd-b75c-564506fc309e")) {
                        encounterVisitUUID.add(response.body().getData().getEncounterDTO().get(i).getVisituuid());
                    }
                }
                listVisitDTO.addAll(response.body().getData().getVisitDTO());
                for (int i = 0; i < encounterVisitUUID.size(); i++) {
                    for (int j = 0; j < listVisitDTO.size(); j++) {
                        if (encounterVisitUUID.get(i).equalsIgnoreCase(listVisitDTO.get(j).getUuid())) {
                            listPatientUUID.add(listVisitDTO.get(j).getPatientuuid());
                        }
                    }
                }

                if (listPatientUUID.size() > 0) {
                    triggerVisitNotification(listPatientUUID);
                }
            }
        } else {
            sessionManager.setTriggerNoti("yes");
        }
        return null;
    }


    public boolean pullData(final Context context, String fromActivity, int pageNo) {

        mDbHelper = new InteleHealthDatabaseHelper(context);
        if (db == null) {
            db = mDbHelper.getWriteDb();
        }
        sessionManager = new SessionManager(context);
        String encoded = sessionManager.getEncoded();
        String oldDate = sessionManager.getPullExcutedTime();
        String url = BuildConfig.SERVER_URL + "/EMR-Middleware/webapi/pull/pulldata/"
                + sessionManager.getLocationUuid() + "/" + sessionManager.getPullExcutedTime() +
                "/" + pageNo + "/" + AppConstants.PAGE_LIMIT;
//        String url =  sessionManager.getServerUrl() + "/pulldata/" + sessionManager.getLocationUuid() + "/" + sessionManager.getPullExcutedTime();
        Call<ResponseDTO> middleWarePullResponseCall = AppConstants.apiInterface.RESPONSE_DTO_CALL(url, "Basic " + encoded);
        Logger.logD("Start pull request", "Started");
        middleWarePullResponseCall.enqueue(new Callback<ResponseDTO>() {
            @Override
            public void onResponse(Call<ResponseDTO> call, Response<ResponseDTO> response) {
//                AppConstants.notificationUtils.showNotifications("Sync background", "Sync in progress..", 1, IntelehealthApplication.getAppContext());
                if (response.body() != null && response.body().getData() != null) {
                    sessionManager.setPulled(response.body().getData().getPullexecutedtime());
                }
                if (response.isSuccessful()) {

                    // SyncDAO syncDAO = new SyncDAO();
                    boolean sync = false;
                    try {
                        sync = SyncData(response.body());
                    } catch (DAOException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                        CustomLog.e(TAG, e.getMessage());
                    }
                    if (sync) {
                        int nextPageNo = response.body().getData().getPageNo();
                        int totalCount = response.body().getData().getTotalCount();
                        int percentage = 0; // this should be only in initialSync....

                        if (nextPageNo != -1) {
                            percentage = (int) Math.round(nextPageNo * AppConstants.PAGE_LIMIT * 100.0 / totalCount);
                            Logger.logD(PULL_ISSUE, "percentage: " + percentage);
                            setProgress(percentage);
                            pullData(context, fromActivity, nextPageNo);
                            return;
                        } else {
                            percentage = 100;
                            sessionManager.setPullExcutedTime(sessionManager.isPulled());
                            Logger.logD(PULL_ISSUE, "percentage page -1: " + percentage);
                            setProgress(percentage);
                            Intent broadcast = new Intent();
                            broadcast.putExtra("JOB", AppConstants.SYNC_PULL_DATA_DONE);
                            broadcast.setAction(AppConstants.SYNC_NOTIFY_INTENT_ACTION);
                            broadcast.setPackage(IntelehealthApplication.getAppContext().getPackageName());
                            context.sendBroadcast(broadcast);
                            sessionManager.setLastSyncDateTime(AppConstants.dateAndTimeUtils.getcurrentDateTime(sessionManager.getAppLanguage()));
                            if (fromActivity.equalsIgnoreCase("home")) {
                                //Toast.makeText(context, context.getResources().getString(R.string.successfully_synced), Toast.LENGTH_LONG).show();
                            } else if (fromActivity.equalsIgnoreCase("visitSummary")) {
                                //Toast.makeText(context, context.getResources().getString(R.string.visit_uploaded_successfully), Toast.LENGTH_LONG).show();
                            } else if (fromActivity.equalsIgnoreCase("downloadPrescription")) {
//                            AppConstants.notificationUtils.DownloadDone(context.getString(R.string.download_from_doctor), context.getString(R.string.prescription_downloaded), 3, context);
//                            Toast.makeText(context, context.getString(R.string.prescription_downloaded), Toast.LENGTH_LONG).show();
                            }
//                        else {
//                            Toast.makeText(context, context.getString(R.string.successfully_synced), Toast.LENGTH_LONG).show();
//                        }
                        }
                    } else {
//                        AppConstants.notificationUtils.DownloadDone(context.getString(R.string.sync), context.getString(R.string.failed_synced), 1, IntelehealthApplication.getAppContext());

                        if (fromActivity.equalsIgnoreCase("home")) {
                            // Toast.makeText(context, context.getString(R.string.failed_synced), Toast.LENGTH_LONG).show();
                        } else if (fromActivity.equalsIgnoreCase("visitSummary")) {
                            //Toast.makeText(context, context.getString(R.string.visit_not_uploaded), Toast.LENGTH_LONG).show();
                        } else if (fromActivity.equalsIgnoreCase("downloadPrescription")) {
                            // Toast.makeText(context, context.getString(R.string.prescription_not_downloaded_check_internet), Toast.LENGTH_LONG).show();
                        }
//                        else {
//                            Toast.makeText(context, context.getString(R.string.failed_synced), Toast.LENGTH_LONG).show();
//                        }
                        IntelehealthApplication.getAppContext().sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                                .setPackage(IntelehealthApplication.getAppContext().getPackageName())
                                .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_FAILED));
                    }

                    if (sessionManager.getTriggerNoti().equals("yes")) {
                        if (response.body().getData() != null) {
                            ArrayList<String> listPatientUUID = new ArrayList<String>();
                            List<VisitDTO> listVisitDTO = new ArrayList<>();
                            ArrayList<String> encounterVisitUUID = new ArrayList<String>();

                            for (int i = 0; i < response.body().getData().getEncounterDTO().size(); i++) {
                                if (response.body().getData().getEncounterDTO().get(i)
                                        .getEncounterTypeUuid().equalsIgnoreCase("bd1fbfaa-f5fb-4ebd-b75c-564506fc309e")) {
                                    encounterVisitUUID.add(response.body().getData().getEncounterDTO().get(i).getVisituuid());
                                }
                            }
                            listVisitDTO.addAll(response.body().getData().getVisitDTO());
                            for (int i = 0; i < encounterVisitUUID.size(); i++) {
                                for (int j = 0; j < listVisitDTO.size(); j++) {
                                    if (encounterVisitUUID.get(i).equalsIgnoreCase(listVisitDTO.get(j).getUuid())) {
                                        listPatientUUID.add(listVisitDTO.get(j).getPatientuuid());
                                    }
                                }
                            }

                            if (listPatientUUID.size() > 0) {
                                triggerVisitNotification(listPatientUUID);
                            }
                        }
                    } else {
                        sessionManager.setTriggerNoti("yes");
                    }
                }

                Logger.logD("End Pull request", "Ended");
                sessionManager.setLastPulledDateTime(AppConstants.dateAndTimeUtils.currentDateTimeInHome());

                //Workmanager request is used in ForeGround sync in place of this as per the intele_Safe
               /* Intent intent = new Intent(IntelehealthApplication.getAppContext(), LastSyncIntentService.class);
                IntelehealthApplication.getAppContext().startService(intent);*/
                IntelehealthApplication.getAppContext().sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                        .setPackage(IntelehealthApplication.getAppContext().getPackageName())
                        .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_PULL_DATA_DONE));
            }

            @Override
            public void onFailure(Call<ResponseDTO> call, Throwable t) {
                Logger.logD("pull data", "exception" + t.getMessage());
                IntelehealthApplication.getAppContext().sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                        .setPackage(IntelehealthApplication.getAppContext().getPackageName())
                        .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_FAILED));
            }
        });
        sessionManager.setPullSyncFinished(true);
        return true;
    }


    /**
     * this method for syncing data first time with background service
     * we starting background service here
     *
     * @param context
     * @param fromActivity
     * @return
     */
    public boolean pullDataBackgroundService(final Context context, String fromActivity, int pageNo) {

        mDbHelper = new InteleHealthDatabaseHelper(context);
        if (db == null) {
            db = mDbHelper.getWriteDb();
        }
        sessionManager = new SessionManager(context);
        String encoded = sessionManager.getEncoded();
        String oldDate = sessionManager.getPullExcutedTime();
        String url = BuildConfig.SERVER_URL + "/EMR-Middleware/webapi/pull/pulldata/" + sessionManager.getLocationUuid() + "/" + sessionManager.getPullExcutedTime() +
                "/" + pageNo + "/" + AppConstants.PAGE_LIMIT;
        ;
//        String url =  sessionManager.getServerUrl() + "/pulldata/" + sessionManager.getLocationUuid() + "/" + sessionManager.getPullExcutedTime();
        Logger.logD(PULL_ISSUE, url);
        Call<ResponseDTO> middleWarePullResponseCall = AppConstants.apiInterface.RESPONSE_DTO_CALL(url, "Basic " + encoded);
        Logger.logD("Start pull request", "Started");
        middleWarePullResponseCall.enqueue(new Callback<ResponseDTO>() {
            @Override
            public void onResponse(Call<ResponseDTO> call, Response<ResponseDTO> response) {
//                AppConstants.notificationUtils.showNotifications("Sync background", "Sync in progress..", 1, IntelehealthApplication.getAppContext());
                if (response.body() != null && response.body().getData() != null) {
                    sessionManager.setPulled(response.body().getData().getPullexecutedtime());
                }
                if (response.isSuccessful()) {
                    ResponseDTO responseDTO = response.body();
                    //Large amount of data passing not possible with intent
                    //we passing data through static function
                    InitialSyncIntentService.setData(responseDTO);

                    //Inserting huge data to database is a heavy operation
                    //that's why we using service here for initial data push
                    Intent intent = new Intent(context, InitialSyncIntentService.class);
                    intent.putExtra("from", fromActivity);
                    context.startService(intent);


                    if (sessionManager.getTriggerNoti().equals("yes")) {
                        if (response.body().getData() != null) {
                            ArrayList<String> listPatientUUID = new ArrayList<String>();
                            List<VisitDTO> listVisitDTO = new ArrayList<>();
                            ArrayList<String> encounterVisitUUID = new ArrayList<String>();

                            for (int i = 0; i < response.body().getData().getEncounterDTO().size(); i++) {
                                if (response.body().getData().getEncounterDTO().get(i)
                                        .getEncounterTypeUuid().equalsIgnoreCase("bd1fbfaa-f5fb-4ebd-b75c-564506fc309e")) {
                                    encounterVisitUUID.add(response.body().getData().getEncounterDTO().get(i).getVisituuid());
                                }
                            }
                            listVisitDTO.addAll(response.body().getData().getVisitDTO());
                            for (int i = 0; i < encounterVisitUUID.size(); i++) {
                                for (int j = 0; j < listVisitDTO.size(); j++) {
                                    if (encounterVisitUUID.get(i).equalsIgnoreCase(listVisitDTO.get(j).getUuid())) {
                                        listPatientUUID.add(listVisitDTO.get(j).getPatientuuid());
                                    }
                                }
                            }

                            if (listPatientUUID.size() > 0) {
                                triggerVisitNotification(listPatientUUID);
                            }
                        }
                    } else {
                        sessionManager.setTriggerNoti("yes");
                    }
                }

                Logger.logD("End Pull request", "Ended");
                sessionManager.setLastPulledDateTime(AppConstants.dateAndTimeUtils.currentDateTimeInHome());

                //Workmanager request is used in ForeGround sync in place of this as per the intele_Safe
               /* Intent intent = new Intent(IntelehealthApplication.getAppContext(), LastSyncIntentService.class);
                IntelehealthApplication.getAppContext().startService(intent);*/
                IntelehealthApplication.getAppContext().sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                        .setPackage(IntelehealthApplication.getAppContext().getPackageName())
                        .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_PULL_DATA_DONE));
            }

            @Override
            public void onFailure(Call<ResponseDTO> call, Throwable t) {
                Logger.logD("pull data", "exception" + t.getMessage());
                IntelehealthApplication.getAppContext().sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                        .setPackage(IntelehealthApplication.getAppContext().getPackageName())
                        .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_FAILED));
            }
        });
        sessionManager.setPullSyncFinished(true);
        return true;
    }

    public void setLocale(String appLanguage) {
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        IntelehealthApplication.getAppContext().getResources().updateConfiguration(config, IntelehealthApplication.getAppContext().getResources().getDisplayMetrics());
    }

    private void triggerVisitNotification(ArrayList<String> listPatientUUID) {
        List<ActivePatientModel> activePatientList = new ArrayList<>();
        getPatients(activePatientList);

        if (listPatientUUID != null) {
            for (int i = 0; i < listPatientUUID.size(); i++) {
                for (int j = 0; j < activePatientList.size(); j++) {
                    if (listPatientUUID.get(i).equalsIgnoreCase(activePatientList.get(j).getPatientuuid())) {
                        CustomLog.e("GET-ID", "" + NotificationID.getID());
                        AppConstants.notificationUtils.DownloadDone(IntelehealthApplication.getAppContext().getResources().getString(R.string.patient) + " " +
                                        activePatientList.get(j).getFirst_name() + " " +
                                        activePatientList.get(j).getLast_name(),
                                IntelehealthApplication.getAppContext().getString(R.string.has_a_new_prescription),
                                NotificationID.getID(), IntelehealthApplication.getAppContext());
                    }
                }
            }
        }
    }

    private void getPatients(List<ActivePatientModel> activePatientList) {

        String query =
                "SELECT   a.uuid, a.patientuuid, a.startdate, a.enddate, b.first_name, b.middle_name, b.last_name, b.date_of_birth,b.openmrs_id  " +
                        "FROM tbl_visit a, tbl_patient b " +
                        "WHERE a.patientuuid = b.uuid " +
                        "AND a.enddate is NULL OR a.enddate='' GROUP BY a.uuid ORDER BY a.startdate ASC";
        final Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    activePatientList.add(new ActivePatientModel(
                            cursor.getString(cursor.getColumnIndexOrThrow("uuid")),
                            cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")),
                            cursor.getString(cursor.getColumnIndexOrThrow("startdate")),
                            cursor.getString(cursor.getColumnIndexOrThrow("enddate")),
                            cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("first_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("middle_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")),
                            "",
                            ""
                    ));
                } while (cursor.moveToNext());
            }
        }
        cursor.close();
    }

    public boolean pushDataApi() {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        PatientsDAO patientsDAO = new PatientsDAO();
        VisitsDAO visitsDAO = new VisitsDAO();
        EncounterDAO encounterDAO = new EncounterDAO();
        ProviderDAO providerDAO = new ProviderDAO();
        AppointmentDAO appointmentDAO = new AppointmentDAO();

        PushRequestApiCall pushRequestApiCall;
        PatientsFrameJson patientsFrameJson = new PatientsFrameJson();
        pushRequestApiCall = patientsFrameJson.frameJson();
        final boolean[] isSucess = {true};
        String encoded = sessionManager.getEncoded();
        Gson gson = new Gson();
        CustomLog.d(TAG, "pushDataApi: encoded : " + encoded);
        Logger.logD(TAG, "push request model" + gson.toJson(pushRequestApiCall));
        CustomLog.e(TAG, "push request model" + gson.toJson(pushRequestApiCall));
        String url = BuildConfig.SERVER_URL + "/EMR-Middleware/webapi/push/pushdata";
        Logger.logD(TAG, "push request url - " + url);
        Logger.logD(TAG, "push request encoded - " + encoded);
        if (!pushRequestApiCall.getVisits().isEmpty()
                || !pushRequestApiCall.getPersons().isEmpty()
                || !pushRequestApiCall.getPatients().isEmpty()
                || !pushRequestApiCall.getEncounters().isEmpty()
                || !pushRequestApiCall.getProviders().isEmpty()
                || !pushRequestApiCall.getAppointments().isEmpty()) {
            Single<PushResponseApiCall> pushResponseApiCallObservable = AppConstants.apiInterface.PUSH_RESPONSE_API_CALL_OBSERVABLE(url, "Basic " + encoded, pushRequestApiCall);
            pushResponseApiCallObservable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableSingleObserver<PushResponseApiCall>() {
                        @Override
                        public void onSuccess(PushResponseApiCall pushResponseApiCall) {
                            CustomLog.d(TAG, "onSuccess: in push api response");
                            Logger.logD(TAG, "success" + pushResponseApiCall);
                            try {
                                for (int i = 0; i < pushResponseApiCall.getData().getPatientlist().size(); i++) {
                                    try {
                                        patientsDAO.updateOpemmrsId(pushResponseApiCall.getData().getPatientlist().get(i).getOpenmrsId(), pushResponseApiCall.getData().getPatientlist().get(i).getSyncd().toString(), pushResponseApiCall.getData().getPatientlist().get(i).getUuid());
                                        CustomLog.d("SYNC", "ProvUUDI" + pushResponseApiCall.getData().getPatientlist().get(i).getUuid());
                                    } catch (DAOException e) {
                                        FirebaseCrashlytics.getInstance().recordException(e);
                                        CustomLog.e(TAG, e.getMessage());
                                    }
                                }

                                for (int i = 0; i < pushResponseApiCall.getData().getVisitlist().size(); i++) {
                                    try {
                                        visitsDAO.updateVisitSync(pushResponseApiCall.getData().getVisitlist().get(i).getUuid(), pushResponseApiCall.getData().getVisitlist().get(i).getSyncd().toString());
                                    } catch (DAOException e) {
                                        FirebaseCrashlytics.getInstance().recordException(e);
                                        CustomLog.e(TAG, e.getMessage());
                                    }
                                }

                                for (int i = 0; i < pushResponseApiCall.getData().getEncounterlist().size(); i++) {
                                    try {
                                        encounterDAO.updateEncounterSync(pushResponseApiCall.getData().getEncounterlist().get(i).getSyncd().toString(), pushResponseApiCall.getData().getEncounterlist().get(i).getUuid());
                                        CustomLog.d("SYNC", "Encounter Data: " + pushResponseApiCall.getData().getEncounterlist().get(i).toString());
                                    } catch (DAOException e) {
                                        FirebaseCrashlytics.getInstance().recordException(e);
                                        CustomLog.e(TAG, e.getMessage());
                                    }
                                }

                                for (int i = 0; i < pushResponseApiCall.getData().getAppointmentList().size(); i++) {
                                    try {
                                        String sync = pushResponseApiCall.getData().getAppointmentList().get(i).getSync();
                                        String visitUuid = pushResponseApiCall.getData().getAppointmentList().get(i).getVisitUuid();
                                        appointmentDAO.updateAppointmentSync(visitUuid, sync);
                                    } catch (DAOException exception) {
                                        FirebaseCrashlytics.getInstance().recordException(exception);
                                        CustomLog.e(TAG, exception.getMessage());
                                    }
                                }

                                //ui2.0 for provider profile details
                                if (pushResponseApiCall.getData().getProviderlist() != null) {
                                    CustomLog.d(TAG, "onSuccess: getProviderlist : " + pushResponseApiCall.getData().getProviderlist().size());
                                    for (int i = 0; i < pushResponseApiCall.getData().getProviderlist().size(); i++) {
                                        try {
                                            providerDAO.updateProviderProfileSync(pushResponseApiCall.getData().getProviderlist().get(i).getUuid(), "true");
                                            CustomLog.d("SYNC", "profile Data: " + pushResponseApiCall.getData().getProviderlist().get(i).toString());
                                        } catch (DAOException e) {
                                            e.printStackTrace();
                                            FirebaseCrashlytics.getInstance().recordException(e);
                                            CustomLog.e(TAG, e.getMessage());
                                        }
                                    }
                                }

                                isSucess[0] = true;
                                sessionManager.setSyncFinished(true);

                                Intent broadcast = new Intent();
                                broadcast.putExtra("JOB", AppConstants.SYNC_PUSH_DATA_DONE);
                                broadcast.setAction(AppConstants.SYNC_NOTIFY_INTENT_ACTION);
                                broadcast.setPackage(IntelehealthApplication.getAppContext().getPackageName());
                                IntelehealthApplication.getAppContext().sendBroadcast(broadcast);

                            } catch (Exception e) {
                                e.printStackTrace();
                                CustomLog.e(TAG, e.getMessage());
                            }

                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.logD(TAG, "Onerror " + e.getMessage());
                            e.printStackTrace();
                            isSucess[0] = false;
                            IntelehealthApplication.getAppContext().sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                                    .setPackage(IntelehealthApplication.getAppContext().getPackageName())
                                    .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_FAILED));
                        }
                    });
            sessionManager.setPullSyncFinished(true);
            IntelehealthApplication.getAppContext().sendBroadcast(new Intent(AppConstants.SYNC_INTENT_ACTION)
                    .setPackage(IntelehealthApplication.getAppContext().getPackageName())
                    .putExtra(AppConstants.SYNC_INTENT_DATA_KEY, AppConstants.SYNC_PUSH_DATA_DONE));
        }

        return isSucess[0];
    }

    private void CalculateAgoTime(Context context) {
        String finalTime = "";

        String syncTime = sessionManager.getLastSyncDateTime();

        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
        ParsePosition pos = new ParsePosition(0);
        long then = formatter.parse(syncTime, pos).getTime();
        long now = new Date().getTime();

        long seconds = (now - then) / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        String time = "";
        long num = 0;
        if (days > 0) {
            num = days;
            time = days + " " + context.getString(R.string.day);
        } else if (hours > 0) {
            num = hours;
            time = hours + " " + context.getString(R.string.hour);
        } else if (minutes >= 0) {
            num = minutes;
            time = minutes + " " + context.getString(R.string.minute);
        }
//      <For seconds>
//      else {
//            num = seconds;
//            time = seconds + " second";
//      }
        if (num > 1) {
            time += context.getString(R.string.s);
        }
        finalTime = time + " " + context.getString(R.string.ago);

        sessionManager.setLastTimeAgo(finalTime);
    }


    public static void setProgress(int progress) {
        liveDataSync.updateProgress(progress);
    }

    public static SyncProgress getSyncProgress_LiveData() {
        return liveDataSync;
    }
}
