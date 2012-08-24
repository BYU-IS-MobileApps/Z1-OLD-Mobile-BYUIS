package BYUIS.app.grade_checker;

import BYUIS.app.grade_checker.BYUISApplication.MaxLoginAttemptsException;
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
import android.widget.TextView;

public class LoginActivity extends BYUISAppActivity implements OnClickListener 
{
	private static final String TAG = "LoginActivity";		// debug tag
	
	private final int BAD_PASSWORD_DIALOG = 0;
	
	private Button loginButton;					// button that initiates login 
	private EditText passwordField;				// text field for password entry
	
	private BYUISApplication app;				// holds state information for the app and is accessor for app data
	
	@Override 
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.splash_screen);
		
		app = (BYUISApplication) getApplication();
		
		onCreateUIElements();
		
		if(app.userIsRegistered())
		{
			TextView welcomeMessage = (TextView) findViewById(R.id.loginWelcomeMessage);
			welcomeMessage.setText("Welcome back " + app.getUserSettings().getRealName());
		}
		else
		{
			startActivity(new Intent(LoginActivity.this, AccountInitActivity.class));
		}
		
		/**
		 * Testing line
		 */
		startService(new Intent(this, GradesUpdaterService.class));
	}
	
	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
	}
	
	
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
				try 
				{
					if(app.login(passwordField.getText().toString()))
						startActivity(new Intent(LoginActivity.this, CourseListActivity.class));
					else
						showDialog(BAD_PASSWORD_DIALOG);
				} 
				catch (MaxLoginAttemptsException e) 
				{
					e.printStackTrace();
					finish();
				}
			break;
		}
	}	
}
