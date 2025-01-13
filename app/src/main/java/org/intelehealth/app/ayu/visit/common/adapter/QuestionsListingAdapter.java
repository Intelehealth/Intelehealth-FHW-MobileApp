package org.intelehealth.app.ayu.visit.common.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;

import org.intelehealth.app.BuildConfig;

import org.intelehealth.app.utilities.CustomLog;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.ajalt.timberkt.Timber;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;
import com.google.gson.Gson;

import org.intelehealth.app.BuildConfig;
import org.intelehealth.app.R;
import org.intelehealth.app.ayu.visit.common.OnItemSelection;
import org.intelehealth.app.ayu.visit.common.VisitUtils;
import org.intelehealth.app.ayu.visit.model.ComplainBasicInfo;
import org.intelehealth.app.ayu.visit.reason.adapter.OptionsChipsGridAdapter;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.knowledgeEngine.PhysicalExam;
import org.intelehealth.app.models.AnswerResult;
import org.intelehealth.app.shared.FirstLetterUpperCaseInputFilter;
import org.intelehealth.app.utilities.CustomLog;
import org.intelehealth.app.utilities.DialogUtils;
import org.intelehealth.app.utilities.FlavorKeys;
import org.intelehealth.app.utilities.SessionManager;
import org.intelehealth.app.utilities.WindowsUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;


