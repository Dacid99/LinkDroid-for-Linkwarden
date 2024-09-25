package com.sbv.linkdroid.api;

import java.util.List;

public interface APICallback {
    void onSuccessfulShareRequest();
    void onFailedShareRequest(String error);
    void onSuccessfulCollectionsRequest(List<CollectionsResponseData.CollectionData> collectionsList) ;
    void onFailedCollectionsRequest(String error);
    void onAuthFailed(String error);
}
