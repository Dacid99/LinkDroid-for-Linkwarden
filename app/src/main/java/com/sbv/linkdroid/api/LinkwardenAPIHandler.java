package com.sbv.linkdroid.api;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.webkit.CookieManager;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

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
    private static final String LINK_API = "/api/v1/links/";
    private static final String TAGS_API = "/api/v1/tags/";
    private static final String COLLECTIONS_API = "/api/v1/collections/";
    private static final String CSRFCOOKIE_NAME = "__Host-next-auth.csrf-token";
    private static final String SESSIONCOOKIE_NAME = "__Secure-next-auth.session-token";

    private final OkHttpClient client = new OkHttpClient();
    private final String baseURL;
    private final Context context;
    private final APICallback callback;

    public LinkwardenAPIHandler(Context context){
        if (! (context instanceof APICallback)) {
            throw new IllegalArgumentException("context must implement APICallback!");
        }
        this.context = context;
        this.callback = (APICallback) context;
        SharedPreferences preferences = getDefaultSharedPreferences(context);
        this.baseURL = preferences.getString("BASE_URL", "");
    }

    private String[] getAuthenticationMethod(){
        String authKey = getCookie();
        String authMethod = "Cookie";
        if (authKey == null){
            authKey = getBearerToken();
            authMethod = "Authorization";
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
        String authToken = preferences.getString("AUTH_TOKEN", "");

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
                    String jsonResponse = response.body().string();
                    Log.d("apiResponse", jsonResponse);
                    Gson gson = new Gson();
                    try {
                        CollectionsRequest.ResponseData collectionsResponse = gson.fromJson(jsonResponse, CollectionsRequest.ResponseData.class);
                        callback.onSuccessfulCollectionsRequest(collectionsResponse.getResponse());
                    } catch (JsonSyntaxException e){
                        // Handle non-success response
                        callback.onFailedCollectionsRequest("Bad response for collections");
                    }
                } else {
                    // Handle non-success response
                    callback.onFailedCollectionsRequest("Failed to get collections");
                }
                Log.d("CategoriesAPI", Integer.toString(response.code()));
                response.close();
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
                    String jsonResponse = response.body().string();
                    Log.d("apiResponse", jsonResponse);
                    Gson gson = new Gson();
                    try {
                        TagsRequest.ResponseData tagsResponse = gson.fromJson(jsonResponse, TagsRequest.ResponseData.class);
                        callback.onSuccessfulTagsRequest(tagsResponse.getResponse());
                    } catch (JsonSyntaxException e){
                        // Handle non-success response
                        callback.onFailedTagsRequest("Bad response for tags");
                    }
                } else {
                    // Handle non-success response
                    callback.onFailedTagsRequest("Failed to get tags");
                }
                Log.d("TagsAPI", Integer.toString(response.code()));
                response.close();
            }
        });
    }

    public void makePostLinkRequest(String linkText, CollectionsRequest.CollectionData collection, String name, String description, List<TagsRequest.TagData> tags) {
        String[] auth = getAuthenticationMethod();
        if (auth[1] == null){
            callback.onFailedShareRequest("Failed: no authentication method found!");
            return;
        }
        String apiUrl = baseURL + LINK_API;

        Gson gson = new Gson();

        RequestBody requestBody = RequestBody.create(
                gson.toJson(new LinkRequestData(linkText, collection, name, description, tags)),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(apiUrl)
                .post(requestBody)
                .addHeader(auth[0], auth[1])
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NonNull IOException e) {
                // Handle failure
                callback.onFailedShareRequest("Failed to save link");
                Log.d("ShareAPI", "Error occured in web request" + e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response){
               if (response.isSuccessful()) {
                    callback.onSuccessfulShareRequest();
                    // Handle success response
                } else {
                    // Handle non-success response
                    callback.onFailedShareRequest("Failed to save link");
                }
                Log.d("ShareAPI", Integer.toString(response.code()));
                response.close();
            }
        });
    }
}

