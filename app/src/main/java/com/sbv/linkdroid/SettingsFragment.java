package com.sbv.linkdroid;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {

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

        EditTextPreference tokenEditText = findPreference("AUTH_TOKEN");
        if (tokenEditText != null){
            tokenEditText.setSummaryProvider(new Preference.SummaryProvider<EditTextPreference>() {
                @Override
                public CharSequence provideSummary(@NonNull EditTextPreference preference) {
                    String token = preference.getText();
                    if (token == null || token.isEmpty()){
                        return "";
                    } else {
                        return "********";
                    }
                }
            });
        }
    }
}
