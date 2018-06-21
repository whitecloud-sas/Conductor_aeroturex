package com.conductor.aeroturex;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TableRow;
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
import com.conductor.aeroturex.service.TaxiLujoService;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.conductor.aeroturex.dummy.DummyContentDirecciones;
import com.conductor.aeroturex.dummy.DummyContentHistorialChat;
import com.conductor.aeroturex.dummy.DummyContentServicios;
import com.conductor.aeroturex.old.Constants;
import com.conductor.aeroturex.old.Tarificador;
import com.conductor.aeroturex.old.WakeLocker;
import com.conductor.aeroturex.old.callAlertBox;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        DireccionesFragment.OnListFragmentInteractionListener,
        ServiciosFragment.OnListFragmentInteractionListener,
        HistorialChatFragment.OnListFragmentInteractionListener,
        ChatFragment.OnFragmentInteractionListener,
        AutocompleteAddressFragment.OnFragmentInteractionListener,
        EditProfilePicFragment.OnFragmentInteractionListener,
        TextToSpeech.OnInitListener,
        BsModalServiceArrival.On_BsModalServiceArrival_Listener,
        BsModalServiceAproval.On_BsModalServiceAproval_Listener,
        BsModalTarificacion.On_BsModalTarificacion_Listener,
        BsModalCobro.On_BsModalCobro_Listener,
        BsModalActions.On_BsModalActions_Listener,
        BsModalDocumentacion.On_BsModalServiceAproval_Listener,
        BsModalCancelations.On_BsModalCancelations_Listener,
        BsModalCumplir.On_BsModal_Listener,
        Fragment_swipe_servicios.On_Fragment_swipe_servicios_Listener,
        Fragment_conversaciones.On_Fragment_mensajes_Listener,
        Fragment_conversacion.On_Fragment_mensaje_Listener,
        callAlertBox.callAlertBox_interface,
        FragmentManager.OnBackStackChangedListener,
        MapaPrincipalFragment.On_MapaPrincipal_Listener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        android.location.LocationListener,
        LocationListener {

    public static MediaPlayer mpSplash;
    public static Vibrator vibrar;
    public static AlertDialog dlg = null;
    public static TextToSpeech tts;
    public static double v_latitud = 0, v_longitud = 0, v_latitudServicio = 0, v_longitudServicio = 0, v_latitudPrevia = 0, v_longitudPrevia = 0;
    public static int v_temporizador = 0, g = 0, reconector = 2, valor_anterior_a_la_interrupcion = 0,
            v_ubicacion, // permite enviar ubicacion actual cada 15 minutos si no ha reportado ubicación en este tiempo
            v_tamanoFuente = 20, p_distancia = 0, total = 0, v_bearing = 0, v_reconexion = 0, v_precision = 0;
    public static String v_ttsDirBarr = "", bs_m_doc_aditional_info = "", v_dir_destino = "", s_medio_pago = "",
            v_clave = null, v_idServicio = "", v_resultado = null, v_turno = "", empresa = "0", v_idServicioRuta = "0", v_fechaGPS = "";
    static GoogleMap mMap;
    static Context mContext;
    public static int v_countAlive = 0;
    CountDownTimer cdt = null;
    static String btnPrincipal_text = Constants.btnPpal.PONERME_DISPONIBLE, /*servicio,*/ ubicacion = "", fecha = null, v_idmensaje = null,
            v_tipoServicio = "", v_pista = "", v_pista_complemento = "", v_estadoTarificacion = "NUEVA";
    static Boolean coordenadas = false, iniciaSesion = false, confirmando = false, v_primera_lectura_gps = false, v_EnSitio = false, v_ruteo = false;
    public static Boolean v_tarificando = false, //tan pronto le da cumplir al servicio se pone en true;
            v_listaPendiente = false, v_desconexion = true, v_disponible = false,
            v_sonando = false, v_cancelado = false, v_autenticado = false, enturnado=false,tiempo_espera=false,
            v_servicio = false, v_tts = true, v_preftts = false, viaje_interrumpido = false;
    static boolean pendiente, running2 = false, v_tmax = false, envia = false, flag = true,
            isCharging = false, pulsaDisponible = false, v_enservicio = false, v_ocupado = false, rec = true;
    static int version_code = 0, aceptando = 30, level = -1, k = 0;
    String versionNumber;
    static ProgressDialog espere = null;
    static Marker service_marker = null;
    static Toast toast = null;
    static Button btnPrincipal;
    ConnectivityManager conMgr = null;
    Boolean bool_KeepAliveGPS = false;
    static String v_direccion_servicio, v_direccion_aproval, v_info_adicional_aproval, v_nombres="", v_placa = "";
    private double distancia;
    MaterialStyledDialog Alert_estadoVale = null;
    CountDownTimer estado_vale_cdt = null;
    BottomSheetDialogFragment bsdFragment_Cumplir, bsdFragment_Cancelations, bsdFragment_Actions, bsdFragment_ServiceArrival, bsdFragment_Documentacion = null,
            bsdFragment_ServiceAproval, bsdFragment_Tarificacion, bsdFragment_Cobro, bsdFragment_toast=null;
    Fragment fragment = null;
    public static Timer myTimerTarificador;
    public static float v_velocidad = 0;
    public static double v_distanciaTotal = 0;
    static TextView tempoView;
    String vale_id = "0", v_razon_social = "";
    public static TextView badge_textView;
    public static int trama_x_enviar_contador = 0;
    final Handler tt_handler = new Handler();
    Runnable runnable = null;
    public static Boolean in_conversacion = false;
    int alive = 35;
    boolean en_aceptacion_servicio = false, ontick = false, leyendo_qr = false;
    public static JSONObject jServicio = null;
    CountDownTimer cdt_salida_pista;
    android.app.AlertDialog.Builder esperar;
    android.app.AlertDialog alert;
    DevicePolicyManager deviceManger;
    ActivityManager activityManager;
    ComponentName compName;
    static String v_transcurrido, hour, min, seg, tiempo = "00:00:00";
    static int horas = 0, minutos = 0, segundos = 0;
    static MapFragment mapFragment = null;
    public static String tipo_tarifa = "0", valor_hora = "0", horas_contratadas = "0";
    public static AlertDialog alertDialogAcciones=null,alertDialogCancelaciones=null;
    private Messenger mMessengerService;
    private GoogleApiClient mGoogleApiClient;
    MapaPrincipalFragment v_MapaPrincipalFragment;
    LocationManager lmgr;
    LocationRequest mLocationRequest;
    Location mLastLocation;

    @Override
    protected void onStart() {
        super.onStart();
        log("onStart()");
        try {
            bindService(new Intent(this, TaxiLujoService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            log( "onStart: Con el servicio");
            e.printStackTrace();
        }

        try {
            JSONObject json = new JSONObject();
            json.put("cmd", "inicio");

            Bundle bundle = new Bundle();
            bundle.putString("json", json.toString());

            try {
                //Class fragmentClass = MapaPrincipalFragment.class;
                v_MapaPrincipalFragment = MapaPrincipalFragment.newInstance();
                fragment = v_MapaPrincipalFragment;
                fragment.setArguments(bundle);
            } catch (Exception e) {
                e.printStackTrace();
            }
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commitAllowingStateLoss();
            fragmentManager.addOnBackStackChangedListener(this);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.activity_main);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mContext = this;

        deviceManger = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        compName = new ComponentName(this, MainActivity.class);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Toolbar toolbar =findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Buttons
        Button btnMenu =  findViewById(R.id.btn_menu);
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(GravityCompat.START);
            }
        });

        badge_textView = findViewById(R.id.badge_textView);
        badge_textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Class fragmentClass = Fragment_conversaciones.class;
                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commitAllowingStateLoss();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                in_conversacion = false;

                log("Click: Info");
            }
        });

        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version_code = pinfo.versionCode;
            versionNumber = pinfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        empresa = getResources().getString(R.string.empresa);

        tts = new TextToSpeech(mContext, MainActivity.this);

        callAlertBox callAlertBox_ = new callAlertBox();
        callAlertBox_.setActivity(this, this);

        if (Inicio_sesion.datasource == null)
            Inicio_sesion.datasource.open();

        int cantidad = Inicio_sesion.datasource.cant_mensajes_sin_leer();
        if (cantidad > 0) {
            badge_textView.setVisibility(View.VISIBLE);
            badge_textView.setText(String.valueOf(cantidad));
        } else {
            badge_textView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        buildGoogleApiClient();
        lmgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        lmgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 100, this);

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

    synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(com.google.android.gms.location.LocationServices.API)
                .build();
        new Thread(new Runnable() {
            public void run() {
                //Aquí ejecutamos nuestras tareas costosas
                mGoogleApiClient.connect();
            }
        }).start();

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

        if (clave.equals("cerrar")) {
            bsdFragment_Cumplir.dismiss();
        } else {
            Inicio_sesion.datasource.updateServtipoCancela("-1");
            if (v_clave.equals(clave)) {
                confirmando = true;
                try {
                    JSONObject jObj2 = new JSONObject();
                    jObj2.put("cmd", "04");
                    jObj2.put("s_id", v_idServicio);
                    jObj2.put("estado", v_estadoTarificacion);
                    Inicio_sesion.datasource.create(jObj2.toString(), "04");
                }catch (JSONException e){
                    e.printStackTrace();
                }
                v_estadoTarificacion = "NUEVA";
                v_ubicacion = 0;
                bsdFragment_Cumplir.dismiss();
            } else {
                toast("Clave incorrecta");
                mpSplash = MediaPlayer.create(getApplicationContext(), R.raw.ding);
                mpSplash.start();
                vibrar = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrar != null) {
                    vibrar.vibrate(400);
                }
            }
        }
    }

    @Override
    public void from_BsModalCancelations(String action) {
        log("RECIBE from_BsModalCancelations: " + action);

        if(!action.equals("")) {
            Inicio_sesion.datasource.updateServtipoCancela(action);
            confirmando = true;
            try {
                JSONObject jObj2 = new JSONObject();
                jObj2.put("cmd", "11");
                jObj2.put("s_id", v_idServicio);
                jObj2.put("tipo_cancela", action);
                Inicio_sesion.datasource.create(jObj2.toString(), "11");
            }catch (JSONException e){
                e.printStackTrace();
            }

            v_ubicacion = 0;
            toast("Enviando Servicio Cancelado");
        }

        if(bsdFragment_Cancelations!=null)
            bsdFragment_Cancelations.dismiss();

    }

    @Override
    public void from_BsModalActions(String action) {
        log("RECIBE from_BsModalActions: " + action);
        switch (action) {
            case "En sitio":
                en_sitio();
                break;
            case "Escanear QR":
                confirmando = true;
                try {
                    JSONObject jObj2 = new JSONObject();
                    jObj2.put("cmd", "04");
                    jObj2.put("s_id", v_idServicio);
                    jObj2.put("estado", v_estadoTarificacion);
                    Inicio_sesion.datasource.create(jObj2.toString(), "04");
                }catch (JSONException e){
                    e.printStackTrace();
                }
                v_estadoTarificacion = "NUEVA";
                v_ubicacion = 0;
                break;
            case "Cancelar":
                bsdFragment_Cancelations = BsModalCancelations.newInstance();
                bsdFragment_Cancelations.show(getSupportFragmentManager(), "bsdFragment_Cancelations");
                break;
            case "Chat con Usuario":
                if (bsdFragment_ServiceArrival != null)
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
                break;
            case "Rechazar":
                rechazar_servicio_aceptado();
                break;
        }

        bsdFragment_Actions.dismiss();

    }

    @Override
    public void from_BsModalCobro(JSONObject jObj) {
        log("RECIBE from_BsModalCobro jObj " + jObj.toString());
        log("RECIBE from_BsModalCobro jServicio " + jServicio.toString());
        try {

            if(jObj.getString("cmd").equals("btnCobrar")) {
                String valor_vale = total + "";
                String clave_vale = jObj.getString("clave_vale");
                if (!clave_vale.trim().equals("") && !valor_vale.trim().equals("")) {
                    //String cedula = Inicio_sesion.datasource.select("vale_cedula");
                    String cedula = jServicio.getString("clave");
                    log("from_BsModalCobro vale_cedula " + cedula);
                    if (clave_vale.trim().equals(cedula)) {

                        RelativeLayout rl_tiempo_espera = findViewById(R.id.rl_tiempo_espera);
                        rl_tiempo_espera.setVisibility(View.GONE);

                        Button btn_solicitar = findViewById(R.id.btn_solicitar);
                        btn_solicitar.setVisibility(View.GONE);

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
            }else{
                if(bsdFragment_Cobro!=null)
                    bsdFragment_Cobro.dismiss();
                bsdFragment_Cobro=null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void from_BsModalTarificacion() {
        log("RECIBE from_BsModalTarificacion");

        //cobrar
        String cedula = Inicio_sesion.datasource.select("vale_cedula");
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

        if(bsdFragment_Cobro!=null)
            bsdFragment_Cobro.dismiss();
        bsdFragment_Cobro=null;

        bsdFragment_Cobro = BsModalCobro.newInstance();
        bsdFragment_Cobro.show(getSupportFragmentManager(), "bsdFragment_Cobro");

    }

    private void pagar_vale_envio() {

        try {
            JSONObject jObj2 = new JSONObject();
            jObj2.put("cmd", "69");
            jObj2.put("empresa", empresa);
            jObj2.put("s_id", v_idServicio);
            jObj2.put("vale", vale_id);
            jObj2.put("lat", v_latitud + "");
            jObj2.put("lng", v_longitud + "");
            jObj2.put("valor", total + "");
            Inicio_sesion.datasource.create(jObj2.toString(), "69");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (myTimerTarificador != null)
            myTimerTarificador.cancel();
        v_tarificando = false;
        v_idServicioRuta = "0";

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

        switch (cadena) {
            case "acepta":
                acepta_recibir_servicio();
                if (bsdFragment_ServiceAproval != null)
                    bsdFragment_ServiceAproval.dismiss();
                break;
            case "rechaza":
                rechaza_servicio();
                if (bsdFragment_ServiceAproval != null)
                    bsdFragment_ServiceAproval.dismiss();
                break;
            case "cierra_documentacion":
                if (bsdFragment_Documentacion != null)
                    bsdFragment_Documentacion.dismiss();
                break;
        }

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

        Switch switch_turno = findViewById(R.id.switch_turno);

        if(switch_turno!=null)
            switch_turno.setChecked(false);
    }

    void acepta_recibir_servicio (){
        cdt.cancel();
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("cmd", "12");
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
        flag = false;
        btnPrincipal.setText(Constants.btnPpal.ESPERANDO_INFORMACION);
        btnPrincipal_text = Constants.btnPpal.ESPERANDO_INFORMACION;
        en_aceptacion_servicio = false;
    }

    public void from_SocketTask(String data) {
        log("RECIBE from_SocketTask: " + data);
        v_countAlive = 0;

            //si no en estan los comandos anteriores es porque estoy recibiendo en json
        try {
            JSONObject jObj = new JSONObject(data);
            String comando = jObj.getString("cmd");
            switch (comando) {
                case "02":
                    recibe_servicio(jObj);
                    break;
                case "19":
                    String respuesta = jObj.getString("msj");
                    if (respuesta.equals("")) {
                        iniciaSesion = true;
                        v_autenticado = true;
                        if (v_disponible) {
                            v_disponible = false;
                            if (toast != null)
                                toast.cancel();
                            ponerme_disponible();
                        }

                        Inicio_sesion.datasource.create(Inicio_sesion.usuario, "cedula");
                       // String estado_telefono = Inicio_sesion.datasource.select("estado_telefono");

                        if (pulsaDisponible) {
                            // Si el dispositivo estaba en estado disponible
                            // antes de recibir una desconexión, entonces se
                            // pone disponible automáticamente
                            ponerme_disponible();
                        }

                        if (jObj.has("dir_d"))
                            v_dir_destino = jObj.getString("dir_d");

                        v_idServicio = jObj.getString("servicio");
                        //revisamos si hay servicio pendiente
                        if (!v_idServicio.equals("")) {
                            revisa_servicio_pendiente(jObj);
                            if(jObj.getString("estado").equals("86")) {
                                v_estadoTarificacion = "RESUME";
                                comienza_tarificador("4");
                            }
                        }
                    } else {
                        callAlertBox.showMyAlertBox(respuesta);
                        v_autenticado = false;
                    }
                    break;
                case "04":
                    v_enservicio = true;
                    if(jServicio.getString("t_serv").equals("18")){
                        comienza_tarificador_preestablecido(jObj, "3");
                    }else {
                        //lee el QR
                        IntentIntegrator integrator = new IntentIntegrator((Activity) mContext);
                        integrator.setPrompt("Escanear código QR");
                        integrator.setOrientationLocked(true);
                        integrator.initiateScan();
                    }
                    break;
                case "16":
                    permite_disponible(jObj);
                    break;
                case "03":
                    pulsaDisponible = false;
                    ponerme_ocupado();
                    v_ocupado = true;
                    break;
                case "08":
                    //confirma aceptacion del servicio
                    break;
                case "24":
                    v_EnSitio = true;
                    toast("Unidad En Sitio..\n" + (int) distancia + " metros.");
                    break;
                case "10": {
                    Inicio_sesion.datasource.update_servicio(v_idServicio, total, (int) v_distanciaTotal, "Rechazado", v_razon_social);
                    confirmando = false;
                    pulsaDisponible = false;
                    pendiente = false;
                    toast("Servicio Rechazado por el Conductor");
                    btnPrincipal.setText(Constants.btnPpal.PONERME_DISPONIBLE);
                    btnPrincipal_text = btnPrincipal.getText().toString();
                    Inicio_sesion.datasource.updateServEstado(4);
                    Inicio_sesion.datasource.updateRegistros(Constants.DB.SERVICIO);

                    if (bsdFragment_ServiceArrival != null)
                        bsdFragment_ServiceArrival.dismiss();

                    Switch switch_turno = findViewById(R.id.switch_turno);

                    if (switch_turno != null)
                        switch_turno.setChecked(false);

                    btnPrincipal.setText(Constants.btnPpal.PONERME_DISPONIBLE);

                    btnPrincipal_text = btnPrincipal.getText().toString();
                    Inicio_sesion.datasource.updateServEstado(4);
                    Inicio_sesion.datasource.updateRegistros(Constants.DB.SERVICIO);

                    if (bsdFragment_ServiceArrival != null)
                        bsdFragment_ServiceArrival.dismiss();

                    RelativeLayout rl_tarificador = findViewById(R.id.rl_tarificador);
                    rl_tarificador.setVisibility(View.GONE);
                    btnPrincipal.setVisibility(View.VISIBLE);

                    break;
                }
                case "11": {
                    Inicio_sesion.datasource.update_servicio(v_idServicio, total, (int) v_distanciaTotal, "Cancelado", v_razon_social);
                    v_enservicio = false;
                    v_clave = null;
                    confirmando = false;
                    pulsaDisponible = false;
                    toast("Servicio Cancelado: Confirmado");
                    Inicio_sesion.datasource.updateServEstado(5);
                    v_idServicio = "";
                    pendiente = false;
                    Inicio_sesion.datasource.updateRegistros(Constants.DB.SERVICIO);
                    ponerme_ocupado();

                    if (bsdFragment_ServiceArrival != null)
                        bsdFragment_ServiceArrival.dismiss();

                    Switch switch_turno = findViewById(R.id.switch_turno);

                    if (switch_turno != null)
                        switch_turno.setChecked(false);
                    break;
                }
                case "12":
                    pregunta_aceptacion_servicio(jObj);
                    break;
                case "34":
                    pista(jObj);
                    break;
                case "76":
                    Inicio_sesion.datasource.updateUbicacion(jObj);
                    break;
                case "69":
                    comando_69(jObj);
                    break;
                case "06":
                    recibe_mensaje(jObj);
                    break;
                case "29":
                    recibe_confirmacion_mensaje(jObj);
                    break;
                case "CU":
                    Button btn_solicitar = findViewById(R.id.btn_solicitar);
                    btn_solicitar.setVisibility(View.VISIBLE);

                    break;
                case "V1":

                    break;
                case "91":
                    salida_de_pista(jObj);
                    break;
                case "A7":
                    TextView tv_pista = findViewById(R.id.tv_pista);
                    //tv_pista.setTextColor(getResources().getColor(R.color.primary_dark));
                    if (v_pista != null) {
                        if (tv_pista != null) {
                            tv_pista.setText(v_pista);
                        }
                    }
                    break;
                case "A9":
                    //pasajero recogido
                    RelativeLayout rl_tiempo_espera = findViewById(R.id.rl_tiempo_espera);
                    rl_tiempo_espera.setVisibility(View.VISIBLE);
                    break;
                case "V2":
                    //Crea servicio de QR
                    if (!jObj.getString("vale_documento").equals("0")) {
                        recibe_servicio(jObj);
                        if (bsdFragment_ServiceArrival != null)
                            bsdFragment_ServiceArrival.dismiss();

                        ponerme_ocupado();
                        v_enservicio = false;
                        // comienza a rutear
                        v_ruteo = true;
                        String cedula = jObj.getString("vale_documento");
                        String vale_valor;
                        if(jObj.has("vale_valor"))
                            vale_valor = jObj.getString("vale_valor");
                        else
                            vale_valor = jObj.getString("valor");
                        String last4 = cedula.substring(cedula.length() - 4);
                        log("Creando últimos 4 dígitos de la cédula: ." + last4 + ".");
                        //creacion en la BD de los datos
                        Inicio_sesion.datasource.create(last4, "vale_cedula");
                        Inicio_sesion.datasource.create(vale_valor, "vale_valor");
                        Inicio_sesion.datasource.create(v_idServicio, "vale_servicio");
                        Inicio_sesion.datasource.create(vale_id, "vale_id");
                        v_ubicacion = 0;

                        if (vale_valor.equals("0")) {
                            comienza_tarificador("1");
                        } else {
                            comienza_tarificador_preestablecido(jObj, "1");
                        }
                    }
                    break;
                default:
                    log("No implementado " + data);
                    break;
            }
            if(jObj.has("slt"))
                Inicio_sesion.datasource.deleteId(jObj.getString("slt"));
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void salida_de_pista(JSONObject jObj){
        if(v_disponible) {
            final TextView t_view = new TextView(MainActivity.mContext);
            final ScrollView s_view = new ScrollView(MainActivity.mContext);
            t_view.setPadding(30, 5, 20, 10);
            t_view.setBackgroundColor(Color.WHITE);
            t_view.setTextColor(Color.BLACK);
            t_view.setTextSize(18);
            s_view.addView(t_view);

            try {
                final String temporizador = jObj.getString("temporizador");
                final String pista = jObj.getString("pista");

                Handler handler1 = new Handler(Looper.getMainLooper());
                handler1.post(new Runnable() {
                    @Override
                    public void run() {

                        int temp = Integer.parseInt(temporizador);
                        cdt_salida_pista = new CountDownTimer(temp * 1000, 1000) {

                            public void onTick(final long millisUntilFinished) {

                                if (!ontick) {
                                    ontick = true;
                                    esperar = new android.app.AlertDialog.Builder(MainActivity.mContext);
                                    wakeup();//salida de pista
                                    mpSplash = MediaPlayer.create(getApplicationContext(), R.raw.siren);
                                    mpSplash.start();
                                    vibrar = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                    if (vibrar != null) {
                                        vibrar.vibrate(400);
                                    }
                                    String text = "Estimado Conductor, Se ha detectado ubicación fuera de la Pista\nDesea salir de '" + pista + "'? " + millisUntilFinished / 1000;
                                    t_view.setText(text);
                                    esperar.setCancelable(false)
                                            .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //sale de pista
                                                    ontick = false;
                                                    v_turno="";

                                                    //oculta el layout
                                                    RelativeLayout rl_pista = findViewById(R.id.rl_pista);
                                                    rl_pista.setVisibility(View.GONE);

                                                    TextView tv_pista = findViewById(R.id.tv_pista);
                                                    //tv_pista.setTextColor(getResources().getColor(R.color.primary_dark));
                                                    tv_pista.setText(v_pista);

                                                    Switch switch_turno = findViewById(R.id.switch_turno);
                                                    switch_turno.setChecked(false);

                                                    try {
                                                        JSONObject jsonObj = new JSONObject();
                                                        jsonObj.put("cmd", "91");
                                                        jsonObj.put("detalle", "conductor");
                                                        envia_json(jsonObj);
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                    alert.dismiss();
                                                    cdt_salida_pista.cancel();
                                                }
                                            })
                                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //sigue en pista
                                                    ontick = false;
                                                    try {
                                                        JSONObject jsonObj = new JSONObject();
                                                        jsonObj.put("cmd", "91");
                                                        jsonObj.put("detalle", "sigue en pista");
                                                        envia_json(jsonObj);
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                    if (cdt_salida_pista != null)
                                                        cdt_salida_pista.cancel();
                                                }
                                            });
                                    if (s_view.getParent() != null)
                                        ((ViewGroup) s_view.getParent()).removeView(s_view);
                                    esperar.setView(s_view);
                                    alert = esperar.create();

                                    alert.show();
                                } else {
                                    String text  = "Estimado Conductor, Se ha detectado ubicación fuera de la Pista\nDesea salir de '" + pista + "'? " + millisUntilFinished / 1000;
                                    log("temporizador sale de pista " + millisUntilFinished / 1000);
                                    t_view.setText(text);
                                }
                            }

                            public void onFinish() {
                                //sale de pista
                                ontick = false;
                                try {
                                    JSONObject jsonObj = new JSONObject();
                                    jsonObj.put("cmd", "91");
                                    jsonObj.put("detalle", "temporizador");
                                    envia_json(jsonObj);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                log("terminé countdown");
                                alert.dismiss();
                            }
                        }.start();
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            try {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("cmd", "91");
                jsonObj.put("detalle", "estado ocupado");
                envia_json(jsonObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    void pista(JSONObject jObj){
        try{

            if(jObj.getString("turno").equals("1")) {
                v_pista_complemento = "Turno " + jObj.getString("turno") + " de " + jObj.getString("de");
                v_pista = "Pista " + jObj.getString("pista") + "\n";
            }else{
                v_pista_complemento = "Turno " + jObj.getString("turno") + " de " + jObj.getString("de") + " atrás de " + jObj.getString("placa");
                v_pista = "Pista " + jObj.getString("pista") + "\n";
            }

            RelativeLayout rl_pista = findViewById(R.id.rl_pista);
            rl_pista.setVisibility(View.VISIBLE);

            TextView tv_pista = findViewById(R.id.tv_pista);
            String text = v_pista + v_pista_complemento;
            tv_pista.setText(text);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    void permite_disponible(JSONObject jObj){
        try {

            String descripcion = jObj.getString("desc");
            if (descripcion.equals("")) {
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


                if(jObj.getString("pista").equals("")){
                    v_pista ="";
                    RelativeLayout rl_pista =  findViewById(R.id.rl_pista);
                    rl_pista.setVisibility(View.GONE);

                }else{

                    v_pista = "Pista " + jObj.getString("pista") + "\n";

                    RelativeLayout rl_pista = findViewById(R.id.rl_pista);
                    rl_pista.setVisibility(View.VISIBLE);

                    TextView tv_pista = findViewById(R.id.tv_pista);
                    tv_pista.setText(v_pista);
                }

            } else {

                bs_m_doc_aditional_info = descripcion.replaceAll("-", ".\n");

                if (bsdFragment_Documentacion != null)
                    bsdFragment_Documentacion.dismiss();

                bsdFragment_Documentacion = BsModalDocumentacion.newInstance();
                bsdFragment_Documentacion.show(getSupportFragmentManager(), "bsdFragment_Documentacion");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnPrincipal.setText(Constants.btnPpal.PONERME_DISPONIBLE);
                        btnPrincipal_text = btnPrincipal.getText().toString();
                    }
                });
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    void revisa_servicio_pendiente(JSONObject jObj){
        try{
            v_idServicio = jObj.getString("servicio");
            if(!v_idServicio.equals("")){
                jServicio = jObj;

                if(jObj.has("user_cedula")) {
                    String cedula = jObj.getString("user_cedula");
                    String last4 = cedula.substring(cedula.length() - 4);
                    log("Creando ultimos 4 dígitos de la cédula: ." + last4 + ".");
                    //creacion en la BD de los datos
                    Inicio_sesion.datasource.create(last4, "vale_cedula");
                }

                if(jObj.has("vale"))
                    vale_id = jObj.getString("vale");

                v_resultado = null;
                String v_direccion, text;

                v_clave = jObj.getString("clave");
                v_direccion = jObj.getString("dir");

                if(jObj.getString("lat").equals(""))
                    v_latitudServicio = 0;
                else
                    v_latitudServicio = Double.valueOf(jObj.getString("lat"));

                if(jObj.getString("lng").equals(""))
                    v_longitudServicio = 0;
                else
                    v_longitudServicio = Double.valueOf(jObj.getString("lng"));

                v_razon_social = jObj.getString("empresa");
                v_temporizador = 0;

                v_dir_destino = jObj.getString("dir_d");

                v_direccion_servicio = v_direccion;

                NumberFormat format;
                format = NumberFormat.getNumberInstance();

                BitmapDescriptor markerIconBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.pinlocationstart);

                service_marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(v_latitudServicio, v_longitudServicio))
                        .draggable(false)
                        .anchor(0.5f, 0.5f)
                        .icon(markerIconBitmapDescriptor));

                v_servicio = true;

                if(!jObj.getString("estado").equals("86")) {

                    ConstraintLayout cl_service_tarificacion =  findViewById(R.id.cl_service_tarificacion);
                    cl_service_tarificacion.setVisibility(View.VISIBLE);

                    TextView lladv_input_origen =  findViewById(R.id.lladv_input_origen);
                    String json = "Recoger en " + jObj.getString("dir");
                    lladv_input_origen.setText(json);

                    TextView tv_titulo = findViewById(R.id.tv_titulo);
                    json = "Recoger Pasajero";
                    tv_titulo.setText(json);

                    TextView lladv_input_destino =  findViewById(R.id.lladv_input_destino);
                    json = "Destino: " + v_dir_destino;
                    lladv_input_destino.setText(json);

                    TextView tv_costo_aproximado_bsmst = findViewById(R.id.tv_costo_aproximado_bsmst);
                    json ="$ "+format.format(Integer.parseInt(jObj.getString("valor")));
                    tv_costo_aproximado_bsmst.setText(json);

                    TextView tv_recorrido_del_viaje_bsmst =  findViewById(R.id.tv_recorrido_del_viaje_bsmst);
                    json = format.format(Integer.parseInt(jObj.getString("recorrido"))) + " m";
                    tv_recorrido_del_viaje_bsmst.setText(json);

                    json = "No Iniciado";
                    MapaPrincipalFragment.tv_tiempo_transcurrido_bsmst.setText(json);

                    Button btn_bmst_cobrar =  findViewById(R.id.btn_bmst_cobrar);
                    btn_bmst_cobrar.setVisibility(View.GONE);

                    Button btn_actions =  findViewById(R.id.btn_actions);
                    btn_actions.setVisibility(View.VISIBLE);

                    btnPrincipal.setText(Constants.btnPpal.MOSTRAR_DIRECCION);
                    btnPrincipal_text = btnPrincipal.getText().toString();
                    btnPrincipal.setVisibility(View.GONE);

                    tipo_tarifa = jObj.getString("tipotarifa");

                    if(!tipo_tarifa.equals("66")){
                        TableRow tr_horas_contratadas_bsmst = findViewById(R.id.tr_horas_contratadas_bsmst);
                        tr_horas_contratadas_bsmst.setVisibility(View.GONE);

                        TableRow tr_valor_hora_bsmst =  findViewById(R.id.tr_valor_hora_bsmst);
                        tr_valor_hora_bsmst.setVisibility(View.GONE);

                    }else{
                        valor_hora = jObj.getString("horas_valor");
                        TextView tv_horas_contratadas_bsmst= findViewById(R.id.tv_horas_contratadas_bsmst);
                        tv_horas_contratadas_bsmst.setText(MainActivity.horas_contratadas);

                        TextView tv_valor_hora_bsmst=  findViewById(R.id.tv_valor_hora_bsmst);
                        text = "$ "+format.format(Math.round(Integer.parseInt(valor_hora)));
                        tv_valor_hora_bsmst.setText(text);
                        text = "Destino: Servicio por Horas";
                        lladv_input_destino.setText(text);
                    }
                }else{
                    Button btn_bmst_cobrar =  findViewById(R.id.btn_bmst_cobrar);
                    btn_bmst_cobrar.setVisibility(View.VISIBLE);

                    Button btn_actions =  findViewById(R.id.btn_actions);
                    btn_actions.setVisibility(View.GONE);

                    btnPrincipal.setText(Constants.btnPpal.MOSTRAR_TARIFICACION);
                    btnPrincipal_text = btnPrincipal.getText().toString();
                }
                btnPrincipal.setBackgroundColor(getResources().getColor(R.color.primary_dark));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    void recibe_confirmacion_mensaje(JSONObject jObj){
        try{
            Inicio_sesion.datasource.deleteId(jObj.getString("slt"));
            Inicio_sesion.datasource.update_postgres_id(jObj);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    void recibe_mensaje(JSONObject jObj){
        try {
            String msg = jObj.getString("desc");
            v_idmensaje = jObj.getString("id");
            switch (msg) {
                case "25":

                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("cmd", "25");
                    envia_json(jsonObj);

                    v_ubicacion = 0;
                    break;
                case "27":
                    en_aceptacion_servicio=false;
                    Inicio_sesion.datasource.update_servicio(v_idServicio, total, (int) v_distanciaTotal, "Cancelado", v_razon_social);
                    if (vibrar != null)
                        vibrar.cancel();

                    if (cdt != null)
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
                    Inicio_sesion.datasource.create("", Constants.DB.RUTA);
                    wakeup();//recibe mensaje de cancelacion del servicio
                    confirmando = false;
                    pulsaDisponible = false;
                    if (dlg != null) {
                        dlg.dismiss();
                    }
                    callAlertBox.showMyAlertBox("Servicio Cancelado por Usuario");
                    btnPrincipal.setText(Constants.btnPpal.PONERME_DISPONIBLE);
                    btnPrincipal_text = Constants.btnPpal.PONERME_DISPONIBLE;
                    Inicio_sesion.datasource.updateServtipoCancela("5");
                    Inicio_sesion.datasource.updateServEstado(5);
                    v_cancelado = true;
                    Inicio_sesion.datasource.updateRegistros(Constants.DB.SERVICIO);

                    new Handler().post(new Runnable() {
                        public void run() {
                            if (bsdFragment_ServiceArrival != null)
                                bsdFragment_ServiceArrival.dismiss();

                            if (bsdFragment_ServiceAproval != null)
                                bsdFragment_ServiceAproval.dismiss();
                        }
                    });

                    ponerme_ocupado();
                    break;
                default:
                    wakeup();//panico de unidad


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

                        Inicio_sesion.datasource.create_mensaje(jObj.toString(), 0);

                        if (getFragmentRefreshListener() != null) {
                            getFragmentRefreshListener().onRefresh(jObj);
                        }
                        if (getFragmentRefreshListener_chats() != null) {
                            getFragmentRefreshListener_chats().onRefresh(jObj);
                        }

                        TextView badge_textView = findViewById(R.id.badge_textView);
                        int cantidad = Inicio_sesion.datasource.cant_mensajes_sin_leer();
                        if (cantidad > 0) {
                            badge_textView.setVisibility(View.VISIBLE);
                            badge_textView.setText(String.valueOf(cantidad));
                        } else {
                            badge_textView.setVisibility(View.GONE);
                        }
                    }

                    if (dlg != null) {
                        dlg.dismiss();
                    }

                    break;
            }

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("cmd", "07");
            jsonObj.put("id", v_idmensaje);
            envia_json(jsonObj);

            v_idmensaje = null;

            Inicio_sesion.datasource.updateRegistros("mensaje");
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

        mMap.clear();

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(v_latitud, v_longitud) , 16f));

        if (myTimerTarificador != null)
            myTimerTarificador.cancel();

        if(bsdFragment_Tarificacion != null)
            bsdFragment_Tarificacion.dismiss();

        p_distancia = 0;
        v_idServicioRuta = "0";
        pendiente = false;
        v_enservicio = false;
        // vaciar ruta en el sqlite
        Inicio_sesion.datasource.create("", Constants.DB.RUTA);

        Inicio_sesion.datasource.updateServEstado(0);
        btnPrincipal.setText(Constants.btnPpal.PONERME_DISPONIBLE);
        btnPrincipal_text = Constants.btnPpal.PONERME_DISPONIBLE;
        //tan pronto llega la confirmacion de pago
        //volvemos a dejar el estado del telefono en modo normal
        Inicio_sesion.datasource.create("normal", "estado_telefono");
        Inicio_sesion.datasource.create("", "id_servicio");
    }

    private void comando_69(JSONObject jObj) {
        Inicio_sesion.datasource.update_servicio(v_idServicio, total, (int) v_distanciaTotal, "Cumplido", v_razon_social);
        v_clave = null;
        //servicio = "";
        confirmando = false;
        pulsaDisponible = false;
        //toast("Servicio Cumplido: Confirmado");
        Inicio_sesion.datasource.updateServEstado(0);
        v_idServicio = "";
        pendiente = false;
        Inicio_sesion.datasource.updateRegistros(Constants.DB.SERVICIO);
        //ahora si cambiar la pantalla
        ponerme_ocupado();
        v_enservicio = false;
        btnPrincipal.setText(Constants.btnPpal.PONERME_DISPONIBLE);
        btnPrincipal_text = Constants.btnPpal.PONERME_DISPONIBLE;
        btnPrincipal.setVisibility(View.VISIBLE);

        Inicio_sesion.datasource.create("", "vale_cedula");
        Inicio_sesion.datasource.create("", "vale_valor");
        Inicio_sesion.datasource.create("", "vale_servicio");
        Inicio_sesion.datasource.create("", "vale_id");
        v_servicio = false;
        v_idServicioRuta = "0";
        Inicio_sesion.datasource.create("normal", "estado_telefono");
        Inicio_sesion.datasource.create("", "id_servicio");
        v_idServicio = "";
        v_ruteo = false;
        if (myTimerTarificador != null)
            myTimerTarificador.cancel();

        recibeMsgPago();

        try {
            String text = "con " + jObj.getString("usuario");
            TextView rat_tv_user = findViewById(R.id.rat_tv_user);
            rat_tv_user.setText(text);

            ConstraintLayout cl_rating = findViewById(R.id.cl_rating_);
            if(cl_rating!=null)
                cl_rating.setVisibility(View.VISIBLE);

            RelativeLayout rl_tarificador = findViewById(R.id.rl_tarificador);
            rl_tarificador.setVisibility(View.VISIBLE);

            ConstraintLayout cl_service_tarificacion =  findViewById(R.id.cl_service_tarificacion);
            cl_service_tarificacion.setVisibility(View.GONE);

            RatingBar rb_user =  findViewById(R.id.rb_user);
            rb_user.setRating(0);

        }catch (JSONException e ){
            e.printStackTrace();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        Switch switch_turno = findViewById(R.id.switch_turno);

        if(switch_turno!=null)
            switch_turno.setChecked(false);
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
                callAlertBox.showMyAlertBox("Unidad sin conexión");
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("empresa", empresa);
                params.put("servicio", v_idServicio);
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
        if(estado_vale_cdt!=null)
            estado_vale_cdt.cancel();

        try {
            //parsear la respuesta //{"cmd":"consulta_estado","estado":"SIN USAR","codigo":"19","empresa":"Baxter","vale_id":"7","vale_nombre":"Martin Figueroa","vale_documento":"94536238","vale_valor":0}
            final JSONObject jObj2 = new JSONObject(response);
            String estado_codigo = jObj2.getString("codigo");
            String estado_nombre = jObj2.getString("estado");
            String empresa_de_vale = jObj2.getString("empresa");
            if (estado_codigo.equals("19")) {//VALIDO
                //si esta en servicio, debe ingresar la informacion
                if (v_enservicio) {
                    String tipo_servicio=jObj2.getString("tiposervicio");
                    Alert_estadoVale = new MaterialStyledDialog(MainActivity.this)
                            .setTitle("Vale Electrónico")
                            .setDescription("Estado del Vale: " + estado_nombre + "\n Empresa: " + empresa_de_vale + "\nTipo de Servicio: " + tipo_servicio+".")
                            .setIcon(R.mipmap.ic_launcher)
                            .setHeaderColor(R.color.mapboxDenim)
                            .withDivider(true)
                            .setPositive("Iniciar Viaje", new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    if(estado_vale_cdt!=null)
                                        estado_vale_cdt.cancel();
                                    ponerme_ocupado();
                                    v_enservicio = false;
                                    // comienza a rutear
                                    v_ruteo = true;
                                    try {
                                        String cedula = jObj2.getString("vale_documento");
                                        String vale_valor = jObj2.getString("vale_valor");
                                        String last4 = cedula.substring(cedula.length() - 4);
                                        vale_id = jObj2.getString("vale_id");

                                        log("Creando ultimos 4 digitos de la cedula: ." + last4 + ".");
                                        //creacion en la BD de los datos
                                        Inicio_sesion.datasource.create(last4, "vale_cedula");
                                        Inicio_sesion.datasource.create(vale_valor, "vale_valor");
                                        Inicio_sesion.datasource.create(v_idServicio, "vale_servicio");
                                        Inicio_sesion.datasource.create(vale_id, "vale_id");
                                        v_ubicacion = 0;

                                        comienza_tarificador_preestablecido(jObj2, "2");

                                        leyendo_qr=false;
                                    }catch (JSONException e){
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .setNegative("Cerrar", new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    try {
                                        JSONObject jObj = new JSONObject();
                                        jObj.put("cmd", "V1");
                                        jObj.put("tiq_id", jObj2.getString("vale_id"));
                                        Inicio_sesion.datasource.createRegistro(jObj.toString(), "V1", 0);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    if(estado_vale_cdt!=null)
                                        estado_vale_cdt.cancel();
                                    leyendo_qr=false;
                                }
                            })
                            .show();


                } else {
                    String tipo_servicio=jObj2.getString("tiposervicio");
                    Alert_estadoVale = new MaterialStyledDialog(MainActivity.this)
                            .setTitle("Vale Electrónico")
                            .setDescription("Estado del Vale: " + estado_nombre + "\n Empresa: " + empresa_de_vale + "\nTipo de Servicio: " + tipo_servicio+".")
                            .setIcon(R.mipmap.ic_launcher)
                            .setHeaderColor(R.color.mapboxDenim)
                            .withDivider(true)
                            .setPositive("Iniciar Viaje", new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    if(estado_vale_cdt!=null)
                                        estado_vale_cdt.cancel();
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
                                    leyendo_qr=false;
                                }
                            })
                            .setNegative("Cerrar", new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    try {
                                        JSONObject jObj = new JSONObject();
                                        jObj.put("cmd", "V1");
                                        jObj.put("tiq_id", jObj2.getString("vale_id"));
                                        Inicio_sesion.datasource.createRegistro(jObj.toString(), "V1", 0);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    if(estado_vale_cdt!=null)
                                        estado_vale_cdt.cancel();
                                    leyendo_qr=false;
                                }
                            })
                            .show();
                }
            } else {
                Alert_estadoVale = new MaterialStyledDialog(MainActivity.this)
                        .setTitle("Vale Electrónico")
                        .setDescription("Estado del Vale: " + estado_nombre)
                        .setIcon(R.mipmap.ic_launcher)
                        .setHeaderColor(R.color.primary)
                        .withDivider(true)
                        .setNegative("Cerrar", new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if(estado_vale_cdt!=null)
                                    estado_vale_cdt.cancel();
                                dialog.dismiss();
                                leyendo_qr=false;
                            }
                        })
                        .show();
            }

            //countdown timer
            estado_vale_cdt = new CountDownTimer(15000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    //nothing to do
                }
                @Override
                public void onFinish() {
                    try {
                        JSONObject jObj = new JSONObject();
                        jObj.put("cmd", "V1");
                        jObj.put("tiq_id", jObj2.getString("vale_id"));
                        Inicio_sesion.datasource.createRegistro(jObj.toString(), "V1", 0);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if(Alert_estadoVale!=null)
                        Alert_estadoVale.dismiss();

                    estado_vale_cdt.cancel();
                    leyendo_qr=false;
                }
            }.start();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void iniciar_servicio_qr(final JSONObject jObj) throws JSONException {
        espere();
        //iniciar_servicio_qr
        final String vale_id = jObj.getString("id");
        log(vale_id);
        RequestQueue queue = Volley.newRequestQueue(mContext);

        String url = "https://www.servidor.com.co/gateway/Vale_virtual/iniciar_qr";
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
                callAlertBox.showMyAlertBox("Unidad sin conexion");
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
        if (!v_desconexion) {
            try {

                JSONObject jResponse = new JSONObject(response);

                log("iniciar_servicio_qr_response "+jResponse.toString());
                final String vale_id = jObj.getString("id");
                //id, nombre, documento, valor
                String cedula = jObj.getString("documento");
                String vale_valor = jObj.getString("valor");
                String last4 = cedula.substring(cedula.length() - 4);
                log("Creando ultimos 4 digitos de la cedula: ." + last4 + ".");
                //creacion en la BD de los datos
                Inicio_sesion.datasource.create(last4, "vale_cedula");
                Inicio_sesion.datasource.create(vale_valor, "vale_valor");
                Inicio_sesion.datasource.create(v_idServicio, "vale_servicio");
                Inicio_sesion.datasource.create(vale_id, "vale_id");
                v_ubicacion = 0;
                //remover marcador
                 if(service_marker != null) {
                     service_marker.remove();
                    service_marker = null;
                }

                jObj.put("user_pic",jResponse.getString("user_pic"));

                jServicio = jObj;

                //comienza_tarificador iniciar_servicio_qr
                comienza_tarificador("3");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            callAlertBox.showMyAlertBox("Unidad sin conexion");
        }
    }

    void crear_servicio_qr(final JSONObject jObj) {
        String dir="";
        try {
            List<Address> addresses;
            vale_id = jObj.getString("vale_id");

            Geocoder gcd = new Geocoder(mContext, Locale.getDefault());
            addresses = gcd.getFromLocation(v_latitud, v_longitud, 1);
            if (addresses.size() > 0) {
                dir = addresses.get(0).getAddressLine(0);
                log("Geocoder Dirección: "+dir);
            }

            JSONObject jObj2 = new JSONObject();
            jObj2.put("cmd", "V2");
            jObj2.put("dir", dir);
            jObj2.put("vale_id", jObj.getString("vale_id"));
            jObj2.put("lat", v_latitud + "");
            jObj2.put("lng", v_longitud + "");
            Inicio_sesion.datasource.create(jObj2.toString(),"V2");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            //en caso que el geocoder no funcione, traemos la direccion de google por http
            getFromLocation(v_latitud, v_longitud, jObj);
        }

    }

    void getFromLocation(double lat, double lng, final JSONObject jObj) {

        String address = String.format(Locale.ENGLISH, "http://maps.googleapis.com/maps/api/geocode/json?latlng=%1$f,%2$f&sensor=true&language=" + Locale.getDefault().getCountry(), lat, lng);

        RequestQueue queue = Volley.newRequestQueue(mContext);

        StringRequest postRequest = new StringRequest(Request.Method.GET, address,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        //log ("Response getFromLocation "+ response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String dir="";
                            if ("OK".equalsIgnoreCase(jsonObject.getString("status"))) {
                                JSONArray results = jsonObject.getJSONArray("results");
                                //for (int i = 0; i < results.length(); i++) {
                                JSONObject result = results.getJSONObject(0);
                                dir = result.getString("formatted_address");
                                log("getFromLocation Dirección: "+dir);
                            }

                            JSONObject jObj2 = new JSONObject();
                            jObj2.put("cmd", "V2");
                            jObj2.put("dir", dir);
                            jObj2.put("vale_id", jObj.getString("vale_id"));
                            jObj2.put("lat", v_latitud + "");
                            jObj2.put("lng", v_longitud + "");
                            Inicio_sesion.datasource.create(jObj2.toString(), "V2");
                        } catch (JSONException e) {
                            log("Error parsing Google geocode webservice response.");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                log("Error.Response");
                termine();
                callAlertBox.showMyAlertBox("Error en el envio, Intenta nuevamente");
            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<>();
                params.put("", "");
                return params;
            }
        };
        int socketTimeout = 50000; //50 segundos de timeout para que no envie doble la peticion de pago
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        postRequest.setRetryPolicy(policy);
        queue.add(postRequest);
    }


    void en_sitio(){
            if (!v_EnSitio) {

                try {
                    JSONObject jObj = new JSONObject();
                    jObj.put("cmd", "24");
                    jObj.put("lat", v_latitud + "");
                    jObj.put("lng", v_longitud + "");
                    jObj.put("s_id", v_idServicio);
                    jObj.put("date_gps", v_fechaGPS + "");
                    Inicio_sesion.datasource.createRegistro(jObj.toString(), "24", 0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

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
                        try {
                            JSONObject jsonObj = new JSONObject();
                            jsonObj.put("cmd", "23");
                            jsonObj.put("opcion", value);
                            envia_json(jsonObj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
                        try {
                            JSONObject jObj2 = new JSONObject();
                            jObj2.put("cmd", "10");
                            Inicio_sesion.datasource.create(jObj2.toString(), "10");
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
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

    void from_MapaPrincipalFragment_onAttach(JSONObject jObj) throws JSONException{
        log("from_MapaPrincipalFragment_onAttach " + jObj.toString());

        if(getIntent().getExtras()!=null) {

            v_nombres = getIntent().getExtras().getString("nombres");
            v_placa   = getIntent().getExtras().getString("placa");

            if(mMap != null) {
                JSONObject jObj2 = new JSONObject(getIntent().getExtras().getString("json"));
                socket_inicia_sesion(jObj2);
            }
        }
    }

    public void from_MapaPrincipalFragment(JSONObject jObj) {

        log("from_MapaPrincipalFragment " + jObj.toString());
        try {
            String comando = jObj.getString("cmd");
            switch (comando) {
                case "mpf_1":
                    String detalle = jObj.getString("detalle");
                    if (!v_desconexion && (detalle.equals(Constants.btnPpal.PONERME_DISPONIBLE) || detalle.equals(Constants.btnPpal.VALIDANDO))) {
                        ponerme_disponible();
                    } else if (detalle.equals(Constants.btnPpal.PONERME_OCUPADO)) {
                        if (!v_servicio) {

                            JSONObject jObj2 = new JSONObject();
                            jObj2.put("cmd", "03");
                            Inicio_sesion.datasource.create(jObj2.toString(), "03");

                            ponerme_ocupado();
                        }
                    } else if (detalle.equals(Constants.btnPpal.MOSTRAR_DIRECCION)) {

                        RelativeLayout rl_tarificador = findViewById(R.id.rl_tarificador);
                        rl_tarificador.setVisibility(View.VISIBLE);

                        ConstraintLayout cl_service_tarificacion = findViewById(R.id.cl_service_tarificacion);
                        cl_service_tarificacion.setVisibility(View.VISIBLE);

                        ConstraintLayout cl_rating_ = findViewById(R.id.cl_rating_);
                        cl_rating_.setVisibility(View.GONE);

                        btnPrincipal.setVisibility(View.GONE);
                    } else if (detalle.equals(Constants.btnPpal.MOSTRAR_TARIFICACION)) {
                        RelativeLayout rl_tarificador = findViewById(R.id.rl_tarificador);
                        rl_tarificador.setVisibility(View.VISIBLE);

                        ConstraintLayout cl_service_tarificacion = findViewById(R.id.cl_service_tarificacion);
                        cl_service_tarificacion.setVisibility(View.VISIBLE);

                        ConstraintLayout cl_rating_ = findViewById(R.id.cl_rating_);
                        cl_rating_.setVisibility(View.GONE);

                        btnPrincipal.setVisibility(View.GONE);
                    } else if (detalle.equals(Constants.btnPpal.MOSTRAR_ACEPTACION)) {

                        if (bsdFragment_ServiceAproval != null)
                            bsdFragment_ServiceAproval.dismiss();

                        bsdFragment_ServiceAproval = BsModalServiceAproval.newInstance();
                        bsdFragment_ServiceAproval.show(getSupportFragmentManager(), "bsdFragment_ServiceAproval");
                    }
                    btnPrincipal_text = detalle;
                    break;
                case "CU":
                    Inicio_sesion.datasource.createRegistro(jObj.toString(), "CU", 0);
                    RelativeLayout rl_tarificador = findViewById(R.id.rl_tarificador);
                    rl_tarificador.setVisibility(View.GONE);

                    ConstraintLayout cl_rating_ = findViewById(R.id.cl_rating_);
                    cl_rating_.setVisibility(View.GONE);

                    ConstraintLayout cl_service_tarificacion =findViewById(R.id.cl_service_tarificacion);
                    cl_service_tarificacion.setVisibility(View.GONE);

                    btnPrincipal.setVisibility(View.VISIBLE);
                    break;
                case "cancelacion":
                    String tipo_cancela = jObj.getString("tipo_cancela");
                    if(!tipo_cancela.equals("")) {
                        Inicio_sesion.datasource.updateServtipoCancela(tipo_cancela);
                        confirmando = true;
                        try {
                            JSONObject jObj2 = new JSONObject();
                            jObj2.put("cmd", "11");
                            jObj2.put("s_id", v_idServicio);
                            jObj2.put("tipo_cancela", tipo_cancela);
                            Inicio_sesion.datasource.create(jObj2.toString(), "11");
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                        v_ubicacion = 0;
                        toast("Enviando Servicio Cancelado");
                    }
                    break;
                case "btn_bmst_cobrar":
                    //cobrar
                    //String cedula = Inicio_sesion.datasource.select("vale_cedula");
                    //if(cedula.trim().equals("")) {
                    //if(jServicio.getString("estado").equals("86")){
                       /* IntentIntegrator integrator = new IntentIntegrator(this);
                        integrator.setPrompt("Por favor lea su QR");
                        integrator.setOrientationLocked(false);
                        integrator.initiateScan();
                    }else {*/
                        cobrar_vale();
                    //}
                    break;
                case "btn_actions":

                    String action = jObj.getString("action");
                    switch (action) {
                        case "En sitio":
                            en_sitio();
                            break;
                        case "onAttach":
                            from_MapaPrincipalFragment_onAttach(jObj);
                            break;
                        case "Ingresar Clave":
                            bsdFragment_Cumplir = BsModalCumplir.newInstance();
                            bsdFragment_Cumplir.show(getSupportFragmentManager(), "bsdFragment_Cumplir");
                            break;
                        case "Escanear QR":
                            confirmando = true;
                            try {
                                JSONObject jObj2 = new JSONObject();
                                jObj2.put("cmd", "04");
                                jObj2.put("s_id", v_idServicio);
                                jObj2.put("estado", v_estadoTarificacion);
                                Inicio_sesion.datasource.create(jObj2.toString(), "04");
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                            v_estadoTarificacion = "NUEVA";
                            v_ubicacion = 0;
                            break;
                        case "Chat con Usuario":
                            if (bsdFragment_ServiceArrival != null)
                                bsdFragment_ServiceArrival.dismiss();

                            try {
                                JSONObject jObj2 = new JSONObject();
                                jObj2.put("id", "0");
                                jObj2.put("user_pic", jServicio.getString("user_pic"));
                                jObj2.put("user_id", jServicio.getString("user_id"));
                                jObj2.put("user_name", jServicio.getString("nombre"));
                                from_Fragment_mensajes(jObj2);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                        case "Rechazar":
                            rechazar_servicio_aceptado();
                            break;
                        case "Compartir Ubicación":
                            String share_text = "Información de " + v_nombres +
                                    "\nHola, quiero informarte que estoy usando la app " + getString(R.string.app_name) + " y me encuentro conduciendo el vehículo " + v_placa +
                                    ".\npuedes ver mi ubicación actual en https://maps.google.com?q=" + v_latitud + "," + v_longitud;

                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.putExtra(Intent.EXTRA_TEXT, share_text);
                            intent.putExtra(Intent.EXTRA_SUBJECT, "Información de " + getString(R.string.app_name));
                            startActivity(Intent.createChooser(intent, "Compartir"));
                            break;
                    }
                    break;
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    public void fragmentMapaPrincipal_onMapReady() {
        Bundle b = getIntent().getExtras();
        if (b != null) {
            String value = b.getString("json");
            try {
                JSONObject jObj = new JSONObject(value);
                if (jObj.get("servicio").equals("")) {
                    log("fragmentMapaPrincipal_onMapReady cargando servicio activo");
                    revisa_servicio_pendiente(jObj);
                    if(jObj.getString("estado").equals("86")) {
                        v_estadoTarificacion = "RESUME";
                        comienza_tarificador("4");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void recibe_servicio(JSONObject jObj) {
        jServicio = jObj;
        try {
            String text ;
            NumberFormat format = NumberFormat.getNumberInstance();
            v_transcurrido = "Tiempo de Espera: 00:00:00";
            hour = "00";
            min = "00";
            seg = "00";

            horas = 0;
            minutos = 0;
            segundos = 0;

            jObj.put("estado", "En Servicio");

            Inicio_sesion.datasource.createRegistro(jObj.toString(), Constants.DB.SERVICIO, 1);

            JSONObject jObj08 = new JSONObject();
            jObj08.put("cmd", "08");
            jObj08.put("s_id", jObj.getString("servicio"));
            jObj08.put("lat", v_latitud + "");
            jObj08.put("lng", v_longitud + "");
            jObj08.put("date_gps", v_fechaGPS + "");
            Inicio_sesion.datasource.create(jObj08.toString(), Constants.ACTION.TRAMA_X_ENVIAR);

            servicioAceptado(jObj);

            //poner el marcador del pasajero
            BitmapDescriptor markerIconBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.pinlocationstart);

            service_marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(v_latitudServicio, v_longitudServicio))
                    .draggable(false)
                    .icon(markerIconBitmapDescriptor));

            v_servicio = true;

            btnPrincipal.setText(Constants.btnPpal.MOSTRAR_DIRECCION);
            btnPrincipal_text = btnPrincipal.getText().toString();

            ConstraintLayout cl_service_tarificacion =  findViewById(R.id.cl_service_tarificacion);
            cl_service_tarificacion.setVisibility(View.VISIBLE);

            RelativeLayout rl_tarificador = findViewById(R.id.rl_tarificador);
            rl_tarificador.setVisibility(View.VISIBLE);

            ConstraintLayout cl_rating_ = findViewById(R.id.cl_rating_);
            cl_rating_.setVisibility(View.GONE);

            TextView lladv_input_origen =  findViewById(R.id.lladv_input_origen);
            String json = "Recoger en " + jObj.getString("dir");
            lladv_input_origen.setText(json);

            TextView tv_titulo = findViewById(R.id.tv_titulo);
            json = "Recoger Pasajero";
            tv_titulo.setText(json);

            TextView lladv_input_destino =  findViewById(R.id.lladv_input_destino);
            json = "Destino: " + v_dir_destino;
            lladv_input_destino.setText(json);

            TableRow tr_recorrido_del_viaje_bsmst = findViewById(R.id.tr_recorrido_del_viaje_bsmst);
            tr_recorrido_del_viaje_bsmst.setVisibility(View.VISIBLE);

            if(jObj.has("tipo_serv")){
                if(jObj.getString("tipo_serv").equals("31")) {
                    tr_recorrido_del_viaje_bsmst.setVisibility(View.GONE);
                }
            }

            TextView tv_costo_aproximado_bsmst = findViewById(R.id.tv_costo_aproximado_bsmst);
            json ="$ "+format.format(Integer.parseInt(jObj.getString("valor")));
            tv_costo_aproximado_bsmst.setText(json);

            TextView tv_recorrido_del_viaje_bsmst =  findViewById(R.id.tv_recorrido_del_viaje_bsmst);
            json = format.format(Integer.parseInt(jObj.getString("recorrido"))) + " m";
            tv_recorrido_del_viaje_bsmst.setText(json);

            json = "No Iniciado";
            MapaPrincipalFragment.tv_tiempo_transcurrido_bsmst.setText(json);

            Button btn_bmst_cobrar =  findViewById(R.id.btn_bmst_cobrar);
            btn_bmst_cobrar.setVisibility(View.GONE);

            Button btn_actions =  findViewById(R.id.btn_actions);
            btn_actions.setVisibility(View.VISIBLE);

            btnPrincipal.setText(Constants.btnPpal.MOSTRAR_DIRECCION);
            btnPrincipal_text = btnPrincipal.getText().toString();
            btnPrincipal.setVisibility(View.GONE);

            tipo_tarifa = jObj.getString("tipotarifa");

            if(!tipo_tarifa.equals("66")){
                TableRow tr_horas_contratadas_bsmst = findViewById(R.id.tr_horas_contratadas_bsmst);
                tr_horas_contratadas_bsmst.setVisibility(View.GONE);

                TableRow tr_valor_hora_bsmst =  findViewById(R.id.tr_valor_hora_bsmst);
                tr_valor_hora_bsmst.setVisibility(View.GONE);
            }else{
                valor_hora = jObj.getString("horas_valor");
                TextView tv_horas_contratadas_bsmst= findViewById(R.id.tv_horas_contratadas_bsmst);
                tv_horas_contratadas_bsmst.setText(MainActivity.horas_contratadas);

                TextView tv_valor_hora_bsmst=  findViewById(R.id.tv_valor_hora_bsmst);
                text = "$ "+format.format(Math.round(Integer.parseInt(valor_hora)));
                tv_valor_hora_bsmst.setText(text);
                text = "Destino: Servicio por Horas";
                lladv_input_destino.setText(text);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void pregunta_aceptacion_servicio(JSONObject jObj) {
        //all llegar servicio, ocultamos el layout de pistas
        RelativeLayout rl_pista =  findViewById(R.id.rl_pista);
        rl_pista.setVisibility(View.GONE);

        try {
            if(leyendo_qr){
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("cmd", "A8");
                    jsonObj.put("servicio", jObj.getString("id"));
                    envia_json(jsonObj);
            }else {
                if (!v_ocupado) {
                    jServicio = jObj;
                    en_aceptacion_servicio = true;
                    wakeup();//pregunta aceptacion de servicio

                    v_disponible = false;
                    String sitio, barrio, razon_social, descripcion, v_valor, v_recorrido, v_t_recorrido="";

                    sitio = jObj.getString("dir");

                    v_direccion_aproval = sitio;
                    v_ttsDirBarr = sitio;

                    barrio = jObj.getString("barrio");

                    NumberFormat format = NumberFormat.getNumberInstance();

                    v_valor = "\nValor Aproximado: $ " + format.format(Integer.parseInt(jObj.getString("valor")));
                    v_recorrido = "\nRecorrido Aproximado: " + format.format(Integer.parseInt(jObj.getString("recorrido"))) + " m";
                    if(jObj.has("t_recorrido"))
                        v_t_recorrido = "\nTiempo Recorrido Aproximado: " +format.format(Integer.parseInt(jObj.getString("t_recorrido"))/60) + " min";
                    razon_social = jObj.getString("empresa");
                    v_razon_social = razon_social;
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
                        razon_social = "\nEMPRESA: " + v_razon_social;

                    v_info_adicional_aproval = barrio + razon_social + descripcion + v_valor + v_recorrido + v_t_recorrido;

                    if (v_tts) {
                        String nuevoMsg = v_ttsDirBarr;
                        nuevoMsg = nuevoMsg.replaceAll(" - ", " raya ");
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
                    new Handler().post(new Runnable() {
                        public void run() {
                            try {
                                if(bsdFragment_ServiceAproval!=null)
                                    bsdFragment_ServiceAproval.dismiss();

                                bsdFragment_ServiceAproval = BsModalServiceAproval.newInstance();
                                bsdFragment_ServiceAproval.show(getSupportFragmentManager(), "bsdFragment_ServiceAproval");
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    if (tempoView != null)
                        tempoView.setText(aceptando + " segs");

                    cdt = new CountDownTimer(aceptando * 1000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {

                            en_aceptacion_servicio = true;

                            String secs;
                            if ((millisUntilFinished / 1000) < 10) {
                                secs = "0" + (millisUntilFinished / 1000);
                                //t_view.setText(msg + "\nTiempo para aceptar: 00:" + secs + " segs");
                                if (tempoView != null)
                                    tempoView.setText(secs + " segs");
                        /*if (secs.equals("01")) {
                            alert.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                            alert.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
                        }*/
                            } else {
                                if (tempoView != null)
                                    tempoView.setText((millisUntilFinished / 1000) + " segs");
                                //t_view.setText(msg + "\nTiempo para aceptar: 00:" + (millisUntilFinished / 1000) + " segs");
                            }

                            aceptando = (int) (millisUntilFinished / 1000);
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

                                new Handler().post(new Runnable() {
                                    public void run() {
                                        try {
                                            if(bsdFragment_ServiceAproval!=null)
                                                bsdFragment_ServiceAproval.dismiss();


                                            Switch switch_turno = findViewById(R.id.switch_turno);

                                            if(switch_turno!=null)
                                                switch_turno.setChecked(false);
                                        } catch (IllegalStateException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }
                    }.start();
                } else {
                    if (cdt != null)
                        cdt.cancel();

                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("cmd", "14");
                    envia_json(jsonObj);

                    Switch switch_turno = findViewById(R.id.switch_turno);

                    if(switch_turno!=null)
                        switch_turno.setChecked(false);

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
            String v_direccion, v_indicacion, v_unidad, v_descripcion, v_nombre;

            v_idServicio = jObj.getString("servicio");
            v_clave = jObj.getString("clave");
            v_direccion = jObj.getString("dir");
            v_latitudServicio = Double.valueOf(jObj.getString("lat"));
            v_longitudServicio = Double.valueOf(jObj.getString("lng"));
            v_unidad = jObj.getString("unidad");
            v_descripcion = jObj.getString("desc");
            v_nombre = jObj.getString("nombre");
            v_razon_social = jObj.getString("empresa");
            v_temporizador = Integer.parseInt("0");
            v_indicacion = jObj.getString("indic");

            v_direccion_servicio = v_direccion;

            v_dir_destino = jObj.getString("dir_d");

            //if (!v_barrio.equals("0") && !v_barrio.equals(""))
            //    msg += "Barrio: " + v_barrio;
            if (!v_nombre.equals("0") && !v_nombre.equals(""))
                msg += "\n" + v_nombre;
            if (!v_unidad.equals("0") && !v_unidad.equals(""))
                msg += "\nUnidad: " + v_unidad;
            if (!v_razon_social.equals("0") && !v_razon_social.equals(""))
                msg += "\nEmpresa: " + v_razon_social;
            if (!v_descripcion.equals("0") && !v_descripcion.equals(""))
                msg += "\nDescripción: " + v_descripcion;
            if (!v_indicacion.equals("0") && !v_indicacion.equals(""))
                msg += "\nIndicación: " + v_indicacion;

            if(!v_dir_destino.equals(""))
                msg += "\nDestino: " + v_dir_destino;

            NumberFormat format = NumberFormat.getNumberInstance();

            if(jObj.has("valor"))
                msg += "\nValor Aproximado: $ " +format.format(Integer.parseInt(jObj.getString("valor")));
            if(jObj.has("recorrido"))
                msg += "\nRecorrido Aproximado: " +format.format(Integer.parseInt(jObj.getString("recorrido"))) + " m";
            if(jObj.has("t_recorrido"))
                msg += "\nTiempo Recorrido Aproximado: " +format.format(Integer.parseInt(jObj.getString("t_recorrido"))/60) + " min";

        }catch(JSONException e){
            e.printStackTrace();
        }
        return msg;
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
            service_marker.remove();
            service_marker = null;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnPrincipal.setBackgroundColor(getResources().getColor(R.color.accent));
                btnPrincipal.setText(Constants.btnPpal.PONERME_DISPONIBLE);
                btnPrincipal.setVisibility(View.VISIBLE);
                btnPrincipal_text = btnPrincipal.getText().toString();

                RelativeLayout rl_pista = findViewById(R.id.rl_pista);
                rl_pista.setVisibility(View.GONE);

                RelativeLayout rl_tarificador = findViewById(R.id.rl_tarificador);
                rl_tarificador.setVisibility(View.GONE);

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
                            }
                        } else {
                            callAlertBox.showMyAlertBox("Imposible Continuar, el nivel de su batería es muy Bajo (" + level + "%)");
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
                    fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commitAllowingStateLoss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if (currentFragment instanceof Fragment_conversaciones) {
                Class fragmentClass = MapaPrincipalFragment.class;
                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commitAllowingStateLoss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if (currentFragment instanceof Fragment_swipe_servicios) {
                Class fragmentClass = MapaPrincipalFragment.class;
                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commitAllowingStateLoss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if (currentFragment instanceof ProfileFragment) {
                Class fragmentClass = MapaPrincipalFragment.class;
                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commitAllowingStateLoss();
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
    @Override
    public void onLocationChanged(Location location) {
        //log("onLocationChanged " + location.toString());

        String str = String.format("%.5f", location.getLatitude());
        str = str.replace(",", ".");
        v_latitud = Double.parseDouble(str);
        str = String.format("%.5f", location.getLongitude());
        str = str.replace(",", ".");
        v_longitud = Double.parseDouble(str);
        v_fechaGPS = location.getTime() + "";
        v_precision = (int) location.getAccuracy();
        v_bearing = (int) location.getBearing();
        v_primera_lectura_gps = true;

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
                Inicio_sesion.datasource.createUbicacion(jsonObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            v_latitudPrevia = v_latitud;
            v_longitudPrevia = v_longitud;
        }

        if (mMap != null) {

            CameraPosition INIT = new CameraPosition.Builder()
                    .target(new LatLng(v_latitud, v_longitud))
                    .zoom(mMap.getCameraPosition().zoom)
                    .bearing(location.getBearing()) // orientation
                    //.tilt(50F) // viewing angle
                    .build();
            // use GoogleMap mMap to move camera into position
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(INIT));

        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        log("onProviderEnabled provider " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        log("onProviderDisabled provider " + provider);
        updateClient("1");
        try {
            JSONObject jObj = new JSONObject();
            jObj.put("cmd", "B4");
            jObj.put("lat", v_latitud + "");
            jObj.put("lng", v_longitud + "");
            jObj.put("date_gps", v_fechaGPS + "");
            Inicio_sesion.datasource.create(jObj.toString(),"B4");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateClient(final String s) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false).setTitle("GPS");

        final AlertDialog alert;
        alert = builder.create();

        final ScrollView s_view = new ScrollView(getApplicationContext());
        final TextView t_view = new TextView(getApplicationContext());
        t_view.setPadding(30, 5, 20, 10);

        t_view.setTextColor(Color.BLACK);

        t_view.setTextSize(v_tamanoFuente);
        s_view.addView(t_view);
        alert.setView(s_view);

        alert.show();

        new CountDownTimer(7000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(s.equals("1"))
                    t_view.setText("Por inactivación del GPS la aplicación se cerrará en " + (millisUntilFinished / 1000) + " segs");
                else if(s.equals("2"))
                    t_view.setText("Por activación de ubicaciones falsas la aplicación se cerrará en " + (millisUntilFinished / 1000) + " segs");
            }

            @Override
            public void onFinish() {
                finish();
            }
        }.start();
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
        if (id ==R.id.action_settings) {
            return false;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
        } else if (id ==R.id.nav_salir) {
            salir();
            return true;
        } else if (id ==R.id.nav_leer_qr) {
            if (v_primera_lectura_gps) {
                if(!v_disponible){
                    callAlertBox.showMyAlertBox("Para Leer un QR debes estar disponible");
                }else {
                    if (v_autenticado) {
                        if(v_enservicio){
                            callAlertBox.showMyAlertBox("Unidad en servicio");
                        }else {
                            leyendo_qr=true;
                            IntentIntegrator integrator = new IntentIntegrator(this);
                            integrator.setPrompt("Escanear código QR");
                            integrator.setOrientationLocked(true);
                            integrator.initiateScan();
                        }
                    } else {
                        callAlertBox.showMyAlertBox("Realizando Conexión! Intenta nuevamente");
                    }
                }
            }else{
                callAlertBox.showMyAlertBox("Esperando lectura de GPS");
            }

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
                    fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commitAllowingStateLoss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }

        in_conversacion=false;
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMessengerService = new Messenger(service);

            Message message = Message.obtain(null, TaxiLujoService.MSJ_NEW_ACTIVITY);
            sendMessageToSocket(message);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMessengerService = null;
            log( "onServiceDisconnected: Detectada la desconexión del servicio");
        }
    };

    @SuppressLint("HandlerLeak")
    Messenger mActivityMessenger = new Messenger(new Handler() {
        @Override
        public String toString() {
            return super.toString();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TaxiLujoService.MSJ_SEND_SOCKET:
                    from_SocketTask(msg.obj.toString());
                    break;
                case TaxiLujoService.MSJ_SOCKET_ERROR:
                    //showAlertDialog(getString(R.string.text_error_socket_server), false);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    });

    private void sendMessageToSocket(Message message) {
        try {
            message.replyTo = mActivityMessenger;
            if(mMessengerService!=null)
                mMessengerService.send(message);
        } catch (RemoteException error) {
            log("onServiceConnected: No es posible enviar el mensaje al servicio");
            error.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        log("onDestroy");
        super.onDestroy();

        /*NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(nManager!=null)
            nManager.cancelAll();*/

        stopService(new Intent(getApplicationContext(), TaxiLujoService.class));
        if (mMessengerService != null) {
            unbindService(mServiceConnection);
            mMessengerService = null;
        }

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        if(runnable!=null)
            tt_handler.removeCallbacks(runnable);

        if(mGoogleApiClient != null)
            mGoogleApiClient.disconnect();

    }

    synchronized void afficher(){

        if(tiempo_espera){
            TextView tv_tiempo_espera = findViewById(R.id.tv_tiempo_espera);
            if(tv_tiempo_espera!=null) {
                if (!tiempo.equals("")) {
                    String[] tokito = splitTotokens(tiempo + ":", ":");
                    segundos = Integer.parseInt(tokito[2]);
                    minutos = Integer.parseInt(tokito[1]);
                    horas = Integer.parseInt(tokito[0]);
                    segundos++;
                    tiempo = "";
                }
                segundos++;

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

                v_transcurrido = "Tiempo de Espera: " + hour + ":" + min + ":" + seg;
                tv_tiempo_espera.setText(v_transcurrido);
            }
        }

        v_countAlive++;

        if(v_servicio)
            alive = 10;
        else
            alive = 35;

        if (v_countAlive >= alive) {
            v_countAlive = 0;
            try {
                JSONObject jObj3 = new JSONObject();
                jObj3.put("cmd", "18");
                jObj3.put("bearing", v_bearing + "");
                envia_json(jObj3);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (k >= reconector) {
            k = 0;
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


                JSONArray jArr = Inicio_sesion.datasource.selectUbicacionPendientes();
                for (int i = 0; i < jArr.length(); i++) {
                    try {
                        servicios_ubicaciones(jArr.getJSONObject(i));
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }

                try {
                    JSONObject jObj = Inicio_sesion.datasource.select_tipo_para_enviar("registra_valor_servicio");
                    if (jObj.length() != 0) {
                        String valor = jObj.getString("descripcion");
                        if (!valor.equals("")) {
                            v_idServicioRuta = "0";
                            String vale_id = Inicio_sesion.datasource.select("vale_id");
                            //String servicio_id = Inicio_sesion.datasource.select("vale_servicio");

                            JSONObject jObj2 = new JSONObject();
                            jObj2.put("cmd", "69");
                            jObj2.put("empresa", empresa);
                            jObj2.put("s_id", v_idServicio);
                            jObj2.put("vale", vale_id);
                            jObj2.put("lat", v_latitud + "");
                            jObj2.put("lng", v_longitud + "");
                            jObj2.put("valor", total + "");
                            Inicio_sesion.datasource.create(jObj2.toString(), "69");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            trama_x_enviar_contador++;
            if (trama_x_enviar_contador >= 2) {
                //envia tramas guardadas en sqlite (cumplir,cancelar,en sitio,rechazar, etc)
                String[] v_tipo = {Constants.ACTION.TRAMA_X_ENVIAR, "CU", "V1", "V2", "34", "A7", "24", "A9", "B3", "B4", "C4", "C5", "69", "03", "04", "11", "10"};

                for (String aV_tipo : v_tipo) {
                    String trama_x_enviar = Inicio_sesion.datasource.select_v2(aV_tipo);
                    if (!trama_x_enviar.equals("")) {
                        try {
                            JSONObject jEnviar = new JSONObject(trama_x_enviar);
                            trama_x_enviar_contador = 0;
                            log("ENVIANDO TRAMA PENDIENTE " + trama_x_enviar);
                            //Message message = Message.obtain(null, TaxiLujoService.MSJ_SEND_SOCKET, trama_x_enviar);
                            // sendMessageToSocket(message);
                            envia_json(jEnviar);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }



    void servicios_ubicaciones(JSONObject jObj){
        Message message = Message.obtain(null, TaxiLujoService.MSJ_SEND_SOCKET, jObj);
        sendMessageToSocket(message);
    }

    private static void reconexion(String incoming, int tiempo) {
        v_desconexion = true;
        log("reconexion " + incoming + ", tiempo " + tiempo);
    }

    private void comienza_tarificador_preestablecido(JSONObject jObj, String desde){
        log("comienza_tarificador_preestablecido desde " +desde + " " + jObj.toString());
        v_enservicio = true;
        v_clave = null;
        confirmando = false;
        pulsaDisponible = false;
        if(jObj.has("dir_d")){
            try {
                v_dir_destino = jObj.getString("dir_d");
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        //toast("Iniciando Tarificación");
        v_ruteo = true;
        // valores dinamicos de tarificacion de acuerdo a la ciudad

        //al llegar al servicio ponemos el estado del telefono como en_servicio
        //solo para validar si es apagado durante un servicio activo que ya se encuentre tarificando
        //y vuelven a prender el celular
        Inicio_sesion.datasource.create("en_servicio", "estado_telefono");
        Inicio_sesion.datasource.create(v_idServicio, "id_servicio");

        try {
            tipo_tarifa = jObj.getString("tipotarifa");
            valor_hora = jObj.getString("horas_valor");
            horas_contratadas = jObj.getString("horas");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView tv_titulo = findViewById(R.id.tv_titulo);
        String json = "En Servicio";
        tv_titulo.setText(json);

        Button btn_actions = findViewById(R.id.btn_actions);
        btn_actions.setVisibility(View.GONE);

        Button btn_bmst_cobrar = findViewById(R.id.btn_bmst_cobrar);
        btn_bmst_cobrar.setVisibility(View.VISIBLE);

        btnPrincipal.setText(Constants.btnPpal.MOSTRAR_TARIFICACION);
        btnPrincipal_text = Constants.btnPpal.MOSTRAR_TARIFICACION;

        Tarificador.setActivity(MainActivity.this);
        Tarificador thTemporizador = new Tarificador(jObj);
        thTemporizador.start();

        v_tarificando = true;
        pendiente = true;

        try {
            JSONObject jObj3 = new JSONObject();
            jObj3.put("cmd", "A9");
            jObj3.put("lat", v_latitud + "");
            jObj3.put("lng", v_longitud + "");
            jObj3.put("date_gps", v_fechaGPS + "");
            Inicio_sesion.datasource.createRegistro(jObj3.toString(), "A9", 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void comienza_tarificador(final String desde) {
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
                                log("comienza_tarificador desde " + desde + " "+ response);//{"cmd":"get_tarifa","tpreciounidad":"96","tmetrounidad":"80","tespera":"50","tminima":"4700","tminimatermina":"4512","t_distancia_gps_error":"30","estado":"0","distancia":"0","valor":"0","tiempo":"0"}
                                JSONObject jObj = new JSONObject(response);
                                v_enservicio = true;
                                v_clave = null;
                                confirmando = false;
                                pulsaDisponible = false;
                                String text;
                                NumberFormat format = NumberFormat.getNumberInstance();
                                //toast("Iniciando Tarificación");
                                v_ruteo = true;

                                try {
                                    tipo_tarifa = jObj.getString("tipotarifa");
                                    valor_hora = jObj.getString("horas_valor");
                                    horas_contratadas = jObj.getString("horas");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                // valores dinamicos de tarificacion de acuerdo a la ciudad

                                //al llegar al servicio ponemos el estado del telefono como en_servicio
                                //solo para validar si es apagado durante un servicio activo que ya se encuentre tarificando
                                //y vuelven a prender el celular
                                Inicio_sesion.datasource.create("en_servicio", "estado_telefono");
                                Inicio_sesion.datasource.create(v_idServicio, "id_servicio");

                                if(bsdFragment_ServiceArrival != null)
                                    bsdFragment_ServiceArrival.dismiss();

                                btnPrincipal.setText(Constants.btnPpal.MOSTRAR_TARIFICACION);
                                btnPrincipal_text = Constants.btnPpal.MOSTRAR_TARIFICACION;

                                RelativeLayout rl_tiempo_espera = findViewById(R.id.rl_tiempo_espera);
                                rl_tiempo_espera.setVisibility(View.VISIBLE);

                                Tarificador.setActivity(MainActivity.this);
                                Tarificador thTemporizador = new Tarificador(jObj);
                                thTemporizador.start();

                                v_tarificando = true;
                                pendiente = true;

                                try {
                                    JSONObject jObj3 = new JSONObject();
                                    jObj3.put("cmd", "A9");
                                    jObj3.put("lat", v_latitud + "");
                                    jObj3.put("lng", v_longitud + "");
                                    jObj3.put("date_gps", v_fechaGPS + "");
                                    Inicio_sesion.datasource.createRegistro(jObj3.toString(), "A9", 0);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                v_servicio = true;

                                ConstraintLayout cl_service_tarificacion =  findViewById(R.id.cl_service_tarificacion);
                                cl_service_tarificacion.setVisibility(View.VISIBLE);

                                RelativeLayout rl_tarificador = findViewById(R.id.rl_tarificador);
                                rl_tarificador.setVisibility(View.VISIBLE);

                                ConstraintLayout cl_rating_ = findViewById(R.id.cl_rating_);
                                cl_rating_.setVisibility(View.GONE);

                                TextView lladv_input_origen =  findViewById(R.id.lladv_input_origen);
                                String json = "Origen: " + jServicio.getString("dir");
                                lladv_input_origen.setText(json);

                                TextView tv_titulo = findViewById(R.id.tv_titulo);
                                json = "En Servicio";
                                tv_titulo.setText(json);

                                TextView lladv_input_destino =  findViewById(R.id.lladv_input_destino);
                                json = "Destino: " + v_dir_destino;
                                lladv_input_destino.setText(json);

                                TableRow tr_recorrido_del_viaje_bsmst = findViewById(R.id.tr_recorrido_del_viaje_bsmst);
                                tr_recorrido_del_viaje_bsmst.setVisibility(View.VISIBLE);

                                if(jServicio.has("tipo_serv")){
                                    if(jServicio.getString("tipo_serv").equals("31")) {
                                        tr_recorrido_del_viaje_bsmst.setVisibility(View.GONE);
                                    }
                                }

                                TextView tv_costo_aproximado_bsmst = findViewById(R.id.tv_costo_aproximado_bsmst);
                                json ="$ "+format.format(Integer.parseInt(jServicio.getString("valor")));
                                tv_costo_aproximado_bsmst.setText(json);

                                TextView tv_recorrido_del_viaje_bsmst =  findViewById(R.id.tv_recorrido_del_viaje_bsmst);
                                json = format.format(Integer.parseInt(jServicio.getString("recorrido"))) + " m";
                                tv_recorrido_del_viaje_bsmst.setText(json);

                                Button btn_bmst_cobrar =  findViewById(R.id.btn_bmst_cobrar);
                                btn_bmst_cobrar.setVisibility(View.VISIBLE);

                                Button btn_actions =  findViewById(R.id.btn_actions);
                                btn_actions.setVisibility(View.GONE);

                                btnPrincipal.setVisibility(View.GONE);

                                tipo_tarifa = jServicio.getString("tipotarifa");

                                if(!tipo_tarifa.equals("66")){
                                    TableRow tr_horas_contratadas_bsmst = findViewById(R.id.tr_horas_contratadas_bsmst);
                                    tr_horas_contratadas_bsmst.setVisibility(View.GONE);

                                    TableRow tr_valor_hora_bsmst =  findViewById(R.id.tr_valor_hora_bsmst);
                                    tr_valor_hora_bsmst.setVisibility(View.GONE);
                                }else{
                                    valor_hora = jServicio.getString("horas_valor");
                                    TextView tv_horas_contratadas_bsmst= findViewById(R.id.tv_horas_contratadas_bsmst);
                                    tv_horas_contratadas_bsmst.setText(MainActivity.horas_contratadas);

                                    TextView tv_valor_hora_bsmst=  findViewById(R.id.tv_valor_hora_bsmst);
                                    text = "$ "+format.format(Math.round(Integer.parseInt(valor_hora)));
                                    tv_valor_hora_bsmst.setText(text);
                                    text = "Destino: Servicio por Horas";
                                    lladv_input_destino.setText(text);
                                }
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
                            params.put("idservicio", v_idServicio);
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

    void envia_json(JSONObject jObj) {
        try {
            jObj.put("lat"     , String.valueOf(v_latitud));
            jObj.put("lng"     , String.valueOf(v_longitud));
            jObj.put("date_gps", String.valueOf(v_fechaGPS));
            jObj.put("bearing" , String.valueOf(v_bearing));
            jObj.put("milis"   , String.valueOf(System.currentTimeMillis()));
            Message message = Message.obtain(null, TaxiLujoService.MSJ_SEND_SOCKET, jObj);
            sendMessageToSocket(message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void wakeup() {

        // Waking up mobile if it is sleeping
        WakeLocker.acquire(getApplicationContext());
        // Releasing wake lock
        WakeLocker.release();

        // MUESTRA LA APP EN PRIMER PLANO
        Intent it = new Intent("intent.my.action");
        it.setComponent(new ComponentName(getApplicationContext().getPackageName(), MainActivity.class.getName()));
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(it);

    }

    private void toast(final String json) {
        if(bsdFragment_toast!=null)
            bsdFragment_toast.dismiss();

        Bundle args = new Bundle();
        args.putString("json", json);
        bsdFragment_toast = BsModalToast.newInstance();
        bsdFragment_toast.setArguments(args);
        bsdFragment_toast.show(getSupportFragmentManager(), "BSDialog");
    }

    @Override
    public void onInit(int i) {
        log("tts iniciando");
        if (i == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.getDefault());

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                log("Lenguaje No soportado");
                v_tts = false;
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
    }

    @Override
    public void from_Fragment_swipe_servicios(JSONObject jObj) {
        log("from_Fragment_swipe_servicios: " + jObj.toString());
    }

    @Override
    public void from_Fragment_mensajes(JSONObject jObj) {
        log("from_Fragment_mensajes: " + jObj.toString());

        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
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
    public void onBackStackChanged() {
        log("onBackStackChanged()");
        FragmentManager fm = getSupportFragmentManager();
        int backStackEntryCount = fm.getBackStackEntryCount();
        for (int i = 0; i < backStackEntryCount; i++) {
            LogBackStackEntry(i + "", fm.getBackStackEntryAt(i));
        }
    }

    public static void LogBackStackEntry(String item,FragmentManager.BackStackEntry entry ){
        if(entry!=null){
            log(item + ". BackStackEntryName: " + entry.getName());
        }else{
            log("BackStackEntryName: <NULL>");
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        log("GPS onConnected");
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000); // Update location every ten seconds
        //mLocationRequest.setSmallestDisplacement(20);//cada 20 metros

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            v_latitud = mLastLocation.getLatitude();
            v_longitud = mLastLocation.getLongitude();
            if(mMap!=null)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(v_latitud, v_longitud) , 16f));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        log("GPS onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    interface FragmentRefreshListener{
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

    interface FragmentRefreshListener_chats{
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
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

    }

    void socket_inicia_sesion(JSONObject jObj) throws JSONException{
        String respuesta = jObj.getString("msj");
        if (respuesta.equals("")) {
            iniciaSesion = true;

            if (jObj.has("valor") && !viaje_interrumpido)
                total = Integer.parseInt(jObj.getString("valor"));

            if (jObj.has("valor"))
                valor_anterior_a_la_interrupcion = Integer.parseInt(jObj.getString("valor"));

            if (jObj.has("medio_pago"))
                s_medio_pago = jObj.getString("medio_pago");

            IniciaSesion();
            if (pulsaDisponible) {
                // Si el dispositivo estaba en estado disponible
                // antes de recibir una desconexión, entonces se
                // pone disponible automáticamente
                ponerme_disponible();
            }

            if (jObj.has("destino"))
                v_dir_destino = jObj.getString("destino");

            if (jObj.has("servicio"))
                v_idServicio = jObj.getString("servicio");

            revisa_servicio_pendiente(jObj);

            v_MapaPrincipalFragment.fromMainActivity_attached(jObj);
        } else {
            toast(respuesta);
            v_autenticado = false;
        }
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

        Inicio_sesion.datasource.create(Inicio_sesion.usuario, "cedula");

    }


}