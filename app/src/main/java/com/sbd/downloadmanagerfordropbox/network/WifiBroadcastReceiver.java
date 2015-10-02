package com.sbd.downloadmanagerfordropbox.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

/**
 * Created by StangaBogdan on 9/2/2015.
 */
public class WifiBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        boolean onlyWifi = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("only_wifi", true);
        if (networkInfo != null && (networkInfo.getType() == ConnectivityManager.TYPE_WIFI || !onlyWifi)) {
            DownloadManager.init(context).start();
        } else {
            DownloadManager.init(context).cancel();
        }
    }
}