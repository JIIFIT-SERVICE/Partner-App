package com.company.jifiit.model;

public class RewardModel {
    private int imageResource;
    private String level;

    public RewardModel(int imageResource, String level) {
        this.imageResource = imageResource;
        this.level = level;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}