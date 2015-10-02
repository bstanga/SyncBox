package com.sbd.downloadmanagerfordropbox.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.sbd.downloadmanagerfordropbox.MainActivity;
import com.sbd.downloadmanagerfordropbox.R;
import com.sbd.downloadmanagerfordropbox.adapters.LocalFoldersAdapter;
import com.sbd.downloadmanagerfordropbox.interfaces.OnLocalFolderListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by StangaBogdan on 8/28/2015.
 */
public class SelectLocationFragment extends Fragment implements OnLocalFolderListener, View.OnClickListener {

    private MainActivity mActivity;

    private RecyclerView mRecycler;
    private LinearLayoutManager mLinearLayoutManager;
    private LocalFoldersAdapter mFoldersAdapter;

    private ArrayList<File> mFolders = new ArrayList<>();
    private File mIteratedFile;

    public static SelectLocationFragment newInstance() {
        return new SelectLocationFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (MainActivity) activity;
        mIteratedFile = Environment.getExternalStorageDirectory();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_location, container, false);
        handleActionbar();

        mRecycler = (RecyclerView) view.findViewById(R.id.recycler_view);
        mLinearLayoutManager = new LinearLayoutManager(mActivity);
        mRecycler.setLayoutManager(mLinearLayoutManager);
        mFoldersAdapter = new LocalFoldersAdapter(mActivity, mFolders);
        mFoldersAdapter.setOnLocalFolderListener(this);
        mRecycler.setAdapter(mFoldersAdapter);

        view.findViewById(R.id.create_folder).setOnClickListener(this);
        view.findViewById(R.id.select_folder).setOnClickListener(this);

        iterateFolder(mIteratedFile);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select_folder:
                mActivity.downloadFiles(mIteratedFile);
                mActivity.getSupportFragmentManager().popBackStack();
                break;
            case R.id.create_folder:
                AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);
                alert.setTitle(mActivity.getResources().getString(R.string.create_folder));
                final EditText et = new EditText(mActivity);
                alert.setView(et);
                et.setHint(mActivity.getResources().getString(R.string.folder_name));
                alert.setNegativeButton(mActivity.getResources().getString(R.string.cancel), null);
                alert.setPositiveButton(mActivity.getResources().getString(R.string.done), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String folderName = et.getText().toString();
                        if (folderName.equals("")) {
                            Toast.makeText(mActivity, "Invalid name", Toast.LENGTH_LONG).show();
                        }
                        File file = new File(mIteratedFile, folderName);
                        try {
                            if (file.mkdirs()) {
                                iterateFolder(mIteratedFile, false);
                                Toast.makeText(mActivity, "Folder created", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(mActivity, "Invalid name", Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(mActivity, "Invalid name", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                alert.show();
                break;
        }
    }

    private void iterateFolder(File file) {
        iterateFolder(file, true);
    }

    private void iterateFolder(File file, boolean scrollToTop) {
        mIteratedFile = file;

        mActivity.getSupportActionBar().setSubtitle(getPathToShow());
        List<File> files = Arrays.asList(file.listFiles());
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        });
        mFolders.clear();
        if (!file.getAbsolutePath().equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
            mFolders.add(null);
        }
        for (File f : files) {
            if (f.isDirectory()) {
                mFolders.add(f);
            }
        }
        mFolders.add(null);

        mFoldersAdapter.notifyDataSetChanged();
        if (scrollToTop) {
            mLinearLayoutManager.scrollToPosition(0);
        }
    }

    @Override
    public void onFolderSelected(File file) {
        iterateFolder(file);
    }

    @Override
    public void onFolderReturned() {
        iterateFolder(mIteratedFile.getParentFile());
    }

    private void handleActionbar() {
        mActivity.getSupportActionBar().setSubtitle(getPathToShow());
        mActivity.getSupportActionBar().setTitle(mActivity.getResources().getString(R.string.select_folder));
        mActivity.setDrawerEnabled(false);
    }

    private String getPathToShow() {
        String path = mIteratedFile.getPath();
        path = path.replace(Environment.getExternalStorageDirectory().getAbsolutePath(), "");
        if (path.equals("")) {
            path = "/";
        }
        return path;
    }

    @Override
    public void onResume() {
        super.onResume();
        handleActionbar();
    }

}
