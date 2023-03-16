package org.intelehealth.app.ayu.visit.common.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.RangeSlider;
import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.ayu.visit.reason.adapter.OptionsChipsGridAdapter;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.knowledgeEngine.PhysicalExam;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class QuestionsListingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    private Context mContext;
    private List<Node> mItemList = new ArrayList<Node>();
    private int mTotalQuery = 0;
    RecyclerView mRecyclerView;
    private int mLastImageCaptureSelectedNodeIndex = 0;

    public void addImageInLastNode(String image) {
        mItemList.get(mLastImageCaptureSelectedNodeIndex).getImagePathList().add(image);
        Log.v("ImageCaptured", new Gson().toJson(mItemList.get(mLastImageCaptureSelectedNodeIndex)));
        notifyItemChanged(mLastImageCaptureSelectedNodeIndex);
    }

    public void removeImageInLastNode(int index, String image) {
        mItemList.get(mLastImageCaptureSelectedNodeIndex).getImagePathList().remove(index);
        notifyItemChanged(mLastImageCaptureSelectedNodeIndex);
    }

    public interface OnItemSelection {
        void onSelect(Node node, int index);

        void needTitleChange(String title);

        void onAllAnswered(boolean isAllAnswered);

        void onCameraRequest();

        void onImageRemoved(int index, String image);
    }

    private OnItemSelection mOnItemSelection;
    private boolean mIsForPhysicalExam;
    private PhysicalExam mPhysicalExam;

    public QuestionsListingAdapter(RecyclerView recyclerView, Context context, boolean isPhyExam, PhysicalExam physicalExam, int totalQuery, OnItemSelection onItemSelection) {
        mContext = context;
        mIsForPhysicalExam = isPhyExam;
        mPhysicalExam = physicalExam;
        mRecyclerView = recyclerView;
        mOnItemSelection = onItemSelection;
        mTotalQuery = totalQuery;
        //mAnimator = new RecyclerViewAnimator(recyclerView);
    }

    private JSONObject mThisScreenLanguageJsonObject = new JSONObject();

    public void addItem(Node node) {
        mItemList.add(node);
        notifyItemInserted(mItemList.size() - 1);
    }

    public void addItemAll(List<Node> nodes) {
        mItemList = nodes;
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

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (holder instanceof GenericViewHolder) {
            GenericViewHolder genericViewHolder = (GenericViewHolder) holder;
            genericViewHolder.node = mItemList.get(position);
            genericViewHolder.index = position;

            genericViewHolder.otherContainerLinearLayout.removeAllViews();
            genericViewHolder.singleComponentContainer.removeAllViews();
            genericViewHolder.singleComponentContainer.setVisibility(View.GONE);
            genericViewHolder.recyclerView.setVisibility(View.GONE);

            if (mIsForPhysicalExam) {

                Node _mNode = mPhysicalExam.getExamNode(position).getOption(0);
                final String parent_name = mPhysicalExam.getExamParentNodeName(position);
                String nodeText = parent_name + " : " + _mNode.findDisplay();

                genericViewHolder.tvQuestion.setText(nodeText);
                genericViewHolder.tvQuestionCounter.setText((position + 1) + " of " + mPhysicalExam.getTotalNumberOfExams() + " questions"); //"1 of 10 questions"

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
                genericViewHolder.tvQuestionCounter.setText((position + 1) + " of " + mTotalQuery + " questions"); //"1 of 10 questions"

            }


            if (genericViewHolder.node.getText().equalsIgnoreCase("Associated symptoms")) {
                mOnItemSelection.needTitleChange("2/4 Visit reason : Associated symptoms");
                showAssociateSymptoms(genericViewHolder.node, genericViewHolder, position);
            } else {
                mOnItemSelection.needTitleChange("");


                String type = genericViewHolder.node.getInputType();
                Log.v("Node", "Type - " + type);
                Log.v("Node", "Node - " + new Gson().toJson(genericViewHolder.node));
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
                        break;
                    case "number":
                        // askNumber(questionNode, context, adapter);
                        addNumberView(mItemList.get(position), genericViewHolder, position);
                        break;
                    case "area":
                        // askArea(questionNode, context, adapter);
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
                        addNumberView(mItemList.get(position), genericViewHolder, position);
                        break;
                    case "camera":
                        // openCamera(context, imagePath, imageName);
                        Log.v("showCameraView", "onBindViewHolder 2");
                        showCameraView(mItemList.get(position), genericViewHolder, position);
                        break;

                    case "options":
                        // openCamera(context, imagePath, imageName);
                        //if (mIsForPhysicalExam)
                        //    showOptionsData(genericViewHolder, mPhysicalExam.getExamNode(position).getOption(0).getOptionsList(), position);
                        //else
                        showOptionsData(genericViewHolder, mItemList.get(position).getOptionsList(), position);
                        break;
                }
            }

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    genericViewHolder.spinKitView.setVisibility(View.GONE);
                    genericViewHolder.bodyLayout.setVisibility(View.VISIBLE);
                    //mRecyclerView.scrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1);
                }
            }, 1000);

            if (!mItemList.get(position).getImagePathList().isEmpty()) {
                Log.v("showCameraView", "onBindViewHolder 1");
                showCameraView(mItemList.get(position), genericViewHolder, position);
            }
        }
    }

    private void addRangeView(Node node, GenericViewHolder holder, int index) {
        holder.singleComponentContainer.removeAllViews();
        View view = View.inflate(mContext, R.layout.ui2_visit_number_range, null);
        RangeSlider rangeSlider = view.findViewById(R.id.range_slider);
        //rangeSlider.setLabelBehavior(LABEL_ALWAYS_VISIBLE); //Label always visible" nothing yet ?
        TextView rangeTextView = view.findViewById(R.id.btn_values);
        TextView submitTextView = view.findViewById(R.id.btn_submit);
        submitTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(rangeTextView.getText().toString().equalsIgnoreCase("---")){
                    Toast.makeText(mContext, "Please select the range!", Toast.LENGTH_SHORT).show();
                }else{
                    List<Float> values = rangeSlider.getValues();
                    int x = values.get(0).intValue();
                    int y = values.get(1).intValue();
                    String durationString = x + " to " + y;
                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", durationString));
                    } else {
                        node.addLanguage(" " + durationString);
                        node.setText(durationString);
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }
                    node.setSelected(true);
                    notifyItemChanged(index);
                    mOnItemSelection.onSelect(node, index);
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

            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onStopTrackingTouch(@NonNull RangeSlider slider) {
                List<Float> values = rangeSlider.getValues();
                int x = values.get(0).intValue();
                int y = values.get(1).intValue();
                rangeTextView.setText(String.format("%d to %d", x, y));
            }
        });


        holder.singleComponentContainer.addView(view);
    }

    private void showAssociateSymptoms(Node node, GenericViewHolder holder, int position) {
        holder.singleComponentContainer.setVisibility(View.VISIBLE);
        holder.tvQuestionDesc.setVisibility(View.VISIBLE);
        holder.recyclerView.setVisibility(View.GONE);
        holder.tvQuestionDesc.setText("Select yes or no");

        View view = View.inflate(mContext, R.layout.associate_symptoms_questionar_main_view, null);
        Button submitButton = view.findViewById(R.id.btn_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemSelection.onAllAnswered(true);

            }
        });
        RecyclerView recyclerView = view.findViewById(R.id.rcv_container);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        AssociateSymptomsQueryAdapter associateSymptomsQueryAdapter = new AssociateSymptomsQueryAdapter(recyclerView, mContext, node.getOptionsList(), new AssociateSymptomsQueryAdapter.OnItemSelection() {
            @Override
            public void onSelect(Node data) {
                Log.v("data", new Gson().toJson(data));
                mItemList.get(position).setSelected(false);
                for (int i = 0; i < node.getOptionsList().size(); i++) {
                    if (node.getOptionsList().get(i).isSelected() || node.getOptionsList().get(i).isNoSelected()) {
                        mItemList.get(position).setSelected(true);

                    }
                }
            }
        });
        recyclerView.setAdapter(associateSymptomsQueryAdapter);
        holder.singleComponentContainer.addView(view);
        mRecyclerView.scrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1);
    }


    private void showOptionsData(final GenericViewHolder holder, List<Node> options, int index) {
        if (options.size() == 1 && (options.get(0).getOptionsList() == null || options.get(0).getOptionsList().isEmpty())) {
            // it seems that inside the options only one view and its simple component like text,date, number, area, duration, range, frequency, camera, etc
            // we we have add same in linear layout dynamically instead of adding in to recyclerView
            holder.singleComponentContainer.setVisibility(View.VISIBLE);
            holder.tvQuestionDesc.setVisibility(View.GONE);
            Node node = options.get(0);
            String type = node.getInputType();

            if (node.getOptionsList() != null && !node.getOptionsList().isEmpty()) {
                type = "options";
            }
            Log.v("Node", "Type - " + type);
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
                    break;
                case "number":
                    // askNumber(questionNode, context, adapter);
                    addNumberView(options.get(0), holder, index);
                    break;
                case "area":
                    // askArea(questionNode, context, adapter);
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
                    addNumberView(options.get(0), holder, index);
                    break;
                case "camera":
                    // openCamera(context, imagePath, imageName);
                    Log.v("showCameraView", "showOptionsData 1");
                    showCameraView(options.get(0), holder, index);
                    break;

                case "options":
                    // openCamera(context, imagePath, imageName);
                    //showOptionsData(genericViewHolder, genericViewHolder.node.getOptionsList());
                    break;
            }
            holder.submitButton.setVisibility(View.GONE);
        } else {
            holder.tvQuestionDesc.setVisibility(View.VISIBLE);
            holder.recyclerView.setVisibility(View.VISIBLE);

            if (mItemList.get(index).isMultiChoice()) {
                holder.tvQuestionDesc.setText(mContext.getString(R.string.select_one_or_more));
                holder.submitButton.setVisibility(View.VISIBLE);
            } else {
                holder.tvQuestionDesc.setText(mContext.getString(R.string.select_any_one));
                holder.submitButton.setVisibility(View.GONE);

            }
            //holder.recyclerView.setLayoutManager(new GridLayoutManager(mContext, options.size() == 1 ? 1 : 2));
            FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(mContext);
            layoutManager.setFlexDirection(FlexDirection.ROW);
            layoutManager.setJustifyContent(JustifyContent.FLEX_START);
            holder.recyclerView.setLayoutManager(layoutManager);
            OptionsChipsGridAdapter optionsChipsGridAdapter = new OptionsChipsGridAdapter(holder.recyclerView, mContext, mItemList.get(index), options, new OptionsChipsGridAdapter.OnItemSelection() {
                @Override
                public void onSelect(Node node) {
                    mItemList.get(index).setSelected(false);
                    for (int i = 0; i < options.size(); i++) {
                        if (options.get(i).isSelected()) {
                            mItemList.get(index).setSelected(true);
                        }
                    }
                    //Toast.makeText(mContext, "Selected : " + data, Toast.LENGTH_SHORT).show();
                    String type = node.getInputType();

                    if (type == null || type.isEmpty() && (node.getOptionsList() != null && !node.getOptionsList().isEmpty())) {
                        type = "options";
                    }
                    if (!type.isEmpty()) {
                        holder.singleComponentContainer.setVisibility(View.VISIBLE);
                    } else {
                        if (!mItemList.get(index).isMultiChoice()) {
                            mOnItemSelection.onSelect(node, index);
                        }
                    }
                    Log.v("Node", "Type - " + type);
                    switch (type) {
                        case "text":
                            // askText(questionNode, context, adapter);
                            addTextEnterView(node, holder, index);
                            break;
                        case "date":
                            //askDate(questionNode, context, adapter);
                            addDateView(node, holder, index);
                            break;
                        case "location":
                            //askLocation(questionNode, context, adapter);
                            break;
                        case "number":
                            // askNumber(questionNode, context, adapter);
                            addNumberView(node, holder, index);
                            break;
                        case "area":
                            // askArea(questionNode, context, adapter);
                            break;
                        case "duration":
                            // askDuration(questionNode, context, adapter);
                            addDurationView(node, holder, index);
                            break;
                        case "range":
                            // askRange(questionNode, context, adapter);
                            addRangeView(node, holder, index);
                            break;
                        case "frequency":
                            //askFrequency(questionNode, context, adapter);
                            addNumberView(node, holder, index);
                            break;
                        case "camera":
                            // openCamera(context, imagePath, imageName);
                            Log.v("showCameraView", "showOptionsData 2");
                            showCameraView(node, holder, index);
                            break;

                        case "options":
                            // openCamera(context, imagePath, imageName);
                            showOptionsData(holder, node.getOptionsList(), index);
                            break;
                    }
                    //notifyDataSetChanged();
                }
            });
            holder.recyclerView.setAdapter(optionsChipsGridAdapter);
            for (int i = 0; i < options.size(); i++) {
                String type = options.get(i).getInputType();
                if (type.equalsIgnoreCase("camera") && options.get(i).isSelected()) {
                    // openCamera(context, imagePath, imageName);
                    showCameraView(options.get(i), holder, index);
                }
            }
        }

    }

    private void showCameraView(Node node, GenericViewHolder holder, int index) {
        Log.v("showCameraView", new Gson().toJson(node));
        Log.v("showCameraView", "ImagePathList - " + new Gson().toJson(node.getImagePathList()));
        holder.otherContainerLinearLayout.removeAllViews();
        View view = View.inflate(mContext, R.layout.ui2_visit_image_capture_view, null);
        Button submitButton = view.findViewById(R.id.btn_submit);
        LinearLayout newImageCaptureLinearLayout = view.findViewById(R.id.ll_emptyView);
        //newImageCaptureLinearLayout.setVisibility(View.VISIBLE);
        newImageCaptureLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //openCamera(getImagePath(), "");
                mLastImageCaptureSelectedNodeIndex = index;
                mOnItemSelection.onCameraRequest();
            }
        });
        view.findViewById(R.id.btn_1st_capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //openCamera(getImagePath(), "");
                mLastImageCaptureSelectedNodeIndex = index;
                mOnItemSelection.onCameraRequest();
            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mOnItemSelection.onSelect(node, index);
            }
        });

        RecyclerView imagesRcv = view.findViewById(R.id.rcv_added_image);
        imagesRcv.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));

        ImageGridAdapter imageGridAdapter = new ImageGridAdapter(imagesRcv, mContext, node.getImagePathList(), new ImageGridAdapter.OnImageAction() {
            @Override
            public void onImageRemoved(int index, String image) {
                mOnItemSelection.onImageRemoved(index, image);
            }

            @Override
            public void onNewImageRequest() {
                mLastImageCaptureSelectedNodeIndex = index;
                mOnItemSelection.onCameraRequest();
            }
        });
        imagesRcv.setAdapter(imageGridAdapter);
        Log.v("showCameraView", "ImagePathList recyclerView - " + imagesRcv.getAdapter().getItemCount());


        if (node.getImagePathList().isEmpty()) {
            newImageCaptureLinearLayout.setVisibility(View.VISIBLE);
            submitButton.setVisibility(View.GONE);
            imagesRcv.setVisibility(View.GONE);
        } else {
            newImageCaptureLinearLayout.setVisibility(View.GONE);
            submitButton.setVisibility(View.VISIBLE);
            imagesRcv.setVisibility(View.VISIBLE);
        }

        holder.otherContainerLinearLayout.addView(view);

    }


    /**
     * Time duration
     *
     * @param node
     * @param holder
     * @param index
     */
    private void addDurationView(Node node, GenericViewHolder holder, int index) {
        Log.v("addDurationView", new Gson().toJson(node));
        holder.singleComponentContainer.removeAllViews();
        View view = View.inflate(mContext, R.layout.ui2_visit_reason_time_range, null);
        final TextView numberRangeTextView = view.findViewById(R.id.tv_number_range);
        final TextView durationTypeTextView = view.findViewById(R.id.tv_duration_type);
        Button submitButton = view.findViewById(R.id.btn_submit);
        if (!node.getLanguage().isEmpty()) {
            String[] val = node.getLanguage().trim().split(" ");
            if (val.length == 2) {
                numberRangeTextView.setText(val[0]);
                durationTypeTextView.setText(val[1]);
            }
        }

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (numberRangeTextView.getText().toString().isEmpty()) {
                    Toast.makeText(mContext, "Please select the duration number", Toast.LENGTH_SHORT).show();
                    return;
                } else if (durationTypeTextView.getText().toString().isEmpty()) {
                    Toast.makeText(mContext, "Please select the duration type", Toast.LENGTH_SHORT).show();
                    return;
                }
                String durationString = numberRangeTextView.getText() + " " + durationTypeTextView.getText();

                if (node.getLanguage().contains("_")) {
                    node.setLanguage(node.getLanguage().replace("_", durationString));
                } else {
                    node.addLanguage(" " + durationString);
                    node.setText(durationString);
                    //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                }
                node.setSelected(true);
                notifyDataSetChanged();
                mOnItemSelection.onSelect(node, index);
            }
        });
        numberRangeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNumberListing(numberRangeTextView, "Select duration number", 0, 100);
            }
        });
        durationTypeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDurationTypes(durationTypeTextView);
            }
        });

        holder.singleComponentContainer.addView(view);
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
        builder.setTitle("Select Duration Type");

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
        View view = View.inflate(mContext, R.layout.visit_reason_input_text, null);
        Button submitButton = view.findViewById(R.id.btn_submit);
        final EditText editText = view.findViewById(R.id.actv_reasons);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText().toString().trim().isEmpty()) {
                    Toast.makeText(mContext, "Please enter the value", Toast.LENGTH_SHORT).show();
                } else {
                    if (!editText.getText().toString().equalsIgnoreCase("")) {
                        if (node.getLanguage().contains("_")) {
                            node.setLanguage(node.getLanguage().replace("_", editText.getText().toString()));
                        } else {
                            node.addLanguage(editText.getText().toString());
                            //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                        }
                        node.setSelected(true);
                    } else {
                        //if (node.isRequired()) {
                        node.setSelected(false);
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
                    mOnItemSelection.onSelect(node, index);
                }
            }
        });

        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setHint(node.getText());
        holder.singleComponentContainer.addView(view);
    }

    private void addTextEnterView(Node node, GenericViewHolder holder, int index) {
        holder.singleComponentContainer.removeAllViews();
        View view = View.inflate(mContext, R.layout.visit_reason_input_text, null);
        Button submitButton = view.findViewById(R.id.btn_submit);
        final EditText editText = view.findViewById(R.id.actv_reasons);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText().toString().trim().isEmpty()) {
                    Toast.makeText(mContext, "Please enter the value", Toast.LENGTH_SHORT).show();
                } else {
                    if (!editText.getText().toString().equalsIgnoreCase("")) {
                        if (node.getLanguage().contains("_")) {
                            node.setLanguage(node.getLanguage().replace("_", editText.getText().toString()));
                        } else {
                            node.addLanguage(editText.getText().toString());
                            //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                        }
                        node.setSelected(true);
                    } else {
                        //if (node.isRequired()) {
                        node.setSelected(false);
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
                    mOnItemSelection.onSelect(node, index);
                }
            }
        });

        editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setMinLines(5);
        editText.setLines(5);
        editText.setHorizontallyScrolling(false);
        editText.setHint(node.getText());
        editText.setMinHeight(320);
        holder.singleComponentContainer.addView(view);
    }

    private void addDateView(Node node, GenericViewHolder holder, int index) {
        holder.singleComponentContainer.removeAllViews();
        View view = View.inflate(mContext, R.layout.visit_reason_date, null);
        final Button submitButton = view.findViewById(R.id.btn_submit);
        final CalendarView calendarView = view.findViewById(R.id.cav_date);
        calendarView.setMaxDate(System.currentTimeMillis() + 1000);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                // display the selected date by using a toast
                submitButton.setText(dayOfMonth + "-" + (month + 1) + "-" + year);
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String d = submitButton.getText().toString().trim();
                if (!d.contains("-")) {
                    Toast.makeText(mContext, "Please select the date", Toast.LENGTH_SHORT).show();
                } else {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(0);
                    cal.set(Integer.parseInt(d.split("-")[2]), Integer.parseInt(d.split("-")[1]) - 1, Integer.parseInt(d.split("-")[0]));
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
                                node.setLanguage(node.getLanguage().replace("_", "Question not answered"));
                            } else {
                                node.addLanguage("Question not answered");
                                //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                            }
                        }
                    }
                    mOnItemSelection.onSelect(node, index);
                }
            }
        });

        holder.singleComponentContainer.addView(view);
    }


    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    private class GenericViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion, tvQuestionDesc, tvQuestionCounter, tvReferenceDesc;
        Node node;
        int index;
        RecyclerView recyclerView;
        // this will contain independent view like, edittext, date, time, range, etc
        LinearLayout singleComponentContainer, referenceContainerLinearLayout, otherContainerLinearLayout;
        SpinKitView spinKitView;
        LinearLayout bodyLayout;
        Button submitButton;


        GenericViewHolder(View itemView) {
            super(itemView);
            submitButton = itemView.findViewById(R.id.btn_submit);
            recyclerView = itemView.findViewById(R.id.rcv_container);
            singleComponentContainer = itemView.findViewById(R.id.ll_single_component_container);
            referenceContainerLinearLayout = itemView.findViewById(R.id.ll_reference_container);
            otherContainerLinearLayout = itemView.findViewById(R.id.ll_others_container);
            tvReferenceDesc = itemView.findViewById(R.id.tv_reference_desc);
            spinKitView = itemView.findViewById(R.id.spin_kit);
            bodyLayout = itemView.findViewById(R.id.rl_body);
            spinKitView.setVisibility(View.VISIBLE);
            bodyLayout.setVisibility(View.GONE);

            tvQuestion = itemView.findViewById(R.id.tv_question);
            tvQuestionDesc = itemView.findViewById(R.id.tv_question_desc);
            tvQuestionCounter = itemView.findViewById(R.id.tv_question_counter);

            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemList.get(index).isSelected())
                        mOnItemSelection.onSelect(node, index);
                    else
                        Toast.makeText(mContext, "Please select at least one option!", Toast.LENGTH_SHORT).show();
                }
            });
        }


    }


}

