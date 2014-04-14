package com.example.trafficstats;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Environment;
import android.util.Log;

/**
 * FileLogger
 * 
 * Instantiate this class with a filehandle and use it to write
 * lines to the file. Line format is:
 * Timestamp: [TYPE] Message
 * 
 */
public class FileLogger {
	
	private File mFile;
	private FileOutputStream mOutputStream;
	
	/**
	 * FileLogger
	 * 
	 * Creates file if not exists and opens filestream
	 * 
	 * @param logfile string
	 */
	public FileLogger(String logfile) {
		mFile = new File("/sdcard/" +  logfile);
		//mFile = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/" + logfile);
		try {
			if (!mFile.exists()) {
				mFile.createNewFile();
			}
			mOutputStream = new FileOutputStream(mFile,false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * FileLogger
	 * 
	 * Creates file if not exists and opens filestream
	 * 
	 * @param logfile filehandle
	 */
	public FileLogger(File logfile) {
		mFile = logfile;
		try {
			mFile.createNewFile();
			mOutputStream = new FileOutputStream(mFile);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * canWrite
	 * 
	 * @return whether application has write permission on this file
	 */
	public boolean canWrite() {
		return mFile.canWrite();
	}
	
	/**
	 * close
	 * 
	 * Closes filestream
	 */
	public void close() {
		if (mOutputStream != null) {
			try {
				mOutputStream.flush();
				mOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * logData
	 * 
	 * writes one unformated line of data into file
	 * 
	 * @param data line to write
	 */
	public void logData(String data) {
		if (mOutputStream != null && !data.equals("")) {
			try {
				mOutputStream.write((data + "\n").getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * logLine
	 * 
	 * writes one line into file
	 * 
	 * @param type tag of linetype
	 * @param line message to write
	 */
	public void logLine(String type, String line) {
		if (mOutputStream != null) {
			try {
				mOutputStream.write((Long.toString(System.currentTimeMillis()) + ": " + "[" + type + "]" + line + "\n").getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
