package org.intelehealth.app.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import org.intelehealth.app.utilities.CustomLog;


import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.List;

import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.models.dto.ProviderDTO;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.exception.DAOException;

public class ProviderDAO {
    private static final String TAG = "ProviderDAO";
    long createdRecordsCount = 0;

    public boolean insertProviders(List<ProviderDTO> providerDTOS) throws DAOException {

        boolean isInserted = true;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            for (ProviderDTO provider : providerDTOS) {
                createProviders(provider, db);
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

    private boolean createProviders(ProviderDTO provider, SQLiteDatabase db) throws DAOException {
        boolean isCreated = true;

        ContentValues values = new ContentValues();
        try {
            values.put("uuid", provider.getUuid());
            values.put("identifier", provider.getIdentifier());
            values.put("given_name", provider.getGivenName());
            values.put("family_name", provider.getFamilyName());
            values.put("voided", provider.getVoided());
            values.put("role", provider.getRole());
            values.put("useruuid", provider.getUseruuid());
            values.put("emailId", provider.getEmailId());
            values.put("telephoneNumber", provider.getTelephoneNumber());
            values.put("dateofbirth", provider.getDateofbirth());
            values.put("gender", provider.getGender());
            values.put("providerId", provider.getProviderId());
            values.put("middle_name", provider.getMiddle_name());
            values.put("countryCode", provider.getCountryCode());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("sync", "false");

            createdRecordsCount = db.insertWithOnConflict("tbl_provider", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
        }
        return isCreated;
    }

    public List<String> getProvidersList() throws DAOException {
        List<String> providersList = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        //db.beginTransaction();
        try {
            String query = "select distinct a.uuid,a.given_name,a.family_name from tbl_provider a, tbl_encounter b , tbl_visit c where a.uuid=b.provider_uuid and b.visituuid=c.uuid";
            Cursor cursor = db.rawQuery(query, new String[]{});
            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    providersList.add(cursor.getString(cursor.getColumnIndexOrThrow("given_name")) + cursor.getString(cursor.getColumnIndexOrThrow("family_name")));

                }
            }
            cursor.close();
            //db.setTransactionSuccessful();
        } catch (SQLException s) {
            FirebaseCrashlytics.getInstance().recordException(s);
            throw new DAOException(s);
        } finally {
            //db.endTransaction();

        }
        return providersList;

    }

    public List<String> getProvidersUuidList() throws DAOException {
        List<String> providersList = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            String query = "select distinct a.uuid,a.given_name,a.family_name from tbl_provider a, tbl_encounter b , tbl_visit c where a.uuid=b.provider_uuid and b.visituuid=c.uuid";
            Cursor cursor = db.rawQuery(query, new String[]{});
            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    providersList.add(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));

                }
            }
            cursor.close();
            db.setTransactionSuccessful();
        } catch (SQLException s) {
            FirebaseCrashlytics.getInstance().recordException(s);
            throw new DAOException(s);
        } finally {
            db.endTransaction();

        }
        return providersList;

    }

    public String getProviderGiven_Lastname(String uuid) throws DAOException {
        String fullname = "";
        String givenname = "", familyname = "";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            String query = "select * from tbl_provider where uuid = ?";
            Cursor cursor = db.rawQuery(query, new String[]{uuid});
            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    givenname = cursor.getString(cursor.getColumnIndexOrThrow("given_name"));
                    familyname = cursor.getString(cursor.getColumnIndexOrThrow("family_name"));
                    fullname = givenname + " " + familyname;
                }
            }
            cursor.close();
            db.setTransactionSuccessful();
        } catch (SQLException s) {
            FirebaseCrashlytics.getInstance().recordException(s);
            throw new DAOException(s);
        } finally {
            db.endTransaction();

        }

        if (!fullname.equalsIgnoreCase(""))
            return fullname;
        else
            return "Test Doctor";

    }

    public ProviderDTO getLoginUserDetails(String uuid) throws DAOException {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        ProviderDTO providerDTO = null;
        //if db in transaction then return null
        if (db.inTransaction()) return null;
        db.beginTransaction();

        try {
            String query = "select * from tbl_provider where uuid = ?";
            Cursor cursor = db.rawQuery(query, new String[]{uuid});
            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    providerDTO = new ProviderDTO();
                    providerDTO.setUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
                    providerDTO.setGivenName(cursor.getString(cursor.getColumnIndexOrThrow("given_name")));
                    providerDTO.setFamilyName(cursor.getString(cursor.getColumnIndexOrThrow("family_name")));
                    providerDTO.setEmailId(cursor.getString(cursor.getColumnIndexOrThrow("emailId")));
                    providerDTO.setTelephoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("telephoneNumber")));
                    providerDTO.setDateofbirth(cursor.getString(cursor.getColumnIndexOrThrow("dateofbirth")));
                    providerDTO.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                    providerDTO.setProviderId(cursor.getString(cursor.getColumnIndexOrThrow("providerId")));
                    providerDTO.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow("imagePath")));
                    providerDTO.setMiddle_name(cursor.getString(cursor.getColumnIndexOrThrow("middle_name")));
                    providerDTO.setCountryCode(cursor.getString(cursor.getColumnIndexOrThrow("countryCode")));
                    providerDTO.setRole(cursor.getString(cursor.getColumnIndexOrThrow("role")));


                }
            }
            cursor.close();
            db.setTransactionSuccessful();
        } catch (SQLException s) {
            FirebaseCrashlytics.getInstance().recordException(s);
            throw new DAOException(s);
        } finally {
            db.endTransaction();

        }
        return providerDTO;

    }

    public ProviderDTO getProviderInfo(String uuid) throws DAOException {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        ProviderDTO providerDTO = null;

        db.beginTransaction();
        try {
            String query = "select * from tbl_provider where uuid = ?";
            Cursor cursor = db.rawQuery(query, new String[]{uuid});
            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    providerDTO = new ProviderDTO();
                    providerDTO.setUuid(cursor.getString(cursor.getColumnIndexOrThrow("uuid")));
                    providerDTO.setGivenName(cursor.getString(cursor.getColumnIndexOrThrow("given_name")));
                    providerDTO.setFamilyName(cursor.getString(cursor.getColumnIndexOrThrow("family_name")));
                    providerDTO.setEmailId(cursor.getString(cursor.getColumnIndexOrThrow("emailId")));
                    providerDTO.setTelephoneNumber(cursor.getString(cursor.getColumnIndexOrThrow("telephoneNumber")));
                    providerDTO.setDateofbirth(cursor.getString(cursor.getColumnIndexOrThrow("dateofbirth")));
                    providerDTO.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
                    providerDTO.setProviderId(cursor.getString(cursor.getColumnIndexOrThrow("providerId")));
                    providerDTO.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow("imagePath")));
                    providerDTO.setMiddle_name(cursor.getString(cursor.getColumnIndexOrThrow("middle_name")));
                    providerDTO.setCountryCode(cursor.getString(cursor.getColumnIndexOrThrow("countryCode")));


                }
            }
            cursor.close();
            db.setTransactionSuccessful();
        } catch (SQLException s) {
            FirebaseCrashlytics.getInstance().recordException(s);
            throw new DAOException(s);
        } finally {
            db.endTransaction();

        }
        return providerDTO;

    }


    public boolean updateProfileDetails(ProviderDTO provider) {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();

        int updatedCount = 0;
        ContentValues values = new ContentValues();
        String selection = "uuid = ?";
        try {
            values.put("given_name", provider.getGivenName());
            values.put("family_name", provider.getFamilyName());
            values.put("emailId", provider.getEmailId());
            values.put("telephoneNumber", provider.getTelephoneNumber());
            values.put("dateofbirth", provider.getDateofbirth());
            values.put("gender", provider.getGender());
            values.put("imagePath", provider.getImagePath());
            values.put("middle_name", provider.getMiddle_name());
            values.put("countryCode", provider.getCountryCode());
            values.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("sync", "false");

            updatedCount = db.update("tbl_provider", values, selection, new String[]{provider.getUuid()});
            CustomLog.d(TAG, "updateProfileDetails: updatedCount : " + updatedCount);
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            e.printStackTrace();
            Logger.logE(TAG, "exception ", e);

        } finally {
            db.endTransaction();

        }
        return true;
    }

    public boolean updateProviderProfileSync(String uuid, String synced) throws DAOException {
        boolean isUpdated = true;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        String whereclause = "uuid=?";
        String[] whereargs = {uuid};
        try {
            values.put("sync", synced);
            // values.put("uuid", uuid);
            int i = db.update("tbl_provider", values, whereclause, whereargs);
            Logger.logD("profile", "updated" + i);
            db.setTransactionSuccessful();
        } catch (SQLException sql) {
            sql.printStackTrace();
            throw new DAOException(sql.getMessage());
        } finally {
            db.endTransaction();


        }

        return isUpdated;
    }

    public List<ProviderDTO> unsyncedProviderDetails(String uuid) throws DAOException {
        CustomLog.d(TAG, "unsyncedProviderDetails: uuid : " + uuid);
        List<ProviderDTO> providerDTOList = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            Cursor idCursor = db.rawQuery("SELECT * FROM tbl_provider where  uuid=? AND (sync = ? OR sync=?) COLLATE NOCASE", new String[]{uuid, "0", "false"});
            ProviderDTO providerProfileDTO = new ProviderDTO();
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    providerProfileDTO = new ProviderDTO();
                    providerProfileDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                    providerProfileDTO.setFamilyName(idCursor.getString(idCursor.getColumnIndexOrThrow("family_name")));
                    providerProfileDTO.setMiddle_name(idCursor.getString(idCursor.getColumnIndexOrThrow("middle_name")));
                    providerProfileDTO.setGivenName(idCursor.getString(idCursor.getColumnIndexOrThrow("given_name")));
                    providerProfileDTO.setGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                    providerProfileDTO.setDateofbirth(idCursor.getString(idCursor.getColumnIndexOrThrow("dateofbirth")));
                    providerProfileDTO.setTelephoneNumber(idCursor.getString(idCursor.getColumnIndexOrThrow("telephoneNumber")));
                    providerProfileDTO.setEmailId(idCursor.getString(idCursor.getColumnIndexOrThrow("emailId")));
                    providerProfileDTO.setProviderId(idCursor.getString(idCursor.getColumnIndexOrThrow("providerId")));
                    providerProfileDTO.setMiddle_name(idCursor.getString(idCursor.getColumnIndexOrThrow("middle_name")));
                    providerProfileDTO.setCountryCode(idCursor.getString(idCursor.getColumnIndexOrThrow("countryCode")));

                    providerDTOList.add(providerProfileDTO);
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

        return providerDTOList;
    }

    public boolean updateLoggedInUserProfileImage(String imagepath, String uuid) throws DAOException {

        boolean isUpdated = false;
        long isupdate = 0;
        SQLiteDatabase localdb = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        localdb.beginTransaction();
        ContentValues contentValues = new ContentValues();
        String whereclause = "uuid = ?";
        try {
            // contentValues.put("uuid", uuid);
            contentValues.put("imagePath", imagepath);
            contentValues.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            contentValues.put("sync", "false");
            isupdate = localdb.update("tbl_provider", contentValues, whereclause, new String[]{uuid});
            if (isupdate != 0)
                isUpdated = true;
            localdb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            isUpdated = false;
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();

        }
      /*  if (isupdate == 0)
            isUpdated = insertPatientProfileImages(imagepath, uuid);*/
        return isUpdated;
    }

    public String getProviderName(String userUuid, String columnName) throws DAOException {
        if (userUuid == null) return "";
        String fullname = "";
        String givenname = "", familyname = "";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        try {
            String query = "select * from tbl_provider where " + columnName + " = ?";
            Cursor cursor = db.rawQuery(query, new String[]{userUuid});
            if (cursor.getCount() != 0) {
                while (cursor.moveToNext()) {
                    givenname = cursor.getString(cursor.getColumnIndexOrThrow("given_name"));
                    familyname = cursor.getString(cursor.getColumnIndexOrThrow("family_name"));
                    fullname = givenname + " " + familyname;
                }
            }
            cursor.close();
            db.setTransactionSuccessful();
        } catch (SQLException s) {
            FirebaseCrashlytics.getInstance().recordException(s);
            throw new DAOException(s);
        } finally {
            db.endTransaction();

        }

        if (!fullname.equalsIgnoreCase(""))
            return fullname;
        else
            return "Test Doctor";

    }
}
