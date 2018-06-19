package com.conductor.aeroturex.service.gps;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.conductor.aeroturex.MainActivity;
import com.conductor.aeroturex.old.OSMTracker;

/**
 * Handles the bind to the GPS Logger service
 * 
 * @author Nicolas Guillaumin
 *
 */
public class GPSLoggerServiceConnection implements ServiceConnection {

	/**
	 * Reference to TrackLogger activity
	 */
	private MainActivity activity;
	
	public GPSLoggerServiceConnection(MainActivity tl) {
		activity = tl;
	}
	
	@Override
	public void onServiceDisconnected(ComponentName name) {
		activity.setGpsLogger(null);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		
		activity.setGpsLogger( ((GPSLogger.GPSLoggerBinder) service).getService());

		// Update record status regarding of current tracking state
		/*GpsStatusRecord gpsStatusRecord = (GpsStatusRecord) activity.findViewById(R.id.gpsStatus);
		if (gpsStatusRecord != null) {
			gpsStatusRecord.manageRecordingIndicator(activity.getGpsLogger().isTracking());
		}*/
		
		// If not already tracking, start tracking
		if (!activity.getGpsLogger().isTracking()) {
			Intent intent = new Intent(OSMTracker.INTENT_START_TRACKING);
			//intent.putExtra(Schema.COL_TRACK_ID, activity.getCurrentTrackId());
			activity.sendBroadcast(intent);
		}
	}

}
