package org.intelehealth.app.ui.draftsurvey.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity2
import org.intelehealth.app.databinding.DraftSurveyItemBinding
import org.intelehealth.app.models.dto.PatientDTO
import org.intelehealth.app.utilities.SessionManager
import org.intelehealth.app.utilities.StringUtils
import org.intelehealth.core.shared.ui.adapter.BaseRecyclerViewAdapter
import org.intelehealth.core.shared.ui.viewholder.BaseViewHolder
import java.io.Serializable

class DraftSurveyAdapter(
    context: Context, private var draftList: List<PatientDTO>,
) : BaseRecyclerViewAdapter<PatientDTO>(context, draftList.toMutableList()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return DraftSurveyItemBinding.inflate(inflater, parent, false).let {
            MyViewHolder(it)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (items.isEmpty()) {
            return
        }
        if (holder is MyViewHolder) {
            viewHolderClickListener?.let { holder.setViewClickListener(it) }

            if (position in 0..<itemCount) {
                holder.bind(getItem(position), context)
            }
        }
    }

    override fun getItemCount(): Int {
        return draftList.size
    }

    fun updateList(newList: List<PatientDTO>) {
        draftList = newList
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
    class MyViewHolder(private val binding: DraftSurveyItemBinding) :
        BaseViewHolder(binding.root) {

        fun bind(patientDTO: PatientDTO, context: Context) {
            binding.draftCardItem.tag = patientDTO
            binding.draftCardItem.setOnClickListener(this)
            StringUtils.setGenderAgeLocalByCommaContact(
                context,
                binding.draftGenderAndAge,
                patientDTO.dateofbirth,
                patientDTO.gender,
                SessionManager(context)
            )
            binding.patientDto = patientDTO
        }
    }
    fun select(patientDTO: PatientDTO) {
        val intent = Intent(context, PatientDetailActivity2::class.java).apply {
            putExtra("patientUuid", patientDTO.uuid)
            putExtra("tag", "draft")
            val args = Bundle().apply {
                putSerializable("patientDTO", patientDTO as? Serializable)
            }
            putExtra("BUNDLE", args)

        }
        context.startActivity(intent)
        (context as Activity).finish()
    }

}
