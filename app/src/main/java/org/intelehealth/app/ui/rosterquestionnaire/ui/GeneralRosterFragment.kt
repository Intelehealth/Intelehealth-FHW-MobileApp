package org.intelehealth.app.ui.rosterquestionnaire.ui

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.intelehealth.app.R
import org.intelehealth.app.databinding.FragmentGeneralRosterBinding
import org.intelehealth.app.ui.rosterquestionnaire.model.RoasterViewQuestion
import org.intelehealth.app.ui.rosterquestionnaire.ui.adapter.MultiViewAdapter
import org.intelehealth.app.ui.rosterquestionnaire.ui.listeners.MultiViewListener
import org.intelehealth.app.ui.rosterquestionnaire.utilities.RosterQuestionnaireStage
import org.intelehealth.app.ui.rosterquestionnaire.viewmodel.RosterViewModel

@AndroidEntryPoint
class GeneralRosterFragment : BaseRosterFragment(R.layout.fragment_general_roster),
    MultiViewListener {
    private lateinit var _binding: FragmentGeneralRosterBinding
    private val generalQuestionAdapter: MultiViewAdapter by lazy {
        MultiViewAdapter(listener = this@GeneralRosterFragment)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGeneralRosterBinding.bind(view)
        rosterViewModel = ViewModelProvider.create(requireActivity())[RosterViewModel::class]
        rosterViewModel.updateRosterStage(RosterQuestionnaireStage.GENERAL_ROSTER)
        setAdapter()
        observeLiveData()
    }

    override fun isInputValid() = rosterViewModel.validateGeneralList()?.let {
        _binding.rvGeneralQuestion.smoothScrollToPosition(it)
        generalQuestionAdapter.updateErrorMessage(it)
        false
    } ?: true

    /**
     * Method to observe LiveData and update the adapter with new data when available
     */
    private fun observeLiveData() {
        rosterViewModel.generalLiveList.observe(viewLifecycleOwner) { generalList ->
            // If the list is not null or empty, notify the adapter to refresh the list
            if (!generalList.isNullOrEmpty()) generalQuestionAdapter.notifyList(generalList)
        }
    }

    private fun setAdapter() {
        _binding.rvGeneralQuestion.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = generalQuestionAdapter
        }
    }

    /**
     * Method for handling item clicks on the RecyclerView (currently not implemented)
     */
    override fun onItemClick(item: RoasterViewQuestion, position: Int, view: View) {
        // Handle item click
    }
}
