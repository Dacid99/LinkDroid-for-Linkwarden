package com.sbv.linkdroid.api;

import java.util.List;

public interface APICallback {
    void onSuccessfulShareRequest();
    void onFailedShareRequest(String error);
    void onSuccessfulCollectionsRequest(List<CollectionsRequestData.CollectionData> collectionsList) ;
    void onFailedCollectionsRequest(String error);
    void onSuccessfulTagsRequest(List<TagsRequestData.TagData> collectionsList) ;
    void onFailedTagsRequest(String error);
    void onAuthFailed(String error);
}
