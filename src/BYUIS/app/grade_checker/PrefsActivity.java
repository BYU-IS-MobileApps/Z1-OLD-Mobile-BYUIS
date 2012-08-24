package BYUIS.app.grade_checker;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

// TODO Change this class into a Log-In activity
public class PrefsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener 
{
	private static final String TAG = "PrefsActivity";
	private static final String USERNAME_ID_PREF = "userID";
	private static final String PREFS_FILE_NAME = "PrefFile";
	
	/*
	* Constructor for preference activity.
	*/
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.prefs);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		preferences.registerOnSharedPreferenceChangeListener(this);
	}

	/*
	 * Validates the preferences for the user.
	 */
	public void onSharedPreferenceChanged(SharedPreferences preferences, String key) 
	{
		// If the username was changed check brain honey to make sure that it is a valid username
		if(key.equals("username"))
			new ValidateUsername().execute(preferences.getString(key, ""));
		
		Log.d(TAG, "Preference changed!");
	}
	
	/*
	 * Asyncrhonous task for checking user credentials.
	 */
	class ValidateUsername extends AsyncTask<String, Void, Boolean>
	{
		/*
		 * Stores the user ID in the shared preferences.
		 */
		private void storeUserID(String userID)
		{
			// store the user name
			SharedPreferences prefs = getSharedPreferences(PREFS_FILE_NAME, MODE_WORLD_WRITEABLE);
			SharedPreferences.Editor preferencesEdit = prefs.edit();
			preferencesEdit.putString(USERNAME_ID_PREF, userID);
			preferencesEdit.commit();
		}
		
		/*
		 * Checks to see if the username and password are valid Brainhoney
		 * credentials.
		 */
		protected Boolean doInBackground(String... credentials) 
		{
			BrainHoneyAccess brainHoney = new BrainHoneyAccess();
			String userID = "";
			try 
			{
				brainHoney.login();
				
				final int USERNAME = 0;
				//userID = brainHoney.getUserID(credentials[USERNAME]);
				
				storeUserID(userID);
			} 
			catch (Exception e) 
			{
				Log.d(TAG, "Unkown exception occurred during BrainHoney login.");
				e.printStackTrace();
				return false;
			}
			
			// if there is no username it is not valid
			if(userID.equals(""))
				return false;
				
			return true;
		}
		
		/*
		 * Indicate to the user whether or not the credentials are valid.
		 */
		protected void onPostExecute(Boolean isValid)
		{
			Log.d(TAG, "Posted!");
			
			if(!isValid)
				Toast.makeText(PrefsActivity.this, "Bad username!", Toast.LENGTH_LONG).show();
			else
				Toast.makeText(PrefsActivity.this, "Valid username", Toast.LENGTH_LONG).show();
		}
		
		
	}
}
