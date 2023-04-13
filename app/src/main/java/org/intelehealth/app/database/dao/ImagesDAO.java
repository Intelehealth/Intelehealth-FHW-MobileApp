package org.intelehealth.app.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;


import com.google.common.io.Files;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.intelehealth.app.models.patientImageModelRequest.ADPImageModel;
import org.intelehealth.app.models.patientImageModelRequest.PatientAdditionalDocModel;
import org.intelehealth.app.utilities.Base64Utils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.UuidDictionary;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.models.ObsImageModel.ObsPushDTO;
import org.intelehealth.app.models.patientImageModelRequest.PatientProfile;
import org.intelehealth.app.utilities.exception.DAOException;

public class ImagesDAO {
    public String TAG = ImagesDAO.class.getSimpleName();

    public boolean insertObsImageDatabase(String uuid, String encounteruuid, String conceptUuid) throws DAOException {
        boolean isInserted = false;
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        localdb.beginTransaction();
        ContentValues contentValues = new ContentValues();
        try {
            contentValues.put("uuid", uuid);
            contentValues.put("encounteruuid", encounteruuid);
            contentValues.put("modified_date", AppConstants.dateAndTimeUtils.currentDateTime());
            contentValues.put("conceptuuid", conceptUuid);
            contentValues.put("voided", "0");
            contentValues.put("sync", "false");
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
        SQLiteDatabase db = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
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
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        String[] coloumns = {"uuid"};
        String[] selectionArgs = {encounterUuid};
        localdb.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put("voided", "1"); //These Fields should be your String values of actual column names
            cv.put("sync", "false");
            localdb.updateWithOnConflict("tbl_obs", cv, "encounteruuid=? AND conceptuuid=?",
                    new String[]{encounterUuid, conceptUuid}, SQLiteDatabase.CONFLICT_REPLACE);
            localdb.setTransactionSuccessful();
        } catch (SQLException sql) {
            FirebaseCrashlytics.getInstance().recordException(sql);
            throw new DAOException(sql);
        } finally {
            localdb.endTransaction();
        }
        return isDeleted;

    }

    public static boolean deleteADPImages(String image_path) {
        boolean isDeleted = false;
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        int delete = localdb.delete("tbl_image_records", "image_path = ?", new String[]{image_path});
        Log.v("ADP", "ADP: " + "delete int: " + delete);

        if (delete > 0)
            return isDeleted = true;
        else
            return isDeleted = false;
    }

