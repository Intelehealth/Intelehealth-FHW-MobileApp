package org.intelehealth.app.app;


import android.content.Context;
import android.os.Environment;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.networkApiCalls.ApiClient;
import org.intelehealth.app.networkApiCalls.ApiInterface;
import org.intelehealth.app.syncModule.LastSyncWork;
import org.intelehealth.app.syncModule.SyncWorkManager;
import org.intelehealth.app.syncModule.VisitSummaryWork;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.NotificationUtils;
import org.intelehealth.app.utilities.UuidGenerator;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class AppConstants {
    //Constants
    public static final String DATABASE_NAME = BuildConfig.FLAVOR_client + "-localrecords.db";
    public static final int DATABASE_VERSION = 4;
    public static final String JSON_FOLDER = "Engines";
    public static final String JSON_FOLDER_Update = "Engines_Update";
    public static final String IMAGE_APP_ID = "app2";
    public static final String dbfilepath = Environment.getExternalStorageDirectory() + File.separator + "InteleHealth_DB" + File.separator + "Intelehealth.db"; // directory: Intelehealth_DB   ,  filename: Intelehealth.db
    public static final int FORGOT_USER_NAME_ACTION = 50001;
    public static final int FORGOT_USER_PASSWORD_ACTION = 50002;
    public static String CONFIG_FILE_NAME = "config.json";
    public static final String IMAGE_PATH = IntelehealthApplication.getAppContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator;
    public static final String MESSAGE_PROGRESS = "message_progress";
    public static final String MCC_USER_TYPE = "mcc";

    public static final int PAGE_LIMIT = 50;

    public static final long FOLLOW_UP_SCHEDULE_ONE_DURATION = 5;
    public static final long FOLLOW_UP_SCHEDULE_TWO_DURATION = 24;

    //App vitals constants

    public static String getMinWeightByAge(int ageInMonth) {
        if (ageInMonth <= 1) {
            return "0.8";
        } else if (ageInMonth <= 12) {
            return "1";
        } else if (ageInMonth <= 12 * 12) {
            return "4";
        } else if (ageInMonth <= 18 * 12) {
            return "15";
        } else {
            return "20";
        }
    }

    public static String getMaxWeightByAge(int ageInMonth) {

        if (ageInMonth <= 1) {
            return "6";
        } else if (ageInMonth <= 12) {
            return "16";
        } else if (ageInMonth <= 12 * 12) {
            return "80";
        } else if (ageInMonth <= 18 * 12) {
            return "150";
        } else {
            return "500";
        }
    }

    public static final String MAXIMUM_HEIGHT = "252";
    public static final String MINIMUM_HEIGHT = "40";

    public static final String MAXIMUM_WEIGHT = "149";
    public static final String MINIMUM_WEIGHT = "1";

    public static int APP_VERSION_CODE = 26;

    public static final String MAXIMUM_BP_SYS = "250";
    public static final String MINIMUM_BP_SYS = "50";

    public static final String MAXIMUM_BP_DSYS = "150";
    public static final String MINIMUM_BP_DSYS = "30";

    public static final String MAXIMUM_PULSE = "240";
    public static final String MINIMUM_PULSE = "30";

    public static final String MAXIMUM_TEMPERATURE_CELSIUS = "43";
    public static final String MINIMUM_TEMPERATURE_CELSIUS = "25";
    public static final String MINIMUM_TEMPERATURE_FARHENIT = "89.6";
    public static final String MAXIMUM_TEMPERATURE_FARHENIT = "109";

    public static final String MAXIMUM_SPO2 = "100";
    public static final String MINIMUM_SPO2 = "70";

    public static final String MAXIMUM_RESPIRATORY = "80";
    public static final String MINIMUM_RESPIRATORY = "10";


    //functions constants

    public static final String UNIQUE_WORK_NAME = "intelehealth_workmanager";
    public static ApiInterface apiInterface = ApiClient.createService(ApiInterface.class);
    public static DateAndTimeUtils dateAndTimeUtils = new DateAndTimeUtils();
    public static String NEW_UUID = new UuidGenerator().UuidGenerator();
    public static NotificationUtils notificationUtils = new NotificationUtils();


    //  Image Conversion Ratio
    public static int IMAGE_JPG_QUALITY = 70;


    public static int REPEAT_INTERVAL = 15;
    public static Constraints MY_CONSTRAINTS = new Constraints.Builder()
            .setRequiresCharging(false)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(false)
            .setRequiresStorageNotLow(false)
            .build();

    public static PeriodicWorkRequest PERIODIC_WORK_REQUEST =
            new PeriodicWorkRequest.Builder(SyncWorkManager.class, REPEAT_INTERVAL, TimeUnit.MINUTES)
                    .setConstraints(MY_CONSTRAINTS)
                    .build();


    // Added by Venu to make the Sync Issue Solutions as intele_safe.
    public static OneTimeWorkRequest VISIT_SUMMARY_WORK_REQUEST =
            new OneTimeWorkRequest.Builder(VisitSummaryWork.class)
                    .setConstraints(MY_CONSTRAINTS)
                    .build();

    public static OneTimeWorkRequest LAST_SYNC_WORK_REQUEST =
            new OneTimeWorkRequest.Builder(LastSyncWork.class)
                    .setConstraints(MY_CONSTRAINTS)
                    .build();

    /*@Lincon*/
    public static final String SYNC_INTENT_ACTION = "org.intelehealth.app.LAST_SYNC";
    public static final String SYNC_NOTIFY_INTENT_ACTION = "org.intelehealth.app.SYNC_DONE_REFRESH_ACTION";
    public static final String SYNC_INTENT_DATA_KEY = "SYNC_JOB_TYPE";
    public static final int SYNC_FAILED = 0;
    public static final int SYNC_PULL_DATA_DONE = 1;
    public static final int SYNC_PUSH_DATA_DONE = 2;

    public static final int ALL_SYNC_DONE = 0;

    public static final int SYNC_PUSH_DATA_TO_LOCAL_DB_DONE = 5;
    public static final int SYNC_APPOINTMENT_PULL_DATA_DONE = 22;
    public static final int SYNC_PULL_PUSH_APPOINTMENT_PULL_DATA_DONE = 25;
    public static final int SYNC_PATIENT_PROFILE_IMAGE_PUSH_DONE = 3;
    public static final int SYNC_OBS_IMAGE_PUSH_DONE = 4;

    public static final String FIREBASE_REAL_TIME_DB_BASE_URL = "https://intelehealth-3-0-default-rtdb.firebaseio.com/";
    public static final String FIREBASE_REAL_TIME_DB_BASE_REF = "rtc_notify/";
    public static final String FIREBASE_REAL_TIME_DB_BASE_REF_SAVE_DEVICE = "device_info/";


    public static String getFirebaseRTDBUrl() {
        return BuildConfig.REAL_TIME_FB_URL;

    }

    public static String getFirebaseRTDBRootRef() {
        return BuildConfig.FB_RT_INSTANCE.replaceAll("\\.", "_") + "/" + FIREBASE_REAL_TIME_DB_BASE_REF;

    }

    public static String getFirebaseRTDBRootRefForDeviceInfo() {
        return BuildConfig.FB_RT_INSTANCE.replaceAll("\\.", "_") + "/" + FIREBASE_REAL_TIME_DB_BASE_REF_SAVE_DEVICE;

    }

    //UI2.0 Constants
    //public static final String DEMO_URL = "uiux.intelehealth.org";
    //public static final String DEMO_URL = "demo2.intelehealth.org";
    //public static final String DEMO_URL = "demo.intelehealth.org";

//    public static final String DEMO_URL = "dev.intelehealth.org";
//    public static final String DEMO_URL = "revamp.intelehealth.org";
    //public static final String DEMO_URL = "testing.intelehealth.org";

    public static final int INTENT_FROM_AYU_FOR_SETUP = 1;
    public static final int INTENT_FROM_HOME_FOR_PATIENT_CREATION = 2;

    public static final int TERMS_CONDITIONS_ACCEPT = 1;
    public static final int TERMS_CONDITIONS_DECLINE = 2;
    public static final int PRIVACY_POLICY_ACCEPT = 3;
    public static final int PRIVACY_POLICY_DECLINE = 4;

    public static final int TELECONSULTATION_CONSENT_ACCEPT = 5;
    public static final int TELECONSULTATION_CONSENT_DECLINE = 6;

    public static final int PERSONAL_CONSENT_ACCEPT = 5;

    public static final int PERSONAL_CONSENT_DECLINE = 6;


    public static final int EVENT_FLAG_START = 0;
    public static final int EVENT_FLAG_SUCCESS = 1;
    public static final int EVENT_FLAG_FAILED = 2;

    // OBS IMAGE TYPE
    public static final String IMAGE_ADDITIONAL_DOC = "ADDITIONAL_DOC";

    public static final int EVENT_APPOINTMENT_BOOKING_FROM_VISIT_SUMMARY = 3992;
    public static final int EVENT_APPOINTMENT_BOOKING_APPOINTMENT_DETAILS = 3993;

    //status of appointment
    public static final String CANCELLED = "cancelled";
    public static final String BOOKED = "booked";

    public static String getAppPlayStoreUrl(Context context) {
        return "https://play.google.com/store/apps/details?id=" + context.getApplicationContext().getPackageName();
    }

    public static String getAppMarketUrl(Context context) {
        return "market://details?id=" + context.getApplicationContext().getPackageName();
    }

    public static final String MINIMUM_GLUCOSE_NON_FASTING = "60";
    public static final String MAXIMUM_GLUCOSE_NON_FASTING = "600";

    public static final String MINIMUM_GLUCOSE_FASTING = "30";
    public static final String MAXIMUM_GLUCOSE_FASTING = "300";

    public static final String MINIMUM_GLUCOSE_RANDOM = "30";
    public static final String MAXIMUM_GLUCOSE_RANDOM = "500";

    public static final String MINIMUM_GLUCOSE_POST_PRANDIAL = "70";
    public static final String MAXIMUM_GLUCOSE_POST_PRANDIAL = "400";

    public static final String MINIMUM_HEMOGLOBIN = "4";
    public static final String MAXIMUM_HEMOGLOBIN = "18";
    public static final String MINIMUM_URIC_ACID = "2";
    public static final String MAXIMUM_URIC_ACID = "12";

    public static final String MINIMUM_TOTAL_CHOLSTEROL = "100";
    public static final String MAXIMUM_TOTAL_CHOLSTEROL = "350";

}

