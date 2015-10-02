package com.sbd.downloadmanagerfordropbox.interfaces;

import java.io.File;

/**
 * Created by StangaBogdan on 8/28/2015.
 */
public interface OnLocalFolderListener {
    void onFolderSelected(File file);
    void onFolderReturned();
}
