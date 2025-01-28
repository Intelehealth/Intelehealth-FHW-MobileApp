package org.intelehealth.app.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import org.intelehealth.app.ayu.visit.vital.VitalPreference;
import org.intelehealth.app.utilities.CustomLog;

import org.intelehealth.app.BuildConfig;

import java.util.Set;

public class SessionManager {
    private static final String VITAL_KEY ="vita_pref" ;
    // Shared preferences file name
    public static SessionManager instance;
    private static final String PREF_NAME = "Intelehealth";
    private static final String VISIT_ID = "visitID";
    private static final String BASE_URL = "base_url";
    private static final String ENCODED = "encoded";
    private static final String PULL_EXECUTED_TIME = "pullexecutedtime";
    private static final String KEY_PREF_SETUP_COMPLETE = "setup";
    private static final String KEY_BLACKOUT = "blackout";
    private static final String APP_LANGUAGE = "Language";
    private static final String SESSION_ID = "sessionid";
    private static final String CREATOR_ID = "creatorid";
    private static final String PROVIDER_ID = "providerid";
    private static final String CHWNAME = "chwname";
    private static final String KEY_PREF_SERVER_URL_REST = "serverurl";
    private static final String KEY_PREF_SERVER_URL = "url";
    private static final String KEY_PREF_SERVER_URL_BASE = "serverurlbase";
    private static final String KEY_PREF_LOCATION_UUID = "locationuuid";
    private static final String KEY_PREF_LOCATION_NAME = "locationname";
    private static final String KEY_PREF_LOCATION_DESCRIPTION = "locationdesc";
    private static final String LICENSE_KEY = "licensekey";
    private static final String DATE = "date";
    private static final String TIME = "time";
    private static final String FIRST_TIME_SYNC_EXECUTE = "FIRST_TIME_SYNC_EXECUTE";
    private static final String LICENSE = "license";
    private static final String RETURNING = "returning";
    private static final String PULLED = "pulled";
    private static final String NEW_DATABASE = "newDatabase";
    private static final String FIRST_TIME_LAUNCHED = "firsttimelaunched";
    private static final String SYNC_FINISHED = "syncfinished";
    private static final String LAST_PULLED_EXECUTED_DATE_TIME = "lastpulledexecutedtimeanddate";
    private static final String LAST_SYNC_SUCCESS_DATE_TIME = "lastsyncsuccessdatetime";
    private static final String LAST_SYNC_TIME_AGO = "LAST_SYNC_TIME_AGO";
    private static final String PULL_SYNC_FINISHED = "pullsyncfinished";
    private static final String PUSH_SYNC_FINISHED = "pushsyncfinished";
    private static final String MIND_MAP_SERVER_URL = "mindmapurl";
    private static final String RETURNING_USER = "returninguser";
    private static final String VISIT_SUMMARY = "visit_summary";
    private static final String EXAM = "exam_";
    private static final String MIGRATION_KEY = "migrationkey";
    private static final String TRIGGER_NOTI = "TRIGGER_NOTI";
    private static final String OFFLINE_OPENMRSID = "OFFLINE_OPENMRSID";
    private static final String CURRENT_LANG = "CURRENT_LANG";
    private static final String IS_LOGOUT = "IS_LOGOUT";
    private static final String HOUSEHOLD_UUID = "HOUSEHOLD_UUID";
    private static final String IS_FIRST_TIME_LAUNCH = "IS_FIRST_TIME_LAUNCH";
    public static final String PREVIOUS_SEARCH_QUERY = "PREVIOUS_SEARCH_QUERY";
    public static final String FIRST_PROVIDER_LOGIN_TIME = "FIRST_LOGIN_TIME";
    private static final String ENABLE_APP_LOCK = "ENABLE_APP_LOCK";
    // prefix for visit edit cache
    public static final String CHIEF_COMPLAIN_LIST = "CHIEF_COMPLAIN_LIST_";
    public static final String CHIEF_COMPLAIN_QUESTION_NODE = "CHIEF_COMPLAIN_QUESTION_NODE_";
    public static final String PHY_EXAM = "PHY_EXAM_";
    public static final String PATIENT_HISTORY = "PATIENT_HISTORY_";
    public static final String FAMILY_HISTORY = "FAMILY_HISTORY_";