public class QuestionsListingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = "QuestionsListingAdapter";
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;

    private Context mContext;
    private List<Node> mItemList = new ArrayList<Node>();
    //private int mTotalQuery = 0;
    RecyclerView mRecyclerView;
    private int mLastImageCaptureSelectedNodeIndex = 0;
    private String engineVersion = "3.0";

    public String getEngineVersion() {
        CustomLog.v(TAG, "engineVersion - " + engineVersion);
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        CustomLog.v(TAG, "setEngineVersion - " + engineVersion);
        if (engineVersion == null) return;
        this.engineVersion = engineVersion;
    }

    public void addImageInLastNode(String image) {

        mItemList.get(mLastImageCaptureSelectedNodeIndex).getImagePathList().add(image);
        if (mIsForPhysicalExam) {
            final String parent_name = mPhysicalExam.getExamParentNodeName(mLastImageCaptureSelectedNodeIndex);
            mItemList.get(mLastImageCaptureSelectedNodeIndex).getImagePathListWithSectionTag().put(image, parent_name);
            CustomLog.v("showCameraView", "addImageInLastNode getImagePathListWithSectionTag - " + mItemList.get(mLastImageCaptureSelectedNodeIndex).getImagePathListWithSectionTag());

        }

        CustomLog.v("showCameraView", "addImageInLastNode mLastImageCaptureSelectedNodeIndex - " + mLastImageCaptureSelectedNodeIndex);
        CustomLog.v("showCameraView", "addImageInLastNode - " + new Gson().toJson(mItemList.get(mLastImageCaptureSelectedNodeIndex)));
        notifyItemChanged(mLastImageCaptureSelectedNodeIndex);
        VisitUtils.scrollNow(mRecyclerView, 1000, 0, 700, mIsEditMode, false);
    }

    public void removeImageInLastNode(int nodeIndex, int imageIndex, String imageName) {
        CustomLog.v("showCameraView", "removeImageInLastNode nodeIndex - " + nodeIndex);
        CustomLog.v("showCameraView", "removeImageInLastNode imageIndex - " + imageIndex);
        CustomLog.v("showCameraView", "removeImageInLastNode imageName - " + imageName);
        CustomLog.v("showCameraView", "removeImageInLastNode - " + new Gson().toJson(mItemList.get(nodeIndex)));
        if (mItemList.get(nodeIndex).getImagePathList() != null && mItemList.get(nodeIndex).getImagePathList().size() > 0)
            mItemList.get(nodeIndex).getImagePathList().remove(imageIndex);
        for (int i = 0; i < mItemList.get(nodeIndex).getOptionsList().size(); i++) {
            if (mItemList.get(nodeIndex).getOptionsList().get(i).getInputType().equalsIgnoreCase("camera")) {
                if (mItemList.get(nodeIndex).getOptionsList().get(i).getImagePathList() != null && mItemList.get(nodeIndex).getOptionsList().get(i).getImagePathList().size() > 0)
                    mItemList.get(nodeIndex).getOptionsList().get(i).getImagePathList().remove(imageIndex);
            }
        }
        notifyItemChanged(nodeIndex);
        VisitUtils.scrollNow(mRecyclerView, 1000, 0, 700, mIsEditMode, mLoadedIds.contains(mItemList.get(nodeIndex).getId()));
    }

    public boolean isIsAssociateSymptomsLoaded() {
        return mIsAssociateSymptomsLoaded;
    }

    public void setAssociateSymptomsLoaded(boolean mIsAssociateSymptomsLoaded) {
        CustomLog.v(TAG, "setAssociateSymptomsLoaded()");
        this.mIsAssociateSymptomsLoaded = mIsAssociateSymptomsLoaded;
    }


    private OnItemSelection mOnItemSelection;
    private boolean mIsForPhysicalExam;
    private boolean mIsFromAssociatedSymptoms;
    private PhysicalExam mPhysicalExam;
    private HashMap<Integer, ComplainBasicInfo> mRootComplainBasicInfoHashMap = new HashMap<>();
    private int mRootIndex = 0;
    private boolean mIsAssociateSymptomsLoaded = false;
    private boolean mIsAssociateSymptomsNestedQuery = false;
    private HashMap<Integer, Integer> mIndexMappingHashMap = new HashMap<>();
    private boolean mIsEditMode;

    private HashMap<Integer, String> mMindMapVersionMappingHashMap = new HashMap<>();
    public Set<String> mLoadedIds = new HashSet<String>();

    public QuestionsListingAdapter(
            RecyclerView recyclerView,
            Context context,
            boolean isFromAssociatedSymptoms,
            boolean isPhyExam,
            PhysicalExam physicalExam,
            int rootIndex,
            HashMap<Integer, ComplainBasicInfo> complainBasicInfoHashMap,
            boolean editMode,
            OnItemSelection onItemSelection
    ) {
        mContext = context;
        mIsFromAssociatedSymptoms = isFromAssociatedSymptoms;
        mIsForPhysicalExam = isPhyExam;
        mPhysicalExam = physicalExam;
        mRecyclerView = recyclerView;
        mOnItemSelection = onItemSelection;
        //mTotalQuery = totalQuery;
        mRootIndex = rootIndex;
        mRootComplainBasicInfoHashMap = complainBasicInfoHashMap;
        mIsEditMode = editMode;
        //mAnimator = new RecyclerViewAnimator(recyclerView);
    }

    public void setRootNodeIndex(int rootIndex) {
        CustomLog.v(TAG, "setRootNodeIndex()");
        mRootIndex = rootIndex;
    }

    public void setAssociateSymptomNestedQueryFlag(boolean isAssociateSymptom) {
        CustomLog.v(TAG, "setAssociateSymptomNestedQueryFlag()");
        mIsAssociateSymptomsNestedQuery = isAssociateSymptom;
    }

    private JSONObject mThisScreenLanguageJsonObject = new JSONObject();

    public void addItem(Node node, String engineVersion) {
        CustomLog.v(TAG, "addItem() engineVersion - " + engineVersion);
        mItemList.add(node);
        int key = mItemList.size() - 1;
        if (!mIndexMappingHashMap.containsKey(key)) mIndexMappingHashMap.put(key, mRootIndex);

        if (engineVersion == null || engineVersion.isEmpty()) {
            engineVersion = "3.0";
        }
        if (!mMindMapVersionMappingHashMap.containsKey(key)) {
            setEngineVersion(engineVersion);
            mMindMapVersionMappingHashMap.put(key, engineVersion);
        }

        CustomLog.v(TAG, "mIndexMappingHashMap - " + new Gson().toJson(mIndexMappingHashMap));
        notifyItemInserted(key);
    }

    public void addItemAll(List<Node> nodes) {
        mItemList = nodes;

        for (int i = 0; i < mItemList.size(); i++) {
            if (!mIndexMappingHashMap.containsKey(i)) mIndexMappingHashMap.put(i, mRootIndex);
        }

        notifyDataSetChanged();
    }

    public List<Node> geItems() {
        return mItemList;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ui2_question_main_root, parent, false);
        /**
         * First item's entrance animations.
         */
        //mAnimator.onCreateViewHolder(itemView);

        return new GenericViewHolder(itemView);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int rawPosition) {
        if (holder instanceof GenericViewHolder) {
            CustomLog.v("showCameraView", "onBindViewHolder - " + rawPosition);
            CustomLog.v("showCameraView", "onBindViewHolder - " + mIndexMappingHashMap);
            GenericViewHolder genericViewHolder = (GenericViewHolder) holder;
            genericViewHolder.node = mItemList.get(genericViewHolder.getAbsoluteAdapterPosition());
            genericViewHolder.index = genericViewHolder.getAbsoluteAdapterPosition();
            genericViewHolder.rootIndex = mIndexMappingHashMap.getOrDefault(genericViewHolder.index, 0);


            genericViewHolder.tvQuestionCounter.setText("");
            String id = mItemList.get(genericViewHolder.index).getId();
            CustomLog.v(TAG, "ID - " + id);
            CustomLog.v(TAG, "mLoadedIds - " + mLoadedIds.contains(id)+ " \t Node findDisplay -  " + genericViewHolder.node.findDisplay());
            Handler handler = new Handler();
            if (!mLoadedIds.contains(id)) {

                genericViewHolder.spinKitView.setVisibility(View.VISIBLE);
                genericViewHolder.bodyLayout.setVisibility(View.GONE);

                handler.postDelayed(() -> {

                    genericViewHolder.spinKitView.setVisibility(View.GONE);
                    genericViewHolder.bodyLayout.setVisibility(View.VISIBLE);
                    //mRecyclerView.scrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1);
                    setData(genericViewHolder.index, genericViewHolder);
                }, 800);
            } else {
                handler.postDelayed(() -> {
                    genericViewHolder.spinKitView.setVisibility(View.GONE);
                    genericViewHolder.bodyLayout.setVisibility(View.VISIBLE);
                    setData(genericViewHolder.index, genericViewHolder);
                }, 100);
            }
            mLoadedIds.add(id);


        }
    }

    private void setData(int position, GenericViewHolder genericViewHolder) {
        CustomLog.v(TAG, "setData");
        genericViewHolder.otherContainerLinearLayout.removeAllViews();
        genericViewHolder.singleComponentContainer.removeAllViews();
        genericViewHolder.singleComponentContainer.setVisibility(View.GONE);
        genericViewHolder.recyclerView.setAdapter(null);
        genericViewHolder.recyclerView.setVisibility(View.GONE);
        genericViewHolder.nestedRecyclerView.setAdapter(null);
        genericViewHolder.nestedRecyclerView.setVisibility(View.GONE);

        //genericViewHolder.nextRelativeLayout.setVisibility(View.GONE);
        //genericViewHolder.superNestedContainerLinearLayout.setVisibility(View.GONE);


        if (mIsForPhysicalExam) {
            genericViewHolder.tvQuestionCounter.setText(String.format("%d %s %d %s", position + 1, mContext.getString(R.string.of), mPhysicalExam.getTotalNumberOfExams(), mContext.getString(R.string.questions))); //"1 of 10 questions"

        } else {
            genericViewHolder.tvQuestionCounter.setText(String.format("%d %s %d %s", getCount(genericViewHolder.index, mIndexMappingHashMap.get(genericViewHolder.index)), mContext.getString(R.string.of), mRootComplainBasicInfoHashMap.get(mIndexMappingHashMap.get(genericViewHolder.index)).getOptionSize(), mContext.getString(R.string.questions))); //"1 of 10 questions"

        }
        genericViewHolder.submitButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, genericViewHolder.node.isDataCaptured() ? R.drawable.ic_baseline_check_18_white : 0, 0);
        genericViewHolder.submitButton.setBackgroundResource(genericViewHolder.node.isDataCaptured() ? R.drawable.ui2_common_primary_bg : R.drawable.ui2_common_button_bg_submit);
        if (genericViewHolder.node.findPopup() != null && !genericViewHolder.node.findPopup().isEmpty()) {
            genericViewHolder.knowMoreTextView.setVisibility(View.VISIBLE);
        } else {
            genericViewHolder.knowMoreTextView.setVisibility(View.GONE);
        }

           /* if (genericViewHolder.node.isDataCaptured() && genericViewHolder.node.isDataCaptured()) {
                genericViewHolder.submitButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_24_white, 0, 0, 0);
            } else {
                genericViewHolder.submitButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }*/
        if (mIsForPhysicalExam) {

            Node _mNode = mPhysicalExam.getExamNode(position).getOption(0);
            final String parent_name = mPhysicalExam.getExamParentNodeName(position);
            String nodeText = (BuildConfig.FLAVOR_client == FlavorKeys.KCDO || BuildConfig.FLAVOR_client == FlavorKeys.UNFPA) ?
                    _mNode.findDisplay() :
                    parent_name + " : " + _mNode.findDisplay();

            genericViewHolder.tvQuestion.setText(nodeText);

            //hiding default text if input type is text
            if(_mNode.getInputType().equals("text")){
                genericViewHolder.tvQuestionDesc.setVisibility(View.GONE);
            }

            if (genericViewHolder.node.getJobAidFile() != null && !genericViewHolder.node.getJobAidFile().isEmpty()) {
                genericViewHolder.referenceContainerLinearLayout.setVisibility(View.VISIBLE);
                genericViewHolder.tvReferenceDesc.setVisibility(View.VISIBLE);
            } else {
                genericViewHolder.referenceContainerLinearLayout.setVisibility(View.GONE);
                genericViewHolder.tvReferenceDesc.setVisibility(View.GONE);
            }
            genericViewHolder.referenceContainerLinearLayout.removeAllViews();
            String[] imgs = genericViewHolder.node.getJobAidFile().split(",");
            for (int i = 0; i < imgs.length; i++) {
                View v2 = View.inflate(mContext, R.layout.ui2_ref_image_view, null);
                ImageView imageView = v2.findViewById(R.id.image);
                if (genericViewHolder.node.getJobAidFile() != null || !genericViewHolder.node.getJobAidFile().isEmpty()) {
                    String drawableName = "physicalExamAssets/" + genericViewHolder.node.getJobAidFile() + ".jpg";
                    try {
                        // get input stream
                        InputStream ims = mContext.getAssets().open(drawableName);
                        // load image as Drawable
                        Drawable d = Drawable.createFromStream(ims, null);
                        // set image to ImageView
                        imageView.setImageDrawable(d);
                        imageView.setMinimumHeight(150);
                        imageView.setMinimumWidth(300);
                        genericViewHolder.referenceContainerLinearLayout.addView(v2);
                    } catch (IOException ex) {
                        ex.printStackTrace();

                    }
                }
            }
        } else {
            genericViewHolder.tvQuestion.setText(genericViewHolder.node.findDisplay());

        }
        mOnItemSelection.needTitleChange(mContext.getString(R.string.visit_reason) + " : " + mRootComplainBasicInfoHashMap.get(mRootIndex).getComplainNameByLocale());

        if (genericViewHolder.node.getText().equalsIgnoreCase(Node.ASSOCIATE_SYMPTOMS)) {
            //mOnItemSelection.needTitleChange("2/4 Visit reason : Associated symptoms");
            showAssociateSymptoms(genericViewHolder.node, genericViewHolder, position);
            genericViewHolder.tvQuestionCounter.setText("");
        } else {
            //mOnItemSelection.needTitleChange("");


            String type = genericViewHolder.node.getInputType();
            CustomLog.v(TAG, "onBindViewHolder Type - " + type);
            //CustomLog.v(TAG, "onBindViewHolder Node - " + new Gson().toJson(genericViewHolder.node));
            if (type == null || type.isEmpty() && (genericViewHolder.node.getOptionsList() != null && !genericViewHolder.node.getOptionsList().isEmpty())) {
                type = "options";
            }
            switch (type) {
                case "text":
                    // askText(questionNode, context, adapter);
                    addTextEnterView(mItemList.get(position), genericViewHolder, position);
                    break;
                case "date":
                    //askDate(questionNode, context, adapter);
                    addDateView(mItemList.get(position), genericViewHolder, position);
                    break;
                case "location":
                    //askLocation(questionNode, context, adapter);
                    addTextEnterView(mItemList.get(position), genericViewHolder, position);
                    break;
                case "number":
                    // askNumber(questionNode, context, adapter);
                    addNumberView(mItemList.get(position), genericViewHolder, position);
                    break;
                case "area":
                    // askArea(questionNode, context, adapter);
                    addTextEnterView(mItemList.get(position), genericViewHolder, position);
                    break;
                case "duration":
                    // askDuration(questionNode, context, adapter);
                    addDurationView(mItemList.get(position), genericViewHolder, position);
                    break;
                case "range":
                    // askRange(questionNode, context, adapter);
                    addRangeView(mItemList.get(position), genericViewHolder, position);
                    break;
                case "frequency":
                    //askFrequency(questionNode, context, adapter);
                    addFrequencyView(mItemList.get(position), genericViewHolder, position);
                    break;
                case "camera":
                    // openCamera(context, imagePath, imageName);
                    CustomLog.v("showCameraView", "onBindViewHolder 2");
                    showCameraView(mItemList.get(position), genericViewHolder, position);
                    break;

                case "options":
                    // openCamera(context, imagePath, imageName);
                    //if (mIsForPhysicalExam)
                    //    showOptionsData(genericViewHolder, mPhysicalExam.getExamNode(position).getOption(0).getOptionsList(), position);
                    //else
                    showOptionsData(mItemList.get(position), genericViewHolder, mItemList.get(position).getOptionsList(), position, false, true);
                    break;
            }
        }
        if (!mItemList.get(position).getImagePathList().isEmpty()) {
            CustomLog.v(TAG, "found images");
            showCameraView(mItemList.get(position), genericViewHolder, position);
        }
    }

    private int getCount(int adapterPosition, int rootIndex) {
        int count = 0;
        //> mRootComplainBasicInfoHashMap.get(mRootIndex).getOptionSize() ? position - mRootComplainBasicInfoHashMap.get(mRootIndex).getOptionSize() - 1 : position + 1)
        if (rootIndex == 0) {
            count = adapterPosition + 1;
        } else {
            int completedCount = 0;
            for (int i = rootIndex - 1; i >= 0; i--) {
                completedCount += mRootComplainBasicInfoHashMap.get(i).getOptionSize();
            }
            count = adapterPosition + 1 - completedCount;
        }
        return count;
    }

    private void addRangeView(Node node, GenericViewHolder holder, int index) {
        holder.singleComponentContainer.removeAllViews();
        holder.singleComponentContainer.setVisibility(View.VISIBLE);
        View view = View.inflate(mContext, R.layout.ui2_visit_number_range, null);
        RangeSlider rangeSlider = view.findViewById(R.id.range_slider);
        //rangeSlider.setLabelBehavior(LABEL_ALWAYS_VISIBLE); //Label always visible" nothing yet ?
        TextView rangeTextView = view.findViewById(R.id.btn_values);
        Button submitButton = view.findViewById(R.id.btn_submit);
        submitButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, node.isDataCaptured() ? R.drawable.ic_baseline_check_18_white : 0, 0);
        submitButton.setBackgroundResource(node.isDataCaptured() ? R.drawable.ui2_common_primary_bg : R.drawable.ui2_common_button_bg_submit);

        Button skipButton = view.findViewById(R.id.btn_skip);

        if (!holder.node.isRequired()) skipButton.setVisibility(View.VISIBLE);
        else skipButton.setVisibility(View.GONE);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                node.setSelected(false);
                holder.node.setSelected(false);

                node.setDataCaptured(false);
                holder.node.setDataCaptured(false);
                mOnItemSelection.onSelect(node, index, true, null);
            }
        });

        if (node.isSelected() && node.getLanguage() != null && node.isDataCaptured()) {
            String[] vals = node.getLanguage().split("-");
            rangeTextView.setText(vals[0].trim() + " " + mContext.getString(R.string.to) + " " + vals[1].trim());
            List<Float> list = new ArrayList<>();
            list.add(Float.valueOf(vals[0]));
            list.add(Float.valueOf(vals[1]));
            rangeSlider.setValues(list);
        }
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rangeTextView.getText().toString().equalsIgnoreCase("---")) {
                    Toast.makeText(mContext, mContext.getString(R.string.please_select_range), Toast.LENGTH_SHORT).show();
                } else {
                    List<Float> values = rangeSlider.getValues();
                    int x = values.get(0).intValue();
                    int y = values.get(1).intValue();
                    String durationString = x + " - " + y;
                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", durationString));
                    } else {
                        node.addLanguage(" " + durationString);
                        node.setText(durationString);
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }

                    node.setSelected(true);
                    holder.node.setSelected(true);

                    node.setDataCaptured(true);
                    holder.node.setDataCaptured(true);


                    AdapterUtils.setToDisable(skipButton);
                    node.setSkipped(false);
                    AdapterUtils.buttonProgressAnimation(mContext, submitButton, true, new AdapterUtils.OnFinishActionListener() {
                        @Override
                        public void onFinish() {
                            notifyItemChanged(index);
                            mOnItemSelection.onSelect(node, index, false, null);
                        }
                    });
                }
            }
        });
        rangeSlider.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });
        rangeSlider.addOnSliderTouchListener(new RangeSlider.OnSliderTouchListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onStartTrackingTouch(@NonNull RangeSlider slider) {
                submitButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
                submitButton.setBackgroundResource(R.drawable.ui2_common_button_bg_submit);
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onStopTrackingTouch(@NonNull RangeSlider slider) {
                List<Float> values = rangeSlider.getValues();
                int x = values.get(0).intValue();
                int y = values.get(1).intValue();
                rangeTextView.setText(String.format(x + " " + mContext.getString(R.string.to) + " " + y));
                AdapterUtils.setToDefault(skipButton);
                AdapterUtils.setToDefault(submitButton);
            }
        });

        boolean isParentNodeIsMandatory = mItemList.get(index).isRequired();
        if (isParentNodeIsMandatory)
            skipButton.setVisibility(View.GONE);
        holder.singleComponentContainer.addView(view);
    }

    private void addFrequencyView(Node node, GenericViewHolder holder, int index) {
        holder.singleComponentContainer.removeAllViews();
        holder.singleComponentContainer.setVisibility(View.VISIBLE);
        final View view = View.inflate(mContext, R.layout.ui2_visit_number_slider_with_icon, null);
        Slider rangeSlider = view.findViewById(R.id.number_slider);
        //rangeSlider.setLabelBehavior(LABEL_ALWAYS_VISIBLE); //Label always visible" nothing yet ?
        TextView rangeTextView = view.findViewById(R.id.btn_values);
        TextView submitTextView = view.findViewById(R.id.btn_submit);

        Button skipButton = view.findViewById(R.id.btn_skip);
        if (!holder.node.isRequired()) skipButton.setVisibility(View.VISIBLE);
        else skipButton.setVisibility(View.GONE);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                node.setSelected(false);
                mOnItemSelection.onSelect(node, index, true, null);
            }
        });


        if (node.getLanguage() != null && !node.getLanguage().isEmpty() && !node.getLanguage().equalsIgnoreCase("%") && TextUtils.isDigitsOnly(node.getLanguage())) {
            int i = Integer.parseInt(node.getLanguage());
            rangeTextView.setText(mContext.getString(R.string.level) + " " + i);
            rangeSlider.setValue(i);
        }
        submitTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rangeTextView.getText().toString().equalsIgnoreCase("---")) {
                    Toast.makeText(mContext, mContext.getString(R.string.drag_to_select), Toast.LENGTH_SHORT).show();
                } else {
                    int x = (int) rangeSlider.getValue();
                    String durationString = String.valueOf(x);
                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", durationString));
                    } else {
                        node.addLanguage(" " + durationString);
                        node.setText(durationString);
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }
                    node.setSelected(true);
                    notifyItemChanged(index);
                    mOnItemSelection.onSelect(node, index, false, null);
                }
            }
        });
        rangeSlider.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });
        rangeSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {

            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                int x = (int) rangeSlider.getValue();
                rangeTextView.setText(mContext.getString(R.string.level) + " " + x);
                updateCustomEmojiSliderUI(view, x);
            }
        });

        updateCustomEmojiSliderUI(view, 0);
        boolean isParentNodeIsMandatory = mItemList.get(index).isRequired();
        if (isParentNodeIsMandatory)
            skipButton.setVisibility(View.GONE);
        holder.singleComponentContainer.addView(view);
    }

    private void updateCustomEmojiSliderUI(View view, int range) {
        TextView tv0 = view.findViewById(R.id.n0_tv);
        TextView tv1 = view.findViewById(R.id.n1_tv);
        TextView tv2 = view.findViewById(R.id.n2_tv);
        TextView tv3 = view.findViewById(R.id.n3_tv);
        TextView tv4 = view.findViewById(R.id.n4_tv);
        TextView tv5 = view.findViewById(R.id.n5_tv);
        TextView tv6 = view.findViewById(R.id.n6_tv);
        TextView tv7 = view.findViewById(R.id.n7_tv);
        TextView tv8 = view.findViewById(R.id.n8_tv);
        TextView tv9 = view.findViewById(R.id.n9_tv);
        TextView tv10 = view.findViewById(R.id.n10_tv);

        ImageView i0 = view.findViewById(R.id.n0_imv);
        ImageView i1 = view.findViewById(R.id.n1_imv);
        ImageView i2 = view.findViewById(R.id.n2_imv);
        ImageView i3 = view.findViewById(R.id.n3_imv);
        ImageView i4 = view.findViewById(R.id.n4_imv);
        ImageView i5 = view.findViewById(R.id.n5_imv);
        ImageView i6 = view.findViewById(R.id.n6_imv);
        ImageView i7 = view.findViewById(R.id.n7_imv);
        ImageView i8 = view.findViewById(R.id.n8_imv);
        ImageView i9 = view.findViewById(R.id.n9_imv);
        ImageView i10 = view.findViewById(R.id.n10_imv);

        // set default values
        tv0.setTextColor(ContextCompat.getColor(mContext,R.color.gray_3));
        tv0.setTextSize(14);
        tv0.setTypeface(tv0.getTypeface(), Typeface.NORMAL);

        tv1.setTextColor(ContextCompat.getColor(mContext,R.color.gray_3));
        tv1.setTextSize(14);
        tv1.setTypeface(tv1.getTypeface(), Typeface.NORMAL);

        tv2.setTextColor(ContextCompat.getColor(mContext,R.color.gray_3));
        tv2.setTextSize(14);
        tv2.setTypeface(tv2.getTypeface(), Typeface.NORMAL);

        tv3.setTextColor(ContextCompat.getColor(mContext,R.color.gray_3));
        tv3.setTextSize(14);
        tv3.setTypeface(tv3.getTypeface(), Typeface.NORMAL);

        tv4.setTextColor(ContextCompat.getColor(mContext,R.color.gray_3));
        tv4.setTextSize(14);
        tv4.setTypeface(tv4.getTypeface(), Typeface.NORMAL);

        tv5.setTextColor(ContextCompat.getColor(mContext,R.color.gray_3));
        tv5.setTextSize(14);
        tv5.setTypeface(tv5.getTypeface(), Typeface.NORMAL);

        tv6.setTextColor(ContextCompat.getColor(mContext,R.color.gray_3));
        tv6.setTextSize(14);
        tv6.setTypeface(tv6.getTypeface(), Typeface.NORMAL);

        tv7.setTextColor(ContextCompat.getColor(mContext,R.color.gray_3));
        tv7.setTextSize(14);
        tv7.setTypeface(tv7.getTypeface(), Typeface.NORMAL);

        tv8.setTextColor(ContextCompat.getColor(mContext,R.color.gray_3));
        tv8.setTextSize(14);
        tv8.setTypeface(tv8.getTypeface(), Typeface.NORMAL);

        tv9.setTextColor(ContextCompat.getColor(mContext,R.color.gray_3));
        tv9.setTextSize(14);
        tv9.setTypeface(tv9.getTypeface(), Typeface.NORMAL);

        tv10.setTextColor(ContextCompat.getColor(mContext,R.color.gray_3));
        tv10.setTextSize(14);
        tv10.setTypeface(tv10.getTypeface(), Typeface.NORMAL);

        i0.setColorFilter(ContextCompat.getColor(mContext, R.color.gray_3));
        i1.setColorFilter(ContextCompat.getColor(mContext, R.color.gray_3));
        i2.setColorFilter(ContextCompat.getColor(mContext, R.color.gray_3));
        i3.setColorFilter(ContextCompat.getColor(mContext, R.color.gray_3));
        i4.setColorFilter(ContextCompat.getColor(mContext, R.color.gray_3));
        i5.setColorFilter(ContextCompat.getColor(mContext, R.color.gray_3));
        i6.setColorFilter(ContextCompat.getColor(mContext, R.color.gray_3));
        i7.setColorFilter(ContextCompat.getColor(mContext, R.color.gray_3));
        i8.setColorFilter(ContextCompat.getColor(mContext, R.color.gray_3));
        i9.setColorFilter(ContextCompat.getColor(mContext, R.color.gray_3));
        i10.setColorFilter(ContextCompat.getColor(mContext, R.color.gray_3));

        if (range == 0) {
            tv0.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            tv0.setTextSize(16);
            tv0.setTypeface(tv0.getTypeface(), Typeface.BOLD);

            i0.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary));

        } else if (range == 1) {
            tv1.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            tv1.setTextSize(16);
            tv1.setTypeface(tv1.getTypeface(), Typeface.BOLD);

            i1.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary));
        } else if (range == 2) {
            tv2.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            tv2.setTextSize(16);
            tv2.setTypeface(tv2.getTypeface(), Typeface.BOLD);

            i2.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary));
        } else if (range == 3) {
            tv3.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            tv3.setTextSize(16);
            tv3.setTypeface(tv3.getTypeface(), Typeface.BOLD);

            i3.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary));
        } else if (range == 4) {
            tv4.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            tv4.setTextSize(16);
            tv4.setTypeface(tv4.getTypeface(), Typeface.BOLD);

            i4.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary));
        } else if (range == 5) {
            tv5.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            tv5.setTextSize(16);
            tv5.setTypeface(tv5.getTypeface(), Typeface.BOLD);

            i5.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary));
        } else if (range == 6) {
            tv6.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            tv6.setTextSize(16);
            tv6.setTypeface(tv6.getTypeface(), Typeface.BOLD);

            i6.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary));
        } else if (range == 7) {
            tv7.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            tv7.setTextSize(16);
            tv7.setTypeface(tv7.getTypeface(), Typeface.BOLD);

            i7.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary));
        } else if (range == 8) {
            tv8.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            tv8.setTextSize(16);
            tv8.setTypeface(tv8.getTypeface(), Typeface.BOLD);

            i8.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary));
        } else if (range == 9) {
            tv9.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            tv9.setTextSize(16);
            tv9.setTypeface(tv9.getTypeface(), Typeface.BOLD);

            i9.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary));
        } else if (range == 10) {
            tv10.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            tv10.setTextSize(16);
            tv10.setTypeface(tv10.getTypeface(), Typeface.BOLD);

            i10.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary));
        }
    }

    private void showAssociateSymptoms(Node node, GenericViewHolder holder, int position) {
        CustomLog.v(TAG, "showAssociateSymptoms()");
        holder.nestedRecyclerView.removeAllViews();
        holder.singleComponentContainer.removeAllViews();
        holder.singleComponentContainer.setVisibility(View.VISIBLE);
        holder.tvQuestionDesc.setVisibility(View.VISIBLE);
        holder.recyclerView.setVisibility(View.GONE);
        holder.nestedRecyclerView.setVisibility(View.GONE);
        holder.submitButton.setVisibility(View.GONE);
        holder.skipButton.setVisibility(View.GONE);
        holder.tvQuestionDesc.setText(mContext.getString(R.string.select_yes_or_no));

        View view = View.inflate(mContext, R.layout.associate_symptoms_questionar_main_view, null);
        Button submitButton = view.findViewById(R.id.btn_submit);
        submitButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, node.isDataCaptured() ? R.drawable.ic_baseline_check_18_white : 0, 0);
        submitButton.setBackgroundResource(node.isDataCaptured() ? R.drawable.ui2_common_primary_bg : R.drawable.ui2_common_button_bg_submit);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AnswerResult answerResult = mItemList.get(position).checkAllRequiredAnsweredRootNode(mContext);
                if (answerResult.result) {
                    node.setSkipped(false);
                    AdapterUtils.buttonProgressAnimation(mContext, submitButton, true, new AdapterUtils.OnFinishActionListener() {
                        @Override
                        public void onFinish() {
                            mItemList.get(position).setDataCaptured(true);
                            mOnItemSelection.onAllAnswered(true);

                        }
                    });
                } else {
                    DialogUtils dialogUtils = new DialogUtils();
                    dialogUtils.showCommonDialog(mContext, 0, mContext.getString(R.string.alert_label_txt), answerResult.requiredStrings, true, mContext.getResources().getString(R.string.generic_ok), mContext.getResources().getString(R.string.cancel), new DialogUtils.CustomDialogListener() {
                        @Override
                        public void onDialogActionDone(int action) {

                        }
                    });
                }

            }
        });
        RecyclerView recyclerView = view.findViewById(R.id.rcv_container);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        AssociateSymptomsQueryAdapter associateSymptomsQueryAdapter = new AssociateSymptomsQueryAdapter(mContext, mRecyclerView, recyclerView, node.getOptionsList(), mIsEditMode, new AssociateSymptomsQueryAdapter.AssociateSymptomsOnItemSelection() {
            @Override
            public void onSelect(Node data) {
                CustomLog.v(TAG, new Gson().toJson(data));
                mItemList.get(position).setSelected(false);
                mItemList.get(position).setDataCaptured(false);
                //VisitUtils.scrollNow(mRecyclerView, 1000, 0, 300, mIsEditMode);
                for (int i = 0; i < mItemList.get(position).getOptionsList().size(); i++) {
                    if (mItemList.get(position).getOptionsList().get(i).isSelected() || node.getOptionsList().get(i).isNoSelected()) {
                        //if (mIsEditMode) mItemList.get(position).setDataCaptured(true);
                        mItemList.get(position).setSelected(true);
                        CustomLog.v(TAG, "updated associate symptoms selected status");
                    }
                }
                AdapterUtils.setToDefault(submitButton);
                AdapterUtils.setToDefault(holder.submitButton);
                //VisitUtils.scrollNow(holder.recyclerView, 1000, 0, 200);
            }
        });
        recyclerView.setAdapter(associateSymptomsQueryAdapter);
