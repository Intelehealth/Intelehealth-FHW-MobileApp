package org.intelehealth.app.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.models.ObsImageModel.ObsPushDTO;
import org.intelehealth.app.models.patientImageModelRequest.PatientProfile;
import org.intelehealth.app.models.providerImageRequestModel.ProviderProfile;
import org.intelehealth.app.utilities.Base64Utils;
import org.intelehealth.app.utilities.CustomLog;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.utilities.exception.DAOException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImagesDAO {
    public String TAG = ImagesDAO.class.getSimpleName();

    public boolean insertObsImageDatabase(String uuid, String encounteruuid, String conceptUuid, String comments) throws DAOException {
        CustomLog.v(TAG, "ImagesDAO - insertObsImageDatabase uuid - " + uuid + "\t" + encounteruuid + "\t" + conceptUuid + "\t" + comments);
        boolean isInserted = false;
        SQLiteDatabase localdb = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        localdb.beginTransaction();
        ContentValues contentValues = new ContentValues();
        try {
            contentValues.put("uuid", uuid);
            contentValues.put("encounteruuid", encounteruuid);
            contentValues.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            contentValues.put("conceptuuid", conceptUuid);
            contentValues.put("voided", "0");
            contentValues.put("sync", "false");
            contentValues.put("comments", comments);
            localdb.insertWithOnConflict("tbl_obs", null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
            isInserted = true;
            localdb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            isInserted = false;
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();

        }
        return isInserted;
    }

    public boolean updateObs(String uuid) throws DAOException {
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        int updatedCount = 0;
        ContentValues values = new ContentValues();
        String selection = "uuid = ?";
        try {
            values.put("sync", "TRUE");
            updatedCount = db.update("tbl_obs", values, selection, new String[]{uuid});
            //If no value is not found, then update fails so insert instead.
            if (updatedCount == 0) {
            }
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Logger.logE(TAG, "exception ", e);

        } finally {
            db.endTransaction();

        }

        return true;
    }

    public boolean deleteConceptImages(String encounterUuid, String conceptUuid) throws DAOException {
        boolean isDeleted = false;
        int updateDeltedRows = 0;
        Logger.logD(TAG, "Deleted image uuid" + encounterUuid);
        SQLiteDatabase localdb = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        String[] coloumns = {"uuid"};
        String[] selectionArgs = {encounterUuid};
        localdb.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put("voided", "1"); //These Fields should be your String values of actual column names
            cv.put("sync", "false");
            localdb.updateWithOnConflict("tbl_obs", cv, "encounteruuid=? AND conceptuuid=?", new String[]{encounterUuid, conceptUuid}, SQLiteDatabase.CONFLICT_REPLACE);
            localdb.setTransactionSuccessful();
        } catch (SQLException sql) {
            FirebaseCrashlytics.getInstance().recordException(sql);
            throw new DAOException(sql);
        } finally {
            localdb.endTransaction();
        }
        return isDeleted;

    }

    public boolean deleteImageFromDatabase(String obsUuid) throws DAOException {
        boolean isDeleted = false;
        int updateDeltedRows = 0;
        Logger.logD(TAG, "Deleted image uuid" + obsUuid);
        SQLiteDatabase localdb = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();

        localdb.beginTransaction();
        try {

            ContentValues cv = new ContentValues();
            cv.put("voided", "1"); //These Fields should be your String values of actual column names
            cv.put("sync", "false");
            localdb.updateWithOnConflict("tbl_obs", cv, "uuid=? ", new String[]{obsUuid}, SQLiteDatabase.CONFLICT_REPLACE);
            localdb.setTransactionSuccessful();
        } catch (SQLException sql) {
            FirebaseCrashlytics.getInstance().recordException(sql);
            throw new DAOException(sql);
        } finally {
            localdb.endTransaction();
        }
        return isDeleted;

    }

    public List<String> getVoidedImageObs() throws DAOException {
        Logger.logD(TAG, "uuid for images");
        ArrayList<String> uuidList = new ArrayList<>();
        SQLiteDatabase localdb = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        //localdb.beginTransaction();
        try {
            Cursor idCursor = localdb.rawQuery("SELECT uuid FROM tbl_obs where (conceptuuid=? OR conceptuuid = ?) AND voided=? AND sync = ? COLLATE NOCASE", new String[]{UuidDictionary.COMPLEX_IMAGE_AD, UuidDictionary.COMPLEX_IMAGE_PE, "1", "false"});
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    uuidList.add(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                }
            }
            idCursor.close();
        } catch (SQLiteException e) {
            throw new DAOException(e);
        } finally {
            //localdb.endTransaction();

        }
        return uuidList;

    }

    public boolean insertPatientProfileImages(String imagepath, String patientUuid) throws DAOException {
        boolean isInserted = false;
        SQLiteDatabase localdb = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        localdb.beginTransaction();
        ContentValues contentValues = new ContentValues();
        try {
            contentValues.put("uuid", patientUuid);
            contentValues.put("patientuuid", patientUuid);
            contentValues.put("visituuid", "");
            contentValues.put("image_path", imagepath);
            contentValues.put("image_type", "PP");
            contentValues.put("obs_time_date", AppConstants.dateAndTimeUtils.currentDateTime());
            contentValues.put("sync", "false");
            localdb.insertWithOnConflict("tbl_image_records", null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
            isInserted = true;
            localdb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            isInserted = false;
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();

        }
        return isInserted;
    }

    public boolean updatePatientProfileImages(String imagepath, String patientuuid) throws DAOException {
        boolean isUpdated = false;
        long isupdate = 0;
        SQLiteDatabase localdb = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        localdb.beginTransaction();
        ContentValues contentValues = new ContentValues();
        String whereclause = "patientuuid = ? AND image_type = ?";
        try {
            contentValues.put("patientuuid", patientuuid);
            contentValues.put("image_path", imagepath);
            contentValues.put("obs_time_date", AppConstants.dateAndTimeUtils.currentDateTime());
            contentValues.put("sync", "false");
            isupdate = localdb.update("tbl_image_records", contentValues, whereclause, new String[]{patientuuid, "PP"});
            if (isupdate != 0)
                if (isupdate != 0)
                    isUpdated = true;
            localdb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            isUpdated = false;
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();

        }
        if (isupdate == 0)
            isUpdated = insertPatientProfileImages(imagepath, patientuuid);
        return isUpdated;
    }

    public List<PatientProfile> getPatientProfileUnsyncedImages() throws DAOException {
        List<PatientProfile> patientProfiles = new ArrayList<>();
        SQLiteDatabase localdb = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        Base64Utils base64Utils = new Base64Utils();
        //localdb.beginTransaction();
        try {
            Cursor idCursor = localdb.rawQuery("SELECT * FROM tbl_image_records where sync = ? OR sync=? AND image_type = ? COLLATE NOCASE", new String[]{"0", "false", "PP"});
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    PatientProfile patientProfile = new PatientProfile();
                    patientProfile.setPerson(idCursor.getString(idCursor.getColumnIndexOrThrow("patientuuid")));
                    patientProfile.setBase64EncodedImage(base64Utils.getBase64FromFileWithConversion(idCursor.getString(idCursor.getColumnIndexOrThrow("image_path"))));
                    patientProfiles.add(patientProfile);
                }
            }
            idCursor.close();
        } catch (SQLiteException e) {
            throw new DAOException(e);
        } finally {
            //localdb.endTransaction();

        }

        return patientProfiles;
    }

    public String getPatientProfileImage(String patientUUID) throws DAOException {
        List<PatientProfile> patientProfiles = new ArrayList<>();
        SQLiteDatabase localdb = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        Base64Utils base64Utils = new Base64Utils();
        String imagePath = "";
        //localdb.beginTransaction();
        try {
            Cursor idCursor = localdb.rawQuery("SELECT image_path FROM tbl_image_records where patinetuuid = ? AND image_type = ? COLLATE NOCASE", new String[]{patientUUID, "PP"});
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    //PatientProfile patientProfile = new PatientProfile();
                    //patientProfile.setPerson(idCursor.getString(idCursor.getColumnIndexOrThrow("patientuuid")));
                    imagePath = idCursor.getString(idCursor.getColumnIndexOrThrow("image_path"));
                    //patientProfile.setBase64EncodedImage(base64Utils.getBase64FromFileWithConversion());
                    //patientProfiles.add(patientProfile);
                }
            }
            idCursor.close();
        } catch (SQLiteException e) {
            throw new DAOException(e);
        } finally {
            //localdb.endTransaction();

        }

        return imagePath;
    }

    public List<ObsPushDTO> getObsUnsyncedImages() throws DAOException {
        List<ObsPushDTO> obsImages = new ArrayList<>();
        SQLiteDatabase localdb = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        //localdb.beginTransaction();
        try {
            Cursor idCursor = localdb.rawQuery("select c.uuid as patientuuid,d.conceptuuid,a.uuid as encounteruuid,d.uuid as obsuuid,d.comments as comment,d.modified_date  from tbl_encounter a , tbl_visit b , tbl_patient c,tbl_obs d where a.visituuid=b.uuid and b.patientuuid=c.uuid and d.encounteruuid=a.uuid and (d.sync=0 or d.sync='false') and (d.conceptuuid=? or d.conceptuuid=?) and d.voided='0'", new String[]{UuidDictionary.COMPLEX_IMAGE_PE, UuidDictionary.COMPLEX_IMAGE_AD});
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    ObsPushDTO obsPushDTO = new ObsPushDTO();
                    obsPushDTO.setConcept(idCursor.getString(idCursor.getColumnIndexOrThrow("conceptuuid")));
                    obsPushDTO.setEncounter(idCursor.getString(idCursor.getColumnIndexOrThrow("encounteruuid")));
                    obsPushDTO.setObsDatetime(idCursor.getString(idCursor.getColumnIndexOrThrow("modified_date")));
                    obsPushDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("obsuuid")));
                    obsPushDTO.setPerson(idCursor.getString(idCursor.getColumnIndexOrThrow("patientuuid")));
                    obsPushDTO.setComment(idCursor.getString(idCursor.getColumnIndexOrThrow("comment")));
                    obsImages.add(obsPushDTO);
                }
            }
            idCursor.close();
        } catch (SQLiteException e) {
            throw new DAOException(e);
        } finally {
            //localdb.endTransaction();

        }

        return obsImages;

    }


    public String getPatientProfileChangeTime(String patientUuid) throws DAOException {
        String datetime = "";
        SQLiteDatabase localdb = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        //localdb.beginTransaction();
        try {
            Cursor idCursor = localdb.rawQuery("SELECT * FROM tbl_image_records where patientuuid=? AND image_type = ? COLLATE NOCASE",
                    new String[]{patientUuid, "PP"});
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    datetime = idCursor.getString(idCursor.getColumnIndexOrThrow("obs_time_date"));
                }
            }
            idCursor.close();
        } catch (SQLiteException e) {
            throw new DAOException(e);
        } finally {
            //localdb.endTransaction();

        }

        return datetime;
    }


    public boolean updateUnsyncedPatientProfile(String patientuuid, String type) throws DAOException {
        boolean isUpdated = false;
        long isupdate = 0;
        SQLiteDatabase localdb = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        localdb.beginTransaction();
        ContentValues contentValues = new ContentValues();
        String whereclause = "patientuuid = ? AND image_type = ?";
        try {
            contentValues.put("patientuuid", patientuuid);
            contentValues.put("sync", "true");
            isupdate = localdb.update("tbl_image_records", contentValues, whereclause, new String[]{patientuuid, type});
            if (isupdate != 0)
                isUpdated = true;
            localdb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            isUpdated = false;
            FirebaseCrashlytics.getInstance().recordException(e);
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();

        }
        return isUpdated;
    }

    public boolean updateUnsyncedObsImages(String uuid) throws DAOException {
        boolean isUpdated = false;
        long isupdate = 0;
        SQLiteDatabase localdb = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        localdb.beginTransaction();
        ContentValues contentValues = new ContentValues();
        String whereclause = "uuid = ?";
        try {
            contentValues.put("uuid", uuid);
            contentValues.put("sync", "true");
            isupdate = localdb.update("tbl_obs", contentValues, whereclause, new String[]{uuid});
            if (isupdate != 0)
                isUpdated = true;
            localdb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            isUpdated = false;
            FirebaseCrashlytics.getInstance().recordException(e);
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();

        }
        return isUpdated;
    }


    public ArrayList getImageUuid(String encounterUuid, String conceptuuid) throws DAOException {
        Logger.logD(TAG, "encounter uuid for image " + encounterUuid);
        ArrayList<String> uuidList = new ArrayList<>();
        SQLiteDatabase localdb = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        //localdb.beginTransaction();
        try {
            Cursor idCursor = localdb.rawQuery("SELECT uuid FROM tbl_obs where encounteruuid=? AND conceptuuid = ? AND voided=? COLLATE NOCASE", new String[]{encounterUuid, conceptuuid, "0"});
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    uuidList.add(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                }
            }
            idCursor.close();
        } catch (SQLiteException e) {
            throw new DAOException(e);
        } finally {
            //localdb.endTransaction();

        }
        return uuidList;
    }


    public List<String> getImages(String encounterUUid, String ConceptUuid) throws DAOException {
        List<String> imagesList = new ArrayList<>();

        SQLiteDatabase localdb = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        //localdb.beginTransaction();
        try {
            Cursor idCursor = localdb.rawQuery("SELECT uuid FROM tbl_obs where encounteruuid=? AND conceptuuid = ? AND voided=? COLLATE NOCASE", new String[]{encounterUUid, ConceptUuid, "0"});
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    imagesList.add(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                }
            }
            idCursor.close();
        } catch (SQLiteException e) {
            throw new DAOException(e);
        } finally {
            //localdb.endTransaction();
        }
        return imagesList;
    }


    public List<String> isImageListObsExists(String encounterUuid, String conceptUuid) throws DAOException {
        List<String> imagesList = new ArrayList<>();
        SQLiteDatabase localdb = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        //localdb.beginTransaction();
        try {
            Cursor idCursor = localdb.rawQuery("SELECT uuid FROM tbl_obs where encounteruuid=? AND conceptuuid = ? AND voided=? COLLATE NOCASE order by modified_date", new String[]{encounterUuid, conceptUuid, "0"});
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    imagesList.add(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                }
            }
            idCursor.close();
        } catch (SQLiteException e) {
            throw new DAOException(e);
        } finally {
            //localdb.endTransaction();
        }

        return imagesList;
    }


    public boolean isLocalImageUuidExists(String imageuuid) throws DAOException {
        boolean isLocalImageExists = false;
        File imagesPath = new File(AppConstants.IMAGE_PATH);
        String imageName = imageuuid + ".jpg";
        if (new File(imagesPath + "/" + imageName).exists()) {
            isLocalImageExists = true;
        }
        return isLocalImageExists;
    }

    //added for push provider profile image to the server
    public boolean updateLoggedInUserProfileImage(String imagepath, String uuid) throws DAOException {
        CustomLog.d(TAG, "updateLoggedInUserProfileImage: imagepath : " + imagepath);
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
            //contentValues.put("sync", "false");
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
        return isUpdated;
    }

    public ProviderProfile getUserProfileUnsyncedImages(String uuid) throws DAOException {
        SQLiteDatabase localdb = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        Base64Utils base64Utils = new Base64Utils();
        ProviderProfile providerProfile = new ProviderProfile();
        //localdb.beginTransaction();
        try {
            Cursor idCursor = localdb.rawQuery("SELECT uuid, imagePath FROM tbl_provider where uuid = ? AND (sync = ? OR sync= ?) COLLATE NOCASE", new String[]{uuid, "0", "false"});
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {

                    providerProfile.setProviderid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                    providerProfile.setFile(base64Utils.getBase64FromFileWithConversion(idCursor.getString(idCursor.getColumnIndexOrThrow("imagePath"))));
                }
            }
            idCursor.close();
        } catch (SQLiteException e) {
            e.printStackTrace();
            throw new DAOException(e);
        } finally {
            //localdb.endTransaction();

        }

        return providerProfile;
    }

    public boolean updateUnsyncedUserProfile(String uuid) throws DAOException {
        boolean isUpdated = false;
        long isupdate = 0;
        SQLiteDatabase localdb = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        localdb.beginTransaction();


        try {
            String updateQuery = "UPDATE tbl_provider SET sync = 'true' WHERE uuid = '" + uuid + "'  AND sync = '0' OR sync = 'false'";
            Cursor c = localdb.rawQuery(updateQuery, null);
            localdb.setTransactionSuccessful();
        } catch (SQLiteException e) {
            isUpdated = false;
            FirebaseCrashlytics.getInstance().recordException(e);
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();

        }
        return isUpdated;
    }

}

