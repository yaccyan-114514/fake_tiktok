package com.example.fake_tiktok;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class Draft {
    private long id;
    private String title;
    private String content;
    private List<Uri> images;
    private List<String> tags;
    private List<String> mentions;
    private Location location;
    private String locationAddress;
    private com.example.fake_tiktok.DAO.DraftLocationDao.LocationData locationData;
    
    public Draft() {
        images = new ArrayList<>();
        tags = new ArrayList<>();
        mentions = new ArrayList<>();
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public List<Uri> getImages() {
        return images;
    }
    
    public void setImages(List<Uri> images) {
        this.images = images != null ? images : new ArrayList<>();
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
    }
    
    public List<String> getMentions() {
        return mentions;
    }
    
    public void setMentions(List<String> mentions) {
        this.mentions = mentions != null ? mentions : new ArrayList<>();
    }
    
    public Location getLocation() {
        return location;
    }
    
    public void setLocation(Location location) {
        this.location = location;
    }
    
    public String getLocationAddress() {
        return locationAddress;
    }
    
    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }
    
    public com.example.fake_tiktok.DAO.DraftLocationDao.LocationData getLocationData() {
        return locationData;
    }
    
    public void setLocationData(com.example.fake_tiktok.DAO.DraftLocationDao.LocationData locationData) {
        this.locationData = locationData;
    }
    
    public static class Location {
        public double latitude;
        public double longitude;
        
        public Location(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}


