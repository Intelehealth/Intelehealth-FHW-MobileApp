package org.intelehealth.app.knowledgeEngine;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.InputFilter;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.ajalt.timberkt.Timber;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.R;
import org.intelehealth.app.activities.complaintNodeActivity.CustomArrayAdapter;
import org.intelehealth.app.activities.questionNodeActivity.QuestionsAdapter;
import org.intelehealth.app.app.IntelehealthApplication;
import org.intelehealth.app.ayu.visit.common.VisitUtils;
import org.intelehealth.app.models.AnswerResult;
import org.intelehealth.app.utilities.CustomLog;
import org.intelehealth.app.utilities.FlavorKeys;
import org.intelehealth.app.utilities.InputFilterMinMax;
import org.intelehealth.app.utilities.ProtocolKeys;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.ihutils.ui.CameraActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by Amal Afroz Alam on 21, April, 2016.
 * Contact me: contact@amal.io
 */
public class Node implements Serializable {

    public static final String ASSOCIATE_SYMPTOMS = "Associated symptoms";
    public static final String NOT_ANSWERED = "Question not answered";
    public static final int TAKE_IMAGE_FOR_NODE = 507;
    public static final String TAG = Node.class.getSimpleName();
    public static final int DIRECT_USER_INPUT_CHILD = 0;
    public static final int CHILD_OPTIONS = 1;
    public static final int CHILD_QUESTION = 2;
    public static String bullet = "\u2022";
    public static String big_bullet = "\u25CF";
    //    public static String bullet_hollow = "\u25CB";
    public static String bullet_hollow = "\u2022";
    public static String bullet_arrow = "\u25BA";
    public static String right_pointing = "\u25BB";
    public static String next_line = "<br/>";
    String space = "\t";
    private String engineVersion;
    private boolean needToHide;
    private String compareDuplicateNode;
    private boolean enableExclusiveOption;
    private boolean isExclusiveOption;
    private boolean havingNestedQuestion;
    private boolean isDataCaptured;
    private boolean isSkipped;
    private boolean isImageUploaded;
    private boolean optional;
    //telugu
    private String id;
    private String text;
    private String display;
    ;
    private String display_oriya;
    private String display_cebuno;
    private String display_gujarati;
    private String display_bengali;
    private String display_tamil;
    private String display_hindi;
    private String display_telugu;
    private String display_kannada;
    private String display_malyalam;
    private String display_marathi;
    private String display_assamese;

    private String display_russian;
    private String language;
    private String choiceType;
    private String inputType;
    private String physicalExams;
    private List<Node> optionsList;
    private String associatedComplaint;
    private String jobAidFile;
    private String jobAidType;
    private String pop_up;
    private String pop_up_hi;
    private String pop_up_or;
    private String gender;
    private String min_age;
    private String max_age;
    private boolean isMultiChoice = false;
    private boolean isExcludedFromMultiChoice = false; //exclude-from-multi-choice
    //for Associated Complaints and medical history only
    private String positiveCondition;
    private String negativeCondition;
    //These are specific for physical exams
    private boolean rootNode;
    private boolean complaint;
    private boolean required = false;
    private boolean terminal;
    private boolean hasAssociations;
    private boolean aidAvailable;
    private boolean selected;
    private boolean subSelected;
    private boolean hasPhysicalExams;
    private boolean hasPopUp;
    private boolean subPopUp;
    private boolean showCalendarHeader = false;


    //• = \u2022, ● = \u25CF, ○ = \u25CB, ▪ = \u25AA, ■ = \u25A0, □ = \u25A1, ► = \u25BA
    private int associated_symptoms = 0;
    private boolean isNoSelected;
    private List<String> imagePathList = new ArrayList<>();
    // will capture the section name wrt the image path in map
    private HashMap<String, String> imagePathListWithSectionTag = new HashMap<String, String>();
    private String imagePath;

    private boolean enableSubmit;

