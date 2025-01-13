package org.intelehealth.app.ayu.visit.reason.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.intelehealth.app.R;
import org.intelehealth.app.knowledgeEngine.Node;
import org.intelehealth.app.utilities.CustomLog;

import java.util.ArrayList;
import java.util.List;


public class OptionsChipsGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    private static final String TAG = OptionsChipsGridAdapter.class.getSimpleName();
    private Context mContext;
    private Node mParentNode;
    private List<Node> mItemList = new ArrayList<Node>();
    private List<String> mEditTimeLoadedIds = new ArrayList<String>();

    public interface OnItemSelection {
        public void onSelect(Node data, boolean isLoadingForNestedEditData);
    }

    private OnItemSelection mOnItemSelection;

    public OptionsChipsGridAdapter(RecyclerView recyclerView, Context context, Node parentNode, List<Node> itemList, OnItemSelection onItemSelection) {
        mContext = context;
        mItemList = itemList;
        mParentNode = parentNode;
        mOnItemSelection = onItemSelection;
        //mAnimator = new RecyclerViewAnimator(recyclerView);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ui2_chips_for_reason_item_view, parent, false);
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
            genericViewHolder.index = genericViewHolder.getAbsoluteAdapterPosition();
            genericViewHolder.node = mItemList.get(genericViewHolder.index);
            genericViewHolder.tvName.setText(mItemList.get(genericViewHolder.index).findDisplay());

            //CustomLog.v("node", String.valueOf(genericViewHolder.node.isSelected()));


            if (genericViewHolder.node.isSelected()) {
                if (genericViewHolder.node.isNeedToHide()) {
                    genericViewHolder.tvName.setEnabled(false);
                    genericViewHolder.tvName.setBackgroundResource(R.drawable.ui2_chip_type_inactive_bg);
                    genericViewHolder.tvName.setTextColor(ContextCompat.getColor(mContext,R.color.ui2_black_text_color));
                    genericViewHolder.node.setSelected(false);
                    genericViewHolder.node.setDataCaptured(false);
                } else {
                    genericViewHolder.tvName.setBackgroundResource(R.drawable.ui2_common_button_bg_submit);
                    genericViewHolder.tvName.setTextColor(ContextCompat.getColor(mContext,R.color.white));
                    genericViewHolder.tvName.setEnabled(true);
                }
                //if (mItemList.get(genericViewHolder.index).getOptionsList() != null && !mItemList.get(genericViewHolder.index).getOptionsList().isEmpty())
                new Handler().postDelayed(() -> {
                    String id = mItemList.get(genericViewHolder.index).getId();
                    CustomLog.v(TAG, "TAG - " + id);
                    CustomLog.v(TAG, "mEditTimeLoadedIds - " + new Gson().toJson(mEditTimeLoadedIds));
                    if (!mEditTimeLoadedIds.contains(id)) {
                        mEditTimeLoadedIds.add(id);
                        mOnItemSelection.onSelect(mItemList.get(genericViewHolder.index), true);
                    }
                }, 100);

            } else {
                genericViewHolder.tvName.setBackgroundResource(R.drawable.edittext_border_blue);
                genericViewHolder.tvName.setTextColor(ContextCompat.getColor(mContext,R.color.ui2_black_text_color));

                if (genericViewHolder.node.isNeedToHide()) {
                    genericViewHolder.tvName.setEnabled(false);
                    genericViewHolder.tvName.setBackgroundResource(R.drawable.ui2_chip_type_inactive_bg);
                } else {
                    genericViewHolder.tvName.setEnabled(true);
                    genericViewHolder.tvName.setBackgroundResource(R.drawable.edittext_border_blue);
                }
            }


        }
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    private class GenericViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        Node node;
        int index;

        GenericViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvName.setOnClickListener(view -> {
                CustomLog.v("node", "isMultiChoice - " + mParentNode.isMultiChoice());
                CustomLog.v("node", "isExcludedFromMultiChoice - " + node.isExcludedFromMultiChoice());
                CustomLog.v("node", "enableExclusiveOption - " + node.isEnableExclusiveOption());
                CustomLog.v("node", "isExclusiveOption - " + node.isExclusiveOption());
                if (!mParentNode.isMultiChoice()) {
                    if (mParentNode.isEnableExclusiveOption()) {
                        if (!node.isExclusiveOption()) {
                            for (int i = 0; i < mItemList.size(); i++) {
                                if (!mItemList.get(i).isExclusiveOption())
                                    mItemList.get(i).setSelected(false);
                            }
                        }
                        mItemList.get(index).setSelected(!mItemList.get(index).isSelected());
                    } else {
                        for (int i = 0; i < mItemList.size(); i++) {
                            if (i == index) {
                                //mItemList.get(i).setSelected(i == index);
                                mItemList.get(index).setSelected(!mItemList.get(index).isSelected());
                            } else {
                                mItemList.get(i).setSelected(false);
                            }
                        }
                    }
                } else {
                    if (node.isExcludedFromMultiChoice()) {
                        for (int i = 0; i < mItemList.size(); i++) {
                            if (i != index && !mItemList.get(i).isExclusiveOption())
                                mItemList.get(i).setSelected(false);
                        }
                    } else {
                        for (int i = 0; i < mItemList.size(); i++) {
                            if (mItemList.get(i).isExcludedFromMultiChoice() && !node.isExclusiveOption())
                                mItemList.get(i).setSelected(false);
                        }
                    }
                    mItemList.get(index).setSelected(!mItemList.get(index).isSelected());
                }
                mOnItemSelection.onSelect(mItemList.get(index), false);
                notifyDataSetChanged();
            });

        }


    }


}

