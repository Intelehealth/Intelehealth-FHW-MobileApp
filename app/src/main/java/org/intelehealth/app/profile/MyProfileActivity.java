package org.intelehealth.app.profile;

import static org.intelehealth.app.syncModule.SyncUtils.syncNow;
import static org.intelehealth.app.utilities.StringUtils.en_hi_dob_updated;
import static org.intelehealth.app.utilities.StringUtils.en_ru_dob_three;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.LocaleList;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import org.intelehealth.app.utilities.CustomLog;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.biometric.BiometricManager;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.ajalt.timberkt.Timber;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.hbb20.CountryCodePicker;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.R;
import org.intelehealth.app.activities.forgotPasswordNew.ChangePasswordActivity_New;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.database.dao.ImagesDAO;
import org.intelehealth.app.database.dao.ImagesPushDAO;
import org.intelehealth.app.database.dao.ProviderDAO;
import org.intelehealth.app.database.dao.SyncDAO;
import org.intelehealth.app.models.MyProfilePOJO;
import org.intelehealth.app.models.dto.ProviderDTO;
import org.intelehealth.app.models.hwprofile.PersonAttributes;
import org.intelehealth.app.models.hwprofile.Profile;
import org.intelehealth.app.models.hwprofile.ProfileCreateAttribute;
import org.intelehealth.app.models.hwprofile.ProfileUpdateAge;
import org.intelehealth.app.models.hwprofile.ProfileUpdateAttribute;
import org.intelehealth.app.networkApiCalls.ApiClient;
import org.intelehealth.app.networkApiCalls.ApiInterface;
import org.intelehealth.app.shared.BaseActivity;
import org.intelehealth.app.ui2.calendarviewcustom.SendSelectedDateInterface;
import org.intelehealth.app.utilities.BitmapUtils;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.DownloadFilesUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.NetworkUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.SnackbarUtils;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.ValidatorUtils;
import org.intelehealth.app.utilities.exception.DAOException;
import org.intelehealth.ihutils.ui.CameraActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class MyProfileActivity extends BaseActivity implements SendSelectedDateInterface, NetworkUtils.InternetCheckUpdateInterface {
    private static final String TAG = "MyProfileActivity";
    TextInputEditText etEmail, etMobileNo;
    TextView tvDob, tvAge, tvChangePhoto, tvErrorFirstName, tvErrorLastName, tvErrorMobileNo, tvErrorDob, etUsername, etFirstName, etMiddleName, etLastName;
    LinearLayout layoutParent, ll_middlename;
    String selectedGender, profileImagePAth = "", errorMsg = "", mSelectedCountryCode = "", dobToDb;
    ImageView ivProfileImage, ivIsInternet, refresh;
    SessionManager sessionManager;
    private static final int PICK_IMAGE_FROM_GALLERY = 2001;
    private Handler mBackgroundHandler;
    private static final int GROUP_PERMISSION_REQUEST = 1000;
    RadioButton rbMale, rbFemale, rbOther;
    Button btnSave;
    SnackbarUtils snackbarUtils;
    private CountryCodePicker countryCodePicker;
    NetworkUtils networkUtils;
    private boolean isSynced = false;
    private MyProfilePOJO myProfilePOJO = new MyProfilePOJO();
    SwitchCompat fingerprintSwitch;
    private int mSelectedMobileNumberValidationLength = 0;
    private ObjectAnimator syncAnimator;
    String prevDOB = "", prevPhoneNum = null, prevEmail = null, prevCountryCode = null;
    String phoneAttributeUuid = null, emailAttributeUuid = null, countryCodeAttributeUuid = null;
    String gender = "F", personUuid = "";
    RelativeLayout layoutChangePassword;
    RadioGroup rgGroupGender;
    ImageView ivBack;
    private Context context;
    private CardView snackbar_cv;
    private TextView snackbar_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile_ui2);
        context = MyProfileActivity.this;
        // Status Bar color -> White
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(Color.WHITE);
        networkUtils = new NetworkUtils(MyProfileActivity.this, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            initUI();
        } catch (DAOException e) {
            e.printStackTrace();
        }


    }


    private void initUI() throws DAOException {
        snackbarUtils = new SnackbarUtils();
        sessionManager = new SessionManager(this);
        View toolbar = findViewById(R.id.toolbar_common);
        refresh = toolbar.findViewById(R.id.imageview_is_internet_common);
        TextView tvTitle = toolbar.findViewById(R.id.tv_screen_title_common);
        ivIsInternet = toolbar.findViewById(R.id.imageview_is_internet_common);
        fingerprintSwitch = findViewById(R.id.fingerprint_enable_Switch);
        snackbar_cv = findViewById(R.id.snackbar_cv);
        snackbar_text = findViewById(R.id.snackbar_text);

        fingerprintSwitch.setChecked(sessionManager.isEnableAppLock());

        ivBack = toolbar.findViewById(R.id.iv_back_arrow_common);
        tvTitle.setText(getResources().getString(R.string.my_profile));

        //initialize all input fields
        etUsername = findViewById(R.id.tv_username_profile);
        etFirstName = findViewById(R.id.tv_first_name_profile);
        etMiddleName = findViewById(R.id.tv_middle_name_profile);
        ll_middlename = findViewById(R.id.ll_middlename);
        etLastName = findViewById(R.id.tv_last_name_profile);
        etEmail = findViewById(R.id.et_email_profile);
        etMobileNo = findViewById(R.id.et_mobile_no_profile);
        tvDob = findViewById(R.id.tv_date_of_birth_profile);
        tvAge = findViewById(R.id.tv_age_profile);
        btnSave = findViewById(R.id.btn_save_profile);
        layoutParent = findViewById(R.id.layout_parent_profile);
        rbMale = findViewById(R.id.rb_male);
        rbFemale = findViewById(R.id.rb_female);
        rbOther = findViewById(R.id.rb_other);
        countryCodePicker = findViewById(R.id.countrycode_spinner_profile);
        countryCodePicker.registerCarrierNumberEditText(etMobileNo); // attaches the ccp spinner with the edittext
        countryCodePicker.setNumberAutoFormattingEnabled(false);
        ivProfileImage = findViewById(R.id.iv_profilePic);
        tvChangePhoto = findViewById(R.id.tv_change_photo_profile);
        tvErrorFirstName = findViewById(R.id.tv_firstname_error);
        tvErrorLastName = findViewById(R.id.tv_lastname_error);
        tvErrorMobileNo = findViewById(R.id.tv_mobile_error);
        layoutChangePassword = findViewById(R.id.view_change_password);
        rgGroupGender = findViewById(R.id.radioGroup_gender_profile);
        tvErrorDob = findViewById(R.id.tv_dob_error);

        manageListeners();
        setMobileNumberLimit();
    }

    private void manageListeners() {

        refresh.setOnClickListener(v -> {
            isSynced = syncNow(MyProfileActivity.this, refresh, syncAnimator);
            if (isSynced) fetchUserDetails();
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyProfileActivity.this, HomeScreenActivity_New.class);
                startActivity(intent);
                finish();
            }
        });

        fingerprintSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    boolean isAvailable = checkFingerprintSensor();
                    if (isAvailable) {
                        sessionManager.setEnableAppLock(true);
                        Toast.makeText(MyProfileActivity.this, getResources().getString(R.string.fingerprint_lock_enabled), Toast.LENGTH_LONG).show();
                    } else {
                        DialogUtils dialogUtils = new DialogUtils();
                        dialogUtils.showCommonDialog(MyProfileActivity.this, R.drawable.ui2_ic_warning_internet, getResources().getString(R.string.no_fingerprint_title), errorMsg, true, getResources().getString(R.string.okay), null, new DialogUtils.CustomDialogListener() {
                            @Override
                            public void onDialogActionDone(int action) {

                            }
                        });
                        sessionManager.setEnableAppLock(false);
                        fingerprintSwitch.setChecked(false);
                    }
                } else {
                    sessionManager.setEnableAppLock(false);
                    Toast.makeText(MyProfileActivity.this, getResources().getString(R.string.fingerprint_lock_disabled), Toast.LENGTH_LONG).show();
                }

            }
        });

