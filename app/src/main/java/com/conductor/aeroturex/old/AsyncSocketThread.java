package com.conductor.aeroturex.old;

import android.util.Log;

import com.conductor.aeroturex.Inicio_sesion;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.StringTokenizer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class AsyncSocketThread extends Thread {

    private AsyncSocketHandler mSocketListener;

    private Boolean socketRun = false;
    private PrintWriter socketOut;
    private BufferedReader socketIn;

    private ScheduledExecutorService scheduledExecutorService;

    public AsyncSocketThread(AsyncSocketHandler socketHandler) {
        this.mSocketListener = socketHandler;
    }

    public void sendDataToSocket(final String dataToSocket) {
        if (socketOut != null && !socketOut.checkError()) {
            //final String dataToSocket = jsonData.toString();
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    if(dataToSocket !=null) {
                        log("ENVIA: " + dataToSocket);
                        socketOut.println(dataToSocket);
                        socketOut.flush();
                    }
                }
            });
        }
    }

    public void closeSocketConnection() {
        socketRun = false;

        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }
    }

   /* private void sendKeepAlive() {

    }*/

    @Override
    public void run() {
        socketRun = true;

        try {
            Socket socket = new Socket(Inicio_sesion.v_ip, Integer.parseInt(Inicio_sesion.v_puerto));
            try {
                socketOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                mSocketListener.didConnect();

                /*scheduledExecutorService = Executors.newScheduledThreadPool(1);
                scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        sendKeepAlive();
                    }
                }, 10, 35, TimeUnit.SECONDS);*/

                while (socketRun) {
                    if (socket.isConnected()) {
                        String readLine = socketIn.readLine();
                        //log("RECIBE: " + readLine);
                        if (readLine != null) {
                            StringTokenizer tokenizer = new StringTokenizer(readLine, System.lineSeparator());
                            while (tokenizer.hasMoreElements()) {
                                try {
                                    mSocketListener.didReceiveData(new JSONObject((String) tokenizer.nextElement()));
                                } catch (JSONException e) {
                                   log( "run: No es posible convertir lo que llega en JSON");
                                   e.printStackTrace();
                                }
                            }
                        } else {
                            socketRun = false;
                            mSocketListener.didDisconnect(new SocketException("Socket is not connected"));
                        }
                    }
                }
            } catch (IOException e) {
                mSocketListener.didDisconnect(e);
            } finally {
                log("run: Cerrando la conexión con al socket");
                if (scheduledExecutorService != null) {
                    scheduledExecutorService.shutdown();
                    scheduledExecutorService = null;
                }
                if (socketOut != null) {
                    socketOut.close();
                    socketOut = null;
                }
                if (socketIn != null) {
                    socketIn.close();
                    socketIn = null;
                }
                socket.close();
            }
        } catch (IOException e) {
            mSocketListener.didDisconnect(e);
        }
    }

    private static void log(String s) {
        Log.d(AsyncSocketThread.class.getSimpleName(), "######" + s + "######");
    }
}