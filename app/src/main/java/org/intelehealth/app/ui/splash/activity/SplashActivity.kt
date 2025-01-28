package org.intelehealth.app.ui.splash.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import androidx.work.WorkInfo
import com.github.ajalt.timberkt.Timber
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import org.intelehealth.app.BuildConfig
import org.intelehealth.app.R
import org.intelehealth.app.activities.IntroActivity.IntroScreensActivity_New
import org.intelehealth.app.activities.homeActivity.HomeScreenActivity_New
import org.intelehealth.app.activities.loginActivity.LoginActivityNew
import org.intelehealth.app.activities.onboarding.SetupPrivacyNoteActivity_New
import org.intelehealth.app.app.AppConstants
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.dataMigration.SmoothUpgrade
import org.intelehealth.app.databinding.ActivitySplashBinding
import org.intelehealth.app.ui.language.activity.LanguageActivity
import org.intelehealth.app.ui.splash.adapter.LanguageAdapter
import org.intelehealth.app.utilities.DialogUtils
import org.intelehealth.app.utilities.DialogUtils.CustomDialogListener
import org.intelehealth.app.utilities.Logger
import org.intelehealth.config.room.entity.ActiveLanguage
import org.intelehealth.config.worker.ConfigSyncWorker
import org.intelehealth.core.shared.ui.viewholder.BaseViewHolder
import org.intelehealth.fcm.utils.FcmRemoteConfig.getRemoteConfig
import org.intelehealth.fcm.utils.FcmTokenGenerator.getDeviceToken
import org.intelehealth.klivekit.utils.extensions.showToast

