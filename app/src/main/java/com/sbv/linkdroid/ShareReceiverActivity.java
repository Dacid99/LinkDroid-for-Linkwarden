package com.sbv.linkdroid;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ShareReceiverActivity extends AppCompatActivity {
    private static final String LINK_API = "/api/v1/links/";
    private static final String CSRFCOOKIE_NAME = "__Host-next-auth.csrf-token";
    private static final String SESSIONCOOKIE_NAME = "__Secure-next-auth.session-token";

    private final OkHttpClient client = new OkHttpClient();
    private String baseURL;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = getDefaultSharedPreferences(this);
        baseURL = preferences.getString("BASE_URL", "");

        if (getIntent() != null) {
            handleIntent(getIntent());
        }

        finish();
    }
    private void handleIntent(Intent intent){
        if ( ( Intent.ACTION_SEND.equals(intent.getAction()) || Intent.ACTION_PROCESS_TEXT.equals(intent.getAction()) ) && intent.getType() != null){
            if ("text/plain".equals(intent.getType())){
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null){
                    sendPostRequest(sharedText);
                }
            }
        }
    }

    private void sendPostRequest(String text){
        String authKey = getCookie();
        String authMethod = "Cookie";
        if (authKey == null){
            authKey = getBearerToken();
            authMethod = "Authorization";
            if (authKey == null){
                Toast.makeText(this, "Failed: no authentication method found!", Toast.LENGTH_LONG).show();
                return;
            }
        }

        String apiUrl = baseURL + LINK_API;

        Gson gson = new Gson();
        RequestBody requestBody = RequestBody.create(
                gson.toJson(new LinkRequestData(text, new String[]{})),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(apiUrl)
                .post(requestBody)
                .addHeader(authMethod, authKey)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NonNull IOException e) {
                // Handle failure
                Log.d("Error", "Error occured in web request" + e);
                runOnUiThread(() -> Toast.makeText(ShareReceiverActivity.this, "Failed to send data", Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response){
                if (response.isSuccessful()) {
                    // Handle success response
                    runOnUiThread(() -> Toast.makeText(ShareReceiverActivity.this, "Data sent successfully", Toast.LENGTH_SHORT).show());
                    Log.d("Share", Integer.toString(response.code()));
                } else {
                    // Handle non-success response
                    runOnUiThread(() -> Toast.makeText(ShareReceiverActivity.this, "Failed: " + response.message(), Toast.LENGTH_LONG).show());
                    Log.d("Share", Integer.toString(response.code()));
                }
            }
        });
    }

    private String getCookie(){
        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie(baseURL);
        if (cookies != null){
            String[] cookieArray = cookies.split("; ");
            StringBuilder fullCookieBuilder = new StringBuilder();
            for (String cookie : cookieArray){
                if (cookie.startsWith(SESSIONCOOKIE_NAME) || cookie.startsWith(CSRFCOOKIE_NAME)) {
                    fullCookieBuilder.append(cookie).append("; ");
                }
            }
            String fullCookie = fullCookieBuilder.toString();
            if (fullCookie.isEmpty()){
                return null;
            }
            return fullCookie;
        }
        return null;

    }

    private String getBearerToken(){
        SharedPreferences preferences = getDefaultSharedPreferences(this);
        String authToken = preferences.getString("AUTH_TOKEN", "");

        if (authToken.isEmpty()){
            return null;
        }
        return "Bearer " + authToken;


    }
}
