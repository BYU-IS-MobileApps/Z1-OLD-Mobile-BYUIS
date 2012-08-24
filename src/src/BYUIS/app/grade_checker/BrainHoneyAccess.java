package BYUIS.app.grade_checker;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;



/*
 * Adapter to the BrainHoney database.
 * 
 * TODO: Implement to work with JSON instead of straight XML.
 */
public class BrainHoneyAccess 
{
	private static final String BRAIN_HONEY_URL = "https://gls.agilix.com/dlap.ashx";
	private static final String BRAIN_HONEY_SUB_DOMAIN = "byuistest";
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String TEXT_XML_TYPE = "text/xml;";
	
	//TODO: Eliminate response and response entity variables.
	
	private HttpClient httpClient;		// Performs execution of internet calls
	private HttpResponse response;		// Container for the responses from the server
	private HttpEntity responseEntity;	// Container for the response entity from the server 
	
	/**
	 * Exception for when login fails due to invalid credentials.
	 */
	public class InvalidBrainHoneyLoginException extends Exception
	{
		public String toString()
		{
			return new String("Invalid username or password");
		}
	}
	
   /*
	* Prints out the response from BrainHoney.
	*/
	private void printResponseEntity() throws IllegalStateException, IOException
	{
		// Output the response
		Scanner reader = new Scanner(responseEntity.getContent());
		String responseString = "";
		while(reader.hasNext())
			responseString += reader.nextLine();
		
		System.out.println(responseString);		
	}
	
	/*
	 * Examines the response entity to see if the login was successful.
	 */
	private boolean successfulLogin(String responseString) throws IllegalStateException, IOException
	{		
		String validLoginCode = "OK";
		if(responseString.contains(validLoginCode))
			return true;
		else
			return false;
	}

	/*
	 * No-argument constructor.
	 */
	public BrainHoneyAccess()
	{
		this.httpClient = new DefaultHttpClient();
	}
	
	/*
	 * Logs into the BrainHoney database.
	 */
	public void login() throws ClientProtocolException, IOException
	{
		// Set login information
		HttpPost httpPost = new HttpPost(BRAIN_HONEY_URL);
		httpPost.setHeader(CONTENT_TYPE, TEXT_XML_TYPE);
		//StringEntity loginXML = new StringEntity("<request cmd='login' username='byuistest/dbuser' password='@dM1n'/>");
		StringEntity loginXML = new StringEntity("<request cmd='login' username='byuis/dbuser' password='@dM1n'/>");
		httpPost.setEntity(loginXML);
		
		response = httpClient.execute(httpPost);
		
		responseEntity = response.getEntity();
		
		printResponseEntity();
	}
	
	/**
	 * Logs user into BrainHoney.  
	 * 
	 * @param userName Username for login.
	 * @param password Password for login.
	 * 
	 * @return The user id if successfully logged in.
	 * 
	 * TODO: Re-implement using JSON.
	 */
	public String login(String userName, String password) throws ClientProtocolException, IOException,
																InvalidBrainHoneyLoginException, ParserConfigurationException, 
																IllegalStateException, SAXException
	{
		// Set login information
		HttpPost httpPost = new HttpPost(BRAIN_HONEY_URL);
		httpPost.setHeader(CONTENT_TYPE, TEXT_XML_TYPE);
		String loginString = "<request cmd='login' username='" + 
								BRAIN_HONEY_SUB_DOMAIN + "/" + userName + "' " +
								"password='" + password + "'/>";
		
		//StringEntity loginXML = new StringEntity("<request cmd='login' username='byuistest/testqs3' password='testing1'/>");//new StringEntity(loginString);
		System.out.println(loginString);
		StringEntity loginXML = new StringEntity(loginString);
		httpPost.setEntity(loginXML);
		
		response = httpClient.execute(httpPost);
		
		String responseString = convertResponseToString(response);
		
		if(!successfulLogin(responseString))
			throw new InvalidBrainHoneyLoginException();
		
		
		// Parse the XML response 
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
		org.w3c.dom.Document doc = documentBuilder.parse(new ByteArrayInputStream(responseString.getBytes()));
		
		// Get the user id
		final int USER_ELEMENT = 0;
		NodeList titleList = doc.getElementsByTagName("user");
		
		if(titleList.getLength() == 0)
			return "";
		
		NamedNodeMap attributes = titleList.item(USER_ELEMENT).getAttributes();	
		Node userIDAttribute = attributes.getNamedItem("userid");
		
		String userID = userIDAttribute.getNodeValue();
		
		return userID;		
	}
	
	/**
	 * Converts an HttpResponse into a string.
	 * @throws IOException 
	 * @throws IllegalStateException 
	 */
	private String convertResponseToString(HttpResponse response) throws IllegalStateException, IOException
	{
		HttpEntity responseEntity = response.getEntity();
		
		Scanner reader = new Scanner(responseEntity.getContent());
		String responseString = "";
		while(reader.hasNext())
			responseString += reader.nextLine();
		
		return responseString;
	}
	
	/**
	 * Gets a course from BrainHoney.
	 * 
	 * Parameters:
	 *	courseID: Id of the course being retrieved.
	 */
	public void getCourse(int courseID) throws ClientProtocolException, IOException 
	{
		String courseQuery = "?cmd=getcourse&courseid=" + Integer.toString(courseID);
		
		HttpGet courseGET = new HttpGet(BRAIN_HONEY_URL + courseQuery);
		
		response = httpClient.execute(courseGET);
		responseEntity = response.getEntity();
		printResponseEntity();		
	}
	
	/**
	 * Gets the real name of the user.
	 */
	public String getRealName(String userId) throws ClientProtocolException, IOException, ParserConfigurationException, IllegalStateException, SAXException
	{
		String getUserQuery = "?cmd=getuser&userid=" + userId;
		
		HttpGet userNameGET = new HttpGet(BRAIN_HONEY_URL + getUserQuery);
		response = httpClient.execute(userNameGET);
		responseEntity = response.getEntity();

		// Parse the XML response 
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
		org.w3c.dom.Document doc = documentBuilder.parse(responseEntity.getContent());
		
		final int USER_ELEMENT = 0;
		NodeList userList = doc.getElementsByTagName("user");
		
		if(userList.getLength() == 0)
			return "";
		
		NamedNodeMap attributes = userList.item(USER_ELEMENT).getAttributes();
		Node firstNameAttribute = attributes.getNamedItem("firstname");
		Node lastNameAttribute = attributes.getNamedItem("lastname");
		
		String name = firstNameAttribute.getNodeValue() + " " + lastNameAttribute.getNodeValue();
		
		return name;		
	}
	
	/*
	 * Get the courses the user is enrolled in.
	 */
	public String getUserGradebook(String userId) throws ClientProtocolException, IOException
	{
		String enrollmentQuery = "?cmd=getusergradebook2&userid=" + userId;
		
		System.out.println(enrollmentQuery);
		
		HttpGet userEnrollmentGET = new HttpGet(BRAIN_HONEY_URL + enrollmentQuery);
		response = httpClient.execute(userEnrollmentGET);
		responseEntity = response.getEntity();
		
		//printResponseEntity();
		
		Scanner reader = new Scanner(responseEntity.getContent());
		String gradeXML = "";
		while(reader.hasNext())
			gradeXML += reader.nextLine();
		
		return gradeXML;
	}
}