/*
        rgGroupGender.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton checkedRadioButton = group.findViewById(checkedId);
            boolean isChecked = checkedRadioButton.isChecked();
            if (isChecked) {
                String selectedGenderText = checkedRadioButton.getText().toString();
                myProfilePOJO.setNewGender(String.valueOf(selectedGenderText.charAt(0)));

                switch (selectedGenderText) {
                    case "Male":
                        selectedGender = "M";
                        rbMale.setButtonDrawable(getDrawable(R.drawable.ui2_ic_selected_green));
                        rbFemale.setButtonDrawable(getDrawable(R.drawable.ui2_ic_circle));
                        rbOther.setButtonDrawable(getDrawable(R.drawable.ui2_ic_circle));
                        break;
                    case "Female":
                        rbMale.setButtonDrawable(getDrawable(R.drawable.ui2_ic_circle));
                        rbFemale.setButtonDrawable(getDrawable(R.drawable.ui2_ic_selected_green));
                        rbOther.setButtonDrawable(getDrawable(R.drawable.ui2_ic_circle));
                        selectedGender = "F";
                        break;
                    case "Other":
                        rbMale.setButtonDrawable(getDrawable(R.drawable.ui2_ic_circle));
                        rbFemale.setButtonDrawable(getDrawable(R.drawable.ui2_ic_circle));
                        rbOther.setButtonDrawable(getDrawable(R.drawable.ui2_ic_selected_green));
                        selectedGender = "O";
                        break;
                }


            }
        });
*/

        layoutChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangePasswordActivity_New.class);
            startActivity(intent);
        });

        btnSave.setOnClickListener(v -> {
            CustomLog.i("Btn Save", ": Clicked");
            hideSoftKeyboard(MyProfileActivity.this, btnSave);
            checkInternetAndUpdateProfile();
        });

