package com.whitecloud.hm.whiteclouduser;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;

import java.util.ArrayList;
import java.util.List;

class Fragment_swipe_servicios_RecyclerAdapter extends RecyclerView.Adapter {
    private List<String> mDataSet = new ArrayList<>();
    private List<String> mDataSetInfo = new ArrayList<>();
    private List<String> mDataSetId = new ArrayList<>();
    private LayoutInflater mInflater;
    private final ViewBinderHelper binderHelper = new ViewBinderHelper();


    Fragment_swipe_servicios_RecyclerAdapter(Context context, List<String> dataSet, List<String> dataSetInfo, List<String> dataSetId) {
        mDataSet = dataSet;
        mDataSetInfo = dataSetInfo;
        mDataSetId = dataSetId;
        mInflater = LayoutInflater.from(context);

        // uncomment if you want to open only one row at a time
        // binderHelper.setOpenOnlyOne(true);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.fragment_servicios_row_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder h, int position) {
        final ViewHolder holder = (ViewHolder) h;

        if (mDataSet != null && 0 <= position && position < mDataSet.size()) {
            String data = mDataSet.get(position);
            String info = mDataSetInfo.get(position);
            String id = mDataSetId.get(position);

            // Use ViewBindHelper to restore and save the open/close state of the SwipeRevealView
            // put an unique string id as value, can be any string which uniquely define the data
            binderHelper.bind(holder.swipeLayout, data);

            // Bind your data here
            holder.bind(data, info, id);
        }
    }

    @Override
    public int getItemCount() {
        if (mDataSet == null)
            return 0;
        return mDataSet.size();
    }

    /**
     * Only if you need to restore open/close state when the orientation is changed.
     * Call this method in {@link android.app.Activity#onSaveInstanceState(Bundle)}
     */
    public void saveStates(Bundle outState) {
        binderHelper.saveStates(outState);
    }

    /**
     * Only if you need to restore open/close state when the orientation is changed.
     * Call this method in {@link android.app.Activity#onRestoreInstanceState(Bundle)}
     */
    public void restoreStates(Bundle inState) {
        binderHelper.restoreStates(inState);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private SwipeRevealLayout swipeLayout;
        private View deleteLayout;
        private TextView tvcontent;
        private TextView tvinfo;

        ViewHolder(View itemView) {
            super(itemView);
            swipeLayout = (SwipeRevealLayout) itemView.findViewById(R.id.swipe_layout);
            deleteLayout = itemView.findViewById(R.id.delete_layout);
            tvcontent = (TextView) itemView.findViewById(R.id.content);
            tvinfo = (TextView) itemView.findViewById(R.id.details);
        }

        void bind(String content, String details, final String id) {
            deleteLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDataSet.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                    log("eliminando servicio " + getAdapterPosition());
                    Inicio_sesion.datasource.delete_desc("\"servicio\":\"" + id + "\"", "servicio");
                }
            });

            tvcontent.setText(content);
            tvinfo.setText(details);
        }
    }

    private static void log(String s) {
        Log.d(Fragment_swipe_servicios_RecyclerAdapter.class.getSimpleName(), "######" + s + "######");
    }
}
