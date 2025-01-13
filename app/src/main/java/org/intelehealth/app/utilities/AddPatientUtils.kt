package org.intelehealth.app.utilities

import android.content.Context
import android.content.Intent
import org.intelehealth.app.BuildConfig
import org.intelehealth.app.activities.onboarding.PersonalConsentActivity
import org.intelehealth.app.activities.onboarding.PrivacyPolicyActivity_New
import java.lang.ref.WeakReference

class AddPatientUtils {
    companion object {

        @JvmStatic
        fun navigate(context: Context) {
            val weakContext = WeakReference(context)
            weakContext.get()?.let { ctx ->
                val intent = if (BuildConfig.FLAVOR_client == FlavorKeys.UNFPA) {
                    Intent(ctx, PersonalConsentActivity::class.java)
                } else {
                    Intent(ctx, PrivacyPolicyActivity_New::class.java).apply {
                        putExtra("intentType", "navigateFurther")
                        putExtra("add_patient", "add_patient")
                    }
                }
                ctx.startActivity(intent)
            }
        }
    }
}