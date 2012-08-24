package BYUIS.app.grade_checker;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Adapter class for the user preferences.  
 */
public class UserSettingsAdapter 
{
	private static final String TAG = "UserSettingsAdapter: ";
	
	public static final String PREFS_FILE = "user_preferences.pref";
	private static final String PREF_USERNAME = "username";
	private static final String PREF_PASSWORD = "password";
	private static final String PREF_USER_ID = "userId";
	private static final String PREF_REAL_NAME = "realName";
	
	private SharedPreferences prefs; 

	/**
	 * Constructor.
	 * 
	 * @param prefs Shared preferences for the application.  
	 */
	public UserSettingsAdapter(SharedPreferences prefs) 
	{
		this.prefs = prefs;
	}
	
	/**
	 * Checks to see if a user is already stored in the system.
	 * 
	 * @return True is if there is a user stored and false otherwise.
	 */
	public boolean userInfoExists()
	{
		return (prefs.contains(PREF_USERNAME) 		&&
					prefs.contains(PREF_PASSWORD)	&&
					prefs.contains(PREF_REAL_NAME));
	}
	
	/**
	 *  Stores username, password, and real name into the
	 *  preferences.
	 *  
	 *  @param username Username string to be stored.
	 *  @param password Password for the user.
	 *  @param realName Real name of the user.
	 */
	public void saveUsernameInfo(String username, String password, String realName)
	{
		SharedPreferences.Editor prefsEditor = prefs.edit();		
		prefsEditor.putString(PREF_USERNAME, username);
		prefsEditor.putString(PREF_PASSWORD, hashPassword(password));
		prefsEditor.putString(PREF_REAL_NAME, realName);
		prefsEditor.commit();
	}
	
	/**
	 * Checks to see if a given password matches the stored password.
	 * 
	 * @param password Password to be checked.
	 * 
	 * @return True if the password matches and false otherwise.
	 */
	public boolean passwordMatch(String password)
	{
		String storedPassword = prefs.getString(PREF_PASSWORD, "");
		
		Log.d(TAG, "Given hash: " + hashPassword(password));
		Log.d(TAG, "Stored password: " + storedPassword);
		
		return (storedPassword.equals(hashPassword(password)));
	}	
	
	/**
	 * Getter for the real name of the user.
	 */
	public String getRealName()
	{
		return prefs.getString(PREF_REAL_NAME, "Student");
	}
	
	/**
	 * Getter for the username.
	 */
	public String getUsername()
	{
		return prefs.getString(PREF_USERNAME, "");
	}
	
	/**
	 * Getter for the user id.
	 */
	public String getUserId()
	{
		return prefs.getString(PREF_USER_ID, "");
	}
	
	/**
	 * Utility function for doing a MD5 hash of the password string.
	 * 
	 * @param password Password to be hashed.
	 * 
	 * @return String of the password hash.
	 */
	private String hashPassword(String password)
	{
		byte[] passwordBytes = null;
		try 
		{
			passwordBytes = password.getBytes("UTF-8");
		} 
		catch (UnsupportedEncodingException e1) 
		{
			e1.printStackTrace();
		}
		
		MessageDigest md = null;
		try 
		{
			md = MessageDigest.getInstance("MD5");
		} 
		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
		}
		byte[] hash = md.digest(passwordBytes);
		
		return new String(hash);
	}
}
