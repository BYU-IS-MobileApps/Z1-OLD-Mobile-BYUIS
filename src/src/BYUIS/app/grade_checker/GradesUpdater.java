package BYUIS.app.grade_checker;

import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

/**
 * Thread for connecting to BrainHoney and updating the database
 */
public class GradesUpdater extends Thread
{	
	private Context context;
	
	private static final String TAG = "UpdateGradesDatabase";
	private String username;
	private String password;
	private ArrayList<ContentValues> grades = new ArrayList<ContentValues>();
	private GradesUpdaterService.GradesDownloadedBroadcaster broadcaster;
	
	/**
	 * Private constructor prevents instantiation
	 */
	public GradesUpdater(Context context, GradesUpdaterService.GradesDownloadedBroadcaster broadcaster)
	{
		this.context = context;
		this.username = UserSettingsAdapter.getInstance(context).getUsername();
		this.password = Login.getInstance(context).getSessionPassword();
		this.broadcaster = broadcaster;
	}
	
	/**
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
	
	private void parseGradeInformation(Document doc) throws ParserConfigurationException
	{
		// find all the course names
		NodeList enrollments = doc.getElementsByTagName("enrollment");
		Node enrollmentIDAttribute;
		Node titleAttribute = null; 
		Node gradeAttribute = null;
		
		System.out.println("Looping through");
		
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
		GradeBookDbAdapter db = new GradeBookDbAdapter(context);
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
			BrainHoneyAcessImproved brainHoney = new BrainHoneyAcessImproved();
			brainHoney.login(username, password);
			parseGradeInformation(brainHoney.getUserGradebook());
			
			updateDatabase();
			broadcaster.broadcast();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
	}
}