package com.example.trafficstats;

import java.sql.Timestamp;
import java.util.Date;
import java.util.PriorityQueue;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class LoggerService extends Service {
	
	private static long TIMER_DELTA = 10;
	
	private Timer updateTimer;
	private Timer writeTimer;
		
	long lastTransmission = 0;
	long packetsReceived = 0;
	long packetsSent = 0;
	long lastTransimission = 0 ;
	
	boolean wifiConnected = false;
	
	private static final double FLOWTIME = 0;
	private StringBuffer buffer = new StringBuffer();
	
	public static final String BROADCAST_ACTION = "com.example.trafficstats.broadcastTrafficEvent";
	
	private PriorityQueue<TrafficEvent> eventQueue = new PriorityQueue<TrafficEvent>(5);
	
	private FileIO file = new FileIO();
	
	
    public class LocalBinder extends Binder {
    	   	
    	LoggerService getService() {
            return LoggerService.this;
        }
    }
        
    public String getEvents(){
    	StringBuffer buff = new StringBuffer();
    	for(TrafficEvent e: eventQueue){
    		buff.append(e.toString() + "\n");
    	}
    	return buff.toString();
    }

    private TimerTask updateTask = new TimerTask() {
	
		public void run() {
			long currentPacketsRecv = TrafficStats.getMobileRxBytes();
			long currentPacketsSent = TrafficStats.getMobileTxBytes();
			if(currentPacketsRecv != packetsReceived || currentPacketsSent!= packetsSent){
				packetsReceived = currentPacketsRecv;
				packetsSent = currentPacketsSent;
				double diff = (SystemClock.elapsedRealtime()-lastTransimission);
				double secs = diff /1000;
				if(secs >= FLOWTIME){
					Log.i("traffic", "Diff is " + secs);
					
			        Timestamp timestamp = new Timestamp(new Date().getTime());
//			        long rounded = Math.round(secs);
			        String output = timestamp.toString() + ";" + secs;
					buffer.append(output + "\r\n");
					
					TrafficEvent event = new TrafficEvent();
					event.setTimeStamp(timestamp);
					event.setDiff(""+secs);
					event.setRealtiveSystemTime(SystemClock.elapsedRealtime());
					eventQueue.add(event);
					
					//broadcast event
					Intent intent = new Intent(BROADCAST_ACTION);
					intent.putExtra("TrafficEvent", event);
					sendBroadcast(intent);
				}
				lastTransimission = SystemClock.elapsedRealtime();
			}
	    }
	};
	
	private TimerTask writerTask = new TimerTask() {
		
		public void run() {
			if(buffer.length() > 0){
				boolean success = file.writeToSDCard(buffer.toString());
				if(success)
					buffer.delete(0, buffer.length());	
			}
	    }
	};
	
	
  public void onCreate() {
	    super.onCreate();
	    
	    packetsReceived = TrafficStats.getMobileRxBytes();
	    packetsSent = TrafficStats.getMobileTxBytes();
	    lastTransimission = SystemClock.elapsedRealtime();
	    
	    updateTimer = new Timer("TrafficTimer");
	    updateTimer.schedule(updateTask, 100, TIMER_DELTA);
	    
	    writeTimer = new Timer("WriteTimer");
	    writeTimer.schedule(writerTask, 10000, 10000);
	    
	    buffer.append(new Timestamp(new Date().getTime()).toString() + ";service started" + "\r\n");
	    
	    this.registerReceiver(this.myWifiReceiver,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	    wifiConnected = ((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
	}

	private final IBinder mBinder = new LocalBinder();  
	  
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	
	
	public void onDestroy() {
	  super.onDestroy();
	  buffer.append(new Timestamp(new Date().getTime()).toString() + ";service stopped" + "\r\n");
	  file.writeToSDCard(buffer.toString());
	  buffer.delete(0, buffer.length());
    	
	  updateTimer.cancel();
	  writeTimer.cancel();
	  }
	
	
	public void onLowMemory(){
		super.onLowMemory();
		buffer.append(new Timestamp(new Date().getTime()).toString() + ";lowMemory" + "\r\n");
		file.writeToSDCard(buffer.toString());
    	buffer.delete(0, buffer.length());
	}

	
	/**
	 * Disable logging when Wifi is on
	 */
	private BroadcastReceiver myWifiReceiver = new BroadcastReceiver(){

	  public void onReceive(Context arg0, Intent arg1) {

	   NetworkInfo networkInfo = (NetworkInfo) arg1.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
	   if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
		   boolean newConnectionState = 
				   ((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
		   
		   if(wifiConnected && !newConnectionState){
			   lastTransimission = SystemClock.elapsedRealtime();
			   wifiConnected = false;
		   }
		   else if(!wifiConnected && newConnectionState){
			   wifiConnected = true;
		   }
	   }
	  }};
	

}
