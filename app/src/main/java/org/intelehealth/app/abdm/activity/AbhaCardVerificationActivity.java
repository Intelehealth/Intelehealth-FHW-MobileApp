package org.intelehealth.app.abdm.activity;

import static org.intelehealth.app.abdm.ABDMConstant.AADHAAR_CARD_SELECTION;
import static org.intelehealth.app.abdm.ABDMConstant.ABHA_OTP_AADHAAR;
import static org.intelehealth.app.abdm.ABDMConstant.ABHA_SELECTION;
import static org.intelehealth.app.abdm.ABDMConstant.MOBILE_NUMBER_SELECTION;
import static org.intelehealth.app.abdm.ABDMConstant.SCOPE_ABHA_ADDRESS;
import static org.intelehealth.app.abdm.ABDMConstant.SCOPE_ABHA_NUMBER;
import static org.intelehealth.app.abdm.ABDMConstant.SCOPE_MOBILE;
import static org.intelehealth.app.activities.identificationActivity.IdentificationActivity_New.PAYLOAD;
import static org.intelehealth.app.utilities.DialogUtils.showOKDialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.LocaleList;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.ajalt.timberkt.Timber;

import org.intelehealth.app.R;
import org.intelehealth.app.abdm.ABDMConstant;
import org.intelehealth.app.abdm.AbhaOtpTypeDialogFragment;
import org.intelehealth.app.abdm.MobileNumberOtpVerificationDialog;
import org.intelehealth.app.abdm.model.AadharApiBody;
import org.intelehealth.app.abdm.model.AbhaCardResponseBody;
import org.intelehealth.app.abdm.model.AbhaProfileRequestBody;
import org.intelehealth.app.abdm.model.AbhaProfileResponse;
import org.intelehealth.app.abdm.model.EnrollSuggestionRequestBody;
import org.intelehealth.app.abdm.model.EnrollSuggestionResponse;
import org.intelehealth.app.abdm.model.ExistUserStatusResponse;
import org.intelehealth.app.abdm.model.MobileLoginApiBody;
import org.intelehealth.app.abdm.model.MobileLoginOnOTPVerifiedResponse;
import org.intelehealth.app.abdm.model.OTPResponse;
import org.intelehealth.app.abdm.model.OTPVerificationRequestBody;
import org.intelehealth.app.abdm.model.OTPVerificationResponse;
import org.intelehealth.app.abdm.model.TokenResponse;
import org.intelehealth.app.activities.identificationActivity.IdentificationActivity_New;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.databinding.ActivityAbhaCardVerificationBinding;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.SnackbarUtils;
import org.intelehealth.app.utilities.StringUtils;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.utilities.VerhoeffAlgorithm;
import org.intelehealth.app.utilities.WindowsUtils;
import org.intelehealth.app.widget.materialprogressbar.CustomProgressDialog;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;


public class AbhaCardVerificationActivity extends AppCompatActivity {
    private final Context context = AbhaCardVerificationActivity.this;
    public static final String TAG = AbhaCardVerificationActivity.class.getSimpleName();
    public static String SCOPE = SCOPE_MOBILE;

    private ActivityAbhaCardVerificationBinding binding;
    private String accessToken = "";
    private String abhaAuthType = ABHA_OTP_AADHAAR;
    String optionSelected = AADHAAR_CARD_SELECTION;
    //    private boolean abhaCard;
    public static final String BEARER_AUTH = "Bearer ";
    private CustomProgressDialog cpd;
    private SnackbarUtils snackbarUtils;
    private SessionManager sessionManager = null;
    private CountDownTimer countDownTimer;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAbhaCardVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowsUtils.setStatusBarColor(AbhaCardVerificationActivity.this);  // changing status bar color
        cpd = new CustomProgressDialog(context);
        snackbarUtils = new SnackbarUtils();
        sessionManager = new SessionManager(context);

        binding.ivBackArrow.setOnClickListener(v -> finish());

        // check internet - start
        if (!NetworkConnection.isOnline(context)) {    // no internet.
            showOKDialog(context, getDrawable(R.drawable.ui2_ic_warning_internet),
                    getString(R.string.error_network), getString(R.string.you_need_an_active_internet_connection_to_use_this_feature),
                    getString(R.string.ok), action -> {
                        if (action == DialogUtils.CustomDialogListener.POSITIVE_CLICK) {
                            finish();
                        }
                    });
        }
        // check internet - end

        clickListenerFor_HasABHA();

        binding.resendBtn.setOnClickListener(v -> {
            resendOtp();
            binding.otpBox.setText("");
            callGenerateTokenApi();
        });

        binding.sendOtpBtn.setOnClickListener(v -> {
            if (checkValidation()) {
                if (binding.flOtpBox.getVisibility() != View.VISIBLE) {
                    binding.flOtpBox.setVisibility(View.VISIBLE);
                    binding.rlResendOTP.setVisibility(View.VISIBLE);
                    binding.resendBtn.setPaintFlags(binding.resendBtn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                }

                if (binding.sendOtpBtn.getTag() == null) {  // ie. fresh call - sending otp.
                    resendOtp();
                    callGenerateTokenApi();
                } else {
                    // ie. otp received and making call to enrollAadhar api.
                    if (Objects.requireNonNull(binding.otpBox.getText()).toString().isEmpty()) {    // ie. OTP not entered in box.
                        snackbarUtils.showSnackLinearLayoutParentSuccess(context, binding.layoutParent,
                                StringUtils.getMessageTranslated(getString(R.string.please_enter_otp_received), sessionManager.getAppLanguage()), false);
                        return;
                    }

                    if (!binding.otpBox.getText().toString().isEmpty()) {
                        String mobileNo;

                        if (optionSelected.equalsIgnoreCase(MOBILE_NUMBER_SELECTION)) {
                            // via. mobile login
                            callOTPForMobileLoginVerificationApi((String) binding.sendOtpBtn.getTag(), binding.otpBox.getText().toString());
                        } else if (optionSelected.equalsIgnoreCase(ABHA_SELECTION)) {
                            callOTPForABHALoginVerificationApi((String) binding.sendOtpBtn.getTag(), binding.otpBox.getText().toString());
                        } else {
                            mobileNo = Objects.requireNonNull(binding.layoutHaveABHANumber.edittextMobileNumber.getText()).toString().trim();
                            callOTPForVerificationApi((String) binding.sendOtpBtn.getTag(), mobileNo, binding.otpBox.getText().toString());
                        }
                    }
                }
            }
        });

        //  resendOtp();
    }

