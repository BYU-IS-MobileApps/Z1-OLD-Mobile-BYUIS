package BYUIS.app.grade_checker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends BYUISAppActivity implements OnClickListener 
{
	private static final String TAG = "LoginActivity";		// debug tag
	
	private final int BAD_PASSWORD_DIALOG = 0;
	private final int CONFIRM_USER_SWITCH_DIALOG = 1;
	private final int TOO_MANY_LOG_IN_ATTEMPTS = 2;
	
	private Button loginButton;					// button that initiates login 
	private EditText passwordField;				// text field for password entry
	private TextView switchUserLink;			// text view that acts as a "switch user" link 
	
	//private BYUISApplication app;				// holds state information for the app and is accessor for app data
	private Login login;					    // model for the login
	
	@Override 
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);
		login = Login.getInstance(this);
		
		// if there is no user to be logged in, the app needs to be initialized
		if(!login.userExists())
			startActivity(new Intent(LoginActivity.this, AccountInitActivity.class));
		
		// else display the welcome message for the user
		TextView welcomeMessage = (TextView) findViewById(R.id.loginWelcomeMessage);
		welcomeMessage.setText(login.getWelcomeMessage());
		
		// in case this activity is resumed after closing, reset allowed attempts
		login.resetAttempts();
		
		onCreateUIElements();
	}
	
	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
	}
	
	/**
	 * Creates the dialog boxes for this activity.
	 */
	protected Dialog onCreateDialog(int id)
	{
		Dialog dialog;
		
		AlertDialog.Builder builder;
		switch(id)
		{	
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
			
			case CONFIRM_USER_SWITCH_DIALOG:
				String confirmationMessage = "Are you sure you want to switch user accounts? Previous user's " +
												"settings and data will be lost!";
				
				builder = new AlertDialog.Builder(this);
				builder.setMessage(confirmationMessage)
					.setCancelable(false)
					.setNegativeButton("No", new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface dialog, int id)
							{	
							}
					})					
					.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
							public void onClick(DialogInterface dialog, int id)
							{	
								// if they want to switch users, delete their data and 
								// open the form for re-initializing the app
								UserSettingsAdapter.getInstance(LoginActivity.this).clearData();
								startActivity(new Intent(LoginActivity.this, AccountInitActivity.class));
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
	
	/**
	 * Initializes the UI objects for the activity.
	 */
	private void onCreateUIElements()
	{
		loginButton = (Button) findViewById(R.id.loginLoginButton);
		loginButton.setOnClickListener(this);
		
		passwordField = (EditText) findViewById(R.id.loginPasswordEntry);	
		
		switchUserLink = (TextView) findViewById(R.id.loginSwitchUser);
		switchUserLink.setOnClickListener(this);
	}
	
	/**
	 * Closes login screen once it is paused.
	 */
	protected void onPause() 
	{
		super.onPause();
		
		finish();
	}

	/**
	 * Click listener.
	 */
	public void onClick(View v) 
	{
		switch(v.getId())
		{
			case R.id.loginLoginButton:
				loginApp();
			break;
			
			case R.id.loginSwitchUser:
				showDialog(CONFIRM_USER_SWITCH_DIALOG);
			break;
		}
	}	
	
	/**
	 * Helper function for logging into the app.  If too many log in
	 * attempts are attempted, the activity gets closed.
	 */
	private void loginApp()
	{
		// try logging in given the password provided
		if(login.logIn(passwordField.getText().toString()))
		{
			// for the moment grades are downloaded whenever the user logs into the app
			startService(new Intent(this, GradesUpdaterService.class));	
			startActivity(new Intent(LoginActivity.this, CourseListActivity.class));
		}
		else
		{
			showDialog(BAD_PASSWORD_DIALOG);
		}
		
		if(login.isLocked())
			showDialog(TOO_MANY_LOG_IN_ATTEMPTS);	
	}
}
