package org.intelehealth.app.ui.rosterquestionnaire.ui

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import org.intelehealth.app.R
import org.intelehealth.app.databinding.ActivityRosterQuestionnaireMainBinding
import org.intelehealth.app.shared.BaseActivity
import org.intelehealth.app.syncModule.SyncUtils
import org.intelehealth.app.ui.patient.activity.PatientRegistrationActivity
import org.intelehealth.app.ui.rosterquestionnaire.utilities.RosterQuestionnaireStage
import org.intelehealth.app.ui.rosterquestionnaire.viewmodel.RosterViewModel
import org.intelehealth.app.utilities.BundleKeys.Companion.PATIENT_CURRENT_STAGE
import org.intelehealth.app.utilities.BundleKeys.Companion.PATIENT_UUID
import org.intelehealth.app.utilities.BundleKeys.Companion.ROSTER_CURRENT_STAGE
import org.intelehealth.app.utilities.DialogUtils
import org.intelehealth.app.utilities.DialogUtils.CustomDialogListener
import org.intelehealth.app.utilities.NetworkConnection
import org.intelehealth.app.utilities.NetworkUtils
import org.intelehealth.app.utilities.PatientRegStage

@AndroidEntryPoint
class RosterQuestionnaireMainActivity : BaseActivity() {
    public lateinit var binding: ActivityRosterQuestionnaireMainBinding
    private lateinit var rosterViewModel: RosterViewModel

    private lateinit var syncAnimator: ObjectAnimator
    private lateinit var actionRefresh: ImageView
    private val networkUtil by lazy {
        NetworkUtils(this, networkStatusListener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rosterViewModel = ViewModelProvider.create(this)[RosterViewModel::class]
        binding = ActivityRosterQuestionnaireMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        manageTitleVisibilityOnScrolling()
        extractAndBindUI()
        setupActionBar()

        observeCurrentRosterStage()
    }

    private fun observeCurrentRosterStage() {
        rosterViewModel.rosterStageData.observe(this) { changeIconStatus(it) }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun handleBackPressed() {
        if (rosterViewModel.isEditMode) finish()
        else {
            DialogUtils.patientRegistrationDialog(
                this,
                ContextCompat.getDrawable(this, R.drawable.close_patient_svg),
                resources.getString(R.string.close_roster_questionnaire),
                resources.getString(R.string.sure_you_want_close_close_roster),
                resources.getString(R.string.yes),
                resources.getString(R.string.no)
            ) { action -> if (action == CustomDialogListener.POSITIVE_CLICK) finish() }
        }
    }


    private fun extractAndBindUI() {
        intent?.let {
            val patientId = if (it.hasExtra(PATIENT_UUID)) it.getStringExtra(PATIENT_UUID)
            else null

            patientId?.let {
                //rosterViewModel.isEditMode = true
                binding.isEditMode = rosterViewModel.isEditMode
                //fetchPatientDetails(id)
            }

            val stage = if (it.hasExtra(ROSTER_CURRENT_STAGE)) {
                IntentCompat.getSerializableExtra(
                    it, ROSTER_CURRENT_STAGE, RosterQuestionnaireStage::class.java
                )
            } else RosterQuestionnaireStage.GENERAL_ROSTER

            stage?.let { it1 -> navigateToStage(it1) }
        }
    }


    private fun navigateToStage(stage: RosterQuestionnaireStage) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostRosterQuestionnaire) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph =
            navController.navInflater.inflate(R.navigation.navigation_roster_questionnaire)
        val startDestination = when (stage) {
            RosterQuestionnaireStage.GENERAL_ROSTER -> R.id.fragmentGeneralRoster
            RosterQuestionnaireStage.PREGNANCY_ROSTER -> R.id.fragmentPregnancyRoster
            RosterQuestionnaireStage.HEALTH_SERVICE -> R.id.fragmentHealthService
        }
        navGraph.setStartDestination(startDestination)
        navController.graph = navGraph
    }

    override fun onResume() {
        super.onResume()
        networkUtil.callBroadcastReceiver()
    }

    override fun onPause() {
        super.onPause()
        try {
            networkUtil.unregisterNetworkReceiver()
        } catch (_: Exception) {
        }
    }

    private val networkStatusListener = NetworkUtils.InternetCheckUpdateInterface {
        if (::actionRefresh.isInitialized) actionRefresh.isEnabled = it
    }

    companion object {
        @JvmStatic
        fun startRosterQuestionnaire(
            context: Context,
            patientId: String? = null,
            stage: RosterQuestionnaireStage = RosterQuestionnaireStage.GENERAL_ROSTER
        ) {
            Intent(context, RosterQuestionnaireMainActivity::class.java).apply {
                putExtra(PATIENT_UUID, patientId)
                putExtra(ROSTER_CURRENT_STAGE, stage)
            }.also { context.startActivity(it) }
        }

        @JvmStatic
        fun handleBackEventFromRosterToPatientReg(
            context: Context,
            patientId: String? = null,
            stage: PatientRegStage = PatientRegStage.OTHER
        ) {
            Intent(context, PatientRegistrationActivity::class.java).apply {
                putExtra(PATIENT_UUID, patientId)
                putExtra(PATIENT_CURRENT_STAGE, stage)
            }.also { context.startActivity(it) }
        }
    }

    private fun changeIconStatus(stage: RosterQuestionnaireStage) {
        when (stage) {
            RosterQuestionnaireStage.GENERAL_ROSTER -> {
                binding.patientTab.tvIndicatorGeneralRoster.isSelected = true
            }
            RosterQuestionnaireStage.PREGNANCY_ROSTER -> {
                binding.patientTab.tvIndicatorGeneralRoster.isActivated = true
                binding.patientTab.tvIndicatorPregnancyRoster.isSelected = true
            }
            RosterQuestionnaireStage.HEALTH_SERVICE -> {
                binding.patientTab.tvIndicatorGeneralRoster.isActivated = true
                binding.patientTab.tvIndicatorPregnancyRoster.isActivated = true
                binding.patientTab.tvIndicatorHealthService.isSelected = true
            }
        }
    }  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_sync, menu)
        menu?.findItem(R.id.action_sync)?.actionView?.let {
            actionRefresh = it.findViewById(R.id.refresh)
            ObjectAnimator.ofFloat(actionRefresh, View.ROTATION, 0f, 359f).apply {
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
}