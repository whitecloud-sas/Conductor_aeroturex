package com.conductor.aeroturex.old;

public class Constants {
    public interface ACTION {
        String TRAMA_X_ENVIAR = "trama_x_enviar";
    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 101;
    }

    public interface btnPpal {
        String PONERME_OCUPADO = "Ponerme Ocupado";
        String PONERME_DISPONIBLE = "Ponerme Disponible";
        String MOSTRAR_DIRECCION = "Mostrar Dirección";
        String MOSTRAR_ACEPTACION = "Mostrar Aceptación";
        String MOSTRAR_TARIFICACION = "Estado del Viaje";
        String ESPERANDO_INFORMACION = "Esperando Información";
        String VALIDANDO = "Validando...";
    }

    public interface DB {
        String SERVICIO = "servicio";
        String RUTA = "ruta";
    }
}