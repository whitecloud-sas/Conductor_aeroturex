package com.whitecloud.hm.whiteclouduser;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class Fragment_conversaciones_RecyclerAdapter extends RecyclerView.Adapter  {

    private List<String> mDataSet = new ArrayList<>();
    private List<String> mDataSetInfo = new ArrayList<>();
    private List<String> mDataSetId = new ArrayList<>();
    private List<String> mDataOrigenId = new ArrayList<>();
    private List<String> mDataSetBadge = new ArrayList<>();
    private LayoutInflater mInflater;
    private final ViewBinderHelper binderHelper = new ViewBinderHelper();
    private Fragment_conversaciones.On_Fragment_mensajes_Listener mCommunicator;

    Fragment_conversaciones_RecyclerAdapter(Context context, List<String> dataSet, List<String> dataSetInfo, List<String> dataSetId, List<String> dataSetBadge, Fragment_conversaciones.On_Fragment_mensajes_Listener communication,List<String>  dataOrigenId) {
        mDataSet = dataSet;
        mDataSetInfo = dataSetInfo;
        mDataSetId = dataSetId;
        mDataSetBadge = dataSetBadge;
        mDataOrigenId = dataOrigenId;
        mInflater = LayoutInflater.from(context);

        mCommunicator=communication;

        // uncomment if you want to open only one row at a time
        // binderHelper.setOpenOnlyOne(true);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.conversaciones_row_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder h, int position) {
        final ViewHolder holder = (ViewHolder) h;

        if (mDataSet != null && 0 <= position && position < mDataSet.size()) {
            String data = mDataSet.get(position);
            String info = mDataSetInfo.get(position);
            String id = mDataSetId.get(position);
            String badge = mDataSetBadge.get(position);
            String Origen_id = mDataOrigenId.get(position);

            // Use ViewBindHelper to restore and save the open/close state of the SwipeRevealView
            // put an unique string id as value, can be any string which uniquely define the data
            binderHelper.bind(holder.swipeLayout, data);

            // Bind your data here
            holder.bind(data, info, id, badge, Origen_id);
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

    public class ViewHolder extends RecyclerView.ViewHolder{
        private SwipeRevealLayout swipeLayout;
        private View deleteLayout;
        private TextView tvcontent;
        private TextView tvinfo;
        private TextView tvbadge;
        private RelativeLayout rl_layout;
        private Button conversacion_go;

        public ViewHolder(View itemView) {
            super(itemView);
            swipeLayout = (SwipeRevealLayout) itemView.findViewById(R.id.swipe_layout);
            deleteLayout = itemView.findViewById(R.id.delete_layout);
            tvcontent = (TextView) itemView.findViewById(R.id.content);
            tvinfo = (TextView) itemView.findViewById(R.id.details);
            tvbadge = (TextView) itemView.findViewById(R.id.textViewBadge);
            rl_layout = (RelativeLayout) itemView.findViewById(R.id.rl_layout);
            conversacion_go = (Button) itemView.findViewById(R.id.conversacion_go);

        }

        void bind(final String content, String details, final String id, String badge, final String origen_id) {

            conversacion_go.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    log("conversacion_go clic " + id);
                    try {
                        JSONObject jObj = new JSONObject();
                        jObj.put("id", id);
                        mCommunicator.from_Fragment_mensajes(jObj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Inicio_sesion.datasource.update_conversacion_readed(origen_id);

                    int cantidad = Inicio_sesion.datasource.cant_mensajes_sin_leer();
                    if (cantidad > 0) {
                        MainActivity.badge_textView.setVisibility(View.VISIBLE);
                        MainActivity.badge_textView.setText(String.valueOf(cantidad));
                    } else {
                        MainActivity.badge_textView.setVisibility(View.GONE);
                    }

                }
            });

            rl_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    log("rl_layout clic " + id);
                    try {
                        JSONObject jObj = new JSONObject();
                        jObj.put("id", id);
                        mCommunicator.from_Fragment_mensajes(jObj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Inicio_sesion.datasource.update_conversacion_readed(content);

                    int cantidad = Inicio_sesion.datasource.cant_mensajes_sin_leer();
                    if (cantidad > 0) {
                        MainActivity.badge_textView.setVisibility(View.VISIBLE);
                        MainActivity.badge_textView.setText(String.valueOf(cantidad));
                    } else {
                        MainActivity.badge_textView.setVisibility(View.GONE);
                    }
                }
            });

            deleteLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDataSet.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                    log("eliminando mensajes");
                    //Inicio_sesion.datasource.delete_desc("\"servicio\":\"" + id + "\"", "servicio");
                    if(!content.equals("Central")) {
                        Inicio_sesion.datasource.deleteOrigen(content);
                    }
                }
            });

            tvcontent.setText(content);
            tvinfo.setText(details);

            int cant = Inicio_sesion.datasource.cant_mensajes_sin_leer_origen(content);
            if(cant>0) {
                tvbadge.setVisibility(View.VISIBLE);
                tvbadge.setText(badge);
            }else{
                tvbadge.setVisibility(View.GONE);
            }

        }
    }

    private static void log(String s) {
        Log.d(Fragment_conversaciones_RecyclerAdapter.class.getSimpleName(), "######" + s + "######");
    }
}
