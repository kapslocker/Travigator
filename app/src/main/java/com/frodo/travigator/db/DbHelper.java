package com.frodo.travigator.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteException;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import android.util.Log;

import com.frodo.travigator.R;
import com.frodo.travigator.app.trApp;
import com.frodo.travigator.events.DBChangedEvent;
import com.frodo.travigator.models.Stop;
import com.frodo.travigator.utils.CommonUtils;

import org.greenrobot.eventbus.EventBus;

public class DbHelper extends SQLiteOpenHelper {
    private Context mContext;
    private SQLiteDatabase db;

    public static final int DATABASE_VERSION = 1;
    public static String DATABASE_DIR = trApp.getAppContext().getString(R.string.DirName) + "/";
    public static String DATABASE_PATH = trApp.getAppContext().getFilesDir().getAbsolutePath() + "/" + DATABASE_DIR;
    public static String DATABASE_NAME;
    public static String TABLE_NAME;

    public static final String S_NO = "StopNumber";
    public static final String STOP_NAME = "StopName";
    public static final String LATITUDE = "Latitude";
    public static final String LONGITUDE = "Longitude";
    public static final String[] COLUMNS = {S_NO, STOP_NAME, LATITUDE, LONGITUDE};

    public DbHelper(Context context, String DbName, String TableName) {
        super(context, DATABASE_PATH + DbName, null, DATABASE_VERSION);
        try {
            DATABASE_NAME = DbName;
            TABLE_NAME = TableName;
            mContext = context;

            File dir = new File(DATABASE_PATH);
            dir.mkdirs();

            db = SQLiteDatabase.openDatabase(DATABASE_PATH + DATABASE_NAME, null,
                    SQLiteDatabase.OPEN_READWRITE |
                            SQLiteDatabase.CREATE_IF_NECESSARY);

            createTable();
        } catch (SQLiteException e) {
            Log.e("TAG", "ERROR", e);
            Toast.makeText(mContext, "SQL Error!", Toast.LENGTH_SHORT).show();
        }

    }

    public DbHelper(Context context, String DbName) {
        super(context, DATABASE_PATH + DbName, null, DATABASE_VERSION);
        try {
            mContext = context;
            DATABASE_NAME = DbName;
            TABLE_NAME = "";

            File dir = new File(DATABASE_PATH);
            dir.mkdirs();

            db = SQLiteDatabase.openDatabase(DATABASE_PATH + DATABASE_NAME, null,
                    SQLiteDatabase.OPEN_READWRITE |
                            SQLiteDatabase.CREATE_IF_NECESSARY);
        } catch (SQLiteException e) {
            Log.e("TAG", "ERROR", e);
            Toast.makeText(mContext, "SQL Error!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private void createTable() {
        try {
            String SQL_CREATE_ENTRIES = "CREATE TABLE IF NOT EXISTS " +
                    TABLE_NAME + " ( " +
                    "StopNumber INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "StopName TEXT, " +
                    "Latitude TEXT NOT NULL, " +
                    "Longitude TEXT NOT NULL " +
                    ")";

            db.execSQL(SQL_CREATE_ENTRIES);
        } catch (SQLiteException e) {
            Log.e("TAG", "ERROR", e);
            Toast.makeText(mContext, "SQL Error!", Toast.LENGTH_SHORT).show();
        }
    }

    public void setTable(Stop[] stops) {
        delTable();
        createTable();
        for (int i = 0; i < stops.length; i++) {
            Stop stop = stops[i];
            addStop(stop.getStop_name(), String.valueOf(stop.getStop_lat()), String.valueOf(stop.getStop_lon()));
        }
        EventBus.getDefault().post(new DBChangedEvent());
    }

    public void addStop(String stopName, String lat, String lon) {
        try {
            ContentValues values = new ContentValues();
            values.put(STOP_NAME, stopName);
            values.put(LATITUDE, lat);
            values.put(LONGITUDE, lon);

            db.insertOrThrow(TABLE_NAME, null, values);
            CommonUtils.log(STOP_NAME+" added");
        } catch (SQLiteException e) {
            Log.e("TAG", "ERROR", e);
            Toast.makeText(mContext, "SQL Error!", Toast.LENGTH_SHORT).show();
        }
    }

    public String getDbPath() {
        return db.getPath();
    }

    public Cursor getTables() {
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        return c;
    }

    public Cursor showTable() {
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        return cursor;
    }

    public void delTable() {
        try {
            String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
            db.execSQL(SQL_DELETE_TABLE);
        } catch (SQLiteException e) {
            Toast.makeText(mContext, "SQL Error!", Toast.LENGTH_SHORT).show();
        }
    }


    public void closeDB() {
        if (db != null) {
            db.close();
        }
    }
}
    