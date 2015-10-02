package com.sbd.downloadmanagerfordropbox.helper;

/**
 * Created by StangaBogdan on 8/28/2015.
 */

import android.app.Activity;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.dropbox.client2.DropboxAPI;
import com.sbd.downloadmanagerfordropbox.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by StangaBogdan on 5/6/2015.
 */
public class ThumbnailDisplayer {

    private MainActivity mActivity;
    private MemoryCache mMemoryCache = new MemoryCache();
    private FileCache mFileCache;
    private Map<ImageView, String> mImageViews = Collections
            .synchronizedMap(new WeakHashMap<ImageView, String>());
    private ExecutorService mExecutorService;

    public ThumbnailDisplayer(MainActivity activity) {
        mActivity = activity;
        mFileCache = new FileCache(activity);
        mExecutorService = Executors.newFixedThreadPool(5);
    }

    public void display(ImageView imageView, String url) {
        if (url == null || url.equals("") || url.equals("null")) {
        } else {
            mImageViews.put(imageView, url);
            Bitmap bitmap = mMemoryCache.get(url);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                queuePhoto(url, imageView);
            }
        }
    }

    private void queuePhoto(String url, ImageView imageView) {
        PhotoToLoad p = new PhotoToLoad(url, imageView);
        mExecutorService.submit(new PhotosLoader(p));
    }

    private Bitmap getBitmap(String url) {
        File f = mFileCache.getFile(url);
        Bitmap b = MediaHelper.decodeThumbnailFromFile(f);
        if (b != null)
            return b;
        try {
            Bitmap bitmap = null;

            FileOutputStream os = new FileOutputStream(f);
            mActivity.getDBApi().getThumbnail(url, os, DropboxAPI.ThumbSize.ICON_64x64, DropboxAPI.ThumbFormat.JPEG, null);

            bitmap = MediaHelper.decodeThumbnailFromFile(f);
            return bitmap;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private class PhotoToLoad {
        public String url;
        public ImageView imageView;

        public PhotoToLoad(String u, ImageView i) {
            url = u;
            imageView = i;
        }
    }

    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;

        PhotosLoader(PhotoToLoad photoToLoad) {
            this.photoToLoad = photoToLoad;
        }

        public void run() {
            if (imageViewReused(photoToLoad))
                return;
            Bitmap bmp = getBitmap(photoToLoad.url);
            mMemoryCache.put(photoToLoad.url, bmp);
            if (imageViewReused(photoToLoad))
                return;
            BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
            Activity a = (Activity) photoToLoad.imageView.getContext();
            a.runOnUiThread(bd);
        }
    }

    boolean imageViewReused(PhotoToLoad photoToLoad) {
        String tag = mImageViews.get(photoToLoad.imageView);
        if (tag == null || !tag.equals(photoToLoad.url))
            return true;
        return false;
    }

    class BitmapDisplayer implements Runnable {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;

        public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
            bitmap = b;
            photoToLoad = p;
        }

        public void run() {
            if (imageViewReused(photoToLoad))
                return;
            if (bitmap != null) {
                photoToLoad.imageView.setImageBitmap(bitmap);
            }
        }
    }

    public void clearCache() {
        mMemoryCache.clear();
        mFileCache.clear();
    }

}