/*

        if (node.isDataCaptured() && node.isDataCaptured()) {
            submitButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_24_white, 0, 0, 0);
        } else {
            submitButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
*/


        holder.singleComponentContainer.addView(view);
        //recyclerView.scrollToPosition(0);
    }

    private void showOptionsDataV2(final Node selectedNode, final GenericViewHolder holder, List<Node> options, int index, boolean isSuperNested, boolean isRootNodeQuestion) {
        //holder.nextRelativeLayout.setVisibility(View.GONE);
        holder.isParallelMultiNestedNode = false;
        boolean tag = false;
        if (holder.singleComponentContainer.getTag() != null) {
            tag = (boolean) holder.singleComponentContainer.getTag();
        }
        Timber.tag(TAG).d("TAG =>%s", tag);
        Timber.tag(TAG).d("Single size=>%s", holder.singleComponentContainer.getChildCount());
        if (!tag) holder.singleComponentContainer.removeAllViews();

        CustomLog.v(TAG, "showOptionsDataV2 selectedNode - " + new Gson().toJson(selectedNode));
        CustomLog.v(TAG, "showOptionsDataV2 options - " + options.size());
        CustomLog.v(TAG, "showOptionsDataV2 index - " + index);
        CustomLog.v(TAG, "showOptionsDataV2 isSuperNested - " + isSuperNested);
        if (!isSuperNested && options != null && options.size() == 1
                && options.get(0).getInputType() != null
                && !options.get(0).getInputType().isEmpty()
                && (options.get(0).getOptionsList() == null || options.get(0).getOptionsList().isEmpty())) {
            CustomLog.v(TAG, "showOptionsDataV2 single option");
            /*if (isSuperNested)
                holder.superNestedContainerLinearLayout.setVisibility(View.VISIBLE);
            else
                holder.superNestedContainerLinearLayout.setVisibility(View.GONE);*/
            holder.submitButton.setVisibility(View.GONE);
            holder.skipButton.setVisibility(View.GONE);
            // it seems that inside the options only one view and its simple component like text,date, number, area, duration, range, frequency, camera, etc
            // we we have add same in linear layout dynamically instead of adding in to recyclerView
            holder.singleComponentContainer.setVisibility(View.VISIBLE);
            if (isRootNodeQuestion) holder.tvQuestionDesc.setVisibility(View.GONE);
            Node node = options.get(0);
            String type = node.getInputType() == null ? "" : node.getInputType();

            if (node.getOptionsList() != null && !node.getOptionsList().isEmpty()) {
                type = "options";
            }
            CustomLog.v(TAG, "Type - " + type);
            switch (type) {
                case "text":
                    // askText(questionNode, context, adapter);
                    holder.singleComponentContainer.setTag(selectedNode.isSelected());
                    addTextEnterView(options.get(0), holder, index);
                    break;
                case "date":
                    node.setShowCalendarHeader(true);
                    //askDate(questionNode, context, adapter);
                    if (node.getText().contains("Rescheduled")) {
                        addFutureDateView(node, holder, index);
                    } else {
                        addDateView(node, holder, index);
                    }
                    break;
                case "location":
                    //askLocation(questionNode, context, adapter);
                    addTextEnterView(options.get(0), holder, index);
                    break;
                case "number":
                    // askNumber(questionNode, context, adapter);
                    addNumberView(options.get(0), holder, index);
                    break;
                case "area":
                    // askArea(questionNode, context, adapter);
                    addTextEnterView(options.get(0), holder, index);
                    break;
                case "duration":
                    // askDuration(questionNode, context, adapter);
                    addDurationView(options.get(0), holder, index);
                    break;
                case "range":
                    // askRange(questionNode, context, adapter);
                    addRangeView(options.get(0), holder, index);
                    break;
                case "frequency":
                    //askFrequency(questionNode, context, adapter);
                    addFrequencyView(options.get(0), holder, index);
                    break;
                case "camera":
                    // openCamera(context, imagePath, imageName);
                    CustomLog.v("showCameraView", "showOptionsData 1");
                    showCameraView(options.get(0), holder, index);
                    break;

                case "options":
                    // openCamera(context, imagePath, imageName);
                    //showOptionsData(genericViewHolder, genericViewHolder.node.getOptionsList());
                    break;
                default:
                    holder.submitButton.setVisibility(View.VISIBLE);
                    break;
            }

        } else {

            holder.tvQuestionDesc.setVisibility(View.VISIBLE);
            holder.recyclerView.setVisibility(View.VISIBLE);


            if (mItemList.get(index).isMultiChoice()) {
                holder.tvQuestionDesc.setText(mContext.getString(R.string.select_one_or_more));
                holder.submitButton.setVisibility(View.VISIBLE);
                holder.submitButton.setBackgroundResource(selectedNode.isDataCaptured() ? R.drawable.ui2_common_primary_bg : R.drawable.ui2_common_button_bg_submit);
                if (mItemList.get(index).isDataCaptured()) {
                    AdapterUtils.setToDisable(holder.skipButton);
                } else {
                    AdapterUtils.setToDefault(holder.skipButton);
                }
            } else {
                holder.tvQuestionDesc.setText(mContext.getString(R.string.select_any_one));
                holder.submitButton.setVisibility(View.GONE);
                if (mItemList.get(index).isDataCaptured()) {
                    AdapterUtils.setToDisable(holder.skipButton);
                } else {
                    AdapterUtils.setToDefault(holder.skipButton);
                }
            }


            if (mItemList.get(index).isRequired()) {
                holder.skipButton.setVisibility(View.GONE);
            } else {
                holder.skipButton.setVisibility(View.VISIBLE);
            }
            //if (isSuperNested) {
            boolean havingNestedQuestion = selectedNode.isHavingNestedQuestion();
            CustomLog.v(TAG, "showOptionsDataV2 havingNestedQuestion - " + havingNestedQuestion);

                /*//holder.superNestedContainerLinearLayout.removeAllViews();
                View v1 = View.inflate(mContext, R.layout.nested_recycle_view, null);
                v1.setTag(selectedNode.getId());
                for (int i = 0; i < holder.superNestedContainerLinearLayout.getChildCount(); i++) {
                    if (String.valueOf(holder.superNestedContainerLinearLayout.getChildAt(i).getTag()).equalsIgnoreCase(selectedNode.getId())) {
                        return;
                    }
                }

                RecyclerView recyclerView = v1.findViewById(R.id.rcv_nested_container);
                TextView questionTextView = v1.findViewById(R.id.tv_question);
                LinearLayout singleComponentContainerLinearLayout = v1.findViewById(R.id.ll_single_component_container);
                LinearLayout othersContainerLinearLayout = v1.findViewById(R.id.ll_others_container);
                FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(mContext);
                layoutManager.setFlexDirection(FlexDirection.ROW);
                layoutManager.setJustifyContent(JustifyContent.FLEX_START);
                recyclerView.setLayoutManager(layoutManager);
                TextView tvQuestionDesc = v1.findViewById(R.id.tv_question_desc);

                if (selectedNode.isMultiChoice()) {
                    tvQuestionDesc.setText(mContext.getString(R.string.select_one_or_more));
                    holder.submitButton.setVisibility(View.VISIBLE);
                } else {
                    tvQuestionDesc.setText(mContext.getString(R.string.select_any_one));
                    holder.submitButton.setVisibility(View.GONE);

                }*/

                /*if (mItemList.get(index).isRequired()) {
                    skipButton.setVisibility(View.GONE);
                } else {
                    skipButton.setVisibility(View.VISIBLE);
                }*/
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
            linearLayoutManager.setStackFromEnd(false);
            linearLayoutManager.setSmoothScrollbarEnabled(true);
            holder.nestedRecyclerView.setLayoutManager(linearLayoutManager);


            //if (holder.nestedRecyclerView.getAdapter() != null && mItemList.get(index).isMultiChoice()) {
            //   nestedQuestionsListingAdapter = (NestedQuestionsListingAdapter) holder.nestedRecyclerView.getAdapter();
            //}else {

            if (holder.nestedQuestionsListingAdapter != null) {

            }
            holder.nestedQuestionsListingAdapter = new NestedQuestionsListingAdapter(mContext, mRecyclerView, holder.nestedRecyclerView,
                    selectedNode, 0, index, mIsEditMode,
                    mItemList.get(Math.max(holder.getAbsoluteAdapterPosition(), 0)).isRequired(), new OnItemSelection() {
                @Override
                public void onSelect(Node node, int index, boolean isSkipped, Node parentNode) {
                    CustomLog.v(TAG, "NestedQuestionsListingAdapter onSelect index- " + index + " isSkipped = " + isSkipped);
                    CustomLog.v(TAG, "NestedQuestionsListingAdapter onSelect selectedNode - " + selectedNode.findDisplay());
                    CustomLog.v(TAG, "NestedQuestionsListingAdapter onSelect selectedNode - " + selectedNode.getId());
                    CustomLog.v(TAG, "NestedQuestionsListingAdapter onSelect node - " + node.findDisplay());
                    CustomLog.v(TAG, "NestedQuestionsListingAdapter onSelect options.size() - " + options.size());
                    if (!node.isSelected()) return;
                    holder.isParallelMultiNestedNode = false;
                    if (selectedNode != null) {
                        holder.isParallelMultiNestedNode = selectedNode.isHavingMoreNestedQuestion();
                        //holder.nextRelativeLayout.setVisibility(View.VISIBLE);
                        //holder.nextRelativeLayout.setVisibility(View.GONE);
                    }
                    VisitUtils.scrollNow(mRecyclerView, 1400, 0, 400, mIsEditMode, mLoadedIds.contains(mItemList.get(index).getId()));
                    ((LinearLayoutManager) Objects.requireNonNull(mRecyclerView.getLayoutManager())).setStackFromEnd(false);
                    boolean isLastNodeSubmit = holder.selectedNestedOptionIndex >= options.size() - 1;
                    if (isSkipped) {
                        if (options.size() == 1) {
                            CustomLog.v(TAG, "NestedQuestionsListingAdapter onSelect isSkipped && options.size() == 1 ");
                            mItemList.get(index).setSelected(false);
                            mItemList.get(index).setDataCaptured(false);
                            selectedNode.setSelected(false);
                            selectedNode.setDataCaptured(false);
                            selectedNode.unselectAllNestedNode();
                            notifyItemChanged(index);
                            if (selectedNode.isRequired()) return;
                        } else {
                            if (isLastNodeSubmit)
                                holder.selectedNestedOptionIndex = holder.selectedNestedOptionIndex - 1;
                        }

                    }

                    CustomLog.v(TAG, "NestedQuestionsListingAdapter onSelect mOnItemSelection.onSelect holder.selectedNestedOptionIndex - " + holder.selectedNestedOptionIndex);
                    CustomLog.v(TAG, "NestedQuestionsListingAdapter onSelect selectedNode.isContainsTheQuestionBeforeOptions() - " + selectedNode.isContainsTheQuestionBeforeOptions());
                    CustomLog.v(TAG, "NestedQuestionsListingAdapter onSelect holder.isParallelMultiNestedNode - " + holder.isParallelMultiNestedNode);
                    CustomLog.v(TAG, "NestedQuestionsListingAdapter onSelect isLastNodeSubmit - " + isLastNodeSubmit);

                    if (holder.isParallelMultiNestedNode && !isLastNodeSubmit) {
                        holder.selectedNestedOptionIndex += 1;
                        holder.nestedQuestionsListingAdapter.addItem(TAG, options.get(holder.selectedNestedOptionIndex));

                    } else if (!selectedNode.isContainsTheQuestionBeforeOptions()) {
                        mOnItemSelection.onSelect(node, index, isSkipped, selectedNode);
                    } else {
                        if (!holder.isParallelMultiNestedNode || isLastNodeSubmit)
                            mOnItemSelection.onSelect(node, index, isSkipped, selectedNode);
                        else {
                            holder.selectedNestedOptionIndex += 1;
                            holder.nestedQuestionsListingAdapter.addItem(TAG, options.get(holder.selectedNestedOptionIndex));
                        }
                    }
                    //VisitUtils.scrollNow(holder.nestedRecyclerView, 1000, 0, 300);
                }

                @Override
                public void needTitleChange(String title) {

                }

                @Override
                public void onAllAnswered(boolean isAllAnswered) {

                }

                @Override
                public void onCameraRequest() {

                }

                @Override
                public void onImageRemoved(int nodeIndex, int imageIndex, String image) {

                }

                @Override
                public void onTerminalNodeAnsweredForParentUpdate(String parentNodeId) {
                    selectedNode.setDataCaptured(true);
                }
            });
            holder.nestedQuestionsListingAdapter.setLoadedIds(mLoadedIds);
            holder.nestedRecyclerView.setAdapter(holder.nestedQuestionsListingAdapter);
            // }
            holder.nestedQuestionsListingAdapter.setSuperNodeList(mItemList);
            boolean isSingleNodeWithNestedQA = selectedNode.isSingleNodeWithNestedQA();
            //if (havingNestedQuestion) {
            if (!havingNestedQuestion) {
                havingNestedQuestion = selectedNode.isHavingMoreNestedQuestion();
            }
            CustomLog.v(TAG, "havingNestedQuestion - " + havingNestedQuestion);
            CustomLog.v(TAG, "isRootNodeQuestion - " + isRootNodeQuestion);
            CustomLog.v(TAG, "Node.CHILD_QUESTION == selectedNode.foundTheNestedQuestionType() - " + (Node.CHILD_QUESTION == selectedNode.foundTheNestedQuestionType()));
            CustomLog.v(TAG, "selectedNode.isContainsTheQuestionBeforeOptions() - " + (selectedNode.isContainsTheQuestionBeforeOptions()));
            if (!isRootNodeQuestion && (havingNestedQuestion || Node.CHILD_QUESTION == selectedNode.foundTheNestedQuestionType())) {
                holder.nestedQuestionsListingAdapter.setEngineVersion(getEngineVersion());
                //questionTextView.setText(options.get(0).findDisplay());
                holder.nestedQuestionsListingAdapter.clearItems();

                if (havingNestedQuestion || selectedNode.isContainsTheQuestionBeforeOptions()) {
                    CustomLog.v(TAG, "options - " + (new Gson().toJson(options)));

                    if (mIsEditMode) {
                        for (int i = 0; i < options.size(); i++) {
                            holder.nestedQuestionsListingAdapter.addItem(TAG, options.get(i));
                        }
                    } else if (holder.selectedNestedOptionIndex > 0) {
                        holder.selectedNestedOptionIndex = 0;
                        CustomLog.v(TAG, "holder.selectedNestedOptionIndex 1 - " + (holder.selectedNestedOptionIndex));
                        holder.nestedQuestionsListingAdapter.addItem(TAG, options.get(holder.selectedNestedOptionIndex));
                       /* for (int i = 0; i <= holder.selectedNestedOptionIndex; i++) {
                            if (options.size() < i) {
                                holder.nestedQuestionsListingAdapter.addItem(options.get(i));
                            }
                        }*/
                    } else {
                        holder.nestedQuestionsListingAdapter.addItem(TAG, options.get(holder.selectedNestedOptionIndex));
                    }
                } else {
                    holder.nestedQuestionsListingAdapter.addItem(TAG, selectedNode);
                }
                holder.isParallelMultiNestedNode = options.size() > 1;

                /*if (holder.isParallelMultiNestedNode) {
                    //holder.nextRelativeLayout.setVisibility(View.VISIBLE);
                    holder.nextRelativeLayout.setVisibility(View.GONE);
                } else {
                    holder.nextRelativeLayout.setVisibility(View.GONE);
                }*/
                holder.nestedRecyclerView.setVisibility(View.VISIBLE);
                holder.submitButton.setVisibility(View.GONE);
                holder.skipButton.setVisibility(View.GONE);
                VisitUtils.scrollNow(mRecyclerView, 1000, 0, 600, mIsEditMode, mLoadedIds.contains(mItemList.get(index).getId()));
            } /*else if (isSuperNested) {
                nestedQuestionsListingAdapter.addItem(selectedNode);
                holder.nestedRecyclerView.setVisibility(View.VISIBLE);
            }*/ else {
                //holder.nextRelativeLayout.setVisibility(View.GONE);
                CustomLog.v(TAG, "showOptionsDataV2 multiple option");
                holder.tvQuestionDesc.setVisibility(View.VISIBLE);
                holder.recyclerView.setVisibility(View.VISIBLE);
                holder.nestedRecyclerView.setVisibility(View.GONE);
                //holder.superNestedContainerLinearLayout.setVisibility(View.GONE);
                if (mItemList.get(index).isMultiChoice()) {
                    holder.tvQuestionDesc.setText(mContext.getString(R.string.select_one_or_more));
                    holder.submitButton.setVisibility(View.VISIBLE);
                    holder.submitButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, selectedNode.isDataCaptured() ? R.drawable.ic_baseline_check_18_white : 0, 0);
                    holder.submitButton.setBackgroundResource(selectedNode.isDataCaptured() ? R.drawable.ui2_common_primary_bg : R.drawable.ui2_common_button_bg_submit);
                    if (mItemList.get(index).isDataCaptured()) {
                        AdapterUtils.setToDisable(holder.skipButton);
                    } else {
                        AdapterUtils.setToDefault(holder.skipButton);
                    }
                } else {
                    holder.tvQuestionDesc.setText(mContext.getString(R.string.select_any_one));
                    holder.submitButton.setVisibility(View.GONE);
                    if (mItemList.get(index).isDataCaptured()) {
                        AdapterUtils.setToDisable(holder.skipButton);
                    } else {
                        AdapterUtils.setToDefault(holder.skipButton);
                    }
                }

                if (selectedNode.isSkipped()) {
                    holder.skipButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_check_18_white, 0);
                    holder.skipButton.setBackgroundResource(R.drawable.ui2_common_primary_bg);
                    AdapterUtils.setToDisable(holder.submitButton);
                }

                if (mItemList.get(index).isRequired()) {
                    holder.skipButton.setVisibility(View.GONE);
                } else {
                    holder.skipButton.setVisibility(View.VISIBLE);
                }
                //holder.recyclerView.setLayoutManager(new GridLayoutManager(mContext, options.size() == 1 ? 1 : 2));
                FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(mContext);
                layoutManager.setFlexDirection(FlexDirection.ROW);
                layoutManager.setJustifyContent(JustifyContent.FLEX_START);
                holder.recyclerView.setLayoutManager(layoutManager);

                //**********
                // Avoid the duplicate options asking to user in connected questions
                //**************
                String duplicateCheckNodeNames = mItemList.get(index).getCompareDuplicateNode();
                CustomLog.v(TAG, "duplicateCheckNodeNames - " + duplicateCheckNodeNames);
                if (duplicateCheckNodeNames != null && !duplicateCheckNodeNames.isEmpty()) {
                    int sourceIndex = 0;
                    Node toCompareWithNode = null;
                    for (int i = 0; i < mItemList.size(); i++) {
                        if (mItemList.get(i).getText().equalsIgnoreCase(duplicateCheckNodeNames)) {
                            toCompareWithNode = mItemList.get(i);
                            CustomLog.v(TAG, "toCompareWithNode - " + new Gson().toJson(toCompareWithNode));
                            break;
                        }
                    }
                    NodeAdapterUtils.updateForHideShowFlag(mContext, mItemList.get(index), toCompareWithNode);
                }
                VisitUtils.scrollNow(mRecyclerView, 1400, 0, 600, mIsEditMode, mLoadedIds.contains(mItemList.get(index).getId()));
                // *****************
                for (int i = 0; i < options.size(); i++) {
                    options.get(i).setNestedLeve(options.get(i).getNestedLeve() + 1);
                }

                OptionsChipsGridAdapter optionsChipsGridAdapter = new OptionsChipsGridAdapter(
                        holder.recyclerView, mContext,
                        mItemList.get(index), options, new OptionsChipsGridAdapter.OnItemSelection() {
                    @Override
                    public void onSelect(Node node, boolean isLoadingForNestedEditData) {

                        if (!isLoadingForNestedEditData)
                            VisitUtils.scrollNow(mRecyclerView, 1000, 0, 300, mIsEditMode, mLoadedIds.contains(mItemList.get(index).getId()));
                        ((LinearLayoutManager) Objects.requireNonNull(mRecyclerView.getLayoutManager())).setStackFromEnd(false);
                        if (!isLoadingForNestedEditData) {
                            mItemList.get(index).setSelected(false);
                            mItemList.get(index).setDataCaptured(false);
                            mItemList.get(index).setSkipped(false);
                            AdapterUtils.setToDefault(holder.submitButton);
                            AdapterUtils.setToDefault(holder.skipButton);
                        }

                        if (node.getInputType().equalsIgnoreCase("text"))
                            holder.singleComponentContainer.setTag(node.isSelected());
                        //holder.submitButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, selectedNode.isDataCaptured() ? R.drawable.ic_baseline_check_18_white : 0, 0);
                        //holder.submitButton.setBackgroundResource(selectedNode.isDataCaptured() ? R.drawable.ui2_common_primary_bg : R.drawable.ui2_common_button_bg_submit);

                        boolean isRequiredToShowParentActionButtons = false;
                        for (int i = 0; i < options.size(); i++) {

                            if (options.get(i).isSelected()) {
                                mItemList.get(index).setSelected(true);
                                if (!options.get(i).isTerminal()) {

                                    //mItemList.get(index).setDataCaptured(true);
                                    //break;
                                    if (!isRequiredToShowParentActionButtons)
                                        isRequiredToShowParentActionButtons = !isAnySubChildOpenedWithAction(options.get(i));
                                }
                            }
                        }
                        //Toast.makeText(mContext, "Selected : " + data, Toast.LENGTH_SHORT).show();
                        String type = node.getInputType();

                        if (type == null || type.isEmpty() && (node.getOptionsList() != null && !node.getOptionsList().isEmpty())) {
                            type = "options";
                        }
                        CustomLog.v(TAG, "optionsChipsGridAdapter - Type - " + type);
                        CustomLog.v(TAG, "optionsChipsGridAdapter - isLoadingForNestedEditData - " + isLoadingForNestedEditData);
                        CustomLog.v(TAG, "optionsChipsGridAdapter - Node - " + node.findDisplay() + " isSelected - " + node.isSelected() + " isExcludedFromMultiChoice - " + node.isExcludedFromMultiChoice());

                        boolean foundUserInputs = false;
                        for (int i = 0; i < options.size(); i++) {
                            if (options.get(i).isSelected()) {
                                foundUserInputs = options.get(i).isUserInputsTypeNode();
                                if (foundUserInputs)
                                    break;
                            }
                        }
                        CustomLog.v(TAG, "foundUserInputs - " + foundUserInputs);

                        if (!node.isSelected()) {
                            node.unselectAllNestedNode();
                            if (type.equalsIgnoreCase("camera"))
                                mItemList.get(index).removeImagesAllNestedNode();
                            if (!isLoadingForNestedEditData)
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {


                                        notifyItemChanged(index);

                                    }
                                }, 100);
                        } else if (type.isEmpty() && node.isSelected()) {

                            if (!foundUserInputs) {
                                holder.singleComponentContainer.removeAllViews();
                                holder.nestedRecyclerView.setAdapter(null);
                            }
                            holder.singleComponentContainer.setVisibility(View.VISIBLE);
                            if (!foundUserInputs) {

                                boolean isNothingNestedOpen = false;
                                for (int i = 0; i < options.size(); i++) {
                                    if (options.get(i).isSelected())
                                        isNothingNestedOpen = options.get(i).isTerminal();

                                }
                                CustomLog.v(TAG, "isNothingNestedOpen - " + isNothingNestedOpen);
                                CustomLog.v(TAG, "isRequiredToShowParentActionButtons - " + isRequiredToShowParentActionButtons);

                                if (mItemList.get(index).isMultiChoice()) {
                                    if (isNothingNestedOpen || isRequiredToShowParentActionButtons) {
                                        if (!mItemList.get(index).isEnableExclusiveOption()) {
                                            holder.submitButton.setVisibility(View.VISIBLE);
                                            if (mItemList.get(index).isRequired()) {
                                                holder.skipButton.setVisibility(View.GONE);
                                            } else {
                                                holder.skipButton.setVisibility(View.VISIBLE);

                                            }
                                        }
                                    }
                                } else {
                                    holder.submitButton.setVisibility(View.GONE);

                                   /* new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (!isLoadingForNestedEditData) {

                                                notifyItemChanged(index);
                                            }
                                        }
                                    }, 100);*/
                                    if (!isLoadingForNestedEditData) {
                                        mOnItemSelection.onSelect(node, index, false, selectedNode);
                                        AdapterUtils.setToDisable(holder.skipButton);
                                    }

                                }
                            }
                        } else if (!type.isEmpty() && node.isSelected()) {
                            Timber.tag(TAG).d("mItemList.get(index)::child=>%s", mItemList.get(index).findDisplay());

                            if (!mItemList.get(index).isMultiChoice()) {
                                for (int i = 0; i < mItemList.get(index).getOptionsList().size(); i++) {
                                    if (!mItemList.get(index).getOptionsList().get(i).getText().equals(node.getText())) {
                                        mItemList.get(index).getOptionsList().get(i).unselectAllNestedNode();
                                    }
                                }
                            }

                            if (!foundUserInputs) {
                                holder.singleComponentContainer.removeAllViews();
                            }
                            holder.singleComponentContainer.setVisibility(View.VISIBLE);
                            if (!mItemList.get(index).isMultiChoice() && !mItemList.get(index).isEnableExclusiveOption()) {
                                holder.nestedRecyclerView.setAdapter(null); /** Note: Sr.No.29 - Fix: This code should not trigger in-case of phys exam take picture so use is-exclusive logic. */
                            }
                            Timber.tag(TAG).d("singleComponentContainer::child=>%s", holder.singleComponentContainer.getChildCount());
                        } else {
                            holder.singleComponentContainer.removeAllViews();
                            //holder.superNestedContainerLinearLayout.removeAllViews();
                            boolean isAnyOtherOptionSelected = false;
                            for (int i = 0; i < options.size(); i++) {
                                if (options.get(i).isSelected()) {
                                    isAnyOtherOptionSelected = true;
                                    break;
                                }
                            }

                            if (isLoadingForNestedEditData) {
                                if (selectedNode.isDataCaptured()) {
                                    AdapterUtils.setToDisable(holder.skipButton);
                                    AdapterUtils.setToDisable(holder.submitButton);
                                } else {
                                    AdapterUtils.setToDefault(holder.skipButton);
                                    AdapterUtils.setToDefault(holder.submitButton);
                                }
                            } else {
                                AdapterUtils.setToDefault(holder.submitButton);
                                AdapterUtils.setToDefault(holder.skipButton);

                            }
                            if (mIsFromAssociatedSymptoms) {
                                CustomLog.v(TAG, "optionsChipsGridAdapter - mItemList.get(index) - " + new Gson().toJson(mItemList.get(index)));
                                CustomLog.v(TAG, "optionsChipsGridAdapter - index - " + index);
                            }
                            /*AdapterUtils.setToDefault(holder.submitButton);
                            AdapterUtils.setToDefault(holder.skipButton);*/
                            /*holder.submitButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0,  0, 0);
                            holder.submitButton.setBackgroundResource(R.drawable.ui2_common_button_bg_submit);*/

                            if (mItemList.get(index).isMultiChoice()) {
                                holder.tvQuestionDesc.setText(mContext.getString(R.string.select_one_or_more));
                                if (!isAnyOtherOptionSelected || isRequiredToShowParentActionButtons)
                                    if (!mItemList.get(index).isEnableExclusiveOption())
                                        holder.submitButton.setVisibility(View.VISIBLE);

                                if (node.isExcludedFromMultiChoice()) {
                                    /*for (int i = 0; i < options.size(); i++) {
                                        if(!options.get(i).getText().equals(node.getText())){
                                            options.get(i).unselectAllNestedNode();
                                        }
                                    }*/
                                    mItemList.get(index).removeImagesAllNestedNode();
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (!isLoadingForNestedEditData) {

                                                notifyItemChanged(index);
                                            }
                                        }
                                    }, 100);
                                }
                            } else {
                                holder.tvQuestionDesc.setText(mContext.getString(R.string.select_any_one));
                                holder.submitButton.setVisibility(View.GONE);

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!isLoadingForNestedEditData) {

                                            notifyItemChanged(index);
                                        }
                                    }
                                }, 100);
                                if (!isLoadingForNestedEditData) {
                                    mOnItemSelection.onSelect(node, index, false, selectedNode);
                                    AdapterUtils.setToDisable(holder.skipButton);
                                }
                            }

                            if (mItemList.get(index).isRequired()) {
                                holder.skipButton.setVisibility(View.GONE);
                            } else {
                                if (!isAnyOtherOptionSelected || isRequiredToShowParentActionButtons)
                                    holder.skipButton.setVisibility(View.VISIBLE);
                            }

                            if (node.isExcludedFromMultiChoice()) {
                                holder.nestedRecyclerView.setVisibility(View.GONE);
                                if (!isLoadingForNestedEditData) {
                                    notifyItemChanged(index);
                                    VisitUtils.scrollNow(mRecyclerView, 1400, 0, 1000, mIsEditMode, mLoadedIds.contains(mItemList.get(index).getId()));
                                }
                            }
                            return;
                        }


                        switch (type) {
                            case "text":
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                holder.singleComponentContainer.setTag(node.isSelected());
                                // askText(questionNode, context, adapter);
                                addTextEnterView(node, holder, index);
                                break;
                            case "date":
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                //askDate(questionNode, context, adapter);
                                addDateView(node, holder, index);
                                break;
                            case "location":
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                // askText(questionNode, context, adapter);
                                addTextEnterView(node, holder, index);
                                break;
                            case "number":
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                // askNumber(questionNode, context, adapter);
                                addNumberView(node, holder, index);
                                break;
                            case "area":
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                // askText(questionNode, context, adapter);
                                addTextEnterView(node, holder, index);
                                break;
                            case "duration":
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                // askDuration(questionNode, context, adapter);
                                addDurationView(node, holder, index);
                                break;
                            case "range":
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                // askRange(questionNode, context, adapter);
                                addRangeView(node, holder, index);
                                break;
                            case "frequency":
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                //askFrequency(questionNode, context, adapter);
                                addFrequencyView(node, holder, index);
                                break;
                            case "camera":
//                                if (!mItemList.get(index).isMultiChoice())
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                // openCamera(context, imagePath, imageName);
                                CustomLog.v("showCameraView", "showOptionsData 2");
                                showCameraView(node, holder, index);
                                break;

                            case "options":
                                // openCamera(context, imagePath, imageName);
                                //holder.superNestedContainerLinearLayout.removeAllViews();
//                                if (node.getOptionsList() != null && node.isRequired() && node.getOptionsList().size() > 0) {
//                                    Timber.tag(TAG).d("node => %s", node.findDisplay());
//                                    for (Node option : node.getOptionsList()) {
//                                        Timber.tag(TAG).d("option => %s", option.findDisplay());
//                                        Timber.tag(TAG).d("option data captured => %s", option.isDataCaptured());
//                                        if (option.isDataCaptured()) {
//                                            node.setDataCaptured(true);
//                                            break;
//                                        } else node.setDataCaptured(false);
//                                    }
//                                }
                                showOptionsData(node, holder, node.getOptionsList(), index, node.getOptionsList().size() > 1, false);
                                break;
                        }
                        //notifyDataSetChanged();
                    }
                });
                holder.recyclerView.setAdapter(optionsChipsGridAdapter);


            }
            /*for (int i = 0; i < options.size(); i++) {
                String type = options.get(i).getInputType();
                if (type.equalsIgnoreCase("camera") && options.get(i).isSelected()) {
                    // openCamera(context, imagePath, imageName);
                    Log.v("showCameraView", "showOptionsData - " + new Gson().toJson(options.get(i).getImagePathList()));
                    showCameraView(options.get(i), holder, index);
                }
            }*/
        }

    }

    private boolean isAnySubChildOpenedWithAction(Node node) {
        String type = node.getInputType() == null ? "" : node.getInputType();

        if (type.isEmpty() && (node.getOptionsList() != null && !node.getOptionsList().isEmpty())) {
            type = "options";
        }
        if (type.isEmpty()) {
            if (node.isSelected()) {
                return node.getOptionsList() != null && node.getOptionsList().size() != 0;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    private void showOptionsData(final Node selectedNode, final GenericViewHolder holder, List<Node> options, int index, boolean isSuperNested, boolean isRootNodeQuestion) {
        if (getEngineVersion().equalsIgnoreCase("3.0")) {
            showOptionsDataV2(selectedNode, holder, options, index, isSuperNested, isRootNodeQuestion);
            return;
        }
        //holder.nextRelativeLayout.setVisibility(View.GONE);
        holder.isParallelMultiNestedNode = false;
        holder.singleComponentContainer.removeAllViews();
        CustomLog.v(TAG, "showOptionsData selectedNode - " + new Gson().toJson(selectedNode));
        CustomLog.v(TAG, "showOptionsData options - " + options.size());
        CustomLog.v(TAG, "showOptionsData index - " + index);
        CustomLog.v(TAG, "showOptionsData isSuperNested - " + isSuperNested);
        if (!isSuperNested && options != null && options.size() == 1 && (options.get(0).getOptionsList() == null || options.get(0).getOptionsList().isEmpty())) {
            CustomLog.v(TAG, "showOptionsData single option");
            /*if (isSuperNested)
                holder.superNestedContainerLinearLayout.setVisibility(View.VISIBLE);
            else
                holder.superNestedContainerLinearLayout.setVisibility(View.GONE);*/
            holder.submitButton.setVisibility(View.GONE);
            holder.skipButton.setVisibility(View.GONE);
            // it seems that inside the options only one view and its simple component like text,date, number, area, duration, range, frequency, camera, etc
            // we we have add same in linear layout dynamically instead of adding in to recyclerView
            holder.singleComponentContainer.setVisibility(View.VISIBLE);
            holder.tvQuestionDesc.setVisibility(View.GONE);
            Node node = options.get(0);
            String type = node.getInputType() == null ? "" : node.getInputType();

            if (node.getOptionsList() != null && !node.getOptionsList().isEmpty()) {
                type = "options";
            }
            CustomLog.v(TAG, "Type - " + type);
            switch (type) {
                case "text":
                    // askText(questionNode, context, adapter);
                    addTextEnterView(options.get(0), holder, index);
                    break;
                case "date":
                    //askDate(questionNode, context, adapter);
                    addDateView(options.get(0), holder, index);
                    break;
                case "location":
                    //askLocation(questionNode, context, adapter);
                    addTextEnterView(options.get(0), holder, index);
                    break;
                case "number":
                    // askNumber(questionNode, context, adapter);
                    addNumberView(options.get(0), holder, index);
                    break;
                case "area":
                    // askArea(questionNode, context, adapter);
                    addTextEnterView(options.get(0), holder, index);
                    break;
                case "duration":
                    // askDuration(questionNode, context, adapter);
                    addDurationView(options.get(0), holder, index);
                    break;
                case "range":
                    // askRange(questionNode, context, adapter);
                    addRangeView(options.get(0), holder, index);
                    break;
                case "frequency":
                    //askFrequency(questionNode, context, adapter);
                    addFrequencyView(options.get(0), holder, index);
                    break;
                case "camera":
                    // openCamera(context, imagePath, imageName);
                    CustomLog.v("showCameraView", "showOptionsData 1");
                    showCameraView(options.get(0), holder, index);
                    break;

                case "options":
                    // openCamera(context, imagePath, imageName);
                    //showOptionsData(genericViewHolder, genericViewHolder.node.getOptionsList());
                    break;
                default:
                    holder.submitButton.setVisibility(View.VISIBLE);
                    break;
            }

        } else {

            holder.tvQuestionDesc.setVisibility(View.VISIBLE);
            holder.recyclerView.setVisibility(View.VISIBLE);

            if (mItemList.get(index).isMultiChoice()) {
                holder.tvQuestionDesc.setText(mContext.getString(R.string.select_one_or_more));
                holder.submitButton.setVisibility(View.VISIBLE);
                holder.submitButton.setBackgroundResource(selectedNode.isDataCaptured() ? R.drawable.ui2_common_primary_bg : R.drawable.ui2_common_button_bg_submit);
                if (mItemList.get(index).isDataCaptured()) {
                    AdapterUtils.setToDisable(holder.skipButton);
                } else {
                    AdapterUtils.setToDefault(holder.skipButton);
                }
            } else {
                holder.tvQuestionDesc.setText(mContext.getString(R.string.select_any_one));
                holder.submitButton.setVisibility(View.GONE);
                if (mItemList.get(index).isDataCaptured()) {
                    AdapterUtils.setToDisable(holder.skipButton);
                } else {
                    AdapterUtils.setToDefault(holder.skipButton);
                }
            }

            if (mItemList.get(index).isRequired()) {
                holder.skipButton.setVisibility(View.GONE);
            } else {
                holder.skipButton.setVisibility(View.VISIBLE);
            }
            //if (isSuperNested) {
            boolean havingNestedQuestion = selectedNode.isHavingMoreNestedQuestion();
            CustomLog.v(TAG, "showOptionsData havingNestedQuestion - " + havingNestedQuestion);

                /*//holder.superNestedContainerLinearLayout.removeAllViews();
                View v1 = View.inflate(mContext, R.layout.nested_recycle_view, null);
                v1.setTag(selectedNode.getId());
                for (int i = 0; i < holder.superNestedContainerLinearLayout.getChildCount(); i++) {
                    if (String.valueOf(holder.superNestedContainerLinearLayout.getChildAt(i).getTag()).equalsIgnoreCase(selectedNode.getId())) {
                        return;
                    }
                }

                RecyclerView recyclerView = v1.findViewById(R.id.rcv_nested_container);
                TextView questionTextView = v1.findViewById(R.id.tv_question);
                LinearLayout singleComponentContainerLinearLayout = v1.findViewById(R.id.ll_single_component_container);
                LinearLayout othersContainerLinearLayout = v1.findViewById(R.id.ll_others_container);
                FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(mContext);
                layoutManager.setFlexDirection(FlexDirection.ROW);
                layoutManager.setJustifyContent(JustifyContent.FLEX_START);
                recyclerView.setLayoutManager(layoutManager);
                TextView tvQuestionDesc = v1.findViewById(R.id.tv_question_desc);

                if (selectedNode.isMultiChoice()) {
                    tvQuestionDesc.setText(mContext.getString(R.string.select_one_or_more));
                    holder.submitButton.setVisibility(View.VISIBLE);
                } else {
                    tvQuestionDesc.setText(mContext.getString(R.string.select_any_one));
                    holder.submitButton.setVisibility(View.GONE);

                }*/

                /*if (mItemList.get(index).isRequired()) {
                    skipButton.setVisibility(View.GONE);
                } else {
                    skipButton.setVisibility(View.VISIBLE);
                }*/
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
            linearLayoutManager.setStackFromEnd(false);
            linearLayoutManager.setSmoothScrollbarEnabled(true);
            holder.nestedRecyclerView.setLayoutManager(linearLayoutManager);


            //if (holder.nestedRecyclerView.getAdapter() != null && mItemList.get(index).isMultiChoice()) {
            //   nestedQuestionsListingAdapter = (NestedQuestionsListingAdapter) holder.nestedRecyclerView.getAdapter();
            //}else {
            holder.nestedQuestionsListingAdapter = new NestedQuestionsListingAdapter(mContext, mRecyclerView, holder.nestedRecyclerView, selectedNode, 0, index, mIsEditMode, mItemList.get(holder.getAbsoluteAdapterPosition()).isRequired(), new OnItemSelection() {
                @Override
                public void onSelect(Node node, int index, boolean isSkipped, Node parentNode) {
                    CustomLog.v(TAG, "NestedQuestionsListingAdapter onSelect index- " + index + " isSkipped = " + isSkipped);
                    CustomLog.v(TAG, "NestedQuestionsListingAdapter onSelect selectedNode - " + selectedNode.findDisplay());
                    CustomLog.v(TAG, "NestedQuestionsListingAdapter onSelect selectedNode - " + selectedNode.getId());
                    CustomLog.v(TAG, "NestedQuestionsListingAdapter onSelect node - " + node.findDisplay());
                    CustomLog.v(TAG, "NestedQuestionsListingAdapter onSelect options.size() - " + options.size());
                   /* if(parentNode.isHavingNestedQuestion()) {
                        boolean isMoreNestedLevel = true;
                        for (int i = 0; i < selectedNode.getOptionsList().size(); i++) {
                            if (selectedNode.getOptionsList().get(i).getId().equalsIgnoreCase(node.getId())) {
                                isMoreNestedLevel = false;
                            }else{

                            }
                        }
                        if (isMoreNestedLevel) {
                            holder.isParallelMultiNestedNode = true;
                            holder.nextRelativeLayout.setVisibility(View.VISIBLE);
                        } else {
                            holder.nextRelativeLayout.setVisibility(View.GONE);
                        }
                    }*/
                    holder.isParallelMultiNestedNode = false;
                    if (parentNode != null) {
                        holder.isParallelMultiNestedNode = parentNode.isHavingMoreNestedQuestion();
                        //holder.nextRelativeLayout.setVisibility(View.VISIBLE);
                        //holder.nextRelativeLayout.setVisibility(View.GONE);
                    }
                    VisitUtils.scrollNow(mRecyclerView, 1400, 0, 400, mIsEditMode, mLoadedIds.contains(mItemList.get(index).getId()));
                    ((LinearLayoutManager) Objects.requireNonNull(mRecyclerView.getLayoutManager())).setStackFromEnd(false);
                    boolean isLastNodeSubmit = holder.selectedNestedOptionIndex >= options.size() - 1;
                    if (isSkipped) {
                        if (options.size() == 1) {
                            CustomLog.v(TAG, "NestedQuestionsListingAdapter onSelect isSkipped && options.size() == 1 ");
                            mItemList.get(index).setSelected(false);
                            mItemList.get(index).setDataCaptured(false);
                            selectedNode.setSelected(false);
                            selectedNode.setDataCaptured(false);
                            selectedNode.unselectAllNestedNode();
                            notifyItemChanged(index);
                            if (selectedNode.isRequired()) return;
                        } else {
                            if (isLastNodeSubmit)
                                holder.selectedNestedOptionIndex = holder.selectedNestedOptionIndex - 1;
                        }

                    }

                    CustomLog.v(TAG, "NestedQuestionsListingAdapter onSelect mOnItemSelection.onSelect holder.selectedNestedOptionIndex - " + holder.selectedNestedOptionIndex);
                    if (!holder.isParallelMultiNestedNode || isLastNodeSubmit)
                        mOnItemSelection.onSelect(node, index, isSkipped, selectedNode);
                    else {
                        holder.selectedNestedOptionIndex += 1;
                        holder.nestedQuestionsListingAdapter.addItem(TAG, options.get(holder.selectedNestedOptionIndex));
                    }
                    //VisitUtils.scrollNow(holder.nestedRecyclerView, 1000, 0, 300);
                }

                @Override
                public void needTitleChange(String title) {

                }

                @Override
                public void onAllAnswered(boolean isAllAnswered) {

                }

                @Override
                public void onCameraRequest() {

                }

                @Override
                public void onImageRemoved(int nodeIndex, int imageIndex, String image) {

                }
                @Override
                public void onTerminalNodeAnsweredForParentUpdate(String parentNodeId) {
                    selectedNode.setDataCaptured(true);
                }
            });
            holder.nestedQuestionsListingAdapter.setLoadedIds(mLoadedIds);
            holder.nestedRecyclerView.setAdapter(holder.nestedQuestionsListingAdapter);
            // }
            holder.nestedQuestionsListingAdapter.setSuperNodeList(mItemList);

            //if (havingNestedQuestion) {
            if (isSuperNested) {

                //questionTextView.setText(options.get(0).findDisplay());
                holder.nestedQuestionsListingAdapter.clearItems();
                if (mIsEditMode) {
                    for (int i = 0; i < options.size(); i++) {
                        holder.nestedQuestionsListingAdapter.addItem(TAG, options.get(i));
                    }
                } else if (holder.selectedNestedOptionIndex > 0) {
                    for (int i = 0; i <= holder.selectedNestedOptionIndex; i++) {
                        if (options.size() < i) {
                            holder.nestedQuestionsListingAdapter.addItem(TAG, options.get(i));
                        }
                    }
                } else {
                    holder.nestedQuestionsListingAdapter.addItem(TAG, options.get(holder.selectedNestedOptionIndex));
                }
                holder.isParallelMultiNestedNode = options.size() > 1;

                /*if (holder.isParallelMultiNestedNode) {
                    //holder.nextRelativeLayout.setVisibility(View.VISIBLE);
                    holder.nextRelativeLayout.setVisibility(View.GONE);
                } else {
                    holder.nextRelativeLayout.setVisibility(View.GONE);
                }*/
                holder.nestedRecyclerView.setVisibility(View.VISIBLE);
                holder.submitButton.setVisibility(View.GONE);
                holder.skipButton.setVisibility(View.GONE);
                VisitUtils.scrollNow(mRecyclerView, 1000, 0, 600, mIsEditMode, mLoadedIds.contains(mItemList.get(index).getId()));
            } /*else if (isSuperNested) {
                nestedQuestionsListingAdapter.addItem(selectedNode);
                holder.nestedRecyclerView.setVisibility(View.VISIBLE);
            }*/ else {
                //holder.nextRelativeLayout.setVisibility(View.GONE);
                CustomLog.v(TAG, "showOptionsData multiple option");
                holder.tvQuestionDesc.setVisibility(View.VISIBLE);
                holder.recyclerView.setVisibility(View.VISIBLE);
                holder.nestedRecyclerView.setVisibility(View.GONE);
                //holder.superNestedContainerLinearLayout.setVisibility(View.GONE);
                if (mItemList.get(index).isMultiChoice()) {
                    holder.tvQuestionDesc.setText(mContext.getString(R.string.select_one_or_more));
                    holder.submitButton.setVisibility(View.VISIBLE);
                    holder.submitButton.setBackgroundResource(selectedNode.isDataCaptured() ? R.drawable.ui2_common_primary_bg : R.drawable.ui2_common_button_bg_submit);
                    if (mItemList.get(index).isDataCaptured()) {
                        AdapterUtils.setToDisable(holder.skipButton);
                    } else {
                        AdapterUtils.setToDefault(holder.skipButton);
                    }
                } else {
                    holder.tvQuestionDesc.setText(mContext.getString(R.string.select_any_one));
                    holder.submitButton.setVisibility(View.GONE);
                    if (mItemList.get(index).isDataCaptured()) {
                        AdapterUtils.setToDisable(holder.skipButton);
                    } else {
                        AdapterUtils.setToDefault(holder.skipButton);
                    }
                }

                if (mItemList.get(index).isRequired()) {
                    holder.skipButton.setVisibility(View.GONE);
                } else {
                    holder.skipButton.setVisibility(View.VISIBLE);
                }
                //holder.recyclerView.setLayoutManager(new GridLayoutManager(mContext, options.size() == 1 ? 1 : 2));
                FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(mContext);
                layoutManager.setFlexDirection(FlexDirection.ROW);
                layoutManager.setJustifyContent(JustifyContent.FLEX_START);
                holder.recyclerView.setLayoutManager(layoutManager);

                //**********
                // Avoid the duplicate options asking to user in connected questions
                //**************
                String duplicateCheckNodeNames = mItemList.get(index).getCompareDuplicateNode();
                CustomLog.v(TAG, "duplicateCheckNodeNames - " + duplicateCheckNodeNames);
                if (duplicateCheckNodeNames != null && !duplicateCheckNodeNames.isEmpty()) {
                    int sourceIndex = 0;
                    Node toCompareWithNode = null;
                    for (int i = 0; i < mItemList.size(); i++) {
                        if (mItemList.get(i).getText().equalsIgnoreCase(duplicateCheckNodeNames)) {
                            toCompareWithNode = mItemList.get(i);
                            CustomLog.v(TAG, "toCompareWithNode - " + new Gson().toJson(toCompareWithNode));
                            break;
                        }
                    }
                    NodeAdapterUtils.updateForHideShowFlag(mContext, mItemList.get(index), toCompareWithNode);
                }
                VisitUtils.scrollNow(mRecyclerView, 1400, 0, 600, mIsEditMode, mLoadedIds.contains(mItemList.get(index).getId()));
                // *****************
                OptionsChipsGridAdapter optionsChipsGridAdapter = new OptionsChipsGridAdapter(holder.recyclerView, mContext, mItemList.get(index), options, new OptionsChipsGridAdapter.OnItemSelection() {
                    @Override
                    public void onSelect(Node node, boolean isLoadingForNestedEditData) {
                        if (!isLoadingForNestedEditData)
                            VisitUtils.scrollNow(mRecyclerView, 1000, 0, 300, mIsEditMode, mLoadedIds.contains(mItemList.get(index).getId()));
                        ((LinearLayoutManager) Objects.requireNonNull(mRecyclerView.getLayoutManager())).setStackFromEnd(false);
                        if (!isLoadingForNestedEditData) {
                            mItemList.get(index).setSelected(false);
                            mItemList.get(index).setDataCaptured(false);
                        }
                        for (int i = 0; i < options.size(); i++) {
                            if (options.get(i).isSelected()) {
                                mItemList.get(index).setSelected(true);
                                //mItemList.get(index).setDataCaptured(true);
                                break;
                            }
                        }
                        //Toast.makeText(mContext, "Selected : " + data, Toast.LENGTH_SHORT).show();
                        String type = node.getInputType();

                        if (type == null || type.isEmpty() && (node.getOptionsList() != null && !node.getOptionsList().isEmpty())) {
                            type = "options";
                        }
                        CustomLog.v(TAG, "optionsChipsGridAdapter - Type - " + type);
                        CustomLog.v(TAG, "optionsChipsGridAdapter - isLoadingForNestedEditData - " + isLoadingForNestedEditData);
                        CustomLog.v(TAG, "optionsChipsGridAdapter - Node - " + node.findDisplay() + " isSelected - " + node.isSelected() + " isExcludedFromMultiChoice - " + node.isExcludedFromMultiChoice());
                        if (!type.isEmpty() && node.isSelected()) {

                            holder.singleComponentContainer.removeAllViews();
                            holder.singleComponentContainer.setVisibility(View.VISIBLE);

                        } else {
                            holder.singleComponentContainer.removeAllViews();
                            //holder.superNestedContainerLinearLayout.removeAllViews();
                            boolean isAnyOtherOptionSelected = false;
                            for (int i = 0; i < options.size(); i++) {
                                if (options.get(i).isSelected()) {
                                    isAnyOtherOptionSelected = true;
                                    Timber.tag(TAG).d("Option[%1s]=>%2s", i, options.get(i).getText());
                                    break;
                                }
                            }

                            Timber.tag(TAG).d("isAnyOtherOptionSelected => %s", isAnyOtherOptionSelected);

                            if (isLoadingForNestedEditData) {
                                if (selectedNode.isDataCaptured()) {
                                    AdapterUtils.setToDisable(holder.skipButton);
                                    AdapterUtils.setToDisable(holder.submitButton);
                                } else {
                                    AdapterUtils.setToDefault(holder.skipButton);
                                    AdapterUtils.setToDefault(holder.submitButton);
                                }
                            } else {
                                AdapterUtils.setToDefault(holder.submitButton);
                                AdapterUtils.setToDefault(holder.skipButton);

                            }
                            if (mIsFromAssociatedSymptoms) {
                                CustomLog.v(TAG, "optionsChipsGridAdapter - mItemList.get(index) - " + new Gson().toJson(mItemList.get(index)));
                                CustomLog.v(TAG, "optionsChipsGridAdapter - index - " + index);
                            }
                            /*AdapterUtils.setToDefault(holder.submitButton);
                            AdapterUtils.setToDefault(holder.skipButton);*/
                            /*holder.submitButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0,  0, 0);
                            holder.submitButton.setBackgroundResource(R.drawable.ui2_common_button_bg_submit);*/

                            if (mItemList.get(index).isMultiChoice()) {
                                holder.tvQuestionDesc.setText(mContext.getString(R.string.select_one_or_more));
                                if (!isAnyOtherOptionSelected)
                                    holder.submitButton.setVisibility(View.VISIBLE);
                            } else {
                                holder.tvQuestionDesc.setText(mContext.getString(R.string.select_any_one));
                                holder.submitButton.setVisibility(View.GONE);

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!isLoadingForNestedEditData) {

                                            notifyItemChanged(index);
                                        }
                                    }
                                }, 100);
                                if (!isLoadingForNestedEditData) {
                                    mOnItemSelection.onSelect(node, index, false, selectedNode);
                                    AdapterUtils.setToDisable(holder.skipButton);
                                }
                            }

                            if (mItemList.get(index).isRequired()) {
                                holder.skipButton.setVisibility(View.GONE);
                            } else {
                                if (!isAnyOtherOptionSelected)
                                    holder.skipButton.setVisibility(View.VISIBLE);
                            }

                            if (node.isExcludedFromMultiChoice()) {
                                holder.nestedRecyclerView.setVisibility(View.GONE);
                                if (!isLoadingForNestedEditData) {
                                    notifyItemChanged(index);
                                    VisitUtils.scrollNow(mRecyclerView, 1400, 0, 1000, mIsEditMode, mLoadedIds.contains(mItemList.get(index).getId()));
                                }
                            }
                            return;
                        }

                        switch (type) {
                            case "text":
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                // askText(questionNode, context, adapter);
                                addTextEnterView(node, holder, index);
                                break;
                            case "date":
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                //askDate(questionNode, context, adapter);
                                addDateView(node, holder, index);
                                break;
                            case "location":
                                //askLocation(questionNode, context, adapter);
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                // askText(questionNode, context, adapter);
                                addTextEnterView(node, holder, index);
                                break;
                            case "number":
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                // askNumber(questionNode, context, adapter);
                                addNumberView(node, holder, index);
                                break;
                            case "area":
                                // askArea(questionNode, context, adapter);
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                // askText(questionNode, context, adapter);
                                addTextEnterView(node, holder, index);
                                break;
                            case "duration":
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                // askDuration(questionNode, context, adapter);
                                addDurationView(node, holder, index);
                                break;
                            case "range":
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                // askRange(questionNode, context, adapter);
                                addRangeView(node, holder, index);
                                break;
                            case "frequency":
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                //askFrequency(questionNode, context, adapter);
                                addFrequencyView(node, holder, index);
                                break;
                            case "camera":
                                holder.submitButton.setVisibility(View.GONE);
                                holder.skipButton.setVisibility(View.GONE);
                                // openCamera(context, imagePath, imageName);
                                CustomLog.v("showCameraView", "showOptionsData 2");
                                showCameraView(node, holder, index);
                                break;

                            case "options":
                                // openCamera(context, imagePath, imageName);
                                //holder.superNestedContainerLinearLayout.removeAllViews();
                                showOptionsData(node, holder, node.getOptionsList(), index, true, false);
                                break;
                        }
                        //notifyDataSetChanged();
                    }
                });
                holder.recyclerView.setAdapter(optionsChipsGridAdapter);


            }
            /*for (int i = 0; i < options.size(); i++) {
                String type = options.get(i).getInputType();
                if (type.equalsIgnoreCase("camera") && options.get(i).isSelected()) {
                    // openCamera(context, imagePath, imageName);
                    Log.v("showCameraView", "showOptionsData - " + new Gson().toJson(options.get(i).getImagePathList()));
                    showCameraView(options.get(i), holder, index);
                }
            }*/
        }

    }

    private void showCameraView(Node node, GenericViewHolder holder, int index) {
        Node parentNode = mItemList.get(index);
        CustomLog.v("showCameraView", "QLA parentNode" + new Gson().toJson(parentNode));
        CustomLog.v("showCameraView", "QLA " + new Gson().toJson(node));
        CustomLog.v("showCameraView", "QLA ImagePathList - " + new Gson().toJson(parentNode.getImagePathList()));
        CustomLog.v("showCameraView", "QLA ImagePathList isDataCaptured - " + parentNode.isDataCaptured());
        CustomLog.v("showCameraView", "QLA ImagePathList isImageUploaded - " + parentNode.isImageUploaded());
        holder.otherContainerLinearLayout.removeAllViews();
//        if (!parentNode.isMultiChoice())
        holder.submitButton.setVisibility(View.GONE);
        holder.skipButton.setVisibility(View.GONE);
        View view = View.inflate(mContext, R.layout.ui2_visit_image_capture_view, null);
        Button submitButton = view.findViewById(R.id.btn_submit);
        submitButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, parentNode.isImageUploaded() ? R.drawable.ic_baseline_check_18_white : 0, 0);
        submitButton.setBackgroundResource(parentNode.isImageUploaded() ? R.drawable.ui2_common_primary_bg : R.drawable.ui2_common_button_bg_submit);
        submitButton.setText(mContext.getString(R.string.visit_summary_button_upload));
        LinearLayout newImageCaptureLinearLayout = view.findViewById(R.id.ll_emptyView);
        //newImageCaptureLinearLayout.setVisibility(View.VISIBLE);
        newImageCaptureLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //openCamera(getImagePath(), "");
                node.setImageUploaded(false);
                node.setDataCaptured(false);
                parentNode.setImageUploaded(false);
                parentNode.setDataCaptured(false);
                mLastImageCaptureSelectedNodeIndex = index;
                mOnItemSelection.onCameraRequest();
            }
        });
        view.findViewById(R.id.btn_1st_capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //openCamera(getImagePath(), "");
                node.setImageUploaded(false);
                node.setDataCaptured(false);
                parentNode.setImageUploaded(false);
                parentNode.setDataCaptured(false);
                mLastImageCaptureSelectedNodeIndex = index;
                mOnItemSelection.onCameraRequest();
            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                node.setSkipped(false);
                AdapterUtils.buttonProgressAnimation(mContext, submitButton, true, new AdapterUtils.OnFinishActionListener() {
                    @Override
                    public void onFinish() {
                        node.setImageUploaded(true);
                        parentNode.setImageUploaded(true);

                        parentNode.setDataCaptured(true);
                        mOnItemSelection.onSelect(node, index, false, null);

                    }
                });
            }
        });

        RecyclerView imagesRcv = view.findViewById(R.id.rcv_added_image);
        imagesRcv.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));

        if (parentNode.getImagePathList().isEmpty()) {
            CustomLog.v("showCameraView", "QLA Images check - empty");
            newImageCaptureLinearLayout.setVisibility(View.VISIBLE);
            submitButton.setVisibility(View.GONE);
            imagesRcv.setVisibility(View.GONE);
        } else {
            CustomLog.v("showCameraView", "QLA Images check - having data");
            newImageCaptureLinearLayout.setVisibility(View.GONE);
            submitButton.setVisibility(View.VISIBLE);
            imagesRcv.setVisibility(View.VISIBLE);
        }

        if (!parentNode.getImagePathList().isEmpty()) {
            ImageGridAdapter imageGridAdapter = new ImageGridAdapter(imagesRcv, mContext, parentNode.getImagePathList(), new ImageGridAdapter.OnImageAction() {
                @Override
                public void onImageRemoved(int imageIndex, String image) {
                    node.setImageUploaded(false);
                    node.setDataCaptured(false);
                    parentNode.setImageUploaded(false);
                    parentNode.setDataCaptured(false);
                    mOnItemSelection.onImageRemoved(index, imageIndex, image);
                }

                @Override
                public void onNewImageRequest() {
                    node.setImageUploaded(false);
                    node.setDataCaptured(false);
                    parentNode.setImageUploaded(false);
                    parentNode.setDataCaptured(false);
                    mLastImageCaptureSelectedNodeIndex = index;
                    mOnItemSelection.onCameraRequest();
                }
            });
            imagesRcv.setAdapter(imageGridAdapter);
            imageGridAdapter.addNull();
            CustomLog.v("showCameraView", "ImagePathList recyclerView - " + imagesRcv.getAdapter().getItemCount());
            if (node.getImagePathList().size() >= 4) {
                imagesRcv.smoothScrollToPosition(imagesRcv.getAdapter().getItemCount() - 1);
            }
        }


        holder.otherContainerLinearLayout.addView(view);
        holder.otherContainerLinearLayout.setVisibility(View.VISIBLE);
        CustomLog.v("showCameraView", "ImagePathList - " + new Gson().toJson(node.getImagePathList()));
        CustomLog.v("showCameraView", "otherContainerLinearLayout getChildCount - " + holder.otherContainerLinearLayout.getChildCount());
    }


    /**
     * Time duration
     *
     * @param node
     * @param holder
     * @param index
     */
    private void addDurationView(Node node, GenericViewHolder holder, int index) {
        CustomLog.v(TAG, "addDurationView - " + new Gson().toJson(node));
        holder.singleComponentContainer.removeAllViews();
        holder.singleComponentContainer.setVisibility(View.VISIBLE);
        View view = View.inflate(mContext, R.layout.ui2_visit_reason_time_range, null);
        final Spinner numberRangeSpinner = view.findViewById(R.id.sp_number_range);
        final Spinner durationTypeSpinner = view.findViewById(R.id.sp_duration_type);
        Button submitButton = view.findViewById(R.id.btn_submit);
        Button skipButton = view.findViewById(R.id.btn_skip);

       /* if (node.isDataCaptured()) {
            AdapterUtils.setToDisable(skipButton);
        } else {
            AdapterUtils.setToDefault(skipButton);
        }*/
        if (!holder.node.isRequired()) skipButton.setVisibility(View.VISIBLE);
        else skipButton.setVisibility(View.GONE);

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                node.setSkipped(true);
                AdapterUtils.setToDisable(submitButton);
                AdapterUtils.buttonProgressAnimation(mContext, skipButton, false, new AdapterUtils.OnFinishActionListener() {
                    @Override
                    public void onFinish() {
                        node.setSelected(false);
                        mOnItemSelection.onSelect(node, index, true, null);
                    }
                });

            }
        });

        // add a list
        int i = 0;
        int max = 100;
        final String[] data = new String[max + 1];
        data[0] = mContext.getString(R.string.number_label);
        for (i = 1; i <= max; i++) {
            data[i] = String.valueOf(i);
        }

        ArrayAdapter<String> adaptador = new ArrayAdapter<String>(mContext,
                R.layout.simple_spinner_item_1, data);
        adaptador.setDropDownViewResource(R.layout.ui2_custome_dropdown_item_view);

        numberRangeSpinner.setAdapter(adaptador);
        numberRangeSpinner.setPopupBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.popup_menu_background));


        numberRangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int which, long l) {
                String newNumber = numberRangeSpinner.getSelectedItem().toString();
                if (!newNumber.equals(VisitUtils.getSplitLangByIndex(node.getLanguage(), 0))) {
                    AdapterUtils.setToDefault(submitButton);
                    AdapterUtils.setToDefault(skipButton);
                    node.setSelected(false);
                    holder.node.setSelected(false);

                    node.setDataCaptured(false);
                    holder.node.setDataCaptured(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // add a list
        final String[] data1 = new String[]{mContext.getString(R.string.duration_type),
                mContext.getString(R.string.Hours), mContext.getString(R.string.Days),
                mContext.getString(R.string.Weeks), mContext.getString(R.string.Months),
                mContext.getString(R.string.Years)};

        ArrayAdapter<String> adaptador1 = new ArrayAdapter<String>(mContext,
                R.layout.simple_spinner_item_1, data1);
        adaptador1.setDropDownViewResource(R.layout.ui2_custome_dropdown_item_view);

        durationTypeSpinner.setAdapter(adaptador1);
        durationTypeSpinner.setPopupBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.popup_menu_background));

        durationTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int which, long l) {
                String newType = durationTypeSpinner.getSelectedItem().toString();
                if (!newType.equals(VisitUtils.getSplitLangByIndex(node.getLanguage(), 1))) {
                    AdapterUtils.setToDefault(submitButton);
                    AdapterUtils.setToDefault(skipButton);
                    node.setSelected(false);
                    holder.node.setSelected(false);

                    node.setDataCaptured(false);
                    holder.node.setDataCaptured(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (!node.getLanguage().isEmpty()) {
            String oldDataNumber = VisitUtils.getSplitLangByIndex(node.getLanguage(), 0), oldDataType = VisitUtils.getSplitLangByIndex(node.getLanguage(), 1);

            if (!oldDataNumber.isEmpty())
                numberRangeSpinner.setSelection(Arrays.asList(data).indexOf(oldDataNumber));
            if (!oldDataType.isEmpty())
                durationTypeSpinner.setSelection(Arrays.asList(data1).indexOf(oldDataType));

        }

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (numberRangeSpinner.getSelectedItemPosition() == 0 || numberRangeSpinner.getSelectedItem().toString().isEmpty()) {
                    Toast.makeText(mContext, mContext.getString(R.string.duration_validation_txt), Toast.LENGTH_SHORT).show();
                    return;
                } else if (durationTypeSpinner.getSelectedItemPosition() == 0 || durationTypeSpinner.getSelectedItem().toString().isEmpty()) {
                    Toast.makeText(mContext, mContext.getString(R.string.duration_type_validation_txt), Toast.LENGTH_SHORT).show();
                    return;
                }
                String durationString = numberRangeSpinner.getSelectedItem().toString() + " " + durationTypeSpinner.getSelectedItem().toString();

                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", durationString));
                } else {
                    node.addLanguage(" " + durationString);
                    node.setText(durationString);
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }

                node.setSelected(true);
                holder.node.setSelected(true);

                node.setDataCaptured(true);
                holder.node.setDataCaptured(true);

                //notifyDataSetChanged();
                AdapterUtils.setToDisable(skipButton);
                node.setSkipped(false);
                AdapterUtils.buttonProgressAnimation(mContext, submitButton, true, new AdapterUtils.OnFinishActionListener() {
                    @Override
                    public void onFinish() {
                        mOnItemSelection.onSelect(node, index, false, null);

                    }
                });
            }
        });
        /*if (node.isDataCaptured() && node.isDataCaptured()) {
            submitButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_24_white, 0, 0, 0);
        } else {
            submitButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }*/

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                submitButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, node.isDataCaptured() ? R.drawable.ic_baseline_check_18_white : 0, 0);
                submitButton.setBackgroundResource(node.isDataCaptured() ? R.drawable.ui2_common_primary_bg : R.drawable.ui2_common_button_bg_submit);

                if (node.isSkipped()) {
                    skipButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_check_18_white, 0);
                    skipButton.setBackgroundResource(R.drawable.ui2_common_primary_bg);
                    AdapterUtils.setToDisable(submitButton);
                }
            }
        }, 1000);
        boolean isParentNodeIsMandatory = mItemList.get(index).isRequired();
        if (isParentNodeIsMandatory)
            skipButton.setVisibility(View.GONE);
        holder.singleComponentContainer.addView(view);
        CustomLog.v(TAG, "addDurationView holder.singleComponentContainer count child - " + holder.singleComponentContainer.getChildCount());
        CustomLog.v(TAG, "addDurationView holder.singleComponentContainer VISIBLE - " + (holder.singleComponentContainer.getVisibility() == View.VISIBLE));
    }

    private void showNumberListing(final TextView textView, String title, int i, int max) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(title);

        // add a list
        final String[] data = new String[max];
        for (; i < max; i++) {
            data[i] = String.valueOf(i);
        }
        builder.setItems(data, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                textView.setText(data[which]);

            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDurationTypes(final TextView textView) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getString(R.string.select_duration_type_title));

        // add a list
        final String[] data = new String[]{
                mContext.getString(R.string.Hours), mContext.getString(R.string.Days),
                mContext.getString(R.string.Weeks), mContext.getString(R.string.Months),
                mContext.getString(R.string.Years)};

        builder.setItems(data, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                textView.setText(data[which]);

            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void addNumberView(Node node, GenericViewHolder holder, int index) {
        holder.singleComponentContainer.removeAllViews();
        holder.singleComponentContainer.setVisibility(View.VISIBLE);
        View view = View.inflate(mContext, R.layout.visit_reason_input_text, null);
        Button submitButton = view.findViewById(R.id.btn_submit);
        submitButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, node.isDataCaptured() ? R.drawable.ic_baseline_check_18_white : 0, 0);
        submitButton.setBackgroundResource(node.isDataCaptured() ? R.drawable.ui2_common_primary_bg : R.drawable.ui2_common_button_bg_submit);

        final EditText editText = view.findViewById(R.id.actv_reasons);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        Button skipButton = view.findViewById(R.id.btn_skip);

        if (node.isSkipped()) {
            skipButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_check_18_white, 0);
            skipButton.setBackgroundResource(R.drawable.ui2_common_primary_bg);
            AdapterUtils.setToDisable(submitButton);
        }

        if (node.isSelected() && node.getLanguage() != null && node.isDataCaptured()) {
            //  editText.setText(node.getLanguage());
            if (node.getLanguage().contains(" : "))
                editText.setText(node.getLanguage().split(" : ")[1]);
            else
                editText.setText(node.getLanguage());
        }
        String oldValue = editText.getText().toString().trim();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().trim().equals(oldValue)) {
                    AdapterUtils.setToDefault(submitButton);
                    AdapterUtils.setToDefault(skipButton);
                }
            }
        });
        /*if (node.isDataCaptured()) {
            AdapterUtils.setToDisable(skipButton);
        } else {
            AdapterUtils.setToDefault(skipButton);
        }*/
        if (!holder.node.isRequired()) skipButton.setVisibility(View.VISIBLE);
        else skipButton.setVisibility(View.GONE);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                node.setSelected(false);
                node.setDataCaptured(false);
                //holder.node.setDataCaptured(true);
                node.setSkipped(true);
                AdapterUtils.setToDisable(submitButton);
                AdapterUtils.buttonProgressAnimation(mContext, skipButton, false, new AdapterUtils.OnFinishActionListener() {
                    @Override
                    public void onFinish() {
                        mOnItemSelection.onSelect(node, index, true, null);
                    }
                });

                WindowsUtils.hideSoftKeyboard((AppCompatActivity) mContext);
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText().toString().trim().isEmpty()) {
                    Toast.makeText(mContext, mContext.getString(R.string.please_enter_the_value), Toast.LENGTH_SHORT).show();
                } else {
                    if (!editText.getText().toString().equalsIgnoreCase("")) {
                        if (node.getLanguage().contains("_")) {
                            node.setLanguage(node.getLanguage().replace("_", editText.getText().toString()));
                        } else {
                            node.addLanguage(editText.getText().toString());
                        }

                        node.setSelected(true);
                        holder.node.setSelected(true);

                        node.setDataCaptured(true);
                        holder.node.setDataCaptured(true);
                    } else {
                        node.setDataCaptured(false);
                        holder.node.setDataCaptured(false);


                        //if (node.isRequired()) {
                        node.setSelected(false);
                        holder.node.setSelected(false);
                        //} else {
                        if (node.getLanguage().contains("_")) {
                            node.setLanguage(node.getLanguage().replace("_", "Question not answered"));
                        } else {
                            node.addLanguage("Question not answered");
                            //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                        }
                        //   node.setSelected(true);
                        //}
                    }
                    //notifyDataSetChanged();
                    AdapterUtils.setToDisable(skipButton);
                    node.setSkipped(false);
                    AdapterUtils.buttonProgressAnimation(mContext, submitButton, true, new AdapterUtils.OnFinishActionListener() {
                        @Override
                        public void onFinish() {
                            mOnItemSelection.onSelect(node, index, false, null);
                        }
                    });

                    WindowsUtils.hideSoftKeyboard((AppCompatActivity) mContext);
                }
            }
        });

        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setHint(mContext.getString(R.string.describe_hint_txt));
        /*if (node.isDataCaptured() && node.isDataCaptured()) {
            submitButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_24_white, 0, 0, 0);
        } else {
            submitButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }*/
        boolean isParentNodeIsMandatory = mItemList.get(index).isRequired();
        if (isParentNodeIsMandatory)
            skipButton.setVisibility(View.GONE);
        holder.singleComponentContainer.addView(view);
        holder.singleComponentContainer.setVisibility(View.VISIBLE);
    }

    private void addTextEnterView(Node node, GenericViewHolder holder, int index) {

        CustomLog.v(TAG, "addTextEnterView");
//        boolean tag = false;
//        if (holder.singleComponentContainer.getTag() != null) {
//            tag = (boolean) holder.singleComponentContainer.getTag();
//        }
//        Timber.tag(TAG).d("TAG =>%s", tag);
//        Timber.tag(TAG).d("Single size=>%s", holder.singleComponentContainer.getChildCount());
//        if (!tag)
        holder.singleComponentContainer.removeAllViews();
        holder.singleComponentContainer.setVisibility(View.VISIBLE);
        View view = View.inflate(mContext, R.layout.visit_reason_input_text, null);
        Button submitButton = view.findViewById(R.id.btn_submit);
        submitButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, node.isDataCaptured() ? R.drawable.ic_baseline_check_18_white : 0, 0);
        submitButton.setBackgroundResource(node.isDataCaptured() ? R.drawable.ui2_common_primary_bg : R.drawable.ui2_common_button_bg_submit);
        final EditText editText = view.findViewById(R.id.actv_reasons);
        Timber.tag(TAG).d("Input =>%s", node.getLanguage());
        if (node.isSelected() && node.getLanguage() != null && node.isDataCaptured()) {
            editText.setText(node.getLanguage());
        }
        String oldValue = editText.getText().toString().trim();
        Button skipButton = view.findViewById(R.id.btn_skip);
        if (node.isSkipped()) {
            skipButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_check_18_white, 0);
            skipButton.setBackgroundResource(R.drawable.ui2_common_primary_bg);
            AdapterUtils.setToDisable(submitButton);
        }
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().trim().equals(oldValue)) {
                    AdapterUtils.setToDefault(submitButton);
                    AdapterUtils.setToDefault(skipButton);
                }
            }
        });
        /*if (node.isDataCaptured()) {
            AdapterUtils.setToDisable(skipButton);
        } else {
            AdapterUtils.setToDefault(skipButton);
        }*/
        if (!holder.node.isRequired()) skipButton.setVisibility(View.VISIBLE);
        else skipButton.setVisibility(View.GONE);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                node.setSelected(false);
                node.setDataCaptured(false);
                node.setSkipped(true);
                AdapterUtils.setToDisable(submitButton);
                AdapterUtils.buttonProgressAnimation(mContext, skipButton, true, new AdapterUtils.OnFinishActionListener() {
                    @Override
                    public void onFinish() {
                        mOnItemSelection.onSelect(node, index, true, null);
                    }
                });
                WindowsUtils.hideSoftKeyboard((AppCompatActivity) mContext);
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText().toString().trim().isEmpty()) {
                    Toast.makeText(mContext, mContext.getString(R.string.please_enter_the_value), Toast.LENGTH_SHORT).show();
                } else {

                    if (!editText.getText().toString().equalsIgnoreCase("")) {
                        if (node.getLanguage().contains("_")) {
                            node.setLanguage(node.getLanguage().replace("_", editText.getText().toString()));
                        } else {
                            node.addLanguage(editText.getText().toString());
                        }
                        node.setSelected(true);
                        holder.node.setSelected(true);

                        node.setDataCaptured(true);
                        holder.node.setDataCaptured(true);

                    } else {
                        node.setDataCaptured(false);
                        holder.node.setDataCaptured(false);
                        //if (node.isRequired()) {
                        node.setSelected(false);
                        holder.node.setSelected(false);
                        //} else {
                        if (node.getLanguage().contains("_")) {
                            node.setLanguage(node.getLanguage().replace("_", "Question not answered"));
                        } else {
                            node.addLanguage("Question not answered");
                            //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                        }
                        //   node.setSelected(true);
                        //}
                    }
                    //notifyDataSetChanged();
                    AdapterUtils.setToDisable(skipButton);
                    node.setSkipped(false);

                    AdapterUtils.buttonProgressAnimation(mContext, submitButton, true, new AdapterUtils.OnFinishActionListener() {
                        @Override
                        public void onFinish() {
                            mOnItemSelection.onSelect(node, index, false, null);
                        }
                    });
                    WindowsUtils.hideSoftKeyboard((AppCompatActivity) mContext);
                }
            }
        });

        editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setFilters(new InputFilter[]{new FirstLetterUpperCaseInputFilter()});
        editText.setMinLines(5);
        editText.setLines(5);
        editText.setHorizontallyScrolling(false);
        editText.setHint(mContext.getString(R.string.describe_hint_txt));
        editText.setMinHeight(320);

        /*if (node.isDataCaptured() && node.isDataCaptured()) {
            submitButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_24_white, 0, 0, 0);
        } else {
            submitButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
*/
        boolean isParentNodeIsMandatory = mItemList.get(index).isRequired();

        if (isParentNodeIsMandatory || (holder.node.getOptionsList() != null && holder.node.getOptionsList().size() > 1))
            skipButton.setVisibility(View.GONE);
        holder.singleComponentContainer.addView(view);
    }

    private void addDateView(Node node, GenericViewHolder holder, int index) {
        holder.singleComponentContainer.removeAllViews();
        holder.singleComponentContainer.setVisibility(View.VISIBLE);
        View view = View.inflate(mContext, R.layout.visit_reason_date, null);
        final Button submitButton = view.findViewById(R.id.btn_submit);
        submitButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, node.isDataCaptured() ? R.drawable.ic_baseline_check_18_white : 0, 0);
        submitButton.setBackgroundResource(node.isDataCaptured() ? R.drawable.ui2_common_primary_bg : R.drawable.ui2_common_button_bg_submit);
        final TextView displayDateButton = view.findViewById(R.id.btn_view_date);
        final TextView calendarHeader = view.findViewById(R.id.date_header);
