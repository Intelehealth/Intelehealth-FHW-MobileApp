package org.intelehealth.app.database.dao;

import static org.intelehealth.app.utilities.UuidDictionary.ENCOUNTER_ADULTINITIAL;
import static org.intelehealth.app.utilities.UuidDictionary.ENCOUNTER_VISIT_COMPLETE;
import static org.intelehealth.app.utilities.UuidDictionary.ENCOUNTER_VISIT_NOTE;
import static org.intelehealth.app.utilities.UuidDictionary.ENCOUNTER_VITALS;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.models.FollowUpNotificationData;
import org.intelehealth.app.models.NotificationModel;
import org.intelehealth.app.models.dto.EncounterDTO;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.utilities.CustomLog;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EncounterDAO {
    private static final String TAG = "EncounterDAO";

    private String tag = EncounterDAO.class.getSimpleName();
    private long createdRecordsCount = 0;

    public boolean insertEncounter(List<EncounterDTO> encounterDTOS) throws DAOException {

        boolean isInserted = true;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            for (EncounterDTO encounter : encounterDTOS) {
                createEncounters(encounter, db);
            }
            db.setTransactionSuccessful();

        } catch (SQLException e) {
            isInserted = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();

        }
        return isInserted;
    }

    private boolean createEncounters(EncounterDTO encounter, SQLiteDatabase db) throws DAOException {
        boolean isCreated = false;

        ContentValues values = new ContentValues();
        try {

            values.put("uuid", encounter.getUuid());
            values.put("visituuid", encounter.getVisituuid());
            values.put("encounter_type_uuid", encounter.getEncounterTypeUuid());
            values.put("provider_uuid", encounter.getProvideruuid());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("sync", encounter.getSyncd());
            values.put("voided", encounter.getVoided());
            values.put("privacynotice_value", encounter.getPrivacynotice_value());
            //CustomLog.d("VALUES:", "VALUES: " + values);
            createdRecordsCount = db.insertWithOnConflict("tbl_encounter", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
        }
        return isCreated;
    }


    public boolean createEncountersToDB(EncounterDTO encounter) throws DAOException {
        boolean isCreated = false;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        try {

            values.put("uuid", encounter.getUuid());
            values.put("visituuid", encounter.getVisituuid());
            values.put("encounter_time", encounter.getEncounterTime());
            values.put("encounter_type_uuid", encounter.getEncounterTypeUuid());
            values.put("provider_uuid", encounter.getProvideruuid());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("sync", "false");
            values.put("voided", encounter.getVoided());
            values.put("privacynotice_value", encounter.getPrivacynotice_value());
            createdRecordsCount = db.insertWithOnConflict("tbl_encounter", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            if (createdRecordsCount != 0)
                isCreated = true;
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();

        }
        return isCreated;
    }

    public String getEncounterTypeUuid(String attr) {
        String encounterTypeUuid = "";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT uuid FROM tbl_uuid_dictionary where name = ? COLLATE NOCASE", new String[]{attr});
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                encounterTypeUuid = cursor.getString(cursor.getColumnIndexOrThrow("uuid"));
            }
        }
        cursor.close();

        return encounterTypeUuid;
    }

    public List<EncounterDTO> unsyncedEncounters() {
        List<EncounterDTO> encounterDTOList = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        //Distinct keyword is used to remove all duplicate records.
        Cursor idCursor = db.rawQuery("SELECT distinct a.uuid,a.visituuid,a.encounter_type_uuid,a.provider_uuid,a.encounter_time,a.voided,a.privacynotice_value FROM tbl_encounter a,tbl_obs b WHERE (a.sync = ? OR a.sync=?) AND a.uuid = b.encounteruuid AND b.sync='false' AND b.voided='0' ", new String[]{"false", "0"});
        EncounterDTO encounterDTO = new EncounterDTO();
        //CustomLog.d("RAINBOW: ", "RAINBOW: " + idCursor.getCount());
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                encounterDTO = new EncounterDTO();
                encounterDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                encounterDTO.setVisituuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visituuid")));
                encounterDTO.setEncounterTypeUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("encounter_type_uuid")));
                encounterDTO.setProvideruuid(idCursor.getString(idCursor.getColumnIndexOrThrow("provider_uuid")));
                CustomLog.d("ENCO", "ENCO_PROV: " + idCursor.getString(idCursor.getColumnIndexOrThrow("provider_uuid")));
                encounterDTO.setEncounterTime(idCursor.getString(idCursor.getColumnIndexOrThrow("encounter_time")));
                CustomLog.d("ENCO", "ENCO_TIME: " + idCursor.getString(idCursor.getColumnIndexOrThrow("encounter_time")));
                encounterDTO.setVoided(idCursor.getInt(idCursor.getColumnIndexOrThrow("voided")));
                encounterDTO.setPrivacynotice_value(idCursor.getString(idCursor.getColumnIndexOrThrow("privacynotice_value")));
                encounterDTOList.add(encounterDTO);
            }
        }
        idCursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();

        Gson gson = new Gson();
        //CustomLog.d("ENC_GSON: ", "ENC_GSON: " + gson.toJson(encounterDTOList));
        return encounterDTOList;
    }

    public List<EncounterDTO> getAllEncounters() {
        List<EncounterDTO> encounterDTOList = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_encounter", null);
        EncounterDTO encounterDTO = new EncounterDTO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                encounterDTO = new EncounterDTO();
                encounterDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                encounterDTO.setVisituuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visituuid")));
                encounterDTO.setEncounterTypeUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("encounter_type_uuid")));
                encounterDTO.setProvideruuid(idCursor.getString(idCursor.getColumnIndexOrThrow("provider_uuid")));
                encounterDTO.setEncounterTime(idCursor.getString(idCursor.getColumnIndexOrThrow("encounter_time")));
                encounterDTO.setVoided(idCursor.getInt(idCursor.getColumnIndexOrThrow("voided")));
                encounterDTO.setPrivacynotice_value(idCursor.getString(idCursor.getColumnIndexOrThrow("privacynotice_value")));
                encounterDTOList.add(encounterDTO);
            }
        }
        idCursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
