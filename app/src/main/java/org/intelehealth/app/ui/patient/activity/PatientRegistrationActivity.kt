package org.intelehealth.app.ui.patient.activity

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.github.ajalt.timberkt.Timber
import com.google.gson.Gson
import org.intelehealth.app.R
import org.intelehealth.app.databinding.ActivityPatientRegistrationBinding
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.shared.BaseActivity
import org.intelehealth.app.syncModule.SyncUtils
import org.intelehealth.app.utilities.BundleKeys.Companion.PATIENT_CURRENT_STAGE
import org.intelehealth.app.utilities.BundleKeys.Companion.PATIENT_UUID
import org.intelehealth.app.utilities.BundleKeys.Companion.PARENT_PATIENT_UUID
import org.intelehealth.app.utilities.DateAndTimeUtils
import org.intelehealth.app.utilities.DialogUtils
import org.intelehealth.app.utilities.DialogUtils.CustomDialogListener
import org.intelehealth.app.utilities.NetworkConnection
import org.intelehealth.app.utilities.NetworkUtils
import org.intelehealth.app.utilities.NetworkUtils.InternetCheckUpdateInterface
import org.intelehealth.app.utilities.PatientRegStage
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.config.presenter.fields.factory.PatientViewModelFactory
import org.intelehealth.config.room.entity.FeatureActiveStatus
import java.util.UUID


