package com.sbv.linkdroid.api;

import com.sbv.linkdroid.database.CollectionEntity;

import java.util.List;

public class CollectionsRequest {
    public static class ResponseData {
        private List<CollectionEntity> response;

        public List<CollectionEntity> getResponse(){
            return response;
        }

        public void setResponse(List<CollectionEntity> response){
            this.response = response;
        }

    }
}