//        db.close();

        return encounterDTOList;
    }

    public EncounterDTO getEncounterByVisitUUID(String visitUUID) {

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_encounter where visituuid = ? limit 1", new String[]{visitUUID});
        EncounterDTO encounterDTO = new EncounterDTO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                encounterDTO = new EncounterDTO();
                encounterDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                encounterDTO.setVisituuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visituuid")));
                encounterDTO.setEncounterTypeUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("encounter_type_uuid")));
                encounterDTO.setProvideruuid(idCursor.getString(idCursor.getColumnIndexOrThrow("provider_uuid")));
                encounterDTO.setEncounterTime(idCursor.getString(idCursor.getColumnIndexOrThrow("encounter_time")));
                encounterDTO.setVoided(idCursor.getInt(idCursor.getColumnIndexOrThrow("voided")));
                encounterDTO.setPrivacynotice_value(idCursor.getString(idCursor.getColumnIndexOrThrow("privacynotice_value")));

            }
        }
        idCursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
//        db.close();

        return encounterDTO;
    }

    public boolean updateEncounterSync(String synced, String uuid) throws DAOException {
        boolean isUpdated = true;

        Logger.logD("encounterdao", "updatesynv encounter " + uuid + "" + synced);
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        String whereclause = "uuid=?";
        String[] whereargs = {uuid};
        try {
            values.put("sync", synced);

            values.put("uuid", uuid);

            int i = db.update("tbl_encounter", values, whereclause, whereargs);
            //Logger.logD(tag, "updated" + i);
            db.setTransactionSuccessful();
        } catch (SQLException sql) {
            //Logger.logD(tag, "updated" + sql.getMessage());
            throw new DAOException(sql.getMessage());
        } finally {
            db.endTransaction();
        }

        return isUpdated;
    }

    public boolean setEmergency(String visitUuid, boolean emergencyChecked) throws DAOException {
        //delete any existing emergency encounter and insert new
        //this is the expected behavior in openMRS
        boolean isExecuted = false;
        EncounterDAO encounterDAO = new EncounterDAO();
        String emergency_uuid = encounterDAO.getEncounterTypeUuid("EMERGENCY");
        SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        //db.beginTransaction();
        ContentValues values = new ContentValues();
        String whereclause = "visituuid = ? AND encounter_type_uuid = ? ";
        String[] whereargs = {visitUuid, emergency_uuid};
        try {
            values.put("voided", "1");
            values.put("sync", false);
            int i = db.update("tbl_encounter", values, whereclause, whereargs);
            Logger.logD("encounter", "description" + i);
            // db.setTransactionSuccessful();
        } catch (SQLException sql) {
            Logger.logD("encounter", "encounter" + sql.getMessage());
            FirebaseCrashlytics.getInstance().recordException(sql);
            throw new DAOException(sql.getMessage());
        } finally {
            //   db.endTransaction();

        }
        if (emergencyChecked) {
            String encounteruuid = UUID.randomUUID().toString();
            EncounterDTO encounterDTO = new EncounterDTO();
            encounterDTO.setUuid(encounteruuid);
            encounterDTO.setVisituuid(visitUuid);
            encounterDTO.setVoided(0);
            encounterDTO.setEncounterTypeUuid(emergency_uuid);
            encounterDTO.setEncounterTime(AppConstants.dateAndTimeUtils.currentDateTime());
            encounterDTO.setSyncd(false);
            encounterDTO.setProvideruuid(sessionManager.getProviderID());
            CustomLog.d("DTO", "DTOdao: " + encounterDTO.getProvideruuid());

            encounterDAO.createEncountersToDB(encounterDTO);

            ObsDTO obsDTO = new ObsDTO();
            ObsDAO obsDAO = new ObsDAO();
            obsDTO.setConceptuuid(UuidDictionary.EMERGENCY_OBS);
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setUuid(UUID.randomUUID().toString());
            obsDTO.setEncounteruuid(encounteruuid);
            obsDTO.setValue("emergency");
            obsDAO.insertObs(obsDTO);
        }
        return isExecuted;
    }

    public String getEmergencyEncounters(String visitUuid, String encounterType) throws DAOException {
        String uuid = "";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            Cursor idCursor = db.rawQuery("SELECT uuid FROM tbl_encounter where visituuid = ? AND " +
                    "encounter_type_uuid = ? AND voided='0' COLLATE NOCASE", new String[]{visitUuid, encounterType});

            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    uuid = idCursor.getString(idCursor.getColumnIndexOrThrow("uuid"));
                }
            }
            idCursor.close();
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            throw new DAOException(e);
        } finally {
            db.endTransaction();

        }
        return uuid;
    }


    public boolean updateEncounterModifiedDate(String encounterUuid) throws DAOException {
        boolean isUpdated = true;
        Logger.logD("encounterdao", "update encounter date and time" + encounterUuid + "" + AppConstants.dateAndTimeUtils.currentDateTime());
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        String whereclause = "uuid=?";
        String[] whereargs = {encounterUuid};
        try {
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("encounter_time", AppConstants.dateAndTimeUtils.currentDateTime());
            int i = db.update("tbl_encounter", values, whereclause, whereargs);
            Logger.logD(tag, "updated" + i);
            db.setTransactionSuccessful();
        } catch (SQLException sql) {
            Logger.logD(tag, "updated" + sql.getMessage());
            throw new DAOException(sql.getMessage());
        } finally {
            db.endTransaction();


        }

        return isUpdated;
    }

    public static List<NotificationModel> check_visit_is_VISIT_COMPLETE_ENC(String currentDate) {
        List<NotificationModel> patientDTOList = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        Cursor idCursor = db.rawQuery("SELECT p.first_name, p.last_name, p.uuid as patientuuid, " +
                        "v.uuid as visitUUID, p.patient_photo, v.startdate, p.gender, p.openmrs_id, p.date_of_birth, " +
                        "substr(o.obsservermodifieddate, 1, 10) as obs_server_modified_date from " +
                        "tbl_patient p, tbl_visit v, tbl_encounter e, tbl_obs o WHERE " +
                        "p.uuid = v.patientuuid AND v.uuid = e.visituuid AND e.uuid = o.encounteruuid AND " +
                        "e.encounter_type_uuid = ? AND obs_server_modified_date = ? AND " +
                        "(e.sync = '1' OR e.sync = 'true' OR e.sync = 'TRUE') COLLATE NOCASE",
                new String[]{ENCOUNTER_VISIT_COMPLETE, currentDate});   // notification type: Prescription.

        try {
            if (idCursor.moveToFirst()) {
                do {
                    NotificationModel model = new NotificationModel();

                    model.setFirst_name(idCursor.getString(idCursor.getColumnIndexOrThrow("first_name")));
                    model.setLast_name(idCursor.getString(idCursor.getColumnIndexOrThrow("last_name")));

                    model.setUuid(UUID.randomUUID().toString());
                    model.setPatientuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("patientuuid")));
                    model.setDescription(model.getFirst_name() + " " + model.getLast_name() + "\'s prescription was received!");
                    model.setObs_server_modified_date(idCursor.getString(idCursor.getColumnIndexOrThrow("obs_server_modified_date")));
                    model.setNotification_type("Prescription");
                    model.setSync("TRUE");

                    model.setVisitUUID(idCursor.getString(idCursor.getColumnIndexOrThrow("visitUUID")));
                    model.setPatient_photo(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_photo")));
                    model.setVisit_startDate(idCursor.getString(idCursor.getColumnIndexOrThrow("startdate")));
                    model.setGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                    model.setOpenmrsID(idCursor.getString(idCursor.getColumnIndexOrThrow("openmrs_id")));
                    model.setDate_of_birth(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                    model.setIsDeleted("false");
                    patientDTOList.add(model);
                }
                while (idCursor.moveToNext());
            }
        } catch (SQLException e) {

        }

        idCursor.close();
        return patientDTOList;
    }

    public static List<NotificationModel> fetchAllVisits() {
        List<NotificationModel> patientDTOList = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        Cursor idCursor = db.rawQuery("SELECT p.first_name, p.last_name, p.uuid as patientuuid, " +
                        "v.uuid as visitUUID, p.patient_photo, v.startdate, p.gender, p.openmrs_id, p.date_of_birth, " +
                        "substr(o.obsservermodifieddate, 1, 10) as obs_server_modified_date from " +
                        "tbl_patient p, tbl_visit v, tbl_encounter e, tbl_obs o WHERE " +
                        "p.uuid = v.patientuuid AND v.uuid = e.visituuid AND e.uuid = o.encounteruuid AND " +
                        "(e.sync = '1' OR e.sync = 'true' OR e.sync = 'TRUE') COLLATE NOCASE",
                null);   // Remove the date condition

        try {
            if (idCursor.moveToFirst()) {
                do {
                    NotificationModel model = new NotificationModel();

                    model.setFirst_name(idCursor.getString(idCursor.getColumnIndexOrThrow("first_name")));
                    model.setLast_name(idCursor.getString(idCursor.getColumnIndexOrThrow("last_name")));

                    model.setUuid(UUID.randomUUID().toString());
                    model.setPatientuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("patientuuid")));
                    model.setDescription(model.getFirst_name() + " " + model.getLast_name() + "\'s prescription was received!");
                    model.setObs_server_modified_date(idCursor.getString(idCursor.getColumnIndexOrThrow("obs_server_modified_date")));
                    model.setNotification_type("Prescription");
                    model.setSync("TRUE");

                    model.setVisitUUID(idCursor.getString(idCursor.getColumnIndexOrThrow("visitUUID")));
                    model.setPatient_photo(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_photo")));
                    model.setVisit_startDate(idCursor.getString(idCursor.getColumnIndexOrThrow("startdate")));
                    model.setGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                    model.setOpenmrsID(idCursor.getString(idCursor.getColumnIndexOrThrow("openmrs_id")));
                    model.setDate_of_birth(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                    model.setIsDeleted("false");
                    patientDTOList.add(model);
                }
                while (idCursor.moveToNext());
            }
        } catch (SQLException e) {

        }

        idCursor.close();
        return patientDTOList;
    }


    public static String getStartVisitNoteEncounterByVisitUUID(String visitUUID) {
        String encounterUuid = "";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();

        Cursor idCursor = db.rawQuery("SELECT uuid FROM tbl_encounter where visituuid = ? AND " +
                        "encounter_type_uuid = ? AND (sync = '1' OR sync = 'true' OR sync = 'TRUE') COLLATE NOCASE",
                new String[]{visitUUID, ENCOUNTER_VISIT_NOTE});
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                encounterUuid = idCursor.getString(idCursor.getColumnIndexOrThrow("uuid"));
            }
        }
        idCursor.close();

        db.setTransactionSuccessful();
        db.endTransaction();

        return encounterUuid;
    }

    public void insertStartVisitNoteEncounterToDb(String encounter, String visitUuid) throws DAOException {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        try {
            values.put("uuid", encounter);
            values.put("visituuid", visitUuid);
            values.put("encounter_type_uuid", ENCOUNTER_VISIT_NOTE);
            values.put("sync", "true");

            db.insertWithOnConflict("tbl_encounter", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }
    }

    public boolean isCompletedOrExited(String visitUUID) throws DAOException {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
//        db.beginTransaction();

        try {
            // ENCOUNTER_VISIT_COMPLETE = "bd1fbfaa-f5fb-4ebd-b75c-564506fc309e"
            //ENCOUNTER_PATIENT_EXIT_SURVEY = "629a9d0b-48eb-405e-953d-a5964c88dc30"

            Cursor idCursor = db.rawQuery("SELECT * FROM tbl_encounter where visituuid = ? and " +
                            "encounter_type_uuid in ('629a9d0b-48eb-405e-953d-a5964c88dc30','bd1fbfaa-f5fb-4ebd-b75c-564506fc309e')",
                    new String[]{visitUUID}); // ENCOUNTER_PATIENT_EXIT_SURVEY
            EncounterDTO encounterDTO = new EncounterDTO();
            if (idCursor.getCount() != 0) {
                return true;
            }
            idCursor.close();
//            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            throw new DAOException(e);
        } finally {
//            db.endTransaction();
        }

        return false;
    }

    public boolean isCompletedExitedSurvey(String visitUUID) throws DAOException {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
//        db.beginTransaction();

        try {
            // ENCOUNTER_VISIT_COMPLETE = "bd1fbfaa-f5fb-4ebd-b75c-564506fc309e"
            //ENCOUNTER_PATIENT_EXIT_SURVEY = "629a9d0b-48eb-405e-953d-a5964c88dc30"

            Cursor idCursor = db.rawQuery("SELECT * FROM tbl_encounter where visituuid = ? and " +
                            "encounter_type_uuid in ('629a9d0b-48eb-405e-953d-a5964c88dc30')",  // ENCOUNTER_PATIENT_EXIT_SURVEY
                    new String[]{visitUUID});
            EncounterDTO encounterDTO = new EncounterDTO();
            //CustomLog.v(TAG, "isCompletedExitedSurvey- visitUUID - "+visitUUID+"\t Count - "+idCursor.getCount());
            if (idCursor.getCount() > 0) {
                return true;
            }
            idCursor.close();
//            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            throw new DAOException(e);
        } finally {
//            db.endTransaction();
        }

        return false;
    }

    public boolean isPrescriptionReceived(String visitUUID) throws DAOException {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        //db.beginTransaction();

        try {
            // ENCOUNTER_VISIT_COMPLETE = "bd1fbfaa-f5fb-4ebd-b75c-564506fc309e"
            //ENCOUNTER_PATIENT_EXIT_SURVEY = "629a9d0b-48eb-405e-953d-a5964c88dc30"

            Cursor idCursor = db.rawQuery("SELECT * FROM tbl_encounter where visituuid = ? and " +
                            "encounter_type_uuid = ?",
                    new String[]{visitUUID, UuidDictionary.ENCOUNTER_VISIT_COMPLETE}); // ENCOUNTER_PATIENT_EXIT_SURVEY
            EncounterDTO encounterDTO = new EncounterDTO();
            if (idCursor.getCount() > 0) {
                return true;
            }
            idCursor.close();
            //db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            throw new DAOException(e);
        } finally {
            //db.endTransaction();
        }

        return false;
    }

    /**
     * Chief Complaint for this visituuid
     */
    public static String getChiefComplaint(String visitUUID) {
        String complaintValue = "";

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();

            if(visitUUID != null) {
                String complaint_query = "select e.uuid, o.value  from tbl_encounter e, tbl_obs o where " +
                        "e.visituuid = ? " +
                        "and e.encounter_type_uuid = '8d5b27bc-c2cc-11de-8d13-0010c6dffd0f' " + // adult_initial
                        "and e.uuid = o.encounteruuid and o.conceptuuid = '3edb0e09-9135-481e-b8f0-07a26fa9a5ce'"; // chief complaint

                final Cursor cursor = db.rawQuery(complaint_query, new String[]{visitUUID});
                if (cursor.moveToFirst()) {
                    do {
                        try {
                            complaintValue = cursor.getString(cursor.getColumnIndexOrThrow("value"));
                            CustomLog.v("Followup", "chiefcomplaint: " + complaintValue);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
                db.setTransactionSuccessful();
                db.endTransaction();
            }
        boolean needToShowCoreValue = false;
        if (complaintValue.startsWith("{") && complaintValue.endsWith("}")) {
            try {
                // isInOldFormat = false;
                JSONObject jsonObject = new JSONObject(complaintValue);
                if (jsonObject.has("l-" + new SessionManager(IntelehealthApplication.getAppContext()).getAppLanguage())) {
                    complaintValue = jsonObject.getString("l-" + new SessionManager(IntelehealthApplication.getAppContext()).getAppLanguage());
                    needToShowCoreValue = false;
                } else {
                    needToShowCoreValue = true;
                    complaintValue = jsonObject.getString("en");
                }
                complaintValue = jsonObject.getString("en");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return complaintValue;
    }

    /**
     * This function we are using to get the encoun modified date so that on VD details we can show the value of Precri received time
     * Eg: Presc received 2 hours ago.
     * @param visitUUID
     * @return
     */
    public static String fetchEncounterModifiedDateForPrescGiven(String visitUUID) {
        String modifiedDate = "";

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();

        if(visitUUID != null) {
            final Cursor cursor = db.rawQuery("select modified_date from tbl_encounter where visituuid = ? and " +
                    "(sync = 1 OR sync = 'true' OR sync = 'TRUE') and voided = 0 and " +
                    "encounter_type_uuid = ?", new String[]{visitUUID, ENCOUNTER_VISIT_NOTE});

            if (cursor.moveToFirst()) {
                do {
                    try {
                        modifiedDate = cursor.getString(cursor.getColumnIndexOrThrow("modified_date"));
                        CustomLog.v("modifiedDate", "modifiedDate: " + modifiedDate);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.setTransactionSuccessful();
            db.endTransaction();
        }

        return modifiedDate;
    }

    /**
     * Fetching the uuid from Enc table for visit having ENCOUNTER_VITALS.
     * @param visitUUID
     * @return
     */
    public static String fetchEncounterUuidForEncounterVitals(String visitUUID) {
        String uuid = "";

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();

        if(visitUUID != null) {
            final Cursor cursor = db.rawQuery("select * from tbl_encounter where visituuid = ? and " +
                    "(sync = 1 OR sync = 'true' OR sync = 'TRUE') and voided = 0 and " +
                    "encounter_type_uuid = ?", new String[]{visitUUID, ENCOUNTER_VITALS});

            if (cursor.moveToFirst()) {
                do {
                    try {
                        uuid = cursor.getString(cursor.getColumnIndexOrThrow("uuid"));
                        CustomLog.v("modifiedDate", "uuid: " + uuid);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.setTransactionSuccessful();
            db.endTransaction();
        }

        return uuid;
    }

    /**
     * Fetching the uuid from Enc table for visit having ENCOUNTER_ADULTINITIALS.
     * @param visitUUID
     * @return
     */
    public static String fetchEncounterUuidForEncounterAdultInitials(String visitUUID) {
        String uuid = "";

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();

        if(visitUUID != null) {
            final Cursor cursor = db.rawQuery("select * from tbl_encounter where visituuid = ? and " +
                    "(sync = 1 OR sync = 'true' OR sync = 'TRUE') and voided = 0 and " +
                    "encounter_type_uuid = ?", new String[]{visitUUID, ENCOUNTER_ADULTINITIAL});

            if (cursor.moveToFirst()) {
                do {
                    try {
                        uuid = cursor.getString(cursor.getColumnIndexOrThrow("uuid"));
                        CustomLog.v("modifiedDate", "uuid: " + uuid);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.setTransactionSuccessful();
            db.endTransaction();
        }

        return uuid;
    }


    public boolean isVisitCompletedOrExited(String visitUUID) throws DAOException {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            // ENCOUNTER_VISIT_COMPLETE = "bd1fbfaa-f5fb-4ebd-b75c-564506fc309e"
            //ENCOUNTER_PATIENT_EXIT_SURVEY = "629a9d0b-48eb-405e-953d-a5964c88dc30"

            Cursor idCursor = db.rawQuery("SELECT * FROM tbl_encounter where visituuid = ? and " +
                            "encounter_type_uuid in ('629a9d0b-48eb-405e-953d-a5964c88dc30','bd1fbfaa-f5fb-4ebd-b75c-564506fc309e')",
                    new String[]{visitUUID}); // ENCOUNTER_PATIENT_EXIT_SURVEY
            EncounterDTO encounterDTO = new EncounterDTO();
            if (idCursor.getCount() != 0) {
                return true;
            }
            idCursor.close();
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            throw new DAOException(e);
        } finally {
            db.endTransaction();
        }

        return false;
    }

    public static String getEncounterIdForCompletedVisit(String visitUUID) throws DAOException {
        String encounterUuid = "";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            // ENCOUNTER_VISIT_COMPLETE = "bd1fbfaa-f5fb-4ebd-b75c-564506fc309e"
            //ENCOUNTER_PATIENT_EXIT_SURVEY = "629a9d0b-48eb-405e-953d-a5964c88dc30"

            Cursor idCursor = db.rawQuery("SELECT uuid FROM tbl_encounter where visituuid = ? and " +
                            "encounter_type_uuid in ('629a9d0b-48eb-405e-953d-a5964c88dc30','bd1fbfaa-f5fb-4ebd-b75c-564506fc309e')",
                    new String[]{visitUUID}); // ENCOUNTER_PATIENT_EXIT_SURVEY
            EncounterDTO encounterDTO = new EncounterDTO();
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    encounterUuid = idCursor.getString(idCursor.getColumnIndexOrThrow("uuid"));
                }
            }
            idCursor.close();
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
            throw new DAOException(e);
        } finally {
            db.endTransaction();
        }

        return encounterUuid;
    }

    public static String getPrescriptionReceivedTime(String visitUUID) {
        String modifiedTime = "";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_obs where encounteruuid = ? AND (sync = '1' OR sync = 'true' OR sync = 'TRUE') COLLATE NOCASE",
                new String[]{visitUUID});
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                modifiedTime = idCursor.getString(idCursor.getColumnIndexOrThrow("obsservermodifieddate"));
                CustomLog.d(TAG, "getPrescriptionReceivedTime:modifiedTime :  " + modifiedTime);

            }
        }
        idCursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
//        db.close();

        return modifiedTime;
    }

    public EncounterDTO getEncounterByVisitUUIDLimit1(String visitUUID) {

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        // db.beginTransaction();
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_encounter where visituuid = ? and voided = '0' AND encounter_type_uuid != ? ORDER BY encounter_time DESC limit 1",
                new String[]{visitUUID, ENCOUNTER_VISIT_COMPLETE});

        EncounterDTO encounterDTO = new EncounterDTO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                encounterDTO = new EncounterDTO();
                encounterDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                encounterDTO.setVisituuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visituuid")));
                encounterDTO.setEncounterTypeUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("encounter_type_uuid")));
                encounterDTO.setProvideruuid(idCursor.getString(idCursor.getColumnIndexOrThrow("provider_uuid")));
                encounterDTO.setEncounterTime(idCursor.getString(idCursor.getColumnIndexOrThrow("encounter_time")));
                encounterDTO.setVoided(idCursor.getInt(idCursor.getColumnIndexOrThrow("voided")));
                encounterDTO.setPrivacynotice_value(idCursor.getString(idCursor.getColumnIndexOrThrow("privacynotice_value")));

            }
        }

        idCursor.close();
        //   db.setTransactionSuccessful();
        //   db.endTransaction();
        //  db.close();

        return encounterDTO;
    }


    public static ArrayList<FollowUpNotificationData> getFollowUpDateListFromConceptId() throws DAOException {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        String followUpDateConcept = "596c7f50-ec12-4ad8-b92a-7491ad80341b";

        ArrayList<FollowUpNotificationData> list = new ArrayList<>();

        try {

            String vitalsQ = "(obs.conceptuuid =  "+"'" + UuidDictionary.HEIGHT  + "' or " +
                    "obs.conceptuuid =  "+"'" + UuidDictionary.WEIGHT  + "' or" +
                    " obs.conceptuuid =  "+"'" + UuidDictionary.DIASTOLIC_BP  + "' or" +
                    " obs.conceptuuid =  "+"'" + UuidDictionary.SYSTOLIC_BP  + "' or" +
                    " obs.conceptuuid =  "+"'" + UuidDictionary.TEMPERATURE  + "' or" +
                    " obs.conceptuuid =  "+"'" + UuidDictionary.SPO2  + "' or" +
                    " obs.conceptuuid =  "+"'" + UuidDictionary.BLOOD_GROUP  + "' or" +
                    " obs.conceptuuid =  "+"'" + UuidDictionary.RESPIRATORY   +"') ";

            String query = """
                    select p.uuid,p.openmrs_id,p.first_name || " " || p.last_name as name,p.gender,e.encounter_type_uuid,v.uuid as visitUuid,
                    obs.conceptuuid,obs.encounteruuid,obs.value from tbl_obs as obs,
                      tbl_encounter as e on obs.encounteruuid = e.uuid,\s
                      tbl_visit as v on e.visituuid = v.uuid,
                      tbl_patient as p on v.patientuuid = p.uuid
                     where v.enddate IS NULL and obs.conceptuuid = \s""" + "'" + followUpDateConcept + "'";


            Cursor idCursor = db.rawQuery(query, new String[]{});

            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    String vitalEncounterUuid = "";
                    String visitUid = idCursor.getString(idCursor.getColumnIndexOrThrow("visitUuid"));
                    String vitalEncounterQuery = " " +
                            "select obs.encounteruuid from tbl_obs as obs, " +
                            "tbl_encounter as e on obs.encounteruuid = e.uuid,  " +
                            "tbl_visit as v on e.visituuid = v.uuid, " +
                            "tbl_patient as p on v.patientuuid = p.uuid " +
                            "where v.enddate IS NULL and  " +
                            " v.uuid = "+"'"+visitUid+"' and "+vitalsQ;

                    Cursor cursor = db.rawQuery(vitalEncounterQuery, new String[]{});
                    if (cursor.getCount() != 0) {
                        while (cursor.moveToNext()) {
                            vitalEncounterUuid = cursor.getString(cursor.getColumnIndexOrThrow("encounteruuid"));
                        }
                    }

                    list.add(new FollowUpNotificationData(
                            idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")),
                            idCursor.getString(idCursor.getColumnIndexOrThrow("openmrs_id")),
                            idCursor.getString(idCursor.getColumnIndexOrThrow("name")),
                            idCursor.getString(idCursor.getColumnIndexOrThrow("gender")),
                            idCursor.getString(idCursor.getColumnIndexOrThrow("encounter_type_uuid")),
                            visitUid,
                            idCursor.getString(idCursor.getColumnIndexOrThrow("conceptuuid")),
                            idCursor.getString(idCursor.getColumnIndexOrThrow("encounteruuid")) == null ? "" : idCursor.getString(idCursor.getColumnIndexOrThrow("encounteruuid")),
                            vitalEncounterUuid,
                            idCursor.getString(idCursor.getColumnIndexOrThrow("value")) == null ? "" : idCursor.getString(idCursor.getColumnIndexOrThrow("value"))
                    ));

                    cursor.close();
                }
            }
            idCursor.close();
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
            throw new DAOException(e);
        } finally {
            db.endTransaction();
        }

        return list;
    }
    public static String fetchEncounterUuidForEncounterDiagnostics(String visitUUID) {
        String uuid = "";

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();

        if(visitUUID != null) {
            final Cursor cursor = db.rawQuery("select * from tbl_encounter where visituuid = ? and " +
                    "(sync = 1 OR sync = 'true' OR sync = 'TRUE') and voided = 0 and " +
                    "encounter_type_uuid = ?", new String[]{visitUUID, ENCOUNTER_VITALS});

            if (cursor.moveToFirst()) {
                do {
                    try {
                        uuid = cursor.getString(cursor.getColumnIndexOrThrow("uuid"));
                        CustomLog.v("modifiedDate", "uuid: " + uuid);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.setTransactionSuccessful();
            db.endTransaction();
        }

        return uuid;
    }

}