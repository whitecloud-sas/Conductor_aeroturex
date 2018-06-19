package com.conductor.aeroturex.old;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.conductor.aeroturex.Inicio_sesion;
import com.conductor.aeroturex.MainActivity;
import com.conductor.aeroturex.MapaPrincipalFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static com.conductor.aeroturex.MapaPrincipalFragment.tv_costo_aproximado_bsmst;
import static com.conductor.aeroturex.MapaPrincipalFragment.tv_recorrido_del_viaje_bsmst;

@SuppressLint("Registered")
public class Tarificador extends Activity implements Runnable {
    Thread t;
    double v_distancia = 0,
            v_intervaloDistancia = 0,
            totaltmp = 0;
    private JSONObject result;
    Double v_tLatitud = 0.0, v_tLongitud = 0.0;//temporales para calcular distancia recorrida
    public static Activity activity;
    String v_transcurrido, hour, min, seg, tiempo_espera = "f", estado = "", tiempo = "";
    int horas = 0, minutos = 0, segundos = 0, tminimatermina = 0, v_TiempoEspera = 0, tespera = 0,
            distancia_tmp = 0, t_distancia_gps_error = 0;
    boolean enviar_ubicacion = true;
    float tmetrounidad = 0, tpreciounidad, v_TiempoEsperaCantidad = 0, tminima;
    MyTimerTask myTask;
    NumberFormat format;

    ArrayList<String> ruta_latitud = new ArrayList<>();
    ArrayList<String> ruta_longitud = new ArrayList<>();

    public static Activity getActivity() {
        return activity;
    }

    public static void setActivity(Activity mactivity) {
        activity = mactivity;
    }

    public Tarificador(JSONObject result) {
        this.result = result;
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
    }

