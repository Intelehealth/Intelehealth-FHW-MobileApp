package org.intelehealth.app.ui.rosterquestionnaire.ui.listeners

import android.view.View
import org.intelehealth.app.ui.rosterquestionnaire.model.HealthServiceModel
import org.intelehealth.app.ui.rosterquestionnaire.model.PregnancyOutComeModel

interface HealthServiceClickListener {
    fun onClickDelete(view: View , position: Int , item: HealthServiceModel)
    fun onClickEdit(view: View , position: Int , item: HealthServiceModel)
    fun onClickOpen(view: View , position: Int , item: HealthServiceModel)
}