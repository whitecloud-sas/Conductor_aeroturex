package com.conductor.aeroturex;

import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.conductor.aeroturex.old.callAlertBox;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class Inicio_sesion
        extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback,
        BsModalVehiculos.On_BsModalVehiculos_Listener {

    private static final int REQUEST_READ_PHONE_STATE = 0;
    Button account;
    EditText password, user;
    static Context mContext;
    String empresa = "0", nickname = "0";
    Socket nsocket = null;
    public static NetworkTask networktask = null;
    public static int v_identificador;
    public static DBDataSource datasource;
    public static String mi_foto="",usuario = "", clave = "", v_packageName = "", v_imei = "0", v_ip = "", v_puerto = "", v_titulo,
            v_vehiculo, v_correo = "", v_celular = "", v_nombres = "", tv_placa = "", tv_modelo = "", tv_movil = "", tv_vigencia = "";
    BottomSheetDialogFragment bsdFragment_Vehiculos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_inicio_sesion);
        user = (EditText) findViewById(R.id.user);
        password = (EditText) findViewById(R.id.password);
        account = (Button) findViewById(R.id.account);
        mContext = Inicio_sesion.this;

        callAlertBox callAlertBox_ = new callAlertBox();
        callAlertBox_.setActivity(this, null);

        v_packageName = getApplicationContext().getPackageName();
        log("packageName " + v_packageName);

        permission_granted();

        //user.setText("1234");
        //password.setText("1");

    }

    private void permission_granted() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        v_imei = telephonyManager.getDeviceId();
        if (v_imei == null) {
            v_imei = "000000000000000";
        }

        empresa = getResources().getString(R.string.empresa);

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

                    // returns true if mock location enabled, false if not enabled.
                    //if (Settings.Secure.getString(getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).equals("0")){

                    final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    //revisar si esta el GPS activo
                    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        buildAlertMessageNoGps();
                    } else {

                        usuario = user.getText().toString();
                        clave = password.getText().toString();

                        if (usuario.length() == 0 || clave.length() == 0) {
                            showMyAlertBox("Campos Incompletos");
                        } else {
                            autenticar();
                        }
                    }
                    //}else{
                    //showMyAlertBox("Imposible Continuar, Desactivar ubicaciones falsas");
                    //}
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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
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

        account.setText("INICIANDO...");

        espere();

        networktask = new NetworkTask(v_ip, v_puerto);
        networktask.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log("Inicio_sesion Termina");
    }

    static void espere() {
        //espere = ProgressDialog.show(mContext, "Iniciando", "Realizando verificación...", true);
    }

    static void termine() {
        //if (espere != null)
         //   espere.dismiss();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        salir();
    }

    static public void salir() {
        System.exit(0);
    }

    public static void showMyAlertBox(String id) {
        AlertDialog.Builder ad = new AlertDialog.Builder(mContext);
        ad.setCancelable(false); // This blocks the 'BACK' button if false
        ad.setMessage(id);

        ad.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        ad.show();

    }

    static void configurar_unidad(JSONObject jObj) throws JSONException {
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

            AlertDialog.Builder ad = new AlertDialog.Builder(mContext);
            ad.setCancelable(false); // This blocks the 'BACK' button if false
            ad.setMessage("Unidad Configurada " + v_titulo + ".");
            ad.setNegativeButton("Salir de Aplicación", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    System.exit(0);
                }
            });
            ad.show();

        } else {

            AlertDialog.Builder ad = new AlertDialog.Builder(mContext);
            ad.setCancelable(false); // This blocks the 'BACK' button if false
            ad.setMessage("Este Dispositivo No tiene Identificador Asignado.\nConfigurar IMEI:\n" + v_imei + ".");
            ad.setNegativeButton("Salir de Aplicación", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    System.exit(0);
                }
            });
            ad.show();

        }
    }

    private void consulta_configurar_unidad() {
        espere();
        RequestQueue queue = Volley.newRequestQueue(mContext);

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
                        termine();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                log(error.toString());
                termine();
                callAlertBox.showMyAlertBox("Error en el envio");
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
        if(bsdFragment_Vehiculos!=null)
            bsdFragment_Vehiculos.dismiss();

        try {


                    v_identificador = Integer.parseInt(jObj.getString("id"));
                    v_titulo = jObj.getString("movil");
                    v_vehiculo = jObj.getString("vehiculo");

                    tv_placa = jObj.getString("placa");
                    tv_modelo = jObj.getString("modelo");
                    tv_movil = jObj.getString("movil");
                    tv_vigencia = jObj.getString("vigencia");


            networktask.SendDataToNetwork("KC");
            Intent it = new Intent(Inicio_sesion.this, MainActivity.class);
            startActivity(it);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private class NetworkTask extends AsyncTask<Void, byte[], Boolean> {
        // Network Socket
        InputStream nis; // Network Input Stream
        OutputStream nos; // Network Output Stream
        String data = ""; // Data Received
        String ip, prt;

        NetworkTask(String ip, String prt) {
            this.ip = ip;
            this.prt = prt;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(Void... params) { // This runs on a
            // different thread

            try {
                log("doInBackground: Creating socket " + ip + ":" + prt);
                SocketAddress sockaddr = new InetSocketAddress(ip, Integer.parseInt(prt));
                nsocket = new Socket();
                nsocket.connect(sockaddr); // 3 second connection timeout
                if (nsocket.isConnected()) {

                    nis = nsocket.getInputStream();
                    nos = nsocket.getOutputStream();
                    log("AsyncTask doInBackground: Socket created, streams assigned");

                    StringTokenizer tokens = new StringTokenizer(v_identificador + "", ".");
                    nickname = tokens.nextToken();

                    try {
                        JSONObject jsonObj = new JSONObject();
                        jsonObj.put("cmd", "19");
                        jsonObj.put("usuario", usuario);
                        jsonObj.put("tipo", "driver");
                        jsonObj.put("clave", clave);
                        jsonObj.put("imei", v_imei);
                        jsonObj.put("nick", nickname);
                        jsonObj.put("iniciado", "no");
                        jsonObj.put("veq_id", "0");
                        envia_json(jsonObj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {// reconecta si hay desconexión
                                account.setText("INGRESAR");
                            }
                        });
                    }

                    byte[] buffer = new byte[4096];
                    int read = nis.read(buffer, 0, 4096);// This is blocking

                    while (read != -1) {
                        byte[] tempdata = new byte[read];
                        System.arraycopy(buffer, 0, tempdata, 0, read);
                        publishProgress(tempdata);
                        read = nis.read(buffer, 0, 4096);// This is blocking
                    }
                }
            } catch (final IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {// reconecta si hay desconexión
                        account.setText("INGRESAR");
                    }
                });
            } finally {
                try {
                    if (nis != null)
                        nis.close();
                    if (nos != null)
                        nos.close();
                    if (nsocket != null)
                        nsocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {// reconecta si hay desconexión
                            account.setText("INGRESAR");
                        }
                    });
                }
                log("AsyncTask doInBackground: Finished");
            }
            termine();
            return false;
        }

        void SendDataToNetwork(String cmd) { // You run this from the main thread.
            try {
                if (nsocket.isConnected()) {
                    log("ENVIANDO: " + cmd);
                    cmd = cmd + "\n";
                    nos.write(cmd.getBytes());
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {// reconecta si hay desconexión
                            account.setText("INGRESAR");
                        }
                    });
                    log("AsyncTask SendDataToNetwork: Cannot send message. Socket is closed " + cmd);
                }
            } catch (Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {// reconecta si hay desconexión
                        account.setText("INGRESAR");
                    }
                });
                log("AsyncTask SendDataToNetwork: Message send failed. " + e.toString() + " " + cmd);
            }
            termine();
        }

        @Override
        protected void onProgressUpdate(byte[]... values) {
            if (values.length > 0) {
                // log("onProgressUpdate: " + values[0].length + " bytes recibidos.");

                data = new String(values[0]);

                StringTokenizer stOR = new StringTokenizer(data, "\n");
                int orCount = stOR.countTokens();

                for (int ji = 0; ji < orCount; ji++) {

                    // Log.i("AsyncTask", "onProgressUpdate: Reading "+ (ji+1));
                    data = stOR.nextToken();
                    data = data.replace("\n", "");
                    log("RECIBE: " + data);
                    try {
                        JSONObject jObj = new JSONObject(data);
                        String comando = jObj.getString("cmd");
                        v_correo = jObj.getString("email");
                        v_celular = jObj.getString("cel");
                        v_nombres = jObj.getString("nombre");
                        if (comando.equals("19")) {

                            InputMethodManager imm = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(password.getWindowToken(), 0);

                            String respuesta = jObj.getString("msj");
                            if (respuesta.equals("")) {

                                mi_foto = jObj.getString("cont_foto");

                                final JSONArray jArr = jObj.getJSONArray("moviles");
                                if (jArr.length() == 0) {
                                    log("sin moviles");
                                } else if (jArr.length() == 1) {

                                    JSONObject JSONCliente = jArr.getJSONObject(0);
                                    v_titulo = JSONCliente.getString("movil");
                                    v_vehiculo = JSONCliente.getString("vehiculo");

                                    tv_placa = JSONCliente.getString("placa");
                                    tv_modelo = JSONCliente.getString("modelo");
                                    tv_movil = JSONCliente.getString("movil");
                                    tv_vigencia = JSONCliente.getString("vigencia");

                                    networktask.SendDataToNetwork("KC");

                                    networktask.cancel(true);

                                    Intent it = new Intent(Inicio_sesion.this, MainActivity.class);
                                    startActivity(it);
                                } else {

                                    if(bsdFragment_Vehiculos!=null)
                                        bsdFragment_Vehiculos.dismiss();

                                    Bundle args = new Bundle();
                                    args.putString("json", jObj.toString());

                                    bsdFragment_Vehiculos = BsModalVehiculos.newInstance();
                                    bsdFragment_Vehiculos.setArguments(args);
                                    bsdFragment_Vehiculos.show(getSupportFragmentManager(), "bsdFragment_Vehiculos");


                                   /* final AlertDialog.Builder singlechoicedialog = new AlertDialog.Builder(mContext);
                                    final CharSequence[] Report_items = new CharSequence[jArr.length()];

                                    for (int i = 0; i < jArr.length(); i++) {
                                        JSONObject JSONCliente = jArr.getJSONObject(i);
                                        //log("vehiculos: "+getString("placa", JSONCliente));//placa - movil - id
                                        Report_items[i] = getString("placa", JSONCliente) + " - " + getString("movil", JSONCliente);
                                    }
                                    singlechoicedialog.setTitle("Seleccionar vehículo");

                                    singlechoicedialog.setSingleChoiceItems(Report_items, -1, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int item) {
                                            // get selected value
                                            //String value = Report_items[item].toString();
                                            //log("Selected position::" + value);
                                            try {
                                                for (int i = 0; i < jArr.length(); i++) {
                                                    if (item == i) {
                                                        JSONObject JSONCliente = jArr.getJSONObject(i);
                                                        v_identificador = Integer.parseInt(getString("id", JSONCliente));
                                                        v_titulo = getString("movil", JSONCliente);
                                                        v_vehiculo = getString("vehiculo", JSONCliente);

                                                        tv_placa = getString("placa", JSONCliente);
                                                        tv_modelo = getString("modelo", JSONCliente);
                                                        tv_movil = getString("movil", JSONCliente);
                                                        tv_vigencia = getString("vigencia", JSONCliente);

                                                    }
                                                }
                                                networktask.SendDataToNetwork("KC");
                                                Intent it = new Intent(Inicio_sesion.this, MainActivity.class);
                                                startActivity(it);
                                                dialog.cancel();
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    AlertDialog alert_dialog = singlechoicedialog.create();
                                    alert_dialog.show();*/
                                }
                            } else {
                                account.setText("INGRESAR");
                                showMyAlertBox(respuesta);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            termine();
            try {
                if (networktask != null) {
                    networktask.cancel(true);
                }
                if (nsocket != null)
                    nsocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            log("Cancelled.");
            try {
                if (nsocket != null)
                    nsocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            termine();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            termine();
        }
    }

    void envia_json(JSONObject jObj) {
        try {
            jObj.put("lat", "0");
            jObj.put("lng", "0");
            jObj.put("date_gps", "0");
            String nsend = jObj.toString();
            networktask.SendDataToNetwork(nsend);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
