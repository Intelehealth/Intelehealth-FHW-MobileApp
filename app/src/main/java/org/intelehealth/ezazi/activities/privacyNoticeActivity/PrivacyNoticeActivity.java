package org.intelehealth.ezazi.activities.privacyNoticeActivity;

import static android.text.Layout.JUSTIFICATION_MODE_INTER_WORD;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.addNewPatient.AddNewPatientActivity;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.ui.shared.BaseActionBarActivity;
import org.intelehealth.ezazi.utilities.FileUtils;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.utilities.TextThemeUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Objects;

public class PrivacyNoticeActivity extends BaseActionBarActivity implements View.OnClickListener {
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
        setContentView(R.layout.activity_privacy_notice_ezazi);
        super.onCreate(savedInstanceState);
        setupActionBar();
        sessionManager = new SessionManager(this);
        privacy_textview = findViewById(R.id.privacy_text);
//        privacy_textview.setAutoLinkMask(Linkify.ALL);

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

            String privacy_string = obj.getString("privacyNoticeText");
            privacy_textview.setText(privacy_string);
            final SpannableString span_string = new SpannableString(privacy_string);
            Linkify.addLinks(span_string, Linkify.ALL);
            privacy_textview.setText(span_string);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                privacy_textview.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
//            }
//            TextThemeUtils.justify(privacy_textview);
            //}
            accept.setOnClickListener(this);
            reject.setOnClickListener(this);

        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Toast.makeText(getApplicationContext(), "JsonException" + e, Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected int getScreenTitle() {
        return R.string.privacy_notice_title;
    }

    @Override
    public void onClick(View v) {

        if (checkBox_cho.isChecked() && v.getId() == R.id.button_accept) {

            //Clear HouseHold UUID from Session for new registration
            sessionManager.setHouseholdUuid("");

            Intent intent = new Intent(getApplicationContext(), AddNewPatientActivity.class);
            intent.putExtra("privacy", accept.getText().toString()); //privacy value send to identificationActivity
            Log.d("Privacy", "selected radio: " + accept.getText().toString());
            startActivity(intent);
            finish();
        } else if (checkBox_cho.isChecked() && v.getId() == R.id.button_reject) {
            Toast.makeText(PrivacyNoticeActivity.this,
                    getString(R.string.privacy_reject_toast), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(PrivacyNoticeActivity.this,
                    getString(R.string.please_read_out_privacy_consent_first), Toast.LENGTH_SHORT).show();
        }

    }
}
