package org.intelehealth.ezazi.activities.addNewPatient.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Kaveri Zaware on 12-06-2023
 * email - kaveri@intelehealth.org
 **/
public class DistData implements Serializable {
    @Expose
    @SerializedName("name")
    private String name;

    @Expose
    @SerializedName("name-hi")
    private String nameHindi;

    @Expose
    @SerializedName("tahasil")
    private List<String> tahasilList;

    public List<String> getTahasilList() {
        return tahasilList;
    }

    public void setTahasilList(List<String> tahasilList) {
        this.tahasilList = tahasilList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameHindi() {
        return nameHindi;
    }

    public void setNameHindi(String nameHindi) {
        this.nameHindi = nameHindi;
    }
}