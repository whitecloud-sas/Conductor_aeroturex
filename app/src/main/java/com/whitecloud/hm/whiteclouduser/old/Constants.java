package com.whitecloud.hm.whiteclouduser.old;

import com.whitecloud.hm.whiteclouduser.Inicio_sesion;

public class Constants {
    public interface ACTION {
        public static String MAIN_ACTION = Inicio_sesion.v_packageName + ".action.main";
        public static String STARTFOREGROUND_ACTION = Inicio_sesion.v_packageName + ".action.startforeground";
        public static String STOPFOREGROUND_ACTION = Inicio_sesion.v_packageName + ".action.stopforeground";
        String TRAMA_X_ENVIAR = "trama_x_enviar";
    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 101;
    }

    public interface PERMISSIONS {
        public static int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;
    }

    public interface btnPpal {
        String PONERME_OCUPADO = "Ponerme Ocupado";
        String PONERME_DISPONIBLE = "Ponerme Disponible";
        String MOSTRAR_DIRECCION = "Mostrar Dirección";
        String MOSTRAR_ACEPTACION = "Mostrar Aceptación";
        String MOSTRAR_TARIFICACION = "Mostrar Tarificación";
        String ESPERANDO_INFORMACION = "Esperando Información";
        String VALIDANDO = "Validando...";
    }
}