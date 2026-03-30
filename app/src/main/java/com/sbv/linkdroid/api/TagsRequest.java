package com.sbv.linkdroid.api;

import androidx.annotation.NonNull;

import java.util.List;

public class TagsRequest {
    public static class ResponseData {
        private TagsData data;

        public TagsData getTagsData(){
            return this.data;
        }

        public void setTagsData(TagsData tagsData){
            this.data = tagsData;
        }

    }

    public static class TagsData{
        private List<TagData> tags;

        public List<TagData> getTags(){
            return this.tags;
        }

        public void setTags(List<TagData> tags){
            this.tags = tags;
        }
    }

    public static class TagData {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @NonNull
        public String toString() {
            return this.name;
        }
    }

}

