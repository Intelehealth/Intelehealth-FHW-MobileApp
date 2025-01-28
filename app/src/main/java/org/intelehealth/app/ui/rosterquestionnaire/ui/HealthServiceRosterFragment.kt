package org.intelehealth.app.ui.rosterquestionnaire.ui

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.intelehealth.app.R
import org.intelehealth.app.databinding.FragmentHealthServiceRosterBinding
import org.intelehealth.app.ui.rosterquestionnaire.model.HealthServiceModel
import org.intelehealth.app.ui.rosterquestionnaire.ui.adapter.HealthServiceAdapter
import org.intelehealth.app.ui.rosterquestionnaire.ui.listeners.HealthServiceClickListener
import org.intelehealth.app.ui.rosterquestionnaire.utilities.RosterQuestionnaireStage
import org.intelehealth.app.ui.rosterquestionnaire.viewmodel.RosterViewModel
import org.intelehealth.app.utilities.SpacingItemDecoration
import org.intelehealth.app.utilities.ToastUtil

@AndroidEntryPoint
class HealthServiceRosterFragment : BaseRosterFragment(R.layout.fragment_health_service_roster),
    HealthServiceClickListener {

    private var healthServiceAdapter: HealthServiceAdapter? = null
    private lateinit var binding: FragmentHealthServiceRosterBinding
    private var patientUuid: String? = null
    private val healthServiceList = mutableListOf<HealthServiceModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHealthServiceRosterBinding.bind(view)

        // Initialize ViewModel and update the roster stage
        rosterViewModel = ViewModelProvider.create(requireActivity())[RosterViewModel::class]
        rosterViewModel.updateRosterStage(RosterQuestionnaireStage.HEALTH_SERVICE)

        initViews()
        setupHealthServiceAdapter()
        observeLiveData()
        setupClickListeners()
    }

    override fun isInputValid(): Boolean {
        if (healthServiceList.isNotEmpty()) {
            return true

        } else {
            ToastUtil.showShortToast(
                requireContext(),
                getString(R.string.please_add_health_service)
            )
            return false
        }
    }

    /**
     * Sets up click listeners for navigation and adding health services.
     */
    private fun setupClickListeners() {
//        binding.frag2BtnNext.setOnClickListener {
//            navigateToDetails()
//        }
//        binding.frag2BtnBack.setOnClickListener {
//            findNavController().popBackStack()
//        }
        binding.tvAddHealthService.setOnClickListener {
            AddHealthServiceDialog().apply {
                setHealthServiceData(rosterViewModel.getHealthServiceList())
            }.show(
                childFragmentManager,
                AddHealthServiceDialog::class.simpleName
            )
        }
    }

    /**
     * Observes LiveData from the ViewModel and updates the adapter when data changes.
     */
    private fun observeLiveData() {
        rosterViewModel.healthServiceLiveList.observe(viewLifecycleOwner) { serviceList ->
            healthServiceList.apply {
                clear()
                addAll(serviceList)
            }
            healthServiceAdapter?.notifyDataSetChanged()
        }

    }

    /**
     * Sets up the RecyclerView with the HealthServiceAdapter.
     */
    private fun setupHealthServiceAdapter() {
        binding.rvHealthService.apply {
            layoutManager = LinearLayoutManager(requireContext())
            healthServiceAdapter =
                HealthServiceAdapter(healthServiceList, this@HealthServiceRosterFragment)
            addItemDecoration(SpacingItemDecoration(16)) // Adds spacing between items
            adapter = healthServiceAdapter
        }
    }

    /**
     * Navigates to the Details screen if there are health services; shows a toast otherwise.
    //     */
//    private fun navigateToDetails() {
//        if (healthServiceList.isNotEmpty()) {
//            rosterViewModel.insertRoster()
//
//        } else {
//            ToastUtil.showShortToast(
//                requireContext(),
//                getString(R.string.please_add_health_service)
//            )
//        }
//    }

    /**
     * Initializes necessary data and retrieves the patient UUID from the intent.
     */
    private fun initViews() {
        patientUuid = requireActivity().intent?.getStringExtra("patientUuid")
    }

    /**
     * Handles delete action for a health service item.
     * @param view The view triggering the action
     * @param position The position of the item to delete
     * @param item The HealthServiceModel to delete
     */
    override fun onClickDelete(view: View, position: Int, item: HealthServiceModel) {
        rosterViewModel.deleteHealthService(position)
        healthServiceList.removeAt(position)
        healthServiceAdapter?.notifyItemRemoved(position)
    }

    /**
     * Handles edit action for a health service item and opens the edit dialog.
     * @param view The view triggering the action
     * @param position The position of the item to edit
     * @param item The HealthServiceModel to edit
     */
    override fun onClickEdit(view: View, position: Int, item: HealthServiceModel) {
        AddHealthServiceDialog().apply {
            setHealthServiceData(item.roasterViewQuestion, position)
        }.show(
            childFragmentManager,
            AddHealthServiceDialog::class.simpleName
        )


    }

    /**
     * Toggles the open/close state of a health service item.
     * @param view The view triggering the action
     * @param position The position of the item to toggle
     * @param item The HealthServiceModel to toggle
     */
    override fun onClickOpen(view: View, position: Int, item: HealthServiceModel) {
        item.isOpen = !item.isOpen
        healthServiceAdapter?.notifyItemChanged(position)
    }
}
