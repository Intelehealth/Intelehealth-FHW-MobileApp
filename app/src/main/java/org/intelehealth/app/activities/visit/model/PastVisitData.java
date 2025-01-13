package org.intelehealth.app.activities.visit.model;

import android.os.Parcel;
import android.os.Parcelable;

public class PastVisitData implements Parcelable {
    private String visitUUID;
    private String visitDate;
    private String chiefComplain;
    private String encounterAdultInitial;
    private String encounterVitals;
    private String diagnostics;


    public PastVisitData() {
    }

    public PastVisitData(Parcel in) {
        visitUUID = in.readString();
        visitDate = in.readString();
        chiefComplain = in.readString();
        encounterAdultInitial = in.readString();
        encounterVitals = in.readString();
        diagnostics = in.readString();
    }

    public static final Creator<PastVisitData> CREATOR = new Creator<PastVisitData>() {
        @Override
        public PastVisitData createFromParcel(Parcel in) {
            return new PastVisitData(in);
        }

        @Override
        public PastVisitData[] newArray(int size) {
            return new PastVisitData[size];
        }
    };

    public String getChiefComplain() {
        return chiefComplain;
    }

    public void setChiefComplain(String chiefComplain) {
        this.chiefComplain = chiefComplain;
    }

    public String getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(String visitDate) {
        this.visitDate = visitDate;
    }

    public String getVisitUUID() {
        return visitUUID;
    }

    public void setVisitUUID(String visitUUID) {
        this.visitUUID = visitUUID;
    }

    public String getEncounterVitals() {
        return encounterVitals;
    }

    public void setEncounterVitals(String encounterVitals) {
        this.encounterVitals = encounterVitals;
    }

    public String getEncounterAdultInitial() {
        return encounterAdultInitial;
    }

    public void setEncounterAdultInitial(String encounterAdultInitial) {
        this.encounterAdultInitial = encounterAdultInitial;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(visitUUID);
        parcel.writeString(visitDate);
        parcel.writeString(chiefComplain);
        parcel.writeString(encounterAdultInitial);
        parcel.writeString(encounterVitals);
        parcel.writeString(diagnostics);
    }
}
