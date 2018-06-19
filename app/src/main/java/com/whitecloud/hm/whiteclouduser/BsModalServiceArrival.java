package com.whitecloud.hm.whiteclouduser;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class BsModalServiceArrival extends BottomSheetDialogFragment {
    static BsModalServiceArrival newInstance() {
        return new BsModalServiceArrival();
    }
    On_BsModalServiceArrival_Listener mCallback;

    // The container Activity must implement this interface so the frag can deliver messages
    public interface On_BsModalServiceArrival_Listener {
        /** Called by On_BsModalServiceArrival_Listener when a list item is selected
         */
        void from_BsModalServiceArrival();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bs_modal_service_arrival, container, false);

        TextView direccionView = (TextView) v.findViewById(R.id.bs_m_sa_direccion);
        direccionView.setText(MainActivity.v_direccion_servicio);

        TextView infoAdicionalView = (TextView) v.findViewById(R.id.bs_m_sa_aditional_info);
        infoAdicionalView.setText(MainActivity.servicio);

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Buttons
        Button btnBack = (Button) getView().findViewById(R.id.btn_actions);
        btnBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //dismiss();
                //crea opciones para en sitio,cumplir,cancelar y rechazar
                mCallback.from_BsModalServiceArrival();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (On_BsModalServiceArrival_Listener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()  + " must implement OnHeadlineSelectedListener");
        }
    }

    private static void log(String s) {
        Log.d(BsModalServiceArrival.class.getSimpleName(), "######" + s + "######");
    }

}

