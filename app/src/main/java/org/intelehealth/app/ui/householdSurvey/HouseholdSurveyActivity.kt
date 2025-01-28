package org.intelehealth.app.ui.householdSurvey

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.navigation.fragment.NavHostFragment
import org.intelehealth.app.R
import org.intelehealth.app.databinding.ActivityHouseholdSurveyBinding
import org.intelehealth.app.shared.BaseActivity
import org.intelehealth.app.syncModule.SyncUtils
import org.intelehealth.app.ui.householdSurvey.factory.HouseHoldViewModelFactory
import org.intelehealth.app.utilities.BundleKeys.Companion.HOUSEHOLD_CURRENT_STAGE
import org.intelehealth.app.utilities.BundleKeys.Companion.PATIENT_UUID
import org.intelehealth.app.utilities.DialogUtils
import org.intelehealth.app.utilities.DialogUtils.CustomDialogListener
import org.intelehealth.app.utilities.HouseholdSurveyStage
import org.intelehealth.app.utilities.NetworkConnection
import org.intelehealth.app.utilities.NetworkUtils

class HouseholdSurveyActivity : BaseActivity() {
    private lateinit var binding: ActivityHouseholdSurveyBinding
    private val houseHoldViewModel by lazy {
        return@lazy HouseHoldViewModelFactory.create(this, this)
    }

    private lateinit var syncAnimator: ObjectAnimator
    private lateinit var actionRefresh: ImageView
    private val networkUtil by lazy {
        NetworkUtils(this, networkStatusListener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHouseholdSurveyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        extractAndBindUI()
        setupActionBar()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun handleBackPressed() {
        if (houseHoldViewModel.isEditMode) finish()
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
                //houseHoldViewModel.isEditMode = true
                binding.isEditMode = houseHoldViewModel.isEditMode
                fetchPatientDetails(id)
            } /*?: generatePatientId()*/

            val stage = if (it.hasExtra(HOUSEHOLD_CURRENT_STAGE)) {
                IntentCompat.getSerializableExtra(
                    it, HOUSEHOLD_CURRENT_STAGE, HouseholdSurveyStage::class.java
                )
            } else HouseholdSurveyStage.FIRST_SCREEN

            stage?.let { it1 -> navigateToStage(it1) }
        }
    }


    private fun navigateToStage(stage: HouseholdSurveyStage) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostHouseholdSurvey) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph =
            navController.navInflater.inflate(R.navigation.navigation_household_survey)
        val startDestination = when (stage) {
            HouseholdSurveyStage.FIRST_SCREEN -> R.id.fragmentFirst
            HouseholdSurveyStage.SECOND_SCREEN -> R.id.fragmentSecond
            HouseholdSurveyStage.THIRD_SCREEN -> R.id.fragmentThird
            HouseholdSurveyStage.FOURTH_SCREEN -> R.id.fragmentFourth
            HouseholdSurveyStage.FIFTH_SCREEN -> R.id.fragmentFifth
            HouseholdSurveyStage.SIXTH_SCREEN -> R.id.fragmentSixth
            HouseholdSurveyStage.SEVENTH_SCREEN -> R.id.fragmentSeventh

        }
        navGraph.setStartDestination(startDestination)
        navController.graph = navGraph
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

    override fun onResume() {
        super.onResume()
        networkUtil.callBroadcastReceiver()
    }

    override fun onPause() {
        super.onPause()
        try {
            networkUtil.unregisterNetworkReceiver()
        } catch (exception: Exception) {
        }
    }

    private val networkStatusListener = NetworkUtils.InternetCheckUpdateInterface {
        if (::actionRefresh.isInitialized) actionRefresh.isEnabled = it
    }

    companion object {
        @JvmStatic
        fun startHouseholdSurvey(
            context: Context,
            patientId: String? = null,
            stage: HouseholdSurveyStage = HouseholdSurveyStage.FIRST_SCREEN
        ) {
            Intent(context, HouseholdSurveyActivity::class.java).apply {
                putExtra(PATIENT_UUID, patientId)
                putExtra(HOUSEHOLD_CURRENT_STAGE, stage)
            }.also { context.startActivity(it) }
        }
    }

    private fun fetchPatientDetails(id: String) {
        houseHoldViewModel.loadPatientDetails(id).observe(this) {
            it ?: return@observe
            houseHoldViewModel.handleResponse(it) { patient ->
                houseHoldViewModel.updatedPatient(updatePatientDetails(patient))
            }
            //check its in observer may change frequently
            if (it.data?.reportDateOfSurveyStarted != null && it.data!!.reportDateOfSurveyStarted.isNotEmpty()) {
                houseHoldViewModel.isEditMode = true
            } else {
                houseHoldViewModel.isEditMode = false
            }

        }
    }

    private fun updatePatientDetails(householdSurveyModel: org.intelehealth.app.ui.householdSurvey.models.HouseholdSurveyModel) =
        householdSurveyModel.apply {
            /* if (createdDate.isNullOrEmpty()) {
                 createdDate = DateAndTimeUtils.getTodaysDateInRequiredFormat("dd MMMM, yyyy")
             }
             if (providerUUID.isNullOrEmpty()) {
                 providerUUID = SessionManager.getInstance(this@HouseholdSurveyActivity).providerID
             }*/
        }

}