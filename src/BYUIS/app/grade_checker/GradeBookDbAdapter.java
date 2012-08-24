package BYUIS.app.grade_checker;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/*
 * Adapts to the gradebook database.  Implements CRUD type operations
 * for the database;
 */
public class GradeBookDbAdapter 
{
	private static final String TAG = "GradebookDbAdapter: "; 
	
	private Context context;
	private SQLiteDatabase db;
	private GradeBookDbHelper dbHelper;
	
	/*
	 * Constructor.
	 */
	GradeBookDbAdapter(Context context)
	{
		this.context = context;
	}
	
	/*
	 * Opens the database for operations.
	 */
	public void open()
	{
		dbHelper = new GradeBookDbHelper(context);
		db = dbHelper.getWritableDatabase();
	}
	
	/*
	 * Closes the database.
	 */
	public void close()
	{
		db.close();
	}
	
	/*
	 * Gets all of the courses with the grades from the database.
	 */
	public Cursor getAllCourses()
	{
		String[] selectedColumns = {
				GradeBookDbHelper.C_ID,
				GradeBookDbHelper.C_COURSE_TITLE,
				GradeBookDbHelper.C_COURSE_GRADE
				};
		
		Cursor results = db.query(
				true, 
				GradeBookDbHelper.OVERALL_GRADES_TABLE_NAME,
				selectedColumns,
				null, null, null, null, null, null);		
		
		Log.d(TAG, "Queried grades in database");
		
		return results;
	}
	
	public void insertTestCourses()
	{
		String enrollmentId = "5";
		String courseTitle = "Spanish";
		String courseGrade = "95.5%";
		
		insertOverallGrade(enrollmentId, courseTitle, courseGrade);
	}
	
	/*
	 * Gets all of the grades for a particular course.
	 */
	public Cursor getCourseGrades(int courseID)
	{
		return null;
	}
	
	/*
	 * Clear all grade information.
	 */
	public void clearAllGrades()
	{
		db.delete(GradeBookDbHelper.OVERALL_GRADES_TABLE_NAME, null, null);
	}
	
	/*
	 * Inserts a new overall grade into the database.
	 */
	public void insertOverallGrade(String enrollmentId, String courseTitle, String courseGrade)
	{
		ContentValues contentValues = new ContentValues();
		contentValues.put(GradeBookDbHelper.C_ENROLLMENT_ID, enrollmentId);
		contentValues.put(GradeBookDbHelper.C_COURSE_TITLE, courseTitle);
		contentValues.put(GradeBookDbHelper.C_COURSE_GRADE, courseGrade);
		
		db.insert(GradeBookDbHelper.OVERALL_GRADES_TABLE_NAME, null, contentValues);
	}
	
	public void insertGrades(ArrayList<ContentValues> gradeEntries)
	{
		for(ContentValues entry : gradeEntries)
			db.insert(GradeBookDbHelper.OVERALL_GRADES_TABLE_NAME, null, entry);
	}
}
