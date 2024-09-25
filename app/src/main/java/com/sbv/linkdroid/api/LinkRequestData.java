package com.sbv.linkdroid.api;

import java.util.List;

public class LinkRequestData {
    String url;
    CollectionsRequest.CollectionData collection;
    String name;
    String description;
    List<TagsRequest.TagData> tags;

    LinkRequestData(String url, CollectionsRequest.CollectionData collection, String name, String description, List<TagsRequest.TagData> tags){
        this.url = url;
        this.collection = collection;
        this.name = name;
        this.description = description;
        this.tags = tags;
    }
}
