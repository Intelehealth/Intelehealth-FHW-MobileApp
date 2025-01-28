package org.intelehealth.app.ui.rosterquestionnaire.ui

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.intelehealth.app.R
import org.intelehealth.app.databinding.FragmentPregnancyRosterBinding
import org.intelehealth.app.ui.rosterquestionnaire.model.PregnancyOutComeModel
import org.intelehealth.app.ui.rosterquestionnaire.ui.adapter.PregnancyOutcomeAdapter
import org.intelehealth.app.ui.rosterquestionnaire.ui.listeners.PregnancyOutcomeClickListener
import org.intelehealth.app.ui.rosterquestionnaire.utilities.NO
import org.intelehealth.app.ui.rosterquestionnaire.utilities.RosterQuestionnaireStage
import org.intelehealth.app.ui.rosterquestionnaire.utilities.YES
import org.intelehealth.app.ui.rosterquestionnaire.viewmodel.RosterViewModel
import org.intelehealth.app.utilities.SpacingItemDecoration
import org.intelehealth.app.utilities.ToastUtil
import org.intelehealth.app.utilities.extensions.validate

@AndroidEntryPoint
class PregnancyRosterFragment : BaseRosterFragment(R.layout.fragment_pregnancy_roster),
    PregnancyOutcomeClickListener {

    private var pregnancyAdapter: PregnancyOutcomeAdapter? = null
    private lateinit var binding: FragmentPregnancyRosterBinding

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

        setupOutcomeAdapter()
        setListeners()
        observeLiveData()
        setExistingData()
    }

    override fun isInputValid(): Boolean {
        if (isValidPregnancy()) {
            rosterViewModel.pregnancyOutcomeCount =
                binding.tilEtPregnancyOutcomeCount.text.toString()
            rosterViewModel.pregnancyCount = binding.tilEtPregnancyCount.text.toString()
            return true
        } else {
            return false
        }
    }

    private fun setExistingData() {
        binding.tilEtPregnancyCount.setText(rosterViewModel.pregnancyCount)
        binding.tilEtPregnancyOutcomeCount.setText(rosterViewModel.pregnancyOutcomeCount)
        if (rosterViewModel.pregnancyOutcome.isNotEmpty()) {
            if (rosterViewModel.pregnancyOutcome == YES) {
                binding.rbYes.isChecked = true
            } else {
                binding.rbNo.isChecked = true
            }
        }
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
     * Configures click listeners .
     */
    private fun setListeners() {
        binding.tvAddPregnancyOutcome.setOnClickListener {
            AddOutcomeDialog().apply {
                setPregnancyOutcomeList(rosterViewModel.getOutcomeQuestionList())
            }.show(childFragmentManager, AddOutcomeDialog::class.simpleName)

        }


        binding.rgPregnancyOutcome.setOnCheckedChangeListener { group, checkedId ->
            val selectedRadioButton = group.findViewById<RadioButton>(checkedId)
            if (selectedRadioButton.text.toString()
                    .equals(getString(R.string.yes), ignoreCase = true)
            ) {
                binding.groupPregnancyOutcome.visibility = View.VISIBLE
                rosterViewModel.pregnancyOutcome = YES
            } else {
                rosterViewModel.pregnancyOutcome = NO
                binding.groupPregnancyOutcome.visibility = View.GONE
                rosterViewModel.pregnancyOutcomeCount = ""
                binding.tilEtPregnancyOutcomeCount.setText("")
            }
        }

        binding.tilEtPregnancyOutcomeCount.addTextChangedListener { editable ->
            rosterViewModel.pregnancyOutcomeCount = editable.toString()
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
        AddOutcomeDialog().apply {
            setPregnancyOutcomeList(item.roasterViewQuestion, position)
        }.show(childFragmentManager, AddOutcomeDialog::class.simpleName)
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


    private fun isValidPregnancy(): Boolean {
        if (!binding.tilPregnancyCount.validate(
                binding.tilEtPregnancyCount,
                R.string.this_field_is_mandatory
            )
        ) {
            return false
        } else if (rosterViewModel.pregnancyOutcome == YES && !binding.tilPregnancyOutcomeCount.validate(
                binding.tilEtPregnancyOutcomeCount,
                R.string.this_field_is_mandatory
            )
        ) {
            return false
        } else if (rosterViewModel.pregnancyOutcome == YES && pregnancyOutComeList.size != rosterViewModel.pregnancyOutcomeCount.toInt()) {
            if (pregnancyOutComeList.size < rosterViewModel.pregnancyOutcomeCount.toInt()) {
                val count =
                    rosterViewModel.pregnancyOutcomeCount.toInt() - pregnancyOutComeList.size
                ToastUtil.showShortToast(
                    requireContext(),
                    getString(R.string.please_add_pregnancy_outcome, count.toString())
                )
                return false
            } else {
                val count =
                    pregnancyOutComeList.size - rosterViewModel.pregnancyOutcomeCount.toInt()
                ToastUtil.showShortToast(
                    requireContext(),
                    getString(R.string.please_delete_pregnancy_outcome, count.toString())
                )
                return false
            }
        }
        return true
    }

}
