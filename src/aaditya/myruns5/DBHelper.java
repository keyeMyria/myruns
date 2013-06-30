package aaditya.myruns5;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper
{
  private static final String DATABASE_NAME = "MyRunsDB";
  private static final int DATABASE_VERSION = 1;

  public DBHelper(Context paramContext)
  {
    super(paramContext, "MyRunsDB", null, 1);
  }

  public void onCreate(SQLiteDatabase paramSQLiteDatabase)
  {
    HistoryTable.onCreate(paramSQLiteDatabase);
  }

  public void onUpgrade(SQLiteDatabase paramSQLiteDatabase, int paramInt1, int paramInt2)
  {
    HistoryTable.onUpgrade(paramSQLiteDatabase, paramInt1, paramInt2);
  }
}

