package com.sbv.linkdroid.api;

import com.sbv.linkdroid.database.CollectionEntity;
import com.sbv.linkdroid.database.TagEntity;

import java.util.List;

public interface APICallback {
    void onSuccessfulShareRequest();
    void onFailedShareRequest(String error);
    void onSuccessfulCollectionsRequest(List<CollectionEntity> collectionsList) ;
    void onFailedCollectionsRequest(String error);
    void onSuccessfulTagsRequest(List<TagEntity> collectionsList) ;
    void onFailedTagsRequest(String error);
    void onAuthFailed(String error);
}