/**
 * Created by Vaghela Mithun R. on 15-04-2024 - 11:28.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
@SuppressLint("CustomSplashScreen")
class SplashActivity : LanguageActivity(), BaseViewHolder.ViewHolderClickListener {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var adapter: LanguageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadConfig()
        handleFcmCall()

        handleButtonClickListener()
        initLanguageList()

        binding.tvTitle.isVisible = BuildConfig.FLAVOR_client != "bmgf"
    }

    private fun loadConfig() {
        if (sessionManager.isFirstTimeLaunch) {
            ConfigSyncWorker.startConfigSyncWorker(this) {
                Timber.d { "Worker state $it" }
                runOnUiThread { binding.pbConfigLoading.isVisible = false }
                if (it == WorkInfo.State.SUCCEEDED.name) {
                    checkPerm()
                    animateViews()
                } else if (it == WorkInfo.State.FAILED.name) {
                    runOnUiThread { showConfigFailDialog() }
                }
            }
        } else {
            //as we are implementing force update now thus commenting this.
            Handler(Looper.getMainLooper()).postDelayed({ nextActivity() }, 3000)
        }
    }

    private fun showConfigFailDialog() {
        val message = getString(R.string.something_went_wrong)
        val title = getString(R.string.error)
        val action = getString(R.string.retry_again)
        val cancel = getString(R.string.cancel)
        DialogUtils().showCommonDialog(
            this, R.drawable.close_patient_svg, title,
            message, false, action, cancel
        ) {
            if (it == CustomDialogListener.NEGATIVE_CLICK) finish()
            else if (it == CustomDialogListener.POSITIVE_CLICK) loadConfig()
        }
    }

    private fun initLanguageList() {
        binding.rvSelectLanguage.layoutManager = LinearLayoutManager(this)
        binding.rvSelectLanguage.itemAnimator = DefaultItemAnimator()
        adapter = LanguageAdapter(this, arrayListOf()).apply {
            this.viewHolderClickListener = this@SplashActivity
            binding.rvSelectLanguage.adapter = this
        }
    }

    override fun onLanguageLoaded(languages: List<ActiveLanguage>) {
        adapter.updateItems(languages.toMutableList())
    }

    private fun handleButtonClickListener() {
        binding.btnNextToIntro.setOnClickListener {
            adapter.getList().find { it.selected }?.let {
                sessionManager.appLanguage = it.code
                setupLanguage()
                Intent(this@SplashActivity, IntroScreensActivity_New::class.java).apply {
                    startActivity(this)
                }
                finish()
            }
        }
    }

    private fun handleFcmCall() {
        // refresh the fcm token
        getDeviceToken { token: String? ->
            IntelehealthApplication.getInstance().refreshedFCMTokenID = token
        }
        getRemoteConfig(this) { checkForceUpdate(it) }
    }

    private fun checkForceUpdate(config: FirebaseRemoteConfig) {
        val forceUpdateVersionCode = config.getLong("force_update_version_code")
        if (forceUpdateVersionCode > BuildConfig.VERSION_CODE) {
            val message = getString(R.string.warning_app_update)
            val title = getString(R.string.new_update_available)
            val action = getString(R.string.update)
            DialogUtils().showCommonDialog(
                this, R.drawable.close_patient_svg, title,
                message, true, action, ""
            ) {
                try {
                    startActivity(getAppIntent(AppConstants.getAppMarketUrl(this@SplashActivity)))
                } catch (anfe: ActivityNotFoundException) {
                    startActivity(getAppIntent(AppConstants.getAppPlayStoreUrl(this@SplashActivity)))
                }
            }
        } else {
            checkPerm()
        }
    }

    private fun getAppIntent(url: String) = Intent(
        Intent.ACTION_VIEW,
        Uri.parse(url)
    )

    private fun checkPerm() {
        if (checkAndRequestPermissions()) {
            val handler = Handler(Looper.getMainLooper())
            if (sessionManager.isMigration) {
                handler.postDelayed({
                    //Do something after 100ms
                    //                        nextActivity();
                }, 2000)
            } else {
                handler.postDelayed({
                    //Do something after 100ms
                    val smoothUpgrade = SmoothUpgrade(this)
                    val smoothupgrade = smoothUpgrade.checkingDatabase()
//                    if (smoothupgrade) {
                    //                            nextActivity();
//                    }
                }, 2000)
            }
        }
    }

    private fun nextActivity() {
        val setup = sessionManager.isSetupComplete
        val LOG_TAG = "SplashActivity"
        Logger.logD(LOG_TAG, setup.toString())
        if (sessionManager.isFirstTimeLaunch) {
            Logger.logD(LOG_TAG, "Starting setup")
            Intent(this, IntroScreensActivity_New::class.java).apply {
                startActivity(this)
            }
            finish()
        } else {
            if (setup) {
                if (sessionManager.isEnableAppLock) fingerPrintAuthenticate() else navigateToNextActivity()
            } else {
                Logger.logD(LOG_TAG, "Starting setup")
                Intent(this, SetupPrivacyNoteActivity_New::class.java).apply {
                    startActivity(this)
                }
                finish()
            }
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val getAccountPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS)
        var writeExternalStoragePermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val bluetoothPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
        val bluetoothAdminPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
        val coarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        val fineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            writeExternalStoragePermission =
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
            val notificationPermission =
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            if (notificationPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val fullScreenIntent =
                ContextCompat.checkSelfPermission(this, Manifest.permission.USE_FULL_SCREEN_INTENT)
            if (fullScreenIntent != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.USE_FULL_SCREEN_INTENT)
            }
        }
        val phoneStatePermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (getAccountPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.GET_ACCOUNTS)
        }
        if (writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                listPermissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES)
                listPermissionsNeeded.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
            } else {
                listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        if (phoneStatePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val bluetoothScanPermission =
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
            val bluetoothConnectPermission =
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)

            if (bluetoothScanPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN)
            }

            if (bluetoothConnectPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        } else {
            if (bluetoothPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.BLUETOOTH)
            }

            if (bluetoothAdminPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.BLUETOOTH_ADMIN)
            }
        }
        if (coarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (fineLocationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionsNeeded.toTypedArray(),
                GROUP_PERMISSION_REQUEST
            )
            return false
        }

//        checkOverlayPermission();
        return true
    }

    @SuppressLint("SwitchIntDef")
    private fun fingerPrintAuthenticate() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> showToast(
                getString(R.string.no_fingerprint_sensor)
            )

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> showToast(
                resources.getString(R.string.fingerprint_not_working)
            )

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> showToast(
                resources.getString(R.string.no_fingerprint_assigned)
            )
        }
        authenticateFingerprint()
    }

    private fun authenticateFingerprint() {
        val executor = ContextCompat.getMainExecutor(this)
        BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    showToast(resources.getString(R.string.login_failed))
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    showToast(resources.getString(R.string.login_successfully))
                    navigateToNextActivity()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    showToast(resources.getString(R.string.login_failed))
                }
            }).apply {
            PromptInfo.Builder()
                .setTitle(resources.getString(R.string.intelehealth_login))
                .setSubtitle(resources.getString(R.string.touch_fingerprint))
                .setNegativeButtonText(resources.getString(R.string.cancel))
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                .build().also {
                    authenticate(it)
                }
        }
    }

    private fun navigateToNextActivity() {
        if (sessionManager.isLogout) {
            Logger.logD(TAG, "Starting login")
            val intent = Intent(this, LoginActivityNew::class.java)
            startActivity(intent)
            finish()
        } else {
            Logger.logD(TAG, "Starting home")
            val intent = Intent(this, HomeScreenActivity_New::class.java)
            intent.putExtra("from", "splash")
            intent.putExtra("username", "")
            intent.putExtra("password", "")
            startActivity(intent)
            finish()
        }
    }

    private fun animateViews() {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            val translateAnim = AnimationUtils.loadAnimation(
                applicationContext, R.anim.ui2_new_center_to_top
            )
            translateAnim.fillAfter = true
            translateAnim.isFillEnabled = true
            translateAnim.fillBefore = false
            translateAnim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    showChooseLanguageUI(true)
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            binding.layoutChild1.startAnimation(translateAnim)
        }, 500)
    }

    private fun showChooseLanguageUI(show: Boolean) {
        val transition: Transition = Slide(Gravity.BOTTOM)
        transition.duration = 2000
        transition.addTarget(R.id.layout_panel)
        TransitionManager.beginDelayedTransition(binding.layoutParent, transition)
        binding.layoutPanel.visibility = if (show) View.VISIBLE else View.GONE
    }

    companion object {
        private const val TAG = "SplashActivity"
        private const val GROUP_PERMISSION_REQUEST = 1000
    }

    override fun onViewHolderViewClicked(view: View?, position: Int) {
        view ?: return
        if (view.id == R.id.layout_rb_choose_language) {
            val lang = view.tag as ActiveLanguage
            adapter.select(position, lang)
        }
    }
}