package org.intelehealth.app.ui.rosterquestionnaire.ui

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import org.intelehealth.app.ui.rosterquestionnaire.viewmodel.RosterViewModel

abstract class BaseRosterFragment(@LayoutRes layoutResId: Int) : Fragment(layoutResId) {

    protected lateinit var rosterViewModel: RosterViewModel


}