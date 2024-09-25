package com.sbv.linkdroid.api;

import java.security.IdentityScope;

public class LinkRequestData {
    String url;
    CollectionsResponseData.CollectionData collection;
    String name;
    String description;

    LinkRequestData(String url, CollectionsResponseData.CollectionData collection, String name, String description){
        this.url = url;
        this.collection = collection;
        this.name = name;
        this.description = description;
    }
}
