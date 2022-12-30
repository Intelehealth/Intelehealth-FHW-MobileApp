package org.intelehealth.app.activities.loginActivity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.activities.forgotPasswordNew.ForgotPasswordActivity_New;
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New;
import org.intelehealth.app.activities.setupActivity.SetupActivityNew;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.models.loginModel.LoginModel;
import org.intelehealth.app.models.loginProviderModel.LoginProviderModel;
import org.intelehealth.app.ui2.customToolip.ActionItemCustom;
import org.intelehealth.app.ui2.customToolip.QuickActionCustom;
import org.intelehealth.app.ui2.customToolip.QuickIntentActionCustom;
import org.intelehealth.app.utilities.Base64Utils;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.NetworkConnection;
import org.intelehealth.app.utilities.OfflineLogin;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.SnackbarUtils;
import org.intelehealth.app.utilities.StringEncryption;
import org.intelehealth.app.utilities.UrlModifiers;
import org.intelehealth.app.widget.materialprogressbar.CustomProgressDialog;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class LoginActivityNew extends AppCompatActivity {
    private static final String TAG = "LoginActivityNew";
    TextInputEditText etUsername, etPassword;
    SessionManager sessionManager = null;
    Context context;
    private OfflineLogin offlineLogin = null;
    CustomProgressDialog cpd;
    UrlModifiers urlModifiers = new UrlModifiers();
    String encoded = null;
    Base64Utils base64Utils = new Base64Utils();
    String provider_url_uuid;
    private long createdRecordsCount = 0;
    TextView tvUsernameError, tvPasswordError;
    CoordinatorLayout layoutParent;
    SnackbarUtils snackbarUtils;
    private QuickActionCustom quickAction;
    private QuickActionCustom quickIntent;
    private static final int ID_DOWN = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_new_ui2);

        context = LoginActivityNew.this;
        sessionManager = new SessionManager(context);
        cpd = new CustomProgressDialog(context);
        snackbarUtils = new SnackbarUtils();


        TextView textviewPassword = findViewById(R.id.tv_forgot_password1);
        TextView buttonLogin = findViewById(R.id.button_login);
        tvUsernameError = findViewById(R.id.tv_username_error);
        tvPasswordError = findViewById(R.id.tv_password_error);

        layoutParent = findViewById(R.id.layout_parent_login);


        etUsername = findViewById(R.id.et_username_login);
        etPassword = findViewById(R.id.et_password_login);

        textviewPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivityNew.this, ForgotPasswordActivity_New.class);
                startActivity(intent);
            }
        });
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(LoginActivityNew.this);
                attemptLogin();
            }
        });


        manageCollapsingLayout();

        offlineLogin = OfflineLogin.getOfflineLogin();

        //Enforces Offline Login Check only if network not present
        if (!NetworkConnection.isOnline(this)) {
            if (OfflineLogin.getOfflineLogin().getOfflineLoginStatus()) {
                Intent intent = new Intent(this, HomeScreenActivity_New.class);
                intent.putExtra("login", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        }


        ImageView ivLoginDetails = findViewById(R.id.iv_login_details_info);
        ivLoginDetails.setOnClickListener(v -> {
            setTooltipForInternet("Enter the credentials given by intelehealth team");

            quickAction.show(v);

        });

        manageErrorFields();
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

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(etPassword.getText().toString())) {
                    tvPasswordError.setVisibility(View.VISIBLE);
                    etPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));

                    return;
                } else {
                    tvPasswordError.setVisibility(View.GONE);
                    etPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));

                }
            }
        });


    }

    private void manageCollapsingLayout() {
        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.layout_collapsing);
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle("Login");
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbarLayout.setTitle(" ");//careful there should a space between double quote otherwise it wont work
                    isShow = false;
                }
            }
        });

        etUsername.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View v, boolean hasFocus) {
                if (hasFocus) {
                    appBarLayout.setExpanded(false, true);
                }
            }
        });

        etPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View v, boolean hasFocus) {
                if (hasFocus) {
                    appBarLayout.setExpanded(false, true);
                }
            }
        });
    }

    private void attemptLogin() {

        // Store values at the time of the login attempt.
        String email = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();


        if (areInputFieldsValid(email, password)) {
            //  mEmailSignInButton.setText(getString(R.string.please_wait_progress));
            //  mEmailSignInButton.setEnabled(false);
            if (NetworkConnection.isOnline(this)) {
                // Show a progress spinner, and kick off a background task to
                // perform the user login attempt.
                UserLoginTask(email, password);
            } else {
                //offlineLogin.login(email, password);
                offlineLogin.offline_login(email, password);

            }

        }
    }

    private boolean areInputFieldsValid(String email, String password) {
        boolean result = false;

        if (TextUtils.isEmpty(email)) {
            result = false;
            tvUsernameError.setVisibility(View.VISIBLE);
            etUsername.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));


        } else if (TextUtils.isEmpty(password)) {
            result = false;
            tvPasswordError.setVisibility(View.VISIBLE);
            etPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));

        } else if (password.length() < 8) {
            tvPasswordError.setText(getString(R.string.error_invalid_password));
            tvPasswordError.setVisibility(View.VISIBLE);
            etPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.input_field_error_bg_ui2));

        } else {
            etUsername.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));
            etPassword.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_input_fieldnew));

            result = true;
        }

        return result;
    }

    /**
     * class UserLoginTask will authenticate user using email and password.
     * Depending on server's response, user may or may not have successful login.
     * This class also uses SharedPreferences to store session ID
     */
    public void UserLoginTask(String mEmail, String mPassword) {
        cpd.show();
        String urlString = urlModifiers.loginUrl(AppConstants.DEMO_URL);

        Log.d(TAG, "UserLoginTask: urlString : " + urlString);
        Logger.logD(TAG, "username and password" + mEmail + mPassword);
        encoded = base64Utils.encoded(mEmail, mPassword);
        sessionManager.setEncoded(encoded);

        //cpd.show();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Observable<LoginModel> loginModelObservable = AppConstants.apiInterface.LOGIN_MODEL_OBSERVABLE(urlString, "Basic " + encoded);
        loginModelObservable.subscribe(new Observer<LoginModel>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(LoginModel loginModel) {
                int responsCode = loginModel.hashCode();
                Boolean authencated = loginModel.getAuthenticated();
                Gson gson = new Gson();
                Logger.logD(TAG, "success" + gson.toJson(loginModel));
                sessionManager.setChwname(loginModel.getUser().getDisplay());
                sessionManager.setCreatorID(loginModel.getUser().getUuid());
                Log.d("SESSOO", "SESSOO_creator: " + loginModel.getUser().getUuid());
                sessionManager.setSessionID(loginModel.getSessionId());
                Log.d("SESSOO", "SESSOO: " + sessionManager.getSessionID());
                sessionManager.setProviderID(loginModel.getUser().getPerson().getUuid());
                Log.d("SESSOO", "SESSOO_PROVIDER: " + loginModel.getUser().getPerson().getUuid());
                Log.d("SESSOO", "SESSOO_PROVIDER_session: " + sessionManager.getProviderID());


                UrlModifiers urlModifiers = new UrlModifiers();
                // String url = urlModifiers.loginUrlProvider(sessionManager.getServerUrl(), loginModel.getUser().getUuid());
                String url = urlModifiers.loginUrlProvider(AppConstants.DEMO_URL, loginModel.getUser().getUuid());
                Log.d(TAG, "onNext: url : " + url);
                if (authencated) {
                    Observable<LoginProviderModel> loginProviderModelObservable = AppConstants.apiInterface.LOGIN_PROVIDER_MODEL_OBSERVABLE(url, "Basic " + encoded);
                    loginProviderModelObservable
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new DisposableObserver<LoginProviderModel>() {
                                @Override
                                public void onNext(LoginProviderModel loginProviderModel) {
                                    if (loginProviderModel.getResults().size() != 0) {
                                        for (int i = 0; i < loginProviderModel.getResults().size(); i++) {
                                            Log.i(TAG, "doInBackground: " + loginProviderModel.getResults().get(i).getUuid());
                                            sessionManager.setProviderID(loginProviderModel.getResults().get(i).getUuid());

                                            provider_url_uuid = loginProviderModel.getResults().get(i).getUuid();
//                                                success = true;
                                          /*  final Account account = new Account(mEmail, "io.intelehealth.openmrs");
                                            manager.addAccountExplicitly(account, mPassword, null);
                                            Log.d("MANAGER", "MANAGER " + account);*/
                                            //offlineLogin.invalidateLoginCredentials();


                                        }
                                    }
                                    SQLiteDatabase sqLiteDatabase = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
                                    //SQLiteDatabase read_db = AppConstants.inteleHealthDatabaseHelper.getReadableDatabase();

                                    sqLiteDatabase.beginTransaction();
                                    //read_db.beginTransaction();
                                    ContentValues values = new ContentValues();

                                    //StringEncryption stringEncryption = new StringEncryption();
                                    String random_salt = getSalt_DATA();

                                    //String random_salt = stringEncryption.getRandomSaltString();
                                    Log.d("salt", "salt: " + random_salt);
                                    //Salt_Getter_Setter salt_getter_setter = new Salt_Getter_Setter();
                                    //salt_getter_setter.setSalt(random`_salt);


                                    String hash_password = null;
                                    try {
                                        //hash_email = StringEncryption.convertToSHA256(random_salt + mEmail);
                                        hash_password = StringEncryption.convertToSHA256(random_salt + mPassword);
                                    } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                                        FirebaseCrashlytics.getInstance().recordException(e);
                                    }

                                    try {
                                        values.put("username", mEmail);
                                        values.put("password", hash_password);
                                        values.put("creator_uuid_cred", loginModel.getUser().getUuid());
                                        values.put("chwname", loginModel.getUser().getDisplay());
                                        values.put("provider_uuid_cred", sessionManager.getProviderID());
                                        createdRecordsCount = sqLiteDatabase.insertWithOnConflict("tbl_user_credentials", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                                        sqLiteDatabase.setTransactionSuccessful();

                                        Logger.logD("values", "values" + values);
                                        Logger.logD("created user credentials", "create user records" + createdRecordsCount);
                                    } catch (SQLException e) {
                                        Log.d("SQL", "SQL user credentials: " + e);
                                    } finally {
                                        sqLiteDatabase.endTransaction();
                                    }


                                    // offlineLogin.setUpOfflineLogin(mEmail, mPassword);
                                    cpd.dismiss();
                                    Intent intent = new Intent(LoginActivityNew.this, HomeScreenActivity_New.class);
                                    intent.putExtra("login", true);
//                startJobDispatcherService(LoginActivity.this);
                                    startActivity(intent);
                                    finish();
                                    //  showProgress(false);

                                    sessionManager.setReturningUser(true);
                                    sessionManager.setLogout(false);
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Logger.logD(TAG, "handle provider error" + e.getMessage());
                                    cpd.dismiss();
                                }

                                @Override
                                public void onComplete() {

                                }
                            });
                }
            }

            @Override
            public void onError(Throwable e) {
                Logger.logD(TAG, "Login Failure" + e.getMessage());
                e.printStackTrace();
                cpd.dismiss();
                DialogUtils dialogUtils = new DialogUtils();
                dialogUtils.showCommonDialog(LoginActivityNew.this, R.drawable.ui2_ic_warning_internet, getResources().getString(R.string.error_login_title), getString(R.string.error_incorrect_password), true, getResources().getString(R.string.ok), getResources().getString(R.string.cancel), new DialogUtils.CustomDialogListener() {
                    @Override
                    public void onDialogActionDone(int action) {

                    }
                });

                // mEmailSignInButton.setText(getString(R.string.action_sign_in));
                //mEmailSignInButton.setEnabled(true);
            }

            @Override
            public void onComplete() {
                Logger.logD(TAG, "completed");
            }
        });

    }

    public String getSalt_DATA() {
        BufferedReader reader = null;
        String salt = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("salt.env")));

            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                //process line
                salt = mLine;
                Log.d("SA", "SA " + salt);
            }
        } catch (Exception e) {
            //log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    //log the exception
                }
            }
        }
        return salt;

    }

    public void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void setTooltipForInternet(String message) {
        QuickActionCustom.setDefaultColor(ResourcesCompat.getColor(getResources(), R.color.red, null));
        QuickActionCustom.setDefaultTextColor(Color.BLACK);

        ActionItemCustom nextItem = new ActionItemCustom(ID_DOWN, message);
        quickAction = new QuickActionCustom(this, QuickActionCustom.HORIZONTAL);
        quickAction.setColorRes(R.color.white);
        quickAction.setTextColorRes(R.color.textColorBlack);
        quickAction.addActionItem(nextItem);
        quickAction.setTextColor(Color.BLACK);


        //Set listener for action item clicked
        quickAction.setOnActionItemClickListener(new QuickActionCustom.OnActionItemClickListener() {
            @Override
            public void onItemClick(ActionItemCustom item) {
                //here we can filter which action item was clicked with pos or actionId parameter
                String title = item.getTitle();
                //  Toast.makeText(LoginActivityNew.this, title + " selected", Toast.LENGTH_SHORT).show();
                if (!item.isSticky()) quickAction.remove(item);
            }
        });

        quickAction.setOnDismissListener(new QuickActionCustom.OnDismissListener() {
            @Override
            public void onDismiss() {
                // Toast.makeText(HomeScreenActivity.this, "Dismissed", Toast.LENGTH_SHORT).show();
            }
        });

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
        sendIntent.setType("text/plain");

        quickIntent = new QuickIntentActionCustom(this)
                .setActivityIntent(sendIntent)
                .create();
        quickIntent.setAnimStyle(QuickActionCustom.Animation.REFLECT);
    }

}