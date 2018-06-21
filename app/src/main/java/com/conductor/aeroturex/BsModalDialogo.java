package com.conductor.aeroturex;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class BsModalDialogo extends BottomSheetDialogFragment {

    String tipo = "";

    On_BsModalDialogo_Listener mCallback;

    static BsModalDialogo newInstance() {
        return new BsModalDialogo();
    }


    // The container Activity must implement this interface so the frag can deliver messages
    public interface On_BsModalDialogo_Listener {
        void from_BsModalDialogo();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bs_modal_dialogo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        log("BsModalDialogo onViewCreated ");
        Bundle mArgs = getArguments();
        String title = null,text=null,text2=null;
        if (mArgs != null) {
            title = mArgs.getString("title");
            text = mArgs.getString("text");
            text2 = mArgs.getString("text2");

            if(mArgs.containsKey("tipo")){
                tipo = mArgs.getString("tipo");
            }
        }

        TextView dialogo_title = view.findViewById(R.id.dialogo_title);
        dialogo_title.setText(title);

        TextView dialogo_modal = view.findViewById(R.id.dialogo_modal);
        dialogo_modal.setText(Html.fromHtml(text));

        TextView dialogo_modal2 = view.findViewById(R.id.dialogo_modal2);
        dialogo_modal2.setText(Html.fromHtml(text2));

        // Buttons
        Button btn_actions_dialogo = view.findViewById(R.id.btn_actions_dialogo);
        btn_actions_dialogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                log("btn_actions_dialogo.setOnClickListener");
                if(tipo.equals("salir_app")){
                    mCallback.from_BsModalDialogo();
                }

                dismiss();
                //crea opciones para en sitio,cumplir,cancelar y rechazar

            }
        });

        assert text2 != null;
        if(text2.equals("")){
            ImageView imageView8 = view.findViewById(R.id.imageView8);
            imageView8.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (BsModalDialogo.On_BsModalDialogo_Listener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()  + " must implement OnHeadlineSelectedListener");
        }
    }

    private static void log(String s) {
        Log.d(BsModalDialogo.class.getSimpleName(), "######" + s + "######");
    }

}

