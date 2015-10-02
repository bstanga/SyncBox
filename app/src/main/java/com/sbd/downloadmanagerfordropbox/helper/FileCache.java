package com.sbd.downloadmanagerfordropbox.helper;

import android.content.Context;

import com.sbd.downloadmanagerfordropbox.R;

import java.io.File;

/**
 * Created by StangaBogdan on 8/28/2015.
 */
public class FileCache {

    private File cacheDir;

    public FileCache(Context context) {
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            File f = new File(android.os.Environment.getExternalStorageDirectory(),
                    context.getResources().getString(R.string.app_name));
            cacheDir = new File(f, "thumbnails");
        } else {
            cacheDir = context.getCacheDir();
        }
        if (!cacheDir.exists())
            cacheDir.mkdirs();
    }

    public File getFile(String url) {
        String filename = String.valueOf(url.hashCode());
        File f = new File(cacheDir, filename);
        return f;
    }

    public void clear() {
        File[] files = cacheDir.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            f.delete();
        }
    }

}
