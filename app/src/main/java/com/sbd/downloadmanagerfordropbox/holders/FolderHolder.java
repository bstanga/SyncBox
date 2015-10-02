package com.sbd.downloadmanagerfordropbox.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sbd.downloadmanagerfordropbox.R;

/**
 * Created by StangaBogdan on 8/28/2015.
 */
public class FolderHolder extends RecyclerView.ViewHolder {

    public TextView name;
    public ImageView icon;

    public FolderHolder(View itemView) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.folder_name);
        icon = (ImageView) itemView.findViewById(R.id.folder_icon);
    }
}
