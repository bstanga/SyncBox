package com.sbd.downloadmanagerfordropbox.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sbd.downloadmanagerfordropbox.MainActivity;
import com.sbd.downloadmanagerfordropbox.R;
import com.sbd.downloadmanagerfordropbox.holders.FolderHolder;
import com.sbd.downloadmanagerfordropbox.interfaces.OnLocalFolderListener;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by StangaBogdan on 8/28/2015.
 */
public class LocalFoldersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    private MainActivity mActivity;
    private OnLocalFolderListener mLocalFolderListener;
    private ArrayList<File> mFolders;

    private static final int FOLDER = 1;
    private static final int EMPTY = 0;
    private static final int RETURN_FOLDER = 2;

    public LocalFoldersAdapter(MainActivity activity, ArrayList<File> folders) {
        mActivity = activity;
        mFolders = folders;
    }

    public void setOnLocalFolderListener(OnLocalFolderListener onLocalFolderListener) {
        this.mLocalFolderListener = onLocalFolderListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (i == RETURN_FOLDER) {
            return new RecyclerView.ViewHolder(LayoutInflater.from(mActivity).inflate(R.layout.item_previous_folder, viewGroup, false)) {
                @Override
                public String toString() {
                    return super.toString();
                }
            };
        } else if (i == EMPTY) {
            return new RecyclerView.ViewHolder(LayoutInflater.from(mActivity).inflate(R.layout.item_empty, viewGroup, false)) {
                @Override
                public String toString() {
                    return super.toString();
                }
            };
        } else {
            return new FolderHolder(LayoutInflater.from(mActivity).inflate(R.layout.item_folder, viewGroup, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && mFolders.get(0) == null) {
            return RETURN_FOLDER;
        } else if (position == mFolders.size() - 1) {
            return EMPTY;
        } else {
            return FOLDER;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
        if (getItemViewType(i) == FOLDER) {
            FolderHolder folderHolder = (FolderHolder) holder;
            File folder = mFolders.get(i);
            folderHolder.name.setText(folder.getName());
            folderHolder.itemView.setTag(folder);
            folderHolder.itemView.setOnClickListener(this);
        } else if (getItemViewType(i) == RETURN_FOLDER) {
            holder.itemView.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() == null) {
            if (mLocalFolderListener != null) {
                mLocalFolderListener.onFolderReturned();
            }
        } else {
            if (mLocalFolderListener != null) {
                mLocalFolderListener.onFolderSelected((File) v.getTag());
            }
        }
    }

    @Override
    public int getItemCount() {
        return mFolders.size();
    }
}
