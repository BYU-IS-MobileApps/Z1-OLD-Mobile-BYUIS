package BYUIS.app.grade_checker;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import BYUIS.app.grade_checker.BrainHoneyAccess.InvalidBrainHoneyLoginException;

public class BrainHoneyAcessImproved 
{
	private static final String BRAIN_HONEY_URL = "https://gls.agilix.com/dlap.ashx";
	private static final String BRAIN_HONEY_SUB_DOMAIN = "byuistest";
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String TEXT_XML_TYPE = "text/xml;";
	
	private HttpClient httpClient = new DefaultHttpClient();
	private String userId;
	
	/**
	 * Logs user into BrainHoney.  
	 * 
	 * @param userName Username for login.
	 * @param password Password for login.
	 * 
	 * @return The user id if successfully logged in.
	 * 
	 * TODO: Re-implement using JSON.
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 * @throws IllegalStateException 
	 */
	public void login(String userName, String password) throws InvalidBrainHoneyLoginException, ClientProtocolException, 
														IOException, ParserConfigurationException, 
														IllegalStateException, SAXException 
	{
		System.out.println("Beginning login");
		
		// Set login information
		HttpPost httpPost = new HttpPost(BRAIN_HONEY_URL);
		httpPost.setHeader(CONTENT_TYPE, TEXT_XML_TYPE);
		String loginString = "<request cmd='login' username='" + 
								BRAIN_HONEY_SUB_DOMAIN + "/" + userName + "' " +
								"password='" + password + "'/>";
		
		System.out.println("Login string: " + loginString);
		
		StringEntity loginXML = new StringEntity(loginString);
		httpPost.setEntity(loginXML);
		
		System.out.println("Going to request login ");
		
		HttpResponse response = httpClient.execute(httpPost);
		
		System.out.println("Login requested");
		
		// Parse the XML response 
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
		Document responseXML = documentBuilder.parse(response.getEntity().getContent());

		if(!successfulLogin(responseXML))
			throw new InvalidBrainHoneyLoginException();
		
		setUserIdFromResponse(responseXML);
	}	
	
	/**
	 * Gets the real name of the user .
	 * @throws BrainHoneyAccessException 
	 */
	public String getRealName() throws BrainHoneyAccessException
	{
		String getUserQuery = "?cmd=getuser&userid=" + userId;
		
		System.out.println("Real name query string: " + getUserQuery);
		
 		HttpGet userNameGET = new HttpGet(BRAIN_HONEY_URL + getUserQuery);
		HttpResponse response = null;
		
		try 
		{
			response = httpClient.execute(userNameGET);
		} 
		catch (Exception e) 
		{
			throw new BrainHoneyAccessException();
		}
		
		System.out.println("Get-real-name query successfully executed!");

		// Parse the XML response 
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = null;
		
		try 
		{
			documentBuilder = dbFactory.newDocumentBuilder();
		} 
		catch (ParserConfigurationException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Document doc = null;
		try 
		{
			doc = documentBuilder.parse(response.getEntity().getContent());
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		printXmlDocument(doc);
		
		final int USER_ELEMENT = 0;
		NodeList userList = doc.getElementsByTagName("user");
		
		if(userList.getLength() == 0)
			return "error student";
		
		NamedNodeMap attributes = userList.item(USER_ELEMENT).getAttributes();
		Node firstNameAttribute = attributes.getNamedItem("firstname");
		Node lastNameAttribute = attributes.getNamedItem("lastname");
		
		String name = firstNameAttribute.getNodeValue() + " " + lastNameAttribute.getNodeValue();
		
		return name;		
	}	
	
	
	/**
	 * Gets the courses the user is enrolled in.
	 *  
	 * @throws BrainHoneyAccessException 
	 * @throws ParserConfigurationException 
	 */
	public Document getUserGradebook() throws BrainHoneyAccessException, ParserConfigurationException 
	{
		HttpClient httpClient = new DefaultHttpClient();
		
		String enrollmentQuery = "?cmd=getusergradebook2&userid=" + userId;
		
		try 
		{
			HttpGet userEnrollmentGET = new HttpGet(BRAIN_HONEY_URL + enrollmentQuery);
			HttpResponse response = httpClient.execute(userEnrollmentGET);
			
			// Parse the XML response 
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
			Document gradeXML = documentBuilder.parse(response.getEntity().getContent());
			
			return gradeXML;
		} 
		catch (ClientProtocolException e) 
		{
			e.printStackTrace();
			return null;
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			return null;
		} 
		catch (IllegalStateException e) 
		{
			e.printStackTrace();
			return null;
		} 
		catch (SAXException e) 
		{
			e.printStackTrace();
			return null;
		}
	}	
	
	/**
	 * Exception for when login fails due to invalid credentials.
	 */
	public class InvalidBrainHoneyLoginException extends Exception
	{
	}
	
	/**
	 * Exception for when the user is 
	 */
	public class BrainHoneyAccessException extends Exception
	{
		
	}
	
	/**
	 * Utility debug function used for printing out parsed xml documents.
	 * @param doc
	 */
	private void printXmlDocument(Document doc)
	{
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer xform = null;
		try {
			xform = factory.newTransformer();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Source src = new DOMSource(doc);
		StringWriter writer = new StringWriter();
		Result result = new javax.xml.transform.stream.StreamResult(writer);
		try {
			xform.transform(src, result);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(writer.toString());		
	}
	
	/**
	 * Examines the response XML for a login and determines if the login was successful or not.
	 * 
	 * @param responseXML
	 * @return
	 */
	private boolean successfulLogin(Document responseXML)
	{
		NodeList response = responseXML.getElementsByTagName("response");
		
		final int RESPONSE_ELEMENT = 0;
		NamedNodeMap attributes = response.item(RESPONSE_ELEMENT).getAttributes();
		Node responseCode = attributes.getNamedItem("code");
		
		if(responseCode.getNodeValue().equals("OK"))
			return true;
		else
			return false;
	}
	
	/**
	 * Sets the user id to used for queries from the login response xml.
	 * 
	 * @param responseXML
	 * @return
	 */
	private void setUserIdFromResponse(Document loginResponseXML)
	{
		final int USER_ELEMENT = 0;
		NodeList titleList = loginResponseXML.getElementsByTagName("user");
		
		NamedNodeMap attributes = titleList.item(USER_ELEMENT).getAttributes();	
		Node userIDAttribute = attributes.getNamedItem("userid");
		
		userId = userIDAttribute.getNodeValue();	
	}	
}
