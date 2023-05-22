package org.intelehealth.app.activities.onboarding;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.IntroActivity.IntroScreensActivity_New;
import org.intelehealth.app.activities.identificationActivity.IdentificationActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.utilities.SessionManager;

import java.util.Locale;

public class PrivacyPolicyActivity_New extends AppCompatActivity {
    private static final String TAG = "PrivacyPolicyActivityNe";
    private Button btn_accept_privacy;
    private int mIntentFrom;
    String appLanguage;
    SessionManager sessionManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy_new_ui2);
        sessionManager = new SessionManager(PrivacyPolicyActivity_New.this);

        appLanguage = sessionManager.getAppLanguage();
        if (!appLanguage.equalsIgnoreCase("")) {
            setLocale(appLanguage);
        }

        // changing status bar color
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
        }
        mIntentFrom = getIntent().getIntExtra("IntentFrom", 0);
        ImageView ivBack = findViewById(R.id.iv_back_arrow_terms);
        btn_accept_privacy = findViewById(R.id.btn_accept_privacy);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(PrivacyPolicyActivity_New.this, SetupPrivacyNoteActivity_New.class);
//                startActivity(intent); // TODO: add finish here...
                finish();
            }
        });

        btn_accept_privacy.setOnClickListener(v -> {
            if(mIntentFrom == AppConstants.INTENT_FROM_AYU_FOR_SETUP){
                setResult(AppConstants.PRIVACY_POLICY_ACCEPT);
                finish();
            }else {
                Intent intent = new Intent(this, IdentificationActivity_New.class);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //overridePendingTransition(R.anim.ui2_slide_in_right, R.anim.ui2_slide_bottom_down);
    }

    public void declinePP(View view) {
        setResult(AppConstants.PRIVACY_POLICY_DECLINE);
        finish();
    }

    public void setLocale(String appLanguage) {
        // here comes en, hi, mr
        Locale locale = new Locale(appLanguage);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }
}