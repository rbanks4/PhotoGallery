package com.bignerdranch.android.photogallery;

import android.net.Uri;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by RBanks on 10/17/2016.
 */

public class GalleryItem {

    public GalleryItem() {
    }

    public String getId() {
        return m_Id;
    }

    public void setId(String id) {
        m_Id = id;
    }

    public String getCaption() {
        return m_caption;
    }

    public void setCaption(String caption) {
        m_caption = caption;
    }
    public String getUrl() {
        return m_Url;
    }

    public void setUrl(String url) {
        m_Url = url;
    }

    private String m_caption;
    private String m_Id;
    private String m_Url;

    @Override
    public String toString() {
        return m_caption;
    }

    public void logInfo(){
        Log.i("GalleryItem", "Caption: " + m_caption
        + " ID: " + m_Id);
    }

    /**
     * page : 1
     * pages : 10
     * perpage : 100
     * total : 1000
     * photos : {[...]}
     */

    @SerializedName("photos")
    private GalleryList m_galleryList;

    public GalleryList getGalleryList() {
        return m_galleryList;
    }

    public void setGalleryList(GalleryList photos) {
        this.m_galleryList = photos;
    }

    public static class GalleryList {
        /**
         * id : 29905298203
         * owner : 93470584@N06
         * secret : 30b6bb7e8b
         * server : 5661
         * farm : 6
         * title : Overall-donna-tuta-intera-tutina-elegante-elastico-bicolor-velata ___
         * ispublic : 1
         * isfriend : 0
         * isfamily : 0
         */

        @SerializedName("photo")
        private List<Photo> m_photos;

        public List<Photo> getPhotos() {
            return m_photos;
        }

        public void setPhotos(List<Photo> photo) {
            this.m_photos = photo;
        }

        public static class Photo {
            @SerializedName("id")
            private String m_id;
            @SerializedName("owner")
            private String m_owner;
            @SerializedName("secret")
            private String m_secret;
            @SerializedName("server")
            private String m_server;
            @SerializedName("title")
            private String m_caption;
            @SerializedName("url_s")
            private String m_url;

            public String getUrl() {
                return m_url;
            }

            public void setUrl(String url) {
                m_url = url;
            }

            public String getId() {
                return m_id;
            }

            public void set_Id(String id) {
                this.m_id = id;
            }

            public String getOwner() {
                return m_owner;
            }

            public void setOwner(String owner) {
                this.m_owner = owner;
            }

            public String getSecret() {
                return m_secret;
            }

            public void setSecret(String secret) {
                this.m_secret = secret;
            }

            public String getServer() {
                return m_server;
            }

            public void setServer(String server) {
                this.m_server = server;
            }

            public String getCaption() {
                return m_caption;
            }

            public void setCaption(String caption) {
                this.m_caption = caption;
            }

            public String toString() {
                return m_caption;
            }

            public Uri getPhotoPageUri() {
                return Uri.parse("http://www.flickr.com/photos/")
                        .buildUpon()
                        .appendPath(m_owner)
                        .appendPath(m_id)
                        .build();
            }
        }
    }
}
