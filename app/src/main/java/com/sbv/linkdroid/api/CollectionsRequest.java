package com.sbv.linkdroid.api;

import androidx.annotation.NonNull;

import java.util.List;

public class CollectionsRequest {
    public static class ResponseData {
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
        private ParentCollectionData parent;

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

        public ParentCollectionData getParent() {
            return parent;
        }

        public void setParent(ParentCollectionData parent) {
            this.parent = parent;
        }

        public String getFullName() {
            if (parent != null) {
                return parent.getName() + " > " + name;
            }
            else return name;
        }

        @NonNull
        public String toString() {
            return this.getFullName();
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()){
                return false;
            }
            CollectionData otherCollectionData = (CollectionData) object;
            return this.name.equals(otherCollectionData.getName());
        }
    }

    public static class ParentCollectionData {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
