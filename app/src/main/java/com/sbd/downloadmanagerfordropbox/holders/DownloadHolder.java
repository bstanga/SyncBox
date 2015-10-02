package com.sbd.downloadmanagerfordropbox.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sbd.downloadmanagerfordropbox.R;


/**
 * Created by StangaBogdan on 9/2/2015.
 */
public class DownloadHolder extends RecyclerView.ViewHolder {

    public TextView status, name, location, timestamp;
    public ImageView icon;

    public DownloadHolder(View itemView) {
        super(itemView);
        status = (TextView) itemView.findViewById(R.id.download_status);
        name = (TextView) itemView.findViewById(R.id.download_name);
        location = (TextView) itemView.findViewById(R.id.download_location);
        timestamp = (TextView) itemView.findViewById(R.id.download_timestamp);
        icon = (ImageView) itemView.findViewById(R.id.download_icon);
    }
}
