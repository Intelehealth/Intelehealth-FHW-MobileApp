package org.intelehealth.nak.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.github.ajalt.timberkt.Timber;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.parse.Parse;

import org.intelehealth.klivekit.RtcEngine;
import org.intelehealth.klivekit.socket.SocketManager;
import org.intelehealth.klivekit.utils.Manager;
import org.intelehealth.nak.R;
import org.intelehealth.nak.database.InteleHealthDatabaseHelper;
import org.intelehealth.nak.utilities.SessionManager;
import org.intelehealth.nak.webrtc.activity.NammaCallLogActivity;
import org.intelehealth.nak.webrtc.activity.NammaChatActivity;
import org.intelehealth.nak.webrtc.activity.NammaVideoActivity;

import io.reactivex.plugins.RxJavaPlugins;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

//Extend Application class with MultiDexApplication for multidex support
public class IntelehealthApplication extends MultiDexApplication {

    private static final String TAG = IntelehealthApplication.class.getSimpleName();
    private static Context mContext;
    private static String androidId;
    private Activity currentActivity;
    SessionManager sessionManager;

    public static Context getAppContext() {
        return mContext;
    }

    public static String getAndroidId() {
        return androidId;
    }

    private static IntelehealthApplication sIntelehealthApplication;
    public String refreshedFCMTokenID = "";
    public String webrtcTempCallId = "";

    public static IntelehealthApplication getInstance() {
        return sIntelehealthApplication;
    }

    public static InteleHealthDatabaseHelper inteleHealthDatabaseHelper;

    private SocketManager socketManager = SocketManager.getInstance();

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sIntelehealthApplication = this;
        inteleHealthDatabaseHelper = InteleHealthDatabaseHelper.getInstance(sIntelehealthApplication);
        //For Vector Drawables Backward Compatibility(<API 21)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        mContext = getApplicationContext();
        sessionManager = new SessionManager(this);
        // keeping the base url in one singleton object for using in apprtc module

        configureCrashReporting();

        RxJavaPlugins.setErrorHandler(throwable -> {
            //   FirebaseCrashlytics.getInstance().recordException(throwable);
        });
        androidId = String
                .format("%16s", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID))
                .replace(' ', '0');

        String url = sessionManager.getServerUrl();
        Log.d(TAG, "onCreate: appurl kk :: " + url);
        if (url == null || url.isEmpty()) {
            Log.i(TAG, "onCreate: Parse not init");
        } else {
            Dispatcher dispatcher = new Dispatcher();
            dispatcher.setMaxRequestsPerHost(1);
            dispatcher.setMaxRequests(4);
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.dispatcher(dispatcher);

            Parse.initialize(new Parse.Configuration.Builder(this)
                    .clientBuilder(builder)
                    .applicationId(AppConstants.IMAGE_APP_ID)
                    .server(sessionManager.getServerUrl() + ":1337/parse/")
                    .build()
            );
            Log.i(TAG, "onCreate: Parse init");

            InteleHealthDatabaseHelper mDbHelper = new InteleHealthDatabaseHelper(this);
            SQLiteDatabase localdb = mDbHelper.getWritableDatabase();
            mDbHelper.onCreate(localdb);
        }

        initSocketConnection();
    }

    private void configureCrashReporting() {
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }


    /**
     * for setting the Alert Dialog Custom Font.
     *
     * @param context
     * @param builderDialog
     */
    public static void setAlertDialogCustomTheme(Context context, Dialog builderDialog) {
        // Getting the view elements
        TextView textView = (TextView) builderDialog.getWindow().findViewById(android.R.id.message);
        TextView alertTitle = (TextView) builderDialog.getWindow().findViewById(androidx.appcompat.R.id.alertTitle);
        Button button1 = (Button) builderDialog.getWindow().findViewById(android.R.id.button1);
        Button button2 = (Button) builderDialog.getWindow().findViewById(android.R.id.button2);
        textView.setTypeface(ResourcesCompat.getFont(context, R.font.lato_regular));
        alertTitle.setTypeface(ResourcesCompat.getFont(context, R.font.lato_bold));
        button1.setTypeface(ResourcesCompat.getFont(context, R.font.lato_bold));
        button2.setTypeface(ResourcesCompat.getFont(context, R.font.lato_bold));
    }

    /**
     * Socket should be open and close app level,
     * so when app create open it and close on app terminate
     */
    public void initSocketConnection() {
        Log.d(TAG, "initSocketConnection: ");
        if (sessionManager.getServerUrl() != null && !sessionManager.getServerUrl().isEmpty()) {
            //Manager.getInstance().setBaseUrl(BuildConfig.SERVER_URL);
            Manager.getInstance().setBaseUrl(sessionManager.getServerUrl());
            String socketUrl = sessionManager.getServerUrl() + ":3004" + "?userId="
                    + sessionManager.getProviderID()
                    + "&name=" + sessionManager.getChwname();
            if (!socketManager.isConnected()) socketManager.connect(socketUrl);
            initRtcConfig();
        }
    }

    private void initRtcConfig() {
      /*  new RtcEngine.Builder()
                .callUrl(BuildConfig.LIVE_KIT_URL)
                .socketUrl(BuildConfig.SOCKET_URL + "?userId="
                        + sessionManager.getProviderID()
                        + "&name=" + sessionManager.getChwname())
                .callIntentClass(NammaVideoActivity.class)
                .chatIntentClass(NammaChatActivity.class)
                .callLogIntentClass(NammaCallLogActivity.class)
                .build().saveConfig(this);*/
        // DEV_LIVE_KIT_URL="wss://naktraining.intelehealth.org:9090"
        // PROD_LIVE_KIT_URL="wss://nak.intelehealth.org:9090"
        //DEV_SOCKET_URL="https://naktraining.intelehealth.org:3004"
        //PROD_SOCKET_URL="https://nak.intelehealth.org:3004"
        String liveKitUrl = "wss://" + sessionManager.getServerUrl() + ":9090";
        String socket_url = sessionManager.getServerUrl() + ":3004";
        String liveKitCleanedUrl = liveKitUrl.replace("wss://https://", "wss://");

        Log.d(TAG, "kzzzzinitRtcConfig: old livekiturl : wss://naktraining.intelehealth.org:9090");
        Log.d(TAG, "kzzzzinitRtcConfig: old socket_url : https://naktraining.intelehealth.org:3004");
        Log.d(TAG, "kzzzzinitRtcConfig: new livekiturl : " + liveKitUrl);
        Log.d(TAG, "kzzzzinitRtcConfig: new liveKitCleanedUrl : " + liveKitCleanedUrl);
        Log.d(TAG, "kzzzzinitRtcConfig: new socket_url : " + socket_url);
        Log.d(TAG, "initRtcConfig: sessionmanager url : " + sessionManager.getServerUrl());

        new RtcEngine.Builder()
                .callUrl(liveKitCleanedUrl)
                .socketUrl(socket_url + "?userId="
                        + sessionManager.getProviderID()
                        + "&name=" + sessionManager.getChwname())
                .callIntentClass(NammaVideoActivity.class)
                .chatIntentClass(NammaChatActivity.class)
                .callLogIntentClass(NammaCallLogActivity.class)
                .build().saveConfig(this);
    }

    @Override
    public void onTerminate() {
        Timber.tag("APP").d("onTerminate");
        disconnectSocket();
        super.onTerminate();
    }

    public void disconnectSocket() {
        socketManager.disconnect();
    }
}
