package com.sbv.linkdroid.api;

import com.sbv.linkdroid.database.CollectionEntity;
import com.sbv.linkdroid.database.TagEntity;

import java.util.List;

public class LinkRequestData {
    String url;
    CollectionEntity collection;
    String name;
    String description;
    List<TagEntity> tags;

    LinkRequestData(String url, CollectionEntity collection, String name, String description, List<TagEntity> tags){
        this.url = url;
        this.collection = collection;
        this.name = name;
        this.description = description;
        this.tags = tags;
    }
}
