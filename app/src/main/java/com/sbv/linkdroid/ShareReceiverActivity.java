package com.sbv.linkdroid;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.sbv.linkdroid.api.APICallback;
import com.sbv.linkdroid.api.LinkwardenAPIHandler;


public class ShareReceiverActivity extends AppCompatActivity implements APICallback {

    private LinkwardenAPIHandler linkwardenAPIHandler;
    private Spinner collections;
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
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialog = inflater.inflate(R.layout.dialog, null);

        EditText sharedTextEdit = dialog.findViewById(R.id.sharedTextEdit);
        sharedTextEdit.setText(sharedText.trim());

        collections = dialog.findViewById(R.id.collectionsDropdown);
        linkwardenAPIHandler.makeCollectionsRequest();

        MaterialButton sendButton = dialog.findViewById(R.id.sendButton);
        sendButton.setOnClickListener(v -> linkwardenAPIHandler.makePostRequest(sharedTextEdit.getText().toString(), collections.getSelectedItem().toString()));
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
    public void onSuccessfulCollectionsRequest(String[] categories) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        collections.setAdapter(adapter);
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
