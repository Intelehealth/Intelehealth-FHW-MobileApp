package org.intelehealth.app.ui.rosterquestionnaire.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.app.databinding.ItemPregnancyOutcomeBinding
import org.intelehealth.app.ui.rosterquestionnaire.model.HealthServiceModel
import org.intelehealth.app.ui.rosterquestionnaire.ui.listeners.HealthServiceClickListener
import org.intelehealth.app.utilities.SpacingItemDecoration

class HealthServiceAdapter(
    private val items: MutableList<HealthServiceModel>,
    private val listener: HealthServiceClickListener,
) : RecyclerView.Adapter<HealthServiceAdapter.ParentViewHolder>() {

    inner class ParentViewHolder(private val binding: ItemPregnancyOutcomeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.rvOutComeItem.apply {
                layoutManager = LinearLayoutManager(
                    binding.root.context,
                    LinearLayoutManager.VERTICAL,
                    false
                )
                addItemDecoration(SpacingItemDecoration(12))
            }
            binding.ivDropDown.setOnClickListener {
                listener.onClickOpen(it, bindingAdapterPosition, items[bindingAdapterPosition])
            }
            binding.tvTitle.setOnClickListener {
                listener.onClickOpen(it, bindingAdapterPosition, items[bindingAdapterPosition])
            }
            binding.btnDelete.setOnClickListener {
                listener.onClickDelete(it, bindingAdapterPosition, items[bindingAdapterPosition])
            }
            binding.btnEdit.setOnClickListener {
                listener.onClickEdit(it, bindingAdapterPosition, items[bindingAdapterPosition])
            }
        }

        fun bind(item: HealthServiceModel) {
            binding.tvTitle.text = item.title ?: ""
            binding.rvOutComeItem.adapter =
                HealthServiceChildAdapter(item.roasterViewQuestion)
            if (item.isOpen) {
                binding.rvOutComeItem.visibility = View.VISIBLE
                binding.bottomDivider.visibility = View.VISIBLE
                binding.ivDropDown.rotation = 180f
            } else {
                binding.rvOutComeItem.visibility = View.GONE
                binding.bottomDivider.visibility = View.GONE
                binding.ivDropDown.rotation = 0f
            }


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val binding = ItemPregnancyOutcomeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ParentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        holder.bind(items[position])

    }

    override fun getItemCount(): Int = items.size
}
