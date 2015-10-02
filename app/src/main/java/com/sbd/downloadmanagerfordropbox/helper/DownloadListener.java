package com.sbd.downloadmanagerfordropbox.helper;

/**
 * Created by StangaBogdan on 9/2/2015.
 */
public class DownloadListener {

    private com.sbd.downloadmanagerfordropbox.interfaces.DownloadListener mDownloadListener;

    private static DownloadListener sInstance;

    public static DownloadListener init() {
        if (sInstance == null) {
            sInstance = new DownloadListener();
        }
        return sInstance;
    }

    public void setOnDownloadListener(com.sbd.downloadmanagerfordropbox.interfaces.DownloadListener downloadListener) {
        mDownloadListener = downloadListener;
    }

    public void refreshDownloads() {
        if (mDownloadListener != null) {
            mDownloadListener.refreshDownloads();
        }
    }

}
