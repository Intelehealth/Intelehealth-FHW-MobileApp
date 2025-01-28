package org.intelehealth.config.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "tbl_patient_diagnostics")
data class Diagnostics (
    val name: String,

    @SerializedName("key")

    @PrimaryKey
    val diagnosticsKey: String,

    val uuid: String,

    @SerializedName("is_mandatory")
    val isMandatory: Boolean,

    /*@SerializedName("is_enabled")
    val isEnabled: Boolean*/
    )

