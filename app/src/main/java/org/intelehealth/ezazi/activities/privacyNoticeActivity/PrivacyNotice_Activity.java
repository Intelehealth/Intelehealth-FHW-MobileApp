package org.intelehealth.ezazi.activities.privacyNoticeActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.identificationActivity.IdentificationActivity;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.utilities.FileUtils;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Objects;

public class PrivacyNotice_Activity extends AppCompatActivity implements View.OnClickListener {
    TextView privacy_textview;
    SessionManager sessionManager = null;
    private boolean hasLicense = false;
    Button accept, reject;
    MaterialCheckBox checkBox_cho;

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
        sessionManager.setCurrentLang(getResources().getConfiguration().locale.toString());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_notice_ezazi);
//        setTitle(getString(R.string.privacy_notice_title));

        /*
         * Toolbar which displays back arrow on action bar
         * Add the below lines for every activity*/
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        toolbar.setTitleTextAppearance(this, R.style.ToolbarTheme);
//        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle(getString(R.string.privacy_notice_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        sessionManager = new SessionManager(this);
        privacy_textview = findViewById(R.id.privacy_text);
        privacy_textview.setAutoLinkMask(Linkify.ALL);
        accept = findViewById(R.id.button_accept);
        reject = findViewById(R.id.button_reject);
        checkBox_cho = findViewById(R.id.checkbox_CHO);


        if (!sessionManager.getLicenseKey().isEmpty())
            hasLicense = true;

        //Check for license key and load the correct config file
        try {
            JSONObject obj = null;
            if (hasLicense) {
                obj = new JSONObject(Objects.requireNonNullElse(
                        FileUtils.readFileRoot(AppConstants.CONFIG_FILE_NAME, this),
                        String.valueOf(FileUtils.encodeJSON(this, AppConstants.CONFIG_FILE_NAME)))); //Load the config file

            } else {
                obj = new JSONObject(String.valueOf(FileUtils.encodeJSON(this, AppConstants.CONFIG_FILE_NAME)));
            }

//            SharedPreferences sharedPreferences = getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
//            if(sharedPreferences.getAll().values().contains("cb"))
            Locale current = getResources().getConfiguration().locale;
           /* if (current.toString().equals("or")) { //Privacy notice support for Oriya
                String privacy_string = obj.getString("privacyNoticeText_Oriya");
                if (privacy_string.isEmpty()) {
                    privacy_string = obj.getString("privacyNoticeText");
                }
                privacy_textview.setText(privacy_string);
            } else if (current.toString().equals("gu")) {
                String privacy_string = obj.getString("privacyNoticeText_Gujarati");
                if (privacy_string.isEmpty()) {
                    privacy_string = obj.getString("privacyNoticeText");
                }
                privacy_textview.setText(privacy_string);

            } else if (current.toString().equals("bn")) { //Privacy notice support for Bengali
                String privacy_string = obj.getString("privacyNoticeText_Bengali");
                if (privacy_string.isEmpty()) {
                    privacy_string = obj.getString("privacyNoticeText");
                }
                privacy_textview.setText(privacy_string);
            } else if (current.toString().equals("hi")) { //Privacy notice support for Hindi
                String privacy_string = obj.getString("privacyNoticeText_Hindi");
                if (privacy_string.isEmpty()) {
                    privacy_string = obj.getString("privacyNoticeText");
                }
                privacy_textview.setText(privacy_string);
            } else if (current.toString().equals("ta")) { //privacy notice support for Tamil language
                String privacy_string = obj.getString("privacyNoticeText_Tamil");
                if (privacy_string.isEmpty()) {
                    privacy_string = obj.getString("privacyNoticeText");
                }
                privacy_textview.setText(privacy_string);

            } else if (current.toString().equals("kn")) { //Privacy text support for Kannada
                String privacy_string = obj.getString("privacyNoticeText_Kannada");
                if (privacy_string.isEmpty()) {
                    privacy_string = obj.getString("privacyNoticeText");
                }
                privacy_textview.setText(privacy_string);
            } else if (current.toString().equals("te")) { //privacy notice support for Telugu language
                String privacy_string = obj.getString("privacyNoticeText_Telugu");
                if (privacy_string.isEmpty()) {
                    privacy_string = obj.getString("privacyNoticeText");
                }
                privacy_textview.setText(privacy_string);
            } else if (current.toString().equals("mr")) { //privacy notice support for Marathi language
                String privacy_string = obj.getString("privacyNoticeText_Marathi");
                if (privacy_string.isEmpty()) {
                    privacy_string = obj.getString("privacyNoticeText");
                }
                privacy_textview.setText(privacy_string);
            } else if (current.toString().equals("as")) { //privacy notice support for Assamese language...
                String privacy_string = obj.getString("privacyNoticeText_Assamese");
                if (privacy_string.isEmpty()) {
                    privacy_string = obj.getString("privacyNoticeText");
                }
                privacy_textview.setText(privacy_string);
            } else if (current.toString().equals("ml")) { // privacy notice support for Malayalam language
                String privacy_string = obj.getString("privacyNoticeText_Malayalam");
                if (privacy_string.isEmpty()) {
                    privacy_string = obj.getString("privacyNoticeText");
                }
                privacy_textview.setText(privacy_string);
            } else if (current.toString().equals("ru")) { // privacy notice support for Russian language
                String privacy_string = obj.getString("privacyNoticeText_Russian");
                if (privacy_string.isEmpty()) {
                    privacy_string = obj.getString("privacyNoticeText");
                }
                privacy_textview.setText(privacy_string);
            } else {*/
            String privacy_string = obj.getString("privacyNoticeText");
            privacy_textview.setText(privacy_string);
            //}
            accept.setOnClickListener(this);
            reject.setOnClickListener(this);

        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Toast.makeText(getApplicationContext(), "JsonException" + e, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {

        if (checkBox_cho.isChecked() && v.getId() == R.id.button_accept) {

            //Clear HouseHold UUID from Session for new registration
            sessionManager.setHouseholdUuid("");

            Intent intent = new Intent(getApplicationContext(), IdentificationActivity.class);
            intent.putExtra("privacy", accept.getText().toString()); //privacy value send to identificationActivity
            Log.d("Privacy", "selected radio: " + accept.getText().toString());
            startActivity(intent);
        } else if (checkBox_cho.isChecked() && v.getId() == R.id.button_reject) {
            Toast.makeText(PrivacyNotice_Activity.this,
                    getString(R.string.privacy_reject_toast), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(PrivacyNotice_Activity.this,
                    getString(R.string.please_read_out_privacy_consent_first), Toast.LENGTH_SHORT).show();
        }

    }
}