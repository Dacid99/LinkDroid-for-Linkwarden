package com.sbv.linkdroid;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.sbv.linkdroid.api.APICallback;
import com.sbv.linkdroid.api.CollectionsRequest;
import com.sbv.linkdroid.api.LinkwardenAPIHandler;
import com.sbv.linkdroid.api.TagsRequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ShareReceiverActivity extends AppCompatActivity implements APICallback {
    private static final String TAG = "ShareReceiverActivity";
    private LinkwardenAPIHandler linkwardenAPIHandler;
    private AlertDialog dialog;
    private MaterialAutoCompleteTextView collectionsDropdown;
    private AutoCompleteTextView tagsInput;
    private ChipGroup tagsList;
    private TagAdapter tagsAdapter;
    private List<CollectionsRequest.CollectionData> collectionsList = new ArrayList<>();

    // Inner class for handling tags
    private static class TagAdapter extends ArrayAdapter<TagsRequest.TagData> implements Filterable {
        private final List<TagsRequest.TagData> allTags;
        private List<TagsRequest.TagData> filteredTags;

        public TagAdapter(Context context) {
            super(context, 0);
            allTags = new ArrayList<>();
            filteredTags = new ArrayList<>();
        }

        public void updateTags(List<TagsRequest.TagData> newTags) {
            allTags.clear();
            allTags.addAll(newTags);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return filteredTags.size();
        }

        @Nullable
        @Override
        public TagsRequest.TagData getItem(int position) {
            return position >= 0 && position < filteredTags.size() ? filteredTags.get(position) : null;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // Inflate the view for each tag item in the dropdown
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.dropdown_menu_popup_item, parent, false);
            }

            TextView textView = (TextView) convertView;
            TagsRequest.TagData tag = getItem(position);
            if (tag != null) {
                textView.setText(tag.getName());
                textView.setPadding(32, 32, 32, 32);
            }

            return convertView;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    List<TagsRequest.TagData> tempFilteredTags = new ArrayList<>();

                    if (constraint == null || constraint.length() == 0) {
                        tempFilteredTags.addAll(allTags);
                    } else {
                        String filterPattern = constraint.toString().toLowerCase().trim();
                        for (TagsRequest.TagData tag : allTags) {
                            if (tag.getName().toLowerCase().contains(filterPattern)) {
                                tempFilteredTags.add(tag);
                            }
                        }
                    }

                    results.values = tempFilteredTags;
                    results.count = tempFilteredTags.size();
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results.values instanceof List<?>) {
                        filteredTags = (List<TagsRequest.TagData>) results.values;
                        notifyDataSetChanged();
                    }
                }
            };
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");

        // Load the base URL and auth token from SharedPreferences
        SharedPreferences prefs = getDefaultSharedPreferences(this);
        String baseUrl = prefs.getString("BASE_URL", "");
        String authToken = prefs.getString("AUTH_TOKEN", "");

        if (baseUrl.isEmpty()) {
            Toast.makeText(this, "Please configure the base URL in settings", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        linkwardenAPIHandler = new LinkwardenAPIHandler(this, this);

        if (getIntent() != null) {
            handleIntent(getIntent());
        } else {
            Log.e(TAG, "No intent received");
            finish();
        }
    }

    private void setupTagsInput(View dialogView) {
        tagsInput = dialogView.findViewById(R.id.tagsInput);
        tagsList = dialogView.findViewById(R.id.tagsList);
        
        tagsAdapter = new TagAdapter(this);
        tagsInput.setAdapter(tagsAdapter);

        tagsInput.post(() -> {
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            int offset = -(screenHeight / 3);
            tagsInput.setDropDownVerticalOffset(offset);
            tagsInput.setDropDownWidth(tagsInput.getWidth());
        });

        tagsInput.setOnItemClickListener((parent, view, position, id) -> {
            TagsRequest.TagData selectedTag = tagsAdapter.getItem(position);
            if (selectedTag != null) {
                addTagChip(selectedTag.getName());
                tagsInput.setText("");
            }
        });

        tagsInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String newTag = tagsInput.getText().toString().trim();
                if (!newTag.isEmpty()) {
                    addTagChip(newTag);
                    tagsInput.setText("");
                }
                return true;
            }
            return false;
        });

        linkwardenAPIHandler.makeTagsRequest();
    }

    private void handleIntent(@NonNull Intent intent) {
        Log.d(TAG, "handleIntent called with action: " + intent.getAction());
        String sharedText = null;
    
        if (Intent.ACTION_SEND.equals(intent.getAction()) && intent.getType() != null) {
            if ("text/plain".equals(intent.getType())) {
                sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                Log.d(TAG, "Received shared text: " + sharedText);
            }
        } else if (Intent.ACTION_PROCESS_TEXT.equals(intent.getAction())) {
            CharSequence processedText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
            if (processedText != null) {
                sharedText = processedText.toString();
                Log.d(TAG, "Received processed text: " + sharedText);
            }
        }
    
        if (sharedText == null || sharedText.isEmpty()) {
            Log.e(TAG, "No valid text received");
            Toast.makeText(this, "No valid text received", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    
        sharedText = extractUrl(sharedText);
        showDialog(sharedText);
    }
    
    private String extractUrl(String text) {
        String[] words = text.split("\\s+");
        for (String word : words) {
            if (word.toLowerCase().startsWith("http://") || 
                word.toLowerCase().startsWith("https://")) {
                return word;
            }
        }
        return text;
    }
    
    private void showDialog(String sharedText) {
        try {
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog, null);
    
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setView(dialogView);
    
            EditText sharedTextEdit = dialogView.findViewById(R.id.sharedTextEdit);
            sharedTextEdit.setText(sharedText.trim());
    
            EditText nameEdit = dialogView.findViewById(R.id.nameEdit);
            String title = getIntent().getStringExtra(Intent.EXTRA_SUBJECT);
            if (title != null && !title.isEmpty()) {
                nameEdit.setText(title);
            }
    
            EditText descriptionEdit = dialogView.findViewById(R.id.descriptionEdit);
    
            setupTagsInput(dialogView);
    
            collectionsDropdown = dialogView.findViewById(R.id.collectionsDropdown);
            linkwardenAPIHandler.makeCollectionsRequest();
    
            MaterialButton sendButton = dialogView.findViewById(R.id.sendButton);
            sendButton.setOnClickListener(v -> handleSendButtonClick(sharedTextEdit, nameEdit, descriptionEdit));
    
            dialogBuilder.setOnDismissListener(v -> finish());
    
            dialog = dialogBuilder.create();
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            Set<String> defaultTags = prefs.getStringSet(SettingsFragment.DEFAULT_TAGS_PREFERENCE_KEY, new HashSet<>());
            for (String tagName : defaultTags) {
                addTagChip(tagName);
            }

            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing dialog", e);
            Toast.makeText(this, "Error showing dialog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    // Modify the handleSendButtonClick method to add better validation and logging
    private void handleSendButtonClick(EditText sharedTextEdit, EditText nameEdit, EditText descriptionEdit) {
        try {
            // Get and validate the shared text
            String editedSharedText = sharedTextEdit.getText().toString().trim();
            if (editedSharedText.isEmpty()) {
                Toast.makeText(getApplicationContext(), "URL cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get and log selected collection
            String selectedCollectionName = collectionsDropdown.getText().toString();
            Log.d(TAG, "Selected collection name: " + selectedCollectionName);
            
            CollectionsRequest.CollectionData selectedCollection = null;
            for (CollectionsRequest.CollectionData collection : collectionsList) {
                if (collection.getFullName().equals(selectedCollectionName)) {
                    selectedCollection = collection;
                    break;
                }
            }

            if (selectedCollection == null) {
                Toast.makeText(getApplicationContext(), "Please select a valid collection", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "No collection selected or collection not found in list");
                return;
            }

            Log.d(TAG, "Selected collection ID: " + selectedCollection.getId());

            // Get and validate the name
            String name = nameEdit.getText().toString().trim();
            if (getDefaultSharedPreferences(this).getBoolean("NAME_REQUIRED", false) && name.isBlank()) {
                Toast.makeText(getApplicationContext(), getString(R.string.name_required_warning), 
                    Toast.LENGTH_SHORT).show();
                return;
            }

            // Get description
            String description = descriptionEdit.getText().toString().trim();

            // Get and validate tags
            List<TagsRequest.TagData> selectedTags = new ArrayList<>();
            for (int i = 0; i < tagsList.getChildCount(); i++) {
                View tagChip = tagsList.getChildAt(i);
                if (tagChip instanceof Chip) {
                    TagsRequest.TagData selectedTagData = new TagsRequest.TagData();
                    String tag = ((Chip) tagChip).getText().toString();
                    selectedTagData.setName(tag);
                    selectedTags.add(selectedTagData);
                }
            }

            // Log the request details
            Log.d(TAG, "Making link request with:" +
                "\nURL: " + editedSharedText +
                "\nCollection: " + selectedCollection.getName() + " (ID: " + selectedCollection.getId() + ")" +
                "\nName: " + name +
                "\nDescription: " + description +
                "\nTags count: " + selectedTags.size());

            // Make the API request
            linkwardenAPIHandler.makePostLinkRequest(
                editedSharedText, 
                selectedCollection, 
                name, 
                description, 
                selectedTags
            );

        } catch (Exception e) {
            Log.e(TAG, "Error in handleSendButtonClick", e);
            Toast.makeText(getApplicationContext(), 
                "Error preparing link: " + e.getMessage(), 
                Toast.LENGTH_LONG).show();
        }
    }
    
    private void addTagChip(String tagName) {
        for (int i = 0; i < tagsList.getChildCount(); i++) {
            Chip chip = (Chip) tagsList.getChildAt(i);
            if (chip.getText().toString().equals(tagName)) {
                return;
            }
        }
    
        Chip chip = new Chip(this);
        chip.setText(tagName);
        chip.setCloseIconVisible(true);
        chip.setClickable(false);
        chip.setCheckable(false);
        
        chip.setChipBackgroundColor(ColorStateList.valueOf(
            androidx.core.content.ContextCompat.getColor(this, R.color.colorSurface)));
        chip.setTextColor(ColorStateList.valueOf(
            androidx.core.content.ContextCompat.getColor(this, R.color.textColorPrimary)));
        chip.setCloseIconTint(ColorStateList.valueOf(
            androidx.core.content.ContextCompat.getColor(this, R.color.textColorPrimary)));

        chip.setOnCloseIconClickListener(v -> tagsList.removeView(chip));
        tagsList.addView(chip);
    
        chip.post(() -> {
            if (tagsList.getParent() instanceof ScrollView) {
                ((ScrollView) tagsList.getParent()).fullScroll(View.FOCUS_DOWN);
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy called");
        if (dialog != null && dialog.isShowing()) {
            try {
                dialog.dismiss();
            } catch (Exception e) {
                Log.e(TAG, "Error dismissing dialog", e);
            }
        }
        super.onDestroy();
    }
    
    @Override
    public void onSuccessfulShareRequest() {
        Log.d(TAG, "Share request successful");
        runOnUiThread(() -> {
            Toast.makeText(getApplicationContext(), 
                getString(R.string.success), 
                Toast.LENGTH_SHORT).show();
            if (dialog != null) {
                dialog.dismiss();
            }
            finish();
        });
    }

    @Override
    public void onFailedShareRequest(String error) {
        Log.e(TAG, "Share request failed: " + error);
        runOnUiThread(() -> {
            Toast.makeText(getApplicationContext(), 
                "Failed to save link: " + error, 
                Toast.LENGTH_LONG).show();
            // Don't finish the activity on failure so user can retry
        });
    }

    
    @Override
    public void onAuthFailed(String error) {
        Log.e(TAG, "Auth failed: " + error);
        runOnUiThread(() -> {
            Toast.makeText(getApplicationContext(), "Authentication failed: " + error, 
                Toast.LENGTH_LONG).show();
            finish();
        });
    }
    
    @Override
    public void onSuccessfulCollectionsRequest(List<CollectionsRequest.CollectionData> collections) {
        Log.d(TAG, "Collections request successful. Received " + collections.size() + " collections");
        runOnUiThread(() -> {
            try {
                this.collectionsList = collections;
                
                if (collections.isEmpty()) {
                    Toast.makeText(getApplicationContext(), 
                        "No collections available", 
                        Toast.LENGTH_LONG).show();
                    return;
                }

                ArrayAdapter<CollectionsRequest.CollectionData> adapter = 
                    new ArrayAdapter<>(this, R.layout.dropdown_menu_popup_item, collections);
                
                collectionsDropdown.setAdapter(adapter);

                SharedPreferences preferences = getDefaultSharedPreferences(this);
                String defaultCollection = preferences.getString(
                    SettingsFragment.DEFAULT_COLLECTION_PREFERENCE_KEY, null);
                
                boolean collectionSet = false;
                if (defaultCollection != null) {
                    for (CollectionsRequest.CollectionData collection : collections) {
                        if (collection.getFullName().equals(defaultCollection)) {
                            collectionsDropdown.setText(collection.getFullName(), false);
                            collectionSet = true;
                            break;
                        }
                    }
                }
                
                if (!collectionSet && !collections.isEmpty()) {
                    collectionsDropdown.setText(collections.get(0).getFullName(), false);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error setting up collections", e);
                Toast.makeText(getApplicationContext(), 
                    "Error setting up collections: " + e.getMessage(), 
                    Toast.LENGTH_LONG).show();
            }
        });
    }
    
    @Override
    public void onFailedCollectionsRequest(String error) {
        Log.e(TAG, "Collections request failed: " + error);
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show());
    }
    
    @Override
    public void onSuccessfulTagsRequest(List<TagsRequest.TagData> tagsList) {
        Log.d(TAG, "Tags request successful. Received " + tagsList.size() + " tags");
        runOnUiThread(() -> tagsAdapter.updateTags(tagsList));
    }
    
    @Override
    public void onFailedTagsRequest(String error) {
        Log.e(TAG, "Tags request failed: " + error);
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show());
    }
}