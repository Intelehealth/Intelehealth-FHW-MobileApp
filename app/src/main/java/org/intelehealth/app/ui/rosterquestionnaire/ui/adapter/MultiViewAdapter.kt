package org.intelehealth.app.ui.rosterquestionnaire.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import org.intelehealth.app.R
import org.intelehealth.app.databinding.ItemBlankViewBinding
import org.intelehealth.app.databinding.ItemDatePickerViewBinding
import org.intelehealth.app.databinding.ItemEditTextViewBinding
import org.intelehealth.app.databinding.ItemSpinnerViewBinding
import org.intelehealth.app.ui.rosterquestionnaire.model.RoasterViewQuestion
import org.intelehealth.app.ui.rosterquestionnaire.ui.listeners.MultiViewListener
import org.intelehealth.app.ui.rosterquestionnaire.utilities.RoasterQuestionView
import org.intelehealth.app.utilities.ArrayAdapterUtils
import org.intelehealth.app.utilities.LanguageUtils
import org.intelehealth.app.utilities.extensions.hideError
import org.intelehealth.app.utilities.extensions.showDropDownError
import org.intelehealth.app.utilities.extensions.validate

private const val SPECIFY: String = "specify"

class MultiViewAdapter(
    private var items: ArrayList<RoasterViewQuestion> = ArrayList(),
    private val listener: MultiViewListener,
    private var isResulCheck: Boolean = false,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var errorPosition = -1

    override fun getItemViewType(position: Int): Int {
        return if (!items[position].isVisible) {
            R.layout.item_blank_view
        } else {
            items[position].layoutId.lavout
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = when (viewType) {
            RoasterQuestionView.SPINNER.lavout -> ItemSpinnerViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

            RoasterQuestionView.DATE_PICKER.lavout -> ItemDatePickerViewBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )

            RoasterQuestionView.EDIT_TEXT.lavout -> ItemEditTextViewBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
            // Add other layouts here
            else -> {
                ItemBlankViewBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ), parent, false
                )
            }
        }

        return GenericViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        (holder as GenericViewHolder).bind(item)
    }

    override fun getItemCount(): Int = items.size

    inner class GenericViewHolder(private val binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: RoasterViewQuestion) {
            val mContext = binding.root.context
            when (binding) {
                is ItemSpinnerViewBinding -> {

                    binding.tvSpinnerHeader.text = data.question
                    binding.spinner.apply {
                        val englishList =
                            LanguageUtils.getStringArrayInLocale(context, data.spinnerItem!!, "en")

                        val listItem = context.resources.getStringArray(data.spinnerItem)
                            .toList()
                        val adapter =
                            ArrayAdapterUtils.getObjectArrayAdapter(context, listItem)

                        if (adapter != this.adapter) {
                            setAdapter(adapter)
                        }
                        // if the spinner already selected then set the selected data otherwise set 'select' text
                        if (text.toString() != data.answer) {
                            if (!data.answer.isNullOrEmpty() && data.answer!!.contains(
                                    SPECIFY,
                                    true
                                )
                            ) {
                                binding.tilOtherText.visibility = View.VISIBLE
                                val newArrayAnswer = data.answer!!.split(":")
                                if (newArrayAnswer.size > 1) {
                                    setText(context.getString(R.string.other_specify), false)
                                    binding.etOther.setText(newArrayAnswer[1])
                                }

                            } else {
                                setText(data.localAnswer, false)
                                binding.tilOtherText.visibility = View.GONE
                            }

                        }
                        // Handling error
                        if (bindingAdapterPosition == errorPosition && data.answer.isNullOrEmpty()) {
                            binding.textInputLayRelation.showDropDownError(
                                data.answer,
                                data.errorMessage
                            )
                        } else {
                            binding.textInputLayRelation.hideError()
                        }
                        // Set the data into model
                        setOnItemClickListener { _, _, position, _ ->
                            data.answer = englishList[position]
                            data.localAnswer = listItem[position]
                            binding.textInputLayRelation.hideError()
                            setSpinnerOtherVisibility(data, binding)
                            listener.onItemClick(data, bindingAdapterPosition, this)
                        }
                    }
                    binding.etOther.addTextChangedListener(object : TextWatcher {
                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int,
                        ) {
                            if (!s.isNullOrEmpty()) {
                                data.answer = LanguageUtils.getStringInLocale(
                                    mContext,
                                    R.string.other_specify,
                                    "en"
                                ) + ":" + s.toString()
                            } else {
                                data.answer = if (!data.answer.isNullOrEmpty()) {
                                    data.answer!!.split(":")[0]
                                } else {
                                    null
                                }
                            }
                        }

                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int,
                        ) {
                        }

                        override fun afterTextChanged(s: Editable?) {}
                    })
                }

                is ItemDatePickerViewBinding -> {
                    binding.tvDatePickerQuestion.text = data.question
                    binding.textInputETDob.setText(data.answer ?: "")
                    if (bindingAdapterPosition == errorPosition && data.answer.isNullOrEmpty()) {
                        binding.textInputLayDob.validate(binding.textInputETDob, data.errorMessage)
                    } else {
                        binding.textInputLayDob.hideError()
                    }
                    binding.textInputETDob.setOnClickListener {
                        listener.onItemClick(data, bindingAdapterPosition, it)
                    }
                }

                is ItemEditTextViewBinding -> {
                    binding.tvEditTextHeader.text = data.question
                    binding.tilEtAnswer.setText(data.answer ?: "")
                    binding.tilEtAnswer.inputType = data.inputType
                    binding.tilEtAnswer.doOnTextChanged { text, start, before, count ->
                        data.answer = text.toString()
                        binding.tilAnswer.hideError()
                    }
                    if (bindingAdapterPosition == errorPosition && data.answer.isNullOrEmpty()) {
                        binding.tilAnswer.validate(binding.tilEtAnswer, data.errorMessage)
                    } else {
                        binding.tilAnswer.hideError()
                    }
                    // Explicitly request focus and show the keyboard
                    binding.tilEtAnswer.setOnFocusChangeListener { _, hasFocus ->
                        val imm =
                            binding.root.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        if (hasFocus) {
                            imm.showSoftInput(binding.tilEtAnswer, InputMethodManager.SHOW_IMPLICIT)
                        } else {
                            imm.hideSoftInputFromWindow(binding.tilEtAnswer.windowToken, 0)
                        }
                    }
                }

            }
        }

        private fun setSpinnerOtherVisibility(
            data: RoasterViewQuestion,
            binding: ItemSpinnerViewBinding,
        ) {
            if (data.answer!!.contains(SPECIFY, true)) {
                binding.tilOtherText.visibility = View.VISIBLE
            } else {
                binding.tilOtherText.visibility = View.GONE
                binding.etOther.setText("")
            }
        }

        private fun getPositionOfSpinnerItem(key: String?, list: Array<String>): Int? {
            if (key.isNullOrEmpty()) {
                return null
            }
            list.forEachIndexed { index, item ->
                if (item == key) {
                    return index
                }
            }
            return null
        }
    }

    fun updateErrorMessage(position: Int) {
        isResulCheck = true
        errorPosition = position
        notifyItemChanged(position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun notifyList(list: ArrayList<RoasterViewQuestion>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }


}