    /**
     * Nodes refer to the structure that is used for a decision tree or mindmap.
     * The knowledgeEngine object is stored in the same structure where the there is a root knowledgeEngine which contains all the sub-nodes.
     * The nodes are also tagged based on the attributes each JSON object shows.
     * <p>
     * Most nodes are single choice questions. Therefore, they can just be clicked and selected.
     * Some nodes may be multi-choice, in which case there must be an attribute within the JSON to dictate that.
     * <p>
     * text - the text that is displayed on the org to user
     * language - the text that is displayed after answering a question
     * differs from the text attribute in that this is the response form of a question
     * inputType - dictates if the knowledgeEngine is something other that choice-based
     * types include: text, number, date, duration, area, range, frequency
     * physicalExams - any physical exams that should be triggered in the application if the knowledgeEngine is selected
     * optionsList - container of sub-nodes of the current knowledgeEngine
     * associatedComplaint - just like the name says
     * jobAidFile - the filename of the job aid
     * should be stored in the physicalExamAssets folder within the org when compiling
     * jobAidType - options are audio, video, or image
     *
     * @param jsonNode A JSON Object of a mindmap should be used here. The object that is generated will hold objects within it.
     */
    public Node(JSONObject jsonNode) {
        try {
            this.engineVersion = jsonNode.optString("engineVersion");
            this.id = jsonNode.getString("id");

            this.isMultiChoice = jsonNode.optBoolean("multi-choice");

            this.isExcludedFromMultiChoice = jsonNode.optBoolean("exclude-from-multi-choice");


            this.text = jsonNode.getString("text");

            this.gender = jsonNode.optString("gender");

            this.min_age = jsonNode.optString("age_min");

            this.max_age = jsonNode.optString("age_max");

            JSONArray optionsArray = jsonNode.optJSONArray("options");
            if (optionsArray == null) {
                this.terminal = true;
            } else {
                this.terminal = false;
                this.optionsList = createOptions(optionsArray);
            }

            this.display = jsonNode.optString("display");
            if (this.display.isEmpty()) {
                this.display = jsonNode.optString("display ");
            }
            if (this.display.isEmpty()) {
                this.display = this.text;
            }

            this.display_gujarati = jsonNode.optString("display-gj");
            if (this.display_gujarati.isEmpty()) {
                this.display_gujarati = jsonNode.optString("display-gj");
            }
            if (this.display_gujarati.isEmpty()) {
                this.display_gujarati = this.display;
            }

            this.display_bengali = jsonNode.optString("display-bn");
            if (this.display_bengali.isEmpty()) {
                this.display_bengali = jsonNode.optString("display-bn");
            }
            if (this.display_bengali.isEmpty()) {
                this.display_bengali = this.display;
            }

            this.display_tamil = jsonNode.optString("display-ta");
            if (this.display_tamil.isEmpty()) {
                this.display_tamil = jsonNode.optString("display-ta");
            }
            if (this.display_tamil.isEmpty()) {
                this.display_tamil = this.display;
            }

            this.display_oriya = jsonNode.optString("display-or");
            if (this.display_oriya.isEmpty()) {
                this.display_oriya = jsonNode.optString("display-or");
            }
            if (this.display_oriya.isEmpty()) {
                this.display_oriya = this.display;
            }
            this.display_cebuno = jsonNode.optString("display-cb");
            if (this.display_cebuno.isEmpty()) {
                this.display_cebuno = jsonNode.optString("display-cb");
            }
            if (this.display_cebuno.isEmpty()) {
                this.display_cebuno = this.display;
            }

            this.display_hindi = jsonNode.optString("display-hi");
            if (this.display_hindi.isEmpty()) {
                this.display_hindi = jsonNode.optString("display-hi");
            }
            if (this.display_hindi.isEmpty()) {
                this.display_hindi = this.display;
            }

//            Telugu language support for display...
            this.display_telugu = jsonNode.optString("display-te");
            if (this.display_telugu.isEmpty()) {
                this.display_telugu = jsonNode.optString("display-te");
            }
            if (this.display_telugu.isEmpty()) {
                this.display_telugu = this.display;
            }
            //marathi
            this.display_marathi = jsonNode.optString("display-mr");
            if (this.display_marathi.isEmpty()) {
                this.display_marathi = jsonNode.optString("display-mr");
            }
            if (this.display_marathi.isEmpty()) {
                this.display_marathi = this.display;
            }

            this.display_assamese = jsonNode.optString("display-as");
            if (this.display_assamese.isEmpty()) {
                this.display_assamese = jsonNode.optString("display-as");
            }
            if (this.display_assamese.isEmpty()) {
                this.display_assamese = this.display;
            }

            this.display_malyalam = jsonNode.optString("display-ml");
            if (this.display_malyalam.isEmpty()) {
                this.display_malyalam = jsonNode.optString("display-ml");
            }
            if (this.display_malyalam.isEmpty()) {
                this.display_malyalam = this.display;
            }


            this.display_kannada = jsonNode.optString("display-kn");
            if (this.display_kannada.isEmpty()) {
                this.display_kannada = jsonNode.optString("display-kn");
            }
            if (this.display_kannada.isEmpty()) {
                this.display_kannada = this.display;
            }

            this.display_russian = jsonNode.optString("display-ru");
            if (this.display_russian.isEmpty()) {
                this.display_russian = jsonNode.optString("display-ru");
            }
            if (this.display_russian.isEmpty()) {
                this.display_russian = this.display;
            }

            this.language = jsonNode.optString("language");
            if (this.language.isEmpty()) {
                this.language = this.text;
            }


            //Only for physical exams
            if (!this.language.isEmpty()) {
                if (this.language.contains(":")) {
                    this.rootNode = true;
                }
            }

            this.inputType = jsonNode.optString("input-type");

            this.physicalExams = jsonNode.optString("perform-physical-exam");
            this.hasPhysicalExams = !(this.physicalExams == null);

            this.jobAidFile = jsonNode.optString("job-aid-file");
            if (!jobAidFile.isEmpty()) {
                this.jobAidType = jsonNode.optString("job-aid-type");
                this.aidAvailable = true;
            } else {
                this.aidAvailable = false;
            }

            this.associatedComplaint = jsonNode.optString("associated-complaint");
            this.hasAssociations = !associatedComplaint.isEmpty();


            this.selected = false;
            this.isNoSelected = false;

            this.choiceType = jsonNode.optString("choice-type");

//            this.required = false;

            this.positiveCondition = jsonNode.optString("pos-condition");
            this.negativeCondition = jsonNode.optString("neg-condition");

            this.pop_up = jsonNode.optString("pop-up");

            this.pop_up_hi = jsonNode.optString("pop-up-hi"); //pop-up for Hindi...
            if (this.pop_up_hi.isEmpty()) {
                this.pop_up_hi = this.pop_up;
            }

            this.pop_up_or = jsonNode.optString("pop-up-or"); //pop-up for Odiya...
            if (this.pop_up_or.isEmpty()) {
                this.pop_up_or = this.pop_up;
            }

            // this.hasPopUp = !pop_up.isEmpty();
            if (!pop_up.isEmpty() || !pop_up_hi.isEmpty() || !pop_up_or.isEmpty()) {
                this.hasPopUp = true;
            }

            this.required = jsonNode.optBoolean("isRequired");
            this.optional = jsonNode.optBoolean("optional");
            this.havingNestedQuestion = jsonNode.optBoolean("havingNestedQuestion");
            this.enableExclusiveOption = jsonNode.optBoolean("enable-exclusive-option");
            this.isExclusiveOption = jsonNode.optBoolean("is-exclusive-option");
            this.compareDuplicateNode = jsonNode.optString("compare-duplicate-node");
            this.enableSubmit = jsonNode.optBoolean("submit");
        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    /**
     * Makes a copy of the knowledgeEngine, so that the original reference knowledgeEngine is not modified.
     *
     * @param source source knowledgeEngine to copy into a new knowledgeEngine. Will always default as unselected.
     */
    public Node(Node source) {
        this.engineVersion = source.engineVersion;
        this.id = source.id;
        this.isMultiChoice = source.isMultiChoice;
        this.isExcludedFromMultiChoice = source.isExcludedFromMultiChoice;
        this.text = source.text;
        this.display = source.display;
        this.display_hindi = source.display_hindi;
        this.display_oriya = source.display_oriya;
        this.display_gujarati = source.display_gujarati;
        this.display_bengali = source.display_bengali;
        this.display_tamil = source.display_tamil;
        this.display_cebuno = source.display_cebuno;
        this.display_telugu = source.display_telugu;
        this.display_kannada = source.display_kannada;
        this.display_assamese = source.display_assamese;
        this.display_russian = source.display_russian;
        this.display_malyalam = source.display_malyalam;
        this.display_marathi = source.display_marathi;
        this.optionsList = source.optionsList;
        this.terminal = source.terminal;
        this.language = source.language;
        this.gender = source.gender;
        this.min_age = source.min_age;
        this.max_age = source.max_age;
        this.inputType = source.inputType;
        this.physicalExams = source.physicalExams;
        this.complaint = source.complaint;
        this.pop_up = source.pop_up;
        this.pop_up_hi = source.pop_up_hi;
        this.pop_up_or = source.pop_up_or;
        this.jobAidFile = source.jobAidFile;
        this.jobAidType = source.jobAidType;
        this.aidAvailable = source.aidAvailable;
        this.associatedComplaint = source.associatedComplaint;
        this.hasAssociations = source.hasAssociations;
        this.hasPhysicalExams = source.hasPhysicalExams;
        this.hasPopUp = source.hasPopUp;
        this.selected = false;
        this.isNoSelected = false;
        this.associated_symptoms = 0;
        this.required = source.required;
        this.positiveCondition = source.positiveCondition;
        this.negativeCondition = source.negativeCondition;
        this.optional = source.optional;
        this.isDataCaptured = source.isDataCaptured;
        this.havingNestedQuestion = source.havingNestedQuestion;
        this.enableExclusiveOption = source.enableExclusiveOption;
        this.isExclusiveOption = source.isExclusiveOption;
        this.compareDuplicateNode = source.compareDuplicateNode;
        this.needToHide = source.needToHide;
        //this.parentNode = source.parentNode;
    }

    public static void subLevelQuestion(final Node node, final Activity context, final QuestionsAdapter callingAdapter,
                                        final String imagePath, final String imageName) {

        node.setSelected(true);
        List<Node> mNodes = node.getOptionsList();
        final CustomArrayAdapter adapter = new CustomArrayAdapter(context, R.layout.list_item_subquestion, mNodes);
        final MaterialAlertDialogBuilder subQuestion = new MaterialAlertDialogBuilder(context);

        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_subquestion, null);
        ImageView imageView = convertView.findViewById(R.id.dialog_subquestion_image_view);
        if (node.isAidAvailable()) {
            if (node.getJobAidType().equals("image")) {
                String drawableName = node.getJobAidFile();
                int resID = context.getResources().getIdentifier(drawableName, "drawable", context.getPackageName());
                imageView.setImageResource(resID);
            } else {
                imageView.setVisibility(View.GONE);
            }
        }


        subQuestion.setTitle(node.findDisplay());
        ListView listView = convertView.findViewById(R.id.dialog_subquestion_list_view);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        listView.setClickable(true);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                node.getOption(position).toggleSelected();
                adapter.notifyDataSetChanged();
                Node currentNode = node.getOption(position);
                if (node.optionsList != null && !node.optionsList.isEmpty() && !node.isMultiChoice) {
                    for (int i = 0; i < node.optionsList.size(); i++) {
                        Node innerNode = node.optionsList.get(i);
                        innerNode.setUnselected();
                    }
                    currentNode.setSelected(true);
                }

                if (node.optionsList != null && !node.optionsList.isEmpty() && node.isMultiChoice) {
                    if (currentNode.isExcludedFromMultiChoice) {

                        if (currentNode.isSelected()) {
                            for (int i = 0; i < node.optionsList.size(); i++) {
                                Node innerNode = node.optionsList.get(i);
                                innerNode.setUnselected();
                            }
                            currentNode.setSelected(true);
                        } else
                            currentNode.setUnselected();

                    } else {
                        for (int i = 0; i < node.optionsList.size(); i++) {
                            Node innerNode = node.optionsList.get(i);
                            if (innerNode.isExcludedFromMultiChoice)
                                innerNode.setUnselected();
                        }
                    }

                }
                if (node.getOption(position).getInputType() != null) {
                    subHandleQuestion(node.getOption(position), context, adapter, imagePath, imageName);
                }

                if (!node.getOption(position).isTerminal()) {
                    subLevelQuestion(node.getOption(position), context, callingAdapter, imagePath, imageName);
                }
            }
        });

        subQuestion.setView(listView);
        subQuestion.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                node.setText(node.generateLanguage());
                callingAdapter.notifyDataSetChanged();
                dialog.dismiss();
                if (node.anySubSelected() && node.anySubPopUp()) {
                    node.generatePopUp(context);
                }
            }
        });
        subQuestion.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                node.toggleSelected();
                callingAdapter.refreshChildAdapter();
                callingAdapter.notifyDataSetChanged();
                dialog.cancel();
            }
        });

        subQuestion.setView(convertView);
        AlertDialog dialog = subQuestion.show();
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);

    }

    public static void askText(final Node node, Activity context, final QuestionsAdapter adapter) {
        final MaterialAlertDialogBuilder textInput = new MaterialAlertDialogBuilder(context);
        textInput.setTitle(R.string.question_text_input);
        final EditText dialogEditText = new EditText(context);
        dialogEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        textInput.setView(dialogEditText);
        textInput.setPositiveButton(R.string.generic_ok, (dialog, which) -> {
            if (!dialogEditText.getText().toString().equalsIgnoreCase("")) {
                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", dialogEditText.getText().toString()));
                } else {
                    node.addLanguage(dialogEditText.getText().toString());
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }
            } else {
                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", NOT_ANSWERED));
                } else {
                    node.addLanguage(NOT_ANSWERED);
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }
            }
            node.setSelected(true);
            adapter.notifyDataSetChanged();
            dialog.dismiss();
        });
        textInput.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (!dialogEditText.getText().toString().equalsIgnoreCase("")) {
                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", dialogEditText.getText().toString()));
                    } else {
                        node.addLanguage(dialogEditText.getText().toString());
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }
                } else {
                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", NOT_ANSWERED));
                    } else {
                        node.addLanguage(NOT_ANSWERED);
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }
                }
                // node.setSelected(true);
                adapter.notifyDataSetChanged();
                dialog.cancel();
            }
        });
        AlertDialog dialog = textInput.show();
        dialog.setCancelable(false);
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
    }

    public static void askDate(final Node node, final Activity context, final QuestionsAdapter adapter) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(0);
                        cal.set(year, monthOfYear, dayOfMonth);
                        Date date = cal.getTime();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy", Locale.ENGLISH);
                        String dateString = simpleDateFormat.format(date);
                        if (!dateString.equalsIgnoreCase("")) {
                            if (node.getLanguage().contains("_")) {
                                node.setLanguage(node.getLanguage().replace("_", dateString));
                            } else {
                                node.addLanguage(dateString);
                                //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                            }
                            node.setSelected(true);
                        } else {
                            if (node.isRequired()) {
                                node.setSelected(false);
                            } else {
                                node.setSelected(true);
                                if (node.getLanguage().contains("_")) {
                                    node.setLanguage(node.getLanguage().replace("_", NOT_ANSWERED));
                                } else {
                                    node.addLanguage(NOT_ANSWERED);
                                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                                }
                            }
                        }

                        adapter.notifyDataSetChanged();
                        //TODO:: Check if the language is actually what is intended to be displayed
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setTitle(R.string.question_date_picker);
        //Set Maximum date to current date because even after bday is less than current date it goes to check date is set after today
        //datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    public static void handleQuestion(Node questionNode, final Activity context, final QuestionsAdapter adapter,
                                      final String imagePath, final String imageName) {
        String type = questionNode.getInputType();
        switch (type) {
            case "text":
                askText(questionNode, context, adapter);
                break;
            case "date":
                askDate(questionNode, context, adapter);
                break;
            case "location":
                askLocation(questionNode, context, adapter);
                break;
            case "number":
                askNumber(questionNode, context, adapter);
                break;
            case "area":
                askArea(questionNode, context, adapter);
                break;
            case "duration":
                askDuration(questionNode, context, adapter);
                break;
            case "range":
                askRange(questionNode, context, adapter);
                break;
            case "frequency":
                askFrequency(questionNode, context, adapter);
                break;
            case "camera":
                openCamera(context, imagePath, imageName);
                break;
        }
    }

    public static void openCamera(Activity activity, String imagePath, String imageName) {
        CustomLog.d(TAG, "open Camera!");
        Intent cameraIntent = new Intent(activity, CameraActivity.class);
        if (imageName != null && imagePath != null) {
            File filePath = new File(imagePath);
            if (!filePath.exists()) {
                boolean res = filePath.mkdirs();
            }
            cameraIntent.putExtra(CameraActivity.SET_IMAGE_NAME, imageName);
            cameraIntent.putExtra(CameraActivity.SET_IMAGE_PATH, imagePath);
        }
        activity.startActivityForResult(cameraIntent, Node.TAKE_IMAGE_FOR_NODE);
    }

    public static void askNumber(final Node node, Activity context, final QuestionsAdapter adapter) {

        final MaterialAlertDialogBuilder numberDialog = new MaterialAlertDialogBuilder(context);
        numberDialog.setTitle(R.string.question_number_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_1_number_picker, null);
        numberDialog.setView(convertView);
       /* final NumberPicker numberPicker = convertView.findViewById(R.id.dialog_1_number_picker);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(1000);*/
        EditText et_enter_value = convertView.findViewById(R.id.et_enter_value);
        et_enter_value.setFilters(new InputFilter[]{new InputFilterMinMax("1", "1000")});
        numberDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               /* numberPicker.setValue(numberPicker.getValue());
                String value = String.valueOf(numberPicker.getValue());*/
//                String value = et_enter_value.getText().toString();

                if (!et_enter_value.getText().toString().equalsIgnoreCase("")) {
                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", et_enter_value.getText().toString()));
                    } else {
                        node.addLanguage(et_enter_value.getText().toString());
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }
                    node.setSelected(true);
                } else {
                    //if (node.isRequired()) {
                    node.setSelected(false);
                    //} else {
                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", NOT_ANSWERED));
                    } else {
                        node.addLanguage(NOT_ANSWERED);
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }
                    //   node.setSelected(true);
                    //}
                }
                adapter.refreshChildAdapter();
                adapter.notifyDataSetChanged();


                dialog.dismiss();
            }
        });
        numberDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        AlertDialog dialog = numberDialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);

    }

    public static void askArea(final Node node, Activity context, final QuestionsAdapter adapter) {

        final MaterialAlertDialogBuilder areaDialog = new MaterialAlertDialogBuilder(context);
        areaDialog.setTitle(R.string.question_area_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        areaDialog.setView(convertView);
        final NumberPicker widthPicker = convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker lengthPicker = convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = convertView.findViewById(R.id.dialog_2_numbers_text);
        final TextView endText = convertView.findViewById(R.id.dialog_2_numbers_text_2);
        endText.setVisibility(View.GONE);
        middleText.setText("X");

        widthPicker.setMinValue(0);
        widthPicker.setMaxValue(100);
        lengthPicker.setMinValue(0);
        lengthPicker.setMaxValue(100);

        areaDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                widthPicker.setValue(widthPicker.getValue());
                lengthPicker.setValue(lengthPicker.getValue());
                String durationString = widthPicker.getValue() + " X " + lengthPicker.getValue();


                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", durationString));
                } else {
                    node.addLanguage(" " + durationString);
                    node.setText(durationString);
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }
                node.setSelected(true);
                adapter.notifyDataSetChanged();

                dialog.dismiss();
            }
        });
        areaDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = areaDialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);

    }

    public static void askRange(final Node node, Activity context, final QuestionsAdapter adapter) {

        final MaterialAlertDialogBuilder rangeDialog = new MaterialAlertDialogBuilder(context);
        rangeDialog.setTitle(R.string.question_range_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        rangeDialog.setView(convertView);
        final NumberPicker startPicker = convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker endPicker = convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = convertView.findViewById(R.id.dialog_2_numbers_text);
        final TextView endText = convertView.findViewById(R.id.dialog_2_numbers_text_2);
        endText.setVisibility(View.GONE);
        middleText.setText(" - ");

        startPicker.setMinValue(0);
        startPicker.setMaxValue(100);
        endPicker.setMinValue(0);
        endPicker.setMaxValue(100);
        rangeDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startPicker.setValue(startPicker.getValue());
                endPicker.setValue(endPicker.getValue());
                String durationString = startPicker.getValue() + " to " + endPicker.getValue();
                //TODO gotta get the units of the range somehow. gotta see what they look like first

                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", durationString));
                } else {
                    node.addLanguage(" " + durationString);
                    node.setText(durationString);
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }
                node.setSelected(true);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        rangeDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = rangeDialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
    }

    public static void askLocation(final Node node, Activity context, final QuestionsAdapter adapter) {

        final MaterialAlertDialogBuilder locationDialog = new MaterialAlertDialogBuilder(context);
        locationDialog.setTitle(R.string.question_location_picker);

        //TODO: Issue #51 on GitHub


    }

    public static void askFrequency(final Node node, Activity context, final QuestionsAdapter adapter) {

        final MaterialAlertDialogBuilder frequencyDialog = new MaterialAlertDialogBuilder(context);
        frequencyDialog.setTitle(R.string.question_frequency_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        frequencyDialog.setView(convertView);
        final NumberPicker quantityPicker = convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker unitPicker = convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = convertView.findViewById(R.id.dialog_2_numbers_text);
        final TextView endText = convertView.findViewById(R.id.dialog_2_numbers_text_2);
        endText.setVisibility(View.GONE);
        middleText.setVisibility(View.GONE);
        //  final String[] units = new String[]{"per Hour", "per Day", "Per Week", "per Month", "per Year"};
        final String[] units = new String[]{context.getString(R.string.per_Hour),
                context.getString(R.string.per_Day), context.getString(R.string.per_Week),
                context.getString(R.string.per_Month), context.getString(R.string.per_Year)};

        final String[] doctorUnits = new String[]{"times per hour", "time per day", "times per week", "times per month", "times per year"};
        unitPicker.setDisplayedValues(units);
        quantityPicker.setMinValue(0);
        quantityPicker.setMaxValue(100);
        unitPicker.setMinValue(0);
        unitPicker.setMaxValue(4);
        frequencyDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                quantityPicker.setValue(quantityPicker.getValue());
                unitPicker.setValue(unitPicker.getValue());
                // String durationString = quantityPicker.getValue() + " " + doctorUnits[unitPicker.getValue()];
                //translate back to English from Hindi if present...
//                String unit_text = "";
//                unit_text = hi_en(units[unitPicker.getValue()]); //for Hindi...
//                unit_text = or_en(unit_text); //for Odiya...

                //String durationString = quantityPicker.getValue() + " " + unit_text;
                String durationString = quantityPicker.getValue() + " " + doctorUnits[unitPicker.getValue()];


                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", durationString));
                } else {
                    node.addLanguage(" " + durationString);
                    node.setText(durationString);
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }
                node.setSelected(true);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        frequencyDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = frequencyDialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);

    }

    public static void askDuration(final Node node, Activity context, final QuestionsAdapter adapter) {
        final MaterialAlertDialogBuilder durationDialog = new MaterialAlertDialogBuilder(context);
        durationDialog.setTitle(R.string.question_duration_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        durationDialog.setView(convertView);
        final NumberPicker quantityPicker = convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker unitPicker = convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = convertView.findViewById(R.id.dialog_2_numbers_text);
        final TextView endText = convertView.findViewById(R.id.dialog_2_numbers_text_2);
        endText.setVisibility(View.GONE);
        middleText.setVisibility(View.GONE);
        // final String[] units = new String[]{"Hours", "Days", "Weeks", "Months", "Years"};
        final String[] units = new String[]{
                context.getString(R.string.Hours), context.getString(R.string.Days),
                context.getString(R.string.Weeks), context.getString(R.string.Months),
                context.getString(R.string.Years)}; //supports Hindi Translations as well...

        unitPicker.setDisplayedValues(units);
        quantityPicker.setMinValue(0);
        quantityPicker.setMaxValue(100);
        unitPicker.setMinValue(0);
        unitPicker.setMaxValue(4);
        durationDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                quantityPicker.setValue(quantityPicker.getValue());
                unitPicker.setValue(unitPicker.getValue());
                //  String durationString = quantityPicker.getValue() + " " + units[unitPicker.getValue()];
                //translate back to English from Hindi if present...
                String unit_text = "";
                unit_text = hi_en(units[unitPicker.getValue()]); //for Hindi...
                unit_text = or_en(unit_text); //for Odiya...
                unit_text = te_en(unit_text); //for telugu...
                unit_text = mr_en(unit_text); //for telugu...
                unit_text = gu_en(unit_text); //for Gujarati...
                unit_text = bn_en(unit_text); //for Bengali...
                unit_text = ta_en(unit_text); //for Tamil...
                String durationString = quantityPicker.getValue() + " " + unit_text;

                if (quantityPicker.getValue() != '0' || !durationString.equalsIgnoreCase("")) {
                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", durationString));
                    } else {
                        node.addLanguage(durationString);
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }
                    node.setSelected(true);
                } else {
                    if (node.isRequired()) {
                        node.setSelected(false);
                    } else {
                        if (node.getLanguage().contains("_")) {
                            node.setLanguage(node.getLanguage().replace("_", NOT_ANSWERED));
                        } else {
                            node.addLanguage(NOT_ANSWERED);
                            //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                        }
                        node.setSelected(true);
                    }
                }


                adapter.refreshChildAdapter();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        durationDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = durationDialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
    }

    private static String hi_en(String unit) {

        switch (unit) {
            case "घंटे":
                unit = "Hours";
                break;

            case "दिन":
                unit = "Days";
                break;

            case "हफ्तों":
                unit = "Weeks";
                break;

            case "महीने":
                unit = "Months";
                break;

            case "वर्षों":
                unit = "Years";
                break;

            default:
                return unit;
        }

        return unit;
    }

    private static String gu_en(String unit) {

        switch (unit) {
            case "કલાક":
                unit = "Hours";
                break;

            case "દિવસ":
                unit = "Days";
                break;

            case "અઠવાડિયું":
                unit = "Weeks";
                break;

            case "માસ":
                unit = "Months";
                break;

            case "વર્ષ":
                unit = "Years";
                break;

            default:
                return unit;
        }

        return unit;
    }

    private static String bn_en(String unit) {

        switch (unit) {
            case "ঘন্টার":
                unit = "Hours";
                break;

            case "দিনগুলি":
                unit = "Days";
                break;

            case "সপ্তাহ":
                unit = "Weeks";
                break;

            case "মাস":
                unit = "Months";
                break;

            case "বছর":
                unit = "Years";
                break;

            default:
                return unit;
        }

        return unit;
    }

    private static String ta_en(String unit) {

        switch (unit) {
            case "மணி":
                unit = "Hours";
                break;

            case "நாட்கள்":
                unit = "Days";
                break;

            case "வாரங்கள்":
                unit = "Weeks";
                break;

            case "மாதங்கள்":
                unit = "Months";
                break;

            case "ஆண்டுகள்":
                unit = "Years";
                break;

            default:
                return unit;
        }

        return unit;
    }

    private static String or_en(String unit) {

        switch (unit) {
            case "ଘଣ୍ଟା":
                unit = "Hours";
                break;

            case "ଦିନ":
                unit = "Days";
                break;

            case "ସପ୍ତାହ":
                unit = "Weeks";
                break;

            case "ମାସ":
                unit = "Months";
                break;

            case "ବର୍ଷ":
                unit = "Years";
                break;

            default:
                return unit;
        }

        return unit;
    }

    /**
     * telugu
     */
    private static String te_en(String unit) {
        switch (unit) {
            case "గంటలు":
                unit = "Hours";
                break;

            case "రోజులు":
                unit = "Days";
                break;

            case "వారాలు":
                unit = "Weeks";
                break;

            case "నెలలు":
                unit = "Months";
                break;

            case "సంవత్సరాలు":
                unit = "Years";
                break;

            default:
                return unit;
        }

        return unit;
    }

    /**
     * marathi
     */
    private static String mr_en(String unit) {

        switch (unit) {
            case "तास":
                unit = "Hours";
                break;

            case "दिवस":
                unit = "Days";
                break;

            case "आठवडे":
                unit = "Weeks";
                break;

            case "महीने":
                unit = "Months";
                break;

            case "वर्षे":
                unit = "Years";
                break;

            default:
                return unit;
        }

        return unit;
    }

    public static void subAskText(final Node node, Activity context, final CustomArrayAdapter adapter) {
        final MaterialAlertDialogBuilder textInput = new MaterialAlertDialogBuilder(context);
        textInput.setTitle(R.string.question_text_input);
        final EditText dialogEditText = new EditText(context);
        dialogEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        textInput.setView(dialogEditText);
        textInput.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", dialogEditText.getText().toString()));
                } else {
                    node.addLanguage(dialogEditText.getText().toString());
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }
                node.setSelected(true);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        textInput.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = textInput.show();
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
    }

    public static void subAskDate(final Node node, final Activity context, final CustomArrayAdapter adapter) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(0);
                        cal.set(year, monthOfYear, dayOfMonth);
                        Date date = cal.getTime();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy", Locale.ENGLISH);
                        String dateString = simpleDateFormat.format(date);
                        if (node.getLanguage().contains("_")) {
                            node.setLanguage(node.getLanguage().replace("_", dateString));
                        } else {
                            node.addLanguage(" " + dateString);
                            node.setText(node.getLanguage());
                            //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                        }
                        node.setSelected(true);
                        adapter.notifyDataSetChanged();
                        //TODO:: Check if the language is actually what is intended to be displayed
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setTitle(R.string.question_date_picker);
        //datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    public static void subAskNumber(final Node node, Activity context, final CustomArrayAdapter adapter) {

        final MaterialAlertDialogBuilder numberDialog = new MaterialAlertDialogBuilder(context);
        numberDialog.setTitle(R.string.question_number_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_1_number_picker, null);
        numberDialog.setView(convertView);
      /*  final NumberPicker numberPicker = convertView.findViewById(R.id.dialog_1_number_picker);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(1000);*/
        EditText et_enter_value = convertView.findViewById(R.id.et_enter_value);
        et_enter_value.setFilters(new InputFilter[]{new InputFilterMinMax("1", "1000")});
        numberDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //numberPicker.setValue(numberPicker.getValue());
                // String value = String.valueOf(numberPicker.getValue());
                String value = et_enter_value.getText().toString();
                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", value));
                } else {
                    node.addLanguage(" " + value);
                    node.setText(value);
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }
                node.setSelected(true);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        numberDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        AlertDialog dialog = numberDialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);

    }

    public static void subHandleQuestion(Node questionNode, final Activity context, final CustomArrayAdapter adapter, final String imagePath, final String imageName) {
        String type = questionNode.getInputType();
        CustomLog.d(TAG, "subQ " + type);
        switch (type) {
            case "text":
                subAskText(questionNode, context, adapter);
                break;
            case "date":
                subAskDate(questionNode, context, adapter);
                break;
            case "location":
                subAskLocation(questionNode, context, adapter);
                break;
            case "number":
                subAskNumber(questionNode, context, adapter);
                break;
            case "area":
                subAskArea(questionNode, context, adapter);
                break;
            case "duration":
                subAskDuration(questionNode, context, adapter);
                break;
            case "range":
                subAskRange(questionNode, context, adapter);
                break;
            case "frequency":
                subAskFrequency(questionNode, context, adapter);
                break;
            case "camera":
                openCamera(context, imagePath, imageName);
                break;
        }
    }

    public static void subAskArea(final Node node, Activity context, final CustomArrayAdapter adapter) {

        final MaterialAlertDialogBuilder areaDialog = new MaterialAlertDialogBuilder(context);
        areaDialog.setTitle(R.string.question_area_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        areaDialog.setView(convertView);
        final NumberPicker widthPicker = convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker lengthPicker = convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = convertView.findViewById(R.id.dialog_2_numbers_text);
        final TextView endText = convertView.findViewById(R.id.dialog_2_numbers_text_2);
        endText.setVisibility(View.GONE);
        middleText.setText("X");

        widthPicker.setMinValue(0);
        widthPicker.setMaxValue(100);
        lengthPicker.setMinValue(0);
        lengthPicker.setMaxValue(100);

        areaDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                widthPicker.setValue(widthPicker.getValue());
                lengthPicker.setValue(lengthPicker.getValue());
                String durationString = widthPicker.getValue() + " X " + lengthPicker.getValue();

                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", durationString));
                } else {
                    node.addLanguage(" " + durationString);
                    node.setText(durationString);
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }
                node.setSelected(true);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        areaDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = areaDialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);

    }

    public static void subAskRange(final Node node, Activity context, final CustomArrayAdapter adapter) {

        final MaterialAlertDialogBuilder rangeDialog = new MaterialAlertDialogBuilder(context);
        rangeDialog.setTitle(R.string.question_range_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        rangeDialog.setView(convertView);
        final NumberPicker startPicker = convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker endPicker = convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = convertView.findViewById(R.id.dialog_2_numbers_text);
        final TextView endText = convertView.findViewById(R.id.dialog_2_numbers_text_2);
        endText.setVisibility(View.GONE);
        middleText.setText(" - ");

        startPicker.setMinValue(0);
        startPicker.setMaxValue(100);
        endPicker.setMinValue(0);
        endPicker.setMaxValue(100);
        rangeDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startPicker.setValue(startPicker.getValue());
                endPicker.setValue(endPicker.getValue());
                String durationString = startPicker.getValue() + " to " + endPicker.getValue();
                //TODO gotta get the units of the range somehow. gotta see what they look like first

                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", durationString));
                } else {
                    node.addLanguage(" " + durationString);
                    node.setText(durationString);
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }
                node.setSelected(true);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        rangeDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = rangeDialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
    }

    public static void subAskLocation(final Node node, Activity context, final CustomArrayAdapter adapter) {

        final MaterialAlertDialogBuilder locationDialog = new MaterialAlertDialogBuilder(context);
        locationDialog.setTitle(R.string.question_location_picker);

        //TODO: Issue #51 on GitHub
    }

    public static void subAskFrequency(final Node node, Activity context, final CustomArrayAdapter adapter) {

        final MaterialAlertDialogBuilder frequencyDialog = new MaterialAlertDialogBuilder(context);
        frequencyDialog.setTitle(R.string.question_frequency_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        frequencyDialog.setView(convertView);
        final NumberPicker quantityPicker = convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker unitPicker = convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = convertView.findViewById(R.id.dialog_2_numbers_text);
        final TextView endText = convertView.findViewById(R.id.dialog_2_numbers_text_2);
        endText.setVisibility(View.GONE);
        middleText.setVisibility(View.GONE);
        // final String[] units = context.getResources().getStringArray(R.array.units);
        final String[] units = new String[]{context.getString(R.string.per_Hour),
                context.getString(R.string.per_Day), context.getString(R.string.per_Week),
                context.getString(R.string.per_Month), context.getString(R.string.per_Year)};

        final String[] doctorUnits = context.getResources().getStringArray(R.array.doctor_units);
        unitPicker.setDisplayedValues(units);
        quantityPicker.setMinValue(0);
        quantityPicker.setMaxValue(100);
        unitPicker.setMinValue(0);
        unitPicker.setMaxValue(4);
        frequencyDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                quantityPicker.setValue(quantityPicker.getValue());
                unitPicker.setValue(unitPicker.getValue());
                String durationString = quantityPicker.getValue() + " " + doctorUnits[unitPicker.getValue()];

                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", durationString));
                } else {
                    node.addLanguage(" " + durationString);
                    node.setText(durationString);
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }
                node.setSelected(true);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        frequencyDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = frequencyDialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);

    }

    public static void subAskDuration(final Node node, Activity context, final CustomArrayAdapter adapter) {
        final MaterialAlertDialogBuilder durationDialog = new MaterialAlertDialogBuilder(context);
        durationDialog.setTitle(R.string.question_duration_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        durationDialog.setView(convertView);
        final NumberPicker quantityPicker = convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker unitPicker = convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = convertView.findViewById(R.id.dialog_2_numbers_text);
        final TextView endText = convertView.findViewById(R.id.dialog_2_numbers_text_2);
        endText.setVisibility(View.GONE);
        middleText.setVisibility(View.GONE);
        // final String[] units = context.getResources().getStringArray(R.array.duration_units);
        final String[] units = new String[]{
                context.getString(R.string.Hours), context.getString(R.string.Days),
                context.getString(R.string.Weeks), context.getString(R.string.Months),
                context.getString(R.string.Years)}; //supports Hindi Translations as well...

        unitPicker.setDisplayedValues(units);
        quantityPicker.setMinValue(0);
        quantityPicker.setMaxValue(100);
        unitPicker.setMinValue(0);
        unitPicker.setMaxValue(4);
        durationDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                quantityPicker.setValue(quantityPicker.getValue());
                unitPicker.setValue(unitPicker.getValue());
                // String durationString = quantityPicker.getValue() + " " + units[unitPicker.getValue()];
                //translate back to English from Hindi if present...
                String unit_text = "";
                unit_text = hi_en(units[unitPicker.getValue()]); //for Hindi...
                unit_text = or_en(unit_text); //for Odiya...
                unit_text = te_en(unit_text); //for Telugu...
                unit_text = mr_en(unit_text); //for Marathi...
                unit_text = gu_en(unit_text); //for Gujarati...
                unit_text = bn_en(unit_text); //for Bengali...
                unit_text = ta_en(unit_text); //for Tamil...

                String durationString = quantityPicker.getValue() + " " + unit_text;

                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", durationString));
                } else {
                    node.addLanguage(" " + durationString);
                    node.setText(durationString);
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }
                node.setSelected(true);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        durationDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = durationDialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
    }

    public boolean isNeedToHide() {
        return needToHide;
    }

    public void setNeedToHide(boolean needToHide) {
        this.needToHide = needToHide;
    }

    // check the answer status for the Associated_symptoms
    public int getAssociated_symptoms() {
        return associated_symptoms;
    }

    public void setAssociated_symptoms(int associated_symptoms) {
        this.associated_symptoms = associated_symptoms;
    }

    //Terminal nodes are important to identify to know so that the org does not keep looking for sub-nodes.
    public boolean isTerminal() {
        return terminal;
    }

    public void setTerminal(boolean terminal) {
        this.terminal = terminal;
    }

    //Only complaints should be presented to the user at Complaint Select.
    public boolean isComplaint() {
        return complaint;
    }

    public void setComplaint(boolean complaint) {
        this.complaint = complaint;
    }

    //In certain instances, the input is added to the starter language given to the user.
    public void addLanguage(String newText) {
        //CustomLog.d("Node", language);
        if (language.contains("_")) {
            language = language.replace("_", newText);
            //CustomLog.d("Node", language);
        } else {
            language = newText;
            //CustomLog.d("Node", language);
        }
    }

    public String findDisplay() {

        SessionManager sessionManager = null;
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());

