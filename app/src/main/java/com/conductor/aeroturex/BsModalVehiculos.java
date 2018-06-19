package com.conductor.aeroturex;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BsModalVehiculos extends BottomSheetDialogFragment {
    static BsModalVehiculos newInstance() {
        return new BsModalVehiculos();
    }

    On_BsModalVehiculos_Listener mCallback;


    // The container Activity must implement this interface so the frag can deliver messages
    interface On_BsModalVehiculos_Listener {
        /**
         * Called by On_BsModalServiceArrival_Listener when a list item is selected
         */
        void from_BsModalVehiculos(JSONObject jObj);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        return inflater.inflate(R.layout.bs_modal_vehiculos, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle mArgs = getArguments();
        String myValue = mArgs.getString("json");
        log("onCreateView args " + myValue);
        try {
            JSONObject jObj = new JSONObject(myValue);
            final JSONArray jArr = jObj.getJSONArray("moviles");


            final RadioGroup radioGrp = (RadioGroup) view.findViewById(R.id.rg_vehiculos);
            //get string array from source
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject JSONCliente = jArr.getJSONObject(i);
                RadioButton radioButton = new RadioButton(getContext());
                radioButton.setText(JSONCliente.getString("placa") + " - " + JSONCliente.getString("movil"));
                radioButton.setTextSize(18);
                radioButton.setId(i);
                radioGrp.addView(radioButton);
            }

            //set listener to radio button group
            radioGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    int checkedRadioButtonId = radioGrp.getCheckedRadioButtonId();
                    RadioButton radioBtn = (RadioButton) view.findViewById(checkedRadioButtonId);
                    log("onCheckedChanged " + radioBtn.getText().toString());
                    try {
                        mCallback.from_BsModalVehiculos(jArr.getJSONObject(checkedId));
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            });
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (On_BsModalVehiculos_Listener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnHeadlineSelectedListener");
        }
    }

    private static void log(String s) {
        Log.d(BsModalVehiculos.class.getSimpleName(), "######" + s + "######");
    }

}

