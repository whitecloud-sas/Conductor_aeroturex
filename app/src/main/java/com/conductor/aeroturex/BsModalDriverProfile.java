package com.conductor.aeroturex;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by hm on 2017-03-27.
 */

public class BsModalDriverProfile extends BottomSheetDialogFragment {
    static BsModalDriverProfile newInstance() {
        return new BsModalDriverProfile();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bs_modal_driver_profile, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Buttons
        Button btnBack = (Button) getView().findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}

