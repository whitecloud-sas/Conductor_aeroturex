package com.whitecloud.hm.whiteclouduser;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by hm on 2017-03-22.
 */

public class ProfileFragment_seccion_info extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.profile_fragment_seccion_info,container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (view != null) {
            TextView tv_correo = (TextView) getView().findViewById(R.id.tv_correo);
            tv_correo.setText(Inicio_sesion.v_correo);

            TextView tv_celular = (TextView) getView().findViewById(R.id.tv_celular);
            tv_celular.setText(Inicio_sesion.v_celular);
        }
    }
}
