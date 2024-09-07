package com.linkwarden.android;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;


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

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            EditTextPreference baseURLEditText = findPreference("BASE_URL");
            if (baseURLEditText != null) {
                baseURLEditText.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                        String newValueString = (String) newValue;
                        if (newValueString.endsWith("/")) {
                            newValueString = newValueString.substring(0, newValueString.length() - 1);
                            baseURLEditText.setText(newValueString);
                        }
                        if (!newValueString.startsWith("http://") && !newValueString.startsWith("https://")) {
                            Toast.makeText(getContext(), "URL must start with either http(s)://", Toast.LENGTH_LONG).show();
                            return false;
                        }
                        return true;
                    }
                });
            }
        }
    }
}