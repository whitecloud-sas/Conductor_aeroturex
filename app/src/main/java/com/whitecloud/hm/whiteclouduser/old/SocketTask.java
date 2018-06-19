package com.whitecloud.hm.whiteclouduser.old;

import android.os.AsyncTask;
import android.util.Log;

import com.whitecloud.hm.whiteclouduser.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.whitecloud.hm.whiteclouduser.MainActivity.g;
import static com.whitecloud.hm.whiteclouduser.MainActivity.otraActividad;
import static com.whitecloud.hm.whiteclouduser.MainActivity.v_desconexion;
import static com.whitecloud.hm.whiteclouduser.MainActivity.v_reconexion;

public class SocketTask extends AsyncTask<Void, byte[], Boolean> {
    // Network Socket
    private InputStream nis; // Network Input Stream
    private OutputStream nos; // Network Output Stream
    private String ip, prt;
    private ExecutorService exec;
    public static Socket nsocket = null;

    private from_SocketTask listener;

    public interface from_SocketTask {
        void from_SocketTask(String cadena);
        void onSocketConnected(String cadena);
    }

    public SocketTask(String ip, String prt, from_SocketTask listener) {

        log("CONECTANDO EN " + ip + ":" + prt);
        this.ip = ip;
        this.prt = prt;
        this.listener = listener;
        exec = Executors.newSingleThreadExecutor();
    }

    @Override
    protected void onPreExecute() {
        log("onPreExecute");
    }

    @Override
    protected Boolean doInBackground(Void... params) { // This runs on a
        // different thread
        boolean result;

        try {
            log("AsyncTask doInBackground: Creating socket");
            SocketAddress sockaddr = new InetSocketAddress(ip, Integer.parseInt(prt));
            nsocket = new Socket();
            nsocket.connect(sockaddr); // 3 second connection timeout
            if (nsocket.isConnected()) {

                nis = nsocket.getInputStream();
                nos = nsocket.getOutputStream();
                log("AsyncTask doInBackground: Socket created, streams assigned");

                v_desconexion = false;

                //networktask.SendDataToNetwork("00|" + Inicio_sesion.v_identificador + "|" + MainActivity.empresa + "|" + Inicio_sesion.email + "|" + Inicio_sesion.v_imei + "|" + v_latitud + "|" + v_longitud);
                MainActivity.login();

                listener.onSocketConnected("");

                byte[] buffer = new byte[4096];
                int read = nis.read(buffer, 0, 4096);// This is blocking

                while (read != -1) {
                    byte[] tempdata = new byte[read];
                    System.arraycopy(buffer, 0, tempdata, 0, read);
                    publishProgress(tempdata);
                    read = nis.read(buffer, 0, 4096);// This is blocking
                }
            }
            v_reconexion = 0;
        } catch (final IOException e) {
            g = 0;
            log("doInBackground: IOException");
            v_desconexion = true;
            v_reconexion = 0;
            e.printStackTrace();
        } catch (final Exception e) {
            g = 0;
            log("doInBackground: Exception");
            v_desconexion = true;
            v_reconexion = 0;
            e.printStackTrace();
        } finally {
            v_reconexion++;
            result = true;
            log("finally Contador reconectando " + v_reconexion);
            try {
                if (nis != null)
                    nis.close();
                if (nos != null)
                    nos.close();
                if (nsocket != null)
                    nsocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            log("doInBackground: Finished");
            if (g < 0)
                g = MainActivity.reconector;
            reconexion("2", g);
            v_desconexion = true;
        }
        return result;
    }

    public void SendDataToNetwork(final String cmd) { //You run this from the main thread.
        if (!v_desconexion) {
            exec.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (nsocket.isConnected() && nos != null) {
                            log("ENVIA: " + cmd);
                            //reemplaza todos los posibles saltos de linea que se puedan enviar hacia el servidor con espacio, dejando solo el salto de linea de esta función
                            String cmd2 = cmd.replace(System.getProperty("line.separator"), " ") + "\n";
                            nos.write(cmd2.getBytes());
                            //ponemos el contador en cero para evitar enviar cada 35 segundos si han habido envios previos
                            MainActivity.v_countAlive = 0;
                        } else {
                            log("SendDataToNetwork: Cannot send message. Socket is closed " + cmd);
                            v_desconexion = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        log("SendDataToNetwork: Message send failed." + cmd);
                        v_desconexion = true;
                    }
                }
            });
        }
    }

    @Override
    protected void onProgressUpdate(byte[]... values) {

        if (values.length > 0) {

            // Log.i("AsyncTask", "onProgressUpdate: " + values[0].length + " bytes recibidos.");
            if (otraActividad != null) {// cerramos otros Intents para
                // evitar cierres inesperados
                otraActividad.finish();
                otraActividad = null;
            }
            String data = new String(values[0]);//data received

            StringTokenizer stOR = new StringTokenizer(data, "\n");
            int orCount = stOR.countTokens();

            for (int ji = 0; ji < orCount; ji++) {
                //MainActivity ma = new MainActivity();
                //ma.socket_receiver(stOR.nextToken());
                listener.from_SocketTask(stOR.nextToken());
            }
        }
    }

    @Override
    protected void onCancelled() {
        log("AsyncTask Cancelled.");
        try {
            if (nsocket != null)
                nsocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            log("AsyncTask onPostExecute: Completed with an Error.");
            g = 0;
            v_desconexion = true;
        } else {
            log("AsyncTask onPostExecute: Completed.");
            v_desconexion = true;
            g = 0;
        }
    }

    public static void reconexion(String incoming, int tiempo) {
        v_desconexion = true;
        log("reconexion " + incoming + ", tiempo " + tiempo);
    }

    private static void log(String s) {
        Log.d(SocketTask.class.getSimpleName(), "######" + s + "######");
    }
}
