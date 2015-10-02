package com.sbd.downloadmanagerfordropbox;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;

/**
 * Created by StangaBogdan on 9/2/2015.
 */
public class SettingsActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.app_color));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Settings");
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        CheckBox cb = (CheckBox) findViewById(R.id.settings_wifi);
        cb.setChecked(mPrefs.getBoolean("only_wifi", true));
        cb.setOnCheckedChangeListener(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mPrefs.edit().putBoolean("only_wifi", isChecked).apply();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
