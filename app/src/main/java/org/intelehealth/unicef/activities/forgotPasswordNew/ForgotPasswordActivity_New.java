package org.intelehealth.unicef.activities.forgotPasswordNew;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.hbb20.CountryCodePicker;

import org.intelehealth.unicef.R;
import org.intelehealth.unicef.activities.base.LocalConfigActivity;
import org.intelehealth.unicef.app.AppConstants;
import org.intelehealth.unicef.models.ForgotPasswordApiResponseModel_New;
import org.intelehealth.unicef.models.RequestOTPParamsModel_New;
import org.intelehealth.unicef.networkApiCalls.ApiClient;
import org.intelehealth.unicef.networkApiCalls.ApiInterface;
import org.intelehealth.unicef.utilities.Logger;
import org.intelehealth.unicef.utilities.SessionManager;
import org.intelehealth.unicef.utilities.SnackbarUtils;
import org.intelehealth.unicef.widget.materialprogressbar.CustomProgressDialog;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class ForgotPasswordActivity_New extends LocalConfigActivity {
    private static final String TAG = ForgotPasswordActivity_New.class.getSimpleName();
    TextInputEditText etUsername, etMobileNo;
    CustomProgressDialog cpd;
    SessionManager sessionManager = null;
    Context context;
    LinearLayout layoutParent;
    SnackbarUtils snackbarUtils;
    ImageView imageviewBack;
    TextView tvUsernameError, tvMobileError;
    String optionSelected = "username";
    private CountryCodePicker countryCodePicker;
    private Button buttonContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_new_ui2);
        snackbarUtils = new SnackbarUtils();
        cpd = new CustomProgressDialog(ForgotPasswordActivity_New.this);
        sessionManager = new SessionManager(ForgotPasswordActivity_New.this);
        context = ForgotPasswordActivity_New.this;
        initUI();
        clickListeners();
        manageErrorFields();
    }

    private void clickListeners() {
        Button buttonUsername = findViewById(R.id.button_username);
        buttonContinue = findViewById(R.id.button_continue);
        Button buttonMobileNumber = findViewById(R.id.button_mobile_number);
        RelativeLayout layoutMobileNo = findViewById(R.id.layout_parent_mobile_no);
        LinearLayout layoutUsername = findViewById(R.id.layout_parent_username);

        imageviewBack.setOnClickListener(v -> {
            /*Intent intent = new Intent(ForgotPasswordActivity_New.this, SetupActivityNew.class);
            startActivity(intent);
            finish();*/
            onBackPressed();
        });

        buttonUsername.setOnClickListener(v -> {
            optionSelected = "username";
            etMobileNo.setText("");
            layoutMobileNo.setVisibility(View.GONE);
            layoutUsername.setVisibility(View.VISIBLE);
            tvMobileError.setVisibility(View.GONE);
            tvUsernameError.setVisibility(View.GONE);
            buttonUsername.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg_forgot_pass_ui2));
            buttonMobileNumber.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg_forgot_pass_disabled_ui2));
            etMobileNo.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
            etUsername.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));

        });
        buttonMobileNumber.setOnClickListener(v -> {
            optionSelected = "mobile";
            etUsername.setText("");
            tvMobileError.setVisibility(View.GONE);
            tvUsernameError.setVisibility(View.GONE);
            layoutUsername.setVisibility(View.GONE);
            layoutMobileNo.setVisibility(View.VISIBLE);
            buttonMobileNumber.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg_forgot_pass_ui2));
            buttonUsername.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg_forgot_pass_disabled_ui2));
            etMobileNo.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
            etUsername.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
        });

        buttonContinue.setOnClickListener(v -> {
            if (areInputFieldsValid(etUsername.getText().toString().trim(), etMobileNo.getText().toString().trim())) {
                apiCallForRequestOTP(ForgotPasswordActivity_New.this, etUsername.getText().toString().trim(),
                        etMobileNo.getText().toString().trim());
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void initUI() {

        etUsername = findViewById(R.id.edittext_username);
        etMobileNo = findViewById(R.id.edittext_mobile_number);
        layoutParent = findViewById(R.id.login_parent);
        imageviewBack = findViewById(R.id.imageview_back_forgot_password);
        tvUsernameError = findViewById(R.id.tv_username_error);
        tvMobileError = findViewById(R.id.tv_mobile_error);
        countryCodePicker = findViewById(R.id.countrycode_spinner_forgot);
        countryCodePicker.registerCarrierNumberEditText(etMobileNo); // attaches the ccp spinner with the edittext
        countryCodePicker.setNumberAutoFormattingEnabled(false);

        etUsername.addTextChangedListener(new MyWatcher(etUsername));
        etMobileNo.addTextChangedListener(new MyWatcher(etMobileNo));
        setMobileNumberLimit();
    }

    private int mSelectedMobileNumberValidationLength = 0;
    private String mSelectedCountryCode = "";

    private void setMobileNumberLimit() {
        mSelectedCountryCode = countryCodePicker.getSelectedCountryCode();
        if (mSelectedCountryCode.equals("91")) {
            mSelectedMobileNumberValidationLength = 10;
        }
        etMobileNo.setInputType(InputType.TYPE_CLASS_PHONE);
        InputFilter inputFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                return null;
            }
        };

        etMobileNo.setFilters(new InputFilter[]{inputFilter, new InputFilter.LengthFilter(mSelectedMobileNumberValidationLength)});
    }

    private class MyWatcher implements TextWatcher {
        EditText editText;

        MyWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            String val = editable.toString().trim();
            buttonContinue.setEnabled(val.length() >= 5);

        }
    }

    public void apiCallForRequestOTP(Context context, String username, String mobileNo) {
        buttonContinue.setEnabled(false);
        cpd.show();
        String serverUrl = "https://" + AppConstants.DEMO_URL + ":3004";
        Log.d(TAG, "apiCallForRequestOTP: serverUrl : " + serverUrl);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        RequestOTPParamsModel_New inputModel = new RequestOTPParamsModel_New("password", username, mobileNo, 91, "");
        ApiClient.changeApiBaseUrl(serverUrl);
        ApiInterface apiService = ApiClient.createService(ApiInterface.class);
        Observable<ForgotPasswordApiResponseModel_New> loginModelObservable = apiService.REQUEST_OTP_OBSERVABLE(inputModel);
        loginModelObservable.subscribe(new Observer<ForgotPasswordApiResponseModel_New>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ForgotPasswordApiResponseModel_New forgotPasswordApiResponseModel_new) {
                cpd.dismiss();
                if (forgotPasswordApiResponseModel_new.getSuccess()) {
                    snackbarUtils.showSnackLinearLayoutParentSuccess(ForgotPasswordActivity_New.this, layoutParent, forgotPasswordApiResponseModel_new.getMessage());
                    //Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ForgotPasswordActivity_New.this, ForgotPasswordOtpVerificationActivity_New.class);
                            intent.putExtra("userUuid", forgotPasswordApiResponseModel_new.getData().getUuid());
                            intent.putExtra("userName", username);
                            intent.putExtra("userPhoneNum", mobileNo);
                            startActivity(intent);
                            finish();
                        }
                    }, 2000);
                } else {
                    if (forgotPasswordApiResponseModel_new.getMessage().equalsIgnoreCase("Invalid username!")) {
                        etUsername.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                        tvUsernameError.setText(getResources().getString(R.string.username_not_found));
                        tvUsernameError.setVisibility(View.VISIBLE);
                    } else if (forgotPasswordApiResponseModel_new.getMessage().equalsIgnoreCase("Invalid phoneNumber!")) {
                        etMobileNo.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                        tvMobileError.setText(getResources().getString(R.string.mobile_not_registered));
                        tvMobileError.setVisibility(View.VISIBLE);
                    }
                }
                buttonContinue.setEnabled(true);
            }

            @Override
            public void onError(Throwable e) {
                Logger.logD(TAG, "Login Failure" + e.getMessage());
                e.printStackTrace();
                cpd.dismiss();
                snackbarUtils.showSnackLinearLayoutParentSuccess(context, layoutParent, getResources().getString(R.string.failed_to_send_otp));
                buttonContinue.setEnabled(true);

            }

            @Override
            public void onComplete() {
                Logger.logD(TAG, "completed");
            }
        });

    }

    private void manageErrorFields() {

        etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    if (TextUtils.isEmpty(etUsername.getText().toString())) {
                        tvUsernameError.setVisibility(View.VISIBLE);
                        etUsername.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                        return;
                    } else {
                        tvUsernameError.setVisibility(View.GONE);
                        etUsername.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
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
                if (TextUtils.isEmpty(etMobileNo.getText().toString())) {
                    tvMobileError.setVisibility(View.VISIBLE);
                    etMobileNo.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                    return;
                } else {
                    tvMobileError.setVisibility(View.GONE);
                    etMobileNo.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
                }
            }
        });
    }

    private boolean areInputFieldsValid(String username, String mobile) {
        boolean result = false;
        if (!optionSelected.isEmpty() && optionSelected.equals("username")) {
            if (TextUtils.isEmpty(username)) {
                result = false;
                tvUsernameError.setVisibility(View.VISIBLE);
                etUsername.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
            } else {
                result = true;
                etUsername.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
            }
            return result;

        } else if (!optionSelected.isEmpty() && optionSelected.equals("mobile")) {
            String code = countryCodePicker.getSelectedCountryCode();
            mobile = mobile.replace(" ", "");
            Log.v(TAG, code);
            Log.v(TAG, mobile);
            if (TextUtils.isEmpty(mobile)) {
                result = false;
                tvMobileError.setVisibility(View.VISIBLE);
                etMobileNo.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));

            } else if (code.equalsIgnoreCase("91") && mobile.trim().length() != 10) {
                result = false;
                tvMobileError.setVisibility(View.VISIBLE);
                tvMobileError.setText(getString(R.string.enter_10_digits));
                etMobileNo.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));

            } else {
                etMobileNo.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
                result = true;
            }

            return result;
        }

        return result;
    }

}