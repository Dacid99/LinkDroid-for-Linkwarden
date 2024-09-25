package com.sbv.linkdroid.api;

public class LinkRequestData {
    String url;
    CollectionsResponseData.CollectionData collection;

    LinkRequestData(String url, CollectionsResponseData.CollectionData collection){
        this.url = url;
        this.collection = collection;
    }
}
