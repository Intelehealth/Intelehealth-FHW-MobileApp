package org.intelehealth.app.ayu.visit.model;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.utilities.FlavorKeys;

import java.io.Serializable;

public class ReasonData implements Serializable {
    private String reasonName; // same as file name
    private String reasonNameLocalized; // locale wise name
    private boolean isSelected;
    private boolean isEnabled;
    private boolean isCustom;
    private String defaultReasonName;

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getReasonName() {
        return reasonName;
    }

    public void setReasonName(String reasonName) {
        this.reasonName = reasonName;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public String getReasonNameLocalized() {
        return reasonNameLocalized;
    }

    public void setReasonNameLocalized(String reasonNameLocalized) {
        this.reasonNameLocalized = reasonNameLocalized;
    }

    public void setCustom(boolean custom) {
        isCustom = custom;
    }

    public String getDefaultReasonName() {
        return defaultReasonName;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public void setDefaultReasonName(String defaultReasonName) {
        this.defaultReasonName = defaultReasonName;
    }
}
