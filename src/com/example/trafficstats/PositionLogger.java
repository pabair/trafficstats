package com.example.trafficstats;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class PositionLogger extends Service {

	private LocationManager locationManager;
	private LocationListener locationListener;
	private GpxLogger gpxLog;
	
	private final IBinder mBinder = new LocalBinder();  
	  
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	
	
	
    public class LocalBinder extends Binder {
	   	
    	PositionLogger getService() {
            return PositionLogger.this;
        }
    }
    
    public void onCreate() {
	    super.onCreate();
	    
		gpxLog = new GpxLogger("gpxLog.gpx", System.currentTimeMillis());
		
		Log.i("location", "service created");
		
		 // Acquire a reference to the system Location Manager
		 locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		 locationListener = new LocationListener() {
			 public void onLocationChanged(Location location) {
			 // Called when a new location is found by the network location provider.
				 Log.i("location", location.getLatitude() + " " + location.getLongitude());
				 gpxLog.addTrackpoint(location.getLatitude(), location.getLongitude(), System.currentTimeMillis());
			 }
		
			 public void onStatusChanged(String provider, int status, Bundle extras) {}
			 public void onProviderEnabled(String provider) {}
			 public void onProviderDisabled(String provider) {}
		 };
		 locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);	  
	}
    
    
    
    public void onDestroy(){
    	locationManager.removeUpdates(locationListener);
		gpxLog.writeToFile();
		gpxLog.close();
    }

	
}
