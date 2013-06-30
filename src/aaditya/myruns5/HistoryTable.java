package aaditya.myruns5;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class HistoryTable
{
  public static final String CREATE_TABLE_ENTRIES = "CREATE TABLE IF NOT EXISTS ENTRIES (_id INTEGER PRIMARY KEY AUTOINCREMENT, input_type INTEGER NOT NULL, activity_type INTEGER NOT NULL, date_time DATETIME NOT NULL, duration INTEGER NOT NULL, distance FLOAT, avg_pace FLOAT, avg_speed FLOAT,calories INTEGER, climb FLOAT, heartrate INTEGER, comment TEXT, privacy INTEGER, gps_data BLOB );";
  public static final String KEY_ACTIVITY_TYPE = "activity_type";
  public static final String KEY_AVG_PACE = "avg_pace";
  public static final String KEY_AVG_SPEED = "avg_speed";
  public static final String KEY_CALORIES = "calories";
  public static final String KEY_CLIMB = "climb";
  public static final String KEY_COMMENT = "comment";
  public static final String KEY_DATE_TIME = "date_time";
  public static final String KEY_DISTANCE = "distance";
  public static final String KEY_DURATION = "duration";
  public static final String KEY_GPS_DATA = "gps_data";
  public static final String KEY_HEARTRATE = "heartrate";
  public static final String KEY_INPUT_TYPE = "input_type";
  public static final String KEY_PRIVACY = "privacy";
  public static final String KEY_ROWID = "_id";
  public static final String TABLE_NAME_ENTRIES = "ENTRIES";

  public static void onCreate(SQLiteDatabase paramSQLiteDatabase)
  {
    paramSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS ENTRIES (_id INTEGER PRIMARY KEY AUTOINCREMENT, input_type INTEGER NOT NULL, activity_type INTEGER NOT NULL, date_time DATETIME NOT NULL, duration INTEGER NOT NULL, distance FLOAT, avg_pace FLOAT, avg_speed FLOAT,calories INTEGER, climb FLOAT, heartrate INTEGER, comment TEXT, privacy INTEGER, gps_data BLOB );");
  }

  public static void onUpgrade(SQLiteDatabase paramSQLiteDatabase, int paramInt1, int paramInt2)
  {
    Log.w(HistoryTable.class.getName(), "Upgrading database from version " + paramInt1 + " to " + paramInt2 + ", which will destroy all old data");
    paramSQLiteDatabase.execSQL("DROP TABLE IF EXISTS ");
    onCreate(paramSQLiteDatabase);
  }
}

