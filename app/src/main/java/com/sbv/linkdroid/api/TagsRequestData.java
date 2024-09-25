package com.sbv.linkdroid.api;

import androidx.annotation.NonNull;

import java.util.List;

public class TagsRequestData {
    public static class APIResponse{
        private List<TagData> response;

        public List<TagData> getResponse(){
            return response;
        }

        public void setResponse(List<TagData> response){
            this.response = response;
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

