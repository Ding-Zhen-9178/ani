package com.ani.anitrack.entity;

public class Anime {
    private Integer id;
    private Integer userId;
    private String name;
    private Integer currentEp;
    private Integer totalEp;
    private String websiteUrl;
    private Integer updateDay;
    private Integer rating;
    private String lastWatchDate;
    private String status;
    private String coverUrl;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getCurrentEp() { return currentEp; }
    public void setCurrentEp(Integer currentEp) { this.currentEp = currentEp; }
    public Integer getTotalEp() { return totalEp; }
    public void setTotalEp(Integer totalEp) { this.totalEp = totalEp; }
    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }
    public Integer getUpdateDay() { return updateDay; }
    public void setUpdateDay(Integer updateDay) { this.updateDay = updateDay; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getLastWatchDate() { return lastWatchDate; }
    public void setLastWatchDate(String lastWatchDate) { this.lastWatchDate = lastWatchDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
}