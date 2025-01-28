package org.intelehealth.app.ui.rosterquestionnaire.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.app.databinding.ItemChildOutcomeBinding
import org.intelehealth.app.ui.rosterquestionnaire.model.RoasterViewQuestion

class PregnancyOutComeChildAdapter(
    private val items: List<RoasterViewQuestion>,
) : RecyclerView.Adapter<PregnancyOutComeChildAdapter.ChildViewHolder>() {

    inner class ChildViewHolder(private val binding: ItemChildOutcomeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RoasterViewQuestion) {
            binding.tvQuestion.text = item.question
            binding.tvAnswer.text = item.localAnswer ?: item.answer
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val binding = ItemChildOutcomeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChildViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
