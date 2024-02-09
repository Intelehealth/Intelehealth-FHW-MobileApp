
package org.intelehealth.ezazi.models.dto;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.intelehealth.klivekit.chat.model.ItemHeader;

public class ObsDTO implements ItemHeader {

    @SerializedName("uuid")
    @Expose
    private String uuid;
    @SerializedName("encounteruuid")
    @Expose
    private String encounteruuid;
    @SerializedName("conceptuuid")
    @Expose
    private String conceptuuid;
    @SerializedName("value")
    @Expose
    private String value;
    @SerializedName("comment")
    @Expose
    private String comment;
    @SerializedName("obsServerModifiedDate")
    @Expose
    private String obsServerModifiedDate;
    @SerializedName("creator")
    @Expose
    private String creator;

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    @SerializedName("voided")
    @Expose
    private Integer voided;

    private String name;

    private String createdDate;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getEncounteruuid() {
        return encounteruuid;
    }

    public void setEncounteruuid(String encounteruuid) {
        this.encounteruuid = encounteruuid;
    }

    public String getConceptuuid() {
        return conceptuuid;
    }

    public void setConceptuuid(String conceptuuid) {
        this.conceptuuid = conceptuuid;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Integer getVoided() {
        return voided;
    }

    public void setVoided(Integer voided) {
        this.voided = voided;
    }

    public String getObsServerModifiedDate() {
        return obsServerModifiedDate;
    }

    public void setObsServerModifiedDate(String obsServerModifiedDate) {
        this.obsServerModifiedDate = obsServerModifiedDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean isHeader() {
        return false;
    }

    @NonNull
    @Override
    public String createdDate() {
        return null;
    }
}
