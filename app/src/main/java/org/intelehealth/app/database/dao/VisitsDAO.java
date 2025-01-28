package org.intelehealth.app.database.dao;

import static org.intelehealth.app.utilities.UuidDictionary.ENCOUNTER_ADULTINITIAL;
import static org.intelehealth.app.utilities.UuidDictionary.ENCOUNTER_VISIT_COMPLETE;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.models.PrescriptionModel;
import org.intelehealth.app.models.dto.VisitAttributeDTO;
import org.intelehealth.app.models.dto.VisitAttribute_Speciality;
import org.intelehealth.app.models.dto.VisitDTO;
import org.intelehealth.app.utilities.CustomLog;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.List;

public class VisitsDAO {


    private long createdRecordsCount = 0;

    private static final String TAG = "VisitsDAO";

    public boolean insertVisit(List<VisitDTO> visitDTOS) throws DAOException {
        boolean isInserted = true;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {

            for (VisitDTO visit : visitDTOS) {
                createVisits(visit, db);
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            isInserted = false;
            CustomLog.e(TAG,e.getMessage());
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();

        }

        return isInserted;
    }

    private boolean createVisits(VisitDTO visit, SQLiteDatabase db) throws DAOException {
        boolean isCreated = true;
        ContentValues values = new ContentValues();
        try {
            values.put("uuid", visit.getUuid());
            values.put("patientuuid", visit.getPatientuuid());
            values.put("locationuuid", visit.getLocationuuid());
            values.put("visit_type_uuid", visit.getVisitTypeUuid());
            values.put("creator", visit.getCreatoruuid());
            values.put("startdate", DateAndTimeUtils.formatDateFromOnetoAnother(visit.getStartdate(), "MMM dd, yyyy hh:mm:ss a", "yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
            values.put("enddate", visit.getEnddate());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("sync", visit.getSyncd());
            createdRecordsCount = db.insertWithOnConflict("tbl_visit", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLException e) {
            isCreated = false;
            CustomLog.e(TAG,e.getMessage());
            throw new DAOException(e.getMessage(), e);
        } finally {
        }
        return isCreated;
    }


    public boolean insertPatientToDB(VisitDTO visit) throws DAOException {
        boolean isCreated = true;
        long createdRecordsCount1 = 0;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        db.beginTransaction();
        List<VisitAttributeDTO> visitAttributeDTOS = new ArrayList<>();
        try {

            values.put("uuid", visit.getUuid());
            values.put("patientuuid", visit.getPatientuuid());
            values.put("locationuuid", visit.getLocationuuid());
            values.put("visit_type_uuid", visit.getVisitTypeUuid());
            values.put("creator", visit.getCreatoruuid());
            values.put("startdate", visit.getStartdate());
            values.put("enddate", visit.getEnddate());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("sync", false);

            visitAttributeDTOS = visit.getVisitAttributeDTOS();
            if (visitAttributeDTOS != null) {
                insertVisitAttribToDB(visitAttributeDTOS, db);
            }

            createdRecordsCount1 = db.insert("tbl_visit", null, values);
            db.setTransactionSuccessful();
            Logger.logD("created records", "created records count" + createdRecordsCount1);
        } catch (SQLException e) {
            isCreated = false;
            CustomLog.e(TAG,e.getMessage());
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();

        }
        return isCreated;

    }

    public boolean insertVisitAttribToDB(List<VisitAttributeDTO> visitAttributeDTOS, SQLiteDatabase db) throws DAOException {
        boolean isCreated = true;
        ContentValues values = new ContentValues();
        db.beginTransaction();
        try {
            for (VisitAttributeDTO visit : visitAttributeDTOS) {
                values.put("uuid", visit.getUuid());
                values.put("value", visit.getValue());
                values.put("visit_attribute_type_uuid", visit.getVisit_attribute_type_uuid());
                values.put("visituuid", visit.getVisit_uuid());
                values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
                values.put("sync", "true");
                createdRecordsCount = db.insertWithOnConflict("tbl_visit_attribute", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
            Logger.logD("created records", "created records count" + createdRecordsCount);
        } catch (SQLException e) {
            isCreated = false;
            CustomLog.e(TAG,e.getMessage());
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }
        return isCreated;
    }


    //update condition for speciality
/*
    public boolean update_visitTbl_speciality(String spinner_value, String visitUUID) throws DAOException {
        boolean isupdatedone = false;
//        String cursor_uuid = "", cursor_value="";
        CustomLog.d("SPINNER", "SPINNER_Selected_valuelogs: "+ spinner_value);
        CustomLog.d("SPINNER", "SPINNER_Selected_uuidlogs: "+ visitUUID);

       */
/* SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        Cursor idCursor = db.rawQuery("SELECT value FROM tbl_dr_speciality WHERE value = ?",
                new String[]{spinner_value});

        if(idCursor.getCount() != 0)
        {
            while(idCursor.moveToNext())
            {
                 cursor_uuid = idCursor.getString(idCursor.getColumnIndexOrThrow("uuid"));
            }
        }
        idCursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();*//*



        SQLiteDatabase db_update = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db_update.beginTransaction();
        ContentValues values = new ContentValues();
        String whereclause = "uuid=?";
        String[] selectionArgs = {visitUUID};
        try
        {
//            values.put("speciality_uuid", cursor_uuid);
            values.put("speciality_value", spinner_value);
            values.put("sync", "0");

            Logger.logD("visit", "updated_specilaity_values " +
                    values.get("speciality_value"));

            int i = db_update.update("tbl_visit", values, whereclause, selectionArgs);

            Logger.logD("visit", "updated_specilaity" + i);
            db_update.setTransactionSuccessful();
            if(i != -1)
                isupdatedone = true;

        }
        catch (SQLException e)
        {
            isupdatedone = false;
            Logger.logD("visit", "updated" + e.getMessage());
            throw new DAOException(e.getMessage());

        }
     finally {
            db_update.endTransaction();
//            db_update.close(); Closing the db was causing the crash on visit onCreate() in update.
            //while updating, do not close the db instance,.

    }

        //Sqlite Db Browser bug isnt showing the updated records...
        //To re-check and confirm that the records are updated & stored in the local db, below is
        //the code....
      */
/*  SQLiteDatabase db_aa = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db_aa.beginTransaction();
        Cursor idCursor_aa = db_aa.rawQuery("SELECT speciality_uuid, speciality_value FROM tbl_visit WHERE uuid = ?", new String[]{visitUUID});

        if(idCursor_aa.getCount() != 0)
        {
            while(idCursor_aa.moveToNext())
            {
                String aa_uuid = idCursor_aa.getString(idCursor_aa.getColumnIndexOrThrow("speciality_uuid"));
                String aa_value = idCursor_aa.getString(idCursor_aa.getColumnIndexOrThrow("speciality_value"));
                CustomLog.d("PRAJ", "PRAJ: "+ aa_uuid + " :: " + aa_value);
            }
        }
        idCursor_aa.close();
        db_aa.setTransactionSuccessful();
        db_aa.endTransaction();*//*


        return  isupdatedone;
}
*/


    //update - end....

    public List<VisitDTO> unsyncedVisits() {
        List<VisitDTO> visitDTOList = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_visit where (sync = ? OR sync=?) COLLATE NOCASE", new String[]{"0", "false"});
        VisitDTO visitDTO = new VisitDTO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                visitDTO = new VisitDTO();
                visitDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                visitDTO.setPatientuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("patientuuid")));
                visitDTO.setLocationuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("locationuuid")));
                visitDTO.setStartdate(idCursor.getString(idCursor.getColumnIndexOrThrow("startdate")));
                visitDTO.setEnddate(idCursor.getString(idCursor.getColumnIndexOrThrow("enddate")));
                visitDTO.setCreatoruuid(idCursor.getString(idCursor.getColumnIndexOrThrow("creator")));
                visitDTO.setVisitTypeUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_type_uuid")));

                List<VisitAttribute_Speciality> list = new ArrayList<>();
                list = fetchVisitAttrs(visitDTO.getUuid());
                visitDTO.setAttributes(list);
//                visitDTOList.add(visitDTO);

                //adding visit attribute list in the visit data.
//               List<VisitAttribute_Speciality> list = new ArrayList<>();
//               VisitAttribute_Speciality speciality = new VisitAttribute_Speciality();
//               speciality.setAttributeType("3f296939-c6d3-4d2e-b8ca-d7f4bfd42c2d");
//               speciality.setValue(idCursor.getString(idCursor.getColumnIndexOrThrow("speciality_value")));
//               list.add(speciality);
//
//
//                visitDTO.setAttributes(list);
                //need a return value as list so that I can then add it to visitDTO.setAttributes(list);
//               list =  fetchVisitAttr_Speciality();
//               visitDTO.setAttributes(list);

                visitDTOList.add(visitDTO);
            }
        }
        idCursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();

//        List<VisitAttribute_Speciality> list = new ArrayList<>();
//        list = fetchVisitAttr_Speciality();
//        visitDTO.setAttributes(list);
//        visitDTOList.add(visitDTO);

        return visitDTOList;
    }

