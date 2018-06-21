package com.conductor.aeroturex;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.conductor.aeroturex.old.DBDataSource;
import com.conductor.aeroturex.service.TaxiLujoService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Inicio_sesion extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback,
        BsModalVehiculos.On_BsModalVehiculos_Listener,
        BsModalDialogo.On_BsModalDialogo_Listener{

    private static final int REQUEST_READ_PHONE_STATE = 0;
    Button account;
    EditText password, user;
    String empresa = "0", v_correo = "", v_celular = "", v_nombres = "", tv_placa = "", tv_modelo = "", tv_movil = "", tv_vigencia = "";
    public static int v_identificador;
    public static DBDataSource datasource;
    public static String mi_foto = "", usuario = "", clave = "", v_packageName = "", v_imei = null, v_ip = "", v_puerto = "", v_titulo, v_vehiculo = "";
    BottomSheetDialogFragment bsdFragment_Vehiculos;
    private Messenger mService;
    BottomSheetDialogFragment bsdFragment_dialogo = null;

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, TaxiLujoService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_inicio_sesion);

        user          = findViewById(R.id.user);
        password      = findViewById(R.id.password);
        account       = findViewById(R.id.account);
        v_packageName = getApplicationContext().getPackageName();
        log("packageName " + v_packageName);

        permission_granted();

    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    private void permission_granted() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            v_imei = telephonyManager.getDeviceId();
        }
        if (v_imei == null) {
            v_imei = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        }

        empresa    = getResources().getString(R.string.empresa);

        datasource = new DBDataSource(this);
        datasource.open();

        if (!datasource.select("ip").equals(""))
            v_ip = datasource.select("ip");
        if (!datasource.select("puerto").equals(""))
            v_puerto = datasource.select("puerto");

        ///TODO cambiar ip
        //v_ip = "192.168.0.227";
        v_ip = "104.130.135.143";
        v_puerto = "5044";

        //validamos que la unidad tenga un identificador definido
        String select = datasource.select("unidad");
        v_identificador = (int) datasource.selectIdentificador();
        if (select.equals("") || v_identificador <= 0) {
            consulta_configurar_unidad();
        } else {
            account.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    //revisar si esta el GPS activo
                    if (manager != null) {
                        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            buildAlertMessageNoGps();
                        } else {

                            usuario = user.getText().toString();
                            clave   = password.getText().toString();

                            if (usuario.length() == 0 || clave.length() == 0) {
                                if (bsdFragment_dialogo != null)
                                    bsdFragment_dialogo.dismiss();

                                Bundle args = new Bundle();
                                args.putString("title", "Información");
                                args.putString("text", "Campos Incompletos");
                                args.putString("text2", "");
                                bsdFragment_dialogo = BsModalDialogo.newInstance();
                                bsdFragment_dialogo.setArguments(args);
                                bsdFragment_dialogo.show(getSupportFragmentManager(), "BsModalDialogo");
                            } else {
                                autenticar();
                            }
                        }
                    }
                }
            });

        }
        String v_cedula = datasource.select("cedula");
        user.setText(v_cedula);
        if (v_cedula.equals(""))
            user.requestFocusFromTouch();
        else
            password.requestFocusFromTouch();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    permission_granted();
                }
                break;

            default:
                break;
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Por favor activar GPS para continuar")
                .setCancelable(false)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void autenticar() {
        String text = "INICIANDO...";
        account.setText(text);
        Map<String, String> params = new HashMap<>();
        params.put("imei",    v_imei);
        params.put("usuario", usuario);
        params.put("clave",   clave);
        params.put("veq_id",  v_vehiculo);
        params.put("nick",    String.valueOf(v_identificador));
        sendLoginSocket(params);
    }

    private void sendLoginSocket(Map<String, String> params) {
        if (mService != null) {
            Message message = Message.obtain(null, TaxiLujoService.MSJ_LOGIN_SOCKET, params);
            message.replyTo = new Messenger(messageHandler);

            try {
                mService.send(message);
            } catch (RemoteException error) {
                log("sendLoginSocket: No es posible enviar el mensaje al servicio");
                error.printStackTrace();
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TaxiLujoService.MSJ_SEND_SOCKET:
                    JSONObject jObj = (JSONObject) msg.obj;
                    log("RECIBE: " + jObj.toString());
                    try {
                        String comando = jObj.getString("cmd");
                        v_correo       = jObj.getString("email");
                        v_celular      = jObj.getString("cel");
                        v_nombres      = jObj.getString("nombre");
                        if (comando.equals("19")) {

                            InputMethodManager imm = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);
                            if (imm != null) {
                                imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
                            }

                            String respuesta = jObj.getString("msj");
                            if (respuesta.equals("")) {

                                mi_foto = jObj.getString("cont_foto");

                                final JSONArray jArr = jObj.getJSONArray("moviles");
                                if (jArr.length() == 0) {
                                    log("sin moviles");
                                } else if (jArr.length() == 1) {

                                    JSONObject JSONCliente = jArr.getJSONObject(0);
                                    v_titulo               = JSONCliente.getString("movil");
                                    v_vehiculo             = JSONCliente.getString("vehiculo");
                                    tv_placa               = JSONCliente.getString("placa");
                                    tv_modelo              = JSONCliente.getString("modelo");
                                    tv_movil               = JSONCliente.getString("movil");
                                    tv_vigencia            = JSONCliente.getString("vigencia");

                                    Bundle b = new Bundle();
                                    b.putString("json", jObj.toString());
                                    Intent it = new Intent(Inicio_sesion.this, MainActivity.class);
                                    it.putExtras(b);
                                    startActivity(it);
                                } else {

                                    if(bsdFragment_Vehiculos!=null)
                                        bsdFragment_Vehiculos.dismiss();

                                    Bundle args = new Bundle();
                                    args.putString("json", jObj.toString());

                                    bsdFragment_Vehiculos = BsModalVehiculos.newInstance();
                                    bsdFragment_Vehiculos.setArguments(args);
                                    bsdFragment_Vehiculos.show(getSupportFragmentManager(), "bsdFragment_Vehiculos");
                                }
                            } else {
                                String text = "INGRESAR";
                                account.setText(text);

                                if (bsdFragment_dialogo != null)
                                    bsdFragment_dialogo.dismiss();

                                Bundle args = new Bundle();
                                args.putString("title", "Información");
                                args.putString("text", respuesta);
                                args.putString("text2", "");
                                bsdFragment_dialogo = BsModalDialogo.newInstance();
                                bsdFragment_dialogo.setArguments(args);
                                bsdFragment_dialogo.show(getSupportFragmentManager(), "BsModalDialogo");
                            }
                        }
                    } catch (JSONException error) {
                        error.printStackTrace();
                    }
                    break;
                case TaxiLujoService.MSJ_SOCKET_ERROR:
                    //showAlertDialog(getString(R.string.text_error_http_server), true);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }

        @Override
        public String toString() {
            return super.toString();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log("Inicio_sesion Termina");
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mService != null) {
            unbindService(mConnection);
            mService = null;
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        finish();
    }

    void configurar_unidad(JSONObject jObj) throws JSONException {
        v_titulo = jObj.getString("unidad");

        // si el identificador es mayor a cero, lo guardamos
        if (Integer.parseInt(v_titulo) > 0) {
            datasource.deleteTipo("unidad");
            datasource.deleteTipo("identificador");
            datasource.deleteTipo("ip");
            datasource.deleteTipo("puerto");
            datasource.createRegistro(v_titulo, "unidad", 0);
            datasource.createRegistro(jObj.getString("identificador"), "identificador", 0);
            datasource.createRegistro(jObj.getString("ip"), "ip", 0);
            datasource.createRegistro(jObj.getString("puerto"), "puerto", 0);

            if (bsdFragment_dialogo != null)
                bsdFragment_dialogo.dismiss();

            Bundle args = new Bundle();
            args.putString("title", "Información");
            args.putString("text", "Unidad Configurada " + v_titulo + ".");
            args.putString("text2", "");
            args.putString("tipo", "salir_app");
            bsdFragment_dialogo = BsModalDialogo.newInstance();
            bsdFragment_dialogo.setArguments(args);
            bsdFragment_dialogo.show(getSupportFragmentManager(), "BsModalDialogo");

        } else {

            if (bsdFragment_dialogo != null)
                bsdFragment_dialogo.dismiss();

            Bundle args = new Bundle();
            args.putString("title", "Información");
            args.putString("text", "Este Dispositivo No tiene Identificador Asignado.\nConfigurar IMEI:\n" + v_imei + ".");
            args.putString("text2", "");
            args.putString("tipo", "salir_app");
            bsdFragment_dialogo = BsModalDialogo.newInstance();
            bsdFragment_dialogo.setArguments(args);
            bsdFragment_dialogo.show(getSupportFragmentManager(), "BsModalDialogo");
        }
    }

    private void consulta_configurar_unidad() {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        String url = "https://www.servidor.com.co/gateway/Cpp/c01_envioconfiguracion";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        try {
                            log("c01_envioconfiguracion " + response);
                            JSONObject jObj = new JSONObject(response);
                            configurar_unidad(jObj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                log(error.toString());
                if (bsdFragment_dialogo != null)
                    bsdFragment_dialogo.dismiss();

                Bundle args = new Bundle();
                args.putString("title", "Información");
                args.putString("text", "Error en el envío");
                args.putString("text2", "");
                bsdFragment_dialogo = BsModalDialogo.newInstance();
                bsdFragment_dialogo.setArguments(args);
                bsdFragment_dialogo.show(getSupportFragmentManager(), "BsModalDialogo");
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("imei", v_imei);
                params.put("empresa", empresa);
                return params;
            }
        };
        int socketTimeout = 50000; //50 segundos de timeout para que no envie doble la peticion
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        postRequest.setRetryPolicy(policy);
        queue.add(postRequest);
    }

    private static void log(String s) {
        Log.d(Inicio_sesion.class.getSimpleName(), "######" + s + "######");
    }

    @Override
    public void from_BsModalVehiculos(JSONObject jObj) {
        log("from_BsModalVehiculos " + jObj.toString());
        if (bsdFragment_Vehiculos != null)
            bsdFragment_Vehiculos.dismiss();

        try {
            v_identificador = Integer.parseInt(jObj.getString("id"));
            v_titulo        = jObj.getString("movil");
            v_vehiculo      = jObj.getString("vehiculo");
            tv_placa        = jObj.getString("placa");
            tv_modelo       = jObj.getString("modelo");
            tv_movil        = jObj.getString("movil");
            tv_vigencia     = jObj.getString("vigencia");
            login_con_vehiculo();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void envia_json(JSONObject jObj) {
        try {
            jObj.put("lat", "0");
            jObj.put("lng", "0");
            jObj.put("date_gps", "0");

            Message message = Message.obtain(null, TaxiLujoService.MSJ_SEND_SOCKET, jObj);
            sendMessageToSocket(message);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

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
                    JSONObject jsonObject = (JSONObject) msg.obj;
                    try {
                        switch (jsonObject.getString("cmd")) {
                            case "PCC":
                                break;
                        }
                    } catch (JSONException error) {
                        log("handleMessage: No es posible convertir la cadena en JSON");
                        error.printStackTrace();
                    }
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
            mService.send(message);
        } catch (RemoteException error) {
            log("onServiceConnected: No es posible enviar el mensaje al servicio");
            error.printStackTrace();
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            log("onServiceConnected: Realizada la conexión con el servicio");
            mService = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            log("onServiceDisconnected: Detectada la des-conexión del servicio");
        }
    };

    void login_con_vehiculo() throws  JSONException{

        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);

            JSONObject jsonObj = new JSONObject();
            jsonObj.put("cmd", "19");
            jsonObj.put("usuario", usuario);
            jsonObj.put("tipo", "driver");
            jsonObj.put("clave", clave);
            jsonObj.put("imei", v_imei);
            jsonObj.put("nick", v_identificador + "");
            jsonObj.put("iniciado", "si");
            jsonObj.put("veq_id", v_vehiculo);
            jsonObj.put("version", pinfo.versionName);
            envia_json(jsonObj);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void from_BsModalDialogo() {
        finish();
    }
}
