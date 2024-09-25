package com.sbv.linkdroid;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.sbv.linkdroid.api.APICallback;
import com.sbv.linkdroid.api.CollectionsRequestData;
import com.sbv.linkdroid.api.LinkwardenAPIHandler;
import com.sbv.linkdroid.api.TagsRequestData;

import java.util.ArrayList;
import java.util.List;


public class ShareReceiverActivity extends AppCompatActivity implements APICallback {

    private LinkwardenAPIHandler linkwardenAPIHandler;
    AlertDialog dialog;
    private Spinner collectionsDropdown;
    private AutoCompleteTextView tagsInput;
    ChipGroup tagsList;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        linkwardenAPIHandler = new LinkwardenAPIHandler(this);

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
        tagsInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction()==KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER){
                    addTag();
                    return true;
                }
                return false;
            }
        });

        collectionsDropdown = dialogView.findViewById(R.id.collectionsDropdown);
        linkwardenAPIHandler.makeCollectionsRequest();

        MaterialButton sendButton = dialogView.findViewById(R.id.sendButton);

        sendButton.setOnClickListener(v -> {
            String editedSharedText =  sharedTextEdit.getText().toString();
            CollectionsRequestData.CollectionData selectedCollection = (CollectionsRequestData.CollectionData) collectionsDropdown.getSelectedItem();
            String name = nameEdit.getText().toString();
            String description = descriptionEdit.getText().toString();
            List<TagsRequestData.TagData> selectedTags = new ArrayList<>();
            for (int i=0; i<tagsList.getChildCount(); i++){
                TagsRequestData.TagData selectedTagData = new TagsRequestData.TagData();
                View tagChip = tagsList.getChildAt(i);
                String tag = ((Chip) tagChip).getText().toString();
                selectedTagData.setName(tag);
                selectedTags.add(selectedTagData);
            }
            linkwardenAPIHandler.makePostLinkRequest(editedSharedText, selectedCollection, name, description, selectedTags.toArray(new TagsRequestData.TagData[0]));
        });

        dialogBuilder.setOnDismissListener( v -> finish() );

        dialog = dialogBuilder.create();
        dialog.show();
        Log.d("debug", "end of showDialofg reached");
    }

    private void addTag(){
        String newTag = tagsInput.getText().toString();
        if (newTag.trim().isEmpty()){
            return;
        }
        Chip newChip = new Chip(this);
        newChip.setText(newTag);
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
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Link saved successfully", Toast.LENGTH_SHORT).show());
        finish();
    }

    @Override
    public void onFailedShareRequest(String error) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), error , Toast.LENGTH_LONG).show());
        finish();
    }

    @Override
    public void onSuccessfulCollectionsRequest(List<CollectionsRequestData.CollectionData> collectionsList) {
        Log.d("APIPResponse", collectionsList.toString());
        runOnUiThread(() -> {
            ArrayAdapter<CollectionsRequestData.CollectionData> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, collectionsList);
            collectionsDropdown.setAdapter(adapter);
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
    public void onSuccessfulTagsRequest(List<TagsRequestData.TagData> tagsList) {
        Log.d("APIPResponse", tagsList.toString());
        runOnUiThread(() -> {
            ArrayAdapter<TagsRequestData.TagData> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, tagsList);
            tagsInput.setAdapter(adapter);
        });
    }

    @Override
    public void onFailedTagsRequest(String error) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), error , Toast.LENGTH_LONG).show());
    }
}
