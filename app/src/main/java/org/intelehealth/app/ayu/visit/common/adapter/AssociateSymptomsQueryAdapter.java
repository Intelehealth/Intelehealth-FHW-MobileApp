package org.intelehealth.app.ayu.visit.common.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.ayu.visit.common.OnItemSelection;
import org.intelehealth.app.ayu.visit.common.VisitUtils;
import org.intelehealth.app.ayu.visit.model.ComplainBasicInfo;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.shared.FirstLetterUpperCaseInputFilter;
import org.intelehealth.app.utilities.CustomLog;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class AssociateSymptomsQueryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = "AssociateSymptomsQueryAdapter";
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    private Context mContext;
    private List<Node> mItemList = new ArrayList<Node>();
    private RecyclerView mRootRecyclerView, mRecyclerView;
    private boolean mIsEditMode;
    private String engineVersion;

    public String getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }


    public interface AssociateSymptomsOnItemSelection {
        public void onSelect(Node data);
    }

    private AssociateSymptomsOnItemSelection mOnItemSelection;

    public AssociateSymptomsQueryAdapter(Context context, RecyclerView rootRecyclerView, RecyclerView recyclerView, List<Node> itemList, boolean editMode, AssociateSymptomsOnItemSelection onItemSelection) {
        mContext = context;
        mItemList = itemList;
        mOnItemSelection = onItemSelection;
        mRootRecyclerView = rootRecyclerView;
        mRecyclerView = recyclerView;
        mIsEditMode = editMode;
        //mAnimator = new RecyclerViewAnimator(recyclerView);
    }

    private JSONObject mThisScreenLanguageJsonObject = new JSONObject();

    public void setLabelJSON(JSONObject json) {
        mThisScreenLanguageJsonObject = json;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ui2_associate_symptoms_list_item, parent, false);
        /**
         * First item's entrance animations.
         */
        //mAnimator.onCreateViewHolder(itemView);

        return new GenericViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int rawPosition) {
        if (holder instanceof GenericViewHolder) {
            GenericViewHolder genericViewHolder = (GenericViewHolder) holder;
            int position = genericViewHolder.getAbsoluteAdapterPosition();
            genericViewHolder.node = mItemList.get(position);
            genericViewHolder.index = position;

            genericViewHolder.questionTextView.setText((position + 1) + ". " + genericViewHolder.node.findDisplay());
            //genericViewHolder.recyclerView.setAdapter(reasonChipsGridAdapter);
            genericViewHolder.noTextView.setSelected(mItemList.get(position).isNoSelected());
//            if (mItemList.get(position).isNoSelected()) {
//                genericViewHolder.noTextView.setTextColor(ContextCompat.getColor(mContext,R.color.white));
//                genericViewHolder.noTextView.setBackgroundResource(R.drawable.ui2_common_button_bg_submit);
//                genericViewHolder.noTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_close_18_white, 0, 0, 0);
//            } else {
//                genericViewHolder.noTextView.setTextColor(ContextCompat.getColor(mContext,R.color.ui2_black_text_color));
//                genericViewHolder.noTextView.setBackgroundResource(R.drawable.normal_white_reounded_bg);
//                genericViewHolder.noTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_close_18_green, 0, 0, 0);
//            }
            genericViewHolder.currentRootOptionList = mItemList.get(position).getOptionsList();
            genericViewHolder.yesTextView.setSelected(mItemList.get(position).isSelected());
            if (mItemList.get(position).isSelected()) {
//                genericViewHolder.yesTextView.setTextColor(ContextCompat.getColor(mContext,R.color.white));
//                genericViewHolder.yesTextView.setBackgroundResource(R.drawable.ui2_common_button_bg_submit);
//                genericViewHolder.yesTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_18_white, 0, 0, 0);

                String type = genericViewHolder.node.getInputType();
                CustomLog.v(TAG, "onBindViewHolder Type - " + type);
                //CustomLog.v(TAG, "onBindViewHolder Node - " + new Gson().toJson(genericViewHolder.node));
                if (type == null || type.isEmpty() && (genericViewHolder.node.getOptionsList() != null && !genericViewHolder.node.getOptionsList().isEmpty())) {
                    type = "options";
                }
                CustomLog.v(TAG, "type - " + type);
                if (type.equalsIgnoreCase("text")) {
                    genericViewHolder.singleComponentContainer.removeAllViews();
                    genericViewHolder.singleComponentContainer.setVisibility(View.VISIBLE);
                    addTextEnterView(mItemList.get(position), genericViewHolder, position);
                } else if (type.equalsIgnoreCase("options") && mItemList.get(position).getOptionsList().size() == 1 && mItemList.get(position).getOptionsList().get(0).getOptionsList() == null) {
                    String _type = mItemList.get(position).getOptionsList().get(0).getInputType();
                    CustomLog.v(TAG, "Single option - " + type);
                    if (_type.equalsIgnoreCase("text")) {
                        genericViewHolder.singleComponentContainer.removeAllViews();
                        genericViewHolder.singleComponentContainer.setVisibility(View.VISIBLE);
                        addTextEnterView(mItemList.get(position).getOptionsList().get(0), genericViewHolder, position);
                    } else if (_type.equalsIgnoreCase("date")) {
                        genericViewHolder.singleComponentContainer.removeAllViews();
                        genericViewHolder.singleComponentContainer.setVisibility(View.VISIBLE);
                        addDateView(mItemList.get(position).getOptionsList().get(0), genericViewHolder, position);
                    }
                } else {
                    genericViewHolder.singleComponentContainer.removeAllViews();
                    genericViewHolder.singleComponentContainer.setVisibility(View.GONE);
                    CustomLog.v(TAG, "onBindViewHolder options" + new Gson().toJson(mItemList.get(position).getOptionsList()));
                    if (mItemList.get(position).getOptionsList() != null && mItemList.get(position).getOptionsList().size() > 0) {
                        genericViewHolder.recyclerView.setVisibility(View.VISIBLE);
                        genericViewHolder.recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
                        genericViewHolder.isHavingDirectOption = !mItemList.get(position).isHavingMoreNestedQuestion();
                        HashMap<Integer, ComplainBasicInfo> rootComplainBasicInfoHashMap = new HashMap<>();
                        ComplainBasicInfo complainBasicInfo = new ComplainBasicInfo();
                        if (genericViewHolder.isHavingDirectOption)
                            complainBasicInfo.setOptionSize(1);
                        else
                            complainBasicInfo.setOptionSize(mItemList.get(position).getOptionsList().size());

                       /* if (!mItemList.get(position).isContainsTheQuestionBeforeOptions()) {
                            complainBasicInfo.setOptionSize(1);
                        }*/
                        rootComplainBasicInfoHashMap.put(0, complainBasicInfo);

                        genericViewHolder.questionsListingAdapter = new QuestionsListingAdapter(genericViewHolder.recyclerView, mContext, true, false, null, 0, rootComplainBasicInfoHashMap, mIsEditMode, new OnItemSelection() {
                            @Override
                            public void onSelect(Node node, int index, boolean isSkipped, Node parentNode) {

                                CustomLog.v(TAG, "currentComplainNodeOptionsIndex - " + genericViewHolder.currentComplainNodeOptionsIndex);
                                CustomLog.v(TAG, "mItemList.get(position).getOptionsList().size() - " + mItemList.get(position).getOptionsList().size());
                                CustomLog.v(TAG, "index - " + index);
                                CustomLog.v(TAG, "Node - " + new Gson().toJson(node));
                                if (genericViewHolder.isHavingDirectOption)
                                    return;
                                if (genericViewHolder.currentComplainNodeOptionsIndex - index >= 1) {
                                    return;
                                }
                                if (isSkipped) {
                                    genericViewHolder.questionsListingAdapter.geItems().get(index).setSelected(false);
                                    genericViewHolder.questionsListingAdapter.geItems().get(index).setDataCaptured(false);
                                    genericViewHolder.questionsListingAdapter.notifyItemChanged(index);
                                }
                                //if (mItemList.get(position).isContainsTheQuestionBeforeOptions()) {
                                //CustomLog.v("onSelect", "node - " + node.getText());
                                if (genericViewHolder.currentComplainNodeOptionsIndex < mItemList.get(position).getOptionsList().size() - 1) {
                                    genericViewHolder.currentComplainNodeOptionsIndex++;
                                    //genericViewHolder.questionsListingAdapter.addItem(mItemList.get(position).getOptionsList().get(genericViewHolder.currentComplainNodeOptionsIndex));
                                    if (genericViewHolder.isHavingDirectOption)
                                        genericViewHolder.questionsListingAdapter.addItem(mItemList.get(position), getEngineVersion());
                                    else
                                        genericViewHolder.questionsListingAdapter.addItem(mItemList.get(position).getOptionsList().get(genericViewHolder.currentComplainNodeOptionsIndex), getEngineVersion());
                                } /*else {
                                    genericViewHolder.currentComplainNodeOptionsIndex = 0;

                                }*/
                                //}
                                VisitUtils.scrollNow(mRootRecyclerView, 1000, 0, 600, mIsEditMode, false);
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
                            public void onImageRemoved(int nodeIndex, int imageIndex, String imagee) {

                            }

                            @Override
                            public void onTerminalNodeAnsweredForParentUpdate(String parentNodeId) {

                            }
                        });
                        genericViewHolder.questionsListingAdapter.setAssociateSymptomNestedQueryFlag(true);
                        genericViewHolder.recyclerView.setAdapter(genericViewHolder.questionsListingAdapter);
                        //for (int i = 0; i <genericViewHolder.currentRootOptionList.size(); i++) {
                        // genericViewHolder.questionsListingAdapter.addItem(mItemList.get(position).getOptionsList().get(i));
//                        if (!mItemList.get(position).isContainsTheQuestionBeforeOptions()) {
//                            genericViewHolder.questionsListingAdapter.addItem(mItemList.get(position), getEngineVersion());
//                        } else {
                        if (genericViewHolder.isHavingDirectOption)
                            genericViewHolder.questionsListingAdapter.addItem(mItemList.get(position), getEngineVersion());
                        else
                            genericViewHolder.questionsListingAdapter.addItem(mItemList.get(position).getOptionsList().get(genericViewHolder.currentComplainNodeOptionsIndex), getEngineVersion());
                        //}
                        //}
                    } else {
                        genericViewHolder.recyclerView.setVisibility(View.GONE);
                    }
                }


            } else {
                genericViewHolder.singleComponentContainer.removeAllViews();
                genericViewHolder.singleComponentContainer.setVisibility(View.GONE);
//                genericViewHolder.yesTextView.setTextColor(ContextCompat.getColor(mContext,R.color.ui2_black_text_color));
//                genericViewHolder.yesTextView.setBackgroundResource(R.drawable.normal_white_reounded_bg);
//                genericViewHolder.yesTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_18_green, 0, 0, 0);
                genericViewHolder.recyclerView.setVisibility(View.GONE);
            }

        }
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    private class GenericViewHolder extends RecyclerView.ViewHolder {
        TextView questionTextView, yesTextView, noTextView;
        Node node;
        int index;
        boolean isHavingDirectOption = false;
        RecyclerView recyclerView;
        QuestionsListingAdapter questionsListingAdapter;
        int currentComplainNodeOptionsIndex = 0;
        List<Node> currentRootOptionList = new ArrayList<>();
        LinearLayout singleComponentContainer;

        GenericViewHolder(View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.rcv_container);
            //recyclerView.setLayoutManager(new GridLayoutManager(mContext, 2));
            singleComponentContainer = itemView.findViewById(R.id.ll_single_component_container);
            questionTextView = itemView.findViewById(R.id.tv_question);
            yesTextView = itemView.findViewById(R.id.tv_yes);
            noTextView = itemView.findViewById(R.id.tv_no);
            yesTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mItemList.get(index).setNoSelected(false);

                    mItemList.get(index).setSelected(true);

                    if (mItemList.get(index).getAssociated_symptoms() == 1) {
                        mItemList.get(index).setAssociated_symptoms(0);
                    } else {
                        mItemList.get(index).setAssociated_symptoms(1);
                    }
                    notifyItemChanged(index);
                    mOnItemSelection.onSelect(mItemList.get(index));
                }

            });

            noTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //mItemList.get(index).setSelected(true);
                    unselectNested(mItemList.get(index));
                    mItemList.get(index).setNoSelected(!mItemList.get(index).isNoSelected());
                    mItemList.get(index).setUnselected();


                    if (mItemList.get(index).getAssociated_symptoms() == -1) {
                        mItemList.get(index).setAssociated_symptoms(0);
                    } else {
                        mItemList.get(index).setAssociated_symptoms(-1);
                    }
                    notifyItemChanged(index);
                    mOnItemSelection.onSelect(mItemList.get(index));
                }
            });

        }


    }

    private void unselectNested(Node node) {
        node.unselectAllNestedNode();
    }

    private void addTextEnterView(Node node, GenericViewHolder holder, int index) {

        View view = View.inflate(mContext, R.layout.visit_reason_input_text, null);
        Button submitButton = view.findViewById(R.id.btn_submit);
        //submitButton.setVisibility(View.GONE);
        submitButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, node.isDataCaptured() ? R.drawable.ic_baseline_check_18_white : 0, 0);
        submitButton.setBackgroundResource(node.isDataCaptured() ? R.drawable.ui2_common_primary_bg : R.drawable.ui2_common_button_bg_submit);

        Button skipButton = view.findViewById(R.id.btn_skip);

        if (node.isDataCaptured()) {
            AdapterUtils.setToDisable(skipButton);
        } else {
            AdapterUtils.setToDefault(skipButton);
        }

        if (node.isRequired()) skipButton.setVisibility(View.VISIBLE);
        else skipButton.setVisibility(View.GONE);
        final EditText editText = view.findViewById(R.id.actv_reasons);
        if (node.isSelected() && node.getLanguage() != null && node.isDataCaptured()) {
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
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                node.setSelected(false);
                holder.node.setSelected(false);
                holder.node.setDataCaptured(false);
                // scroll little bit
                VisitUtils.scrollNow(mRootRecyclerView, 1000, 0, 300, mIsEditMode, false);
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

                       /* if (node.getLanguage().contains("_")) {
                            node.setLanguage(node.getLanguage().replace("_", editText.getText().toString()));
                        } else {
                            node.addLanguage(node.getText().replace("[Describe]", "") + " : " + editText.getText().toString());
                            //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                        }*/
                        node.setSelected(true);
                        node.setDataCaptured(true);

                        holder.node.setSelected(true);
                        holder.node.setDataCaptured(true);

                    } else {
                        //if (node.isRequired()) {
                        node.setSelected(false);
                        node.setDataCaptured(false);

                        holder.node.setSelected(false);
                        holder.node.setDataCaptured(false);
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
                    AdapterUtils.setToDisable(skipButton);
                    AdapterUtils.buttonProgressAnimation(mContext, submitButton, true, new AdapterUtils.OnFinishActionListener() {
                        @Override
                        public void onFinish() {

                        }
                    });
                    // scroll little bit
                    VisitUtils.scrollNow(mRootRecyclerView, 1000, 0, 300, mIsEditMode, false);
                }


            }
        });


        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setFilters(new InputFilter[]{new FirstLetterUpperCaseInputFilter()});
        editText.setMinLines(5);
        editText.setLines(5);
        editText.setHorizontallyScrolling(false);
        editText.setHint(mContext.getString(R.string.describe_hint_txt));
        editText.setMinHeight(320);
        holder.singleComponentContainer.addView(view);
    }

    private void addDateView(Node node, GenericViewHolder holder, int index) {
        holder.singleComponentContainer.removeAllViews();
        View view = View.inflate(mContext, R.layout.visit_reason_date, null);
        final Button submitButton = view.findViewById(R.id.btn_submit);
        final Button skipButton = view.findViewById(R.id.btn_skip);
        submitButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, node.isDataCaptured() ? R.drawable.ic_baseline_check_18_white : 0, 0);
        submitButton.setBackgroundResource(node.isDataCaptured() ? R.drawable.ui2_common_primary_bg : R.drawable.ui2_common_button_bg_submit);
        final TextView displayDateButton = view.findViewById(R.id.btn_view_date);
        final CalendarView calendarView = view.findViewById(R.id.cav_date);
        calendarView.setMaxDate(System.currentTimeMillis() + 1000);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                // display the selected date by using a toast
                int m = month + 1;
                //String date = (dayOfMonth < 10 ? "0" + dayOfMonth : String.valueOf(dayOfMonth))
                //        + "-" + (m < 10 ? "0" + m : String.valueOf(m)) + "-" + String.valueOf(year);
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(0);
                //cal.set(Integer.parseInt(d.split("-")[2]), Integer.parseInt(d.split("-")[1]) - 1, Integer.parseInt(d.split("-")[0]));
                cal.set(year, month, dayOfMonth);
                Date date = cal.getTime();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy", Locale.ENGLISH);
                String dateString = simpleDateFormat.format(date);
                displayDateButton.setText(dateString);
                VisitUtils.scrollNow(mRecyclerView, 400, 0, 400, mIsEditMode, false);
                AdapterUtils.setToDefault(submitButton);
                AdapterUtils.setToDefault(skipButton);
            }
        });
        //holder.skipButton.setVisibility(View.GONE);

        if (node.isDataCaptured()) {
            AdapterUtils.setToDisable(skipButton);
        } else {
            AdapterUtils.setToDefault(skipButton);
        }
        if (!holder.node.isRequired()) skipButton.setVisibility(View.VISIBLE);
        else skipButton.setVisibility(View.GONE);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AdapterUtils.buttonProgressAnimation(mContext, skipButton, false, new AdapterUtils.OnFinishActionListener() {
                    @Override
                    public void onFinish() {
                        node.setSelected(false);
                        node.setSelected(false);
                        //mOnItemSelection.onSelect(node, index);
                        VisitUtils.scrollNow(mRootRecyclerView, 1000, 0, 400, mIsEditMode, false);
                    }
                });


            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String d = displayDateButton.getText().toString().trim();
                if (!d.contains("/")) {
                    Toast.makeText(mContext, mContext.getString(R.string.please_select_date), Toast.LENGTH_SHORT).show();
                } else {
                    /*Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(0);
                    cal.set(Integer.parseInt(d.split("-")[2]), Integer.parseInt(d.split("-")[1]) - 1, Integer.parseInt(d.split("-")[0]));
                    Date date = cal.getTime();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy", Locale.ENGLISH);*/

                    if (node.getLanguage().contains("_")) {
                        node.setLanguage(node.getLanguage().replace("_", d));
                    } else if (node.getLanguage().equalsIgnoreCase("%")) {
                        node.addLanguage(d);
                    } else {
                        node.addLanguage(node.getLanguage() + " - " + d);
                        //knowledgeEngine.setText(knowledgeEngine.getLanguage());
                    }
                    node.setSelected(true);
                    holder.node.setSelected(true);

                    node.setDataCaptured(true);
                    holder.node.setDataCaptured(true);

                    //notifyDataSetChanged();
                    //mOnItemSelection.onSelect(node, index);
                    AdapterUtils.setToDisable(skipButton);
                    AdapterUtils.buttonProgressAnimation(mContext, submitButton, true, new AdapterUtils.OnFinishActionListener() {
                        @Override
                        public void onFinish() {

                        }
                    });
                    VisitUtils.scrollNow(mRootRecyclerView, 1000, 0, 400, mIsEditMode, false);
                }
            }
        });
        /*if (node.isDataCaptured() && node.isDataCaptured()) {
            submitButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_24_white, 0, 0, 0);
        } else {
            submitButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }*/
        holder.singleComponentContainer.addView(view);
    }
}