//        calendarHeader.setVisibility(node.isShowCalendarHeader() ? View.VISIBLE : View.GONE);
//        calendarHeader.setText(node.getText());
        final CalendarView calendarView = view.findViewById(R.id.cav_date);
        calendarView.setMaxDate(System.currentTimeMillis() + 1000);
        Button skipButton = view.findViewById(R.id.btn_skip);
        String oldValue = node.getLanguage();
        if (node.isSkipped()) {
            skipButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_check_18_white, 0);
            skipButton.setBackgroundResource(R.drawable.ui2_common_primary_bg);
            AdapterUtils.setToDisable(submitButton);
        }
        if (node.isDataCaptured()) {
            displayDateButton.setText(oldValue);
            displayDateButton.setTag(oldValue);
        }
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                // display the selected date by using a toast
                int m = month + 1;
                //String date = (dayOfMonth < 10 ? "0" + dayOfMonth : String.valueOf(dayOfMonth))
                //       + "-" + (m < 10 ? "0" + m : String.valueOf(m)) + "-" + String.valueOf(year);
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(0);
                //cal.set(Integer.parseInt(d.split("-")[2]), Integer.parseInt(d.split("-")[1]) - 1, Integer.parseInt(d.split("-")[0]));
                cal.set(year, month, dayOfMonth);
                Date date = cal.getTime();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy", Locale.ENGLISH);
                SimpleDateFormat simpleDateFormatLocal = new SimpleDateFormat("dd/MMM/yyyy", new Locale(new SessionManager(mContext).getAppLanguage()));
                String dateString = simpleDateFormat.format(date);
                displayDateButton.setText(simpleDateFormatLocal.format(date));
                displayDateButton.setTag(dateString);
                VisitUtils.scrollNow(mRecyclerView, 400, 0, 400, mIsEditMode, mLoadedIds.contains(mItemList.get(index).getId()));
                AdapterUtils.setToDefault(submitButton);
                AdapterUtils.setToDefault(skipButton);
            }
        });
        holder.skipButton.setVisibility(View.GONE);

