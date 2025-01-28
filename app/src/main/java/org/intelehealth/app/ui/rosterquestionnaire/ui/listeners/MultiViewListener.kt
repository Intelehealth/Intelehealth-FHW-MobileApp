package org.intelehealth.app.ui.rosterquestionnaire.ui.listeners

import android.view.View
import org.intelehealth.app.ui.rosterquestionnaire.model.RoasterViewQuestion

interface MultiViewListener {
    fun onItemClick(
        item: RoasterViewQuestion,
        position: Int,
        view: View,
    )
}

