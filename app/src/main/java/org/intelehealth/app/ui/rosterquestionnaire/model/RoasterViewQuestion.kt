package org.intelehealth.app.ui.rosterquestionnaire.model

import android.text.InputType
import org.intelehealth.app.ui.rosterquestionnaire.utilities.RoasterQuestionView

data class RoasterViewQuestion(
    val layoutId: RoasterQuestionView,
    val data: Any? = null,
    val question: String,
    val spinnerItem: Int ?=null,
    var spinnerPosition: Int?= null,
    var answer: String?= null,
    val errorMessage: String= "",
    val inputType: Int = InputType.TYPE_CLASS_TEXT,
    var attribute: String = "",
    var uuid: String? = null,
    var isVisible: Boolean = true,
    var localAnswer: String?= null,
)
