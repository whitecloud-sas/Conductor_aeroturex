package com.conductor.aeroturex.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.conductor.aeroturex.Inicio_sesion;
import com.conductor.aeroturex.MainActivity;
import com.conductor.aeroturex.R;
import com.conductor.aeroturex.old.AsyncSocketHandler;
import com.conductor.aeroturex.old.AsyncSocketThread;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class TaxiLujoService extends Service {

    public static final int MSJ_SOCKET_ERROR = 0;
    public static final int MSJ_LOGIN_SOCKET = 10;
    //public static final int MSJ_RESTORE_SOCKET = 11;
    public static final int MSJ_NEW_ACTIVITY = 20;
    public static final int MSJ_SEND_SOCKET  = 30;

    private String RETRY_CONNECTION = "no";
    private int RETRY_COUNT = 0;
    private Handler mTimerHandler = new Handler();

    private final Messenger mMessenger = new Messenger(new IncomingHandler());
    private static AsyncSocketThread socketThread;
    private static Messenger replyTo;
    static HashMap params;

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSJ_LOGIN_SOCKET:
                    if (socketThread.getState() == Thread.State.NEW){
                        socketThread.start();
                    }else{
                        socketThread.interrupt();
                        socketThread = new AsyncSocketThread(socketHandler);
                        socketThread.start();
                    }

                    params =  (HashMap) msg.obj;
                    replyTo = msg.replyTo;
                    break;
                /*case MSJ_RESTORE_SOCKET:
                    socketThread.start();
                    //mAuthDevice = (AuthDevice) msg.obj;
                    replyTo = msg.replyTo;
                    break;*/
                case MSJ_NEW_ACTIVITY:
                    log( "handleMessage: Conectando con una nueva actividad");
                    replyTo = msg.replyTo;
                    break;
                case MSJ_SEND_SOCKET:
                    socketThread.sendDataToSocket( msg.obj.toString());
                default:
                    super.handleMessage(msg);
            }
        }
    }

    void notificacion(){
        // Instanciamos e inicializamos nuestro manager.
        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(getApplicationContext())
                .setContentTitle(getText(R.string.app_name))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                //.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                ;
        Notification n;

        n = builder.build();

        n.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        if (nManager != null)
            nManager.notify(1, n);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (socketThread == null) {
            log("onCreate: Creando el objeto AsyncSocketThread");
            socketThread = new AsyncSocketThread(socketHandler);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        log( "onBind: " + mMessenger.toString());

        return mMessenger.getBinder();
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        super.unbindService(conn);
    }

    /*@Override
    public boolean stopService(Intent name) {
        log( "stopService: Deteniendo el servicio");
        socketThread.closeSocketConnection();
        socketThread.interrupt();
        socketThread = null;
        replyTo = null;
        return super.stopService(name);
    }*/

    @Override
    public void onDestroy() {
        mTimerHandler.removeCallbacks(r);
        log("onDestroy: Eliminando el servicio");

        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(nManager!=null)
            nManager.cancelAll();

        socketThread.closeSocketConnection();
        socketThread.interrupt();
        socketThread = null;
        replyTo      = null;

        super.onDestroy();
        System.exit(0);
    }

    Runnable r = new Runnable() {
        @Override
        public void run() {
            socketThread = new AsyncSocketThread(socketHandler);
            socketThread.start();
        }
    };

    private void reConnectSocket() {
        if(mTimerHandler!=null) {
            mTimerHandler.postDelayed( r, 3000);
        }
    }

    private AsyncSocketHandler socketHandler = new AsyncSocketHandler() {
        @Override
        public void didReceiveData(JSONObject data) {
            if (replyTo != null) {
                try {
                    Message message = Message.obtain(null, TaxiLujoService.MSJ_SEND_SOCKET, data);
                    replyTo.send(message);

                    if (data.getString("cmd").equals("19")) {
                        if (data.getString("msj").equals("")) {
                            if (!data.has("nivel")) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(5000);
                                            notificacion();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }).start();
                            }
                        }
                    }

                } catch (RemoteException error) {
                    log( "didReceiveData: No es posible enviar el mensaje al servicio");
                    error.printStackTrace();
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void didDisconnect(Exception error) {
            log( "didDisconnect: " + error.toString());
            MainActivity.v_desconexion = true;
            if(socketThread!=null)
                socketThread.interrupt();
            RETRY_CONNECTION = "si";

            if (RETRY_COUNT >= 5) {
                RETRY_COUNT = 0;

                try {
                    Message message = Message.obtain(null, TaxiLujoService.MSJ_SOCKET_ERROR);
                    replyTo.send(message);

                    reConnectSocket();
                } catch (RemoteException e) {
                   log("didDisconnect: No es posible enviar el mensaje al servicio");
                    error.printStackTrace();
                } catch (NullPointerException e) {
                    log( "didDisconnect: No es posible enviar ");
                    error.printStackTrace();
                }
            } else {
                RETRY_COUNT ++;
                reConnectSocket();
            }
        }

        @Override
        public void didConnect() {
            MainActivity.v_desconexion = false;
            log( "didConnect: La conexión con el socket fue satisfactoria");
            try {
                PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);

                JSONObject jsonObject            = new JSONObject();
                jsonObject.put("cmd", "19");
                jsonObject.put("imei",     params.get("imei"));
                jsonObject.put("tipo", "driver");
                jsonObject.put("iniciado", RETRY_CONNECTION);
                jsonObject.put("veq_id",   Inicio_sesion.v_vehiculo);
                jsonObject.put("usuario",  params.get("usuario").toString());
                jsonObject.put("clave",    params.get("clave").toString());
                jsonObject.put("nick",     params.get("nick").toString());
                jsonObject.put("lat",      String.valueOf(MainActivity.v_latitud));
                jsonObject.put("lng",      String.valueOf(MainActivity.v_longitud));
                jsonObject.put("version",  pinfo.versionName);

                socketThread.sendDataToSocket(jsonObject.toString());
            } catch (JSONException error) {
               log( "didConnect: No es posible crear el objeto de Login en el socket");
               error.printStackTrace();
            }catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    };

    private static void log(String s) {
        Log.d(TaxiLujoService.class.getSimpleName(), "######" + s + "######");
    }

}
