package com.sbv.linkdroid.api;

import com.sbv.linkdroid.database.TagEntity;

import java.util.List;

public class TagsRequest {
    public static class ResponseData {
        private List<TagEntity> response;

        public List<TagEntity> getResponse(){
            return response;
        }

        public void setResponse(List<TagEntity> response){
            this.response = response;
        }

    }
}

