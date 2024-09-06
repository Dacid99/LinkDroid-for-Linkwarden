package com.hortusfox.android;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class ShareReceiverActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        handleSendIntent(getIntent());

        finish();
    }
    private void handleSendIntent(Intent intent){

    }
}
