package aaditya.myruns5;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class HistoryProvider extends ContentProvider
{
	// database
	private DBHelper database;

	private static final String AUTHORITY = "aaditya.myruns5.historyprovider";

	public static final int ENTRIES_DIR = 100;
	public static final int ENTRIES_ID = 110;
	private static final String BASE_PATH= "history";
	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/"+BASE_PATH);


	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	static 
	{
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, ENTRIES_DIR);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", ENTRIES_ID);
	}



	//used to initialize this content provider. this method runs on ui thread, so should be quick. 
	//good place to instantiate database helper, if using database. 
	public boolean onCreate()
	{
		this.database = new DBHelper(getContext());
		return false;
	}

	//queries the provider for the records specified by either uri or 'selection'.
	@Override
	public Cursor query(Uri paramUri, String[] paramArrayOfString1, String paramString1, String[] paramArrayOfString2, String paramString2)
	{
		SQLiteQueryBuilder localSQLiteQueryBuilder = new SQLiteQueryBuilder();
		localSQLiteQueryBuilder.setTables("ENTRIES");
		switch (sURIMatcher.match(paramUri))
		{
		default:
			throw new IllegalArgumentException("Unknown URI: " + paramUri);
		case ENTRIES_ID:
			localSQLiteQueryBuilder.appendWhere("_id=" + paramUri.getLastPathSegment());
		case ENTRIES_DIR:
		}
		Cursor localCursor = localSQLiteQueryBuilder.query(this.database.getWritableDatabase(), paramArrayOfString1, paramString1, paramArrayOfString2, null, null, paramString2);
		localCursor.setNotificationUri(getContext().getContentResolver(), paramUri);
		return localCursor;
	}

	//Deletes row(s) specified by a content URI.
	@Override
	public int delete(Uri paramUri, String paramString, String[] paramArrayOfString)
	{
		int i = sURIMatcher.match(paramUri);
		SQLiteDatabase localSQLiteDatabase = this.database.getWritableDatabase();
		int j;
		switch (i)
		{
		default:
			throw new IllegalArgumentException("Unknown URI: " + paramUri);
		case ENTRIES_DIR:
			j = localSQLiteDatabase.delete("ENTRIES", paramString, paramArrayOfString);
		case ENTRIES_ID:
			getContext().getContentResolver().notifyChange(paramUri, null);
			String str = paramUri.getLastPathSegment();
			if (TextUtils.isEmpty(paramString))
				j = localSQLiteDatabase.delete("ENTRIES", "_id=" + str, null);
			else
				j = localSQLiteDatabase.delete("ENTRIES", "_id=" + str + " and " + paramString, paramArrayOfString);
			return j;
		}
	}


	@Override
	public String getType(Uri uri) {
		return null;
	}

	//insert the ContentValues into SQlite database.
	@Override
	public Uri insert(Uri paramUri, ContentValues paramContentValues)
	{
		//put the values in ContentValues to the ContentProvider which is denoted by the Uri. 
		long l = this.database.getWritableDatabase().insert("ENTRIES", null, paramContentValues);
		getContext().getContentResolver().notifyChange(paramUri, null);
		return Uri.parse("history/" + l);
	}

	  //Update row(s) in a content URI.
	public int update(Uri paramUri, ContentValues paramContentValues, String paramString, String[] paramArrayOfString)
	{
		int i = sURIMatcher.match(paramUri);
		SQLiteDatabase localSQLiteDatabase = this.database.getWritableDatabase();
		int j;
		switch (i)
		{
		default:
			throw new IllegalArgumentException("Unknown URI: " + paramUri);
		case ENTRIES_DIR:
			j = localSQLiteDatabase.update("ENTRIES", paramContentValues, paramString, paramArrayOfString);
		case ENTRIES_ID:
			getContext().getContentResolver().notifyChange(paramUri, null);
			String str = paramUri.getLastPathSegment();
			if (TextUtils.isEmpty(paramString))
				j = localSQLiteDatabase.update("ENTRIES", paramContentValues, "_id=" + str, null);
			else
				j = localSQLiteDatabase.update("ENTRIES", paramContentValues, "_id=" + str + " and " + paramString, paramArrayOfString);
			return j;
		}
	}
}

