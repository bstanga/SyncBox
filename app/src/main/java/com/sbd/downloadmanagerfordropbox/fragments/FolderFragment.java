package com.sbd.downloadmanagerfordropbox.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import com.cocosw.bottomsheet.BottomSheet;
import com.dropbox.client2.DropboxAPI;
import com.google.android.gms.analytics.HitBuilders;
import com.sbd.downloadmanagerfordropbox.App;
import com.sbd.downloadmanagerfordropbox.MainActivity;
import com.sbd.downloadmanagerfordropbox.R;
import com.sbd.downloadmanagerfordropbox.adapters.FilesAdapter;
import com.sbd.downloadmanagerfordropbox.interfaces.OnFileListener;
import com.sbd.downloadmanagerfordropbox.items.DBFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by StangaBogdan on 8/27/2015.
 */
public class FolderFragment extends Fragment implements OnFileListener {

    private MainActivity mActivity;
    private FilesAdapter mFilesAdapter;

    private RecyclerView mRecycler;
    private View mProgressView;

    private ArrayList<DBFile> mFiles = new ArrayList<>();

    private static final String PATH = "path";
    private static final String NAME = "name";

    public static FolderFragment newInstance() {
        return newInstance(null, "/");
    }

    public static FolderFragment newInstance(String name, String path) {
        Bundle b = new Bundle();
        b.putString(NAME, name);
        b.putString(PATH, path + "/");
        FolderFragment folderFragment = new FolderFragment();
        folderFragment.setArguments(b);
        return folderFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (MainActivity) activity;
        handleActionbar();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_folder, container, false);
        mRecycler = (RecyclerView) view.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mActivity);
        mRecycler.setLayoutManager(layoutManager);

        mProgressView = view.findViewById(R.id.folders_loading);

        printFiles(getArguments().getString(PATH));

        return view;
    }

    @Override
    public void onFileSelected(DBFile file) {
        if (file.isDir) {
            mActivity.addFragment(FolderFragment.newInstance(file.name, file.path));
        }
    }

    @Override
    public void onFileMoreSelected(final DBFile file) {
        new BottomSheet.Builder(mActivity).title(file.name).sheet(R.menu.menu_file).listener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case R.id.download:
                        ArrayList<DBFile> paths = new ArrayList<>();
                        paths.add(file);
                        mActivity.chooseLocationForDownloadingFiles(paths);
                        break;
                    case R.id.share:
                        final ProgressDialog progressDialog = new ProgressDialog(mActivity);
                        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        if (file.isDir) {
                            progressDialog.setMessage("Getting folder url");
                        } else {
                            progressDialog.setMessage("Getting file url");
                        }
                        progressDialog.show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    final DropboxAPI.DropboxLink dbLink = mActivity.getDBApi().share(file.path);
                                    mActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            App.init(mActivity).getDefaultTracker().send(new HitBuilders.EventBuilder()
                                                    .setCategory("Action")
                                                    .setAction("Share")
                                                    .setLabel("Link")
                                                    .build());
                                            progressDialog.dismiss();
                                            Intent i = new Intent(Intent.ACTION_SEND);
                                            i.setType("text/plain");
                                            i.putExtra(Intent.EXTRA_SUBJECT, file.name);
                                            i.putExtra(Intent.EXTRA_TEXT, dbLink.url);
                                            startActivity(Intent.createChooser(i, dbLink.url));
                                        }
                                    });

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    mActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.dismiss();
                                        }
                                    });
                                }
                            }
                        }).start();
                        break;
                }
            }
        }).grid().show();
    }

    private void printFiles(final String path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DropboxAPI.Entry entry = mActivity.getDBApi().metadata(path, 5000, null, true, null);
                    for (DropboxAPI.Entry e : entry.contents) {
                        DBFile dbFile = new DBFile();
                        dbFile.isDir = e.isDir;
                        dbFile.name = e.fileName();
                        dbFile.path = e.path;
                        dbFile.size = e.bytes;
                        dbFile.hasThumbnail = e.thumbExists;
                        mFiles.add(dbFile);
                    }

                    Collections.sort(mFiles, new Comparator<DBFile>() {
                        @Override
                        public int compare(DBFile file1, DBFile file2) {
                            if (file1.isDir && file2.isDir) {
                                return file1.name.compareToIgnoreCase(file2.name);
                            } else if (file1.isDir) {
                                return -1;
                            } else if (file2.isDir) {
                                return 1;
                            } else {
                                return file1.name.compareToIgnoreCase(file2.name);
                            }
                        }
                    });

                    if (mFiles.size() == 0) {
                        mFiles.add(new DBFile());
                    }

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressView.setVisibility(View.GONE);
                            mFilesAdapter = new FilesAdapter(mActivity, mFiles);
                            mFilesAdapter.setOnFileListener(FolderFragment.this);
                            mRecycler.setAdapter(mFilesAdapter);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressView.setVisibility(View.GONE);
                            Toast.makeText(mActivity, "Check your internet connection", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        handleActionbar();
    }

    private void handleActionbar() {
        String name = getArguments().getString(NAME);
        ActionBar actionBar = mActivity.getSupportActionBar();
        if (actionBar != null) {
            if (name == null) {
                actionBar.setSubtitle(null);
                actionBar.setTitle(mActivity.getResources().getString(R.string.app_name));
                mActivity.setDrawerEnabled(true);
            } else {
                String path = getArguments().getString(PATH);
                path = path.substring(0, path.length() - 1);
                path = path.substring(0, path.lastIndexOf("/") + 1);
                actionBar.setSubtitle(path);
                actionBar.setTitle(name);
                mActivity.setDrawerEnabled(false);
            }
        }
    }

}
