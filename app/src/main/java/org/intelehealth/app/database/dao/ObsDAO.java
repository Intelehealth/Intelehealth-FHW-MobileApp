package org.intelehealth.app.database.dao;

import static org.intelehealth.app.utilities.UuidDictionary.ENCOUNTER_VISIT_COMPLETE;
import static org.intelehealth.app.utilities.UuidDictionary.ENCOUNTER_VITALS;
import static org.intelehealth.app.utilities.UuidDictionary.HW_FOLLOWUP_CONCEPT_ID;
import static org.intelehealth.app.utilities.UuidDictionary.FOLLOW_UP_VISIT;
import static org.intelehealth.app.utilities.UuidDictionary.OBS_TYPE_DIAGNOSTICS_SET;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import org.intelehealth.app.utilities.CustomLog;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.activities.prescription.PrescDataModel;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.models.dto.ObsDTO;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ObsDAO {


    private SQLiteDatabase db = null;
    SessionManager sessionManager = null;
    String TAG = ObsDAO.class.getSimpleName();

    public boolean insertObsTemp(List<ObsDTO> obsDTOS) throws DAOException {
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        boolean isInserted = true;
        db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        try {
            db.beginTransaction();
            Logger.logD("insert", " insert obs");
            for (ObsDTO obs : obsDTOS) {
                if (sessionManager.isFirstTimeSyncExcuted() && obs.getVoided() == 1) continue;
                createObs(obs);
            }
            db.setTransactionSuccessful();
            Logger.logD("insert obs finished", " insert obs finished");
        } catch (SQLException e) {
            isInserted = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();

        }

        return isInserted;

    }

    private boolean createObs(ObsDTO obsDTOS) throws DAOException {
        boolean isCreated = true;
        long createdRecordsCount = 0;
        ContentValues values = new ContentValues();
        try {
            values.put("uuid", obsDTOS.getUuid());
            values.put("encounteruuid", obsDTOS.getEncounteruuid());
            values.put("creator", obsDTOS.getCreator());
            values.put("conceptuuid", obsDTOS.getConceptuuid());
            values.put("value", obsDTOS.getValue());
            values.put("obsservermodifieddate", obsDTOS.getObsServerModifiedDate());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("voided", obsDTOS.getVoided());
            values.put("sync", "TRUE");
            values.put("conceptsetuuid", obsDTOS.getConceptsetuuid());
            createdRecordsCount = db.insertWithOnConflict("tbl_obs", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
        }

        return isCreated;

    }

    public boolean insertObs(ObsDTO obsDTO) throws DAOException {
        Log.d(TAG, "insertObskkk: obsdto : " + new Gson().toJson(obsDTO));
        boolean isUpdated = true;
        long insertedCount = 0;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();

        try {
            values.put("uuid", UUID.randomUUID().toString());
            values.put("encounteruuid", obsDTO.getEncounteruuid());
            values.put("creator", obsDTO.getCreator());
            values.put("conceptuuid", obsDTO.getConceptuuid());
            values.put("value", obsDTO.getValue());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("voided", "0");
            values.put("sync", "false");
            values.put("conceptsetuuid", obsDTO.getConceptsetuuid());
            insertedCount = db.insertWithOnConflict("tbl_obs", null, values, SQLiteDatabase.CONFLICT_REPLACE);

            db.setTransactionSuccessful();
            Logger.logD("updated", "updatedrecords count" + insertedCount);
        } catch (SQLException e) {
            isUpdated = false;
            throw new DAOException(e);
        } finally {
            db.endTransaction();

        }

        return isUpdated;

    }


    public boolean updateObs(ObsDTO obsDTO) {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        int updatedCount = 0;
        ContentValues values = new ContentValues();
        String selection = "uuid = ?";
        try {

            values.put("encounteruuid", obsDTO.getEncounteruuid());
            values.put("creator", obsDTO.getCreator());
            values.put("conceptuuid", obsDTO.getConceptuuid());
            values.put("value", obsDTO.getValue());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("voided", "0");
            values.put("sync", "false");
            values.put("conceptsetuuid", obsDTO.getConceptsetuuid());
            updatedCount = db.update("tbl_obs", values, selection, new String[]{obsDTO.getUuid()});

            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Logger.logE(TAG, "exception ", e);

        } finally {
            db.endTransaction();

        }
//        If no value is not found, then update fails so insert instead.
        if (updatedCount == 0) {
            try {
                insertObs(obsDTO);
            } catch (DAOException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }


        return true;
    }

    public boolean insertObsToDb(List<ObsDTO> obsDTO) throws DAOException {
        boolean isUpdated = true;
        long insertedCount = 0;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();

        try {
            for (ObsDTO ob : obsDTO) {
                values.put("uuid", UUID.randomUUID().toString());
                values.put("encounteruuid", ob.getEncounteruuid());
                values.put("creator", ob.getCreator());
                values.put("conceptuuid", ob.getConceptuuid());
                values.put("value", ob.getValue());
                values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
                values.put("voided", "0");
                values.put("sync", "false");    //Earlier was set to FALSE which caused the issue.
                insertedCount = db.insert("tbl_obs", null, values);
            }
            db.setTransactionSuccessful();
            Logger.logD("updated", "updatedrecords count" + insertedCount);
        } catch (SQLException e) {
            isUpdated = false;
            FirebaseCrashlytics.getInstance().recordException(e);
            throw new DAOException(e);
        } finally {
            db.endTransaction();

        }

        return isUpdated;

    }

    public List<ObsDTO> obsDTOList(String encounteruuid) {
        List<ObsDTO> obsDTOList = new ArrayList<>();
        db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        //take All obs except image obs
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_obs where encounteruuid = ? AND (conceptuuid != ? AND conceptuuid != ?) AND voided='0' AND sync='false'", new String[]{encounteruuid, UuidDictionary.COMPLEX_IMAGE_AD, UuidDictionary.COMPLEX_IMAGE_PE});
        ObsDTO obsDTO = new ObsDTO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                obsDTO = new ObsDTO();
                obsDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                obsDTO.setEncounteruuid(idCursor.getString(idCursor.getColumnIndexOrThrow("encounteruuid")));
                obsDTO.setConceptuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("conceptuuid")));
                obsDTO.setValue(idCursor.getString(idCursor.getColumnIndexOrThrow("value")));
                if (idCursor.getColumnIndex("comments") < 0) {
                    obsDTO.setComments(idCursor.getString(idCursor.getColumnIndexOrThrow("comments")));
                } else {
                    obsDTO.setComments("");
                }
                obsDTOList.add(obsDTO);
            }
        }
        idCursor.close();

        return obsDTOList;
    }

    public List<String> getImageStrings(String conceptuuid, String encounterUuidAdultIntials) {
        List<String> rawStrings = new ArrayList<>();
        db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        Cursor idCursor = db.rawQuery("SELECT uuid FROM tbl_obs where conceptuuid = ? AND encounteruuid = ? AND voided='0'", new String[]{conceptuuid, encounterUuidAdultIntials});
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                rawStrings.add(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
            }
        }
        idCursor.close();


        return rawStrings;
    }

    public String getObsuuid(String encounterUuid, String conceptUuid) throws DAOException {
        String obsuuid = null;
        db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        Cursor obsCursoursor = db.rawQuery("Select uuid from tbl_obs where conceptuuid=? and encounteruuid=? and voided='0' order by created_date,obsservermodifieddate desc limit 1 ", new String[]{conceptUuid, encounterUuid});
        try {
            if (obsCursoursor.getCount() != 0) {
                while (obsCursoursor.moveToNext()) {
                    obsuuid = obsCursoursor.getString(obsCursoursor.getColumnIndexOrThrow("uuid"));
                }

            }
        } catch (SQLException sql) {
            FirebaseCrashlytics.getInstance().recordException(sql);
            throw new DAOException(sql);
        } finally {
            obsCursoursor.close();
        }


        return obsuuid;
    }

    public List<PrescDataModel> fetchAllObsPrescData(String encounterVisitNote, String CONCEPTUUID, String sync) {
        List<PrescDataModel> prescDataModelList = new ArrayList<>();

        db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        Cursor obsCursoursor = db.rawQuery("Select * from tbl_obs where conceptuuid=? and encounteruuid=? and sync=? and voided =?", new String[]{CONCEPTUUID, encounterVisitNote, sync, "0"});
        try {
            if (obsCursoursor.getCount() != 0) {
                while (obsCursoursor.moveToNext()) {
                    prescDataModelList.add(new PrescDataModel(obsCursoursor.getString(obsCursoursor.getColumnIndexOrThrow("uuid")), obsCursoursor.getString(obsCursoursor.getColumnIndexOrThrow("value")), obsCursoursor.getString(obsCursoursor.getColumnIndexOrThrow("encounteruuid")), obsCursoursor.getString(obsCursoursor.getColumnIndexOrThrow("conceptuuid"))));
                }
            }
        } catch (SQLException sql) {
            FirebaseCrashlytics.getInstance().recordException(sql);
        } finally {
            obsCursoursor.close();
        }

        return prescDataModelList;
    }

    /**
     * @param obsDTO This obsDTO contains the Prescription obs values that we dont want to push to the server but need to save in localdb
     *               so as when user goes back to the Presc activity then we an see all the data that he had provided as presc. Due to this
     *               I have set sync flag = true. Then when user comes back to Presc screen in the oncreate() we will call the fetch query of db
     *               and fetch all the obs against their conceptuuid and show in the RecyclerView...
     * @return boolean Is insertion was successful or not.
     * @throws DAOException
     */
    public boolean insertPrescObs(ObsDTO obsDTO) throws DAOException {
        boolean isUpdated = true;
        long insertedCount = 0;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();

        try {
            values.put("uuid", UUID.randomUUID().toString());
            values.put("encounteruuid", obsDTO.getEncounteruuid());
            values.put("creator", obsDTO.getCreator());
            values.put("conceptuuid", obsDTO.getConceptuuid());
            values.put("value", obsDTO.getValue());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("voided", "0");
            values.put("sync", "true");
            insertedCount = db.insertWithOnConflict("tbl_obs", null, values, SQLiteDatabase.CONFLICT_REPLACE);

            db.setTransactionSuccessful();
            Logger.logD("updated", "updatedrecords count" + insertedCount);
        } catch (SQLException e) {
            isUpdated = false;
            throw new DAOException(e);
        } finally {
            db.endTransaction();
        }

        return isUpdated;

    }

    /**
     * This fetches the value of Follow up shared for this Visit UUID.
     *
     * @param visitUUID
     * @return Followup date Eg. 30-11-2022
     */
    public static String getFollowupDataForVisitUUID(String visitUUID) {
        String result = null;

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        //db.beginTransaction();

        if (visitUUID != null) {
            final Cursor cursor = db.rawQuery("select o.value, SUBSTR(o.value,1,10) AS value_text from " + "tbl_visit v, tbl_encounter e, tbl_obs o where v.uuid = e.visituuid and e.uuid = o.encounteruuid and " + "(o.sync=1 or o.sync='TRUE' or o.sync='true') and o.voided = 0 and " + "v.uuid = ? and o.conceptuuid = ?", new String[]{visitUUID, FOLLOW_UP_VISIT});  // e8caffd6-5d22-41c4-8d6a-bc31a44d0c86

            if (cursor.moveToFirst()) {
                do {
                    try {
                        result = cursor.getString(cursor.getColumnIndexOrThrow("value_text"));
                        CustomLog.v("value_text", "value_text: " + result);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
//            db.setTransactionSuccessful();
//            db.endTransaction();
        }

        return result;
    }

    public static String fetchDrDetailsFromLocalDb(String visitUuid) {
        // fetch dr details from local db - start
        String dbValue = null;

        String visitnote = "";
        EncounterDAO encounterDAO = new EncounterDAO();

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
//        db.beginTransaction();

        String encounterIDSelection = "visituuid = ? ";
        String[] encounterIDArgs = {visitUuid};
        String encounter_type_uuid_comp = ENCOUNTER_VISIT_COMPLETE; // bd1fbfaa-f5fb-4ebd-b75c-564506fc309e // make the encounter_type_uuid as constant later on.
        Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null);

        if (encounterCursor != null && encounterCursor.moveToFirst()) {
            do {
                if (encounter_type_uuid_comp.equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                    visitnote = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                }
            } while (encounterCursor.moveToNext());

        }
        encounterCursor.close();

        String[] columns = {"value", " conceptuuid"};
        String visitSelection = "encounteruuid = ? and voided!='1' and conceptuuid!='" + HW_FOLLOWUP_CONCEPT_ID + "'";
        String[] visitArgs = {visitnote};
        Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, null);
        if (visitCursor.moveToFirst()) {
            do {
                String dbConceptID = visitCursor.getString(visitCursor.getColumnIndex("conceptuuid"));
                dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
            } while (visitCursor.moveToNext());
        }
        visitCursor.close();

//        db.setTransactionSuccessful();
//        db.endTransaction();

        return dbValue;
        // fetch dr details from local db - end
    }

    public static String fetchValueFromLocalDb(String visitUuid) {
        // fetch dr details from local db - start
        String dbValue = null;

        String visitnote = "";
        EncounterDAO encounterDAO = new EncounterDAO();

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
//        db.beginTransaction();

        String encounterIDSelection = "visituuid = ? ";
        String[] encounterIDArgs = {visitUuid};
        String encounter_type_uuid_comp = ENCOUNTER_VISIT_COMPLETE; // bd1fbfaa-f5fb-4ebd-b75c-564506fc309e // make the encounter_type_uuid as constant later on.
        Cursor encounterCursor = db.query("tbl_encounter", null, encounterIDSelection, encounterIDArgs, null, null, null);

        if (encounterCursor != null && encounterCursor.moveToFirst()) {
            do {
                if (encounter_type_uuid_comp.equalsIgnoreCase(encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("encounter_type_uuid")))) {
                    visitnote = encounterCursor.getString(encounterCursor.getColumnIndexOrThrow("uuid"));
                }
            } while (encounterCursor.moveToNext());

        }
        encounterCursor.close();

        String[] columns = {"value", " conceptuuid"};
        String visitSelection = "encounteruuid = ? and voided!='1'";
        String[] visitArgs = {visitnote};
        Cursor visitCursor = db.query("tbl_obs", columns, visitSelection, visitArgs, null, null, null);
        if (visitCursor.moveToFirst()) {
            do {
                dbValue = visitCursor.getString(visitCursor.getColumnIndex("value"));
            } while (visitCursor.moveToNext());
        }
        visitCursor.close();

//        db.setTransactionSuccessful();
//        db.endTransaction();

        return dbValue;
        // fetch dr details from local db - end
    }

    public static void deleteExistingVitalsDataIfExists(String visitUuid) {
        boolean doesVitalsEncounterExist = false;
        String encounterUuid = "";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();

        // Check if the vitals encounter exists
        // If it does,fetch the vitals encounter
        Cursor cursor = db.rawQuery("SELECT * FROM tbl_encounter WHERE visituuid = ? AND encounter_type_uuid = ?", new String[]{visitUuid, ENCOUNTER_VITALS});
        if (cursor.moveToFirst()) {
            doesVitalsEncounterExist = true;
            encounterUuid = cursor.getString(cursor.getColumnIndexOrThrow("uuid"));
        }
        cursor.close();

        // In case the vitals encounter exists
        // delete all the entries which have encounteruuid
        if (doesVitalsEncounterExist) {
            String deleteClause = "encounteruuid = ? AND conceptsetuuid = ?";
            db.delete("tbl_obs", deleteClause, new String[]{encounterUuid, UuidDictionary.OBS_TYPE_VITAL_SET});
        }
    }

    public static void deleteExistingDiagnosticsDataIfExists(String visitUuid) {
        boolean doesVitalsEncounterExist = false;
        String encounterUuid = "";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();

        // Check if the vitals encounter exists
        // If it does,fetch the vitals encounter
        Cursor cursor = db.rawQuery("SELECT * FROM tbl_encounter WHERE visituuid = ? AND encounter_type_uuid = ?", new String[]{visitUuid, ENCOUNTER_VITALS});
        if (cursor.moveToFirst()) {
            doesVitalsEncounterExist = true;
            encounterUuid = cursor.getString(cursor.getColumnIndexOrThrow("uuid"));
        }
        cursor.close();

        // In case the vitals encounter exists
        // delete all the entries which have encounteruuid
        if (doesVitalsEncounterExist) {
            String deleteClause = "encounteruuid = ? AND conceptsetuuid = ?";
            db.delete("tbl_obs", deleteClause, new String[]{encounterUuid, OBS_TYPE_DIAGNOSTICS_SET});
        }
    }
}