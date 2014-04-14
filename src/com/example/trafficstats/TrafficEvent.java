package com.example.trafficstats;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class TrafficEvent implements Serializable, Comparable<TrafficEvent>{

	private static final long serialVersionUID = 1303198670617351926L;

	private Timestamp timeStamp;
	private long realtiveSystemTime;
	private String diff;

	
	public String toString(){
		return timeStamp + ";" + diff;
	}

	@Override
	public int compareTo(TrafficEvent another) {
		if(another.timeStamp.after(timeStamp))
			return 1;
		return -1;
	}
	
	public Timestamp getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Timestamp timeStamp) {
		this.timeStamp = timeStamp;
	}

	public long getRealtiveSystemTime() {
		return realtiveSystemTime;
	}

	public void setRealtiveSystemTime(long realtiveSystemTime) {
		this.realtiveSystemTime = realtiveSystemTime;
	}

	public String getDiff() {
		return diff;
	}

	public void setDiff(String diff) {
		this.diff = diff;
	}

	public static TrafficEvent parseEvent(String string) {
		if(string.equals(""))
			return null;
		
		TrafficEvent event = new TrafficEvent();	
		String[] parts = string.split(";");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		java.util.Date parsedDate;
		try {
			parsedDate = dateFormat.parse(parts[0]);
			event.setTimeStamp(new java.sql.Timestamp(parsedDate.getTime()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
						
		event.setDiff(parts[1]);
				
		return event;
	}
}
