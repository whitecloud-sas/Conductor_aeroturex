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

public class BsModalToast extends BottomSheetDialogFragment {
    static BsModalToast newInstance() {
        return new BsModalToast();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bs_modal_toast, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle mArgs = getArguments();
        String myValue = null;
        if (mArgs != null) {
            myValue = mArgs.getString("json");
            log("onCreateView args " + myValue);
        }

        TextView tv_toast = view.findViewById(R.id.tv_toast);
        tv_toast.setText(myValue);

        Button btn_cancelService_bmc = view.findViewById(R.id.btn_cancelService_toast);
        btn_cancelService_bmc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private static void log(String s) {
        Log.d(BsModalToast.class.getSimpleName(), "######" + s + "######");
    }

}