/*
        tvDob.setOnClickListener(v -> {
            CustomCalendarViewUI2 customCalendarViewUI2 = new CustomCalendarViewUI2(MyProfileActivity.this, MyProfileActivity.this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                customCalendarViewUI2.showDatePicker(MyProfileActivity.this, "");
            }
        });
*/

        tvChangePhoto.setOnClickListener(v -> checkPerm());

        userFeedbackMsg();


        etFirstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    if (TextUtils.isEmpty(etFirstName.getText().toString())) {
                        tvErrorFirstName.setVisibility(View.VISIBLE);
                        etFirstName.setBackground(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));

                        return;
                    } else {
                        tvErrorFirstName.setVisibility(View.GONE);
                        etFirstName.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));

                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                myProfilePOJO.setNewFirstName(s.toString());
            }
        });

        etLastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                myProfilePOJO.setNewLastName(s.toString());
                if (TextUtils.isEmpty(etLastName.getText().toString())) {
                    tvErrorLastName.setVisibility(View.VISIBLE);
                    etLastName.setBackground(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));

                    return;
                } else {
                    tvErrorLastName.setVisibility(View.GONE);
                    etLastName.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));

                }
            }
        });

        etMobileNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                myProfilePOJO.setNewPhoneNumber(s.toString());
                shouldActivateSaveButton();
                if (TextUtils.isEmpty(etMobileNo.getText().toString())) {
                    tvErrorMobileNo.setVisibility(View.VISIBLE);
                    etMobileNo.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                    return;
                } else {
                    tvErrorMobileNo.setVisibility(View.GONE);
                    etMobileNo.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
                }
            }
        });

        etMiddleName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                myProfilePOJO.setNewMiddleName(s.toString());
            }
        });


        countryCodePicker.setOnCountryChangeListener(() -> {
            myProfilePOJO.setNewCountryCode(countryCodePicker.getSelectedCountryCodeWithPlus());
            shouldActivateSaveButton();
        });

        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                myProfilePOJO.setNewEmail(s.toString());
                shouldActivateSaveButton();

            }
        });

        fetchUserDetails();
    }

    private void showSnackBarAndRemoveLater(String appLanguage, int iconResId) {
        ImageView ivAlertIcon = findViewById(R.id.snackbar_icon);
        ivAlertIcon.setImageDrawable(ContextCompat.getDrawable(context, iconResId));
        snackbar_cv.setVisibility(View.VISIBLE);
        snackbar_text.setText(appLanguage);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                snackbar_cv.setVisibility(View.GONE);
            }
        }, 4000);
    }

    private void userFeedbackMsg() {
        etUsername.setOnClickListener(v -> {
            showSnackBarAndRemoveLater(getResources().getString(R.string.please_contact_your_system_administrator_to_change_these_profile_details), R.drawable.survey_snackbar_icon);
        });
        etFirstName.setOnClickListener(v -> {
            showSnackBarAndRemoveLater(getResources().getString(R.string.please_contact_your_system_administrator_to_change_these_profile_details), R.drawable.survey_snackbar_icon);
        });
        etMiddleName.setOnClickListener(v -> {
            showSnackBarAndRemoveLater(getResources().getString(R.string.please_contact_your_system_administrator_to_change_these_profile_details), R.drawable.survey_snackbar_icon);
        });
        etLastName.setOnClickListener(v -> {
            showSnackBarAndRemoveLater(getResources().getString(R.string.please_contact_your_system_administrator_to_change_these_profile_details), R.drawable.survey_snackbar_icon);
        });
        rgGroupGender.setOnClickListener(v -> {
            showSnackBarAndRemoveLater(getResources().getString(R.string.please_contact_your_system_administrator_to_change_these_profile_details), R.drawable.survey_snackbar_icon);
        });
        rbMale.setOnClickListener(v -> {
            showSnackBarAndRemoveLater(getResources().getString(R.string.please_contact_your_system_administrator_to_change_these_profile_details), R.drawable.survey_snackbar_icon);
        });
        rbFemale.setOnClickListener(v -> {
            showSnackBarAndRemoveLater(getResources().getString(R.string.please_contact_your_system_administrator_to_change_these_profile_details), R.drawable.survey_snackbar_icon);
        });
        rbOther.setOnClickListener(v -> {
            showSnackBarAndRemoveLater(getResources().getString(R.string.please_contact_your_system_administrator_to_change_these_profile_details), R.drawable.survey_snackbar_icon);
        });
        tvDob.setOnClickListener(v -> {
            showSnackBarAndRemoveLater(getResources().getString(R.string.please_contact_your_system_administrator_to_change_these_profile_details), R.drawable.survey_snackbar_icon);
        });
        tvAge.setOnClickListener(v -> {
            showSnackBarAndRemoveLater(getResources().getString(R.string.please_contact_your_system_administrator_to_change_these_profile_details), R.drawable.survey_snackbar_icon);
        });

    }

    private void checkInternetAndUpdateProfile() {
        if (NetworkConnection.isOnline(MyProfileActivity.this)) {
            if (isValidData()) {
                if (personUuid == null || personUuid.isEmpty()) {
                    Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    updateDetails();
                }
            }
        } else {
            MaterialAlertDialogBuilder builder = new DialogUtils().showErrorDialogWithTryAgainButton(this, ContextCompat.getDrawable(context, R.drawable.ui2_icon_logging_in), getString(R.string.network_failure), getString(R.string.profile_update_requires_internet), getString(R.string.try_again));
            AlertDialog networkFailureDialog = builder.show();

            networkFailureDialog.getWindow().setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg); // show rounded corner for the dialog
            networkFailureDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);   // dim backgroun
            int width = MyProfileActivity.this.getResources().getDimensionPixelSize(R.dimen.internet_dialog_width);    // set width to your dialog.
            networkFailureDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);

            Button tryAgainButton = networkFailureDialog.findViewById(R.id.positive_btn);
            if (tryAgainButton != null) tryAgainButton.setOnClickListener(v -> {
                networkFailureDialog.dismiss();
                checkInternetAndUpdateProfile();
            });
        }
    }

    private boolean isValidData() {
        if (etEmail.getText() != null && etEmail.getText().length() > 0) {
            String email = etEmail.getText().toString();
            if (!ValidatorUtils.isValidEmail(email)) {
                showSnackBarAndRemoveLater(getString(R.string.error_invalid_email), R.drawable.fingerprint_dialog_error);
                return false;
            }
        }

        if (etMobileNo.getText() != null && etMobileNo.getText().length() > 0) {
            String mobile = etMobileNo.getText().toString();
            if (mobile.length() != 10) {
                showSnackBarAndRemoveLater(getString(R.string.invalid_mobile_no), R.drawable.fingerprint_dialog_error);
                return false;
            }
        }
        return true;
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

    private void updateDetails() {

        Integer updatedAge = Integer.parseInt(tvAge.getText().toString());
        String updatedDOB = tvDob.getText().toString();
        String formattedDOB = dobToDb + "T00:00:00.000+0530";
        String updatedPhoneNum = etMobileNo.getText().toString();
        String updatedEmailID = etEmail.getText().toString();
        String updatedCountryCode = countryCodePicker.getSelectedCountryCode();
        boolean profileUpdated = false;
        if (!updatedDOB.equalsIgnoreCase(prevDOB)) {
            updateDOB(updatedAge, formattedDOB, gender);
        }
        if (prevPhoneNum == null && phoneAttributeUuid == null && !updatedPhoneNum.trim().equalsIgnoreCase("") && !updatedPhoneNum.equalsIgnoreCase(prevPhoneNum)) {
            createProfileAttribute("e3a7e03a-5fd0-4e6c-b2e3-938adb3bbb37", updatedPhoneNum);
        } else if (phoneAttributeUuid != null && !updatedPhoneNum.equalsIgnoreCase(prevPhoneNum)) {
            updateProfileAttribute(phoneAttributeUuid, updatedPhoneNum);
        }

        if (prevEmail == null && emailAttributeUuid == null && !updatedEmailID.trim().equalsIgnoreCase("") && !updatedEmailID.equalsIgnoreCase(prevEmail)) {
            createProfileAttribute("226c0494-d67e-47b4-b7ec-b368064844bd", updatedEmailID);
        } else if (emailAttributeUuid != null && !updatedEmailID.equalsIgnoreCase(prevEmail)) {
            updateProfileAttribute(emailAttributeUuid, updatedEmailID);
        }

        if (prevCountryCode == null && countryCodeAttributeUuid == null && !updatedCountryCode.trim().equalsIgnoreCase("") && !updatedCountryCode.equalsIgnoreCase(prevCountryCode)) {
            createProfileAttribute("2d4d8e6d-21c4-4710-a3ad-4daf5c0dfbbb", updatedCountryCode);
        } else if (countryCodeAttributeUuid != null && !updatedCountryCode.equalsIgnoreCase(prevCountryCode)) {
            updateProfileAttribute(countryCodeAttributeUuid, updatedCountryCode);
        }

        Intent i = new Intent(this, HomeScreenActivity_New.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("intentTag", "profile updated");
        startActivity(i);
    }

    private void createProfileAttribute(String attributeTypeUuid, String newValue) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ProfileCreateAttribute inputModel = new ProfileCreateAttribute(newValue, attributeTypeUuid);

        ApiClient.changeApiBaseUrl(BuildConfig.SERVER_URL);
        ApiInterface apiService = ApiClient.createService(ApiInterface.class);
        Observable<ResponseBody> profileAttributeCreateRequest = apiService.PROFILE_ATTRIBUTE_CREATE(sessionManager.getProviderID(), inputModel, "Basic " + sessionManager.getEncoded());
        profileAttributeCreateRequest.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new DisposableObserver<ResponseBody>() {
            @Override
            public void onNext(ResponseBody responseBody) {
                Logger.logD(TAG, responseBody.toString());
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                Logger.logD(TAG, e.getMessage());
            }

            @Override
            public void onComplete() {
                Logger.logD(TAG, "completed");
            }
        });
    }

    private void updateProfileAttribute(String attributeTypeUuid, String newValue) {
        Timber.tag(TAG).d("Mobile:%s", newValue);
        Timber.tag(TAG).d("Attributes:%s", attributeTypeUuid);
        String serverUrl = BuildConfig.SERVER_URL + "/openmrs/ws/rest/v1/provider/" + sessionManager.getProviderID() + "/"; //${target_provider_uuid}/attribute/${target_provider_attribute_uuid}
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ProfileUpdateAttribute inputModel = new ProfileUpdateAttribute(newValue);

        ApiClient.changeApiBaseUrl(serverUrl);
        ApiInterface apiService = ApiClient.createService(ApiInterface.class);
        Observable<ResponseBody> profileAttributeUpdateRequest = apiService.PROFILE_ATTRIBUTE_UPDATE(attributeTypeUuid, inputModel, "Basic " + sessionManager.getEncoded());
        profileAttributeUpdateRequest.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new DisposableObserver<ResponseBody>() {
            @Override
            public void onNext(ResponseBody responseBody) {
                Logger.logD(TAG, responseBody.toString());
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                Logger.logD(TAG, e.getMessage());
            }

            @Override
            public void onComplete() {
                Logger.logD(TAG, "completed");
            }
        });
    }

    private void updateDOB(Integer updatedAge, String updatedDOB, String gender) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ProfileUpdateAge inputModel = new ProfileUpdateAge(updatedAge, updatedDOB, gender);

        ApiClient.changeApiBaseUrl(BuildConfig.SERVER_URL);
        ApiInterface apiService = ApiClient.createService(ApiInterface.class);
        Observable<ResponseBody> profileAgeUpdateRequest = apiService.PROFILE_AGE_UPDATE(personUuid, inputModel, "Basic " + sessionManager.getEncoded());
        profileAgeUpdateRequest.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new DisposableObserver<ResponseBody>() {
            @Override
            public void onNext(ResponseBody responseBody) {
                Logger.logD(TAG, responseBody.toString());
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                Logger.logD(TAG, e.getMessage());
            }

            @Override
            public void onComplete() {
                Logger.logD(TAG, "completed");
            }
        });
    }

    private boolean checkFingerprintSensor() {
        boolean isFingerPrintAvailable = true;
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                errorMsg = getResources().getString(R.string.no_fingerprint_sensor_dialog);
                isFingerPrintAvailable = false;
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                errorMsg = getResources().getString(R.string.fingerprint_not_working_dialog);
                isFingerPrintAvailable = false;
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                errorMsg = getResources().getString(R.string.no_fingerprint_assigned_dialog);
                isFingerPrintAvailable = false;
                break;
        }
        return isFingerPrintAvailable;
    }

    private void setMobileNumberLimit() {
        mSelectedCountryCode = countryCodePicker.getSelectedCountryCode();
        if (mSelectedCountryCode.equals("91")) {
            mSelectedMobileNumberValidationLength = 10;
        }
        etMobileNo.setInputType(InputType.TYPE_CLASS_NUMBER);
        InputFilter inputFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                return null;
            }
        };

        etMobileNo.setFilters(new InputFilter[]{inputFilter, new InputFilter.LengthFilter(mSelectedMobileNumberValidationLength)});
    }

    private void fetchUserDetailsIfAdded() {
        try {

            ProviderDAO providerDAO = new ProviderDAO();
            ProviderDTO providerDTO = providerDAO.getLoginUserDetails(sessionManager.getProviderID());

            etUsername.setText(sessionManager.getChwname());

            etFirstName.setText(providerDTO.getGivenName());
            myProfilePOJO.setFirstName(providerDTO.getGivenName());

            etLastName.setText(providerDTO.getFamilyName());
            myProfilePOJO.setLastName(providerDTO.getFamilyName());

            etEmail.setText(providerDTO.getEmailId());
            myProfilePOJO.setEmail(providerDTO.getEmailId());

            etMiddleName.setText(providerDTO.getMiddle_name());
            myProfilePOJO.setMiddleName(providerDTO.getMiddle_name());

            tvDob.setText(DateAndTimeUtils.getDisplayDateForApp(providerDTO.getDateofbirth()));
            myProfilePOJO.setDateOfBirth(DateAndTimeUtils.getDisplayDateForApp(providerDTO.getDateofbirth()));

            //for updating in db
            dobToDb = providerDTO.getDateofbirth();

            String age = DateAndTimeUtils.getAge_FollowUp(providerDTO.getDateofbirth(), this);
            tvAge.setText(age);

            String phoneWithCountryCode = providerDTO.getCountryCode() + providerDTO.getTelephoneNumber();
            countryCodePicker.setFullNumber(phoneWithCountryCode); // automatically assigns cc to spinner and number to edittext field.
            myProfilePOJO.setPhoneNumber(providerDTO.getTelephoneNumber());
            myProfilePOJO.setCountryCode(providerDTO.getCountryCode());

            String gender = providerDTO.getGender();
            myProfilePOJO.setGender(providerDTO.getGender());

            if (gender != null && !gender.isEmpty()) {

                if (gender.equalsIgnoreCase("m")) {
                    rbMale.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.ui2_ic_selected_green));
                    rbFemale.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.ui2_ic_circle));
                    rbOther.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.ui2_ic_circle));

                } else if (gender.equalsIgnoreCase("f")) {
                    rbMale.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.ui2_ic_circle));
                    rbFemale.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.ui2_ic_selected_green));
                    rbOther.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.ui2_ic_circle));
                } else if (gender.equalsIgnoreCase("o")) {
                    rbMale.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.ui2_ic_circle));
                    rbFemale.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.ui2_ic_circle));
                    rbOther.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.ui2_ic_selected_green));
                }
            }

            if (providerDTO.getImagePath() != null && !providerDTO.getImagePath().isEmpty()) {
                bindProfilePictureToUI(providerDTO.getImagePath());
            } else {
                ivProfileImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.avatar1));
            }

            CustomLog.d(TAG, "fetchUserDetailsIfAdded: path : " + providerDTO.getImagePath());
            if (providerDTO.getImagePath() == null || providerDTO.getImagePath().equalsIgnoreCase("")) {
                if (NetworkConnection.isOnline(this)) {
                    profilePicDownloaded(providerDTO);
                }
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

    private AlertDialog mImagePickerAlertDialog;

    private final ActivityResultLauncher<Intent> cameraIntentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        //Timber.tag(TAG).d("Camera result=>%s", new Gson().toJson(result));
        if (result.getResultCode() == RESULT_OK) {
            if (result.getData() != null) captureImage(result.getData());
        }
    });

    private final ActivityResultLauncher<Intent> galleryIntentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        //Timber.tag(TAG).d("Gallery result=>%s", new Gson().toJson(result));
        if (result.getResultCode() == RESULT_OK) {
            if (result.getData() != null) pickImage(result.getData());
        }
    });

    private void bindProfilePictureToUI(String url) {
        RequestBuilder<Drawable> requestBuilder = Glide.with(this).asDrawable().sizeMultiplier(0.25f);
        Glide.with(MyProfileActivity.this).load(new File(url)).thumbnail(requestBuilder).centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(ivProfileImage);
    }

    private void captureImage(Intent data) {
        String mCurrentPhotoPath = data.getStringExtra("RESULT");
        bindProfilePictureToUI(mCurrentPhotoPath);
        saveImage(mCurrentPhotoPath);
    }

    private void pickImage(Intent data) {
        if (data != null) {
            try {
                Uri selectedImage = data.getData();
                String[] filePath = {MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                //Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
                CustomLog.v("path", picturePath + "");

                // copy & rename the file
                String finalImageName = UUID.randomUUID().toString();
                final String finalFilePath = AppConstants.IMAGE_PATH + finalImageName + ".jpg";

                Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream);
                ivProfileImage.invalidate();

                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        //run on ui thread
                        runOnUiThread(() -> bindProfilePictureToUI(finalFilePath));
                    }
                };
                thread.start();

                BitmapUtils.copyFile(picturePath, finalFilePath);
                compressImageAndSave(finalFilePath);
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

    private void selectImage() {
        mImagePickerAlertDialog = DialogUtils.showCommonImagePickerDialog(this, getString(R.string.select_image_hdr), new DialogUtils.ImagePickerDialogListener() {
            @Override
            public void onActionDone(int action) {
                mImagePickerAlertDialog.dismiss();
                if (action == DialogUtils.ImagePickerDialogListener.CAMERA) {
                    Intent cameraIntent = new Intent(MyProfileActivity.this, CameraActivity.class);
                    String imageName = UUID.randomUUID().toString();
                    cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
                    cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, AppConstants.IMAGE_PATH);
                    cameraIntentLauncher.launch(cameraIntent);

                } else if (action == DialogUtils.ImagePickerDialogListener.GALLERY) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    galleryIntentLauncher.launch(intent);
                }
            }
        });
        /*final CharSequence[] options = {getString(R.string.take_photo), getString(R.string.choose_from_gallery), getString(R.string.cancel)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_profile_image);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    Intent cameraIntent = new Intent(MyProfileActivity.this, CameraActivity.class);
                    String imageName = UUID.randomUUID().toString();
                    cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
                    cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, AppConstants.IMAGE_PATH);
                    startActivityForResult(cameraIntent, CameraActivity.TAKE_IMAGE);

                } else if (item == 1) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, PICK_IMAGE_FROM_GALLERY);
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();*/
    }

    private void updateProfileDetailsToLocalDb() throws DAOException {

        if (areInputFieldsValid()) {
            String selectedCode = countryCodePicker.getSelectedCountryCodeWithPlus();
            ProviderDAO providerDAO = new ProviderDAO();
            ProviderDTO providerDTO = providerDAO.getLoginUserDetails(sessionManager.getProviderID());
            if (providerDTO != null) {
                ProviderDTO inputDTO = new ProviderDTO(providerDTO.getRole(), providerDTO.getUseruuid(), etEmail.getText().toString().trim(), etMobileNo.getText().toString().trim(), providerDTO.getProviderId(), etFirstName.getText().toString().trim(), etLastName.getText().toString().trim(), providerDTO.getVoided(), selectedGender, dobToDb, providerDTO.getUuid(), providerDTO.getIdentifier(), selectedCode, etMiddleName.getText().toString().trim());

                String imagePath = "";
                if (profileImagePAth != null && !profileImagePAth.isEmpty()) {
                    imagePath = profileImagePAth;
                } else {
                    imagePath = providerDTO.getImagePath();
                }

                if (imagePath != null && !imagePath.isEmpty()) inputDTO.setImagePath(imagePath);

                try {
                    boolean isUpdated = providerDAO.updateProfileDetails(inputDTO);
                    if (isUpdated)
                        snackbarUtils.showSnackLinearLayoutParentSuccess(this, layoutParent, getResources().getString(R.string.profile_details_updated_new), true);
                    SyncDAO syncDAO = new SyncDAO();
                    syncDAO.pushDataApi();

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Intent intent = new Intent(MyProfileActivity.this, HomeScreenActivity_New.class);
                            //  startActivity(intent);
                        }
                    }, 2000);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    void compressImageAndSave(final String filePath) {
        getBackgroundHandler().post(() -> {
            boolean flag = BitmapUtils.fileCompressed(filePath);
            runOnUiThread(() -> {
                if (flag) {
                    saveImage(filePath);
                } else
                    Toast.makeText(MyProfileActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            });

        });

    }

    private void saveImage(String picturePath) {
        CustomLog.v("saveImage", "picturePath = " + picturePath);
        File photo = new File(picturePath);
        if (photo.exists()) {
            try {

                long length = photo.length();
                length = length / 1024;
                CustomLog.e("------->>>>", length + "");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("File not found : " + e.getMessage() + e);
            }

            //   recyclerViewAdapter.add(new DocumentObject(photo.getName(), photo.getAbsolutePath()));
            //   updateProfileImage(StringUtils.getFileNameWithoutExtension(photo));

        } else {
        }
        updateProfileImage(picturePath);


    }

    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    private void updateProfileImage(String imagePath) {
        //update profile image to local db after its selected
        profileImagePAth = imagePath;
        ProviderDAO providerProfileDao = new ProviderDAO();
        try {
            boolean isUpdated = providerProfileDao.updateLoggedInUserProfileImage(imagePath, sessionManager.getProviderID());
            if (isUpdated) {
                snackbarUtils.showSnackLinearLayoutParentSuccess(this, layoutParent, getResources().getString(R.string.profile_photo_updated_new), true);

            }
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        if (NetworkConnection.isOnline(MyProfileActivity.this)) {
            ImagesPushDAO imagesPushDAO = new ImagesPushDAO();
            imagesPushDAO.loggedInUserProfileImagesPush();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CameraActivity.TAKE_IMAGE) {
            if (resultCode == RESULT_OK) {
                String mCurrentPhotoPath = data.getStringExtra("RESULT");
                RequestBuilder<Drawable> requestBuilder = Glide.with(MyProfileActivity.this).asDrawable().sizeMultiplier(0.3f);
                Glide.with(MyProfileActivity.this).load(new File(mCurrentPhotoPath)).thumbnail(requestBuilder).centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(ivProfileImage);

                saveImage(mCurrentPhotoPath);
            }
        } else if (requestCode == PICK_IMAGE_FROM_GALLERY) {
            if (data != null) {
                try {
                    Uri selectedImage = data.getData();
                    String[] filePath = {MediaStore.Images.Media.DATA};
                    Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                    c.moveToFirst();
                    int columnIndex = c.getColumnIndex(filePath[0]);
                    String picturePath = c.getString(columnIndex);
                    c.close();
                    //Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
                    CustomLog.v("path", picturePath + "");

                    // copy & rename the file
                    String finalImageName = UUID.randomUUID().toString();
                    final String finalFilePath = AppConstants.IMAGE_PATH + finalImageName + ".jpg";

                    Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream);
                    ivProfileImage.invalidate();

                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() //run on ui thread
                            {
                                public void run() {
                                    RequestBuilder<Drawable> requestBuilder = Glide.with(MyProfileActivity.this).asDrawable().sizeMultiplier(0.3f);
                                    Glide.with(MyProfileActivity.this).load(finalFilePath).thumbnail(requestBuilder).centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(ivProfileImage);
                                }
                            });
                        }
                    };
                    thread.start();

                    BitmapUtils.copyFile(picturePath, finalFilePath);
                    compressImageAndSave(finalFilePath);
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }
    }

    private void checkPerm() {
        if (checkAndRequestPermissions()) {
            selectImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GROUP_PERMISSION_REQUEST) {
            boolean allGranted = grantResults.length != 0;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                checkPerm();
            } else {
                showPermissionDeniedAlert(permissions);
            }

        }
    }

    private void showPermissionDeniedAlert(String[] permissions) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(MyProfileActivity.this);

        // AlertDialog.Builder alertdialogBuilder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        alertdialogBuilder.setMessage(R.string.reject_permission_results);
        alertdialogBuilder.setPositiveButton(R.string.retry_again, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                checkPerm();
            }
        });
        alertdialogBuilder.setNegativeButton(R.string.ok_close_now, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.show();

        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);

        positiveButton.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
        //positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        negativeButton.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
        //negativeButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        IntelehealthApplication.setAlertDialogCustomTheme(this, alertDialog);
    }

    private boolean checkAndRequestPermissions() {
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), GROUP_PERMISSION_REQUEST);
            return false;
        }
        return true;
    }

    private void fetchUserDetails() {
        String uuid = new SessionManager(MyProfileActivity.this).getCreatorID();
        ProviderDAO providerDAO = new ProviderDAO();
        ProviderDTO providerDTO = null;
        try {
            providerDTO = providerDAO.getLoginUserDetails(sessionManager.getProviderID());
        } catch (DAOException e) {
            e.printStackTrace();
        }
        String url = new UrlModifiers().getHWProfileDetails(uuid);
        CustomLog.d(TAG, "profilePicDownloaded:: url : " + url);

        Observable<Profile> profileDetailsDownload = AppConstants.apiInterface.PROVIDER_PROFILE_DETAILS_DOWNLOAD(url, "Basic " + sessionManager.getEncoded());
        profileDetailsDownload.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new DisposableObserver<Profile>() {
            @Override
            public void onNext(Profile profile) {
                if (profile != null) {
                    //Timber.tag(TAG).d("Profile =>%s", new Gson().toJson(profile));
                    CustomLog.d(TAG, "fetchUserDetails: " + profile.getResults().get(0).getPerson().getPreferredName().getMiddleName());

                    personUuid = profile.getResults().get(0).getPerson().getUuid();
                    if (profile.getResults().get(0).getPerson().getPreferredName().getGivenName() != null)
                        etFirstName.setText(profile.getResults().get(0).getPerson().getPreferredName().getGivenName());
                    if (profile.getResults().get(0).getPerson().getPreferredName().getMiddleName() != null) {
                        ll_middlename.setVisibility(View.VISIBLE);
                        etMiddleName.setText(profile.getResults().get(0).getPerson().getPreferredName().getMiddleName());
                    } else {
                        ll_middlename.setVisibility(View.GONE);
                    }
                    if (profile.getResults().get(0).getPerson().getPreferredName().getFamilyName() != null)
                        etLastName.setText(profile.getResults().get(0).getPerson().getPreferredName().getFamilyName());
                    if (sessionManager.getChwname() != null)
                        etUsername.setText(sessionManager.getChwname());
                    String dob = profile.getResults().get(0).getPerson().getDateOfBirth();
                    if (dob != null) {
                        String[] split = dob.split("T");
                        tvDob.setText(DateAndTimeUtils.getDisplayDateForApp(split[0]));
                        prevDOB = DateAndTimeUtils.getDisplayDateForApp(split[0]);


                        if (sessionManager.getAppLanguage() != null && sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                            tvDob.setText(en_hi_dob_updated(DateAndTimeUtils.getDisplayDateForApp(split[0])));
                            prevDOB = en_hi_dob_updated(DateAndTimeUtils.getDisplayDateForApp(split[0]));
                        }
                        String age = DateAndTimeUtils.getAge_FollowUp(split[0], MyProfileActivity.this);
                        tvAge.setText(age);
                    }

                    gender = profile.getResults().get(0).getPerson().getGender();
                    if (gender != null && !gender.isEmpty()) {
                        if (gender.equalsIgnoreCase("m")) {
                            rbMale.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.ui2_ic_selected_green));
                            rbFemale.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.ui2_ic_circle));
                            rbOther.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.ui2_ic_circle));

                        } else if (gender.equalsIgnoreCase("f")) {
                            rbMale.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.ui2_ic_circle));
                            rbFemale.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.ui2_ic_selected_green));
                            rbOther.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.ui2_ic_circle));
                        } else if (gender.equalsIgnoreCase("o")) {
                            rbMale.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.ui2_ic_circle));
                            rbFemale.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.ui2_ic_circle));
                            rbOther.setButtonDrawable(ContextCompat.getDrawable(context, R.drawable.ui2_ic_selected_green));
                        }
                    }
                    List<PersonAttributes> personAttributes = new ArrayList<>();
                    personAttributes = profile.getResults().get(0).getAttributes();
                    if (personAttributes != null && personAttributes.size() > 0) {
                        for (int i = 0; i < personAttributes.size(); i++) {
                            String attributeName = personAttributes.get(i).getAttributeTpe().getDisplay();
                            if (attributeName.equalsIgnoreCase("phoneNumber") && !personAttributes.get(i).isVoided()) {
                                etMobileNo.setText(personAttributes.get(i).getValue().toString());
                                prevPhoneNum = personAttributes.get(i).getValue().toString();
                                phoneAttributeUuid = personAttributes.get(i).getUuid();
                            }
                            if (attributeName.equalsIgnoreCase("emailId") && !personAttributes.get(i).isVoided()) {
                                etEmail.setText(personAttributes.get(i).getValue().toString());
                                prevEmail = personAttributes.get(i).getValue().toString();
                                emailAttributeUuid = personAttributes.get(i).getUuid();
                            }
                            if (attributeName.equalsIgnoreCase("countryCode") && !personAttributes.get(i).isVoided()) {
                                if (isInteger(personAttributes.get(i).getValue())) {
                                    countryCodePicker.setCountryForPhoneCode(Integer.parseInt(personAttributes.get(i).getValue()));
                                    prevCountryCode = personAttributes.get(i).getValue().toString();
                                    countryCodeAttributeUuid = personAttributes.get(i).getUuid();
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                Logger.logD(TAG, e.getMessage());
                // need to close this activity if not able to fetch the data
                Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onComplete() {
                btnSave.setEnabled(false);
                Logger.logD(TAG, "complete");
            }
        });


        if (providerDTO != null && providerDTO.getImagePath() != null && !providerDTO.getImagePath().isEmpty()) {
            RequestBuilder<Drawable> requestBuilder = Glide.with(this).asDrawable().sizeMultiplier(0.3f);
            Glide.with(this).load(providerDTO.getImagePath()).thumbnail(requestBuilder).centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(ivProfileImage);
        } else {
            ivProfileImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.avatar1));
        }

        CustomLog.d(TAG, "fetchUserDetailsIfAdded: path : " + providerDTO.getImagePath());
        if (providerDTO.getImagePath() == null || providerDTO.getImagePath().equalsIgnoreCase("")) {
            if (NetworkConnection.isOnline(this)) {
                try {
                    profilePicDownloaded(providerDTO);
                } catch (DAOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean isInteger(String s) {
        return isInteger(s, 10);
    }

    public static boolean isInteger(String s, int radix) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (i == 0 && s.charAt(i) == '-') {
                if (s.length() == 1) return false;
                else continue;
            }
            if (Character.digit(s.charAt(i), radix) < 0) return false;
        }
        return true;
    }

    public void profilePicDownloaded(ProviderDTO providerDTO) throws DAOException {
        CustomLog.d(TAG, "profilePicDownloaded: ");
        SessionManager sessionManager = new SessionManager(MyProfileActivity.this);
        UrlModifiers urlModifiers = new UrlModifiers();
        String uuid = sessionManager.getProviderID();
        String url = urlModifiers.getProviderProfileImageUrl(uuid);
        CustomLog.d(TAG, "profilePicDownloaded:: url : " + url);


        Observable<ResponseBody> profilePicDownload = AppConstants.apiInterface.PROVIDER_PROFILE_PIC_DOWNLOAD(url, "Basic " + sessionManager.getEncoded());


        profilePicDownload.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new DisposableObserver<ResponseBody>() {
            @Override
            public void onNext(ResponseBody file) {
                CustomLog.d(TAG, "onNext: ");
                DownloadFilesUtils downloadFilesUtils = new DownloadFilesUtils();
                downloadFilesUtils.saveToDisk(file, uuid);
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                Logger.logD(TAG, e.getMessage());
            }

            @Override
            public void onComplete() {
                ProviderDAO providerDAO = new ProviderDAO();
                boolean updated = false;
                try {
                    updated = providerDAO.updateLoggedInUserProfileImage(AppConstants.IMAGE_PATH + uuid + ".jpg", sessionManager.getProviderID());

                } catch (DAOException e) {
                    e.printStackTrace();
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
                if (updated) {
                    RequestBuilder<Drawable> requestBuilder = Glide.with(MyProfileActivity.this).asDrawable().sizeMultiplier(0.3f);
                    Glide.with(MyProfileActivity.this).load(AppConstants.IMAGE_PATH + uuid + ".jpg").thumbnail(requestBuilder).centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(ivProfileImage);
                }
                ImagesDAO imagesDAO = new ImagesDAO();
                boolean isImageDownloaded = false;
                try {
                    isImageDownloaded = imagesDAO.updateLoggedInUserProfileImage(AppConstants.IMAGE_PATH + uuid + ".jpg", sessionManager.getProviderID());

                } catch (DAOException e) {
                    e.printStackTrace();
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }
        });
    }

    @Override
    public void getSelectedDate(String selectedDate, String whichDate) {
        CustomLog.d(TAG, "getSelectedDate: selectedDate from interface : " + selectedDate);
        String dateToshow1 = DateAndTimeUtils.getDateWithDayAndMonthFromDDMMFormat(selectedDate);
        if (!selectedDate.isEmpty()) {
            dobToDb = DateAndTimeUtils.convertDateToYyyyMMddFormat(selectedDate);

            String dateForAge = selectedDate;
            //dobToDb = dateForAge.replace("/","-");
            String age = DateAndTimeUtils.getAge_FollowUp(DateAndTimeUtils.convertDateToYyyyMMddFormat(selectedDate), this);
            //for age
            CustomLog.d(TAG, "getSelectedDate: date : " + DateAndTimeUtils.convertDateToYyyyMMddFormat(selectedDate));
            String[] splitedDate = selectedDate.split("/");

            CustomLog.d(TAG, "getSelectedDate: age : " + age);
            if (age != null && !age.isEmpty() && Integer.parseInt(age) >= 18) {
                tvAge.setText(age);
                tvDob.setText(dateToshow1 + ", " + splitedDate[2]);
                if (sessionManager.getAppLanguage() != null && sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                    tvDob.setText(en_hi_dob_updated(dateToshow1) + ", " + splitedDate[2]);
                }else if(sessionManager.getAppLanguage() != null && sessionManager.getAppLanguage().equalsIgnoreCase("ru")){
                    tvDob.setText(en_ru_dob_three(dateToshow1) + ", " + splitedDate[2]);
                }
                myProfilePOJO.setNewDateOfBirth(dateToshow1 + ", " + splitedDate[2]);
                if (tvErrorDob.getVisibility() == View.VISIBLE) tvErrorDob.setVisibility(View.GONE);
                shouldActivateSaveButton();
                CustomLog.d(TAG, "getSelectedDate: " + dateToshow1 + ", " + splitedDate[2]);
            } else if (age != null && !age.isEmpty() && Integer.parseInt(age) < 18) {
                tvAge.setText("");
                tvDob.setText("");
                tvErrorDob.setVisibility(View.VISIBLE);
                btnSave.setEnabled(false);

            } else {
                tvAge.setText("");
                tvDob.setText("");

            }


        } else {
            CustomLog.d(TAG, "onClick: date empty");
        }
    }

    private boolean areInputFieldsValid() {
        boolean result = false;
        String firstName = etFirstName.getText().toString();
        String lastName = etLastName.getText().toString();
        String mobileNo = etMobileNo.getText().toString();

        if (TextUtils.isEmpty(firstName)) {
            result = false;
            tvErrorFirstName.setVisibility(View.VISIBLE);
            etFirstName.setBackground(ContextCompat.getDrawable(this, R.drawable.input_field_error_bg_ui2));

        } else if (TextUtils.isEmpty(lastName)) {
            result = false;
            tvErrorLastName.setVisibility(View.VISIBLE);
            etLastName.setBackground(ContextCompat.getDrawable(this, R.drawable.input_field_error_bg_ui2));

        } else if (TextUtils.isEmpty(mobileNo)) {
            result = false;
            tvErrorMobileNo.setVisibility(View.VISIBLE);
            etMobileNo.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.input_field_error_bg_ui2));

        } else {
            etFirstName.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_input_fieldnew));
            etLastName.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_input_fieldnew));
            etMobileNo.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_input_fieldnew));

            result = true;
        }

        return result;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //register receiver for internet check
        networkUtils.callBroadcastReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            //unregister receiver for internet check
            networkUtils.unregisterNetworkReceiver();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateUIForInternetAvailability(boolean isInternetAvailable) {
        if (isInternetAvailable) {
            ivIsInternet.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ui2_ic_internet_available));

        } else {
            ivIsInternet.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ui2_ic_no_internet));

        }
    }

    private void shouldActivateSaveButton() {
//        boolean hasDataChanged = myProfilePOJO.hasDataChanged();
        btnSave.setEnabled(true);
    }

    public static void hideSoftKeyboard(Activity activity, View view) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }
}