//        String locale = Locale.getDefault().getLanguage();
        String locale = sessionManager.getCurrentLang();
        CustomLog.i(TAG, "findDisplay:" + locale);
        switch (locale) {
            case "en": {
                //CustomLog.i(TAG, "findDisplay: eng");
                if (display != null && display.isEmpty()) {
                    //CustomLog.i(TAG, "findDisplay: eng txt");
                    return text;
                } else {
                    //CustomLog.i(TAG, "findDisplay: eng dis");
                    return display;
                }
            }
            case "gu": {
                if (display_gujarati != null && !display_gujarati.isEmpty()) {
                    return display_gujarati;
                } else {
                    if (display == null || display.isEmpty()) {
                        //CustomLog.i(TAG, "findDisplay: eng/o txt");
                        return text;
                    } else {
                        //CustomLog.i(TAG, "findDisplay: eng/o dis");
                        return display;
                    }
                }
            }
            case "bn": {
                if (display_bengali != null && !display_bengali.isEmpty()) {
                    return display_bengali;
                } else {
                    if (display == null || display.isEmpty()) {
                        //CustomLog.i(TAG, "findDisplay: eng/o txt");
                        return text;
                    } else {
                        //CustomLog.i(TAG, "findDisplay: eng/o dis");
                        return display;
                    }
                }
            }
            case "ta": {
                if (display_tamil != null && !display_tamil.isEmpty()) {
                    return display_tamil;
                } else {
                    if (display == null || display.isEmpty()) {
                        //CustomLog.i(TAG, "findDisplay: eng/o txt");
                        return text;
                    } else {
                        //CustomLog.i(TAG, "findDisplay: eng/o dis");
                        return display;
                    }
                }
            }
            case "or": {
                //CustomLog.i(TAG, "findDisplay: ori");
                if (display_oriya != null && !display_oriya.isEmpty()) {
                    //CustomLog.i(TAG, "findDisplay: ori dis");
                    return display_oriya;
                } else {
                    if (display == null || display.isEmpty()) {
                        //CustomLog.i(TAG, "findDisplay: eng/o txt");
                        return text;
                    } else {
                        //CustomLog.i(TAG, "findDisplay: eng/o dis");
                        return display;
                    }
                }

            }
            case "hi": {
                //CustomLog.i(TAG, "findDisplay: cb");
                if (display_hindi != null && !display_hindi.isEmpty()) {
                    //CustomLog.i(TAG, "findDisplay: cb ");
                    return display_hindi;
                } else {
                    if (display == null || display.isEmpty()) {
                        //CustomLog.i(TAG, "findDisplay: eng/o txt");
                        return text;
                    } else {
                        //CustomLog.i(TAG, "findDisplay: eng/o dis");
                        return display;
                    }
                }
            }
            case "te": {
                if (display_telugu != null && !display_telugu.isEmpty()) {
                    return display_telugu;
                } else {
                    if (display == null || display.isEmpty()) {
                        //CustomLog.i(TAG, "findDisplay: eng/o txt");
                        return text;
                    } else {
                        //CustomLog.i(TAG, "findDisplay: eng/o dis");
                        return display;
                    }
                }
            }
            case "mr": {
                if (display_marathi != null && !display_marathi.isEmpty()) {
                    //CustomLog.i(TAG, "findDisplay: mr ");
                    return display_marathi;
                } else {
                    if (display == null || display.isEmpty()) {
                        //CustomLog.i(TAG, "findDisplay: eng/o txt");
                        return text;
                    } else {
                        //CustomLog.i(TAG, "findDisplay: eng/o dis");
                        return display;
                    }
                }
            }  //Assamese language support...
            case "as": {
                if (display_assamese != null && !display_assamese.isEmpty()) {
                    return display_assamese;
                } else {
                    if (display == null || display.isEmpty()) {
                        return text;
                    } else {
                        return display;
                    }
                }
            }

            //Malyalam language support...
            case "ml": {
                if (display_malyalam != null && !display_malyalam.isEmpty()) {
                    return display_malyalam;
                } else {
                    if (display == null || display.isEmpty()) {
                        return text;
                    } else {
                        return display;
                    }
                }
            }
            case "kn": {
                if (display_kannada != null && !display_kannada.isEmpty()) {
                    return display_kannada;
                } else {
                    if (display == null || display.isEmpty()) {
                        return text;
                    } else {
                        return display;
                    }
                }
            }
            case "ru": {
                if (display_russian != null && !display_russian.isEmpty()) {
                    if (display_russian.equals(ProtocolKeys.VISIT_REASON) && BuildConfig.FLAVOR_client == FlavorKeys.UNFPA) {
                        if (display == null || display.isEmpty()) {
                            return text;
                        } else {
                            return display;
                        }
                    }
                    return display_russian;
                } else {
                    if (display == null || display.isEmpty()) {
                        return text;
                    } else {
                        return display;
                    }
                }
            }

            default: {
                {
                    if (display != null && display.isEmpty()) {
                        return text;
                    } else {
                        return display;
                    }
                }
            }
        }
    }

    public String getPositiveCondition() {
        return positiveCondition;
    }

    public void setPositiveCondition(String positiveCondition) {
        this.positiveCondition = positiveCondition;
    }

    public String getNegativeCondition() {
        return negativeCondition;
    }

    public void setNegativeCondition(String negativeCondition) {
        this.negativeCondition = negativeCondition;
    }

    public int size() {
        return optionsList.size();
    }

    public boolean hasAssociations() {
        return hasAssociations;
    }

    public boolean isHasPhysicalExams() {
        return hasPhysicalExams;
    }

    public String getAssociatedComplaint() {
        return associatedComplaint;
    }

    public boolean isAidAvailable() {
        return aidAvailable;
    }

    public List<Node> getOptionsList() {
        return optionsList;
    }

    public void setOptionsList(List<Node> optionsList) {
        this.optionsList = optionsList;
    }

    public Node getOptionByName(String name) {
        Node foundNode = null;
        if (optionsList != null) {
            for (Node node : optionsList) {
                if (node.getText().equals(name)) {
                    foundNode = node;
                }
            }
        }
        if (foundNode == null) CustomLog.i(TAG, "getOptionByName [Not Found]: " + name);
        return foundNode;
    }

    public Node getOption(int i) {
        return optionsList.get(i);
    }

    public void addOptions(Node node) {
        optionsList.add(node);
    }

    public String getJobAidFile() {
        return jobAidFile;
    }

    public String getJobAidType() {
        return jobAidType;
    }

    public String getPop_up() {
        return pop_up;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean isSelectedStatus) {
        selected = isSelectedStatus;
    }

    public boolean isNoSelected() {
        return isNoSelected;
    }

    public void setNoSelected(boolean noSelected) {
        isNoSelected = noSelected;
    }

    public void setUnselected() {
        selected = false;
    }

    public void toggleSelected() {
        selected = !selected;
    }

    public boolean anySubSelected() {
        if (!terminal) {
            for (int i = 0; i < optionsList.size(); i++) {
                if (optionsList.get(i).isSelected()) {
                    subSelected = true;
                    break;
                } else {
                    subSelected = false;
                }
            }
            return subSelected;
        } else {
            return false;
        }
    }

    public boolean anySubPopUp() {
        if (!terminal) {
            for (int i = 0; i < optionsList.size(); i++) {
                if (optionsList.get(i).isSelected()) {
                    subPopUp = true;
                    break;
                } else {
                    subPopUp = false;
                }
            }
            return subPopUp;
        } else {
            return false;
        }
    }

    public String generateLanguage() {

        String raw = "";
        List<Node> mOptions = optionsList;
        if (optionsList != null && !optionsList.isEmpty()) {
            for (Node node_opt : mOptions) {
                if (node_opt.isSelected()) {
                    String associatedTest = node_opt.getText();
                    if (associatedTest != null && (associatedTest.trim().equals(Node.ASSOCIATE_SYMPTOMS) || associatedTest.trim().equals("जुड़े लक्षण") ||
                            //(associatedTest.trim().equals("H/o specific illness")) ||
                            (associatedTest.trim().equals("హ / ఓ నిర్దిష్ట అనారోగ్యం")) || (associatedTest.trim().equals("అనుబంధ లక్షణాలు")) ||
                            (associatedTest.trim().equals("ସମ୍ପର୍କିତ ଲକ୍ଷଣଗୁଡ଼ିକ")) || (associatedTest.trim().equals("संबंधित लक्षणे"))
                            || (associatedTest.trim().equals("સંકળાયેલ લક્ષણો"))
                            || (associatedTest.trim().equals("জড়িত লক্ষণগুলি"))
                            || (associatedTest.trim().equals("தொடர்புடைய அறிகுறிகள்"))
                            || (associatedTest.trim().equals("সম্পৰ্কিত লক্ষণসমূহ")))) {
                        String r1 = generateAssociatedSymptomsOrHistory(node_opt, false);

                        if ((associatedTest.trim().equals(Node.ASSOCIATE_SYMPTOMS)) || associatedTest.trim().equals("जुड़े लक्षण") ||
                                (associatedTest.trim().equals("అనుబంధ లక్షణాలు")) ||
                                (associatedTest.trim().equals("জড়িত লক্ষণগুলি")) ||
                                (associatedTest.trim().equals("ସମ୍ପର୍କିତ ଲକ୍ଷଣଗୁଡ଼ିକ")) || (associatedTest.trim().equals("संबंधित लक्षणे"))
                                || (associatedTest.trim().equals("સંકળાયેલ લક્ષણો"))
                                || (associatedTest.trim().equals("தொடர்புடைய அறிகுறிகள்"))
                                || (associatedTest.trim().equals("সম্পৰ্কিত লক্ষণসমূহ"))) {
                            if (!r1.isEmpty()) {
                                //raw = raw + (generateAssociatedSymptomsOrHistory(node_opt)) + next_line;
                                //raw = raw.substring(6);
                                raw = raw + (Node.bullet_arrow + "<b> " + node_opt.getLanguage() + "</b>: " + r1) + next_line;
                                CustomLog.e("FinalText= ", raw);
                            } else {
                                CustomLog.e("FinalText= ", raw);

                            }
                        } else {
                            raw = raw + (Node.bullet_arrow + "<b> " + node_opt.getLanguage() + "</b>: " + r1) + next_line;
                        }

                    } else {
                        if (!node_opt.getLanguage().isEmpty()) {
                            if (node_opt.getLanguage().equals("%")) {
                                raw = raw + bullet + " " + node_opt.formLanguage() + next_line;
                            } else if (node_opt.getLanguage().substring(0, 1).equals("%")) {
                                raw = raw + (bullet + " " + node_opt.getLanguage().substring(1) + " - " + node_opt.formLanguage()) + next_line;
                            } else {
                                raw = raw + (bullet + " " + node_opt.getLanguage() + " - " + node_opt.formLanguage()) + next_line;
                            }
                        }
                    }
                    //raw = raw + ("\n"+"\n" + bullet +" "+ node_opt.formLanguage());

                } else {
                    String associatedTest = node_opt.getText();
                    if (associatedTest != null && (associatedTest.trim().equals(Node.ASSOCIATE_SYMPTOMS)
                            || associatedTest.trim().equals("जुड़े लक्षण") || associatedTest.trim().equals("అనుబంధ లక్షణాలు") ||
                            (associatedTest.trim().equals("ସମ୍ପର୍କିତ ଲକ୍ଷଣଗୁଡ଼ିକ")) || (associatedTest.trim().equals("संबंधित लक्षणे"))
                            || (associatedTest.trim().equals("સંકળાયેલ લક્ષણો"))
                            || (associatedTest.trim().equals("জড়িত লক্ষণগুলি"))
                            || (associatedTest.trim().equals("தொடர்புடைய அறிகுறிகள்"))
                            || (associatedTest.trim().equals("সম্পৰ্কিত লক্ষণসমূহ")))) {
                        String r1 = generateAssociatedSymptomsOrHistory(node_opt, false);

                        if (!r1.isEmpty()) {
                            //raw = raw + (generateAssociatedSymptomsOrHistory(node_opt)) + next_line;
                            //raw = raw.substring(6);
                            raw = raw + (Node.bullet_arrow + "<b> " + node_opt.getLanguage() + "</b>: " + r1) + next_line;
                            CustomLog.e("FinalText= ", raw);
                        } else {
                            CustomLog.e("FinalText= ", raw);
                        }
                    }
                }
                CustomLog.v("node", raw);
            }
        }

        String formatted;
        if (!raw.isEmpty()) {
            CustomLog.i(TAG, "generateLanguage: " + raw);
            if (Character.toString(raw.charAt(0)).equals(",")) {
                formatted = raw.substring(2);
            } else {
                formatted = raw;
            }
            formatted = formatted.replaceAll("\\. -", ".");
            formatted = formatted.replaceAll("\\.,", ", ");
            CustomLog.i(TAG, "generateLanguage: " + formatted);
            return formatted;
        }
        return null;
    }

    public String generateLanguageSingleNode() {

        String raw = "";

        if (isSelected()) {
            String associatedTest = getText();
            if (associatedTest != null && (associatedTest.trim().equals(Node.ASSOCIATE_SYMPTOMS) || associatedTest.trim().equals("जुड़े लक्षण") ||
                    //(associatedTest.trim().equals("H/o specific illness")) ||
                    (associatedTest.trim().equals("హ / ఓ నిర్దిష్ట అనారోగ్యం")) || (associatedTest.trim().equals("అనుబంధ లక్షణాలు")) ||
                    (associatedTest.trim().equals("ସମ୍ପର୍କିତ ଲକ୍ଷଣଗୁଡ଼ିକ")) || (associatedTest.trim().equals("संबंधित लक्षणे"))
                    || (associatedTest.trim().equals("સંકળાયેલ લક્ષણો"))
                    || (associatedTest.trim().equals("জড়িত লক্ষণগুলি"))
                    || (associatedTest.trim().equals("தொடர்புடைய அறிகுறிகள்"))
                    || (associatedTest.trim().equals("সম্পৰ্কিত লক্ষণসমূহ")))) {
                String r1 = generateAssociatedSymptomsOrHistory(this, true);
                if ((associatedTest.trim().equals(Node.ASSOCIATE_SYMPTOMS)) || associatedTest.trim().equals("जुड़े लक्षण") ||
                        (associatedTest.trim().equals("అనుబంధ లక్షణాలు")) ||
                        (associatedTest.trim().equals("জড়িত লক্ষণগুলি")) ||
                        (associatedTest.trim().equals("ସମ୍ପର୍କିତ ଲକ୍ଷଣଗୁଡ଼ିକ")) || (associatedTest.trim().equals("संबंधित लक्षणे"))
                        || (associatedTest.trim().equals("સંકળાયેલ લક્ષણો"))
                        || (associatedTest.trim().equals("தொடர்புடைய அறிகுறிகள்"))
                        || (associatedTest.trim().equals("সম্পৰ্কিত লক্ষণসমূহ"))) {

                    if (!r1.isEmpty()) {
                        //raw = raw + (generateAssociatedSymptomsOrHistory(node_opt)) + next_line;
                        //raw = raw.substring(6);
                        raw = raw + (Node.bullet_arrow + "<b> " + getLanguage() + "</b>: " + r1) + next_line;
                        CustomLog.e("FinalText= ", raw);
                    } else {
                        CustomLog.e("FinalText= ", raw);

                    }
                } else {
                    raw = raw + (Node.bullet_arrow + "<b> " + getLanguage() + "</b>: " + r1) + next_line;
                }

            } else {
                if (!getLanguage().isEmpty()) {
                    if (getLanguage().equals("%")) {
                        raw = raw + bullet + " " + formLanguage() + next_line;
                    } else if (getLanguage().substring(0, 1).equals("%")) {
                        raw = raw + (bullet + " " + getLanguage().substring(1) + " - " + formLanguage()) + next_line;
                    } else {
                        raw = raw + (bullet + " " + getLanguage() + " - " + formLanguage()) + next_line;
                    }
                }
            }
            //raw = raw + ("\n"+"\n" + bullet +" "+ node_opt.formLanguage());

        } else {
            String associatedTest = getText();
            if (associatedTest != null && (associatedTest.trim().equals(Node.ASSOCIATE_SYMPTOMS)
                    || associatedTest.trim().equals("जुड़े लक्षण") || associatedTest.trim().equals("అనుబంధ లక్షణాలు") ||
                    (associatedTest.trim().equals("ସମ୍ପର୍କିତ ଲକ୍ଷଣଗୁଡ଼ିକ")) || (associatedTest.trim().equals("संबंधित लक्षणे"))
                    || (associatedTest.trim().equals("સંકળાયેલ લક્ષણો"))
                    || (associatedTest.trim().equals("জড়িত লক্ষণগুলি"))
                    || (associatedTest.trim().equals("தொடர்புடைய அறிகுறிகள்"))
                    || (associatedTest.trim().equals("সম্পৰ্কিত লক্ষণসমূহ")))) {
                String r1 = generateAssociatedSymptomsOrHistory(this, true);
                if (!r1.isEmpty()) {
                    //raw = raw + (generateAssociatedSymptomsOrHistory(node_opt)) + next_line;
                    //raw = raw.substring(6);
                    raw = raw + (Node.bullet_arrow + "<b> " + getLanguage() + "</b>: " + r1) + next_line;
                    CustomLog.e("FinalText= ", raw);
                } else {
                    CustomLog.e("FinalText= ", raw);
                }
            }
        }
        CustomLog.v("node", raw);


        String formatted;
        if (!raw.isEmpty()) {
            CustomLog.i(TAG, "generateLanguage: " + raw);
            if (Character.toString(raw.charAt(0)).equals(",")) {
                formatted = raw.substring(2);
            } else {
                formatted = raw;
            }
            formatted = formatted.replaceAll("\\. -", ".");
            formatted = formatted.replaceAll("\\.,", ", ");
            CustomLog.i(TAG, "generateLanguage: " + formatted);
            return formatted;
        }
        return null;
    }

    //TODO: Check this, as associated complaints are not being triggered.
    public ArrayList<String> getSelectedAssociations() {
        ArrayList<String> selectedAssociations = new ArrayList<>();
        List<Node> mOptions = optionsList;
        for (int i = 0; i < mOptions.size(); i++) {
            if (mOptions.get(i).isSelected() & mOptions.get(i).hasAssociations()) {
                selectedAssociations.add(mOptions.get(i).getAssociatedComplaint());
                if (!mOptions.get(i).isTerminal()) {
                    selectedAssociations.addAll(mOptions.get(i).getSelectedAssociations());
                }
            }
        }
        return selectedAssociations;
    }

    public ArrayList<String> getPhysicalExamList() {
        ArrayList<String> selectedExams = new ArrayList<>();
        List<Node> mOptions = optionsList;
        for (int i = 0; i < mOptions.size(); i++) {
            if (mOptions.get(i).isSelected() & mOptions.get(i).isHasPhysicalExams()) {
                String rawExams = mOptions.get(i).getPhysicalExams();
                if (!rawExams.trim().isEmpty()) {
                    String[] splitExams = rawExams.split(";");
                    selectedExams.addAll(Arrays.asList(splitExams));
                }
                if (!mOptions.get(i).isTerminal()) {
                    selectedExams.addAll(mOptions.get(i).getPhysicalExamList());
                }
            }
        }
        return selectedExams;
    }



   /* public ArrayList<String> getPhysicalExams() {
        ArrayList<String> selectedAssociations = new ArrayList<>();
        List<Node> mOptions = optionsList;
        for (int i = 0; i < mOptions.size(); i++) {
            if (mOptions.get(i).isSelected() & mOptions.get(i).hasAssociations()) {
                selectedAssociations.add(mOptions.get(i).getAssociatedComplaint());
                if (!mOptions.get(i).isTerminal()) {
                    selectedAssociations.addAll(mOptions.get(i).getSelectedAssociations());
                }
            }
        }
        return selectedAssociations;
    }
    */

    public String findPopup() {

        SessionManager sessionManager = null;
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        String locale = sessionManager.getCurrentLang();

        switch (locale) {
            case "or": {
                if (pop_up_or != null && !pop_up_or.isEmpty()) {
                    return pop_up_or;
                } else {
                    return pop_up;
                }

            }
            case "hi": {
                if (pop_up_hi != null && !pop_up_hi.isEmpty()) {
                    return pop_up_hi;
                } else {
                    return pop_up;
                }
            }
            default: {
                return pop_up;
            }
        }
    }

    public void generatePopUp(final Activity context) {
        SessionManager sessionManager = null;
        sessionManager = new SessionManager(IntelehealthApplication.getAppContext());

        HashSet<String> messages = new HashSet<String>();
        List<Node> mOptions = optionsList;
        if (optionsList != null && !optionsList.isEmpty()) {
            for (Node node_opt : mOptions) {
                if (node_opt.isSelected() && node_opt.hasPopUp) {
                    if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                        messages.add(node_opt.pop_up_hi);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
                        messages.add(node_opt.pop_up_or);
                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
                        messages.add(node_opt.pop_up_or);
                    }
//                    } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
//                        messages.add(node_opt.pop_up_or);
//                    }
                    else {
                        messages.add(node_opt.pop_up);
                    }

                }
            }
        }

        String finalMessage = "";
        Iterator<String> i = messages.iterator();
        while (i.hasNext()) {
            finalMessage = i.next() + "\n";
        }

        if (!finalMessage.isEmpty()) {
            MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(context);
            alertDialogBuilder.setMessage(finalMessage);
            alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            IntelehealthApplication.setAlertDialogCustomTheme(context, alertDialog);
        }
    }

    /**
     * Takes a JSON Array from a knowledgeEngine and creates the sub-nodes to store within it.
     * This is how we handle recursive construction.
     * Nodes are stores within each other. This method is maintains good organizational structure, but makes it difficult to loop back to higher level nodes.
     * This is will be modified as the knowledge curating method is updated.
     * <p>
     * The current structure of the knowledge, and the way it is stored here, is as follows"
     * Node 1 {
     * Node 1.1 {
     * Node 1.1.1
     * Node 1.1.2
     * Node 1.1.3
     * }
     * }
     *
     * @param jsonArray JSON Array of JSON Objects, which are nodes in the knowledge
     * @return List of nodes generated based on input JSON Array
     */
    private List<Node> createOptions(JSONArray jsonArray) {
        List<Node> createdOptions = new ArrayList<>();

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject current = jsonArray.getJSONObject(i);
                createdOptions.add(i, new Node(current));
            }
        } catch (JSONException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        //write your code here for removing an option item...

        return createdOptions;
    }

    /*
        Language needs to be built recursively for each first level question of a complaint.
        In this context, all the language must be built by searching a knowledgeEngine, and then looking at sub-nodes to determine which are selected.
        Once a terminal knowledgeEngine is found, then the "sentence" of the primary starting knowledgeEngine is complete.
        So for Question 1 of Complaint X, all of the nodes of Q1 are examined to see which are selected, and the selected branch's language attributes are merged.
        Once the Q1 sentence is saved, Q2 is now formed.
     */
    public String formLanguage() {
        List<String> stringsList = new ArrayList<>();
        List<Node> mOptions = optionsList;
        boolean isTerminal = false;
        if (mOptions != null && !mOptions.isEmpty()) {
            for (int i = 0; i < mOptions.size(); i++) {
                if (mOptions.get(i).isSelected()) {
                    String test = mOptions.get(i).getLanguage();
                    if (!test.isEmpty()) {
                        if (test.equals("%")) {
                        } else if (test.substring(0, 1).equals("%")) {
                            stringsList.add(test.substring(1));
                        } else {
                            stringsList.add(test);
                        }
                    }

                    if (!mOptions.get(i).isTerminal()) {
                        stringsList.add(mOptions.get(i).formLanguage());
                        isTerminal = false;
                    } else {
                        isTerminal = true;
                    }
                }
            }
        }

        String languageSeparator;
        if (isTerminal) {
            languageSeparator = ", ";
        } else {
            languageSeparator = " - ";
        }
        String mLanguage = "";
        for (int i = 0; i < stringsList.size(); i++) {
            if (i == 0) {

                if (!stringsList.get(i).isEmpty()) {
                    if (i == stringsList.size() - 1 && isTerminal) {
                        mLanguage = mLanguage.concat(stringsList.get(i) + ".");
                    } else {
                        mLanguage = mLanguage.concat(stringsList.get(i));
                    }
                }
            } else {
                if (!stringsList.get(i).isEmpty()) {
                    if (i == stringsList.size() - 1 && isTerminal) {
                        mLanguage = mLanguage.concat(languageSeparator + stringsList.get(i) + ".");
                    } else {
                        mLanguage = mLanguage.concat(languageSeparator + stringsList.get(i));
                    }
                }
            }
        }
        return mLanguage;
    }

    public AlertDialog displayImage(final Activity context, final String path, final String name) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setPositiveButton(R.string.button_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addImageToList();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.button_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File temp = new File(imagePath);
                if (temp.exists()) temp.delete();
                imagePath = null;
                openCamera(context, path, name);
                dialog.cancel();
            }
        });
        final AlertDialog dialog = builder.create();
        LayoutInflater inflater = context.getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.image_confirmation_dialog, null);

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            dialog.supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        } else {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        dialog.setView(dialogLayout);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);


        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {

                DisplayMetrics displayMetrics = new DisplayMetrics();
                context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int screen_height = displayMetrics.heightPixels;
                int screen_width = displayMetrics.widthPixels;

                ImageView imageView = dialog.findViewById(R.id.confirmationImageView);
                final ProgressBar progressBar = dialog.findViewById(R.id.progressBar);
                Glide.with(context.getApplicationContext())
                        .load(new File(imagePath))
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .override(screen_width, screen_height)
                        .into(imageView);
            }
        });

        dialog.show();
        IntelehealthApplication.setAlertDialogCustomTheme(context, dialog);
        return dialog;

    }

    public String getPhysicalExams() {
        return physicalExams;
    }

    public void removeOptionsList() {
        this.optionsList = new ArrayList<>();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getDisplay_gujarati() {
        return display_gujarati;
    }

    public void setDisplay_gujarati(String display_gujarati) {
        this.display_gujarati = display_gujarati;
    }

    public String getDisplay_bengali() {
        return display_bengali;
    }

    public String getDisplay_tamil() {
        return display_tamil;
    }

    public void setDisplay_tamil(String display_tamil) {
        this.display_tamil = display_tamil;
    }

    public String getDisplay_oriya() {
        return display_oriya;
    }

    public void setDisplay_oriya(String display_oriya) {
        this.display_oriya = display_oriya;
    }

    public String getDisplay_cebuno() {
        return display_cebuno;
    }

    public void setDisplay_cebuno(String display_cebuno) {
        this.display_cebuno = display_cebuno;
    }

    public String getDisplay_hindi() {
        return display_hindi;
    }

    public void setDisplay_hindi(String display_hindi) {
        this.display_hindi = display_hindi;
    }

    public String getDisplay_marathi() {
        return display_marathi;
    }

    public void setDisplay_marathi(String display_marathi) {
        this.display_marathi = display_marathi;
    }

    public String getDisplay_telugu() {
        return display_telugu;
    }

    public void setDisplay_telugu(String display_telugu) {
        this.display_telugu = display_telugu;
    }

    public String getDisplay_kannada() {
        return display_kannada;
    }

    public void setDisplay_kannada(String display_kannada) {
        this.display_kannada = display_kannada;
    }

    public String getDisplay_assamese() {
        return display_assamese;
    }

    public void setDisplay_assamese(String display_assamese) {
        this.display_assamese = display_assamese;
    }

    public String getDisplay_malyalam() {
        return display_malyalam;
    }

    public void setDisplay_malyalam(String display_malyalam) {
        this.display_malyalam = display_malyalam;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMin_age() {
        return min_age;
    }

    public void setMin_age(String min_age) {
        this.min_age = min_age;
    }

    public String getMax_age() {
        return max_age;
    }

    public void setMax_age(String max_age) {
        this.max_age = max_age;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getInputType() {
        return inputType;
    }

    public String getChoiceType() {
        return choiceType;
    }

    public void setChoiceType(String choiceType) {
        this.choiceType = choiceType;
    }

    public boolean isRootNode() {
        return rootNode;
    }

    public void setRootNode(boolean rootNode) {
        this.rootNode = rootNode;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public List<String> getImagePathList() {
        return imagePathList;
    }

    public void setImagePathList(List<String> imagePathList) {
        this.imagePathList = imagePathList;
    }

    public void addImageToList() {
        if (imagePathList == null) {
            imagePathList = new ArrayList<>();
        }
        if (imagePath != null && !imagePath.isEmpty()) {
            imagePathList.add(imagePath);

        }
    }

    public boolean isMultiChoice() {
        return isMultiChoice;
    }

    public void setMultiChoice(boolean multiChoice) {
        isMultiChoice = multiChoice;
    }

    public boolean isExcludedFromMultiChoice() {
        return isExcludedFromMultiChoice;
    }

    public void setExcludedFromMultiChoice(boolean excludedFromMultiChoice) {
        isExcludedFromMultiChoice = excludedFromMultiChoice;
    }

    private String generateAssociatedSymptomsOrHistory(Node associatedSymptomNode, boolean isForAssociatedSymptoms) {

        List<String> positiveAssociations = new ArrayList<>();
        List<String> negativeAssociations = new ArrayList<>();
        List<String> finalTexts = new ArrayList<>();
        List<Node> mOptions = associatedSymptomNode.getOptionsList();
        boolean flagPositive = false;
        boolean flagNegative = false;
//        String mLanguagePositive = associatedSymptomNode.positiveCondition;
        String mLanguagePositive = "Patient reports -" + next_line;
//        String mLanguageNegative = associatedSymptomNode.negativeCondition;
        String mLanguageNegative = "Patient denies -" + next_line;

        CustomLog.i(TAG, "generateAssociatedSymptomsOrHistory: " + mLanguagePositive);
        CustomLog.i(TAG, "generateAssociatedSymptomsOrHistory: " + mLanguageNegative);


        for (int i = 0; i < mOptions.size(); i++) {


            if (mOptions.get(i).isSelected()) {
                if (!mOptions.get(i).getLanguage().isEmpty()) {
                    if (!mOptions.get(i).isTerminal()) {
                        if (positiveAssociations.size() > 0) {
                            String tempString = mOptions.get(i).getText() + bullet_arrow +
                                    mOptions.get(i).formLanguage();

                            positiveAssociations.add(tempString);
                        }
                    } else if (isForAssociatedSymptoms && mOptions.get(i).isUserInputsTypeNode()) {
                        positiveAssociations.add(mOptions.get(i).getText().replaceAll("\\[(.*?)\\]", "") + bullet_arrow + mOptions.get(i).getLanguage());
                    } else {
                        if (mOptions.get(i).getLanguage().equals("%")) {
                        } else if (mOptions.get(i).getLanguage().substring(0, 1).equals("%")) {
                            positiveAssociations.add(mOptions.get(i).getLanguage().substring(1));
                        } else if (mOptions.get(i).getLanguage().isEmpty()) {
                            positiveAssociations.add(mOptions.get(i).getText());
                        } else {
                            positiveAssociations.add(mOptions.get(i).getLanguage());
                        }
                    }
                }


            } else if (mOptions.get(i).isNoSelected()) {
                if (!mOptions.get(i).getLanguage().isEmpty()) {
                    if (mOptions.get(i).getLanguage().equals("%")) {
                    } else if (mOptions.get(i).getLanguage().substring(0, 1).equals("%")) {
                        negativeAssociations.add(mOptions.get(i).getLanguage().substring(1));
                    } else if (mOptions.get(i).getLanguage().isEmpty()) {
                        negativeAssociations.add(mOptions.get(i).getText());
                    } else {
                        negativeAssociations.add(mOptions.get(i).getLanguage());
                    }
                }
            }


//            else {
//                if (mOptions.get(i).getLanguage().equals("%")) {
//                } else if (mOptions.get(i).getLanguage().substring(0, 1).equals("%")) {
//                    negativeAssociations.add(mOptions.get(i).getLanguage().substring(1));
//                } else if (mOptions.get(i).getLanguage().isEmpty()) {
//                    negativeAssociations.add(mOptions.get(i).getText());
//                } else {
//                    negativeAssociations.add(mOptions.get(i).getLanguage());
//                }
//            }
        }

        if (positiveAssociations != null && !positiveAssociations.isEmpty()) {
            finalTexts.add(bullet_hollow);
            for (String string : positiveAssociations) {
                CustomLog.i(TAG, "generateAssociatedSymptomsOrHistory:  " + mLanguagePositive);
                if (!flagPositive) {
                    flagPositive = true;
                    finalTexts.add(mLanguagePositive + " " + string);
                } else {
                    finalTexts.add(" " + string);
                }
            }
        }


        if (negativeAssociations != null && !negativeAssociations.isEmpty()) {
            finalTexts.add(bullet_hollow);
            for (String string : negativeAssociations) {
                CustomLog.i(TAG, "generateAssociatedSymptomsOrHistory:  " + mLanguageNegative);
                if (!flagNegative) {
                    flagNegative = true;
                    finalTexts.add(mLanguageNegative + " " + string);
                } else {
                    finalTexts.add(" " + string);
                }
            }
        }

        String final_language = "";

        if (!finalTexts.isEmpty()) {
            for (int l = 0; l < finalTexts.size(); l++) {
                final_language = final_language + ", " + finalTexts.get(l);
            }
        }

        final_language = final_language.replaceAll("- ,", "- ");
        final_language = final_language.replaceAll("of,", "of");
        final_language = final_language.replaceAll("\\, \\[", " [");
        final_language = final_language.replaceAll(", " + bullet_hollow + ", ", " " + next_line + bullet_hollow + " ");
        CustomLog.i(TAG, "generateAssociatedSymptomsOrHistory: " + final_language);

        return final_language;

    }

    public String formQuestionAnswer(int level, boolean isAssociateSymptomsType) {
        List<String> stringsList = new ArrayList<>();
        List<String> stringsListNoSelected = new ArrayList<>();
        List<Node> mOptions = optionsList;
        boolean flag = false;
        boolean isAssociatedSymEmpty = false;

        for (int i = 0; i < mOptions.size(); i++) {
            //isSelected set from  thisNode.setUnselected(); method
            if (mOptions.get(i).isSelected()) {
                String question;
                if (level == 0) {
                    question = big_bullet + " " + mOptions.get(i).findDisplay();
                    if ((mOptions.get(i).getText().equalsIgnoreCase(Node.ASSOCIATE_SYMPTOMS))
                            || (mOptions.get(i).getText().equalsIgnoreCase("जुड़े लक्षण"))
                            || (mOptions.get(i).getText().equalsIgnoreCase("தொடர்புடைய அறிகுறிகள்"))
                            || (mOptions.get(i).getText().equalsIgnoreCase("ସମ୍ପର୍କିତ ଲକ୍ଷଣଗୁଡ଼ିକ")) || (mOptions.get(i).getText().equalsIgnoreCase("સંકળાયેલ લક્ષણો")) || (mOptions.get(i).getText().equalsIgnoreCase("জড়িত লক্ষণগুলি"))) {
                        question = question + next_line + "Patient reports -";
                    }
                } else {
                    if (isAssociateSymptomsType) {
                        question = right_pointing + " " + mOptions.get(i).findDisplay();
                    } else {

                        if (mOptions.get(i).isTerminal()) {
                            question = bullet + " " + mOptions.get(i).findDisplay();
                        } else {
                            if (level >= 1) {
                                question = right_pointing + " " + mOptions.get(i).findDisplay();
                            } else {
                                question = bullet + " " + mOptions.get(i).findDisplay();
                            }
                            //question = right_pointing + " " + mOptions.get(i).findDisplay();
                        }
                    }
                }
                question = question.replaceAll("\\[(.*?)\\]", "");
                String answer = mOptions.get(i).getLanguage();
                Timber.tag(TAG).i("ipt: +++++++++++++++++++++++++++START++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                Timber.tag(TAG).i("ipt: isTerminal - %s", mOptions.get(i).isTerminal());
                Timber.tag(TAG).i("ipt: level - %s", level);
                Timber.tag(TAG).i("ipt: getInputType %s", mOptions.get(i).getInputType());
                Timber.tag(TAG).i("ipt: findDisplay %s", mOptions.get(i).findDisplay());
                Timber.tag(TAG).i("ipt: getText %s", mOptions.get(i).getText());
                Timber.tag(TAG).i("ipt: answer %s", answer);
                Timber.tag(TAG).i("ipt: question %s", question);
                if (answer.equals("%") && level != 0) {
                    level = level - 1;
                }

                if (mOptions.get(i).isTerminal()) {
                    if (mOptions.get(i).getInputType() != null && !mOptions.get(i).getInputType().trim().isEmpty()) {

                        if (mOptions.get(i).getInputType().equals("camera")) {
                            stringsList.add(bullet_hollow + answer + next_line);
                        } else {
                            if (!answer.isEmpty()) {
                                if (isAssociateSymptomsType) {
                                    if (answer.equals("%")) {
                                    } else if (mOptions.get(i).getText().equals(mOptions.get(i).getLanguage())) {
                                        stringsList.add(question + " " + right_pointing + answer + next_line);
                                    } else if (answer.substring(0, 1).equals("%")) {
                                        stringsList.add(question + " " + right_pointing + answer.substring(1) + next_line);
                                    } else {
                                        stringsList.add(question + " " + right_pointing + answer + next_line);
                                    }
                                } else {
                                    if (answer.equals("%")) {
                                    } else if (mOptions.get(i).getText().trim().equals(mOptions.get(i).getLanguage().trim())) {
                                        stringsList.add((level > 1 ? right_pointing : bullet_hollow) + answer + next_line);
                                    } else if (answer.charAt(0) == '%') {
                                        stringsList.add(bullet_hollow + answer.substring(1) + next_line);
                                    } else {
                                        stringsList.add((level > 1 ? right_pointing : bullet_hollow) + answer + next_line);
                                    }
                                }
                            }
                        }
                    } else {
                        if (isAssociateSymptomsType) {
                            if (level > 0) {
                                stringsList.add(right_pointing + mOptions.get(i).findDisplay() + next_line);
                            } else {
                                stringsList.add(bullet_hollow + mOptions.get(i).findDisplay() + next_line);
                            }

                        } else {
                            if (level == 0) {
                                stringsList.add(bullet_hollow + mOptions.get(i).findDisplay() + next_line);
                            } else {
                                //stringsList.add(bullet_hollow + mOptions.get(i).findDisplay() + "," + next_line);
                                stringsList.add((level > 1 ? right_pointing : bullet_hollow) + mOptions.get(i).findDisplay() + next_line);

                            }

                        }

                    }
                } else {
                    CustomLog.i(TAG, "ipt: nested question " + question);
                    CustomLog.i(TAG, "ipt: nested question level - " + level);
                    if (level > 0 && level % 2 != 0)
                        if (question.startsWith("▻"))
                            question = bullet_hollow + " " + question.substring(1);
                    if (level == 0) {
                        stringsList.add(question + next_line);

                    } else if (!answer.equals("%")) {
                        stringsList.add(question + next_line);
                    }


                    Timber.tag(TAG).i("ipt: nested question %s", question);
                    Timber.tag(TAG).i("ipt: nested answer stringsList%s", stringsList);
                    String temp1 = mOptions.get(i).formQuestionAnswer(level + 1, isAssociateSymptomsType);

                    Timber.tag(TAG).i("ipt: nested answer %s", temp1);
                    temp1 = temp1.replaceAll("<br/>•", ",");
                    if (level == 0)
                        if (temp1.startsWith("▻") && temp1.chars().filter(ch -> ch == '▻').count() > 1)
                            temp1 = bullet_hollow + " " + temp1.substring(1);
                    Timber.tag(TAG).v("ipt: nested answer %s", temp1);
                    stringsList.add(temp1);
                    // cleanup duplicate text
                    String lastVal = "";
                    /*for (int j = 0; j < stringsList.size(); j++) {
                        String v = stringsList.get(j).replaceAll(right_pointing, "").trim().replaceAll(bullet_hollow, "").trim();
                        Log.v(TAG, "ipt: nested answer v" + v);
                        if (!lastVal.isEmpty() && v.startsWith(lastVal)) {
                            stringsList.get(j).replace(lastVal, "");
                        }
                        lastVal = v;
                    }*/
                    if (stringsList.contains(bullet_hollow + NOT_ANSWERED + next_line) || stringsList.contains(right_pointing + NOT_ANSWERED + next_line)) {
                        //stringsList.clear();
                        stringsList.remove(stringsList.size() - 1);
                    }
                    Timber.tag(TAG).i("ipt: stringsList %s", stringsList);
                    Timber.tag(TAG).i("ipt: ******************END********************************* %s", level);
                }
            } else if (mOptions.get(i).getText() != null &&
                    ((mOptions.get(i).getText().equalsIgnoreCase(Node.ASSOCIATE_SYMPTOMS))
                            || (mOptions.get(i).getText().equalsIgnoreCase("जुड़े लक्षण"))
                            || (mOptions.get(i).getText().equalsIgnoreCase("అనుబంధ లక్షణాలు"))
                            || (mOptions.get(i).getText().equalsIgnoreCase("জড়িত লক্ষণগুলি"))
                            || (mOptions.get(i).getText().equalsIgnoreCase("தொடர்புடைய அறிகுறிகள்"))
                            || (mOptions.get(i).getText().equalsIgnoreCase("ସମ୍ପର୍କିତ ଲକ୍ଷଣଗୁଡ଼ିକ")) || (mOptions.get(i).getText().equalsIgnoreCase("સંકળાયેલ લક્ષણો")))) {

                if (!mOptions.get(i).isTerminal()) {
                    stringsList.add(big_bullet + " " + mOptions.get(i).findDisplay() + next_line);
                    stringsList.add(mOptions.get(i).formQuestionAnswer(level + 1, isAssociateSymptomsType));
                }

                if (mOptions.get(i).getOptionsList().size() > 0) {

                    for (int j = 0; j < mOptions.get(i).getOptionsList().size(); j++) {

                        if (mOptions.get(i).getOptionsList().get(j).isSelected()
                                || mOptions.get(i).getOptionsList().get(j).isNoSelected()) {

                            if (!mOptions.get(i).isTerminal()) {
                                stringsList.add(big_bullet + " " + mOptions.get(i).findDisplay() + next_line);
                                stringsList.add(mOptions.get(i).formQuestionAnswer(level + 1, isAssociateSymptomsType));
                            }
                        }
                    }
                }
            } else {
                //in case of weird null exception...
            }

            // to add Patient denies entry
            if (mOptions.get(i).isNoSelected()) {
                if (!flag) {
                    flag = true;
                    stringsListNoSelected.add("Patient denies -" + next_line);
                }
                stringsListNoSelected.add(bullet_hollow + mOptions.get(i).findDisplay() + next_line);
                Timber.tag(TAG).e("ipt: %s", stringsListNoSelected);
            }

        }


        if (stringsListNoSelected.size() > 0) {
            stringsList.addAll(stringsListNoSelected);
        }
        Timber.tag(TAG).

                i("ipt: stringsList: %s", stringsList);

        String mLanguage = "";
        for (
                int i = 0; i < stringsList.size(); i++) {

            if (!stringsList.get(i).isEmpty()) {
                mLanguage = mLanguage.concat(stringsList.get(i));
            }

        }
        Timber.tag(TAG).

                i("ipt: formQuestionAnswer: %s", mLanguage);

        mLanguage = mLanguage.replaceAll(",<br/>•", ",");
        if (mLanguage.endsWith(",")) {
            mLanguage = mLanguage.substring(0, mLanguage.length() - 1);
        }
        Timber.tag(TAG).

                i("ipt: formQuestionAnswer: %s", mLanguage);

        if (mLanguage.equalsIgnoreCase("")) {
            mLanguage = (level == 0 ? bullet_hollow : right_pointing) + NOT_ANSWERED + next_line;
        }
        Timber.tag(TAG).

                i("ipt: +++++++++++++++++++++++++++++++++++END+++++++++++++++++++++++++++++++++++++++++++++");

        return mLanguage;
    }

    @Override
    public String toString() {
        return "Node{" +
                "id='" + id + '\'' +
                ", text='" + text + '\'' +
                ", gender='" + gender + '\'' +
                ", min_age='" + min_age + '\'' +
                ", max_age='" + max_age + '\'' +
                ", display='" + display + '\'' +
                ", display_gujarati='" + display_gujarati + '\'' +
                ", display_malyalam='" + display_malyalam + '\'' +
                ", display_marathi='" + display_marathi + '\'' +
                ", display_assamese='" + display_assamese + '\'' +
                ", display_telugu='" + display_telugu + '\'' +
                ", display_kannada='" + display_kannada + '\'' +
                ", display_bengali='" + display_bengali + '\'' +
                ", display_tamil='" + display_tamil + '\'' +
                ", display_oriya='" + display_oriya + '\'' +
                ", display_cebuno='" + display_cebuno + '\'' +
                ", display_hindi='" + display_hindi + '\'' +
                ", language='" + language + '\'' +
                ", choiceType='" + choiceType + '\'' +
                ", inputType='" + inputType + '\'' +
                ", physicalExams='" + physicalExams + '\'' +
                ", optionsList=" + optionsList +
                ", associatedComplaint='" + associatedComplaint + '\'' +
                ", jobAidFile='" + jobAidFile + '\'' +
                ", jobAidType='" + jobAidType + '\'' +
                ", pop_up='" + pop_up + '\'' +
                ", pop_up_hi='" + pop_up_hi + '\'' +
                ", pop_up_or='" + pop_up_or + '\'' +
                ", positiveCondition='" + positiveCondition + '\'' +
                ", negativeCondition='" + negativeCondition + '\'' +
                ", rootNode=" + rootNode +
                ", complaint=" + complaint +
                ", required=" + required +
                ", terminal=" + terminal +
                ", hasAssociations=" + hasAssociations +
                ", aidAvailable=" + aidAvailable +
                ", selected=" + selected +
                ", subSelected=" + subSelected +
                ", hasPhysicalExams=" + hasPhysicalExams +
                ", hasPopUp=" + hasPopUp +
                ", subPopUp=" + subPopUp +
                ", isNoSelected=" + isNoSelected +
                ", imagePathList=" + imagePathList +
                ", space='" + space + '\'' +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }

    public void fetchAge(float age) {

        //for 1st level
        for (int i = 0; i < optionsList.size(); i++) {
            if (!optionsList.get(i).getMin_age().equalsIgnoreCase("") &&
                    !optionsList.get(i).getMax_age().equalsIgnoreCase("")) {
                if (age < Float.parseFloat(optionsList.get(i).getMin_age().trim())) { //age = 1 , min_age = 5
                    remove(optionsList, i);
                    i--;
                }

                //else if(!optionsList.get(i).getMax_age().equalsIgnoreCase(""))
                else if (age > Float.parseFloat(optionsList.get(i).getMax_age())) { //age = 15 , max_age = 10
                    remove(optionsList, i);
                    i--;
                }
            }
        }

        //2nd level
        for (int i = 0; i < optionsList.size(); i++) {
            if (optionsList.get(i).getOptionsList() != null) {
                for (int j = 0; j < optionsList.get(i).getOptionsList().size(); j++) {
                    if (!optionsList.get(i).getOptionsList()
                            .get(j).getMin_age().equalsIgnoreCase("") &&
                            !optionsList.get(i).getOptionsList()
                                    .get(j).getMax_age().equalsIgnoreCase("")) {
                        if (age < Float.parseFloat(optionsList.get(i).getOptionsList()
                                .get(j).getMin_age())) {
                            remove(optionsList.get(i).getOptionsList(), j);
                            j--;
                        } else if (age > Float.parseFloat(optionsList.get(i).getOptionsList()
                                .get(j).getMax_age())) {
                            remove(optionsList.get(i).getOptionsList(), j);
                            j--;
                        }
                    }
                }
            }
        }

        //3rd level
        for (int i = 0; i < optionsList.size(); i++) {
            if (optionsList.get(i).getOptionsList() != null) {
                for (int j = 0; j < optionsList.get(i).getOptionsList().size(); j++) { //2nd level
                    if (optionsList.get(i).getOptionsList().get(j).getOptionsList() != null) {
                        for (int k = 0; k < optionsList.get(i).getOptionsList().get(j).getOptionsList().size(); k++) {
                            if (!optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getMin_age().equalsIgnoreCase("") && !optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getMax_age().equalsIgnoreCase("")) {
                                if (age < Float.parseFloat(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getMin_age())) {
//                                remove(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList(), k);
                                    remove(optionsList.get(i).getOptionsList().get(j).getOptionsList(), k);
                                    k--;
                                } else if (age > Float.parseFloat(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getMax_age())) {
                                    remove(optionsList.get(i).getOptionsList().get(j).getOptionsList(), k);
                                    k--;
                                }
                            }
                        }
                    }
                }
            }
        }

        //4th level
        for (int i = 0; i < optionsList.size(); i++) {
            if (optionsList.get(i).getOptionsList() != null) {
                for (int j = 0; j < optionsList.get(i).getOptionsList().size(); j++) { //2nd level
                    if (optionsList.get(i).getOptionsList().get(j).getOptionsList() != null) {
                        for (int k = 0; k < optionsList.get(i).getOptionsList().get(j).getOptionsList().size(); k++) {
                            if (optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList() != null) {
                                for (int l = 0; l < optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList().size(); l++) {

                                    if (!optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList().get(l)
                                            .getMin_age().equalsIgnoreCase("") && !optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList().get(l)
                                            .getMax_age().equalsIgnoreCase("")) {

                                        if (age < Float.parseFloat(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList().get(l)
                                                .getMin_age())) {
//                                remove(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList(), k);
                                            remove(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList(), l);
                                            l--;
                                        } else if (age > Float.parseFloat(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList().get(l)
                                                .getMax_age())) {
                                            remove(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList(), l);
                                            l--;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        //5th level
        for (int i = 0; i < optionsList.size(); i++) {
            if (optionsList.get(i).getOptionsList() != null) {
                for (int j = 0; j < optionsList.get(i).getOptionsList().size(); j++) { //2nd level
                    if (optionsList.get(i).getOptionsList().get(j).getOptionsList() != null) {
                        for (int k = 0; k < optionsList.get(i).getOptionsList().get(j).getOptionsList().size(); k++) {
                            if (optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList() != null) {
                                for (int l = 0; l < optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList().size(); l++) {
                                    if (optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList()
                                            .get(l).getOptionsList() != null) {
                                        for (int m = 0; m < optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList()
                                                .get(l).getOptionsList().size(); m++) {

                                            if (!optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList()
                                                    .get(l).getOptionsList().get(m).getMin_age().equalsIgnoreCase("") && !optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList()
                                                    .get(l).getOptionsList().get(m).getMax_age().equalsIgnoreCase("")) {
                                                if (age < Float.parseFloat(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList()
                                                        .get(l).getOptionsList().get(m).getMin_age())) {
//                                remove(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList(), k);
                                                    remove(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList()
                                                            .get(l).getOptionsList(), m);
                                                    m--;
                                                } else if (age > Float.parseFloat(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList()
                                                        .get(l).getOptionsList().get(m).getMax_age())) {

                                                    remove(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList()
                                                            .get(l).getOptionsList(), m);
                                                    m--;

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


    }

    public void fetchItem(String s) {

        //for 1st level
        for (int i = 0; i < optionsList.size(); i++) {
            if (optionsList.get(i).getGender().equalsIgnoreCase(s)) {
                remove(optionsList, i);
                i--;
            }
        }

        //2nd level
        for (int i = 0; i < optionsList.size(); i++) {
            if (optionsList.get(i).getOptionsList() != null) {
                for (int j = 0; j < optionsList.get(i).getOptionsList().size(); j++) {
                    if (optionsList.get(i).getOptionsList().get(j).getGender().equalsIgnoreCase(s)) {
                        remove(optionsList.get(i).getOptionsList(), j);
                        j--;
                    }
                }
            }
        }

        //3rd level
        for (int i = 0; i < optionsList.size(); i++) {
            if (optionsList.get(i).getOptionsList() != null) {
                for (int j = 0; j < optionsList.get(i).getOptionsList().size(); j++) { //2nd level
                    if (optionsList.get(i).getOptionsList().get(j).getOptionsList() != null) {
                        for (int k = 0; k < optionsList.get(i).getOptionsList().get(j).getOptionsList().size(); k++) {
                            if (optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getGender().equalsIgnoreCase(s)) {
//                                remove(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList(), k);
                                remove(optionsList.get(i).getOptionsList().get(j).getOptionsList(), k);
                                k--;
                            }
                        }
                    }
                }
            }
        }

        //4th level
        for (int i = 0; i < optionsList.size(); i++) {
            if (optionsList.get(i).getOptionsList() != null) {
                for (int j = 0; j < optionsList.get(i).getOptionsList().size(); j++) { //2nd level
                    if (optionsList.get(i).getOptionsList().get(j).getOptionsList() != null) {
                        for (int k = 0; k < optionsList.get(i).getOptionsList().get(j).getOptionsList().size(); k++) {
                            if (optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList() != null) {
                                for (int l = 0; l < optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList().size(); l++) {
                                    if (optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList().get(l)
                                            .getGender().equalsIgnoreCase(s)) {
//                                remove(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList(), k);
                                        remove(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList(), l);
                                        l--;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        //5th level
        for (int i = 0; i < optionsList.size(); i++) {
            if (optionsList.get(i).getOptionsList() != null) {
                for (int j = 0; j < optionsList.get(i).getOptionsList().size(); j++) { //2nd level
                    if (optionsList.get(i).getOptionsList().get(j).getOptionsList() != null) {
                        for (int k = 0; k < optionsList.get(i).getOptionsList().get(j).getOptionsList().size(); k++) {
                            if (optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList() != null) {
                                for (int l = 0; l < optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList().size(); l++) {
                                    if (optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList()
                                            .get(l).getOptionsList() != null) {
                                        for (int m = 0; m < optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList()
                                                .get(l).getOptionsList().size(); m++) {

                                            if (optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList()
                                                    .get(l).getOptionsList().get(m).getGender().equalsIgnoreCase(s)) {
//                                remove(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList(), k);
                                                remove(optionsList.get(i).getOptionsList().get(j).getOptionsList().get(k).getOptionsList()
                                                        .get(l).getOptionsList(), m);
                                                m--;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


    }

    // removes options specific to index from the json
    public void remove(List<Node> no, int index) {
        no.remove(index);
    }

    //Check to see if all required exams have been answered before moving on.
    public AnswerResult checkAllRequiredAnswered(Context context) {

        SessionManager sessionManager = null;
        sessionManager = new SessionManager(context);
        String locale = sessionManager.getCurrentLang();

        AnswerResult answerResult = new AnswerResult();
        answerResult.totalCount = optionsList.size();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(context.getResources().getString(R.string.answer_following_questions));
        stringBuilder.append("\n");
        for (int i = 0; i < optionsList.size(); i++) {
            Node node = optionsList.get(i);
            new VisitUtils().updateParentNodesIfSelectedAndDataCaptured(node);
            if (node.isRequired()) {
                if (node.optionsList != null && !node.optionsList.isEmpty()) {
                    if (!node.isSelected() || !node.anySubSelected() || (node.isSelected() && !isNestedMandatoryOptionsAnswered(node))) {
                        switch (locale) {
                            case "en":
                                stringBuilder.append("\n").append(bullet + " ").append(node.display);
                                break;
                            case "hi":
                                stringBuilder.append("\n").append(bullet + " ").append(node.display_hindi);
                                break;
                        }
                        answerResult.result = false;
                    }
                } else {
                    if (!node.isSelected()) {
                        switch (locale) {
                            case "en":
                                stringBuilder.append("\n").append(bullet + " ").append(node.display);
                                break;
                            case "hi":
                                stringBuilder.append("\n").append(bullet + " ").append(node.display_hindi);
                                break;
                        }
                        answerResult.result = false;
                    }
                }

                CustomLog.v(TAG, "checkAllRequiredAnswered - " + node.text);
                CustomLog.v(TAG, "checkAllRequiredAnswered - " + String.valueOf(node.isSelected()));
            }
            CustomLog.v(TAG, "checkAllRequiredAnswered requiredStrings - " + stringBuilder.toString());
        }
        answerResult.requiredStrings = stringBuilder.toString();
        return answerResult;
    }

    //
    /*private Node parentNode;

    public Node getParentNode() {
        return parentNode;
    }

    public void setParentNode(Node parentNode) {
        this.parentNode = parentNode;
    }*/


    public AnswerResult checkAllRequiredAnsweredRootNode(Context context) {

        SessionManager sessionManager = null;
        sessionManager = new SessionManager(context);
        String locale = sessionManager.getCurrentLang();

        AnswerResult answerResult = new AnswerResult();
        answerResult.totalCount = optionsList.size();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(context.getResources().getString(R.string.answer_following_questions));
        stringBuilder.append("\n");
        answerResult.result = true;

        if (isRequired()) {
            if (optionsList != null && !optionsList.isEmpty()) {
                if (!isAllAssociatedSymptomsOptionsAnswered()) {
                    switch (locale) {
                        case "en":
                            stringBuilder.append("\n").append(bullet + " ").append(display);
                            break;
                        case "hi":
                            stringBuilder.append("\n").append(bullet + " ").append(display_hindi);
                            break;
                    }
                    answerResult.result = false;
                }
            } else {
                if (!isSelected()) {
                    switch (locale) {
                        case "en":
                            stringBuilder.append("\n").append(bullet + " ").append(display);
                            break;
                        case "hi":
                            stringBuilder.append("\n").append(bullet + " ").append(display_hindi);
                            break;
                    }
                    answerResult.result = false;
                }
            }

            CustomLog.v(TAG, "checkAllRequiredAnsweredRootNode - " + text);
            CustomLog.v(TAG, "checkAllRequiredAnsweredRootNode isSelected - " + String.valueOf(isSelected()));
            CustomLog.v(TAG, "checkAllRequiredAnsweredRootNode isNoSelected - " + String.valueOf(isNoSelected()));
            CustomLog.v(TAG, "checkAllRequiredAnsweredRootNode anySubSelected - " + String.valueOf(anySubSelected()));
            CustomLog.v(TAG, "checkAllRequiredAnsweredRootNode isNestedMandatoryOptionsAnswered - " + String.valueOf(isNestedMandatoryOptionsAnswered(this)));

        }

        answerResult.requiredStrings = stringBuilder.toString();
        return answerResult;
    }

    public boolean isNestedMandatoryOptionsAnswered(Node node) {
        CustomLog.v(TAG, "isNestedMandatory - " + new Gson().toJson(node).toString());
        boolean allAnswered = node.isSelected();
        /*if(node.isSelected() && node.isRequired() && node.optionsList.size()==1){
            if(!node.optionsList.get(0).isSelected()){
                return  false;
            }
        }*/
        if (node.optionsList != null && !node.optionsList.isEmpty()) {
            for (int i = 0; i < node.optionsList.size(); i++) {
                Node innerNode = node.optionsList.get(i);
                if (innerNode.isRequired() && innerNode.isSelected() && innerNode.optionsList != null && !innerNode.optionsList.isEmpty()) {
                    if (!isNestedMandatoryOptionsAnswered(innerNode)) {
                        allAnswered = false;
                        break;
                    }
                }
            }

        }
        CustomLog.v(TAG, "isNestedMandatory allAnswered - " + allAnswered);
        return allAnswered;
    }

    public boolean isAllAssociatedSymptomsOptionsAnswered() {
        CustomLog.v(TAG, "isAllAssociatedSymptomsOptionsAnswered - " + new Gson().toJson(this).toString());
        boolean allAnswered = true;
        /*if(node.isSelected() && node.isRequired() && node.optionsList.size()==1){
            if(!node.optionsList.get(0).isSelected()){
                return  false;
            }
        }*/
        if (optionsList != null && !optionsList.isEmpty()) {
            for (int i = 0; i < optionsList.size(); i++) {
                Node innerNode = optionsList.get(i);
                CustomLog.v(TAG, "isNestedMandatory innerNode findDisplay- " + innerNode.findDisplay());
                CustomLog.v(TAG, "isNestedMandatory innerNode isNoSelected- " + innerNode.isNoSelected());
                CustomLog.v(TAG, "isNestedMandatory innerNode isNoSelected- " + innerNode.isSelected());
                if (!innerNode.isNoSelected() && !innerNode.isSelected()) {
                    allAnswered = false;
                    break;

                }
            }

        }
        CustomLog.v(TAG, "isNestedMandatory allAnswered - " + allAnswered);
        return allAnswered;
    }

    public boolean checkIsAnswered() {
        Timber.tag(TAG).d("Node => %s", findDisplay());
        boolean isAnswered = isSelected();
        if (isRequired() && !isSelected()) return false;

        if (optionsList != null && !optionsList.isEmpty()) {
            ArrayList<Node> selectedNodes = getSelectedNode();
            if (isRequired() && selectedNodes.isEmpty()) return false;
            for (Node child : selectedNodes) {
                if (!child.checkIsAnswered()) {
                    isAnswered = false;
                    break;
                }
            }
        } else if (isUserInputsTypeNode()) isAnswered = isDataCaptured();

        return isAnswered;
    }

    public ArrayList<Node> getSelectedNode() {
        ArrayList<Node> nodes = new ArrayList<>();
        for (Node node : optionsList) {
            if (node.isSelected()) nodes.add(node);
        }
        return nodes;
    }

    public boolean isNestedMandatoryOptionsAnswered() {
        CustomLog.v(TAG, "isNestedMandatory isNestedMandatoryOptionsAnswered - " + findDisplay());
        CustomLog.v(TAG, "isNestedMandatory isNestedMandatoryOptionsAnswered isDataCaptured- " + isDataCaptured());
        boolean allAnswered = isSelected();
        if (isMultiChoice() && !isDataCaptured()) {
            allAnswered = false;
        }
        /*if(node.isSelected() && node.isRequired() && node.optionsList.size()==1){
            if(!node.optionsList.get(0).isSelected()){
                return  false;
            }
        }*/
        if (optionsList != null && !optionsList.isEmpty()) {
            int countSelected = 0;
            for (int i = 0; i < optionsList.size(); i++) {
                Node innerNode = optionsList.get(i);
                if (innerNode.isSelected()) {
                    CustomLog.v(TAG, innerNode.isRequired() + " = isNestedMandatory - " + innerNode.findDisplay() + " innerNode.isDataCaptured() - " + innerNode.isDataCaptured());
                    countSelected++;
                    if (innerNode.isRequired()) {
                        //if (innerNode.optionsList != null && !innerNode.optionsList.isEmpty()) {
                        if (!innerNode.isTerminal()) {
                            if (!innerNode.isNestedMandatoryOptionsAnswered()) {
                                allAnswered = false;
                                break;
                            }
                        } else {
                            if (innerNode.isUserInputsTypeNode() && !innerNode.isDataCaptured()) {
                                allAnswered = false;
                                break;
                            }
                        }
                    }
                }
            }
            if (isSelected()) {
                allAnswered = countSelected != 0;
            }

        }
        CustomLog.v(TAG, "isNestedMandatory allAnswered result - " + allAnswered + "\t" + findDisplay());
        return allAnswered;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public boolean isDataCaptured() {
        return isDataCaptured;
    }

    public void setDataCaptured(boolean dataCaptured) {
        isDataCaptured = dataCaptured;
    }

    public boolean isHavingNestedQuestion() {
        return havingNestedQuestion;
    }

    public void setHavingNestedQuestion(boolean havingNestedQuestion) {
        this.havingNestedQuestion = havingNestedQuestion;
    }

    public boolean isEnableExclusiveOption() {
        return enableExclusiveOption;
    }

    public void setEnableExclusiveOption(boolean enableExclusiveOption) {
        this.enableExclusiveOption = enableExclusiveOption;
    }

    public boolean isExclusiveOption() {
        return isExclusiveOption;
    }

    public void setExclusiveOption(boolean exclusiveOption) {
        isExclusiveOption = exclusiveOption;
    }

    public String getCompareDuplicateNode() {
        return compareDuplicateNode;
    }

    public void setCompareDuplicateNode(String compareDuplicateNode) {
        this.compareDuplicateNode = compareDuplicateNode;
    }

    /**
     * unselect all nested Node recursively
     */
    public void unselectAllNestedNode() {
        CustomLog.v(TAG, "unselectAllNestedNode - " + getText());
        if (optionsList != null) {
            for (int i = 0; i < optionsList.size(); i++) {
                optionsList.get(i).setSelected(false);
                optionsList.get(i).setDataCaptured(false);
                if (optionsList.get(i).optionsList != null) {
                    optionsList.get(i).unselectAllNestedNode();
                }
            }
        }
    }

    public void removeImagesAllNestedNode() {
        CustomLog.v(TAG, "removeImagesAllNestedNode - " + getText());
        setImageUploaded(false);
        setImagePath("");
        setImagePathList(new ArrayList<String>());
        if (optionsList != null) {
            for (int i = 0; i < optionsList.size(); i++) {
                optionsList.get(i).setImageUploaded(false);
                optionsList.get(i).setImagePath("");
                optionsList.get(i).setImagePathList(new ArrayList<String>());
                if (optionsList.get(i).optionsList != null) {
                    optionsList.get(i).removeImagesAllNestedNode();
                }
            }
        }
    }

    public boolean isImageUploaded() {
        return isImageUploaded;
    }

    public void setImageUploaded(boolean imageUploaded) {
        isImageUploaded = imageUploaded;
    }

    /*Node Engine - 3.0 support with new UI*/
    public String getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }

    private int nestedLeve = 0;

    public int getNestedLeve() {
        return nestedLeve;
    }

    public void setNestedLeve(int nestedLeve) {
        this.nestedLeve = nestedLeve;
    }


    public int foundTheNestedQuestionType() {
        int type = -1;
        if (getOptionsList() != null) {
            if (getOptionsList().size() == 1) {
                type = DIRECT_USER_INPUT_CHILD;
            } else if (getOptionsList().size() >= 1 && nestedLeve == 0) {
                type = CHILD_OPTIONS;
            } else if (getOptionsList().size() >= 1 && nestedLeve != 0) {
                type = CHILD_QUESTION;
            }
        }
        return type;
    }

    public boolean isContainsTheQuestionBeforeOptions() {
        boolean flag = false;
        if (getOptionsList() != null) {
            flag = getOptionsList().size() == 1;
        }
        return flag;
    }

    public boolean isHavingMoreNestedQuestion() {
        if (isHavingNestedQuestion()) {
            return true;
        }
        boolean flag = true;
        if (getOptionsList() != null) {
            for (int i = 0; i < getOptionsList().size(); i++) {
                Node _node = getOptionsList().get(i);
                if (_node.getOptionsList() == null || _node.getOptionsList().size() != 1) {
                    flag = false;
                    break;
                }
            }
        }
        return flag;
    }

    public boolean isSingleNodeWithNestedQA() {
        if (isHavingNestedQuestion()) {
            return true;
        }
        boolean flag = true;
        if (getOptionsList() != null && getOptionsList().size() == 1) {
            Node _node = getOptionsList().get(0);
            if (_node.getOptionsList() == null || _node.getOptionsList().size() >= 1) {
                flag = false;

            }

        }
        return flag;
    }

    public boolean isUserInputsTypeNode() {
        boolean result = false;
        String type = "";
        if (isHavingNestedQuestion()) {
            type = getOptionsList() != null && getOptionsList().size() == 1 && getOptionsList().get(0).getOptionsList() != null && getOptionsList().get(0).getOptionsList().size() == 1 ? getOptionsList().get(0).getOptionsList().get(0).getInputType() : getInputType();
        } else {
            type = getOptionsList() != null && getOptionsList().size() == 1 ? getOptionsList().get(0).getInputType() : getInputType();
        }

        CustomLog.v(TAG, "isUserInputsTypeNode - type : " + getText() + "\t=" + type);
        if (type.equals("text") || type.equals("date") || type.equals("location") || type.equals("number") || type.equals("area") || type.equals("duration") || type.equals("range") || type.equals("frequency")) {
            result = true;
        }
        CustomLog.v(TAG, "isUserInputsTypeNode - result : " + result);
        return result;
    }

    /*public String formQuestionAnswerV2(int level) {
        List<String> stringsList = new ArrayList<>();
        List<String> stringsListNoSelected = new ArrayList<>();
        List<Node> mOptions = optionsList;
        boolean flag = false;
        boolean isAssociatedSymEmpty = false;

        for (int i = 0; i < mOptions.size(); i++) {
            //isSelected set from  thisNode.setUnselected(); method
            if (mOptions.get(i).isSelected()) {
                String question;
                if (level == 0) {
                    question = big_bullet + " " + mOptions.get(i).findDisplay();

                } else {
                    question = bullet + " " + mOptions.get(i).findDisplay();
                }
                String answer = mOptions.get(i).getLanguage();
                if (mOptions.get(i).isTerminal()) {
                    if (mOptions.get(i).getInputType() != null && !mOptions.get(i).getInputType().trim().isEmpty()) {

                        if (mOptions.get(i).getInputType().equals("camera")) {
                        } else {
                            if (!answer.isEmpty()) {
                                if (answer.equals("%")) {
                                } else if (mOptions.get(i).getText().equals(mOptions.get(i).getLanguage())) {
                                    stringsList.add(bullet_hollow + answer + next_line);
                                } else if (answer.substring(0, 1).equals("%")) {
                                    stringsList.add(bullet_hollow + answer.substring(1) + next_line);
                                } else {
                                    stringsList.add(bullet_hollow + answer + next_line);
                                }
                            }
                        }
                    } else {
                        stringsList.add(bullet_hollow + mOptions.get(i).findDisplay() + next_line);
                    }
                } else {

                    stringsList.add(question + next_line);
                    stringsList.add(mOptions.get(i).formQuestionAnswerV2(level + 1));
                    CustomLog.i(TAG, "ipt: stringsList " + stringsList);
                }
            } else if (mOptions.get(i).getText() != null &&
                    ((mOptions.get(i).getText().equalsIgnoreCase("Associated symptoms"))
                            || (mOptions.get(i).getText().equalsIgnoreCase("जुड़े लक्षण"))
                            || (mOptions.get(i).getText().equalsIgnoreCase("అనుబంధ లక్షణాలు"))
                            || (mOptions.get(i).getText().equalsIgnoreCase("জড়িত লক্ষণগুলি"))
                            || (mOptions.get(i).getText().equalsIgnoreCase("தொடர்புடைய அறிகுறிகள்"))
                            || (mOptions.get(i).getText().equalsIgnoreCase("ସମ୍ପର୍କିତ ଲକ୍ଷଣଗୁଡ଼ିକ")) || (mOptions.get(i).getText().equalsIgnoreCase("સંકળાયેલ લક્ષણો")))) {

                if (!mOptions.get(i).isTerminal()) {
                    stringsList.add(big_bullet + " " + mOptions.get(i).findDisplay() + next_line);
                    stringsList.add(mOptions.get(i).formQuestionAnswerV2(level + 1));
                }
                String v = formQuestionAnswerForAssociateSymptomsV2(level);
                if (mOptions.get(i).getOptionsList().size() > 0) {

                    for (int j = 0; j < mOptions.get(i).getOptionsList().size(); j++) {

                        if (mOptions.get(i).getOptionsList().get(j).isSelected()
                                || mOptions.get(i).getOptionsList().get(j).isNoSelected()) {

                            if (!mOptions.get(i).isTerminal()) {
                                stringsList.add(big_bullet + " " + mOptions.get(i).findDisplay() + next_line);
                                stringsList.add(mOptions.get(i).formQuestionAnswerV2(level + 1));
                            }
                        }
                    }
                }
            }

            // to add Patient denies entry
            if (mOptions.get(i).isNoSelected()) {
                if (!flag) {
                    flag = true;
                    stringsListNoSelected.add("Patient denies -" + next_line);
                }
                stringsListNoSelected.add(bullet_hollow + mOptions.get(i).findDisplay() + next_line);
                CustomLog.e(TAG, "ipt: " + stringsListNoSelected);
            }
        }


        if (stringsListNoSelected.size() > 0) {
            stringsList.addAll(stringsListNoSelected);
        }

        String mLanguage = "";
        for (int i = 0; i < stringsList.size(); i++) {

            if (!stringsList.get(i).isEmpty()) {
                mLanguage = mLanguage.concat(stringsList.get(i));
            }

        }


        return mLanguage;
    }*/

    /*public String formQuestionAnswerForAssociateSymptomsV2(int level) {
        List<String> stringsList = new ArrayList<>();
        List<String> stringsListNoSelected = new ArrayList<>();
        List<Node> mOptions = optionsList;
        boolean flag = false;
        boolean isAssociatedSymEmpty = false;

        for (int i = 0; i < mOptions.size(); i++) {
            //isSelected set from  thisNode.setUnselected(); method
            if (mOptions.get(i).isSelected()) {
                String question;
                if (level == 0) {
                    question = big_bullet + " " + mOptions.get(i).findDisplay();
                    if ((mOptions.get(i).getText().equalsIgnoreCase("Associated symptoms"))
                            || (mOptions.get(i).getText().equalsIgnoreCase("जुड़े लक्षण"))
                            || (mOptions.get(i).getText().equalsIgnoreCase("தொடர்புடைய அறிகுறிகள்"))
                            || (mOptions.get(i).getText().equalsIgnoreCase("ସମ୍ପର୍କିତ ଲକ୍ଷଣଗୁଡ଼ିକ")) || (mOptions.get(i).getText().equalsIgnoreCase("સંકળાયેલ લક્ષણો")) || (mOptions.get(i).getText().equalsIgnoreCase("জড়িত লক্ষণগুলি"))) {
                        question = question + next_line + "Patient reports -";
                    }
                } else {
                    question = bullet + " " + mOptions.get(i).findDisplay();
                }
                String answer = mOptions.get(i).getLanguage();
                if (mOptions.get(i).isTerminal()) {
                    if (mOptions.get(i).getInputType() != null && !mOptions.get(i).getInputType().trim().isEmpty()) {

                        if (mOptions.get(i).getInputType().equals("camera")) {
                        } else {
                            if (!answer.isEmpty()) {
                                if (answer.equals("%")) {
                                } else if (mOptions.get(i).getText().equals(mOptions.get(i).getLanguage())) {
                                    stringsList.add(bullet_hollow + answer + next_line);
                                } else if (answer.substring(0, 1).equals("%")) {
                                    stringsList.add(bullet_hollow + answer.substring(1) + next_line);
                                } else {
                                    stringsList.add(bullet_hollow + answer + next_line);
                                }
                            }
                        }
                    } else {
                        stringsList.add(bullet_hollow + mOptions.get(i).findDisplay() + next_line);
                    }
                } else {

                    stringsList.add(question + next_line);
                    stringsList.add(mOptions.get(i).formQuestionAnswer(level + 1));
                    CustomLog.i(TAG, "ipt: stringsList " + stringsList);
                }
            } else if (mOptions.get(i).getText() != null &&
                    ((mOptions.get(i).getText().equalsIgnoreCase("Associated symptoms"))
                            || (mOptions.get(i).getText().equalsIgnoreCase("जुड़े लक्षण"))
                            || (mOptions.get(i).getText().equalsIgnoreCase("అనుబంధ లక్షణాలు"))
                            || (mOptions.get(i).getText().equalsIgnoreCase("জড়িত লক্ষণগুলি"))
                            || (mOptions.get(i).getText().equalsIgnoreCase("தொடர்புடைய அறிகுறிகள்"))
                            || (mOptions.get(i).getText().equalsIgnoreCase("ସମ୍ପର୍କିତ ଲକ୍ଷଣଗୁଡ଼ିକ")) || (mOptions.get(i).getText().equalsIgnoreCase("સંકળાયેલ લક્ષણો")))) {

                if (!mOptions.get(i).isTerminal()) {
                    stringsList.add(big_bullet + " " + mOptions.get(i).findDisplay() + next_line);
                    stringsList.add(mOptions.get(i).formQuestionAnswer(level + 1));
                }

                if (mOptions.get(i).getOptionsList().size() > 0) {

                    for (int j = 0; j < mOptions.get(i).getOptionsList().size(); j++) {

                        if (mOptions.get(i).getOptionsList().get(j).isSelected()
                                || mOptions.get(i).getOptionsList().get(j).isNoSelected()) {

                            if (!mOptions.get(i).isTerminal()) {
                                stringsList.add(big_bullet + " " + mOptions.get(i).findDisplay() + next_line);
                                stringsList.add(mOptions.get(i).formQuestionAnswer(level + 1));
                            }
                        }
                    }
                }
            }

            // to add Patient denies entry
            if (mOptions.get(i).isNoSelected()) {
                if (!flag) {
                    flag = true;
                    stringsListNoSelected.add("Patient denies -" + next_line);
                }
                stringsListNoSelected.add(bullet_hollow + mOptions.get(i).findDisplay() + next_line);
                CustomLog.e(TAG, "ipt: " + stringsListNoSelected);
            }
        }


        if (stringsListNoSelected.size() > 0) {
            stringsList.addAll(stringsListNoSelected);
        }

        String mLanguage = "";
        for (int i = 0; i < stringsList.size(); i++) {

            if (!stringsList.get(i).isEmpty()) {
                mLanguage = mLanguage.concat(stringsList.get(i));
            }

        }


        return mLanguage;
    }*/

    public boolean isSkipped() {
        return isSkipped;
    }

    public void setSkipped(boolean skipped) {
        isSkipped = skipped;
    }

    public HashMap<String, String> getImagePathListWithSectionTag() {
        return imagePathListWithSectionTag;
    }

    public void setImagePathListWithSectionTag(HashMap<String, String> imagePathListWithSectionTag) {
        this.imagePathListWithSectionTag = imagePathListWithSectionTag;
    }

    public boolean isFoundCompareAttribute() {
        CustomLog.v(TAG, "isFoundCompareAttribute - " + getText());
        if (compareDuplicateNode != null && !compareDuplicateNode.isEmpty()) return true;
        if (optionsList != null) {
            for (int i = 0; i < optionsList.size(); i++) {
                optionsList.get(i).isFoundCompareAttribute();
            }
        }
        return false;
    }

    public boolean isEnableSubmit() {
        return enableSubmit;
    }

    public void setEnableSubmit(boolean enableSubmit) {
        this.enableSubmit = enableSubmit;
    }

    public void setShowCalendarHeader(boolean showCalendarHeader) {
        this.showCalendarHeader = showCalendarHeader;
    }

    public boolean isShowCalendarHeader() {
        return showCalendarHeader;
    }

    /*End*/
}

