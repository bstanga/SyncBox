package com.sbd.downloadmanagerfordropbox;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by StangaBogdan on 8/27/2015.
 */
public class App {

    private Context mContext;
    private SharedPreferences mPrefs;
    private AndroidAuthSession mSession;
    private Tracker mTracker;

    private static App sInstance;
    private static final String DB_TOKEN = "db_token";

    final static private String APP_KEY = "398e2v9wrso4mqe";
    final static private String APP_SECRET = "ihgl2f4hmqb2cnd";

    public static App init(Context context) {
        if (sInstance == null) {
            sInstance = new App(context);
        }
        return sInstance;
    }

    public App(Context context) {
        mContext = context;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        mSession = new AndroidAuthSession(appKeys);
    }

    public AndroidAuthSession getDBAuthSession(){
        return mSession;
    }

    public String getDBToken() {
        return mPrefs.getString(DB_TOKEN, null);
    }

    public void setDBToken(String token) {
        mPrefs.edit().putString(DB_TOKEN, token).apply();
    }

    public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(mContext);
            mTracker = analytics.newTracker(R.xml.global_tracker);
            mTracker.enableExceptionReporting(true);
            mTracker.enableAdvertisingIdCollection(true);
            mTracker.enableAutoActivityTracking(true);
        }
        return mTracker;
    }

}
