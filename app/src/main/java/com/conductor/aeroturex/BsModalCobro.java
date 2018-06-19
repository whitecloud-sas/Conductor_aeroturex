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
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;

public class BsModalCobro extends BottomSheetDialogFragment {
    static BsModalCobro newInstance() {
        return new BsModalCobro();
    }
    On_BsModalCobro_Listener mCallback;
    EditText clave_vale;
    EditText valor_vale;

    // The container Activity must implement this interface so the frag can deliver messages
    public interface On_BsModalCobro_Listener {
        /** Called by On_BsModalServiceArrival_Listener when a list item is selected
         */
        void from_BsModalCobro(JSONObject jObj);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.bs_modal_cobro, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        clave_vale = view.findViewById(R.id.clave_vale);
        valor_vale = view.findViewById(R.id.valor_vale);

        NumberFormat format;
        format = NumberFormat.getNumberInstance();

        String text = "$ " + format.format(MainActivity.total);
        valor_vale.setText(text);
        valor_vale.setEnabled(false);

        // Buttons
        Button btnCobrar = view.findViewById(R.id.btn_cobrar);
        btnCobrar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                try {
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("cmd", "btnCobrar");
                    jsonObj.put("clave_vale", clave_vale.getText().toString());
                    mCallback.from_BsModalCobro(jsonObj);
                }catch (JSONException e){
                    e.printStackTrace();
                }

                //mCallback.from_BsModalCobro(clave_vale.getText().toString(), MainActivity.total+"");
            }
        });


        Button btn_cancelar = view.findViewById(R.id.btn_cancelar);
        btn_cancelar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                try {
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("cmd", "btn_cancelar");
                    mCallback.from_BsModalCobro(jsonObj);
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (On_BsModalCobro_Listener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()  + " must implement OnHeadlineSelectedListener");
        }
    }

    private static void log(String s) {
        Log.d(BsModalCobro.class.getSimpleName(), "######" + s + "######");
    }

}