    private static final String JWT_AUTH_TOKEN = "JWT_AUTH_TOKEN";

    private static final String ACTIVITY_RESULT_APPOINTMENT = "ACTIVITY_RESULT_APPOINTMENT";

    public static final String PRIVACY_POLICY = "PRIVACY_POLICY";
    public static final String TERMS_OF_USE = "TERMS_OF_USE";
    public static final String PERSONAL_DATA_PROCESSING_POLICY = "PERSONAL_DATA_PROCESSING_POLICY";
    private static final String CUSTOM_LOG_VERSION = "custom_log_version";
    private static final String DEFAULT_PULL_START_TIME = "2006-08-22 22:21:48";
//    private static final int DAYS_DELTA_FOR_SMOOTH_SYNC = -300;
//    private static final String DEFAULT_PULL_START_TIME = DateAndTimeUtils.convertDateObjectToString(DateUtils.addDays(new Date(), DAYS_DELTA_FOR_SMOOTH_SYNC), "yyyy-MM-dd HH:mm:ss");  ;


    // LogCat tag
    private static String TAG = SessionManager.class.getSimpleName();
    // Shared Preferences
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context _context;
    // Shared pref mode
    private int PRIVATE_MODE = 0;

    //UI2.0
    private static final String IS_LOGGED_IN = "IS_LOGGED_IN";


    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public static SessionManager getInstance(Context context) {
        if (instance == null) instance = new SessionManager(context);
        return instance;
    }

    public String getPreviousSearchQuery() {
        return pref.getString(PREVIOUS_SEARCH_QUERY, "");
    }

    public void setPreviousSearchQuery(String query) {
        editor.putString(PREVIOUS_SEARCH_QUERY, query);
        editor.commit();
    }

    public String getVisitId() {
        return pref.getString(VISIT_ID, "");
    }

    public void setVisitId(String token) {
        editor.putString(VISIT_ID, token);
        editor.commit();
    }

    public String getBaseUrl() {
        return pref.getString(BASE_URL, "https://openmrs.intelehealth.io");
    }

    public void setBaseUrl(String baseUrl) {
        editor.putString(BASE_URL, baseUrl);
        editor.commit();
    }

    public String getEncoded() {
        return pref.getString(ENCODED, "");
    }

    public void setEncoded(String encoded) {
        editor.putString(ENCODED, encoded);
        editor.commit();
    }

    public void setEnableAppLock(boolean isFirstTime) {
        editor.putBoolean(ENABLE_APP_LOCK, isFirstTime);
        editor.commit();
    }

    public boolean isEnableAppLock() {
        return pref.getBoolean(ENABLE_APP_LOCK, false);
    }

    public String getPullExcutedTime() {
        return pref.getString(PULL_EXECUTED_TIME, DEFAULT_PULL_START_TIME);
    }

    public void setPullExcutedTime(String pullExcutedTime) {
        editor.putString(PULL_EXECUTED_TIME, pullExcutedTime);
        editor.commit();
    }

    public String getAppLanguage() {
        return pref.getString(APP_LANGUAGE, "en");
    }

    public void setAppLanguage(String pullExcutedTime) {
        editor.putString(APP_LANGUAGE, pullExcutedTime);
        editor.commit();
    }

    public boolean isSetupComplete() {
        return pref.getBoolean(KEY_PREF_SETUP_COMPLETE, false);
    }

    public void setSetupComplete(Boolean setupComplete) {
        editor.putBoolean(KEY_PREF_SETUP_COMPLETE, setupComplete);
        editor.commit();
    }

    public boolean isBlackout() {
        return pref.getBoolean(KEY_BLACKOUT, false);
    }

    public void setBlackout(Boolean blackout) {
        editor.putBoolean(KEY_BLACKOUT, blackout);
        editor.commit();
    }

    public String getSessionID() {
        return pref.getString(SESSION_ID, "");
    }

    public void setSessionID(String sessionID) {
        editor.putString(SESSION_ID, sessionID);
        editor.commit();
    }

