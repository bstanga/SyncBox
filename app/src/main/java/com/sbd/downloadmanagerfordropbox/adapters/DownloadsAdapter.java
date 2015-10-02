package com.sbd.downloadmanagerfordropbox.adapters;

import android.content.Intent;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sbd.downloadmanagerfordropbox.MainActivity;
import com.sbd.downloadmanagerfordropbox.R;
import com.sbd.downloadmanagerfordropbox.SettingsActivity;
import com.sbd.downloadmanagerfordropbox.helper.ThumbnailDisplayer;
import com.sbd.downloadmanagerfordropbox.holders.DownloadHolder;
import com.sbd.downloadmanagerfordropbox.items.DBDownload;

import java.util.ArrayList;

/**
 * Created by StangaBogdan on 9/2/2015.
 */
public class DownloadsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<DBDownload> mDownloads;
    private MainActivity mActivity;
    private ThumbnailDisplayer mThumbnailDisplayer;

    public DownloadsAdapter(MainActivity activity, ArrayList<DBDownload> dbDownloads) {
        mActivity = activity;
        mDownloads = dbDownloads;
        mThumbnailDisplayer = new ThumbnailDisplayer(activity);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                return new HeaderHolder(LayoutInflater.from(mActivity).inflate(R.layout.drawer_header, parent, false));
            case 1:
                return new RecyclerView.ViewHolder(LayoutInflater.from(mActivity).inflate(R.layout.item_empty_download, parent, false)) {
                    @Override
                    public String toString() {
                        return super.toString();
                    }
                };
            default:
                return new DownloadHolder(LayoutInflater.from(mActivity).inflate(R.layout.item_download, null));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        } else {
            return mDownloads.get(position - 1).path != null ? 2 : 1;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position == 0) {
            HeaderHolder headerHolder = (HeaderHolder) holder;
            String name = PreferenceManager.getDefaultSharedPreferences(mActivity).getString("name", "Unknown name");
            headerHolder.name.setText(name);
            String[] names = name.split(" ");
            int len = names.length > 2 ? 2 : names.length;
            String initials = "";
            for (int i = 0; i < len; i++) {
                initials += names[i].substring(0, 1);
            }
            headerHolder.initials.setText(initials);
            headerHolder.settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.startActivity(new Intent(mActivity, SettingsActivity.class));
                }
            });
        } else {
            DBDownload dbDownload = mDownloads.get(position - 1);
            if (dbDownload.path != null) {
                DownloadHolder downloadHolder = (DownloadHolder) holder;
                downloadHolder.name.setText(dbDownload.name);
                String path = dbDownload.location;
                path = path.replace(Environment.getExternalStorageDirectory().getAbsolutePath(), "");
                if (path.equals("")) {
                    path = "/";
                }
                downloadHolder.location.setText(path);
                downloadHolder.timestamp.setText(getReadableTime(dbDownload.timestamp));
                if (dbDownload.isDir) {
                    downloadHolder.icon.setImageResource(R.drawable.icon_folder);
                } else {
                    downloadHolder.icon.setImageResource(R.drawable.icon_file);
                    if (dbDownload.hasThumbnail) {
                        mThumbnailDisplayer.display(downloadHolder.icon, dbDownload.path);
                    }
                }
                if (dbDownload.stringStatus != null) {
                    downloadHolder.status.setText(dbDownload.stringStatus);
                    downloadHolder.status.setVisibility(View.VISIBLE);
                } else {
                    downloadHolder.status.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDownloads.size() + 1;
    }

    public String getReadableTime(long time) {
        long currentTime = System.currentTimeMillis() / 1000;
        time /= 1000;
        if (time >= currentTime - 60) {
            return "A few seconds ago";
        }
        time /= 60;
        currentTime /= 60;
        if (time >= currentTime - 60) {
            long returnTime = (currentTime - time) % 60;
            return returnTime == 1 ? "1 minute ago" : returnTime + " minutes ago";
        }
        time /= 60;
        currentTime /= 60;
        if (time >= currentTime - 24) {
            long returnTime = (currentTime - time) % 60;
            return returnTime == 1 ? "1 hour ago" : returnTime + " hours ago";
        }
        time /= 24;
        currentTime /= 24;
        if (time >= currentTime - 30) {
            long returnTime = (currentTime - time) % 24;
            return returnTime == 1 ? returnTime + " day ago" : returnTime + " days ago";
        }
        time /= 30;
        currentTime /= 30;
        long returnTime = currentTime - time;
        return returnTime == 1 ? "1 month ago" : returnTime + " months ago";
    }


    private class HeaderHolder extends RecyclerView.ViewHolder {

        public TextView name, initials;
        public View settings;

        public HeaderHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.header_name);
            initials = (TextView) itemView.findViewById(R.id.header_initials);
            settings = itemView.findViewById(R.id.settings);
        }
    }
}
