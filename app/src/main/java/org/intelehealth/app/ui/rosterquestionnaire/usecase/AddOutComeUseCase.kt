package org.intelehealth.app.ui.rosterquestionnaire.usecase

import org.intelehealth.app.ui.rosterquestionnaire.model.RoasterViewQuestion
import org.intelehealth.app.ui.rosterquestionnaire.repository.RosterRepository
import javax.inject.Inject

class AddOutComeUseCase @Inject constructor(private val repository: RosterRepository) {
    fun getOutComeList(existingRoasterQuestionList: ArrayList<RoasterViewQuestion>?): ArrayList<RoasterViewQuestion> {
        return if (existingRoasterQuestionList.isNullOrEmpty()) {
            repository.getOutcomeQuestionList()
        } else {
            existingRoasterQuestionList
        }
    }
}