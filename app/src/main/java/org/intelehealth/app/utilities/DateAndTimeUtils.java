package org.intelehealth.app.utilities;

import android.content.Context;
import android.text.format.DateUtils;

import org.intelehealth.app.utilities.CustomLog;

import androidx.annotation.NonNull;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.app.R;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DateAndTimeUtils {
    private static final String TAG = "DateAndTimeUtils";
    public static final String D_FORMAT_dd_M_yyyy = "dd/M/yyyy";

    public static float getFloat_Age_Year_Month(String date_of_birth) {
        float year_month = 0;

        if (date_of_birth == null) return 0;

        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date = null;
        try {
            date = originalFormat.parse(date_of_birth);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String formattedDate = targetFormat.format(date);  // 20120821

        String[] components = formattedDate.split("\\-");

        int year = Integer.parseInt(components[2]);
        int month = Integer.parseInt(components[1]);
        int day = Integer.parseInt(components[0]);

        LocalDate birthdate = new LocalDate(year, month, day);          //Birth date
        LocalDate now = new LocalDate();                    //Today's date
        Period period = new Period(birthdate, now, PeriodType.yearMonthDay());

        int xyears, xmonths;
        String x_format = "";

        if (period.getYears() > 0) xyears = period.getYears();
        else xyears = 0;
        if (period.getMonths() > 0) xmonths = period.getMonths();
        else xmonths = 0;

        x_format = xyears + "." + xmonths;
        year_month = Float.parseFloat(x_format);

        return year_month;
    }

    public String currentDateTime() {
        Locale.setDefault(Locale.ENGLISH);
        DateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
// you can get seconds by adding  "...:ss" to it
        Date todayDate = new Date();
        return date.format(todayDate);
    }

    public static int getAge(String s, Context context) {
        if (s == null) return 0;

        SessionManager sessionManager = new SessionManager(context);
        String language = sessionManager.getAppLanguage();
        CustomLog.d("LANG", "LANG: " + sessionManager.getAppLanguage());

        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date date = null;
        try {
            date = originalFormat.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String formattedDate = targetFormat.format(date);  // 20120821

        String[] components = formattedDate.split("\\-");

        if (language.matches("ar") || language.equals("ar") || language == "ar") {
            int year = Integer.parseInt(components[2]);
            int month = Integer.parseInt(components[1]);
            int day = Integer.parseInt(components[0]);
            LocalDate birthdate = new LocalDate(year, month, day);          //Birth date
            LocalDate now = new LocalDate();                    //Today's date
            Period period = new Period(birthdate, now, PeriodType.yearMonthDay());
            return period.getYears();

        } else {
            int year = Integer.parseInt(components[0]);
            int month = Integer.parseInt(components[1]);
            int day = Integer.parseInt(components[2]);
            LocalDate birthdate = new LocalDate(year, month, day);          //Birth date
            LocalDate now = new LocalDate();                    //Today's date
            Period period = new Period(birthdate, now, PeriodType.yearMonthDay());
            return period.getYears();

        }

    }

    public static String getAge_FollowUp(String s, Context context) {
//        CustomLog.d(TAG, "getAge_FollowUp: s: " + s);
//        CustomLog.d("TAG", "getAge_FollowUp: s : " + s);
        if (s == null) return "";
        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date = null;
        try {
            date = originalFormat.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String formattedDate = targetFormat.format(date);  // 20120821
        String[] components = formattedDate.split("\\-");

        int year = Integer.parseInt(components[2]);
        int month = Integer.parseInt(components[1]);
        int day = Integer.parseInt(components[0]);

        LocalDate birthdate = new LocalDate(year, month, day);          //Birth date
        LocalDate now = new LocalDate();                    //Today's date
        Period period = new Period(birthdate, now, PeriodType.yearMonthDay());
        String age = "";
        String tyears = "", tmonth = "", tdays = "";

       /* if (period.getYears() > 0) {
            tyears = String.valueOf(period.getYears());
        }*/

        if (period.getValue(0) > 0) {  // o index -> years
            tyears = String.valueOf(period.getValue(0));
            age = tyears;
            //CustomLog.d("TAG", "getAge_FollowUp: s : " + age);
        } else {
            age = "0";
        }

        return age;
    }


    //calculate year, month, days from two date
    public static String getAgeInYearMonth(String s, Context context) {
        if (s == null) return "";
        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date = null;
        try {
            date = originalFormat.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String formattedDate = targetFormat.format(date);  // 20120821

        String[] components = formattedDate.split("\\-");

        int year = Integer.parseInt(components[2]);
        int month = Integer.parseInt(components[1]);
        int day = Integer.parseInt(components[0]);

        //call to function to pass this year and month for age mindmaps questions...
        //getAge_Year_Month(year, month, day);

        LocalDate birthdate = new LocalDate(year, month, day);          //Birth date
        LocalDate now = new LocalDate();                    //Today's date
        Period period = new Period(birthdate, now, PeriodType.yearMonthDay());

        String age = "";
        String tyears = "", tmonth = "", tdays = "";
        //String xyears = "", xmonths = "";

        if (period.getYears() > 0) {
            tyears = period.getYears() + " " + context.getResources().getString(R.string.years);
            //xyears = String.valueOf(period.getYears());
        }
        if (period.getMonths() > 0) {
            tmonth = period.getMonths() + " " + context.getResources().getString(R.string.months);
            //xmonths = String.valueOf(period.getMonths());
        }
        if (period.getDays() > 0)
            tdays = period.getDays() + " " + context.getResources().getString(R.string.days);

        age = tyears + " " + tmonth + " " + tdays;

        return age;
    }

    public static String formatAgeInYearsMonthsDate(Context context, int year, int month, int day) {
        String age = "";
        if (year < 1) {
            age = month + " " + context.getResources().getString(R.string.identification_screen_text_months) + " - " + day + " " + context.getResources().getString(R.string.days);
        } else if (year < 3) {
            age = year + " " + context.getResources().getString(R.string.identification_screen_text_years) + " - " + month + " " + context.getResources().getString(R.string.identification_screen_text_months);
        } else {
            age = year + " " + context.getResources().getString(R.string.identification_screen_text_years);
        }
        return age;
    }

    public static String getAgeInYearMonth(String s) {
        if (s == null) return "";
        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date = null;
        try {
            date = originalFormat.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String formattedDate = targetFormat.format(date);  // 20120821

        String[] components = formattedDate.split("\\-");

        int year = Integer.parseInt(components[2]);
        int month = Integer.parseInt(components[1]);
        int day = Integer.parseInt(components[0]);

        LocalDate birthdate = new LocalDate(year, month, day);          //Birth date
        LocalDate now = new LocalDate();                    //Today's date
        Period period = new Period(birthdate, now, PeriodType.yearMonthDay());

        String age = "";
        String tyears = "0", tmonth = "0", tdays = "0";

        if (period.getYears() > 0) tyears = "" + period.getYears();

        if (period.getMonths() > 0) tmonth = "" + period.getMonths();

        if (period.getDays() > 0) tdays = "" + period.getDays();

        age = tyears + " " + tmonth + " " + tdays;

        return age;
    }

    public static String getAgeInYears(String s, Context context) {
        if (s == null) return "";
        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date = null;
        try {
            date = originalFormat.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String formattedDate = targetFormat.format(date);  // 20120821

        String[] components = formattedDate.split("\\-");

        int year = Integer.parseInt(components[2]);
        int month = Integer.parseInt(components[1]);
        int day = Integer.parseInt(components[0]);

        //call to function to pass this year and month for age mindmaps questions...
        //getAge_Year_Month(year, month, day);

        LocalDate birthdate = new LocalDate(year, month, day);          //Birth date
        LocalDate now = new LocalDate();                    //Today's date
        Period period = new Period(birthdate, now, PeriodType.yearMonthDay());

        String age = "";
        String tyears = "";
        //String xyears = "", xmonths = "";

        if (period.getYears() > 0) {
            tyears = period.getYears() + " " + context.getResources().getString(R.string.years);
            //xyears = String.valueOf(period.getYears());
        }
        age = tyears;
        return age;
    }

    public static String getFormatedDateOfBirth(String oldformatteddate) {

        DateFormat originalFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date date = null;
        try {
            date = originalFormat.parse(oldformatteddate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date == null) {
            originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            try {
                date = originalFormat.parse(oldformatteddate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return targetFormat.format(date);
    }

    public static String getFormatedDateOfBirthAsView(String oldformatteddate) {
        CustomLog.e(TAG, "getFormatedDateOfBirthAsView - " + oldformatteddate);
        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
        Date date = null;
        try {
            date = originalFormat.parse(oldformatteddate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String formattedDate = targetFormat.format(date);  // 20120821

        return formattedDate;

    }

    public String currentDateTimeInHome() {
        DateFormat date = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault());
// you can get seconds by adding  "...:ss" to it
        Date todayDate = new Date();
        return date.format(todayDate);
    }

    public String getcurrentDateTime() {
        DateFormat date = new SimpleDateFormat("hh:mm a, dd MMM yyyy", Locale.ENGLISH);
        Date todayDate = new Date();
        return date.format(todayDate);
    }

    public String getcurrentDateTime(String localeCode) {
        DateFormat date = new SimpleDateFormat("hh:mm a, dd MMMM yyyy", Locale.ENGLISH);
        Date todayDate = new Date();
        return date.format(todayDate);
    }

    public static String SimpleDatetoLongFollowupDate(String dateString) {
        String formattedDate = null;
        try {
            DateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
            DateFormat targetFormat = new SimpleDateFormat("dd-MMMM-yyyy", Locale.ENGLISH);
            Date date = originalFormat.parse(dateString);
            formattedDate = targetFormat.format(date);
        } catch (Exception ex) {
            FirebaseCrashlytics.getInstance().recordException(ex);
        }
        return formattedDate;
    }

    public static String SimpleDatetoLongDate(String dateString) {
        String formattedDate = null;
        try {
            DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            DateFormat targetFormat = new SimpleDateFormat("dd-MMMM-yyyy", Locale.ENGLISH);
            Date date = originalFormat.parse(dateString);
            formattedDate = targetFormat.format(date);
        } catch (Exception ex) {
            FirebaseCrashlytics.getInstance().recordException(ex);
        }
        return formattedDate;
    }

    public static int getMonth(String s1) {
        if (s1 == null) return 0;

        String[] components = s1.split("\\-");

        int year = Integer.parseInt(components[0]);
        int month = Integer.parseInt(components[1]);
        int day = Integer.parseInt(components[2]);

//        LocalDate birthdate1 = new LocalDate(year, month + 1, day);          //Birth date
        LocalDate birthdate1 = new LocalDate(year, month, day);          //Birth date
        LocalDate now = new LocalDate();                    //Today's date
        Period period = new Period(birthdate1, now, PeriodType.yearMonthDay());
        return period.getMonths();
    }

    public static String formatDateFromOnetoAnother(String date, String sourceFormat, String anotherFormat) {
        if (date == null || date.isEmpty()) return "";
        String result = "";
        SimpleDateFormat sdf;
        SimpleDateFormat sdf1;

        try {
            sdf = new SimpleDateFormat(sourceFormat, Locale.ENGLISH);
            sdf1 = new SimpleDateFormat(anotherFormat, Locale.ENGLISH);
            result = sdf1.format(sdf.parse(date));
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            return "";
        } finally {
            sdf = null;
            sdf1 = null;
        }
        return result;
    }

    public static String date_formatter(String dateString, String format, String result_format) {
        String formattedDate = null;
        try {
            DateFormat originalFormat = new SimpleDateFormat(format, Locale.ENGLISH);
            DateFormat targetFormat = new SimpleDateFormat(result_format, Locale.ENGLISH);
            Date date = originalFormat.parse(dateString);
            formattedDate = targetFormat.format(date);
        } catch (Exception ex) {
            FirebaseCrashlytics.getInstance().recordException(ex);
            CustomLog.v("SearchPatient", "date_ex: " + ex);
        }
        return formattedDate;
    }

    /**
     * This function is used to calculate value like Eg: '2 hours ago' or '2 minutes ago'.
     *
     * @param datetime
     * @return
     */
    public static String timeAgoFormat(String datetime) {
        if (datetime == null || datetime.trim().isEmpty()) return "";
        String time = "";

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            long date = format.parse(datetime).getTime();
            long now = System.currentTimeMillis();
            CharSequence ago = DateUtils.getRelativeTimeSpanString(date, now, DateUtils.MINUTE_IN_MILLIS);
            time = String.valueOf(ago);
            CustomLog.v("TimeAgo", "TimeAgo: " + time);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return time;
    }

    public static String parse_DateToddMMyyyy(String time) {
        String inputPattern = "dd-MM-yyyy";
        String outputPattern = "dd MMM yyyy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String parse_DateToddMMyyyy_new(String time) {
        String inputPattern = "yyyy-MM-dd";
        String outputPattern = "dd MMM yyyy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }


    public static String getDisplayDateForApp(String date) {
        String finalDate = "";
        if (date != null && !date.isEmpty()) {

            String[] dateSplit = date.split("-");
            String year = dateSplit[0];
            String month = dateSplit[1];
            String day = dateSplit[2];


            String monthString = "";
            switch (month) {
                case "01":
                    monthString = "Jan";
                    break;
                case "02":
                    monthString = "Feb";
                    break;
                case "03":
                    monthString = "March";
                    break;
                case "04":
                    monthString = "April";
                    break;
                case "05":
                    monthString = "May";
                    break;
                case "06":
                    monthString = "June";
                    break;
                case "07":
                    monthString = "July";
                    break;
                case "08":
                    monthString = "Aug";
                    break;
                case "09":
                    monthString = "Sept";
                    break;
                case "10":
                    monthString = "Oct";
                    break;
                case "11":
                    monthString = "Nov";
                    break;
                case "12":
                    monthString = "Dec";
                    break;

            }

            // finalDate = day + " " + monthString + " " + year;
            finalDate = monthString + " " + day + ", " + year;

        }
        return finalDate;


    }

    public static String getCurrentDateNew() {
        Date cDate = new Date();
        String fDate = new SimpleDateFormat("dd/MM/yyyy").format(cDate);
        CustomLog.d("TAG", "getCurrentDateNew: fDate : " + fDate);
        return fDate;
    }

    public static String getCurrentDateInDDMMYYYYFormat() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        return simpleDateFormat.format(new Date());
    }

    public static String getOneMonthAheadDateInDDMMYYYYFormat() {
        //get date with one month ahead of current date
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        return simpleDateFormat.format(new Date(new Date().getTime() + 30L * 24 * 60 * 60 * 1000));
    }

    public static String getDisplayDateAndTime(String dateTime, Context context) {
        SessionManager sessionManager = new SessionManager(context);
        String language = sessionManager.getAppLanguage();
        CustomLog.d(TAG, "getDisplayDateAndTime: dateTime : " + dateTime);
        String finalDate = "";
        String hourFormated = "";
        String textTime;
        if (dateTime != null && !dateTime.isEmpty()) {

            String[] splitedString = dateTime.split("\\s+");
            String[] splitedTime = splitedString[1].split(":");
            if (Integer.parseInt(splitedTime[0]) >= 12) {
                hourFormated = getTwelveHourFormat(splitedTime[0]);
                textTime = "pm";
            } else {
                hourFormated = splitedTime[0];
                textTime = "am";
            }
            String timeDisplay = hourFormated + ":" + splitedTime[1] + " " + textTime;
            String displayDate = getDateWithDayAndMonth(splitedString[0]);
            finalDate = displayDate + ", at " + timeDisplay;
            if (language.equalsIgnoreCase("hi"))
                finalDate = displayDate + ", " + timeDisplay + " " + context.getString(R.string.at);
        }
        return finalDate;


    }

    public static String getDisplayDateAndTimeFromDDMMFormat(String inputDate) {
        String finalDate = "";
        String hourFormated = "";
        String textTime;
        if (inputDate != null && !inputDate.isEmpty()) {

            String[] splitedString = inputDate.split("/");
            String[] splitedTime = splitedString[1].split(":");
            if (Integer.parseInt(splitedTime[0]) > 12) {
                hourFormated = getTwelveHourFormat(splitedTime[0]);
                textTime = "pm";
            } else {
                hourFormated = splitedTime[0];
                textTime = "am";
            }
            String timeDisplay = hourFormated + ":" + splitedTime[1] + " " + textTime;
            String displayDate = getDateWithDayAndMonth(splitedString[0]);
            finalDate = displayDate + ", at " + timeDisplay;
        }
        return finalDate;


    }

    public static String getTwelveHourFormat(String hour) {
        String hourString = "";
        switch (hour) {
            case "12":
                hourString = "12";
                break;
            case "13":
                hourString = "01";
                break;
            case "14":
                hourString = "02";
                break;
            case "15":
                hourString = "03";
                break;
            case "16":
                hourString = "04";
                break;
            case "17":
                hourString = "05";
                break;
            case "18":
                hourString = "06";
                break;
            case "19":
                hourString = "07";
                break;
            case "20":
                hourString = "08";
                break;
            case "21":
                hourString = "09";
                break;
            case "22":
                hourString = "10";
                break;
            case "23":
                hourString = "11";
                break;
            case "24":
                hourString = "12";
                break;

        }
        return hourString;
    }

    public static String getDateWithDayAndMonth(String date) {
        String finalDate = "";
        if (date != null && !date.isEmpty()) {

            String[] dateSplit = date.split("-");
            String year = dateSplit[0];
            String month = dateSplit[1];
            String day = dateSplit[2];


            String monthString = "";
            switch (month) {
                case "01":
                    monthString = "Jan";
                    break;
                case "02":
                    monthString = "Feb";
                    break;
                case "03":
                    monthString = "March";
                    break;
                case "04":
                    monthString = "April";
                    break;
                case "05":
                    monthString = "May";
                    break;
                case "06":
                    monthString = "June";
                    break;
                case "07":
                    monthString = "July";
                    break;
                case "08":
                    monthString = "Aug";
                    break;
                case "09":
                    monthString = "Sept";
                    break;
                case "10":
                    monthString = "Oct";
                    break;
                case "11":
                    monthString = "Nov";
                    break;
                case "12":
                    monthString = "Dec";
                    break;

            }

            finalDate = day + " " + monthString;


        }
        return finalDate;


    }

    public static String getDateWithDayAndMonthFromDDMMFormat(String date) {
        CustomLog.d(TAG, "getDateWithDayAndMonthFromDDMMFormat: date : " + date);
        String finalDate = "";
        if (date != null && !date.isEmpty()) {
            String[] dateSplit = date.split("/");
            String month = dateSplit[1];
            String day = dateSplit[0];
            if (!month.isEmpty() && month.length() == 1) {
                month = "0" + month;
            }
            String monthString = "";
            switch (month) {
                case "01":
                    monthString = "Jan";
                    break;
                case "02":
                    monthString = "Feb";
                    break;
                case "03":
                    monthString = "March";
                    break;
                case "04":
                    monthString = "April";
                    break;
                case "05":
                    monthString = "May";
                    break;
                case "06":
                    monthString = "June";
                    break;
                case "07":
                    monthString = "July";
                    break;
                case "08":
                    monthString = "Aug";
                    break;
                case "09":
                    monthString = "Sept";
                    break;
                case "10":
                    monthString = "Oct";
                    break;
                case "11":
                    monthString = "Nov";
                    break;
                case "12":
                    monthString = "Dec";
                    break;

            }

            finalDate = day + " " + monthString;
        }
        return finalDate;


    }

    public static String[] getMonthAndYearFromGivenDate(String date) {
        String[] result = new String[0];
        if (date != null && !date.isEmpty()) {

            String[] dateSplit = date.split("/");
            String month = dateSplit[1];
            String day = dateSplit[0];
            String year = dateSplit[2];


            String monthString = "";
            switch (month) {
                case "01":
                    monthString = "January";
                    break;
                case "02":
                    monthString = "February";
                    break;
                case "03":
                    monthString = "March";
                    break;
                case "04":
                    monthString = "April";
                    break;
                case "05":
                    monthString = "May";
                    break;
                case "06":
                    monthString = "June";
                    break;
                case "07":
                    monthString = "July";
                    break;
                case "08":
                    monthString = "August";
                    break;
                case "09":
                    monthString = "September";
                    break;
                case "10":
                    monthString = "October";
                    break;
                case "11":
                    monthString = "November";
                    break;
                case "12":
                    monthString = "December";
                    break;

            }
            result = new String[]{monthString, year};
        }
        return result;


    }

    public static String getDateInDDMMMMYYYYFormat(String inputDate) {
        String dateFormatted = "";
        //input date must be in dd/mm/yyyy format
        if (inputDate != null && !inputDate.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date d = null;
            try {
                d = sdf.parse(inputDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            SimpleDateFormat sdf2 = new SimpleDateFormat("dd MMMM, yyyy");
            dateFormatted = sdf2.format(d);

        }
        return dateFormatted;
    }

    public static String convertDateToYyyyMMddFormat(String dateToConvert) {
        CustomLog.d(TAG, "convertDateToYyyyMMddFormat: dateToConvert : " + dateToConvert);

        java.text.DateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
        // java.text.DateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy"); //gives month name
        java.text.DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");

        Date date = null;
        try {
            date = inputFormat.parse(dateToConvert);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return outputFormat.format(date);
    }

    @NonNull
    public static String convertDateToDdMmYyyyHhMmFormat(String dateToConvert, String time) {
        CustomLog.d(TAG, "convertDateToYyyyMMddFormat: dateToConvert : " + dateToConvert);

        java.text.DateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
        // java.text.DateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy"); //gives month name
        java.text.DateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy, HH:mm");

        Date date = null;
        try {
            date = inputFormat.parse(dateToConvert + " " + time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return outputFormat.format(date);
    }

    public static String getTodaysDateInRequiredFormat(String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.ENGLISH);
        return simpleDateFormat.format(new Date());
    }

    public static String getTodaysDateInRequiredFormat(String format, String localeCode) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, new Locale(localeCode));
        return simpleDateFormat.format(new Date());
    }

    public static String getYesterdaysDateInRequiredFormat(String format, String localeCode) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, new Locale(localeCode));
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return simpleDateFormat.format(cal.getTime());
    }

    public static Date convertStringToDateObject(String date, String format, String localeCode) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, new Locale(localeCode));
        Date parsedDate = null;

        try {
            parsedDate = simpleDateFormat.parse(date);
        } catch (ParseException exception) {
            exception.printStackTrace();
        }

        return parsedDate;
    }

    public static Calendar convertStringToCalendarObject(String date, String format, String localeCode) {
        Calendar calendar = Calendar.getInstance();
        Date parsedDate = convertStringToDateObject(date, format, localeCode);
        if (parsedDate != null) {
            calendar.setTime(parsedDate);
        }
        return calendar;
    }

    public static Calendar convertStringToCalendarObjectMinusOne(String date, String format, String localeCode) {
        Calendar calendar = Calendar.getInstance();
        Date parsedDate = convertStringToDateObject(date, format, localeCode);
        if (parsedDate != null) {
            calendar.setTime(parsedDate);
            calendar.add(Calendar.DATE, -1);
        }
        return calendar;
    }


    // method returns the 12 A.M. time of the current day in milliseconds
    public static long getTodaysDateInMilliseconds() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int date = cal.get(Calendar.DATE);
        cal.clear();
        cal.set(year, month, date);
        return cal.getTimeInMillis();
    }

    public static long getEndDateInMilliseconds(String date, String format, String localeCode) {
        Calendar calendar = Calendar.getInstance();
        Date parsedDate = convertStringToDateObject(date, format, localeCode);

        if (parsedDate != null) {
            calendar.setTime(parsedDate);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DATE);
            calendar.clear();
            calendar.set(year, month, day, 23, 59, 59);
        }

        return calendar.getTimeInMillis();
    }

    public static long convertStringDateToMilliseconds(String date, String format, String localeCode) {
        Calendar calendarObject = convertStringToCalendarObject(date, format, localeCode);
        return calendarObject.getTimeInMillis();
    }

    public static String convertDateObjectToString(Date date, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.ENGLISH);
        return simpleDateFormat.format(date);
    }

    public static boolean isGivenDateBetweenTwoDates(String date, String startDate, String endDate, String format, String localeCode) {
        Date createdDateObject = convertStringToDateObject(date, format, localeCode);
        Date startDateObject = convertStringToDateObject(startDate, format, localeCode);
        Date endDateObject = convertStringToDateObject(endDate, format, localeCode);
        if (createdDateObject == null || startDateObject == null || endDateObject == null)
            return false;
        return createdDateObject.getTime() >= startDateObject.getTime() && createdDateObject.getTime() <= endDateObject.getTime();
    }

    public static String convertMillisecondsToHoursAndMinutes(long timeInMilliseconds) {
        int minutes = (int) ((timeInMilliseconds / (1000 * 60)) % 60);
        int hours = (int) ((timeInMilliseconds / (1000 * 60 * 60)) % 24);
        return String.format(Locale.ENGLISH, "%dh %dm", hours, minutes);
    }

    public static String[] findDateFromStringDDMMMYYY(String str) {
        //23-JUN-1996
        String strPattern = "\\d{2}['/']\\w{3}['/']\\d{4}";
        return findDate(str, strPattern);
    }

    public static String[] findDateFromStringDDMMYYY(String str) {
        //23-JUN-1996
        String strPattern = "\\d{2}['/']\\d{2}['/']\\d{4}";
        return findDate(str, strPattern);
    }

    public static String[] findDate(String str, String strPattern) {
        //23-JUN-1996
        CustomLog.v("UTILS", "findDate - " + str);
        str = str.replaceAll("<.*?>", "");
        CustomLog.v("UTILS", "findDate celan html- " + str);
        String result = "";
        Pattern pattern = Pattern.compile(strPattern);
        Matcher matcher = pattern.matcher(str);
        StringBuilder stringBuilder = new StringBuilder();
        while (matcher.find()) {
            if (!stringBuilder.toString().isEmpty()) stringBuilder.append(",");
            stringBuilder.append(matcher.group());
        }
        result = stringBuilder.toString();
        CustomLog.v("UTILS", "findDate - " + result);
        if (!result.isEmpty()) {
            return result.split(",");
        } else {
            return null;
        }

    }

    public static String formatInLocalDateForDDMMMYYYY(String inputDate, String localeCode) {
        String dateFormatted = "";
        CustomLog.v("UTILS", "formatInLocalDateForDDMMMYYYY - " + inputDate);
        //input date must be in dd/mm/yyyy format
        if (inputDate != null && !inputDate.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy", new Locale("en"));
            Date d = null;
            try {
                d = sdf.parse(inputDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MMM/yyyy", new Locale(localeCode));
            dateFormatted = sdf2.format(d);

        }
        CustomLog.v("UTILS", "formatInLocalDateForDDMMMYYYY - " + dateFormatted);
        return dateFormatted;
    }

    public static boolean isCurrentDateBeforeFollowUpDate(String followUpDate, String followUpDateFormat) {
        Date currentTime = new Date();
        try {
            Date parseFollowUpDate = new SimpleDateFormat(followUpDateFormat, Locale.ENGLISH).parse(followUpDate);
            return currentTime.before(parseFollowUpDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String extractDateFromString(String followUpString) {
        String result = "";
        String regex = "(\\d{4}-\\d{2}-\\d{2})";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(followUpString);
        while (matcher.find()) {
            result = matcher.group();
        }
        return result;
    }

    public static boolean isDateInCurrentWeek(Date date) {
        Calendar currentCalendar = Calendar.getInstance();
        int week = currentCalendar.get(Calendar.WEEK_OF_YEAR);
        int year = currentCalendar.get(Calendar.YEAR);
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.setTime(date);
        int targetWeek = targetCalendar.get(Calendar.WEEK_OF_YEAR);
        int targetYear = targetCalendar.get(Calendar.YEAR);
        return week == targetWeek && year == targetYear;
    }

    public static Date getCurrentDateWithoutTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        try {
            return formatter.parse(formatter.format(new Date()));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isCurrentDateTimeAfterAppointmentTime(String givenDateTime) {
        boolean isGivenDateTimeGreater;
        Date currentDate = new Date();

        try {
            Date givenDate = new SimpleDateFormat("dd/MM/yyyy h:mm a", Locale.ENGLISH).parse(givenDateTime);
            isGivenDateTimeGreater = currentDate.after(givenDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return isGivenDateTimeGreater;
    }

    public static boolean isBefore(String currentDateStr, String targetDateStr, String dateFormatStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatStr);
        try {
            Date currentDate = dateFormat.parse(currentDateStr);
            Date targetDate = dateFormat.parse(targetDateStr);
            assert currentDate != null;
            return currentDate.before(targetDate);
        } catch (ParseException e) {
            e.printStackTrace();
            // Handle parsing exception
            return false;
        }
    }

    public static boolean isAfter(String currentDateStr, String targetDateStr, String dateFormatStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatStr);
        try {
            Date currentDate = dateFormat.parse(currentDateStr);
            Date targetDate = dateFormat.parse(targetDateStr);
            return currentDate.after(targetDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static long getTimeStampFromString(String dateString, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.US);

        try {
            Date date = dateFormat.parse(dateString);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String parseDateTimeToDateTime(String input) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd, 'Time: 'hh:mm a", Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd 'at' h:mm a", Locale.ENGLISH);

        try {
            Date date = inputFormat.parse(input);
            return outputFormat.format(date);
        } catch (ParseException e) {
            CustomLog.e("ERRRR", e.getMessage() != null ? e.getMessage() : "");
            e.printStackTrace();
        }
        return "";
    }

    public static String getDateTimeFromTimestamp(long currentTimeMillis, String format) {
        SimpleDateFormat simpleFormat = new SimpleDateFormat(format, Locale.ENGLISH);
        Date date = new Date(currentTimeMillis);
        return simpleFormat.format(date);
    }

    public static String currentDateTimeFormat() {
        Locale.setDefault(Locale.ENGLISH);
        DateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
// you can get seconds by adding  "...:ss" to it
        Date todayDate = new Date();
        return date.format(todayDate);
    }

    public static boolean isDateGreaterThan15Years(String selectedDate) {
        // Define the date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            // Parse the selected date
            Date parsedDate = dateFormat.parse(selectedDate);

            // Get the current date
            Calendar currentDate = Calendar.getInstance();

            // Set the selected date in a Calendar instance
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.setTime(parsedDate);

            // Add 15 years to the selected date
            selectedCalendar.add(Calendar.YEAR, 15);

            // Check if the modified selected date is before or after the current date
            return selectedCalendar.before(currentDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return false; // Return false if there's an error parsing the date
        }
    }
}