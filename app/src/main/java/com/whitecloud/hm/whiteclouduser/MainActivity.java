package com.whitecloud.hm.whiteclouduser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.whitecloud.hm.whiteclouduser.dummy.DummyContentDirecciones;
import com.whitecloud.hm.whiteclouduser.dummy.DummyContentServicios;
import com.whitecloud.hm.whiteclouduser.dummy.DummyContentHistorialChat;
import com.whitecloud.hm.whiteclouduser.listener.SensorListener;
import com.whitecloud.hm.whiteclouduser.old.Constants;
import com.whitecloud.hm.whiteclouduser.old.SocketTask;
import com.whitecloud.hm.whiteclouduser.old.Tarificador;
import com.whitecloud.hm.whiteclouduser.old.WakeLocker;
import com.whitecloud.hm.whiteclouduser.old.callAlertBox;
import com.whitecloud.hm.whiteclouduser.service.gps.GPSLogger;
import com.whitecloud.hm.whiteclouduser.service.gps.GPSLoggerServiceConnection;
import com.whitecloud.hm.whiteclouduser.utils.NotificationUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;

import static com.whitecloud.hm.whiteclouduser.Inicio_sesion.datasource;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MapaPrincipalFragment.OnFragmentInteractionListener,
        DireccionesFragment.OnListFragmentInteractionListener,
        ServiciosFragment.OnListFragmentInteractionListener,
        ProfileFragment.OnFragmentInteractionListener,
        HistorialChatFragment.OnListFragmentInteractionListener,
        ChatFragment.OnFragmentInteractionListener,
        RatingFragment.OnFragmentInteractionListener,
        AutocompleteAddressFragment.OnFragmentInteractionListener,
        EditProfilePicFragment.OnFragmentInteractionListener,
        SocketTask.from_SocketTask,
        TextToSpeech.OnInitListener,
        BsModalServiceArrival.On_BsModalServiceArrival_Listener,
        BsModalServiceAproval.On_BsModalServiceAproval_Listener,
        BsModalTarificacion.On_BsModalTarificacion_Listener,
        BsModalCobro.On_BsModalCobro_Listener,
        BsModalActions.On_BsModalActions_Listener,
        BsModalCancelations.On_BsModalCancelations_Listener,
        BsModalCumplir.On_BsModal_Listener,
        Fragment_swipe_servicios.On_Fragment_swipe_servicios_Listener,
        Fragment_conversaciones.On_Fragment_mensajes_Listener,
        Fragment_conversacion.On_Fragment_mensaje_Listener,
        callAlertBox.callAlertBox_interface,
        MapaPrincipalFragment.On_MapaPrincipal_Listener{

    public static MediaPlayer mpSplash;
    public static Vibrator vibrar;
    public static AlertDialog dlg = null;
    public static TextToSpeech tts;
    public static double v_latitud = 0, v_longitud = 0, v_latitudServicio = 0, v_longitudServicio = 0, v_latitudPrevia = 0, v_longitudPrevia = 0;
    public static int v_temporizador = 0, g = 0, reconector = 2,
            v_ubicacion, // permite enviar ubicacion actual cada 15 minutos si no ha reportado ubicación en este tiempo
            v_tamanoFuente = 20, p_distancia = 0, total = 0, v_bearing = 0, v_reconexion = 0, v_precision = 0;
    public static String v_ttsDirBarr = "",
            v_clave = null, v_idServicio = "", v_resultado = null, v_turno = "", empresa = "0", v_idServicioRuta = "0", v_fechaGPS = "";
    private GPSLogger gpsLogger;
    Intent gpsLoggerServiceIntent;
    private ServiceConnection gpsLoggerConnection = new GPSLoggerServiceConnection(this);
    SensorListener sensorListener;
    static MapboxMap map;
    static Context mContext;
    public static SocketTask networktask = null;
    public static Activity otraActividad = null;
    public static int v_countAlive = 0;
    CountDownTimer cdt = null;
    static String btnPrincipal_text = Constants.btnPpal.PONERME_DISPONIBLE, servicio, ubicacion = "", fecha = null, v_idmensaje = null,
            v_tipoServicio = "", v_pista = "", v_estadoTarificacion = "NUEVA";
    static Boolean coordenadas = false, iniciaSesion = false, confirmando = false, v_primera_lectura_gps = false, v_EnSitio = false, v_ruteo = false;
    public static Boolean v_tarificando = false, //tan pronto le da cumplir al servicio se pone en true;
                v_enviando_ubicaciones = false, v_listaPendiente = false, v_desconexion = true, v_disponible = false,
                v_sonando = false, v_cancelado = false, v_autenticado = false,
                v_servicio = false, v_prefIlumina = true, v_tts = true, v_preftts = false;
    static boolean pendiente, running2 = false, v_tmax = false, envia = false, flag = true,
            isCharging = false, pulsaDisponible = false, v_enservicio = false, v_ocupado = false, rec = true;
    static int  versionNumber = 0, aceptando = 30, level = -1, k= 0;
    static ProgressDialog espere = null;
    static MarkerView service_marker = null;
    static Toast toast = null;
    static Button btnPrincipal;
    ConnectivityManager conMgr = null;
    Boolean bool_KeepAliveGPS = false;
    static String v_direccion_servicio, v_direccion_aproval, v_info_adicional_aproval;
    private double distancia;
    MaterialStyledDialog Alert_estadoVale = null;
    BottomSheetDialogFragment bsdFragment_Cumplir,bsdFragment_Cancelations,bsdFragment_Actions,bsdFragment_ServiceArrival,
            bsdFragment_ServiceAproval, bsdFragment_Tarificacion, bsdFragment_Cobro;
    Fragment fragment = null;
    public static Timer myTimerTarificador;
    public static float v_velocidad = 0;
    public static double v_distanciaTotal;
    static TextView tempoView;
    String vale_id = "0";
    public static TextView tarificadorView;
    public static int trama_x_enviar_contador = 0;
    final Handler tt_handler = new Handler();
    Runnable runnable = null;
    public static Boolean in_conversacion = false;
    public static TextView badge_textView;
    int alive = 35;
    boolean en_aceptacion_servicio = false;
    public static JSONObject jServicio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        gpsLoggerServiceIntent = new Intent(this, GPSLogger.class);
        sensorListener = new SensorListener();
        startService(gpsLoggerServiceIntent);
        bindService(gpsLoggerServiceIntent, gpsLoggerConnection, 0);
        sensorListener.register(this);
        mContext = this;

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Add fragment con el mapa principal
        Fragment fragment_ = null;
        Class fragmentClass = MapaPrincipalFragment.class;
        try {
            fragment_ = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment_).commit();

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Buttons
        Button btnMenu = (Button) findViewById(R.id.btn_menu);
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(GravityCompat.START);
            }
        });
        badge_textView = (TextView) findViewById(R.id.badge_textView);
        badge_textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Class fragmentClass = Fragment_conversaciones.class;
                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                in_conversacion = false;

                /*Button btninfo = (Button) findViewById(R.id.btn_info);
                Drawable image = mContext.getResources().getDrawable( R.drawable.ic_info);
                int h = image.getIntrinsicHeight();
                int w = image.getIntrinsicWidth();
                image.setBounds( 0, 0, w, h );
                btninfo.setCompoundDrawables(null, null, image, null);*/

                log("Click: Info");
            }
        });

        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionNumber = pinfo.versionCode;
            // String versionName = pinfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        empresa = getResources().getString(R.string.empresa);

        tts = new TextToSpeech(mContext, MainActivity.this);

        callAlertBox callAlertBox_ = new callAlertBox();
        callAlertBox_.setActivity(this, this);

        int cantidad = Inicio_sesion.datasource.cant_mensajes_sin_leer();
        if(cantidad>0) {
            badge_textView.setVisibility(View.VISIBLE);
            badge_textView.setText(String.valueOf(cantidad));
        }else{
            badge_textView.setVisibility(View.GONE);
        }

    }
    @Override
    public void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
    }


    // Check screen orientation or screen rotate event here
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void from_BsModalCumplir(String clave) {
        log("RECIBE from_BsModalCumplir: " + clave);
        if(clave.equals("cerrar")){
            bsdFragment_Cumplir.dismiss();
        }else {
            datasource.updateServtipoCancela("-1");
            if (v_clave.equals(clave)) {
                confirmando = true;
                datasource.create("04|" + v_idServicio + "|" + v_latitud + "|" + v_longitud + "|" + v_estadoTarificacion, Constants.ACTION.TRAMA_X_ENVIAR);
                //networktask.SendDataToNetwork("04|" + v_idServicio + "|" + v_latitud + "|" + v_longitud + "|" + v_estadoTarificacion);
                v_estadoTarificacion = "NUEVA";
                v_ubicacion = 0;
                bsdFragment_Cumplir.dismiss();
            } else {
                toast("Clave incorrecta");
                mpSplash = MediaPlayer.create(getApplicationContext(), R.raw.ding);
                mpSplash.start();
                vibrar = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrar.vibrate(400);
            }
        }
    }

    @Override
    public void from_BsModalCancelations(String action) {
        log("RECIBE from_BsModalCancelations: " + action);

        if(!action.equals("")) {
            datasource.updateServtipoCancela(action);
            confirmando = true;
            datasource.create("11|" + action + "|" + v_idServicio + "|" + v_latitud + "|" + v_longitud, Constants.ACTION.TRAMA_X_ENVIAR);
            v_ubicacion = 0;
            toast("Enviando Servicio Cancelado");
        }

        if(bsdFragment_Cancelations!=null)
            bsdFragment_Cancelations.dismiss();

    }

    @Override
    public void from_BsModalActions(String action) {
        log("RECIBE from_BsModalActions: " + action);
        if(action.equals("En sitio")){
            en_sitio();
        }else if(action.equals("Cumplir")){
            bsdFragment_Cumplir = BsModalCumplir.newInstance();
            bsdFragment_Cumplir.show(getSupportFragmentManager(), "bsdFragment_Cumplir");
        }else if(action.equals("Cancelar")){
            bsdFragment_Cancelations = BsModalCancelations.newInstance();
            bsdFragment_Cancelations.show(getSupportFragmentManager(), "bsdFragment_Cancelations");
        }else if(action.equals("Chat con Usuario")){

            if(bsdFragment_ServiceArrival!=null)
                bsdFragment_ServiceArrival.dismiss();

            try {
                JSONObject jObj = new JSONObject();
                jObj.put("id", "0");
                jObj.put("user_pic", jServicio.getString("user_pic"));
                jObj.put("user_id", jServicio.getString("user_id"));
                jObj.put("user_name", jServicio.getString("nombre"));
                from_Fragment_mensajes(jObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }else if(action.equals("Rechazar")){
            rechazar_servicio_aceptado();
        }

        bsdFragment_Actions.dismiss();

    }

    @Override
    public void from_BsModalCobro(String clave_vale, String valor_vale) {
        log("RECIBE from_BsModalCobro " );

        if (!clave_vale.trim().equals("") && !valor_vale.trim().equals("")) {
            String cedula = datasource.select("vale_cedula");
            log("vale_cedula " + cedula);
            if (clave_vale.trim().equals(cedula)) {
                if (bsdFragment_Tarificacion != null)
                    bsdFragment_Tarificacion.dismiss();
                if (bsdFragment_ServiceArrival != null)
                    bsdFragment_ServiceArrival.dismiss();
                if (bsdFragment_ServiceAproval != null)
                    bsdFragment_ServiceAproval.dismiss();
                if (bsdFragment_Cobro != null)
                    bsdFragment_Cobro.dismiss();
                pagar_vale_envio();
            } else {
                callAlertBox.showMyAlertBox("Clave Incorrecta");
            }
        } else {
            callAlertBox.showMyAlertBox("Imposible continuar, Campo Vacío");
        }
    }

    @Override
    public void from_BsModalTarificacion() {
        log("RECIBE from_BsModalTarificacion");

        //cobrar
        String cedula = datasource.select("vale_cedula");
        if(cedula.trim().equals("")) {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setPrompt("Por favor lea su QR");
            integrator.setOrientationLocked(false);
            integrator.initiateScan();
        }else {
            cobrar_vale();
        }
    }

    void cobrar_vale(){

        bsdFragment_Cobro = BsModalCobro.newInstance();
        bsdFragment_Cobro.show(getSupportFragmentManager(), "bsdFragment_Cobro");

    }

    private void pagar_vale_envio() {
        //pago de vale

        //TODO detener pago asi no hayan datos para no cobrar mas al usuario
        //if(v_desconexion){
        //    datasource.create("cobro_enviado", "estado_telefono");
        //}else{
            String vale_id = datasource.select("vale_id");
            String servicio_id = datasource.select("vale_servicio");
            datasource.create("69|" + servicio_id + "|" + empresa + "|" + v_latitud + "|" + v_longitud + "|" + vale_id + "|" + total,Constants.ACTION.TRAMA_X_ENVIAR);
        //}

        if (myTimerTarificador != null)
            myTimerTarificador.cancel();
        v_tarificando = false;
        v_idServicioRuta = "";

    }

    @Override
    public void from_BsModalServiceArrival() {
        log("RECIBE from_BsModalServiceArrival");
        bsdFragment_Actions = BsModalActions.newInstance();
        bsdFragment_Actions.show(getSupportFragmentManager(), "bsdFragment_Actions");
    }

    @Override
    public void from_BsModalServiceAproval(String cadena) {
        log("RECIBE from_BsModalServiceAproval: " + cadena);

        if(cadena.equals("acepta")){
            acepta_recibir_servicio();
        }else if(cadena.equals("rechaza")){
            rechaza_servicio();
        }

        bsdFragment_ServiceAproval.dismiss();

    }

    void rechaza_servicio(){
        en_aceptacion_servicio = false;
        cdt.cancel();
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("cmd", "14");
            envia_json(jsonObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        v_ubicacion = 0;
        //dialog.dismiss();
        if (vibrar != null)
            vibrar.cancel();
        if (mpSplash != null)
            mpSplash.stop();
        ponerme_ocupado();
        flag = false;
        pulsaDisponible = false;
        v_servicio = false;
    }

    void acepta_recibir_servicio (){
        cdt.cancel();
        networktask.SendDataToNetwork("12|" + v_latitud + "|" + v_longitud);
        v_ubicacion = 0;
        //dialog.dismiss();
        if (vibrar != null)
            vibrar.cancel();
        if (mpSplash != null)
            mpSplash.stop();
        flag = false;
        btnPrincipal.setText(Constants.btnPpal.ESPERANDO_INFORMACION);
        btnPrincipal_text = Constants.btnPpal.ESPERANDO_INFORMACION;
        en_aceptacion_servicio = false;
    }

    @Override
    public void from_SocketTask(String data) {
        log("RECIBE from_SocketTask: " + data);
        v_countAlive = 0;

            //si no en estan los comandos anteriores es porque estoy recibiendo en json
        try {
            JSONObject jObj = new JSONObject(data);
            String comando = jObj.getString("cmd");
            if (comando.equals("02")) {
                recibe_servicio(jObj);
            } else if (comando.equals("19")) {
                String respuesta = jObj.getString("msj");
                if (respuesta.equals("")) {
                    iniciaSesion = true;
                    IniciaSesion();
                    if (pulsaDisponible) {
                        // Si el dispositivo estaba en estado disponible
                        // antes de recibir una desconexión, entonces se
                        // pone disponible automáticamente

                        ponerme_disponible();
                    }

                    //revisamos si hay servicio pendiente
                    if (v_idServicio.equals("")) {
                        revisa_servicio_pendiente(jObj);
                    }
                } else {
                    callAlertBox.showMyAlertBox(respuesta);
                    v_autenticado = false;
                }
            } else if (comando.equals("04")) {
                v_enservicio = true;
                //lee el QR
                IntentIntegrator integrator = new IntentIntegrator((Activity) mContext);
                integrator.setPrompt("");
                integrator.setOrientationLocked(true);
                integrator.initiateScan();
            } else if (comando.equals("16")) {
                String descripcion=jObj.getString("desc");
                if(descripcion.equals("")) {
                    vibrar = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v_disponible = true;
                    envia = true;
                    v_EnSitio = false;
                    if (toast != null)
                        toast.cancel();
                    mpSplash = MediaPlayer.create(getApplicationContext(), R.raw.ding);
                    mpSplash.start();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnPrincipal.setBackgroundColor(getResources().getColor(R.color.primary_dark));
                            btnPrincipal.setText(Constants.btnPpal.PONERME_OCUPADO);
                            btnPrincipal_text = btnPrincipal.getText().toString();
                        }
                    });
                }else{

                    descripcion = descripcion.replaceAll("-", ".\n");

                    callAlertBox.showMyAlertBox("Se Requiere Atención en la Siguiente Documentación:\n\n" + descripcion);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnPrincipal.setText(Constants.btnPpal.PONERME_DISPONIBLE);
                            btnPrincipal_text = btnPrincipal.getText().toString();
                        }
                    });
                }
            } else if (comando.equals("03")) {
                pulsaDisponible = false;
                ponerme_ocupado();
                v_ocupado = true;
            } else if (comando.equals("08")) {
            } else if (comando.equals("24")) {
                //En sitio
                v_EnSitio = true;
                toast("Unidad En Sitio..\n" + (int) distancia + " metros.");
            } else if (comando.equals("10")) {
                confirmando = false;
                pulsaDisponible = false;
                pendiente = false;
                toast("Servicio Rechazado por el Conductor Confirmado");
                btnPrincipal.setText(Constants.btnPpal.PONERME_DISPONIBLE);
                btnPrincipal_text = btnPrincipal.getText().toString();
                datasource.updateServEstado(4);
                datasource.updateRegistros("servicio");
                bsdFragment_ServiceArrival.dismiss();
            } else if (comando.equals("11")) {
                datasource.deleteId(jObj.getString("slt"));
                v_enservicio = false;
                v_clave = null;
                confirmando = false;
                pulsaDisponible = false;
                toast("Servicio Cancelado: Confirmado");
                datasource.updateServEstado(5);
                v_idServicio = "";
                pendiente = false;
                datasource.updateRegistros("servicio");
                ponerme_ocupado();
                bsdFragment_ServiceArrival.dismiss();
            } else if (comando.equals("12")) {
                pregunta_aceptacion_servicio(jObj);
            } else if (comando.equals("76")) {
                datasource.updateUbicacion(jObj);
            } else if (comando.equals("69")) {
                comando_69();
            } else if (comando.equals("06")) {
                recibe_mensaje(jObj);
            } else if (comando.equals("29")) {
                recibe_confirmacion_mensaje(jObj);
            } else if (comando.equals("CU")) {

            } else {
                log("No implementado " + data);
            }
            if(jObj.has("slt"))
                datasource.deleteId(jObj.getString("slt"));
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void revisa_servicio_pendiente(JSONObject jObj){
        //{"barrio":"Prados del Norte","clave":"99","cmd":"19","descripcion":"0","direccion":"Calle 36AN - 3GN 07","empresa_vale":"0","indicacion":"0","lat":"3.474372","lng":"-76.523361","msj":"","nombre":"Martin Figueroa","servicio":"236","tipo_serv":"GEOSERVICIO","unidad":"0"}
        try{
            v_idServicio = jObj.getString("servicio");
            if(!v_idServicio.equals("")){
                jServicio = jObj;

                v_resultado = null;
                String msg = "", v_direccion, v_barrio, v_indicacion, v_unidad, v_descripcion, v_nombre, v_razonsocial;
                //02|id_servicio|clave|direccion|barrio|latitud|longitud|unidad|descripcion|nombre|tipo_servicio|empresa_vale|tiempo para cumplir|indicacion

                v_clave = jObj.getString("clave");
                v_direccion = jObj.getString("dir");
                v_barrio = jObj.getString("barrio");
                v_latitudServicio = Double.valueOf(jObj.getString("lat")).doubleValue();
                v_longitudServicio = Double.valueOf(jObj.getString("lng")).doubleValue();
                v_unidad = jObj.getString("unidad");
                v_descripcion = jObj.getString("desc");
                v_nombre = jObj.getString("nombre");
                v_razonsocial = jObj.getString("empresa");
                v_temporizador = 0;
                v_indicacion = jObj.getString("indic");

                String v_dir_destino = jObj.getString("dir_d");

                v_direccion_servicio = v_direccion;
                if (!v_barrio.equals("0") && !!v_barrio.equals(""))
                    msg += "Barrio: " + v_barrio;
                if (!v_nombre.equals("0") && !v_nombre.equals(""))
                    msg += "\nNombre: " + v_nombre;
                if (!v_unidad.equals("0") && !v_unidad.equals(""))
                    msg += "\nUnidad: " + v_unidad;
                if (!v_razonsocial.equals("0") && !v_razonsocial.equals(""))
                    msg += "\nEmpresa: " + v_razonsocial;
                if (!v_descripcion.equals("0") && !v_descripcion.equals(""))
                    msg += "\nDescripción: " + v_descripcion;
                if (!v_indicacion.equals("0") && !v_indicacion.equals(""))
                    msg += "\nIndicación: " + v_indicacion;

                msg += "\nDestino: " + v_dir_destino;

                v_idServicioRuta = v_idServicio;

                servicio = msg;

                //poner el marcador del pasajero
                IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
                Drawable iconDrawable = ContextCompat.getDrawable(MainActivity.this, R.drawable.direction_down);
                Icon icon = iconFactory.fromDrawable(iconDrawable);

                //crear marcador del servicio y borrarlo cuando termine el sericio
                service_marker = map.addMarker(new MarkerViewOptions()
                        .position(new LatLng(v_latitudServicio, v_longitudServicio))
                        .icon(icon)
                        .title("Servicio")
                        .snippet(servicio)
                        .infoWindowAnchor(0.5f, 0.5f)
                        .flat(true));

                v_servicio = true;

                bsdFragment_ServiceArrival = BsModalServiceArrival.newInstance();
                bsdFragment_ServiceArrival.show(getSupportFragmentManager(), "bsdFragment_ServiceArrival");

                btnPrincipal.setBackgroundColor(getResources().getColor(R.color.primary_dark));

                btnPrincipal.setText(Constants.btnPpal.MOSTRAR_DIRECCION);
                btnPrincipal_text = btnPrincipal.getText().toString();
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    void recibe_confirmacion_mensaje(JSONObject jObj){
        try{
            datasource.deleteId(jObj.getString("slt"));
            datasource.update_postgres_id(jObj);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    void recibe_mensaje(JSONObject jObj){
        try {
            String msg = jObj.getString("desc");
            v_idmensaje = jObj.getString("id");
            if (msg.equals("25")) {

                ubicacion = v_latitud + "|" + v_longitud + "|" + v_velocidad + "|" + v_precision;
                networktask.SendDataToNetwork("25|" + ubicacion);

                v_ubicacion = 0;
            } else if (msg.equals("27")) {

                if (vibrar != null)
                    vibrar.cancel();

                if(cdt!=null)
                    cdt.cancel();

                // al cancelar servicio se debe detener la tarificacion
                if (myTimerTarificador != null)
                    myTimerTarificador.cancel();
                if (bsdFragment_Tarificacion != null)
                    bsdFragment_Tarificacion.dismiss();
                p_distancia = 0;
                v_idServicioRuta = "0";
                pendiente = false;
                v_enservicio = false;
                // vaciar ruta en el sqlite
                datasource.create("", "ruta");
                total = 0;

                wakeup();
                confirmando = false;
                pulsaDisponible = false;
                if (dlg != null) {
                    dlg.dismiss();
                }
                callAlertBox.showMyAlertBox("Servicio Cancelado por Usuario");
                btnPrincipal.setText(Constants.btnPpal.PONERME_DISPONIBLE);
                btnPrincipal_text = Constants.btnPpal.PONERME_DISPONIBLE;
                datasource.updateServtipoCancela("5");
                datasource.updateServEstado(5);
                v_cancelado = true;
                datasource.updateRegistros("servicio");

                if(bsdFragment_ServiceArrival!=null)
                    bsdFragment_ServiceArrival.dismiss();

                if(bsdFragment_ServiceAproval!=null)
                    bsdFragment_ServiceAproval.dismiss();

                ponerme_ocupado();
            } else {
                wakeup();

                if (msg.contains("PANICO: UNIDAD ")) {
                    if (!v_sonando) {
                        v_sonando = true;
                        //vibrar = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        //vibrar.vibrate(2000);
                        mpSplash = MediaPlayer.create(getApplicationContext(), R.raw.siren);
                        mpSplash.start();
                    }
                } else {
                    //mpSplash = MediaPlayer.create(getApplicationContext(), R.raw.mensaje);
                    //mpSplash.start();
                    try {
                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                        r.play();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    datasource.create_mensaje(jObj.toString(), 0);

                    if(getFragmentRefreshListener()!=null){
                        getFragmentRefreshListener().onRefresh(jObj);
                    }
                    if(getFragmentRefreshListener_chats()!=null){
                        getFragmentRefreshListener_chats().onRefresh(jObj);
                    }

                    TextView badge_textView = (TextView) findViewById(R.id.badge_textView);
                    int cantidad = Inicio_sesion.datasource.cant_mensajes_sin_leer();
                    if(cantidad > 0) {
                        badge_textView.setVisibility(View.VISIBLE);
                        badge_textView.setText(String.valueOf(cantidad));
                    }else{
                        badge_textView.setVisibility(View.GONE);
                    }

                    //FragmentManager fm = getSupportFragmentManager();
                    //Fragment currentFragment = fm.findFragmentById(R.id.flContent);
                    //if (currentFragment instanceof Fragment_conversacion) {

                    //TODO mostrar notificacion
                    //handleNotification(jObj.getString("desc"));
                    //handleDataMessage(jObj);
                    //callAlertBox.showMyAlertBox_msg(jObj);

                    //Drawable d = getResources().getDrawable(R.drawable.ic_menu);

                    /*Button btninfo = (Button) findViewById(R.id.btn_info);

                    Drawable image = mContext.getResources().getDrawable( R.drawable.ic_info_red);
                    int h = image.getIntrinsicHeight();
                    int w = image.getIntrinsicWidth();
                    image.setBounds( 0, 0, w, h );
                    btninfo.setCompoundDrawables(null, null, image, null);*/

                }

                if (dlg != null) {
                    dlg.dismiss();
                }

            }
            networktask.SendDataToNetwork("07|" + v_idmensaje);
            v_idmensaje = null;

            datasource.updateRegistros("mensaje");
        }catch (JSONException e){
            e.printStackTrace();
        }

    }

    protected void recibeMsgPago(){

        v_disponible = false;
        v_servicio = false;
        v_temporizador = 0;
        v_tarificando = false;
        fecha = null;

        map.clear();
        CameraPosition INIT = new CameraPosition.Builder().target(new LatLng(v_latitud, v_longitud)).zoom(16F)
                .bearing(300F) // orientation
                .tilt(50F) // viewing angle
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(INIT));

        if (myTimerTarificador != null)
            myTimerTarificador.cancel();

        if(bsdFragment_Tarificacion != null)
            bsdFragment_Tarificacion.dismiss();

        p_distancia = 0;
        v_idServicioRuta = "0";
        pendiente = false;
        v_enservicio = false;
        // vaciar ruta en el sqlite
        datasource.create("", "ruta");

        total = 0;
        datasource.updateServEstado(0);
        btnPrincipal.setText(Constants.btnPpal.PONERME_DISPONIBLE);
        btnPrincipal_text = Constants.btnPpal.PONERME_DISPONIBLE;
        //tan pronto llega la confirmacion de pago
        //volvemos a dejar el estado del telefono en modo normal
        datasource.create("normal", "estado_telefono");
        datasource.create("", "id_servicio");
    }

    private void comando_69(){
        v_clave = null;
        servicio = "";
        confirmando = false;
        pulsaDisponible = false;
        toast("Servicio Cumplido: Confirmado");
        datasource.updateServEstado(0);
        v_idServicio = "";
        pendiente = false;
        datasource.updateRegistros("servicio");
        //ahora si cambiar la pantalla
        ponerme_ocupado();
        v_enservicio = false;
        btnPrincipal.setText(Constants.btnPpal.PONERME_DISPONIBLE);
        btnPrincipal_text = Constants.btnPpal.PONERME_DISPONIBLE;

        datasource.create("", "vale_cedula");
        datasource.create("", "vale_valor");
        datasource.create("", "vale_servicio");
        datasource.create("", "vale_id");
        v_servicio = false;
        v_idServicioRuta = "0";
        datasource.create("normal", "estado_telefono");
        datasource.create("", "id_servicio");
        v_idServicio = "";
        v_ruteo = false;
        if (myTimerTarificador != null)
            myTimerTarificador.cancel();

        recibeMsgPago();

        //abrimos el fragment de calificacion
        Class fragmentClass = RatingFragment.class;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //handle QR result
        log("onActivityResult");
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                try {
                    String encripted = result.getContents();
                    //id, nombre, documento, valor
                    //el estado del vale debe ser consultado
                    //{"cmd":"consulta_estado","estado":"SIN USAR","codigo":"19","empresa":"Baxter","vale_id":"7","vale_nombre":"Martin Figueroa","vale_documento":"94536238","vale_valor":0}
                    //log("QR " + encripted);
                    consultar_vale_estado(encripted);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    static void espere() {
        espere = ProgressDialog.show(mContext, "Consultando", "Un momento por favor...", true);
    }

    private void consultar_vale_estado(final String encripted){
        //consultar_vale_estado
        espere();
        RequestQueue queue = Volley.newRequestQueue(mContext);

        String url = "https://www.servidor.com.co/gateway/Vale_virtual/consulta_estado";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        log("consultar_vale_estado " + response);
                        termine();
                        consultar_vale_estado_response(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                log("consultar_vale_estado " + error.toString());
                termine();
                callAlertBox.showMyAlertBox("Unidad sin conexion\n" + error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("empresa", empresa);
                params.put("servicio", v_idServicioRuta);
                params.put("encripted", encripted);
                return params;
            }
        };
        int socketTimeout = 50000; //50 segundos de timeout para que no envie doble la peticion
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        postRequest.setRetryPolicy(policy);
        queue.add(postRequest);

    }

    private void consultar_vale_estado_response(String response){
        try {
            //parsear la respuesta //{"cmd":"consulta_estado","estado":"SIN USAR","codigo":"19","empresa":"Baxter","vale_id":"7","vale_nombre":"Martin Figueroa","vale_documento":"94536238","vale_valor":0}
            final JSONObject jObj2 = new JSONObject(response);
            String estado_codigo = jObj2.getString("codigo");
            String estado_nombre = jObj2.getString("estado");
            String empresa_de_vale = jObj2.getString("empresa");
            if (estado_codigo.equals("19")) {//VALIDO

                //si esta en servicio, debe ingresar la informacion
                if (v_enservicio) {
                    ponerme_ocupado();
                    v_enservicio = false;
                    // comienza a rutear
                    v_ruteo = true;
                    String cedula = jObj2.getString("vale_documento");
                    String vale_valor = jObj2.getString("vale_valor");
                    String last4 = cedula.substring(cedula.length() - 4);
                    vale_id = jObj2.getString("vale_id");
                    log("Creando ultimos 4 digitos de la cedula: ." + last4 + ".");
                    //creacion en la BD de los datos
                    datasource.create(last4, "vale_cedula");
                    datasource.create(vale_valor, "vale_valor");
                    datasource.create(v_idServicio, "vale_servicio");
                    datasource.create(vale_id, "vale_id");
                    v_ubicacion = 0;

                    comienza_tarificador();
                } else {
                    Alert_estadoVale = new MaterialStyledDialog(MainActivity.this)
                            .setTitle("Vale Electrónico")
                            .setDescription("Estado del Vale: " + estado_nombre + "\n Empresa: " + empresa_de_vale + ".")
                            .setIcon(R.mipmap.ic_launcher)
                            .setHeaderColor(R.color.mapboxDenim)
                            .withDivider(true)
                            .setPositive("Iniciar Servicio", new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    if (v_servicio) {//si esta en servicio
                                        //si esta en un servicio se debe dar cumplir al servicio, arrancar temporizador,
                                        //actualizar el estado del vale, poner el id del vale en el servicio
                                        try {
                                            iniciar_servicio_qr(jObj2);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        //si no esta en un servicio, el servicio se debe crear el servicio, arrancar temporizador,
                                        //actualizar el estado del vale, poner el id del vale en el servicio
                                        crear_servicio_qr(jObj2);
                                    }
                                }
                            })
                            .setNegative("Cancelar", new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            })
                            .show();
                }
            } else {
                Alert_estadoVale = new MaterialStyledDialog(MainActivity.this)
                        .setTitle("Vale Electrónico")
                        .setDescription("Estado del Vale: " + estado_nombre)
                        .setIcon(R.mipmap.ic_launcher)
                        .setHeaderColor(R.color.mapboxDenim)
                        .withDivider(true)
                        .setNegative("Cancelar", new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            }
                        })
                        .show();
            }

            //countdown timer
            new CountDownTimer(15000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    //nothing to do
                }
                @Override
                public void onFinish() {
                    if(Alert_estadoVale!=null)
                    Alert_estadoVale.dismiss();
                }
            }.start();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void pretarificador(String response, JSONObject jObj) throws JSONException {
        JSONObject jObj2 = new JSONObject(response);//response = {"cmd":"crear_servicio_qr","servicio_id":"3771334"}
        v_idServicio = jObj2.getString("servicio_id");//jObj = {"codigo":"19","cmd":"consulta_estado","estado":"SIN USAR","vale_documento":"94536238","vale_nombre":"Martin  Salazar","empresa":"BAXTER","vale_valor":0,"vale_id":"77"}
        v_idServicioRuta = v_idServicio;
        String cedula = jObj.getString("vale_documento").trim();// este trim es importante porque el documento podria ir con un espacio
        String vale_valor = jObj.getString("vale_valor");
        String qr_id = jObj.getString("vale_id");
        String last4 = cedula.substring(cedula.length() - 4);
        log("Creando ultimos 4 digitos de la cedula: ." + last4 + ".");
        //creacion en la BD de los datos
        datasource.create(last4, "vale_cedula");
        datasource.create(vale_valor, "vale_valor");
        datasource.create(v_idServicio, "vale_servicio");
        datasource.create(qr_id, "vale_id");
        v_ubicacion = 0;
    }

    void iniciar_servicio_qr(final JSONObject jObj) throws JSONException {
        espere();
        //iniciar_servicio_qr
        final String vale_id = jObj.getString("id");
        log(vale_id);
        com.android.volley.RequestQueue queue = Volley.newRequestQueue(mContext);

        String url = "https://www.servidor.com.co/credibancoCheckout/Vale_virtual/iniciar_servicio_qr";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        termine();
                        iniciar_servicio_qr_response(jObj,response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                log(error.toString());
                termine();
                callAlertBox.showMyAlertBox("Unidad sin conexion\n" + error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("empresa", empresa);
                params.put("vale_id", vale_id);
                params.put("servicio_id", v_idServicio);
                params.put("nickname", Inicio_sesion.v_identificador+"");
                params.put("latitud", v_latitud + "");
                params.put("longitud", v_longitud + "");
                return params;
            }
        };
        int socketTimeout = 50000; //50 segundos de timeout para que no envie doble la peticion
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        postRequest.setRetryPolicy(policy);
        queue.add(postRequest);
    }

    void iniciar_servicio_qr_response(JSONObject jObj,String response){
        //poner la unidad como si hubiese cumplido la carrera

        if (!v_desconexion) {
            try {
                log(response);
                final String vale_id = jObj.getString("id");
                //id, nombre, documento, valor
                String cedula = jObj.getString("documento");
                String vale_valor = jObj.getString("valor");
                String last4 = cedula.substring(cedula.length() - 4);
                log("Creando ultimos 4 digitos de la cedula: ." + last4 + ".");
                //creacion en la BD de los datos
                datasource.create(last4, "vale_cedula");
                datasource.create(vale_valor, "vale_valor");
                datasource.create(v_idServicio, "vale_servicio");
                datasource.create(vale_id, "vale_id");
                v_ubicacion = 0;
                //remover marcador
                if (service_marker != null) {
                    map.removeMarker(service_marker);
                    service_marker = null;
                }
                //comienza_tarificador iniciar_servicio_qr
                comienza_tarificador();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            callAlertBox.showMyAlertBox("Unidad sin conexion");
        }
    }

    void crear_servicio_qr(final JSONObject jObj) {
        espere();
        //{"codigo":"19","cmd":"consulta_estado","estado":"SIN USAR","vale_documento":"94536238","vale_nombre":"Martin  Salazar","empresa":"BAXTER","vale_valor":0,"vale_id":"77"}
        RequestQueue queue = Volley.newRequestQueue(mContext);
        try{
            final String qr_id = jObj.getString("vale_id");
            String url = "https://www.servidor.com.co/credibancoCheckout/Vale_virtual/crear_servicio_qr_especial";
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            log(response);
                            termine();
                            try {
                                pretarificador(response,jObj);
                                //comienza_tarificador crear_servicio_qr
                                comienza_tarificador();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    log(error.toString());
                    termine();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("empresa", empresa);
                    params.put("vale_id", qr_id);
                    params.put("nickname", Inicio_sesion.v_identificador+"");
                    params.put("latitud", v_latitud + "");
                    params.put("longitud", v_longitud + "");
                    return params;
                }
            };
            int socketTimeout = 50000; //50 segundos de timeout para que no envie doble la peticion
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            postRequest.setRetryPolicy(policy);
            queue.add(postRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void en_sitio(){
            if (!v_EnSitio) {
                datasource.create("24|" + v_latitud + "|" + v_longitud,Constants.ACTION.TRAMA_X_ENVIAR);
                //networktask.SendDataToNetwork("24|" + v_latitud + "|" + v_longitud);
                v_ubicacion = 0;
                running2 = true;
                v_tmax = true;

                Location loc1 = new Location("");
                loc1.setLatitude(v_latitud);
                loc1.setLongitude(v_longitud);

                Location loc2 = new Location("");
                loc2.setLatitude(v_latitudServicio);
                loc2.setLongitude(v_longitudServicio);

                distancia = loc1.distanceTo(loc2);

                toast("Enviando En Sitio");
            } else {//cuando ya este en sitio, puede abrir el cuadro de Novedades
                // Mostrar enviar msg predeterminado
                ArrayList<String> list = new ArrayList<>();
                list.add("Llamar al Usuario");
                list.add("Confirmar Nombre");
                list.add("Confirmar Apartamento");
                list.add("Confirmar Casa");
                final AlertDialog.Builder singlechoicedialog1 = new AlertDialog.Builder(mContext);
                final CharSequence[] Report_items1 = list.toArray(new CharSequence[list.size()]);
                singlechoicedialog1.setTitle("Enviar a Central:");
                singlechoicedialog1.setCancelable(true);
                singlechoicedialog1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // cancel code here
                    }
                });
                singlechoicedialog1.setSingleChoiceItems(Report_items1, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        // get selected value
                        String value = Report_items1[item].toString();
                        System.out.println("Selected position::" + value);
                        networktask.SendDataToNetwork("23|" + value);
                        toast("Mensaje '" + value + "' Enviado");
                        dialog.dismiss();
                    }
                });
                AlertDialog alert_dialog1 = singlechoicedialog1.create();
                alert_dialog1.show();
            }
    }

    void rechazar_servicio_aceptado(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Desea Rechazar este Servicio?").setCancelable(false).setTitle("Confirmación")
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        flag = false;
                        v_servicio = false;
                        confirmando = true;
                            datasource.create("10|" + v_latitud + "|" + v_longitud,Constants.ACTION.TRAMA_X_ENVIAR);
                            //networktask.SendDataToNetwork("10|" + v_latitud + "|" + v_longitud);
                            v_ubicacion = 0;

                        ponerme_ocupado();
                        dialog.dismiss();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog alert;
        alert = builder.create();
        alert.show();
    }

    public void from_MapaPrincipalFragment(JSONObject jObj) {

        log("from_MapaPrincipalFragment " + jObj.toString());
        try {
            String comando = jObj.getString("cmd");
            if(comando.equals("mpf_1")){
                String detalle = jObj.getString("detalle");
                if(detalle.equals(Constants.btnPpal.PONERME_DISPONIBLE)) {
                    ponerme_disponible();
                }else if (detalle.equals(Constants.btnPpal.PONERME_OCUPADO)){
                    if (!v_servicio) {
                        datasource.create("03|" + v_latitud + "|" + v_longitud,Constants.ACTION.TRAMA_X_ENVIAR);
                        ponerme_ocupado();
                    }
                }else if (detalle.equals(Constants.btnPpal.MOSTRAR_DIRECCION)){

                    if(bsdFragment_ServiceArrival!=null)
                        bsdFragment_ServiceArrival.dismiss();

                    bsdFragment_ServiceArrival = BsModalServiceArrival.newInstance();
                    bsdFragment_ServiceArrival.show(getSupportFragmentManager(), "bsdFragment_ServiceArrival");
                }else if (detalle.equals(Constants.btnPpal.MOSTRAR_TARIFICACION)){

                    if(bsdFragment_Tarificacion!=null)
                        bsdFragment_Tarificacion.dismiss();

                    bsdFragment_Tarificacion = BsModalTarificacion.newInstance();
                    bsdFragment_Tarificacion.show(getSupportFragmentManager(), "bsdFragment_Tarificacion");
                } else if (detalle.equals(Constants.btnPpal.MOSTRAR_ACEPTACION)) {

                    if(bsdFragment_ServiceAproval!=null)
                        bsdFragment_ServiceAproval.dismiss();

                    bsdFragment_ServiceAproval = BsModalServiceAproval.newInstance();
                    bsdFragment_ServiceAproval.show(getSupportFragmentManager(), "bsdFragment_ServiceAproval");
                }
                btnPrincipal_text = detalle;
            }
        }catch (JSONException e){
            e.printStackTrace();
        }

    }

    private void recibe_servicio(JSONObject jObj){
        jServicio = jObj;
        datasource.createRegistro(jObj.toString(), "servicio", 1);
        datasource.create("08|"+ v_latitud + "|" + v_longitud,Constants.ACTION.TRAMA_X_ENVIAR);
        servicio = servicioAceptado(jObj);

        //poner el marcador del pasajero
        IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
        Drawable iconDrawable = ContextCompat.getDrawable(MainActivity.this, R.drawable.direction_down);
        Icon icon = iconFactory.fromDrawable(iconDrawable);

        //crear marcador del servicio y borrarlo cuando termine el sericio
        service_marker = map.addMarker(new MarkerViewOptions()
                .position(new LatLng(v_latitudServicio, v_longitudServicio))
                .icon(icon)
                .title("Servicio")
                .snippet(servicio)
                .infoWindowAnchor(0.5f, 0.5f)
                .flat(true));

        v_servicio = true;

        bsdFragment_ServiceArrival = BsModalServiceArrival.newInstance();
        bsdFragment_ServiceArrival.show(getSupportFragmentManager(), "bsdFragment_ServiceArrival");

        btnPrincipal.setText(Constants.btnPpal.MOSTRAR_DIRECCION);
        btnPrincipal_text = btnPrincipal.getText().toString();

    }

    private void pregunta_aceptacion_servicio(JSONObject jObj) {
        try {
            if (!v_ocupado) {
                en_aceptacion_servicio = true;
                wakeup();

                v_disponible = false;
                String sitio, barrio, razon_social, descripcion;

               sitio = jObj.getString("dir");

                v_direccion_aproval = sitio;
                v_ttsDirBarr = sitio;

                barrio = jObj.getString("barrio");
                razon_social = jObj.getString("empresa");
                descripcion = jObj.getString("desc");
                v_tipoServicio = jObj.getString("tipo_serv");
                aceptando = Integer.parseInt(jObj.getString("t_acepta"));

                vibrar.vibrate(aceptando * 1000);

                if (barrio.equals("0") || barrio.equals(""))
                    barrio = "";

                if (descripcion.equals("0") || descripcion.equals(""))
                    descripcion = "";
                else
                    descripcion = "\nDESCRIPCIÓN: " + descripcion;

                if (razon_social.equals("0") || razon_social.equals(""))
                    razon_social = "";
                else
                    razon_social = "\nEMPRESA: " + razon_social;

                v_info_adicional_aproval = barrio + razon_social + descripcion;

                if (v_tts) {
                    String nuevoMsg = v_ttsDirBarr;
                    nuevoMsg = nuevoMsg.replaceAll(" - ", " número ");
                    nuevoMsg = nuevoMsg.replaceAll(" # ", " número ");
                    nuevoMsg = nuevoMsg.replaceAll("CL ", "calle ");
                    nuevoMsg = nuevoMsg.replaceAll("CR ", "carrera ");
                    nuevoMsg = nuevoMsg.replaceAll("TV ", "transversal ");
                    nuevoMsg = nuevoMsg.replaceAll("DG ", "diagonal ");
                    nuevoMsg = nuevoMsg.replaceAll("PJ ", "pasaje ");
                    nuevoMsg = nuevoMsg.replaceAll("CJ ", "callejón ");
                    nuevoMsg = nuevoMsg.replaceAll("AV ", "avenida ");
                    nuevoMsg = nuevoMsg.replaceAll("GEOSERVICIO", "geo servicio");
                    tts.speak(nuevoMsg.toLowerCase(), TextToSpeech.QUEUE_FLUSH, null);
                }

                btnPrincipal.setText(Constants.btnPpal.MOSTRAR_ACEPTACION);
                btnPrincipal_text = btnPrincipal.getText().toString();

                flag = true;
                if (dlg != null) {
                    dlg.dismiss();
                }

                try {
                    bsdFragment_ServiceAproval = BsModalServiceAproval.newInstance();
                    bsdFragment_ServiceAproval.show(getSupportFragmentManager(), "bsdFragment_ServiceAproval");
                } catch (IllegalStateException e) {
                    Log.d("ABSDIALOGFRAG", "Exception", e);
                }

                if(tempoView!=null)
                    tempoView.setText(aceptando +" segs");

                cdt = new CountDownTimer(aceptando * 1000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                        en_aceptacion_servicio = true;

                        String secs;
                        if ((millisUntilFinished / 1000) < 10) {
                            secs = "0" + (millisUntilFinished / 1000);
                            //t_view.setText(msg + "\nTiempo para aceptar: 00:" + secs + " segs");
                            if(tempoView!=null)
                                tempoView.setText(secs + " segs");
                        /*if (secs.equals("01")) {
                            alert.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                            alert.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
                        }*/
                        } else {
                            if(tempoView!=null)
                                tempoView.setText((millisUntilFinished / 1000) + " segs");
                            //t_view.setText(msg + "\nTiempo para aceptar: 00:" + (millisUntilFinished / 1000) + " segs");
                        }

                        aceptando = (int)(millisUntilFinished / 1000);
                    }

                    @Override
                    public void onFinish() {
                        en_aceptacion_servicio = false;
                        if (!v_servicio && flag) {
                            //alert.dismiss();
                            try {
                                JSONObject jsonObj = new JSONObject();
                                jsonObj.put("cmd", "15");
                                envia_json(jsonObj);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            v_ubicacion = 0;
                            pulsaDisponible = false;
                            if (vibrar != null)
                                vibrar.cancel();
                            if (mpSplash != null)
                                mpSplash.stop();
                            callAlertBox.showMyAlertBox("Tiempo agotado.\nEl servicio no ha sido aceptado");
                            ponerme_ocupado();
                            flag = false;
                            bsdFragment_ServiceAproval.dismiss();
                        }
                    }
                }.start();
            } else {
                if (cdt != null)
                    cdt.cancel();
                networktask.SendDataToNetwork("14|" + v_latitud + "|" + v_longitud);
                v_ubicacion = 0;
                if (vibrar != null)
                    vibrar.cancel();
                if (mpSplash != null)
                    mpSplash.stop();
                ponerme_ocupado();
                flag = false;
                pulsaDisponible = false;
                v_servicio = false;
            }

        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private String servicioAceptado(JSONObject jObj) {
        log("servicioAceptado " + jObj.toString());
        String msg = "";
        try {
            v_resultado = null;
            String v_direccion, v_barrio, v_indicacion, v_unidad, v_descripcion, v_nombre, v_razonsocial;
            //02|id_servicio|clave|direccion|barrio|latitud|longitud|unidad|descripcion|nombre|tipo_servicio|empresa_vale|tiempo para cumplir|indicacion

            v_idServicio = jObj.getString("servicio");
            v_clave = jObj.getString("clave");
            v_direccion = jObj.getString("dir");
            v_barrio = jObj.getString("barrio");
            v_latitudServicio = Double.valueOf(jObj.getString("lat")).doubleValue();
            v_longitudServicio = Double.valueOf(jObj.getString("lng")).doubleValue();
            v_unidad = jObj.getString("unidad");
            v_descripcion = jObj.getString("desc");
            v_nombre = jObj.getString("nombre");
            v_razonsocial = jObj.getString("empresa");
            v_temporizador = Integer.parseInt("0");
            v_indicacion = jObj.getString("indic");

            v_direccion_servicio = v_direccion;

            String v_dir_destino = jObj.getString("dir_d");

            if (!v_barrio.equals("0") && !!v_barrio.equals(""))
                msg += "Barrio: " + v_barrio;
            if (!v_nombre.equals("0") && !v_nombre.equals(""))
                msg += "\nNombre: " + v_nombre;
            if (!v_unidad.equals("0") && !v_unidad.equals(""))
                msg += "\nUnidad: " + v_unidad;
            if (!v_razonsocial.equals("0") && !v_razonsocial.equals(""))
                msg += "\nEmpresa: " + v_razonsocial;
            if (!v_descripcion.equals("0") && !v_descripcion.equals(""))
                msg += "\nDescripción: " + v_descripcion;
            if (!v_indicacion.equals("0") && !v_indicacion.equals(""))
                msg += "\nIndicación: " + v_indicacion;

            msg += "\nDestino: " + v_dir_destino;

            v_idServicioRuta = v_idServicio;
        }catch(JSONException e){
            e.printStackTrace();
        }
        return msg;
    }

    public static void servicios_ubicaciones(String trama){
        networktask.SendDataToNetwork(trama);
    }

    private void ponerme_ocupado() {
        v_turno = "";
        v_pista = "";
        //InputMethodManager imm = (InputMethodManager) this.getSystemService(Service.INPUT_METHOD_SERVICE);
        //imm.hideSoftInputFromWindow(txtClave.getWindowToken(), 0);
        v_disponible = false;
        v_servicio = false;
        v_temporizador = 0;
        fecha = null;

        //borrar marcador del servicio
        if (service_marker != null) {
            map.removeMarker(service_marker);
            service_marker = null;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnPrincipal.setBackgroundColor(getResources().getColor(R.color.accent));
                btnPrincipal.setText(Constants.btnPpal.PONERME_DISPONIBLE);
                btnPrincipal_text=btnPrincipal.getText().toString();
            }
        });
    }

    void ponerme_disponible(){
        if (!v_enservicio) {
            v_ocupado = false;
            if (!v_servicio) {
                if (!v_disponible) {
                    if (confirmando) {
                        toast("Confirmando servicio, intenta nuevamente");
                    } else {
                        ponerme_ocupado();
                        if (level > 5 || isCharging || level <= 0) {
                            coordenadas = false;
                            pendiente = false;
                            v_tmax = false;

                            if (!v_desconexion) {
                                btnPrincipal.setText(Constants.btnPpal.VALIDANDO);
                                btnPrincipal_text = Constants.btnPpal.VALIDANDO;

                                try {
                                    JSONObject jsonObj = new JSONObject();
                                    jsonObj.put("cmd", "16");
                                    envia_json(jsonObj);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                v_ubicacion = 0;
                                v_listaPendiente = false;
                            }//else {
                               // callAlertBox.showMyAlertBox("Imposible continuar, Unidad sin conexión");
                            //}
                        } else {
                            callAlertBox.showMyAlertBox("Imposible Continuar, el nivel de su batería es muy Bajo (" + level + "%)");

                            //vibrar = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            //vibrar.vibrate(400);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            FragmentManager fm = getSupportFragmentManager();
            Fragment currentFragment = fm.findFragmentById(R.id.flContent);
            if (currentFragment instanceof Fragment_conversacion) {
                Class fragmentClass = Fragment_conversaciones.class;
                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if (currentFragment instanceof Fragment_conversaciones) {
                Class fragmentClass = MapaPrincipalFragment.class;
                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else {
                if (!(v_servicio || pendiente)) {
                    salir();
                }
            }
        }
    }

    protected void salir() {
        if (v_servicio) {
            callAlertBox.showMyAlertBox("Vehículo en Servicio");
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Salir de la aplicación?").setCancelable(false)
                    .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            onDestroy();
                        }
                    }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    public static String[] splitTotokens(String line, String delim) {
        String s = line;
        int i = 0;

        while (s.contains(delim)) {
            s = s.substring(s.indexOf(delim) + delim.length());
            i++;
        }
        String token, remainder;
        String[] tokens = new String[i];

        for (int j = 0; j < i; j++) {
            token = line.substring(0, line.indexOf(delim));
            // System.out.print("#" + token + "#");
            tokens[j] = token;
            remainder = line.substring(line.indexOf(delim) + delim.length());
            // System.out.println("#" + remainder + "#");
            line = remainder;
        }

        return tokens;
    }

    public static void onLocationChanged(Location location) {
        //log("onLocationChanged " + location.toString());
        String str = String.format("%.5f", location.getLatitude() );
        str = str.replace(",", ".");
        v_latitud = Double.parseDouble(str);
        str = String.format("%.5f", location.getLongitude() );
        str = str.replace(",", ".");
        v_longitud = Double.parseDouble(str);
        v_fechaGPS = location.getTime() + "";
        v_precision = (int) location.getAccuracy();
        v_bearing = (int) location.getBearing();

        if (!v_idServicioRuta.equals("0") && v_primera_lectura_gps) {

            try {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("su_id", "0");
                jsonObj.put("su_latitud", v_latitud);
                jsonObj.put("su_longitud", v_longitud);
                jsonObj.put("su_precio", total);
                jsonObj.put("su_distancia", (int) v_distanciaTotal);
                jsonObj.put("su_tiempoespera", "f");
                jsonObj.put("su_orientacion", v_bearing);
                jsonObj.put("su_velocidad", v_velocidad);
                jsonObj.put("su_servicio", v_idServicioRuta);
                jsonObj.put("su_fechagps", v_fechaGPS);
                jsonObj.put("su_precision", v_precision);
                jsonObj.put("su_esbuena", 1);
                datasource.createUbicacion(jsonObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            v_latitudPrevia = v_latitud;
            v_longitudPrevia = v_longitud;
        }

        if (map != null) {

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(v_latitud, v_longitud))
                    .bearing(location.getBearing())
                    .build();

            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 3000, null);
        }
    }

    public void setGpsLogger(GPSLogger l) {
        this.gpsLogger = l;
    }

    public GPSLogger getGpsLogger() {
        return gpsLogger;
    }

    private static void log(String s) {
        Log.d(MainActivity.class.getSimpleName(), "######" + s + "######");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());
        if(en_aceptacion_servicio) {
            if (bsdFragment_ServiceAproval != null)
                bsdFragment_ServiceAproval.dismiss();

            try {
                bsdFragment_ServiceAproval = BsModalServiceAproval.newInstance();
                bsdFragment_ServiceAproval.show(getSupportFragmentManager(), "bsdFragment_ServiceAproval");
            } catch (IllegalStateException e) {
                Log.d("ABSDIALOGFRAG", "Exception", e);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return false;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Class fragmentClass=null;

        if (id == R.id.nav_servicios) {
            //fragmentClass = ServiciosFragment.class;
            fragmentClass = Fragment_swipe_servicios.class;
        } else if (id == R.id.nav_perfil) {
            fragmentClass = ProfileFragment.class;
        } else if (id == R.id.nav_chat) {
            //fragmentClass = HistorialChatFragment.class;
            fragmentClass = Fragment_conversaciones.class;

            /*Button btninfo = (Button) findViewById(R.id.btn_info);
            Drawable image = mContext.getResources().getDrawable( R.drawable.ic_info);
            int h = image.getIntrinsicHeight();
            int w = image.getIntrinsicWidth();
            image.setBounds( 0, 0, w, h );
            btninfo.setCompoundDrawables(null, null, image, null);*/

        } else if (id == R.id.nav_salir) {
            salir();
            return true;
        } else if (id == R.id.nav_acerca_de) {
            new MaterialStyledDialog(MainActivity.this)
                    .setTitle(getString(R.string.app_name))
                    .setDescription(getString(R.string.info_dialog_description) + " " + versionNumber)
                    .setIcon(R.mipmap.ic_launcher)
                    .setHeaderColor(R.color.primary)
                    .withDivider(true)
                    .setPositive("White Cloud", new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse("http://www.whitecloud.com.co/"));
                            startActivity(i);
                        }
                    })
                    .setNegative("Cerrar", new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        }
                    })
                    .show();
        }   else {
            fragmentClass = MapaPrincipalFragment.class;
        }

        if(fragmentClass!=null) {
            try {
                fragment = (Fragment) fragmentClass.newInstance();
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        in_conversacion=false;
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
       log("onFragmentInteraction");
    }

    @Override
    public void onListFragmentInteraction(DummyContentDirecciones.DummyItem item) {
        log("onListFragmentInteraction DummyContentDirecciones");
    }

    @Override
    public void onListFragmentInteraction(DummyContentServicios.DummyItem item) {
        log("onListFragmentInteraction DummyContentServicios item.content "  + item.content);
        log("onListFragmentInteraction DummyContentServicios item.details "  + item.details);
        log("onListFragmentInteraction DummyContentServicios item.id "  + item.id);
    }

    @Override
    public void onListFragmentInteraction(DummyContentHistorialChat.DummyItem item) {
        log("onListFragmentInteraction DummyContentHistorialChat");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        log("onDestroy");

        try {
            if (SocketTask.nsocket != null)
                SocketTask.nsocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent stopIntent = new Intent(MainActivity.this, GPSLogger.class);
        stopService(stopIntent);

        // stop sensors
        sensorListener.unregister();

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        if(tt_handler!=null && runnable!=null)
            tt_handler.removeCallbacks(runnable);

        Inicio_sesion.salir();

    }

    @Override
    public void onSocketConnected(String data) {
        //onSocketConnected
        log("onSocketConnected");

        if(!bool_KeepAliveGPS) {
            bool_KeepAliveGPS=true;

            runnable = new Runnable() {
                public void run() {
                    afficher();
                    tt_handler.postDelayed(runnable, 1000);
                }
            };

            tt_handler.postDelayed(runnable, 1000);
        }
    }

    void afficher(){
        v_countAlive++;

        if(v_servicio)
            alive = 5;
        else
            alive = 35;

        if (v_countAlive >= alive) {
            v_countAlive = 0;
            if (networktask != null) {
                //envio de alive, cada 35 segundos, envia ubicacion
                networktask.SendDataToNetwork("18|" + v_latitud + "|" + v_longitud + "|" + v_precision);
            }
        }

        if (k >= reconector) {
            k = 0;

            if (v_desconexion) {
                v_desconexion = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {// reconecta si hay desconexión
                        if (rec)
                            conectate();
                    }
                });
            }
        }

        if (v_desconexion) {
            k++;

            if (g < 0)
                g = reconector;

            reconexion("1", g);

        } else {

            conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();

            if (activeNetwork != null && activeNetwork.isConnected()) {

            } else {
                v_desconexion = true;
                g = 0;
            }

        }

        //revisa si hay conexion para enviar las ubicaciones del recorrido
        if (!v_desconexion) {
            int cantidad = datasource.selectUbicacionPendientes();
            if (cantidad == 0) {
                try {
                    JSONObject jObj = datasource.select_tipo_para_enviar("registra_valor_servicio");
                    if (jObj.length() != 0) {
                        String valor = jObj.getString("descripcion");
                        if (!valor.equals("")) {
                            v_idServicioRuta = "0";
                            String vale_id = datasource.select("vale_id");
                            String servicio_id = datasource.select("vale_servicio");
                            datasource.create("69|" + servicio_id + "|" + empresa + "|" + v_latitud + "|" + v_longitud + "|" + vale_id + "|" + total, Constants.ACTION.TRAMA_X_ENVIAR);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            trama_x_enviar_contador++;
            if (trama_x_enviar_contador >= 2) {
                //envia tramas guardadas en sqlite (cumplir,cancelar,en sitio,rechazar, etc)
                String[] v_tipo = {Constants.ACTION.TRAMA_X_ENVIAR, "CU"};

                for (int x = 0; x < v_tipo.length; x++) {
                    final String trama_x_enviar = datasource.select_v2(v_tipo[x]);
                    if (!trama_x_enviar.equals("")) {
                        trama_x_enviar_contador = 0;
                        log("ENVIANDO TRAMA PENDIENTE " + trama_x_enviar);
                        networktask.SendDataToNetwork(trama_x_enviar);
                    }
                }

                /*

                String trama_x_enviar = datasource.select_v2(Constants.ACTION.TRAMA_X_ENVIAR);
                if (!trama_x_enviar.equals("")) {
                    trama_x_enviar_contador = 0;
                    log("ENVIANDO TRAMA PENDIENTE " + trama_x_enviar);
                    networktask.SendDataToNetwork(trama_x_enviar);
                }*/
            }
        }
    }

    public void conectate() {
        // PRIMERO VALIDAMOS QUE HAYA CONFIGURADO UN NUMERO DE UNIDAD PARA ESTA
        // TERMINAL SI NO HAY, SE ENVIA LA SOLICITUD DE CONFIGURACION
        // SI YA EXISTE SE ENVIAN LOS DATOS QUE EL USUARIO HA INGRESADO
        try {

            if (networktask != null)
                networktask.cancel(true);
            if (SocketTask.nsocket != null)
                SocketTask.nsocket.close();

            networktask = new SocketTask(Inicio_sesion.v_ip, Inicio_sesion.v_puerto, this);
            networktask.execute();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void reconexion(String incoming, int tiempo) {
        v_desconexion = true;
        log("reconexion " + incoming + ", tiempo " + tiempo);
    }

    public void IniciaSesion() {
        log("IniciaSesion");
        v_autenticado = true;
        log("v_disponible " + v_disponible);

        if (v_disponible) {
            v_disponible = false;
            if (toast != null)
                toast.cancel();
            ponerme_disponible();
        }
        //datasource.getAllRegs("servicio");
        //datasource.getAllRegs("mensaje");
        datasource.create(Inicio_sesion.usuario, "cedula");
        String estado_telefono = datasource.select("estado_telefono");
        if (estado_telefono.equals("en_servicio")) {
            v_idServicioRuta = datasource.select("id_servicio");
            v_idServicio = v_idServicioRuta;
            //si el estado es en_servicio resumimos la tarificacion en el estado que quedó
            v_estadoTarificacion = "RESUME";
            comienza_tarificador();
        }
    }

    private void comienza_tarificador() {
        if(!v_tarificando) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    mpSplash = MediaPlayer.create(getApplicationContext(), R.raw.ding);
                    mpSplash.start();

                    RequestQueue queue = Volley.newRequestQueue(mContext);

                    String url = "https://www.servidor.com.co/gateway/Cpp/get_tarifa";
                    StringRequest postRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // response
                            try {
                                log("comienza_tarificador " + response);//{"cmd":"get_tarifa","tpreciounidad":"96","tmetrounidad":"80","tespera":"50","tminima":"4700","tminimatermina":"4512","t_distancia_gps_error":"30","estado":"0","distancia":"0","valor":"0","tiempo":"0"}
                                JSONObject jObj = new JSONObject(response);
                                v_enservicio = true;
                                v_clave = null;
                                confirmando = false;
                                pulsaDisponible = false;
                                //toast("Iniciando Tarificación");
                                v_ruteo = true;
                                // valores dinamicos de tarificacion de acuerdo a la ciudad

                                //al llegar al servicio ponemos el estado del telefono como en_servicio
                                //solo para validar si es apagado durante un servicio activo que ya se encuentre tarificando
                                //y vuelven a prender el celular
                                datasource.create("en_servicio", "estado_telefono");
                                datasource.create(v_idServicioRuta, "id_servicio");

                                if(bsdFragment_ServiceArrival != null)
                                    bsdFragment_ServiceArrival.dismiss();

                                bsdFragment_Tarificacion = BsModalTarificacion.newInstance();
                                bsdFragment_Tarificacion.show(getSupportFragmentManager(), "bsdFragment_Tarificacion");

                                btnPrincipal.setText(Constants.btnPpal.MOSTRAR_TARIFICACION);

                                Tarificador.setActivity(MainActivity.this);
                                Tarificador thTemporizador = new Tarificador(jObj);
                                thTemporizador.start();

                                v_tarificando = true;

                                pendiente = true;

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            termine();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            log("comienza_tarificador " + error.toString());
                            termine();
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<>();
                            params.put("empresa", empresa);
                            params.put("idservicio", v_idServicioRuta);
                            params.put("estado", v_estadoTarificacion);
                            return params;
                        }
                    };
                    int socketTimeout = 50000; //50 segundos de timeout para que no envie doble la peticion
                    RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    postRequest.setRetryPolicy(policy);
                    queue.add(postRequest);
                }
            });
        }
    }

    static void termine() {
        if (espere != null)
            espere.dismiss();
    }

    public static void login() {

        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("cmd", "19");
            jsonObj.put("usuario", Inicio_sesion.usuario);
            jsonObj.put("tipo", "driver");
            jsonObj.put("clave", Inicio_sesion.clave);
            jsonObj.put("imei", Inicio_sesion.v_imei);
            jsonObj.put("nick", Inicio_sesion.v_identificador+"");
            jsonObj.put("iniciado", "si");
            jsonObj.put("veq_id", Inicio_sesion.v_vehiculo);
            envia_json(jsonObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void envia_json(JSONObject jObj) {
        try {
            jObj.put("lat", v_latitud + "");
            jObj.put("lng", v_longitud + "");
            jObj.put("date_gps", v_fechaGPS + "");
            String nsend = jObj.toString();
            networktask.SendDataToNetwork(nsend);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void wakeup() {
        if (v_prefIlumina) {
            // Waking up mobile if it is sleeping
            WakeLocker.acquire(getApplicationContext());
            // Releasing wake lock
            WakeLocker.release();
            // MUESTRA LA APP EN PRIMER PLANO
            Intent it = new Intent("intent.my.action");
            it.setComponent(new ComponentName(getApplicationContext().getPackageName(), MainActivity.class.getName()));
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(it);
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
    }

    private void toast(final String cadena) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (toast != null)
                    toast.cancel();
                toast = Toast.makeText(MainActivity.this, cadena, Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    @Override
    public void onInit(int i) {
        log("tts iniciando");
        if (i == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.getDefault());

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                log("Lenguaje No soportado");
                v_tts = false;
                // missing data, install it
				/*
				 * Intent installIntent = new Intent();
				 * installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA); startActivity(installIntent);
				 */
            }

        } else {
            // TTS no inciado
            log("tts no iniciado");
            v_tts = false;
            // missing data, install it
			/*
			 * Intent installIntent = new Intent();
			 * installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA); startActivity(installIntent);
			 */
        }

        networktask = new SocketTask(Inicio_sesion.v_ip, Inicio_sesion.v_puerto, this);
        networktask.execute();
    }

    @Override
    public void from_Fragment_swipe_servicios(JSONObject jObj) {
        log("from_Fragment_swipe_servicios: " + jObj.toString());
    }

    @Override
    public void from_Fragment_mensajes(JSONObject jObj) {
        log("from_Fragment_mensajes: " + jObj.toString());

        Bundle bundle = new Bundle();
        try {

            if(!jObj.getString("id").equals("0")) {
                String from_postgres = datasource.get_from_postgres_id(jObj.getString("id"));
                log("test " + from_postgres);

                JSONObject jObj2 = new JSONObject(from_postgres);

                if(jObj2.has("pic"))
                    jObj.put("user_pic", jObj2.getString("pic"));
                else
                    jObj.put("user_pic", jServicio.getString("user_pic"));

                jObj.put("user_id", jObj2.getString("origen"));
                jObj.put("user_name", jObj2.getString("origen_name"));
            }

            bundle.putString("json", jObj.toString());

            Class fragmentClass = Fragment_conversacion.class;
            fragment = (Fragment) fragmentClass.newInstance();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragment.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

    }

    @Override
    public void from_Fragment_mensaje(JSONObject jObj) {
        log("from_Fragment_mensaje: " + jObj.toString());
    }

    //fragment conversacion listener
    public FragmentRefreshListener getFragmentRefreshListener() {
        return fragmentRefreshListener;
    }

    public void setFragmentRefreshListener(FragmentRefreshListener fragmentRefreshListener) {
        this.fragmentRefreshListener = fragmentRefreshListener;
    }

    private FragmentRefreshListener fragmentRefreshListener;

    @Override
    public void onFragmentInteraction(JSONObject jObj) {
        log("from onFragmentInteraction Rating " + jObj);

        datasource.createRegistro(jObj.toString(), "CU", 0);

        //mostramos el mapa nuavemente
        Class fragmentClass = MapaPrincipalFragment.class;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface FragmentRefreshListener{
        void onRefresh(JSONObject jObj);
    }
    //end fragment conversacion listener

    //fragment conversacion listener
    public FragmentRefreshListener_chats getFragmentRefreshListener_chats() {
        return fragmentRefreshListener_chats;
    }

    public void setFragmentRefreshListener_chats(FragmentRefreshListener_chats fragmentRefreshListener) {
        this.fragmentRefreshListener_chats = fragmentRefreshListener;
    }

    private FragmentRefreshListener_chats fragmentRefreshListener_chats;

    public interface FragmentRefreshListener_chats{
        void onRefresh(JSONObject jObj);
    }
    //end fragment conversacion listener

    @Override
    public void callAlertBox_interface(JSONObject jObj) {
        log("callAlertBox_interface: " + jObj.toString());

        Bundle bundle = new Bundle();
        bundle.putString("json", jObj.toString());

        try {
            Class fragmentClass;
            fragmentClass = Fragment_conversacion.class;
            fragment = (Fragment) fragmentClass.newInstance();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragment.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

    }


}