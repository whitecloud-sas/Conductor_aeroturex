package com.whitecloud.hm.whiteclouduser;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class Fragment_conversacion_RecyclerAdapter extends RecyclerView.Adapter <RecyclerView.ViewHolder>  {

    private List<String> mDataSet = new ArrayList<>();
    private List<String> mDataSetInfo = new ArrayList<>();
    private List<String> mDataSetId = new ArrayList<>();
    private List<Integer> mDataSetMe = new ArrayList<>();
    private LayoutInflater mInflater;
    private final ViewBinderHelper binderHelper = new ViewBinderHelper();
    private Fragment_conversacion.On_Fragment_mensaje_Listener mCommunicator;
    private static int MY_MESSAGE = 0;

    Fragment_conversacion_RecyclerAdapter(Context context, List<String> Origen, List<String> dataSetInfo, List<String> dataSetId, List<Integer> dataSetMe,Fragment_conversacion.On_Fragment_mensaje_Listener communication) {
        mDataSet = Origen;
        mDataSetInfo = dataSetInfo;
        mDataSetId = dataSetId;
        mDataSetMe = dataSetMe;
        mInflater = LayoutInflater.from(context);

        mCommunicator=communication;

        // uncomment if you want to open only one row at a time
        binderHelper.setOpenOnlyOne(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        //log("viewType " + viewType +" MY_MESSAGE "+MY_MESSAGE);
        if (MY_MESSAGE == 1) {
            view = mInflater.inflate(R.layout.conversacion_row_list_me, parent, false);
        } else {
            view = mInflater.inflate(R.layout.conversacion_row_list_other, null, false);
        }

        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder h, int position) {
        final ViewHolder holder = (ViewHolder) h;

        if (mDataSet != null){// && 0 <= position && position < mDataSet.size()) {
            String data = mDataSet.get(position);
            String info = mDataSetInfo.get(position);
            String id = mDataSetId.get(position);
            int me = mDataSetMe.get(position);


            // Use ViewBindHelper to restore and save the open/close state of the SwipeRevealView
            // put an unique string id as value, can be any string which uniquely define the data
            binderHelper.bind(holder.swipeLayout, data);

            // Bind your data here
            holder.bind(data, info, id, me);
        }

    }


    @Override
    public int getItemViewType(int position) {
        //este es necesario para que actualice la cardinalidad del texto dependiendo del origen del mensaje
        //cada vez que se hace scroll

        MY_MESSAGE = mDataSetMe.get(position);


        return position;
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

    class ViewHolder extends RecyclerView.ViewHolder{
        private SwipeRevealLayout swipeLayout;
        private View deleteLayout;
        private TextView tvinfo, tvtime;
        private RelativeLayout rl_layout;

        ViewHolder(View itemView) {
            super(itemView);
            swipeLayout = (SwipeRevealLayout) itemView.findViewById(R.id.swipe_layout);
            deleteLayout = itemView.findViewById(R.id.delete_layout);
            tvinfo = (TextView) itemView.findViewById(R.id.details);
            tvtime  = (TextView) itemView.findViewById(R.id.time);

            rl_layout = (RelativeLayout) itemView.findViewById(R.id.rl_layout);
        }

        void bind(final String content, final String details, final String id, final int me) {

            rl_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    log("rl_layout clic " + id + " content " + content);

                    try{
                        JSONObject jObj = new JSONObject();
                        jObj.put("id", id);
                        mCommunicator.from_Fragment_mensaje(jObj);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            });

            deleteLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mDataSet.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                    log("eliminando mensaje content " + content + ", details " + details + ", id " + id);
                    Inicio_sesion.datasource.delete_msg_id(id);

                }
            });

            tvinfo.setText(details);

            String data = "Yo";
            if (me == 0) {
                data = content;
            }

            tvtime.setText(data);

        }
    }

    private static void log(String s) {
        Log.d(Fragment_conversacion_RecyclerAdapter.class.getSimpleName(), "######" + s + "######");
    }
}
