package org.intelehealth.app.activities.draftSurvey

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.intelehealth.app.R
import org.intelehealth.app.activities.patientDetailActivity.PatientDetailActivity2
import org.intelehealth.app.databinding.FollowupListItemBinding


/**
 * Created by - Prajwal W. on 26/11/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/

class DraftSurveyAdapter(context: Context, draftList: ArrayList<DraftSurveyModel>) :
    RecyclerView.Adapter<DraftSurveyAdapter.MyViewHolder>() {

    private val context: Context = context
    private val draftList: ArrayList<DraftSurveyModel> = draftList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
    : DraftSurveyAdapter.MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.followup_list_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: DraftSurveyAdapter.MyViewHolder, position: Int) {
        holder.patientNameAndOpenMrsId.text = draftList[position].patientNameAndOpenMrsId
        holder.patientOpenMrsId.text = draftList[position].patientAge

       /* holder.item.setOnClickListener {
            Intent(context, PatientDetailActivity2::class.java).also {
                context.startActivity(it)
            }     // TODO: handle this later.
        }*/
    }

    override fun getItemCount(): Int {
        return draftList.size
    }


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = FollowupListItemBinding.bind(itemView)
        val item = binding.fuCardviewItem
        val priorityTag = binding.flPriority.let { it.visibility = View.GONE }
        val shareiconLL = binding.shareiconLL.let { it.visibility = View.GONE }
        val shareImgBtn = binding.shareImgBtn.let { it.visibility = View.GONE }
        val endVisitBtn = binding.endVisitBtn.let { it.visibility = View.GONE }
        val patientNameAndOpenMrsId = binding.fuPatnameTxtview
        val patientOpenMrsId = binding.fuDateTxtview
        val fuItemCalendar = binding.fuItemCalendar.let { it.visibility = View.GONE }
    }

}
