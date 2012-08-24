package BYUIS.app.grade_checker;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import BYUIS.app.grade_checker.BrainHoneyAcessImproved.BrainHoneyAccessException;
import android.content.Context;
import android.content.Intent;

/**
 * Singleton class that encapsulates the logic behind logging into the app.
 *
 */
public class Login 
{
	private static Login instance;
	
	private static final int MAX_LOGIN_ATTEMPTS = 5;  // maximum number of tries allowed before login is locked
	private int loginAttempts = 0;					  // the current number of attempts
	private String sessionPassword;					  // the password for the current session
	private boolean LOGGED_IN = false;
	private UserSettingsAdapter userSettings;
	private BrainHoneyAcessImproved brainHoney;	      // access to brainhoney
	
	/**
	 * Private constructor prevents instantiation
	 */
	private Login(Context context)
	{
		userSettings = UserSettingsAdapter.getInstance(context);
		brainHoney = new BrainHoneyAcessImproved();
	}
	
	/**
	 * Getter for the single instance
	 */
	public static Login getInstance(Context context)
	{
		if(instance == null)
			instance = new Login(context);
		
		return instance;
	}
	
	/**
	 * Logs the user into the app.  Returns false of log in fails
	 */
	public boolean logIn(String password)
	{
		// login fails if it is locked
		if(isLocked())
			return false;
		
		if(userSettings.passwordMatch(password))
		{
			LOGGED_IN = true;
			sessionPassword = password;
		}
		else
		{
			// else if the password did not match, record an attempt 
			loginAttempts++;	
		}
		
		return LOGGED_IN;
	}
	
	/**
	 * Initializes a new user to the app.  This method should be called asynchronously.
	 * 
	 * Returns true if login was successful and false otherwise.
	 */
	public boolean asyncInitializeUser(String username, String password)
	{
		// login fails if it is locked
		if(isLocked())
			return false;
			
		try 
		{
			brainHoney.login(username, password);
			userSettings.saveUsernameInfo(username, password, brainHoney.getRealName());
			sessionPassword = password;		
			LOGGED_IN = true;	
		} 
		catch (BrainHoneyAccessException e) 
		{
			LOGGED_IN = false;
			e.printStackTrace();
		} 
		catch (ClientProtocolException e) 
		{
			LOGGED_IN = false;
			e.printStackTrace();
		} 
		catch (IllegalStateException e) 
		{
			LOGGED_IN = false;
			e.printStackTrace();
		} 
		catch (BYUIS.app.grade_checker.BrainHoneyAcessImproved.InvalidBrainHoneyLoginException e) 
		{
			LOGGED_IN = false;
			loginAttempts += 1;
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			LOGGED_IN = false;
			e.printStackTrace();
		} 
		catch (ParserConfigurationException e) 
		{
			LOGGED_IN = false;
			e.printStackTrace();
		} 
		catch (SAXException e) 
		{
			LOGGED_IN = false;
			e.printStackTrace();
		}
		
		return LOGGED_IN;
	}
	
	/**
	 * Rests the login attempts so that the user has a fresh set of attempts
	 */
	public void resetAttempts()
	{
		loginAttempts = 0;
	}
	
	/**
	 * Getter for the session password
	 */
	public String getSessionPassword()
	{
		return sessionPassword;
	}
	
	/**
	 * Getter for the welcome message.
	 */
	public String getWelcomeMessage()
	{
		return "Welcome back " + userSettings.getRealName();
	}
	
	/**
	 * Indicates whether or not the login is locked
	 */
	public boolean isLocked()
	{
		if(loginAttempts >= MAX_LOGIN_ATTEMPTS)
			return true;
		else
			return false;
	}
	
	/**
	 * Indicates whether or not the user is logged in
	 */
	boolean isLoggedIn()
	{
		return LOGGED_IN;
	}
	
	/**
	 * Indicates whether or not a user exists that can be logged in
	 */
	boolean userExists()
	{
		return userSettings.userInfoExists();
	}
}