    private List<VisitAttribute_Speciality> fetchVisitAttrs(String visit_uuid) {
        List<VisitAttribute_Speciality> list = new ArrayList<>();
        // VisitAttribute_Speciality speciality = new VisitAttribute_Speciality();

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        //db.beginTransaction();

//        Cursor cursor = db.rawQuery("SELECT * FROM tbl_visit_attribute WHERE visit_uuid=? LIMIT 1",
//                new String[]{/*"0", */visit_uuid});

        Cursor cursor = db.rawQuery("SELECT * FROM tbl_visit_attribute WHERE visit_uuid = ?",
                new String[]{/*"0", */visit_uuid});
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                VisitAttribute_Speciality attribute = new VisitAttribute_Speciality();
                attribute.setUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
                attribute.setAttributeType(cursor.getString(cursor.getColumnIndexOrThrow("visit_attribute_type_uuid")));
                attribute.setValue(cursor.getString(cursor.getColumnIndexOrThrow("value")));
                list.add(attribute);
            }
        }
        cursor.close();
        //db.setTransactionSuccessful();
        //db.endTransaction();

        return list;
    }

    public List<VisitDTO> getAllVisits() {
        List<VisitDTO> visitDTOList = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        //db.beginTransaction();
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_visit", null);
        VisitDTO visitDTO = new VisitDTO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                visitDTO = new VisitDTO();
                visitDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                visitDTO.setPatientuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("patientuuid")));
                visitDTO.setLocationuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("locationuuid")));
                visitDTO.setStartdate(idCursor.getString(idCursor.getColumnIndexOrThrow("startdate")));
                visitDTO.setEnddate(idCursor.getString(idCursor.getColumnIndexOrThrow("enddate")));
                visitDTO.setCreatoruuid(idCursor.getString(idCursor.getColumnIndexOrThrow("creator")));
                visitDTO.setVisitTypeUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_type_uuid")));
                visitDTOList.add(visitDTO);
            }
        }
        idCursor.close();
        //db.setTransactionSuccessful();
        //db.endTransaction();