    public String getProviderID() {
        return pref.getString(PROVIDER_ID, "");
    }

    public void setProviderID(String providerID) {
        editor.putString(PROVIDER_ID, providerID);
        editor.commit();
    }

    public String getCreatorID() {
        return pref.getString(CREATOR_ID, "");
    }

    public void setCreatorID(String creatorID) {
        editor.putString(CREATOR_ID, creatorID);
        editor.commit();
    }

    public String getChwname() {
        return pref.getString(CHWNAME, "");
    }

    public void setChwname(String chwname) {
        editor.putString(CHWNAME, chwname);
        editor.commit();
    }

    public String getLocationName() {
        return pref.getString(KEY_PREF_LOCATION_NAME, "");
    }

    public void setLocationName(String locationName) {
        editor.putString(KEY_PREF_LOCATION_NAME, locationName);
        editor.commit();
    }

    public String getLocationUuid() {
        return pref.getString(KEY_PREF_LOCATION_UUID, "");
    }

    public void setLocationUuid(String locationUuid) {
        editor.putString(KEY_PREF_LOCATION_UUID, locationUuid);
        editor.commit();
    }

    public String getLocationDescription() {
        return pref.getString(KEY_PREF_LOCATION_DESCRIPTION, "");
    }

    public void setLocationDescription(String locationDescription) {
        editor.putString(KEY_PREF_LOCATION_DESCRIPTION, locationDescription);
        editor.commit();
    }

    public String getServerUrl() {
        //added the default server url instead of ""
        //because some times crash happens for the empty string
        return pref.getString(KEY_PREF_SERVER_URL, BuildConfig.SERVER_URL);
    }

    public void setServerUrl(String serverUrl) {
        editor.putString(KEY_PREF_SERVER_URL, serverUrl);
        editor.commit();
    }

    public String getHouseholdUuid() {
        return pref.getString(HOUSEHOLD_UUID, "");
    }

    public void setHouseholdUuid(String UUID) {
        editor.putString(HOUSEHOLD_UUID, UUID);
        editor.commit();
    }

    public String getTriggerNoti() {
        return pref.getString(TRIGGER_NOTI, "");
    }

    public void setTriggerNoti(String flag) {
        editor.putString(TRIGGER_NOTI, flag);
        editor.commit();
    }

    public String getOfllineOpenMRSID() {
        return pref.getString(OFFLINE_OPENMRSID, "");
    }

    public void setOfllineOpenMRSID(String id) {
        editor.putString(OFFLINE_OPENMRSID, id);
        editor.commit();
    }

    public String getCurrentLang() {
        return pref.getString(CURRENT_LANG, "en");
    }  //setting default language as english

    public void setCurrentLang(String lang) {
        editor.putString(CURRENT_LANG, lang);
        editor.commit();
    }

    public String getServerUrlRest() {
        return pref.getString(KEY_PREF_SERVER_URL_REST, "");
    }

    public void setServerUrlRest(String serverUrlRest) {
        editor.putString(KEY_PREF_SERVER_URL_REST, serverUrlRest);
        editor.commit();
    }

    public String getServerUrlBase() {
        return pref.getString(KEY_PREF_SERVER_URL_BASE, "");
    }

    public void setServerUrlBase(String serverUrlBase) {
        editor.putString(KEY_PREF_SERVER_URL_BASE, serverUrlBase);
        editor.commit();
    }

    public String getLicenseKey() {
        return pref.getString(LICENSE_KEY, "");
    }

    public void setLicenseKey(String licenseKey) {
        CustomLog.e("MindMapURL", "setLicenseKey - " + licenseKey);
        editor.putString(LICENSE_KEY, licenseKey);
        editor.commit();
    }

    public void deleteLicensekey() {
        CustomLog.e("MindMapURL", "deleteLicensekey - ");
        editor.remove(LICENSE_KEY);
        editor.commit();
    }

    public String getDate() {
        return pref.getString(DATE, "");
    }

    public void setDate(String date) {
        editor.putString(DATE, date);
        editor.commit();
    }

    public String getTime() {
        return pref.getString(TIME, "");
    }