//        if (node.isDataCaptured()) {
//            AdapterUtils.setToDisable(skipButton);
//        } else {
//            AdapterUtils.setToDefault(skipButton);
//        }

        if (!holder.node.isRequired()) skipButton.setVisibility(View.VISIBLE);
        else skipButton.setVisibility(View.GONE);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                node.setSkipped(true);
                AdapterUtils.setToDisable(submitButton);
                AdapterUtils.buttonProgressAnimation(mContext, skipButton, false, new AdapterUtils.OnFinishActionListener() {
                    @Override
                    public void onFinish() {
                        node.setSelected(false);
                        mOnItemSelection.onSelect(node, index, true, null);
                    }
                });

            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String d = (String) displayDateButton.getTag();
                if (d == null || d.equalsIgnoreCase("null") || !d.contains("/")) {
                    Toast.makeText(mContext, mContext.getString(R.string.please_select_date), Toast.LENGTH_SHORT).show();
                } else {
                    /*Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(0);
                    cal.set(Integer.parseInt(d.split("-")[2]), Integer.parseInt(d.split("-")[1]) - 1, Integer.parseInt(d.split("-")[0]));
                    Date date = cal.getTime();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy", Locale.ENGLISH);*/


                    node.addLanguage(d);

                    node.setSelected(true);
                    holder.node.setSelected(true);

                    node.setDataCaptured(true);
                    holder.node.setDataCaptured(true);

                    //notifyDataSetChanged();
                    AdapterUtils.setToDisable(skipButton);
                    AdapterUtils.buttonProgressAnimation(mContext, submitButton, true, new AdapterUtils.OnFinishActionListener() {
                        @Override
                        public void onFinish() {
                            mOnItemSelection.onSelect(node, index, false, null);

                        }
                    });

                }
            }
        });
        /*if (node.isDataCaptured() && node.isDataCaptured()) {
            submitButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_24_white, 0, 0, 0);
        } else {
            submitButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }*/
        boolean isParentNodeIsMandatory = mItemList.get(index).isRequired();
        if (isParentNodeIsMandatory)
            skipButton.setVisibility(View.GONE);
        holder.singleComponentContainer.addView(view);
    }

    private void addFutureDateView(Node node, GenericViewHolder holder, int index) {
        holder.singleComponentContainer.setVisibility(View.VISIBLE);
        View view = View.inflate(mContext, R.layout.visit_reason_date, null);
        final Button submitButton = view.findViewById(R.id.btn_submit);
        submitButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, node.isDataCaptured() ? R.drawable.ic_baseline_check_18_white : 0, 0);
        submitButton.setBackgroundResource(node.isDataCaptured() ? R.drawable.ui2_common_primary_bg : R.drawable.ui2_common_button_bg_submit);

        final TextView displayDateButton = view.findViewById(R.id.btn_view_date);
        final TextView calendarHeader = view.findViewById(R.id.date_header);

        final CalendarView calendarView = view.findViewById(R.id.cav_date);

        // Restrict past dates by setting minDate to the current time
        calendarView.setMinDate(System.currentTimeMillis());
        calendarView.setMaxDate(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365)); // Allow up to 1 year ahead

        Button skipButton = view.findViewById(R.id.btn_skip);
        String oldValue = node.getLanguage();

        if (node.isSkipped()) {
            skipButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_check_18_white, 0);
            skipButton.setBackgroundResource(R.drawable.ui2_common_primary_bg);
            AdapterUtils.setToDisable(submitButton);
        }
        if (node.isDataCaptured()) {
            displayDateButton.setText(oldValue);
            displayDateButton.setTag(oldValue);
        }

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                Calendar cal = Calendar.getInstance();
                cal.set(year, month, dayOfMonth);
                Date date = cal.getTime();

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy", Locale.ENGLISH);
                SimpleDateFormat simpleDateFormatLocal = new SimpleDateFormat("dd/MMM/yyyy", new Locale(new SessionManager(mContext).getAppLanguage()));

                String dateString = simpleDateFormat.format(date);
                displayDateButton.setText(simpleDateFormatLocal.format(date));
                displayDateButton.setTag(dateString);

                VisitUtils.scrollNow(mRecyclerView, 400, 0, 400, mIsEditMode, mLoadedIds.contains(mItemList.get(index).getId()));
                AdapterUtils.setToDefault(submitButton);
                AdapterUtils.setToDefault(skipButton);
            }
        });

        holder.skipButton.setVisibility(View.GONE);
        if (!holder.node.isRequired()) skipButton.setVisibility(View.VISIBLE);
        else skipButton.setVisibility(View.GONE);

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                node.setSkipped(true);
                AdapterUtils.setToDisable(submitButton);
                AdapterUtils.buttonProgressAnimation(mContext, skipButton, false, new AdapterUtils.OnFinishActionListener() {
                    @Override
                    public void onFinish() {
                        node.setSelected(false);
                        mOnItemSelection.onSelect(node, index, true, null);
                    }
                });
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String d = (String) displayDateButton.getTag();
                if (d == null || d.equalsIgnoreCase("null") || !d.contains("/")) {
                    Toast.makeText(mContext, mContext.getString(R.string.please_select_date), Toast.LENGTH_SHORT).show();
                } else {
                    node.addLanguage(d);
                    node.setSelected(true);
                    holder.node.setSelected(true);
                    node.setDataCaptured(true);
                    holder.node.setDataCaptured(true);

                    AdapterUtils.setToDisable(skipButton);
                    AdapterUtils.buttonProgressAnimation(mContext, submitButton, true, new AdapterUtils.OnFinishActionListener() {
                        @Override
                        public void onFinish() {
                            mOnItemSelection.onSelect(node, index, false, null);
                        }
                    });
                }
            }
        });

        boolean isParentNodeIsMandatory = mItemList.get(index).isRequired();
        if (isParentNodeIsMandatory) skipButton.setVisibility(View.GONE);
        holder.singleComponentContainer.addView(view);
    }


    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    private class GenericViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion, tvQuestionDesc, tvQuestionCounter, tvReferenceDesc;
        Node node;
        int index, rootIndex;
        RecyclerView recyclerView, nestedRecyclerView;
        // this will contain independent view like, edittext, date, time, range, etc
        LinearLayout singleComponentContainer, referenceContainerLinearLayout, otherContainerLinearLayout;//, superNestedContainerLinearLayout;
        SpinKitView spinKitView;
        LinearLayout bodyLayout;
        Button submitButton, skipButton/*, nextButton*/;
        TextView knowMoreTextView;
        //RelativeLayout nextRelativeLayout;
        boolean isParallelMultiNestedNode = false;

        NestedQuestionsListingAdapter nestedQuestionsListingAdapter = null;
        int selectedNestedOptionIndex = 0;

        GenericViewHolder(View itemView) {
            super(itemView);
            //nextRelativeLayout = itemView.findViewById(R.id.rl_next_action);
            //nextButton = itemView.findViewById(R.id.btn_next);

            knowMoreTextView = itemView.findViewById(R.id.tv_know_more);
            knowMoreTextView.setPaintFlags(knowMoreTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            knowMoreTextView.setVisibility(View.GONE);

            skipButton = itemView.findViewById(R.id.btn_skip);
            submitButton = itemView.findViewById(R.id.btn_submit);
            recyclerView = itemView.findViewById(R.id.rcv_container);
            nestedRecyclerView = itemView.findViewById(R.id.rcv_nested_container);
            singleComponentContainer = itemView.findViewById(R.id.ll_single_component_container);
            referenceContainerLinearLayout = itemView.findViewById(R.id.ll_reference_container);
            otherContainerLinearLayout = itemView.findViewById(R.id.ll_others_container);

            //superNestedContainerLinearLayout = itemView.findViewById(R.id.ll_super_nested_container);
            //superNestedContainerLinearLayout.setVisibility(View.GONE);

            tvReferenceDesc = itemView.findViewById(R.id.tv_reference_desc);
            spinKitView = itemView.findViewById(R.id.spin_kit);
            bodyLayout = itemView.findViewById(R.id.rl_body);
            spinKitView.setVisibility(View.VISIBLE);
            bodyLayout.setVisibility(View.GONE);

            tvQuestion = itemView.findViewById(R.id.tv_question);
            tvQuestionDesc = itemView.findViewById(R.id.tv_question_desc);
            tvQuestionCounter = itemView.findViewById(R.id.tv_question_counter);

            submitButton.setOnClickListener(view -> {
                if (mItemList.get(index).isSelected()) {
                    AdapterUtils.setToDisable(skipButton);
                    AdapterUtils.buttonProgressAnimation(mContext, submitButton, true, () -> {
                        //if (mIsEditMode) {
                        int targetIndex = index + 1;
                        if (targetIndex < mItemList.size()) {
                            List<Node> optionList = mItemList.get(targetIndex).getOptionsList();
                            for (int i = 0; i < optionList.size(); i++) {
                                if (optionList.get(i).isFoundCompareAttribute()) {
                                    notifyItemChanged(targetIndex);
                                    break;
                                }
                            }
                            // }
                        }
                        mItemList.get(index).setDataCaptured(true);
                        mOnItemSelection.onSelect(node, index, false, null);
                    });
                } else
                    Toast.makeText(mContext, mContext.getString(R.string.select_at_least_one_option), Toast.LENGTH_SHORT).show();
            });
            /*nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemList.get(index).isSelected())
                        mOnItemSelection.onSelect(node, index, false, null);
                    else
                        Toast.makeText(mContext, mContext.getString(R.string.select_at_least_one_option), Toast.LENGTH_SHORT).show();
                }
            });*/

            skipButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mItemList.get(index).setSelected(false);
                    mItemList.get(index).setDataCaptured(false);
                    if (mItemList.get(index).getOptionsList() != null && mItemList.get(index).getOptionsList().size() > 0)
                        for (int i = 0; i < mItemList.get(index).getOptionsList().size(); i++) {
                            mItemList.get(index).getOptionsList().get(i).setSelected(false);
                            mItemList.get(index).getOptionsList().get(i).setDataCaptured(false);
                        }
                    mItemList.get(index).setSkipped(true);
                    AdapterUtils.setToDisable(submitButton);
                    AdapterUtils.buttonProgressAnimation(mContext, skipButton, false, new AdapterUtils.OnFinishActionListener() {
                        @Override
                        public void onFinish() {
                            notifyItemChanged(index);
                            mOnItemSelection.onSelect(node, index, true, null);
                        }
                    });


                }
            });

            knowMoreTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showKnowMoreDialog(node.findDisplay(), node.findPopup());
                }
            });
        }


    }

    private void showKnowMoreDialog(String title, String message) {
        DialogUtils dialogUtils = new DialogUtils();
        dialogUtils.showCommonDialog(mContext, 0, title, message, true, mContext.getResources().getString(R.string.okay), mContext.getResources().getString(R.string.cancel), new DialogUtils.CustomDialogListener() {
            @Override
            public void onDialogActionDone(int action) {

            }
        });
    }


}

