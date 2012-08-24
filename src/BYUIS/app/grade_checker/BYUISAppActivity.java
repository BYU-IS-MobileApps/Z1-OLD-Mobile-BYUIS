package BYUIS.app.grade_checker;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

/*
 * Encapsulates the common settings for an activity.  For example
 * each activity is run in full screen mode.  Instead of setting
 * full screen mode in each onCreate method for each activity those
 * commands are factored into this single class that each activity
 * inherits from.
 */
public class BYUISAppActivity extends Activity 
{
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		// go into full screen mode
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
							WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		// set transitions
		getWindow().setWindowAnimations(android.R.anim.slide_out_right);
	}
	
}
