package com.sbv.linkdroid.api;

public class LinkRequestData {
    String url;
    CollectionsRequestData.CollectionData collection;
    String name;
    String description;
    TagsRequestData.TagData[] tags;

    LinkRequestData(String url, CollectionsRequestData.CollectionData collection, String name, String description, TagsRequestData.TagData[] tags){
        this.url = url;
        this.collection = collection;
        this.name = name;
        this.description = description;
        this.tags = tags;
    }
}
