package org.intelehealth.app.ui.rosterquestionnaire.repository

import org.intelehealth.app.ui.rosterquestionnaire.model.RoasterViewQuestion

interface RosterRepository
{
    fun getOutcomeQuestionList(): ArrayList<RoasterViewQuestion>
    fun getHealthServiceQuestionList(): ArrayList<RoasterViewQuestion>
}
