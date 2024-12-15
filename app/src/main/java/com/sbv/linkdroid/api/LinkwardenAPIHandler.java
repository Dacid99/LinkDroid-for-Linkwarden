package com.sbv.linkdroid.api;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.webkit.CookieManager;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sbv.linkdroid.SettingsFragment;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LinkwardenAPIHandler {
    private static final String TAG = "LinkwardenAPIHandler";
    private static final String LINK_API = "/api/v1/links/";
    private static final String TAGS_API = "/api/v1/tags/";
    private static final String COLLECTIONS_API = "/api/v1/collections/";
    private static final String CSRFCOOKIE_NAME = "__Host-next-auth.csrf-token";
    private static final String SESSIONCOOKIE_NAME = "__Secure-next-auth.session-token";

    private final OkHttpClient client = new OkHttpClient();
    private final String baseURL;
    private final Context context;
    private final APICallback callback;

    public LinkwardenAPIHandler(Context context, @NonNull APICallback callback){
        this.context = context;
        this.callback = callback;
        SharedPreferences preferences = getDefaultSharedPreferences(context);
        this.baseURL = preferences.getString("BASE_URL", "");
//        testAuthInBackground();
    }

//    private void testAuthInBackground(){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                    String[] auth = getAuthenticationMethod();
//                    Log.d("test", Arrays.toString(auth));
//                    try {
//                        TimeUnit.SECONDS.sleep(1);
//                    } catch (InterruptedException e) {
//                        Log.d("test", "Something interrupted sleep");
//                    }
//                }
//            }
//        }).start();
//    }

    private String[] getAuthenticationMethod(){
        String authKey = getBearerToken();
        String authMethod = "Authorization";
        if (authKey == null){
            authKey = getCookie();
            authMethod = "Cookie";
        }
        return new String[]{authMethod, authKey}; //authkey can be null!
    }

    private String getCookie(){
        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie(baseURL);
        if (cookies == null){
            return null;
        }

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

    private String getBearerToken(){
        SharedPreferences preferences = getDefaultSharedPreferences(context);
        String authToken = preferences.getString(SettingsFragment.AUTH_TOKEN_PREFERENCE_KEY, "");

        if (authToken.isEmpty()){
            return null;
        }
        return "Bearer " + authToken;
    }


    public void makeCollectionsRequest(){
        String[] auth = getAuthenticationMethod();
        if (auth[1] == null){
            callback.onAuthFailed("No auth method found!");
            return;
        }
        String apiUrl = baseURL + COLLECTIONS_API;

        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader(auth[0], auth[1])
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NonNull IOException e) {
                // Handle failure
                Log.d("Share", "Error occured in web request" + e);
                callback.onFailedCollectionsRequest("Failed to get collections");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException{
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String jsonResponse = response.body().string();

                        Gson gson = new Gson();
                        try {
                            CollectionsRequest.ResponseData collectionsResponse = gson.fromJson(jsonResponse, CollectionsRequest.ResponseData.class);
                            callback.onSuccessfulCollectionsRequest(collectionsResponse.getResponse());
                        } catch (JsonSyntaxException e) {
                            // Handle non-success response
                            callback.onFailedCollectionsRequest("Bad response for collections");
                        }
                    } else {
                        // Handle non-success response
                        callback.onFailedCollectionsRequest("Failed to get collections");
                    }
                    response.close();
                }
            }
        });
    }

    public void makeTagsRequest(){
        String[] auth = getAuthenticationMethod();
        if (auth[1] == null){
            callback.onAuthFailed("No auth method found!");
            return;
        }
        String apiUrl = baseURL + TAGS_API;

        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader(auth[0], auth[1])
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NonNull IOException e) {
                // Handle failure
                Log.d("Share", "Error occured in web request" + e);
                callback.onFailedCollectionsRequest("Failed to get tags");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException{
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String jsonResponse = response.body().string();
                        Gson gson = new Gson();
                        try {
                            TagsRequest.ResponseData tagsResponse = gson.fromJson(jsonResponse, TagsRequest.ResponseData.class);
                            callback.onSuccessfulTagsRequest(tagsResponse.getResponse());
                        } catch (JsonSyntaxException e) {
                            // Handle non-success response
                            callback.onFailedTagsRequest("Bad response for tags");
                        }
                    } else {
                        // Handle non-success response
                        callback.onFailedTagsRequest("Failed to get tags");
                    }
                    response.close();
                }
            }
        });
    }

    public void makePostLinkRequest(String linkText, CollectionsRequest.CollectionData collection, 
            String name, String description, List<TagsRequest.TagData> tags) {
        String[] auth = getAuthenticationMethod();
        if (auth[1] == null) {
            String error = "No authentication method found";
            Log.e(TAG, error);
            callback.onFailedShareRequest(error);
            return;
        }
        String apiUrl = baseURL + LINK_API;

        Gson gson = new Gson();
        LinkRequestData requestData = new LinkRequestData(linkText, collection, name, description, tags);
        String jsonBody = gson.toJson(requestData);
        
        // Log the request details (excluding sensitive info)
        Log.d(TAG, String.format("Making POST request to %s with data:%n" +
            "URL: %s%n" +
            "Collection ID: %s%n" +
            "Name: %s%n" +
            "Description length: %d%n" +
            "Tags count: %d",
            apiUrl, linkText, collection.getId(), name, 
            description != null ? description.length() : 0,
            tags != null ? tags.size() : 0));

        RequestBody requestBody = RequestBody.create(
                jsonBody,
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(apiUrl)
                .post(requestBody)
                .addHeader(auth[0], auth[1])
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NonNull IOException e) {
                String error = "Network error: " + e.getMessage();
                Log.e(TAG, "Request failed", e);
                callback.onFailedShareRequest(error);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    if (response.isSuccessful()) {
                        String responseBody = response.body() != null ? response.body().string() : null;
                        Log.d(TAG, "Successful response: " + responseBody);
                        callback.onSuccessfulShareRequest();
                    } else {
                        String responseBody = response.body() != null ? response.body().string() : null;
                        String error = String.format("Server error: %d - %s. Response: %s", 
                            response.code(), response.message(), responseBody);
                        Log.e(TAG, error);
                        callback.onFailedShareRequest(error);
                    }
                } catch (IOException e) {
                    String error = "Error reading response: " + e.getMessage();
                    Log.e(TAG, error, e);
                    callback.onFailedShareRequest(error);
                } finally {
                    response.close();
                }
            }
        });
    }
}

