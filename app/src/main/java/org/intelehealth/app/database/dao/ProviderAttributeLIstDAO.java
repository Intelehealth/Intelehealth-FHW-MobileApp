package org.intelehealth.app.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.models.Uuid_Value;
import org.intelehealth.app.models.dto.ProviderAttributeListDTO;
import org.intelehealth.app.utilities.exception.DAOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Prajwal Waingankar
 * on 14-Jul-20.
 * Github: prajwalmw
 */


public class ProviderAttributeLIstDAO {
    private long createdRecordsCount = 0;

    public boolean insertProvidersAttributeList(List<ProviderAttributeListDTO> providerAttributeListDTOS)
            throws DAOException {

        boolean isInserted = true;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            for (ProviderAttributeListDTO providerAttributeListDTO : providerAttributeListDTOS) {
                createProvidersAttributeList(providerAttributeListDTO, db);
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

    private boolean createProvidersAttributeList(ProviderAttributeListDTO attributeListDTO, SQLiteDatabase db) throws DAOException {
        boolean isCreated = true;
        ContentValues values = new ContentValues();

        try {
            values.put("uuid", attributeListDTO.getUuid());
            values.put("provideruuid", attributeListDTO.getProvideruuid());
            values.put("attributetypeuuid", attributeListDTO.getAttributetypeuuid());
            values.put("value", attributeListDTO.getValue());
            values.put("voided", attributeListDTO.getVoided());

            if (attributeListDTO.getVoided() == 0 &&
                    attributeListDTO.getAttributetypeuuid().equalsIgnoreCase("ed1715f5-93e2-404e-b3c9-2a2d9600f062")) {
                createdRecordsCount = db.insertWithOnConflict("tbl_dr_speciality", null, values, SQLiteDatabase.CONFLICT_REPLACE);

                /*if (createdRecordsCount != -1) {
                    CustomLog.d("SPECI", "SIZEXXX: " + createdRecordsCount);
                } else {
                    CustomLog.d("SPECI", "SIZEXXX: " + createdRecordsCount);
                }*/

            }


        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {

        }

        return isCreated;
    }

    public List<String> getAllValues() {
        List<String> listDTOArrayList = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        //db.beginTransaction();
        String selectionArgs[] = {"ed1715f5-93e2-404e-b3c9-2a2d9600f062", "0"};
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_dr_speciality WHERE " +
                "attributetypeuuid = ? AND voided = ?", selectionArgs); //checking....

        ProviderAttributeListDTO dto = new ProviderAttributeListDTO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                dto = new ProviderAttributeListDTO();
                dto.setValue(idCursor.getString(idCursor.getColumnIndexOrThrow("value")));
                listDTOArrayList.add(dto.getValue());
            }
        }

        sortSpecialties(listDTOArrayList);
        idCursor.close();
       // db.setTransactionSuccessful();
        //db.endTransaction();
//        db.close();
        return listDTOArrayList;
    }


    public List<Uuid_Value> getSpeciality_Uuid_Value() {
        List<Uuid_Value> listDTOArrayList = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        String selectionArgs[] = {"ed1715f5-93e2-404e-b3c9-2a2d9600f062", "0"};
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_dr_speciality WHERE " +
                "attributetypeuuid = ? AND voided = ?", selectionArgs);

        ProviderAttributeListDTO dto = new ProviderAttributeListDTO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                dto = new ProviderAttributeListDTO();
                dto.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                dto.setValue(idCursor.getString(idCursor.getColumnIndexOrThrow("value")));
                listDTOArrayList.add(new Uuid_Value(dto.getUuid(), dto.getValue()));
            }
        }
        idCursor.close();
        db.setTransactionSuccessful();
        db.endTransaction();
//        db.close();
        return listDTOArrayList;
    }


    private void sortSpecialties(List<String> specialtyList) {
        String gpSpecialtyString = "General Physician";
        Collections.sort(specialtyList);
        if (specialtyList.contains(gpSpecialtyString)) {
            specialtyList.remove(gpSpecialtyString);
            specialtyList.add(0, gpSpecialtyString);
        }
    }
}