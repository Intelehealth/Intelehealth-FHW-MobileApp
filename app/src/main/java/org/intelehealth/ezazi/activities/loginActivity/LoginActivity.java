package org.intelehealth.ezazi.activities.loginActivity;

import android.content.Context;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;

import androidx.appcompat.app.AppCompatActivity;

import android.text.SpannableString;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import java.io.File;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import org.intelehealth.ezazi.R;
import org.intelehealth.ezazi.activities.setupActivity.SetupActivity;
import org.intelehealth.ezazi.app.AppConstants;
import org.intelehealth.ezazi.app.IntelehealthApplication;
import org.intelehealth.ezazi.models.loginModel.LoginModel;
import org.intelehealth.ezazi.models.loginProviderModel.LoginProviderModel;
import org.intelehealth.ezazi.ui.InputChangeValidationListener;
import org.intelehealth.ezazi.ui.dialog.ConfirmationDialogFragment;
import org.intelehealth.ezazi.ui.password.activity.ForgotPasswordActivity;
import org.intelehealth.ezazi.utilities.Base64Utils;
import org.intelehealth.ezazi.utilities.Logger;
import org.intelehealth.ezazi.utilities.OfflineLogin;
import org.intelehealth.ezazi.utilities.SessionManager;
import org.intelehealth.ezazi.utilities.StringEncryption;
import org.intelehealth.ezazi.utilities.TextThemeUtils;
import org.intelehealth.ezazi.utilities.UrlModifiers;
import org.intelehealth.ezazi.widget.materialprogressbar.CustomProgressDialog;

import org.intelehealth.ezazi.activities.homeActivity.HomeActivity;
import org.intelehealth.ezazi.utilities.NetworkConnection;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity {
    TextView txt_cant_login;
    /**
     * A dummy authentication store containing known user names and passwords.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "username:password", "admin:nimda"
    };
    private final String TAG = LoginActivity.class.getSimpleName();
    //    protected AccountManager manager;
//    Account Manager is commented....
//    ProgressDialog progress;
    Context context;
    CustomProgressDialog cpd;
    SessionManager sessionManager = null;
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
//    private UserLoginTask mAuthTask = null;
    private OfflineLogin offlineLogin = null;

    UrlModifiers urlModifiers = new UrlModifiers();
    Base64Utils base64Utils = new Base64Utils();
    String encoded = null;
    // UI references.
    private EditText mUsernameView;
    //    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;

    private TextInputLayout mEmailInputView;
    private TextInputLayout mPasswordInputView;

    private ImageView icLogo;

    private long createdRecordsCount = 0;
    String provider_url_uuid;
    private Button mEmailSignInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_ezazi);
        sessionManager = new SessionManager(this);

        context = LoginActivity.this;
        sessionManager = new SessionManager(context);
        cpd = new CustomProgressDialog(context);

        setTitle(R.string.title_activity_login);

        MaterialButton forgotPassword = findViewById(R.id.tvActionForgotPassword);
        TextThemeUtils.applyUnderline(forgotPassword);

        offlineLogin = OfflineLogin.getOfflineLogin();
        txt_cant_login = findViewById(R.id.tvActionForgotPassword);
        mEmailInputView = findViewById(R.id.etUsernameLayout);
        mPasswordInputView = findViewById(R.id.etPasswordLayout);
        addValidationListener();
        txt_cant_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
                // cantLogin();
            }
        });
      /*  manager = AccountManager.get(LoginActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }*/

      /*  Account[] accountList = manager.getAccountsByType("io.intelehealth.openmrs");
        if (accountList.length > 0) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.putExtra("login", true);
//            startJobDispatcherService(LoginActivity.this);
            startActivity(intent);
            finish();
        }*/

        //Enforces Offline Login Check only if network not present
        if (!NetworkConnection.isOnline(this)) {
            if (OfflineLogin.getOfflineLogin().getOfflineLoginStatus()) {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra("login", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        }

        icLogo = findViewById(R.id.iv_logo);
        setLogo();

        // Set up the login form.
        mUsernameView = findViewById(R.id.et_email);
        // populateAutoComplete(); TODO: create our own autocomplete code
        mPasswordView = findViewById(R.id.et_password);
//      mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
//                if (id == R.id.login || id == EditorInfo.IME_NULL) {
//                    attemptLogin();
//                    return true;
//                }
//                return false;
//            }
//        });
        mEmailSignInButton = findViewById(R.id.btnLogin);
        changeButtonStatus(false);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Logger.logD(TAG, "button pressed");
                attemptLogin();
            }
        });

    }

    private void setLogo() {

        File f = new File("/data/data/" + context.getPackageName() + "/files/logo/ic_logo.png");
        if (f.isFile()) {
            Bitmap bitmap = BitmapFactory.decodeFile("/data/data/" + context.getPackageName() + "/files/logo/ic_logo.png");
            icLogo.setImageBitmap(bitmap);
        } else {
            Log.e("SetLogo", "No Logo Found in Mindmap Folder");
        }
    }

    private void addValidationListener() {
//        new InputChangeValidationListener(mEmailInputView, this::isEmailValid)
//                .validate(getString(R.string.error_invalid_email));
//        new InputChangeValidationListener(mPasswordInputView, this::isPasswordValid)
//                .validate(getString(R.string.error_invalid_password));

        new InputChangeValidationListener(mEmailInputView, new InputChangeValidationListener.InputValidator() {
            @Override
            public boolean validate(String text) {
                return isEmailValid(text);
            }

            @Override
            public void onValidatted(boolean isValid) {
                changeButtonStatus(isValid);
            }
        }).validate(getString(R.string.error_invalid_email));

        new InputChangeValidationListener(mPasswordInputView, new InputChangeValidationListener.InputValidator() {
            @Override
            public boolean validate(String text) {
                return isPasswordValid(text);
            }

            @Override
            public void onValidatted(boolean isValid) {
                changeButtonStatus(isValid);
            }
        }).validate(getString(R.string.error_invalid_password));
    }

    private void changeButtonStatus(boolean isValid) {
        mEmailSignInButton.setEnabled(isValid && checkInputNotEmpty());
    }

    private boolean checkInputNotEmpty() {
        return Objects.requireNonNull(mUsernameView.getText()).length() > 0
                && Objects.requireNonNull(mPasswordView.getText()).length() > 0;
    }

    private boolean isEmailValid(String email) {
        return true;// !TextUtils.isEmpty(email);
    }

    /**
     * Returns void.
     * This method checks if valid username and password are given as input.
     *
     * @return void
     */
    private void attemptLogin() {

        // Store values at the time of the login attempt.
        String email = mUsernameView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();
        mEmailInputView.setError(null);
        mPasswordInputView.setError(null);

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailInputView.setError(getString(R.string.error_require_email));
            mEmailInputView.requestFocus();
            return;
        }
        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordInputView.setError(getString(R.string.error_require_password));
            mPasswordInputView.requestFocus();
            return;
        }

        if (password.length() < 8) {
            mPasswordInputView.setError(getString(R.string.error_invalid_password));
            mPasswordInputView.requestFocus();
            return;
        }

        mEmailSignInButton.setText(getString(R.string.please_wait_progress));
        mEmailSignInButton.setEnabled(false);
        if (NetworkConnection.isOnline(this)) {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            UserLoginTask(email, password);
        } else {
            //offlineLogin.login(email, password);
            offlineLogin.offline_login(email, password);

        }

    }

    /**
     * @param password Password
     * @return boolean
     */
    private boolean isPasswordValid(String password) {
        return password.length() > 7;
    }

    public void cantLogin() {
        final SpannableString span_string = new SpannableString(getApplicationContext().getText(R.string.email_link));
        Linkify.addLinks(span_string, Linkify.EMAIL_ADDRESSES);

        ConfirmationDialogFragment dialog = new ConfirmationDialogFragment.Builder(this)
                .content(getString(R.string.contact_whatsapp))
                .positiveButtonLabel(R.string.contact)
                .negativeButtonLabel(R.string.close_button)
                .build();

        dialog.setListener(() -> {
            String phoneNumberWithCountryCode = AppConstants.HELP_NUMBER;//"+917005308163";
            String message =
                    getString(R.string.hello_my_name_is) + sessionManager.getChwname() +
                            /*" from " + sessionManager.getState() + */getString(R.string.i_need_assistance);

            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(
                            String.format("https://api.whatsapp.com/send?phone=%s&text=%s",
                                    phoneNumberWithCountryCode, message))));
        });

        dialog.show(getSupportFragmentManager(), dialog.getClass().getCanonicalName());


