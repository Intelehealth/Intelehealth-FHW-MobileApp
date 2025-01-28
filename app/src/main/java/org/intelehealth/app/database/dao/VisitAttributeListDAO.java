package org.intelehealth.app.database.dao;

import static org.intelehealth.app.utilities.UuidDictionary.ADDITIONAL_NOTES;
import static org.intelehealth.app.utilities.UuidDictionary.CONSULTATION_TYPE;
import static org.intelehealth.app.utilities.UuidDictionary.DIAGNOSIS;
import static org.intelehealth.app.utilities.UuidDictionary.PRESCRIPTION_LINK;
import static org.intelehealth.app.utilities.UuidDictionary.SPECIALITY;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.models.dto.VisitAttributeDTO;
import org.intelehealth.app.utilities.CustomLog;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.List;
import java.util.UUID;

/**
 * Created by Prajwal Waingankar
 * on 20-Jul-20.
 * Github: prajwalmw
 */


public class VisitAttributeListDAO {
    private long createdRecordsCount = 0;
    private static final String TAG = "VisitAttributeListDAO";

    public boolean insertProvidersAttributeList(List<VisitAttributeDTO> visitAttributeDTOS)
            throws DAOException {

        boolean isInserted = true;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            for (VisitAttributeDTO visitDTO : visitAttributeDTOS) {
                createVisitAttributeList(visitDTO, db);
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

    private boolean createVisitAttributeList(VisitAttributeDTO visitDTO, SQLiteDatabase db) throws DAOException {

        boolean isCreated = true;
        ContentValues values = new ContentValues();
        String where = "visit_uuid=?";
        String whereArgs[] = {visitDTO.getVisit_uuid()};
        try {

//            values.put("speciality_value", visitDTO.getValue());
            values.put("uuid", visitDTO.getUuid());
            values.put("visit_uuid", visitDTO.getVisit_uuid());
            values.put("value", visitDTO.getValue());
            values.put("visit_attribute_type_uuid", visitDTO.getVisit_attribute_type_uuid());
            values.put("voided", visitDTO.getVoided());
            values.put("sync", "1");

            if (visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase(SPECIALITY) ||
                    visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase(ADDITIONAL_NOTES) ||
                    visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase(PRESCRIPTION_LINK) ||
                    visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase(DIAGNOSIS) ||
                    visitDTO.getVisit_attribute_type_uuid().equalsIgnoreCase(CONSULTATION_TYPE)) {
                createdRecordsCount = db.insertWithOnConflict("tbl_visit_attribute", null, values, SQLiteDatabase.CONFLICT_REPLACE);

//                if (createdRecordsCount != -1) {
//                    CustomLog.d("SPECI", "SIZEVISTATTR: " + createdRecordsCount);
//                } else {
//                    CustomLog.d("SPECI", "SIZEVISTATTR: " + createdRecordsCount);
//                }
            }
        } catch (SQLException e) {
            isCreated = false;
            CustomLog.e(TAG,e.getMessage());
            throw new DAOException(e.getMessage(), e);
        } finally {

        }

        return isCreated;
    }

    public String getVisitAttributesList_specificVisit(String VISITUUID, String visit_attribute_type_uuid) {
        String isValue = "";

        if (VISITUUID != null) {
            CustomLog.d("specc", "spec_fun: " + VISITUUID);
            SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
            //db.beginTransaction();

            Cursor cursor = db.rawQuery("SELECT value FROM tbl_visit_attribute WHERE visit_uuid = ? and " +
                            "visit_attribute_type_uuid = ? and voided = 0",
                    new String[]{VISITUUID, visit_attribute_type_uuid});

            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    isValue = cursor.getString(cursor.getColumnIndexOrThrow("value"));
                    CustomLog.d("specc", "spec_3: " + isValue);
                }
            } else {
                isValue = "";
            }
            cursor.close();
            //db.setTransactionSuccessful();
            //db.endTransaction();
//            db.close();

            CustomLog.d("specc", "spec_4: " + isValue);
        }

        return isValue;
    }

    /**
     * Inserting Visit Attributes...
     *
     * @param visitUuid
     * @param value
     * @param attributeTypeUUID
     * @return
     * @throws DAOException
     */
    public boolean insertVisitAttributes(String visitUuid, String value, String attributeTypeUUID) throws
            DAOException {
        boolean isInserted = false;

        CustomLog.d("SPINNER", "SPINNER_Selected_visituuid_logs: " + visitUuid);
        CustomLog.d("SPINNER", "SPINNER_Selected_value_logs: " + value);

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        try {
            values.put("uuid", UUID.randomUUID().toString()); //as per patient attributes uuid generation.
            values.put("visit_uuid", visitUuid);
            values.put("value", value);
            values.put("visit_attribute_type_uuid", attributeTypeUUID);
            values.put("voided", "0");
            values.put("sync", "0");

            long count = db.insertWithOnConflict("tbl_visit_attribute", null,
                    values, SQLiteDatabase.CONFLICT_REPLACE);

            if (count != -1)
                isInserted = true;

            db.setTransactionSuccessful();
        } catch (SQLException e) {
            isInserted = false;
            CustomLog.e(TAG,e.getMessage());
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }

        CustomLog.d("isInserted", "isInserted: " + isInserted);
        return isInserted;
    }

    /**
     * Fetching speciality value for the visit.
     *
     * @param visitUUID
     * @return
     */
    public static String fetchSpecialityValue(String visitUUID) {
        String specialityValue = "No data found";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
        //db.beginTransaction();

        Cursor cursor = db.rawQuery("SELECT distinct(value) FROM tbl_visit_attribute WHERE visit_uuid=? and visit_attribute_type_uuid = ? and voided = 0",
                new String[]{visitUUID, "3f296939-c6d3-4d2e-b8ca-d7f4bfd42c2d"});

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                specialityValue = cursor.getString(cursor.getColumnIndexOrThrow("value"));
            }
        }
        cursor.close();
        //db.setTransactionSuccessful();
        //db.endTransaction();

        return specialityValue;
    }
}
