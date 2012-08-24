package BYUIS.app.grade_checker;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import BYUIS.app.grade_checker.BYUISApplication.MaxLoginAttemptsException;
import BYUIS.app.grade_checker.BrainHoneyAccess.InvalidBrainHoneyLoginException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;

/*
 * Activity that handles first-run login.  In other words, when the app is started
 * for the first time, users will login through this activity.
 */
public class AccountInitActivity extends BYUISAppActivity implements OnClickListener
{

	
	/*public AccountInitActivity(Class type) 
	{
		super(AccountInitActivity.class);
	}*/

	private static final String TAG = "NewUsernameConfig";
	
	private final int NO_CONNECTION_DIALOG = 0;
	private final int BAD_PASSWORD_DIALOG = 1;
	
	private Button loginButton;
	private EditText userNameEntry;
	private EditText passwordEntry;
	
	private BYUISApplication app;
	
	public void onCreate(Bundle savedInstanceState)
	{
		//Log.d("AccountInitActivity", "Reached the beginning");
		super.onCreate(savedInstanceState);
		//Log.d("AccountInitActivity", "Reached the beginning 1");
		setContentView(R.layout.new_username_config);
		
		loginButton = (Button) findViewById(R.id.validateNewUser);
		loginButton.setOnClickListener(this);
		
		userNameEntry = (EditText) findViewById(R.id.userNameEntry);
		passwordEntry = (EditText) findViewById(R.id.passwordEntry);
		
		if(!networkAvailable())
			showDialog(NO_CONNECTION_DIALOG);
		//Log.d("AccountInitActivity", "Reached here");
	}
	
	/*
	 * Checks for network activity.
	 */
	private boolean networkAvailable()
	{
		return true;
	}	
	
	protected Dialog onCreateDialog(int id)
	{
		Dialog dialog;
		
		AlertDialog.Builder builder;
		switch(id)
		{
			case NO_CONNECTION_DIALOG:
				String noConnectionMessage = "Unable to register you at this time. " +
									"No connection is available.";
				
				builder = new AlertDialog.Builder(this);
				builder.setMessage(noConnectionMessage)
					.setCancelable(false)
					.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface dialog, int id)
							{	
								finish();
							}
					});
				
				dialog = builder.create();
			break;
			
			case BAD_PASSWORD_DIALOG:
				String badPasswordMessage = "Bad username or password!";
	
				builder = new AlertDialog.Builder(this);
				builder.setMessage(badPasswordMessage)
					.setCancelable(false)
					.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface dialog, int id)
							{	
							}
					});
	
				dialog = builder.create();
			break;
			
			default:
				dialog = null;
			break;
		}
		
		return dialog;
	}

	/*
	 * Initiates storage of credentials when the login button is clicked.
	 */
	public void onClick(View view) 
	{
		if(view.getId() == R.id.validateNewUser)
		{
			new RegisterNewUser().execute(userNameEntry.getText().toString(), passwordEntry.getText().toString());
		}
	}

	/**
	 * Asynchronous task that stores a new user to the device. 
	 */
	private class RegisterNewUser extends AsyncTask<String, Void, Void>
	{	
		// state flags
		private boolean LOGGED_IN = false;
		private boolean INCORRECT_PASSWORD = false;
		private boolean CONNECTION_ERROR = false;
		
		BYUISApplication app = (BYUISApplication) getApplication();
		
		/**
		 * Attempts to login.
		 */
		protected Void doInBackground(String... credentials) 
		{
			Log.d(AccountInitActivity.TAG, "doInBackground");
			
			final int USERNAME = 0;
			final int PASSWORD = 1;
			
			try 
			{
				app.asyncRegisterNewUser(credentials[USERNAME], credentials[PASSWORD]);
			} 
			catch (MaxLoginAttemptsException e) 
			{
				e.printStackTrace();
				AccountInitActivity.this.finish();
			}
			
			return null;
		}
		
		protected void onPostExecute(Void voidArg)
		{
			Log.d(AccountInitActivity.TAG, "onPostExecute");
			
			if(app.userIsLoggedIn())
				startActivity(new Intent(AccountInitActivity.this, CourseListActivity.class));
			else
				showDialog(BAD_PASSWORD_DIALOG);
			
			/*if(LOGGED_IN)
			{
				Log.d(UserPreferencesActivity.TAG, "Logged in!");
				
				startActivity(new Intent(UserPreferencesActivity.this, CourseListActivity.class));
			}
			else if(INCORRECT_PASSWORD)
			{
				Log.d(UserPreferencesActivity.TAG, "Incorrect password!");
				showDialog(BAD_PASSWORD_DIALOG);
			}
			else if(CONNECTION_ERROR)
			{
				Log.d(UserPreferencesActivity.TAG, "Connection error!");
			}*/
		}
	}	
	
	/**
	 * Asynchronous task that stores a new user to the device. 
	 */
	private class StoreNewUser extends AsyncTask<String, Void, Void>
	{	
		// state flags
		private boolean LOGGED_IN = false;
		private boolean INCORRECT_PASSWORD = false;
		private boolean CONNECTION_ERROR = false;
		
		private UserSettingsAdapter settings;
		
		/**
		 * No-arg constructor.
		 */
		public StoreNewUser()
		{
			/*BYUISApplication app = (BYUISApplication) getApplication();
			settings = new UserSettingsAdapter(app.getSharedPreferences());*/
			
			settings = ((BYUISApplication) getApplication()).getUserSettings();
		}
		
		/**
		 * Attempts to login.
		 */
		protected Void doInBackground(String... credentials) 
		{
			Log.d(AccountInitActivity.TAG, "doInBackground");
			
			final int USER_NAME = 0;
			final int PASSWORD = 1;
			BrainHoneyAccess brainHoney = new BrainHoneyAccess();

			try 
			{
				String userId = brainHoney.login(credentials[USER_NAME], credentials[PASSWORD]);
				String realName = brainHoney.getRealName(userId);
				
				settings.saveUsernameInfo(credentials[USER_NAME], credentials[PASSWORD], realName);
				
				Log.d(AccountInitActivity.TAG , "Real name is:" + realName);
				LOGGED_IN = true;
			} 
			catch(InvalidBrainHoneyLoginException e)
			{
				Log.d(AccountInitActivity.TAG , "Bad password");
				INCORRECT_PASSWORD = true;	
			}
			catch (Exception e) 
			{
				CONNECTION_ERROR = true;
				e.printStackTrace();
			} 
				
			return null;
		}
		
		protected void onPostExecute(Void voidArg)
		{
			Log.d(AccountInitActivity.TAG, "onPostExecute");
			
			if(LOGGED_IN)
			{
				Log.d(AccountInitActivity.TAG, "Logged in!");
				
				startActivity(new Intent(AccountInitActivity.this, CourseListActivity.class));
			}
			else if(INCORRECT_PASSWORD)
			{
				Log.d(AccountInitActivity.TAG, "Incorrect password!");
				showDialog(BAD_PASSWORD_DIALOG);
			}
			else if(CONNECTION_ERROR)
			{
				Log.d(AccountInitActivity.TAG, "Connection error!");
			}
		}
	}
}
