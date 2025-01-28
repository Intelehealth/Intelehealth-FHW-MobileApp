package org.intelehealth.app.ui.rosterquestionnaire.utilities

import org.intelehealth.app.R

enum class RoasterQuestionView(val lavout : Int) {
    SPINNER(R.layout.item_spinner_view),
    DATE_PICKER(R.layout.item_date_picker_view),
    EDIT_TEXT(R.layout.item_edit_text_view)
}