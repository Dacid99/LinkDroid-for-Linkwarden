package com.linkwarden.android;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.preference.Preference;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ShareReceiverActivity extends AppCompatActivity {

    private final OkHttpClient client = new OkHttpClient();
    private String baseURL;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = getDefaultSharedPreferences(this);
        baseURL = preferences.getString("BASE_URL", "");

        handleSendIntent(getIntent());

        finish();
    }
    private void handleSendIntent(Intent intent){
        if (Intent.ACTION_SEND.equals(intent.getAction()) && intent.getType() != null){
            if ("text/plain".equals(intent.getType())){
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null){
                    sendPostRequest(sharedText);
                }
            }
        }
    }
    private void sendPostRequest(String text){
        String url = baseURL + "/api/v1/links";

        RequestBody requestBody = new FormBody.Builder()
                .add("url", text)
                .add("tags", "[]")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NonNull IOException e) {
                // Handle failure
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ShareReceiverActivity.this, "Failed to send data", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response){
                if (response.isSuccessful()) {
                    // Handle success response
                    runOnUiThread(() -> Toast.makeText(ShareReceiverActivity.this, "Data sent successfully", Toast.LENGTH_SHORT).show());
                } else {
                    // Handle non-success response
                    runOnUiThread(() -> Toast.makeText(ShareReceiverActivity.this, "Failed: " + response.message(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
