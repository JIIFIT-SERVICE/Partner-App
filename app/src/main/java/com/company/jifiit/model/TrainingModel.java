package com.company.jifiit.model;

public class TrainingModel {
    private String 	id;
    private String 	thumbnail, title;
    private String 	description;
    private String 	uploaded_date;
    private String 	video_path;
    private String fileType;
    private int index;

    public TrainingModel(String id, String thumbnail, String description, String uploaded_date, String video_path, String title, String fileType,int index) {
        this.id = id;
        this.thumbnail = thumbnail;
        this.description = description;
//        this.views = views;
//        this.likes = likes;
//        this.dislikes = dislikes;
        this.uploaded_date = uploaded_date;
        this.video_path =video_path;
        this.title = title;
        this.fileType = fileType;
        this.index = index;
    }


    public String getId() {
        return id;
    }

    public String getThumbnail() {
        return thumbnail;
    }
    public String getDescription() {
        return description;
    }

//    public String getViews() {
//        return views;
//    }
//
//    public String getLikes() {
//        return likes;
//    }
//
//    public String getDislikes() {
//        return dislikes;
//    }

    public String getUploaded_date() {
        return uploaded_date;
    }
    public String getVideo_path() {
        return video_path;
    }


    public String getTitle() {
        return title;
    }

    public String getFileType() {
        return fileType;
    }
    public int getIndex() { // ✅ Getter for index
        return index;
    }
}
