package com.whitecloud.hm.whiteclouduser;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by hm on 2017-03-22.
 */

public class BsModalEditProfile extends BottomSheetDialogFragment {
    static BsModalEditProfile newInstance() {
        return new BsModalEditProfile();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bs_modal_edit_profile, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Buttons
        Button btnAdd = (Button) view.findViewById(R.id.btn_save_profile);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        Button btnCancel = (Button) view.findViewById(R.id.btn_cancel_profile);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}