/**
 * Created by Vaghela Mithun R. on 27-06-2024 - 13:41.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class PatientRegistrationActivity : BaseActivity() {
    private lateinit var binding: ActivityPatientRegistrationBinding
    private val patientViewModel by lazy {
        return@lazy PatientViewModelFactory.create(this, this)
    }

    private lateinit var syncAnimator: ObjectAnimator
    private lateinit var actionRefresh: ImageView
    private val networkUtil by lazy {
        NetworkUtils(this, networkStatusListener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        manageTitleVisibilityOnScrolling()
        extractAndBindUI()
        setupActionBar()
        observeCurrentPatientStage()
    }

    private fun observeCurrentPatientStage() {
        patientViewModel.patientStageData.observe(this) { changeIconStatus(it) }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbar)
//        binding.toolbar.setNavigationOnClickListener {
//            handleBackPressed()
//        }
    }

    private fun handleBackPressed() {
        if (patientViewModel.isEditMode) finish()
        else {
            DialogUtils.patientRegistrationDialog(
                this,
                ContextCompat.getDrawable(this, R.drawable.close_patient_svg),
                resources.getString(R.string.close_patient_registration),
                resources.getString(R.string.sure_you_want_close_registration),
                resources.getString(R.string.yes),
                resources.getString(R.string.no)
            ) { action -> if (action == CustomDialogListener.POSITIVE_CLICK) finish() }
        }
    }

    private fun extractAndBindUI() {
        intent?.let {
            val patientId = if (it.hasExtra(PATIENT_UUID)) it.getStringExtra(PATIENT_UUID)
            else null

            patientId?.let { id ->
                patientViewModel.isEditMode = true
                binding.isEditMode = patientViewModel.isEditMode
                fetchPatientDetails(id)
            } ?: generatePatientId()

            val stage = if (it.hasExtra(PATIENT_CURRENT_STAGE)) {
                IntentCompat.getSerializableExtra(
                    it, PATIENT_CURRENT_STAGE, PatientRegStage::class.java
                )
            } else PatientRegStage.PERSONAL

            stage?.let { it1 -> navigateToStage(it1) }
        }
    }

    private fun navigateToStage(stage: PatientRegStage) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostPatientReg) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph =
            navController.navInflater.inflate(R.navigation.navigation_patient_registration)
        val startDestination = when (stage) {
            PatientRegStage.PERSONAL -> R.id.fragmentPatientPersonalInfo
            PatientRegStage.ADDRESS -> R.id.fragmentPatientAddressInfo
            PatientRegStage.OTHER -> R.id.fragmentPatientOtherInfo
        }
        navGraph.setStartDestination(startDestination)
        navController.graph = navGraph
    }

    private fun generatePatientId() {
        PatientDTO().apply {
            uuid = UUID.randomUUID().toString()
            createdDate = DateAndTimeUtils.getTodaysDateInRequiredFormat("dd MMMM, yyyy")
            providerUUID = SessionManager.getInstance(this@PatientRegistrationActivity).providerID
            reportDateOfPatientCreated = DateAndTimeUtils.currentDateTimeFormat()

            householdLinkingUUIDlinking = UUID.randomUUID().toString()

            val parentPatientId = if (intent.hasExtra(PARENT_PATIENT_UUID)) intent.getStringExtra(PARENT_PATIENT_UUID)
            else null

            parentPatientId?.let {
                patientViewModel.loadPatientDetails(parentPatientId).observe(this@PatientRegistrationActivity) {
                    it ?: return@observe
                    patientViewModel.handleResponse(it) { patient ->
                        address1 = patient.address1 // household value
                        householdLinkingUUIDlinking = patient.householdLinkingUUIDlinking
                        cityvillage = patient.cityvillage
                        postalcode = patient.postalcode
                        address3 = patient.address3//after migration discussion block will be saved in address3

                        // TODO: add postalcode, village, state, block, district, country.
                        Log.v("Familyyy", "patreg: " + address1 + " :" + cityvillage + " : "
                                + postalcode + " : " + householdLinkingUUIDlinking)
                    }
                }
            }

        }.also { patientViewModel.updatedPatient(it) }
    }

    private fun fetchPatientDetails(id: String) {
        patientViewModel.loadPatientDetails(id).observe(this) {
            it ?: return@observe
            patientViewModel.handleResponse(it) { patient ->
                patientViewModel.updatedPatient(updatePatientDetails(patient))
            }
        }
    }

    private fun updatePatientDetails(patient: PatientDTO) = patient.apply {
        if (createdDate.isNullOrEmpty()) {
            createdDate = DateAndTimeUtils.getTodaysDateInRequiredFormat("dd MMMM, yyyy")
        }
        if (providerUUID.isNullOrEmpty()) {
            providerUUID = SessionManager.getInstance(this@PatientRegistrationActivity).providerID
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_sync, menu)
        menu?.findItem(R.id.action_sync)?.actionView?.let {
            actionRefresh = it.findViewById(R.id.refresh)
            ObjectAnimator.ofFloat<View>(actionRefresh, View.ROTATION, 0f, 359f).apply {
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
                duration = 1200
            }.also { anim -> syncAnimator = anim }

            actionRefresh.setOnClickListener { startRefreshing() }
        }

        return true
    }

    private fun startRefreshing() {

        if (NetworkConnection.isOnline(this)) {
            SyncUtils().syncBackground()
        }
        actionRefresh.clearAnimation()
        syncAnimator.start()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_sync) startRefreshing()
        else if (item.itemId == R.id.action_cancel) handleBackPressed()
        return true
    }

    private fun changeIconStatus(stage: PatientRegStage) {
        if (stage == PatientRegStage.PERSONAL) {
            binding.patientTab.tvIndicatorPatientPersonal.isSelected = true
        } else if (stage == PatientRegStage.ADDRESS) {
            binding.patientTab.tvIndicatorPatientPersonal.isActivated = true
            binding.patientTab.tvIndicatorPatientAddress.isSelected = true
        } else if (stage == PatientRegStage.OTHER) {
            binding.patientTab.tvIndicatorPatientPersonal.isActivated = true
            binding.patientTab.tvIndicatorPatientAddress.isActivated = true
            binding.patientTab.tvIndicatorPatientOther.isSelected = true
        }
    }

    override fun onFeatureActiveStatusLoaded(activeStatus: FeatureActiveStatus?) {
        super.onFeatureActiveStatusLoaded(activeStatus)
        if (::syncAnimator.isInitialized) syncAnimator.cancel()
        activeStatus?.let {
            patientViewModel.activeStatusAddressSection = it.activeStatusPatientAddress
            patientViewModel.activeStatusOtherSection = it.activeStatusPatientOther

            if (it.activeStatusPatientOther.not() && it.activeStatusPatientAddress.not()) {
                binding.patientTab.root.isVisible = false
            } else {
                binding.patientTab.root.isVisible = true
                binding.addressActiveStatus = it.activeStatusPatientAddress
                binding.otherActiveStatus = it.activeStatusPatientOther
            }
            patientViewModel.activeStatusRosterSection = it.activeStatusRosterQuestionnaireSection
            Log.d("TAG", "onFeatureActiveStatusLoaded: FeatureActiveStatus : "+Gson().toJson(activeStatus))
        }
    }

    override fun onResume() {
        super.onResume()
        networkUtil.callBroadcastReceiver()
    }

    override fun onPause() {
        super.onPause()
        networkUtil.unregisterNetworkReceiver()
    }

    private val networkStatusListener = InternetCheckUpdateInterface {
        if (::actionRefresh.isInitialized) actionRefresh.isEnabled = it
    }

    companion object {
        @JvmStatic
        fun startPatientRegistration(
            context: Context,
            patientId: String? = null,
            stage: PatientRegStage = PatientRegStage.PERSONAL,
        ) {
            Intent(context, PatientRegistrationActivity::class.java).apply {
                putExtra(PATIENT_UUID, patientId)
                putExtra(PATIENT_CURRENT_STAGE, stage)
            }.also { context.startActivity(it) }
        }

        @JvmStatic
        fun startPatientRegistrationForFamilyMemberRegistration(
            context: Context,
            parentPatientId: String? = null,
            childPatientId: String? = null,
            stage: PatientRegStage = PatientRegStage.PERSONAL
        ) {
            Intent(context, PatientRegistrationActivity::class.java).apply {
                putExtra(PARENT_PATIENT_UUID, parentPatientId)
                putExtra(PATIENT_UUID, childPatientId)
                putExtra(PATIENT_CURRENT_STAGE, stage)
            }.also { context.startActivity(it) }
        }
    }
}