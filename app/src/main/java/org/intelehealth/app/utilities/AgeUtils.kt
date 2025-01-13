package org.intelehealth.app.utilities

import org.intelehealth.app.ui.dialog.CalendarDialog
import org.intelehealth.klivekit.utils.DateTimeUtils
import java.util.Calendar

/**
 * Created by Tanvir Hasan on 14-05-2024 : 14-38.
 * Email: mhasan@intelehealth.org
 */
object AgeUtils {

    // guardian required if age is below 18 or equal 18
    // guardian not require for above 18 patient ex: 18y 1m
    fun isGuardianRequired(years: Int, months: Int, days: Int): Boolean {
        return ((years < 18) || (years == 18 && months == 0 && days == 0))
    }


}