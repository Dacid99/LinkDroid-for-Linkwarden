package com.sbv.linkdroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.preference.DropDownPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.sbv.linkdroid.api.APICallback;
import com.sbv.linkdroid.api.CollectionsRequest;
import com.sbv.linkdroid.api.LinkwardenAPIHandler;
import com.sbv.linkdroid.api.TagsRequest;
import com.sbv.linkdroid.database.CollectionEntity;
import com.sbv.linkdroid.database.TagEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SettingsFragment extends PreferenceFragmentCompat implements APICallback {

    private static final String TAG = "SettingsFragment";
    public static final String DEFAULT_COLLECTION_PREFERENCE_KEY = "COLLECTION_DEFAULT";
    public static final String BASE_URL_PREFERENCE_KEY = "BASE_URL";
    public static final String AUTH_TOKEN_PREFERENCE_KEY = "AUTH_TOKEN";
    public static final String DEFAULT_TAGS_PREFERENCE_KEY = "DEFAULT_TAGS";
    
    private MultiSelectListPreference defaultTagsPreference;
    private List<String> allTags = new ArrayList<>();
    private LinkwardenAPIHandler linkwardenAPIHandler;
    private DropDownPreference defaultCollectionPreference;
    private View rootView;

    private SpannableString getColoredText(String text) {
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(
            new ForegroundColorSpan(
                androidx.core.content.ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            ),
            0,
            text.length(),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        return spannableString;
    }
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        addPreferencesFromResource(R.xml.help_preferences);
        linkwardenAPIHandler = new LinkwardenAPIHandler(requireContext(), this);

        // Style all preference and category titles to light blue
        PreferenceScreen screen = getPreferenceScreen();
        for (int i = 0; i < screen.getPreferenceCount(); i++) {
            Preference preference = screen.getPreference(i);
            
            // Handle PreferenceCategory
            if (preference instanceof PreferenceCategory) {
                PreferenceCategory category = (PreferenceCategory) preference;
                CharSequence title = category.getTitle();
                if (title != null && !title.toString().isEmpty()) {
                    category.setTitle(getColoredText(title.toString()));
                }
                
                // Style preferences within the category
                for (int j = 0; j < category.getPreferenceCount(); j++) {
                    Preference childPreference = category.getPreference(j);
                    CharSequence childTitle = childPreference.getTitle();
                    if (childTitle != null && !childTitle.toString().isEmpty()) {
                        childPreference.setTitle(getColoredText(childTitle.toString()));
                    }
                }
            } else {
                // Handle regular preferences
                CharSequence title = preference.getTitle();
                if (title != null && !title.toString().isEmpty()) {
                    preference.setTitle(getColoredText(title.toString()));
                }
            }
        }

        // Set up summaries with light blue color
        SwitchPreference nameRequired = findPreference("NAME_REQUIRED");
        if (nameRequired != null) {
            nameRequired.setSummary(getColoredText(getString(R.string.name_required_summary)));
        }

        // Set up default collection preference
        defaultCollectionPreference = findPreference(DEFAULT_COLLECTION_PREFERENCE_KEY);
        if (defaultCollectionPreference != null) {
            defaultCollectionPreference.setSummaryProvider(preference -> 
                getColoredText(((DropDownPreference) preference).getValue())
            );
        }

        // Set up base URL preference
        EditTextPreference baseURLEditText = findPreference(BASE_URL_PREFERENCE_KEY);
        if (baseURLEditText != null) {
            baseURLEditText.setSummaryProvider(preference -> {
                String value = ((EditTextPreference) preference).getText();
                return getColoredText(value == null ? "" : value);
            });

            baseURLEditText.setOnPreferenceChangeListener((preference, newValue) -> {
                String newValueString = (String) newValue;
                if (newValueString.endsWith("/")) {
                    newValueString = newValueString.substring(0, newValueString.length() - 1);
                    baseURLEditText.setText(newValueString);
                }
                if (!newValueString.startsWith("http://") && !newValueString.startsWith("https://")) {
                    showErrorMessage("URL must start with either http(s)://");
                    return false;
                }
                return true;
            });
        }

        // Set up auth token preference
        EditTextPreference tokenEditText = findPreference(AUTH_TOKEN_PREFERENCE_KEY);
        if (tokenEditText != null) {
            tokenEditText.setSummaryProvider(preference -> {
                String token = ((EditTextPreference) preference).getText();
                return getColoredText(token == null || token.isEmpty() ? "" : "********");
            });
        }

        // Set up default tags preference
        defaultTagsPreference = findPreference(DEFAULT_TAGS_PREFERENCE_KEY);
        if (defaultTagsPreference != null) {
            defaultTagsPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                Set<String> selectedTags = (Set<String>) newValue;
                SharedPreferences.Editor editor = PreferenceManager
                    .getDefaultSharedPreferences(requireContext())
                    .edit();
                editor.putStringSet(DEFAULT_TAGS_PREFERENCE_KEY, selectedTags).apply();
                return true;
            });
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = view;  // Store reference to root view for Snackbar

        // Now that view is created, it's safe to make API calls
        if (defaultTagsPreference != null) {
            linkwardenAPIHandler.makeTagsRequest();

            if (defaultCollectionPreference.getEntryValues() == null ||
                defaultCollectionPreference.getEntryValues().length == 0) {
                   linkwardenAPIHandler.makeCollectionsRequest();
            }
        }

        // Set up GitHub link preference
        Preference githubLink = findPreference("GITHUB_LINK");
        if (githubLink != null) {
            githubLink.setOnPreferenceClickListener(preference -> {
                try {
                    String url = "https://github.com/Dacid99/LinkDroid-for-Linkwarden/issues";
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    
                    startActivity(intent);
                    return true;
                } catch (Exception e) {
                    Log.e(TAG, "Error opening URL: " + e.getMessage());
                    showErrorMessage("Unable to open GitHub: " + e.getMessage());
                    return false;
                }
            });
        }

        // Set up debug log preference
        Preference debugLog = findPreference("DEBUG_LOG");
        if (debugLog != null) {
            debugLog.setOnPreferenceClickListener(preference -> {
                shareDebugLog();
                return true;
            });
        }
    }

    private void showErrorMessage(String message) {
        if (rootView != null && isAdded()) {
            Snackbar.make(rootView, getColoredText(message), Snackbar.LENGTH_LONG).show();
        } else {
            // Fallback to Toast if view is not available
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void shareDebugLog() {
        try {
            DebugLogUtils.shareDebugLog(requireActivity());
        } catch (Exception e) {
            showErrorMessage(getString(R.string.share_error));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rootView = null;  // Clean up view reference
    }

    @Override
    public void onSuccessfulCollectionsRequest(List<CollectionEntity> collectionsList) {
        if (!isAdded()) return;  // Check if fragment is still attached

        requireActivity().runOnUiThread(() -> {
            try {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
                String defaultCollection = preferences.getString(DEFAULT_COLLECTION_PREFERENCE_KEY, null);
                if (defaultCollection != null) {
                    CollectionEntity defaultCollectionData = new CollectionEntity();
                    defaultCollectionData.setName(defaultCollection);
                    int defaultCollectionIndex = collectionsList.indexOf(defaultCollectionData);
                    if (defaultCollectionIndex != -1) {
                        Collections.swap(collectionsList, defaultCollectionIndex, 0);
                    }
                }

                String[] collectionsStringArray = new String[collectionsList.size()];
                for (int i = 0; i < collectionsList.size(); i++) {
                    collectionsStringArray[i] = collectionsList.get(i).toString();
                }

                if (defaultCollectionPreference != null) {
                    defaultCollectionPreference.setEntries(collectionsStringArray);
                    defaultCollectionPreference.setEntryValues(collectionsStringArray);
                    defaultCollectionPreference.setValue(collectionsStringArray[0]);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing collections", e);
                showErrorMessage("Error setting up collections: " + e.getMessage());
            }
        });
    }

    @Override
    public void onFailedCollectionsRequest(String error) {
        if (!isAdded()) return;  // Check if fragment is still attached

        requireActivity().runOnUiThread(() -> showErrorMessage(error));
    }

    @Override
    public void onSuccessfulTagsRequest(List<TagEntity> tagsList) {
        if (!isAdded()) return;  // Check if fragment is still attached

        requireActivity().runOnUiThread(() -> {
            try {
                allTags = tagsList.stream()
                    .map(TagEntity::getName)
                    .collect(Collectors.toList());
                
                if (defaultTagsPreference != null) {
                    defaultTagsPreference.setEntries(allTags.toArray(new String[0]));
                    defaultTagsPreference.setEntryValues(allTags.toArray(new String[0]));

                    // Restore previously selected tags
                    Set<String> selectedTags = PreferenceManager
                        .getDefaultSharedPreferences(requireContext())
                        .getStringSet(DEFAULT_TAGS_PREFERENCE_KEY, new HashSet<>());
                    defaultTagsPreference.setValues(selectedTags);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing tags", e);
                showErrorMessage("Error setting up tags: " + e.getMessage());
            }
        });
    }

    @Override
    public void onAuthFailed(String error) {
        if (!isAdded()) return;  // Check if fragment is still attached

        requireActivity().runOnUiThread(() -> showErrorMessage("Authentication failed: " + error));
    }

    @Override
    public void onFailedTagsRequest(String error) {
        if (!isAdded()) return;  // Check if fragment is still attached

        requireActivity().runOnUiThread(() -> showErrorMessage(error));
    }

    @Override
    public void onSuccessfulShareRequest() {
        // Not used in settings fragment
    }

    @Override
    public void onFailedShareRequest(String error) {
        // Not used in settings fragment
    }
}