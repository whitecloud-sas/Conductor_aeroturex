package com.conductor.aeroturex.service.gps;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.conductor.aeroturex.Inicio_sesion;
import com.conductor.aeroturex.MainActivity;
import com.conductor.aeroturex.R;
import com.conductor.aeroturex.db.DataHelper;
import com.conductor.aeroturex.db.TrackContentProvider.Schema;
import com.conductor.aeroturex.listener.SensorListener;
import com.conductor.aeroturex.old.Constants;
import com.conductor.aeroturex.old.OSMTracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import org.json.JSONException;
import org.json.JSONObject;

import static com.conductor.aeroturex.MainActivity.v_fechaGPS;
import static com.conductor.aeroturex.MainActivity.v_latitud;
import static com.conductor.aeroturex.MainActivity.v_longitud;

/**
 * GPS logging service.
 *
 * @author Nicolas Guillaumin
 */
public class GPSLogger extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        android.location.LocationListener,
        LocationListener {

    Location mLastLocation;

    private GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Context mContext = null;
    private long lastUpdateTime;
    protected final long period=7*1000;
    boolean mocklocation = false;


    /**
     * Data helper.
     */
    private DataHelper dataHelper;

    /**
     * Are we currently tracking ?
     */
    private boolean isTracking = false;

    /**
     * System notification id.
     */
    private static final int NOTIFICATION_ID = 1;

    /**
     * Last known location
     */
    private Location lastLocation;

    /**
     * Last number of satellites used in fix.
     */
    private int lastNbSatellites;



    /**
     * Current Track ID
     */
    private long currentTrackId = -1;



    /**
     * the interval (in ms) to log GPS fixes defined in the preferences
     */
    long gpsLoggingInterval;

    /**
     * sensors for magnetic orientation
     */
    private SensorListener sensorListener = new SensorListener();

    Callbacks activity;
    LocationManager lmgr;

    /**
     * Receives Intent for way point tracking, and stop/start logging.
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            log("Received intent " + intent.getAction());
            if (OSMTracker.INTENT_TRACK_WP.equals(intent.getAction())) {
                // Track a way point
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    // because of the gps logging interval our last fix could be very old
                    // so we'll request the last known location from the gps provider
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    //lastLocation = lmgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (lastLocation != null) {
                        Long trackId = extras.getLong(Schema.COL_TRACK_ID);
                        String uuid = extras.getString(OSMTracker.INTENT_KEY_UUID);
                        String name = extras.getString(OSMTracker.INTENT_KEY_NAME);
                        String link = extras.getString(OSMTracker.INTENT_KEY_LINK);

                        dataHelper.wayPoint(trackId, lastLocation, lastNbSatellites, name, link, uuid, sensorListener.getAzimuth(), sensorListener.getAccuracy());
                    }
                }
            } else if (OSMTracker.INTENT_UPDATE_WP.equals(intent.getAction())) {
                // Update an existing waypoint
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    Long trackId = extras.getLong(Schema.COL_TRACK_ID);
                    String uuid = extras.getString(OSMTracker.INTENT_KEY_UUID);
                    String name = extras.getString(OSMTracker.INTENT_KEY_NAME);
                    String link = extras.getString(OSMTracker.INTENT_KEY_LINK);
                    dataHelper.updateWayPoint(trackId, uuid, name, link);
                }
            } else if (OSMTracker.INTENT_DELETE_WP.equals(intent.getAction())) {
                // Delete an existing waypoint
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    String uuid = extras.getString(OSMTracker.INTENT_KEY_UUID);
                    dataHelper.deleteWayPoint(uuid);
                }
            } else if (OSMTracker.INTENT_START_TRACKING.equals(intent.getAction())) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    Long trackId = extras.getLong(Schema.COL_TRACK_ID);
                    startTracking(trackId);
                }
            } else if (OSMTracker.INTENT_STOP_TRACKING.equals(intent.getAction())) {
                stopTrackingAndSave();
            }
        }
    };

    /**
     * Binder for service interaction
     */
    private final IBinder binder = new GPSLoggerBinder();

    @Override
    public IBinder onBind(Intent intent) {
        log("Service onBind()");

        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        log("Service onUnbind()");
        // If we aren't currently tracking we can
        // stop ourselves
        if (!isTracking) {
            log("Service self-stopping");
            stopSelf();
        }

        // We don't want onRebind() to be called, so return false.
        return false;
    }

    /**
     * Bind interface for service interaction
     */
    public class GPSLoggerBinder extends Binder {

        /**
         * Called by the activity when binding.
         * Returns itself.
         *
         * @return the GPS Logger service
         */
        public GPSLogger getService() {
            return GPSLogger.this;
        }
    }

    @Override
    public void onCreate() {
        log("Service onCreate()");
        dataHelper = new DataHelper(this);

        //read the logging interval from preferences
        gpsLoggingInterval = Long.parseLong(PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString(
                OSMTracker.Preferences.KEY_GPS_LOGGING_INTERVAL, OSMTracker.Preferences.VAL_GPS_LOGGING_INTERVAL)) * 1000;

        log("gpsLoggingInterval " + gpsLoggingInterval);
        // Register our broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(OSMTracker.INTENT_TRACK_WP);
        filter.addAction(OSMTracker.INTENT_UPDATE_WP);
        filter.addAction(OSMTracker.INTENT_DELETE_WP);
        filter.addAction(OSMTracker.INTENT_START_TRACKING);
        filter.addAction(OSMTracker.INTENT_STOP_TRACKING);
        registerReceiver(receiver, filter);

        // Register ourselves for location updates
        lmgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lmgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, gpsLoggingInterval, 0, this);
        //}




        mContext = this;
        //register for Orientation updates
        sensorListener.register(this);
        buildGoogleApiClient();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("Service onStartCommand(-," + flags + "," + startId + ")");
        startForeground(NOTIFICATION_ID, getNotification());
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        log("Service onDestroy()");
        if (isTracking) {
            // If we're currently tracking, save user data.
            stopTrackingAndSave();
        }

        // Unregister listener
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //lmgr.removeUpdates(this);

        // Unregister broadcast receiver
        unregisterReceiver(receiver);

        // Cancel any existing notification
        stopNotifyBackgroundService();

        // stop sensors
        sensorListener.unregister();

        super.onDestroy();
    }

    /**
     * Start GPS tracking.
     */
    private void startTracking(long trackId) {
        currentTrackId = trackId;
        log("Starting track logging for track #" + trackId);
        // Refresh notification with correct Track ID
        NotificationManager nmgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nmgr.notify(NOTIFICATION_ID, getNotification());
        isTracking = true;
    }

    /**
     * Stops GPS Logging
     */
    private void stopTrackingAndSave() {
        isTracking = false;
        dataHelper.stopTracking(currentTrackId);
        currentTrackId = -1;
        this.stopSelf();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        log("GPS onConnected");
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(7000); // Update location every ten seconds
        //mLocationRequest.setSmallestDisplacement(20);//cada 20 metros

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        com.google.android.gms.location.LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        mLastLocation = com.google.android.gms.location.LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            v_latitud = mLastLocation.getLatitude();
            v_longitud = mLastLocation.getLongitude();

            activity.setlmg(mLastLocation);

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
    public void onConnectionSuspended(int i) {
        log("GPS onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        log("GPS onConnectionFailed");
        log(connectionResult.toString());
        buildGoogleApiClient();
    }

    @Override
    public void onLocationChanged(Location location) {

        if (location != null && location.getTime() - lastUpdateTime >= period) {
            lastUpdateTime = location.getTime();

            if(!mocklocation) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    if (location.isFromMockProvider()) {
                        mocklocation = true;
                        activity.updateClient("2");
                        try {
                            JSONObject jObj = new JSONObject();
                            jObj.put("cmd", "B3");
                            jObj.put("lat", v_latitud + "");
                            jObj.put("lng", v_longitud + "");
                            jObj.put("date_gps", v_fechaGPS + "");
                            Inicio_sesion.datasource.create(jObj.toString(),"B3");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            MainActivity.onLocationChanged3(location);
            lastLocation = location;
            lastNbSatellites = countSatellites();
        }

    }


    /**
     * Counts number of satellites used in last fix.
     *
     * @return The number of satellites
     */
    private int countSatellites() {
        int count = 0;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return count;
        }
        /*GpsStatus status = lmgr.getGpsStatus(null);
        for (GpsSatellite sat : status.getSatellites()) {
            if (sat.usedInFix()) {
                count++;
            }
        }*/

        return count;
    }

    /**
     * Builds the notification to display when tracking in background.
     */
    private Notification getNotification() {
/*
        Notification n = new Notification(R.mipmap.ic_launcher,
                getResources().getString(R.string.notification_ticker_text),
                System.currentTimeMillis());

        Intent startTrackLogger = new Intent(this, MainActivity.class);
        startTrackLogger.putExtra(TrackContentProvider.Schema.COL_TRACK_ID, currentTrackId);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, startTrackLogger, PendingIntent.FLAG_UPDATE_CURRENT);
        n.flags = Notification.FLAG_FOREGROUND_SERVICE | Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;




        n.setLatestEventInfo(
                getApplicationContext(),
                getResources().getString(R.string.notification_title).replace("{0}", (currentTrackId > -1) ? Long.toString(currentTrackId) : "?"),
                getResources().getString(R.string.notification_text),
                contentIntent);
		*/

        Intent notificationIntent = new Intent(this, MainActivity.class);
        // notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        // notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
        // Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification n = new NotificationCompat.Builder(this)
                .setContentTitle(getText(R.string.app_name))
                .setTicker(getText(R.string.app_name))
                .setContentText("Iniciado")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();

        n.flags |= Notification.FLAG_NO_CLEAR;

        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, n);

        return n;
    }

    /**
     * Stops notifying the user that we're tracking in the background
     */
    private void stopNotifyBackgroundService() {
        NotificationManager nmgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nmgr.cancel(NOTIFICATION_ID);
    }


    /**
     * Setter for isTracking
     *
     * @return true if we're currently tracking, otherwise false.
     */
    public boolean isTracking() {
        return isTracking;
    }


    //callbacks interface for communication with service clients!
    public interface Callbacks{
        void updateClient(String s);
        void setlmg(Location mLastLocation);
    }

    //Here Activity register to the service as Callbacks client
    public void registerClient(Activity activity){
        this.activity = (Callbacks)activity;
    }

    private static void log(String s) {
        Log.d(GPSLogger.class.getSimpleName(), "######" + s + "######");
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //log("onStatusChanged provider " + provider);
        //log("onStatusChanged status " + status);
    }

    @Override
    public void onProviderEnabled(String provider) {
        log("onProviderEnabled provider " + provider);

    }

    @Override
    public void onProviderDisabled(String provider) {
        log("onProviderDisabled provider " + provider);
        activity.updateClient("1");
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


}