    public boolean deleteImageFromDatabase(String obsUuid) throws DAOException {
        boolean isDeleted = false;
        int updateDeltedRows = 0;
        Logger.logD(TAG, "Deleted image uuid" + obsUuid);
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();

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
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        localdb.beginTransaction();
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
            localdb.endTransaction();

        }
        return uuidList;

    }

    public boolean insertPatientProfileImages(String imagepath, String imageType, String patientUuid) throws DAOException {
        // todo: handle for Patient Detail download flow...
        boolean isInserted = false;
        if(imagepath == null)
            return true;
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        localdb.beginTransaction();
        ContentValues contentValues = new ContentValues();
        try {
            contentValues.put("uuid", UUID.randomUUID().toString());
            contentValues.put("patientuuid", patientUuid);
            contentValues.put("visituuid", "");
            contentValues.put("image_path", imagepath);
            contentValues.put("image_type", imageType);
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

    /**
     * This function is used on initial sync when user tries to open the patient for editing at that time
     * since this pat is of another user its images has to be downloaded and stored in local db table with sync = true.
     * @param imagepath
     * @param imageType
     * @param patientUuid
     * @return
     * @throws DAOException
     */
    public boolean pullSaveADPImages(String imagepath, String imageType, String patientUuid) throws DAOException {
        // todo: handle for Patient Detail download flow...
        boolean isInserted = false;
        if(imagepath == null)
            return true;
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        localdb.beginTransaction();
        ContentValues contentValues = new ContentValues();
        try {
            contentValues.put("uuid", UUID.randomUUID().toString());
            contentValues.put("patientuuid", patientUuid);
            contentValues.put("visituuid", "");
            contentValues.put("image_path", imagepath);
            contentValues.put("image_type", imageType);
            contentValues.put("obs_time_date", AppConstants.dateAndTimeUtils.currentDateTime());
            contentValues.put("sync", "true");  // sync set to true as this image is from another users device.
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

    public boolean updatePatientProfileImages(String imagepath, String patientuuid, String image_type) throws DAOException {
        boolean isUpdated = false;
        long isupdate = 0;
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        localdb.beginTransaction();
        ContentValues contentValues = new ContentValues();
        String whereclause = "patientuuid = ? AND image_path = ? AND image_type = ?";
        try {
            contentValues.put("patientuuid", patientuuid);
            contentValues.put("image_path", imagepath);
            contentValues.put("obs_time_date", AppConstants.dateAndTimeUtils.currentDateTime());
            contentValues.put("sync", "false");
            isupdate = localdb.update("tbl_image_records", contentValues, whereclause, new String[]{patientuuid, image_type});
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
            isUpdated = insertPatientProfileImages(imagepath, image_type, patientuuid);
        return isUpdated;
    }

    public List<PatientProfile> getPatientProfileUnsyncedImages() throws DAOException {
        List<PatientProfile> patientProfiles = new ArrayList<>();
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        Base64Utils base64Utils = new Base64Utils();
        localdb.beginTransaction();
        try {
            Cursor idCursor = localdb.rawQuery("SELECT * FROM tbl_image_records where sync = ? OR sync=? AND image_type = ? COLLATE NOCASE",
                    new String[]{"0", "false", "PP"});
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
            localdb.endTransaction();

        }

        return patientProfiles;
    }
    public List<ADPImageModel> getPatientADPUnsyncedImages() throws DAOException {
        List<ADPImageModel> adpImageModels = new ArrayList<>();
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        Base64Utils base64Utils = new Base64Utils();
        localdb.beginTransaction();
        try {
            Cursor idCursor = localdb.rawQuery("SELECT * FROM tbl_image_records where sync = ? OR sync=? AND image_type = ? COLLATE NOCASE",
                    new String[]{"0", "false", "ADP"});
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    ADPImageModel imageModel = new ADPImageModel();
                    imageModel.setPatientuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("patientuuid")));
                    imageModel.setFile(base64Utils.getBase64FromFileWithConversion
                            (idCursor.getString(idCursor.getColumnIndexOrThrow("image_path"))));
                    // Todo: Note: This converts the image to a Base64 image and pushes it.

                    imageModel.setFilePath(idCursor.getString(idCursor.getColumnIndexOrThrow("image_path"))); // String file path
                    String fileName = (idCursor.getString((idCursor.getColumnIndexOrThrow("image_path"))));
                    Log.v("ADPFile", "ADPFile: " + Files.getNameWithoutExtension(fileName));
                    imageModel.setfName(Files.getNameWithoutExtension(fileName));

                    adpImageModels.add(imageModel);

                   /* PatientAdditionalDocModel patientAdditionalDocModel = new PatientAdditionalDocModel();
                    patientAdditionalDocModel.setPatientuuid(idCursor.getString(idCursor.getColumnIndexOrThrow("patientuuid")));
                    patientAdditionalDocModel.setFile(base64Utils.getBase64FromFileWithConversion
                            (idCursor.getString(idCursor.getColumnIndexOrThrow("image_path"))));
                    // Todo: Note: This converts the image to a Base64 image and pushes it.

                    String fileName = (idCursor.getString((idCursor.getColumnIndexOrThrow("image_path"))));
                    Log.v("ADPFile", "ADPFile: " + Files.getNameWithoutExtension(fileName));
                    patientAdditionalDocModel.setF_name(Files.getNameWithoutExtension(fileName));
                    patientProfiles.add(patientAdditionalDocModel);*/
                }

            }
            idCursor.close();
        } catch (SQLiteException e) {
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();
        }

        return adpImageModels;
    }

    public List<ObsPushDTO> getObsUnsyncedImages() throws DAOException {
        List<ObsPushDTO> obsImages = new ArrayList<>();
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        localdb.beginTransaction();
        try {
            Cursor idCursor = localdb.rawQuery("select c.uuid as patientuuid,d.conceptuuid,a.uuid as encounteruuid,d.uuid as obsuuid,d.modified_date  from tbl_encounter a , tbl_visit b , tbl_patient c,tbl_obs d where a.visituuid=b.uuid and b.patientuuid=c.uuid and d.encounteruuid=a.uuid and (d.sync=0 or d.sync='false') and (d.conceptuuid=? or d.conceptuuid=?) and d.voided='0'", new String[]{UuidDictionary.COMPLEX_IMAGE_PE, UuidDictionary.COMPLEX_IMAGE_AD});
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    ObsPushDTO obsPushDTO = new ObsPushDTO();
                    obsPushDTO.setConcept(idCursor.getString(idCursor.getColumnIndexOrThrow("conceptuuid")));
                    obsPushDTO.setEncounter(idCursor.getString(idCursor.getColumnIndexOrThrow("encounteruuid")));
                    obsPushDTO.setObsDatetime(idCursor.getString(idCursor.getColumnIndexOrThrow("modified_date")));
                    obsPushDTO.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("obsuuid")));
                    obsPushDTO.setPerson(idCursor.getString(idCursor.getColumnIndexOrThrow("patientuuid")));
                    obsImages.add(obsPushDTO);
                }
            }
            idCursor.close();
        } catch (SQLiteException e) {
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();

        }

        return obsImages;

    }


    public String getPatientProfileChangeTime(String patientUuid) throws DAOException { // todo: handle same logic for ADP as well in PatientDetail screen.
        String datetime = "";
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        localdb.beginTransaction();
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
            localdb.endTransaction();

        }

        return datetime;
    }


    public boolean updateUnsyncedPatientProfile(String patientuuid, String type) throws DAOException {
        boolean isUpdated = false;
        long isupdate = 0;
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
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

    public boolean updateUnsyncedPatientADP(String patientuuid, String filePath, String type) throws DAOException {
        boolean isUpdated = false;
        long isupdate = 0;
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        localdb.beginTransaction();
        ContentValues contentValues = new ContentValues();
        String whereclause = "patientuuid = ? AND image_path = ? AND image_type = ?";
        try {
//            contentValues.put("patientuuid", patientuuid);
            contentValues.put("sync", "true");
            isupdate = localdb.update("tbl_image_records", contentValues, whereclause, new String[]{patientuuid, filePath, type});
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
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
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
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        localdb.beginTransaction();
        try {
            Cursor idCursor = localdb.rawQuery("SELECT uuid FROM tbl_obs where encounteruuid=? AND conceptuuid = ? AND voided=? COLLATE NOCASE",
                    new String[]{encounterUuid, conceptuuid, "0"});
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    uuidList.add(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                }
            }
            idCursor.close();
        } catch (SQLiteException e) {
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();

        }
        return uuidList;
    }


    public List<String> getImages(String encounterUUid, String ConceptUuid) throws DAOException {
        List<String> imagesList = new ArrayList<>();

        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        localdb.beginTransaction();
        try {
            Cursor idCursor = localdb.rawQuery("SELECT uuid FROM tbl_obs where encounteruuid=? AND conceptuuid = ? AND voided=? COLLATE NOCASE",
                    new String[]{encounterUUid, ConceptUuid, "0"});
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    imagesList.add(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                }
            }
            idCursor.close();
        } catch (SQLiteException e) {
            throw new DAOException(e);
        } finally {
            localdb.endTransaction();
        }
        return imagesList;
    }


    public List<String> isImageListObsExists(String encounterUuid, String conceptUuid) throws DAOException {
        List<String> imagesList = new ArrayList<>();
        SQLiteDatabase localdb = AppConstants.inteleHealthDatabaseHelper.getWriteDb();
        localdb.beginTransaction();
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
            localdb.endTransaction();
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



}

