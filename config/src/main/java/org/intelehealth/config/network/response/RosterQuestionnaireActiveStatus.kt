package org.intelehealth.config.network.response

import com.google.gson.annotations.SerializedName

data class RosterQuestionnaireActiveStatus( @SerializedName("general_roster")
                                            val generalRoster: Boolean,
                                            @SerializedName("pregnancy_roster")
                                            val pregnancyRoster: Boolean,
                                            @SerializedName("health_service_roster")
                                            val healthServiceRoster: Boolean)
