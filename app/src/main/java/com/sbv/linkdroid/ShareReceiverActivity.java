package com.sbv.linkdroid;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.sbv.linkdroid.api.APICallback;
import com.sbv.linkdroid.api.CollectionsRequest;
import com.sbv.linkdroid.api.LinkwardenAPIHandler;
import com.sbv.linkdroid.api.TagsRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class ShareReceiverActivity extends AppCompatActivity implements APICallback {

    private LinkwardenAPIHandler linkwardenAPIHandler;
    AlertDialog dialog;
    private Spinner collectionsDropdown;
    private AutoCompleteTextView tagsInput;
    ChipGroup tagsList;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        linkwardenAPIHandler = new LinkwardenAPIHandler(this, this);

        if (getIntent() != null) {
            handleIntent(getIntent());
        }
    }

    private void handleIntent(@NonNull Intent intent){
        if ( Intent.ACTION_SEND.equals(intent.getAction()) && intent.getType() != null ){
            if ("text/plain".equals(intent.getType())){
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null){
                    showDialog(sharedText);
                }
            }
        } else if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Intent.ACTION_PROCESS_TEXT.equals(intent.getAction()) ){
            CharSequence sharedText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
            if (sharedText != null){
                showDialog(sharedText.toString());
            }
        }
    }

    private void showDialog(String sharedText) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView);

        EditText sharedTextEdit = dialogView.findViewById(R.id.sharedTextEdit);
        sharedTextEdit.setText(sharedText.trim());

        EditText nameEdit = dialogView.findViewById(R.id.nameEdit);
        EditText descriptionEdit = dialogView.findViewById(R.id.descriptionEdit);

        tagsList = dialogView.findViewById(R.id.tagsList);

        tagsInput = dialogView.findViewById(R.id.tagsInput);
        tagsInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().endsWith("\n")){
                    addTag();
                }
            }
        });
        linkwardenAPIHandler.makeTagsRequest();

        collectionsDropdown = dialogView.findViewById(R.id.collectionsDropdown);
        linkwardenAPIHandler.makeCollectionsRequest();

        MaterialButton sendButton = dialogView.findViewById(R.id.sendButton);

        sendButton.setOnClickListener(v -> {
            String editedSharedText =  sharedTextEdit.getText().toString();
            CollectionsRequest.CollectionData selectedCollection = (CollectionsRequest.CollectionData) collectionsDropdown.getSelectedItem();
            String name = nameEdit.getText().toString();
            if (getDefaultSharedPreferences(this).getBoolean("NAME_REQUIRED", false) && name.isBlank()){
                Toast.makeText(getApplicationContext(), getString(R.string.name_required_warning), Toast.LENGTH_SHORT).show();
                return;
            }
            String description = descriptionEdit.getText().toString();
            List<TagsRequest.TagData> selectedTags = new ArrayList<>();
            for (int i=0; i<tagsList.getChildCount(); i++){
                TagsRequest.TagData selectedTagData = new TagsRequest.TagData();
                View tagChip = tagsList.getChildAt(i);
                String tag = ((Chip) tagChip).getText().toString();
                selectedTagData.setName(tag);
                selectedTags.add(selectedTagData);
            }
            linkwardenAPIHandler.makePostLinkRequest(editedSharedText, selectedCollection, name, description, selectedTags);//.toArray(new TagsRequestData.TagData[0])
        });

        dialogBuilder.setOnDismissListener( v -> finish() );

        dialog = dialogBuilder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void addTag(){
        String newTag = tagsInput.getText().toString();
        if (newTag.trim().isEmpty()){
            return;
        }
        Chip newChip = new Chip(this);
        newChip.setText(newTag);
        newChip.setCloseIconVisible(true);
        newChip.setOnCloseIconClickListener(v -> tagsList.removeView(newChip));
        tagsList.addView(newChip);
        tagsInput.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialog!= null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    public void onSuccessfulShareRequest() {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), getString(R.string.success), Toast.LENGTH_SHORT).show());
        finish();
    }

    @Override
    public void onFailedShareRequest(String error) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), error , Toast.LENGTH_LONG).show());
        finish();
    }

    @Override
    public void onSuccessfulCollectionsRequest(List<CollectionsRequest.CollectionData> collectionsList) {
        runOnUiThread(() -> {
            ArrayAdapter<CollectionsRequest.CollectionData> adapter = new ArrayAdapter<>(this, R.layout.spinner_dropdown_item, collectionsList);
            collectionsDropdown.setAdapter(adapter);

            SharedPreferences preferences = getDefaultSharedPreferences(this);
            String defaultCollection = preferences.getString(SettingsFragment.DEFAULT_COLLECTION_PREFERENCE_KEY, null);
            if (defaultCollection != null){

                CollectionsRequest.CollectionData defaultCollectionData = new CollectionsRequest.CollectionData();
                defaultCollectionData.setName(defaultCollection);
                int defaultCollectionIndex = collectionsList.indexOf(defaultCollectionData);

                if (defaultCollectionIndex != -1){
                    collectionsDropdown.setSelection(defaultCollectionIndex);
                }
            }
        });
    }

    @Override
    public void onFailedCollectionsRequest(String error) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), error , Toast.LENGTH_LONG).show());
    }

    @Override
    public void onAuthFailed(String error){
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), error , Toast.LENGTH_LONG).show());
    }

    @Override
    public void onSuccessfulTagsRequest(List<TagsRequest.TagData> tagsList) {
        Log.d("APIPResponse", tagsList.toString());
        runOnUiThread(() -> {
            ArrayAdapter<TagsRequest.TagData> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, tagsList);
            tagsInput.setAdapter(adapter);
        });
    }

    @Override
    public void onFailedTagsRequest(String error) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), error , Toast.LENGTH_LONG).show());
    }
}
