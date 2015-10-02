package com.sbd.downloadmanagerfordropbox;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.sbd.downloadmanagerfordropbox.adapters.DownloadsAdapter;
import com.sbd.downloadmanagerfordropbox.fragments.FolderFragment;
import com.sbd.downloadmanagerfordropbox.fragments.SelectLocationFragment;
import com.sbd.downloadmanagerfordropbox.interfaces.DownloadListener;
import com.sbd.downloadmanagerfordropbox.items.DBDownload;
import com.sbd.downloadmanagerfordropbox.items.DBFile;
import com.sbd.downloadmanagerfordropbox.network.DownloadManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements DownloadListener, FragmentManager.OnBackStackChangedListener {

    private DropboxAPI<AndroidAuthSession> mDropBoxApi;
    private App mApp;
    private Db mDb;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private FragmentManager mFragmentManager;
    private DownloadsAdapter mDownloadsAdapter;
    private Tracker mTracker;

    private ArrayList<DBFile> mDBFilesToDownload;
    private ArrayList<DBDownload> mDownloads = new ArrayList<>();

    final static private int FRAGMENT_CONTAINER = R.id.fragment_container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mApp = App.init(this);
        mDb = Db.init(this);

        mTracker = mApp.getDefaultTracker();
        mTracker.setScreenName("MainActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.app_color));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        AndroidAuthSession session = mApp.getDBAuthSession();
        String dbToken = mApp.getDBToken();
        if (dbToken == null) {
            mDropBoxApi = new DropboxAPI<>(session);
            mDropBoxApi.getSession().startOAuth2Authentication(this);
        } else {
            session.setOAuth2AccessToken(dbToken);
            mDropBoxApi = new DropboxAPI<>(session);
            setUI();
        }
    }

    private void setUI() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getString("name", null) == null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        prefs.edit().putString("name", mDropBoxApi.accountInfo().displayName).commit();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mDownloadsAdapter != null) {
                                    mDownloadsAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        //App UI
        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.addOnBackStackChangedListener(this);
        mFragmentManager.beginTransaction().add(FRAGMENT_CONTAINER, FolderFragment.newInstance()).commit();
        //Sidemenu Recycler
        RecyclerView recycler = (RecyclerView) findViewById(R.id.sidemenu_recycler);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);
        mDownloadsAdapter = new DownloadsAdapter(this, mDownloads);
        recycler.setAdapter(mDownloadsAdapter);
        new Thread(new Runnable() {
            @Override
            public void run() {
                setDownloads();
            }
        }).start();
        com.sbd.downloadmanagerfordropbox.helper.DownloadListener.init().setOnDownloadListener(this);
    }

    private void setDownloads() {
        ArrayList<DBDownload> downloads = mDb.getHistoryDownloads();
        Collections.sort(downloads, new Comparator<DBDownload>() {
            @Override
            public int compare(DBDownload lhs, DBDownload rhs) {
                if (lhs.status == 0 && rhs.status == 1) {
                    return -1;
                } else if (lhs.status == 1 && rhs.status == 0) {
                    return 1;
                } else {
                    return lhs.timestamp >= rhs.timestamp ? -1 : 1;
                }
            }
        });
        if (downloads.size() == 0) {
            downloads.add(new DBDownload());
        } else {
            if (downloads.get(0).status == 0) {
                downloads.get(0).stringStatus = "PENDING";
                for (int i = 1; i < downloads.size(); i++) {
                    if (downloads.get(i).status == 1) {
                        downloads.get(i).stringStatus = "DOWNLOADED";
                        break;
                    }
                }
            } else {
                downloads.get(0).stringStatus = "DOWNLOADED";
            }
        }
        mDownloads.clear();
        mDownloads.addAll(downloads);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDownloadsAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void refreshDownloads() {
        if (mDownloadsAdapter != null) {
            setDownloads();
        }
    }

    public void addFragment(Fragment fragment) {
        mFragmentManager.beginTransaction().setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit, R.anim.pop_enter, R.anim.pop_exit)
                .add(MainActivity.FRAGMENT_CONTAINER, fragment).addToBackStack(null).commit();
    }

    public void chooseLocationForDownloadingFiles(ArrayList<DBFile> files) {
        mDBFilesToDownload = new ArrayList<>(files);
        addFragment(SelectLocationFragment.newInstance());
    }

    public void downloadFiles(final File location) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < mDBFilesToDownload.size(); i++) {
                    DBFile dbFile = mDBFilesToDownload.get(i);
                    if (dbFile.isDir) {
                        mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory("Action")
                                .setAction("Download")
                                .setLabel("Folder")
                                .build());
                    } else {
                        mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory("Action")
                                .setAction("Download")
                                .setLabel("File")
                                .build());
                    }
                    mDb.addDownload(dbFile, location.getAbsolutePath());
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DownloadManager.init(MainActivity.this).start();
                    }
                });
                setDownloads();
            }
        }).start();
    }

    public DropboxAPI<AndroidAuthSession> getDBApi() {
        return mDropBoxApi;
    }

    public void setDrawerEnabled(boolean enabled) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerToggle.setDrawerIndicatorEnabled(enabled);
        mDrawerLayout.setDrawerLockMode(enabled ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public void onBackStackChanged() {
        Fragment fragment = mFragmentManager.findFragmentById(FRAGMENT_CONTAINER);
        if (fragment != null) {
            fragment.onResume();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        if (item.getItemId() == android.R.id.home) {
            mFragmentManager.popBackStack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onResume() {
        super.onResume();
        if (mDropBoxApi.getSession().authenticationSuccessful()) {
            try {
                mDropBoxApi.getSession().finishAuthentication();
                mApp.setDBToken(mDropBoxApi.getSession().getOAuth2AccessToken());
                setUI();
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }
    }

}
