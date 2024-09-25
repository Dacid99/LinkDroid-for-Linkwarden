package com.sbv.linkdroid.api;

public interface APICallback {
    public void onSuccessfulShareRequest();
    public void onFailedShareRequest(String error);
    public void onSuccessfulCollectionsRequest(String[] categories);
    public void onFailedCollectionsRequest(String error);
    public void onAuthFailed(String error);
}
