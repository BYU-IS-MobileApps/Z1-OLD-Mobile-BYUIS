package BYUIS.app.grade_checker;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/*
 * Activity that handles first-run login.  In other words, when the app is started
 * for the first time, users will login through this activity.
 */
public class AccountInitActivity extends BYUISAppActivity implements OnClickListener
{
	private static final String TAG = "NewUsernameConfig";
	
	private final int NO_CONNECTION_DIALOG = 0;
	private final int BAD_PASSWORD_DIALOG = 1;
	private final int TOO_MANY_LOG_IN_ATTEMPTS = 2;
	
	private Button loginButton;
	private EditText userNameEntry;
	private EditText passwordEntry;
	
	private Login login;					    // model for the login
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_username_config);
		login = Login.getInstance(this);
		
		// in case this activity is resumed after closing, reset allowed attempts
		login.resetAttempts();
		
		loginButton = (Button) findViewById(R.id.validateNewUser);
		loginButton.setOnClickListener(this);
		
		userNameEntry = (EditText) findViewById(R.id.userNameEntry);
		passwordEntry = (EditText) findViewById(R.id.passwordEntry);
		
		if(!networkAvailable())
			showDialog(NO_CONNECTION_DIALOG);
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
			
			case TOO_MANY_LOG_IN_ATTEMPTS:
				String tooManyTriesMessage = "Too many attempts!  Closing the app.";
	
				builder = new AlertDialog.Builder(this);
				builder.setMessage(tooManyTriesMessage)
					.setCancelable(false)
					.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface dialog, int id)
							{	
								// close the app
								finish();
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
		// error flags
		private boolean LOGGED_IN = false;
		private boolean TOO_MANY_ATTEMPTS = false;
		
		/**
		 * Attempts to login.
		 */
		protected Void doInBackground(String... credentials) 
		{
			Log.d(AccountInitActivity.TAG, "doInBackground");
			
			final int USERNAME = 0;
			final int PASSWORD = 1;
	
			if(login.isLocked())
			{
				TOO_MANY_ATTEMPTS = true;
				return null;
			}
				
			LOGGED_IN = login.asyncInitializeUser(credentials[USERNAME], credentials[PASSWORD]);
			
			return null;
		}
		
		protected void onPostExecute(Void voidArg)
		{
			Log.d(AccountInitActivity.TAG, "onPostExecute");
			
			if(LOGGED_IN)
			{
				// for the moment grades are downloaded whenever the user logs into the app
				startService(new Intent(AccountInitActivity.this, GradesUpdaterService.class));	
				startActivity(new Intent(AccountInitActivity.this, CourseListActivity.class));
			}
			else if(TOO_MANY_ATTEMPTS)
			{
				AccountInitActivity.this.showDialog(TOO_MANY_LOG_IN_ATTEMPTS);
			}
			else
			{
				AccountInitActivity.this.showDialog(BAD_PASSWORD_DIALOG);
			}
		}
	}
}
