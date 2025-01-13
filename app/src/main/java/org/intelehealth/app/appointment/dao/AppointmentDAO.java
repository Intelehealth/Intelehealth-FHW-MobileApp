package org.intelehealth.app.appointment.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import org.intelehealth.app.utilities.CustomLog;

import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.app.AppConstants;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.appointment.model.AppointmentInfo;
import org.intelehealth.app.appointment.model.BookAppointmentRequest;
import org.intelehealth.app.appointmentNew.MyAppointmentNew.PastAppointmentsFragment;
import org.intelehealth.app.database.dao.EncounterDAO;
import org.intelehealth.app.enums.AppointmentTabType;
import org.intelehealth.app.models.RescheduledAppointmentsModel;
import org.intelehealth.app.utilities.Base64Utils;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.Logger;
import org.intelehealth.app.utilities.UuidGenerator;
import org.intelehealth.app.utilities.exception.DAOException;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AppointmentDAO {

    private static final String TAG = AppointmentDAO.class.getSimpleName();
    private long createdRecordsCount = 0;


    public void insert(AppointmentInfo appointmentInfo) throws DAOException {

        AppointmentInfo checkAppointmentInfo = getAppointmentByVisitId(appointmentInfo.getVisitUuid());
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {

            ContentValues values = new ContentValues();
            values.put("uuid", checkAppointmentInfo != null ? checkAppointmentInfo.getUuid() : new UuidGenerator().UuidGenerator());
            values.put("appointment_id", appointmentInfo.getId());
            values.put("slot_day", appointmentInfo.getSlotDay());
            values.put("slot_date", appointmentInfo.getSlotDate());
            values.put("slot_js_date", appointmentInfo.getSlotJsDate());
            values.put("slot_duration", appointmentInfo.getSlotDuration());
            values.put("slot_duration_unit", appointmentInfo.getSlotDurationUnit());
            values.put("slot_time", appointmentInfo.getSlotTime());
            values.put("speciality", appointmentInfo.getSpeciality());
            values.put("user_uuid", appointmentInfo.getUserUuid());
            values.put("dr_name", appointmentInfo.getDrName());
            values.put("visit_uuid", appointmentInfo.getVisitUuid());
            values.put("patient_id", appointmentInfo.getPatientId());
            values.put("patient_name", appointmentInfo.getPatientName());
            values.put("open_mrs_id", appointmentInfo.getOpenMrsId());
            values.put("status", appointmentInfo.getStatus());
            values.put("created_at", appointmentInfo.getCreatedAt());

            if (appointmentInfo.getRescheduledAppointments() != null && appointmentInfo.getRescheduledAppointments().size() > 0) {
                int rescheduledSize = appointmentInfo.getRescheduledAppointments().size();
                RescheduledAppointmentsModel rescheduledAppointmentsModel = appointmentInfo.getRescheduledAppointments().get(rescheduledSize - 1);

                values.put("prev_slot_day", rescheduledAppointmentsModel.getSlotDay());
                values.put("prev_slot_date", rescheduledAppointmentsModel.getSlotDate());
                values.put("prev_slot_time", rescheduledAppointmentsModel.getSlotTime());

            }
            createdRecordsCount = db.insertWithOnConflict("tbl_appointments", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }
    }

    public void updateAppointmentSync(String uuid, String synced) throws DAOException {
        CustomLog.v(TAG, "updateAppointmentSync uuid- " + uuid + "\t synced - " + synced);
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        String whereClause = "visit_uuid=?";
        String[] whereArgs = {uuid};

        try {
            values.put("sync", synced);
            db.update("tbl_appointments", values, whereClause, whereArgs);
        } catch (SQLException exception) {
            throw new DAOException(exception.getMessage());
        }
    }

    public void insertAppointmentToDb(BookAppointmentRequest bookAppointmentRequest) throws DAOException {
        CustomLog.v(TAG, "insertAppointmentToDb bookAppointmentRequest - " + new Gson().toJson(bookAppointmentRequest));

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put("uuid", bookAppointmentRequest.getUuid());
            values.put("appointment_id", bookAppointmentRequest.getAppointmentId());
            values.put("slot_day", bookAppointmentRequest.getSlotDay());
            values.put("slot_date", bookAppointmentRequest.getSlotDate());
            values.put("slot_duration", bookAppointmentRequest.getSlotDuration());
            values.put("slot_duration_unit", bookAppointmentRequest.getSlotDurationUnit());
            values.put("slot_time", bookAppointmentRequest.getSlotTime());
            values.put("speciality", bookAppointmentRequest.getSpeciality());
            values.put("user_uuid", bookAppointmentRequest.getUserUuid());
            values.put("dr_name", bookAppointmentRequest.getDrName());
            values.put("visit_uuid", bookAppointmentRequest.getVisitUuid());
            values.put("patient_id", bookAppointmentRequest.getPatientId());
            values.put("patient_name", bookAppointmentRequest.getPatientName());
            values.put("open_mrs_id", bookAppointmentRequest.getOpenMrsId());
            values.put("location_uuid", bookAppointmentRequest.getLocationUuid());
            values.put("hw_uuid", bookAppointmentRequest.getHwUUID());
            values.put("reason", bookAppointmentRequest.getReason());
            values.put("created_at", AppConstants.dateAndTimeUtils.currentDateTime());
            values.put("voided", 0);
            values.put("sync", false);

            createdRecordsCount = db.insertWithOnConflict("tbl_appointments", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLException exception) {
            exception.printStackTrace();
            throw new DAOException(exception.getMessage(), exception);
        }
    }

    public AppointmentInfo getAppointmentByVisitId(String visitUUID) {
        CustomLog.v(TAG, "getByVisitUUID - visitUUID - " + visitUUID);

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        //db.beginTransaction();
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_appointments where visit_uuid = ?", new String[]{visitUUID});
        AppointmentInfo appointmentInfo = null;
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                appointmentInfo = new AppointmentInfo();
                appointmentInfo.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                appointmentInfo.setId(idCursor.getInt(idCursor.getColumnIndexOrThrow("appointment_id")));
                appointmentInfo.setSlotDay(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_day")));
                appointmentInfo.setSlotDate(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_date")));
                appointmentInfo.setSlotDuration(idCursor.getInt(idCursor.getColumnIndexOrThrow("slot_duration")));
                appointmentInfo.setSlotDurationUnit(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_duration_unit")));
                appointmentInfo.setSlotTime(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_time")));
                appointmentInfo.setSpeciality(idCursor.getString(idCursor.getColumnIndexOrThrow("speciality")));
                appointmentInfo.setUserUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("user_uuid")));
                appointmentInfo.setDrName(idCursor.getString(idCursor.getColumnIndexOrThrow("dr_name")));
                appointmentInfo.setVisitUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")));
                appointmentInfo.setPatientId(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_id")));
                appointmentInfo.setPatientName(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_name")));
                appointmentInfo.setOpenMrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("open_mrs_id")));
                appointmentInfo.setStatus(idCursor.getString(idCursor.getColumnIndexOrThrow("status")));
                appointmentInfo.setCreatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("created_at")));
                appointmentInfo.setUpdatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("updated_at")));
            }

        }
        idCursor.close();
        //db.setTransactionSuccessful();
        //db.endTransaction();
        //db.close();

        return appointmentInfo;
    }

    public void deleteAppointmentByVisitId(String visitUuid) {
        CustomLog.v(TAG, "deleteAppointmentByVisitId - visitUUID - " + visitUuid);
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        db.delete("tbl_appointments", "visit_uuid=?", new String[]{visitUuid});
        db.setTransactionSuccessful();
        db.endTransaction();
        //db.close();
    }

    public void deleteAllAppointments() {
        CustomLog.v(TAG, "deleteAllAppointments ");
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        try{
        db.beginTransaction();
        db.delete("tbl_appointments", null, null);
        db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
        }
        //db.close();
    }


    public List<AppointmentInfo> getAppointments() {
        List<AppointmentInfo> appointmentInfos = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_appointments", new String[]{});
        EncounterDAO encounterDAO = new EncounterDAO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                AppointmentInfo appointmentInfo = new AppointmentInfo();
                appointmentInfo.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                appointmentInfo.setId(idCursor.getInt(idCursor.getColumnIndexOrThrow("appointment_id")));
                appointmentInfo.setSlotDay(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_day")));
                appointmentInfo.setSlotDate(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_date")));
                appointmentInfo.setSlotDuration(idCursor.getInt(idCursor.getColumnIndexOrThrow("slot_duration")));
                appointmentInfo.setSlotDurationUnit(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_duration_unit")));
                appointmentInfo.setSlotTime(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_time")));
                appointmentInfo.setSpeciality(idCursor.getString(idCursor.getColumnIndexOrThrow("speciality")));
                appointmentInfo.setUserUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("user_uuid")));
                appointmentInfo.setDrName(idCursor.getString(idCursor.getColumnIndexOrThrow("dr_name")));
                appointmentInfo.setVisitUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")));
                appointmentInfo.setPatientId(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_id")));
                appointmentInfo.setPatientName(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_name")));
                appointmentInfo.setOpenMrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("open_mrs_id")));
                try {
                    if (!encounterDAO.isCompletedOrExited(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")))) {
                        appointmentInfo.setStatus(idCursor.getString(idCursor.getColumnIndexOrThrow("status")));
                    } else {
                        appointmentInfo.setStatus(IntelehealthApplication.getAppContext().getString(R.string.visit_closed));
                    }
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                appointmentInfo.setCreatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("created_at")));
                appointmentInfo.setUpdatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("updated_at")));
                appointmentInfos.add(appointmentInfo);
            }
        }
        idCursor.close();
        return appointmentInfos;
    }

    private String getDateInFormat(String slotDate) {
        String finalString = "";
        if (slotDate != null && !slotDate.trim().equalsIgnoreCase("")) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            Date date = null;
            try {
                date = (Date) formatter.parse(slotDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd");
            finalString = newFormat.format(date);
        }
        return finalString;
    }

    public List<AppointmentInfo> getAppointmentsWithFilters(String fromDate, String toDate, String searchPatientText) {
        fromDate = getDateInFormat(fromDate);
        toDate = getDateInFormat(toDate);
        String search = searchPatientText.trim().replaceAll("\\s", "");
        List<AppointmentInfo> appointmentInfos = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        Cursor idCursor;

        if (!fromDate.isEmpty() && !toDate.isEmpty() && !searchPatientText.isEmpty()) {

            String selectQuery = "SELECT p.patient_photo,p.first_name || ' ' || p.last_name AS patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid, a.appointment_id, "
                    + "a.slot_date, substr(a.slot_js_date, 1, 10) AS new_slot_date, a.slot_day, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, a.user_uuid, a.dr_name,"
                    + " a.visit_uuid, a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id  "
                    + "FROM tbl_patient p, tbl_appointments a "
                    + "WHERE p.uuid = a.patient_id "
                    + "AND patient_name_new LIKE " + "'%" + search + "%'  "
                    + "AND new_slot_date BETWEEN '" + fromDate + "' AND '" + toDate + "' "
                    + "AND a.status = 'booked'"
                    + "AND datetime(a.slot_js_date) >= datetime('now')";

            idCursor = db.rawQuery(selectQuery, new String[]{});
        } else if (!fromDate.isEmpty() && !toDate.isEmpty()) {
            String selectQuery = "select p.patient_photo, p.first_name || ' ' || p.last_name as patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid, a.appointment_id,"
                    + "a.slot_date, substr(a.slot_js_date, 1, 10) as new_slot_date, a.slot_day, a.slot_js_date, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, "
                    + "a.user_uuid, a.dr_name, a.visit_uuid, a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id "
                    + "from tbl_patient p, tbl_appointments a "
                    + "where p.uuid = a.patient_id and new_slot_date "
                    + "BETWEEN '" + fromDate + "'  and '" + toDate + "' "
                    + "AND a.status = 'booked'"
                    + "AND datetime(a.slot_js_date) >= datetime('now')";

            idCursor = db.rawQuery(selectQuery, null);
        } else if (!searchPatientText.isEmpty()) {
            String selectQuery = "select p.patient_photo, p.first_name || ' ' || p.last_name as patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid,"
                    + " a.appointment_id,a.slot_date, a.slot_day, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, a.user_uuid, a.dr_name, a.visit_uuid, "
                    + "a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id "
                    + "from tbl_patient p, tbl_appointments a "
                    + "where p.uuid = a.patient_id "
                    + "and patient_name_new LIKE " + "'%" + search + "%' "
                    + "AND a.status = 'booked'"
                    + "AND datetime(a.slot_js_date) >= datetime('now')";

            idCursor = db.rawQuery(selectQuery, new String[]{});
        } else {
            idCursor = db.rawQuery("select p.patient_photo, p.first_name || ' ' || p.last_name as patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid, "
                            + "a.appointment_id,a.slot_date, a.slot_day, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, a.user_uuid, a.dr_name, a.visit_uuid, "
                            + "a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id "
                            + "from tbl_patient p, tbl_appointments a "
                            + "where p.uuid = a.patient_id "
                            + "AND a.status = 'booked'"
                            + "AND datetime(a.slot_js_date) >= datetime('now')"
                    , new String[]{});

        }
        EncounterDAO encounterDAO = new EncounterDAO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                AppointmentInfo appointmentInfo = new AppointmentInfo();
                appointmentInfo.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                appointmentInfo.setId(idCursor.getInt(idCursor.getColumnIndexOrThrow("appointment_id")));
                appointmentInfo.setSlotDay(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_day")));
                appointmentInfo.setSlotDate(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_date")));
                appointmentInfo.setSlotDuration(idCursor.getInt(idCursor.getColumnIndexOrThrow("slot_duration")));
                appointmentInfo.setSlotDurationUnit(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_duration_unit")));
                appointmentInfo.setSlotTime(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_time")));
                appointmentInfo.setSpeciality(idCursor.getString(idCursor.getColumnIndexOrThrow("speciality")));
                appointmentInfo.setUserUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("user_uuid")));
                appointmentInfo.setDrName(idCursor.getString(idCursor.getColumnIndexOrThrow("dr_name")));
                appointmentInfo.setVisitUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")));
                appointmentInfo.setPatientId(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_id")));
                appointmentInfo.setPatientName(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_name_new")));
                appointmentInfo.setOpenMrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("open_mrs_id")));
                appointmentInfo.setPatientDob(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                appointmentInfo.setPatientGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                try {
                    if (!encounterDAO.isCompletedOrExited(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")))) {
                        appointmentInfo.setStatus(idCursor.getString(idCursor.getColumnIndexOrThrow("status")));
                    } else {
                        appointmentInfo.setStatus(IntelehealthApplication.getAppContext().getString(R.string.visit_closed));
                    }
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                appointmentInfo.setCreatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("created_at")));
                appointmentInfo.setUpdatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("updated_at")));
                appointmentInfos.add(appointmentInfo);
            }
        }
        idCursor.close();
        return appointmentInfos;
    }

    public List<AppointmentInfo> getUpcomingAppointmentsWithFilters(String fromDate, String toDate, int limit, int offset, String currentDate) {
        fromDate = getDateInFormat(fromDate);
        toDate = getDateInFormat(toDate);
        List<AppointmentInfo> appointmentInfos = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        Cursor idCursor;

        if (!fromDate.isEmpty() && !toDate.isEmpty()) {
            String selectQuery = "select p.patient_photo, p.first_name || ' ' || p.last_name as patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid, a.appointment_id,"
                    + "a.slot_date, substr(a.slot_js_date, 1, 10) as new_slot_date, a.slot_day, a.slot_js_date, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, "
                    + "a.user_uuid, a.dr_name, a.visit_uuid, a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id "
                    + "from tbl_patient p, tbl_appointments a "
                    + "where p.uuid = a.patient_id and new_slot_date "
                    + "BETWEEN '" + fromDate + "'  and '" + toDate + "' "
                    + "AND a.status = 'booked'"
                    + "AND datetime(a.slot_js_date) >= datetime('now') "
                    + "LIMIT ? OFFSET ?";

            idCursor = db.rawQuery(selectQuery, new String[]{String.valueOf(limit), String.valueOf(offset)});
        } else {
            idCursor = db.rawQuery("select p.patient_photo, p.first_name || ' ' || p.last_name as patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid, "
                            + "a.appointment_id,a.slot_date, a.slot_day, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, a.user_uuid, a.dr_name, a.visit_uuid, "
                            + "a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id "
                            + "from tbl_patient p, tbl_appointments a "
                            + "where p.uuid = a.patient_id "
                            + "AND a.status = 'booked'"
                            + "AND a.slot_date != ?"
                            + "AND datetime(a.slot_js_date) >= datetime('now') "
                            + "LIMIT ? OFFSET ?"
                    , new String[]{currentDate, String.valueOf(limit), String.valueOf(offset)});
        }
        EncounterDAO encounterDAO = new EncounterDAO();

        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                AppointmentInfo appointmentInfo = new AppointmentInfo();
                appointmentInfo.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                appointmentInfo.setId(idCursor.getInt(idCursor.getColumnIndexOrThrow("appointment_id")));
                appointmentInfo.setSlotDay(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_day")));
                appointmentInfo.setSlotDate(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_date")));
                appointmentInfo.setSlotDuration(idCursor.getInt(idCursor.getColumnIndexOrThrow("slot_duration")));
                appointmentInfo.setSlotDurationUnit(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_duration_unit")));
                appointmentInfo.setSlotTime(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_time")));
                appointmentInfo.setSpeciality(idCursor.getString(idCursor.getColumnIndexOrThrow("speciality")));
                appointmentInfo.setUserUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("user_uuid")));
                appointmentInfo.setDrName(idCursor.getString(idCursor.getColumnIndexOrThrow("dr_name")));
                appointmentInfo.setVisitUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")));
                appointmentInfo.setPatientId(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_id")));
                appointmentInfo.setPatientName(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_name_new")));
                appointmentInfo.setOpenMrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("open_mrs_id")));
                appointmentInfo.setPatientDob(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                appointmentInfo.setPatientGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                String status = idCursor.getString(idCursor.getColumnIndexOrThrow("status"));

                try {
                    if (!encounterDAO.isCompletedOrExited(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")))) {
                        appointmentInfo.setStatus(idCursor.getString(idCursor.getColumnIndexOrThrow("status")));
                    } else if (status.equalsIgnoreCase("cancelled")) {
                        appointmentInfo.setStatus(status);
                    } else {
                        appointmentInfo.setStatus(IntelehealthApplication.getAppContext().getString(R.string.visit_closed));
                    }
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                appointmentInfo.setCreatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("created_at")));
                appointmentInfo.setUpdatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("updated_at")));
                appointmentInfos.add(appointmentInfo);
            }

        }

        idCursor.close();
        return appointmentInfos;
    }

    public @Nullable List<AppointmentInfo> getPastAppointmentsWithFilters(Context context, int limit, int offset, @Nullable String orderType, @Nullable String query, @Nullable List<PastAppointmentsFragment.FilterOptionsModel> filtersList) {
        List<AppointmentInfo> appointmentInfos = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        Cursor idCursor;

        String searchQuery = "";
        StringBuilder filterQuery = new StringBuilder();
        String middleName = "CASE WHEN p.middle_name IS NOT NULL THEN ' ' || p.middle_name || ' ' ELSE ' ' END";

        if (query != null && !query.isEmpty()) {
            searchQuery = " AND ((patient_name_new LIKE " + "'%" + query + "%') OR (p.openmrs_id LIKE " + "'%" + query + "%'))";
        }


        if (filtersList != null && filtersList.size() > 0) {
            String andOr = "";

            for (PastAppointmentsFragment.FilterOptionsModel item : filtersList) {
                if (filterQuery.length() > 0) {
                    andOr = " or ";
                }
                if (item.getFilterValue().equals(context.getString(R.string.completed))) {
                    filterQuery.append(andOr).append("((").append("datetime(a.slot_js_date) < datetime('now')").append(" and ").append("a.status = 'completed'))");
                }
                if (item.getFilterValue().equals(context.getString(R.string.cancelled))) {
                    filterQuery.append(andOr).append("((").append("datetime(a.slot_js_date) < datetime('now')").append(" and ").append("a.status = 'cancelled'))");
                }
                if (item.getFilterValue().equals(context.getString(R.string.missed))) {
                    filterQuery.append(andOr).append("((").append("datetime(a.slot_js_date) < datetime('now')").append(" and ").append("a.status = 'booked'))");
                }
            }
        } else {
            filterQuery.append("(").append("datetime(a.slot_js_date) < datetime('now'))");
        }

        idCursor = db.rawQuery("select p.patient_photo, p.first_name || " + middleName + " || p.last_name as patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid, "
                        + "a.appointment_id,a.slot_date, a.slot_day, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, a.user_uuid, a.dr_name, a.visit_uuid, "
                        + "a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id "
                        + "from tbl_patient p, tbl_appointments a "
                        + "where p.uuid = a.patient_id "
                        + "AND (" + filterQuery + ")"
                        + searchQuery
                        + "ORDER BY datetime(a.slot_js_date) " + orderType
                        + " LIMIT ? OFFSET ?"
                , new String[]{String.valueOf(limit), String.valueOf(offset)});

        EncounterDAO encounterDAO = new EncounterDAO();

        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                AppointmentInfo appointmentInfo = new AppointmentInfo();
                appointmentInfo.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                appointmentInfo.setId(idCursor.getInt(idCursor.getColumnIndexOrThrow("appointment_id")));
                appointmentInfo.setSlotDay(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_day")));
                appointmentInfo.setSlotDate(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_date")));
                appointmentInfo.setSlotDuration(idCursor.getInt(idCursor.getColumnIndexOrThrow("slot_duration")));
                appointmentInfo.setSlotDurationUnit(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_duration_unit")));
                appointmentInfo.setSlotTime(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_time")));
                appointmentInfo.setSpeciality(idCursor.getString(idCursor.getColumnIndexOrThrow("speciality")));
                appointmentInfo.setUserUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("user_uuid")));
                appointmentInfo.setDrName(idCursor.getString(idCursor.getColumnIndexOrThrow("dr_name")));
                appointmentInfo.setVisitUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")));
                appointmentInfo.setPatientId(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_id")));
                appointmentInfo.setPatientName(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_name_new")));
                appointmentInfo.setOpenMrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("open_mrs_id")));
                appointmentInfo.setPatientDob(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                appointmentInfo.setPatientGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                appointmentInfo.setStatus(idCursor.getString(idCursor.getColumnIndexOrThrow("status")));

                appointmentInfo.setCreatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("created_at")));
                appointmentInfo.setUpdatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("updated_at")));
                appointmentInfos.add(appointmentInfo);
            }

        }

        idCursor.close();
        return appointmentInfos;
    }

    public List<AppointmentInfo> getAllUpcomingAppointmentsWithFilters(String fromDate, String toDate) {
        fromDate = getDateInFormat(fromDate);
        toDate = getDateInFormat(toDate);
        List<AppointmentInfo> appointmentInfos = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        Cursor idCursor;

        if (!fromDate.isEmpty() && !toDate.isEmpty()) {
            String selectQuery = "select p.patient_photo, p.first_name || ' ' || p.last_name as patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid, a.appointment_id,"
                    + "a.slot_date, substr(a.slot_js_date, 1, 10) as new_slot_date, a.slot_day, a.slot_js_date, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, "
                    + "a.user_uuid, a.dr_name, a.visit_uuid, a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id "
                    + "from tbl_patient p, tbl_appointments a "
                    + "where p.uuid = a.patient_id and new_slot_date "
                    + "BETWEEN '" + fromDate + "'  and '" + toDate + "' "
                    + "AND a.status = 'booked'"
                    + "AND datetime(a.slot_js_date) >= datetime('now') ";

            idCursor = db.rawQuery(selectQuery, null);
        } else {
            idCursor = db.rawQuery("select p.patient_photo, p.first_name || ' ' || p.last_name as patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid, "
                            + "a.appointment_id,a.slot_date, a.slot_day, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, a.user_uuid, a.dr_name, a.visit_uuid, "
                            + "a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id "
                            + "from tbl_patient p, tbl_appointments a "
                            + "where p.uuid = a.patient_id "
                            + "AND a.status = 'booked'"
                            + "AND datetime(a.slot_js_date) >= datetime('now') "
                    , null);
        }

        EncounterDAO encounterDAO = new EncounterDAO();

        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                AppointmentInfo appointmentInfo = new AppointmentInfo();
                appointmentInfo.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                appointmentInfo.setId(idCursor.getInt(idCursor.getColumnIndexOrThrow("appointment_id")));
                appointmentInfo.setSlotDay(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_day")));
                appointmentInfo.setSlotDate(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_date")));
                appointmentInfo.setSlotDuration(idCursor.getInt(idCursor.getColumnIndexOrThrow("slot_duration")));
                appointmentInfo.setSlotDurationUnit(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_duration_unit")));
                appointmentInfo.setSlotTime(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_time")));
                appointmentInfo.setSpeciality(idCursor.getString(idCursor.getColumnIndexOrThrow("speciality")));
                appointmentInfo.setUserUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("user_uuid")));
                appointmentInfo.setDrName(idCursor.getString(idCursor.getColumnIndexOrThrow("dr_name")));
                appointmentInfo.setVisitUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")));
                appointmentInfo.setPatientId(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_id")));
                appointmentInfo.setPatientName(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_name_new")));
                appointmentInfo.setOpenMrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("open_mrs_id")));
                appointmentInfo.setPatientDob(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                appointmentInfo.setPatientGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                try {
                    if (!encounterDAO.isCompletedOrExited(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")))) {
                        appointmentInfo.setStatus(idCursor.getString(idCursor.getColumnIndexOrThrow("status")));
                    } else {
                        appointmentInfo.setStatus(IntelehealthApplication.getAppContext().getString(R.string.visit_closed));
                    }
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                appointmentInfo.setCreatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("created_at")));
                appointmentInfo.setUpdatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("updated_at")));
                appointmentInfos.add(appointmentInfo);
            }

        }

        idCursor.close();
        return appointmentInfos;
    }

    /**
     * getting upcoming and past appointment count
     *
     * @param isUpcoming
     * @return
     */
    int appointmentFunCount = 0;

    public int getAppointmentCountsByStatus(AppointmentTabType appointmentTabType) {
        int count = -1;
        if (appointmentFunCount > 5) return 0;

        try {
            SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getReadableDatabase();
            db.beginTransactionNonExclusive();

            Cursor cursor = null;
            String query = "";
            if (appointmentTabType == AppointmentTabType.UPCOMING) {
                query = "SELECT count(*) "
                        + "from tbl_patient p, tbl_appointments a "
                        + "where p.uuid = a.patient_id "
                        + "AND a.status = 'booked' "
                        + "AND  (datetime(a.slot_js_date) >= datetime('now'))";
                cursor = db.rawQuery(query, new String[]{});
            } else if (appointmentTabType == AppointmentTabType.PAST) {
                query = "select count(*) "
                        + "from tbl_patient p, tbl_appointments a "
                        + "where p.uuid = a.patient_id "
                        + "and (datetime(a.slot_js_date) < datetime('now'))";
                cursor = db.rawQuery(query, new String[]{});
            }
            db.setTransactionSuccessful();
            db.endTransaction();

            if (cursor != null) {
                cursor.moveToFirst();
                count = cursor.getInt(0);
                cursor.close();
            }
        } catch (Exception e) {
            try {
                Thread.sleep(1000);
                appointmentFunCount++;
                getAppointmentCountsByStatus(appointmentTabType);
            } catch (Exception ignored) {
                CustomLog.d("CCCCCC", "" + ignored.getMessage());
            }
            CustomLog.d("CCCCCC", "" + e.getMessage());
        }

        CustomLog.d("CCCCCC", "" + count);

        return count;
    }

    public List<AppointmentInfo> getCompletedAppointmentsWithFilters(String fromDate, String toDate, String searchPatientText) {
        fromDate = getDateInFormat(fromDate);
        toDate = getDateInFormat(toDate);
        String search = searchPatientText.trim().replaceAll("\\s", "");
        List<AppointmentInfo> appointmentInfos = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        Cursor idCursor;

        if (!fromDate.isEmpty() && !toDate.isEmpty() && !searchPatientText.isEmpty()) {

            String selectQuery = "SELECT p.patient_photo,p.first_name || ' ' || p.last_name AS patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid, a.appointment_id, "
                    + "a.slot_date, substr(a.slot_js_date, 1, 10) AS new_slot_date, a.slot_day, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, a.user_uuid, a.dr_name,"
                    + " a.visit_uuid, a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id  "
                    + "FROM tbl_patient p, tbl_appointments a "
                    + "WHERE p.uuid = a.patient_id "
                    + "AND patient_name_new LIKE " + "'%" + search + "%'  "
                    + "AND new_slot_date BETWEEN '" + fromDate + "' AND '" + toDate + "' "
                    + "AND a.status = 'booked'"
                    + "AND datetime(a.slot_js_date) < datetime('now')";

            idCursor = db.rawQuery(selectQuery, new String[]{});
        } else if (!fromDate.isEmpty() && !toDate.isEmpty()) {
            String selectQuery = "select p.patient_photo, p.first_name || ' ' || p.last_name as patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid, a.appointment_id,"
                    + "a.slot_date, substr(a.slot_js_date, 1, 10) as new_slot_date, a.slot_day, a.slot_js_date, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, "
                    + "a.user_uuid, a.dr_name, a.visit_uuid, a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id "
                    + "from tbl_patient p, tbl_appointments a "
                    + "where p.uuid = a.patient_id and new_slot_date "
                    + "BETWEEN '" + fromDate + "'  and '" + toDate + "' "
                    + "AND a.status = 'booked'"
                    + "AND datetime(a.slot_js_date) < datetime('now')";

            idCursor = db.rawQuery(selectQuery, null);
        } else if (!searchPatientText.isEmpty()) {
            String selectQuery = "select p.patient_photo, p.first_name || ' ' || p.last_name as patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid,"
                    + " a.appointment_id,a.slot_date, a.slot_day, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, a.user_uuid, a.dr_name, a.visit_uuid, "
                    + "a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id "
                    + "from tbl_patient p, tbl_appointments a "
                    + "where p.uuid = a.patient_id "
                    + "and patient_name_new LIKE " + "'%" + search + "%' "
                    + "AND a.status = 'booked'"
                    + "AND datetime(a.slot_js_date) < datetime('now')";

            idCursor = db.rawQuery(selectQuery, new String[]{});
        } else {
            idCursor = db.rawQuery("select p.patient_photo, p.first_name || ' ' || p.last_name as patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid, "
                            + "a.appointment_id,a.slot_date, a.slot_day, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, a.user_uuid, a.dr_name, a.visit_uuid, "
                            + "a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id "
                            + "from tbl_patient p, tbl_appointments a "
                            + "where p.uuid = a.patient_id "
                            + "AND a.status = 'booked'"
                            + "AND datetime(a.slot_js_date) < datetime('now')"
                    , new String[]{});

        }
        EncounterDAO encounterDAO = new EncounterDAO();

        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                AppointmentInfo appointmentInfo = new AppointmentInfo();
                appointmentInfo.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                appointmentInfo.setId(idCursor.getInt(idCursor.getColumnIndexOrThrow("appointment_id")));
                appointmentInfo.setSlotDay(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_day")));
                appointmentInfo.setSlotDate(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_date")));
                appointmentInfo.setSlotDuration(idCursor.getInt(idCursor.getColumnIndexOrThrow("slot_duration")));
                appointmentInfo.setSlotDurationUnit(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_duration_unit")));
                appointmentInfo.setSlotTime(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_time")));
                appointmentInfo.setSpeciality(idCursor.getString(idCursor.getColumnIndexOrThrow("speciality")));
                appointmentInfo.setUserUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("user_uuid")));
                appointmentInfo.setDrName(idCursor.getString(idCursor.getColumnIndexOrThrow("dr_name")));
                appointmentInfo.setVisitUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")));
                appointmentInfo.setPatientId(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_id")));
                appointmentInfo.setPatientName(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_name_new")));
                appointmentInfo.setOpenMrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("open_mrs_id")));
                appointmentInfo.setPatientDob(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                appointmentInfo.setPatientGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                try {
                    if (!encounterDAO.isCompletedOrExited(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")))) {
                        appointmentInfo.setStatus(idCursor.getString(idCursor.getColumnIndexOrThrow("status")));
                    } else {
                        appointmentInfo.setStatus(IntelehealthApplication.getAppContext().getString(R.string.visit_closed));
                    }
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                appointmentInfo.setCreatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("created_at")));
                appointmentInfo.setUpdatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("updated_at")));
                appointmentInfos.add(appointmentInfo);
            }

        }
        idCursor.close();
        //db.setTransactionSuccessful();
        //db.endTransaction();
        //db.close();

        return appointmentInfos;
    }

    public List<AppointmentInfo> getCompletedAppointmentsWithFilters(String fromDate, String toDate, int limit, int offset, String currentDate) {
        fromDate = getDateInFormat(fromDate);
        toDate = getDateInFormat(toDate);
        List<AppointmentInfo> appointmentInfos = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        Cursor idCursor;

        if (!fromDate.isEmpty() && !toDate.isEmpty()) {
            String selectQuery = "select p.patient_photo, p.first_name || ' ' || p.last_name as patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid, a.appointment_id,"
                    + "a.slot_date, substr(a.slot_js_date, 1, 10) as new_slot_date, a.slot_day, a.slot_js_date, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, "
                    + "a.user_uuid, a.dr_name, a.visit_uuid, a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id "
                    + "from tbl_patient p, tbl_appointments a "
                    + "where p.uuid = a.patient_id and new_slot_date "
                    + "BETWEEN '" + fromDate + "'  and '" + toDate + "' "
                    + "AND a.status = 'booked'"
                    + "AND datetime(a.slot_js_date) < datetime('now') "
                    + "LIMIT ? OFFSET ?";

            idCursor = db.rawQuery(selectQuery, new String[]{String.valueOf(limit), String.valueOf(offset)});
        } else {
            idCursor = db.rawQuery("select p.patient_photo, p.first_name || ' ' || p.last_name as patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid, "
                            + "a.appointment_id,a.slot_date, a.slot_day, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, a.user_uuid, a.dr_name, a.visit_uuid, "
                            + "a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id "
                            + "from tbl_patient p, tbl_appointments a "
                            + "where p.uuid = a.patient_id "
                            + "AND a.slot_date != ?"
                            + "AND a.status = 'booked'"
                            + "AND datetime(a.slot_js_date) < datetime('now') "
                            + "LIMIT ? OFFSET ?"
                    , new String[]{currentDate, String.valueOf(limit), String.valueOf(offset)});

        }
        EncounterDAO encounterDAO = new EncounterDAO();

        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                AppointmentInfo appointmentInfo = new AppointmentInfo();
                appointmentInfo.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                appointmentInfo.setId(idCursor.getInt(idCursor.getColumnIndexOrThrow("appointment_id")));
                appointmentInfo.setSlotDay(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_day")));
                appointmentInfo.setSlotDate(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_date")));
                appointmentInfo.setSlotDuration(idCursor.getInt(idCursor.getColumnIndexOrThrow("slot_duration")));
                appointmentInfo.setSlotDurationUnit(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_duration_unit")));
                appointmentInfo.setSlotTime(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_time")));
                appointmentInfo.setSpeciality(idCursor.getString(idCursor.getColumnIndexOrThrow("speciality")));
                appointmentInfo.setUserUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("user_uuid")));
                appointmentInfo.setDrName(idCursor.getString(idCursor.getColumnIndexOrThrow("dr_name")));
                appointmentInfo.setVisitUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")));
                appointmentInfo.setPatientId(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_id")));
                appointmentInfo.setPatientName(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_name_new")));
                appointmentInfo.setOpenMrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("open_mrs_id")));
                appointmentInfo.setPatientDob(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                appointmentInfo.setPatientGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                try {
                    if (!encounterDAO.isCompletedOrExited(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")))) {
                        appointmentInfo.setStatus(idCursor.getString(idCursor.getColumnIndexOrThrow("status")));
                    } else {
                        appointmentInfo.setStatus(IntelehealthApplication.getAppContext().getString(R.string.visit_closed));
                    }
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                appointmentInfo.setCreatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("created_at")));
                appointmentInfo.setUpdatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("updated_at")));
                appointmentInfos.add(appointmentInfo);
            }

        }
        idCursor.close();
        //db.setTransactionSuccessful();
        //db.endTransaction();
        //db.close();

        return appointmentInfos;
    }

    public List<AppointmentInfo> getAllCompletedAppointmentsWithFilters(String fromDate, String toDate) {
        fromDate = getDateInFormat(fromDate);
        toDate = getDateInFormat(toDate);
        List<AppointmentInfo> appointmentInfos = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        Cursor idCursor;

        if (!fromDate.isEmpty() && !toDate.isEmpty()) {
            String selectQuery = "select p.patient_photo, p.first_name || ' ' || p.last_name as patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid, a.appointment_id,"
                    + "a.slot_date, substr(a.slot_js_date, 1, 10) as new_slot_date, a.slot_day, a.slot_js_date, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, "
                    + "a.user_uuid, a.dr_name, a.visit_uuid, a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id "
                    + "from tbl_patient p, tbl_appointments a "
                    + "where p.uuid = a.patient_id and new_slot_date "
                    + "BETWEEN '" + fromDate + "'  and '" + toDate + "' "
                    + "AND a.status = 'booked'"
                    + "AND datetime(a.slot_js_date) < datetime('now') ";

            idCursor = db.rawQuery(selectQuery, null);
        } else {
            idCursor = db.rawQuery("select p.patient_photo, p.first_name || ' ' || p.last_name as patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid, "
                            + "a.appointment_id,a.slot_date, a.slot_day, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, a.user_uuid, a.dr_name, a.visit_uuid, "
                            + "a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id "
                            + "from tbl_patient p, tbl_appointments a "
                            + "where p.uuid = a.patient_id "
                            + "AND a.status = 'booked'"
                            + "AND datetime(a.slot_js_date) < datetime('now') "
                            + "LIMIT ? OFFSET ?"
                    , null);

        }
        EncounterDAO encounterDAO = new EncounterDAO();

        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                AppointmentInfo appointmentInfo = new AppointmentInfo();
                appointmentInfo.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                appointmentInfo.setId(idCursor.getInt(idCursor.getColumnIndexOrThrow("appointment_id")));
                appointmentInfo.setSlotDay(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_day")));
                appointmentInfo.setSlotDate(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_date")));
                appointmentInfo.setSlotDuration(idCursor.getInt(idCursor.getColumnIndexOrThrow("slot_duration")));
                appointmentInfo.setSlotDurationUnit(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_duration_unit")));
                appointmentInfo.setSlotTime(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_time")));
                appointmentInfo.setSpeciality(idCursor.getString(idCursor.getColumnIndexOrThrow("speciality")));
                appointmentInfo.setUserUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("user_uuid")));
                appointmentInfo.setDrName(idCursor.getString(idCursor.getColumnIndexOrThrow("dr_name")));
                appointmentInfo.setVisitUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")));
                appointmentInfo.setPatientId(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_id")));
                appointmentInfo.setPatientName(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_name_new")));
                appointmentInfo.setOpenMrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("open_mrs_id")));
                appointmentInfo.setPatientDob(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                appointmentInfo.setPatientGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));

                String status = idCursor.getString(idCursor.getColumnIndexOrThrow("status"));
                try {
                    if (!encounterDAO.isCompletedOrExited(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")))) {
                        appointmentInfo.setStatus(status);
                    } else if (status.equalsIgnoreCase("cancelled")) {
                        appointmentInfo.setStatus(status);
                    } else {
                        appointmentInfo.setStatus(IntelehealthApplication.getAppContext().getString(R.string.visit_closed));
                    }
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                appointmentInfo.setCreatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("created_at")));
                appointmentInfo.setUpdatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("updated_at")));
                appointmentInfos.add(appointmentInfo);
            }

        }
        idCursor.close();
        //db.setTransactionSuccessful();
        //db.endTransaction();
        //db.close();

        return appointmentInfos;
    }

    public List<AppointmentInfo> getAppointmentsWithFiltersV1(String fromDate, String toDate, String searchPatientText) {
        List<AppointmentInfo> appointmentInfos = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        Cursor idCursor = db.rawQuery("select p.patient_photo, p.first_name || ' ' || p.last_name as patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid, a.appointment_id,a.slot_date, a.slot_day, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, a.user_uuid, a.dr_name, a.visit_uuid, a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id from tbl_patient p, tbl_appointments a where p.uuid = a.patient_id", new String[]{});
        EncounterDAO encounterDAO = new EncounterDAO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                AppointmentInfo appointmentInfo = new AppointmentInfo();
                appointmentInfo.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                appointmentInfo.setId(idCursor.getInt(idCursor.getColumnIndexOrThrow("appointment_id")));
                appointmentInfo.setSlotDay(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_day")));
                appointmentInfo.setSlotDate(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_date")));
                appointmentInfo.setSlotDuration(idCursor.getInt(idCursor.getColumnIndexOrThrow("slot_duration")));
                appointmentInfo.setSlotDurationUnit(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_duration_unit")));
                appointmentInfo.setSlotTime(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_time")));
                appointmentInfo.setSpeciality(idCursor.getString(idCursor.getColumnIndexOrThrow("speciality")));
                appointmentInfo.setUserUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("user_uuid")));
                appointmentInfo.setDrName(idCursor.getString(idCursor.getColumnIndexOrThrow("dr_name")));
                appointmentInfo.setVisitUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")));
                appointmentInfo.setPatientId(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_id")));
                appointmentInfo.setPatientName(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_name_new")));
                appointmentInfo.setOpenMrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("open_mrs_id")));
                try {
                    if (!encounterDAO.isCompletedOrExited(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")))) {
                        appointmentInfo.setStatus(idCursor.getString(idCursor.getColumnIndexOrThrow("status")));
                    } else {
                        appointmentInfo.setStatus(IntelehealthApplication.getAppContext().getString(R.string.visit_closed));
                    }
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                appointmentInfo.setCreatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("created_at")));
                appointmentInfo.setUpdatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("updated_at")));
                appointmentInfos.add(appointmentInfo);
            }
        }
        idCursor.close();
        return appointmentInfos;
    }

    public List<AppointmentInfo> getCancelledAppointmentsWithFiltersV1() {
        List<AppointmentInfo> appointmentInfos = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        Cursor idCursor = db.rawQuery("select p.patient_photo, p.first_name || ' ' || p.last_name as patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid, a.appointment_id,a.slot_date, a.slot_day, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, a.user_uuid, a.dr_name, a.visit_uuid, a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id from tbl_patient p, tbl_appointments a where p.uuid = a.patient_id and a.status = 'cancelled'", new String[]{});
        EncounterDAO encounterDAO = new EncounterDAO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                AppointmentInfo appointmentInfo = new AppointmentInfo();
                appointmentInfo.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                appointmentInfo.setId(idCursor.getInt(idCursor.getColumnIndexOrThrow("appointment_id")));
                appointmentInfo.setSlotDay(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_day")));
                appointmentInfo.setSlotDate(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_date")));
                appointmentInfo.setSlotDuration(idCursor.getInt(idCursor.getColumnIndexOrThrow("slot_duration")));
                appointmentInfo.setSlotDurationUnit(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_duration_unit")));
                appointmentInfo.setSlotTime(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_time")));
                appointmentInfo.setSpeciality(idCursor.getString(idCursor.getColumnIndexOrThrow("speciality")));
                appointmentInfo.setUserUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("user_uuid")));
                appointmentInfo.setDrName(idCursor.getString(idCursor.getColumnIndexOrThrow("dr_name")));
                appointmentInfo.setVisitUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")));
                appointmentInfo.setPatientId(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_id")));
                appointmentInfo.setPatientName(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_name_new")));
                appointmentInfo.setOpenMrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("open_mrs_id")));
                try {
                    if (!encounterDAO.isCompletedOrExited(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")))) {
                        appointmentInfo.setStatus(idCursor.getString(idCursor.getColumnIndexOrThrow("status")));
                    } else {
                        appointmentInfo.setStatus(IntelehealthApplication.getAppContext().getString(R.string.visit_closed));
                    }
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                appointmentInfo.setCreatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("created_at")));
                appointmentInfo.setUpdatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("updated_at")));
                appointmentInfos.add(appointmentInfo);
            }
        }
        idCursor.close();
        return appointmentInfos;
    }

    public List<AppointmentInfo> getAppointmentsWithFiltersForToday(String searchPatientText, String currentDate) {
        CustomLog.v(TAG, "getAppointmentsWithFiltersForToday - " + currentDate);
        String search = searchPatientText.trim().replaceAll("\\s", "");
        List<AppointmentInfo> appointmentInfos = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        Cursor idCursor;
        if (!searchPatientText.isEmpty()) {
            String selectQuery = "select p.patient_photo,p.first_name || ' ' || p.last_name as patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid, a.appointment_id," + " a.slot_date, a.slot_day, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, a.user_uuid, a.dr_name, a.visit_uuid," + " a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id  from tbl_patient p, tbl_appointments a where p.uuid = a.patient_id and patient_name_new LIKE " + "'%" + search + "%' and a.slot_date = '" + currentDate + "'";
            idCursor = db.rawQuery(selectQuery, new String[]{});
        } else {
            idCursor = db.rawQuery("select p.patient_photo, p.first_name || ' ' || p.last_name as patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid, a.appointment_id,a.slot_date, a.slot_day, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, a.user_uuid, a.dr_name, a.visit_uuid, a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id from tbl_patient p, tbl_appointments a where p.uuid = a.patient_id and a.slot_date = '" + currentDate + "'", new String[]{});

        }
        EncounterDAO encounterDAO = new EncounterDAO();
        CustomLog.v(TAG, "getAppointmentsWithFiltersForToday idCursor.getCount()  - " + idCursor.getCount());
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                AppointmentInfo appointmentInfo = new AppointmentInfo();
                appointmentInfo.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                appointmentInfo.setId(idCursor.getInt(idCursor.getColumnIndexOrThrow("appointment_id")));
                appointmentInfo.setSlotDay(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_day")));
                appointmentInfo.setSlotDate(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_date")));
                appointmentInfo.setSlotDuration(idCursor.getInt(idCursor.getColumnIndexOrThrow("slot_duration")));
                appointmentInfo.setSlotDurationUnit(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_duration_unit")));
                appointmentInfo.setSlotTime(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_time")));
                appointmentInfo.setSpeciality(idCursor.getString(idCursor.getColumnIndexOrThrow("speciality")));
                appointmentInfo.setUserUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("user_uuid")));
                appointmentInfo.setDrName(idCursor.getString(idCursor.getColumnIndexOrThrow("dr_name")));
                appointmentInfo.setVisitUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")));
                appointmentInfo.setPatientId(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_id")));
                appointmentInfo.setPatientName(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_name_new")));
                appointmentInfo.setOpenMrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("open_mrs_id")));
                appointmentInfo.setPatientDob(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                appointmentInfo.setPatientGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                try {
                    if (!encounterDAO.isCompletedOrExited(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")))) {
                        appointmentInfo.setStatus(idCursor.getString(idCursor.getColumnIndexOrThrow("status")));
                    } else {
                        appointmentInfo.setStatus(IntelehealthApplication.getAppContext().getString(R.string.visit_closed));
                    }
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                appointmentInfo.setCreatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("created_at")));
                appointmentInfo.setUpdatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("updated_at")));
                appointmentInfos.add(appointmentInfo);
            }

        }
        idCursor.close();
        return appointmentInfos;
    }

    public boolean updatePreviousAppointmentDetails(String appointment_id, String visit_uuid, String prev_slot_day, String prev_slot_date, String prev_slot_time) throws DAOException {

        boolean isCreated = true;
        long createdRecordsCount1 = 0;
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        String whereclause = "visit_uuid = ? ";
        String[] whereargs = {visit_uuid};

        db.beginTransaction();
        try {
            values.put("prev_slot_day", prev_slot_day);
            values.put("prev_slot_date", prev_slot_date);
            values.put("prev_slot_time", prev_slot_time);
            //String query = "update tbl_appointments set prev_slot_day = "

            //String strSQL = "UPDATE tbl_appointments SET prev_slot_day = '" + prev_slot_day + "' and prev_slot_date = '" + prev_slot_date + "' and prev_slot_time = '" + prev_slot_time + "'  WHERE visit_uuid = " + visit_uuid;
            //CustomLog.d(TAG, "updatePreviousAppointmentDetails:strSQL :  "+strSQL);
            // db.execSQL(strSQL, new String[]{});
            createdRecordsCount1 = db.update("tbl_appointments", values, whereclause, whereargs);
            db.setTransactionSuccessful();
            Logger.logD("created records", "created records count" + createdRecordsCount1);
        } catch (SQLException e) {
            isCreated = false;
            throw new DAOException(e.getMessage(), e);
        } finally {
            db.endTransaction();
        }
        return isCreated;

    }

    public AppointmentInfo getDetailsOfRescheduledAppointment(String visitUUID, String appointmentId) {
        CustomLog.v(TAG, "getDetailsOfRescheduledAppointment - visitUUID - " + visitUUID);

        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        //db.beginTransaction();
        Cursor idCursor = db.rawQuery("SELECT * FROM tbl_appointments where appointment_id = ? and visit_uuid = ?", new String[]{appointmentId, visitUUID});
        AppointmentInfo appointmentInfo = null;
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                appointmentInfo = new AppointmentInfo();
                appointmentInfo.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                appointmentInfo.setId(idCursor.getInt(idCursor.getColumnIndexOrThrow("appointment_id")));
                appointmentInfo.setSlotDay(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_day")));
                appointmentInfo.setSlotDate(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_date")));
                appointmentInfo.setSlotDuration(idCursor.getInt(idCursor.getColumnIndexOrThrow("slot_duration")));
                appointmentInfo.setSlotDurationUnit(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_duration_unit")));
                appointmentInfo.setSlotTime(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_time")));
                appointmentInfo.setStatus(idCursor.getString(idCursor.getColumnIndexOrThrow("status")));
                appointmentInfo.setPrev_slot_day(idCursor.getString(idCursor.getColumnIndexOrThrow("prev_slot_day")));
                appointmentInfo.setPrev_slot_date(idCursor.getString(idCursor.getColumnIndexOrThrow("prev_slot_date")));
                appointmentInfo.setPrev_slot_time(idCursor.getString(idCursor.getColumnIndexOrThrow("prev_slot_time")));

            }

        }
        idCursor.close();
        return appointmentInfo;
    }

    public List<AppointmentInfo> getCancelledAppointmentsWithFilters(String fromDate, String toDate, String searchPatientText) {
        Cursor idCursor;
        fromDate = getDateInFormat(fromDate);
        toDate = getDateInFormat(toDate);
        String search = searchPatientText.trim().replaceAll("\\s", "");
        List<AppointmentInfo> appointmentInfos = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();

        if (!fromDate.isEmpty() && !toDate.isEmpty() && !searchPatientText.isEmpty()) {
            String selectQuery = "SELECT p.patient_photo,p.first_name || ' ' || p.last_name AS patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid, a.appointment_id,"
                    + " a.slot_date, substr(a.slot_js_date, 1, 10) AS new_slot_date, a.slot_day, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, a.user_uuid, "
                    + "a.dr_name, a.visit_uuid," + " a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id  "
                    + "FROM tbl_patient p, tbl_appointments a "
                    + "WHERE p.uuid = a.patient_id "
                    + "AND status = 'cancelled' "
                    + "AND patient_name_new LIKE " + "'%" + search + "%'  "
                    + "AND new_slot_date BETWEEN '" + fromDate + "' AND '" + toDate + "'";

            idCursor = db.rawQuery(selectQuery, new String[]{});
        } else if (!fromDate.isEmpty() && !toDate.isEmpty()) {
            String selectQuery = "SELECT p.patient_photo, p.first_name || ' ' || p.last_name AS patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid,"
                    + " a.appointment_id,a.slot_date, a.slot_day, substr(a.slot_js_date, 1, 10) AS new_slot_date, a.slot_duration,a.slot_duration_unit, a.slot_time, "
                    + "a.speciality, a.user_uuid, a.dr_name, a.visit_uuid, a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id "
                    + "FROM tbl_patient p, tbl_appointments a "
                    + "WHERE p.uuid = a.patient_id "
                    + "AND a.status = 'cancelled' "
                    + "AND new_slot_date BETWEEN '" + fromDate + "' AND '" + toDate + "'";

            idCursor = db.rawQuery(selectQuery, null);
        } else if (!searchPatientText.isEmpty()) {
            String selectQuery = "SELECT p.patient_photo, p.first_name || ' ' || p.last_name as patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid, "
                    + "a.appointment_id,a.slot_date, a.slot_day, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, a.user_uuid, a.dr_name, a.visit_uuid, "
                    + "a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id "
                    + "FROM tbl_patient p, tbl_appointments a "
                    + "WHERE p.uuid = a.patient_id "
                    + "AND a.status = 'cancelled' "
                    + "AND patient_name_new LIKE " + "'%" + search + "%' ORDER BY patient_name ASC";

            idCursor = db.rawQuery(selectQuery, new String[]{});
        } else {
            idCursor = db.rawQuery("SELECT p.patient_photo, p.first_name || ' ' || p.last_name AS patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, "
                            + "a.uuid, a.appointment_id,a.slot_date, a.slot_day, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, a.user_uuid, a.dr_name, "
                            + "a.visit_uuid, a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id "
                            + "FROM tbl_patient p, tbl_appointments a "
                            + "WHERE p.uuid = a.patient_id "
                            + "AND a.status = 'cancelled' ",
                    new String[]{});

        }
        EncounterDAO encounterDAO = new EncounterDAO();
        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                AppointmentInfo appointmentInfo = new AppointmentInfo();
                appointmentInfo.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                appointmentInfo.setId(idCursor.getInt(idCursor.getColumnIndexOrThrow("appointment_id")));
                appointmentInfo.setSlotDay(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_day")));
                appointmentInfo.setSlotDate(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_date")));
                appointmentInfo.setSlotDuration(idCursor.getInt(idCursor.getColumnIndexOrThrow("slot_duration")));
                appointmentInfo.setSlotDurationUnit(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_duration_unit")));
                appointmentInfo.setSlotTime(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_time")));
                appointmentInfo.setSpeciality(idCursor.getString(idCursor.getColumnIndexOrThrow("speciality")));
                appointmentInfo.setUserUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("user_uuid")));
                appointmentInfo.setDrName(idCursor.getString(idCursor.getColumnIndexOrThrow("dr_name")));
                appointmentInfo.setVisitUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")));
                appointmentInfo.setPatientId(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_id")));
                appointmentInfo.setPatientName(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_name_new")));
                appointmentInfo.setOpenMrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("open_mrs_id")));
                appointmentInfo.setPatientDob(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                appointmentInfo.setPatientGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                try {
                    if (!encounterDAO.isCompletedOrExited(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")))) {
                        appointmentInfo.setStatus(idCursor.getString(idCursor.getColumnIndexOrThrow("status")));
                    } else {
                        appointmentInfo.setStatus(IntelehealthApplication.getAppContext().getString(R.string.visit_closed));
                    }
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                appointmentInfo.setCreatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("created_at")));
                appointmentInfo.setUpdatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("updated_at")));
                appointmentInfos.add(appointmentInfo);
            }
        }
        idCursor.close();
        return appointmentInfos;
    }

    public List<AppointmentInfo> getCancelledAppointmentsWithFilters(String fromDate, String toDate, int limit, int offset, String currentDate) {
        Cursor idCursor;
        fromDate = getDateInFormat(fromDate);
        toDate = getDateInFormat(toDate);
        List<AppointmentInfo> appointmentInfos = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();

        if (!fromDate.isEmpty() && !toDate.isEmpty()) {
            String selectQuery = "SELECT p.patient_photo, p.first_name || ' ' || p.last_name AS patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid,"
                    + " a.appointment_id,a.slot_date, a.slot_day, substr(a.slot_js_date, 1, 10) AS new_slot_date, a.slot_duration,a.slot_duration_unit, a.slot_time, "
                    + "a.speciality, a.user_uuid, a.dr_name, a.visit_uuid, a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id "
                    + "FROM tbl_patient p, tbl_appointments a "
                    + "WHERE p.uuid = a.patient_id "
                    + "AND a.status = 'cancelled' "
                    + "AND new_slot_date BETWEEN '" + fromDate + "' AND '" + toDate + "' "
                    + "LIMIT ? OFFSET ?";

            idCursor = db.rawQuery(selectQuery, new String[]{String.valueOf(limit), String.valueOf(offset)});
        } else {
            idCursor = db.rawQuery("SELECT p.patient_photo, p.first_name || ' ' || p.last_name AS patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, "
                            + "a.uuid, a.appointment_id,a.slot_date, a.slot_day, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, a.user_uuid, a.dr_name, "
                            + "a.visit_uuid, a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id "
                            + "FROM tbl_patient p, tbl_appointments a "
                            + "WHERE p.uuid = a.patient_id "
                            + "AND a.slot_date != ?"
                            + "AND a.status = 'cancelled'"
                            + "LIMIT ? OFFSET ?",
                    new String[]{currentDate, String.valueOf(limit), String.valueOf(offset)});
        }

        EncounterDAO encounterDAO = new EncounterDAO();

        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                AppointmentInfo appointmentInfo = new AppointmentInfo();
                appointmentInfo.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                appointmentInfo.setId(idCursor.getInt(idCursor.getColumnIndexOrThrow("appointment_id")));
                appointmentInfo.setSlotDay(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_day")));
                appointmentInfo.setSlotDate(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_date")));
                appointmentInfo.setSlotDuration(idCursor.getInt(idCursor.getColumnIndexOrThrow("slot_duration")));
                appointmentInfo.setSlotDurationUnit(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_duration_unit")));
                appointmentInfo.setSlotTime(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_time")));
                appointmentInfo.setSpeciality(idCursor.getString(idCursor.getColumnIndexOrThrow("speciality")));
                appointmentInfo.setUserUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("user_uuid")));
                appointmentInfo.setDrName(idCursor.getString(idCursor.getColumnIndexOrThrow("dr_name")));
                appointmentInfo.setVisitUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")));
                appointmentInfo.setPatientId(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_id")));
                appointmentInfo.setPatientName(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_name_new")));
                appointmentInfo.setOpenMrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("open_mrs_id")));
                appointmentInfo.setPatientDob(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                appointmentInfo.setPatientGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                try {
                    if (!encounterDAO.isCompletedOrExited(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")))) {
                        appointmentInfo.setStatus(idCursor.getString(idCursor.getColumnIndexOrThrow("status")));
                    } else {
                        appointmentInfo.setStatus(IntelehealthApplication.getAppContext().getString(R.string.visit_closed));
                    }
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                appointmentInfo.setCreatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("created_at")));
                appointmentInfo.setUpdatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("updated_at")));
                appointmentInfos.add(appointmentInfo);
            }

        }

        idCursor.close();
        return appointmentInfos;
    }

    public List<AppointmentInfo> getAllCancelledAppointmentsWithFilters(String fromDate, String toDate) {
        Cursor idCursor;
        fromDate = getDateInFormat(fromDate);
        toDate = getDateInFormat(toDate);
        List<AppointmentInfo> appointmentInfos = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();

        if (!fromDate.isEmpty() && !toDate.isEmpty()) {
            String selectQuery = "SELECT p.patient_photo, p.first_name || ' ' || p.last_name AS patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid,"
                    + " a.appointment_id,a.slot_date, a.slot_day, substr(a.slot_js_date, 1, 10) AS new_slot_date, a.slot_duration,a.slot_duration_unit, a.slot_time, "
                    + "a.speciality, a.user_uuid, a.dr_name, a.visit_uuid, a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id "
                    + "FROM tbl_patient p, tbl_appointments a "
                    + "WHERE p.uuid = a.patient_id "
                    + "AND a.status = 'cancelled' "
                    + "AND new_slot_date BETWEEN '" + fromDate + "' AND '" + toDate + "' ";

            idCursor = db.rawQuery(selectQuery, null);
        } else {
            idCursor = db.rawQuery("SELECT p.patient_photo, p.first_name || ' ' || p.last_name AS patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, "
                    + "a.uuid, a.appointment_id,a.slot_date, a.slot_day, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, a.user_uuid, a.dr_name, "
                    + "a.visit_uuid, a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id "
                    + "FROM tbl_patient p, tbl_appointments a "
                    + "WHERE p.uuid = a.patient_id "
                    + "AND a.status = 'cancelled'", null);
        }

        EncounterDAO encounterDAO = new EncounterDAO();

        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                AppointmentInfo appointmentInfo = new AppointmentInfo();
                appointmentInfo.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                appointmentInfo.setId(idCursor.getInt(idCursor.getColumnIndexOrThrow("appointment_id")));
                appointmentInfo.setSlotDay(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_day")));
                appointmentInfo.setSlotDate(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_date")));
                appointmentInfo.setSlotDuration(idCursor.getInt(idCursor.getColumnIndexOrThrow("slot_duration")));
                appointmentInfo.setSlotDurationUnit(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_duration_unit")));
                appointmentInfo.setSlotTime(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_time")));
                appointmentInfo.setSpeciality(idCursor.getString(idCursor.getColumnIndexOrThrow("speciality")));
                appointmentInfo.setUserUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("user_uuid")));
                appointmentInfo.setDrName(idCursor.getString(idCursor.getColumnIndexOrThrow("dr_name")));
                appointmentInfo.setVisitUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")));
                appointmentInfo.setPatientId(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_id")));
                appointmentInfo.setPatientName(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_name_new")));
                appointmentInfo.setOpenMrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("open_mrs_id")));
                appointmentInfo.setPatientDob(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                appointmentInfo.setPatientGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                try {
                    if (!encounterDAO.isCompletedOrExited(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")))) {
                        appointmentInfo.setStatus(idCursor.getString(idCursor.getColumnIndexOrThrow("status")));
                    } else {
                        appointmentInfo.setStatus(IntelehealthApplication.getAppContext().getString(R.string.visit_closed));
                    }
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                appointmentInfo.setCreatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("created_at")));
                appointmentInfo.setUpdatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("updated_at")));
                appointmentInfos.add(appointmentInfo);
            }

        }

        idCursor.close();
        return appointmentInfos;
    }


    public List<AppointmentInfo> getCancelledAppointmentsWithFiltersForToday(String searchPatientText, String currentDate) {
        String search = searchPatientText.trim().replaceAll("\\s", "");
        List<AppointmentInfo> appointmentInfos = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        Cursor idCursor;
        if (!searchPatientText.isEmpty()) {
            String selectQuery = "select p.patient_photo,p.first_name || ' ' || p.last_name as patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid, a.appointment_id," + "a.slot_date, a.slot_day, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, a.user_uuid, a.dr_name, a.visit_uuid," + "a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id  from tbl_patient p, tbl_appointments a where p.uuid = a.patient_id " + "and a.status = 'cancelled' and patient_name_new LIKE " + "'%" + search + "%' and a.slot_date = '" + currentDate + "'";
            idCursor = db.rawQuery(selectQuery, new String[]{});
        } else {
            idCursor = db.rawQuery("select p.patient_photo, p.first_name || ' ' || p.last_name as patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid, a.appointment_id,a.slot_date, a.slot_day, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, a.user_uuid, a.dr_name, a.visit_uuid, a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id from tbl_patient p, tbl_appointments a where p.uuid = a.patient_id and a.status = 'cancelled' and a.slot_date = '" + currentDate + "'", new String[]{});

        }
        EncounterDAO encounterDAO = new EncounterDAO();

        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                AppointmentInfo appointmentInfo = new AppointmentInfo();
                appointmentInfo.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                appointmentInfo.setId(idCursor.getInt(idCursor.getColumnIndexOrThrow("appointment_id")));
                appointmentInfo.setSlotDay(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_day")));
                appointmentInfo.setSlotDate(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_date")));
                appointmentInfo.setSlotDuration(idCursor.getInt(idCursor.getColumnIndexOrThrow("slot_duration")));
                appointmentInfo.setSlotDurationUnit(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_duration_unit")));
                appointmentInfo.setSlotTime(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_time")));
                appointmentInfo.setSpeciality(idCursor.getString(idCursor.getColumnIndexOrThrow("speciality")));
                appointmentInfo.setUserUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("user_uuid")));
                appointmentInfo.setDrName(idCursor.getString(idCursor.getColumnIndexOrThrow("dr_name")));
                appointmentInfo.setVisitUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")));
                appointmentInfo.setPatientId(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_id")));
                appointmentInfo.setPatientName(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_name_new")));
                appointmentInfo.setOpenMrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("open_mrs_id")));
                appointmentInfo.setPatientDob(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                appointmentInfo.setPatientGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                try {
                    if (!encounterDAO.isCompletedOrExited(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")))) {
                        appointmentInfo.setStatus(idCursor.getString(idCursor.getColumnIndexOrThrow("status")));
                    } else {
                        appointmentInfo.setStatus(IntelehealthApplication.getAppContext().getString(R.string.visit_closed));
                    }
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                appointmentInfo.setCreatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("created_at")));
                appointmentInfo.setUpdatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("updated_at")));
                appointmentInfos.add(appointmentInfo);
            }
        }
        idCursor.close();
        return appointmentInfos;
    }

    public List<BookAppointmentRequest> getUnsyncedAppointments() throws DAOException {
        List<BookAppointmentRequest> requestList = new ArrayList<>();
        BookAppointmentRequest bookAppointmentRequest = new BookAppointmentRequest();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWritableDatabase();
        try {
            Cursor idCursor = db.rawQuery("SELECT * FROM tbl_appointments where (sync = ? OR sync=?) COLLATE NOCASE", new String[]{"0", "false"});
            if (idCursor.getCount() != 0) {
                while (idCursor.moveToNext()) {
                    bookAppointmentRequest = new BookAppointmentRequest();
                    bookAppointmentRequest.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                    bookAppointmentRequest.setAppointmentId(idCursor.getInt(idCursor.getColumnIndexOrThrow("appointment_id")));
                    bookAppointmentRequest.setSlotDay(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_day")));
                    bookAppointmentRequest.setSlotDate(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_date")));
                    bookAppointmentRequest.setSlotDuration(idCursor.getInt(idCursor.getColumnIndexOrThrow("slot_duration")));
                    bookAppointmentRequest.setSlotDurationUnit(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_duration_unit")));
                    bookAppointmentRequest.setSlotTime(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_time")));
                    bookAppointmentRequest.setSpeciality(idCursor.getString(idCursor.getColumnIndexOrThrow("speciality")));
                    bookAppointmentRequest.setUserUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("user_uuid")));
                    bookAppointmentRequest.setDrName(idCursor.getString(idCursor.getColumnIndexOrThrow("dr_name")));
                    bookAppointmentRequest.setVisitUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")));
                    bookAppointmentRequest.setPatientId(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_id")));
                    bookAppointmentRequest.setPatientName(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_name")));
                    bookAppointmentRequest.setOpenMrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("open_mrs_id")));
                    bookAppointmentRequest.setLocationUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("location_uuid")));
                    bookAppointmentRequest.setHwUUID(idCursor.getString(idCursor.getColumnIndexOrThrow("hw_uuid")));
                    bookAppointmentRequest.setReason(idCursor.getString(idCursor.getColumnIndexOrThrow("reason")));
                    bookAppointmentRequest.setSync(idCursor.getString(idCursor.getColumnIndexOrThrow("sync")));
                }

                String patientSelection = "uuid = ?";
                String[] patientArgs = {bookAppointmentRequest.getPatientId()};
                String table = "tbl_patient";
                String patientAge = "", patientGender = "", patientPic = "";

                String[] columnsToReturn = {"date_of_birth", "gender", "patient_photo"};
                final Cursor patientCursor = db.query(table, columnsToReturn, patientSelection, patientArgs, null, null, null);

                if (patientCursor.moveToFirst()) {
                    do {
                        patientAge = DateAndTimeUtils.getAgeInYears(patientCursor.getString(patientCursor.getColumnIndex("date_of_birth")), IntelehealthApplication.getAppContext());
                        patientGender = patientCursor.getString(patientCursor.getColumnIndex("gender"));
                        patientPic = patientCursor.getString(patientCursor.getColumnIndex("patient_photo"));
                    } while (patientCursor.moveToNext());
                }

                bookAppointmentRequest.setPatientAge(patientAge);
                bookAppointmentRequest.setPatientGender(patientGender);
                bookAppointmentRequest.setPatientPic(new Base64Utils().getBase64FromFileWithConversion(patientPic));

                patientCursor.close();

                String hwSelection = "uuid = ?";
                String[] hwArgs = {bookAppointmentRequest.getHwUUID()};
                String hwTable = "tbl_provider";
                String hwAge = "", hwGender = "", hwName = "";

                String[] hwColumnsToReturn = {"dateofbirth", "gender", "given_name", "middle_name", "family_name"};
                final Cursor providerCursor = db.query(hwTable, hwColumnsToReturn, hwSelection, hwArgs, null, null, null);
                if (providerCursor.moveToFirst()) {
                    do {
                        hwAge = DateAndTimeUtils.getAgeInYearMonth(providerCursor.getString(providerCursor.getColumnIndex("dateofbirth")));
                        hwGender = providerCursor.getString(providerCursor.getColumnIndex("gender"));

                        String firstName = providerCursor.getString(providerCursor.getColumnIndex("given_name"));
                        String middleName = providerCursor.getString(providerCursor.getColumnIndex("middle_name"));
                        String lastName = providerCursor.getString(providerCursor.getColumnIndex("family_name"));
                        hwName = firstName + " " + ((!TextUtils.isEmpty(middleName)) ? middleName : "") + " " + lastName;
                    } while (providerCursor.moveToNext());
                }

                bookAppointmentRequest.setHwAge(hwAge);
                bookAppointmentRequest.setHwGender(hwGender);
                bookAppointmentRequest.setHwName(hwName);

                providerCursor.close();

                requestList.add(bookAppointmentRequest);
            }
            idCursor.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
            throw new DAOException(exception.getMessage(), exception);
        }

        return requestList;
    }

    public List<AppointmentInfo> getUpcomingAppointmentsForToday(String currentDate, int limit, int offset) {
        Cursor idCursor;
        List<AppointmentInfo> appointmentInfos = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();

        idCursor = db.rawQuery("SELECT p.patient_photo, p.first_name || ' ' || p.last_name AS patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid, "
                        + "a.appointment_id,a.slot_date, a.slot_day, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, a.user_uuid, a.dr_name, a.visit_uuid, a.patient_id, "
                        + "a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id "
                        + "FROM tbl_patient p, tbl_appointments a "
                        + "WHERE p.uuid = a.patient_id "
                        + "AND a.slot_date = '" + currentDate + "'"
                        + "AND a.status = 'booked'"
                        + "AND datetime(a.slot_js_date) >= datetime('now') "
                        + "LIMIT ? OFFSET ?"
                , new String[]{String.valueOf(limit), String.valueOf(offset)});

        EncounterDAO encounterDAO = new EncounterDAO();

        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                AppointmentInfo appointmentInfo = new AppointmentInfo();
                appointmentInfo.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                appointmentInfo.setId(idCursor.getInt(idCursor.getColumnIndexOrThrow("appointment_id")));
                appointmentInfo.setSlotDay(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_day")));
                appointmentInfo.setSlotDate(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_date")));
                appointmentInfo.setSlotDuration(idCursor.getInt(idCursor.getColumnIndexOrThrow("slot_duration")));
                appointmentInfo.setSlotDurationUnit(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_duration_unit")));
                appointmentInfo.setSlotTime(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_time")));
                appointmentInfo.setSpeciality(idCursor.getString(idCursor.getColumnIndexOrThrow("speciality")));
                appointmentInfo.setUserUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("user_uuid")));
                appointmentInfo.setDrName(idCursor.getString(idCursor.getColumnIndexOrThrow("dr_name")));
                appointmentInfo.setVisitUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")));
                appointmentInfo.setPatientId(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_id")));
                appointmentInfo.setPatientName(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_name_new")));
                appointmentInfo.setOpenMrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("open_mrs_id")));
                appointmentInfo.setPatientDob(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                appointmentInfo.setPatientGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                try {
                    if (!encounterDAO.isCompletedOrExited(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")))) {
                        appointmentInfo.setStatus(idCursor.getString(idCursor.getColumnIndexOrThrow("status")));
                    } else {
                        appointmentInfo.setStatus(IntelehealthApplication.getAppContext().getString(R.string.visit_closed));
                    }
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                appointmentInfo.setCreatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("created_at")));
                appointmentInfo.setUpdatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("updated_at")));
                appointmentInfos.add(appointmentInfo);
            }

        }

        idCursor.close();
        return appointmentInfos;
    }

    public List<AppointmentInfo> getUpcomingAppointments(int limit, int offset, String orderType, String query) {
        Cursor idCursor;
        List<AppointmentInfo> appointmentInfos = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        String searchQuery = "";
        if (!query.isEmpty()) {
            searchQuery = " AND ((patient_name_new LIKE " + "'%" + query + "%') OR (p.openmrs_id LIKE " + "'%" + query + "%'))";
        }

        String middleName = "CASE WHEN p.middle_name IS NOT NULL THEN ' ' || p.middle_name || ' ' ELSE ' ' END";

        idCursor = db.rawQuery("SELECT p.patient_photo, p.first_name || " + middleName + " || p.last_name AS patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid, "
                        + "a.appointment_id,a.slot_date, a.slot_day, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, a.user_uuid, a.dr_name, a.visit_uuid, a.patient_id, "
                        + "a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id "
                        + "FROM tbl_patient p, tbl_appointments a "
                        + "WHERE p.uuid = a.patient_id "
                        + "AND a.status = 'booked'"
                        + "AND datetime(a.slot_js_date) >= datetime('now')"
                        + searchQuery
                        + "ORDER BY  datetime(a.slot_js_date) " + orderType
                        + " LIMIT ? OFFSET ?"
                , new String[]{String.valueOf(limit), String.valueOf(offset)});

        EncounterDAO encounterDAO = new EncounterDAO();

        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                AppointmentInfo appointmentInfo = new AppointmentInfo();
                appointmentInfo.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                appointmentInfo.setId(idCursor.getInt(idCursor.getColumnIndexOrThrow("appointment_id")));
                appointmentInfo.setSlotDay(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_day")));
                appointmentInfo.setSlotDate(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_date")));
                appointmentInfo.setSlotDuration(idCursor.getInt(idCursor.getColumnIndexOrThrow("slot_duration")));
                appointmentInfo.setSlotDurationUnit(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_duration_unit")));
                appointmentInfo.setSlotTime(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_time")));
                appointmentInfo.setSpeciality(idCursor.getString(idCursor.getColumnIndexOrThrow("speciality")));
                appointmentInfo.setUserUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("user_uuid")));
                appointmentInfo.setDrName(idCursor.getString(idCursor.getColumnIndexOrThrow("dr_name")));
                appointmentInfo.setVisitUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")));
                appointmentInfo.setPatientId(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_id")));
                appointmentInfo.setPatientName(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_name_new")));
                appointmentInfo.setOpenMrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("open_mrs_id")));
                appointmentInfo.setPatientDob(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                appointmentInfo.setPatientGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                try {
                    if (!encounterDAO.isCompletedOrExited(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")))) {
                        appointmentInfo.setStatus(idCursor.getString(idCursor.getColumnIndexOrThrow("status")));
                    } else {
                        appointmentInfo.setStatus(IntelehealthApplication.getAppContext().getString(R.string.visit_closed));
                    }
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                appointmentInfo.setCreatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("created_at")));
                appointmentInfo.setUpdatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("updated_at")));
                appointmentInfos.add(appointmentInfo);
            }

        }

        idCursor.close();
        return appointmentInfos;
    }

    public List<AppointmentInfo> getAllUpcomingAppointmentsForToday(String currentDate) {
        Cursor idCursor;
        List<AppointmentInfo> appointmentInfos = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();

        idCursor = db.rawQuery("SELECT p.patient_photo, p.first_name || ' ' || p.last_name AS patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid, "
                        + "a.appointment_id,a.slot_date, a.slot_day, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, a.user_uuid, a.dr_name, a.visit_uuid, a.patient_id, "
                        + "a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id "
                        + "FROM tbl_patient p, tbl_appointments a "
                        + "WHERE p.uuid = a.patient_id "
                        + "AND a.slot_date = '" + currentDate + "'"
                        + "AND a.status = 'booked'"
                        + "AND (substr(a.slot_date, 7, 4) || '-' || substr(a.slot_date, 4, 2) || '-' || substr(a.slot_date, 1, 2) || ' ' ||"
                        + " CASE"
                        + " WHEN substr(a.slot_time, -2) = 'PM' THEN strftime('%H:%M:%S', CASE WHEN LENGTH(a.slot_time) = 7 THEN '0' || SUBSTR(a.slot_time, 1, 5) ELSE SUBSTR(a.slot_time, 1, 5) END, '+12 hours')"
                        + " ELSE strftime('%H:%M:%S', '0' || CASE WHEN LENGTH(a.slot_time) = 7 THEN '0' || SUBSTR(a.slot_time, 1, 5) ELSE SUBSTR(a.slot_time, 1, 5) END)"
                        + " END) >= datetime('now', 'localtime')",
                new String[]{});


        EncounterDAO encounterDAO = new EncounterDAO();

        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                AppointmentInfo appointmentInfo = new AppointmentInfo();
                appointmentInfo.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                appointmentInfo.setId(idCursor.getInt(idCursor.getColumnIndexOrThrow("appointment_id")));
                appointmentInfo.setSlotDay(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_day")));
                appointmentInfo.setSlotDate(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_date")));
                appointmentInfo.setSlotDuration(idCursor.getInt(idCursor.getColumnIndexOrThrow("slot_duration")));
                appointmentInfo.setSlotDurationUnit(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_duration_unit")));
                appointmentInfo.setSlotTime(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_time")));
                appointmentInfo.setSpeciality(idCursor.getString(idCursor.getColumnIndexOrThrow("speciality")));
                appointmentInfo.setUserUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("user_uuid")));
                appointmentInfo.setDrName(idCursor.getString(idCursor.getColumnIndexOrThrow("dr_name")));
                appointmentInfo.setVisitUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")));
                appointmentInfo.setPatientId(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_id")));
                appointmentInfo.setPatientName(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_name_new")));
                appointmentInfo.setOpenMrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("open_mrs_id")));
                appointmentInfo.setPatientDob(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                appointmentInfo.setPatientGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                try {
                    if (!encounterDAO.isCompletedOrExited(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")))) {
                        appointmentInfo.setStatus(idCursor.getString(idCursor.getColumnIndexOrThrow("status")));
                    } else {
                        appointmentInfo.setStatus(IntelehealthApplication.getAppContext().getString(R.string.visit_closed));
                    }
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                appointmentInfo.setCreatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("created_at")));
                appointmentInfo.setUpdatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("updated_at")));
                appointmentInfos.add(appointmentInfo);
            }

        }

        idCursor.close();
        return appointmentInfos;
    }

    public List<AppointmentInfo> getCancelledAppointmentsForToday(String currentDate, int limit, int offset) {
        List<AppointmentInfo> appointmentInfos = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        Cursor idCursor;

        idCursor = db.rawQuery("select p.patient_photo, p.first_name || ' ' || p.last_name as patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid, "
                        + "a.appointment_id,a.slot_date, a.slot_day, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, a.user_uuid, "
                        + "a.dr_name, a.visit_uuid, a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id "
                        + "from tbl_patient p, tbl_appointments a "
                        + "where p.uuid = a.patient_id "
                        + "and a.status = 'cancelled' "
                        + "and a.slot_date = '" + currentDate + "' "
                        + "LIMIT ? OFFSET ?",
                new String[]{String.valueOf(limit), String.valueOf(offset)});

        EncounterDAO encounterDAO = new EncounterDAO();

        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                AppointmentInfo appointmentInfo = new AppointmentInfo();
                appointmentInfo.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                appointmentInfo.setId(idCursor.getInt(idCursor.getColumnIndexOrThrow("appointment_id")));
                appointmentInfo.setSlotDay(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_day")));
                appointmentInfo.setSlotDate(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_date")));
                appointmentInfo.setSlotDuration(idCursor.getInt(idCursor.getColumnIndexOrThrow("slot_duration")));
                appointmentInfo.setSlotDurationUnit(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_duration_unit")));
                appointmentInfo.setSlotTime(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_time")));
                appointmentInfo.setSpeciality(idCursor.getString(idCursor.getColumnIndexOrThrow("speciality")));
                appointmentInfo.setUserUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("user_uuid")));
                appointmentInfo.setDrName(idCursor.getString(idCursor.getColumnIndexOrThrow("dr_name")));
                appointmentInfo.setVisitUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")));
                appointmentInfo.setPatientId(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_id")));
                appointmentInfo.setPatientName(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_name_new")));
                appointmentInfo.setOpenMrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("open_mrs_id")));
                appointmentInfo.setPatientDob(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                appointmentInfo.setPatientGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                try {
                    if (!encounterDAO.isCompletedOrExited(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")))) {
                        appointmentInfo.setStatus(idCursor.getString(idCursor.getColumnIndexOrThrow("status")));
                    } else {
                        appointmentInfo.setStatus(IntelehealthApplication.getAppContext().getString(R.string.visit_closed));
                    }
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                appointmentInfo.setCreatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("created_at")));
                appointmentInfo.setUpdatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("updated_at")));
                appointmentInfos.add(appointmentInfo);
            }

        }

        idCursor.close();
        return appointmentInfos;
    }

    public List<AppointmentInfo> getAllCancelledAppointmentsForToday(String currentDate) {
        List<AppointmentInfo> appointmentInfos = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        Cursor idCursor;

        idCursor = db.rawQuery("select p.patient_photo, p.first_name || ' ' || p.last_name as patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid, "
                        + "a.appointment_id,a.slot_date, a.slot_day, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, a.user_uuid, "
                        + "a.dr_name, a.visit_uuid, a.patient_id, a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id "
                        + "from tbl_patient p, tbl_appointments a "
                        + "where p.uuid = a.patient_id "
                        + "and a.status = 'cancelled' "
                        + "and a.slot_date = '" + currentDate + "' ",
                new String[]{});


        EncounterDAO encounterDAO = new EncounterDAO();

        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                AppointmentInfo appointmentInfo = new AppointmentInfo();
                appointmentInfo.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                appointmentInfo.setId(idCursor.getInt(idCursor.getColumnIndexOrThrow("appointment_id")));
                appointmentInfo.setSlotDay(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_day")));
                appointmentInfo.setSlotDate(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_date")));
                appointmentInfo.setSlotDuration(idCursor.getInt(idCursor.getColumnIndexOrThrow("slot_duration")));
                appointmentInfo.setSlotDurationUnit(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_duration_unit")));
                appointmentInfo.setSlotTime(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_time")));
                appointmentInfo.setSpeciality(idCursor.getString(idCursor.getColumnIndexOrThrow("speciality")));
                appointmentInfo.setUserUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("user_uuid")));
                appointmentInfo.setDrName(idCursor.getString(idCursor.getColumnIndexOrThrow("dr_name")));
                appointmentInfo.setVisitUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")));
                appointmentInfo.setPatientId(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_id")));
                appointmentInfo.setPatientName(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_name_new")));
                appointmentInfo.setOpenMrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("open_mrs_id")));
                appointmentInfo.setPatientDob(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                appointmentInfo.setPatientGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                try {
                    if (!encounterDAO.isCompletedOrExited(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")))) {
                        appointmentInfo.setStatus(idCursor.getString(idCursor.getColumnIndexOrThrow("status")));
                    } else {
                        appointmentInfo.setStatus(IntelehealthApplication.getAppContext().getString(R.string.visit_closed));
                    }
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                appointmentInfo.setCreatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("created_at")));
                appointmentInfo.setUpdatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("updated_at")));
                appointmentInfos.add(appointmentInfo);
            }

        }

        idCursor.close();
        return appointmentInfos;
    }

    public List<AppointmentInfo> getCompletedAppointmentsForToday(String currentDate, int limit, int offset) {
        Cursor idCursor;
        List<AppointmentInfo> appointmentInfos = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();

        idCursor = db.rawQuery("SELECT p.patient_photo, p.first_name || ' ' || p.last_name AS patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid, "
                        + "a.appointment_id,a.slot_date, a.slot_day, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, a.user_uuid, a.dr_name, a.visit_uuid, a.patient_id, "
                        + "a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id "
                        + "FROM tbl_patient p, tbl_appointments a "
                        + "WHERE p.uuid = a.patient_id "
                        + "AND a.slot_date = '" + currentDate + "'"
                        + "AND a.status = 'booked'"
                        + "AND datetime(a.slot_js_date) < datetime('now') "
                        + "LIMIT ? OFFSET ?"
                , new String[]{String.valueOf(limit), String.valueOf(offset)});

        EncounterDAO encounterDAO = new EncounterDAO();

        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                AppointmentInfo appointmentInfo = new AppointmentInfo();
                appointmentInfo.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                appointmentInfo.setId(idCursor.getInt(idCursor.getColumnIndexOrThrow("appointment_id")));
                appointmentInfo.setSlotDay(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_day")));
                appointmentInfo.setSlotDate(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_date")));
                appointmentInfo.setSlotDuration(idCursor.getInt(idCursor.getColumnIndexOrThrow("slot_duration")));
                appointmentInfo.setSlotDurationUnit(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_duration_unit")));
                appointmentInfo.setSlotTime(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_time")));
                appointmentInfo.setSpeciality(idCursor.getString(idCursor.getColumnIndexOrThrow("speciality")));
                appointmentInfo.setUserUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("user_uuid")));
                appointmentInfo.setDrName(idCursor.getString(idCursor.getColumnIndexOrThrow("dr_name")));
                appointmentInfo.setVisitUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")));
                appointmentInfo.setPatientId(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_id")));
                appointmentInfo.setPatientName(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_name_new")));
                appointmentInfo.setOpenMrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("open_mrs_id")));
                appointmentInfo.setPatientDob(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                appointmentInfo.setPatientGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                try {
                    if (!encounterDAO.isCompletedOrExited(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")))) {
                        appointmentInfo.setStatus(idCursor.getString(idCursor.getColumnIndexOrThrow("status")));
                    } else {
                        appointmentInfo.setStatus(IntelehealthApplication.getAppContext().getString(R.string.visit_closed));
                    }
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                appointmentInfo.setCreatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("created_at")));
                appointmentInfo.setUpdatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("updated_at")));
                appointmentInfos.add(appointmentInfo);
            }

        }

        idCursor.close();
        return appointmentInfos;
    }

    public List<AppointmentInfo> getAllCompletedAppointmentsForToday(String currentDate) {
        Cursor idCursor;
        List<AppointmentInfo> appointmentInfos = new ArrayList<>();
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();

        idCursor = db.rawQuery("SELECT p.patient_photo, p.first_name || ' ' || p.last_name AS patient_name_new, p.openmrs_id, p.date_of_birth, p.gender, a.uuid, "
                        + "a.appointment_id,a.slot_date, a.slot_day, a.slot_duration,a.slot_duration_unit, a.slot_time, a.speciality, a.user_uuid, a.dr_name, a.visit_uuid, a.patient_id, "
                        + "a.created_at, a.updated_at, a.status, a.visit_uuid, a.open_mrs_id "
                        + "FROM tbl_patient p, tbl_appointments a "
                        + "WHERE p.uuid = a.patient_id "
                        + "AND a.slot_date = '" + currentDate + "'"
                        + "AND a.status = 'booked'"
                        + "AND datetime(a.slot_js_date) < datetime('now') ",
                new String[]{});

        EncounterDAO encounterDAO = new EncounterDAO();

        if (idCursor.getCount() != 0) {
            while (idCursor.moveToNext()) {
                AppointmentInfo appointmentInfo = new AppointmentInfo();
                appointmentInfo.setUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("uuid")));
                appointmentInfo.setId(idCursor.getInt(idCursor.getColumnIndexOrThrow("appointment_id")));
                appointmentInfo.setSlotDay(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_day")));
                appointmentInfo.setSlotDate(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_date")));
                appointmentInfo.setSlotDuration(idCursor.getInt(idCursor.getColumnIndexOrThrow("slot_duration")));
                appointmentInfo.setSlotDurationUnit(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_duration_unit")));
                appointmentInfo.setSlotTime(idCursor.getString(idCursor.getColumnIndexOrThrow("slot_time")));
                appointmentInfo.setSpeciality(idCursor.getString(idCursor.getColumnIndexOrThrow("speciality")));
                appointmentInfo.setUserUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("user_uuid")));
                appointmentInfo.setDrName(idCursor.getString(idCursor.getColumnIndexOrThrow("dr_name")));
                appointmentInfo.setVisitUuid(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")));
                appointmentInfo.setPatientId(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_id")));
                appointmentInfo.setPatientName(idCursor.getString(idCursor.getColumnIndexOrThrow("patient_name_new")));
                appointmentInfo.setOpenMrsId(idCursor.getString(idCursor.getColumnIndexOrThrow("open_mrs_id")));
                appointmentInfo.setPatientDob(idCursor.getString(idCursor.getColumnIndexOrThrow("date_of_birth")));
                appointmentInfo.setPatientGender(idCursor.getString(idCursor.getColumnIndexOrThrow("gender")));
                try {
                    if (!encounterDAO.isCompletedOrExited(idCursor.getString(idCursor.getColumnIndexOrThrow("visit_uuid")))) {
                        appointmentInfo.setStatus(idCursor.getString(idCursor.getColumnIndexOrThrow("status")));
                    } else {
                        appointmentInfo.setStatus(IntelehealthApplication.getAppContext().getString(R.string.visit_closed));
                    }
                } catch (DAOException e) {
                    e.printStackTrace();
                }
                appointmentInfo.setCreatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("created_at")));
                appointmentInfo.setUpdatedAt(idCursor.getString(idCursor.getColumnIndexOrThrow("updated_at")));
                appointmentInfos.add(appointmentInfo);
            }

        }

        idCursor.close();
        return appointmentInfos;
    }

    public boolean doesAppointmentExistForVisit(String visitUUID) {
        boolean doesAppointmentExist = false;
        String query = "SELECT * FROM tbl_appointments WHERE visit_uuid = ?";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        Cursor cursor = db.rawQuery(query, new String[]{visitUUID});

        if (cursor != null && cursor.moveToFirst()) {
            String appointmentId = cursor.getString(cursor.getColumnIndexOrThrow("uuid"));
            if (appointmentId != null) {
                doesAppointmentExist = true;
            }
            cursor.close();
        }

        db.setTransactionSuccessful();
        db.endTransaction();
        return doesAppointmentExist;
    }

    /*
     checking appointment status here.
     */
    public String checkAppointmentStatus(String visitUUID) {
        String status = "";
        String query = "SELECT * FROM tbl_appointments WHERE visit_uuid = ?";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        Cursor cursor = db.rawQuery(query, new String[]{visitUUID});

        if (cursor != null && cursor.moveToFirst()) {
            status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
            cursor.close();
        }

        db.setTransactionSuccessful();
        db.endTransaction();
        return status;
    }

    public String getTimeAndDateForAppointment(String visitUUID) {
        String appointmentDateTime = "";
        String query = "SELECT * FROM tbl_appointments WHERE visit_uuid = ?";
        SQLiteDatabase db = IntelehealthApplication.inteleHealthDatabaseHelper.getWriteDb();
        db.beginTransaction();
        Cursor cursor = db.rawQuery(query, new String[]{visitUUID});

        if (cursor != null && cursor.moveToFirst()) {
            String appointmentDate = cursor.getString(cursor.getColumnIndexOrThrow("slot_date"));
            String appointmentTime = cursor.getString(cursor.getColumnIndexOrThrow("slot_time"));
            appointmentDateTime = appointmentDate + " " + appointmentTime;
            cursor.close();
        }

        db.setTransactionSuccessful();
        db.endTransaction();
        return appointmentDateTime;
    }
}