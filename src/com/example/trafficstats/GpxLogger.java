package com.example.trafficstats;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;

import org.xmlpull.v1.XmlSerializer;

import android.util.Log;
import android.util.Xml;

public class GpxLogger extends FileLogger {
	private SimpleDateFormat mFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	private XmlSerializer mSerializer;
	private StringWriter mWriter;
	
	public GpxLogger(String logfile, long time) {
		super(logfile);
		
        mSerializer = Xml.newSerializer();
        mWriter = new StringWriter(); 
               
        try {
        	mSerializer.setOutput(mWriter);
        	mSerializer.startDocument("UTF-8", true);
        	mSerializer.startTag("", "gpx");
        	mSerializer.attribute("", "version", "1.1");
        	mSerializer.attribute("", "creator", "GPSBabel - http://www.gpsbabel.org");
        	mSerializer.attribute("", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        	mSerializer.attribute("", "xmlns", "http://www.topografix.com/GPX/1/1");
        	mSerializer.attribute("", "xsi:schemaLocation", "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd");
        	mSerializer.startTag("", "metadata");
        	mSerializer.startTag("", "time");
        	mSerializer.text(mFormatter.format(time));
        	mSerializer.endTag("", "time");
        	mSerializer.endTag("", "metadata");
        	mSerializer.startTag("", "trk");
        	mSerializer.startTag("", "trkseg");
        } catch (Exception e) {
        	e.printStackTrace();
        }
		logData(mWriter.toString());
		Log.i("FAPRA", mWriter.toString());
	}	
		
	public void addTrackpoint(double latitude, double longitude, long timestamp) {
		try {
			mSerializer.startTag("", "trkpt");
			mSerializer.attribute("", "lat", Double.toString(latitude));
			mSerializer.attribute("", "lon", Double.toString(longitude));
			mSerializer.startTag("", "time");
			mSerializer.text(mFormatter.format(timestamp));
			mSerializer.endTag("", "time");
			mSerializer.endTag("", "trkpt");
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public void writeToFile() {
		try {			
			mSerializer.endTag("", "trkseg");
			mSerializer.endTag("", "trk");
			mSerializer.endTag("", "gpx");
			mSerializer.endDocument();
		    logData(mWriter.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}
