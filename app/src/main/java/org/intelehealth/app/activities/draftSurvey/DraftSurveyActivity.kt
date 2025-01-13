package org.intelehealth.app.activities.draftSurvey

import android.graphics.Color
import android.os.Bundle
import android.view.View
import org.intelehealth.app.R
import org.intelehealth.app.databinding.ActivityDraftSurveyBinding
import org.intelehealth.app.shared.BaseActivity

class DraftSurveyActivity : BaseActivity() {
    private lateinit var binding: ActivityDraftSurveyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDraftSurveyBinding.inflate(layoutInflater)
        setContentView(binding.root)
       // enableEdgeToEdge()
        setupToolbar()

        binding.toolbar.ivBack.setOnClickListener {   // toolbar - arrow icon - back click.
            onBackPressedDispatcher.onBackPressed()
        }

        var draftList = ArrayList<DraftSurveyModel>()
        draftList = setupDraftSurveyList(draftList)
        binding.rvDraftSurvey.adapter = DraftSurveyAdapter(this@DraftSurveyActivity, draftList)
    }

    private fun setupDraftSurveyList(draftList: ArrayList<DraftSurveyModel>): ArrayList<DraftSurveyModel> {
        draftList.add(DraftSurveyModel("","Prajwal W.","1264M-5"))
        draftList.add(DraftSurveyModel("","Kavita W.","26423-K"))
        draftList.add(DraftSurveyModel("","Aparna W.","64530-V"))
        return draftList
    }

    private fun setupToolbar() {
        // changing status bar color
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.statusBarColor = Color.WHITE
        binding.toolbar.tvTitle.text = getString(R.string.draft_survey)
    }


}