    private void clickListenerFor_HasABHA() {
        binding.otpBox.setText("");

        binding.layoutHaveABHANumber.buttonUsername.setOnClickListener(v -> {
            optionSelected = AADHAAR_CARD_SELECTION;
            binding.layoutHaveABHANumber.llAadharMobile.setVisibility(View.VISIBLE);
            binding.layoutHaveABHANumber.layoutParentMobileNo.setVisibility(View.VISIBLE);  // show mobile no as well for aadhar as api requires it.
            binding.layoutHaveABHANumber.layoutParentUsername.setVisibility(View.VISIBLE);
            binding.layoutHaveABHANumber.tvMobileError.setVisibility(View.GONE);
            binding.layoutHaveABHANumber.tvUsernameError.setVisibility(View.GONE);
            binding.layoutHaveABHANumber.flAbhaDetails.setVisibility(View.GONE);
            binding.layoutHaveABHANumber.buttonUsername.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg_forgot_pass_ui2));
            binding.layoutHaveABHANumber.buttonMobileNumber.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg_forgot_pass_disabled_ui2));
            binding.layoutHaveABHANumber.buttonAbhaNumber.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg_forgot_pass_disabled_ui2));
            binding.layoutHaveABHANumber.edittextMobileNumber.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
            binding.layoutHaveABHANumber.edittextUsername.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
        });

        binding.layoutHaveABHANumber.buttonMobileNumber.setOnClickListener(v -> {
            optionSelected = MOBILE_NUMBER_SELECTION;
            binding.layoutHaveABHANumber.llAadharMobile.setVisibility(View.VISIBLE);
            binding.layoutHaveABHANumber.layoutParentUsername.setVisibility(View.GONE);
            binding.layoutHaveABHANumber.layoutParentMobileNo.setVisibility(View.VISIBLE);
            binding.layoutHaveABHANumber.tvMobileError.setVisibility(View.GONE);
            binding.layoutHaveABHANumber.tvUsernameError.setVisibility(View.GONE);
            binding.layoutHaveABHANumber.flAbhaDetails.setVisibility(View.GONE);
            binding.layoutHaveABHANumber.buttonMobileNumber.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg_forgot_pass_ui2));
            binding.layoutHaveABHANumber.buttonUsername.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg_forgot_pass_disabled_ui2));
            binding.layoutHaveABHANumber.buttonAbhaNumber.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg_forgot_pass_disabled_ui2));
            binding.layoutHaveABHANumber.edittextMobileNumber.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
            binding.layoutHaveABHANumber.edittextUsername.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
        });

        binding.layoutHaveABHANumber.buttonAbhaNumber.setOnClickListener(v -> {
            optionSelected = ABHA_SELECTION;
            binding.layoutHaveABHANumber.llAadharMobile.setVisibility(View.GONE);
            binding.layoutHaveABHANumber.flAbhaDetails.setVisibility(View.VISIBLE);
            binding.layoutHaveABHANumber.buttonAbhaNumber.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg_forgot_pass_ui2));
            binding.layoutHaveABHANumber.buttonUsername.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg_forgot_pass_disabled_ui2));
            binding.layoutHaveABHANumber.buttonMobileNumber.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_bg_forgot_pass_disabled_ui2));

        });

    }

    private void callGenerateTokenApi() {   // Step 1.
        cpd.show(getString(R.string.otp_sending));
        binding.sendOtpBtn.setEnabled(false);    // btn disabled.
        binding.sendOtpBtn.setTag(null);    // resetting...

        Single<TokenResponse> tokenResponse = AppConstants.apiInterface.GET_TOKEN(UrlModifiers.getABDM_TokenUrl());
        new Thread(new Runnable() {
            @Override
            public void run() {
                // api - start
                tokenResponse.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableSingleObserver<>() {
                            @Override
                            public void onSuccess(TokenResponse tokenResponse) {
                                accessToken = BEARER_AUTH + tokenResponse.getAccessToken();
                                if (!optionSelected.isEmpty() && optionSelected.equalsIgnoreCase(AADHAAR_CARD_SELECTION)) {
                                    callAadhaarVerificationApi(accessToken);   // via. aadharEnroll api
                                } else if (!optionSelected.isEmpty() && (optionSelected.equalsIgnoreCase(MOBILE_NUMBER_SELECTION))) {
                                    // call mobile api.
                                    callMobileNumberVerificationApi(accessToken);
                                } else if (!optionSelected.isEmpty() && optionSelected.equalsIgnoreCase(ABHA_SELECTION)) {
                                    cpd.dismiss();
                                    AbhaOtpTypeDialogFragment dialog = new AbhaOtpTypeDialogFragment();
                                    dialog.openAuthSelectionDialogDialog(authType -> {
                                        abhaAuthType = authType;
                                        callAbhaVerificationApi(accessToken, abhaAuthType);
                                    });
                                    dialog.show(getSupportFragmentManager(), "");
                                }

                            }

                            @Override
                            public void onError(Throwable e) {
                                Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                                Timber.tag(TAG).e("onError: callGenerateTokenApi: %s", e.toString());
                                binding.sendOtpBtn.setEnabled(true);
                                binding.sendOtpBtn.setText(R.string.send_otp);  // Send otp.
                                cancelResendAndHideView();
                                cpd.dismiss();
                            }
                        });
                // api - end
            }
        }).start();

    }

    public static String formatIntoAbhaString(String input) {
        StringBuilder result = new StringBuilder();
        int length = input.length();
        int[] groupSizes = {2, 4, 4, 4}; // The size of each group

        int startIndex = 0;
        for (int groupSize : groupSizes) {
            int endIndex = startIndex + groupSize;
            result.append(input.substring(startIndex, endIndex));
            if (endIndex < length) {
                result.append("-");
            }
            startIndex = endIndex;
        }

        return result.toString();
    }

    private void callAbhaVerificationApi(String accessToken, String authType) {  // mobile: Step 2
        cpd.show(getString(R.string.otp_sending));
        MobileLoginApiBody mobileLoginApiBody = new MobileLoginApiBody();
        String value = TextUtils.isEmpty(binding.layoutHaveABHANumber.abhaDetails.etAbhaNumber.getText()) ? Objects.requireNonNull(binding.layoutHaveABHANumber.abhaDetails.etAbhaAddress.getText()).toString() : formatIntoAbhaString(binding.layoutHaveABHANumber.abhaDetails.etAbhaNumber.getText().toString());
        mobileLoginApiBody.setValue(value); // mobile value.
        mobileLoginApiBody.setScope(TextUtils.isEmpty(binding.layoutHaveABHANumber.abhaDetails.etAbhaNumber.getText()) ? SCOPE_ABHA_ADDRESS : SCOPE_ABHA_NUMBER);
        mobileLoginApiBody.setAuthMethod(authType);
        String url = UrlModifiers.getMobileLoginVerificationUrl();
        // payload - end

        Single<OTPResponse> mobileResponseSingle = AppConstants.apiInterface.GET_OTP_FOR_MOBILE(url, accessToken, mobileLoginApiBody);
        new Thread(() -> {
            // api - start
            mobileResponseSingle
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableSingleObserver<OTPResponse>() {
                        @Override
                        public void onSuccess(OTPResponse otpResponse) {
                            cpd.dismiss();
                            snackbarUtils.showSnackLinearLayoutParentSuccess(context, binding.layoutParent,
                                    StringUtils.getMessageTranslated(otpResponse.getMessage(), sessionManager.getAppLanguage()), true);

                            Timber.tag(TAG).d("onSuccess: callMobileNumberVerificationApi: %s", otpResponse.toString());
                            // here, we will receive: txtID and otp will be received via SMS.
                            // and we need to pass to another api: otp, mobileNo and txtID will go in Header.

                            if (binding.flOtpBox.getVisibility() != View.VISIBLE) {
                                binding.flOtpBox.setVisibility(View.VISIBLE);
                                binding.rlResendOTP.setVisibility(View.VISIBLE);
                                binding.resendBtn.setPaintFlags(binding.resendBtn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                            }

                            binding.sendOtpBtn.setTag(otpResponse.getTxnId());
                            binding.sendOtpBtn.setText(getString(R.string.verify));
                            binding.sendOtpBtn.setEnabled(true);    // btn enabled -> since otp is received.
                        }

                        @Override
                        public void onError(Throwable e) {
                            binding.sendOtpBtn.setEnabled(true);
                            binding.sendOtpBtn.setText(R.string.send_otp);  // Send otp.
                            binding.otpBox.setText("");
                            Timber.tag(TAG).e("onError: callMobileNumberVerificationApi: %s", e.getMessage());
                            Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                            cancelResendAndHideView();
                            cpd.dismiss();
                        }
                    });
            // api - end
        }).start();
    }

    private void callMobileNumberVerificationApi(String accessToken) {  // mobile: Step 2

        MobileLoginApiBody mobileLoginApiBody = new MobileLoginApiBody();
        if (!optionSelected.isEmpty() && optionSelected.equalsIgnoreCase(ABHA_SELECTION)) {
            SCOPE = TextUtils.isEmpty(binding.layoutHaveABHANumber.abhaDetails.etAbhaNumber.getText()) ? SCOPE_ABHA_ADDRESS : SCOPE_ABHA_NUMBER;
            String value = TextUtils.isEmpty(binding.layoutHaveABHANumber.abhaDetails.etAbhaNumber.getText()) ? Objects.requireNonNull(binding.layoutHaveABHANumber.abhaDetails.etAbhaAddress.getText()).toString() : formatIntoAbhaString(binding.layoutHaveABHANumber.abhaDetails.etAbhaNumber.getText().toString());
            mobileLoginApiBody.setValue(value); // mobile value.
        } else {
            SCOPE = SCOPE_MOBILE;
            mobileLoginApiBody.setValue(Objects.requireNonNull(binding.layoutHaveABHANumber.edittextMobileNumber.getText()).toString().trim()); // mobile value.
        }

        mobileLoginApiBody.setScope(SCOPE);
        String url = UrlModifiers.getMobileLoginVerificationUrl();
        // payload - end

        Single<OTPResponse> mobileResponseSingle = AppConstants.apiInterface.GET_OTP_FOR_MOBILE(url, accessToken, mobileLoginApiBody);
        new Thread(() -> {
            // api - start
            mobileResponseSingle
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableSingleObserver<OTPResponse>() {
                        @Override
                        public void onSuccess(OTPResponse otpResponse) {
                            cpd.dismiss();
                            snackbarUtils.showSnackLinearLayoutParentSuccess(context, binding.layoutParent,
                                    StringUtils.getMessageTranslated(otpResponse.getMessage(), sessionManager.getAppLanguage()), true);

                            Timber.tag(TAG).d("onSuccess: callMobileNumberVerificationApi: %s", otpResponse.toString());
                            // here, we will receive: txtID and otp will be received via SMS.
                            // and we need to pass to another api: otp, mobileNo and txtID will go in Header.

                            if (binding.flOtpBox.getVisibility() != View.VISIBLE) {
                                binding.flOtpBox.setVisibility(View.VISIBLE);
                                binding.rlResendOTP.setVisibility(View.VISIBLE);
                                binding.resendBtn.setPaintFlags(binding.resendBtn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                            }

                            binding.sendOtpBtn.setTag(otpResponse.getTxnId());
                            binding.sendOtpBtn.setText(getString(R.string.verify));
                            binding.sendOtpBtn.setEnabled(true);    // btn enabled -> since otp is received.
                        }

                        @Override
                        public void onError(Throwable e) {
                            binding.sendOtpBtn.setEnabled(true);
                            binding.sendOtpBtn.setText(R.string.send_otp);  // Send otp.
                            binding.otpBox.setText("");
                            Timber.tag(TAG).e("onError: callMobileNumberVerificationApi: %s", e.getMessage());
                            Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                            cancelResendAndHideView();
                            cpd.dismiss();
                        }
                    });
            // api - end
        }).start();
    }

    private void callAadhaarVerificationApi(String accessToken) {
        // payload
        AadharApiBody aadharApiBody = new AadharApiBody();
        String aadharNo;
        aadharNo = Objects.requireNonNull(binding.layoutHaveABHANumber.edittextUsername.getText()).toString().trim();

        aadharApiBody.setScope(ABDMConstant.SCOPE_AADHAAR);
        aadharApiBody.setValue(aadharNo);
        String url = UrlModifiers.getAadharOTPVerificationUrl();

        Single<OTPResponse> responseBodySingle = AppConstants.apiInterface.GET_OTP_FOR_AADHAR(url, accessToken, aadharApiBody);
        new Thread(() -> {
            // api - start
            responseBodySingle.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableSingleObserver<OTPResponse>() {
                        @Override
                        public void onSuccess(OTPResponse otpResponse) {
                            cpd.dismiss();
                            snackbarUtils.showSnackLinearLayoutParentSuccess(context, binding.layoutParent,
                                    StringUtils.getMessageTranslated(otpResponse.getMessage(), sessionManager.getAppLanguage()), true);

                            Timber.tag(TAG).d("onSuccess: AadharResponse: %s", otpResponse.toString());
                            // here, we will receive: txtID, otp
                            // and we need to pass to another api: otp, mobileNo and txtID will go in Header.

                            if (binding.flOtpBox.getVisibility() != View.VISIBLE) {
                                binding.flOtpBox.setVisibility(View.VISIBLE);
                                binding.rlResendOTP.setVisibility(View.VISIBLE);
                                binding.resendBtn.setPaintFlags(binding.resendBtn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                            }

                            binding.sendOtpBtn.setTag(otpResponse.getTxnId());
                            binding.sendOtpBtn.setText(getString(R.string.verify));
                            binding.sendOtpBtn.setEnabled(true);    // btn enabled -> since otp is received.
                        }

                        @Override
                        public void onError(Throwable e) {
                            Timber.tag(TAG).e("onError: AadharResponse: " + e.getMessage());
                            Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                            binding.sendOtpBtn.setEnabled(true);
                            binding.sendOtpBtn.setText(R.string.send_otp);  // Send otp.
                            binding.otpBox.setText("");
                            cancelResendAndHideView();
                            cpd.dismiss();
                        }
                    });
            // api - end
        }).start();

    }

    private void callOTPForABHALoginVerificationApi(String txnId, String otp) {   // Mobile: Step 3
        cpd = new CustomProgressDialog(context);
        cpd.show(getString(R.string.verifying_otp));
        Timber.tag("callOTPForVerificationApi: ").d("parameters: " + txnId + ", " + otp);
        binding.sendOtpBtn.setEnabled(false);    // btn disabled.
        binding.sendOtpBtn.setTag(null);    // resetting...

        // payload
        String url = UrlModifiers.getOTPForMobileLoginVerificationUrl();
        OTPVerificationRequestBody requestBody = new OTPVerificationRequestBody();
        requestBody.setTxnId(txnId);
        requestBody.setOtp(otp);
        requestBody.setAuthMethod(abhaAuthType);
        requestBody.setScope(TextUtils.isEmpty(binding.layoutHaveABHANumber.abhaDetails.etAbhaNumber.getText()) ? SCOPE_ABHA_ADDRESS : SCOPE_ABHA_NUMBER);

        Single<MobileLoginOnOTPVerifiedResponse> mobileLoginOnOTPVerifiedResponseSingle =
                AppConstants.apiInterface.PUSH_OTP_FOR_MOBILE_LOGIN_VERIFICATION(url, accessToken, requestBody);
        new Thread(() -> mobileLoginOnOTPVerifiedResponseSingle
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<MobileLoginOnOTPVerifiedResponse>() {
                    @Override
                    public void onSuccess(MobileLoginOnOTPVerifiedResponse mobileLoginOnOTPVerifiedResponse) {
                        cpd.dismiss();

                        Timber.tag("callOTPForMobileLoginVerificationApi").d("onSuccess: %s", mobileLoginOnOTPVerifiedResponse.toString());
                        if (SCOPE.equalsIgnoreCase("abha-address")) {
                            String X_TOKEN = BEARER_AUTH + mobileLoginOnOTPVerifiedResponse.getToken();
                            callFetchUserProfileAPI(null, mobileLoginOnOTPVerifiedResponse.getTxnId(), X_TOKEN);
                            return;
                        }
                        if (mobileLoginOnOTPVerifiedResponse.getAccounts() != null) {
                            if (mobileLoginOnOTPVerifiedResponse.getAccounts().size() > 0) {    // ie. there is atleast one (1) account.

                                if (mobileLoginOnOTPVerifiedResponse.getAccounts().size() > 1) {
                                    // ie. there are more than 1 accounts for this mobile number than show -> Accounts selection screen.
                                    Intent intent = new Intent(context, AccountSelectionLoginActivity.class);
                                    String X_TOKEN = BEARER_AUTH + mobileLoginOnOTPVerifiedResponse.getToken();

                                    intent.putExtra("X_TOKEN", X_TOKEN);
                                    intent.putExtra("payload", mobileLoginOnOTPVerifiedResponse);
                                    intent.putExtra("accessToken", accessToken);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // ie. Only 1 account for this mobile number than call -> fetch User Profile details api.
                                    String ABHA_NUMBER = mobileLoginOnOTPVerifiedResponse.getAccounts().get(0).getABHANumber();
                                    String X_TOKEN = BEARER_AUTH + mobileLoginOnOTPVerifiedResponse.getToken();
                                    callFetchUserProfileAPI(ABHA_NUMBER, mobileLoginOnOTPVerifiedResponse.getTxnId(), X_TOKEN);
                                }

                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        cpd.dismiss();
                        Timber.tag("callOTPForMobileLoginVerificationApi").e("onError: %s", e.toString());
                        Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                        binding.sendOtpBtn.setEnabled(true);
                        binding.sendOtpBtn.setText(R.string.send_otp);  // Send otp.
                        binding.otpBox.setText("");
                        cancelResendAndHideView();
                    }
                })).start();
    }


    /**
     * Here, this function will only be called if user has ABHA number and he wants to use the login via. Mobile login flow.
     *
     * @param txnId: txnId received in success response.
     * @param otp    : otp received via. SMS
     */
    private void callOTPForMobileLoginVerificationApi(String txnId, String otp) {   // Mobile: Step 3
        cpd = new CustomProgressDialog(context);
        cpd.show(getString(R.string.verifying_otp));
        Timber.tag("callOTPForVerificationApi: ").d("parameters: " + txnId + ", " + otp);
        binding.sendOtpBtn.setEnabled(false);    // btn disabled.
        binding.sendOtpBtn.setTag(null);    // resetting...

        // payload
        String url = UrlModifiers.getOTPForMobileLoginVerificationUrl();
        OTPVerificationRequestBody requestBody = new OTPVerificationRequestBody();
        requestBody.setTxnId(txnId);
        requestBody.setOtp(otp);
        requestBody.setScope(SCOPE);
        Single<MobileLoginOnOTPVerifiedResponse> mobileLoginOnOTPVerifiedResponseSingle =
                AppConstants.apiInterface.PUSH_OTP_FOR_MOBILE_LOGIN_VERIFICATION(url, accessToken, requestBody);
        new Thread(() -> mobileLoginOnOTPVerifiedResponseSingle
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<MobileLoginOnOTPVerifiedResponse>() {
                    @Override
                    public void onSuccess(MobileLoginOnOTPVerifiedResponse mobileLoginOnOTPVerifiedResponse) {
                        cpd.dismiss();

                        Timber.tag("callOTPForMobileLoginVerificationApi").d("onSuccess: %s", mobileLoginOnOTPVerifiedResponse.toString());
                        if (SCOPE.equalsIgnoreCase("abha-address")) {
                            String X_TOKEN = BEARER_AUTH + mobileLoginOnOTPVerifiedResponse.getToken();
                            callFetchUserProfileAPI(null, mobileLoginOnOTPVerifiedResponse.getTxnId(), X_TOKEN);
                            return;
                        }
                        if (mobileLoginOnOTPVerifiedResponse.getAccounts() != null) {
                            if (mobileLoginOnOTPVerifiedResponse.getAccounts().size() > 0) {    // ie. there is atleast one (1) account.

                                if (mobileLoginOnOTPVerifiedResponse.getAccounts().size() > 1) {
                                    // ie. there are more than 1 accounts for this mobile number than show -> Accounts selection screen.
                                    Intent intent = new Intent(context, AccountSelectionLoginActivity.class);
                                    String X_TOKEN = BEARER_AUTH + mobileLoginOnOTPVerifiedResponse.getToken();

                                    intent.putExtra("X_TOKEN", X_TOKEN);
                                    intent.putExtra("payload", mobileLoginOnOTPVerifiedResponse);
                                    intent.putExtra("accessToken", accessToken);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // ie. Only 1 account for this mobile number than call -> fetch User Profile details api.
                                    String ABHA_NUMBER = mobileLoginOnOTPVerifiedResponse.getAccounts().get(0).getABHANumber();
                                    String X_TOKEN = BEARER_AUTH + mobileLoginOnOTPVerifiedResponse.getToken();
                                    callFetchUserProfileAPI(ABHA_NUMBER, mobileLoginOnOTPVerifiedResponse.getTxnId(), X_TOKEN);
                                }

                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        cpd.dismiss();
                        Timber.tag("callOTPForMobileLoginVerificationApi").e("onError: %s", e.toString());
                        Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                        binding.sendOtpBtn.setEnabled(true);
                        binding.sendOtpBtn.setText(R.string.send_otp);  // Send otp.
                        binding.otpBox.setText("");
                        cancelResendAndHideView();
                    }
                })).start();
    }

    /**
     * This api is used to call the GET Abha card api which returns a base64 encoded image.
     */
    private void callGETAbhaCardApi(String xToken, String accessToken, MobileLoginOnOTPVerifiedResponse mobileLoginOnOTPVerifiedResponse) {
        Log.d(TAG, "callGETAbhaCardApi: " + accessToken + " : " + xToken);
        String url = UrlModifiers.getABHACardUrl();
        Single<AbhaCardResponseBody> responseBodySingle = AppConstants.apiInterface.GET_ABHA_CARD(url, accessToken, xToken);
        new Thread(() -> responseBodySingle
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<AbhaCardResponseBody>() {
                    @Override
                    public void onSuccess(AbhaCardResponseBody abhaCardResponseBody) {
                        if (abhaCardResponseBody != null) {
                            Timber.tag("callGETAbhaCardApi").d("onSuccess: %s", abhaCardResponseBody.toString());

                            // TODO: here it will return base64 encoded image.
                            Intent intent = new Intent(context, AbhaCardActivity.class);
                            intent.putExtra("payload", abhaCardResponseBody);
                            intent.putExtra("data", mobileLoginOnOTPVerifiedResponse);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.tag("callGETAbhaCardApi").e("onError: %s", e.toString());
                        cpd.dismiss();
                        Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                        binding.sendOtpBtn.setEnabled(true);
                        binding.sendOtpBtn.setText(R.string.send_otp);  // Send otp.
                        binding.otpBox.setText("");
                        cancelResendAndHideView();
                    }
                })).start();
    }

    /**
     * This will call the Fetch Profile Details api to fetch the details related to this user.
     *
     * @param abhaNumber
     * @param txnId
     * @param xToken
     */
    private void callFetchUserProfileAPI(String abhaNumber, String txnId, String xToken) {
        binding.sendOtpBtn.setEnabled(false);    // btn disabled.
        binding.sendOtpBtn.setTag(null);    // resetting...

        // payload - start
        String url = UrlModifiers.getABHAProfileUrl();
        AbhaProfileRequestBody requestBody = new AbhaProfileRequestBody();
        requestBody.setTxnId(txnId);
        requestBody.setAbhaNumber(abhaNumber);
        cpd.show();

        if (!optionSelected.isEmpty() && optionSelected.equalsIgnoreCase(ABHA_SELECTION)) {
            requestBody.setScope(TextUtils.isEmpty(binding.layoutHaveABHANumber.abhaDetails.etAbhaNumber.getText()) ? SCOPE_ABHA_ADDRESS : SCOPE_ABHA_NUMBER);
        }
        // payload - end

        Single<AbhaProfileResponse> abhaProfileResponseSingle =
                AppConstants.apiInterface.PUSH_ABHA_PROFILE(url, accessToken, xToken, requestBody);
        new Thread(() -> abhaProfileResponseSingle
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<>() {
                    @Override
                    public void onSuccess(AbhaProfileResponse abhaProfileResponse) {
                        cpd.dismiss();
                        Timber.tag("callFetchUserProfileAPI").d("onSuccess: %s", abhaProfileResponse);
                        checkUserExist(abhaProfileResponse.getPreferredAbhaAddress(), abhaProfileResponse);
                    }

                    @Override
                    public void onError(Throwable e) {
                        cpd.dismiss();
                        Timber.tag("callFetchUserProfileAPI").e("onError: %s", e.toString());
                    }
                })).start();

    }

    private void checkUserExist(String abhaAddress, OTPVerificationResponse abhaProfileResponse) {

        sessionManager = new SessionManager(context);
        String encoded = sessionManager.getEncoded();
        String url = UrlModifiers.getCheckExistingUserUrl();
        cpd.show();
        // payload - end
        Single<ExistUserStatusResponse> abhaProfileResponseSingle =
                AppConstants.apiInterface.checkExistingUser(url + abhaAddress, "Basic " + encoded);
        new Thread(() -> abhaProfileResponseSingle
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<>() {
                    @Override
                    public void onSuccess(ExistUserStatusResponse response) {
                        cpd.dismiss();
                        Timber.tag("checkExistingUserAPI").d("onSuccess: %s", response);
                        Intent intent;
                        if (response != null && response.getData() != null && !Objects.requireNonNull(response.getData().getUuid()).equalsIgnoreCase("NA")) {
                            abhaProfileResponse.setOpenMrsId(response.getData().getOpenmrsid());
                            abhaProfileResponse.setUuID(response.getData().getUuid());
                            intent = new Intent(context, IdentificationActivity_New.class);
                            intent.putExtra(PAYLOAD, abhaProfileResponse);
                            intent.putExtra("accessToken", accessToken);
                            intent.putExtra("patient_detail", true);
                            startActivity(intent);
                        } else {
                            intent = new Intent(context, IdentificationActivity_New.class);
                            intent.putExtra(PAYLOAD, abhaProfileResponse);
                            intent.putExtra("accessToken", accessToken);
                            startActivity(intent);
                        }
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        cpd.dismiss();
                        Timber.tag("checkExistingUserAPI").e("onError: %s", e.toString());
                    }
                })).start();

    }

    private void checkUserExist(String abhaAddress, AbhaProfileResponse abhaProfileResponse) {

        sessionManager = new SessionManager(context);
        String encoded = sessionManager.getEncoded();
        String url = UrlModifiers.getCheckExistingUserUrl();
        cpd.show();
        // payload - end
        Single<ExistUserStatusResponse> abhaProfileResponseSingle =
                AppConstants.apiInterface.checkExistingUser(url + abhaAddress, "Basic " + encoded);
        new Thread(() -> abhaProfileResponseSingle
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<>() {
                    @Override
                    public void onSuccess(ExistUserStatusResponse response) {
                        cpd.dismiss();
                        Timber.tag("checkExistingUserAPI").d("onSuccess: %s", response);
                        Intent intent;
                        if (response != null && response.getData() != null && !Objects.requireNonNull(response.getData().getUuid()).equalsIgnoreCase("NA")) {
                            abhaProfileResponse.setOpenMrsId(response.getData().getOpenmrsid());
                            abhaProfileResponse.setUuiD(response.getData().getUuid());
                            intent = new Intent(context, IdentificationActivity_New.class);
                            intent.putExtra("mobile_payload", abhaProfileResponse);
                            intent.putExtra("accessToken", accessToken);
                            intent.putExtra("patient_detail", true);
                            startActivity(intent);
                        } else {
                            intent = new Intent(context, IdentificationActivity_New.class);
                            intent.putExtra("mobile_payload", abhaProfileResponse);
                            intent.putExtra("accessToken", accessToken);
                            startActivity(intent);
                        }
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        cpd.dismiss();
                        Timber.tag("checkExistingUserAPI").e("onError: %s", e.toString());
                    }
                })).start();

    }


    /**
     * Here, this function is used to call the EnrollByAadhar api which takes @BODY: txtId, mobileNo, otp and will return us
     * patient's details.
     *
     * @param txnId    get from aadhar card verification api
     * @param mobileNo user which enter
     * @param otp      get from aadhar card verification api
     */
    private void callOTPForVerificationApi(String txnId, String mobileNo, String otp) {
        cpd = new CustomProgressDialog(context);
        cpd.show(getString(R.string.verifying_otp));
        Timber.tag("callOTPForVerificationApi: ").d("parameters: " + txnId + ", " + mobileNo + ", " + otp);

        binding.sendOtpBtn.setEnabled(false);    // btn disabled.
        binding.sendOtpBtn.setTag(null);    // resetting...

        // payload
        String url = UrlModifiers.getOTPForVerificationUrl();
        OTPVerificationRequestBody requestBody = new OTPVerificationRequestBody();
        requestBody.setOtp(otp);
        requestBody.setTxnId(txnId);
        requestBody.setMobileNo(mobileNo);

        Single<OTPVerificationResponse> otpVerificationResponseObservable =
                AppConstants.apiInterface.PUSH_OTP_FOR_VERIFICATION(url, accessToken, requestBody);

        new Thread(() -> {
            // api - start
            otpVerificationResponseObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableSingleObserver<>() {
                        @Override
                        public void onSuccess(OTPVerificationResponse otpVerificationResponse) {
                            cpd.dismiss();
                            Timber.tag("callOTPForVerificationApi: ").d("onSuccess: %s", otpVerificationResponse.toString());

                            String mobile = otpVerificationResponse.getABHAProfile().getMobile();
                            boolean isMobileEmpty = TextUtils.isEmpty(mobile);
                            boolean isNewUser = otpVerificationResponse.getIsNew();

                            if (isMobileEmpty || !mobile.equalsIgnoreCase(mobileNo)) {
                                MobileNumberOtpVerificationDialog dialog = new MobileNumberOtpVerificationDialog();
                                dialog.openMobileNumberVerificationDialog(accessToken, otpVerificationResponse.getTxnId(), mobileNo, onMobileEnrollCompleted -> handleUserFlow(otpVerificationResponse, accessToken, isNewUser));
                                dialog.show(getSupportFragmentManager(), "");
                            } else {
                                handleUserFlow(otpVerificationResponse, accessToken, isNewUser);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            binding.sendOtpBtn.setEnabled(true);
                            binding.sendOtpBtn.setText(R.string.send_otp);  // Send otp.
                            binding.otpBox.setText("");
                            cpd.dismiss();
                            Timber.tag("callOTPForVerificationApi: ").e("onError: %s", e.toString());
                            Toast.makeText(context, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                            cancelResendAndHideView();
                        }
                    });
            // api - end
        }).start();

    }

    private void handleUserFlow(OTPVerificationResponse otpVerificationResponse, String accessToken, boolean isNewUser) {
        if (isNewUser) {
            // New user -> fetch address suggestions and navigate to ABHA address screen.
            callFetchAbhaAddressSuggestionsApi(otpVerificationResponse, accessToken);
        } else {
            // Existing user -> check user existence.
            checkUserExist(otpVerificationResponse.getABHAProfile().getPhrAddress().get(0), otpVerificationResponse);
        }
    }

    private void callFetchAbhaAddressSuggestionsApi(OTPVerificationResponse otpVerificationResponse, String accessToken) {
        ArrayList<String> addressList = new ArrayList<>();
        // api - start
        String url = UrlModifiers.getEnrollABHASuggestionUrl();
        EnrollSuggestionRequestBody body = new EnrollSuggestionRequestBody();
        body.setTxnId(otpVerificationResponse.getTxnId());

        Single<EnrollSuggestionResponse> enrollSuggestionResponseSingle =
                AppConstants.apiInterface.PUSH_ENROLL_ABHA_ADDRESS_SUGGESTION(url, accessToken, body);
        new Thread(() -> enrollSuggestionResponseSingle
                .observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<>() {
                    @Override
                    public void onSuccess(EnrollSuggestionResponse enrollSuggestionResponse) {
                        Timber.tag(TAG).d("onSuccess: suggestion: %s", enrollSuggestionResponse);
                        if (enrollSuggestionResponse.getAbhaAddressList() != null) {

                            for (String phrAddressAutoGenerated : otpVerificationResponse.getABHAProfile().getPhrAddress()) { // auto-generated abha preferred address from abdm end.
                                addressList.add(phrAddressAutoGenerated);
                            }
                            for (String phrAddress : enrollSuggestionResponse.getAbhaAddressList()) {
                                addressList.add(phrAddress);
                            }

                            if (addressList.size() > 0) {
                                Intent intent = new Intent(context, AbhaAddressSuggestionsActivity.class);
                                intent.putStringArrayListExtra("addressList", addressList);
                                intent.putExtra("payload", otpVerificationResponse);
                                intent.putExtra("accessToken", accessToken);
                                startActivity(intent);
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.tag(TAG).e("onError: suggestion%s", e.toString());
                    }
                })).start();
        // api - end

    }

    public static boolean validateAadharNumber(String aadharNumber) {
        Pattern aadharPattern = Pattern.compile("\\d{12}");
        boolean isValidAadhar = aadharPattern.matcher(aadharNumber).matches();
        if (isValidAadhar) {
            isValidAadhar = VerhoeffAlgorithm.validateVerhoeff(aadharNumber);
        }
        return isValidAadhar;
    }

    private boolean checkValidation() {
        boolean isValid;

        isValid = areInputFieldsValid_HasABHA();

        // common area...
        if (binding.flOtpBox.getVisibility() == View.VISIBLE) {
            if (binding.otpBox.getText() != null) {
                if (binding.otpBox.getText().toString().isEmpty()) {
                    Toast.makeText(context, getString(R.string.please_enter_otp_received), Toast.LENGTH_LONG).show();
                    isValid = false;
                }
            }
        }

        return isValid;
    }

    private boolean areInputFieldsValid_HasABHA() {
        boolean isvalid = true;
        if (!optionSelected.isEmpty() && optionSelected.equals(AADHAAR_CARD_SELECTION)) {

            // aadhar validation - start
            String aadharNo = binding.layoutHaveABHANumber.edittextUsername.getText().toString().replace(" ", "").trim();
            if (aadharNo.isEmpty()) {
                binding.layoutHaveABHANumber.aadharError.setVisibility(View.VISIBLE);
                binding.layoutHaveABHANumber.aadharError.setText(getString(R.string.error_field_required));
                binding.layoutHaveABHANumber.edittextUsername.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                isvalid = false;
            } else {
                if (aadharNo.length() != 12) {
                    binding.layoutHaveABHANumber.aadharError.setVisibility(View.VISIBLE);
                    binding.layoutHaveABHANumber.aadharError.setText(getString(R.string.enter_12_digits));
                    isvalid = false;
                } else if (!validateAadharNumber(binding.layoutHaveABHANumber.edittextUsername.getText().toString())) {   // Verhoeff algo. check
                    binding.layoutHaveABHANumber.aadharError.setVisibility(View.VISIBLE);
                    binding.layoutHaveABHANumber.aadharError.setText(R.string.aadhar_number_is_not_valid);
                    binding.layoutHaveABHANumber.edittextUsername.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                    isvalid = false;
                } else {
                    binding.layoutHaveABHANumber.edittextUsername.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
                    binding.layoutHaveABHANumber.aadharError.setVisibility(View.GONE);
                }
            }   // aadhar validaiton - end


            // mobile for aadhar - start
            String mobile = binding.layoutHaveABHANumber.edittextMobileNumber.getText().toString().replace(" ", "").trim();

            if (mobile.isEmpty()) {
                binding.layoutHaveABHANumber.mobileError.setVisibility(View.VISIBLE);
                binding.layoutHaveABHANumber.mobileError.setText(getString(R.string.error_field_required));
                binding.layoutHaveABHANumber.edittextMobileNumber.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                isvalid = false;
            } else {
                if (/*code.equalsIgnoreCase("91") &&*/ mobile.length() != 10) {
                    binding.layoutHaveABHANumber.mobileError.setVisibility(View.VISIBLE);
                    binding.layoutHaveABHANumber.mobileError.setText(getString(R.string.enter_10_digits));
                    binding.layoutHaveABHANumber.edittextMobileNumber.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                    isvalid = false;
                } else {
                    binding.layoutHaveABHANumber.edittextMobileNumber.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
                    binding.layoutHaveABHANumber.mobileError.setVisibility(View.GONE);
                }
            }

            // mobile for aadhar - end
        } else if (!optionSelected.isEmpty() && optionSelected.equals(MOBILE_NUMBER_SELECTION)) {  // Phone number field

            String mobile = binding.layoutHaveABHANumber.edittextMobileNumber.getText().toString().replace(" ", "").trim();
            Timber.tag(TAG).v(mobile);

            if (mobile.isEmpty()) {
                binding.layoutHaveABHANumber.mobileError.setVisibility(View.VISIBLE);
                binding.layoutHaveABHANumber.mobileError.setText(getString(R.string.error_field_required));
                binding.layoutHaveABHANumber.edittextMobileNumber.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                isvalid = false;
            } else {
                if (/*code.equalsIgnoreCase("91") &&*/ mobile.length() != 10) {
                    binding.layoutHaveABHANumber.mobileError.setVisibility(View.VISIBLE);
                    binding.layoutHaveABHANumber.mobileError.setText(getString(R.string.enter_10_digits));
                    binding.layoutHaveABHANumber.edittextMobileNumber.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));
                    isvalid = false;
                } else {
                    binding.layoutHaveABHANumber.edittextMobileNumber.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
                    binding.layoutHaveABHANumber.mobileError.setVisibility(View.GONE);
                }
            }
        } else if (!optionSelected.isEmpty() && optionSelected.equals(ABHA_SELECTION)) {
            binding.layoutHaveABHANumber.abhaDetails.tvAbhaNumberError.setVisibility(View.GONE);
            binding.layoutHaveABHANumber.abhaDetails.tvAbhaNumberError.setVisibility(View.GONE);
            if (TextUtils.isEmpty(binding.layoutHaveABHANumber.abhaDetails.etAbhaAddress.getText()) && TextUtils.isEmpty(binding.layoutHaveABHANumber.abhaDetails.etAbhaNumber.getText())) {
                binding.layoutHaveABHANumber.abhaDetails.tvAbhaNumberError.setVisibility(View.VISIBLE);
                binding.layoutHaveABHANumber.abhaDetails.tvAbhaAddressError.setVisibility(View.VISIBLE);
                isvalid = false;
            }
        }

        return isvalid;
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

    private void cancelResendAndHideView() {
        if (countDownTimer != null)
            countDownTimer.cancel();    // reset any existing countdown.

        if (binding.rlResendOTP.getVisibility() == View.VISIBLE)    // hide resend view
            binding.rlResendOTP.setVisibility(View.GONE);

        if (binding.flOtpBox.getVisibility() == View.VISIBLE) { // hide otp view
            binding.flOtpBox.setVisibility(View.GONE);
            if (binding.otpBox.getText() != null) {
                if (!binding.otpBox.getText().toString().isEmpty()) {
                    binding.otpBox.setText("");
                }
            }
        }
    }

    private void resendOtp() {
        binding.resendBtn.setEnabled(false);
        binding.resendBtn.setTextColor(getColor(R.color.medium_gray));
        binding.sendOtpBtn.setText(R.string.send_otp);  // Send otp.

        String resendTime = getResources().getString(R.string.resend_otp_in);

        if (countDownTimer != null)
            countDownTimer.cancel();    // reset any existing countdown.
        countDownTimer = new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                String time = resendTime + " " + millisUntilFinished / 1000 + " " + getResources().getString(R.string.seconds);
                binding.resendBtn.setText(time);
                Timber.tag(TAG).d("onTick: %s", time);
            }

            public void onFinish() {
                binding.resendBtn.setEnabled(true);
                binding.resendBtn.setText(getResources().getString(R.string.resend_otp));
                binding.resendBtn.setTextColor(getColor(R.color.colorPrimary));
                if (cpd != null && cpd.isShowing())
                    cpd.dismiss();
            }

        }.start();
    }


}