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

public class BsModalDocumentacion extends BottomSheetDialogFragment {
    static BsModalDocumentacion newInstance() {
        return new BsModalDocumentacion();
    }
    On_BsModalServiceAproval_Listener mCallback;

    // The container Activity must implement this interface so the frag can deliver messages
    public interface On_BsModalServiceAproval_Listener {
        /** Called by On_BsModalServiceArrival_Listener when a list item is selected
         * @param cadena*/
        void from_BsModalServiceAproval(String cadena);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.bs_modal_documentacion, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView infoAdicionalView = (TextView) view.findViewById(R.id.bs_m_doc_aditional_info);
        infoAdicionalView.setText(MainActivity.bs_m_doc_aditional_info);

        Button btnReject = (Button) view.findViewById(R.id.btn_doc_accept);
        btnReject.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //dismiss();
                mCallback.from_BsModalServiceAproval("cierra_documentacion");
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

    private static void log(String s) {
        Log.d(BsModalDocumentacion.class.getSimpleName(), "######" + s + "######");
    }
}

