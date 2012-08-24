package BYUIS.app.grade_checker;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;

public class CourseListActivity extends ListActivity 
{
	private static final String TAG = "CourseGradeList";
	private ProgressDialog loadingDialog;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);	
		
		loadingDialog = ProgressDialog.show(this, "", "Loading courses...", true, true);
		new LoadCoursesFromDb().execute();
	}

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
			startManagingCursor(gradesQueryResult);
			
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
