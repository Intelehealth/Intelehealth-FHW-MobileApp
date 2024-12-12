package org.intelehealth.app.ui.rosterquestionnaire.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.intelehealth.app.R
import org.intelehealth.app.databinding.DialogAddHealthServiceBinding
import org.intelehealth.app.ui.dialog.CalendarDialog
import org.intelehealth.app.ui.rosterquestionnaire.model.RoasterViewQuestion
import org.intelehealth.app.ui.rosterquestionnaire.ui.adapter.MultiViewAdapter
import org.intelehealth.app.ui.rosterquestionnaire.ui.listeners.MultiViewListener
import org.intelehealth.app.ui.rosterquestionnaire.viewmodel.RosterViewModel
import org.intelehealth.app.utilities.SpacingItemDecoration


class AddHealthServiceDialog : DialogFragment(), MultiViewListener {

    private lateinit var pregnancyAdapter: MultiViewAdapter
    private lateinit var _binding: DialogAddHealthServiceBinding
    private lateinit var rosterViewModel: RosterViewModel
    private var healthServiceQuestionList: ArrayList<RoasterViewQuestion> = arrayListOf()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Inflate the binding
        _binding = DialogAddHealthServiceBinding.inflate(LayoutInflater.from(context))
        rosterViewModel = ViewModelProvider.create(requireActivity())[RosterViewModel::class]
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setView(_binding.root)

        val dialog = builder.create()
        dialog.setCancelable(true)

        val alertDialog: androidx.appcompat.app.AlertDialog = builder.create()
        alertDialog.window!!.setBackgroundDrawableResource(R.drawable.ui2_rounded_corners_dialog_bg) // show rounded corner for the dialog
        alertDialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND) // dim background
        requireContext().resources.getDimensionPixelSize(R.dimen.internet_dialog_width) // set width to your dialog.
        alertDialog.window!!.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)

        return alertDialog
    }


    override fun onResume() {
        super.onResume()
        setAdapter()
        setClickListeners()
    }

    private fun setClickListeners() {
        _binding.btnSave.setOnClickListener {
            if (isValidList(healthServiceQuestionList)) {
                rosterViewModel.addHealthService(healthServiceQuestionList)
                dismiss()
            }

        }
        _binding.btnCancel.setOnClickListener { dismiss() }
    }


    private fun setAdapter() {
        healthServiceQuestionList.addAll(rosterViewModel.getHealthServiceList())

        _binding.rvHealthServiceQuestions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            pregnancyAdapter = MultiViewAdapter(
                healthServiceQuestionList,
                this@AddHealthServiceDialog
            )
            adapter = pregnancyAdapter
            addItemDecoration(SpacingItemDecoration(16))
        }
    }

    private fun isValidList(healthServiceQuestionList: ArrayList<RoasterViewQuestion>): Boolean {
        healthServiceQuestionList.forEach {
            if (it.answer.isNullOrEmpty()) {
                pregnancyAdapter.updateErrorMessage(true)
                return false
            }
        }
        return true
    }


    override fun onItemClick(item: RoasterViewQuestion, position: Int, view: View) {
        CalendarDialog.showDatePickerDialog(object : CalendarDialog.OnDatePickListener {
            override fun onDatePick(day: Int, month: Int, year: Int, value: String?) {
                item.answer = value
                pregnancyAdapter.notifyItemChanged(position)

            }
        }, childFragmentManager)
    }

}
