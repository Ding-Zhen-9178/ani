package com.ani.anitrack.entity;

public class UrlSuggestion {
    private Integer id;
    private Integer userId;
    private String label;
    private String url;
    private String searchUrl;
    private Boolean isPreset;
    private Boolean pinned;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getSearchUrl() { return searchUrl; }
    public void setSearchUrl(String searchUrl) { this.searchUrl = searchUrl; }
    public Boolean getIsPreset() { return isPreset; }
    public void setIsPreset(Boolean isPreset) { this.isPreset = isPreset; }
    public Boolean getPinned() { return pinned; }
    public void setPinned(Boolean pinned) { this.pinned = pinned; }
}
