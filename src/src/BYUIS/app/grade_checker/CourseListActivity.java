package BYUIS.app.grade_checker;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;

public class CourseListActivity extends ListActivity 
{
	private static final String TAG = "CourseGradeList";
	private ProgressDialog loadingDialog;
	private BroadcastReceiver reciever = new BroadcastReceiver()
		{
			@Override
			public void onReceive(Context context, Intent intent) 
			{
				System.out.println("Recieved!");
				new LoadCoursesFromDb().execute();
			}
		};
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);	
		
		loadingDialog = ProgressDialog.show(this, "", "Loading courses...", true, true);
		new LoadCoursesFromDb().execute();
		
		IntentFilter filter = new IntentFilter(GradesUpdaterService.GRADES_DWNLDED_BRDCST);
		registerReceiver(reciever, filter);
	}
	
	/**
	 * Setup the action bar menu
	 */
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.action_bar_menu, menu);
		return true;
    }

    /**
     * Handles action bar selections
     */
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch(item.getItemId())
    	{
    		case R.id.menu_download_grades:
    			startService(new Intent(this, GradesUpdaterService.class));
    		return true;
    		
    		default:
    		return super.onOptionsItemSelected(item); 
    	}
    }
    
    /**
     * Loads a courses into the DB
     */
	private class LoadCoursesFromDb extends AsyncTask<Void, Void, Cursor>
	{
		private GradeBookDbAdapter gradesDb;

		@Override
		protected Cursor doInBackground(Void... params) 
		{
			gradesDb = new GradeBookDbAdapter(CourseListActivity.this);
			gradesDb.open();
			Cursor gradesQueryResult = gradesDb.getAllCourses();
			
			return gradesQueryResult;
		}
		
		protected void onPostExecute(Cursor gradesQueryResult)
		{		
			Log.d(TAG, "" + gradesQueryResult.getColumnCount());
			
			String[] fromColumns = {GradeBookDbHelper.C_COURSE_TITLE, GradeBookDbHelper.C_COURSE_GRADE}; 
			int[] toRowIds = {R.id.courseName, R.id.courseGrade};
			
			ListAdapter dbListAdapter = new SimpleCursorAdapter(CourseListActivity.this,
												R.layout.overall_grade_row,
												gradesQueryResult,
												fromColumns,
												toRowIds);			
			setListAdapter(dbListAdapter);
			
			gradesDb.close();
			loadingDialog.dismiss();
		}		
	}
}
