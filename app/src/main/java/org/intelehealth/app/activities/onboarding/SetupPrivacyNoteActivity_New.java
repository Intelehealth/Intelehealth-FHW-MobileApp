package org.intelehealth.app.activities.onboarding;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.LocaleList;
import android.os.Looper;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.setupActivity.SetupActivityNew;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.ui.splash.activity.SplashActivity;
import org.intelehealth.app.utilities.SessionManager;

import java.util.Locale;

public class SetupPrivacyNoteActivity_New extends AppCompatActivity {
    private static final String TAG = "SetupPrivacyNoteActivit";
    TextView tvTermsAndPrivacy;
    CustomDialog customDialog;
    private CardView cardNoteSnack;
    private Context context = SetupPrivacyNoteActivity_New.this;
    private int startingPositionTC, endingPositionTC, startingPositionPP, endingPositionPP;
    private Button btnSetup;
    private ImageView ivBack;
    private CheckBox chkBoxPrivacyPolicy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_privacy_note_ui2);
        tvTermsAndPrivacy = findViewById(R.id.tv_privacy_notice_link_1);
        tvTermsAndPrivacy.setMovementMethod(LinkMovementMethod.getInstance());
        tvTermsAndPrivacy.setSelected(true);
        tvTermsAndPrivacy.setClickable(true);

        btnSetup = findViewById(R.id.btn_setup);
        cardNoteSnack = findViewById(R.id.card_note_snack_policy);
        ivBack = findViewById(R.id.iv_setup_privacy_back);
        termsAndPrivacyPolicy();

        ivBack.setOnClickListener(v -> {
            Intent intent = new Intent(SetupPrivacyNoteActivity_New.this, SplashActivity.class);
            startActivity(intent);
        });

//        btnSetup.getBackground().setAlpha(60);
        chkBoxPrivacyPolicy = findViewById(R.id.checkbox_privacy_policy);
        chkBoxPrivacyPolicy.setOnCheckedChangeListener((compoundButton, b) -> btnSetup.setEnabled(b));
        btnSetup.setEnabled(chkBoxPrivacyPolicy.isChecked());

        btnSetup.setOnClickListener(v -> {
            if (chkBoxPrivacyPolicy.isChecked()) {
                customDialog = new CustomDialog(SetupPrivacyNoteActivity_New.this);
                customDialog.showDialog1();
            } else {
                showSnackBarAndRemoveLater();
            }
        });

    }

    public void termsAndPrivacyPolicy() {
        SpannableString SpanString = new SpannableString(getResources().getString(R.string.agree_to_terms));
        ClickableSpan termsAndCondition = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Intent mIntent = new Intent(context, TermsAndConditionsActivity_New.class);
                mIntent.putExtra("isTermsAndCondition", true);
                startActivity(mIntent);
            }
        };
        ClickableSpan privacy = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Intent mIntent = new Intent(context, PrivacyPolicyActivity_New.class);
                mIntent.putExtra("intentType", "doNotNavigateFurther");
                mIntent.putExtra("isPrivacyPolicy", true);
                startActivity(mIntent);
            }
        };

        startingPositionTC = getResources().getString(R.string.agree_to_terms).indexOf(getResources().getString(R.string.terms_and_conditions));
        endingPositionTC = startingPositionTC + getResources().getString(R.string.terms_and_conditions).length();
        startingPositionPP = getResources().getString(R.string.agree_to_terms).indexOf(getResources().getString(R.string.privacy_policy));
        endingPositionPP = startingPositionPP + getResources().getString(R.string.privacy_policy).length();

        SpanString.setSpan(termsAndCondition, startingPositionTC, endingPositionTC, 0);
        SpanString.setSpan(privacy, startingPositionPP, endingPositionPP, 0);

        tvTermsAndPrivacy.setText(SpanString, TextView.BufferType.SPANNABLE);
    }

    ActivityResultLauncher<Intent> mStartForResultTCPP = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == AppConstants.TERMS_CONDITIONS_ACCEPT || result.getResultCode() == AppConstants.PRIVACY_POLICY_ACCEPT) {
                        //Intent intent = result.getData();
                        // Handle the Intent
                        customDialog = new CustomDialog(SetupPrivacyNoteActivity_New.this);
                        customDialog.showDialog1();
                    } else if (result.getResultCode() == AppConstants.TERMS_CONDITIONS_DECLINE || result.getResultCode() == AppConstants.PRIVACY_POLICY_DECLINE) {
                        finish();
                    } else {

                    }
                }
            });


    class CustomDialog extends DialogFragment {
        Context context;

        public CustomDialog(Context context) {
            this.context = context;
        }

        public void showDialog1() {
            Intent intent = new Intent(SetupPrivacyNoteActivity_New.this, SetupActivityNew.class);
            context.startActivity(intent);
            finish();
        }
    }

    private void showSnackBarAndRemoveLater() {
        cardNoteSnack.setVisibility(View.VISIBLE);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                cardNoteSnack.setVisibility(View.GONE);
            }
        }, 3000);
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
}