package com.example.trafficstats;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Environment;
import android.util.Log;

public class FileIO {

	private final static String FILENAME = "conTimes.txt";
	
	public FileIO(){
		
	}
	
	/**
	 *  write to sd-card
	 */
	public boolean writeToSDCard(String text){
		try{
			File root = Environment.getExternalStorageDirectory();
			File file = new File(root, FILENAME);
			BufferedWriter out = new BufferedWriter(new FileWriter(file,true));
			out.write(text);
		    out.close();
		    Log.i("traffic", text);
		    
		    return true;
			}
			catch (IOException e) {		
				Log.i("traffic", "Writing failed");
				e.printStackTrace();
				return false;
			}
		}

	
	/**
	 * read the last nrLines traffic events from sd-card 
	 */
	public TrafficEvent[] getTrafficEventsTail(int nrLines){
		StringBuffer[] bLines = new StringBuffer[nrLines];
		for(int i=0; i<nrLines;i++){
			bLines[i] = new StringBuffer();
		}
						
		try {
			
			File root = Environment.getExternalStorageDirectory();
			File file = new File(root, FILENAME);
			
			BufferedReader in = new BufferedReader(new FileReader(file));
			String s ="";
			int counter = 0;
			while ((s = in.readLine()) != null) {	
				bLines[counter].delete(0, bLines[counter].length());
				bLines[counter++].append(s);
				counter %= nrLines;
			}				
			in.close();
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		//parse Events
		TrafficEvent[] events = new TrafficEvent[nrLines];		
		for(int i=0; i<nrLines;i++){
			events[i] = TrafficEvent.parseEvent(bLines[i].toString());
		}
		
		return events;
	}
	
}
