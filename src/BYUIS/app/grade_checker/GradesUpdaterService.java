package BYUIS.app.grade_checker;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

/*
 * Android service for updating the grades files.
 */
public class GradesUpdaterService extends Service
{
	static final String TAG = "GradesUpdaterService";
	
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
		
		gradesUpdater = new GradesUpdater();
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
	
	@Override
	public IBinder onBind(Intent intent) 
	{
		return null;
	}

	/**
	 * Thread for connecting to BrainHoney and updating the database
	 */
	private class GradesUpdater extends Thread
	{	
		private static final String TAG = "UpdateGradesDatabase";
		private String username;
		private String password;
		private String userID;
		private ArrayList<ContentValues> grades;
		
		
		/**
		 * Constructor
		 */
		public GradesUpdater()
		{
			/*SharedPreferences sharedPrefs = getSharedPreferences("PrefFile", MODE_WORLD_READABLE);
			this.userID = (String) sharedPrefs.getString("userID", "-1"); */
			
			UserSettingsAdapter userSettings = ((BYUISApplication) getApplication()).getUserSettings();
			/*this.username = userSettings.getUsername();
			this.userID = userSettings.getUserId();
			this.password = ((BYUISApplication) getApplication()).getSessionPassword();*/
			
			grades = new ArrayList<ContentValues>();
			
			Log.d(TAG, "Username is: " + this.username);
			Log.d(TAG, "Password is: " + this.password);
			Log.d(TAG, "User ID is: " + this.userID);
		}		
		
		/*
		 * Downloads the grades XML from BrainHoney
		 */
		private String downloadGradesXML(String userID)
		{
			Log.d(TAG, "downloading XML.");
			
			BrainHoneyAccess brainHoney = new BrainHoneyAccess();
			String gradesXML = "";
			try 
			{
				brainHoney.login(username, password);
				gradesXML = brainHoney.getUserGradebook(userID);
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			} 
			
			System.out.println(gradesXML);
			
			return gradesXML;
		}
		
		private void storeParsedData(Node enrollmentIDAttribute, Node titleAttribute, Node gradeAttribute)
		{
			ContentValues gradesEntry = new ContentValues();
			gradesEntry.put(GradeBookDbHelper.C_ENROLLMENT_ID, enrollmentIDAttribute.getNodeValue());
			gradesEntry.put(GradeBookDbHelper.C_COURSE_TITLE, trimTitle(titleAttribute.getNodeValue()));
			gradesEntry.put(GradeBookDbHelper.C_COURSE_GRADE, gradeAttribute.getNodeValue()+"%");		
			
			grades.add(gradesEntry);
		}
		
		/*
		 * Parses the grade information
		 */
		private void parseGradeInformation(String gradeXML) throws ParserConfigurationException, SAXException, IOException
		{	
			System.out.println("Parsing the string: " + gradeXML);
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
			org.w3c.dom.Document doc = documentBuilder.parse(new ByteArrayInputStream(gradeXML.getBytes()));
			
			// find all the course names
			NodeList enrollments = doc.getElementsByTagName("enrollment");
			Node enrollmentIDAttribute;
			Node titleAttribute = null; 
			Node gradeAttribute = null;
			
			for(int i = 0; i < enrollments.getLength(); i++)
			{
				NamedNodeMap attributes = enrollments.item(i).getAttributes();
				enrollmentIDAttribute = attributes.getNamedItem("id");
				Log.d(TAG, enrollmentIDAttribute.getNodeValue());
				
				NodeList enrollmentChildren = enrollments.item(i).getChildNodes();
				
				for(int j = 0; j < enrollmentChildren.getLength(); j++)
				{
					if(enrollmentChildren.item(j).getNodeName().equals("entity"))
					{
						titleAttribute = enrollmentChildren.item(j).getAttributes().getNamedItem("title");
						Log.d(TAG, titleAttribute.getNodeValue());
					}
					
					if(enrollmentChildren.item(j).getNodeName().equals("grades"))
					{
						gradeAttribute = enrollmentChildren.item(j).getAttributes().getNamedItem("achieved");
						Log.d(TAG, gradeAttribute.getNodeValue());
					}
				}
				
				storeParsedData(gradeAttribute, titleAttribute, gradeAttribute);
			}		
		}
		
		/*
		 * Updates the database.
		 */
		private void updateDatabase()
		{
			GradeBookDbAdapter db = new GradeBookDbAdapter(GradesUpdaterService.this);
			db.open();
			db.clearAllGrades();
			db.insertGrades(grades);
			db.close();
		}
		
		/*
		 * Utility function for trimming the title string
		 */
		private String trimTitle(String titleString)
		{
			return titleString.replaceFirst(".*?: ", "");
		}
		
		/*
		 * Downloads the grades XML, parses it, and stores it in the database
		 */
		public void run()
		{
			Log.d(TAG, "Updating database");
			try 
			{
				parseGradeInformation(downloadGradesXML(userID));
				updateDatabase();
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			} 
		}
	}
}