//        db.close();
        return visitDTOList;
    }

    public boolean updateVisitSync(String uuid, String synced) throws DAOException {
        boolean isUpdated = true;
        Logger.logD("visitdao", "updatesynv visit " + uuid + synced);
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        String whereclause = "uuid=?";
        String[] whereargs = {uuid};
        try {
            values.put("sync", synced);
            values.put("uuid", uuid);
            int i = db.update("tbl_visit", values, whereclause, whereargs);
            Logger.logD("visit", "updated" + i);
            db.setTransactionSuccessful();
        } catch (SQLException sql) {
            CustomLog.e("visit", "updated" + sql.getMessage());
            throw new DAOException(sql.getMessage());
        } finally {
            db.endTransaction();


        }

        return isUpdated;
    }


    public boolean updateVisitEnddate(String uuid, String enddate) throws DAOException {
        boolean isUpdated = true;
        Logger.logD("visitdao", "updatesynv visit " + uuid + enddate);
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        String whereclause = "uuid=?";
        String[] whereargs = {uuid};
        try {
            values.put("enddate", enddate);
            values.put("sync", "0");
            int i = db.update("tbl_visit", values, whereclause, whereargs);
            Logger.logD("visit", "updated" + i);
            db.setTransactionSuccessful();
        } catch (SQLException sql) {
            CustomLog.e("visit", "updated" + sql.getMessage());
            throw new DAOException(sql.getMessage());
        } finally {
            db.endTransaction();


        }

        return isUpdated;
    }

    public String patientUuidByViistUuid(String visituuid) {
        String patientUuidByViistUuid = "";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        //db.beginTransaction();
        Cursor cursor = db.rawQuery("SELECT patientuuid FROM tbl_visit where uuid = ? ", new String[]{visituuid});
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                patientUuidByViistUuid = cursor.getString(cursor.getColumnIndexOrThrow("patientuuid"));
            }
        }
        cursor.close();
        //db.setTransactionSuccessful();
        //db.endTransaction();


        return patientUuidByViistUuid;
    }

    public boolean isUpdatedDownloadColumn(String visitUuid, boolean isupdated) throws DAOException {
        boolean isUpdated = false;
        int updatedcount = 0;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        String whereclause = "uuid=?";
        String[] whereargs = {visitUuid};
        try {
            values.put("isdownloaded", isupdated);
            updatedcount = db.update("tbl_visit", values, whereclause, whereargs);
            if (updatedcount != 0)
                isUpdated = true;
            Logger.logD("visit", "updated isdownloaded" + updatedcount);
            db.setTransactionSuccessful();
        } catch (SQLException sql) {
            isUpdated = false;
            FirebaseCrashlytics.getInstance().recordException(sql);
            CustomLog.e("visit", "updated isdownloaded" + sql.getMessage());
            throw new DAOException(sql.getMessage());
        } finally {
            db.endTransaction();
        }
        return isUpdated;
    }

    public String getDownloadedValue(String visituuid) throws DAOException {
        String isDownloaded = null;

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        //db.beginTransaction();

        try {
            Cursor cursor = db.rawQuery("SELECT isdownloaded FROM tbl_visit where uuid = ? ", new String[]{visituuid});
            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    isDownloaded = cursor.getString(cursor.getColumnIndexOrThrow("isdownloaded"));
                }
            }
            cursor.close();

        } catch (SQLiteException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            CustomLog.e(TAG,e.getMessage());
            throw new DAOException(e);
        } finally {
            //db.setTransactionSuccessful();
            //db.endTransaction();
        }
        return isDownloaded;
    }

    /**
     * Checking for the provided visitUUID if the visit is Ended or not by checking the enddate column for NULL value.
     *
     * @param visitUUID
     * @return
     */
    public static PrescriptionModel isVisitNotEnded(String visitUUID) {
        PrescriptionModel model = new PrescriptionModel();

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        //db.beginTransaction();

        Cursor cursor = db.rawQuery("SELECT * FROM tbl_visit where uuid = ? and (sync = 1 OR sync = 'TRUE' OR sync = 'true') AND " +
                "voided = 0 AND enddate is null", new String[]{visitUUID});  // enddate is null ie. visit is not yet ended.

        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            do {
                model.setVisitUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
                model.setPatientUuid(cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")));
            }
            while (cursor.moveToNext());
        }

        cursor.close();
//        db.setTransactionSuccessful();
//        db.endTransaction();

        return model;
    }


    /**
     * Todays Visits that are not Ended.
     */
    public static List<PrescriptionModel> recentNotEndedVisits(int limit, int offset) {
        List<PrescriptionModel> arrayList = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        //db.beginTransaction();

        Cursor cursor = db.rawQuery("SELECT p.uuid, v.uuid as visitUUID, p.patient_photo, p.first_name, p.middle_name, p.last_name, p.phone_number,p.date_of_birth,p.gender,p.openmrs_id," +
                        " v.startdate " +
                        "FROM tbl_patient p, tbl_visit v WHERE p.uuid = v.patientuuid and (v.sync = 1 OR v.sync = 'TRUE' OR v.sync = 'true') AND " +
                        "v.voided = 0 AND " +
//                "(substr(v.startdate, 1, 4) ||'-'|| substr(v.startdate, 6,2) ||'-'|| substr(v.startdate, 9,2)) = DATE('now')" +
                        " v.startdate > DATETIME('now', '-4 day') " +
                        " AND v.enddate IS NULL ORDER BY v.startdate DESC limit ? offset ?",
                new String[]{String.valueOf(limit), String.valueOf(offset)});

        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            do {
                PrescriptionModel model = new PrescriptionModel();

                model.setPatientUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
                model.setPatient_photo(cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")));
                model.setVisitUuid(cursor.getString(cursor.getColumnIndexOrThrow("visitUUID")));
                model.setFirst_name(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
                model.setMiddle_name(cursor.getString(cursor.getColumnIndexOrThrow("middle_name")));
                model.setPhone_number(cursor.getString(cursor.getColumnIndexOrThrow("phone_number")));
                model.setLast_name(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                //    model.setVisit_start_date(cursor.getString(cursor.getColumnIndexOrThrow("startdate")).substring(0, 10));  // IDA-1350
                model.setVisit_start_date(cursor.getString(cursor.getColumnIndexOrThrow("startdate")));
                model.setDob(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                model.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                model.setOpenmrs_id(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));

                try {
                    model.setHasPrescription(new EncounterDAO().isPrescriptionReceived(model.getVisitUuid()));
                } catch (DAOException e) {
                    CustomLog.e(TAG,e.getMessage());
                    throw new RuntimeException(e);
                }
                arrayList.add(model);
            }
            while (cursor.moveToNext());
        }

        cursor.close();
        //db.setTransactionSuccessful();
        //db.endTransaction();

        return arrayList;
    }

    public static List<PrescriptionModel> allNotEndedVisits() {
        List<PrescriptionModel> arrayList = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();

        Cursor cursor = db.rawQuery("SELECT p.uuid, v.uuid as visitUUID, p.patient_photo, p.first_name, p.middle_name, p.last_name, p.phone_number, v.startdate " +
                        "FROM tbl_patient p, tbl_visit v WHERE p.uuid = v.patientuuid and (v.sync = 1 OR v.sync = 'TRUE' OR v.sync = 'true') AND " +
                        "v.voided = 0 AND " +
                        //  " v.startdate > DATETIME('now', '-4 day') " +
                        // " AND v.enddate IS NULL ORDER BY v.startdate DESC",
                        "v.enddate IS NULL ORDER BY v.startdate DESC",
                new String[]{});

        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            do {
                PrescriptionModel model = new PrescriptionModel();

                model.setPatientUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
                model.setPatient_photo(cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")));
                model.setVisitUuid(cursor.getString(cursor.getColumnIndexOrThrow("visitUUID")));
                model.setFirst_name(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
                model.setMiddle_name(cursor.getString(cursor.getColumnIndexOrThrow("middle_name")));
                model.setPhone_number(cursor.getString(cursor.getColumnIndexOrThrow("phone_number")));
                model.setLast_name(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                //    model.setVisit_start_date(cursor.getString(cursor.getColumnIndexOrThrow("startdate")).substring(0, 10));  // IDA-1350
                model.setVisit_start_date(cursor.getString(cursor.getColumnIndexOrThrow("startdate")));

                model.setDob(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                model.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                model.setOpenmrs_id(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));

                try {
                    model.setHasPrescription(new EncounterDAO().isPrescriptionReceived(model.getVisitUuid()));
                } catch (DAOException e) {
                    CustomLog.e(TAG,e.getMessage());
                    throw new RuntimeException(e);
                }
                arrayList.add(model);
            }
            while (cursor.moveToNext());
        }

        cursor.close();
//        db.setTransactionSuccessful();
//        db.endTransaction();

        return arrayList;
    }

    public static List<PrescriptionModel> recentNotEndedVisits() {
        List<PrescriptionModel> arrayList = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
//        db.beginTransaction();

        Cursor cursor = db.rawQuery("SELECT p.uuid, v.uuid as visitUUID, p.patient_photo, p.first_name, p.middle_name, p.last_name, p.phone_number,p.date_of_birth,p.gender,p.openmrs_id," +
                        " v.startdate " +
                        "FROM tbl_patient p, tbl_visit v WHERE p.uuid = v.patientuuid and (v.sync = 1 OR v.sync = 'TRUE' OR v.sync = 'true') AND " +
                        "v.voided = 0 AND" +
//                "(substr(v.startdate, 1, 4) ||'-'|| substr(v.startdate, 6,2) ||'-'|| substr(v.startdate, 9,2)) = DATE('now')" +
                        " v.startdate > DATETIME('now', '-4 day')" +
                        " AND v.enddate IS NULL ORDER BY v.startdate DESC",
                new String[]{});

        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            do {
                PrescriptionModel model = new PrescriptionModel();
                //
                model.setPatientUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
                model.setPatient_photo(cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")));
                model.setVisitUuid(cursor.getString(cursor.getColumnIndexOrThrow("visitUUID")));
                model.setFirst_name(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
                model.setMiddle_name(cursor.getString(cursor.getColumnIndexOrThrow("middle_name")));
                model.setPhone_number(cursor.getString(cursor.getColumnIndexOrThrow("phone_number")));
                model.setLast_name(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                //    model.setVisit_start_date(cursor.getString(cursor.getColumnIndexOrThrow("startdate")).substring(0, 10));  // IDA-1350
                model.setVisit_start_date(cursor.getString(cursor.getColumnIndexOrThrow("startdate")));
                model.setDob(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                model.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                model.setOpenmrs_id(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));

                try {
                    model.setHasPrescription(new EncounterDAO().isPrescriptionReceived(model.getVisitUuid()));
                } catch (DAOException e) {
                    CustomLog.e(TAG,e.getMessage());
                    throw new RuntimeException(e);
                }
                //
                arrayList.add(model);
            }
            while (cursor.moveToNext());
        }

        cursor.close();
//        db.setTransactionSuccessful();
//        db.endTransaction();

        return arrayList;
    }

    /**
     * This Weeks Visits that are not Ended.
     */
    public static List<PrescriptionModel> olderNotEndedVisits() {
        List<PrescriptionModel> arrayList = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
//        db.beginTransaction();

        Cursor cursor = db.rawQuery("SELECT p.uuid, v.uuid as visitUUID, p.patient_photo, p.first_name,  p.middle_name, p.last_name, p.phone_number, p.date_of_birth,p.gender,p.openmrs_id," +
                        " v.startdate " +
                        "FROM tbl_patient p, tbl_visit v WHERE p.uuid = v.patientuuid and (v.sync = 1 OR v.sync = 'TRUE' OR v.sync = 'true') AND " +
                        "v.voided = 0 AND " +
//                "STRFTIME('%Y',date(substr(v.startdate, 1, 4)||'-'||substr(v.startdate, 6, 2)||'-'||substr(v.startdate, 9,2))) = STRFTIME('%Y',DATE('now')) " +
//                "AND STRFTIME('%W',date(substr(v.startdate, 1, 4)||'-'||substr(v.startdate, 6, 2)||'-'||substr(v.startdate, 9,2))) = STRFTIME('%W',DATE('now')) AND " +
                        " v.startdate < DATETIME('now', '-4 day') AND " +
                        "v.enddate IS NULL ORDER BY v.startdate DESC",
                new String[]{});

        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            do {
                PrescriptionModel model = new PrescriptionModel();

                model.setPatientUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
                model.setPatient_photo(cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")));
                model.setVisitUuid(cursor.getString(cursor.getColumnIndexOrThrow("visitUUID")));
                model.setFirst_name(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
                model.setMiddle_name(cursor.getString(cursor.getColumnIndexOrThrow("middle_name")));
                model.setPhone_number(cursor.getString(cursor.getColumnIndexOrThrow("phone_number")));
                model.setLast_name(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                //   model.setVisit_start_date(cursor.getString(cursor.getColumnIndexOrThrow("startdate")).substring(0, 10)); // IDA-1350
                model.setVisit_start_date(cursor.getString(cursor.getColumnIndexOrThrow("startdate")));
                model.setDob(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                model.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                model.setOpenmrs_id(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
                try {
                    model.setHasPrescription(new EncounterDAO().isPrescriptionReceived(model.getVisitUuid()));
                } catch (DAOException e) {
                    CustomLog.e(TAG,e.getMessage());
                    throw new RuntimeException(e);
                }
                arrayList.add(model);
            }
            while (cursor.moveToNext());
        }

        cursor.close();
//        db.setTransactionSuccessful();
//        db.endTransaction();

        return arrayList;
    }

    public static List<PrescriptionModel> olderNotEndedVisits(int limit, int offset) {
        List<PrescriptionModel> arrayList = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
//        db.beginTransaction();

        Cursor cursor = db.rawQuery("SELECT p.uuid, v.uuid as visitUUID, p.patient_photo, p.first_name,  p.middle_name, p.last_name, p.phone_number, p.date_of_birth,p.gender,p.openmrs_id," +
                        " v.startdate " +
                        "FROM tbl_patient p, tbl_visit v WHERE p.uuid = v.patientuuid and (v.sync = 1 OR v.sync = 'TRUE' OR v.sync = 'true') AND " +
                        "v.voided = 0 AND " +
//                "STRFTIME('%Y',date(substr(v.startdate, 1, 4)||'-'||substr(v.startdate, 6, 2)||'-'||substr(v.startdate, 9,2))) = STRFTIME('%Y',DATE('now')) " +
//                "AND STRFTIME('%W',date(substr(v.startdate, 1, 4)||'-'||substr(v.startdate, 6, 2)||'-'||substr(v.startdate, 9,2))) = STRFTIME('%W',DATE('now')) AND " +
                        " v.startdate < DATETIME('now', '-4 day') AND " +
                        "v.enddate IS NULL ORDER BY v.startdate DESC limit ? offset ?",
                new String[]{String.valueOf(limit), String.valueOf(offset)});

        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            do {
                PrescriptionModel model = new PrescriptionModel();

                model.setPatientUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
                model.setPatient_photo(cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")));
                model.setVisitUuid(cursor.getString(cursor.getColumnIndexOrThrow("visitUUID")));
                model.setFirst_name(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
                model.setMiddle_name(cursor.getString(cursor.getColumnIndexOrThrow("middle_name")));
                model.setPhone_number(cursor.getString(cursor.getColumnIndexOrThrow("phone_number")));
                model.setLast_name(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                //   model.setVisit_start_date(cursor.getString(cursor.getColumnIndexOrThrow("startdate")).substring(0, 10)); // IDA-1350
                model.setVisit_start_date(cursor.getString(cursor.getColumnIndexOrThrow("startdate")));
                model.setDob(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                model.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                model.setOpenmrs_id(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
                try {
                    model.setHasPrescription(new EncounterDAO().isPrescriptionReceived(model.getVisitUuid()));
                } catch (DAOException e) {
                    CustomLog.e(TAG,e.getMessage());
                    throw new RuntimeException(e);
                }
                arrayList.add(model);
            }
            while (cursor.moveToNext());
        }

        cursor.close();
//        db.setTransactionSuccessful();
//        db.endTransaction();

        return arrayList;
    }

    /**
     * This Months Visits that are not Ended.
     */
    public static List<PrescriptionModel> thisMonths_NotEndedVisits() {
        List<PrescriptionModel> arrayList = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
//        db.beginTransaction();

        Cursor cursor = db.rawQuery("SELECT p.uuid, v.uuid as visitUUID, p.patient_photo, p.first_name, p.last_name, p.phone_number, v.startdate " +
                "FROM tbl_patient p, tbl_visit v WHERE p.uuid = v.patientuuid and (v.sync = 1 OR v.sync = 'TRUE' OR v.sync = 'true') AND " +
                "v.voided = 0 AND " +
                "STRFTIME('%Y',date(substr(v.startdate, 1, 4)||'-'||substr(v.startdate, 6, 2)||'-'||substr(v.startdate, 9,2))) = STRFTIME('%Y',DATE('now')) AND " +
                "STRFTIME('%m',date(substr(v.startdate, 1, 4)||'-'||substr(v.startdate, 6, 2)||'-'||substr(v.startdate, 9,2))) = STRFTIME('%m',DATE('now')) AND " +
                "v.enddate IS NULL", new String[]{});

        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            do {
                PrescriptionModel model = new PrescriptionModel();

                model.setPatientUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
                model.setPatient_photo(cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")));
                model.setVisitUuid(cursor.getString(cursor.getColumnIndexOrThrow("visitUUID")));
                model.setFirst_name(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
                model.setPhone_number(cursor.getString(cursor.getColumnIndexOrThrow("phone_number")));
                model.setLast_name(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                model.setVisit_start_date(cursor.getString(cursor.getColumnIndexOrThrow("startdate")).substring(0, 10));
                arrayList.add(model);
            }
            while (cursor.moveToNext());
        }

        cursor.close();
//        db.setTransactionSuccessful();
//        db.endTransaction();

        return arrayList;
    }


    public static List<PrescriptionModel> recentVisits(int limit, int offset) {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        List<PrescriptionModel> recentList = new ArrayList<>();
        db.beginTransaction();

        // ie. visit is active and presc is given.
        Cursor cursor = db.rawQuery("select p.patient_photo, p.first_name, p.middle_name, p.last_name, p.openmrs_id, p.date_of_birth, p.phone_number, p.gender, v.startdate, v.patientuuid, e.visituuid, e.uuid as euid," +
                        " o.uuid as ouid, o.obsservermodifieddate, o.sync as osync from tbl_patient p, tbl_visit v, tbl_encounter e, tbl_obs o where" +
                        " p.uuid = v.patientuuid and v.uuid = e.visituuid and euid = o.encounteruuid " +
                        //" and v.enddate is null " +
                        "and e.encounter_type_uuid = ? and" +
                        " (o.sync = 1 OR o.sync = 'TRUE' OR o.sync = 'true') AND o.voided = 0 " +//and" + " o.conceptuuid = ? and " +
                        " and v.startdate > DATETIME('now', '-4 day') " +
                        " group by p.openmrs_id ORDER BY v.startdate DESC limit ? offset ?",

                new String[]{ENCOUNTER_VISIT_COMPLETE, String.valueOf(limit), String.valueOf(offset)});  // 537bb20d-d09d-4f88-930b-cc45c7d662df -> Diagnosis conceptID.

        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            do {
                PrescriptionModel model = new PrescriptionModel();
                // emergency - start
                String visitID = cursor.getString(cursor.getColumnIndexOrThrow("visituuid"));
                boolean isCompletedExitedSurvey = false;
                boolean isPrescriptionReceived = false;
                try {
                    isCompletedExitedSurvey = new EncounterDAO().isCompletedExitedSurvey(visitID);
                    isPrescriptionReceived = new EncounterDAO().isPrescriptionReceived(visitID);
                } catch (DAOException e) {
                    CustomLog.e(TAG,e.getMessage());
                    e.printStackTrace();
                }
                if (!isCompletedExitedSurvey && isPrescriptionReceived) {
                    String emergencyUuid = "";
                    EncounterDAO encounterDAO = new EncounterDAO();
                    try {
                        emergencyUuid = encounterDAO.getEmergencyEncounters(visitID, encounterDAO.getEncounterTypeUuid("EMERGENCY"));
                    } catch (DAOException e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                        CustomLog.e(TAG,e.getMessage());
                        emergencyUuid = "";
                    }

                    if (!emergencyUuid.equalsIgnoreCase("")) // ie. visit is emergency visit.
                        model.setEmergency(true);
                    else
                        model.setEmergency(false);
                    // emergency - end

                    model.setHasPrescription(true);
                    model.setEncounterUuid(cursor.getString(cursor.getColumnIndexOrThrow("euid")));
                    model.setVisitUuid(visitID);
                    model.setSync(cursor.getString(cursor.getColumnIndexOrThrow("osync")));
                    model.setPatientUuid(cursor.getString(cursor.getColumnIndexOrThrow("patientuuid")));
                    model.setVisit_start_date(cursor.getString(cursor.getColumnIndexOrThrow("startdate")));
                    model.setPatient_photo(cursor.getString(cursor.getColumnIndexOrThrow("patient_photo")));
                    model.setFirst_name(cursor.getString(cursor.getColumnIndexOrThrow("first_name")));
                    model.setMiddle_name(cursor.getString(cursor.getColumnIndexOrThrow("middle_name")));
                    model.setPhone_number(cursor.getString(cursor.getColumnIndexOrThrow("phone_number")));
                    model.setLast_name(cursor.getString(cursor.getColumnIndexOrThrow("last_name")));
                    model.setOpenmrs_id(cursor.getString(cursor.getColumnIndexOrThrow("openmrs_id")));
                    model.setDob(cursor.getString(cursor.getColumnIndexOrThrow("date_of_birth")));
                    model.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                    model.setObsservermodifieddate(cursor.getString(cursor.getColumnIndexOrThrow("obsservermodifieddate")));
                    recentList.add(model);

                }
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();

        return recentList;
    }


    public static String fetchVisitModifiedDateForPrescPending(String visitUUID) {
        String modifiedDate = "";

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
//        db.beginTransaction();

        if (visitUUID != null) {
            final Cursor cursor = db.rawQuery("select p.first_name, p.last_name, o.obsservermodifieddate from tbl_patient as p, tbl_visit as v, tbl_encounter as e, tbl_obs as o where " +
                            "p.uuid = v.patientuuid and v.uuid = e.visituuid and e.uuid = o.encounteruuid and " +
                            "(o.sync = 'TRUE' OR o.sync = 'true' OR o.sync = 1) and o.voided = 0 and " +
                            "v.uuid = ? and " +
                            "e.encounter_type_uuid = ? group by p.openmrs_id",
                    new String[]{visitUUID, ENCOUNTER_ADULTINITIAL});

            if (cursor.moveToFirst()) {
                do {
                    try {
                        modifiedDate = cursor.getString(cursor.getColumnIndexOrThrow("obsservermodifieddate"));
                        CustomLog.v("obsservermodifieddate", "obsservermodifieddate: " + modifiedDate);
                    } catch (Exception e) {
                        e.printStackTrace();
                        CustomLog.e(TAG,e.getMessage());
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
//            db.setTransactionSuccessful();
//            db.endTransaction();
        }

        return modifiedDate;
    }

    /**
     * This function is used to return counts of todays, thisweeks, thismonths visit who are NOT ENDED by HW.
     *
     * @return
     */
    public static int getTotalCounts_EndVisit() {
        int total = 0;

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
//        db.beginTransaction();

        // Todays cursor
     /*   final Cursor today_cursor = db.rawQuery("SELECT count(*) FROM  tbl_visit  where (sync = 1 OR sync = 'TRUE' OR sync = 'true') AND voided = 0 AND " +
                "(substr(startdate, 1, 4) ||'-'|| substr(startdate, 6,2) ||'-'|| substr(startdate, 9,2)) = DATE('now') AND enddate IS NULL", new String[]{});
        if (today_cursor.moveToFirst()) {
            do {
                total = total + today_cursor.getInt(0);
            }
            while (today_cursor.moveToNext());
        }
            today_cursor.close();

                // Week cursor
        final Cursor week_cursor = db.rawQuery("SELECT count(*) FROM  tbl_visit  where (sync = 1 OR sync = 'TRUE' OR sync = 'true') AND voided = 0 AND " +
                "STRFTIME('%Y',date(substr(startdate, 1, 4)||'-'||substr(startdate, 6, 2)||'-'||substr(startdate, 9,2))) = STRFTIME('%Y',DATE('now')) " +
                "AND STRFTIME('%W',date(substr(startdate, 1, 4)||'-'||substr(startdate, 6, 2)||'-'||substr(startdate, 9,2))) = STRFTIME('%W',DATE('now')) AND enddate IS NULL", new String[]{});
        if (week_cursor.moveToFirst()) {
            do {
                total = total + week_cursor.getInt(0);
            }
            while (week_cursor.moveToNext());
        }
            week_cursor.close();*/

        // Month cursor
        final Cursor month_cursor = db.rawQuery("SELECT count(*) FROM  tbl_visit  where (sync = 1 OR sync = 'TRUE' OR sync = 'true') AND voided = 0 AND " +
                "STRFTIME('%Y',date(substr(startdate, 1, 4)||'-'||substr(startdate, 6, 2)||'-'||substr(startdate, 9,2))) = STRFTIME('%Y',DATE('now')) " +
                "AND STRFTIME('%m',date(substr(startdate, 1, 4)||'-'||substr(startdate, 6, 2)||'-'||substr(startdate, 9,2))) = STRFTIME('%m',DATE('now')) AND enddate IS NULL", new String[]{});
        if (month_cursor.moveToFirst()) {
            do {
                total = total + month_cursor.getInt(0);
            }
            while (month_cursor.moveToNext());
        }
        month_cursor.close();

//        db.setTransactionSuccessful();
//        db.endTransaction();

        CustomLog.v("totalCount", "totalCountsEndVisit: " + total);

        return total;
    }

    public static boolean isVisitUploaded(String visitUUID) {
        boolean isUploaded = false;
        String query = "SELECT sync FROM tbl_visit WHERE uuid = ? LIMIT 1";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        db.beginTransaction();
        Cursor cursor = db.rawQuery(query, new String[]{visitUUID});
        cursor.moveToFirst();

        String syncValue = cursor.getString(cursor.getColumnIndexOrThrow("sync"));
        if (syncValue.equalsIgnoreCase("1"))
            isUploaded = true;

        cursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
        return isUploaded;
    }

    public static boolean isVisitEnded(String visitUUID) {
        boolean isVisitEnded = false;
        String query = "SELECT enddate FROM tbl_visit WHERE uuid = ? LIMIT 1";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        db.beginTransaction();
        Cursor cursor = db.rawQuery(query, new String[]{visitUUID});

        if (cursor != null && cursor.moveToFirst()) {
            String endVisitValue = cursor.getString(cursor.getColumnIndexOrThrow("enddate"));
            if (endVisitValue != null) {
                isVisitEnded = true;
            }
            cursor.close();
        }

        db.setTransactionSuccessful();
        db.endTransaction();
        return isVisitEnded;
    }

    public static int getPendingPrescCount() {
        int count = 0;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
//        db.beginTransaction();

        Cursor cursor = db.rawQuery("select p.patient_photo, p.first_name, p.last_name, p.openmrs_id, p.date_of_birth, p.phone_number, p.gender, v.startdate, v.patientuuid, e.visituuid, e.uuid as euid," +
                " o.uuid as ouid, o.obsservermodifieddate, o.sync as osync from tbl_patient p, tbl_visit v, tbl_encounter e, tbl_obs o where" +
                " p.uuid = v.patientuuid and v.uuid = e.visituuid and euid = o.encounteruuid and" +
                " v.enddate is null and" +
                " (o.sync = 1 OR o.sync = 'TRUE' OR o.sync = 'true') AND o.voided = 0" +
                " group by e.visituuid ORDER BY v.startdate DESC", new String[]{});

        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            do {
                String visitID = cursor.getString(cursor.getColumnIndexOrThrow("visituuid"));
                boolean isCompletedExitedSurvey = false;
                boolean isPrescriptionReceived = false;
                try {
                    isCompletedExitedSurvey = new EncounterDAO().isCompletedExitedSurvey(visitID);
                    isPrescriptionReceived = new EncounterDAO().isPrescriptionReceived(visitID);
                } catch (DAOException e) {
                    e.printStackTrace();
                    CustomLog.e(TAG,e.getMessage());
                }

                if (!isCompletedExitedSurvey && !isPrescriptionReceived) {  //
                    count++;
                }
            }
            while (cursor.moveToNext());
        }

        cursor.close();
//        db.setTransactionSuccessful();
//        db.endTransaction();

        CustomLog.d("TAG", "getPendingPrescCount: " + String.valueOf(count));
        return count;
    }

    public String getVisitIdByPatientId(String patientId) {
        String visitId = "";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT uuid FROM tbl_visit where patientuuid = ? ", new String[]{patientId});
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                visitId = cursor.getString(cursor.getColumnIndexOrThrow("uuid"));
            }
        }
        cursor.close();

        return visitId;
    }

    //visit count to retry db operation
    //sometimes app crash cause of db lock
    //that's why added the retry mechanism whenever db will be lock
    int getVisitCount = 0;
    public int getVisitCountsByStatus(boolean isForReceivedPrescription) {
        int count = 0;
        //we are retrying db operation for 5 times
        if(getVisitCount > 5) return 0;

        try{
            SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
            db.beginTransaction();
            Cursor cursor = null;
            if (isForReceivedPrescription)
                cursor = db.rawQuery("select p.patient_photo, p.first_name, p.last_name, p.openmrs_id, p.date_of_birth, p.gender, v.startdate, v.patientuuid, e.visituuid, e.uuid as euid,"
                        + " o.uuid as ouid, o.obsservermodifieddate, o.sync as osync from tbl_patient p, tbl_visit v, tbl_encounter e, tbl_obs o where"
                        + " p.uuid = v.patientuuid and v.uuid = e.visituuid and euid = o.encounteruuid and"
                        + "  e.encounter_type_uuid = ? and"
                        + " (o.sync = 1 OR o.sync = 'TRUE' OR o.sync = 'true') AND o.voided = 0 " //+ " o.conceptuuid = ? "
                        //+ " and STRFTIME('%Y',date(substr(o.obsservermodifieddate, 1, 10))) = STRFTIME('%Y',DATE('now')) AND "
                        //+ " STRFTIME('%m',date(substr(o.obsservermodifieddate, 1, 10))) = STRFTIME('%m',DATE('now'))"
//                    +" and v.startdate <= DATETIME('now', '-4 day') "
                        + " group by p.openmrs_id ORDER BY v.startdate DESC", new String[]{ENCOUNTER_VISIT_COMPLETE});  // 537bb20d-d09d-4f88-930b-cc45c7d662df -> Diagnosis conceptID.
            else
                cursor = db.rawQuery("select p.patient_photo, p.first_name, p.last_name, p.openmrs_id, p.date_of_birth, p.gender, v.startdate, v.patientuuid, e.visituuid, e.uuid as euid,"
                        + " o.uuid as ouid, o.obsservermodifieddate, o.sync as osync from tbl_patient p, tbl_visit v, tbl_encounter e, tbl_obs o where" + " p.uuid = v.patientuuid and v.uuid = e.visituuid and euid = o.encounteruuid and" +
                        //" e.encounter_type_uuid = ?  and " +
                        " (o.sync = 1 OR o.sync = 'TRUE' OR o.sync = 'true') AND o.voided = 0 "
                        //+ "and STRFTIME('%Y',date(substr(o.obsservermodifieddate, 1, 10))) = STRFTIME('%Y',DATE('now')) AND "
                        //+ " STRFTIME('%m',date(substr(o.obsservermodifieddate, 1, 10))) = STRFTIME('%m',DATE('now'))"
//                    +" and v.startdate <= DATETIME('now', '-4 day') "
                        + "  group by p.openmrs_id ORDER BY v.startdate DESC", new String[]{});
            if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                do {

                    String visitID = cursor.getString(cursor.getColumnIndexOrThrow("visituuid"));
                    boolean isCompletedExitedSurvey = false;
                    boolean isPrescriptionReceived = false;
                    try {
                        isCompletedExitedSurvey = new EncounterDAO().isCompletedExitedSurvey(visitID);
                        isPrescriptionReceived = new EncounterDAO().isPrescriptionReceived(visitID);
                    } catch (DAOException e) {
                        e.printStackTrace();
                        CustomLog.e(TAG,e.getMessage());
                    }
                    //TODO: need more improvement in main query, this condition can be done by join query
                    if (isForReceivedPrescription) {
                        if (!isCompletedExitedSurvey && isPrescriptionReceived) {
                            count += 1;
//                            Timber.tag("getVisitCountsByStatus").v("Received - " + cursor.getString(cursor.getColumnIndexOrThrow("first_name"))
//                                    + " " + cursor.getString(cursor.getColumnIndexOrThrow("last_name")) + " Gender - " + cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                        }
                    } else {
                        if (!isCompletedExitedSurvey && !isPrescriptionReceived) {
                            count += 1;
//                            Timber.tag("getVisitCountsByStatus").v("Pending - " + cursor.getString(cursor.getColumnIndexOrThrow("first_name"))
//                                    + " " + cursor.getString(cursor.getColumnIndexOrThrow("last_name")) + " Gender - " + cursor.getString(cursor.getColumnIndexOrThrow("gender")));

                        }
                    }
                } while (cursor.moveToNext());
            }

            cursor.close();
            db.setTransactionSuccessful();
            db.endTransaction();

            //resetting count after successful db operation
            getVisitCount = 0;
        }
        catch (Exception e){
            //if db is locked then retrying to execute db operation
            try{
                Thread.sleep(2000);
                getVisitCount++;
                getVisitCountsByStatus(isForReceivedPrescription);
            }catch (Exception ex){
                FirebaseCrashlytics.getInstance().recordException(ex);
                CustomLog.e(TAG,ex.getMessage());
            }
            CustomLog.e(TAG,e.getMessage());
        }

        return count;
    }
}