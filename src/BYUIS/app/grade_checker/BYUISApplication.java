package BYUIS.app.grade_checker;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import BYUIS.app.grade_checker.BrainHoneyAccess.InvalidBrainHoneyLoginException;
import BYUIS.app.grade_checker.BrainHoneyAcessImproved.BrainHoneyAccessException;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

/*
 * Represents common state of the application.  Also acts 
 * as an accessor to common application data.
 * 
 * TODO: ReCreate this class as a PreferenceAdapter class.
 */
public class BYUISApplication extends Application
{
	private final String TAG = "BYUISApplication";
	
	private final int MAX_LOGIN_ATTEMPTS = 5; 	// the max number of times a login can be attempted
	private int loginAttempts = 0;				// the count of how many times a login was attempted

	private String sessionPassword;			 	// password used by the user to initially login, used for brainhoney queries
	private BrainHoneyAcessImproved brainHoney;	// access to brainhoney
	private UserSettingsAdapter userSettings;	// adapter to the user settings
	
	private boolean LOGGED_IN = false;			// login state
	
	@Override
	public void onCreate() 
	{
		super.onCreate();
		
		sessionPassword = "";
		brainHoney = new BrainHoneyAcessImproved();		
		userSettings = new UserSettingsAdapter(
								getSharedPreferences(UserSettingsAdapter.PREFS_FILE, MODE_PRIVATE));
	}
	
	/**
	 * Exception that is thrown when login attempts have been exceeded
	 */
	public class MaxLoginAttemptsException extends Exception
	{
		
	}
	
	/**
	 * Logs the user into the app.  Each time the function is called a login
	 * attempt is recorded.  If the user exceeds the max number of login attempts
	 * allowed, an exception is thrown indicating that the max number of attempts
	 * has been exceeded. 
	 *  
	 * @param password User password.
	 * 
	 * @return True if the successfully logged in and false otherwise.
	 * @throws MaxLoginAttemptsException 
	 */
	public boolean login(String password) throws MaxLoginAttemptsException
	{
		if(loginAttempts >= MAX_LOGIN_ATTEMPTS){
			loginAttempts = 0;
			throw new MaxLoginAttemptsException();
		}
		if(!userSettings.passwordMatch(password))
		{
			loginAttempts += 1;
			return false;
		}
		
		LOGGED_IN = true;
		sessionPassword = password;
		
		return true;
	}
	
	/**
	 * Registers a new user to the application.  This method should be called
	 * asynchronously or on a separate thread.
	 * @throws MaxLoginAttemptsException 
	 */
	public void asyncRegisterNewUser(String username, String password) throws MaxLoginAttemptsException
	{	
		if(loginAttempts >= MAX_LOGIN_ATTEMPTS)
			throw new MaxLoginAttemptsException();
		
		try 
		{
			brainHoney.login(username, password);
			userSettings.saveUsernameInfo(username, password, brainHoney.getRealName());
			sessionPassword = password;		
			LOGGED_IN = true;
		} 
		catch (BrainHoneyAccessException e) 
		{
			e.printStackTrace();
		} 
		catch (ClientProtocolException e) 
		{
			e.printStackTrace();
		} 
		catch (IllegalStateException e) 
		{
			e.printStackTrace();
		} 
		catch (BYUIS.app.grade_checker.BrainHoneyAcessImproved.InvalidBrainHoneyLoginException e) 
		{
			loginAttempts += 1;
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (ParserConfigurationException e) 
		{
			e.printStackTrace();
		} 
		catch (SAXException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Indicates whether or not a user is already registed for the
	 * application or not.
	 * 
	 * @return True if a user is registered and false otherwise.
	 */
	public boolean userIsRegistered()
	{
		return userSettings.userInfoExists();
	}
	
	/**
	 * Indicates whether or not a user is logged into the app already.
	 * 
	 * @return True if a user is logged in and false otherwise.
	 */
	public boolean userIsLoggedIn()
	{
		return LOGGED_IN;
	}
	
	/**
	 * Getter for the user settings.
	 * 
	 * @return Adapter to the user settings.
	 */
	public UserSettingsAdapter getUserSettings()
	{
		return userSettings;
	}
}
