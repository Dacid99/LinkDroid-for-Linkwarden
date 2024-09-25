package com.sbv.linkdroid;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.sbv.linkdroid.api.APICallback;
import com.sbv.linkdroid.api.CollectionsResponseData;
import com.sbv.linkdroid.api.LinkwardenAPIHandler;

import java.util.List;


public class ShareReceiverActivity extends AppCompatActivity implements APICallback {

    private LinkwardenAPIHandler linkwardenAPIHandler;
    AlertDialog dialog;
    private Spinner collectionsDropdown;
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

        collectionsDropdown = dialogView.findViewById(R.id.collectionsDropdown);
        linkwardenAPIHandler.makeCollectionsRequest();

        MaterialButton sendButton = dialogView.findViewById(R.id.sendButton);

        sendButton.setOnClickListener(v -> {
            String editedSharedText =  sharedTextEdit.getText().toString();
            CollectionsResponseData.CollectionData selectedCollection = (CollectionsResponseData.CollectionData) collectionsDropdown.getSelectedItem();
            String name = nameEdit.getText().toString();
            String description = descriptionEdit.getText().toString();
            linkwardenAPIHandler.makePostLinkRequest(editedSharedText, selectedCollection, name, description);
        });

        dialogBuilder.setOnDismissListener( v -> finish() );

        dialog = dialogBuilder.create();
        dialog.show();
        Log.d("debug", "end of showDialofg reached");
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
    public void onSuccessfulCollectionsRequest(List<CollectionsResponseData.CollectionData> collectionsList) {
        Log.d("APIPResponse", collectionsList.toString());
        runOnUiThread(() -> {
            ArrayAdapter<CollectionsResponseData.CollectionData> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, collectionsList);
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
}
