package com.sbv.linkdroid.api;

import androidx.annotation.NonNull;

import java.util.List;

public class CollectionsRequestData {
    public static class APIResponse {
        private List<CollectionData> response;

        public List<CollectionData> getResponse(){
            return response;
        }

        public void setResponse(List<CollectionData> response){
            this.response = response;
        }

    }


    public static class CollectionData {
        private int id;
        private String name;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

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
