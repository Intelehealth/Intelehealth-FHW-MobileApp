package org.intelehealth.app.utilities

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.annotation.ArrayRes
import com.google.gson.Gson
import org.intelehealth.app.activities.identificationActivity.model.Block
import org.intelehealth.app.activities.identificationActivity.model.DistData
import org.intelehealth.app.activities.identificationActivity.model.GramPanchayat
import org.intelehealth.app.activities.identificationActivity.model.ProvincesAndCities
import org.intelehealth.app.activities.identificationActivity.model.StateData
import org.intelehealth.app.activities.identificationActivity.model.StateDistMaster
import org.intelehealth.app.activities.identificationActivity.model.Village
import org.intelehealth.app.app.IntelehealthApplication
import java.util.Locale

/**
 * Created by Vaghela Mithun R. on 26-06-2024 - 20:19.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
object LanguageUtils {
    private const val STATE_DISTRICT_JSON = "state_district_tehsil.json"
    private const val PROVINCE_AND_CITIES_JSON = "province_and_cities.json"

    private var provincesAndCities: ProvincesAndCities? = null

    @JvmStatic
    fun getLocalLang(): String {
        val context = IntelehealthApplication.getAppContext()
        return SessionManager(context).appLanguage
    }

    @JvmStatic
    fun getState(state: String): StateData? {
        return parseStatesJson().stateDataList.find { it.state == state }
    }

    @JvmStatic
    fun getProvince(province: String): String? {
        val locations = getProvincesAndCities()
        val index =  getProvincesAndCities().provinces.indexOf(province)
        return when(getLocalLang()){
            "ru"-> locations.provinces_ru[index]
            else -> locations.provinces[index]
        }
    }

    @JvmStatic
    fun getCity(city: String): String? {
        val locations = getProvincesAndCities()
        val index =  getProvincesAndCities().cities.indexOf(city)
        return when(getLocalLang()){
            "ru"-> locations.cities_ru[index]
            else -> locations.cities[index]
        }
    }

    @JvmStatic
    fun getCodeOfHf(province: String): String? {
        if(province.isEmpty()) return ""
        val codeMap =  getProvincesAndCities().code_of_hf.find { it.contains(province) }
        return codeMap?.split(":")?.last()
    }

    @JvmStatic
    fun getStateList(): List<StateData>? {
        return parseStatesJson().stateDataList
    }


    @JvmStatic
    fun parseStatesJson(): StateDistMaster {
        val context = IntelehealthApplication.getAppContext()
        val jsonObject = FileUtils.encodeJSON(context, STATE_DISTRICT_JSON)

        return Gson().fromJson(
            jsonObject.toString(),
            StateDistMaster::class.java
        )
    }

    /**
     * specially for Kazakhstan
     */
    @JvmStatic
    fun getProvincesAndCities(): ProvincesAndCities {
        val context = IntelehealthApplication.getAppContext()
        val jsonObject = FileUtils.encodeJSON(context, PROVINCE_AND_CITIES_JSON)

        if(provincesAndCities == null){
            provincesAndCities =  Gson().fromJson(
                jsonObject.toString(),
                ProvincesAndCities::class.java
            )
        }

        return provincesAndCities!!
    }

    @JvmStatic
    fun getDistrict(state: StateData?, district: String): DistData? {
        return state?.distDataList?.find { it.name == district }
    }

    @JvmStatic
    fun getBlock(district: DistData?, block: String?): Block? {
        return block?.let { return@let district?.blocks?.find { it.name == block } }
    }

    @JvmStatic
    fun getGramPanchayat(block: Block?, gramPanchayat: String?): GramPanchayat? {
        return gramPanchayat?.let { return@let block?.gramPanchayats?.find { it.name == gramPanchayat } }
    }

    @JvmStatic
    fun getVillage(gramPanchayat: GramPanchayat?, village: String?): Village? {
        return village?.let { return@let gramPanchayat?.villages?.find { it.name == village } }
    }

    @JvmStatic
    fun getStateLocal(state: StateData): String {
        if (getLocalLang().equals("hi")) return state.stateHindi
        return state.state
    }

    @JvmStatic
    fun getDistrictLocal(district: DistData): String {
        if (getLocalLang().equals("hi")) return district.nameHindi
        return district.name
    }

    @JvmStatic
    fun getBlockLocal(block: Block): String {
        if (getLocalLang().equals("hi")) return block.nameHindi ?: block.name ?: ""
        return block.name ?: ""
    }

    @JvmStatic
    fun getGramPanchayatLocal(gramPanchayat: GramPanchayat): String {
        if (getLocalLang().equals("hi")) return gramPanchayat.nameHindi ?: gramPanchayat.name ?: ""
        return gramPanchayat.name ?: ""
    }

    @JvmStatic
    fun getVillageLocal(village: Village): String {
        if (getLocalLang().equals("hi")) return village.nameHindi ?: village.name ?: ""
        return village.name ?: ""
    }

    @JvmStatic
    fun getSpecificLocalResource(context: Context, locale: String): Resources {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(Locale(locale))
        return context.createConfigurationContext(configuration).resources
    }

    @JvmStatic
    fun getLocalValueFromArray(
        context: Context,
        dbString: String,
        @ArrayRes arrayResId: Int
    ): String {
        return if (SessionManager(context).appLanguage.equals("en").not()) {
            val array = context.resources.getStringArray(arrayResId)
            val index = getSpecificLocalResource(context, "en")
                .getStringArray(arrayResId).indexOf(dbString)
            return if (index > 0) array[index]
            else ""
        } else dbString
    }

    fun getProvinceByLocal(it: ProvincesAndCities): List<String> {
        return when(getLocalLang()){
            "ru"-> it.provinces_ru
            else -> it.provinces
        }
    }

    fun getCityByLocal(it: ProvincesAndCities): List<String> {
        return when(getLocalLang()){
            "ru"-> it.cities_ru
            else -> it.cities
        }
    }
}