package com.jwd.vlcplayer.utils;

import android.util.Log;

import java.io.Serializable;


public class FileInfo implements Serializable, Cloneable {
    private static final String TAG = FileInfo.class.getSimpleName();
    public String videoTitle;
    public String cover;
    public String videoUrl;
    public String authorName;
    public String authorPhoto;
    public String fileId;
//    public String category;

    public FileInfo() {
    }


    @Override
    protected Object clone() {
        Object clone = null;
        try {
            clone = super.clone();
        } catch (CloneNotSupportedException e) {
            Log.e(TAG, "throw CloneNotSupportedException: " + e.getMessage());
        }
        return clone;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void setAuthorPhoto(String authorPhoto) {
        this.authorPhoto = authorPhoto;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

//    public void setCategory(String category) {
//        this.category = category;
//    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public String getCover() {
        return cover;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorPhoto() {
        return videoTitle;
    }

    public String getFileId() {
        return fileId;
    }

//    public String getCategory() {
//        return category;
//    }

}