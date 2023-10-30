package org.intelehealth.app.models.dispenseAdministerModel;

import java.io.Serializable;

public class MedicationAidModel implements Serializable {
    private String uuid;
    private String value;
    private boolean isChecked;

    public MedicationAidModel(String uuid, String value, boolean isChecked) {
        this.uuid = uuid;
        this.value = value;
        this.isChecked = isChecked;
    }

    public MedicationAidModel() {
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}