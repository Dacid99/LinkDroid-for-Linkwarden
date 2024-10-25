package com.sbv.linkdroid;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.DropDownPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.sbv.linkdroid.api.APICallback;
import com.sbv.linkdroid.api.CollectionsRequest;
import com.sbv.linkdroid.api.LinkwardenAPIHandler;
import com.sbv.linkdroid.api.TagsRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SettingsFragment extends PreferenceFragmentCompat implements APICallback {

    public static final String DEFAULT_COLLECTION_PREFERENCE_KEY = "COLLECTION_DEFAULT";
    public static final String BASE_URL_PREFERENCE_KEY = "CATEGORY_DEFAULT";
    public static final String AUTH_TOKEN_PREFERENCE_KEY = "AUTH_TOKEN";

    private DropDownPreference defaultCollectionPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        LinkwardenAPIHandler linkwardenAPIHandler = new LinkwardenAPIHandler(requireContext(), this);

        defaultCollectionPreference = findPreference(DEFAULT_COLLECTION_PREFERENCE_KEY);
        if (defaultCollectionPreference != null){
            defaultCollectionPreference.setSummaryProvider(preference -> ((DropDownPreference) preference).getValue());
            linkwardenAPIHandler.makeCollectionsRequest();
//            defaultCollectionPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//                @Override
//                public boolean onPreferenceClick(@NonNull Preference preference) {
//                    if (defaultCollectionPreference.getEntryValues() == null || defaultCollectionPreference.getEntryValues().length == 0 ) {
//
//                    }
//                    return false;
//                }
//            });
        }

        EditTextPreference baseURLEditText = findPreference(BASE_URL_PREFERENCE_KEY);
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

        EditTextPreference tokenEditText = findPreference(AUTH_TOKEN_PREFERENCE_KEY);
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

    @Override
    public void onSuccessfulCollectionsRequest(List<CollectionsRequest.CollectionData> collectionsList) {
        requireActivity().runOnUiThread(() -> {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
            String defaultCollection = preferences.getString(SettingsFragment.DEFAULT_COLLECTION_PREFERENCE_KEY, null);
            if (defaultCollection != null){

                CollectionsRequest.CollectionData defaultCollectionData = new CollectionsRequest.CollectionData();
                defaultCollectionData.setName(defaultCollection);
                int defaultCollectionIndex = collectionsList.indexOf(defaultCollectionData);
                if (defaultCollectionIndex != -1){
                    Collections.swap(collectionsList, defaultCollectionIndex, 0);
                }
            }
            String[] collectionsStringArray = new String[collectionsList.size()];
            for (int i = 0; i < collectionsList.size(); i++){
                collectionsStringArray[i] = collectionsList.get(i).toString();
            }

            defaultCollectionPreference.setEntries(collectionsStringArray);
            defaultCollectionPreference.setEntryValues(collectionsStringArray);

            defaultCollectionPreference.setValue(collectionsStringArray[0]);
        });
    }

    @Override
    public void onFailedCollectionsRequest(String error) {
        requireActivity().runOnUiThread(() -> Toast.makeText(requireActivity().getApplicationContext(), error , Toast.LENGTH_LONG).show());
    }

    @Override
    public void onAuthFailed(String error) {
        requireActivity().runOnUiThread(() -> Toast.makeText(requireActivity().getApplicationContext(), error , Toast.LENGTH_LONG).show());
    }


    @Override
    public void onSuccessfulShareRequest() { }

    @Override
    public void onFailedShareRequest(String error) { }

    @Override
    public void onSuccessfulTagsRequest(List<TagsRequest.TagData> collectionsList) { }

    @Override
    public void onFailedTagsRequest(String error) { }

}

