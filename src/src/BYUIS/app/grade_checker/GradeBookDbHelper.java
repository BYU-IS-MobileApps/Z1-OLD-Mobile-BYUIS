package BYUIS.app.grade_checker;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/*
 * Interface to grade and course information. The name of the class, "GradeBookDb", 
 * suggests that there is a database being accessed, but for the time being the 
 * implementation is accessing cached XML files.
 */
public class GradeBookDbHelper extends SQLiteOpenHelper  
{
	private static final String DB_NAME = "gradebook.db";						// name of the database
	private static final int DB_VERSION = 2;									// database version 
	public static final String OVERALL_GRADES_TABLE_NAME = "overall_grades";	//name of the overall grade table
	
	// column names for the overall grades table
	public static final String C_ID = "_id";
	public static final String C_ENROLLMENT_ID = "enrollment_id";
	public static final String C_COURSE_TITLE = "course_title";
	public static final String C_COURSE_GRADE = "course_grade";
	
	// table creation strings
	private static final String SQL_CREATE_OVERALL_GRADES_TABLE = 
						"CREATE TABLE " + OVERALL_GRADES_TABLE_NAME + " (" +
							C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
							C_ENROLLMENT_ID + " TEXT, " +
							C_COURSE_TITLE + " TEXT, " + 
							C_COURSE_GRADE + " TEXT);";
	
	private static final String SQL_DELETE_OVERALL_GRADES_TABLE =
			"DROP TABLE IF EXISTS " + OVERALL_GRADES_TABLE_NAME;	
	
	GradeBookDbHelper(Context context)
	{
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		db.execSQL(SQL_CREATE_OVERALL_GRADES_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		if(oldVersion != newVersion)
			db.execSQL(SQL_DELETE_OVERALL_GRADES_TABLE);
		
		onCreate(db);
	}
}
