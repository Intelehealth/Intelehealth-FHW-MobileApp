package org.intelehealth.app.ui2.calendarviewcustom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import org.intelehealth.app.utilities.CustomLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.intelehealth.app.R;
import org.intelehealth.app.utilities.DateAndTimeUtils;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.ToastUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class CustomCalendarViewUI2 extends DialogFragment {
    public static final String TAG = "CustomCalendarViewUI2";
    RecyclerView rvCalendarView;
    Spinner spinnerMonths, spinnerYear;
    CalendarViewMonthModel spinnerSelectedMonthModel;
    CalendarviewYearModel spinnerSelectedYearModel;
    Calendar calendarInstanceDefault;
    int monthTotalDays;
    ImageView ivPrevMonth, ivNextMonth;
    String yearToCompare = "";
    String monthToCompare = "";
    int currentMonth;
    int currentYear;
    Context context;
    String selectedDate = "";
    private SendSelectedDateInterface listener;
    int MY_REQUEST_CODE = 5555;
    String whichDate = "";
    List<CalendarviewYearModel> yearsList;

    public CustomCalendarViewUI2(Context context) {
        this.context = context;
    }

    public CustomCalendarViewUI2(Context context, SendSelectedDateInterface listener) {
        this.context = context;
        this.listener = listener;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public AlertDialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle mArgs = getArguments();
        assert mArgs != null;
        whichDate = mArgs.getString("whichDate");
        CustomLog.d(TAG, "onCreateDialog:whichDate :  " + whichDate);
        AlertDialog alertDialog = showDatePicker(context, "");
        return alertDialog;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getPreviousMonthDates() {
        calendarInstanceDefault.add(Calendar.MONTH, -1);
        Date monthNameNEw = calendarInstanceDefault.getTime();
        Date date = null;
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy");
        try {
            date = formatter.parse(monthNameNEw.toString());
            String formateDate = new SimpleDateFormat("dd/MM/yyyy").format(date);

            String[] dateSplit = formateDate.split("/");
            yearToCompare = dateSplit[2];
            monthToCompare = dateSplit[1];
            String[] monthYear = DateAndTimeUtils.getMonthAndYearFromGivenDate(formateDate);

            if (monthYear.length > 0) {
                String selectedPrevMonth = monthYear[0];
                String selectedPrevMonthYear = monthYear[1];
                //tvSelectedMonthYear.setText(selectedPrevMonth + ", " + selectedPrevMonthYear);
                CustomLog.d(TAG, "getPreviousMonthDates: tvSelectedMonthYear : " + selectedPrevMonth + ", " + selectedPrevMonthYear);
                if (monthToCompare.equals(String.valueOf(currentMonth)) && yearToCompare.equals(String.valueOf(currentYear))) {
                    // enableDisablePreviousButton(false);

                    spinnerSelectedYearModel = new CalendarviewYearModel(Integer.parseInt(yearToCompare), true);
                    spinnerSelectedMonthModel = new CalendarViewMonthModel("", Integer.parseInt(monthToCompare), true);
                    setValuesToTheYearSpinnerForDefault(Integer.parseInt(yearToCompare));
                    setValuesToTheMonthSpinnerForDefault(Integer.parseInt(monthToCompare));
                    fillDatesMonthsWise("prevButton");
                } else {
                    spinnerSelectedYearModel = new CalendarviewYearModel(Integer.parseInt(yearToCompare), true);
                    spinnerSelectedMonthModel = new CalendarViewMonthModel("", Integer.parseInt(monthToCompare), true);
                    setValuesToTheYearSpinnerForDefault(Integer.parseInt(yearToCompare));
                    setValuesToTheMonthSpinnerForDefault(Integer.parseInt(monthToCompare));
                    fillDatesMonthsWise("prevButton");
                }

            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void setValuesToTheMonthSpinnerForDefault(int currentMonth) {
        switch (currentMonth) {
            case 1:
                spinnerMonths.setSelection(0, true);
                break;
            case 2:
                spinnerMonths.setSelection(1, true);
                break;
            case 3:
                spinnerMonths.setSelection(2, true);
                break;
            case 4:
                spinnerMonths.setSelection(3, true);
                break;
            case 5:
                spinnerMonths.setSelection(4, true);
                break;
            case 6:
                spinnerMonths.setSelection(5, true);
                break;
            case 7:
                spinnerMonths.setSelection(6, true);
                break;
            case 8:
                spinnerMonths.setSelection(7, true);
                break;
            case 9:
                spinnerMonths.setSelection(8, true);
                break;
            case 10:
                spinnerMonths.setSelection(9, true);
                break;
            case 11:
                spinnerMonths.setSelection(10, true);
                break;
            case 12:
                spinnerMonths.setSelection(11, true);
                break;
        }

    }

    private void setValuesToTheYearSpinnerForDefault(int currentYear) {
        CustomLog.d(TAG, "setValuesToTheYearSpinnerForDefault: currentYear : " + currentYear);

        for (int i = 0; i < yearsList.size(); i++) {
            CalendarviewYearModel calendarviewYearModel = yearsList.get(i);
            if (currentYear == calendarviewYearModel.getYear()) {
                spinnerYear.setSelection(i, true);
                break;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void fillDatesMonthsWise(String tag) {
        if (!tag.equals("default")) {
            selectedDate = "";
        }

        if (spinnerSelectedYearModel.getYear() == currentYear && spinnerSelectedMonthModel.getMonthNo() == currentMonth) {
            // enableDisablePreviousButton(false);
        } else {
            //  enableDisablePreviousButton(true);

        }
        String firstDay = "";
        String lastDay = "";

        // 1. get previous months all dates
        // 2. get current months all dates
        // 3. get next months all dates
        // Logic  - total 42 entries must be in recyclerview
        // start day  - end day

        //  String spinnerSelectedYear = spinnerYear.getSelectedItem().toString();
        //  String spinnerSelectedMonth = spinnerMonths.getSelectedItem().toString();

        if (spinnerSelectedMonthModel != null && spinnerSelectedYearModel != null) {
            CustomLog.d(TAG, "fillDatesMonthsWise: month : " + spinnerSelectedMonthModel.getMonthNo());
            CustomLog.d(TAG, "fillDatesMonthsWise: year : " + spinnerSelectedYearModel.getYear());

            YearMonth ym = YearMonth.of(spinnerSelectedYearModel.getYear(), spinnerSelectedMonthModel.getMonthNo());


            firstDay = (ym.atDay(1).getDayOfWeek().name()).substring(0, 2);
            lastDay = (ym.atEndOfMonth().getDayOfWeek().name()).substring(0, 2);


            //maximum 42 entries
            int headerDayPosition = 111;
            int headerDayPositionForLastDay = 222;
            int noOfPrevMonthDaysRequired = 0;
            int noOfNextMonthDaysRequired = 0;

            switch (firstDay.toLowerCase()) {
                case "mo":
                    headerDayPosition = 0;
                    noOfPrevMonthDaysRequired = 0;
                    break;
                case "tu":
                    headerDayPosition = 1;
                    noOfPrevMonthDaysRequired = 1;

                    break;
                case "we":
                    headerDayPosition = 2;
                    noOfPrevMonthDaysRequired = 2;

                    break;
                case "th":
                    headerDayPosition = 3;
                    noOfPrevMonthDaysRequired = 3;

                    break;
                case "fr":
                    headerDayPosition = 4;
                    noOfPrevMonthDaysRequired = 4;

                    break;
                case "sa":
                    headerDayPosition = 5;
                    noOfPrevMonthDaysRequired = 5;

                    break;
                case "su":
                    headerDayPosition = 6;
                    noOfPrevMonthDaysRequired = 6;

                    break;

            }

            switch (lastDay.toLowerCase()) {
                case "mo":
                    headerDayPositionForLastDay = 0;
                    noOfNextMonthDaysRequired = 6;
                    break;
                case "tu":
                    headerDayPositionForLastDay = 1;
                    noOfNextMonthDaysRequired = 5;

                    break;
                case "we":
                    headerDayPositionForLastDay = 2;
                    noOfNextMonthDaysRequired = 4;

                    break;
                case "th":
                    headerDayPositionForLastDay = 3;
                    noOfNextMonthDaysRequired = 3;

                    break;
                case "fr":
                    headerDayPositionForLastDay = 4;
                    noOfNextMonthDaysRequired = 2;

                    break;
                case "sa":
                    headerDayPositionForLastDay = 5;
                    noOfNextMonthDaysRequired = 1;

                    break;
                case "su":
                    headerDayPositionForLastDay = 6;
                    noOfNextMonthDaysRequired = 0;
                    break;

            }
            CustomLog.d(TAG, "fillDatesMonthsWise: spinnerSelectedYearModel : " + spinnerSelectedYearModel.getYear());
            CustomLog.d(TAG, "fillDatesMonthsWise: spinnerSelectedMonthModel : " + spinnerSelectedMonthModel.getMonthNo());

            //calculate total days for recyclerview
            int totalViewDays = monthTotalDays + noOfPrevMonthDaysRequired;
            int daysFromNextMonth = 42 - totalViewDays;

            List<CalendarviewModel> listOfPrevMonthDays = new ArrayList<>();
            List<CalendarviewModel> listOfDates = new ArrayList<>();
            List<CalendarviewModel> listOfNextMonthDays = new ArrayList<>();

            CalendarviewModel calendarviewModel;
            Calendar calForSelectedMonth = Calendar.getInstance();
            calForSelectedMonth.set(spinnerSelectedYearModel.getYear(), spinnerSelectedMonthModel.getMonthNo(), 1);//for selected
            calForSelectedMonth.add(Calendar.MONTH, -1);


            Calendar calForPrevMonth = Calendar.getInstance();
            calForPrevMonth.set(spinnerSelectedYearModel.getYear(), spinnerSelectedMonthModel.getMonthNo(), 1);//2022/11/1

            calForPrevMonth.add(Calendar.MONTH, -2);


            int prevMonthMaxDays = calForPrevMonth.getActualMaximum(Calendar.DAY_OF_MONTH);


            if (noOfPrevMonthDaysRequired > 0) {
                for (int i = prevMonthMaxDays; i >= 1; i--) {
                    calendarviewModel = new CalendarviewModel(i, 111, headerDayPositionForLastDay, false, true, false, false, spinnerSelectedMonthModel.getMonthNo(), spinnerSelectedYearModel.getYear());
                    listOfPrevMonthDays.add(calendarviewModel);
                    if (listOfPrevMonthDays.size() == noOfPrevMonthDaysRequired) {
                        break;
                    }
                }

                Collections.reverse(listOfPrevMonthDays);
                listOfDates.addAll(listOfPrevMonthDays);
            }


            //  int monthTotalDays = calForSelectedMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
            monthTotalDays = calForSelectedMonth.getActualMaximum(Calendar.DAY_OF_MONTH);

            int currentDay = calendarInstanceDefault.get(Calendar.DAY_OF_MONTH);  //today from default instance
            int selectedMonth = spinnerSelectedMonthModel.getMonthNo();
            int selectedYear = spinnerSelectedYearModel.getYear();


            for (int i = 1; i <= monthTotalDays; i++) {

                if ((selectedMonth == currentMonth && selectedYear == currentYear && i < currentDay) || (selectedYear == currentYear && selectedMonth < currentMonth)) {
                    calendarviewModel = new CalendarviewModel(i, headerDayPosition, headerDayPositionForLastDay, false, false, false, true, spinnerSelectedMonthModel.getMonthNo(), spinnerSelectedYearModel.getYear());

                } else {
                    calendarviewModel = new CalendarviewModel(i, headerDayPosition, headerDayPositionForLastDay, false, false, false, false, spinnerSelectedMonthModel.getMonthNo(), spinnerSelectedYearModel.getYear());

                }
                listOfDates.add(calendarviewModel);
            }


            Calendar calForNextMonth = Calendar.getInstance();
            calForNextMonth.set(spinnerSelectedYearModel.getYear(), spinnerSelectedMonthModel.getMonthNo(), 1);
            calForNextMonth.add(Calendar.MONTH, +1);


            int nextMonthMaxDays = calForNextMonth.getActualMaximum(Calendar.DAY_OF_MONTH);

            int noOfNextMonthDaysRequiredNew = 42 - listOfDates.size();

            if (noOfNextMonthDaysRequiredNew > 0) {
                for (int i = 1; i <= nextMonthMaxDays; i++) {
                    calendarviewModel = new CalendarviewModel(i, 111, headerDayPositionForLastDay, false, false, true, false, spinnerSelectedMonthModel.getMonthNo(), spinnerSelectedYearModel.getYear());
                    listOfNextMonthDays.add(calendarviewModel);
                    if (listOfNextMonthDays.size() == noOfNextMonthDaysRequiredNew) {
                        break;
                    }
                }
            }


            listOfDates.addAll(listOfNextMonthDays);

            rvCalendarView.setHasFixedSize(true);
            rvCalendarView.setLayoutManager(new GridLayoutManager(context, 7));
            rvCalendarView.setAdapter(new CalendarviewNewAdapter(context, listOfDates, calendarModel1 -> {
                int date = calendarModel1.getDate();
                int month = calendarModel1.getSelectedMonth();
                int year = calendarModel1.getSelectedYear();

                if (calendarModel1.isPrevMonth || calendarModel1.isNextMonth || calendarModel1.isCurrentMonthCompletedDate()) {
                    //for previous dates
                    selectedDate = date + "/" + month + "/" + year;
                } else {
                    //for upcoming dates
                    selectedDate = date + "/" + month + "/" + year;
                }
                CustomLog.d(TAG, "selected from adapter fillDatesMonthsWise: selectedDate : " + selectedDate);
            }, whichDate));


        } else {
            CustomLog.d(TAG, "fillDatesMonthsWise: models are null");
        }


    }

    private void fillMonthsSpinner(Context context) {

        List<CalendarViewMonthModel> monthsList = new ArrayList<>();
        String[] monthsArray = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        SessionManager sessionManager = new SessionManager(context);
        if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")){
            monthsArray = new String[]{"जनवरी", "फ़रवरी", "मार्च", "अप्रैल", "मई", "जून", "जुलाई", "अगस्त", "सितंबर", "अक्टूबर", "नवंबर", "दिसंबर"};
        }else if(sessionManager.getAppLanguage().equalsIgnoreCase("ru")){
            monthsArray = new String[]{"Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};
        }

        int[] monthsNoArray = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        for (int i = 0; i < monthsArray.length; i++) {
            CalendarViewMonthModel model1 = new CalendarViewMonthModel(monthsArray[i], monthsNoArray[i], false);
            monthsList.add(model1);
        }

        MonthsSpinnerAdapter adapter = new MonthsSpinnerAdapter(context, R.layout.custom_spinner_text_calview_ui2, monthsList);
        adapter.setDropDownViewResource(R.layout.ui2_custome_dropdown_item_view);
        spinnerMonths.setPopupBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.popup_menu_background));
        spinnerMonths.setAdapter(adapter);

        spinnerMonths.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                spinnerSelectedMonthModel = adapter.getItem(position);
                ((TextView) adapterView.getChildAt(0)).setTextColor(ContextCompat.getColor(view.getContext(), R.color.colorPrimary));
                spinnerMonths.setBackground(ContextCompat.getDrawable(context, R.drawable.spinner_cal_view_bg_selected));
                ((TextView) adapterView.getChildAt(0)).setTypeface(((TextView) adapterView.getChildAt(0)).getTypeface(), Typeface.BOLD);

                fillDatesMonthsWise("fromSpinnerMonth");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapter) {
            }
        });
    }

    public class MonthsSpinnerAdapter extends ArrayAdapter<CalendarViewMonthModel> {

        private Context context;
        List<CalendarViewMonthModel> listOfMonths;

        public MonthsSpinnerAdapter(Context context, int textViewResourceId, List<CalendarViewMonthModel> listOfMonths) {
            super(context, textViewResourceId, listOfMonths);
            this.context = context;
            this.listOfMonths = listOfMonths;
        }

        @Override
        public int getCount() {
            return listOfMonths.size();
        }

        @Override
        public CalendarViewMonthModel getItem(int position) {
            return listOfMonths.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView label = (TextView) super.getView(position, convertView, parent);
            label.setTextColor(Color.BLACK);
            label.setText(listOfMonths.get(position).getMonthName());

            return label;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView label = (TextView) super.getDropDownView(position, convertView, parent);
            label.setTextColor(Color.BLACK);
            label.setText(listOfMonths.get(position).getMonthName());

            return label;
        }
    }


    private void fillYearSpinner() {

        yearsList = new ArrayList<>();
        int[] yearArray = {2018, 2019, 2020, 2021, 2022, 2023, 2024};
        CalendarviewYearModel model1 = null;
        int startYear = currentYear - 100;
        int totalNumberOfYears = currentYear - startYear;

        for (int i = 0; i < totalNumberOfYears + 1; i++) {
            if (startYear == currentYear) {
                model1 = new CalendarviewYearModel(startYear, true);

            } else {
                model1 = new CalendarviewYearModel(startYear, false);

            }
            yearsList.add(model1);
            startYear = startYear + 1;
        }


        YearSpinnerAdapter adapter = new YearSpinnerAdapter(context, R.layout.custom_spinner_text_calview_ui2, yearsList);
        adapter.setDropDownViewResource(R.layout.ui2_custome_dropdown_item_view);
        spinnerYear.setPopupBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.popup_menu_background));

        spinnerYear.setAdapter(adapter);

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                spinnerSelectedYearModel = adapter.getItem(position);
                //spinnerYear.setBackground(getResources().getDrawable(R.drawable.spinner_cal_view_bg_selected));
                ((TextView) adapterView.getChildAt(0)).setTextColor(ContextCompat.getColor(view.getContext(), R.color.colorPrimary));
                ((TextView) adapterView.getChildAt(0)).setTypeface(((TextView) adapterView.getChildAt(0)).getTypeface(), Typeface.BOLD);

                fillDatesMonthsWise("fromSpinnerYear");


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapter) {
            }
        });
    }

    public class YearSpinnerAdapter extends ArrayAdapter<CalendarviewYearModel> {

        private Context context;
        List<CalendarviewYearModel> listOfYears;

        public YearSpinnerAdapter(Context context, int textViewResourceId, List<CalendarviewYearModel> listOfYears) {
            super(context, textViewResourceId, listOfYears);
            this.context = context;
            this.listOfYears = listOfYears;
        }

        @Override
        public int getCount() {
            return listOfYears.size();
        }

        @Override
        public CalendarviewYearModel getItem(int position) {
            return listOfYears.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView label = (TextView) super.getView(position, convertView, parent);
            label.setTextColor(Color.BLACK);
            label.setText(listOfYears.get(position).getYear() + "");

            return label;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView label = (TextView) super.getDropDownView(position, convertView, parent);
            label.setTextColor(Color.BLACK);
            label.setText(listOfYears.get(position).getYear() + "");

            return label;
        }
    }

  /*  private void enableDisablePreviousButton(boolean wantToEnable) {
        if (wantToEnable) {
            ivPrevMonth.setEnabled(true);
            ivPrevMonth.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);

        } else {
            ivPrevMonth.setEnabled(false);
            ivPrevMonth.setColorFilter(ContextCompat.getColor(this, R.color.font_black_3), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }*/

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getNextMonthDates() {
        // enableDisablePreviousButton(true);

        calendarInstanceDefault.add(Calendar.MONTH, 1);
        Date monthNameNEw = calendarInstanceDefault.getTime();
        Date date = null;
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy");
        try {
            date = formatter.parse(monthNameNEw.toString());
            String formateDate = new SimpleDateFormat("dd/MM/yyyy").format(date);

            String[] monthYear = DateAndTimeUtils.getMonthAndYearFromGivenDate(formateDate);
            String selectedNextMonth;
            String selectedMonthYear;

            if (monthYear.length > 0) {
                selectedNextMonth = monthYear[0];
                selectedMonthYear = monthYear[1];
                String[] dateSplit = formateDate.split("/");

                //tvSelectedMonthYear.setText(selectedNextMonth + ", " + selectedMonthYear);
                CustomLog.d(TAG, "getNextMonthDates: tvSelectedMonthYear : " + selectedNextMonth + ", " + selectedMonthYear);
                CustomLog.d(TAG, "getNextMonthDates: currentMonth : " + currentMonth);
                CustomLog.d(TAG, "getNextMonthDates: currentYear : " + currentYear);
                CustomLog.d(TAG, "getNextMonthDates: selectedMonthYear : " + selectedMonthYear);
                CustomLog.d(TAG, "getNextMonthDates: selectedNextMonth : " + selectedNextMonth);
                CustomLog.d(TAG, "getNextMonthDates: selectedNextMonthnew : " + dateSplit[1]);

                if (dateSplit[1].equals(String.valueOf(currentMonth)) && selectedMonthYear.equals(String.valueOf(currentYear))) {
                    // getAllDatesOfSelectedMonth(calendarInstance, true, selectedNextMonth, selectedMonthYear, dateSplit[1]);
                    spinnerSelectedYearModel = new CalendarviewYearModel(Integer.parseInt(selectedMonthYear), true);
                    spinnerSelectedMonthModel = new CalendarViewMonthModel("", Integer.parseInt(dateSplit[1]), true);
                    setValuesToTheYearSpinnerForDefault(Integer.parseInt(selectedMonthYear));
                    setValuesToTheMonthSpinnerForDefault(Integer.parseInt(dateSplit[1]));
                    fillDatesMonthsWise("nextButton");

                } else {
                    spinnerSelectedYearModel = new CalendarviewYearModel(Integer.parseInt(selectedMonthYear), true);
                    spinnerSelectedMonthModel = new CalendarViewMonthModel("", Integer.parseInt(dateSplit[1]), true);
                    setValuesToTheYearSpinnerForDefault(Integer.parseInt(selectedMonthYear));
                    fillDatesMonthsWise("nextButton");
                    setValuesToTheMonthSpinnerForDefault(Integer.parseInt(dateSplit[1]));

                    // getAllDatesOfSelectedMonth(calendarInstance, false, selectedNextMonth, selectedMonthYear, dateSplit[1]);

                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    public AlertDialog showDatePicker(Context context, String whichDate1) {
        MaterialAlertDialogBuilder alertdialogBuilder = new MaterialAlertDialogBuilder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.layout_dialog_custom_calendarview, null);
        alertdialogBuilder.setView(convertView);

        rvCalendarView = convertView.findViewById(R.id.rv_calendarview_new);
        spinnerMonths = convertView.findViewById(R.id.spinner_months_caleview);
        spinnerYear = convertView.findViewById(R.id.spinner_year_caleview);
        ivPrevMonth = convertView.findViewById(R.id.iv_prev_month2);
        ivNextMonth = convertView.findViewById(R.id.iv_next_month2);
        Button btnCancelCalendar = convertView.findViewById(R.id.btn_cancel_calendar);
        Button btnOkCalendar = convertView.findViewById(R.id.btn_okay_calendar);


        ivPrevMonth.setOnClickListener(v -> {
            getPreviousMonthDates();
        });
        ivNextMonth.setOnClickListener(v -> {
            getNextMonthDates();
        });

        calendarInstanceDefault = Calendar.getInstance();
        currentMonth = calendarInstanceDefault.get(Calendar.MONTH) + 1;
        currentYear = calendarInstanceDefault.get(Calendar.YEAR);
        monthTotalDays = calendarInstanceDefault.getActualMaximum(Calendar.DAY_OF_MONTH);
        CustomLog.v(TAG, "currentMonth - " + currentMonth);
        CustomLog.v(TAG, "currentYear - " + currentYear);
        CustomLog.v(TAG, "monthTotalDays - " + monthTotalDays);


        spinnerSelectedYearModel = new CalendarviewYearModel(currentYear, true);
        spinnerSelectedMonthModel = new CalendarViewMonthModel("", currentMonth, true);


        fillMonthsSpinner(context);
        fillYearSpinner();
        setValuesToTheMonthSpinnerForDefault(currentMonth);
        setValuesToTheYearSpinnerForDefault(currentYear);
        fillDatesMonthsWise("default");

        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.calendarview_bg_ui2);
        alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        int width = context.getResources().getDimensionPixelSize(R.dimen.dialog_custom_cal_width);
        alertDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        alertDialog.setCancelable(false);

        btnCancelCalendar.setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        btnOkCalendar.setOnClickListener(v -> {
            //for get the selected date - if calendar view called from activity
            if (TextUtils.isEmpty(selectedDate)) {
                ToastUtil.showShortToast(context, context.getString(R.string.please_select_date));
                return;
            }
            if (listener != null) {
                listener.getSelectedDate(selectedDate, "");
            }
            //for get the selected date - if calendar view called from fragment
            try {
                if (getParentFragmentManager() != null) {
                    //converted intent to bundle to support new method
                    Bundle bundle = new Bundle();
                    bundle.putString("selectedDate", selectedDate);
                    bundle.putString("whichDate", whichDate);
                    getParentFragmentManager().setFragmentResult("requestKey", bundle);
                }
            } catch (Exception e) {
            }
            alertDialog.dismiss();
        });
        alertDialog.show();
        return alertDialog;
    }


}
