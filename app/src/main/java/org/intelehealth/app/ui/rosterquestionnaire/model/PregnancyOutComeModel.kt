package org.intelehealth.app.ui.rosterquestionnaire.model

data class PregnancyOutComeModel(
    val title: String?,
    val roasterViewQuestion: List<RoasterViewQuestion>,
    var isOpen: Boolean = false,
)