package com.sbd.downloadmanagerfordropbox.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.sbd.downloadmanagerfordropbox.R;

/**
 * Created by StangaBogdan on 8/27/2015.
 */
public class FileHolder extends RecyclerView.ViewHolder {

    public TextView name;
    public ImageView icon, more;
    public CheckBox selector;

    public FileHolder(View itemView) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.file_name);
        icon = (ImageView) itemView.findViewById(R.id.file_icon);
        more = (ImageView) itemView.findViewById(R.id.file_more);
        selector = (CheckBox) itemView.findViewById(R.id.file_selector);
    }
}
