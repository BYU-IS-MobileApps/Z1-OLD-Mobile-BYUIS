package BYUIS.app.grade_checker;

import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

/*
 * Android service for updating the grades files.
 */
public class GradesUpdaterService extends Service
{
	static final String TAG = "GradesUpdaterService";
	public static final String GRADES_DWNLDED_BRDCST = "BYUIS.app.grade_checker.grades_downloaded"; 
	
	//private BYUISApplication app;
	private GradesUpdater gradesUpdater;  
	
	/*
	 * Overrides the onCreate method from the Service class
	 */
	public void onCreate()
	{
		super.onCreate();
		Log.d(TAG, "onCreate");
	}
	
	/*
	 * Overrides the onStartCommand method from Service class
	 */	
	public int onStartCommand(Intent intent, int flags, int startID)
	{
		super.onStartCommand(intent, flags, startID);
		
		gradesUpdater = new GradesUpdater(this, new GradesDownloadedBroadcaster());
		gradesUpdater.start();
		
		Log.d(TAG, "onStartCommand");
		
		return START_STICKY;
	}
	
	/*
	 * Overrides the onDestroy method from the Service class
	 */
	public void onDestroy()
	{
		super.onDestroy();
		
		Log.d(TAG, "onDestroy");
	}
	
	
	/**
	 * Callback function for 
	 */
	public class GradesDownloadedBroadcaster
	{
		public void broadcast()
		{
			System.out.println("Broadcast sent!");
			sendBroadcast(new Intent("BYUIS.app.grade_checker.grades_downloaded"));
			//sendBroadcast(new Intent(GradesUpdaterService.this, TestReciever.class));
		}
	}
	
	
	@Override
	public IBinder onBind(Intent intent) 
	{
		return null;
	}
}
