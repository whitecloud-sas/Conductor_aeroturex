package com.conductor.aeroturex;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ProfileFragment_seccion_info extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.profile_fragment_seccion_info,container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (view != null) {
            TextView tv_correo = (TextView) view.findViewById(R.id.tv_correo);
            tv_correo.setText(Inicio_sesion.v_correo);

            TextView tv_celular = (TextView) view.findViewById(R.id.tv_celular);
            tv_celular.setText(Inicio_sesion.v_celular);

            TextView tv_placa = (TextView) view.findViewById(R.id.tv_placa);
            tv_placa.setText("Placa " + Inicio_sesion.tv_placa);

            TextView tv_modelo = (TextView) view.findViewById(R.id.tv_modelo);
            tv_modelo.setText("Modelo " + Inicio_sesion.tv_modelo);

            TextView tv_movil = (TextView) view.findViewById(R.id.tv_movil);
            tv_movil.setText("Móvil " + Inicio_sesion.tv_movil);

            TextView tv_vigencia = (TextView) view.findViewById(R.id.tv_vigencia);
            tv_vigencia.setText("Vigencia " + Inicio_sesion.tv_vigencia);
        }
    }
}
