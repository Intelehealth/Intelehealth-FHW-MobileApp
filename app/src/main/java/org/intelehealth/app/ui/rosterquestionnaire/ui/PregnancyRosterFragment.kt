package org.intelehealth.app.ui.rosterquestionnaire.ui

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.intelehealth.app.R
import org.intelehealth.app.databinding.FragmentPregnancyRosterBinding
import org.intelehealth.app.ui.rosterquestionnaire.model.PregnancyOutComeModel
import org.intelehealth.app.ui.rosterquestionnaire.ui.adapter.PregnancyOutcomeAdapter
import org.intelehealth.app.ui.rosterquestionnaire.ui.listeners.PregnancyOutcomeClickListener
import org.intelehealth.app.ui.rosterquestionnaire.utilities.RosterQuestionnaireStage
import org.intelehealth.app.ui.rosterquestionnaire.viewmodel.RosterViewModel
import org.intelehealth.app.utilities.SpacingItemDecoration
import org.intelehealth.app.utilities.ToastUtil
import org.intelehealth.app.utilities.extensions.validate
import org.intelehealth.app.utilities.extensions.validateDropDowb

@AndroidEntryPoint
class PregnancyRosterFragment : BaseRosterFragment(R.layout.fragment_pregnancy_roster),
    PregnancyOutcomeClickListener {

    private var pregnancyAdapter: PregnancyOutcomeAdapter? = null
    private lateinit var binding: FragmentPregnancyRosterBinding
    private var patientUuid: String? = null
    private val pregnancyOutComeList = ArrayList<PregnancyOutComeModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPregnancyRosterBinding.bind(view)

        // Initialize ViewModel and bind lifecycle
        rosterViewModel = ViewModelProvider.create(requireActivity())[RosterViewModel::class]
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = rosterViewModel

        // Update the current roster stage
        rosterViewModel.updateRosterStage(RosterQuestionnaireStage.PREGNANCY_ROSTER)

        initViews()
        setupOutcomeAdapter()
        setupClickListeners()
        observeLiveData()
    }

    /**
     * Observes LiveData for updates to the pregnancy outcome list and refreshes the adapter.
     */
    private fun observeLiveData() {
        rosterViewModel.outComeLiveList.observe(viewLifecycleOwner) { outcomeList ->
            pregnancyOutComeList.apply {
                clear()
                addAll(outcomeList)
            }
            pregnancyAdapter?.notifyList()
        }
    }

    /**
     * Initializes necessary data and extracts the patient UUID from the intent.
     */
    private fun initViews() {
        patientUuid = requireActivity().intent?.getStringExtra("patientUuid")
    }

    /**
     * Sets up the RecyclerView adapter for displaying the list of pregnancy outcomes.
     */
    private fun setupOutcomeAdapter() {
        binding.rvPregnancyOutcome.apply {
            layoutManager = LinearLayoutManager(requireContext())
            pregnancyAdapter =
                PregnancyOutcomeAdapter(pregnancyOutComeList, this@PregnancyRosterFragment)
            addItemDecoration(SpacingItemDecoration(16)) // Adds spacing between items
            adapter = pregnancyAdapter
        }
    }

    /**
     * Configures click listeners for UI actions like adding outcomes, navigating forward, and going back.
     */
    private fun setupClickListeners() {
        binding.tvAddPregnancyOutcome.setOnClickListener {
            AddOutcomeDialog().show(childFragmentManager, AddOutcomeDialog::class.simpleName)
        }

        binding.frag2BtnNext.setOnClickListener {
            if (!isValidRoaster()) {
                return@setOnClickListener
            } else if (!isValidOutcome()) {
                ToastUtil.showShortToast(
                    requireContext(),
                    getString(R.string.please_select_pregnancy_outcome)
                )
                return@setOnClickListener
            }
            if (pregnancyOutComeList.isEmpty()) {
                ToastUtil.showShortToast(
                    requireContext(),
                    getString(R.string.please_add_pregnancy_outcome)
                )
            } else {
                navigateToHealthService()
            }
        }

        binding.frag2BtnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    /**
     * Navigates to the Health Service section of the application.
     */
    private fun navigateToHealthService() {
        PregnancyRosterFragmentDirections.navigationPregnancyToHealthService().apply {
            findNavController().navigate(this)
        }
    }

    /**
     * Deletes the selected pregnancy outcome from the list and updates the RecyclerView.
     * @param view The view triggering the event
     * @param position The position of the item to be deleted
     * @param item The pregnancy outcome model to delete
     */
    override fun onClickDelete(view: View, position: Int, item: PregnancyOutComeModel) {
        rosterViewModel.deletePregnancyOutcome(position)
        pregnancyOutComeList.removeAt(position)
        pregnancyAdapter?.notifyItemRemoved(position)
    }

    /**
     * Opens the edit dialog for a selected pregnancy outcome.
     * @param view The view triggering the event
     * @param position The position of the item to edit
     * @param item The pregnancy outcome model to edit
     */
    override fun onClickEdit(view: View, position: Int, item: PregnancyOutComeModel) {
        rosterViewModel.existPregnancyOutComePosition = position
        rosterViewModel.existingRoasterQuestionList =
            ArrayList(item.roasterViewQuestion)
        AddOutcomeDialog().show(childFragmentManager, AddOutcomeDialog::class.simpleName)
    }

    /**
     * Toggles the open/close state of the pregnancy outcome item and updates the view.
     * @param view The view triggering the event
     * @param position The position of the item to toggle
     * @param item The pregnancy outcome model to toggle
     */
    override fun onClickOpen(view: View, position: Int, item: PregnancyOutComeModel) {
        item.isOpen = !item.isOpen
        pregnancyAdapter?.notifyItemChanged(position)
    }

    private fun isValidRoaster(): Boolean {
        return binding.tilPregnancyCount.validate(
            binding.tilEtPregnancyCount,
            R.string.this_field_is_mandatory
        ) && binding.tilPregnancyOutcomeCount.validate(
            binding.tilEtPregnancyOutcomeCount,
            R.string.this_field_is_mandatory
        )
    }

    private fun isValidOutcome(): Boolean = binding.rgPregnancyOutcome.checkedRadioButtonId != -1

}