//      MaterialAlertDialogBuilder builder =   new MaterialAlertDialogBuilder(this)
//                .setMessage(getApplicationContext().getText(R.string.contact_whatsapp))
//                .setNegativeButton(R.string.contact, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        //finish();
////                        Intent intent = new Intent(Intent.ACTION_SENDTO); //to get only the list of e-mail clients
////                        intent.setType("text/plain");
////                        intent.setData(Uri.parse("mailto:support@intelehealth.io"));
////                        // intent.putExtra(Intent.EXTRA_EMAIL, "support@intelehealth.io");
////                        // intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
////                        //  intent.putExtra(Intent.EXTRA_TEXT, "I'm email body.");
////
////                        startActivity(Intent.createChooser(intent, "Send Email"));
////                        //add email function here !
//
//                        String phoneNumberWithCountryCode = AppConstants.HELP_NUMBER;//"+917005308163";
//                        String message =
//                                getString(R.string.hello_my_name_is) + sessionManager.getChwname() +
//                                        /*" from " + sessionManager.getState() + */getString(R.string.i_need_assistance);
//
//                        startActivity(new Intent(Intent.ACTION_VIEW,
//                                Uri.parse(
//                                        String.format("https://api.whatsapp.com/send?phone=%s&text=%s",
//                                                phoneNumberWithCountryCode, message))));
//
//                    }
//
//                })
//                .setPositiveButton(R.string.close_button, null);
//
//      AlertDialog alertDialog = builder.show();
//        IntelehealthApplication.setAlertDialogCustomTheme(this,alertDialog);

        //prajwal_changes
    }

    /**
     * class UserLoginTask will authenticate user using email and password.
     * Depending on server's response, user may or may not have successful login.
     * This class also uses SharedPreferences to store session ID
     */
    public void UserLoginTask(String mEmail, String mPassword) {
        cpd.show();
//        String urlString = urlModifiers.loginUrl(sessionManager.getServerUrl());
        String urlString = urlModifiers.loginUrl(getString(R.string.setupUrl));
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
                String url = urlModifiers.loginUrlProvider(sessionManager.getServerUrl(), loginModel.getUser().getUuid());
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
                                            IntelehealthApplication.getInstance().initSocketConnection();
                                            IntelehealthApplication.getInstance().startRealTimeObserver();
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
                                    } catch (NoSuchAlgorithmException |
                                             UnsupportedEncodingException e) {
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
                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
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
                cpd.dismiss();
                Toast.makeText(LoginActivity.this, getString(R.string.error_incorrect_password), Toast.LENGTH_SHORT).show();
                mEmailSignInButton.setText(getString(R.string.action_sign_in));
                mEmailSignInButton.setEnabled(true);
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
}