package org.intelehealth.app.ui.rosterquestionnaire.ui.listeners

import android.view.View
import org.intelehealth.app.ui.rosterquestionnaire.model.PregnancyOutComeModel

interface PregnancyOutcomeClickListener {
    fun onClickDelete(view: View , position: Int , item: PregnancyOutComeModel)
    fun onClickEdit(view: View , position: Int , item: PregnancyOutComeModel)
    fun onClickOpen(view: View , position: Int , item: PregnancyOutComeModel)
}