package com.sbd.downloadmanagerfordropbox.adapters;

import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.sbd.downloadmanagerfordropbox.MainActivity;
import com.sbd.downloadmanagerfordropbox.R;
import com.sbd.downloadmanagerfordropbox.helper.ThumbnailDisplayer;
import com.sbd.downloadmanagerfordropbox.holders.FileHolder;
import com.sbd.downloadmanagerfordropbox.interfaces.OnFileListener;
import com.sbd.downloadmanagerfordropbox.items.DBFile;

import java.util.ArrayList;

/**
 * Created by StangaBogdan on 8/27/2015.
 */
public class FilesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener,
        View.OnLongClickListener {

    private MainActivity mActivity;
    private ThumbnailDisplayer mThumbnailDisplayer;
    private ArrayList<DBFile> mFiles;
    private OnFileListener mOnFileListener;
    private ActionMode mActionMode;

    private ArrayList<DBFile> mSelectedFiles = new ArrayList<>();

    public FilesAdapter(MainActivity activity, ArrayList<DBFile> files) {
        mActivity = activity;
        mFiles = files;
        mThumbnailDisplayer = new ThumbnailDisplayer(activity);
    }

    public void setOnFileListener(OnFileListener onFileListener) {
        this.mOnFileListener = onFileListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        switch (i) {
            case 0:
                return new RecyclerView.ViewHolder(LayoutInflater.from(mActivity).inflate(R.layout.item_empty_download, viewGroup, false)) {
                    @Override
                    public String toString() {
                        return super.toString();
                    }
                };
            default:
                return new FileHolder(LayoutInflater.from(mActivity).inflate(R.layout.item_file, viewGroup, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mFiles.get(position).path != null ? 1 : 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
        DBFile dbFile = mFiles.get(i);
        if (dbFile.path != null) {
            FileHolder fileHolder = (FileHolder) holder;
            fileHolder.name.setText(dbFile.name);
            fileHolder.itemView.setSelected(mSelectedFiles.contains(dbFile));
            fileHolder.itemView.setTag(dbFile);
            fileHolder.itemView.setOnClickListener(this);
            fileHolder.itemView.setOnLongClickListener(this);
            if (dbFile.isDir) {
                fileHolder.icon.setImageResource(R.drawable.icon_folder);
            } else {
                fileHolder.icon.setImageResource(R.drawable.icon_file);
                if (dbFile.hasThumbnail) {
                    mThumbnailDisplayer.display(fileHolder.icon, dbFile.path);
                }
            }
            if (mActionMode == null) {
                fileHolder.selector.setVisibility(View.GONE);
                fileHolder.more.setVisibility(View.VISIBLE);
                fileHolder.more.setTag(dbFile);
                fileHolder.more.setOnClickListener(this);
            } else {
                fileHolder.more.setVisibility(View.GONE);
                fileHolder.selector.setVisibility(View.VISIBLE);
                fileHolder.selector.setChecked(mSelectedFiles.contains(dbFile));
                fileHolder.selector.setTag(dbFile);
                fileHolder.selector.setOnClickListener(mOnCheckBoxClickListener);
            }
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.file_more) {
            if (mOnFileListener != null) {
                mOnFileListener.onFileMoreSelected((DBFile) v.getTag());
            }
        } else {
            if (mActionMode != null) {
                if (v.getTag() == null) {
                    return;
                }
                DBFile dbFile = (DBFile) v.getTag();
                if (v.isSelected()) {
                    v.setSelected(false);
                    mSelectedFiles.remove(dbFile);
                    ((CheckBox) v.findViewById(R.id.file_selector)).setChecked(false);
                    if (mSelectedFiles.isEmpty()) {
                        if (mActionMode != null) {
                            mActionMode.finish();
                        }
                    }
                } else {
                    v.setSelected(true);
                    mSelectedFiles.add(dbFile);
                    ((CheckBox) v.findViewById(R.id.file_selector)).setChecked(true);
                }
                if (mActionMode != null) {
                    mActionMode.setTitle(mSelectedFiles.size() + " selected");
                }
            } else if (mOnFileListener != null) {
                mOnFileListener.onFileSelected((DBFile) v.getTag());
            }
        }
    }


    private View.OnClickListener mOnCheckBoxClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mActionMode != null) {
                DBFile dbFile = (DBFile) v.getTag();
                if (((CheckBox) v).isChecked()) {
                    if (mSelectedFiles.contains(dbFile)) {
                        return;
                    }
                    mSelectedFiles.add(dbFile);
                    ((View) v.getParent()).setSelected(true);
                } else {
                    if (!mSelectedFiles.contains(dbFile)) {
                        return;
                    }
                    mSelectedFiles.remove(dbFile);
                    ((View) v.getParent()).setSelected(false);
                    if (mSelectedFiles.isEmpty()) {
                        if (mActionMode != null) {
                            mActionMode.finish();
                        }
                    }
                }
                if (mActionMode != null) {
                    mActionMode.setTitle(mSelectedFiles.size() + " selected");
                }
            }
        }
    };

    @Override
    public boolean onLongClick(View v) {
        if (v.getTag() == null) {
            return false;
        }
        if (mActionMode != null) {
            return false;
        }
        DBFile dbFile = (DBFile) v.getTag();
        mActionMode = mActivity.startSupportActionMode(mActionModeCallback);
        v.setSelected(true);
        mSelectedFiles.add(dbFile);
        ((CheckBox) v.findViewById(R.id.file_selector)).setChecked(true);
        if (mActionMode != null) {
            mActionMode.setTitle(mSelectedFiles.size() + " selected");
        }
        notifyDataSetChanged();
        return true;
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_selected_files, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.download:
                    ArrayList<DBFile> files = new ArrayList<>();
                    files.addAll(mSelectedFiles);
                    mActivity.chooseLocationForDownloadingFiles(files);
                    break;
            }
            mActionMode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            if (mSelectedFiles.size() > 0) {
                mSelectedFiles.clear();
            }
            notifyDataSetChanged();
        }
    };

}