    public void setTime(String time) {
        editor.putString(TIME, time);
        editor.commit();
    }

    public boolean valueContains(String value) {
        boolean hasvalue = false;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(_context);
        hasvalue = sharedPreferences.contains(value);
        return hasvalue;
    }

    public boolean isFirstTimeSyncExcuted() {
        return pref.getBoolean(FIRST_TIME_SYNC_EXECUTE, true);
    }

    public void setFirstTimeSyncExecute(Boolean firstTimeSyncExecute1) {
        editor.putBoolean(FIRST_TIME_SYNC_EXECUTE, firstTimeSyncExecute1);
        editor.commit();
    }

    public boolean isReturning() {
        return pref.getBoolean(RETURNING, false);
    }

    public void setReturning(Boolean returning) {
        editor.putBoolean(RETURNING, returning);
        editor.commit();
    }

    public String isPulled() {
        return pref.getString(PULLED, DEFAULT_PULL_START_TIME);
    }  //getting the sync value  and time and saving in the sharedpref

    public void setPulled(String pulled) {
        editor.putString(PULLED, pulled);
        editor.commit();
    }

    public String getNewDatabase() {
        return pref.getString(NEW_DATABASE, "");
    }  //getting the sync value  and time and saving in the sharedpref

    public void setNewDatabase(String newDatabase) {
        editor.putString(NEW_DATABASE, newDatabase);
        editor.commit();
    }

    public boolean newDatabaseContains(String value) {
        boolean hasvalue = false;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(_context);
        hasvalue = sharedPreferences.contains(value);
        return hasvalue;
    }

    public boolean isFirstTimeLaunched() {
        return pref.getBoolean(FIRST_TIME_LAUNCHED, true);
    }

    public void setFirstTimeLaunched(boolean firstTimeLaunched) {
        editor.putBoolean(FIRST_TIME_LAUNCHED, firstTimeLaunched);
        editor.commit();
    }


    public boolean isSyncFinished() {
        return pref.getBoolean(SYNC_FINISHED, false);
    }

    public void setSyncFinished(Boolean syncFinished) {
        editor.putBoolean(SYNC_FINISHED, syncFinished);
        editor.commit();
    }

    public String getLastPulledDateTime() {
        return pref.getString(LAST_PULLED_EXECUTED_DATE_TIME, "01 Jan 2019 12:15:26");
    }  //getting the sync value  and time and saving in the sharedpref

    public void setLastPulledDateTime(String lastPulledDateTime) {
        editor.putString(LAST_PULLED_EXECUTED_DATE_TIME, lastPulledDateTime);
        editor.commit();
    }

    public String
    getLastSyncDateTime() {
        return pref.getString(LAST_SYNC_SUCCESS_DATE_TIME, "- - - -");
    }  //getting the sync value  and time and saving in the sharedpref

    public void setLastSyncDateTime(String lastPulledDateTime) {
        editor.putString(LAST_SYNC_SUCCESS_DATE_TIME, lastPulledDateTime);
        editor.commit();
    }

    public String getLastTimeAgo() {
        return pref.getString(LAST_SYNC_TIME_AGO, "");
    }  //getting the sync value  and time and saving in the sharedpref

    public void setLastTimeAgo(String lastPulledDateTime) {
        editor.putString(LAST_SYNC_TIME_AGO, lastPulledDateTime);
        editor.commit();
    }

    public boolean isPullSyncFinished() {
        return pref.getBoolean(PULL_SYNC_FINISHED, false);
    }

    public void setPullSyncFinished(Boolean syncFinished) {
        editor.putBoolean(PULL_SYNC_FINISHED, syncFinished);
        editor.commit();
    }

    public boolean isPushSyncFinished() {
        return pref.getBoolean(PUSH_SYNC_FINISHED, false);
    }

    public void setPushSyncFinished(Boolean syncFinished) {
        editor.putBoolean(PUSH_SYNC_FINISHED, syncFinished);
        editor.commit();
    }

    public String getMindMapServerUrl() {
        return pref.getString(MIND_MAP_SERVER_URL, "https://mindmaps2.intelehealth.io");
    }  //getting the sync value  and time and saving in the sharedpref

