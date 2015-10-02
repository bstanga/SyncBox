package com.sbd.downloadmanagerfordropbox.interfaces;


import com.sbd.downloadmanagerfordropbox.items.DBFile;

/**
 * Created by StangaBogdan on 8/27/2015.
 */
public interface OnFileListener {
    void onFileSelected(DBFile file);

    void onFileMoreSelected(DBFile file);
}
