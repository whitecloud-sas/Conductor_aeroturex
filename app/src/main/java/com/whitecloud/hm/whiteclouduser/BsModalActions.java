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
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;

public class BsModalActions extends BottomSheetDialogFragment {
    static BsModalActions newInstance() {
        return new BsModalActions();
    }

    On_BsModalActions_Listener mCallback;

    // The container Activity must implement this interface so the frag can deliver messages
    public interface On_BsModalActions_Listener {
        /**
         * Called by On_BsModalServiceArrival_Listener when a list item is selected
         */
        void from_BsModalActions(String action);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bs_modal_actions, container, false);

        RadioGroup rGroup = (RadioGroup) v.findViewById(R.id.rg_actions);
        // This will get the radiobutton in the radiogroup that is checked
        RadioButton checkedRadioButton = (RadioButton) rGroup.findViewById(rGroup.getCheckedRadioButtonId());

        rGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // This will get the radiobutton that has changed in its check state
                RadioButton checkedRadioButton = (RadioButton) group.findViewById(checkedId);
                // This puts the value (true/false) into the variable
                boolean isChecked = checkedRadioButton.isChecked();
                // If the radiobutton that has changed in check state is now checked...
                if (isChecked) {
                    log(checkedRadioButton.getText().toString());

                    mCallback.from_BsModalActions(checkedRadioButton.getText().toString());
                }
            }
        });

        Button btnBack = (Button) v.findViewById(R.id.btn_cancelar_actions);
        btnBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //dismiss();
                mCallback.from_BsModalActions("");
            }
        });


        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (On_BsModalActions_Listener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnHeadlineSelectedListener");
        }
    }

    private static void log(String s) {
        Log.d(BsModalActions.class.getSimpleName(), "######" + s + "######");
    }

}