    public void start() {
        MainActivity.v_distanciaTotal=0;
        MainActivity.total = 0;
        MainActivity.v_idServicioRuta = MainActivity.v_idServicio;
        myTask = new MyTimerTask();
        MainActivity.myTimerTarificador = new Timer();
        t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    MainActivity.v_tarificando = true;
                    log("v_tarificando true 65");

                    if(result.has("tpreciounidad")) {
                        String res = result.getString("tpreciounidad");
                        tpreciounidad = Float.parseFloat(res);//180
                        res = result.getString("tmetrounidad");
                        tmetrounidad = Float.parseFloat(res);//100
                        res = result.getString("tespera");
                        tespera = Integer.parseInt(res);//50
                        res = result.getString("tminima");
                        tminima = Integer.parseInt(res);//7000
                        res = result.getString("tminimatermina");
                        tminimatermina = Integer.parseInt(res);//4000
                        res = result.getString("t_distancia_gps_error");
                        t_distancia_gps_error = Integer.parseInt(res);//0
                        MainActivity.total = (int) tminima;
                        //si el estado es RESUME, se debe continuar con el VALOR y la DISTANCIA que devuelve el servidor
                        estado = result.getString("estado");//NUEVA/RESUME
                        if (estado.equals("RESUME")) {
                            res = result.getString("distancia");
                            v_distancia = Integer.parseInt(res);//2120 metros
                            res = result.getString("valor");
                            tminima = Integer.parseInt(res);//$ 7180
                            MainActivity.total = (int) tminima;
                            totaltmp = MainActivity.total;
                            tiempo = result.getString("tiempo");//07:08:15
                        }
                    }

                    format = NumberFormat.getNumberInstance();

                    MainActivity.myTimerTarificador.schedule(myTask, 500, 1000);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private class MyTimerTask extends TimerTask {
        public void run() {

            if (!tiempo.equals("")) {
                String[] tokito = MainActivity.splitTotokens(tiempo + ":", ":");
                segundos = Integer.parseInt(tokito[2]);
                minutos = Integer.parseInt(tokito[1]);
                horas = Integer.parseInt(tokito[0]);
                segundos++;
                tiempo = "";
            }

            segundos++;

            //parámetros para visualizar el alert
            if (segundos >= 60) {
                minutos++;
                segundos = 0;
            }

            if (segundos < 10)
                seg = "0" + segundos;
            else
                seg = segundos + "";

            if (minutos > 60) {
                minutos = 0;
            }

            if (minutos < 10)
                min = "0" + minutos;
            else
                min = minutos + "";

            if (horas < 10)
                hour = "0" + horas;
            else
                hour = horas + "";

            v_transcurrido = hour + ":" + min + ":" + seg;

            if (v_tLongitud != 0.0 && v_tLatitud != 0.0) {

                Location loc1 = new Location("");
                loc1.setLatitude(MainActivity.v_latitud);
                loc1.setLongitude(MainActivity.v_longitud);

                Location loc2 = new Location("");
                loc2.setLatitude(v_tLatitud);
                loc2.setLongitude(v_tLongitud);

                v_distancia = loc1.distanceTo(loc2);
            }

            v_tLatitud = MainActivity.v_latitud;
            v_tLongitud = MainActivity.v_longitud;
            MainActivity.v_distanciaTotal += v_distancia;

            MainActivity.p_distancia = (int) MainActivity.v_distanciaTotal;

            v_intervaloDistancia += v_distancia;
            v_TiempoEspera++;

            if (MainActivity.v_velocidad > 0) {
                //se reinician los segundos
                v_TiempoEspera = 0;
            }

            if (v_TiempoEspera >= tespera) {
                //log("pasaron " + tespera + " segundos sin moverse mas de " + t_distancia_gps_error + " metros, adicionando valor");
                // si el vehiculo no se ha movido en 50 segundos,
                // adicionar valor asi este en carrera minima
                v_TiempoEsperaCantidad++;
                // transmite la adicion del valor,
                enviar_ubicacion = true;
                //se reinician los segundos
                v_TiempoEspera = 0;
                //reinicia el intervalo de distancia recorrida,
                v_intervaloDistancia = 0;
                tiempo_espera = "t";
            }

            //double recorrido = MainActivity.v_distanciaTotal - tminimatermina;

            if(tmetrounidad==0) {
                //la tarifa es preestablecida
                try {
                    if(result.has("vale_valor"))
                        MainActivity.total = Integer.parseInt(result.getString("vale_valor"));
                    else
                        MainActivity.total = Integer.parseInt(result.getString("valor"));
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }/*else{
                if (recorrido > 0) {
                    //MainActivity.total = (int) (tminima + ((tpreciounidad / tmetrounidad) * recorrido));
                } else {
                   // MainActivity.total = (int) tminima;
                }
            }*/

            //si el valor de la distancia ha cambiado, transmite
            if (distancia_tmp != (int) MainActivity.v_distanciaTotal) {
                //log("El valor de la distancia ha cambiado");
                distancia_tmp = (int) MainActivity.v_distanciaTotal;
                //enviar_ubicacion = true;
            }

            //si el valor total ha cambiado, transmite
            if (totaltmp != MainActivity.total) {
                log("El valor de la carrera ha cambiado");
                totaltmp = MainActivity.total;
                enviar_ubicacion = true;
            }

            runOnUiThread(new Runnable() {
                public void run() {

                    if (MainActivity.tipo_tarifa.equals("66")) {
                        int segundos = (((horas/60)/60)  + (Integer.parseInt(min)/60) + Integer.parseInt(seg));
                        segundos += 3600;
                        MainActivity.total = Math.round(segundos/60/60)*Integer.parseInt(MainActivity.valor_hora);
                    }
                    String text = "$ " + format.format(MainActivity.total);
                    if(tv_costo_aproximado_bsmst!=null) {
                        tv_costo_aproximado_bsmst.setText(text);
                    }

                    if(tv_recorrido_del_viaje_bsmst!=null) {
                        NumberFormat format = NumberFormat.getNumberInstance();
                        text = format.format(Math.round((int) MainActivity.v_distanciaTotal)) + " metros";
                        tv_recorrido_del_viaje_bsmst.setText(text);
                    }

                    if(MapaPrincipalFragment.tv_tiempo_transcurrido_bsmst!=null) {
                        MapaPrincipalFragment.tv_tiempo_transcurrido_bsmst.setText(v_transcurrido);
                    }
                }
            });

            if (enviar_ubicacion) {
                ruteo();
                enviar_ubicacion = false;
                tiempo_espera = "f";
            }

        }

        void ruteo() {
            try {
                if (!MainActivity.v_idServicioRuta.equals("0")) {

                    ruta_latitud.add(MainActivity.v_latitud + "");
                    ruta_longitud.add(MainActivity.v_longitud + "");

                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("su_id", "0");
                    jsonObj.put("su_latitud", MainActivity.v_latitud);
                    jsonObj.put("su_longitud", MainActivity.v_longitud);
                    jsonObj.put("su_precio", MainActivity.total);
                    jsonObj.put("su_distancia", (int) MainActivity.v_distanciaTotal);
                    jsonObj.put("su_tiempoespera", tiempo_espera);
                    jsonObj.put("su_orientacion", MainActivity.v_bearing);
                    jsonObj.put("su_velocidad", MainActivity.v_velocidad);
                    jsonObj.put("su_servicio", MainActivity.v_idServicioRuta);
                    jsonObj.put("su_fechagps", MainActivity.v_fechaGPS);
                    jsonObj.put("su_precision", MainActivity.v_precision);
                    jsonObj.put("su_esbuena", 1);

                    Inicio_sesion.datasource.createUbicacion(jsonObj);

                } else {
                    activity.finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private static void log(String s) {
        Log.d(Tarificador.class.getSimpleName(), "######" + s + "######");
    }
}