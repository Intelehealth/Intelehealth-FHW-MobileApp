package org.intelehealth.app.ui.rosterquestionnaire.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class HealthIssues(
    @SerializedName("healthIssueReported")
    @Expose
    var healthIssueReported: String = "",

    @SerializedName("numberOfEpisodesInTheLastYear")
    @Expose
    var numberOfEpisodesInTheLastYear: String = "",

    @SerializedName("primaryHealthcareProviderValue")
    @Expose
    var primaryHealthcareProviderValue: String = "",

    @SerializedName("firstLocationOfVisit")
    @Expose
    var firstLocationOfVisit: String = "",

    @SerializedName("referredTo")
    @Expose
    var referredTo: String = "",

    @SerializedName("modeOfTransportation")
    @Expose
    var modeOfTransportation: String = "",

    @SerializedName("averageCostOfTravelAndStayPerEpisode")
    @Expose
    var averageCostOfTravelAndStayPerEpisode: String = "",

    @SerializedName("averageCostOfConsultation")
    @Expose
    var averageCostOfConsultation: String = "",

    @SerializedName("averageCostOfMedicine")
    @Expose
    var averageCostOfMedicine: String = "",

    @SerializedName("scoreForExperienceOfTreatment")
    @Expose
    var scoreForExperienceOfTreatment: String = "",
)
