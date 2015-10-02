package com.sbd.downloadmanagerfordropbox;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import com.sbd.downloadmanagerfordropbox.items.DBDownload;
import com.sbd.downloadmanagerfordropbox.items.DBFile;

import java.util.ArrayList;

/**
 * Created by StangaBogdan on 8/30/2015.
 */
public class Db extends SQLiteOpenHelper {

    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME = "dbdownloader.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_DOWNLOADS = "table_downloads";
    public static final String ID = "id";
    public static final String PATH = "path";
    public static final String NAME = "name";
    public static final String LOCATION = "location";
    public static final String TIMESTAMP = "timestamp";
    public static final String HAS_THUMBNAIL = "thumbail";
    public static final String STATUS = "status";
    public static final String IS_DIR = "is_dir";
    public static final String SIZE = "size";

    public static final int PENDING = 0;
    public static final int COMPLETED = 1;

    private static Db sInstance;

    public static Db init(Context context) {
        if (sInstance == null) {
            sInstance = new Db(context);
        }
        return sInstance;
    }

    public Db(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mDb = getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String downloadsTableSQL = "CREATE TABLE " + TABLE_DOWNLOADS + " ("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + PATH + " TEXT,"
                + NAME + " TEXT,"
                + LOCATION + " TEXT,"
                + IS_DIR + " INTEGER,"
                + HAS_THUMBNAIL + " INTEGER,"
                + SIZE + " LONG,"
                + TIMESTAMP + " LONG,"
                + STATUS + " INTEGER DEFAULT " + PENDING + ")";
        db.execSQL(downloadsTableSQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void addDownload(DBFile dbFile, String location) {
        ContentValues cv = new ContentValues();
        cv.put(PATH, dbFile.path);
        cv.put(NAME, dbFile.name);
        cv.put(LOCATION, location);
        cv.put(SIZE, dbFile.size);
        cv.put(IS_DIR, dbFile.isDir ? 1 : 0);
        cv.put(HAS_THUMBNAIL, dbFile.hasThumbnail ? 1 : 0);
        cv.put(TIMESTAMP, System.currentTimeMillis());
        mDb.insert(TABLE_DOWNLOADS, null, cv);
    }

    public ArrayList<DBDownload> getDownloads() {
        ArrayList<DBDownload> downloads = new ArrayList<>();
        Cursor cursor = mDb.rawQuery("SELECT * FROM " + TABLE_DOWNLOADS + " WHERE " + STATUS + " = ?",
                new String[]{String.valueOf(PENDING)});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    DBDownload dbDownload = new DBDownload();

                    dbDownload.id = cursor.getString(0);
                    dbDownload.path = cursor.getString(1);
                    dbDownload.name = cursor.getString(2);
                    dbDownload.location = cursor.getString(3);
                    dbDownload.isDir = cursor.getInt(4) == 1;
                    dbDownload.hasThumbnail = cursor.getInt(5) == 1;
                    dbDownload.size = cursor.getLong(6);
                    dbDownload.timestamp = cursor.getLong(7);
                    dbDownload.status = cursor.getInt(8);

                    downloads.add(dbDownload);

                    cursor.moveToNext();
                }
            }
            cursor.close();
        }
        return downloads;
    }

    public ArrayList<DBDownload> getHistoryDownloads() {
        ArrayList<DBDownload> downloads = new ArrayList<>();
        Cursor cursor = mDb.rawQuery("SELECT * FROM " + TABLE_DOWNLOADS, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    DBDownload dbDownload = new DBDownload();

                    dbDownload.id = cursor.getString(0);
                    dbDownload.path = cursor.getString(1);
                    dbDownload.name = cursor.getString(2);
                    dbDownload.location = cursor.getString(3);
                    dbDownload.isDir = cursor.getInt(4) == 1;
                    dbDownload.hasThumbnail = cursor.getInt(5) == 1;
                    dbDownload.size = cursor.getLong(6);
                    dbDownload.timestamp = cursor.getLong(7);
                    dbDownload.status = cursor.getInt(8);

                    downloads.add(dbDownload);

                    cursor.moveToNext();
                }
            }
            cursor.close();
        }
        return downloads;
    }

    public void fileDownloaded(DBDownload dbDownload) {
        ContentValues cv = new ContentValues();
        cv.put(STATUS, COMPLETED);
        mDb.update(TABLE_DOWNLOADS, cv, ID + " = ?", new String[]{dbDownload.id});
    }

}