    public void setMindMapServerUrl(String mindMapServerUrl) {
        editor.putString(MIND_MAP_SERVER_URL, mindMapServerUrl);
        editor.commit();
    }

    public boolean isReturningUser() {
        return pref.getBoolean(RETURNING_USER, false);
    }

    public void setReturningUser(Boolean returningUser) {
        editor.putBoolean(RETURNING_USER, returningUser);
        editor.commit();
    }

    public boolean isLogout() {
        return pref.getBoolean(IS_LOGOUT, false);
    }

    public void setLogout(Boolean isLogout) {
        editor.putBoolean(IS_LOGOUT, isLogout);
        editor.commit();
    }

    public Set<String> getVisitSummary(String patientUUid) {

        return pref.getStringSet(EXAM + patientUUid, null);
    }

    public void setVisitSummary(String patientUuid, Set<String> selectedExams) {
        editor.putStringSet(EXAM + patientUuid, selectedExams);
        editor.commit();

    }

    public void removeVisitSummary(String patientUuid, String visitUuid) {
        editor.remove(EXAM + patientUuid + "_" + visitUuid);
        editor.commit();

    }

    public boolean isMigration() {
        return pref.getBoolean(MIGRATION_KEY, false);
    }

    public void setMigration(Boolean migration) {
        editor.putBoolean(MIGRATION_KEY, migration);
        editor.commit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    // UI2.0 newly added
    public void setIsLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(IS_LOGGED_IN, isLoggedIn);
        editor.commit();

    }

    public boolean getIsLoggedIn() {

        return pref.getBoolean(IS_LOGGED_IN, false);
    }

    public void setFirstProviderLoginTime(String time) {
        editor.putString(FIRST_PROVIDER_LOGIN_TIME, time);
        editor.commit();
    }

    public String getFirstProviderLoginTime() {
        return pref.getString(FIRST_PROVIDER_LOGIN_TIME, "");
    }

    // set the visit edit cache data as json string
    public void setVisitEditCache(String key, String valueJson) {
        editor.putString(key, valueJson);
        editor.commit();
    }

    // get the visit edit cache data as json string
    public String getVisitEditCache(String key) {
        return pref.getString(key, "");
    }

    public void removeVisitEditCache(String key) {
        editor.remove(key);
        editor.commit();
    }


    public void saveVitalPreference(VitalPreference vitalPreference) {
        String json = new Gson().toJson(vitalPreference);
        editor.putString(VITAL_KEY, json).apply();
    }

    public VitalPreference getVitalPreference() {
        String json = pref.getString(VITAL_KEY, null);
        if (json != null) {
            return new Gson().fromJson(json, VitalPreference.class);
        } else {
            return null;
        }
    }

    public void clearVitalPreference() {
        editor.remove(VITAL_KEY).apply();
    }

    /**
     * handling token here
     */
    // get the visit edit cache data as json string
    public void setJwtAuthToken(String token) {
        editor.putString(JWT_AUTH_TOKEN, token);
        editor.commit();
    }

    public String getJwtAuthToken() {
        return pref.getString(JWT_AUTH_TOKEN, "");
    }

    /**
     * Handling appointment result here
     *
     * @return
     */

    public Boolean getAppointmentResult() {
        return pref.getBoolean(ACTIVITY_RESULT_APPOINTMENT, false);
    }

    public void setAppointmentResult(Boolean appointmentResult) {
        editor.putBoolean(ACTIVITY_RESULT_APPOINTMENT, appointmentResult);
        editor.commit();
    }

    /**
     * setting webview html here
     * to support offline
     *
     * @param key
     * @return
     */

    public String getHtml(String key) {
        return pref.getString(key, "");
    }

    public void setHtml(String key, String html) {
        editor.putString(key, html);
        editor.commit();
    }

    /**
     * custom logger version
     */
    public void setCustomLogVersion(String version) {
        editor.putString(CUSTOM_LOG_VERSION, version);
        editor.commit();
    }

    public String getCustomLogVersion() {
        return pref.getString(CUSTOM_LOG_VERSION,"");
    }
}