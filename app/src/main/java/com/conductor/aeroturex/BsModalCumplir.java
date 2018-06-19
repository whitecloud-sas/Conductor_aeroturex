package com.conductor.aeroturex;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class BsModalCumplir extends BottomSheetDialogFragment {
    static BsModalCumplir newInstance() {
        return new BsModalCumplir();
    }
    On_BsModal_Listener mCallback;
    EditText clave;

    // The container Activity must implement this interface so the frag can deliver messages
    public interface On_BsModal_Listener {
        /** Called by On_BsModalServiceArrival_Listener when a list item is selected
         */
        void from_BsModalCumplir(String clave);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bs_modal_cumplir, container, false);

        clave = v.findViewById(R.id.clave);

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Buttons
        Button btnCumplir= getView().findViewById(R.id.btn_cumplir);
        btnCumplir.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mCallback.from_BsModalCumplir(clave.getText().toString());
            }
        });

        Button btnCerrar= getView().findViewById(R.id.btn_cumplir_cerrar);
        btnCerrar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mCallback.from_BsModalCumplir("cerrar");
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (On_BsModal_Listener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()  + " must implement OnHeadlineSelectedListener");
        }
    }

}

