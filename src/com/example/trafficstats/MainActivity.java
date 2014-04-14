package com.example.trafficstats;

import java.util.List;
import java.util.PriorityQueue;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;



public class MainActivity extends Activity implements OnClickListener, OnCheckedChangeListener  {
	
	private Button getTraffic;
	private Button stopTraffic;
	private WebView webview;
	private CheckBox gpsBox;
	
	private boolean gpsEnable = false;

	
	private FileIO file = new FileIO();
	private static final int HISTORY_SIZE = 10;
	
	PriorityQueue<TrafficEvent> eventHistory = new PriorityQueue<TrafficEvent>(HISTORY_SIZE);

    protected void onDestroy() {
    	if(stopTraffic.isEnabled())
    		unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getTraffic = (Button) this.findViewById(R.id.button1);
        getTraffic.setOnClickListener(this);
        
        stopTraffic = (Button) this.findViewById(R.id.button2);
        stopTraffic.setOnClickListener(this);
        
        webview = (WebView) this.findViewById(R.id.webView1);
        
        gpsBox = (CheckBox) this.findViewById(R.id.checkBox1);
        gpsBox.setOnCheckedChangeListener(this);
        
        
        if(isServiceRunning("com.example.trafficstats.LoggerService")){
        	getTraffic.setEnabled(false);
        	registerReceiver(broadcastReceiver, new IntentFilter(LoggerService.BROADCAST_ACTION));
        }
        else
        	stopTraffic.setEnabled(false);
        
        if(isServiceRunning("com.example.trafficstats.PositionLogger")){
        	gpsBox.setChecked(true);
        }
        else
        	gpsBox.setChecked(false);
        
        updateEventHistory(null);

    }
        
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    /**
     * receive new TrafficEvent from LoggerSerivce
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
        	updateEventHistory((TrafficEvent) intent.getSerializableExtra("TrafficEvent")); 
        }
    };
    
    /**
     *  updates the event history and writes to GUI
     */
    public void updateEventHistory(TrafficEvent e){
    	if(e != null){
    		eventHistory.offer(e);
    	}
    	else{	// load last traffic events from file on sd-card
    		TrafficEvent[] events = file.getTrafficEventsTail(HISTORY_SIZE);
    		if(events==null){
    			webview.loadData("Cannot read from SD-Card", "text/html", null);
    			return;
    		}
    		
    		for(TrafficEvent event : events){
    			if(event != null)
    				eventHistory.offer(event);
    		}
    	}
    	
    	PriorityQueue<TrafficEvent> saveQueue = new PriorityQueue<TrafficEvent>(HISTORY_SIZE);
    	
    	StringBuffer data = new StringBuffer();
    	data.append("<ul>");
    	for(int i=0; i < HISTORY_SIZE; i++){
    		TrafficEvent event = eventHistory.poll();
    		
    		 if(event == null)
    			 continue;
    		 
    		 saveQueue.offer(event);
    		 String newEvent =  event.getTimeStamp() + " " + event.getDiff();
    		 data.append("<li>" + newEvent + "</li>");
    	}
    	data.append("</ul>");
    	webview.loadData(data.toString(), "text/html", null);
    	
    	
    	eventHistory.addAll(saveQueue);
    }
    
    
	public void onClick(View v) {
		if(getTraffic.getId() == ((Button)v).getId() ){
			startService(new Intent(this, LoggerService.class));
			registerReceiver(broadcastReceiver, new IntentFilter(LoggerService.BROADCAST_ACTION));
			
			getTraffic.setEnabled(false);
			stopTraffic.setEnabled(true);
		}
		else if(stopTraffic.getId() == ((Button)v).getId() ){
			unregisterReceiver(broadcastReceiver);
			stopService(new Intent(this, LoggerService.class));
			
			getTraffic.setEnabled(true);
			stopTraffic.setEnabled(false);
		}
	}
	
	/**
	 * check if a given service is running
	 */
	public boolean isServiceRunning(String serviceClassName){
        final ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        final List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClassName)){
            	
                return true;
            }
        }
        return false;
     }

	
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		if(!gpsEnable){	//enable
			startService(new Intent(this, PositionLogger.class));
			gpsEnable = true;
		}
		else{
			gpsEnable = false;
			stopService(new Intent(this, PositionLogger.class));
		}
	}
		
}
