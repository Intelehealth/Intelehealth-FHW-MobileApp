package org.intelehealth.app.ui.draftsurvey

import android.app.ProgressDialog
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import org.intelehealth.app.R
import org.intelehealth.app.app.IntelehealthApplication
import org.intelehealth.app.database.dao.PatientsDAO
import org.intelehealth.app.databinding.ActivityDraftSurveyBinding
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.ui.draftsurvey.adapter.DraftSurveyAdapter
import org.intelehealth.app.ui.draftsurvey.repository.DraftSurveyRepository
import org.intelehealth.app.ui.draftsurvey.viewmodel.DraftSurveyViewModel
import org.intelehealth.core.shared.ui.viewholder.BaseViewHolder

class DraftSurveyActivity : AppCompatActivity(), BaseViewHolder.ViewHolderClickListener {
    private lateinit var viewModel: DraftSurveyViewModel
    private lateinit var draftSurveyAdapter: DraftSurveyAdapter
    private lateinit var progressDialog: ProgressDialog
    private lateinit var binding: ActivityDraftSurveyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDraftSurveyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = getDatabaseInstance()
        val patientsDAO = PatientsDAO()
        val repository = DraftSurveyRepository(database, patientsDAO)
        viewModel = DraftSurveyViewModel(repository)

        draftSurveyAdapter = DraftSurveyAdapter(this, arrayListOf()).apply {
            this.viewHolderClickListener = this@DraftSurveyActivity
            binding.rvDraftSurvey.adapter = this
        }
        binding.rvDraftSurvey.adapter = draftSurveyAdapter
        setupUI()
        observeViewModel()
        viewModel.loadPatientData()
        setupActionBar()

    }

    private fun setupUI() {
        binding.rvDraftSurvey.layoutManager = LinearLayoutManager(this)
        progressDialog = ProgressDialog(this, R.style.AlertDialogStyle).apply {
            setTitle(getString(R.string.loading))
            setCancelable(false)
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) progressDialog.show() else progressDialog.dismiss()
        }

        viewModel.patientDTOList.observe(this) { patientList ->
            draftSurveyAdapter.updateList(patientList)
        }
    }

    private fun getDatabaseInstance(): SQLiteDatabase {
        return IntelehealthApplication.inteleHealthDatabaseHelper.writableDatabase
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    override fun onViewHolderViewClicked(view: View?, position: Int) {
        view ?: return
        if (view.id == R.id.draft_card_item) {
            val patientDTO = view.tag as PatientDTO
            draftSurveyAdapter.select(patientDTO)
        }
    }
}