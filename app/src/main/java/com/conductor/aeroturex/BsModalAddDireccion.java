package com.conductor.aeroturex;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by hm on 2017-03-20.
 */

public class BsModalAddDireccion extends BottomSheetDialogFragment {
    static BsModalAddDireccion newInstance() {
        return new BsModalAddDireccion();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bs_modal_add_direccion, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Buttons
        Button btnAdd = (Button) view.findViewById(R.id.btn_add_direccion);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        Button btnCancel = (Button) view.findViewById(R.id.btn_cancel_direccion);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}
