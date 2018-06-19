package com.conductor.aeroturex;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;

import java.text.NumberFormat;

public class BsModalServiceAproval extends BottomSheetDialogFragment {
    static BsModalServiceAproval newInstance() {
        return new BsModalServiceAproval();
    }
    On_BsModalServiceAproval_Listener mCallback;

    // The container Activity must implement this interface so the frag can deliver messages
    public interface On_BsModalServiceAproval_Listener {
        void from_BsModalServiceAproval(String cadena);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.bs_modal_service_aproval, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String text;
        try {

            NumberFormat format = NumberFormat.getNumberInstance();

            text = "Origen: " + MainActivity.jServicio.getString("dir");

            TextView lladv_input_origen_sap = view.findViewById(R.id.lladv_input_origen_sap);
            lladv_input_origen_sap.setText(text);

            TextView lladv_input_destino_sap = view.findViewById(R.id.lladv_input_destino_sap);
            text = "Destino: " + MainActivity.jServicio.getString("dir_dest");
            lladv_input_destino_sap.setText(text);

            TextView tv_tiempo_recorrido_aproximado_bsmsa = view.findViewById(R.id.tv_tiempo_recorrido_aproximado_bsmsa);
            text = format.format(Integer.parseInt(MainActivity.jServicio.getString("t_recorrido"))/60) + " min";
            tv_tiempo_recorrido_aproximado_bsmsa.setText(text);

            TextView tv_costo_aproximado_bsmsa = view.findViewById(R.id.tv_costo_aproximado_bsmsa);
            text = "$ " + format.format(Integer.parseInt(MainActivity.jServicio.getString("valor")));
            tv_costo_aproximado_bsmsa.setText(text);

            TextView tv_recorrido_del_viaje_bsmsa = view.findViewById(R.id.tv_recorrido_del_viaje_bsmsa);
            text = format.format(Integer.parseInt(MainActivity.jServicio.getString("recorrido"))) + " m";
            tv_recorrido_del_viaje_bsmsa.setText(text);

            //RatingBar rat_usu3 = view.findViewById(R.id.rat_usu3);
            //rat_usu3.setRating(MainActivity.user_cal);


            mCallback.from_BsModalServiceAproval("consulta_recogida_aproval");


        }catch (JSONException e){
            e.printStackTrace();
        }

        MainActivity.tempoView =  view.findViewById(R.id.bs_m_sa_aditional_tempo);

        // Buttons
        Button btnAccept = view.findViewById(R.id.btn_bsmsa_accept);
        btnAccept.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //dismiss();
                //crea opciones para en sitio,cumplir,cancelar y rechazar
                mCallback.from_BsModalServiceAproval("acepta");
            }
        });

        Button btnReject = view.findViewById(R.id.btn_bsmsa_reject);
        btnReject.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //dismiss();
                //crea opciones para en sitio,cumplir,cancelar y rechazar
                mCallback.from_BsModalServiceAproval("rechaza");
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (On_BsModalServiceAproval_Listener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()  + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    private static void log(String s) {
        Log.d(BsModalServiceAproval.class.getSimpleName(), "######" + s + "######");
    }
}

