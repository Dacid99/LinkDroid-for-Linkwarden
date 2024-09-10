package com.linkwarden.android;

public class LinkRequestData {
    String url;
    String[] tags;

    LinkRequestData(String url, String[] tags){
        this.url = url;
        this.tags = tags;
    }
}
