package com.sbv.linkdroid;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar toolbar = getSupportActionBar();
        if (toolbar != null){
            toolbar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){

        if (menuItem.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }
}