package com.sbd.downloadmanagerfordropbox.network;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.android.AndroidAuthSession;
import com.sbd.downloadmanagerfordropbox.App;
import com.sbd.downloadmanagerfordropbox.Db;
import com.sbd.downloadmanagerfordropbox.MainActivity;
import com.sbd.downloadmanagerfordropbox.R;
import com.sbd.downloadmanagerfordropbox.helper.DownloadListener;
import com.sbd.downloadmanagerfordropbox.items.DBDownload;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by StangaBogdan on 8/30/2015.
 */
public class DownloadManager {

    private Context mContext;
    private App mApp;
    private Db mDb;
    private DropboxAPI<AndroidAuthSession> mDBApi;
    private File mZIPTemporaryFolder;
    private boolean mRunning = false;
    private boolean mCanceled = false;

    private NotificationCompat.Builder mNotification;
    private NotificationManager mNotificationManager;

    private static final int NOTIFICATION_ID = 1232;

    private static DownloadManager sInstance;

    public static DownloadManager init(Context context) {
        if (sInstance == null) {
            sInstance = new DownloadManager(context);
        }
        return sInstance;
    }

    public DownloadManager(Context context) {
        mContext = context;
        mApp = App.init(mContext);
        mDb = Db.init(context);
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        AndroidAuthSession session = mApp.getDBAuthSession();
        session.setOAuth2AccessToken(mApp.getDBToken());
        mDBApi = new DropboxAPI<>(session);
        mZIPTemporaryFolder = new File(Environment.getExternalStorageDirectory(), mContext.getResources().getString(R.string.app_name));
        mZIPTemporaryFolder.mkdirs();
    }

    public void start() {
        if (!mRunning) {
            ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
            boolean onlyWifi = PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("only_wifi", true);
            if (networkInfo != null && (networkInfo.getType() == ConnectivityManager.TYPE_WIFI || !onlyWifi)) {
                mCanceled = false;
                new DownloadAsync().execute();
            }
        }
    }

    public void cancel() {
        if (mRunning) {
            mCanceled = true;
        }
    }

    private class DownloadAsync extends AsyncTask<Void, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            mRunning = true;
            try {
                ArrayList<DBDownload> downloads = mDb.getDownloads();
                while (downloads.size() > 0) {
                    for (int i = 0; i < downloads.size(); i++) {
                        DBDownload dbDownload = downloads.get(i);
                        Log.i("DOWNLOAD", dbDownload.name);
                        showDownloadNotification(dbDownload);
                        download(dbDownload);
                    }
                    downloads = new ArrayList<>(mDb.getDownloads());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            removeNotification();
            mRunning = false;
            mCanceled = false;
            return null;
        }

    }

    public void download(DBDownload dbDownload) throws Exception {
        File file = null;
        try {
            if (dbDownload.isDir) {
                DropboxAPI.DropboxLink dbLink = mDBApi.share(dbDownload.path);
                String url = getDownloadURL(dbLink.url);
                File zippedFile = new File(mZIPTemporaryFolder, dbDownload.name + ".zip");
                downloadFile(url, zippedFile);
                File locationFile = new File(new File(dbDownload.location), dbDownload.name);
                locationFile.mkdirs();
                unzip(zippedFile, locationFile);
                zippedFile.delete();
            } else {
                File parentFile = new File(dbDownload.location);
                parentFile.mkdirs();
                file = new File(parentFile, dbDownload.name);
                if (!file.exists()) {
                    file.createNewFile();
                }
                final FileOutputStream outputStream = new FileOutputStream(file);
                mDBApi.getFile(dbDownload.path, null, outputStream, new ProgressListener() {
                    @Override
                    public void onProgress(long l, long l1) {
                        float progress = ((float) l * 100) / l1;
                        setNotificationProgress((int) progress);
                        if (mCanceled) {
                            try {
                                outputStream.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
            if (mCanceled) {
                throw new Exception();
            }
            mDb.fileDownloaded(dbDownload);
            DownloadListener.init().refreshDownloads();
        } catch (Exception e) {
            e.printStackTrace();
            if (file != null && file.exists()) {
                file.delete();
            }
            throw e;
        }
    }

    private String getDownloadURL(String shortUrl) throws Exception {
        URL inputURL = new URL(shortUrl);
        URLConnection conn = inputURL.openConnection();
        conn.connect();
        InputStream is = conn.getInputStream();
        String redirectedUrl = conn.getURL().toString();
        is.close();
        if (redirectedUrl.contains("?dl=0")) {
            redirectedUrl = redirectedUrl.replace("?dl=0", "?dl=1");
        } else {
            redirectedUrl += "?dl=1";
        }
        return redirectedUrl;
    }

    private void downloadFile(String downloadUrl, File location) throws Exception {
        URL url = new URL(downloadUrl);
        URLConnection connexion = url.openConnection();
        connexion.connect();
        InputStream input = new BufferedInputStream(url.openStream());
        OutputStream output = new FileOutputStream(location);
        byte data[] = new byte[1024];
        int count;
        while ((count = input.read(data)) != -1 && !mCanceled) {
            output.write(data, 0, count);
        }
        output.flush();
        output.close();
        input.close();
        if (mCanceled) {
            throw new Exception();
        }
    }

    public static void unzip(File zipFile, File targetDirectory) throws IOException {
        ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(zipFile)));
        try {
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Failed to ensure directory: " +
                            dir.getAbsolutePath());
                if (ze.isDirectory())
                    continue;
                FileOutputStream fout = new FileOutputStream(file);
                while ((count = zis.read(buffer)) != -1) {
                    fout.write(buffer, 0, count);
                }
                fout.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            zis.close();
        }
    }

    private void showNotification(DBDownload dbDownload) {
        mNotification = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.icon_small)
                .setContentText("Downloading")
                .setContentTitle(dbDownload.name)
                .setOngoing(true);
        Intent notificationIntent = new Intent(mContext, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mNotification.setContentIntent(resultPendingIntent);
        mNotification.setProgress(0, 100, true);
        mNotificationManager.notify(NOTIFICATION_ID, mNotification.build());
    }

    private void showDownloadNotification(DBDownload dbDownload) {
        if (mNotification == null) {
            showNotification(dbDownload);
        } else {
            mNotification.setContentText("Downloading")
                    .setContentTitle(dbDownload.name).setProgress(100, 0, true);
            mNotificationManager.notify(NOTIFICATION_ID, mNotification.build());
        }
    }

    private void setNotificationProgress(int progress) {
        if (mNotification != null) {
            mNotification.setContentText("Downloading " + progress + "%");
            mNotification.setProgress(100, progress, false);
            mNotificationManager.notify(NOTIFICATION_ID, mNotification.build());
        }
    }

    private void removeNotification() {
        mNotificationManager.cancel(NOTIFICATION_ID);
        mNotification = null;
    }

}
